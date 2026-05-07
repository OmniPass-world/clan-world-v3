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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import world.clan.app.ui.components.Sigil
import world.clan.app.ui.components.bigSigilSpec
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.clanColor
import world.clan.app.viewmodel.clanDisplayName

/**
 * Short loader between InftDetail's "Enter Cockpit" tap and the actual
 * CockpitScreen mount. Buys ~1.4s for two reasons:
 *   1. Cockpit data fetches start from a clean state — gives them a
 *      moment to land before the screen is on-stage.
 *   2. The transition feels weighty rather than instant — matches the
 *      "the strait is preparing" copy.
 *
 * The screen is intentionally still: a single rotating Sigil with a slow
 * breathing-fade subtitle. No tap targets — backstack handles cancel.
 */
@Composable
fun BridgeScreen(
  clanId: Int,
  onReady: () -> Unit,
) {
  // Auto-advance after ~1.4s. If composition is torn down before then
  // (back-press, etc.), the LaunchedEffect cancels cleanly.
  LaunchedEffect(clanId) {
    delay(1400L)
    onReady()
  }

  val transition = rememberInfiniteTransition(label = "bridge-pulse")
  val pulse by transition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = EaseInOut),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Sigil(
        spec = bigSigilSpec(clanColor(clanId)),
        modifier = Modifier.size(160.dp),
      )

      Spacer(Modifier.height(34.dp))

      Text(
        text = "preparing the strait…",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warm,
        modifier = Modifier.alpha(pulse),
        textAlign = TextAlign.Center,
      )

      Spacer(Modifier.height(8.dp))

      Text(
        text = clanDisplayName(clanId).uppercase(),
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.gold,
        textAlign = TextAlign.Center,
      )
    }
  }
}
