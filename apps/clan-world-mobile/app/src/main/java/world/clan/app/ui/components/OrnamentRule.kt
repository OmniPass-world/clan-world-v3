package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.fade

/**
 * Hairline rule with one or more lozenge accents in the center.
 * prototype: .ornament-rule (lines 387–400)
 */
@Composable
fun OrnamentRule(
  modifier: Modifier = Modifier,
  lozengeCount: Int = 1,
  color: Color = ClanWorldTheme.colors.gold,
  lineWidth: Dp = 42.dp,
  lineAlpha: Float = 0.6f,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(
      Modifier
        .width(lineWidth)
        .height(1.dp)
        .background(color.fade(lineAlpha)),
    )
    repeat(lozengeCount) {
      Box(
        Modifier
          .size(6.dp)
          .rotate(45f)
          .background(color),
      )
    }
    Box(
      Modifier
        .width(lineWidth)
        .height(1.dp)
        .background(color.fade(lineAlpha)),
    )
  }
}

/** Single 5dp lozenge — used in section headers. */
@Composable
fun Lozenge(
  modifier: Modifier = Modifier,
  size: Dp = 5.dp,
  color: Color = ClanWorldTheme.colors.gold,
) {
  Box(
    modifier
      .size(size)
      .rotate(45f)
      .background(color),
  )
}
