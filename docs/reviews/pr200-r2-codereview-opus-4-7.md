I have enough context. Let me write the final review.

# Phase Super-Swarm Review — PR #200 (head 2bec876)

## SUMMARY

**NEEDS_FIXES** — but no HIGH-severity blockers. The OTC surface is structurally sound: nonReentrant guards everywhere, atomic balance checks, settle-before-debit (a fix-round addition that's correctly placed), and proper proposal-cap accounting via `_openOtcProposalsByClan`. The boundary-token redesign (deployer→engine permission split with one-shot `configureBoundary`/`seedTreasury`) is clean. The major non-OTC behavior change — wood gathering moving from per-tick to per-mission lump-sum yield — is correct: `_settleMissionForClansman` only invokes `_resolveAction` once when `tick >= m.settlesAtTick` (line 354 of head), so `WOOD_YIELD_PER_TICK * getActionDuration(ChopWood)` does not double-apply. The medium findings are mostly API hygiene and ABI inconsistencies that are cheap to fix now and expensive to fix post-merge. Recommend addressing the `expiryTick` type asymmetry pre-merge (fix #1 below); ship the rest as follow-up issues.

## HIGH severity findings

CLEAN — no findings.

## MEDIUM severity findings

### M1. `expiryTick` type asymmetry between gold and the other three OTC types

`packages/contracts/src/IClanWorld.sol` and `src/ClanWorld.sol`:

```solidity
function proposeGoldTransfer(uint32 fromClanId, uint32 toClanId, uint256 amount, uint256 expiryTick) ...
function proposeVaultTransfer(..., uint64 expiryTick) ...
function proposeBlueprintTransfer(..., uint64 expiryTick) ...
function proposeBundledTransfer(..., uint64 expiryTick) ...
```

Gold uses `uint256 expiryTick` (then casts to uint64 with overflow check). The corresponding event mirrors this:

```solidity
event GoldTransferProposed(..., uint256 expiryTick);     // uint256 here
event VaultTransferProposed(..., uint64 expiryTick);     // uint64
event BlueprintTransferProposed(..., uint64 expiryTick); // uint64
event BundledTransferProposed(..., uint64 expiryTick);   // uint64
```

This is a baked-in ABI inconsistency that will outlive the contract. Indexers and clients will need to special-case gold's expiry type. **Recommended fix**: change `proposeGoldTransfer` and `GoldTransferProposed` to `uint64 expiryTick` to match the rest. Cheap pre-merge, expensive (ABI break) post-merge.

### M2. `ResourcesDeposited` event field rename is a backward-incompatible ABI label change

`packages/contracts/src/IClanWorld.sol`:

```solidity
event ResourcesDeposited(
    uint32 indexed clanId,
    uint32 indexed clansmanId,
    uint256 woodDelta,    // was: wood
    uint256 ironDelta,    // was: iron
    uint256 wheatDelta,   // was: wheat
    uint256 fishDelta,    // was: fish
    uint64 atTick
);
```

The signature topic stays identical (param types unchanged), so old indexers won't crash — but any subgraph/GraphQL schema that references `event.params.wood` will return `undefined`. Empty-deposit no-op (zero-delta event suppression in `_doDeposit`) is also a behavior change indexers may rely on for "deposit attempted" metrics. **Recommended action**: document both changes in the release changelog under "Indexer-affecting changes" and bump indexer schema versions.

### M3. `CLANSMAN_CARRY_CAP` naming is misleading — only applies to wood

`packages/contracts/src/IClanWorld.sol`:

```solidity
uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
uint256 internal constant IRON_CAP = 5e18;
uint256 internal constant WHEAT_CAP = 40e18;
uint256 internal constant FISH_CAP = 8e18;
```

The "clansman carry cap" name implies a unified per-clansman carrying limit, but only `_gatherWood` actually uses it. Iron/wheat/fish still cap at their own values. A future contributor adding a new resource will likely apply `CLANSMAN_CARRY_CAP` and silently break game balance. **Recommended fix**: either rename to `WOOD_CARRY_CAP` to match scope, or actually unify the caps across all gather paths (likely a Phase 8+ scope change).

### M4. Inconsistent error API — string reverts vs StatusCode enum

OTC paths revert with string literals: `"ERR_NOT_ENOUGH_GOLD"`, `"ERR_ZERO_AMOUNT"`, `"ERR_SELF_TRANSFER"`, `"ERR_OTC_CAP"`, `"ERR_CLAN_DEAD"`. Pre-existing `submitClanOrders` returns these as `StatusCode` enum values in `OrderResult.status`. The PR even *adds* `ERR_ZERO_AMOUNT`/`ERR_SELF_TRANSFER`/`ERR_OTC_CAP` to the `StatusCode` enum (`IClanWorld.sol`), then uses them only as strings — a strong signal the choice is unintentional. Off-chain code will need two parallel decoding paths. **Recommended fix** (follow-up issue, not blocker): pick one mechanism (probably custom errors `error InsufficientGold(uint32 clanId, uint256 have, uint256 want)`) and migrate.

### M5. `blueprint` token deployed but never seeded or boundary-configured

`packages/contracts/script/Deploy.s.sol`:

The deploy script creates a `MinimalERC20("ClanWorld Blueprint", "BPRT")`, registers it in `initTreasury`, but never calls `configureBoundary` or `seedTreasury` on it. Result: zero total supply, no engine bound, no path to mint or burn. The ERC20 contract is dead-on-arrival. Clan blueprint accounting works fine because it's internal storage decoupled from the ERC20, but the on-chain token is unreachable. **Recommended action**: either remove blueprint from the deploy script for now (and add it back when a phase actually crosses the boundary), or call `configureBoundary` + `seedTreasury(0)` to lock the configuration into the intended Phase-X state. Document either way.

## LOW severity findings

### L1. `accepted`/`cancelled` proposal struct fields are dead state

All four proposal structs (`OtcProposal`, `VaultTransferProposal`, `BlueprintTransferProposal`, `BundledTransferProposal`) contain `bool accepted; bool cancelled;` fields, but accept/cancel paths immediately `delete _otcXProposals[proposalId]`. The flags are written-but-never-read. Either remove from structs (gas savings on storage write at propose time) or actually use them (long-lived audit trail without delete).

### L2. No `expiryTick > currentTick` check at propose time

A user can propose with `expiryTick = currentTick` (or in the past). The proposal occupies a cap slot until proposer cancels. Self-inflicted only; not a third-party attack. **Suggested**: `require(expiryTick > _world.currentTick, "ClanWorld: expiry not future");`.

### L3. Code duplication across 4 OTC types

Roughly 250 lines of near-identical propose/accept/cancel logic. A future fifth proposal type (e.g., bandit-takedown bounty?) will compound the surface. Refactor candidate for a follow-up issue: a single `_proposeTransfer<T>(T data, ...)` template using a discriminator-keyed mapping.

### L4. Mixed `if (...) revert(...)` and `require(...)` styles in the same function

`acceptGoldTransfer`:
```solidity
require(toClan.owner == msg.sender, "ClanWorld: not clan owner");
if (fromClan.goldBalance < amount) revert("ERR_NOT_ENOUGH_GOLD");
```

Both compile to the same revert. Pick one. (Same pattern in vault/blueprint/bundled accepts.)

### L5. `MinimalERC20.burn` underflow has no typed error

`burn(address from, uint256 amount)` does `balanceOf[from] -= amount; totalSupply -= amount;` without an explicit `require`. On underflow, Solidity 0.8.x reverts with `Panic(0x11)` rather than a meaningful message. UX nit; off-chain error decoding is harder. Suggest `require(balanceOf[from] >= amount, "MinimalERC20: insufficient");`.

### L6. Test gap: cap counter behavior for expired (uncancelled) proposals

`GoldTransferOtc.t.sol::test_goldTransfer_openProposalCapDecrementsAfterAccept` covers accept and cancel paths but never tests "8 proposals expire, none get cancelled, can the proposer recover?" Add a regression that fills the cap, lets all expire, then verifies that `cancelGoldTransfer` works post-expiry (which it does, because cancel doesn't check expiry — but the test doesn't pin that contract).

### L7. Test gap: cross-type proposalId collision

There's no test verifying that calling (e.g.) `acceptGoldTransfer` with a proposalId issued by `proposeVaultTransfer` reverts cleanly. Today it does (different mappings, default-zero `from` triggers `proposal not found`), but future refactors could regress this silently. Add one cross-type test per `accept*` function.

### L8. Wood crit semantics changed from additive to multiplicative

Old: `yield = WOOD_BASE_YIELD; if (crit) yield += WOOD_CRIT_BONUS;` (2e18 base + 1e18 = 3e18 on crit, ~33% boost).
New: `yield = WOOD_YIELD_PER_TICK * duration; if (crit) yield *= 2;` (4e18 base × 2 = 8e18 on crit, 100% boost), with crit rate halved 20% → 10%.

Long-tail variance is much higher under the new model. Likely intentional Phase-6/7 rebalance, but worth flagging for game-balance QA before mainnet. The `Gathering.t.sol::test_chopWoodCritDistributionAcrossSeeds` only verifies `[3, 20]` crit count out of 100 seeds — it doesn't check the magnitude or balance impact.

## Cross-cutting observations

- **`_settleClan` ordering inside accept paths is correct.** Pending mission outcomes (e.g., a deposit that's about to land) are settled before the OTC debit, so a transfer accepted at the same heartbeat as a settling deposit will see the post-settle balance. The fix-round commit message names this explicitly ("settle-before-debit"), and `VaultTransferOtcTest::test_acceptVaultTransfer_settlesPendingDepositBeforeDebit` covers it. Good.

- **Proposal-cap counter cannot underflow.** `_closeOtcProposal` guards with `if (openCount > 0)`. Defensive but reachable only via storage corruption.

- **No third-party DoS via cap counter.** Only `fromClan.owner` can propose from a clan; only the same owner can cancel from-side. Receiver cannot grief by ignoring proposals because the proposer can self-cancel any expired or unwanted proposal (cancel doesn't check expiry). Net: cap-related issues are self-griefing only.

- **StubPool k-invariant check is mathematically correct.** Integer-division rounds amountOut down (favoring pool) on exact-in, rounds amountIn up (favoring pool) on exact-out, so `reserveA' * reserveB' >= priorK` always holds. The runtime `require` is belt-and-suspenders against future swap-fee additions.

- **Boundary token model is sound but partial.** `MinimalERC20` correctly enforces one-shot `configureBoundary` + one-shot `seedTreasury` + engine-only `mint`/`burn`. Both flags are write-once. Gold and blueprint don't get boundaries (gold by design — fixed supply lives in pools; blueprint by oversight per M5).

- **Reentrancy posture is intentionally conservative.** None of the OTC paths make external calls today, but `nonReentrant` is correctly applied throughout — important once future phases (likely Phase 8+) start crossing the ERC20 boundary via `MinimalERC20.mint`/`burn` and ERC20 hooks become composable.

- **One ABI-level cleanup opportunity:** the alphabetical reordering of functions in `IClanWorld.json` (e.g., `getActionDuration` moved up, `getMissionTiming` moved to its current alphabetical slot) is a net diff churn but produces a stable ABI ordering for future diffs. No semantic effect.
EXIT=0
