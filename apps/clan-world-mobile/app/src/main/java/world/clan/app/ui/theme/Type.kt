@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package world.clan.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import world.clan.app.R

// All five families ship as variable-axis OFL fonts (one TTF per family,
// weight selected via FontVariation.weight). Cuts the asset count from 17
// static files to 6.

private fun cinzelFont(weight: Int) = Font(
  resId = R.font.cinzel_variable,
  weight = FontWeight(weight),
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun ebGaramondFont(weight: Int, italic: Boolean = false) = Font(
  resId = if (italic) R.font.eb_garamond_italic_variable else R.font.eb_garamond_variable,
  weight = FontWeight(weight),
  style = if (italic) FontStyle.Italic else FontStyle.Normal,
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun cormorantItalicFont(weight: Int) = Font(
  resId = R.font.cormorant_italic_variable,
  weight = FontWeight(weight),
  style = FontStyle.Italic,
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun jetbrainsMonoFont(weight: Int) = Font(
  resId = R.font.jetbrains_mono_variable,
  weight = FontWeight(weight),
  variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Cinzel = FontFamily(
  cinzelFont(400),
  cinzelFont(500),
  cinzelFont(600),
  cinzelFont(700),
)

val EBGaramond = FontFamily(
  ebGaramondFont(400),
  ebGaramondFont(500),
  ebGaramondFont(600),
  ebGaramondFont(400, italic = true),
  ebGaramondFont(500, italic = true),
)

/**
 * The "poetic voice" of the design — script italic. Cormorant Garamond is
 * shipped italic-only because the prototype uses it solely for italic prose.
 */
val CormorantItalic = FontFamily(
  cormorantItalicFont(400),
  cormorantItalicFont(500),
  cormorantItalicFont(600),
)

val JetBrainsMono = FontFamily(
  jetbrainsMonoFont(300),
  jetbrainsMonoFont(400),
  jetbrainsMonoFont(500),
  jetbrainsMonoFont(600),
)

val UncialAntiqua = FontFamily(
  Font(R.font.uncial_antiqua_regular, FontWeight.Normal),
)

/**
 * Material3 needs a Typography for ripple/widget defaults. Almost every
 * piece of UI we render reaches for [ClanWorldTypography] via CompositionLocal
 * instead — Material3 styles are kept lean and on-brand for the few system
 * widgets that consume them.
 */
val ClanWorldMaterialTypography = Typography(
  displayLarge   = TextStyle(fontFamily = Cinzel,        fontWeight = FontWeight.Medium, fontSize = 48.sp),
  displayMedium  = TextStyle(fontFamily = Cinzel,        fontWeight = FontWeight.Medium, fontSize = 22.sp),
  titleMedium    = TextStyle(fontFamily = Cinzel,        fontWeight = FontWeight.Medium, fontSize = 13.sp),
  bodyLarge      = TextStyle(fontFamily = EBGaramond,                                    fontSize = 16.sp),
  bodyMedium     = TextStyle(fontFamily = EBGaramond,                                    fontSize = 14.sp),
  labelSmall     = TextStyle(fontFamily = JetBrainsMono,                                 fontSize = 10.sp),
)
