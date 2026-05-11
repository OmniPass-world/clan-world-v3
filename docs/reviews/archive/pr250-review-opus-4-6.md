# PR #250 Blind Swarm Review

- PR: [Phase 10 — Winter + Elimination](https://github.com/OmniPass-world/clan-world/pull/250)
- Branch: `dev-phase-10-winter` -> `dev`
- Review date: 2026-04-30 01:10:33 EDT
- Model: Codex 5.3 (multi-agent swarm)
- Method: blind independent swarm (7 parallel Wave 1 agents + 3 Wave 2 deep-dive agents), no `docs/reviews/*` inputs used by swarm agents

## Triage Table

| # | File | Line | Finding | Category |
|---|---|---:|---|---|
| 1 | `apps/orchestrator/src/main.ts` | 18 | `gotoRegion: 0` is `REGION_NOOP`, not Forest. For `ChopWood`, this only works when the clansman is already in Forest; otherwise `_validateAction` returns `ERR_INVALID_REGION`. | **SHOULD FIX** |
| 2 | `packages/contracts/src/ClanWorld.sol` | 445-447, 614-616 | Starvation semantics appear inconsistent with spec wording: starvation is recorded at current `tick` and `_isStarving` is keyed on `_world.currentTick`, while spec says starvation begins on the next tick. | **SHOULD FIX** |
| 3 | `packages/shared/scripts/check-chain-abi-parity.mjs` | 66-83 | New parity checker validates only `activeMission` tuple shape inside `getClanFullView`; `packages/shared/package.json` wires this as `test`, which can imply broader ABI parity than is actually checked. | **SHOULD FIX** |
| 4 | `packages/contracts/src/ClanWorld.sol` | 1685-1692, 1698-1707 | Scheduled market overflow appends then insertion-sorts the merged queue. This can become high-cost under sustained backlog and is a liveness risk, but this logic predates PR #250. | **DEFER** (new issue) |
| 5 | `packages/contracts/src/ClanWorldStub.sol` | 77-82 | Stub heartbeat has no rate-limit `require` and sets `nextHeartbeatAtTs` to `block.timestamp`, diverging from production cadence semantics. Useful to track, but not introduced by this PR. | **DEFER** (new issue) |
| 6 | `packages/contracts/test/ClanWorld.t.sol` | n/a | No direct assertion found for first-starvation-tick gather penalty behavior vs spec “next tick” semantics. Add a focused test case to lock intended behavior. | **DEFER** (new issue) |
| 7 | `packages/shared/src/adapters/IChainClient.ts` | 36-258 | Claim of major ABI drift is not substantiated for exercised selectors. Current adapter uses a deliberate minimal ABI subset + projection, and tuple shapes for changed paths are aligned. | **SKIP / FALSE POSITIVE** |
| 8 | `packages/contracts/src/ClanWorld.sol` | 407-409, 418-420 | Duplicate winter-exit `coldDamage = 0` writes are redundant but idempotent; no functional regression demonstrated. | **SKIP / FALSE POSITIVE** |
| 9 | `apps/orchestrator/src/main.ts` | 18 | “`gotoRegion: 0` always fails” is overstated. It succeeds for clan-1 defaults because clan-1 spawns in Forest; this is a robustness issue, not universal failure. | **SKIP / FALSE POSITIVE** |
| 10 | `packages/contracts/src/ClanWorldStub.sol` | multiple | “Stub must fully match production behavior to merge” is too strict for current architecture; stub intentionally implements interface shape with reduced behavior. | **SKIP / FALSE POSITIVE** |

## Summary Stats

- Total findings triaged: **10**
- **MUST FIX:** 0
- **SHOULD FIX:** 3
- **DEFER:** 3
- **SKIP / FALSE POSITIVE:** 4

## Recommended Next Steps

1. Address the three **SHOULD FIX** items in a focused follow-up (or amend this PR if still open for edits):
   - Make orchestrator region explicit (`REGION_FOREST`) instead of `0` for ChopWood demo flow.
   - Resolve starvation timing semantics (code or spec wording) so behavior is unambiguous.
   - Expand/rename ABI parity checks so `test` coverage expectation matches reality.
2. Open three follow-up issues for deferred items:
   - Market queue backlog/sort liveness hardening.
   - Stub cadence semantics alignment decision (parity vs intentionally loose stub).
   - Starvation gather timing test coverage gap.
3. Merge readiness for PR #250 itself: **needs-work** (non-blocking but meaningful correctness/maintainability issues remain).

