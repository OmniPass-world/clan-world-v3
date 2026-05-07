package io.easya.kickstart

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
  private lateinit var webView: WebView
  private lateinit var content: FrameLayout
  private lateinit var homeTab: TextView
  private lateinit var listTab: TextView
  private var selectedUrl: String = BuildConfig.HOME_URL
  private var walletDialog: AlertDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val rawIncomingUrl = intent.getStringExtra(EXTRA_URL)
    selectedUrl = sanitizeIncomingUrl(rawIncomingUrl)
    window.statusBarColor = getColor(R.color.kickstart_bg)
    window.navigationBarColor = getColor(R.color.kickstart_bg)

    val root = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(getColor(R.color.kickstart_bg))
    }
    val tabs = LinearLayout(this).apply {
      orientation = LinearLayout.HORIZONTAL
      setPadding(dp(10), dp(8), dp(10), dp(8))
      setBackgroundColor(getColor(R.color.kickstart_bg))
    }
    homeTab = tab("Home")
    listTab = tab("Top 100")
    tabs.addView(homeTab, LinearLayout.LayoutParams(0, dp(42), 1f))
    tabs.addView(listTab, LinearLayout.LayoutParams(0, dp(42), 1f))
    content = FrameLayout(this)
    root.addView(tabs)
    root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    root.setOnApplyWindowInsetsListener { view, insets ->
      @Suppress("DEPRECATION")
      view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
      insets
    }
    setContentView(root)
    root.requestApplyInsets()

    homeTab.setOnClickListener { showHome(selectedUrl) }
    listTab.setOnClickListener { showLeaderboard() }
    if (rawIncomingUrl != null) {
      showHome(selectedUrl)
    } else {
      showLeaderboard()
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val rawNextUrl = intent.getStringExtra(EXTRA_URL) ?: intent.dataString
    if (rawNextUrl != null && ::webView.isInitialized) {
      val safeUrl = sanitizeIncomingUrl(rawNextUrl)
      selectedUrl = safeUrl
      showHome(safeUrl)
    }
  }

  private fun showHome(url: String) {
    selectedUrl = url
    selectTab(homeTab)
    // Tear down any prior WebView before re-allocating: showHome() can be
    // re-invoked (tab switch, deep link, onNewIntent) and otherwise leaks
    // the renderer process per re-entry.
    destroyWebViewIfPresent()
    content.removeAllViews()
    webView = WebView(this).apply {
      webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
          return handleExternalUrl(request.url)
        }

        @Deprecated("Deprecated in Android")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
          return handleExternalUrl(Uri.parse(url))
        }
      }
      settings.javaScriptEnabled = true
      settings.domStorageEnabled = true
      setBackgroundColor(getColor(R.color.kickstart_bg))
    }
    content.addView(webView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    webView.loadUrl(url)
  }

  private fun showLeaderboard() {
    selectTab(listTab)
    content.removeAllViews()
    val scroll = ScrollView(this).apply { setBackgroundColor(getColor(R.color.kickstart_bg)) }
    val list = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(dp(12), dp(8), dp(12), dp(24))
    }
    scroll.addView(list)
    content.addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    list.addView(rowText("Loading Kickstart top 100...", 14, getColor(R.color.widget_muted)))
    Thread {
      val tokens = runCatching { KickstartClient.listTokens() }.getOrElse { emptyList() }
      runOnUiThread {
        list.removeAllViews()
        if (tokens.isEmpty()) {
          list.addView(rowText("No leaderboard data yet. Try again in a moment.", 14, getColor(R.color.widget_muted)))
        } else {
          tokens.forEach { token ->
            list.addView(tokenRow(token) {
              KickstartClient.watchToken(token.tokenMint)
              selectedUrl = "${BuildConfig.HOME_URL.trimEnd('/')}/token/${token.tokenMint}"
              selectTab(homeTab)
              showHome(selectedUrl)
            })
          }
        }
      }
    }.start()
  }

  private fun tokenRow(token: KickstartToken, onClick: () -> Unit): View {
    return LinearLayout(this).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
      setPadding(dp(10), dp(10), dp(10), dp(10))
      setBackgroundColor(Colorish.card)
      setOnClickListener { onClick() }
      val icon = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        clipToOutline = true
      }
      TokenImageLoader.loadInto(icon, token.iconUrl)
      val left = TextView(context).apply {
        text = "#${token.rank}  ${token.symbol}\n${token.name}"
        setTextColor(getColor(R.color.widget_text))
        textSize = 14f
      }
      val right = TextView(context).apply {
        text = "${formatMoney(token.mcap)}\n${formatChange(token.priceChange24h)} 24H"
        setTextColor(if ((token.priceChange24h ?: 0.0) >= 0.0) getColor(R.color.widget_green) else getColor(R.color.widget_red))
        textSize = 13f
        gravity = Gravity.END
      }
      addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)).apply {
        marginEnd = dp(10)
      })
      addView(left, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
      addView(right)
    }
  }

  private fun tab(label: String) = TextView(this).apply {
    text = label
    gravity = Gravity.CENTER
    textSize = 15f
    setTextColor(getColor(R.color.kickstart_text))
  }

  private fun selectTab(selected: TextView) {
    val bg = getColor(R.color.kickstart_bg)
    val bgSelected = getColor(R.color.kickstart_bg_selected)
    homeTab.setBackgroundColor(if (selected === homeTab) bgSelected else bg)
    listTab.setBackgroundColor(if (selected === listTab) bgSelected else bg)
  }

  private fun rowText(text: String, size: Int, color: Int) = TextView(this).apply {
    this.text = text
    textSize = size.toFloat()
    setTextColor(color)
    setPadding(dp(10), dp(10), dp(10), dp(10))
  }

  private fun formatMoney(value: Double): String = "$" + "%,.0f".format(value)
  private fun formatChange(value: Double?): String {
    if (value == null) return "--"
    val sign = if (value > 0.0) "+" else ""
    return "$sign${"%.1f".format(value)}%"
  }

  private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

  private fun handleExternalUrl(uri: Uri): Boolean {
    val scheme = uri.scheme?.lowercase() ?: return false
    if (scheme == "http" || scheme == "https") return false
    if (isWalletUrl(uri)) showOpenInChromeDialog()
    return true
  }

  private fun isWalletUrl(uri: Uri): Boolean {
    val value = uri.toString().lowercase()
    val scheme = uri.scheme?.lowercase()
    if (scheme == "solana-wallet" || scheme == "phantom" || scheme == "solflare") return true
    if (scheme != "intent") return value.contains("solana-wallet") || value.contains("phantom") || value.contains("solflare")
    val intent = runCatching { Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME) }.getOrNull()
    val parsedScheme = intent?.scheme?.lowercase()
    val fallback = intent?.getStringExtra("browser_fallback_url")?.lowercase()
    return parsedScheme == "solana-wallet" ||
      parsedScheme == "phantom" ||
      parsedScheme == "solflare" ||
      fallback?.contains("solana-wallet") == true ||
      fallback?.contains("phantom") == true ||
      fallback?.contains("solflare") == true ||
      value.contains("solana-wallet") ||
      value.contains("phantom") ||
      value.contains("solflare")
  }

  private fun showOpenInChromeDialog() {
    if (isFinishing || isDestroyed) return
    if (walletDialog?.isShowing == true) return
    walletDialog = AlertDialog.Builder(this)
      .setTitle("Open in Chrome")
      .setMessage("Wallet connection is not supported inside this app. Open this token page in Chrome to connect a wallet.")
      .setPositiveButton("Open") { _, _ -> openSelectedPageInBrowser() }
      .setNegativeButton("Cancel", null)
      .create()
      .also { dialog ->
        dialog.setOnDismissListener { walletDialog = null }
        dialog.show()
      }
  }

  private fun openSelectedPageInBrowser() {
    val pageUrl = selectedUrl.takeIf { it.startsWith("http://") || it.startsWith("https://") } ?: BuildConfig.HOME_URL
    val uri = Uri.parse(pageUrl)
    val chromeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
      addCategory(Intent.CATEGORY_BROWSABLE)
      setPackage("com.android.chrome")
    }
    runCatching { startActivity(chromeIntent) }
      .recoverCatching { error ->
        if (error is ActivityNotFoundException) {
          startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
        }
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

  /**
   * Allow only HTTPS URLs whose host matches the configured kickstart home or
   * an explicit allowlist of trusted subdomains. Any other input falls back to
   * the configured `BuildConfig.HOME_URL` so an external app or deep link
   * can't redirect this exported, JS-enabled WebView at an arbitrary origin.
   */
  private fun sanitizeIncomingUrl(rawUrl: String?): String {
    if (rawUrl.isNullOrBlank()) return BuildConfig.HOME_URL
    return if (isUrlAllowed(rawUrl)) rawUrl else BuildConfig.HOME_URL
  }

  private fun isUrlAllowed(url: String): Boolean {
    return runCatching {
      val uri = Uri.parse(url)
      val scheme = uri.scheme?.lowercase()
      if (scheme != "https") return@runCatching false
      val host = uri.host?.lowercase() ?: return@runCatching false
      val homeHost = Uri.parse(BuildConfig.HOME_URL).host?.lowercase()
      val allowedHosts = buildSet {
        if (!homeHost.isNullOrBlank()) add(homeHost)
        addAll(EXTRA_ALLOWED_HOSTS)
      }
      allowedHosts.any { allowed -> host == allowed || host.endsWith(".$allowed") }
    }.getOrDefault(false)
  }

  companion object {
    const val EXTRA_URL = "io.easya.kickstart.EXTRA_URL"

    /**
     * Hosts allowed for WebView loads in addition to the configured
     * `BuildConfig.HOME_URL` host. Keep this list narrow; every entry
     * widens the trust surface of an exported JS-enabled WebView.
     */
    private val EXTRA_ALLOWED_HOSTS = setOf(
      "kickstart.easya.io",
      "easya.io",
    )
  }
}

private object Colorish {
  val card: Int = android.graphics.Color.rgb(22, 22, 22)
}
