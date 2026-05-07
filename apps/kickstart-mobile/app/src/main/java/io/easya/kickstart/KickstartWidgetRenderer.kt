package io.easya.kickstart

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import kotlin.math.max
import kotlin.math.min

object KickstartWidgetRenderer {
  private val GREEN = Color.rgb(83, 226, 143)
  private val RED = Color.rgb(255, 104, 119)
  private val MUTED = Color.rgb(143, 138, 123)
  private val GOLD = Color.rgb(245, 197, 66)

  fun render(
    context: Context,
    token: KickstartToken?,
    layoutRes: Int = R.layout.gold_widget,
    chartType: String = "candles",
    coinBitmap: Bitmap? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, layoutRes)
    views.setTextViewText(R.id.symbol, token?.symbol ?: "PICK")
    views.setTextViewText(R.id.price, token?.let { formatPrice(it.usdPrice) } ?: "$--")
    if (layoutRes == R.layout.gold_widget_tall) {
      views.setTextViewText(R.id.project_name, token?.name ?: "Choose a token")
    }
    views.setChange(R.id.change_1h, "1H", token?.priceChange1h)
    views.setChange(R.id.change_6h, "6H", token?.priceChange6h)
    views.setChange(R.id.change_24h, "24H", token?.priceChange24h)
    views.setChange(R.id.change_7d, "7D", token?.priceChange7d)
    coinBitmap?.let { views.setImageViewBitmap(R.id.coin, it) }
    views.setImageViewBitmap(R.id.sparkline, drawChart(token, chartType))
    return views
  }

  private fun RemoteViews.setChange(id: Int, label: String, value: Double?) {
    setTextViewText(id, "${formatChange(value)} $label")
    setTextColor(id, when {
      value == null -> MUTED
      value > 0.0 -> GREEN
      value < 0.0 -> RED
      else -> MUTED
    })
  }

  private fun formatPrice(price: Double): String {
    return when {
      price >= 1.0 -> "$" + "%,.2f".format(price)
      price >= 0.01 -> "$" + "%.4f".format(price)
      else -> "$" + "%.8f".format(price).trimEnd('0').trimEnd('.')
    }
  }

  private fun formatChange(value: Double?): String {
    if (value == null) return "--"
    val sign = if (value > 0) "+" else ""
    return "$sign${"%.1f".format(value)}%"
  }

  private fun drawChart(token: KickstartToken?, chartType: String): Bitmap {
    return if (chartType == "candles") drawCandles(token) else drawSparkline(token)
  }

  private fun drawSparkline(token: KickstartToken?): Bitmap {
    val width = 360
    val height = 120
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val points = sparklinePrices(token)
    if (points.size < 2) return bitmap
    val minPrice = points.minOrNull() ?: return bitmap
    val maxPrice = points.maxOrNull() ?: return bitmap
    val span = max(maxPrice - minPrice, 0.0000000001)
    val pad = 10f
    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = if ((token?.priceChange24h ?: 0.0) < 0.0) RED else GREEN
      strokeWidth = 5f
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeJoin = Paint.Join.ROUND
    }
    val glow = Paint(line).apply {
      color = GOLD
      alpha = 42
      strokeWidth = 12f
    }
    fun x(index: Int) = pad + (width - pad * 2) * (index.toFloat() / (points.size - 1).toFloat())
    fun y(value: Double): Float {
      val normalized = ((value - minPrice) / span).toFloat()
      return (height - pad) - ((height - pad * 2) * normalized)
    }
    for (i in 0 until points.lastIndex) {
      canvas.drawLine(x(i), y(points[i]), x(i + 1), y(points[i + 1]), glow)
      canvas.drawLine(x(i), y(points[i]), x(i + 1), y(points[i + 1]), line)
    }
    return bitmap
  }

  private fun sparklinePrices(token: KickstartToken?): List<Double> {
    if (token == null) return emptyList()
    val sampled = token.sparkline24h.map { it.price }.filter { it.isFinite() && it > 0.0 }
    if (sampled.size >= 2) return sampled
    val change = token.priceChange24h ?: return listOf(token.usdPrice, token.usdPrice)
    val start = token.usdPrice / max(0.01, 1.0 + change / 100.0)
    return listOf(start, (start + token.usdPrice) / 2.0, token.usdPrice).map { max(min(it, Double.MAX_VALUE), 0.0) }
  }

  private fun drawCandles(token: KickstartToken?): Bitmap {
    val width = 360
    val height = 120
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val candles = token?.sparkline24h
      ?.mapNotNull { point ->
        val open = point.open ?: point.price
        val high = point.high ?: max(open, point.price)
        val low = point.low ?: min(open, point.price)
        val close = point.close ?: point.price
        if (open.isFinite() && high.isFinite() && low.isFinite() && close.isFinite() && high > 0.0 && low > 0.0) {
          Candle(open, high, low, close)
        } else null
      }
      ?: emptyList()
    if (candles.size < 2) return drawSparkline(token)
    val minPrice = candles.minOf { it.low }
    val maxPrice = candles.maxOf { it.high }
    val span = max(maxPrice - minPrice, 0.0000000001)
    val pad = 8f
    val candleWidth = max(2f, ((width - pad * 2) / candles.size.toFloat()) * 0.56f)
    val wick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      strokeWidth = 2f
      strokeCap = Paint.Cap.ROUND
    }
    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
    }
    fun x(index: Int) = pad + (width - pad * 2) * ((index + 0.5f) / candles.size.toFloat())
    fun y(value: Double): Float {
      val normalized = ((value - minPrice) / span).toFloat()
      return (height - pad) - ((height - pad * 2) * normalized)
    }
    candles.forEachIndexed { index, candle ->
      val color = if (candle.close >= candle.open) GREEN else RED
      wick.color = color
      body.color = color
      val cx = x(index)
      canvas.drawLine(cx, y(candle.high), cx, y(candle.low), wick)
      val top = min(y(candle.open), y(candle.close))
      val bottom = max(y(candle.open), y(candle.close))
      canvas.drawRoundRect(cx - candleWidth / 2f, top, cx + candleWidth / 2f, max(bottom, top + 2f), 1.5f, 1.5f, body)
    }
    return bitmap
  }

  private data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
  )
}
