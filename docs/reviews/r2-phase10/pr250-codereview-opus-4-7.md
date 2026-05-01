# Phase Super-Swarm Review — PR #250 (head e4a0d4c)

## SUMMARY

PR #250 is a substantial Phase-10 cohesion ship: winter schedule, 2× food upkeep + per-clansman wood burn, cold damage → wall degradation → RNG clansman death, wheat plot WinterLocked transitions, and a unified `_markClanDead` helper. The structural shape is good — death routes through `_markClansmanDead`/`_markClanDead`, dead clans short-circuit at `submitClanOrders` and `_settleCompletingMissions`, defenders are cleared, vault resources burn, gold survives. Tests are dense and exercise most happy paths.

But three real issues land: (1) **cold-damage RNG uses `_world.currentTickSeed` instead of the historical `_tickSeeds[tick]`**, which the rest of the codebase consistently uses for lazy-settle RNG — this breaks lazy-settle determinism and is timing-exploitable; (2) **starvation kill condition has an inverted-fairness guard** that grants pre-winter-starvers immunity from winter starvation kills; (3) **wood burn semantics deviate from spec** (per-clansman 0.5e18 vs spec's per-base 1e18). Plus a handful of MEDIUM/LOW smaller issues. Recommend NOT merging until at least the first two are addressed (or explicitly resolved as design changes with an ADR/spec patch).

---

## HIGH severity findings

### H1. Cold-damage RNG breaks lazy-settle determinism
`packages/contracts/src/ClanWorld.sol:498-503`

```solidity
uint256 pick = RNG.rngBounded(
    _world.currentTickSeed,                                     // ← wrong seed
    RNG.DOMAIN_COLD_DAMAGE,
    uint256(keccak256(abi.encodePacked(clan.clanId, tick, coldDamage))),
    livingCount
);
```

The rest of the contract uses **historical** seeds for lazy-settle RNG (line 321-323: `tickSeed = _tickSeeds[tick]; ... _resolveAction(...tickSeed)` — passes through to all gather/fish/iron-bonus rolls). `_world.currentTickSeed` is mutated each heartbeat (line 1057-1059), so two callers settling the same dead-clan at different world ticks will get **different** RNG outcomes for the *same historical cold-damage event*. The salt (`clanId, tick, coldDamage`) varies per event but the base seed does not.

**Why it matters.** This is exactly the lazy-settle invariant the rest of the engine carefully maintains. Concretely: an Elder who sees their wall collapsing can race to `settleClan` at a beneficial heartbeat to bias which of their own clansmen die (e.g., spare their Elder/best worker by resampling). It also makes off-chain replay of a dead clan's history non-deterministic without snapshotting the current seed at observation time.

**Fix.** Replace `_world.currentTickSeed` with `_tickSeeds[tick]`. Trivial diff. Add a regression test that calls `settleClan` for the same death scenario at two different "now" ticks and asserts the same clansman dies.

### H2. Starvation kill guard grants pre-winter starvers immunity
`packages/contracts/src/ClanWorld.sol:440-443`

```solidity
(, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
    _killNextClansmanFromStarvation(clan, tick);
}
```

Trace: clan A starves continuously since tick 105 → `starvationStartsAtTick = 105`. Winter window is `[110, 120)`. For ticks 110-119, condition evaluates `winter=true && starving=true && (105 >= 110)` = **false**. **No clansmen die from starvation.** Meanwhile clan B that just barely tipped into starvation at tick 112 has `starvationStartsAtTick = 112 >= 110` and loses a clansman every tick.

**Inverted fairness.** A clan that has been starving longer should die *faster*, not be immune. Worse, this is gameable: an Elder who anticipates running out of food in winter can deliberately start starving pre-winter to claim immunity. Spec §4.12/v4.1 A10 is silent on the exact in-winter starvation cadence (only "outside winter, starvation does not directly kill"), so the guard isn't required by spec.

**Fix.** Drop the `starvationStartsAtTick >= winterStartsAtTick` clause and just gate on `winter && starving`. (Or, if there's a design reason for a "settle-in" delay, use `tick >= winterStartsAtTick + N` rather than tying it to the starvation start tick.) Add a regression test that pre-winter-starves a clan and asserts it loses clansmen during winter at the same cadence as a fresh-winter-starver.

---

## MEDIUM severity findings

### M1. Wood burn semantics deviate from spec
`packages/contracts/src/IClanWorld.sol:64`, `ClanWorld.sol:447`

Spec (§7.3, v4.3 K.1): `WINTER_WOOD_BURN_PER_BASE = 1e18` — fixed per base, regardless of clansman count. PR replaces with `WINTER_WOOD_BURN_PER_CLANSMAN = 5e17` (0.5 wood) and computes `woodNeeded = livingClansmen * 0.5e18`. At full strength (4 clansmen) that's **2e18 per tick = 2× spec**; as clansmen die, burn drops. This may be an intentional balance change (cold deaths cascade less when only one clansman remains), but the spec is the canonical reference and was not updated. Either:

- amend `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md` (or a new addendum) to document the per-clansman semantics, OR
- restore `WINTER_WOOD_BURN_PER_BASE = 1e18` and use `clan.livingClansmen > 0 ? 1e18 : 0`.

Flag this for product/spec sign-off. Not a code bug per se but a silent design departure.

### M2. Wheat plot regrow lost at winter-start boundary
`packages/contracts/src/ClanWorld.sol:1132-1142` (lock) vs `377-384` (lazy regrow)

`_lockWheatPlotsForWinter` *unconditionally* sets every plot's `state = WinterLocked` regardless of pre-existing state. If a plot was `Regrowing` with `regrowUntilTick <= 109` (pre-winter), and the clan didn't trigger `_settleClan` in the [regrow-end, winter-start] window, the lazy regrow check (`if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick)`) at line 379 will never see `Regrowing` — it's already been overwritten to `WinterLocked` by the heartbeat. The "should-have-been-Harvestable" tick is silently dropped.

Practical impact is bounded: post-winter `_restartWheatPlotsAfterWinter` resets all plots to a fresh `Regrowing(winterEnd + 4)`, so the only damage is lost harvest *opportunity* in the pre-winter regrow window for clans that didn't settle. With orchestrator polling every tick the window is small. Still, fairness asymmetry between settled/unsettled clans is real.

**Fix options.** (a) In `_lockWheatPlotsForWinter`, perform the lazy regrow check first: `if (plot.state == Regrowing && plot.regrowUntilTick <= currentTick) { plot.state = Harvestable; ... }` then lock. (b) Skip lock for plots already past their regrow tick and let the lazy-settle eat them. Option (a) is straightforward and adds 1 read + 1 conditional per plot.

### M3. Orphan `ClansmanDiedFromCold` event in interface
`packages/contracts/src/IClanWorld.sol:511` (new) and `:623` (old)

```solidity
event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint32 tick);     // emitted
event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);             // never emitted
```

Both events are exported in the ABI (`abi/IClanWorld.json:2974, 2999`). The old name is dead — no `emit ClansmanDiedFromCold(...)` anywhere. This is interface bloat, but worse, any indexer/frontend code that subscribed to the old event will silently receive zero notifications going forward.

**Fix.** Remove `ClansmanDiedFromCold` from `IClanWorldEvents`. Audit `apps/server/convex/*` and `apps/web/*` for any reference. (Indexer in this repo, so blast radius is small — but worth the grep before merge.)

### M4. `WinterStarted` / `WinterEnded` ABI break (uint64 → uint32)
`packages/contracts/src/IClanWorld.sol:497-498`, `abi/IClanWorld.json:3754, 3767`

```solidity
event WinterStarted(uint32 indexed tick);  // was uint64
event WinterEnded(uint32 indexed tick);    // was uint64
```

This is a real selector change (different event signatures). Any pre-existing indexer subscription bound to the uint64 version stops firing. The contract is pre-prod so blast radius is local — confirm `apps/server/convex` and `RealChainClient` ABIs are regenerated as part of merge. Worth a one-line note in the PR body.

The motivation appears to be aligning with `_eventTick` (uint32 cast guarded by `require(tick <= type(uint32).max)`). With ~4B ticks the cast is safe in practice but the saved bytes don't justify the ABI churn — flag for design review whether the entire winter event family should standardize on uint64 (as `TickAdvanced` and `SeasonFinalized` do).

### M5. RNG domain naming inconsistency
`packages/contracts/src/lib/RNG.sol:12`

```solidity
uint256 internal constant DOMAIN_COLD_DAMAGE = uint256(keccak256("cold_damage"));
```

Existing domains use a versioned namespace: `"clanworld.market.noise.v1"`, `"clanworld.weather.v1"`, `"clanworld.fair.iteration.v1"`. The new one is bare `"cold_damage"`. Functionally fine (collision-resistant), but it makes future v2 rotations awkward and breaks the implicit convention. **Fix:** rename to `"clanworld.cold_damage.v1"`. Trivial, and worth doing before mainnet deploys lock in the seed.

### M6. `submitClanOrders` has both `require` revert and soft-error path for dead clan
`packages/contracts/src/ClanWorld.sol:1323` (revert) vs `:1347` (soft return)

```solidity
require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");   // line 1323
...
_settleClan(clanId);
results = new OrderResult[](orders.length);
if (clan.clanState == ClanState.DEAD) {                                // line 1347
    for (...) results[i] = ... ERR_CLAN_DEAD ...;
    return results;
}
```

The require makes the soft-error path only reachable when a clan transitions DEAD *during* `_settleClan`. That's correct (lazy starvation/cold death mid-settle should not revert the caller's batch). But Test B (`test_deadClanCannotSubmitClanOrders`) only covers the revert side; there is **no test** that the soft-error path actually fires for a clan that dies during `_settleClan`. Add one.

This is also an inconsistency in user contract — for callers, "is my clan dead?" returns either a revert or a per-order ERR_CLAN_DEAD depending on settlement timing. The orchestrator/Elder integration code needs to handle both. Worth a short comment near line 1347 stating the invariant.

---

## LOW severity findings

### L1. `_markClanDead` doesn't clear clansman carry inventory
`ClanWorld.sol:565-591`. Vaults are wiped (`vaultWood/Wheat/Fish/Iron = 0`), but per-clansman `carryWood/Wheat/Fish/Iron` on the now-dead clansmen is preserved. Spec is silent on this. Inconsistent with vault wipe; harmless because dead clansmen can't deposit, but a future `getClanFullView` consumer might surface phantom carry. Either wipe or document the invariant.

### L2. `lastSettledTick = curTick` set unconditionally after death-break
`ClanWorld.sol:371-397`. When the loop breaks early because the clan died at tick T < curTick, `lastSettledTick` is still set to `curTick`. Functionally fine (dead clans no-op on subsequent settles), but semantically the clan was last *meaningfully* settled at the death tick. If a future analytics query reads `lastSettledTick` on dead clans it will be misleading. Cosmetic.

### L3. Redundant cold-damage reset at `_settleClan` lines 393-395
The block

```solidity
if (curTick > fromTick && !_isWinterActiveAt(curTick) && _isWinterActiveAt(curTick - 1)) {
    clan.coldDamage = 0;
}
```

duplicates the per-tick reset already done inside `_applyUpkeep` (line 404-406). Idempotent because both write 0, but smells of belt-and-suspenders that one of them will rot. Pick one location and delete the other; comment why it's there.

### L4. `_resetColdDamageForAllClans` does not skip dead clans
`ClanWorld.sol:1161-1165`. Iterates all clans, including DEAD ones. Resetting `coldDamage = 0` on a dead clan is harmless (the field is unread for dead clans). Wasted gas at winter-end heartbeat. Add `if (clan.clanState == ClanState.DEAD) continue;`. Same for `_lockWheatPlotsForWinter` and `_restartWheatPlotsAfterWinter` (lines 1132-1158) — locking/unlocking a dead clan's plots writes to slots that will never be read again. Trivial gas cleanup.

### L5. `MAX_CROP_TRANSITION_PER_TICK = 48` is a `require`-cliff
`ClanWorld.sol:80, 1137, 1149`. Hardcoded for "24 clans × 2 plots" but the project ships with 8 clans (per `docs/planning/V1/07 ... v4.5 ... §3.1`). If anyone ever bumps `MAX_CLANS` past 24 without bumping this constant, the heartbeat that crosses winter will revert and the world freezes. Either make the cap a function of `MAX_CLANS`, or downgrade the `require` to a "cap = process up to N this tick, defer the rest" pattern (mirroring `MAX_MARKET_ACTIONS_PER_TICK`). Since the plot transition needs to be atomic at the boundary, the deferred approach doesn't fit cleanly — at minimum, add an invariant test `assertLe(MAX_CLANS * 2, MAX_CROP_TRANSITION_PER_TICK)`.

### L6. Starvation clansman pick is deterministic by storage order
`ClanWorld.sol:519-533`. `_killNextClansmanFromStarvation` walks `_clanClansmanIds` and picks the first non-DEAD. This is deterministic but predictable — Elder code can detect that "clansman with the lowest ID dies first" and pre-position lower-ID clansmen as expendables. Spec is silent on selection. If non-determinism is desired, route through RNG with `DOMAIN_STARVATION_PICK` (consistent with cold). If determinism is desired, document it on the function. Either way, it's a design knob worth confirming.

### L7. Test gap — boundary tick coverage
The test suite tests winter onset, end, and the next cycle, plus crop locks/restart, plus all death paths. **Missing** regression tests for:

- The lazy-settle RNG determinism (H1) — settle the same scenario at different "now" ticks, assert same clansman dies.
- Pre-winter starvation kill behavior (H2) — clan starving since tick 100, winter at 110, assert clansmen die in winter.
- Plot regrow loss at boundary (M2) — harvest at tick 105, do not settle, advance through winter, assert (or document) the lost mid-summer regrow.
- Soft `ERR_CLAN_DEAD` path in `submitClanOrders` (M6) — clan that dies during the lazy settle inside the call.
- Wood burn rate as multiple of full-strength clansman count (M1) — assert the actual per-tick burn matches whichever spec is authoritative.

Hackathon rule says "minimal tests", but each of these is a single happy-path test that protects a documented invariant.

### L8. Unused `_markClanDead(uint32)` overload
`ClanWorld.sol:561-563`. The single-arg version is currently unreferenced. It exists for future Phase 9.4 attack-final-kill integration. Defensible, but flag with a `// TODO Phase 9.4: wire from attack handler` so it isn't silently deleted in cleanup.

---

## Cross-cutting observations

1. **The `_markClanDead` helper is good infrastructure.** Both starvation and cold death route through it; defenders are cleared; vault wipe is consistent; both `ClanEliminated` (legacy) and `ClanDied(reason)` are emitted (good for off-chain consumer migration). Phase 9.4 attack-final-kill should plug into this pathway via `_markClansmanDead` followed by the existing `if (livingClansmen == 0) _markClanDead(...)` check — keep that pattern uniform.

2. **Lazy-settle RNG convention is mostly correct.** The cold-damage path is the lone outlier (H1). After fixing H1, audit the file for any future `_world.currentTickSeed` reads from inside settle paths — there should be none.

3. **`_world.winterActive` storage field is now dead.** It's set to `false` at construction (line 97, `ClanWorldStub.sol:54`) and *never written again*. The view derives `winterActive` from `_winterWindowForTick`. This is fine but confusing — consider removing the field from `WorldState` storage (keep it only in the view struct) so future readers don't get tripped up. (ABI-stable: `WorldState` is exported via `getWorldState()` so removal is an ABI break — defer to a coordinated state-layout rework.)

4. **Heartbeat ordering is correct (Step 1 settle → Step 4 winter transition → Step 5 tick increment).** Mission settlements at the boundary close-tick run against pre-lock plot state, which is what you'd want. The boundary fragility is in lazy-settle plot state (M2), not heartbeat ordering.

5. **OTC integration is N/A for this PR** — `transferGold/transferVaultResource/transferBlueprint/transferBundle` revert with "OTC transfers not implemented" (lines 1986-2003). When Phase 7.5 lands, the dead-clan check (per spec v4.3 §M, "dead clans may not initiate outbound value transfer") needs to gate the `from` clan, mirroring `submitClanOrders:1323`. Tracking issue to remember.

6. **Spec compliance is solid except for M1 (wood burn).** §A2 winter timing is correctly implemented as a global recurring schedule (PR v4.5 addendum supersedes the per-season-reset older spec). §10 elimination — vault burn yes, gold preservation yes, mission/defender cleanup yes. §10.2/§10.3 cold damage thresholds (every 2) — correct via integer-division-step pattern. §10.4 wheat plot WinterLocked + restart — correct, modulo M2 boundary.

7. **PR scope cleanliness.** The diff also contains a lot of unrelated work (frontend sprite assets, runner package, agent CLI, docs). For Phase 10 contract review the relevant files are `ClanWorld.sol`, `ClanWorldStub.sol`, `IClanWorld.sol`, `lib/RNG.sol`, `test/ClanWorld.t.sol`, `test/ClanWorldStub.t.sol`, `abi/IClanWorld.json` — the rest is noise. Future cohesive-phase PRs should land in tighter cuts to make review tractable.

---

**Recommendation.** Fix H1 + H2 before merge (both are 5-line diffs and ship with one regression test each). Land M1 either as a code revert or as an explicit spec amendment in this PR. Schedule M2-M6 + L1-L8 for a follow-up cleanup PR rather than blocking the phase ship.
