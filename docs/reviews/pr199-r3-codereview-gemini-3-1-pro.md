Error executing tool grep_search: Path does not exist: /home/claude/claudes-world/packages/contracts/src/ClanWorld.sol
Error executing tool grep_search: Path does not exist: /home/claude/claudes-world/packages/contracts/src/ClanWorld.sol
# Phase Super-Swarm Review (R3) — PR #199 (head 36420da)

## SUMMARY
NEEDS_FIXES. The Phase 8 progression and ranking logic is structurally complete, and the R2 fixes properly resolved the resource economy and racing reservations. However, the read-only settlement simulation introduces severe regressions that break clan rankings and drastically undercount loot for unsettled clans. Additionally, the complete removal of the deprecated `BuildWall` action state handler will permanently orphan any flighted missions in live state, locking up clansmen. A final fix round is required before merge.

## HIGH severity findings

**1. Unsettled Monument Simulation awards max time-bonus to `getClanScore`**
*File: `ClanWorld.sol`, lines 882 & 2795*
In `_simulateSettleMonumentUpgrade`, the simulation correctly increments `sim.clan.monumentLevel`, but as a view-only context it cannot write to the `_monumentLevelReachedAt` mapping. When `_getClanScoreFromClan` evaluates the simulated clan state, it fetches `_monumentLevelReachedAt[clanId][monumentLevel]` directly from unmutated storage, yielding `0`. The scoring formula `type(uint64).max - 0` then inadvertently grants the simulated clan the maximum possible time component. Unsettled clans that simulate reaching a monument level will unjustly jump to the top of the leaderboard, overtaking settled clans that reached the same level at a legitimate historical tick.
*Suggested fix:* Move `currentMonumentReachedAtTick` directly into the `Clan` struct instead of using a standalone mapping. This allows the simulation memory copy to naturally update the reach tick and guarantees accurate scoring.

**2. Simulation abruptly terminates continuous gathering missions**
*File: `ClanWorld.sol`, line 969*
In `_simulateSettleMissionForClansman`, if a continuous mission (like `ChopWood`, duration > 0) executes its action but does not hit its carry cap (`m.active` remains true), the simulation does the following:
```solidity
if (m.active && getActionDuration(m.action) > 0) {
    (cs, m) = _simulateCompleteMission(cs, m);
}
```
This forces all continuous gathering missions to prematurely complete after exactly one yield tick. It fails to reschedule `m.settlesAtTick` like the real settlement logic does. As a result, `quoteLootValueSettled` and `getRankings` severely underestimate the gathered resources of unsettled clans.
*Suggested fix:* Mirror the real settlement logic in the simulation by rescheduling the tick rather than aborting:
```solidity
if (m.active && getActionDuration(m.action) > 0) {
    m.settlesAtTick = tick + getActionDuration(m.action);
}
```

**3. Unbounded simulation loop in `getRankings` enables View DoS**
*File: `ClanWorld.sol`, line 2735 & 896*
`getRankings` calls `_simulateSettleToTick(clanId, _world.currentTick)` for up to 24 clans. This simulation iterates tick-by-tick (`for (uint64 tick = fromTick; tick < toTick; tick++)`). If multiple clans have not been settled for a prolonged period, the tick delta becomes enormous. Running an `O(unsettled_ticks * clansmen)` loop for up to 24 clans within a single unpaginated view function will quickly hit RPC timeout thresholds or out-of-gas limits, breaking the leaderboard completely.
*Suggested fix:* Implement a maximum tick-delta cap for the simulation (falling back to raw state if too far behind), lazily enforce periodic settlements via incentives, or rely on a trailing indexer for rankings instead of an on-chain `O(N)` loop.

**4. Deprecated `BuildWall` action orphans flighted missions**
*File: `ClanWorld.sol`, line 528 & 2570*
The deprecation of `ActionType.BuildWall` removed it from `_resolveAction` and removed its duration handler (implicitly returning 0). If a clan has an existing, flighted `BuildWall` mission from prior to this release, it will fall through the action resolver, never call `_completeMission`, and `m.settlesAtTick` will never update. Since `tick >= m.settlesAtTick` remains permanently true but the action does not resolve, the clansman will be permanently stuck in the `ACTING` state.
*Suggested fix:* Restore `ActionType.BuildWall` inside `_resolveAction` as a graceful fallback that immediately calls `_completeMission(cs, m)` or refunds the resources, freeing the clansman to accept new orders.

## MEDIUM severity findings

**1. Clansman re-assignment between building types fails strict resource validation**
*File: `ClanWorld.sol`, line 2155 & 2217*
When re-assigning a clansman from one building task to another (e.g., from `UpgradeWall` to `UpgradeBase`), `submitClanOrders` will correctly refund the old wall reservation before processing the new base reservation. However, the new order is validated via `_validateUpgradeBaseOrder` *before* the refund occurs. `_validateUpgradeBaseOrder` only considers `_baseUpgradeReservations` for its `releasedWood` calculation, ignoring the wood locked in the active wall reservation. If the vault lacks surplus resources, the switch will falsely revert with `ERR_MISSING_RESOURCES`.
*Suggested fix:* Compute total cross-type refundable resources during validation, or perform the refund to memory state before running `_validateClanOrder`.

## LOW severity findings

**1. Empty deposits execute silently but consume a tick**
*File: `ClanWorld.sol`, line 745*
`_doDeposit` currently aborts early without emitting a `ResourcesDeposited` event if the clansman's carry is empty. This is an intended UI optimization, but it still enforces the 1-tick `DEPOSIT_DURATION_TICKS` penalty. 
*Suggested fix:* Defer-OK. It operates safely, but `_validateClanOrder` could proactively block empty deposits to save players from wasting ticks.

## Cross-cutting observations

- **Strong reservation logic:** The `_spendableAfterReleasing` / reservation pattern added in this PR effectively solves the R1 race conditions. The queue correctly accounts for sequential planned levels (`pendingUpgrades`) and self-heals beautifully when reservations are cancelled out of order.
- **Bitpacking elegance:** The `getClanScore` implementation successfully packs four variables (monument level, reached tick, capped loot value, and wall level) into a single `uint256` integer. The bitwise offsets and caps are perfectly aligned without truncation overlap.
EXIT_GEMINI=0
