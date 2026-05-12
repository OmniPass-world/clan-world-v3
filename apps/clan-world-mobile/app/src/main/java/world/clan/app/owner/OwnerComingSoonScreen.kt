package world.clan.app.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.data.elderById
import world.clan.app.owner.shared.BackChevron
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Post-sign-in placeholder. Per the plan, the existing `/agents/<id>`
 * AgentControlPage is being redesigned; we render a clan-themed
 * "Coming Soon" page in the meantime so signed-in flow has somewhere
 * to land.
 */
@Composable
fun OwnerComingSoonScreen(
  clanId: Int,
  onBack: () -> Unit,
) {
  val elder = remember(clanId) { elderById(clanId) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Iron),
    // Status-bar + horizontal-system-bar safe inset is applied at the
    // Scaffold level in ClanWorldApp.kt; re-applying it here stacked
    // the inset and shifted content down by an extra status-bar height
    // (super-swarm v2.6.0 HIGH from codex 5.5). No interactive controls
    // live at the bottom of this screen so we let content fill behind
    // the nav bar / gesture handle.
  ) {
    // 2dp top accent line (matches MiniCockpit panel chrome)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(2.dp)
        .background(elder.accent),
    )

    BackChevron(onBack = onBack, modifier = Modifier.padding(start = 8.dp, top = 12.dp))

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = elder.glyph,
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 80.sp,
          color = elder.accent,
        ),
      )
      Box(modifier = Modifier.height(12.dp))
      Text(
        text = elder.name.uppercase(),
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = CockpitTokens.TextC.OnIron,
          letterSpacing = 5.04.sp, // 0.18em
        ),
        textAlign = TextAlign.Center,
      )
      Box(modifier = Modifier.height(4.dp))
      Text(
        text = "OWNER CONTROL",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 12.sp,
          color = CockpitTokens.TextC.OnIronDim,
          letterSpacing = 3.84.sp, // 0.32em
        ),
        textAlign = TextAlign.Center,
      )
      Box(modifier = Modifier.height(40.dp))
      Text(
        text = "Coming Soon",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.Accent,
          letterSpacing = 2.2.sp, // 0.10em
        ),
        textAlign = TextAlign.Center,
      )
    }
  }
}

