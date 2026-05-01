# PR #198 review — Wave 3 synthesis

| Field | Value |
| --- | --- |
| **PR** | [#198 Phase 6 — Unicorn Town Market](https://github.com/OmniPass-world/clan-world/pull/198) |
| **Base / head** | `dev` ← `dev-phase-6-market` |
| **Review date** | 2026-04-30 (ET) |
| **Model** | Claude Opus 4.6 (Wave 3 synthesis) |
| **Evidence root** | Blind worktree `review-pr-198-blind` only (no `docs/reviews/` reads) |

## Summary stats

| Category | Count |
| --- | ---: |
| **MUST FIX** | 2 |
| **SHOULD FIX** | 7 |
| **DEFER** | 2 |
| **SKIP / FALSE POSITIVE** | 1 |
| **Total findings** | 12 |

## Triage table

| # | File | Line(s) | Finding | Category | Rationale |
| --- | --- | --- | --- | --- | --- |
| 1 | `packages/shared/src/adapters/IChainClient.ts` | 69–96 (`getWorldSnapshot` outputs) | ABI tuple for `getWorldSnapshot` omits `currentSeasonNumber` and `nextHeartbeatAtTick` that the contract returns. | **MUST FIX** | On-chain `getWorldSnapshot()` builds `WorldSnapshot` with both fields (`ClanWorld.sol` 2535–2536). A shorter ABI causes decode/layout mismatch against the canonical contract return (risk: failed reads, wrong field offsets, or silently missing data for indexers/clients). |
| 2 | `packages/shared/src/adapters/IChainClient.ts` | 169–232 (`activeMission` nested tuples ×2) | Mission ABI omits `submittedAtTick`, `executesAtTick`, `settlesAtTick` present on-chain. | **MUST FIX** | `Mission` in `IClanWorld.sol` includes those three `uint64` fields before `clansmanId` (282–288). Client ABI does not; any full decode of `getClanFullView` mission data is misaligned with storage. |
| 3 | `packages/contracts/test/HeartbeatOrdering.t.sol` | 184–243 (`test_heartbeat_settlementBeforeMarket`) | Test narrative claims proof that settlement runs before scheduled market because vault was zero and sell “would fail” if reversed; setup gives **both** clansmen wood **carry**, and scheduled sell uses carry (`_executeMarketSell` deducts carry), not vault. | **SHOULD FIX** | Assertions (`goldAfter > goldBefore`, carry cleared) hold regardless of Step 1 vs Step 2 order for this scenario. Either redesign (e.g., sell path that requires vault liquidity after deposit) or narrow the test comment to what is actually asserted (same-tick coexistence), matching `test_heartbeat_multipleStepsInOneTick`. |
| 4 | `packages/contracts/src/ClanWorld.sol` | 1802; `packages/contracts/src/StubPool.sol` | Immediate sell uses `swapExactInForOut(amount, 1)`; scheduled path calls `sellResource` which forwards `_swapExactInForOut(amountIn, 1)`. | **SHOULD FIX** | `minOut == 1` is effectively no slippage protection (only excludes zero-output rounding). Players/agents cannot specify minOut; economic / MEV posture is weak for a market feature. |
| 5 | `packages/contracts/src/IClanWorld.sol` | 585–607 vs 531–536, 608–616 | Market events use parameter name `csId` while sibling events use `clansmanId`; `ScheduledMarketActionCommitted` mixes indexed `clanId` with trailing `clansmanId`. | **SHOULD FIX** | Same logical field, inconsistent naming across the interface — hurts indexer schemas, codegen, and cross-event joins (no behavior bug, integration friction). |
| 6 | `packages/contracts/src/IClanWorld.sol` | 560–577; `packages/contracts/src/ClanWorld.sol` | `ResourcesDeposited` / `ResourcesWithdrawn` emit `uint32 tick` while global tick and heartbeat paths use `uint64`; `_doDeposit` / `_doWithdrawResources` take `uint32 tick` (672, 706). | **SHOULD FIX** | Intentional mirror comment exists for withdraw (461), but type skew vs `uint64` world tick is a long-horizon footgun and complicates event-driven pipelines; align to `uint64` or document invariant “tick never exceeds uint32” at system level. |
| 7 | `packages/contracts/src/ClanWorld.sol` | 1455–1488 | Scheduled market `try/catch` binds `marketStatus` then discards it (`marketStatus;`). | **SHOULD FIX** | Today non-OK paths inside `_executeMarketSell` / `_executeMarketBuy` route through `_handleMarketFailure` (emits + returns). Ignoring the return is redundant and brittle if a future branch returns a failure code without emitting. Prefer `require(marketStatus == StatusCode.OK)` or branch on status explicitly. |
| 8 | `packages/contracts/src/ClanWorld.sol` | 1931–1969 | NatSpec says scheduled sell “deduct resource from vault”; implementation calls `_deductFromCarry`. | **SHOULD FIX** | Misleading spec for auditors/indexers; behavior is carry-based (comments elsewhere correctly describe CEI on immediate sell). |
| 9 | `packages/contracts/src/StubPool.sol` | 66–75, 86–95 | Swaps update `reserveA` / `reserveB` only; no ERC20 `transfer` on swap. | **SHOULD FIX** | On-chain `balanceOf(pool)` stays at seeded amounts while `getReserves()` reflects traded state — breaks composability and any invariant checks that compare balances to reserves. Acceptable only if documented as **demo-only stub**; otherwise fix or replace for production-like pools. |
| 10 | `packages/contracts/src/ClanWorld.sol` | 67, 919–920 | `_tickSeeds[closedTick] = newSeed` retains every historical tick seed in storage. | **DEFER** | Unbounded growth over game lifetime; acceptable short-term for hackathon scale. Follow-up: pruning policy or ring buffer if seasons run long / gas becomes material. |
| 11 | `packages/contracts/src/ClanWorld.sol` | 1415–1493, 1495–1513 | Per-tick scheduled queue + insertion sort over full queue each execution. | **DEFER** | Under abnormal heartbeat delays or spam, queue length and O(n²) sort could gas-bound ticks. Mitigation is ops cadence + action limits; optimize if measured on-chain. |
| 12 | `packages/contracts/src/ClanWorld.sol` | 1456–1471 vs 1958–1972 | Claim: scheduled sell deducts carry before swap so revert loses carry. | **SKIP / FALSE POSITIVE** | Failure inside `this._executeMarketSellExternal` **reverts the external call**, rolling back storage including `_deductFromCarry` (Solidity call semantics). Immediate path’s `_addToCarry` restore (1816) covers non-reverting swap failures; scheduled path relies on revert rollback — carry is not burned on pool revert. |

## Recommended next steps

### MUST FIX (ordered)

1. **Extend `CLAN_WORLD_ABI` `getWorldSnapshot` outputs** to include `currentSeasonNumber` and `nextHeartbeatAtTick` in the exact on-chain order (`IChainClient.ts`, matching `IClanWorld.sol` `WorldSnapshot` and `ClanWorld.sol` 2530–2542).
2. **Extend both `activeMission` tuple definitions** in `getClanFullView` ABI with `submittedAtTick`, `executesAtTick`, `settlesAtTick` (matching `Mission` in `IClanWorld.sol` 282–308), then adjust any TypeScript mapping/tests that consume decoded structs.

### SHOULD FIX (by theme)

- **Testing / claims:** Fix or rewrite `test_heartbeat_settlementBeforeMarket` so the scenario actually depends on heartbeat step ordering (Finding 3).
- **Market economics:** Replace hardcoded `minOut = 1` with caller-supplied or policy-derived minimum (Finding 4).
- **Interfaces & observability:** Normalize `csId` vs `clansmanId` in events (Finding 5); fix scheduled-market NatSpec vs carry implementation (Finding 8); consider tightening scheduled-market status handling (Finding 7).
- **Cross-layer contracts:** Align deposit/withdraw event tick width with `uint64` or publish explicit bounds (Finding 6).
- **Stub realism:** Document ERC20 vs reserve divergence for `StubPool`, or add transfers if tests/indexers need parity (Finding 9).

### DEFER — proposed follow-up issue titles (do not open from this review)

- “Cap or prune `_tickSeeds` historical mapping for long-running seasons”
- “Replace insertion sort / bound scheduled market queue under stress scenarios”

### Overall PR health

**needs-work** — Two concrete ABI drifts against live contract structs (`getWorldSnapshot`, `Mission`) are integration blockers for correct decoding; remaining items are test accuracy, market parameterization, event/schema hygiene, and stub semantics rather than core game-loop correctness.

---

## Top findings (for reviewers)

**MUST FIX:** (1) `getWorldSnapshot` ABI missing season / next-heartbeat fields — (2) `Mission` ABI missing mission timing fields  

**SHOULD FIX:** (3) Heartbeat ordering test does not prove stated vault-ordering claim — (4) `minOut` hardcoded to 1 — (5) Event parameter naming inconsistency (`csId` vs `clansmanId`) — (6) `uint32` vs `uint64` tick in resource deposit/withdraw events
