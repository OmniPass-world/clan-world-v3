package world.clan.app.wallet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
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
 * As of 2026-05 `.skr` resolution is intentionally deferred to a
 * follow-up AllDomains / Solana Mobile integration. Likely options:
 *
 *  - AllDomains SDK / RPC lookup for the owner pubkey.
 *  - A Solana Mobile first-party endpoint if one becomes available.
 *  - Third-party Solana indexer support for the `.skr` TLD.
 *
 * Until Liam supplies a working endpoint or program ID, [resolveSkr]
 * returns null and the cascade falls through to `.sol`. The wallet pill
 * still renders correctly — just without the Seeker-verified glow for
 * `.skr` holders.
 *
 * ───────────────────────────────────────────────────────────────────────
 *  .sol — Bonfida SNS REST endpoint
 * ───────────────────────────────────────────────────────────────────────
 * Bonfida runs a public primary-domain endpoint:
 *   GET https://sns-api.bonfida.com/v2/user/fav-domains/<pubkey>
 *
 * Successful response shape (as of 2026-05):
 *   { "<pubkey>": "<name>" }
 * Failure / no primary name:
 *   {}
 *
 * We treat any missing key or empty string as "no .sol name." The
 * returned name usually does NOT include the `.sol` suffix; we append it
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

  private const val SNS_PRIMARY_DOMAINS_BASE = "https://sns-api.bonfida.com/v2/user/fav-domains"

  /**
   * Resolve a pubkey to a `.skr` handle. Currently unimplemented — see
   * file header. Returns null so the cascade falls through.
   */
  @Suppress("UNUSED_PARAMETER")
  suspend fun resolveSkr(pubkeyBase58: String): String? = null

  /**
   * Resolve a pubkey to a `.sol` handle via Bonfida's primary-domain endpoint.
   * Returns null on any failure (network error, no name, malformed
   * response). Suffix `.sol` is appended to the name before return.
   */
  suspend fun resolveSol(pubkeyBase58: String): String? = withContext(Dispatchers.IO) {
    runCatching {
      val url = "$SNS_PRIMARY_DOMAINS_BASE/$pubkeyBase58"
      val request = Request.Builder().url(url).get().build()
      http.newCall(request).execute().use { resp ->
        if (!resp.isSuccessful) return@use null
        val text = resp.body?.string().orEmpty()
        parsePrimarySolDomain(pubkeyBase58, text)
      }
    }.getOrNull()
  }

  internal fun parsePrimarySolDomain(pubkeyBase58: String, body: String): String? =
    runCatching {
      if (body.isBlank()) return@runCatching null
      val raw = json.parseToJsonElement(body)
        .jsonObject[pubkeyBase58]
        ?.jsonPrimitive
        ?.contentOrNull
        ?.trim()
        ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
      raw?.let { if (it.endsWith(".sol", ignoreCase = true)) it else "$it.sol" }
    }.getOrNull()

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
