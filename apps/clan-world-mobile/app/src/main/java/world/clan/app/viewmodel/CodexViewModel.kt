package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.clan.app.BuildConfig
import world.clan.app.data.LineageEntry
import world.clan.app.data.LineageStore
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
  val lineage: List<LineageEntry> = emptyList(),
)

/**
 * Codex — read-only identity / device / about / lineage summary. Powered
 * by BuildConfig + SessionStore + LineageStore.
 */
class CodexViewModel(
  private val sessionStore: SessionStore,
  private val lineageStore: LineageStore,
  deviceClass: DeviceClass,
) : ViewModel() {

  private val _state = MutableStateFlow(
    run {
      val s = sessionStore.read()
      val owned = (HearthViewModel.LINKED_CLAN_IDS + sessionStore.getOwnedClanIdsExtra()).distinct()
      CodexUiState(
        deviceClass = deviceClass,
        solanaPubkey = s?.solanaPubkeyBase58,
        walletLabel = s?.walletLabel,
        build = "v ${BuildConfig.VERSION_NAME} · build ${BuildConfig.VERSION_CODE}",
        convexUrl = BuildConfig.CONVEX_URL,
        linkedClansCount = owned.size,
        linkedClanIds = owned,
        apkShareUrl = "https://shared.claude.do/public/clanworld-mobile-slice-1.apk",
        lineage = lineageStore.read(),
      )
    },
  )
  val state: StateFlow<CodexUiState> = _state.asStateFlow()

  /**
   * Re-read lineage + hired clans on (re-)entry so newly-signed actions
   * and freshly-hired sigils appear in Codex without an app kill.
   */
  fun refreshLineage() {
    val owned = (HearthViewModel.LINKED_CLAN_IDS + sessionStore.getOwnedClanIdsExtra()).distinct()
    _state.value = _state.value.copy(
      lineage = lineageStore.read(),
      linkedClansCount = owned.size,
      linkedClanIds = owned,
    )
  }

  /**
   * Wipe demo-only state and reload the screen. Useful for repeat demos
   * on the same device — restores Hall to LINKED_CLAN_IDS, empties
   * Lineage, re-enables disabled Forge clans, re-shows the onboarding
   * coachmark.
   */
  fun resetDemoState() {
    sessionStore.resetDemoState()
    lineageStore.clear()
    val owned = HearthViewModel.LINKED_CLAN_IDS.distinct()
    _state.value = _state.value.copy(
      lineage = emptyList(),
      linkedClansCount = owned.size,
      linkedClanIds = owned,
    )
  }
}
