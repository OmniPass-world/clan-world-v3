# Phase Super-Swarm Synthesis — PR #250 (head e4a0d4c)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro (skip — quota) | Opus 4.6 (skip)
**Phase:** dev-phase-10-winter — Winter + Elimination
**Diff size:** 1645 lines

## Summary

**Verdict: NEEDS_FIXES — 3 cross-model HIGH (real game-logic bugs) + several MED.**

All 3 reviewers landed on the same critical issues. The phase ships winter cadence, food upkeep, cold damage, crop transitions, and clan death — but the death paths have determinism + timing + scaling bugs that change game outcomes.

## MUST FIX (cross-model consensus)

| File:line | Models | Severity | Finding |
|---|---|---|---|
| H1: `ClanWorld.sol:486-503` (`_killRandomClansmanFromCold`) | 5.4 + 5.5 + 4.7 = **3/3** | HIGH | **Cold-damage RNG uses `_world.currentTickSeed` instead of historical `_tickSeeds[tick]`.** Breaks lazy-settle determinism. Two callers settling the same dead-clan at different world ticks get different RNG outcomes for the SAME historical cold-damage event. **Exploitable:** Elder who sees their wall collapsing can race `settleClan` at a beneficial heartbeat to bias which of their own clansmen die. **Fix:** replace `_world.currentTickSeed` with `_tickSeeds[tick]`. Trivial diff. Add regression test: same death scenario settled at two different "now" ticks → same clansman dies. |
| H2: `ClanWorld.sol:431-442` (`_applyUpkeep` + `_killNextClansmanFromStarvation`) | 5.4 + 5.5 + 4.7 = **3/3** | HIGH | **Starvation kill timing/guard issue.** Two reads converge on the same code: (a) Codex 5.4 reads it as off-by-one (kills on tick starvation first detected, conflicts with spec's "next tick" ordering), (b) 5.5 + 4.7 read it as inverted guard (`starvationStartsAtTick >= winterStartsAtTick` grants pre-winter starvers IMMUNITY from winter starvation kills — opposite of intent). **Both are real.** Fix: drop the `>= winterStartsAtTick` clause AND defer kill to `tick + 1` per spec §4.12/A10. Update test at `ClanWorld.t.sol:2219-2242` (off-by-one expectation) + add inverted-guard regression. |
| H3: `_applyUpkeep` wood-burn semantics | 5.5 + 4.7 = **2/3** | HIGH | **Wood burn implemented as `livingClansmen × 0.5e18` (per-clansman) but spec §7.3 says `1e18` per BASE per tick.** A 4-clansman clan burns 2e18 vs spec's 1e18. New tests codify the wrong expectation. **Fix:** flat `1e18` per base per winter tick, regardless of clansman count. Update tests. |

## SHOULD FIX (single-model HIGH + cross-cutting)

| Severity | Models | Finding |
|---|---|---|
| HIGH (1/3) | 5.5 | `WinterStarted(uint64)` / `WinterEnded(uint64)` event signatures changed to `uint32`. Hand-written decoders break. Either keep uint64 OR ensure all consumers regenerate from updated ABI. |
| MED (2/3) | 5.4 + 4.7 | Wheat plots NOT cleared on winter start — spec §843 says clear. Tests assert preserved state. Spec-vs-impl mismatch. |
| MED (1/3) | 5.4 | Tests at `:2190-2268` assert "one clansman died" but never pin which `csId` or determinism. Test gap masks the seed bug. |
| MED (1/3) | 4.7 | Several smaller findings in Opus 4.7's truncated review (unable to extract all). |
| LOW (1/3) | 5.4 | `DOMAIN_COLD_DAMAGE = keccak256("cold_damage")` lacks namespacing/versioning vs other RNG salts (`clanworld.*.v1`). |

## Per-model verdicts

- **Codex 5.4:** NOT READY — 2 HIGH (RNG + starvation off-by-one) + 2 MED + 1 LOW
- **Codex 5.5:** NEEDS FIXES — 4 HIGH (RNG + wood burn + starvation guard + ABI break) + MED
- **Opus 4.7:** NEEDS FIXES — 3 HIGH (RNG + starvation guard + wood burn) + several MED

## Cross-model overlap stats

- 3/3 consensus: 2 HIGHs (RNG + starvation timing/guard) — STRONGEST SIGNAL
- 2/3 overlap: 1 HIGH (wood burn) + 1 MED (wheat plot clear)
- 1/3 only: ABI uint64→uint32 break, RNG domain naming, test determinism gap

## Decision

**Recommend Path A (fix-round).** Three game-balance HIGHs caught by cross-model consensus is exactly what super-swarm is for. Specifically:
- H1 is a 1-line code change (use `_tickSeeds[tick]`)
- H2 is a guard-clause logic flip + spec-aligned timing
- H3 is a per-clansman → per-base scaling fix

Plus the H4 ABI uint64→uint32 needs a decision (keep uint64 OR explicit ABI break note).

**Liam: Path A (fix-round, ~30-60 min codex), Path B (file as Phase 10B follow-ups + ship now), or Path C (super-swarm round 2 after fix)?**

If Path A, dispatch codex on a Phase 10B fix-round with the 3 cross-model HIGHs + 1 single-model HIGH + 1 MED (wheat plot clear). Re-super-swarm.

If Path B, file as follow-ups + Phase 10 ships at this result. The bugs are real but specific to winter mechanics — they affect game balance not contract safety.
