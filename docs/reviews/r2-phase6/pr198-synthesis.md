# Phase Super-Swarm Synthesis — PR #198 (head 9f93593)

**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✗ (quota) | Opus 4.6 (skipped)
**Phase:** dev-phase-6-market — Unicorn Town Market
**Diff size:** 3747 lines

## Summary

**Verdict: NEEDS_FIXES — 1 cross-model HIGH (3/3 consensus) + 2 single-model HIGH + several MED.**

The market plumbing is structurally sound (immediate + scheduled FIFO, k-invariant AMM, heartbeat ordering preserved, 116/116 tests). All 3 reviewers independently flag the same fundamental finding: **market trades operate on the clan vault, not worker carry — violating spec §8.3-8.4.** The impl + tests are mutually consistent but the spec contract is broken. Implications cascade into bandit loot timing + physical-presence game design.

Two additional single-model HIGHs from Opus 4.7: scheduled-queue cap removed leaves O(n²) storage-sort unbounded; `MinimalERC20.mint/burn` boundary is never called from the engine (dead surface).

**Recommendation needed from Liam:**
- Path A (spec-correct): rewrite trades to operate on worker carry. Invasive — touches 4 functions + ~15 tests + bandit interaction. ~1-2 hour fix-round.
- Path B (impl-correct): patch spec §8.3-8.4 to match vault-based trades. Document deviation. Zero code change. Spec amendment.

## MUST FIX (cross-model consensus)

| File:line | Models | Severity | Finding |
|---|---|---|---|
| H1: `ClanWorld.sol:1657, 1669, 1825, 1773, 1885+` | 5.4 + 5.5 + 4.7 = **3/3** | HIGH | **Market sell/buy bypass worker carry, mutate vault directly.** Spec §8.3: sell = worker carry → gold purse. §8.4: buy = gold purse → worker carry. Impl: sells `_deductFromVault`, buys `_addToVault`. BUY internally inconsistent — validates `_remainingCarryForToken` (correct gate) then writes to vault. Tests codify the broken behavior. Fixing requires test updates too. **Game-design impact:** worker physically in Unicorn Town is irrelevant — only homebase vault is consulted. Bandit loot timing changes (vault drains immediately vs eventually). Bought goods are instantly upkeep-eligible without a return trip. |

## SHOULD FIX (single-model HIGHs)

| File:line | Models | Severity | Finding |
|---|---|---|---|
| H2: `ClanWorld.sol:1173-1178` | 5.5 = 1/3 | HIGH | **Cooldown bypass via scheduled fallback.** Worker can complete an immediate trade, enter cooldown, then submit another market order that falls into scheduled mode despite cooldown. Scheduled fallback should not become a cooldown escape hatch. |
| H3: `ClanWorld.sol:1379-1457, 1459-1469` | 4.7 = 1/3 | HIGH | **`MAX_MARKET_ACTIONS_PER_TICK` removed in r1 fix-round → unbounded insertion sort on storage array.** Within 12-clan × 4-clansman cap (48 entries) it fits ~30M gas, but no guard if the cap rises or multiple ticks converge. Either reinstate cap OR sort in memory + write back. |
| H4: dead `MinimalERC20.mint/burn` | 5.4 + 4.7 = 2/3 | HIGH/MED | **ERC20 boundary is vestigial.** Engine never calls `mint`/`burn` on resource tokens. Pool seeding is the only mint path. Vault accounting stays in storage scalars. If the design is "ERC20 only at AMM boundary", delete the unused mint/burn surface. If it's "ERC20 mirrors vault", wire it up. |

## DEFER / Document

(MEDIUM and LOW from each model — full details in per-model review files. Common themes: queue visibility lag for indexer, immediate-action mission state surface, status code naming consistency.)

## Per-model verdicts

- **Codex 5.4:** NEEDS_FIXES — 2 HIGH (vault/carry + ERC20 boundary), MED on queue/scheduled timing
- **Codex 5.5:** NEEDS_FIXES — 2 HIGH (vault/carry + cooldown bypass), MED on lifecycle
- **Opus 4.7:** NEEDS_FIXES — 3 HIGH (vault/carry + queue sort + ERC20), several MED

## Cross-model overlap stats

- 3/3 consensus: vault-vs-carry (HIGH) — strongest signal
- 2/3 overlap: dead ERC20 boundary (HIGH/MED)
- 1/3 only: cooldown bypass + queue sort

## Decision matrix

If Liam says **fix to match spec (Path A):**
1. Rewrite `_executeImmediate*Market*` + `_executeMarket*` to use `cs.carryX` not `clan.vault*`
2. Update all market-flow tests to assert on carry, not vault
3. Add a "physical presence" check: market actions only valid when `cs.currentRegion == REGION_UNICORN_TOWN` (already there?)
4. Verify bandit-loot timing tests still pass (vault changes vs carry changes)
5. Address H2 + H3 + H4 in same round
6. Re-super-swarm

If Liam says **patch spec to match impl (Path B):**
1. Add a v4.6 spec amendment: "Market trades use clan vault as source/dest, not worker carry"
2. Document tradeoff: simpler impl, instant bandit-loot-target updates, but loses physical-presence enforcement
3. Address H2 + H3 + H4 only (smaller fix-round)
4. Re-super-swarm OR ship per Liam directive

If Liam says **ship as-is (no more rounds):**
1. File H1-H4 as follow-up issues
2. Mark Phase 6 ready for UAT
