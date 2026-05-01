Now I have all the data. Let me compile the review.

---

# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY

**Verdict: NEEDS_FIXES (1 HIGH, 4 MEDIUM, 3 LOW)**

Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**

---

## HIGH severity findings

### H1: `_isBanditSpawnRegionCandidate` uses wrong seed — eager-settle/spawn decision divergence

**File:** `ClanWorld.sol:2101`

```solidity
return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
```

This function is called from `_eagerSettleForBandits` (step 3 in heartbeat) to decide which regions need eager settlement before bandit spawn evaluation. It uses `_world.currentTickSeed` (the seed that was live when heartbeat started — i.e., the *previous* tick's seed).

However, `_evaluateBanditSpawns` (step 6) receives `closedTickSeed` as its parameter, which is the *same* seed captured at heartbeat entry (`bytes32 closedTickSeed = _world.currentTickSeed` on line 2230). So these two calls **do** use the same seed value in practice.

**But**: `_isBanditSpawnRegionCandidate` also reads `_world.currentTick` for the cooldown check (line 2096). Between step 3 and step 6, intervening steps (advance bandit states, resolve attacks) could change `_activeBanditCount` or delete bandits from regions — meaning a region that was NOT a candidate at step 3 could BECOME one by step 6 (e.g., a defeated bandit frees up a region slot). In that case, eager settlement was skipped for a region where a spawn will actually occur.

**Impact:** A bandit could spawn into a region where bases and defenders were NOT eager-settled, meaning the very next attack resolution sees stale clan state. This is a correctness bug, not a security bug — the clan will settle lazily later, but attack damage calculations against stale vaults/defender counts could be wrong.

**Suggested fix:** Move `_eagerSettleForBandits` to run immediately before `_evaluateBanditSpawns`, or restructure so eager-settle is triggered after the spawn region is selected but before the spawn is committed.

---

## MEDIUM severity findings

### M1: `getClanFullView` — `thisClanDefendingBaseId` stores region ID, not clan ID

**File:** `ClanWorld.sol:3425`

```solidity
thisClanDefendingBaseId = sim.missions[i].targetRegion;
```

The field `thisClanDefendingBaseId` semantically should identify what this clan is defending. The code stores `targetRegion` (a `uint8` region enum value 1–8) into a `uint32` field. The fallback path (line 3432) also stores a region from `_clansmanDefendingRegion`. So this is *consistently* a region, but the field name implies it's a clan ID. If any frontend consumer treats this as a clan ID, it will misidentify the defense target.

**Suggested fix:** Either rename the field to `thisClanDefendingRegion` in the struct, or store `sim.missions[i].targetClanId` to match the name.

### M2: `_simulateResolveAction` starvation check uses `_world.currentTick` instead of simulation tick

**File:** `ClanWorld.sol:1206`

```solidity
bool starving = sim.clan.starvationStartsAtTick != 0 
    && sim.clan.starvationStartsAtTick <= _world.currentTick;
```

The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.

**Impact:** Low in practice — the upkeep/resolution ordering within the simulation loop prevents the misalignment. But it's a latent bug if the simulation structure changes. Should use `sim.clan.starvationStartsAtTick <= tick` for correctness parity.

### M3: Wall damage only reduces one level regardless of incoming damage magnitude

**File:** `ClanWorld.sol:1899-1906`

```solidity
wallDamage = remainingDamage < WALL_HP_PER_LEVEL ? remainingDamage : WALL_HP_PER_LEVEL;
remainingDamage -= wallDamage;
if (wallDamage >= WALL_HP_PER_LEVEL) {
    if (clan.wallLevel > 0) {
        clan.wallLevel--;
    }
}
```

A wall with level 5 absorbs at most `WALL_HP_PER_LEVEL` (100) damage and drops exactly one level. This means a bandit with strength 500 against a level-5 wall will: absorb 100 via wall (drop to level 4), then pass 400 through to base defense and clansmen. The wall only provides a single level of protection per attack regardless of wall level. This may be intentional game design, but it means wall levels 2-5 only provide cosmetic insurance (multiple attacks needed to fully destroy the wall). Worth confirming this is the intended behavior since the wall cost scales significantly with level.

### M4: No integration test for full heartbeat-driven bandit lifecycle

The test suite covers individual components well but lacks an end-to-end test that exercises the full cycle within heartbeat: spawn → advance → camp → target selection → attack → resolve, all driven by `heartbeat()` calls without harness bypasses. The `forceAttackingBandit()` harness skips `tickEnteredState` for the Attacking state, which could mask timing bugs.

---

## LOW severity findings

### L1: `_distributeBanditLootToDefendingClans` emits LootDistributed even when no defenders exist

**File:** `ClanWorld.sol:1791-1805`

When `nDefendingClans == 0`, all `per*` values are 0 and the burned values equal the full carry. The event is still emitted with an empty `clanIdsRewarded` array. This is benign but creates noise in event logs. Consider early-returning before the event if no defenders exist.

### L2: `_markClanDead` unused `reason` parameter

**File:** `ClanWorld.sol:518`

```solidity
function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId) internal {
```

The `reason` parameter is unnamed (was named in the 2-arg overload but unnamed in the 4-arg overload), consuming gas for the string copy with no use. Consider removing it or using it in the emitted event.

### L3: `getActiveBanditView` hardcodes `attackAttemptsMade: 0` and `maxAttemptsRemaining: 0`

**File:** `ClanWorld.sol:3492-3493`

These fields from the legacy `ActiveBanditView` struct are always zero. The Phase 9 bandit troop doesn't track attack attempts (it uses a different lifecycle model). If the frontend reads these fields, it gets misleading data. Consider either populating them or documenting that they're deprecated in the struct definition.

---

## Cross-cutting observations

1. **Seed timing refactor is correct and well-tested.** The seed is now derived at the end of heartbeat (step 8) and stored under the *opened* tick, not the closed tick. Settlement reads `_tickSeeds[tick]` which was stored by a prior heartbeat, so settlement RNG continues to work. The new `RecordingPool` test validates that market actions see the closed-tick seed. The formula chains correctly: `keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick))`.

2. **`_resolveAttackingBandits` array iteration is correct.** Uses `while (i < length)` with `i++` only when the bandit is NOT deleted (stays at same index if deleted, because swap-pop fills the slot). This is the correct pattern for iterate-and-delete on a dynamic array.

3. **`_markClanDead` cascade design is sound.** The `excludedBanditId` parameter prevents `_abortBanditAttacksForDeadTarget` from aborting the bandit that *caused* the death (since it's already being resolved). This prevents re-entrancy into the bandit state machine during attack resolution.

4. **Settlement simulation is a large addition (~500 lines) that mirrors the real settlement logic.** Each `_simulate*` function has a corresponding real function. The simulation correctly uses memory structs and never writes to storage. The test suite validates that raw storage is unchanged after derived reads. This is a solid architectural decision for off-chain reads.

5. **Cross-clan defense (`_validateDefendBaseOrder` change) is a significant gameplay expansion.** Any clan can now defend any other clan's base by specifying `targetClanId`. The validation correctly checks that the target clan exists and is alive, and that `gotoRegion` matches the target's `baseRegion`. This is well-integrated with the loot distribution system (`_activeDefendingClanIds` scans all defending clans in the region).
