# PR #396 — Dev merge — Review (gpt-5-2-cursor)

- **PR**: `https://github.com/OmniPass-world/clan-world/pull/396`
- **Base → Head**: `dev` → `dev-merge`
- **Reviewed at**: 2026-05-01
- **Method**: isolated linked worktree review + local commands; **no code changes** made during review

## Scope snapshot

- **Commits**: 88
- **Files changed**: 68
- **Additions / deletions**: +12,583 / -1,151
- **Touched areas**: contracts engine + Foundry tests, runner heartbeat/tick loop, shared adapters + ABI parity tooling, web app + Playwright e2e, server tooling/docs, env + CI.

## Checks run (local)

- **`pnpm install --frozen-lockfile`**: ✅
- **`pnpm lint`**: ✅ (most packages still stub lint)
- **`pnpm typecheck`**: ✅
- **`pnpm test`**: ✅ (Vitest suites + Foundry tests ran; 292 Solidity tests passed)
- **`pnpm build`**: ✅ (web + landing Vite builds succeeded)
- **`pnpm test:chainclient-abi`**: ✅
- **`pnpm test:e2e`**: ❌ (2 failures; details below)

## Triage table

| # | File | Line | Finding | Category |
|---|------|------|---------|----------|
| 1 | `packages/runner/src/runnerCastHeartbeat.ts` | 38-57 | `getWorldState` ABI tuple **duplicates** `currentSeasonNumber` + `nextHeartbeatAtTick`, making the minimal ABI **not match** the canonical `WorldState` and risking decode/offset bugs in viem reads. | **MUST FIX** |
| 2 | `apps/server/convex/verify.ts` | 45-55 | World ID verify stores the nullifier **before** successful upstream verification. If the first verification fails, later requests return `{success: true, cached: true}` and can **bypass verification**. (Note: appears **pre-existing on `dev`**, not introduced by this PR.) | **MUST FIX** |
| 3 | `apps/web/playwright.config.ts` + `apps/web/src/main.tsx` + `apps/web/tests/e2e/00-smoke.spec.ts` | 64-67 / 68-98 / 46-48 | `pnpm test:e2e` fails on a fresh environment because the spawned dev server has no `VITE_CONVEX_URL`, so `/` renders a degraded fallback with **no canvas**, but the smoke test requires a visible `<canvas>`. | **MUST FIX** |
| 4 | `apps/web/tests/e2e/02-demo-mode.spec.ts` | 42-55 | `getByText('IG')` is too ambiguous and can false-pass by matching the substring `ig` inside “conf**ig**ured” on the degraded fallback screen. Prefer `exact: true` or a dedicated `data-testid`. | **MUST FIX** |
| 5 | `packages/contracts/src/ClanWorld.sol` | 3516-3624 | Scheduled market execution processes the **entire tick queue** (`_executeScheduledMarketActions`) and sorts via insertion sort \(O(n^2)\) with external calls inside the loop. This is a griefable **gas / liveness** risk for `heartbeat()`. | **SHOULD FIX** |
| 6 | `packages/shared/src/adapters/IChainClient.ts` | 2373-2409 | Env/key provisioning violates “one env var per concept”: `ELDER_WALLET_KEY_PATH` is primary but it falls back to `DEPLOYER_PRIVATE_KEY` with a “deprecated” warning (shim behavior + confusing ops). | **SHOULD FIX** |
| 7 | `packages/runner/src/runnerCastHeartbeat.ts` | 11, 114-123 | Runner heartbeat is hardwired to `baseSepolia`. Repo supports multiple submission realms; this should be env-driven (and match `.env.template`’s `CHAIN`, or delete `CHAIN` if unused). | **SHOULD FIX** |
| 8 | `.github/workflows/chainclient-abi.yml` | 15-22 | CI guards only `pnpm test:chainclient-abi`; Actions are pinned to moving tags (`@v4`). Broader merge safety (typecheck/tests/forge/e2e) isn’t enforced by workflow. | **SHOULD FIX** |
| 9 | `.env.template` | 14-22, 57-101 | Template drift: documents `DEPLOYER_PRIVATE_KEY` but not `RUNNER_PRIVATE_KEY` / `ELDER_WALLET_KEY_PATH`; includes a secret-looking placeholder (`CLAUDE_CODE_OAUTH_TOKEN=sk-ant-xxx`); includes `CHAIN=...` which appears unused in code paths reviewed. | **SHOULD FIX** |
| 10 | `apps/server/package.json` | 9 | `convex codegen --typecheck disable` may not be a valid `convex@1.17.4` flag combination (risk: broken local `pnpm --filter @clan-world/server codegen`). | **DEFER** → [#402](https://github.com/OmniPass-world/clan-world/issues/402) |
| 11 | `packages/runner/src/zeroGMemoryStore.ts` | 204-256 | `ZeroGMemoryStore.save()` creates a **fresh** 0G `Batcher` per save. Probably fine behind `OG_STORAGE_API_KEY`, but could become a perf bottleneck once enabled. | **DEFER** → [#403](https://github.com/OmniPass-world/clan-world/issues/403) |
| 12 | `packages/runner/src/composeSituationBlock.ts` + `packages/runner/src/pollChainTick.ts` | (see note) | Per-tick repeated snapshot reads and unbounded compose timeouts can turn into a scalability / liveness issue under slow Convex or peer inboxes. | **DEFER** → [#404](https://github.com/OmniPass-world/clan-world/issues/404) |
| 13 | `apps/web/src/App.tsx` | (varies) | Post-IDKit verify path mostly logs to console on failure; missing user-visible error/loading state. Not a merge blocker, but poor UX for the primary CTA. | **DEFER** → [#405](https://github.com/OmniPass-world/clan-world/issues/405) |

## MUST FIX — details

### 1) RunnerCastHeartbeat minimal ABI mismatch

In `packages/runner/src/runnerCastHeartbeat.ts`, the minimal ABI includes duplicate fields:

- Duplicates at:
  - `currentSeasonNumber` (already present at line 43)
  - `nextHeartbeatAtTick` (already present at line 44)

This tuple must match `WorldState` in `packages/contracts/src/IClanWorld.sol` exactly (15 fields, ending with `nextCommitSequence`).

### 2) World ID verify idempotency is implemented as “first hit wins”

`apps/server/convex/verify.ts` inserts into `verifiedNullifiers` before verifying with Worldcoin:

- If the upstream call fails, the nullifier remains inserted
- Subsequent calls with the same nullifier return cached “success” without verification

Even though this doesn’t look introduced by PR #396, it’s severe enough to fix immediately.

### 3–4) E2E suite is not runnable as-is (and contains a false-positive assertion)

Observed `pnpm test:e2e` failures:

- `apps/web/tests/e2e/00-smoke.spec.ts` times out waiting for `<canvas>` because `/` renders the “Backend not configured…” fallback when `VITE_CONVEX_URL` is missing (`apps/web/src/main.tsx`).
- `apps/web/tests/e2e/02-demo-mode.spec.ts` uses `getByText('IG')` which can match “conf**ig**ured” on the fallback screen, yielding a false pass.

## SHOULD FIX — themes

- **Heartbeat + chain config**: remove ABI duplication; make chain selection env-driven in runner and shared chain client; keep `.env.template` aligned.
- **Contract liveness**: add a cap/chunking strategy for scheduled market execution so a single tick can’t OOG the keeper/heartbeat.
- **CI coverage**: add at least `pnpm typecheck` + `pnpm test` (and optionally `forge test` + `pnpm test:e2e`) to merge-gate workflows; pin third-party actions to SHAs.

## SKIP / false positives

- “Breaking ABI” concerns are largely mitigated by the existing ABI parity checks and the fact that `pnpm test:chainclient-abi` passed locally and in PR checks.

## DEFER issues filed

- [#402](https://github.com/OmniPass-world/clan-world/issues/402): server convex codegen flags validation
- [#403](https://github.com/OmniPass-world/clan-world/issues/403): runner 0G batcher reuse
- [#404](https://github.com/OmniPass-world/clan-world/issues/404): runner per-tick snapshot reuse + compose timeouts
- [#405](https://github.com/OmniPass-world/clan-world/issues/405): web World ID verify UX/loading/error states

## Recommended next steps (ordered)

1. **Fix `RunnerCastHeartbeat` ABI tuple** to match `WorldState` exactly; add a tiny unit test that asserts ABI field count/order against a single source of truth.
2. **Fix World ID verify nullifier flow**: only persist nullifier after a successful upstream verification (or introduce a pending/verified state).
3. **Make `pnpm test:e2e` green by default**: ensure the webServer gets a sane `VITE_CONVEX_URL` for tests (or adjust smoke test expectations), and make the demo-mode assertion unambiguous.
4. **Add a market execution budget** (cap/chunk) or document keeper gas requirements explicitly; consider avoiding \(O(n^2)\) sorting on-chain.
5. **Env cleanup**: pick one private-key provisioning path (file vs env), delete deprecated shims, and update `.env.template` to list every read var.
6. **CI uplift**: expand the workflow coverage beyond ABI drift once merge velocity allows.
