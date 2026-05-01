# PR #396 — "Dev merge" — Swarm Review

**Reviewer model:** claude-sonnet-4-6  
**Review date:** 2026-05-01  
**PR:** dev-merge → dev | 71 commits | 73 files | Phases 5.5–9.1+  
**Worktree:** `/tmp/pr396-review` (isolated from active work)  
**Wave structure:** 6 parallel Wave-1 agents → 3 parallel Wave-2 deep-validation agents → synthesis  

**Agents:**
- 1A: Solidity Security
- 1B: Solidity Correctness
- 1C: TypeScript & Adapters
- 1D: Test Quality
- 1E: Config & Build
- 1F: Documentation & Conventions
- 2A: ABI Parity Deep Validation (verified 1C claims)
- 2B: Deposit Timing & OTC Security Deep Validation (verified 1A/1B claims)
- 2C: Cross-Package Integration Consistency

---

## Executive Summary

No CRITICAL findings. **2 HIGH** issues that are straightforward fixes. **~10 MEDIUM** issues spanning contract security, TypeScript adapter gaps, and documentation. The overall PR is structurally sound — the Solidity state machine is correct, tests are well-written, and the BanditTroop/deposit changes are internally consistent. The two HIGH issues should be fixed before merge. The MEDIUM OTC reserved-resource bypass (security) is the most consequential game-economic risk.

---

## Consolidated Issue List

### MUST FIX (Blockers)

---

#### [MUST-1] HEARTBEAT_ABI has duplicate fields — corrupts WorldState decoding at runtime

**Severity:** HIGH  
**File:** `packages/runner/src/runnerCastHeartbeat.ts:55-56`  
**Confirmed by:** Wave 1C + Wave 2A (independently)

`HEARTBEAT_ABI`'s `getWorldState` component array declares **17 fields** for a **15-field** on-chain struct. Fields `currentSeasonNumber` (index 4) and `nextHeartbeatAtTick` (index 5) are duplicated at positions 15 and 16 under a misleading comment "appended fields — must match IClanWorld.sol WorldState layout."

viem decodes the ABI-encoded 15-field tuple against a 17-field descriptor — positions 15 and 16 read past the actual data boundary, returning garbage values. `RunnerCastHeartbeat.isHeartbeatDue()` and the `callHeartbeat()` rate-limit check are directly affected. The heartbeat scheduler may fire continuously or never.

**Fix:** Delete lines 54–56 (the comment and two duplicate field entries).

---

#### [MUST-2] `SubmitOrderResult` silently drops `marketMode` from on-chain `OrderResult`

**Severity:** HIGH  
**Files:** `packages/shared/src/adapters/IChainClient.ts:13-18` (interface), `:2428-2433` (mapping)  
**Confirmed by:** Wave 1C + Wave 2A (independently)

The ABI for `submitClanOrders` returns `OrderResult[]` with 5 fields: `clansmanId`, `status`, `cooldownEndsAtTs`, `missionNonce`, `marketMode`. The `SubmitOrderResult` TypeScript interface has only 4 fields — `marketMode` is absent. The result mapping at line 2428 explicitly drops it. Any consumer that needs to distinguish `Immediate` vs `Scheduled` market execution (Elders, orchestrator, frontend order display) gets `undefined`.

**Fix:**
1. Add `marketMode: number` to `SubmitOrderResult` interface
2. Add `marketMode: Number(result.marketMode)` to the mapping at line 2431

---

#### [MUST-3] `clanWorldFixture.ts` still uses old `BanditTroop` field names

**Severity:** HIGH (latent schema divergence)  
**File:** `packages/shared/src/mocks/clanWorldFixture.ts:77,79,310,312`  
**Found by:** Wave 2C

The `BanditState` interface and `BANDIT_STATE` fixture constant still reference `banditId` and `currentRegion` — the pre-Phase-9.1 field names. The Solidity `BanditTroop` struct and generated ABI both use `id` and `region`. This file is exported from `packages/shared/src/index.ts`. No TS consumer currently imports it, so it's not a live runtime break — but any code that adds a `getBandit`-based feature will consume a stale type.

**Fix:** Rename `banditId→id`, `currentRegion→region` in both the interface and the constant.

---

### SHOULD FIX

---

#### [SHOULD-1] OTC transfers bypass reserved-resource accounting — double-spend of upgrade reservations

**Severity:** MEDIUM (game-economic integrity)  
**Files:** `packages/contracts/src/ClanWorld.sol:4636-4677` (transferVaultResource), `:3702-3724` (_deductFromVault)  
**Confirmed by:** Wave 1A + Wave 2B (independently)

`transferVaultResource`, `transferBlueprint`, and `transferBundle` check raw vault balances (`vaultWood >= amount`) without calling `_deductFromVault` or `_spendableAfterReleasing`. The market engine correctly uses `_deductFromVault`, which subtracts `_reservedWoodByClan[clanId]` from the spendable amount.

**Attack path:** (1) Submit `UpgradeWall` — 50 WOOD is reserved in `_reservedWoodByClan[A]`. (2) Call `transferVaultResource(A, B, Wood, 50)` — passes raw check, debits `vaultWood`. (3) Next heartbeat: `_settleWallUpgrade` tries to debit 50 WOOD again — Solidity 0.8 reverts on underflow, silently aborting the upgrade. The wood is permanently transferred away while the reservation entry remains inconsistent. Clan owner can exfiltrate reserved resources to another clan they control, bypassing upgrade cost enforcement.

**Fix:** Replace raw balance checks in all OTC transfer functions with `_spendableAfterReleasing(vault, reserved, 0) >= amount`, or call `_deductFromVault` before crediting the recipient.

---

#### [SHOULD-2] `mintClan` is permissionless — cap-stuffing griefing possible

**Severity:** MEDIUM  
**File:** `packages/contracts/src/ClanWorld.sol:3106`  
**Found by:** Wave 1A

Any address can call `mintClan(anyAddress)` with no caller check. The MAX_CLANS cap can be filled by a griefer before legitimate players join, permanently blocking new clan creation. Minting to arbitrary addresses could also confuse ownership accounting.

**Fix:** Add a temporary `require(msg.sender == _treasury.treasuryOwner, "ClanWorld: only treasury")` guard until the World ID iNFT integration lands in Wave 1/S2.

---

#### [SHOULD-3] `_executeMarketSell` credits gold after external call — CEI gap

**Severity:** MEDIUM (currently safe, architectural fragility)  
**File:** `packages/contracts/src/ClanWorld.sol:4056-4061`  
**Found by:** Wave 1A

The scheduled market sell deducts carry before the `StubPool.sellResource()` external call (good), but `clan.goldBalance += goldOut` occurs after it. Currently safe because `nonReentrant` on `heartbeat()` and `StubPool.onlyEngine` prevent exploitation. If StubPool is replaced with an untrusted pool, this creates a classic reentrancy window. `finalizeSeason()` (line 3097) has no `nonReentrant` guard — it is a future entry point.

**Fix:** Move `clan.goldBalance += goldOut` before the external call; add `nonReentrant` to `finalizeSeason()` now.

---

#### [SHOULD-4] `_executeMarketBuy` TOCTOU between quote and swap

**Severity:** MEDIUM  
**File:** `packages/contracts/src/ClanWorld.sol:4099-4122`  
**Found by:** Wave 1A

The buy path calls `quoteBuy` for balance pre-check, then `swapExactOutForInWithMaxIn`. If another scheduled order runs between the two calls in the same heartbeat loop and moves pool reserves, `actualGoldIn` from the swap can exceed the `goldIn` from the stale quote, causing `clan.goldBalance -= actualGoldIn` to emit the wrong error code or silently fail the order with `ERR_LIQUIDITY_INSUFFICIENT` instead of `ERR_NOT_ENOUGH_GOLD`.

**Fix:** Remove the separate `quoteBuy` pre-check; deduct gold before the swap call and rely solely on `swapExactOutForInWithMaxIn`'s internal slippage check.

---

#### [SHOULD-5] `DEPLOYER_PRIVATE_KEY` backwards-compat shim + missing vars in `.env.template`

**Severity:** MEDIUM (violates CLAUDE.md §4 env var rule)  
**Files:** `packages/shared/src/adapters/IChainClient.ts:2397-2404`, `.env.template`  
**Found by:** Wave 1C

Two env var issues:
- `ELDER_WALLET_KEY_PATH` falls back to `DEPLOYER_PRIVATE_KEY` — both reference the same concept (Elder signing key). CLAUDE.md §4: "ONE env var per concept, NO backwards-compat shims." The fallback shim should be removed.
- `RUNNER_PRIVATE_KEY` is read by `runnerCastHeartbeat.ts` but absent from `.env.template`. Operators copying the template will miss it and get silent auth failures.

**Fix:** Remove the `DEPLOYER_PRIVATE_KEY` fallback. Add `ELDER_WALLET_KEY_PATH=` and `RUNNER_PRIVATE_KEY=` to `.env.template`.

---

#### [SHOULD-6] Zero events in `CLAN_WORLD_ABI` — Phase 5.5 deposit event param renames invisible to TS consumers

**Severity:** MEDIUM  
**File:** `scripts/gen-chainclient-abi.mjs:15-34`  
**Found by:** Wave 1C

The ABI generator allowlist contains 18 function names and zero event names. `ResourcesDeposited` event param renames (from `w/ir/wh/fi` to `wood/iron/wheat/fish`), new Phase 9 bandit events, and Phase 10 `WinterStarted`/`WinterEnded`/`ClanEliminated` events are all absent from `CLAN_WORLD_ABI`. Any future off-chain indexer or Convex webhook handler that imports `CLAN_WORLD_ABI` to decode logs will silently fail.

**Fix:** Add the relevant events to the generator allowlist, regenerate `IChainClient.ts`.

---

#### [SHOULD-7] ABI parity test only validates hardcoded fixtures against each other

**Severity:** MEDIUM  
**File:** `packages/shared/src/adapters/check-chain-abi-parity.test.ts:83-123`  
**Confirmed by:** Wave 1C + Wave 2C (independently)

The parity test uses two inline hardcoded fixtures (`ABI_SNAPSHOT` and `REQUIRED_OUTPUTS`) and asserts them against each other — it never imports the live `CLAN_WORLD_ABI` from `IChainClient.ts` or reads `packages/contracts/abi/IClanWorld.json`. The test cannot catch drift between the generated TS adapter and the compiled Solidity ABI. The HEARTBEAT_ABI duplicate field bug (MUST-1) was not caught by any automated check.

**Fix:** Add a test that loads both `IClanWorld.json` and the exported `CLAN_WORLD_ABI` and compares function names and parameter counts.

---

#### [SHOULD-8] PR description has no `Closes #N`, no labels, no milestone

**Severity:** MEDIUM (gitflow violation)  
**Source:** Wave 1F

PR body is: `"Final Merge Review gate"` — one line. Per CLAUDE.md §9: "PR body must include `Closes #N`." This PR bundles ~80+ issue-closing commits (issues #184–#391 spanning all phases). None are cited in the PR body. No labels or milestone are attached.

**Fix:** Add `Closes #N` references for all phases merged, apply `phase: multi` label, attach the `dev-merge` milestone.

---

#### [SHOULD-9] Duplicate agent CLI test files — both run in CI, testing conflicting state

**Severity:** MEDIUM  
**Files:** `packages/agents/src/__tests__/cli.test.ts`, `packages/agents/test/cli.test.ts`  
**Found by:** Wave 1D

Both files exist and vitest's default glob (`**/*.test.ts`) picks up both. They test the same CLI surface with slightly different stubs (`tick=42` vs `tick=7` for `world snapshot`). These are not the same test; they test slightly different exported surfaces, creating a dual-truth situation and CI confusion.

**Fix:** Remove `packages/agents/src/__tests__/cli.test.ts` — `packages/agents/test/cli.test.ts` is the superior file (richer coverage, path-traversal safety tests, issue #94 regression test).

---

#### [SHOULD-10] `DEMO_DRIFT.md` has stale file location reference

**Severity:** MEDIUM  
**File:** `docs/planning/DEMO_DRIFT.md:35`  
**Found by:** Wave 1F

Row for `VITE_REQUIRE_WORLD_APP_GUARD` cites `apps/web/src/App.tsx — (line 41)` as the definition location. After the env flag rename commit, the definition moved to `apps/web/src/config/env.ts:38-39`. App.tsx line 41 is now just an import.

**Fix:** Update the location column to `apps/web/src/config/env.ts:38`.

---

### DEFER (New GitHub Issues)

These are valid concerns but not PR-blocking. Each should become a GitHub issue.

| # | Severity | Finding | Suggested Issue Title |
|---|---|---|---|
| D-1 | LOW | `finalizeSeason()` has no `nonReentrant` guard and no access control stub | `chore(contracts): add nonReentrant + tick-gate stub to finalizeSeason()` |
| D-2 | LOW | No `toClan.clanState != DEAD` check in OTC transfer functions — resources orphaned in dead vault | `fix(contracts): guard OTC transfers against dead recipient clan` |
| D-3 | LOW | `_world`, `_clans`, `_clansmen` changed `private→internal` — opens inheritance write access | `docs(contracts): document internal storage visibility rationale` |
| D-4 | LOW | `block.prevrandao` is validator-influenceable for bandit spawn/gather crit rolls | `feat(contracts): document prevrandao bias risk + future VRF path` |
| D-5 | LOW | `ResourcesDeposited` uses `wood/iron/wheat/fish`, `ResourcesWithdrawn` uses `woodDelta/*` — parallel events named inconsistently | `fix(contracts): harmonize ResourcesDeposited param naming to *Delta convention` |
| D-6 | LOW | `_executeMarketSellExternal`/`_executeMarketBuyExternal` appear in the public ABI — confusing but guarded | `chore(contracts): consider convention suffix for try/catch internal helpers` |
| D-7 | LOW | `getBanditTroop` struct updated in ABI but not covered by any parity test; not exposed through `IChainClient` interface | `feat(shared): expose getBanditTroop via IChainClient and add parity test` |
| D-8 | LOW | `StubChainClient.getClanFullView` always returns empty `controlledRegions`/`pendingOrders`/`whispers` with no indicator | `chore(shared): add stub-limitation comment to getClanFullView` |
| D-9 | LOW | `readNextHeartbeatAt` uses unsafe `as { nextHeartbeatAtTs: bigint }` cast; `Number(bigint)` loses precision >2^53 | `fix(runner): use typed viem return instead of manual cast in readNextHeartbeatAt` |
| D-10 | LOW | `withdrawResources` fallback uses entire `o.payload` as resource bag when field is absent | `fix(shared): use zeroed struct as withdrawResources fallback` |
| D-11 | LOW | `VITE_WORLD_RP_ID` and `VITE_CONVEX_URL` absent from root `.env.template` | `chore(ops): add missing web env vars to .env.template` |
| D-12 | LOW | `build` task in `turbo.json` does not depend on `codegen` — stale ABI on clean checkout if codegen not run first | `chore(build): add codegen as dependency of build in turbo.json` |
| D-13 | LOW | `dev-cross-phase-hygiene` hardcoded in CI push trigger — stale after branch deletion | `chore(ci): remove stale branch from chainclient-abi.yml push trigger` |
| D-14 | LOW | `test/**/*` in `packages/shared/tsconfig.json` points to a non-existent directory | `chore(shared): remove dead test glob from tsconfig include` |
| D-15 | LOW | `@clan-world/contracts` declared as runtime `dependency` in `packages/shared` but should be `devDependencies` | `chore(shared): move @clan-world/contracts to devDependencies` |
| D-16 | LOW | `phase-3-test-spec.md` still says `pre-stage`, references non-existent `BanditState.Camping`/`Retreating` | `docs: update phase-3-test-spec.md to reflect current implementation` |
| D-17 | LOW | `clanworld_v4_spec.md §6.5` lists `CAMPING` state — impl has `Spawned+Camped` (4-tick pre-attack, not 3-tick) | `docs: update v4_spec bandit state model to Spawned+Camped` |
| D-18 | LOW | `ClansmanColdDeath` event uses `csId` — all other events use `clansmanId` — undocumented inconsistency | `fix(contracts): rename csId→clansmanId in ClansmanColdDeath event` |
| D-19 | LOW | `ERR_SLIPPAGE_REQUIRED` new status code (ordinal 31) not documented in any spec | `docs: add ERR_SLIPPAGE_REQUIRED to status code spec` |
| D-20 | LOW | Missing test: `getActionDuration(ActionType.WithdrawResources)` absent from duration enumeration test | `test(contracts): add WithdrawResources to MissionTiming duration enum test` |
| D-21 | LOW | Missing test: `ClanWorldStub.t.sol` does not cover `mintClan`/`submitClanOrders` parity | `test(contracts): add mintClan + submitClanOrders to ClanWorldStub test` |
| D-22 | LOW | Missing test: Bandit swap-delete with interior-index deletion (3 bandits, remove middle) | `test(contracts): add interior-index bandit delete to BanditSpawn test` |
| D-23 | LOW | `check-chain-abi-parity.mjs` only checks `getClanFullView.activeMission` tuple — 30+ functions unverified | `chore(shared): broaden ABI parity script to cover all functions in CLAN_WORLD_ABI` |
| D-24 | INFO | 45/88 commits in this PR missing `(#N)` issue reference — gitflow convention not fully followed | (note only — history already written) |

---

### SKIP / FALSE POSITIVE

The following Wave 1 concerns were investigated in Wave 2 and found to be non-issues:

| Concern | Verdict | Evidence |
|---|---|---|
| BanditTroop struct renames cause storage slot collision | FALSE POSITIVE | Non-upgradeable contract; only field names change, not slot order/types. All consumers updated. |
| `getBandit()` gas DoS or out-of-bounds revert risk | FALSE POSITIVE | Returns zero struct for missing/deleted IDs via mapping zero-init. Bounded read, no loops. |
| `BanditState.None = 0` semantically wrong default | FALSE POSITIVE | Correct — it means "no bandit." New bandits always start with `Spawned`. `delete _bandits[id]` correctly zeroes. |
| `ERR_NOT_AT_HOMEBASE` vs `ERR_INVALID_REGION` security regression | FALSE POSITIVE | Both codes coexist with non-overlapping semantics. Tests correctly assert each. |
| `IClanWorld.json` ABI does not match `IClanWorld.sol` | FALSE POSITIVE | Exactly 48 functions, all events and structs match. ABI correctly regenerated. |
| `_executeMarketSellExternal` publicly callable ABI exposure | FALSE POSITIVE | Correctly guarded by `require(msg.sender == address(this))`. Pattern is valid for `try/catch` internals. |
| Env var rename `VITE_DEMO_BYPASS_WORLD_GUARD→VITE_REQUIRE_WORLD_APP_GUARD` left stale refs | FALSE POSITIVE | Fully propagated across all 6 consumption sites. Zero occurrences of old name anywhere. |
| PR description's "instant (0-tick) deposit" is a code bug | FALSE POSITIVE | It is a **doc bug only**. Code was deliberately changed from 0→1 tick in commit `632e2b2` to align with Phase 4 contract design. All tests correctly assert 1-tick behavior. |
| `parseClanId` accepting leading zeros (`'007'`) is a bug | FALSE POSITIVE | `parseInt('007', 10)` correctly returns 7. Solidity `uint32` has no leading-zero concept. Intentional. |
| `@clan-world/contracts` added to shared causes import chain issues | FALSE POSITIVE | It's a workspace link only. No TS in shared imports from contracts. |
| `tsconfig.json` strict settings were relaxed | FALSE POSITIVE | No strictness settings changed. `test/**/*` include is the only tsconfig change (though it points to a nonexistent dir — see D-14). |
| VITE env var rename is semantically wrong | FALSE POSITIVE | Inversion is intentional and correct. Default-false (gate disabled) ≡ old default-true (bypass enabled). |
| React version skew between apps/web and apps/landing | FALSE POSITIVE | No shared React components. No runtime collision. Pre-existing, not introduced by this PR. |

---

## Findings Requiring Deposit Timing Clarification

Wave 2B confirmed: **the 1-tick behavior is deliberate, not a bug.** Commit `632e2b2` explicitly changed from `DEPOSIT_DURATION_TICKS = 0` (instant) to `= 1` (1-tick) to align with the Phase 4 contract. The PR description's "instant (0 tick duration)" language is a stale artifact from the initial Phase 5.5 commit before the alignment fix. The code, tests, and timing model are internally consistent. No code change needed — only the PR description is wrong (addressed in SHOULD-8).

---

## Recommended Next Steps

**Before merging PR #396:**

1. **Fix MUST-1** (5 min): Delete 3 lines from `runnerCastHeartbeat.ts` (the duplicate `currentSeasonNumber`/`nextHeartbeatAtTick` fields at lines 54–56).

2. **Fix MUST-2** (15 min): Add `marketMode: number` to `SubmitOrderResult` interface and the mapping at `IChainClient.ts:2431`.

3. **Fix MUST-3** (5 min): Rename `banditId→id`, `currentRegion→region` in `packages/shared/src/mocks/clanWorldFixture.ts`.

4. **Fix SHOULD-5** (10 min): Remove `DEPLOYER_PRIVATE_KEY` fallback shim; add `ELDER_WALLET_KEY_PATH=` and `RUNNER_PRIVATE_KEY=` to `.env.template`.

5. **Fix SHOULD-9** (2 min): Delete `packages/agents/src/__tests__/cli.test.ts`.

6. **Fix SHOULD-10** (2 min): Update `DEMO_DRIFT.md:35` location column to `apps/web/src/config/env.ts:38`.

7. **Fix SHOULD-8** (10 min): Update PR description with `Closes #N` references, add label and milestone.

**High-priority post-merge issues to open:**

- Open GitHub issue for SHOULD-1 (OTC reserved-resource bypass) — this is the most consequential game-economic correctness issue and should be fixed before any real-value deployment.
- Open GitHub issue for SHOULD-3 + SHOULD-4 (market CEI gaps) — fix before StubPool is replaced with a real AMM.
- Open GitHub issue for SHOULD-2 (mintClan permissionless) — add temporary treasury-owner guard before testnet demo.

**Defer tracking (batch-file as one issue sweep):**

Create a single cleanup sweep issue referencing D-1 through D-24 for post-hackathon cleanup. The D-11 env template gaps (`VITE_WORLD_RP_ID`, `VITE_CONVEX_URL`) are particularly likely to bite operators during demo setup.

---

## Wave-by-Wave Agent Summary

| Agent | Findings | Key Result |
|---|---|---|
| 1A Solidity Security | 4 MEDIUM, 5 LOW, 6 INFO | OTC reserved-resource bypass is the most actionable finding |
| 1B Solidity Correctness | 1 MEDIUM (doc), 1 LOW (naming), 8 PASS | 0-tick vs 1-tick is doc-only; all Solidity logic correct |
| 1C TypeScript/Adapters | 2 HIGH, 5 MEDIUM, 3 LOW, 4 INFO | HEARTBEAT_ABI duplicate and missing marketMode are blockers |
| 1D Test Quality | 1 MUST (dup files), 5 MISSING, 7 GOOD | Tests are well-written; duplicate CLI test file is the only blocker |
| 1E Config/Build | 1 HIGH (pre-existing, fixed), 1 MEDIUM, 5 LOW | PR fixes a pre-existing duplicate `test` key; build/codegen ordering gap |
| 1F Docs/Conventions | 1 HIGH, 6 MEDIUM, 5 LOW | PR description is severely underspecified |
| 2A ABI Parity | CONFIRMED H-1, CONFIRMED H-2, +1 (30 missing functions context) | Both HIGH claims independently verified |
| 2B Deposit/Security | CONFIRMED 1-tick is deliberate, CONFIRMED OTC bypass | Deposit doc bug only; OTC bypass is a real MEDIUM-HIGH game risk |
| 2C Integration | 1 MEDIUM (stale fixture), 4 CLEAN | clanWorldFixture.ts is the only cross-package stale ref |

---

*Generated by claude-sonnet-4-6 via 9-agent swarm (6 Wave 1 + 3 Wave 2)*
