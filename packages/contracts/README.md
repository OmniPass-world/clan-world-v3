# @clan-world/contracts

Solidity contracts for ClanWorld. The canonical interface is `src/IClanWorld.sol` — every other component (frontend, backend, orchestrator, agents) talks to the chain through this seam. See `docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md`.

Contracts deploy to **Base Sepolia** for the active ClanWorld V3 build, with heartbeat driven by the runner/foundry loop in dev and KeeperHub for live demo flows.

## Workflow

```bash
forge build
forge test
forge script script/Deploy.s.sol --rpc-url $RPC_URL_PRIMARY --broadcast
```

`script/Deploy.s.sol` is a Wave 1 deliverable; the directory is empty in Wave 0.

## Layout

- `src/IClanWorld.sol` — canonical seam interface (24KB, do not modify without ADR)
- `foundry.toml` — Foundry config; reads `RPC_URL_PRIMARY` from env

## World Pause Policy

`pauseWorld()` is a true game-time freeze. While paused, gameplay entrypoints that advance ticks, settle state, alter player-owned clans, or commit ranking inputs revert with `ClanWorld: world paused`.

Blocked during pause:

- `heartbeat`
- `settleClan`, `settleClansman`
- `submitClanOrders`
- `mintClan`
- `transferClanOwnership`
- `transferGold`, `transferVaultResource`, `transferBlueprint`, `transferBundle`
- treasury writes: `initTreasury`, `seedPools`
- `finalizeSeason`

Allowed during pause, owner only:

- `triggerBanditSpawn` — queues a spawn for the first post-unpause heartbeat; no bandit spawns until the clock resumes
- `setHeartbeatIntervalSeconds`
- `setClansmanCooldownSeconds`
- ownership/admin controls such as `transferOwnership` and `diamondCut`

Rationale: queued admin actions that do not advance ticks are allowed so operators can stage recovery safely. Gameplay state changes and deterministic season/ranking commits are blocked until `unpauseWorld()` resumes the clock.

See `../../docs/guides/stream-contracts.md` for the full deploy workflow.
