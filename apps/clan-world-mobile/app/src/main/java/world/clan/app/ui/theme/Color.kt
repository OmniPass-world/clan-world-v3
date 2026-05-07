package world.clan.app.ui.theme

import androidx.compose.ui.graphics.Color

// === Surfaces ============================================================
val Obsidian       = Color(0xFF08070A)
val Obsidian2      = Color(0xFF0D0C10)
val Iron           = Color(0xFF15110E)
val Iron2          = Color(0xFF1F1A14)
val Iron3          = Color(0xFF2A2418)
val IronEdge       = Color(0xFF3A2F20)

// === Hairlines ============================================================
val Hairline       = Color(0x29D4A04A) // rgba(212,160,74,0.16)
val HairlineMid    = Color(0x4DD4A04A) // 0.30
val HairlineStrong = Color(0x8CD4A04A) // 0.55

// === Ember ================================================================
val Ember          = Color(0xFFFF6B35)
val EmberHover     = Color(0xFFFF8A55)
val EmberGlow      = Color(0x66FF6B35) // 0.40
val EmberDeep      = Color(0xFFB34423)

// === Rune (signal / AI events) ============================================
val Rune           = Color(0xFF5FC5D4)
val RuneDim        = Color(0x8C5FC5D4)
val RuneGlow       = Color(0xFF8CE0EC)

// === Gold (currency / value) ==============================================
val Gold           = Color(0xFFD4A04A)
val GoldBright     = Color(0xFFE8B658)
val GoldDim        = Color(0x8CD4A04A)

// === Parchment family =====================================================
val Parchment      = Color(0xFFE8DEC7)
val Parchment2     = Color(0xFFD8C79F)
val ParchmentShade = Color(0xFFC8B58E)

// === Inks on parchment ====================================================
val Ink            = Color(0xFF2A1F10)
val Ink2           = Color(0xFF4A3820)
val Ink3           = Color(0xFF6A532F)

// === Warm text on dark ====================================================
val Warm           = Color(0xFFC9B88A)
val WarmDim        = Color(0x9EC9B88A) // 0.62
val WarmFaint      = Color(0x52C9B88A) // 0.32

// === Status ===============================================================
val Danger         = Color(0xFFFF5050)
val Success        = Color(0xFF2F855A)
val Warn           = Color(0xFF9B2A2F)

// === Eight clan accents ===================================================
val Clan1 = Color(0xFFC53030) // Storm-Edge / blade
val Clan2 = Color(0xFF2C5282) // Tideborne / tide
val Clan3 = Color(0xFFB7791F) // Sunhold / crown
val Clan4 = Color(0xFF2F855A) // Vale-Ward / leaf
val Clan5 = Color(0xFF6B46C1) // Twilight / eye
val Clan6 = Color(0xFFC05621) // Ember-Kin / flame
val Clan7 = Color(0xFF319795) // (anvil)
val Clan8 = Color(0xFF744210) // (star)

val ClanColors = listOf(Clan1, Clan2, Clan3, Clan4, Clan5, Clan6, Clan7, Clan8)
fun clanColor(clanId: Int): Color = ClanColors[(clanId - 1).coerceIn(0, 7)]
