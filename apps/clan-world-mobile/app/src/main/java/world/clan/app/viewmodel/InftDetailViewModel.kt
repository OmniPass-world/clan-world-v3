package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.ClanWorldConvexClient
import world.clan.app.data.CombinedComm
import world.clan.app.data.InftDemoState
import world.clan.app.data.VaultMovement

enum class DetailTab { Memory, Vault, Whispers, Bulletin }

data class InftDetailUiState(
  val isLoading: Boolean = true,
  val activeTab: DetailTab = DetailTab.Memory,
  val state: InftDemoState? = null,
  val comms: List<CombinedComm> = emptyList(),
  val vault: List<VaultMovement> = emptyList(),
  val errorMessage: String? = null,
)

/**
 * Pulls the iNFT demo state plus the comms and vault feeds that feed the
 * three non-Memory tabs. The Memory tab uses the data inside
 * [InftDemoState.memory] directly; vault/whispers/bulletins come from
 * separate queries.
 *
 * Slice 1 paints the Memory tab fully and shows a "(coming next slice)"
 * placeholder for the other tabs — the data is fetched anyway so when the
 * UI lands we have something to render.
 */
class InftDetailViewModel(
  private val convex: ClanWorldConvexClient,
  private val clanId: Int,
) : ViewModel() {

  private val _state = MutableStateFlow(InftDetailUiState())
  val state: StateFlow<InftDetailUiState> = _state.asStateFlow()

  init { refresh() }

  fun refresh() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }
      val demo = runCatching { convex.getInftDemoState(clanId) }.getOrNull()
      val comms = runCatching { convex.getCombinedComms(clanId, limit = 20) }
        .getOrDefault(emptyList())
        .sortedByDescending { it.tick ?: it.timestamp ?: 0L }
      val vault = runCatching { convex.getVaultMovements(clanId, limit = 20) }
        .getOrDefault(emptyList())
        .sortedByDescending { it.tick }
      _state.update {
        it.copy(
          isLoading = false,
          state = demo,
          comms = comms,
          vault = vault,
          errorMessage = if (demo == null) "Couldn't load iNFT detail." else null,
        )
      }
    }
  }

  fun selectTab(tab: DetailTab) {
    _state.update { it.copy(activeTab = tab) }
  }
}
