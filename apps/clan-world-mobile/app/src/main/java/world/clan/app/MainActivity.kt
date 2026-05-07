package world.clan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import world.clan.app.ui.ClanWorldApp
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Obsidian

/**
 * The Compose host activity. Slice 1's launcher.
 *
 * `enableEdgeToEdge` lets the obsidian background flow under the status
 * and navigation bars; per-screen Scaffolds handle inset padding for
 * content that needs to clear them.
 */
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Obsidian.toArgb()),
      navigationBarStyle = SystemBarStyle.dark(Obsidian.toArgb()),
    )
    super.onCreate(savedInstanceState)
    setContent {
      ClanWorldTheme {
        ClanWorldApp(app = applicationContext as App, hostActivity = this)
      }
    }
  }
}
