package io.easya.kickstart

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : Activity() {
  private lateinit var content: FrameLayout
  private lateinit var homeTab: TextView
  private lateinit var listTab: TextView
  private var selectedUrl: String = BuildConfig.HOME_URL

  private var customTabsClient: CustomTabsClient? = null
  private var customTabsSession: CustomTabsSession? = null
  private var customTabsServiceConn: CustomTabsServiceConnection? = null
  private var preWarmed = false
  private var leaderboardLoaded = false
  private val pendingImageLoads = AtomicInteger(0)
  private val mainHandler = Handler(Looper.getMainLooper())
  private val preWarmFallback = Runnable {
    leaderboardLoaded = true
    maybePreWarmCustomTabs()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val rawIncomingUrl = intent.getStringExtra(EXTRA_URL)
    selectedUrl = sanitizeIncomingUrl(rawIncomingUrl)
    window.statusBarColor = getColor(R.color.kickstart_bg)
    window.navigationBarColor = getColor(R.color.widget_bg)

    val root = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(getColor(R.color.widget_bg))
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

    homeTab.setOnClickListener { launchHomeInCustomTab(selectedUrl) }
    listTab.setOnClickListener { showLeaderboard() }

    bindCustomTabsService()

    if (rawIncomingUrl != null) {
      // Deep-linked into a specific token page → open it in CCT immediately.
      // Show the leaderboard underneath so the back gesture lands somewhere
      // useful instead of an empty FrameLayout.
      showLeaderboard()
      launchHomeInCustomTab(selectedUrl)
    } else {
      showLeaderboard()
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val rawNextUrl = intent.getStringExtra(EXTRA_URL) ?: intent.dataString
    if (rawNextUrl != null) {
      val safeUrl = sanitizeIncomingUrl(rawNextUrl)
      selectedUrl = safeUrl
      launchHomeInCustomTab(safeUrl)
    }
  }

  private fun launchHomeInCustomTab(url: String) {
    selectedUrl = url
    selectTab(homeTab)
    val intent = CustomTabsIntent.Builder(customTabsSession)
      .setDefaultColorSchemeParams(
        CustomTabColorSchemeParams.Builder()
          .setToolbarColor(getColor(R.color.kickstart_bg))
          .build(),
      )
      .setShowTitle(true)
      .build()
    runCatching { intent.launchUrl(this, Uri.parse(url)) }
  }

  private fun showLeaderboard() {
    selectTab(listTab)
    content.removeAllViews()
    val scroll = ScrollView(this).apply { setBackgroundColor(getColor(R.color.widget_bg)) }
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
          // No images to wait on — gate the CCT pre-warm now.
          leaderboardLoaded = true
          maybePreWarmCustomTabs()
        } else {
          // Only count image loads on the very first leaderboard render. After
          // pre-warming has fired (or once we've decided not to wait), let the
          // counter stay at zero so subsequent re-renders don't reopen the gate.
          val countImages = !preWarmed && !leaderboardLoaded
          if (countImages) {
            pendingImageLoads.set(tokens.size)
            // Belt + suspenders: if some image load silently never reports
            // completion (slow CDN, crashed decode, view recycled), fire the
            // pre-warm anyway 12s after the rows are added.
            mainHandler.removeCallbacks(preWarmFallback)
            mainHandler.postDelayed(preWarmFallback, 12_000L)
          }
          tokens.forEach { token ->
            list.addView(tokenRow(
              token = token,
              onImageLoaded = if (countImages) {
                {
                  if (pendingImageLoads.decrementAndGet() <= 0) {
                    mainHandler.removeCallbacks(preWarmFallback)
                    leaderboardLoaded = true
                    maybePreWarmCustomTabs()
                  }
                }
              } else null,
              onClick = {
                KickstartClient.watchToken(token.tokenMint)
                selectedUrl = "${BuildConfig.HOME_URL.trimEnd('/')}/token/${token.tokenMint}"
                launchHomeInCustomTab(selectedUrl)
              },
            ))
          }
        }
      }
    }.start()
  }

  private fun tokenRow(token: KickstartToken, onImageLoaded: (() -> Unit)?, onClick: () -> Unit): View {
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
      TokenImageLoader.loadInto(icon, token.iconUrl, onLoaded = onImageLoaded)
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

  private fun bindCustomTabsService() {
    val packageName = CustomTabsClient.getPackageName(this, null) ?: return
    val conn = object : CustomTabsServiceConnection() {
      override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        customTabsClient = client
        // Don't warmup here — we wait until the leaderboard's images have
        // finished loading so the cold CCT bind doesn't compete with a
        // burst of icon HTTP fetches on first launch.
        maybePreWarmCustomTabs()
      }

      override fun onServiceDisconnected(name: ComponentName?) {
        customTabsClient = null
        customTabsSession = null
      }
    }
    customTabsServiceConn = conn
    runCatching { CustomTabsClient.bindCustomTabsService(this, packageName, conn) }
  }

  private fun maybePreWarmCustomTabs() {
    if (preWarmed) return
    if (!leaderboardLoaded) return
    val client = customTabsClient ?: return
    preWarmed = true
    runCatching { client.warmup(0L) }
    val session = runCatching { client.newSession(null) }.getOrNull()
    customTabsSession = session
    if (session != null) {
      val homeUri = runCatching { Uri.parse(BuildConfig.HOME_URL) }.getOrNull()
      if (homeUri != null) {
        runCatching { session.mayLaunchUrl(homeUri, null, null) }
      }
    }
  }

  override fun onDestroy() {
    mainHandler.removeCallbacks(preWarmFallback)
    customTabsServiceConn?.let { conn ->
      runCatching { unbindService(conn) }
    }
    customTabsServiceConn = null
    customTabsClient = null
    customTabsSession = null
    super.onDestroy()
  }

  /**
   * Allow only HTTPS URLs whose host matches the configured kickstart home or
   * an explicit allowlist of trusted subdomains. Any other input falls back to
   * the configured `BuildConfig.HOME_URL` so an external app or deep link
   * can't redirect this exported activity at an arbitrary origin.
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
     * Hosts allowed for incoming intent URLs in addition to the configured
     * `BuildConfig.HOME_URL` host. Keep this list narrow.
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
