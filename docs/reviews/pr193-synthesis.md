# Phase Super-Swarm Synthesis — PR #193 (head 9ccf94a)

**Models run:** Codex 5.3 ✓ | Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.6 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✗ (429 capacity exhausted, no recovery)
**Phase:** dev-phase-5-economy — Gathering + Deposits + Economy
**Diff size:** 476 lines

## Summary

**Verdict: NEEDS_FIXES — 5/5 working-reviewer consensus on uint32 ABI regression (3rd time this anti-pattern has been caught + reverted).**

Phase 5 delivers wood gathering, deposit semantics, and RNG simplification cleanly with solid test coverage. Three cross-model HIGHs block merge — all are surgical fixes:

1. **H1 ResourcesDeposited uint32/uint64 ABI regression** — 5/5 consensus, recognized anti-pattern (Phase 8 R2 + Phase 10 R2 already reverted identical fix)
2. **H2 Wood-only batch yield refactor** — iron/wheat/fish still single-tick; wood produces ~4× per gather vs other resources on same wall-clock
3. **H3 ERR_INVALID_REGION vs ERR_NOT_AT_HOMEBASE inconsistency** — same submit-validation block, half-migrated

## MUST FIX — multi-model consensus

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H1 | `IClanWorld.sol:535-542`, `ClanWorld.sol:455-458, :670-700`, `abi/IClanWorld.json:3272-3315` | Codex 5.3 + 5.4 + 5.5 + Opus 4.7 = HIGH; Opus 4.6 = LOW. **5/5 noted.** | **HIGH** | **`ResourcesDeposited` is the lone event downcasting tick to `uint32`** while every other event in `IClanWorldEvents` (`ResourcesGathered`, `WallLevelChanged`, `BaseLevelChanged`, `ColdDamageApplied`, `BanditDefeated`, etc.) uses `uint64`. Topic-0 changes → existing indexers/subgraphs silently miss every deposit event. **Plus the committed ABI artifact at `abi/IClanWorld.json` still advertises old `uint64 atTick`** so even contract-side it's drifted. **Opus 4.7 notes:** Phase 8 R2 (commit `36420da`) and Phase 10 R2 (commit `1210e90`) BOTH reverted identical `uint32` ABI regressions — this is a recognized anti-pattern. **Fix:** revert `ResourcesDeposited` to `uint64 atTick` (matches naming `atTick` of peer events too); revert `_doDeposit` signature to `uint64`; drop `unsafe-typecast` lint comment + `DEPOSIT_DURATION_TICKS` typing change cascade; regenerate ABI artifact + add CI parity check. |
| H2 | `ClanWorld.sol:339-343, :513-667, :1799-1805` (gather paths); `IClanWorld.sol:50` (constants) | Codex 5.4 = HIGH; Opus 4.6 + 4.7 = MED. **3/5.** | **HIGH** | **Wood-only batch yield refactor — iron/wheat/fish still on legacy single-tick yield model.** The new model `yield = WOOD_YIELD_PER_TICK × getActionDuration(action)` is only applied to `_gatherWood`. Verified: `_gatherIron` still `IRON_BASE_YIELD` (no duration), `_gatherFishDocks/_gatherFishDeepSea` hardcoded `1e18` per gather, `_gatherWheat` constant `WHEAT_HARVEST_RATE`. All four resources have `getActionDuration(...) = 4` ticks, so wood now produces **~4× per gather call** vs the others. Either deliberate Phase 5.1 step (file follow-ups) OR incomplete refactor breaking economy balance. Title says "Gathering / Deposits / Economy" — implies broader scope. **Fix:** either migrate iron/wheat/fish to per-tick model in this PR, OR file Phase 5B follow-up issues with `IRON_YIELD_PER_TICK` / `WHEAT_YIELD_PER_TICK` / `FISH_YIELD_PER_TICK` and explicitly defer with code comment. |
| H3 | `ClanWorld.sol:1613, :1619, :1689` (validation block); `IClanWorld.sol:153-170` | Opus 4.7 = HIGH; Codex 5.3 + 5.4 + 5.5 = LOW/MED. **4/5 noted, 1 HIGH.** | HIGH | **`ERR_INVALID_REGION` vs `ERR_NOT_AT_HOMEBASE` inconsistency.** Three actions in same submit-validation pass enforce identical "gotoRegion != clan.baseRegion" check: BuildWall (line 1613) returns `ERR_NOT_AT_HOMEBASE`, UpgradeBase (line 1619) returns `ERR_NOT_AT_HOMEBASE`, but DepositResources (line 1689, changed in this PR) returns `ERR_INVALID_REGION`. `ERR_NOT_AT_HOMEBASE` enum value is STILL defined — not an enum cleanup. UI/clients lose deposit-specific feedback. **Fix:** revert `_doDeposit`'s submit-validation back to `ERR_NOT_AT_HOMEBASE`, OR migrate all three actions in one pass and remove the enum entry. Half-migration is not acceptable. |

## SHOULD FIX (MEDIUM)

| # | Models | Finding |
|---|---|---|
| M1 | Opus 4.6 + 4.7 = 2/5 | **`CLANSMAN_CARRY_CAP` naming misleads.** Named as per-clansman limit but only enforced in `_gatherWood`. Iron/wheat/fish keep their own caps. A clansman can carry 10+5+40+8 = 63e18 total. Either rename to `WOOD_CARRY_CAP` OR implement true aggregate cap. As-is = naming trap for future contributors. |
| M2 | Codex 5.5 = 1/5 | **Wood economy spec drift** — planning specs define wood cap=15e18, base=2e18, crit=20%/+1e18, total=2e18 or 3e18. PR changes cap=10e18, base=1e18*duration, crit=10% / 2× multiplier. Tests encode new behavior but no spec update or rationale. |
| M3 | Opus 4.6 = 1/5 | **Backward-compat constants are semantically misleading.** `WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK` (1e18) for backward-compat, but crit logic is now multiplicative (`yield *= 2`) not additive (`yield += WOOD_CRIT_BONUS`). External consumers reading `WOOD_CRIT_BONUS` expecting additive will be wrong. Add `@deprecated` natspec or remove. |
| M4 | Opus 4.7 = 1/5 | **Crit-distribution test ~0.4% false-fail rate.** Binomial(100, 0.10) → P(X<3)≈0.0019 + P(X>20)≈0.0024. CI flake risk. Tighten with deterministic seed batch OR widen bounds to [1, 25]. |
| M5 | Opus 4.7 = 1/5 | **Single-gather can fill 80% of cap on crit; clamp path untested.** Crit yields 8e18, cap 10e18 — consecutive gathers in same mission rarely add full yield. Add fuzz test for clamp path. |
| M6 | Codex 5.3 + 5.4 = 2/5 | **Deposit event tick coverage narrow** (only no-travel, early tick). Add regression for travel + heartbeat-settle + later tick. |

## DEFER (LOW)

- Hardcoded tick=1 in event-emission test (4/5 noted)
- RNG variable naming inconsistency
- Test harness duplication (ClanWorldTestHarness vs GatheringHarness)
- `forge-lint: disable-next-line(unsafe-typecast)` smell
- Empty deposits test coverage tightening

## Per-model verdicts

- **Codex 5.3:** NEEDS_FIXES — 1 HIGH (ABI stale) + 2 MED + 2 LOW. Conservative.
- **Codex 5.4:** NEEDS_FIXES — 2 HIGH (gathering wood-only + ABI stale) + 2 MED + 2 LOW. Caught the wood-only batch yield issue uniquely.
- **Codex 5.5:** NEEDS_FIXES — 1 HIGH (ABI stale) + 2 MED (wood economy spec drift, ResourcesDeposited inconsistency) + 1 LOW.
- **Opus 4.6:** NEEDS_FIXES MEDIUM-only — 0 HIGH + 1 MED (CLANSMAN_CARRY_CAP naming) + 5 LOW. Most lenient on severity grading.
- **Opus 4.7:** NEEDS_FIXES — 2 HIGH (ABI lone uint32 + ERR_INVALID_REGION) + 4 MED + 5 LOW. Most thorough; identified anti-pattern history.
- **Gemini Pro:** ✗ failed — 429 capacity exhausted, no recovery. Not counted in tally.

## Cross-model overlap stats

- 5/5: H1 ABI uint32/uint64 inconsistency
- 4/5: H3 ERR_INVALID_REGION inconsistency (1 HIGH, 3 LOW/MED)
- 3/5: H2 wood-only batch yield (1 HIGH, 2 MED)
- 2/5: M1 CLANSMAN_CARRY_CAP naming, M6 deposit event tick coverage
- 1/5: various single-model M/L findings

## Decision

**Recommend Path A R1 fix-round.** Scope:

1. **H1 ABI uint32 → uint64 revert** — match Phase 8 R2 + Phase 10 R2 pattern, regenerate ABI, add CI parity check (anti-pattern keeps recurring; the parity check is the durable fix)
2. **H2 gathering economy** — Liam decision: migrate iron/wheat/fish to per-tick yield IN this fix-round, OR file Phase 5B follow-ups + add deferral comment in code
3. **H3 ERR_NOT_AT_HOMEBASE revert** — easiest one, match the other 2 validators
4. **M1 CLANSMAN_CARRY_CAP rename** to `WOOD_CARRY_CAP` (or document as scope marker)

Total fix-round budget: ~30-45 min codex.

**Liam decision needed for H2:** migrate other 3 resources now, or file 5B follow-ups?

**Cross-phase observation:** the uint32 ABI regression has now been caught + reverted on Phase 8, Phase 10, AND Phase 5. **Strongly recommend** adding a permanent CI guard (`pnpm test` pre-step) that diffs `out/IClanWorld.sol/IClanWorld.json` against `packages/contracts/abi/IClanWorld.json` and fails on drift — this stops the anti-pattern at source.
