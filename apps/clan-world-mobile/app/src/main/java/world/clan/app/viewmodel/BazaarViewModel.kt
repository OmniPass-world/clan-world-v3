package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.clan.app.data.BAZAAR_LISTINGS
import world.clan.app.data.BazaarListing
import world.clan.app.data.SessionStore

data class BazaarUiState(
  val isLoading: Boolean = false,
  val listings: List<BazaarListing> = emptyList(),
  val errorMessage: String? = null,
)

/**
 * Bazaar viewmodel. Listings are a static demo set; no Convex marketplace
 * query exists yet. Filters out clans already hired (so a hired sigil
 * can't be hired again in the same install).
 */
class BazaarViewModel(
  private val sessionStore: SessionStore? = null,
) : ViewModel() {

  private val _state = MutableStateFlow(BazaarUiState(listings = currentListings()))
  val state: StateFlow<BazaarUiState> = _state.asStateFlow()

  /** Re-read hired set + reapply filter — call when re-entering the screen. */
  fun refresh() {
    _state.update { it.copy(listings = currentListings()) }
  }

  private fun currentListings(): List<BazaarListing> {
    val hired = sessionStore?.getHiredClanIds().orEmpty()
    return BAZAAR_LISTINGS.filter { it.clanId !in hired }
  }
}
