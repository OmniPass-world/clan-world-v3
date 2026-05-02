# Super-Swarm Synthesis ROUND 2 — PR #413 (Wave 7 head `5c68235`)

**Date:** 2026-05-01 23:42 ET
**Reviewers:** codex 5-5 ✅, codex 5-4 ✅, Claude subagent (in flight)
**Scope:** Wave 7 delta only (`82d0d44..5c68235`) — focused fix-review

**Verdict:** **NEEDS_FIXES** — Wave 7's MUST-7.2 boundary-freeze fix introduced 2 new HIGH regressions, both convergent across 2 codex reviewers. Wave 8 required before merge.

---

## MUST FIX (Wave 8)

### MUST-8.1 — Boundary freeze placed too late in heartbeat

**Confirmed by:** codex 5-4 + codex 5-5 (convergent)

**File:** `packages/contracts/src/ClanWorld.sol:3019`

**Bug:** Wave 7's Option B freeze sits at Step 8 of heartbeat, AFTER Steps 1-7 have already executed. With `currentTick` pinned at `seasonEndTick - 1`, every subsequent `heartbeat()` REPLAYS the same closed tick:

- `_evaluateBanditSpawns()` (`:2728`) increments `probabilityAccum` and can spawn bandits **repeatedly** during limbo
- `_resolveWorldEvents()` reruns transition logic for the same `closedTick/newTick` pair
- `WinterStarted`/`WinterEnded` events can fire multiple times if the boundary coincides with a winter edge (codex 5-4 MEDIUM, same root cause)
- Wheat-plot reset logic can run multiple times for the same logical transition

This is a TRUE Wave 7 regression. Before Option B, the engine never re-entered the same closed tick.

**Fix:** Move the freeze check to the TOP of `heartbeat()`. If `_world.currentTick + 1 >= _world.seasonEndTick && !_world.seasonFinalized`, return early (no-op) BEFORE running any per-tick side effects.

```solidity
function heartbeat() external override {
    // Wave 8: freeze boundary BEFORE side effects (was at Step 8 in Wave 7 — repeated replay bug)
    if (_world.currentTick + 1 >= _world.seasonEndTick && !_world.seasonFinalized) {
        return; // limbo: no further side effects until finalizeSeason
    }
    // ... existing Steps 1-8 ...
}
```

Then `_resolveWorldEvents` no longer needs the freeze guard at the bottom (move it to the top of heartbeat instead).

### MUST-8.2 — `submitClanOrders` accepts orders during frozen limbo

**Confirmed by:** codex 5-4 + codex 5-5 (convergent)

**File:** `packages/contracts/src/ClanWorld.sol:3254`

**Bug:** With `currentTick` pinned at `seasonEndTick - 1`, `submitClanOrders()` keeps accepting fresh player orders. Orders are stamped with stale tick (`submittedAtTick`, `startTick`, `arrivalTick` at `:3423`, `:3495`, `:3505`). Worse: immediate market actions can execute and mutate balances BEFORE `finalizeSeason()` computes rankings. This breaks the "rankings snapshot is frozen at season end" invariant.

**Fix:** Reject order submission in the frozen-unfinalized state.

```solidity
function submitClanOrders(...) external override {
    // ... existing checks ...
    require(
        !(_world.currentTick + 1 >= _world.seasonEndTick && !_world.seasonFinalized),
        "ClanWorld: season ended, awaiting finalization"
    );
    // ... rest ...
}
```

Apply the same guard to any other player-driven mutators (codex 5-4 noted "consider applying the same pause to any other player-driven mutators" — review `prepareMarket`, `claimRewards`, etc. for similar exposure).

---

## SHOULD FIX (Wave 8 if cheap)

### SHOULD-8.3 — Snapshot pinning uses payload over receipt

**Confirmed by:** codex 5-5

**File:** `apps/server/convex/heartbeat.ts:201`

**Bug:** `refreshSnapshot` block param uses `payload.blockNumber ?? receipt.blockNumber`. Receipt is the chain fact; payload is keeper metadata. If payload block is wrong, logs are ingested from receipt (correct) but `refreshSnapshot` reads at a different block.

**Fix:** Always use `receipt.blockNumber`. Drop the `??`.

---

## DEFER

### LOW-8a — Hard-coded `RESOURCE_GOLD = 4`

`apps/server/convex/indexer.ts:152` `pricePointFromEvent` hard-codes gold id `4`. Defensive guard recommended for malformed resource-for-resource events. Pre-existing; not Wave 7 regression.

### LOW-8b — `receipt.to` hard requirement

`apps/server/convex/heartbeat.ts:65` strict `receipt.to === engine` check. Fine for current direct-call architecture. Refactor needed if heartbeat moves behind a router/forwarder. Not a Wave 7 regression.

### LOW-8c — Webhook does not validate `payload.chain`

`apps/server/convex/heartbeat.ts:127` MUST-7.3 auth fix didn't add chain validation. Codex 5-4 explicitly notes "I do not count that as a Wave 7 regression" — round-1 recommendation was incompletely applied. Defer to Wave 9 unless Liam wants it now.

### LOW-8d — Mission settlesAtTick == seasonEndTick

`packages/contracts/src/ClanWorld.sol:3153` (codex 5-5) — missions with `settlesAtTick == seasonEndTick` aren't finalized in the frozen season because `_settleClanThroughTick` is exclusive. Probably correct if `seasonEndTick` is first next-season tick, but document this.

---

## SKIP

None. The 3 LOWs above are all explicitly flagged as pre-existing / non-regressions.

---

## Wave 8 dispatch shape

**Bundle:** Single codex Pattern B-bulk dispatch covering all 3 fixes (MUST-8.1, MUST-8.2, SHOULD-8.3).

**Branch:** `fix/v110-wave8` based on `release/v1.0.1-wave1@f89b26d` (current HEAD includes Wave 7 + lockfile fix).

**Validation gate:**
- forge tests >= 336 (current). New tests for:
  - `test_heartbeatFrozenLimboIsNoOp` — at boundary, run heartbeat 100 times, assert no state changes (no bandit spawns, no winter events, no probabilityAccum increment)
  - `test_submitClanOrdersRevertsInFrozenLimbo` — at boundary, attempt submitClanOrders, assert revert with "season ended, awaiting finalization"
  - `test_winterTransitionEmittedOnceAtSeasonBoundary` — coincide season + winter boundary, assert WinterStarted fires exactly once
- Existing freeze tests (Wave 7's `test_heartbeatFreezesAtSeasonBoundary`) need update — old assertion was `currentTick == seasonEndTick - 1` after one heartbeat, but the test should probably also assert "no side effects ran in the second heartbeat call"

**Time:** 30-60 min.

---

## Round 2 reviewer attribution

| Reviewer | File | Verdict | HIGH count | Notes |
|---|---|---|---|---|
| codex 5-5 | `/tmp/wave7-review-codex-5-5.md` | NEEDS_FIXES | 2 | Found freeze placement + submit-during-limbo + snapshot pinning MED |
| codex 5-4 | `/tmp/wave7-review-codex-5-4.md` | NEEDS_FIXES | 2 | Same 2 HIGHs + winter-replay MED (same root) + chain-validation LOW |
| Claude subagent | `/tmp/wave7-review-claude-subagent.md` | (in flight) | — | Will append if it finds new |

**Convergence:** 2/2 codex agree on freeze placement + submit-during-limbo as the dominant Wave 7 regressions. Strong signal — Wave 8 is required.

🤖 Synthesized by Claude Opus 4.7
