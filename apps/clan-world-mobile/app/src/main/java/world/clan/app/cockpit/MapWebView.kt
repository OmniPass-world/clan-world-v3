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
 * Loads the canonical world-map surface configured by [BuildConfig.MAP_URL].
 *
 * As of issue #354 (URL scheme rename) the web app serves the raw map at
 * `/map`. The Android webview is expected to point at that path directly —
 * e.g. `https://app.clan-world.com/map` — so this surface and the iframe
 * embedded inside the web cockpit share the exact same render pipeline
 * (one Pixi canvas + version badge + ghost layer + HUD + event ticker).
 *
 * Operators set `CLAN_WORLD_MAP_URL` (env) or `clanWorldMapUrl` (Gradle
 * property) to override at build time. See `app/build.gradle.kts` for the
 * resolution order and `README.md` for the production value.
 *
 * JS + DOM storage are required by the Pixi map; everything else is left
 * at WebView defaults.
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
