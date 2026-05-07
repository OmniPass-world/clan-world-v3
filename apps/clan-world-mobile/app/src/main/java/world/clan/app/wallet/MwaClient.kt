package world.clan.app.wallet

import android.net.Uri
import androidx.activity.ComponentActivity
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.Solana
import com.solana.mobilewalletadapter.clientlib.TransactionResult

/** Result of a connect / reauthorize call. */
data class MwaSession(
  val solanaPubkeyBase58: String,
  val authToken: String,
  val walletUriBase: String?,
  val walletLabel: String?,
)

sealed class MwaResult<out T> {
  data class Ok<T>(val value: T) : MwaResult<T>()
  data object UserDeclined : MwaResult<Nothing>()
  data object WalletNotFound : MwaResult<Nothing>()
  data class Error(val cause: Throwable) : MwaResult<Nothing>()
}

/**
 * Thin wrapper around the Solana Mobile Wallet Adapter Kotlin client
 * (`com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.1.0`).
 *
 * Slice 2A — replaces the slice 1 stub with real authorize/reauthorize/
 * disconnect calls. signMessage is wired but unused; slice 2 (full) will
 * exercise it for the SIWS identity-link challenge.
 *
 * V3 doesn't transact on Solana, so [Solana.Devnet] is cosmetic — a
 * wallet may surface "Connect to Devnet" but we never broadcast anything.
 *
 * Activity must be a [ComponentActivity] (required by [ActivityResultSender]).
 */
class MwaClient(
  private val identityName: String = "Clan World",
  private val identityUri: Uri = Uri.parse("https://clan-world.com"),
  private val iconUri: Uri = Uri.parse("/favicon.png"),
) {

  private fun adapter(): MobileWalletAdapter = MobileWalletAdapter(
    connectionIdentity = ConnectionIdentity(
      identityUri = identityUri,
      iconUri = iconUri,
      identityName = identityName,
    ),
  ).apply {
    blockchain = Solana.Devnet
  }

  // ── Connect (first time) ───────────────────────────────────────────────

  suspend fun connect(activity: ComponentActivity): MwaResult<MwaSession> {
    val sender = ActivityResultSender(activity)
    val a = adapter()
    val result = a.connect(sender)
    return mapAuthResult(result)
  }

  // ── Reauthorize (subsequent launches) ──────────────────────────────────

  /**
   * Setting [MobileWalletAdapter.authToken] before invoking transact (via
   * the convenience `connect()`) makes the SDK route through reauthorize
   * instead of authorize internally.
   */
  suspend fun reauthorize(
    activity: ComponentActivity,
    authToken: String,
  ): MwaResult<MwaSession> {
    val sender = ActivityResultSender(activity)
    val a = adapter().apply { this.authToken = authToken }
    val result = a.connect(sender)
    return mapAuthResult(result)
  }

  // ── Wallet-side teardown ───────────────────────────────────────────────

  /**
   * Tear down the wallet-side authorization so the wallet's connected-dApps
   * list clears. Best-effort: failures are swallowed because the local
   * session has already been cleared by the caller and the user has been
   * routed back to Connect — there's nothing useful to do with a failure.
   */
  suspend fun disconnect(activity: ComponentActivity, authToken: String) {
    runCatching {
      val sender = ActivityResultSender(activity)
      val a = adapter().apply { this.authToken = authToken }
      a.disconnect(sender)
    }
  }

  // ── Sign message (slice 2 full uses this for SIWS) ─────────────────────

  suspend fun signMessage(
    activity: ComponentActivity,
    authToken: String,
    message: ByteArray,
  ): MwaResult<ByteArray> {
    val sender = ActivityResultSender(activity)
    val a = adapter().apply { this.authToken = authToken }
    val result: TransactionResult<ByteArray> = a.transact(sender) { authResult ->
      val pubkey = authResult.accounts.first().publicKey
      val signed = signMessagesDetached(arrayOf(message), arrayOf(pubkey))
      signed.messages.first().signatures.first()
    }
    return when (result) {
      is TransactionResult.Success -> MwaResult.Ok(result.payload)
      is TransactionResult.NoWalletFound -> MwaResult.WalletNotFound
      is TransactionResult.Failure -> classifyFailure(result)
    }
  }

  // ── Internals ──────────────────────────────────────────────────────────

  private fun mapAuthResult(result: TransactionResult<Unit>): MwaResult<MwaSession> = when (result) {
    is TransactionResult.Success -> {
      // `authResult` accessor is valid on Success when transact ran a real
      // authorize/reauthorize — always the case from connect(). MWA 2.0+
      // exposes the connected account through `accounts[0]`; the top-level
      // `publicKey`/`accountLabel` fields are deprecated back-compat aliases.
      val auth = result.authResult
      val account = auth.accounts.firstOrNull()
      if (account == null) {
        MwaResult.Error(IllegalStateException("MWA returned no authorized account"))
      } else {
        MwaResult.Ok(
          MwaSession(
            solanaPubkeyBase58 = Base58.encode(account.publicKey),
            authToken = auth.authToken,
            walletUriBase = auth.walletUriBase?.toString(),
            walletLabel = account.accountLabel,
          ),
        )
      }
    }
    is TransactionResult.NoWalletFound -> MwaResult.WalletNotFound
    is TransactionResult.Failure -> classifyFailure(result)
  }

  private fun <T> classifyFailure(result: TransactionResult.Failure<T>): MwaResult<Nothing> {
    val msg = result.message.lowercase() + " " + (result.e.message?.lowercase().orEmpty())
    return if (
      "declin" in msg ||
      "cancel" in msg ||
      "user did not approve" in msg ||
      "user_canceled" in msg
    ) {
      MwaResult.UserDeclined
    } else {
      MwaResult.Error(result.e)
    }
  }
}
