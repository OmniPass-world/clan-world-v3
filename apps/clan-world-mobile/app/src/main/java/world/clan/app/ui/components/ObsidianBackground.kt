package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import world.clan.app.R
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Screen background — solid obsidian + three subtle radial gradients
 * (ember up top, rune in the bottom-right, gold at lower-left), and a
 * faint warm grain overlay tiled in `BlendMode.Overlay`.
 *
 * Mirrors the prototype `body` background-image triple-radial + the
 * `body::before` SVG noise overlay.
 *
 * Use as the bottom of every Scaffold's content stack.
 */
@Composable
fun ObsidianBackground(
  modifier: Modifier = Modifier,
  emberAlpha: Float = 0.06f,
  runeAlpha: Float = 0.04f,
  goldAlpha: Float = 0.03f,
  grainAlpha: Float = 0.7f,
) {
  val obsidian = ClanWorldTheme.colors.obsidian
  val ember = ClanWorldTheme.colors.ember
  val rune = ClanWorldTheme.colors.rune
  val gold = ClanWorldTheme.colors.gold

  val grain = ImageBitmap.imageResource(id = R.drawable.obsidian_grain)

  Box(
    modifier
      .fillMaxSize()
      .background(obsidian)
      .drawBehind {
        // Ember radial — top-center, bleeding ~50% down
        drawCircle(
          brush = Brush.radialGradient(
            0f to ember.copy(alpha = emberAlpha),
            1f to Color.Transparent,
            center = Offset(size.width * 0.5f, -size.height * 0.05f),
            radius = size.width * 0.7f,
          ),
          radius = size.width * 0.7f,
          center = Offset(size.width * 0.5f, -size.height * 0.05f),
        )
        // Rune radial — bottom-right
        drawCircle(
          brush = Brush.radialGradient(
            0f to rune.copy(alpha = runeAlpha),
            1f to Color.Transparent,
            center = Offset(size.width * 1.0f, size.height * 1.0f),
            radius = size.width * 0.6f,
          ),
          radius = size.width * 0.6f,
          center = Offset(size.width * 1.0f, size.height * 1.0f),
        )
        // Gold radial — lower-left
        drawCircle(
          brush = Brush.radialGradient(
            0f to gold.copy(alpha = goldAlpha),
            1f to Color.Transparent,
            center = Offset(0f, size.height * 0.85f),
            radius = size.width * 0.5f,
          ),
          radius = size.width * 0.5f,
          center = Offset(0f, size.height * 0.85f),
        )
      }
      .drawWithContent {
        drawContent()
        // Tile the obsidian grain with the alpha already baked in.
        val gw = grain.width.toFloat()
        val gh = grain.height.toFloat()
        var y = 0f
        while (y < size.height) {
          var x = 0f
          while (x < size.width) {
            withTransform({ translate(left = x, top = y) }) {
              drawImage(grain, alpha = grainAlpha)
            }
            x += gw
          }
          y += gh
        }
      },
  )
}
