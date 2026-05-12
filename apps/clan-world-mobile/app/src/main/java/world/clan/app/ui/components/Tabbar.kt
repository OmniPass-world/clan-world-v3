package world.clan.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import world.clan.app.R
import world.clan.app.ui.theme.ClanWorldTheme

enum class RootTab(val label: String, val drawableRes: Int) {
  Hearth("Hearth", R.drawable.ui_tab_hearth),
  Hall("Hall", R.drawable.ui_tab_hall),
  Cockpit("Cockpit", R.drawable.tab_cockpit_clanworld),
  Bazaar("Bazaar", R.drawable.ui_tab_bazaar),
  Codex("Codex", R.drawable.ui_tab_codex),
}

/**
 * Custom root bottom bar.
 *
 * Hosted in the top-level Scaffold (not per-screen) so it does NOT animate
 * with page transitions — only its single shared "ember halo" indicator
 * slides between tabs when the selected tab changes.
 *
 * prototype: .tabbar (lines 327–353)
 */
@Composable
fun ClanWorldTabBar(
  selected: RootTab,
  onSelect: (RootTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hairline = ClanWorldTheme.colors.hairline
  val obsidian = ClanWorldTheme.colors.obsidian
  val emberGlow = ClanWorldTheme.colors.emberGlow
  val ember = ClanWorldTheme.colors.ember
  val warmFaint = ClanWorldTheme.colors.warmFaint

  val selectedIndex = RootTab.values().indexOf(selected)

  // Outer Box: background gradient + top hairline fill the full unsafe
  // bottom area (extends behind the gesture handle). Inner content adds
  // `navigationBarsPadding()` so the tab icons + halo sit ABOVE the
  // gesture handle while the obsidian still paints to screen bottom.
  Box(
    modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          0f to Color.Transparent,
          0.5f to obsidian,
          1f to obsidian,
        ),
      )
      .drawBehind {
        // Top hairline
        drawLine(
          color = hairline,
          start = Offset(0f, 0f),
          end = Offset(size.width, 0f),
          strokeWidth = 1f,
        )
      },
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .height(84.dp)
        .padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 20.dp),
    ) {
      val tabCount = RootTab.values().size
      val tabWidth = maxWidth / tabCount

      // Sliding ember halo indicator — one shared element that animates
      // its X offset as `selected` changes. Sits behind the icons.
      val targetX = tabWidth * selectedIndex + (tabWidth - 28.dp) / 2f
      val haloX by animateDpAsState(
        targetValue = targetX,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "tab-halo-x",
      )

      Box(
        Modifier
          .offset(x = haloX, y = (-2).dp)
          .size(28.dp)
          .drawBehind {
            // Soft halo at the indicator position. Plus-blend so it
            // glows over the obsidian background.
            drawCircle(
              color = emberGlow,
              radius = 14.dp.toPx(),
              blendMode = BlendMode.Plus,
            )
          },
      )

      // Tab row — icons + labels. Tabs themselves do NOT animate; only
      // the icon tint smoothly crossfades on selection.
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        RootTab.values().forEach { tab ->
          if (tab == RootTab.Cockpit) {
            CenterCockpitTabItem(
              selected = tab == selected,
              onClick = { onSelect(tab) },
              modifier = Modifier.weight(1f),
            )
          } else {
            TabItem(
              tab = tab,
              selected = tab == selected,
              onClick = { onSelect(tab) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CenterCockpitTabItem(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val ember = ClanWorldTheme.colors.ember
  val emberGlow = ClanWorldTheme.colors.emberGlow
  val obsidian = ClanWorldTheme.colors.obsidian
  val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
  val border by animateColorAsState(
    targetValue = if (selected) ember else ember.copy(alpha = 0.72f),
    animationSpec = tween(220, easing = FastOutSlowInEasing),
    label = "cockpit-tab-border",
  )

  Box(
    modifier = modifier
      .height(64.dp)
      .offset(y = (-14).dp)
      .clickable(role = Role.Tab) {
        if (!selected) {
          haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
        onClick()
      },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(58.dp)
        .drawBehind {
          drawCircle(
            color = emberGlow,
            radius = 30.dp.toPx(),
            blendMode = BlendMode.Plus,
          )
        }
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF21150D), obsidian),
          ),
        )
        .border(1.dp, border, CircleShape),
    )
    Image(
      painter = painterResource(R.drawable.tab_cockpit_clanworld),
      contentDescription = RootTab.Cockpit.label,
      contentScale = ContentScale.Fit,
      modifier = Modifier.size(72.dp),
    )
  }
}

@Composable
private fun TabItem(
  tab: RootTab,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val ember = ClanWorldTheme.colors.ember
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

  // Smooth tint crossfade on selection. The actual halo is drawn ONCE
  // by the parent at an animated offset — this composable never moves.
  val tint by animateColorAsState(
    targetValue = if (selected) ember else warmFaint,
    animationSpec = tween(220, easing = FastOutSlowInEasing),
    label = "tab-tint",
  )

  Column(
    modifier = modifier.clickable(role = Role.Tab) {
      // Lighter tick than the EmberCta LongPress — tabs are navigation,
      // not commit-style actions.
      if (!selected) {
        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
      }
      onClick()
    },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    Icon(
      painter = painterResource(tab.drawableRes),
      contentDescription = tab.label,
      tint = tint,
      modifier = Modifier.size(20.dp),
    )
    Text(
      text = tab.label.uppercase(),
      color = tint,
      style = ClanWorldTheme.type.monoNano,
    )
  }
}
