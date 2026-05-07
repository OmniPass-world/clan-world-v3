package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.clan.app.data.BAZAAR_LISTINGS
import world.clan.app.data.BazaarListing

data class BazaarUiState(
  val isLoading: Boolean = false,
  val listings: List<BazaarListing> = emptyList(),
  val errorMessage: String? = null,
)

/**
 * Slice 4: Bazaar viewmodel. The listings are a static demo list; no Convex
 * marketplace query exists yet. ViewModel still exists so that the screen
 * can subscribe through StateFlow like every other screen — when a real
 * listings query lands, only this file changes.
 */
class BazaarViewModel : ViewModel() {

  private val _state = MutableStateFlow(
    BazaarUiState(listings = BAZAAR_LISTINGS),
  )
  val state: StateFlow<BazaarUiState> = _state.asStateFlow()
}
