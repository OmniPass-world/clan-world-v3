package world.clan.app.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Unified "the wallet sealed it" success notice. Used by Forge "FORGED ✓",
 * Hire "SEALED ✓", Steering "QUEUED ✓", Strategy "SEALED ✓".
 *
 * - Success-tinted bg at 0.18 alpha, full-width pill, 54dp tall.
 * - Label in CTA-label type, success color.
 * - Subtle breathing alpha (0.85↔1.0) so the moment feels alive rather
 *   than a static stamp; makes the 1.3-1.5s linger more rewarding.
 */
@Composable
fun SealedNotice(
  label: String,
  modifier: Modifier = Modifier,
) {
  val success = ClanWorldTheme.colors.success

  val transition = rememberInfiniteTransition(label = "sealed-pulse")
  val pulse by transition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = EaseInOut),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "alpha",
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(54.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(success.copy(alpha = 0.18f))
      .alpha(pulse),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label.uppercase(),
      style = ClanWorldTheme.type.ctaLabel,
      color = success,
    )
  }
}
