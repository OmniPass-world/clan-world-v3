package world.clan.app.cockpit.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.BuildConfig
import world.clan.app.cockpit.tabs.shared.SectionHeader
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.toDomain
import world.clan.app.data.convex.useClansmen
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private val HUNGER_FILL_NORMAL = Color(0xFFB8862E)
private val READY_GREEN = Color(0xFF3A7A3A)

/**
 * Clansman roster tab. Visual port of ClansmanTab.tsx — 4-column row
 * (ID, mission/location, ETA-or-cooldown, hunger bar) per clansman.
 * Starvation (hunger > 0.7) flips the row border + bar fill to danger red.
 */
@Composable
fun ClansmanTab(elder: Elder, modifier: Modifier = Modifier) {
  val live = useClansmen(elder.clanId)
  val rows = when (live) {
    is QueryState.Live -> live.data.map { it.toDomain() }
    else -> if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.clansmen(elder.clanId) else emptyList()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Parchment)
      .verticalScroll(rememberScrollState())
      .padding(CockpitTokens.Space.md),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    SectionHeader("CLANSMEN", trailing = "${rows.size} active")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      rows.forEach { ClansmanRowItem(it, accent = elder.accent) }
    }
  }
}

@Composable
private fun ClansmanRowItem(c: StubData.ClansmanRow, accent: Color) {
  val starving = c.hunger > 0.7f
  val borderColor = if (starving) CockpitTokens.TextC.Danger else CockpitTokens.Border.ParchmentEdge

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(Color.White.copy(alpha = 0.22f))
      .border(1.dp, borderColor, RoundedCornerShape(CockpitTokens.Radius.sm))
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // ID column
    Text(
      text = c.id,
      modifier = Modifier.width(28.dp),
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = accent,
      ),
    )

    // Mission + Location
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = c.mission,
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.OnParchment,
        ),
      )
      Text(
        text = c.location,
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 10.sp,
          color = CockpitTokens.TextC.OnParchmentDim,
        ),
      )
    }

    // ETA / Cooldown / Ready
    val (statusText, statusColor) = when {
      c.eta != null && c.eta > 0 -> "${c.eta}t" to CockpitTokens.TextC.OnParchmentDim
      c.cooldown > 0             -> "cd ${c.cooldown}t" to CockpitTokens.TextC.Muted
      else                       -> "ready" to READY_GREEN
    }
    Text(
      text = statusText,
      modifier = Modifier.width(60.dp),
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        color = statusColor,
        textAlign = TextAlign.End,
      ),
    )

    // Hunger bar
    HungerBar(hunger = c.hunger, starving = starving)
  }
}

@Composable
private fun HungerBar(hunger: Float, starving: Boolean) {
  val fillColor = if (starving) CockpitTokens.TextC.Danger else HUNGER_FILL_NORMAL
  Box(
    modifier = Modifier
      .width(36.dp)
      .height(8.dp)
      .clip(RoundedCornerShape(1.dp))
      .background(Color.Black.copy(alpha = 0.18f))
      .border(1.dp, CockpitTokens.Border.ParchmentEdge, RoundedCornerShape(1.dp)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(hunger.coerceIn(0f, 1f))
        .background(fillColor),
    )
  }
}
