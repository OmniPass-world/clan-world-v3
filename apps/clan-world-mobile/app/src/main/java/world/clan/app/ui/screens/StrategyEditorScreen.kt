package world.clan.app.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import world.clan.app.App
import world.clan.app.R
import world.clan.app.data.gold.GoldSolanaClient
import world.clan.app.ui.components.EmberCta
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.viewmodel.SendPhase
import world.clan.app.viewmodel.StrategyEditorUiState
import world.clan.app.viewmodel.StrategyEditorViewModel
import world.clan.app.viewmodel.StrategyEditorViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.wallet.MwaResult
import java.security.MessageDigest

private const val STRATEGY_LIMIT = 800
private const val NOTES_LIMIT = 400

/**
 * Slice 4d redesign: dual STRATEGY + NOTES textarea pattern that mirrors
 * web PR #51's `EssenceSection.tsx`. Each FieldShell glows ember when dirty.
 * "Sign to Commit" CTA becomes the primary action when there are unsaved
 * changes; its post-sign success transitions to "Sealed ✓" then auto-pops.
 */
@Composable
fun StrategyEditorScreenRoute(
  app: App,
  mwaSender: ActivityResultSender,
  clanId: Int,
  onBack: () -> Unit,
  onSaved: () -> Unit,
) {
  val vm: StrategyEditorViewModel = viewModel(
    factory = StrategyEditorViewModelFactory(app, clanId),
  )
  val state by vm.state.collectAsState()
  val scope = rememberCoroutineScope()
  val view = LocalView.current

  // Auto-pop ~1.3s after Queued — fire a confirm haptic on the transition.
  LaunchedEffect(state.savePhase) {
    if (state.savePhase == SendPhase.Queued) {
      view.performHapticFeedback(
        if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.VIRTUAL_KEY,
      )
      delay(1_300L)
      onSaved()
    }
  }

  StrategyEditor(
    state = state,
    onBack = onBack,
    onStrategyChange = vm::setDraftStrategy,
    onNotesChange = vm::setDraftNotes,
    onCommit = {
      scope.launch {
        vm.setSavePhase(SendPhase.Signing)
        val session = app.sessionStore.read()
        val token = session?.mwaAuthToken
        val owner = session?.solanaPubkeyBase58
        if (token == null || owner == null) {
          vm.setSavePhase(SendPhase.Failed, "not signed in.")
          return@launch
        }
        val payloadHash = "${state.draftStrategy}\n---notes---\n${state.draftNotes}".sha256Hex()
        val memo = "clanworld:doctrine:v1:${state.clanId}:$payloadHash:$owner:${GoldSolanaClient.BURN_AMOUNT}:0"
        val tx = runCatching {
          app.goldClient.buildBurn(
            ownerBase58 = owner,
            amount = GoldSolanaClient.BURN_AMOUNT,
            memo = memo,
          )
        }.getOrElse {
          vm.setSavePhase(SendPhase.Failed, it.message ?: "could not build GOLD burn.")
          return@launch
        }
        val result = app.mwaClient.signAndSendTransaction(mwaSender, token, tx)
        when (result) {
          is MwaResult.Ok -> {
            runCatching {
              app.convexClient.saveDoctrineAfterGoldTx(
                clanId = state.clanId,
                strategy = state.draftStrategy,
                notes = state.draftNotes,
                owner = owner,
                signature = result.value,
                burnAmount = GoldSolanaClient.BURN_AMOUNT,
                memo = memo,
              )
            }.onSuccess {
              vm.confirmSeal(result.value)
            }.onFailure {
              vm.setSavePhase(
                SendPhase.Failed,
                it.message ?: "GOLD burned, but doctrine was not recorded.",
              )
            }
          }
          is MwaResult.UserDeclined -> vm.setSavePhase(SendPhase.Idle)
          is MwaResult.WalletNotFound ->
            vm.setSavePhase(SendPhase.Failed, "no wallet found on device.")
          is MwaResult.Error ->
            vm.setSavePhase(SendPhase.Failed, result.cause.message ?: "the wallet refused the seal.")
        }
      }
    },
    onDismissStatus = vm::clearStatus,
  )
}

@Composable
private fun StrategyEditor(
  state: StrategyEditorUiState,
  onBack: () -> Unit,
  onStrategyChange: (String) -> Unit,
  onNotesChange: (String) -> Unit,
  onCommit: () -> Unit,
  onDismissStatus: () -> Unit,
) {
  val strategyDirty = state.draftStrategy != state.committedStrategy
  val notesDirty = state.draftNotes != state.committedNotes
  val anyDirty = strategyDirty || notesDirty

  Column(
    modifier = Modifier
      .fillMaxSize()
      .imePadding()
      .verticalScroll(rememberScrollState()),
  ) {
    BackBar(text = clanDisplayName(state.clanId), onBack = onBack)

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
      OrnamentRule(label = "ÆLDER ESSENCE")

      Spacer(Modifier.height(18.dp))

      FieldShell(
        label = "STRATEGY",
        charCount = state.draftStrategy.length,
        limit = STRATEGY_LIMIT,
        dirty = strategyDirty,
      ) {
        EssenceTextField(
          value = state.draftStrategy,
          onValueChange = onStrategyChange,
          minHeight = 110.dp,
          maxHeight = 220.dp,
          enabled = state.savePhase != SendPhase.Signing,
          placeholder = "Tell your Elder what to focus on this season — economy, alliances, defense priorities. They'll read this every time their context resets.",
        )
      }

      Spacer(Modifier.height(12.dp))

      FieldShell(
        label = "NOTES",
        charCount = state.draftNotes.length,
        limit = NOTES_LIMIT,
        dirty = notesDirty,
      ) {
        EssenceTextField(
          value = state.draftNotes,
          onValueChange = onNotesChange,
          minHeight = 80.dp,
          maxHeight = 160.dp,
          enabled = state.savePhase != SendPhase.Signing,
          placeholder = "Free-form reminders, intel about other clans, debts owed. Anything you want them to remember.",
        )
      }

      Spacer(Modifier.height(20.dp))

      val ctaEnabled = anyDirty &&
        (state.savePhase == SendPhase.Idle || state.savePhase == SendPhase.Failed)
      EmberCta(
        text = when (state.savePhase) {
          SendPhase.Idle -> if (anyDirty) "Burn 5 GOLD to Commit" else "Sealed · No Changes"
          SendPhase.Signing -> "Sealing…"
          SendPhase.Queued -> "Sealed ✓"
          SendPhase.Failed -> "Try Again"
        },
        onClick = onCommit,
        enabled = ctaEnabled,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(Modifier.height(10.dp))

      StatusLine(
        successMsg = state.statusBody,
        errorMsg = state.errorMessage,
        onDismiss = onDismissStatus,
      )

      Spacer(Modifier.height(40.dp))
    }
  }
}

private fun String.sha256Hex(): String =
  MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString("") { "%02x".format(it) }

// ─────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────

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
      text = "the writ of ${text.split(" ").lastOrNull()?.lowercase() ?: "—"}",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )
  }
}

@Composable
private fun OrnamentRule(label: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    SideRule()
    Text(
      text = label,
      style = ClanWorldTheme.type.sectionHead,
      color = ClanWorldTheme.colors.parchment,
    )
    SideRule()
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SideRule() {
  Box(
    modifier = Modifier
      .weight(1f)
      .height(1.dp)
      .background(ClanWorldTheme.colors.hairlineStrong),
  )
}

/**
 * Iron-card with a gold-mono header (label + dirty dot + char count) and
 * the inner text field. Border + glow shift to ember when dirty. Mirrors
 * web PR #51's `FieldShell` in `EssenceSection.tsx`.
 */
@Composable
private fun FieldShell(
  label: String,
  charCount: Int,
  limit: Int,
  dirty: Boolean,
  content: @Composable () -> Unit,
) {
  val ember = ClanWorldTheme.colors.ember
  val emberGlow = ClanWorldTheme.colors.emberGlow
  val borderIron = ClanWorldTheme.colors.hairline
  val borderColor by animateColorAsState(
    targetValue = if (dirty) ember else borderIron,
    animationSpec = tween(220),
    label = "field-border",
  )
  val overshoot = charCount > (limit * 0.9).toInt()
  val shape = RoundedCornerShape(6.dp)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .drawBehind {
        if (dirty) {
          val expand = 6.dp.toPx()
          drawRoundRect(
            color = emberGlow.copy(alpha = 0.18f),
            topLeft = Offset(-expand, -expand + 1.dp.toPx()),
            size = Size(size.width + expand * 2, size.height + expand * 2),
            cornerRadius = CornerRadius(6.dp.toPx() + expand),
          )
        }
      }
      .clip(shape)
      .background(ClanWorldTheme.colors.iron2)
      .border(1.dp, borderColor, shape),
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = label,
          style = ClanWorldTheme.type.eyebrow.copy(fontSize = 10.sp),
          color = if (dirty) emberGlow else ClanWorldTheme.colors.warmFaint,
        )
        if (dirty) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .drawBehind {
                val r = size.minDimension / 2f
                drawCircle(ember.copy(alpha = 0.55f), radius = r * 1.6f, center = Offset(r, r))
              }
              .background(ember),
          )
        }
        Spacer(Modifier.weight(1f))
        Text(
          text = "$charCount / $limit",
          style = ClanWorldTheme.type.monoNano,
          color = if (overshoot) ClanWorldTheme.colors.goldBright
          else ClanWorldTheme.colors.warmFaint,
        )
      }
      content()
    }
  }
}

@Composable
private fun EssenceTextField(
  value: String,
  onValueChange: (String) -> Unit,
  minHeight: Dp,
  maxHeight: Dp,
  enabled: Boolean,
  placeholder: String,
) {
  val ember = ClanWorldTheme.colors.ember
  val warm = ClanWorldTheme.colors.warm
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val interactionSource = remember { MutableInteractionSource() }

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    textStyle = ClanWorldTheme.type.body.copy(color = warm),
    cursorBrush = SolidColor(ember),
    interactionSource = interactionSource,
    keyboardOptions = KeyboardOptions(
      capitalization = KeyboardCapitalization.Sentences,
      imeAction = ImeAction.Default,
    ),
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = minHeight, max = maxHeight)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    decorationBox = { inner ->
      if (value.isEmpty()) {
        Text(
          text = placeholder,
          style = ClanWorldTheme.type.scriptItalic.copy(color = warmFaint),
        )
      }
      inner()
    },
  )
}

@Composable
private fun StatusLine(
  successMsg: String?,
  errorMsg: String?,
  onDismiss: () -> Unit,
) {
  LaunchedEffect(successMsg) {
    if (successMsg != null) {
      delay(5_000L)
      onDismiss()
    }
  }
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(18.dp)
      .clickable(enabled = errorMsg != null || successMsg != null) { onDismiss() },
    contentAlignment = Alignment.CenterStart,
  ) {
    AnimatedVisibility(
      visible = errorMsg != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Text(
        text = "✕  ${errorMsg ?: ""}",
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.danger,
      )
    }
    AnimatedVisibility(
      visible = errorMsg == null && successMsg != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Text(
        text = "✓  ${successMsg ?: ""}",
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.success,
      )
    }
  }
}

@Suppress("unused") private val _kKeepColor: Color = Color.Transparent
