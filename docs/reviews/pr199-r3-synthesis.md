# Phase Super-Swarm Synthesis (R3) — PR #199 (head 36420da)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.6 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✓ — full 5-model lineup per Liam directive
**Phase:** dev-phase-8-buildings — Buildings + Progression
**Diff size:** 2788 lines (post R2 fix-round merge of PR #288)

## Summary

**Verdict: NEEDS_FIXES — STRONG signal. 3-model consensus on 2 HIGHs (monument sim reach-tick + ABI staleness), 2-model overlap on 3 more HIGHs, plus 2 single-model HIGHs that are credible.**

R2 correctly addressed all 6 R1 HIGHs (death-refund, BuildWall→UpgradeWall, level-binding struct, reservation timing, durations, rank simulation). But the R2 fixes themselves introduced/left several integrated-state regressions — exactly the pattern super-swarm exists to catch (same shape as Phase 10 R2's `_resetColdDamageForAllClans` regression).

The riskiest pattern: Phase 8 now has THREE notions of settlement truth — heartbeat-mutated storage, lazy `_settleClan`, view-only `_simulateSettleToTick`. Once rankings depend on simulation, any side-effect missing from the simulation amplifies into a shipping bug.

## MUST FIX — multi-model consensus

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| **H1** | `ClanWorld.sol:1262-1286, :2807, :2818` (`_simulateSettleMonumentUpgrade`, `_getClanScoreFromClan`) | 5.5 + Gemini Pro + 4.7 = **3/5** | **HIGH** | **Simulated monument upgrade gets `type(uint64).max` time-component → top of leaderboard.** Sim increments `sim.clan.monumentLevel` in memory but never writes `_monumentLevelReachedAt[clanId][newLevel]` (cannot — view). `_getClanScoreFromClan` reads `_monumentLevelReachedAt` from STORAGE → returns 0 → `timeComponent = type(uint64).max - 0 = MAX`. Any clan whose upgrade is queued but unsettled outranks every clan whose upgrade was actually committed. Violates v4 §6.9 spec ("earlier first-reached tick wins"). Gameable: re-queue between heartbeats to flash to #1. **R2's "rank getters stale" fix only propagated the simulated level, not the reach-tick.** Fix: thread `simMonumentReachedAt` into `SettlementSimulation`, populate inside `_simulateSettleMonumentUpgrade`, prefer in-memory value when storage is 0. Add regression test. |
| **H2** | `packages/contracts/abi/IClanWorld.json` (full file) + `packages/runner/src/runnerCastHeartbeat.ts:22, :164` + `packages/shared/src/adapters/IChainClient.ts:24, :280, :377` | 5.4 + 5.5 + 4.7 + cloud Copilot = **4/5 + cloud** | **HIGH** | **ABI artifact + hardcoded TS adapters all stale.** Verified: `WallUpgraded`, `BaseUpgraded`, `MonumentUpgraded` events declared in `IClanWorld.sol:548-550` + emitted in `ClanWorld.sol:822/869/906` — completely absent from committed ABI JSON. `ResourcesDeposited` still encodes old `wood/iron/wheat/fish/atTick uint64`; source now uses `woodDelta/.../tick uint32`. Topic-0 = `keccak(canonicalSig)` — uint64→uint32 rotates the topic hash, indexers silently miss every deposit event. Plus: `WorldSnapshot` + `Mission` tuple shifts (`currentSeasonNumber`, `nextHeartbeatAtTick`, `submittedAtTick`, `executesAtTick`, `settlesAtTick`) inserted before existing fields → handwritten viem ABIs in `IChainClient` + `runnerCastHeartbeat` decode wrong slots → `getCurrentTick()` + `readNextHeartbeatAt()` broken. **Fix:** regenerate ABI JSON via `forge build` + copy. Update hardcoded TS structs OR import canonical ABI fragment. Add CI/pretest guard that diffs regenerated against committed + fails on drift. |

## MUST FIX — 2-model + credible 1-model HIGHs

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H3 | `ClanWorld.sol:944` (sim) vs `:414-417` (real) | Gemini Pro + 4.7 = **2/5** | HIGH | **`_simulateSettleToTick` has no tick-loop cap → RPC-timeout DoS for passive clans.** Real `_settleClan` caps at 200 ticks/call; sim has no cap. `getRankings()` invokes sim per-clan, iterating `(currentTick - lastSettledTick) × clansmen × per-tick work`. Clans with all clansmen WAITING/IDLE never get `lastSettledTick` advanced by heartbeat (heartbeat only touches clans with mission settlements). View function so no on-chain DoS, but indexers + season-end UIs hitting non-archive RPCs will time out exactly when needed most. **Fix:** mirror real cap inside sim: `if (toTick > fromTick + 200) toTick = fromTick + 200;`. |
| H4 | `ClanWorld.sol:1356, :799, :829, :861, :2181, :2251, :2332` | Codex 5.4 + Codex 5.5 = 2/5; Opus 4.6 grades MED | HIGH | **Reservation level-binding still violated — undercharging on out-of-order settlement.** Reservations store `fromLevel/toLevel` per R2 H6 but settlement does `_min(heldCost, currentCost)`. If two upgrades queued cs2→cs1 settle in clansman-id order cs1→cs2, level-2 reservation can settle first as level 1 → pays cheaper, then level-1 reservation settles as level 2 paying only old level-1 cost. Concrete: queue wall upgrades cs2 then cs1; clan reaches level 2 paying `20+20` instead of `20+35`. **R2's H6 fix added the struct fields but settlement never reads them.** Fix: bind settlement to reservation order OR require `currentLevel == reservation.fromLevel` before applying. |
| H5 | `ClanWorld.sol:1326, :1368` (heartbeat) vs `:423-440` (lazy) | Codex 5.4 + Codex 5.5 = **2/5** | HIGH | **Heartbeat vs lazy settlement chronological mismatch.** `heartbeat()` settles due missions via `_settleCompletingMissions` but does NOT apply per-tick upkeep/regrow or advance `clan.lastSettledTick`. Lazy `_settleClan` applies upkeep FIRST (line 423-440). Outcomes order-dependent: base/monument upgrades can spend wheat before upkeep on heartbeat path, while lazy would consume upkeep first; deposits/market credits available before unsettled upkeep on heartbeat. **Plus:** `quoteLootValueSettled` / `getClanScore` / `getRankings` use sim from stale checkpoint → diverge from heartbeat. Fix: shared chronological settlement core for heartbeat + lazy + simulation; OR heartbeat advances the same checkpoint as lazy. |
| H6 | `ClanWorld.sol:448, :1956, :2157, :2221, :2294` | Codex 5.4 = 1/5 (CRITICAL) | HIGH | **`_reserved*ByClan` only affects order validation — `_applyUpkeep()` and `_deductFromVault()` still spend raw vault.** Queued upgrade does NOT lock reserved resources against upkeep/market sells. Same TOCTOU class the reservation fix was meant to remove: upgrade validates successfully → upkeep/market consumes the "held" wheat/wood/iron → settlement fails with `ERR_MISSING_RESOURCES`. Even though reservation accounting stays consistent, the lock is incomplete. **Fix:** escrow/debit reserved resources at queue time, OR make every vault consumer spend only from `vault - reserved`. |
| H7 | `ClanWorld.sol:528, :2570` | Gemini Pro = 1/5 | HIGH | **Deprecated `BuildWall` action orphans flighted missions.** R2 removed `BuildWall` from `_resolveAction` and removed its duration handler (implicitly returns 0). If a clan has an existing flighted `BuildWall` mission from prior to this release, it falls through the action resolver, never calls `_completeMission`, and `m.settlesAtTick` never updates. `tick >= m.settlesAtTick` permanently true but action doesn't resolve → clansman permanently stuck in ACTING. **Fix:** restore `BuildWall` in `_resolveAction` as graceful fallback that immediately calls `_completeMission(cs, m)` or refunds resources. |
| H8 | `ClanWorld.sol:969` | Gemini Pro = 1/5 | HIGH | **Simulation abruptly terminates continuous gathering missions.** `_simulateSettleMissionForClansman` for continuous missions (e.g. ChopWood, duration > 0) that execute action but don't hit carry cap (`m.active` remains true) calls `_simulateCompleteMission`. This forces all continuous gathering missions to prematurely complete after exactly one yield tick. Fails to reschedule `m.settlesAtTick` like real settlement does. `quoteLootValueSettled` and `getRankings` SEVERELY underestimate gathered resources of unsettled clans. Same family as H1 — sim missing a side-effect of real settlement. **Fix:** mirror real settlement — reschedule `m.settlesAtTick = tick + getActionDuration(m.action)` rather than abort. |

## SHOULD FIX (MEDIUM)

| # | Models | Finding |
|---|---|---|
| M1 | 5.4 + 5.5 + 4.7 + 4.6 + Gemini = **5/5 consensus** | **Cross-type upgrade-mission switch falsely reverts when resources are tight.** Validation runs before refund of previous building's reservation. Switching from UpgradeWall → UpgradeBase: validation only releases same-type reservation, sees old wall's wood still in `_reservedWoodByClan` → `ERR_MISSING_RESOURCES`. UX-only no data corruption. |
| M2 | 4.7 = 1/5 | Sim death-branch does not credit refund into `sim.clan.vault*`. Latent until Phase 9 adds death paths but worth proactive fix. |
| M3 | 4.7 = 1/5 | Two redundant events per upgrade (`WallLevelChanged` + `WallUpgraded` etc). 1.5k extra gas per upgrade. Drop the `*Upgraded` events. |
| M4 | 4.7 = 1/5 | Insertion-sort + sim scaling at MAX_CLAN_SCAN_FOR_RANKING=24 untested. All ranking tests use 2-3 clans. |
| M5 | 5.4 = 1/5 | `getDerivedClanState/Clansman` interface promises settled-forward semantics; impl returns mostly raw storage. |
| M6 | 5.4 = 1/5 | `getWorldSnapshot()` / `getClanFullView()` bypass simulator; `getClanScore`/`getRankings` use sim. Mixed views per block. |
| M7 | 5.4 = 1/5 | `BuildWall` still public ActionType enum value but impl hard-rejects → silent runtime break for stale callers. |
| M8 | 5.5 = 1/5 | `ClanWorldStub.sol` returns 0 for action durations; real returns 1. Stub-backed previews wrong. |
| M9 | 5.5 = 1/5 | Reserved resources invisible to callers — `getClan()` returns full vault; planners see resources as available. |

## DEFER (LOW)

- L1: `_allClanIds.length < 12` (line 1443) but `getRankings` comment says 12 → off-by-one cosmetic.
- L2: `CLANSMAN_CARRY_CAP = 10e18` dead code — not referenced; conflicts with `WOOD_CAP = 15e18`. Delete or wire up.
- L3: `WOOD_YIELD_PER_TICK = 1e18` only used as RHS of `WOOD_CRIT_BONUS` definition.
- L4: `ClanWorldStub.sol` re-implements upgrade cost tables — extract to shared library.
- L5: `BuildWall` enum slot left for wire-format stability — document in NatSpec.
- L6: `Gathering.t.sol::test_chopWoodCritDistributionAcrossSeeds` window is 2.5σ flaky.
- L7: Missing tests for empty-world rankings, single-clan rankings, DEAD-mid-flight, vault-drain-race-at-settle.
- L8: `monumentReachedAtTick == 0` sentinel collides with literal-zero — fragile. Encode as `tick + 1`.
- Various polish from gemini + 4.7.

## Cross-model overlap stats

- **5/5 consensus:** M1 cross-type swap rejection
- **3/5 + cloud:** H2 ABI staleness (Codex 5.4 + 5.5 + Opus 4.7 + cloud Copilot)
- **3/5:** H1 monument sim reach-tick (Codex 5.5 + Gemini + 4.7)
- **2/5:** H3 sim tick-loop cap, H4 reservation undercharge, H5 heartbeat-vs-lazy
- **1/5:** H6 reserved-vs-upkeep TOCTOU (Codex 5.4 unique, CRITICAL), H7 BuildWall orphan (Gemini), H8 continuous mission terminate (Gemini)

## Per-model verdicts

- **Codex 5.4:** NEEDS_FIXES — 5 HIGH + 4 MED. Most thorough on TOCTOU + heartbeat semantics.
- **Codex 5.5:** NEEDS_FIXES — 4 HIGH + 3 MED.
- **Opus 4.6:** APPROVE / CLEAN HIGH — 0 HIGH + 2 MED. Outlier; says "R2 fixes introduced no regressions" but 4 other models disagree.
- **Opus 4.7:** NEEDS_FIXES — 3 HIGH + 5 MED + 8 LOW. Most concise + verified ABI staleness directly.
- **Gemini 3.1 Pro:** NEEDS_FIXES — 4 HIGH + 1 MED + 1 LOW. Caught BuildWall orphan + continuous-mission terminate uniquely.

## Decision

**Recommend Path A (R4 fix-round) — multi-model consensus on 2 HIGHs is decisive. Phase 8 cannot ship to UAT in current state.**

Fix scope (priority order):
1. **H2 ABI staleness** — regenerate JSON + update TS adapters + add CI guard. ~15 min. Largest model-overlap.
2. **H1 monument sim reach-tick** — thread reach-tick into SettlementSimulation. ~20 min. 3-model consensus + spec violation + gameable.
3. **H4 reservation level binding** — settlement reads `reservation.fromLevel`. ~15 min. 2-model HIGH + opus 4.6 MED concession.
4. **H5 heartbeat vs lazy chronological** — shared settlement core OR heartbeat advances checkpoint. ~30-45 min. 2-model HIGH; structural.
5. **H6 reserved-vs-upkeep TOCTOU** — make all vault consumers respect `vault - reserved`. ~20 min. Single-model but critical.
6. **H7 BuildWall flighted-mission orphan** — restore graceful fallback in `_resolveAction`. ~10 min.
7. **H8 continuous mission termination** — reschedule `settlesAtTick` in sim. ~10 min.
8. **M1 cross-type swap** — release sibling-type reservations in validation. ~15 min. 5/5 consensus.

Total budget: ~2-3 hours codex.

R4 fix-round → re-merge → R4 super-swarm (all 5 again) before Liam UAT.

**Pattern observation (worth a memory):** R2 fixes that touched the duplicate state machine (sim) introduced the H1 + H8 regressions. Same shape as Phase 10 R2's `_resetColdDamageForAllClans` regression. **Whenever a fix touches a duplicate state machine, propagate ALL relevant side-effects, not just the obviously-named field.** Long-term: extract shared logic into pure functions both real and sim invoke.

**Liam: Path A R4 fix-round confirmed?**
