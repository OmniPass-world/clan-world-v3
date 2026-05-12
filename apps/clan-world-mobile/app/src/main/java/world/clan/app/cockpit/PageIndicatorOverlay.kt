package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.clan.app.data.elderById
import world.clan.app.ui.theme.CockpitTokens

/**
 * Page-indicator dots — overlay anchored to the bottom of the clan panel
 * pager (NOT the map). Each dot tints to the corresponding clan's accent
 * when active. Tapping a dot moves the pager.
 *
 * Caller is responsible for adding `Modifier.navigationBarsPadding()` so
 * the dots sit above the gesture handle / nav bar — the panel background
 * itself fills behind the unsafe bottom inset, but interactive controls
 * must not be obscured.
 */
@Composable
fun PageIndicatorOverlay(
  pageCount: Int,
  currentPage: Int,
  onDotClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .wrapContentWidth()
      .height(24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(pageCount) { idx ->
      val isActive = idx == currentPage
      val color = if (isActive) elderById(idx + 1).accent else CockpitTokens.TextC.OnIron
      val alpha = if (isActive) 1f else 0.65f
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = alpha))
          .clickableNoIndication { onDotClick(idx) },
      )
    }
  }
}

@Composable
private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier {
  val src = remember { MutableInteractionSource() }
  return this.clickable(interactionSource = src, indication = null, onClick = onClick)
}
