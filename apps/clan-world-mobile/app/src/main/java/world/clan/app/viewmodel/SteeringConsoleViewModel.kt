package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SendPhase { Idle, Signing, Queued, Failed }

data class SteeringConsoleUiState(
  val targetClanId: Int,
  val draft: String = "",
  val phase: SendPhase = SendPhase.Idle,
  val errorMessage: String? = null,
)

/**
 * Slice 4c: SteeringConsole. The owner's whisper composer — drafts a
 * single-line steering message addressed to one of their elders.
 *
 * No real Convex mutation exists yet (per BUILD_PLAN — whisper send
 * needs a backend mutation that doesn't exist). This view-model holds
 * draft state; the screen handles the MWA sign + the (stubbed) send.
 */
class SteeringConsoleViewModel(
  initialClanId: Int,
) : ViewModel() {

  private val _state = MutableStateFlow(
    SteeringConsoleUiState(targetClanId = initialClanId),
  )
  val state: StateFlow<SteeringConsoleUiState> = _state.asStateFlow()

  fun setDraft(text: String) {
    _state.update { it.copy(draft = text) }
  }

  fun setTarget(clanId: Int) {
    _state.update { it.copy(targetClanId = clanId) }
  }

  fun setPhase(p: SendPhase, error: String? = null) {
    _state.update { it.copy(phase = p, errorMessage = error) }
  }
}
