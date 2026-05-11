# Phase Super-Swarm Synthesis (R3 post-fix-round-merge) — PR #250 (head 400463e)

**Models run:** Codex 5.3 ✓ | Codex 5.5 ✓ | Codex 5.4 ✓ | Opus 4.6 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✓ — full 6-model lineup
**Phase:** dev-phase-10-winter — Winter + Elimination
**Diff size:** 1797 lines (post fix-round PR #289 merge)

## Summary

**Verdict: NEEDS_FIXES — strong cross-model consensus on 3 HIGHs.**

R3 fix-round addressed all 8 R2 findings cleanly. New R3 super-swarm flagged 3 architectural HIGHs (each 2-3 model consensus): `getClanFullView` ABI Mission tuple stale (3 codex), cold-damage RNG settlement-time dependent (3-tier consensus), heartbeat mission settlement bypasses upkeep (cross-phase pattern). Plus 1 single-model HIGH on stale winter storage.

## MUST FIX — multi-model consensus

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H1 | `IChainClient.ts:132, :136, :161, :380` + `IClanWorld.sol:274, :285` | Codex 5.3 + 5.4 + 5.5 = **3/6 (all 3 codex)** | HIGH | **`getClanFullView()` Mission tuple ABI stale.** Handwritten viem ABI for `getClanFullView()` omits `submittedAtTick`, `executesAtTick`, `settlesAtTick` fields in nested `activeMission` tuples. Canonical contract ABI includes them between `nonce` and `clansmanId`. Direct-chain `getClanFullView()` calls decode garbage or fail outright once mission data is present. **R3 fix-round handled `getWorldState` + `getWorldSnapshot` ABI staleness but missed this third site.** Fix: regenerate ABI fragment from canonical, OR add the 3 `uint64` fields in both mission tuple definitions. Add CI parity test against `packages/contracts/abi/IClanWorld.json`. |
| H2 | `ClanWorld.sol:505-506` (`_killRandomClansmanFromCold`) | Codex 5.3 = HIGH; Opus 4.7 = MED; Gemini = HIGH. **3/6 cross-tier.** | HIGH | **Cold-death RNG uses `_world.currentTickSeed`, not seed of the historical winter tick being settled.** Lazy settle at different chain-block heights picks DIFFERENT clansmen as victim. Salt mixes (clanId, tick, coldDamage) so within one block result is deterministic, but the choice of victim shifts with settle timing. **Gemini concretely identified MEV/delay manipulation: a player can simulate off-chain every tick + only broadcast settleClan when the rotating seed picks their least-valuable worker.** Fix: use `_tickSeeds[tick]` for cold-death RNG (deterministic per historical tick) OR drop `_world.currentTickSeed` from the salt entirely (clanId+tick+coldDamage are sufficient). |
| H3 | `ClanWorld.sol:1070, :1092` (`_settleCompletingMissions`) vs `:386-408` (`_settleClan`) | Codex 5.5 = 1/6 (cross-phase: P7 + P8 H5) | HIGH | **Heartbeat mission settlement bypasses normal upkeep/death pipeline.** Heartbeat directly calls `_settleMissionForClansman()` for due missions but skips canonical `_settleClan` per-tick ordering: upkeep, winter food/wood burn, cold/starvation deaths, crop regrow, `lastSettledTick` advancement. A clansman that should have died from starvation/cold before the due tick can still complete the mission. Same architectural pattern as P7 + P8 H5 — fix in concert. |

## MUST FIX — single-model HIGH (worth fixing)

| # | File:line | Models | Severity | Finding |
|---|---|---|---|---|
| H4 | `ClanWorld.sol:95-97` (constructor) + `:1196` (`_worldStateView`) | Opus 4.7 = HIGH; Opus 4.6 = MED. 2/6. | HIGH (footgun) | **`_world.winterActive / winterStartsAtTick / winterEndsAtTick` storage permanently stale after R3 made winter purely derived.** Three storage fields written once in constructor, never updated. External readers go through `_worldStateView()` so OK today, but any future code that reads `_world.winterActive` directly breaks silently. **Fix:** drop fields from on-chain `WorldState` struct entirely — let `_worldStateView()` synthesize for ABI return. OR keep fields but update them in heartbeat winter-transition block + add comment. |

## SHOULD FIX (MEDIUM)

| # | Models | Finding |
|---|---|---|
| M1 | Codex 5.5 + Opus 4.7 = 2/6 | `submitClanOrders` adapter discards return data. Contract returns per-order `StatusCode[]` including new `ERR_WINTER_LOCKED` and `ERR_MUST_SETTLE_FIRST`. `writeContract` discards results — Elders treat rejected orders as successful submitted txs. Use `simulateContract` before broadcasting + widen adapter return type. |
| M2 | Codex 5.4 + Codex 5.5 + Opus 4.7 = 3/6 | **Legacy `ColdDamageApplied` + `ClansmanDiedFromCold` events still in interface/ABI but never emitted; new events `ClanColdShortage`, `WallDegradedByCold`, `ClansmanColdDeath`, `ClanDied` replace them.** Indexers subscribed to old events miss every winter death. Remove dead events from interface OR emit compat events for one release. |
| M3 | Opus 4.6 + Opus 4.7 = 2/6 | RNG domain naming inconsistency: `DOMAIN_COLD_DAMAGE` uses bare `"cold_damage"`, all other domains follow `"clanworld.<subsystem>.v1"` convention. Rename to `"clanworld.cold.damage.v1"`. |
| M4 | Gemini = 1/6 | Newly minted clans post-winter skip global crop regrow phase. `mintClan` evaluates `_isWinterActiveAt(currentTick) = false` post-winter end → starting plot `Harvestable`, while existing clans wait 4-tick regrow. Gameplay fairness wrinkle. |
| M5 | Codex 5.3 + Codex 5.5 = 2/6 | Elimination/upkeep is lazy-settlement-bound, not heartbeat-global. Winter deaths can lag until clan settlement; weakens "tick-authoritative elimination" semantics. |
| M6 | Opus 4.7 = 1/6 | Spec gap §7.4: `regrowUntilTick = newTick + 4` interpretation defensible but alternative reading (`winterEnd_last_winter_tick + 4`) defensible too. Clarify in spec. |
| M7 | Codex 5.4 = 1/6 | Coverage gap: settle-first guard validated only with single-order batch. Add multi-order batch tests. |

## DEFER (LOW)

- L1 (3/6): Dead clans permanently consume `_lockWheatPlots` + `_restartWheatPlots` global crop transition budget. Skip dead clans in loop.
- L2: Cold-death event tests only count signature occurrence, not payload correctness.
- L3 (Opus 4.7): `_winterEventTick` is identity function — placeholder, remove or document.
- L4 (Opus 4.6): `_markClanDead(uint32)` 1-arg overload appears unused.
- L5: Test revert expectation misaligned with graceful error return on dead-clan order submit.
- L6 (Opus 4.7 L8): `monumentReachedAtTick == 0` sentinel collides with literal-zero — fragile (cross-phase same as P8 R3 L8).

## Per-model verdicts

- **Codex 5.3:** NEEDS_FIXES — 2 HIGH (cold-RNG + getClanFullView ABI) + 3 MED + 1 LOW.
- **Codex 5.4:** NEEDS_FIXES — 1 HIGH (getClanFullView ABI) + 1 MED.
- **Codex 5.5:** NEEDS_FIXES — 2 HIGH (heartbeat-bypasses-upkeep + getClanFullView ABI) + 3 MED + 1 LOW.
- **Opus 4.6:** CLEAN merge-ready (their words) — 0 HIGH + 3 MED. M2 stale-storage graded MED.
- **Opus 4.7:** NEEDS_FIXES — 1 HIGH (stale winter storage footgun) + 6 MED + 8 LOW. Most thorough on dead code surfaces.
- **Gemini Pro:** NEEDS_FIXES — 1 HIGH (cold-RNG MEV repro) + 2 MED + 2 LOW.

## Decision

**Recommend Path A R4 fix-round.** Scope:

1. **H1 getClanFullView ABI**: add 3 `uint64` Mission fields in `IChainClient.ts` (~ same fix as R3 did for `getWorldState` + `getWorldSnapshot`)
2. **H2 cold-RNG MEV**: switch to `_tickSeeds[tick]` OR drop `_world.currentTickSeed` from salt
3. **H3 heartbeat upkeep bypass**: defer to Phase 8 R4 architectural fix (shared cross-phase)
4. **H4 stale winter storage**: drop fields from struct OR document/comment + maintain in heartbeat
5. **M1 adapter return discard**: widen adapter to return `OrderResult[]`
6. **M2 dead cold events**: remove from interface
7. **M3 RNG domain rename**: `clanworld.cold.damage.v1`

Total fix budget: ~30-45 min codex.

H3 (heartbeat-vs-lazy) is the cross-phase architectural finding shared by Phase 7 + Phase 8 + Phase 10. Best to fix once in Phase 8 R4 then rebase Phase 7 + Phase 10 against dev.
