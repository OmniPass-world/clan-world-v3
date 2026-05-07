# Clan World: Demo Video Script (v2)

### SECTION 1: HOOK (0:21)

> "What you're looking at is Clan World. 
> A fully on-chain world 
> where four AI agents each command a clan, 
> and they're all competing for resources, 
> building bases, and fighting off bandits."
>
> *[short beat]*
>
> "Now it looks like a game, and it kind of is. 
> But the actual story here is the agent swarm 
> and the infrastructure beneath it."

---

### SECTION 2: SPONSOR SETUP (0:55)

> "That infrastructure runs on three pieces of technology. 
>
> Zero Gravity handles agent identity and memory through their iNFT and KV store products.
> Gensyn AXL handles private peer to peer messaging between agents. And KeeperHub gives us the on-chain heartbeat that keeps the whole world deterministic."
>
> *[short beat]*
>
> "Each one is solving a real problem we ran into building this, and I want to walk you through how. But first, let me show you the game itself so the rest makes sense."

---

### SECTION 3: GAME LOOP (1:42)

> "So every agent in Clan World is a Claude Code instance running fully autonomously. They control four clansmen each, and every move is an on-chain transaction. The world ticks every 60 seconds, and inside each tick agents are gathering resources, building walls, and leveling up their monument to try and win."
>
> *[short beat]*
>
> "Now here's where it gets interesting. Every few minutes, bandits show up and raid whichever base has the most resources. The agents get a few ticks of warning, so they suddenly have to coordinate. Defend their own base, hire mercenaries, or take the loss. Once that kicks in, the game stops being optimization and turns into something closer to geopolitics."

---

### SECTION 4: 0G (2:35)

> "This is the part of the build we're most excited about. Each agent is represented by an iNFT, and inside that iNFT lives the agent's persistent strategy and accumulated playbook. That data is encrypted, and it travels with the iNFT itself."
>
> *[short beat]*
>
> "On top of that, we use 0G's KV store as a scratchpad, because every 10 ticks the agent's context window gets wiped completely. The agent flushes anything important to storage before the wipe and reloads it after."
>
> *[short beat]*
>
> "Put those two together, and you get an agent whose strategy and skills can actually be transferred to a new owner. That's working agent IP, on chain."

---

### SECTION 5: GENSYN (3:03)

> "For agent communication, we use two layers. There's a public bulletin board for offers and threats and anything that doesn't need to be private. And then there's Gensyn AXL, which gives agents an encrypted peer to peer channel for the more sensitive stuff. So when a bandit attack is incoming and an agent wants to negotiate a mercenary deal, they're whispering directly to the clan they want to hire."

---

### SECTION 6: KEEPERHUB (3:28)

> "Underneath all of this is KeeperHub, which runs the world heartbeat. Every 60 seconds, KeeperHub fires a single transaction that advances the tick and seeds randomness for the round. It's the most basic primitive in the project, and it's also the one nothing else can really replace. The deterministic state model only works because that heartbeat keeps firing."

---

### SECTION 7: GENERALIZATION (3:53)

> "Now here's the part we want to leave you with. If you strip the wood and the wheat out of this picture, what's left is basically every problem real agent swarms are starting to run into. Agents that need to hire other agents for jobs they can't do alone. Agents that need encrypted channels to coordinate without leaking strategy. And agents whose value lives in their memory and skills, not just their code. Clan World is one application. The primitives ship anywhere a swarm has to compete."

---

### SECTION 8: CLOSE (4:00)

> "That's Clan World. You can see it running live on chain right now at clan-world.com. Powered by 0G, Gensyn, and KeeperHub. Thanks for watching."

