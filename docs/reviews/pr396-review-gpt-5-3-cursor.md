# PR #396 — Dev Merge Review

| Field | Value |
|-------|-------|
| **PR** | [#396 "Dev merge"](https://github.com/OmniPass-world/clan-world/pull/396) |
| **Branch** | `dev-merge` → `dev` |
| **Date** | 2026-05-01 |
| **Reviewer** | GPT-5.3 Cursor (11-agent swarm) |
| **Scope** | 68 files, +12 583 / −1 151 lines, 88 commits |
| **Verdict** | **Needs Work** — 3 MUST FIX, 13 SHOULD FIX, 14 DEFER, 10 SKIP |

---

## Evidence Commands

| Command | Result |
|---------|--------|
| `pnpm test:chainclient-abi` | PASS — IChainClient ABI fragment is up to date |
| `pnpm turbo run typecheck --filter=@clan-world/shared --filter=@clan-world/runner --filter=@clan-world/web` | PASS — 6/6 tasks cached green |
| `pnpm install --frozen-lockfile` | PASS — lockfile consistent |

---

## Triage Table

### MUST FIX (3)

| # | File | Lines | Finding | Confidence |
|---|------|-------|---------|------------|
| M-1 | `packages/runner/src/runnerCastHeartbeat.ts` | 54–57 | **Runner WorldState ABI has 17 tuple components; on-chain struct has 15.** The trailing "appended fields" (`currentSeasonNumber`, `nextHeartbeatAtTick`) are duplicates already present at indices 4–5. viem will attempt to decode 17 fields from 15 fields of returndata — likely throws `DecodeError` at runtime, breaking `isHeartbeatDue()` and heartbeat rate-limit detection entirely. Fix: delete lines 54–57 (the 2 trailing components). | HIGH |
| M-2 | `docs/planning/clanworld_v4_spec.md` | §6.2, §6.4, §6.5, §6.15 | **v4 spec contradicts implementation on bandit mechanics.** Spec says bandits never spawn in Unicorn Town / Deep Sea; code has no region denylist. Spec uses uniform random spawn; code uses weighted selection. Spec defense threshold is `clansmanDefense >= banditAttack`; code uses `totalClansmanDefense >= banditAttackPower * 2`. Spec uses `CAMPING`; code uses `Camped`. Any agent or dev following the spec will produce wrong logic. | HIGH |
| M-3 | `docs/planning/phase-3-test-spec.md` | entire file | **Phase 3 test spec is severely stale.** References `BuildWall`, `RepairWall`, `getLeaderboard()`, `BanditState.Camping`, region exclusion tests — none match current `IClanWorld.sol` / `ClanWorld.sol`. If dispatched as-is, agents would implement wrong test scenarios. | HIGH |

### SHOULD FIX (13)

| # | File | Lines | Finding | Confidence |
|---|------|-------|---------|------------|
| S-1 | `packages/contracts/src/ClanWorld.sol` | ~503–530 | **Dead clan `lastSettledTick` set to `curTick` despite early break.** When `_applyUpkeep` kills a clan mid-loop, remaining ticks are never processed but `lastSettledTick` and `ClanSettled` event claim full settlement. Indexers/UI could misinterpret death timing. | HIGH |
| S-2 | `packages/contracts/src/ClanWorld.sol` | ~486–523 | **Multi-tick lazy catch-up modeling error.** Missions settled globally by heartbeat already modified clan resources before `_settleClan` walks per-tick upkeep on "today's" balances. Upkeep for tick `k` sees post-mission state of later ticks. Impact: starvation/death timing can be slightly off for lagging clans. | MEDIUM |
| S-3 | `packages/contracts/src/ClanWorldStub.sol` | 453–456 | **`getDerivedClanState(uint32)` ignores clanId parameter — always calls `getClan(0)`.** Multi-clan stub consumers get empty/wrong data. Also affects `getClanFullView`. | HIGH |
| S-4 | `packages/shared/src/adapters/IChainClient.ts` | ~26–28 | **`uintValue` does not reject negative bigints.** `uintValue(-5n)` silently returns `-5n`, which would encode as an underflowing uint on chain. Missing guard for `typeof bigint && value < 0n`. | HIGH |
| S-5 | `packages/shared/src/adapters/IChainClient.ts` | ~2192–2223 | **`SubmitOrderResult` omits `marketMode` field** returned by contract `OrderResult`. Callers cannot see market execution mode from the typed API. | HIGH |
| S-6 | `packages/contracts/src/ClanWorld.sol` | ~3096–3099 | **`finalizeSeason()` missing `nonReentrant` guard.** Currently a no-op stub, but when implemented, a malicious pool could call it during a swap frame. Add guard now to prevent future footgun. | HIGH |
| S-7 | `apps/web/playwright.config.ts` | 57–67 | **Playwright webServer does not set `VITE_CONVEX_URL`.** Fresh CI without `.env.local` renders "Backend not configured" fallback; demo-mode e2e tests fail looking for game UI elements. | HIGH |
| S-8 | `.env.template` | 37–52 | **Root template missing `VITE_CONVEX_URL`, `VITE_WORLD_RP_ID`, `VITE_CLANWORLD_DEMO_MODE`.** Anyone cloning from template alone misses web-required vars. | HIGH |
| S-9 | `packages/agents/` | `test/` + `src/__tests__/` | **Two CLI test suites coexist.** Both discovered by Vitest — duplicated maintenance, divergent coverage, risk of fixing bug in one and missing in the other. Consolidate to one location. | MEDIUM |
| S-10 | `README.md` | onboarding section | **README says `pnpm@10.28.1`; `packageManager` pins `pnpm@10.33.2`.** Misleads onboarding. | HIGH |
| S-11 | `docs/planning/DEMO_DRIFT.md` | heartbeat + orchestrator sections | **Stale references.** `heartbeat.ts` now has epoch-gating logic not described; orchestrator uses `CLAN_ID=1` not `clan-0`; `App.tsx` env handling moved to `config/env.ts`. | HIGH |
| S-12 | `apps/server/convex/README.md` | entire file | **Convex README is generic examples only.** Does not document actual module surface (`heartbeat.ts`, `crons.ts`, `getSnapshot.ts`, `mock.ts`). | HIGH |
| S-13 | `packages/shared/scripts/check-chain-abi-parity.mjs` | entire file | **Parity check only validates Mission tuple duplication** — not full ABI vs source. Name overstates what it guards. | HIGH |

### DEFER (14) — recommend new GH issues

| # | File | Finding | Proposed Issue Title |
|---|------|---------|---------------------|
| D-1 | `ClanWorld.sol` ~2200–2212 | Bandit attack bypass when target remains unsettled after capped `_settleClan` (>200 tick lag). Bandit transitions to `Resting` silently. | `fix(contracts): bandit attack should not skip targets only partially settled` |
| D-2 | `ClanWorld.sol` ~2943–2946 | Tick 0 mission RNG uses unset `_tickSeeds[0]` (bytes32(0)). | `fix(contracts): seed _tickSeeds[0] at deploy or first heartbeat` |
| D-3 | `ClanWorld.sol` ~4195–4246 | Winter locks wheat harvest but not market buy/sell. Confirm design intent. | `docs(contracts): clarify winter does not gate market actions (intentional?)` |
| D-4 | `ClanWorldStub.sol` ~108–126 | `mintClan` emits `baseRegion=0` (REGION_NOOP). Indexers assuming valid region on spawn may misbehave. | `fix(contracts): ClanWorldStub mintClan should emit valid baseRegion` |
| D-5 | `ClanWorld.sol` ~3293–3300 | Comment says cooldown may fall back to scheduled path; code rejects all orders during cooldown. Misleading. | `docs(contracts): fix cooldown/scheduled fallback comment` |
| D-6 | `IChainClient.ts` ~2354–2359 | Market/PvP fields hardcoded to zero in `submitOrders`. Must be updated when enabling market orders from TS. | `feat(shared): wire market/PvP fields in IChainClient submitOrders` |
| D-7 | `IChainClient.ts` ~2306–2312 | `getCurrentTick` uses `Number()` on uint64. Safe for realistic game ticks but theoretically lossy. | `fix(shared): use BigInt for getCurrentTick return type` |
| D-8 | `IChainClient.ts` ~2344–2348 | `withdrawResources` fallback reads full `o.payload`. Malformed payloads could set unintended withdraw amounts. | `fix(shared): explicit withdrawResources-only destructure in submitOrders` |
| D-9 | `ClanWorld.sol` ~4551–4563 | `seedPools` does not enforce cfg matches `INITIAL_*` constants. Malicious treasury owner could skew AMM pricing. | `fix(contracts): validate seedPools config against constants` |
| D-10 | `StubPool.sol` ~67–74 | `reserveA * reserveB` can overflow with astronomically large reserves (DoS vector in theory). | `fix(contracts): add SafeMath guard or cap check in StubPool` |
| D-11 | `ClanWorld.sol` ~3557–3597 | Empty `catch` in scheduled market execution masks all reverts as `ERR_LIQUIDITY_INSUFFICIENT`. Wrong event/status for off-chain indexers. | `fix(contracts): propagate revert reason in scheduled market failure events` |
| D-12 | `runnerCastHeartbeat.ts` ~11 | Chain hardcoded to `baseSepolia`. Misconfiguration risk if pointing at other realms. | `feat(runner): make chain configurable via env` |
| D-13 | `check-chain-abi-parity.test.ts` | Test only checks hardcoded function name list — does not load actual `IClanWorld.json`. Misleading guard. | `fix(shared): strengthen check-chain-abi-parity to compare actual ABI` |
| D-14 | `.github/workflows/chainclient-abi.yml` | CI validates TS↔JSON parity but not Solidity↔JSON. ABI can drift from source if `forge build` + regen not run. | `ci: add forge build + ABI regen verification step` |

### SKIP / FALSE POSITIVE (10)

| # | File | Finding | Rationale |
|---|------|---------|-----------|
| K-1 | `IClanWorld.sol` / `IClanWorld.json` | ABI artifact vs source mismatch | **Verified no mismatch.** Spot-checked all 48 functions, 46 events, struct field orders. Consistent. |
| K-2 | `scripts/gen-contract-abi.mjs` | Transformation risk | Script does safe `{ abi: artifact.abi }` extraction — no risky transforms. |
| K-3 | `Deploy.s.sol` ordering | Wrong deployment order | Verified: ClanWorld deployed before pools; `initTreasury` / `seedPools` use correct owner and matching constant seeds. |
| K-4 | `turbo.json` task graph | Incorrect dependencies | `typecheck → ^build` is correct; task ordering sound. |
| K-5 | `pnpm-lock.yaml` header | Suspicious version bumps | `lockfileVersion: 9.0`, packages consistent, no anomalies in header. |
| K-6 | `StubPool.sol` reserve semantics | Pool balance divergence from reserves | By design: ClanWorld uses internal carry/gold accounting, not ERC20 balance-backed swaps. Self-consistent. |
| K-7 | `MinimalERC20.sol` missing zero-address check | Token sent to address(0) | Solidity 0.8+ underflow protection sufficient for testnet boundary token. |
| K-8 | `ClanWorldConstants` not in ABI | Off-chain can't read constants | By design: constants are compile-time, documented in source/docs. |
| K-9 | `shared/package.json` `build` + `noEmit` | "build" doesn't emit artifacts | Intentional source-first consumption pattern; `main` → `src/index.ts`. |
| K-10 | `apps/web/package.json` no local `test:e2e` | Missing local script | Root `test:e2e` with web Playwright config is the monorepo pattern. |

---

## Summary Stats

| Category | Count |
|----------|-------|
| **MUST FIX** | 3 |
| **SHOULD FIX** | 13 |
| **DEFER** | 14 |
| **SKIP** | 10 |
| **Total Findings** | 40 |

---

## Convergent Findings (flagged by 2+ agents)

- **Runner WorldState ABI mismatch** (Agents 5, 4) — M-1
- **`uintValue` negative bigint gap** (Agents 4, 6) — S-4
- **Spec-to-implementation bandit drift** (Agents 1, 10) — M-2
- **Stub `getDerivedClanState` ignores clanId** (Agents 1, 6) — S-3
- **CI only validates TS↔JSON not Solidity↔JSON** (Agents 3, 8) — D-14
- **Dual CLI test suites** (Agents 6, 11) — S-9

---

## Recommended Next Steps

### Priority 1 — MUST FIX (blocks merge)

1. **M-1:** Delete the 2 trailing "appended fields" components from `HEARTBEAT_ABI` in `packages/runner/src/runnerCastHeartbeat.ts` (lines 54–57). Verify with `pnpm --filter @clan-world/runner test`.

2. **M-2 + M-3:** Decide whether to update the spec docs to match implementation, or tag them as superseded. Recommended: add a header banner `> SUPERSEDED by implementation — see IClanWorld.sol and ADR002/ADR003 for canonical mechanics` to both files. Do not delete (they have historical planning value).

### Priority 2 — SHOULD FIX (address in this PR)

Grouped by theme:

**Contract correctness (S-1, S-2, S-3, S-6):**
- S-3 is a one-line fix: `getClan(clanId)` instead of `getClan(0)` in `ClanWorldStub.sol`.
- S-6 is adding `nonReentrant` to `finalizeSeason()`.
- S-1 and S-2 are deeper semantic issues in `_settleClan`. Minimum fix for S-1: set `lastSettledTick = tick` (the death tick) instead of `curTick` when breaking early. S-2 is a design-level issue worth documenting even if not fixing now.

**TypeScript adapter (S-4, S-5):**
- S-4: Add `if (value < 0n) throw ...` to the bigint branch of `uintValue`.
- S-5: Add `marketMode` to `SubmitOrderResult` type and forward from decoded result.

**Environment/CI (S-7, S-8, S-10):**
- S-7: Add `VITE_CONVEX_URL: process.env.VITE_CONVEX_URL ?? ''` to Playwright webServer env.
- S-8: Add missing `VITE_*` vars to root `.env.template`.
- S-10: Update README pnpm version to `10.33.2`.

**Documentation (S-11, S-12):**
- Quick doc updates; 5-10 minutes each.

**Housekeeping (S-9, S-13):**
- S-9: Delete `packages/agents/src/__tests__/cli.test.ts` (older location); keep `packages/agents/test/cli.test.ts`.
- S-13: Rename or document scope limitation of parity check.

### Priority 3 — DEFER (new issues post-merge)

Create GitHub issues for D-1 through D-14 after merge. None block this PR. Highest-priority defers: D-1 (bandit skip on partial settle), D-6 (market field wiring for Phase 6 TS support), D-14 (CI coverage gap).

---

## Overall Health Assessment

**Needs Work.** The contract logic is solid and well-tested (254+ forge tests pass per commit messages). The M-1 runner ABI mismatch is a clear runtime blocker for the heartbeat daemon. M-2/M-3 are doc-level blockers that would mislead downstream agents. The SHOULD FIX items are individually small but collectively indicate rushed merge hygiene (stub bugs, missing env vars, stale docs). Recommended: fix M-1 + S-3 + S-4 + S-6 as minimum gate, merge, then batch the remaining SHOULD FIX items in a follow-up cleanup PR within 24h.
