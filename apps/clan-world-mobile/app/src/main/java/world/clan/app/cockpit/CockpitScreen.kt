package world.clan.app.cockpit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.clan.app.data.ELDERS
import world.clan.app.data.convex.CockpitDataSource
import world.clan.app.data.convex.LocalCockpitData
import world.clan.app.ui.theme.CockpitTokens

/**
 * Top-level cockpit page. Edge-to-edge — content fills the entire screen
 * including the unsafe area at the top.
 *
 * Z-order is deliberate:
 *   1. Cockpit body (map + panel) — drawn first.
 *   2. Bulletin flyout — drawn ON TOP of the body when open.
 *   3. CockpitHeader — drawn LAST, covering any flyout shadow that bleeds
 *      into the header area, so the flyout reads as "attached to / falling
 *      out of" the header rather than passing under it.
 *
 * The body uses spacers in place of the header's height so layout still
 * computes correctly even though the header isn't a child of the column.
 */
@Composable
fun CockpitScreen(
  onOwnerControl: (Int) -> Unit,
  initialClanId: Int = 1,
) {
  var collapsed by rememberSaveable { mutableStateOf(false) }
  // Key the saver on initialClanId so navigating into Cockpit for a
  // different clan resets the active selection instead of stickying the
  // previous clan's id from process restoration.
  var activeClanId by rememberSaveable(initialClanId) { mutableStateOf(initialClanId) }
  var bulletinOpen by rememberSaveable { mutableStateOf(false) }

  // Singleton-per-screen data source. Survives recompositions, dies with
  // the screen — its scope is tied to composition lifecycle so all polling
  // flows stop when CockpitScreen leaves the tree.
  val coroutineScope = rememberCoroutineScope()
  val dataSource = remember(coroutineScope) { CockpitDataSource(scope = coroutineScope) }

  val mapWeight by animateFloatAsState(
    targetValue = if (collapsed) 1f else 0.5f,
    animationSpec = tween(durationMillis = 250),
    label = "mapWeight",
  )
  val pagerWeight by animateFloatAsState(
    targetValue = if (collapsed) 0.0001f else 0.5f,
    animationSpec = tween(durationMillis = 250),
    label = "pagerWeight",
  )
  val pagerAlpha by animateFloatAsState(
    targetValue = if (collapsed) 0f else 1f,
    animationSpec = tween(durationMillis = 250),
    label = "pagerAlpha",
  )

  CompositionLocalProvider(LocalCockpitData provides dataSource) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Void),
  ) {
    // (1) Cockpit body — header height reserved by spacers so the map's
    //     weighted height matches the post-header layout.
    Column(modifier = Modifier.fillMaxSize()) {
      // Spacer for row 1 (status-bar inset, where CLAN/WORLD render)
      Box(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
      // Spacer for row 2 (controls strip)
      Box(modifier = Modifier.fillMaxWidth().height(HeaderRow2Height))

      // Map region — fills its weighted height; dots + toggle overlay the
      // bottom edge so there's no gap before the panel. Map's bottom
      // corners are rounded (mirrors the device's natural top-corner curve).
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

        PageIndicatorOverlay(
          pageCount = ELDERS.size,
          currentPage = (activeClanId - 1).coerceIn(0, ELDERS.size - 1),
          onDotClick = { activeClanId = it + 1 },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 56.dp),
        )

        CollapseToggle(
          collapsed = collapsed,
          activeClanId = activeClanId,
          onToggle = { collapsed = !collapsed },
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 12.dp, bottom = 12.dp),
        )
      }

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

    // (2) Bulletin flyout — drawn over the body. Its top shadow extends
    //     UPWARD into the area the header occupies, where (3) hides it.
    BulletinFlyout(
      visible = bulletinOpen,
      onClose = { bulletinOpen = false },
    )

    // (3) Header drawn last — covers the flyout's top shadow so the flyout
    //     reads as if it falls out of the bottom of the header.
    CockpitHeader(
      modifier = Modifier.fillMaxWidth(),
      bulletinOpen = bulletinOpen,
      onBulletinToggle = { bulletinOpen = !bulletinOpen },
    )
  }
  }
}
