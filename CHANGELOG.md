# Changelog

All notable changes to ClanWorld are documented in this file.

Format follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/).

---

## [2.1.1] — 2026-05-03

Demo operations patch for the live Base Sepolia world.

### Added

- **Diamond demo config controls**: `HeartbeatConfigFacet` now exposes owner-only `setClansmanCooldownSeconds(uint64)` for rapid manual testing and owner-only `triggerBanditSpawn()` to arm a one-shot forced bandit spawn on the next heartbeat.
- **Bandit trigger runbook notes**: Base Sepolia deployment runbook documents the 1-second cooldown setting and one-shot forced bandit spawn command.

### Changed

- Bumped all ClanWorld workspace package versions to `2.1.1`.

---

## [2.1.0] — 2026-05-03

Pre-demo feature drop. iNFT demo wiring, AXL transport, 0G storage scaffolding, graphics polish, and the full pipeline from the OpenAgents Track 2 submission scope.

### Added

- **ERC-7857 iNFT demo flow** (PR #494): `ClanAgentNFT` contract + `Mock7857Verifier` + Foundry deploy/mint/transfer scripts, Convex `inft.ts` mirror module (auth-gated by `INDEXER_SECRET`), `OwnerEditor` + cockpit `ZeroGTab` UI for mint/transfer/edit. Includes `safeTransferFrom` with `IERC721Receiver` callback, `transferProof.newDataHash` validation, per-item `IntelligentDataItem` event for full URI list reconstruction, and `waitForTransactionReceipt` between `writeContract` and UI refresh.
- **0G mainnet smoke test scaffolding** (`infra/0g/`, PR #494): `smoke-test.ts` exercises `ZeroGMemoryStore.save/recall` for all 4 elders, `setup-env.sh` derives env from `~/.secrets/clanworld-elder-wallets.json`, README documents operator runbook + cost model. Smoke test currently fails on mainnet `FLOW_CONTRACT` submit despite verified-correct address — open environmental issue, file fallback ready as demo path.
- **Gensyn AXL Docker sidecar** (PR #493): `infra/axl/` — `Dockerfile` (Go 1.25 builder + alpine runtime), `docker-compose.yml` (peered axl-1/axl-2 nodes on mutual TLS), `setup.sh` (peer-ID registration for clans 1-4), `test-whisper.sh` (end-to-end `elder whisper send/recv` over real AXL transport, validates `AxlPeerInbox` path not `FilePeerInbox` fallback).
- **8 new clan base sprite themes** (PR #491): 5-level progressions for `cobalt-keep` (knights), `bone-standard` (warlords), `gilded-hold` (merchants), `tide-wardens` (fishers), plus `pale-cathedral`, `amethyst-spire`, `black-forge`, `verdant-grove` shipped as ready-to-wire assets in `apps/web/public/bases/`. 4 active clans now reskinned via `MOCK_CLANS.basePng` swap; sprites scaled 30% from initial render for tile-proportional fit.
- **Live event ticker, top HUD bar, pixel burst effects** (PR #489): `EventTicker.tsx` streams chain events with clan-color coding, `TopHud.tsx` shows live tick + season progress + winter indicator + bandit countdown chip, `WorldMap` agent-log → pixel burst lifecycle. Demo cockpit feels alive instead of static.
- **`getSnapshot` exposes season/winter state** (PR #489): pure `deriveSeasonState(tick)` mirroring `LibSeason.sol` semantics — no chain or schema change needed; `seasonStartTick` / `seasonEndTick` / `winterActive` / `winterStartsAtTick` available client-side.
- **Diamond winter boundary tests** (PRs #472, #473, #474): `DiamondWinterBoundary.t.sol` covers winter-start parity, winter-end parity, and the `MAX_CROP_TRANSITION_PER_TICK` stress path against the diamond.
- **Expanded README** (PR #490): 86 → 215 lines. Game mechanics (regions, missions, wheelbarrows, vault, trading, bandits, winter, seasons, monument), agent architecture (Four Ælders, Elder CLI, Memory & iNFT, Communication channels), tech-stack table, beyond-the-game pitch.
- **AGENTS.md World out-of-scope banner** (PR #495): one-line sticky directing all agents to ignore WorldChain / WorldMiniApp / MiniKit / World ID references — Submission 2 only.

### Changed

- **`OG_STORAGE_API_KEY` → `OG_STORAGE_ENABLED`** (PR #492). The var is a feature flag, not a credential — real auth comes from `ELDER_MNEMONIC`-derived wallets. Misleading legacy name removed across `.env.template`, runner code, README, and 54 tests. Per-clan KV stream IDs (`OG_STREAM_ID_CLAN_<id>`) and per-elder peer-ID env vars (`AXL_PEER_ID_1..4`) added alongside.
- **iNFT identity plane env block added to `.env.template`**: `OG_INFT_ADDRESS`, `INFT_OWNER`, `INFT_NEW_OWNER`, `INFT_TOKEN_ID`, `INFT_METADATA_URI`, `INFT_TRANSFER_URI`, plus `VITE_OG_*` and `VITE_OWNER_EDITOR_ENABLED` for the cockpit owner-editor route.
- **Convex mirror mutations gated by `INDEXER_SECRET`**: all four `mirrorToken` / `mirrorTransfer` / `mirrorMemoryEntry` / `mirrorBulletin` mutations now require the secret arg matching the deployment env var, fail-closed when env unset.

### Fixed

- **`OwnerEditor` stale-state on RPC failure** (PR #494): unminted-tokenId loads now reset to canonical demo state instead of leaving the prior token's owner/data on screen.
- **`OwnerEditor` no longer optimistic-updates ahead of chain**: `setData` + `persistDemoState` only run after `loadToken()` re-fetches a confirmed receipt — rejected wallet prompts can no longer leave the cockpit lying about post-update state.
- **`safeNum` zero-string handling** (PR #489): `Number(v) || fallback` was treating valid `"0"` as falsy. Replaced with `Number.isFinite` check; `wood=0` / `resourceIn=0` events now render correctly.
- **Runner `txHash` surfaced on successful 0G saves**: one-line `console.log` in `ZeroGMemoryStore.save()` exposes the post-submit txHash + rootHash for ops visibility.

### Notes

- All 6 PR #494 review HIGHs (4 contract/Convex + 2 UI) addressed in 3 review rounds (orch inline + parallel opus-4-7 + codex-5-5 file-pointer dispatch). Reviews live in `docs/reviews/pr494-codereview-*.md`.
- 0G mainnet smoke test FLOW_CONTRACT issue documented in PR #494 body — likely an SDK 0.3.3 estimateGas quirk or unsatisfied Market contract permission gate. File fallback works; testnet path mapped if mainnet remains blocked.

---

## [2.0.2] — 2026-05-03

### Fixed

- **Diamond season finalization init guard** (PR #475): `FinalizeSeasonFacet.finalizeSeason()` now requires initialized app storage before it can run, preventing a public deploy race where `finalizeSeason()` could be called after the facet selector was installed but before `ClanWorldDiamondInit.init()` executed.
- **Diamond init season flag reset**: `ClanWorldDiamondInit.init()` explicitly sets `seasonFinalized = false`, so a newly initialized world cannot inherit poisoned pre-init season state.

### Added

- **Pre-init finalization regression coverage**: `testDiamondFinalizeSeasonBeforeInitReverts()` installs the season facet without running init and asserts `finalizeSeason()` reverts.
- **GPT-5.5 Pro PR 468 follow-up triage doc**: `docs/reviews/pr468-gpt-5-5-pro-followup.md` records the stale-but-useful review, the immediate fix, and linked post-demo hardening issues.

### Changed

- Bumped the root package and ClanWorld workspace package versions to `2.0.2`.

---

## [2.0.1] — 2026-05-02

### Fixed

- **Dead-target cleanup helpers consolidated in `LibBanditCombat`** (PR #469). `releaseDefendersForDeadTarget` + `abortBanditAttacksForDeadTarget` were literally duplicated between `LibBanditCombat` (`public`) and `LibSettlement` (`internal`) after the round-1 SuperSwarm `markClanDead` parity fix. Both opus 4.6 + opus 4.7 r2 reviews flagged the silent-divergence risk: any future change to one copy without the other would re-create the round-1 parity break. Canonical copy now lives in `LibBanditCombat`; `LibSettlement` calls into it. Both functions changed from `public` to `internal` — gets inlined into callers, saves ~700 gas per call vs DELEGATECALL (also addresses opus 4.6 / opus 4.7 r2 MEDIUM about library function visibility). All 58 diamond parity tests pass.

### Still queued for future patch releases

The remaining v2.0.1-target items from the v2.0.0 changelog (lazy-settlement clan death event-emission parity, `_settleClan` 6× duplication, 41 library functions `public→internal` sweep, storage layout field-offset snapshot, `bac7c6a` write-then-overwrite refactor, `MAX_CROP_TRANSITION_PER_TICK` access-modifier parity, `LibDiamond.setContractOwner` zero-address guard) ship in subsequent patch releases.

---

## [2.0.0] — 2026-05-02

### Highlights

> [!IMPORTANT]
> **Diamond proxy migration — major architecture change.** The monolithic `ClanWorld.sol` engine (~3,500 lines, hitting EIP-170 bytecode limit) is replaced by an EIP-2535 Diamond proxy with 24 facets sharing a single `LibStorage.appStorage()` slot. The 52 `IClanWorld` selectors are preserved bit-for-bit — game logic, events, and ABI are identical from a consumer's perspective. The on-chain deploy address changes; clients hardcoding the v1.x contract address must redeploy. PR #468.
>
> *v1.x = monolith era. v2.x = diamond era. ClanWorld is pre-prod with no on-chain mainnet state to migrate; the version bump signals the architectural cut.*

### Added

- **`packages/contracts/src/diamond/`** — full diamond infrastructure
  - `Diamond.sol` proxy entry-point + selector router
  - `IDiamondCut.sol` + `IDiamondLoupe.sol` admin/introspection
  - `ClanWorldDiamondInit.sol` single-shot init mirroring monolith constructor field-for-field
  - `OwnershipFacet.sol` exposing `transferOwnership(address)` + `owner()` for upgrade-key rotation
  - 24 logic facets covering heartbeat, settlement, submit-orders, bandit lifecycle/combat/spawning, season finalize, gold/vault/blueprint/bundle/clan-ownership transfers, treasury, market views, world/clan/bandit views, raw views, derived views, and diamond cut admin
  - 11 shared libraries: `LibStorage`, `LibDiamond`, `LibSettlement`, `LibSettlementMath`, `LibBanditCombat`, `LibBanditLifecycle`, `LibBanditSpawning`, `LibSeason`, `LibMission`, `LibGameRules`, plus `LibOrder*` order-handling libs
- **`packages/contracts/script/DeployDiamond.s.sol`** — full deployment lifecycle: 24 facets across 3 cut batches → `ClanWorldDiamondInit.init()` → `ClanWorldLens` → 6 boundary tokens → 4 StubPools → `initTreasury` → token seeds → `seedPools()`. CI dry-runs the script.
- **`packages/contracts/script/DiamondSelectors.sol`** — per-domain selector enumeration (52 `IClanWorld` selectors mapped across 24 facet cuts).
- **`packages/contracts/test/diamond/`** — 1,688-line `DiamondSkeleton.t.sol` parity test suite + `DiamondEventParity.t.sol` covering 58 tests across heartbeat / settlement / transfers / views / bandit flows. Field-level equality verification between monolith and diamond.
- **`StorageLayoutGuard.t.sol`** — asserts `clan.world.app.storage.v1` and `clan.world.diamond.storage.v1` slot constants stay distinct + match expected keccak hashes.
- **`docs/architecture/diamond-pattern.md`** — operator/contributor guide to the diamond architecture.
- **CI gates** (`.github/workflows/contracts.yml`, `scripts/check-contract-sizes.mjs`):
  - Per-facet EIP-170 size enforcement (24,576 bytes)
  - Storage layout snapshot guard
  - Diamond parity test suite as separate job
  - `DeployDiamond.s.sol` dry-run

### Changed

- **`Deploy.s.sol`** is now a 3-line wrapper (`contract Deploy is DeployDiamond {}`) — operator muscle memory deploys the diamond, not the oversized monolith. Zero monolith-deploy paths remain.
- **Off-chain ABI consumers** (`packages/shared/src/adapters/IChainClient.ts`, Convex `apps/server/convex/`) regenerated from updated `packages/contracts/abi/IClanWorld.json`. Event field renames (`wood/iron/wheat/fish` → `woodDelta/ironDelta/wheatDelta/fishDelta`) propagated via `pnpm gen:chainclient-abi`.

### Fixed

Two SuperSwarm rounds × 5 reviewers each (Codex 5.4 + 5.5 + Gemini 3 Pro + Opus 4.6 + 4.7) surfaced and resolved 5 MUST-fix items:

- **OwnershipFacet** added so deployer EOA isn't permanent upgrade key (4-way convergent finding)
- **`Deploy.s.sol` rerouted to diamond** (was still deploying oversized monolith)
- **`DeployDiamond.s.sol` completed** with treasury init + pool seeding (was stopping after facet cut)
- **`MAX_CROP_TRANSITION_PER_TICK`** restored to 48 — matches monolith; silent parity break in audited safety constant
- **`markClanDead` cleanup parity** restored: `_clearDefender`, `_refundUpgradeReservation`, `_releaseDefendersForDeadTarget`, `_abortBanditAttacksForDeadTarget` all mirrored from monolith (opus 4.6 unique find — others missed entirely)

Plus:

- **Settlement reservation simulation** (`bac7c6a`): diamond simulation now tracks wood/iron/blueprint reservations during commit. Diamond actually IMPROVES on monolith here per opus 4.6 audit (monolith only tracked wheat in simulation).
- **Season finalization tick boundary** (`e713728`): `currentTick = last tick closed/settled`. Heartbeat freezes at `seasonEndTick`, `finalizeSeason()` settles through `currentTick`, sets `seasonFinalized=true`. Next heartbeat rolls. No double-processing.
- **`LibDiamond.addFunctions/replaceFunctions/initializeDiamondCut`** now have `enforceHasContractCode()` checks — owner-footgun protection against bad cuts to EOAs or dead addresses.
- **`chainclient-abi` CI** — regenerate ABI fragment to track event field renames in `IChainClient.ts`.

### Removed

- `derivedViewsFacetVersion()` orphaned external function (was exposed but not wired to selectors)
- `rawViewsSelectors()` legacy 26-entry function fully replaced by 4 per-domain functions
- `ClanWorldFacetPlaceholders.sol` 12 empty placeholder contracts

### Deferred to v2.0.1

- **Helper consolidation** (PR #469): `releaseDefendersForDeadTarget` + `abortBanditAttacksForDeadTarget` literally duplicated between `LibBanditCombat` and `LibSettlement` after the round-1 markClanDead fix. Both opus 4.6 + opus 4.7 r2 reviews flagged the silent-divergence risk.
- **Lazy-settlement clan death event-emission parity** (codex 5.4 r2 MEDIUM): observer/indexer-facing only; on-chain state correct.
- **6× duplicated `_settleClan` private function** across 6 facets (opus 4.6 MEDIUM): extract to shared lazy-settle.
- **41 library functions `public` instead of `internal`** (opus 4.6 MEDIUM): DELEGATECALL overhead. Optimization only.
- **Storage layout field-offset snapshot** beyond slot constants (opus 4.7 r2 MEDIUM).
- **`bac7c6a` write-then-overwrite pattern** in `LibSettlement.commitSimulation` (opus 4.7 r2 MEDIUM).
- **`MAX_CROP_TRANSITION_PER_TICK` access-modifier parity** (opus 4.7 r2 LOW).
- **`LibDiamond.setContractOwner` zero-address guard** (opus 4.7 r2 LOW).

### Review coverage

- 2× SuperSwarm rounds (5/5 reviewers each: Codex 5.4 + 5.5 + Gemini 3 Pro + Opus 4.6 + 4.7) — convergent SHIP verdict at HEAD `1e01c38`
- 1× cloud (Copilot + ChatGPT codex bot)
- Local 3-tier review on individual round-1 fix commits

### Migration notes (for ops)

The deploy address changes — Diamond.sol is a different contract type than the ClanWorld monolith. Consumers hardcoding the v1.x `ClanWorld` address need to redeploy with the new Diamond address. Off-chain ABI consumers regenerate from `packages/contracts/abi/IClanWorld.json` (unchanged shape; `pnpm gen:chainclient-abi` keeps `IChainClient.ts` in sync).

`OwnershipFacet.transferOwnership(address)` enables upgrade-key rotation post-deploy. Recommend transferring ownership to a multisig or DAO immediately after the initial deploy + diamond cut.

---

## [1.2.0] — 2026-05-02

### Highlights

> [!NOTE]
> **v5 animation demo-day subset.** v1.2.0 lands the high-ROI slice of the full v5 animation north-star spec — ships the premium-feel cues that read on stage without committing to the multi-week full implementation. Three implementation rounds, six fix-rounds across 3-tier local review × 3 + SuperSwarm × 4 + cloud (Copilot + ChatGPT codex bot), all convergent CLEAN. PR #455.
>
> - **Z-sort architecture fix** — single sortable `worldDynamic` container with global `zIndex = Math.round(y)` enables true 2.5D occlusion (clansman walking behind a building actually renders behind). Previously each entity type lived in a separate Pixi `Container`, breaking cross-type Y-sort even with `sortableChildren = true`. Spec §14 rewrite captures the architectural fix + child-of-host attachment patterns + combat reparenting protocol.
> - **Building breathe** — every base does a 1-pixel vertical sin sway at proper 0.25 Hz (4-second period), with position-derived phase offsets so adjacent bases desync. Invisible until missing — single biggest premium-feel cue.
> - **Day/night cycle** — single GPU `ColorMatrixFilter` on the world container cycles through 4 keyframes (dawn / day / dusk / night) over 30 ticks. Per-base window glow Graphics (alpha tied to `1 - daylightBrightness`) lights up bases at night.
> - **Carry indicators** — fill bar above each traveling clansman tweens 0→1 during gather/travel, drains on deposit. 16×3px parchment-cream fill on ink background with 1px outline.
> - **Tap-to-zoom + selection ring** — `pixi-viewport.animate` to tapped sprite over 400ms easeInOutQuad with scale 2.0; rotating dashed ring (8 segments, 1Hz rotation, 0.5Hz alpha pulse) attached as first child of the selected sprite. Esc tweens viewport back to fit-world.
> - **Counter ticks (RollingNumber)** — vault values wrapped in `<RollingNumber>` with `min(400, 100 + log2(|delta|)*40)`ms easeOutQuad tween; `+N` (green) / `-N` (red) delta floater drifts up 16px and fades over 800ms. Demo-only `useDemoResourceJiggle` 6s interval mutates one random resource so the animation is observable on stage without backend changes.
> - **Combat vignette (3.7s)** — replaces the spec's full-tick 10-phase choreography per codex DA recommendation. Triggered at start of pre-attack tick (or last 4s with precise tickEpoch): world dim fade-in 600ms → combatants reparent to `combatHighlight` above dim → advance to base center 1.5s → idle/jitter 0.5s → full-screen white flash 200ms → resolution 1.5s (success: bandit launch + shrink/fade + defenders cheer; failure: clansmen knockback + wall scale.y drop). `?combat=success|failure` URL toggle for stage flexibility. §10.8 day/night cap rule (`max(0.2, 0.55 - existingDarkness)`) so combat at night stays readable.
>
> *Full v5 animation spec authored by Liam (1,172 lines) is the post-hackathon north-star target — committed but explicitly out of scope for this release. Demo-day subset (235 lines) is the ruthless cut shipped here.*

---

### Added

- `docs/planning/clanworld_v5_animation_spec.md` — full v5 animation north-star (post-hackathon target)
- `docs/planning/clanworld_v5_demo_day_subset.md` — hackathon-scope cut (8 items, 13h budget)
- `apps/web/src/WorldMap.tsx` — tiered Pixi container layout (`terrainBackground`, `terrainAccents`, `worldDynamic`, `inWorldEffects`, `selectionRings`, `bubbleLayer`, `screenEffects`); building breathe ticker; day/night `ColorMatrixFilter` + per-base window glow; tap-to-zoom + dashed selection ring + Esc clear; combat vignette state machine with `combatVignetteRef` + `banditDefeatedRef` lifecycle; carry-indicator child container per traveling clansman
- `apps/web/src/components/cockpit/tabs/VaultTab.tsx` — `RollingNumber` component (rAF tween + `+N`/`-N` floater) wrapping every vault counter; `useDemoResourceJiggle` 6s mock-tick hook

### Fixed

- **Z-sort:** `sortableChildren` only sorts within a single Container — the original §14 layer split (buildings layer 3, clansmen layer 5) made cross-type Y-sort impossible. Single `worldDynamic` container resolves
- **Carry indicator memory leak:** `t.gfx.destroy()` wasn't recursive, leaving carry-bar `Graphics` children to accumulate per-spawn — `destroy({ children: true })` at both expiration sites
- **Breathe frequency:** `Math.sin(t / 4000)` gave a ~25 second period (≈0.04 Hz), not the spec's 4-second period (0.25 Hz). Sin argument is in radians; correct formula is `Math.sin(t * Math.PI / 2000)`
- **Day/night live tick:** `dayNightCb` registered once at Pixi init captured the initial `snapshot` prop forever, leaving the cycle stuck on the `Date.now()` fallback. `snapshotRef` updated by useEffect resolves
- **Bandit fallback selection:** `banditIcon` (Graphics) had `position=(0,0)` and drew shapes at world `(iconX, iconY)`. Tap-zoom called `target.getGlobalPosition()` which returned `worldDynamic` origin, snapping camera to map (0,0) instead of bandit. Position the icon at `(iconX, iconY)` and draw locally
- **Combat dim cap rule:** Earlier `min(0.55, 1 - brightness)` was inverted — at full daylight (brightness=1), combat dim collapsed to 0. Replaced with `max(0.2, COMBAT_DIM_ALPHA - existingDarkness)`: full dim during day, gentle clamp at night
- **Bandit pulse vs vignette:** pulse ticker overwrote `bandit.alpha` every frame after the combat ticker, silently nullifying the success-outcome fade-out. Pulse `onTick` early-returns when `combatVignetteRef.current` is set
- **Combat vignette trigger window:** `getMsUntilTickClose()` falls back to 60s when `tickEpoch` is unavailable, but logs-driven `liveTick` advances every ~20s. The `msUntilTickClose <= 4000` branch never fired before the tick advanced. Detect fallback mode and trigger at start of pre-attack tick instead of last 4s
- **Defeated bandit reappearance:** `finishCombatVignette` unconditionally restored `bandit.alpha = banditStart.alpha`, popping the defeated bandit back at full opacity after a `?combat=success` fade-out. New `banditDefeatedRef` flag set on natural success; respected by `redrawBandit`, pulse `onTick`, and post-vignette restore
- **`Assets.load` mid-vignette regression:** Earlier guard early-returned the entire `.then()` callback, permanently losing the bandit sprite if the asset resolved during the 4s vignette window. Now creates the sprite + assigns to `drawn.banditSprite` unconditionally; only the `redrawBandit()` call is gated
- **`selectTarget` during vignette:** new selection ring during the dimmed combat scene undercut focus and could persist after dim layer faded. Early-return when `combatVignetteRef.current` is set
- **`relayout` during vignette:** snapshot-driven relayout's `redrawBandit()` could snap the reparented bandit back to home anchor mid-choreography. Skip when `combatVignetteRef.current` is set
- **`RollingNumber` rapid-update jump + StrictMode skip:** `previousValueRef.current` was updated immediately in the effect, so back-to-back updates and StrictMode double-invoke produced visible jumps. Two refs (`renderedValueRef` for current displayed value + `targetValueRef` for last seen target) decouple tween-from from no-change-detection
- **Selection ring Graphics leak:** `clearSelection` removed the ring from its parent and nulled the ref but never called `ring.destroy()`. Old rings were JS-GC-able but their WebGL geometry buffers stayed in VRAM until GC fired. `selected.ring.destroy()` before nulling
- **VaultTab `delta` string stale:** `useDemoResourceJiggle` mutated only `value`, leaving the static `delta` string showing conflicting movement. Now updates both alongside

### Deferred to post-demo

- Full v5 spec implementation (combat full-tick 10-phase choreography, strategic 8×8 atlas, cross-fade transitions, Submission 2 transfer demo cinematic, monument tier-up cinematic, speech bubble anti-occlusion, particle pool of 32, asset pipeline validation)
- Bridged GOLD token integration (PR #466 scoping doc only — no code changes; ships post-Diamond-proxy)
- Cosmetic cleanups: `frame.sat` dead config, `combatPlayedTickRef` comment-vs-code mismatch, `combatHighlight` zIndex no-ops, level-badge orphaning defensive path, inline `<style>` collision risk

### Review coverage

- 3× local 3-tier (Codex 5.5 + Claude Opus 4.7 + Gemini 3 Flash) — 1 fix-round per round
- 4× SuperSwarm (Codex 5.4 + 5.5 + Gemini 3 Pro + Opus 4.6 + 4.7) — 2 fix-rounds before convergent CLEAN
- 1× Cloud (Copilot + ChatGPT codex bot) — 1 fix-round
- 13 commits on the feature branch (3 docs + 3 implementation rounds + 7 fix-round commits)

---

## [1.1.0] — 2026-05-02

### Highlights

> [!NOTE]
> **GOLD Bridge workspace + GPT-5.5 Pro audit hotfix bundle.** v1.1.0 introduces the cross-chain GOLD bridge as a sibling workspace and lands 13 MUST-fix findings from external static review across 8 wave-stack fixes:
>
> - **GOLD Bridge workspace (#412)** — standalone `gold-bridge-monorepo/` with the 9-decimal upgradeable Base GOLD token, NTT (Wormhole Native Token Transfer) deployment helpers, recovery/timelock tooling, deployment cockpit UI, and Reown wallet integration. Wired into the root pnpm/turbo workspace. **Not yet integrated into ClanWorld game flows** — bridge ships first, integration follows in v1.2.
> - **`finalizeSeason()` now actually finalizes** — emits `SeasonFinalized(tick, rankedClanIds, scores)` per spec §13. Was previously dead code (`// TODO Phase 3`). Boundary-freeze guard at the top of `heartbeat()` ensures the engine cannot replay closed ticks while limbo-pending. All 9 clan-state mutators reject submissions during frozen-unfinalized limbo.
> - **Heartbeat upkeep-before-mission ordering** — `_settleClanThroughTick` mirrors lazy-settle path. Heartbeat advances `lastSettledTick`. New `HeartbeatLazyParity.t.sol` proves both paths converge.
> - **Cooldown is submit-side only** — stripped erroneous cooldown reset on natural mission completion. Elders chaining gather→deposit→gather no longer pay ~50% extra wall-clock per cycle.
> - **Convex real-indexer rolled out** behind `CLANWORLD_USE_REAL_INDEXER` flag — webhook tx-decoder, idempotent `(txHash, logIndex)` dedup, 5-block confirmation depth, 8 dedicated tables (`chainEvents`, `tickHistory`, `clanView`, `marketState`, `banditView`, `pricePoint`, `eventCheckpoint`, slim `worldSnapshot`). Webhook validates `receipt.status`, `receipt.to`, payload `engineAddress`; filters logs to engine before parseEventLogs. Mutually exclusive with v1.0.0 fake heartbeat. Indexer cursor isolation: webhook ingests events but only `pollLogs` advances the contiguous-scan cursor — closes a permanent-event-loss class.
> - **Reservation-aware vault primitives** — `_spendableAfterReleasing` + `_deductFromVault`. Bandit theft and winter wood burn now respect resource reservations.
> - **Demo-mode default flipped to opt-in** — `DEMO_MODE` is OFF by default. Prepares for live-chain UAT.
> - **Phase 5B v4.6 economy alignment** — clan death from starvation with next-tick semantic, traveling defender cleanup on dead target, treasury init validation, bandit forbidden region spawn ban (UnicornTown/DeepSea), bandit defeat 1e18 Gold reward, `RealChainClient.submitOrders` field preservation.
> - **CI hardening** — `chainclient-abi.yml` now installs `foundry-rs/foundry-toolchain@v1` AND hard-fails if forge missing. Loud-warns to stderr if dev runs without forge instead of silent-skip. Closes a class of ABI drift that v1.0.0's silent-skip masked.
>
> *v1.0.0 shipped a feature-complete game. v1.1.0 adds cross-chain bridge plumbing for cross-game GOLD flows, lands 13 MUST-fix findings from external GPT-5.5 Pro static review, and hardens the on-chain contract through 8 sequential review-and-fix waves — the last work before live UAT.*

---

### Audit-Driven Hotfixes (8 Waves)

13 MUST findings from GPT-5.5 Pro external static review of v1.0.0, validated by parallel codex + Claude validators (12/13 confirmed by codex; 13/13 confirmed by Claude with 2 forge tests proving bugs present). Implemented as 8 sequential codex waves with super-swarm review rounds catching fix-introduced regressions.

#### Wave 1 — silent-skip → loud-warn + demo-default flip + RealChainClient fields
- **MUST-12** `9af9834` + `a98f66b` — `pnpm test/build/check:abi` loud-warns when forge missing instead of silent-skip; `chainclient-abi.yml` adds hard-fail Foundry guard + foundry-toolchain install step
- **MUST-13** `7e97f7a` → `c635e8f` — flip `DEMO_MODE` default to opt-in (was always-on); gate fake heartbeat cron behind `CLANWORLD_USE_FAKE_HEARTBEAT`; webhook reads tx data instead of calling fake mutation
- **MUST-11** `cd10fba` → `6256043` — `RealChainClient.submitOrders` preserves `targetClanId`, `marketToken`, `marketAmount`, `maxGoldIn`, withdraw fields (was hardcoded to zero)

#### Convex real-indexer rollout
- **C1 schema** `0af22f9` — 8 new Convex tables
- **C2-C8 bulk** `4ba21c8` — webhook decoder + cron pollers + cutover plan, feature-flagged behind `CLANWORLD_USE_REAL_INDEXER`
- **Critical fix-round** `7e298bb` — addresses 5 critical findings from claude reviewer (cold-start RPC bomb prevention, 15s receipt timeout, async snapshot scheduling, 5-block confirmation depth, frontend-compat `clans[]` backfill)

#### Wave 2 — Phase 9 bandit + treasury (4 fixes bundled)
- **MUST-4** `fb399bb` → `58436d2` — bandit forbidden region spawn ban (UnicornTown, DeepSea)
- **MUST-9** — dead-target traveling defender cleanup
- **MUST-10** — treasury init validation (zero/duplicate guards)
- **Synthesis Gap 9.5** — bandit defeat reward includes 1e18 Gold per spec §6.17

#### Wave 3 — heartbeat upkeep-before-mission ordering (MUST-2)
- **`0b2830c`** — `_settleClanThroughTick(clanId, throughTick)` mirrors `_settleClan` upkeep-then-mission ordering. Heartbeat now advances `lastSettledTick`. 7 existing tests updated (they had codified the buggy ordering); new `HeartbeatLazyParity.t.sol` verifies path equivalence.

#### Wave 4 — Phase 9 candidate eager-settle + reservation primitives + upgrade queue
- **`e466189`** — `_eagerSettleBanditCandidateRegion` settles candidates before pickTarget (MUST-5); one-pending-upgrade-per-type guard replaces multi-pending dependency chain (MUST-7); reservation-aware vault primitives — bandit theft + winter wood burn now respect reservations (MUST-8)

#### Wave 5 — `finalizeSeason()` emit-only + auto-roll guard (MUST-1)
- **`3c086d7`** → **`82d0d44`** — `finalizeSeason()` body implements eager-settle + rankings + `SeasonFinalized(tick, rankedClanIds, scores)` emit. Per Liam Decision 0.3, no payout in v1.1.0 (deferred to v1.2+). `_resolveWorldEvents` only auto-rolls when `seasonFinalized == true` — prevents bypass.

#### Wave 6 — strip cooldown on natural completion (MUST-3)
- **`f1e8bfd`** → **`98521ff`** — Per spec v4.2 §10.2, cooldown is a submit-side rate-limit only. Stripped the erroneous reset in `_completeMission`. Saves Elders ~50% wall-clock on chained gather→deposit→gather cycles.

#### Wave 7 — round-1 super-swarm fix bundle (4 MUSTs + 3 SHOULDs)
Round-1 super-swarm (codex 5-3 + 5-4 + 5-5 + Gemini 3.1 Pro) on the post-Wave-6 state caught 4 convergent HIGH bugs, all addressed in Wave 7 `5c68235`:
- **MUST-7.1** — `SeasonFinalized` event ABI drift; regenerated artifacts; new `SeasonFinalizedAbi.t.sol` topic-hash test
- **MUST-7.2** — `finalizeSeason` boundary off-by-one; freeze heartbeat at `seasonEndTick - 1` until finalized
- **MUST-7.3** — Indexer cursor isolation (webhook does NOT advance `eventCheckpoint`) + auth validation (`receipt.status`, `receipt.to`, payload `engineAddress`, log filtering)
- **MUST-7.4** — `validateSubmitOrderPayload` allows `DefendBase` self-orders
- **SHOULD-7.5/7.6** — snapshot block pinning + `pricePointFromEvent` direction

#### Wave 8 — round-2 super-swarm regression fix
Round-2 super-swarm caught Wave 7's MUST-7.2 freeze placement bug (freeze at end of heartbeat → repeated tick replay → bandit `probabilityAccum` runaway). Wave 8 `d6dd56b`:
- **MUST-8.1** — moved boundary freeze to TOP of `heartbeat()`; engine never re-enters same closed tick
- **MUST-8.2** — `_requireNoPendingSeasonFinalization()` guard added to all 9 clan-state mutators (`submitClanOrders`, `settleClan`, `settleClansman`, `mintClan`, `transferClanOwnership`, `transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`)
- **SHOULD-8.3** — snapshot pinning unconditionally uses `receipt.blockNumber` (not payload override)

---

### Phase 5B Economy Alignment (#378)

Path A canonization of v4.6 economy semantics:
- **Clan death from starvation** with `tick+1` next-tick onset semantic (`starvationStartsAtTick = tick + 1` on first detection during settlement)
- **Strict less-than kill check** — `effectiveStarvationStartsAtTick < tick` (kill fires only AFTER onset tick); deferred kill cadence
- **Winter mechanics** — wheat plot lock at winter start (`_lockWheatPlotsForWinter`), restart after winter end (`_restartWheatPlotsAfterWinter`), winter doubles wheat+fish upkeep, winter wood burn per base + per living clansman, cold damage accrual that can degrade walls or kill clansmen
- **Gather actions** — reschedule until carry cap or plot depletion (was: single 4-tick batches always terminate to WAITING)
- **`ResourcesDeposited` event rename** — `wood/iron/wheat/fish` → `*Delta` (clarity-first, per ClanWorld no-backcompat policy; pre-prod GA)
- **`WOOD_CAP = 15e18`** distinct from `CLANSMAN_CARRY_CAP = 10e18` (wood uses WOOD_CAP)

---

### Cross-Phase Plan + Spec Doc Updates

- **Phase 9 v4.6 bandit redesign addendum** (#341) — Path A canonize impl
- **Phase 12 agent infrastructure rework plan** (#373) — design doc for hybrid `CLAUDE_CONFIG_DIR` (shared `agents/.claude` + per-elder `elder-N/.claude`); 376 lines; ready for v1.2 implementation
- **Phase 5B economy alignment doc** (#378) — UAT checklist updated to validate current code; doc accuracy fixes for winter mechanics, gather rescheduling, wood cap, starvation timing

---

### Validation at Release HEAD

- forge test: **352/352 passing** (was 318 baseline at v1.0.0; +34 from waves 1-8 + Phase 5B)
- pnpm typecheck green
- pnpm test:chainclient-abi green
- pnpm test:abi-parity green
- pnpm -F @clan-world/{shared,runner,agents,server,web} all green
- ABI parity test wired in CI; SeasonFinalized topic-hash directly verified

### Sources

- GPT-5.5 Pro static review of v1.0.0 main — 13 MUST-fix release blockers
- Codex validator (12/13 confirmed) + Claude validator (13/13 + 2 proof tests)
- Round-1 super-swarm: 6 LLMs (codex 5-3 + 5-4 + 5-5 + Opus 4.6/4.7 silent-failed + Gemini 3.1 Pro) — `docs/reviews/pr413-codereview-*.md`
- Round-1 synthesis: `docs/reviews/pr413-synthesis.md`
- Round-2 focused review on Wave 7 deltas (codex 5-4 + 5-5 + Claude) — `docs/reviews/pr413-synthesis-round2.md`
- Convex indexer hybrid plan synthesized from parallel codex + Claude planners
- Merge-fix diagnose-only convergence (codex 5-5 + Claude feature-dev:code-reviewer) — both ROOT_CAUSE_FOUND identifying test fixture issues, not contract bugs

---

## [1.0.0] — 2026-05-01

### Highlights

- Full on-chain game engine: 10 contract phases covering gathering, markets, buildings, bandits, winter, and clan death — 310/310 Forge tests green at ship
- Four AI Elder agents run autonomously on Base Sepolia, each submitting real transactions via `RealChainClient` on every heartbeat tick
- Resource reservation invariant enforced: `WithdrawResources` and all OTC transfer paths are reservation-aware, closing a class of vault-drain exploits found during pre-release audit
- ABI drift is structurally impossible: generated `CLAN_WORLD_ABI` replaces every hand-rolled tuple; `gen-enums.mjs` and `gen-constants.mjs` keep TypeScript in sync with Solidity
- Pixi.js canvas world map with 8 regions, isometric base sprites at five upgrade levels, clansman walking animations, speech bubbles, pinch-to-zoom, and a live scoreboard
- World ID humanity verification at clan mint via MiniKit + IDKit integration
- Convex real-time backend with heartbeat webhook, safety-net cron, and mock-mode for offline development
- ABI parity test wired into CI — contract shape drift fails the build automatically

---

## Game Engine — Phases 1 through 10

The contract evolved through ten ordered phases. Each phase is its own ratcheted-up version of the engine, with its own super-swarm review pass, its own fix-rounds, and its own integration tests. They merge sequentially: each phase's `dev-phase-N-*` integration branch lands into `dev-merge` only after the prior phase is green.

### Phase 1 — Real ClanWorld engine (#79, #98)

> [!NOTE]
> **Foundation Engine Online:**
> 1. **`ClanWorld.sol`** — the real on-chain game contract replacing the planning stubs
> 2. **`mintClan`** — clan creation with World ID verification handle
> 3. **Order submission** — clansman action queue with explicit `ClanOrder` struct
> 4. **Heartbeat skeleton** — the tick-advancement entry point for the world
> 5. **Lazy settlement core** — clans replay tick-by-tick when next touched (the central performance pattern)
> 6. **View-only simulation** — derived getters can preview state without writing (#261)
>
> Phase 1 is the *substrate*. Without lazy settlement and without the on-chain entry point, none of the later phases compose. Everything from gathering to bandits assumes "I can read the freshest derived state without paying gas," and Phase 1 is what makes that affordable.

- Phase 1 real engine: `mintClan`, order submission, heartbeat, lazy settlement core (#79, #98)
- View-only settlement simulation for derived getters (#261)

### Phase 2 — Economy foundation (bundle E, #91, #137)

> [!NOTE]
> **First Resource Loop:**
> 1. **Initial economy types** — `ResourceType` enum (Wood / Iron / Wheat / Fish), `Vault` struct, vault accounting
> 2. **Resource flows** — gather → vault → carry primitives that later phases extend
> 3. **`bundle E`** — collapses the post-rebase Phase 2 implementation onto the pre-Phase-3 substrate
>
> Phase 2 is the *vocabulary*. Once it landed, every subsequent phase could speak in terms of "wood, iron, wheat, fish" instead of inventing its own resource shapes.

- Bundle E: Phase 2 economy (#91 post-rebase, #137)

### Phase 3 — Mission assignment + lazy settlement (#176–#181, #115)

> [!NOTE]
> **Missions Have Time:**
> 1. **`submitOrders`** — the public order queue API used by Elders + UI
> 2. **`defend_base`** — first defensive mission type, prerequisite for the bandit phase
> 3. **Mission timing rules** — every action gets a duration, a `settlesAtTick`, and a settle path
> 4. **39-case Foundry test spec** — exhaustive coverage scaffold for mission state transitions (#115)
> 5. **Bundle A `feat/phase-3-test-spec`** — the test spec that locked Phase 3 mechanics into the contract
> 6. **Orch-r1 integration fixes** — review-driven correctness pass (#181)
>
> Phase 3 is when the engine *gains time*. Before this, every action was instantaneous; after this, missions take ticks to complete and clansmen can be in flight. This is the substrate for everything that *waits* — bandits camping, walls building, winter approaching.

- Phase 3 mission assignment + lazy settlement: `submitOrders`, `defend_base`, mission timing rules (#176, #177, #178, #179, #180, #181)
- Phase 3 integration fixes from orch-r1 review (#181)
- Phase 3 Foundry test specification — 39 cases (#115)
- Bundle A: `feat/phase-3-test-spec`

### Phase 4 — Heartbeat + progression (#173–#175, #182, #183, #239)

> [!NOTE]
> **The World's Pulse:**
> 1. **Permissionless heartbeat** — anyone can fire ticks; rate-limited via `nextHeartbeatAtTs`
> 2. **Domain-separated RNG** — `keccak256(seed, clan, csId, nonce)` per use site, no cross-contamination
> 3. **Winter + season timers** — the calendar machinery that Phase 10 hangs winter mechanics on
> 4. **Heartbeat ordering fix** — HIGH spec drift between heartbeat and lazy paths corrected (#239)
> 5. **Tick seed publication** — every tick commits a fresh RNG seed visible to indexers and views
>
> Phase 4 is *autonomy*. Once the heartbeat is permissionless, the game runs whether or not any specific keeper is alive — multiple keepers can race, the rate-limit handles contention, and the off-chain runner becomes a *helper* instead of a *requirement*.

- Phase 4 permissionless heartbeat, RNG helpers, winter/season timers, heartbeat ordering fix (#173, #174, #175, #182, #183, #239)
- Phase 4 heartbeat ordering (HIGH spec drift) (#239)

### Phase 5 — Gathering + deposit (#188, #190, #234, #298, #371, #356)

> [!NOTE]
> **Resources Move:**
> 1. **Wood gathering** at forest regions — the first real resource action
> 2. **Deposit action** — vault-to-base resource transfer that settles at clan's home region
> 3. **Per-tick yield** with carry-cap enforcement
> 4. **Starvation next-tick semantics** — first-tick starvation flags, second-tick kills (#234)
> 5. **`ResourcesDeposited` event ordering** — explicit `atTick` field for indexers (#234, #298)
> 6. **Wood carry cap clamping** — clansman can't over-carry forest yield (#234)
> 7. **v4.6 Phase 5 economy alignment addendum** — spec-vs-impl reconciliation (#356)
>
> Phase 5 is the moment ClanWorld stops being a planning doc and *starts working*. A clansman walks to the forest, chops wood, walks home, deposits — every step settles on-chain and emits an event a UI can render.

- Wood gathering, deposit action, per-tick yield, starvation next-tick, wood carry cap, `ResourcesDeposited` event ordering (#188, #190, #234, #298, #371)
- Phase 5 R1 fixes — `ResourcesDeposited` event order + tick + four medium fixes (#234)
- Phase 5 ABI `uint64` revert + per-tick yield migration + `ERR_NOT_AT_HOMEBASE` (#298)
- v4.6 Phase 5 economy alignment addendum (#356)

### Phase 6 — Markets + pools (#228, #240, #257, #260, #263, #262, #284, #324, #298, #270, #294, #295, #380, #357)

> [!NOTE]
> **Liquid Economy Online:**
> 1. **Resource-bound ERC20 tokens + treasury seeder** — wood/iron/wheat/fish each get a token (#228)
> 2. **Seeded constant-product pools** — wood/gold, iron/gold, wheat/gold, fish/gold (#240)
> 3. **Immediate + scheduled market actions** — clansmen can buy/sell now or queue for next heartbeat
> 4. **Carry-based market trades** — workers *physically haul* the resource to/from the market (#284)
> 5. **`StatusCode` enum stability** — locked by Solidity test, off-chain consumers can rely on ordinals (#324)
> 6. **`MarketBuy` error path + `uintValue` robustness** (#295)
> 7. **Market failure observability** — distinct status codes per failure mode for indexers (#283, #294)
>
> Phase 6 is when ClanWorld becomes a *trading game*. Resources can be *converted* now, not just gathered — and the carry-based mechanic means a clan can be raided mid-trade, which is the seam Phase 9 (bandits) exploits.

- Resource boundary tokens + treasury seeder (#228)
- Seed pools, immediate and scheduled market actions, carry-based market trades, market failure semantics, market events surface (#240, #257, #260, #263, #262, #284, #283)
- `StatusCode` enum stability (#324)
- Phase 6 cloud-review fix-round (#270)
- Phase 6 R3 wheelbarrow vault-carry + sell validation (#294)
- Phase 6 R4 `ActionType` enum stability + `MarketBuy` error + `uintValue` robustness (#295)
- Phase 6B market spec cleanup — seed ratios, `executeAtTick`, slippage alignment (#380, #357)
- Phase 5/6 ABI `uint64` revert + per-tick migration (#298)

### Phase 7 — OTC transfers + ownership (#243, #246, #248, #252, #256, #389, #397, #292)

> [!NOTE]
> **Inter-Clan Diplomacy:**
> 1. **`transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`** — five direct transfer functions for cross-clan resource flows
> 2. **`transferClanOwnership`** — explicit owner handoff with settle + dead-clan guard (#397)
> 3. **OTC strip-out** — replaced the legacy OTC order type with direct transfers (#389)
> 4. **Phase 7 R3 stale OTC + `expiryTick uint64` + cap reap + access cleanup** (#292)
> 5. **OTC dead-clan restriction** (#256)
> 6. **Codegen allowlist updated** for the 5 new transfer functions (#397)
>
> Phase 7 turns ClanWorld into a *negotiation game*. Two clans can now form alliances, fund each other's upgrades, or pay tribute — and the contract enforces the *atomic guarantees* (settle-then-debit, dead-clan checks) so the negotiation can't be exploited.

- Gold, vault, blueprint, bundle transfer functions (#243, #246, #248, #252)
- OTC dead-clan restriction (#256)
- Phase 7 OTC strip-out — direct transfers replace OTC orders (#389)
- `transferClanOwnership` (#397)
- Phase 7 R3 stale OTC + `expiryTick uint64` + cap reap + access cleanup (#292)

### Phase 8 — Buildings + upgrades (#236, #238, #242, #251, #360, #361, #364, #291, #296, #391, #355)

> [!NOTE]
> **Bases Grow:**
> 1. **Wall, base, monument upgrades** — three building tracks each with multiple levels (#236, #238, #242)
> 2. **Score + rank getters** — `getRankings`, `_getClanScore`, `quoteLootValueSettled` derive from monument level + vault (#251)
> 3. **Upgrade reservation system** — wood/iron/wheat/blueprints are *held* in `_reserved*ByClan` from queue-time to settle-time (#236, #238, #242)
> 4. **`MAX_CLAN_SCAN_FOR_RANKING`** derived from `MAX_CLANS` (#360)
> 5. **8 HIGH findings** from super-swarm review fixed in one round (#291)
> 6. **Sim/real `fromLevel` parity + ABI pretty-print** (#296)
> 7. **Phase 8B v4.6 buildings alignment addendum** — spec-vs-impl reconciliation (#355)
>
> Phase 8 introduces *time-locked capital*. A clan that queues a wall upgrade has committed wood for the next N ticks — that wood is *no longer spendable* even though it shows in the vault total. This is the invariant that the Tier A reservation-bypass fixes had to retroactively defend.

- Wall, base, monument upgrades; score + rank getters; upgrade reservation coverage (#236, #238, #242, #251, #364)
- Dead internal function cleanup (#361)
- `MAX_CLAN_SCAN_FOR_RANKING` derivation (#360)
- Phase 8 R4 — eight HIGHs from super-swarm review (#291)
- Phase 8 R5 sim/real `fromLevel` parity + ABI pretty-print (#296)
- Phase 8 dev-merge test regressions — winter init + assertion alignment (#391)
- Phase 8B v4.6 buildings alignment addendum (#355)

### Phase 9 — Bandits (#189, #191, #244, #247, #253, #255, #258, #374, #266, #265, #341)

> [!NOTE]
> **Existential Threat Delivered:**
> 1. **Bandit troop state machine** — `Spawned → Camped → Attacking → Resting → Escaped` lifecycle (#189, #244)
> 2. **Spawn chance logic** with global cap and per-region eligibility (#191)
> 3. **Eager-settle scope** — base + defenders in spawn-candidate regions get refreshed pre-spawn (#247)
> 4. **Deterministic attack resolution** — settled defense vs bandit attack power, with two outcomes per spec §6.15 (#253)
> 5. **Defender reward split + blueprint reward on successful defense** (#255, #258)
> 6. **Vault loot theft + rampage path + WAITING-at-home defense** (#374)
> 7. **Cleanup on bandit target death** — defender release + state cleanup (#258)
> 8. **5 HIGH findings** from super-swarm review fixed in one round (#266)
>
> Phase 9 turns boring resource collection into a *strategic shared experience of existential threat*. A bandit can spawn in any region, target the highest-loot clan there, and either steal vault resources or deal damage on attack. Plus the **Phase 9 redesign addendum (#341)** locked v4.6 mechanics. **This is the suspense mechanism** that forces Elders to communicate and cooperate — without bandits, ClanWorld is a flat optimization game; with them, it's a story.

- Bandit troop state machine, spawn chance logic, eager-settle scope, deterministic attack resolution, defender reward split, blueprint reward on successful defense (#189, #191, #244, #247, #253, #255, #258)
- Vault loot theft + rampage path + WAITING-at-home defense (#374)
- Cleanup on bandit target death (#258)
- Phase 9 super-swarm R2 — five HIGH findings (#266)
- Phase 9 cloud-review fix-round (#265)
- v4.6 Phase 9 bandit redesign addendum (#341)

### Phase 10 — Winter + cold + clan death (#235, #237, #241, #245, #249, #289, #293, #383, #363, #287, #345)

> [!NOTE]
> **Seasons Have Consequences:**
> 1. **Winter schedule** — explicit ranges within the season calendar (#235)
> 2. **Winter upkeep** — wheat consumption *doubles*, fish consumption doubles, wood burn for warmth (#237)
> 3. **Cold damage** — clansmen can take cold damage from insufficient wood, accumulates per-tick (#241)
> 4. **Crop winter transitions** — wheat plots `Harvestable → WinterLocked → Regrowing` (#245)
> 5. **Clan death** — starvation or all-clansmen-cold-death marks `clanState = DEAD`, vault burned, gold preserved (#249)
> 6. **Starvation + cold-reset semantics** — first-tick flag, second-tick kill; reset on winter exit (#289)
> 7. **3 super-swarm HIGHs + cleanups** (#293)
> 8. **Sim/winter parity** — `_simulateApplyUpkeep` mirrors real winter logic (#393)
>
> Phase 10 is the *clock that punishes complacency*. A clan that hoards gold but neglects wheat will starve in winter; a clan with no wood will freeze. Phase 10 is what makes the game's resource priorities *time-dependent* instead of static.

- Winter schedule, winter upkeep, cold damage, crop winter transitions, clan death, starvation + cold-reset semantics (#235, #237, #241, #245, #249, #289, #293, #383, #363)
- Phase 10 super-swarm R2 fixes (#287)
- Phase 10 R3 cold-reset regression + cloud findings (#289)
- Phase 10 R4 three super-swarm HIGHs + cleanups (#293)
- Phase 10 spec-compliance UAT review (#345)
- Phase 10 dev-merge follow-ups — dead constant + sim/winter parity (#393)

---

## Pre-Release Hardening (2026-05-01)

After all 10 phases landed in `dev-merge`, an 8–11 reviewer super-swarm (codex 5.3 + 5.4 + 5.5, Claude Opus 4.6 + 4.7, Sonnet 4.6, Gemini 3.1 Pro, plus per-PR cloud reviewers) audited the integrated state. Three tiers of fixes followed.

### Tier A — Reservation-bypass criticals (#394, #395, #397)

> [!NOTE]
> **Vault-Drain Class Closed:**
> 1. **`WithdrawResources` reservation-aware** — adds `_hasSpendableForWithdraw` helper, blocks withdraws of wood/iron/wheat/blueprints already reserved for upgrades (#394)
> 2. **Phase 7 OTC transfers reservation-aware** — `transferVaultResource`, `transferBlueprint`, `transferBundle` all routed through `_deductFromVault` instead of raw subtraction (#395)
> 3. **`transferClanOwnership` dead-clan guard** — was allowed on dead clans, now settles-then-dead-checks (#397)
> 4. **5 + 49 + new exploit tests** added to lock these regressions out
>
> An entire *class* of vault-drain exploits would have shipped silently with v1.0.0 if the super-swarm hadn't caught the pattern. Tier A is the win that justified the audit-after-merge cadence as a permanent practice.

- WithdrawResources reservation-aware: blocks reserved-resource withdraws (#394)
- Phase 7 OTC transfers reservation-aware: vault transfers route through `_deductFromVault` (#395)
- `transferClanOwnership` settle-then-dead-check + codegen allowlist + `ERR_MUST_SETTLE_FIRST` consistency (#397)

### Tier B — 6-item surgical bundle (#407)

> [!NOTE]
> **Surgical Cleanup:**
> 1. **`HEARTBEAT_ABI` duplicate fields deleted** — silent runtime decode bug caught by *8 of 8 reviewers*
> 2. **`marketMode` field added** to TS `SubmitOrderResult` to match on-chain 5-field struct
> 3. **Fake parity test deleted** — `check-chain-abi-parity.test.ts` was self-tautology (compared two fixtures to each other)
> 4. **Duplicate `cli.test.ts` deleted** — canonical copy lives elsewhere
> 5. **Stub `getDerivedClanState` clanId fix** — multi-clan callers were getting clan 0's data
> 6. **`WithdrawResources` simulation branch wired** — `_simulateResolveAction` now mirrors `_resolveAction`
>
> All six were trivial individually, but each was a surface where *the same kind of bug* could have hidden in production. Tier B is the *cheap good move*.

- 6 surgical fixes from PR #396 superswarm (#407)

### Audit — Hand-coded types (#408, #409)

> [!NOTE]
> **Drift Hazards Eliminated:**
> 1. **`HEARTBEAT_ABI` fully replaced with generated import** — `heartbeat()` added to codegen allowlist, runner now imports `CLAN_WORLD_ABI` (audit MUST 1, #408)
> 2. **`gen-enums.mjs` shipped** — regex-parses `IClanWorld.sol` for all 8 contract enums, emits TS `as const` lookup tables (audit MUST 2, #409)
> 3. **Orchestrator `action: 1` literal becomes `ActionType.ChopWood`** — out-of-band knowledge becomes compile-checked (#409)
> 4. **Parity test refactored** — encoder side now reads canonical `IClanWorld.json` via `getAbiItem` (audit MUST 3, #409)
> 5. **`gen-constants.mjs` shipped** — `ClanWorldConstants.sol` → TS `bigint` exports (#409)
> 6. **`anyApi` casts replaced** with generated Convex API types in `IConvexClient.ts` + `useAgentLogs.ts` (#409)
> 7. **Heartbeat-interval values aligned** across `start-heartbeat-loop.sh` + `getSnapshot.ts` empty-state (#409)
> 8. **`check-chain-abi-parity.mjs` extended + wired into CI** — drift fails the build (#409)
>
> The audit asked *"are there other places like the HEARTBEAT_ABI bug?"* — answer was yes, three more, plus six soft-drift surfaces. **Hand-rolled type mirrors are no longer a viable shortcut** in this codebase. The new `handcoded-types-audit` skill captures the methodology so future pre-release moments re-run the same scan.

- Audit MUST 1 — Replace runner `HEARTBEAT_ABI` with generated `CLAN_WORLD_ABI` import (#408)
- Audit phase 2 — `gen-enums.mjs` + `gen-constants.mjs` + parity test refactor + anyApi cleanup + heartbeat-interval alignment (#409)
- Audit `handcoded-types-audit` skill captured for future pre-releases

---

## Cross-Phase Infrastructure

### Agents and orchestrator

> [!NOTE]
> **Autonomous AI Players:**
> 1. **Elder CLI** — full `status`, `orders`, `submit` subcommand coverage (#71)
> 2. **`RealChainClient` integration** — Elder clan submits real on-chain transactions every heartbeat tick (#32)
> 3. **Elder harness in-repo** with `make install` — sandboxed Claude Code agent per Elder (#154)
> 4. **Orchestrator REGION_FOREST routing + `submitOrders` sim semantics** (#383)
> 5. **`ActionType` enum import** replaces bare numeric literal (#409)
>
> Each clan has an *Elder* — an autonomous Claude Code agent with its own wallet, its own private key, its own `submitOrders` cadence. The orchestrator coordinates them; the harness sandboxes them; the CLI lets a human poke at any of them mid-game.

- Elder CLI full subcommand coverage (#71)
- Elder clan `submitOrders` with real on-chain transactions via `RealChainClient` (#32)
- Elder harness in-repo with `make install` (#154)
- Orchestrator `REGION_FOREST` routing + `submitOrders` sim semantics (#383)
- `ActionType` enum replaces bare numeric literal `action: 1` (#409)

### Shared / adapters

- `RealChainClient` with viem — full typed on-chain interface (#27)
- `IChainClient` adapter interface + codegen pipeline (#362, #385)
- Cross-phase hygiene bundle: stub heartbeat parity, ABI parity broadening (#362, #385)

### Web app — Pixi.js canvas

> [!NOTE]
> **The World You Watch:**
> 1. **8-region canvas world map** with clan flags + speech bubbles (#19)
> 2. **`agentLogs` speech bubbles on canvas** — Elder reasoning surfaces visually (#33)
> 3. **MiniKit + IDKit clan-join + World ID verify endpoint** (#34)
> 4. **Isometric base sprites at 5 upgrade levels** + region zones + floating level labels + fullscreen mode (#52, #161)
> 5. **Walking clansman sprites** replace worker dots (#59)
> 6. **Pinch-to-zoom via `pixi-viewport`** — multi-touch + Pixi v8 EventSystem fix (#50, #51, #53)
> 7. **Bubble polish** — clan-colored Elder header, backdrop, tail, fade (#43, #55, #99)
> 8. **Worker travel dot animation** along routes (#45)
>
> Pixi gives ClanWorld its *spectator surface*. You don't need to read JSON to know what's happening — a clansman is walking from the forest to base, the wall just leveled up, an Elder said *"I'm worried about winter."*

- Pixi.js canvas shell — 8 regions, clan flags, speech bubbles (#19)
- Convex `agentLogs` speech bubbles (#33)
- MiniKit + IDKit clan-join + World ID verify endpoint (#34)
- Visual rework — isometric base sprites, region zones, floating level labels, fullscreen mode (#52, #161)
- Clansman walking sprites (#59)
- Speech bubble polish (#43, #55, #99)
- Pinch-to-zoom (#50, #51, #53)
- Bubble tails, world notice panel, live tick counter (#54)
- Demo bypass env for offline recording (#37)
- Graceful render fallback (#35)
- Worker travel dot animation (#45)
- Monument visual + wall opacity by building level (#44)

### Server / backend

- Convex `MOCK_MODE` backend — `getSnapshot` + `agentLogs` (#20)
- Convex heartbeat-webhook HTTP action + safety-net cron (#25)
- Foundry `Heartbeat` script + `start-heartbeat-loop.sh` (#29)

### Tooling and codegen

- `gen-chainclient-abi.mjs` — allowlist-driven ABI extraction to TypeScript (#385)
- `gen-enums.mjs` — regex-parses `IClanWorld.sol` for all 8 contract enums, outputs `as const` lookup tables (#409)
- `gen-constants.mjs` — `ClanWorldConstants.sol` → TypeScript `bigint` exports (#409)
- `check-chain-abi-parity.mjs` extended + wired into CI (#409)
- Playwright e2e harness for `apps/web` (#88)
- Elder vitest CLI suite + regression coverage (#105)
- Vite dev servers default to `port-for` slots (#139)
- Post-bundle-A dev-tooling follow-ups (#140)

### Landing page and docs

- `clan-world.com` landing page — full copy, palette, tale frames, sponsor logos (#30, #48)
- Hackathon judge quick-start banner + submission video embed (#61, #62)
- README polish — hero copy, tech stack, sponsor framing (#31)
- Landing factual corrections — clan count + winter cadence (#36)

---

## Cross-cutting

### Refactor

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
> The OTC strip-out and chain pivot were the two big *spec-vs-impl* alignments — once they landed, every downstream phase had a *consistent* substrate to build on.

- Phase 7 OTC strip-out (#389)
- Base Sepolia chain pivot (#132)
- Drop `*Upgraded` events, keep `*LevelChanged` (#365)
- `MAX_CLAN_SCAN_FOR_RANKING` derivation (#360)
- Carry-based market trades (#284)
- Orchestrator action literals replaced with `ActionType` enum (#409)
- 4 dead internal contract functions deleted (#361)

### Tests

> [!NOTE]
> **Validation Footprint at Ship:**
> 1. **310/310 Forge tests green** at release HEAD
> 2. **WithdrawResources exploit test** + wood/iron/fish/surplus-ok variants (#394)
> 3. **Phase 7 transfer reservation tests** (#395)
> 4. **`transferClanOwnership` dead-clan revert test** (#397)
> 5. **Heartbeat + `getRankings` gas profiling** (#359)
> 6. **ABI parity test wired into CI** — reads canonical `IClanWorld.json` (#409)
> 7. **Playwright e2e harness** for `apps/web` (#88)
> 8. **Phase 3 Foundry spec** — *39 cases* (#115)
>
> Every reservation-bypass exploit and every cross-tier integration shape has a *named test* — regressions can't sneak back in. CI fails the build the moment the contract ABI drifts from the TypeScript adapter.

- 310/310 Forge tests at release
- WithdrawResources exploit test (#394)
- Phase 7 transfer reservation tests (#395)
- `transferClanOwnership` dead-clan revert test (#397)
- Heartbeat + `getRankings` gas profiling (#359)
- Upgrade reservation coverage strengthened (#364)
- Phase 3 Foundry test specification (#115)
- Elder vitest CLI suite + regression (#105)
- Playwright e2e harness (#88)
- ABI parity test refactored to canonical-derived shapes, wired into CI (#409)

### Docs

> [!NOTE]
> **Spec + Planning Artifacts Shipped:**
> 1. **`CANONICAL_SPEC.md`** with precedence + conflict resolutions (#70)
> 2. **v4.1–v4.5 engine spec copies** (#70)
> 3. **v4.6 Phase 5 economy alignment addendum** (#356)
> 4. **Phase 8B v4.6 buildings alignment addendum** (#355)
> 5. **v4.6 Phase 9 bandit redesign addendum** (#341)
> 6. **Phase 10 spec-compliance UAT review** (#345)
> 7. **Phase 3 Foundry test specification** (#115)
> 8. **Hackathon coding rules** — minimal tests + env var simplicity (#18)
>
> The spec evolved through 5 named versions during the build — `CANONICAL_SPEC` is the *current source of truth* for every conflict, and the alignment addenda capture *exactly* what changed between versions and *why*.

- `CANONICAL_SPEC`, `DEMO_DRIFT`, v4.1–v4.5 engine spec copies (#70)
- Phase 3 Foundry test specification (#115)
- v4.6 Phase 5 economy alignment addendum (#356)
- Phase 8B v4.6 buildings alignment addendum (#355)
- v4.6 Phase 9 bandit redesign addendum (#341)
- Phase 10 spec-compliance UAT review (#345)
- Hackathon coding rules — minimal tests + env var simplicity (#18)

---

[1.0.0]: https://github.com/OmniPass-world/clan-world/compare/world-build-submission-1...v1.0.0
