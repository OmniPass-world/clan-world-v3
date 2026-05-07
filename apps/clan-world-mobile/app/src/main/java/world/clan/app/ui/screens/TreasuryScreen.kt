package world.clan.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.R
import world.clan.app.data.VaultMovement
import world.clan.app.ui.components.ParchmentCard
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.viewmodel.ResourceBalance
import world.clan.app.viewmodel.TreasuryFilter
import world.clan.app.viewmodel.TreasuryUiState
import world.clan.app.viewmodel.TreasuryViewModel
import world.clan.app.viewmodel.TreasuryViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.filteredMovements

@Composable
fun TreasuryScreenRoute(
  app: App,
  clanId: Int,
  onBack: () -> Unit,
) {
  val vm: TreasuryViewModel = viewModel(factory = TreasuryViewModelFactory(app, clanId))
  val state by vm.state.collectAsState()
  TreasuryScreen(
    state = state,
    clanId = clanId,
    onBack = onBack,
    onSetFilter = vm::setFilter,
    onRefresh = vm::refresh,
  )
}

@Composable
private fun TreasuryScreen(
  state: TreasuryUiState,
  clanId: Int,
  onBack: () -> Unit,
  onSetFilter: (TreasuryFilter) -> Unit,
  onRefresh: () -> Unit = {},
) {
  Column(modifier = Modifier.fillMaxSize()) {
    BackBar(text = "back to ${clanDisplayName(clanId).lowercase()}", onBack = onBack)

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
      TreasuryHead(clanId = clanId, balance = state.balance)
      Spacer(Modifier.height(14.dp))

      if (state.isLoading) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          world.clan.app.ui.components.SkeletonTreasuryHero()
          Spacer(Modifier.height(2.dp))
          repeat(5) { world.clan.app.ui.components.SkeletonMovementRow() }
        }
      } else if (state.errorMessage != null) {
        world.clan.app.ui.components.RetryNotice(
          message = state.errorMessage,
          onRetry = onRefresh,
        )
      } else {
        GoldHeroCard(gold = state.balance.gold, clanId = clanId)
        Spacer(Modifier.height(12.dp))
        ResourceStripCard(balance = state.balance)
        Spacer(Modifier.height(18.dp))
        Text(
          text = "MOVEMENTS",
          style = ClanWorldTheme.type.monoNano,
          color = ClanWorldTheme.colors.warmFaint,
        )
        Spacer(Modifier.height(8.dp))
        FilterRow(active = state.filter, onSelect = onSetFilter)
        Spacer(Modifier.height(10.dp))
      }
    }

    if (!state.isLoading && state.errorMessage == null) {
      val items = state.filteredMovements()
      world.clan.app.ui.components.RefreshableContent(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
      ) {
        if (items.isEmpty()) {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
              Text(
                text = "the vault keeps its silence",
                style = ClanWorldTheme.type.scriptItalic,
                color = ClanWorldTheme.colors.warmFaint,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
          ) {
            items(items.withIndex().toList(), key = { (i, m) -> "${i}-${m.tick}-${m.resource}" }) { (_, mv) ->
              MovementRow(mv)
            }
            item { Spacer(Modifier.height(40.dp)) }
          }
        }
      }
    }
  }
}

@Composable
private fun BackBar(text: String, onBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp, vertical = 14.dp)
      .clickable { onBack() },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(
      painter = painterResource(R.drawable.ui_arrow),
      contentDescription = "back",
      tint = ClanWorldTheme.colors.warm,
      modifier = Modifier.height(16.dp),
    )
    Text(
      text = text,
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.warmDim,
    )
  }
}

@Composable
private fun TreasuryHead(clanId: Int, balance: ResourceBalance) {
  val parchment = ClanWorldTheme.colors.parchment
  val warmDim = ClanWorldTheme.colors.warmDim
  val hairline = ClanWorldTheme.colors.hairline

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .drawBehind {
        drawLine(
          color = hairline,
          start = Offset(0f, size.height + 14f),
          end = Offset(size.width, size.height + 14f),
          strokeWidth = 1f,
        )
      },
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom,
  ) {
    Column {
      Text(
        text = "TREASURY",
        style = ClanWorldTheme.type.displayHero,
        color = parchment,
      )
      Text(
        text = "the keeping of ${clanDisplayName(clanId).lowercase()}",
        style = ClanWorldTheme.type.scriptItalic,
        color = warmDim,
        modifier = Modifier.padding(top = 2.dp),
      )
    }
  }
}

@Composable
private fun GoldHeroCard(gold: Long, clanId: Int) {
  ParchmentCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "GOLD",
          style = ClanWorldTheme.type.monoNano,
          color = Ink3,
        )
        Text(
          text = formatGold(gold),
          style = ClanWorldTheme.type.displayHero,
          color = Ink,
          modifier = Modifier.padding(top = 4.dp),
        )
        Text(
          text = "stored under ${clanDisplayName(clanId).lowercase()}",
          style = ClanWorldTheme.type.scriptItalic,
          color = Ink2,
          modifier = Modifier.padding(top = 2.dp),
        )
      }
      // accent dot in clan color
      Box(
        modifier = Modifier
          .padding(start = 8.dp)
          .clip(RoundedCornerShape(50))
          .background(clanColor(clanId))
          .padding(8.dp),
      ) {
        Text(
          text = " ",
          style = ClanWorldTheme.type.monoNano,
        )
      }
    }
  }
}

@Composable
private fun ResourceStripCard(balance: ResourceBalance) {
  ParchmentCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "RESOURCES",
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
    )
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ResourceCell(label = "Wood", value = balance.wood)
      ResourceCell(label = "Iron", value = balance.iron)
      ResourceCell(label = "Wheat", value = balance.wheat)
      ResourceCell(label = "Fish", value = balance.fish)
    }
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ResourceCell(label: String, value: Long) {
  Column(Modifier.weight(1f)) {
    Text(
      text = label.uppercase(),
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
    )
    Text(
      text = "%,d".format(value),
      style = ClanWorldTheme.type.monoData,
      color = Ink,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}

@Composable
private fun FilterRow(active: TreasuryFilter, onSelect: (TreasuryFilter) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(label = "All", selected = active == TreasuryFilter.All, onClick = { onSelect(TreasuryFilter.All) })
    FilterChip(label = "Gold", selected = active == TreasuryFilter.Gold, onClick = { onSelect(TreasuryFilter.Gold) })
    FilterChip(label = "Resources", selected = active == TreasuryFilter.Resources, onClick = { onSelect(TreasuryFilter.Resources) })
  }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
  val bg = if (selected) ClanWorldTheme.colors.iron2 else Color.Transparent
  val border = if (selected) ClanWorldTheme.colors.hairlineStrong else ClanWorldTheme.colors.hairline
  val tint = if (selected) ClanWorldTheme.colors.parchment else ClanWorldTheme.colors.warmDim
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bg)
      .drawBehind {
        drawRoundRect(
          color = border,
          cornerRadius = CornerRadius(4.dp.toPx()),
          style = Stroke(width = 1.dp.toPx()),
        )
      }
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 8.dp),
  ) {
    Text(
      text = label.uppercase(),
      style = ClanWorldTheme.type.monoMicro,
      color = tint,
    )
  }
}

@Composable
private fun MovementRow(mv: VaultMovement) {
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val gold = ClanWorldTheme.colors.gold
  val danger = ClanWorldTheme.colors.danger
  val rune = ClanWorldTheme.colors.rune
  val hairline = ClanWorldTheme.colors.hairline

  val sign = if (mv.type == "spend") "−" else "+"
  val amountColor = when (mv.type) {
    "spend" -> danger
    "transfer" -> rune
    else -> gold
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .drawBehind {
        drawLine(
          color = hairline,
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1f,
        )
      }
      .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = "T %04d".format(mv.tick),
      style = ClanWorldTheme.type.monoNano,
      color = warmFaint,
      modifier = Modifier.padding(end = 4.dp),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = mv.resource.replaceFirstChar { it.uppercase() },
        style = ClanWorldTheme.type.body,
        color = warm,
      )
      mv.source?.takeIf { it.isNotBlank() }?.let { src ->
        Text(
          text = src,
          style = ClanWorldTheme.type.monoNano,
          color = warmDim,
          modifier = Modifier.padding(top = 2.dp),
        )
      }
    }
    Text(
      text = "$sign%.2f".format(mv.amount),
      style = ClanWorldTheme.type.monoData,
      color = amountColor,
    )
  }
}

private fun formatGold(value: Long): String =
  if (value >= 1_000_000) "%.1fM".format(value / 1_000_000.0)
  else if (value >= 1_000) "%.1fk".format(value / 1_000.0)
  else "%,d".format(value)
