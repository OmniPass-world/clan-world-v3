package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.ClanWorldConvexClient
import world.clan.app.data.InftDemoState
import world.clan.app.data.InftToken
import world.clan.app.data.MemoryEntry
import world.clan.app.data.SessionStore

data class HallUiState(
  val isLoading: Boolean = true,
  val isRefreshing: Boolean = false,
  val items: List<HallCard> = emptyList(),
  val totalClans: Int = 8,
  val walletShort: String = "",
  val errorMessage: String? = null,
)

/**
 * Aggregated card data for one iNFT in the Hall list.
 */
data class HallCard(
  val tokenId: Int,
  val clanId: Int,
  val token: InftToken?,
  val memoryCount: Int,
  val sealedAtTick: Long?,
  val mostRecentTransferTick: Long?,
)

/**
 * Renders the Hall: the user's "linked" iNFTs.
 *
 * Slice 1 is a UI demo — there is no EVM linkage step. The set of "yours"
 * clans is a hardcoded constant; one query is issued per linked clanId in
 * parallel via `awaitAll`, results stitched into HallCard rows.
 */
class HallViewModel(
  private val convex: ClanWorldConvexClient,
  private val sessionStore: SessionStore,
) : ViewModel() {

  private val _state = MutableStateFlow(initial())
  val state: StateFlow<HallUiState> = _state.asStateFlow()

  init { refresh() }

  private fun initial(): HallUiState {
    val s = sessionStore.read()
    return HallUiState(walletShort = s?.solanaPubkeyBase58?.shortenPubkey().orEmpty())
  }

  fun refresh() {
    viewModelScope.launch {
      _state.update { it.copy(isRefreshing = true, errorMessage = null) }
      val ownedClanIds = (HearthViewModel.LINKED_CLAN_IDS + sessionStore.getOwnedClanIdsExtra()).distinct()
      runCatching {
        coroutineScope {
          ownedClanIds
            .map { clanId -> async { runCatching { convex.getInftDemoState(clanId) }.getOrNull() to clanId } }
            .awaitAll()
        }
      }.onSuccess { results ->
        val cards = results.mapNotNull { (demo, clanId) -> demo?.let { toCard(clanId, it) } }
        _state.update {
          it.copy(
            isLoading = false,
            isRefreshing = false,
            items = cards,
            errorMessage = if (cards.isEmpty()) "No linked iNFTs found." else null,
          )
        }
      }.onFailure { e ->
        _state.update {
          it.copy(
            isLoading = false,
            isRefreshing = false,
            errorMessage = e.message ?: "Failed to load Hall.",
          )
        }
      }
    }
  }

  private fun toCard(clanId: Int, demo: InftDemoState): HallCard {
    return HallCard(
      tokenId = demo.token?.tokenId ?: clanId,
      clanId = clanId,
      token = demo.token,
      memoryCount = demo.memory.size,
      sealedAtTick = demo.transfers.minOfOrNull { it.transferredAt ?: 0L }?.takeIf { it > 0 },
      mostRecentTransferTick = demo.transfers.maxOfOrNull { it.transferredAt ?: 0L }?.takeIf { it > 0 },
    )
  }
}
