package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import world.clan.app.data.ELDERS
import world.clan.app.data.elderById
import world.clan.app.ui.theme.CockpitTokens

/**
 * Bottom-half pager: 4 clan panels swiped horizontally. Mirrors the
 * scroll-snap pager in MobileCockpitLayout.tsx, with native paging
 * (HorizontalPager) replacing the CSS scroll-snap. Page-indicator dots
 * sit above the pager. Tapping a dot animates to that page.
 *
 * `contentAlpha` lets the parent fade the whole pager during the
 * collapse transition (the web uses opacity 0 → 1 over 250ms).
 */
@Composable
fun ClanPanelPager(
  modifier: Modifier = Modifier,
  activeClanId: Int,
  onActiveClanChange: (Int) -> Unit,
  contentAlpha: Float = 1f,
  onOwnerControl: (Int) -> Unit,
) {
  val pagerState = rememberPagerState(
    initialPage = (activeClanId - 1).coerceIn(0, ELDERS.size - 1),
    pageCount = { ELDERS.size },
  )
  val scope = rememberCoroutineScope()

  // Sync pager → activeClanId (when user swipes)
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }
      .distinctUntilChanged()
      .collect { page -> onActiveClanChange(page + 1) }
  }
  // Sync activeClanId → pager (when set externally, e.g. dot tap)
  LaunchedEffect(activeClanId) {
    val target = (activeClanId - 1).coerceIn(0, ELDERS.size - 1)
    if (pagerState.currentPage != target) {
      pagerState.animateScrollToPage(target)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .alpha(contentAlpha)
      .background(CockpitTokens.Bg.Void),
  ) {
    PageIndicatorDots(
      pageCount = ELDERS.size,
      currentPage = pagerState.currentPage,
      onDotClick = { idx ->
        scope.launch { pagerState.animateScrollToPage(idx) }
      },
    )

    HorizontalPager(
      state = pagerState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      pageSpacing = 0.dp,
    ) { pageIndex ->
      val elder = remember(pageIndex) { elderById(pageIndex + 1) }
      ClanPanel(
        elder = elder,
        onOwnerControl = { onOwnerControl(elder.clanId) },
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 4.dp, vertical = 4.dp),
      )
    }
  }
}

@Composable
private fun PageIndicatorDots(
  pageCount: Int,
  currentPage: Int,
  onDotClick: (Int) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(pageCount) { idx ->
      val isActive = idx == currentPage
      val color = if (isActive) elderById(idx + 1).accent else CockpitTokens.TextC.OnIronDim
      val alpha = if (isActive) 1f else 0.4f
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = alpha))
          .clickableNoIndication { onDotClick(idx) }
      )
    }
  }
}

// ---- helpers -------------------------------------------------------------

@Composable
private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier {
  val src = remember { MutableInteractionSource() }
  return this.clickable(interactionSource = src, indication = null, onClick = onClick)
}
