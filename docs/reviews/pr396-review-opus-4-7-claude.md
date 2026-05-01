# PR #396 — Independent Review (Opus 4.7, 1M context)

**Reviewer:** `opus-4-7-claude`
**Date:** 2026-05-01
**PR:** [#396 "Dev merge"](https://github.com/OmniPass-world/clan-world/pull/396) — base `dev` ← head `dev-merge`
**Scope:** 68 files, +12,583 / −1,151 — phases 5 → 10 (bandits, buildings, transfers, market, winter, gas profiling) + ABI/adapter regen
**Worktree:** `/home/claude/code/omnipass-world/clan-world-pr396-blind-review` (`origin/dev-merge` @ 784b4bb)

## Summary

**Recommendation: merge after must-fix items are addressed.**

PR #396 is a large but coherent merge gate that consolidates roughly six phases of contract work plus cross-cutting infrastructure. **Build and test signals are green** (`forge test` 292/292, vitest 178/178 across shared/runner/agents, `check:abi` parity, `pnpm -w build` and `typecheck` clean). The ABI between `IClanWorld.sol` ↔ `packages/contracts/abi/IClanWorld.json` ↔ `IChainClient.ts` is verified by both `check:abi` and `gen-chainclient-abi.mjs --check`.

The review found **3 must-fix items** (1 latent runtime bug, 2 spec divergences in bandit combat that need either reconciliation or an ADR), **~16 should-fix items** (mostly hardening, observability, and small ABI/test gaps), **~12 deferrable items**, and **~6 false-positives** that are explicitly sanctioned by the v4.5 addendum, v4.6 buildings doc, or `CLAUDE.md`.

Headline counts:

| Category | Count |
|---|---|
| Must-fix | 3 |
| Should-fix | 16 |
| Defer (open issue) | 12 |
| Skip / false positive | 6 |

## Methodology

- **Wave 1 — 6 parallel specialist sub-agents** in a fresh `dev-merge` worktree, each with an independence guardrail forbidding access to any prior review docs (`docs/reviews/`, `.phase*-*-codex*.md`, etc.) and `/home/claude/.claude/plans/`. Specialists: (1) Bandits, (2) Buildings/Upgrades, (3) Transfers/Market/Gathering, (4) Security/Cross-cutting, (5) Adapters/ABI/Interface, (6) Build/Config/Docs.
- **Wave 2 — 2 parallel validators**: (7) build/test verification ran the toolchain end-to-end; (8) spec-compliance cross-check validated the most-cited Wave-1 findings against the v4.5 addendum, v4.6 buildings doc, v4 spec, ADR002, ADR003, and `CLAUDE.md`.
- **Wave 3 — synthesis** (this report): dedupe across the 8 reports, reconcile contradictions using Wave-2 verdicts as the tiebreaker, categorize, and recommend next steps.

Build/test signals (Wave 2):

| Step | Exit | Result |
|---|---|---|
| `pnpm install --frozen-lockfile` | 0 | lockfile current, no peer-dep errors |
| `pnpm -w typecheck` (forced) | 0 | 11 tasks, zero TS errors |
| `pnpm -w build` | 0 | 8 tasks clean |
| `pnpm -F @clan-world/contracts run check:abi` | 0 | `out/IClanWorld.sol/IClanWorld.json` byte-equal to `abi/IClanWorld.json` |
| `pnpm -F @clan-world/contracts run test` (forge) | 0 | 22 suites · 292 tests · 0 failed |
| `pnpm -F @clan-world/shared test` | 0 | 4 files · 20 passed |
| `pnpm -F @clan-world/runner test` | 0 | 9 files · 121 passed · 1 intentional skip |
| `pnpm -F @clan-world/agents test` | 0 | 2 files · 37 passed |

Toolchain: pnpm 10.33.2, node v24.13.1, forge 1.5.1-stable.

---

## Findings — Must-fix

### MF-1 [CRITICAL] Runner `HEARTBEAT_ABI` has 17 components vs canonical 15 — duplicates `currentSeasonNumber` and `nextHeartbeatAtTick`

**File:** `packages/runner/src/runnerCastHeartbeat.ts:39-57`
**Why must-fix:** Latent runtime bug. The runner's hand-rolled tuple ABI for `getWorldState()` lists `currentSeasonNumber`/`nextHeartbeatAtTick` at positions 4–5 (correct) **and again at positions 15–16** with the comment "appended fields — must match IClanWorld.sol WorldState layout." Canonical `IClanWorld.json` has 15 fields total; on-chain `getWorldState()` returns a 15-tuple. viem will fail to decode the 17-tuple shape against a 15-tuple result, breaking heartbeat rate-limit detection at runtime.
**Why tests didn't catch:** the only runner test for this file is `configFromEnv` — never the broken decode path.
**Recommendation:** delete lines 55–56 (the duplicate "appended" fields). Add a smoke test that issues `getWorldState()` against `ClanWorldStub` and asserts the read decodes (the stub is already in the contracts package).
**Confidence:** very high — verified by component count of `IClanWorld.json` and direct read of the runner file. Wave 2 spec cross-check confirms (Finding 14).

### MF-2 [HIGH] Bandit combat: wall absorption + 2× defeat threshold + missing Case B diverge from spec §6.13/§6.15 with no ratifying ADR

This is one logical decision split across three Wave-1 findings:

- **Wall:** `_applyBanditWallDamage` (`ClanWorld.sol:2553-2570`) absorbs flat `WALL_HP_PER_LEVEL = 100` per hit regardless of `wallLevel`. Spec §6.13: "wallDefense = 10 × wallLevel" added to total defense.
- **Defeat threshold:** `ClanWorld.sol:2225` — `defeated = totalClansmanDefense >= banditAttackPower * 2`. Spec §6.15 Case A: `clansmanDefense >= banditAttack` (1×).
- **Case B not implemented:** `ClanWorld.sol:2234-2244` has only binary defeat-or-fail. Spec §6.15 Case B: defended-with-wall-chip when `clansmanDefense < banditAttack` and `totalDefense >= banditAttack`.

**Why must-fix:** these are spec divergences, not bugs — but they materially change the difficulty of bandit defense and Elder strategy. Without an ADR, the spec is the source of truth and the contract is wrong. With an ADR, the spec needs updating.
**Recommendation:** **either** (a) reconcile impl to spec §6.13/§6.15 (clansmanDefense ≥ 1× defeats; level-scaling wallDefense; Case B with wall chip), **or** (b) write an ADR ratifying the rebalance and update `clanworld_v4_spec.md` §6.13/§6.15. The author's intent is unclear; ADR002 (spawn ramp) sets the precedent for ratifying rebalances.
**Confidence:** very high — Wave 2 confirmed all three as CONFIRMED divergences with explicit spec citations.

### MF-3 [HIGH] `check-chain-abi-parity.test.ts` is a self-tautology — does not import the canonical ABI it claims to guard

**File:** `packages/shared/src/adapters/check-chain-abi-parity.test.ts:14-92`
**Why must-fix:** The test compares `REQUIRED_OUTPUTS` (literal in the file) against `ABI_SNAPSHOT` (also a literal in the same file). It never imports `CLAN_WORLD_ABI` from `IChainClient.ts`, never reads `packages/contracts/abi/IClanWorld.json`. The test passes regardless of drift; it documents nothing and proves nothing. This file's existence creates false confidence — anyone reading it will assume parity is enforced.
**Why this is must-fix and not should-fix:** the test is a guard *that the team will trust*, and it doesn't work. Either delete it, or fix it. Leaving a misleading guard in place is worse than no guard.
**Recommendation:** delete the file (sufficient — `gen-chainclient-abi.mjs --check` already enforces real parity in CI via `chainclient-abi.yml`), **or** rewrite it to `import { CLAN_WORLD_ABI } from '../adapters/IChainClient'` and walk that ABI for each entry in `REQUIRED_OUTPUTS`. The companion `clanWorldAbi.test.ts` shows the right pattern.
**Confidence:** very high — Wave 2 confirmed (Finding 15).

---

## Findings — Should-fix

### SF-1 Duplicate `cli.test.ts` files maintained in parallel
**File:** `packages/agents/src/__tests__/cli.test.ts` and `packages/agents/test/cli.test.ts`
**Detail:** both files exist, both run under vitest, both received the same 5-line addition in this PR — they're maintained in parallel. Contents differ (one uses vi.fn mocks, one uses async stubs), violating `CLAUDE.md §4` "minimal tests only."
**Action:** delete `packages/agents/src/__tests__/cli.test.ts`; the `test/` location is the more thorough copy.

### SF-2 `OrderResult.marketMode` field present in ABI but missing from TypeScript `SubmitOrderResult`
**File:** `packages/shared/src/adapters/IChainClient.ts:13-18`
**Detail:** ABI tuple has 5 fields (clansmanId, status, cooldownEndsAtTs, missionNonce, marketMode); TS interface has 4. `RealChainClient.submitOrders` silently drops the field on its return shape.
**Action:** add a `MarketExecutionMode` const enum mirror; include `marketMode: MarketExecutionMode` in `SubmitOrderResult`.

### SF-3 `transferClanOwnership` does not call `_settleClan` first
**File:** `packages/contracts/src/ClanWorld.sol:4587-4597`
**Detail:** asymmetric with `transferGold/Vault/Blueprint/Bundle` which all settle then check `fromClan.clanState != DEAD`. A clan can transfer ownership while it has unsettled ticks pending — including ticks that would mark it DEAD. New owner inherits a possibly-doomed clan.
**Action:** insert `_settleClan(clanId)` before mutating owner, then reject if dead.

### SF-4 `mintClan` is fully permissionless — anyone can grief the 12-clan cap
**File:** `packages/contracts/src/ClanWorld.sol:3106-3185`
**Detail:** no auth at all. A single address can mint all 12 clan slots before a demo. CLAUDE.md §10 calls out humanity verification as "S2 if scoped"; spec §12.1 doesn't gate it. Acceptable for a controlled demo, but trivially griefable.
**Action:** for S1, gate behind `_treasury.treasuryOwner` or require `msg.sender == to` with a per-EOA cap. For S2, route through World ID.

### SF-5 First-tick RNG seed is `bytes32(0)` (constructor doesn't seed)
**File:** `packages/contracts/src/ClanWorld.sol:222-233`
**Detail:** `_world.currentTickSeed` defaults to `bytes32(0)`. First heartbeat closes tick 0 with a zero seed, mixed via keccak with per-clan/per-csId nonces. Domain separation makes practical bias negligible, but it's free to fix and removes a "did anyone notice?" liability.
**Action:** in constructor, set `_world.currentTickSeed = keccak256(abi.encode(block.prevrandao, address(this), block.timestamp));` and write `_tickSeeds[0]`.

### SF-6 Bandit spawn accumulator does not decay during global cap — burst spawn after kill
**File:** `packages/contracts/src/ClanWorld.sol:2665-2700`
**Detail:** while `_activeBanditCount >= MAX_TOTAL_BANDITS`, `_evaluateBanditSpawns` early-returns without decaying per-region `probabilityAccum`. Once a bandit terminally exits, every region simultaneously rolls at up to 80%. Coordinated burst-spawn potential.
**Action:** either decay accumulators while cap is full, or reset them when a bandit is terminally removed. Add a test exercising the cap-clear → next-tick-spawn path.

### SF-7 `initTreasury` accepts zero / colliding token & pool addresses silently
**File:** `packages/contracts/src/ClanWorld.sol:4533-4548`
**Detail:** one-shot init, no per-element validation. A typo permanently bricks the contract (no resetter). `seedPools` will fail later but by then the operator has lost the deploy fee.
**Action:** add `require(tokens[i] != address(0)) && require(pools[i] != address(0))` per index, plus a uniqueness check.

### SF-8 `MinimalERC20.transfer/transferFrom` don't reject `to == address(0)` (silent burn)
**File:** `packages/contracts/src/MinimalERC20.sol:51-64`
**Detail:** unreachable in current paths (no clan ever calls these), but a one-liner of cheap insurance against future accounting drift.
**Action:** `require(to != address(0), "MinimalERC20: zero to")`.

### SF-9 Stub `submitClanOrders` returns empty array — `OrderResult` tuple shape never exercised
**File:** `packages/contracts/src/ClanWorldStub.sol:117-126`
**Detail:** `return new OrderResult[](0)` means the new `marketMode` field is never built or returned. If the on-chain shape changes again, stub-side parity goes undetected.
**Action:** echo `clanOrders.length` with a default `OrderResult{ status: OK, marketMode: Immediate }`. Add a stub test that decodes via `CLAN_WORLD_ABI`.

### SF-10 `BlueprintAwarded` event in interface but never emitted
**File:** `packages/contracts/src/IClanWorld.sol:670`
**Detail:** declared next to active `BlueprintEarned`. Indexers may subscribe and receive nothing.
**Action:** delete from interface, or emit alongside `BlueprintEarned` if external consumers depend on it.

### SF-11 World App guard flag inverted — secure-default lost (S2 risk)
**File:** `apps/web/src/App.tsx:74-75`, `apps/web/src/config/env.ts:38-39`, `.env.template:51`
**Detail:** old `VITE_DEMO_BYPASS_WORLD_GUARD` (unset) defaulted gate ON; new `VITE_REQUIRE_WORLD_APP_GUARD` (unset) defaults gate OFF. Wave-2 spec check confirms the inversion is INTENTIONAL for S1's thin-wrapper scope (CLAUDE.md §2). However, an S2 production deploy that forgets to set the var ships with the gate silently bypassed.
**Action:** keep the rename (intentional) **but** add a build-time guard at module scope: `if (import.meta.env.PROD && !VITE_REQUIRE_WORLD_APP_GUARD) throw new Error("VITE_REQUIRE_WORLD_APP_GUARD must be set in production")`. Also delete the stale comment block at `App.tsx:71-73`.

### SF-12 `BanditState` enum reorder breaks any numeric ABI client
**File:** `packages/contracts/src/IClanWorld.sol:117-127`
**Detail:** identifiers PascalCased and a new `Spawned=1` inserted, shifting all subsequent positions. CAMPING was 1 → Camped is now 2. Per CLAUDE.md "no production users yet, break things freely" — but Phase 9B/9C downstream consumers (Convex indexer, frontend) may have hardcoded numbers.
**Action:** add a `BanditStateEnum` const map in `IChainClient.ts` and a positional-invariance test (`expect(BanditStateEnum.Camped).toBe(2)`).

### SF-13 `ImmediateMarketActionExecuted` event signature changed — topic-0 churn
**File:** `packages/contracts/src/IClanWorld.sol:592-616`
**Detail:** field set + indexed-ness changed. Same for `ScheduledMarketActionExecuted`. Topic-0 changes; clansmanId no longer indexed. Convex indexer subscriptions must be regenerated from this PR's ABI artifact.
**Action:** confirm with backend that the indexer reads from `IClanWorld.json` directly (not a hand-maintained schema) and that the running deployment will pick up the new topic.

### SF-14 `StubPool` virtual reserves drift from real ERC20 balances — misleading "TVL" reads
**File:** `packages/contracts/src/StubPool.sol:62-95`
**Detail:** swap functions mutate `reserveA`/`reserveB` storage but never `transfer`/`transferFrom` real ERC20 tokens. Indexers reading `IERC20(token).balanceOf(pool)` (the universal AMM TVL invariant) will see a constant; `pool.getReserves()` will diverge.
**Action:** rename to `VirtualConstantProductPool`, add a NatSpec banner explaining the model. Acceptable for hackathon scope.

### SF-15 Cross-function reentrancy through self-`try`/`catch` masks revert reason as `ERR_LIQUIDITY_INSUFFICIENT`
**File:** `packages/contracts/src/ClanWorld.sol:3568-3577`
**Detail:** `_executeScheduledMarketActions` catches all reverts (including reentrancy guard fires) and emits `ERR_LIQUIDITY_INSUFFICIENT`. Real reentrancy is blocked (Reentrancy.t.sol confirms), but observability is wrong.
**Action:** distinguish revert reasons in the catch block and emit a more accurate StatusCode.

### SF-16 `_findOldestActiveBandit` and `_releaseDefendersForDeadTarget` scan all regions/clansmen — fragile if `MAX_TOTAL_BANDITS` is ever raised
**File:** `packages/contracts/src/ClanWorld.sol:2048-2064, 2275-2310`
**Detail:** `MAX_TOTAL_BANDITS = 1` keeps these O(small). With cap raised, these become hot paths called inside heartbeat resolution.
**Action:** add a NatSpec note that complexity is gated by the bandit cap. Add a test that flips the cap (test-only) and asserts heartbeat gas remains bounded.

---

## Findings — Defer (open follow-up issues)

For each, a draft `gh issue create` title + 2-line body. None of these block merge.

### D-1 — `feat(runner): add ABI-shape smoke test for getWorldState`
> The runner's hand-maintained `HEARTBEAT_ABI` has drifted in the past. Add a vitest that decodes a real `getWorldState()` response (stub or fixture) using the runner's local ABI tuple to detect future drift.

### D-2 — `chore(ci): install foundry in chainclient-abi.yml so check:abi runs in CI`
> Local `pnpm -F @clan-world/contracts run check:abi` works (forge installed); CI workflow has no foundry step. Either install foundry there or hard-fail when forge is missing instead of silently skipping (`packages/contracts/package.json:8`).

### D-3 — `feat(contracts): emit MissionStalled event when future-level upgrade reservations cannot progress`
> Per v4.6 §13, future-level reservations are intentionally retained until invalidated. Today the only way to detect a stuck reservation is to read storage. Emit a `MissionStalled(clanId, clansmanId, reason)` event after N ticks of no progress for observability.

### D-4 — `chore(turbo): split codegen task per output set so caching isn't lopsided`
> `turbo.json` `codegen` task lists three different output globs that exist in three different packages (`abi/IClanWorld.json`, `src/adapters/IChainClient.ts`, `convex/_generated/**`). Split into `codegen:abi`, `codegen:adapter`, `codegen:convex` with strict per-task outputs.

### D-5 — `test(contracts): add gas profiling assertion at MAX_LAZY_SETTLE_BACKLOG=200 with 12 clans`
> `GasProfiling.t.sol` logs ~17.4M gas for the 200-tick × 12-clan worst case but does not assert (intentional per #322). Add a soft ceiling assertion (e.g. 30M) so regressions are caught.

### D-6 — `test(shared): replace check-chain-abi-parity.test.ts with real parity assertions` *(see MF-3)*
> If MF-3 chooses delete, this issue is the rewrite path. If MF-3 chooses rewrite, this issue is closed by the same PR.

### D-7 — `test(contracts): cover ClanWorldStub mintClan / transferClanOwnership / transferGold paths`
> Stub gained ~200 lines of behavior; only one test exists. Add 3-4 happy-path tests per CLAUDE.md §4.

### D-8 — `refactor(contracts): align WorldState and WorldSnapshot field ordering`
> Same fields in different orders today. Easy decoder mistake. Either align or add a NatSpec banner on `WorldSnapshot` explicitly listing the divergence.

### D-9 — `chore(contracts): rename ResourceBoundaryTokens.t.sol → BoundaryTokenWiring.t.sol`
> Misleading name suggests numeric overflow coverage; actually tests ERC20 wiring. One-line rename.

### D-10 — `docs(adr): cross-link ADR002, ADR003, and clanworld_v4_6_buildings_alignment.md`
> Three docs cover overlapping decisions. Add reciprocal links for discoverability.

### D-11 — `chore(contracts): remove or short-circuit dead branches under MAX_TOTAL_BANDITS=1`
> `_findOldestActiveBandit` and the "stale below current" upgrade refund branch (`_settleWallUpgrade` line 1139, etc.) are structurally unreachable today. Either add `// defense-in-depth: unreachable` NatSpec or replace with `assert`/`revert`.

### D-12 — `chore(contracts): align trailing-newline conventions in gen-*.mjs`
> `gen-contract-abi.mjs` writes trailing `\n`; `gen-chainclient-abi.mjs` does not. Harmless but creates noisy diffs.

---

## Findings — Skip / false positive

For completeness, six items raised by Wave-1 specialists that Wave-2 spec cross-check refuted as either intentional per spec or out-of-scope per `CLAUDE.md`. Listing here so future reviewers don't re-raise them.

| # | Wave-1 claim | Why skip |
|---|---|---|
| FP-1 | "MarketSell has no slippage protection — HIGH severity" | `clanworld_v4_spec.md` §5.9, §14.1 explicitly lock "no slippage guard in v1." |
| FP-2 | "Wood/iron/fish gathering has no tile-occupancy lock" | Spec §4.7/§4.9 model wood/iron/fish as open commons; only wheat is plotted. |
| FP-3 | "BUILDING_DURATION_TICKS = 1 is suspiciously short" | Spec §3 / §8.1: "single-tick actions" — exact match. |
| FP-4 | "Future-level reservation orphaned indefinitely if prerequisite is canceled" | v4.6 §13: "the mission remains pending until the prerequisite level lands or **the mission is invalidated**" — explicit escape hatch. |
| FP-5 | "Heartbeat is permissionless after `nextHeartbeatAtTs`" | v4.5 addendum §3.2 / CLAUDE.md §6: explicit decision, multiple keepers safe. |
| FP-6 | "Noop counts toward bandit 6-attempt cap" | Spec §6.11 / §6.12 treat "attempt or noop" as one cycle slot. |

Two additional Wave-1 items were AMBIGUOUS in spec:

- **Direct transfers credit dead recipients** — spec §11.1 "non-atomic" + §12.7 "purse gold remains bound to the dead clan and is unusable" together support the current behavior as intentional stranding. Worth an observability event (D-3 covers a related case for upgrades), but no code change required.
- **`getBanditTargetPreview` returns 0 for non-Attacking states** — spec §6.7 binds target at resolution, not at camp; advance-warning preview is not specified. No spec basis for change; if the UI needs a "likely target" hint, that's a frontend feature, not a contract bug.

---

## Cross-cutting observations

1. **ABI parity is the riskiest seam.** Three independent guards exist (`check:abi` against forge output, `gen-chainclient-abi.mjs --check` against canonical JSON, `clanWorldAbi.test.ts` field-by-field) and they all pass — but a fourth claimed guard (`check-chain-abi-parity.test.ts`) is fake (MF-3), and a fifth (runner's hand-maintained tuple) drifted (MF-1). The team should commit to the principle: **only generated code reads the ABI; nothing hand-maintained.** D-1 + MF-1 + D-2 close this.

2. **Bandit combat is the single largest spec divergence.** MF-2 collapses three Wave-1 findings into one decision point. The team has demonstrated the ADR pattern works (ADR002 ratified the 10%/+10%/80% spawn ramp); applying the same approach to §6.13/§6.15 closes this cleanly.

3. **S1 vs S2 scope discipline holds.** Most "secure default" concerns (World App guard inversion, permissionless mintClan, no slippage guard) are explicitly S2 work per CLAUDE.md §2 / §6. The codebase does not pretend they're done. SF-11 adds a one-line build-time tripwire so an S2 prod deploy can't ship the S1 default by accident.

4. **Test bar is appropriate for hackathon scope.** Per CLAUDE.md §4, only happy-path + a few error cases are required. Wave-1 surfaced ~30 coverage gaps; the most valuable to close (D-1, D-5, D-7) are listed; the rest can be deferred without risk.

5. **No real secrets, no premature MiniKit/idkit integration, no mainnet RPCs.** Build/Config review confirms the .env templates and config files are clean.

---

## Recommended next steps

In priority order:

1. **Address must-fix items in this PR before merge:**
   - Delete the duplicate ABI fields in `runnerCastHeartbeat.ts:55-56` (MF-1).
   - Decide bandit combat direction (MF-2): either reconcile to spec or write an ADR sanctioning the rebalance.
   - Delete or rewrite `check-chain-abi-parity.test.ts` (MF-3) — recommend deletion since `gen-chainclient-abi.mjs --check` already enforces real parity.

2. **Should-fix items: bundle into this PR if cheap, else open issues immediately.** SF-1 (delete duplicate cli.test.ts) and SF-10 (delete unused event) are one-line. SF-2 (`marketMode` in TS), SF-7 (initTreasury validation), SF-8 (MinimalERC20 zero-addr), SF-11 (S2 prod tripwire) are <10 lines each. The rest can be batched into a hardening PR.

3. **Defer items: open as GH issues now, link from this PR.** All 12 are scoped, each with a draft title above. A single `for issue in D-1..D-12; do gh issue create ...; done` round closes them.

4. **Build/test status before merge:** GREEN. `forge test` 292/292, vitest 178/178, `pnpm -w typecheck` and `pnpm -w build` clean, ABI parity verified.

5. **After merge:** schedule a follow-up agent in 2 weeks to verify SF-11's prod guard fired correctly during S2 setup and to triage any of D-1..D-12 that haven't been picked up.

---

## Appendix

### Wave-1 specialist scope summary

| Agent | Scope | Findings (filtered) |
|---|---|---|
| Bandits | Bandit subsystem incl. ADR002 | 15 |
| Buildings/Upgrades | Base/Monument/Walls + reservations | 15 |
| Transfers/Market/Gathering | Phase 5/6/7 economy | 15 |
| Security/Cross-cutting | reentrancy, MEV, gas, access | 20 |
| Adapters/ABI/Interface | IClanWorld → ABI → IChainClient | 15 |
| Build/Config/Docs | env, package.json, ADRs, CI | 15 |

Total raw findings: 95. After Wave-2 dedupe + verdict: 37 categorized.

### Build/test output (Wave 2)

```
forge: 1.5.1-stable
pnpm install --frozen-lockfile        → exit 0 (lockfile current)
pnpm -w typecheck (forced)            → exit 0 (11 tasks · 5m50s)
pnpm -w build                         → exit 0 (8 tasks · cache-warm)
pnpm -F @clan-world/contracts check:abi → exit 0 (jq diff empty)
pnpm -F @clan-world/contracts test     → exit 0 (22 suites · 292/292)
pnpm -F @clan-world/shared test        → exit 0 (4 files · 20/20)
pnpm -F @clan-world/runner test        → exit 0 (9 files · 121/121 +1 skip)
pnpm -F @clan-world/agents test        → exit 0 (2 files · 37/37)
```

### Worktree

The review worktree at `/home/claude/code/omnipass-world/clan-world-pr396-blind-review` is left in place so claims in this report can be verified. Remove with `git worktree remove clan-world-pr396-blind-review` when no longer needed.
