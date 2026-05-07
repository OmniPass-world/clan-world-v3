package world.clan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * App-level theme wrapper. Material3 supplies scaffolding primitives;
 * surfaces are styled directly via [CockpitTokens] to match the web
 * cockpit's parchment-on-iron aesthetic 1:1. We force a dark scheme so
 * any system-driven fallbacks don't bleed through.
 *
 * Edge-to-edge + transparent system bars are configured in [world.clan.app.MainActivity]
 * via `enableEdgeToEdge()`, which is the modern replacement for the
 * deprecated `Window.statusBarColor` / `Window.navigationBarColor` setters.
 */
@Composable
fun ClanWorldTheme(
  @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val scheme = darkColorScheme(
    background   = CockpitTokens.Bg.Void,
    surface      = CockpitTokens.Bg.Iron,
    primary      = CockpitTokens.TextC.Accent,
    onPrimary    = CockpitTokens.Bg.Ink,
    onBackground = CockpitTokens.TextC.OnIron,
    onSurface    = CockpitTokens.TextC.OnIron,
  )

  MaterialTheme(
    colorScheme = scheme,
    content = content,
  )
}
