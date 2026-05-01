# Phase Super-Swarm Synthesis R2 — PR #194 (head 218f902)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.7 ✓ (partial) | Gemini 3.1 Pro ✗ (429 rate limit) | Opus 4.6 (skipped, weak signal R1)
**Phase:** dev-phase-9-bandits — Bandit System
**Diff size:** 5150 lines (post-r2 fix-round)

## Summary

**Verdict: All 5 R1 HIGH findings confirmed FIXED. 1 new HIGH found (single-model, lower confidence). Per Liam directive 2026-04-29 13:35 ET: Phase 9 ships at this R2 result.**

R1 HIGH fixes verified landed:
- ✅ Camped→Attacking dispatch wired into heartbeat
- ✅ TS ABIs aligned (runner + shared) via generated ABI
- ✅ Winter starvation replay uses replayed-tick winter status
- ✅ Blueprint reward = 1e18 (matches monument cost)
- ✅ `_resolveBanditAttack` revert race fixed (returns safely on target-death)

**Single-model new HIGH (Codex 5.4 only):** `_pickBanditAttackTarget` ranks competing targets from RAW unsettled clan state. Eager-settle only covers spawn-candidate regions, skipped at global bandit cap. Multi-clan target selection can pick the wrong clan based on stale loot/liveness in production.

This concern was hinted at in R1 (Opus 4.6's H1 + Gemini Pro's eager-settle-cap-bypass MED). Real but bounded; tests don't cover competitive-selection because the new heartbeat E2E test only exercises a single clan.

## MUST FIX

**None at the R1-fixes level — all 5 confirmed clean.**

## SHOULD FIX (follow-up, not-blocking per Liam directive)

| File:line | Models | Severity | Finding |
|---|---|---|---|
| `ClanWorld.sol:1703, 2156, 2305` | 5.4 = 1/3 | HIGH (single-model) | **Stale-state competitive target selection.** `_pickBanditAttackTarget` reads raw `_clans` storage without settling competing clans first. With multiple clans in a region + bandit at cap, target choice diverges from actual loot. **Action:** file as follow-up issue, address before Submission 2 demo. |

## Per-model verdicts

- **Codex 5.4:** NEEDS_FIXES — 1 new HIGH (target selection) + multiple MED (likely defender list mutation, cost balance, etc.)
- **Codex 5.5:** NEEDS_FIXES — HIGH CLEAN, 2 MED, 1 LOW (declared "no new HIGH found")
- **Opus 4.7:** confirmed HIGH#4+#5 fixed; review file truncated before full cross-cutting (model context exhaustion)
- **Gemini 3.1 Pro:** failed — 429 RESOURCE_EXHAUSTED on quota
- **Opus 4.6:** intentionally skipped (weakest R1 signal + token budget)

## Cross-model overlap stats

- R1 HIGHs verified fixed: **5/5** (cross-model agreement: Codex 5.4 + 5.5 + partial Opus 4.7 = 3 confirmations)
- New HIGH findings: 1 (single-model only — Codex 5.4 sees it, Codex 5.5 explicitly says HIGH CLEAN)
- New MED findings: 2-3 (Codex 5.5 surfaces some)

## Decision

Per Liam directive 13:35 ET: "No need for more review cycles on 9 after this one." Phase 9 release PR #194 ready for Liam UAT.

**Single-model HIGH filed as follow-up issue rather than blocking R3** — competitive-selection bug is a real concern but localized to a specific multi-clan attack scenario; current tests don't exercise it but the fix is well-scoped.

## Action items

- [ ] File issue: "Phase 9 follow-up — `_pickBanditAttackTarget` reads stale clan state" — non-blocking
- [ ] PR #194 marked ready for Liam UAT
- [ ] Move to Phase 6 super-swarm next per dispatch order
