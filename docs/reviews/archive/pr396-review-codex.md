# PR #396 Review — Codex

## Summary

Independent superswarm review of PR `#396` (`dev-merge` into `dev`, head `784b4bb9b6c5a498d8be245c9449a472951743c4`).

- Review mode: fresh local PR review in detached worktree
- Worktree: `/home/claude/code/omnipass-world/review-pr-396-superswarm`
- Independence constraint honored: did **not** read `docs/reviews/**` contents or `docs/conventions/pr-review.md`
- Swarm shape: 6 first-wave reviewers + 3 validation/arbitration reviewers
- Local validation run:
  - `pnpm test:chainclient-abi` ✅
  - `pnpm --filter @clan-world/shared test` ✅
  - `pnpm --filter @clan-world/shared typecheck` ✅
  - `pnpm --filter @clan-world/web build` ✅
  - `forge` unavailable in this environment, so no Foundry execution
  - extra spot-check: a local `viem` decode experiment against the runner’s handwritten `HEARTBEAT_ABI` fails with an out-of-bounds tuple decode, confirming the ABI mismatch is runtime-real

## Must Fix

1. `packages/runner` heartbeat ABI no longer matches `WorldState`
   - `packages/runner/src/runnerCastHeartbeat.ts` duplicates `currentSeasonNumber` and `nextHeartbeatAtTick` at the end of `HEARTBEAT_ABI`, but the canonical `WorldState` contains those fields only once and ends with `nextCommitSequence`.
   - Evidence: [runnerCastHeartbeat.ts](/home/claude/code/omnipass-world/clan-world/packages/runner/src/runnerCastHeartbeat.ts:43), [runnerCastHeartbeat.ts](/home/claude/code/omnipass-world/clan-world/packages/runner/src/runnerCastHeartbeat.ts:54), [IClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/IClanWorld.sol:202), [runnerCastHeartbeat.ts](/home/claude/code/omnipass-world/clan-world/packages/runner/src/runnerCastHeartbeat.ts:169)
   - Why it matters: `readNextHeartbeatAt()` is used for `isHeartbeatDue()` and rate-limit upgrade logic in `callHeartbeat()`. With the tuple shape wrong, runner heartbeat reads can fail or misdecode.

2. OTC transfers are reservation-blind and can spend held upgrade resources
   - `transferVaultResource`, `transferBlueprint`, and the resource/blueprint legs of `transferBundle` check raw balances and debit raw balances instead of spendable-after-reservations balances.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:4656), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:4696), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:4733), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:4509)
   - Why it matters: a clan can reserve wood/iron/wheat/blueprints for upgrades, OTC-transfer the same assets away, and leave the upgrade stuck retrying against a reservation ledger that still thinks the assets are held.

3. `WithdrawResources` is reservation-blind
   - Submit-time validation uses `_hasVaultResources`, and settlement debits raw vault balances, with no reservation-aware guard.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:3790), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:4150), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:1077), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:1083)
   - Why it matters: one clansman can withdraw resources already reserved by another clansman’s pending upgrade, creating the same stuck-reservation failure mode as the OTC transfer bug.

4. Simulated death paths do not release held reservations
   - Real death settlement refunds reservations before deactivating missions; simulation-only death paths only flip `mission.active = false`.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:687), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:701), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:1441), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:1454), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:5061)
   - Why it matters: `quoteLootValueSettled`, `getRankings`, and `getClanFullView` can diverge from real settlement after starvation/cold deaths because simulated upkeep still sees wheat as reserved when real settlement would have released it.

5. Dead-target cleanup misses traveling defenders
   - `_releaseDefendersForDeadTarget` only clears defenders whose `_clansmanDefendingRegion` is already set. In-flight `DefendBase` missions are not registered yet, so they survive cleanup and can later arrive to defend a dead clan forever.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:749), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:758), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:458), [BanditAttackResolution.t.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/test/BanditAttackResolution.t.sol:347)
   - Why it matters: this strands defenders on dead targets and also poisons defender-related read paths later.

## Should Fix

1. Winter upkeep burns reserved wood before upgrade settlement
   - Wheat reservations are upkeep-aware; winter wood burn is not.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:503), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:588), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:4293)
   - Impact: a wall/base/monument upgrade can reserve exact wood, then fail on its settlement tick because winter upkeep spent the “held” wood first.

2. `IChainClient` drops `marketMode` from `SubmitOrdersResult`
   - Canonical `OrderResult` includes `marketMode`, but the TS result type and result mapper omit it.
   - Evidence: [IChainClient.ts](/home/claude/code/omnipass-world/clan-world/packages/shared/src/adapters/IChainClient.ts:13), [IChainClient.ts](/home/claude/code/omnipass-world/clan-world/packages/shared/src/adapters/IChainClient.ts:2218), [IChainClient.ts](/home/claude/code/omnipass-world/clan-world/packages/shared/src/adapters/IChainClient.ts:2428), [IClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/IClanWorld.sol:411)
   - Impact: shared consumers cannot tell whether a market order executed immediately or was scheduled, even though the contract returns that distinction.

3. The shared-package “ABI parity” Vitest is self-referential
   - The new Vitest checks its own fixtures instead of inspecting the exported `CLAN_WORLD_ABI`.
   - Evidence: [check-chain-abi-parity.test.ts](/home/claude/code/omnipass-world/clan-world/packages/shared/src/adapters/check-chain-abi-parity.test.ts:63), [check-chain-abi-parity.test.ts](/home/claude/code/omnipass-world/clan-world/packages/shared/src/adapters/check-chain-abi-parity.test.ts:83), [packages/shared/package.json](/home/claude/code/omnipass-world/clan-world/packages/shared/package.json:16)
   - Impact: repo CI still has a stronger root-level ABI drift check, but `packages/shared`’s own test path gives false confidence and will not catch real local ABI/test drift.

4. `getClanFullView` defender fields can stay stale in simulated-death preview paths
   - The getter simulates clan/clansman state, then falls back to raw defender registries for `thisClanDefendingBaseId` and always returns raw `_defendingClansByRegion` for `incomingDefenderIds`.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:5284), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:5293), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:5309)
   - Impact: preview getters can disagree with actual settled defender state after simulated death flows. I’m keeping this as `should`, not `must`, because `incomingDefenderIds` is already labeled legacy in the interface.

5. Bandit target selection uses stale raw loot/state once any bandit is alive
   - Once step 3 of heartbeat skips the general eager-settle bandit pass, camped bandits pick targets from raw storage `_lootValueRaw(clan)` and raw clan state instead of settled/derived state.
   - Evidence: [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:2114), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:2081), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:2759), [ClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/ClanWorld.sol:5061)
   - Impact: attacks can be aimed at the wrong base relative to the “settled” world view.

## Defer (new GH issue)

1. Market event schema churn should be frozen before the real indexer/log consumer lands
   - Event signatures and indexed fields changed materially.
   - Evidence: [IClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/IClanWorld.sol:602), [IClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/IClanWorld.sol:612), [IClanWorld.sol](/home/claude/code/omnipass-world/clan-world/packages/contracts/src/IClanWorld.sol:622)
   - Why defer: the current backend is still stubbed and not decoding chain logs yet, so this is real future integration risk but not the thing I would block `dev-merge` on today. See [apps/server/AGENTS.md](/home/claude/code/omnipass-world/clan-world/apps/server/AGENTS.md:11) and [apps/server/convex/heartbeat.ts](/home/claude/code/omnipass-world/clan-world/apps/server/convex/heartbeat.ts:21).

## Skip / False Positive / Merged Away

1. `ownerNonce` inserted mid-`Clan` tuple as a standalone blocker
   - Skipped. In this repo, hackathon policy explicitly allows breaking compatibility when there are no production users yet, and in-repo consumers regenerate from the shared ABI seam. See [AGENTS.md](/home/claude/code/omnipass-world/clan-world/AGENTS.md:48) and [AGENTS.md](/home/claude/code/omnipass-world/clan-world/AGENTS.md:53).

2. `submitClanOrders` selector shape drift as a standalone blocker
   - Skipped. Same reason: repo-local consumers are expected to move with the adapter seam. The real actionable bug is the adapter dropping `marketMode`, already captured above.

3. `BanditState` numeric shift as a blocker
   - Skipped. There is no repo-local guarantee that `BanditState` ordinals are frozen the way `ActionType` and `StatusCode` are.

4. “A stale attacking target can permanently suppress all future bandit spawns”
   - Skipped as an actionable blocker for this PR. The deferred-attack chain is real in a harness-forced scenario, but I could not confirm normal heartbeat flow reaches that state without extra assumptions.

5. Duplicate/overlap findings merged into broader roots
   - OTC transfer reservation blindness and withdraw reservation blindness are kept as separate `must fix` items because they are separate public spend paths, but they share the same deeper cause: upgrade validation became reservation-aware while later spend paths still debit raw balances.
   - Simulated stale defender previews are mostly subsumed by the simulated-death reservation bug and the traveling-defender cleanup bug.

## Recommended Next Steps

1. Fix the runner ABI immediately and add a tiny test that decodes canonical `getWorldState()` through `HEARTBEAT_ABI`.
2. Unify all spend paths around one reservation-aware primitive:
   - OTC transfer debits
   - withdraw validation + withdraw settlement
   - winter wood burn, if reservations are intended to protect wood too
3. Add targeted Foundry tests for the missing cases:
   - transfer reserved wood/iron/wheat/blueprint away before upgrade settlement
   - withdraw reserved resources before upgrade settlement
   - winter reserved-wood upgrade case
   - simulated death while a wheat reservation is held
   - traveling defender against a target that dies before arrival
4. Fix the shared adapter seam by preserving `marketMode` in `SubmitOrdersResult`.
5. Decide whether the self-referential shared Vitest should inspect `CLAN_WORLD_ABI` directly or be removed in favor of the stronger root-level drift check.
