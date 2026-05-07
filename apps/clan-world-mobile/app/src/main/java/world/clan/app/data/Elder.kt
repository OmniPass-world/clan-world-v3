package world.clan.app.data

import androidx.compose.ui.graphics.Color
import world.clan.app.ui.theme.CockpitTokens

/**
 * Elder / clan definition. Mirrors `ELDERS` in
 * apps/web/src/styles/cockpit-tokens.ts — the hardcoded Phase A roster.
 * Treat this as authoritative for the native cockpit; if the web roster
 * changes we update it here too.
 */
data class Elder(
  val clanId: Int,
  val name: String,
  val archetype: String,
  val glyph: String,
  val accent: Color,
  val rune: String = DEFAULT_RUNE,
  val oneLineEssence: String = DEFAULT_ESSENCE,
) {
  companion object {
    private const val DEFAULT_RUNE = "ᚠᚱᛟ"
    private const val DEFAULT_ESSENCE =
      "Bound by covenant of iron and ember."
  }
}

val ELDERS: List<Elder> = listOf(
  Elder(
    clanId = 1,
    name = "Storm Riders",
    archetype = "Aggressive Raider",
    glyph = "⚡",
    accent = Color(0xFF5A8AA8),
    rune = "ᚦᚱᚷ",
    oneLineEssence = "Strike before the storm passes.",
  ),
  Elder(
    clanId = 2,
    name = "Iron Guard",
    archetype = "Cautious Accumulator",
    glyph = "⛨",
    accent = Color(0xFF7A8A6A),
    rune = "ᛁᚱᚾ",
    oneLineEssence = "Hold the line; the line holds the world.",
  ),
  Elder(
    clanId = 3,
    name = "Crimson",
    archetype = "Volatile Opportunist",
    glyph = "✦",
    accent = Color(0xFFA85A5A),
    rune = "ᚲᚱᛗ",
    oneLineEssence = "What burns brightest is bought in blood.",
  ),
  Elder(
    clanId = 4,
    name = "Verdant Wardens",
    archetype = "Patient Builder",
    glyph = "❦",
    accent = Color(0xFF6AA888),
    rune = "ᚹᚱᛞ",
    oneLineEssence = "The forest does not hurry; the forest is.",
  ),
)

fun elderById(clanId: Int): Elder =
  ELDERS.firstOrNull { it.clanId == clanId }
    ?: error("Unknown clanId: $clanId")

@Suppress("unused")
fun Elder.onIronAccent(): Color = CockpitTokens.TextC.OnIron
