# PR #198 Review — gpt-5.3-codex-xhigh

- PR: [#198 — Phase 6 — Unicorn Town Market](https://github.com/OmniPass-world/clan-world/pull/198)
- Review date: 2026-04-30 (ET)
- Model: `gpt-5.3-codex-xhigh`
- Review mode: blind swarm (`10` independent Wave 1 reviewers + Wave 2 validation + Wave 3 escalation)
- Blind constraint: swarm prompts explicitly denied access to `docs/reviews/**`

## Validation Run

- `forge test` in `packages/contracts`: `128 passed, 0 failed`
- `pnpm --filter @clan-world/shared typecheck`: pass
- `pnpm --filter @clan-world/shared test`: `5 passed, 0 failed`

## Consolidated Triage

| # | File | Line | Finding | Category |
|---|---|---:|---|---|
| 1 | `packages/contracts/src/IClanWorld.sol` | ~178-187 | `StatusCode` removed `ERR_MARKET_BUY_MAX_GOLD_EXCEEDED` mid-enum and appended new statuses, shifting ordinal values despite seam stating enum order is stable. | **MUST FIX** |
| 2 | `packages/contracts/src/IClanWorld.sol` | ~16, ~560-607 | Stable seam contract says field/enum/event order is stable, but event signatures changed (`ResourcesDeposited` tick width/name, `MarketActionFailed` shape, market event surface). Needs explicit compatibility strategy or versioning. | **MUST FIX** |
| 3 | `packages/contracts/src/ClanWorld.sol` | ~1207-1214, ~2182-2187 | Comments imply market orders can fall back to scheduled path during cooldown, but code returns `ERR_COOLDOWN_ACTIVE` before market branching. | **SHOULD FIX** |
| 4 | `packages/contracts/src/ClanWorld.sol` | ~1931-1932, ~1987-1988 | NatSpec says scheduled market sell/buy uses vault, but implementation uses carry (`_deductFromCarry`, `_addToCarry`). | **SHOULD FIX** |
| 5 | `packages/contracts/src/ClanWorld.sol` | ~1455-1487 | Scheduled market `try/catch` maps all caught failures to `ERR_LIQUIDITY_INSUFFICIENT`, collapsing error semantics and reducing observability. | **SHOULD FIX** |
| 6 | `packages/contracts/src/ClanWorld.sol` | ~2163-2167 | Submit-time market validation has explicit zero amount and token guard branches, but critical targeted tests for these paths are thin/missing (`ERR_MARKET_ZERO_AMOUNT`, zero/gold token in initialized treasury path). | **SHOULD FIX** |
| 7 | `packages/contracts/src/ClanWorld.sol` | ~2210-2225 | `initTreasury` does not validate pool/token pairing or pool uniqueness, increasing configuration-footgun risk in deploy/ops paths. | **SHOULD FIX** |
| 8 | `packages/shared/src/adapters/IChainClient.ts` | ~66-77, ~169-174, ~204-209 | Pre-existing adapter ABI drift vs canonical contract tuples (`WorldSnapshot`, `Mission`) remains unresolved; confirmed pre-existing from base SHA. Tracked separately. | **DEFER** → [#305](https://github.com/OmniPass-world/clan-world/issues/305) |
| 9 | `packages/contracts/test/ActionTypeEnumStability.t.sol` | ~8-24 | Stability guard exists only for `ActionType`; other exported enums remain unguarded against accidental reordering. Tracked separately. | **DEFER** → [#306](https://github.com/OmniPass-world/clan-world/issues/306) |
| 10 | `packages/contracts/src/ClanWorld.sol` | ~1414-1514 | Scheduled queue execution can become expensive under stale-row buildup; boundedness hardening deferred to focused follow-up. | **DEFER** → [#307](https://github.com/OmniPass-world/clan-world/issues/307) |
| 11 | `packages/contracts/src/StubPool.sol` | ~8-11, ~66-94 | Virtual-reserve AMM model vs token custody semantics is intentional in stub but easy to misinterpret; needs explicit documentation/hardening path. | **DEFER** → [#308](https://github.com/OmniPass-world/clan-world/issues/308) |
| 12 | `packages/contracts/src/ClanWorld.sol` | ~1516-1526, ~1455-1487 | Claimed reentrancy hole in scheduled market execution is not supported by code/tests; internal wrapper + guards prevent reported exploit path. | **SKIP / FALSE POSITIVE** |
| 13 | `packages/contracts/src/ClanWorld.sol` | ~1207-1214 | Claimed cooldown bypass via scheduled market is false; current behavior rejects cooldown submissions consistently. | **SKIP / FALSE POSITIVE** |
| 14 | `packages/contracts/src/StubPool.sol` | ~66-94 | Claimed immediate drain from reserve/accounting split is not supported as a current exploit in this stub model; treated as model/debt, not merge blocker. | **SKIP / FALSE POSITIVE** |

## Summary Stats

- Total findings: `14`
- MUST FIX: `2`
- SHOULD FIX: `5`
- DEFER: `4`
- SKIP / FALSE POSITIVE: `3`

## Recommended Next Steps

1. Resolve MUST FIX items first:
   - Decide seam policy for enum/event compatibility (`append-only`/back-compat) vs explicit versioned break.
   - Update contract/interface/ABI accordingly before merge.
2. Apply SHOULD FIX set in this PR:
   - Align comments/NatSpec with behavior.
   - Improve market failure reason fidelity.
   - Add targeted submit-validation tests.
   - Add minimal treasury wiring guards or explicit constraints.
3. Keep PR-scoped follow-ups in new issues:
   - [#305](https://github.com/OmniPass-world/clan-world/issues/305)
   - [#306](https://github.com/OmniPass-world/clan-world/issues/306)
   - [#307](https://github.com/OmniPass-world/clan-world/issues/307)
   - [#308](https://github.com/OmniPass-world/clan-world/issues/308)
4. Re-run validation gates after fixes:
   - `forge test`
   - `pnpm --filter @clan-world/shared typecheck`
   - `pnpm --filter @clan-world/shared test`

## PR Health Verdict

`needs-work` before merge (blocking seam-compatibility concerns).
