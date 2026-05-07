package world.clan.app.ui.screens

import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.clan.app.App
import world.clan.app.R
import world.clan.app.ui.components.EmberCta
import world.clan.app.ui.components.ParchmentCard
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.ui.theme.clanGlyphRes
import world.clan.app.viewmodel.HearthViewModel
import world.clan.app.viewmodel.SendPhase
import world.clan.app.viewmodel.SteeringConsoleUiState
import world.clan.app.viewmodel.SteeringConsoleViewModel
import world.clan.app.viewmodel.SteeringConsoleViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.wallet.MwaResult

@Composable
fun SteeringConsoleScreenRoute(
  app: App,
  mwaSender: ActivityResultSender,
  initialClanId: Int,
  onBack: () -> Unit,
  onSent: () -> Unit,
) {
  val vm: SteeringConsoleViewModel =
    viewModel(factory = SteeringConsoleViewModelFactory(app, initialClanId))
  val state by vm.state.collectAsState()
  val scope = rememberCoroutineScope()

  SteeringConsole(
    state = state,
    onBack = onBack,
    onDraftChange = vm::setDraft,
    onTargetChange = vm::setTarget,
    onSend = {
      scope.launch {
        vm.setPhase(SendPhase.Signing)
        val token = app.sessionStore.read()?.mwaAuthToken
        if (token == null) {
          vm.setPhase(SendPhase.Queued)
          return@launch
        }
        val msg = "ClanWorld Steer — clan ${state.targetClanId}: ${state.draft}".toByteArray()
        val result = app.mwaClient.signMessage(mwaSender, token, msg)
        when (result) {
          is MwaResult.Ok -> vm.setPhase(SendPhase.Queued)
          is MwaResult.UserDeclined -> vm.setPhase(SendPhase.Idle)
          is MwaResult.WalletNotFound ->
            vm.setPhase(SendPhase.Failed, "no wallet found on device.")
          is MwaResult.Error ->
            vm.setPhase(SendPhase.Failed, result.cause.message ?: "the wallet refused the seal.")
        }
      }
    },
  )

  // Auto-pop after queued state lands.
  if (state.phase == SendPhase.Queued) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
      delay(1300L)
      onSent()
    }
  }
}

@Composable
private fun SteeringConsole(
  state: SteeringConsoleUiState,
  onBack: () -> Unit,
  onDraftChange: (String) -> Unit,
  onTargetChange: (Int) -> Unit,
  onSend: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
  ) {
    BackBar(text = "back to whispers", onBack = onBack)

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
      ConsoleHead()
      Spacer(Modifier.height(14.dp))
      TargetClanRow(
        active = state.targetClanId,
        onSelect = onTargetChange,
      )
      Spacer(Modifier.height(18.dp))
      DraftCard(
        draft = state.draft,
        onChange = onDraftChange,
        enabled = state.phase == SendPhase.Idle || state.phase == SendPhase.Failed,
      )
      Spacer(Modifier.height(14.dp))
      ConsoleStatus(state)
      Spacer(Modifier.height(14.dp))
      EmberCta(
        text = when (state.phase) {
          SendPhase.Idle -> "Sign and Send"
          SendPhase.Signing -> "Awaiting the seal…"
          SendPhase.Queued -> "Queued ✓"
          SendPhase.Failed -> "Try Again"
        },
        onClick = onSend,
        enabled = state.draft.isNotBlank() &&
          (state.phase == SendPhase.Idle || state.phase == SendPhase.Failed),
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(40.dp))
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
private fun ConsoleHead() {
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
    Text(
      text = "STEERING CONSOLE",
      style = ClanWorldTheme.type.displayHero,
      color = parchment,
    )
    Text(
      text = "speak to your elder before the next tick",
      style = ClanWorldTheme.type.scriptItalic,
      color = warmDim,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}

@Composable
private fun TargetClanRow(active: Int, onSelect: (Int) -> Unit) {
  Column {
    Text(
      text = "TO ELDER",
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(HearthViewModel.LINKED_CLAN_IDS) { clanId ->
        ClanPill(
          clanId = clanId,
          selected = clanId == active,
          onClick = { onSelect(clanId) },
        )
      }
    }
  }
}

@Composable
private fun ClanPill(clanId: Int, selected: Boolean, onClick: () -> Unit) {
  val accent = clanColor(clanId)
  val bg = if (selected) accent.copy(alpha = 0.18f) else Color.Transparent
  val border = if (selected) accent else ClanWorldTheme.colors.hairline
  val tint = if (selected) ClanWorldTheme.colors.parchment else ClanWorldTheme.colors.warmDim
  Row(
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
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(RoundedCornerShape(50))
        .background(accent),
    )
    Text(
      text = clanDisplayName(clanId).uppercase(),
      style = ClanWorldTheme.type.monoMicro,
      color = tint,
    )
  }
}

@Composable
private fun DraftCard(draft: String, onChange: (String) -> Unit, enabled: Boolean) {
  ParchmentCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "STEER",
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
    )
    Spacer(Modifier.height(6.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(120.dp),
    ) {
      if (draft.isEmpty()) {
        Text(
          text = "the river runs cold; press for the strait, hold the western shore until the next tick…",
          style = ClanWorldTheme.type.scriptItalic.copy(color = Ink3),
        )
      }
      BasicTextField(
        value = draft,
        onValueChange = if (enabled) onChange else { _ -> },
        textStyle = LocalTextStyle.current.copy(
          fontFamily = ClanWorldTheme.type.scriptItalic.fontFamily,
          fontStyle = ClanWorldTheme.type.scriptItalic.fontStyle,
          fontSize = ClanWorldTheme.type.scriptItalic.fontSize,
          color = Ink,
        ),
        cursorBrush = SolidColor(Ink2),
        modifier = Modifier.fillMaxSize(),
      )
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = "${draft.length}/280",
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
      textAlign = TextAlign.End,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ConsoleStatus(state: SteeringConsoleUiState) {
  when (state.phase) {
    SendPhase.Idle -> {
      Text(
        text = "your seal is required before the message goes to the elder.",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warmDim,
      )
    }
    SendPhase.Signing -> {
      Text(
        text = "the wallet is preparing the seal…",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.gold,
      )
    }
    SendPhase.Queued -> {
      Text(
        text = "queued for next tick — the elder will hear before the heartbeat.",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.success,
      )
    }
    SendPhase.Failed -> {
      Text(
        text = state.errorMessage ?: "the seal could not be set.",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.danger,
      )
    }
  }
}
