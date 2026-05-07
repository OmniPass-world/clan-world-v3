package world.clan.app.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import world.clan.app.data.elderById
import world.clan.app.ui.theme.CockpitFonts

/**
 * Pill-shaped toggle anchored bottom-center over the world map. Mirrors
 * the web `CollapseToggle` in MobileCockpitLayout.tsx: 28dp tall,
 * rgba(10,10,10,0.72) background with a subtle iron border, glyph
 * (▲ collapsed / ▼ expanded) + label "EXPAND"/"COLLAPSE".
 *
 * Color tracks the active clan accent so the chrome breathes with the
 * currently-selected clan — same as the web.
 */
@Composable
fun CollapseToggle(
  collapsed: Boolean,
  activeClanId: Int,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val accent = elderById(activeClanId).accent
  val glyph = if (collapsed) "▲" else "▼"
  val label = if (collapsed) "EXPAND" else "COLLAPSE"
  val pillBg = Color(0xB80A0A0A) // 0.72 alpha black
  val pillBorder = Color(0xFF3A3528).copy(alpha = 0.85f)

  Row(
    modifier = modifier
      .padding(bottom = 8.dp)
      .height(28.dp)
      .widthIn(min = 96.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(pillBg)
      .border(1.dp, pillBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onToggle)
      .padding(horizontal = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
  ) {
    Text(
      text = glyph,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        color = accent,
      ),
    )
    Text(
      text = label,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = accent,
        letterSpacing = 0.88.sp, // 0.08em ≈ 11 * 0.08
      ),
    )
  }
}
