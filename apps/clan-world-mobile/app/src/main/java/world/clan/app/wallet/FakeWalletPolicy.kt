package world.clan.app.wallet

import java.util.Locale

internal class FakeWalletBlockedException : RuntimeException(FakeWalletPolicy.BLOCKED_MESSAGE)

object FakeWalletPolicy {
  const val BLOCKED_MESSAGE =
    "Test wallet is not supported in release builds. Choose Phantom, Solflare, or Seed Vault Wallet."

  // The MWA SDK launches Android's implicit solana-wallet chooser internally,
  // so release builds enforce this after wallet authorization metadata returns.
  fun shouldBlock(
    allowFakeWallets: Boolean,
    walletUriBase: String?,
  ): Boolean {
    if (allowFakeWallets) return false
    return isFakeWallet(walletUriBase)
  }

  fun isFakeWallet(walletUriBase: String?): Boolean =
    listOfNotNull(walletUriBase)
      .map(::normalize)
      .any { value ->
        value.contains("fakewallet") || value.contains("fakerwallet")
      }

  private fun normalize(value: String): String =
    value.lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)
}
