# ClanWorld V3 Active Build Plan

V3 is focused on the OpenAgents Track 2 demo: Base Sepolia chain state, 0G
memory/iNFT identity, AXL whispers, and KeeperHub heartbeat. Historical
hackathon material is archived under `docs/archive/`.

## Active Milestones

1. **Base Sepolia engine**
   - Keep the diamond deployment buildable.
   - Keep `IClanWorld` ABI generation in sync with shared adapters.
   - Drive heartbeats through the runner/foundry loop in dev and KeeperHub for live runs.

2. **Convex live state**
   - Webhook-first indexer with polling as safety net.
   - Frontend reads from Convex by default.
   - Fake heartbeat and demo data stay opt-in for local UAT only.

3. **Elder runner**
   - Cycle A heartbeat scheduler.
   - Cycle B per-tick situation blocks and order submission.
   - File-backed defaults with 0G memory available behind explicit config.

4. **iNFT transfer demo**
   - Mint clan agent identity.
   - Play through visible ticks.
   - Transfer ownership mid-game.
   - Show memory/identity continuity in cockpit and owner tooling.

5. **AXL communications**
   - Elder-to-Elder whispers.
   - Owner steering messages.
   - Cockpit comms view wired to live Convex tables.

## Validation Gates

- `pnpm --filter @clan-world/web typecheck`
- `pnpm --filter @clan-world/web build`
- `cd apps/server && npx convex dev --once --typecheck=disable`
- `cd packages/contracts && forge build --skip test`

Keep tests minimal and happy-path weighted per `docs/conventions/hackathon-rules.md`.
