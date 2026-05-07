package world.clan.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/** Color of the left border for a whisper row. */
enum class WhisperAccent { Default, Rune, Gold, Ember }

/**
 * One whisper / orchestrator / steering message in the Hearth feed.
 * prototype: .whisper variants (lines 742–765)
 */
@Composable
fun WhisperRow(
  meta: AnnotatedString,
  body: String,
  accent: WhisperAccent = WhisperAccent.Rune,
  modifier: Modifier = Modifier,
) {
  val accentColor = when (accent) {
    WhisperAccent.Default -> ClanWorldTheme.colors.hairlineMid
    WhisperAccent.Rune -> ClanWorldTheme.colors.runeDim
    WhisperAccent.Gold -> ClanWorldTheme.colors.goldDim
    WhisperAccent.Ember -> ClanWorldTheme.colors.ember
  }
  val warm = ClanWorldTheme.colors.warm
  val warmFaint = ClanWorldTheme.colors.warmFaint

  Column(
    modifier
      .drawBehind {
        // 1px left border accent
        drawLine(
          color = accentColor,
          start = Offset(0f, 0f),
          end = Offset(0f, size.height),
          strokeWidth = 1.dp.toPx(),
        )
      }
      .padding(start = 12.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
    verticalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    Text(
      text = meta,
      style = ClanWorldTheme.type.monoNano,
      color = warmFaint,
    )
    Text(
      text = body,
      style = ClanWorldTheme.type.scriptItalic,
      color = warm,
    )
  }
}
