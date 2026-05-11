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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.BuildConfig
import world.clan.app.cockpit.tabs.shared.SectionHeader
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.VaultMovementWire
import world.clan.app.data.convex.findClan
import world.clan.app.data.convex.toDomain
import world.clan.app.data.convex.toResources
import world.clan.app.data.convex.useSnapshot
import world.clan.app.data.convex.useVaultMovements
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private val GAIN_GREEN = Color(0xFF3A7A3A)

/**
 * Vault tab. Visual port of apps/web/src/components/cockpit/tabs/VaultTab.tsx:
 *  - parchment background, 12dp padding, scrollable
 *  - "RESOURCES" 2-col grid of cards (glyph + label + value + status)
 *  - "ASSET MOVEMENTS" live movement feed from Convex
 *
 * Data is sourced from `StubData` per the plan's deferred data wiring.
 */
@Composable
fun VaultTab(elder: Elder, modifier: Modifier = Modifier) {
  val snapshotState = useSnapshot()
  val movementsState = useVaultMovements(elder.clanId)

  val resources = when (snapshotState) {
    is QueryState.Live -> snapshotState.data.findClan(elder.clanId)?.toResources()
      ?: if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.vaultResources(elder.clanId) else emptyList()
    else -> if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.vaultResources(elder.clanId) else emptyList()
  }
  val tickLabel = (snapshotState as? QueryState.Live)?.data?.tick?.toInt()
    ?: if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.CURRENT_TICK else 0

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Parchment)
      .verticalScroll(rememberScrollState())
      .padding(CockpitTokens.Space.md),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.md),
  ) {
    SectionHeader("RESOURCES", trailing = "T$tickLabel")

    // 2-column grid
    val rowsOfTwo = resources.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm)) {
      rowsOfTwo.forEach { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm)) {
          pair.forEach { res ->
            ResourceCard(
              resource = res,
              accent = elder.accent,
              modifier = Modifier.weight(1f),
            )
          }
          if (pair.size == 1) Box(modifier = Modifier.weight(1f))
        }
      }
    }

    SectionHeader("ASSET MOVEMENTS")
    MovementList(movementsState)
  }
}

@Composable
private fun MovementList(state: QueryState<List<VaultMovementWire>>) {
  when (state) {
    is QueryState.Live -> {
      val movements = state.data.map { it.toDomain() }
      if (movements.isEmpty()) {
        EmptyMovementState("no live asset movements yet")
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          movements.forEach { m -> MovementRow(m) }
        }
      }
    }
    QueryState.Loading -> EmptyMovementState("loading asset movements...")
    QueryState.Stub -> EmptyMovementState("no live asset movements yet")
    is QueryState.Error -> EmptyMovementState("asset movements unavailable")
  }
}

@Composable
private fun EmptyMovementState(message: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.White.copy(alpha = 0.18f))
      .border(1.dp, CockpitTokens.Border.ParchmentEdge)
      .padding(horizontal = 10.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = message,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        color = CockpitTokens.TextC.OnParchmentDim,
      ),
    )
  }
}

@Composable
private fun ResourceCard(
  resource: StubData.VaultResource,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(Color.White.copy(alpha = 0.35f))
      .border(1.dp, CockpitTokens.Border.ParchmentEdge, RoundedCornerShape(CockpitTokens.Radius.sm))
      .padding(CockpitTokens.Space.sm),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    Text(
      text = resource.glyph,
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 20.sp,
        color = accent,
      ),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = resource.label.uppercase(),
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 9.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.OnParchmentDim,
          letterSpacing = 0.72.sp,
        ),
      )
      Text(
        text = resource.value.toString(),
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.OnParchment,
        ),
      )
    }
  }
}

@Composable
private fun MovementRow(m: StubData.VaultMovement) {
  val isGain = m.type == StubData.MovementType.Gain
  val borderColor = if (isGain) GAIN_GREEN else CockpitTokens.TextC.Danger

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .background(Color.White.copy(alpha = 0.18f)),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .width(2.dp)
        .fillMaxHeight()
        .background(borderColor),
    )
    Row(
      modifier = Modifier
        .padding(horizontal = 6.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "T${m.tick.toString().padStart(2, '0')}",
        modifier = Modifier.width(32.dp),
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 11.sp,
          color = CockpitTokens.TextC.Muted,
        ),
      )
      Text(
        text = m.amount,
        modifier = Modifier.width(76.dp),
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = borderColor,
        ),
      )
      Text(
        text = m.source,
        modifier = Modifier.weight(1f),
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 11.sp,
          color = CockpitTokens.TextC.OnParchment,
        ),
      )
    }
  }
}
