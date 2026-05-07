package world.clan.gold

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
  private lateinit var webView: WebView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.statusBarColor = getColor(R.color.widget_bg)
    window.navigationBarColor = getColor(R.color.widget_bg)

    val container = FrameLayout(this)
    webView = WebView(this)
    webView.webViewClient = WebViewClient()
    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true
    container.setBackgroundColor(getColor(R.color.widget_bg))
    webView.setBackgroundColor(getColor(R.color.widget_bg))
    container.addView(
      webView,
      FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
    )
    container.setOnApplyWindowInsetsListener { view, insets ->
      @Suppress("DEPRECATION")
      view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
      insets
    }
    setContentView(container)
    container.requestApplyInsets()
    webView.loadUrl(BuildConfig.TOKEN_URL)
  }

  override fun onBackPressed() {
    if (::webView.isInitialized && webView.canGoBack()) {
      webView.goBack()
    } else {
      super.onBackPressed()
    }
  }
}
