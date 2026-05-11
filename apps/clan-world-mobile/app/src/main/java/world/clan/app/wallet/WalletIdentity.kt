package world.clan.app.wallet

import world.clan.app.data.Session

/**
 * Display-ready identity for the connected wallet.
 *
 * Resolution priority for [displayName]:
 *   1. `.skr` name      (Seeker-verified handle, glowing border)
 *   2. `.sol` name      (Bonfida SNS reverse-lookup)
 *   3. wallet label     (e.g. Phantom's account nickname like "Trading")
 *   4. truncated pubkey (e.g. "9pK4 · 7vQy")
 *
 * Name resolution is async — see [WalletNameService]. [fromSession]
 * produces a fallback identity with `skrName = solName = null`, suitable
 * for synchronous initial render. [App.walletNameCache] holds the fully
 * resolved identity, written by [ConnectViewModel] after a successful
 * authorize. The cache is keyed by base58 pubkey.
 */
data class WalletIdentity(
  val pubkeyBase58: String,
  val skrName: String? = null,
  val solName: String? = null,
  /** Account nickname provided by the wallet itself (MWA `accounts[0].accountLabel`). */
  val walletLabel: String? = null,
) {
  /** What the pill renders. */
  val displayName: String
    get() = skrName
      ?: solName
      ?: walletLabel?.takeIf { it.isNotBlank() }
      ?: pubkeyBase58.shortenPubkey()

  /**
   * Subtitle: the small label above the display name. Communicates which
   * resolution path matched.
   */
  val subtitle: String
    get() = when {
      skrName != null -> "Seeker · verified"
      solName != null -> "SNS · resolved"
      !walletLabel.isNullOrBlank() -> "Wallet"
      else -> "Connected"
    }

  /** True when the user holds a Seeker `.skr` name. Drives the rotating glow border. */
  val isSeekerVerified: Boolean
    get() = skrName != null

  companion object {
    /**
     * Build a fallback WalletIdentity from a persisted Session.
     * `.skr` and `.sol` names are left null — real resolution is async
     * and happens via [WalletNameService.resolve], driven by
     * [world.clan.app.viewmodel.ConnectViewModel] on successful
     * authorize. Returns null when no session exists.
     */
    fun fromSession(session: Session?): WalletIdentity? {
      val s = session ?: return null
      return WalletIdentity(
        pubkeyBase58 = s.solanaPubkeyBase58,
        skrName = null,
        solName = null,
        walletLabel = s.walletLabel,
      )
    }
  }
}

/** Truncate a Base58 pubkey for header display ("9pK4 · 7vQy"). */
fun String.shortenPubkey(): String {
  if (length < 9) return this
  return "${take(4)} · ${takeLast(4)}"
}
