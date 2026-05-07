package world.clan.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens ported from apps/web/src/styles/cockpit-tokens.ts and
 * apps/web/src/pages/agent/agent-tokens.ts. Hex values are exact; treat
 * this file as the canonical source of truth for the native cockpit.
 */
object CockpitTokens {

  object Bg {
    val Void          = Color(0xFF0A0A0A)
    val Iron          = Color(0xFF2A2620)
    val IronDeep      = Color(0xFF16140F)
    val Ink           = Color(0xFF1A1612)
    val Parchment     = Color(0xFFE8DEC7)
    val ParchmentDim  = Color(0xFFCDBFA0)
  }

  object TextC {
    val OnIron        = Color(0xFFC9B88A)
    val OnIronDim     = Color(0xFF7A6B4A)
    val OnParchment   = Color(0xFF2A1F10)
    val OnParchmentDim= Color(0xFF5A4628)
    val Accent        = Color(0xFFD4A544)
    val Danger        = Color(0xFFB03A2E)
    val Muted         = Color(0xFF6B5E44)
  }

  object Border {
    val Iron          = Color(0xFF3A3528)
    val IronLight     = Color(0xFF4A4232)
    val ParchmentEdge = Color(0xFFA89B78)
  }

  /** Owner page palette — ember (primary CTAs) + rune (AI/iNFT signal). */
  object Ember {
    val Core = Color(0xFFFF6B35)
    val Glow = Color(0xFFFF8A55)
    val Deep = Color(0xFFB34423)
    val Dim  = Color(0xFF6B2614)
  }
  object Rune {
    val Core = Color(0xFF5FC5D4)
    val Glow = Color(0xFF8CE0EC)
    val Deep = Color(0xFF2D6F7A)
  }
  object Obsidian {
    /** Owner page bg gradient seed (radial start at top). */
    val Top    = Color(0xFF150F0A)
    val Bottom = Color(0xFF050405)
  }

  object Space {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
  }

  object Radius {
    val sm: Dp = 2.dp
    val md: Dp = 4.dp
    val lg: Dp = 8.dp
  }
}
