# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY

**Verdict: CLEAN — merge-ready.** The Winter + Elimination phase is soundly implemented. Winter scheduling migrated from mutable-storage timers to a pure-function modular arithmetic approach (`_isWinterActiveAt`), eliminating an entire class of state-management bugs. Cold damage escalation (wall → clansman), starvation-during-winter kills, crop locking/unlocking, and clan death are correctly ordered with proper early-exit guards and idempotency. Test coverage is thorough — 20+ new test cases covering upkeep math, cold damage thresholds, cross-winter settlement, death events, dead-clan order rejection, and lazy reset convergence. The ABI/adapter additions for `currentSeasonNumber` and `nextHeartbeatAtTick` align with the existing struct layout. No HIGH severity findings.

## HIGH severity findings

CLEAN — no findings.

## MEDIUM severity findings

**M1. RNG domain naming inconsistency — `RNG.sol:12`**

```solidity
uint256 internal constant DOMAIN_COLD_DAMAGE = uint256(keccak256("cold_damage"));
```

All other domains follow `"clanworld.<subsystem>.v1"` convention (`clanworld.bandit.v1`, `clanworld.market.noise.v1`, `clanworld.weather.v1`, `clanworld.fair.iteration.v1`). This one uses bare `"cold_damage"`. While the keccak hash makes collision near-impossible regardless of naming, the broken convention weakens the implicit guarantee that all domain strings are namespaced. Suggest: `"clanworld.cold.damage.v1"`.

**M2. Stale winter storage fields never updated after constructor — `ClanWorld.sol:95-97`**

The `_world.winterActive`, `_world.winterStartsAtTick`, and `_world.winterEndsAtTick` storage fields are written once in the constructor and never again — the old season-transition writes were correctly removed since all reads now use `_isWinterActiveAt()` / `_worldStateView()`. But the stale storage values remain in the contract, consuming 3 storage slots that will have wrong values after tick 120. Any future code, tooling, or upgrade that reads raw storage (e.g., a proxy migration, storage-slot-level debugging, a subgraph indexing raw slots) would get incorrect winter state. Consider: zero out those fields after construction or add a `@dev` comment warning that they're vestigial.

**M3. `_markClanDead(uint32)` 1-arg overload appears unused — `ClanWorld.sol:545-547`**

```solidity
function _markClanDead(uint32 clanId) internal {
    _markClanDead(clanId, "unknown", _world.currentTick);
}
```

Not called anywhere in the diff. If it's also not called by pre-existing code, this is dead code with a hardcoded `"unknown"` reason that would emit a misleading `ClanDied` event. Verify via `grep -rn '_markClanDead(clanId)' --include='*.sol'` and remove if unused.

## LOW severity findings

**L1. `_winterEventTick` is an identity function — `ClanWorld.sol:1166`**

```solidity
function _winterEventTick(uint64 tick) internal pure returns (uint64) {
    return tick;
}
```

Both ClanWorld and ClanWorldStub have identical identity-function implementations. This is presumably a placeholder for future event-tick computation (e.g., snapping to winter boundary). Harmless but adds an unnecessary indirection layer. Remove if no future use is planned.

**L2. Dead clans' plots locked/unlocked during winter transitions — `ClanWorld.sol:1134-1160`**

`_lockWheatPlotsForWinter` and `_restartWheatPlotsAfterWinter` iterate `_allClanIds` without checking clan state. Dead clans' plots are locked and unlocked uselessly. With the 12-clan cap this is negligible gas, but adding `if (_clans[clanId].clanState == ClanState.DEAD) continue;` would be a clean optimization.

**L3. `require` inside crop transition loops could revert heartbeat — `ClanWorld.sol:1138, 1152`**

```solidity
require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
```

If the `mintClan` invariant (`< 12` and `< MAX_CLANS_FOR_CROP_TRANSITIONS`) were ever violated (impossible today, but defensive coding), this `require` would revert the heartbeat and brick the game. A `break` would be strictly safer while achieving the same gas-bounding effect.

**L4. Duplicate `_isWinterActiveAt` / `_winterWindowForTick` / `_worldStateView` / `_winterEventTick` implementations — `ClanWorldStub.sol:376-412`**

ClanWorldStub duplicates four pure/view functions verbatim from ClanWorld. These should ideally live in a shared base contract or library to prevent drift. Not actionable for this PR since the stub is test-only, but worth noting for maintenance.

## Cross-cutting observations

- **Starvation + cold ordering is correct and well-tested.** `livingBeforeStarvation` capture → starvation kill → DEAD check → wood burn with pre-death count → cold damage consequence. The `test_winter_starvationWoodBurnUsesPreDeathLivingCount` test directly validates this.
- **Cold damage reset has two complementary paths** (in-loop at the first non-winter tick, and post-loop when `curTick` IS the first non-winter tick). These are not redundant — they cover disjoint cases depending on whether the winter boundary falls inside or at the edge of the settlement range. The `test_winterColdDamageResetIsLazyPerClanAfterPartialSettlement` convergence test validates both paths produce identical results.
- **Dead clan order rejection has correct dual-path design**: `require(ACTIVE)` reverts for pre-existing dead clans (caller error), while post-settlement `ERR_CLAN_DEAD` gracefully handles clans that die during settlement (not caller's fault).
- **ABI field additions** (`currentSeasonNumber`, `nextHeartbeatAtTick` in WorldState/WorldSnapshot) match the existing Solidity struct layout — these fields already existed in the struct and were just missing from the off-chain ABI/adapter. The runner and shared adapter additions are correctly ordered.
- **`ERR_MUST_SETTLE_FIRST` / `ERR_WINTER_LOCKED` status code indices** are appended (28, 29) and verified stable in the test, preventing ABI-breaking enum shifts.
EXIT=0
