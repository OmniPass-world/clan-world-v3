I have enough analysis. Writing the final review now.

# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
**NEEDS_FIXES.** The phase ships a mostly-coherent bandit subsystem, but it has a critical architectural gap: bandits **never actually attack in production**. There is code for spawn, camp transition, attack resolution, defender split, blueprint reward, and target-death cleanup — but nothing in `heartbeat()` ever transitions a Camped bandit into the Attacking state, so all of `_resolveAttackingBandits` / `_resolveBanditAttack` / loot / blueprint / cleanup logic is unreachable from a normal heartbeat. The defender registry, simulation parity, seed timing, and arithmetic all look sound otherwise. Recommend a single follow-up fix to wire Camped→Attacking dispatch (or explicit acknowledgment that Wave-N attack dispatch is deferred to a sub-issue) before merging.

## HIGH severity findings

### H1. Bandits never attack — Camped→Attacking dispatch missing in production
`packages/contracts/src/ClanWorld.sol:1551` (`_transitionBanditToAttacking`) is **only called from test harnesses** (`Bandit.t.sol:18`, `BanditAttackResolution.t.sol`, `BanditSpawn.t.sol`). The heartbeat steps 4–6 (`_advanceBanditStates`, `_resolveAttackingBandits`, `_evaluateBanditSpawns`, `ClanWorld.sol:2243-2252`) only handle Spawned→Camped, Resting→Camped, attack-resolution-of-already-Attacking, and new spawns. There is no target-selection + state-transition step for Camped bandits, so on the live engine bandits spawn, transition to Camped, and remain there forever. Consequently `_resolveAttackingBandits`, `_resolveBanditAttack`, `_distributeBanditLootToDefendingClans`, `BlueprintEarned`, `_releaseDefendersForDeadTarget`, `_abortBanditAttacksForDeadTarget`, and the `BanditTargetDied` event are **dead code in the live heartbeat path**. The PR description claims "Pending sub-issues: 9.4 Deterministic attack resolution," but the merged commit `d02061f feat(contracts): deterministic bandit attack resolution (#207)` is part of this PR — so the gap is dispatch wiring, not resolution logic. **Suggested fix:** add a step between `_advanceBanditStates` and `_resolveAttackingBandits` that, for each Camped bandit whose camp window has elapsed, picks a target via `_banditSpawnRegionWeights`-style scoring (or the loot-value heuristic in `_lootValueRaw`) and calls `_transitionBanditToAttacking`. Alternatively, file a follow-up issue and add a top-of-file comment in the heartbeat docstring explicitly marking attack dispatch as deferred so reviewers and the Convex indexer don't assume the system is live.

## MEDIUM severity findings

### M1. `getDerivedClanState` / `getClanFullView` / `quoteLootValueSettled` now return "phantom" simulated state without firing events
The new `_simulateSettleToTick` chain (`ClanWorld.sol:~1014-1505`) faithfully mirrors the storage-mutating settlement, but it **cannot emit events** (it's `view`). That means UIs/indexers that read `getDerivedClanState` or `getClanFullView` will see wall/base/monument upgrades, deposits, and resource yields **before** any matching `WallLevelChanged`, `BaseLevelChanged`, `MonumentLevelChanged`, `ResourcesGathered`, `ResourcesDeposited`, `ClanStarvationChanged`, or `MissionCompleted` event is logged. This was a pre-existing tradeoff for read-side simulation, but it introduces a new operational risk: any indexer (`apps/server/convex`) that reconciles state from events will silently disagree with the contract's view-function output until the next real `settleClan()` runs. **Recommendation:** add a comment block on the simulation cluster noting that derived state may "lead" the event log by up to ~200 ticks, and confirm the Convex indexer is event-driven (not view-poll-driven) to avoid double-counting.

### M2. Heartbeat docstring step numbers don't match implementation
`ClanWorld.sol:~2210-2230` documents the heartbeat as 5 steps ("1. Settle… 2. Execute markets… 3. Eager-settle… 4. Advance bandit timers and resolve closed-tick bandit/world events… 5. Increment tick and publish the next tick seed atomically"), but the body has 8 inline-numbered steps (`Step 1`…`Step 8`), bandit spawn evaluation is its own step, and `_resolveWorldEvents` runs at step 7 not step 4. Stale natspec → reviewer/operator confusion. Trivial fix.

### M3. `_markClanDead` swallows the `reason` string parameter
`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.

### M4. `_pickBanditClansmanVictim` kills clansmen anywhere in the world
`ClanWorld.sol:~1813-1830`: the victim pool is the entire `_clanClansmanIds[clanId]` filtered only by `state != DEAD`. A clansman fishing in REGION_DEEP_SEA can die when a bandit attacks the home base. This may be intentional ("base raid causes generalized casualties"), but it's a notable design choice that the spec extract in `docs/reference/architecture-decisions.md` should confirm. If not intentional, restrict the pool to `cs.currentRegion == clan.baseRegion` (or `cs.state == ClansmanState.WAITING`/`DefendBase`).

### M5. `_applyBanditWallDamage` absorbs a flat 100 HP regardless of wall level
`ClanWorld.sol:~1772-1788`: `wallDamage = remainingDamage < WALL_HP_PER_LEVEL ? remainingDamage : WALL_HP_PER_LEVEL;` followed by `if (wallDamage >= WALL_HP_PER_LEVEL) clan.wallLevel--`. So a level-5 wall absorbs the same 100 damage as a level-1 wall before being chipped, and any single attack that lands ≥100 damage chips one level. Higher-level walls give no extra incoming-damage cushion per attack — they only give *more attacks* before the wall is fully gone. Verify this matches the v4.5 spec; if the intent was "level N wall absorbs N×100", the math is off.

### M6. `_resolveBanditAttack` re-settles target & defenders that step 3 already settled
`ClanWorld.sol:~1716-1717` calls `_settleClan(targetClanId)` and `_eagerSettleActiveDefendersForBase(targetClanId, …)` on every attack resolution, but step 3 (`_eagerSettleForBandits`) already settles candidate-region bases and defenders on the same closedTick. With the typical 4-clansman x 12-clan limit this is bounded, but it's pure wasted gas under the live-attack path. Once H1 is fixed and attacks actually fire, this redundancy will compound. Consider removing the redundant settle inside `_resolveBanditAttack` — the heartbeat-level eager-settle already guarantees the target is current.

### M7. `_releaseDefendersForDeadTarget` linearly scans all clans × all clansmen
`ClanWorld.sol:~547-572`: bounded by 12×4=48, so safe today, but if `MAX_CLANS` ever grows the iteration count goes quadratic in the global clansman population. Cheaper alternative: walk `_defendingClansByRegion[baseRegion]` and only inspect that clan's clansmen. Defer-OK while caps stay tight, but worth a TODO.

## LOW severity findings

### L1. `getActiveBanditView` still returns hard-coded zeros for legacy fields
`ClanWorld.sol:~3473-3510` populates `carryWood/Iron/Wheat/Fish/Gold` and `attackPower` from the new `BanditTroop`, but `attackAttemptsMade`, `maxAttemptsRemaining`, and `tier` are still hard-zeroed. If the UI uses these to gate "danger" indicators, they'll always read 0. Either remove from the view struct or wire them up.

### L2. `_findOldestActiveBandit` definition of "oldest" is "lowest banditId"
`ClanWorld.sol:~1648-1664`: comment says "oldest active bandit" but the implementation returns the lowest live ID. That's actually the *first-spawned* (chronologically oldest), so the behavior matches the comment — but the description "oldest" without saying "by ID" is ambiguous. Single-sentence comment fix.

### L3. Genesis seed for tick 0 is zero
`_tickSeeds[0]` and `_tickSeeds[1]` are both zero at deploy time (no constructor seed). Spawning is gated by `MIN_SPAWN_COOLDOWN_TICKS`, so first spawns can't happen at tick 0–1 anyway, and mission RNG mixes in `(clansmanId, nonce, tick)` which still gives per-mission distinct hashes — but the deterministic-from-zero base is technically attackable by anyone who can pre-image `keccak256(abi.encode(…, 0x0, …))`. Trivial mitigation: seed `_world.currentTickSeed = keccak256(abi.encode(block.prevrandao, address(this)))` in the constructor.

### L4. `getDerivedClansmanState` has an unreachable fallback path
`ClanWorld.sol:~3280-3292`: the simulation always builds `sim.clansmen` from `_clanClansmanIds[cs.clanId]`, and `_findSimulatedClansman` walks that list — so the fallback (using raw mission when not found) cannot trigger. Dead code; remove or annotate.

### L5. `getBanditsInRegion` returns the storage array — caller sees swap-pop ordering
`ClanWorld.sol:~3197-3199`: returns `_banditsByRegion[region]` directly. After deletes via `_deleteBandit`, the order changes due to swap-pop. Any UI that assumed insertion order will see flicker on bandit deaths. Document this or guarantee an order via a copy with a stable sort.

### L6. `_canBanditLeaveSpawned` requires `currentTick >= tickEnteredState + 1` but `_advanceBanditStates` checks `closedTick > bandit.tickEnteredState`
Equivalent for integers but worth aligning the predicate so future readers don't suspect off-by-one.

### L7. The `getBandit` and `getBanditTroop` selectors are duplicates
`ClanWorld.sol:3195-3197` — `getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) { return getBandit(banditId); }`. ABI now has both. Pick one and keep it; the other is just a back-compat fossil with no production users in this branch.

## Cross-cutting observations

1. **Phase delivers ~85% of the stated bandit goal.** State machine ✓, spawn ✓, eager-settle ✓, attack resolution mechanics ✓, defender split ✓, blueprint reward ✓, target-death cleanup ✓. Missing piece: target-selection + Camped→Attacking dispatch in production heartbeat (H1). All six newly added Solidity events for the attack lifecycle (`BanditTargetDied`, `WallDamagedByBandit`, `ClansmanKilledByBandit`, `BlueprintEarned`, `LootDistributed`) are unreachable until that gap closes — Convex indexer wiring for those events is therefore untested in integration.

2. **ABI redesign is clean.** `BanditTroop` and `BanditState` got a clean rewrite (`internalType: "enum BanditState"` reorder, `tickEnteredState`/`region`/`strength`/`carryGold` rename and additions). The interface comment explicitly flags ABI consumers must regenerate. Both the engine and the stub are aligned. New `getBandit` / `getBanditsInRegion` view methods plus the `getBanditTroop` shim look complete.

3. **Settlement simulation is a major design shift.** The 16+ new `_simulate*` functions are a faithful read-only mirror of the storage-mutating settlement (Agent 2 confirmed all math/branches match — only event emission diverges). That's a significant architectural choice: derived state now "leads" committed state. It's the right shape for a UI/agent-facing engine but introduces a long-term maintenance liability — every future change to `_applyUpkeep`, `_gatherWood`, `_doDeposit`, etc. must update *two* functions. Strongly consider adding a `BANDIT_AND_SIMULATION_CHECKLIST.md` or a forge-lint custom check that fails CI when one half changes without the other.

4. **Heartbeat seed publication shifted from "front of tick" to "end of tick" (Agent 4 confirmed safe).** Determinism + replayability are preserved. Off-chain replay tooling should be re-tested against the new ordering — `apps/server` and `apps/orchestrator` should be checked.

5. **The 200-tick clansman-cap test (`test_settle_emergencyEjectAfter200Ticks`) was relaxed to follow the eager-settle invariant.** Sensible accommodation, but it now may pass even if eager-settle accidentally settles every clan every heartbeat. Consider adding an explicit "no cross-region eager-settle" assertion if you want to keep the cap test sharp.

6. **Defender registry invariants are clean** (Agent 3 confirmed). All three registries (`_defendingClansByRegion`, `_defenderCountByRegionClan`, `_clansmanDefendingRegion`) are updated atomically through `_registerDefender` and `_clearDefender`. New paths (`_markClansmanDead`, `_markClanDead`, `_releaseDefendersForDeadTarget`) all hit `_clearDefender` correctly. No leak surface found.

7. **Test coverage for the integration seam is thin.** `BanditAttackResolution.t.sol` exercises attack resolution but only via the `BanditAttackHarness.forceAttackingBandit` test hook. There is no end-to-end test that proves the full `heartbeat → spawn → camp → attack → resolve` cycle works through *normal* heartbeats — exactly because H1 makes such a test impossible today. Once H1 is fixed, add at least one integration test that drives the cycle solely through `heartbeat()` calls.

Want me to `/schedule` an agent in 1 week to verify the H1 follow-up issue (Camped→Attacking dispatch) ships before Submission 1 cutoff?
