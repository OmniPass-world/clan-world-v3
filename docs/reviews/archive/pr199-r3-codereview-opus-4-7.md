All 5 agents complete. Confirmed staleness in ABI directly (WallUpgraded/BaseUpgraded/MonumentUpgraded missing; ResourcesDeposited still has old `atTick uint64` shape). Writing final synthesis.

# Phase Super-Swarm Review (R3) — PR #199 (head 36420da)

## SUMMARY

**Verdict: NEEDS_FIXES.** The reservation system, deposit-duration change, action enum extension, and per-sub-issue test coverage are solid. R2 fix-round correctly refunds upgrade reservations on dead clansmen and on order replacement. However, 3 cross-cutting HIGH issues remain: (1) the new ranking simulation gives a queued-but-unsettled monument upgrade a *maximal* `timeComponent` because sim advances `monumentLevel` without propagating an in-memory reach-tick (R2 fixed half of "rank getters stale"); (2) the committed ABI artifact is stale — new events absent, `ResourcesDeposited` topic-0 changed under it; (3) `_simulateSettleToTick` has no per-call tick cap (real settlement caps at 200), enabling RPC-timeout DoS via passive clans. **Hold the merge** until H1 + H2 are addressed; H3 + the cross-type swap MED can ship in a follow-up.

## HIGH severity findings

### H1. Simulated monument upgrade gets `type(uint64).max` time-component → ranking spec violation
**File:** `packages/contracts/src/ClanWorld.sol:1262-1286, 2807, 2818`

`_simulateSettleMonumentUpgrade` increments `sim.clan.monumentLevel += 1` in memory but never writes `_monumentLevelReachedAt[clanId][newLevel]` (cannot — view). `_getClanScoreFromClan` then reads:

```solidity
monumentLevel = clan.monumentLevel;                          // simulated, e.g. 1
monumentReachedAtTick = _monumentLevelReachedAt[clanId][1];  // STORAGE, == 0
timeComponent = uint256(type(uint64).max) - 0;               // == type(uint64).max
```

The score is `(monumentLevel << 248) | (timeComponent << 184) | ...`. With `monumentReachedAtTick == 0`, the time-component is the *largest* possible value — a clan whose upgrade is sitting in simulation outranks every clan whose upgrade was *actually* committed (whose recorded tick is ≥ 1). This violates the v4 §6.9 spec ("earlier first-reached tick wins") and is gameable: any UI / indexer that calls `getRankings()` between submit and the next heartbeat sees wrong rankings; a clan can intentionally re-queue to flash to #1 between heartbeats. The R2 fix-round was supposed to cure "rank getters stale" but only propagated the simulated level, not the simulated reach-tick.

**Fix:** thread the tick at which sim advances the monument level into a `SettlementSimulation` field (e.g. `uint64[MONUMENT_MAX_LEVEL+1] simMonumentReachedAt`), populate it inside `_simulateSettleMonumentUpgrade` from the current sim tick, and have `_getClanScoreFromClan` accept the sim and prefer the in-memory value when storage is 0. Also recommend separating the "never recorded" sentinel from "reached at tick 0" by storing `tick + 1` (architecturally fragile today, just unreachable).

Add a regression test (none exists): queue + advance to settle tick *minus one*, call `getRankings`, assert the queued clan does *not* outrank a clan that has the same level already persisted.

### H2. ABI artifact `packages/contracts/abi/IClanWorld.json` is stale — new events missing, `ResourcesDeposited` topic-0 changed
**File:** `packages/contracts/abi/IClanWorld.json` (full file)

Verified directly:
- `WallUpgraded`, `BaseUpgraded`, `MonumentUpgraded` — declared in `IClanWorld.sol:548-550` and emitted in `ClanWorld.sol:822/869/906` — are **completely absent** from the committed ABI.
- `ResourcesDeposited` still encodes the *old* field names + types: `wood`, `iron`, `wheat`, `fish`, `atTick uint64` (lines 3334-3349 + the `atTick uint64` form at line 2590, 3319). Source now uses `woodDelta`...`fishDelta` plus `tick uint32`.

Because event topic-0 = `keccak(canonicalSig)`, the trailing `uint64 → uint32` change rotates the topic hash. Any downstream indexer / mini-app subscribed via the committed ABI silently misses every deposit event after deploy AND has no way to observe upgrade settlements.

**Fix:** regenerate via `forge build` and copy `out/IClanWorld.sol/IClanWorld.json` over the artifact, then add a CI/`pretest` guard that diffs regenerated against committed and fails on drift (this exact failure mode will recur every phase otherwise). Document the topic-0 break for `ResourcesDeposited` in CHANGELOG and coordinate with whichever indexer/mini-app consumes it.

### H3. `_simulateSettleToTick` has no tick-loop cap — RPC-timeout DoS for passive clans
**File:** `packages/contracts/src/ClanWorld.sol:944` (sim) vs `:414-417` (real)

Real `_settleClan` caps work at `maxSettleTicks = 200` per call. Sim has no equivalent. `getRankings()` invokes `_simulateSettleToTick` for every clan, and per-clan it iterates `(currentTick - lastSettledTick) × clansmen.length × per-tick gather/upkeep work`. A clan whose 4 clansmen are all WAITING/IDLE never gets `lastSettledTick` advanced by heartbeat (heartbeat only touches clans with mission settlements at the closing tick), so `lastSettledTick` can drift arbitrarily far behind. View-function so no on-chain DoS, but indexers / season-end UIs hitting non-archive RPCs will time out exactly when they're needed most.

**Fix:** mirror the real cap inside `_simulateSettleToTick`: `if (toTick > fromTick + 200) toTick = fromTick + 200;` (acceptable because heartbeat-not-running-for-200-ticks is a separate operational alarm), or ensure heartbeat advances every clan's `lastSettledTick` even for no-mission ticks (bigger refactor — defer). Document the gas/RPC-time bound in NatSpec on `getRankings` either way.

## MEDIUM severity findings

### M1. Cross-type upgrade-mission switch falsely reverts when resources are tight
**File:** `packages/contracts/src/ClanWorld.sol:1637` (validation) vs `:1656-1664` (refund)

`_validateAction` runs *before* the refund of the previous building's reservation. `_validateUpgradeBaseOrder` (`:2221`) only releases the **same-type** reservation (`_baseUpgradeReservations[clansmanId]`) when computing `availableWood`. So if clansman C currently has `UpgradeWall` queued (50W reserved) and submits `UpgradeBase` (60W needed), validation sees `_spendableAfterReleasing(100, 50, 0) = 50 < 60 → ERR_MISSING_RESOURCES`. The order is rejected even though refund-then-reserve would succeed.

Symmetric for wall↔monument and base↔monument swaps. No test covers this — `test_upgradeWall_repricesLowerLevelAfterEarlierReservationCancelled` switches to `Wait`, not to a different upgrade type.

**Fix:** in `_validateUpgrade*Order`, also release any *sibling-type* reservation held by the same clansman by adding its costs to `releasedWood/Iron/Wheat/Blueprint`. Add tests `test_orderReplacement_switchUpgradeType_releasesPreviousReservationForValidation` for all 6 swap directions.

### M2. Sim death-branch does not credit refund into `sim.clan.vault*` (latent until Phase 9 adds death paths)
**File:** `packages/contracts/src/ClanWorld.sol:996-999`

Real `_invalidateActiveMission` refunds upgrade reservations back to clan vault. Sim death branch only sets `m.active = false` — it does not mirror the refund into `sim.clan`. Today no production code path decrements `livingClansmen` or sets `ClansmanState.DEAD`, so dormant — but Phase 9 attack/combat will introduce death and rankings will under-report dead clans' loot until the next heartbeat. Fix proactively while this code is hot.

**Fix:** in the sim DEAD branch, peek at `_*UpgradeReservations[cs.clansmanId]` for the matching action and add the held costs back to `sim.clan.vault*` / `sim.clan.blueprintBalance` (no storage write — sim is view).

### M3. Two redundant events per upgrade (`WallLevelChanged` + `WallUpgraded`, etc.)
**File:** `packages/contracts/src/ClanWorld.sol:822/825, 869/872, 902/906`

Each upgrade settlement emits both `WallLevelChanged(clanId, old, new, uint64 atTick)` and `WallUpgraded(clanId, new, uint32 settledAtTick)`. Two log entries, two indexer-decode steps, double the gas (`LOG3` × 2 ≈ 1.5k gas extra per upgrade). `WallLevelChanged` is strictly more informative (carries `oldLevel`).

**Fix:** drop the `*Upgraded` events; keep only `*LevelChanged`. Or — if the indexer team specifically requested a "level-up signal" distinct from "level-changed", document the rationale in NatSpec.

### M4. Insertion-sort + simulation scaling at MAX_CLAN_SCAN_FOR_RANKING=24 untested
**File:** `packages/contracts/test/RankGetters.t.sol`

All ranking tests use 2-3 clans. No test exercises the sort or sim at 12 clans (current production cap), let alone the 24-clan scan ceiling. `getRankings` complexity is `O(N²)` sort + `O(N × clansmen × tickSpan)` sim. Add at least one test that mints 12 clans with varied (level, reachTick, loot, wallLevel) and asserts ordering invariants, plus a gas snapshot.

### M5. Wheat plot regrow tick math diverges between real and sim (theoretical)
**File:** real `:432-433, :727`; sim `:1171`

Real writes `plot.regrowUntilTick = tick + WHEAT_PLOT_REGROW_TICKS` (raw add, would revert on overflow under 0.8 — unreachable in 8000-year season). Sim writes `_addTicksClamped(...)` (saturates). Observable difference is unreachable under realistic ticks but breaks the parity-discipline contract — when divergence becomes reachable later it will be a silent bug. Align: pick one, prefer dropping the clamp from sim.

## LOW severity findings

- **L1.** `_allClanIds.length < 12` (`:1443`) means actual mint cap is 11; the comment on `getRankings` (`:2742`) says 12. Off-by-one cosmetic. Define `MAX_LIVING_CLANS = 12` and use `<= MAX_LIVING_CLANS` to remove the magic numbers; have `MAX_CLAN_SCAN_FOR_RANKING = 2 * MAX_LIVING_CLANS`.
- **L2.** `CLANSMAN_CARRY_CAP = 10e18` (`IClanWorld.sol:43`) is dead — not referenced anywhere; conflicts with `WOOD_CAP = 15e18`. Delete or wire up.
- **L3.** `WOOD_YIELD_PER_TICK = 1e18` (`IClanWorld.sol:50`) only used as the RHS of `WOOD_CRIT_BONUS` definition. Either ground it in a real per-tick model (which the wood-economy revert removed) or inline `1e18` and delete.
- **L4.** `ClanWorldStub.sol` re-implements all three upgrade cost tables. Will silently drift from the real cost helpers. Extract `_wallUpgradeCost` / `_baseUpgradeCost` / `_monumentUpgradeCost` to a shared pure library both implementations import.
- **L5.** `BuildWall` enum slot still in `ActionType` (`IClanWorld.sol:138`). Intentional for wire-format stability but make it explicit in NatSpec and tell the indexer team it will never appear in `MissionAssigned` events post-Phase-8.
- **L6.** `Gathering.t.sol::test_chopWoodCritDistributionAcrossSeeds` window `[10, 30]` for 100 trials at p=0.2 is ±2.5σ → ~1-in-80 flake. Widen to `[5, 35]` or raise N to 500.
- **L7.** Missing tests: empty-world `getRankings` (zero clans, all-defeated), single-clan `getRankings` (degenerate insertion sort), `getClanScore(CLAN_ID_NULL)` behavior, refund-on-replace-with-non-Wait (current test uses `Wait` only), DEAD-during-TRAVELING / DEAD-during-ACTING with mid-flight reservation, vault-drain race at settle (vault < woodDebit).
- **L8.** `monumentReachedAtTick == 0` sentinel collides with "literally reached at tick 0" — practically unreachable under current heartbeat semantics (mintClan→submit→settle path makes tick 0 settlements impossible) but architecturally fragile. Encode as `tick + 1` or use an explicit `bool recorded` field.

## Cross-cutting observations

1. **Simulation is a duplicate state machine.** ~330 lines of view-only re-implementation (`_simulate*`) parallel the real `_settle*`/`_resolve*`/`_gather*` paths. The reach-tick bug (H1) is exactly the kind of bug this duplication invites — it's load-bearing for ranking but missing one specific side-effect from the real path. Recommend a follow-up issue to either (a) extract shared logic into pure functions both real and sim invoke, or (b) add an invariant test that fuzzes a handful of operations and asserts `simulate(state) == real_settle_then_read(state)` for every observable. This will pay back across phases.

2. **The reservation refund machinery is solid.** Three independent reviewers traced `_invalidateActiveMission` for both heartbeat and lazy `_settleClan` paths and for both TRAVELING and ACTING DEAD; refunds wire correctly through `_clear*` → `_pendingX` and `_reservedXByClan` decrements. Only the cross-type swap edge (M1) is broken, and only in the *validation* layer, not the actual reservation accounting.

3. **ABI artifact discipline is missing across the project.** H2 is structural — there's no CI guard preventing the source ABI from drifting from the committed JSON. This will recur every phase. One pre-commit / pre-push step (`forge build && diff out/.../IClanWorld.json packages/contracts/abi/IClanWorld.json`) eliminates the class.

4. **The R2 fix-round was effective on its stated targets.** Death-refund, BuildWall→UpgradeWall split, level-binding, reservation timing, and duration spec all check out. But the R1 super-swarm "rank getters stale" finding got a partial fix that introduced H1 — same shape as the Phase 10 R2 `_resetColdDamageForAllClans` regression you flagged. Worth filing a memory entry: "R1 fixes that touch a duplicate state machine must propagate ALL relevant side-effects, not just the obviously-named field."

5. **Gas/cost concerns scale with clan count.** Both `_simulateSettleToTick` (per-clan, per-tick, per-clansman) and the insertion sort in `getRankings` (N²) are bounded by today's tiny clan cap, but headroom shrinks fast if Phase 11 raises it. The simulation cap (H3) and sort scaling test (M4) are the immediate mitigations; longer-term the sim needs a memoization or differential structure.

Want me to /schedule a 2-week follow-up agent to verify the ABI-drift CI guard from H2 actually got installed?
EXIT_4_7=0
