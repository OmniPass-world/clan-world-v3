# Phase Super-Swarm Synthesis (R2 post-fix-round-merge) — PR #200 (head 2bec876)

**Models run:** Codex 5.3 ✓ | Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.6 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✓ — full 6-model lineup
**Phase:** dev-phase-7-otc — OTC Transfer Surface
**Diff size:** 4780 lines (post fix-round PR #290 merge)

## Summary

**Verdict: NEEDS_FIXES — 2 cross-model HIGHs (200-tick stale OTC + heartbeat-vs-lazy chronological) + 6/6 consensus on `expiryTick` type asymmetry MED.**

The OTC core is structurally sound — settle-before-debit is correctly placed (R1 fix landed clean), nonReentrant guards everywhere, atomic balance checks, proper proposal-cap accounting via `_openOtcProposalsByClan`, dead-clan handling. But two architectural-class HIGHs surfaced that are also flagged on Phase 10 + Phase 8 (cross-phase pattern):

- 200-tick settlement window cap means OTC accepts can transfer value against partially-settled state for stale clans
- Heartbeat mission settlement bypasses upkeep/cursor advancement — same family as Phase 8 H5 + Phase 10 H1

## MUST FIX — multi-model consensus

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H1 | `ClanWorld.sol:1809, :1891, :1975, :2065` (accept paths) + `:376-380` (settle cap) | Codex 5.4 + Codex 5.5 = **2/6** | HIGH | **OTC accepts trust partially-settled clan state; `_settleClan` is capped at 200 ticks/call.** If either party clan is more than 200 ticks behind, `_settleClan()` only catches up the most recent 200 ticks — accept then trusts balances and clan state. For vault/bundled transfers this can move wheat/fish/resources before overdue upkeep/starvation has fully been applied, corrupting balances and clan-state invariants. `submitClanOrders()` already guards this case via `MUST_SETTLE_FIRST`; OTC needs the same protection. **Fix:** at top of each `accept*Transfer()`, require `clan.lastSettledTick == _world.currentTick` (or loop-settle until current). Add tests where stale clans have OTC proposals + overdue upkeep. |
| H2 | `ClanWorld.sol:929-945` (`_settleCompletingMissions`) vs `:386-408` (`_settleClan`) | Codex 5.5 = 1/6 (cross-phase: same as P8 H5 + P10) | HIGH | **Heartbeat mission settlement bypasses normal upkeep/cursor advancement.** Heartbeat directly calls `_settleMissionForClansman()` for due missions but does NOT run per-tick `_applyUpkeep()` or advance `clan.lastSettledTick`. Lazy `_settleClan` applies upkeep first. A heartbeat-settled deposit credits the vault, then later `settleClan()` replays earlier ticks from stale cursor with deposited resources already present → retroactive upkeep payment, incorrect starvation outcomes. **Fix:** make heartbeat mission settlement use the same per-tick chronological pipeline; or update `_settleCompletingMissions()` to apply upkeep + advance cursor. Architectural — best handled in concert with Phase 8 R4 H5. |

## DE-FACTO MUST FIX — 6/6 consensus on grade-vs-MED

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| M1→H | `IClanWorld.sol:669, :804`, `ClanWorld.sol:1769, :1785` | Gemini = HIGH; Codex 5.3, 5.4, 5.5, Opus 4.6, 4.7 = MED. **6/6 noticed.** | DE-FACTO HIGH | **`expiryTick` type asymmetry — `proposeGoldTransfer` uses `uint256`, all other 3 use `uint64`.** `GoldTransferProposed` event also `uint256`, others `uint64`. Indexers/clients need different decoders for the same logical operation across the 4 proposal types. ABI break is cheap pre-merge, expensive post-merge. **Fix:** change `proposeGoldTransfer` parameter + `GoldTransferProposed` event to `uint64 expiryTick`; drop the overflow `require`. |

## SHOULD FIX (MEDIUM)

| # | Models | Finding |
|---|---|---|
| M2 | Codex 5.4 + Codex 5.5 = 2/6 | **Expired proposals still consume `MAX_OPEN_OTC_PROPOSALS_PER_CLAN`.** Counter only decrements on explicit accept/cancel; expired ones block new proposals until manual cancel. Add lazy reap on propose-time, OR allow new propose to reclaim expired slots, OR public expire/prune flow. |
| M3 | Codex 5.4 + Codex 5.5 + Opus 4.6 = 3/6 | **`initTreasury()` + `seedPools()` lack token/pool address validation + access control.** `seedPools` lacks `treasuryOwner` check (now consequential with real ERC20 transfers via `_transferSeed`). Plus `initTreasury` accepts arbitrary pool addresses without code-length / engine-pairing checks. Front-runner with approved tokens could seed adversarial reserve ratios in any deployment with init/seed gap. **Fix:** add `require(msg.sender == _treasury.treasuryOwner)` on `seedPools`; validate `code.length`, `ENGINE`, token pairing in `initTreasury`. |
| M4 | Codex 5.4 + Codex 5.5 = 2/6 | **StubPool token balances diverge from AMM reserves after swaps.** `seed()` verifies real ERC20 balances, but `swapExactInForOut` mutates only `reserveA/B`; token balances at pool never change. Pool/token surface misleading for explorers/indexers. Either keep as math oracle (don't expose ERC20 seam) or wire balance sync. |
| M5 | Codex 5.4 + Codex 5.5 + Opus 4.6 + Opus 4.7 + Gemini = 5/6 | **`ResourcesDeposited` event field rename `wood/iron/wheat/fish` → `woodDelta/.../atTick`** changes ABI labels (topic-0 unchanged since types unchanged). Subgraphs referencing `event.params.wood` return undefined. Coordinate indexer schema versions. |
| M6 | Codex 5.3 + Codex 5.4 + Codex 5.5 = 3/6 | **Legacy `transferGold/transferVaultResource/transferBlueprint/transferBundle` still in interface but hard-revert "OTC transfers not implemented".** Misleading public surface; client trap. Remove or implement as proposal-flow wrappers. |
| M7 | Codex 5.5 = 1/6 | `DepositResources` returns `ERR_INVALID_REGION` for non-homebase but other homebase-only actions return `ERR_NOT_AT_HOMEBASE`. Inconsistent; clients can't distinguish "bad region" from "valid region but not homebase". |
| M8 | Gemini = 1/6 | **Target clan has no on-chain mechanism to reject inbound OTC spam.** Cap protects senders not receivers. Add `reject*Transfer` for `toClan.owner`, OR allow cancel by either party. |
| M9 | Codex 5.3 + Codex 5.5 + Gemini = 3/6 | **Dead struct fields `accepted` / `cancelled`.** All 4 OTC structs allocate them but accept/cancel `delete` storage so flags are never set. Remove fields + dead `require(!proposal.accepted)` checks. |

## DEFER (LOW)

- L1: `CLANSMAN_CARRY_CAP` naming misleading — only applies to wood; iron/wheat/fish keep separate caps (3/6 noted)
- L2: `getPrice` previews zero-output sells but `sellResource` hardcodes `minOut = 1` — preview/exec mismatch (codex 5.4 + 5.5)
- L3: Generic `swapExactInForOut(minOut=0)` allows zero-output reserve donations
- L4: Fallback chain client points at old base-sepolia deployment (codex 5.5)

## Per-model verdicts

- **Codex 5.3:** NEEDS_FIXES — 0 HIGH + 4 MED. Conservative.
- **Codex 5.4:** NEEDS_FIXES — 1 HIGH + 4 MED + 1 LOW. Caught 200-tick stale OTC.
- **Codex 5.5:** NEEDS_FIXES — 2 HIGH + 5 MED + 2 LOW. Caught both architectural HIGHs.
- **Opus 4.6:** NEEDS_FIXES (MEDIUM-only) — 0 HIGH + 3 MED. CLEAN HIGH but flagged seedPools access control + indexer breaks.
- **Opus 4.7:** NEEDS_FIXES — 0 HIGH + 3 MED. Strong on noting fix-round was effective + flagging ABI/event indexer hazards.
- **Gemini Pro:** NEEDS_FIXES — 1 HIGH (expiryTick) + 2 MED + 1 LOW.

## Decision

**Recommend Path A R3 fix-round.** Scope:

1. **H1 stale-OTC**: add `MUST_SETTLE_FIRST` guard to all 4 accept paths
2. **H2 heartbeat upkeep bypass**: defer to Phase 8 R4 architectural fix (cross-phase)
3. **M1→H expiryTick**: change `proposeGoldTransfer` + event to `uint64`
4. **M2 expired proposal cap**: lazy reap on propose
5. **M3 seedPools access control**: add `treasuryOwner` check
6. **M6 dead direct-transfer methods**: remove from interface
7. **M9 dead struct fields**: remove `accepted/cancelled`
8. M4, M5, M7, M8, LOWs: file as Phase 7B follow-ups

Total fix budget: ~45-60 min codex.

H2 (heartbeat-vs-lazy) is shared with Phase 8 R4 + Phase 10 R3 — fixing in one place can ripple. Recommend: Phase 8 R4 implements the chronological-settlement core, then Phases 7 + 10 inherit via dev-branch rebase.
