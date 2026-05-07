package world.clan.app.ui.screens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.R
import world.clan.app.data.CombinedComm
import world.clan.app.ui.components.ClanWorldTabBar
import world.clan.app.ui.components.CrownHeader
import world.clan.app.ui.components.LeaderboardRow
import world.clan.app.ui.components.ObsidianBackground
import world.clan.app.ui.components.RootTab
import world.clan.app.ui.components.SectionHeader
import world.clan.app.ui.components.StaggeredEntry
import world.clan.app.ui.components.WhisperAccent
import world.clan.app.ui.components.WhisperRow
import world.clan.app.ui.components.boldedMeta
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.clanColor
import world.clan.app.viewmodel.ClanWorldViewModelFactory
import world.clan.app.viewmodel.HearthUiState
import world.clan.app.viewmodel.HearthViewModel
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.clanTagline

@Composable
fun HearthScreenRoute(
  app: App,
  factory: ClanWorldViewModelFactory,
  onOpenInft: (Int) -> Unit,
) {
  val vm: HearthViewModel = viewModel(factory = factory)
  val state by vm.state.collectAsState()
  HearthScreen(state = state, onRefresh = vm::refresh)
}

@Composable
private fun HearthScreen(
  state: HearthUiState,
  onRefresh: () -> Unit = {},
) {
  // Background and tab bar are hosted at the app level (ClanWorldApp.kt);
  // this screen is just the page content.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp),
  ) {
    CrownHeader(
      screenName = "Hearth",
      modifier = Modifier.fillMaxWidth(),
    )

    if (state.errorMessage != null && state.leaderboard.isEmpty()) {
      world.clan.app.ui.components.RetryNotice(
        message = state.errorMessage,
        onRetry = onRefresh,
      )
      return@Column
    }

    world.clan.app.ui.components.RefreshableContent(
      isRefreshing = state.isRefreshing,
      onRefresh = onRefresh,
      modifier = Modifier.fillMaxSize(),
    ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(top = 14.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
          StaggeredEntry(index = 0) {
            HearthBanner(
              tick = state.tick,
              seasonNumber = state.seasonNumber,
              seasonStartTick = state.seasonStartTick,
              seasonEndTick = state.seasonEndTick,
              winterActive = state.winterActive,
            )
          }

          if (state.banditAlert != null) {
            StaggeredEntry(index = 1) {
              BanditAlertPill(alert = state.banditAlert)
            }
          }
          StaggeredEntry(index = 1) {
            SectionHeader(
              title = "Leaderboard",
              meta = "${state.leaderboard.count { (it.gold > 0L) }} of 8 active",
            )
          }
          StaggeredEntry(index = 2) {
            LeaderboardSurface(state.leaderboard, isLoading = state.isLoading)
          }

          StaggeredEntry(index = 3) {
            SectionHeader(
              title = "Whispers",
              meta = "tick %04d".format((state.tick - 1).coerceAtLeast(0L)),
            )
          }
      StaggeredEntry(index = 4) {
        WhispersList(state.recentComms, isLoading = state.isLoading)
      }
    }
    } // close RefreshableContent
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Banner
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun BanditAlertPill(alert: HearthUiState.BanditAlert) {
  val danger = ClanWorldTheme.colors.danger
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(danger.copy(alpha = 0.10f))
      .border(1.dp, danger.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(RoundedCornerShape(50))
        .background(danger),
    )
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = "BANDIT · ID ${alert.banditId}" + (alert.tier?.let { " · TIER $it" } ?: ""),
        style = ClanWorldTheme.type.monoMicro,
        color = danger,
      )
      Text(
        text = alert.regionName?.let { "rumored at $it." }
          ?: "moves through the realms.",
        style = ClanWorldTheme.type.scriptItalicSmall,
        color = warmDim,
      )
    }
  }
}

@Composable
private fun HearthBanner(
  tick: Long,
  seasonNumber: Int,
  seasonStartTick: Long,
  seasonEndTick: Long,
  winterActive: Boolean = false,
) {
  val iron = ClanWorldTheme.colors.iron
  val gold = ClanWorldTheme.colors.gold
  val goldDim = ClanWorldTheme.colors.goldDim
  val goldBright = ClanWorldTheme.colors.goldBright
  val ember = ClanWorldTheme.colors.ember
  val emberGlow = ClanWorldTheme.colors.emberGlow
  val hairline = ClanWorldTheme.colors.hairline
  val parchment = ClanWorldTheme.colors.parchment
  val warm = ClanWorldTheme.colors.warm
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val warmDim = ClanWorldTheme.colors.warmDim
  val rune = ClanWorldTheme.colors.rune

  // Season progress fraction
  val span = (seasonEndTick - seasonStartTick).coerceAtLeast(1L)
  val progressFrac = ((tick - seasonStartTick).toFloat() / span.toFloat()).coerceIn(0f, 1f)

  // Pulse for the bead riding the progress bar
  val pulse by rememberInfiniteTransition(label = "bead").animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOut), RepeatMode.Reverse),
    label = "p",
  )

  val seasonName = when (seasonNumber) {
    1 -> "Season I"
    2 -> "Season II"
    3 -> "Season III"
    4 -> "Season IV"
    else -> "Season $seasonNumber"
  }
  val seasonDay = (((tick - seasonStartTick).toInt()).coerceAtLeast(0)).coerceAtMost(span.toInt())

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(
        brush = Brush.verticalGradient(
          0f to gold.copy(alpha = 0.04f),
          1f to rune.copy(alpha = 0.02f),
        ),
      )
      .background(iron.copy(alpha = 0.92f))
      .border(width = 1.dp, color = hairline, shape = RoundedCornerShape(10.dp))
      .drawBehind {
        // Inner inset border @ 1dp
        drawRoundRect(
          color = goldDim.copy(alpha = 0.10f),
          topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
          size = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx()),
          style = Stroke(width = 1f),
        )
        // Four L-bracket corner ornaments
        val cs = 10.dp.toPx()
        val mg = 5.dp.toPx()
        val s = 1f
        // Top-left
        drawLine(goldDim, Offset(mg, mg), Offset(mg + cs, mg), s)
        drawLine(goldDim, Offset(mg, mg), Offset(mg, mg + cs), s)
        // Top-right
        drawLine(goldDim, Offset(size.width - mg, mg), Offset(size.width - mg - cs, mg), s)
        drawLine(goldDim, Offset(size.width - mg, mg), Offset(size.width - mg, mg + cs), s)
        // Bottom-left
        drawLine(goldDim, Offset(mg, size.height - mg), Offset(mg + cs, size.height - mg), s)
        drawLine(goldDim, Offset(mg, size.height - mg), Offset(mg, size.height - mg - cs), s)
        // Bottom-right
        drawLine(goldDim, Offset(size.width - mg, size.height - mg), Offset(size.width - mg - cs, size.height - mg), s)
        drawLine(goldDim, Offset(size.width - mg, size.height - mg), Offset(size.width - mg, size.height - mg - cs), s)
      }
      .padding(horizontal = 18.dp, vertical = 18.dp),
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Tick block
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("TICK", style = ClanWorldTheme.type.monoMicro, color = gold)
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "%04d".format(tick),
              style = ClanWorldTheme.type.tickNumber,
              color = parchment,
            )
            Spacer(Modifier.width(8.dp))
            Text(
              text = "/ ${seasonEndTick - seasonStartTick}",
              style = ClanWorldTheme.type.tickNumberDim,
              color = warmFaint,
            )
          }
        }
        // Season block
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.widthIn(min = 120.dp),
        ) {
          if (winterActive) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(rune.copy(alpha = 0.18f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
              Text(
                text = "WINTER",
                style = ClanWorldTheme.type.monoNano,
                color = rune,
              )
            }
          }
          Text(
            text = seasonName.uppercase(),
            style = ClanWorldTheme.type.crownLabel,
            color = warm,
          )
          Text(
            text = "day ${seasonDay.spelledOut()} of ${(span).toInt().spelledOut()}",
            style = ClanWorldTheme.type.scriptItalicSmall,
            color = warmDim,
          )
        }
      }

      Spacer(Modifier.height(14.dp))

      // Progress bar
      Box(
        Modifier
          .fillMaxWidth()
          .height(2.dp)
          .clip(RoundedCornerShape(1.dp))
          .background(goldDim.copy(alpha = 0.12f))
          .drawBehind {
            // Filled portion with gradient
            drawRect(
              brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                0.3f to gold,
                1f to goldBright,
              ),
              topLeft = Offset(0f, 0f),
              size = Size(size.width * progressFrac, size.height),
            )
          },
      )

      // Pulse bead — sits at progressFrac × parent width, riding above the bar
      Box(
        Modifier
          .fillMaxWidth()
          .height(0.dp)
          .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
              placeable.place(0, 0)
            }
          }
          .drawBehind {
            val cx = size.width * progressFrac
            val cy = -1.dp.toPx()
            val baseR = 3.5.dp.toPx()
            // Halo
            drawCircle(
              color = emberGlow.copy(alpha = (1f - pulse * 0.3f)),
              radius = baseR + (4.dp.toPx() * pulse),
              center = Offset(cx, cy),
            )
            // Solid core
            drawCircle(color = ember, radius = baseR, center = Offset(cx, cy))
          },
      )

      Spacer(Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text("S I", style = ClanWorldTheme.type.monoNano, color = warmFaint)
        Text("·",   style = ClanWorldTheme.type.monoNano, color = warmFaint)
        Text("S II", style = ClanWorldTheme.type.monoNano, color = warmFaint)
        Text("·",    style = ClanWorldTheme.type.monoNano, color = warmFaint)
        Text("S III", style = ClanWorldTheme.type.monoNano, color = warmFaint)
      }
    }
  }
}

// Simple spelled-out numbers up to 60 — used for "day fourteen of thirty".
private val SPELLED = arrayOf(
  "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
  "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
  "seventeen", "eighteen", "nineteen", "twenty",
)
private val TENS = mapOf(20 to "twenty", 30 to "thirty", 40 to "forty", 50 to "fifty", 60 to "sixty")
private fun Int.spelledOut(): String {
  if (this in SPELLED.indices) return SPELLED[this]
  val ten = (this / 10) * 10
  val ones = this % 10
  val tens = TENS[ten] ?: this.toString()
  return if (ones == 0) tens else "$tens-${SPELLED[ones]}"
}

// ─────────────────────────────────────────────────────────────────────────
// Leaderboard
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun LeaderboardSurface(
  rows: List<HearthUiState.LeaderboardRow>,
  isLoading: Boolean = false,
) {
  val hairline = ClanWorldTheme.colors.hairline
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(hairline) // hairline color shows through 1dp gaps between rows
      .border(1.dp, hairline, RoundedCornerShape(6.dp)),
    verticalArrangement = Arrangement.spacedBy(1.dp),
  ) {
    if (rows.isEmpty() && isLoading) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(ClanWorldTheme.colors.iron)
          .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        repeat(4) { world.clan.app.ui.components.SkeletonLeaderboardRow() }
      }
    } else if (rows.isEmpty()) {
      Box(
        Modifier
          .fillMaxWidth()
          .background(ClanWorldTheme.colors.iron)
          .padding(20.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "the realms are quiet…",
          style = ClanWorldTheme.type.scriptItalic,
          color = ClanWorldTheme.colors.warmFaint,
        )
      }
    } else {
      rows.take(8).forEach { row ->
        LeaderboardRow(
          rank = row.rank,
          clanColor = clanColor(row.clanId),
          glyph = painterResource(world.clan.app.ui.theme.clanGlyphRes(row.clanId)),
          clanName = row.name,
          clanTagline = row.tagline,
          gold = row.gold,
          delta = row.delta,
          isMine = row.isMine,
        )
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Whispers
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun WhispersList(
  comms: List<CombinedComm>,
  isLoading: Boolean = false,
) {
  if (comms.isEmpty() && isLoading) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      repeat(2) { world.clan.app.ui.components.SkeletonWhisperRow() }
    }
    return
  }
  if (comms.isEmpty()) {
    Text(
      text = "no whispers in the wind…",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmFaint,
      modifier = Modifier.padding(start = 12.dp),
    )
    return
  }
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    comms.take(3).forEach { c ->
      val accent = when (c.kind) {
        "whisper" -> WhisperAccent.Rune
        "orch" -> WhisperAccent.Gold
        "human" -> WhisperAccent.Ember
        else -> WhisperAccent.Default
      }
      WhisperRow(
        meta = whisperMetaText(c),
        body = c.body,
        accent = accent,
      )
    }
  }
}

@Composable
private fun whisperMetaText(c: CombinedComm): AnnotatedString {
  val from = c.fromClan?.let { clanDisplayName(it) }
    ?: c.speaker
    ?: when (c.kind) {
      "orch" -> "Orchestrator"
      "human" -> "Owner"
      else -> "—"
    }
  val to = c.targetClan?.let { clanDisplayName(it) }
  val tickStr = c.tick?.let { "%04d".format(it) } ?: "—"
  val raw = when (c.kind) {
    "whisper" -> if (to != null) "*$from* · whispered to · *$to* · $tickStr"
                 else "*$from* · whispered · $tickStr"
    "orch" -> "*Orchestrator* · $tickStr"
    "human" -> "*$from* · steered · $tickStr"
    else -> "*$from* · $tickStr"
  }
  return boldedMeta(raw.uppercase())
}
