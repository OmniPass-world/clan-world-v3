package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.clan.app.R
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink

/**
 * Parchment letter surface — used by Hall letter cards and the iNFT Detail
 * hero card. Composition mirrors prototype `.letter` (lines 805–837) and
 * `.hero` (lines 938–959):
 *
 *   1. 135° linear gradient Parchment → Parchment2 → ParchmentShade  (base)
 *   2. Tiled grain texture (R.drawable.parchment_grain)              (under content)
 *   3. Deckled 6dp left edge — vertical-gradient strip + 1dp shadow  (under content)
 *   4. Children                                                      (the writing on the page)
 *   5. Inset 1dp border, Ink @ 0.10 alpha                            (over content, subtle)
 *   6. Outer 12dp drop shadow                                        (around the card)
 */
@Composable
fun ParchmentCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 6.dp,
  contentPadding: PaddingValues = PaddingValues(start = 22.dp, top = 18.dp, end = 18.dp, bottom = 16.dp),
  content: @Composable ColumnScope.() -> Unit,
) {
  val shape = RoundedCornerShape(cornerRadius)

  val parchment = ClanWorldTheme.colors.parchment
  val parchment2 = ClanWorldTheme.colors.parchment2
  val parchmentShade = ClanWorldTheme.colors.parchmentShade
  val ink = Ink

  // 135° gradient — top-left to bottom-right.
  val parchmentBrush = Brush.linearGradient(
    colors = listOf(parchment, parchment2, parchmentShade),
    start = Offset.Zero,
    end = Offset.Infinite,
  )

  val grain = ImageBitmap.imageResource(id = R.drawable.parchment_grain)

  Box(
    modifier
      .shadow(elevation = 12.dp, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
      .clip(shape)
      .background(parchmentBrush)
      // (2) Grain + (3) Deckled edge, both drawn under content.
      .drawBehind {
        // Tile the parchment grain (alpha pre-baked).
        val gw = grain.width.toFloat()
        val gh = grain.height.toFloat()
        var y = 0f
        while (y < size.height) {
          var x = 0f
          while (x < size.width) {
            withTransform({ translate(left = x, top = y) }) {
              drawImage(grain, alpha = 0.6f)
            }
            x += gw
          }
          y += gh
        }
        // Deckled left edge — 6dp vertical-gradient strip.
        val edgeW = 6.dp.toPx()
        drawRect(
          brush = Brush.verticalGradient(
            0f to parchmentShade,
            1f to parchment2,
          ),
          topLeft = Offset.Zero,
          size = Size(edgeW, size.height),
        )
        // 1px inner shadow line at the strip's right edge.
        drawLine(
          color = ink.copy(alpha = 0.18f),
          start = Offset(edgeW, 0f),
          end = Offset(edgeW, size.height),
          strokeWidth = 1f,
        )
      }
      // (5) Inset border, drawn after content.
      .drawWithContent {
        drawContent()
        val r = (cornerRadius.toPx() - 1f).coerceAtLeast(0f)
        drawRoundRect(
          color = ink.copy(alpha = 0.10f),
          topLeft = Offset(1f, 1f),
          size = Size(size.width - 2f, size.height - 2f),
          cornerRadius = CornerRadius(r),
          style = Stroke(width = 1f),
        )
      },
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding),
      content = content,
    )
  }
}
