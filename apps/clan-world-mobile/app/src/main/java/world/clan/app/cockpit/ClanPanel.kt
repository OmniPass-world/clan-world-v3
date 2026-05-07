package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.clan.app.cockpit.tabs.ClansmanTab
import world.clan.app.cockpit.tabs.CommsTab
import world.clan.app.cockpit.tabs.TerminalTab
import world.clan.app.cockpit.tabs.VaultTab
import world.clan.app.cockpit.tabs.ZeroGTab
import world.clan.app.data.Elder
import world.clan.app.ui.theme.CockpitTokens

enum class TabId { Terminal, Vault, Clansman, ZeroG, Comms }

/**
 * One clan panel — full-width, sharp top corners (so the rounded bottom
 * of the map sits flush against it). 2dp accent line at the top echoes
 * the clan colour. Tab bar above content; floating Owner Control button
 * overlays only the Terminal tab, top-right.
 */
@Composable
fun ClanPanel(
  elder: Elder,
  onOwnerControl: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val initialTab = if (elder.clanId == 1) TabId.Terminal else TabId.Vault
  var active by rememberSaveable(elder.clanId) { mutableStateOf(initialTab) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Iron),
  ) {
    // 2dp top accent line in clan colour — meets the map's rounded bottom
    // edge directly.
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(2.dp)
        .background(elder.accent),
    )

    CockpitTabBar(
      active = active,
      onSelect = { active = it },
      elder = elder,
      modifier = Modifier.fillMaxWidth(),
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.TopStart,
    ) {
      when (active) {
        TabId.Terminal -> TerminalTab(elder = elder, modifier = Modifier.fillMaxSize())
        TabId.Vault    -> VaultTab(elder = elder, modifier = Modifier.fillMaxSize())
        TabId.Clansman -> ClansmanTab(elder = elder, modifier = Modifier.fillMaxSize())
        TabId.ZeroG    -> ZeroGTab(elder = elder, modifier = Modifier.fillMaxSize())
        TabId.Comms    -> CommsTab(elder = elder, modifier = Modifier.fillMaxSize())
      }

      if (active == TabId.Terminal) {
        OwnerControlButton(
          elder = elder,
          onClick = onOwnerControl,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 6.dp, top = 6.dp),
        )
      }
    }
  }
}
