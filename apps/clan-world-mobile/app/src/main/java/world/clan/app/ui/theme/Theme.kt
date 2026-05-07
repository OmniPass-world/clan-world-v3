package world.clan.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Access the Clan World colors and typography from any composable:
 *
 *     val tone = ClanWorldTheme.colors.ember
 *     val style = ClanWorldTheme.type.scriptItalic
 *
 * Material3's MaterialTheme is wrapped only so a few system widgets
 * (NavigationBar ripple, etc.) read sane defaults. App code should NOT
 * reach into MaterialTheme.colorScheme — go through ClanWorldTheme.
 *
 * Edge-to-edge + transparent system bars are configured in [world.clan.app.MainActivity]
 * via `enableEdgeToEdge()`.
 */
object ClanWorldTheme {
  val colors: ClanWorldColors
    @Composable @ReadOnlyComposable
    get() = LocalClanWorldColors.current
  val type: ClanWorldTypography
    @Composable @ReadOnlyComposable
    get() = LocalClanWorldTypography.current
}

@Composable
fun ClanWorldTheme(
  content: @Composable () -> Unit,
) {
  val materialScheme = darkColorScheme(
    primary       = Ember,
    onPrimary     = Color(0xFF1A0C04),
    secondary     = Rune,
    onSecondary   = Obsidian,
    background    = Obsidian,
    onBackground  = Warm,
    surface       = Iron,
    onSurface     = Warm,
    surfaceVariant = Iron2,
    error         = Danger,
  )

  CompositionLocalProvider(
    LocalClanWorldColors      provides ClanWorldColors(),
    LocalClanWorldTypography  provides ClanWorldTypography(),
  ) {
    MaterialTheme(
      colorScheme = materialScheme,
      typography  = ClanWorldMaterialTypography,
      content     = content,
    )
  }
}
