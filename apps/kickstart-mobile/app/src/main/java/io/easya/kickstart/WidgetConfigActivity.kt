package io.easya.kickstart

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class WidgetConfigActivity : Activity() {
  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setResult(RESULT_CANCELED)
    appWidgetId = intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID,
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    val scroll = ScrollView(this).apply { setBackgroundColor(getColor(R.color.widget_bg)) }
    val list = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(dp(14), dp(18), dp(14), dp(24))
    }
    scroll.addView(list)
    scroll.setOnApplyWindowInsetsListener { view, insets ->
      @Suppress("DEPRECATION")
      view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
      insets
    }
    setContentView(scroll)
    scroll.requestApplyInsets()
    list.addView(text("Choose token", 22, getColor(R.color.widget_text)))
    list.addView(text("Top Kickstart tokens by market cap", 13, getColor(R.color.widget_muted)))
    list.addView(text("Loading...", 14, getColor(R.color.widget_muted)))

    Thread {
      val tokens = runCatching { KickstartClient.listTokens() }.getOrElse { emptyList() }
      runOnUiThread {
        renderTokenPicker(list, tokens)
      }
    }.start()
  }

  private fun renderTokenPicker(list: LinearLayout, tokens: List<KickstartToken>) {
    list.removeAllViews()
    list.addView(text("Choose token", 22, getColor(R.color.widget_text)))
    list.addView(text("Top Kickstart tokens by market cap", 13, getColor(R.color.widget_muted)))
    val search = EditText(this).apply {
      hint = "Search token"
      textSize = 15f
      setSingleLine(true)
      setTextColor(getColor(R.color.widget_text))
      setHintTextColor(getColor(R.color.widget_muted))
      setBackgroundColor(android.graphics.Color.rgb(22, 22, 22))
      setPadding(dp(10), dp(8), dp(10), dp(8))
    }
    val results = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(0, dp(8), 0, 0)
    }
    fun fill(query: String) {
      val needle = query.trim().lowercase()
      results.removeAllViews()
      tokens
        .filter { token ->
          needle.isBlank() ||
            token.symbol.lowercase().contains(needle) ||
            token.name.lowercase().contains(needle) ||
            token.tokenMint.lowercase().contains(needle)
        }
        .forEach { token -> results.addView(tokenRow(token) { choose(token) }) }
    }
    search.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        fill(s?.toString().orEmpty())
      }
      override fun afterTextChanged(s: Editable?) {}
    })
    list.addView(search, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    list.addView(results)
    fill("")
  }

  private fun choose(token: KickstartToken) {
    WidgetStore.setWidgetTokenMint(this, appWidgetId, token.tokenMint)
    WidgetStore.saveCachedToken(this, appWidgetId, token)
    Thread { KickstartClient.watchToken(token.tokenMint) }.start()
    val manager = AppWidgetManager.getInstance(this)
    val info = manager.getAppWidgetInfo(appWidgetId)
    KickstartWidgetProvider.updateWidget(this, manager, appWidgetId, token, widgetLayoutForProvider(info?.provider?.className))
    setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
    finish()
  }

  private fun tokenRow(token: KickstartToken, onClick: () -> Unit) = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    setPadding(dp(10), dp(12), dp(10), dp(12))
    setOnClickListener { onClick() }
    val left = text("#${token.rank}  ${token.symbol}\n${token.name}", 14, getColor(R.color.widget_text))
    val right = text("$" + "%,.0f".format(token.mcap), 13, getColor(R.color.widget_gold)).apply { gravity = Gravity.END }
    addView(left, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    addView(right)
  }

  private fun text(value: String, size: Int, color: Int) = TextView(this).apply {
    text = value
    textSize = size.toFloat()
    setTextColor(color)
    setPadding(0, dp(6), 0, dp(6))
  }

  private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
