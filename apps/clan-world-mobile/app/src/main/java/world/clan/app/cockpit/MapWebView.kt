package world.clan.app.cockpit

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import world.clan.app.BuildConfig
import world.clan.app.ui.theme.CockpitTokens

/**
 * Renders the live game map by hosting a WebView that loads
 * [BuildConfig.MAP_URL] (defaults to https://app.clan-world.com — the
 * dedicated map-only surface). JS + DOM storage are required by the
 * Pixi map; everything else is left at WebView defaults.
 */
@Composable
@SuppressLint("SetJavaScriptEnabled")
fun MapWebView(modifier: Modifier = Modifier) {
  AndroidView(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Void),
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
        setBackgroundColor(CockpitTokens.Bg.Void.toArgb())
        webViewClient = WebViewClient()
        with(settings) {
          javaScriptEnabled = true
          domStorageEnabled = true
          mediaPlaybackRequiresUserGesture = false
        }
        loadUrl(BuildConfig.MAP_URL)
      }
    },
    onRelease = { it.destroy() },
  )
}
