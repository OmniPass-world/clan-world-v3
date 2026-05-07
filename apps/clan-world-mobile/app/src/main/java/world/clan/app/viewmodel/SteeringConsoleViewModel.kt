package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.clan.app.data.SessionStore

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
 * Drafts are persisted through process death via SessionStore (key
 * "draft:steer:<clanId>"); cleared on successful submit (Queued phase).
 *
 * No real Convex mutation exists yet (per BUILD_PLAN — whisper send
 * needs a backend mutation that doesn't exist). This view-model holds
 * draft state; the screen handles the MWA sign + the (stubbed) send.
 */
class SteeringConsoleViewModel(
  initialClanId: Int,
  private val sessionStore: SessionStore? = null,
) : ViewModel() {

  private val draftScope = "steer:$initialClanId"

  private val _state = MutableStateFlow(
    SteeringConsoleUiState(
      targetClanId = initialClanId,
      draft = sessionStore?.getDraft(draftScope, "body").orEmpty(),
    ),
  )
  val state: StateFlow<SteeringConsoleUiState> = _state.asStateFlow()

  fun setDraft(text: String) {
    _state.update { it.copy(draft = text) }
    sessionStore?.setDraft(draftScope, "body", text)
  }

  fun setTarget(clanId: Int) {
    _state.update { it.copy(targetClanId = clanId) }
  }

  fun setPhase(p: SendPhase, error: String? = null) {
    _state.update { it.copy(phase = p, errorMessage = error) }
    if (p == SendPhase.Queued) sessionStore?.clearDrafts(draftScope)
  }
}
