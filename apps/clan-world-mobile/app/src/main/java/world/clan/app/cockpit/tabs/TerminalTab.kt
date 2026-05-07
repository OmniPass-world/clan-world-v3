package world.clan.app.cockpit.tabs

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import world.clan.app.BuildConfig
import world.clan.app.data.Elder
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private const val RETRY_DELAY_MS = 3_000L

/**
 * Terminal tab — read-only WebView mirroring the live ttyd iframe used in
 * the web cockpit (`https://cockpit.clan-world.com/elder-{N}-tty/`).
 *
 *  - **Tap blocker** — a [pointerInteropFilter] wrapper consumes all touch
 *    events so the user can't focus the WebView (which would pop up the
 *    soft keyboard) or accidentally scroll.
 *  - **Auto-reconnect** — main-frame load failures (DNS, network) flip an
 *    `isReconnecting` state. A small spinner pill appears in the bottom-right
 *    corner over a black backdrop (so the WebView's default broken-Android
 *    error page is hidden), and the load is retried every 3 seconds until
 *    `onPageFinished` reports success. Mirrors the web cockpit's auto-
 *    reconnect indicator pattern.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
fun TerminalTab(
  elder: Elder,
  modifier: Modifier = Modifier,
) {
  val url = remember(elder.clanId) {
    "${BuildConfig.TERMINAL_BASE_URL.trimEnd('/')}/elder-${elder.clanId}-tty/"
  }

  var isReconnecting by remember(elder.clanId) { mutableStateOf(false) }
  var reloadToken by remember(elder.clanId) { mutableIntStateOf(0) }

  // Auto-retry loop: each time we land in `isReconnecting`, wait
  // RETRY_DELAY_MS then bump reloadToken to trigger a fresh load. If the
  // load succeeds, onPageFinished clears isReconnecting and we stop. If
  // it fails again, onReceivedError flips it back true and the loop
  // re-arms via the (reloadToken, isReconnecting) key.
  LaunchedEffect(reloadToken, isReconnecting) {
    if (isReconnecting) {
      delay(RETRY_DELAY_MS)
      reloadToken += 1
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      // Backdrop matches the panel chrome (not Bg.Ink) so the brief
      // "first-frame" flash while the WebView measures itself is invisible
      // — the panel just looks like an empty panel for a split second.
      .background(CockpitTokens.Bg.Iron)
      // Consume every pointer event before it reaches the WebView so taps
      // can't focus an input element and pop the soft keyboard.
      .pointerInteropFilter { true },
  ) {
    AndroidView(
      // matchParentSize keeps the AndroidView strictly within its parent
      // Box's bounds, so the WebView's native wrap_content measure can't
      // briefly push the panel taller than the layout allows.
      modifier = Modifier.matchParentSize(),
      factory = { ctx ->
        // Per-instance flag set during onReceivedError(mainFrame) and read
        // at onPageFinished — lets us distinguish "page loaded successfully"
        // from "page loaded the WebView's default error doc".
        var mainFrameFailedThisLoad = false

        WebView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )
          isFocusable = false
          isFocusableInTouchMode = false
          setBackgroundColor(CockpitTokens.Bg.Iron.toArgb())
          webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
              mainFrameFailedThisLoad = false
            }

            override fun onReceivedError(
              view: WebView,
              request: WebResourceRequest,
              error: WebResourceError,
            ) {
              if (request.isForMainFrame) {
                mainFrameFailedThisLoad = true
              }
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
              view: WebView?,
              errorCode: Int,
              description: String?,
              failingUrl: String?,
            ) {
              mainFrameFailedThisLoad = true
            }

            override fun onPageFinished(view: WebView, finishedUrl: String) {
              if (mainFrameFailedThisLoad) {
                // Hide the WebView's built-in error page behind a blank
                // doc; the Compose overlay will render the reconnect chip.
                // NOTE: this loadData() will fire a SECONDARY onPageFinished
                // for the data: URL — gated below so it doesn't clear
                // isReconnecting and kill the auto-reconnect loop.
                view.loadData("<html><body style=\"background:#1a1612\"></body></html>", "text/html", "utf-8")
                isReconnecting = true
              } else if (finishedUrl.startsWith(url)) {
                // Only clear the reconnecting flag when the REAL target
                // URL finishes loading successfully. The data:/about:blank
                // mask loaded above must not flip us out of reconnect.
                isReconnecting = false
              }
            }
          }
          with(settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
          }
          tag = -1
          loadUrl(url)
        }
      },
      update = { webView ->
        if ((webView.tag as? Int) != reloadToken) {
          webView.tag = reloadToken
          webView.loadUrl(url)
        }
      },
      onRelease = { it.destroy() },
    )

    if (isReconnecting) {
      ReconnectingChip(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(12.dp),
      )
    }
  }
}

/**
 * Small bottom-right indicator: thin pill, rotating glyph + "RECONNECTING".
 * Same vibe as the cockpit's connection pills — quiet, doesn't grab focus.
 */
@Composable
private fun ReconnectingChip(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "reconnect")
  val angle by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1_400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "spin",
  )

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .background(CockpitTokens.Bg.IronDeep.copy(alpha = 0.92f))
      .border(1.dp, CockpitTokens.TextC.Accent.copy(alpha = 0.55f), RoundedCornerShape(CockpitTokens.Radius.sm))
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.rotate(angle),
      text = "◐",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 12.sp,
        color = CockpitTokens.TextC.Accent,
      ),
    )
    Box(modifier = Modifier.padding(start = 6.dp))
    Text(
      text = "RECONNECTING",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = CockpitTokens.TextC.OnIron,
        letterSpacing = 1.08.sp, // 0.12em
      ),
    )
  }
}
