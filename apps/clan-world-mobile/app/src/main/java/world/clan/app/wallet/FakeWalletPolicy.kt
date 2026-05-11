package world.clan.app.wallet

object FakeWalletPolicy {
  const val BLOCKED_MESSAGE =
    "Test wallet is not supported in release builds. Choose Phantom, Solflare, or Seed Vault Wallet."

  // The MWA SDK launches Android's implicit solana-wallet chooser internally,
  // so release builds enforce this after wallet authorization metadata returns.
  fun shouldBlock(
    allowFakeWallets: Boolean,
    walletUriBase: String?,
    walletLabel: String?,
  ): Boolean {
    if (allowFakeWallets) return false
    return isFakeWallet(walletUriBase, walletLabel)
  }

  fun isFakeWallet(walletUriBase: String?, walletLabel: String?): Boolean =
    listOfNotNull(walletUriBase, walletLabel)
      .map(::normalize)
      .any { value ->
        value.contains("fakewallet") || value.contains("fakerwallet")
      }

  private fun normalize(value: String): String =
    value.lowercase().filter(Char::isLetterOrDigit)
}
