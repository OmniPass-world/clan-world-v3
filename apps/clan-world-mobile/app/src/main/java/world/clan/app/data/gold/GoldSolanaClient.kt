package world.clan.app.data.gold

import com.solana.publickey.ProgramDerivedAddress
import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.AccountMeta
import com.solana.transaction.Message
import com.solana.transaction.Transaction
import com.solana.transaction.TransactionInstruction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import world.clan.app.wallet.Base58
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class GoldSolanaException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

data class GoldTransferReceipt(
  val signature: String,
  val burnAmount: Long,
  val skipTax: Long = 0L,
)

/**
 * Minimal Solana Devnet client for ClanWorld GOLD.
 *
 * The app builds standard SPL Token instructions directly and lets Mobile
 * Wallet Adapter sign + broadcast. Convex later verifies the signature before
 * recording game state.
 */
class GoldSolanaClient(
  private val rpcUrl: String,
  mintBase58: String,
  faucetProgramBase58: String,
  treasuryOwnerBase58: String,
  private val decimals: Int,
) {
  private val http = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()
  private val json = Json { ignoreUnknownKeys = true }
  private val mediaType = "application/json".toMediaType()

  val mint = mintBase58.toPubkey()
  val faucetProgram = faucetProgramBase58.toPubkey()
  val treasuryOwner = treasuryOwnerBase58.toPubkey()

  suspend fun balance(ownerBase58: String): Long = withContext(Dispatchers.IO) {
    val ata = associatedTokenAddress(ownerBase58.toPubkey(), mint)
    val result = rpc("getTokenAccountBalance", buildJsonArray { add(JsonPrimitive(ata.base58())) })
    result.jsonObject["value"]
      ?.jsonObject
      ?.get("amount")
      ?.jsonPrimitive
      ?.contentOrNull
      ?.toLongOrNull()
      ?: 0L
  }

  suspend fun maybeAirdropFeeSol(ownerBase58: String) = withContext(Dispatchers.IO) {
    val owner = ownerBase58.toPubkey()
    val balance = rpc("getBalance", buildJsonArray { add(JsonPrimitive(owner.base58())) })
      .jsonObject["value"]
      ?.jsonPrimitive
      ?.longOrNull
      ?: 0L
    if (balance < 25_000_000L) {
      runCatching {
        rpc("requestAirdrop", buildJsonArray {
          add(JsonPrimitive(owner.base58()))
          add(JsonPrimitive(1_000_000_000L))
        })
      }
    }
  }

  suspend fun buildFaucetClaim(ownerBase58: String, memo: String): ByteArray {
    val owner = ownerBase58.toPubkey()
    val ownerAta = associatedTokenAddress(owner, mint)
    val mintAuthority = pda("mint-authority")
    return buildTransaction(
      payer = owner,
      instructions = listOf(
        createAssociatedTokenAccountIdempotent(owner, owner, mint, ownerAta),
        TransactionInstruction(
          faucetProgram,
          listOf(
            AccountMeta(owner, isSigner = true, isWritable = true),
            AccountMeta(mint, isSigner = false, isWritable = true),
            AccountMeta(ownerAta, isSigner = false, isWritable = true),
            AccountMeta(mintAuthority, isSigner = false, isWritable = false),
            AccountMeta(TOKEN_PROGRAM_ID, isSigner = false, isWritable = false),
          ),
          anchorDiscriminator("global:claim"),
        ),
        memo(owner, memo),
      ),
    )
  }

  suspend fun buildBurn(
    ownerBase58: String,
    amount: Long,
    memo: String,
    skipTax: Long = 0L,
  ): ByteArray {
    val owner = ownerBase58.toPubkey()
    val ownerAta = associatedTokenAddress(owner, mint)
    val instructions = mutableListOf<TransactionInstruction>()
    instructions += burnChecked(ownerAta, mint, owner, amount)
    if (skipTax > 0L) {
      val treasuryAta = associatedTokenAddress(treasuryOwner, mint)
      instructions += createAssociatedTokenAccountIdempotent(owner, treasuryOwner, mint, treasuryAta)
      instructions += transferChecked(ownerAta, mint, treasuryAta, owner, skipTax)
    }
    instructions += memo(owner, memo)
    return buildTransaction(owner, instructions)
  }

  private suspend fun buildTransaction(
    payer: SolanaPublicKey,
    instructions: List<TransactionInstruction>,
  ): ByteArray {
    val blockhash = latestBlockhash()
    val message = Message.Builder(instructions.toMutableList(), payer)
      .setRecentBlockhash(blockhash)
      .build()
    return Transaction(message).serialize()
  }

  private suspend fun latestBlockhash(): String = withContext(Dispatchers.IO) {
    val result = rpc("getLatestBlockhash", buildJsonArray {
      add(buildJsonObject { put("commitment", "confirmed") })
    })
    result.jsonObject["value"]
      ?.jsonObject
      ?.get("blockhash")
      ?.jsonPrimitive
      ?.contentOrNull
      ?: throw GoldSolanaException("Solana RPC returned no blockhash")
  }

  private suspend fun associatedTokenAddress(owner: SolanaPublicKey, mint: SolanaPublicKey): SolanaPublicKey {
    return ProgramDerivedAddress
      .find(
        listOf(owner.bytes(), TOKEN_PROGRAM_ID.bytes(), mint.bytes()),
        ASSOCIATED_TOKEN_PROGRAM_ID,
      )
      .getOrThrow()
  }

  private suspend fun pda(seed: String): SolanaPublicKey =
    ProgramDerivedAddress.find(listOf(seed.toByteArray()), faucetProgram).getOrThrow()

  private fun createAssociatedTokenAccountIdempotent(
    payer: SolanaPublicKey,
    owner: SolanaPublicKey,
    mint: SolanaPublicKey,
    ata: SolanaPublicKey,
  ) = TransactionInstruction(
    ASSOCIATED_TOKEN_PROGRAM_ID,
    listOf(
      AccountMeta(payer, isSigner = true, isWritable = true),
      AccountMeta(ata, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(SYSTEM_PROGRAM_ID, isSigner = false, isWritable = false),
      AccountMeta(TOKEN_PROGRAM_ID, isSigner = false, isWritable = false),
    ),
    byteArrayOf(1),
  )

  private fun burnChecked(
    source: SolanaPublicKey,
    mint: SolanaPublicKey,
    owner: SolanaPublicKey,
    amount: Long,
  ) = TransactionInstruction(
    TOKEN_PROGRAM_ID,
    listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    byteArrayOf(15) + u64(amount) + byteArrayOf(decimals.toByte()),
  )

  private fun transferChecked(
    source: SolanaPublicKey,
    mint: SolanaPublicKey,
    destination: SolanaPublicKey,
    owner: SolanaPublicKey,
    amount: Long,
  ) = TransactionInstruction(
    TOKEN_PROGRAM_ID,
    listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    byteArrayOf(12) + u64(amount) + byteArrayOf(decimals.toByte()),
  )

  private fun memo(owner: SolanaPublicKey, text: String) = TransactionInstruction(
    MEMO_PROGRAM_ID,
    listOf(AccountMeta(owner, isSigner = true, isWritable = false)),
    text.toByteArray(),
  )

  private fun anchorDiscriminator(name: String): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(name.toByteArray()).copyOfRange(0, 8)

  private fun u64(value: Long): ByteArray =
    ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array()

  private fun String.toPubkey(): SolanaPublicKey = SolanaPublicKey(decodeBase58(this))

  private fun SolanaPublicKey.bytes(): ByteArray = bytes

  private fun decodeBase58(value: String): ByteArray {
    var bytes = byteArrayOf(0)
    value.forEach { c ->
      val digit = BASE58_ALPHABET.indexOf(c)
      if (digit < 0) throw GoldSolanaException("Invalid base58 public key: $value")
      var carry = digit
      for (i in bytes.indices.reversed()) {
        carry += (bytes[i].toInt() and 0xff) * 58
        bytes[i] = carry.toByte()
        carry = carry ushr 8
      }
      while (carry > 0) {
        bytes = byteArrayOf((carry and 0xff).toByte()) + bytes
        carry = carry ushr 8
      }
    }
    val leadingZeros = value.takeWhile { it == '1' }.length
    val decoded = ByteArray(leadingZeros) + bytes.dropWhile { it.toInt() == 0 }.toByteArray()
    if (decoded.size != 32) throw GoldSolanaException("Expected 32-byte public key: $value")
    return decoded
  }

  private fun rpc(method: String, params: JsonArray): JsonElement {
    val payload = buildJsonObject {
      put("jsonrpc", "2.0")
      put("id", "clanworld")
      put("method", method)
      put("params", params)
    }.toString()
    val req = Request.Builder()
      .url(rpcUrl)
      .post(payload.toRequestBody(mediaType))
      .build()
    val resp = http.newCall(req).execute()
    val body = resp.body?.string().orEmpty()
    if (!resp.isSuccessful) throw GoldSolanaException("Solana RPC ${resp.code}: $body")
    val root = json.parseToJsonElement(body).jsonObject
    val error = root["error"]
    if (error != null && error !is JsonNull) throw GoldSolanaException("Solana RPC $method failed: $error")
    return root["result"] ?: throw GoldSolanaException("Solana RPC $method returned no result")
  }

  companion object {
    const val FAUCET_AMOUNT = 100_000L
    const val BURN_AMOUNT = 5L

    private const val BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA".toPubkeyStatic()
    private val ASSOCIATED_TOKEN_PROGRAM_ID = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL".toPubkeyStatic()
    private val SYSTEM_PROGRAM_ID = "11111111111111111111111111111111".toPubkeyStatic()
    private val MEMO_PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr".toPubkeyStatic()

    private fun String.toPubkeyStatic(): SolanaPublicKey {
      var bytes = byteArrayOf(0)
      forEach { c ->
        val digit = BASE58_ALPHABET.indexOf(c)
        require(digit >= 0) { "invalid base58" }
        var carry = digit
        for (i in bytes.indices.reversed()) {
          carry += (bytes[i].toInt() and 0xff) * 58
          bytes[i] = carry.toByte()
          carry = carry ushr 8
        }
        while (carry > 0) {
          bytes = byteArrayOf((carry and 0xff).toByte()) + bytes
          carry = carry ushr 8
        }
      }
      val decoded = ByteArray(takeWhile { it == '1' }.length) + bytes.dropWhile { it.toInt() == 0 }.toByteArray()
      return SolanaPublicKey(decoded)
    }
  }
}
