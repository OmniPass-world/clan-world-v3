# Diamond Parity Roadmap

## Summary

This roadmap expands confidence in the diamond migration in two phases. Phase 1
adds old-core-vs-diamond event parity around order and transaction flows. Phase
2 investigates settlement parity in the places most likely to drift: reservation
accounting, winter upkeep, death cleanup, and mission completion semantics.

Keep each track small, independently testable, and committed after the focused
tests pass.

## Phase 1: Log Parity Expansion

No public ABI changes. Extend `DiamondEventParity.t.sol` using the existing
core-vs-diamond log fingerprint harness.

- Track 1A: Gather and resource movement logs.
  - Add parity tests for one gather action, `DepositResources`, and
    `WithdrawResources`.
  - Validate the emitted sequence for the settlement heartbeat matches between
    the old monolith and diamond.
  - Run `forge test --match-path test/diamond/DiamondEventParity.t.sol`.
- Track 1B: Upgrade logs.
  - Add parity tests for successful `UpgradeWall`, `UpgradeBase`, and
    `UpgradeMonument`.
  - Validate submit-time mission logs and completion-time level-change logs.
  - Run the event parity test plus the wall/base/monument upgrade suites.
- Track 1C: Scheduled market logs.
  - Add parity tests for scheduled market commit, scheduled success, scheduled
    failure, and stale nonce skip.
  - Compare the heartbeat log sequence for the tick that executes market work.
  - Run the event parity test plus focused market tests.
- Track 1D: Remaining simple transaction logs.
  - Add parity for `transferVaultResource`, `transferBlueprint`,
    `transferBundle`, and `transferClanOwnership`.
  - Heartbeat both systems before transfers that would otherwise emit extra
    lazy-settle logs.
  - Run the event parity test and `DiamondSkeleton.t.sol`.

## Phase 2: Settlement Parity Investigation

Use focused old-core-vs-diamond tests. If a mismatch appears, classify it as
intentional spec drift, harness setup error, or diamond bug before patching.

- Track 2A: Reservation accounting during lazy settlement.
  - Cover wall/base/monument reservation clearing inside the same settlement
    window.
  - Assert final balances, observable reservation behavior, mission state, and
    logs match.
  - Run `ReservationConsistency.t.sol`, upgrade suites, and diamond parity
    tests.
- Track 2B: Cold/starvation multi-tick settlement.
  - Compare old core and diamond over winter upkeep, wood shortage, wall
    degradation, starvation toggles, and cold deaths.
  - Include one surviving case and one death case.
  - Run targeted winter/season tests and `SeasonFinalization.t.sol`.
- Track 2C: Defender cleanup on death.
  - Test clansman death while defending and clan death while being defended.
  - Assert defender registries, active missions, and emitted cleanup/bandit
    target logs match expected behavior.
  - Run `BanditAttackResolution.t.sol`, `DiamondSkeleton.t.sol`, and event
    parity if logs are covered there.
- Track 2D: Settlement cooldown and mission completion semantics.
  - Confirm natural mission completion does not reset cooldown, retask after
    cooldown behaves the same, and stale missions do not emit duplicate
    completion logs.
  - Run the event parity test and `DiamondSkeleton.t.sol`.

## Validation And Commit Rules

- After every track, run the smallest relevant `forge test --match-path ...`
  command and `pnpm --dir packages/contracts check:sizes`.
- Add tests to `.github/workflows/contracts.yml` only if they are fast and
  protect migration-critical invariants.
- Commit each passing track separately.
- If code fixes are needed, commit tests and fixes together only when the test
  directly proves the fix.

## Assumptions

- `ClanWorld.sol` remains available as the migration oracle until parity is
  complete.
- The diamond is the release target; parity tests are migration confidence tests,
  not a reason to keep deploying the monolith.
- No public API, selector, or ABI changes are expected for these phases.
- Keep coverage focused on high-risk happy paths and critical drift cases, not
  exhaustive enum coverage.
