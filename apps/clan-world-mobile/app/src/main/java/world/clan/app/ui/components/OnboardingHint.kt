package world.clan.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import world.clan.app.R
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * One-shot dismissible coachmark for first-time users. Renders a small
 * parchment-tinted banner with title + body + close icon. Disappears
 * permanently when dismissed (or on first prevent-show-again signal from
 * the host).
 *
 * Hosts manage the "have they seen it?" flag via SessionStore.hasSeenFlag /
 * markFlagSeen. The banner shrinks itself out cleanly on dismiss so the
 * surrounding content reflows without a layout pop.
 */
@Composable
fun OnboardingHint(
  visible: Boolean,
  title: String,
  body: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Local visibility tracks the parent's `visible` plus an in-flight
  // dismissal. Without this the parent flag flips synchronously and the
  // exit animation has nothing to animate.
  var localVisible by remember(visible) { mutableStateOf(visible) }

  AnimatedVisibility(
    visible = localVisible,
    enter = fadeIn(),
    exit = fadeOut() + shrinkVertically(),
    modifier = modifier,
  ) {
    val gold = ClanWorldTheme.colors.gold
    val goldDim = ClanWorldTheme.colors.goldDim
    val warm = ClanWorldTheme.colors.warm
    val warmDim = ClanWorldTheme.colors.warmDim
    val iron2 = ClanWorldTheme.colors.iron2

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(
          Brush.verticalGradient(
            0f to gold.copy(alpha = 0.10f),
            1f to gold.copy(alpha = 0.04f),
          ),
        )
        .drawBehind {
          drawRoundRect(
            color = goldDim,
            cornerRadius = CornerRadius(6.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
          )
        }
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Small ember-pill marker on the left
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(RoundedCornerShape(50))
          .background(gold),
      )
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = title.uppercase(),
          style = ClanWorldTheme.type.monoMicro,
          color = gold,
        )
        Text(
          text = body,
          style = ClanWorldTheme.type.scriptItalic,
          color = warm,
        )
      }
      // Dismiss × button
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(50))
          .background(iron2)
          .clickable {
            localVisible = false
            onDismiss()
          }
          .padding(6.dp),
      ) {
        Icon(
          painter = painterResource(R.drawable.ui_check),
          contentDescription = "dismiss",
          tint = warmDim,
          modifier = Modifier.size(12.dp),
        )
      }
    }
  }
}
