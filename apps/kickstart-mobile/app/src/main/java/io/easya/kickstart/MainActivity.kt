package io.easya.kickstart

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
  private lateinit var webView: WebView
  private lateinit var content: FrameLayout
  private lateinit var homeTab: TextView
  private lateinit var listTab: TextView
  private var selectedUrl: String = BuildConfig.HOME_URL

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    selectedUrl = intent.getStringExtra(EXTRA_URL) ?: BuildConfig.HOME_URL
    window.statusBarColor = getColor(R.color.widget_bg)
    window.navigationBarColor = getColor(R.color.widget_bg)

    val root = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(getColor(R.color.widget_bg))
    }
    val tabs = LinearLayout(this).apply {
      orientation = LinearLayout.HORIZONTAL
      setPadding(dp(10), dp(8), dp(10), dp(8))
      setBackgroundColor(getColor(R.color.widget_bg))
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
    showHome(selectedUrl)
  }

  private fun showHome(url: String) {
    selectedUrl = url
    selectTab(homeTab)
    content.removeAllViews()
    webView = WebView(this)
    webView.webViewClient = WebViewClient()
    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true
    webView.setBackgroundColor(getColor(R.color.widget_bg))
    content.addView(webView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    webView.loadUrl(url)
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
        } else {
          tokens.forEach { token ->
            list.addView(tokenRow(token) {
              KickstartClient.watchToken(token.tokenMint)
              showHome("${BuildConfig.HOME_URL.trimEnd('/')}/token/${token.tokenMint}")
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
      addView(left, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
      addView(right)
    }
  }

  private fun tab(label: String) = TextView(this).apply {
    text = label
    gravity = Gravity.CENTER
    textSize = 15f
    setTextColor(getColor(R.color.widget_text))
  }

  private fun selectTab(selected: TextView) {
    homeTab.setBackgroundColor(if (selected === homeTab) Colorish.card else getColor(R.color.widget_bg))
    listTab.setBackgroundColor(if (selected === listTab) Colorish.card else getColor(R.color.widget_bg))
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

  override fun onBackPressed() {
    if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
    else super.onBackPressed()
  }

  companion object {
    const val EXTRA_URL = "io.easya.kickstart.EXTRA_URL"
  }
}

private object Colorish {
  val card: Int = android.graphics.Color.rgb(22, 22, 22)
}
