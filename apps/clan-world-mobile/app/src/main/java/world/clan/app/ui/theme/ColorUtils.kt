package world.clan.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Lighten a color toward white by [amount] (0..1).
 * 0 = no change, 1 = full white. Uses simple per-channel blending.
 */
fun Color.lighten(amount: Float): Color {
  val a = amount.coerceIn(0f, 1f)
  return Color(
    red   = red   + (1f - red)   * a,
    green = green + (1f - green) * a,
    blue  = blue  + (1f - blue)  * a,
    alpha = alpha,
  )
}

/**
 * Darken a color toward black by [amount] (0..1).
 * 0 = no change, 1 = full black.
 */
fun Color.darken(amount: Float): Color {
  val a = amount.coerceIn(0f, 1f)
  val factor = 1f - a
  return Color(
    red   = red   * factor,
    green = green * factor,
    blue  = blue  * factor,
    alpha = alpha,
  )
}

/** Returns a color with the alpha multiplied by [factor]. */
fun Color.fade(factor: Float): Color = copy(alpha = (alpha * factor).coerceIn(0f, 1f))

/** Convenience: linearly mix two colors. */
fun lerp(a: Color, b: Color, t: Float): Color {
  val k = t.coerceIn(0f, 1f)
  val inv = 1f - k
  return Color(
    red   = a.red   * inv + b.red   * k,
    green = a.green * inv + b.green * k,
    blue  = a.blue  * inv + b.blue  * k,
    alpha = a.alpha * inv + b.alpha * k,
  )
}

private fun max3(a: Float, b: Float, c: Float) = max(a, max(b, c))
private fun min3(a: Float, b: Float, c: Float) = min(a, min(b, c))
