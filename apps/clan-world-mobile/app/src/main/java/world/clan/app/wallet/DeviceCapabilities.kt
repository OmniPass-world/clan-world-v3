package world.clan.app.wallet

import android.content.Context
import android.os.Build

/**
 * What we know about the host device. The authoritative "is this a Seeker?"
 * signal is whether the Seed Vault feature is available on the package
 * manager — manufacturer/model strings are only used for human-readable
 * labels on the Codex screen.
 */
data class DeviceClass(
  val isSeeker: Boolean,
  val seedVaultAvailable: Boolean,
  val manufacturer: String,
  val model: String,
  val androidRelease: String = "",
) {

  /** Compact "Model · Android NN" line for debug surfaces (Codex). */
  val buildLine: String
    get() {
      val device = model.ifBlank { manufacturer }.takeIf { it.isNotBlank() }
      val android = androidRelease.takeIf { it.isNotBlank() }?.let { "Android $it" }
      return listOfNotNull(device, android).joinToString(" · ")
    }

  /** Display label for the Codex device chip. */
  val displayLabel: String
    get() = when {
      seedVaultAvailable -> "Seeker · Seed Vault"
      isSeeker -> "Solana Mobile · ${model.ifBlank { manufacturer }}"
      else -> "Standard Android"
    }

  val description: String
    get() = when {
      seedVaultAvailable ->
        "on-device key custody detected. signing happens in silicon, never in software."
      isSeeker ->
        "Seeker hardware detected, but Seed Vault is not active for this user."
      else ->
        "Seed Vault not available — MWA will route to any installed Solana wallet."
    }
}

object DeviceCapabilities {
  private const val SEED_VAULT_FEATURE = "com.solanamobile.seedvault"

  fun inspect(context: Context): DeviceClass {
    val pm = context.packageManager
    val seedVaultAvailable = pm.hasSystemFeature(SEED_VAULT_FEATURE)
    val manufacturer = Build.MANUFACTURER ?: "unknown"
    val model = Build.MODEL ?: "unknown"
    val seekerByManufacturer =
      manufacturer.equals("Solana Mobile", ignoreCase = true) ||
      manufacturer.equals("OSOM", ignoreCase = true) ||
      model.contains("Seeker", ignoreCase = true) ||
      model.contains("Saga", ignoreCase = true)
    val isSeeker = seedVaultAvailable || seekerByManufacturer
    return DeviceClass(
      isSeeker = isSeeker,
      seedVaultAvailable = seedVaultAvailable,
      manufacturer = manufacturer,
      model = model,
      androidRelease = Build.VERSION.RELEASE ?: "",
    )
  }
}
