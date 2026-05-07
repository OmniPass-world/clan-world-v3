package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Pull-to-refresh wrapper using Material3's [PullToRefreshBox].
 *
 * Drop-in replacement for `Box(Modifier.fillMaxSize())` around a vertically
 * scrollable child. Pulls a refresh callback from the host viewmodel.
 *
 * Notes:
 * - The Material3 indicator is themed by ColorScheme; we let it inherit
 *   from the obsidian color scheme rather than pinning. If the default
 *   indicator looks wrong, swap to a custom one via `indicator =`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableContent(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier,
  ) {
    content()
  }
}

/**
 * Error notice for data-fetch failures. Renders the message + a tap-to-retry
 * affordance. Same script-italic + danger-color palette as the other
 * screens' error texts, with an extra CTA button beneath.
 */
@Composable
fun RetryNotice(
  message: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val danger = ClanWorldTheme.colors.danger
  val iron2 = ClanWorldTheme.colors.iron2
  val hairlineMid = ClanWorldTheme.colors.hairlineMid
  val parchment = ClanWorldTheme.colors.parchment
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = message,
      style = ClanWorldTheme.type.scriptItalic,
      color = danger,
      textAlign = TextAlign.Center,
    )
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(iron2)
        .drawBehind {
          drawRoundRect(
            color = hairlineMid,
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
          )
        }
        .clickable { onRetry() }
        .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
      Text(
        text = "TAP TO RETRY",
        style = ClanWorldTheme.type.monoMicro,
        color = parchment,
      )
    }
    Spacer(Modifier.height(4.dp))
  }
}
