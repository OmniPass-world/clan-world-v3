package world.clan.app.cockpit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.clan.app.data.ELDERS
import world.clan.app.ui.theme.CockpitTokens

/**
 * Top-level cockpit page. Edge-to-edge — content fills the entire screen
 * including the unsafe area at the top (where CLAN | WORLD straddles the
 * camera cutout) and bottom.
 *
 * Layout:
 *  - Two-row header (CLAN/WORLD up top in unsafe space; tick + connection
 *    + bulletin button in the second row).
 *  - Map area (weighted) — WebView fills it; page-indicator dots and the
 *    collapse toggle overlay the bottom of the map. Map's bottom corners
 *    are rounded to match the device's natural top-corner curvature so the
 *    map and panel meet flush.
 *  - Panel area (weighted) — full-width HorizontalPager, no padding.
 *  - Bulletin flyout slides down from the header when toggled.
 */
@Composable
fun CockpitScreen(
  onOwnerControl: (Int) -> Unit,
) {
  var collapsed by rememberSaveable { mutableStateOf(false) }
  var activeClanId by rememberSaveable { mutableStateOf(1) }
  var bulletinOpen by rememberSaveable { mutableStateOf(false) }

  val mapWeight by animateFloatAsState(
    targetValue = if (collapsed) 1f else 0.5f,
    animationSpec = tween(durationMillis = 250),
    label = "mapWeight",
  )
  val pagerWeight by animateFloatAsState(
    targetValue = if (collapsed) 0.0001f else 0.5f, // 0 not allowed in weight
    animationSpec = tween(durationMillis = 250),
    label = "pagerWeight",
  )
  val pagerAlpha by animateFloatAsState(
    targetValue = if (collapsed) 0f else 1f,
    animationSpec = tween(durationMillis = 250),
    label = "pagerAlpha",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Void),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      CockpitHeader(
        modifier = Modifier.fillMaxWidth(),
        bulletinOpen = bulletinOpen,
        onBulletinToggle = { bulletinOpen = !bulletinOpen },
      )

      // Map region — fills its weighted height; dots + toggle overlay
      // the bottom edge so there's no gap before the panel.
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(mapWeight)
          .clip(
            RoundedCornerShape(
              bottomStart = 16.dp,
              bottomEnd = 16.dp,
            )
          ),
      ) {
        MapWebView(modifier = Modifier.fillMaxSize())

        // Page-indicator dots overlay (anchored above the toggle)
        PageIndicatorOverlay(
          pageCount = ELDERS.size,
          currentPage = (activeClanId - 1).coerceIn(0, ELDERS.size - 1),
          onDotClick = { activeClanId = it + 1 },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 44.dp),
        )

        CollapseToggle(
          collapsed = collapsed,
          activeClanId = activeClanId,
          onToggle = { collapsed = !collapsed },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 8.dp),
        )
      }

      // Pager region — full width, no padding around the panels.
      ClanPanelPager(
        modifier = Modifier
          .fillMaxWidth()
          .weight(pagerWeight),
        activeClanId = activeClanId,
        onActiveClanChange = { activeClanId = it },
        contentAlpha = pagerAlpha,
        onOwnerControl = onOwnerControl,
      )
    }

    // Bulletin flyout slides down from beneath the header.
    BulletinFlyout(
      visible = bulletinOpen,
      onClose = { bulletinOpen = false },
    )
  }
}
