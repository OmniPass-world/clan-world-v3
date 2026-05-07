package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.clan.app.data.SessionStore

enum class ForgeStep { PickClan, NameSigil, PickHarness, Confirm }

/**
 * Themed harness choices for a freshly forged sigil.
 *  - Iron   → defensive bias
 *  - Tide   → balanced bias
 *  - Ember  → aggressive bias
 */
enum class Harness(val display: String, val tagline: String) {
  Iron("Iron", "hold the line; trade no ground without weight"),
  Tide("Tide", "read the strait; move when both shores agree"),
  Ember("Ember", "forge ahead; the season favors the bold"),
}

data class ForgeUiState(
  val step: ForgeStep = ForgeStep.PickClan,
  val clanId: Int? = null,
  val sigilName: String = "",
  val harness: Harness = Harness.Tide,
  val mintPhase: SendPhase = SendPhase.Idle,
  val errorMessage: String? = null,
  /** Clans the user already owns — disabled in step 1 (PickClan). */
  val ownedClanIds: Set<Int> = emptySet(),
)

/**
 * Slice 6: Forge. Four-step mint wizard.
 *
 * No real on-chain mint exists. The MWA sign at the end is the seam —
 * a future on-chain mint replaces only the screen-level send call.
 */
class ForgeViewModel(
  private val sessionStore: SessionStore? = null,
) : ViewModel() {

  private val draftScope = "forge"

  private val _state = MutableStateFlow(initial())
  val state: StateFlow<ForgeUiState> = _state.asStateFlow()

  private fun initial(): ForgeUiState {
    val draftClan = sessionStore?.getDraft(draftScope, "clanId")?.toIntOrNull()
    val draftName = sessionStore?.getDraft(draftScope, "sigilName").orEmpty()
    val draftHarness = sessionStore?.getDraft(draftScope, "harness")
      ?.let { runCatching { Harness.valueOf(it) }.getOrNull() }
      ?: Harness.Tide
    val owned = (HearthViewModel.LINKED_CLAN_IDS + sessionStore?.getOwnedClanIdsExtra().orEmpty()).toSet()
    // If the persisted draftClan was a clan you've since acquired, drop
    // it so step 1 doesn't preselect a now-disabled card.
    val safeClanId = draftClan?.takeIf { it !in owned }
    return ForgeUiState(
      clanId = safeClanId,
      sigilName = draftName,
      harness = draftHarness,
      ownedClanIds = owned,
    )
  }

  fun setClanId(clanId: Int) {
    // Defensive: ignore taps on already-owned clans (step 1 should already
    // tap-disable them, but the VM is the source of truth).
    if (clanId in _state.value.ownedClanIds) return
    _state.update { it.copy(clanId = clanId) }
    sessionStore?.setDraft(draftScope, "clanId", clanId.toString())
  }
  fun setSigilName(name: String) {
    val capped = name.take(32)
    _state.update { it.copy(sigilName = capped) }
    sessionStore?.setDraft(draftScope, "sigilName", capped)
  }
  fun setHarness(h: Harness) {
    _state.update { it.copy(harness = h) }
    sessionStore?.setDraft(draftScope, "harness", h.name)
  }

  fun goTo(step: ForgeStep) = _state.update { it.copy(step = step) }

  fun next() = _state.update {
    val nxt = when (it.step) {
      ForgeStep.PickClan -> ForgeStep.NameSigil
      ForgeStep.NameSigil -> ForgeStep.PickHarness
      ForgeStep.PickHarness -> ForgeStep.Confirm
      ForgeStep.Confirm -> ForgeStep.Confirm
    }
    it.copy(step = nxt)
  }

  fun back() = _state.update {
    val prv = when (it.step) {
      ForgeStep.PickClan -> ForgeStep.PickClan
      ForgeStep.NameSigil -> ForgeStep.PickClan
      ForgeStep.PickHarness -> ForgeStep.NameSigil
      ForgeStep.Confirm -> ForgeStep.PickHarness
    }
    it.copy(step = prv)
  }

  fun setMintPhase(phase: SendPhase, error: String? = null) {
    _state.update { it.copy(mintPhase = phase, errorMessage = error) }
    if (phase == SendPhase.Queued) sessionStore?.clearDrafts(draftScope)
  }
}
