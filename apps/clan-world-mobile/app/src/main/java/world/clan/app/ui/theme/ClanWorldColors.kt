package world.clan.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The full Clan World color set, exposed via CompositionLocal because
 * Material3's ColorScheme is too narrow for this design (no slot for
 * ember.glow, parchment.shade, hairline.strong, eight clan accents).
 */
@Immutable
data class ClanWorldColors(
  val obsidian: Color = Obsidian,
  val obsidian2: Color = Obsidian2,
  val iron: Color = Iron,
  val iron2: Color = Iron2,
  val iron3: Color = Iron3,
  val ironEdge: Color = IronEdge,

  val hairline: Color = Hairline,
  val hairlineMid: Color = HairlineMid,
  val hairlineStrong: Color = HairlineStrong,

  val ember: Color = Ember,
  val emberHover: Color = EmberHover,
  val emberGlow: Color = EmberGlow,
  val emberDeep: Color = EmberDeep,

  val rune: Color = Rune,
  val runeDim: Color = RuneDim,
  val runeGlow: Color = RuneGlow,

  val gold: Color = Gold,
  val goldBright: Color = GoldBright,
  val goldDim: Color = GoldDim,

  val parchment: Color = Parchment,
  val parchment2: Color = Parchment2,
  val parchmentShade: Color = ParchmentShade,

  val ink: Color = Ink,
  val ink2: Color = Ink2,
  val ink3: Color = Ink3,

  val warm: Color = Warm,
  val warmDim: Color = WarmDim,
  val warmFaint: Color = WarmFaint,

  val danger: Color = Danger,
  val success: Color = Success,
  val warn: Color = Warn,

  val clans: List<Color> = ClanColors,
)

val LocalClanWorldColors = staticCompositionLocalOf { ClanWorldColors() }
