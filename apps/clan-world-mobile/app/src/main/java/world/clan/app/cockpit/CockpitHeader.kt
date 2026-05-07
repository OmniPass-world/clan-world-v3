package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Page-level cockpit header. Mirrors apps/web/src/components/cockpit/CockpitHeader.tsx:
 * 40dp tall, ironDeep bg, 1px iron bottom border, title (Cinzel uppercase tracked)
 * on the left, tick-counter pill + connection pill on the right.
 *
 * Tick + connection are stubbed for the first cut — the visual is faithful but
 * data wiring (Convex) is deferred per the plan.
 */
@Composable
fun CockpitHeader(
  modifier: Modifier = Modifier,
  currentTick: Int = 142,
  ticksUntilWipe: Int = 8,
  connection: ConnectionStatus = ConnectionStatus.Connected,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .background(CockpitTokens.Bg.IronDeep)
      .border(width = 1.dp, color = CockpitTokens.Border.Iron)
      .padding(horizontal = CockpitTokens.Space.md),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = "ÆLDER · COCKPIT",
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.88.sp,
        color = CockpitTokens.TextC.OnIron,
      ),
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
    ) {
      TickCounterPill(currentTick = currentTick, ticksUntilWipe = ticksUntilWipe)
      ConnectionPill(status = connection)
    }
  }
}

@Composable
private fun TickCounterPill(currentTick: Int, ticksUntilWipe: Int) {
  val accent = CockpitTokens.TextC.Accent
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(CockpitTokens.Bg.Ink)
      .border(1.dp, CockpitTokens.Border.Iron, RoundedCornerShape(CockpitTokens.Radius.sm))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    Text(
      text = "MEMORY WIPE in ${ticksUntilWipe}t",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 11.sp,
        color = accent,
        letterSpacing = 1.0.sp,
      ),
    )
    Text(
      text = "│",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 11.sp,
        color = CockpitTokens.Border.Iron,
      ),
    )
    Text(
      text = "tick $currentTick",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = accent,
        letterSpacing = 0.6.sp,
      ),
    )
  }
}

enum class ConnectionStatus { Connected, Reconnecting, Disconnected }

@Composable
private fun ConnectionPill(status: ConnectionStatus) {
  val (color, glyph, label) = when (status) {
    ConnectionStatus.Connected    -> Triple(Color(0xFF7A8A6A), "●", "LIVE")
    ConnectionStatus.Reconnecting -> Triple(CockpitTokens.TextC.Accent, "◐", "SYNC")
    ConnectionStatus.Disconnected -> Triple(CockpitTokens.TextC.Danger, "○", "OFF")
  }
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .border(1.dp, color, RoundedCornerShape(CockpitTokens.Radius.sm))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = glyph,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        color = color,
      ),
    )
    Text(
      text = label,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        letterSpacing = 1.2.sp,
      ),
      maxLines = 1,
      overflow = TextOverflow.Clip,
    )
  }
}
