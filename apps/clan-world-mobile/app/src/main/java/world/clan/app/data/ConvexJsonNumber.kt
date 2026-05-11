package world.clan.app.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Convex's JSON output renders ALL numeric fields as floats — e.g. an
 * integer-typed `bandit.id` arrives as `1.0`, not `1`. kotlinx.serialization
 * rejects that when the Kotlin field is `Int`/`Long`, surfacing the v2.4.0
 * "Could not decode getSnapshot:getSnapshot response" crash on the Hearth
 * home screen (issue #229).
 *
 * Walking the parsed JsonElement tree once before `decodeFromJsonElement`
 * rewrites every whole-number Double primitive (e.g. `1.0` → `1`) so the
 * default `Long`/`Int` decoders accept it. Fractional values are passed
 * through untouched so `Double`-typed fields still decode correctly.
 *
 * The matcher is intentionally string-based — going through Double would
 * silently collapse 64-bit integers > 2^53 (e.g. timestamps in
 * nanoseconds) since `asLong.toDouble() == d` is still true after the
 * binary64 precision floor. We only rewrite tokens of the literal shape
 * `<digits>.0` (optionally signed), which is exactly the shape Convex
 * emits for v.number()-typed integer columns.
 */
internal fun JsonElement.normalizeConvexNumbers(): JsonElement = when (this) {
  is JsonObject -> JsonObject(mapValues { (_, v) -> v.normalizeConvexNumbers() })
  is JsonArray -> JsonArray(map { it.normalizeConvexNumbers() })
  is JsonPrimitive -> if (isString) this else coerceWholeDouble(this)
}

private val WHOLE_DOUBLE = Regex("""^-?\d+\.0+$""")

private fun coerceWholeDouble(p: JsonPrimitive): JsonPrimitive {
  val s = p.content
  // Bare integers, exponents, NaN/Infinity, fractional digits — all left alone.
  if (!WHOLE_DOUBLE.matches(s)) return p
  // Strip the trailing `.0`(s) and let the standard Long parser bound-check.
  // Outside Long range → fall back to the original token so a Double-typed
  // field can still decode it.
  val asLong = s.substringBefore('.').toLongOrNull() ?: return p
  return JsonPrimitive(asLong)
}
