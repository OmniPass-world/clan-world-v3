@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package world.clan.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import world.clan.app.R

/**
 * Font families used throughout the cockpit. The bundled .ttf files are
 * variable fonts (single file, multiple weights via the wght axis), so
 * each FontFamily reuses the same resource with different FontVariation
 * settings per weight slot.
 *
 * Mapping (mirrors the web's font.* tokens):
 *   - display → Cinzel        (ritual headings, panel titles)
 *   - body    → Inter         (UI text, descriptions)
 *   - mono    → JetBrains Mono (ticks, balances, KV tables)
 *   - rune    → Uncial Antiqua (clan glyphs, sigils)
 */
object CockpitFonts {

  private fun variableFont(resourceId: Int, weight: FontWeight): Font =
    Font(
      resourceId,
      weight,
      variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight)
      )
    )

  val Cinzel: FontFamily = FontFamily(
    variableFont(R.font.cinzel_regular, FontWeight.Normal),
    variableFont(R.font.cinzel_regular, FontWeight.SemiBold),
    variableFont(R.font.cinzel_regular, FontWeight.Bold),
  )

  val Inter: FontFamily = FontFamily(
    variableFont(R.font.inter_regular, FontWeight.Normal),
    variableFont(R.font.inter_regular, FontWeight.Medium),
    variableFont(R.font.inter_regular, FontWeight.SemiBold),
    variableFont(R.font.inter_regular, FontWeight.Bold),
  )

  val JetBrainsMono: FontFamily = FontFamily(
    variableFont(R.font.jetbrains_mono_regular, FontWeight.Normal),
    variableFont(R.font.jetbrains_mono_regular, FontWeight.SemiBold),
    variableFont(R.font.jetbrains_mono_regular, FontWeight.Bold),
  )

  val UncialAntiqua: FontFamily = FontFamily(
    Font(R.font.uncial_antiqua_regular, FontWeight.Normal)
  )
}
