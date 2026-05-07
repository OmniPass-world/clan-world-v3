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
 * In slice 2A the .skr/.sol names are still mocked; the wallet label now
 * comes through from real MWA. Slice 2 (full) will replace the mock with
 * a Solana RPC + SNS lookup.
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
     * Build a WalletIdentity from a persisted Session, applying the
     * slice-1 mock name resolver. Returns null when no session exists.
     */
    fun fromSession(session: Session?): WalletIdentity? {
      val s = session ?: return null
      return WalletIdentity(
        pubkeyBase58 = s.solanaPubkeyBase58,
        skrName = mockSkr(s.solanaPubkeyBase58),
        solName = mockSol(s.solanaPubkeyBase58),
        walletLabel = s.walletLabel,
      )
    }

    /**
     * Mock .skr resolver. Returns a deterministic name for the slice-1
     * demo wallet (so users see the rotating glow), null otherwise.
     */
    private fun mockSkr(pubkey: String): String? {
      // Slice 2 (full) replaces this with a real SNS reverse-record lookup
      // for the .skr TLD. Until then: a deterministic demo handle so the
      // glowing-border state exercises in user testing.
      return "elderkeep.skr"
    }

    private fun mockSol(pubkey: String): String? {
      // No mock — when .skr is null, the wallet label (or truncated
      // pubkey) is what the pill shows.
      return null
    }
  }
}

/** Truncate a Base58 pubkey for header display ("9pK4 · 7vQy"). */
fun String.shortenPubkey(): String {
  if (length < 9) return this
  return "${take(4)} · ${takeLast(4)}"
}
