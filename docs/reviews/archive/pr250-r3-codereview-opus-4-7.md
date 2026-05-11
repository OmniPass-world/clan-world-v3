# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY

**Verdict: NEEDS_FIXES (1 HIGH, 6 MEDIUM)** — The phase delivers winter mechanics, cold-damage cadence, and clan elimination cleanly, with strong test coverage on lock/regrow/cadence. The biggest concern is that the heartbeat now derives winter purely from tick math but **leaves the old `_world.winterActive / winterStartsAtTick / winterEndsAtTick` storage slots permanently stale** — any direct internal/external reader of those slots (current or future) silently gets wrong data. Also worth gating before merge: lazy-settle path-dependence in cold-death RNG, a dead `ClansmanDiedFromCold` event still in the public interface, and a redundant `mintClan` cap that hides the real intent.

---

## HIGH severity findings

### H1. `_world.winterActive / winterStartsAtTick / winterEndsAtTick` are now permanently stale storage

**File:** `packages/contracts/src/ClanWorld.sol` (heartbeat winter block, ~lines 1120–1170; constructor lines 89–96; `_worldStateView` ~line 1196)

The PR moves winter from a stored timer to a pure-derived schedule (`_isWinterActiveAt`, `_winterWindowForTick`). The heartbeat's prior block that wrote `_world.winterActive`, `_world.winterStartsAtTick`, `_world.winterEndsAtTick` (and reset them at season rollover) is **deleted**. Those three storage fields are now written exactly once — in the constructor — and never updated again.

Reads are routed through `_worldStateView()` for `getWorldState()` / `getWorldSnapshot()`, so external view callers see correct values. **But the storage values themselves are lies after tick 110.** Any code path that reads `_world.winterActive` (or the start/end ticks) directly — anywhere in the contract today, or a future patch that forgets the indirection — will get the constructor-time values forever. Same risk for off-chain indexers reading raw storage slots.

This is a footgun, not a bug-today (I scanned the diff and didn't see remaining direct reads in the patched code), but it's a high-blast-radius latent issue: the next dev who writes `if (_world.winterActive)` in a new feature will silently break.

**Suggested fix (pick one):**
- **Cleanest:** drop those three fields from the on-chain `WorldState` struct entirely and let `_worldStateView()` synthesize them only for the external `WorldState` ABI return. Storage and source-of-truth become one and the same.
- **Cheaper:** keep the fields but update them inside the heartbeat winter-transition block at the same point where you currently emit `WinterStarted`/`WinterEnded`, mirroring `nowWinter` and `_winterWindowForTick(newTick)`. Add a one-line comment that reads must go through `_worldStateView()` and writes must stay in lockstep.

Ship-blocker because it's exactly the kind of regression cold-damage / next-phase code is most likely to introduce silently.

---

## MEDIUM severity findings

### M1. Cold-death RNG is path-dependent on settle timing

**File:** `packages/contracts/src/ClanWorld.sol` `_killRandomClansmanFromCold` (~line ~530)

`RNG.rngBounded` is seeded with `_world.currentTickSeed` — which is the **current** heartbeat's seed, not the seed of the historical winter tick the cold death actually occurred at. Lazy settles at different chain-block heights will pick **different** clansmen as the victim. The salt mixes `(clanId, tick, coldDamage)` so within one block result is deterministic, but the choice of victim shifts depending on when someone calls `settleClan`. This is mildly gameable (an elder could time settleClan to favor a roll) and breaks the implicit "lazy ≡ eager" invariant.

**Suggested fix:** if `_world.tickSeeds[tick]` (or equivalent) is preserved, use the historical tick's seed. Otherwise, derive the per-tick seed deterministically from `(blockhash anchor, tick)` so lazy settle is reproducible. Same concern likely applies to other lazy-settle RNG sites — worth a one-line audit follow-up.

### M2. Dead `ClansmanDiedFromCold` event still in `IClanWorldEvents`

**File:** `packages/contracts/src/IClanWorld.sol` events block

`event ClansmanDiedFromCold(...)` remains in the interface (visible in the diff context) but is never emitted by the implementation; the new `ClansmanColdDeath` event replaces it. Off-chain consumers who subscribed to the old event will silently see zero events.

**Suggested fix:** remove the dead event from the interface, or document a deprecation in the spec changelog. If any subgraph/indexer is already wired to the old event, this is a regression they'll hit in prod.

### M3. Redundant `mintClan` clan-cap check obscures intent

**File:** `packages/contracts/src/ClanWorld.sol` `mintClan` (~line 1233–1234)

```solidity
require(_allClanIds.length < 12, "ClanWorld: max clans");
require(_allClanIds.length < MAX_CLANS_FOR_CROP_TRANSITIONS, "ClanWorld: clan cap");
```

`MAX_CLANS_FOR_CROP_TRANSITIONS = 24`, so the second require is dead code as long as the literal `12` stands. If anyone later raises `12` to `24` (or higher), they hit a confusing error. Either:
- Replace the literal `12` with a named `MAX_CLANS` constant and assert at compile time that `MAX_CLANS <= MAX_CLANS_FOR_CROP_TRANSITIONS`, **or**
- Drop the redundant `MAX_CLANS_FOR_CROP_TRANSITIONS` check and add a single comment by `MAX_CROP_TRANSITION_PER_TICK` saying "must be ≥ 2 × max clans".

The current shape pretends to be defense-in-depth but is just future-fragility.

### M4. Cold-damage reset is duplicated in two paths

**File:** `packages/contracts/src/ClanWorld.sol` — `_settleClan` post-loop (~line ~395) and `_applyUpkeep` head (~line ~410)

Both reset `clan.coldDamage = 0` at the same condition (first non-winter tick after winter). They overlap — `_applyUpkeep` always handles it inside the loop, and the post-loop check fires again at `curTick`. The result is correct (idempotent), but a future change to one site will diverge silently from the other.

**Suggested fix:** keep only the `_applyUpkeep` reset (it runs per-tick and naturally covers both partial and full settlements), remove the duplicate from `_settleClan`. Tests `test_winter_upkeep_returnsToNormalAfterWinter` and `test_winterColdDamageResetIsLazyPerClanAfterPartialSettlement` should still pass.

### M5. `_lockWheatPlotsForWinter` / `_restartWheatPlotsAfterWinter` iterate dead clans

**File:** `packages/contracts/src/ClanWorld.sol` ~lines 1140–1175

Both loops iterate `_allClanIds` unconditionally, including clans whose `clanState == DEAD`. Dead clans' plot transitions don't matter (the clan never harvests again) and consume budget against `MAX_CROP_TRANSITION_PER_TICK = 48`. With max 12 clans this is fine today, but as soon as the cap is raised — or after several seasons accumulate dead clans — the budget will hit. Skip dead clans in both loops.

### M6. `DOMAIN_COLD_DAMAGE = keccak256("cold_damage")` breaks the `clanworld.<feature>.v1` salt convention

**File:** `packages/contracts/src/lib/RNG.sol` line 12

All other RNG domains use a versioned, namespaced salt (`clanworld.market.noise.v1`, `clanworld.weather.v1`, etc.). The new domain uses bare `"cold_damage"`. Functionally fine — the keccak space is sparse — but it breaks audit grep and forfeits versioning for future salt rotations. Rename to `keccak256("clanworld.cold.damage.v1")`.

---

## LOW severity findings

### L1. `_winterEventTick(tick)` is an identity function

**File:** `packages/contracts/src/ClanWorld.sol` ~line ~1180 (and duplicated in stub)

```solidity
function _winterEventTick(uint64 tick) internal pure returns (uint64) { return tick; }
```

No abstraction value today. Either remove and inline `tick` at the two callsites, or add a comment noting the intended future hook (e.g., adjust event tick during late-heartbeat catch-up). Right now it just adds noise.

### L2. `_killRandomClansmanFromCold` re-scans `csIds` to recount living

**File:** `packages/contracts/src/ClanWorld.sol` ~line ~510

The function iterates `csIds` once to count living, then again to pick. `clan.livingClansmen` is already maintained authoritatively — using it would halve the loop. Defensive but wasteful at higher clansman counts.

### L3. JSON ABI consumers beyond `runnerCastHeartbeat.ts` / `IChainClient.ts`

**File:** `packages/contracts/abi/IClanWorld.json`

The regenerated ABI now includes `currentSeasonNumber` and `nextHeartbeatAtTick` in the `WorldState` tuple (the underlying Solidity struct already had them — the old JSON was stale). Two TS readers were updated. If there are other off-chain readers (UI, subgraph, snapshot service) decoding `WorldState` with a hard-coded ABI, they will mis-decode silently. Worth one `grep` across the monorepo + an indexer ping before merge.

### L4. `test_deadClanCannotSubmitClanOrders` expects `vm.expectRevert("ClanWorld: clan dead")` but the new `submitClanOrders` path returns `ERR_CLAN_DEAD` per-order without reverting

**File:** `packages/contracts/test/ClanWorld.t.sol`

The expected revert string isn't visible in the patched `submitClanOrders` body. Either:
- The revert comes from an unchanged path (`_submitOrder` test helper, or a single-order entrypoint with a `require`),
- Or the test is calling a different ingress that does revert.

If the test passes against `400463e`, this is just a naming inconsistency between batch (returns) vs. single (reverts) ingresses — but worth a 30s confirmation that the dead-clan check is enforced in **both** ingresses, not just one.

---

## Cross-cutting observations

- **Phase goal delivery:** ✅ Winter cadence (110-tick recurring window), 2x food upkeep, wood burn (base + per-living-clansman), cold damage with wall-then-clansman cascade, wheat-plot lock/unlock, winter-block on `HarvestWheat`, mid-winter mint with locked plots, season-spanning winter, clan elimination with vault burn + gold survival. All match the spec changes in `clanworld_v4_spec.md` §7.
- **Test coverage on integration seams:** Strong. Particularly good: lazy-vs-partial settlement convergence (`test_winterColdDamageResetIsLazyPerClanAfterPartialSettlement`), pre-winter starver cadence parity (`test_pre_winter_starver_dies_in_winter_at_same_cadence`), mid-winter mint (`test_winterMintedClanStartsWithLockedEmptyWheatPlots`), and crop-transition-cap headroom (`test_winterCropTransitionCapCoversCurrentClanCap`).
- **Missing test surfaces worth adding follow-up for:**
  - Cold death victim selection across multiple lazy-settle timings (would have caught M1).
  - A clan freshly minted **just before** winter start tick, where plots start `Harvestable` and then must lock — covered indirectly by `test_winter_cropTransitions_lockThenRestartRegrow` but not explicitly with a single-tick-window mint.
  - Reading `_world.winterActive` directly from a subcomponent (would have caught H1).
- **Fix-round regressions:** I did not spot a fix-round-introduced regression in this batch — the cold-damage-reset duplication (M4) is the most plausible candidate, but both paths set the same value. The `livingBeforeStarvation` snapshot ordering looks deliberate and is asserted by `test_winter_starvationWoodBurnUsesPreDeathLivingCount`.
- **State-machine invariants verified by tracing:**
  - `_applyUpkeep` correctly checks `clanState == DEAD` mid-loop via the `_settleClan` early-break.
  - `submitClanOrders` correctly aborts the batch on a DEAD clan after `_settleClan` (death-during-settle covered).
  - `_markClansmanDead` cleanup of in-flight DefendBase missions is consistent with `_clearDefender`.

**Merge recommendation:** Fix H1 in this PR (cheap path: re-add the storage-mirror writes inside the heartbeat winter-transition block). M1–M3 can ship as a fast follow-up before phase 11 — flag them in the merge PR description so the next phase doesn't compound the divergence. The rest are clean-up and follow-up territory.
EXIT=0
