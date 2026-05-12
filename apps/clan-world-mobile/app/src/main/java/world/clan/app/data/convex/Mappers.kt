package world.clan.app.data.convex

import world.clan.app.data.StubData
import java.math.BigInteger

/**
 * Wire → domain conversions. Each wire type maps to whichever StubData type
 * the existing tab render code already consumes — keeps the tabs' diff
 * minimal: live data flows through the same code paths as the stubs.
 */

private val WEI_PER_RESOURCE = BigInteger("1000000000000000000")

private fun wholeUnits(value: String?): Int {
  if (value.isNullOrBlank()) return 0
  return runCatching { BigInteger(value).divide(WEI_PER_RESOURCE).toInt() }
    .getOrElse { value.toIntOrNull() ?: 0 }
}

// ---- vault movements -------------------------------------------------

fun VaultMovementWire.toDomain(): StubData.VaultMovement {
  val isGain = type != "spend"
  val sign = if (isGain) "+" else "-"
  val amountInt = amount.toInt()
  return StubData.VaultMovement(
    tick = tick.toInt(),
    type = if (isGain) StubData.MovementType.Gain else StubData.MovementType.Spend,
    amount = "$sign$amountInt $resource",
    source = source,
  )
}

// ---- clan resources (from snapshot) ----------------------------------

private val resourceGlyphs = listOf(
  "◈" to "Gold",
  "⌬" to "Wood",
  "◆" to "Iron",
  "✦" to "Wheat",
  "◇" to "Fish",
  "▣" to "Blueprint",
)

fun ClanWire.toResources(): List<StubData.VaultResource> {
  val values = listOf(
    wholeUnits(goldBalance ?: treasury),
    wholeUnits(vaultWood),
    wholeUnits(vaultIron),
    wholeUnits(vaultWheat),
    wholeUnits(vaultFish),
    wholeUnits(blueprintBalance),
  )
  return resourceGlyphs.zip(values).map { (g, v) ->
    StubData.VaultResource(glyph = g.first, label = g.second, value = v)
  }
}

/** Pluck the clan record matching [clanId] from a snapshot. */
fun SnapshotWire.findClan(clanId: Int): ClanWire? =
  clans.firstOrNull { it.id == clanId.toString() }

// ---- clansmen --------------------------------------------------------

fun ClansmanWire.toDomain(): StubData.ClansmanRow = StubData.ClansmanRow(
  id = id,
  mission = mission,
  location = location,
  eta = eta?.toInt(),
  cooldown = cooldown.toInt(),
  hunger = hunger.toFloat(),
  isDead = isDead,
)

// ---- 0G memory -------------------------------------------------------

fun MemoryKvWire.toDomain(): StubData.KvRow =
  StubData.KvRow(key = key, value = value)

fun MemoryEventWire.toDomain(): StubData.CrudRow = StubData.CrudRow(
  tick = tick.toInt(),
  op = if (op.equals("write", ignoreCase = true)) StubData.CrudOp.Write else StubData.CrudOp.Read,
  key = key,
  note = note,
)

/** Per-clan bulletin row used by the 0G tab — formatted age relative to currentSlot. */
fun BulletinWire.toDomain(currentSlot: Int): StubData.BulletinRow {
  val ageTicks = (currentSlot - slot.toInt()).coerceAtLeast(0)
  return StubData.BulletinRow(age = "${ageTicks}t", body = body)
}

/** Public-bulletin post used by the comms public-view + bulletin flyout. */
fun BulletinWire.toPublicPost(): StubData.BulletinPost = StubData.BulletinPost(
  tick = slot.toInt(),
  speaker = "clan-${clanId.toInt()}",
  body = body,
)

// ---- comms -----------------------------------------------------------

fun CombinedCommsWire.toDomain(): StubData.CommsLine {
  val kindEnum = when (kind) {
    "whisper" -> StubData.CommsKind.Whisper
    "human"   -> StubData.CommsKind.Human
    else      -> StubData.CommsKind.Orch
  }
  return StubData.CommsLine(
    tick = tick.toInt(),
    kind = kindEnum,
    speaker = speaker,
    body = body,
    recipients = recipients?.map { it.toInt() } ?: emptyList(),
  )
}
