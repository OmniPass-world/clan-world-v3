package world.clan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import world.clan.app.nav.AppNav
import world.clan.app.ui.theme.ClanWorldTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ClanWorldTheme {
        AppNav()
      }
    }
  }
}
