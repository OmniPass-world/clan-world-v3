# PR 199 Review — Codex 5.3 (Blind Swarm)

- PR: [#199 — Phase 8 — Buildings + Progression](https://github.com/OmniPass-world/clan-world/pull/199)
- Date: 2026-04-30 (ET)
- Model: Codex 5.3
- Method: blind multi-wave swarm review in isolated linked worktree (`review-pr-199-blind-r4`), with explicit denylist on `docs/reviews/**` for subagents.

## Scope

- Files reviewed: PR changed files only (15 files across `packages/contracts`, `packages/shared`, `packages/runner`).
- Validation done:
  - Parallel Wave 1 specialist swarm (9 roles).
  - Wave 2 deep validation on high-impact findings.
  - Targeted commands in review worktree:
    - `forge test --match-path test/RankGetters.t.sol` (pass)
    - `forge test --match-test simHidesRefundedReservationFromLaterRetry` (pass)
    - `forge test --match-path test/UpgradeReservationSwitches.t.sol` (pass)
    - `pnpm --filter @clan-world/contracts run check:abi` (fails; only `id` field drift in full JSON artifact diff).

## Consolidated Triage

| # | File | Line | Finding | Category |
|---|---|---|---|---|
| 1 | `packages/contracts/package.json` | `7` | New `check:abi` compares full Forge artifact JSON (`out/.../IClanWorld.json`) against committed `abi/IClanWorld.json`; on fresh run this fails due non-semantic `id` drift, causing `pnpm test` (which calls `check:abi` first) to fail even when ABI content is otherwise aligned. | **MUST FIX** |
| 2 | `packages/contracts/src/ClanWorld.sol` | `463-478`, `1002-1012` | Simulation parity bug: `_applyUpkeep` uses reservation-aware spendable wheat (`_spendableAfterReleasing`), while `_simulateApplyUpkeep` uses raw `vaultWheat`; ranking/score/settled-quote view paths can diverge from real settlement when wheat is reserved for upgrades. | **SHOULD FIX** |
| 3 | `packages/contracts/src/IClanWorld.sol` | `536-544` | `ResourcesDeposited` signature changed (`uint64 atTick` -> `uint32 tick`, plus renamed fields). Internally consistent in this PR, but this is a breaking event signature for downstream consumers; PR should include explicit compatibility/migration note and consumer audit. | **SHOULD FIX** |
| 4 | `packages/contracts/test/UpgradeReservationSwitches.t.sol` | `51-62` | Cross-type reservation switch tests assert status only (`OK`) and do not verify post-settlement resource deltas/pending counters/level transitions. Valid gap, but non-blocking under hackathon minimal-test policy. | **DEFER** |
| 5 | `packages/contracts/test/BaseUpgrades.t.sol`, `packages/contracts/test/MonumentUpgrades.t.sol` | file-level | Wall upgrades have richer edge-case coverage (cancel/death/ordering parity) than base/monument. Valid quality gap, but deferrable. | **DEFER** |
| 6 | `packages/shared/src/adapters/IChainClient.ts` | file-level (non-hunk debt) | Hand-maintained minimal ABI is still a drift-prone pattern (PR updated parts of it correctly, but process debt remains). Track as follow-up debt, not a PR blocker. | **DEFER** |
| 7 | `packages/contracts/src/ClanWorld.sol` | `797-801` | Claim: off-homebase `_doBuilding` branch can complete mission without refund and leak reservations. Deep validation found this unreachable in normal upgrade lifecycle due order validation + mission target/base invariants. | **SKIP / FALSE POSITIVE** |
| 8 | `packages/contracts/src/IClanWorld.sol` | `130-146` | Claim: `ActionType` ordinal drift from mid-enum insertion. False: `UpgradeWall` is appended, not inserted, so prior ordinals are preserved. | **SKIP / FALSE POSITIVE** |
| 9 | `packages/contracts/src/ClanWorld.sol` | `3045-3063` vs `2925-2964` | Claim: snapshot vs ranking mismatch is a bug. Current behavior is explicitly documented as intentional (`getWorldSnapshot` last-settled; `getRankings` simulated). Treat as product semantics, not regression. | **SKIP / FALSE POSITIVE** |
| 10 | `packages/contracts/src/IClanWorld.sol` | `47` (constant not modified in this PR) | Claim: heartbeat interval mismatch vs architecture docs. Valid docs/config concern but not introduced by PR 199 diff. | **SKIP / FALSE POSITIVE** |

## Summary Stats

- Total triaged findings: **10**
- **MUST FIX:** 1
- **SHOULD FIX:** 2
- **DEFER:** 3
- **SKIP / FALSE POSITIVE:** 4

## Recommended Next Steps

1. Address the **MUST FIX** first:
   - Stabilize `check:abi` to compare deterministic ABI payloads only (e.g. normalize and diff `.abi` arrays / strip volatile fields like top-level artifact `id`), so `pnpm test` is reproducible.
2. Address **SHOULD FIX** items in this PR if possible:
   - Align `_simulateApplyUpkeep` with reservation-aware wheat logic used by `_applyUpkeep`.
   - Add explicit PR note for `ResourcesDeposited` signature change and confirm downstream indexers/consumers have regenerated ABI/topics.
3. Defer test/debt items into follow-up issues (titles below) if timeline is tight.
4. Re-run focused validation after fixes:
   - `pnpm --filter @clan-world/contracts run check:abi`
   - `forge test --match-path test/RankGetters.t.sol`
   - targeted tests for reservation/upkeep parity.

## Proposed Follow-up Issues (for DEFER)

1. **Strengthen cross-type upgrade switch tests with post-settle invariants**
   - Add assertions for vault deltas, pending reservation counters, and final level outcomes after replacement.
2. **Add Base/Monument edge-case parity tests matching Wall upgrade coverage**
   - Mirror high-value cases: cancellation, death interrupts, ordering conflicts, sim/real parity checks.
3. **Automate shared minimal ABI generation or drift checks for `IChainClient`**
   - Reduce manual tuple-maintenance risk by deriving minimal ABI slices from canonical contract artifacts.

## Overall PR Health

**Needs work before merge** due one workflow-blocking issue (`check:abi` reliability).  
With that fixed, remaining items are mostly correctness hardening and compatibility clarity.
