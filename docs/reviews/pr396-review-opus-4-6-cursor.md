# PR #396 Superswarm Review — Opus 4.6

- **PR**: [#396 "Dev merge"](https://github.com/OmniPass-world/clan-world/pull/396)
- **Base / Head**: `dev` ← `dev-merge` (`784b4bb`)
- **Size**: 68 files, +12,583 / −1,151 lines, 88 commits
- **Phases**: 5 (gathering/deposits), 6 (markets/pools), 7 (OTC transfers), 8 (buildings), 9 (bandits), 10 (winter/cold/death)
- **Review date**: 2026-05-01
- **Model**: Opus 4.6 Cursor (orchestrator) + 11 sub-agents (8 Wave 1 parallel + 3 Wave 2 targeted)
- **Worktree**: `/home/claude/code/omnipass-world/review-pr-396-cursor` (detached HEAD at `784b4bb`)

---

## Review Structure

### Wave 1 — Broad Sweep (8 parallel agents)

| Agent | Scope | Findings |
|-------|-------|----------|
| W1-1 | Solidity Phases 5+6 (Economy) | 9 |
| W1-2 | Solidity Phases 8+10 (Buildings+Winter) | 9 |
| W1-3 | Solidity Phases 7+9 (Transfers+Bandits) | 14 |
| W1-4 | Security + Reentrancy | 12 |
| W1-5 | ABI + Codegen Parity | 10 |
| W1-6 | Test Coverage + Quality | 11 |
| W1-7 | Architecture + Integration | 11 |
| W1-8 | Style, Docs, Conventions, Merge Hygiene | 14 |

### Wave 2 — Targeted Deep Dives (3 sequential agents)

| Agent | Scope | Findings |
|-------|-------|----------|
| W2-9 | Simulation vs Real Parity Sweep (20 function pairs) | 7 divergences |
| W2-10 | Merge Regression Hunt | 8 |
| W2-11 | Reservation System Deep Dive | 7 |

---

## Triage Table

Findings are de-duplicated across agents. Convergence count shows how many independent agents flagged each issue.

### MUST FIX (5 findings — merge-blocking)

| # | Finding | File(s) | Lines | Convergence | Agents |
|---|---------|---------|-------|-------------|--------|
| M-1 | **OTC transfers reservation-blind** — `transferVaultResource`, `transferBlueprint`, `transferBundle` check raw vault balances without subtracting `_reserved*ByClan`. A clan can transfer away resources committed to in-flight upgrades. Root cause: PR #395 (`da95c34`) not merged into `dev-merge`. | `ClanWorld.sol` | 4656–4745 | **4 agents** | W1-3, W1-4, W2-10, W2-11 |
| M-2 | **WithdrawResources reservation-blind** — `_hasVaultResources` / `_doWithdrawResources` use raw vault with no `_spendableAfterReleasing` check. Root cause: commit `a12de36` not merged into `dev-merge`. | `ClanWorld.sol` | 1077–1086, 3790–3793 | **2 agents** | W2-10, W2-11 |
| M-3 | **Winter wood burn not reservation-aware** — `_applyUpkeep` (and `_simulateApplyUpkeep`) burns wood from full vault without subtracting `_reservedWoodByClan`. Can consume wood committed to wall upgrades. Wheat correctly uses reservation-aware math. | `ClanWorld.sol` | 588–602, 1349–1362 | **1 agent** | W2-11 |
| M-4 | **Runner `getWorldState` ABI has duplicate tuple fields** — Embedded ABI has 17 components vs 15 in `WorldState` struct. `currentSeasonNumber` and `nextHeartbeatAtTick` appear twice. Will cause viem decode failures at runtime. | `runnerCastHeartbeat.ts` | 38–57 | **2 agents** | W1-7, W2-10 |
| M-5 | **Simulation `_simulateResolveAction` missing `WithdrawResources` branch** — Real `_resolveAction` calls `_doWithdrawResources`; sim falls through with no-op. Withdraw missions never settle in simulation, causing `getClanFullView`/`getRankings` to diverge from chain. | `ClanWorld.sol` | 1529–1561 vs 797–835 | **1 agent** | W2-9 |

### SHOULD FIX (17 findings — address in this PR before merge)

| # | Finding | File(s) | Lines | Convergence | Agents |
|---|---------|---------|-------|-------------|--------|
| S-1 | **Sim `_simulateMarkClansmanDead` missing reservation refund** — Real path refunds upgrade reservations on death; sim only clears `m.active`. `sim.reservedWheat` stays inflated, causing starvation timing divergence in views. | `ClanWorld.sol` | 1441–1452 vs 687–704 | **3 agents** | W1-2, W2-9, W2-11 |
| S-2 | **Sim settles beyond `MAX_LAZY_SETTLE_BACKLOG`** — `_simulateSettleToTick` loops to `currentTick`; real `_settleClan` caps at 200 ticks. Views project a "fully settled" clan that on-chain is still lagging. | `ClanWorld.sol` | 1260–1302 vs 486–531 | **1 agent** | W2-9 |
| S-3 | **Wall upgrade deadlock after cold degrades below `fromLevel`** — Cold damage can reduce `wallLevel` below `fromLevel`. The mismatch branch returns `false` with no refund, creating a perpetually active mission with locked resources. | `ClanWorld.sol` | 1134–1141 | **2 agents** | W1-2, W2-11 |
| S-4 | **Cooldown gate blocks scheduled market orders** — Comment says scheduled orders bypass cooldown, but code returns `ERR_COOLDOWN_ACTIVE` for all order types before branching. | `ClanWorld.sol` | 3291–3300 | **1 agent** | W1-1 |
| S-5 | **Market try/catch maps all reverts to `ERR_LIQUIDITY_INSUFFICIENT`** — Catches collapse all swap failures to one error code, hiding bugs from operators/indexers. | `ClanWorld.sol` | 3568–3576 | **1 agent** | W1-1 |
| S-6 | **Market sells use `minOut = 0`** — `swapExactInForOut(amount, 0)` accepts any positive output. No slippage protection for sellers (buys have `maxGoldIn`). | `ClanWorld.sol` | 3912; `StubPool.sol` 99–100 | **2 agents** | W1-1, W1-4 |
| S-7 | **`BanditSpawned` event missing `atTick`** — Other bandit lifecycle events carry a tick; spawn does not, breaking indexer time-anchoring. | `IClanWorld.sol` | ~644 | **1 agent** | W1-3 |
| S-8 | **`BlueprintAwarded` declared but never emitted** — Only `BlueprintEarned` fires on defense. Consumers subscribing to `BlueprintAwarded` see nothing. | `IClanWorld.sol`, `ClanWorld.sol` | ~670, ~2259 | **1 agent** | W1-3 |
| S-9 | **`ActiveBanditView` missing `carryGold` + `nextActionTick` unset for Attacking** — View struct omits gold carry; `Attacking` state leaves `nextActionTick = 0`. | `ClanWorld.sol` | 5337–5372 | **1 agent** | W1-3 |
| S-10 | **`getBanditTargetPreview` contradicts interface semantics** — Returns stored `targetClanId` (usually 0 pre-attack) not a simulation of `_pickBanditAttackTarget`. | `ClanWorld.sol` | 5035–5037 | **1 agent** | W1-3 |
| S-11 | **Transfers credit dead recipient clan** — `toClan` has no `clanState != DEAD` check. Resources/gold can be pushed to a dead clan's ledger. | `ClanWorld.sol` | 4611–4764 | **2 agents** | W1-3, W1-4 |
| S-12 | **`submitOrders` drops `marketMode` from `OrderResult`** — Solidity `OrderResult` includes `marketMode`; TypeScript `SubmitOrderResult` omits it. Elders can't distinguish scheduled vs immediate market. | `IChainClient.ts` | 13–18, 2427–2433 | **1 agent** | W1-7 |
| S-13 | **StubPool reserves vs ERC20 balances diverge** — Swaps update `reserveA`/`reserveB` in storage only; no ERC20 transfers after seeding. Any consumer assuming reserves equal balances is wrong. | `StubPool.sol` | 61–94 | **2 agents** | W1-1, W1-4 |
| S-14 | **CI workflow missing `forge build` + `check:abi` step** — Workflow only checks TS fragment vs committed ABI JSON. Does not verify Solidity source → ABI artifact consistency. | `.github/workflows/chainclient-abi.yml` | 11–22 | **1 agent** | W1-5 |
| S-15 | **IChainClient exposes only 8 of 48 contract functions** — Generated `CLAN_WORLD_ABI` covers 19 via whitelist; TS interface wraps 8. Growing contract surface is not automatically mirrored. | `IChainClient.ts`, `gen-chainclient-abi.mjs` | 53–62, 15–34 | **2 agents** | W1-5, W1-7 |
| S-16 | **Gas test name misleading** — `test_heartbeat_gasUnder12ClansAnd3DayLag` actually tests 1-tick normal cadence, not 3-day lag. | `GasProfiling.t.sol` | ~90–103 | **1 agent** | W1-6 |
| S-17 | **Heartbeat mission/upkeep coupling** — `_settleCompletingMissions` runs in heartbeat without calling `_settleClan` first. If `lastSettledTick` lags, a mission could complete without intervening upkeep ticks. | `ClanWorld.sol` | 2956–2974 | **1 agent** | W1-7 |

### DEFER (14 findings — open new GH issues)

| # | Finding | File(s) | Agents |
|---|---------|---------|--------|
| D-1 | Permissionless `mintClan` — no per-address limit, allowlist, or fee | `ClanWorld.sol` | W1-4 |
| D-2 | `finalizeSeason()` is empty stub — no on-chain season finality | `ClanWorld.sol` | W1-4, W1-6 |
| D-3 | `initTreasury` does not validate token/pool addresses | `ClanWorld.sol` | W1-4 |
| D-4 | Bandit spawn RNG is proposer-influenceable via `block.prevrandao` | `ClanWorld.sol` | W1-3, W1-4 |
| D-5 | `transferClanOwnership` has no settlement or death guard | `ClanWorld.sol` | W1-3 |
| D-6 | `_canBanditLeaveResting` allows immediate `Resting → Escaped` | `ClanWorld.sol` | W1-3 |
| D-7 | Untested view functions: `getDerivedClanState`, `getDerivedClansmanState`, `getRegionPopulation`, `getBanditTargetPreview` | test suite | W1-6 |
| D-8 | Worst-case heartbeat gas (~17.4M for 200-tick cap) exceeds 5M gate | `GasProfiling.t.sol` | W1-6 |
| D-9 | `check-chain-abi-parity.mjs` not wired into CI or pnpm scripts | `check-chain-abi-parity.mjs` | W1-5 |
| D-10 | `ClanWorldStub` partial `WorldState` init (bandit/seed fields zeroed) | `ClanWorldStub.sol` | W1-7, W2-10 |
| D-11 | Turbo `codegen` inputs exclude `abi/IClanWorld.json` (surprising cache) | `turbo.json` | W1-5 |
| D-12 | Shared TS types (`WorldSnapshot`, `ClanFullView`) not ABI-aligned | `types.ts` | W1-5 |
| D-13 | Bandit vault theft steals from total vault, not spendable vault | `ClanWorld.sol` | W2-11 |
| D-14 | Undeclared events (`ResourceMinted`, `ResourceBurned`) in interface never emitted | `IClanWorld.sol` | W1-1, W1-4 |

### SKIP / FALSE POSITIVE (13 findings — not issues)

| # | Finding | Reason |
|---|---------|--------|
| FP-1 | Gathering/deposit/timing logic | Reviewed thoroughly; arithmetic and ordering are correct |
| FP-2 | MinimalERC20 standard allowance | Acceptable for deploy+seed flow |
| FP-3 | Pool seed ratio asymmetry | Intentional per-resource pricing |
| FP-4 | Post-settle dead-state check ordering in transfers | Correct: settle → completeness → dead check |
| FP-5 | Stale-target skip for bandit attacks | Intentional design with 200-tick cap |
| FP-6 | Vault theft credit + defense blueprint logic | Correct: debit target, credit carry; blueprint on defense only |
| FP-7 | `getBandit`/`getBanditTroop` struct completeness | Full struct returned including tier + attackAttemptsMade |
| FP-8 | `BanditEscaped`/`BanditTargetDied` tick values | Correct: uses caller-provided tick |
| FP-9 | Upgrade events use `*LevelChanged` (not deprecated `*Upgraded`) | Correct |
| FP-10 | Winter boundary/cold reset alignment between sim and real upkeep | Matched |
| FP-11 | Cross-type upgrade switching refunds previous reservation | Correct |
| FP-12 | Codegen scripts deterministic | Confirmed |
| FP-13 | pnpm version in CI workflow | Fixed (reads from `packageManager`) |

---

## Summary Statistics

| Category | Count |
|----------|-------|
| **MUST FIX** | 5 |
| **SHOULD FIX** | 17 |
| **DEFER** | 14 |
| **SKIP / FALSE POSITIVE** | 13 |
| **Total findings** | 49 (de-duplicated from ~90 raw across 11 agents) |

---

## Convergent Findings (flagged by 2+ independent agents)

These carry the highest confidence:

| Finding | Agents | Severity |
|---------|--------|----------|
| OTC transfers reservation-blind | W1-3, W1-4, W2-10, W2-11 (4) | **MUST FIX** |
| WithdrawResources reservation-blind | W2-10, W2-11 (2) | **MUST FIX** |
| Runner ABI duplicate tuple fields | W1-7, W2-10 (2) | **MUST FIX** |
| Sim death missing reservation refund | W1-2, W2-9, W2-11 (3) | SHOULD FIX |
| Wall upgrade deadlock after cold | W1-2, W2-11 (2) | SHOULD FIX |
| Market sell slippage = 0 | W1-1, W1-4 (2) | SHOULD FIX |
| Transfer to dead clan | W1-3, W1-4 (2) | SHOULD FIX |
| StubPool virtual accounting | W1-1, W1-4 (2) | SHOULD FIX |
| IChainClient partial mirror | W1-5, W1-7 (2) | SHOULD FIX |
| Bandit RNG proposer influence | W1-3, W1-4 (2) | DEFER |

---

## Recommended Next Steps

### 1. Address MUST FIX items (in priority order)

1. **Merge PR #395 (`da95c34`) + commit `a12de36`** into `dev-merge`. These two commits fix M-1 and M-2 (reservation-aware transfers and withdrawals). They were completed but never integrated into this branch. Cherry-pick or merge.

2. **Fix winter wood burn reservation awareness (M-3)**. Mirror the wheat pattern: compute `spendableWood = vaultWood - _reservedWoodByClan[clanId]`, burn `min(woodNeeded, spendableWood)`, apply cold damage for the shortfall. Apply same fix in `_simulateApplyUpkeep`.

3. **Fix runner ABI (M-4)**. Remove the duplicate `currentSeasonNumber` and `nextHeartbeatAtTick` entries at lines 55–56 of `runnerCastHeartbeat.ts`. 15 components must match `WorldState` in `IClanWorld.sol`.

4. **Add `WithdrawResources` branch to `_simulateResolveAction` (M-5)**. Mirror `_doWithdrawResources` logic in the sim path so views reflect withdraw mission completion.

### 2. Address SHOULD FIX items (grouped by theme)

**Simulation parity (S-1, S-2)**:
- Add reservation refund to `_simulateMarkClansmanDead`
- Cap sim loop at `MAX_LAZY_SETTLE_BACKLOG` to match `_settleClan`

**Reservation system (S-3)**:
- Refund when `fromLevel > currentLevel` (wall degradation edge case)

**Market/economy (S-4, S-5, S-6, S-13)**:
- Fix cooldown vs scheduled market comment/behavior mismatch
- Narrow try/catch error codes
- Add `minGoldOut` for sells
- Document StubPool as virtual-accounting-only

**Events/ABI (S-7, S-8, S-9, S-10, S-12)**:
- Add `atTick` to `BanditSpawned`
- Remove or emit `BlueprintAwarded`
- Add `carryGold` to `ActiveBanditView`
- Fix `getBanditTargetPreview` name/semantics
- Add `marketMode` to TS `SubmitOrderResult`

**Infrastructure (S-14, S-15)**:
- Add `forge build` + `check:abi` to CI workflow
- Extend `gen-chainclient-abi.mjs` function whitelist

**Other (S-11, S-16, S-17)**:
- Add `toClan` liveness check on transfers
- Fix gas test name
- Document heartbeat mission/upkeep coupling invariant

### 3. Open DEFER issues

Create GitHub issues for D-1 through D-14 to track post-merge.

### 4. Overall PR Health Assessment

**Verdict: NEEDS WORK**

The contract logic is architecturally sound — the heartbeat pipeline, lazy settlement model, upgrade reservation system, bandit state machine, and winter mechanics are coherent and well-tested (292 Solidity tests + 16 TypeScript tests). However, **5 MUST FIX issues block merge**, the most critical being the missing reservation-aware transfer/withdrawal fixes (PR #395 not integrated) and the runner ABI regression. The simulation parity gaps (17 SHOULD FIX items) are significant but not merge-blocking for testnet.

The codebase is **safe for testnet deployment** once MUST FIX items are addressed. SHOULD FIX items should be addressed before any competitive or public-facing deployment.
