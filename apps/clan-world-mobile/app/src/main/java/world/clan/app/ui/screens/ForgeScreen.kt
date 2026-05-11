package world.clan.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.clan.app.App
import world.clan.app.R
import world.clan.app.ui.components.EmberCta
import world.clan.app.ui.components.ParchmentCard
import world.clan.app.ui.components.Sigil
import world.clan.app.ui.components.WaxSeal
import world.clan.app.ui.components.bigSigilSpec
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.ui.theme.clanGlyphRes
import world.clan.app.viewmodel.ClanWorldViewModelFactory
import world.clan.app.viewmodel.ForgeStep
import world.clan.app.viewmodel.ForgeUiState
import world.clan.app.viewmodel.ForgeViewModel
import world.clan.app.viewmodel.Harness
import world.clan.app.viewmodel.SendPhase
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.clanTagline
import world.clan.app.wallet.FakeWalletPolicy
import world.clan.app.wallet.MwaResult

@Composable
fun ForgeScreenRoute(
  app: App,
  factory: ClanWorldViewModelFactory,
  mwaSender: ActivityResultSender,
  onBack: () -> Unit,
  onForged: (clanId: Int, name: String) -> Unit,
) {
  val vm: ForgeViewModel = viewModel(factory = factory)
  val state by vm.state.collectAsState()
  val scope = rememberCoroutineScope()

  // System back inside the wizard walks back through steps rather than
  // popping the whole route. Only on step 1 (PickClan) does back actually
  // exit the wizard back to Hall.
  androidx.activity.compose.BackHandler(enabled = state.step != ForgeStep.PickClan) {
    vm.back()
  }

  ForgeScreen(
    state = state,
    onBack = onBack,
    onSelectClan = vm::setClanId,
    onSetName = vm::setSigilName,
    onSetHarness = vm::setHarness,
    onNext = vm::next,
    onPrev = vm::back,
    onForge = {
      scope.launch {
        vm.setMintPhase(SendPhase.Signing)
        val token = app.sessionStore.read()?.mwaAuthToken
        if (token == null) {
          vm.setMintPhase(SendPhase.Queued)
          return@launch
        }
        val msg = ("ClanWorld Forge — clan ${state.clanId} | ${state.sigilName} | " +
          "${state.harness.name}").toByteArray()
        when (val r = app.mwaClient.signMessage(mwaSender, token, msg)) {
          is MwaResult.Ok -> vm.setMintPhase(SendPhase.Queued)
          is MwaResult.UserDeclined -> vm.setMintPhase(SendPhase.Idle)
          is MwaResult.WalletNotFound ->
            vm.setMintPhase(SendPhase.Failed, "no wallet found on device.")
          is MwaResult.WalletNotAllowed ->
            vm.setMintPhase(SendPhase.Failed, FakeWalletPolicy.BLOCKED_MESSAGE)
          is MwaResult.WrongNetwork ->
            vm.setMintPhase(SendPhase.Failed, "switch your wallet to Solana Devnet, then try again.")
          is MwaResult.Error ->
            vm.setMintPhase(SendPhase.Failed, r.cause.message ?: "the wallet refused the seal.")
        }
      }
    },
  )

  if (state.mintPhase == SendPhase.Queued) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
      delay(1500L)
      val cid = state.clanId ?: 1
      onForged(cid, state.sigilName)
    }
  }
}

@Composable
private fun ForgeScreen(
  state: ForgeUiState,
  onBack: () -> Unit,
  onSelectClan: (Int) -> Unit,
  onSetName: (String) -> Unit,
  onSetHarness: (Harness) -> Unit,
  onNext: () -> Unit,
  onPrev: () -> Unit,
  onForge: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    BackBar(text = "back to hall", onBack = onBack)

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
      ForgeHead(state.clanId, showClanContext = state.step != ForgeStep.PickClan)
      Spacer(Modifier.height(10.dp))
      ProgressDots(step = state.step)
      Spacer(Modifier.height(18.dp))
    }

    AnimatedContent(
      targetState = state.step,
      transitionSpec = {
        val direction = if (targetState.ordinal > initialState.ordinal) SlideDirection.Start else SlideDirection.End
        (slideIntoContainer(direction, animationSpec = tween(280)) + fadeIn(tween(220))) togetherWith
          (slideOutOfContainer(direction, animationSpec = tween(280)) + fadeOut(tween(180)))
      },
      label = "forge-step",
      modifier = Modifier.weight(1f),
    ) { step ->
      when (step) {
        ForgeStep.PickClan -> StepPickClan(
          state = state,
          onSelect = onSelectClan,
        )
        ForgeStep.NameSigil -> StepNameSigil(
          state = state,
          onChange = onSetName,
        )
        ForgeStep.PickHarness -> StepPickHarness(
          state = state,
          onSelect = onSetHarness,
        )
        ForgeStep.Confirm -> StepConfirm(
          state = state,
          onForge = onForge,
        )
      }
    }

    NavRow(state = state, onNext = onNext, onPrev = onPrev)
    Spacer(Modifier.height(20.dp))
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Header + progress dots
// ─────────────────────────────────────────────────────────────────────────

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
private fun ForgeHead(clanId: Int?, showClanContext: Boolean) {
  val parchment = ClanWorldTheme.colors.parchment
  val warmDim = ClanWorldTheme.colors.warmDim
  val hairline = ClanWorldTheme.colors.hairline

  Column(
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
  ) {
    Text(text = "FORGE", style = ClanWorldTheme.type.displayHero, color = parchment)
    if (showClanContext && clanId != null) {
      val accent = clanColor(clanId)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp),
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
            .background(accent),
        )
        Text(
          text = "FORGING IN ${clanDisplayName(clanId).uppercase()}",
          style = ClanWorldTheme.type.monoMicro,
          color = accent,
        )
      }
    } else {
      Text(
        text = "the unmade seal awaits its line",
        style = ClanWorldTheme.type.scriptItalic,
        color = warmDim,
        modifier = Modifier.padding(top = 2.dp),
      )
    }
  }
}

@Composable
private fun ProgressDots(step: ForgeStep) {
  val ember = ClanWorldTheme.colors.ember
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val total = ForgeStep.values().size
  val activeIdx = step.ordinal
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    for (i in 0 until total) {
      val color = if (i <= activeIdx) ember else warmFaint
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(RoundedCornerShape(50))
          .background(color),
      )
    }
    Spacer(Modifier.fillMaxWidth().weight(1f, fill = false))
    Text(
      text = "STEP ${activeIdx + 1} OF $total",
      style = ClanWorldTheme.type.monoNano,
      color = warmFaint,
      modifier = Modifier.padding(start = 12.dp),
    )
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Step 1 — Pick clan
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun StepPickClan(state: ForgeUiState, onSelect: (Int) -> Unit) {
  // If every clan is already owned, the wizard has nothing to offer —
  // render an EmptyState pointing to Codex's reset rather than 8
  // greyed cards.
  if (state.ownedClanIds.size >= 8) {
    world.clan.app.ui.components.EmptyState(
      title = "every clan is yours",
      body = "all eight sigils stand under your hand. to forge again, reset demo state in Codex.",
    )
    return
  }
  Column(modifier = Modifier.fillMaxSize()) {
    Text(
      text = "CHOOSE YOUR CLAN",
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
      modifier = Modifier.padding(horizontal = 22.dp),
    )
    if (state.ownedClanIds.isNotEmpty()) {
      Spacer(Modifier.height(4.dp))
      Text(
        text = "clans already under your hand can't be re-forged.",
        style = ClanWorldTheme.type.scriptItalicSmall,
        color = ClanWorldTheme.colors.warmFaint,
        modifier = Modifier.padding(horizontal = 22.dp),
      )
    }
    Spacer(Modifier.height(10.dp))
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
    ) {
      items((1..8).toList()) { clanId ->
        val owned = clanId in state.ownedClanIds
        ClanCard(
          clanId = clanId,
          selected = state.clanId == clanId,
          owned = owned,
          onClick = { if (!owned) onSelect(clanId) },
        )
      }
    }
  }
}

@Composable
private fun ClanCard(
  clanId: Int,
  selected: Boolean,
  owned: Boolean,
  onClick: () -> Unit,
) {
  val accent = clanColor(clanId)
  val borderColor = when {
    owned -> ClanWorldTheme.colors.hairline
    selected -> accent
    else -> ClanWorldTheme.colors.hairline
  }
  val borderStroke = if (selected && !owned) 2.dp else 1.dp
  val cardAlpha = if (owned) 0.45f else 1f
  val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .alpha(cardAlpha)
      .clickable(enabled = !owned) {
        if (!selected) {
          haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
        onClick()
      }
      .drawBehind {
        drawRoundRect(
          color = borderColor,
          cornerRadius = CornerRadius(6.dp.toPx()),
          style = Stroke(width = borderStroke.toPx()),
        )
      },
  ) {
    ParchmentCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        WaxSeal(
          glyph = painterResource(clanGlyphRes(clanId)),
          clanColor = accent,
          size = 44.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = clanDisplayName(clanId),
            style = ClanWorldTheme.type.letterName,
            color = Ink,
          )
          Text(
            text = if (owned) "already under your hand" else clanTagline(clanId),
            style = ClanWorldTheme.type.scriptItalic,
            color = Ink2,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Step 2 — Name the sigil
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun StepNameSigil(state: ForgeUiState, onChange: (String) -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 22.dp),
  ) {
    Text(
      text = "NAME THE SIGIL",
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = "what shall the elders call this iNFT?",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )
    Spacer(Modifier.height(20.dp))

    // Picked-clan sigil preview — gives the user something to look at
    // while they decide on a name. Animates so it doesn't feel static.
    val pickedClan = state.clanId
    if (pickedClan != null) {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Sigil(
          spec = bigSigilSpec(clanColor(pickedClan)),
          modifier = Modifier.size(110.dp),
        )
      }
      Spacer(Modifier.height(20.dp))
    }

    ParchmentCard(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "NAME",
        style = ClanWorldTheme.type.monoNano,
        color = Ink3,
      )
      Spacer(Modifier.height(6.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
      ) {
        if (state.sigilName.isEmpty()) {
          Text(
            text = "the unnamed seal…",
            style = ClanWorldTheme.type.scriptItalic.copy(color = Ink3),
          )
        }
        BasicTextField(
          value = state.sigilName,
          onValueChange = onChange,
          singleLine = true,
          textStyle = LocalTextStyle.current.copy(
            fontFamily = ClanWorldTheme.type.letterName.fontFamily,
            fontSize = ClanWorldTheme.type.letterName.fontSize,
            color = Ink,
          ),
          cursorBrush = SolidColor(Ink2),
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(8.dp))
      val nameCounterColor = when {
        state.sigilName.length >= 32 -> ClanWorldTheme.colors.danger
        state.sigilName.length >= 27 -> ClanWorldTheme.colors.warn  // 85% of 32
        else -> Ink3
      }
      Text(
        text = "${state.sigilName.length}/32",
        style = ClanWorldTheme.type.monoNano,
        color = nameCounterColor,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Step 3 — Pick harness
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun StepPickHarness(state: ForgeUiState, onSelect: (Harness) -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 22.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = "PICK YOUR HARNESS",
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Spacer(Modifier.height(2.dp))
    Text(
      text = "the bend the elder will favor",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )
    Spacer(Modifier.height(12.dp))

    // Picked-clan sigil preview (smaller than step 2; the harness cards
    // are the focus here). Mirrors step 2's "see what you're naming"
    // pattern.
    val pickedClan = state.clanId
    if (pickedClan != null) {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Sigil(
          spec = bigSigilSpec(clanColor(pickedClan)),
          modifier = Modifier.size(80.dp),
        )
      }
      Spacer(Modifier.height(4.dp))
    }

    Harness.values().forEach { h ->
      HarnessCard(
        harness = h,
        selected = state.harness == h,
        onClick = { onSelect(h) },
      )
    }
  }
}

@Composable
private fun HarnessCard(harness: Harness, selected: Boolean, onClick: () -> Unit) {
  val accent = when (harness) {
    Harness.Iron -> ClanWorldTheme.colors.rune
    Harness.Tide -> ClanWorldTheme.colors.gold
    Harness.Ember -> ClanWorldTheme.colors.ember
  }
  val border = if (selected) accent else ClanWorldTheme.colors.hairline
  val stroke = if (selected) 2.dp else 1.dp
  val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        if (!selected) {
          haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
        onClick()
      }
      .drawBehind {
        drawRoundRect(
          color = border,
          cornerRadius = CornerRadius(6.dp.toPx()),
          style = Stroke(width = stroke.toPx()),
        )
      },
  ) {
    ParchmentCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(50))
            .background(accent),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = harness.display.uppercase(),
            style = ClanWorldTheme.type.letterName,
            color = Ink,
          )
          Text(
            text = harness.tagline,
            style = ClanWorldTheme.type.scriptItalic,
            color = Ink2,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Step 4 — Confirm + sign
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun StepConfirm(state: ForgeUiState, onForge: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 22.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = "CONFIRM THE FORGE",
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Text(
      text = "your seal is required before the iNFT goes to your wallet.",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )

    Spacer(Modifier.height(2.dp))

    val clanId = state.clanId
    if (clanId == null) {
      Text(
        text = "no clan chosen — go back to step 1.",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.danger,
      )
      return@Column
    }

    ParchmentCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        WaxSeal(
          glyph = painterResource(clanGlyphRes(clanId)),
          clanColor = clanColor(clanId),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = state.sigilName.ifBlank { "the unnamed seal" },
            style = ClanWorldTheme.type.letterName,
            color = Ink,
          )
          Text(
            text = clanDisplayName(clanId),
            style = ClanWorldTheme.type.scriptItalic,
            color = Ink2,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
      Spacer(Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        ConfirmStat(label = "Clan", value = clanDisplayName(clanId))
        ConfirmStat(label = "Harness", value = state.harness.display)
        ConfirmStat(label = "Cost", value = "free • demo")
      }
    }

    Spacer(Modifier.height(2.dp))

    when (state.mintPhase) {
      SendPhase.Idle, SendPhase.Failed -> {
        EmberCta(
          text = if (state.mintPhase == SendPhase.Failed) "Try Again" else "Sign and Forge",
          onClick = onForge,
          enabled = state.sigilName.isNotBlank(),
          modifier = Modifier.fillMaxWidth(),
        )
        if (state.mintPhase == SendPhase.Failed) {
          Text(
            text = state.errorMessage ?: "the seal could not be set.",
            style = ClanWorldTheme.type.scriptItalic,
            color = ClanWorldTheme.colors.danger,
          )
        }
      }
      SendPhase.Signing -> {
        Text(
          text = "the wallet is preparing the seal…",
          style = ClanWorldTheme.type.scriptItalic,
          color = ClanWorldTheme.colors.gold,
        )
      }
      SendPhase.Queued -> {
        world.clan.app.ui.components.SealedNotice(label = "Forged ✓")
      }
    }
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ConfirmStat(label: String, value: String) {
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

// ─────────────────────────────────────────────────────────────────────────
// Bottom nav row (Back / Next)
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun NavRow(state: ForgeUiState, onNext: () -> Unit, onPrev: () -> Unit) {
  val isFirst = state.step == ForgeStep.PickClan
  val isLast = state.step == ForgeStep.Confirm
  val canAdvance = when (state.step) {
    ForgeStep.PickClan -> state.clanId != null
    ForgeStep.NameSigil -> state.sigilName.isNotBlank()
    ForgeStep.PickHarness -> true
    ForgeStep.Confirm -> false
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (!isFirst) {
      Text(
        text = "← BACK",
        style = ClanWorldTheme.type.monoMicro,
        color = ClanWorldTheme.colors.warmDim,
        modifier = Modifier
          .clickable { onPrev() }
          .padding(horizontal = 8.dp, vertical = 10.dp),
      )
    } else {
      Spacer(Modifier.size(1.dp))
    }
    if (!isLast) {
      val tint = if (canAdvance) ClanWorldTheme.colors.gold else ClanWorldTheme.colors.warmFaint
      Text(
        text = "NEXT →",
        style = ClanWorldTheme.type.monoMicro,
        color = tint,
        modifier = Modifier
          .clickable(enabled = canAdvance) { onNext() }
          .padding(horizontal = 8.dp, vertical = 10.dp),
      )
    } else {
      Spacer(Modifier.size(1.dp))
    }
  }
}
