package world.clan.app.owner

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Wax-seal sign modal — shown while MWA hand-off is in flight. Mirrors
 * SignSealModal.tsx: radial scrim, central seal that springs in like a
 * stamp, two rotating rune rings, "SIGN MESSAGE" caption.
 *
 * No close affordance: the overlay dismisses when the parent's signing
 * coroutine resolves.
 */
@Composable
fun SignSealOverlay(
  visible: Boolean,
  sigil: String,
  modifier: Modifier = Modifier,
) {
  if (!visible) return

  // Spring-stamp scale on enter.
  var stamped by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) { stamped = true }
  val sealScale by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (stamped) 1f else 2.2f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "sealScale",
  )

  val transition = rememberInfiniteTransition(label = "ringSpin")
  val outerSpin by transition.animateFloat(
    initialValue = 0f, targetValue = 360f,
    animationSpec = infiniteRepeatable(tween(22_000, easing = LinearEasing), RepeatMode.Restart),
    label = "outer",
  )
  val innerSpin by transition.animateFloat(
    initialValue = 0f, targetValue = -360f,
    animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Restart),
    label = "inner",
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xC70D0A08), Color(0xF208070A)),
          center = Offset.Unspecified,
        ),
      ),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center,
      ) {
        // Outer ring (dashed-feel via thin border + rotation)
        Box(
          modifier = Modifier
            .size(160.dp)
            .rotate(outerSpin)
            .clip(CircleShape)
            .background(Color.Transparent)
            .let { mod ->
              mod.then(
                Modifier
                  .size(160.dp)
                  .background(Color.Transparent)
              )
            }
            .border1Circle(CockpitTokens.Rune.Deep.copy(alpha = 0.5f))
        )
        // Inner ring
        Box(
          modifier = Modifier
            .size(140.dp)
            .rotate(innerSpin)
            .border1Circle(CockpitTokens.Ember.Core.copy(alpha = 0.55f))
        )
        // Central wax seal (gradient, scales in with stamp spring)
        Box(
          modifier = Modifier
            .size(128.dp)
            .scale(sealScale)
            .clip(CircleShape)
            .background(
              brush = Brush.radialGradient(
                colors = listOf(
                  CockpitTokens.Ember.Glow,
                  CockpitTokens.Ember.Core,
                  CockpitTokens.Ember.Deep,
                ),
              ),
            )
            .border1Circle(CockpitTokens.Ember.Deep, width = 2.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = sigil,
            style = TextStyle(
              fontFamily = CockpitFonts.UncialAntiqua,
              fontSize = 46.sp,
              color = Color(0xFFFFE2C2),
            ),
          )
        }
      }

      Box(modifier = Modifier.height(28.dp))

      Text(
        text = "SIGN MESSAGE",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFE6DCCD),
          letterSpacing = 5.76.sp, // 0.32em
          textAlign = TextAlign.Center,
        ),
      )
      Box(modifier = Modifier.height(4.dp))
      Text(
        text = "to authenticate the ælder",
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 11.sp,
          color = Color(0xFFA89B85),
          textAlign = TextAlign.Center,
        ),
      )
      Box(modifier = Modifier.height(20.dp))
      Text(
        text = "awaiting wallet seal…",
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 10.sp,
          color = Color(0xFF6B5E48),
          letterSpacing = 1.2.sp,
        ),
      )
    }
  }
}

/** Tiny helper: 1dp circle border in [color]. */
private fun Modifier.border1Circle(color: Color, width: androidx.compose.ui.unit.Dp = 1.dp): Modifier =
  this.border(width = width, color = color, shape = CircleShape)
