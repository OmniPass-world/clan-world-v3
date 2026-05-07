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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.R
import world.clan.app.data.CombinedComm
import world.clan.app.ui.components.WhisperAccent
import world.clan.app.ui.components.WhisperRow
import world.clan.app.ui.components.boldedMeta
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.viewmodel.WhispersFilter
import world.clan.app.viewmodel.WhispersUiState
import world.clan.app.viewmodel.WhispersViewModel
import world.clan.app.viewmodel.WhispersViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.filtered

@Composable
fun WhispersScreenRoute(
  app: App,
  clanId: Int,
  onBack: () -> Unit,
  onCompose: () -> Unit = {},
) {
  val vm: WhispersViewModel = viewModel(factory = WhispersViewModelFactory(app, clanId))
  val state by vm.state.collectAsState()
  WhispersScreen(
    state = state,
    clanId = clanId,
    onBack = onBack,
    onSetFilter = vm::setFilter,
    onCompose = onCompose,
    onRefresh = vm::refresh,
  )
}

@Composable
private fun WhispersScreen(
  state: WhispersUiState,
  clanId: Int,
  onBack: () -> Unit,
  onSetFilter: (WhispersFilter) -> Unit,
  onCompose: () -> Unit = {},
  onRefresh: () -> Unit = {},
) {
  Column(modifier = Modifier.fillMaxSize()) {
    InboxBackBar(clanId = clanId, onBack = onBack)

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
      InboxHead(clanId = clanId, count = state.filtered().size)
      Spacer(Modifier.height(12.dp))
      FilterRow(active = state.filter, onSelect = onSetFilter)
      Spacer(Modifier.height(10.dp))
      Text(
        text = "+ NEW WHISPER",
        style = ClanWorldTheme.type.monoMicro,
        color = ClanWorldTheme.colors.gold,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onCompose() }
          .padding(vertical = 8.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      )
      Spacer(Modifier.height(8.dp))
    }

    val items = state.filtered()
    when {
      state.isLoading -> {
        Column(
          modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          repeat(5) { world.clan.app.ui.components.SkeletonWhisperRow() }
        }
      }
      state.errorMessage != null && items.isEmpty() -> {
        world.clan.app.ui.components.RetryNotice(
          message = state.errorMessage,
          onRetry = onRefresh,
        )
      }
      else -> {
        world.clan.app.ui.components.RefreshableContent(
          isRefreshing = state.isLoading,
          onRefresh = onRefresh,
          modifier = Modifier.fillMaxSize(),
        ) {
          if (items.isEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
              item {
                val isFiltered = state.filter != WhispersFilter.All
                world.clan.app.ui.components.EmptyState(
                  title = if (isFiltered) "no whispers carry this kind" else "the inbox is silent",
                  body = if (isFiltered) "try another filter, or compose one yourself."
                  else "no whispers heard yet — be the first to speak.",
                  ctaLabel = if (isFiltered) null else "+ New whisper",
                  onCta = if (isFiltered) null else onCompose,
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
            ) {
              items(items.withIndex().toList(), key = { (i, c) -> "${i}-${c.tick ?: c.timestamp ?: 0}" }) { (_, c) ->
                InboxRow(comm = c)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun InboxBackBar(clanId: Int, onBack: () -> Unit) {
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
      text = "back to ${clanDisplayName(clanId).lowercase()}",
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.warmDim,
    )
  }
}

@Composable
private fun InboxHead(clanId: Int, count: Int) {
  val parchment = ClanWorldTheme.colors.parchment
  val warmDim = ClanWorldTheme.colors.warmDim
  val gold = ClanWorldTheme.colors.gold
  val hairline = ClanWorldTheme.colors.hairline

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp, bottom = 4.dp)
      .drawBehind {
        drawLine(
          color = hairline,
          start = Offset(0f, size.height + 14f),
          end = Offset(size.width, size.height + 14f),
          strokeWidth = 1f,
        )
      },
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column {
      Text(
        text = "INBOX",
        style = ClanWorldTheme.type.displayHero,
        color = parchment,
      )
      Text(
        text = "whispers heard at ${clanDisplayName(clanId).lowercase()}",
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
        text = "shown",
        style = ClanWorldTheme.type.monoMicro,
        color = gold,
      )
    }
  }
}

@Composable
private fun FilterRow(active: WhispersFilter, onSelect: (WhispersFilter) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(label = "All", selected = active == WhispersFilter.All, onClick = { onSelect(WhispersFilter.All) })
    FilterChip(label = "Peers", selected = active == WhispersFilter.Whisper, onClick = { onSelect(WhispersFilter.Whisper) })
    FilterChip(label = "Orch", selected = active == WhispersFilter.Orch, onClick = { onSelect(WhispersFilter.Orch) })
    FilterChip(label = "Owner", selected = active == WhispersFilter.Human, onClick = { onSelect(WhispersFilter.Human) })
  }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
  val bg = if (selected) ClanWorldTheme.colors.iron2 else Color.Transparent
  val border = if (selected) ClanWorldTheme.colors.hairlineStrong else ClanWorldTheme.colors.hairline
  val tint = if (selected) ClanWorldTheme.colors.parchment else ClanWorldTheme.colors.warmDim
  val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bg)
      .drawBehind {
        drawLine(
          color = border,
          start = Offset(0f, 0f),
          end = Offset(size.width, 0f),
          strokeWidth = 1f,
        )
        drawLine(
          color = border,
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1f,
        )
      }
      .clickable {
        if (!selected) {
          haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
        onClick()
      }
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
private fun InboxRow(comm: CombinedComm) {
  val accent = when (comm.kind) {
    "whisper" -> WhisperAccent.Rune
    "orch" -> WhisperAccent.Gold
    "human" -> WhisperAccent.Ember
    else -> WhisperAccent.Default
  }
  WhisperRow(
    meta = world.clan.app.ui.components.whisperMetaText(comm),
    body = comm.body,
    accent = accent,
  )
}
