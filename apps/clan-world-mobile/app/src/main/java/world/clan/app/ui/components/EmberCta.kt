package world.clan.app.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Primary CTA: ember pill with breathing glow + shimmer sweep.
 *
 * prototype: .ember-cta (lines 460–496) + @keyframes ctaBreathe + @keyframes shimmer.
 *
 * Implementation notes:
 * - We do NOT use Modifier.shadow for the breathing glow — shadow's color
 *   isn't animatable cleanly. Instead two soft halos drawn in drawBehind,
 *   modulated by `glow.value`.
 * - Shimmer is one translated linear gradient, painted via drawWithContent
 *   inside the clipped surface so it doesn't bleed past the corners.
 */
@Composable
fun EmberCta(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val ember = ClanWorldTheme.colors.ember
  val emberGlow = ClanWorldTheme.colors.emberGlow
  val shape = RoundedCornerShape(6.dp)

  val infinite = rememberInfiniteTransition(label = "ember-cta")
  val glow by infinite.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.55f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2500, easing = EaseInOut),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "glow",
  )
  val shimmerX by infinite.animateFloat(
    initialValue = -1f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "shimmer",
  )

  Box(
    modifier
      // Soft halos behind the pill (the breathing glow).
      .drawBehind {
        // Tight halo (~24dp blur radius simulated via stroked rounded rect)
        val rTight = 8.dp.toPx()
        for (i in 0..5) {
          val expand = (i * 3).dp.toPx()
          val a = (glow * (1f - i / 6f)).coerceIn(0f, 1f) * 0.18f
          drawRoundRect(
            color = ember.copy(alpha = a),
            topLeft = Offset(-expand, -expand + 2.dp.toPx()),
            size = Size(size.width + expand * 2, size.height + expand * 2),
            cornerRadius = CornerRadius(rTight + expand),
          )
        }
      }
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(ember, Color(0xFFE85825)),
        ),
      )
      // Inner top bevel highlight (1px stroke @ alpha 0.4)
      .drawWithContent {
        drawContent()
        drawRoundRect(
          color = Color(0x66FFAA78),
          style = Stroke(width = 1.dp.toPx()),
          cornerRadius = CornerRadius(6.dp.toPx()),
        )
        // Shimmer band — diagonal white sweep at low alpha
        val w = size.width
        val bandW = w * 0.4f
        val cx = shimmerX * w * 1.5f
        drawRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color.Transparent,
              Color.White.copy(alpha = 0.18f),
              Color.Transparent,
            ),
            start = Offset(cx, 0f),
            end = Offset(cx + bandW, size.height),
          ),
          topLeft = Offset(0f, 0f),
          size = Size(w, size.height),
        )
      }
      .clickable(enabled = enabled, role = Role.Button) { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text.uppercase(),
      style = ClanWorldTheme.type.ctaLabel,
      color = Color(0xFF1A0C04),
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
    )
  }
}
