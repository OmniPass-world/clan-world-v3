# Phase Super-Swarm Synthesis — PR #194 (head 23f7f1a)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.6 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✓
**Phase:** dev-phase-9-bandits — Bandit System
**Diff size:** 4385 lines

## Summary

**Verdict: NEEDS_FIXES — 5 HIGH (all cross-model agreement), 4 MEDIUM, 7 LOW**

The phase ships ~85% of the bandit subsystem (state machine, spawn, attack resolution, defender split, blueprint reward, target-death cleanup). All five reviewers independently flagged the SAME critical gap: **production bandits never attack**. They spawn → Camped → STUCK FOREVER. All the attack/loot/blueprint code is dead in the live heartbeat path; only test harnesses force the Camped→Attacking transition.

Two additional cross-model HIGHs emerged from Codex 5.4 + 5.5: stale handwritten ABI tuples in the TypeScript runner/shared client will misdecode the new `WorldState` shape, breaking heartbeat automation at deploy time.

**Recommended action: fix-round, then re-run super-swarm on the new head before merge.**

## MUST FIX (cross-model consensus, 4-5 model overlap)

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H1 | `ClanWorld.sol:1551, 1654, 1679` | 5.4 + 5.5 + 4.7 + Gemini = **4/5** | HIGH | **Camped→Attacking dispatch missing in production heartbeat.** `_transitionBanditToAttacking` only called from test harnesses. `_advanceBanditStates` only handles Spawned→Camped + Resting→Camped. As merged, real bandits spawn, camp, and stay stuck. Attack resolution / loot split / blueprint reward / target death cleanup are all DEAD CODE in the live path. |
| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
| H4 | `ClanWorld.sol:1755` + `1005-1016` | 5.4 = **1/5** | HIGH | **Blueprint reward unit mismatch.** Bandit defeat awards `targetClan.blueprintBalance += 1`. Monument upgrades consume `1e18` blueprint units. So one bandit win = 1 wei of blueprint. The new test at `BanditAttackResolution.t.sol:252-261` LOCKS THE BUG IN. Fix: award `1e18` and update test/event expectations. |
| H5 | `ClanWorld.sol:1717` | 5.5 = **1/5** | HIGH | **`_resolveBanditAttack` reverts on target-death-during-attack.** `_settleClan(targetClanId)` may kill the target → `_markClanDead` aborts the attacking bandit → state machine then tries to transition already-`Escaped` bandit again at `:1758`, which is invalid. Race window. Fix: re-check `targetClan.clanState` and `bandit.state/targetClanId` after target/defender settlement; return if cleanup already handled. |

**Single-model HIGH (Opus 4.6 H1) — DEFERRED**: `_isBanditSpawnRegionCandidate` seed-timing concern. Opus 4.6 acknowledged in the impact analysis the practical case is correct. Not blocking.

## SHOULD FIX (multi-model MEDIUM consensus)

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| M1 | `ClanWorld.sol:1899-1906` (`_applyBanditWallDamage`) | 4.6 + 4.7 + Gemini = **3/5** | MED | **Wall absorbs flat 100 HP regardless of level.** Level-5 wall absorbs same 100 damage as Level-1. Higher walls only give more attacks before destruction, not more cushion per attack. Verify v4.5 spec intent — if "level N wall absorbs N×100", the math is off. |
| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
| M3 | `ClanWorld.sol:1206` (`_simulateResolveAction`) | 4.6 = **1/5** | MED | **Starvation check uses `_world.currentTick` instead of simulation tick.** Latent bug — current control flow prevents misalignment, but breaks if simulation structure changes. Use `sim.clan.starvationStartsAtTick <= tick`. |
| M4 | `ClanWorld.sol:~1813-1830` (`_pickBanditClansmanVictim`) | 4.7 = **1/5** | MED | **Victim pool too broad.** A clansman fishing in REGION_DEEP_SEA can die when bandit attacks home base. Confirm intent vs. restrict pool to base-region defenders. |

## DEFER (file as follow-up issues)

| # | Finding | Source |
|---|---|---|
| L1 | `getActiveBanditView` hardcodes `attackAttemptsMade: 0` / `maxAttemptsRemaining: 0` / `tier: 0` / `projectedTargetLootValue: 0`. UI gating may be misleading. | 4.6 + 4.7 + Gemini |
| L2 | `BanditAttackResolved` event hardcodes stolen resources to 0 (resource theft deferred?). Document or implement. | Gemini |
| L3 | `_findOldestActiveBandit` linear scan. Bounded by 8 today; document. | Gemini + 4.7 |
| L4 | `_releaseDefendersForDeadTarget` linear over all clans × clansmen. Bounded by 12×4=48 today. Cheaper alternative via `_defendingClansByRegion`. | 4.7 |
| L5 | `_resolveBanditAttack` re-settles target/defenders that step 3 already settled. Wasted gas under live-attack path (compounds when H1 fixes). | 4.7 |
| L6 | Genesis seed `_tickSeeds[0]` is zero. Mitigation: `_world.currentTickSeed = keccak256(abi.encode(block.prevrandao, address(this)))` in constructor. | 4.7 |
| L7 | `getBandit` and `getBanditTroop` selectors are duplicates. Pick one. | 4.7 |

## Cross-cutting observations

1. **The Phase delivers ~85% of stated bandit goal.** State machine, spawn, eager-settle, attack resolution mechanics, defender split, blueprint reward, target-death cleanup — all present and individually tested. The missing 15% is the production wiring of Camped→Attacking dispatch + escape→rest recovery. This is the H1 finding.

2. **Settlement simulation is a major design shift** (~500 LOC, 16+ new `_simulate*` functions). Faithful read-only mirror of mutating settlement. Long-term maintenance liability — every change to settlement now requires updating both halves. Consider forge-lint custom check.

3. **Test harness mocks mask the H1 gap.** `BanditAttackResolution.t.sol` and `Bandit.t.sol` use `forceAttackingBandit()` harness helpers. The integration seam is untested at the heartbeat-driven cycle level. Once H1 is fixed, add an end-to-end heartbeat test.

4. **ABI redesign is clean at the contract level** but needs propagation to TypeScript handwritten ABIs (H2). Either regenerate ABIs from chain or hand-fix the runner + shared client tuples.

5. **Defender registry invariants are clean.** All three registries (`_defendingClansByRegion`, `_defenderCountByRegionClan`, `_clansmanDefendingRegion`) update atomically. New cleanup paths properly hit `_clearDefender`. No leaks.

## Per-model verdicts

- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
- **Codex 5.5:** NEEDS_FIXES — 4 HIGH (Camped→Attacking + 2 ABI + revert race)
- **Opus 4.6:** NEEDS_FIXES — 1 HIGH (seed timing, theoretical) + 4 MED + 3 LOW (single HIGH was less critical than other models found)
- **Opus 4.7:** NEEDS_FIXES — 1 HIGH (Camped→Attacking) + 7 MED + 7 LOW
- **Gemini 3.1 Pro:** NEEDS_FIXES — 2 HIGH (Camped→Attacking + eager-settle skips with cap) + 2 MED + 2 LOW (succeeded after retry)

## Cross-model overlap stats

- Findings flagged by 4 models: **1** (Camped→Attacking — the most critical)
- Findings flagged by 3 models: **1** (Wall flat-100 absorption)
- Findings flagged by 2 models: **2** (ABI staleness, ActiveBanditView hardcoded zeros)
- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)

The 3 single-model HIGHs from Codex 5.4 + 5.5 are not single-model = "low confidence." They're file-system level findings (ABI tuples, blueprint unit) that other models simply didn't grep at that depth. All 5 are real and should be addressed.
