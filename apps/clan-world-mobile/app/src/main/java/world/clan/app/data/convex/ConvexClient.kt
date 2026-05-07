package world.clan.app.data.convex

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import world.clan.app.BuildConfig
import java.util.concurrent.TimeUnit

/**
 * Tiny HTTP client for Convex's public `POST /api/query` endpoint.
 *
 *   POST {CONVEX_URL}/api/query
 *   {"path":"module:function","args":{...},"format":"json"}
 *
 * Success body: `{"status":"success","value":<T>}`
 * Error body:   `{"code":"…","message":"…"}` — caught here as Result.failure.
 *
 * No auth — all the queries the cockpit uses are public.
 */
object ConvexClient {

  private val http: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
  }

  private val MediaJson = "application/json".toMediaType()

  private val endpoint: String
    get() = "${BuildConfig.CONVEX_URL.trimEnd('/')}/api/query"

  /** Generic POST — returns parsed `value` field on success. */
  suspend fun <T> query(
    path: String,
    args: JsonObject = JsonObject(emptyMap()),
    valueDeserializer: KSerializer<T>,
  ): Result<T> = runCatching {
    val body = buildJsonObject {
      put("path", path)
      put("args", args)
      put("format", "json")
    }
    val request = Request.Builder()
      .url(endpoint)
      .post(body.toString().toRequestBody(MediaJson))
      .build()

    http.newCall(request).execute().use { resp ->
      val text = resp.body?.string().orEmpty()
      if (!resp.isSuccessful) {
        error("HTTP ${resp.code}: ${text.take(200)}")
      }
      val root = json.parseToJsonElement(text).jsonObject
      val status = root["status"]?.jsonPrimitive?.contentOrNull
      if (status != "success") {
        val msg = root["errorMessage"]?.jsonPrimitive?.contentOrNull
          ?: root["message"]?.jsonPrimitive?.contentOrNull
          ?: text.take(200)
        error("Convex query error: $msg")
      }
      val valueElement = root["value"]
        ?: error("Missing `value` field in Convex response")
      json.decodeFromJsonElement(valueDeserializer, valueElement)
    }
  }

  // ---- typed wrappers --------------------------------------------------

  suspend fun fetchSnapshot(): Result<SnapshotWire> =
    query(
      path = "getSnapshot:getSnapshot",
      valueDeserializer = SnapshotWire.serializer(),
    )

  suspend fun fetchVaultMovements(clanId: Int): Result<List<VaultMovementWire>> =
    query(
      path = "vault:getVaultMovements",
      args = clanIdArgs(clanId),
      valueDeserializer = ListSerializer(VaultMovementWire.serializer()),
    )

  suspend fun fetchClansmen(clanId: Int): Result<List<ClansmanWire>> =
    query(
      path = "clansmen:getClanClansmen",
      args = clanIdArgs(clanId),
      valueDeserializer = ListSerializer(ClansmanWire.serializer()),
    )

  suspend fun fetchMemoryKv(clanId: Int): Result<List<MemoryKvWire>> =
    query(
      path = "memory:getByClan",
      args = clanIdArgs(clanId),
      valueDeserializer = ListSerializer(MemoryKvWire.serializer()),
    )

  suspend fun fetchMemoryEvents(clanId: Int): Result<List<MemoryEventWire>> =
    query(
      path = "memory:getEventsByClan",
      args = clanIdArgs(clanId),
      valueDeserializer = ListSerializer(MemoryEventWire.serializer()),
    )

  suspend fun fetchBulletins(clanId: Int): Result<List<BulletinWire>> =
    query(
      path = "bulletins:getByClan",
      args = clanIdArgs(clanId),
      valueDeserializer = ListSerializer(BulletinWire.serializer()),
    )

  suspend fun fetchCombinedComms(clanId: Int): Result<List<CombinedCommsWire>> =
    query(
      path = "comms:getCombinedComms",
      args = clanIdArgs(clanId),
      valueDeserializer = ListSerializer(CombinedCommsWire.serializer()),
    )

  private fun clanIdArgs(clanId: Int): JsonObject = buildJsonObject {
    put("clanId", clanId)
  }
}
