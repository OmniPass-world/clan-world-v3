# PR #194 Blind Swarm Review (codex-5-3)

- **PR:** [#194 - Phase 9 - Bandit System](https://github.com/OmniPass-world/clan-world/pull/194)
- **Base / Head:** `dev` <- `dev-phase-9-bandits`
- **Reviewed head SHA:** `218f9020ecb0f4b8277ef59dd55de8e004404d80`
- **Review date (ET):** 2026-04-30
- **Review mode:** blind independent swarm in linked worktree (`/home/claude/code/omnipass-world/review-pr-194-blind`)

## Blind Protocol

- Used isolated linked worktree checked out directly to PR #194 head.
- Reviewer prompts explicitly denied reading `docs/reviews/**` and prior review/synthesis markdown.
- Wave 1 used 10 parallel reviewers; Wave 2 used 4 targeted deep-dive reviewers for validation/adjudication.
- Final synthesis performed after dedupe and conflict resolution.

## Validation Run

- `pnpm --filter @clan-world/contracts test` -> **PASS** (`133 passed, 0 failed`)
- `pnpm --filter @clan-world/shared test` -> **PASS** (`2 passed`)
- `pnpm --filter @clan-world/runner test` -> **PASS** (`121 passed, 1 skipped`)

## Consolidated Triage

| # | File | Line / Symbol | Finding | Category |
|---|---|---|---|---|
| 1 | `packages/contracts/src/ClanWorld.sol` | `2200-2227` (`_eagerSettleActiveDefendersForBase`) + `1939-1966` (`_totalBanditClansmanDefense`) | Eager defender-settle scan caps can skip defenders relevant to `targetClanId`, allowing stale defender state to influence attack outcomes. Wave 2 repro validation confirmed this path. | **MUST FIX** |
| 2 | `packages/contracts/src/ClanWorld.sol` | `399-411` (`_settleClan` cap) + `1781-1802` (`_resolveBanditAttack`) | Attack resolution settles target once, but `_settleClan` hard-caps at 200 ticks. Long-lag targets can be partially settled during combat resolution. Wave 2 repro validation confirmed. | **MUST FIX** |
| 3 | `packages/contracts/src/ClanWorld.sol` | `490-500`, `532-563`, `590-605`, `2027-2029` | Tick semantics for death-linked events are inconsistent: `ClanEliminated` can emit historical replay tick while `BanditEscaped/BanditTargetDied` emits `_world.currentTick`. Creates indexer timeline ambiguity. | **SHOULD FIX** |
| 4 | `packages/shared/src/adapters/IChainClient.ts` | `165-178` (`getClanFullView`) | Uses `parseInt(clanId, 10)` without strict normalization (accepts malformed suffixes) and narrow cast (`clanId: number`), weakening runtime/type safety on a changed adapter seam. | **SHOULD FIX** |
| 5 | `packages/contracts/src/ClanWorld.sol` | `44-49` (contract header comment) | Header still says Phase 3 bandits are stubbed, conflicting with implemented Phase 9 bandit engine and test suite. Operationally misleading for future reviewers. | **SHOULD FIX** |
| 6 | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/src/IClanWorld.sol` | `1817-1828` (`BanditAttackResolved`) + event spec | `BanditAttackResolved` stolen resource fields are always emitted as zero. This is not currently breaking chain state but is consumer-facing semantic debt. Tracked in issue #302. | **DEFER** -> [#302](https://github.com/OmniPass-world/clan-world/issues/302) |
| 7 | `packages/contracts/src/IClanWorld.sol`, `packages/contracts/src/ClanWorld.sol` | event declarations vs emits | Interface declares bandit events that are not emitted by current implementation (`BanditMoved`, `BlueprintAwarded`, `LootDistributedToDefender`). ABI/event-surface hygiene follow-up tracked in #303. | **DEFER** -> [#303](https://github.com/OmniPass-world/clan-world/issues/303) |
| 8 | `packages/contracts/src/ClanWorld.sol` | `2866-2875` (`_sortScheduledMarketActionsByCommitSequence`) | Insertion sort is O(n^2) under overflow merges. Valid scalability concern, but not a demonstrated merge blocker at current bounded load. Tracked in #304. | **DEFER** -> [#304](https://github.com/OmniPass-world/clan-world/issues/304) |
| 9 | `packages/contracts/src/ClanWorld.sol` | `_resolveBanditAttack` early-return checks | Claim that early-return leaves bandit permanently stuck in `Attacking` was not reproducible under current death/abort transitions (`_markClanDead` + `_abortBanditAttacksForDeadTarget`). | **SKIP / FALSE POSITIVE** |
| 10 | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/src/IClanWorld.sol` | `3378-3380` (`getBanditTargetPreview`) + `759-761` natspec | Returning stored target in preview getter is consistent with documented non-binding semantics; not a correctness bug by itself. | **SKIP / FALSE POSITIVE** |
| 11 | `packages/runner/src/runnerCastHeartbeat.ts`, `packages/contracts/src/ClanWorld.sol` | `84-117` (runner classification logic) | Potential non-rate-limit misclassification concern lacked concrete path against current `heartbeat` ordering and timestamp guard behavior; kept as non-actionable in this PR. | **SKIP / FALSE POSITIVE** |

## Summary Stats

- **Total triaged findings:** 11
- **MUST FIX:** 2
- **SHOULD FIX:** 3
- **DEFER:** 3
- **SKIP / FALSE POSITIVE:** 3

## Recommended Next Steps

1. Fix the two MUST FIX correctness paths first:
   - defender eager-settle scan behavior,
   - partial-settlement attack resolution when lag exceeds 200 ticks.
2. Patch the SHOULD FIX integration/observability issues in the same PR if possible:
   - death-event tick consistency,
   - strict `clanId` parsing + safer adapter typing,
   - stale contract header comments.
3. Keep PR #194 scope tight by deferring non-blocking surface/scalability concerns to issues already created:
   - [#302](https://github.com/OmniPass-world/clan-world/issues/302),
   - [#303](https://github.com/OmniPass-world/clan-world/issues/303),
   - [#304](https://github.com/OmniPass-world/clan-world/issues/304).
4. Re-run contracts/shared/runner tests after MUST+SHOULD fixes, then re-evaluate merge readiness.

## Overall PR Health

**Needs work before merge.** Core functionality is extensive and tests are green, but two correctness findings are high-impact enough to block merge until addressed.
