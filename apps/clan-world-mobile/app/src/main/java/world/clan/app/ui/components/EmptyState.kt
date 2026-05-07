package world.clan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Inviting empty state. Renders centered title + body italics, plus an
 * optional outlined-pill CTA at the bottom. Use when a list is genuinely
 * empty (no error, just nothing yet) and there's a sensible next action.
 *
 * Distinguished from RetryNotice (which is for errors): this is for
 * "all good, nothing here yet" rather than "something went wrong".
 */
@Composable
fun EmptyState(
  title: String,
  body: String,
  ctaLabel: String? = null,
  onCta: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim
  val parchment = ClanWorldTheme.colors.parchment
  val gold = ClanWorldTheme.colors.gold
  val hairlineMid = ClanWorldTheme.colors.hairlineMid
  val iron2 = ClanWorldTheme.colors.iron2

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp, vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = title,
      style = ClanWorldTheme.type.scriptItalic,
      color = warm,
      textAlign = TextAlign.Center,
    )
    Text(
      text = body,
      style = ClanWorldTheme.type.scriptItalic,
      color = warmDim,
      textAlign = TextAlign.Center,
    )
    if (ctaLabel != null && onCta != null) {
      Spacer(Modifier.height(6.dp))
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(iron2)
          .drawBehind {
            drawRoundRect(
              color = hairlineMid,
              cornerRadius = CornerRadius(4.dp.toPx()),
              style = Stroke(width = 1.dp.toPx()),
            )
          }
          .clickable { onCta() }
          .padding(horizontal = 18.dp, vertical = 10.dp),
      ) {
        Text(
          text = ctaLabel.uppercase(),
          style = ClanWorldTheme.type.monoMicro,
          color = gold,
        )
      }
    }
  }
}
