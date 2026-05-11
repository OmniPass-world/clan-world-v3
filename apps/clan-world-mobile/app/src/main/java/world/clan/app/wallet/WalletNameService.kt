package world.clan.app.wallet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Resolves Solana wallet pubkeys → human-readable handles for the
 * wallet pill cascade in [WalletIdentity].
 *
 * Cascade priority (already in [WalletIdentity.displayName]):
 *   1. `.skr` (Seeker)        — see [resolveSkr]
 *   2. `.sol` (Bonfida SNS)   — see [resolveSol]
 *   3. wallet label / truncated pubkey — fallback in [WalletIdentity]
 *
 * ───────────────────────────────────────────────────────────────────────
 *  .skr — Seeker Name Service — UNRESOLVED
 * ───────────────────────────────────────────────────────────────────────
 * As of 2026-05 there is no public REST endpoint or first-party SDK we
 * have been able to identify for reverse-resolving an arbitrary Solana
 * pubkey to a `.skr` handle. Likely options for follow-up work:
 *
 *  - Direct Solana RPC `getProgramAccounts` against the Seeker Name
 *    Service program ID, filtered on the parent name = `.skr` TLD hash.
 *    Requires knowing the program ID + the TLD root account — neither is
 *    documented in this codebase, nor in Bonfida's `@bonfida/spl-name-
 *    service` SDK at the time of writing.
 *  - Third-party Solana indexer (Helius, Shyft, Solscan) that has
 *    indexed the `.skr` TLD specifically. None confirmed to exist.
 *  - A Seeker-first-party REST endpoint. If one exists it is not
 *    public-facing.
 *
 * Until Liam supplies a working endpoint or program ID, [resolveSkr]
 * returns null and the cascade falls through to `.sol`. The wallet pill
 * still renders correctly — just without the Seeker-verified glow for
 * `.skr` holders.
 *
 * ───────────────────────────────────────────────────────────────────────
 *  .sol — Bonfida SNS REST endpoint
 * ───────────────────────────────────────────────────────────────────────
 * Bonfida runs a public reverse-lookup endpoint:
 *   GET https://sns-sdk.bonfida.org/reverse/<pubkey>
 *
 * Successful response shape (as of 2026-05):
 *   { "s": "ok", "result": "<name>" }      // a single primary name
 *   { "s": "ok", "result": ["<name>", …] } // sometimes an array
 * Failure / no name:
 *   { "s": "error", "result": "..." }      // or non-2xx
 *
 * We treat any non-"ok" status or empty string as "no .sol name."
 * The returned name does NOT include the `.sol` suffix; we append it
 * before storing on [WalletIdentity.solName].
 */
object WalletNameService {

  private val http: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(8, TimeUnit.SECONDS)
    .build()

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
  }

  private const val BONFIDA_REVERSE_BASE = "https://sns-sdk.bonfida.org/reverse"

  /**
   * Resolve a pubkey to a `.skr` handle. Currently unimplemented — see
   * file header. Returns null so the cascade falls through.
   */
  @Suppress("UNUSED_PARAMETER")
  suspend fun resolveSkr(pubkeyBase58: String): String? = null

  /**
   * Resolve a pubkey to a `.sol` handle via Bonfida's reverse endpoint.
   * Returns null on any failure (network error, no name, malformed
   * response). Suffix `.sol` is appended to the name before return.
   */
  suspend fun resolveSol(pubkeyBase58: String): String? = withContext(Dispatchers.IO) {
    runCatching {
      val url = "$BONFIDA_REVERSE_BASE/$pubkeyBase58"
      val request = Request.Builder().url(url).get().build()
      http.newCall(request).execute().use { resp ->
        if (!resp.isSuccessful) return@use null
        val text = resp.body?.string().orEmpty()
        if (text.isBlank()) return@use null
        val root = json.parseToJsonElement(text).jsonObject
        val status = root["s"]?.jsonPrimitive?.content
        if (status != "ok") return@use null
        val result = root["result"] ?: return@use null
        val name = when {
          result is JsonPrimitive -> result.content
          // Some Bonfida responses return an array of names; take the first.
          result.toString().startsWith("[") -> {
            val arr = result.jsonArray
            arr.firstOrNull()?.jsonPrimitive?.content
          }
          // Object-shape fallback (e.g. {"name": "...", ...}).
          else -> (result as? JsonObject)
            ?.get("name")?.jsonPrimitive?.content
        }
        name?.takeIf { it.isNotBlank() }?.let { "$it.sol" }
      }
    }.getOrNull()
  }

  /**
   * Run the full cascade for [session], producing a fully-resolved
   * [WalletIdentity]. Network calls run sequentially — `.skr` first,
   * then `.sol` only if `.skr` returned null. Both calls have short
   * timeouts so the worst-case wait is bounded ~13s (5s + 8s).
   *
   * On any failure the cascade still returns a valid identity: the
   * truncated pubkey + wallet label fallback in [WalletIdentity] is
   * always available.
   */
  suspend fun resolve(session: world.clan.app.data.Session): WalletIdentity {
    val pubkey = session.solanaPubkeyBase58
    val skr = resolveSkr(pubkey)
    val sol = if (skr == null) resolveSol(pubkey) else null
    return WalletIdentity(
      pubkeyBase58 = pubkey,
      skrName = skr,
      solName = sol,
      walletLabel = session.walletLabel,
    )
  }
}
