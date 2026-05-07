package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import world.clan.app.data.ELDERS
import world.clan.app.data.elderById
import world.clan.app.ui.theme.CockpitTokens

/**
 * Bottom pager — 4 clan panels swiped horizontally, full-width with no
 * surrounding padding. Page-indicator dots are overlaid on the map by
 * [CockpitScreen]; the activeClanId state lives in the parent and is
 * synchronised both ways with the pager.
 *
 * `contentAlpha` lets the parent fade the pager during the collapse
 * transition.
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

  // Pager → activeClanId
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }
      .distinctUntilChanged()
      .collect { page -> onActiveClanChange(page + 1) }
  }
  // activeClanId → pager
  LaunchedEffect(activeClanId) {
    val target = (activeClanId - 1).coerceIn(0, ELDERS.size - 1)
    if (pagerState.currentPage != target) {
      pagerState.animateScrollToPage(target)
    }
  }

  HorizontalPager(
    state = pagerState,
    modifier = modifier
      .fillMaxSize()
      .alpha(contentAlpha)
      .background(CockpitTokens.Bg.Void),
    pageSpacing = 0.dp,
  ) { pageIndex ->
    val elder = remember(pageIndex) { elderById(pageIndex + 1) }
    ClanPanel(
      elder = elder,
      onOwnerControl = { onOwnerControl(elder.clanId) },
      modifier = Modifier.fillMaxWidth().fillMaxSize(),
    )
  }
}
