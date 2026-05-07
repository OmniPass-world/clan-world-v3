package world.clan.app.cockpit.tabs

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import world.clan.app.BuildConfig
import world.clan.app.data.Elder
import world.clan.app.ui.theme.CockpitTokens

/**
 * Terminal tab — a WebView mirroring the live ttyd iframe used in the
 * web cockpit (`https://cockpit.clan-world.com/elder-{N}-tty/`). The base
 * URL is `BuildConfig.TERMINAL_BASE_URL`; we append `/elder-{N}-tty/`.
 *
 * The web iframe is an XTerm session, so DOM storage and JS are required.
 */
@Composable
@SuppressLint("SetJavaScriptEnabled")
fun TerminalTab(
  elder: Elder,
  modifier: Modifier = Modifier,
) {
  val url = remember(elder.clanId) {
    "${BuildConfig.TERMINAL_BASE_URL.trimEnd('/')}/elder-${elder.clanId}-tty/"
  }
  AndroidView(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Ink),
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
        setBackgroundColor(CockpitTokens.Bg.Ink.toArgb())
        webViewClient = WebViewClient()
        with(settings) {
          javaScriptEnabled = true
          domStorageEnabled = true
        }
        loadUrl(url)
      }
    },
    update = { webView ->
      if (webView.url != url) webView.loadUrl(url)
    },
  )
}
