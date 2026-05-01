Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.5
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019dda65-c2bf-72b3-84f9-fc6dac9c8267
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output review in requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.

Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.

## Your task
This is a COHESIVE PHASE — review the integrated whole. Look for:

1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
3. SECURITY — re-entrancy in pool swaps, slippage protection, MEV / front-running
4. DATA-flow — vault accounting bridge to ERC20 boundary, treasury seeding integrity
5. Integration risks — interaction with Phase 5 vault state + Phase 9 bandit attack timing
6. Test coverage on the integration seams (immediate + scheduled, both buy + sell, all 4 resources)

USE PARALLEL TOOL CALLS. Read all changed files. Trace state machines. Don't just skim.

## Output format

# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns.

## HIGH severity findings

## MEDIUM severity findings

## LOW severity findings

## Cross-cutting observations

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..1c59b75 100644
--- a/packages/contracts/abi/IClanWorld.json
+++ b/packages/contracts/abi/IClanWorld.json
@@ -7,6 +7,25 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "getActionDuration",
+      "inputs": [
+        {
+          "name": "action",
+          "type": "uint8",
+          "internalType": "enum ActionType"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "pure"
+    },
     {
       "type": "function",
       "name": "getActiveBanditView",
@@ -232,83 +251,6 @@
       ],
       "stateMutability": "view"
     },
-    {
-      "type": "function",
-      "name": "getMissionTiming",
-      "inputs": [
-        {
-          "name": "clanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        },
-        {
-          "name": "clansmanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "submitted",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "executes",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "settles",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "view"
-    },
-    {
-      "type": "function",
-      "name": "getActionDuration",
-      "inputs": [
-        {
-          "name": "action",
-          "type": "uint8",
-          "internalType": "enum ActionType"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
-    {
-      "type": "function",
-      "name": "getTravelTicks",
-      "inputs": [
-        {
-          "name": "fromRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        },
-        {
-          "name": "toRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
     {
       "type": "function",
       "name": "getBanditTargetPreview",
@@ -1639,6 +1581,83 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getMissionTiming",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "clansmanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "submitted",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "executes",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "settles",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getPool",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getPrice",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        },
+        {
+          "name": "amountIn",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "amountOut",
+          "type": "uint256",
+          "internalType": "uint256"
+        }
+      ],
+      "stateMutability": "view"
+    },
     {
       "type": "function",
       "name": "getRegionPopulation",
@@ -1685,6 +1704,25 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getResourceToken",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],
+      "stateMutability": "view"
+    },
     {
       "type": "function",
       "name": "getScheduledMarketActionsForTick",
@@ -1751,6 +1789,30 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getTravelTicks",
+      "inputs": [
+        {
+          "name": "fromRegion",
+          "type": "uint8",
+          "internalType": "uint8"
+        },
+        {
+          "name": "toRegion",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "pure"
+    },
     {
       "type": "function",
       "name": "getTreasuryState",
@@ -1929,6 +1991,16 @@
               "type": "bool",
               "internalType": "bool"
             },
+            {
+              "name": "currentSeasonNumber",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "nextHeartbeatAtTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
             {
               "name": "winterActive",
               "type": "bool",
@@ -2036,6 +2108,16 @@
               "type": "bool",
               "internalType": "bool"
             },
+            {
+              "name": "currentSeasonNumber",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "nextHeartbeatAtTick",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
             {
               "name": "nextHeartbeatAtTs",
               "type": "uint64",
@@ -2270,6 +2352,19 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "settleClansman",
+      "inputs": [
+        {
+          "name": "csId",
+          "type": "uint32",
+          "internalType": "uint32"
+        }
+      ],
+      "outputs": [],
+      "stateMutability": "nonpayable"
+    },
     {
       "type": "function",
       "name": "submitClanOrders",
@@ -2347,6 +2442,11 @@
               "name": "missionNonce",
               "type": "uint64",
               "internalType": "uint64"
+            },
+            {
+              "name": "marketMode",
+              "type": "uint8",
+              "internalType": "enum MarketExecutionMode"
             }
           ]
         }
@@ -2970,22 +3070,22 @@
           "internalType": "uint32"
         },
         {
-          "name": "clansmanId",
+          "name": "csId",
           "type": "uint32",
-          "indexed": true,
+          "indexed": false,
           "internalType": "uint32"
         },
         {
-          "name": "tokenIn",
-          "type": "address",
+          "name": "action",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "enum ActionType"
         },
         {
-          "name": "tokenOut",
-          "type": "address",
+          "name": "resourceIn",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "uint8"
         },
         {
           "name": "amountIn",
@@ -2993,6 +3093,12 @@
           "indexed": false,
           "internalType": "uint256"
         },
+        {
+          "name": "resourceOut",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "uint8"
+        },
         {
           "name": "amountOut",
           "type": "uint256",
@@ -3000,7 +3106,7 @@
           "internalType": "uint256"
         },
         {
-          "name": "atTick",
+          "name": "tick",
           "type": "uint64",
           "indexed": false,
           "internalType": "uint64"
@@ -3068,9 +3174,9 @@
           "internalType": "uint32"
         },
         {
-          "name": "clansmanId",
+          "name": "csId",
           "type": "uint32",
-          "indexed": true,
+          "indexed": false,
           "internalType": "uint32"
         },
         {
@@ -3079,11 +3185,23 @@
           "indexed": false,
           "internalType": "enum ActionType"
         },
+        {
+          "name": "mode",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "enum MarketExecutionMode"
+        },
         {
           "name": "reason",
           "type": "uint8",
           "indexed": false,
           "internalType": "enum StatusCode"
+        },
+        {
+          "name": "tick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
         }
       ],
       "anonymous": false
@@ -3267,6 +3385,56 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "ResourceBurned",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "indexed": true,
+          "internalType": "uint8"
+        },
+        {
+          "name": "from",
+          "type": "address",
+          "indexed": true,
+          "internalType": "address"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "ResourceMinted",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "indexed": true,
+          "internalType": "uint8"
+        },
+        {
+          "name": "to",
+          "type": "address",
+          "indexed": true,
+          "internalType": "address"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "ResourcesDeposited",
@@ -3284,34 +3452,34 @@
           "internalType": "uint32"
         },
         {
-          "name": "wood",
+          "name": "woodDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "iron",
+          "name": "ironDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "wheat",
+          "name": "wheatDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "fish",
+          "name": "fishDelta",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
         },
         {
-          "name": "atTick",
-          "type": "uint64",
+          "name": "tick",
+          "type": "uint32",
           "indexed": false,
-          "internalType": "uint64"
+          "internalType": "uint32"
         }
       ],
       "anonymous": false
@@ -3436,18 +3604,6 @@
       "type": "event",
       "name": "ScheduledMarketActionExecuted",
       "inputs": [
-        {
-          "name": "executeAtTick",
-          "type": "uint64",
-          "indexed": true,
-          "internalType": "uint64"
-        },
-        {
-          "name": "commitSequence",
-          "type": "uint64",
-          "indexed": true,
-          "internalType": "uint64"
-        },
         {
           "name": "clanId",
           "type": "uint32",
@@ -3455,22 +3611,22 @@
           "internalType": "uint32"
         },
         {
-          "name": "clansmanId",
+          "name": "csId",
           "type": "uint32",
           "indexed": false,
           "internalType": "uint32"
         },
         {
-          "name": "tokenIn",
-          "type": "address",
+          "name": "action",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "enum ActionType"
         },
         {
-          "name": "tokenOut",
-          "type": "address",
+          "name": "resourceIn",
+          "type": "uint8",
           "indexed": false,
-          "internalType": "address"
+          "internalType": "uint8"
         },
         {
           "name": "amountIn",
@@ -3478,11 +3634,23 @@
           "indexed": false,
           "internalType": "uint256"
         },
+        {
+          "name": "resourceOut",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "uint8"
+        },
         {
           "name": "amountOut",
           "type": "uint256",
           "indexed": false,
           "internalType": "uint256"
+        },
+        {
+          "name": "settledAtTick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
         }
       ],
       "anonymous": false
diff --git a/packages/contracts/script/Deploy.s.sol b/packages/contracts/script/Deploy.s.sol
index 0219458..e5cd0ca 100644
--- a/packages/contracts/script/Deploy.s.sol
+++ b/packages/contracts/script/Deploy.s.sol
@@ -5,20 +5,21 @@ import {Script, console} from "forge-std/Script.sol";
 import {MinimalERC20} from "../src/MinimalERC20.sol";
 import {StubPool} from "../src/StubPool.sol";
 import {ClanWorld} from "../src/ClanWorld.sol";
-import {PoolSeedConfig} from "../src/IClanWorld.sol";
+import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
 
 contract Deploy is Script {
     function run() external {
         uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
+        address treasury = vm.addr(deployerPrivateKey);
 
         vm.startBroadcast(deployerPrivateKey);
 
-        // 1. Deploy 6 resource tokens (still needed for treasury/pool references)
-        MinimalERC20 wood = new MinimalERC20("ClanWorld Wood", "WOOD");
-        MinimalERC20 iron = new MinimalERC20("ClanWorld Iron", "IRON");
-        MinimalERC20 wheat = new MinimalERC20("ClanWorld Wheat", "WHEAT");
-        MinimalERC20 fish = new MinimalERC20("ClanWorld Fish", "FISH");
-        MinimalERC20 gold = new MinimalERC20("ClanWorld Gold", "GOLD");
+        // 1. Deploy boundary tokens (gold existed in Phase 2 and is reused here).
+        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
+        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
+        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
+        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
+        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
         MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");
 
         console.log("wood:     ", address(wood));
@@ -28,11 +29,16 @@ contract Deploy is Script {
         console.log("gold:     ", address(gold));
         console.log("blueprint:", address(blueprint));
 
-        // 3. Deploy ClanWorld first (needed as engine arg for pools)
+        // 2. Deploy ClanWorld first (needed as engine arg for pools).
         ClanWorld game = new ClanWorld();
         console.log("CLAN_WORLD_CONTRACT_ADDRESS:", address(game));
 
-        // 2. Deploy 4 AMM pools (Phase 2: real constant-product pools)
+        wood.configureBoundary(uint8(ResourceType.Wood), address(game));
+        iron.configureBoundary(uint8(ResourceType.Iron), address(game));
+        wheat.configureBoundary(uint8(ResourceType.Wheat), address(game));
+        fish.configureBoundary(uint8(ResourceType.Fish), address(game));
+
+        // 3. Deploy 4 AMM pools (Phase 6.2: constant-product pools).
         StubPool woodGold = new StubPool(address(wood), address(gold), address(game));
         StubPool wheatGold = new StubPool(address(wheat), address(gold), address(game));
         StubPool fishGold = new StubPool(address(fish), address(gold), address(game));
@@ -49,8 +55,22 @@ contract Deploy is Script {
 
         game.initTreasury(tokens, pools);
 
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        uint256 resSeed = game.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = game.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        wood.seedTreasury(treasury, resSeed);
+        wheat.seedTreasury(treasury, resSeed);
+        fish.seedTreasury(treasury, resSeed);
+        iron.seedTreasury(treasury, resSeed);
+        gold.seedTreasury(treasury, totalGoldSeed);
+
+        wood.approve(address(game), resSeed);
+        wheat.approve(address(game), resSeed);
+        fish.approve(address(game), resSeed);
+        iron.approve(address(game), resSeed);
+        gold.approve(address(game), totalGoldSeed);
+
         game.seedPools(
             PoolSeedConfig({
                 woodSeed: resSeed,
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..60c0277 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -37,6 +37,7 @@ import {
     ActiveBanditView,
     RegionOccupant
 } from "./IClanWorld.sol";
+import {MinimalERC20} from "./MinimalERC20.sol";
 import {StubPool} from "./StubPool.sol";
 import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
 
@@ -58,6 +59,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     mapping(uint32 => Mission) private _missions; // keyed by clansmanId
     mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
     mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
+    mapping(uint32 => uint64) private _marketMissionCommitSequence; // clansmanId => FIFO sequence captured at submit
     mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
     mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
     mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
@@ -74,10 +76,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
     // =========================================================================
 
+    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
-    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
-    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
-
+    uint256 public constant INITIAL_RESOURCE_POOL_SEED = 100_000e18;
+    uint256 public constant INITIAL_GOLD_POOL_SEED = 50_000e18;
     // =========================================================================
     // CONSTRUCTOR
     // =========================================================================
@@ -91,8 +93,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
         // i.e. ticks [100, 110)
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
         _world.winterActive = false;
         _treasury.treasuryOwner = msg.sender;
@@ -429,7 +430,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
     }
 
-    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
+    /// @dev Resolve an action for a clansman that is in ACTING state.
     function _resolveAction(
         Clan storage clan,
         Clansman storage cs,
@@ -452,7 +453,9 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         } else if (action == ActionType.HarvestWheat) {
             _gatherWheat(clan, cs, m, clanId, tick, starving);
         } else if (action == ActionType.DepositResources) {
-            _doDeposit(clan, cs, m, clanId, tick);
+            // Deposit event ABI intentionally stores the current tick as uint32.
+            // forge-lint: disable-next-line(unsafe-typecast)
+            _doDeposit(clan, cs, m, clanId, uint32(tick));
         } else if (action == ActionType.Wait) {
             // NOOP — worker stays ACTING (continuous), no transition needed
             // Wait mission is effectively persistent until interrupted
@@ -464,9 +467,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             // Phase 1 stub: check homebase, check resources; if ok, stub success
             _doBuilding(clan, cs, m, clanId, tick, action);
         } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
-            // Scheduled market actions: already enqueued at submitClanOrders time.
-            // Settlement resolves this action slot — just complete the mission.
-            // (Actual execution happened or will happen at heartbeat.)
+            _enqueueScheduledMarketAction(clanId, cs.clansmanId, m);
             _completeMission(cs, m);
         }
     }
@@ -484,28 +485,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         bool starving,
         bytes32 tickSeed
     ) internal {
-        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
-        if (remaining == 0) {
+        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
             _completeMission(cs, m);
             return;
         }
-        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
-        // Crit roll: domain-separated RNG
-        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
-        uint256 critRoll = uint256(critRng) % 10000;
-        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
-            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
+
+        uint256 remaining = ClanWorldConstants.WOOD_CARRY_CAP - cs.carryWood;
+        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
+        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
+        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
+            yield *= 2;
         }
+
         if (starving) yield = yield / 2;
         if (yield > remaining) yield = remaining;
         cs.carryWood += yield;
 
         emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
 
-        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
+        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
             _completeMission(cs, m);
         }
-        // else continuous — worker stays ACTING
     }
 
     function _gatherIron(
@@ -665,7 +665,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         // else continuous
     }
 
-    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
+    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
         internal
     {
         // Must be at homebase region
@@ -675,26 +675,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         }
         bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
         if (!hasAnything) {
+            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
             _completeMission(cs, m);
             return;
         }
 
-        uint256 w = cs.carryWood;
-        uint256 ir = cs.carryIron;
-        uint256 wh = cs.carryWheat;
-        uint256 fi = cs.carryFish;
+        uint256 woodDelta = cs.carryWood;
+        uint256 ironDelta = cs.carryIron;
+        uint256 wheatDelta = cs.carryWheat;
+        uint256 fishDelta = cs.carryFish;
 
-        clan.vaultWood += w;
-        clan.vaultIron += ir;
-        clan.vaultWheat += wh;
-        clan.vaultFish += fi;
+        clan.vaultWood += woodDelta;
+        clan.vaultIron += ironDelta;
+        clan.vaultWheat += wheatDelta;
+        clan.vaultFish += fishDelta;
 
         cs.carryWood = 0;
         cs.carryIron = 0;
         cs.carryWheat = 0;
         cs.carryFish = 0;
 
-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
+        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
         _completeMission(cs, m);
     }
 
@@ -1100,7 +1101,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
                         clansmanId: orders[i].clansmanId,
                         status: StatusCode.ERR_INVALID_ACTION,
                         cooldownEndsAtTs: 0,
-                        missionNonce: 0
+                        missionNonce: 0,
+                        marketMode: MarketExecutionMode.None
                     });
                 }
                 return results;
@@ -1168,8 +1170,11 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             }
         }
 
-        // Cooldown check
-        if (block.timestamp < cs.cooldownEndsAtTs) {
+        bool isMarketAction = order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell;
+
+        // Cooldown check. Market orders may still fall back to the scheduled path;
+        // only the immediate path requires the worker to be off cooldown.
+        if (!isMarketAction && block.timestamp < cs.cooldownEndsAtTs) {
             result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
             result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
             result.missionNonce = cs.lastMissionNonce;
@@ -1202,6 +1207,28 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             return result;
         }
 
+        if (
+            isMarketAction && ctx.fromRegion == ClanWorldConstants.REGION_UNICORN_TOWN
+                && cs.state == ClansmanState.WAITING && block.timestamp >= cs.cooldownEndsAtTs
+        ) {
+            ctx.newNonce = cs.lastMissionNonce + 1;
+            cs.lastMissionNonce = ctx.newNonce;
+
+            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
+
+            if (marketStatus == StatusCode.OK) {
+                cs.state = ClansmanState.WAITING;
+                cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
+            }
+            _clearDefender(cs.clansmanId);
+
+            result.status = StatusCode.OK;
+            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
+            result.missionNonce = ctx.newNonce;
+            result.marketMode = MarketExecutionMode.Immediate;
+            return result;
+        }
+
         // Capture existing mission state
         Mission storage existingM = _missions[order.clansmanId];
         ctx.wasActive = existingM.active;
@@ -1232,12 +1259,6 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         _clearDefender(cs.clansmanId);
 
-        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
-        // executeAtTick = arrivalTick (not arrivalTick+1).
-        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
-            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
-        }
-
         if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
             _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
         }
@@ -1260,6 +1281,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         result.status = StatusCode.OK;
         result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
         result.missionNonce = ctx.newNonce;
+        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
         return result;
     }
 
@@ -1288,36 +1310,30 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         m.marketToken = order.marketToken;
         m.marketAmount = order.marketAmount;
         m.maxGoldIn = order.maxGoldIn;
+
+        if (m.marketMode == MarketExecutionMode.Scheduled) {
+            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
+        } else {
+            delete _marketMissionCommitSequence[cs.clansmanId];
+        }
     }
 
-    function _enqueueScheduledMarketAction(
-        uint32 clanId,
-        ClanOrder calldata order,
-        uint32 clansmanId,
-        uint64 executeAtTick,
-        uint64 missionNonce
-    ) internal {
+    function _enqueueScheduledMarketAction(uint32 clanId, uint32 clansmanId, Mission storage m) internal {
+        uint64 executeAtTick = m.settlesAtTick;
         ScheduledMarketAction memory sma = ScheduledMarketAction({
             executeAtTick: executeAtTick,
-            commitSequence: _world.nextCommitSequence++,
-            missionNonce: missionNonce,
+            commitSequence: _marketMissionCommitSequence[clansmanId],
+            missionNonce: m.nonce,
             clanId: clanId,
             clansmanId: clansmanId,
-            action: order.action,
-            marketToken: order.marketToken,
-            marketAmount: order.marketAmount,
-            maxGoldIn: order.maxGoldIn
+            action: m.action,
+            marketToken: m.marketToken,
+            marketAmount: m.marketAmount,
+            maxGoldIn: m.maxGoldIn
         });
         _scheduledMarketActions[executeAtTick].push(sma);
         emit ScheduledMarketActionCommitted(
-            executeAtTick,
-            sma.commitSequence,
-            clanId,
-            clansmanId,
-            order.action,
-            order.marketToken,
-            order.marketAmount,
-            order.maxGoldIn
+            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
         );
     }
 
@@ -1359,22 +1375,28 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // MARKET EXECUTION (Phase 2)
     // =========================================================================
 
-    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
-    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
+    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
     function _executeScheduledMarketActions(uint64 tick) internal {
         ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
         uint256 len = actions.length;
         if (len == 0) return;
 
-        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;
+        _sortScheduledMarketActionsByCommitSequence(actions);
 
-        for (uint256 i = 0; i < processCount; i++) {
+        for (uint256 i = 0; i < len; i++) {
             ScheduledMarketAction storage sma = actions[i];
 
             // Validate clansman still belongs to the clan
             Clansman storage cs = _clansmen[sma.clansmanId];
             if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
-                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
+                _emitMarketActionFailed(
+                    sma.clanId,
+                    sma.clansmanId,
+                    sma.action,
+                    MarketExecutionMode.Scheduled,
+                    StatusCode.ERR_INVALID_CLANSMAN,
+                    tick
+                );
                 continue;
             }
 
@@ -1383,47 +1405,54 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             // cannot use m.active as a validity signal here — check action type and nonce.
             Mission storage m = _missions[sma.clansmanId];
             if (m.action != sma.action || m.nonce != sma.missionNonce) {
-                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
+                _emitMarketActionFailed(
+                    sma.clanId,
+                    sma.clansmanId,
+                    sma.action,
+                    MarketExecutionMode.Scheduled,
+                    StatusCode.ERR_INVALID_ACTION,
+                    tick
+                );
                 continue;
             }
 
             if (sma.action == ActionType.MarketSell) {
                 try this._executeMarketSellExternal(
-                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
+                ) returns (
+                    StatusCode marketStatus
                 ) {
-                // success
-                }
-                catch {
-                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
+                    marketStatus;
+                } catch {
+                    _handleMarketFailure(
+                        sma.clanId,
+                        sma.clansmanId,
+                        sma.action,
+                        MarketExecutionMode.Scheduled,
+                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                        tick
+                    );
                 }
             } else if (sma.action == ActionType.MarketBuy) {
                 try this._executeMarketBuyExternal(
-                    tick,
-                    sma.clanId,
-                    sma.clansmanId,
-                    sma.marketToken,
-                    sma.marketAmount,
-                    sma.maxGoldIn,
-                    sma.commitSequence
+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
+                ) returns (
+                    StatusCode marketStatus
                 ) {
-                // success
-                }
-                catch {
-                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
+                    marketStatus;
+                } catch {
+                    _handleMarketFailure(
+                        sma.clanId,
+                        sma.clansmanId,
+                        sma.action,
+                        MarketExecutionMode.Scheduled,
+                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                        tick
+                    );
                 }
             }
         }
 
-        if (len > processCount) {
-            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
-            for (uint256 i = processCount; i < len; i++) {
-                nextActions.push(actions[i]);
-            }
-            // Invariant: each tick queue executes in global commitSequence order, including
-            // older overflow actions merged into a tick that already has native actions.
-            _sortScheduledMarketActionsByCommitSequence(nextActions);
-        }
-
         delete _scheduledMarketActions[tick];
     }
 
@@ -1445,11 +1474,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         uint32 clanId,
         uint32 clansmanId,
         address token,
-        uint256 amount,
-        uint64 commitSequence
-    ) external {
+        uint256 amount
+    ) external returns (StatusCode) {
         require(msg.sender == address(this), "ClanWorld: internal only");
-        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
+        return _executeMarketSell(closedTick, clanId, clansmanId, token, amount);
     }
 
     /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
@@ -1459,11 +1487,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         uint32 clansmanId,
         address token,
         uint256 amountOut,
-        uint256 maxGoldIn,
-        uint64 commitSequence
-    ) external {
+        uint256 maxGoldIn
+    ) external returns (StatusCode) {
         require(msg.sender == address(this), "ClanWorld: internal only");
-        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
+        return _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn);
     }
 
     /// @dev Map a resource token address to its pool address.
@@ -1475,6 +1502,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return address(0);
     }
 
+    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
+    function _marketResourceForToken(address token) internal view returns (uint8) {
+        if (token == _treasury.woodToken) return uint8(ResourceType.Wood);
+        if (token == _treasury.ironToken) return uint8(ResourceType.Iron);
+        if (token == _treasury.wheatToken) return uint8(ResourceType.Wheat);
+        if (token == _treasury.fishToken) return uint8(ResourceType.Fish);
+        if (token == _treasury.goldToken) return ClanWorldConstants.RESOURCE_GOLD;
+        revert("ClanWorld: invalid market resource");
+    }
+
+    function _emitMarketActionFailed(
+        uint32 clanId,
+        uint32 clansmanId,
+        ActionType action,
+        MarketExecutionMode mode,
+        StatusCode reason,
+        uint64 tick
+    ) internal {
+        emit MarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
+    }
+
     /// @dev Add an amount of a resource token to the clan vault.
     function _addToVault(Clan storage clan, address token, uint256 amount) internal {
         if (token == _treasury.woodToken) {
@@ -1520,37 +1568,285 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return false;
     }
 
-    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
-    function _executeMarketSell(
-        uint64 closedTick,
+    /// @dev Check a clan vault balance without mutating storage.
+    function _hasVaultBalance(Clan storage clan, address token, uint256 amount) internal view returns (bool) {
+        if (token == _treasury.woodToken) return clan.vaultWood >= amount;
+        if (token == _treasury.ironToken) return clan.vaultIron >= amount;
+        if (token == _treasury.wheatToken) return clan.vaultWheat >= amount;
+        if (token == _treasury.fishToken) return clan.vaultFish >= amount;
+        return false;
+    }
+
+    function _handleMarketFailure(
+        uint32 clanId,
+        uint32 clansmanId,
+        ActionType action,
+        MarketExecutionMode mode,
+        StatusCode reason,
+        uint64 tick
+    ) internal returns (StatusCode) {
+        Clansman storage cs = _clansmen[clansmanId];
+        if (cs.clansmanId != 0 && cs.state != ClansmanState.DEAD) {
+            cs.state = ClansmanState.WAITING;
+            cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
+        }
+        _emitMarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
+        return reason;
+    }
+
+    function _remainingCarryForToken(Clansman storage cs, address token) internal view returns (uint256) {
+        uint256 carried;
+        uint256 cap;
+        if (token == _treasury.woodToken) {
+            carried = cs.carryWood;
+            cap = ClanWorldConstants.WOOD_CAP;
+        } else if (token == _treasury.ironToken) {
+            carried = cs.carryIron;
+            cap = ClanWorldConstants.IRON_CAP;
+        } else if (token == _treasury.wheatToken) {
+            carried = cs.carryWheat;
+            cap = ClanWorldConstants.WHEAT_CAP;
+        } else if (token == _treasury.fishToken) {
+            carried = cs.carryFish;
+            cap = ClanWorldConstants.FISH_CAP;
+        } else {
+            return 0;
+        }
+
+        if (carried >= cap) return 0;
+        return cap - carried;
+    }
+
+    function _executeImmediateMarket(uint32 clanId, ClanOrder calldata order, uint32 clansmanId)
+        internal
+        returns (StatusCode)
+    {
+        if (order.action == ActionType.MarketSell) {
+            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
+        }
+        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
+    }
+
+    function _executeImmediateMarketSell(uint32 clanId, uint32 clansmanId, address token, uint256 amount)
+        internal
+        returns (StatusCode)
+    {
+        if (!_treasury.poolsSeeded) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
+        }
+        address poolAddr = _poolFor(token);
+        if (poolAddr == address(0)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
+        }
+
+        Clan storage clan = _clans[clanId];
+        if (!_hasVaultBalance(clan, token, amount)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MISSING_RESOURCES,
+                _world.currentTick
+            );
+        }
+
+        try StubPool(poolAddr).swapExactInForOut(amount, 1) returns (uint256 goldOut) {
+            _deductFromVault(clan, token, amount);
+            clan.goldBalance += goldOut;
+            emit ImmediateMarketActionExecuted(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                _marketResourceForToken(token),
+                amount,
+                ClanWorldConstants.RESOURCE_GOLD,
+                goldOut,
+                _world.currentTick
+            );
+            return StatusCode.OK;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                _world.currentTick
+            );
+        }
+    }
+
+    function _executeImmediateMarketBuy(
         uint32 clanId,
         uint32 clansmanId,
         address token,
-        uint256 amount,
-        uint64 commitSequence
-    ) internal {
+        uint256 amountOut,
+        uint256 maxGoldIn
+    ) internal returns (StatusCode) {
         if (!_treasury.poolsSeeded) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
         }
         address poolAddr = _poolFor(token);
         if (poolAddr == address(0)) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                _world.currentTick
+            );
+        }
+
+        Clansman storage cs = _clansmen[clansmanId];
+        if (amountOut > _remainingCarryForToken(cs, token)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_CARRY_FULL,
+                _world.currentTick
+            );
+        }
+
+        uint256 goldIn;
+        try StubPool(poolAddr).getAmountInForExactOut(amountOut) returns (uint256 quotedGoldIn) {
+            goldIn = quotedGoldIn;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                _world.currentTick
+            );
+        }
+
+        Clan storage clan = _clans[clanId];
+        if (goldIn > maxGoldIn) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+                _world.currentTick
+            );
+        }
+        if (clan.goldBalance < goldIn) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_NOT_ENOUGH_GOLD,
+                _world.currentTick
+            );
+        }
+
+        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 actualGoldIn) {
+            clan.goldBalance -= actualGoldIn;
+            _addToVault(clan, token, amountOut);
+            emit ImmediateMarketActionExecuted(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                ClanWorldConstants.RESOURCE_GOLD,
+                actualGoldIn,
+                _marketResourceForToken(token),
+                amountOut,
+                _world.currentTick
+            );
+            return StatusCode.OK;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Immediate,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                _world.currentTick
+            );
+        }
+    }
+
+    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
+    function _executeMarketSell(uint64 closedTick, uint32 clanId, uint32 clansmanId, address token, uint256 amount)
+        internal
+        returns (StatusCode)
+    {
+        if (!_treasury.poolsSeeded) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
+        }
+        address poolAddr = _poolFor(token);
+        if (poolAddr == address(0)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
         }
 
         Clan storage clan = _clans[clanId];
         if (!_deductFromVault(clan, token, amount)) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketSell,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MISSING_RESOURCES,
+                closedTick
+            );
         }
 
         uint256 goldOut = StubPool(poolAddr).sellResource(amount);
         clan.goldBalance += goldOut;
 
         emit ScheduledMarketActionExecuted(
-            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
+            clanId,
+            clansmanId,
+            ActionType.MarketSell,
+            _marketResourceForToken(token),
+            amount,
+            ClanWorldConstants.RESOURCE_GOLD,
+            goldOut,
+            closedTick
         );
+        return StatusCode.OK;
     }
 
     /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
@@ -1560,43 +1856,107 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         uint32 clansmanId,
         address token,
         uint256 amountOut,
-        uint256 maxGoldIn,
-        uint64 commitSequence
-    ) internal {
+        uint256 maxGoldIn
+    ) internal returns (StatusCode) {
         if (!_treasury.poolsSeeded) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
         }
         address poolAddr = _poolFor(token);
         if (poolAddr == address(0)) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
+                closedTick
+            );
         }
 
-        // Quote gold cost without updating reserves
-        uint256 goldIn = StubPool(poolAddr).quoteBuy(amountOut);
+        Clansman storage cs = _clansmen[clansmanId];
+        if (amountOut > _remainingCarryForToken(cs, token)) {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_CARRY_FULL,
+                closedTick
+            );
+        }
+
+        uint256 goldIn;
+        try StubPool(poolAddr).quoteBuy(amountOut) returns (uint256 quotedGoldIn) {
+            goldIn = quotedGoldIn;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                closedTick
+            );
+        }
 
         Clan storage clan = _clans[clanId];
 
         if (goldIn > maxGoldIn) {
-            emit MarketActionFailed(
-                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+                closedTick
             );
-            return;
         }
         if (clan.goldBalance < goldIn) {
-            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
-            return;
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_NOT_ENOUGH_GOLD,
+                closedTick
+            );
+        }
+
+        uint256 actualGoldIn;
+        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 spentGold) {
+            actualGoldIn = spentGold;
+        } catch {
+            return _handleMarketFailure(
+                clanId,
+                clansmanId,
+                ActionType.MarketBuy,
+                MarketExecutionMode.Scheduled,
+                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+                closedTick
+            );
         }
 
-        // Execute — use return value to guard against any future pool divergence
-        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
         clan.goldBalance -= actualGoldIn;
         _addToVault(clan, token, amountOut);
 
         emit ScheduledMarketActionExecuted(
-            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
+            clanId,
+            clansmanId,
+            ActionType.MarketBuy,
+            ClanWorldConstants.RESOURCE_GOLD,
+            actualGoldIn,
+            _marketResourceForToken(token),
+            amountOut,
+            closedTick
         );
+        return StatusCode.OK;
     }
 
     function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
@@ -1610,7 +1970,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         // DepositResources: must go to homebase
         if (action == ActionType.DepositResources) {
-            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
+            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
         }
 
         // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
@@ -1670,10 +2030,15 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             ) {
                 return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
             }
-            // Market orders are always enqueued for the arrivalTick FIFO queue.
-            // _resolveAction records mission completion but does not execute any swap.
+            // Over-capacity buys are rejected at submission; no partial fills or overflow refunds.
+            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
+                return StatusCode.ERR_CARRY_FULL;
+            }
+            // Immediate market orders execute during submitClanOrders when the
+            // worker is waiting in Unicorn Town and off cooldown. Other valid
+            // market orders enqueue when the scheduled mission resolves.
             if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
-                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
+                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
             }
         }
 
@@ -1720,6 +2085,15 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
         require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
 
+        _transferSeed(_treasury.woodToken, _treasury.woodGoldPool, cfg.woodSeed);
+        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
+        _transferSeed(_treasury.wheatToken, _treasury.wheatGoldPool, cfg.wheatSeed);
+        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
+        _transferSeed(_treasury.fishToken, _treasury.fishGoldPool, cfg.fishSeed);
+        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
+        _transferSeed(_treasury.ironToken, _treasury.ironGoldPool, cfg.ironSeed);
+        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);
+
         StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
         StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
         StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
@@ -1732,6 +2106,11 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         );
     }
 
+    function _transferSeed(address token, address pool, uint256 amount) internal {
+        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
+        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
+    }
+
     // =========================================================================
     // OTC TRANSFERS
     // =========================================================================
@@ -1768,6 +2147,30 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return _treasury;
     }
 
+    function getResourceToken(uint8 resourceType) external view override returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
+        revert("ClanWorld: invalid resource");
+    }
+
+    function getPool(uint8 resourceType) external view override returns (address) {
+        return _poolForResourceType(resourceType);
+    }
+
+    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {
+        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
+    }
+
+    function _poolForResourceType(uint8 resourceType) internal view returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
+        revert("ClanWorld: invalid resource");
+    }
+
     function getClan(uint32 clanId) external view override returns (Clan memory) {
         return _clans[clanId];
     }
@@ -1801,10 +2204,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             return 4;
         }
 
+        if (action == ActionType.DepositResources) {
+            return DEPOSIT_DURATION_TICKS;
+        }
+
         if (
-            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
-                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
-                || action == ActionType.MarketSell
+            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
+                || action == ActionType.MarketBuy || action == ActionType.MarketSell
         ) {
             return 1;
         }
diff --git a/packages/contracts/src/ClanWorldStub.sol b/packages/contracts/src/ClanWorldStub.sol
index 36ce29b..863432d 100644
--- a/packages/contracts/src/ClanWorldStub.sol
+++ b/packages/contracts/src/ClanWorldStub.sol
@@ -137,6 +137,26 @@ contract ClanWorldStub is IClanWorld {
         return _treasury;
     }
 
+    function getResourceToken(uint8 resourceType) external view override returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
+        revert("ClanWorldStub: invalid resource");
+    }
+
+    function getPool(uint8 resourceType) external view override returns (address) {
+        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
+        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
+        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
+        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
+        revert("ClanWorldStub: invalid resource");
+    }
+
+    function getPrice(uint8, uint256) external pure override returns (uint256) {
+        return 0;
+    }
+
     function getClan(uint32) external pure override returns (Clan memory) {
         return Clan({
             clanId: 0,
diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
index 2b80fbe..151e334 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -40,15 +40,17 @@ library ClanWorldConstants {
     uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
 
     // Carry caps (per clansman)
-    uint256 internal constant WOOD_CAP = 15e18;
+    uint256 internal constant WOOD_CARRY_CAP = 10e18;
+    uint256 internal constant WOOD_CAP = WOOD_CARRY_CAP;
     uint256 internal constant IRON_CAP = 5e18;
     uint256 internal constant WHEAT_CAP = 40e18;
     uint256 internal constant FISH_CAP = 8e18;
 
     // Gathering yields
-    uint256 internal constant WOOD_BASE_YIELD = 2e18;
-    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
-    uint16 internal constant WOOD_CRIT_BPS = 2000; // 20%
+    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
+    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
+    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
+    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%
 
     uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
     uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
@@ -82,6 +84,10 @@ library ClanWorldConstants {
     uint8 internal constant REGION_EAST_DOCKS = 7;
     uint8 internal constant REGION_DEEP_SEA = 8;
 
+    // Market event resource ids. ResourceType covers the four vault resources;
+    // gold is the quote asset emitted as resource id 4.
+    uint8 internal constant RESOURCE_GOLD = 4;
+
     // Sentinels
     uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
     uint32 internal constant BANDIT_ID_NULL = 0;
@@ -171,12 +177,13 @@ enum StatusCode {
     ERR_MARKET_UNSUPPORTED_TOKEN,
     ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
     ERR_MARKET_BUY_OVER_CAPACITY,
-    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
     ERR_WORLD_TICK_MISMATCH,
     ERR_NO_ACTIVE_BANDIT,
     ERR_SEASON_ENDED,
     ERR_NOT_ENOUGH_GOLD,
-    ERR_CARRY_FULL
+    ERR_CARRY_FULL,
+    ERR_LIQUIDITY_INSUFFICIENT,
+    ERR_MAX_GOLD_IN_EXCEEDED
 }
 
 // =============================================================================
@@ -188,8 +195,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -377,6 +384,7 @@ struct OrderResult {
     StatusCode status;
     uint64 cooldownEndsAtTs;
     uint64 missionNonce;
+    MarketExecutionMode marketMode;
 }
 
 struct PoolSeedConfig {
@@ -533,11 +541,11 @@ interface IClanWorldEvents {
     event ResourcesDeposited(
         uint32 indexed clanId,
         uint32 indexed clansmanId,
-        uint256 wood,
-        uint256 iron,
-        uint256 wheat,
-        uint256 fish,
-        uint64 atTick
+        uint256 woodDelta,
+        uint256 ironDelta,
+        uint256 wheatDelta,
+        uint256 fishDelta,
+        uint32 tick
     );
 
     // ----- building -----
@@ -548,22 +556,26 @@ interface IClanWorldEvents {
     // ----- market -----
     event ImmediateMarketActionExecuted(
         uint32 indexed clanId,
-        uint32 indexed clansmanId,
-        address tokenIn,
-        address tokenOut,
+        uint32 csId,
+        ActionType action,
+        uint8 resourceIn,
         uint256 amountIn,
+        uint8 resourceOut,
         uint256 amountOut,
-        uint64 atTick
+        uint64 tick
     );
     event ScheduledMarketActionExecuted(
-        uint64 indexed executeAtTick,
-        uint64 indexed commitSequence,
         uint32 indexed clanId,
-        uint32 clansmanId,
-        address tokenIn,
-        address tokenOut,
+        uint32 csId,
+        ActionType action,
+        uint8 resourceIn,
         uint256 amountIn,
-        uint256 amountOut
+        uint8 resourceOut,
+        uint256 amountOut,
+        uint64 settledAtTick
+    );
+    event MarketActionFailed(
+        uint32 indexed clanId, uint32 csId, ActionType action, MarketExecutionMode mode, StatusCode reason, uint64 tick
     );
     event ScheduledMarketActionCommitted(
         uint64 indexed executeAtTick,
@@ -575,7 +587,8 @@ interface IClanWorldEvents {
         uint256 marketAmount,
         uint256 maxGoldIn
     );
-    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
 
     // ----- bandits -----
     event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
@@ -699,6 +712,12 @@ interface IClanWorld is IClanWorldEvents {
 
     function getTreasuryState() external view returns (TreasuryState memory);
 
+    function getResourceToken(uint8 resourceType) external view returns (address);
+
+    function getPool(uint8 resourceType) external view returns (address);
+
+    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut);
+
     function getClan(uint32 clanId) external view returns (Clan memory);
 
     function getClansman(uint32 clansmanId) external view returns (Clansman memory);
diff --git a/packages/contracts/src/MinimalERC20.sol b/packages/contracts/src/MinimalERC20.sol
index b9ac0b8..fd704ce 100644
--- a/packages/contracts/src/MinimalERC20.sol
+++ b/packages/contracts/src/MinimalERC20.sol
@@ -1,39 +1,87 @@
 // SPDX-License-Identifier: MIT
 pragma solidity ^0.8.34;
 
-/// @notice Minimal ERC20 with owner-only mint. No external deps.
+/// @notice Minimal ERC20 boundary token. No external deps.
 contract MinimalERC20 {
     string public name;
     string public symbol;
     uint8 public constant decimals = 18;
 
     uint256 public totalSupply;
-    address public immutable OWNER;
+    address public immutable DEPLOYER;
+    address public engine;
+    uint8 public resourceType;
+    bool public boundaryConfigured;
+    bool public treasurySeeded;
 
     mapping(address => uint256) public balanceOf;
     mapping(address => mapping(address => uint256)) public allowance;
 
     event Transfer(address indexed from, address indexed to, uint256 value);
     event Approval(address indexed owner_, address indexed spender, uint256 value);
+    event BoundaryConfigured(uint8 indexed resourceType, address indexed engine);
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
 
     constructor(string memory name_, string memory symbol_) {
         name = name_;
         symbol = symbol_;
-        OWNER = msg.sender;
+        DEPLOYER = msg.sender;
+        resourceType = type(uint8).max;
     }
 
     /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
     /// body slim so the require isn't duplicated at every call site.
-    function _onlyOwner() internal view {
-        require(msg.sender == OWNER, "not owner");
+    function _onlyDeployer() internal view {
+        require(msg.sender == DEPLOYER, "MinimalERC20: not deployer");
     }
 
-    modifier onlyOwner() {
-        _onlyOwner();
+    function _onlyEngine() internal view {
+        require(boundaryConfigured && msg.sender == engine, "MinimalERC20: not engine");
+    }
+
+    modifier onlyDeployer() {
+        _onlyDeployer();
+        _;
+    }
+
+    modifier onlyEngine() {
+        _onlyEngine();
         _;
     }
 
-    function mint(address to, uint256 amount) external onlyOwner {
+    function configureBoundary(uint8 resourceType_, address engine_) external onlyDeployer {
+        require(!boundaryConfigured, "MinimalERC20: boundary configured");
+        require(engine_ != address(0), "MinimalERC20: zero engine");
+
+        resourceType = resourceType_;
+        engine = engine_;
+        boundaryConfigured = true;
+
+        emit BoundaryConfigured(resourceType_, engine_);
+    }
+
+    function seedTreasury(address treasury, uint256 amount) external onlyDeployer {
+        require(!treasurySeeded, "MinimalERC20: treasury seeded");
+        require(treasury != address(0), "MinimalERC20: zero treasury");
+
+        treasurySeeded = true;
+        _mint(treasury, amount);
+    }
+
+    function mint(address to, uint256 amount) external onlyEngine {
+        _mint(to, amount);
+        emit ResourceMinted(resourceType, to, amount);
+    }
+
+    function burn(address from, uint256 amount) external onlyEngine {
+        balanceOf[from] -= amount;
+        totalSupply -= amount;
+        emit Transfer(from, address(0), amount);
+        emit ResourceBurned(resourceType, from, amount);
+    }
+
+    function _mint(address to, uint256 amount) internal {
         totalSupply += amount;
         balanceOf[to] += amount;
         emit Transfer(address(0), to, amount);
diff --git a/packages/contracts/src/StubPool.sol b/packages/contracts/src/StubPool.sol
index 15fc86d..d35b4f4 100644
--- a/packages/contracts/src/StubPool.sol
+++ b/packages/contracts/src/StubPool.sol
@@ -1,18 +1,21 @@
 // SPDX-License-Identifier: MIT
 pragma solidity ^0.8.34;
 
+interface IERC20Balance {
+    function balanceOf(address account) external view returns (uint256);
+}
+
 /// @notice Constant-product AMM pool for one resource/gold pair.
-///         Reserves are tracked internally; ClanWorld calls the math
-///         functions to compute exchange rates, then mints/credits balances
-///         directly via the internal accounting model.
-///         No real ERC20 transfers occur here — the pool is the math oracle.
+///         TOKEN_A is the resource token and TOKEN_B is gold. ClanWorld owns
+///         the swap surface because clans use the engine's internal accounting,
+///         while the treasury seeds real ERC20 balances into the pool once.
 contract StubPool {
-    address public immutable TOKEN_A;  // resource token
-    address public immutable TOKEN_B;  // gold token
-    address public immutable ENGINE;   // ClanWorld address
+    address public immutable TOKEN_A; // resource token
+    address public immutable TOKEN_B; // gold token
+    address public immutable ENGINE; // ClanWorld address
 
-    uint256 public reserveA;          // resource reserve
-    uint256 public reserveB;          // gold reserve
+    uint256 public reserveA; // resource reserve
+    uint256 public reserveB; // gold reserve
 
     bool private _seeded;
 
@@ -32,45 +35,80 @@ contract StubPool {
     function seed(uint256 amountA, uint256 amountB) external onlyEngine {
         require(!_seeded, "StubPool: already seeded");
         require(amountA > 0 && amountB > 0, "StubPool: zero seed");
+        require(IERC20Balance(TOKEN_A).balanceOf(address(this)) >= amountA, "StubPool: missing token A");
+        require(IERC20Balance(TOKEN_B).balanceOf(address(this)) >= amountB, "StubPool: missing token B");
         reserveA = amountA;
         reserveB = amountB;
         _seeded = true;
     }
 
-    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
-    ///         Constant-product: goldOut = reserveB * amountIn / (reserveA + amountIn).
-    ///         Updates reserves. Called by ClanWorld; no token transfers.
-    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
+    function getAmountOutForExactIn(uint256 amountIn) public view returns (uint256 amountOut) {
         require(amountIn > 0, "StubPool: zero amount");
         require(reserveA > 0 && reserveB > 0, "StubPool: not seeded");
-        goldOut = (reserveB * amountIn) / (reserveA + amountIn);
-        require(goldOut > 0, "StubPool: zero output");
-        reserveA += amountIn;
-        reserveB -= goldOut;
+        amountOut = (reserveB * amountIn) / (reserveA + amountIn);
     }
 
-    /// @notice Quote a buy without updating reserves (view).
-    ///         goldIn = ceil(reserveB * amountOut / (reserveA - amountOut)).
-    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
+    function getAmountInForExactOut(uint256 amountOut) public view returns (uint256 amountIn) {
         require(amountOut > 0, "StubPool: zero amount");
         require(amountOut < reserveA, "StubPool: insufficient resource reserve");
-        uint256 num = reserveB * amountOut;
-        uint256 denom_ = reserveA - amountOut;
-        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
+        require(reserveB > 0, "StubPool: not seeded");
+
+        uint256 numerator = reserveB * amountOut;
+        uint256 denominator = reserveA - amountOut;
+        amountIn = numerator / denominator + (numerator % denominator == 0 ? 0 : 1);
+    }
+
+    /// @notice Exact-input sell: resource in, gold out.
+    function swapExactInForOut(uint256 amountIn, uint256 minOut) external onlyEngine returns (uint256 amountOut) {
+        return _swapExactInForOut(amountIn, minOut);
+    }
+
+    function _swapExactInForOut(uint256 amountIn, uint256 minOut) internal returns (uint256 amountOut) {
+        uint256 priorK = reserveA * reserveB;
+        amountOut = getAmountOutForExactIn(amountIn);
+        require(amountOut >= minOut, "StubPool: insufficient output");
+
+        reserveA += amountIn;
+        reserveB -= amountOut;
+
+        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
+    }
+
+    /// @notice Exact-output buy: gold in, exact resource out.
+    function swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn)
+        external
+        onlyEngine
+        returns (uint256 amountIn)
+    {
+        return _swapExactOutForInWithMaxIn(amountOut, maxIn);
+    }
+
+    function _swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn) internal returns (uint256 amountIn) {
+        uint256 priorK = reserveA * reserveB;
+        amountIn = getAmountInForExactOut(amountOut);
+        require(amountIn <= maxIn, "StubPool: max input exceeded");
+
+        reserveA -= amountOut;
+        reserveB += amountIn;
+
+        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
+    }
+
+    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
+    ///         Legacy alias for Phase 6.1 scheduled-market code.
+    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
+        goldOut = _swapExactInForOut(amountIn, 1);
+    }
+
+    /// @notice Legacy quote alias for exact-output resource buys.
+    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
+        goldIn = getAmountInForExactOut(amountOut);
     }
 
     /// @notice Exact-output buy: clan buys amountOut of resource, pays goldIn.
-    ///         Returns actual goldIn charged (ceiling arithmetic).
-    ///         Updates reserves. Called by ClanWorld; no token transfers.
+    ///         Legacy alias for Phase 6.1 scheduled-market code.
     function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
-        require(amountOut > 0, "StubPool: zero amount");
-        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
-        require(reserveB > 0, "StubPool: not seeded");
-        uint256 num = reserveB * amountOut;
-        uint256 denom_ = reserveA - amountOut;
-        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
-        reserveA -= amountOut;
-        reserveB += goldIn;
+        goldIn = _swapExactOutForInWithMaxIn(amountOut, type(uint256).max);
     }
 
     function getReserves() external view returns (uint256, uint256) {
diff --git a/packages/contracts/test/ClanWorld.t.sol b/packages/contracts/test/ClanWorld.t.sol
index 92781d3..9df303e 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -25,7 +25,6 @@ import {
     Clansman,
     Mission,
     BanditTroop,
-    ScheduledMarketAction,
     DefenseContribution,
     PackedRoute,
     DerivedClanState,
@@ -48,6 +47,19 @@ contract ClanWorldTestHarness is ClanWorld {
     function killClansman(uint32 csId) external {
         _clansmen[csId].state = ClansmanState.DEAD;
     }
+
+    function setCarry(uint32 csId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
+        _clansmen[csId].carryWood = wood;
+        _clansmen[csId].carryIron = iron;
+        _clansmen[csId].carryWheat = wheat;
+        _clansmen[csId].carryFish = fish;
+    }
+
+    function setClansmanForTest(uint32 csId, ClansmanState state, uint8 region, uint64 cooldownEndsAtTs) external {
+        _clansmen[csId].state = state;
+        _clansmen[csId].currentRegion = region;
+        _clansmen[csId].cooldownEndsAtTs = cooldownEndsAtTs;
+    }
 }
 
 contract ClanWorldTest is Test {
@@ -73,6 +85,10 @@ contract ClanWorldTest is Test {
 
     /// @dev Deploy tokens + pools, call initTreasury + seedPools. Returns wood token address.
     function _setupMarket() internal returns (address woodAddr) {
+        return _setupMarketWithWoodSeed(world.INITIAL_RESOURCE_POOL_SEED());
+    }
+
+    function _setupMarketWithWoodSeed(uint256 woodSeed) internal returns (address woodAddr) {
         woodToken = new MinimalERC20("Wood", "WOOD");
         ironToken = new MinimalERC20("Iron", "IRON");
         wheatToken = new MinimalERC20("Wheat", "WHEAT");
@@ -98,11 +114,25 @@ contract ClanWorldTest is Test {
 
         world.initTreasury(tokens, pools);
 
-        // Seed: 1000 wood + 1000 gold per pool (spot price 1 gold / 1 wood)
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        // Seed: 100,000 resource + 50,000 gold per pool (spot price 0.5 gold / resource).
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), woodSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), woodSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
-            woodSeed: resSeed,
+            woodSeed: woodSeed,
             wheatSeed: resSeed,
             fishSeed: resSeed,
             ironSeed: resSeed,
@@ -447,7 +477,7 @@ contract ClanWorldTest is Test {
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
         _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
 
-        // Advance travel back to homebase + deposit duration.
+        // Advance through travel back to homebase and the deposit's 1-tick transfer.
         (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
         for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
             _advanceTick();
@@ -465,6 +495,105 @@ contract ClanWorldTest is Test {
         cs1;
     }
 
+    function test_depositResources_woodCarryMovesToVaultAndClears() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint256 woodDelta = 5e18;
+
+        harness.setCarry(csId, woodDelta, 0, 0, 0);
+        uint256 vaultBefore = harness.getClan(clanId).vaultWood;
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
+
+        Mission memory mission = harness.getActiveMission(csId);
+        assertEq(
+            mission.settlesAtTick,
+            mission.executesAtTick + harness.getActionDuration(ActionType.DepositResources),
+            "deposit settles after transfer duration"
+        );
+
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+
+        assertEq(harness.getClan(clanId).vaultWood, vaultBefore + woodDelta, "vault wood receives carried wood");
+        assertEq(harness.getClansman(csId).carryWood, 0, "carry wood is cleared");
+    }
+
+    function test_depositResources_emptyCarryNoopsWithoutEvent() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "empty deposit order should be accepted");
+
+        vm.recordLogs();
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
+
+        for (uint256 i = 0; i < logs.length; i++) {
+            assertTrue(logs[i].topics[0] != depositedTopic, "empty deposit emits no ResourcesDeposited event");
+        }
+
+        assertFalse(harness.getActiveMission(csId).active, "empty deposit still completes");
+    }
+
+    function test_depositResources_multipleTypesMoveTogether() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint256 woodDelta = 4e18;
+        uint256 ironDelta = 2e18;
+        uint256 fishDelta = 3e18;
+
+        harness.setCarry(csId, woodDelta, ironDelta, 0, fishDelta);
+        Clan memory beforeClan = harness.getClan(clanId);
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
+
+        _advanceTickHarness(harness);
+        _advanceTickHarness(harness);
+
+        Clan memory afterClan = harness.getClan(clanId);
+        Clansman memory afterCs = harness.getClansman(csId);
+
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood + woodDelta, "wood transferred");
+        assertEq(afterClan.vaultIron, beforeClan.vaultIron + ironDelta, "iron transferred");
+        assertEq(afterClan.vaultFish, beforeClan.vaultFish + fishDelta, "fish transferred");
+        assertEq(afterCs.carryWood, 0, "wood carry cleared");
+        assertEq(afterCs.carryIron, 0, "iron carry cleared");
+        assertEq(afterCs.carryFish, 0, "fish carry cleared");
+    }
+
+    function test_depositResources_requiresHomeRegion() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+        uint8 nonHomeRegion = homeRegion == ClanWorldConstants.REGION_FOREST
+            ? ClanWorldConstants.REGION_MOUNTAINS
+            : ClanWorldConstants.REGION_FOREST;
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, nonHomeRegion, ActionType.DepositResources);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_REGION), "deposit must target home region");
+    }
+
+    function test_depositResources_eventHasCorrectDeltas() public {
+        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
+        uint8 homeRegion = harness.getClan(clanId).baseRegion;
+
+        harness.setCarry(csId, 5e18, 2e18, 1e18, 3e18);
+
+        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
+
+        _advanceTickHarness(harness);
+        vm.expectEmit(true, true, false, true);
+        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
+        _advanceTickHarness(harness);
+    }
+
     function test_quoteTravel_outOfBounds_returnsZero() public {
         (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
         assertEq(ticks, 0, "out-of-range src should return 0 ticks");
@@ -611,11 +740,267 @@ contract ClanWorldTest is Test {
         return count;
     }
 
+    function _advanceUntilNextHeartbeatCloses(uint64 tick) internal {
+        while (world.getWorldState().currentTick < tick) {
+            _advanceTick();
+        }
+    }
+
     // Helper: get the first clansman id for a clan
     function _firstCs(uint32 clanId) internal view returns (uint32) {
         return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
     }
 
+    function _setupHarnessClanAt(ClansmanState state, uint8 region, uint64 cooldownEndsAtTs)
+        internal
+        returns (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId)
+    {
+        return _setupHarnessClanAtWithWoodSeed(state, region, cooldownEndsAtTs, world.INITIAL_RESOURCE_POOL_SEED());
+    }
+
+    function _setupHarnessClanAtWithWoodSeed(
+        ClansmanState state,
+        uint8 region,
+        uint64 cooldownEndsAtTs,
+        uint256 woodSeed
+    ) internal returns (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) {
+        harness = new ClanWorldTestHarness();
+        world = harness;
+        woodAddr = _setupMarketWithWoodSeed(woodSeed);
+        clanId = _mintClan();
+        csId = _firstCs(clanId);
+        harness.setClansmanForTest(csId, state, region, cooldownEndsAtTs);
+    }
+
+    function test_immediateMarketSell_executesInSubmitTx() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        uint256 expectedGoldOut = woodPool.getAmountOutForExactIn(5e18);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ImmediateMarketActionExecuted(
+            clanId,
+            csId,
+            ActionType.MarketSell,
+            uint8(ResourceType.Wood),
+            5e18,
+            ClanWorldConstants.RESOURCE_GOLD,
+            expectedGoldOut,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate sell should be accepted");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "sell should be immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood - 5e18, "vault wood should be sold immediately");
+        assertGt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be credited immediately");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "worker should remain waiting");
+        assertEq(cs.currentRegion, ClanWorldConstants.REGION_UNICORN_TOWN, "worker should stay in town");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "immediate action should consume cooldown");
+        assertFalse(world.getActiveMission(csId).active, "immediate action should not install a mission");
+        assertEq(world.getScheduledMarketActionsForTick(world.getWorldState().currentTick).length, 0, "no queue entry");
+    }
+
+    function test_immediateMarket_townOnCooldown_fallsBackToScheduled() public {
+        uint64 cooldownEndsAt = uint64(block.timestamp + 30);
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, cooldownEndsAt);
+
+        uint256 goldBefore = world.getClan(clanId).goldBalance;
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+        Mission memory m = world.getActiveMission(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market order should schedule while cooling down");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "cooldown should force scheduled mode");
+        assertTrue(m.active, "scheduled fallback should install a mission");
+        assertEq(uint8(m.marketMode), uint8(MarketExecutionMode.Scheduled), "mission should be scheduled");
+        assertEq(
+            world.getScheduledMarketActionsForTick(m.settlesAtTick).length,
+            0,
+            "scheduled action queues when mission settles"
+        );
+        assertEq(world.getClan(clanId).goldBalance, goldBefore, "scheduled fallback should not trade immediately");
+    }
+
+    function test_immediateMarket_notInTown_fallsBackToScheduled() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_FOREST, 0);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+        Mission memory m = world.getActiveMission(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "out-of-town should be scheduled");
+        assertTrue(m.active, "scheduled fallback should install a mission");
+        assertEq(
+            world.getScheduledMarketActionsForTick(m.settlesAtTick).length,
+            0,
+            "scheduled action queues when mission settles"
+        );
+    }
+
+    function test_immediateMarket_busyWorker_fallsBackToScheduled() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.ACTING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
+        Mission memory m = world.getActiveMission(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "busy worker should be scheduled");
+        assertTrue(m.active, "scheduled fallback should install a mission");
+        assertEq(
+            world.getScheduledMarketActionsForTick(m.settlesAtTick).length,
+            0,
+            "scheduled action queues when mission settles"
+        );
+    }
+
+    function test_immediateMarketBuy_executesWhenMaxGoldSatisfied() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        uint256 expectedGoldIn = woodPool.getAmountInForExactOut(1e18);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ImmediateMarketActionExecuted(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            ClanWorldConstants.RESOURCE_GOLD,
+            expectedGoldIn,
+            uint8(ResourceType.Wood),
+            1e18,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 2e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate buy should be accepted");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "buy should be immediate");
+        assertGt(afterClan.vaultWood, beforeClan.vaultWood, "vault wood should increase immediately");
+        assertLt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be debited immediately");
+        assertFalse(world.getActiveMission(csId).active, "immediate buy should not install a mission");
+    }
+
+    function test_immediateMarket_insufficientLiquidityFailsAndConsumesCooldown() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAtWithWoodSeed(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0, 5e18);
+
+        Clan memory beforeClan = world.getClan(clanId);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Immediate,
+            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r =
+            _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 6e18, type(uint256).max);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
+        assertEq(
+            uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed immediate action stays immediate"
+        );
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
+    }
+
+    function test_immediateMarketBuy_maxGoldExceededFailsAndConsumesCooldown() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Immediate,
+            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
+    }
+
+    function test_immediateMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
+        (, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Immediate,
+            StatusCode.ERR_NOT_ENOUGH_GOLD,
+            world.getWorldState().currentTick
+        );
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 7e18, 10e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
+        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
+    }
+
+    function test_immediateMarketBuy_overCapacityRejectsAtSubmit() public {
+        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
+            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
+        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP - 5e17, 0, 0, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        Clansman memory beforeCs = world.getClansman(csId);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 10e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertEq(cs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "rejected buy should not consume cooldown");
+        assertEq(uint8(cs.state), uint8(beforeCs.state), "rejected buy should not change worker state");
+        assertFalse(world.getActiveMission(csId).active, "rejected buy should not install a mission");
+    }
+
     // -------------------------------------------------------------------------
     // Test 11: sell_creditsGold — after scheduled sell, clan.goldBalance > starter gold
     // -------------------------------------------------------------------------
@@ -628,21 +1013,34 @@ contract ClanWorldTest is Test {
         // Clan starts with 20 wood in vault (starter pack)
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        // Submit sell order — clansman travels to Unicorn Town then executes at actionStartTick
+        // Submit sell order — clansman travels to Unicorn Town then executes when the market mission settles
         OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "sell order should be accepted");
 
         // Find out which tick the action fires
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
+        uint64 executeAtTick = m.settlesAtTick;
+        uint256 expectedGoldOut = woodPool.getAmountOutForExactIn(5e18);
 
-        // Advance ticks until heartbeat closes executeAtTick
+        // Advance ticks until the heartbeat before executeAtTick
         uint64 curTick = world.getWorldState().currentTick;
-        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
-        for (uint256 i = 0; i < ticksNeeded; i++) {
+        for (uint256 i = 0; i < uint256(executeAtTick - curTick); i++) {
             _advanceTick();
         }
 
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ScheduledMarketActionExecuted(
+            clanId,
+            csId,
+            ActionType.MarketSell,
+            uint8(ResourceType.Wood),
+            5e18,
+            ClanWorldConstants.RESOURCE_GOLD,
+            expectedGoldOut,
+            executeAtTick
+        );
+        _advanceTick();
+
         // Settle clan to apply any mission resolution
         world.settleClan(clanId);
 
@@ -671,7 +1069,7 @@ contract ClanWorldTest is Test {
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
 
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
+        uint64 executeAtTick = m.settlesAtTick;
 
         uint64 curTick = world.getWorldState().currentTick;
         uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
@@ -688,7 +1086,7 @@ contract ClanWorldTest is Test {
     }
 
     // -------------------------------------------------------------------------
-    // Test 13: buy_maxGoldIn — buy fails with ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
+    // Test 13: buy_maxGoldIn — buy fails with ERR_MAX_GOLD_IN_EXCEEDED
     // -------------------------------------------------------------------------
 
     function test_buy_maxGoldIn() public {
@@ -701,7 +1099,7 @@ contract ClanWorldTest is Test {
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "order submission should succeed");
 
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
+        uint64 executeAtTick = m.settlesAtTick;
         uint64 curTick = world.getWorldState().currentTick;
 
         // Advance all ticks UP TO (but not including) the execute tick
@@ -713,14 +1111,115 @@ contract ClanWorldTest is Test {
 
         // Now the next heartbeat will close executeAtTick — that's when MarketActionFailed fires
         // Place expectEmit right before the final heartbeat
-        vm.expectEmit(true, true, false, true);
+        vm.expectEmit(true, false, false, true);
         emit IClanWorldEvents.MarketActionFailed(
-            clanId, csId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Scheduled,
+            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
+            executeAtTick
         );
         _advanceTick();
 
+        Clansman memory cs = world.getClansman(csId);
+
         // Verify gold balance unchanged (buy failed)
         assertEq(world.getClan(clanId).goldBalance, 3e18, "gold should be unchanged after failed buy");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
+    }
+
+    function test_scheduledMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 7e18, 10e18);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
+
+        Mission memory m = world.getActiveMission(csId);
+        uint64 executeAtTick = m.settlesAtTick;
+        Clan memory beforeClan = world.getClan(clanId);
+
+        _advanceUntilNextHeartbeatCloses(executeAtTick);
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Scheduled,
+            StatusCode.ERR_NOT_ENOUGH_GOLD,
+            executeAtTick
+        );
+        _advanceTick();
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
+    }
+
+    function test_scheduledMarketBuy_overCapacityRejectsAtSubmit() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP - 5e17, 0, 0, 0);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        Clansman memory beforeCs = world.getClansman(csId);
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 10e18);
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+
+        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "rejected buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "rejected buy should not debit gold");
+        assertEq(cs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "rejected buy should not consume cooldown");
+        assertEq(uint8(cs.state), uint8(beforeCs.state), "rejected buy should not change worker state");
+        assertFalse(world.getActiveMission(csId).active, "rejected buy should not install a mission");
+    }
+
+    function test_scheduledMarketBuy_insufficientLiquidityFailsAndConsumesCooldown() public {
+        address woodAddr = _setupMarketWithWoodSeed(5e18);
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        OrderResult[] memory r =
+            _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 6e18, type(uint256).max);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
+
+        Mission memory m = world.getActiveMission(csId);
+        uint64 executeAtTick = m.settlesAtTick;
+        Clan memory beforeClan = world.getClan(clanId);
+
+        _advanceUntilNextHeartbeatCloses(executeAtTick);
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.MarketActionFailed(
+            clanId,
+            csId,
+            ActionType.MarketBuy,
+            MarketExecutionMode.Scheduled,
+            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
+            executeAtTick
+        );
+        _advanceTick();
+
+        Clan memory afterClan = world.getClan(clanId);
+        Clansman memory cs = world.getClansman(csId);
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
     }
 
     // -------------------------------------------------------------------------
@@ -736,10 +1235,7 @@ contract ClanWorldTest is Test {
         assertEq(uint8(r[0].status), uint8(StatusCode.OK));
 
         Mission memory m = world.getActiveMission(csId);
-        uint64 executeAtTick = m.actionStartTick;
-
-        // Verify queue has entry before heartbeat
-        assertGt(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should have entry");
+        uint64 executeAtTick = m.settlesAtTick;
 
         uint64 curTick = world.getWorldState().currentTick;
         uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
@@ -761,7 +1257,7 @@ contract ClanWorldTest is Test {
         OrderResult[] memory r1 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first sell order should be accepted");
         Mission memory oldMission = world.getActiveMission(csId);
-        uint64 oldExecuteAtTick = oldMission.actionStartTick;
+        uint64 oldExecuteAtTick = oldMission.settlesAtTick;
 
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
         _advanceTick();
@@ -769,28 +1265,30 @@ contract ClanWorldTest is Test {
         OrderResult[] memory r2 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 2e18, 0);
         assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "replacement sell order should be accepted");
         Mission memory newMission = world.getActiveMission(csId);
-        uint64 newExecuteAtTick = newMission.actionStartTick;
+        uint64 newExecuteAtTick = newMission.settlesAtTick;
         assertGt(newMission.nonce, oldMission.nonce, "replacement should bump nonce");
 
-        ScheduledMarketAction[] memory oldQueue = world.getScheduledMarketActionsForTick(oldExecuteAtTick);
-        ScheduledMarketAction[] memory newQueue = world.getScheduledMarketActionsForTick(newExecuteAtTick);
-        assertEq(oldQueue[0].missionNonce, oldMission.nonce, "old queue captures old nonce");
-        assertEq(newQueue[0].missionNonce, newMission.nonce, "new queue captures new nonce");
+        assertEq(
+            world.getScheduledMarketActionsForTick(oldExecuteAtTick).length, 0, "old mission not queued before settle"
+        );
+        assertEq(
+            world.getScheduledMarketActionsForTick(newExecuteAtTick).length, 0, "new mission not queued before settle"
+        );
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        _advanceTick(); // close tick before the stale entry
-
-        vm.expectEmit(true, true, false, true);
-        emit IClanWorldEvents.MarketActionFailed(clanId, csId, ActionType.MarketSell, StatusCode.ERR_INVALID_ACTION);
-        _advanceTick(); // close stale entry tick
+        while (world.getWorldState().currentTick <= oldExecuteAtTick) {
+            _advanceTick();
+        }
         assertEq(world.getClan(clanId).goldBalance, goldBefore, "stale sell must not execute");
 
-        _advanceTick(); // close replacement entry tick
+        while (world.getWorldState().currentTick <= newExecuteAtTick) {
+            _advanceTick();
+        }
         assertGt(world.getClan(clanId).goldBalance, goldBefore, "replacement sell should execute");
     }
 
-    function test_scheduledMarket_defersActionsAbovePerTickCap() public {
+    function test_scheduledMarket_executesAllActionsForClosedTick() public {
         address woodAddr = _setupMarket();
         uint32[] memory distOneClans = new uint32[](12);
         uint32[] memory distTwoClans = new uint32[](12);
@@ -819,192 +1317,89 @@ contract ClanWorldTest is Test {
             totalQueued += _submitAllClanMarketSells(distOneClans[i], woodAddr);
         }
 
-        uint64 executeAtTick = 2;
-        uint256 cap = world.MAX_MARKET_ACTIONS_PER_TICK();
-        assertGt(totalQueued, cap, "test setup must exceed cap");
-        assertEq(
-            world.getScheduledMarketActionsForTick(executeAtTick).length,
-            totalQueued,
-            "all aligned actions should share tick 2"
-        );
-
-        _advanceTick(); // close tick 1
-        _advanceTick(); // close tick 2, process cap and defer remainder
-
-        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "original tick queue cleared");
-        assertEq(
-            world.getScheduledMarketActionsForTick(executeAtTick + 1).length,
-            totalQueued - cap,
-            "overflow actions deferred to next tick"
-        );
-
-        _advanceTick(); // close tick 3, process deferred actions
-        assertEq(world.getScheduledMarketActionsForTick(executeAtTick + 1).length, 0, "deferred queue cleared");
-    }
-
-    function test_scheduledMarket_overflowExecutesBeforeNativeNextTickActions() public {
-        address woodAddr = _setupMarket();
-        uint32[] memory distOneClans = new uint32[](12);
-        uint32[] memory distTwoClans = new uint32[](12);
-        uint256 distOneCount;
-        uint256 distTwoCount;
-
-        for (uint256 i = 0; i < 12; i++) {
-            uint32 clanId = _mintClan();
-            ClanFullView memory view_ = world.getClanFullView(clanId);
-            (uint8 travelTicks,) = world.quoteTravel(view_.clan.clan.baseRegion, ClanWorldConstants.REGION_UNICORN_TOWN);
-            if (travelTicks == 1) {
-                distOneClans[distOneCount++] = clanId;
-            } else if (travelTicks == 2) {
-                distTwoClans[distTwoCount++] = clanId;
-            }
-        }
-        assertGt(distTwoCount, 0, "test setup needs a distance-two clan");
-
-        uint32 nativeClanId = distTwoClans[0];
-        ClanFullView memory nativeView = world.getClanFullView(nativeClanId);
-        uint32 nativeCsId = nativeView.clansmen[3].clansman.clansman.clansmanId;
-
-        uint256 totalQueuedForTickTwo = _submitFirstClanMarketSells(nativeClanId, woodAddr, 3);
-        for (uint256 i = 1; i < distTwoCount; i++) {
-            totalQueuedForTickTwo += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
-        }
-
-        _advanceTick();
-
-        for (uint256 i = 0; i < distOneCount; i++) {
-            totalQueuedForTickTwo += _submitAllClanMarketSells(distOneClans[i], woodAddr);
-        }
-
-        OrderResult[] memory nativeResult =
-            _submitMarketOrder(nativeClanId, nativeCsId, ActionType.MarketSell, woodAddr, 1e18, 0);
-        assertEq(uint8(nativeResult[0].status), uint8(StatusCode.OK), "native next-tick action should enqueue");
-
-        uint64 overflowTick = 2;
-        uint64 nextTick = overflowTick + 1;
-        uint256 cap = world.MAX_MARKET_ACTIONS_PER_TICK();
-        assertGt(totalQueuedForTickTwo, cap, "test setup must exceed cap");
-
-        ScheduledMarketAction[] memory nativeQueueBefore = world.getScheduledMarketActionsForTick(nextTick);
-        assertEq(nativeQueueBefore.length, 1, "native next-tick queue should exist before overflow merge");
-        uint64 nativeSeq = nativeQueueBefore[0].commitSequence;
+        uint64 executeAtTick = 3;
+        bytes32 executedSig =
+            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
 
         _advanceTick(); // close tick 1
-        _advanceTick(); // close tick 2, process cap and defer overflow into tick 3
+        _advanceTick(); // close tick 2
 
-        uint256 overflowCount = totalQueuedForTickTwo - cap;
-        ScheduledMarketAction[] memory mergedQueue = world.getScheduledMarketActionsForTick(nextTick);
-        assertEq(mergedQueue.length, overflowCount + 1, "overflow should merge with native next-tick action");
-        assertEq(
-            mergedQueue[mergedQueue.length - 1].commitSequence,
-            nativeSeq,
-            "native action must stay after older overflow"
-        );
-        for (uint256 i = 1; i < mergedQueue.length; i++) {
-            assertGt(mergedQueue[i].commitSequence, mergedQueue[i - 1].commitSequence, "merged queue must be FIFO");
-        }
-
-        bytes32 executedSig =
-            keccak256("ScheduledMarketActionExecuted(uint64,uint64,uint32,uint32,address,address,uint256,uint256)");
         vm.recordLogs();
-        _advanceTick(); // close tick 3 and execute the sorted merged queue
+        _advanceTick(); // close tick 3 and execute every scheduled action for the tick
         Vm.Log[] memory logs = vm.getRecordedLogs();
 
-        bool sawExecution;
-        uint64 previousSeq;
         uint256 executedCount;
         for (uint256 i = 0; i < logs.length; i++) {
             if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != executedSig) {
                 continue;
             }
-            uint64 executedTick = uint64(uint256(logs[i].topics[1]));
-            if (executedTick != nextTick) continue;
-
-            uint64 seq = uint64(uint256(logs[i].topics[2]));
-            if (sawExecution) assertGt(seq, previousSeq, "execution events must be FIFO");
-            sawExecution = true;
-            previousSeq = seq;
+            (,,,,,, uint64 settledAtTick) =
+                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
+            if (settledAtTick != executeAtTick) continue;
             executedCount++;
         }
 
-        assertEq(executedCount, overflowCount + 1, "all merged actions should execute");
-        assertEq(previousSeq, nativeSeq, "native action should execute after older overflow actions");
+        assertEq(executedCount, totalQueued, "all same-tick actions should execute");
+        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
     }
 
     // -------------------------------------------------------------------------
-    // Test 15: scheduledMarket_fifo — two clans queue sells; commitSequence is FIFO
+    // Test 15: scheduledMarket_fifo — same-tick sells execute in submission order
     // -------------------------------------------------------------------------
 
     function test_scheduledMarket_fifo() public {
         address woodAddr = _setupMarket();
+        uint32 clanId = _mintClan();
+        ClanFullView memory view_ = world.getClanFullView(clanId);
 
-        // Mint two clans — they get region 1 and region 2 respectively
-        uint32 clanId1 = _mintClan();
-        vm.prank(elder2);
-        (uint32 clanId2,) = world.mintClan(elder2);
-
-        uint32 csId1 = _firstCs(clanId1);
-        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
-
-        // Submit clan1's sell order first (commitSequence = 0)
-        ClanOrder[] memory orders1 = new ClanOrder[](1);
-        orders1[0] = ClanOrder({
-            clansmanId: csId1,
-            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
-            action: ActionType.MarketSell,
-            targetClanId: 0,
-            marketToken: woodAddr,
-            marketAmount: 2e18,
-            maxGoldIn: 0
-        });
+        ClanOrder[] memory orders = new ClanOrder[](3);
+        for (uint256 i = 0; i < orders.length; i++) {
+            orders[i] = ClanOrder({
+                clansmanId: view_.clansmen[i].clansman.clansman.clansmanId,
+                gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+                action: ActionType.MarketSell,
+                targetClanId: 0,
+                marketToken: woodAddr,
+                marketAmount: uint256(i + 1) * 1e18,
+                maxGoldIn: 0
+            });
+        }
         vm.prank(elder);
-        OrderResult[] memory r1 = world.submitClanOrders(clanId1, orders1);
-        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "clan1 sell order ok");
+        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
+        for (uint256 i = 0; i < results.length; i++) {
+            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "sell order ok");
+        }
 
-        // Submit clan2's sell order second (commitSequence = 1)
-        ClanOrder[] memory orders2 = new ClanOrder[](1);
-        orders2[0] = ClanOrder({
-            clansmanId: csId2,
-            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
-            action: ActionType.MarketSell,
-            targetClanId: 0,
-            marketToken: woodAddr,
-            marketAmount: 2e18,
-            maxGoldIn: 0
-        });
-        vm.prank(elder2);
-        OrderResult[] memory r2 = world.submitClanOrders(clanId2, orders2);
-        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "clan2 sell order ok");
+        Mission memory m = world.getActiveMission(orders[0].clansmanId);
+        uint64 executeAtTick = m.settlesAtTick;
+        bytes32 executedSig =
+            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
 
-        // Verify FIFO: clan1 committed before clan2 so has lower commitSequence
-        Mission memory m1 = world.getActiveMission(csId1);
-        Mission memory m2 = world.getActiveMission(csId2);
+        while (world.getWorldState().currentTick < executeAtTick) {
+            _advanceTick();
+        }
 
-        // Check commitSequence in the queues for each clan's respective tick
-        ScheduledMarketAction[] memory q1 = world.getScheduledMarketActionsForTick(m1.actionStartTick);
-        ScheduledMarketAction[] memory q2 = world.getScheduledMarketActionsForTick(m2.actionStartTick);
+        vm.recordLogs();
+        _advanceTick();
+        Vm.Log[] memory logs = vm.getRecordedLogs();
 
-        uint64 seq1;
-        uint64 seq2;
-        for (uint256 i = 0; i < q1.length; i++) {
-            if (q1[i].clanId == clanId1) seq1 = q1[i].commitSequence;
-        }
-        for (uint256 i = 0; i < q2.length; i++) {
-            if (q2[i].clanId == clanId2) seq2 = q2[i].commitSequence;
-        }
-        assertLt(seq1, seq2, "clan1 submitted first: lower commitSequence");
+        uint256 executedCount;
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != executedSig) {
+                continue;
+            }
+            (,, uint8 resourceIn, uint256 amountIn,,, uint64 settledAtTick) =
+                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
+            if (settledAtTick != executeAtTick) continue;
 
-        // Advance ticks to cover both actions
-        uint64 curTick = world.getWorldState().currentTick;
-        uint64 maxTick = m1.actionStartTick > m2.actionStartTick ? m1.actionStartTick : m2.actionStartTick;
-        uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
-        for (uint256 i = 0; i < ticksNeeded; i++) {
-            _advanceTick();
+            assertEq(uint32(uint256(logs[i].topics[1])), clanId, "same clan should execute");
+            assertEq(resourceIn, uint8(ResourceType.Wood), "sell should input wood");
+            assertEq(amountIn, uint256(executedCount + 1) * 1e18, "execution should be FIFO");
+            executedCount++;
         }
 
-        // Both clans should have gained gold (starter 3e18 + sell proceeds)
-        assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have gained gold from sell");
-        assertGt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have gained gold from sell");
+        assertEq(executedCount, 3, "three same-tick sells should execute");
+        assertGt(world.getClan(clanId).goldBalance, 3e18, "clan should gain gold from sells");
     }
 
     // -------------------------------------------------------------------------
@@ -1052,16 +1447,41 @@ contract ClanWorldTest is Test {
         Mission memory m1 = world.getActiveMission(csId1);
         Mission memory m2 = world.getActiveMission(csId2);
 
-        uint64 maxTick = m1.actionStartTick > m2.actionStartTick ? m1.actionStartTick : m2.actionStartTick;
+        uint64 maxTick = m1.settlesAtTick > m2.settlesAtTick ? m1.settlesAtTick : m2.settlesAtTick;
         uint64 curTick = world.getWorldState().currentTick;
         uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
+        bytes32 scheduledSig =
+            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
+        vm.recordLogs();
         for (uint256 i = 0; i < ticksNeeded; i++) {
             _advanceTick();
         }
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+
+        bool sawClan1SellEvent;
+        bool sawClan2BuyEvent;
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != scheduledSig) {
+                continue;
+            }
+
+            uint32 eventClanId = uint32(uint256(logs[i].topics[1]));
+            (uint32 eventCsId, ActionType eventAction,,,,,) =
+                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
+
+            if (eventClanId == clanId1 && eventCsId == csId1 && eventAction == ActionType.MarketSell) {
+                sawClan1SellEvent = true;
+            }
+            if (eventClanId == clanId2 && eventCsId == csId2 && eventAction == ActionType.MarketBuy) {
+                sawClan2BuyEvent = true;
+            }
+        }
 
         world.settleClan(clanId1);
         world.settleClan(clanId2);
 
+        assertTrue(sawClan1SellEvent, "sell event should carry clan1 id");
+        assertTrue(sawClan2BuyEvent, "buy event should carry clan2 id");
         // Clan1 sold wood → gold should increase
         assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have more gold after sell");
         // Clan2 bought wood → vault wood should increase beyond starter 20e18
@@ -1070,6 +1490,49 @@ contract ClanWorldTest is Test {
         assertLt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have less gold after buy");
     }
 
+    function test_scheduledMarketFailure_doesNotAffectAnotherClan() public {
+        address woodAddr = _setupMarket();
+
+        uint32 clanId1 = _mintClan();
+        vm.prank(elder2);
+        (uint32 clanId2,) = world.mintClan(elder2);
+
+        uint32 csId1 = _firstCs(clanId1);
+        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
+
+        OrderResult[] memory buyFail = _submitMarketOrder(clanId1, csId1, ActionType.MarketBuy, woodAddr, 1e18, 0);
+        assertEq(uint8(buyFail[0].status), uint8(StatusCode.OK), "failing buy should enqueue");
+
+        ClanOrder[] memory sellOrders = new ClanOrder[](1);
+        sellOrders[0] = ClanOrder({
+            clansmanId: csId2,
+            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
+            action: ActionType.MarketSell,
+            targetClanId: 0,
+            marketToken: woodAddr,
+            marketAmount: 5e18,
+            maxGoldIn: 0
+        });
+        vm.prank(elder2);
+        OrderResult[] memory sellOk = world.submitClanOrders(clanId2, sellOrders);
+        assertEq(uint8(sellOk[0].status), uint8(StatusCode.OK), "other clan sell should enqueue");
+
+        uint64 tick1 = world.getActiveMission(csId1).settlesAtTick;
+        uint64 tick2 = world.getActiveMission(csId2).settlesAtTick;
+        uint64 maxTick = tick1 > tick2 ? tick1 : tick2;
+        Clan memory failingBefore = world.getClan(clanId1);
+        uint256 sellerGoldBefore = world.getClan(clanId2).goldBalance;
+
+        while (world.getWorldState().currentTick <= maxTick) {
+            _advanceTick();
+        }
+
+        Clan memory failingAfter = world.getClan(clanId1);
+        assertEq(failingAfter.vaultWood, failingBefore.vaultWood, "failed buy should not credit resources");
+        assertEq(failingAfter.goldBalance, failingBefore.goldBalance, "failed buy should not debit gold");
+        assertGt(world.getClan(clanId2).goldBalance, sellerGoldBefore, "other clan sell should execute");
+    }
+
     // -------------------------------------------------------------------------
     // Test 17: marketOrder_rejectsInvalidRegion — non-UT market order rejected
     // -------------------------------------------------------------------------
@@ -1293,15 +1756,14 @@ contract ClanWorldTest is Test {
         uint32 csId = v.clansmen[0].clansman.clansman.clansmanId;
         uint8 homeRegion = v.clan.clan.baseRegion;
 
-        // Send clansman to homebase to DepositResources (empty carry → completes in 1 tick).
-        // This is the cleanest single-tick completion: _doDeposit with empty carry calls _completeMission immediately.
+        // Send clansman to homebase to DepositResources (empty carry -> completes when the arrival tick closes).
+        // _doDeposit with empty carry calls _completeMission immediately during settlement.
         OrderResult[] memory r = _submitOrder(clanId, csId, homeRegion, ActionType.DepositResources);
         assertEq(uint8(r[0].status), uint8(StatusCode.OK), "3.E5: order must succeed");
 
         // Wait for submit-time cooldown to expire (warp only; no ticks yet)
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
 
-        // Advance until the one-tick deposit duration has closed.
         _advanceTick();
         _advanceTick();
 
@@ -1814,7 +2276,11 @@ contract ClanWorldTest is Test {
         // Call settleClansman on-demand — must be idempotent (no crash, no double-credit).
         uint256 ironBefore = world.getClansman(csId).carryIron;
         world.settleClansman(csId);
-        assertEq(world.getClansman(csId).carryIron, ironBefore, "onDemand: settleClansman is idempotent after heartbeat settle");
+        assertEq(
+            world.getClansman(csId).carryIron,
+            ironBefore,
+            "onDemand: settleClansman is idempotent after heartbeat settle"
+        );
     }
 
     // -------------------------------------------------------------------------
@@ -1921,8 +2387,7 @@ contract ClanWorldTest is Test {
         assertEq(ws.seasonStartTick, 0);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
         assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
         );
         assertFalse(ws.winterActive);
     }
@@ -1931,8 +2396,7 @@ contract ClanWorldTest is Test {
         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
         // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
-        uint64 winterStart =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         for (uint64 i = 0; i < winterStart - 1; i++) {
             vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
             world.heartbeat();
diff --git a/packages/contracts/test/Gathering.t.sol b/packages/contracts/test/Gathering.t.sol
new file mode 100644
index 0000000..3d4f8a4
--- /dev/null
+++ b/packages/contracts/test/Gathering.t.sol
@@ -0,0 +1,134 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {
+    ClanWorldConstants,
+    ActionType,
+    ClansmanState,
+    StatusCode,
+    Clansman,
+    Mission,
+    ClanFullView,
+    ClanOrder,
+    OrderResult
+} from "../src/IClanWorld.sol";
+
+contract GatheringHarness is ClanWorld {
+    function setCarryWood(uint32 csId, uint256 wood) external {
+        _clansmen[csId].carryWood = wood;
+    }
+}
+
+contract GatheringTest is Test {
+    GatheringHarness world;
+    address elder = address(0xA1);
+
+    function setUp() public {
+        world = new GatheringHarness();
+    }
+
+    function _advanceTick() internal {
+        vm.warp(world.getWorldState().nextHeartbeatAtTs);
+        world.heartbeat();
+    }
+
+    function _advanceUntilCurrentTick(uint64 targetTick) internal {
+        while (world.getWorldState().currentTick < targetTick) {
+            _advanceTick();
+        }
+    }
+
+    function _mintClan() internal returns (uint32 clanId) {
+        vm.prank(elder);
+        (clanId,) = world.mintClan(elder);
+    }
+
+    function _firstCs(uint32 clanId) internal view returns (uint32) {
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        return view_.clansmen[0].clansman.clansman.clansmanId;
+    }
+
+    function _submitChopWood(uint32 clanId, uint32 csId) internal returns (OrderResult[] memory) {
+        ClanOrder[] memory orders = new ClanOrder[](1);
+        orders[0] = ClanOrder({
+            clansmanId: csId,
+            gotoRegion: ClanWorldConstants.REGION_FOREST,
+            action: ActionType.ChopWood,
+            targetClanId: 0,
+            marketToken: address(0),
+            marketAmount: 0,
+            maxGoldIn: 0
+        });
+
+        vm.prank(elder);
+        return world.submitClanOrders(clanId, orders);
+    }
+
+    function _settleChopWood(uint32 clanId, uint32 csId) internal returns (Clansman memory) {
+        OrderResult[] memory result = _submitChopWood(clanId, csId);
+        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
+
+        Mission memory mission = world.getActiveMission(csId);
+        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
+        world.settleClan(clanId);
+        return world.getClansman(csId);
+    }
+
+    function test_chopWoodAtForestYieldsBaseTimesActionDuration() public {
+        vm.prevrandao(bytes32(uint256(2)));
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
+        assertEq(cs.carryWood, expected, "base wood yield");
+    }
+
+    function test_chopWoodCritDistributionAcrossSeeds() public {
+        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
+        uint256 critCount = 0;
+        world = new GatheringHarness();
+        uint256 cleanState = vm.snapshotState();
+
+        for (uint256 i = 0; i < 100; i++) {
+            assertTrue(vm.revertToState(cleanState), "reset gathering world");
+            vm.prevrandao(bytes32(uint256(i + 10_000)));
+            uint32 clanId = _mintClan();
+            uint32 csId = _firstCs(clanId);
+
+            Clansman memory cs = _settleChopWood(clanId, csId);
+            if (cs.carryWood == baseYield * 2) {
+                critCount++;
+            } else {
+                assertEq(cs.carryWood, baseYield, "non-crit yield");
+            }
+        }
+
+        assertGe(critCount, 3, "crit count too low");
+        assertLe(critCount, 20, "crit count too high");
+    }
+
+    function test_chopWoodClampsToCarryCap() public {
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+        world.setCarryWood(csId, ClanWorldConstants.WOOD_CARRY_CAP - 1e18);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        assertEq(cs.carryWood, ClanWorldConstants.WOOD_CARRY_CAP, "wood carry cap");
+    }
+
+    function test_chopWoodAppliesCooldownPostSettle() public {
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
+        assertFalse(world.getActiveMission(csId).active, "mission inactive");
+        assertGt(cs.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
+    }
+}
diff --git a/packages/contracts/test/HeartbeatOrdering.t.sol b/packages/contracts/test/HeartbeatOrdering.t.sol
index 9d012e8..9f73531 100644
--- a/packages/contracts/test/HeartbeatOrdering.t.sol
+++ b/packages/contracts/test/HeartbeatOrdering.t.sol
@@ -149,8 +149,22 @@ contract HeartbeatOrderingTest is Test {
         address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
         world.initTreasury(tokens, pools);
 
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
             woodSeed: resSeed,
             wheatSeed: resSeed,
@@ -169,10 +183,10 @@ contract HeartbeatOrderingTest is Test {
     //
     // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
     // heartbeat closing tick T:
-    //   - cs0 is placed at Mountains (region 2). Deposit to homebase Forest (region 1)
-    //     = 1 tick travel. arrivalTick = T0+1, settlesAtTick = T0+2.
+    //   - cs0 is placed at Unicorn Town (region 3). Deposit to homebase Forest
+    //     (region 1) = 2 ticks travel. arrivalTick = T0+2, settlesAtTick = T0+3.
     //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
-    //     executeAtTick = T0+2. (Same tick as cs0 settles.)
+    //     settlesAtTick = T0+3. (Same tick as cs0 settles.)
     //   - vault starts at 0; cs0 has carry wood.
     //   - Heartbeat at T0+2: Step 1 deposits cs0 carry wood to vault, Step 2 sells
     //     it. Gold increases only if Step 1 ran first (vault was 0 before Step 1).
@@ -186,22 +200,24 @@ contract HeartbeatOrderingTest is Test {
 
         uint64 t0 = world.getWorldState().currentTick;
 
-        // Place cs0 at Mountains (region 2). Homebase = Forest (region 1).
-        // Deposit from Mountains to Forest: travel = 1 tick.
-        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
+        // Place cs0 at Unicorn Town (region 3). Homebase = Forest (region 1).
+        // Deposit from Unicorn Town to Forest: travel = 2 ticks.
+        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_UNICORN_TOWN);
 
-        // cs0: submit Deposit. arrivalTick = t0+1, settlesAtTick = t0+2.
-        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
+        // cs0: submit Deposit. arrivalTick = t0+2, settlesAtTick = t0+3.
+        OrderResult[] memory r0 =
+            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
         assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
         Mission memory depositMission = world.getActiveMission(csId0);
-        assertEq(depositMission.settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
+        assertEq(depositMission.settlesAtTick, t0 + 3, "deposit settlesAtTick must be t0+3");
 
         // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
-        // executeAtTick = t0+2. Same tick as deposit settles.
+        // settlesAtTick = t0+3. Same tick as deposit settles.
         OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
         Mission memory sellMission = world.getActiveMission(csId1);
         assertEq(sellMission.arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
+        assertEq(sellMission.settlesAtTick, t0 + 3, "sell settlesAtTick must be t0+3");
 
         // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
         world.setCarryWood(csId0, 10e18);
@@ -210,11 +226,11 @@ contract HeartbeatOrderingTest is Test {
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        // Advance to tick t0+2. The heartbeat closing t0+2 runs:
-        //   Step 1: _settleCompletingMissions(t0+2) → deposit fires, cs0 carry 10e18 → vault
-        //   Step 2: _executeScheduledMarketActions(t0+2) → sell fires, 5e18 vault wood → gold
+        // Advance to tick t0+4. The heartbeat closing t0+3 runs:
+        //   Step 1: _settleCompletingMissions(t0+3) → deposit fires, cs0 carry 10e18 → vault
+        //   Step 2: _executeScheduledMarketActions(t0+3) → sell fires, 5e18 vault wood → gold
         // If reversed: sell would fail (vault=0), gold unchanged.
-        _advanceToTick(t0 + 3);
+        _advanceToTick(t0 + 4);
 
         uint256 goldAfter = world.getClan(clanId).goldBalance;
         assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
@@ -302,9 +318,9 @@ contract HeartbeatOrderingTest is Test {
     //
     // Proves that when a mission settles at tick T AND a market action is queued at
     // tick T, both fire in the same heartbeat without conflict.
-    //   - cs0 placed at Mountains, deposits to Forest homebase: settlesAtTick = T0+2
-    //   - cs1 sells wood to UT from Forest: executeAtTick = T0+2
-    //   Both succeed in the same heartbeat call at T0+2.
+    //   - cs0 placed at Unicorn Town, deposits to Forest homebase: settlesAtTick = T0+3
+    //   - cs1 sells wood to UT from Forest: settlesAtTick = T0+3
+    //   Both succeed in the same heartbeat call at T0+3.
     // -------------------------------------------------------------------------
     function test_heartbeat_multipleStepsInOneTick() public {
         _setupMarket();
@@ -314,26 +330,28 @@ contract HeartbeatOrderingTest is Test {
 
         uint64 t0 = world.getWorldState().currentTick;
 
-        // cs0: placed at Mountains (1 tick from Forest homebase).
-        // Deposit: arrivalTick = t0+1, settlesAtTick = t0+2.
-        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
+        // cs0: placed at Unicorn Town (2 ticks from Forest homebase).
+        // Deposit: arrivalTick = t0+2, settlesAtTick = t0+3.
+        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_UNICORN_TOWN);
         world.setCarryWood(csId0, 10e18);
-        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
+        OrderResult[] memory r0 =
+            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
         assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
-        assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
+        assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 3, "deposit settlesAtTick must be t0+3");
 
-        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. executeAtTick = t0+2.
+        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. settlesAtTick = t0+3.
         // Vault has 20e18 starter wood — sell always has enough.
         OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
-        assertEq(world.getActiveMission(csId1).arrivalTick, t0 + 2, "sell executeAtTick must be t0+2");
+        assertEq(world.getActiveMission(csId1).arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
+        assertEq(world.getActiveMission(csId1).settlesAtTick, t0 + 3, "sell settlesAtTick must be t0+3");
 
         uint256 goldBefore = world.getClan(clanId).goldBalance;
 
-        // Advance to tick t0+3 (the heartbeat closing t0+2 runs step1+step2 together).
-        _advanceToTick(t0 + 3);
+        // Advance to tick t0+4 (the heartbeat closing t0+3 runs step1+step2 together).
+        _advanceToTick(t0 + 4);
 
-        // Both cs0 deposit (settled at T0+2) and cs1 sell (at T0+2) must have fired.
+        // Both cs0 deposit (settled at T0+3) and cs1 sell (at T0+3) must have fired.
         assertEq(world.getClansman(csId0).carryWood, 0, "deposit settled: carry cleared");
         assertGt(world.getClan(clanId).goldBalance, goldBefore, "sell executed: gold increased");
         assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
diff --git a/packages/contracts/test/Reentrancy.t.sol b/packages/contracts/test/Reentrancy.t.sol
index 30632fe..fd4d3f1 100644
--- a/packages/contracts/test/Reentrancy.t.sol
+++ b/packages/contracts/test/Reentrancy.t.sol
@@ -125,7 +125,7 @@ contract ReentrancyTest is Test {
 
         Mission memory mission = world.getActiveMission(csId);
         uint64 currentTick = world.getWorldState().currentTick;
-        uint256 ticksNeeded = uint256(mission.actionStartTick - currentTick) + 1;
+        uint256 ticksNeeded = uint256(mission.settlesAtTick - currentTick) + 1;
         for (uint256 i = 0; i < ticksNeeded; i++) {
             _advanceTick();
         }
@@ -160,8 +160,22 @@ contract ReentrancyTest is Test {
         address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
         world.initTreasury(tokens, pools);
 
-        uint256 resSeed = 1000e18;
-        uint256 goldSeed = 1000e18;
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+
         PoolSeedConfig memory cfg = PoolSeedConfig({
             woodSeed: resSeed,
             wheatSeed: resSeed,
diff --git a/packages/contracts/test/ResourceBoundaryTokens.t.sol b/packages/contracts/test/ResourceBoundaryTokens.t.sol
new file mode 100644
index 0000000..864ee1f
--- /dev/null
+++ b/packages/contracts/test/ResourceBoundaryTokens.t.sol
@@ -0,0 +1,105 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {MinimalERC20} from "../src/MinimalERC20.sol";
+import {StubPool} from "../src/StubPool.sol";
+import {ResourceType} from "../src/IClanWorld.sol";
+
+contract ResourceBoundaryTokensTest is Test {
+    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
+    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
+
+    ClanWorld world;
+    MinimalERC20 woodToken;
+    MinimalERC20 ironToken;
+    MinimalERC20 wheatToken;
+    MinimalERC20 fishToken;
+    MinimalERC20 goldToken;
+    MinimalERC20 blueprintToken;
+
+    function setUp() public {
+        world = new ClanWorld();
+        woodToken = new MinimalERC20("Wood", "WOOD");
+        ironToken = new MinimalERC20("Iron", "IRON");
+        wheatToken = new MinimalERC20("Wheat", "WHEAT");
+        fishToken = new MinimalERC20("Fish", "FISH");
+        goldToken = new MinimalERC20("Gold", "GOLD");
+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
+
+        woodToken.configureBoundary(uint8(ResourceType.Wood), address(world));
+        ironToken.configureBoundary(uint8(ResourceType.Iron), address(world));
+        wheatToken.configureBoundary(uint8(ResourceType.Wheat), address(world));
+        fishToken.configureBoundary(uint8(ResourceType.Fish), address(world));
+    }
+
+    function test_getResourceTokenReturnsConfiguredBoundaryTokens() public {
+        StubPool woodPool = new StubPool(address(woodToken), address(goldToken), address(world));
+        StubPool wheatPool = new StubPool(address(wheatToken), address(goldToken), address(world));
+        StubPool fishPool = new StubPool(address(fishToken), address(goldToken), address(world));
+        StubPool ironPool = new StubPool(address(ironToken), address(goldToken), address(world));
+
+        address[6] memory tokens = [
+            address(woodToken),
+            address(ironToken),
+            address(wheatToken),
+            address(fishToken),
+            address(goldToken),
+            address(blueprintToken)
+        ];
+        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
+
+        world.initTreasury(tokens, pools);
+
+        assertEq(world.getResourceToken(uint8(ResourceType.Wood)), address(woodToken), "wood");
+        assertEq(world.getResourceToken(uint8(ResourceType.Iron)), address(ironToken), "iron");
+        assertEq(world.getResourceToken(uint8(ResourceType.Wheat)), address(wheatToken), "wheat");
+        assertEq(world.getResourceToken(uint8(ResourceType.Fish)), address(fishToken), "fish");
+    }
+
+    function test_onlyEngineCanMintAndBurnBoundaryTokens() public {
+        address holder = address(0xCAFE);
+
+        vm.expectRevert(bytes("MinimalERC20: not engine"));
+        woodToken.mint(holder, 10e18);
+
+        vm.expectEmit(true, true, false, true, address(woodToken));
+        emit ResourceMinted(uint8(ResourceType.Wood), holder, 10e18);
+        vm.prank(address(world));
+        woodToken.mint(holder, 10e18);
+
+        assertEq(woodToken.balanceOf(holder), 10e18, "minted balance");
+
+        vm.expectEmit(true, true, false, true, address(woodToken));
+        emit ResourceBurned(uint8(ResourceType.Wood), holder, 4e18);
+        vm.prank(address(world));
+        woodToken.burn(holder, 4e18);
+
+        assertEq(woodToken.balanceOf(holder), 6e18, "burned balance");
+    }
+
+    function test_seedTreasuryMintsStartingSupply() public {
+        address treasury = address(0xBEEF);
+
+        woodToken.seedTreasury(treasury, 1000e18);
+
+        assertEq(woodToken.balanceOf(treasury), 1000e18, "treasury balance");
+        assertEq(woodToken.totalSupply(), 1000e18, "total supply");
+    }
+
+    function test_seedTreasuryCanOnlyRunOnce() public {
+        address treasury = address(0xBEEF);
+
+        woodToken.seedTreasury(treasury, 1000e18);
+
+        vm.expectRevert(bytes("MinimalERC20: treasury seeded"));
+        woodToken.seedTreasury(treasury, 1e18);
+    }
+
+    function test_onlyDeployerCanSeedTreasury() public {
+        vm.prank(address(0xBAD));
+        vm.expectRevert(bytes("MinimalERC20: not deployer"));
+        woodToken.seedTreasury(address(0xBEEF), 1000e18);
+    }
+}
diff --git a/packages/contracts/test/SeedPools.t.sol b/packages/contracts/test/SeedPools.t.sol
new file mode 100644
index 0000000..0252a89
--- /dev/null
+++ b/packages/contracts/test/SeedPools.t.sol
@@ -0,0 +1,148 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {MinimalERC20} from "../src/MinimalERC20.sol";
+import {StubPool} from "../src/StubPool.sol";
+import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
+
+contract SeedPoolsTest is Test {
+    ClanWorld world;
+    MinimalERC20 woodToken;
+    MinimalERC20 ironToken;
+    MinimalERC20 wheatToken;
+    MinimalERC20 fishToken;
+    MinimalERC20 goldToken;
+    MinimalERC20 blueprintToken;
+    StubPool woodPool;
+    StubPool ironPool;
+    StubPool wheatPool;
+    StubPool fishPool;
+
+    function setUp() public {
+        world = new ClanWorld();
+
+        woodToken = new MinimalERC20("Wood", "WOOD");
+        ironToken = new MinimalERC20("Iron", "IRON");
+        wheatToken = new MinimalERC20("Wheat", "WHEAT");
+        fishToken = new MinimalERC20("Fish", "FISH");
+        goldToken = new MinimalERC20("Gold", "GOLD");
+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
+
+        address engine = address(world);
+        woodPool = new StubPool(address(woodToken), address(goldToken), engine);
+        wheatPool = new StubPool(address(wheatToken), address(goldToken), engine);
+        fishPool = new StubPool(address(fishToken), address(goldToken), engine);
+        ironPool = new StubPool(address(ironToken), address(goldToken), engine);
+
+        address[6] memory tokens = [
+            address(woodToken),
+            address(ironToken),
+            address(wheatToken),
+            address(fishToken),
+            address(goldToken),
+            address(blueprintToken)
+        ];
+        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
+        world.initTreasury(tokens, pools);
+
+        _seedTreasuryAndApprove();
+        world.seedPools(_seedConfig());
+    }
+
+    function test_deploysFourPoolsAndGetterReturnsExpectedPools() public {
+        assertEq(world.getPool(uint8(ResourceType.Wood)), address(woodPool), "wood pool");
+        assertEq(world.getPool(uint8(ResourceType.Wheat)), address(wheatPool), "wheat pool");
+        assertEq(world.getPool(uint8(ResourceType.Fish)), address(fishPool), "fish pool");
+        assertEq(world.getPool(uint8(ResourceType.Iron)), address(ironPool), "iron pool");
+    }
+
+    function test_treasurySeedingTransfersExpectedPoolBalances() public {
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+
+        _assertPoolSeeded(woodToken, woodPool, resSeed, goldSeed, "wood");
+        _assertPoolSeeded(wheatToken, wheatPool, resSeed, goldSeed, "wheat");
+        _assertPoolSeeded(fishToken, fishPool, resSeed, goldSeed, "fish");
+        _assertPoolSeeded(ironToken, ironPool, resSeed, goldSeed, "iron");
+        assertEq(goldToken.balanceOf(address(this)), 0, "treasury gold fully seeded");
+    }
+
+    function test_kInvariantHoldsAfterEachSwap() public {
+        uint256 kBefore = _k(woodPool);
+
+        vm.prank(address(world));
+        woodPool.swapExactInForOut(10e18, 1);
+        uint256 kAfterSell = _k(woodPool);
+        assertGe(kAfterSell, kBefore, "k after exact-in sell");
+
+        vm.prank(address(world));
+        woodPool.swapExactOutForInWithMaxIn(3e18, 10e18);
+        uint256 kAfterBuy = _k(woodPool);
+        assertGe(kAfterBuy, kAfterSell, "k after exact-out buy");
+    }
+
+    function test_pricePreviewMatchesActualSwap() public {
+        uint256 amountIn = 25e18;
+        uint256 preview = world.getPrice(uint8(ResourceType.Wood), amountIn);
+
+        vm.prank(address(world));
+        uint256 actual = woodPool.swapExactInForOut(amountIn, 0);
+
+        assertEq(actual, preview, "preview should match exact-in swap");
+    }
+
+    function _seedTreasuryAndApprove() internal {
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        uint256 totalGoldSeed = goldSeed * 4;
+
+        woodToken.seedTreasury(address(this), resSeed);
+        wheatToken.seedTreasury(address(this), resSeed);
+        fishToken.seedTreasury(address(this), resSeed);
+        ironToken.seedTreasury(address(this), resSeed);
+        goldToken.seedTreasury(address(this), totalGoldSeed);
+
+        woodToken.approve(address(world), resSeed);
+        wheatToken.approve(address(world), resSeed);
+        fishToken.approve(address(world), resSeed);
+        ironToken.approve(address(world), resSeed);
+        goldToken.approve(address(world), totalGoldSeed);
+    }
+
+    function _seedConfig() internal view returns (PoolSeedConfig memory) {
+        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
+        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
+        return PoolSeedConfig({
+            woodSeed: resSeed,
+            wheatSeed: resSeed,
+            fishSeed: resSeed,
+            ironSeed: resSeed,
+            goldSeedForWood: goldSeed,
+            goldSeedForWheat: goldSeed,
+            goldSeedForFish: goldSeed,
+            goldSeedForIron: goldSeed
+        });
+    }
+
+    function _assertPoolSeeded(
+        MinimalERC20 resourceToken,
+        StubPool pool,
+        uint256 expectedResource,
+        uint256 expectedGold,
+        string memory label
+    ) internal view {
+        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
+
+        assertEq(resourceReserve, expectedResource, string.concat(label, " resource reserve"));
+        assertEq(goldReserve, expectedGold, string.concat(label, " gold reserve"));
+        assertEq(resourceToken.balanceOf(address(pool)), expectedResource, string.concat(label, " resource balance"));
+        assertEq(goldToken.balanceOf(address(pool)), expectedGold, string.concat(label, " gold balance"));
+    }
+
+    function _k(StubPool pool) internal view returns (uint256) {
+        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
+        return resourceReserve * goldReserve;
+    }
+}
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
I’ll treat this as a final review rather than an implementation pass. I’m going to inspect the actual repo around the changed contracts/tests and use subagents for independent market/security and test/spec sweeps, then reconcile findings against the supplied diff.
exec
/bin/bash -lc "pwd && rg --files packages/contracts/src packages/contracts/test packages/contracts/script docs | sed -n '1,160p'" in /home/claude/code/clan-world
 succeeded in 0ms:
/home/claude/code/omnipass-world/clan-world
packages/contracts/src/ClanWorld.sol
packages/contracts/src/util/ReentrancyGuard.sol
packages/contracts/src/StubPool.sol
packages/contracts/src/IClanWorld.sol
packages/contracts/src/MinimalERC20.sol
packages/contracts/src/lib/RNG.sol
packages/contracts/src/ClanWorldStub.sol
packages/contracts/test/RNG.t.sol
packages/contracts/test/ClanWorldStub.t.sol
packages/contracts/test/MissionTiming.t.sol
packages/contracts/test/BanditAttackResolution.t.sol
packages/contracts/test/HeartbeatOrdering.t.sol
packages/contracts/test/Reentrancy.t.sol
packages/contracts/test/Bandit.t.sol
packages/contracts/test/ClanWorld.t.sol
packages/contracts/test/BanditSpawn.t.sol
packages/contracts/test/DefendBase.t.sol
packages/contracts/script/Heartbeat.s.sol
packages/contracts/script/Deploy.s.sol
docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md
docs/reviews/pr194-r2-codereview-codex-5-5.md
docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md
docs/reviews/r2/pr194-codereview-codex-5-5.md
docs/reviews/r2/pr194-codereview-opus-4-6.md
docs/reviews/r2/pr194-codereview-codex-5-4.md
docs/reviews/r2/pr194-synthesis.md
docs/reviews/r2/pr194-codereview-opus-4-7.md
docs/reviews/r2/pr194-codereview-gemini-3-1-pro.md
docs/reviews/pr198-codereview-codex-5-5.md
docs/reviews/pr198-codereview-opus-4-7.md
docs/reviews/pr137-review-claude-opus.md
docs/reviews/pr194-r2-codereview-codex-5-4.md
docs/reviews/pr153-review-codex-5-3.md
docs/reviews/pr194-r2-synthesis.md
docs/reviews/pr153-review-claude-opus.md
docs/reviews/pr194-r2-codereview-opus-4-7.md
docs/reviews/pr136-review-claude-opus.md
docs/reviews/pr133-review-claude-opus.md
docs/reviews/pr132-review-claude-opus.md
docs/reviews/pr198-codereview-codex-5-4.md
docs/reference/prize-strategy.md
docs/reference/architecture-decisions.md
docs/reference/sponsor-tech.md
docs/conventions/adapter-interfaces.md
docs/conventions/pr-review.md
docs/conventions/gitflow.md
docs/conventions/hackathon-rules.md
docs/planning/phase-3-test-spec.md
docs/planning/clanworld 0g battleplan and spec.pdf
docs/planning/clanworld_v4_3_schema_patch.md
docs/planning/battleplan-v1.txt
docs/guides/stream-agents.md
docs/guides/stream-contracts.md
docs/guides/stream-ops.md
docs/guides/stream-frontend.md
docs/guides/stream-backend.md
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx
docs/planning/DEMO_DRIFT.md
docs/planning/clanworld v4 5 keeper integration spec.pdf
docs/planning/clanworld_v4_2_state_schema_interface_spec.md
docs/planning/clanworld_numbered_implementation_plan.md
docs/planning/clanworld axl integration spec.pdf
docs/planning/clanworld_v4_5_alignment_addendum.md
docs/planning/clanworld_v4_spec.md
docs/planning/V1.zip
docs/planning/clanworld_v4_4_ui_indexer_getters.md
docs/planning/clanworld_v4_1_addendum.md
docs/planning/CANONICAL_SPEC.md
docs/planning/clanworld_v1_implementation_profile.md
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md
docs/planning/V1/04 AXL integration spec.pdf
docs/planning/gold-bridge-monorepo.zip
docs/planning/V1/03 KeeperHub integration spec.pdf
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md
docs/planning/V1/02 Frontend Spec/Makefile
docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md
docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md
docs/planning/V1/05 0G/clanworld_clan_memory_spec.md
docs/planning/V1/05 0G/clanworld_clan_identity_spec.md
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md

codex
The workspace path is a symlink-ish variant but the repo content matches. I’ve got the file list now; next I’m reading the core state-machine and spec passages in parallel so the review can anchor on concrete lines, not vibes.
2026-04-29T18:00:04.619955Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "nl -ba packages/contracts/src/IClanWorld.sol | sed -n '1,760p'" in /home/claude/code/clan-world
exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1120,1905p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/**
     5	 * @title IClanWorld
     6	 * @notice Canonical seam interface for the ClanWorld v4 hackathon build.
     7	 *
     8	 * Synthesized from:
     9	 *   - clanworld_v4_spec.md (mechanics)
    10	 *   - clanworld_v4_1_addendum.md (locked clarifications)
    11	 *   - clanworld_v4_2_state_schema_interface_spec.md (state schema)
    12	 *   - clanworld_v4_3_schema_patch.md (final lockdowns)
    13	 *   - clanworld_v4_4_ui_indexer_getters.md (UI convenience aggregators)
    14	 *
    15	 * This is the contract surface every other workstream depends on.
    16	 * Treat field names, event names, struct layouts, and enum order as STABLE.
    17	 *
    18	 * Implementation lives behind this interface. Mocks and indexer types are
    19	 * derived from it. Do not change without coordinating with all streams.
    20	 */
    21	
    22	// =============================================================================
    23	// CONSTANTS
    24	// =============================================================================
    25	
    26	library ClanWorldConstants {
    27	    // World cadence
    28	    uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
    29	    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
    30	    uint64 internal constant WINTER_DURATION_TICKS = 10;
    31	    uint64 internal constant SEASON_DURATION_TICKS = 360;
    32	
    33	    // Bandit cadence
    34	    uint64 internal constant BANDIT_COOLDOWN_TICKS = 10;
    35	    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    36	    uint64 internal constant BANDIT_REST_TICKS = 2;
    37	    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;
    38	
    39	    // Clansman cadence
    40	    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
    41	
    42	    // Carry caps (per clansman)
    43	    uint256 internal constant WOOD_CAP = 15e18;
    44	    uint256 internal constant IRON_CAP = 5e18;
    45	    uint256 internal constant WHEAT_CAP = 40e18;
    46	    uint256 internal constant FISH_CAP = 8e18;
    47	
    48	    // Gathering yields
    49	    uint256 internal constant WOOD_BASE_YIELD = 2e18;
    50	    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
    51	    uint16 internal constant WOOD_CRIT_BPS = 2000; // 20%
    52	
    53	    uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
    54	    uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
    55	    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;
    56	
    57	    uint16 internal constant FISH_DOCKS_BPS = 2500; // 25%
    58	    uint16 internal constant FISH_DEEP_BPS = 7500; // 75%
    59	
    60	    // Upkeep
    61	    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    62	    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    63	    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    64	    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
    65	
    66	    // Wheat plots
    67	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    68	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
    69	
    70	    // Bandit combat
    71	    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    72	    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%
    73	
    74	    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    75	    uint8 internal constant REGION_NOOP = 0;
    76	    uint8 internal constant REGION_FOREST = 1;
    77	    uint8 internal constant REGION_MOUNTAINS = 2;
    78	    uint8 internal constant REGION_UNICORN_TOWN = 3;
    79	    uint8 internal constant REGION_WEST_FARMS = 4;
    80	    uint8 internal constant REGION_EAST_FARMS = 5;
    81	    uint8 internal constant REGION_WEST_DOCKS = 6;
    82	    uint8 internal constant REGION_EAST_DOCKS = 7;
    83	    uint8 internal constant REGION_DEEP_SEA = 8;
    84	
    85	    // Sentinels
    86	    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    87	    uint32 internal constant BANDIT_ID_NULL = 0;
    88	}
    89	
    90	// =============================================================================
    91	// ENUMS
    92	// =============================================================================
    93	
    94	enum ClanState {
    95	    ACTIVE,
    96	    DEAD
    97	}
    98	
    99	enum ClansmanState {
   100	    WAITING,
   101	    TRAVELING,
   102	    ACTING,
   103	    DEAD
   104	}
   105	
   106	// v1 ABI: Bandit state machine redesigned in Phase 9. ABI consumers must regenerate.
   107	enum BanditState {
   108	    None,
   109	    Spawned,
   110	    Camped,
   111	    Resting,
   112	    Attacking,
   113	    Defeated,
   114	    Escaped
   115	}
   116	
   117	enum WheatPlotState {
   118	    Harvestable,
   119	    Regrowing,
   120	    WinterLocked
   121	}
   122	
   123	enum ResourceType {
   124	    Wood,
   125	    Iron,
   126	    Wheat,
   127	    Fish
   128	}
   129	
   130	enum ActionType {
   131	    None,
   132	    ChopWood,
   133	    MineIron,
   134	    FishDocks,
   135	    FishDeepSea,
   136	    HarvestWheat,
   137	    DepositResources,
   138	    BuildWall,
   139	    UpgradeBase,
   140	    UpgradeMonument,
   141	    DefendBase,
   142	    MarketBuy,
   143	    MarketSell,
   144	    Wait
   145	}
   146	
   147	enum MarketExecutionMode {
   148	    None,
   149	    Immediate,
   150	    Scheduled
   151	}
   152	
   153	enum StatusCode {
   154	    OK,
   155	    ERR_CLAN_DEAD,
   156	    ERR_CLAN_NOT_OWNED,
   157	    ERR_CLANSMAN_DEAD,
   158	    ERR_INVALID_CLANSMAN,
   159	    ERR_INVALID_REGION,
   160	    ERR_INVALID_ACTION,
   161	    ERR_INVALID_TARGET,
   162	    ERR_COOLDOWN_ACTIVE,
   163	    ERR_NOT_WAITING,
   164	    ERR_NOT_IN_UNICORN_TOWN,
   165	    ERR_NOT_AT_HOMEBASE,
   166	    ERR_NOT_AT_TARGET_BASE,
   167	    ERR_NOT_DEFENDABLE,
   168	    ERR_MISSING_RESOURCES,
   169	    ERR_EMPTY_CARGO,
   170	    ERR_PLOT_NOT_READY,
   171	    ERR_PLOT_EMPTY,
   172	    ERR_MARKET_ZERO_AMOUNT,
   173	    ERR_MARKET_UNSUPPORTED_TOKEN,
   174	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   175	    ERR_MARKET_BUY_OVER_CAPACITY,
   176	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   177	    ERR_WORLD_TICK_MISMATCH,
   178	    ERR_NO_ACTIVE_BANDIT,
   179	    ERR_SEASON_ENDED,
   180	    ERR_NOT_ENOUGH_GOLD,
   181	    ERR_CARRY_FULL
   182	}
   183	
   184	// =============================================================================
   185	// CORE STATE STRUCTS (raw storage shape)
   186	// =============================================================================
   187	
   188	struct WorldState {
   189	    uint64 currentTick;
   190	    uint64 seasonStartTick;
   191	    uint64 seasonEndTick;
   192	    bool seasonFinalized;
   193	    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
   194	    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
   195	
   196	    uint64 nextHeartbeatAtTs;
   197	    uint64 nextBanditSpawnEligibleTick;
   198	    uint16 currentBanditSpawnChanceBps;
   199	    bytes32 currentTickSeed;
   200	
   201	    uint32 activeBanditId; // 0 if none
   202	    bool winterActive;
   203	    uint64 winterStartsAtTick;
   204	    uint64 winterEndsAtTick; // 0 if not active
   205	
   206	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
   207	}
   208	
   209	struct TreasuryState {
   210	    address treasuryOwner;
   211	    uint256 prizePotGold;
   212	
   213	    bool poolsSeeded;
   214	
   215	    address woodToken;
   216	    address wheatToken;
   217	    address fishToken;
   218	    address ironToken;
   219	    address goldToken;
   220	    address blueprintToken;
   221	
   222	    address woodGoldPool;
   223	    address wheatGoldPool;
   224	    address fishGoldPool;
   225	    address ironGoldPool;
   226	}
   227	
   228	struct Clan {
   229	    uint32 clanId;
   230	    uint256 iftTokenId;
   231	    address owner;
   232	    ClanState clanState;
   233	
   234	    uint8 baseRegion;
   235	    uint8 baseLevel;
   236	    uint8 wallLevel;
   237	    uint8 monumentLevel;
   238	    uint8 livingClansmen;
   239	
   240	    uint64 lastSettledTick;
   241	    uint64 starvationStartsAtTick; // 0 = none
   242	
   243	    uint16 coldDamage; // resets to 0 at winter end
   244	
   245	    uint256 goldBalance;
   246	    uint256 blueprintBalance;
   247	
   248	    uint256 vaultWood;
   249	    uint256 vaultIron;
   250	    uint256 vaultWheat;
   251	    uint256 vaultFish;
   252	}
   253	
   254	struct WheatPlot {
   255	    WheatPlotState state;
   256	    uint8 region; // West Farms or East Farms
   257	    uint256 remainingWheat;
   258	    uint64 regrowUntilTick;
   259	}
   260	
   261	struct Clansman {
   262	    uint32 clansmanId;
   263	    uint32 clanId;
   264	    ClansmanState state;
   265	    uint8 currentRegion;
   266	
   267	    uint64 cooldownEndsAtTs;
   268	    uint64 lastMissionNonce;
   269	
   270	    uint256 carryWood;
   271	    uint256 carryIron;
   272	    uint256 carryWheat;
   273	    uint256 carryFish;
   274	}
   275	
   276	struct Mission {
   277	    bool active;
   278	
   279	    uint64 nonce;
   280	    uint64 submittedAtTick;
   281	    uint64 executesAtTick;
   282	    uint64 settlesAtTick;
   283	    uint32 clansmanId;
   284	
   285	    uint8 startRegion;
   286	    uint8 targetRegion;
   287	    ActionType action;
   288	
   289	    uint64 startTick;
   290	    uint64 arrivalTick;
   291	    uint64 actionStartTick;
   292	
   293	    bytes32 missionSeed;
   294	    MarketExecutionMode marketMode;
   295	
   296	    uint32 targetClanId; // DefendBase only
   297	    address marketToken; // market token for buy/sell
   298	    uint256 marketAmount; // exact-in for sell, exact-out for buy
   299	    uint256 maxGoldIn; // market_buy only, 0 otherwise
   300	}
   301	
   302	// v1 ABI: Bandit troop layout redesigned in Phase 9. ABI consumers must regenerate.
   303	struct BanditTroop {
   304	    uint32 id;
   305	    uint8 region;
   306	    BanditState state;
   307	    uint32 targetClanId; // 0 if not attacking
   308	    uint64 tickEnteredState;
   309	    uint32 strength; // hp / combat power
   310	    uint256 carryWood;
   311	    uint256 carryIron;
   312	    uint256 carryWheat;
   313	    uint256 carryFish;
   314	    uint256 carryGold;
   315	}
   316	
   317	struct ScheduledMarketAction {
   318	    uint64 executeAtTick;
   319	    uint64 commitSequence; // global monotonic FIFO order
   320	    uint64 missionNonce; // mission nonce captured when the action was queued
   321	    uint32 clanId;
   322	    uint32 clansmanId;
   323	    ActionType action; // MarketBuy or MarketSell
   324	
   325	    address marketToken;
   326	    uint256 marketAmount; // exact-in for sell, exact-out for buy
   327	    uint256 maxGoldIn; // buy only, 0 otherwise
   328	}
   329	
   330	struct DefenseContribution {
   331	    uint32 clansmanId;
   332	    uint32 clanId;
   333	    uint16 defensePoints;
   334	}
   335	
   336	struct PackedRoute {
   337	    uint8 travelTicks;
   338	    bytes8 path; // ordered region ids, e.g. [6,4,3,2,0,0,0,0]
   339	}
   340	
   341	// =============================================================================
   342	// DERIVED VIEW STRUCTS (read-only, settled forward to current tick)
   343	// =============================================================================
   344	
   345	struct DerivedClanState {
   346	    Clan clan; // settled to current tick
   347	    bool isStarving;
   348	    uint256 lootValue; // current weighted loot value
   349	    uint64 derivedAtTick;
   350	}
   351	
   352	struct DerivedClansmanState {
   353	    Clansman clansman; // settled to current tick
   354	    Mission activeMission; // active=false if none
   355	    uint8 effectiveRegion; // for traveling, derived from route + elapsed ticks
   356	    uint64 derivedAtTick;
   357	}
   358	
   359	// =============================================================================
   360	// WRITE INPUT / OUTPUT STRUCTS
   361	// =============================================================================
   362	
   363	struct ClanOrder {
   364	    uint32 clansmanId;
   365	    uint8 gotoRegion;
   366	    ActionType action;
   367	
   368	    uint32 targetClanId; // DefendBase only
   369	    address marketToken;
   370	    uint256 marketAmount;
   371	    uint256 maxGoldIn;
   372	}
   373	
   374	struct OrderResult {
   375	    uint32 clansmanId;
   376	    StatusCode status;
   377	    uint64 cooldownEndsAtTs;
   378	    uint64 missionNonce;
   379	}
   380	
   381	struct PoolSeedConfig {
   382	    uint256 woodSeed;
   383	    uint256 wheatSeed;
   384	    uint256 fishSeed;
   385	    uint256 ironSeed;
   386	    uint256 goldSeedForWood;
   387	    uint256 goldSeedForWheat;
   388	    uint256 goldSeedForFish;
   389	    uint256 goldSeedForIron;
   390	}
   391	
   392	// =============================================================================
   393	// UI INDEXER AGGREGATOR STRUCTS (v4.4 additions)
   394	// =============================================================================
   395	
   396	struct LeaderboardEntry {
   397	    uint32 clanId;
   398	    address owner;
   399	    uint8 monumentLevel;
   400	    uint8 baseLevel;
   401	    uint8 wallLevel;
   402	    uint8 livingClansmen;
   403	    ClanState state;
   404	    uint256 lootValue; // settled
   405	}
   406	
   407	struct WorldSnapshot {
   408	    uint64 currentTick;
   409	    uint64 seasonStartTick;
   410	    uint64 seasonEndTick;
   411	    bool seasonFinalized;
   412	    uint64 currentSeasonNumber;
   413	    uint64 nextHeartbeatAtTick;
   414	    bool winterActive;
   415	    uint64 winterStartsAtTick;
   416	    uint64 winterEndsAtTick;
   417	    uint32 activeBanditId;
   418	    bytes32 currentTickSeed;
   419	
   420	    LeaderboardEntry[] leaderboard;
   421	}
   422	
   423	struct ClansmanFullView {
   424	    DerivedClansmanState clansman;
   425	    Mission activeMission;
   426	}
   427	
   428	struct ClanFullView {
   429	    DerivedClanState clan;
   430	    ClansmanFullView[] clansmen;
   431	    WheatPlot westPlot;
   432	    WheatPlot eastPlot;
   433	    uint32[] incomingDefenderIds; // legacy UI field; clanIds defending this clan's home region
   434	    uint32 thisClanDefendingBaseId; // defended home region, or 0 if none
   435	}
   436	
   437	struct PoolReserves {
   438	    address resourceToken;
   439	    uint256 resourceReserve;
   440	    uint256 goldReserve;
   441	    uint256 spotPriceGoldPerResource; // = goldReserve * 1e18 / resourceReserve, or 0
   442	}
   443	
   444	struct MarketState {
   445	    PoolReserves wood;
   446	    PoolReserves wheat;
   447	    PoolReserves fish;
   448	    PoolReserves iron;
   449	
   450	    uint64 currentTick;
   451	    ScheduledMarketAction[] currentTickQueue;
   452	    ScheduledMarketAction[] nextTickQueue;
   453	}
   454	
   455	struct ActiveBanditView {
   456	    bool exists;
   457	    uint32 banditId;
   458	    BanditState state;
   459	    uint8 currentRegion;
   460	    uint8 attackAttemptsMade;
   461	    uint8 maxAttemptsRemaining;
   462	    uint64 stateEnteredTick;
   463	    uint64 nextActionTick;
   464	    uint8 tier;
   465	    uint16 attackPower;
   466	
   467	    uint256 carryWood;
   468	    uint256 carryIron;
   469	    uint256 carryWheat;
   470	    uint256 carryFish;
   471	
   472	    uint32 projectedTargetClanId; // 0 if no eligible target in current region
   473	    uint256 projectedTargetLootValue;
   474	}
   475	
   476	struct RegionOccupant {
   477	    uint32 clansmanId;
   478	    uint32 clanId;
   479	    ClansmanState state;
   480	    ActionType currentAction;
   481	    uint64 missionNonce;
   482	}
   483	
   484	// =============================================================================
   485	// EVENTS
   486	// =============================================================================
   487	
   488	interface IClanWorldEvents {
   489	    // ----- world clock -----
   490	    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
   491	    event WinterStarted(uint64 indexed tick);
   492	    event WinterEnded(uint64 indexed tick);
   493	    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
   494	
   495	    // ----- clan lifecycle -----
   496	    event ClanSpawned(
   497	        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
   498	    );
   499	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   500	    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
   501	    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
   502	
   503	    // ----- missions -----
   504	    event MissionAssigned(
   505	        uint32 indexed clanId,
   506	        uint32 indexed clansmanId,
   507	        uint64 missionNonce,
   508	        ActionType action,
   509	        uint8 startRegion,
   510	        uint8 targetRegion,
   511	        uint64 startTick,
   512	        uint64 arrivalTick
   513	    );
   514	    event MissionInterrupted(
   515	        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
   516	    );
   517	    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   518	    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   519	
   520	    // ----- gathering / vault movement -----
   521	    event ResourcesGathered(
   522	        uint32 indexed clanId,
   523	        uint32 indexed clansmanId,
   524	        ActionType action,
   525	        uint256 woodGained,
   526	        uint256 ironGained,
   527	        uint256 wheatGained,
   528	        uint256 fishGained,
   529	        uint256 goldBonus,
   530	        uint64 atTick
   531	    );
   532	    event ResourcesDeposited(
   533	        uint32 indexed clanId,
   534	        uint32 indexed clansmanId,
   535	        uint256 wood,
   536	        uint256 iron,
   537	        uint256 wheat,
   538	        uint256 fish,
   539	        uint64 atTick
   540	    );
   541	
   542	    // ----- building -----
   543	    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   544	    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   545	    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   546	
   547	    // ----- market -----
   548	    event ImmediateMarketActionExecuted(
   549	        uint32 indexed clanId,
   550	        uint32 indexed clansmanId,
   551	        address tokenIn,
   552	        address tokenOut,
   553	        uint256 amountIn,
   554	        uint256 amountOut,
   555	        uint64 atTick
   556	    );
   557	    event ScheduledMarketActionExecuted(
   558	        uint64 indexed executeAtTick,
   559	        uint64 indexed commitSequence,
   560	        uint32 indexed clanId,
   561	        uint32 clansmanId,
   562	        address tokenIn,
   563	        address tokenOut,
   564	        uint256 amountIn,
   565	        uint256 amountOut
   566	    );
   567	    event ScheduledMarketActionCommitted(
   568	        uint64 indexed executeAtTick,
   569	        uint64 indexed commitSequence,
   570	        uint32 indexed clanId,
   571	        uint32 clansmanId,
   572	        ActionType action,
   573	        address marketToken,
   574	        uint256 marketAmount,
   575	        uint256 maxGoldIn
   576	    );
   577	    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
   578	
   579	    // ----- bandits -----
   580	    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
   581	    event BanditStateChanged(
   582	        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
   583	    );
   584	    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
   585	    event BanditAttackResolved(
   586	        uint32 indexed banditId,
   587	        uint32 indexed targetClanId,
   588	        bool defended,
   589	        uint16 attackPower,
   590	        uint16 totalDefense,
   591	        uint16 wallLevelAfter,
   592	        uint256 stolenWood,
   593	        uint256 stolenIron,
   594	        uint256 stolenWheat,
   595	        uint256 stolenFish,
   596	        uint64 atTick
   597	    );
   598	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
   599	    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
   600	    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
   601	    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
   602	    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
   603	    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
   604	    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
   605	    event LootDistributed(
   606	        uint32 indexed banditId,
   607	        uint32[] clanIdsRewarded,
   608	        uint256 perClanWood,
   609	        uint256 perClanWheat,
   610	        uint256 perClanFish,
   611	        uint256 perClanIron,
   612	        uint256 perClanGold,
   613	        uint256 burnedWood,
   614	        uint256 burnedWheat,
   615	        uint256 burnedFish,
   616	        uint256 burnedIron,
   617	        uint256 burnedGold
   618	    );
   619	    event LootDistributedToDefender(
   620	        uint32 indexed banditId,
   621	        uint32 indexed clanId,
   622	        uint32 indexed clansmanId,
   623	        uint256 wood,
   624	        uint256 iron,
   625	        uint256 wheat,
   626	        uint256 fish
   627	    );
   628	
   629	    // ----- winter cold damage -----
   630	    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
   631	    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
   632	
   633	    // ----- OTC transfers -----
   634	    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   635	    event VaultResourceTransferred(
   636	        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
   637	    );
   638	    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   639	
   640	    // ----- treasury / pools -----
   641	    event PoolsSeeded(address woodGoldPool, address wheatGoldPool, address fishGoldPool, address ironGoldPool);
   642	}
   643	
   644	// =============================================================================
   645	// MAIN INTERFACE
   646	// =============================================================================
   647	
   648	interface IClanWorld is IClanWorldEvents {
   649	    // -------------------------------------------------------------------------
   650	    // World progression
   651	    // -------------------------------------------------------------------------
   652	
   653	    /// @notice Permissionless heartbeat. Closes the current tick, resolves
   654	    ///         scheduled market actions and world events, advances the tick.
   655	    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
   656	    function heartbeat() external;
   657	
   658	    /// @notice Lazily settle a clan forward to current tick. Idempotent.
   659	    function settleClan(uint32 clanId) external;
   660	
   661	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   662	    function settleClansman(uint32 csId) external;
   663	
   664	    /// @notice Finalize the current season. Permissionless after seasonEndTick.
   665	    function finalizeSeason() external;
   666	
   667	    // -------------------------------------------------------------------------
   668	    // Clan lifecycle
   669	    // -------------------------------------------------------------------------
   670	
   671	    /// @notice Mint a new clan iNFT and spawn its homebase in a valid region.
   672	    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
   673	
   674	    /// @notice Submit one or more orders for a single clan's clansmen.
   675	    ///         Per-order failures do not revert the tx.
   676	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
   677	
   678	    // -------------------------------------------------------------------------
   679	    // Treasury / pool seeding
   680	    // -------------------------------------------------------------------------
   681	
   682	    /// @notice Owner-only. Registers token and pool addresses once before seeding.
   683	    ///         tokens order: wood, iron, wheat, fish, gold, blueprint.
   684	    ///         pools order: wood, wheat, fish, iron.
   685	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external;
   686	
   687	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
   688	    function seedPools(PoolSeedConfig calldata cfg) external;
   689	
   690	    // -------------------------------------------------------------------------
   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
   692	    // -------------------------------------------------------------------------
   693	
   694	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   695	
   696	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
   697	
   698	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   699	
   700	    function transferBundle(
   701	        uint32 fromClanId,
   702	        uint32 toClanId,
   703	        uint256 gold,
   704	        uint256 blueprint,
   705	        uint256 wood,
   706	        uint256 iron,
   707	        uint256 wheat,
   708	        uint256 fish
   709	    ) external;
   710	
   711	    // -------------------------------------------------------------------------
   712	    // Raw read getters (committed storage, no settlement simulation)
   713	    // -------------------------------------------------------------------------
   714	
   715	    function getWorldState() external view returns (WorldState memory);
   716	
   717	    function getTreasuryState() external view returns (TreasuryState memory);
   718	
   719	    function getClan(uint32 clanId) external view returns (Clan memory);
   720	
   721	    function getClansman(uint32 clansmanId) external view returns (Clansman memory);
   722	
   723	    function getActiveMission(uint32 clansmanId) external view returns (Mission memory);
   724	
   725	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
   726	        external
   727	        view
   728	        returns (uint64 submitted, uint64 executes, uint64 settles);
   729	
   730	    function getActionDuration(ActionType action) external pure returns (uint64);
   731	
   732	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
   733	
   734	    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
   735	
   736	    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
   737	
   738	    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
   739	
   740	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
   741	
   742	    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
   743	
   744	    function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds);
   745	
   746	    function getDefendingClans(uint8 region) external view returns (uint32[] memory clanIds);
   747	
   748	    // -------------------------------------------------------------------------
   749	    // Derived read getters (read-only simulation forward to current tick)
   750	    //
   751	    // Per v4.3 §J ("Derived Getter Non-Mutation Rule"), these MUST NOT mutate
   752	    // any storage, including settlement checkpoints, cached flags, or queues.
   753	    // -------------------------------------------------------------------------
   754	
   755	    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory);
   756	
   757	    function getDerivedClansmanState(uint32 clansmanId) external view returns (DerivedClansmanState memory);
   758	
   759	    /// @notice Non-binding preview. Bandit targeting is recomputed at attack
   760	    ///         resolution time using then-current eagerly settled state.

 succeeded in 1ms:
  1120	            return;
  1121	        }
  1122	    }
  1123	
  1124	    function _simulateMarkClansmanDead(SettlementSimulation memory sim, uint256 index) internal pure {
  1125	        if (sim.clansmen[index].state == ClansmanState.DEAD) return;
  1126	
  1127	        sim.clansmen[index].state = ClansmanState.DEAD;
  1128	        sim.clansmen[index].cooldownEndsAtTs = 0;
  1129	        if (sim.clan.livingClansmen > 0) {
  1130	            sim.clan.livingClansmen--;
  1131	        }
  1132	        if (sim.missions[index].active) {
  1133	            sim.missions[index].active = false;
  1134	        }
  1135	    }
  1136	
  1137	    function _simulateMarkClanDead(SettlementSimulation memory sim) internal pure {
  1138	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
  1139	
  1140	        sim.clan.clanState = ClanState.DEAD;
  1141	        sim.clan.vaultWood = 0;
  1142	        sim.clan.vaultWheat = 0;
  1143	        sim.clan.vaultFish = 0;
  1144	        sim.clan.vaultIron = 0;
  1145	        sim.clan.starvationStartsAtTick = 0;
  1146	        sim.clan.livingClansmen = 0;
  1147	
  1148	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  1149	            sim.clansmen[i].state = ClansmanState.DEAD;
  1150	            sim.clansmen[i].cooldownEndsAtTs = 0;
  1151	            if (sim.missions[i].active) {
  1152	                sim.missions[i].active = false;
  1153	            }
  1154	        }
  1155	    }
  1156	
  1157	    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
  1158	        for (uint256 pi = 0; pi < 2; pi++) {
  1159	            WheatPlot memory plot = sim.wheatPlots[pi];
  1160	            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
  1161	                plot.state = WheatPlotState.Harvestable;
  1162	                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
  1163	                plot.regrowUntilTick = 0;
  1164	                sim.wheatPlots[pi] = plot;
  1165	            }
  1166	        }
  1167	    }
  1168	
  1169	    function _simulateSettleMissionForClansman(
  1170	        SettlementSimulation memory sim,
  1171	        uint256 index,
  1172	        uint64 fromTick,
  1173	        uint64 toTick
  1174	    ) internal view {
  1175	        Clansman memory cs = sim.clansmen[index];
  1176	        Mission memory m = sim.missions[index];
  1177	
  1178	        if (cs.state == ClansmanState.DEAD) {
  1179	            if (m.active) {
  1180	                m.active = false;
  1181	            }
  1182	            sim.missions[index] = m;
  1183	            return;
  1184	        }
  1185	
  1186	        if (!m.active) return;
  1187	
  1188	        for (uint64 tick = fromTick; tick < toTick; tick++) {
  1189	            bytes32 tickSeed = _tickSeeds[tick];
  1190	
  1191	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
  1192	                cs.state = ClansmanState.ACTING;
  1193	                cs.currentRegion = m.targetRegion;
  1194	            }
  1195	
  1196	            if (m.action == ActionType.DefendBase) continue;
  1197	
  1198	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
  1199	                (cs, m) = _simulateResolveAction(sim, cs, m, tick, tickSeed);
  1200	                if (m.active && getActionDuration(m.action) > 0) {
  1201	                    (cs, m) = _simulateCompleteMission(cs, m);
  1202	                }
  1203	            }
  1204	
  1205	            if (!m.active) break;
  1206	        }
  1207	
  1208	        sim.clansmen[index] = cs;
  1209	        sim.missions[index] = m;
  1210	    }
  1211	
  1212	    function _simulateResolveAction(
  1213	        SettlementSimulation memory sim,
  1214	        Clansman memory cs,
  1215	        Mission memory m,
  1216	        uint64 tick,
  1217	        bytes32 tickSeed
  1218	    ) internal view returns (Clansman memory, Mission memory) {
  1219	        bool starving =
  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
  1221	        ActionType action = m.action;
  1222	
  1223	        if (action == ActionType.ChopWood) {
  1224	            (cs, m) = _simulateGatherWood(cs, m, tick, starving, tickSeed);
  1225	        } else if (action == ActionType.MineIron) {
  1226	            (cs, m) = _simulateGatherIron(sim, cs, m, tick, starving, tickSeed);
  1227	        } else if (action == ActionType.FishDocks) {
  1228	            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DOCKS_BPS);
  1229	        } else if (action == ActionType.FishDeepSea) {
  1230	            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DEEP_BPS);
  1231	        } else if (action == ActionType.HarvestWheat) {
  1232	            (cs, m) = _simulateGatherWheat(sim, cs, m, tick, starving);
  1233	        } else if (action == ActionType.DepositResources) {
  1234	            (cs, m) = _simulateDoDeposit(sim, cs, m);
  1235	        } else if (
  1236	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
  1237	        ) {
  1238	            (cs, m) = _simulateDoBuilding(sim, cs, m, action);
  1239	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  1240	            (cs, m) = _simulateCompleteMission(cs, m);
  1241	        }
  1242	
  1243	        return (cs, m);
  1244	    }
  1245	
  1246	    function _simulateGatherWood(Clansman memory cs, Mission memory m, uint64 tick, bool starving, bytes32 tickSeed)
  1247	        internal
  1248	        view
  1249	        returns (Clansman memory, Mission memory)
  1250	    {
  1251	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
  1252	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1253	
  1254	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
  1255	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
  1256	        if (uint256(critRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
  1257	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
  1258	        }
  1259	        if (starving) yield = yield / 2;
  1260	        if (yield > remaining) yield = remaining;
  1261	        cs.carryWood += yield;
  1262	
  1263	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
  1264	            return _simulateCompleteMission(cs, m);
  1265	        }
  1266	        return (cs, m);
  1267	    }
  1268	
  1269	    function _simulateGatherIron(
  1270	        SettlementSimulation memory sim,
  1271	        Clansman memory cs,
  1272	        Mission memory m,
  1273	        uint64 tick,
  1274	        bool starving,
  1275	        bytes32 tickSeed
  1276	    ) internal view returns (Clansman memory, Mission memory) {
  1277	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
  1278	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1279	
  1280	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
  1281	        if (starving) yield = yield / 2;
  1282	        if (yield > remaining) yield = remaining;
  1283	        cs.carryIron += yield;
  1284	
  1285	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, cs.clansmanId, m.nonce, tick));
  1286	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
  1287	            sim.clan.goldBalance += ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
  1288	        }
  1289	
  1290	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
  1291	            return _simulateCompleteMission(cs, m);
  1292	        }
  1293	        return (cs, m);
  1294	    }
  1295	
  1296	    function _simulateGatherFish(
  1297	        Clansman memory cs,
  1298	        Mission memory m,
  1299	        uint64 tick,
  1300	        bool starving,
  1301	        bytes32 tickSeed,
  1302	        uint256 successBps
  1303	    ) internal view returns (Clansman memory, Mission memory) {
  1304	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
  1305	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1306	
  1307	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
  1308	        uint256 yield = uint256(fishRng) % 10000 < successBps ? RESOURCE_UNIT : 0;
  1309	        if (starving) yield = yield / 2;
  1310	        if (yield > remaining) yield = remaining;
  1311	        if (yield > 0) {
  1312	            cs.carryFish += yield;
  1313	        }
  1314	
  1315	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
  1316	            return _simulateCompleteMission(cs, m);
  1317	        }
  1318	        return (cs, m);
  1319	    }
  1320	
  1321	    function _simulateGatherWheat(
  1322	        SettlementSimulation memory sim,
  1323	        Clansman memory cs,
  1324	        Mission memory m,
  1325	        uint64 tick,
  1326	        bool starving
  1327	    ) internal view returns (Clansman memory, Mission memory) {
  1328	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
  1329	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1330	
  1331	        uint256 plotIdx;
  1332	        if (m.targetRegion == ClanWorldConstants.REGION_WEST_FARMS) {
  1333	            plotIdx = 0;
  1334	        } else if (m.targetRegion == ClanWorldConstants.REGION_EAST_FARMS) {
  1335	            plotIdx = 1;
  1336	        } else {
  1337	            return _simulateCompleteMission(cs, m);
  1338	        }
  1339	
  1340	        WheatPlot memory plot = sim.wheatPlots[plotIdx];
  1341	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
  1342	            return _simulateCompleteMission(cs, m);
  1343	        }
  1344	
  1345	        uint256 yield = WHEAT_HARVEST_RATE;
  1346	        if (starving) yield = yield / 2;
  1347	        if (yield > remaining) yield = remaining;
  1348	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
  1349	
  1350	        cs.carryWheat += yield;
  1351	        plot.remainingWheat -= yield;
  1352	
  1353	        if (plot.remainingWheat == 0) {
  1354	            plot.state = WheatPlotState.Regrowing;
  1355	            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
  1356	        }
  1357	        sim.wheatPlots[plotIdx] = plot;
  1358	
  1359	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
  1360	            return _simulateCompleteMission(cs, m);
  1361	        }
  1362	        return (cs, m);
  1363	    }
  1364	
  1365	    function _simulateDoDeposit(SettlementSimulation memory sim, Clansman memory cs, Mission memory m)
  1366	        internal
  1367	        view
  1368	        returns (Clansman memory, Mission memory)
  1369	    {
  1370	        if (cs.currentRegion != sim.clan.baseRegion) return _simulateCompleteMission(cs, m);
  1371	
  1372	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
  1373	        if (!hasAnything) return _simulateCompleteMission(cs, m);
  1374	
  1375	        sim.clan.vaultWood += cs.carryWood;
  1376	        sim.clan.vaultIron += cs.carryIron;
  1377	        sim.clan.vaultWheat += cs.carryWheat;
  1378	        sim.clan.vaultFish += cs.carryFish;
  1379	
  1380	        cs.carryWood = 0;
  1381	        cs.carryIron = 0;
  1382	        cs.carryWheat = 0;
  1383	        cs.carryFish = 0;
  1384	
  1385	        return _simulateCompleteMission(cs, m);
  1386	    }
  1387	
  1388	    function _simulateDoBuilding(
  1389	        SettlementSimulation memory sim,
  1390	        Clansman memory cs,
  1391	        Mission memory m,
  1392	        ActionType action
  1393	    ) internal view returns (Clansman memory, Mission memory) {
  1394	        if (cs.currentRegion == sim.clan.baseRegion) {
  1395	            if (action == ActionType.BuildWall) {
  1396	                _simulateTryBuildWall(sim);
  1397	            } else if (action == ActionType.UpgradeBase) {
  1398	                _simulateTryUpgradeBase(sim);
  1399	            } else if (action == ActionType.UpgradeMonument) {
  1400	                _simulateTryUpgradeMonument(sim);
  1401	            }
  1402	        }
  1403	        return _simulateCompleteMission(cs, m);
  1404	    }
  1405	
  1406	    function _simulateTryBuildWall(SettlementSimulation memory sim) internal pure {
  1407	        uint8 nextLevel = sim.clan.wallLevel + 1;
  1408	        if (nextLevel > 5) return;
  1409	
  1410	        uint256 woodCost;
  1411	        uint256 ironCost;
  1412	        if (nextLevel == 1) {
  1413	            woodCost = 20e18;
  1414	        } else if (nextLevel == 2) {
  1415	            woodCost = 35e18;
  1416	        } else if (nextLevel == 3) {
  1417	            woodCost = 30e18;
  1418	            ironCost = 5e18;
  1419	        } else if (nextLevel == 4) {
  1420	            woodCost = 40e18;
  1421	            ironCost = 10e18;
  1422	        } else {
  1423	            woodCost = 50e18;
  1424	            ironCost = 15e18;
  1425	        }
  1426	
  1427	        if (sim.clan.vaultWood < woodCost || sim.clan.vaultIron < ironCost) return;
  1428	        sim.clan.vaultWood -= woodCost;
  1429	        sim.clan.vaultIron -= ironCost;
  1430	        sim.clan.wallLevel = nextLevel;
  1431	    }
  1432	
  1433	    function _simulateTryUpgradeBase(SettlementSimulation memory sim) internal pure {
  1434	        uint8 nextLevel = sim.clan.baseLevel + 1;
  1435	        if (nextLevel > 5) return;
  1436	
  1437	        uint256 woodCost;
  1438	        uint256 ironCost;
  1439	        uint256 wheatCost;
  1440	        if (nextLevel == 2) {
  1441	            woodCost = 40e18;
  1442	            wheatCost = 20e18;
  1443	        } else if (nextLevel == 3) {
  1444	            woodCost = 60e18;
  1445	            ironCost = 5e18;
  1446	            wheatCost = 30e18;
  1447	        } else if (nextLevel == 4) {
  1448	            woodCost = 80e18;
  1449	            ironCost = 10e18;
  1450	            wheatCost = 40e18;
  1451	        } else {
  1452	            woodCost = 100e18;
  1453	            ironCost = 15e18;
  1454	            wheatCost = 50e18;
  1455	        }
  1456	
  1457	        if (sim.clan.vaultWood < woodCost || sim.clan.vaultIron < ironCost || sim.clan.vaultWheat < wheatCost) {
  1458	            return;
  1459	        }
  1460	        sim.clan.vaultWood -= woodCost;
  1461	        sim.clan.vaultIron -= ironCost;
  1462	        sim.clan.vaultWheat -= wheatCost;
  1463	        sim.clan.baseLevel = nextLevel;
  1464	    }
  1465	
  1466	    function _simulateTryUpgradeMonument(SettlementSimulation memory sim) internal pure {
  1467	        uint8 nextLevel = sim.clan.monumentLevel + 1;
  1468	        if (nextLevel > 10) return;
  1469	
  1470	        uint256 woodCost;
  1471	        uint256 wheatCost;
  1472	        uint256 ironCost;
  1473	        uint256 blueprintCost;
  1474	        if (nextLevel == 1) {
  1475	            woodCost = 30e18;
  1476	            wheatCost = 20e18;
  1477	        } else if (nextLevel == 2) {
  1478	            woodCost = 50e18;
  1479	            wheatCost = 30e18;
  1480	        } else if (nextLevel == 3) {
  1481	            woodCost = 70e18;
  1482	            wheatCost = 40e18;
  1483	            ironCost = 5e18;
  1484	        } else if (nextLevel == 4) {
  1485	            woodCost = 90e18;
  1486	            wheatCost = 50e18;
  1487	            ironCost = 10e18;
  1488	        } else if (nextLevel == 5) {
  1489	            woodCost = 120e18;
  1490	            wheatCost = 60e18;
  1491	            ironCost = 15e18;
  1492	        } else if (nextLevel == 6) {
  1493	            woodCost = 150e18;
  1494	            wheatCost = 80e18;
  1495	            ironCost = 20e18;
  1496	        } else {
  1497	            woodCost = 200e18;
  1498	            wheatCost = 100e18;
  1499	            ironCost = 25e18;
  1500	            blueprintCost = 1e18;
  1501	        }
  1502	
  1503	        if (
  1504	            sim.clan.vaultWood < woodCost || sim.clan.vaultWheat < wheatCost || sim.clan.vaultIron < ironCost
  1505	                || sim.clan.blueprintBalance < blueprintCost
  1506	        ) return;
  1507	
  1508	        sim.clan.vaultWood -= woodCost;
  1509	        sim.clan.vaultWheat -= wheatCost;
  1510	        sim.clan.vaultIron -= ironCost;
  1511	        sim.clan.blueprintBalance -= blueprintCost;
  1512	        sim.clan.monumentLevel = nextLevel;
  1513	    }
  1514	
  1515	    function _simulateCompleteMission(Clansman memory cs, Mission memory m)
  1516	        internal
  1517	        view
  1518	        returns (Clansman memory, Mission memory)
  1519	    {
  1520	        cs.state = ClansmanState.WAITING;
  1521	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1522	        m.active = false;
  1523	        return (cs, m);
  1524	    }
  1525	
  1526	    // =========================================================================
  1527	    // BANDIT STATE MACHINE
  1528	    // =========================================================================
  1529	
  1530	    function _spawnBandit(uint8 region, uint32 strength) internal returns (uint32 id) {
  1531	        require(
  1532	            region >= ClanWorldConstants.REGION_FOREST && region <= ClanWorldConstants.REGION_DEEP_SEA,
  1533	            "ClanWorld: invalid bandit region"
  1534	        );
  1535	        require(strength > 0, "ClanWorld: invalid bandit strength");
  1536	
  1537	        id = _nextBanditId++;
  1538	        _bandits[id] = BanditTroop({
  1539	            id: id,
  1540	            region: region,
  1541	            state: BanditState.Spawned,
  1542	            targetClanId: 0,
  1543	            tickEnteredState: _world.currentTick,
  1544	            strength: strength,
  1545	            carryWood: 0,
  1546	            carryIron: 0,
  1547	            carryWheat: 0,
  1548	            carryFish: 0,
  1549	            carryGold: 0
  1550	        });
  1551	        _banditsByRegion[region].push(id);
  1552	        _activeBanditCount += 1;
  1553	
  1554	        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
  1555	        spawnState.lastSpawnTick = _world.currentTick;
  1556	        spawnState.probabilityAccum = 0;
  1557	
  1558	        if (_world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
  1559	            _world.activeBanditId = id;
  1560	        }
  1561	
  1562	        emit BanditSpawned(id, region, 0, _banditStrengthForLegacyEvent(strength));
  1563	    }
  1564	
  1565	    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
  1566	        require(targetClanId != ClanWorldConstants.CLAN_ID_NULL, "ClanWorld: invalid bandit target");
  1567	        _bandits[id].targetClanId = targetClanId;
  1568	        _transitionBanditState(id, BanditState.Attacking);
  1569	    }
  1570	
  1571	    function _transitionBanditState(uint32 id, BanditState newState) internal {
  1572	        BanditTroop storage bandit = _bandits[id];
  1573	        require(bandit.id != ClanWorldConstants.BANDIT_ID_NULL, "ClanWorld: bandit not found");
  1574	        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
  1575	
  1576	        BanditState oldState = bandit.state;
  1577	        require(_isValidBanditTransition(bandit, newState), "ClanWorld: invalid bandit transition");
  1578	
  1579	        if (newState == BanditState.Defeated) {
  1580	            emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
  1581	            _deleteBandit(id);
  1582	            return;
  1583	        }
  1584	
  1585	        bandit.state = newState;
  1586	        bandit.tickEnteredState = _world.currentTick;
  1587	        if (newState != BanditState.Attacking) {
  1588	            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
  1589	        }
  1590	
  1591	        emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
  1592	    }
  1593	
  1594	    function _isValidBanditTransition(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1595	        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
  1596	        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
  1597	        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
  1598	        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
  1599	        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
  1600	        return false;
  1601	    }
  1602	
  1603	    function _canBanditLeaveSpawned(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1604	        return newState == BanditState.Escaped
  1605	            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
  1606	    }
  1607	
  1608	    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1609	        return newState == BanditState.Escaped
  1610	            || (newState == BanditState.Attacking
  1611	                && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
  1612	                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS);
  1613	    }
  1614	
  1615	    function _canBanditLeaveAttacking(BanditState newState) internal pure returns (bool) {
  1616	        return newState == BanditState.Defeated || newState == BanditState.Escaped;
  1617	    }
  1618	
  1619	    function _canBanditLeaveEscaped(BanditState newState) internal pure returns (bool) {
  1620	        return newState == BanditState.Resting;
  1621	    }
  1622	
  1623	    function _canBanditLeaveResting(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1624	        return newState == BanditState.Escaped
  1625	            || (newState == BanditState.Camped
  1626	                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS);
  1627	    }
  1628	
  1629	    function _deleteBandit(uint32 id) internal {
  1630	        BanditTroop storage bandit = _bandits[id];
  1631	        uint8 region = bandit.region;
  1632	        uint32[] storage regionBandits = _banditsByRegion[region];
  1633	        for (uint256 i = 0; i < regionBandits.length; i++) {
  1634	            if (regionBandits[i] == id) {
  1635	                regionBandits[i] = regionBandits[regionBandits.length - 1];
  1636	                regionBandits.pop();
  1637	                break;
  1638	            }
  1639	        }
  1640	
  1641	        delete _bandits[id];
  1642	        if (_activeBanditCount > 0) {
  1643	            _activeBanditCount -= 1;
  1644	        }
  1645	        if (_world.activeBanditId == id) {
  1646	            _world.activeBanditId = _findOldestActiveBandit();
  1647	        }
  1648	    }
  1649	
  1650	    function _findOldestActiveBandit() internal view returns (uint32 oldestBanditId) {
  1651	        // V1 caps live troops at MAX_TOTAL_BANDITS = 8, so scanning the region
  1652	        // indexes is bounded even though storage mappings cannot be enumerated.
  1653	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1654	            uint32[] storage regionBandits = _banditsByRegion[region];
  1655	            for (uint256 i = 0; i < regionBandits.length; i++) {
  1656	                uint32 candidateId = regionBandits[i];
  1657	                BanditTroop storage candidate = _bandits[candidateId];
  1658	                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
  1659	                    continue;
  1660	                }
  1661	                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
  1662	                    oldestBanditId = candidateId;
  1663	                }
  1664	            }
  1665	        }
  1666	    }
  1667	
  1668	    function _advanceBanditStates(uint64 closedTick) internal {
  1669	        require(_world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
  1670	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1671	            uint32[] storage regionBandits = _banditsByRegion[region];
  1672	            for (uint256 i = 0; i < regionBandits.length; i++) {
  1673	                uint32 banditId = regionBandits[i];
  1674	                BanditTroop storage bandit = _bandits[banditId];
  1675	                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
  1676	                    _transitionBanditState(banditId, BanditState.Camped);
  1677	                } else if (
  1678	                    bandit.state == BanditState.Camped
  1679	                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS
  1680	                ) {
  1681	                    uint32 targetClanId = _pickBanditAttackTarget(bandit);
  1682	                    if (targetClanId == ClanWorldConstants.CLAN_ID_NULL) {
  1683	                        _transitionBanditState(banditId, BanditState.Escaped);
  1684	                        emit BanditEscaped(banditId, closedTick);
  1685	                    } else {
  1686	                        _transitionBanditToAttacking(banditId, targetClanId);
  1687	                    }
  1688	                } else if (
  1689	                    bandit.state == BanditState.Escaped
  1690	                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
  1691	                ) {
  1692	                    _transitionBanditState(banditId, BanditState.Resting);
  1693	                } else if (
  1694	                    bandit.state == BanditState.Resting
  1695	                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
  1696	                ) {
  1697	                    _transitionBanditState(banditId, BanditState.Camped);
  1698	                }
  1699	            }
  1700	        }
  1701	    }
  1702	
  1703	    function _pickBanditAttackTarget(BanditTroop storage bandit) internal view returns (uint32 targetClanId) {
  1704	        uint32[MAX_CLANS] memory tiedClanIds;
  1705	        uint256 tiedCount;
  1706	        uint256 bestLootValue;
  1707	
  1708	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1709	            uint32 clanId = _allClanIds[i];
  1710	            Clan storage clan = _clans[clanId];
  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
  1712	                continue;
  1713	            }
  1714	
  1715	            uint256 lootValue = _lootValueRaw(clan);
  1716	            if (tiedCount == 0 || lootValue > bestLootValue) {
  1717	                bestLootValue = lootValue;
  1718	                tiedClanIds[0] = clanId;
  1719	                tiedCount = 1;
  1720	            } else if (lootValue == bestLootValue) {
  1721	                tiedClanIds[tiedCount] = clanId;
  1722	                tiedCount++;
  1723	            }
  1724	        }
  1725	
  1726	        if (tiedCount == 0) {
  1727	            return ClanWorldConstants.CLAN_ID_NULL;
  1728	        }
  1729	        if (tiedCount == 1) {
  1730	            return tiedClanIds[0];
  1731	        }
  1732	
  1733	        uint256 selected = RNG.rngBounded(_world.currentTickSeed, DOMAIN_BANDIT_TARGET_PICK, bandit.id, tiedCount);
  1734	        return tiedClanIds[selected];
  1735	    }
  1736	
  1737	    function _banditStrengthForLegacyEvent(uint32 strength) internal pure returns (uint16) {
  1738	        if (strength > type(uint16).max) return type(uint16).max;
  1739	        // forge-lint: disable-next-line(unsafe-typecast)
  1740	        return uint16(strength);
  1741	    }
  1742	
  1743	    function _resolveAttackingBandits(uint64 closedTick) internal {
  1744	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1745	            uint32[] storage regionBandits = _banditsByRegion[region];
  1746	            uint256 i = 0;
  1747	            while (i < regionBandits.length) {
  1748	                uint32 banditId = regionBandits[i];
  1749	                BanditTroop storage bandit = _bandits[banditId];
  1750	                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
  1751	                if (shouldResolve) {
  1752	                    _resolveBanditAttack(banditId, closedTick);
  1753	                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
  1754	                        continue;
  1755	                    }
  1756	                }
  1757	                i++;
  1758	            }
  1759	        }
  1760	    }
  1761	
  1762	    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
  1763	        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");
  1764	
  1765	        BanditTroop storage bandit = _bandits[banditId];
  1766	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
  1767	            return;
  1768	        }
  1769	        if (bandit.tickEnteredState != closedTick) {
  1770	            return;
  1771	        }
  1772	
  1773	        uint32 targetClanId = bandit.targetClanId;
  1774	        Clan storage targetClan = _clans[targetClanId];
  1775	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
  1776	            _transitionBanditState(banditId, BanditState.Escaped);
  1777	            emit BanditEscaped(banditId, closedTick);
  1778	            return;
  1779	        }
  1780	
  1781	        _settleClan(targetClanId);
  1782	        if (bandit.state != BanditState.Attacking || bandit.targetClanId != targetClanId) {
  1783	            return;
  1784	        }
  1785	        if (targetClan.clanState == ClanState.DEAD) {
  1786	            _transitionBanditState(banditId, BanditState.Escaped);
  1787	            emit BanditEscaped(banditId, closedTick);
  1788	            return;
  1789	        }
  1790	
  1791	        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
  1792	        if (
  1793	            bandit.state != BanditState.Attacking || bandit.targetClanId != targetClanId
  1794	                || targetClan.clanState == ClanState.DEAD
  1795	        ) {
  1796	            return;
  1797	        }
  1798	
  1799	        bytes32 tickSeed = _world.currentTickSeed;
  1800	        uint32 banditAttackPower = bandit.strength;
  1801	        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
  1802	        bool defeated = uint256(totalClansmanDefense) >= uint256(banditAttackPower) * 2;
  1803	
  1804	        uint32 wallDamage;
  1805	        uint32 baseAbsorbed;
  1806	        uint32 clansmanDamageAbsorbed;
  1807	        if (!defeated) {
  1808	            uint32 incomingDamage =
  1809	                banditAttackPower > totalClansmanDefense ? banditAttackPower - totalClansmanDefense : 0;
  1810	            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
  1811	            (incomingDamage, baseAbsorbed) = _applyBanditBaseDefense(targetClan, incomingDamage);
  1812	            clansmanDamageAbsorbed =
  1813	                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
  1814	        }
  1815	
  1816	        uint32 totalDefense = totalClansmanDefense + wallDamage + baseAbsorbed + clansmanDamageAbsorbed;
  1817	        emit BanditAttackResolved(
  1818	            banditId,
  1819	            targetClanId,
  1820	            defeated,
  1821	            _uint16Clamp(banditAttackPower),
  1822	            _uint16Clamp(totalDefense),
  1823	            targetClan.wallLevel,
  1824	            0,
  1825	            0,
  1826	            0,
  1827	            0,
  1828	            closedTick
  1829	        );
  1830	
  1831	        if (defeated) {
  1832	            emit BanditDefeated(banditId, targetClanId, closedTick);
  1833	            _distributeBanditLootToDefendingClans(banditId, targetClanId);
  1834	            targetClan.blueprintBalance += BLUEPRINT_UNIT;
  1835	            emit BlueprintEarned(targetClanId, banditId, BLUEPRINT_UNIT, closedTick);
  1836	            _transitionBanditState(banditId, BanditState.Defeated);
  1837	        } else {
  1838	            _transitionBanditState(banditId, BanditState.Escaped);
  1839	            emit BanditEscaped(banditId, closedTick);
  1840	        }
  1841	    }
  1842	
  1843	    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
  1844	        BanditTroop storage bandit = _bandits[banditId];
  1845	        uint32[] memory rewardedClanIds = _activeDefendingClanIds(targetClanId);
  1846	        uint256 nDefendingClans = rewardedClanIds.length;
  1847	
  1848	        uint256 perWood;
  1849	        uint256 perIron;
  1850	        uint256 perWheat;
  1851	        uint256 perFish;
  1852	        uint256 perGold;
  1853	        if (nDefendingClans > 0) {
  1854	            perWood = _perClanBanditLootShare(bandit.carryWood, nDefendingClans);
  1855	            perIron = _perClanBanditLootShare(bandit.carryIron, nDefendingClans);
  1856	            perWheat = _perClanBanditLootShare(bandit.carryWheat, nDefendingClans);
  1857	            perFish = _perClanBanditLootShare(bandit.carryFish, nDefendingClans);
  1858	            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
  1859	
  1860	            for (uint256 i = 0; i < rewardedClanIds.length; i++) {
  1861	                Clan storage defenderClan = _clans[rewardedClanIds[i]];
  1862	                defenderClan.vaultWood += perWood;
  1863	                defenderClan.vaultIron += perIron;
  1864	                defenderClan.vaultWheat += perWheat;
  1865	                defenderClan.vaultFish += perFish;
  1866	                defenderClan.goldBalance += perGold;
  1867	            }
  1868	        }
  1869	
  1870	        emit LootDistributed(
  1871	            banditId,
  1872	            rewardedClanIds,
  1873	            perWood,
  1874	            perWheat,
  1875	            perFish,
  1876	            perIron,
  1877	            perGold,
  1878	            bandit.carryWood - (perWood * nDefendingClans),
  1879	            bandit.carryWheat - (perWheat * nDefendingClans),
  1880	            bandit.carryFish - (perFish * nDefendingClans),
  1881	            bandit.carryIron - (perIron * nDefendingClans),
  1882	            bandit.carryGold - (perGold * nDefendingClans)
  1883	        );
  1884	    }
  1885	
  1886	    function _perClanBanditLootShare(uint256 loot, uint256 nDefendingClans) internal pure returns (uint256) {
  1887	        if (nDefendingClans == 1) {
  1888	            return loot;
  1889	        }
  1890	        return ((loot / RESOURCE_UNIT) / nDefendingClans) * RESOURCE_UNIT;
  1891	    }
  1892	
  1893	    function _activeDefendingClanIds(uint32 targetClanId) internal view returns (uint32[] memory clanIds) {
  1894	        uint8 targetRegion = _clans[targetClanId].baseRegion;
  1895	        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
  1896	        uint256 count;
  1897	
  1898	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1899	            if (_clanHasActiveDefenderForTarget(defendingClans[i], targetClanId, targetRegion)) {
  1900	                count++;
  1901	            }
  1902	        }
  1903	
  1904	        clanIds = new uint32[](count);
  1905	        uint256 out;

exec
/bin/bash -lc "nl -ba packages/contracts/src/StubPool.sol | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/// @notice Constant-product AMM pool for one resource/gold pair.
     5	///         Reserves are tracked internally; ClanWorld calls the math
     6	///         functions to compute exchange rates, then mints/credits balances
     7	///         directly via the internal accounting model.
     8	///         No real ERC20 transfers occur here — the pool is the math oracle.
     9	contract StubPool {
    10	    address public immutable TOKEN_A;  // resource token
    11	    address public immutable TOKEN_B;  // gold token
    12	    address public immutable ENGINE;   // ClanWorld address
    13	
    14	    uint256 public reserveA;          // resource reserve
    15	    uint256 public reserveB;          // gold reserve
    16	
    17	    bool private _seeded;
    18	
    19	    modifier onlyEngine() {
    20	        require(msg.sender == ENGINE, "StubPool: only engine");
    21	        _;
    22	    }
    23	
    24	    constructor(address tokenA_, address tokenB_, address engine_) {
    25	        TOKEN_A = tokenA_;
    26	        TOKEN_B = tokenB_;
    27	        ENGINE = engine_;
    28	    }
    29	
    30	    /// @notice Called by ClanWorld at seedPools time to set initial reserves.
    31	    ///         Can only be called once.
    32	    function seed(uint256 amountA, uint256 amountB) external onlyEngine {
    33	        require(!_seeded, "StubPool: already seeded");
    34	        require(amountA > 0 && amountB > 0, "StubPool: zero seed");
    35	        reserveA = amountA;
    36	        reserveB = amountB;
    37	        _seeded = true;
    38	    }
    39	
    40	    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
    41	    ///         Constant-product: goldOut = reserveB * amountIn / (reserveA + amountIn).
    42	    ///         Updates reserves. Called by ClanWorld; no token transfers.
    43	    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
    44	        require(amountIn > 0, "StubPool: zero amount");
    45	        require(reserveA > 0 && reserveB > 0, "StubPool: not seeded");
    46	        goldOut = (reserveB * amountIn) / (reserveA + amountIn);
    47	        require(goldOut > 0, "StubPool: zero output");
    48	        reserveA += amountIn;
    49	        reserveB -= goldOut;
    50	    }
    51	
    52	    /// @notice Quote a buy without updating reserves (view).
    53	    ///         goldIn = ceil(reserveB * amountOut / (reserveA - amountOut)).
    54	    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
    55	        require(amountOut > 0, "StubPool: zero amount");
    56	        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
    57	        uint256 num = reserveB * amountOut;
    58	        uint256 denom_ = reserveA - amountOut;
    59	        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
    60	    }
    61	
    62	    /// @notice Exact-output buy: clan buys amountOut of resource, pays goldIn.
    63	    ///         Returns actual goldIn charged (ceiling arithmetic).
    64	    ///         Updates reserves. Called by ClanWorld; no token transfers.
    65	    function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
    66	        require(amountOut > 0, "StubPool: zero amount");
    67	        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
    68	        require(reserveB > 0, "StubPool: not seeded");
    69	        uint256 num = reserveB * amountOut;
    70	        uint256 denom_ = reserveA - amountOut;
    71	        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
    72	        reserveA -= amountOut;
    73	        reserveB += goldIn;
    74	    }
    75	
    76	    function getReserves() external view returns (uint256, uint256) {
    77	        return (reserveA, reserveB);
    78	    }
    79	}

exec
/bin/bash -lc "nl -ba packages/contracts/src/MinimalERC20.sol | sed -n '1,180p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/// @notice Minimal ERC20 with owner-only mint. No external deps.
     5	contract MinimalERC20 {
     6	    string public name;
     7	    string public symbol;
     8	    uint8 public constant decimals = 18;
     9	
    10	    uint256 public totalSupply;
    11	    address public immutable OWNER;
    12	
    13	    mapping(address => uint256) public balanceOf;
    14	    mapping(address => mapping(address => uint256)) public allowance;
    15	
    16	    event Transfer(address indexed from, address indexed to, uint256 value);
    17	    event Approval(address indexed owner_, address indexed spender, uint256 value);
    18	
    19	    constructor(string memory name_, string memory symbol_) {
    20	        name = name_;
    21	        symbol = symbol_;
    22	        OWNER = msg.sender;
    23	    }
    24	
    25	    /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
    26	    /// body slim so the require isn't duplicated at every call site.
    27	    function _onlyOwner() internal view {
    28	        require(msg.sender == OWNER, "not owner");
    29	    }
    30	
    31	    modifier onlyOwner() {
    32	        _onlyOwner();
    33	        _;
    34	    }
    35	
    36	    function mint(address to, uint256 amount) external onlyOwner {
    37	        totalSupply += amount;
    38	        balanceOf[to] += amount;
    39	        emit Transfer(address(0), to, amount);
    40	    }
    41	
    42	    function transfer(address to, uint256 amount) external returns (bool) {
    43	        balanceOf[msg.sender] -= amount;
    44	        balanceOf[to] += amount;
    45	        emit Transfer(msg.sender, to, amount);
    46	        return true;
    47	    }
    48	
    49	    function transferFrom(address from, address to, uint256 amount) external returns (bool) {
    50	        allowance[from][msg.sender] -= amount;
    51	        balanceOf[from] -= amount;
    52	        balanceOf[to] += amount;
    53	        emit Transfer(from, to, amount);
    54	        return true;
    55	    }
    56	
    57	    function approve(address spender, uint256 amount) external returns (bool) {
    58	        allowance[msg.sender][spender] = amount;
    59	        emit Approval(msg.sender, spender, amount);
    60	        return true;
    61	    }
    62	}

codex
One wrinkle: the checked-out files here look like the pre-fix version while your stdin contains the review target diff. I’m using the repo for surrounding context and the stdin diff as source of truth for changed code; that’s still enough to trace the behavior.
exec
/bin/bash -lc 'rg -n "function heartbeat|_settleCompletingMissions|_executeScheduledMarketActions|_resolveAction|function submitClanOrders|function _processClanOrder|_createMission|_sortScheduledMarketActions|settleClansman|nonReentrant|ReentrancyGuard" packages/contracts/src/ClanWorld.sol packages/contracts/src/util/ReentrancyGuard.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:42:import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
packages/contracts/src/ClanWorld.sol:49:contract ClanWorld is IClanWorld, ReentrancyGuard {
packages/contracts/src/ClanWorld.sol:344:    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
packages/contracts/src/ClanWorld.sol:386:                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
packages/contracts/src/ClanWorld.sol:616:    function _resolveAction(
packages/contracts/src/ClanWorld.sol:2305:    function heartbeat() external override nonReentrant {
packages/contracts/src/ClanWorld.sol:2316:        _settleCompletingMissions(closedTick);
packages/contracts/src/ClanWorld.sol:2319:        _executeScheduledMarketActions(closedTick);
packages/contracts/src/ClanWorld.sol:2350:    function _settleCompletingMissions(uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:2415:    function settleClan(uint32 clanId) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:2423:    function settleClansman(uint32 csId) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:2439:    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
packages/contracts/src/ClanWorld.sol:2516:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
packages/contracts/src/ClanWorld.sol:2519:        nonReentrant
packages/contracts/src/ClanWorld.sol:2800:    function _executeScheduledMarketActions(uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:2860:            _sortScheduledMarketActionsByCommitSequence(nextActions);
packages/contracts/src/ClanWorld.sol:2866:    function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
packages/contracts/src/ClanWorld.sol:3110:            // _resolveAction records mission completion but does not execute any swap.
packages/contracts/src/ClanWorld.sol:3140:    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:3158:    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
packages/contracts/src/util/ReentrancyGuard.sol:4:abstract contract ReentrancyGuard {
packages/contracts/src/util/ReentrancyGuard.sol:13:    modifier nonReentrant() {

exec
/bin/bash -lc "rg -n \"Unicorn|market|Market|FIFO|slippage|gold|pool|5\\.5|5\\.6|5\\.7|11\\.2\" docs/planning/clanworld_v4_spec.md docs/planning/clanworld_v4_1_addendum.md docs/planning/clanworld_v4_2_state_schema_interface_spec.md docs/planning/clanworld_v4_3_schema_patch.md docs/planning/CANONICAL_SPEC.md docs/planning/V1/01\\ Blockchain\\ Game\\ Spec/clanworld_v4_spec.md docs/planning/V1/01\\ Blockchain\\ Game\\ Spec/clanworld_v4_2_state_schema_interface_spec.md" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/CANONICAL_SPEC.md:27:### Market-buy: exact-input vs exact-output
docs/planning/CANONICAL_SPEC.md:29:**The conflict:** `clanworld_v4_1_addendum.md §A7` originally stated "All v1 market actions are Exact Input actions only" and described `market_buy` as spending an exact amount of gold. This contradicts `IClanWorld.sol`, `clanworld_v4_2_state_schema_interface_spec.md §8.3–8.4`, and `clanworld_v4_3_schema_patch.md`.
docs/planning/CANONICAL_SPEC.md:32:- `market_sell` — exact input: `marketAmount` = exact resource amount to sell; gold out is AMM-determined.
docs/planning/CANONICAL_SPEC.md:33:- `market_buy` — exact output: `marketAmount` = exact resource amount to receive; `maxGoldIn` = maximum gold willing to spend (slippage guard). Buy fails if required gold exceeds `maxGoldIn` at execution time.
docs/planning/CANONICAL_SPEC.md:45:v4.5 remains authoritative for: tick cadence per-submission (§2), agent vocabulary (Elder / orchestrator / toolbelt / situation block — §11.2), and §11.3 sections of `clanworld_agent_runner_spec.md` that remain valid.
docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/clanworld_v4_spec.md:39:3 = Unicorn Town
docs/planning/clanworld_v4_spec.md:100:Homebases may exist across the world but may **not** be placed in Unicorn Town.
docs/planning/clanworld_v4_spec.md:110:Deep Sea and Unicorn Town are not valid homebase spawn regions.
docs/planning/clanworld_v4_spec.md:127:2. resolve all **scheduled market actions** that mature in the closing tick
docs/planning/clanworld_v4_spec.md:162:- all scheduled market actions at heartbeat boundaries
docs/planning/clanworld_v4_spec.md:212:- goto Unicorn Town, sell wood
docs/planning/clanworld_v4_spec.md:215:- goto noop, sell wood immediately in Unicorn Town
docs/planning/clanworld_v4_spec.md:264:- `market_sell` requested with invalid params
docs/planning/clanworld_v4_spec.md:317:- scheduled market buy
docs/planning/clanworld_v4_spec.md:318:- scheduled market sell
docs/planning/clanworld_v4_spec.md:323:V1 only includes **immediate market actions in Unicorn Town**.
docs/planning/clanworld_v4_spec.md:340:This is especially important for immediate market actions in Unicorn Town.
docs/planning/clanworld_v4_spec.md:424:- gold bonus chance = `2%`
docs/planning/clanworld_v4_spec.md:425:- gold bonus amount = `1e18`
docs/planning/clanworld_v4_spec.md:502:# 5. Unicorn Town Market Model
docs/planning/clanworld_v4_spec.md:504:## 5.1 Market venue
docs/planning/clanworld_v4_spec.md:505:Unicorn Town is the only region where Uniswap market actions may be executed in v1.
docs/planning/clanworld_v4_spec.md:508:V1 seeds the following pools:
docs/planning/clanworld_v4_spec.md:514:Blueprint Fragments are transferable but are **not** part of Uniswap pool routing in v1.
docs/planning/clanworld_v4_spec.md:516:## 5.3 Market execution modes
docs/planning/clanworld_v4_spec.md:517:Market actions support two execution modes:
docs/planning/clanworld_v4_spec.md:518:1. scheduled market action
docs/planning/clanworld_v4_spec.md:519:2. immediate market action
docs/planning/clanworld_v4_spec.md:521:## 5.4 Scheduled market action
docs/planning/clanworld_v4_spec.md:522:Scheduled market action is used when the clansman is not already eligible to trade immediately.
docs/planning/clanworld_v4_spec.md:525:- worker must travel to Unicorn Town first
docs/planning/clanworld_v4_spec.md:526:- after arrival, the market action occupies one full action tick
docs/planning/clanworld_v4_spec.md:530:- `goto Unicorn Town, action = sell wood`
docs/planning/clanworld_v4_spec.md:534:- one action tick in Unicorn Town is consumed
docs/planning/clanworld_v4_spec.md:536:- worker becomes `WAITING` in Unicorn Town
docs/planning/clanworld_v4_spec.md:538:## 5.5 Immediate market action
docs/planning/clanworld_v4_spec.md:539:Immediate market action is allowed if and only if all of the following are true at tx execution time:
docs/planning/clanworld_v4_spec.md:540:- clansman is physically in Unicorn Town
docs/planning/clanworld_v4_spec.md:544:- required inventory or gold balance is available
docs/planning/clanworld_v4_spec.md:548:- `action = market_buy` or `market_sell`
docs/planning/clanworld_v4_spec.md:552:## 5.6 Immediate market action effects
docs/planning/clanworld_v4_spec.md:553:On successful immediate market action:
docs/planning/clanworld_v4_spec.md:555:- the worker remains in Unicorn Town
docs/planning/clanworld_v4_spec.md:560:Immediate market actions are therefore still **missions**, not an out-of-band bypass.
docs/planning/clanworld_v4_spec.md:562:## 5.7 Immediate vs scheduled collision ordering
docs/planning/clanworld_v4_spec.md:563:If immediate and scheduled market actions affect the same pool in the same tick:
docs/planning/clanworld_v4_spec.md:564:- immediate market actions execute against the current pool state at the time their Elder tx lands
docs/planning/clanworld_v4_spec.md:565:- scheduled market actions execute later, at the heartbeat that closes the tick, in deterministic FIFO order by mission commit order
docs/planning/clanworld_v4_spec.md:567:This intentionally allows a clansman already stationed in Unicorn Town to front-run scheduled trades that will execute at tick close.
docs/planning/clanworld_v4_spec.md:569:This is an intended feature of the game’s adversarial market design, not a bug.
docs/planning/clanworld_v4_spec.md:571:## 5.8 Scheduled market action ordering
docs/planning/clanworld_v4_spec.md:572:If multiple scheduled market actions mature in the same tick, Keeper executes them in deterministic FIFO order based on mission commit order.
docs/planning/clanworld_v4_spec.md:574:Each swap sees updated market state from earlier swaps in that sequence.
docs/planning/clanworld_v4_spec.md:577:For v1, market actions execute with **no slippage guard**.
docs/planning/clanworld_v4_spec.md:584:- scheduled market actions resolved at heartbeat close
docs/planning/clanworld_v4_spec.md:585:- immediate market actions executed in Elder txs while already in Unicorn Town
docs/planning/clanworld_v4_spec.md:588:- adversarial market play
docs/planning/clanworld_v4_spec.md:591:- no swap-failure branching due to slippage in v1
docs/planning/clanworld_v4_spec.md:593:## 5.10 Failed market actions
docs/planning/clanworld_v4_spec.md:594:If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
docs/planning/clanworld_v4_spec.md:597:- clansman becomes or remains `WAITING` in Unicorn Town
docs/planning/clanworld_v4_spec.md:599:## 5.11 Initial pool seeding
docs/planning/clanworld_v4_spec.md:600:At deploy time, a treasury or deployer-controlled bootstrap address mints and seeds the initial resource and gold liquidity.
docs/planning/clanworld_v4_spec.md:603:- Wood / Gold: `1000 wood : 500 gold`
docs/planning/clanworld_v4_spec.md:604:- Wheat / Gold: `1000 wheat : 700 gold`
docs/planning/clanworld_v4_spec.md:605:- Fish / Gold: `500 fish : 600 gold`
docs/planning/clanworld_v4_spec.md:606:- Iron / Gold: `250 iron : 800 gold`
docs/planning/clanworld_v4_spec.md:608:These numbers are economic placeholders and may be tuned later, but v1 requires concrete initial pool anchors.
docs/planning/clanworld_v4_spec.md:615:- market sales into seeded resource/gold pools
docs/planning/clanworld_v4_spec.md:618:If a pool becomes extremely imbalanced or one side is heavily drained, trading continues subject to normal AMM pricing.
docs/planning/clanworld_v4_spec.md:620:There is no special pool reset or emergency refill mechanic in v1.
docs/planning/clanworld_v4_spec.md:638:Bandits may not spawn in Unicorn Town or Deep Sea.
docs/planning/clanworld_v4_spec.md:683:- converting resources into gold where feasible
docs/planning/clanworld_v4_spec.md:702:- gold = `0` points
docs/planning/clanworld_v4_spec.md:713:Bandits never enter Unicorn Town or Deep Sea.
docs/planning/clanworld_v4_spec.md:789:Bandits do not steal gold in v1.
docs/planning/clanworld_v4_spec.md:796:Bandits do not convert resources into gold in v1.
docs/planning/clanworld_v4_spec.md:797:Bandits do not use Unicorn Town in v1.
docs/planning/clanworld_v4_spec.md:1057:## 11.2 Intentional gameplay consequence
docs/planning/clanworld_v4_spec.md:1104:## 12.5 Starting gold
docs/planning/clanworld_v4_spec.md:1106:- `3e18` gold
docs/planning/clanworld_v4_spec.md:1108:This is intended to enable at least one meaningful early Unicorn Town trade.
docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1165:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/clanworld_v4_spec.md:1168:- immediate market actions require the worker to be in Unicorn Town and `WAITING`
docs/planning/clanworld_v4_spec.md:1169:- scheduled market actions are never lazily resolved; they resolve eagerly at the heartbeat that closes their action tick
docs/planning/clanworld_v4_spec.md:1190:- scheduled vs immediate market execution split
docs/planning/clanworld_v4_spec.md:1191:- no slippage guard in v1
docs/planning/clanworld_v4_spec.md:1207:- initial pool seeding ratios
docs/planning/clanworld_v4_spec.md:1208:- starter vault and gold amounts
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:39:- clan gold purse balances
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:44:- market intent execution timing
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:59:2. resolve all scheduled market actions whose `executeAtTick == T`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:82:- scheduled market action resolution
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:88:### 2.6 Immediate market execution
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:89:Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:92:- requires the worker already be physically in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:167:- clan gold purse balance
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:175:- interaction with seeded Uniswap pools
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:210:- gold
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:214:- market sale proceeds
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:215:- market purchase spending
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:230:If a worker hits the iron mining gold bonus:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:231:- the bonus gold is credited directly to the **clan gold purse** during settlement
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:346:    uint64 nextCommitSequence;   // global monotonic FIFO sequence for scheduled market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:357:    bool poolsSeeded;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:362:    address goldToken;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:393:    uint256 goldBalance;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:460:    MarketExecutionMode marketMode;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:463:    address marketToken;   // market token for buy/sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:464:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:465:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:491:### 7.8 ScheduledMarketAction
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:493:Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:496:struct ScheduledMarketAction {
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:498:    uint64 commitSequence;   // global monotonic FIFO order
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:501:    ActionType action;       // MarketBuy or MarketSell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:503:    address marketToken;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:504:    uint256 marketAmount;    // exact-in for sell, exact-out for buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:565:### 8.3 MarketSell semantics
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:566:`MarketSell` is **exact input only**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:570:- destination = clan gold purse
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:571:- `marketAmount` = exact amount of resource to sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:574:### 8.4 MarketBuy semantics
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:575:`MarketBuy` is **exact output only**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:578:- source = clan gold purse
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:580:- `marketAmount` = exact amount of resource to buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:581:- `maxGoldIn` = max purse gold willing to spend
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:585:- if required gold at execution time exceeds `maxGoldIn`, buy fails
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:586:- if clan purse lacks enough gold at execution time, buy fails
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:588:### 8.5 Market token validation
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:610:function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:671:    uint256 gold,
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:682:- clan gold purse
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:696:    address marketToken;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:697:    uint256 marketAmount;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:721:    uint256 goldSeedForWood;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:722:    uint256 goldSeedForWheat;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:723:    uint256 goldSeedForFish;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:724:    uint256 goldSeedForIron;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:735:## 11. Randomness, market execution, and reentrancy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:755:### 11.2 Immediate vs scheduled market ordering
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:756:Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:758:Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:764:A scheduled market action:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:765:- is created when a travel-based market mission is accepted
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:778:Even if only known V2 pools are used in v1, implementation should still follow safe external-call patterns.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:861:event ImmediateMarketActionExecuted(
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:870:event ScheduledMarketActionExecuted(
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:881:event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:934:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:938:- scheduled market actions are never lazily replayed; they resolve eagerly at heartbeat
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:940:- immediate market actions require `Waiting` + Unicorn Town + cooldown clear
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:958:### 16.2 Immediate market sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:960:- worker is `Waiting` in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:963:- Elder submits `MarketSell(wood, 12e18)`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:967:- clan gold purse increases by output
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:968:- worker stays in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:972:### 16.3 Scheduled market buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:974:- worker is one tick away from Unicorn Town in tick 307
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:975:- Elder submits `MarketBuy(wood, 5e18, 10e18)`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:980:- heartbeat closing tick 308 attempts to buy exactly 5 wood spending at most 10 gold
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:981:- if price moved above 10 gold or purse balance is insufficient, trade fails, worker ends `Waiting`, cooldown consumed
docs/planning/clanworld_v4_1_addendum.md:69:- immediate market action missions submitted while already in Unicorn Town
docs/planning/clanworld_v4_1_addendum.md:107:## A7. Market action input mode
docs/planning/clanworld_v4_1_addendum.md:109:> **[PATCHED 2026-04-26]** The original text of this section incorrectly stated that all v1 market actions are Exact Input. `market_buy` is Exact Output, not Exact Input. The corrected text is below. Authoritative source: `clanworld_v4_2_state_schema_interface_spec.md §8.4` and `IClanWorld.sol`.
docs/planning/clanworld_v4_1_addendum.md:111:### `market_sell`
docs/planning/clanworld_v4_1_addendum.md:112:`market_sell` is **Exact Input**.
docs/planning/clanworld_v4_1_addendum.md:113:- `marketAmount` = exact amount of resource token to sell
docs/planning/clanworld_v4_1_addendum.md:114:- gold output is whatever the AMM returns at execution time
docs/planning/clanworld_v4_1_addendum.md:117:### `market_buy`
docs/planning/clanworld_v4_1_addendum.md:118:`market_buy` is **Exact Output**.
docs/planning/clanworld_v4_1_addendum.md:119:- `marketAmount` = exact amount of resource to receive
docs/planning/clanworld_v4_1_addendum.md:120:- `maxGoldIn` = max purse gold willing to spend (slippage guard)
docs/planning/clanworld_v4_1_addendum.md:121:- buy fails if required gold at execution time exceeds `maxGoldIn`
docs/planning/clanworld_v4_1_addendum.md:122:- buy fails if clan purse lacks sufficient gold at execution time
docs/planning/clanworld_v4_1_addendum.md:126:## A8. Immediate vs scheduled market execution ordering
docs/planning/clanworld_v4_1_addendum.md:130:### Immediate market actions
docs/planning/clanworld_v4_1_addendum.md:132:- physically in Unicorn Town,
docs/planning/clanworld_v4_1_addendum.md:136:then an Elder may submit a market mission that executes **immediately in that tx**.
docs/planning/clanworld_v4_1_addendum.md:138:Immediate actions execute against the current AMM pool state at tx execution time.
docs/planning/clanworld_v4_1_addendum.md:140:### Scheduled market actions
docs/planning/clanworld_v4_1_addendum.md:141:If a market mission requires travel or action-tick maturation, it executes later at the heartbeat that closes its action tick.
docs/planning/clanworld_v4_1_addendum.md:143:Scheduled market actions execute:
docs/planning/clanworld_v4_1_addendum.md:145:- in FIFO order by mission commit order
docs/planning/clanworld_v4_1_addendum.md:148:An immediate market action during tick `T` can front-run scheduled market actions that will execute at the close of tick `T`.
docs/planning/clanworld_v4_1_addendum.md:150:This is intentional and part of the adversarial market design.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:39:- clan gold purse balances
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:44:- market intent execution timing
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:59:2. resolve all scheduled market actions whose `executeAtTick == T`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:82:- scheduled market action resolution
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:88:### 2.6 Immediate market execution
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:89:Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:92:- requires the worker already be physically in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:167:- clan gold purse balance
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:175:- interaction with seeded Uniswap pools
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:210:- gold
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:214:- market sale proceeds
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:215:- market purchase spending
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:230:If a worker hits the iron mining gold bonus:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:231:- the bonus gold is credited directly to the **clan gold purse** during settlement
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:346:    uint64 nextCommitSequence;   // global monotonic FIFO sequence for scheduled market actions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:357:    bool poolsSeeded;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:362:    address goldToken;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:393:    uint256 goldBalance;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:460:    MarketExecutionMode marketMode;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:463:    address marketToken;   // market token for buy/sell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:464:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:465:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:491:### 7.8 ScheduledMarketAction
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:493:Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:496:struct ScheduledMarketAction {
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:498:    uint64 commitSequence;   // global monotonic FIFO order
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:501:    ActionType action;       // MarketBuy or MarketSell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:503:    address marketToken;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:504:    uint256 marketAmount;    // exact-in for sell, exact-out for buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:565:### 8.3 MarketSell semantics
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:566:`MarketSell` is **exact input only**.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:570:- destination = clan gold purse
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:571:- `marketAmount` = exact amount of resource to sell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:574:### 8.4 MarketBuy semantics
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:575:`MarketBuy` is **exact output only**.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:578:- source = clan gold purse
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:580:- `marketAmount` = exact amount of resource to buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:581:- `maxGoldIn` = max purse gold willing to spend
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:585:- if required gold at execution time exceeds `maxGoldIn`, buy fails
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:586:- if clan purse lacks enough gold at execution time, buy fails
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:588:### 8.5 Market token validation
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:610:function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:671:    uint256 gold,
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:682:- clan gold purse
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:696:    address marketToken;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:697:    uint256 marketAmount;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:721:    uint256 goldSeedForWood;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:722:    uint256 goldSeedForWheat;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:723:    uint256 goldSeedForFish;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:724:    uint256 goldSeedForIron;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:735:## 11. Randomness, market execution, and reentrancy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:755:### 11.2 Immediate vs scheduled market ordering
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:756:Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:758:Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:764:A scheduled market action:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:765:- is created when a travel-based market mission is accepted
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:778:Even if only known V2 pools are used in v1, implementation should still follow safe external-call patterns.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:861:event ImmediateMarketActionExecuted(
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:870:event ScheduledMarketActionExecuted(
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:881:event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:934:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:938:- scheduled market actions are never lazily replayed; they resolve eagerly at heartbeat
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:940:- immediate market actions require `Waiting` + Unicorn Town + cooldown clear
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:958:### 16.2 Immediate market sell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:960:- worker is `Waiting` in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:963:- Elder submits `MarketSell(wood, 12e18)`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:967:- clan gold purse increases by output
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:968:- worker stays in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:972:### 16.3 Scheduled market buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:974:- worker is one tick away from Unicorn Town in tick 307
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:975:- Elder submits `MarketBuy(wood, 5e18, 10e18)`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:980:- heartbeat closing tick 308 attempts to buy exactly 5 wood spending at most 10 gold
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:981:- if price moved above 10 gold or purse balance is insufficient, trade fails, worker ends `Waiting`, cooldown consumed
docs/planning/clanworld_v4_3_schema_patch.md:23:For immediate market actions:
docs/planning/clanworld_v4_3_schema_patch.md:26:  - in `UnicornTown`
docs/planning/clanworld_v4_3_schema_patch.md:94:## D. Scheduled Market Action Storage
docs/planning/clanworld_v4_3_schema_patch.md:97:Scheduled market actions are stored as:
docs/planning/clanworld_v4_3_schema_patch.md:99:`mapping(uint64 => ScheduledMarketAction[]) scheduledMarketActionsByTick`
docs/planning/clanworld_v4_3_schema_patch.md:105:- append-only FIFO array for that tick
docs/planning/clanworld_v4_3_schema_patch.md:107:### D.2 FIFO ordering
docs/planning/clanworld_v4_3_schema_patch.md:108:Within a given `executeAtTick`, scheduled market actions execute in:
docs/planning/clanworld_v4_3_schema_patch.md:109:1. ascending append order in `scheduledMarketActionsByTick[executeAtTick]`
docs/planning/clanworld_v4_3_schema_patch.md:114:- iterate `scheduledMarketActionsByTick[T]`
docs/planning/clanworld_v4_3_schema_patch.md:115:- execute each action in FIFO order
docs/planning/clanworld_v4_3_schema_patch.md:117:- delete `scheduledMarketActionsByTick[T]` after processing
docs/planning/clanworld_v4_3_schema_patch.md:142:- `keccak256(abi.encode("iron_gold_bonus", currentTickSeed, clansmanId, missionNonce, tick))`
docs/planning/clanworld_v4_3_schema_patch.md:275:`ERR_CARRY_FULL` is especially relevant for explicit market buy validation and any direct action that is rejected because requested output cannot fit remaining carry capacity.
docs/planning/clanworld_v4_3_schema_patch.md:318:- scheduled market execution
docs/planning/clanworld_v4_3_schema_patch.md:344:- scheduled market actions stored by `executeAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:39:3 = Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:100:Homebases may exist across the world but may **not** be placed in Unicorn Town.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:110:Deep Sea and Unicorn Town are not valid homebase spawn regions.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:127:2. resolve all **scheduled market actions** that mature in the closing tick
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:162:- all scheduled market actions at heartbeat boundaries
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:212:- goto Unicorn Town, sell wood
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:215:- goto noop, sell wood immediately in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:264:- `market_sell` requested with invalid params
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:317:- scheduled market buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:318:- scheduled market sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:323:V1 only includes **immediate market actions in Unicorn Town**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:340:This is especially important for immediate market actions in Unicorn Town.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:424:- gold bonus chance = `2%`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:425:- gold bonus amount = `1e18`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:502:# 5. Unicorn Town Market Model
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:504:## 5.1 Market venue
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:505:Unicorn Town is the only region where Uniswap market actions may be executed in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:508:V1 seeds the following pools:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:514:Blueprint Fragments are transferable but are **not** part of Uniswap pool routing in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:516:## 5.3 Market execution modes
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:517:Market actions support two execution modes:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:518:1. scheduled market action
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:519:2. immediate market action
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:521:## 5.4 Scheduled market action
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:522:Scheduled market action is used when the clansman is not already eligible to trade immediately.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:525:- worker must travel to Unicorn Town first
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:526:- after arrival, the market action occupies one full action tick
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:530:- `goto Unicorn Town, action = sell wood`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:534:- one action tick in Unicorn Town is consumed
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:536:- worker becomes `WAITING` in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:538:## 5.5 Immediate market action
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:539:Immediate market action is allowed if and only if all of the following are true at tx execution time:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:540:- clansman is physically in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:544:- required inventory or gold balance is available
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:548:- `action = market_buy` or `market_sell`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:552:## 5.6 Immediate market action effects
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:553:On successful immediate market action:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:555:- the worker remains in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:560:Immediate market actions are therefore still **missions**, not an out-of-band bypass.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:562:## 5.7 Immediate vs scheduled collision ordering
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:563:If immediate and scheduled market actions affect the same pool in the same tick:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:564:- immediate market actions execute against the current pool state at the time their Elder tx lands
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:565:- scheduled market actions execute later, at the heartbeat that closes the tick, in deterministic FIFO order by mission commit order
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:567:This intentionally allows a clansman already stationed in Unicorn Town to front-run scheduled trades that will execute at tick close.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:569:This is an intended feature of the game’s adversarial market design, not a bug.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:571:## 5.8 Scheduled market action ordering
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:572:If multiple scheduled market actions mature in the same tick, Keeper executes them in deterministic FIFO order based on mission commit order.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:574:Each swap sees updated market state from earlier swaps in that sequence.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:577:For v1, market actions execute with **no slippage guard**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:584:- scheduled market actions resolved at heartbeat close
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:585:- immediate market actions executed in Elder txs while already in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:588:- adversarial market play
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:591:- no swap-failure branching due to slippage in v1
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:593:## 5.10 Failed market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:594:If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:597:- clansman becomes or remains `WAITING` in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:599:## 5.11 Initial pool seeding
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:600:At deploy time, a treasury or deployer-controlled bootstrap address mints and seeds the initial resource and gold liquidity.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:603:- Wood / Gold: `1000 wood : 500 gold`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:604:- Wheat / Gold: `1000 wheat : 700 gold`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:605:- Fish / Gold: `500 fish : 600 gold`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:606:- Iron / Gold: `250 iron : 800 gold`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:608:These numbers are economic placeholders and may be tuned later, but v1 requires concrete initial pool anchors.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:615:- market sales into seeded resource/gold pools
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:618:If a pool becomes extremely imbalanced or one side is heavily drained, trading continues subject to normal AMM pricing.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:620:There is no special pool reset or emergency refill mechanic in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:638:Bandits may not spawn in Unicorn Town or Deep Sea.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:683:- converting resources into gold where feasible
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:702:- gold = `0` points
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:713:Bandits never enter Unicorn Town or Deep Sea.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:789:Bandits do not steal gold in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:796:Bandits do not convert resources into gold in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:797:Bandits do not use Unicorn Town in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1057:## 11.2 Intentional gameplay consequence
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1104:## 12.5 Starting gold
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1106:- `3e18` gold
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1108:This is intended to enable at least one meaningful early Unicorn Town trade.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1165:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1168:- immediate market actions require the worker to be in Unicorn Town and `WAITING`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1169:- scheduled market actions are never lazily resolved; they resolve eagerly at the heartbeat that closes their action tick
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1190:- scheduled vs immediate market execution split
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1191:- no slippage guard in v1
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1207:- initial pool seeding ratios
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1208:- starter vault and gold amounts

exec
/bin/bash -lc "rg -n \"Market(Buy|Sell)|ResourceType\\.(Iron|Wheat|Fish)|getResourceToken|getPool|getPrice|ScheduledMarketActionExecuted|ImmediateMarketActionExecuted|MarketActionFailed|seedPools|all 4|four\" packages/contracts/test packages/contracts/src" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/test/MissionTiming.t.sol:100:        assertEq(mission.settlesAtTick, mission.executesAtTick + 4, "four tick action duration");
packages/contracts/test/MissionTiming.t.sol:133:        assertEq(world.getActionDuration(ActionType.MarketBuy), 1, "market buy");
packages/contracts/test/MissionTiming.t.sol:134:        assertEq(world.getActionDuration(ActionType.MarketSell), 1, "market sell");
packages/contracts/test/Reentrancy.t.sol:123:        OrderResult[] memory results = _submitMarketSell(clanId, csId, address(woodToken), 5e18);
packages/contracts/test/Reentrancy.t.sol:175:        world.seedPools(cfg);
packages/contracts/test/Reentrancy.t.sol:192:    function _submitMarketSell(uint32 clanId, uint32 csId, address token, uint256 amount)
packages/contracts/test/Reentrancy.t.sol:200:            action: ActionType.MarketSell,
packages/contracts/src/ClanWorld.sol:649:        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
packages/contracts/src/ClanWorld.sol:1239:        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
packages/contracts/src/ClanWorld.sol:2673:        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
packages/contracts/src/ClanWorld.sol:2720:        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
packages/contracts/src/ClanWorld.sol:2813:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
packages/contracts/src/ClanWorld.sol:2822:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
packages/contracts/src/ClanWorld.sol:2826:            if (sma.action == ActionType.MarketSell) {
packages/contracts/src/ClanWorld.sol:2827:                try this._executeMarketSellExternal(
packages/contracts/src/ClanWorld.sol:2833:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
packages/contracts/src/ClanWorld.sol:2835:            } else if (sma.action == ActionType.MarketBuy) {
packages/contracts/src/ClanWorld.sol:2836:                try this._executeMarketBuyExternal(
packages/contracts/src/ClanWorld.sol:2848:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
packages/contracts/src/ClanWorld.sol:2878:    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
packages/contracts/src/ClanWorld.sol:2879:    function _executeMarketSellExternal(
packages/contracts/src/ClanWorld.sol:2888:        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
packages/contracts/src/ClanWorld.sol:2891:    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
packages/contracts/src/ClanWorld.sol:2892:    function _executeMarketBuyExternal(
packages/contracts/src/ClanWorld.sol:2902:        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
packages/contracts/src/ClanWorld.sol:2960:    function _executeMarketSell(
packages/contracts/src/ClanWorld.sol:2969:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
packages/contracts/src/ClanWorld.sol:2974:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
packages/contracts/src/ClanWorld.sol:2980:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
packages/contracts/src/ClanWorld.sol:2987:        emit ScheduledMarketActionExecuted(
packages/contracts/src/ClanWorld.sol:2993:    function _executeMarketBuy(
packages/contracts/src/ClanWorld.sol:3003:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
packages/contracts/src/ClanWorld.sol:3008:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
packages/contracts/src/ClanWorld.sol:3018:            emit MarketActionFailed(
packages/contracts/src/ClanWorld.sol:3019:                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
packages/contracts/src/ClanWorld.sol:3024:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
packages/contracts/src/ClanWorld.sol:3033:        emit ScheduledMarketActionExecuted(
packages/contracts/src/ClanWorld.sol:3091:        // MarketBuy/MarketSell: must target Unicorn Town
packages/contracts/src/ClanWorld.sol:3092:        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
packages/contracts/src/ClanWorld.sol:3139:    ///         Must be called before seedPools. Callable only once.
packages/contracts/src/ClanWorld.sol:3157:    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
packages/contracts/src/ClanWorld.sol:3158:    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:3246:                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
packages/contracts/src/ClanWorld.sol:3247:                || action == ActionType.MarketSell
packages/contracts/test/HeartbeatOrdering.t.sol:231:        world.seedPools(cfg);
packages/contracts/test/HeartbeatOrdering.t.sol:271:        world.seedPools(cfg);
packages/contracts/test/HeartbeatOrdering.t.sol:281:    //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
packages/contracts/test/HeartbeatOrdering.t.sol:306:        // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
packages/contracts/test/HeartbeatOrdering.t.sol:308:        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
packages/contracts/test/HeartbeatOrdering.t.sol:367:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, address(woodToken), 5e18, 0);
packages/contracts/test/HeartbeatOrdering.t.sol:461:        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
packages/contracts/test/ClanWorld.t.sol:78:    /// @dev Deploy tokens + pools, call initTreasury + seedPools. Returns wood token address.
packages/contracts/test/ClanWorld.t.sol:118:        world.seedPools(cfg);
packages/contracts/test/ClanWorld.t.sol:494:        // Advance until the arrival tick and four-tick action duration have both closed.
packages/contracts/test/ClanWorld.t.sol:526:        // Advance through travel and the four-tick mining duration.
packages/contracts/test/ClanWorld.t.sol:676:    function _submitAllClanMarketSells(uint32 clanId, address token) internal returns (uint256 count) {
packages/contracts/test/ClanWorld.t.sol:679:        return _submitFirstClanMarketSells(clanId, token, count);
packages/contracts/test/ClanWorld.t.sol:682:    function _submitFirstClanMarketSells(uint32 clanId, address token, uint256 count) internal returns (uint256) {
packages/contracts/test/ClanWorld.t.sol:690:                action: ActionType.MarketSell,
packages/contracts/test/ClanWorld.t.sol:723:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
packages/contracts/test/ClanWorld.t.sol:761:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 500e18);
packages/contracts/test/ClanWorld.t.sol:791:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
packages/contracts/test/ClanWorld.t.sol:805:        // Now the next heartbeat will close executeAtTick — that's when MarketActionFailed fires
packages/contracts/test/ClanWorld.t.sol:808:        emit IClanWorldEvents.MarketActionFailed(
packages/contracts/test/ClanWorld.t.sol:809:            clanId, csId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
packages/contracts/test/ClanWorld.t.sol:826:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
packages/contracts/test/ClanWorld.t.sol:852:        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
packages/contracts/test/ClanWorld.t.sol:860:        OrderResult[] memory r2 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 2e18, 0);
packages/contracts/test/ClanWorld.t.sol:876:        emit IClanWorldEvents.MarketActionFailed(clanId, csId, ActionType.MarketSell, StatusCode.ERR_INVALID_ACTION);
packages/contracts/test/ClanWorld.t.sol:904:            totalQueued += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
packages/contracts/test/ClanWorld.t.sol:910:            totalQueued += _submitAllClanMarketSells(distOneClans[i], woodAddr);
packages/contracts/test/ClanWorld.t.sol:959:        uint256 totalQueuedForTickTwo = _submitFirstClanMarketSells(nativeClanId, woodAddr, 3);
packages/contracts/test/ClanWorld.t.sol:961:            totalQueuedForTickTwo += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
packages/contracts/test/ClanWorld.t.sol:967:            totalQueuedForTickTwo += _submitAllClanMarketSells(distOneClans[i], woodAddr);
packages/contracts/test/ClanWorld.t.sol:971:            _submitMarketOrder(nativeClanId, nativeCsId, ActionType.MarketSell, woodAddr, 1e18, 0);
packages/contracts/test/ClanWorld.t.sol:999:            keccak256("ScheduledMarketActionExecuted(uint64,uint64,uint32,uint32,address,address,uint256,uint256)");
packages/contracts/test/ClanWorld.t.sol:1045:            action: ActionType.MarketSell,
packages/contracts/test/ClanWorld.t.sol:1060:            action: ActionType.MarketSell,
packages/contracts/test/ClanWorld.t.sol:1120:            action: ActionType.MarketSell,
packages/contracts/test/ClanWorld.t.sol:1134:            action: ActionType.MarketBuy,
packages/contracts/test/ClanWorld.t.sol:1178:            action: ActionType.MarketSell,
packages/contracts/test/ClanWorld.t.sol:1200:            action: ActionType.MarketSell,
packages/contracts/test/ClanWorld.t.sol:1688:        // Advance through travel and the four-tick mining duration.
packages/contracts/test/ClanWorld.t.sol:1896:        // Advance through travel and the four-tick mining duration WITHOUT calling settleClan.
packages/contracts/src/StubPool.sol:30:    /// @notice Called by ClanWorld at seedPools time to set initial reserves.
packages/contracts/src/ClanWorldStub.sol:114:    function seedPools(PoolSeedConfig calldata) external override {}
packages/contracts/src/IClanWorld.sol:142:    MarketBuy,
packages/contracts/src/IClanWorld.sol:143:    MarketSell,
packages/contracts/src/IClanWorld.sol:323:    ActionType action; // MarketBuy or MarketSell
packages/contracts/src/IClanWorld.sol:548:    event ImmediateMarketActionExecuted(
packages/contracts/src/IClanWorld.sol:557:    event ScheduledMarketActionExecuted(
packages/contracts/src/IClanWorld.sol:577:    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
packages/contracts/src/IClanWorld.sol:687:    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
packages/contracts/src/IClanWorld.sol:688:    function seedPools(PoolSeedConfig calldata cfg) external;

exec
/bin/bash -lc "git show --stat --oneline --decorate 9f93593 -- packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/src/StubPool.sol packages/contracts/src/MinimalERC20.sol packages/contracts/test/ClanWorld.t.sol | sed -n '1,120p'" in /home/claude/code/clan-world
 succeeded in 0ms:
9f93593 (origin/dev-phase-6-market) fix(contracts): Phase 6 cloud-review fix-round - duplicate enums, unused param, MAX_MARKET_ACTIONS, carry overflow (Closes #269) (#270)
 packages/contracts/src/ClanWorld.sol    | 50 ++++++++++++------------------
 packages/contracts/src/IClanWorld.sol   |  6 ++--
 packages/contracts/test/ClanWorld.t.sol | 55 ++++++++++++---------------------
 3 files changed, 41 insertions(+), 70 deletions(-)

exec
/bin/bash -lc 'git rev-parse --show-toplevel && git status --short && git rev-parse HEAD && git branch --show-current' in /home/claude/code/clan-world
 succeeded in 0ms:
/home/claude/code/omnipass-world/clan-world
?? docs/planning/gold-bridge-monorepo.zip
?? docs/reviews/pr153-review-claude-opus.md
?? docs/reviews/pr153-review-codex-5-3.md
?? docs/reviews/pr194-r2-codereview-codex-5-4.md
?? docs/reviews/pr194-r2-codereview-codex-5-5.md
?? docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md
?? docs/reviews/pr194-r2-codereview-opus-4-7.md
?? docs/reviews/pr194-r2-synthesis.md
?? docs/reviews/pr198-codereview-codex-5-4.md
?? docs/reviews/pr198-codereview-codex-5-5.md
?? docs/reviews/pr198-codereview-opus-4-7.md
?? docs/reviews/r2/
218f9020ecb0f4b8277ef59dd55de8e004404d80

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1160,1885p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1160	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1161	            Mission storage currentM = _missions[order.clansmanId];
  1162	            if (
  1163	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  1164	                    && currentM.targetClanId == defendTargetClanId
  1165	            ) {
  1166	                result.status = StatusCode.OK;
  1167	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1168	                result.missionNonce = currentM.nonce;
  1169	                return result;
  1170	            }
  1171	        }
  1172	
  1173	        bool isMarketAction = order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell;
  1174	
  1175	        // Cooldown check. Market orders may still fall back to the scheduled path;
  1176	        // only the immediate path requires the worker to be off cooldown.
  1177	        if (!isMarketAction && block.timestamp < cs.cooldownEndsAtTs) {
  1178	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
  1179	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1180	            result.missionNonce = cs.lastMissionNonce;
  1181	            return result;
  1182	        }
  1183	
  1184	        OrderCtx memory ctx;
  1185	        ctx.fromRegion = cs.currentRegion;
  1186	        ctx.gotoRegion = order.gotoRegion;
  1187	        ctx.targetClanId =
  1188	            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
  1189	
  1190	        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
  1191	        ctx.isNoop = order.action != ActionType.DefendBase
  1192	            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
  1193	        if (ctx.isNoop) {
  1194	            ctx.gotoRegion = ctx.fromRegion;
  1195	        }
  1196	
  1197	        // Validate target region (1-8 or 0=noop)
  1198	        if (ctx.gotoRegion > 8) {
  1199	            result.status = StatusCode.ERR_INVALID_REGION;
  1200	            return result;
  1201	        }
  1202	
  1203	        // Validate action
  1204	        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
  1205	        if (actionErr != StatusCode.OK) {
  1206	            result.status = actionErr;
  1207	            return result;
  1208	        }
  1209	
  1210	        if (
  1211	            isMarketAction && ctx.fromRegion == ClanWorldConstants.REGION_UNICORN_TOWN
  1212	                && cs.state == ClansmanState.WAITING && block.timestamp >= cs.cooldownEndsAtTs
  1213	        ) {
  1214	            ctx.newNonce = cs.lastMissionNonce + 1;
  1215	            cs.lastMissionNonce = ctx.newNonce;
  1216	
  1217	            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
  1218	
  1219	            if (marketStatus == StatusCode.OK) {
  1220	                cs.state = ClansmanState.WAITING;
  1221	                cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1222	            }
  1223	            _clearDefender(cs.clansmanId);
  1224	
  1225	            result.status = StatusCode.OK;
  1226	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1227	            result.missionNonce = ctx.newNonce;
  1228	            result.marketMode = MarketExecutionMode.Immediate;
  1229	            return result;
  1230	        }
  1231	
  1232	        // Capture existing mission state
  1233	        Mission storage existingM = _missions[order.clansmanId];
  1234	        ctx.wasActive = existingM.active;
  1235	        ctx.oldNonce = existingM.nonce;
  1236	
  1237	        // Compute travel from the clansman's current region to the order target.
  1238	        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
  1239	        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
  1240	
  1241	        // New nonce
  1242	        ctx.newNonce = cs.lastMissionNonce + 1;
  1243	        cs.lastMissionNonce = ctx.newNonce;
  1244	
  1245	        // Install mission via helper to keep stack shallow
  1246	        _installMission(existingM, order, cs, ctx);
  1247	
  1248	        // Update clansman state
  1249	        if (ctx.travelTicks > 0) {
  1250	            cs.state = ClansmanState.TRAVELING;
  1251	        } else {
  1252	            // NOOP / same-region: no traveling state per v4.3 §A
  1253	            cs.state = ClansmanState.ACTING;
  1254	            cs.currentRegion = ctx.gotoRegion;
  1255	        }
  1256	
  1257	        // Start cooldown
  1258	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1259	
  1260	        _clearDefender(cs.clansmanId);
  1261	
  1262	        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
  1263	            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
  1264	        }
  1265	
  1266	        if (ctx.wasActive) {
  1267	            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
  1268	        }
  1269	
  1270	        emit MissionAssigned(
  1271	            clanId,
  1272	            order.clansmanId,
  1273	            ctx.newNonce,
  1274	            order.action,
  1275	            ctx.fromRegion,
  1276	            ctx.gotoRegion,
  1277	            _world.currentTick,
  1278	            ctx.arrivalTick
  1279	        );
  1280	
  1281	        result.status = StatusCode.OK;
  1282	        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1283	        result.missionNonce = ctx.newNonce;
  1284	        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
  1285	        return result;
  1286	    }
  1287	
  1288	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  1289	        internal
  1290	    {
  1291	        m.active = true;
  1292	        m.nonce = ctx.newNonce;
  1293	        m.clansmanId = cs.clansmanId;
  1294	        m.submittedAtTick = _world.currentTick;
  1295	        m.executesAtTick = ctx.arrivalTick;
  1296	        m.settlesAtTick = order.action == ActionType.DefendBase
  1297	            ? type(uint64).max
  1298	            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
  1299	        m.startRegion = ctx.fromRegion;
  1300	        m.targetRegion = ctx.gotoRegion;
  1301	        m.action = order.action;
  1302	        m.startTick = _world.currentTick;
  1303	        m.arrivalTick = ctx.arrivalTick;
  1304	        m.actionStartTick = ctx.arrivalTick;
  1305	        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
  1306	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
  1307	            ? MarketExecutionMode.Scheduled
  1308	            : MarketExecutionMode.None;
  1309	        m.targetClanId = ctx.targetClanId;
  1310	        m.marketToken = order.marketToken;
  1311	        m.marketAmount = order.marketAmount;
  1312	        m.maxGoldIn = order.maxGoldIn;
  1313	
  1314	        if (m.marketMode == MarketExecutionMode.Scheduled) {
  1315	            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
  1316	        } else {
  1317	            delete _marketMissionCommitSequence[cs.clansmanId];
  1318	        }
  1319	    }
  1320	
  1321	    function _enqueueScheduledMarketAction(uint32 clanId, uint32 clansmanId, Mission storage m) internal {
  1322	        uint64 executeAtTick = m.settlesAtTick;
  1323	        ScheduledMarketAction memory sma = ScheduledMarketAction({
  1324	            executeAtTick: executeAtTick,
  1325	            commitSequence: _marketMissionCommitSequence[clansmanId],
  1326	            missionNonce: m.nonce,
  1327	            clanId: clanId,
  1328	            clansmanId: clansmanId,
  1329	            action: m.action,
  1330	            marketToken: m.marketToken,
  1331	            marketAmount: m.marketAmount,
  1332	            maxGoldIn: m.maxGoldIn
  1333	        });
  1334	        _scheduledMarketActions[executeAtTick].push(sma);
  1335	        emit ScheduledMarketActionCommitted(
  1336	            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
  1337	        );
  1338	    }
  1339	
  1340	    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
  1341	        if (_clansmanDefendingRegion[clansmanId] == region) return;
  1342	        _clearDefender(clansmanId);
  1343	
  1344	        if (_defenderCountByRegionClan[region][clanId] == 0) {
  1345	            _defendingClansByRegion[region].push(clanId);
  1346	        }
  1347	        _defenderCountByRegionClan[region][clanId]++;
  1348	        _clansmanDefendingRegion[clansmanId] = region;
  1349	    }
  1350	
  1351	    function _clearDefender(uint32 clansmanId) internal {
  1352	        uint8 region = _clansmanDefendingRegion[clansmanId];
  1353	        if (region == 0) return;
  1354	
  1355	        uint32 clanId = _clansmen[clansmanId].clanId;
  1356	        uint256 count = _defenderCountByRegionClan[region][clanId];
  1357	        if (count > 1) {
  1358	            _defenderCountByRegionClan[region][clanId] = count - 1;
  1359	        } else {
  1360	            delete _defenderCountByRegionClan[region][clanId];
  1361	            uint32[] storage clans = _defendingClansByRegion[region];
  1362	            for (uint256 i = 0; i < clans.length; i++) {
  1363	                if (clans[i] == clanId) {
  1364	                    clans[i] = clans[clans.length - 1];
  1365	                    clans.pop();
  1366	                    break;
  1367	                }
  1368	            }
  1369	        }
  1370	
  1371	        delete _clansmanDefendingRegion[clansmanId];
  1372	    }
  1373	
  1374	    // =========================================================================
  1375	    // MARKET EXECUTION (Phase 2)
  1376	    // =========================================================================
  1377	
  1378	    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
  1379	    function _executeScheduledMarketActions(uint64 tick) internal {
  1380	        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
  1381	        uint256 len = actions.length;
  1382	        if (len == 0) return;
  1383	
  1384	        _sortScheduledMarketActionsByCommitSequence(actions);
  1385	
  1386	        for (uint256 i = 0; i < len; i++) {
  1387	            ScheduledMarketAction storage sma = actions[i];
  1388	
  1389	            // Validate clansman still belongs to the clan
  1390	            Clansman storage cs = _clansmen[sma.clansmanId];
  1391	            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
  1392	                _emitMarketActionFailed(
  1393	                    sma.clanId,
  1394	                    sma.clansmanId,
  1395	                    sma.action,
  1396	                    MarketExecutionMode.Scheduled,
  1397	                    StatusCode.ERR_INVALID_CLANSMAN,
  1398	                    tick
  1399	                );
  1400	                continue;
  1401	            }
  1402	
  1403	            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
  1404	            // Note: _completeMission sets m.active=false during settlement (by design), so we
  1405	            // cannot use m.active as a validity signal here — check action type and nonce.
  1406	            Mission storage m = _missions[sma.clansmanId];
  1407	            if (m.action != sma.action || m.nonce != sma.missionNonce) {
  1408	                _emitMarketActionFailed(
  1409	                    sma.clanId,
  1410	                    sma.clansmanId,
  1411	                    sma.action,
  1412	                    MarketExecutionMode.Scheduled,
  1413	                    StatusCode.ERR_INVALID_ACTION,
  1414	                    tick
  1415	                );
  1416	                continue;
  1417	            }
  1418	
  1419	            if (sma.action == ActionType.MarketSell) {
  1420	                try this._executeMarketSellExternal(
  1421	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
  1422	                ) returns (
  1423	                    StatusCode marketStatus
  1424	                ) {
  1425	                    marketStatus;
  1426	                } catch {
  1427	                    _handleMarketFailure(
  1428	                        sma.clanId,
  1429	                        sma.clansmanId,
  1430	                        sma.action,
  1431	                        MarketExecutionMode.Scheduled,
  1432	                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1433	                        tick
  1434	                    );
  1435	                }
  1436	            } else if (sma.action == ActionType.MarketBuy) {
  1437	                try this._executeMarketBuyExternal(
  1438	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
  1439	                ) returns (
  1440	                    StatusCode marketStatus
  1441	                ) {
  1442	                    marketStatus;
  1443	                } catch {
  1444	                    _handleMarketFailure(
  1445	                        sma.clanId,
  1446	                        sma.clansmanId,
  1447	                        sma.action,
  1448	                        MarketExecutionMode.Scheduled,
  1449	                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1450	                        tick
  1451	                    );
  1452	                }
  1453	            }
  1454	        }
  1455	
  1456	        delete _scheduledMarketActions[tick];
  1457	    }
  1458	
  1459	    function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
  1460	        for (uint256 i = 1; i < actions.length; i++) {
  1461	            ScheduledMarketAction memory key = actions[i];
  1462	            uint256 j = i;
  1463	            while (j > 0 && actions[j - 1].commitSequence > key.commitSequence) {
  1464	                actions[j] = actions[j - 1];
  1465	                j--;
  1466	            }
  1467	            actions[j] = key;
  1468	        }
  1469	    }
  1470	
  1471	    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
  1472	    function _executeMarketSellExternal(
  1473	        uint64 closedTick,
  1474	        uint32 clanId,
  1475	        uint32 clansmanId,
  1476	        address token,
  1477	        uint256 amount
  1478	    ) external returns (StatusCode) {
  1479	        require(msg.sender == address(this), "ClanWorld: internal only");
  1480	        return _executeMarketSell(closedTick, clanId, clansmanId, token, amount);
  1481	    }
  1482	
  1483	    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
  1484	    function _executeMarketBuyExternal(
  1485	        uint64 closedTick,
  1486	        uint32 clanId,
  1487	        uint32 clansmanId,
  1488	        address token,
  1489	        uint256 amountOut,
  1490	        uint256 maxGoldIn
  1491	    ) external returns (StatusCode) {
  1492	        require(msg.sender == address(this), "ClanWorld: internal only");
  1493	        return _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn);
  1494	    }
  1495	
  1496	    /// @dev Map a resource token address to its pool address.
  1497	    function _poolFor(address token) internal view returns (address pool) {
  1498	        if (token == _treasury.woodToken) return _treasury.woodGoldPool;
  1499	        if (token == _treasury.ironToken) return _treasury.ironGoldPool;
  1500	        if (token == _treasury.wheatToken) return _treasury.wheatGoldPool;
  1501	        if (token == _treasury.fishToken) return _treasury.fishGoldPool;
  1502	        return address(0);
  1503	    }
  1504	
  1505	    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
  1506	    function _marketResourceForToken(address token) internal view returns (uint8) {
  1507	        if (token == _treasury.woodToken) return uint8(ResourceType.Wood);
  1508	        if (token == _treasury.ironToken) return uint8(ResourceType.Iron);
  1509	        if (token == _treasury.wheatToken) return uint8(ResourceType.Wheat);
  1510	        if (token == _treasury.fishToken) return uint8(ResourceType.Fish);
  1511	        if (token == _treasury.goldToken) return ClanWorldConstants.RESOURCE_GOLD;
  1512	        revert("ClanWorld: invalid market resource");
  1513	    }
  1514	
  1515	    function _emitMarketActionFailed(
  1516	        uint32 clanId,
  1517	        uint32 clansmanId,
  1518	        ActionType action,
  1519	        MarketExecutionMode mode,
  1520	        StatusCode reason,
  1521	        uint64 tick
  1522	    ) internal {
  1523	        emit MarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
  1524	    }
  1525	
  1526	    /// @dev Add an amount of a resource token to the clan vault.
  1527	    function _addToVault(Clan storage clan, address token, uint256 amount) internal {
  1528	        if (token == _treasury.woodToken) {
  1529	            clan.vaultWood += amount;
  1530	            return;
  1531	        }
  1532	        if (token == _treasury.ironToken) {
  1533	            clan.vaultIron += amount;
  1534	            return;
  1535	        }
  1536	        if (token == _treasury.wheatToken) {
  1537	            clan.vaultWheat += amount;
  1538	            return;
  1539	        }
  1540	        if (token == _treasury.fishToken) {
  1541	            clan.vaultFish += amount;
  1542	            return;
  1543	        }
  1544	    }
  1545	
  1546	    /// @dev Deduct an amount of a resource token from the clan vault. Returns false if insufficient.
  1547	    function _deductFromVault(Clan storage clan, address token, uint256 amount) internal returns (bool) {
  1548	        if (token == _treasury.woodToken) {
  1549	            if (clan.vaultWood < amount) return false;
  1550	            clan.vaultWood -= amount;
  1551	            return true;
  1552	        }
  1553	        if (token == _treasury.ironToken) {
  1554	            if (clan.vaultIron < amount) return false;
  1555	            clan.vaultIron -= amount;
  1556	            return true;
  1557	        }
  1558	        if (token == _treasury.wheatToken) {
  1559	            if (clan.vaultWheat < amount) return false;
  1560	            clan.vaultWheat -= amount;
  1561	            return true;
  1562	        }
  1563	        if (token == _treasury.fishToken) {
  1564	            if (clan.vaultFish < amount) return false;
  1565	            clan.vaultFish -= amount;
  1566	            return true;
  1567	        }
  1568	        return false;
  1569	    }
  1570	
  1571	    /// @dev Check a clan vault balance without mutating storage.
  1572	    function _hasVaultBalance(Clan storage clan, address token, uint256 amount) internal view returns (bool) {
  1573	        if (token == _treasury.woodToken) return clan.vaultWood >= amount;
  1574	        if (token == _treasury.ironToken) return clan.vaultIron >= amount;
  1575	        if (token == _treasury.wheatToken) return clan.vaultWheat >= amount;
  1576	        if (token == _treasury.fishToken) return clan.vaultFish >= amount;
  1577	        return false;
  1578	    }
  1579	
  1580	    function _handleMarketFailure(
  1581	        uint32 clanId,
  1582	        uint32 clansmanId,
  1583	        ActionType action,
  1584	        MarketExecutionMode mode,
  1585	        StatusCode reason,
  1586	        uint64 tick
  1587	    ) internal returns (StatusCode) {
  1588	        Clansman storage cs = _clansmen[clansmanId];
  1589	        if (cs.clansmanId != 0 && cs.state != ClansmanState.DEAD) {
  1590	            cs.state = ClansmanState.WAITING;
  1591	            cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1592	        }
  1593	        _emitMarketActionFailed(clanId, clansmanId, action, mode, reason, tick);
  1594	        return reason;
  1595	    }
  1596	
  1597	    function _remainingCarryForToken(Clansman storage cs, address token) internal view returns (uint256) {
  1598	        uint256 carried;
  1599	        uint256 cap;
  1600	        if (token == _treasury.woodToken) {
  1601	            carried = cs.carryWood;
  1602	            cap = ClanWorldConstants.WOOD_CAP;
  1603	        } else if (token == _treasury.ironToken) {
  1604	            carried = cs.carryIron;
  1605	            cap = ClanWorldConstants.IRON_CAP;
  1606	        } else if (token == _treasury.wheatToken) {
  1607	            carried = cs.carryWheat;
  1608	            cap = ClanWorldConstants.WHEAT_CAP;
  1609	        } else if (token == _treasury.fishToken) {
  1610	            carried = cs.carryFish;
  1611	            cap = ClanWorldConstants.FISH_CAP;
  1612	        } else {
  1613	            return 0;
  1614	        }
  1615	
  1616	        if (carried >= cap) return 0;
  1617	        return cap - carried;
  1618	    }
  1619	
  1620	    function _executeImmediateMarket(uint32 clanId, ClanOrder calldata order, uint32 clansmanId)
  1621	        internal
  1622	        returns (StatusCode)
  1623	    {
  1624	        if (order.action == ActionType.MarketSell) {
  1625	            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
  1626	        }
  1627	        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
  1628	    }
  1629	
  1630	    function _executeImmediateMarketSell(uint32 clanId, uint32 clansmanId, address token, uint256 amount)
  1631	        internal
  1632	        returns (StatusCode)
  1633	    {
  1634	        if (!_treasury.poolsSeeded) {
  1635	            return _handleMarketFailure(
  1636	                clanId,
  1637	                clansmanId,
  1638	                ActionType.MarketSell,
  1639	                MarketExecutionMode.Immediate,
  1640	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1641	                _world.currentTick
  1642	            );
  1643	        }
  1644	        address poolAddr = _poolFor(token);
  1645	        if (poolAddr == address(0)) {
  1646	            return _handleMarketFailure(
  1647	                clanId,
  1648	                clansmanId,
  1649	                ActionType.MarketSell,
  1650	                MarketExecutionMode.Immediate,
  1651	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1652	                _world.currentTick
  1653	            );
  1654	        }
  1655	
  1656	        Clan storage clan = _clans[clanId];
  1657	        if (!_hasVaultBalance(clan, token, amount)) {
  1658	            return _handleMarketFailure(
  1659	                clanId,
  1660	                clansmanId,
  1661	                ActionType.MarketSell,
  1662	                MarketExecutionMode.Immediate,
  1663	                StatusCode.ERR_MISSING_RESOURCES,
  1664	                _world.currentTick
  1665	            );
  1666	        }
  1667	
  1668	        try StubPool(poolAddr).swapExactInForOut(amount, 1) returns (uint256 goldOut) {
  1669	            _deductFromVault(clan, token, amount);
  1670	            clan.goldBalance += goldOut;
  1671	            emit ImmediateMarketActionExecuted(
  1672	                clanId,
  1673	                clansmanId,
  1674	                ActionType.MarketSell,
  1675	                _marketResourceForToken(token),
  1676	                amount,
  1677	                ClanWorldConstants.RESOURCE_GOLD,
  1678	                goldOut,
  1679	                _world.currentTick
  1680	            );
  1681	            return StatusCode.OK;
  1682	        } catch {
  1683	            return _handleMarketFailure(
  1684	                clanId,
  1685	                clansmanId,
  1686	                ActionType.MarketSell,
  1687	                MarketExecutionMode.Immediate,
  1688	                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1689	                _world.currentTick
  1690	            );
  1691	        }
  1692	    }
  1693	
  1694	    function _executeImmediateMarketBuy(
  1695	        uint32 clanId,
  1696	        uint32 clansmanId,
  1697	        address token,
  1698	        uint256 amountOut,
  1699	        uint256 maxGoldIn
  1700	    ) internal returns (StatusCode) {
  1701	        if (!_treasury.poolsSeeded) {
  1702	            return _handleMarketFailure(
  1703	                clanId,
  1704	                clansmanId,
  1705	                ActionType.MarketBuy,
  1706	                MarketExecutionMode.Immediate,
  1707	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1708	                _world.currentTick
  1709	            );
  1710	        }
  1711	        address poolAddr = _poolFor(token);
  1712	        if (poolAddr == address(0)) {
  1713	            return _handleMarketFailure(
  1714	                clanId,
  1715	                clansmanId,
  1716	                ActionType.MarketBuy,
  1717	                MarketExecutionMode.Immediate,
  1718	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1719	                _world.currentTick
  1720	            );
  1721	        }
  1722	
  1723	        Clansman storage cs = _clansmen[clansmanId];
  1724	        if (amountOut > _remainingCarryForToken(cs, token)) {
  1725	            return _handleMarketFailure(
  1726	                clanId,
  1727	                clansmanId,
  1728	                ActionType.MarketBuy,
  1729	                MarketExecutionMode.Immediate,
  1730	                StatusCode.ERR_CARRY_FULL,
  1731	                _world.currentTick
  1732	            );
  1733	        }
  1734	
  1735	        uint256 goldIn;
  1736	        try StubPool(poolAddr).getAmountInForExactOut(amountOut) returns (uint256 quotedGoldIn) {
  1737	            goldIn = quotedGoldIn;
  1738	        } catch {
  1739	            return _handleMarketFailure(
  1740	                clanId,
  1741	                clansmanId,
  1742	                ActionType.MarketBuy,
  1743	                MarketExecutionMode.Immediate,
  1744	                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1745	                _world.currentTick
  1746	            );
  1747	        }
  1748	
  1749	        Clan storage clan = _clans[clanId];
  1750	        if (goldIn > maxGoldIn) {
  1751	            return _handleMarketFailure(
  1752	                clanId,
  1753	                clansmanId,
  1754	                ActionType.MarketBuy,
  1755	                MarketExecutionMode.Immediate,
  1756	                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
  1757	                _world.currentTick
  1758	            );
  1759	        }
  1760	        if (clan.goldBalance < goldIn) {
  1761	            return _handleMarketFailure(
  1762	                clanId,
  1763	                clansmanId,
  1764	                ActionType.MarketBuy,
  1765	                MarketExecutionMode.Immediate,
  1766	                StatusCode.ERR_NOT_ENOUGH_GOLD,
  1767	                _world.currentTick
  1768	            );
  1769	        }
  1770	
  1771	        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 actualGoldIn) {
  1772	            clan.goldBalance -= actualGoldIn;
  1773	            _addToVault(clan, token, amountOut);
  1774	            emit ImmediateMarketActionExecuted(
  1775	                clanId,
  1776	                clansmanId,
  1777	                ActionType.MarketBuy,
  1778	                ClanWorldConstants.RESOURCE_GOLD,
  1779	                actualGoldIn,
  1780	                _marketResourceForToken(token),
  1781	                amountOut,
  1782	                _world.currentTick
  1783	            );
  1784	            return StatusCode.OK;
  1785	        } catch {
  1786	            return _handleMarketFailure(
  1787	                clanId,
  1788	                clansmanId,
  1789	                ActionType.MarketBuy,
  1790	                MarketExecutionMode.Immediate,
  1791	                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1792	                _world.currentTick
  1793	            );
  1794	        }
  1795	    }
  1796	
  1797	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
  1798	    function _executeMarketSell(uint64 closedTick, uint32 clanId, uint32 clansmanId, address token, uint256 amount)
  1799	        internal
  1800	        returns (StatusCode)
  1801	    {
  1802	        if (!_treasury.poolsSeeded) {
  1803	            return _handleMarketFailure(
  1804	                clanId,
  1805	                clansmanId,
  1806	                ActionType.MarketSell,
  1807	                MarketExecutionMode.Scheduled,
  1808	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1809	                closedTick
  1810	            );
  1811	        }
  1812	        address poolAddr = _poolFor(token);
  1813	        if (poolAddr == address(0)) {
  1814	            return _handleMarketFailure(
  1815	                clanId,
  1816	                clansmanId,
  1817	                ActionType.MarketSell,
  1818	                MarketExecutionMode.Scheduled,
  1819	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1820	                closedTick
  1821	            );
  1822	        }
  1823	
  1824	        Clan storage clan = _clans[clanId];
  1825	        if (!_deductFromVault(clan, token, amount)) {
  1826	            return _handleMarketFailure(
  1827	                clanId,
  1828	                clansmanId,
  1829	                ActionType.MarketSell,
  1830	                MarketExecutionMode.Scheduled,
  1831	                StatusCode.ERR_MISSING_RESOURCES,
  1832	                closedTick
  1833	            );
  1834	        }
  1835	
  1836	        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
  1837	        clan.goldBalance += goldOut;
  1838	
  1839	        emit ScheduledMarketActionExecuted(
  1840	            clanId,
  1841	            clansmanId,
  1842	            ActionType.MarketSell,
  1843	            _marketResourceForToken(token),
  1844	            amount,
  1845	            ClanWorldConstants.RESOURCE_GOLD,
  1846	            goldOut,
  1847	            closedTick
  1848	        );
  1849	        return StatusCode.OK;
  1850	    }
  1851	
  1852	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
  1853	    function _executeMarketBuy(
  1854	        uint64 closedTick,
  1855	        uint32 clanId,
  1856	        uint32 clansmanId,
  1857	        address token,
  1858	        uint256 amountOut,
  1859	        uint256 maxGoldIn
  1860	    ) internal returns (StatusCode) {
  1861	        if (!_treasury.poolsSeeded) {
  1862	            return _handleMarketFailure(
  1863	                clanId,
  1864	                clansmanId,
  1865	                ActionType.MarketBuy,
  1866	                MarketExecutionMode.Scheduled,
  1867	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1868	                closedTick
  1869	            );
  1870	        }
  1871	        address poolAddr = _poolFor(token);
  1872	        if (poolAddr == address(0)) {
  1873	            return _handleMarketFailure(
  1874	                clanId,
  1875	                clansmanId,
  1876	                ActionType.MarketBuy,
  1877	                MarketExecutionMode.Scheduled,
  1878	                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
  1879	                closedTick
  1880	            );
  1881	        }
  1882	
  1883	        Clansman storage cs = _clansmen[clansmanId];
  1884	        if (amountOut > _remainingCarryForToken(cs, token)) {
  1885	            return _handleMarketFailure(

codex
Good, the target commit exists in git, so I can inspect exact head files directly with `git show`. I’ve found one suspicious path already around immediate failures and return status; I’m checking whether that’s intentional semantics or a real state/reporting bug.
exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1880,2205p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1880	            );
  1881	        }
  1882	
  1883	        Clansman storage cs = _clansmen[clansmanId];
  1884	        if (amountOut > _remainingCarryForToken(cs, token)) {
  1885	            return _handleMarketFailure(
  1886	                clanId,
  1887	                clansmanId,
  1888	                ActionType.MarketBuy,
  1889	                MarketExecutionMode.Scheduled,
  1890	                StatusCode.ERR_CARRY_FULL,
  1891	                closedTick
  1892	            );
  1893	        }
  1894	
  1895	        uint256 goldIn;
  1896	        try StubPool(poolAddr).quoteBuy(amountOut) returns (uint256 quotedGoldIn) {
  1897	            goldIn = quotedGoldIn;
  1898	        } catch {
  1899	            return _handleMarketFailure(
  1900	                clanId,
  1901	                clansmanId,
  1902	                ActionType.MarketBuy,
  1903	                MarketExecutionMode.Scheduled,
  1904	                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1905	                closedTick
  1906	            );
  1907	        }
  1908	
  1909	        Clan storage clan = _clans[clanId];
  1910	
  1911	        if (goldIn > maxGoldIn) {
  1912	            return _handleMarketFailure(
  1913	                clanId,
  1914	                clansmanId,
  1915	                ActionType.MarketBuy,
  1916	                MarketExecutionMode.Scheduled,
  1917	                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
  1918	                closedTick
  1919	            );
  1920	        }
  1921	        if (clan.goldBalance < goldIn) {
  1922	            return _handleMarketFailure(
  1923	                clanId,
  1924	                clansmanId,
  1925	                ActionType.MarketBuy,
  1926	                MarketExecutionMode.Scheduled,
  1927	                StatusCode.ERR_NOT_ENOUGH_GOLD,
  1928	                closedTick
  1929	            );
  1930	        }
  1931	
  1932	        uint256 actualGoldIn;
  1933	        try StubPool(poolAddr).swapExactOutForInWithMaxIn(amountOut, maxGoldIn) returns (uint256 spentGold) {
  1934	            actualGoldIn = spentGold;
  1935	        } catch {
  1936	            return _handleMarketFailure(
  1937	                clanId,
  1938	                clansmanId,
  1939	                ActionType.MarketBuy,
  1940	                MarketExecutionMode.Scheduled,
  1941	                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1942	                closedTick
  1943	            );
  1944	        }
  1945	
  1946	        clan.goldBalance -= actualGoldIn;
  1947	        _addToVault(clan, token, amountOut);
  1948	
  1949	        emit ScheduledMarketActionExecuted(
  1950	            clanId,
  1951	            clansmanId,
  1952	            ActionType.MarketBuy,
  1953	            ClanWorldConstants.RESOURCE_GOLD,
  1954	            actualGoldIn,
  1955	            _marketResourceForToken(token),
  1956	            amountOut,
  1957	            closedTick
  1958	        );
  1959	        return StatusCode.OK;
  1960	    }
  1961	
  1962	    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
  1963	        internal
  1964	        view
  1965	        returns (StatusCode)
  1966	    {
  1967	        ActionType action = order.action;
  1968	
  1969	        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
  1970	
  1971	        // DepositResources: must go to homebase
  1972	        if (action == ActionType.DepositResources) {
  1973	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1974	        }
  1975	
  1976	        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
  1977	        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
  1978	        {
  1979	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  1980	        }
  1981	
  1982	        // ChopWood: must go to Forest
  1983	        if (action == ActionType.ChopWood) {
  1984	            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
  1985	        }
  1986	        // MineIron: must go to Mountains
  1987	        if (action == ActionType.MineIron) {
  1988	            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
  1989	        }
  1990	        // FishDocks: must go to WestDocks or EastDocks
  1991	        if (action == ActionType.FishDocks) {
  1992	            if (
  1993	                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
  1994	            ) {
  1995	                return StatusCode.ERR_INVALID_REGION;
  1996	            }
  1997	        }
  1998	        // FishDeepSea: must go to DeepSea
  1999	        if (action == ActionType.FishDeepSea) {
  2000	            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
  2001	        }
  2002	        // HarvestWheat: must go to WestFarms or EastFarms
  2003	        if (action == ActionType.HarvestWheat) {
  2004	            if (
  2005	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  2006	            ) {
  2007	                return StatusCode.ERR_INVALID_REGION;
  2008	            }
  2009	        }
  2010	
  2011	        if (action == ActionType.DefendBase) {
  2012	            return _validateDefendBaseOrder(clan, order, gotoRegion);
  2013	        }
  2014	
  2015	        // MarketBuy/MarketSell: must target Unicorn Town
  2016	        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  2017	            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  2018	            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
  2019	                return StatusCode.ERR_INVALID_REGION;
  2020	            }
  2021	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
  2022	            // Validate token is a supported resource token (not gold itself)
  2023	            address tok = order.marketToken;
  2024	            if (tok == address(0) || tok == _treasury.goldToken) {
  2025	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  2026	            }
  2027	            if (
  2028	                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
  2029	                    && tok != _treasury.fishToken
  2030	            ) {
  2031	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  2032	            }
  2033	            // Over-capacity buys are rejected at submission; no partial fills or overflow refunds.
  2034	            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
  2035	                return StatusCode.ERR_CARRY_FULL;
  2036	            }
  2037	            // Immediate market orders execute during submitClanOrders when the
  2038	            // worker is waiting in Unicorn Town and off cooldown. Other valid
  2039	            // market orders enqueue when the scheduled mission resolves.
  2040	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
  2041	                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
  2042	            }
  2043	        }
  2044	
  2045	        cs; // suppress unused warning
  2046	        return StatusCode.OK;
  2047	    }
  2048	
  2049	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  2050	        internal
  2051	        view
  2052	        returns (StatusCode)
  2053	    {
  2054	        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  2055	        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
  2056	        return StatusCode.OK;
  2057	    }
  2058	
  2059	    // =========================================================================
  2060	    // TREASURY / POOL SEEDING
  2061	    // =========================================================================
  2062	
  2063	    /// @notice One-time treasury initialization: register token and pool addresses.
  2064	    ///         Must be called before seedPools. Callable only once.
  2065	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  2066	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  2067	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  2068	
  2069	        _treasury.woodToken = tokens[0];
  2070	        _treasury.ironToken = tokens[1];
  2071	        _treasury.wheatToken = tokens[2];
  2072	        _treasury.fishToken = tokens[3];
  2073	        _treasury.goldToken = tokens[4];
  2074	        _treasury.blueprintToken = tokens[5];
  2075	
  2076	        _treasury.woodGoldPool = pools[0];
  2077	        _treasury.wheatGoldPool = pools[1];
  2078	        _treasury.fishGoldPool = pools[2];
  2079	        _treasury.ironGoldPool = pools[3];
  2080	    }
  2081	
  2082	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  2083	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  2084	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  2085	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  2086	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  2087	
  2088	        _transferSeed(_treasury.woodToken, _treasury.woodGoldPool, cfg.woodSeed);
  2089	        _transferSeed(_treasury.goldToken, _treasury.woodGoldPool, cfg.goldSeedForWood);
  2090	        _transferSeed(_treasury.wheatToken, _treasury.wheatGoldPool, cfg.wheatSeed);
  2091	        _transferSeed(_treasury.goldToken, _treasury.wheatGoldPool, cfg.goldSeedForWheat);
  2092	        _transferSeed(_treasury.fishToken, _treasury.fishGoldPool, cfg.fishSeed);
  2093	        _transferSeed(_treasury.goldToken, _treasury.fishGoldPool, cfg.goldSeedForFish);
  2094	        _transferSeed(_treasury.ironToken, _treasury.ironGoldPool, cfg.ironSeed);
  2095	        _transferSeed(_treasury.goldToken, _treasury.ironGoldPool, cfg.goldSeedForIron);
  2096	
  2097	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  2098	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  2099	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  2100	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  2101	
  2102	        _treasury.poolsSeeded = true;
  2103	
  2104	        emit PoolsSeeded(
  2105	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  2106	        );
  2107	    }
  2108	
  2109	    function _transferSeed(address token, address pool, uint256 amount) internal {
  2110	        require(token != address(0) && pool != address(0), "ClanWorld: treasury not init");
  2111	        require(MinimalERC20(token).transferFrom(msg.sender, pool, amount), "ClanWorld: seed transfer failed");
  2112	    }
  2113	
  2114	    // =========================================================================
  2115	    // OTC TRANSFERS
  2116	    // =========================================================================
  2117	
  2118	    function transferGold(uint32, uint32, uint256) external pure override {
  2119	        revert("OTC transfers not implemented");
  2120	    }
  2121	
  2122	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  2123	        revert("OTC transfers not implemented");
  2124	    }
  2125	
  2126	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  2127	        revert("OTC transfers not implemented");
  2128	    }
  2129	
  2130	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  2131	        external
  2132	        pure
  2133	        override
  2134	    {
  2135	        revert("OTC transfers not implemented");
  2136	    }
  2137	
  2138	    // =========================================================================
  2139	    // RAW READ GETTERS
  2140	    // =========================================================================
  2141	
  2142	    function getWorldState() external view override returns (WorldState memory) {
  2143	        return _world;
  2144	    }
  2145	
  2146	    function getTreasuryState() external view override returns (TreasuryState memory) {
  2147	        return _treasury;
  2148	    }
  2149	
  2150	    function getResourceToken(uint8 resourceType) external view override returns (address) {
  2151	        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodToken;
  2152	        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironToken;
  2153	        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatToken;
  2154	        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishToken;
  2155	        revert("ClanWorld: invalid resource");
  2156	    }
  2157	
  2158	    function getPool(uint8 resourceType) external view override returns (address) {
  2159	        return _poolForResourceType(resourceType);
  2160	    }
  2161	
  2162	    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {
  2163	        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
  2164	    }
  2165	
  2166	    function _poolForResourceType(uint8 resourceType) internal view returns (address) {
  2167	        if (resourceType == uint8(ResourceType.Wood)) return _treasury.woodGoldPool;
  2168	        if (resourceType == uint8(ResourceType.Iron)) return _treasury.ironGoldPool;
  2169	        if (resourceType == uint8(ResourceType.Wheat)) return _treasury.wheatGoldPool;
  2170	        if (resourceType == uint8(ResourceType.Fish)) return _treasury.fishGoldPool;
  2171	        revert("ClanWorld: invalid resource");
  2172	    }
  2173	
  2174	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  2175	        return _clans[clanId];
  2176	    }
  2177	
  2178	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  2179	        return _clansmen[clansmanId];
  2180	    }
  2181	
  2182	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  2183	        return _missions[clansmanId];
  2184	    }
  2185	
  2186	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  2187	        external
  2188	        view
  2189	        override
  2190	        returns (uint64 submitted, uint64 executes, uint64 settles)
  2191	    {
  2192	        Mission memory m = _missions[clansmanId];
  2193	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  2194	            return (0, 0, 0);
  2195	        }
  2196	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  2197	    }
  2198	
  2199	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  2200	        if (
  2201	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  2202	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  2203	        ) {
  2204	            return 4;
  2205	        }

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '250,690p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   250	            if (curr == dst) break;
   251	            for (uint256 ni = 0; ni < 3; ni++) {
   252	                uint8 nb = adj[curr][ni];
   253	                if (nb == 0) break;
   254	                if (!visited[nb]) {
   255	                    visited[nb] = true;
   256	                    parent[nb] = curr;
   257	                    queue[tail++] = nb;
   258	                }
   259	            }
   260	        }
   261	
   262	        // Reconstruct path backwards
   263	        uint8[8] memory path;
   264	        uint256 pathLen;
   265	        uint8 cur = dst;
   266	        while (cur != src) {
   267	            path[pathLen++] = cur;
   268	            cur = parent[cur];
   269	        }
   270	        path[pathLen++] = src;
   271	
   272	        // Reverse into result
   273	        bytes8 packed;
   274	        uint64 byteShift = 56;
   275	        for (uint256 i = pathLen; i > 0; i--) {
   276	            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
   277	            if (byteShift >= 8) byteShift -= 8;
   278	        }
   279	        return packed;
   280	    }
   281	
   282	    function _travelTicks(uint8 fromRegion, uint8 toRegion) private pure returns (uint8) {
   283	        if (fromRegion == 0 || toRegion == 0) return 0; // NOOP region
   284	        if (fromRegion == toRegion) return 0;
   285	        if (fromRegion > 8 || toRegion > 8) return 0;
   286	        return _distMatrix(fromRegion, toRegion);
   287	    }
   288	
   289	    function _addTicksClamped(uint64 tick, uint64 delta) private pure returns (uint64) {
   290	        if (type(uint64).max - tick < delta) return type(uint64).max;
   291	        return tick + delta;
   292	    }
   293	
   294	    // =========================================================================
   295	    // INTERNAL SETTLEMENT
   296	    // =========================================================================
   297	
   298	    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
   299	    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
   300	    function _settleMissionForClansman(
   301	        Clan storage clan,
   302	        Clansman storage cs,
   303	        uint32 clanId,
   304	        uint64 fromTick,
   305	        uint64 toTick
   306	    ) internal {
   307	        Mission storage m = _missions[cs.clansmanId];
   308	
   309	        // Path 6: dead clansman — invalidate active mission if any
   310	        if (cs.state == ClansmanState.DEAD) {
   311	            if (m.active) {
   312	                if (m.action == ActionType.DefendBase) {
   313	                    _clearDefender(cs.clansmanId);
   314	                }
   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
   316	            }
   317	            return;
   318	        }
   319	
   320	        if (!m.active) return; // no active mission — nothing to settle
   321	
   322	        bytes32 tickSeed;
   323	        for (uint64 tick = fromTick; tick < toTick; tick++) {
   324	            tickSeed = _tickSeeds[tick];
   325	
   326	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   327	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   328	                cs.state = ClansmanState.ACTING;
   329	                cs.currentRegion = m.targetRegion;
   330	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   331	
   332	                if (m.action == ActionType.DefendBase) {
   333	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   334	                }
   335	            }
   336	
   337	            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
   338	
   339	            // Path 3: ACTING at/past settlesAtTick → resolve
   340	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
   341	                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   342	                if (m.active && getActionDuration(m.action) > 0) {
   343	                    _completeMission(cs, m);
   344	                }
   345	            }
   346	
   347	            // If mission completed during this tick, stop iterating
   348	            if (!m.active) break;
   349	        }
   350	    }
   351	
   352	    /// @dev Lazy settlement of a clan forward to currentTick.
   353	    ///      Mutates storage. Called before order submission and by public settleClan().
   354	    function _settleClan(uint32 clanId) internal {
   355	        Clan storage clan = _clans[clanId];
   356	        if (clan.clanId == 0) return;
   357	
   358	        uint64 curTick = _world.currentTick;
   359	        uint64 fromTick = clan.lastSettledTick;
   360	        if (fromTick >= curTick) return;
   361	
   362	        // Cap ticks settled per call to prevent block gas limit issues
   363	        uint64 maxSettleTicks = 200;
   364	        if (curTick > fromTick + maxSettleTicks) {
   365	            curTick = fromTick + maxSettleTicks;
   366	        }
   367	
   368	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   369	
   370	        // Settle tick by tick from fromTick to curTick - 1
   371	        // (curTick is still open; we settle through the last closed tick)
   372	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   373	            // 1. Apply upkeep for this tick
   374	            _applyUpkeep(clan, tick);
   375	
   376	            // 2. Wheat plot regrow check (lazy, per tick)
   377	            for (uint256 pi = 0; pi < 2; pi++) {
   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   380	                    plot.state = WheatPlotState.Harvestable;
   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   382	                    plot.regrowUntilTick = 0;
   383	                }
   384	            }
   385	
   386	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   387	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   388	                Clansman storage cs = _clansmen[clansmanIds[i]];
   389	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   390	            }
   391	        }
   392	
   393	        clan.lastSettledTick = curTick;
   394	        emit ClanSettled(clanId, curTick);
   395	    }
   396	
   397	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
   398	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   399	        if (clan.livingClansmen == 0) return;
   400	
   401	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   402	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   403	
   404	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   405	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   406	
   407	        if (hadEnoughWheat) {
   408	            clan.vaultWheat -= wheatNeeded;
   409	        } else {
   410	            clan.vaultWheat = 0;
   411	        }
   412	        if (hadEnoughFish) {
   413	            clan.vaultFish -= fishNeeded;
   414	        } else {
   415	            clan.vaultFish = 0;
   416	        }
   417	
   418	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   419	        if (starving && clan.starvationStartsAtTick == 0) {
   420	            clan.starvationStartsAtTick = tick;
   421	            emit ClanStarvationChanged(clan.clanId, true, tick);
   422	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   423	            clan.starvationStartsAtTick = 0;
   424	            emit ClanStarvationChanged(clan.clanId, false, tick);
   425	        }
   426	    }
   427	
   428	    /// @dev Check if a clan is currently starving (lazy read).
   429	    function _isStarving(Clan storage clan) internal view returns (bool) {
   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   431	    }
   432	
   433	    /// @dev Resolve an action for a clansman that is in ACTING state.
   434	    function _resolveAction(
   435	        Clan storage clan,
   436	        Clansman storage cs,
   437	        Mission storage m,
   438	        uint32 clanId,
   439	        uint64 tick,
   440	        bytes32 tickSeed
   441	    ) internal {
   442	        bool starving = _isStarving(clan);
   443	        ActionType action = m.action;
   444	
   445	        if (action == ActionType.ChopWood) {
   446	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   447	        } else if (action == ActionType.MineIron) {
   448	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   449	        } else if (action == ActionType.FishDocks) {
   450	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   451	        } else if (action == ActionType.FishDeepSea) {
   452	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   453	        } else if (action == ActionType.HarvestWheat) {
   454	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   455	        } else if (action == ActionType.DepositResources) {
   456	            // Deposit event ABI intentionally stores the current tick as uint32.
   457	            // forge-lint: disable-next-line(unsafe-typecast)
   458	            _doDeposit(clan, cs, m, clanId, uint32(tick));
   459	        } else if (action == ActionType.Wait) {
   460	            // NOOP — worker stays ACTING (continuous), no transition needed
   461	            // Wait mission is effectively persistent until interrupted
   462	        } else if (action == ActionType.DefendBase) {
   463	            // Persistent mission. Registration happens atomically at order submission.
   464	        } else if (
   465	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   466	        ) {
   467	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   468	            _doBuilding(clan, cs, m, clanId, tick, action);
   469	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   470	            _enqueueScheduledMarketAction(clanId, cs.clansmanId, m);
   471	            _completeMission(cs, m);
   472	        }
   473	    }
   474	
   475	    // -------------------------------------------------------------------------
   476	    // Gathering helpers
   477	    // -------------------------------------------------------------------------
   478	
   479	    function _gatherWood(
   480	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   481	        Clansman storage cs,
   482	        Mission storage m,
   483	        uint32 clanId,
   484	        uint64 tick,
   485	        bool starving,
   486	        bytes32 tickSeed
   487	    ) internal {
   488	        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
   489	            _completeMission(cs, m);
   490	            return;
   491	        }
   492	
   493	        uint256 remaining = ClanWorldConstants.WOOD_CARRY_CAP - cs.carryWood;
   494	        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
   495	        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   496	        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
   497	            yield *= 2;
   498	        }
   499	
   500	        if (starving) yield = yield / 2;
   501	        if (yield > remaining) yield = remaining;
   502	        cs.carryWood += yield;
   503	
   504	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   505	
   506	        if (cs.carryWood >= ClanWorldConstants.WOOD_CARRY_CAP) {
   507	            _completeMission(cs, m);
   508	        }
   509	    }
   510	
   511	    function _gatherIron(
   512	        Clan storage clan,
   513	        Clansman storage cs,
   514	        Mission storage m,
   515	        uint32 clanId,
   516	        uint64 tick,
   517	        bool starving,
   518	        bytes32 tickSeed
   519	    ) internal {
   520	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   521	        if (remaining == 0) {
   522	            _completeMission(cs, m);
   523	            return;
   524	        }
   525	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   526	        if (starving) yield = yield / 2;
   527	        if (yield > remaining) yield = remaining;
   528	        cs.carryIron += yield;
   529	
   530	        // Gold bonus roll — scoped to reduce stack depth
   531	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   532	
   533	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   534	
   535	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   536	            _completeMission(cs, m);
   537	        }
   538	    }
   539	
   540	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   541	        internal
   542	        returns (uint256 goldBonus)
   543	    {
   544	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   545	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   546	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   547	            clan.goldBalance += goldBonus;
   548	        }
   549	    }
   550	
   551	    function _gatherFishDocks(
   552	        Clan storage,
   553	        Clansman storage cs,
   554	        Mission storage m,
   555	        uint32 clanId,
   556	        uint64 tick,
   557	        bool starving,
   558	        bytes32 tickSeed
   559	    ) internal {
   560	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   561	        if (remaining == 0) {
   562	            _completeMission(cs, m);
   563	            return;
   564	        }
   565	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   566	        uint256 fishRoll = uint256(fishRng) % 10000;
   567	        uint256 yield = 0;
   568	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   569	            yield = 1e18;
   570	        }
   571	        if (starving) yield = yield / 2;
   572	        if (yield > remaining) yield = remaining;
   573	        if (yield > 0) {
   574	            cs.carryFish += yield;
   575	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   576	        }
   577	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   578	            _completeMission(cs, m);
   579	        }
   580	    }
   581	
   582	    function _gatherFishDeepSea(
   583	        Clan storage,
   584	        Clansman storage cs,
   585	        Mission storage m,
   586	        uint32 clanId,
   587	        uint64 tick,
   588	        bool starving,
   589	        bytes32 tickSeed
   590	    ) internal {
   591	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   592	        if (remaining == 0) {
   593	            _completeMission(cs, m);
   594	            return;
   595	        }
   596	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   597	        uint256 fishRoll = uint256(fishRng) % 10000;
   598	        uint256 yield = 0;
   599	        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
   600	            yield = 1e18;
   601	        }
   602	        if (starving) yield = yield / 2;
   603	        if (yield > remaining) yield = remaining;
   604	        if (yield > 0) {
   605	            cs.carryFish += yield;
   606	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
   607	        }
   608	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   609	            _completeMission(cs, m);
   610	        }
   611	    }
   612	
   613	    function _gatherWheat(
   614	        Clan storage,
   615	        /* clan — unused but kept positional for callsite parity */
   616	        Clansman storage cs,
   617	        Mission storage m,
   618	        uint32 clanId,
   619	        uint64 tick,
   620	        bool starving
   621	    ) internal {
   622	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
   623	        if (remaining == 0) {
   624	            _completeMission(cs, m);
   625	            return;
   626	        }
   627	        // Determine plot index from region
   628	        uint8 region = m.targetRegion;
   629	        uint256 plotIdx;
   630	        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
   631	            plotIdx = 0;
   632	        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
   633	            plotIdx = 1;
   634	        } else {
   635	            // Wrong region — complete (no harvest)
   636	            _completeMission(cs, m);
   637	            return;
   638	        }
   639	
   640	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
   641	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
   642	            // Plot not ready — worker waits
   643	            _completeMission(cs, m);
   644	            return;
   645	        }
   646	
   647	        uint256 yield = WHEAT_HARVEST_RATE;
   648	        if (starving) yield = yield / 2;
   649	        if (yield > remaining) yield = remaining;
   650	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
   651	
   652	        cs.carryWheat += yield;
   653	        plot.remainingWheat -= yield;
   654	
   655	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
   656	
   657	        if (plot.remainingWheat == 0) {
   658	            plot.state = WheatPlotState.Regrowing;
   659	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
   660	        }
   661	
   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
   663	            _completeMission(cs, m);
   664	        }
   665	        // else continuous
   666	    }
   667	
   668	    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
   669	        internal
   670	    {
   671	        // Must be at homebase region
   672	        if (cs.currentRegion != clan.baseRegion) {
   673	            _completeMission(cs, m);
   674	            return;
   675	        }
   676	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
   677	        if (!hasAnything) {
   678	            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
   679	            _completeMission(cs, m);
   680	            return;
   681	        }
   682	
   683	        uint256 woodDelta = cs.carryWood;
   684	        uint256 ironDelta = cs.carryIron;
   685	        uint256 wheatDelta = cs.carryWheat;
   686	        uint256 fishDelta = cs.carryFish;
   687	
   688	        clan.vaultWood += woodDelta;
   689	        clan.vaultIron += ironDelta;
   690	        clan.vaultWheat += wheatDelta;

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '35,190p;360,390p;535,590p;700,720p'" in /home/claude/code/clan-world
 succeeded in 0ms:
    35	    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    36	    uint64 internal constant BANDIT_REST_TICKS = 2;
    37	    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;
    38	
    39	    // Clansman cadence
    40	    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
    41	
    42	    // Carry caps (per clansman)
    43	    uint256 internal constant WOOD_CARRY_CAP = 10e18;
    44	    uint256 internal constant WOOD_CAP = WOOD_CARRY_CAP;
    45	    uint256 internal constant IRON_CAP = 5e18;
    46	    uint256 internal constant WHEAT_CAP = 40e18;
    47	    uint256 internal constant FISH_CAP = 8e18;
    48	
    49	    // Gathering yields
    50	    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
    51	    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
    52	    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
    53	    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%
    54	
    55	    uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
    56	    uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
    57	    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;
    58	
    59	    uint16 internal constant FISH_DOCKS_BPS = 2500; // 25%
    60	    uint16 internal constant FISH_DEEP_BPS = 7500; // 75%
    61	
    62	    // Upkeep
    63	    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    64	    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    65	    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    66	    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
    67	
    68	    // Wheat plots
    69	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    70	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
    71	
    72	    // Bandit combat
    73	    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    74	    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%
    75	
    76	    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    77	    uint8 internal constant REGION_NOOP = 0;
    78	    uint8 internal constant REGION_FOREST = 1;
    79	    uint8 internal constant REGION_MOUNTAINS = 2;
    80	    uint8 internal constant REGION_UNICORN_TOWN = 3;
    81	    uint8 internal constant REGION_WEST_FARMS = 4;
    82	    uint8 internal constant REGION_EAST_FARMS = 5;
    83	    uint8 internal constant REGION_WEST_DOCKS = 6;
    84	    uint8 internal constant REGION_EAST_DOCKS = 7;
    85	    uint8 internal constant REGION_DEEP_SEA = 8;
    86	
    87	    // Market event resource ids. ResourceType covers the four vault resources;
    88	    // gold is the quote asset emitted as resource id 4.
    89	    uint8 internal constant RESOURCE_GOLD = 4;
    90	
    91	    // Sentinels
    92	    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    93	    uint32 internal constant BANDIT_ID_NULL = 0;
    94	}
    95	
    96	// =============================================================================
    97	// ENUMS
    98	// =============================================================================
    99	
   100	enum ClanState {
   101	    ACTIVE,
   102	    DEAD
   103	}
   104	
   105	enum ClansmanState {
   106	    WAITING,
   107	    TRAVELING,
   108	    ACTING,
   109	    DEAD
   110	}
   111	
   112	enum BanditState {
   113	    NONE,
   114	    CAMPING,
   115	    RESTING,
   116	    ATTACKING,
   117	    DEFEATED,
   118	    ESCAPED
   119	}
   120	
   121	enum WheatPlotState {
   122	    Harvestable,
   123	    Regrowing,
   124	    WinterLocked
   125	}
   126	
   127	enum ResourceType {
   128	    Wood,
   129	    Iron,
   130	    Wheat,
   131	    Fish
   132	}
   133	
   134	enum ActionType {
   135	    None,
   136	    ChopWood,
   137	    MineIron,
   138	    FishDocks,
   139	    FishDeepSea,
   140	    HarvestWheat,
   141	    DepositResources,
   142	    BuildWall,
   143	    UpgradeBase,
   144	    UpgradeMonument,
   145	    DefendBase,
   146	    MarketBuy,
   147	    MarketSell,
   148	    Wait
   149	}
   150	
   151	enum MarketExecutionMode {
   152	    None,
   153	    Immediate,
   154	    Scheduled
   155	}
   156	
   157	enum StatusCode {
   158	    OK,
   159	    ERR_CLAN_DEAD,
   160	    ERR_CLAN_NOT_OWNED,
   161	    ERR_CLANSMAN_DEAD,
   162	    ERR_INVALID_CLANSMAN,
   163	    ERR_INVALID_REGION,
   164	    ERR_INVALID_ACTION,
   165	    ERR_INVALID_TARGET,
   166	    ERR_COOLDOWN_ACTIVE,
   167	    ERR_NOT_WAITING,
   168	    ERR_NOT_IN_UNICORN_TOWN,
   169	    ERR_NOT_AT_HOMEBASE,
   170	    ERR_NOT_AT_TARGET_BASE,
   171	    ERR_NOT_DEFENDABLE,
   172	    ERR_MISSING_RESOURCES,
   173	    ERR_EMPTY_CARGO,
   174	    ERR_PLOT_NOT_READY,
   175	    ERR_PLOT_EMPTY,
   176	    ERR_MARKET_ZERO_AMOUNT,
   177	    ERR_MARKET_UNSUPPORTED_TOKEN,
   178	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   179	    ERR_MARKET_BUY_OVER_CAPACITY,
   180	    ERR_WORLD_TICK_MISMATCH,
   181	    ERR_NO_ACTIVE_BANDIT,
   182	    ERR_SEASON_ENDED,
   183	    ERR_NOT_ENOUGH_GOLD,
   184	    ERR_CARRY_FULL,
   185	    ERR_LIQUIDITY_INSUFFICIENT,
   186	    ERR_MAX_GOLD_IN_EXCEEDED
   187	}
   188	
   189	// =============================================================================
   190	// CORE STATE STRUCTS (raw storage shape)
   360	struct DerivedClansmanState {
   361	    Clansman clansman; // settled to current tick
   362	    Mission activeMission; // active=false if none
   363	    uint8 effectiveRegion; // for traveling, derived from route + elapsed ticks
   364	    uint64 derivedAtTick;
   365	}
   366	
   367	// =============================================================================
   368	// WRITE INPUT / OUTPUT STRUCTS
   369	// =============================================================================
   370	
   371	struct ClanOrder {
   372	    uint32 clansmanId;
   373	    uint8 gotoRegion;
   374	    ActionType action;
   375	
   376	    uint32 targetClanId; // DefendBase only
   377	    address marketToken;
   378	    uint256 marketAmount;
   379	    uint256 maxGoldIn;
   380	}
   381	
   382	struct OrderResult {
   383	    uint32 clansmanId;
   384	    StatusCode status;
   385	    uint64 cooldownEndsAtTs;
   386	    uint64 missionNonce;
   387	    MarketExecutionMode marketMode;
   388	}
   389	
   390	struct PoolSeedConfig {
   535	        uint256 ironGained,
   536	        uint256 wheatGained,
   537	        uint256 fishGained,
   538	        uint256 goldBonus,
   539	        uint64 atTick
   540	    );
   541	    event ResourcesDeposited(
   542	        uint32 indexed clanId,
   543	        uint32 indexed clansmanId,
   544	        uint256 woodDelta,
   545	        uint256 ironDelta,
   546	        uint256 wheatDelta,
   547	        uint256 fishDelta,
   548	        uint32 tick
   549	    );
   550	
   551	    // ----- building -----
   552	    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   553	    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   554	    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   555	
   556	    // ----- market -----
   557	    event ImmediateMarketActionExecuted(
   558	        uint32 indexed clanId,
   559	        uint32 csId,
   560	        ActionType action,
   561	        uint8 resourceIn,
   562	        uint256 amountIn,
   563	        uint8 resourceOut,
   564	        uint256 amountOut,
   565	        uint64 tick
   566	    );
   567	    event ScheduledMarketActionExecuted(
   568	        uint32 indexed clanId,
   569	        uint32 csId,
   570	        ActionType action,
   571	        uint8 resourceIn,
   572	        uint256 amountIn,
   573	        uint8 resourceOut,
   574	        uint256 amountOut,
   575	        uint64 settledAtTick
   576	    );
   577	    event MarketActionFailed(
   578	        uint32 indexed clanId, uint32 csId, ActionType action, MarketExecutionMode mode, StatusCode reason, uint64 tick
   579	    );
   580	    event ScheduledMarketActionCommitted(
   581	        uint64 indexed executeAtTick,
   582	        uint64 indexed commitSequence,
   583	        uint32 indexed clanId,
   584	        uint32 clansmanId,
   585	        ActionType action,
   586	        address marketToken,
   587	        uint256 marketAmount,
   588	        uint256 maxGoldIn
   589	    );
   590	    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
   700	        uint256 blueprint,
   701	        uint256 wood,
   702	        uint256 iron,
   703	        uint256 wheat,
   704	        uint256 fish
   705	    ) external;
   706	
   707	    // -------------------------------------------------------------------------
   708	    // Raw read getters (committed storage, no settlement simulation)
   709	    // -------------------------------------------------------------------------
   710	
   711	    function getWorldState() external view returns (WorldState memory);
   712	
   713	    function getTreasuryState() external view returns (TreasuryState memory);
   714	
   715	    function getResourceToken(uint8 resourceType) external view returns (address);
   716	
   717	    function getPool(uint8 resourceType) external view returns (address);
   718	
   719	    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut);
   720	

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/StubPool.sol | nl -ba | sed -n '1,180p' && git show 9f93593:packages/contracts/src/MinimalERC20.sol | nl -ba | sed -n '1,140p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	interface IERC20Balance {
     5	    function balanceOf(address account) external view returns (uint256);
     6	}
     7	
     8	/// @notice Constant-product AMM pool for one resource/gold pair.
     9	///         TOKEN_A is the resource token and TOKEN_B is gold. ClanWorld owns
    10	///         the swap surface because clans use the engine's internal accounting,
    11	///         while the treasury seeds real ERC20 balances into the pool once.
    12	contract StubPool {
    13	    address public immutable TOKEN_A; // resource token
    14	    address public immutable TOKEN_B; // gold token
    15	    address public immutable ENGINE; // ClanWorld address
    16	
    17	    uint256 public reserveA; // resource reserve
    18	    uint256 public reserveB; // gold reserve
    19	
    20	    bool private _seeded;
    21	
    22	    modifier onlyEngine() {
    23	        require(msg.sender == ENGINE, "StubPool: only engine");
    24	        _;
    25	    }
    26	
    27	    constructor(address tokenA_, address tokenB_, address engine_) {
    28	        TOKEN_A = tokenA_;
    29	        TOKEN_B = tokenB_;
    30	        ENGINE = engine_;
    31	    }
    32	
    33	    /// @notice Called by ClanWorld at seedPools time to set initial reserves.
    34	    ///         Can only be called once.
    35	    function seed(uint256 amountA, uint256 amountB) external onlyEngine {
    36	        require(!_seeded, "StubPool: already seeded");
    37	        require(amountA > 0 && amountB > 0, "StubPool: zero seed");
    38	        require(IERC20Balance(TOKEN_A).balanceOf(address(this)) >= amountA, "StubPool: missing token A");
    39	        require(IERC20Balance(TOKEN_B).balanceOf(address(this)) >= amountB, "StubPool: missing token B");
    40	        reserveA = amountA;
    41	        reserveB = amountB;
    42	        _seeded = true;
    43	    }
    44	
    45	    function getAmountOutForExactIn(uint256 amountIn) public view returns (uint256 amountOut) {
    46	        require(amountIn > 0, "StubPool: zero amount");
    47	        require(reserveA > 0 && reserveB > 0, "StubPool: not seeded");
    48	        amountOut = (reserveB * amountIn) / (reserveA + amountIn);
    49	    }
    50	
    51	    function getAmountInForExactOut(uint256 amountOut) public view returns (uint256 amountIn) {
    52	        require(amountOut > 0, "StubPool: zero amount");
    53	        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
    54	        require(reserveB > 0, "StubPool: not seeded");
    55	
    56	        uint256 numerator = reserveB * amountOut;
    57	        uint256 denominator = reserveA - amountOut;
    58	        amountIn = numerator / denominator + (numerator % denominator == 0 ? 0 : 1);
    59	    }
    60	
    61	    /// @notice Exact-input sell: resource in, gold out.
    62	    function swapExactInForOut(uint256 amountIn, uint256 minOut) external onlyEngine returns (uint256 amountOut) {
    63	        return _swapExactInForOut(amountIn, minOut);
    64	    }
    65	
    66	    function _swapExactInForOut(uint256 amountIn, uint256 minOut) internal returns (uint256 amountOut) {
    67	        uint256 priorK = reserveA * reserveB;
    68	        amountOut = getAmountOutForExactIn(amountIn);
    69	        require(amountOut >= minOut, "StubPool: insufficient output");
    70	
    71	        reserveA += amountIn;
    72	        reserveB -= amountOut;
    73	
    74	        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
    75	    }
    76	
    77	    /// @notice Exact-output buy: gold in, exact resource out.
    78	    function swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn)
    79	        external
    80	        onlyEngine
    81	        returns (uint256 amountIn)
    82	    {
    83	        return _swapExactOutForInWithMaxIn(amountOut, maxIn);
    84	    }
    85	
    86	    function _swapExactOutForInWithMaxIn(uint256 amountOut, uint256 maxIn) internal returns (uint256 amountIn) {
    87	        uint256 priorK = reserveA * reserveB;
    88	        amountIn = getAmountInForExactOut(amountOut);
    89	        require(amountIn <= maxIn, "StubPool: max input exceeded");
    90	
    91	        reserveA -= amountOut;
    92	        reserveB += amountIn;
    93	
    94	        require(reserveA * reserveB >= priorK, "StubPool: k invariant");
    95	    }
    96	
    97	    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
    98	    ///         Legacy alias for Phase 6.1 scheduled-market code.
    99	    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
   100	        goldOut = _swapExactInForOut(amountIn, 1);
   101	    }
   102	
   103	    /// @notice Legacy quote alias for exact-output resource buys.
   104	    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
   105	        goldIn = getAmountInForExactOut(amountOut);
   106	    }
   107	
   108	    /// @notice Exact-output buy: clan buys amountOut of resource, pays goldIn.
   109	    ///         Legacy alias for Phase 6.1 scheduled-market code.
   110	    function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
   111	        goldIn = _swapExactOutForInWithMaxIn(amountOut, type(uint256).max);
   112	    }
   113	
   114	    function getReserves() external view returns (uint256, uint256) {
   115	        return (reserveA, reserveB);
   116	    }
   117	}
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/// @notice Minimal ERC20 boundary token. No external deps.
     5	contract MinimalERC20 {
     6	    string public name;
     7	    string public symbol;
     8	    uint8 public constant decimals = 18;
     9	
    10	    uint256 public totalSupply;
    11	    address public immutable DEPLOYER;
    12	    address public engine;
    13	    uint8 public resourceType;
    14	    bool public boundaryConfigured;
    15	    bool public treasurySeeded;
    16	
    17	    mapping(address => uint256) public balanceOf;
    18	    mapping(address => mapping(address => uint256)) public allowance;
    19	
    20	    event Transfer(address indexed from, address indexed to, uint256 value);
    21	    event Approval(address indexed owner_, address indexed spender, uint256 value);
    22	    event BoundaryConfigured(uint8 indexed resourceType, address indexed engine);
    23	    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
    24	    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
    25	
    26	    constructor(string memory name_, string memory symbol_) {
    27	        name = name_;
    28	        symbol = symbol_;
    29	        DEPLOYER = msg.sender;
    30	        resourceType = type(uint8).max;
    31	    }
    32	
    33	    /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
    34	    /// body slim so the require isn't duplicated at every call site.
    35	    function _onlyDeployer() internal view {
    36	        require(msg.sender == DEPLOYER, "MinimalERC20: not deployer");
    37	    }
    38	
    39	    function _onlyEngine() internal view {
    40	        require(boundaryConfigured && msg.sender == engine, "MinimalERC20: not engine");
    41	    }
    42	
    43	    modifier onlyDeployer() {
    44	        _onlyDeployer();
    45	        _;
    46	    }
    47	
    48	    modifier onlyEngine() {
    49	        _onlyEngine();
    50	        _;
    51	    }
    52	
    53	    function configureBoundary(uint8 resourceType_, address engine_) external onlyDeployer {
    54	        require(!boundaryConfigured, "MinimalERC20: boundary configured");
    55	        require(engine_ != address(0), "MinimalERC20: zero engine");
    56	
    57	        resourceType = resourceType_;
    58	        engine = engine_;
    59	        boundaryConfigured = true;
    60	
    61	        emit BoundaryConfigured(resourceType_, engine_);
    62	    }
    63	
    64	    function seedTreasury(address treasury, uint256 amount) external onlyDeployer {
    65	        require(!treasurySeeded, "MinimalERC20: treasury seeded");
    66	        require(treasury != address(0), "MinimalERC20: zero treasury");
    67	
    68	        treasurySeeded = true;
    69	        _mint(treasury, amount);
    70	    }
    71	
    72	    function mint(address to, uint256 amount) external onlyEngine {
    73	        _mint(to, amount);
    74	        emit ResourceMinted(resourceType, to, amount);
    75	    }
    76	
    77	    function burn(address from, uint256 amount) external onlyEngine {
    78	        balanceOf[from] -= amount;
    79	        totalSupply -= amount;
    80	        emit Transfer(from, address(0), amount);
    81	        emit ResourceBurned(resourceType, from, amount);
    82	    }
    83	
    84	    function _mint(address to, uint256 amount) internal {
    85	        totalSupply += amount;
    86	        balanceOf[to] += amount;
    87	        emit Transfer(address(0), to, amount);
    88	    }
    89	
    90	    function transfer(address to, uint256 amount) external returns (bool) {
    91	        balanceOf[msg.sender] -= amount;
    92	        balanceOf[to] += amount;
    93	        emit Transfer(msg.sender, to, amount);
    94	        return true;
    95	    }
    96	
    97	    function transferFrom(address from, address to, uint256 amount) external returns (bool) {
    98	        allowance[from][msg.sender] -= amount;
    99	        balanceOf[from] -= amount;
   100	        balanceOf[to] += amount;
   101	        emit Transfer(from, to, amount);
   102	        return true;
   103	    }
   104	
   105	    function approve(address spender, uint256 amount) external returns (bool) {
   106	        allowance[msg.sender][spender] = amount;
   107	        emit Approval(msg.sender, spender, amount);
   108	        return true;
   109	    }
   110	}

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2285,2365p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2285	            }
  2286	        }
  2287	    }
  2288	
  2289	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
  2290	        return _defendingClansByRegion[region];
  2291	    }
  2292	
  2293	    // =========================================================================
  2294	    // DERIVED READ GETTERS (read-only, no storage mutation)
  2295	    // =========================================================================
  2296	
  2297	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
  2298	    ///      Carry amounts lag until next settleClan().
  2299	    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
  2300	        Clan memory clan = _clans[clanId];
  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  2302	        uint256 lootVal = _lootValueRaw(clan);
  2303	        return
  2304	            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
  2305	    }
  2306	
  2307	    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
  2308	        Clansman memory cs = _clansmen[clansmanId];
  2309	        Mission memory m = _missions[clansmanId];
  2310	        uint8 effectiveRegion = cs.currentRegion;
  2311	        if (cs.state == ClansmanState.TRAVELING && m.active) {
  2312	            // Simplified: if past arrivalTick, they're at target; else at start
  2313	            if (_world.currentTick >= m.arrivalTick) {
  2314	                effectiveRegion = m.targetRegion;
  2315	            } else {
  2316	                effectiveRegion = m.startRegion;
  2317	            }
  2318	        }
  2319	        return DerivedClansmanState({
  2320	            clansman: cs, activeMission: m, effectiveRegion: effectiveRegion, derivedAtTick: _world.currentTick
  2321	        });
  2322	    }
  2323	
  2324	    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
  2325	        return 0; // Phase 3
  2326	    }
  2327	
  2328	    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
  2329	        external
  2330	        pure
  2331	        override
  2332	        returns (uint8 travelTicks, bytes8 path)
  2333	    {
  2334	        if (srcRegion > 8 || dstRegion > 8) {
  2335	            return (0, bytes8(0));
  2336	        }
  2337	        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
  2338	            travelTicks = 0;
  2339	            path = bytes8(uint64(srcRegion) << 56);
  2340	            return (travelTicks, path);
  2341	        }
  2342	        travelTicks = _travelTicks(srcRegion, dstRegion);
  2343	        path = _buildPath(srcRegion, dstRegion);
  2344	    }
  2345	
  2346	    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
  2347	        return _lootValueRaw(_clans[clanId]);
  2348	    }
  2349	
  2350	    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
  2351	        // Phase 1: return raw value (no simulation)
  2352	        return _lootValueRaw(_clans[clanId]);
  2353	    }
  2354	
  2355	    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
  2356	    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
  2357	        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
  2358	    }
  2359	
  2360	    // =========================================================================
  2361	    // UI INDEXER AGGREGATOR GETTERS
  2362	    // =========================================================================
  2363	
  2364	    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
  2365	    ///      Carry amounts not included. Full view-only settlement deferred.

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '740,1460p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   740	        return count;
   741	    }
   742	
   743	    function _advanceUntilNextHeartbeatCloses(uint64 tick) internal {
   744	        while (world.getWorldState().currentTick < tick) {
   745	            _advanceTick();
   746	        }
   747	    }
   748	
   749	    // Helper: get the first clansman id for a clan
   750	    function _firstCs(uint32 clanId) internal view returns (uint32) {
   751	        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
   752	    }
   753	
   754	    function _setupHarnessClanAt(ClansmanState state, uint8 region, uint64 cooldownEndsAtTs)
   755	        internal
   756	        returns (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId)
   757	    {
   758	        return _setupHarnessClanAtWithWoodSeed(state, region, cooldownEndsAtTs, world.INITIAL_RESOURCE_POOL_SEED());
   759	    }
   760	
   761	    function _setupHarnessClanAtWithWoodSeed(
   762	        ClansmanState state,
   763	        uint8 region,
   764	        uint64 cooldownEndsAtTs,
   765	        uint256 woodSeed
   766	    ) internal returns (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) {
   767	        harness = new ClanWorldTestHarness();
   768	        world = harness;
   769	        woodAddr = _setupMarketWithWoodSeed(woodSeed);
   770	        clanId = _mintClan();
   771	        csId = _firstCs(clanId);
   772	        harness.setClansmanForTest(csId, state, region, cooldownEndsAtTs);
   773	    }
   774	
   775	    function test_immediateMarketSell_executesInSubmitTx() public {
   776	        (, address woodAddr, uint32 clanId, uint32 csId) =
   777	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
   778	
   779	        Clan memory beforeClan = world.getClan(clanId);
   780	        uint256 expectedGoldOut = woodPool.getAmountOutForExactIn(5e18);
   781	
   782	        vm.expectEmit(true, false, false, true);
   783	        emit IClanWorldEvents.ImmediateMarketActionExecuted(
   784	            clanId,
   785	            csId,
   786	            ActionType.MarketSell,
   787	            uint8(ResourceType.Wood),
   788	            5e18,
   789	            ClanWorldConstants.RESOURCE_GOLD,
   790	            expectedGoldOut,
   791	            world.getWorldState().currentTick
   792	        );
   793	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
   794	
   795	        Clan memory afterClan = world.getClan(clanId);
   796	        Clansman memory cs = world.getClansman(csId);
   797	
   798	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate sell should be accepted");
   799	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "sell should be immediate");
   800	        assertEq(afterClan.vaultWood, beforeClan.vaultWood - 5e18, "vault wood should be sold immediately");
   801	        assertGt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be credited immediately");
   802	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "worker should remain waiting");
   803	        assertEq(cs.currentRegion, ClanWorldConstants.REGION_UNICORN_TOWN, "worker should stay in town");
   804	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "immediate action should consume cooldown");
   805	        assertFalse(world.getActiveMission(csId).active, "immediate action should not install a mission");
   806	        assertEq(world.getScheduledMarketActionsForTick(world.getWorldState().currentTick).length, 0, "no queue entry");
   807	    }
   808	
   809	    function test_immediateMarket_townOnCooldown_fallsBackToScheduled() public {
   810	        uint64 cooldownEndsAt = uint64(block.timestamp + 30);
   811	        (, address woodAddr, uint32 clanId, uint32 csId) =
   812	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, cooldownEndsAt);
   813	
   814	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   815	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
   816	        Mission memory m = world.getActiveMission(csId);
   817	
   818	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market order should schedule while cooling down");
   819	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "cooldown should force scheduled mode");
   820	        assertTrue(m.active, "scheduled fallback should install a mission");
   821	        assertEq(uint8(m.marketMode), uint8(MarketExecutionMode.Scheduled), "mission should be scheduled");
   822	        assertEq(
   823	            world.getScheduledMarketActionsForTick(m.settlesAtTick).length,
   824	            0,
   825	            "scheduled action queues when mission settles"
   826	        );
   827	        assertEq(world.getClan(clanId).goldBalance, goldBefore, "scheduled fallback should not trade immediately");
   828	    }
   829	
   830	    function test_immediateMarket_notInTown_fallsBackToScheduled() public {
   831	        (, address woodAddr, uint32 clanId, uint32 csId) =
   832	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_FOREST, 0);
   833	
   834	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
   835	        Mission memory m = world.getActiveMission(csId);
   836	
   837	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
   838	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "out-of-town should be scheduled");
   839	        assertTrue(m.active, "scheduled fallback should install a mission");
   840	        assertEq(
   841	            world.getScheduledMarketActionsForTick(m.settlesAtTick).length,
   842	            0,
   843	            "scheduled action queues when mission settles"
   844	        );
   845	    }
   846	
   847	    function test_immediateMarket_busyWorker_fallsBackToScheduled() public {
   848	        (, address woodAddr, uint32 clanId, uint32 csId) =
   849	            _setupHarnessClanAt(ClansmanState.ACTING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
   850	
   851	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
   852	        Mission memory m = world.getActiveMission(csId);
   853	
   854	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
   855	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "busy worker should be scheduled");
   856	        assertTrue(m.active, "scheduled fallback should install a mission");
   857	        assertEq(
   858	            world.getScheduledMarketActionsForTick(m.settlesAtTick).length,
   859	            0,
   860	            "scheduled action queues when mission settles"
   861	        );
   862	    }
   863	
   864	    function test_immediateMarketBuy_executesWhenMaxGoldSatisfied() public {
   865	        (, address woodAddr, uint32 clanId, uint32 csId) =
   866	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
   867	
   868	        Clan memory beforeClan = world.getClan(clanId);
   869	        uint256 expectedGoldIn = woodPool.getAmountInForExactOut(1e18);
   870	
   871	        vm.expectEmit(true, false, false, true);
   872	        emit IClanWorldEvents.ImmediateMarketActionExecuted(
   873	            clanId,
   874	            csId,
   875	            ActionType.MarketBuy,
   876	            ClanWorldConstants.RESOURCE_GOLD,
   877	            expectedGoldIn,
   878	            uint8(ResourceType.Wood),
   879	            1e18,
   880	            world.getWorldState().currentTick
   881	        );
   882	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 2e18);
   883	
   884	        Clan memory afterClan = world.getClan(clanId);
   885	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate buy should be accepted");
   886	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "buy should be immediate");
   887	        assertGt(afterClan.vaultWood, beforeClan.vaultWood, "vault wood should increase immediately");
   888	        assertLt(afterClan.goldBalance, beforeClan.goldBalance, "gold should be debited immediately");
   889	        assertFalse(world.getActiveMission(csId).active, "immediate buy should not install a mission");
   890	    }
   891	
   892	    function test_immediateMarket_insufficientLiquidityFailsAndConsumesCooldown() public {
   893	        (, address woodAddr, uint32 clanId, uint32 csId) =
   894	            _setupHarnessClanAtWithWoodSeed(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0, 5e18);
   895	
   896	        Clan memory beforeClan = world.getClan(clanId);
   897	
   898	        vm.expectEmit(true, false, false, true);
   899	        emit IClanWorldEvents.MarketActionFailed(
   900	            clanId,
   901	            csId,
   902	            ActionType.MarketBuy,
   903	            MarketExecutionMode.Immediate,
   904	            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
   905	            world.getWorldState().currentTick
   906	        );
   907	        OrderResult[] memory r =
   908	            _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 6e18, type(uint256).max);
   909	
   910	        Clan memory afterClan = world.getClan(clanId);
   911	        Clansman memory cs = world.getClansman(csId);
   912	
   913	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
   914	        assertEq(
   915	            uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed immediate action stays immediate"
   916	        );
   917	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
   918	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
   919	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
   920	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
   921	        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
   922	    }
   923	
   924	    function test_immediateMarketBuy_maxGoldExceededFailsAndConsumesCooldown() public {
   925	        (, address woodAddr, uint32 clanId, uint32 csId) =
   926	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
   927	
   928	        Clan memory beforeClan = world.getClan(clanId);
   929	
   930	        vm.expectEmit(true, false, false, true);
   931	        emit IClanWorldEvents.MarketActionFailed(
   932	            clanId,
   933	            csId,
   934	            ActionType.MarketBuy,
   935	            MarketExecutionMode.Immediate,
   936	            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
   937	            world.getWorldState().currentTick
   938	        );
   939	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
   940	
   941	        Clan memory afterClan = world.getClan(clanId);
   942	        Clansman memory cs = world.getClansman(csId);
   943	
   944	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
   945	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
   946	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
   947	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
   948	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
   949	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
   950	        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
   951	    }
   952	
   953	    function test_immediateMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
   954	        (, address woodAddr, uint32 clanId, uint32 csId) =
   955	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
   956	
   957	        Clan memory beforeClan = world.getClan(clanId);
   958	
   959	        vm.expectEmit(true, false, false, true);
   960	        emit IClanWorldEvents.MarketActionFailed(
   961	            clanId,
   962	            csId,
   963	            ActionType.MarketBuy,
   964	            MarketExecutionMode.Immediate,
   965	            StatusCode.ERR_NOT_ENOUGH_GOLD,
   966	            world.getWorldState().currentTick
   967	        );
   968	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 7e18, 10e18);
   969	
   970	        Clan memory afterClan = world.getClan(clanId);
   971	        Clansman memory cs = world.getClansman(csId);
   972	
   973	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
   974	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
   975	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
   976	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
   977	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed immediate action should consume cooldown");
   978	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed immediate worker waits");
   979	        assertFalse(world.getActiveMission(csId).active, "failed immediate action should not schedule fallback");
   980	    }
   981	
   982	    function test_immediateMarketBuy_overCapacityRejectsAtSubmit() public {
   983	        (ClanWorldTestHarness harness, address woodAddr, uint32 clanId, uint32 csId) =
   984	            _setupHarnessClanAt(ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);
   985	        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP - 5e17, 0, 0, 0);
   986	
   987	        Clan memory beforeClan = world.getClan(clanId);
   988	        Clansman memory beforeCs = world.getClansman(csId);
   989	
   990	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 10e18);
   991	
   992	        Clan memory afterClan = world.getClan(clanId);
   993	        Clansman memory cs = world.getClansman(csId);
   994	
   995	        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
   996	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
   997	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
   998	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
   999	        assertEq(cs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "rejected buy should not consume cooldown");
  1000	        assertEq(uint8(cs.state), uint8(beforeCs.state), "rejected buy should not change worker state");
  1001	        assertFalse(world.getActiveMission(csId).active, "rejected buy should not install a mission");
  1002	    }
  1003	
  1004	    // -------------------------------------------------------------------------
  1005	    // Test 11: sell_creditsGold — after scheduled sell, clan.goldBalance > starter gold
  1006	    // -------------------------------------------------------------------------
  1007	
  1008	    function test_sell_creditsGold() public {
  1009	        address woodAddr = _setupMarket();
  1010	        uint32 clanId = _mintClan();
  1011	        uint32 csId = _firstCs(clanId);
  1012	
  1013	        // Clan starts with 20 wood in vault (starter pack)
  1014	        uint256 goldBefore = world.getClan(clanId).goldBalance;
  1015	
  1016	        // Submit sell order — clansman travels to Unicorn Town then executes when the market mission settles
  1017	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
  1018	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "sell order should be accepted");
  1019	
  1020	        // Find out which tick the action fires
  1021	        Mission memory m = world.getActiveMission(csId);
  1022	        uint64 executeAtTick = m.settlesAtTick;
  1023	        uint256 expectedGoldOut = woodPool.getAmountOutForExactIn(5e18);
  1024	
  1025	        // Advance ticks until the heartbeat before executeAtTick
  1026	        uint64 curTick = world.getWorldState().currentTick;
  1027	        for (uint256 i = 0; i < uint256(executeAtTick - curTick); i++) {
  1028	            _advanceTick();
  1029	        }
  1030	
  1031	        vm.expectEmit(true, false, false, true);
  1032	        emit IClanWorldEvents.ScheduledMarketActionExecuted(
  1033	            clanId,
  1034	            csId,
  1035	            ActionType.MarketSell,
  1036	            uint8(ResourceType.Wood),
  1037	            5e18,
  1038	            ClanWorldConstants.RESOURCE_GOLD,
  1039	            expectedGoldOut,
  1040	            executeAtTick
  1041	        );
  1042	        _advanceTick();
  1043	
  1044	        // Settle clan to apply any mission resolution
  1045	        world.settleClan(clanId);
  1046	
  1047	        uint256 goldAfter = world.getClan(clanId).goldBalance;
  1048	        assertGt(goldAfter, goldBefore, "gold should increase after sell");
  1049	    }
  1050	
  1051	    // -------------------------------------------------------------------------
  1052	    // Test 12: buy_debitsGold — after scheduled buy, gold decreases, vault resource increases
  1053	    // -------------------------------------------------------------------------
  1054	
  1055	    function test_buy_debitsGold() public {
  1056	        address woodAddr = _setupMarket();
  1057	        uint32 clanId = _mintClan();
  1058	
  1059	        // Give clan ample gold for the buy
  1060	        // We need a second clansman to do the buy while first does something else
  1061	        ClanFullView memory view_ = world.getClanFullView(clanId);
  1062	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
  1063	
  1064	        uint256 goldBefore = world.getClan(clanId).goldBalance;
  1065	        uint256 vaultWoodBefore = world.getClan(clanId).vaultWood;
  1066	
  1067	        // Submit buy order for 1e18 wood, maxGoldIn = generous 500e18
  1068	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 500e18);
  1069	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
  1070	
  1071	        Mission memory m = world.getActiveMission(csId);
  1072	        uint64 executeAtTick = m.settlesAtTick;
  1073	
  1074	        uint64 curTick = world.getWorldState().currentTick;
  1075	        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
  1076	        for (uint256 i = 0; i < ticksNeeded; i++) {
  1077	            _advanceTick();
  1078	        }
  1079	
  1080	        world.settleClan(clanId);
  1081	
  1082	        uint256 goldAfter = world.getClan(clanId).goldBalance;
  1083	        uint256 vaultWoodAfter = world.getClan(clanId).vaultWood;
  1084	        assertLt(goldAfter, goldBefore, "gold should decrease after buy");
  1085	        assertGt(vaultWoodAfter, vaultWoodBefore, "vault wood should increase after buy");
  1086	    }
  1087	
  1088	    // -------------------------------------------------------------------------
  1089	    // Test 13: buy_maxGoldIn — buy fails with ERR_MAX_GOLD_IN_EXCEEDED
  1090	    // -------------------------------------------------------------------------
  1091	
  1092	    function test_buy_maxGoldIn() public {
  1093	        address woodAddr = _setupMarket();
  1094	        uint32 clanId = _mintClan();
  1095	        uint32 csId = _firstCs(clanId);
  1096	
  1097	        // maxGoldIn = 0 (will always be exceeded for any nonzero buy)
  1098	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
  1099	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "order submission should succeed");
  1100	
  1101	        Mission memory m = world.getActiveMission(csId);
  1102	        uint64 executeAtTick = m.settlesAtTick;
  1103	        uint64 curTick = world.getWorldState().currentTick;
  1104	
  1105	        // Advance all ticks UP TO (but not including) the execute tick
  1106	        if (executeAtTick > curTick) {
  1107	            for (uint256 i = 0; i < uint256(executeAtTick - curTick); i++) {
  1108	                _advanceTick();
  1109	            }
  1110	        }
  1111	
  1112	        // Now the next heartbeat will close executeAtTick — that's when MarketActionFailed fires
  1113	        // Place expectEmit right before the final heartbeat
  1114	        vm.expectEmit(true, false, false, true);
  1115	        emit IClanWorldEvents.MarketActionFailed(
  1116	            clanId,
  1117	            csId,
  1118	            ActionType.MarketBuy,
  1119	            MarketExecutionMode.Scheduled,
  1120	            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
  1121	            executeAtTick
  1122	        );
  1123	        _advanceTick();
  1124	
  1125	        Clansman memory cs = world.getClansman(csId);
  1126	
  1127	        // Verify gold balance unchanged (buy failed)
  1128	        assertEq(world.getClan(clanId).goldBalance, 3e18, "gold should be unchanged after failed buy");
  1129	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
  1130	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
  1131	        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
  1132	    }
  1133	
  1134	    function test_scheduledMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
  1135	        address woodAddr = _setupMarket();
  1136	        uint32 clanId = _mintClan();
  1137	        uint32 csId = _firstCs(clanId);
  1138	
  1139	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 7e18, 10e18);
  1140	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
  1141	
  1142	        Mission memory m = world.getActiveMission(csId);
  1143	        uint64 executeAtTick = m.settlesAtTick;
  1144	        Clan memory beforeClan = world.getClan(clanId);
  1145	
  1146	        _advanceUntilNextHeartbeatCloses(executeAtTick);
  1147	        vm.expectEmit(true, false, false, true);
  1148	        emit IClanWorldEvents.MarketActionFailed(
  1149	            clanId,
  1150	            csId,
  1151	            ActionType.MarketBuy,
  1152	            MarketExecutionMode.Scheduled,
  1153	            StatusCode.ERR_NOT_ENOUGH_GOLD,
  1154	            executeAtTick
  1155	        );
  1156	        _advanceTick();
  1157	
  1158	        Clan memory afterClan = world.getClan(clanId);
  1159	        Clansman memory cs = world.getClansman(csId);
  1160	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
  1161	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
  1162	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
  1163	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
  1164	        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
  1165	    }
  1166	
  1167	    function test_scheduledMarketBuy_overCapacityRejectsAtSubmit() public {
  1168	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  1169	        world = harness;
  1170	        address woodAddr = _setupMarket();
  1171	        uint32 clanId = _mintClan();
  1172	        uint32 csId = _firstCs(clanId);
  1173	        harness.setCarry(csId, ClanWorldConstants.WOOD_CAP - 5e17, 0, 0, 0);
  1174	
  1175	        Clan memory beforeClan = world.getClan(clanId);
  1176	        Clansman memory beforeCs = world.getClansman(csId);
  1177	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 10e18);
  1178	
  1179	        Clan memory afterClan = world.getClan(clanId);
  1180	        Clansman memory cs = world.getClansman(csId);
  1181	
  1182	        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
  1183	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
  1184	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "rejected buy should not credit resources");
  1185	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "rejected buy should not debit gold");
  1186	        assertEq(cs.cooldownEndsAtTs, beforeCs.cooldownEndsAtTs, "rejected buy should not consume cooldown");
  1187	        assertEq(uint8(cs.state), uint8(beforeCs.state), "rejected buy should not change worker state");
  1188	        assertFalse(world.getActiveMission(csId).active, "rejected buy should not install a mission");
  1189	    }
  1190	
  1191	    function test_scheduledMarketBuy_insufficientLiquidityFailsAndConsumesCooldown() public {
  1192	        address woodAddr = _setupMarketWithWoodSeed(5e18);
  1193	        uint32 clanId = _mintClan();
  1194	        uint32 csId = _firstCs(clanId);
  1195	
  1196	        OrderResult[] memory r =
  1197	            _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 6e18, type(uint256).max);
  1198	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
  1199	
  1200	        Mission memory m = world.getActiveMission(csId);
  1201	        uint64 executeAtTick = m.settlesAtTick;
  1202	        Clan memory beforeClan = world.getClan(clanId);
  1203	
  1204	        _advanceUntilNextHeartbeatCloses(executeAtTick);
  1205	        vm.expectEmit(true, false, false, true);
  1206	        emit IClanWorldEvents.MarketActionFailed(
  1207	            clanId,
  1208	            csId,
  1209	            ActionType.MarketBuy,
  1210	            MarketExecutionMode.Scheduled,
  1211	            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
  1212	            executeAtTick
  1213	        );
  1214	        _advanceTick();
  1215	
  1216	        Clan memory afterClan = world.getClan(clanId);
  1217	        Clansman memory cs = world.getClansman(csId);
  1218	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "failed buy should not credit resources");
  1219	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "failed buy should not debit gold");
  1220	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "failed scheduled buy should consume cooldown");
  1221	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "failed scheduled worker waits");
  1222	        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
  1223	    }
  1224	
  1225	    // -------------------------------------------------------------------------
  1226	    // Test 14: scheduledMarket_deletedAfterHeartbeat
  1227	    // -------------------------------------------------------------------------
  1228	
  1229	    function test_scheduledMarket_deletedAfterHeartbeat() public {
  1230	        address woodAddr = _setupMarket();
  1231	        uint32 clanId = _mintClan();
  1232	        uint32 csId = _firstCs(clanId);
  1233	
  1234	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
  1235	        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
  1236	
  1237	        Mission memory m = world.getActiveMission(csId);
  1238	        uint64 executeAtTick = m.settlesAtTick;
  1239	
  1240	        uint64 curTick = world.getWorldState().currentTick;
  1241	        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
  1242	        for (uint256 i = 0; i < ticksNeeded; i++) {
  1243	            _advanceTick();
  1244	        }
  1245	
  1246	        // Queue should be empty after heartbeat processes it
  1247	        assertEq(
  1248	            world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be empty after heartbeat"
  1249	        );
  1250	    }
  1251	
  1252	    function test_scheduledMarket_sameTypeRetask_skipsStaleNonce() public {
  1253	        address woodAddr = _setupMarket();
  1254	        uint32 clanId = _mintClan();
  1255	        uint32 csId = _firstCs(clanId);
  1256	
  1257	        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
  1258	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first sell order should be accepted");
  1259	        Mission memory oldMission = world.getActiveMission(csId);
  1260	        uint64 oldExecuteAtTick = oldMission.settlesAtTick;
  1261	
  1262	        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
  1263	        _advanceTick();
  1264	
  1265	        OrderResult[] memory r2 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 2e18, 0);
  1266	        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "replacement sell order should be accepted");
  1267	        Mission memory newMission = world.getActiveMission(csId);
  1268	        uint64 newExecuteAtTick = newMission.settlesAtTick;
  1269	        assertGt(newMission.nonce, oldMission.nonce, "replacement should bump nonce");
  1270	
  1271	        assertEq(
  1272	            world.getScheduledMarketActionsForTick(oldExecuteAtTick).length, 0, "old mission not queued before settle"
  1273	        );
  1274	        assertEq(
  1275	            world.getScheduledMarketActionsForTick(newExecuteAtTick).length, 0, "new mission not queued before settle"
  1276	        );
  1277	
  1278	        uint256 goldBefore = world.getClan(clanId).goldBalance;
  1279	
  1280	        while (world.getWorldState().currentTick <= oldExecuteAtTick) {
  1281	            _advanceTick();
  1282	        }
  1283	        assertEq(world.getClan(clanId).goldBalance, goldBefore, "stale sell must not execute");
  1284	
  1285	        while (world.getWorldState().currentTick <= newExecuteAtTick) {
  1286	            _advanceTick();
  1287	        }
  1288	        assertGt(world.getClan(clanId).goldBalance, goldBefore, "replacement sell should execute");
  1289	    }
  1290	
  1291	    function test_scheduledMarket_executesAllActionsForClosedTick() public {
  1292	        address woodAddr = _setupMarket();
  1293	        uint32[] memory distOneClans = new uint32[](12);
  1294	        uint32[] memory distTwoClans = new uint32[](12);
  1295	        uint256 distOneCount;
  1296	        uint256 distTwoCount;
  1297	
  1298	        for (uint256 i = 0; i < 12; i++) {
  1299	            uint32 clanId = _mintClan();
  1300	            ClanFullView memory view_ = world.getClanFullView(clanId);
  1301	            (uint8 travelTicks,) = world.quoteTravel(view_.clan.clan.baseRegion, ClanWorldConstants.REGION_UNICORN_TOWN);
  1302	            if (travelTicks == 1) {
  1303	                distOneClans[distOneCount++] = clanId;
  1304	            } else if (travelTicks == 2) {
  1305	                distTwoClans[distTwoCount++] = clanId;
  1306	            }
  1307	        }
  1308	
  1309	        uint256 totalQueued;
  1310	        for (uint256 i = 0; i < distTwoCount; i++) {
  1311	            totalQueued += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
  1312	        }
  1313	
  1314	        _advanceTick();
  1315	
  1316	        for (uint256 i = 0; i < distOneCount; i++) {
  1317	            totalQueued += _submitAllClanMarketSells(distOneClans[i], woodAddr);
  1318	        }
  1319	
  1320	        uint64 executeAtTick = 3;
  1321	        bytes32 executedSig =
  1322	            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
  1323	
  1324	        _advanceTick(); // close tick 1
  1325	        _advanceTick(); // close tick 2
  1326	
  1327	        vm.recordLogs();
  1328	        _advanceTick(); // close tick 3 and execute every scheduled action for the tick
  1329	        Vm.Log[] memory logs = vm.getRecordedLogs();
  1330	
  1331	        uint256 executedCount;
  1332	        for (uint256 i = 0; i < logs.length; i++) {
  1333	            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != executedSig) {
  1334	                continue;
  1335	            }
  1336	            (,,,,,, uint64 settledAtTick) =
  1337	                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
  1338	            if (settledAtTick != executeAtTick) continue;
  1339	            executedCount++;
  1340	        }
  1341	
  1342	        assertEq(executedCount, totalQueued, "all same-tick actions should execute");
  1343	        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "queue should be deleted");
  1344	    }
  1345	
  1346	    // -------------------------------------------------------------------------
  1347	    // Test 15: scheduledMarket_fifo — same-tick sells execute in submission order
  1348	    // -------------------------------------------------------------------------
  1349	
  1350	    function test_scheduledMarket_fifo() public {
  1351	        address woodAddr = _setupMarket();
  1352	        uint32 clanId = _mintClan();
  1353	        ClanFullView memory view_ = world.getClanFullView(clanId);
  1354	
  1355	        ClanOrder[] memory orders = new ClanOrder[](3);
  1356	        for (uint256 i = 0; i < orders.length; i++) {
  1357	            orders[i] = ClanOrder({
  1358	                clansmanId: view_.clansmen[i].clansman.clansman.clansmanId,
  1359	                gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
  1360	                action: ActionType.MarketSell,
  1361	                targetClanId: 0,
  1362	                marketToken: woodAddr,
  1363	                marketAmount: uint256(i + 1) * 1e18,
  1364	                maxGoldIn: 0
  1365	            });
  1366	        }
  1367	        vm.prank(elder);
  1368	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
  1369	        for (uint256 i = 0; i < results.length; i++) {
  1370	            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "sell order ok");
  1371	        }
  1372	
  1373	        Mission memory m = world.getActiveMission(orders[0].clansmanId);
  1374	        uint64 executeAtTick = m.settlesAtTick;
  1375	        bytes32 executedSig =
  1376	            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
  1377	
  1378	        while (world.getWorldState().currentTick < executeAtTick) {
  1379	            _advanceTick();
  1380	        }
  1381	
  1382	        vm.recordLogs();
  1383	        _advanceTick();
  1384	        Vm.Log[] memory logs = vm.getRecordedLogs();
  1385	
  1386	        uint256 executedCount;
  1387	        for (uint256 i = 0; i < logs.length; i++) {
  1388	            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != executedSig) {
  1389	                continue;
  1390	            }
  1391	            (,, uint8 resourceIn, uint256 amountIn,,, uint64 settledAtTick) =
  1392	                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
  1393	            if (settledAtTick != executeAtTick) continue;
  1394	
  1395	            assertEq(uint32(uint256(logs[i].topics[1])), clanId, "same clan should execute");
  1396	            assertEq(resourceIn, uint8(ResourceType.Wood), "sell should input wood");
  1397	            assertEq(amountIn, uint256(executedCount + 1) * 1e18, "execution should be FIFO");
  1398	            executedCount++;
  1399	        }
  1400	
  1401	        assertEq(executedCount, 3, "three same-tick sells should execute");
  1402	        assertGt(world.getClan(clanId).goldBalance, 3e18, "clan should gain gold from sells");
  1403	    }
  1404	
  1405	    // -------------------------------------------------------------------------
  1406	    // Test 16: twoClan_sellBuyCycle — clan A sells wood, clan B buys wood; both succeed
  1407	    // -------------------------------------------------------------------------
  1408	
  1409	    function test_twoClan_sellBuyCycle() public {
  1410	        address woodAddr = _setupMarket();
  1411	
  1412	        uint32 clanId1 = _mintClan();
  1413	        vm.prank(elder2);
  1414	        (uint32 clanId2,) = world.mintClan(elder2);
  1415	
  1416	        uint32 csId1 = _firstCs(clanId1);
  1417	        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
  1418	
  1419	        // Clan 1: sell 5e18 wood
  1420	        ClanOrder[] memory sellOrders = new ClanOrder[](1);
  1421	        sellOrders[0] = ClanOrder({
  1422	            clansmanId: csId1,
  1423	            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
  1424	            action: ActionType.MarketSell,
  1425	            targetClanId: 0,
  1426	            marketToken: woodAddr,
  1427	            marketAmount: 5e18,
  1428	            maxGoldIn: 0
  1429	        });
  1430	        vm.prank(elder);
  1431	        world.submitClanOrders(clanId1, sellOrders);
  1432	
  1433	        // Clan 2: buy 1e18 wood with maxGoldIn = 100e18
  1434	        ClanOrder[] memory buyOrders = new ClanOrder[](1);
  1435	        buyOrders[0] = ClanOrder({
  1436	            clansmanId: csId2,
  1437	            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
  1438	            action: ActionType.MarketBuy,
  1439	            targetClanId: 0,
  1440	            marketToken: woodAddr,
  1441	            marketAmount: 1e18,
  1442	            maxGoldIn: 100e18
  1443	        });
  1444	        vm.prank(elder2);
  1445	        world.submitClanOrders(clanId2, buyOrders);
  1446	
  1447	        Mission memory m1 = world.getActiveMission(csId1);
  1448	        Mission memory m2 = world.getActiveMission(csId2);
  1449	
  1450	        uint64 maxTick = m1.settlesAtTick > m2.settlesAtTick ? m1.settlesAtTick : m2.settlesAtTick;
  1451	        uint64 curTick = world.getWorldState().currentTick;
  1452	        uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
  1453	        bytes32 scheduledSig =
  1454	            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
  1455	        vm.recordLogs();
  1456	        for (uint256 i = 0; i < ticksNeeded; i++) {
  1457	            _advanceTick();
  1458	        }
  1459	        Vm.Log[] memory logs = vm.getRecordedLogs();
  1460	

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/SeedPools.t.sol | nl -ba | sed -n '1,180p'; git show 9f93593:packages/contracts/test/ResourceBoundaryTokens.t.sol | nl -ba | sed -n '1,140p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {MinimalERC20} from "../src/MinimalERC20.sol";
     7	import {StubPool} from "../src/StubPool.sol";
     8	import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
     9	
    10	contract SeedPoolsTest is Test {
    11	    ClanWorld world;
    12	    MinimalERC20 woodToken;
    13	    MinimalERC20 ironToken;
    14	    MinimalERC20 wheatToken;
    15	    MinimalERC20 fishToken;
    16	    MinimalERC20 goldToken;
    17	    MinimalERC20 blueprintToken;
    18	    StubPool woodPool;
    19	    StubPool ironPool;
    20	    StubPool wheatPool;
    21	    StubPool fishPool;
    22	
    23	    function setUp() public {
    24	        world = new ClanWorld();
    25	
    26	        woodToken = new MinimalERC20("Wood", "WOOD");
    27	        ironToken = new MinimalERC20("Iron", "IRON");
    28	        wheatToken = new MinimalERC20("Wheat", "WHEAT");
    29	        fishToken = new MinimalERC20("Fish", "FISH");
    30	        goldToken = new MinimalERC20("Gold", "GOLD");
    31	        blueprintToken = new MinimalERC20("BPRT", "BPRT");
    32	
    33	        address engine = address(world);
    34	        woodPool = new StubPool(address(woodToken), address(goldToken), engine);
    35	        wheatPool = new StubPool(address(wheatToken), address(goldToken), engine);
    36	        fishPool = new StubPool(address(fishToken), address(goldToken), engine);
    37	        ironPool = new StubPool(address(ironToken), address(goldToken), engine);
    38	
    39	        address[6] memory tokens = [
    40	            address(woodToken),
    41	            address(ironToken),
    42	            address(wheatToken),
    43	            address(fishToken),
    44	            address(goldToken),
    45	            address(blueprintToken)
    46	        ];
    47	        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
    48	        world.initTreasury(tokens, pools);
    49	
    50	        _seedTreasuryAndApprove();
    51	        world.seedPools(_seedConfig());
    52	    }
    53	
    54	    function test_deploysFourPoolsAndGetterReturnsExpectedPools() public {
    55	        assertEq(world.getPool(uint8(ResourceType.Wood)), address(woodPool), "wood pool");
    56	        assertEq(world.getPool(uint8(ResourceType.Wheat)), address(wheatPool), "wheat pool");
    57	        assertEq(world.getPool(uint8(ResourceType.Fish)), address(fishPool), "fish pool");
    58	        assertEq(world.getPool(uint8(ResourceType.Iron)), address(ironPool), "iron pool");
    59	    }
    60	
    61	    function test_treasurySeedingTransfersExpectedPoolBalances() public {
    62	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
    63	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
    64	
    65	        _assertPoolSeeded(woodToken, woodPool, resSeed, goldSeed, "wood");
    66	        _assertPoolSeeded(wheatToken, wheatPool, resSeed, goldSeed, "wheat");
    67	        _assertPoolSeeded(fishToken, fishPool, resSeed, goldSeed, "fish");
    68	        _assertPoolSeeded(ironToken, ironPool, resSeed, goldSeed, "iron");
    69	        assertEq(goldToken.balanceOf(address(this)), 0, "treasury gold fully seeded");
    70	    }
    71	
    72	    function test_kInvariantHoldsAfterEachSwap() public {
    73	        uint256 kBefore = _k(woodPool);
    74	
    75	        vm.prank(address(world));
    76	        woodPool.swapExactInForOut(10e18, 1);
    77	        uint256 kAfterSell = _k(woodPool);
    78	        assertGe(kAfterSell, kBefore, "k after exact-in sell");
    79	
    80	        vm.prank(address(world));
    81	        woodPool.swapExactOutForInWithMaxIn(3e18, 10e18);
    82	        uint256 kAfterBuy = _k(woodPool);
    83	        assertGe(kAfterBuy, kAfterSell, "k after exact-out buy");
    84	    }
    85	
    86	    function test_pricePreviewMatchesActualSwap() public {
    87	        uint256 amountIn = 25e18;
    88	        uint256 preview = world.getPrice(uint8(ResourceType.Wood), amountIn);
    89	
    90	        vm.prank(address(world));
    91	        uint256 actual = woodPool.swapExactInForOut(amountIn, 0);
    92	
    93	        assertEq(actual, preview, "preview should match exact-in swap");
    94	    }
    95	
    96	    function _seedTreasuryAndApprove() internal {
    97	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
    98	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
    99	        uint256 totalGoldSeed = goldSeed * 4;
   100	
   101	        woodToken.seedTreasury(address(this), resSeed);
   102	        wheatToken.seedTreasury(address(this), resSeed);
   103	        fishToken.seedTreasury(address(this), resSeed);
   104	        ironToken.seedTreasury(address(this), resSeed);
   105	        goldToken.seedTreasury(address(this), totalGoldSeed);
   106	
   107	        woodToken.approve(address(world), resSeed);
   108	        wheatToken.approve(address(world), resSeed);
   109	        fishToken.approve(address(world), resSeed);
   110	        ironToken.approve(address(world), resSeed);
   111	        goldToken.approve(address(world), totalGoldSeed);
   112	    }
   113	
   114	    function _seedConfig() internal view returns (PoolSeedConfig memory) {
   115	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
   116	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
   117	        return PoolSeedConfig({
   118	            woodSeed: resSeed,
   119	            wheatSeed: resSeed,
   120	            fishSeed: resSeed,
   121	            ironSeed: resSeed,
   122	            goldSeedForWood: goldSeed,
   123	            goldSeedForWheat: goldSeed,
   124	            goldSeedForFish: goldSeed,
   125	            goldSeedForIron: goldSeed
   126	        });
   127	    }
   128	
   129	    function _assertPoolSeeded(
   130	        MinimalERC20 resourceToken,
   131	        StubPool pool,
   132	        uint256 expectedResource,
   133	        uint256 expectedGold,
   134	        string memory label
   135	    ) internal view {
   136	        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
   137	
   138	        assertEq(resourceReserve, expectedResource, string.concat(label, " resource reserve"));
   139	        assertEq(goldReserve, expectedGold, string.concat(label, " gold reserve"));
   140	        assertEq(resourceToken.balanceOf(address(pool)), expectedResource, string.concat(label, " resource balance"));
   141	        assertEq(goldToken.balanceOf(address(pool)), expectedGold, string.concat(label, " gold balance"));
   142	    }
   143	
   144	    function _k(StubPool pool) internal view returns (uint256) {
   145	        (uint256 resourceReserve, uint256 goldReserve) = pool.getReserves();
   146	        return resourceReserve * goldReserve;
   147	    }
   148	}
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {MinimalERC20} from "../src/MinimalERC20.sol";
     7	import {StubPool} from "../src/StubPool.sol";
     8	import {ResourceType} from "../src/IClanWorld.sol";
     9	
    10	contract ResourceBoundaryTokensTest is Test {
    11	    event ResourceMinted(uint8 indexed resourceType, address indexed to, uint256 amount);
    12	    event ResourceBurned(uint8 indexed resourceType, address indexed from, uint256 amount);
    13	
    14	    ClanWorld world;
    15	    MinimalERC20 woodToken;
    16	    MinimalERC20 ironToken;
    17	    MinimalERC20 wheatToken;
    18	    MinimalERC20 fishToken;
    19	    MinimalERC20 goldToken;
    20	    MinimalERC20 blueprintToken;
    21	
    22	    function setUp() public {
    23	        world = new ClanWorld();
    24	        woodToken = new MinimalERC20("Wood", "WOOD");
    25	        ironToken = new MinimalERC20("Iron", "IRON");
    26	        wheatToken = new MinimalERC20("Wheat", "WHEAT");
    27	        fishToken = new MinimalERC20("Fish", "FISH");
    28	        goldToken = new MinimalERC20("Gold", "GOLD");
    29	        blueprintToken = new MinimalERC20("BPRT", "BPRT");
    30	
    31	        woodToken.configureBoundary(uint8(ResourceType.Wood), address(world));
    32	        ironToken.configureBoundary(uint8(ResourceType.Iron), address(world));
    33	        wheatToken.configureBoundary(uint8(ResourceType.Wheat), address(world));
    34	        fishToken.configureBoundary(uint8(ResourceType.Fish), address(world));
    35	    }
    36	
    37	    function test_getResourceTokenReturnsConfiguredBoundaryTokens() public {
    38	        StubPool woodPool = new StubPool(address(woodToken), address(goldToken), address(world));
    39	        StubPool wheatPool = new StubPool(address(wheatToken), address(goldToken), address(world));
    40	        StubPool fishPool = new StubPool(address(fishToken), address(goldToken), address(world));
    41	        StubPool ironPool = new StubPool(address(ironToken), address(goldToken), address(world));
    42	
    43	        address[6] memory tokens = [
    44	            address(woodToken),
    45	            address(ironToken),
    46	            address(wheatToken),
    47	            address(fishToken),
    48	            address(goldToken),
    49	            address(blueprintToken)
    50	        ];
    51	        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
    52	
    53	        world.initTreasury(tokens, pools);
    54	
    55	        assertEq(world.getResourceToken(uint8(ResourceType.Wood)), address(woodToken), "wood");
    56	        assertEq(world.getResourceToken(uint8(ResourceType.Iron)), address(ironToken), "iron");
    57	        assertEq(world.getResourceToken(uint8(ResourceType.Wheat)), address(wheatToken), "wheat");
    58	        assertEq(world.getResourceToken(uint8(ResourceType.Fish)), address(fishToken), "fish");
    59	    }
    60	
    61	    function test_onlyEngineCanMintAndBurnBoundaryTokens() public {
    62	        address holder = address(0xCAFE);
    63	
    64	        vm.expectRevert(bytes("MinimalERC20: not engine"));
    65	        woodToken.mint(holder, 10e18);
    66	
    67	        vm.expectEmit(true, true, false, true, address(woodToken));
    68	        emit ResourceMinted(uint8(ResourceType.Wood), holder, 10e18);
    69	        vm.prank(address(world));
    70	        woodToken.mint(holder, 10e18);
    71	
    72	        assertEq(woodToken.balanceOf(holder), 10e18, "minted balance");
    73	
    74	        vm.expectEmit(true, true, false, true, address(woodToken));
    75	        emit ResourceBurned(uint8(ResourceType.Wood), holder, 4e18);
    76	        vm.prank(address(world));
    77	        woodToken.burn(holder, 4e18);
    78	
    79	        assertEq(woodToken.balanceOf(holder), 6e18, "burned balance");
    80	    }
    81	
    82	    function test_seedTreasuryMintsStartingSupply() public {
    83	        address treasury = address(0xBEEF);
    84	
    85	        woodToken.seedTreasury(treasury, 1000e18);
    86	
    87	        assertEq(woodToken.balanceOf(treasury), 1000e18, "treasury balance");
    88	        assertEq(woodToken.totalSupply(), 1000e18, "total supply");
    89	    }
    90	
    91	    function test_seedTreasuryCanOnlyRunOnce() public {
    92	        address treasury = address(0xBEEF);
    93	
    94	        woodToken.seedTreasury(treasury, 1000e18);
    95	
    96	        vm.expectRevert(bytes("MinimalERC20: treasury seeded"));
    97	        woodToken.seedTreasury(treasury, 1e18);
    98	    }
    99	
   100	    function test_onlyDeployerCanSeedTreasury() public {
   101	        vm.prank(address(0xBAD));
   102	        vm.expectRevert(bytes("MinimalERC20: not deployer"));
   103	        woodToken.seedTreasury(address(0xBEEF), 1000e18);
   104	    }
   105	}

exec
/bin/bash -lc 'git show 9f93593:packages/contracts/src/ClanWorld.sol | rg -n "function heartbeat|_settleCompletingMissions|_resolveAttackingBandits|_advanceBanditStates|TickAdvanced"' in /home/claude/code/clan-world
 succeeded in 0ms:
878:    function heartbeat() external override nonReentrant {
893:        _settleCompletingMissions(closedTick);
909:        emit TickAdvanced(closedTick, newTick, newSeed);
915:    function _settleCompletingMissions(uint64 tick) internal {

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '860,945p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   860	        m.active = false;
   861	        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
   862	    }
   863	
   864	    // =========================================================================
   865	    // WORLD PROGRESSION
   866	    // =========================================================================
   867	
   868	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
   869	    ///         Execution order per spec §4.2 (CEI-safe):
   870	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
   871	    ///         Seed:      closedTick seed derived and published before step 1 so
   872	    ///                    settlement RNG reads real entropy, not zero.
   873	    ///         1. Settle missions completing this tick.
   874	    ///         2. Execute scheduled market actions for closedTick (external calls).
   875	    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
   876	    ///         4. Resolve world events (season boundary, winter transitions).
   877	    ///         5. Increment tick and publish (seed already written above).
   878	    function heartbeat() external override nonReentrant {
   879	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
   880	
   881	        uint64 closedTick = _world.currentTick;
   882	
   883	        // CEI: update rate-limit guard before any external calls
   884	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
   885	
   886	        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
   887	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
   888	        _tickSeeds[closedTick] = newSeed;
   889	        _world.currentTickSeed = newSeed;
   890	
   891	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
   892	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   893	        _settleCompletingMissions(closedTick);
   894	
   895	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
   896	        _executeScheduledMarketActions(closedTick);
   897	
   898	        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
   899	        // TODO Phase 3: _settleClansNearBandit(closedTick);
   900	
   901	        // Step 4: Resolve world events (season boundary, winter transitions).
   902	        _resolveWorldEvents(closedTick);
   903	
   904	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
   905	        uint64 newTick = closedTick + 1;
   906	        _world.currentTick = newTick;
   907	        _world.nextHeartbeatAtTick = newTick + 1;
   908	
   909	        emit TickAdvanced(closedTick, newTick, newSeed);
   910	    }
   911	
   912	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
   913	    ///      Called from heartbeat before market execution and tick increment.
   914	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   915	    function _settleCompletingMissions(uint64 tick) internal {
   916	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   917	            uint32 clanId = _allClanIds[i];
   918	            Clan storage clan = _clans[clanId];
   919	            if (clan.clanState == ClanState.DEAD) continue;
   920	
   921	            uint32[] storage csIds = _clanClansmanIds[clanId];
   922	            for (uint256 j = 0; j < csIds.length; j++) {
   923	                Clansman storage cs = _clansmen[csIds[j]];
   924	                if (cs.state == ClansmanState.DEAD) continue;
   925	
   926	                Mission storage m = _missions[cs.clansmanId];
   927	                if (!m.active) continue;
   928	                if (m.settlesAtTick != tick) continue; // not due this tick
   929	
   930	                // Settle this mission using the single-tick range [tick, tick+1).
   931	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   932	            }
   933	        }
   934	    }
   935	
   936	    /// @dev Resolve world events for the tick that was just closed.
   937	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
   938	    function _resolveWorldEvents(uint64 closedTick) internal {
   939	        uint64 newTick = closedTick + 1;
   940	
   941	        // --- season boundary ---
   942	        if (newTick >= _world.seasonEndTick) {
   943	            _world.currentSeasonNumber += 1;
   944	            _world.seasonStartTick = _world.seasonEndTick;
   945	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;

exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr198-codereview-codex-5-5.md" in /home/claude/code/clan-world
 succeeded in 0ms:
Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.5
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019dda65-c2bf-72b3-84f9-fc6dac9c8267
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output review in requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.

Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.

## Your task
This is a COHESIVE PHASE — review the integrated whole. Look for:

1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
3. SECURITY — re-entrancy in pool swaps, slippage protection, MEV / front-running
4. DATA-flow — vault accounting bridge to ERC20 boundary, treasury seeding integrity
5. Integration risks — interaction with Phase 5 vault state + Phase 9 bandit attack timing
6. Test coverage on the integration seams (immediate + scheduled, both buy + sell, all 4 resources)

USE PARALLEL TOOL CALLS. Read all changed files. Trace state machines. Don't just skim.

## Output format

# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns.

## HIGH severity findings

## MEDIUM severity findings

## LOW severity findings

## Cross-cutting observations

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..1c59b75 100644
--- a/packages/contracts/abi/IClanWorld.json
+++ b/packages/contracts/abi/IClanWorld.json
@@ -7,6 +7,25 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "getActionDuration",
+      "inputs": [
+        {
+          "name": "action",
+          "type": "uint8",
+          "internalType": "enum ActionType"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "pure"
+    },
     {
       "type": "function",
       "name": "getActiveBanditView",
@@ -232,83 +251,6 @@
       ],
       "stateMutability": "view"
     },
-    {
-      "type": "function",
-      "name": "getMissionTiming",
-      "inputs": [
-        {
-          "name": "clanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        },
-        {
-          "name": "clansmanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "submitted",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "executes",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "settles",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "view"
-    },
-    {
-      "type": "function",
-      "name": "getActionDuration",
-      "inputs": [
-        {
-          "name": "action",
-          "type": "uint8",
-          "internalType": "enum ActionType"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
-    {
-      "type": "function",
-      "name": "getTravelTicks",
-      "inputs": [
-        {
-          "name": "fromRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        },
-        {
-          "name": "toRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
     {
       "type": "function",
       "name": "getBanditTargetPreview",
@@ -1639,6 +1581,83 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getMissionTiming",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "clansmanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "submitted",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "executes",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "settles",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getPool",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],

exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr198-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr198-codereview-codex-5-4.md" in /home/claude/code/clan-world
 succeeded in 0ms:
Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.4
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019dda65-bc9d-74b1-898f-44824da0a158
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output review in requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.

Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.

## Your task
This is a COHESIVE PHASE — review the integrated whole. Look for:

1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
3. SECURITY — re-entrancy in pool swaps, slippage protection, MEV / front-running
4. DATA-flow — vault accounting bridge to ERC20 boundary, treasury seeding integrity
5. Integration risks — interaction with Phase 5 vault state + Phase 9 bandit attack timing
6. Test coverage on the integration seams (immediate + scheduled, both buy + sell, all 4 resources)

USE PARALLEL TOOL CALLS. Read all changed files. Trace state machines. Don't just skim.

## Output format

# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns.

## HIGH severity findings

## MEDIUM severity findings

## LOW severity findings

## Cross-cutting observations

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..1c59b75 100644
--- a/packages/contracts/abi/IClanWorld.json
+++ b/packages/contracts/abi/IClanWorld.json
@@ -7,6 +7,25 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "getActionDuration",
+      "inputs": [
+        {
+          "name": "action",
+          "type": "uint8",
+          "internalType": "enum ActionType"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "pure"
+    },
     {
       "type": "function",
       "name": "getActiveBanditView",
@@ -232,83 +251,6 @@
       ],
       "stateMutability": "view"
     },
-    {
-      "type": "function",
-      "name": "getMissionTiming",
-      "inputs": [
-        {
-          "name": "clanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        },
-        {
-          "name": "clansmanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "submitted",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "executes",
-          "type": "uint64",
-          "internalType": "uint64"
-        },
-        {
-          "name": "settles",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "view"
-    },
-    {
-      "type": "function",
-      "name": "getActionDuration",
-      "inputs": [
-        {
-          "name": "action",
-          "type": "uint8",
-          "internalType": "enum ActionType"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
-    {
-      "type": "function",
-      "name": "getTravelTicks",
-      "inputs": [
-        {
-          "name": "fromRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        },
-        {
-          "name": "toRegion",
-          "type": "uint8",
-          "internalType": "uint8"
-        }
-      ],
-      "outputs": [
-        {
-          "name": "",
-          "type": "uint64",
-          "internalType": "uint64"
-        }
-      ],
-      "stateMutability": "pure"
-    },
     {
       "type": "function",
       "name": "getBanditTargetPreview",
@@ -1639,6 +1581,83 @@
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getMissionTiming",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        },
+        {
+          "name": "clansmanId",
+          "type": "uint32",
+          "internalType": "uint32"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "submitted",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "executes",
+          "type": "uint64",
+          "internalType": "uint64"
+        },
+        {
+          "name": "settles",
+          "type": "uint64",
+          "internalType": "uint64"
+        }
+      ],
+      "stateMutability": "view"
+    },
+    {
+      "type": "function",
+      "name": "getPool",
+      "inputs": [
+        {
+          "name": "resourceType",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "address",
+          "internalType": "address"
+        }
+      ],

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1010,1135p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1010	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1011	        uint8[6] memory spawnRegions = [
  1012	            ClanWorldConstants.REGION_FOREST,
  1013	            ClanWorldConstants.REGION_MOUNTAINS,
  1014	            ClanWorldConstants.REGION_WEST_FARMS,
  1015	            ClanWorldConstants.REGION_EAST_FARMS,
  1016	            ClanWorldConstants.REGION_WEST_DOCKS,
  1017	            ClanWorldConstants.REGION_EAST_DOCKS
  1018	        ];
  1019	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1020	
  1021	        // Create clan
  1022	        Clan storage clan = _clans[clanId];
  1023	        clan.clanId = clanId;
  1024	        clan.iftTokenId = iftTokenId;
  1025	        clan.owner = to;
  1026	        clan.clanState = ClanState.ACTIVE;
  1027	        clan.baseRegion = baseRegion;
  1028	        clan.baseLevel = 1;
  1029	        clan.wallLevel = 0;
  1030	        clan.monumentLevel = 0;
  1031	        clan.livingClansmen = 4;
  1032	        clan.lastSettledTick = _world.currentTick;
  1033	        clan.starvationStartsAtTick = 0;
  1034	        clan.coldDamage = 0;
  1035	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1036	        clan.blueprintBalance = 0;
  1037	        // Starting vault per v4 spec §12.4
  1038	        clan.vaultWood = 20e18;
  1039	        clan.vaultIron = 0;
  1040	        clan.vaultWheat = 20e18;
  1041	        clan.vaultFish = 2e18;
  1042	
  1043	        // Wheat plots
  1044	        _wheatPlots[clanId][0] = WheatPlot({
  1045	            state: WheatPlotState.Harvestable,
  1046	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1047	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1048	            regrowUntilTick: 0
  1049	        });
  1050	        _wheatPlots[clanId][1] = WheatPlot({
  1051	            state: WheatPlotState.Harvestable,
  1052	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1053	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1054	            regrowUntilTick: 0
  1055	        });
  1056	
  1057	        // Create 4 clansmen
  1058	        for (uint256 i = 0; i < 4; i++) {
  1059	            uint32 csId = _nextClansmanId++;
  1060	            Clansman storage cs = _clansmen[csId];
  1061	            cs.clansmanId = csId;
  1062	            cs.clanId = clanId;
  1063	            cs.state = ClansmanState.WAITING;
  1064	            cs.currentRegion = baseRegion;
  1065	            cs.cooldownEndsAtTs = 0;
  1066	            cs.lastMissionNonce = 0;
  1067	            cs.carryWood = 0;
  1068	            cs.carryIron = 0;
  1069	            cs.carryWheat = 0;
  1070	            cs.carryFish = 0;
  1071	            _clanClansmanIds[clanId].push(csId);
  1072	        }
  1073	
  1074	        _allClanIds.push(clanId);
  1075	
  1076	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1077	        return (clanId, iftTokenId);
  1078	    }
  1079	
  1080	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1081	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1082	        external
  1083	        override
  1084	        nonReentrant
  1085	        returns (OrderResult[] memory results)
  1086	    {
  1087	        Clan storage clan = _clans[clanId];
  1088	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1089	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1090	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1091	
  1092	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1093	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1094	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  1095	        {
  1096	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1097	            if (_world.currentTick > lastSettled + 200) {
  1098	                results = new OrderResult[](orders.length);
  1099	                for (uint256 i = 0; i < orders.length; i++) {
  1100	                    results[i] = OrderResult({
  1101	                        clansmanId: orders[i].clansmanId,
  1102	                        status: StatusCode.ERR_INVALID_ACTION,
  1103	                        cooldownEndsAtTs: 0,
  1104	                        missionNonce: 0,
  1105	                        marketMode: MarketExecutionMode.None
  1106	                    });
  1107	                }
  1108	                return results;
  1109	            }
  1110	        }
  1111	
  1112	        // Lazy settle before processing orders
  1113	        _settleClan(clanId);
  1114	
  1115	        results = new OrderResult[](orders.length);
  1116	
  1117	        for (uint256 i = 0; i < orders.length; i++) {
  1118	            results[i] = _processOrder(clanId, clan, orders[i]);
  1119	        }
  1120	
  1121	        return results;
  1122	    }
  1123	
  1124	    struct OrderCtx {
  1125	        uint8 gotoRegion;
  1126	        uint8 fromRegion;
  1127	        bool isNoop;
  1128	        uint8 travelTicks;
  1129	        uint64 arrivalTick;
  1130	        uint64 newNonce;
  1131	        uint32 targetClanId;
  1132	        bool wasActive;
  1133	        uint64 oldNonce;
  1134	    }
  1135	

exec
/bin/bash -lc 'git show 9f93593:packages/contracts/src/ClanWorld.sol | rg -n "function settleClan|function settleClansman" -A45' in /home/claude/code/clan-world
 succeeded in 0ms:
980:    function settleClan(uint32 clanId) external override nonReentrant {
981-        _settleClan(clanId);
982-    }
983-
984-    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
985-    ///         Internally settles the entire clan (including upkeep) to guarantee
986-    ///         correct ordering and prevent double-settlement. Callers may call this
987-    ///         or settleClan interchangeably; both are safe and idempotent.
988:    function settleClansman(uint32 csId) external override nonReentrant {
989-        Clansman storage cs = _clansmen[csId];
990-        if (cs.clansmanId == 0) return;
991-        _settleClan(cs.clanId);
992-    }
993-
994-    /// @notice Finalize season. Phase 1 stub.
995-    function finalizeSeason() external override {
996-        // TODO Phase 3
997-    }
998-
999-    // =========================================================================
1000-    // CLAN LIFECYCLE
1001-    // =========================================================================
1002-
1003-    /// @notice Mint a new clan and spawn its homebase.
1004-    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
1005-        require(to != address(0), "ClanWorld: zero address");
1006-        require(_allClanIds.length < 12, "ClanWorld: max clans");
1007-        clanId = _nextClanId++;
1008-        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
1009-
1010-        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
1011-        uint8[6] memory spawnRegions = [
1012-            ClanWorldConstants.REGION_FOREST,
1013-            ClanWorldConstants.REGION_MOUNTAINS,
1014-            ClanWorldConstants.REGION_WEST_FARMS,
1015-            ClanWorldConstants.REGION_EAST_FARMS,
1016-            ClanWorldConstants.REGION_WEST_DOCKS,
1017-            ClanWorldConstants.REGION_EAST_DOCKS
1018-        ];
1019-        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
1020-
1021-        // Create clan
1022-        Clan storage clan = _clans[clanId];
1023-        clan.clanId = clanId;
1024-        clan.iftTokenId = iftTokenId;
1025-        clan.owner = to;
1026-        clan.clanState = ClanState.ACTIVE;
1027-        clan.baseRegion = baseRegion;
1028-        clan.baseLevel = 1;
1029-        clan.wallLevel = 0;
1030-        clan.monumentLevel = 0;
1031-        clan.livingClansmen = 4;
1032-        clan.lastSettledTick = _world.currentTick;
1033-        clan.starvationStartsAtTick = 0;

exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_spec.md | sed -n '520,600p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   520	
   521	## 5.4 Scheduled market action
   522	Scheduled market action is used when the clansman is not already eligible to trade immediately.
   523	
   524	Typical case:
   525	- worker must travel to Unicorn Town first
   526	- after arrival, the market action occupies one full action tick
   527	- Keeper executes the swap at the closing heartbeat of that action tick
   528	
   529	Example:
   530	- `goto Unicorn Town, action = sell wood`
   531	
   532	If the mission is not interrupted:
   533	- travel resolves first
   534	- one action tick in Unicorn Town is consumed
   535	- swap executes at that tick close
   536	- worker becomes `WAITING` in Unicorn Town
   537	
   538	## 5.5 Immediate market action
   539	Immediate market action is allowed if and only if all of the following are true at tx execution time:
   540	- clansman is physically in Unicorn Town
   541	- clansman is in `WAITING` state
   542	- clansman is not on cooldown
   543	- action params are valid
   544	- required inventory or gold balance is available
   545	
   546	If those conditions hold, an Elder may submit:
   547	- `goto = noop`
   548	- `action = market_buy` or `market_sell`
   549	
   550	and the swap executes immediately in that Elder tx.
   551	
   552	## 5.6 Immediate market action effects
   553	On successful immediate market action:
   554	- the swap executes immediately in normal blockchain tx order
   555	- the worker remains in Unicorn Town
   556	- the worker returns to `WAITING`
   557	- cooldown is consumed as if the worker had completed a normal mission action
   558	- mission metadata / nonce is updated like any other mission assignment
   559	
   560	Immediate market actions are therefore still **missions**, not an out-of-band bypass.
   561	
   562	## 5.7 Immediate vs scheduled collision ordering
   563	If immediate and scheduled market actions affect the same pool in the same tick:
   564	- immediate market actions execute against the current pool state at the time their Elder tx lands
   565	- scheduled market actions execute later, at the heartbeat that closes the tick, in deterministic FIFO order by mission commit order
   566	
   567	This intentionally allows a clansman already stationed in Unicorn Town to front-run scheduled trades that will execute at tick close.
   568	
   569	This is an intended feature of the game’s adversarial market design, not a bug.
   570	
   571	## 5.8 Scheduled market action ordering
   572	If multiple scheduled market actions mature in the same tick, Keeper executes them in deterministic FIFO order based on mission commit order.
   573	
   574	Each swap sees updated market state from earlier swaps in that sequence.
   575	
   576	## 5.9 Slippage rule for v1
   577	For v1, market actions execute with **no slippage guard**.
   578	
   579	Formally:
   580	- `minAmountOut = 0`
   581	- swaps do not revert due to price movement or adverse output
   582	
   583	This applies to:
   584	- scheduled market actions resolved at heartbeat close
   585	- immediate market actions executed in Elder txs while already in Unicorn Town
   586	
   587	This is intentional and preserves:
   588	- adversarial market play
   589	- MEV / front-running strategy
   590	- punishing but simple execution semantics
   591	- no swap-failure branching due to slippage in v1
   592	
   593	## 5.10 Failed market actions
   594	If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
   595	- no partial swap occurs
   596	- clansman retains their inventory or balances
   597	- clansman becomes or remains `WAITING` in Unicorn Town
   598	
   599	## 5.11 Initial pool seeding
   600	At deploy time, a treasury or deployer-controlled bootstrap address mints and seeds the initial resource and gold liquidity.

exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_2_state_schema_interface_spec.md | sed -n '88,110p;755,770p;958,982p'" in /home/claude/code/clan-world
 succeeded in 0ms:
    88	### 2.6 Immediate market execution
    89	Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
    90	
    91	This is still a mission/order action. It:
    92	- requires the worker already be physically in Unicorn Town
    93	- requires the worker be in `Waiting`
    94	- executes immediately in transaction order
    95	- consumes the worker cooldown
    96	- returns the worker to `Waiting`
    97	- updates mission metadata/nonce like any other mission
    98	
    99	---
   100	
   101	## 3. Tick and timing semantics
   102	
   103	### 3.1 Tick interval
   104	Target heartbeat cadence is **60 seconds per tick**.
   105	
   106	Implementation note:
   107	- real heartbeat arrival may drift by a few seconds due to automation / block timing
   108	- game semantics still treat each successful heartbeat as the boundary between discrete ticks
   109	
   110	### 3.2 Open tick model
   755	### 11.2 Immediate vs scheduled market ordering
   756	Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
   757	
   758	Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
   759	
   760	Therefore:
   761	- immediate actions during tick `T` may front-run scheduled actions that will execute at the close of tick `T`
   762	
   763	### 11.3 Scheduled action lifecycle
   764	A scheduled market action:
   765	- is created when a travel-based market mission is accepted
   766	- is indexed under `executeAtTick`
   767	- executes eagerly at the corresponding heartbeat
   768	- is deleted from storage after success or failure resolution
   769	
   770	### 11.4 Reentrancy and interaction discipline
   958	### 16.2 Immediate market sell
   959	If:
   960	- worker is `Waiting` in Unicorn Town
   961	- cooldown is clear
   962	- worker carries 12 wood
   963	- Elder submits `MarketSell(wood, 12e18)`
   964	
   965	Then:
   966	- swap executes immediately in that tx
   967	- clan gold purse increases by output
   968	- worker stays in Unicorn Town
   969	- worker returns to `Waiting`
   970	- cooldown is consumed
   971	
   972	### 16.3 Scheduled market buy
   973	If:
   974	- worker is one tick away from Unicorn Town in tick 307
   975	- Elder submits `MarketBuy(wood, 5e18, 10e18)`
   976	
   977	Then:
   978	- tick 307 = travel
   979	- tick 308 = single action tick in town
   980	- heartbeat closing tick 308 attempts to buy exactly 5 wood spending at most 10 gold
   981	- if price moved above 10 gold or purse balance is insufficient, trade fails, worker ends `Waiting`, cooldown consumed
   982	

exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_3_schema_patch.md | sed -n '20,36p;94,118p'" in /home/claude/code/clan-world
 succeeded in 0ms:
    20	- no `WorkerArrived` event is required for same-region / noop travel bypass
    21	
    22	### A.2 Immediate action interaction
    23	For immediate market actions:
    24	- same-region / noop bypass is required
    25	- worker must already be:
    26	  - in `UnicornTown`
    27	  - in `WAITING`
    28	  - off cooldown
    29	- action executes in the Elder tx itself
    30	
    31	### A.3 Same-region scheduled actions
    32	For non-immediate same-region actions:
    33	- action phase begins in the **current tick**
    34	- `actionStartTick = startTick`
    35	
    36	---
    94	## D. Scheduled Market Action Storage
    95	
    96	### D.1 Canonical storage layout
    97	Scheduled market actions are stored as:
    98	
    99	`mapping(uint64 => ScheduledMarketAction[]) scheduledMarketActionsByTick`
   100	
   101	Key:
   102	- `executeAtTick`
   103	
   104	Value:
   105	- append-only FIFO array for that tick
   106	
   107	### D.2 FIFO ordering
   108	Within a given `executeAtTick`, scheduled market actions execute in:
   109	1. ascending append order in `scheduledMarketActionsByTick[executeAtTick]`
   110	2. equivalently, ascending global `commitSequence`
   111	
   112	### D.3 Heartbeat cleanup
   113	When heartbeat closes tick `T`:
   114	- iterate `scheduledMarketActionsByTick[T]`
   115	- execute each action in FIFO order
   116	- emit success/failure events
   117	- delete `scheduledMarketActionsByTick[T]` after processing
   118	

exec
/bin/bash -lc "nl -ba docs/planning/clanworld_v4_2_state_schema_interface_spec.md | sed -n '160,205p;565,586p;735,781p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   160	## 4. Canonical accounting model
   161	
   162	### 4.1 Source of truth
   163	The canonical source of truth for gameplay balances is the **internal game ledger**, not ERC20 wallet balances.
   164	
   165	Canonical internal balances are:
   166	- clan vault resources
   167	- clan gold purse balance
   168	- clan blueprint balance
   169	- per-clansman carry balances
   170	
   171	### 4.2 Role of ERC20 tokens
   172	ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
   173	
   174	ERC20s are used for:
   175	- interaction with seeded Uniswap pools
   176	- optional prize payout plumbing
   177	- optional future externalization / withdrawal features
   178	
   179	ERC20 balances are **not** the authoritative representation of a clan’s vault or a clansman’s wheelbarrow.
   180	
   181	### 4.3 Balance domains
   182	There are three balance domains:
   183	
   184	**Clansman carry domain**
   185	- wood
   186	- iron
   187	- wheat
   188	- fish
   189	
   190	Used for:
   191	- gathering
   192	- hauling
   193	- direct sell-to-market when physically in Unicorn Town
   194	- receiving bought resources from market actions
   195	
   196	**Clan vault domain**
   197	- wood
   198	- iron
   199	- wheat
   200	- fish
   201	
   202	Used for:
   203	- upkeep
   204	- winter burn
   205	- construction spending
   565	### 8.3 MarketSell semantics
   566	`MarketSell` is **exact input only**.
   567	
   568	Meaning:
   569	- source = worker carry balance
   570	- destination = clan gold purse
   571	- `marketAmount` = exact amount of resource to sell
   572	- no `minOut` in v1
   573	
   574	### 8.4 MarketBuy semantics
   575	`MarketBuy` is **exact output only**.
   576	
   577	Meaning:
   578	- source = clan gold purse
   579	- destination = worker carry balance
   580	- `marketAmount` = exact amount of resource to buy
   581	- `maxGoldIn` = max purse gold willing to spend
   582	
   583	Validation:
   584	- requested output must fit within remaining carry capacity of that worker
   585	- if required gold at execution time exceeds `maxGoldIn`, buy fails
   586	- if clan purse lacks enough gold at execution time, buy fails
   735	## 11. Randomness, market execution, and reentrancy
   736	
   737	### 11.1 Tick seed derivation
   738	Heartbeat must derive the tick seed onchain.
   739	
   740	Suggested v1 formula:
   741	
   742	```solidity
   743	bytes32 tickSeed = keccak256(
   744	    abi.encode(
   745	        currentTick,
   746	        block.prevrandao,
   747	        block.timestamp,
   748	        block.number
   749	    )
   750	);
   751	```
   752	
   753	If target chain behavior differs, the implementation may swap to a chain-appropriate source later, but caller-supplied seed input is forbidden.
   754	
   755	### 11.2 Immediate vs scheduled market ordering
   756	Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
   757	
   758	Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
   759	
   760	Therefore:
   761	- immediate actions during tick `T` may front-run scheduled actions that will execute at the close of tick `T`
   762	
   763	### 11.3 Scheduled action lifecycle
   764	A scheduled market action:
   765	- is created when a travel-based market mission is accepted
   766	- is indexed under `executeAtTick`
   767	- executes eagerly at the corresponding heartbeat
   768	- is deleted from storage after success or failure resolution
   769	
   770	### 11.4 Reentrancy and interaction discipline
   771	Any function path that can trigger AMM interaction must follow checks-effects-interactions discipline.
   772	
   773	Recommended:
   774	- apply `nonReentrant` to immediate-order paths that can swap
   775	- apply `nonReentrant` to `heartbeat()` if it performs scheduled swaps
   776	- never leave partially updated clan/carry state around external calls
   777	
   778	Even if only known V2 pools are used in v1, implementation should still follow safe external-call patterns.
   779	
   780	---
   781	

codex
The biggest thing I’m seeing is not reentrancy; the guards and self-call wrappers mostly do their job. The more serious fracture is accounting: market buys/sells are wired to clan vault balances while the canonical market semantics and the new carry-cap validation point at clansman carry, which creates odd gameplay and bandit-timing side effects.
collab: Wait
collab: Wait
exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1135,1178p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1135	
  1136	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  1137	        internal
  1138	        returns (OrderResult memory result)
  1139	    {
  1140	        result.clansmanId = order.clansmanId;
  1141	
  1142	        // Validate clansman
  1143	        Clansman storage cs = _clansmen[order.clansmanId];
  1144	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  1145	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  1146	            return result;
  1147	        }
  1148	        if (cs.state == ClansmanState.DEAD) {
  1149	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  1150	            return result;
  1151	        }
  1152	
  1153	        if (order.action == ActionType.DefendBase) {
  1154	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  1155	            if (defendErr != StatusCode.OK) {
  1156	                result.status = defendErr;
  1157	                return result;
  1158	            }
  1159	
  1160	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1161	            Mission storage currentM = _missions[order.clansmanId];
  1162	            if (
  1163	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  1164	                    && currentM.targetClanId == defendTargetClanId
  1165	            ) {
  1166	                result.status = StatusCode.OK;
  1167	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1168	                result.missionNonce = currentM.nonce;
  1169	                return result;
  1170	            }
  1171	        }
  1172	
  1173	        bool isMarketAction = order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell;
  1174	
  1175	        // Cooldown check. Market orders may still fall back to the scheduled path;
  1176	        // only the immediate path requires the worker to be off cooldown.
  1177	        if (!isMarketAction && block.timestamp < cs.cooldownEndsAtTs) {
  1178	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;

codex
# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
NEEDS_FIXES. The integrated market path is close structurally, but it drifts from the canonical market accounting model: trades operate on clan vaults while the spec says market sell/buy source and destination are clansman carry. I also see a cooldown bypass in the scheduled fallback path and a queue visibility/lifecycle mismatch that weakens the FIFO/indexer story.

## HIGH severity findings

- `packages/contracts/src/ClanWorld.sol:1656`, `1771`, `1824`, `1946` — Market trades bypass clansman carry and mutate clan vaults directly. Spec says `MarketSell` source is worker carry and `MarketBuy` destination is worker carry (`docs/planning/clanworld_v4_2_state_schema_interface_spec.md:565-585`). Current code lets a worker in Unicorn Town sell homebase vault resources without carrying them, and bought resources appear in the vault without a return/deposit trip. This also changes bandit loot/target timing because vault value changes immediately.

- `packages/contracts/src/ClanWorld.sol:1173-1178` — Market orders bypass cooldown unless they qualify for immediate execution. A worker can fail/finish an immediate trade, enter cooldown, then submit another market order that falls into scheduled mode, including in the same batch or while interrupting active work. Scheduled fallback should not become a general cooldown escape hatch.

## MEDIUM severity findings

- `packages/contracts/src/ClanWorld.sol:469-471`, `1321-1337`, `1378-1456` — Scheduled market actions are only enqueued when the mission settles, then executed and deleted in the same heartbeat. Spec says scheduled actions are created when accepted and indexed under `executeAtTick` (`docs/planning/clanworld_v4_2_state_schema_interface_spec.md:763-768`; v4.3 `D.1-D.3`). As implemented, `getScheduledMarketActionsForTick` and market-state views cannot show upcoming committed trades.

- `packages/contracts/src/ClanWorld.sol:1214-1228` — Immediate market actions update `lastMissionNonce`, but do not install mission metadata or emit mission lifecycle events. Spec §5.6 says immediate actions are still missions and update mission metadata/nonce like any other mission. This may be acceptable if intentionally narrowed, but it is architectural drift and should be explicit.

- `packages/contracts/test/HeartbeatOrdering.t.sol` encodes the vault-based market model: one clansman deposits into vault and another sells from vault in the same heartbeat. That protects heartbeat ordering, but it masks the carry-accounting regression above.

## LOW severity findings

- Market execution tests are wood-heavy. Pool/token setup covers all four resources, but immediate/scheduled buy/sell execution coverage mostly uses `woodAddr`; add at least one buy/sell seam test across iron/wheat/fish.

- No direct test covers §5.7: immediate trade front-runs a scheduled same-pool trade in the same tick and the scheduled trade observes the moved pool price.

## Cross-cutting observations

Reentrancy posture is mostly sound: `submitClanOrders`, `heartbeat`, and seeding are `nonReentrant`, and scheduled swaps are isolated through self-call wrappers.

MEV/front-running is intentionally part of the design per §5.7/§11.2, but that makes the missing same-tick immediate-vs-scheduled collision test more important.
2026-04-29T18:04:03.927699Z ERROR codex_core::session: failed to record rollout items: thread 019dda65-c2bf-72b3-84f9-fc6dac9c8267 not found
2026-04-29T18:04:03.927707Z ERROR codex_core::session: failed to record rollout items: thread 019dda66-0709-7380-abdc-bf66806afdcf not found
2026-04-29T18:04:03.927845Z ERROR codex_core::session: failed to record rollout items: thread 019dda65-f872-7f20-bc65-447b6a9a8c80 not found
tokens used
300,597
# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
NEEDS_FIXES. The integrated market path is close structurally, but it drifts from the canonical market accounting model: trades operate on clan vaults while the spec says market sell/buy source and destination are clansman carry. I also see a cooldown bypass in the scheduled fallback path and a queue visibility/lifecycle mismatch that weakens the FIFO/indexer story.

## HIGH severity findings

- `packages/contracts/src/ClanWorld.sol:1656`, `1771`, `1824`, `1946` — Market trades bypass clansman carry and mutate clan vaults directly. Spec says `MarketSell` source is worker carry and `MarketBuy` destination is worker carry (`docs/planning/clanworld_v4_2_state_schema_interface_spec.md:565-585`). Current code lets a worker in Unicorn Town sell homebase vault resources without carrying them, and bought resources appear in the vault without a return/deposit trip. This also changes bandit loot/target timing because vault value changes immediately.

- `packages/contracts/src/ClanWorld.sol:1173-1178` — Market orders bypass cooldown unless they qualify for immediate execution. A worker can fail/finish an immediate trade, enter cooldown, then submit another market order that falls into scheduled mode, including in the same batch or while interrupting active work. Scheduled fallback should not become a general cooldown escape hatch.

## MEDIUM severity findings

- `packages/contracts/src/ClanWorld.sol:469-471`, `1321-1337`, `1378-1456` — Scheduled market actions are only enqueued when the mission settles, then executed and deleted in the same heartbeat. Spec says scheduled actions are created when accepted and indexed under `executeAtTick` (`docs/planning/clanworld_v4_2_state_schema_interface_spec.md:763-768`; v4.3 `D.1-D.3`). As implemented, `getScheduledMarketActionsForTick` and market-state views cannot show upcoming committed trades.

- `packages/contracts/src/ClanWorld.sol:1214-1228` — Immediate market actions update `lastMissionNonce`, but do not install mission metadata or emit mission lifecycle events. Spec §5.6 says immediate actions are still missions and update mission metadata/nonce like any other mission. This may be acceptable if intentionally narrowed, but it is architectural drift and should be explicit.

- `packages/contracts/test/HeartbeatOrdering.t.sol` encodes the vault-based market model: one clansman deposits into vault and another sells from vault in the same heartbeat. That protects heartbeat ordering, but it masks the carry-accounting regression above.

## LOW severity findings

- Market execution tests are wood-heavy. Pool/token setup covers all four resources, but immediate/scheduled buy/sell execution coverage mostly uses `woodAddr`; add at least one buy/sell seam test across iron/wheat/fish.

- No direct test covers §5.7: immediate trade front-runs a scheduled same-pool trade in the same tick and the scheduled trade observes the moved pool price.

## Cross-cutting observations

Reentrancy posture is mostly sound: `submitClanOrders`, `heartbeat`, and seeding are `nonReentrant`, and scheduled swaps are isolated through self-call wrappers.

MEV/front-running is intentionally part of the design per §5.7/§11.2, but that makes the missing same-tick immediate-vs-scheduled collision test more important.
