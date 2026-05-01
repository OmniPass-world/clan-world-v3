I have enough to write the final review. Spec confirms HIGH-severity carry-vs-vault deviation.

# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY

**Verdict: NEEDS_FIXES.** Phase 6 ships a working immediate + scheduled market with FIFO ordering, k-invariant-checked AMM, deterministic heartbeat ordering, and 116/116 green tests. But the resource-side accounting **inverts the spec**: §8.3-8.4 require SELL to source from worker carry and BUY to credit worker carry, while the implementation drains/credits the clan vault on both sides — and the BUY path is even internally contradictory (validates `_remainingCarryForToken` then writes to `_addToVault`). Two other meaningful concerns: the per-tick scheduled-queue cap was deleted in the fix-round leaving an O(n²) storage-sort with no upper bound, and the `MinimalERC20.mint/burn` "boundary" is dead code never invoked by the engine.

## HIGH severity findings

### H1 — Market action source/destination violates spec §8.3 + §8.4 (carry vs vault)

Spec text (`docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md` §8.3-8.4):
- `MarketSell` — *"source = worker carry balance, destination = clan gold purse"*
- `MarketBuy` — *"source = clan gold purse, destination = worker carry balance"*
- v4.3 §K: *"`ERR_CARRY_FULL` is especially relevant for explicit market buy validation… requested output cannot fit remaining carry capacity"*

Implementation in `packages/contracts/src/ClanWorld.sol`:
- Immediate sell (`_executeImmediateMarketSell`, lines 1657 + 1669): checks `_hasVaultBalance` then `_deductFromVault` — pulls from **vault**, never reads `cs.carryX`.
- Scheduled sell (`_executeMarketSell`, line 1825): same `_deductFromVault`.
- Immediate buy (`_executeImmediateMarketBuy`, lines 1724 + 1773): validates `amountOut > _remainingCarryForToken(cs, token)` (correct per spec), then on success calls `_addToVault(clan, token, amountOut)` — credits **vault**, never touches `cs.carryX`.
- Scheduled buy (`_executeMarketBuy`, line 1885 area): same pattern.

The buy path is **internally contradictory** — it gates on worker carry capacity but mutates vault, so the carry-cap check no longer reflects what the action actually does. Practical consequences: a worker with full carry but an empty vault is rejected from buying; a worker physically present at UT with carry to sell is irrelevant — only the clan's homebase vault is consulted (vault is at the homebase, far from UT).

This breaks the game's physical fiction and the spec's accounting domains (§4.3 distinguishes "worker carry" from "vault"). Tests (`test_immediateMarketSell_executesInSubmitTx` etc.) assert against `vaultWood`, so the test suite has codified the wrong semantics — fixing the impl will require updating those assertions too.

### H2 — Scheduled-queue cap removed; insertion sort on storage is now unbounded O(n²)

Diff removes `MAX_MARKET_ACTIONS_PER_TICK = 32` and the deferred-overflow logic. `ClanWorld.sol:1379-1457` now sorts and processes the full queue. `_sortScheduledMarketActionsByCommitSequence` (`ClanWorld.sol:1459-1469`) is **insertion sort directly on a `storage` array**, so each shift on line 1464 is a struct-wide SSTORE swap (`ScheduledMarketAction` has 9 fields). Worst case at the documented 12-clan × 4-clansman cap = 48 entries ≈ 1128 shifts × ~9 fields ≈ ~10K SSTOREs just for the sort, before the 48 try/catch external swap calls. Plausibly fits in a 30M-gas block at the Phase-6 cap, but there's no guard if the cap ever rises (or if multiple ticks worth converge — e.g. several fast-traveling clans with aligned `arrivalTick + 1`). Either reinstate a per-tick cap or sort in memory and write back.

### H3 — `MinimalERC20.mint()` / `burn()` are dead code; the "boundary" is vestigial

Grep across `packages/contracts/src/` for `.mint(` / `.burn(` returns zero call sites in `ClanWorld.sol`. The only mint path that ever runs is `seedTreasury → _mint` (deployer-only, called once at deploy). The `onlyEngine`-gated external `mint`/`burn` are never invoked by the engine — vault accounting stays in storage scalars (`vaultWood += …`, etc.) and ERC20 transfers happen only at pool seeding (`_transferSeed` → `transferFrom`). If the prize narrative or indexer expects on-chain ERC20 mint events when wood is gathered or burn events on vault drain, this is a silent gap. If the design is genuinely "ERC20 only at the AMM boundary, scalars elsewhere", delete the dead surface (and the `boundaryConfigured`/`engine` storage that backs it) — keeping it advertises a contract you don't honor.

## MEDIUM severity findings

### M1 — Immediate market failure is invisible in the synchronous return value

`ClanWorld.sol:1217-1228`: `_executeImmediateMarket` returns a `StatusCode`, but the caller writes `result.status = StatusCode.OK` and `result.marketMode = MarketExecutionMode.Immediate` unconditionally. A failed immediate swap (e.g. `ERR_NOT_ENOUGH_GOLD`, `ERR_LIQUIDITY_INSUFFICIENT`, `ERR_MAX_GOLD_IN_EXCEEDED`) is signalled only via the `MarketActionFailed` event. Tests confirm this is current behavior (`test_immediateMarket_insufficientLiquidityFailsAndConsumesCooldown` asserts `r[0].status == OK`). The Convex indexer/frontend cannot tell from the tx return whether the trade settled — it must subscribe to the failure event in the same tx. Either propagate the inner status into `result.status` or document the asymmetry vs the spec's "results are returned per-clansman" implication (§12.1).

### M2 — Test coverage is wood-only on the four-resource market

Per the test-coverage agent: only the wood pool is exercised in `test_immediateMarketSell/Buy_*`, `test_buy_*`, `test_sell_*`, `test_scheduledMarket_fifo`, `test_scheduledMarket_executesAllActionsForClosedTick`, and `test_kInvariantHoldsAfterEachSwap`. Iron, wheat, and fish pools are deployed and seeded in every fixture but never traded. Asymmetric pool seed sizes between resources (e.g. `WOOD_CARRY_CAP = 10e18` vs `WHEAT_CAP = 40e18`) increase the chance a per-resource bug would only surface for wheat/fish/iron. Add at least one happy-path scheduled buy + sell for each.

### M3 — `test_scheduledMarketFailure_doesNotAffectAnotherClan` does not actually prove same-tick isolation

In `ClanWorld.t.sol`, the test computes `tick1 = world.getActiveMission(csId1).settlesAtTick` and `tick2 = world.getActiveMission(csId2).settlesAtTick` independently and walks `while (currentTick <= maxTick)`. There is no `assertEq(tick1, tick2, ...)`. If the two clans happen to have different baseRegion travel distances to UT, the failing buy and the OK sell land on different ticks, and the test passes by trivially proving cross-tick independence — not the same-tick FIFO-poisoning property the name implies. Add `assertEq(tick1, tick2)` (force the configuration if needed) and assert that both events fire in the same heartbeat.

### M4 — No test for bandit-killed clansman with a queued scheduled market action

Phase 9 ships bandits that can kill clansmen between mission submission and settlement. `_executeScheduledMarketActions` correctly checks `cs.state == ClansmanState.DEAD` (line 1391) and emits `ERR_INVALID_CLANSMAN`, but no test exercises the seam. With the bandit suite landed in PR #266, this integration point should have at least one regression test — kill the clansman after enqueue, run heartbeat, assert the queue entry no-ops and surrounding entries still execute.

### M5 — `_validateAction` line 2040-2042 is a comment-only dead block; no `cs.state` gate on the scheduled path

```solidity
if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
    // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
}
```
No code, just a comment. The branch was likely a refactor leftover. Delete it. Separately, the validator never gates market orders on `cs.state` — a worker mid-mission (`TRAVELING` or `ACTING`) accepts a new market order and the existing mission is interrupted. That's the standard interruption path and probably intended, but worth a one-line comment confirming.

## LOW severity findings

### L1 — `ResourcesDeposited.tick` is `uint32` (event ABI breaking change with cast)

`IClanWorld.sol:541`: changed from `uint64 atTick` to `uint32 tick`, with a `forge-lint: disable-next-line(unsafe-typecast)` at `ClanWorld.sol:457`. Truncates after `2³² - 1` ≈ 4.3B ticks (~2700 years at 20s cadence). Acceptable for hackathon, but the event signature change will break any indexer ABI generated against pre-PR ABIs.

### L2 — `Mission.nonce` is not zeroed in `_completeMission`

`ClanWorld.sol:856-862` only flips `m.active = false`. The execution-time guard at line 1407 (`m.nonce != sma.missionNonce`) works because every `_installMission` bumps the nonce, but the invariant is implicit. Add a comment or zero-out for defensiveness.

### L3 — `marketStatus;` no-op-statement in heartbeat try blocks (lines 1425, 1442)

Used to suppress "unused return value" — replace with `_ = marketStatus` or annotate, since the bare expression statement looks like dead code at first read.

### L4 — `MarketSell` has no slippage protection (`minGoldOut`)

Per spec §8.3 ("no minOut in v1") this is **spec-compliant**. Worth flagging because it's an asymmetry with BUY's `maxGoldIn` and a soft attack surface for adversarial elders that try to sandwich same-tick scheduled sells with their own immediate trades (spec §11.2 explicitly admits immediate-may-front-run-scheduled). Document that this is intentional v1 behavior in the elder toolbelt docs so agents don't expect price guarantees on sells.

### L5 — `ClanWorldStub.getPrice()` always returns 0

Fine for backend mocks but indexers/Convex shouldn't read prices from stub mode without checking `CLAN_WORLD_USE_STUB_CHAIN`. Worth a one-line comment in `ClanWorldStub.sol`.

### L6 — Empty deposit silent no-op (no zero-delta event)

`_doDeposit` (line 678) returns silently without emitting. Documented in code, but Convex `getMissionTiming`/event consumers that rely on always-on deposit events for "mission complete" need a different signal (`MissionCompleted` does fire from `_completeMission`, so OK in practice).

## Cross-cutting observations

- **Heartbeat ordering is correct.** Settle (step 1) → market exec (step 2) → world events (step 4) → tick increment (step 5) is verified by `HeartbeatOrdering.t.sol::test_heartbeat_settlementBeforeMarket` and `test_heartbeat_multipleStepsInOneTick`. Same-tick deposit-then-sell works because deposit's `_addToVault` happens before the queued sell drains the vault.
- **Scheduled enqueue + execute happen in the SAME heartbeat** (executeAtTick = settlesAtTick = arrivalTick + 1). This is a 1-tick delay vs the OLD `executeAtTick = arrivalTick`, but spec §11.3 only requires "is indexed under `executeAtTick` … executes eagerly at the corresponding heartbeat" — the new design is spec-compliant and conceptually cleaner (action duration is honored uniformly).
- **FIFO determinism holds.** `commitSequence` is assigned at `_installMission` time (submission); enqueue order at settlement is deterministic by clanId-then-clansmanId iteration; the in-storage insertion sort then reimposes global FIFO. Tested by `test_scheduledMarket_fifo`.
- **Re-entrancy is safe.** `heartbeat()` and immediate paths are `nonReentrant`; pool swaps don't make external calls (only update internal reserves); seed-time ERC20 transfers happen before `seedPools` finishes and the pool's `_seeded` flag prevents re-seeding.
- **K invariant is enforced** post-swap (`StubPool.sol:74, 94`) but only spot-tested for the wood pool — extend coverage to all four pools.
- **Treasury seeding integrity:** the `_transferSeed → MinimalERC20.transferFrom` chain plus pool's `balanceOf` check at `seed()` is correct — pool reserves cannot exceed actually-deposited balances.

**Recommended pre-merge actions:** fix H1 (carry vs vault) — this is the only finding I'd hold the merge for. Reinstate a per-tick cap or memory-sort for H2. Decide whether H3 should be deleted or wired up. M1-M5 can land as follow-up issues if time-boxed by the deadline.
