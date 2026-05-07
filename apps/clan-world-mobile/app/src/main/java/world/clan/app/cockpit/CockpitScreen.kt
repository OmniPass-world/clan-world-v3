package world.clan.app.cockpit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import world.clan.app.ui.theme.CockpitTokens

/**
 * Top-level cockpit page. Mirrors the mobile branch of
 * apps/web/src/pages/Cockpit.tsx (specifically MobileCockpitLayout):
 *
 *   ┌──────────────────────────────────┐
 *   │ CockpitHeader  (40dp)            │
 *   ├──────────────────────────────────┤
 *   │ Map WebView  (flex weight)       │
 *   │   ┌────────┐  CollapseToggle ↕   │
 *   ├──┘────────└──────────────────────┤
 *   │ ClanPanelPager  (flex weight)    │
 *   └──────────────────────────────────┘
 *
 * When `collapsed` is true the bottom (pager) area animates to weight 0
 * and the map fills the entire body. The 250ms ease transition matches
 * the web's `flex-basis 250ms ease`.
 */
@Composable
fun CockpitScreen(
  onOwnerControl: (Int) -> Unit,
) {
  // Persist active page + collapsed across config changes (rotation, theme)
  // — Compose equivalent of the web's localStorage keys.
  var collapsed by rememberSaveable { mutableStateOf(false) }
  var activeClanId by rememberSaveable { mutableStateOf(1) }

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
      .background(CockpitTokens.Bg.Void)
      .windowInsetsPadding(WindowInsets.systemBars),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      CockpitHeader(modifier = Modifier.fillMaxWidth())

      // Map region
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(mapWeight),
        contentAlignment = Alignment.BottomCenter,
      ) {
        MapWebView(modifier = Modifier.fillMaxSize())

        CollapseToggle(
          collapsed = collapsed,
          activeClanId = activeClanId,
          onToggle = { collapsed = !collapsed },
        )
      }

      // Pager region (4 clan panels)
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
  }
}
