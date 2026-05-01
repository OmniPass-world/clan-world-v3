I've reproduced a real bug: the heartbeat-eager `_resetColdDamageForAllClans()` causes 1 fewer wall-degradation (6 vs 5) when a clan is settled mid-winter at an odd coldDamage value. Now writing the review.

# Phase Super-Swarm Review (R2) — PR #250 (head 1210e90)

## SUMMARY

The four targeted fixes (H2 starvation, H3 wood burn, H4 uint64 events, H5 wheat plot clear) are correctly implemented and exercised by 15 new tests, all passing. Spec §7.3 was patched as required. **However, the fix-round introduces a new HIGH-severity correctness bug**: the heartbeat-eager `_resetColdDamageForAllClans()` call at winter-end destroys the mid-winter `coldDamage` value for any clan whose `lastSettledTick` is inside the winter window, causing the lazy re-settle to under-count cold consequences (wall degradations / clansman deaths). I reproduced it locally — a clan settled mid-winter with `coldDamage=3` ends winter with `wallLevel=6` instead of `5` for the otherwise-identical never-settled clan. The fix is a one-liner: drop the eager reset; the in-loop / post-loop checks in `_settleClan` already handle the boundary correctly.

Targeted-fix verification (each ✓ confirmed via code + passing test):

| # | Fix | Status | Evidence |
|---|---|---|---|
| H2 | Starvation guard `< tick` (not `<=`) + defer to tick+1 | ✓ | ClanWorld.sol:440 (`starvationStartsAtTick < tick`); `test_starvation_kill_deferred_to_next_tick`, `test_pre_winter_starver_dies_in_winter_at_same_cadence` |
| H3 | Wood burn = base + per-clansman | ✓ | ClanWorld.sol:446-447; `test_winter_upkeep_doublesFoodAndBurnsWood` (4-clansman = 3e18 ✓) |
| H4 | WinterStarted/Ended back to uint64 | ✓ | IClanWorld.sol:498-499 (`uint64 indexed tick`); `_winterEventTick` returns uint64 |
| H5 | Wheat plots cleared at winter start | ✓ | `_lockWheatPlotsForWinter` sets state+remainingWheat+regrowUntilTick; `test_winter_cropTransitions_lockThenRestartRegrow` |
| Spec §7.3 | Per-base + per-clansman doc | ✓ | docs/planning/clanworld_v4_spec.md:835 |

## HIGH severity findings

### H-1 (NEW) — `_resetColdDamageForAllClans()` corrupts mid-winter state for partially-settled clans

**File:** `packages/contracts/src/ClanWorld.sol:1126`, called from `_resolveWorldEvents` (1118-1130)

When the heartbeat opens the first post-winter tick, `_resolveWorldEvents` eagerly walks every clan and zeros `coldDamage` in storage. But lazy settlement is the contract's authoritative replay of past ticks — and for any clan whose `lastSettledTick` is *inside* the winter window, the eager reset destroys storage state that the lazy `_settleClan` then reads as the starting point when it replays the remaining winter ticks.

**Reproduced locally** (test added under `/tmp/pr250-review/packages/contracts/test/ColdDamageResetBug.t.sol`):

```
clanA settled at tick 113 (3 winter ticks processed): coldDamage=3, wallLevel=9
heartbeat advances to tick 120 → _resetColdDamageForAllClans → clanA.coldDamage = 0
settleClan(clanA) → replays ticks 113-119 starting from coldDamage=0
                 → 7 short-wood ticks: 0→7 → 3 wall degradations (crossings at 2,4,6)
clanA wall FINAL = 6

clanB never settled: settleClan(clanB) at tick 120 replays 0-119 in one go
                 → 10 short-wood ticks: 0→10 → 5 wall degradations
clanB wall FINAL = 5
```

Both clans had identical inputs (4 clansmen, no wood, full food, wallLevel=10, 10 winter ticks of shortage). They diverge by 1 wall-degradation. In the worst case (clansman deaths instead of wall absorption), this is a missed clansman death — directly affecting `livingClansmen` and elimination outcomes.

**Why the in-loop reset isn't enough:** the in-loop reset at `_applyUpkeep:404-406` and the post-loop reset at `_settleClan:393-395` both correctly zero `coldDamage` *at the boundary tick during replay*. They do the right thing. The eager heartbeat reset is **redundant and harmful** — it pre-zeros storage before the replay needs to read the carry-over.

**Fix:** Delete `_resetColdDamageForAllClans()` and its call site at line 1126. The function `_resetColdDamageForAllClans` (1164-1168) becomes unused and should be removed too. Lazy-settle logic already handles the reset correctly. This also removes an O(n_clans) heartbeat scan, slightly reducing heartbeat gas.

**Severity rationale:** silent state divergence between two execution paths that should produce identical results — exactly the class of bug this swarm review exists to catch. Repro is deterministic.

## MEDIUM severity findings

### M-1 — Undocumented winter-tick shift from [100,110) to [110,120)

**Files:** `IClanWorld.sol:28-31`, `ClanWorld.sol:95-97`, both `t.sol` files

The fix-round renamed `TICKS_PER_WINTER_CYCLE = 110` → `WINTER_START_TICK = 110` + `WINTER_PERIOD_TICKS = 110`. Old constructor: `winterStartsAtTick = 110 - 10 = 100` → first winter at ticks [100,110). New constructor: `winterStartsAtTick = 110` → first winter at ticks [110,120). This is a 10-tick semantic shift not listed under H2-H5.

Spec §7.1 ("Winter occurs every 110 ticks") and §7.2 ("Winter lasts 10 ticks") are silent on the offset of the *first* winter, so the new interpretation is defensible. But this is an undocumented behavior change, and any downstream tooling/agents calibrated to the old timing will desynchronize. Recommend either:
- Add a one-line note in the spec ("the first winter occurs at ticks [110, 120)") or
- Add a code comment near `WINTER_START_TICK` explaining the choice.

### M-2 — Tick-width inconsistency in cold/death event surface

**File:** `IClanWorld.sol:506-509`

The fix-round added new events:
```solidity
event ClanColdShortage(uint32 indexed clanId, uint32 tick, uint256 woodShort);
event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint32 tick);
event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint32 tick);
event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
```

Three of the four use `uint32` for tick; `ClanDied` and `WinterStarted/Ended/ClanSettled/MissionAssigned/...` use `uint64`. The `_eventTick` helper (line 1174) safely casts but reverts at `tick > 2^32`. With 60s ticks that is ~8000 years, so no overflow risk in practice — but the inconsistency invites indexer / ABI-schema bugs. The H4 fix specifically reverted Winter events back to uint64; the new events should match.

**Recommendation:** widen `tick` field on `ClanColdShortage`, `WallDegradedByCold`, `ClansmanColdDeath` to `uint64`, drop `_eventTick`.

## LOW severity findings

### L-1 — Asymmetric kill selection: starvation deterministic, cold RNG

`_killNextClansmanFromStarvation` (519-533) kills the first non-dead clansman by `_clanClansmanIds` order. `_killRandomClansmanFromCold` (486-517) uses `RNG.rngBounded`. Spec doesn't mandate either; both are reasonable. Worth a comment explaining the deliberate split, otherwise a future reader will assume one or the other is wrong.

### L-2 — `_markClanDead` doesn't zero `coldDamage`

Lines 581-... zero `vaultWood/Wheat/Fish/Iron`, `starvationStartsAtTick`, `livingClansmen` — but not `coldDamage`. Cosmetic since `_applyUpkeep` returns early on `livingClansmen == 0`, but storage retains stale value. Add `clan.coldDamage = 0` for consistency with the rest of the burn-on-death state.

### L-3 — Stale `_world.winter*` storage values

After this PR, the heartbeat no longer writes to `_world.winterActive`, `_world.winterStartsAtTick`, `_world.winterEndsAtTick`. They retain constructor values (110 / 120) forever. All reads now go through `_worldStateView()`, which recomputes from `_isWinterActiveAt`. This works, but anyone reading raw storage (slot access, future internal code, or a forge inspector) sees stale data — a footgun. Consider either:
- updating the storage at the transitions (small gas cost), or
- adding a comment at the struct declaration: `// computed view-only via _worldStateView; do NOT read directly from storage`.

### L-4 — Both `ClanEliminated` and `ClanDied` emitted on every death

`_markClanDead` (lines 593-594) emits both. They share the same `(clanId, tick)` key; `ClanDied` adds `reason`. Indexers will dedupe but it doubles event-log gas and is a small noise source. Either drop one, or keep `ClanEliminated` only for compatibility with prior consumers and document that.

### L-5 — `MAX_CROP_TRANSITION_PER_TICK = 48` is a soft brick risk

ClanWorld.sol:81 caps wheat plot transitions at 48 (= 24 clans × 2 plots). If `_allClanIds.length > 24`, both `_lockWheatPlotsForWinter` and `_restartWheatPlotsAfterWinter` revert with `"ClanWorld: crop transition cap"` — and they're called from `_resolveWorldEvents` inside `heartbeat()`, so a heartbeat revert bricks the world. I don't see an enforced max-clan-count anywhere obvious; if `mintClan` can produce >24 clans, this is a latent DoS. Either:
- add `require(_allClanIds.length <= 24)` to `mintClan`, or
- raise the cap, or
- replace the per-iteration require with a per-loop computed bound and let it fail safely without reverting heartbeat.

### L-6 — Wood burn ordered before mission settlement in the tick loop

`_settleClan` runs `_applyUpkeep` first, then the regrow check, then mission settlement (line 387-390). Effect: a clansman returning with 100 wood at tick X cannot save the clan from cold-death at tick X if the vault is short — the wood is still on the clansman when `_applyUpkeep` runs. Probably intentional (turn-order: burn → settle → end-of-tick), but worth a comment.

### L-7 — Spec file lost trailing newline

`docs/planning/clanworld_v4_spec.md`: the diff removes the final blank line. Minor style nit; many tools warn on missing final newline.

## Cross-cutting observations

- **Test coverage:** 15 new fix-round tests, all green. The harness pattern (`setClanUpkeepState` directly mutating storage) is convenient but **bypasses the heartbeat-eager-reset path** — that's why H-1 above slipped through. A test that **lets `_advanceToTick` cross the winter-end boundary on a clan with `lastSettledTick` mid-winter and non-zero `coldDamage`** would catch H-1. Recommend adding one.
- **Spec §7.4 interpretation:** §844 "both plots restart regrowing after winter ends" + §845 "harvestable again 4 ticks after winter ends" — implementation sets `regrowUntilTick = newTick + 4` where `newTick` is the first post-winter tick (e.g., 124 for first winter ending at tick 119/120). This is a defensible interpretation; alternative reading is `winterEnd_last_winter_tick + 4 = 123`. Not flagging as a bug, but worth a one-line spec clarification.
- **Wheat-plot regrow loss for unsettled clans pre-winter:** if a plot is `Regrowing` with `regrowUntilTick=108` at tick 109 and the clan never settles before heartbeat opens tick 110, `_lockWheatPlotsForWinter` overwrites it with `WinterLocked` — the regrow that *should* have completed in summer is silently lost (extra winter delay). Edge case, low impact, but a fairness wrinkle.
- **Starvation-kill cadence verification:** the `< tick` guard does what's claimed — pre-winter starver dies on the first winter tick (because `starvationStartsAtTick = winterStart - 5 < winterStart` is true on day 1), fresh-winter starver who became starving at `winterStart+2` dies at `winterStart+3`. Both fall to "1 tick after starvation began OR winter began, whichever is later." That's the correct one-tick grace for the *starvation* event itself; the test asserts equal end-state count which holds because both are seen with `starvationStartsAtTick < tick` for every iterated winter tick.

---

**Recommended pre-merge action:** fix H-1 (one-line removal). M-1, M-2 are documentation/consistency cleanups, can ship as a follow-up if time-pressed. L-5 (clan cap) deserves a quick `require` since it's a brick risk if any path can produce >24 clans.
