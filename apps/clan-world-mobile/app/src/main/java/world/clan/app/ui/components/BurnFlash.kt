package world.clan.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Ephemeral post-tx burn-counter flash. Renders only briefly after each
 * successful whisper send: scale-in + flicker, hold, fade. Replaces the
 * persistent web BurnCounter that drifted upward with a fake number.
 *
 *     BurnFlash(triggerKey = lastBurnAt, amount = 5L)
 *
 * Pass any value that changes per send (e.g. `sentCount` or `lastBurnAt`)
 * as `triggerKey`. The flash mounts whenever the key changes from a
 * non-zero value.
 *
 * Mirrors web PR #51's `apps/web/src/pages/agent/BurnFlash.tsx`.
 */
@Composable
fun BurnFlash(
  triggerKey: Long,
  amount: Long,
  modifier: Modifier = Modifier,
  ttlMs: Long = 2_800L,
) {
  val opacity = remember(triggerKey) { Animatable(0f) }

  LaunchedEffect(triggerKey) {
    if (triggerKey == 0L) return@LaunchedEffect
    // Scale-in + fade-in (200ms)
    opacity.animateTo(1f, tween(200))
    // Hold + flicker (~280ms × 2 cycles)
    repeat(2) {
      opacity.animateTo(0.78f, tween(140))
      opacity.animateTo(1f, tween(140))
    }
    // Hold the rest of the window
    delay((ttlMs - 760L).coerceAtLeast(0L))
    // Fade out (300ms)
    opacity.animateTo(0f, tween(300))
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(28.dp),
    contentAlignment = Alignment.Center,
  ) {
    if (opacity.value > 0f) {
      Row(
        modifier = Modifier.alpha(opacity.value),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = "🔥",
          style = ClanWorldTheme.type.body.copy(fontSize = 13.sp),
        )
        Text(
          text = "+${amount}",
          style = ClanWorldTheme.type.monoData.copy(
            color = ClanWorldTheme.colors.goldBright,
            fontSize = 12.sp,
          ),
        )
        Text(
          text = "BURNED",
          style = ClanWorldTheme.type.eyebrow.copy(
            color = ClanWorldTheme.colors.emberHover,
            fontSize = 10.sp,
          ),
        )
      }
    }
  }
}
