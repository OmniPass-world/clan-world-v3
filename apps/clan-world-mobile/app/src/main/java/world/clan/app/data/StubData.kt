package world.clan.app.data

/**
 * Realistic stub data mirroring the STUB_* constants in the web tab files.
 * Used directly by Vault/Clansman/0G/Comms tabs — live Convex wiring is a
 * follow-up PR per the plan's "Data wiring" section. Demos read clean off
 * these without any network calls.
 *
 * Speaker conventions match the web (`clan-N`, `iNFT Owner`, `orchestrator`).
 */
object StubData {

  const val CURRENT_TICK = 6
  const val VISIBLE_TICKS = 4

  // ---- Vault -----------------------------------------------------------

  data class VaultResource(val glyph: String, val label: String, val value: Int)
  data class VaultMovement(val tick: Int, val type: MovementType, val amount: String, val source: String)
  enum class MovementType { Gain, Spend }

  fun vaultResources(clanId: Int): List<VaultResource> {
    // Clan-flavored, realistic numbers; differentiates panels visually.
    val seed = clanId
    return listOf(
      VaultResource("◈", "Gold",       240 + seed * 23),
      VaultResource("⌬", "Wood",       180 + seed * 11),
      VaultResource("◆", "Iron",        96 + seed * 7),
      VaultResource("✦", "Wheat",       64 + seed * 9),
      VaultResource("◇", "Fish",        38 + seed * 4),
      VaultResource("▣", "Blueprint",   if (clanId % 2 == 0) 1 else 0),
    )
  }

  val vaultMovements: List<VaultMovement> = listOf(
    VaultMovement(4, MovementType.Gain,  "+45 gold",  "raid · forest"),
    VaultMovement(3, MovementType.Spend, "-12 wood",  "mill upkeep"),
    VaultMovement(2, MovementType.Gain,  "+8 stone",  "quarry"),
    VaultMovement(1, MovementType.Gain,  "+24 gold",  "tribute"),
  )

  // ---- Clansmen --------------------------------------------------------

  data class ClansmanRow(
    val id: String,
    val mission: String,
    val location: String,
    val eta: Int?,        // ticks until mission ends; null = idle
    val cooldown: Int,    // ticks of cooldown remaining; 0 = ready
    val hunger: Float,    // 0..1
    val isDead: Boolean = false, // true = chain reports ClansmanState.DEAD; row renders darkened
  )

  fun clansmen(clanId: Int): List<ClansmanRow> = listOf(
    ClansmanRow("C1", "Raid",   "Forest",     eta = 2, cooldown = 0, hunger = 0.4f + 0.05f * clanId),
    ClansmanRow("C2", "Mill",   "East Farms", eta = 1, cooldown = 0, hunger = 0.2f + 0.04f * clanId),
    ClansmanRow("C3", "Idle",   "Home",       eta = null, cooldown = 3, hunger = 0.78f),
    ClansmanRow("C4", "Quarry", "Mountains",  eta = 4, cooldown = 0, hunger = 0.55f),
  )

  // ---- 0G (iNFT memory) ------------------------------------------------

  data class KvRow(val key: String, val value: String)
  data class CrudRow(val tick: Int, val op: CrudOp, val key: String, val note: String?)
  enum class CrudOp { Read, Write }
  data class BulletinRow(val age: String, val body: String)

  fun kv(clanId: Int): List<KvRow> = listOf(
    KvRow("last_grudge",     "clan-${(clanId % 4) + 1}"),
    KvRow("wood_threshold",  (60 + clanId * 5).toString()),
    KvRow("pref_target",     listOf("forest", "mountain", "river", "orchard")[clanId - 1]),
    KvRow("mood",            listOf("aggressive", "cautious", "volatile", "patient")[clanId - 1]),
  )

  fun crud(clanId: Int): List<CrudRow> = listOf(
    CrudRow(4, CrudOp.Write, "mood",            "${kv(clanId)[3].value} → wary"),
    CrudRow(3, CrudOp.Read,  "last_grudge",     "planning retort"),
    CrudRow(2, CrudOp.Write, "wood_threshold",  "raise to ${kv(clanId)[1].value}"),
    CrudRow(1, CrudOp.Read,  "pref_target",     "mission seeding"),
  )

  fun bulletinsForOwner(clanId: Int): List<BulletinRow> = when (clanId) {
    1 -> listOf(BulletinRow("2t", "\"Wood scarce — millers prioritize.\""), BulletinRow("5t", "\"Crimson moves — watch the river.\""))
    2 -> listOf(BulletinRow("1t", "\"Hold the orchard line.\""), BulletinRow("4t", "\"No raid this tick — defensive only.\""))
    3 -> listOf(BulletinRow("3t", "\"Forest opens. Strike consideration.\""), BulletinRow("6t", "\"Markets twitch — read the river.\""))
    else -> listOf(BulletinRow("2t", "\"North pass clear; share scout report.\""), BulletinRow("5t", "\"Patient yield — five-tick curve holding.\""))
  }

  data class InftMeta(val tokenId: String, val owner: String, val archetype: String, val stateRoot: String, val encrypted: Boolean, val version: String)

  fun inftMeta(clanId: Int): InftMeta = InftMeta(
    tokenId   = "0G-${clanId.toString().padStart(4, '0')}",
    owner     = "0x4a${(clanId * 0x4f3a).toString(16).padStart(4, '0')}…b1",
    archetype = elderById(clanId).archetype,
    stateRoot = "0x${(clanId * 0xabc12345).toLong().toString(16).take(16)}…",
    encrypted = true,
    version   = "v0.${clanId}.7",
  )

  // ---- Comms -----------------------------------------------------------

  enum class CommsKind { Whisper, Orch, Human }
  data class CommsLine(
    val tick: Int,
    val kind: CommsKind,
    val speaker: String,
    val body: String,
    val recipients: List<Int> = emptyList(),
  )

  data class BulletinPost(val tick: Int, val speaker: String, val body: String)

  private val COMMS_BY_CLAN: Map<Int, List<CommsLine>> = mapOf(
    1 to listOf(
      CommsLine(6, CommsKind.Whisper, "clan-1",       "Forest patrol — reporting two travelers near west bank.", listOf(2, 3, 4)),
      CommsLine(6, CommsKind.Orch,    "orchestrator", "Tick T06 begun. Yield <directives>."),
      CommsLine(5, CommsKind.Whisper, "clan-3",       "AXL: \"trade ore for wood, 2:1?\""),
      CommsLine(4, CommsKind.Human,   "iNFT Owner",   "Slow your raids — diplomacy first."),
      CommsLine(4, CommsKind.Whisper, "clan-2",       "AXL: declined; counter offered 3:1."),
      CommsLine(3, CommsKind.Whisper, "clan-1",       "Acknowledged — pulling raid plan. Patrols only.", listOf(3)),
      CommsLine(3, CommsKind.Orch,    "orchestrator", "Bandit camp surfaced at forest."),
      CommsLine(2, CommsKind.Whisper, "clan-4",       "AXL: \"north pass clear, sharing scout report.\""),
      CommsLine(1, CommsKind.Whisper, "clan-1",       "Trade window — willing to swap ore favor for wood.", listOf(2, 4)),
      CommsLine(0, CommsKind.Orch,    "orchestrator", "Season started. Four clans seeded."),
    ),
    2 to listOf(
      CommsLine(6, CommsKind.Orch,    "orchestrator", "Tick T06 begun. Yield <directives>."),
      CommsLine(6, CommsKind.Whisper, "clan-2",       "Counter to clan-3: ore for wood at 3:1.", listOf(3)),
      CommsLine(5, CommsKind.Whisper, "clan-1",       "AXL: \"patrol report — bandit camp confirmed forest.\""),
      CommsLine(4, CommsKind.Human,   "iNFT Owner",   "Hold positions. Do not engage bandits this tick."),
      CommsLine(4, CommsKind.Whisper, "clan-2",       "Confirmed — defensive hold maintained.", listOf(1, 3, 4)),
      CommsLine(3, CommsKind.Whisper, "clan-2",       "Vault status: 240 ore / 180 wood. Stockpile holding.", listOf(1, 4)),
      CommsLine(2, CommsKind.Orch,    "orchestrator", "Bandit camp surfaced at forest."),
      CommsLine(1, CommsKind.Whisper, "clan-4",       "AXL: \"trade window — wood for ore?\""),
      CommsLine(0, CommsKind.Orch,    "orchestrator", "Season started. Four clans seeded."),
    ),
    3 to listOf(
      CommsLine(6, CommsKind.Whisper, "clan-3",       "\"Volatile day. Considering raid on bandit camp.\"", listOf(1, 2, 4)),
      CommsLine(5, CommsKind.Orch,    "orchestrator", "Tick T05 begun. Yield <directives>."),
      CommsLine(5, CommsKind.Whisper, "clan-3",       "AXL: \"trade ore for wood, 2:1?\"", listOf(2)),
      CommsLine(4, CommsKind.Whisper, "clan-2",       "AXL: declined; counter offered 3:1."),
      CommsLine(3, CommsKind.Human,   "iNFT Owner",   "Crimson — pace yourself. Don't blow the season on T08."),
      CommsLine(3, CommsKind.Orch,    "orchestrator", "Bandit camp surfaced at forest."),
      CommsLine(2, CommsKind.Whisper, "clan-3",       "Noted. Eyes still on the forest.", listOf(1)),
      CommsLine(1, CommsKind.Whisper, "clan-1",       "AXL: \"Storm Riders moving — forest sweep T07.\""),
      CommsLine(0, CommsKind.Orch,    "orchestrator", "Season started. Four clans seeded."),
    ),
    4 to listOf(
      CommsLine(6, CommsKind.Whisper, "clan-4",       "\"north pass clear, sharing scout report.\"", listOf(1, 2, 3)),
      CommsLine(5, CommsKind.Orch,    "orchestrator", "Tick T05 begun. Yield <directives>."),
      CommsLine(5, CommsKind.Whisper, "clan-1",       "AXL: \"patrol moving north, request escort.\""),
      CommsLine(4, CommsKind.Human,   "iNFT Owner",   "Verdant — hold the orchard rotation through T08."),
      CommsLine(4, CommsKind.Whisper, "clan-4",       "Confirmed. Five-tick yield protected.", listOf(1, 2, 3)),
      CommsLine(3, CommsKind.Orch,    "orchestrator", "Bandit camp surfaced at forest."),
      CommsLine(2, CommsKind.Whisper, "clan-4",       "Ore reserves stable; wood surplus building.", listOf(2)),
      CommsLine(1, CommsKind.Whisper, "clan-2",       "AXL: trade window — wood for ore?"),
      CommsLine(0, CommsKind.Orch,    "orchestrator", "Season started. Four clans seeded."),
    ),
  )

  fun commsLines(clanId: Int): List<CommsLine> = COMMS_BY_CLAN[clanId].orEmpty()

  private val PUBLIC_BULLETINS_BY_CLAN: Map<Int, List<BulletinPost>> = mapOf(
    1 to listOf(
      BulletinPost(6, "clan-1", "STORM RIDERS DECLARE: forest sweep tick T07. Allied clans welcome."),
      BulletinPost(5, "clan-1", "\"Speed wins what stillness loses.\" — Aldric."),
      BulletinPost(4, "clan-1", "Patrols out from west bank. Two travelers tracked, non-hostile."),
      BulletinPost(2, "clan-1", "Open call for ore — willing to trade favor in T09 raid."),
      BulletinPost(0, "clan-1", "Storm Riders enter the season. May winds favor the bold."),
    ),
    2 to listOf(
      BulletinPost(6, "clan-2", "IRON GUARD POSTS: vault at 240 ore / 180 wood. Trade window open."),
      BulletinPost(5, "clan-2", "Trade counter posted: ore for wood at 3:1."),
      BulletinPost(4, "clan-2", "No raid commitment T07. Defensive posture maintained."),
      BulletinPost(2, "clan-2", "Cautious accumulation continues. Stockpile is freedom."),
      BulletinPost(0, "clan-2", "Iron Guard begins T0 with patient resolve."),
    ),
    3 to listOf(
      BulletinPost(6, "clan-3", "CRIMSON: opportunistic raid eyed for T08. Watch the forest."),
      BulletinPost(4, "clan-3", "Volatility is a weapon. The patient miss the moment."),
      BulletinPost(2, "clan-3", "Crimson holds — but only because the moment is wrong."),
      BulletinPost(0, "clan-3", "Crimson colors raised. Watch the markets."),
    ),
    4 to listOf(
      BulletinPost(6, "clan-4", "VERDANT WARDENS: orchard rotation steady. Five-tick yield curve holds."),
      BulletinPost(5, "clan-4", "Scout report posted: north pass clear of hostiles."),
      BulletinPost(3, "clan-4", "Long view: investing two ticks of wood toward T10 surplus."),
      BulletinPost(1, "clan-4", "Wardens accept all incoming whispers. We answer in kind."),
      BulletinPost(0, "clan-4", "Verdant Wardens take the field. The patient inherit."),
    ),
  )

  fun publicBulletins(clanId: Int): List<BulletinPost> = PUBLIC_BULLETINS_BY_CLAN[clanId].orEmpty()
}

/** tick-distance opacity ladder (web's fadeOpacity). Older ticks fade. */
fun fadeOpacityForTick(tick: Int): Float {
  val distance = (StubData.CURRENT_TICK - tick).coerceAtLeast(0)
  return (1f - distance * 0.12f).coerceIn(0.4f, 1f)
}
