package world.clan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import world.clan.app.ui.ClanWorldApp
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Obsidian

class MainActivity : ComponentActivity() {

  // ActivityResultSender registers an ActivityResultLauncher in its
  // constructor; AndroidX requires registration before the Activity
  // reaches STARTED. Constructing it lazily on Connect-tap throws
  // "LifecycleOwners must call register before they are STARTED" —
  // crashed v0.1.15 on first tap. Hoist to onCreate (pre-super) and
  // thread through Compose.
  private lateinit var mwaSender: ActivityResultSender

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Obsidian.toArgb()),
      navigationBarStyle = SystemBarStyle.dark(Obsidian.toArgb()),
    )
    mwaSender = ActivityResultSender(this)
    super.onCreate(savedInstanceState)
    setContent {
      ClanWorldTheme {
        ClanWorldApp(
          app = applicationContext as App,
          hostActivity = this,
          mwaSender = mwaSender,
        )
      }
    }
  }
}
