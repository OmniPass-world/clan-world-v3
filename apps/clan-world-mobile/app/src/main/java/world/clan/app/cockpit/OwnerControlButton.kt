package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.data.Elder
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Floating "⟢ Owner Control" button overlaid on the Terminal panel.
 * Recreates the spec from web commit 91ed5ec:
 *   - rgba(0,0,0,0.72) bg, 1dp accent80 border, 2dp radius
 *   - 6/10dp padding, 8dp gap (between glyph and label)
 *   - Inter 11sp 700, accent fg, accent-tinted glow shadow
 */
@Composable
fun OwnerControlButton(
  elder: Elder,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val accent = elder.accent
  val borderColor = accent.copy(alpha = 0.5f)
  val pillBg = Color(0xB8000000) // 0.72 alpha black

  Row(
    modifier = modifier
      .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(CockpitTokens.Radius.sm),
        ambientColor = accent,
        spotColor = accent,
      )
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(pillBg)
      .border(1.dp, borderColor, RoundedCornerShape(CockpitTokens.Radius.sm))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = "⟢",
      style = TextStyle(
        fontFamily = CockpitFonts.Inter,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = accent,
      ),
    )
    Text(
      text = "Owner Control",
      style = TextStyle(
        fontFamily = CockpitFonts.Inter,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = accent,
        letterSpacing = 0.66.sp, // 0.06em ≈ 11 * 0.06
      ),
    )
  }
}
