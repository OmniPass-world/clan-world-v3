package world.clan.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.ClanWorldTheme

/**
 * Balance + faucet row. GOLD line on the left, a small cyan-rune dashed-
 * outline button on the right that mints DEVNET-only test gold. The
 * DEVNET label is underline-styled in cyan — impossible to confuse with
 * a real economic action.
 *
 * Mirrors web PR #51's `apps/web/src/pages/agent/BalanceRow.tsx`.
 */
@Composable
fun BalanceRow(
  gold: Long,
  bouncing: Boolean,
  faucetCooling: Boolean,
  faucetDrop: Long,
  onFaucet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Bounce animation on faucet drop.
  val scale = remember { Animatable(1f) }
  LaunchedEffect(bouncing) {
    if (bouncing) {
      scale.animateTo(1.12f, tween(150))
      scale.animateTo(1f, tween(350))
    }
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      modifier = Modifier.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
      },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "◈",
        style = ClanWorldTheme.type.body.copy(fontSize = 14.sp),
        color = ClanWorldTheme.colors.goldBright,
      )
      Text(
        text = "GOLD",
        style = ClanWorldTheme.type.eyebrow.copy(fontSize = 11.sp),
        color = ClanWorldTheme.colors.warmDim,
      )
      Text(
        text = "·",
        style = ClanWorldTheme.type.eyebrow,
        color = ClanWorldTheme.colors.warmFaint,
      )
      Text(
        text = "${gold.formatThousands()} g",
        style = ClanWorldTheme.type.monoData.copy(fontSize = 14.sp),
        color = ClanWorldTheme.colors.goldBright,
      )
    }

    DevnetFaucetButton(
      faucetDrop = faucetDrop,
      cooling = faucetCooling,
      onClick = onFaucet,
    )
  }
}

@Composable
private fun DevnetFaucetButton(
  faucetDrop: Long,
  cooling: Boolean,
  onClick: () -> Unit,
) {
  val rune = ClanWorldTheme.colors.rune
  val runeGlow = ClanWorldTheme.colors.runeGlow
  val runeDim = ClanWorldTheme.colors.runeDim
  val labelColor = if (cooling) runeDim else rune
  val accentColor = if (cooling) runeDim else runeGlow

  val shape = RoundedCornerShape(4.dp)
  val borderColor = if (cooling) runeDim else rune.copy(alpha = 0.65f)

  Column(
    modifier = Modifier
      .clip(shape)
      .border(BorderStroke(1.dp, borderColor), shape)
      .clickable(enabled = !cooling) { onClick() }
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.End,
  ) {
    Text(
      text = "+ MINT ${(faucetDrop / 1000L)}K",
      style = ClanWorldTheme.type.eyebrow.copy(fontSize = 10.sp),
      color = labelColor,
    )
    Text(
      text = "DEVNET ONLY",
      style = ClanWorldTheme.type.eyebrow.copy(fontSize = 8.sp),
      color = accentColor,
    )
  }
}

private fun Long.formatThousands(): String {
  val s = toString()
  return s.reversed().chunked(3).joinToString(",").reversed()
}
