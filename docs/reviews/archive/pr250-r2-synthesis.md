# Phase Super-Swarm Synthesis (R2) — PR #250 (head 1210e90)

**Models run:** Codex 5.5 ✓ | Opus 4.7 ✗ (file empty, suspected timeout/quota) | Codex 5.4 + Opus 4.6 + Gemini 3.1 Pro skipped (R2 dispatched as duo, opus failed)
**Phase:** dev-phase-10-winter — Winter + Elimination
**Diff size:** R2 fix-round on PR #287

## Summary

**Verdict: NEEDS_FIXES — single-model verdict but concrete + targeted findings.** R1 fixes mostly landed, but Codex 5.5 found 2 NEW HIGHs that the R1 fix-round introduced or didn't fully close: starvation timing edge case for pre-winter starvers + hardcoded TS ABI staleness after WorldState/WorldSnapshot layout additions.

## R1 Fixes Verified Cleanly

- ✅ Wood formula correct for non-starving 4-clansman clan: `3e18` (per-clansman 0.5e18 × 4 + per-base 1e18)
- ✅ `WinterStarted` / `WinterEnded` events back to `uint64` (matches spec)
- ✅ Existing wheat plots lock with `remainingWheat = 0` and `regrowUntilTick = 0` at winter start
- ✅ Spec §7.3 patched to document per-base + per-clansman wood burn

## MUST FIX — single-model HIGH (codex 5.5)

| # | File:line | Severity | Finding |
|---|---|---|---|
| H1 | `ClanWorld.sol:431-441` | HIGH | **Pre-winter starvers still die one tick earlier than fresh winter starvers.** The guard `if (winter && starving && clan.starvationStartsAtTick < tick)` defers death for a clan that starts starving on the first winter tick, but not for a clan already starving BEFORE winter. Example: winter starts at tick 110; a pre-winter starver with `starvationStartsAtTick = 105` dies on tick 110, while a fresh winter starver with `starvationStartsAtTick = 110` first dies on tick 111. **The added test missed this** because it sets both clans' `lastSettledTick` after winter has already begun. **Fix:** comparison needs effective winter starvation start, e.g. `max(starvationStartsAtTick, currentWinterStartTick)`. The H2 R1 fix went 80% but missed the edge. |
| H2 | `IClanWorld.sol:192, :414`; `packages/runner/src/runnerCastHeartbeat.ts:38-51`; `packages/shared/src/adapters/IChainClient.ts:34-44` | HIGH | **Hardcoded TS ABIs stale after WorldState / WorldSnapshot layout additions.** `IClanWorld.sol` adds `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields, but the TS hardcoded structs in runner + shared adapters do NOT include them. Practical impact: `runnerCastHeartbeat.ts` decodes `currentSeasonNumber` as `nextHeartbeatAtTs` (wrong offset), and `IChainClient.ts` decodes `getWorldSnapshot` later fields at wrong offsets — the dynamic `leaderboard` offset is especially likely to fail decoding. **Fix:** regenerate TS ABI structs from current `IClanWorld.sol` state, OR if the hardcoded copy is intentional, add the new fields. The generated ABI JSON is correct; only the hardcoded TS mirrors are stale. |

## SHOULD FIX (MEDIUM)

| # | File:line | Finding |
|---|---|---|
| M1 | `ClanWorld.sol:440-447` | **Wood burn uses post-starvation living count on same tick.** If starvation kills a clansman, wood burn is calculated AFTER from the reduced `livingClansmen`. A 4-clansman clan that loses one to starvation on a tick burns `2.5e18`, not `3e18` (per-clansman 0.5×3 + per-base 1.0). Fairness wrinkle — same-tick ordering. Liam: deliberate or fix? |
| M2 | `ClanWorld.sol:1276-1290` | **Clans minted during winter start `WinterLocked` with nonzero wheat.** Winter-minted plots get `state = WinterLocked`, but still initialize `remainingWheat = WHEAT_PLOT_STARTING_WHEAT`. Harvesting is blocked, so this is a state/indexer invariant bug not a gameplay exploit, but it violates the winter-lock triple expected for plots. Fix: gate the `remainingWheat` init on winter state too. |

## DEFER

- **H1 cold-damage RNG seed** — intentionally parked per Liam directive on Phase 10 R1 ("could actually be more interesting to see if elders figure this out"). Not counted as R2 finding.

## Per-model verdicts

- **Codex 5.5:** NEEDS_FIXES — 2 HIGH (starvation edge + ABI staleness) + 2 MED (wood burn ordering + winter-mint plot state).
- **Opus 4.7:** ✗ failed (file empty) — likely timeout or quota. Single-model verdict by codex 5.5 alone.

## Cross-model overlap stats

- N/A — single-model verdict. Confidence is lower than typical R2 but findings are specific + reproducible from code-grep.

## Decision

**Recommend Path A (fix-round) on H1 + H2 + M1 + M2.** All four are concrete + actionable + small.

H1 is a 1-line `max()` change. H2 is an ABI regen. M1 is a same-tick ordering swap. M2 is a winter-state gate on plot init. Total fix budget < 30 min codex.

If Liam wants single-model R2 to be more confident, can re-dispatch Opus 4.7 (or fall back to Opus 4.6) to verify before merging. Default: trust codex 5.5 on these — they're code-grep-verified findings, not LLM speculation.

**Liam: Path A or merge as-is + file as Phase 10B?**

(My read: H1 + H2 are both shipping bugs — H1 violates spec ordering for pre-winter starvers, H2 means runner can't read heartbeat-due correctly. Both warrant a fix-round, not a defer.)
