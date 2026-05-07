package world.clan.app.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

  fun connect(activity: ComponentActivity) {
    if (_state.value.phase == ConnectUiState.Phase.Connecting) return
    viewModelScope.launch {
      _state.update {
        it.copy(phase = ConnectUiState.Phase.Connecting, errorMessage = null)
      }
      val existing = sessionStore.read()
      val result = if (existing != null) {
        mwa.reauthorize(activity, existing.mwaAuthToken)
      } else {
        mwa.connect(activity)
      }
      handle(result)
    }
  }

  fun disconnect() {
    sessionStore.clear()
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
        // If the user declined a cold-launch reauthorize the stored
        // session is no longer trustworthy — clear it so the next
        // launch is a clean Connect.
        if (_state.value.pendingVerification) sessionStore.clear()
        _state.update {
          it.copy(
            phase = ConnectUiState.Phase.Idle,
            solanaPubkeyBase58 = null.takeIf { _state.value.pendingVerification }
              ?: it.solanaPubkeyBase58,
            pendingVerification = false,
            errorMessage = if (it.pendingVerification)
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
        if (_state.value.pendingVerification) sessionStore.clear()
        _state.update {
          it.copy(
            phase = ConnectUiState.Phase.Error,
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
