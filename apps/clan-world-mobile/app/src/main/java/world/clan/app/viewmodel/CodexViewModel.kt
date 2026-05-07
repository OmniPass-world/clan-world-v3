package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.clan.app.BuildConfig
import world.clan.app.data.SessionStore
import world.clan.app.wallet.DeviceClass

data class CodexUiState(
  val deviceClass: DeviceClass,
  val solanaPubkey: String?,
  val walletLabel: String?,
  val build: String,
  val convexUrl: String,
  val linkedClansCount: Int,
  val linkedClanIds: List<Int>,
  val apkShareUrl: String,
)

/**
 * Codex — read-only identity / device / about summary. Powered by
 * BuildConfig + SessionStore.
 */
class CodexViewModel(
  private val sessionStore: SessionStore,
  deviceClass: DeviceClass,
) : ViewModel() {

  private val _state = MutableStateFlow(
    run {
      val s = sessionStore.read()
      CodexUiState(
        deviceClass = deviceClass,
        solanaPubkey = s?.solanaPubkeyBase58,
        walletLabel = s?.walletLabel,
        build = "v ${BuildConfig.VERSION_NAME} · build ${BuildConfig.VERSION_CODE}",
        convexUrl = BuildConfig.CONVEX_URL,
        linkedClansCount = HearthViewModel.LINKED_CLAN_IDS.size,
        linkedClanIds = HearthViewModel.LINKED_CLAN_IDS,
        apkShareUrl = "https://shared.claude.do/public/clanworld-mobile-slice-1.apk",
      )
    },
  )
  val state: StateFlow<CodexUiState> = _state.asStateFlow()

  fun disconnect() {
    sessionStore.clear()
    _state.value = _state.value.copy(solanaPubkey = null, walletLabel = null)
  }
}
