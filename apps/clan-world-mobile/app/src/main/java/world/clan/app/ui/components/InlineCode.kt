package world.clan.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.JetBrainsMono

/**
 * Build an italic Cormorant body string with embedded `mono code` spans.
 * Splits the input on `…` markdown-ish backticks and renders the marked
 * chunks in JetBrains Mono with a faint gold background.
 *
 *     val body = inlineCodeBody("Withdraw inland when bandits exceed `tier ii`; otherwise hold.")
 *     Text(body, style = scriptItalic, ...)
 *
 * Used by MemoryRow body text.
 */
@Composable
@ReadOnlyComposable
fun inlineCodeBody(text: String): AnnotatedString {
  val gold = ClanWorldTheme.colors.gold
  return buildAnnotatedString {
    var i = 0
    while (i < text.length) {
      val tickStart = text.indexOf('`', startIndex = i)
      if (tickStart < 0) {
        append(text.substring(i))
        break
      }
      append(text.substring(i, tickStart))
      val tickEnd = text.indexOf('`', startIndex = tickStart + 1)
      if (tickEnd < 0) {
        append(text.substring(tickStart))
        break
      }
      val codeText = text.substring(tickStart + 1, tickEnd)
      withStyle(
        SpanStyle(
          fontFamily = JetBrainsMono,
          fontWeight = FontWeight.Normal,
          fontStyle = FontStyle.Normal,
          fontSize = 12.sp,
          color = gold,
          background = gold.copy(alpha = 0.08f),
        ),
      ) {
        // Pad with NBSPs so the tinted background reads as a slim chip.
        append(' ')
        append(codeText)
        append(' ')
      }
      i = tickEnd + 1
    }
  }
}

/**
 * Convenience for whisper meta lines with bolded clan names:
 *   "<b>Tideborne</b> · whispered to · <b>Vale-Ward</b> · 0426"
 *
 * Splits on `*…*` for bold spans.
 */
@Composable
@ReadOnlyComposable
fun boldedMeta(text: String): AnnotatedString {
  val gold = ClanWorldTheme.colors.gold
  return buildAnnotatedString {
    var i = 0
    while (i < text.length) {
      val starStart = text.indexOf('*', startIndex = i)
      if (starStart < 0) {
        append(text.substring(i))
        break
      }
      append(text.substring(i, starStart))
      val starEnd = text.indexOf('*', startIndex = starStart + 1)
      if (starEnd < 0) {
        append(text.substring(starStart))
        break
      }
      val boldText = text.substring(starStart + 1, starEnd)
      withStyle(SpanStyle(color = gold, fontWeight = FontWeight.Medium)) {
        append(boldText)
      }
      i = starEnd + 1
    }
  }
}
