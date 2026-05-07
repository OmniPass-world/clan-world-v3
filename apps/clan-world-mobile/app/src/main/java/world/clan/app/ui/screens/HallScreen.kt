package world.clan.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.ui.components.ClanWorldTabBar
import world.clan.app.ui.components.CrownHeader
import world.clan.app.ui.components.ObsidianBackground
import world.clan.app.ui.components.ParchmentCard
import world.clan.app.ui.components.RootTab
import world.clan.app.ui.components.StaggeredEntry
import world.clan.app.ui.components.WaxSeal
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.CormorantItalic
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.ui.theme.clanGlyphRes
import world.clan.app.viewmodel.ClanWorldViewModelFactory
import world.clan.app.viewmodel.HallCard
import world.clan.app.viewmodel.HallProvenance
import world.clan.app.viewmodel.HallUiState
import world.clan.app.viewmodel.HallViewModel
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.clanTagline

@Composable
fun HallScreenRoute(
  app: App,
  factory: ClanWorldViewModelFactory,
  onOpenInft: (Int) -> Unit,
  onForge: () -> Unit = {},
) {
  val vm: HallViewModel = viewModel(factory = factory)
  val state by vm.state.collectAsState()
  // Refresh on every (re)entry into the Hall route. The HallViewModel is
  // held by the NavHost across navigations (Hall → Forge/Hire → celebration
  // → back), so without this LaunchedEffect the user sees a stale list
  // until pull-to-refresh. SessionStore.add{Forged,Hired}ClanId() are
  // called before the celebration screen lands, so by the time we pop
  // back to Hall the extras are already present.
  LaunchedEffect(Unit) {
    vm.refresh()
  }
  // First-launch coachmark: read once from SessionStore, dismiss-on-tap
  // toggles the local state and writes the persistent flag.
  val hintFlagKey = "hall.hintSeen"
  val hintInitial = remember { !app.sessionStore.hasSeenFlag(hintFlagKey) }
  var hintVisible by remember { mutableStateOf(hintInitial) }
  HallScreen(
    state = state,
    onOpenInft = onOpenInft,
    onForge = onForge,
    onRefresh = vm::refresh,
    showHint = hintVisible,
    onDismissHint = {
      hintVisible = false
      app.sessionStore.markFlagSeen(hintFlagKey)
    },
  )
}

@Composable
private fun HallScreen(
  state: HallUiState,
  onOpenInft: (Int) -> Unit,
  onForge: () -> Unit = {},
  onRefresh: () -> Unit = {},
  showHint: Boolean = false,
  onDismissHint: () -> Unit = {},
) {
  // Background and tab bar are app-level (ClanWorldApp.kt).
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp),
  ) {
    CrownHeader(
      screenName = "Hall",
      modifier = Modifier.fillMaxWidth(),
    )

    // Hall head: title + count
    StaggeredEntry(index = 0) {
      HallHead(count = state.items.size)
    }

    // First-launch coachmark — points new users at the FORGE entry below.
    Spacer(Modifier.height(14.dp))
    world.clan.app.ui.components.OnboardingHint(
      visible = showHint,
      title = "Begin your line",
      body = "Tap “+ Forge a new sigil” below to mint your first iNFT.",
      onDismiss = onDismissHint,
    )

    // Forge entry — sits between the head divider and the list. Always
    // available; an empty hall still wants the option to forge a new one.
    Text(
      text = "+ FORGE A NEW SIGIL",
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.gold,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 18.dp, bottom = 8.dp)
        .clickable { onForge() }
        .padding(vertical = 8.dp),
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )

    Spacer(Modifier.height(10.dp))

    if (state.isLoading) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        repeat(3) { world.clan.app.ui.components.SkeletonLetterCard() }
      }
    } else if (state.errorMessage != null && state.items.isEmpty()) {
      world.clan.app.ui.components.RetryNotice(
        message = state.errorMessage,
        onRetry = onRefresh,
      )
    } else {
      world.clan.app.ui.components.RefreshableContent(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
      ) {
        if (state.items.isEmpty()) {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
              world.clan.app.ui.components.EmptyState(
                title = "your hall is quiet",
                body = "no sigils stand under your hand yet — forge one to begin.",
                ctaLabel = "+ Forge a sigil",
                onCta = onForge,
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
          ) {
            items(state.items, key = { it.tokenId }) { card ->
              StaggeredEntry(index = card.tokenId.coerceAtMost(8)) {
                LetterCard(
                  card = card,
                  onClick = { onOpenInft(card.clanId) },
                )
              }
            }
          }
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun HallHead(count: Int) {
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
        text = "MY HALL",
        style = ClanWorldTheme.type.displayHero,
        color = parchment,
      )
      Text(
        text = sigilLine(count),
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
        text = "of viii",
        style = ClanWorldTheme.type.monoMicro,
        color = gold,
      )
    }
  }
}

private fun sigilLine(n: Int) = when (n) {
  0 -> "no sigils stand under your hand"
  1 -> "one sigil stands under your hand"
  2 -> "two sigils stand under your hand"
  3 -> "three sigils stand under your hand"
  4 -> "four sigils stand under your hand"
  5 -> "five sigils stand under your hand"
  6 -> "six sigils stand under your hand"
  7 -> "seven sigils stand under your hand"
  8 -> "all eight sigils stand under your hand"
  else -> "$n sigils stand under your hand"
}

@Composable
private fun LetterCard(
  card: HallCard,
  onClick: () -> Unit,
) {
  ParchmentCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
  ) {
    val clanCol = clanColor(card.clanId)

    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = letterTitle(card.clanId),
            style = ClanWorldTheme.type.letterName,
            color = Ink,
            modifier = Modifier.weight(1f, fill = false),
          )
          ProvenanceBadge(card.provenance)
        }
        Text(
          text = "tkn 0x${"%04x".format(card.tokenId)} · clan ${roman(card.clanId).lowercase()}",
          style = ClanWorldTheme.type.monoMicro,
          color = Ink3,
          modifier = Modifier.padding(top = 2.dp),
        )
      }
      WaxSeal(
        glyph = painterResource(clanGlyphRes(card.clanId)),
        clanColor = clanCol,
      )
    }

    Spacer(Modifier.height(8.dp))

    Text(
      text = letterFlavor(card),
      style = ClanWorldTheme.type.scriptItalic,
      color = Ink2,
    )

    Spacer(Modifier.height(12.dp))

    // Dashed top divider
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
      LetterStat(label = "Hunger", value = "tended", color = ClanWorldTheme.colors.success)
      LetterStat(label = "Memory", value = "${card.memoryCount} keys", color = Ink)
      LetterStat(
        label = "Last Move",
        value = card.mostRecentTransferTick?.let { "T %04d".format(it) } ?: "—",
        color = Ink,
      )
    }
  }
}

@Composable
private fun ProvenanceBadge(provenance: HallProvenance) {
  if (provenance == HallProvenance.Linked) return
  val (label, accent) = when (provenance) {
    HallProvenance.Hired -> "HIRED" to ClanWorldTheme.colors.rune
    HallProvenance.Forged -> "FORGED" to ClanWorldTheme.colors.ember
    HallProvenance.Linked -> return
  }
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(2.dp))
      .background(accent.copy(alpha = 0.16f))
      .padding(horizontal = 6.dp, vertical = 3.dp),
  ) {
    Text(
      text = label,
      style = ClanWorldTheme.type.monoNano,
      color = accent,
    )
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.LetterStat(
  label: String,
  value: String,
  color: Color,
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

@Composable
private fun letterTitle(clanId: Int): AnnotatedString {
  val name = clanDisplayName(clanId)
  val house = when (clanId) {
    1 -> "of the Storm Pass"
    2 -> "of the Strait"
    3 -> "of Sunhold"
    4 -> "of the Vale"
    5 -> "of the Watch"
    6 -> "of the Ember"
    7 -> "of the Forge"
    8 -> "of the Star"
    else -> ""
  }
  val tail = when (clanId) {
    1 -> "Storm-Edge"
    2 -> "Tideborne"
    3 -> "Sunhold"
    4 -> "Vale-Ward"
    5 -> "Twilight"
    6 -> "Ember"
    7 -> "Forge"
    8 -> "Star"
    else -> name
  }
  return buildAnnotatedString {
    append("$house  ")
    withStyle(
      SpanStyle(
        fontFamily = CormorantItalic,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium,
        color = Ink2,
        fontSize = 18.sp,
      ),
    ) {
      append(tail)
    }
  }
}

private fun letterFlavor(card: HallCard): String {
  val tagline = clanTagline(card.clanId)
  val sealedAt = card.sealedAtTick
  return when {
    sealedAt != null -> "Sealed at tick $sealedAt. ${tagline.replaceFirstChar { it.uppercase() }}."
    else -> "Newly forged. ${tagline.replaceFirstChar { it.uppercase() }}."
  }
}

private fun roman(n: Int): String = when (n) {
  1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"
  5 -> "V"; 6 -> "VI"; 7 -> "VII"; 8 -> "VIII"
  else -> n.toString()
}
