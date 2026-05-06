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

See `../../docs/guides/stream-contracts.md` for the full deploy workflow.
