# Phase 10 R2 Synthesis — UPDATE (Opus 4.7 completed)

Adding Opus 4.7's review (it actually completed; the file was 0 bytes during my first synth pass — re-dispatched and finished). This is now a duo-verdict (Codex 5.5 + Opus 4.7) on PR #250 (head 1210e90).

## Updated cross-model verdict

**Codex 5.5 + Opus 4.7 = 2/2 NEEDS_FIXES.** R1 fix-round addressed the requested 4 HIGHs cleanly (verified by both), but the fix-round ALSO INTRODUCED a NEW HIGH that Opus reproduced locally + matches a cloud reviewer (Codex P1) finding I had not captured.

## NEW HIGH (cross-tier consensus: Opus 4.7 + Codex P1 cloud)

| # | File:line | Finding |
|---|---|---|
| H3 | `ClanWorld.sol:1126` (`_resolveWorldEvents`), `:1164-1168` (`_resetColdDamageForAllClans`) | **Heartbeat-eager `_resetColdDamageForAllClans()` corrupts mid-winter state for partially-settled clans.** Opus reproduced locally: a clan settled at tick 113 (3 winter ticks processed, coldDamage=3, wallLevel=9) gets `coldDamage=0` zeroed by the eager heartbeat reset at winter-end. Lazy `_settleClan` then replays ticks 113-119 starting from `coldDamage=0` — gets only 3 wall degradations vs the 5 the never-settled clan gets. **Same clan ends winter at wallLevel=6 instead of 5.** Worst case: missed clansman death (livingClansmen + elimination outcome divergence). Cloud Codex P1 flagged this independently as DoS surface. Opus's repro shows it's a CORRECTNESS bug, not just DoS. **Fix:** delete `_resetColdDamageForAllClans()` + its call site at line 1126. The in-loop reset at `_applyUpkeep:404-406` and post-loop reset at `_settleClan:393-395` already handle the boundary correctly during replay. The eager scan is redundant + harmful, AND saves O(n_clans) heartbeat gas. |

## Cross-tier finding tally (updated)

| # | File:line | Tier 1 (codex 5.5) | Tier 2 (opus 4.7) | Tier 3 (cloud Copilot/Gemini-CA/Codex-bot) | Severity |
|---|---|---|---|---|---|
| H1 | `ClanWorld.sol:431-441` (starvation guard) | HIGH | (in test verification — agrees) | — | HIGH |
| H2 | `IClanWorld.sol:192,:414` (TS ABI staleness) | HIGH | — | — | HIGH (single tier) |
| H3 (NEW) | `ClanWorld.sol:1126` (cold reset) | — | HIGH (repro) | Codex-bot P1 | **HIGH (2-tier consensus)** |
| H4 (NEW) | `ClanWorld.sol:1137-1152` (require could brick world) | — | LOW (L-5) | Gemini-CA HIGH | **HIGH (cloud, opus low)** |
| M1 | `ClanWorld.sol:440-447` (wood burn post-starv) | MED | — | — | MED |
| M2 | `ClanWorld.sol:1276-1290` (winter-mint plots) | MED | — | — | MED |
| M3 (NEW) | `IClanWorld.sol:28-31` (winter-tick shift) | — | MED | — | MED |
| M4 (NEW) | `IClanWorld.sol:506-509` (uint32/uint64 tick mismatch) | — | MED | — | MED |
| L-1...7 | various | — | LOW (7) | — | LOW (polish) |

## Updated MUST FIX scope (consolidated for fix-round dispatch)

1. **H1 starvation `max(starvationStartsAtTick, winterStart)` guard** — codex 5.5
2. **H2 TS ABI regen for `WorldState` + `WorldSnapshot` layouts** — codex 5.5
3. **H3 delete `_resetColdDamageForAllClans()` + call site** — opus 4.7 + cloud codex
4. **H4 add `require(_allClanIds.length <= 24)` to `mintClan` OR raise `MAX_CROP_TRANSITION_PER_TICK`** — gemini-CA + opus
5. **M1 wood burn ordering** — codex 5.5
6. **M2 winter-mint plot state** — codex 5.5
7. **M3 spec note for winter-tick offset** — opus
8. **M4 uint64 tick consistency on new ColdShortage/WallDegraded/ClansmanColdDeath events** — opus

## Decision

**Path A fix-round on H1+H2+H3+H4 (HIGHs) + M1+M2+M4 (correctness MEDs). M3 is doc-only, can defer.**

Total fix budget ~45 min codex (4 HIGHs + 3 correctness MEDs).

The Phase 10 R1 fix-round has already shipped a CORRECTNESS REGRESSION (H3 cold reset bug) — this is exactly what super-swarm exists to catch. Opus's local repro is deterministic. Don't merge phase 10 → dev until H3 is fixed.

H1 cold-damage RNG seed remains parked per Liam (MAYBE-future curiosity, #283).

**Liam: Path A (fix-round) confirmed?**
