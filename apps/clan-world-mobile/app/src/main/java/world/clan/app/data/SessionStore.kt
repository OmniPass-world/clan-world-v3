package world.clan.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * The persisted session (Solana pubkey + MWA auth token + UI state).
 * Slice 1 is Solana-only; there is no EVM seam.
 *
 * `walletLabel` is the human-readable nickname the wallet provides for the
 * connected account (e.g. Phantom returns "Trading"). Surfaced on the
 * wallet pill when no `.skr` / `.sol` name is available.
 */
data class Session(
  val solanaPubkeyBase58: String,
  val mwaAuthToken: String,
  val walletLabel: String? = null,
  val selectedClanId: Int? = null,
  val lastConnectAtEpochMs: Long = 0L,
)

private const val PREFS_FILE        = "clan-world-session.enc"
private const val KEY_SOLANA_PK     = "solanaPubkeyBase58"
private const val KEY_MWA_AUTH      = "mwaAuthToken"
private const val KEY_WALLET_LABEL  = "walletLabel"
private const val KEY_CLAN_ID       = "selectedClanId"
private const val KEY_LAST_CONNECT  = "lastConnectAtEpochMs"

/**
 * EncryptedSharedPreferences wrapper. Keys live in the AndroidKeyStore;
 * `allowBackup="false"` in the manifest prevents cross-device backup of
 * the per-device key. AES256_GCM master key + AES256_SIV (key) /
 * AES256_GCM (value) encryption.
 */
class SessionStore(context: Context) {

  private val prefs by lazy {
    val masterKey = MasterKey.Builder(context)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    EncryptedSharedPreferences.create(
      context,
      PREFS_FILE,
      masterKey,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
  }

  fun read(): Session? {
    val pk = prefs.getString(KEY_SOLANA_PK, null) ?: return null
    val auth = prefs.getString(KEY_MWA_AUTH, null) ?: return null
    return Session(
      solanaPubkeyBase58 = pk,
      mwaAuthToken = auth,
      walletLabel = prefs.getString(KEY_WALLET_LABEL, null),
      selectedClanId = prefs.getInt(KEY_CLAN_ID, -1).takeIf { it >= 0 },
      lastConnectAtEpochMs = prefs.getLong(KEY_LAST_CONNECT, 0L),
    )
  }

  fun save(s: Session) {
    val ed = prefs.edit()
      .putString(KEY_SOLANA_PK, s.solanaPubkeyBase58)
      .putString(KEY_MWA_AUTH, s.mwaAuthToken)
      .putLong(KEY_LAST_CONNECT, s.lastConnectAtEpochMs)
    if (s.walletLabel != null) ed.putString(KEY_WALLET_LABEL, s.walletLabel)
    else ed.remove(KEY_WALLET_LABEL)
    if (s.selectedClanId != null) ed.putInt(KEY_CLAN_ID, s.selectedClanId)
    else ed.remove(KEY_CLAN_ID)
    ed.apply()
  }

  fun saveSelectedClanId(clanId: Int?) {
    val ed = prefs.edit()
    if (clanId != null) ed.putInt(KEY_CLAN_ID, clanId)
    else ed.remove(KEY_CLAN_ID)
    ed.apply()
  }

  fun clear() {
    prefs.edit().clear().apply()
  }

  // ── Form-draft persistence ────────────────────────────────────────────
  //
  // SteeringConsole / StrategyEditor / Forge text inputs survive process
  // death + backgrounding. Keys are namespaced "draft:<scope>:<field>".
  // On successful submit the relevant scope is cleared.

  fun getDraft(scope: String, field: String): String? =
    prefs.getString("draft:$scope:$field", null)

  fun setDraft(scope: String, field: String, value: String) {
    prefs.edit().putString("draft:$scope:$field", value).apply()
  }

  fun clearDrafts(scope: String) {
    val toRemove = prefs.all.keys.filter { it.startsWith("draft:$scope:") }
    if (toRemove.isEmpty()) return
    val ed = prefs.edit()
    toRemove.forEach { ed.remove(it) }
    ed.apply()
  }

  // ── Hired clan IDs ────────────────────────────────────────────────────
  // After a successful Bazaar hire, the clan is added to this set. Hall +
  // Hearth + Codex union LINKED_CLAN_IDS with this set when displaying
  // "your" sigils, so a freshly-hired card appears in the user's hall
  // without a relaunch.

  fun getHiredClanIds(): Set<Int> =
    prefs.getStringSet("hired:clanIds", emptySet())
      ?.mapNotNullTo(mutableSetOf()) { it.toIntOrNull() }
      ?: emptySet()

  fun addHiredClanId(clanId: Int) {
    val current = getHiredClanIds()
    if (clanId in current) return
    val next = (current + clanId).map { it.toString() }.toSet()
    prefs.edit().putStringSet("hired:clanIds", next).apply()
  }

  // ── One-shot UI flags ─────────────────────────────────────────────────
  // Persisted booleans for onboarding hints / coachmarks. Once a flag is
  // marked seen, it stays seen across reinstalls (within EncryptedShared
  // Preferences' device-keystore lifetime).

  fun hasSeenFlag(key: String): Boolean = prefs.getBoolean("flag:$key", false)

  fun markFlagSeen(key: String) {
    prefs.edit().putBoolean("flag:$key", true).apply()
  }
}
