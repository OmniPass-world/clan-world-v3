package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * One clan panel — equivalent to MiniCockpit.tsx. Mirrors:
 *  - bg = Bg.Iron, 1dp Border.Iron, 2dp top accent line in elder.accent
 *  - radius 4dp, top tab bar (46dp), flex-1 content area
 *  - the floating Owner Control button overlays the Terminal tab only
 *    (matches the web placement from commit 91ed5ec).
 */
@Composable
fun ClanPanel(
  elder: Elder,
  onOwnerControl: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Default tab matches the web: clan 1 starts on terminal, others on vault.
  val initialTab = if (elder.clanId == 1) TabId.Terminal else TabId.Vault
  var active by rememberSaveable(elder.clanId) { mutableStateOf(initialTab) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(CockpitTokens.Radius.md))
      .background(CockpitTokens.Bg.Iron)
      .border(1.dp, CockpitTokens.Border.Iron, RoundedCornerShape(CockpitTokens.Radius.md)),
  ) {
    // 2dp top accent line in clan color
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

      // Floating Owner Control — only on Terminal tab, matching web placement.
      if (active == TabId.Terminal) {
        OwnerControlButton(
          elder = elder,
          onClick = onOwnerControl,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = CockpitTokens.Space.md, top = 32.dp),
        )
      }
    }
  }
}
