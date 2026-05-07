package world.clan.app.ui.theme

import world.clan.app.R

/**
 * Map a clanId (1..8) to the vector drawable resource for that clan's
 * sigil glyph. Used by the leaderboard, wax seals, and Codex profile rows.
 */
fun clanGlyphRes(clanId: Int): Int = when (clanId) {
  1 -> R.drawable.g_blade   // Storm-Edge
  2 -> R.drawable.g_tide    // Tideborne
  3 -> R.drawable.g_crown   // Sunhold
  4 -> R.drawable.g_leaf    // Vale-Ward
  5 -> R.drawable.g_eye     // Twilight Watch
  6 -> R.drawable.g_flame   // Ember-Kin
  7 -> R.drawable.g_anvil   // Hearth-Forge
  8 -> R.drawable.g_star    // Star-Walkers
  else -> R.drawable.g_crown
}
