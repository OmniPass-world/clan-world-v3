# Phase Super-Swarm Synthesis (R2) — PR #198 (head fdcc04b)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.7 ✓ (Opus 4.6 + Gemini 3.1 Pro skipped — duo+ scaled-down sufficient given clear cross-model overlap)
**Phase:** dev-phase-6-market — Unicorn Town Market (carry-based rewrite from R1)
**Diff size:** ~1500 lines (PR #284 + 3 follow-ups)

## Summary

**Verdict: NEEDS_FIXES — STRONG signal. R1's vault-vs-carry HIGH was fixed; R2 surfaced a NEW HIGH (sell-side submit validation missing) plus a critical scope question about the wheelbarrow mechanic.**

The R1 fixes all landed cleanly — Opus 4.7 verifies all four R1 HIGHs are genuinely closed (vault→carry rewrite, cooldown bypass, sort O(n²) on storage, ERC20 mint/burn boundary). But the R2 review surfaced cross-model agreement on a HIGH that R1 didn't catch + reignited the wheelbarrow question.

## MUST FIX — cross-model overlap

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H1 | `ClanWorld.sol:2096-2098`, `:1217`, `:1225`, `:2077`, `:2095`, `:1887`; `ClanWorld.t.sol:2533` | 5.4 + 5.5 + 4.7 = **3/3** | **HIGH** (4.7 grades it MED) | **Sell-side submit-time carry validation missing.** `_validateAction` rejects `MarketBuy` at submit if carry can't hold the bought resource (line 2096) but has NO symmetric pre-check for `MarketSell`. A sell with insufficient carry passes validation, installs mission, travels to UT, then fails at `_executeMarketSell:1887` with `ERR_MISSING_RESOURCES` AFTER the mission burned its tick + cooldown. Plus: `_processOrder` for immediate sells discards the failure return and still returns `StatusCode.OK`, so callers are told the trade succeeded when it didn't. **Fix:** add `if (action == MarketSell && _carryBalance(cs, tok) < order.marketAmount) return StatusCode.ERR_MISSING_RESOURCES;` symmetric to the buy precheck. Also fix the immediate-sell failure propagation back through `_processOrder`. |

## DECISION NEEDED — Wheelbarrow vault→carry path (2/3 HIGH, 1/3 SCOPE)

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H2/SCOPE | `ClanWorld.sol:668, 1237, 1245, 1556, 1858, 2184`; `IClanWorld.sol:134-149` (ActionType enum) | 5.4 + 5.5 = HIGH; 4.7 = SCOPE | DECISION | **Wheelbarrow mechanic only half-implemented.** Carry→vault deposit works (`DepositResources`), but NO production `vault→carry` withdraw path exists. `transferVaultResource()` still reverts. Practical consequence: starter/deposited resources are TRAPPED in the vault — a clan cannot move stockpiled wood to a worker's carry to take to market, or to load up before a bandit raid. Tests bypass this by using harness `setCarry`. **Codex view:** "the intended wheelbarrow anti-bandit mechanic is not actually implementable" — HIGH because Phase 6's R1→Path C reroute was specifically to enable wheelbarrow semantics. **Opus view:** spec gap not PR bug — would need new `WithdrawResources` action type, out of scope for Phase 6. **Liam decision: was Phase 6's Path C scoped to deliver wheelbarrow end-to-end (i.e. add `WithdrawResources` action), or just to fix the vault-vs-carry execution path? If end-to-end → fix-round adds `WithdrawResources`. If execution-only → file as Phase 6B + ship.** |

## SHOULD FIX (MEDIUM)

| # | File:line | Models | Finding |
|---|---|---|---|
| M1 | `ClanWorld.sol:469, 1314, 1321`; `IClanWorld.sol:541` | 5.4 + 5.5 = 2/3 | **Scheduled market queue no longer observable between submit and execution.** `commitSequence` is captured at submit, but `ScheduledMarketAction` is only pushed during mission settlement, immediately before heartbeat market execution + then `delete`d. `getScheduledMarketActionsForTick()` and `getMarketState().nextTickQueue` cannot preview pending trades anymore. Indexers + UI consumers expecting the old queue lifecycle will see empty results. Either restore the documented submit-time enqueue OR update spec + consumers. |
| M2 | `ClanWorld.sol:1832-1834, 1994-2009` | 5.5 = 1/3, 4.7 = 1/3 (LOW) | **Buy-path CEI inverted.** External pool swap happens BEFORE `clan.goldBalance` debit + `cs.carryX` credit. Sells follow proper CEI; buys are asymmetric. Mitigated by `nonReentrant` + `StubPool` `onlyEngine` + internal accounting, but worth normalizing for symmetry + future-proofing if pool is replaced by a real ERC20 router. |

## DEFER (LOW — file as polish PR)

- L1: Misleading test name `test_immediateMarket_townOnCooldown_fallsBackToScheduled` — actually asserts cooldown REJECTION not fallback. Rename to `…_rejected`.
- L2: Dead `ResourceMinted` / `ResourceBurned` events declared in `IClanWorld.sol:590-591`, never emitted anywhere. Vestigial after MinimalERC20 boundary removal. Delete or wire when blueprint mint/burn returns in Phase 8.
- L3: Redundant cooldown clause in immediate-eligibility gate at `ClanWorld.sol:1212` — execution can only reach 1210 after cooldown gate at 1177 already returned. Dead code.
- L4: Stale doc comments at `ClanWorld.sol:1858, 1914` reference "vault" — both functions now operate on carry. Update.
- L5: Empty `if` block in `_validateAction` at `ClanWorld.sol:2102-2104` — informational comment only.
- L6: `_emitMarketActionFailed` 1-line wrapper indirection — style.
- L7: Empty-carry/full-vault sell test at `ClanWorld.t.sol:2533` is vacuous (never calls `_setupMarket()`, fails on token=0 not on empty-carry assertion). Rewrite or delete.

## Cross-cutting observations

- **R1 HIGHs all genuinely closed (4.7 verified).** Vault→carry rewrite, cooldown bypass, sort O(n²) on storage, ERC20 boundary — all fixed. PM execution on Path C was correct on the surface area covered.
- **Phase 5/9 integration: clean.** Phase 9 (bandits) is currently a pure stub. When Phase 9 lands, carry-based market means a clansman in transit to UT carries proceeds the bandit cannot loot — wheelbarrow semantics emerge naturally from the architecture EXCEPT for the vault→carry pre-load step (H2 above).
- **`WOOD_CRIT_BPS` 2000 → 1000 (20% → 10%).** Visible balance change at `IClanWorld.sol:53`. With `WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK = 1e18`, a crit doubles yield. Liam ack but not correctness.
- **Constant aliasing is consistent** — both new and legacy names point at same values.
- **Test coverage on the rewrite is solid** for what's implemented (vault-vs-carry separation, FIFO, cap removal). Misses the wheelbarrow vault→carry path (because it doesn't exist) and the sell-side submit validation (H1) edge.

## Per-model verdicts

- **Codex 5.4:** NEEDS_FIXES — 2 HIGH (sell validation + wheelbarrow trap) + 2 MED + 1 LOW.
- **Codex 5.5:** NEEDS_FIXES — 2 HIGH (same as 5.4) + 2 MED + 2 LOW.
- **Opus 4.7:** APPROVE — HIGH CLEAN, 1 MED (sell validation), 7 LOW. Treats wheelbarrow as out-of-scope spec gap.

## Cross-model overlap stats

- 3/3 consensus: H1 sell-side submit validation (codex calls HIGH, opus calls MED — same finding, different severity weight)
- 2/3 consensus: H2 wheelbarrow trap (codex HIGH, opus scope) — REQUIRES LIAM DECISION
- 2/3 consensus: M1 scheduled queue observability
- 1/3: various LOW polish items + buy-path CEI

## Decision

**Recommend Path A (fix-round) for H1 + Liam decision on H2.**

H1 is clear-cut — all 3 models agree it's a real bug, codex says HIGH, opus says MED. Either way it ships submit-time UX that lies to Elder agents (returns OK on failure).

H2 is the question. Phase 6 R1 was rejected because Liam directed Path C: "The game mechanics will be broken if we don't do A and fix it. Or actually let's do C..." The carry-based rewrite was meant to fix it. PM delivered carry→vault (deposit) but not vault→carry (withdraw). The wheelbarrow anti-bandit mechanic still doesn't work end-to-end.

**Two paths:**
- **A1 (recommended for design fidelity):** This fix-round adds `WithdrawResources` action type + vault→carry path. Closes the wheelbarrow design loop in Phase 6 itself. ~30-60 min codex work.
- **A2 (recommended for shipping speed):** This fix-round handles H1 + M1 + LOWs only. File `WithdrawResources` action as Phase 6B + ship Phase 6 now. Wheelbarrow becomes a Phase 6B follow-up.

**Liam: A1 or A2?**

(M1 + LOWs roll into either path. M2 buy-path CEI inversion is mitigated by `nonReentrant` + engine-only pool — fine to file as Phase 6B polish.)
