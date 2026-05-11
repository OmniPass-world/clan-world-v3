@file:OptIn(ExperimentalSerializationApi::class)

package world.clan.app.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── getSnapshot:getSnapshot ────────────────────────────────────────────
//
// Convex returns: { tick, tickEpoch, regions[], clans[], … }
// Wei-precision balances arrive as JSON strings; we keep them as String
// here and parse to BigInteger in the ViewModel layer.

@Serializable
data class WorldSnapshot(
  val tick: Long,
  val tickEpoch: TickEpoch? = null,
  val regions: List<Region> = emptyList(),
  val clans: List<ClanSummary> = emptyList(),
  val activeBanditId: Int? = null,
  val bandit: BanditSummary? = null,
  val seasonStartTick: Long? = null,
  val seasonEndTick: Long? = null,
  val winterActive: Boolean = false,
  val winterStartsAtTick: Long? = null,
)

@Serializable
data class TickEpoch(
  val startedAt: Long? = null,
  val durationMs: Long? = null,
)

@Serializable
data class Region(
  val id: String,
  val name: String,
  val ownerClanId: String? = null,
)

/**
 * One clan's summary inside getSnapshot. Wei amounts (`treasury`,
 * `goldBalance`, `vault*`) are `String` — parse with `String.weiToWhole()`.
 *
 * Server-side numeric clan-level fields (`baseLevel`, `baseRegion`,
 * `wallLevel`, `monumentLevel`, `livingClansmen`) are intentionally
 * NOT modelled — they arrive as bare JSON numbers (e.g. `1.0`) which
 * conflict with the `String?` shape the cockpit demo previously assumed.
 * `ignoreUnknownKeys = true` on the deserializer drops them silently.
 */
@Serializable
data class ClanSummary(
  val id: String,
  val name: String,
  val treasury: String? = null,
  val goldBalance: String? = null,
  val blueprintBalance: String? = null,
  val vaultWood: String? = null,
  val vaultIron: String? = null,
  val vaultWheat: String? = null,
  val vaultFish: String? = null,
  val owner: String? = null,
)

@Serializable
data class BanditSummary(
  val id: Int,
  val region: Int? = null,
  val state: Int? = null,
  val tier: Int? = null,
  val attackPower: Int? = null,
  val stateEnteredTick: Long? = null,
  val nextActionTick: Long? = null,
  val projectedTargetClanId: Int? = null,
)

// ─── inft:getInftDemoState ──────────────────────────────────────────────

@Serializable
data class InftToken(
  val tokenId: Int,
  val clanId: Int,
  val owner: String,
  val dataHash: String? = null,
  val encryptedKeyHash: String? = null,
  val metadataUri: String? = null,
  val updatedAt: Long? = null,
  val txHash: String? = null,
)

@Serializable
data class InftDemoState(
  val token: InftToken? = null,
  val transfers: List<InftTransfer> = emptyList(),
  val memory: List<MemoryEntry> = emptyList(),
  val bulletins: List<Bulletin> = emptyList(),
)

@Serializable
data class InftTransfer(
  val tokenId: Int,
  val clanId: Int,
  val from: String,
  val to: String,
  val dataHash: String? = null,
  val encryptedKeyHash: String? = null,
  val txHash: String? = null,
  val transferredAt: Long? = null,
)

@Serializable
data class MemoryEntry(
  val clanId: Int,
  val key: String,
  val value: String,
  val dataHash: String? = null,
  /** "local" | "0g" | "demo" */
  val source: String? = null,
  val updatedAt: Long? = null,
  val txHash: String? = null,
)

// ─── comms:getCombinedComms ─────────────────────────────────────────────
//
// Server returns a flat list with a `kind` discriminator:
//   "whisper" | "orch" | "human"
// Whisper-only fields (recipients) are nullable.

@Serializable
data class CombinedComm(
  val kind: String,
  val tick: Long? = null,
  val speaker: String? = null,
  val body: String,
  val recipients: List<Int>? = null,
  val timestamp: Long? = null,
  val targetClan: Int? = null,
  val fromClan: Int? = null,
)

// ─── vault:getVaultMovements ────────────────────────────────────────────

@Serializable
data class VaultMovement(
  val tick: Long,
  /** "gain" | "spend" | "transfer" */
  val type: String,
  /** Already wei-divided server-side (vault.ts) */
  val amount: Double,
  /** "wood" | "iron" | "wheat" | "fish" | "gold" | "blueprint" */
  val resource: String,
  val source: String? = null,
  val timestamp: Long? = null,
)

// ─── bulletins:getByClan ────────────────────────────────────────────────

@Serializable
data class Bulletin(
  @SerialName("_id") val id: String? = null,
  val clanId: Int,
  val slot: Int,
  val body: String,
  val updatedAt: Long? = null,
  val dataHash: String? = null,
  val txHash: String? = null,
)

// ─── Helpers ────────────────────────────────────────────────────────────

private val WEI = java.math.BigInteger("1000000000000000000")

/** Parse a wei-precision JSON-string balance into whole tokens. */
fun String?.weiToWhole(): Long = this?.let {
  runCatching { java.math.BigInteger(it).divide(WEI).toLong() }.getOrNull()
} ?: 0L
