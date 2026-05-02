# ClanWorld Diamond Pattern Plan

## Summary

ClanWorld must move from one oversized engine contract to an EIP-2535 Diamond.
The current monolith is far above the 24,576 byte EIP-170 runtime limit, and
read-only lenses alone are unlikely to create enough headroom. The diamond keeps
one logical game address and shared state while distributing logic across many
small facets.

Design goals:

- Keep `IClanWorld` ABI stable for callers unless an ABI break is explicitly
  approved.
- Use one shared `AppStorage` through `LibStorage`.
- Prefer many small, domain-focused facets over a few near-limit facets.
- Target each facet under `16 KB`; split before merge if any facet approaches
  `20 KB`.
- Keep cross-facet calls rare and intentional.

## Storage Model

Use a single `AppStorage` struct at a deterministic slot:

```solidity
library LibStorage {
    bytes32 internal constant STORAGE_SLOT =
        keccak256("clan.world.app.storage.v1");

    struct AppStorage {
        // shared reentrancy flag
        // world, treasury, clans, clansmen, missions, wheat plots
        // market queues, reservations, defenders, bandits, seeds
    }

    function appStorage() internal pure returns (AppStorage storage s) {
        bytes32 slot = STORAGE_SLOT;
        assembly {
            s.slot := slot
        }
    }
}
```

Rules:

- No facet-level state variables.
- Existing storage fields are append-only after deployment.
- Nested structs used inside `AppStorage` are also append-only.
- Storage layout snapshots must be updated and reviewed whenever storage changes.
- The shared reentrancy guard lives in `AppStorage`, not in individual facets.

## Facet Boundaries

Recommended initial facet set:

| Facet | Responsibility |
|---|---|
| `WorldClockFacet` | `heartbeat`, tick advancement, orchestration of settlement, market, bandits, and seasons. |
| `SeasonFacet` | `finalizeSeason`, season freeze checks, ranking snapshot behavior. |
| `ClanLifecycleFacet` | `mintClan`, ownership transfer, clan death/elimination cleanup entrypoints. |
| `OrdersFacet` | `submitClanOrders`, order validation, mission install/interruption. |
| `SettlementFacet` | Lazy settlement loop and mission progress mechanics. |
| `UpkeepFacet` | Starvation, winter upkeep/cold damage, crop lock/regrow transitions. |
| `GatheringFacet` | Wood, iron, fish, wheat action resolution. |
| `DepositWithdrawFacet` | Deposit and withdraw mission resolution. |
| `UpgradesFacet` | Wall/base/monument reservations, refunds, settlement, cost tables. |
| `MarketFacet` | Immediate/scheduled market execution, pool calls, market failure handling. |
| `TreasuryFacet` | Treasury init, pool seeding, token/pool routing. |
| `DirectTransfersFacet` | Gold/resource/blueprint/bundle transfers. |
| `BanditStateFacet` | Spawn probability, active registry, movement, transitions, escape/delete. |
| `BanditTargetingFacet` | Spawn weights, target selection, defender discovery. |
| `BanditCombatFacet` | Attack resolution, wall/base damage, casualties, loot and rewards. |
| `RawViewsFacet` | Raw storage getters and simple state views. |
| `DerivedViewsFacet` | Settlement simulation, rankings, scores, full clan views. |
| `MarketViewsFacet` | Pool reserves, market state, quotes. |
| `BanditViewsFacet` | Active bandit view, target preview, bandit/region read models. |

Expected size-risk facets:

- `SettlementFacet`
- `UpgradesFacet`
- `BanditCombatFacet`
- `DerivedViewsFacet`
- `OrdersFacet`

Split these further before merging if any one crosses the warning line.

## Libraries

Use libraries for small shared rules that should not become public selectors:

- `LibTravel`: route matrix, path packing, travel ticks.
- `LibResourceAccounting`: vault/carry add, deduct, spendable, reserved math.
- `LibRules`: small pure action durations, clamps, rule tables.
- `LibStorage`: storage pointer, shared modifiers, and common diamond helpers.

Avoid large internal libraries that get inlined into many facets unless bytecode
measurements prove the duplication is acceptable.

## Cross-Facet Calls

Cross-facet calls are allowed but should be rare.

- Prefer libraries for small pure/shared helpers.
- Prefer same-facet internal calls for tightly coupled mutation.
- Use external self-calls through the diamond only for intentional orchestration
  boundaries, such as heartbeat invoking market execution with `try/catch`.
- Guard internal-only external selectors with `onlyDiamond`.
- Document any orchestrator function that cannot use `nonReentrant` because it
  must call another facet through the diamond.

## Migration Plan

1. **Rules and design**
   - Update `packages/contracts/AGENTS.md`.
   - Keep this architecture doc current.
   - Re-run `forge build --sizes` after pending main/dev merges settle.

2. **Diamond skeleton**
   - Add `Diamond`, `DiamondCutFacet`, `DiamondLoupeFacet`, `IDiamondCut`,
     `LibDiamond`, `LibStorage`, deploy helper, and empty facets.
   - Add selector collision and loupe coverage tests.

3. **Storage and raw reads**
   - Move storage layout into `LibStorage.AppStorage`.
   - Add `RawViewsFacet` and prove raw getter parity with the monolith.

4. **World and lifecycle**
   - Move world clock, season finalization, clan minting, ownership transfer, and
     death cleanup entrypoints.

5. **Settlement and orders**
   - Move settlement, upkeep, gathering, deposit/withdraw, upgrades, orders, and
     shared resource accounting.

6. **Economy**
   - Move market execution, treasury setup, pool seeding, and direct transfers.

7. **Bandits**
   - Move bandit state, targeting, and combat into separate facets.

8. **Derived views**
   - Move settlement simulation, rankings, scores, full clan views, market views,
     and bandit views.

9. **Final parity and deploy**
   - Run full test suite, ABI parity, selector coverage, storage snapshot, and
     size report.
   - Deploy to local Anvil, then Base Sepolia.
   - Archive or remove the monolith only after parity is proven.

## Known Intentional Divergence

The monolith contains narrower eager-settle hooks for bandit-related shared
state. The diamond heartbeat currently settles every live clan before market,
bandit, and season logic runs, so the same correctness invariant is met for
normal heartbeat execution without adding another cross-cutting settlement hook
during the first migration pass.

This is intentionally not final policy. Track the follow-up as a gas and backlog
hardening item: restore or replace the narrower bandit eager-settle hooks if
testnet/mainnet profiling shows the global heartbeat settlement pass is too
expensive or if partial-backlog scenarios need tighter compatibility with the
monolith implementation.

## Acceptance Criteria

- Every facet runtime is below `20 KB`; target is below `16 KB`.
- `IClanWorld` ABI selectors route through the diamond.
- Full `forge test` passes.
- `forge build --sizes` report is saved in the PR.
- Existing heartbeat, settlement, market, bandit, transfer, and view behavior is
  preserved.
- Cross-facet reentrancy and internal-only selector guards are tested.
- Storage layout snapshot is committed and enforced.
