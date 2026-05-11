# PR 396 Super-Swarm Review (Dev Merge)

**Review Date:** 2026-05-01
**Model:** geimini-cursor-super-think

## Summary Stats
- **Total Findings:** 33
- **MUST FIX:** 6
- **SHOULD FIX:** 23
- **DEFER:** 4
- **SKIP / FALSE POSITIVE:** 0 (filtered from output for brevity)

## Triage Table

| # | File | Line | Finding | Category |
|---|------|------|---------|----------|
| 1 | `packages/runner/src/runnerCastHeartbeat.ts` | 54-56 | Corrupt minimal `getWorldState` ABI in runner (duplicate tuple fields `currentSeasonNumber`, `nextHeartbeatAtTick`). Breaks viem decoding. | **MUST FIX** |
| 2 | `packages/contracts/src/ClanWorld.sol` | 2911-2922 | Heartbeat mission settlement skips tick upkeep. `_settleCompletingMissions(closedTick)` runs before `_applyUpkeep(clan, closedTick)`, breaking lazy settlement invariant and starvation/gathering math. | **MUST FIX** |
| 3 | `packages/contracts/src/ClanWorld.sol` | 4636-4674 | `transferVaultResource` ignores `_reservedWoodByClan` etc., allowing transfers of reserved resources. | **MUST FIX** |
| 4 | `packages/contracts/src/ClanWorld.sol` | 4696-4699 | `transferBlueprint` ignores `_reservedBlueprintByClan`. | **MUST FIX** |
| 5 | `packages/contracts/src/ClanWorld.sol` | 4731-4745 | `transferBundle` repeats raw checks, ignoring reservations. | **MUST FIX** |
| 6 | `packages/contracts/src/ClanWorld.sol` | 1003-1007 | `_gatherWheat` treats global `WheatPlotState.WinterLocked` as no harvest without tying it to the `closedTick` being settled, causing lazy replay desync. | **MUST FIX** |
| 7 | `packages/contracts/src/ClanWorld.sol` | 3293-3300 | Misleading comment about market orders and cooldown. | **SHOULD FIX** |
| 8 | `packages/contracts/src/ClanWorld.sol` | 4240-4245 | Dead branch `if (cs.currentRegion == REGION_UNICORN_TOWN && cs.state == WAITING)` repeating cooldown comments but not enforcing it. | **SHOULD FIX** |
| 9 | `packages/contracts/src/ClanWorld.sol` | 5035-5037 | `getBanditTargetPreview` always returns 0 while camped. | **SHOULD FIX** |
| 10 | `packages/contracts/src/ClanWorld.sol` | 5337-5372 | `getActiveBanditView` hardcodes `projectedTargetLootValue` to 0, contradicting ABI. | **SHOULD FIX** |
| 11 | `packages/contracts/src/ClanWorld.sol` | 5338-5349 | `nextActionTick` is 0 for `Attacking` state, misleading UIs. | **SHOULD FIX** |
| 12 | `packages/contracts/src/ClanWorld.sol` | 568-569 | Starvation comment cites non-existent spec document. | **SHOULD FIX** |
| 13 | `packages/contracts/src/ClanWorld.sol` | 1638-1660 | Simulation `_simulateGatherWheat` doesn't model `WinterLocked` explicitly. | **SHOULD FIX** |
| 14 | `docs/reviews/pr250-spec-compliance-uat.md` | 8 | Tension between direct winter starvation kills and looser A10 prose. | **SHOULD FIX** |
| 15 | `packages/contracts/test/*Upgrades.t.sol` | Various | Internal review labels committed into NatSpec/comments. | **SHOULD FIX** |
| 16 | `packages/contracts/test/MonumentUpgrades.t.sol` | Various | Less symmetric test coverage compared to Base/Wall. | **SHOULD FIX** |
| 17 | `packages/contracts/src/ClanWorld.sol` | 3199-3216 | OTC transfers vs `submitClanOrders` backlog handling inconsistency. | **SHOULD FIX** |
| 18 | `packages/contracts/src/ClanWorldStub.sol` | 166-270 | OTC implementations omit several guards present on mainnet. | **SHOULD FIX** |
| 19 | `packages/contracts/src/ClanWorldStub.sol` | 126 | Stub `mintClan` emits `ClanSpawned` with invalid region `0`. | **SHOULD FIX** |
| 20 | `packages/shared/src/adapters/check-chain-abi-parity.test.ts` | 81-92 | Test does not assert real `CLAN_WORLD_ABI` contents. | **SHOULD FIX** |
| 21 | `packages/contracts/test/HeartbeatOrdering.t.sol` | N/A | No automated test asserting upkeep-before-mission parity. | **SHOULD FIX** |
| 22 | `apps/web/src/App.tsx` | 71 | Stale merge/contradictory comments regarding World App guard. | **SHOULD FIX** |
| 23 | `apps/web/src/App.tsx` | 147-149 | Misleading gate footer instructions. | **SHOULD FIX** |
| 24 | `docs/reviews/pr250-spec-compliance-uat.md` | 8 | Broken template link to `./pr194-spec-compliance-uat.md`. | **SHOULD FIX** |
| 25 | `turbo.json` | 11-14 | `!abi/IClanWorld.json` in codegen task inputs might cause incorrect cache reuse. | **SHOULD FIX** |
| 26 | `packages/contracts/src/ClanWorld.sol` | 2200-2212 | Bandit attack path settles target/defenders only through tick `< closedTick`. | **SHOULD FIX** |
| 27 | `packages/contracts/test/BanditSpawn.t.sol` | 326-374 | Tests exercise eager settle but not upkeep-vs-heartbeat mission order. | **SHOULD FIX** |
| 28 | `packages/runner/test/runnerCastHeartbeat.test.ts` | N/A | No automated guard on runner minimal ABI vs canonical ABI. | **SHOULD FIX** |
| 29 | `packages/contracts/test/GasProfiling.t.sol` | 38-57 | Materially mis-describes heartbeat worst-case gas (claims 17.4M, actually ~881k). | **SHOULD FIX** |
| 30 | `packages/contracts/src/ClanWorld.sol` | 888-911 | Iron carry yield halved when starving, but gold bonus roll runs at full probability. | **DEFER** |
| 31 | `packages/contracts/src/ClanWorld.sol` | 2114-2132 | Targeting uses raw vault loot, not settled-derived loot. | **DEFER** |
| 32 | `packages/contracts/src/ClanWorld.sol` | 3096-3099 | `finalizeSeason` is a no-op public external. | **DEFER** |
| 33 | `packages/contracts/src/ClanWorld.sol` | 4587-4596 | `transferClanOwnership` allows transfer of DEAD clans. | **DEFER** |

## Recommended Next Steps

1. **Address MUST FIX issues immediately:**
   - Revert the duplicate `WorldState` fields in `packages/runner/src/runnerCastHeartbeat.ts`.
   - Fix the `heartbeat()` mission settlement ordering to ensure `_applyUpkeep(clan, closedTick)` runs before `_settleCompletingMissions(closedTick)` for affected clans.
   - Update all `transfer*` functions in `ClanWorld.sol` (vault, blueprint, bundle) to use `_spendableAfterReleasing()` instead of raw vault balances to respect upgrade reservations.
   - Gate `_gatherWheat` winter lock on `_isWinterActiveAt(tick)` instead of the global `WheatPlotState.WinterLocked` to maintain lazy replay correctness.

2. **Address SHOULD FIX items grouped by theme:**
   - **Hygiene & Docs:** Fix comments and misleading gate footers in `App.tsx`, remove inline review notes from tests, and fix broken links in `pr250-spec-compliance-uat.md`. Correct the gas profiling claims in `GasProfiling.t.sol`.
   - **ABI & Testing:** Improve `check-chain-abi-parity.test.ts` and `runnerCastHeartbeat.test.ts` to actually assert against `CLAN_WORLD_ABI` definitions. Add missing tests for heartbeat vs upkeep ordering and symmetric monument upgrades. Modify `turbo.json` caching inputs to safely include ABI changes.
   - **State logic:** Adjust OTC transfers to return the same backlog error as `submitClanOrders`. Ensure `getBanditTargetPreview` and `getActiveBanditView` correctly project target values or document their current limitations.

3. **DEFER:**
   - Create GitHub issues for the DEFER findings (e.g. Iron gold bonus starvation mechanics, `finalizeSeason` access control, and transferring DEAD clans).