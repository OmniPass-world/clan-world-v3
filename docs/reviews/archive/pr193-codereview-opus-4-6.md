# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY

**NEEDS_FIXES (1 MEDIUM, rest LOW).** The deposit and wood-gathering changes are internally consistent, well-tested, and the resource accounting is correct. The main concern is the `CLANSMAN_CARRY_CAP` naming implying universality while only being enforced for wood — combined with the yield-formula migration (`yield_per_tick * duration`) only applied to wood, this creates a design asymmetry that risks confusion in future phases. The event ABI change (`uint64 → uint32` tick, param renames) is intentional and documented but is an indexer-breaking change that needs coordination. No security or anti-cheat holes found; checked math prevents overflow, domain-separated RNG is standard, and carry values are trusted internal state with no external setter.

## HIGH severity findings

CLEAN — no findings.

## MEDIUM severity findings

### M1: `CLANSMAN_CARRY_CAP` naming implies universality but only gates wood

**Files:** `IClanWorld.sol:43`, `ClanWorld.sol:490-510`

`CLANSMAN_CARRY_CAP = 10e18` is named as a per-clansman limit, yet it's only enforced in `_gatherWood`. Other resources retain independent caps (`IRON_CAP = 5e18`, `WHEAT_CAP = 40e18`, `FISH_CAP = 8e18`). A clansman can still carry 40e18 wheat — 4x the "carry cap." If this is intentional (per-resource caps with wood migrated to a shared constant first), the constant should be `WOOD_CARRY_CAP` or documented as phase-incremental. If the intent is a true universal cap, iron/wheat/fish gathering functions need the same migration.

Closely related: `_gatherWood` now uses `WOOD_YIELD_PER_TICK * getActionDuration(ChopWood)` (batch yield over the action period), but `_gatherIron`/`_gatherWheat`/`_gatherFish` presumably still use flat per-resolution yields. This yield-model inconsistency will compound if another phase adds duration-based gathering for those resources without realizing wood was migrated first.

**Recommendation:** Either rename to `WOOD_CARRY_CAP`, or add a code comment at the constant declaration stating the migration plan for other resources.

## LOW severity findings

### L1: `ResourcesDeposited` event ABI break — `uint64 atTick` → `uint32 tick`

**Files:** `IClanWorld.sol:539-541`, `ClanWorld.sol:454-456`

The event signature changed in two ways: type narrowed (`uint64 → uint32`) and parameter renamed (`atTick → tick`). This changes the topic hash. Any off-chain indexer, subgraph, or frontend listening for the old `ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint64)` signature will silently miss events. The `unsafe-typecast` lint suppression at the call site is appropriate (uint32 covers ~136 years of 1-second ticks). Just ensure indexer migration is tracked.

### L2: Backward-compat constants are semantically misleading

**Files:** `IClanWorld.sol:48-50`

`WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK` (1e18) exists for backward compatibility, but the crit logic now uses `yield *= 2` (multiplicative doubling), not `yield += WOOD_CRIT_BONUS` (additive). Any code outside this contract that reads `WOOD_CRIT_BONUS` expecting additive semantics will be wrong. `WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK` is similarly misleading since the actual base yield is now `WOOD_YIELD_PER_TICK * actionDuration`, not `WOOD_BASE_YIELD` alone.

**Recommendation:** Add `@deprecated` natspec to the aliases, or remove them if no external consumers exist.

### L3: Error code semantics change on deposit region check

**File:** `ClanWorld.sol:1616`

`ERR_NOT_AT_HOMEBASE → ERR_INVALID_REGION` changes the error returned when a deposit targets a non-home region. If any client-side code pattern-matches on the old error code for user-facing messaging, it will fall through to a generic error. Low risk since this is likely pre-deployment, but worth a grep for `ERR_NOT_AT_HOMEBASE` references in frontend/indexer code.

### L4: Test harness duplication

**Files:** `ClanWorld.t.sol:51-56` (`setCarry`), `Gathering.t.sol:22-24` (`setCarryWood`)

`ClanWorldTestHarness.setCarry(csId, wood, iron, wheat, fish)` and `GatheringHarness.setCarryWood(csId, wood)` are independent harnesses with overlapping functionality. `GatheringHarness` could extend `ClanWorldTestHarness` or at least use the same `setCarry` signature for consistency. Minor — test-only concern.

### L5: Hardcoded tick value in event assertion

**File:** `ClanWorld.t.sol:571`

```solidity
emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
```

The expected tick `1` is hardcoded rather than derived from the mission's `settlesAtTick`. If the initial world state or order processing timing changes, this test breaks for the wrong reason. Consider computing the expected tick from the mission struct.

## Cross-cutting observations

1. **Anti-cheat surface is clean.** Carry values are internal state set only by gather functions (which cap correctly). Deposits transfer trusted carry → vault with Solidity 0.8.x checked arithmetic. No external setter exists outside test harnesses. RNG is domain-separated with tickSeed — predictable by block proposers but standard for on-chain games.

2. **Deposit idempotency.** Empty deposits are silently completed (no event, mission completes). This is correctly tested in `test_depositResources_emptyCarryNoopsWithoutEvent`. The explicit comment at the no-op branch is good documentation.

3. **Phase scope is well-bounded.** The diff cleanly separates deposit mechanics (ABI, duration, region validation) from gathering mechanics (yield formula, crit, carry cap). Test coverage for deposits is thorough (5 new tests covering happy path, empty, multi-resource, wrong region, event shape). Gathering tests cover base yield, crit distribution, cap clamp, and cooldown.

4. **DEPOSIT_DURATION_TICKS as private constant** (line 77) is good — separating it from the `getActionDuration` fallthrough group makes the 1-tick deposit duration explicit and independently tunable.
EXIT=0
