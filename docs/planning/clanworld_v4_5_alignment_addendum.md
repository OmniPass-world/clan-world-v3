# ClanWorld v4.5 Addendum — Alignment Notes

**Status:** Authoritative addendum
**Read order:** This doc supersedes any conflicting language in the prior 7 spec docs. When implementers find disagreement, this doc wins.
**Purpose:** Capture all alignment issues introduced by the v4.5 keeper spec and the multi-submission hackathon timeline, without rewriting the existing specs.
**Audience:** Project manager, all stream leads.

---

## 0. Reading order for the full spec set

When onboarding a new agent or reviewer, read in this order:

### Core game spec (read for both submissions)

1. `clanworld_v4_spec.md` — base game mechanics
2. `clanworld_v4_1_addendum.md` — locked clarifications
3. `clanworld_v4_2_state_schema_interface_spec.md` — contract schema
4. `clanworld_v4_3_schema_patch.md` — schema lockdowns
5. `clanworld_v4_4_ui_indexer_getters.md` — UI aggregator getters
6. `IClanWorld.sol` — seam interface (canonical)

### Submission 1 build docs (World mini app, earlier deadline)

7. `clanworld_v4_5_keeper_integration_spec.md` — keeper integration (Foundry loop only for Submission 1)
8. `clanworld_frontend_spec.md` — frontend architecture
9. `clanworld_convex_backend_spec.md` — backend architecture
10. `clanworld_agent_runner_spec.md` — agent runner *(see §11 of THIS addendum: tool-use architecture in §7-§11 of agent runner spec is deprecated; Submission 1 uses the simpler `elder` CLI per §11.4 below)*
11. `clanworld_master_coordination.md` — cross-stream coordination
12. **This doc (`clanworld_v4_5_alignment_addendum.md`)** — alignment notes; supersedes conflicts above

### Submission 2 source materials (later deadline)

When Submission 2 work begins (after Submission 1 ships), read:

13. `clanworld_0g_battleplan_and_spec.md` — OpenAgents Track 2 strategy + 7-step plan
14. `clanworld_clan_memory_spec.md` — `ClanMemory` library wrapping 0G Storage KV
15. `clanworld_clan_identity_spec.md` — `ClanIdentity` library wrapping iNFT encrypted blob
16. `clanworld_inft_deployment_notes.md` — ERC-7857 reference impl annotations + deployment runbook
17. `AXL-integration-spec.md` — Gensyn AXL whisper transport + identity binding
18. `clanworld_v4_5_keeper_integration_spec.md` §2 — KeeperHub on Base Sepolia (now in scope)
19. **`clanworld_submission2_master_spec.md`** — unified Submission 2 plan (placeholder; written when Submission 1 ships)

---

## 1. Hackathon submission timeline — the actual plan

The project is delivering **two hackathon submissions** at different deadlines, plus follow-up integrations:

### 1.1 Submission 1 — World mini app (earlier deadline)

**Scope:**
- ClanWorld game working end-to-end on **World Chain Sepolia**
- Frontend wrapped as a **World mini app** using MiniKit / World SDK
- 4 LLM agents running locally as separate Claude Code instances
- Heartbeat driven by a local Foundry shell loop on the operator's machine
- Convex backend running with a single deployment pointed at World Chain Sepolia

**Out of scope for Submission 1:**
- KeeperHub integration
- Base Sepolia deployment
- 0G storage / iNFT
- AXL messaging
- Any of the larger hackathon's bounty integrations

**Definition of done:** working demo recordable on a phone via World App, showing all 8 demo moments from `clanworld_v1_implementation_profile.md` §8.

### 1.2 Submission 2 — Larger hackathon (5 days later)

**Scope adds:**
- Re-deploy contracts and stack on **Base Sepolia**
- KeeperHub workflow driving heartbeat at 60s cadence
- 0G storage integration (likely for sprite assets, agent reasoning logs, or game state archival — TBD per stream)
- 0G iNFT integration (likely for clan iNFT minting — TBD per stream)
- 0G compute integration (optional, TBD)
- AXL messaging integration (likely for cross-clan whispers — TBD per stream)
- Game continues running live post-submission as a public demo

**Out of scope for Submission 2:**
- World mini app frontend wrapping (Submission 1's UI is the canonical client; if it works in a regular browser, it works in the World app and vice versa — the wrapping does not block Base Sepolia operation)
- Production mainnet deployment
- Multiplayer scaling beyond 8 clans

### 1.3 Implication: only one realm runs at a time

Per the user's confirmation: **the two realms are never both live simultaneously.** Submission 1 runs on World Chain Sepolia. After that submission ships, the same codebase is redeployed and reconfigured for Submission 2 on Base Sepolia.

This dramatically simplifies the coordination story:

- **One frontend codebase**, parameterized by chain config at build/deploy time.
- **One Convex deployment**, reconfigured between submissions if needed (or two deployments toggled by env var, also fine).
- **One agent runner harness** with chain-specific configs swapped in.
- **One Makefile** with per-chain deploy targets.
- **No cross-chain coordination logic ever needed.**

This contradicts the earlier reading of the keeper spec that suggested two parallel stacks. It is the correct simplification.

---

## 2. Tick cadence — per-realm decision

### 2.1 Resolution

| Submission | Chain | Cadence | Driver |
|---|---|---|---|
| Submission 1 (World) | World Chain Sepolia | **20s/tick** | Foundry shell loop |
| Submission 2 (Base) live demo | Base Sepolia | **60s/tick** | KeeperHub workflow |
| Local dev / testing on either chain | (any) | **20s/tick or faster** | Foundry shell loop |

### 2.2 Why the difference

KeeperHub's cron has a 1-minute floor (`* * * * *`). For the live KeeperHub demo on Base Sepolia, 60s/tick is what KeeperHub natively supports. Submission 2 plans to leave the game running publicly post-submission, so the KeeperHub-driven 60s cadence is the steady-state production setting.

For Submission 1 and for all local development, we want faster ticks so the demo recording and dev iteration are tight. A Foundry shell loop has no cadence floor — `sleep 20` works fine.

### 2.3 Configs that need to know

| Config location | World Chain (Sub 1) | Base Sepolia live (Sub 2) | Local dev |
|---|---|---|---|
| Frontend `tickEpoch.durationMs` (from Convex) | 20000 | 60000 | 20000 |
| Backend `config.tickDurationMs` | 20000 | 60000 | 20000 |
| Agent runner `config.tickDurationMs` | 20000 | 60000 | 20000 |
| Agent runner `config.agentThinkBudgetMs` | 12000 | up to 45000 (much more headroom at 60s) | 12000 |
| Keeper schedule | `sleep 20` | `* * * * *` cron | `sleep 20` |

The cadence is a single env var (`TICK_DURATION_MS`) read by all four streams. Already specced. Confirmed correct here.

### 2.4 Side benefit at 60s cadence

LLM agents have **3× more thinking budget per tick**. Reasoning quality goes up, more elaborate whispers, better strategic depth. The Submission 2 demo (post-submission live game) will look smarter than Submission 1's, which is the right direction.

---

## 3. Heartbeat caller — supersedes backend spec §6

The earlier backend spec made the Convex `heartbeatCaller` cron the primary mechanism for advancing ticks. **This is now the fallback.**

### 3.1 Primary heartbeat caller per submission

| Submission | Primary caller | Fallback |
|---|---|---|
| Submission 1 (World, local dev) | Foundry shell loop (`scripts/start-heartbeat-loop.sh`) | Convex `heartbeatCaller` cron, off by default |
| Submission 2 (Base, live demo) | KeeperHub workflow | Convex `heartbeatCaller` cron, off by default |
| Submission 2 (Base, local dev pre-submission) | Foundry shell loop | Convex `heartbeatCaller` cron, off by default |

### 3.2 Convex `heartbeatCaller` semantics

The code stays in the codebase per backend spec §6. It is feature-flagged off by default (`HEARTBEAT_CALLER_ENABLED=false` in `.env`). Two reasons it stays:

1. **Disaster recovery during demo.** If the Foundry loop or KeeperHub workflow stops, an operator can flip the flag and Convex resumes ticking the world from a safe place.
2. **Self-contained CI / smoke tests.** Test environments can run the full stack with no external keeper.

Per v4.5 §5.1, running multiple keepers simultaneously is safe (the contract is self-rate-limiting), so even an accidental double-fire is harmless.

### 3.3 Configuration

```bash
# Submission 1 / Submission 2 dev — Foundry loop drives heartbeat
HEARTBEAT_CALLER_ENABLED=false
KEEPER_MODE=foundry-loop

# Submission 2 live demo — KeeperHub drives heartbeat
HEARTBEAT_CALLER_ENABLED=false
KEEPER_MODE=keeperhub

# Disaster fallback — Convex drives heartbeat
HEARTBEAT_CALLER_ENABLED=true
KEEPER_MODE=convex
```

These are coordination labels for the operator, not flags the code reads (other than `HEARTBEAT_CALLER_ENABLED`). The Foundry loop and KeeperHub workflow are managed externally.

---

## 4. Indexer trigger model — supersedes backend spec §5

The earlier backend spec described two indexer loops:

- **State-poll** — triggered by indexed `TickAdvanced` event, refreshes snapshot tables
- **Event-poll** — runs every 5s, processes new logs since checkpoint

The keeper spec adds a third signal: a **post-tick webhook** fired by KeeperHub or the Foundry loop after each successful `heartbeat()` tx.

### 4.1 Resolution

The webhook becomes the **primary trigger** for both loops. The 5s poll stays as a **safety net** in case the webhook fails or arrives late.

### 4.2 New flow

1. Keeper (Foundry loop or KeeperHub) calls `heartbeat()` and the tx confirms
2. Keeper fires `POST /api/heartbeat-webhook` with `{ chain, engineAddress, txHash, firedAtTs, source }`
3. Convex receives the webhook via an HTTP action
4. The HTTP action runs both:
   - The event indexer's "process logs since checkpoint" routine
   - The state indexer's "refresh all snapshot tables" routine
5. The 5s event-poll cron continues to run as a fallback. If the webhook arrived first and processed the same logs, the 5s poll is a no-op.

### 4.3 Why webhook-primary

- Lower latency than the 5s poll (webhook fires within ~1s of tx confirmation)
- Eliminates the race between `TickAdvanced` event indexing and snapshot refresh — both happen in the same handler invocation
- Aligns with the keeper architecture; the keeper now does something useful beyond just calling the contract

### 4.4 Webhook payload — minimal

Per recommendation:

```json
{
  "chain": "base-sepolia" | "worldchain-sepolia",
  "engineAddress": "0x...",
  "txHash": "0x...",
  "firedAtTs": 1735689600,
  "source": "keeperhub" | "foundry-loop"
}
```

**Do not include `currentTick`.** Adding it would require an extra RPC read after the tx confirms — added latency, added failure mode. Convex re-derives `currentTick` from the chain via its existing `getWorldSnapshot` call. The webhook is a wake-up signal, not authoritative game state.

### 4.5 Webhook auth

For hackathon: a shared secret in a header. Both keepers (Foundry loop and KeeperHub) include it; the Convex HTTP action rejects requests without it.

```bash
# .env
WEBHOOK_SHARED_SECRET=...
```

KeeperHub workflows can include arbitrary HTTP headers. The Foundry loop's `curl` call adds `-H "Authorization: Bearer $WEBHOOK_SHARED_SECRET"`. Trivial, no signing infrastructure required.

### 4.6 Webhook on revert

Per v4.5 §2.4, KeeperHub may fire the webhook even if the contract write reverts. This is fine — the indexer's "process logs since checkpoint" routine is idempotent. If no new logs exist, nothing happens. The state poll is also idempotent. Wasted work but harmless.

---

## 5. World mini app — Submission 1 frontend wrapping

The keeper spec defers all World mini app integration to "the World hackathon submission but does not affect the keeper integration." That deferral was correct for the keeper layer. **It is not correct for the frontend layer.**

### 5.1 What needs to be confirmed (PM action item)

Before frontend M5 (Convex wiring), the team needs answers from the World hackathon docs:

- **Does the submission require MiniKit SDK integration?** If yes, what surface area — auth, payments, share, all of the above?
- **Does the submission require World ID verification?** If yes, where in the UX flow?
- **Are there UI/UX requirements** (specific viewport, navigation patterns, theming) for mini apps?
- **Is the app distributed via the World App's directory**, or via direct URL?
- **Does the submission require any specific onchain interaction** (e.g., proving World ID humanity for clan minting)?

### 5.2 Frontend impact, depending on answers

**Most likely:** the mini app wrapping is a thin layer on top of the existing frontend — install MiniKit, add a World ID gate at app entry (one-time check), make sure the viewport works in World App's webview. ~4 hours of work.

**Less likely but possible:** World requires deeper integration — clans must be minted by verified humans, identity surfaces in the UI, specific payment flows. ~1–2 days of work.

### 5.3 Recommendation

Add this to master coordination §2 as a 🔴 blocking question for Submission 1. Get the answer before frontend M5 starts. If it's "minimal MiniKit wrapping," fold the work into frontend M6 polish. If it's heavier, scope it as a dedicated milestone.

This answer also affects whether World ID becomes a clan-minting requirement, which would touch the contract layer (one of the open questions for Submission 2's larger hackathon as well — defer until we know).

---

## 6. Submission 2 integrations — defer specs until Submission 1 ships

The Submission 2 hackathon adds 0G storage, 0G iNFT, possibly 0G compute, AXL messaging, and KeeperHub. Each of these likely warrants its own short integration spec when the time comes.

### 6.1 Don't write those specs yet

Reasons:

- We don't know the exact bounty requirements yet (judges' criteria, required SDK touchpoints, etc.)
- Submission 1 must ship before Submission 2 work begins, or both fail
- Premature specs become stale or constrain the actual integrations in unhelpful ways

### 6.2 What to track now

In master coordination §2, add three open questions tagged 🟢 (not blocking, but flagged):

- **0G integration scope.** What does the bounty require? What's the minimum integration that qualifies? Likely candidates: iNFT contract for clans, asset storage for sprites/agent logs, optional compute for LLM hosting.
- **AXL messaging scope.** Is this the cross-clan whisper layer? If yes, replaces the Convex `whispers` table. If just a notification surface, keeps Convex.
- **KeeperHub workflow.** Confirm the Base Sepolia workflow per v4.5 §2 is exactly what the bounty requires; check whether they want richer integration (e.g., conditional triggers based on game state).

These are reminders to revisit, not blocking items.

### 6.3 Architectural impact preview

Some Submission 2 integrations may displace existing Submission 1 architecture. Likely candidates:

- **iNFT minting via 0G** could replace the simple `mintClan` flow — clan iNFTs would be minted on the 0G iNFT contract, with a proof passed to ClanWorld at clan-spawn time. The seam may need a new entrypoint like `claimClan(uint256 ogTokenId, bytes proof)`.
- **AXL messaging** could replace the `whispers` table entirely. If AXL is the canonical cross-clan messaging layer, the Convex table becomes a cache/index instead of the source of truth.
- **0G compute** could replace local LLM calls. If agents run their LLMs through 0G compute, the agent runner's `LLMClient` swaps out the Anthropic/OpenAI client for a 0G-hosted endpoint.

None of these block Submission 1. All of them are reasons to keep the existing architecture loosely coupled — which it already is.

---

## 7. Master coordination doc — items to update post-addendum

If/when the master coordination doc is revised, these are the changes to make. **For now, this addendum supersedes:**

### 7.1 §1 Stream Contracts updates

Add to **Contracts owes:**
- ⏳ Contract deployed twice (World Chain Sepolia for Submission 1, Base Sepolia for Submission 2)

Add to **Backend owes:**
- ⏳ `/api/heartbeat-webhook` HTTP action (Convex internal action exposed via HTTP)
- ⏳ Per-chain config support (one Convex deployment, redeployed/reconfigured between submissions)

Add to **Frontend owes:**
- ⏳ MiniKit SDK integration if required by World mini app (see §5)

Add to **Ops owes:**
- ⏳ `start-heartbeat-loop.sh` script for Foundry-driven local heartbeat
- ⏳ KeeperHub workflow configuration for Base Sepolia (Submission 2)
- ⏳ Webhook shared secret in `.env`

### 7.2 §2 Open Questions updates

Resolve:
- §2.13 (tick duration) → resolved per §2.1 of this doc

Add:
- 🔴 [PM + FRONTEND] World mini app wrapping requirements (see §5.1 of this doc)
- 🟢 [PM] 0G integration scope for Submission 2
- 🟢 [PM] AXL messaging scope for Submission 2

### 7.3 §3 Dependency Graph updates

The graph applies to **one realm at a time.** Add a note: "This graph runs once for Submission 1 (World Chain Sepolia) and again for Submission 2 (Base Sepolia). Same codebase, different chain configs."

The keeper layer is now a top-level node:

```
[Keeper layer]
   ├── Foundry shell loop (Submission 1, all dev)
   ├── KeeperHub workflow (Submission 2 live)
   └── Convex heartbeatCaller (fallback only)
        │
        ▼
   calls heartbeat(), fires webhook
        │
        ▼
[Convex /api/heartbeat-webhook]
        │
        ▼
   triggers indexer + state poll
```

### 7.4 §7 Risk Register updates

Add:
- "World mini app integration scope unknown" — Medium / Medium — Mitigation: confirm requirements ASAP, scope as M6 polish if minimal
- "Submission 1 / Submission 2 coordination — same code, two configs" — Low / Low — Mitigation: env-var-driven chain config from day one; never hardcode chain ID

Update:
- "Onchain tx reverts cascading" — note that keeper-side reverts (heartbeat called too early) are now a separate failure mode; harmless but log-noisy

---

## 8. Makefile — items to update

Per-chain targets needed:

```makefile
deploy-worldchain-sepolia:
deploy-base-sepolia:

start-heartbeat-loop:           # Foundry shell loop, reads CHAIN env var
configure-keeperhub:             # script that uploads/updates KeeperHub workflow

# existing targets stay; add chain awareness:
fund-agents:                     # uses CHAIN env to pick the right RPC and wallet set
spawn-clans:                     # ditto
demo-reset:                      # ditto, reads CHAIN to know which chain to reset
```

Single env var (`CHAIN=worldchain-sepolia` or `CHAIN=base-sepolia`) drives all per-chain behavior.

---

## 9. Locked summary

**Note:** This summary has been superseded by §14 below, which incorporates the agent architecture realignment from the 0G integration specs. Read §14 for the current locked decisions.

- **Two submissions, one stack, one realm at a time.** Same codebase, chain config swapped between submissions.
- **Submission 1 (World) is first.** World Chain Sepolia, 20s ticks, Foundry-loop heartbeat, MiniKit-wrapped frontend, local agents.
- **Submission 2 (Base) is later.** Base Sepolia, 60s ticks, KeeperHub heartbeat, plus 0G + AXL integrations TBD.
- **KeeperHub is primary heartbeat for Submission 2 live.** Foundry loop is primary for Submission 1 and all dev. Convex heartbeatCaller is fallback only.
- **Webhook is the primary indexer trigger.** 5s poll stays as fallback. Webhook payload is minimal.
- **World mini app integration scope is the most urgent open question.** PM must clarify before frontend M5.
- **0G and AXL specs are deferred.** Add them after Submission 1 ships.
- **No cross-chain coordination ever exists.** No bridges, no relays, no shared state. Each realm is independent and self-contained.

---

## 10. What to do next

If you're the project manager:

1. **Confirm World mini app requirements** before frontend M5. Read the World hackathon docs and update master coordination §2 with answers.
2. **Pick the chain config approach.** Single Convex deployment with env var swap, or two deployments. Either works. I lean single, swap on each submission boundary.
3. **Update master coordination §2.13** to reflect the per-realm cadence resolution from §2.1 of this doc.
4. **Schedule the kickoff call** before any other stream starts work. Resolve remaining 🔴 items from master coordination §2 plus the new World mini app one from §5.1 here.

If you're a stream lead:

1. **Read this doc top to bottom** before starting work.
2. **Use 20s ticks for everything you build.** Submission 2's 60s adjustment is a single env var change at the end.
3. **Don't hardcode chain ID anywhere.** All chain config flows through env vars from day one.
4. **Code the webhook handler in Convex** as part of backend M3 (event indexer), not as a later task.

---

## 11. Agent architecture realignment — supersedes agent runner spec §7-§11

The 0G battleplan (`clanworld_0g_battleplan_and_spec.md` §5, §7, §9) introduces a fundamentally different agent runtime model than what's in `clanworld_agent_runner_spec.md`. The new model is correct. The agent runner spec's tool-use architecture is **deprecated**.

This change applies to **both submissions**, with different scope:
- **Submission 1** uses the new vocabulary with a minimal CLI surface.
- **Submission 2** uses the full `elder` toolbelt described in the 0G battleplan.

### 11.1 What changed

| Layer | Old (agent runner spec §7-§11) | New (0G battleplan §5, §9) |
|---|---|---|
| Process model | Single Node process running N agents in-process | One Claude Code session per clan, separate processes |
| LLM tool surface | Custom Anthropic tool-use schema (`dispatch_missions`, `send_whisper`, `transfer_resources`, `do_nothing`) | `elder *` CLI commands invoked via Bash by the Claude Code session |
| Decision loop | Per-tick `observe → think → act` polling Convex | Long-running Claude Code session with `<situation>` blocks pumped into stdin by an orchestrator |
| Context | Full observation rebuilt every tick | Continuous; reset every ~10 ticks deliberately to force memory consolidation |
| Memory | None (stateless across ticks) | `elder mem` against 0G KV (Submission 2 only); Submission 1 uses Convex agent logs as a weaker substitute |
| Identity | Hardcoded system prompt + agent runner config | Encrypted iNFT blob loaded at boot (Submission 2); CLAUDE.md file (Submission 1) |
| Wallets | One per agent, gas + game txs | **Two per clan** — agent wallet (gas, game txs, KV writes) + owner wallet (iNFT control); Submission 1 may use single wallet shortcut |
| Spawner | The agent runner Node process | An orchestrator process that boots Claude Code sessions and pumps situations |

### 11.2 New vocabulary (adopt across both submissions)

The 0G battleplan introduces vocabulary that's clearer than what was in the agent runner spec. Use these terms going forward:

| Term | Meaning |
|---|---|
| **Elder** | The autonomous agent controlling a clan. One per clan, runs as a long-running Claude Code session. |
| **Toolbelt** | The `elder *` CLI commands the Elder uses to interact with the world. |
| **Orchestrator** | The process that spawns Elder sessions, pumps situations, handles resets, and bridges external transports (AXL, etc.). |
| **Situation block** | An XML-tagged context update pumped into the Elder's stdin by the orchestrator. Trigger conditions in 0G battleplan §9. |
| **`<whisper_from_god>`** | Owner→Elder chat message; ephemeral, pumped as a block at the front of the next situation. |
| **Persistent Strategy & Notes** | Owner-edited tail section of CLAUDE.md. Submission 2 only. Submission 1 hardcodes equivalent guidance into the static CLAUDE.md. |
| **Notebook** | Agent-written freeform notes in 0G KV under `clan:{id}:notebook/*`. Submission 2 only. |

The deprecated terms from the agent runner spec — "agent runner," "AgentObservation," "ToolCall," "agent class" — should not be used in any new doc.

### 11.3 What stays valid in the agent runner spec

Despite the architectural shift, the following parts of `clanworld_agent_runner_spec.md` remain accurate and should be kept as reference:

- **§4 Wallet Model** — wallet derivation from mnemonic, BIP-39 paths, funding, mapping file. Still correct.
- **§8.2 Strategic alignment seeds** — Aldric/Mira/Brennan/Sora archetypes are pure prompt content. Still useful for both submissions; just paste them into CLAUDE.md instead of injecting via `system_prompt`.
- **§14 Logging and observability** — log rules, demo dashboard. Still valid (log lines come from the orchestrator now instead of the agent runner).
- **§15 Failure modes table** — most still applies (LLM timeout, tx revert, etc.).

### 11.4 Submission 1 — minimal `elder` CLI

For Submission 1, the toolbelt is a slim subset of the full 0G version. Submission 1 has:

```
elder world snapshot              # getWorldSnapshot
elder world clan [clanId]         # getClanFullView
elder world bandit                # getActiveBanditView
elder world market                # getMarketState
elder world all                   # convenience

elder act <orders.json>           # submitClanOrders + log to Convex agentLogs

elder say whisper <toClanId> <body>     # writes to Convex whispers (no AXL)
elder say broadcast <body>              # writes to Convex whispers with toClanId=null

elder chat tail [--clan <id>]    # tail recent whispers
```

**Not in Submission 1 toolbelt:** `elder mem`, `elder strategy`, `elder say bulletin`, AXL whispers, iNFT operations.

The Submission 1 orchestrator is also minimal:
- Spawns 4 Claude Code sessions with 4 different CLAUDE.md files
- Pumps situation blocks based on the same triggers as 0G battleplan §9 (waiting clansmen, new whisper, mission completion, tick advanced + 60s heartbeat)
- Resets sessions every ~10 ticks (still useful even without 0G memory; produces cleaner reasoning)
- Watches for crashes; restarts on failure
- Reads game state from Convex; submits transactions via the Elder wallet

The single-wallet shortcut (battleplan §9) is acceptable for Submission 1.

### 11.5 Submission 2 — full `elder` toolbelt

Submission 2 adopts the full 0G battleplan §5 toolbelt:

```
elder world *                     # same as Submission 1
elder act ...                     # adds mission attestation auto-write to KV
elder mem read|write|list|delete  # 0G KV memory
elder say whisper ...             # via AXL transport, mirrors to Convex
elder say bulletin <slot> <body>  # 0G KV town square
elder strategy read               # read CLAUDE.md persistent strategy tail from iNFT
```

The Submission 2 orchestrator adds:
- Cross-chain client (Base for game state, 0G for iNFT updates)
- AXL bridge sidecar for whisper polling and inbox indexing
- DEK file reading for iNFT decryption
- TEE attestation capture (if Option β model selected — see battleplan §10)
- Two-wallet model: agent wallet + owner wallet, never co-located

### 11.6 Implications for other streams

**Frontend impact:**
- Submission 1: speech bubbles read from Convex `agentLogs` table (already specced)
- Submission 2: speech bubbles still read from Convex; Convex now mirrors agent logs via the toolbelt's writes during `elder act`
- Submission 2 adds: Town Square panel (3 slots × N clans, reads from 0G KV bulletins via Convex mirror), Owner Editor UI (separate route, wallet-connected, edits iNFT metadata), Transfer Demo flow (the killer Track 2 moment)

**Backend impact:**
- Submission 1: Convex schema is unchanged from the existing backend spec. Agent logs and whispers tables are written by the toolbelt instead of the deprecated agent runner.
- Submission 2: AXL `messages` table replaces `whispers`. New `clans_directory` table. New `bulletins` table mirroring 0G KV. New `mission_attestations` table mirroring KV-stored TEE signatures.

**Contracts impact:**
- Submission 1: no changes from existing IClanWorld.sol.
- Submission 2: add `setClanAgent(clanId, newAgentAddress)` to IClanWorld; modify `mintClan` to accept an `iftTokenId` parameter (so the deployer can pass the real 0G iNFT token id); deploy `AgentNFT` (ERC-7857) on 0G mainnet with `MockOracle` and the `updateMetadata()` extension.

### 11.7 Rewriting the agent runner spec

`clanworld_agent_runner_spec.md` should be **superseded**, not edited. The deprecation note in §0 of this addendum points readers at this section. When Submission 1 work begins, write a small replacement: `clanworld_elder_orchestrator_spec.md` (~30% the length of the agent runner spec, since most of the runtime detail is delegated to Claude Code itself).

For now: leave the agent runner spec in the project. Note its deprecated status in the reading order (above). Don't waste time on edits.

---

## 12. Submission 2 master spec — placeholder

**Status:** Skeleton only. Do not start writing detail until Submission 1 ships.

When Submission 2 work begins, write `clanworld_submission2_master_spec.md` covering the integrations and architectural changes described across the 0G + AXL + KeeperHub source materials. The master spec is the single landing page; detailed designs live in the source spec docs (which are already written).

### 12.1 What the master spec will cover

A unified Submission 2 plan that resolves the current 4-doc fragmentation by writing a single coordination doc with these sections:

1. **Submission 2 scope and prize narrative** — OpenAgents Track 2 + larger hackathon's other bounties, demo punchline (iNFT transfer with intelligence intact)
2. **Updated reading order** — references to the 4 0G docs, AXL spec, KeeperHub spec
3. **Migration plan from Submission 1** — what changes, what stays, what's redeployed
4. **The orchestrator spec** — full version with situation pump, AXL bridge, DEK reading, multi-chain event listening, two-wallet model
5. **The full `elder` toolbelt spec** — replaces the deprecated agent runner spec
6. **Convex schema diff** — replace `whispers` with AXL `messages`, add `clans_directory`, add `bulletins`, add `mission_attestations`
7. **Frontend additions** — Town Square panel, Owner Editor UI, Transfer Demo flow
8. **Contract changes** — `setClanAgent`, `mintClan(tokenId)` parameterization, `AgentNFT` + `MockOracle` deploy on 0G mainnet
9. **Cross-stream open questions** — pending Discord answers (model selection, KV access control), pending bounty requirements
10. **Demo script** — the 3-minute Track 2 video, beat by beat, ending on the iNFT transfer moment

### 12.2 What the master spec will NOT do

- Restate the 4 0G docs, AXL spec, or KeeperHub spec. They're already detailed. The master spec is a coordination layer.
- Lock 0G's open Discord questions (`pc.0g.ai` auth model, KV access control) — those need answers from 0G first.
- Spec the Owner Editor UI in detail — it's a small standalone feature whose design will be obvious once the iNFT contract is deployed.

### 12.3 When to write it

After Submission 1 ships and the team has a working game with simple agents on World Chain Sepolia. At that point:

- The Submission 1 frontend is the basis for Submission 2 — we know what works
- The Submission 1 orchestrator is the basis for the more complex Submission 2 orchestrator — we know the pumping cadence and reset model are right
- Real LLM agent behavior is observed — strategic alignments work or don't, situation block format is good or needs revision
- The team has bandwidth to plan vs. ship

Best estimate: write this doc in the first 4 hours of Submission 2 work, then dispatch streams for the remaining 5 days.

### 12.4 Action items now (pre-Submission 2)

While Submission 1 is being built, the PM should track these so they don't surprise us later:

- **Discord answers from 0G** on the questions in battleplan §10 (model selection auth, KV access control)
- **Discord answers from Gensyn** on AXL endpoint surface (recv polling vs long-poll, send delivery confirmation)
- **OpenAgents Track 2 bounty criteria** — re-read closer to deadline to make sure the spec still meets criteria
- **Clarify `pc.0g.ai` Anthropic-compat** — only blocks Submission 2 if we're going to ship sealed inference

These are tracked in master coordination §2 as 🟢 items (non-blocking, but worth answering before Submission 2 work starts).

---

## 13. Owner Editor UI and Transfer Demo — Submission 2 standalone workstreams

The 0G battleplan calls out two frontend-adjacent components that are **orthogonal to the main game UI**. Track them as separate workstreams for Submission 2 planning.

### 13.1 Owner Editor UI

**What:** a wallet-connected page where the human owner edits the persistent strategy & notes tail of their clan's CLAUDE.md, signs the change with their owner wallet, and triggers an `updateMetadata()` tx on 0G iNFT contract.

**Why:** the Track 2 prize narrative includes "dynamic upgrades" — owners updating their agent's intelligence post-mint. Without this UI, the dynamic-upgrade story is hand-waved. With it, judges see a real demo.

**Scope:** ~1–2 days of frontend work for Submission 2.

**Cut priority:** #3 in battleplan §3.1 — first thing to cut if time runs short.

**Dependencies:** ClanIdentity library (read-write mode), wallet connector, iNFT contract deployed.

### 13.2 Transfer Demo flow

**What:** a scripted demo flow where:
1. Owner of clan 7 transfers iNFT to a new wallet
2. Old DEK is handed to the new owner UI (manual, contrived for `MockOracle` deploy)
3. The orchestrator detects the ownership change (or is manually restarted with new config)
4. The new clan 7 Elder boots with the same CLAUDE.md, same skills, same notebook
5. The new Elder's first action references something specific from the prior owner's gameplay (e.g., "based on my notes, clan 3 betrayed us at tick 412, do not trust them")

**Why:** this is **the entire Track 2 pitch** in 60 seconds. Without it, Track 2 is a gamble; with it, Track 2 is winnable.

**Scope:** ~half a day of scripting + UI work, but requires Owner Editor UI to exist.

**Dependencies:** iNFT contract, Owner Editor UI, working orchestrator with ClanIdentity boot path.

**Cut priority:** **NEVER cut.** This is the demo. If we cut Owner Editor UI, we still need a way to run this transfer flow even if it's a CLI script with no UI. The narrative cannot be cut.

### 13.3 Recommendation

Plan these as a single 2-day Submission 2 workstream owned by one person, after the main game UI Submission 2 polish is done. The Owner Editor UI is the prerequisite; the Transfer Demo is the payoff. Without both, Submission 2's Track 2 narrative is weaker.

---

## 14. Updated locked summary

- **Two submissions, one stack, one realm at a time.** Same codebase, chain config swapped between submissions.
- **Submission 1 (World) is first.** World Chain Sepolia, 20s ticks, Foundry-loop heartbeat, MiniKit-wrapped frontend, simple Claude Code Elders with minimal `elder` toolbelt, single wallet per agent, Convex `whispers` table.
- **Submission 2 (Base + OpenAgents Track 2 + others) is later.** Base Sepolia, 60s ticks, KeeperHub heartbeat. Plus: 0G iNFT (ERC-7857 with MockOracle) for Elder identity, 0G KV for agent memory + town square bulletins, AXL for private whispers, two-wallet model, Owner Editor UI, Transfer Demo. Plus possibly: 0G Compute sealed inference (model selection TBD).
- **Agent architecture is `Elder + orchestrator + toolbelt`.** Old "agent runner" tool-use architecture is deprecated. Both submissions use the new vocabulary; Submission 1 uses a minimal subset of the toolbelt.
- **KeeperHub is primary heartbeat for Submission 2 live.** Foundry loop is primary for Submission 1 and all dev. Convex heartbeatCaller is fallback only.
- **Webhook is the primary indexer trigger.** 5s poll stays as fallback. Webhook payload is minimal.
- **World mini app integration scope is the most urgent open question.** PM must clarify before frontend M5.
- **0G + AXL detailed integration specs exist** (4 docs for 0G, 1 for AXL) but are not folded into the main spec set yet. They activate when Submission 2 work begins.
- **Submission 2 master spec is a placeholder.** Write it after Submission 1 ships, in the first 4 hours of Submission 2 work.
- **No cross-chain coordination ever exists.** No bridges, no relays, no shared state. Each realm is independent and self-contained.
- **The Track 2 demo punchline (iNFT transfer with intelligence intact) is the entire Submission 2 narrative.** Build the Owner Editor UI + Transfer Demo flow as a dedicated workstream; cut anything else first.
