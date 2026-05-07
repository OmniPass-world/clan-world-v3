package world.clan.app.cockpit

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * Two-row cockpit header.
 *
 * Row 1 — "CLAN" left + camera cutout (centred) + "WORLD" right. Sits in
 *   the unsafe space at the top of the screen so the entire app feels
 *   edge-to-edge. Height = the system status-bar inset, so words clear
 *   the cutout naturally on the target device.
 *
 * Row 2 — 40dp ironDeep strip carrying the public-bulletin toggle on the
 *   left, and the tick counter + connection pill on the right. The web's
 *   "ÆLDER · COCKPIT" title is intentionally dropped to free up vertical
 *   space for the rest of the cockpit.
 */
@Composable
fun CockpitHeader(
  modifier: Modifier = Modifier,
  bulletinOpen: Boolean = false,
  onBulletinToggle: () -> Unit = {},
  currentTick: Int = 142,
  ticksUntilWipe: Int = 8,
  connection: ConnectionStatus = ConnectionStatus.Connected,
) {
  Column(modifier = modifier) {
    // Row 1 — CLAN | (cutout) | WORLD, in the status-bar/cutout area.
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsTopHeight(WindowInsets.statusBars)
        .padding(horizontal = 24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "CLAN",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.2.sp, // 0.2em ≈ 11 * 0.2
          color = CockpitTokens.TextC.OnIron,
        ),
      )
      Text(
        text = "WORLD",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.2.sp,
          color = CockpitTokens.TextC.OnIron,
        ),
      )
    }

    // Row 2 — bulletin button + tick + connection pill.
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .background(CockpitTokens.Bg.IronDeep)
        .border(width = 1.dp, color = CockpitTokens.Border.Iron)
        .padding(horizontal = CockpitTokens.Space.md),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      BulletinButton(open = bulletinOpen, onClick = onBulletinToggle)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
      ) {
        TickCounterPill(currentTick = currentTick, ticksUntilWipe = ticksUntilWipe)
        ConnectionPill(status = connection)
      }
    }
  }
}

@Composable
private fun BulletinButton(open: Boolean, onClick: () -> Unit) {
  val accent = CockpitTokens.TextC.Accent
  val borderColor = if (open) accent else CockpitTokens.Border.Iron
  val labelColor  = if (open) accent else CockpitTokens.TextC.OnIron
  val bg = if (open) accent.copy(alpha = 0.10f) else CockpitTokens.Bg.Ink

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(bg)
      .border(1.dp, borderColor, RoundedCornerShape(CockpitTokens.Radius.sm))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = "▤",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 13.sp,
        color = labelColor,
      ),
    )
    Text(
      text = "PUBLIC BULLETIN",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 11.sp,
        color = labelColor,
        letterSpacing = 0.66.sp, // 0.06em
      ),
      maxLines = 1,
      overflow = TextOverflow.Clip,
    )
  }
}

@Composable
private fun TickCounterPill(currentTick: Int, ticksUntilWipe: Int) {
  val accent = CockpitTokens.TextC.Accent

  // Until live tick events arrive, simulate the periodic "tick advanced"
  // pulse the web header has on every snapshot change: short ~250ms flash
  // of the border into accent, then back to neutral, on an 8-second loop.
  val transition = rememberInfiniteTransition(label = "tickPulse")
  val borderColor by transition.animateColor(
    initialValue = CockpitTokens.Border.Iron,
    targetValue = CockpitTokens.Border.Iron,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 8_000
        CockpitTokens.Border.Iron at 0
        accent                    at 250
        CockpitTokens.Border.Iron at 600
        CockpitTokens.Border.Iron at 8_000
      },
      repeatMode = RepeatMode.Restart,
    ),
    label = "tickPulseBorder",
  )

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(CockpitTokens.Bg.Ink)
      .border(1.dp, borderColor, RoundedCornerShape(CockpitTokens.Radius.sm))
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

