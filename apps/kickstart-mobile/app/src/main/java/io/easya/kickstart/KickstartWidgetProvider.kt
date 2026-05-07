package io.easya.kickstart

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import org.json.JSONObject

open class KickstartWidgetProvider : AppWidgetProvider() {
  protected open val layoutRes: Int = R.layout.gold_widget

  override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
    appWidgetIds.forEach { appWidgetId ->
      updateWidget(context, manager, appWidgetId, WidgetStore.loadCachedToken(context, appWidgetId), layoutRes)
      Thread {
        val mint = WidgetStore.getWidgetTokenMint(context, appWidgetId)
        val fresh = mint?.let { runCatching { KickstartClient.fetchToken(it) }.getOrNull() }
        if (fresh != null) WidgetStore.saveCachedToken(context, appWidgetId, fresh)
        updateWidget(context, manager, appWidgetId, fresh ?: WidgetStore.loadCachedToken(context, appWidgetId), layoutRes)
      }.start()
    }
  }

  companion object {
    fun updateWidget(
      context: Context,
      manager: AppWidgetManager,
      appWidgetId: Int,
      token: KickstartToken?,
      layoutRes: Int,
    ) {
      val targetUrl = token?.let { "${BuildConfig.HOME_URL.trimEnd('/')}/token/${it.tokenMint}" } ?: BuildConfig.HOME_URL
      val clickIntent = Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_URL, targetUrl)
      val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        clickIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
      val views = KickstartWidgetRenderer.render(context, token, layoutRes)
      views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
      manager.updateAppWidget(appWidgetId, views)
    }
  }
}

class KickstartTallWidgetProvider : KickstartWidgetProvider() {
  override val layoutRes: Int = R.layout.gold_widget_tall
}

fun widgetLayoutForProvider(providerClassName: String?): Int {
  return if (providerClassName?.contains("Tall") == true) R.layout.gold_widget_tall else R.layout.gold_widget
}

object WidgetStore {
  private const val PREFS = "kickstart_widgets"

  fun setWidgetTokenMint(context: Context, appWidgetId: Int, tokenMint: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putString("widget_${appWidgetId}_mint", tokenMint)
      .apply()
  }

  fun getWidgetTokenMint(context: Context, appWidgetId: Int): String? {
    return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .getString("widget_${appWidgetId}_mint", null)
  }

  fun saveCachedToken(context: Context, appWidgetId: Int, token: KickstartToken) {
    val sparkline = token.sparkline24h.fold(org.json.JSONArray()) { arr, point ->
      arr.put(JSONObject().put("price", point.price).put("observedAt", point.observedAt))
    }
    val payload = JSONObject()
      .put("tokenMint", token.tokenMint)
      .put("poolAddress", token.poolAddress)
      .put("name", token.name)
      .put("symbol", token.symbol)
      .put("iconUrl", token.iconUrl)
      .put("usdPrice", token.usdPrice)
      .put("mcap", token.mcap)
      .put("liquidity", token.liquidity)
      .put("volume24h", token.volume24h)
      .put("priceChange1h", token.priceChange1h)
      .put("priceChange6h", token.priceChange6h)
      .put("priceChange24h", token.priceChange24h)
      .put("priceChange7d", token.priceChange7d)
      .put("rank", token.rank)
      .put("sparkline24h", sparkline)
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putString("widget_${appWidgetId}_token", payload.toString())
      .apply()
  }

  fun loadCachedToken(context: Context, appWidgetId: Int): KickstartToken? {
    val cached = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .getString("widget_${appWidgetId}_token", null) ?: return null
    return runCatching { KickstartClient.parseToken(JSONObject(cached)) }.getOrNull()
  }
}
