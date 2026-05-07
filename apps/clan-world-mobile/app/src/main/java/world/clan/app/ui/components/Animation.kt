package world.clan.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Page-load stagger helper. Each child reveals at `60ms + index * 100ms`,
 * fading + sliding up — matches the prototype's `@keyframes rise` timing
 * with `cubic-bezier(.2,.8,.2,1)` ≈ FastOutSlowInEasing.
 */
@Composable
fun StaggeredEntry(
  index: Int,
  enabled: Boolean = true,
  baseDelayMs: Long = 60L,
  stepMs: Long = 100L,
  content: @Composable () -> Unit,
) {
  if (!enabled) {
    content()
    return
  }
  var visible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(baseDelayMs + index * stepMs)
    visible = true
  }
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(animationSpec = tween(700, easing = FastOutSlowInEasing)) +
        slideInVertically(
          animationSpec = tween(700, easing = FastOutSlowInEasing),
          initialOffsetY = { it / 4 },
        ),
  ) { content() }
}
