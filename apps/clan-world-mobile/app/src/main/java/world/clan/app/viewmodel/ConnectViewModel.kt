package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.LineageStore
import world.clan.app.data.Session
import world.clan.app.data.SessionStore
import world.clan.app.wallet.MwaClient
import world.clan.app.wallet.MwaResult

data class ConnectUiState(
  val phase: Phase = Phase.Idle,
  val solanaPubkeyBase58: String? = null,
  /**
   * True when a stored session exists but has crossed the freshness
   * threshold; the Connect screen should auto-trigger reauthorize on
   * mount. See [ConnectViewModel.SESSION_FRESH_THRESHOLD_MS].
   */
  val pendingVerification: Boolean = false,
  val errorMessage: String? = null,
) {
  enum class Phase { Idle, Connecting, Connected, Error }
}

/**
 * Drives the Connect screen.
 *
 * Cold-launch routing rules:
 * - **No session**: phase = Idle. User taps Connect → fresh authorize.
 * - **Session, fresh** (< 24h since last successful connect): phase =
 *   Connected. ClanWorldApp routes straight to Hearth — fast cold launch.
 * - **Session, stale** (≥ 24h): phase = Idle, `pendingVerification = true`.
 *   Connect screen auto-triggers reauthorize on first composition. If
 *   the wallet still recognizes the auth token, this is a brief Phantom
 *   flash followed by silent transition to Hearth. If the token was
 *   revoked, the user lands back on Connect with the failure message
 *   ("Authorization declined.", etc.) and a working manual retry.
 *
 * The 24h gate is a heuristic — long enough that daily users never see
 * the verification UI, short enough that a wallet-side disconnect is
 * caught within a day.
 */
class ConnectViewModel(
  private val mwa: MwaClient,
  private val sessionStore: SessionStore,
  private val lineageStore: LineageStore? = null,
) : ViewModel() {

  private val _state = MutableStateFlow(initialState())
  val state: StateFlow<ConnectUiState> = _state.asStateFlow()

  private fun initialState(): ConnectUiState {
    val s = sessionStore.read() ?: return ConnectUiState()
    val ageMs = System.currentTimeMillis() - s.lastConnectAtEpochMs
    val isFresh = s.lastConnectAtEpochMs > 0 && ageMs < SESSION_FRESH_THRESHOLD_MS
    return ConnectUiState(
      phase = if (isFresh) ConnectUiState.Phase.Connected else ConnectUiState.Phase.Idle,
      solanaPubkeyBase58 = s.solanaPubkeyBase58,
      pendingVerification = !isFresh,
    )
  }

  fun connect(sender: ActivityResultSender) {
    if (_state.value.phase == ConnectUiState.Phase.Connecting) return
    viewModelScope.launch {
      _state.update {
        it.copy(phase = ConnectUiState.Phase.Connecting, errorMessage = null)
      }
      val existing = sessionStore.read()
      val result = runCatching {
        if (existing != null) mwa.reauthorize(sender, existing.mwaAuthToken)
        else mwa.connect(sender)
      }.getOrElse { MwaResult.Error(it) }
      handle(result)
    }
  }

  fun disconnect() {
    sessionStore.clear()
    // Lineage lives in a separate prefs file from sessionStore; clear
    // it on disconnect so the next-connected wallet doesn't see the
    // previous wallet's owner-action history.
    lineageStore?.clear()
    _state.value = ConnectUiState()
  }

  private fun handle(result: MwaResult<world.clan.app.wallet.MwaSession>) {
    when (result) {
      is MwaResult.Ok -> {
        val s = result.value
        val existing = sessionStore.read()
        sessionStore.save(
          Session(
            solanaPubkeyBase58 = s.solanaPubkeyBase58,
            mwaAuthToken = s.authToken,
            walletLabel = s.walletLabel,
            selectedClanId = existing?.selectedClanId,
            lastConnectAtEpochMs = System.currentTimeMillis(),
          ),
        )
        _state.update {
          it.copy(
            phase = ConnectUiState.Phase.Connected,
            solanaPubkeyBase58 = s.solanaPubkeyBase58,
            pendingVerification = false,
            errorMessage = null,
          )
        }
      }
      MwaResult.UserDeclined -> {
        // Any failed authorize/reauthorize invalidates the stored session.
        // Clearing here means the next "Open Seed Vault" tap is a fresh
        // mwa.connect() — not another doomed reauthorize against a stale
        // auth token. v0.2.0 demo regression: previously this only cleared
        // on cold-launch pendingVerification, so a manual tap that hit a
        // wallet-side-revoked token would loop ("tap → bounce → tap →
        // bounce") forever.
        val wasVerifying = _state.value.pendingVerification
        sessionStore.clear()
        _state.update {
          it.copy(
            phase = ConnectUiState.Phase.Idle,
            solanaPubkeyBase58 = null,
            pendingVerification = false,
            errorMessage = if (wasVerifying)
              "Your sigil could not be verified — please reconnect."
            else
              "Authorization declined.",
          )
        }
      }
      MwaResult.WalletNotFound ->
        _state.update {
          it.copy(
            phase = ConnectUiState.Phase.Error,
            pendingVerification = false,
            errorMessage = "No MWA-compatible Solana wallet installed.",
          )
        }
      is MwaResult.Error -> {
        // Same rationale as UserDeclined: clear so we never retry a token
        // the wallet has rejected. A real network/IPC error will simply
        // result in a fresh authorize on the user's next tap.
        sessionStore.clear()
        _state.update {
          it.copy(
            phase = ConnectUiState.Phase.Error,
            solanaPubkeyBase58 = null,
            pendingVerification = false,
            errorMessage = result.cause.message ?: "MWA error.",
          )
        }
      }
    }
  }

  companion object {
    /**
     * How long a stored session is trusted without reauthorize-verifying.
     * Sessions older than this are routed through the Connect screen on
     * cold launch which auto-triggers reauthorize.
     */
    const val SESSION_FRESH_THRESHOLD_MS: Long = 24L * 60L * 60L * 1000L
  }
}
