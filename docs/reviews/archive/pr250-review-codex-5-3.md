# PR #250 Review — Phase 10 Winter + Elimination

- PR: https://github.com/OmniPass-world/clan-world/pull/250
- Base/Head: `dev` <- `dev-phase-10-winter`
- Review date (ET): 2026-04-30
- Reviewer model: `codex-5-3`
- Review mode: Blind independent swarm in isolated linked worktree (`review-pr-250-blind-r2`)
- Constraint honored: swarm agents were explicitly instructed not to read `docs/reviews/*`

## Summary Stats

- Total findings triaged: **8**
- **MUST FIX:** 0
- **SHOULD FIX:** 2
- **DEFER (tracked issue):** 2
- **SKIP / FALSE POSITIVE:** 4
- Overall PR health: **No merge-blocking defect found; should-fix follow-up recommended before relying on new review surfaces**

## Validation Checkpoints Run

- `pnpm --filter @clan-world/shared test` ✅ (`getClanFullView Mission ABI parity OK`)
- `forge test` in `packages/contracts` ✅ (`106 passed, 0 failed`)
- `pnpm --filter ... typecheck` for shared/runner/orchestrator ❌ blocked in linked worktree (`node_modules` missing / `tsc: not found`)

## Triage Table

| # | File | Line / Symbol | Finding | Category |
|---|---|---|---|---|
| 1 | `packages/shared/src/adapters/IChainClient.ts`, `apps/orchestrator/src/main.ts` | `submitOrders` (`387-405`), orchestrator usage (`23-34`) | `submitOrders` now returns `results` from `simulateContract` and orchestrator logs them as transaction outcomes. These are pre-send simulation outputs, so they can drift from finalized on-chain execution in edge race conditions. | **SHOULD FIX** |
| 2 | `packages/shared/scripts/check-chain-abi-parity.mjs` | `extractHardcodedMissionShapes` + `canonicalMission` check (`38-83`) | New parity guard validates only the two `activeMission` tuple shapes under `getClanFullView`; it does not cover other hardcoded ABI fragments (`getWorldSnapshot`, `submitClanOrders`, runner `getWorldState` tuple). This can create false confidence for future ABI edits. | **SHOULD FIX** |
| 3 | `packages/shared/src/adapters/IChainClient.ts` | `StubChainClient.submitOrders` (`264-266`) | Stub always returns `results: []`, so local/stub flows do not exercise per-order status handling introduced in PR #250 and can look cleaner than production semantics. | **DEFER** → [#309](https://github.com/OmniPass-world/clan-world/issues/309) |
| 4 | `packages/contracts/src/ClanWorld.sol` | `mintClan` guards (`1257-1258`) | Dual cap checks are currently redundant (`< 12` and `< MAX_CLANS_FOR_CROP_TRANSITIONS` where derived cap is 24). Behavior is fine today, but this is drift-prone and confusing for future edits. | **DEFER** → [#310](https://github.com/OmniPass-world/clan-world/issues/310) |
| 5 | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/test/ClanWorld.t.sol` | starvation gate (`457-460`), test (`2406-2424`) | “Off-by-one starvation kill” was flagged, but PR tests explicitly codify one-tick delayed first death (`test_starvation_kill_deferred_to_next_tick`). This appears intentional, not a defect. | **SKIP / FALSE POSITIVE** |
| 6 | `packages/contracts/src/ClanWorld.sol` | cold reset sites (`407-420`) | Duplicate `coldDamage = 0` reset sites were flagged. Current logic is idempotent and used at two winter-boundary points; no concrete behavioral break reproduced in tests. | **SKIP / FALSE POSITIVE** |
| 7 | `packages/runner/src/runnerCastHeartbeat.ts` | `HEARTBEAT_ABI` tuple additions (`39-45`) | “Runner local clock vs chain timestamp” concern is valid in general but not introduced by this PR. PR #250 change in runner is ABI tuple alignment only. | **SKIP / FALSE POSITIVE** |
| 8 | `packages/contracts/src/ClanWorld.sol` | `_executeMarketBuy` (unchanged in PR diff) | Quote-vs-actual market-buy slippage concern was raised by one lane reviewer, but this function was not modified in PR #250 and is out of scope for this diff triage pass. | **SKIP / FALSE POSITIVE** |

## Recommended Next Steps

1. **Address SHOULD FIX items before treating new outputs as authoritative**
   - Clarify `submitOrders` semantics (`simulatedResults` vs finalized results), or fetch/derive post-confirmation outcomes before logging as authoritative.
   - Expand ABI parity checking beyond `activeMission` to at least include hardcoded `getWorldSnapshot`, `submitClanOrders`, and runner `getWorldState`.
2. **Track deferred cleanup via created issues**
   - [#309](https://github.com/OmniPass-world/clan-world/issues/309)
   - [#310](https://github.com/OmniPass-world/clan-world/issues/310)
3. **If you want full local confidence in this linked worktree**
   - Install JS dependencies in the worktree and rerun package `typecheck` jobs that were blocked by missing `node_modules`.
4. **Merge readiness**
   - Current assessment: **mergeable with follow-up work**, with no MUST FIX blocker found in this blind pass.
