Both agents reported back. The first agent missed `_markClansmanDead` (lines 544-559) which DOES set `cs.state = DEAD` for cold/starvation kills — that path is reachable via Phase 4.4 winter (already merged), making the death-path refund interaction a current bug, not Phase-3-future. Writing the final review now.

# Phase Super-Swarm Review — PR #199 (head 33193b7)

## SUMMARY

**Verdict: NEEDS FIXES — do not merge as-is.** Phase 8.1–8.4 ship the correct shape (queue-time vault deduction, mission-nonce-bound reservations, score/rank getters), but four HIGH-severity bugs land with this PR — three of which produce permanent, silent resource loss in well-reachable scenarios (cold death, starvation, BuildWall+UpgradeWall race, level-binding mismatch on cancellation). One MEDIUM-severity bug re-violates the #261 derived-getter contract that #230 was supposed to enforce. The score formula bit-packing and `getRankings` sort are correct and well-designed; the bugs are entirely in the reservation/refund lifecycle and in the choice to read raw committed storage from a documented "settled"-named getter. Cost tables match v4 spec §8 line-for-line. Durations diverge from spec ("single-tick") to 2/2/4 — likely intentional but unannounced.

Cohesive-phase scoring:
1. Cross-cutting bugs across building tiers — **5 findings, 3 HIGH**
2. Spec compliance §8 — costs ✅, durations ❌ (drift)
3. Vault accounting integrity at queue — ✅ basic case, ❌ on death/cancel/race
4. Phase 9 attack-resolution interaction (wall safe-clamp) — pattern survives but exposes resource-burn footgun
5. Phase 8.4 leaderboard correctness vs #261 — ❌ regression
6. Test coverage — happy-path + max-level + isolation across clans is solid; refund/death/cross-type/race coverage is absent

## HIGH severity findings

### H1 — Cold-damage and starvation death paths permanently burn upgrade reservations and orphan the `_pending*UpgradesByClan` counter [`packages/contracts/src/ClanWorld.sol:544-559`, `:565-591`]

This is **the most severe issue in the PR** because winter cold damage and starvation are already shipped (Phase 4.4) and routinely kill clansmen.

`_markClansmanDead` sets `cs.state = DEAD` and `m.active = false` immediately, but does **not** call `_refundWallUpgradeReservation` / `_refundBaseUpgradeReservation` / `_refundMonumentUpgradeReservation`:

```solidity
function _markClansmanDead(Clan storage clan, Clansman storage cs) internal {
    if (cs.state == ClansmanState.DEAD) return;
    cs.state = ClansmanState.DEAD;
    ...
    Mission storage m = _missions[cs.clansmanId];
    if (m.active) {
        if (m.action == ActionType.DefendBase) { _clearDefender(...); }
        m.active = false;   // <-- only DefendBase has cleanup; UpgradeWall/Base/Monument missed
    }
}
```

The death-path refund branch in `_settleMissionForClansman` (lines 349-364) is intended to catch this — but it gates on `if (m.active)`, which has already been set false by `_markClansmanDead`. So when settlement next visits the dead clansman, the refund block is skipped.

Net effect when a cold/starvation kill hits a clansman with an active upgrade mission:
- Resources stay deducted from `vaultWood/vaultIron/vaultWheat/blueprintBalance` forever
- `_pendingWallUpgradesByClan[clanId]` (or base/monument counter) stays elevated forever
- Eventually the elevated counter blocks all future upgrades because `plannedCurrentLevel = clan.wallLevel + pendingUpgrades >= MAX_LEVEL` rejects with `ERR_INVALID_ACTION`

**Same issue exists in `_markClanDead` (line 565-591)** — it loops `_clansmen[csIds[i]].state = DEAD; m.active = false;` without calling refunds. The vault is wiped to 0 anyway in this path, so the "lost resources" claim is moot for the dying clan, but the clansmen's reservation structs and the pending counter are never cleaned up. (Probably no functional consequence since the clan is dead, but inconsistent.)

**Fix:** in `_markClansmanDead` (and the per-clansman loop in `_markClanDead`), check `m.action` against UpgradeWall/Base/Monument and call the refund helper before setting `m.active = false`. Or better: extract a `_invalidateActiveMission(cs, m)` helper and route every death/invalidation path through it so the refund-on-death contract has a single source of truth.

### H2 — `BuildWall` and `UpgradeWall` are dual code paths for the same cost table; running both in flight burns resources

`BuildWall` (existing, duration 1, no reservation, deducts at settle via `_tryBuildWall` at lines 901-933) and `UpgradeWall` (new, duration 2, reserves at queue) both increment `wallLevel` 0→5 against the **same cost table**. `_validateAction` lets BOTH through when at homebase, and `_validateUpgradeWallOrder` only consults `_pendingWallUpgradesByClan` — which `_tryBuildWall` does not increment.

Concrete race at `wallLevel = 4`:
1. Clansman A queues `UpgradeWall`. Validator: planned=4, pending=0, OK. Reserve 50w+15i (the L4→5 cost). pending=1.
2. Clansman B queues `BuildWall`. Validator: only homebase check — passes. **No reservation, no pending bump, no vault deduct.**
3. B settles first (duration 1 vs UpgradeWall's 2). `_tryBuildWall`: `nextLevel = 4+1 = 5`, costs 50w+15i, vault has it (UpgradeWall's reservation already accounted), deduct 50w+15i, set wallLevel=5.
4. A settles. `_settleWallUpgrade` finds reservation valid → calls `_clearWallUpgradeReservation` (no refund) → sees `wallLevel >= WALL_MAX_LEVEL` → returns false. **Resources A reserved (50w+15i) are silently destroyed.**

Same race exists for any wallLevel — A reserves at level N, B builds to N+1 first, A's settle hits the cap-clear path.

**Fix:** either (a) remove `BuildWall` and `_tryBuildWall` entirely (recommended — UpgradeWall covers L0→L5), or (b) route `BuildWall` through the same reservation path so `_pendingWallUpgradesByClan` covers both.

### H3 — Reservations bind cost to *planned* level, but settlement applies *current+1* — cancelling an earlier reservation leaves later ones overpaying (esp. blueprint at monument L7-10) [`ClanWorld.sol:1771-1782`, `:1907-1922`]

`_reserveWallUpgrade` reads `clan.wallLevel + _pendingWallUpgradesByClan[clanId]` and prices the *next-planned* transition. `_settleWallUpgrade` does `clan.wallLevel = old + 1` regardless of what cost was paid. There is no link from a reservation back to a specific level.

Concrete monument scenario:
1. `monumentLevel = 5`, no pending. Clansman A queues UpgradeMonument. Reserve at planned-level 5: 150w+20i+80wh, **no blueprint**.
2. Clansman B queues UpgradeMonument. Reserve at planned-level 6: 200w+25i+100wh+**1e18 blueprint** (`_monumentUpgradeCost(6)`).
3. Owner re-tasks A to ChopWood. `_processOrder` line 1258 calls `_refundMonumentUpgradeReservation(A)` — A's reservation refunded, pending → 1.
4. B settles. Reservation valid, clears, monumentLevel: `5 + 1 = 6`. Returns true. **B paid the L6→7 cost (200w+25i+100wh+1e18 blueprint) for a L5→6 transition. The blueprint is burned for nothing.**

Same shape applies to wall and base; blueprint at monument L7-10 makes monument the worst case.

**Fix options:** (a) bind each reservation to its target level and refuse settlement if `clan.monumentLevel + 1 != reservedTargetLevel` (refund on mismatch), or (b) compute and store a `levelDelta` on the reservation and re-quote at settle, refunding the difference. (a) is cleaner.

### H4 — `getClanScore`/`quoteLootValueSettled` read raw committed storage, regressing the #261/#230 derived-getter invariant [`ClanWorld.sol:2274-2277`, `:2346-2368`]

```solidity
function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
    return _lootValueRaw(_clans[clanId]);   // identical to quoteLootValueRaw — raw storage
}
function _getClanScore(uint32 clanId) internal view returns (...) {
    Clan memory clan = _clans[clanId];      // raw committed read — no settlement simulation
    ...
    uint256 lootValue = _lootValueRaw(clan);
    ...
}
```

The PR's own NatSpec on `getClanScore` admits this: *"...does not simulate pending lazy settlement until derived settlement getters are fixed in #230."* But #230 was supposed to be the fix for #261, which #261 in turn closed by simulating settlement in derived getters. Phase 8.4 was explicitly tracked as "uses derived getters per #261 fix" in the review brief. This getter does the opposite.

The downstream consequence is twofold:
- An in-flight upgrade reservation **deducts** vault, depressing the loot component until the level field is bumped at settle. Score temporarily understates while the player is mid-investment — a bad incentive at season-end.
- An unsettled clan's vault upkeep / starvation / cold-damage deltas haven't been applied yet, so a stale `lastSettledTick` makes the clan look richer than it actually is.

**Fix:** materialize a settled `Clan` view (via `getDerivedClanState` or an internal helper that does the same simulation as `_settleClan` without persistence — same pattern #230 uses) and pass that into `_lootValueRaw`. Same fix applies to `quoteLootValueSettled` itself.

## MEDIUM severity findings

### M1 — `_settle*Upgrade` clears reservation BEFORE the MAX_LEVEL check, with no refund on the cleared path [`ClanWorld.sol:815-834`, `:836-855`, `:857-880`]

```solidity
function _settleWallUpgrade(...) internal returns (bool) {
    ...nonce check...
    _clearWallUpgradeReservation(clansmanId);          // <-- BURN (no refund)
    if (clan.wallLevel >= WALL_MAX_LEVEL) return false; // <-- CAP CHECK AFTER
    ...
}
```

`_clearWallUpgradeReservation` only deletes the reservation struct and decrements the pending counter — it does NOT credit vault. Currently this resource-burn path is unreachable in normal play (validator's pending counter prevents reserving past max with ≤ 4 clansmen per clan), but **Phase 9 attack-resolution will introduce wall-destruction writes**, creating a real race: queue UpgradeWall at L4 → bandit destroys wall to L3 between queue and settle → wall is back at L4 (then settle overwrites with `old+1=4` which is fine actually).

The actual reachable bad case is via H1 (BuildWall steals the slot) and via H2 (early reservation cancellation desyncs pending count vs actual level). **Even setting H1/H2 aside, the defensive coding fix is trivial:** swap order so the cap check runs first, or change `_clearReservation` semantics so the failure path refunds instead of burning. I'd recommend ordering: nonce-check → cap-check → clear-reservation → emit (so any bail returns resources via `_refund*Reservation`).

### M2 — `_doBuilding` failure path is silent: empty `if (!success)` block, no failure event, indexers can't distinguish success from cap-rejection [`ClanWorld.sol:750-779`]

```solidity
if (!success) {
    // Resources missing — worker transitions to WAITING
}
_completeMission(cs, m);
```

When `_settleWallUpgrade` returns false (max-level cap hit, or reservation nonce mismatch from re-tasking), the only on-chain trace is a generic `MissionCompleted`. No `WallUpgradeFailed` or equivalent. Indexers correlating "did this upgrade actually apply?" must diff `wallLevel` before/after — feasible but fragile, and frontend agents will think the upgrade succeeded.

**Fix:** emit a `BuildingActionFailed(clanId, clansmanId, action, reason)` (or repurpose `MarketActionFailed`'s pattern) on the false path.

### M3 — Cross-type reassignment (e.g. UpgradeWall → UpgradeBase) validates against pre-refund vault, can spuriously reject [`ClanWorld.sol:1233`, `:1251-1268`, `:1806-1828`]

`_validateAction` runs at line 1233, BEFORE `_refundWallUpgradeReservation` at line 1252. So switching a clansman from a queued UpgradeWall to a fresh UpgradeBase can return `ERR_MISSING_RESOURCES` even when, after refund, the vault would be sufficient. Workaround is "submit Wait first, then base" — but it's a bad UX seam for Elder agents.

**Fix:** in `_validateUpgradeBaseOrder`, also synthesize the vault refund of any active wall/monument reservation for the same clansman. Or refactor: refund FIRST in `_processOrder` at the point of "we're definitely re-tasking", then validate the new action.

### M4 — Spec drift on action durations (spec §8.1: "single-tick"; impl: 2/2/4) [`IClanWorld.sol`, `ClanWorld.sol:2117-2138`]

`clanworld_v4_spec.md` §8.1: *"build_wall, upgrade_base, upgrade_monument consume one full action tick at homebase."* Implementation uses:
- `UPGRADE_WALL_DURATION_TICKS = 2`
- UpgradeBase reuses `UPGRADE_WALL_DURATION_TICKS = 2`
- `UPGRADE_MONUMENT_DURATION_TICKS = 4`
- BuildWall = 1 (matches spec)

Likely a deliberate hackathon balance choice (heavier buildings should take longer) but not announced in the PR description and not reflected in the spec. The codex-5-4 review noted "materially changes the building timing model" before the session died.

**Fix:** amend `clanworld_v4_5_alignment_addendum.md` (the canonical updates doc) with the new duration table, OR revert to single-tick. Don't merge silent spec drift.

### M5 — `pendingUpgrades -= 1` underflow risk in validators is gated by a tight invariant with no defensive guard [`ClanWorld.sol:1757`, `:1814`, `:1882`]

```solidity
WallUpgradeReservation storage existing = _wallUpgradeReservations[clansmanId];
if (existing.active && existing.clanId == clan.clanId) {
    pendingUpgrades -= 1;   // panic-revert if counter was 0
```

The invariant "every active reservation contributes exactly +1 to the corresponding pending counter" must hold. Today it does — `_reserveX` increments and `_clearX` decrements (with a defensive `> 0` guard at the clear site). But H1/H2/H4 above all involve scenarios where the counter could desync. A hostile sequence under those bugs could reach an active reservation with pending=0 and cause a `panic(0x11)` revert on `submitClanOrders`, locking the clansman out until the reservation is cleared via mission settlement.

**Fix:** mirror the defensive guard already present in `_clearWallUpgradeReservation` (line 1799 `if (_pendingWallUpgradesByClan[clanId] > 0)`) — apply the same guard at validator subtraction sites. Cost: 3 lines.

### M6 — Out-of-scope wood gathering changes shipped silently in this PR

The diff modifies wood gathering behavior in ways NOT mentioned in the PR description:
- `WOOD_BASE_YIELD`: 2e18 → 1e18 (now `WOOD_YIELD_PER_TICK`)
- `WOOD_CRIT_BPS`: 2000 (20%) → 1000 (10%); crit bonus changes from `+1e18` to `*= 2`
- Wood yield model: continuous-per-tick → single-shot at settle (`yield * actionDuration`)
- `WOOD_CAP`: 15e18 → 10e18 (renamed `CLANSMAN_CARRY_CAP`, aliased back as `WOOD_CAP`)

Old E[yield] over 4-tick mission: `2e18*4 + 0.2*1e18*4 = 8.8e18` (capped 15). New E[yield]: `0.9*4e18 + 0.1*8e18 = 4.4e18` (capped 10). **Half the wood economy.** This is a Phase 5 game-balance change in a Phase 8 PR.

Two concerns: (a) scope creep — this should have been a separate PR per gitflow conventions, and (b) the `WOOD_CAP = CLANSMAN_CARRY_CAP` aliasing is misleading, since iron/fish/wheat caps remain distinct constants. Either the rename was sloppy or the intent is to converge all carry caps to one number eventually.

### M7 — `ResourcesDeposited` event ABI break (uint64 atTick → uint32 tick, field renames) [`IClanWorld.sol:536-544`]

```solidity
// before:
event ResourcesDeposited(uint32 indexed clanId, uint32 indexed clansmanId,
    uint256 wood, uint256 iron, uint256 wheat, uint256 fish, uint64 atTick);
// after:
event ResourcesDeposited(uint32 indexed clanId, uint32 indexed clansmanId,
    uint256 woodDelta, uint256 ironDelta, uint256 wheatDelta, uint256 fishDelta, uint32 tick);
```

Indexer subscriptions must be regenerated. Tick narrowing from uint64 → uint32 is safe for hackathon season horizons but is a forward-incompat downgrade. Not flagged in the PR description.

## LOW severity findings

### L1 — `recordMonumentReachTick` is the sole internal function in PR199 missing the `_` prefix [`ClanWorld.sol:882`]

```solidity
function recordMonumentReachTick(uint32 clanId, uint8 newLevel, uint64 tick) internal {
```

Every other internal/private function in the contract follows the `_<name>` convention (`_completeMission`, `_settleWallUpgrade`, `_reserveBaseUpgrade`, `_doBuilding`, etc.). Trivial style fix: rename to `_recordMonumentReachTick`.

### L2 — `DepositResources` error code regression: ERR_NOT_AT_HOMEBASE → ERR_INVALID_REGION, inconsistent with sibling actions [`ClanWorld.sol:1666-1675`]

The diff changes only `DepositResources` to return `ERR_INVALID_REGION` while `BuildWall`/`UpgradeWall`/`UpgradeBase`/`UpgradeMonument` (which have the same homebase requirement) continue to return `ERR_NOT_AT_HOMEBASE`. Either both should use ERR_NOT_AT_HOMEBASE (the more specific code) or both ERR_INVALID_REGION. The split is a code smell. Test was updated to match — but the inconsistency remains.

### L3 — Dual event emission on every upgrade (legacy `*LevelChanged` + new `*Upgraded`) [`ClanWorld.sol:828-829`, `:849-850`, `:871-872`]

```solidity
emit WallLevelChanged(clanId, old, clan.wallLevel, tick);
emit WallUpgraded(clanId, clan.wallLevel, uint32(tick));
```

Indexers subscribed to either signal get correct data, but those subscribed to both see duplicates. Acceptable as a transition mechanism if the legacy event is documented for deprecation; otherwise pick one.

### L4 — `_allClanIds` is append-only; dead clans persist in scan, wasting gas on `getRankings` [`ClanWorld.sol:69`, `:2310-2312`]

Bounded by 12-clan mint cap and `MAX_CLAN_SCAN_FOR_RANKING=24`, so not exploitable. Worth noting for Phase 11 if the cap rises.

### L5 — `ClanWorldStub.getActionDuration` returns 0 for ALL actions [`ClanWorldStub.sol:325-327`]

Stub consumers cannot rely on duration semantics. Documented limitation but a hidden footgun; consider returning the real values via the same constants.

### L6 — Cost-table tests sample tier transitions, not every transition

`test_getWallUpgradeCost_matchesSpecTable` asserts levels 0, 2, 4 only (skips 1, 3). Same pattern in base/monument tests. An off-by-one in the unhit row would slip past CI. Hackathon scope justifies the brevity, but a single per-level loop assertion would close the gap in <20 lines.

### L7 — No test covers reservation refund on death, on cross-type retask, or on the BuildWall/UpgradeWall race

Phase 8 tests verify happy-path queue, max-level rejection, isolation across clans, and cost-table sampling. They do NOT cover: (a) cold-damage death with active upgrade (would catch H1), (b) UpgradeWall→UpgradeBase swap (would surface M3), (c) BuildWall + UpgradeWall same-clan racing (would catch H2). Two-clansman parallel upgrade interaction is also untested. Hackathon discipline tolerates minimal tests, but H1/H2 are real bugs that a 30-line happy-path test would catch.

## Cross-cutting observations

- **The reservation lifecycle is the structural weak point.** Every HIGH/MEDIUM finding except H4 and M4 traces back to the same shape: there are at least four state-mutation paths that touch a reservation (`_reserve`, `_refund`, `_clear`-via-settle-success, `_clear`-via-settle-cap-fail, plus death-path missed handlers), and they don't share a single invalidation routine. The codex-5-5 review reached the same conclusion. Refactor recommendation: extract a `_invalidateUpgradeReservation(clansmanId, refundVault: bool)` helper and route every cancellation/death/cap-fail path through it. This collapses H1, H2, M1 into a single, testable seam.

- **Score formula is solid; the basis is wrong.** Bit packing math checks out (8 + 64 + 176 + 8 = 256, no overlap), insertion sort is stable for ties, clanId tiebreak is deterministic, and `MAX_CLAN_SCAN_FOR_RANKING=24` is well-bounded. The only correctness defect (H4) is in the *inputs* to the score, not the formula. Fix the input source and 8.4 ships clean.

- **Phase 9 attack-resolution interaction is fine architecturally** — the `clan.wallLevel + pending` validation pattern correctly clamps planned upgrades against current+pending state. The pending counter never goes negative under in-flight attacks; the worst Phase 9 introduces is an "overpaid for level" outcome (H3 generalized), which is structurally the same bug. Fix H3 properly (level-bind reservations) and the wall-clamp pattern survives Phase 9 unchanged.

- **Spec compliance on §8 cost tables is line-perfect** — wall L1=20w, L2=35w, L3=30w+5i, L4=40w+10i, L5=50w+15i; base L2=40w+20wh through L5=100w+15i+50wh; monument L1=30w+20wh through L6=150w+20i+80wh, L7-10=200w+25i+100wh+1e18 blueprint. No drift here. Drift is on durations only (M4) and on ResourcesDeposited event shape (M7).

- **codex-5-4 produced no signal** — the session was killed mid-investigation; treat its file as empty. codex-5-5 is the only complete prior automated review and identified H2/H3/H4 (originally numbered H1/H2/H3 in their report) plus several MEDIUMs that overlap with M3/M5 here. This review adds H1 (cold/starvation refund miss — current bug, not theoretical), M1 (resource-burn ordering at settle), M2 (silent failure path), M6 (wood scope creep), M7 (event ABI break), L1 (`_` prefix), L2 (error-code regression), L7 (test gaps).

- **Recommendation:** block merge on H1 + H2 + H3 + H4 + M4. The remaining M's and L's can either be folded into the same fix-round or queued as cleanup PRs. H1 is the highest priority — it's actively losing resources every winter.
