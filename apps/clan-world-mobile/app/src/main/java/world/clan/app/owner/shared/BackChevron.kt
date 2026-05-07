package world.clan.app.owner.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Top-left back affordance used by both Owner screens. 36dp circular
 * chip, semi-opaque black, faint iron-tone border, Cinzel chevron.
 * System back works too — this is just a visible touch target.
 */
@Composable
fun BackChevron(onBack: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(Color(0x8B000000))
      .border(1.dp, CockpitTokens.TextC.OnIronDim.copy(alpha = 0.6f), CircleShape)
      .clickable(onClick = onBack),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "‹",
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 22.sp,
        color = CockpitTokens.TextC.OnIron,
        fontWeight = FontWeight.Bold,
      ),
    )
  }
}
