package world.clan.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.data.BazaarListing
import world.clan.app.ui.components.CrownHeader
import world.clan.app.ui.components.ParchmentCard
import world.clan.app.ui.components.StaggeredEntry
import world.clan.app.ui.components.WaxSeal
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.ui.theme.clanGlyphRes
import world.clan.app.viewmodel.BazaarUiState
import world.clan.app.viewmodel.BazaarViewModel
import world.clan.app.viewmodel.ClanWorldViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.clanTagline

@Composable
fun BazaarScreenRoute(
  app: App,
  factory: ClanWorldViewModelFactory,
  onOpenListing: (Int) -> Unit,
) {
  val vm: BazaarViewModel = viewModel(factory = factory)
  val state by vm.state.collectAsState()
  // Re-apply the hired-clan filter on (re-)entry so a freshly-hired
  // sigil drops out of the listings without an app kill.
  androidx.compose.runtime.LaunchedEffect(Unit) { vm.refresh() }
  BazaarScreen(state = state, onOpenListing = onOpenListing)
}

@Composable
private fun BazaarScreen(
  state: BazaarUiState,
  onOpenListing: (Int) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp),
  ) {
    CrownHeader(
      screenName = "Bazaar",
      modifier = Modifier.fillMaxWidth(),
    )

    StaggeredEntry(index = 0) {
      BazaarHead(count = state.listings.size)
    }

    Spacer(Modifier.height(18.dp))

    if (state.isLoading) {
      Text(
        text = "the merchants are setting their stalls…",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warmFaint,
        modifier = Modifier.padding(top = 24.dp),
      )
    } else if (state.listings.isEmpty()) {
      Text(
        text = state.errorMessage ?: "the bazaar is quiet — no listings posted.",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warmFaint,
        modifier = Modifier.padding(top = 24.dp),
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
      ) {
        items(state.listings, key = { it.tokenId }) { listing ->
          StaggeredEntry(index = listing.clanId.coerceAtMost(8)) {
            ListingCard(
              listing = listing,
              onClick = { onOpenListing(listing.clanId) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BazaarHead(count: Int) {
  val parchment = ClanWorldTheme.colors.parchment
  val warmDim = ClanWorldTheme.colors.warmDim
  val gold = ClanWorldTheme.colors.gold
  val hairline = ClanWorldTheme.colors.hairline

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 14.dp, bottom = 4.dp)
      .drawBehind {
        drawLine(
          color = hairline,
          start = Offset(0f, size.height + 18f),
          end = Offset(size.width, size.height + 18f),
          strokeWidth = 1f,
        )
      },
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column {
      Text(
        text = "BAZAAR",
        style = ClanWorldTheme.type.displayHero,
        color = parchment,
      )
      Text(
        text = "merchants of the realm — sigils for hire",
        style = ClanWorldTheme.type.scriptItalic,
        color = warmDim,
        modifier = Modifier.padding(top = 2.dp),
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = "%02d".format(count),
        style = ClanWorldTheme.type.monoBig,
        color = parchment,
      )
      Text(
        text = "listings",
        style = ClanWorldTheme.type.monoMicro,
        color = gold,
      )
    }
  }
}

@Composable
private fun ListingCard(
  listing: BazaarListing,
  onClick: () -> Unit,
) {
  ParchmentCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
  ) {
    val clanCol = clanColor(listing.clanId)

    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = clanDisplayName(listing.clanId),
          style = ClanWorldTheme.type.letterName,
          color = Ink,
        )
        Text(
          text = "tkn 0x${"%04x".format(listing.tokenId)} · clan ${roman(listing.clanId).lowercase()}",
          style = ClanWorldTheme.type.monoMicro,
          color = Ink3,
          modifier = Modifier.padding(top = 2.dp),
        )
      }
      WaxSeal(
        glyph = painterResource(clanGlyphRes(listing.clanId)),
        clanColor = clanCol,
      )
    }

    Spacer(Modifier.height(8.dp))

    Text(
      text = listing.pitch,
      style = ClanWorldTheme.type.scriptItalic,
      color = Ink2,
    )

    Spacer(Modifier.height(12.dp))

    Box(
      Modifier
        .fillMaxWidth()
        .height(1.dp)
        .drawBehind {
          drawLine(
            color = Ink.copy(alpha = 0.20f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())),
          )
        },
    )

    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
      ListingStat(label = "Price", value = "${listing.pricePerSeason}g/season", color = ClanWorldTheme.colors.gold)
      ListingStat(label = "Status", value = listing.availability.substringBefore("—").trim(), color = ClanWorldTheme.colors.success)
      ListingStat(label = "Owner", value = listing.ownerShort, color = Ink3)
    }
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ListingStat(
  label: String,
  value: String,
  color: androidx.compose.ui.graphics.Color,
) {
  Column(Modifier.weight(1f)) {
    Text(
      text = label.uppercase(),
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
    )
    Text(
      text = value,
      style = ClanWorldTheme.type.monoData,
      color = color,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}

private fun roman(n: Int): String = when (n) {
  1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"
  5 -> "V"; 6 -> "VI"; 7 -> "VII"; 8 -> "VIII"
  else -> n.toString()
}
