package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.ClanWorldConvexClient
import world.clan.app.data.VaultMovement
import world.clan.app.data.weiToWhole

enum class TreasuryFilter { All, Gold, Resources }

data class ResourceBalance(
  val gold: Long,
  val wood: Long,
  val iron: Long,
  val wheat: Long,
  val fish: Long,
)

data class TreasuryUiState(
  val isLoading: Boolean = true,
  val clanId: Int,
  val balance: ResourceBalance = ResourceBalance(0, 0, 0, 0, 0),
  val movements: List<VaultMovement> = emptyList(),
  val filter: TreasuryFilter = TreasuryFilter.All,
  val errorMessage: String? = null,
)

/**
 * Slice 5: per-clan Treasury.
 *
 * Aggregates two existing queries in parallel:
 *   - getSnapshot — to read goldBalance / vaultWood / vaultIron / etc for THIS clan
 *   - getVaultMovements — recent gains/spends/transfers
 *
 * Wei-precision balances (`String?` → `Long`) parsed via existing weiToWhole().
 * Filtering is client-side over `movements`.
 */
class TreasuryViewModel(
  private val convex: ClanWorldConvexClient,
  private val clanId: Int,
) : ViewModel() {

  private val _state = MutableStateFlow(TreasuryUiState(clanId = clanId))
  val state: StateFlow<TreasuryUiState> = _state.asStateFlow()

  init {
    refresh()
    // Gold movements / balance should refresh while screen is open.
    // 30s cadence matches Hearth/Hall/Whispers.
    viewModelScope.launch {
      while (true) {
        kotlinx.coroutines.delay(HearthViewModel.REFRESH_INTERVAL_MS)
        if (_state.value.isLoading) continue
        refresh()
      }
    }
  }

  fun refresh() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }
      runCatching {
        coroutineScope {
          val snap = async { convex.getSnapshot() }
          val moves = async {
            runCatching { convex.getVaultMovements(clanId, limit = 24) }
              .getOrDefault(emptyList())
              .sortedByDescending { it.tick }
          }
          snap.await() to moves.await()
        }
      }
        .onSuccess { (snapshot, moves) ->
          val mine = snapshot.clans.firstOrNull { it.id.toIntOrNull() == clanId }
          val bal = if (mine == null) ResourceBalance(0, 0, 0, 0, 0)
          else ResourceBalance(
            gold = mine.goldBalance.weiToWhole(),
            wood = mine.vaultWood.weiToWhole(),
            iron = mine.vaultIron.weiToWhole(),
            wheat = mine.vaultWheat.weiToWhole(),
            fish = mine.vaultFish.weiToWhole(),
          )
          _state.update {
            it.copy(
              isLoading = false,
              balance = bal,
              movements = moves,
              errorMessage = null,
            )
          }
        }
        .onFailure { e ->
          _state.update {
            it.copy(
              isLoading = false,
              errorMessage = e.message ?: "Failed to load treasury.",
            )
          }
        }
    }
  }

  fun setFilter(f: TreasuryFilter) {
    _state.update { it.copy(filter = f) }
  }
}

fun TreasuryUiState.filteredMovements(): List<VaultMovement> = when (filter) {
  TreasuryFilter.All -> movements
  TreasuryFilter.Gold -> movements.filter { it.resource == "gold" }
  TreasuryFilter.Resources -> movements.filter { it.resource != "gold" }
}
