package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.clan.app.data.SessionStore
import world.clan.app.wallet.DeviceClass

data class CodexUiState(
  val deviceClass: DeviceClass,
  val solanaPubkey: String?,
  val build: String,
)

/**
 * Slice 1 Codex screen — read-only summary of identity + device + about.
 * No EVM paste, no profile selector, no validation. The Solana pubkey is
 * the only identifier this app cares about.
 */
class CodexViewModel(
  private val sessionStore: SessionStore,
  deviceClass: DeviceClass,
) : ViewModel() {

  private val _state = MutableStateFlow(
    CodexUiState(
      deviceClass = deviceClass,
      solanaPubkey = sessionStore.read()?.solanaPubkeyBase58,
      build = "v 0.1.0 · slice I",
    ),
  )
  val state: StateFlow<CodexUiState> = _state.asStateFlow()

  fun disconnect() {
    sessionStore.clear()
    _state.value = _state.value.copy(solanaPubkey = null)
  }
}
