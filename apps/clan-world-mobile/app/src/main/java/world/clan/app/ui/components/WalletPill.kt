package world.clan.app.ui.components

import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.SweepGradient
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import world.clan.app.R
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.wallet.WalletIdentity

/**
 * Pill displayed in the [CrownHeader]. Tap to open a dropdown with
 * Disconnect.
 *
 * - Two-line layout (subtitle on top, name below) so the pill reads as
 *   substantial in the header bar.
 * - Name resolution per [WalletIdentity.displayName].
 * - When `identity.isSeekerVerified`, the border becomes a rotating
 *   ember/gold/rune sweep gradient and an outer halo pulses — this is
 *   the visual hook that says "this user is on a verified Seeker."
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun WalletPill(
  identity: WalletIdentity,
  goldBalance: Long?,
  onMintGold: () -> Unit,
  onViewWallet: () -> Unit,
  onDisconnect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuOpen by remember { mutableStateOf(false) }
  val pillShape = RoundedCornerShape(999.dp)
  val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
  val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

  val rune = ClanWorldTheme.colors.rune
  val ember = ClanWorldTheme.colors.ember
  val gold = ClanWorldTheme.colors.gold
  val goldBright = ClanWorldTheme.colors.goldBright
  val hairline = ClanWorldTheme.colors.hairline
  val parchment = ClanWorldTheme.colors.parchment
  val warmDim = ClanWorldTheme.colors.warmDim

  // Rotating glow + pulse driven from one infinite transition so the two
  // animations stay phase-locked.
  val transition = rememberInfiniteTransition(label = "wallet-pill")
  val angle by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
    label = "skr-angle",
  )
  val pulse by transition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOut), RepeatMode.Reverse),
    label = "pulse",
  )
  val runePulse by transition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
    label = "rune-pulse",
  )

  Box(modifier = modifier) {
    val pillModifier = if (identity.isSeekerVerified) {
      val brush = RotatingSweepBrush(
        colors = listOf(
          ember,
          goldBright,
          gold,
          ember.copy(alpha = 0.3f),
          ember,
          gold,
          goldBright,
          ember,
        ),
        angleDegrees = angle,
      )
      Modifier
        // Outer halo — pulsing ember glow under the pill.
        .drawBehind {
          val r = (size.height / 2f)
          val expandPx = (4.dp.toPx() * pulse)
          drawRoundRect(
            color = ember.copy(alpha = 0.20f * pulse),
            topLeft = Offset(-expandPx, -expandPx),
            size = Size(size.width + expandPx * 2, size.height + expandPx * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r + expandPx),
          )
        }
        .border(
          border = BorderStroke(1.5.dp, brush),
          shape = pillShape,
        )
    } else {
      Modifier.border(
        width = 1.dp,
        color = hairline,
        shape = pillShape,
      )
    }

    Row(
      modifier = Modifier
        .then(pillModifier)
        .background(
          color = if (identity.isSeekerVerified)
            ember.copy(alpha = 0.04f)
          else
            gold.copy(alpha = 0.04f),
          shape = pillShape,
        )
        .combinedClickable(
          onClick = { menuOpen = !menuOpen },
          onLongClick = {
            clipboard.setText(androidx.compose.ui.text.AnnotatedString(identity.pubkeyBase58))
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
          },
        )
        .padding(horizontal = 14.dp, vertical = 8.dp)
        .widthIn(min = 140.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Status dot (rune cyan, pulsing)
      Box(
        Modifier
          .size(6.dp)
          .drawBehind {
            drawCircle(
              color = rune.copy(alpha = 0.45f * runePulse),
              radius = (size.minDimension / 2f) + (3.dp.toPx() * runePulse),
            )
            drawCircle(color = rune, radius = size.minDimension / 2f)
          },
      )

      Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
          text = identity.subtitle.uppercase(),
          style = ClanWorldTheme.type.monoNano,
          color = if (identity.isSeekerVerified) ember else warmDim,
        )
        Text(
          text = identity.displayName,
          style = ClanWorldTheme.type.monoData,
          color = parchment,
        )
      }

      // Chevron-down hint that this is tappable
      Icon(
        painter = androidx.compose.ui.res.painterResource(R.drawable.ui_chevron_down),
        contentDescription = "Wallet menu",
        tint = warmDim,
        modifier = Modifier.size(12.dp),
      )
    }

    // Dropdown menu — opens below the pill, right-aligned to the parent.
    DropdownMenu(
      expanded = menuOpen,
      onDismissRequest = { menuOpen = false },
      modifier = Modifier
        .background(ClanWorldTheme.colors.iron)
        .border(1.dp, ClanWorldTheme.colors.hairline, RoundedCornerShape(6.dp)),
      offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 6.dp),
    ) {
      Row(
        modifier = Modifier
          .widthIn(min = 190.dp)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "GOLD",
          style = ClanWorldTheme.type.monoMicro,
          color = ClanWorldTheme.colors.warmDim,
        )
        Text(
          text = goldBalance?.let { "${it.formatThousands()} g" } ?: "LOADING",
          style = ClanWorldTheme.type.monoData,
          color = ClanWorldTheme.colors.goldBright,
        )
      }
      Box(
        modifier = Modifier
          .width(190.dp)
          .height(1.dp)
          .padding(horizontal = 12.dp)
          .border(0.5.dp, ClanWorldTheme.colors.hairline, RoundedCornerShape(999.dp)),
      )
      DropdownMenuItem(
        text = {
          Text(
            text = "MINT 100K GOLD",
            style = ClanWorldTheme.type.monoMicro,
            color = ClanWorldTheme.colors.rune,
          )
        },
        onClick = {
          menuOpen = false
          onMintGold()
        },
      )
      DropdownMenuItem(
        text = {
          Text(
            text = "VIEW WALLET",
            style = ClanWorldTheme.type.monoMicro,
            color = ClanWorldTheme.colors.warm,
          )
        },
        onClick = {
          menuOpen = false
          onViewWallet()
        },
      )
      DropdownMenuItem(
        text = {
          Text(
            text = "DISCONNECT",
            style = ClanWorldTheme.type.monoMicro,
            color = ClanWorldTheme.colors.danger,
          )
        },
        onClick = {
          menuOpen = false
          onDisconnect()
        },
      )
    }
  }
}

private fun Long.formatThousands(): String {
  val s = toString()
  return s.reversed().chunked(3).joinToString(",").reversed()
}

// ─────────────────────────────────────────────────────────────────────────
// Rotating sweep-gradient brush — used by the .skr glow border.
// ─────────────────────────────────────────────────────────────────────────

/**
 * A [ShaderBrush] backed by an Android [SweepGradient] whose local matrix
 * rotates by [angleDegrees] around the brush's center. Re-created each
 * composition; the underlying shader is rebuilt per draw.
 */
private class RotatingSweepBrush(
  colors: List<Color>,
  private val angleDegrees: Float,
) : ShaderBrush() {
  private val argbColors = colors.map { it.toArgb() }.toIntArray()

  override fun createShader(size: Size): Shader {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val sweep = SweepGradient(cx, cy, argbColors, null)
    val matrix = Matrix().apply { setRotate(angleDegrees, cx, cy) }
    sweep.setLocalMatrix(matrix)
    return sweep
  }
}
