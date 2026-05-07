package world.clan.app.cockpit.tabs.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Section header used at the top of each parchment-tab section: small
 * Cinzel uppercase label, optional right-aligned mono trailing badge,
 * and a 1dp parchment edge divider beneath. Mirrors the recurring
 * pattern across VaultTab/ClansmanTab/ZeroGTab/CommsTab in the web app.
 */
@Composable
fun SectionHeader(label: String, trailing: String? = null) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 4.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.OnParchmentDim,
          letterSpacing = 1.98.sp, // 0.18em
        ),
      )
      if (trailing != null) {
        Text(
          text = trailing,
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            color = CockpitTokens.TextC.Muted,
            letterSpacing = 0.6.sp,
          ),
        )
      }
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(CockpitTokens.Border.ParchmentEdge),
    )
  }
}
