# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY

**NEEDS_FIXES (MEDIUM-only).** The OTC proposal state machine is correctly implemented with proper reentrancy guards, balance-at-accept-time semantics, settle-before-debit ordering, and dead-clan checks. Test coverage is comprehensive including integration seams. Three MEDIUM findings: a type inconsistency in `proposeGoldTransfer` that breaks API uniformity across the four proposal types, missing access control on `seedPools` (now consequential with real ERC20 transfers), and an indexer-breaking event field rename. No HIGH bugs found — merge after addressing the MEDIUMs.

## HIGH severity findings

CLEAN — no findings.

## MEDIUM severity findings

### M1: `proposeGoldTransfer` expiryTick is `uint256` while all other propose functions use `uint64`

**File:** `packages/contracts/src/ClanWorld.sol` — `proposeGoldTransfer` signature, and `packages/contracts/src/IClanWorld.sol` — event + interface

`proposeGoldTransfer` takes `uint256 expiryTick` and emits `GoldTransferProposed(..., uint256 expiryTick)`. The other three propose functions (`proposeVaultTransfer`, `proposeBlueprintTransfer`, `proposeBundledTransfer`) all take `uint64 expiryTick` and emit with `uint64`. The overflow `require(expiryTick <= type(uint64).max)` prevents data loss, but:

1. **Inconsistent ABI** — callers need different parameter types for the same logical operation across proposal types
2. **Event schema mismatch** — indexers need a different decoder for gold proposal events vs all others
3. **Gas waste** — 256-bit calldata slot for a value that fits in 64 bits

**Suggested fix:** Change `proposeGoldTransfer` parameter and `GoldTransferProposed` event field to `uint64 expiryTick`, drop the overflow require.

---

### M2: `seedPools` lacks `treasuryOwner` access control — now consequential with real ERC20 transfers

**File:** `packages/contracts/src/ClanWorld.sol:1735`

```solidity
function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
    require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
    require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
    // no msg.sender == _treasury.treasuryOwner check
```

Previously, pools were math-only oracles — `seed()` just set reserves with no token movement. Phase 7 adds `_transferSeed` which does `transferFrom(msg.sender, pool, amount)`, making the pools hold real ERC20 balances. The `poolsSeeded` one-shot guard limits this to a single call, and the Deploy script calls `initTreasury` + `seedPools` atomically. But in any deployment scenario with a gap between the two calls, a front-runner with approved tokens could seed with adversarial reserve ratios.

**Suggested fix:** Add `require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not treasury owner")` to `seedPools`.

---

### M3: `ResourcesDeposited` event field names renamed — indexer breaking change

**File:** `packages/contracts/src/IClanWorld.sol:582`

Fields renamed from `wood, iron, wheat, fish` to `woodDelta, ironDelta, wheatDelta, fishDelta`. The Solidity event topic hash is type-based (not name-based), so on-chain signature is unchanged. But any indexer, subgraph, or frontend that uses ABI-generated name-based decoding will break when the ABI is updated. This should be called out in release notes.

**Suggested fix:** Coordinate rename with frontend/indexer deploy, or defer the rename to a planned breaking-changes release.

## LOW severity findings

### L1: Error code changed from `ERR_NOT_AT_HOMEBASE` to `ERR_INVALID_REGION` for deposit validation

**File:** `packages/contracts/src/ClanWorld.sol:1628`

The deposit-to-non-homebase validation now returns `ERR_INVALID_REGION` instead of `ERR_NOT_AT_HOMEBASE`. Any frontend mapping error codes to user-facing messages will show wrong text until updated. Non-blocking; note in changelog.

### L2: Expired proposals are never auto-cleaned — proposer must manually cancel

All four OTC proposal types remain in storage after expiry. The `_openOtcProposalsByClan` counter stays incremented until the proposer explicitly cancels. If a clan creates 8 proposals and lets them all expire without cancelling, they can't create new proposals until they cancel old ones. This is acceptable since cancel is always available (even for dead clans), but UX-unfriendly. Consider adding an `expired` check to the cap enforcement in a future phase.

### L3: No access control prevents proposing to a dead target clan at propose time

`proposeGoldTransfer` checks `_clans[toClanId].clanState != ClanState.DEAD` — this is actually correct, I see it in the code. Disregard this finding.

### L4: Cancel functions allow dead clans to cancel — intentional but worth documenting

Cancel functions check `fromClan.owner == msg.sender` but not clan state. The `DeadClanOtc.t.sol` test explicitly verifies this behavior. This is correct — dead clans should be able to clean up their proposals to free the cap counter.

## Cross-cutting observations

1. **Integration seam: settle-before-debit is correctly implemented.** All four `accept*` functions call `_settleClan(fromClanId)` and `_settleClan(toClanId)` before balance checks. The `VaultTransferOtcTest::test_acceptVaultTransfer_settlesPendingUpkeepBeforeDebit` validates that starvation-induced vault changes are reflected before the transfer check. The deposit integration test (`test_acceptVaultTransfer_settlesPendingDepositBeforeDebit`) confirms pending deposits land before OTC debits. This is the most important integration seam and it's solid.

2. **Shared `_nextOtcProposalId` counter across all 4 types.** Global IDs with type-specific storage means `acceptGoldTransfer(N)` and `acceptVaultTransfer(N)` can never collide — they hit different mappings and revert "not found" for the wrong type. Correct.

3. **`_closeOtcProposal` defensive guard** (`if openCount > 0`) prevents underflow but masks bugs. In normal operation the counter should never be 0 when close is called. Consider using `require(openCount > 0)` in debug builds to catch accounting bugs during testing.

4. **Test coverage is strong.** All four OTC types are tested for: propose/accept/cancel, expiry, balance changes, wrong callers, zero amounts, self-transfers, dead clans (both proposer and target), cap management, and integration with settlement. The gathering tests cover yield formula, crit distribution (statistical test across 100 seeds), carry cap clamping, and cooldown. Pool tests verify k-invariant and price preview accuracy.

5. **Fix-round regressions: none detected.** The formatting changes (whitespace cleanup, long-line wrapping) are cosmetic. The `DEPOSIT_DURATION_TICKS` extraction from the grouped action-duration block preserves the original 1-tick semantics. The `WOOD_CAP = CLANSMAN_CARRY_CAP` alias preserves backward compatibility for any code referencing `WOOD_CAP`.
EXIT=0
