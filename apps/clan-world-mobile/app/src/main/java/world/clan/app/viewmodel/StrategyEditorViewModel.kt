package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.ClanWorldConvexClient

data class StrategyEditorUiState(
  val isLoading: Boolean = true,
  val clanId: Int,
  /** Server-truth strategy + notes. Used to detect dirty state on the drafts. */
  val committedStrategy: String = "",
  val committedNotes: String = "",
  /** Live editor drafts. */
  val draftStrategy: String = "",
  val draftNotes: String = "",
  val savePhase: SendPhase = SendPhase.Idle,
  val errorMessage: String? = null,
  val statusBody: String? = null,
)

/**
 * Slice 4d redesign of StrategyEditor — dual STRATEGY + NOTES textarea
 * pattern that mirrors the polished web design from PR #51
 * (`apps/web/src/pages/agent/EssenceSection.tsx`).
 *
 * State is per-iNFT; hydration seeds the drafts from the clan's first two
 * memory entries. No real Convex mutation yet — `confirmSeal()` flips
 * commit values to drafts (locally; chain integration is a follow-up).
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
          val strategy = demo.memory.firstOrNull { it.key.contains("strategy", ignoreCase = true) }
            ?.value
            ?: demo.memory.firstOrNull()?.value
            ?: defaultStrategy(clanId)
          val notes = demo.memory.firstOrNull { it.key.contains("notes", ignoreCase = true) }
            ?.value
            ?: demo.memory.getOrNull(1)?.value
            ?: defaultNotes(clanId)
          _state.update {
            it.copy(
              isLoading = false,
              committedStrategy = strategy,
              committedNotes = notes,
              draftStrategy = strategy,
              draftNotes = notes,
            )
          }
        }
        .onFailure {
          _state.update {
            it.copy(
              isLoading = false,
              committedStrategy = defaultStrategy(clanId),
              committedNotes = defaultNotes(clanId),
              draftStrategy = defaultStrategy(clanId),
              draftNotes = defaultNotes(clanId),
            )
          }
        }
    }
  }

  fun setDraftStrategy(text: String) =
    _state.update { it.copy(draftStrategy = text.take(800), errorMessage = null) }

  fun setDraftNotes(text: String) =
    _state.update { it.copy(draftNotes = text.take(400), errorMessage = null) }

  fun setSavePhase(phase: SendPhase, error: String? = null) =
    _state.update { it.copy(savePhase = phase, errorMessage = error) }

  /** Optimistic commit after the wallet burn and Convex write succeed. */
  fun confirmSeal(signature: String) {
    val s = _state.value
    val fields = buildList {
      if (s.draftStrategy != s.committedStrategy) add("strategy")
      if (s.draftNotes != s.committedNotes) add("notes")
    }
    _state.update {
      it.copy(
        committedStrategy = it.draftStrategy,
        committedNotes = it.draftNotes,
        savePhase = SendPhase.Queued,
        errorMessage = null,
        statusBody = if (fields.isEmpty()) "essence sealed"
        else "essence sealed · ${fields.joinToString(" + ")} written · ${signature.take(6)}…",
      )
    }
  }

  fun clearStatus() = _state.update { it.copy(statusBody = null, errorMessage = null) }

  private fun defaultStrategy(clanId: Int): String = when (clanId) {
    1 -> "Forest economy first. Build wood vault to 50, secure forest perimeter. Ally with clan-2 if Iron Guard whispers cooperation by tick 80. Don't trust clan-3 — Crimson burns bridges by tick 60 every season."
    2 -> "Patient stockpile. Hold ore at 240+ before any trade. Defensive posture until tick 90, then exploit whoever starves first. Never raid first — counter-raid only."
    3 -> "Volatility is leverage. Burn one bridge per season for shock value. Raid the weakest stockpile at tick 70. Don't hoard — convert wins into pressure immediately."
    4 -> "Build for the long arc. Wood + ore parity by tick 60. Form a 3-clan defensive pact early, then break it the season AFTER everyone trusts us. Patience compounds."
    else -> "Hold the line. Read the signs. Move only when both shores agree."
  }

  private fun defaultNotes(clanId: Int): String = when (clanId) {
    1 -> "Crimson owes 8 wood from T42 trade. Iron Guard prefers ore-for-wood at 3:1. Verdant Wardens reliable on truces but slow to strike."
    2 -> "Storm Riders aggressive on T20-40, then exhausted. Crimson volatile — never trust truce past tick 70. Verdant Wardens make solid 3-clan defensive pacts."
    3 -> "Storm Riders mirror our energy — match their tempo, then break it. Iron Guard slow to react; raid them before tick 80 for clean wins."
    4 -> "Truces hold us back if we keep them past tick 100. Storm Riders predictable raiders — let them tire on Iron Guard. Crimson hates being mirrored."
    else -> "—"
  }
}
