package world.clan.app.ui.screens

import androidx.activity.ComponentActivity
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
import world.clan.app.viewmodel.Posture
import world.clan.app.viewmodel.SendPhase
import world.clan.app.viewmodel.StrategyEditorUiState
import world.clan.app.viewmodel.StrategyEditorViewModel
import world.clan.app.viewmodel.StrategyEditorViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.wallet.MwaResult

@Composable
fun StrategyEditorScreenRoute(
  app: App,
  hostActivity: ComponentActivity,
  clanId: Int,
  onBack: () -> Unit,
  onSaved: () -> Unit,
) {
  val vm: StrategyEditorViewModel =
    viewModel(factory = StrategyEditorViewModelFactory(app, clanId))
  val state by vm.state.collectAsState()
  val scope = rememberCoroutineScope()

  StrategyEditor(
    state = state,
    onBack = onBack,
    onSetPosture = vm::setPosture,
    onSetDoctrine = vm::setDoctrine,
    onSetPinnedKey = vm::setPinnedKey,
    onSave = {
      scope.launch {
        vm.setSavePhase(SendPhase.Signing)
        val token = app.sessionStore.read()?.mwaAuthToken
        if (token == null) {
          vm.setSavePhase(SendPhase.Queued)
          return@launch
        }
        val msg = ("ClanWorld Strategy — clan ${state.clanId} | ${state.posture.name} | " +
          "${state.pinnedKey} = ${state.doctrine}").toByteArray()
        when (val r = app.mwaClient.signMessage(hostActivity, token, msg)) {
          is MwaResult.Ok -> vm.setSavePhase(SendPhase.Queued)
          is MwaResult.UserDeclined -> vm.setSavePhase(SendPhase.Idle)
          is MwaResult.WalletNotFound ->
            vm.setSavePhase(SendPhase.Failed, "no wallet found on device.")
          is MwaResult.Error ->
            vm.setSavePhase(SendPhase.Failed, r.cause.message ?: "the wallet refused the seal.")
        }
      }
    },
  )

  if (state.savePhase == SendPhase.Queued) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
      delay(1300L)
      onSaved()
    }
  }
}

@Composable
private fun StrategyEditor(
  state: StrategyEditorUiState,
  onBack: () -> Unit,
  onSetPosture: (Posture) -> Unit,
  onSetDoctrine: (String) -> Unit,
  onSetPinnedKey: (String) -> Unit,
  onSave: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
  ) {
    BackBar(text = "back to ${clanDisplayName(state.clanId).lowercase()}", onBack = onBack)

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
      EditorHead(state.clanId)
      Spacer(Modifier.height(14.dp))

      if (state.isLoading) {
        Text(
          text = "the elder's last counsel is being read…",
          style = ClanWorldTheme.type.scriptItalic,
          color = ClanWorldTheme.colors.warmFaint,
          modifier = Modifier.padding(top = 24.dp),
        )
      } else {
        // ── Posture ───────────────────────────────────────────────────────
        Text(
          text = "POSTURE",
          style = ClanWorldTheme.type.monoNano,
          color = ClanWorldTheme.colors.warmFaint,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Posture.values().forEach { p ->
            PosturePill(
              posture = p,
              selected = p == state.posture,
              onClick = { onSetPosture(p) },
            )
          }
        }
        Spacer(Modifier.height(18.dp))

        // ── Pinned memory key ─────────────────────────────────────────────
        ParchmentCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "PINNED KEY",
            style = ClanWorldTheme.type.monoNano,
            color = Ink3,
          )
          Spacer(Modifier.height(6.dp))
          BasicTextField(
            value = state.pinnedKey,
            onValueChange = onSetPinnedKey,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
              fontFamily = ClanWorldTheme.type.monoData.fontFamily,
              fontSize = ClanWorldTheme.type.monoData.fontSize,
              color = Ink,
            ),
            cursorBrush = SolidColor(Ink2),
            modifier = Modifier.fillMaxWidth(),
          )
        }

        Spacer(Modifier.height(14.dp))

        // ── Doctrine ──────────────────────────────────────────────────────
        ParchmentCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "DOCTRINE",
            style = ClanWorldTheme.type.monoNano,
            color = Ink3,
          )
          Spacer(Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
          ) {
            if (state.doctrine.isEmpty()) {
              Text(
                text = "what should the elder hold to, even when the season turns?",
                style = ClanWorldTheme.type.scriptItalic.copy(color = Ink3),
              )
            }
            BasicTextField(
              value = state.doctrine,
              onValueChange = onSetDoctrine,
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
            text = "${state.doctrine.length}/280",
            style = ClanWorldTheme.type.monoNano,
            color = Ink3,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        Spacer(Modifier.height(14.dp))

        // ── Save status ───────────────────────────────────────────────────
        EditorStatus(state)
        Spacer(Modifier.height(14.dp))

        EmberCta(
          text = when (state.savePhase) {
            SendPhase.Idle -> "Sign and Seal"
            SendPhase.Signing -> "Awaiting the seal…"
            SendPhase.Queued -> "Sealed ✓"
            SendPhase.Failed -> "Try Again"
          },
          onClick = onSave,
          enabled = state.doctrine.isNotBlank() &&
            state.pinnedKey.isNotBlank() &&
            (state.savePhase == SendPhase.Idle || state.savePhase == SendPhase.Failed),
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
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
private fun EditorHead(clanId: Int) {
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
      text = "STRATEGY",
      style = ClanWorldTheme.type.displayHero,
      color = parchment,
    )
    Text(
      text = "the doctrine kept by ${clanDisplayName(clanId).lowercase()}",
      style = ClanWorldTheme.type.scriptItalic,
      color = warmDim,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}

@Composable
private fun PosturePill(posture: Posture, selected: Boolean, onClick: () -> Unit) {
  val accent = when (posture) {
    Posture.Defensive -> ClanWorldTheme.colors.rune
    Posture.Balanced -> ClanWorldTheme.colors.gold
    Posture.Aggressive -> ClanWorldTheme.colors.ember
  }
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
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(RoundedCornerShape(50))
        .background(accent),
    )
    Text(
      text = posture.name.uppercase(),
      style = ClanWorldTheme.type.monoMicro,
      color = tint,
    )
  }
}

@Composable
private fun EditorStatus(state: StrategyEditorUiState) {
  when (state.savePhase) {
    SendPhase.Idle -> {
      Text(
        text = "your seal will write the doctrine to the elder's memory.",
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
        text = "sealed — the elder will hold this counsel through the next tick.",
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
