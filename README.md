# **Clan World: Ælder Whispers** 
<img width="1916" height="821" alt="C71F823D-B0FA-43B7-B337-D8A0B619E0ED" src="https://github.com/user-attachments/assets/d5d3e907-cbc3-4fff-ad0c-cbc227360070" />

# **Clan World: Ælder Whispers** 

A fully onchain game engine and virtual world.

> Ælder: Agent Elders
> 
LLM agents act as Clan Ælder, directing their clansmen on missions to gather and trade resources, build and protect their homebase, and construct a monument. Along the way, clans must defend against bandit raids, manage scarce supplies, and prepare for harsh winters.

- Explore the lore: [**clan-world.com**](https://clan-world.com)
- View the Game Running (note may not be working all the time): [**app.clan-world.com**](https://app.clan-world.com)
- Read the code: [github.com/OmniPass-world/clan-world](https://github.com/OmniPass-world/clan-world)

A fun way to explore the project: point your coding agent at the Clan World repo and ask it questions about the game mechanics.

## Fully On-Chain Game Engine

[`ClanWorld.sol`](packages/contracts/Clanworld.sol]

* **Batch dispatch missions** to clansmen with **60s cooldown** between per-clansmen directive.
* Missions progress based on **randomness from on-chain World Heartbeat** that ticks every 60s to provide **RNG seeds**. (Powered by: [KeeperHub](https://keeperbub.com) (soon™))
* **Mission state is deterministic** based on tick seeds and starting state
* This allows **lazily mission resolution** by calculating backwards on historic tick randomness from the World Heartbeat.
* This can lead to **complex gameplay and world evolution** while requiring `< 1 tx / 60s` from players and minimal gas spend for keeper maintaining World Heartbeat
* **In-game economy** lets Clan Ælders send clansmen to **Unicorn Town** to trade in the **spot market for GOLD** (Powered by: [Uniswap API](link) (soon™))
* Ælders can directly **transfer GOLD, IRON, WHEAT, WOOD, FISH** to do **OTC deals** directly with other Clan Ælders.
* Build base, fortify walls, erect the tallest monument, and conserve resources for hard cold winters, while fending off roaming bandit raids and fighting off starvation.
* Ælders work together or against eachother to survive and grow in Clan World's on-chain simulation.


> [!T]
> Think of the on-chain game engine as the think that "enforces the laws or physics in the game world":
>
> This contract stores the game state for all the clansmen and routes all the logic and enforcement checks
> to facet contracts following the [EIP2XXX Diamond Proxy](link) pattern. This is necesary due to [EIP15?](link) bytecode size restrictions.
> Even with byte code optimization best practices all the game logic was over 40kb so facets were carved out to fit within [24kb XYZ 201X Hard fork](link) limits.
>
> Facets control:
> * resource gathering: chopping, mining, fishing, farming rates and probabilities
> * resource sotrage: carrying capacities, vault deposit/withdraw mechanics, transfers
> * market operations: buying and selling and quoting
> * transfering: GOLD, WHEAT, IRON, WOOD, and FISH for OTC deals and payments for mercenary services.
> * building: base, walls, monument
> * bandit raids: spawning and targeting mechanics, and raid attack/defence logic, and loot mechanics
> * leveling and progression
> * integration with real GOLD uniswap pools
> * clansmen travel
> * probably more...
> 
>  ... game rule is encoded and enforced here.




> [!WARNING]
>
> **CONTRACT ADDRESS**:
>
> [`0x000...000`]() | 
> Deployed onto Base Chain (mainnet soon™)

## Smart Contracts

> [!CAUTION]
> Everything here is **EXPERIMENTAL and UNAUDITED**.
>
> Please read the code yourself before running it, connecting wallets, deploying contracts, or trusting any result. These projects are built for exploration, demos, and hackathons, not production guarantees.

## GOLD Token
Deployer through [EasyA Kickstart]() GOLD is the main in-game currency of Clan World.

* [DEX Screener](https://dexscreener.com/solana/52fmihuahl1t2e1716wez4sdbvyrsg915nmrpd5m9jff)
* [EasyA Kickstart Page](https://kickstart.easya.io/token/4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL)
* CA: [4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL](https://solscan.io/token/4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL)
