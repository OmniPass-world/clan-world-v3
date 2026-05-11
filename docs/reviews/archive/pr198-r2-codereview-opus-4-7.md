All three agents have reported. Synthesizing the findings into the final review now.

# Phase Super-Swarm Review (R2) — PR #198 (head fdcc04b)

## SUMMARY

**Verdict: GREEN with one MEDIUM and several LOW polish items.** The four R1 HIGH bugs are all genuinely closed:

1. **Vault→Carry rewrite (HIGH #1)** — verified. All four execution paths (`_executeImmediateMarketSell` 1678-1753, `_executeImmediateMarketBuy` 1755-1856, `_executeMarketSell` 1859-1912, `_executeMarketBuy` 1915-2022) touch only `cs.carryX` for the resource leg and `clan.goldBalance` for the gold leg. Zero references to `_addToVault`/`_deductFromVault`/`_hasVaultBalance` from any market path; vault is no longer mutated by trades.
2. **Cooldown bypass (HIGH #2)** — verified. `ClanWorld.sol:1177-1182` returns `ERR_COOLDOWN_ACTIVE` *before* the immediate-market block at 1210-1230 AND before the mission-install path at 1232+. There is no path where a market order on cooldown gets silently scheduled. `test_marketCooldown_noBypassViaScheduled` (test:2566) tests the exact (WAITING + UT + cooldown-active) condition.
3. **Sort O(n²) on storage (HIGH #3)** — verified. `_sortScheduledMarketActionsByCommitSequence` (1459-1478) allocates a memory array, copies storage→memory once, sorts in memory, writes back to storage once. `MAX_MARKET_ACTIONS_PER_TICK` and the deferred-overflow path are removed; the queue executes in full per tick and is `delete`d after. Worst-case n=32 (8 clans × 4 clansmen) makes O(n²) memory comparisons trivial.
4. **Dead ERC20 boundary (HIGH #4)** — verified. `MinimalERC20.mint`/`burn` removed; replaced with one-shot `seedTreasury` (guarded by `treasurySeeded` bool, `onlyDeployer`). `Deploy.s.sol:57-67` and `seedPools` (2145-2169) correctly route deployer→treasury→engine→pool via `transferFrom`. `_mint` is internal-only.

CEI on `_executeImmediateMarketSell` correctly restores carry via `_addToCarry` in the catch block (1742-1743). The "vacuous test" was rewritten as `test_immediate/scheduledMarketBuy_overCapacityRejectsAtSubmit` and now asserts five meaningful properties (vault, gold, cooldown, state, mission-not-installed).

Forge build passes (one unused-import lint, unrelated to this PR).

## HIGH severity findings

**CLEAN — no findings.**

## MEDIUM severity findings

**M1. Missing sell-side carry pre-check in `_validateAction`.** `ClanWorld.sol:2096-2098` rejects `MarketBuy` at submit-time if `marketAmount > _remainingCarryForToken(cs, tok)`, but there is **no symmetric pre-check for `MarketSell`**. A sell submitted with insufficient carry passes `_validateAction` cleanly, installs a mission, travels to UT, then fails at `_executeMarketSell:1887` with `ERR_MISSING_RESOURCES` after the mission has burned its tick and cooldown. The buy precheck reasoning ("travel doesn't change carry, so submit-time carry == arrival carry") applies symmetrically — this is an asymmetry, not a deliberate design. Recommend adding `if (action == MarketSell && _carryBalance(cs, tok) < order.marketAmount) return StatusCode.ERR_MISSING_RESOURCES;` so Elders get deterministic submit-time feedback. Gameplay UX rather than correctness — no state corruption.

## LOW severity findings

**L1. Misleading test name.** `test_immediateMarket_townOnCooldown_fallsBackToScheduled` (test:812) actually asserts `ERR_COOLDOWN_ACTIVE` (line 821) and unchanged gold — it tests cooldown *rejection*, not fallback to scheduled. Companion test `test_immediateMarket_notInTown_fallsBackToScheduled` (test:826) is the real fallback test. Rename to `…_rejected` to avoid future confusion.

**L2. Dead events `ResourceMinted` / `ResourceBurned`.** Declared at `IClanWorld.sol:590-591` and present in the regenerated ABI, but never `emit`ed anywhere in `src/`. Vestigial after `MinimalERC20.mint`/`burn` removal. Either wire them when ERC20 mint/burn returns (Phase 8 blueprint?) or delete to keep the ABI honest.

**L3. Redundant cooldown clause in immediate-eligibility gate.** `ClanWorld.sol:1212` includes `&& block.timestamp >= cs.cooldownEndsAtTs`, but execution can only reach line 1210 after the cooldown gate at 1177 already returned for cooldown-active workers. The clause is dead-code (always true). Drop it or add a `// belt-and-suspenders` comment.

**L4. Stale doc comments referencing "vault" on scheduled market functions.** `ClanWorld.sol:1858` (`/// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.`) and `:1914` (`/// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.`). Both functions now operate on carry. Update to "carry" for accuracy.

**L5. Empty `if` block in `_validateAction`.** `ClanWorld.sol:2102-2104` contains `if (cs.currentRegion == REGION_UNICORN_TOWN && cs.state == WAITING) { /* comment only */ }` with no body. The comment is informational; safe to delete the `if`.

**L6. Buy-path CEI is inverted (Interactions before Effects), mitigated.** `_executeImmediateMarketBuy:1832-1834` and `_executeMarketBuy:1995-2009` perform the external pool swap *before* updating `clan.goldBalance` and `cs.carryX`. Sells follow proper CEI; buys are asymmetric. Currently safe because (a) `submitClanOrders` and `heartbeat` are `nonReentrant`, (b) `StubPool` is engine-controlled with `onlyEngine`, and (c) all state lives in internal accounting (no real ERC20 transfer per swap). Worth normalizing for symmetry and future-proofing if the pool is ever replaced by a real ERC20 router.

**L7. `_emitMarketActionFailed` 1-line wrapper indirection** (`ClanWorld.sol:1524-1533`). Wraps a single `emit MarketActionFailed(...)` call. Used 3 times. Style-only — keep or inline.

## Cross-cutting observations

- **Phase 5/9 integration: clean.** Phase 9 (bandits) is currently a pure stub — `_processBanditActions` does not exist, the bandit settle step is `// TODO Phase 3` at `:898-899`. No collision possible between bandit raids and same-tick market trades. When Phase 9 lands, the carry-based market means a clansman in transit to UT carries proceeds the bandit cannot loot — the *intended* "wheelbarrow" semantics emerge naturally from the architecture.

- **Wheelbarrow mechanic NOT implemented (out of scope).** The reverse direction (vault → carry, "fill carry from vault to save resources from a same-tick bandit raid") would require a new `WithdrawResources` action type. None exists in the `ActionType` enum (`IClanWorld.sol:134-149`). Today, the only path to fund a market trade is to gather resources directly into carry without depositing. **Spec gap, not a PR bug** — flag for future Phase planning.

- **`WOOD_CRIT_BPS` 2000 → 1000 (20% → 10%).** Visible balance change at `IClanWorld.sol:53`. With `WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK = 1e18`, a crit doubles yield. Worth a Liam ack but not a correctness issue.

- **Constant aliasing is consistent.** `WOOD_CAP = WOOD_CARRY_CAP = 10e18`, `WOOD_BASE_YIELD = WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK = 1e18`. Both new and legacy names point at the same values; no caller sees a stale interpretation.

- **`ERR_MARKET_BUY_MAX_GOLD_EXCEEDED → ERR_MAX_GOLD_IN_EXCEEDED` rename complete.** Zero stale references in `packages/` source or tests.

- **`getMissionTiming` ABI churn = sort reorder, not signature change.** Same inputs/outputs, just moved alphabetically in the JSON. New view functions `getResourceToken`, `getPool`, `getPrice` are exercised by `SeedPools.t.sol` and `ResourceBoundaryTokens.t.sol`.

- **Test coverage on the rewrite is solid.** `test_marketSell_deductsFromCarry_notVault` (t.sol:2496), `test_marketBuy_creditsCarry_notVault` (:2591), `test_marketSell_fails_emptyCarry_fullVault` (:2533) explicitly assert the vault-vs-carry separation. `test_scheduledMarket_executesAllActionsForClosedTick` (:1300) validates the cap removal with 12 clans × 4 clansmen. `test_scheduledMarket_fifo` (:1365) asserts amount-by-amount FIFO ordering from logs.

**Recommendation: APPROVE merge. Address M1 (sell-side submit-time carry check) in this PR if quick, or as a follow-up. L1-L7 are polish items suitable for a sweep PR.**
