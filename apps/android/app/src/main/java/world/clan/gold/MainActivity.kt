package world.clan.gold

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.browser.customtabs.CustomTabsIntent

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.statusBarColor = getColor(R.color.widget_bg)
    window.navigationBarColor = getColor(R.color.widget_bg)

    val container = FrameLayout(this).apply { setBackgroundColor(getColor(R.color.widget_bg)) }
    setContentView(container)
    CustomTabsIntent.Builder()
      .setShowTitle(true)
      .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
      .build()
      .launchUrl(this, Uri.parse(BuildConfig.TOKEN_URL))
  }
}
