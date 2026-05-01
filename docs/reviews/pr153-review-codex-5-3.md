# PR #153 Review — Dev Branch Consolidated Sweep

| Field | Value |
|---|---|
| PR | [#153](https://github.com/OmniPass-world/clan-world/pull/153) |
| Title | Dev - DO NOT MERGE this is only for code reviews |
| Branch | `dev` -> `main` |
| Author | `mcorrig4` |
| Size | +15,321 / -553 across 125 files |
| Commits | 44 |
| Review date | 2026-04-28 |
| Review model | Codex 5.3 |
| Methodology | Wave 1: 12 parallel reviewers; Wave 2: 4 targeted deep dives; Wave 3: adjudication/dedup |

## Summary Stats

| Category | Count |
|---|---|
| MUST FIX | 3 |
| SHOULD FIX | 9 |
| DEFER (new GH issue) | 5 |
| SKIP / FALSE POSITIVE | 5 |
| Total findings | 22 |

## Triage Table

### MUST FIX — Blocking merge

| # | File | Line(s) | Finding | Category |
|---|---|---|---|---|
| 1 | `packages/runner/src/runnerCastHeartbeat.ts` | 22-48, 163-171 | `HEARTBEAT_ABI` declares a truncated `getWorldState` tuple (5 fields) that diverges from canonical `WorldState` shape; this can break decoding and heartbeat scheduling/rate-limit checks at runtime. | MUST FIX |
| 2 | `packages/runner/src/runnerCastHeartbeat.ts`, `packages/shared/src/adapters/IChainClient.ts` | 51-56, 108-126; 15-22 | Runner heartbeat client is pinned to World Chain Sepolia (`4801`) while shared chain defaults are Base Sepolia (`84532`); mismatched chain ids/RPC expectations can break tx submission or split runtime behavior. | MUST FIX |
| 3 | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/src/IClanWorld.sol` | 1408-1412; 646-650 | `PoolsSeeded` emit order is `(wood, iron, wheat, fish)` but interface declares `(wood, wheat, fish, iron)`, causing ABI-level event interpretation errors for off-chain consumers. | MUST FIX |

### SHOULD FIX — Strongly recommended in this PR

| # | File | Line(s) | Finding | Category |
|---|---|---|---|---|
| 4 | `packages/runner/src/filePeerInbox.ts` | 53-54, 83-84, 113-116 | Inbox read path uses `elderN` without applying `assertSafeInboxKey`; send path is guarded but read path is not, so malformed clan-id/env values can route reads to unintended locations. | SHOULD FIX |
| 5 | `apps/web/src/WorldMap.tsx` | 324-548 | `app.init(...).then(async ...)` has no `.catch()`, so async Pixi init failures become unhandled rejections and can leave map rendering silently broken. | SHOULD FIX |
| 6 | `apps/web/src/App.tsx`, `apps/web/src/pages/Cockpit.tsx` | 178; 153-155 | Main route renders `WorldMap` without `WorldMapBoundary`, while cockpit route wraps it. Error handling behavior is inconsistent across routes. | SHOULD FIX |
| 7 | `apps/web/src/App.tsx` | 80-96 | Verification flow has console-only failure behavior when backend verification cannot complete; users can remain locked out of map flow without actionable UI feedback. | SHOULD FIX |
| 8 | `packages/contracts/src/ClanWorld.sol` | 1345-1355 | Market token validation is gated behind treasury initialization; pre-init market actions can be accepted with invalid tokens and fail later, creating avoidable state/UX debt. | SHOULD FIX |
| 9 | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/src/IClanWorld.sol` | 1373-1399; 692-693 | `initTreasury` is a required lifecycle step but not present on `IClanWorld`; interface/NatSpec lifecycle guidance is incomplete and increases integration risk. | SHOULD FIX |
| 10 | `packages/agents/src/cli.ts` | 31-33, 117-125 | CLI `peer whisper` filename construction uses raw recipient clan id without path-segment validation, unlike runner inbox guardrails. | SHOULD FIX |
| 11 | `PR #153 metadata` | n/a | Review-only PR has no body, no explicit issue linkage, and is non-draft despite "DO NOT MERGE" title; process guardrails are weaker than intended and accidental merge risk is higher. | SHOULD FIX |
| 12 | `packages/contracts/src/ClanWorld.sol`, `packages/contracts/src/ClanWorldStub.sol` | 1389-1392; 58-61 | Pool ordering conventions differ between `ClanWorld` and `ClanWorldStub` (`wood/iron/wheat/fish` vs `wood/wheat/fish/iron`), creating script/operator misconfiguration risk. | SHOULD FIX |

### DEFER — Valid concerns tracked as new issues

| # | File | Line(s) | Finding | Category |
|---|---|---|---|---|
| 13 | `packages/runner/src/pollChainTick.ts`, `apps/server/convex/heartbeat.ts` | 16-18; 33-64 | Tick authority alignment between Convex snapshot progression and canonical on-chain tick semantics should be resolved as architecture follow-up. Tracked in [#155](https://github.com/OmniPass-world/clan-world/issues/155). | DEFER |
| 14 | `apps/web/src/components/cockpit/shared/CockpitTabBar.tsx`, `apps/web/src/components/cockpit/CockpitHeader.tsx` | 62-112; 148-211 | Cockpit accessibility improvements (tab semantics, live status announcements) are valid but can be tracked as dedicated UX/a11y follow-up. Tracked in [#156](https://github.com/OmniPass-world/clan-world/issues/156). | DEFER |
| 15 | `packages/runner/src/axlPeerInbox.ts` | 29-34, 339-343, 449-457 | AXL transport hardening items (identity binding model, peer-map invariants, durability semantics) are legitimate security hardening work beyond this pass. Tracked in [#157](https://github.com/OmniPass-world/clan-world/issues/157). | DEFER |
| 16 | `packages/runner/src/runnerCastHeartbeat.ts`, `packages/runner/test/` | 22-48, 121-171 | Runner heartbeat adapter needs direct integration-style coverage for ABI/decode and failure-path behavior. Tracked in [#159](https://github.com/OmniPass-world/clan-world/issues/159). | DEFER |
| 17 | `docs/planning/CANONICAL_SPEC.md`, `.env.template` | 19-21, 47-51; 53-100 | Canonical docs/env alignment drift (stale path references, runtime key matrix clarity) should be handled in a focused docs pass. Tracked in [#158](https://github.com/OmniPass-world/clan-world/issues/158). | DEFER |

### SKIP / FALSE POSITIVE — Not actionable as merge blockers

| # | File | Line(s) | Finding | Category |
|---|---|---|---|---|
| 18 | `packages/contracts/src/ClanWorld.sol` | 1120-1145 | Market sell "resource loss on pool revert" concern was not upheld; external self-call/try-catch flow rolls back state in revert path, so this specific loss claim is false positive. | SKIP |
| 19 | `packages/runner/src/filePeerInbox.ts` | 83-84 | Earlier "arbitrary filesystem escape via `../`" framing overstates impact; current risk is constrained misrouting under runner state layout, not confirmed full escape exploit. | SKIP |
| 20 | `apps/web/src/App.tsx`, `apps/web/src/main.tsx` | 80-83; 68-98 | "Missing `CONVEX_SITE_URL` always breaks production verify flow" is overstated; `main.tsx` already blocks normal app mount when backend URL is absent. | SKIP |
| 21 | `packages/runner/src/tickLoop.ts` | 128-131 | Treating duplicate tick delivery responses as a critical correctness defect was not sufficiently supported; this is currently an idempotency/liveness tradeoff rather than a proven blocker. | SKIP |
| 22 | `packages/contracts/abi/IClanWorld.json` | 17-108 | `pure`/`view` mutability noise in ABI artifact is tooling polish, not a demonstrated runtime regression for this PR. | SKIP |

## Wave 2 Adjudication Highlights

- Runner heartbeat issues (#1, #2) were independently validated in deep-dive review and remain the highest-risk operational blockers.
- Contracts event ordering issue (#3) was confirmed by direct interface/implementation comparison and upgraded to blocking due to ABI seam break.
- Several Wave 1 "critical" candidates were downgraded to SKIP/QUALIFIED after targeted verification (notably path escape overstatement and missing-CONVEX-url severity framing).

## Recommended Next Steps

### 1) MUST FIX first (in order)

1. Fix `RunnerCastHeartbeat` ABI parity for `getWorldState` decoding.
2. Unify heartbeat chain config with the current canonical deployment network and shared adapter defaults.
3. Correct `PoolsSeeded` emit ordering to match `IClanWorld` ABI contract.

### 2) SHOULD FIX in this PR (grouped)

- **Runner/agents hardening:** #4, #10.
- **Web reliability:** #5, #6, #7.
- **Contracts integration clarity:** #8, #9, #12.
- **PR process hygiene:** #11.

### 3) DEFER issues created

- [#155](https://github.com/OmniPass-world/clan-world/issues/155) — Tick authority alignment.
- [#156](https://github.com/OmniPass-world/clan-world/issues/156) — Cockpit accessibility follow-up.
- [#157](https://github.com/OmniPass-world/clan-world/issues/157) — Runner transport hardening.
- [#159](https://github.com/OmniPass-world/clan-world/issues/159) — Runner heartbeat adapter test hardening.
- [#158](https://github.com/OmniPass-world/clan-world/issues/158) — Docs/env alignment pass.

## Overall PR Health Assessment

**Verdict: NEEDS WORK**

PR #153 contains substantial platform progress, but the three MUST FIX items represent concrete merge-risk issues across runtime heartbeat reliability and ABI seam correctness. Once MUST FIX is resolved and SHOULD FIX set is addressed (or explicitly triaged), this review branch can serve as a significantly cleaner integration baseline for subsequent targeted PRs.
