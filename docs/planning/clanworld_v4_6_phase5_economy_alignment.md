# ClanWorld v4.6 Addendum — Phase 5 Economy Alignment (Path A: as-built canonization)

**Status:** Authoritative addendum, scoped to Phase 5 gathering / deposit / economy mechanics
**Read order:** This doc supersedes the gathering-yield and economy-rate language in `clanworld_v4_spec.md` §3.10, §4.3–4.13, `clanworld_v4_1_addendum.md` A5–A6, A10, and the economy-related constants in `clanworld_v4_2_state_schema_interface_spec.md` §5. When implementers or UAT runners find disagreement on yield rates, carry caps, or starvation semantics, this doc wins.
**Purpose:** Canonize the as-built Phase 5 economy as the authoritative Phase 5 ruleset for the hackathon submission, covering contract behavior, ABI/event naming, tests, and docs, and explicitly enumerate the v4 mechanics that diverged as DEFERRED-TO-RESTORATION.
**Audience:** Reviewers, UAT runners against the contract on Base Sepolia, future restoration work.

---

## 0. Why this doc exists — Path A decision (Liam, 2026-04-30)

A spec-vs-impl audit by Claude Opus 4.7 (1M ctx) — the "PR #193 spec-compliance UAT" report at [`docs/reviews/pr193-spec-compliance-uat.md`](../reviews/pr193-spec-compliance-uat.md) — confirmed that the Phase 5 economy implementation diverges from the v4 spec on 14 substantial mechanics. The core structural divergence is **action lifecycle**: the v4 spec modeled gathering as a continuous tick-by-tick yield loop ("resolve tick-by-tick until stopped or blocked"); the implementation uses a **rescheduling batched-action model** where each gather settlement represents 4 ticks of work and the mission keeps rescheduling until cap, depletion, or lock.

Two interpretive paths were available:

- **Path A (chosen)** — declare the implementation canonical, write this addendum as the new authoritative ruleset, defer the v4-specific mechanics to a post-hackathon restoration milestone.
- **Path B (rejected for hackathon timeline)** — declare the v4 spec canonical, treat the implementation as drift, rewrite the gathering pipeline to continuous per-tick semantics.

Path A was selected because:
1. The rescheduling batched-action model is deeply structural — the `getActionDuration / executesAtTick / settlesAtTick` mission shape was locked in Phases 3–4 (merged PRs #181/#183). Phase 5 inherits and extends it; reverting would invalidate downstream phases.
2. The hackathon timeline makes a gathering pipeline rewrite (Path B) untenable — multi-day lift for Phase 5.2–5.4, with regression risk across Phase 6–9.
3. The as-built code is internally consistent and well-tested for its own batched semantics. The multi-round super-swarm and per-issue PR pipeline already validated it.
4. Per-action yield magnitudes (4 ticks × yield_per_tick) are calibrated to the batched duty cycle, and can be rebalanced post-hackathon under restoration without touching architecture.

This doc canonizes the as-built mechanics. The 14 drift items are tracked as deferred-to-restoration on GH issue [#352](https://github.com/OmniPass-world/clan-world/issues/352) under the `spec-v4-restoration-post-hackathon` GH milestone (#25) for later evaluation.

---

## 1. Scope of this addendum

**In scope:** gathering mechanics (wood, iron, fish, wheat), deposit, carry caps, upkeep, starvation, wheat plot lifecycle, starting vault — as implemented in:

- `packages/contracts/src/ClanWorld.sol` — gather helpers, `_doDeposit`, `_applyUpkeep`, wheat plot regrow, starvation flag
- `packages/contracts/src/IClanWorld.sol` — economy constants, `Clan` struct vault fields, `WheatPlot` struct, carry fields
- `packages/contracts/test/Gathering.t.sol`, `ClanWorld.t.sol` — codified behavior

**Out of scope:** all non-economy mechanics. Bandit interactions with vault state (loot value getter — see `clanworld_v4_6_bandit_phase9_redesign.md`), winter mechanics (deferred — §9), market mechanics (Phase 6), deaths (Phase 10), gold/blueprint payouts. The rest of the v4 spec remains canonical unless overridden by a later addendum.

**Authoritative as of HEAD:** `9b67414f87ab6c83cf848fbe8a8bd7e3dcc03218` (origin/dev-phase-5-economy, the HEAD against which the spec-compliance UAT was run).

---

## 2. As-built rescheduling-action lifecycle

### 2.1 Action model

The v4 spec (§3.10) described gathering as a **continuous** action: "resolve tick-by-tick until stopped or blocked." The implementation uses a **rescheduling batched-action model**:

- Every gather action has `getActionDuration = 4` — the mission `settlesAtTick = actionStartTick + 4`.
- `_settleMissionForClansman` calls `_resolveAction` only when `tick >= settlesAtTick` — i.e., 4 ticks after the action starts.
- At resolution, the gather helper credits `YIELD_PER_TICK × 4` to the clansman's carry in one atomic call.
- If the relevant carry cap is not reached and the plot/resource remains usable, the mission stays active and `settlesAtTick` is rescheduled by another action duration.
- The worker transitions to WAITING only when the carry cap is reached, the wheat plot is depleted/locked/not harvestable, or another completion condition fires.

**There is no continuous per-tick yield loop.** One gather submission can produce multiple 4-tick settlements until cap/depletion, rather than terminating after a single settlement.

### 2.2 Gather mission lifecycle (canonical)

```
Submit gather order
  → validation: target-region correct (carry-full checked at resolution, not at submit)
  → action stamp: actionStartTick = currentTick, settlesAtTick = currentTick + 4
  → cooldown stamp on submission (A5)
per tick: _settleMissionForClansman runs
  → if tick < settlesAtTick: noop (traveling or waiting for batch to complete)
  → if tick >= settlesAtTick: resolve batch
      → gather helper called once
      → yield = YIELD_PER_TICK × 4 (± crit/RNG modifier per §3)
      → yield clamped to remaining carry capacity
      → if carry already at cap: _completeMission immediately (no yield)
      → carry += yield; emit ResourcesGathered
      → if carry cap reached or plot depleted/locked: _completeMission → worker WAITING
      → otherwise mission remains ACTING and settlesAtTick is advanced by 4 ticks
```

**After _completeMission:** worker is WAITING at gather region. Elder must travel home + deposit + submit new gather, or submit gather again from the same region (subject to cooldown).

### 2.3 Deposit lifecycle

Deposit is a **single-tick action** per spec §3.10 (unchanged by this addendum):

- `DEPOSIT_DURATION_TICKS = 1`
- Worker must be at own clan's `baseRegion`
- Atomically transfers all 4 carry resources to vault in one settlement
- Empty-carry deposit is a silent no-op (no event emitted)
- `ResourcesDeposited` event emitted on non-empty deposit with per-resource amounts + `atTick`

### 2.4 Cooldown semantics (A5 unchanged)

Per `clanworld_v4_1_addendum.md` A5 (unchanged): every successful mission submission starts cooldown (`cooldownEndsAtTs`). This applies to gather + deposit. A clansman whose cooldown is active cannot accept a new mission submission. This means the effective gather duty cycle is: **4 ticks ON + cooldown-period OFF per batch** (≈ 4 ticks ON + 7 ticks OFF at 20s/tick cadence, or 4 ticks ON + 5 ticks OFF at 60s/tick).

---

## 3. Per-action yields — canonical as-built values

### 3.1 Wood (Forest)

| Property | As-built canonical | Source |
|---|---|---|
| Base yield per batch | `WOOD_YIELD_PER_TICK × 4 = 1e18 × 4 = 4e18` | `IClanWorld.sol:50, 51`; `ClanWorld.sol:493` |
| Crit chance | `WOOD_CRIT_BPS = 1000` (10%) | `IClanWorld.sol:55` |
| Crit effect | multiply yield × 2 → **8e18 on crit** | `ClanWorld.sol:495-497` |
| RNG domain key | `keccak256(abi.encode("wood_crit", tickSeed, clansmanId, missionNonce, tick))` | `ClanWorld.sol:494` |
| Region | `REGION_FOREST` | — |

**Ratified design decisions:**
- **Crit shape is ×2 multiplicative** (not +1e18 additive as in v4 §4.7). Rationale: the as-built multiplier is simpler, internally consistent, and already tested. Note: v4 §4.7 describes crit as a per-tick additive bonus — in a continuous model this means +1e18 per crit tick; in the batched model the equivalent per-mission interpretation is ambiguous (4 rolls of +1e18 each, or a single roll applied to the whole batch). The ×2 multiplicative shape resolves this ambiguity with a single per-batch roll that is already codified in tests. Restoring additive crit is tracked as item #4 in §10.
- **Wood yield per tick is 1e18** (half of v4 spec's 2e18). Yield rate drift is a consequence of the batched model: 1e18/tick × 4-tick batch = 4e18 per submission. Restoration to 2e18/tick per spec is tracked as item #2 in §10.

### 3.2 Iron (Mountains)

| Property | As-built canonical | Source |
|---|---|---|
| Base yield per batch | `IRON_YIELD_PER_TICK × 4 = 1.25e17 × 4 = 5e17 = 0.5e18` | `IClanWorld.sol:57-58`; `ClanWorld.sol:524` |
| Gold bonus chance | `GOLD_FROM_IRON_BPS = 200` (2%) | `IClanWorld.sol:59` |
| Gold bonus amount | `GOLD_FROM_IRON_AMOUNT = 1e18` | `IClanWorld.sol:60` |
| Gold bonus roll cadence | **once per batch call** (not once per tick) | `ClanWorld.sol:539-548` |
| RNG domain key | `keccak256(abi.encode("iron_gold_bonus", ...))` | `ClanWorld.sol:543` |
| Region | `REGION_MOUNTAINS` | — |

**Ratified design decisions:**
- **Gold bonus rolls once per 4-tick batch** (not once per action tick). Sustained gold-bonus opportunity rate = 2% per batch vs spec's 2%/tick. Restoring per-tick roll cadence is tracked as item #6 in §10.
- **Iron per-tick rate is 0.125e18** (¼ of spec's 0.5e18/tick). Restoration tracked as item #5.

### 3.3 Fish — Docks (West/East Docks)

| Property | As-built canonical | Source |
|---|---|---|
| Hit chance | `FISH_DOCKS_BPS = 2500` (25%) | `IClanWorld.sol:65` |
| Yield on hit | `FISH_YIELD_PER_TICK × 4 = 25e16 × 4 = 1e18` | `IClanWorld.sol:63`; `ClanWorld.sol:563-578` |
| Yield on miss | 0 | — |
| Roll cadence | **once per batch call** | `ClanWorld.sol:563-578` |
| RNG domain key | `keccak256(abi.encode("fish_roll", ...))` | `ClanWorld.sol:564` |
| Regions | `REGION_WEST_DOCKS`, `REGION_EAST_DOCKS` | — |

**Sustained expected value per batch:** 25% × 1e18 = 0.25e18 fish per batch.

**Ratified design decision:** Roll cadence is once per batch (not once per action tick). Restoration tracked as item #7.

### 3.4 Fish — Deep Sea

| Property | As-built canonical | Source |
|---|---|---|
| Hit chance | `FISH_DEEP_BPS = 7500` (75%) | `IClanWorld.sol:66` |
| Yield on hit | `FISH_YIELD_PER_TICK × 4 = 25e16 × 4 = 1e18` | `IClanWorld.sol:63`; `ClanWorld.sol:594-609` |
| Yield on miss | 0 | — |
| Roll cadence | once per batch call | `ClanWorld.sol:594-609` |
| RNG domain key | `keccak256(abi.encode("fish_roll", ...))` | `ClanWorld.sol:595` |
| Region | `REGION_DEEP_SEA` | — |

**Sustained expected value per batch:** 75% × 1e18 = 0.75e18 fish per batch.

### 3.5 Wheat (West/East Farms)

| Property | As-built canonical | Source |
|---|---|---|
| Base yield per batch | `WHEAT_YIELD_PER_TICK × 4 = 5e18 × 4 = 20e18` | `IClanWorld.sol:62`; `ClanWorld.sol:646` |
| Regions | `REGION_WEST_FARMS` (plot 0), `REGION_EAST_FARMS` (plot 1) | `ClanWorld.sol:626-637` |
| Plot depletion | yield clamped to `plot.remainingWheat`; plot enters Regrowing when `remainingWheat == 0` | `ClanWorld.sol:648-659` |

**Ratified design decision:** Wheat per-tick rate is 5e18 (¼ of spec's 20e18/tick per continuous model). Restoration tracked as item #9.

---

## 4. Per-resource carry caps — canonical as-built values

These supersede the carry cap values in `clanworld_v4_spec.md` §4.3 and `clanworld_v4_2_state_schema_interface_spec.md` §5.

| Resource | Spec value (deprecated) | As-built canonical | Source |
|---|---|---|---|
| Wood | 15e18 | **15e18** (`WOOD_CAP`; `CLANSMAN_CARRY_CAP` remains legacy 10e18) | `IClanWorld.sol:43-44` ✅ unchanged |
| Iron | 5e18 | **5e18** | `IClanWorld.sol:45` ✅ unchanged |
| Wheat | 40e18 | **40e18** | `IClanWorld.sol:46` ✅ unchanged |
| Fish | 8e18 | **8e18** | `IClanWorld.sol:47` ✅ unchanged |

**Ratified design decision:** Wood carry cap is **15e18**. `WOOD_CAP` is independent from the legacy generic `CLANSMAN_CARRY_CAP` constant (10e18).

**Carry enforcement mechanics (unchanged from spec):** each gather helper enforces the per-resource cap independently. Yield is clamped to `cap - currentCarry` if it would overflow. If carry is already at cap when resolution fires, the mission terminates immediately (no yield credited). This matches spec §4.8.

---

## 5. Upkeep and starvation semantics

### 5.1 Per-tick upkeep

Per living clansman per tick (unchanged from spec):

| Resource | Rate | Source |
|---|---|---|
| Wheat | `WHEAT_UPKEEP_PER_CLANSMAN = 1e18` | `IClanWorld.sol:69` |
| Fish | `FISH_UPKEEP_PER_CLANSMAN = 1e17` | `IClanWorld.sol:70` |

Upkeep applies only when `clan.livingClansmen > 0`. If the vault cannot fully satisfy the wheat or fish demand, the respective vault field is drained to 0 (not negative).

Execution order in `_settleClan` (per tick): `_applyUpkeep` → wheat-plot regrow → per-clansman mission settlement. Upkeep fires before any mission resolution in that tick, which means same-tick deposit cannot rescue that tick's upkeep (A6 compliant).

### 5.2 Starvation onset — next-tick ratification

**Ratified design decision:** starvation activates on the **next tick** after the upkeep failure, matching v4 §4.11.

v4 §4.11 states: "If the vault cannot satisfy both wheat and fish demands in full, **starvation status begins on the next tick**." The implementation sets `clan.starvationStartsAtTick = tick + 1` inside `_applyUpkeep`.

Starvation effects are evaluated against the tick being resolved. During lazy settlement, mission resolution uses the loop's `tick`, not live `_world.currentTick`, so an upkeep failure on tick `T` does not penalize actions resolved on tick `T`; penalties begin for actions resolved on tick `T + 1`.

### 5.3 Starvation effects (unchanged from spec, A10)

While `_isStarving == true`:

- All gather outputs are reduced by 50% (`yield = yield / 2` applied in wood/iron/fish-docks/fish-deep/wheat helpers before carry assignment)
- Defense contribution = 0 (starving clans are ineligible for `_resolveBanditAttack` defender path)
- No death from starvation outside winter (summer starvation = debuff only, matches A10)

Note: wheat plot depletion uses the post-halving yield value when calculating `plot.remainingWheat -= yield`, so the plot drains at the same rate as carry accumulation (no double-deduct bug).

### 5.4 Starvation recovery

Starvation clears in the same `_applyUpkeep` tick where the vault CAN satisfy both wheat + fish in full: `if (!starving && clan.starvationStartsAtTick != 0) clan.starvationStartsAtTick = 0`. Recovery is immediate (same tick as successful upkeep payment).

---

## 6. Wheat plot lifecycle

| State | Condition | Behavior |
|---|---|---|
| `Harvestable` | `remainingWheat > 0` | Clansman can harvest; yield clamped to min(20e18, remainingWheat) per batch |
| `Regrowing` | `remainingWheat == 0` | No harvest; regrow timer: `regrowUntilTick = tick + WHEAT_PLOT_REGROW_TICKS` |
| `WinterLocked` | Winter active (see §9) | No new wheat harvest orders; queued harvest missions complete with no yield if they resolve while locked |

**Regrow → Harvestable:** when `tick >= regrowUntilTick`, the per-tick `_settleClan` loop resets the plot: `state = Harvestable; remainingWheat = 100e18`.

**Starting state per `mintClan`:** both West and East Farms start `Harvestable` at 100e18.

`WHEAT_PLOT_REGROW_TICKS` is the canonical regrow duration. Both plots (`targetRegion == REGION_WEST_FARMS` → `plotIdx = 0`, `targetRegion == REGION_EAST_FARMS` → `plotIdx = 1`) follow identical mechanics.

---

## 7. Deposit mechanics (unchanged from spec)

| Property | Value | Source |
|---|---|---|
| Duration | `DEPOSIT_DURATION_TICKS = 1` (single-tick action) | `ClanWorld.sol:77, 1804-1806` |
| Location requirement | worker at own `clan.baseRegion` | `ClanWorld.sol:670-674, 1611-1614` |
| Transfer semantics | all 4 carry resources → vault atomically; carry fields zeroed | `ClanWorld.sol:682-695` |
| Empty-carry behavior | silent no-op, no event | `ClanWorld.sol:675-680` |
| Event | `ResourcesDeposited(clanId, woodDelta, ironDelta, wheatDelta, fishDelta, atTick)` | `IClanWorld.sol:541-549` |
| Error on wrong region | `ERR_NOT_AT_HOMEBASE` | `IClanWorld.sol:171` |

---

## 8. Starting vault and gold (§12.4, §12.5 — unchanged from spec)

| Field | Value | Source |
|---|---|---|
| `vaultWood` | 20e18 | `ClanWorld.sol:1037` |
| `vaultIron` | 0 | `ClanWorld.sol:1038` |
| `vaultWheat` | 20e18 | `ClanWorld.sol:1039` |
| `vaultFish` | 2e18 | `ClanWorld.sol:1040` |
| `goldBalance` | 3e18 | `ClanWorld.sol:1034` |

---

## 9. Winter mechanics

Winter mechanics are wired into heartbeat advancement and per-clan upkeep:

| Mechanic | v4 spec rule | Impl status | Source |
|---|---|---|---|
| Winter wheat plot lockdown (§7.4) | plots enter `WinterLocked` at winter start; no harvest | IMPLEMENTED — heartbeat locks all wheat plots at winter start; harvest orders are rejected during winter; queued harvests complete with no yield if the plot is locked | `_lockWheatPlotsForWinter`, `_gatherWheat`, `_validateOrder` |
| Winter upkeep 2× (§7.3) | wheat + fish consumption doubles during winter | IMPLEMENTED — `_applyUpkeep` multiplies wheat and fish needs by `WINTER_UPKEEP_MULTIPLIER_BPS = 20000` while winter is active | `_applyUpkeep` |
| Winter wood burn (§7.3) | wood burns during winter | IMPLEMENTED — `_applyUpkeep` burns `WINTER_WOOD_BURN_PER_BASE + livingClansmen × WINTER_WOOD_BURN_PER_CLANSMAN`; shortages increment `coldDamage` and can degrade walls or kill clansmen | `_applyUpkeep`, `_applyColdDamageConsequence` |
| WinterLocked → Regrowing at winter end (v4_2 §7.4) | on winter end, locked plots → Regrowing | IMPLEMENTED — heartbeat restarts locked plots as `Regrowing` with `regrowUntilTick = currentTick + WHEAT_PLOT_REGROW_TICKS` | `_restartWheatPlotsAfterWinter` |

**Behavior during winter (as-built):** non-wheat gathering and deposit work as described in §2–§7. Wheat plots are locked, food upkeep doubles, wood burn applies, and sustained cold shortages can degrade walls or kill clansmen. Cold damage resets after winter ends.

**Additional vestigial constant:** `WOOD_CRIT_BONUS` (at `IClanWorld.sol:52-54`) is declared but unused — the implementation performs the wood crit inline as a ×2 multiply rather than reading this constant. It is flagged here as dead code for a future cleanup pass.

---

## 10. Deferred to v4-restoration milestone (`spec-v4-restoration-post-hackathon`)

The following 14 drift items are tracked for evaluation under the `spec-v4-restoration-post-hackathon` GH milestone (#25) and the umbrella tracking issue [#352](https://github.com/OmniPass-world/clan-world/issues/352). Restoration is conditional on post-hackathon evaluation.

| # | v4 spec mechanic | As-built behavior | Restoration cost (rough) |
|---|---|---|---|
| 1 | **Continuous tick-by-tick yield** (§3.10) — gather resolves once per tick, looping until stopped or blocked | Rescheduling batched model: each settlement represents 4 ticks of work; mission remains active and reschedules until cap/depletion/lock | Medium — would require changing `getActionDuration`/settlement cadence from 4-tick batches to per-tick resolution |
| 2 | **Wood base yield 2e18/tick** (§4.7) — 2e18 per action tick (with continuous model) | 1e18/tick × 4 = 4e18/batch in batched model | Tiny constant change — but only meaningful after item #1 restores continuous model |
| 3 | **Wood crit chance 20%** (§4.7) | `WOOD_CRIT_BPS = 1000` (10%) | Tiny — 1 constant |
| 4 | **Wood crit shape additive +1e18** (§4.7) — crit ADDS +1e18 per tick (spec ambiguous on per-tick vs per-mission cadence in batched model) | Crit DOUBLES yield → 8e18/batch | Small — modify crit branch in `_gatherWood` |
| 5 | **Iron base yield 0.5e18/tick** (§4.7) | 0.125e18/tick × 4 = 0.5e18/batch | Tiny constant — but meaningful only after #1 |
| 6 | **Iron gold bonus per-tick roll** (§4.7) — roll at every action tick | Rolls once per 4-tick batch call | Small — loop the roll or roll once per tick in continuous model |
| 7 | **Fish docks 25%/tick × 1e18** (§4.7) | 25% per batch call × 1e18 | Tiny constant — meaningful after #1 |
| 8 | **Fish deep sea 75%/tick × 1e18** (§4.7) | 75% per batch call × 1e18 | Tiny constant — meaningful after #1 |
| 9 | **Wheat harvest rate 20e18/tick** (§4.9) | 5e18/tick × 4 = 20e18/batch | Tiny constant — meaningful after #1 |
| 10 | **Wood carry cap 15e18** (§4.3) | IMPLEMENTED — `WOOD_CAP = 15e18`; `CLANSMAN_CARRY_CAP` remains legacy 10e18 | Done |
| 11 | **Winter wheat plot lockdown** (§7.4) — plots enter `WinterLocked` at winter start; no harvest | IMPLEMENTED — plots lock at winter start and restart regrowing after winter | Done |
| 12 | **Winter upkeep 2× multiplier** (§7.3) — wheat + fish consumption doubles in winter | IMPLEMENTED — `_applyUpkeep` doubles wheat and fish needs during winter | Done |
| 13 | **Winter wood burn 1e18/base/tick** (§7.3) | IMPLEMENTED — winter burns base + per-clansman wood and shortages apply cold damage | Done |
| 14 | **Starvation onset next-tick** (§4.11) — starvation begins on the tick after upkeep failure | IMPLEMENTED — `_applyUpkeep` records `starvationStartsAtTick = tick + 1`; settlement checks starvation against the tick being resolved | Done |

Items 11–13 were originally tracked as restoration work and are now implemented in the current contract.

---

## 11. Constants — as-built canonical values

These supersede the economy-related constants in `clanworld_v4_2_state_schema_interface_spec.md` §5 for the listed entries.

| Constant | Spec value (deprecated) | As-built canonical | Source |
|---|---|---|---|
| `WOOD_YIELD_PER_TICK` | 2e18 (implied by continuous) | **1e18** | `IClanWorld.sol:50` |
| `WOOD_CRIT_BONUS` | +1e18 additive | **×2 multiplicative (constant declared at `IClanWorld.sol:52-54` but unused by impl; impl does ×2 inline)** | `ClanWorld.sol:495-497` |
| `WOOD_CRIT_BPS` | 2000 (20%) | **1000 (10%)** | `IClanWorld.sol:55` |
| `WOOD_CAP` | 15e18 | **15e18** | `IClanWorld.sol:44` ✅ unchanged |
| `IRON_YIELD_PER_TICK` | 5e17 (implied by continuous) | **1.25e17** | `IClanWorld.sol:58` |
| `GOLD_FROM_IRON_BPS` | 200 (2%) | **200 (2%)** ✅ unchanged | `IClanWorld.sol:59` |
| `GOLD_FROM_IRON_AMOUNT` | 1e18 | **1e18** ✅ unchanged | `IClanWorld.sol:60` |
| `FISH_DOCKS_BPS` | 2500 (25%) | **2500 (25%)** ✅ unchanged | `IClanWorld.sol:65` |
| `FISH_DEEP_BPS` | 7500 (75%) | **7500 (75%)** ✅ unchanged | `IClanWorld.sol:66` |
| `FISH_YIELD_PER_TICK` | 1e18 (implied by continuous) | **25e16 (= 2.5e17)** | `IClanWorld.sol:63` |
| `WHEAT_YIELD_PER_TICK` | 20e18 (implied by continuous) | **5e18** | `IClanWorld.sol:62` |
| `WHEAT_CAP` | 40e18 | **40e18** ✅ unchanged | `IClanWorld.sol:46` |
| `IRON_CAP` | 5e18 | **5e18** ✅ unchanged | `IClanWorld.sol:45` |
| `FISH_CAP` | 8e18 | **8e18** ✅ unchanged | `IClanWorld.sol:47` |
| `WHEAT_UPKEEP_PER_CLANSMAN` | 1e18 | **1e18** ✅ unchanged | `IClanWorld.sol:69` |
| `FISH_UPKEEP_PER_CLANSMAN` | 1e17 | **1e17** ✅ unchanged | `IClanWorld.sol:70` |
| `WHEAT_PLOT_REGROW_TICKS` | 4 | **4** ✅ unchanged | `IClanWorld.sol:75` |
| `WHEAT_PLOT_STARTING_WHEAT` | 100e18 | **100e18** ✅ unchanged | `IClanWorld.sol:76` |
| `WINTER_UPKEEP_MULTIPLIER_BPS` | 20000 (2×) | **20000 (used during winter upkeep — see §9)** | `IClanWorld.sol:72` |
| `WINTER_WOOD_BURN_PER_BASE` | 1e18 | **1e18 (used during winter wood burn — see §9)** | `IClanWorld.sol:71` |
| `WINTER_WOOD_BURN_PER_CLANSMAN` | 0.5e18 | **0.5e18 (used during winter wood burn — see §9)** | `IClanWorld.sol:73` |

---

## 12. UAT runners — what to expect

When running interactive UAT against this contract, **expect the as-built mechanics (this addendum), not v4 §3.10 / §4.3–4.13**. Specifically:

- ✅ Each gather order yields 4-tick batches and reschedules until the relevant carry cap is reached or the resource/plot becomes unavailable.
- ✅ Wood: 4e18 per batch base, 8e18 on crit (10% chance) — NOT 2e18/tick continuous
- ✅ Iron: 0.5e18 per batch, 2% chance of +1e18 gold bonus per batch — NOT 0.5e18/tick continuous
- ✅ Fish docks: 25% × 1e18 per batch — NOT 25% × 1e18/tick
- ✅ Fish deep sea: 75% × 1e18 per batch — NOT 75% × 1e18/tick
- ✅ Wheat: 20e18 per batch — NOT 20e18/tick continuous
- ✅ Wood carry cap is 15e18 via `WOOD_CAP`; the legacy generic `CLANSMAN_CARRY_CAP` remains 10e18.
- ✅ Starvation activates on the NEXT TICK after upkeep failure, matching v4 §4.11
- ✅ Wheat plots lock during winter; new wheat harvest orders return `ERR_WINTER_LOCKED`, and queued harvests against locked plots produce no yield.
- ✅ Winter doubles wheat and fish upkeep.
- ✅ Winter burns wood per base plus per living clansman; shortages accrue cold damage that can degrade walls or kill clansmen.

**Aggregate throughput note (for Phase 6 market calibration):** Rescheduled 4-tick batches produce lower and chunkier throughput than a naive reading of the v4 per-tick yield rates would imply. Plan market exchange rates and upgrade costs against per-batch figures and carry-cap stop conditions, not per-tick spec values.

If a UAT scenario expects v4-spec per-tick yields or one-shot gather termination, it will fail against this contract. Use this addendum as the oracle.

---

## 13. References

- **Spec-compliance UAT report:** [`docs/reviews/pr193-spec-compliance-uat.md`](../reviews/pr193-spec-compliance-uat.md) — full mechanic-by-mechanic table, test-coverage gap analysis, UAT scenario predictions (in flight: PR #342)
- **Path A precedent (bandit mechanics):** [`docs/planning/clanworld_v4_6_bandit_phase9_redesign.md`](./clanworld_v4_6_bandit_phase9_redesign.md) (in flight: PR #341)
- **Source v4 docs being superseded for economy mechanics:**
  - `clanworld_v4_spec.md` §3.10, §4.3–4.13, §7.3–7.5, §12.4–12.5
  - `clanworld_v4_1_addendum.md` A5, A6, A10
  - `clanworld_v4_2_state_schema_interface_spec.md` §5 (economy constants listed in §11 above)
  - `clanworld_v4_3_schema_patch.md` E (RNG domain keys — fully matched, unchanged), L (starvationStartsAtTick — matched)
- **Implementation HEAD canonized by this addendum:** `9b67414f87ab6c83cf848fbe8a8bd7e3dcc03218`
- **Restoration tracking:** GH milestone `spec-v4-restoration-post-hackathon` (#25) and tracking issue [#352](https://github.com/OmniPass-world/clan-world/issues/352)
