Cross-cutting verification complete. Now writing the review.

# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY
**NEEDS_FIXES** — Phase 5 delivers wood gathering, deposit semantics, and the RNG simplification cleanly, with solid test coverage for the deposit path. **Two HIGH concerns block clean merge:** (1) the `ResourcesDeposited` event downsizes `tick` to `uint32` while every other event in `IClanWorldEvents` uses `uint64` — Phases 8 and 10 have already reverted identical `uint32 ABI` regressions, so this is a known anti-pattern; (2) the deposit wrong-region path returns `ERR_INVALID_REGION` while `BuildWall`/`UpgradeBase` (literally adjacent in the same submit-validation block) still return `ERR_NOT_AT_HOMEBASE` for the same condition. Recommend one fix-round on those two before merging to dev.

## HIGH severity findings

### H1. `ResourcesDeposited` is the lone event downcasting tick to `uint32`
`IClanWorld.sol` defines a uniform tick-typing convention: `ResourcesGathered(...uint64 tick)`, `WallLevelChanged(...uint64 atTick)`, `BaseLevelChanged`, `ColdDamageApplied`, `BanditDefeated`, `BlueprintTransferred`, `WorkerArrived`, `ClanEliminated`, `GoldTransferred` — **all `uint64`**. Only `ResourcesDeposited` was migrated to `uint32 tick` in this PR. Consequences:

- Indexers/subgraphs key off topic-hash; `keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint64)")` ≠ `...uint32)`. Existing consumers break silently.
- The ABI surface gains a one-off shape that future refactors will trip on.
- Forces a `_doDeposit` signature change from `uint64` → `uint32` and a cast at call site that needs the `forge-lint: disable-next-line(unsafe-typecast)` suppression.
- **Project history shows this exact regression has been fixed twice already**: commits `36420da` ("Phase 8 super-swarm r2 — … uint64 ABI revert"), `1210e90` ("Phase 10 super-swarm r2 — … uint64 ABI revert"). This is a recognized anti-pattern.

**Fix:** revert `ResourcesDeposited` to `uint64 atTick` (preserving naming consistency with peer events too — `tick` → `atTick`), revert `_doDeposit` signature to `uint64`, drop the unsafe-typecast comment + the `DEPOSIT_DURATION_TICKS` typing change cascade. Update the test's hardcoded topic hash accordingly.

### H2. Inconsistent error code for "must be at homebase" condition
`ClanWorld.sol:1613` (BuildWall): `return StatusCode.ERR_NOT_AT_HOMEBASE;`  
`ClanWorld.sol:1619` (UpgradeBase): `return StatusCode.ERR_NOT_AT_HOMEBASE;`  
`ClanWorld.sol:1689` (DepositResources, changed in this PR): `return StatusCode.ERR_INVALID_REGION;`

Three actions in the same submit-validation pass enforce the **identical** "gotoRegion != clan.baseRegion" check, but Phase 5 changed the deposit case to return a strictly less-specific code. The `ERR_NOT_AT_HOMEBASE` enum value is **still defined** in `IClanWorld.sol:163` — this isn't an enum cleanup. UI/clients that branch on status code now lose deposit-specific feedback.

**Fix:** revert `_doDeposit`'s submit-validation back to `ERR_NOT_AT_HOMEBASE`, OR migrate all three (build/upgrade/deposit) in one pass and remove the enum entry. Either is acceptable; the half-migration is not.

## MEDIUM severity findings

### M1. Wood-only economy refactor; iron/wheat/fish still on legacy yield model
The new model — `yield = WOOD_YIELD_PER_TICK × getActionDuration(action)`, crit doubles — is sound (per-tick yield × duration normalizes resources-per-second across actions). But verified by reading `ClanWorld.sol:511-655`:

- `_gatherIron`: `yield = IRON_BASE_YIELD` (no duration scaling), additive gold-bonus crit
- `_gatherFishDocks` / `_gatherFishDeepSea`: hardcoded `yield = 1e18` per gather
- `_gatherWheat`: `yield = WHEAT_HARVEST_RATE` (constant, no duration scaling)

All four resources have `getActionDuration(...) = 4`, so wood now produces ~4× per gather call vs the others, on the same wall-clock. This is either:
- A **deliberate Phase-5.1-scoped step** (reasonable; document with follow-up issue for iron/wheat/fish migration), or
- An **incomplete refactor** that breaks the resource-economy balance.

The PR title "Phase 5 — Gathering / Deposits / Economy" suggests the broader refactor was intended. **Recommend:** either migrate iron/wheat/fish in this PR, or open follow-up issues with `IRON_YIELD_PER_TICK`, `WHEAT_YIELD_PER_TICK`, `FISH_YIELD_PER_TICK` constants and explicitly defer.

### M2. `CLANSMAN_CARRY_CAP` naming misleads
`IClanWorld.sol:43`: `uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;` is referenced **only by wood**. Iron still caps at `IRON_CAP = 5e18`, wheat at `WHEAT_CAP = 40e18` (4× higher than the supposed "carry cap"), fish at `FISH_CAP = 8e18`. A clansman can carry `10 + 5 + 40 + 8 = 63e18` total. The constant's name implies a per-clansman aggregate that the contract does not enforce. Either rename to `WOOD_CARRY_CAP_V2` or implement an actual aggregate cap. As-is, this is a naming trap for future contributors.

### M3. Crit-distribution test is statistically flaky (~0.4% false-fail)
`Gathering.t.sol:test_chopWoodCritDistributionAcrossSeeds` runs 100 seeds, expects crit count ∈ [3, 20] with p=0.10. Binomial(100, 0.10) tail probabilities: P(X<3) ≈ 0.0019, P(X>20) ≈ 0.0024 → combined ~0.4% false-fail rate. Acceptable but will surface as flake on CI over time. Tightening: pick a fixed deterministic seed batch with known crit count, or widen bounds to [1, 25] and rely on the deterministic-base-yield case in `test_chopWoodAtForestYieldsBaseTimesActionDuration` for non-crit verification.

### M4. Single-gather can fill 80% of cap on crit
`WOOD_YIELD_PER_TICK × duration = 1e18 × 4 = 4e18` base; crit → `8e18`; cap `10e18`. Means consecutive gathers in the same mission rarely add full yield (clamped via `if (yield > remaining) yield = remaining;`). If intentional (each gather mission ≈ 1-2 cycles before deposit run), fine — but no test asserts the clamp path. Add a fuzz test: pre-set `cs.carryWood = CLANSMAN_CARRY_CAP - 2e18`, trigger crit, assert yield clamped to 2e18 (not 8e18) and event emits the clamped value.

## LOW severity findings

### L1. Hardcoded `tick = 1` in event-emission test
`ClanWorld.t.sol:test_depositResources_eventHasCorrectDeltas` does `emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);`. The `1` is the expected tick value, but it's coupled to setUp's tick state. If `_setupHarness` ever advances additional ticks during clan creation, this test silently asserts the wrong tick. Read the current tick before the `expectEmit` and use that variable.

### L2. RNG variable naming inconsistency
Wood gather now uses `woodRng`; iron uses `goldRng` (for the gold bonus); fish uses `fishRng`. Standardize on `<purpose>Rng` or `<resource>CritRng`, or leave alone. Cosmetic only.

### L3. Two test harnesses with overlapping responsibility
`ClanWorldTestHarness.setCarry(csId, w, ir, wh, fi)` (multi-resource) and `GatheringHarness.setCarryWood(csId, wood)` (wood-only) both expose raw clansman carry storage. Either consolidate into one shared harness or have `GatheringHarness` extend `ClanWorldTestHarness`.

### L4. `forge-lint: disable-next-line(unsafe-typecast)` is a smell, not a fix
The comment correctly documents that the cast is intentional given the event ABI choice — but H1 makes the underlying ABI choice the actual issue. Fixing H1 removes the cast and the comment together.

### L5. `vm.expectEmit(true, true, false, true)` third positional arg
`ResourcesDeposited` has only 2 indexed args (`clanId`, `clansmanId`); the third position correctly = false. Fine, but consider named args (`vm.expectEmit({checkTopic1: true, ...})`) for clarity since the four-bool signature is a frequent foot-gun.

## Cross-cutting observations

**Phase 5 strengths:**
- Five new tests (`test_depositResources_*`) give thorough coverage of the deposit path: happy path, multi-resource, wrong-region rejection, empty-carry no-op-without-event, event delta values.
- New `Gathering.t.sol` adds a clean harness pattern for gather-only testing isolated from full-clan setup.
- 1-tick `DEPOSIT_DURATION_TICKS` constant + cross-phase alignment with phase-4 timing (commit `632e2b2`) is good seam discipline — see memory `feedback_cross_phase_contract_in_briefs.md`.
- RNG simplification (RNG.rngBool/rngBounded → direct `keccak256 % 10000`) preserves the 10% crit rate (previous `50% gate × 20% bounded = 10% effective`) while matching the pattern used by iron/fish — *good consistency move*.

**Phase 5 cohesion gaps:**
- Wood economy refactored but iron/wheat/fish gatherers still on legacy single-tick yield model (M1). Title says "Gathering / Deposits / Economy" but only deposits + wood-gather migrated — title oversells.
- ABI uniformity broken in one event (H1). This regression has been fixed twice in earlier phases; the codebase clearly wants `uint64 atTick`.
- Status-code uniformity broken across submit-validation block (H2).

**Anti-cheat / data-flow risks not covered by tests:**
- No test for resource conservation across gather→carry→deposit→vault (sum invariant).
- No test for deposit during clan starvation state (does `_doDeposit` care? — diff suggests no, which is correct, but confirm by test).
- No test for replay/double-deposit (mission completes, immediately re-submit deposit on same tick).
- No fuzz test for yield × cap clamp interaction (M4).
- No test for `_doDeposit` when clan is eliminated mid-mission (mission was queued before elimination).

**Merge recommendation:** Address H1 + H2 in a single fix-round (mechanical, ~30 LOC + test topic-hash update). M1–M4 can defer to follow-up issues if explicitly scoped. Ship after H1+H2 clean.
EXIT=0
