package world.clan.app.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

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
 */
internal fun JsonElement.normalizeConvexNumbers(): JsonElement = when (this) {
  is JsonObject -> JsonObject(mapValues { (_, v) -> v.normalizeConvexNumbers() })
  is JsonArray -> JsonArray(map { it.normalizeConvexNumbers() })
  is JsonPrimitive -> if (isString) this else coerceWholeDouble(this)
}

private fun coerceWholeDouble(p: JsonPrimitive): JsonPrimitive {
  val d = p.doubleOrNull ?: return p
  // Skip ints already-encoded as ints; only rewrite when source has a '.'.
  if ('.' !in p.content && 'e' !in p.content && 'E' !in p.content) return p
  // NaN / Infinity are not whole numbers — leave alone (will fail Long parse
  // later if a Long field is hit, but that's a true data error).
  if (!d.isFinite()) return p
  val asLong = d.toLong()
  return if (asLong.toDouble() == d) JsonPrimitive(asLong) else p
}
