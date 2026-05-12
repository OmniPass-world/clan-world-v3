package world.clan.app.data.gold

import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.LegacyMessage
import com.solana.transaction.Message
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for issue #240.
 *
 * # The bug
 *
 * `com.solanamobile:web3-solana-jvm:0.3.0-beta4` `Message.Builder.build()`
 * buckets `AccountMeta`s by `(isSigner, isWritable)` GLOBALLY across all
 * instructions. The compiled `accounts` list dedups pubkeys via
 * `LinkedHashSet`, but the header counters
 * (`signatureCount`, `readOnlyAccounts`, `readOnlyNonSigners`) use the RAW
 * bucket sizes pre-dedup. So a pubkey that appears in 2+ instructions with
 * different flag tuples produces a malformed header/accounts mismatch and
 * the on-chain sanitizer rejects the transaction with
 * "Transaction failed to sanitize accounts offsets correctly".
 *
 * Real-world hit (faucet path): `memo()` emits `owner(true, false)`
 * (readonly-signer) while ATA-create's payer slot + claim's payer slot
 * emit `owner(true, true)` (writable-signer). Same pubkey lands in two
 * buckets. Same shape in `buildBurn(skipTax > 0)`.
 *
 * # The fix
 *
 * `GoldSolanaClient.buildTransaction` runs every instruction list through
 * `unionAccountFlags` BEFORE handing it to `Message.Builder`. The pre-pass
 * computes the OR-union of `(isSigner, isWritable)` for every pubkey and
 * normalizes all occurrences. Compiled message now has every pubkey in
 * exactly ONE bucket, and the header counters match the accounts list.
 *
 * # What these tests assert
 *
 * The canonical invariant: the compiled `LegacyMessage` has
 *
 *   `accounts.size == signatureCount + writableNonSigners + readOnlyNonSigners`
 *
 * where `writableNonSigners` is derived as
 * `accounts.size - signatureCount - readOnlyNonSigners`. If the union
 * pre-pass is reverted, `writableNonSigners` can go negative — that's the
 * exact header/accounts mismatch the on-chain sanitizer trips on.
 *
 * We also assert: any pubkey that is `isWritable=true` in ANY input
 * instruction MUST end up in a writable bucket in the compiled message.
 * (Owner is writable in ATA-create + claim, so it must be a writable
 * signer — NOT readonly-signer.)
 */
class GoldSolanaClientTest {

  private val owner = SolanaPublicKey(ByteArray(32) { (it + 1).toByte() })
  private val ata = SolanaPublicKey(ByteArray(32) { (it + 32).toByte() })
  private val mint = SolanaPublicKey(ByteArray(32) { (it + 64).toByte() })
  private val differentPayer = SolanaPublicKey(ByteArray(32) { (it + 96).toByte() })
  private val blockhash = SolanaPublicKey(ByteArray(32) { (it + 128).toByte() })

  // Real devnet-shaped base58 strings so the eager toPubkey() in the
  // constructor decodes 32 bytes — system-program pubkey is the canonical
  // safe placeholder.
  private val client = GoldSolanaClient(
    rpcUrl = "http://localhost",
    mintBase58 = "11111111111111111111111111111111",
    faucetProgramBase58 = "11111111111111111111111111111111",
    treasuryOwnerBase58 = "11111111111111111111111111111111",
    decimals = 6,
  )

  // ─── ATA-create instruction unit ──────────────────────────────────────────

  @Test
  fun selfFundedAtaCreateOwnerEndsUpWritableSignerInCompiledMessage() = runBlocking {
    // Replicates round-1 PR #241 narrow-path test, but asserts the meaningful
    // invariant — header-counter consistency — instead of tautologies.
    val ix = client.createAssociatedTokenAccountIdempotent(
      payer = owner,
      owner = owner,
      mint = mint,
      ata = ata,
    )

    val message = buildLegacyMessage(client.unionAccountFlags(listOf(ix), payer = owner))

    assertHeaderAccountsConsistent(message)
    assertOwnerInWritableSignerBucket(message, owner)
  }

  @Test
  fun separatePayerKeepsOwnerReadonlyNonSigner() = runBlocking {
    val ix = client.createAssociatedTokenAccountIdempotent(
      payer = differentPayer,
      owner = owner,
      mint = mint,
      ata = ata,
    )

    val message = buildLegacyMessage(client.unionAccountFlags(listOf(ix), payer = differentPayer))

    assertHeaderAccountsConsistent(message)
    // payer is the only signer; owner is readonly non-signer.
    val payerIndex = message.accounts.indexOfFirst { it.bytes.contentEquals(differentPayer.bytes) }
    val ownerIndex = message.accounts.indexOfFirst { it.bytes.contentEquals(owner.bytes) }
    val signers = message.signatureCount.toInt()
    val readonlyNonSigners = message.readOnlyNonSigners.toInt()
    assertTrue("payer should be in signer prefix", payerIndex in 0 until signers)
    assertTrue(
      "owner should land in readonly-non-signer suffix",
      ownerIndex in (message.accounts.size - readonlyNonSigners) until message.accounts.size,
    )
  }

  // ─── Full buildFaucetClaim instruction list (Tier 1 + Tier 2 critical) ───

  @Test
  fun faucetClaimFullInstructionListHasConsistentHeader() = runBlocking {
    // This is the empirical reproduction the round-1 swarm identified:
    // the full instruction list — ATA-create + claim + memo — must compile
    // to a Message whose header counters partition `accounts` cleanly.
    val instructions = client.faucetClaimInstructions(owner, "faucet:test")
    val canonical = client.unionAccountFlags(instructions, payer = owner)
    val message = buildLegacyMessage(canonical)

    assertHeaderAccountsConsistent(message)
    // No duplicate pubkeys in the compiled accounts list.
    assertEquals(
      "Compiled message must not have duplicate pubkeys",
      message.accounts.size,
      message.accounts.map { it.bytes.toList() }.distinct().size,
    )
    // Owner needs to sign AND be writable (ATA-create payer slot + claim
    // payer slot both require it), so it MUST land in the writable-signer
    // bucket — NOT readonly-signer.
    assertOwnerInWritableSignerBucket(message, owner)
  }

  @Test
  fun faucetClaimUnionsOwnerFlagsAcrossMemoAndPayerSlots() = runBlocking {
    val raw = client.faucetClaimInstructions(owner, "faucet:test")
    // Sanity-check the input shape: memo() emits owner as readonly-signer
    // while ATA-create + claim emit owner as writable-signer. This is the
    // bucket-split that triggers the bug.
    val memoIx = raw.last()
    val memoOwnerMeta = memoIx.accounts.single { it.publicKey.bytes.contentEquals(owner.bytes) }
    assertTrue("memo() emits owner as readonly-signer (input shape)", memoOwnerMeta.isSigner && !memoOwnerMeta.isWritable)

    // After union, every owner meta should be writable-signer.
    val canonical = client.unionAccountFlags(raw, payer = owner)
    canonical.forEach { ix ->
      ix.accounts.filter { it.publicKey.bytes.contentEquals(owner.bytes) }.forEach { meta ->
        assertTrue(
          "Owner meta after union must be writable signer (instruction ${canonical.indexOf(ix)})",
          meta.isSigner && meta.isWritable,
        )
      }
    }
  }

  // ─── Full buildBurn(skipTax > 0) instruction list ────────────────────────

  @Test
  fun burnWithSkipTaxHasConsistentHeader() = runBlocking {
    // Burn-with-skip-tax exercises the same multi-bucket shape:
    // burnChecked emits owner(true,false), the inner ATA-create for
    // treasuryAta emits payer=owner(true,true), transferChecked emits
    // owner(true,false), and memo emits owner(true,false).
    val instructions = client.burnInstructions(owner, amount = 100L, memo = "burn:test", skipTax = 5L)
    val canonical = client.unionAccountFlags(instructions, payer = owner)
    val message = buildLegacyMessage(canonical)

    assertHeaderAccountsConsistent(message)
    assertOwnerInWritableSignerBucket(message, owner)
  }

  // v2.5.1 hotfix regression: super-swarm flagged that buildBurn(skipTax=0L)
  // produced ZERO writable signers because burnChecked + memo both emit
  // owner as readonly-signer. Message.Builder in web3-solana-jvm:0.3.0-beta4
  // has no setFeePayer — it infers from the first writable signer in the
  // bucketed accounts list. No writable signer → Solana fee-payer derivation
  // fails → pre-flight rejection ("Transaction has no writable signer").
  //
  // Fix: unionAccountFlags(instructions, payer) force-promotes the payer
  // pubkey to (isSigner=true, isWritable=true) so the compiled message
  // always has a writable-signer bucket. The payer (== owner here) MUST
  // land in writable-signer regardless of whether any instruction emits
  // owner as writable.
  @Test
  fun burnWithoutSkipTaxStillProducesConsistentHeaderAndHasWritablePayer() = runBlocking {
    val instructions = client.burnInstructions(owner, amount = 100L, memo = "burn:test", skipTax = 0L)
    val canonical = client.unionAccountFlags(instructions, payer = owner)
    val message = buildLegacyMessage(canonical)

    assertHeaderAccountsConsistent(message)

    // The critical regression assertion: the compiled message has AT LEAST
    // ONE writable signer (i.e. writableSigners = signatureCount -
    // readOnlyAccounts >= 1). Without payer promotion, this is 0 and Solana
    // pre-flight rejects.
    val signers = message.signatureCount.toInt()
    val readonlySigners = message.readOnlyAccounts.toInt()
    val writableSigners = signers - readonlySigners
    assertTrue(
      "Compiled message MUST have >= 1 writable signer for Solana fee-payer " +
        "derivation. signatureCount=$signers, readOnlyAccounts=$readonlySigners, " +
        "writableSigners=$writableSigners. If this fails, payer promotion is broken.",
      writableSigners >= 1,
    )

    // Owner is the payer here, so it MUST land in the writable-signer slice.
    assertOwnerInWritableSignerBucket(message, owner)
  }

  // ─── unionAccountFlags unit semantics ────────────────────────────────────

  @Test
  fun unionAccountFlagsIsIdempotentOnAlreadyCanonicalInput() = runBlocking {
    val canonical = client.unionAccountFlags(client.faucetClaimInstructions(owner, "memo"), payer = owner)
    val twice = client.unionAccountFlags(canonical, payer = owner)
    assertEquals(canonical.size, twice.size)
    canonical.zip(twice).forEach { (a, b) ->
      assertEquals(a.accounts.size, b.accounts.size)
      a.accounts.zip(b.accounts).forEach { (am, bm) ->
        assertEquals(am.publicKey.bytes.toList(), bm.publicKey.bytes.toList())
        assertEquals(am.isSigner, bm.isSigner)
        assertEquals(am.isWritable, bm.isWritable)
      }
    }
  }

  @Test
  fun unionAccountFlagsPromotesReadonlySignerPayerToWritableSigner() = runBlocking {
    // Direct unit test for the v2.5.1 hotfix on the precise input shape
    // that triggered the regression: payer appears in instructions ONLY as
    // readonly-signer (isSigner=true, isWritable=false). Pre-fix, the union
    // map would dutifully record (true, false) and the compiled message
    // would have ZERO writable signers, breaking fee-payer derivation.
    //
    // Post-fix, the payer is force-seeded with (true, true) before the
    // union loop runs, so the union output is (true, true) even though
    // every input meta said (true, false).
    val readonlySignerIx = com.solana.transaction.TransactionInstruction(
      mint,
      listOf(
        com.solana.transaction.AccountMeta(mint, isSigner = false, isWritable = true),
        com.solana.transaction.AccountMeta(owner, isSigner = true, isWritable = false),
      ),
      byteArrayOf(0),
    )

    val canonical = client.unionAccountFlags(listOf(readonlySignerIx), payer = owner)

    // After union, owner meta in the unioned instruction must be
    // (isSigner=true, isWritable=true).
    val ownerMeta = canonical.single().accounts.single { it.publicKey.bytes.contentEquals(owner.bytes) }
    assertTrue(
      "Payer promotion must upgrade readonly-signer payer to writable-signer. " +
        "Got isSigner=${ownerMeta.isSigner}, isWritable=${ownerMeta.isWritable}",
      ownerMeta.isSigner && ownerMeta.isWritable,
    )

    // And the compiled message must have a writable-signer slice with the payer in it.
    val message = buildLegacyMessage(canonical)
    assertHeaderAccountsConsistent(message)
    val signers = message.signatureCount.toInt()
    val readonlySigners = message.readOnlyAccounts.toInt()
    val writableSigners = signers - readonlySigners
    assertTrue("compiled message must have >= 1 writable signer", writableSigners >= 1)
    assertOwnerInWritableSignerBucket(message, owner)
  }

  // ─── helpers ─────────────────────────────────────────────────────────────

  private fun buildLegacyMessage(
    instructions: List<com.solana.transaction.TransactionInstruction>,
  ): LegacyMessage {
    val builder = Message.Builder().setRecentBlockhash(blockhash)
    instructions.forEach { builder.addInstruction(it) }
    val message = builder.build()
    require(message is LegacyMessage) { "Expected LegacyMessage, got ${message::class}" }
    return message
  }

  /**
   * The canonical Solana message invariant:
   * `accounts.size == signatureCount + writableNonSigners + readOnlyNonSigners`
   * where `writableNonSigners = accounts.size - signatureCount - readOnlyNonSigners`.
   *
   * Equivalently: `writableNonSigners >= 0` AND `readOnlyAccounts <= signatureCount`.
   *
   * Before the union pre-pass, a pubkey landing in two buckets could push
   * the raw bucket-sum past the deduped `accounts.size`, making
   * `writableNonSigners` negative — that's the exact mismatch that yields
   * "Transaction failed to sanitize accounts offsets correctly" at the RPC.
   */
  private fun assertHeaderAccountsConsistent(m: LegacyMessage) {
    val signers = m.signatureCount.toInt()
    val readonlySigners = m.readOnlyAccounts.toInt()
    val readonlyNonSigners = m.readOnlyNonSigners.toInt()
    val total = m.accounts.size

    assertTrue(
      "readOnlyAccounts ($readonlySigners) must be <= signatureCount ($signers)",
      readonlySigners <= signers,
    )
    assertTrue(
      "signatureCount + readOnlyNonSigners ($signers + $readonlyNonSigners = " +
        "${signers + readonlyNonSigners}) must be <= accounts.size ($total). " +
        "If this fails, a pubkey landed in 2 buckets and the on-chain sanitizer will reject.",
      signers + readonlyNonSigners <= total,
    )
    // No duplicate pubkeys in the compiled accounts list.
    assertEquals(
      "Compiled message must not have duplicate pubkeys in accounts list",
      total,
      m.accounts.map { it.bytes.toList() }.distinct().size,
    )
  }

  /**
   * The compiled `accounts` list is ordered: writable-signers,
   * readonly-signers, writable-non-signers, readonly-non-signers.
   * "Owner in writable-signer bucket" means index in [0, signatureCount - readOnlyAccounts).
   */
  private fun assertOwnerInWritableSignerBucket(m: LegacyMessage, ownerKey: SolanaPublicKey) {
    val signers = m.signatureCount.toInt()
    val readonlySigners = m.readOnlyAccounts.toInt()
    val writableSigners = signers - readonlySigners
    val ownerIndex = m.accounts.indexOfFirst { it.bytes.contentEquals(ownerKey.bytes) }
    assertTrue("owner must appear in compiled accounts", ownerIndex >= 0)
    assertTrue(
      "Owner index ($ownerIndex) must be in writable-signer slice [0, $writableSigners). " +
        "writableSigners=$writableSigners, signatureCount=$signers, readOnlyAccounts=$readonlySigners. " +
        "If owner lands outside this slice, the multi-bucket bug is back.",
      ownerIndex in 0 until writableSigners,
    )
  }
}
