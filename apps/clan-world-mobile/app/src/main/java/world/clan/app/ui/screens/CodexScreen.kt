package world.clan.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.R
import world.clan.app.ui.components.ClanWorldTabBar
import world.clan.app.ui.components.CrownHeader
import world.clan.app.ui.components.ObsidianBackground
import world.clan.app.ui.components.RootTab
import world.clan.app.ui.components.SectionHeader
import world.clan.app.ui.components.StaggeredEntry
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.viewmodel.ClanWorldViewModelFactory
import world.clan.app.viewmodel.CodexUiState
import world.clan.app.viewmodel.CodexViewModel
import world.clan.app.viewmodel.shortenPubkey

@Composable
fun CodexScreenRoute(
  app: App,
  factory: ClanWorldViewModelFactory,
  onOpenInft: (Int) -> Unit = {},
) {
  val vm: CodexViewModel = viewModel(factory = factory)
  val state by vm.state.collectAsState()
  // Re-read lineage on each Codex visit so newly-signed actions appear
  // without a kill/relaunch.
  androidx.compose.runtime.LaunchedEffect(Unit) { vm.refreshLineage() }
  CodexScreen(
    state = state,
    onResetDemo = vm::resetDemoState,
    onOpenInft = onOpenInft,
  )
}

@Composable
private fun CodexScreen(
  state: CodexUiState,
  onResetDemo: () -> Unit = {},
  onOpenInft: (Int) -> Unit = {},
) {
  // Background and tab bar are app-level. Disconnect now lives in the
  // wallet-pill dropdown in CrownHeader — no big "FORGET THIS SIGIL"
  // button at the bottom anymore.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    CrownHeader(
      screenName = "Codex",
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(14.dp))

    StaggeredEntry(index = 0) {
      CodexHead()
    }

    Spacer(Modifier.height(18.dp))

    // ── Identity ─────────────────────────────────────────────────────
    StaggeredEntry(index = 1) {
      SectionHeader(title = "Identity", showLozenge = false)
    }
    StaggeredEntry(index = 2) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RowCard(
          label = "Solana pubkey · MWA",
          value = state.solanaPubkey?.shortenPubkey() ?: "not connected",
          copyable = state.solanaPubkey != null,
          copyText = state.solanaPubkey,
        )
        if (state.walletLabel != null) {
          RowCard(
            label = "Wallet account",
            value = state.walletLabel,
          )
        }
        RowCard(
          label = "Linked clans · ${state.linkedClansCount} of viii",
          value = linkedClansLine(state.linkedClanIds),
          isScript = true,
        )
      }
    }

    Spacer(Modifier.height(20.dp))

    // ── Device ───────────────────────────────────────────────────────
    StaggeredEntry(index = 3) {
      SectionHeader(title = "Device", showLozenge = false)
    }
    StaggeredEntry(index = 4) {
      DeviceChip(state)
    }

    Spacer(Modifier.height(20.dp))

    // ── Lineage ──────────────────────────────────────────────────────
    if (state.lineage.isNotEmpty()) {
      StaggeredEntry(index = 9) {
        SectionHeader(title = "Lineage", showLozenge = false)
      }
      StaggeredEntry(index = 10) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          state.lineage.take(12).forEach { entry ->
            LineageRow(entry, onClick = { onOpenInft(entry.clanId) })
          }
        }
      }
      Spacer(Modifier.height(20.dp))
    }

    // ── Share ────────────────────────────────────────────────────────
    StaggeredEntry(index = 5) {
      SectionHeader(title = "Share", showLozenge = false)
    }
    StaggeredEntry(index = 6) {
      RowCard(
        label = "APK download · for friends",
        value = state.apkShareUrl,
        copyable = true,
        copyText = state.apkShareUrl,
      )
    }

    Spacer(Modifier.height(20.dp))

    // ── About ────────────────────────────────────────────────────────
    StaggeredEntry(index = 7) {
      SectionHeader(title = "About", showLozenge = false)
    }
    StaggeredEntry(index = 8) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RowCard(label = "Build", value = state.build)
        RowCard(
          label = "Convex deployment",
          value = state.convexUrl,
          copyable = true,
          copyText = state.convexUrl,
        )
      }
    }

    Spacer(Modifier.height(28.dp))

    // ── Demo reset (bottom of Codex) ─────────────────────────────────
    StaggeredEntry(index = 11) {
      DemoResetRow(onConfirm = onResetDemo)
    }

    Spacer(Modifier.height(40.dp))
  }
}

@Composable
private fun DemoResetRow(onConfirm: () -> Unit) {
  val armedState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  val armed = armedState.value
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = "Demo".uppercase(),
      style = ClanWorldTheme.type.crownLabel,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(ClanWorldTheme.colors.iron)
        .border(
          1.dp,
          if (armed) ClanWorldTheme.colors.danger else ClanWorldTheme.colors.hairline,
          RoundedCornerShape(6.dp),
        )
        .clickable {
          if (armed) {
            onConfirm()
            armedState.value = false
          } else {
            armedState.value = true
          }
        }
        .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = if (armed) "TAP AGAIN TO CONFIRM RESET" else "RESET DEMO STATE",
          style = ClanWorldTheme.type.monoMicro,
          color = if (armed) ClanWorldTheme.colors.danger else ClanWorldTheme.colors.warmDim,
        )
        Text(
          text = "wipes hired + forged clans, drafts, and lineage. wallet stays connected.",
          style = ClanWorldTheme.type.scriptItalicSmall,
          color = ClanWorldTheme.colors.warmFaint,
        )
      }
    }
  }
}

private fun linkedClansLine(ids: List<Int>): String {
  if (ids.isEmpty()) return "no clans linked yet"
  val names = ids.map { id ->
    when (id) {
      1 -> "Storm-Edge"; 2 -> "Tideborne"; 3 -> "Sunhold"; 4 -> "Vale-Ward"
      5 -> "Twilight"; 6 -> "Ember"; 7 -> "Forge"; 8 -> "Star"
      else -> "Clan $id"
    }
  }
  return when (names.size) {
    1 -> "${names[0]} stands under your hand."
    2 -> "${names[0]} and ${names[1]} stand under your hand."
    else -> {
      val head = names.dropLast(1).joinToString(", ")
      val tail = names.last()
      "$head, and $tail stand under your hand."
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun CodexHead() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 14.dp, bottom = 18.dp)
      .drawBehind {
        // Bottom hairline
        val color = ClanWorldThemeHairline
        drawLine(
          color = color,
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1f,
        )
      },
  ) {
    Text(
      text = "CODEX",
      style = ClanWorldTheme.type.displayHero,
      color = ClanWorldTheme.colors.parchment,
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = "where the keeper records what the world should know of you",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )
  }
}

private val ClanWorldThemeHairline get() = world.clan.app.ui.theme.Hairline

@Composable
private fun RowCard(
  label: String,
  value: String,
  copyable: Boolean = false,
  copyText: String? = null,
  isScript: Boolean = false,
) {
  val clipboard = LocalClipboardManager.current
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(ClanWorldTheme.colors.iron)
      .border(1.dp, ClanWorldTheme.colors.hairline, RoundedCornerShape(6.dp))
      .padding(horizontal = 14.dp, vertical = 12.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = label.uppercase(),
        style = ClanWorldTheme.type.monoMicro,
        color = ClanWorldTheme.colors.warmFaint,
      )
      if (isScript) {
        Text(
          text = value,
          style = ClanWorldTheme.type.scriptItalic,
          color = ClanWorldTheme.colors.warm,
        )
      } else {
        Text(
          text = value,
          style = ClanWorldTheme.type.monoData,
          color = ClanWorldTheme.colors.parchment,
        )
      }
    }
    if (copyable && copyText != null) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .clip(RoundedCornerShape(4.dp))
          .border(1.dp, ClanWorldTheme.colors.hairline, RoundedCornerShape(4.dp))
          .clickable {
            clipboard.setText(AnnotatedString(copyText))
          }
          .padding(horizontal = 8.dp, vertical = 4.dp),
      ) {
        Icon(
          painter = painterResource(R.drawable.ui_copy),
          contentDescription = "Copy",
          tint = ClanWorldTheme.colors.warmDim,
          modifier = Modifier.size(10.dp),
        )
      }
    }
  }
}

@Composable
private fun LineageRow(
  entry: world.clan.app.data.LineageEntry,
  onClick: () -> Unit = {},
) {
  val parchment = ClanWorldTheme.colors.parchment
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val accent = world.clan.app.ui.theme.clanColor(entry.clanId)
  val kindGlyph = when (entry.kind) {
    "forged" -> "✦"
    "hired" -> "◈"
    "whispered" -> "≈"
    "sealed" -> "✕"
    else -> "·"
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(ClanWorldTheme.colors.iron)
      .border(1.dp, ClanWorldTheme.colors.hairline, RoundedCornerShape(6.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(accent.copy(alpha = 0.18f))
        .border(1.dp, accent.copy(alpha = 0.55f), CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = kindGlyph,
        style = ClanWorldTheme.type.body,
        color = accent,
      )
    }
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = entry.title,
        style = ClanWorldTheme.type.body,
        color = parchment,
      )
      if (!entry.subtitle.isNullOrBlank()) {
        Text(
          text = entry.subtitle,
          style = ClanWorldTheme.type.scriptItalicSmall,
          color = warmDim,
        )
      }
    }
    Text(
      text = formatRelativeTime(entry.tsEpochMs),
      style = ClanWorldTheme.type.monoNano,
      color = warmFaint,
    )
  }
}

private fun formatRelativeTime(tsMs: Long): String {
  val deltaMs = System.currentTimeMillis() - tsMs
  return when {
    deltaMs < 60_000 -> "just now"
    deltaMs < 60 * 60_000 -> "${deltaMs / 60_000}m"
    deltaMs < 24 * 60 * 60_000 -> "${deltaMs / (60 * 60_000)}h"
    else -> "${deltaMs / (24 * 60 * 60_000)}d"
  }
}

@Composable
private fun DeviceChip(state: CodexUiState) {
  val rune = ClanWorldTheme.colors.rune
  val runeDim = ClanWorldTheme.colors.runeDim
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(
        Brush.verticalGradient(
          0f to rune.copy(alpha = 0.06f),
          1f to rune.copy(alpha = 0.02f),
        ),
      )
      .border(1.dp, runeDim, RoundedCornerShape(8.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(rune.copy(alpha = 0.10f)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = painterResource(R.drawable.ui_seeker),
        contentDescription = null,
        tint = rune,
        modifier = Modifier.size(20.dp),
      )
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = state.deviceClass.displayLabel.uppercase(),
        style = ClanWorldTheme.type.crownLabel,
        color = if (state.deviceClass.seedVaultAvailable) rune else warm,
      )
      Text(
        text = state.deviceClass.description,
        style = ClanWorldTheme.type.scriptItalicSmall,
        color = warmDim,
      )
    }
  }
}

