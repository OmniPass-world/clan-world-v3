package world.clan.app.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ember
import world.clan.app.ui.theme.EmberGlow
import world.clan.app.ui.theme.EmberHover
import world.clan.app.ui.theme.GoldBright
import world.clan.app.ui.theme.GoldDim

/** A single ring in a sigil. */
data class RingSpec(
  /** Radius as fraction of half the box (0..1). */
  val radiusFraction: Float,
  /** Stroke width in dp. */
  val strokeWidthDp: Float,
  /** Stroke color. */
  val color: Color,
  /** Optional dash (on, off) in dp. Null = solid. */
  val dash: Pair<Float, Float>? = null,
  /** Optional rotation period in milliseconds. Positive = clockwise; negative = ccw; null = static. */
  val rotationPeriodMs: Int? = null,
  /** If true, draw 4 cardinal tick marks just outside the ring. */
  val tickMarks: Boolean = false,
)

/** A complete sigil definition. */
data class SigilSpec(
  val rings: List<RingSpec>,
  val coreColor: Color,
  /** True if a small filled core dot should be drawn at center. */
  val showCore: Boolean = true,
  /** True to render the breathing ember halo behind everything. */
  val withHalo: Boolean = true,
)

/**
 * Render a sigil — concentric rings (some rotating), an optional breathing
 * halo, an optional small filled core, and an inscribed "tide-blade" glyph
 * (a thin vertical staff with two arcs).
 *
 * The glyph is rendered in [SigilSpec.coreColor] and pinned (non-rotating).
 *
 * prototype: Connect rings (lines 1473–1509) + #big-sigil (lines 1396–1412)
 */
@Composable
fun Sigil(
  spec: SigilSpec,
  modifier: Modifier = Modifier,
  animated: Boolean = true,
) {
  val transition = rememberInfiniteTransition(label = "sigil")

  // Pre-compute one rotation animation per ring (those that have a period).
  val rotations = spec.rings.map { ring ->
    if (animated && ring.rotationPeriodMs != null) {
      transition.animateFloat(
        initialValue = 0f,
        targetValue = if (ring.rotationPeriodMs > 0) 360f else -360f,
        animationSpec = infiniteRepeatable(
          animation = tween(ring.rotationPeriodMs.absoluteValue, easing = LinearEasing),
        ),
        label = "ring-${ring.radiusFraction}",
      )
    } else null
  }

  // Halo breath — slow scale + alpha modulation (matches @keyframes emberPulse).
  val halo by transition.animateFloat(
    initialValue = if (spec.withHalo && animated) 0.55f else 1f,
    targetValue = if (spec.withHalo && animated) 1f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(6000, easing = EaseInOut),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "halo",
  )

  Box(modifier) {
    // Ember halo — drawn first, sits underneath everything else.
    if (spec.withHalo) {
      Canvas(
        Modifier
          .fillMaxSize()
          .scale(0.95f + halo * 0.10f),
      ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.7f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              EmberGlow.copy(alpha = 0.55f * halo),
              Color.Transparent,
            ),
            center = Offset(cx, cy),
            radius = r,
          ),
          radius = r,
          center = Offset(cx, cy),
        )
      }
    }

    // Concentric rings + tick marks.
    Canvas(Modifier.fillMaxSize()) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val half = size.minDimension / 2f

      spec.rings.forEachIndexed { i, ring ->
        val angle = rotations[i]?.value ?: 0f
        val radius = half * ring.radiusFraction
        val stroke = Stroke(
          width = ring.strokeWidthDp.dp.toPx(),
          pathEffect = ring.dash?.let {
            PathEffect.dashPathEffect(
              floatArrayOf(it.first.dp.toPx(), it.second.dp.toPx()),
            )
          },
        )
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
          drawCircle(
            color = ring.color,
            radius = radius,
            center = Offset(cx, cy),
            style = stroke,
          )
          if (ring.tickMarks) {
            // 4 cardinal tick marks just outside the ring.
            val tickInner = radius + 1.dp.toPx()
            val tickOuter = radius + 5.dp.toPx()
            for (cardinal in 0 until 4) {
              val a = cardinal * 90f
              val rad = Math.toRadians(a.toDouble()).toFloat()
              val cosA = cos(rad)
              val sinA = sin(rad)
              drawLine(
                color = ring.color,
                start = Offset(cx + tickInner * cosA, cy + tickInner * sinA),
                end = Offset(cx + tickOuter * cosA, cy + tickOuter * sinA),
                strokeWidth = ring.strokeWidthDp.dp.toPx(),
              )
            }
          }
        }
      }

      // Central glyph — tide-blade: vertical staff + two crescent arcs.
      // Pinned (does not rotate). Drawn in coreColor.
      val glyphR = half * 0.20f
      drawLine(
        color = spec.coreColor,
        start = Offset(cx, cy - glyphR * 1.6f),
        end = Offset(cx, cy + glyphR * 1.6f),
        strokeWidth = 0.9.dp.toPx(),
      )
      // Two crescent arcs (top and bottom): rendered as Bezier-ish curves
      // approximated with quarter-circle strokes.
      val arcR = glyphR * 0.7f
      drawArc(
        color = spec.coreColor,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - arcR, cy - glyphR * 1.5f - arcR / 2f),
        size = androidx.compose.ui.geometry.Size(arcR * 2f, arcR),
        style = Stroke(width = 0.9.dp.toPx()),
      )
      drawArc(
        color = spec.coreColor,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - arcR, cy + glyphR * 1.5f - arcR / 2f),
        size = androidx.compose.ui.geometry.Size(arcR * 2f, arcR),
        style = Stroke(width = 0.9.dp.toPx()),
      )

      // Filled core dot.
      if (spec.showCore) {
        drawCircle(
          color = spec.coreColor,
          radius = 3.dp.toPx(),
          center = Offset(cx, cy),
        )
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────
// Sigil presets
// ─────────────────────────────────────────────────────────────────────────

/**
 * Connect-screen sigil — 5 rotating rings + breathing halo. Matches
 * prototype lines 1473–1509.
 */
val ConnectSigilSpec: SigilSpec
  get() = SigilSpec(
    rings = listOf(
      RingSpec(0.96f, 0.4f, GoldDim,                    rotationPeriodMs = 120_000, tickMarks = true),
      RingSpec(0.72f, 0.5f, EmberHover.copy(alpha = 0.55f), dash = 1f to 1.6f, rotationPeriodMs = -90_000),
      RingSpec(0.60f, 0.4f, Ember.copy(alpha = 0.35f),  rotationPeriodMs = -90_000),
      RingSpec(0.44f, 0.7f, GoldBright.copy(alpha = 0.85f), rotationPeriodMs = 60_000),
      RingSpec(0.28f, 0.4f, GoldBright.copy(alpha = 0.85f), dash = 2f to 2f, rotationPeriodMs = 60_000),
    ),
    coreColor = Ember,
    showCore = true,
    withHalo = true,
  )

/**
 * iNFT Detail hero sigil — 4 static concentric rings, no halo.
 * Color is the clan's accent. Matches prototype #big-sigil (lines 1396–1412).
 */
fun bigSigilSpec(clanColor: Color): SigilSpec = SigilSpec(
  rings = listOf(
    RingSpec(0.92f, 0.6f, clanColor.copy(alpha = 0.6f)),
    RingSpec(0.76f, 0.4f, clanColor.copy(alpha = 0.5f), dash = 0.8f to 2f),
    RingSpec(0.56f, 0.6f, clanColor),
    RingSpec(0.40f, 0.4f, clanColor.copy(alpha = 0.7f), dash = 2f to 1.5f),
  ),
  coreColor = clanColor,
  showCore = true,
  withHalo = false,
)
