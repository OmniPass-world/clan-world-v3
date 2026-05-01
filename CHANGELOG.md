# Changelog

All notable changes to ClanWorld are documented in this file.

Format follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/).

---

## [1.0.0] — 2026-05-01

### Highlights

- Full on-chain game engine: 10 contract phases covering gathering, markets, buildings, bandits, winter, and clan death — 310/310 Forge tests green at ship
- Four AI Elder agents run autonomously on Base Sepolia, each submitting real transactions via `RealChainClient` on every heartbeat tick
- Resource reservation invariant enforced: `WithdrawResources` and all OTC transfer paths are now reservation-aware, closing a class of vault-drain exploits found during pre-release audit
- ABI drift is now structurally impossible: generated `CLAN_WORLD_ABI` replaces every hand-rolled tuple; `gen-enums.mjs` and `gen-constants.mjs` codegen scripts keep TypeScript in sync with Solidity
- Pixi.js canvas world map with 8 regions, isometric base sprites at five upgrade levels, clansman walking animations, speech bubbles, pinch-to-zoom, and a live scoreboard
- World ID humanity verification at clan mint via MiniKit + IDKit integration
- Convex real-time backend with heartbeat webhook, safety-net cron, and mock-mode for offline development
- ABI parity test wired into CI — contract shape drift fails the build automatically

> [!NOTE]
> **The Whole Game Shipped:**
> 1. **10 contract phases** end-to-end — gathering, markets, OTC transfers, building upgrades, bandits, winter, clan death
> 2. **4 autonomous AI Elders** on Base Sepolia via `RealChainClient` — *real on-chain transactions* every heartbeat
> 3. **Pixi.js canvas world map** — 8 regions, isometric bases at 5 upgrade levels, walking clansmen, speech bubbles, pinch-to-zoom
> 4. **World ID humanity verification** at clan mint via MiniKit + IDKit
> 5. **Convex real-time backend** — `getSnapshot`, heartbeat webhook, safety-net cron, `MOCK_MODE` for offline dev
> 6. **Permissionless heartbeat + lazy settlement** — anyone can fire ticks, clans settle on touch
> 7. **Carry-based market trades** — workers *physically haul* resources to and from market (no teleport)
> 8. **`gen-chainclient-abi` + `gen-enums` + `gen-constants`** — full TS-from-Solidity codegen pipeline
>
> Together these turn ClanWorld from a flat resource-loop into a **living strategic world**: AI Elders making real-money decisions on a public chain, a watchable canvas where every tick is *their* tick, and an engine that scales to new actions without rewriting the off-chain glue every time.

#### Contract engine (Phases 1–10)

- Phase 1: `ClanWorld.sol` real engine — `mintClan`, order submission, heartbeat, lazy settlement core (#79, #98)
- Phase 3: mission assignment + lazy settlement engine — `submitOrders`, `defend_base`, mission timing rules (#176, #177, #178, #179, #180, #181)
- Phase 4: permissionless heartbeat, domain-separated RNG helpers, winter/season timers, heartbeat ordering fix (#173, #174, #175, #182, #183, #239)
- Phase 5: wood gathering, deposit action, per-tick yield, starvation next-tick, wood carry cap, `ResourcesDeposited` event ordering (#188, #190, #234, #298, #371)
- Phase 6: resource boundary tokens + treasury seeder, seed pools, immediate and scheduled market actions, carry-based market trades, market failure semantics, market events surface, `StatusCode` enum stability, ABI `uint64` revert (#228, #240, #257, #260, #263, #262, #284, #324, #298)
- Phase 7: gold transfer OTC, vault resource transfer OTC, blueprint transfer OTC, bundled transfer convenience, OTC expiry restriction for dead clans, direct transfers replace OTC model, `transferClanOwnership` (#243, #246, #248, #252, #256, #389, #397)
- Phase 8: wall upgrades, base upgrades, monument upgrades, score and rank getters, upgrade reservation coverage, dead internal function cleanup, `MAX_CLAN_SCAN_FOR_RANKING` derivation (#236, #238, #242, #251, #360, #361, #364)
- Phase 9: bandit troop state machine, bandit spawn chance logic, eager-settle scope, deterministic attack resolution, defender reward split, blueprint reward on successful defense, cleanup on bandit target death, vault loot theft + rampage path + WAITING-at-home defense (#189, #191, #244, #247, #253, #255, #258, #374)
- Phase 10: winter schedule, winter upkeep, cold damage, crop winter transitions, clan death, starvation and cold-reset semantics (#235, #237, #241, #245, #249, #289, #293, #383, #363)
- Phase 1 view-only settlement simulation for derived getters (#261)
- `ERR_MUST_SETTLE_FIRST` + `winterStartsAtTick` initialization fix (#259)

#### Agents and orchestrator

- Elder CLI full subcommand coverage — status, orders, submit (#71)
- Elder clan `submitOrders` with real on-chain transactions via `RealChainClient` (#32)
- Elder harness in-repo with `make install` — sandboxed Claude Code agent per elder (#154)
- Orchestrator `REGION_FOREST` routing + `submitOrders` sim semantics (#383)
- `ActionType` enum replaces bare numeric literal `action: 1` in orchestrator (#409)

#### Shared / infrastructure

- `RealChainClient` with viem — full typed on-chain interface (#27)
- `IChainClient` adapter interface + codegen pipeline (#362, #385)
- Cross-phase hygiene bundle: stub heartbeat parity, ABI parity broadening, `IChainClient` codegen (#362, #385)

#### Web app

- Pixi.js canvas shell — 8 regions, clan flags, speech bubbles (#19)
- Convex `agentLogs` speech bubbles on canvas (#33)
- MiniKit + IDKit clan-join + World ID verify endpoint (#34)
- Visual rework — isometric base sprites, region zones, floating level labels, fullscreen mode (#52, #161)
- Clansman walking sprites replace worker dots (#59)
- Speech bubble polish — clan-colored Elder header, backdrop, tail, fade (#43, #55, #99)
- Pinch-to-zoom via `pixi-viewport` — multi-touch + Pixi v8 EventSystem fix (#50, #51, #53)
- Bubble tails pointing to source, world notice panel, live tick counter (#54)
- Demo bypass env (`VITE_DEMO_BYPASS_WORLD_GUARD`) for offline/demo recording (#37)
- Graceful render fallback (#35)
- Worker travel dot animation along routes (#45)
- Monument visual + wall opacity by building level (#44)

#### Landing page and docs

- `clan-world.com` landing page — full copy, palette, tale frames, sponsor logos (#30, #48)
- Hackathon judge quick-start banner and submission video embed (#61, #62)
- README polish — hero copy, tech stack, sponsor framing (#31)

#### Server / backend

- Convex MOCK_MODE backend — `getSnapshot` + `agentLogs` (#20)
- Convex heartbeat-webhook HTTP action + safety-net cron (#25)
- Foundry `Heartbeat` script + `start-heartbeat-loop.sh` (#29)

#### Tooling and codegen

- `gen-chainclient-abi.mjs` — allowlist-driven ABI extraction to TypeScript (#385)
- `gen-enums.mjs` — regex-parses `IClanWorld.sol` for all 8 contract enums, outputs `as const` lookup tables (#409)
- `gen-constants.mjs` — `ClanWorldConstants.sol` → TypeScript `bigint` exports (#409)
- `check-chain-abi-parity.mjs` extended to cover all major struct getters; wired into `pnpm test` (#409)
- Playwright e2e harness for `apps/web` (#88)
- Elder vitest suite for CLI subcommands + regression coverage (#105)
- Vite dev servers default to `port-for` slots (#139)
- Post-bundle-A dev-tooling follow-ups (#140)

> [!NOTE]
> **Critical Pre-Release Saves:**
> 1. **Reservation-bypass closed** in `WithdrawResources` and all 4 Phase 7 OTC transfer paths — entire class of *vault-drain exploits* caught and fixed (#394, #395)
> 2. **`transferClanOwnership` dead-clan guard** — was allowed on dead clans, now settles-then-dead-checks (#397)
> 3. **Phase 4 heartbeat ordering bug** — HIGH spec divergence between heartbeat and lazy paths fixed (#239)
> 4. **`StatusCode` + `ActionType` enum stability** locked by Solidity tests (#324, #295)
> 5. **Phase 6 market** — wheelbarrow vault-carry semantics + sell validation + scheduled-execution observability (#294)
> 6. **5 HIGH bandit findings** from super-swarm review fixed in one round (#266)
> 7. **8 HIGH building findings** from Phase 8 super-swarm review (#291)
> 8. **Phase 10 cold-reset regression** + super-swarm HIGHs (#289, #293)
>
> The vault-drain exploits would have **silently broken upgrade economics** in production — a clan could queue a wall upgrade, transfer the reserved wood to a sibling clan, and watch the upgrade fail at settle while the wood was already gone. Caught by a 4-reviewer cross-validation sweep on PR #396 *before* tagging v1.0.0.

#### Pre-release reservation bypass (Tier A — critical)

- `WithdrawResources` bypassed Phase 8 upgrade reservations — a clansman could drain wheat, wood, or iron that an active upgrade had already reserved (#394)
- Phase 7 OTC transfer functions (`transferVaultResource`, `transferBundle`, `transferBlueprint`) were reservation-blind, allowing reserved resources to be transferred out before settlement (#395)

#### Contract correctness

- Phase 4 heartbeat ordering (HIGH spec drift) (#239)
- Phase 5 `ResourcesDeposited` event order + tick + four medium fixes (#234)
- Phase 6 cloud-review fix-round (#270)
- Phase 6 R3 wheelbarrow vault-carry + sell validation (#294)
- Phase 6 R4 `ActionType` enum stability + `MarketBuy` error + `uintValue` robustness (#295)
- Phase 6 R5 `StatusCode` enum stability (#324)
- Phase 7 R3 stale OTC + `expiryTick uint64` + cap reap + access cleanup (#292)
- Phase 8 R4 eight HIGHs from super-swarm review (#291)
- Phase 8 R5 sim/real `fromLevel` parity + ABI pretty-print (#296)
- Phase 9 super-swarm R2 — five HIGH findings (#266)
- Phase 9 cloud-review fix-round (#265)
- Phase 10 super-swarm R2 fixes (#287)
- Phase 10 R3 cold-reset regression + cloud findings (#289)
- Phase 10 R4 three super-swarm HIGHs + cleanups (#293)
- Phase 8 dev-merge test regressions — winter init + assertion alignment (#391)
- Phase 10 dev-merge follow-ups — dead constant + sim/winter parity (#393)
- Phase 3 integration fixes from orch-r1 review (#181)
- PR #153 review must-fix/should-fix slices across contracts, runner, web, and agents (#170, #171, #172)
- `transferClanOwnership` allowed on DEAD clans — settle-then-DEAD-check guard added (S-1 fix, #397)
- Codegen allowlist missing the five new Phase 7 transfer functions (S-2 fix, #397)
- `transferClanOwnership` missing from ABI + `IChainClient` (S-3 fix, #397)
- Phase 5 ABI `uint64` revert + per-tick yield migration + `ERR_NOT_AT_HOMEBASE` (#298)

#### Web fixes

- Pinch-zoom — override Pixi v8 `EventSystem` `touchAction='none'` (#51)
- Bubble tails point to source, world notice panel, tick counter live (#54)
- Demo bypass also skips IDKit verified gate (#39)
- Landing factual corrections — clan count + winter cadence (#36)
- Graceful render fallback on canvas load failure (#35)

#### Runner / agents

- Elder sandbox tightened; live config improvements mirrored in-repo (#160)

### Hardening pre-release (post-merge audit, 2026-05-01)

> [!NOTE]
> **Audit-Driven Cleanup of the Release Branch:**
> 1. **`HEARTBEAT_ABI` duplicate fields deleted** — silent runtime decode bug caught by *8 of 8 reviewers* (#407)
> 2. **`HEARTBEAT_ABI` fully replaced with generated import** — drift hazard *structurally eliminated* (#408)
> 3. **`gen-enums.mjs` + `gen-constants.mjs` shipped** — TypeScript now generated from Solidity for enums + constants (#409)
> 4. **Parity test refactored** — encoder side now reads canonical `IClanWorld.json` instead of hand-rolled fixture (#409)
> 5. **`anyApi` casts replaced** with generated Convex API types in `IConvexClient.ts` + `useAgentLogs.ts` (#409)
> 6. **`marketMode` field added** to TS `SubmitOrderResult` to match on-chain 5-field struct (#407)
> 7. **Stub `getDerivedClanState` clanId fix** — multi-clan callers were getting clan 0's data (#407)
>
> A parallel codex+claude audit asked the question *"are there other places like the HEARTBEAT_ABI bug?"* — answer was yes, and they're all gone now. **Hand-rolled type mirrors are no longer a viable shortcut** in this codebase: the codegen pipeline + parity check covers every drift surface that matters.

These PRs all targeted reviewer findings on the `dev-merge` release gate (#396) after an 8–11 reviewer superswarm pass:

- **PR #407** — Tier B 6-item bundle:
  - Delete duplicate `currentSeasonNumber` / `nextHeartbeatAtTick` fields from runner `HEARTBEAT_ABI` tuple (caused silent runtime decode bug; flagged by 8/8 reviewers)
  - Add `marketMode` field to `SubmitOrderResult` TypeScript interface to match on-chain 5-field struct
  - Delete fake parity test that compared two literal fixtures to each other (self-tautology)
  - Delete duplicate `cli.test.ts` (canonical copy lived elsewhere)
  - Fix stub `clanId` assignment in simulation path
  - Wire `WithdrawResources` branch into simulation settlement

- **PR #408** — Replace hand-rolled `HEARTBEAT_ABI` viem tuple in runner with import of generated `CLAN_WORLD_ABI`; add `heartbeat` to codegen allowlist (audit MUST 1/3)

- **PR #409** — Phase 2 of hand-coded types audit:
  - New `gen-enums.mjs` generates TypeScript enum lookup tables from Solidity source; replaces `action: 1` literal with `ActionType.ChopWood` (audit MUST 2/3)
  - Parity test refactored to use `getAbiItem` from canonical `IClanWorld.json` — test now verifies generated vs. canonical shapes, not fixture vs. fixture (audit MUST 3/3)
  - New `gen-constants.mjs` for Solidity constants → TypeScript `bigint` exports
  - `anyApi` casts in `IConvexClient.ts` and `useAgentLogs.ts` replaced with generated Convex API types
  - Heartbeat-interval constant unified across runner and contract

> [!NOTE]
> **Cleaner Surfaces:**
> 1. **Phase 7 OTC strip-out** — OTC order type replaced with 5 *direct transfer functions* (#389)
> 2. **Base Sepolia chain pivot** — replaces earlier World Chain Sepolia config (#132)
> 3. **`*Upgraded` events dropped**, `*LevelChanged` kept — cleaner event surface (#365)
> 4. **`MAX_CLAN_SCAN_FOR_RANKING` derived** from `MAX_CLANS` instead of hardcoded (#360)
> 5. **Carry-based market trades** — workers haul resources, no teleport (#284)
> 6. **Orchestrator enum literals** — `action: 1` becomes `ActionType.ChopWood` (#409)
> 7. **4 dead internal contract functions deleted** (#361)
>
> The OTC strip-out and the chain pivot were the two big *spec-vs-impl* alignments — once they landed, every downstream phase had a *consistent* substrate to build on instead of routing around legacy assumptions.

- Drop `*Upgraded` events, keep `*LevelChanged` — cleaner event surface (#365)
- Delete four dead internal contract functions (#361)
- `MAX_CLAN_SCAN_FOR_RANKING` derived from `MAX_CLANS` constant rather than hardcoded (#360)
- Base Sepolia chain pivot — replaces earlier World Chain Sepolia config (bundle B, #132)
- Phase 7 OTC strip-out — replaces OTC order type with direct transfer functions (#389)
- Phase 6B market spec cleanup — seed ratios, `executeAtTick`, slippage alignment (#380, #357)
- Phase 6B carry-based market trades — workers carry resources to market rather than teleporting (#284)
- Orchestrator action literals replaced with `ActionType` enum (#409)

> [!NOTE]
> **Validation Footprint At Ship:**
> 1. **310/310 Forge tests green** at release HEAD
> 2. **WithdrawResources exploit test** + wood/iron/fish/surplus-ok variants (#394)
> 3. **Phase 7 transfer reservation tests** (#395)
> 4. **Heartbeat + `getRankings` gas profiling** (#359)
> 5. **ABI parity test wired into CI** — reads canonical `IClanWorld.json` (#409)
> 6. **Playwright e2e harness** for `apps/web` (#88)
> 7. **Phase 3 Foundry spec** — *39 cases* (#115)
> 8. **Elder vitest CLI suite** + issue #94 regression (#105)
>
> Every reservation-bypass exploit and every cross-tier integration shape has a *named test* — regressions can't sneak back in. CI now fails the build the moment the contract ABI drifts from the TypeScript adapter, which means **future drift is a compile-time problem, not a production incident**.

- Heartbeat + `getRankings` gas profiling (#359)
- Upgrade reservation coverage strengthened (#364)
- Phase 3 Foundry test specification — 39 cases (#115)
- Elder vitest suite for CLI subcommands + issue #94 regression (#105)
- Playwright e2e harness (#88)
- WithdrawResources exploit test + wood/iron/fish/surplus-ok cases added — 310/310 Forge tests at release (#394)
- Phase 7 transfer reservation tests (#395)
- `transferClanOwnership` dead-clan revert test (#397)
- ABI parity test refactored to canonical-derived shapes, wired into CI (#409)

> [!NOTE]
> **Spec + Planning Artifacts Shipped:**
> 1. **`CANONICAL_SPEC.md`** with precedence + conflict resolutions (#70)
> 2. **v4.1–v4.5 engine spec copies** (#70)
> 3. **v4.6 Phase 5 economy alignment addendum** (#356)
> 4. **Phase 8B v4.6 buildings alignment addendum** (#355)
> 5. **Phase 10 spec-compliance UAT review** (#345)
> 6. **Phase 3 Foundry test specification** (#115)
> 7. **Hackathon coding rules** — minimal tests + env var simplicity (#18)
>
> The spec evolved through 5 named versions during the build — `CANONICAL_SPEC` is the *current source of truth* for every conflict, and the alignment addenda capture *exactly* what changed between versions and *why*. Future agents reading the docs will know which version supersedes which.

- `CANONICAL_SPEC`, `DEMO_DRIFT`, v4.1–v4.5 engine spec copies (#70)
- Phase 3 Foundry test specification (#115)
- v4.6 Phase 5 economy alignment addendum (#356)
- Phase 8B v4.6 buildings alignment addendum (#355)
- Phase 10 spec-compliance UAT review (#345)
- Hackathon coding rules — minimal tests + env var simplicity (#18)

---

[1.0.0]: https://github.com/OmniPass-world/clan-world/compare/world-build-submission-1...release/v1.0.0
