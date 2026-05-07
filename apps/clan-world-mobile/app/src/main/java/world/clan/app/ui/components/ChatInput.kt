package world.clan.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * LLM-style chat input. Single rounded rectangle with the textarea up top
 * and the cooldown chip + send button packed into the bottom edge.
 *
 *   ┌─────────────────────────────────────┐
 *   │ Whisper to your Ælder…              │
 *   │                                     │
 *   │  [▓░ FORGE COOLING 1:42]   [⟢SEND] │
 *   └─────────────────────────────────────┘
 *
 * Focus → ember-glow border + soft halo. Insufficient gold → red border +
 * "Need X g" inside the chip. Cooling but tappable → gold-tone send (force
 * send pays a per-minute skip-tax).
 *
 * Mirrors the web ChatInput shipped in PR #51
 * (`apps/web/src/pages/agent/ChatInput.tsx`).
 */
@Composable
fun ChatInput(
  draft: String,
  onDraftChange: (String) -> Unit,
  /** Wall-clock ms when the last whisper was sent. -1 if no sends yet. */
  lastSentAt: Long,
  /** Total cooldown window in ms. */
  cooldownTotalMs: Long,
  /** Live wall-clock — pass a ticking value (e.g. via `useNow(250)`). */
  nowMs: Long,
  /** User's GOLD balance — gates the send button. */
  gold: Long,
  /** Send-in-flight. */
  sending: Boolean,
  /** Cost per send (whole units). */
  burnAmount: Long = 5L,
  /** Skip-tax per full minute remaining. */
  skipTaxPerMinute: Long = 1_000L,
  modifier: Modifier = Modifier,
  onSend: () -> Unit,
) {
  val cooldownMs =
    if (lastSentAt < 0L) 0L else (cooldownTotalMs - (nowMs - lastSentAt)).coerceAtLeast(0L)
  val cooldownActive = cooldownMs > 0L
  val fullMinutes = ((cooldownMs + 59_999L) / 60_000L).coerceAtLeast(0L)
  val skipTax = if (cooldownActive) fullMinutes * skipTaxPerMinute else 0L
  val totalCost = burnAmount + skipTax
  val insufficient = gold < totalCost
  val empty = draft.trim().isEmpty()
  val disabled = sending || empty || insufficient

  val cooldownPct =
    if (cooldownTotalMs > 0L) (cooldownMs.toFloat() / cooldownTotalMs.toFloat()).coerceIn(0f, 1f)
    else 0f
  val cooldownLabel = formatCooldown(cooldownMs)

  val interactionSource = remember { MutableInteractionSource() }
  val focused by interactionSource.collectIsFocusedAsState()

  val ember = ClanWorldTheme.colors.ember
  val emberGlow = ClanWorldTheme.colors.emberGlow
  val danger = ClanWorldTheme.colors.danger
  val hairlineStrong = ClanWorldTheme.colors.hairlineStrong

  val borderTarget = when {
    insufficient -> danger
    focused -> ember
    else -> hairlineStrong
  }
  val borderColor by animateColorAsState(borderTarget, tween(220), label = "chat-border")

  val shape = RoundedCornerShape(22.dp)

  Box(
    modifier = modifier
      // Soft halo behind the input when focused.
      .drawBehind {
        if (focused && !insufficient) {
          val expand = 4.dp.toPx()
          drawRoundRect(
            color = emberGlow.copy(alpha = 0.25f),
            topLeft = Offset(-expand, -expand),
            size = Size(size.width + expand * 2, size.height + expand * 2),
            cornerRadius = CornerRadius(22.dp.toPx() + expand),
          )
        }
      }
      .clip(shape)
      .background(ClanWorldTheme.colors.iron2)
      .border(1.dp, borderColor, shape)
      .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp),
  ) {
    Column {
      // Textarea — autosizes 1–5 rows.
      val textStyle = ClanWorldTheme.type.body.copy(color = ClanWorldTheme.colors.warm)
      BasicTextField(
        value = draft,
        onValueChange = { if (it.length <= 1000) onDraftChange(it) },
        textStyle = textStyle,
        cursorBrush = SolidColor(ember),
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Sentences,
          imeAction = ImeAction.Default,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 64.dp, max = 160.dp),
        decorationBox = { innerTextField ->
          if (draft.isEmpty()) {
            Text(
              text = "Whisper to your Ælder…",
              style = ClanWorldTheme.type.scriptItalic.copy(color = ClanWorldTheme.colors.warmFaint),
            )
          }
          innerTextField()
        },
      )

      Spacer(Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        CooldownChip(
          modifier = Modifier.weight(1f),
          cooldownActive = cooldownActive,
          cooldownPct = cooldownPct,
          label = cooldownLabel,
          skipTax = skipTax,
          insufficient = insufficient,
          totalCost = totalCost,
          burnAmount = burnAmount,
        )
        SendButton(
          cooldownActive = cooldownActive,
          disabled = disabled,
          sending = sending,
          onSend = onSend,
        )
      }
    }
  }
}

@Composable
private fun CooldownChip(
  cooldownActive: Boolean,
  cooldownPct: Float,
  label: String,
  skipTax: Long,
  insufficient: Boolean,
  totalCost: Long,
  burnAmount: Long,
  modifier: Modifier = Modifier,
) {
  val ember = ClanWorldTheme.colors.ember
  val emberDeep = ClanWorldTheme.colors.emberDeep
  val goldBright = ClanWorldTheme.colors.goldBright
  val danger = ClanWorldTheme.colors.danger
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim
  val iron3 = ClanWorldTheme.colors.iron3

  val shape = RoundedCornerShape(17.dp)
  Box(
    modifier = modifier
      .height(34.dp)
      .clip(shape)
      .background(iron3)
      .drawBehind {
        // Ember drain bar — drains right-to-left as time ticks.
        if (cooldownActive) {
          val w = size.width * cooldownPct
          drawRect(
            brush = Brush.horizontalGradient(
              colors = listOf(
                emberDeep.copy(alpha = 0.55f),
                ember.copy(alpha = 0.45f),
                goldBright.copy(alpha = 0.40f),
              ),
            ),
            topLeft = Offset.Zero,
            size = Size(w, size.height),
          )
        }
      },
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val dotColor = when {
        insufficient -> danger
        cooldownActive -> ember
        else -> goldBright
      }
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .drawBehind {
            // Soft halo
            val r = size.minDimension / 2f
            drawCircle(dotColor.copy(alpha = 0.45f), radius = r * 1.6f, center = Offset(r, r))
          }
          .background(dotColor),
      )

      when {
        insufficient -> {
          Text(
            text = "Need ${totalCost.formatThousands()} g",
            style = ClanWorldTheme.type.monoNano,
            color = danger,
          )
        }
        cooldownActive -> {
          Text("Forge cooling", style = ClanWorldTheme.type.monoNano, color = warm)
          Text("·", style = ClanWorldTheme.type.monoNano, color = warmDim)
          Text(label, style = ClanWorldTheme.type.monoNano, color = warm)
          Spacer(Modifier.weight(1f))
          if (skipTax > 0L) {
            Text(
              text = "+ ${skipTax.formatThousands()} g",
              style = ClanWorldTheme.type.monoNano,
              color = goldBright,
            )
          }
        }
        else -> {
          Text("Whisper ready", style = ClanWorldTheme.type.monoNano, color = warmDim)
          Text("·", style = ClanWorldTheme.type.monoNano, color = warmDim)
          Text(
            text = "${burnAmount.formatThousands()} g per send",
            style = ClanWorldTheme.type.monoNano,
            color = goldBright,
          )
        }
      }
    }
  }
}

@Composable
private fun SendButton(
  cooldownActive: Boolean,
  disabled: Boolean,
  sending: Boolean,
  onSend: () -> Unit,
) {
  val ember = ClanWorldTheme.colors.ember
  val emberDeep = ClanWorldTheme.colors.emberDeep
  val gold = ClanWorldTheme.colors.gold
  val goldDim = ClanWorldTheme.colors.goldDim
  val iron3 = ClanWorldTheme.colors.iron3
  val onEmber = Color(0xFF1A0C04)

  val bgBrush = when {
    disabled -> Brush.verticalGradient(listOf(iron3, iron3))
    cooldownActive -> Brush.verticalGradient(listOf(gold, goldDim))
    else -> Brush.verticalGradient(listOf(ember, emberDeep))
  }
  val borderColor = when {
    disabled -> ClanWorldTheme.colors.hairline
    cooldownActive -> gold
    else -> ember
  }
  val shape = RoundedCornerShape(17.dp)

  Box(
    modifier = Modifier
      .size(width = 44.dp, height = 34.dp)
      .clip(shape)
      .drawBehind {
        if (!disabled) {
          val color = if (cooldownActive) gold else ember
          val expand = 6.dp.toPx()
          drawRoundRect(
            color = color.copy(alpha = 0.40f),
            topLeft = Offset(-expand, -expand + 1.dp.toPx()),
            size = Size(size.width + expand * 2, size.height + expand * 2),
            cornerRadius = CornerRadius(17.dp.toPx() + expand),
          )
        }
      }
      .background(bgBrush)
      .border(1.dp, borderColor, shape)
      .clickable(enabled = !disabled, role = Role.Button) { onSend() },
    contentAlignment = Alignment.Center,
  ) {
    if (sending) {
      val infinite = rememberInfiniteTransition(label = "send-spin")
      val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
      )
      Box(
        modifier = Modifier
          .size(13.dp)
          .rotate(angle)
          .drawBehind {
            drawArc(
              color = onEmber,
              startAngle = 0f,
              sweepAngle = 270f,
              useCenter = false,
              style = Stroke(width = 1.5.dp.toPx()),
            )
          },
      )
    } else {
      Text(
        text = if (cooldownActive) "⟡" else "⟢",
        style = ClanWorldTheme.type.ctaLabel.copy(
          fontSize = 15.sp,
          letterSpacing = 0.sp,
        ),
        color = if (disabled) ClanWorldTheme.colors.warmFaint
        else if (cooldownActive) onEmber else onEmber,
      )
    }
  }
}

private fun formatCooldown(ms: Long): String {
  if (ms <= 0L) return ""
  val totalSec = ((ms + 999L) / 1000L)
  val m = totalSec / 60L
  val s = totalSec % 60L
  return "%02d:%02d".format(m, s)
}

private fun Long.formatThousands(): String =
  toString().reversed().chunked(3).joinToString(",").reversed()

