package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import world.clan.app.App

/**
 * Single factory for the four screen-level ViewModels. The per-route
 * [InftDetailViewModel] takes a `clanId` and uses [InftDetailViewModelFactory].
 */
class ClanWorldViewModelFactory(private val app: App) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
    ConnectViewModel::class.java -> ConnectViewModel(app.mwaClient, app.sessionStore)
    HearthViewModel::class.java -> HearthViewModel(app.convexClient, app.sessionStore)
    HallViewModel::class.java -> HallViewModel(app.convexClient, app.sessionStore)
    BazaarViewModel::class.java -> BazaarViewModel()
    CodexViewModel::class.java -> CodexViewModel(app.sessionStore, app.deviceClass)
    else -> error("Unknown ViewModel: ${modelClass.simpleName}")
  } as T
}

/** Per-route factory for InftDetail, which depends on a clanId. */
class InftDetailViewModelFactory(
  private val app: App,
  private val clanId: Int,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
    InftDetailViewModel(app.convexClient, clanId) as T
}

/** Per-route factory for Whispers, which depends on a clanId. */
class WhispersViewModelFactory(
  private val app: App,
  private val clanId: Int,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
    WhispersViewModel(app.convexClient, clanId) as T
}
