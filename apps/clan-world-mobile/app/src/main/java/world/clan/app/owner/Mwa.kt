package world.clan.app.owner

import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.Solana
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import world.clan.app.BuildConfig
import world.clan.app.data.Elder
import world.clan.app.wallet.FakeWalletBlockedException
import world.clan.app.wallet.FakeWalletPolicy

/**
 * Real Mobile Wallet Adapter (Solana) sign-in. The user's installed
 * wallet returns a signed message; we do **not** verify the signature —
 * any successful return is treated as "signed in" per the demo spec.
 *
 * On no-wallet-installed or user-cancelled, returns [SignInResult.Failed].
 */
sealed interface SignInResult {
  data object Success : SignInResult
  data class Failed(val reason: String) : SignInResult
}

object Mwa {

  private val identity = ConnectionIdentity(
    identityUri = Uri.parse("https://clan-world.com"),
    iconUri = Uri.parse("favicon.svg"),
    identityName = "Clan World",
  )

  /**
   * Triggers the wallet hand-off and asks the wallet to sign a per-clan
   * message. Suspends until the user completes or cancels in the wallet
   * app, or an error occurs (no wallet, transport failure, etc.).
   *
   * [sender] MUST be the long-lived ActivityResultSender created in
   * MainActivity.onCreate — constructing one here would call
   * registerForActivityResult on a RESUMED activity and crash the app.
   */
  suspend fun signInAsOwner(
    sender: ActivityResultSender,
    elder: Elder,
  ): SignInResult {
    val mwa = MobileWalletAdapter(connectionIdentity = identity)
    mwa.blockchain = Solana.Devnet

    val message = ("Sign in as Ælder of ${elder.name} (clan ${elder.clanId})")
      .toByteArray(Charsets.UTF_8)

    val outcome: TransactionResult<ByteArray> = mwa.transact(sender) { authResult ->
      if (
        FakeWalletPolicy.shouldBlock(
          allowFakeWallets = BuildConfig.ALLOW_FAKE_WALLETS,
          walletUriBase = authResult.walletUriBase?.toString(),
        )
      ) {
        throw FakeWalletBlockedException()
      }
      val account = authResult.accounts.firstOrNull()
        ?: error("No account returned from wallet authorization")
      val signed = signMessagesDetached(
        messages = arrayOf(message),
        addresses = arrayOf(account.publicKey),
      )
      // We deliberately do NOT verify the signature — any successful return
      // is treated as a successful sign-in per the demo spec.
      signed.messages.firstOrNull()?.signatures?.firstOrNull() ?: ByteArray(0)
    }

    return when (outcome) {
      is TransactionResult.Success -> SignInResult.Success
      is TransactionResult.NoWalletFound   -> SignInResult.Failed("No Solana wallet found")
      is TransactionResult.Failure         -> {
        val message = if (outcome.e is FakeWalletBlockedException) {
          FakeWalletPolicy.BLOCKED_MESSAGE
        } else {
          outcome.e.message ?: "Sign-in failed"
        }
        SignInResult.Failed(message)
      }
    }
  }
}
