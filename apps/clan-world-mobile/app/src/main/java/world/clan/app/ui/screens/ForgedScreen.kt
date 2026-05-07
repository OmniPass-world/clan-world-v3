package world.clan.app.ui.screens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import world.clan.app.ui.components.EmberCta
import world.clan.app.ui.components.Sigil
import world.clan.app.ui.components.bigSigilSpec
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.clanColor
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.viewmodel.clanTagline

/**
 * Celebration landing for both Forge mint + Bazaar hire flows.
 *
 * Replaces the previous "tiny Sealed ✓ then nav.popBackStack()" UX gap —
 * after a successful sign, the user lands here for a beat to actually see
 * the sigil they just bound to their wallet, then taps "Enter Your Hall"
 * to land in Hall (popUpTo Hall, inclusive=false, so back doesn't bounce
 * through the wizard).
 *
 * Visual hierarchy:
 *   [LABEL]                       — small mono ("FORGED" or "HIRED")
 *   <large rotating Sigil>        — clan-color, with breathing halo
 *   <sigil name>                  — display script
 *   <clan name>                   — italic
 *   <tagline>                     — italic, dim
 *   [Enter Your Hall]             — primary CTA
 */
@Composable
fun ForgedScreen(
  clanId: Int,
  label: String,
  sigilName: String,
  onEnterHall: () -> Unit,
) {
  val accent = clanColor(clanId)
  val transition = rememberInfiniteTransition(label = "forged-pulse")
  val haloPulse by transition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = EaseInOut),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "halo",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label.uppercase(),
        style = ClanWorldTheme.type.monoNano,
        color = accent,
      )

      Spacer(Modifier.height(20.dp))

      // Sigil with a breathing accent halo behind it.
      Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center,
      ) {
        // Halo
        Box(
          modifier = Modifier
            .size(200.dp)
            .alpha(haloPulse * 0.40f)
            .drawBehind {
              drawCircle(
                color = accent,
                radius = size.minDimension / 2f,
                blendMode = BlendMode.Plus,
              )
            },
        )
        Sigil(
          spec = bigSigilSpec(accent),
          modifier = Modifier.size(160.dp),
        )
      }

      Spacer(Modifier.height(28.dp))

      Text(
        text = sigilName.ifBlank { "the unnamed seal" },
        style = ClanWorldTheme.type.displayHero,
        color = ClanWorldTheme.colors.parchment,
        textAlign = TextAlign.Center,
      )

      Spacer(Modifier.height(6.dp))

      Text(
        text = clanDisplayName(clanId),
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warm,
        textAlign = TextAlign.Center,
      )

      Spacer(Modifier.height(2.dp))

      Text(
        text = clanTagline(clanId),
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warmDim,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 28.dp),
      )

      Spacer(Modifier.height(36.dp))

      Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        EmberCta(
          text = "Enter Your Hall",
          onClick = onEnterHall,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}
