package world.clan.app.data.gold

import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression test for issue #240: when the ATA-create-idempotent instruction
 * was emitted with `payer == owner` but conflicting AccountMeta flags
 * (writable signer vs. readonly non-signer), web3-solana's
 * [Message.Builder.build] bucket-added the owner pubkey to BOTH the
 * writable-signers set AND the readonly-non-signers set. The compiled
 * message then carried the same pubkey twice in `accounts`, which
 * Solana's pre-flight sanitizer rejects with
 * "Transaction failed to sanitize accounts offsets correctly".
 *
 * The fix promotes the owner slot's flags to match the payer slot when
 * `payer == owner`, so both slots route to the same bucket and the
 * underlying LinkedHashSet collapses them to a single entry in `accounts`.
 */
class GoldSolanaClientTest {

  private val owner = SolanaPublicKey(ByteArray(32) { (it + 1).toByte() })
  private val ata = SolanaPublicKey(ByteArray(32) { (it + 32).toByte() })
  private val mint = SolanaPublicKey(ByteArray(32) { (it + 64).toByte() })
  private val differentPayer = SolanaPublicKey(ByteArray(32) { (it + 96).toByte() })
  private val blockhash = SolanaPublicKey(ByteArray(32) { (it + 128).toByte() })

  private val client = GoldSolanaClient(
    rpcUrl = "http://localhost",
    mintBase58 = "11111111111111111111111111111111",
    faucetProgramBase58 = "11111111111111111111111111111111",
    treasuryOwnerBase58 = "11111111111111111111111111111111",
    decimals = 6,
  )

  @Test
  fun selfFundedAtaCreatePutsOwnerInAccountsExactlyOnce() {
    val ix = client.createAssociatedTokenAccountIdempotent(
      payer = owner,
      owner = owner,
      mint = mint,
      ata = ata,
    )
    val message = Message.Builder()
      .setRecentBlockhash(blockhash)
      .addInstruction(ix)
      .build()

    val ownerOccurrences = message.accounts.count { it.bytes.contentEquals(owner.bytes) }
    assertEquals(
      "Owner pubkey must appear exactly once in compiled message accounts " +
        "(double-occurrence is what triggers the sanitize-offsets RPC error)",
      1,
      ownerOccurrences,
    )

    val distinctKeys = message.accounts.map { it.bytes.toList() }.distinct()
    assertEquals(
      "Compiled message must not contain duplicate account keys",
      message.accounts.size,
      distinctKeys.size,
    )

    val payerSlot = ix.accounts[0]
    val ownerSlot = ix.accounts[2]
    assertTrue("Self-funded payer slot stays a writable signer", payerSlot.isSigner && payerSlot.isWritable)
    assertTrue("Self-funded owner duplicate slot is also writable signer", ownerSlot.isSigner && ownerSlot.isWritable)
  }

  @Test
  fun separatePayerKeepsOwnerReadonly() {
    val ix = client.createAssociatedTokenAccountIdempotent(
      payer = differentPayer,
      owner = owner,
      mint = mint,
      ata = ata,
    )

    val payerSlot = ix.accounts[0]
    val ownerSlot = ix.accounts[2]

    assertTrue("Payer is writable signer", payerSlot.isSigner && payerSlot.isWritable)
    assertTrue(
      "Owner slot is readonly non-signer when distinct from payer",
      !ownerSlot.isSigner && !ownerSlot.isWritable,
    )
    assertNotEquals(payerSlot.publicKey.bytes.toList(), ownerSlot.publicKey.bytes.toList())

    val message = Message.Builder()
      .setRecentBlockhash(blockhash)
      .addInstruction(ix)
      .build()

    val payerOccurrences = message.accounts.count { it.bytes.contentEquals(differentPayer.bytes) }
    val ownerOccurrences = message.accounts.count { it.bytes.contentEquals(owner.bytes) }
    assertEquals(1, payerOccurrences)
    assertEquals(1, ownerOccurrences)
  }
}
