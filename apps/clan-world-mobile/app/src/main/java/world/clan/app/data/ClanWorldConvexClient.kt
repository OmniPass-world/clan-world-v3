package world.clan.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ConvexException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Convex-over-HTTP client for the Clan World mobile app.
 *
 * Wire format mirrors `apps/kickstart-mobile/.../KickstartClient.kt`:
 *   POST $convexUrl/api/query
 *   { "path": "<module>:<function>", "args": {…}, "format": "json" }
 *
 * Differs from the kickstart reference in that we standardize on OkHttp +
 * kotlinx.serialization (compile-time guarantees on the heterogenous iNFT
 * / comms / vault payloads).
 *
 * Slice 1 surfaces 5 read methods. No mutations are wired — slice 1 is a
 * UI demo and the only mutating action (MWA wallet authorization) is
 * handled by the wallet/MwaClient.kt layer.
 */
class ClanWorldConvexClient(private val convexUrl: String) {

  private val http = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()
  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
  }
  private val mediaType = "application/json".toMediaType()

  // ── Public surface ─────────────────────────────────────────────────────

  suspend fun getSnapshot(): WorldSnapshot =
    callQuery("getSnapshot:getSnapshot", buildJsonObject { })

  suspend fun getInftDemoState(clanId: Int): InftDemoState =
    callQuery("inft:getInftDemoState", buildJsonObject { put("clanId", clanId) })

  suspend fun getCombinedComms(clanId: Int, limit: Int = 40): List<CombinedComm> =
    callQuery("comms:getCombinedComms", buildJsonObject {
      put("clanId", clanId); put("limit", limit)
    })

  suspend fun getVaultMovements(clanId: Int, limit: Int = 24): List<VaultMovement> =
    callQuery("vault:getVaultMovements", buildJsonObject {
      put("clanId", clanId); put("limit", limit)
    })

  suspend fun getBulletinsByClan(clanId: Int, limit: Int = 20): List<Bulletin> =
    callQuery("bulletins:getByClan", buildJsonObject {
      put("clanId", clanId); put("limit", limit)
    })

  suspend fun recordWhisperAfterGoldTx(
    clanId: Int,
    body: String,
    owner: String,
    signature: String,
    skipTax: Long,
  ) {
    callAction<JsonObject>("gold:recordWhisperAfterTx", buildJsonObject {
      put("clanId", clanId)
      put("body", body)
      put("owner", owner)
      put("signature", signature)
      put("skipTax", skipTax)
    })
  }

  suspend fun saveDoctrineAfterGoldTx(
    clanId: Int,
    strategy: String,
    notes: String,
    owner: String,
    signature: String,
  ) {
    callAction<JsonObject>("gold:saveDoctrineAfterTx", buildJsonObject {
      put("clanId", clanId)
      put("strategy", strategy)
      put("notes", notes)
      put("owner", owner)
      put("signature", signature)
    })
  }

  // ── Internals ──────────────────────────────────────────────────────────

  private suspend inline fun <reified T> callQuery(path: String, args: JsonObject): T =
    callConvex("query", path, args)

  private suspend inline fun <reified T> callMutation(path: String, args: JsonObject): T =
    callConvex("mutation", path, args)

  private suspend inline fun <reified T> callAction(path: String, args: JsonObject): T =
    callConvex("action", path, args)

  private suspend inline fun <reified T> callConvex(kind: String, path: String, args: JsonObject): T =
    withContext(Dispatchers.IO) {
      val payload = buildJsonObject {
        put("path", path)
        put("args", args)
        put("format", "json")
      }.toString()

      val req = Request.Builder()
        .url("${convexUrl.trimEnd('/')}/api/$kind")
        .post(payload.toRequestBody(mediaType))
        .build()

      val resp = http.newCall(req).execute()
      val body = resp.body?.string().orEmpty()
      if (!resp.isSuccessful) {
        throw ConvexException("Convex HTTP ${resp.code}: $body")
      }
      val root = runCatching { json.parseToJsonElement(body).jsonObject }
        .getOrElse { throw ConvexException("Convex returned invalid JSON: $body", it) }
      val status = root["status"]?.jsonPrimitive?.contentOrNull
      if (status != "success") {
        throw ConvexException("Convex function $path failed: $body")
      }
      val value = root["value"]
        ?: throw ConvexException("Convex $path returned no `value`: $body")
      val normalized = value.normalizeConvexNumbers()
      runCatching { json.decodeFromJsonElement<T>(normalized) }
        .getOrElse {
          throw ConvexException(
            "Could not decode $path response into ${T::class.simpleName}: $body",
            it,
          )
        }
    }
}
