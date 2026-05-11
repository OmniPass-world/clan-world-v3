# PR #396 Review — Codex GPT-5

Reviewed PR: #396, `dev-merge` into `dev`  
Base: `46a61a65242b4ae0f9b87ac248d7cb66b6bc388d`  
Head: `784b4bb9b6c5a498d8be245c9449a472951743c4`  
Worktree: `/home/claude/code/omnipass-world/clan-world-pr396-review`

Existing review artifacts under `docs/reviews/**` were excluded from inspection.

## Validation

- `pnpm install --frozen-lockfile` — passed.
- `pnpm --filter @clan-world/contracts test` — passed, 292 tests.
- `pnpm --filter @clan-world/shared test` — passed, 20 tests.
- `pnpm --filter @clan-world/agents test` — passed, 37 tests.
- `pnpm --filter @clan-world/runner test` — passed, 121 passed / 1 skipped.
- `pnpm turbo typecheck` — passed, 11 tasks.
- `pnpm test:chainclient-abi` — passed.
- `pnpm --filter @clan-world/web build` — passed.
- `pnpm exec playwright test --config apps/web/playwright.config.ts --list` — listed 14 tests; full e2e not run.

## Must Fix

1. Runner heartbeat ABI cannot decode `getWorldState`.
   - Source: `packages/runner/src/runnerCastHeartbeat.ts:53`
   - The hand-written runner ABI appends duplicate `currentSeasonNumber` and `nextHeartbeatAtTick` fields, making a 17-field tuple while canonical `WorldState` has 15 fields in `packages/contracts/src/IClanWorld.sol:202`. Viem decode against the canonical ABI fails with `PositionOutOfBoundsError`, so `isHeartbeatDue()` and rate-limit recovery can fail before the runner can call heartbeat.
   - Fix: remove the duplicate fields or derive this minimal ABI fragment from the canonical ABI.

2. Reserved upgrade resources can be drained before settlement.
   - Sources: `packages/contracts/src/ClanWorld.sol:1077`, `packages/contracts/src/ClanWorld.sol:3790`, `packages/contracts/src/ClanWorld.sol:4656`, `packages/contracts/src/ClanWorld.sol:4696`, `packages/contracts/src/ClanWorld.sol:4732`, `packages/contracts/src/ClanWorld.sol:2372`
   - Upgrade resources are held in `_reserved*ByClan`, but withdraws, direct vault transfers, blueprint transfers, bundles, and bandit theft use raw vault/blueprint balances. A clan can reserve an upgrade, move the committed resources away, then leave the upgrade stuck retrying or violate the reservation invariant.
   - Fix: route all player-facing vault/blueprint debits through reservation-aware spendable helpers. Explicitly decide whether bandits can steal reserved materials and test that rule.

3. Active bandit target selection uses stale raw clan state.
   - Sources: `packages/contracts/src/ClanWorld.sol:2761`, `packages/contracts/src/ClanWorld.sol:2117`, `packages/contracts/src/ClanWorld.sol:2200`, `packages/contracts/src/IClanWorld.sol:853`
   - `_eagerSettleForBandits` returns when a bandit is already active, then `_pickBanditAttackTarget` ranks raw `_clans` / `_lootValueRaw`. The selected clan is settled only after selection, so a stale, doomed, high-loot clan can absorb the target decision and cause escape/rest instead of attacking the best live target.
   - Fix: settle candidate target clans before ranking, or retarget/count an attempt when the selected target dies or remains stale.

4. Real `submitOrders` drops new mission fields and `marketMode`.
   - Sources: `packages/shared/src/adapters/IChainClient.ts:2351`, `packages/shared/src/adapters/IChainClient.ts:2428`, `packages/contracts/src/IClanWorld.sol:398`, `packages/contracts/src/IClanWorld.sol:411`
   - The shared real client hardcodes `targetClanId`, `marketToken`, `marketAmount`, and `maxGoldIn` to zero/null for every mission order, so real DefendBase target and market orders cannot be submitted through the adapter. It also omits `marketMode` from `SubmitOrderResult`, hiding immediate/scheduled semantics from callers.
   - Fix: map the full `ClanOrder` contract tuple from payload fields, return `marketMode`, and add one minimal TS test for market or DefendBase mapping.

## Should Fix

1. Derived/read APIs do not simulate `WithdrawResources`.
   - Source: `packages/contracts/src/ClanWorld.sol:1529`
   - Storage settlement resolves withdraws, but `_simulateResolveAction` has no withdraw branch. `getClanFullView`, `getDerivedClanState`, and rankings can show stale vault/carry and active mission state.

2. Bandit preview getters do not preview.
   - Sources: `packages/contracts/src/ClanWorld.sol:5035`, `packages/contracts/src/ClanWorld.sol:5370`
   - `getBanditTargetPreview` and `getActiveBanditView` return stored `targetClanId` and hardcoded loot `0`, so camped/spawned bandits show no projected target despite the interface promise of a preview.

3. Stub contract diverges from real behavior.
   - Sources: `packages/contracts/src/ClanWorldStub.sol:166`, `packages/contracts/src/ClanWorldStub.sol:222`, `packages/contracts/src/ClanWorldStub.sol:453`, `packages/contracts/src/ClanWorldStub.sol:495`, `packages/contracts/src/ClanWorldStub.sol:513`
   - Stub transfers allow zero/same-clan/empty-bundle flows that real `ClanWorld` rejects, and derived/full/snapshot views ignore minted clan state.

4. Root env template is missing active web and runner vars.
   - Source: `.env.template:45`
   - Repo-root env is the Vite `envDir`, but the root template omits `VITE_CONVEX_URL`, `VITE_WORLD_RP_ID`, and `VITE_CLANWORLD_DEMO_MODE`. It also omits `RUNNER_PRIVATE_KEY`, which `runnerCastHeartbeat` requires.

5. ABI/test/CI guards are too fragmented.
   - Sources: `.github/workflows/chainclient-abi.yml:21`, `packages/shared/src/adapters/check-chain-abi-parity.test.ts:83`, `packages/contracts/package.json:8`
   - GitHub Actions checks generated `IChainClient.ts` against the committed ABI, but not committed ABI against Solidity. The Vitest ABI parity test is mostly fixture-vs-fixture despite `CLAN_WORLD_ABI` now being exported.

6. `@clan-world/shared` gained an unnecessary runtime dependency.
   - Source: `packages/shared/package.json:24`
   - `@clan-world/contracts` is not imported in shared runtime code. If it exists only for Turbo ordering, move it to `devDependencies` or make ordering explicit elsewhere.

7. Market sells have no slippage floor.
   - Sources: `packages/contracts/src/ClanWorld.sol:3912`, `packages/contracts/src/ClanWorld.sol:4060`
   - Sell execution uses `minOut = 0`, so FIFO ordering or front-running can force bad fills. Add a sell-side bound such as `minGoldOut`.

8. Scheduled market queues can grow with stale entries.
   - Sources: `packages/contracts/src/ClanWorld.sol:3472`, `packages/contracts/src/ClanWorld.sol:3518`, `packages/contracts/src/ClanWorld.sol:3605`
   - Queues are append-only until heartbeat and sorted O(n^2). Delayed heartbeats plus repeated retasks can accumulate stale entries.

9. World App guard lacks positive coverage.
   - Sources: `apps/web/src/config/env.ts:38`, `apps/web/src/App.tsx:100`, `apps/web/tests/e2e/02-demo-mode.spec.ts:43`
   - Existing e2e coverage runs with the guard bypassed. Add one guard-enabled smoke test.

## Defer / New Issue

1. `uintValue(-1n)` is accepted.
   - Source: `packages/shared/src/adapters/IChainClient.ts:25`
   - JSON paths use strings/numbers today, but bigint validation should eventually reject negatives.

2. RNG is predictable during the order window.
   - Source: `packages/contracts/src/ClanWorld.sol:2937`
   - Current tick seed is known before spawn/combat resolution. Track commit-reveal/VRF-style randomness later if adversarial play matters.

3. Market events became less filterable.
   - Source: `packages/contracts/src/IClanWorld.sol:602`
   - Event fields lost some indexing/queue metadata. Not a blocker, but indexer ergonomics are weaker.

4. `apps/server/.env.template` still advertises `MOCK_MODE`.
   - Source: `apps/server/.env.template:4`
   - Pre-existing drift from the one-env-var rule; should become `CLAN_WORLD_USE_STUB_CHAIN`.

5. Playwright falls back to a hardcoded port when `port-for` has no lock.
   - Source: `apps/web/playwright.config.ts:7`
   - In this worktree `port-for` warned that `.world/ports.lock` was missing. Track separately unless e2e binding failures appear.

## Skip / False Positive

- No ABI drift in the committed `IChainClient` fragment: `pnpm test:chainclient-abi` passed.
- No new `process.env` usage in `packages/shared`; changed code uses `readEnv`.
- Web dev script removal of `VITE_DEMO_BYPASS_WORLD_GUARD=true` is okay with the inverted `VITE_REQUIRE_WORLD_APP_GUARD` default.
- `convex codegen --typecheck disable` appears valid for the current Convex CLI.
- Pool reentrancy paths look guarded by `nonReentrant` plus `msg.sender == address(this)` wrappers, and the reentrancy test passes.
- The full Forge suite passes, so the issues above are semantic gaps, not current test failures.

## Recommended Next Steps

1. Fix the runner `getWorldState` ABI first; it is small and can break heartbeat operation immediately.
2. Fix reservation-aware debits across withdraws/transfers/bandit theft, then add one public-surface invariant test.
3. Fix active bandit target selection to use settled candidate state or retarget stale/dead selections.
4. Update `RealChainClient.submitOrders` mapping and return `marketMode`, with one focused TS test.
5. Batch the should-fix tooling/env/stub cleanup into a follow-up PR if hackathon timing is tight.
