package world.clan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

/**
 * App-level theme wrapper. Material3 is used only for its scaffolding
 * primitives — the actual surfaces are styled directly via CockpitTokens
 * to match the web cockpit's parchment-on-iron aesthetic 1:1. We force
 * a dark scheme so any system-driven fallbacks don't bleed through.
 */
@Composable
fun ClanWorldTheme(
  @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val scheme = darkColorScheme(
    background = CockpitTokens.Bg.Void,
    surface    = CockpitTokens.Bg.Iron,
    primary    = CockpitTokens.TextC.Accent,
    onPrimary  = CockpitTokens.Bg.Ink,
    onBackground = CockpitTokens.TextC.OnIron,
    onSurface  = CockpitTokens.TextC.OnIron,
  )

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      window.statusBarColor = CockpitTokens.Bg.Void.toArgb()
      window.navigationBarColor = CockpitTokens.Bg.Void.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
  }

  MaterialTheme(
    colorScheme = scheme,
    content = content,
  )
}
