package world.clan.app.cockpit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.useConnectionStatus
import world.clan.app.data.convex.useSnapshot
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Title typography reused across the header (CLAN | WORLD) and the
 * bulletin-flyout heading. Mirrors the original "ÆLDER · COCKPIT" style
 * we removed: Cinzel SemiBold 12sp, 0.24em letter-spacing, OnIron.
 */
val CockpitTitleStyle: TextStyle = TextStyle(
  fontFamily = CockpitFonts.Cinzel,
  fontSize = 12.sp,
  fontWeight = FontWeight.SemiBold,
  letterSpacing = 2.88.sp, // 0.24em
  color = CockpitTokens.TextC.OnIron,
)

/** Width of the gap between CLAN and WORLD — sized to clear the camera cutout. */
private val CutoutGapWidth = 32.dp

/**
 * Height of the first row (CLAN/WORLD title band).
 *
 * Was previously sized to the system status-bar inset
 * (`windowInsetsTopHeight(WindowInsets.statusBars)`), but the Scaffold
 * (ClanWorldApp.kt) already applies the status-bar inset to its content
 * via `contentWindowInsets = WindowInsets.statusBars.only(Top + Horizontal)`.
 * That meant Row 1 paid the status-bar height TWICE: once via Scaffold's
 * outer padding, once via this Row's own inset-driven height. Fixing it
 * to a small constant matches the visible title-band design and removes
 * the duplicate (super-swarm v2.6.0 HIGH from codex 5.5).
 */
val HeaderRow1Height = 28.dp

/** Height of the second row (chrome strip carrying the controls). */
val HeaderRow2Height = 40.dp

/**
 * Two-row cockpit header.
 *
 * Row 1 — CLAN | (cutout) | WORLD, centred as a pair around the camera
 *   cutout. Height = system status-bar inset.
 *
 * Row 2 — LIVE indicator (left) · BULLETINS button (centre) · MEMORY WIPE
 *   pill (right). 40dp tall, ironDeep bg.
 */
/**
 * Memory wipe interval in ticks — every Nth tick the world's memory wipes.
 * Mirrors the web's `INTERVAL_TICKS` constant.
 */
private const val WIPE_INTERVAL_TICKS = 10

@Composable
fun CockpitHeader(
  modifier: Modifier = Modifier,
  bulletinOpen: Boolean = false,
  onBulletinToggle: () -> Unit = {},
) {
  // Live snapshot drives the tick counter + memory-wipe countdown.
  val snapshotState = useSnapshot()
  val connection = useConnectionStatus()

  val tick: Int = (snapshotState as? QueryState.Live)?.data?.tick?.roundToInt() ?: 0
  val ticksUntilWipe: Int = max(0, WIPE_INTERVAL_TICKS - (tick % WIPE_INTERVAL_TICKS))

  Column(modifier = modifier) {
    // Row 1 — CLAN [cutout] WORLD, centred on the cutout. Fixed
    // HeaderRow1Height (28dp) instead of `windowInsetsTopHeight` —
    // see the constant's doc for the duplicate-inset rationale.
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(HeaderRow1Height),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = "CLAN", style = CockpitTitleStyle)
      Spacer(modifier = Modifier.width(CutoutGapWidth))
      Text(text = "WORLD", style = CockpitTitleStyle)
    }

    // Row 2 — LIVE (left) · BULLETINS (centre) · MEMORY WIPE (right)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(HeaderRow2Height)
        .background(CockpitTokens.Bg.IronDeep)
        .border(width = 1.dp, color = CockpitTokens.Border.Iron)
        .padding(horizontal = CockpitTokens.Space.md),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ConnectionPill(status = connection)
      Spacer(modifier = Modifier.weight(1f))
      BulletinButton(open = bulletinOpen, onClick = onBulletinToggle)
      Spacer(modifier = Modifier.weight(1f))
      TickCounterPill(tick = tick, ticksUntilWipe = ticksUntilWipe)
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
      text = "BULLETINS",
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
private fun TickCounterPill(tick: Int, ticksUntilWipe: Int) {
  val accent = CockpitTokens.TextC.Accent

  // Border flashes accent for ~250ms on every real tick advance, then
  // settles back to neutral. Driven by the snapshot's tick — no tick
  // change → no flash, so the pill is genuinely silent during outages.
  var pulseActive by remember { mutableStateOf(false) }
  LaunchedEffect(tick) {
    if (tick > 0) {
      pulseActive = true
      delay(250)
      pulseActive = false
    }
  }
  val borderColor by animateColorAsState(
    targetValue = if (pulseActive) accent else CockpitTokens.Border.Iron,
    animationSpec = tween(durationMillis = if (pulseActive) 120 else 420),
    label = "tickPulseBorder",
  )

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(CockpitTokens.Bg.Ink)
      .border(1.dp, borderColor, RoundedCornerShape(CockpitTokens.Radius.sm))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
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
