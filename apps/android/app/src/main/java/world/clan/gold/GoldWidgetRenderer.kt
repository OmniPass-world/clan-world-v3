package world.clan.gold

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import kotlin.math.max
import kotlin.math.min

object GoldWidgetRenderer {
  private val GREEN = Color.rgb(83, 226, 143)
  private val RED = Color.rgb(255, 104, 119)
  private val MUTED = Color.rgb(143, 138, 123)
  private val GOLD = Color.rgb(245, 197, 66)

  fun render(context: Context, quote: GoldQuote?, layoutRes: Int = R.layout.gold_widget): RemoteViews {
    val views = RemoteViews(context.packageName, layoutRes)
    views.setTextViewText(R.id.price, quote?.let { formatPrice(it.usdPrice) } ?: "$--")
    views.setChange(R.id.change_1h, "1H", quote?.priceChange1h)
    views.setChange(R.id.change_6h, "6H", quote?.priceChange6h)
    views.setChange(R.id.change_24h, "24H", quote?.priceChange24h)
    views.setChange(R.id.change_7d, "7D", quote?.priceChange7d)
    views.setImageViewBitmap(R.id.sparkline, drawSparkline(quote))
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

  private fun drawSparkline(quote: GoldQuote?): Bitmap {
    val width = 360
    val height = 120
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val points = sparklinePrices(quote)
    if (points.size < 2) return bitmap

    val minPrice = points.minOrNull() ?: return bitmap
    val maxPrice = points.maxOrNull() ?: return bitmap
    val span = max(maxPrice - minPrice, 0.0000000001)
    val pad = 10f

    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = if ((quote?.priceChange24h ?: 0.0) < 0.0) RED else GREEN
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
      val x1 = x(i)
      val y1 = y(points[i])
      val x2 = x(i + 1)
      val y2 = y(points[i + 1])
      canvas.drawLine(x1, y1, x2, y2, glow)
      canvas.drawLine(x1, y1, x2, y2, line)
    }

    return bitmap
  }

  private fun sparklinePrices(quote: GoldQuote?): List<Double> {
    if (quote == null) return emptyList()
    val sampled = quote.sparkline24h.map { it.price }.filter { it.isFinite() && it > 0.0 }
    if (sampled.size >= 2) return sampled

    val change = quote.priceChange24h ?: return listOf(quote.usdPrice, quote.usdPrice)
    val divisor = max(0.01, 1.0 + change / 100.0)
    val start = quote.usdPrice / divisor
    val mid = (start + quote.usdPrice) / 2.0
    return listOf(start, mid, quote.usdPrice).map { max(min(it, Double.MAX_VALUE), 0.0) }
  }
}
