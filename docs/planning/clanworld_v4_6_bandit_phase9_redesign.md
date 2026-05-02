# ClanWorld v4.6 Addendum — Bandit Phase 9 Redesign (Path A: as-built canonization)

**Status:** Authoritative addendum, scoped to bandit mechanics only
**Read order:** This doc supersedes the bandit-mechanic language in `clanworld_v4_spec.md` §6, `clanworld_v4_1_addendum.md` (A3, A4, A11), and the bandit-relevant constants in `clanworld_v4_2_state_schema_interface_spec.md` §5 / §7.7 / §7.10 / §7.11. When implementers find disagreement on bandit behavior, this doc wins.
**Purpose:** Canonize the as-built Phase 9 bandit implementation in `packages/contracts/src/ClanWorld.sol` as the new authoritative ruleset for the hackathon submissions, and explicitly enumerate the v4 mechanics that did not ship as DEFERRED-TO-RESTORATION.
**Audience:** Reviewers, UAT runs against the contract on World Chain Sepolia / Base Sepolia, future restoration work.

---

## 0. Why this doc exists — Path A decision (Liam, 2026-04-30)

A spec-vs-impl audit by Claude Opus 4.7 (1M ctx) — the "PR #194 spec-compliance UAT" report at [`docs/reviews/pr194-spec-compliance-uat.md`](../reviews/pr194-spec-compliance-uat.md) — confirmed that the Phase 9 bandit implementation diverges from the v4 spec on numerous core mechanics. Eleven of those drifts are significant (rampage path, vault loot theft, attack-attempt cap, threshold combat, per-clansman split, etc.).

Two interpretive paths were available:

- **Path A (chosen)** — declare the implementation canonical, write this addendum as the new authoritative ruleset, defer the v4-specific mechanics to a post-hackathon restoration milestone.
- **Path B (rejected for hackathon timeline)** — declare the v4 spec canonical, treat the implementation as drift, dispatch a Phase 9.5 rework to align.

Path A was selected because (1) the as-built code is internally consistent and well-tested (the multi-round super-swarm + per-issue PR pipeline already validated it against itself); (2) Phase 9 work has fully landed and downstream phases (10 winter, market) have been built on top of the as-built bandit mechanics; (3) restoring the v4 ruleset is a multi-day Phase 9.5 lift that isn't on the hackathon critical path; (4) the v4 spec was written before Phase 9 implementation experimentation, and several of its mechanics (rampage path, vault theft, terminal escape) trade off complexity for tension that the as-built simpler model has been shown to deliver well enough.

This doc canonizes the as-built mechanics. The eight largest drifted mechanics are tracked as a deferred-to-restoration list in §6 and on GH issue [#340](https://github.com/OmniPass-world/clan-world/issues/340) under the `spec-v4-restoration-post-hackathon` milestone for later evaluation.

---

## 1. Scope of this addendum

**In scope:** every bandit mechanic visible to a player or a UAT runner, as implemented in:

- `packages/contracts/src/ClanWorld.sol` — bandit lifecycle, attack resolution, defender contribution, defeat reward distribution
- `packages/contracts/src/IClanWorld.sol` — bandit struct fields, state enum, ABI surface, constants
- `packages/contracts/test/Bandit.t.sol`, `BanditAttackResolution.t.sol`, `BanditSpawn.t.sol` — codified behavior

**Out of scope:** all non-bandit mechanics. The rest of the v4 spec (clan minting, missions, market, winter, deaths, gold/blueprint payouts unrelated to bandits) remains canonical.

**Authoritative as of HEAD:** `218f9020ecb0f4b8277ef59dd55de8e004404d80` (the HEAD against which the spec-compliance UAT was run; identical to PR #194's merged commit on `dev-phase-9-bandits`).

---

## 2. As-built bandit lifecycle

### 2.1 State machine

The implementation defines **7 states** (vs. 5 in v4 §6.5):

```
None → Spawned → Camped → Attacking → (Defeated | Escaped) → Resting → Camped → Attacking → ...
```

| State | Duration | Transition trigger | Notes |
|---|---|---|---|
| `None` | (sentinel) | Default zero value, indicates no troop | Defensive — prevents zero-init from looking like a real state |
| `Spawned` | 1 tick | Heartbeat `_advanceBanditStates` | Settles candidate region, then transitions to `Camped` |
| `Camped` | 3 ticks (`BANDIT_CAMP_TICKS`) | Tick counter expires | Picks attack target on exit (NOT at attack resolution time) |
| `Attacking` | 1 tick | Heartbeat resolves attack via `_resolveBanditAttack` | Either Defeated (if defenders win threshold) or Escaped (if attack lands as damage event) |
| `Defeated` | terminal | n/a | Bandit removed; carry distributed; blueprint awarded to target clan |
| `Escaped` | 2 ticks | Tick counter expires → `Resting` | NON-TERMINAL — distinguishes from spec's terminal escape |
| `Resting` | 2 ticks | Tick counter expires → `Camped` | Bandit stays in the same region; no rampage |

**Effective spawn-to-attack interval = 4 ticks** (1 Spawned + 3 Camped). After the first attack, the steady-state cycle is `Attacking → Escaped(2) → Resting(2) → Camped(3) → Attacking` = 7 ticks per attempt.

**Bandit `region` is set on spawn and never changes.** A bandit lives in its spawn region for its entire lifecycle. There is no rampage path, no inter-region movement.

### 2.2 Spawn cadence

| Constant | Value | Source |
|---|---|---|
| `BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS` | 1000 (10%) | `ClanWorld.sol:91` |
| `BANDIT_SPAWN_MAX_PROBABILITY_BPS` | 8000 (80%) | `ClanWorld.sol:92` |
| `MIN_SPAWN_COOLDOWN_TICKS` (= `BANDIT_COOLDOWN_TICKS`) | 10 | `ClanWorld.sol:90` |
| `MAX_TOTAL_BANDITS` | 8 | `ClanWorld.sol:93` |
| `MAX_BANDITS_PER_REGION` | 3 | `ClanWorld.sol:94` |

**Per-tick spawn check** (per region, with per-region cooldown timer):

1. If region cooldown active → skip
2. If active bandit count for region ≥ `MAX_BANDITS_PER_REGION` → skip
3. If global active bandit count ≥ `MAX_TOTAL_BANDITS` → skip
4. Probability accumulator for region += 1000 bps (capped at 8000)
5. Roll RNG vs accumulator → if hit, spawn this region; otherwise carry accumulator forward to next tick
6. On spawn: reset that region's accumulator + start that region's cooldown timer (10 ticks)

**Up to 8 bandits can be active simultaneously globally** (vs spec's "at most 1"), with up to 3 per region.

**Cooldown is per-region, not global, and starts at spawn time, not at resolution time.** A defeat in Forest does not pause Mountains spawns; Mountains can still roll its own spawn next tick.

### 2.3 Spawn region selection

Eligible regions are determined by **clansman presence**, not by hard-coded ID. The implementation does NOT exclude region 3 (UnicornTown) or region 8 (DeepSea); a clansman traveling through those regions on a market trip can make them spawn-eligible.

**Region selection is loot-and-clansman weighted** (NOT uniform random — see `ClanWorld.sol:2230-2269`):

- `weights[r] += 100 + lootValue / 1e18`  (base + loot bonus)
- `weights[r] += 25 * clansmenInRegion(r)`  (per-clansman bonus)

Then RNG-weighted pick across `weights[]`. This biases bandits toward loot-rich, populated regions — intentional per inline code comment ("heuristic to make bandits feel pressuring").

### 2.4 Bandit strength

**No tier system.** Each spawned bandit has a uniform-random `strength` value in `[100, 250]`:

- `MIN_BANDIT_SPAWN_STRENGTH = 100` (`ClanWorld.sol:108`)
- `BANDIT_SPAWN_STRENGTH_SPREAD = 151` (`ClanWorld.sol:109`)
- `attackPower = MIN_BANDIT_SPAWN_STRENGTH + rng % BANDIT_SPAWN_STRENGTH_SPREAD`

The struct field `tier` from v4_3 G is absent. The ABI emits `tier: 0` for backward compatibility but it is not load-bearing.

### 2.5 Targeting

At `Camped → Attacking` transition (one tick before resolution), the implementation picks a target:

- Filters to clans with `baseRegion == bandit.region` (matches spec §6.8)
- Eager-settles the eventually-picked target (per-target settlement; does NOT eager-settle ALL bases in region as v4_1 A4 demands)
- Ranks remaining bases by loot value: `wood + wheat + 2*fish + 4*iron` (matches spec §6.9 — gold is NOT counted)
- Tiebreak among equal-loot bases: **uniform random across ties** (NOT lowest-clanId as spec §6.8 requires)
- If no eligible base in region → bandit transitions to `Escaped` (spec wanted noop "stay in region, try next tick"; impl marks Escaped which is non-terminal here, so functionally similar but surfaces as Escaped state in ABI)

---

## 3. As-built combat model

The implementation uses a **damage-based HP model** (vs spec's threshold-comparison model). On `Attacking → resolution` tick:

### 3.1 Defender contribution roll

For each clan with at least one clansman currently on a `DefendBase` mission **at home base**:

- For each such defending clansman → RNG roll uniform in `[0, 100]` → contributes that as defense damage
- Starving clans contribute 0 (matches spec A9 — see `_isStarving` filter at `ClanWorld.sol:1920, 1950`)
- WAITING-at-home clansmen (not on `DefendBase` mission) contribute 0 — they do NOT count for defense (spec §6.13 wanted them to contribute 5 each; impl ignores them)

Total defender damage = sum across all defending clansmen.

### 3.2 Attack outcome — damage absorption order

Compare `totalClansmanDefense` vs `2 * attackPower`:

**Outcome A — clean defeat:**
- If `totalClansmanDefense >= 2 * attackPower` → bandit defeated, no other damage applied
- Note: spec's threshold was `1x`, impl uses `2x` — defenders need twice as much firepower
- Bandit transitions to `Defeated` → blueprint reward awarded → carry distributed (carry is always 0 in production; see §3.3)

**Outcome B — damage event (loss path):**
- If `totalClansmanDefense < 2 * attackPower` → damage = `2 * attackPower - totalClansmanDefense`
- Damage absorbed in tiers:
  1. Wall HP first: `WALL_HP_PER_LEVEL = 100`. Wall absorbs up to `wallLevel * 100` damage. Wall level decrements by 1 per attack that hits the wall.
  2. Base HP next: `BASE_HP_PER_LEVEL = 25`. Base absorbs up to `baseLevel * 25` damage.
  3. Clansman casualties: if damage exceeds combined wall+base HP, **clansmen die** to absorb the residual. This contradicts spec §6.15 which says no clansman casualties from bandits in v1.
  4. If all clansmen die → clan is marked dead.
- Bandit transitions to `Escaped` (which is non-terminal in this impl).

### 3.3 No vault loot theft

**The implementation does not implement vault loot theft.** Spec §6.15C and §6.16 mandate that a successful attack steals 20% of each vault resource (wood, wheat, fish, iron) into the bandit's `carryX` fields. The impl:

- Has `BanditTroop.carryWood/Iron/Wheat/Fish/Gold` struct fields (`IClanWorld.sol:310-314`) but no production code path ever writes to them. Only the test harness `setBanditCarry` populates these.
- The constants `BANDIT_BASE_STEAL_BPS = 2000` (`IClanWorld.sol:71`) and `BANDIT_DROP_TO_DEFENDERS_BPS = 5000` (`IClanWorld.sol:72`) are declared but never referenced by production code.
- The `BanditAttackResolved` event hardcodes `stolenWood/Iron/Wheat/Fish` to 0 (`ClanWorld.sol:1824-1827`).

**Net effect:** target clan vaults are unchanged by bandit attacks. Bandits do damage to wall/base/clansmen but never extract physical loot.

### 3.4 No attack-attempt cap

Spec §6.12 requires bandits to be removed from the world after 6 attempts (7th = ESCAPED + carried loot burned + troop deleted). The impl has no `attackAttemptsMade` counter; the field is absent from the struct (`IClanWorld.sol:303-315`) and the ABI returns `attackAttemptsMade: 0` always. Bandits cycle indefinitely until either the target clan dies or defenders defeat them.

---

## 4. As-built defender contributions and rewards

### 4.1 Eligibility rule

A clansman counts as a defender **if and only if** all of the following hold:

- Their clan is the bandit's target clan (only target-clan clansmen can defend in the impl)
- They are alive (`!_isClansmanDead`)
- They are at home base (mission location is the home region)
- Their active mission action is `DefendBase`
- Their clan is not starving (`!_isStarving(defenderClan)`)

WAITING-at-home (no active mission, or mission action ≠ `DefendBase`) clansmen do NOT contribute. This is a behavioral departure from spec §6.13 which wanted them to contribute 5 each.

### 4.2 Defense damage roll

Per-clansman: uniform RNG roll in `[0, 100]`. Sum across all eligible defenders = `totalClansmanDefense`.

The roll can return 0; impl does NOT filter zero-roll defenders from reward eligibility (every active defender is reward-eligible regardless of actual roll).

### 4.3 Defeat reward distribution

On `Defeated` outcome:

- **Blueprint reward:** 1e18 Blueprint deposited into target clan's vault. (Spec §6.17 also called for 1e18 Gold to the same vault; impl omits the gold portion.)
- **Carry distribution:** 100% of bandit's carry is split equally among defending clans. Distribution is **per-clan**, not per-clansman: each unique clan with ≥1 active defender gets exactly 1 share, regardless of how many of its clansmen are defending. (Spec A11 wanted per-clansman granularity.)
- **Whole-token rounding:** integer division per-resource, residue burned.
- **Carry is zero in practice** (see §3.3) — so this distribution code only fires non-zero in test harness scenarios with `setBanditCarry`.

### 4.4 Defender registry semantics

- `DefendBase` is a persistent mission — a clansman set to defend stays defending across multiple bandit attacks until reassigned, killed, or until the defended clan dies (matches v4.2 §8.2).
- Defender registries are cleaned on clansman death (`_clearDefender` at `ClanWorld.sol:565-588`) and on defended-clan death (`_releaseDefendersForDeadTarget` at `ClanWorld.sol:2771-2792`).
- Dead-target cleanup transitions any active bandit attack against a dead clan to `Escaped` (`_abortBanditAttacksForDeadTarget` at `ClanWorld.sol:590-608`).

---

## 5. Constants — as-built canonical values

These supersede the bandit-related constants in `clanworld_v4_2_state_schema_interface_spec.md` §5.

| Constant | Spec value (deprecated) | As-built canonical | Source |
|---|---|---|---|
| `BANDIT_COOLDOWN_TICKS` | 10 | 10 | `ClanWorld.sol:90` ✅ unchanged |
| `BANDIT_CAMP_TICKS` | 3 | 3 | `ClanWorld.sol:1605, 1612` ✅ unchanged |
| `BANDIT_REST_TICKS` | 2 (post-attack) | 2 (in `Resting`); 2 additional in `Escaped` | `ClanWorld.sol:1689-1697` |
| `BANDIT_MAX_ATTEMPTS` | 6 | not implemented | — |
| `BANDIT_BASE_STEAL_BPS` | 2000 (20%) | declared, not referenced | `IClanWorld.sol:71` |
| `BANDIT_DROP_TO_DEFENDERS_BPS` | 5000 (50%) | declared, not referenced; impl uses 100% | `IClanWorld.sol:72` |
| Spawn base chance | 5% | 10% | `ClanWorld.sol:91` |
| Spawn increment per missed tick | +1% | +10% | `ClanWorld.sol:91` |
| Spawn cap | 20% | 80% | `ClanWorld.sol:92` |
| `MAX_TOTAL_BANDITS` | (implicitly 1) | 8 | `ClanWorld.sol:93` |
| `MAX_BANDITS_PER_REGION` | (n/a) | 3 | `ClanWorld.sol:94` |
| `MIN_BANDIT_SPAWN_STRENGTH` | (n/a; tier 1 = 30) | 100 | `ClanWorld.sol:108` |
| `BANDIT_SPAWN_STRENGTH_SPREAD` | (n/a; tier-fixed) | 151 → strength uniform `[100,250]` | `ClanWorld.sol:109` |
| `WALL_HP_PER_LEVEL` | (n/a; threshold = 10×wall) | 100 | `ClanWorld.sol:111` |
| `BASE_HP_PER_LEVEL` | (n/a; threshold = 5×base) | 25 | `ClanWorld.sol:112` |

The state enum is also expanded:

| State | v4 spec (§6.5) | As-built (`IClanWorld.sol:107-115`) |
|---|---|---|
| `None` | — | 0 (sentinel) |
| `Spawned` | — | 1 (1-tick pre-camp) |
| `CAMPING` / `Camped` | yes | 2 |
| `RESTING` / `Resting` | yes | 3 |
| `ATTACKING` / `Attacking` | yes | 4 |
| `DEFEATED` / `Defeated` | yes (terminal) | 5 (terminal) |
| `ESCAPED` / `Escaped` | yes (terminal in spec) | 6 (NON-terminal in impl; cycles back to Resting) |

---

## 6. Deferred to restoration milestone (`spec-v4-restoration-post-hackathon`)

The following v4 mechanics are **explicitly NOT being implemented for the hackathon submissions**. They are tracked for evaluation under the `spec-v4-restoration-post-hackathon` GH milestone. Restoration is conditional on hackathon timeline and the post-hackathon evaluation that the original v4 mechanics deliver more gameplay value than the as-built simpler model.

| # | v4 spec mechanic | As-built behavior | Restoration cost (rough) |
|---|---|---|---|
| 1 | **Vault loot theft** (§6.15C, §6.16) — bandits steal 20% of wood/wheat/fish/iron on failed defense, accumulate carry until defeat/escape | Bandits never extract loot; carry remains 0 in production | Medium — re-wire `_resolveBanditAttack`, write carry update path, plumb through `BanditAttackResolved` event |
| 2 | **Rampage path** (§6.10, §6.11) — bandit moves 1→2→5→7→6→4→1 clockwise on completion of each attack-rest cycle | Bandit stays in spawn region forever | Medium — add region-transition logic on rest-end, ensure single-troop-per-rampage invariant if combined with #5 |
| 3 | **Attack-attempt cap** (§6.12) — 6 attempts max, 7th = ESCAPED + carry burned + troop removed | No counter; bandits cycle indefinitely | Small — add `attackAttemptsMade` field to `BanditTroop`, increment on each Attacking-tick, terminal-Escape branch when ≥ 6 |
| 4 | **Tier-based attack power** (§6.14, v4.3 G) — `tier` 1..5 with `attackPower` ∈ {30, 45, 60, 80, 95}, derivation via `getBanditAttackPower(tier)` | Random uniform `[100, 250]` strength; no tier field | Small — add `tier` field, derivation helper, replace strength roll |
| 5 | **Lowest-clanId tiebreak** (§6.8) — when multiple bases tied for highest loot value, lowest clanId targeted (deterministic) | RNG-bounded uniform pick (non-deterministic across replay) | Tiny — replace RNG pick with min-clanId scan |
| 6 | **WAITING-at-home defense contribution** (§6.13) — clansmen passively at home contribute 5 defense each (vs 10 for explicit `DefendBase`) | Only `DefendBase` mission counts; passive home clansmen contribute 0 | Small — extend `_collectDefenderContribution` to include WAITING-at-home, add 5-or-10 dispatch on mission state |
| 7 | **Terminal Escaped state** (§6.12) — Escaped is a removal event; troop deleted from world; carry burned | Escaped is a 2-tick transient that cycles back into Resting | Small — when terminal-escape branch fires (e.g. from #3), remove troop instead of cycling |
| 8 | **Single-troop global limit** (§6.1) — at most ONE active bandit globally | Up to 8 globally; up to 3 per region | Small — set `MAX_TOTAL_BANDITS = 1`; remove per-region cap (or set both to 1); collapse multi-troop spawn loop |

**Additional smaller drifts** noted in the UAT report but bundled with the above on restoration (no separate tracking):

- Threshold-comparison combat (Cases A/B/C in §6.15) instead of damage-HP combat — entangled with #1, #6, and the wall/base HP retuning
- Per-clansman defeat-reward granularity (A11) instead of per-clan — entangled with #1 (no carry to distribute means impl difference doesn't yet matter)
- Global cooldown semantics (§6.3) instead of per-region — entangled with #8 (with single-troop, the distinction collapses)
- Spawn rate curve (5% / +1% / 20% cap) instead of (10% / +10% / 80% cap) — small constant retune, bundled with #8 because rate tuning is most relevant at single-troop saturation
- UnicornTown / DeepSea spawn ban (§6.2) — small allowlist check; bundled with #8 because the active-troop-1 model makes spawn region pickier
- v4.3 E domain-key separation (`bandit_spawn` vs `bandit_spawn_region`) — small, can ship independently
- v4.1 A4 eager-settle ALL bases in region — small; can ship independently or with #2 (rampage needs eager-settle anyway)
- 1e18 Gold reward in defeat (§6.17) — tiny, line-level fix; can ship anytime
- Schema cleanup: drop unused `carryGold`, populate `tier` field (v4.3 G), drop unused constants — bundled with #1, #4

---

## 7. UAT runners — what to expect

When Liam (or anyone running interactive UAT against this contract) observes bandit behavior, **expect the as-built mechanics, not the v4 spec mechanics.** Specifically:

- ✅ Bandits spawn frequently (10% per region per tick, ramping to 80%) and can stack up to 8 globally / 3 per region
- ✅ Bandits stay in their spawn region for their entire lifecycle
- ✅ Bandits attack the same region repeatedly until their target clan dies, defenders defeat them, or you stop watching
- ✅ Bandits do damage to wall, base, and clansmen (clansmen DO die from bandit attacks in this impl)
- ✅ Bandits do NOT steal vault resources; vault wood/wheat/fish/iron are unchanged after bandit attacks
- ✅ Defeated bandits drop only the blueprint (no carry, no gold) to the target clan vault
- ✅ Defenders must be on `DefendBase` mission AT HOME — passive WAITING clansmen contribute 0 defense
- ✅ Tied loot values across multiple region bases break to a random base, not lowest-clanId
- ✅ Bandits can spawn in UnicornTown or DeepSea if a clansman is passing through (no spec ban enforced)

If a UAT scenario expects v4-spec behavior (vault theft, rampage transitions, attack-cap removal, etc.), it will fail. **Use this addendum as the oracle**, not v4 §6.

---

## 8. References

- **Spec-compliance UAT report:** [`docs/reviews/pr194-spec-compliance-uat.md`](../reviews/pr194-spec-compliance-uat.md) — full mechanic-by-mechanic table, test-coverage gap analysis, scenario predictions
- **Source v4 docs being superseded for bandit mechanics:**
  - `clanworld_v4_spec.md` §6.1–6.18, §4.14 (bandit drop overflow)
  - `clanworld_v4_1_addendum.md` A3, A4, A9, A11
  - `clanworld_v4_2_state_schema_interface_spec.md` §5 (bandit constants), §7.7 (BanditTroop layout), §7.10–7.11 (defender registries), §8.2 (DefendBase semantics)
  - `clanworld_v4_3_schema_patch.md` E (domain RNG), F (defender cleanup), G (tier canonical), H (loot getter split — v4.3 H still applies; impl matches)
- **Implementation HEAD canonized by this addendum:** `218f9020ecb0f4b8277ef59dd55de8e004404d80`
- **Restoration tracking:** GH milestone `spec-v4-restoration-post-hackathon` (#25) and tracking issue [#340](https://github.com/OmniPass-world/clan-world/issues/340) — bandit mechanics gap list with the 8 deferred items as a checkbox list
