package world.clan.app.data.convex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shapes for Convex `POST /api/query` responses. Each query's `value`
 * field is decoded directly into one of these. Unknown fields are ignored
 * by the Json config — the live snapshot payload is enormous and we only
 * model the parts the cockpit reads.
 *
 * Numeric fields come back as JSON floats (e.g. `78.0`), so all integer-
 * looking values are typed as `Double` and converted in the mapping layer.
 */

// ---- snapshot --------------------------------------------------------

@Serializable
data class TickEpochWire(
  val durationMs: Double = 60_000.0,
  val startedAt: Double = 0.0,
)

@Serializable
data class ClanWire(
  val id: String,
  val goldBalance: String? = null,
  val treasury: String? = null,
  val vaultWood: String? = null,
  val vaultIron: String? = null,
  val vaultWheat: String? = null,
  val vaultFish: String? = null,
  val blueprintBalance: String? = null,
)

@Serializable
data class SnapshotWire(
  val tick: Double,
  val tickEpoch: TickEpochWire? = null,
  val seasonStartTick: Double = 0.0,
  val seasonEndTick: Double = 0.0,
  val clans: List<ClanWire> = emptyList(),
)

// ---- vault movements -------------------------------------------------

@Serializable
data class VaultMovementWire(
  val tick: Double,
  val type: String,         // "gain" | "spend"
  val amount: Double,
  val resource: String,
  val source: String,
  val timestamp: Double = 0.0,
)

// ---- clansmen --------------------------------------------------------

@Serializable
data class ClansmanWire(
  val id: String,
  val mission: String,
  val location: String,
  val eta: Double? = null,
  val cooldown: Double = 0.0,
  val hunger: Double = 0.0,
  /**
   * True when the chain reports `ClansmanState.DEAD` (enum=3). The cockpit
   * renders "DEAD" instead of the mission verb so a wiped clan reads as
   * fallen rather than "Idle / ready". Default false for back-compat with
   * older query payloads that didn't ship this field.
   */
  val isDead: Boolean = false,
)

// ---- 0G memory -------------------------------------------------------

@Serializable
data class MemoryKvWire(
  @SerialName("_id") val id: String? = null,
  val clanId: Double = 0.0,
  val key: String,
  val value: String,
  val source: String? = null,
  val updatedAt: Double = 0.0,
)

@Serializable
data class MemoryEventWire(
  @SerialName("_id") val id: String? = null,
  val tick: Double = 0.0,
  val clanId: Double = 0.0,
  val op: String,           // "read" | "write"
  val key: String,
  val note: String? = null,
)

@Serializable
data class BulletinWire(
  @SerialName("_id") val id: String? = null,
  val clanId: Double = 0.0,
  val slot: Double,
  val body: String,
  val updatedAt: Double = 0.0,
)

// ---- comms -----------------------------------------------------------

@Serializable
data class CombinedCommsWire(
  val kind: String,         // "whisper" | "orch" | "human"
  val tick: Double,
  val speaker: String,
  val body: String,
  val recipients: List<Double>? = null,
)
