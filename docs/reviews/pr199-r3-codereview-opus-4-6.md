I'll review this Phase 8 diff by launching parallel agents to trace specific areas while I analyze the core logic.

# Phase Super-Swarm Review (R3) — PR #199 (head 36420da)

## SUMMARY

**CLEAN (conditional)** — no HIGH findings. The reservation-based building system (reserve-at-queue, debit-at-settle) is correctly designed. Death-refund, cancellation, and cross-clan isolation all work. The R2 fixes properly address all 6 R1 HIGHs without introducing regressions. Two MEDIUM findings: (1) cross-building-type order switching can cause false validation rejections because the about-to-be-freed reservation from the OLD building type isn't accounted for in the NEW type's validation, and (2) the `_min(held.cost, currentCost)` settlement pattern combined with non-monotonic cost tables allows a small resource underpayment (~5 wood) when reservations shift levels due to cancellation. Both are correctness deviations with negligible gameplay impact. **Recommend merge** — both MEDIUMs are follow-up-issue material, not blockers.

## HIGH severity findings

CLEAN — no findings.

## MEDIUM severity findings

### M1. Cross-type building reservation switch causes false validation rejection

**Files:** `ClanWorld.sol` — `_validateUpgradeWallOrder`, `_validateUpgradeBaseOrder`, `_validateUpgradeMonumentOrder`

Each validation function only considers the SAME-TYPE existing reservation when computing `releasedWood`/etc. If clansman A has an active **wall** upgrade reservation holding 20 wood, and you submit a **base** upgrade order for the same clansman, the base validation sees `_reservedWoodByClan` still includes the wall's 20 wood (because refund happens AFTER validation in `_validateAndInstallMission`). The `releasedWood` in `_validateUpgradeBaseOrder` only checks `_baseUpgradeReservations[clansmanId]`, which is empty.

**Concrete scenario:** Clan has 50 wood. Clansman A has wall reservation (20 wood reserved). Available for base validation = 50 - 20 = 30. Base 1→2 costs 40 wood → **rejected**. But wall refund would free 20 wood → actual available = 50 → should succeed.

**Impact:** UX-only — false rejections when switching building types. No data corruption or economic exploit.

**Suggested fix:** Before calling `_validateSingleOrder`, check if the clansman's current mission is a different building type and temporarily release that reservation for validation purposes, or move the refund step before validation.

---

### M2. Non-monotonic cost tables + `_min` allows small resource underpayment on reservation level shift

**Files:** `ClanWorld.sol` — `_settleWallUpgrade` (and analogous base/monument settle functions), `_wallUpgradeCost`

Wall cost table is non-monotonic: level 1→2 costs 35 wood, level 2→3 costs 30 wood. When reservations shift levels due to cancellation, `_min(held.cost, currentCost)` can debit less than the actual upgrade cost.

**Concrete scenario:**
1. Wall=0. A reserves 0→1 (20 wood). B reserves 1→2 (35 wood). C reserves 2→3 (30 wood, 5 iron).
2. A and B cancel.
3. C settles. Actual wall=0. Cost for 0→1 = (20, 0). `_min(30, 20) = 20` wood, `_min(5, 0) = 0` iron. Pays (20, 0). ✓
4. But if only A cancels: C settles at wall=1. Cost for 1→2 = (35, 0). `_min(30, 35) = 30` wood, `_min(5, 0) = 0` iron. Pays (30, 0) for a 35-wood upgrade. **5 wood underpaid.**

**Impact:** Negligible economic leak (bounded by max adjacent-level cost delta, ~5-10 wood). Requires 3+ concurrent reservations with cancellation and non-monotonic cost progression. Not economically rational to exploit given the clansman opportunity cost.

## LOW severity findings

### L1. `fromLevel`/`toLevel` stored in reservation structs but never read during settlement

`WallUpgradeReservation.fromLevel`, `.toLevel` (and analogous fields) are set during `_reserveWallUpgrade` but never referenced in `_settleWallUpgrade`. Settlement always uses `clan.wallLevel` (the live value). These fields are informational/decorative storage that consumes gas but serves no functional purpose. Could be removed to save gas.

### L2. Redundant guard in `_refundXUpgradeReservation` wrappers

All three `_refundWallUpgradeReservation`/`_refundBaseUpgradeReservation`/`_refundMonumentUpgradeReservation` functions check `if (!reservation.active) return;` and then call `_clearXUpgradeReservation`, which performs the same `!reservation.active` guard. The wrapper adds no behavior — it's a pass-through with a redundant check. Not harmful, just unnecessary indirection.

### L3. `CLANSMAN_CARRY_CAP` constant defined but unused

`ClanWorldConstants.CLANSMAN_CARRY_CAP = 10e18` is added to `IClanWorld.sol` but not referenced anywhere in the diff. `WOOD_YIELD_PER_TICK = 1e18` is used as an alias for `WOOD_CRIT_BONUS`, which is fine. `CLANSMAN_CARRY_CAP` appears to be a forward declaration for future use.

## Cross-cutting observations

**Reservation system architecture is sound.** The three-mapping design (reservations per clansman, pending counts per clan, reserved amounts per clan) correctly handles concurrent upgrades, cancellation, death-refund, and cross-clan isolation. The shared resource pool (`_reservedWoodByClan` etc.) correctly tracks reservations across all building types.

**Settlement simulation (`_simulateSettleToTick`) is consistent with real settlement.** The starvation check uses `_world.currentTick` in both real and simulated paths — since all simulated ticks ≤ `_world.currentTick`, the inequality `starvationStartsAtTick <= _world.currentTick` produces the same result as real settlement. The simulation correctly deactivates dead clansmen's missions without attempting reservation refunds (which would require state mutation incompatible with `view`).

**Score bit-packing is correct.** The layout `(monumentLevel:8 | timeComponent:64 | lootValue:176 | wallLevel:8)` occupies exactly 256 bits with no overlap. The `maxLootComponent` clamp prevents bleed into the time field. The insertion sort in `getRankings` is bounded by `MAX_CLAN_SCAN_FOR_RANKING = 24`, keeping gas predictable.

**R2 fixes introduced no regressions.** The dead-clansman settlement loop change (moving `DEAD` check after `!m.active` but before `settlesAtTick`) correctly ensures reservation cleanup happens at the first tick of settlement, not just at the mission's settle tick. Double-processing is prevented by the `m.active` guard. The `BuildWall → UpgradeWall` enum migration correctly deprecates `BuildWall` at validation while preserving ABI backward compatibility.

**Event ABI change:** `ResourcesDeposited` tick parameter narrowed from `uint64` to `uint32`. This is a breaking change for any existing indexers. Acceptable for a pre-production contract but worth noting for indexer teams.
EXIT_4_6=0
