package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * A single memory entry in the iNFT detail Memory tab.
 * prototype: .mem-row (lines 1064–1098)
 *
 * Body is an AnnotatedString to allow inline `code` chunks (rendered with
 * a JetBrains Mono span, gold color, faint background).
 */
@Composable
fun MemoryRow(
  key: String,
  body: AnnotatedString,
  stamp: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .background(ClanWorldTheme.colors.iron)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    Text(
      text = key.uppercase(),
      style = ClanWorldTheme.type.monoMicro, // 10sp, 0.32em uppercase
      color = ClanWorldTheme.colors.rune,
    )
    Text(
      text = body,
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warm,
    )
    Text(
      text = stamp,
      style = ClanWorldTheme.type.monoNano,
      color = ClanWorldTheme.colors.warmFaint,
      modifier = Modifier.padding(top = 3.dp),
    )
  }
}
