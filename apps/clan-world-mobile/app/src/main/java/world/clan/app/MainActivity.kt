package world.clan.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class MainActivity : Activity() {
  private lateinit var webView: WebView
  private lateinit var shell: FrameLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)
    window.statusBarColor = getColor(R.color.clan_bg)
    window.navigationBarColor = getColor(R.color.clan_bg)

    shell = FrameLayout(this).apply {
      setBackgroundColor(getColor(R.color.clan_bg))
    }
    setContentView(shell)
    showHome()
  }

  private fun showHome() {
    // If a previous WebView exists (e.g. coming back from background or
    // re-entering the home tab), tear it down before allocating a new one
    // so we don't leak the renderer process across re-creates.
    destroyWebViewIfPresent()
    shell.removeAllViews()

    val root = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(getColor(R.color.clan_bg))
    }

    val header = LinearLayout(this).apply {
      gravity = Gravity.CENTER_VERTICAL
      orientation = LinearLayout.HORIZONTAL
      setPadding(dp(16), dp(10), dp(10), dp(10))
      setBackgroundColor(getColor(R.color.clan_panel))
    }

    val logo = ImageView(this).apply {
      setImageResource(R.drawable.square_logo_main)
      scaleType = ImageView.ScaleType.FIT_CENTER
      contentDescription = getString(R.string.app_name)
    }
    val title = TextView(this).apply {
      text = getString(R.string.app_name)
      setTextColor(getColor(R.color.clan_text))
      textSize = 18f
      includeFontPadding = false
    }
    val open = ImageButton(this).apply {
      setImageResource(android.R.drawable.ic_menu_view)
      setColorFilter(getColor(R.color.clan_gold))
      setBackgroundColor(Color.TRANSPARENT)
      contentDescription = "Open in browser"
      setOnClickListener { openExternal(BuildConfig.HOME_URL) }
    }

    header.addView(logo, LinearLayout.LayoutParams(dp(36), dp(36)).apply {
      marginEnd = dp(12)
    })
    header.addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    header.addView(open, LinearLayout.LayoutParams(dp(44), dp(44)))

    webView = WebView(this).apply {
      webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
          return handleUrl(request.url)
        }

        @Deprecated("Deprecated in Android")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
          return handleUrl(Uri.parse(url))
        }
      }
      settings.javaScriptEnabled = true
      settings.domStorageEnabled = true
      setBackgroundColor(getColor(R.color.clan_bg))
      loadUrl(BuildConfig.HOME_URL)
    }

    root.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)))
    root.addView(webView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    root.setOnApplyWindowInsetsListener { view, insets ->
      @Suppress("DEPRECATION")
      view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
      insets
    }

    shell.addView(root, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    root.requestApplyInsets()
  }

  private fun handleUrl(uri: Uri): Boolean {
    val scheme = uri.scheme?.lowercase() ?: return true
    if (scheme == "http" || scheme == "https") {
      // Allowlist enforcement: only allow http(s) navigations whose host
      // matches the configured HOME_URL host or an explicit allowlist.
      // Anything else (a phishing link injected via window.location, an
      // adversarial deep link, etc.) is blocked from this JS-enabled,
      // storage-backed WebView and instead handed to an external browser.
      return if (isHttpUrlAllowed(uri)) {
        false
      } else {
        runCatching {
          startActivity(
            Intent(Intent.ACTION_VIEW, uri)
              .addCategory(Intent.CATEGORY_BROWSABLE),
          )
        }
        true
      }
    }
    val intent = if (scheme == "intent") {
      runCatching { Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME) }.getOrNull()
    } else {
      Intent(Intent.ACTION_VIEW, uri)
    } ?: return true
    runCatching {
      intent.addCategory(Intent.CATEGORY_BROWSABLE)
      intent.component = null
      startActivity(intent)
    }.recoverCatching { error ->
      if (error is ActivityNotFoundException && intent.getStringExtra("browser_fallback_url") != null) {
        val fallbackUrl = intent.getStringExtra("browser_fallback_url")!!
        if (isHttpUrlAllowed(Uri.parse(fallbackUrl))) {
          webView.loadUrl(fallbackUrl)
        }
      }
    }
    return true
  }

  /**
   * Allow only HTTPS URLs whose host matches the configured clan-world home
   * or an explicit allowlist of trusted subdomains. Mirrors the kickstart
   * pattern: every entry widens the trust surface of an exported, JS-enabled
   * WebView, so keep the list narrow.
   */
  private fun isHttpUrlAllowed(uri: Uri): Boolean {
    return runCatching {
      val scheme = uri.scheme?.lowercase()
      if (scheme != "https" && scheme != "http") return@runCatching false
      val host = uri.host?.lowercase() ?: return@runCatching false
      val homeHost = Uri.parse(BuildConfig.HOME_URL).host?.lowercase()
      val allowedHosts = buildSet {
        if (!homeHost.isNullOrBlank()) add(homeHost)
        addAll(EXTRA_ALLOWED_HOSTS)
      }
      allowedHosts.any { allowed -> host == allowed || host.endsWith(".$allowed") }
    }.getOrDefault(false)
  }

  private fun openExternal(url: String) {
    runCatching {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
  }

  override fun onBackPressed() {
    if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
    else super.onBackPressed()
  }

  override fun onDestroy() {
    destroyWebViewIfPresent()
    super.onDestroy()
  }

  private fun destroyWebViewIfPresent() {
    if (::webView.isInitialized) {
      webView.stopLoading()
      webView.loadUrl("about:blank")
      (webView.parent as? ViewGroup)?.removeView(webView)
      webView.removeAllViews()
      webView.destroy()
    }
  }

  private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

  companion object {
    /**
     * Hosts allowed for WebView loads in addition to the configured
     * `BuildConfig.HOME_URL` host. Keep this list narrow; every entry
     * widens the trust surface of an exported JS-enabled WebView.
     */
    private val EXTRA_ALLOWED_HOSTS = setOf(
      "clan-world.com",
      "app.clan-world.com",
    )
  }
}
