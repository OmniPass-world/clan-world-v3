package world.clan.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.darken
import world.clan.app.ui.theme.lighten

/**
 * Wax seal — a circular wax-stamp impression used on Hall letter cards.
 *
 * prototype: .seal (lines 876–895). Layered effects:
 *   - Drop shadow (offset 4dp)
 *   - Radial gradient: lighten(0.18) → darken(0.30) → darken(0.55)
 *     centered at (35%, 30%) for a 3D wax look
 *   - Dashed inner ring (white @ 0.32 alpha)
 *   - Inset top highlight (white @ 0.18) + bottom shadow (black @ 0.35)
 *   - Glyph at center, white tint
 *   - Slight rotation (-6° default)
 */
@Composable
fun WaxSeal(
  glyph: Painter,
  clanColor: Color,
  modifier: Modifier = Modifier,
  size: Dp = 54.dp,
  rotationDeg: Float = -6f,
) {
  Box(
    modifier
      .size(size)
      .drawBehind {
        // Drop shadow under the seal — rendered before the rotation transform
        drawCircle(
          color = Color.Black.copy(alpha = 0.4f),
          radius = (size.toPx() / 2f),
          center = Offset(this.size.width / 2f, this.size.height / 2f + 4.dp.toPx()),
        )
      }
      .rotate(rotationDeg)
      .clip(CircleShape)
      .drawWithCache {
        val r = size.toPx() / 2f
        // Center the radial highlight at (35%, 30%) of the box, like the
        // CSS background-position for the gradient.
        val centerOff = Offset(this.size.width * 0.35f, this.size.height * 0.30f)
        val gradient = Brush.radialGradient(
          colors = listOf(
            clanColor.lighten(0.18f),
            clanColor.darken(0.30f),
            clanColor.darken(0.55f),
          ),
          center = centerOff,
          radius = r * 1.1f,
        )
        val stroke = 1.5.dp.toPx()
        val intervals = floatArrayOf(2.dp.toPx(), 2.dp.toPx())

        onDrawWithContent {
          // 1) The wax pour itself
          drawRect(brush = gradient)
          // 2) Inset top highlight (a 3dp white stroke offset upward)
          drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = r,
            center = Offset(this.size.width / 2f, this.size.height / 2f - 2.dp.toPx()),
            style = Stroke(width = 3.dp.toPx()),
          )
          // 3) Inset bottom shadow (a 3dp black stroke offset downward)
          drawCircle(
            color = Color.Black.copy(alpha = 0.35f),
            radius = r,
            center = Offset(this.size.width / 2f, this.size.height / 2f + 2.dp.toPx()),
            style = Stroke(width = 3.dp.toPx()),
          )
          // 4) Dashed inner ring (sits 4dp inset from the edge)
          drawCircle(
            color = Color.White.copy(alpha = 0.32f),
            radius = r - 4.dp.toPx() - stroke / 2f,
            style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(intervals)),
          )
          drawContent()
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      painter = glyph,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(22.dp),
    )
  }
}
