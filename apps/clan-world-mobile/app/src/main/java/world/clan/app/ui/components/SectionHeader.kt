package world.clan.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Lozenge + small-caps Cinzel title + gradient hairline + optional right meta.
 * prototype: .section-head (lines 645–671)
 *
 * @param showLozenge defaults to true. The Codex screen variant uses false
 * to match the prototype `.codex-section h3` rule (no leading lozenge).
 */
@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  meta: String? = null,
  color: Color = ClanWorldTheme.colors.gold,
  showLozenge: Boolean = true,
) {
  val hairline = ClanWorldTheme.colors.hairlineStrong
  Row(
    modifier = modifier.padding(top = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (showLozenge) {
      Lozenge(color = color)
    }
    Text(
      text = title.uppercase(),
      style = ClanWorldTheme.type.sectionHead,
      color = color,
    )
    Spacer(
      Modifier
        .weight(1f)
        .height(1.dp)
        .drawBehind {
          drawRect(
            brush = Brush.horizontalGradient(
              colors = listOf(hairline, Color.Transparent),
            ),
          )
        },
    )
    if (meta != null) {
      Text(
        text = meta,
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.warmFaint,
      )
    }
  }
}
