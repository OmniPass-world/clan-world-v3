# Prize Strategy

## Active Target

ClanWorld V3 targets the OpenAgents Track 2 narrative:

1. Mint an autonomous clan Elder identity as an ERC-7857-style iNFT.
2. Let the Elder play through live Base Sepolia ticks.
3. Transfer the iNFT mid-game.
4. Show the new owner inheriting the Elder's memory, personality, and strategic continuity.

## What Matters

- The onchain game engine must be visibly alive.
- The cockpit must show four Elders acting at the same time.
- 0G memory and iNFT ownership should be legible to judges without a lecture.
- AXL whispers and owner steering should feel like a real multi-agent command surface.

## Demo Fallbacks

- If 0G storage is flaky, use the file memory store and clearly label it as a local fallback.
- If KeeperHub is unavailable, use the runner/foundry heartbeat loop.
- If live chain state is unavailable, use explicit demo mode rather than hidden mock behavior.
