# Phase Super-Swarm Synthesis — PR #199 (head 33193b7)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.7 ✓
**Phase:** dev-phase-8-buildings — Buildings + Progression
**Diff size:** 2147 lines

## Summary

**Verdict: NEEDS_FIXES — STRONG signal. 4 cross-model HIGHs + 2 single-model HIGHs. Resource-burn footguns reachable in production.**

This is the most actionable super-swarm result so far. All 3 reviewers found the same fundamental issues + each surfaced unique critical findings. Phase 8 ships the right shape (queue-time vault reservation, score/rank getters) but the reservation/refund lifecycle has multiple silent-resource-loss paths.

## MUST FIX — cross-model HIGHs

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H1 | `ClanWorld.sol` `_processOrder`, `_validate*Upgrade*`, `_settle*Upgrade*` | 5.4 + 5.5 + 4.7 = **3/3** | HIGH | **Upgrade reservations debit vault at submit-time, not at homebase-arrival.** Spec §8 (`clanworld_v4_spec.md:882, :941`): build "not instant at submission time" — checked "if a clansman reaches homebase and attempts a build action." Impl debits during `_processOrder` even before travel completes. Affects upkeep + score + bandit-loot timing while builder is traveling. **Fix:** defer debit to settle-time (when clansman reaches homebase + ACTING tick). |
| H2 | `ClanWorld.sol:2130, 2134` (durations); spec `clanworld_v4_spec.md:877, :942` | 5.4 + 5.5 = **2/3** | HIGH | **Upgrade durations 2/2/4 ticks, but spec says single-tick.** New `MissionTiming.t.sol:130-136` tests CODIFY the wrong values. Either fix code (durations → 1) OR patch spec (durations → 2/2/4) — but the implementation MUST match exactly one of them. |
| H3 | `ClanWorld.sol:2274, 2300, 2351` (`quoteLootValueSettled`, `_getClanScore`, `getRankings`) | 5.4 + 5.5 + 4.7 = **3/3** | HIGH | **Rank getters read raw committed storage, NOT settled/derived path.** Violates Phase 8.4 / #261 derived-getter contract from earlier today. Pending upkeep/deposits/deaths produce stale scores. **Regression of #230 fix.** Fix: rewire getters through `_simulateSettleToTick` per #261. |
| H4 | `ClanWorld.sol` `_doBuilding`, `_tryBuildWall`, `_settleWallUpgrade` | 5.5 + 4.7 = **2/3** | HIGH | **Legacy `BuildWall` action still exists alongside new `UpgradeWall` — both increment wallLevel + use same cost table.** Race: clansman A queues UpgradeWall (reserves 50w+15i); clansman B queues BuildWall (no reservation); B settles first (duration 1 vs 2), reaches wallLevel 5; A settles, hits cap, reservation cleared but NOT refunded → 50w+15i destroyed. **Fix:** remove or route BuildWall through reservation path. |

## MUST FIX — single-model but CRITICAL

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H5 | `ClanWorld.sol:544-559` (`_markClansmanDead`), `:565-591` (`_markClanDead`) | 4.7 = 1/3 | HIGH | **Cold-damage + starvation deaths DON'T refund upgrade reservations.** `_markClansmanDead` sets `cs.state = DEAD; m.active = false;` immediately. The death-path refund branch in `_settleMissionForClansman:349-364` gates on `if (m.active)` — already false. Permanent vault resource loss + `_pending*UpgradesByClan` counter never decrements → eventually blocks ALL future upgrades. **Phase 4.4 winter cold damage already ships this regression.** Fix: extract `_invalidateActiveMission` helper that calls refund THEN sets m.active=false. Apply in `_markClansmanDead` + `_markClanDead`. |
| H6 | `ClanWorld.sol` `_reserve*Upgrade`, `_settle*Upgrade` | 5.5 = 1/3 | HIGH | **Reservations not bound to fromLevel/toLevel they paid for.** Reservations price `currentLevel + pendingUpgrades`; settlement does `old + 1`. Earlier queued upgrade canceled → later reservation settles lower transition while paying for higher. **Concrete:** monument L5 queues 2 upgrades; second pays L7 cost (incl 1e18 blueprint); first canceled → second settles L6 while burning L7 blueprint. Fix: store `fromLevel/toLevel` in reservation struct. |

## SHOULD FIX (MEDIUM)

| # | Models | Finding |
|---|---|---|
| M1 | 5.5 | Cross-type retasking (UpgradeWall→UpgradeBase) fails with `ERR_MISSING_RESOURCES` because validation runs before previous reservation refunded. |
| M2 | 5.4 + 5.5 | **PR silently retunes wood economy constants OUTSIDE Phase 8 scope** — `WOOD_CAP`/`WOOD_BASE_YIELD`/`WOOD_CRIT_BONUS`/`WOOD_CRIT_BPS` changed in `IClanWorld.sol:43-53`. Spec still has original values. Either patch spec OR revert PR diff. |
| M3 | 4.7 | Test coverage missing for: travel-before-build, replacement refund, death refund, cross-type replacement. |
| M4 | 5.4 | New tests don't pin RNG determinism / death identity — masks H5 reservation orphan bug. |

## DEFER (LOW)

- `getRankings` truncates to first 24 clan IDs — masked by 12-clan mint cap but future correctness cliff.
- Wall level as 4th ranking key matches `clanworld_v4_spec.md:956` but disagrees with `clanworld_frontend_spec.md:169` (frontend sorts only by monument level, reach tick, loot value). Spec drift between contract + frontend doc.
- `DOMAIN_COLD_DAMAGE` keccak naming hygiene (Phase 10 super-swarm caught same).

## Per-model verdicts

- **Codex 5.4:** NOT READY — 3 HIGH (durations + reservation timing + rank stale) + 2 MED + 1 LOW
- **Codex 5.5:** NEEDS FIXES — 4 HIGH (BuildWall/UpgradeWall race + reservation level-binding + reservation timing + rank stale) + 1 MED + 1 LOW
- **Opus 4.7:** NEEDS FIXES — 4 HIGH (death-refund orphan + BuildWall race + reservation timing + others) + several MED. Clearest writeup of the death-refund bug (H5).

## Cross-model overlap stats

- 3/3 consensus: H1 (reservation timing) + H3 (rank stale) — STRONGEST signals
- 2/3 overlap: H2 (durations off-spec) + H4 (BuildWall race) + M2 (wood economy retune)
- 1/3 only: H5 (death-refund orphan — but CRITICAL because reachable via shipped Phase 4.4 winter), H6 (level-binding mismatch)

## Decision

**Recommend Path A (fix-round) — high confidence.** Phase 8 has the most multi-model HIGH overlap of any phase super-swarm so far + 1 critical single-model finding (H5 death-refund) that's already shipping resource-loss bugs via Phase 4.4 winter. Fix scope:

1. **H1:** defer upgrade vault debit to settle-time, not submit-time
2. **H2:** fix durations to 1/1/1 (or patch spec to 2/2/4 with rationale)
3. **H3:** rewire `quoteLootValueSettled` + `_getClanScore` through `_simulateSettleToTick`
4. **H4:** delete legacy `BuildWall` OR route through reservation
5. **H5:** extract `_invalidateActiveMission` helper, call refund in death paths
6. **H6:** add `fromLevel/toLevel` to reservation struct, validate at settle
7. **M2:** revert wood economy constants OR patch spec

Phase 8 fix-round is 1-2 hours of codex work. Re-super-swarm after.

**Path B (defer all):** file H1-H6 + M2 as Phase 8B follow-ups. Risky — H5 is exploitable + already shipping. Path B not recommended.

**Liam: Path A or B?**
