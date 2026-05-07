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

enum class WhispersFilter { All, Whisper, Orch, Human }

data class WhispersUiState(
  val isLoading: Boolean = true,
  val items: List<CombinedComm> = emptyList(),
  val filter: WhispersFilter = WhispersFilter.All,
  val errorMessage: String? = null,
)

/**
 * Inbox view-model for the per-clan whispers screen. Reuses the existing
 * `comms:getCombinedComms` query — no new server-side surface needed.
 *
 * Filtering is client-side: the server returns the combined feed and the
 * filter chips just slice the local list.
 */
class WhispersViewModel(
  private val convex: ClanWorldConvexClient,
  private val clanId: Int,
) : ViewModel() {

  private val _state = MutableStateFlow(WhispersUiState())
  val state: StateFlow<WhispersUiState> = _state.asStateFlow()

  init { refresh() }

  fun refresh() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }
      runCatching { convex.getCombinedComms(clanId, limit = 80) }
        .onSuccess { comms ->
          _state.update {
            it.copy(isLoading = false, items = comms, errorMessage = null)
          }
        }
        .onFailure { e ->
          _state.update {
            it.copy(
              isLoading = false,
              errorMessage = e.message ?: "Failed to load whispers.",
            )
          }
        }
    }
  }

  fun setFilter(f: WhispersFilter) {
    _state.update { it.copy(filter = f) }
  }
}

/** Apply the active filter to the loaded items. */
fun WhispersUiState.filtered(): List<CombinedComm> = when (filter) {
  WhispersFilter.All -> items
  WhispersFilter.Whisper -> items.filter { it.kind == "whisper" }
  WhispersFilter.Orch -> items.filter { it.kind == "orch" }
  WhispersFilter.Human -> items.filter { it.kind == "human" }
}
