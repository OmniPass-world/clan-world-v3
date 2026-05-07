package world.clan.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * One row in the user's local lineage — the things they've signed in this
 * install. Demo-only persistence (EncryptedSharedPreferences); no on-chain
 * source-of-truth yet.
 *
 * `kind` discriminates which flow produced the row:
 *   - "forged"      Forge wizard mint
 *   - "hired"       Bazaar HireModal seal
 *   - "whispered"   SteeringConsole compose
 *   - "sealed"      StrategyEditor doctrine save
 */
@Serializable
data class LineageEntry(
  val kind: String,
  val clanId: Int,
  val title: String,
  val subtitle: String? = null,
  val tsEpochMs: Long = System.currentTimeMillis(),
)

/**
 * Append-only local log of the user's owner actions. Backed by the same
 * EncryptedSharedPreferences file as SessionStore — distinct file is
 * unnecessary because the data is just-as-private and the lifetime
 * matches.
 *
 * Capped at 64 entries; older entries roll off when full so the prefs
 * blob stays small.
 */
class LineageStore(context: Context) {

  private val prefs by lazy {
    val masterKey = MasterKey.Builder(context)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    EncryptedSharedPreferences.create(
      context,
      "clan-world-lineage.enc",
      masterKey,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
  }

  private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun read(): List<LineageEntry> {
    val raw = prefs.getString("entries", null) ?: return emptyList()
    return runCatching { json.decodeFromString<List<LineageEntry>>(raw) }
      .getOrElse { emptyList() }
  }

  fun append(entry: LineageEntry) {
    val current = read()
    val next = (listOf(entry) + current).take(MAX_ENTRIES)
    prefs.edit().putString("entries", json.encodeToString<List<LineageEntry>>(next)).apply()
  }

  fun clear() {
    prefs.edit().clear().apply()
  }

  companion object {
    const val MAX_ENTRIES = 64
  }
}
