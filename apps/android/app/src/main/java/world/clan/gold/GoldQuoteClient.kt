package world.clan.gold

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GoldQuoteClient {
  fun fetch(): GoldQuote {
    val endpoint = URL("${BuildConfig.CONVEX_URL.trimEnd('/')}/api/query")
    val connection = endpoint.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = 8_000
    connection.readTimeout = 8_000
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")

    OutputStreamWriter(connection.outputStream).use { writer ->
      writer.write("""{"path":"goldQuote:getGoldQuote","args":{},"format":"json"}""")
    }

    val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
      .bufferedReader()
      .use(BufferedReader::readText)

    if (connection.responseCode !in 200..299) {
      throw IllegalStateException("Convex quote fetch failed: ${connection.responseCode} $body")
    }

    val root = JSONObject(body)
    if (root.optString("status") != "success") {
      throw IllegalStateException("Convex quote query failed: $body")
    }

    val value = root.optJSONObject("value") ?: throw IllegalStateException("No GOLD quote has been cached yet")
    return parseQuote(value)
  }

  fun parseQuote(value: JSONObject): GoldQuote {
    val sparkline = value.optJSONArray("sparkline24h") ?: JSONArray()
    val points = buildList {
      for (i in 0 until sparkline.length()) {
        val point = sparkline.optJSONObject(i) ?: continue
        val price = point.optDouble("price", Double.NaN)
        if (price.isFinite()) {
          add(SparkPoint(price, point.optLong("observedAt", 0L)))
        }
      }
    }

    return GoldQuote(
      usdPrice = value.getDouble("usdPrice"),
      priceChange1h = value.nullableDouble("priceChange1h"),
      priceChange6h = value.nullableDouble("priceChange6h"),
      priceChange24h = value.nullableDouble("priceChange24h"),
      priceChange7d = value.nullableDouble("priceChange7d"),
      fetchedAt = value.optLong("fetchedAt", 0L),
      sparkline24h = points,
    )
  }

  private fun JSONObject.nullableDouble(name: String): Double? {
    if (!has(name) || isNull(name)) return null
    val value = optDouble(name, Double.NaN)
    return value.takeIf { it.isFinite() }
  }
}
