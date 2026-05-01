# PR #194 Spec-Compliance UAT — `dev-phase-9-bandits`

**Reviewer:** Claude Opus 4.7 (1M ctx) — static spec-compliance pass
**HEAD:** `218f9020ecb0f4b8277ef59dd55de8e004404d80`
**Date:** 2026-04-29
**Method:** read spec docs → walk impl → no test execution

> Scope: does the bandit implementation in `packages/contracts/src/ClanWorld.sol` match the documented v4 spec ruleset for bandit mechanics? This is **not** a re-run of the cloud reviewers; it is an **independent spec-vs-code audit** focused on whether shipped behavior matches the canonical contract spec.

---

## 1. Spec sources read

| Doc | Bandit-relevant sections |
|---|---|
| `docs/planning/clanworld_v4_spec.md` | §6.1–6.18 (the canonical bandit algorithm: spawn cadence, state machine, targeting, defense, outcomes, loot, blueprint reward, bookkeeping); §4.14 (bandit drop overflow) |
| `docs/planning/clanworld_v4_1_addendum.md` | A3 (movement = 0 ticks between regions); A4 (eager-settle scope: ALL bases in bandit's current region); A9 (starving defenders contribute 0); A11 (equal defender split rule) |
| `docs/planning/clanworld_v4_2_state_schema_interface_spec.md` | §5 constants (BANDIT_COOLDOWN_TICKS=10, CAMP=3, REST=2, MAX_ATTEMPTS=6, STEAL_BPS=2000, DROP_BPS=5000); §7.7 BanditTroop layout; §7.10 defender registries; §7.11 defense contribution map; §8.2 DefendBase persistent semantics |
| `docs/planning/clanworld_v4_3_schema_patch.md` | E (domain-separated RNG with `bandit_spawn`, `bandit_spawn_region` keys); F (defender registry cleanup); G (`tier` is canonical, `attackPower = getBanditAttackPower(tier)`); H (loot value getter split: raw vs settled) |

**Authoritative ruleset:** v4 spec + v4_1 addendum (which controls on conflict) + v4_2 schema + v4_3 patches. The v4_5 alignment addendum has no bandit-specific changes. No phase-9-specific design doc exists; the implementation plan at `clanworld_numbered_implementation_plan.md` only enumerates issue-level steps, not new mechanics.

Key spec assertions extracted (one-line each):
- **6.1** at most ONE bandit troop active globally
- **6.2** spawn regions = {Forest, Mountains, W/E Farms, W/E Docks}, NEVER UnicornTown or DeepSea
- **6.3** GLOBAL 10-tick cooldown after defeat/escape; chance starts 5%, +1% per missed tick, cap 20%
- **6.4** uniform random region pick, single troop per spawn
- **6.5** states = {CAMPING, RESTING, ATTACKING, DEFEATED, ESCAPED} (5)
- **6.6** camp 3 ticks before first attack
- **6.8** target = highest loot-value base in current region; tiebreak = LOWEST clanId
- **6.9** loot value = wood + wheat + 2*fish + 4*iron (vault only, gold = 0)
- **6.10** rampage path = 1 → 2 → 5 → 7 → 6 → 4 → 1 (clockwise)
- **6.11** rest 2 ticks → MOVE to next region → next attack
- **6.12** max 6 attempts; 7th = ESCAPED + carried loot burned
- **6.13** defense = clansmanDefense(10/5/0) + wallDefense(10*wall) + baseDefense(5*base)
- **6.14** tier-based attack power: 30/45/60/80/95
- **6.15A** clansmanDef ≥ atk → win, no damage
- **6.15B** totalDef ≥ atk > clansmanDef → win, wall -1, no base damage, NO casualties
- **6.15C** totalDef < atk → bandits steal 20% each vault resource (wood/wheat/fish/iron, NOT gold), wall -1, base 0 damage, **no clansman casualties in v1**, bandit alive + continues
- **6.16** bandits carry stolen physical resources internally; no gold conversion
- **6.17** on defeat: 50% drop split equally among defenders (any nonzero contribution), 50% burned, base vault gets 1e18 Blueprint + 1e18 Gold
- **6.18** must track which clansmen contributed nonzero defense for distribution
- **A3** inter-region movement is 0 extra ticks
- **A4** eager-settle ALL bases in bandit's current region before target pick
- **G** `tier` canonical, `attackPower` derived

---

## 2. Mechanic-by-mechanic verification

| Mechanic | Spec says | Code does | File:line | Verdict |
|---|---|---|---|---|
| **Active troop limit** | At most ONE active globally (§6.1) | `MAX_TOTAL_BANDITS = 8`, `MAX_BANDITS_PER_REGION = 3` | `ClanWorld.sol:93-94` | ❌ DRIFT — fundamentally different scaling model |
| **Eligible spawn regions** | 6 outer-ring only; NEVER region 3 (UnicornTown) or 8 (DeepSea) (§6.2) | No region-id allowlist; spawn regions filtered only by `regionWeights[r] != 0`. Weights are populated for ANY region that has clansmen present (incl. clansmen passing through UnicornTown / DeepSea on market trips or DeepFishing) | `ClanWorld.sol:2247-2266`, `2169-2181` | ❌ DRIFT — bandits CAN spawn in region 3/8 if any clansman is there. No explicit ban. |
| **Spawn cadence — base chance** | 5% per tick post-cooldown (§6.3) | Starts at 0; first eligible tick increments to `BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS = 1000` (=10%) | `ClanWorld.sol:91, 2179, 2117` | ⚠️ DRIFT — base chance is 10%, not 5% |
| **Spawn cadence — increment** | +1% per missed tick (§6.3) | +10% per missed tick (1000 bps) | `ClanWorld.sol:91, 2117` | ❌ DRIFT — 10x faster ramp |
| **Spawn cadence — cap** | 20% (§6.3) | 80% (`BANDIT_SPAWN_MAX_PROBABILITY_BPS = 8000`) | `ClanWorld.sol:92, 2118` | ❌ DRIFT — 4x higher saturation |
| **Cooldown trigger** | GLOBAL, starts on defeat/escape (§6.3) | Per-region, starts on **spawn** (not on resolution). No global cooldown. | `ClanWorld.sol:1554-1556, 2096, 2175` | ❌ DRIFT — wrong semantics. After a bandit is defeated/escaped, OTHER regions can spawn immediately, and the spawn region's cooldown started 10 ticks ago (likely already expired). |
| **Cooldown duration** | 10 ticks (§6.3) | 10 ticks (`MIN_SPAWN_COOLDOWN_TICKS = BANDIT_COOLDOWN_TICKS`) | `ClanWorld.sol:90` | ✅ MATCHES |
| **Spawn region selection** | Uniform random over eligible (§6.4) | Loot-and-clansman-weighted region scoring (`weights[r] += 100 + lootValue/1e18; +25 per clansman in region`), then RNG-weighted pick | `ClanWorld.sol:2230-2269, 2138-2147` | ❌ DRIFT — selection is biased toward loot-rich and clansman-dense regions, not uniform. Intentional "heuristic" per code comment line 96-98 but contradicts spec §6.4. |
| **Bandit state set** | {CAMPING, RESTING, ATTACKING, DEFEATED, ESCAPED} (§6.5) | {None, Spawned, Camped, Resting, Attacking, Defeated, Escaped} (7) | `IClanWorld.sol:107-115` | ⚠️ DRIFT — extra `None` (zero sentinel — defensible) and extra `Spawned` (1-tick pre-camp delay — adds 1 unspecified tick to spawn-to-attack path) |
| **Camp duration** | 3 ticks (§6.6) | `BANDIT_CAMP_TICKS = 3` from `Camped` entry; but `Spawned → Camped` requires 1 extra tick first | `ClanWorld.sol:1605, 1612` | ⚠️ DRIFT — effective spawn-to-attack = 1 (Spawned) + 3 (Camped) = **4 ticks**, not 3 |
| **Targeting time** | "Current vault state at attack resolution time" (§6.7) | Target picked at Camped→Attacking transition in `_advanceBanditStates` (one tick BEFORE attack resolves), not at resolution. After picking, `_resolveBanditAttack` re-settles target+defenders but does NOT re-pick target. | `ClanWorld.sol:1681, 1762` | ⚠️ DRIFT — picked one tick early. Vault state may differ between pick-tick and resolve-tick. |
| **Target eligibility** | Bases in bandit's current region (§6.8) | `clan.baseRegion != bandit.region` filters | `ClanWorld.sol:1711` | ✅ MATCHES |
| **Empty-region noop** | "If no base exists in the region, attack resolution is a noop" (§6.8) | If `_pickBanditAttackTarget` returns 0, transitions to `Escaped` (which then leads to Resting→Camped per impl loop) | `ClanWorld.sol:1681-1684` | ⚠️ DRIFT — spec says noop (continue to next region); impl marks Escaped (terminal in spec, but impl's Escaped is non-terminal) |
| **Tiebreak rule** | Lowest clanId wins (§6.8) | RNG-bounded uniform pick across tied clans | `ClanWorld.sol:1733-1734` | ❌ DRIFT — non-deterministic tiebreak. Spec explicitly calls out lowest-clanId for determinism. |
| **Loot-value formula** | wood + wheat + 2*fish + 4*iron (vault only, gold=0) (§6.9) | `clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4` | `ClanWorld.sol:3410-3412` | ✅ MATCHES |
| **Quote getter split (raw vs settled)** | Both must exist (§v4.3 H) | Both implemented | `ClanWorld.sol:3400-3407` | ✅ MATCHES |
| **Rampage path** | 1→2→5→7→6→4→1 clockwise (§6.10) | NOT IMPLEMENTED. Bandit `region` field is set on spawn and never changed. | (no code) | ❌ MISSING — bandits stay in spawn region forever |
| **Inter-region move ticks** | 0 (A3) | n/a (no movement) | (no code) | ❌ MISSING |
| **Rampage cycle** | attack → REST(2 ticks) → next region → attack (§6.11) | attack → ESCAPED(2 ticks) → RESTING(2 ticks) → CAMPED(3 ticks) → attack same region | `ClanWorld.sol:1689-1697` | ❌ DRIFT — wrong cycle shape, wrong duration (~7 ticks vs 2), no movement |
| **Max attempts cap** | 6 attacks then ESCAPED + loot burned + removed from world (§6.12) | NO `attackAttemptsMade` counter exists in struct (`IClanWorld.sol:303-315`); ABI view returns `attackAttemptsMade: 0` always | `ClanWorld.sol:3571` | ❌ MISSING — bandit can attack forever (until target dies or is defeated) |
| **ESCAPED state semantics** | Terminal: troop removed from world, all carried loot burned (§6.12) | Non-terminal: ESCAPED → RESTING → CAMPED, indefinite loop. No loot burn (carry is always 0 anyway). | `ClanWorld.sol:1619-1621, 1689-1692` | ❌ DRIFT — fundamentally wrong lifecycle |
| **Defense components** | clansmanDefense + wallDefense (10×wall) + baseDefense (5×base) (§6.13) | RNG-rolled per-clansman defense damage `[0,100]` + wall absorbs up to `WALL_HP_PER_LEVEL=100` damage + base absorbs up to `BASE_HP_PER_LEVEL*baseLevel = 25*L` damage | `ClanWorld.sol:1969-2001, 2065-2074` | ❌ DRIFT — completely different model. Damage-based HP system instead of threshold-comparison. |
| **Clansman defense — defend_base** | Each contributes 10 (§6.13) | Each contributes RNG `[0,100]` per attack | `ClanWorld.sol:2065-2074` | ❌ DRIFT — value + nondeterminism |
| **Clansman defense — WAITING at home** | Each contributes 5 (§6.13) | NOT counted at all. Only `mission.action == DefendBase` workers contribute. | `ClanWorld.sol:1959-1962` | ❌ MISSING — passive home-defenders give 0 |
| **Starving clansman defense** | Contributes 0 (§6.13, A9) | Whole clan filtered out via `_isStarving(defenderClan)` | `ClanWorld.sol:1920, 1950` | ✅ MATCHES (test: `test_starvingDefenderContributesZeroDefense`) |
| **Wall defense formula** | 10 × wallLevel (§6.13) | Wall absorbs 100 damage per level (`WALL_HP_PER_LEVEL = 100`) | `ClanWorld.sol:111, 1978-1985` | ❌ DRIFT — different scaling (10x stronger walls in damage units) |
| **Base defense formula** | 5 × baseLevel (§6.13) | 25 × baseLevel (`BASE_HP_PER_LEVEL = 25`) | `ClanWorld.sol:112, 1998` | ❌ DRIFT — 5x stronger |
| **Bandit attack tiers** | tier 1=30 / 2=45 / 3=60 / 4=80 / 5=95 (§6.14) | Random uint32 strength in `[100, 250]` (`MIN_BANDIT_SPAWN_STRENGTH=100`, `BANDIT_SPAWN_STRENGTH_SPREAD=151`) | `ClanWorld.sol:108-109, 2149-2154` | ❌ DRIFT — no tiers; strength is random in completely different range; no `getBanditAttackPower` helper. v4_3 G says `tier` is canonical — impl has no `tier` field |
| **`tier` canonical (v4.3 G)** | `BanditTroop.tier` is source of truth | Struct has no `tier` field; ABI emits `tier: 0` always | `IClanWorld.sol:303-315`, `ClanWorld.sol:1562, 3575` | ❌ MISSING |
| **Attack outcome A (clean win)** | clansmanDef ≥ atk → win, NO wall/base/clansman damage (§6.15A) | If `totalClansmanDefense >= attackPower * 2` → defeated; no other damage applied | `ClanWorld.sol:1802, 1807` | ⚠️ DRIFT — uses `2x` factor. Threshold differs from spec's `1x`. |
| **Attack outcome B (partial)** | totalDef ≥ atk > clansmanDef → win, wall -1, base 0 dmg (§6.15B) | NOT a distinct case; if `totalClansmanDefense < 2*attackPower`, falls into "loss" path (wall+base absorb damage, clansman casualties applied) | `ClanWorld.sol:1807-1814` | ❌ DRIFT — no Case B branch. Either you reach 2x clansman def or you take damage including casualties. |
| **Attack outcome C (loss)** | totalDef < atk → bandits steal 20% of EACH vault resource (wood/wheat/fish/iron); NO base damage; NO clansman casualties (v1) (§6.15C) | Wall/base/clansman absorb damage in tiers; **clansmen DIE** when wall+base saturate (line 2003-2032); **NO vault theft anywhere in code** | `ClanWorld.sol:2003-2032` | ❌ DRIFT — flagrantly wrong outcome. Bandits never steal loot. Clansmen die instead (spec says no v1 deaths from bandits). |
| **`BANDIT_BASE_STEAL_BPS = 2000`** | constant for 20% theft (v4.2 §5) | declared but **NEVER REFERENCED** anywhere in code | `IClanWorld.sol:71` | ❌ MISSING — dead constant |
| **`BANDIT_DROP_TO_DEFENDERS_BPS = 5000`** | constant for 50% defender drop (v4.2 §5) | declared but **NEVER REFERENCED** | `IClanWorld.sol:72` | ❌ MISSING — dead constant. Distribution code uses full carry (which is always 0). |
| **Bandit loot inventory** | Bandits carry stolen resources until defeat/escape (§6.16) | `BanditTroop.carryWood/Iron/Wheat/Fish/Gold` exists in struct, BUT no production code path ever sets them. Only test harness `setBanditCarry` populates. | `IClanWorld.sol:310-314`, `ClanWorld.sol:1545-1549` | ❌ MISSING — carry is implemented as a struct field but never populated outside tests |
| **No gold conversion** | Bandits don't convert resources to gold; no Unicorn Town in v1 (§6.16) | n/a (no theft happens, no conversion happens). `carryGold` field exists outside spec — minor schema drift only relevant if theft were implemented. | — | ⚠️ DRIFT — schema includes `carryGold` not in spec; behaviorally moot |
| **Defeat reward — 50% drop** | 50% of carry split equally among defenders with nonzero contribution (§6.17, A11) | Distributes 100% of carry (no halving), split equally per **defending CLAN** (one share per defending clan, not per defender). Whole-token rounding, residue burned. | `ClanWorld.sol:1843-1891` | ❌ DRIFT (1) — uses 100% not 50%; (2) — splits per clan not per clansman |
| **Defeat reward — defender granularity** | "Split evenly among all defending clansmen who contributed nonzero defense" (§6.17), reaffirmed in A11 | Splits per CLAN (each unique clan with ≥1 active defender gets 1 share) — see `_activeDefendingClanIds` and per-clan loop | `ClanWorld.sol:1843-1867, 1893-1912` | ❌ DRIFT — clan-level granularity, not clansman-level. Test `test_multipleDefendersFromSameClanStillReceiveOneClanShare` codifies this (3 defenders from same clan = 1 share). Direct contradiction of A11. |
| **Defeat reward — burn 50%** | Remaining 50% burned (§6.17) | Whole-token-rounding residue burned, but the "burn 50%" semantics is absent | `ClanWorld.sol:1878-1882` | ❌ DRIFT |
| **Carry overflow → burn** | "any clansman loot above carrying capacity is burned" (§6.17) | n/a — distribution goes directly to vault, not clansman carry | `ClanWorld.sol:1862-1867` | ⚠️ DRIFT — different design (clan vault deposit instead of clansman carry); no overflow because vaults have no per-resource cap during defeat reward |
| **Blueprint reward** | 1e18 Blueprint + 1e18 Gold to defended base vault (§6.17) | 1e18 Blueprint to target clan, **NO 1e18 Gold** | `ClanWorld.sol:1834-1835` | ⚠️ DRIFT — gold reward missing |
| **Defender contribution bookkeeping** | Track which clansmen contributed nonzero defense (§6.18) | Only checks IS-defending (mission active + at base + DefendBase action). RNG roll is `[0, 100]`; could be 0. Code does NOT filter clansmen with 0 roll from reward eligibility. | `ClanWorld.sol:1939-1967` | ⚠️ DRIFT — every active defender gets reward share regardless of actual roll, even if RNG returned 0 defense. (Per-clansman doesn't matter much because impl rewards per-clan anyway.) |
| **Domain-separated RNG** | `bandit_spawn`, `bandit_spawn_region` keys (v4.3 E) | Uses `DOMAIN_BANDIT_SPAWN = keccak256("clanworld.bandit.spawn.v1")` for both spawn-roll and spawn-region; differentiated by per-region nonces | `ClanWorld.sol:88, 2133-2147` | ⚠️ DRIFT — single domain key with nonce-differentiation works but doesn't match spec's two-key recommendation. Functionally OK if nonces are non-colliding. |
| **Eager-settle scope** | ALL bases in bandit's current region (A4) | Only spawn-CANDIDATE regions get eager-settle (`_eagerSettleForBandits`). For an active bandit's region, only settles the eventually-picked target, not all rivals. | `ClanWorld.sol:2156-2167, 1781-1791` | ❌ DRIFT — already flagged by Codex 5.4 in r2 super-swarm; confirmed real |
| **DefendBase persistence** | Continuous; doesn't auto-complete after one event (§v4.2 8.2) | Marked "persistent defender mission" line 382; defender stays active across attacks | `ClanWorld.sol:382` | ✅ MATCHES |
| **Defender registry cleanup on death** | Clear when defender dies (v4.3 F.2) + when defended clan dies (F.3) | `_clearDefender` on clansman death; `_releaseDefendersForDeadTarget` on clan death | `ClanWorld.sol:565-588, 2771-2792` | ✅ MATCHES |
| **Dead-target cleanup of attacks** | (implicit; not explicit in spec but required for sane lifecycle) | `_abortBanditAttacksForDeadTarget` transitions to Escaped when target dies | `ClanWorld.sol:590-608` | ✅ MATCHES (test: `test_deadTargetCleanupReleasesDefendersAndEscapesBandit`) |
| **Winter interaction — starving defender** | 0 defense (A9) | Filtered via `_isStarving` | `ClanWorld.sol:1920, 1950` | ✅ MATCHES |
| **Winter interaction — bandit during winter** | (no explicit prohibition) | n/a | — | ✅ NO CONFLICT |

---

### Summary of mechanic verification

| Verdict | Count |
|---|---|
| ✅ MATCHES | 9 |
| ⚠️ DRIFT (close-but-different) | 12 |
| ❌ DRIFT/MISSING (significant) | 22 |

**The implementation is closer to a damage-based HP combat system with persistent regional troops than to the spec's threshold-comparison single-troop rampage model.** Multiple core mechanics — rampage path, attack-attempt cap, Case B/C outcomes, vault loot theft, terminal-Escaped state, clansman-granularity loot split — are either missing or replaced with substantially different mechanics.

---

## 3. Test coverage gap analysis

### What IS tested (from `Bandit.t.sol` + `BanditAttackResolution.t.sol` + `BanditSpawn.t.sol`)

State machine:
- Spawn happy path (`test_spawnBandit_recordsSpawnedTroopAndRegionIndex`)
- Spawned → Camped tick delay
- Camped → Attacking with target
- Attacking → Defeated deletes bandit
- Active-bandit promotion on defeat (`test_defeatingActiveBanditPromotesOldestRemainingBandit`)
- Escaped → Resting → Camped cycle (`test_attackingToEscapedRestingCampedCycleWorks`) — note: this test codifies the IMPL behavior that conflicts with spec §6.12
- Invalid transitions revert
- Per-region/global caps enforced
- Cooldown blocks second spawn
- Probability accumulator increments
- Spawn resets only selected region accumulator
- Eager-settle of candidate-region bases + defenders before spawn
- RNG nonce per-region independence
- Region selection deterministic for same seed

Attack resolution:
- e2e lifecycle through heartbeat (single clan, single bandit, no defenders)
- Defeated bandit awards blueprint
- Defeated bandit distributes harness-injected carry to single defending clan (full carry)
- 4-clan equal split with overflow burn
- Mixed-clan defense: blueprint only to base owner
- Multiple defeated bandits each award blueprint
- Winter starvation replay uses historical winter ticks
- `_resolveBanditAttack` returns safely when target dies during settlement
- Whole-token overflow burn rounding (3 defenders, 100e18 each)
- Multiple defenders from same clan still get one share
- Escaped bandit doesn't distribute carry
- Escaped bandit doesn't award blueprint
- Two alive defenders sufficient → defeat without wall chip
- Weak defense chips wall one level
- Wall-zero + weak defense kills clansman deterministically
- All clansmen dead → mark clan dead
- Starving defender contributes zero defense
- Two attacks same tick: determinism across replay
- Dead target cleanup releases defenders + escapes bandit

### What is NOT tested (keyed by criticality)

#### MUST-COVER (bug-class blockers)

| Scenario | Why critical |
|---|---|
| **Loot stealing on attack failure (§6.15C)** | The entire vault-theft mechanic is unimplemented and untested. No test asserts `bandit.carryWood += stolen` after a failed defense, nor that target vault decreases by 20%. This is THE core bandit mechanic. |
| **Attack-attempt cap (§6.12)** | No test verifies bandit is removed from world after 6 attacks. Currently bandits attack indefinitely; a long-running game would have stuck-state troops. |
| **Bandit movement to next region (§6.10–6.11)** | No test verifies region transition (impl doesn't do it). A multi-region rampage scenario is the bandit "experience"; missing. |
| **Carry-burn on escape (§6.12)** | No test that escape burns carried loot (also unimplemented). |
| **Tiebreak determinism — lowest clanId (§6.8)** | Tied loot values + RNG selection is non-deterministic across replay; spec wants determinism. No test asserts the lowest-clanId rule. |
| **WAITING-at-home defenders contribute 5 (§6.13)** | Spec mandates passive defenders count. Zero tests assert this; impl doesn't implement. |
| **Spawn region exclusion of UnicornTown(3) and DeepSea(8) (§6.2)** | A clansman in market trip in UnicornTown could trigger a spawn there. No test asserts the impossible-region constraint. |
| **Active-troop-limit = 1 (§6.1)** | All multi-bandit tests presuppose `MAX_TOTAL_BANDITS = 8`. No test asserts spec invariant of "at most one active troop". |

#### SHOULD-COVER (production-plausible edge cases)

| Scenario | Why |
|---|---|
| **Competitive target selection across multiple bases (R2 super-swarm finding)** | Two clans in same region with different vault values; verify highest-loot picked, then verify state of vault is settled-at-resolution time, not at pick-time. |
| **Attack resolution after vault drained between pick and resolve** | Spec says target picks from vault state at resolution. Impl picks one tick early. A defender clan that submits a vault-emptying market action between camp-end and attack-resolve should still get attacked? Or should the bandit re-pick? |
| **Loot value getter raw vs settled divergence** | Both getters exist (✅ matched), but no test asserts they DIFFER for an unsettled clan with pending mission. |
| **Blueprint awarded with 1e18 Gold (§6.17)** | Test `test_defeatedBanditAwardsBlueprintToTargetClan` only asserts blueprint, not gold. |
| **Per-CLANSMAN drop split (§6.17 + A11)** | Current `test_multipleDefendersFromSameClanStillReceiveOneClanShare` enforces per-clan, which contradicts A11. A spec-faithful test would split 4 ways for a 1-clan-with-4-defenders defeat. |
| **Cooldown semantics: global vs per-region** | All cooldown tests are per-region. No test asserts "after defeat in Forest, Mountains is also blocked for 10 ticks" (which the spec demands). |

#### NICE-TO-HAVE

- Determinism across two parallel runs with different prevrandao but matched bandit pick sequence
- Heartbeat at exact tick of camp expiration (off-by-one boundary)
- Bandit attempt count near cap (5 attempts → 6 → escaped)
- Spawn weight = 0 region (no clans, no clansmen) — does not spawn even at max chance
- Spawn rate convergence: avg spawn-tick over many trials matches expected geometric distribution

**Headline gap count:** 8 MUST-COVER, 6 SHOULD-COVER, 5 NICE-TO-HAVE.

---

## 4. Potential UAT findings (if Liam runs interactive scenarios)

### Scenario 1 — Bandit lifecycle expectation mismatch
**Setup:** Mint 1 clan in Forest. Wait for bandit to spawn there (via heartbeat repeat with prevrandao). Submit no defenders.
**Expected per spec:** Bandit spawns → camps 3 ticks → attacks → if defense=0, takes 20% of vault → bandit alive with carry → moves to Mountains (next region clockwise) → 2-tick rest → next attack there → etc., max 6 attacks total.
**Actual per impl:** Bandit spawns → 1-tick Spawned → 3-tick Camped → 1-tick Attacking → DEALS DAMAGE TO CLANSMEN (kills them) → Escaped 2 ticks → Resting 2 ticks → Camped 3 ticks → ATTACKS SAME REGION AGAIN → forever.
**He should verify:** (a) does a clansman actually die in his runtime? (b) does the bandit's `region` field ever change? (c) does `attackAttemptsMade` ever increment? (d) does target vault ever decrease (loot theft)? Watch the indexer for region transitions.

### Scenario 2 — Loot theft mechanic
**Setup:** 1 clan, no defenders, full vault (20e18 wood, 20e18 wheat, 2e18 fish, 0 iron). Force bandit attack.
**Expected per spec:** Vault wood drops to 16e18, wheat to 16e18, fish to 1.6e18; bandit `carryWood = 4e18`, etc. Wall -1.
**Actual per impl:** Vault unchanged. Bandit `carryWood = 0`. One clansman dies if wall=0. Wall -1 if `wallLevel > 0`.
**He should verify:** Watch `getBandit(N).carryWood` after a failed attack — confirm it's 0 (proves theft is unimplemented). Also confirm that BanditAttackResolved event has `stolenWood/Iron/Wheat/Fish` all set to 0 (hardcoded in `ClanWorld.sol:1824-1827`).

### Scenario 3 — Multi-clan target selection in same region
**Setup:** Two clans both in same starting region (modulo `_mintClan` round-robin — may need to mint 6 to put two in Forest). Drain clan A's vault to 0; leave clan B with full vault.
**Expected:** Bandit picks clan B (higher loot value).
**Actual:** Likely matches IF bandit was about to attack and `_settleClan(B)` was called. But if both have stale settlement state (e.g., post-deposit pending tick), the eager-settle may be bypassed (R2 finding). Verify by checking `getBanditTargetPreview(banditId)` mid-camp vs after-resolve.

### Scenario 4 — Active-troop limit
**Setup:** Heartbeat for 60+ ticks with multiple clans across regions and high spawn chance.
**Expected per spec:** at most 1 active bandit at a time (§6.1).
**Actual:** Up to 8 active bandits (3 per region max).
**He should verify:** count `BanditSpawned` events in window; should see at most 1 simultaneously, but will see up to 8.

### Scenario 5 — Attack-attempt cap
**Setup:** Spawn bandit in Forest, no defenders ever. Run heartbeat for 50+ ticks.
**Expected per spec:** After ~6 attempts (~12-15 ticks of cycling), bandit ESCAPED + removed from world.
**Actual:** Bandit cycles indefinitely, attacking same clan forever (or until clan dies).
**He should verify:** `getBanditsInRegion(1).length` should drop to 0 after ~15 ticks per spec; in impl it stays 1 indefinitely.

### Scenario 6 — UnicornTown spawn ban
**Setup:** Have several clansmen send market missions to UnicornTown. While they're there, run many heartbeats.
**Expected per spec:** Bandit MUST NOT spawn in region 3.
**Actual:** Possible (because clansman-presence weights non-zero spawn region 3). Whether it actually triggers depends on clansman density and roll, but the SAFETY CHECK is missing.
**He should verify:** instrument the test to assert `getBandit(N).region != 3 && getBandit(N).region != 8` over many spawns.

### Scenario 7 — Defender contribution rule (WAITING)
**Setup:** Clan with 4 clansmen all WAITING at home (no `DefendBase` mission), wallLevel=0. Force bandit attack with strength=15.
**Expected per spec:** clansmanDefense = 4 × 5 = 20 ≥ 15 → clean win, no damage.
**Actual:** clansmanDefense = 0 (WAITING workers not counted). Bandit wins, kills a clansman.
**He should verify:** check `livingClansmen` after attack; spec says it stays 4, impl will show 3.

### Scenario 8 — Tiebreak determinism
**Setup:** 2 clans in same region with identical 0-loot vaults.
**Expected per spec:** lowest clanId targeted always.
**Actual:** RNG-bounded uniform pick. Across two harness runs with different prevrandao, target may differ.
**He should verify:** run scenario twice with different randao sources; confirm target switches (impl) vs stays (spec).

---

## 5. UAT verdict

**PRE-UAT WORK NEEDED — DO NOT RUN INTERACTIVE UAT YET.**

The bandit implementation in PR #194 is closer to a **standalone "raid simulator"** than to the v4 spec's bandit algorithm. Multiple core mechanics are either missing or replaced with substantially different behavior:

- ❌ Single active troop → 8-troop concurrent
- ❌ Rampage path 1→2→5→7→6→4 → bandit never moves
- ❌ Vault loot theft (the central tension mechanic) → never implemented
- ❌ 6-attempt cap with terminal Escaped → infinite attack loop
- ❌ Threshold-comparison combat → damage-HP combat with clansman casualties
- ❌ Per-clansman loot split (A11) → per-clan
- ❌ Lowest-clanId tiebreak → RNG
- ❌ WAITING-at-home defenders count for 5 → ignored
- ❌ Global cooldown on resolve → per-region cooldown on spawn
- ❌ 5%/+1%/20% spawn curve → 0%/+10%/80%
- ❌ Tier-based attack power (30..95) → random strength (100..250)

**Two interpretive paths for Liam:**

**Path A — Implementation is canonical, spec is stale:** Phase 9 has clearly evolved beyond the v4 spec into a different design (likely informed by v1.5 playtest learnings). If that's the case, the v4 spec needs an addendum (`clanworld_v4_6_bandit_redesign.md` or similar) documenting the new model BEFORE Liam UAT — otherwise UAT can't have an oracle. Per the v4_5 alignment addendum precedent, this is a normal cadence.

**Path B — Spec is canonical, implementation has drifted:** the impl needs significant rework to align with v4 + v4_1 + v4_3. This is a much larger lift — at minimum: rampage loop, loot theft, attack-attempt cap, Case A/B/C outcome model, single-troop limit, lowest-clanId tiebreak, WAITING-at-home defense, global cooldown semantics, tier-based attack powers.

**Recommended next step (before Liam runs UAT):** Liam decides Path A or Path B. If Path A, dispatch a doc-writing subagent to draft `clanworld_v4_6_bandit_phase9_redesign.md` documenting the as-built mechanics as the new authoritative spec, file as ADR, and proceed to UAT against that. If Path B, file the missing-mechanics list as a Phase-9.5 implementation issue.

The cloud reviewers' "5 HIGH findings fixed" assessment is correct **internal to the impl's own design** — the impl is internally consistent. But the impl's design is not the spec's design. The super-swarm did not run a spec-vs-impl alignment pass; it ran a code-quality + implementation-bug pass against the impl as authoritative.

**Cleanly merge-able as-is?** Functionally yes (it's well-tested for what it does), but only if the team explicitly accepts that the v4 spec bandit algorithm is being superseded.

---

## Appendix A — Files inspected

- `/home/claude/code/omnipass-world/review-pr-194-blind/packages/contracts/src/ClanWorld.sol` (3624 lines, full)
- `/home/claude/code/omnipass-world/review-pr-194-blind/packages/contracts/src/IClanWorld.sol` (enums + structs + constants)
- `/home/claude/code/omnipass-world/review-pr-194-blind/packages/contracts/test/Bandit.t.sol` (202 lines)
- `/home/claude/code/omnipass-world/review-pr-194-blind/packages/contracts/test/BanditAttackResolution.t.sol` (693 lines)
- `/home/claude/code/omnipass-world/review-pr-194-blind/packages/contracts/test/BanditSpawn.t.sol` (403 lines)
- `/home/claude/code/omnipass-world/review-pr-194-blind/docs/planning/clanworld_v4_spec.md` §6 (bandit algorithm)
- `/home/claude/code/omnipass-world/review-pr-194-blind/docs/planning/clanworld_v4_1_addendum.md` A3, A4, A9, A11
- `/home/claude/code/omnipass-world/review-pr-194-blind/docs/planning/clanworld_v4_2_state_schema_interface_spec.md` §5, §7.7, §7.10, §7.11, §8.2
- `/home/claude/code/omnipass-world/review-pr-194-blind/docs/planning/clanworld_v4_3_schema_patch.md` E, F, G, H

## Appendix B — What I deliberately did NOT do

- Run `forge test` (per UAT brief: static analysis only)
- Re-litigate prior-reviewer findings already adjudicated in `pr194-r2-synthesis.md`
- File any GitHub issues
- Edit any code in `~/code/clan-world/`
