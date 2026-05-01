# PR #199 — Phase 8: Buildings + Progression — Review

- **PR**: [#199](https://github.com/OmniPass-world/clan-world/pull/199)
- **Review date**: 2026-04-30
- **Model**: Claude Opus 4.6 (10-agent swarm: 7 Wave 1 parallel + 3 Wave 2 targeted)
- **Method**: Blind independent review (no prior review docs read)
- **Base**: `dev` → Head: `dev-phase-8-buildings`

## Summary Stats

| Metric | Count |
|--------|-------|
| Total findings | 23 |
| **MUST FIX** | 2 |
| **SHOULD FIX** | 6 |
| **DEFER** (new GH issue) | 8 |
| **SKIP / FALSE POSITIVE** | 7 |

## Scope

- 15 files changed, +7186/−281 (4377 lines are ABI JSON)
- ~1252 new lines in `ClanWorld.sol`
- 6 new test files, 2 modified test files
- 2 TypeScript adapter changes
- 11 commits (includes multiple fix rounds from prior reviews)

---

## Triage Table

| # | File | Line(s) | Finding | Category |
|---|------|---------|---------|----------|
| 1 | `packages/contracts/src/ClanWorld.sol` | 1002–1019 vs 463–493 | **Simulation upkeep divergence**: `_simulateApplyUpkeep` uses raw `vaultWheat >= wheatNeeded` and zeros vault on shortage; `_applyUpkeep` uses `_spendableAfterReleasing` (reservation-aware) and subtracts only spendable wheat. Affects `getClanScore`, `getRankings`, `quoteLootValueSettled` — previews can disagree with chain state when wheat reservations exist. | **MUST FIX** |
| 2 | `packages/contracts/src/ClanWorld.sol` | (whole contract) | **EIP-170 contract size exceeded**: `ClanWorld` runtime bytecode is ~46KB vs 24KB limit. Cannot be deployed on standard EVM chains as a single contract. Requires library extraction, proxy/facet pattern, or significant code splitting. | **MUST FIX** |
| 3 | `packages/contracts/src/ClanWorld.sol` | 434–456, 817–834, 854–890, 893–939 | **Concurrent upgrade fromLevel ordering**: When two clansmen complete same building type on same tick, settlement runs in `_clanClansmanIds` array order. If the higher-fromLevel worker runs first, it sees a mismatch → refund → `return false`. On next tick, reservation is gone → `return true` without upgrading → mission auto-completes. Valid upgrade silently lost. | **SHOULD FIX** |
| 4 | `packages/contracts/src/IClanWorld.sol` | Event declarations | **ResourcesDeposited breaking change**: `uint64 atTick` → `uint32 tick` changes the event topic hash. Field renames (`wood` → `woodDelta`, etc.) also affect named decoding. Any downstream consumer filtering by old topic will miss new events. | **SHOULD FIX** |
| 5 | `packages/contracts/src/IClanWorld.sol` | 189–208 | **WorldState struct insertion**: `currentSeasonNumber` and `nextHeartbeatAtTick` inserted mid-struct (after `seasonFinalized`, before `nextHeartbeatAtTs`). Breaks positional decoding for any existing consumer not using updated ABI. | **SHOULD FIX** |
| 6 | `packages/shared/src/adapters/IChainClient.ts` | 24–246 | **IChainClient.ts missing Phase 8 reads**: Embedded ABI has no entries for `getWallUpgradeCost`, `getBaseUpgradeCost`, `getMonumentUpgradeCost`, `getClanScore`, `getRankings`. Agents/runner cannot call these through the adapter. | **SHOULD FIX** |
| 7 | `packages/contracts/test/` (all upgrade suites) | N/A | **Missing wrong-region revert test**: No test asserts `ERR_NOT_AT_HOMEBASE` when upgrade `gotoRegion` differs from `clan.baseRegion`. All three upgrade types share this gap. | **SHOULD FIX** |
| 8 | `packages/contracts/test/` (upgrade suites) | N/A | **Missing settle-time insolvency test**: No test drains vault between order submission and settlement to verify retry behavior when vault is insufficient at settle time. | **SHOULD FIX** |
| 9 | `packages/contracts/src/ClanWorld.sol` | 1467–1488 | **Dead code: `_settleCompletingMissions`**: Defined but never called. `heartbeat` uses `_settleClanToTick` instead. Stale docstring still says "called from heartbeat." | **DEFER** |
| 10 | `packages/contracts/src/ClanWorld.sol` | 2516–2558 | **Dead code: `_hasEarlier*UpgradeReservation`**: Three helpers defined but never called anywhere. Either ordering enforcement was planned but not wired, or these are leftover stubs. | **DEFER** |
| 11 | `packages/contracts/src/ClanWorld.sol` | 2997–3008 | **Dead code: `_getClanScoreFromClan`**: Defined but unused. | **DEFER** |
| 12 | `packages/contracts/src/ClanWorld.sol` | 847–850, 886–889, 935–938 | **Dual event emission per upgrade**: Both `*LevelChanged` and `*Upgraded` emitted for each upgrade. Different schemas (`uint64` vs `uint32` tick, different fields). Indexers that listen to both without deduplication could double-count. | **DEFER** |
| 13 | `packages/contracts/src/ClanWorld.sol` | 1428–1445 | **Heartbeat gas increase**: `heartbeat` now settles **every** clan via `_settleClanToTick` (up to 200 inner ticks × all clansmen). Significant gas increase over old "completing missions only" approach. Scales O(clans × lag × clansmen). | **DEFER** |
| 14 | `packages/contracts/src/ClanWorld.sol` | 2925–2965 | **getRankings simulation cost**: Full `_simulateSettleToTick` per clan (up to 24 clans × 200 ticks). Heavy `eth_call` / RPC cost. May need off-chain indexing for production. | **DEFER** |
| 15 | `packages/contracts/test/UpgradeReservationSwitches.t.sol` | 51–61 | **Weak reservation switch assertions**: Tests only assert both submits return `OK` but don't verify reservation state (pending counts, reserved resource totals). Regressions that stack reservations could slip through. | **DEFER** |
| 16 | `packages/contracts/src/ClanWorld.sol` | 144, 2925–2929 | **Rankings scan cap at 24**: `MAX_CLAN_SCAN_FOR_RANKING = 24` with current `mintClan` cap at 12. Safe today, but future footgun if cap grows past 24. | **DEFER** |
| 17 | `packages/contracts/src/ClanWorldStub.sol` | 324–329 | **Stub `pure` vs `view`**: `getClanScore` and `getRankings` declared `pure` in stub but `view` on interface. Solidity allows this but diverges from canonical mutability. | **SKIP** |
| 18 | `packages/contracts/src/ClanWorld.sol` | 438–456 | **Loop counter unchecked**: `i++` in checked arithmetic where bounds are safe. Minor gas savings with `unchecked { ++i; }`. | **SKIP** |
| 19 | `packages/contracts/src/ClanWorld.sol` | Reservation structs | **`toLevel` derivable**: Always `fromLevel + 1`. Wastes one byte of packed storage. | **SKIP** |
| 20 | `packages/contracts/src/ClanWorld.sol` | ~2745–2748 | **Monument levels 6–9 share cost tier**: Spec question, not a bug — confirm against game design. | **SKIP** |
| 21 | `packages/contracts/src/ClanWorld.sol` | 1265–1290 | **`_simClansmanIndex` linear scan**: O(clansmen²) in view simulation. Acceptable for 4 clansmen per clan. | **SKIP** |
| 22 | `packages/contracts/src/ClanWorld.sol` | 949–954 | **`_completeMission` redundant SLOAD**: Reads `_clans[cs.clanId].clanId` when `cs.clanId` is already available. ~100 gas savings. | **SKIP** |
| 23 | `packages/contracts/src/ClanWorld.sol` | sim settle funcs | **Sim max-level branch skip**: Sim doesn't call `_simClearUpgradeReservation` on max-level early return. Asymmetric with real path but functionally harmless since mission completes. | **SKIP** |

---

## Detailed Analysis: MUST FIX Items

### #1 — Simulation Upkeep Divergence (CRITICAL)

**Confirmed by**: 5/10 agents independently (Correctness, Security, Gas, Sim-Parity, Cross-Validator)

**Root cause**: `_applyUpkeep` (line 463) computes spendable wheat via `_spendableAfterReleasing(clan.vaultWheat, _reservedWheatByClan[clan.clanId], 0)`, which excludes reserved wheat from the sufficiency check and only drains spendable wheat on shortage. `_simulateApplyUpkeep` (line 1002) uses raw `sim.clan.vaultWheat >= wheatNeeded` and sets `vaultWheat = 0` on shortage.

**Impact**: All view-layer score/loot/ranking functions route through `_simulateSettleToTick` → `_simulateApplyUpkeep`:
- `getClanScore()` → wrong score when wheat reservations exist
- `getRankings()` → wrong ordering
- `quoteLootValueSettled()` → wrong loot preview

**Fix direction**: Thread `_reservedWheatByClan[clanId]` (read-only in view context) into `SettlementSimulation` and mirror the `_spendableAfterReleasing` logic in `_simulateApplyUpkeep`.

### #2 — EIP-170 Contract Size (BLOCKING)

**Confirmed by**: Architecture agent ran `forge build --sizes`; runtime bytecode ~46KB vs 24KB limit.

**Impact**: ClanWorld cannot be deployed as a single contract on any standard EVM chain (World Chain Sepolia included).

**Fix direction**: Extract pure/view math into Solidity `library` functions (linked, not embedded), or adopt a proxy/facet pattern (Diamond, UUPS). View-heavy functions like `getRankings`, `_simulateSettleToTick`, and cost getters are prime candidates for library extraction.

---

## Detailed Analysis: SHOULD FIX Items

### #3 — Concurrent Upgrade fromLevel Ordering

**Trace**: Clan queues wall 0→1 on worker B (index 3), wall 1→2 on worker A (index 1). Same tick settlement: A runs first (`fromLevel=1` vs `wallLevel=0` → mismatch → refund → `return false`). Next tick: reservation gone → `return true` without upgrading → mission completes. Worker A loses the upgrade.

**Fix direction**: Either (a) sort upgrade settlements by `fromLevel` ascending within each tick, (b) on fromLevel mismatch where `fromLevel > wallLevel`, return `false` WITHOUT refunding (retry until lower level settles), or (c) wire `_hasEarlier*UpgradeReservation` into validation to prevent out-of-order queueing.

### #4 — ResourcesDeposited Breaking Change

The `uint64 atTick` → `uint32 tick` type change alters the event topic hash. Any log filter using the old selector will silently miss events. The ABI JSON is updated but downstream consumers (Convex indexer) must also update their event subscriptions.

### #5 — WorldState Struct Insertion

New fields inserted between `seasonFinalized` and `nextHeartbeatAtTs` shift all subsequent field positions. The runner's `HEARTBEAT_ABI` in `runnerCastHeartbeat.ts` is updated, but any other consumer using positional decoding needs the new ABI.

### #6 — IChainClient Missing Phase 8 Reads

The TypeScript adapter only has ABI fragments for `getWorldSnapshot`, `getClanFullView`, and `submitClanOrders`. Phase 8 getters are missing. When agents or the frontend need to call `getClanScore`, `getRankings`, or cost getters, they'll need direct viem calls with the raw ABI.

### #7–8 — Test Gaps

Two specific test gaps identified:
1. No test for `ERR_NOT_AT_HOMEBASE` on upgrade orders targeting wrong region
2. No test for settle-time vault insolvency (resources drained between submit and settle)

---

## Overall PR Health Assessment

**Status: NEEDS WORK**

The core upgrade mechanics (wall/base/monument) are well-structured and follow established patterns from earlier phases. Reservation lifecycle, refund paths, and cross-type switching are solid. Test coverage is good for a hackathon project.

However, two blocking issues prevent merge:
1. The simulation upkeep divergence means all view-layer rankings/scores can be wrong — this undermines a core Phase 8 deliverable (score/rank getters)
2. The EIP-170 size limit means the contract literally cannot be deployed

The concurrent upgrade ordering bug (#3) is a gameplay-impacting issue that should also be fixed before merge, though it only manifests when two same-type upgrades complete on the same tick.

---

## Recommended Next Steps

### Immediate (MUST FIX — blocks merge)
1. **Fix simulation upkeep**: Mirror `_spendableAfterReleasing` logic in `_simulateApplyUpkeep`
2. **Reduce contract size**: Extract view/pure functions into libraries to get under 24KB

### Before merge (SHOULD FIX)
3. **Fix concurrent fromLevel ordering**: Choose one of the three fix directions above
4. **Verify event consumers**: Ensure Convex indexer handles `ResourcesDeposited` topic change and `WorldState` struct shift
5. **Add Phase 8 reads to IChainClient.ts**: At minimum, cost getters and score/ranking functions
6. **Add 2 targeted tests**: Wrong-region revert + settle-time insolvency

### Post-merge (DEFER — new GH issues)
7. Dead code cleanup (`_settleCompletingMissions`, `_hasEarlier*`, `_getClanScoreFromClan`)
8. Dual event rationalization (pick one event family per upgrade type)
9. Heartbeat gas profiling and optimization
10. Rankings function gas/RPC cost assessment for production
11. Reservation switch test strengthening
