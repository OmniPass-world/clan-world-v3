# PR #153 Review — Full Dev Branch Integration

| Field | Value |
|-------|-------|
| **PR** | [#153](https://github.com/OmniPass-world/clan-world/pull/153) |
| **Title** | Dev - DO NOT MERGE this is only for code reviews |
| **Branch** | `dev` → `main` |
| **Size** | +14,441 / −553 across 96 files, 43 commits |
| **Review date** | 2026-04-28 |
| **Model** | Claude Opus 4.6 (Cursor) |
| **Methodology** | 11-agent swarm: 8 parallel Wave 1 (broad sweep) + 3 parallel Wave 2 (targeted deep dives) |

---

## Summary Stats

| Category | Count |
|----------|-------|
| **MUST FIX** | 3 |
| **SHOULD FIX** | 17 |
| **DEFER** (new GH issue) | 21 |
| **SKIP / FALSE POSITIVE** | 12 |
| **Total findings** | **53** |

---

## Triage Table

### MUST FIX — Blocking merge

| # | File | Line(s) | Finding | Domain |
|---|------|---------|---------|--------|
| 1 | `contracts/src/ClanWorld.sol` | 1093–1100 | **Stale queue guard incomplete for same-type mission replacement.** `ScheduledMarketAction` has no `missionNonce` field. Guard only checks `m.action != sma.action`. Replacing MarketSell(wood,100) with MarketSell(iron,50) for the same tick leaves the old queue entry valid — heartbeat executes both with stale parameters. **PR #137 Finding #1 — NOT FIXED.** Wave 2 Agent 2.2 traced full lifecycle confirming exploit: old row runs with wrong asset/size, corrupting vault/pool state. No test covers this scenario. | Correctness |
| 2 | `runner/src/runnerCastHeartbeat.ts` | 51–56 | **Runner heartbeat targets World Chain Sepolia (chainId 4801) instead of Base Sepolia (84532).** After the Base Sepolia chain pivot (PR #125), `RealChainClient`, `foundry.toml`, `start-heartbeat-loop.sh`, and `deployments/base-sepolia.json` all target chainId 84532. But `RunnerCastHeartbeat` still defines `worldChainSepolia` with `id: 4801`. Signed transactions carry wrong chain ID — they will fail, target the wrong network, or suffer replay-protection mismatch. Wave 2 confirmed this is stale config, not intentional. | Correctness |
| 3 | `agents/src/cli.ts` | 117–125 | **`peer whisper` path traversal.** `recipientInboxFile(clanId)` builds `path.join(..., 'peer-inbox', \`elder-${clanId}.jsonl\`)` with no sanitization. A `clanId` containing `../` can escape the inbox directory. Runner-side `assertSafeInboxKey` exists but CLI writer is unguarded. | Security |

### SHOULD FIX — Address in this PR before merge

| # | File | Line(s) | Finding | Domain |
|---|------|---------|---------|--------|
| 4 | `contracts/src/ClanWorld.sol` | 1408–1413 | **`PoolsSeeded` event argument order mismatches `IClanWorld`.** Interface declares `(wood, wheat, fish, iron)` but implementation emits `(wood, iron, wheat, fish)`. Indexers decoding by position assign wrong pool addresses. PR #137 #2 — NOT FIXED. | Correctness |
| 5 | `contracts/src/ClanWorld.sol` | 1049 | **Unbounded per-tick market queue.** No cap on `_scheduledMarketActions[tick].length`; griefing via permissionless `mintClan` + mass scheduling can OOG the heartbeat. PR #137 #3 — NOT FIXED. | Security |
| 6 | `contracts/script/Deploy.s.sol` | 15–37 | **Deploy script never calls `initTreasury` / `seedPools`.** Deployments leave market offline (`poolsSeeded == false`). PR #137 #7 — NOT FIXED. | Ops |
| 7 | `contracts/src/IClanWorld.sol` | — | **`initTreasury` not declared on `IClanWorld`.** Typed clients miss mandatory lifecycle step. PR #137 #4 — NOT FIXED. | API |
| 8 | `contracts/src/ClanWorld.sol` | 42–46 | **NatSpec still says "Phase 2 … are stubbed."** Phase 2 AMM economy is implemented. PR #137 #5 — NOT FIXED. | Docs |
| 9 | `contracts/src/ClanWorld.sol` | 1417–1437 | **OTC revert strings say "Phase 2".** Misleading since Phase 2 economy is now live; OTC is a separate unimplemented feature. PR #137 #6 — NOT FIXED. | Docs |
| 10 | `runner/README.md` | 8–18 | **Heartbeat loop pseudocode is stale.** Shows heartbeat inside tick loop; code decouples Cycle A (heartbeat scheduler) from Cycle B (tick loop) via `settleLatch`. PR #136 #12 — NOT FIXED. | Docs |
| 11 | `.env.template` | — | **Materially incomplete.** 16+ env vars read in code are missing: `RUNNER_PRIVATE_KEY`, `CHAIN_RPC_URL`, `CLAN_WORLD_RUNNER_STATE_DIR`, `RUNNER_ACK_TIMEOUT_MS`, `RUNNER_HEARTBEAT_CHECK_INTERVAL_MS`, `RUNNER_TMUX_SESSION_PREFIX`, `CLAN_ID`, `MY_CLAN_ID`, `VITE_CONVEX_URL`, `VITE_CLANWORLD_DEMO_MODE`, `VITE_WORLD_RP_ID`, `ELDER_WALLET_KEY_PATH`, `EVM_RPC`, `INDEXER_RPC`, `FLOW_CONTRACT`, `CONVEX_DEPLOY_URL`. Violates AGENTS.md §7. | Docs/Ops |
| 12 | `runner/src/filePeerInbox.ts` | 83–84 | **Read path `elderN` not validated.** `inbox()` uses `this.elderN` in `path.join` without `assertSafeInboxKey`. If `inboxKeyForClanId(ownClanId)` falls through to a raw clanId, defense-in-depth gap. | Security |
| 13 | `runner/src/fileMemoryStore.ts`, `zeroGMemoryStore.ts`, `axlPeerInbox.ts` | Various | **Sensitive files written without restrictive permissions.** `writeFileSync` / `appendFileSync` use default umask — cache, journal, memory files world-readable on shared hosts. Use `{ mode: 0o600 }`. | Security |
| 14 | `runner/src/main.ts` | 98 | **`{} as Record<ElderId, PerElderDeps>` unsafe type assertion.** Empty object widened before keys exist. PR #136 #6 — NOT FIXED. | Type Safety |
| 15 | `runner/src/axlPeerInbox.ts` | 519–520 | **`as ElderId` unsafe narrowing.** Factory fallback casts `parseInt(env['ELDER_N'])` to `ElderId` without validation. PR #136 #7 — NOT FIXED. | Type Safety |
| 16 | `web/src/WorldMap.tsx` | 324–548 | **`app.init().then(...)` has no `.catch`.** PIXI initialization rejection → unhandled promise rejection; `pixiReady` never set; error boundary won't catch (async, outside render). | Frontend |
| 17 | `web/src/WorldMap.tsx` | 705–718 | **`Assets.load(clan.sigil).then(...)` no mounted guard.** Late resolution after unmount calls `viewport.addChild(sprite)` on destroyed graph. Background load at L385 has this guard; sigil load does not. | Frontend |
| 18 | `orchestrator/package.json` | 7 | **`pnpm start` doesn't load `.env.local`.** `dev` has `--env-file=../../.env.local`; `start` is plain `tsx src/main.ts`. Production-style runs miss all env vars. | Ops |
| 19 | `.env.template` + `scripts/start-heartbeat-loop.sh` | Various | **Naming inconsistencies.** `CONVEX_URL` (adapters) vs `CONVEX_DEPLOY_URL` (shell script); `RPC_URL_PRIMARY` (IChainClient) vs `CHAIN_RPC_URL` (runner heartbeat). Operators configure one and miss the other. | Ops |
| 20 | `runtime/elders/template/run.sh.template` | ~20 | **Hardcodes repo path** `$HOME/code/omnipass-world/clan-world/packages/agents/bin`. Breaks on machines with different checkout locations. | Ops |

### DEFER — Valid concerns, open new GitHub issues

| # | File | Line(s) | Finding | Domain |
|---|------|---------|---------|--------|
| 21 | `runner/src/tickLoop.ts` | 198–207 | **`withTimeout` does not observe `AbortSignal`.** Shutdown depends on `deliveryAbort` + inbox honoring signal. Hung adapter can delay shutdown up to `deliveryTimeoutMs`. PR #136 #4 — deferred. | Correctness |
| 22 | `contracts/src/ClanWorld.sol` | 1102–1112 | **Catch-all `ERR_INVALID_ACTION` masks failure modes.** All pool/math/slippage reverts collapse to one code. PR #137 #8 — deferred. | Correctness |
| 23 | `runner/clanworld-runner.service` | 1–28 | **Missing systemd hardening.** No `PrivateTmp`, `ProtectSystem`, `NoNewPrivileges`, `CapabilityBoundingSet`, memory limits. | Security |
| 24 | `runner/src/axlPeerInbox.ts` | 167–217 | **AXL API key over cleartext HTTP for remote hosts.** Default loopback OK; no guard against non-TLS remote `AXL_NODE_URL`. | Security |
| 25 | `runner/src/zeroGMemoryStore.ts` | 66–72, 355–363 | **`JSON.parse(...) as Record<string, string>` without validation.** Corrupt/wrong-shape JSON silently becomes wrong types. | Type Safety |
| 26 | `.env.template` | 78–81 | **`OG_STORAGE_API_KEY` naming misleading.** It's a feature flag, not an API credential. PR #136 #5 — partially addressed (docs), rename still open. | Docs |
| 27 | `runner/src/tickLoop.ts` | (module) | **Zero test coverage for tick loop.** Core orchestration path has no tests. PR #136 #20 — still open. | Tests |
| 28 | `agents/src/__tests__/cli.test.ts` + `agents/test/cli.test.ts` | — | **Overlapping CLI test suites.** Two files with heavy overlap; different stubs/assertions. Double CI cost. Not exact duplicates — complementary but redundant. | Tests |
| 29 | `runner/test/filePeerInbox.test.ts` | 24–61 | **Temp dir never cleaned in afterEach.** `fs.mkdtempSync` allocations accumulate. | Tests |
| 30 | `web/tests/e2e/00-smoke.spec.ts` | 44–51 | **E2e relies on real dev server + timing.** `waitForSelector('canvas', { timeout: 10_000 })` + `waitForTimeout(500)` — sensitive to CI speed. Convex not stubbed in Playwright. | Tests |
| 31 | `contracts/test/ClanWorld.t.sol` | — | **`finalizeSeason()` untested.** Game-ending function has no dedicated test. | Tests |
| 32 | `web/src/components/cockpit/shared/WorldMapBoundary.tsx` | — | **Error boundary can't catch async errors.** PIXI init failures in `useEffect` bypass React error boundaries. | Frontend |
| 33 | `runtime/elders/Makefile` | 138–143 | **`make elders-up` fails from source tree.** `ELDER_DIR` points at repo `runtime/elders/` which has no `elder-N/` dirs; must run from installed `DEST`. | Ops |
| 34 | `runtime/elders/Makefile` + `systemd/ttyd-elder.service.template` | — | **Two sources of truth for ttyd units.** Embedded `printf` in Makefile vs checked-in template. Can drift. | Ops |
| 35 | `AGENTS.md` | 93–99 | **Per-package guides omit `apps/landing/AGENTS.md` and `packages/runner/AGENTS.md`.** Progressive discovery gap. | Docs |
| 36 | `docs/guides/stream-agents.md` | 34–47 | **Doesn't mention runner daemon.** Per-tick flow only describes orchestrator driving heartbeat. | Docs |
| 37 | `docs/planning/phase-3-test-spec.md` | 3–6 | **Status banner stale.** Says Phase 2 "pending"; Phase 2 is merged. | Docs |
| 38 | `docs/planning/DEMO_DRIFT.md` | 51–55 | **Stale claim about CLI.** Says agents CLI is "Wave 0 stub with only snapshot"; CLI has full subcommands. | Docs |
| 39 | `.env.template` | 89–94 | **AXL_PEER_ID naming mismatch.** Template documents `AXL_PEER_ID_1`…`4`; code builds keys via `AXL_PEER_ID_<CLAN_UPPER_SNAKE>` convention. | Docs |
| 40 | `runner/src/heartbeatScheduler.ts` | 52–66 | **Minor TOCTOU between `isHeartbeatDue` and `callHeartbeat`.** On-chain guard catches spurious attempts. | Correctness |
| 41 | `runner/src/tmuxRunnerInbox.ts` | 154–161 | **`waitForFile` lacks AbortSignal.** Bounded by `timeoutMs`; only matters on shutdown-sensitive paths. | Correctness |

### SKIP / FALSE POSITIVE

| # | File | Finding | Reason |
|---|------|---------|--------|
| 42 | `runner/src/filePeerInbox.ts` | PR #136 MUST FIX #2 (send-side path traversal) | **FIXED** — `assertSafeInboxKey` validates before write. |
| 43 | `runner/src/filePeerInbox.ts` + `main.ts` | PR #136 MUST FIX #3 (ELDER_N cross-talk) | **FIXED** — explicit `elder`/`myClanId` from `main.ts`; `ELDER_N` only honored when matching slot. |
| 44 | `web/src/WorldMap.tsx`, `config/env.ts`, `e2e tests` | PR #133 MUST FIX #1–3 (test skip, setTimeout, circular dep) | **ALL FIXED** — verified in Wave 1 and Wave 2. |
| 45 | `agents/src/seams/IRunnerInbox.ts` | PR #136 SHOULD FIX #10 (`DeliveryStatus` lacks `'aborted'`) | **FIXED** — `reason: ... \| 'aborted'` present; tmuxRunnerInbox uses it. |
| 46 | `runner/src/tickLoop.ts` 216–226 | `raceAbort` doesn't cancel underlying promise | By design; documented in comments. Bounded work. |
| 47 | `runner/src/runnerCastHeartbeat.ts` 131–154 | Rate-limit guard scope | Correctly scoped: only `ContractFunctionRevertedError` upgraded; pre-flight errors pass through. |
| 48 | `runner/src/heartbeatScheduler.ts` 55–66 | Heartbeat snapshot-before-call | Correctly implemented: `settledSnapshot` read before `callHeartbeat()`; assigned on success only. |
| 49 | `runner/src/tickLoop.ts` 90–171 | Option C retry-then-degrade | Correctly implemented: `Promise.all` safe (attemptElder catches); MAX_RETRIES=2; loud log on degrade. |
| 50 | `runner/src/settleLatch.ts` 16 | SettleLatch monotonicity | Correct: `markSettled` only raises `_tick` when `tick > _tick`. |
| 51 | `agents/src/__tests__/cli.test.ts` vs `test/cli.test.ts` | "Duplicate test files" | Not duplicates — complementary suites with different stubs. Overlap warrants consolidation (DEFER #28) but not a bug. |
| 52 | `apps/web/.vercel/project.json` | Committed Vercel config | Safe — contains only `projectId`/`orgId`, no tokens. Standard practice. |
| 53 | `runner/src/runnerCastHeartbeat.ts` 72–91 | Private key handling | Correct — validates format, normalizes `0x` prefix, no secrets logged. |

---

## Review Methodology

### Wave 1 — Broad Sweep (8 agents, parallel)

| Agent | Domain | Key Findings |
|-------|--------|--------------|
| 1.1 | Contracts Correctness | HIGH stale queue guard (#1); MEDIUM PoolsSeeded order (#4); MEDIUM unbounded queue (#5) |
| 1.2 | Security & Secrets | HIGH CLI path traversal (#3); MEDIUM file permissions (#13); MEDIUM AXL HTTP (#24) |
| 1.3 | Runner Correctness | MEDIUM withTimeout gap (#21); verified Option C retries, settle latch, raceAbort |
| 1.4 | Type Safety & Contracts | MEDIUM unsafe assertions (#14, #15); MEDIUM JSON.parse casts (#25); HIGH initTreasury (#7) |
| 1.5 | Frontend & Cockpit | MEDIUM async PIXI errors (#16); MEDIUM sigil load guard (#17); verified PR #133 fixes |
| 1.6 | Architecture & Integration | HIGH .env.template gaps (#11); HIGH chain config split (escalated to MUST FIX #2); HIGH orchestrator start (#18) |
| 1.7 | Test Coverage | HIGH tickLoop untested (#27); MEDIUM duplicate CLI tests (#28); MEDIUM finalizeSeason gap (#31) |
| 1.8 | Style, Conventions & Docs | HIGH NatSpec stale (#8, #9); HIGH README stale (#10); MEDIUM commit ref gaps |

### Wave 2 — Targeted Deep Dives (3 agents, parallel)

| Agent | Domain | Key Findings |
|-------|--------|--------------|
| 2.1 | Env Var Audit + Chain Config | **CONFIRMED CRITICAL** #2 (runner heartbeat wrong chain ID 4801 vs 84532); comprehensive 16-var gap audit |
| 2.2 | Stale Queue Guard Deep Dive | **CONFIRMED** #1 with full lifecycle trace — no missionNonce, no test for same-type replacement |
| 2.3 | runtime/elders + False Positives | MEDIUM Makefile footgun (#33); MEDIUM run.sh path hardcode (#20); confirmed chain mismatch not false positive; confirmed orchestrator env gap |

### Wave 3 — Not needed

Waves 1 + 2 provided comprehensive coverage. No findings required escalation.

---

## Key Wave 2 Resolutions

### CRITICAL Escalation: Runner Heartbeat Wrong Chain (Finding #2)

Agent 1.6 flagged a chain config split. Agent 2.1 and 2.3 both independently confirmed: `runnerCastHeartbeat.ts` defines `worldChainSepolia` with `id: 4801`, while every other component (IChainClient, foundry.toml, deployments/base-sepolia.json, start-heartbeat-loop.sh) targets Base Sepolia (`id: 84532`). This is stale configuration from before the Base Sepolia pivot (PR #125). Signed transactions carry wrong chain ID and will fail or target the wrong network.

### CONFIRMED: Stale Queue Guard (Finding #1)

Agent 2.2 traced the full lifecycle:
- `ScheduledMarketAction` struct has NO `missionNonce` field
- `_enqueueScheduledMarketAction` stores NO nonce
- `_executeScheduledMarketActions` guard is ONLY `m.action != sma.action`
- `Mission` struct HAS `nonce` but it is never compared to queue entries
- Same-type replacement (MarketSell→MarketSell) appends new row without invalidating old
- No test covers same-type replacement scenario

### Prior Review Rollup

| Prior review | Finding | Status in PR #153 |
|-------------|---------|-------------------|
| PR #137 #1 (MUST FIX) | Stale queue guard | **Not fixed** |
| PR #137 #2 (SHOULD FIX) | PoolsSeeded event order | **Not fixed** |
| PR #137 #3 (SHOULD FIX) | Unbounded queue | **Not fixed** |
| PR #137 #4 (SHOULD FIX) | initTreasury on IClanWorld | **Not fixed** |
| PR #137 #5 (SHOULD FIX) | NatSpec "Phase 2 stubbed" | **Not fixed** |
| PR #137 #6 (SHOULD FIX) | OTC revert strings | **Not fixed** |
| PR #137 #7 (SHOULD FIX) | Deploy script init calls | **Not fixed** |
| PR #137 #8 (SHOULD FIX) | Catch-all ERR_INVALID_ACTION | **Not fixed** |
| PR #136 #1 (MUST FIX) | Tick loop partial failure | **Fixed** (Option C retries) |
| PR #136 #2 (MUST FIX) | filePeerInbox path traversal | **Fixed** (assertSafeInboxKey) |
| PR #136 #3 (MUST FIX) | ELDER_N cross-talk | **Fixed** |
| PR #136 #4 (SHOULD FIX) | withTimeout abort-aware | **Not fixed** (deferred) |
| PR #136 #5 (SHOULD FIX) | OG_STORAGE_API_KEY naming | **Partially** (docs, not rename) |
| PR #136 #6 (SHOULD FIX) | Unsafe Record assertion | **Not fixed** |
| PR #136 #7 (SHOULD FIX) | ElderId narrowing | **Not fixed** |
| PR #136 #8 (SHOULD FIX) | AGENTS.md package count | **Fixed** |
| PR #136 #9 (SHOULD FIX) | .env.template runner keys | **Partially** (some added, many missing) |
| PR #136 #10 (SHOULD FIX) | DeliveryStatus 'aborted' | **Fixed** |
| PR #136 #12 (SHOULD FIX) | README pseudocode stale | **Not fixed** |
| PR #133 #1–3 (MUST FIX) | Test skip, setTimeout, circular dep | **All fixed** |

---

## Recommended Next Steps

### 1. Address MUST FIX items (3 items — do first)

**#2 — Runner heartbeat wrong chain** (highest priority — affects all on-chain operations)
- Change `runnerCastHeartbeat.ts` `defineChain` to use `id: 84532` (Base Sepolia)
- Align RPC URL naming: use `RPC_URL_PRIMARY` consistently or document `CHAIN_RPC_URL` as alias
- Update default RPC URL from Alchemy World Chain to Base Sepolia endpoint
- Consider importing `baseSepolia` from viem/chains instead of manual `defineChain`

**#1 — Stale queue guard** (economic integrity)
- Add `uint64 missionNonce` to `ScheduledMarketAction` struct
- Set it from `ctx.newNonce` in `_enqueueScheduledMarketAction`
- Require `m.nonce == sma.missionNonce` in `_executeScheduledMarketActions`
- Add regression test: two same-type market orders for same tick, verify only latest executes

**#3 — CLI peer whisper path traversal**
- Reuse runner's `assertSafeInboxKey` pattern (regex `/^[A-Za-z0-9_-]+$/`) in `recipientInboxFile`

### 2. Address SHOULD FIX items (17 items — before merge)

**Quick fixes** (< 15 min each):
- #4: Fix `PoolsSeeded` emit order to match IClanWorld: `(wood, wheat, fish, iron)`
- #7: Add `initTreasury` to `IClanWorld`
- #8: Update ClanWorld.sol NatSpec ("Phase 2 implemented")
- #9: Change OTC revert strings to "OTC transfers not implemented"
- #10: Update runner README pseudocode (Cycle A / Cycle B split)
- #18: Add `--env-file=../../.env.local` to orchestrator `start` script
- #20: Use `$ELDER_DIR` or relative path in `run.sh.template`

**Medium effort** (30–60 min each):
- #5: Add per-tick queue cap (e.g. `MAX_SCHEDULED_ACTIONS_PER_TICK = 100`)
- #6: Add `initTreasury` + `seedPools` calls to Deploy.s.sol
- #11: Extend `.env.template` with all 16+ missing vars
- #12: Add `assertSafeInboxKey(this.elderN)` in `FilePeerInbox.inbox()`
- #13: Set `{ mode: 0o600 }` on file writes for memory/journal/cache
- #14: Replace `{} as Record<...>` with builder/reduce pattern
- #15: Add `asElderId()` validation guard before casting
- #16: Add `.catch()` to `app.init().then()` in WorldMap
- #17: Add mounted guard to `Assets.load(clan.sigil).then()`
- #19: Unify `CONVEX_URL` / `CONVEX_DEPLOY_URL` and `RPC_URL_PRIMARY` / `CHAIN_RPC_URL`

### 3. Create DEFER issues (21 items)

Suggested grouping:
- **Security hardening**: #23 (systemd), #24 (AXL TLS)
- **Correctness**: #21 (withTimeout abort), #22 (ERR_INVALID_ACTION granularity), #40 (heartbeat TOCTOU), #41 (waitForFile abort)
- **Type safety**: #25 (JSON.parse validation)
- **Tests**: #27 (tickLoop coverage), #28 (CLI test consolidation), #29 (temp dir cleanup), #30 (e2e hermeticity), #31 (finalizeSeason)
- **Frontend**: #32 (error boundary async limitation)
- **Ops**: #26 (OG_STORAGE_API_KEY rename), #33 (Makefile footgun), #34 (unit file drift)
- **Docs**: #35 (per-package guide bullets), #36 (stream-agents runner mention), #37 (phase-3-test-spec status), #38 (DEMO_DRIFT staleness), #39 (AXL_PEER_ID naming)

### 4. Overall PR Health Assessment

**Status: NEEDS WORK**

The 3 MUST FIX items are blocking:
- **Finding #2** (wrong chain ID) is a **showstopper** — the runner daemon cannot call heartbeat on the correct network. This likely means the runner has never successfully fired a heartbeat on Base Sepolia.
- **Finding #1** (stale queue guard) is an **economic integrity bug** — stale market orders can execute with wrong parameters, corrupting vault and pool state.
- **Finding #3** (CLI path traversal) is a **security gap** — while impact is limited to local Elder file I/O, it follows a pattern already patched on the runner side.

The 17 SHOULD FIX items include 6 unfixed items from prior PR #137 review (contracts), 4 unfixed from PR #136 (runner type safety), and 7 new findings (frontend async errors, env completeness, ops gaps).

**Positive notes:** PR #136's 3 MUST FIX items are all resolved (tick loop retries, send-side path traversal, ELDER_N cross-talk). PR #133's 3 MUST FIX items are all resolved. Runner abort handling, settle latch, and heartbeat scheduler are well-implemented. Zero `any` in runner/agents code. Adapter pattern is clean with no circular dependencies.

After addressing the 3 MUST FIX items and the 17 SHOULD FIX items, the PR should be merge-ready.
