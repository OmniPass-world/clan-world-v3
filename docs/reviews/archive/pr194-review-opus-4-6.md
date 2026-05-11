# PR #194 Review — Phase 9 Bandit System

- PR: [https://github.com/OmniPass-world/clan-world/pull/194](https://github.com/OmniPass-world/clan-world/pull/194)
- Base/Head: `dev` <- `dev-phase-9-bandits`
- Review date (ET): 2026-04-30
- Review mode: Blind independent swarm (10 sub-agents, 2 waves + synthesis)
- Constraint honored: swarm agents did not read `docs/reviews/`

## Summary Stats

- Total findings triaged: **15**
- **MUST FIX:** 2
- **SHOULD FIX:** 7
- **DEFER (tracked issue):** 3
- **SKIP / FALSE POSITIVE:** 3
- Overall PR health: **Needs work before merge**

## Triage Table


| #   | File                                                                               | Line / Symbol                                                                   | Finding                                                                                                                                                                                                           | Category                                                                    |
| --- | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| 1   | `packages/contracts/src/ClanWorld.sol`                                             | `_resolveBanditAttack` (`1791-1796`)                                            | Early return can leave a troop in `Attacking` if `targetClan` becomes `DEAD` after defender eager-settle, without transitioning to `Escaped`/`Defeated`; this risks a stuck state due one-tick resolution gating. | **MUST FIX**                                                                |
| 2   | `packages/runner/src/runnerCastHeartbeat.ts`                                       | `callHeartbeat` catch path (`107-117`)                                          | Non-rate-limit reverts can be upgraded to `HeartbeatRateLimitedError` when `nextHeartbeatAtTs` is still in the future, masking real failures.                                                                     | **MUST FIX**                                                                |
| 3   | `packages/contracts/src/IClanWorld.sol`, `packages/contracts/src/ClanWorld.sol`    | `getBanditTargetPreview` NatSpec (`759-761`) vs impl (`3378-3380`)              | Interface says preview is recomputed from current settled state; implementation returns stored `targetClanId` only.                                                                                               | **SHOULD FIX**                                                              |
| 4   | `packages/contracts/src/ClanWorld.sol`                                             | `getActiveBanditView` (`3566-3583`)                                             | Attempt/projection fields are placeholder zeros (`attackAttemptsMade`, `maxAttemptsRemaining`, `projectedTargetLootValue`) with no explicit contract-level caveat.                                                | **SHOULD FIX**                                                              |
| 5   | `packages/contracts/src/ClanWorldStub.sol`, `packages/contracts/src/ClanWorld.sol` | `heartbeat` (`78-83`) vs (`2305-2312`)                                          | Stub heartbeat cadence/rate-limit semantics diverge from production (`nextHeartbeatAtTs = now` vs `now + interval` + require), weakening integration confidence when stub is used.                                | **SHOULD FIX**                                                              |
| 6   | `packages/contracts/src/IClanWorld.sol`                                            | event `LootDistributedToDefender` (`619`)                                       | Event is declared in interface but not emitted in contract paths, causing dead event surface / indexer confusion.                                                                                                 | **SHOULD FIX**                                                              |
| 7   | `packages/shared/test/clanWorldAbi.test.ts`                                        | `63-126`                                                                        | ABI decode tests validate only synthetic `getWorldState`/`getWorldSnapshot`; no bandit getter decode checks and leaderboard is empty-only. Coverage is narrow for Phase 9 ABI churn.                              | **SHOULD FIX**                                                              |
| 8   | `packages/shared/src/adapters/IChainClient.ts`                                     | `getClanFullView` cast (`171-178`)                                              | Narrow manual cast (`clanId: number`) can hide future `bigint`/shape drift at compile time and fail later at runtime.                                                                                             | **SHOULD FIX**                                                              |
| 9   | `packages/contracts/src/ClanWorld.sol`                                             | heartbeat doc block (`2297-2304`)                                               | Header comment still says “Phase 9 stub”/old step narrative while implementation is 8-step deterministic flow.                                                                                                    | **SHOULD FIX**                                                              |
| 10  | `packages/contracts/src/ClanWorld.sol`                                             | `_lootValueRaw(clan)` call sites (`1703-1735`, `2230-2268`, `3448-3462`)        | Passing `Clan storage` into `_lootValueRaw(Clan memory)` on hot paths causes avoidable storage-to-memory copy overhead.                                                                                           | **SHOULD FIX**                                                              |
| 11  | `packages/contracts/src/ClanWorld.sol`                                             | `_eagerSettleForBandits` + `_pickBanditAttackTarget` (`2156-2180`, `1703-1735`) | Target selection can use stale raw clan state because eager settlement is conditional/capped and target scoring uses raw loot.                                                                                    | **DEFER** → [#268](https://github.com/OmniPass-world/clan-world/issues/268) |
| 12  | `packages/contracts/src/IClanWorld.sol`, `packages/contracts/src/ClanWorld.sol`    | constants (`71-72`), spawn carry (`1545-1549`), attack event (`1817-1828`)      | Raid steal/drop economics are effectively inert in production flow; constants and event fields suggest behavior not implemented.                                                                                  | **DEFER** → [#300](https://github.com/OmniPass-world/clan-world/issues/300) |
| 13  | `packages/contracts/src/IClanWorld.sol`, `packages/contracts/src/ClanWorld.sol`    | `BANDIT_MAX_ATTACK_ATTEMPTS` (`37`), `ActiveBanditView` fields (`3571-3572`)    | Attempt-cap constant exists but is unused in state machine; related view fields remain zero placeholders.                                                                                                         | **DEFER** → [#301](https://github.com/OmniPass-world/clan-world/issues/301) |
| 14  | `packages/contracts/src/ClanWorld.sol`                                             | `_markClanDead`/`_abortBanditAttacksForDeadTarget` (`532-563`, `590-607`)       | “Missing `BanditTargetDied` for the killer bandit” is likely intentional due `excludedBanditId`; event still emitted for other aborted attackers.                                                                 | **SKIP / FALSE POSITIVE**                                                   |
| 15  | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/src/IClanWorld.sol`    | eager settle scan caps + clan cap (`2184-2187`), `MAX_CLANS` (`1`)              | “Base settle scan misses clans” does not hold under current hard cap (`MAX_CLANS = 12`, scan cap per region = 12).                                                                                                | **SKIP / FALSE POSITIVE**                                                   |


## Recommended Next Steps

1. **Fix MUST FIX items first (blocking):**
  - Patch `_resolveBanditAttack` to guarantee terminal transition when post-defender-settle target is dead.
  - Tighten runner error classification so only true rate-limit failures map to `HeartbeatRateLimitedError`.
2. **Address SHOULD FIX set in this PR if possible:**
  - Align preview/getter semantics and comments with actual behavior.
  - Resolve dead event/API ambiguities.
  - Strengthen ABI regression tests and type-safe decoding boundaries.
  - Decide whether to take the low-risk `_lootValueRaw` perf cleanup now or in a follow-up.
3. **Track deferred work via linked issues:**
  - Existing: [#268](https://github.com/OmniPass-world/clan-world/issues/268)
  - New: [#300](https://github.com/OmniPass-world/clan-world/issues/300), [#301](https://github.com/OmniPass-world/clan-world/issues/301)
4. **Merge decision:**
  - Current status: **not merge-ready** until MUST FIX items are resolved.