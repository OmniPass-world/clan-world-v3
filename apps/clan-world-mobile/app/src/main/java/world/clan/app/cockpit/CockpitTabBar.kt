package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.data.Elder
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private data class TabSpec(val id: TabId, val icon: String, val label: String)

private val TABS = listOf(
  TabSpec(TabId.Terminal, "▣", "TERM"),
  TabSpec(TabId.Vault,    "◈", "VAULT"),
  TabSpec(TabId.Clansman, "☗", "CLAN"),
  TabSpec(TabId.ZeroG,    "◉", "NFT"),
  TabSpec(TabId.Comms,    "✉", "COMMS"),
)

/**
 * Per-panel tab bar. Mirrors apps/web/src/components/cockpit/shared/CockpitTabBar.tsx
 * but with the layout flipped:
 *  - clan badge (glyph + name) on the LEFT, takes weight(1f)
 *  - 5 tabs (44dp each, icon over label) on the RIGHT
 *  - active tab → bg.ink + accent text + 2dp accent underline + glow
 *  - 1dp Border.Iron separators between tabs (and between badge + tabs)
 */
@Composable
fun CockpitTabBar(
  active: TabId,
  onSelect: (TabId) -> Unit,
  elder: Elder,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(46.dp)
      .background(CockpitTokens.Bg.IronDeep),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left: clan badge
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .padding(horizontal = CockpitTokens.Space.sm),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = elder.glyph,
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 14.sp,
          color = elder.accent,
        ),
      )
      Text(
        text = elder.name.uppercase(),
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 12.sp,
          color = CockpitTokens.TextC.OnIron,
          letterSpacing = 1.92.sp, // 0.16em
          fontWeight = FontWeight.SemiBold,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    // Separator before tabs
    Box(
      modifier = Modifier
        .width(1.dp)
        .fillMaxHeight()
        .background(CockpitTokens.Border.Iron)
    )

    // Right: 5 tabs
    TABS.forEachIndexed { idx, spec ->
      val isActive = spec.id == active
      TabButton(
        spec = spec,
        isActive = isActive,
        onClick = { onSelect(spec.id) },
      )
      if (idx != TABS.lastIndex) {
        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(CockpitTokens.Border.Iron)
        )
      }
    }
  }
}

@Composable
private fun TabButton(
  spec: TabSpec,
  isActive: Boolean,
  onClick: () -> Unit,
) {
  val bg: Color = if (isActive) CockpitTokens.Bg.Ink else Color.Transparent
  val fg: Color = if (isActive) CockpitTokens.TextC.Accent else CockpitTokens.TextC.OnIronDim
  Box(
    modifier = Modifier
      .width(44.dp)
      .fillMaxHeight()
      .background(bg)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = spec.icon,
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 18.sp,
          color = fg,
        ),
      )
      Box(modifier = Modifier.height(2.dp))
      Text(
        text = spec.label,
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 8.sp,
          fontWeight = FontWeight.SemiBold,
          color = fg,
          letterSpacing = 0.96.sp, // 0.12em
        ),
        textAlign = TextAlign.Center,
      )
    }
    if (isActive) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(2.dp)
          .background(CockpitTokens.TextC.Accent)
          .align(Alignment.BottomCenter),
      )
    }
  }
}
