package io.easya.kickstart

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object KickstartClient {
  fun listTokens(): List<KickstartToken> {
    val value = callConvexQuery("kickstart:listKickstartTokens", "{}") as? JSONArray ?: JSONArray()
    return buildList {
      for (i in 0 until value.length()) {
        value.optJSONObject(i)?.let { add(parseToken(it)) }
      }
    }
  }

  fun fetchToken(tokenMint: String): KickstartToken? {
    val args = JSONObject().put("tokenMint", tokenMint)
    val value = callConvexQuery("kickstart:getKickstartTokenQuote", args.toString()) as? JSONObject
    return value?.let { parseToken(it) }
  }

  fun watchToken(tokenMint: String) {
    val args = JSONObject().put("tokenMint", tokenMint)
    runCatching { callConvexAction("kickstart:watchAndRefreshToken", args.toString()) }
  }

  private fun callConvexQuery(path: String, argsJson: String): Any? {
    return callConvex("/api/query", path, argsJson)
  }

  private fun callConvexAction(path: String, argsJson: String): Any? {
    return callConvex("/api/action", path, argsJson)
  }

  private fun callConvex(route: String, path: String, argsJson: String): Any? {
    val endpoint = URL("${BuildConfig.CONVEX_URL.trimEnd('/')}$route")
    val connection = endpoint.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = 8_000
    connection.readTimeout = 10_000
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")

    OutputStreamWriter(connection.outputStream).use { writer ->
      writer.write("""{"path":"$path","args":$argsJson,"format":"json"}""")
    }

    val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
      .bufferedReader()
      .use(BufferedReader::readText)

    if (connection.responseCode !in 200..299) {
      throw IllegalStateException("Convex call failed: ${connection.responseCode} $body")
    }

    val root = JSONObject(body)
    if (root.optString("status") != "success") {
      throw IllegalStateException("Convex function failed: $body")
    }
    return root.opt("value")
  }

  fun parseToken(value: JSONObject): KickstartToken {
    val sparkline = value.optJSONArray("sparkline24h") ?: JSONArray()
    val points = buildList {
      for (i in 0 until sparkline.length()) {
        val point = sparkline.optJSONObject(i) ?: continue
        val price = point.optDouble("price", Double.NaN)
        if (price.isFinite()) add(SparkPoint(price, point.optLong("observedAt", 0L)))
      }
    }

    return KickstartToken(
      tokenMint = value.getString("tokenMint"),
      poolAddress = value.optString("poolAddress"),
      name = value.optString("name"),
      symbol = value.optString("symbol"),
      iconUrl = value.nullableString("iconUrl"),
      usdPrice = value.getDouble("usdPrice"),
      mcap = value.optDouble("mcap", 0.0),
      liquidity = value.nullableDouble("liquidity"),
      volume24h = value.nullableDouble("volume24h"),
      priceChange1h = value.nullableDouble("priceChange1h"),
      priceChange6h = value.nullableDouble("priceChange6h"),
      priceChange24h = value.nullableDouble("priceChange24h"),
      priceChange7d = value.nullableDouble("priceChange7d"),
      rank = value.optInt("rank", 0),
      sparkline24h = points,
    )
  }

  private fun JSONObject.nullableDouble(name: String): Double? {
    if (!has(name) || isNull(name)) return null
    val value = optDouble(name, Double.NaN)
    return value.takeIf { it.isFinite() }
  }

  private fun JSONObject.nullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
  }
}
