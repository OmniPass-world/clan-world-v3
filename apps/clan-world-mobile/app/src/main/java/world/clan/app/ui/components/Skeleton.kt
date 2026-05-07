package world.clan.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Sweep a translucent gold band diagonally across the modifier's bounds,
 * looping every ~1500ms. Apply via .then(rememberShimmer()).
 *
 * The sweep is drawn ON TOP of existing content via drawWithContent, so the
 * caller renders a solid base color first and the shimmer overlays it.
 */
@Composable
fun Modifier.shimmer(): Modifier {
  val gold = ClanWorldTheme.colors.goldDim
  val transition = rememberInfiniteTransition(label = "skeleton-shimmer")
  val sweepX by transition.animateFloat(
    initialValue = -1f,
    targetValue = 2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "shimmer-x",
  )
  return this.drawWithContent {
    drawContent()
    val w = size.width
    val band = w * 0.45f
    val x0 = sweepX * w * 1.5f
    drawRect(
      brush = Brush.linearGradient(
        colors = listOf(
          Color.Transparent,
          gold.copy(alpha = 0.18f),
          Color.Transparent,
        ),
        start = Offset(x0, 0f),
        end = Offset(x0 + band, size.height),
      ),
      topLeft = Offset(0f, 0f),
      size = Size(w, size.height),
    )
  }
}

/**
 * One rectangular skeleton "bone" — solid iron2 base + gold shimmer sweep.
 * Building block for every higher-level skeleton card.
 */
@Composable
fun SkeletonBone(
  width: Dp? = null,
  height: Dp = 12.dp,
  cornerRadius: Dp = 4.dp,
  modifier: Modifier = Modifier,
) {
  val base = ClanWorldTheme.colors.iron2
  val mod = (if (width != null) modifier.width(width) else modifier.fillMaxWidth())
    .height(height)
    .clip(RoundedCornerShape(cornerRadius))
    .background(base)
    .shimmer()
  Box(modifier = mod)
}

/**
 * Hall list placeholder — mimics LetterCard shape: parchment-toned bg,
 * round seal area on the right, two text lines on the left, divider, three
 * stat columns. Renders four of these stacked when called from HallScreen.
 */
@Composable
fun SkeletonLetterCard(modifier: Modifier = Modifier) {
  val parchmentTint = ClanWorldTheme.colors.parchmentShade.copy(alpha = 0.10f)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(parchmentTint)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      verticalAlignment = androidx.compose.ui.Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SkeletonBone(height = 18.dp)
        SkeletonBone(height = 10.dp, width = 140.dp)
      }
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(ClanWorldTheme.colors.iron2)
          .shimmer(),
      )
    }
    SkeletonBone(height = 12.dp)
    SkeletonBone(height = 12.dp, width = 200.dp)
    Spacer(Modifier.height(2.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
      repeat(3) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          SkeletonBone(height = 8.dp, width = 50.dp)
          SkeletonBone(height = 12.dp)
        }
      }
    }
  }
}

/**
 * WhisperRow placeholder — left-edge accent line, meta + body bones.
 */
@Composable
fun SkeletonWhisperRow(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 12.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier = Modifier
        .width(2.dp)
        .height(46.dp)
        .background(ClanWorldTheme.colors.runeDim),
    )
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      SkeletonBone(height = 8.dp, width = 110.dp)
      SkeletonBone(height = 14.dp)
      SkeletonBone(height = 14.dp, width = 220.dp)
    }
  }
}

/**
 * Treasury hero placeholder — gold value bone + clan dot.
 */
@Composable
fun SkeletonTreasuryHero(modifier: Modifier = Modifier) {
  val parchmentTint = ClanWorldTheme.colors.parchmentShade.copy(alpha = 0.10f)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(parchmentTint)
      .padding(horizontal = 16.dp, vertical = 18.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      SkeletonBone(height = 8.dp, width = 50.dp)
      SkeletonBone(height = 28.dp, width = 160.dp)
      SkeletonBone(height = 10.dp, width = 200.dp)
    }
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(ClanWorldTheme.colors.iron2)
        .shimmer(),
    )
  }
}

/**
 * Treasury movement row placeholder — tick label + resource + amount.
 */
@Composable
fun SkeletonMovementRow(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
  ) {
    SkeletonBone(height = 8.dp, width = 36.dp)
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      SkeletonBone(height = 12.dp, width = 80.dp)
      SkeletonBone(height = 8.dp, width = 50.dp)
    }
    SkeletonBone(height = 14.dp, width = 60.dp)
  }
}

/**
 * Leaderboard row placeholder — rank + name + tagline + value.
 */
@Composable
fun SkeletonLeaderboardRow(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
  ) {
    SkeletonBone(height = 14.dp, width = 24.dp)
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      SkeletonBone(height = 14.dp, width = 140.dp)
      SkeletonBone(height = 8.dp, width = 90.dp)
    }
    SkeletonBone(height = 14.dp, width = 64.dp)
  }
}
