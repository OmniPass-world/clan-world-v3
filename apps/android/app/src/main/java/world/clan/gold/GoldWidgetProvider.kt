package world.clan.gold

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.json.JSONObject

open class GoldWidgetProvider : AppWidgetProvider() {
  protected open val layoutRes: Int = R.layout.gold_widget

  override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
    updateWidgets(context, manager, appWidgetIds, loadCachedQuote(context), layoutRes)
    Thread {
      val fresh = runCatching { GoldQuoteClient.fetch() }.getOrNull()
      if (fresh != null) saveCachedQuote(context, fresh)
      updateWidgets(context, manager, appWidgetIds, fresh ?: loadCachedQuote(context), layoutRes)
    }.start()
  }

  companion object {
    fun refreshAll(context: Context) {
      val manager = AppWidgetManager.getInstance(context)
      val ids = manager.getAppWidgetIds(ComponentName(context, GoldWidgetProvider::class.java))
      GoldWidgetProvider().onUpdate(context, manager, ids)
    }

    private fun updateWidgets(
      context: Context,
      manager: AppWidgetManager,
      appWidgetIds: IntArray,
      quote: GoldQuote?,
      layoutRes: Int = R.layout.gold_widget,
    ) {
      val clickIntent = Intent(context, MainActivity::class.java)
      val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        clickIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      appWidgetIds.forEach { id ->
        val views = GoldWidgetRenderer.render(context, quote, layoutRes)
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        manager.updateAppWidget(id, views)
      }
    }

    private fun saveCachedQuote(context: Context, quote: GoldQuote) {
      val payload = JSONObject()
        .put("usdPrice", quote.usdPrice)
        .put("priceChange1h", quote.priceChange1h)
        .put("priceChange6h", quote.priceChange6h)
        .put("priceChange24h", quote.priceChange24h)
        .put("priceChange7d", quote.priceChange7d)
        .put("fetchedAt", quote.fetchedAt)
        .put("sparkline24h", quote.sparkline24h.fold(org.json.JSONArray()) { arr, point ->
          arr.put(JSONObject().put("price", point.price).put("observedAt", point.observedAt))
        })
      context.getSharedPreferences("gold_quote", Context.MODE_PRIVATE)
        .edit()
        .putString("latest", payload.toString())
        .apply()
    }

    private fun loadCachedQuote(context: Context): GoldQuote? {
      val cached = context.getSharedPreferences("gold_quote", Context.MODE_PRIVATE)
        .getString("latest", null) ?: return null
      return runCatching { GoldQuoteClient.parseQuote(JSONObject(cached)) }.getOrNull()
    }
  }
}

class GoldTallWidgetProvider : GoldWidgetProvider() {
  override val layoutRes: Int = R.layout.gold_widget_tall
}
