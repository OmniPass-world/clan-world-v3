package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * One row of the Hearth leaderboard.
 * prototype: .lb-row (lines 680–737), with the .mine variant (line 686)
 *
 * Layout: rank | glyph | name+tagline | gold | delta — using weighted
 * widths to match the prototype's 22 / 24 / 1fr / auto / auto grid.
 */
@Composable
fun LeaderboardRow(
  rank: Int,
  clanColor: Color,
  glyph: Painter,
  clanName: String,
  clanTagline: String,
  gold: Long,
  delta: Long,
  isMine: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val ember = ClanWorldTheme.colors.ember
  val rune = ClanWorldTheme.colors.rune
  val danger = ClanWorldTheme.colors.danger
  val parchment = ClanWorldTheme.colors.parchment
  val warmFaint = ClanWorldTheme.colors.warmFaint
  val warmDim = ClanWorldTheme.colors.warmDim
  val iron = ClanWorldTheme.colors.iron

  val deltaColor = if (delta < 0) danger else rune
  val deltaText = when {
    delta == 0L -> "—"
    delta > 0L -> "+${formatGold(delta)}"
    else -> "−${formatGold(-delta)}"   // U+2212 minus sign
  }

  val background = if (isMine) {
    Modifier
      .background(
        Brush.horizontalGradient(
          0f to ember.copy(alpha = 0.13f),
          1f to ember.copy(alpha = 0.02f),
        ),
      )
      .drawBehind {
        // 2dp ember left border
        drawLine(
          color = ember,
          start = Offset(0f, 0f),
          end = Offset(0f, size.height),
          strokeWidth = 2.dp.toPx(),
        )
      }
  } else {
    Modifier.background(iron)
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .then(background)
      .padding(
        start = if (isMine) 10.dp else 12.dp,
        end = 12.dp,
        top = 9.dp,
        bottom = 9.dp,
      ),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = "%02d".format(rank),
      style = ClanWorldTheme.type.monoNano,
      color = warmFaint,
      modifier = Modifier.widthIn(min = 22.dp),
    )
    GlyphCircle(glyph = glyph, color = clanColor)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = clanName,
        style = ClanWorldTheme.type.body,
        color = parchment,
      )
      Text(
        text = clanTagline,
        style = ClanWorldTheme.type.scriptItalicSmall,
        color = warmDim,
      )
    }
    Text(
      text = formatGold(gold),
      style = ClanWorldTheme.type.monoData,
      color = ClanWorldTheme.colors.gold,
      textAlign = TextAlign.End,
    )
    Text(
      text = deltaText,
      style = ClanWorldTheme.type.monoNano,
      color = deltaColor,
      textAlign = TextAlign.End,
      modifier = Modifier.widthIn(min = 34.dp),
    )
  }
}

/**
 * Tiny circular badge with a clan-colored stroke and centered glyph.
 * Used by leaderboard rows AND the Codex profile list.
 */
@Composable
fun GlyphCircle(
  glyph: Painter,
  color: Color,
  modifier: Modifier = Modifier,
  outerSize: androidx.compose.ui.unit.Dp = 22.dp,
  innerSize: androidx.compose.ui.unit.Dp = 13.dp,
) {
  Box(
    modifier
      .size(outerSize)
      .background(Color.White.copy(alpha = 0.04f), CircleShape)
      .border(width = 1.dp, color = color, shape = CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      painter = glyph,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(innerSize),
    )
  }
}

private fun formatGold(g: Long): String {
  if (g < 1000) return g.toString()
  // Insert thousands separator (comma) — keeps the prototype's 14,820 look.
  val s = g.toString()
  val sb = StringBuilder()
  var count = 0
  for (i in s.indices.reversed()) {
    sb.append(s[i])
    count++
    if (count == 3 && i > 0) {
      sb.append(',')
      count = 0
    }
  }
  return sb.reverse().toString()
}
