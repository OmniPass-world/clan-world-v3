package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.ClanWorldConvexClient

enum class Posture { Defensive, Balanced, Aggressive }

data class StrategyEditorUiState(
  val isLoading: Boolean = true,
  val clanId: Int,
  val posture: Posture = Posture.Balanced,
  val doctrine: String = "",
  val pinnedKey: String = "",
  val savePhase: SendPhase = SendPhase.Idle,
  val errorMessage: String? = null,
)

/**
 * Slice 4c: per-iNFT strategy form.
 *
 * No real strategy mutation exists yet. We seed initial draft state from
 * the clan's first memory entry (best-effort) and call sign-only on save,
 * then surface a "Sealed" state for ~1.3s before pop. When the backend
 * mutation lands, the only change is swapping the no-op send for a real
 * convex.setStrategy(...) call inside the screen.
 */
class StrategyEditorViewModel(
  private val convex: ClanWorldConvexClient,
  clanId: Int,
) : ViewModel() {

  private val _state = MutableStateFlow(StrategyEditorUiState(clanId = clanId))
  val state: StateFlow<StrategyEditorUiState> = _state.asStateFlow()

  init { hydrate(clanId) }

  private fun hydrate(clanId: Int) {
    viewModelScope.launch {
      runCatching { convex.getInftDemoState(clanId) }
        .onSuccess { demo ->
          val firstMem = demo.memory.firstOrNull()
          _state.update {
            it.copy(
              isLoading = false,
              doctrine = firstMem?.value?.take(180) ?: defaultDoctrine(clanId),
              pinnedKey = firstMem?.key ?: "policy:default",
            )
          }
        }
        .onFailure {
          _state.update {
            it.copy(
              isLoading = false,
              doctrine = defaultDoctrine(clanId),
              pinnedKey = "policy:default",
            )
          }
        }
    }
  }

  fun setPosture(p: Posture) = _state.update { it.copy(posture = p) }
  fun setDoctrine(text: String) = _state.update { it.copy(doctrine = text.take(280)) }
  fun setPinnedKey(text: String) = _state.update { it.copy(pinnedKey = text.take(64)) }
  fun setSavePhase(phase: SendPhase, error: String? = null) =
    _state.update { it.copy(savePhase = phase, errorMessage = error) }

  private fun defaultDoctrine(clanId: Int): String = when (clanId) {
    1 -> "Hold the high pass. Trade no ground without iron."
    2 -> "Read the strait. Move only when both shores agree."
    3 -> "Coin discipline first. Spend gold the way a hawk spends wing."
    4 -> "Tend the green country. Refuse winter; outlast it."
    5 -> "Watch the signs. The first move is rarely the right one."
    6 -> "Forge before all. Steel pays for itself."
    7 -> "The iron that holds. Build slow; break later."
    8 -> "Walk the longer line. The season is a cipher; read it twice."
    else -> "—"
  }
}
