package world.clan.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import world.clan.app.App
import world.clan.app.R
import world.clan.app.data.MemoryEntry
import world.clan.app.data.VaultMovement
import world.clan.app.ui.components.ClanWorldTabBar
import world.clan.app.ui.components.Lozenge
import world.clan.app.ui.components.MemoryRow
import world.clan.app.ui.components.ObsidianBackground
import world.clan.app.ui.components.OrnamentRule
import world.clan.app.ui.components.ParchmentCard
import world.clan.app.ui.components.RootTab
import world.clan.app.ui.components.Sigil
import world.clan.app.ui.components.WhisperAccent
import world.clan.app.ui.components.WhisperRow
import world.clan.app.ui.components.bigSigilSpec
import world.clan.app.ui.components.boldedMeta
import world.clan.app.ui.components.inlineCodeBody
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.viewmodel.DetailTab
import world.clan.app.viewmodel.InftDetailUiState
import world.clan.app.viewmodel.InftDetailViewModel
import world.clan.app.viewmodel.InftDetailViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.clanTagline

@Composable
fun InftDetailScreenRoute(
  app: App,
  clanId: Int,
  onBack: () -> Unit,
  onEnterCockpit: () -> Unit = {},
  onOpenInbox: () -> Unit = {},
  onEditStrategy: () -> Unit = {},
  onOpenTreasury: () -> Unit = {},
  isBazaar: Boolean = false,
  mwaSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender? = null,
  onHireConfirmed: (() -> Unit)? = null,
) {
  val vm: InftDetailViewModel = viewModel(factory = InftDetailViewModelFactory(app, clanId))
  val state by vm.state.collectAsState()
  InftDetailScreen(
    state = state,
    clanId = clanId,
    onBack = onBack,
    onSelectDetailTab = vm::selectTab,
    onEnterCockpit = onEnterCockpit,
    onOpenInbox = onOpenInbox,
    onEditStrategy = onEditStrategy,
    onOpenTreasury = onOpenTreasury,
    isBazaar = isBazaar,
    app = app,
    mwaSender = mwaSender,
    onHireConfirmed = onHireConfirmed ?: onBack,
  )
}

@Composable
private fun InftDetailScreen(
  state: InftDetailUiState,
  clanId: Int,
  onBack: () -> Unit,
  onSelectDetailTab: (DetailTab) -> Unit,
  onEnterCockpit: () -> Unit,
  onOpenInbox: () -> Unit = {},
  onEditStrategy: () -> Unit = {},
  onOpenTreasury: () -> Unit = {},
  isBazaar: Boolean = false,
  app: App? = null,
  mwaSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender? = null,
  onHireConfirmed: () -> Unit = {},
) {
  // Background and tab bar are app-level (ClanWorldApp.kt).
  val showHireModal = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  Box(modifier = Modifier.fillMaxSize()) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
  ) {
    DetailBackBar(clanId = clanId, onBack = onBack)

    // Hero parchment letter
    HeroLetter(
      clanId = clanId,
      state = state,
      modifier = Modifier.padding(horizontal = 22.dp),
    )

    Spacer(Modifier.height(14.dp))

    // ── Primary CTA: Hire (bazaar mode) or Enter Cockpit (linked mode) ─
    world.clan.app.ui.components.EmberCta(
      text = if (isBazaar) "Hire This Sigil" else "Enter Cockpit",
      onClick = if (isBazaar) ({ showHireModal.value = true }) else onEnterCockpit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 22.dp),
    )

    Spacer(Modifier.height(22.dp))

    DetailTabs(active = state.activeTab, onSelect = onSelectDetailTab)

    Spacer(Modifier.height(14.dp))

    Crossfade(
      targetState = state.activeTab,
      animationSpec = tween(220),
      label = "detail-tab",
    ) { tab ->
      when (tab) {
        DetailTab.Memory -> MemoryPanel(
          state.state?.memory.orEmpty(),
          onEditStrategy = if (!isBazaar) onEditStrategy else null,
        )
        DetailTab.Vault -> VaultPanel(state.vault, onOpenTreasury = onOpenTreasury)
        DetailTab.Whispers -> WhispersPanel(state.comms, onOpenInbox = onOpenInbox)
        DetailTab.Bulletin -> BulletinPanel(state.state?.bulletins?.map { it.body } ?: emptyList())
      }
    }

    Spacer(Modifier.height(40.dp))
  }

  // ── HireModal overlay (bazaar mode only) ────────────────────────────────
  if (isBazaar && showHireModal.value && app != null && mwaSender != null) {
    val listing = world.clan.app.data.bazaarListingByClan(clanId)
    if (listing != null) {
      world.clan.app.ui.components.HireModal(
        app = app,
        mwaSender = mwaSender,
        listing = listing,
        onDismiss = { showHireModal.value = false },
        onConfirmed = {
          showHireModal.value = false
          onHireConfirmed()
        },
      )
    }
  }
  } // close outer Box
}

// ─────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailBackBar(clanId: Int, onBack: () -> Unit) {
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
      contentDescription = "Back",
      tint = ClanWorldTheme.colors.gold,
      modifier = Modifier.size(14.dp),
    )
    Text(
      text = "HALL",
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.warmDim,
    )
    Text(
      text = "·",
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Text(
      text = "the writ of ${(clanDisplayName(clanId)).split(" ").lastOrNull() ?: "—"}",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )
  }
}

@Composable
private fun HeroLetter(
  clanId: Int,
  state: InftDetailUiState,
  modifier: Modifier = Modifier,
) {
  ParchmentCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 20.dp),
  ) {
    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = "TKN 0x${"%04x".format(state.state?.token?.tokenId ?: clanId)}  ·  0G",
          style = ClanWorldTheme.type.monoMicro,
          color = Ink3,
        )
        Text(
          text = clanDisplayName(clanId),
          style = ClanWorldTheme.type.heroName,
          color = Ink,
          modifier = Modifier.padding(top = 2.dp),
        )
        Text(
          text = clanTagline(clanId).lowercase(),
          style = ClanWorldTheme.type.scriptItalic,
          color = Ink2,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      HeroClanPill(clanId = clanId)
    }

    Spacer(Modifier.height(18.dp))

    // Hero sigil — the clan's runic glyph at large scale
    Box(
      modifier = Modifier
        .fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      Sigil(
        spec = bigSigilSpec(clanColor(clanId)),
        modifier = Modifier.size(140.dp),
        animated = false,
      )
    }

    Spacer(Modifier.height(14.dp))

    // Sealed-tick stamp in Uncial
    Text(
      text = sealedTickLine(state),
      style = ClanWorldTheme.type.runeLabel,
      color = Ink2,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(14.dp))

    // Divider — hairline + lozenge + hairline
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        Modifier
          .weight(1f)
          .height(1.dp)
          .background(Ink2.copy(alpha = 0.30f)),
      )
      Lozenge(color = Ink2)
      Box(
        Modifier
          .weight(1f)
          .height(1.dp)
          .background(Ink2.copy(alpha = 0.30f)),
      )
    }

    Spacer(Modifier.height(14.dp))

    // Hero meta: 3 stats
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      HeroStat(label = "Owned", value = "${state.state?.transfers?.size ?: 0} ticks")
      HeroStat(label = "Memory", value = "${state.state?.memory?.size ?: 0} keys")
      HeroStat(label = "Hires", value = "—")
    }
  }
}

@Composable
private fun HeroClanPill(clanId: Int) {
  val clanCol = clanColor(clanId)
  Row(
    modifier = Modifier
      .clip(CircleShape)
      .background(clanCol)
      .drawBehind {
        // 2dp inset bottom shadow
        drawRect(
          color = Color.Black.copy(alpha = 0.25f),
          topLeft = Offset(0f, size.height - 2.dp.toPx()),
          size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx()),
        )
      }
      .padding(horizontal = 10.dp, vertical = 4.dp),
  ) {
    Text(
      text = "HOUSE ${roman(clanId)}",
      style = ClanWorldTheme.type.monoMicro,
      color = Color.White,
    )
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeroStat(label: String, value: String) {
  Column(Modifier.weight(1f)) {
    Text(
      text = label.uppercase(),
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
    )
    Text(
      text = value,
      style = ClanWorldTheme.type.monoData,
      color = Ink,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}

private fun sealedTickLine(state: InftDetailUiState): String {
  val firstTick = state.state?.transfers?.minOfOrNull { it.transferredAt ?: Long.MAX_VALUE }
    ?.takeIf { it != Long.MAX_VALUE }
  return if (firstTick != null) "— sealed tick %04d —".format(firstTick) else "— newly forged —"
}

// ── Tabs ────────────────────────────────────────────────────────────────

@Composable
private fun DetailTabs(active: DetailTab, onSelect: (DetailTab) -> Unit) {
  val ember = ClanWorldTheme.colors.ember
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val hairline = ClanWorldTheme.colors.hairline
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp)
      .drawBehind {
        // Bottom hairline (inactive tabs underline)
        drawLine(hairline, Offset(0f, size.height), Offset(size.width, size.height), 1f)
      },
  ) {
    DetailTab.values().forEach { tab ->
      Box(
        modifier = Modifier
          .weight(1f)
          .clickable { onSelect(tab) }
          .padding(vertical = 10.dp)
          .drawBehind {
            if (tab == active) {
              drawLine(
                color = ember,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.5.dp.toPx(),
              )
            }
          },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = tab.label.uppercase(),
          style = ClanWorldTheme.type.ctaLabel.copy(fontSize = 10.sp),
          color = if (tab == active) ember else warmFaint,
        )
      }
    }
  }
}

private val DetailTab.label get() = when (this) {
  DetailTab.Memory -> "Memory"
  DetailTab.Vault -> "Vault"
  DetailTab.Whispers -> "Whispers"
  DetailTab.Bulletin -> "Bulletin"
}

// ── Panels ──────────────────────────────────────────────────────────────

@Composable
private fun MemoryPanel(
  memory: List<MemoryEntry>,
  onEditStrategy: (() -> Unit)? = null,
) {
  Column {
    PanelSurface(modifier = Modifier.padding(horizontal = 22.dp)) {
      if (memory.isEmpty()) {
        Box(
          Modifier
            .fillMaxWidth()
            .padding(20.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            "no memory written yet",
            style = ClanWorldTheme.type.scriptItalic,
            color = ClanWorldTheme.colors.warmFaint,
          )
        }
      } else {
        memory.take(10).forEach { entry ->
          MemoryRow(
            key = entry.key,
            body = inlineCodeBody(entry.value),
            stamp = "written tick ${entry.updatedAt ?: 0L} · ${entry.source ?: "local"}",
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
    if (onEditStrategy != null) {
      Text(
        text = "EDIT STRATEGY →",
        style = ClanWorldTheme.type.monoMicro,
        color = ClanWorldTheme.colors.gold,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onEditStrategy() }
          .padding(horizontal = 22.dp, vertical = 14.dp),
      )
    }
  }
}

@Composable
private fun VaultPanel(
  vault: List<VaultMovement>,
  onOpenTreasury: () -> Unit = {},
) {
  Column {
  PanelSurface(modifier = Modifier.padding(horizontal = 22.dp)) {
    if (vault.isEmpty()) {
      ComingNextSlice("the vault keeps its silence")
    } else {
      vault.take(10).forEach { mv ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(ClanWorldTheme.colors.iron)
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "T %04d".format(mv.tick),
            style = ClanWorldTheme.type.monoNano,
            color = ClanWorldTheme.colors.warmFaint,
            modifier = Modifier.padding(end = 4.dp),
          )
          Text(
            text = mv.resource.replaceFirstChar { it.uppercase() },
            style = ClanWorldTheme.type.body,
            color = ClanWorldTheme.colors.parchment,
            modifier = Modifier.weight(1f),
          )
          Text(
            text = "${if (mv.type == "spend") "−" else "+"}%.2f".format(mv.amount),
            style = ClanWorldTheme.type.monoData,
            color = if (mv.type == "spend") ClanWorldTheme.colors.danger else ClanWorldTheme.colors.gold,
          )
        }
      }
    }
  }
  Text(
    text = "FULL TREASURY →",
    style = ClanWorldTheme.type.monoMicro,
    color = ClanWorldTheme.colors.gold,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onOpenTreasury() }
      .padding(horizontal = 22.dp, vertical = 14.dp),
  )
  } // close outer Column
}

@Composable
private fun WhispersPanel(
  comms: List<world.clan.app.data.CombinedComm>,
  onOpenInbox: () -> Unit = {},
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (comms.isEmpty()) {
      ComingNextSlicePlain("no whispers yet for this elder")
    } else {
      comms.take(8).forEach { c ->
        val accent = when (c.kind) {
          "whisper" -> WhisperAccent.Rune
          "orch" -> WhisperAccent.Gold
          "human" -> WhisperAccent.Ember
          else -> WhisperAccent.Default
        }
        val from = c.fromClan?.let { clanDisplayName(it) }
          ?: c.speaker
          ?: when (c.kind) {
            "orch" -> "Orchestrator"; "human" -> "Owner"; else -> "—"
          }
        val tickStr = c.tick?.let { "%04d".format(it) } ?: "—"
        WhisperRow(
          meta = boldedMeta("*$from* · $tickStr".uppercase()),
          body = c.body,
          accent = accent,
        )
      }
      Spacer(Modifier.height(4.dp))
      Text(
        text = "OPEN FULL INBOX →",
        style = ClanWorldTheme.type.monoMicro,
        color = ClanWorldTheme.colors.gold,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenInbox() }
          .padding(vertical = 10.dp),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun BulletinPanel(bulletins: List<String>) {
  PanelSurface(modifier = Modifier.padding(horizontal = 22.dp)) {
    if (bulletins.isEmpty()) {
      ComingNextSlice("no bulletins posted")
    } else {
      bulletins.forEachIndexed { i, body ->
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(ClanWorldTheme.colors.iron)
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "SLOT %02d".format(i + 1),
            style = ClanWorldTheme.type.monoNano,
            color = ClanWorldTheme.colors.gold,
          )
          Text(
            text = body,
            style = ClanWorldTheme.type.scriptItalic,
            color = ClanWorldTheme.colors.warm,
          )
        }
      }
    }
  }
}

@Composable
private fun PanelSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(ClanWorldTheme.colors.hairline)
      .border(1.dp, ClanWorldTheme.colors.hairline, RoundedCornerShape(6.dp)),
    verticalArrangement = Arrangement.spacedBy(1.dp),
  ) {
    content()
  }
}

@Composable
private fun ComingNextSlice(message: String) {
  Box(
    Modifier
      .fillMaxWidth()
      .background(ClanWorldTheme.colors.iron)
      .padding(20.dp),
    contentAlignment = Alignment.Center,
  ) {
    ComingNextSlicePlain(message)
  }
}

@Composable
private fun ComingNextSlicePlain(message: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = message,
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Text(
      text = "(coming next slice)",
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
    )
  }
}

private fun roman(n: Int): String = when (n) {
  1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"
  5 -> "V"; 6 -> "VI"; 7 -> "VII"; 8 -> "VIII"
  else -> n.toString()
}

private typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope
