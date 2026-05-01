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
I’m reviewing the Phase 6 market changes as an integrated whole, with the focus on state-machine seams, market execution timing, and the new ERC20 boundary flow. I’ll pull the contract/test context in parallel, then split a couple of bounded checks across subagents so we can compare notes before I write the final verdict.
exec
/bin/bash -lc "sed -n '1,260p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    IClanWorld,
    IClanWorldEvents,
    ClanWorldConstants,
    ClanState,
    ClansmanState,
    BanditState,
    WheatPlotState,
    ResourceType,
    ActionType,
    MarketExecutionMode,
    StatusCode,
    WorldState,
    TreasuryState,
    Clan,
    WheatPlot,
    Clansman,
    Mission,
    BanditTroop,
    ScheduledMarketAction,
    DefenseContribution,
    PackedRoute,
    DerivedClanState,
    DerivedClansmanState,
    ClanOrder,
    OrderResult,
    PoolSeedConfig,
    LeaderboardEntry,
    WorldSnapshot,
    ClansmanFullView,
    ClanFullView,
    PoolReserves,
    MarketState,
    ActiveBanditView,
    RegionOccupant
} from "./IClanWorld.sol";
import {RNG} from "./lib/RNG.sol";
import {StubPool} from "./StubPool.sol";
import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";

/// @title ClanWorld
/// @notice Phase 1+2 real engine implementation of IClanWorld v4.
///         Implements: world clock, clan lifecycle, lazy settlement, resource gathering,
///         deposit, wheat harvest, travel, NOOP bypass, order validation, and market execution.
///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
contract ClanWorld is IClanWorld, ReentrancyGuard {
    // =========================================================================
    // STORAGE
    // =========================================================================

    WorldState private _world;
    TreasuryState private _treasury;

    mapping(uint32 => Clan) internal _clans;
    mapping(uint32 => Clansman) internal _clansmen;
    mapping(uint32 => Mission) private _missions; // keyed by clansmanId
    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
    mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
    mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
    mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
    mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
    mapping(uint32 => BanditTroop) internal _bandits;
    mapping(uint8 => uint32[]) internal _banditsByRegion; // region => bandit IDs
    mapping(uint8 => BanditSpawnState) internal _banditSpawnByRegion;
    mapping(uint64 => bytes32) private _tickSeeds; // tick => seed

    uint32 private _nextClanId;
    uint32 private _nextClansmanId;
    uint32 internal _nextBanditId;
    uint32 internal _activeBanditCount;
    uint32[] private _allClanIds;

    // per-clan clansman list: clanId => clansmanId[]
    mapping(uint32 => uint32[]) private _clanClansmanIds;

    // =========================================================================
    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    // =========================================================================

    uint256 private constant WHEAT_HARVEST_RATE = 20e18;
    uint256 private constant RESOURCE_UNIT = 1e18;
    uint256 internal constant BLUEPRINT_UNIT = 1e18;
    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
    uint256 internal constant DOMAIN_BANDIT_SPAWN = uint256(keccak256("clanworld.bandit.spawn.v1"));
    uint256 internal constant DOMAIN_BANDIT_TARGET_PICK = uint256(keccak256("bandit_target_pick"));
    uint64 internal constant MIN_SPAWN_COOLDOWN_TICKS = ClanWorldConstants.BANDIT_COOLDOWN_TICKS;
    uint16 internal constant BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS = 1000;
    uint16 internal constant BANDIT_SPAWN_MAX_PROBABILITY_BPS = 8000;
    uint8 internal constant MAX_BANDITS_PER_REGION = 3;
    uint8 internal constant MAX_TOTAL_BANDITS = 8;
    uint8 internal constant MAX_CLANS = 12;
    /// @dev Bandit spawn weights are a heartbeat-time heuristic. V1 has
    ///      MAX_CLANS = 12, so scanning 8 clans per tick covers the live cap in
    ///      at most two rotating heartbeats while keeping heartbeat gas bounded.
    uint256 internal constant MAX_BANDIT_SPAWN_SCAN_PER_REGION = 8;
    uint256 internal constant MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION = MAX_BANDIT_SPAWN_SCAN_PER_REGION * 4;
    /// @dev Eager settlement scans the clan-indexed bases in each spawn-candidate
    ///      region, not every clan globally per region forever. MAX_CLANS = 12,
    ///      so this settles all possible bases today while keeping the heartbeat
    ///      loop explicitly bounded if that cap grows.
    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION = 12;
    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION = 12;
    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION = 48;
    uint32 internal constant MIN_BANDIT_SPAWN_STRENGTH = 100;
    uint32 internal constant BANDIT_SPAWN_STRENGTH_SPREAD = 151;
    uint32 internal constant CLANSMAN_MAX_DEFENSE_DAMAGE = 100;
    uint32 internal constant WALL_HP_PER_LEVEL = 100;
    uint32 internal constant BASE_HP_PER_LEVEL = 25;
    uint32 internal constant CLANSMAN_HP = 100;

    struct BanditSpawnState {
        uint64 lastSpawnTick;
        uint16 probabilityAccum;
    }

    struct SettlementSimulation {
        Clan clan;
        Clansman[] clansmen;
        Mission[] missions;
        WheatPlot[2] wheatPlots;
    }

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    constructor() {
        _world.currentTick = 0;
        _world.nextHeartbeatAtTs = uint64(block.timestamp);
        _world.seasonStartTick = 0;
        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
        _world.currentSeasonNumber = 1;
        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
        // i.e. ticks [100, 110)
        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
        _world.winterActive = false;
        _treasury.treasuryOwner = msg.sender;
        _nextClanId = 1;
        _nextClansmanId = 1;
        _nextBanditId = 1;
    }

    // =========================================================================
    // TRAVEL DISTANCE MATRIX
    // =========================================================================

    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
    // Adjacency:
    //   Forest(1):       Mountains(2), WestFarms(4)
    //   Mountains(2):    Forest(1), UnicornTown(3)
    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
    //   WestDocks(6):    WestFarms(4), DeepSea(8)
    //   EastDocks(7):    EastFarms(5), DeepSea(8)
    //   DeepSea(8):      WestDocks(6), EastDocks(7)
    //
    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
    // Distance of 0 = same region.
    //
    // Full BFS-computed 8x8 matrix:
    //   src\dst  1  2  3  4  5  6  7  8
    //      1     0  1  2  1  3  2  4  3
    //      2     1  0  1  2  2  3  3  4
    //      3     2  1  0  1  1  2  2  3
    //      4     1  2  1  0  2  1  3  2
    //      5     3  2  1  2  0  3  1  2
    //      6     2  3  2  1  3  0  2  1
    //      7     4  3  2  3  1  2  0  1
    //      8     3  4  3  2  2  1  1  0

    function _distMatrix(uint8 src, uint8 dst) private pure returns (uint8) {
        if (src == dst) return 0;
        // Encode as (src-1)*8 + (dst-1), values from BFS
        uint8[64] memory d = [
            // src=1: to 1,2,3,4,5,6,7,8
            0,
            1,
            2,
            1,
            3,
            2,
            4,
            3,
            // src=2: to 1,2,3,4,5,6,7,8
            1,
            0,
            1,
            2,
            2,
            3,
            3,
            4,
            // src=3: to 1,2,3,4,5,6,7,8
            2,
            1,
            0,
            1,
            1,
            2,
            2,
            3,
            // src=4: to 1,2,3,4,5,6,7,8
            1,
            2,
            1,
            0,
            2,
            1,
            3,
            2,
            // src=5: to 1,2,3,4,5,6,7,8
            3,
            2,
            1,
            2,
            0,
            3,
            1,
            2,
            // src=6: to 1,2,3,4,5,6,7,8
            2,
            3,
            2,
            1,
            3,
            0,
            2,
            1,
            // src=7: to 1,2,3,4,5,6,7,8
            4,
            3,
            2,
            3,
            1,
            2,
            0,
            1,
            // src=8: to 1,2,3,4,5,6,7,8
            3,
            4,
            3,
            2,
            2,
            1,
            1,
            0
        ];
        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
    }

    // Build a canonical path from src to dst (BFS on the adjacency graph).
    // Returns packed bytes8: region IDs in order, zero-padded.
    function _buildPath(uint8 src, uint8 dst) private pure returns (bytes8) {

exec
/bin/bash -lc "sed -n '1,240p' packages/contracts/AGENTS.md" in /home/claude/code/clan-world
 succeeded in 0ms:
# packages/contracts — AGENTS.md

Solidity contracts for ClanWorld. Foundry-managed. The canonical seam interface is `src/IClanWorld.sol` — every other workspace talks to chain through it (via the `IChainClient` adapter).

## What this package does

- Defines the `IClanWorld` interface (24KB, ABI-stable).
- (Wave 1+) Implements the engine: tick advancement, mission resolution, heartbeat throttle, treasury accounting.
- (Wave 2+) Includes deploy script for World Chain Sepolia (S1) and Base Sepolia (S2).
- (S2 only) Adds `ClanIdentity` (ERC-7857 iNFT wrapper) and `ClanMemory` (0G Storage KV wrapper) libraries.

## Wave 0 status

Only `src/IClanWorld.sol` exists. No engine impl, no deploy scripts, no tests. Wave 1 ships the engine + deploy.

## Key files

- `src/IClanWorld.sol` — canonical seam (DO NOT MODIFY without ADR; see `../../docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md`).
- `foundry.toml` — Foundry config; reads `RPC_URL_PRIMARY` from env.
- `package.json` — minimal; this is a Foundry project, not a TS package. The npm scripts shell out to `forge`.

## Local conventions

- **No TypeScript in this package.** Type generation for consumers happens via `wagmi typegen` or hand-written ABI types in `@clan-world/shared`.
- **`IClanWorld.sol` is sacred.** Changes require: ADR amendment, full team sign-off, all 6 spec docs reviewed for ripple effects.
- **Solc version pinned** in `foundry.toml` (`0.8.24`). Don't bump without a CI test run.
- **Two deploy targets:** `worldchain-sepolia` (S1) and `base-sepolia` (S2). Both use the same contract bytecode.
- **Test on a local anvil first.** Don't burn testnet faucet funds debugging silly bugs.

## How it interacts with adapters

This package IS one side of `IChainClient` — `RealChainClient` (in `@clan-world/shared/adapters`) calls into the deployed contract via viem/ethers using ABIs derived from this package.

## Running

```bash
forge build          # build contracts
forge test           # run tests (Wave 1+)
forge script script/Deploy.s.sol --rpc-url $RPC_URL_PRIMARY --broadcast
```

See `../../docs/guides/stream-contracts.md` for the full deploy workflow including wallet management.

exec
/bin/bash -lc "sed -n '1,260p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/**
 * @title IClanWorld
 * @notice Canonical seam interface for the ClanWorld v4 hackathon build.
 *
 * Synthesized from:
 *   - clanworld_v4_spec.md (mechanics)
 *   - clanworld_v4_1_addendum.md (locked clarifications)
 *   - clanworld_v4_2_state_schema_interface_spec.md (state schema)
 *   - clanworld_v4_3_schema_patch.md (final lockdowns)
 *   - clanworld_v4_4_ui_indexer_getters.md (UI convenience aggregators)
 *
 * This is the contract surface every other workstream depends on.
 * Treat field names, event names, struct layouts, and enum order as STABLE.
 *
 * Implementation lives behind this interface. Mocks and indexer types are
 * derived from it. Do not change without coordinating with all streams.
 */

// =============================================================================
// CONSTANTS
// =============================================================================

library ClanWorldConstants {
    // World cadence
    uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
    uint64 internal constant WINTER_DURATION_TICKS = 10;
    uint64 internal constant SEASON_DURATION_TICKS = 360;

    // Bandit cadence
    uint64 internal constant BANDIT_COOLDOWN_TICKS = 10;
    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    uint64 internal constant BANDIT_REST_TICKS = 2;
    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;

    // Clansman cadence
    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;

    // Carry caps (per clansman)
    uint256 internal constant WOOD_CAP = 15e18;
    uint256 internal constant IRON_CAP = 5e18;
    uint256 internal constant WHEAT_CAP = 40e18;
    uint256 internal constant FISH_CAP = 8e18;

    // Gathering yields
    uint256 internal constant WOOD_BASE_YIELD = 2e18;
    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
    uint16 internal constant WOOD_CRIT_BPS = 2000; // 20%

    uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
    uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;

    uint16 internal constant FISH_DOCKS_BPS = 2500; // 25%
    uint16 internal constant FISH_DEEP_BPS = 7500; // 75%

    // Upkeep
    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x

    // Wheat plots
    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;

    // Bandit combat
    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%

    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    uint8 internal constant REGION_NOOP = 0;
    uint8 internal constant REGION_FOREST = 1;
    uint8 internal constant REGION_MOUNTAINS = 2;
    uint8 internal constant REGION_UNICORN_TOWN = 3;
    uint8 internal constant REGION_WEST_FARMS = 4;
    uint8 internal constant REGION_EAST_FARMS = 5;
    uint8 internal constant REGION_WEST_DOCKS = 6;
    uint8 internal constant REGION_EAST_DOCKS = 7;
    uint8 internal constant REGION_DEEP_SEA = 8;

    // Sentinels
    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    uint32 internal constant BANDIT_ID_NULL = 0;
}

// =============================================================================
// ENUMS
// =============================================================================

enum ClanState {
    ACTIVE,
    DEAD
}

enum ClansmanState {
    WAITING,
    TRAVELING,
    ACTING,
    DEAD
}

// v1 ABI: Bandit state machine redesigned in Phase 9. ABI consumers must regenerate.
enum BanditState {
    None,
    Spawned,
    Camped,
    Resting,
    Attacking,
    Defeated,
    Escaped
}

enum WheatPlotState {
    Harvestable,
    Regrowing,
    WinterLocked
}

enum ResourceType {
    Wood,
    Iron,
    Wheat,
    Fish
}

enum ActionType {
    None,
    ChopWood,
    MineIron,
    FishDocks,
    FishDeepSea,
    HarvestWheat,
    DepositResources,
    BuildWall,
    UpgradeBase,
    UpgradeMonument,
    DefendBase,
    MarketBuy,
    MarketSell,
    Wait
}

enum MarketExecutionMode {
    None,
    Immediate,
    Scheduled
}

enum StatusCode {
    OK,
    ERR_CLAN_DEAD,
    ERR_CLAN_NOT_OWNED,
    ERR_CLANSMAN_DEAD,
    ERR_INVALID_CLANSMAN,
    ERR_INVALID_REGION,
    ERR_INVALID_ACTION,
    ERR_INVALID_TARGET,
    ERR_COOLDOWN_ACTIVE,
    ERR_NOT_WAITING,
    ERR_NOT_IN_UNICORN_TOWN,
    ERR_NOT_AT_HOMEBASE,
    ERR_NOT_AT_TARGET_BASE,
    ERR_NOT_DEFENDABLE,
    ERR_MISSING_RESOURCES,
    ERR_EMPTY_CARGO,
    ERR_PLOT_NOT_READY,
    ERR_PLOT_EMPTY,
    ERR_MARKET_ZERO_AMOUNT,
    ERR_MARKET_UNSUPPORTED_TOKEN,
    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
    ERR_MARKET_BUY_OVER_CAPACITY,
    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
    ERR_WORLD_TICK_MISMATCH,
    ERR_NO_ACTIVE_BANDIT,
    ERR_SEASON_ENDED,
    ERR_NOT_ENOUGH_GOLD,
    ERR_CARRY_FULL
}

// =============================================================================
// CORE STATE STRUCTS (raw storage shape)
// =============================================================================

struct WorldState {
    uint64 currentTick;
    uint64 seasonStartTick;
    uint64 seasonEndTick;
    bool seasonFinalized;
    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)

    uint64 nextHeartbeatAtTs;
    uint64 nextBanditSpawnEligibleTick;
    uint16 currentBanditSpawnChanceBps;
    bytes32 currentTickSeed;

    uint32 activeBanditId; // 0 if none
    bool winterActive;
    uint64 winterStartsAtTick;
    uint64 winterEndsAtTick; // 0 if not active

    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
}

struct TreasuryState {
    address treasuryOwner;
    uint256 prizePotGold;

    bool poolsSeeded;

    address woodToken;
    address wheatToken;
    address fishToken;
    address ironToken;
    address goldToken;
    address blueprintToken;

    address woodGoldPool;
    address wheatGoldPool;
    address fishGoldPool;
    address ironGoldPool;
}

struct Clan {
    uint32 clanId;
    uint256 iftTokenId;
    address owner;
    ClanState clanState;

    uint8 baseRegion;
    uint8 baseLevel;
    uint8 wallLevel;
    uint8 monumentLevel;
    uint8 livingClansmen;

    uint64 lastSettledTick;
    uint64 starvationStartsAtTick; // 0 = none

    uint16 coldDamage; // resets to 0 at winter end

    uint256 goldBalance;
    uint256 blueprintBalance;

    uint256 vaultWood;
    uint256 vaultIron;
    uint256 vaultWheat;
    uint256 vaultFish;
}

struct WheatPlot {
    WheatPlotState state;
    uint8 region; // West Farms or East Farms
    uint256 remainingWheat;
    uint64 regrowUntilTick;
}


exec
/bin/bash -lc "sed -n '1,240p' packages/contracts/src/MinimalERC20.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/// @notice Minimal ERC20 with owner-only mint. No external deps.
contract MinimalERC20 {
    string public name;
    string public symbol;
    uint8 public constant decimals = 18;

    uint256 public totalSupply;
    address public immutable OWNER;

    mapping(address => uint256) public balanceOf;
    mapping(address => mapping(address => uint256)) public allowance;

    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner_, address indexed spender, uint256 value);

    constructor(string memory name_, string memory symbol_) {
        name = name_;
        symbol = symbol_;
        OWNER = msg.sender;
    }

    /// @dev Wrapped per forge-lint unwrapped-modifier-logic — keeps modifier
    /// body slim so the require isn't duplicated at every call site.
    function _onlyOwner() internal view {
        require(msg.sender == OWNER, "not owner");
    }

    modifier onlyOwner() {
        _onlyOwner();
        _;
    }

    function mint(address to, uint256 amount) external onlyOwner {
        totalSupply += amount;
        balanceOf[to] += amount;
        emit Transfer(address(0), to, amount);
    }

    function transfer(address to, uint256 amount) external returns (bool) {
        balanceOf[msg.sender] -= amount;
        balanceOf[to] += amount;
        emit Transfer(msg.sender, to, amount);
        return true;
    }

    function transferFrom(address from, address to, uint256 amount) external returns (bool) {
        allowance[from][msg.sender] -= amount;
        balanceOf[from] -= amount;
        balanceOf[to] += amount;
        emit Transfer(from, to, amount);
        return true;
    }

    function approve(address spender, uint256 amount) external returns (bool) {
        allowance[msg.sender][spender] = amount;
        emit Approval(msg.sender, spender, amount);
        return true;
    }
}

exec
/bin/bash -lc "sed -n '1,260p' packages/contracts/src/StubPool.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/// @notice Constant-product AMM pool for one resource/gold pair.
///         Reserves are tracked internally; ClanWorld calls the math
///         functions to compute exchange rates, then mints/credits balances
///         directly via the internal accounting model.
///         No real ERC20 transfers occur here — the pool is the math oracle.
contract StubPool {
    address public immutable TOKEN_A;  // resource token
    address public immutable TOKEN_B;  // gold token
    address public immutable ENGINE;   // ClanWorld address

    uint256 public reserveA;          // resource reserve
    uint256 public reserveB;          // gold reserve

    bool private _seeded;

    modifier onlyEngine() {
        require(msg.sender == ENGINE, "StubPool: only engine");
        _;
    }

    constructor(address tokenA_, address tokenB_, address engine_) {
        TOKEN_A = tokenA_;
        TOKEN_B = tokenB_;
        ENGINE = engine_;
    }

    /// @notice Called by ClanWorld at seedPools time to set initial reserves.
    ///         Can only be called once.
    function seed(uint256 amountA, uint256 amountB) external onlyEngine {
        require(!_seeded, "StubPool: already seeded");
        require(amountA > 0 && amountB > 0, "StubPool: zero seed");
        reserveA = amountA;
        reserveB = amountB;
        _seeded = true;
    }

    /// @notice Exact-input sell: clan sells amountIn of resource, gets goldOut.
    ///         Constant-product: goldOut = reserveB * amountIn / (reserveA + amountIn).
    ///         Updates reserves. Called by ClanWorld; no token transfers.
    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
        require(amountIn > 0, "StubPool: zero amount");
        require(reserveA > 0 && reserveB > 0, "StubPool: not seeded");
        goldOut = (reserveB * amountIn) / (reserveA + amountIn);
        require(goldOut > 0, "StubPool: zero output");
        reserveA += amountIn;
        reserveB -= goldOut;
    }

    /// @notice Quote a buy without updating reserves (view).
    ///         goldIn = ceil(reserveB * amountOut / (reserveA - amountOut)).
    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
        require(amountOut > 0, "StubPool: zero amount");
        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
        uint256 num = reserveB * amountOut;
        uint256 denom_ = reserveA - amountOut;
        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
    }

    /// @notice Exact-output buy: clan buys amountOut of resource, pays goldIn.
    ///         Returns actual goldIn charged (ceiling arithmetic).
    ///         Updates reserves. Called by ClanWorld; no token transfers.
    function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
        require(amountOut > 0, "StubPool: zero amount");
        require(amountOut < reserveA, "StubPool: insufficient resource reserve");
        require(reserveB > 0, "StubPool: not seeded");
        uint256 num = reserveB * amountOut;
        uint256 denom_ = reserveA - amountOut;
        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
        reserveA -= amountOut;
        reserveB += goldIn;
    }

    function getReserves() external view returns (uint256, uint256) {
        return (reserveA, reserveB);
    }
}

codex
I’ve confirmed the package-level contract guidance and I’m switching from the repo baseline to the actual `9f93593` snapshot so the review stays anchored to the PR head, not local history. In parallel, I’m splitting one pass on market/tick sequencing and one on pool/token/security so we can sanity-check the seam risks from two angles.
2026-04-29T18:00:21.850722Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T18:00:21.910552Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
exec
/bin/bash -lc 'git rev-parse HEAD' in /home/claude/code/clan-world
 succeeded in 0ms:
218f9020ecb0f4b8277ef59dd55de8e004404d80

exec
/bin/bash -lc 'git show --stat --oneline 9f93593' in /home/claude/code/clan-world
 succeeded in 0ms:
9f93593 fix(contracts): Phase 6 cloud-review fix-round - duplicate enums, unused param, MAX_MARKET_ACTIONS, carry overflow (Closes #269) (#270)
 packages/contracts/src/ClanWorld.sol    | 50 ++++++++++++------------------
 packages/contracts/src/IClanWorld.sol   |  6 ++--
 packages/contracts/test/ClanWorld.t.sol | 55 ++++++++++++---------------------
 packages/contracts/test/Gathering.t.sol |  4 +--
 4 files changed, 43 insertions(+), 72 deletions(-)

codex
The local checkout isn’t on `9f93593`, so I’m reading the PR head directly through `git show` and treating the stdin diff as the review surface. That should keep us from mixing in unrelated local history while we trace the actual market flow.
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc 'git show 9f93593:packages/contracts/test/ClanWorld.t.sol | rg -n "immediateMarket|scheduledMarket|depositResources|MarketActionFailed|ScheduledMarketActionExecuted|scheduledMarket_fifo|scheduledMarket_executesAllActionsForClosedTick|scheduledMarketFailure_doesNotAffectAnotherClan|HeartbeatOrdering"' in /home/claude/code/clan-world
 succeeded in 0ms:
498:    function test_depositResources_woodCarryMovesToVaultAndClears() public {
523:    function test_depositResources_emptyCarryNoopsWithoutEvent() public {
543:    function test_depositResources_multipleTypesMoveTogether() public {
570:    function test_depositResources_requiresHomeRegion() public {
582:    function test_depositResources_eventHasCorrectDeltas() public {
775:    function test_immediateMarketSell_executesInSubmitTx() public {
809:    function test_immediateMarket_townOnCooldown_fallsBackToScheduled() public {
830:    function test_immediateMarket_notInTown_fallsBackToScheduled() public {
847:    function test_immediateMarket_busyWorker_fallsBackToScheduled() public {
864:    function test_immediateMarketBuy_executesWhenMaxGoldSatisfied() public {
892:    function test_immediateMarket_insufficientLiquidityFailsAndConsumesCooldown() public {
899:        emit IClanWorldEvents.MarketActionFailed(
924:    function test_immediateMarketBuy_maxGoldExceededFailsAndConsumesCooldown() public {
931:        emit IClanWorldEvents.MarketActionFailed(
953:    function test_immediateMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
960:        emit IClanWorldEvents.MarketActionFailed(
982:    function test_immediateMarketBuy_overCapacityRejectsAtSubmit() public {
1032:        emit IClanWorldEvents.ScheduledMarketActionExecuted(
1112:        // Now the next heartbeat will close executeAtTick — that's when MarketActionFailed fires
1115:        emit IClanWorldEvents.MarketActionFailed(
1134:    function test_scheduledMarketBuy_insufficientGoldFailsAndConsumesCooldown() public {
1148:        emit IClanWorldEvents.MarketActionFailed(
1167:    function test_scheduledMarketBuy_overCapacityRejectsAtSubmit() public {
1191:    function test_scheduledMarketBuy_insufficientLiquidityFailsAndConsumesCooldown() public {
1206:        emit IClanWorldEvents.MarketActionFailed(
1226:    // Test 14: scheduledMarket_deletedAfterHeartbeat
1229:    function test_scheduledMarket_deletedAfterHeartbeat() public {
1252:    function test_scheduledMarket_sameTypeRetask_skipsStaleNonce() public {
1291:    function test_scheduledMarket_executesAllActionsForClosedTick() public {
1322:            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
1347:    // Test 15: scheduledMarket_fifo — same-tick sells execute in submission order
1350:    function test_scheduledMarket_fifo() public {
1376:            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
1454:            keccak256("ScheduledMarketActionExecuted(uint32,uint32,uint8,uint8,uint256,uint8,uint256,uint64)");
1493:    function test_scheduledMarketFailure_doesNotAffectAnotherClan() public {

exec
/bin/bash -lc 'git show 9f93593:packages/contracts/src/ClanWorld.sol | rg -n "_executeImmediateMarket|_executeImmediateMarketSell|_executeImmediateMarketBuy|_enqueueScheduledMarketAction|_executeScheduledMarketActions|_handleMarketFailure|_remainingCarryForToken|submitClanOrders|_validateAction|seedPools|getActionDuration|_doDeposit|_gatherWood|_installMission"' in /home/claude/code/clan-world
 succeeded in 0ms:
342:                if (m.active && getActionDuration(m.action) > 0) {
446:            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
458:            _doDeposit(clan, cs, m, clanId, uint32(tick));
470:            _enqueueScheduledMarketAction(clanId, cs.clansmanId, m);
479:    function _gatherWood(
494:        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
668:    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
896:        _executeScheduledMarketActions(closedTick);
1081:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
1204:        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
1217:            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
1246:        _installMission(existingM, order, cs, ctx);
1288:    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
1298:            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
1321:    function _enqueueScheduledMarketAction(uint32 clanId, uint32 clansmanId, Mission storage m) internal {
1379:    function _executeScheduledMarketActions(uint64 tick) internal {
1427:                    _handleMarketFailure(
1444:                    _handleMarketFailure(
1580:    function _handleMarketFailure(
1597:    function _remainingCarryForToken(Clansman storage cs, address token) internal view returns (uint256) {
1620:    function _executeImmediateMarket(uint32 clanId, ClanOrder calldata order, uint32 clansmanId)
1625:            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
1627:        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
1630:    function _executeImmediateMarketSell(uint32 clanId, uint32 clansmanId, address token, uint256 amount)
1635:            return _handleMarketFailure(
1646:            return _handleMarketFailure(
1658:            return _handleMarketFailure(
1683:            return _handleMarketFailure(
1694:    function _executeImmediateMarketBuy(
1702:            return _handleMarketFailure(
1713:            return _handleMarketFailure(
1724:        if (amountOut > _remainingCarryForToken(cs, token)) {
1725:            return _handleMarketFailure(
1739:            return _handleMarketFailure(
1751:            return _handleMarketFailure(
1761:            return _handleMarketFailure(
1786:            return _handleMarketFailure(
1803:            return _handleMarketFailure(
1814:            return _handleMarketFailure(
1826:            return _handleMarketFailure(
1862:            return _handleMarketFailure(
1873:            return _handleMarketFailure(
1884:        if (amountOut > _remainingCarryForToken(cs, token)) {
1885:            return _handleMarketFailure(
1899:            return _handleMarketFailure(
1912:            return _handleMarketFailure(
1922:            return _handleMarketFailure(
1936:            return _handleMarketFailure(
1962:    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
2034:            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
2037:            // Immediate market orders execute during submitClanOrders when the
2064:    ///         Must be called before seedPools. Callable only once.
2083:    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
2199:    function getActionDuration(ActionType action) public pure override returns (uint64) {

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '430,540p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1080,1338p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1376,1970p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2030,2215p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
  2206	
  2207	        if (action == ActionType.DepositResources) {
  2208	            return DEPOSIT_DURATION_TICKS;
  2209	        }
  2210	
  2211	        if (
  2212	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
  2213	                || action == ActionType.MarketBuy || action == ActionType.MarketSell
  2214	        ) {
  2215	            return 1;

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
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
   191	// =============================================================================
   192	
   193	struct WorldState {
   194	    uint64 currentTick;
   195	    uint64 seasonStartTick;
   196	    uint64 seasonEndTick;
   197	    bool seasonFinalized;
   198	    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
   199	    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
   200	
   201	    uint64 nextHeartbeatAtTs;
   202	    uint64 nextBanditSpawnEligibleTick;
   203	    uint16 currentBanditSpawnChanceBps;
   204	    bytes32 currentTickSeed;
   205	
   206	    uint32 activeBanditId; // 0 if none
   207	    bool winterActive;
   208	    uint64 winterStartsAtTick;
   209	    uint64 winterEndsAtTick; // 0 if not active
   210	
   211	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
   212	}
   213	
   214	struct TreasuryState {
   215	    address treasuryOwner;
   216	    uint256 prizePotGold;
   217	
   218	    bool poolsSeeded;
   219	
   220	    address woodToken;
   221	    address wheatToken;
   222	    address fishToken;
   223	    address ironToken;
   224	    address goldToken;
   225	    address blueprintToken;
   226	
   227	    address woodGoldPool;
   228	    address wheatGoldPool;
   229	    address fishGoldPool;
   230	    address ironGoldPool;
   231	}
   232	
   233	struct Clan {
   234	    uint32 clanId;
   235	    uint256 iftTokenId;
   236	    address owner;
   237	    ClanState clanState;
   238	
   239	    uint8 baseRegion;
   240	    uint8 baseLevel;
   241	    uint8 wallLevel;
   242	    uint8 monumentLevel;
   243	    uint8 livingClansmen;
   244	
   245	    uint64 lastSettledTick;
   246	    uint64 starvationStartsAtTick; // 0 = none
   247	
   248	    uint16 coldDamage; // resets to 0 at winter end
   249	
   250	    uint256 goldBalance;
   251	    uint256 blueprintBalance;
   252	
   253	    uint256 vaultWood;
   254	    uint256 vaultIron;
   255	    uint256 vaultWheat;
   256	    uint256 vaultFish;
   257	}
   258	
   259	struct WheatPlot {
   260	    WheatPlotState state;

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/MinimalERC20.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "git show 9f93593:packages/contracts/src/StubPool.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 9f93593:packages/contracts/script/Deploy.s.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.24;
     3	
     4	import {Script, console} from "forge-std/Script.sol";
     5	import {MinimalERC20} from "../src/MinimalERC20.sol";
     6	import {StubPool} from "../src/StubPool.sol";
     7	import {ClanWorld} from "../src/ClanWorld.sol";
     8	import {PoolSeedConfig, ResourceType} from "../src/IClanWorld.sol";
     9	
    10	contract Deploy is Script {
    11	    function run() external {
    12	        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
    13	        address treasury = vm.addr(deployerPrivateKey);
    14	
    15	        vm.startBroadcast(deployerPrivateKey);
    16	
    17	        // 1. Deploy boundary tokens (gold existed in Phase 2 and is reused here).
    18	        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
    19	        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
    20	        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
    21	        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
    22	        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
    23	        MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");
    24	
    25	        console.log("wood:     ", address(wood));
    26	        console.log("iron:     ", address(iron));
    27	        console.log("wheat:    ", address(wheat));
    28	        console.log("fish:     ", address(fish));
    29	        console.log("gold:     ", address(gold));
    30	        console.log("blueprint:", address(blueprint));
    31	
    32	        // 2. Deploy ClanWorld first (needed as engine arg for pools).
    33	        ClanWorld game = new ClanWorld();
    34	        console.log("CLAN_WORLD_CONTRACT_ADDRESS:", address(game));
    35	
    36	        wood.configureBoundary(uint8(ResourceType.Wood), address(game));
    37	        iron.configureBoundary(uint8(ResourceType.Iron), address(game));
    38	        wheat.configureBoundary(uint8(ResourceType.Wheat), address(game));
    39	        fish.configureBoundary(uint8(ResourceType.Fish), address(game));
    40	
    41	        // 3. Deploy 4 AMM pools (Phase 6.2: constant-product pools).
    42	        StubPool woodGold = new StubPool(address(wood), address(gold), address(game));
    43	        StubPool wheatGold = new StubPool(address(wheat), address(gold), address(game));
    44	        StubPool fishGold = new StubPool(address(fish), address(gold), address(game));
    45	        StubPool ironGold = new StubPool(address(iron), address(gold), address(game));
    46	
    47	        console.log("woodGoldPool: ", address(woodGold));
    48	        console.log("wheatGoldPool:", address(wheatGold));
    49	        console.log("fishGoldPool: ", address(fishGold));
    50	        console.log("ironGoldPool: ", address(ironGold));
    51	
    52	        address[6] memory tokens =
    53	            [address(wood), address(iron), address(wheat), address(fish), address(gold), address(blueprint)];
    54	        address[4] memory pools = [address(woodGold), address(wheatGold), address(fishGold), address(ironGold)];
    55	
    56	        game.initTreasury(tokens, pools);
    57	
    58	        uint256 resSeed = game.INITIAL_RESOURCE_POOL_SEED();
    59	        uint256 goldSeed = game.INITIAL_GOLD_POOL_SEED();
    60	        uint256 totalGoldSeed = goldSeed * 4;
    61	
    62	        wood.seedTreasury(treasury, resSeed);
    63	        wheat.seedTreasury(treasury, resSeed);
    64	        fish.seedTreasury(treasury, resSeed);
    65	        iron.seedTreasury(treasury, resSeed);
    66	        gold.seedTreasury(treasury, totalGoldSeed);
    67	
    68	        wood.approve(address(game), resSeed);
    69	        wheat.approve(address(game), resSeed);
    70	        fish.approve(address(game), resSeed);
    71	        iron.approve(address(game), resSeed);
    72	        gold.approve(address(game), totalGoldSeed);
    73	
    74	        game.seedPools(
    75	            PoolSeedConfig({
    76	                woodSeed: resSeed,
    77	                wheatSeed: resSeed,
    78	                fishSeed: resSeed,
    79	                ironSeed: resSeed,
    80	                goldSeedForWood: goldSeed,
    81	                goldSeedForWheat: goldSeed,
    82	                goldSeedForFish: goldSeed,
    83	                goldSeedForIron: goldSeed
    84	            })
    85	        );
    86	
    87	        vm.stopBroadcast();
    88	    }
    89	}

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/ResourceBoundaryTokens.t.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "git show 9f93593:packages/contracts/test/SeedPools.t.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
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

codex
I’ve got the head snapshot loaded now. Next pass is checking whether the actual behavior lines up with the market spec sections the PR claims to satisfy, not just whether the new tests pass.
exec
/bin/bash -lc "rg -n \"5\\.5|5\\.6|5\\.7|11\\.2|Unicorn Town|market\" docs packages/contracts -g '*.md' -g '*.sol'" in /home/claude/code/clan-world
exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/HeartbeatOrdering.t.sol | nl -ba | sed -n '180,390p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   180	
   181	    // -------------------------------------------------------------------------
   182	    // test_heartbeat_settlementBeforeMarket
   183	    //
   184	    // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
   185	    // heartbeat closing tick T:
   186	    //   - cs0 is placed at Unicorn Town (region 3). Deposit to homebase Forest
   187	    //     (region 1) = 2 ticks travel. arrivalTick = T0+2, settlesAtTick = T0+3.
   188	    //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
   189	    //     settlesAtTick = T0+3. (Same tick as cs0 settles.)
   190	    //   - vault starts at 0; cs0 has carry wood.
   191	    //   - Heartbeat at T0+2: Step 1 deposits cs0 carry wood to vault, Step 2 sells
   192	    //     it. Gold increases only if Step 1 ran first (vault was 0 before Step 1).
   193	    //   - If ordering were reversed, sell would fail (vault still 0) and gold stays flat.
   194	    // -------------------------------------------------------------------------
   195	    function test_heartbeat_settlementBeforeMarket() public {
   196	        _setupMarket();
   197	        uint32 clanId = _mintClan();
   198	        uint32 csId0 = _firstCs(clanId);
   199	        uint32 csId1 = _secondCs(clanId);
   200	
   201	        uint64 t0 = world.getWorldState().currentTick;
   202	
   203	        // Place cs0 at Unicorn Town (region 3). Homebase = Forest (region 1).
   204	        // Deposit from Unicorn Town to Forest: travel = 2 ticks.
   205	        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_UNICORN_TOWN);
   206	
   207	        // cs0: submit Deposit. arrivalTick = t0+2, settlesAtTick = t0+3.
   208	        OrderResult[] memory r0 =
   209	            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
   210	        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
   211	        Mission memory depositMission = world.getActiveMission(csId0);
   212	        assertEq(depositMission.settlesAtTick, t0 + 3, "deposit settlesAtTick must be t0+3");
   213	
   214	        // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
   215	        // settlesAtTick = t0+3. Same tick as deposit settles.
   216	        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
   217	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
   218	        Mission memory sellMission = world.getActiveMission(csId1);
   219	        assertEq(sellMission.arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
   220	        assertEq(sellMission.settlesAtTick, t0 + 3, "sell settlesAtTick must be t0+3");
   221	
   222	        // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
   223	        world.setCarryWood(csId0, 10e18);
   224	        world.setVaultWood(clanId, 0);
   225	        assertEq(world.getClan(clanId).vaultWood, 0, "vault wood must be 0 before test tick");
   226	
   227	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   228	
   229	        // Advance to tick t0+4. The heartbeat closing t0+3 runs:
   230	        //   Step 1: _settleCompletingMissions(t0+3) → deposit fires, cs0 carry 10e18 → vault
   231	        //   Step 2: _executeScheduledMarketActions(t0+3) → sell fires, 5e18 vault wood → gold
   232	        // If reversed: sell would fail (vault=0), gold unchanged.
   233	        _advanceToTick(t0 + 4);
   234	
   235	        uint256 goldAfter = world.getClan(clanId).goldBalance;
   236	        assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
   237	        assertEq(world.getClansman(csId0).carryWood, 0, "cs0 carry wood cleared by deposit");
   238	        assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
   239	    }
   240	
   241	    // -------------------------------------------------------------------------
   242	    // test_heartbeat_atomicTickSeedPublish
   243	    //
   244	    // Proves Step 5: after heartbeat, currentTick increments by 1, currentTickSeed
   245	    // changes, and the new seed is deterministic from block.prevrandao + prior seed.
   246	    // -------------------------------------------------------------------------
   247	    function test_heartbeat_atomicTickSeedPublish() public {
   248	        WorldState memory before_ = world.getWorldState();
   249	        uint64 tickBefore = before_.currentTick;
   250	        bytes32 seedBefore = before_.currentTickSeed;
   251	
   252	        bytes32 expectedSeed = keccak256(abi.encode(block.prevrandao, seedBefore, tickBefore));
   253	        world.heartbeat();
   254	
   255	        WorldState memory after_ = world.getWorldState();
   256	        assertEq(after_.currentTick, tickBefore + 1, "tick must increment by 1");
   257	        assertNotEq(after_.currentTickSeed, seedBefore, "seed must change after heartbeat");
   258	        assertEq(after_.currentTickSeed, expectedSeed, "seed must match keccak(prevrandao, oldSeed, closedTick)");
   259	
   260	        // Second heartbeat chains from the new seed
   261	        uint64 tick2 = after_.currentTick;
   262	        bytes32 seed2 = after_.currentTickSeed;
   263	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   264	        bytes32 expectedSeed2 = keccak256(abi.encode(block.prevrandao, seed2, tick2));
   265	        world.heartbeat();
   266	
   267	        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
   268	        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
   269	    }
   270	
   271	    // -------------------------------------------------------------------------
   272	    // test_heartbeat_seasonTransition
   273	    //
   274	    // Proves Step 4 (world events) fires AFTER Steps 1-3:
   275	    //   - season boundary is crossed at SEASON_DURATION_TICKS
   276	    //   - currentSeasonNumber increments on the heartbeat that closes tick
   277	    //     SEASON_DURATION_TICKS-1 (newTick = SEASON_DURATION_TICKS >= seasonEndTick)
   278	    //   - no crash or revert when there are no pending missions or market actions
   279	    // -------------------------------------------------------------------------
   280	    function test_heartbeat_seasonTransition() public {
   281	        WorldState memory ws0 = world.getWorldState();
   282	        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
   283	        assertEq(ws0.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS, "seasonEndTick starts at 360");
   284	
   285	        // Advance SEASON_DURATION_TICKS heartbeats to cross the boundary
   286	        for (uint256 i = 0; i < ClanWorldConstants.SEASON_DURATION_TICKS; i++) {
   287	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   288	            world.heartbeat();
   289	        }
   290	
   291	        WorldState memory ws1 = world.getWorldState();
   292	        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");
   293	        assertEq(ws1.currentTick, ClanWorldConstants.SEASON_DURATION_TICKS, "tick must be at season boundary");
   294	        assertEq(ws1.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS, "new seasonStartTick");
   295	        assertEq(ws1.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2, "new seasonEndTick");
   296	    }
   297	
   298	    // -------------------------------------------------------------------------
   299	    // test_heartbeat_noopTick
   300	    //
   301	    // Heartbeat with no pending missions and no queued market actions must:
   302	    //   - not revert
   303	    //   - increment currentTick exactly by 1
   304	    //   - change currentTickSeed
   305	    // -------------------------------------------------------------------------
   306	    function test_heartbeat_noopTick() public {
   307	        uint64 tickBefore = world.getWorldState().currentTick;
   308	        bytes32 seedBefore = world.getWorldState().currentTickSeed;
   309	
   310	        world.heartbeat();
   311	
   312	        assertEq(world.getWorldState().currentTick, tickBefore + 1, "tick must increment");
   313	        assertNotEq(world.getWorldState().currentTickSeed, seedBefore, "seed must change");
   314	    }
   315	
   316	    // -------------------------------------------------------------------------
   317	    // test_heartbeat_multipleStepsInOneTick
   318	    //
   319	    // Proves that when a mission settles at tick T AND a market action is queued at
   320	    // tick T, both fire in the same heartbeat without conflict.
   321	    //   - cs0 placed at Unicorn Town, deposits to Forest homebase: settlesAtTick = T0+3
   322	    //   - cs1 sells wood to UT from Forest: settlesAtTick = T0+3
   323	    //   Both succeed in the same heartbeat call at T0+3.
   324	    // -------------------------------------------------------------------------
   325	    function test_heartbeat_multipleStepsInOneTick() public {
   326	        _setupMarket();
   327	        uint32 clanId = _mintClan();
   328	        uint32 csId0 = _firstCs(clanId);
   329	        uint32 csId1 = _secondCs(clanId);
   330	
   331	        uint64 t0 = world.getWorldState().currentTick;
   332	
   333	        // cs0: placed at Unicorn Town (2 ticks from Forest homebase).
   334	        // Deposit: arrivalTick = t0+2, settlesAtTick = t0+3.
   335	        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_UNICORN_TOWN);
   336	        world.setCarryWood(csId0, 10e18);
   337	        OrderResult[] memory r0 =
   338	            _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
   339	        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
   340	        assertEq(world.getActiveMission(csId0).settlesAtTick, t0 + 3, "deposit settlesAtTick must be t0+3");
   341	
   342	        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. settlesAtTick = t0+3.
   343	        // Vault has 20e18 starter wood — sell always has enough.
   344	        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
   345	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
   346	        assertEq(world.getActiveMission(csId1).arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
   347	        assertEq(world.getActiveMission(csId1).settlesAtTick, t0 + 3, "sell settlesAtTick must be t0+3");
   348	
   349	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   350	
   351	        // Advance to tick t0+4 (the heartbeat closing t0+3 runs step1+step2 together).
   352	        _advanceToTick(t0 + 4);
   353	
   354	        // Both cs0 deposit (settled at T0+3) and cs1 sell (at T0+3) must have fired.
   355	        assertEq(world.getClansman(csId0).carryWood, 0, "deposit settled: carry cleared");
   356	        assertGt(world.getClan(clanId).goldBalance, goldBefore, "sell executed: gold increased");
   357	        assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
   358	    }
   359	}

 succeeded in 0ms:
docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md:7:	3.	ScheduledMarketActionCommitted is a new event — fires when a market mission’s scheduled action is queued. The UI uses this to show “trade pending” indicators before execution.
packages/contracts/test/MissionTiming.t.sol:57:            marketToken: address(0),
packages/contracts/test/MissionTiming.t.sol:58:            marketAmount: 0,
packages/contracts/test/MissionTiming.t.sol:133:        assertEq(world.getActionDuration(ActionType.MarketBuy), 1, "market buy");
packages/contracts/test/MissionTiming.t.sol:134:        assertEq(world.getActionDuration(ActionType.MarketSell), 1, "market sell");
docs/planning/phase-3-test-spec.md:34:**Setup:** Deploy fresh `ClanWorld`. Force spawn probability to 100% (set `currentBanditSpawnChanceBps = 10_000`). Manipulate `tickSeed` (via known block hash / `vm.roll`) so region selection would land on Unicorn Town.
docs/planning/phase-3-test-spec.md:38:**Assert:** Either (a) no bandit spawns (spawn skipped due to excluded region) OR (b) bandit's `currentRegion` is NOT Unicorn Town and NOT Deep Sea.
packages/contracts/src/ClanWorld.sol:47:///         deposit, wheat harvest, travel, NOOP bypass, order validation, and market execution.
packages/contracts/src/ClanWorld.sol:86:    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
packages/contracts/src/ClanWorld.sol:650:            // Scheduled market actions: already enqueued at submitClanOrders time.
packages/contracts/src/ClanWorld.sol:2301:    ///         2. Execute scheduled market actions for closedTick (external calls).
packages/contracts/src/ClanWorld.sol:2318:        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
packages/contracts/src/ClanWorld.sol:2348:    ///      Called from heartbeat before market execution and tick increment.
packages/contracts/src/ClanWorld.sol:2720:        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
packages/contracts/src/ClanWorld.sol:2724:        m.marketToken = order.marketToken;
packages/contracts/src/ClanWorld.sol:2725:        m.marketAmount = order.marketAmount;
packages/contracts/src/ClanWorld.sol:2743:            marketToken: order.marketToken,
packages/contracts/src/ClanWorld.sol:2744:            marketAmount: order.marketAmount,
packages/contracts/src/ClanWorld.sol:2754:            order.marketToken,
packages/contracts/src/ClanWorld.sol:2755:            order.marketAmount,
packages/contracts/src/ClanWorld.sol:2798:    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
packages/contracts/src/ClanWorld.sol:2828:                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
packages/contracts/src/ClanWorld.sol:2840:                    sma.marketToken,
packages/contracts/src/ClanWorld.sol:2841:                    sma.marketAmount,
packages/contracts/src/ClanWorld.sol:2959:    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
packages/contracts/src/ClanWorld.sol:2992:    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
packages/contracts/src/ClanWorld.sol:3091:        // MarketBuy/MarketSell: must target Unicorn Town
packages/contracts/src/ClanWorld.sol:3097:            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
packages/contracts/src/ClanWorld.sol:3099:            address tok = order.marketToken;
packages/contracts/src/ClanWorld.sol:3112:                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
packages/contracts/src/ClanWorld.sol:3157:    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
packages/contracts/test/BanditAttackResolution.t.sol:143:                marketToken: address(0),
packages/contracts/test/BanditAttackResolution.t.sol:144:                marketAmount: 0,
packages/contracts/test/HeartbeatOrdering.t.sol:164:            marketToken: address(0),
packages/contracts/test/HeartbeatOrdering.t.sol:165:            marketAmount: 0,
packages/contracts/test/HeartbeatOrdering.t.sol:186:            marketToken: token,
packages/contracts/test/HeartbeatOrdering.t.sol:187:            marketAmount: amount,
packages/contracts/test/HeartbeatOrdering.t.sol:277:    // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
packages/contracts/test/HeartbeatOrdering.t.sol:309:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
packages/contracts/test/HeartbeatOrdering.t.sol:313:        // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
packages/contracts/test/HeartbeatOrdering.t.sol:327:        assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
packages/contracts/test/HeartbeatOrdering.t.sol:368:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
packages/contracts/test/HeartbeatOrdering.t.sol:381:        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
packages/contracts/test/HeartbeatOrdering.t.sol:382:        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
packages/contracts/test/HeartbeatOrdering.t.sol:385:        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
packages/contracts/test/HeartbeatOrdering.t.sol:396:    //   - no crash or revert when there are no pending missions or market actions
packages/contracts/test/HeartbeatOrdering.t.sol:419:    // Heartbeat with no pending missions and no queued market actions must:
packages/contracts/test/HeartbeatOrdering.t.sol:437:    // Proves that when a mission settles at tick T AND a market action is queued at
packages/contracts/test/HeartbeatOrdering.t.sol:462:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
packages/contracts/test/Reentrancy.t.sol:116:    function test_marketPoolHeartbeatCallback_revertsWithReentrancyGuard() public {
packages/contracts/test/Reentrancy.t.sol:124:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "market sell should enqueue");
packages/contracts/test/Reentrancy.t.sol:135:        assertGt(world.getClan(clanId).goldBalance, goldBefore, "heartbeat should finish original market action");
packages/contracts/test/Reentrancy.t.sol:202:            marketToken: token,
packages/contracts/test/Reentrancy.t.sol:203:            marketAmount: amount,
docs/planning/clanworld_v4_3_schema_patch.md:23:For immediate market actions:
docs/planning/clanworld_v4_3_schema_patch.md:97:Scheduled market actions are stored as:
docs/planning/clanworld_v4_3_schema_patch.md:108:Within a given `executeAtTick`, scheduled market actions execute in:
docs/planning/clanworld_v4_3_schema_patch.md:275:`ERR_CARRY_FULL` is especially relevant for explicit market buy validation and any direct action that is rejected because requested output cannot fit remaining carry capacity.
docs/planning/clanworld_v4_3_schema_patch.md:318:- scheduled market execution
docs/planning/clanworld_v4_3_schema_patch.md:344:- scheduled market actions stored by `executeAtTick`
packages/contracts/src/IClanWorld.sol:206:    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
packages/contracts/src/IClanWorld.sol:294:    MarketExecutionMode marketMode;
packages/contracts/src/IClanWorld.sol:297:    address marketToken; // market token for buy/sell
packages/contracts/src/IClanWorld.sol:298:    uint256 marketAmount; // exact-in for sell, exact-out for buy
packages/contracts/src/IClanWorld.sol:299:    uint256 maxGoldIn; // market_buy only, 0 otherwise
packages/contracts/src/IClanWorld.sol:325:    address marketToken;
packages/contracts/src/IClanWorld.sol:326:    uint256 marketAmount; // exact-in for sell, exact-out for buy
packages/contracts/src/IClanWorld.sol:369:    address marketToken;
packages/contracts/src/IClanWorld.sol:370:    uint256 marketAmount;
packages/contracts/src/IClanWorld.sol:547:    // ----- market -----
packages/contracts/src/IClanWorld.sol:573:        address marketToken,
packages/contracts/src/IClanWorld.sol:574:        uint256 marketAmount,
packages/contracts/src/IClanWorld.sol:654:    ///         scheduled market actions and world events, advances the tick.
packages/contracts/src/IClanWorld.sol:687:    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
packages/contracts/src/IClanWorld.sol:786:    /// @notice Single-call market panel data: 4 pool reserves + spot prices +
packages/contracts/src/IClanWorld.sol:787:    ///         scheduled queues for current and next tick. Drives market UI
docs/reviews/pr194-r2-codereview-codex-5-5.md:5:model: gpt-5.5
docs/reviews/pr194-r2-codereview-codex-5-5.md:695:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr194-r2-codereview-codex-5-5.md:2474:     ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2499:         // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2940:     uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/pr194-r2-codereview-codex-5-5.md:3364:+                marketToken: address(0),
docs/reviews/pr194-r2-codereview-codex-5-5.md:3365:+                marketAmount: 0,
docs/reviews/pr194-r2-codereview-codex-5-5.md:4051:+            marketToken: address(0),
docs/reviews/pr194-r2-codereview-codex-5-5.md:4052:+            marketAmount: 0,
docs/reviews/pr194-r2-codereview-codex-5-5.md:4060:+            marketToken: address(0),
docs/reviews/pr194-r2-codereview-codex-5-5.md:4061:+            marketAmount: 0,
docs/reviews/pr194-r2-codereview-codex-5-5.md:4671:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4684:+        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4685:+        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4688:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4918:-                      { name: 'marketMode', type: 'uint8' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4920:-                      { name: 'marketToken', type: 'address' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4921:-                      { name: 'marketAmount', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4943:-                  { name: 'marketMode', type: 'uint8' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4945:-                  { name: 'marketToken', type: 'address' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4946:-                  { name: 'marketAmount', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4992:-          { name: 'marketToken', type: 'address' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4993:-          { name: 'marketAmount', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5195:+        version: 3.2.4(@types/node@25.6.0)(lightningcss@1.32.0)
docs/reviews/pr194-r2-codereview-codex-5-5.md:6189:  2301	    ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/pr194-r2-codereview-codex-5-5.md:6206:  2318	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/pr194-r2-codereview-codex-5-5.md:6236:  2348	    ///      Called from heartbeat before market execution and tick increment.
docs/reviews/pr194-r2-codereview-codex-5-5.md:6448:   650	            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/pr194-r2-codereview-codex-5-5.md:6589:   206	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/pr194-r2-codereview-codex-5-5.md:6601:   297	    address marketToken; // market token for buy/sell
docs/reviews/pr194-r2-codereview-codex-5-5.md:6602:   298	    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/pr194-r2-codereview-codex-5-5.md:6603:   299	    uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/pr194-r2-codereview-codex-5-5.md:6764:   105	        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6765:   106	        marketAmount: 0n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6916:  2348	    ///      Called from heartbeat before market execution and tick increment.
docs/reviews/pr194-r2-codereview-codex-5-5.md:7439:    "@types/node": "25.6.0",
docs/reviews/pr194-r2-codereview-codex-5-5.md:7496:    "@types/node": "25.6.0",
docs/reviews/pr194-r2-codereview-codex-5-5.md:8003:     9	    uint256 internal constant DOMAIN_MARKET_NOISE = uint256(keccak256("clanworld.market.noise.v1"));
docs/reviews/pr194-r2-codereview-codex-5-5.md:8316:  2720	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/pr194-r2-codereview-codex-5-5.md:8320:  2724	        m.marketToken = order.marketToken;
docs/reviews/pr194-r2-codereview-codex-5-5.md:8321:  2725	        m.marketAmount = order.marketAmount;
docs/reviews/pr194-r2-codereview-codex-5-5.md:8418:  3091	        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/pr194-r2-codereview-codex-5-5.md:8424:  3097	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
docs/reviews/pr194-r2-codereview-codex-5-5.md:8426:  3099	            address tok = order.marketToken;
docs/reviews/pr194-r2-codereview-codex-5-5.md:8439:  3112	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
packages/contracts/test/ClanWorld.t.sol:62:    // Phase 2 market infrastructure
packages/contracts/test/ClanWorld.t.sol:153:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:154:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:393:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:394:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:403:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:404:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:451:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:452:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:603:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:604:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:653:    // Helper: submit a market order for a specific clansman
packages/contracts/test/ClanWorld.t.sol:668:            marketToken: token,
packages/contracts/test/ClanWorld.t.sol:669:            marketAmount: amount,
packages/contracts/test/ClanWorld.t.sol:692:                marketToken: token,
packages/contracts/test/ClanWorld.t.sol:693:                marketAmount: 1e18,
packages/contracts/test/ClanWorld.t.sol:700:            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "market sell should enqueue");
packages/contracts/test/ClanWorld.t.sol:722:        // Submit sell order — clansman travels to Unicorn Town then executes at actionStartTick
packages/contracts/test/ClanWorld.t.sol:1047:            marketToken: woodAddr,
packages/contracts/test/ClanWorld.t.sol:1048:            marketAmount: 2e18,
packages/contracts/test/ClanWorld.t.sol:1062:            marketToken: woodAddr,
packages/contracts/test/ClanWorld.t.sol:1063:            marketAmount: 2e18,
packages/contracts/test/ClanWorld.t.sol:1122:            marketToken: woodAddr,
packages/contracts/test/ClanWorld.t.sol:1123:            marketAmount: 5e18,
packages/contracts/test/ClanWorld.t.sol:1136:            marketToken: woodAddr,
packages/contracts/test/ClanWorld.t.sol:1137:            marketAmount: 1e18,
packages/contracts/test/ClanWorld.t.sol:1165:    // Test 17: marketOrder_rejectsInvalidRegion — non-UT market order rejected
packages/contracts/test/ClanWorld.t.sol:1168:    function test_marketOrder_rejectsInvalidRegion() public {
packages/contracts/test/ClanWorld.t.sol:1173:        // Try to submit market sell to Forest (wrong region)
packages/contracts/test/ClanWorld.t.sol:1180:            marketToken: woodAddr,
packages/contracts/test/ClanWorld.t.sol:1181:            marketAmount: 1e18,
packages/contracts/test/ClanWorld.t.sol:1186:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "market sell to Forest should fail");
packages/contracts/test/ClanWorld.t.sol:1189:    function test_marketOrder_returnsErrorWhenTreasuryUninitialized() public {
packages/contracts/test/ClanWorld.t.sol:1192:        uint32 marketCsId = view_.clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:1198:            clansmanId: marketCsId,
packages/contracts/test/ClanWorld.t.sol:1202:            marketToken: address(0xBEEF),
packages/contracts/test/ClanWorld.t.sol:1203:            marketAmount: 1e18,
packages/contracts/test/ClanWorld.t.sol:1211:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1212:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1222:            "uninitialized treasury should be a per-order market error"
packages/contracts/test/ClanWorld.t.sol:1244:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1245:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1316:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1317:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1348:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1349:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1471:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1472:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1593:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1594:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1780:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1781:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1851:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1852:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1955:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1956:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1966:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1967:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1977:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1978:            marketAmount: 0,
packages/contracts/test/ClanWorld.t.sol:1988:            marketToken: address(0),
packages/contracts/test/ClanWorld.t.sol:1989:            marketAmount: 0,
packages/contracts/test/BanditSpawn.t.sol:131:            marketToken: address(0),
packages/contracts/test/BanditSpawn.t.sol:132:            marketAmount: 0,
packages/contracts/test/BanditSpawn.t.sol:140:            marketToken: address(0),
packages/contracts/test/BanditSpawn.t.sol:141:            marketAmount: 0,
packages/contracts/test/DefendBase.t.sol:42:            marketToken: address(0),
packages/contracts/test/DefendBase.t.sol:43:            marketAmount: 0,
packages/contracts/test/DefendBase.t.sol:56:            marketToken: address(0),
packages/contracts/test/DefendBase.t.sol:57:            marketAmount: 0,
packages/contracts/src/lib/RNG.sol:9:    uint256 internal constant DOMAIN_MARKET_NOISE = uint256(keccak256("clanworld.market.noise.v1"));
packages/contracts/src/ClanWorldStub.sol:193:            marketMode: MarketExecutionMode.None,
packages/contracts/src/ClanWorldStub.sol:195:            marketToken: address(0),
packages/contracts/src/ClanWorldStub.sol:196:            marketAmount: 0,
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
docs/planning/clanworld_v4_spec.md:502:# 5. Unicorn Town Market Model
docs/planning/clanworld_v4_spec.md:505:Unicorn Town is the only region where Uniswap market actions may be executed in v1.
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
docs/planning/clanworld_v4_spec.md:593:## 5.10 Failed market actions
docs/planning/clanworld_v4_spec.md:594:If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
docs/planning/clanworld_v4_spec.md:597:- clansman becomes or remains `WAITING` in Unicorn Town
docs/planning/clanworld_v4_spec.md:615:- market sales into seeded resource/gold pools
docs/planning/clanworld_v4_spec.md:638:Bandits may not spawn in Unicorn Town or Deep Sea.
docs/planning/clanworld_v4_spec.md:713:Bandits never enter Unicorn Town or Deep Sea.
docs/planning/clanworld_v4_spec.md:797:Bandits do not use Unicorn Town in v1.
docs/planning/clanworld_v4_spec.md:1057:## 11.2 Intentional gameplay consequence
docs/planning/clanworld_v4_spec.md:1108:This is intended to enable at least one meaningful early Unicorn Town trade.
docs/planning/clanworld_v4_spec.md:1165:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/clanworld_v4_spec.md:1168:- immediate market actions require the worker to be in Unicorn Town and `WAITING`
docs/planning/clanworld_v4_spec.md:1169:- scheduled market actions are never lazily resolved; they resolve eagerly at the heartbeat that closes their action tick
docs/planning/clanworld_v4_spec.md:1190:- scheduled vs immediate market execution split
docs/reviews/r2/pr194-codereview-codex-5-5.md:5:model: gpt-5.5
docs/reviews/r2/pr194-codereview-codex-5-5.md:716:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/r2/pr194-codereview-codex-5-5.md:2417:     ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2442:         // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2883:     uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/r2/pr194-codereview-codex-5-5.md:3293:+                marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-5.md:3294:+                marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-5.md:3877:+            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-5.md:3878:+            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-5.md:3886:+            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-5.md:3887:+            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-5.md:4438:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4451:+        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4452:+        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4455:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4585:    46	///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/r2/pr194-codereview-codex-5-5.md:4925:   365	            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/r2/pr194-codereview-codex-5-5.md:5137:   202	    uint64 nextCommitSequence;   // global FIFO sequence for scheduled market actions
docs/reviews/r2/pr194-codereview-codex-5-5.md:5222:   287	    MarketExecutionMode marketMode;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5225:   290	    address marketToken;   // market token for buy/sell
docs/reviews/r2/pr194-codereview-codex-5-5.md:5226:   291	    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-5.md:5227:   292	    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/reviews/r2/pr194-codereview-codex-5-5.md:5255:   320	    address marketToken;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5256:   321	    uint256 marketAmount;         // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-5.md:5311:docs/reviews/pr194-codereview-codex-5-5.md:716:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/r2/pr194-codereview-codex-5-5.md:5923:docs/reviews/pr194-codereview-codex-5-5.md:4455:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/r2/pr194-codereview-codex-5-5.md:6032:docs/reviews/pr194-codereview-codex-5-4.md:716:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/r2/pr194-codereview-codex-5-5.md:6644:docs/reviews/pr194-codereview-codex-5-4.md:4455:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/r2/pr194-codereview-codex-5-5.md:6720:docs/reviews/pr194-codereview-codex-5-4.md:5911:    /// @dev Execute all scheduled market actions for the given tick. Called from heartbeat.
docs/reviews/r2/pr194-codereview-codex-5-5.md:6725:docs/reviews/pr194-codereview-codex-5-4.md:6007:        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/r2/pr194-codereview-codex-5-5.md:6726:docs/reviews/pr194-codereview-codex-5-4.md:6030:                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/r2/pr194-codereview-codex-5-5.md:7969:  2222	    ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/r2/pr194-codereview-codex-5-5.md:7986:  2239	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/r2/pr194-codereview-codex-5-5.md:8016:  2269	    ///      Called from heartbeat before market execution and tick increment.
docs/reviews/r2/pr194-codereview-codex-5-5.md:8405:  3078	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
docs/reviews/r2/pr194-codereview-codex-5-5.md:8480:     9	    uint256 internal constant DOMAIN_MARKET_NOISE = uint256(keccak256("clanworld.market.noise.v1"));
docs/reviews/r2/pr194-codereview-codex-5-5.md:8927:   206	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/r2/pr194-codereview-codex-5-5.md:8946:   294	    MarketExecutionMode marketMode;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8949:   297	    address marketToken; // market token for buy/sell
docs/reviews/r2/pr194-codereview-codex-5-5.md:8950:   298	    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-5.md:8951:   299	    uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/r2/pr194-codereview-codex-5-5.md:9248:  2641	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/r2/pr194-codereview-codex-5-5.md:9252:  2645	        m.marketToken = order.marketToken;
docs/reviews/r2/pr194-codereview-codex-5-5.md:9253:  2646	        m.marketAmount = order.marketAmount;
docs/reviews/r2/pr194-codereview-codex-5-5.md:9269:  2675	            order.marketToken,
docs/reviews/r2/pr194-codereview-codex-5-5.md:9270:  2676	            order.marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-5.md:9313:  2719	    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
docs/reviews/r2/pr194-codereview-codex-5-5.md:9539:   193	            marketMode: MarketExecutionMode.None,
docs/reviews/r2/pr194-codereview-codex-5-5.md:9541:   195	            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-5.md:9542:   196	            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-5.md:10009:   687	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
docs/reviews/r2/pr194-codereview-codex-5-5.md:10498:   317	        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
docs/reviews/r2/pr194-codereview-codex-5-5.md:10499:   318	        marketAmount: 0n,
docs/reviews/r2/pr194-codereview-opus-4-6.md:114:1. **Seed timing refactor is correct and well-tested.** The seed is now derived at the end of heartbeat (step 8) and stored under the *opened* tick, not the closed tick. Settlement reads `_tickSeeds[tick]` which was stored by a prior heartbeat, so settlement RNG continues to work. The new `RecordingPool` test validates that market actions see the closed-tick seed. The formula chains correctly: `keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick))`.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:44:- market intent execution timing
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:59:2. resolve all scheduled market actions whose `executeAtTick == T`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:82:- scheduled market action resolution
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:88:### 2.6 Immediate market execution
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:89:Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:92:- requires the worker already be physically in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:214:- market sale proceeds
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:215:- market purchase spending
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:346:    uint64 nextCommitSequence;   // global monotonic FIFO sequence for scheduled market actions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:460:    MarketExecutionMode marketMode;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:463:    address marketToken;   // market token for buy/sell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:464:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:465:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:493:Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:503:    address marketToken;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:504:    uint256 marketAmount;    // exact-in for sell, exact-out for buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:571:- `marketAmount` = exact amount of resource to sell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:580:- `marketAmount` = exact amount of resource to buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:696:    address marketToken;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:697:    uint256 marketAmount;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:735:## 11. Randomness, market execution, and reentrancy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:755:### 11.2 Immediate vs scheduled market ordering
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:756:Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:758:Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:764:A scheduled market action:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:765:- is created when a travel-based market mission is accepted
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:934:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:938:- scheduled market actions are never lazily replayed; they resolve eagerly at heartbeat
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:940:- immediate market actions require `Waiting` + Unicorn Town + cooldown clear
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:958:### 16.2 Immediate market sell
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:960:- worker is `Waiting` in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:968:- worker stays in Unicorn Town
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:972:### 16.3 Scheduled market buy
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:974:- worker is one tick away from Unicorn Town in tick 307
docs/planning/clanworld_numbered_implementation_plan.md:175:- no market or bandits yet
docs/planning/clanworld_numbered_implementation_plan.md:193:2. resolve scheduled market actions
docs/planning/clanworld_numbered_implementation_plan.md:243:### 5.5 Implement deposit action
docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/clanworld_numbered_implementation_plan.md:260:## Phase 6 — Unicorn Town market integration
docs/planning/clanworld_numbered_implementation_plan.md:275:### 6.3 Implement immediate market actions
docs/planning/clanworld_numbered_implementation_plan.md:282:### 6.4 Implement scheduled market actions
docs/planning/clanworld_numbered_implementation_plan.md:287:### 6.5 Implement market failure semantics
docs/planning/clanworld_numbered_implementation_plan.md:296:### 6.6 Add events and market result surface
docs/planning/clanworld_numbered_implementation_plan.md:298:- immediate market action executed
docs/planning/clanworld_numbered_implementation_plan.md:299:- scheduled market action executed
docs/planning/clanworld_numbered_implementation_plan.md:300:- market action failed
docs/planning/clanworld_numbered_implementation_plan.md:307:**Cut line:** immediate market actions can be deferred if necessary, but scheduled market execution must exist.
docs/planning/clanworld_numbered_implementation_plan.md:454:### 11.2 Implement rank computation
docs/planning/clanworld_numbered_implementation_plan.md:487:- maybe market preview
docs/planning/clanworld_numbered_implementation_plan.md:504:3. trade in Unicorn Town
docs/planning/clanworld_numbered_implementation_plan.md:525:- Unicorn Town trades
docs/planning/clanworld_numbered_implementation_plan.md:532:- immediate market actions
docs/planning/clanworld_numbered_implementation_plan.md:559:7. market sell/buy  
docs/planning/clanworld_v1_implementation_profile.md:7:**ship a working, legible, demoable autonomous-agent strategy game with real onchain state transitions and real market interaction.**
docs/planning/clanworld_v1_implementation_profile.md:65:ERC20s exist at the **market / payout boundary**, not as the canonical live game-state store.
docs/planning/clanworld_v1_implementation_profile.md:90:- market actions are heartbeat-eager, not lazy
docs/planning/clanworld_v1_implementation_profile.md:97:**Immediate market actions**
docs/planning/clanworld_v1_implementation_profile.md:98:- worker already in Unicorn Town
docs/planning/clanworld_v1_implementation_profile.md:103:**Scheduled market actions**
docs/planning/clanworld_v1_implementation_profile.md:111:- `market_sell` = exact input, from worker carry to clan gold purse
docs/planning/clanworld_v1_implementation_profile.md:112:- `market_buy` = exact output with `maxGoldIn`, from clan gold purse to worker carry
docs/planning/clanworld_v1_implementation_profile.md:172:- market buy
docs/planning/clanworld_v1_implementation_profile.md:173:- market sell
docs/planning/clanworld_v1_implementation_profile.md:177:- Unicorn Town
docs/planning/clanworld_v1_implementation_profile.md:179:- scheduled and immediate market actions
docs/planning/clanworld_v1_implementation_profile.md:219:- market transparency is a feature
docs/planning/clanworld_v1_implementation_profile.md:244:- immediate market actions
docs/planning/clanworld_v1_implementation_profile.md:253:- Unicorn Town market
docs/planning/clanworld_v1_implementation_profile.md:272:### Milestone B — market alive
docs/planning/clanworld_v1_implementation_profile.md:274:- worker travels to Unicorn Town
docs/planning/clanworld_v1_implementation_profile.md:314:4. a Unicorn Town trade
docs/planning/clanworld_v4_1_addendum.md:69:- immediate market action missions submitted while already in Unicorn Town
docs/planning/clanworld_v4_1_addendum.md:109:> **[PATCHED 2026-04-26]** The original text of this section incorrectly stated that all v1 market actions are Exact Input. `market_buy` is Exact Output, not Exact Input. The corrected text is below. Authoritative source: `clanworld_v4_2_state_schema_interface_spec.md §8.4` and `IClanWorld.sol`.
docs/planning/clanworld_v4_1_addendum.md:111:### `market_sell`
docs/planning/clanworld_v4_1_addendum.md:112:`market_sell` is **Exact Input**.
docs/planning/clanworld_v4_1_addendum.md:113:- `marketAmount` = exact amount of resource token to sell
docs/planning/clanworld_v4_1_addendum.md:117:### `market_buy`
docs/planning/clanworld_v4_1_addendum.md:118:`market_buy` is **Exact Output**.
docs/planning/clanworld_v4_1_addendum.md:119:- `marketAmount` = exact amount of resource to receive
docs/planning/clanworld_v4_1_addendum.md:126:## A8. Immediate vs scheduled market execution ordering
docs/planning/clanworld_v4_1_addendum.md:130:### Immediate market actions
docs/planning/clanworld_v4_1_addendum.md:132:- physically in Unicorn Town,
docs/planning/clanworld_v4_1_addendum.md:136:then an Elder may submit a market mission that executes **immediately in that tx**.
docs/planning/clanworld_v4_1_addendum.md:140:### Scheduled market actions
docs/planning/clanworld_v4_1_addendum.md:141:If a market mission requires travel or action-tick maturation, it executes later at the heartbeat that closes its action tick.
docs/planning/clanworld_v4_1_addendum.md:143:Scheduled market actions execute:
docs/planning/clanworld_v4_1_addendum.md:148:An immediate market action during tick `T` can front-run scheduled market actions that will execute at the close of tick `T`.
docs/planning/clanworld_v4_1_addendum.md:150:This is intentional and part of the adversarial market design.
docs/planning/clanworld_v4_5_alignment_addendum.md:438:### 11.2 New vocabulary (adopt across both submissions)
docs/planning/clanworld_v4_5_alignment_addendum.md:471:elder world market                # getMarketState
docs/planning/clanworld_v4_4_ui_indexer_getters.md:122:**Purpose:** Single-call complete Unicorn Town market state including spot prices and the next two ticks of scheduled queue.
docs/planning/clanworld_v4_4_ui_indexer_getters.md:153:- The indexer stores each tick's spot price into Convex, building a per-resource price-history series for sparkline charts in the market panel.
docs/planning/clanworld_v4_4_ui_indexer_getters.md:154:- Drives: market panel, scheduled-trade ticker, per-resource price charts.
docs/planning/clanworld_v4_4_ui_indexer_getters.md:251:| `getMarketState()`              | ~5 calls           | Medium — market panel + price charts   |
docs/planning/CANONICAL_SPEC.md:29:**The conflict:** `clanworld_v4_1_addendum.md §A7` originally stated "All v1 market actions are Exact Input actions only" and described `market_buy` as spending an exact amount of gold. This contradicts `IClanWorld.sol`, `clanworld_v4_2_state_schema_interface_spec.md §8.3–8.4`, and `clanworld_v4_3_schema_patch.md`.
docs/planning/CANONICAL_SPEC.md:32:- `market_sell` — exact input: `marketAmount` = exact resource amount to sell; gold out is AMM-determined.
docs/planning/CANONICAL_SPEC.md:33:- `market_buy` — exact output: `marketAmount` = exact resource amount to receive; `maxGoldIn` = maximum gold willing to spend (slippage guard). Buy fails if required gold exceeds `maxGoldIn` at execution time.
docs/planning/CANONICAL_SPEC.md:45:v4.5 remains authoritative for: tick cadence per-submission (§2), agent vocabulary (Elder / orchestrator / toolbelt / situation block — §11.2), and §11.3 sections of `clanworld_agent_runner_spec.md` that remain valid.
docs/reviews/pr153-review-codex-5-3.md:43:| 8 | `packages/contracts/src/ClanWorld.sol` | 1345-1355 | Market token validation is gated behind treasury initialization; pre-init market actions can be accepted with invalid tokens and fail later, creating avoidable state/UX debt. | SHOULD FIX |
docs/reviews/pr194-r2-synthesis.md:3:**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.7 ✓ (partial) | Gemini 3.1 Pro ✗ (429 rate limit) | Opus 4.6 (skipped, weak signal R1)
docs/reviews/pr194-r2-synthesis.md:35:- **Codex 5.5:** NEEDS_FIXES — HIGH CLEAN, 2 MED, 1 LOW (declared "no new HIGH found")
docs/reviews/pr194-r2-synthesis.md:42:- R1 HIGHs verified fixed: **5/5** (cross-model agreement: Codex 5.4 + 5.5 + partial Opus 4.7 = 3 confirmations)
docs/reviews/pr194-r2-synthesis.md:43:- New HIGH findings: 1 (single-model only — Codex 5.4 sees it, Codex 5.5 explicitly says HIGH CLEAN)
docs/reviews/pr194-r2-synthesis.md:44:- New MED findings: 2-3 (Codex 5.5 surfaces some)
docs/reviews/pr153-review-claude-opus.md:42:| 5 | `contracts/src/ClanWorld.sol` | 1049 | **Unbounded per-tick market queue.** No cap on `_scheduledMarketActions[tick].length`; griefing via permissionless `mintClan` + mass scheduling can OOG the heartbeat. PR #137 #3 — NOT FIXED. | Security |
docs/reviews/pr153-review-claude-opus.md:43:| 6 | `contracts/script/Deploy.s.sol` | 15–37 | **Deploy script never calls `initTreasury` / `seedPools`.** Deployments leave market offline (`poolsSeeded == false`). PR #137 #7 — NOT FIXED. | Ops |
docs/reviews/pr153-review-claude-opus.md:190:- Add regression test: two same-type market orders for same tick, verify only latest executes
docs/reviews/pr153-review-claude-opus.md:235:- **Finding #1** (stale queue guard) is an **economic integrity bug** — stale market orders can execute with wrong parameters, corrupting vault and pool state.
docs/reviews/r2/pr194-codereview-codex-5-4.md:716:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/r2/pr194-codereview-codex-5-4.md:2417:     ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2442:         // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2883:     uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/r2/pr194-codereview-codex-5-4.md:3293:+                marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:3294:+                marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:3877:+            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:3878:+            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:3886:+            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:3887:+            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:4438:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4451:+        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4452:+        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4455:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4670:    uint64 nextCommitSequence;   // global FIFO sequence for scheduled market actions
docs/reviews/r2/pr194-codereview-codex-5-4.md:4782:packages/contracts/src/ClanWorld.sol:365:            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:4859:///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/r2/pr194-codereview-codex-5-4.md:5178:            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:5555:        // Phase 2: execute scheduled market actions for closedTick
docs/reviews/r2/pr194-codereview-codex-5-4.md:5858:        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/r2/pr194-codereview-codex-5-4.md:5862:        m.marketToken = order.marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5863:        m.marketAmount = order.marketAmount;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5879:            marketToken: order.marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5880:            marketAmount: order.marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5890:            order.marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5891:            order.marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5911:    /// @dev Execute all scheduled market actions for the given tick. Called from heartbeat.
docs/reviews/r2/pr194-codereview-codex-5-4.md:5937:                try this._executeMarketSellExternal(tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5943:                try this._executeMarketBuyExternal(tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn, sma.commitSequence) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:6007:        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/r2/pr194-codereview-codex-5-4.md:6012:            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6015:                address tok = order.marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6030:                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/r2/pr194-codereview-codex-5-4.md:6064:    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
docs/reviews/r2/pr194-codereview-codex-5-4.md:6504:    MarketExecutionMode marketMode;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6507:    address marketToken;   // market token for buy/sell
docs/reviews/r2/pr194-codereview-codex-5-4.md:6508:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-4.md:6509:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/reviews/r2/pr194-codereview-codex-5-4.md:6537:    address marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6538:    uint256 marketAmount;         // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-4.md:6581:    address marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6582:    uint256 marketAmount;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6779:    // ----- market -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:6805:        address marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:6806:        uint256 marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:6892:    ///         scheduled market actions and world events, advances the tick.
docs/reviews/r2/pr194-codereview-codex-5-4.md:6919:    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7071:    47	///         deposit, wheat harvest, travel, NOOP bypass, order validation, and market execution.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7109:    85	    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7665:   636	            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:9261:  2222	    ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/r2/pr194-codereview-codex-5-4.md:9278:  2239	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/r2/pr194-codereview-codex-5-4.md:9308:  2269	    ///      Called from heartbeat before market execution and tick increment.
docs/reviews/r2/pr194-codereview-codex-5-4.md:9685:  2641	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/r2/pr194-codereview-codex-5-4.md:9689:  2645	        m.marketToken = order.marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9690:  2646	        m.marketAmount = order.marketAmount;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9708:  2664	            marketToken: order.marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9709:  2665	            marketAmount: order.marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9719:  2675	            order.marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9720:  2676	            order.marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9763:  2719	    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
docs/reviews/r2/pr194-codereview-codex-5-4.md:9793:  2749	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
docs/reviews/r2/pr194-codereview-codex-5-4.md:9805:  2761	                    sma.marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9806:  2762	                    sma.marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9924:  2880	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/r2/pr194-codereview-codex-5-4.md:9957:  2913	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
docs/reviews/r2/pr194-codereview-codex-5-4.md:10056:  3012	        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/r2/pr194-codereview-codex-5-4.md:10062:  3018	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10064:  3020	            address tok = order.marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10077:  3033	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/r2/pr194-codereview-codex-5-4.md:10122:  3078	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
docs/reviews/r2/pr194-codereview-codex-5-4.md:10717:   206	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/r2/pr194-codereview-codex-5-4.md:10810:   294	    MarketExecutionMode marketMode;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10813:   297	    address marketToken; // market token for buy/sell
docs/reviews/r2/pr194-codereview-codex-5-4.md:10814:   298	    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-4.md:10815:   299	    uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/r2/pr194-codereview-codex-5-4.md:10841:   325	    address marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10842:   326	    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/r2/pr194-codereview-codex-5-4.md:10885:   369	    address marketToken;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10886:   370	    uint256 marketAmount;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10954:   573	        address marketToken,
docs/reviews/r2/pr194-codereview-codex-5-4.md:10955:   574	        uint256 marketAmount,
docs/reviews/r2/pr194-codereview-codex-5-4.md:11035:   654	    ///         scheduled market actions and world events, advances the tick.
docs/reviews/r2/pr194-codereview-codex-5-4.md:11068:   687	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:11294:   129	                marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:11295:   130	                marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:11750:./docs/planning/phase-3-test-spec.md:38:**Assert:** Either (a) no bandit spawns (spawn skipped due to excluded region) OR (b) bandit's `currentRegion` is NOT Unicorn Town and NOT Deep Sea.
docs/reviews/r2/pr194-codereview-codex-5-4.md:13254:   131	            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:13255:   132	            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13263:   140	            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:13264:   141	            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13436:   101	  { id: 'unicorn-town', name: 'Unicorn Town', ownerClanId: null },
docs/reviews/r2/pr194-codereview-codex-5-4.md:13514:   144	                      { name: 'marketMode', type: 'uint8' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:13516:   146	                      { name: 'marketToken', type: 'address' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:13517:   147	                      { name: 'marketAmount', type: 'uint256' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:13612:   193	            marketMode: MarketExecutionMode.None,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13614:   195	            marketToken: address(0),
docs/reviews/r2/pr194-codereview-codex-5-4.md:13615:   196	            marketAmount: 0,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13822:   368	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/r2/pr194-codereview-codex-5-4.md:13835:   381	        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
docs/reviews/r2/pr194-codereview-codex-5-4.md:13836:   382	        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
docs/reviews/r2/pr194-codereview-codex-5-4.md:13839:   385	        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/r2/pr194-codereview-codex-5-4.md:13850:   396	    //   - no crash or revert when there are no pending missions or market actions
docs/reviews/r2/pr194-codereview-codex-5-4.md:13873:   419	    // Heartbeat with no pending missions and no queued market actions must:
docs/reviews/r2/pr194-codereview-codex-5-4.md:14527:   713	Bandits never enter Unicorn Town or Deep Sea.
docs/reviews/r2/pr194-codereview-codex-5-4.md:14620:   797	Bandits do not use Unicorn Town in v1.
docs/reviews/r2/pr194-synthesis.md:3:**Models run:** Codex 5.4 ✓ | Codex 5.5 ✓ | Opus 4.6 ✓ | Opus 4.7 ✓ | Gemini 3.1 Pro ✓
docs/reviews/r2/pr194-synthesis.md:13:Two additional cross-model HIGHs emerged from Codex 5.4 + 5.5: stale handwritten ABI tuples in the TypeScript runner/shared client will misdecode the new `WorldState` shape, breaking heartbeat automation at deploy time.
docs/reviews/r2/pr194-synthesis.md:21:| H1 | `ClanWorld.sol:1551, 1654, 1679` | 5.4 + 5.5 + 4.7 + Gemini = **4/5** | HIGH | **Camped→Attacking dispatch missing in production heartbeat.** `_transitionBanditToAttacking` only called from test harnesses. `_advanceBanditStates` only handles Spawned→Camped + Resting→Camped. As merged, real bandits spawn, camp, and stay stuck. Attack resolution / loot split / blueprint reward / target death cleanup are all DEAD CODE in the live path. |
docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
docs/reviews/r2/pr194-synthesis.md:25:| H5 | `ClanWorld.sol:1717` | 5.5 = **1/5** | HIGH | **`_resolveBanditAttack` reverts on target-death-during-attack.** `_settleClan(targetClanId)` may kill the target → `_markClanDead` aborts the attacking bandit → state machine then tries to transition already-`Escaped` bandit again at `:1758`, which is invalid. Race window. Fix: re-check `targetClan.clanState` and `bandit.state/targetClanId` after target/defender settlement; return if cleanup already handled. |
docs/reviews/r2/pr194-synthesis.md:65:- **Codex 5.5:** NEEDS_FIXES — 4 HIGH (Camped→Attacking + 2 ABI + revert race)
docs/reviews/r2/pr194-synthesis.md:77:The 3 single-model HIGHs from Codex 5.4 + 5.5 are not single-model = "low confidence." They're file-system level findings (ABI tuples, blueprint unit) that other models simply didn't grep at that depth. All 5 are real and should be addressed.
docs/reviews/r2/pr194-codereview-opus-4-7.md:19:`ClanWorld.sol:~2210-2230` documents the heartbeat as 5 steps ("1. Settle… 2. Execute markets… 3. Eager-settle… 4. Advance bandit timers and resolve closed-tick bandit/world events… 5. Increment tick and publish the next tick seed atomically"), but the body has 8 inline-numbered steps (`Step 1`…`Step 8`), bandit spawn evaluation is its own step, and `_resolveWorldEvents` runs at step 7 not step 4. Stale natspec → reviewer/operator confusion. Trivial fix.
docs/reviews/pr198-codereview-codex-5-4.md:17:You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.
docs/reviews/pr198-codereview-codex-5-4.md:19:Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.
docs/reviews/pr198-codereview-codex-5-4.md:24:1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
docs/reviews/pr198-codereview-codex-5-4.md:25:2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
docs/reviews/pr198-codereview-codex-5-4.md:367:+              "name": "marketMode",
docs/reviews/pr198-codereview-codex-5-4.md:727:+    mapping(uint32 => uint64) private _marketMissionCommitSequence; // clansmanId => FIFO sequence captured at submit
docs/reviews/pr198-codereview-codex-5-4.md:737:-    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr198-codereview-codex-5-4.md:779:-            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/pr198-codereview-codex-5-4.md:876:+                        marketMode: MarketExecutionMode.None
docs/reviews/pr198-codereview-codex-5-4.md:905:+            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
docs/reviews/pr198-codereview-codex-5-4.md:907:+            if (marketStatus == StatusCode.OK) {
docs/reviews/pr198-codereview-codex-5-4.md:916:+            result.marketMode = MarketExecutionMode.Immediate;
docs/reviews/pr198-codereview-codex-5-4.md:940:+        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
docs/reviews/pr198-codereview-codex-5-4.md:945:         m.marketToken = order.marketToken;
docs/reviews/pr198-codereview-codex-5-4.md:946:         m.marketAmount = order.marketAmount;
docs/reviews/pr198-codereview-codex-5-4.md:949:+        if (m.marketMode == MarketExecutionMode.Scheduled) {
docs/reviews/pr198-codereview-codex-5-4.md:950:+            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
docs/reviews/pr198-codereview-codex-5-4.md:952:+            delete _marketMissionCommitSequence[cs.clansmanId];
docs/reviews/pr198-codereview-codex-5-4.md:969:+            commitSequence: _marketMissionCommitSequence[clansmanId],
docs/reviews/pr198-codereview-codex-5-4.md:974:-            marketToken: order.marketToken,
docs/reviews/pr198-codereview-codex-5-4.md:975:-            marketAmount: order.marketAmount,
docs/reviews/pr198-codereview-codex-5-4.md:978:+            marketToken: m.marketToken,
docs/reviews/pr198-codereview-codex-5-4.md:979:+            marketAmount: m.marketAmount,
docs/reviews/pr198-codereview-codex-5-4.md:989:-            order.marketToken,
docs/reviews/pr198-codereview-codex-5-4.md:990:-            order.marketAmount,
docs/reviews/pr198-codereview-codex-5-4.md:992:+            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
docs/reviews/pr198-codereview-codex-5-4.md:1000:-    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
docs/reviews/pr198-codereview-codex-5-4.md:1002:+    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
docs/reviews/pr198-codereview-codex-5-4.md:1048:-                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
docs/reviews/pr198-codereview-codex-5-4.md:1049:+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
docs/reviews/pr198-codereview-codex-5-4.md:1051:+                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-4.md:1057:+                    marketStatus;
docs/reviews/pr198-codereview-codex-5-4.md:1073:-                    sma.marketToken,
docs/reviews/pr198-codereview-codex-5-4.md:1074:-                    sma.marketAmount,
docs/reviews/pr198-codereview-codex-5-4.md:1077:+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
docs/reviews/pr198-codereview-codex-5-4.md:1079:+                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-4.md:1085:+                    marketStatus;
docs/reviews/pr198-codereview-codex-5-4.md:1146:+    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
docs/reviews/pr198-codereview-codex-5-4.md:1147:+    function _marketResourceForToken(address token) internal view returns (uint8) {
docs/reviews/pr198-codereview-codex-5-4.md:1153:+        revert("ClanWorld: invalid market resource");
docs/reviews/pr198-codereview-codex-5-4.md:1174:-    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/pr198-codereview-codex-5-4.md:1231:+            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
docs/reviews/pr198-codereview-codex-5-4.md:1233:+        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
docs/reviews/pr198-codereview-codex-5-4.md:1281:+                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:1393:+                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:1410:+    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/pr198-codereview-codex-5-4.md:1459:+            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:1468:     /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
docs/reviews/pr198-codereview-codex-5-4.md:1586:+            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:1610:+            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
docs/reviews/pr198-codereview-codex-5-4.md:1613:+            // Immediate market orders execute during submitClanOrders when the
docs/reviews/pr198-codereview-codex-5-4.md:1614:+            // worker is waiting in Unicorn Town and off cooldown. Other valid
docs/reviews/pr198-codereview-codex-5-4.md:1615:+            // market orders enqueue when the scheduled mission resolves.
docs/reviews/pr198-codereview-codex-5-4.md:1617:-                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/pr198-codereview-codex-5-4.md:1618:+                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
docs/reviews/pr198-codereview-codex-5-4.md:1797:+    MarketExecutionMode marketMode;
docs/reviews/pr198-codereview-codex-5-4.md:1819:     // ----- market -----
docs/reviews/pr198-codereview-codex-5-4.md:1856:         uint256 marketAmount,
docs/reviews/pr198-codereview-codex-5-4.md:2091:+    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-4.md:2104:+    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-4.md:2366:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "sell should be immediate");
docs/reviews/pr198-codereview-codex-5-4.md:2385:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market order should schedule while cooling down");
docs/reviews/pr198-codereview-codex-5-4.md:2386:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "cooldown should force scheduled mode");
docs/reviews/pr198-codereview-codex-5-4.md:2388:+        assertEq(uint8(m.marketMode), uint8(MarketExecutionMode.Scheduled), "mission should be scheduled");
docs/reviews/pr198-codereview-codex-5-4.md:2404:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
docs/reviews/pr198-codereview-codex-5-4.md:2405:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "out-of-town should be scheduled");
docs/reviews/pr198-codereview-codex-5-4.md:2421:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
docs/reviews/pr198-codereview-codex-5-4.md:2422:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "busy worker should be scheduled");
docs/reviews/pr198-codereview-codex-5-4.md:2453:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "buy should be immediate");
docs/reviews/pr198-codereview-codex-5-4.md:2482:+            uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed immediate action stays immediate"
docs/reviews/pr198-codereview-codex-5-4.md:2512:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
docs/reviews/pr198-codereview-codex-5-4.md:2541:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
docs/reviews/pr198-codereview-codex-5-4.md:2563:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
docs/reviews/pr198-codereview-codex-5-4.md:2578:-        // Submit sell order — clansman travels to Unicorn Town then executes at actionStartTick
docs/reviews/pr198-codereview-codex-5-4.md:2579:+        // Submit sell order — clansman travels to Unicorn Town then executes when the market mission settles
docs/reviews/pr198-codereview-codex-5-4.md:2716:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
docs/reviews/pr198-codereview-codex-5-4.md:2974:-            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-4.md:2975:-            marketAmount: 2e18,
docs/reviews/pr198-codereview-codex-5-4.md:2985:+                marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-4.md:2986:+                marketAmount: uint256(i + 1) * 1e18,
docs/reviews/pr198-codereview-codex-5-4.md:3005:-            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-4.md:3006:-            marketAmount: 2e18,
docs/reviews/pr198-codereview-codex-5-4.md:3135:+            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-4.md:3136:+            marketAmount: 5e18,
docs/reviews/pr198-codereview-codex-5-4.md:3160:     // Test 17: marketOrder_rejectsInvalidRegion — non-UT market order rejected
docs/reviews/pr198-codereview-codex-5-4.md:3278:+            marketToken: address(0),
docs/reviews/pr198-codereview-codex-5-4.md:3279:+            marketAmount: 0,
docs/reviews/pr198-codereview-codex-5-4.md:3384:     // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
docs/reviews/pr198-codereview-codex-5-4.md:3388:+    //   - cs0 is placed at Unicorn Town (region 3). Deposit to homebase Forest
docs/reviews/pr198-codereview-codex-5-4.md:3403:+        // Place cs0 at Unicorn Town (region 3). Homebase = Forest (region 1).
docs/reviews/pr198-codereview-codex-5-4.md:3404:+        // Deposit from Unicorn Town to Forest: travel = 2 ticks.
docs/reviews/pr198-codereview-codex-5-4.md:3421:         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/pr198-codereview-codex-5-4.md:3426:         // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
docs/reviews/pr198-codereview-codex-5-4.md:3443:         assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
docs/reviews/pr198-codereview-codex-5-4.md:3446:     // Proves that when a mission settles at tick T AND a market action is queued at
docs/reviews/pr198-codereview-codex-5-4.md:3451:+    //   - cs0 placed at Unicorn Town, deposits to Forest homebase: settlesAtTick = T0+3
docs/reviews/pr198-codereview-codex-5-4.md:3464:+        // cs0: placed at Unicorn Town (2 ticks from Forest homebase).
docs/reviews/pr198-codereview-codex-5-4.md:3479:         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
docs/reviews/pr198-codereview-codex-5-4.md:3802:I’m reviewing the Phase 6 market changes as an integrated whole, with the focus on state-machine seams, market execution timing, and the new ERC20 boundary flow. I’ll pull the contract/test context in parallel, then split a couple of bounded checks across subagents so we can compare notes before I write the final verdict.
docs/reviews/pr198-codereview-codex-5-4.md:3852:///         deposit, wheat harvest, travel, NOOP bypass, order validation, and market execution.
docs/reviews/pr198-codereview-codex-5-4.md:3891:    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr198-codereview-codex-5-4.md:4321:    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/pr198-codereview-codex-5-4.md:4527:I’ve confirmed the package-level contract guidance and I’m switching from the repo baseline to the actual `9f93593` snapshot so the review stays anchored to the PR head, not local history. In parallel, I’m splitting one pass on market/tick sequencing and one on pool/token/security so we can sanity-check the seam risks from two angles.
docs/reviews/pr198-codereview-codex-5-4.md:4548:The local checkout isn’t on `9f93593`, so I’m reading the PR head directly through `git show` and treating the stdin diff as the review surface. That should keep us from mixing in unrelated local history while we trace the actual market flow.
docs/reviews/pr198-codereview-codex-5-4.md:4603:1217:            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
docs/reviews/pr198-codereview-codex-5-4.md:4614:1625:            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
docs/reviews/pr198-codereview-codex-5-4.md:4615:1627:        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
docs/reviews/pr198-codereview-codex-5-4.md:4642:2034:            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
docs/reviews/pr198-codereview-codex-5-4.md:4643:2037:            // Immediate market orders execute during submitClanOrders when the
docs/reviews/pr198-codereview-codex-5-4.md:4791:  1105	                        marketMode: MarketExecutionMode.None
docs/reviews/pr198-codereview-codex-5-4.md:4903:  1217	            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
docs/reviews/pr198-codereview-codex-5-4.md:4905:  1219	            if (marketStatus == StatusCode.OK) {
docs/reviews/pr198-codereview-codex-5-4.md:4914:  1228	            result.marketMode = MarketExecutionMode.Immediate;
docs/reviews/pr198-codereview-codex-5-4.md:4970:  1284	        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
docs/reviews/pr198-codereview-codex-5-4.md:4992:  1306	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/pr198-codereview-codex-5-4.md:4996:  1310	        m.marketToken = order.marketToken;
docs/reviews/pr198-codereview-codex-5-4.md:4997:  1311	        m.marketAmount = order.marketAmount;
docs/reviews/pr198-codereview-codex-5-4.md:5000:  1314	        if (m.marketMode == MarketExecutionMode.Scheduled) {
docs/reviews/pr198-codereview-codex-5-4.md:5001:  1315	            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
docs/reviews/pr198-codereview-codex-5-4.md:5003:  1317	            delete _marketMissionCommitSequence[cs.clansmanId];
docs/reviews/pr198-codereview-codex-5-4.md:5011:  1325	            commitSequence: _marketMissionCommitSequence[clansmanId],
docs/reviews/pr198-codereview-codex-5-4.md:5016:  1330	            marketToken: m.marketToken,
docs/reviews/pr198-codereview-codex-5-4.md:5017:  1331	            marketAmount: m.marketAmount,
docs/reviews/pr198-codereview-codex-5-4.md:5022:  1336	            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
docs/reviews/pr198-codereview-codex-5-4.md:5031:  1378	    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
docs/reviews/pr198-codereview-codex-5-4.md:5074:  1421	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
docs/reviews/pr198-codereview-codex-5-4.md:5076:  1423	                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-4.md:5078:  1425	                    marketStatus;
docs/reviews/pr198-codereview-codex-5-4.md:5091:  1438	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
docs/reviews/pr198-codereview-codex-5-4.md:5093:  1440	                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-4.md:5095:  1442	                    marketStatus;
docs/reviews/pr198-codereview-codex-5-4.md:5158:  1505	    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
docs/reviews/pr198-codereview-codex-5-4.md:5159:  1506	    function _marketResourceForToken(address token) internal view returns (uint8) {
docs/reviews/pr198-codereview-codex-5-4.md:5165:  1512	        revert("ClanWorld: invalid market resource");
docs/reviews/pr198-codereview-codex-5-4.md:5278:  1625	            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
docs/reviews/pr198-codereview-codex-5-4.md:5280:  1627	        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
docs/reviews/pr198-codereview-codex-5-4.md:5328:  1675	                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:5433:  1780	                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:5450:  1797	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/pr198-codereview-codex-5-4.md:5496:  1843	            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:5505:  1852	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
docs/reviews/pr198-codereview-codex-5-4.md:5608:  1955	            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-4.md:5632:  2034	            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
docs/reviews/pr198-codereview-codex-5-4.md:5635:  2037	            // Immediate market orders execute during submitClanOrders when the
docs/reviews/pr198-codereview-codex-5-4.md:5636:  2038	            // worker is waiting in Unicorn Town and off cooldown. Other valid
docs/reviews/pr198-codereview-codex-5-4.md:5637:  2039	            // market orders enqueue when the scheduled mission resolves.
docs/reviews/pr198-codereview-codex-5-4.md:5639:  2041	                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
docs/reviews/pr198-codereview-codex-5-4.md:5680:  2082	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
docs/reviews/pr198-codereview-codex-5-4.md:6028:   211	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/pr198-codereview-codex-5-4.md:6293:    98	    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-4.md:6304:   109	    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-4.md:6669:I’ve got the head snapshot loaded now. Next pass is checking whether the actual behavior lines up with the market spec sections the PR claims to satisfy, not just whether the new tests pass.
docs/reviews/pr198-codereview-codex-5-5.md:5:model: gpt-5.5
docs/reviews/pr198-codereview-codex-5-5.md:17:You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.
docs/reviews/pr198-codereview-codex-5-5.md:19:Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.
docs/reviews/pr198-codereview-codex-5-5.md:24:1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
docs/reviews/pr198-codereview-codex-5-5.md:25:2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
docs/reviews/pr198-codereview-codex-5-5.md:367:+              "name": "marketMode",
docs/reviews/pr198-codereview-codex-5-5.md:727:+    mapping(uint32 => uint64) private _marketMissionCommitSequence; // clansmanId => FIFO sequence captured at submit
docs/reviews/pr198-codereview-codex-5-5.md:737:-    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr198-codereview-codex-5-5.md:779:-            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/pr198-codereview-codex-5-5.md:876:+                        marketMode: MarketExecutionMode.None
docs/reviews/pr198-codereview-codex-5-5.md:905:+            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
docs/reviews/pr198-codereview-codex-5-5.md:907:+            if (marketStatus == StatusCode.OK) {
docs/reviews/pr198-codereview-codex-5-5.md:916:+            result.marketMode = MarketExecutionMode.Immediate;
docs/reviews/pr198-codereview-codex-5-5.md:940:+        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
docs/reviews/pr198-codereview-codex-5-5.md:945:         m.marketToken = order.marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:946:         m.marketAmount = order.marketAmount;
docs/reviews/pr198-codereview-codex-5-5.md:949:+        if (m.marketMode == MarketExecutionMode.Scheduled) {
docs/reviews/pr198-codereview-codex-5-5.md:950:+            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
docs/reviews/pr198-codereview-codex-5-5.md:952:+            delete _marketMissionCommitSequence[cs.clansmanId];
docs/reviews/pr198-codereview-codex-5-5.md:969:+            commitSequence: _marketMissionCommitSequence[clansmanId],
docs/reviews/pr198-codereview-codex-5-5.md:974:-            marketToken: order.marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:975:-            marketAmount: order.marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:978:+            marketToken: m.marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:979:+            marketAmount: m.marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:989:-            order.marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:990:-            order.marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:992:+            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
docs/reviews/pr198-codereview-codex-5-5.md:1000:-    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
docs/reviews/pr198-codereview-codex-5-5.md:1002:+    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
docs/reviews/pr198-codereview-codex-5-5.md:1048:-                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
docs/reviews/pr198-codereview-codex-5-5.md:1049:+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
docs/reviews/pr198-codereview-codex-5-5.md:1051:+                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-5.md:1057:+                    marketStatus;
docs/reviews/pr198-codereview-codex-5-5.md:1073:-                    sma.marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:1074:-                    sma.marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:1077:+                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
docs/reviews/pr198-codereview-codex-5-5.md:1079:+                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-5.md:1085:+                    marketStatus;
docs/reviews/pr198-codereview-codex-5-5.md:1146:+    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
docs/reviews/pr198-codereview-codex-5-5.md:1147:+    function _marketResourceForToken(address token) internal view returns (uint8) {
docs/reviews/pr198-codereview-codex-5-5.md:1153:+        revert("ClanWorld: invalid market resource");
docs/reviews/pr198-codereview-codex-5-5.md:1174:-    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/pr198-codereview-codex-5-5.md:1231:+            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
docs/reviews/pr198-codereview-codex-5-5.md:1233:+        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
docs/reviews/pr198-codereview-codex-5-5.md:1281:+                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:1393:+                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:1410:+    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/pr198-codereview-codex-5-5.md:1459:+            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:1468:     /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
docs/reviews/pr198-codereview-codex-5-5.md:1586:+            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:1610:+            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
docs/reviews/pr198-codereview-codex-5-5.md:1613:+            // Immediate market orders execute during submitClanOrders when the
docs/reviews/pr198-codereview-codex-5-5.md:1614:+            // worker is waiting in Unicorn Town and off cooldown. Other valid
docs/reviews/pr198-codereview-codex-5-5.md:1615:+            // market orders enqueue when the scheduled mission resolves.
docs/reviews/pr198-codereview-codex-5-5.md:1617:-                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/pr198-codereview-codex-5-5.md:1618:+                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
docs/reviews/pr198-codereview-codex-5-5.md:1797:+    MarketExecutionMode marketMode;
docs/reviews/pr198-codereview-codex-5-5.md:1819:     // ----- market -----
docs/reviews/pr198-codereview-codex-5-5.md:1856:         uint256 marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:2091:+    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-5.md:2104:+    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-5.md:2366:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "sell should be immediate");
docs/reviews/pr198-codereview-codex-5-5.md:2385:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market order should schedule while cooling down");
docs/reviews/pr198-codereview-codex-5-5.md:2386:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "cooldown should force scheduled mode");
docs/reviews/pr198-codereview-codex-5-5.md:2388:+        assertEq(uint8(m.marketMode), uint8(MarketExecutionMode.Scheduled), "mission should be scheduled");
docs/reviews/pr198-codereview-codex-5-5.md:2404:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
docs/reviews/pr198-codereview-codex-5-5.md:2405:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "out-of-town should be scheduled");
docs/reviews/pr198-codereview-codex-5-5.md:2421:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
docs/reviews/pr198-codereview-codex-5-5.md:2422:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "busy worker should be scheduled");
docs/reviews/pr198-codereview-codex-5-5.md:2453:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "buy should be immediate");
docs/reviews/pr198-codereview-codex-5-5.md:2482:+            uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed immediate action stays immediate"
docs/reviews/pr198-codereview-codex-5-5.md:2512:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
docs/reviews/pr198-codereview-codex-5-5.md:2541:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
docs/reviews/pr198-codereview-codex-5-5.md:2563:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
docs/reviews/pr198-codereview-codex-5-5.md:2578:-        // Submit sell order — clansman travels to Unicorn Town then executes at actionStartTick
docs/reviews/pr198-codereview-codex-5-5.md:2579:+        // Submit sell order — clansman travels to Unicorn Town then executes when the market mission settles
docs/reviews/pr198-codereview-codex-5-5.md:2716:+        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
docs/reviews/pr198-codereview-codex-5-5.md:2974:-            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:2975:-            marketAmount: 2e18,
docs/reviews/pr198-codereview-codex-5-5.md:2985:+                marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:2986:+                marketAmount: uint256(i + 1) * 1e18,
docs/reviews/pr198-codereview-codex-5-5.md:3005:-            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:3006:-            marketAmount: 2e18,
docs/reviews/pr198-codereview-codex-5-5.md:3135:+            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:3136:+            marketAmount: 5e18,
docs/reviews/pr198-codereview-codex-5-5.md:3160:     // Test 17: marketOrder_rejectsInvalidRegion — non-UT market order rejected
docs/reviews/pr198-codereview-codex-5-5.md:3278:+            marketToken: address(0),
docs/reviews/pr198-codereview-codex-5-5.md:3279:+            marketAmount: 0,
docs/reviews/pr198-codereview-codex-5-5.md:3384:     // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
docs/reviews/pr198-codereview-codex-5-5.md:3388:+    //   - cs0 is placed at Unicorn Town (region 3). Deposit to homebase Forest
docs/reviews/pr198-codereview-codex-5-5.md:3403:+        // Place cs0 at Unicorn Town (region 3). Homebase = Forest (region 1).
docs/reviews/pr198-codereview-codex-5-5.md:3404:+        // Deposit from Unicorn Town to Forest: travel = 2 ticks.
docs/reviews/pr198-codereview-codex-5-5.md:3421:         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/pr198-codereview-codex-5-5.md:3426:         // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
docs/reviews/pr198-codereview-codex-5-5.md:3443:         assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
docs/reviews/pr198-codereview-codex-5-5.md:3446:     // Proves that when a mission settles at tick T AND a market action is queued at
docs/reviews/pr198-codereview-codex-5-5.md:3451:+    //   - cs0 placed at Unicorn Town, deposits to Forest homebase: settlesAtTick = T0+3
docs/reviews/pr198-codereview-codex-5-5.md:3464:+        // cs0: placed at Unicorn Town (2 ticks from Forest homebase).
docs/reviews/pr198-codereview-codex-5-5.md:3479:         assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
docs/reviews/pr198-codereview-codex-5-5.md:3802:I’ll treat this as a final review rather than an implementation pass. I’m going to inspect the actual repo around the changed contracts/tests and use subagents for independent market/security and test/spec sweeps, then reconcile findings against the supplied diff.
docs/reviews/pr198-codereview-codex-5-5.md:4114:   206	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/pr198-codereview-codex-5-5.md:4202:   294	    MarketExecutionMode marketMode;
docs/reviews/pr198-codereview-codex-5-5.md:4205:   297	    address marketToken; // market token for buy/sell
docs/reviews/pr198-codereview-codex-5-5.md:4206:   298	    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/pr198-codereview-codex-5-5.md:4207:   299	    uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/pr198-codereview-codex-5-5.md:4233:   325	    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:4234:   326	    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/pr198-codereview-codex-5-5.md:4277:   369	    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:4278:   370	    uint256 marketAmount;
docs/reviews/pr198-codereview-codex-5-5.md:4455:   547	    // ----- market -----
docs/reviews/pr198-codereview-codex-5-5.md:4481:   573	        address marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:4482:   574	        uint256 marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:4562:   654	    ///         scheduled market actions and world events, advances the tick.
docs/reviews/pr198-codereview-codex-5-5.md:4595:   687	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
docs/reviews/pr198-codereview-codex-5-5.md:5636:/bin/bash -lc "rg -n \"Unicorn|market|Market|FIFO|slippage|gold|pool|5\\.5|5\\.6|5\\.7|11\\.2\" docs/planning/clanworld_v4_spec.md docs/planning/clanworld_v4_1_addendum.md docs/planning/clanworld_v4_2_state_schema_interface_spec.md docs/planning/clanworld_v4_3_schema_patch.md docs/planning/CANONICAL_SPEC.md docs/planning/V1/01\\ Blockchain\\ Game\\ Spec/clanworld_v4_spec.md docs/planning/V1/01\\ Blockchain\\ Game\\ Spec/clanworld_v4_2_state_schema_interface_spec.md" in /home/claude/code/clan-world
docs/reviews/pr198-codereview-codex-5-5.md:5639:docs/planning/CANONICAL_SPEC.md:29:**The conflict:** `clanworld_v4_1_addendum.md §A7` originally stated "All v1 market actions are Exact Input actions only" and described `market_buy` as spending an exact amount of gold. This contradicts `IClanWorld.sol`, `clanworld_v4_2_state_schema_interface_spec.md §8.3–8.4`, and `clanworld_v4_3_schema_patch.md`.
docs/reviews/pr198-codereview-codex-5-5.md:5640:docs/planning/CANONICAL_SPEC.md:32:- `market_sell` — exact input: `marketAmount` = exact resource amount to sell; gold out is AMM-determined.
docs/reviews/pr198-codereview-codex-5-5.md:5641:docs/planning/CANONICAL_SPEC.md:33:- `market_buy` — exact output: `marketAmount` = exact resource amount to receive; `maxGoldIn` = maximum gold willing to spend (slippage guard). Buy fails if required gold exceeds `maxGoldIn` at execution time.
docs/reviews/pr198-codereview-codex-5-5.md:5642:docs/planning/CANONICAL_SPEC.md:45:v4.5 remains authoritative for: tick cadence per-submission (§2), agent vocabulary (Elder / orchestrator / toolbelt / situation block — §11.2), and §11.3 sections of `clanworld_agent_runner_spec.md` that remain valid.
docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-5.md:5644:docs/planning/clanworld_v4_spec.md:39:3 = Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5645:docs/planning/clanworld_v4_spec.md:100:Homebases may exist across the world but may **not** be placed in Unicorn Town.
docs/reviews/pr198-codereview-codex-5-5.md:5646:docs/planning/clanworld_v4_spec.md:110:Deep Sea and Unicorn Town are not valid homebase spawn regions.
docs/reviews/pr198-codereview-codex-5-5.md:5647:docs/planning/clanworld_v4_spec.md:127:2. resolve all **scheduled market actions** that mature in the closing tick
docs/reviews/pr198-codereview-codex-5-5.md:5648:docs/planning/clanworld_v4_spec.md:162:- all scheduled market actions at heartbeat boundaries
docs/reviews/pr198-codereview-codex-5-5.md:5649:docs/planning/clanworld_v4_spec.md:212:- goto Unicorn Town, sell wood
docs/reviews/pr198-codereview-codex-5-5.md:5650:docs/planning/clanworld_v4_spec.md:215:- goto noop, sell wood immediately in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5651:docs/planning/clanworld_v4_spec.md:264:- `market_sell` requested with invalid params
docs/reviews/pr198-codereview-codex-5-5.md:5652:docs/planning/clanworld_v4_spec.md:317:- scheduled market buy
docs/reviews/pr198-codereview-codex-5-5.md:5653:docs/planning/clanworld_v4_spec.md:318:- scheduled market sell
docs/reviews/pr198-codereview-codex-5-5.md:5654:docs/planning/clanworld_v4_spec.md:323:V1 only includes **immediate market actions in Unicorn Town**.
docs/reviews/pr198-codereview-codex-5-5.md:5655:docs/planning/clanworld_v4_spec.md:340:This is especially important for immediate market actions in Unicorn Town.
docs/reviews/pr198-codereview-codex-5-5.md:5658:docs/planning/clanworld_v4_spec.md:502:# 5. Unicorn Town Market Model
docs/reviews/pr198-codereview-codex-5-5.md:5660:docs/planning/clanworld_v4_spec.md:505:Unicorn Town is the only region where Uniswap market actions may be executed in v1.
docs/reviews/pr198-codereview-codex-5-5.md:5665:docs/planning/clanworld_v4_spec.md:518:1. scheduled market action
docs/reviews/pr198-codereview-codex-5-5.md:5666:docs/planning/clanworld_v4_spec.md:519:2. immediate market action
docs/reviews/pr198-codereview-codex-5-5.md:5667:docs/planning/clanworld_v4_spec.md:521:## 5.4 Scheduled market action
docs/reviews/pr198-codereview-codex-5-5.md:5668:docs/planning/clanworld_v4_spec.md:522:Scheduled market action is used when the clansman is not already eligible to trade immediately.
docs/reviews/pr198-codereview-codex-5-5.md:5669:docs/planning/clanworld_v4_spec.md:525:- worker must travel to Unicorn Town first
docs/reviews/pr198-codereview-codex-5-5.md:5670:docs/planning/clanworld_v4_spec.md:526:- after arrival, the market action occupies one full action tick
docs/reviews/pr198-codereview-codex-5-5.md:5671:docs/planning/clanworld_v4_spec.md:530:- `goto Unicorn Town, action = sell wood`
docs/reviews/pr198-codereview-codex-5-5.md:5672:docs/planning/clanworld_v4_spec.md:534:- one action tick in Unicorn Town is consumed
docs/reviews/pr198-codereview-codex-5-5.md:5673:docs/planning/clanworld_v4_spec.md:536:- worker becomes `WAITING` in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5674:docs/planning/clanworld_v4_spec.md:538:## 5.5 Immediate market action
docs/reviews/pr198-codereview-codex-5-5.md:5675:docs/planning/clanworld_v4_spec.md:539:Immediate market action is allowed if and only if all of the following are true at tx execution time:
docs/reviews/pr198-codereview-codex-5-5.md:5676:docs/planning/clanworld_v4_spec.md:540:- clansman is physically in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5678:docs/planning/clanworld_v4_spec.md:548:- `action = market_buy` or `market_sell`
docs/reviews/pr198-codereview-codex-5-5.md:5679:docs/planning/clanworld_v4_spec.md:552:## 5.6 Immediate market action effects
docs/reviews/pr198-codereview-codex-5-5.md:5680:docs/planning/clanworld_v4_spec.md:553:On successful immediate market action:
docs/reviews/pr198-codereview-codex-5-5.md:5681:docs/planning/clanworld_v4_spec.md:555:- the worker remains in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5682:docs/planning/clanworld_v4_spec.md:560:Immediate market actions are therefore still **missions**, not an out-of-band bypass.
docs/reviews/pr198-codereview-codex-5-5.md:5683:docs/planning/clanworld_v4_spec.md:562:## 5.7 Immediate vs scheduled collision ordering
docs/reviews/pr198-codereview-codex-5-5.md:5684:docs/planning/clanworld_v4_spec.md:563:If immediate and scheduled market actions affect the same pool in the same tick:
docs/reviews/pr198-codereview-codex-5-5.md:5685:docs/planning/clanworld_v4_spec.md:564:- immediate market actions execute against the current pool state at the time their Elder tx lands
docs/reviews/pr198-codereview-codex-5-5.md:5686:docs/planning/clanworld_v4_spec.md:565:- scheduled market actions execute later, at the heartbeat that closes the tick, in deterministic FIFO order by mission commit order
docs/reviews/pr198-codereview-codex-5-5.md:5687:docs/planning/clanworld_v4_spec.md:567:This intentionally allows a clansman already stationed in Unicorn Town to front-run scheduled trades that will execute at tick close.
docs/reviews/pr198-codereview-codex-5-5.md:5688:docs/planning/clanworld_v4_spec.md:569:This is an intended feature of the game’s adversarial market design, not a bug.
docs/reviews/pr198-codereview-codex-5-5.md:5689:docs/planning/clanworld_v4_spec.md:571:## 5.8 Scheduled market action ordering
docs/reviews/pr198-codereview-codex-5-5.md:5690:docs/planning/clanworld_v4_spec.md:572:If multiple scheduled market actions mature in the same tick, Keeper executes them in deterministic FIFO order based on mission commit order.
docs/reviews/pr198-codereview-codex-5-5.md:5691:docs/planning/clanworld_v4_spec.md:574:Each swap sees updated market state from earlier swaps in that sequence.
docs/reviews/pr198-codereview-codex-5-5.md:5692:docs/planning/clanworld_v4_spec.md:577:For v1, market actions execute with **no slippage guard**.
docs/reviews/pr198-codereview-codex-5-5.md:5693:docs/planning/clanworld_v4_spec.md:584:- scheduled market actions resolved at heartbeat close
docs/reviews/pr198-codereview-codex-5-5.md:5694:docs/planning/clanworld_v4_spec.md:585:- immediate market actions executed in Elder txs while already in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5695:docs/planning/clanworld_v4_spec.md:588:- adversarial market play
docs/reviews/pr198-codereview-codex-5-5.md:5697:docs/planning/clanworld_v4_spec.md:593:## 5.10 Failed market actions
docs/reviews/pr198-codereview-codex-5-5.md:5698:docs/planning/clanworld_v4_spec.md:594:If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
docs/reviews/pr198-codereview-codex-5-5.md:5699:docs/planning/clanworld_v4_spec.md:597:- clansman becomes or remains `WAITING` in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5707:docs/planning/clanworld_v4_spec.md:615:- market sales into seeded resource/gold pools
docs/reviews/pr198-codereview-codex-5-5.md:5710:docs/planning/clanworld_v4_spec.md:638:Bandits may not spawn in Unicorn Town or Deep Sea.
docs/reviews/pr198-codereview-codex-5-5.md:5713:docs/planning/clanworld_v4_spec.md:713:Bandits never enter Unicorn Town or Deep Sea.
docs/reviews/pr198-codereview-codex-5-5.md:5716:docs/planning/clanworld_v4_spec.md:797:Bandits do not use Unicorn Town in v1.
docs/reviews/pr198-codereview-codex-5-5.md:5717:docs/planning/clanworld_v4_spec.md:1057:## 11.2 Intentional gameplay consequence
docs/reviews/pr198-codereview-codex-5-5.md:5720:docs/planning/clanworld_v4_spec.md:1108:This is intended to enable at least one meaningful early Unicorn Town trade.
docs/reviews/pr198-codereview-codex-5-5.md:5722:docs/planning/clanworld_v4_spec.md:1165:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/reviews/pr198-codereview-codex-5-5.md:5723:docs/planning/clanworld_v4_spec.md:1168:- immediate market actions require the worker to be in Unicorn Town and `WAITING`
docs/reviews/pr198-codereview-codex-5-5.md:5724:docs/planning/clanworld_v4_spec.md:1169:- scheduled market actions are never lazily resolved; they resolve eagerly at the heartbeat that closes their action tick
docs/reviews/pr198-codereview-codex-5-5.md:5725:docs/planning/clanworld_v4_spec.md:1190:- scheduled vs immediate market execution split
docs/reviews/pr198-codereview-codex-5-5.md:5730:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:44:- market intent execution timing
docs/reviews/pr198-codereview-codex-5-5.md:5731:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:59:2. resolve all scheduled market actions whose `executeAtTick == T`
docs/reviews/pr198-codereview-codex-5-5.md:5732:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:82:- scheduled market action resolution
docs/reviews/pr198-codereview-codex-5-5.md:5733:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:88:### 2.6 Immediate market execution
docs/reviews/pr198-codereview-codex-5-5.md:5734:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:89:Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
docs/reviews/pr198-codereview-codex-5-5.md:5735:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:92:- requires the worker already be physically in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5737:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/reviews/pr198-codereview-codex-5-5.md:5739:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5740:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/reviews/pr198-codereview-codex-5-5.md:5742:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:214:- market sale proceeds
docs/reviews/pr198-codereview-codex-5-5.md:5743:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:215:- market purchase spending
docs/reviews/pr198-codereview-codex-5-5.md:5746:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:346:    uint64 nextCommitSequence;   // global monotonic FIFO sequence for scheduled market actions
docs/reviews/pr198-codereview-codex-5-5.md:5750:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:460:    MarketExecutionMode marketMode;
docs/reviews/pr198-codereview-codex-5-5.md:5751:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:463:    address marketToken;   // market token for buy/sell
docs/reviews/pr198-codereview-codex-5-5.md:5752:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:464:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/reviews/pr198-codereview-codex-5-5.md:5753:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:465:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/reviews/pr198-codereview-codex-5-5.md:5755:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:493:Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.
docs/reviews/pr198-codereview-codex-5-5.md:5759:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:503:    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:5760:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:504:    uint256 marketAmount;    // exact-in for sell, exact-out for buy
docs/reviews/pr198-codereview-codex-5-5.md:5764:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:571:- `marketAmount` = exact amount of resource to sell
docs/reviews/pr198-codereview-codex-5-5.md:5768:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:580:- `marketAmount` = exact amount of resource to buy
docs/reviews/pr198-codereview-codex-5-5.md:5773:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/reviews/pr198-codereview-codex-5-5.md:5774:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/reviews/pr198-codereview-codex-5-5.md:5778:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:696:    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:5779:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:697:    uint256 marketAmount;
docs/reviews/pr198-codereview-codex-5-5.md:5784:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:735:## 11. Randomness, market execution, and reentrancy
docs/reviews/pr198-codereview-codex-5-5.md:5785:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:755:### 11.2 Immediate vs scheduled market ordering
docs/reviews/pr198-codereview-codex-5-5.md:5786:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:756:Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
docs/reviews/pr198-codereview-codex-5-5.md:5787:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:758:Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
docs/reviews/pr198-codereview-codex-5-5.md:5788:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:764:A scheduled market action:
docs/reviews/pr198-codereview-codex-5-5.md:5789:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:765:- is created when a travel-based market mission is accepted
docs/reviews/pr198-codereview-codex-5-5.md:5794:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:934:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/reviews/pr198-codereview-codex-5-5.md:5795:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:938:- scheduled market actions are never lazily replayed; they resolve eagerly at heartbeat
docs/reviews/pr198-codereview-codex-5-5.md:5796:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:940:- immediate market actions require `Waiting` + Unicorn Town + cooldown clear
docs/reviews/pr198-codereview-codex-5-5.md:5797:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:958:### 16.2 Immediate market sell
docs/reviews/pr198-codereview-codex-5-5.md:5798:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:960:- worker is `Waiting` in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5801:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:968:- worker stays in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5802:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:972:### 16.3 Scheduled market buy
docs/reviews/pr198-codereview-codex-5-5.md:5803:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:974:- worker is one tick away from Unicorn Town in tick 307
docs/reviews/pr198-codereview-codex-5-5.md:5807:docs/planning/clanworld_v4_1_addendum.md:69:- immediate market action missions submitted while already in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5809:docs/planning/clanworld_v4_1_addendum.md:109:> **[PATCHED 2026-04-26]** The original text of this section incorrectly stated that all v1 market actions are Exact Input. `market_buy` is Exact Output, not Exact Input. The corrected text is below. Authoritative source: `clanworld_v4_2_state_schema_interface_spec.md §8.4` and `IClanWorld.sol`.
docs/reviews/pr198-codereview-codex-5-5.md:5810:docs/planning/clanworld_v4_1_addendum.md:111:### `market_sell`
docs/reviews/pr198-codereview-codex-5-5.md:5811:docs/planning/clanworld_v4_1_addendum.md:112:`market_sell` is **Exact Input**.
docs/reviews/pr198-codereview-codex-5-5.md:5812:docs/planning/clanworld_v4_1_addendum.md:113:- `marketAmount` = exact amount of resource token to sell
docs/reviews/pr198-codereview-codex-5-5.md:5814:docs/planning/clanworld_v4_1_addendum.md:117:### `market_buy`
docs/reviews/pr198-codereview-codex-5-5.md:5815:docs/planning/clanworld_v4_1_addendum.md:118:`market_buy` is **Exact Output**.
docs/reviews/pr198-codereview-codex-5-5.md:5816:docs/planning/clanworld_v4_1_addendum.md:119:- `marketAmount` = exact amount of resource to receive
docs/reviews/pr198-codereview-codex-5-5.md:5820:docs/planning/clanworld_v4_1_addendum.md:126:## A8. Immediate vs scheduled market execution ordering
docs/reviews/pr198-codereview-codex-5-5.md:5821:docs/planning/clanworld_v4_1_addendum.md:130:### Immediate market actions
docs/reviews/pr198-codereview-codex-5-5.md:5822:docs/planning/clanworld_v4_1_addendum.md:132:- physically in Unicorn Town,
docs/reviews/pr198-codereview-codex-5-5.md:5823:docs/planning/clanworld_v4_1_addendum.md:136:then an Elder may submit a market mission that executes **immediately in that tx**.
docs/reviews/pr198-codereview-codex-5-5.md:5825:docs/planning/clanworld_v4_1_addendum.md:140:### Scheduled market actions
docs/reviews/pr198-codereview-codex-5-5.md:5826:docs/planning/clanworld_v4_1_addendum.md:141:If a market mission requires travel or action-tick maturation, it executes later at the heartbeat that closes its action tick.
docs/reviews/pr198-codereview-codex-5-5.md:5827:docs/planning/clanworld_v4_1_addendum.md:143:Scheduled market actions execute:
docs/reviews/pr198-codereview-codex-5-5.md:5829:docs/planning/clanworld_v4_1_addendum.md:148:An immediate market action during tick `T` can front-run scheduled market actions that will execute at the close of tick `T`.
docs/reviews/pr198-codereview-codex-5-5.md:5830:docs/planning/clanworld_v4_1_addendum.md:150:This is intentional and part of the adversarial market design.
docs/reviews/pr198-codereview-codex-5-5.md:5832:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:44:- market intent execution timing
docs/reviews/pr198-codereview-codex-5-5.md:5833:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:59:2. resolve all scheduled market actions whose `executeAtTick == T`
docs/reviews/pr198-codereview-codex-5-5.md:5834:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:82:- scheduled market action resolution
docs/reviews/pr198-codereview-codex-5-5.md:5835:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:88:### 2.6 Immediate market execution
docs/reviews/pr198-codereview-codex-5-5.md:5836:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:89:Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
docs/reviews/pr198-codereview-codex-5-5.md:5837:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:92:- requires the worker already be physically in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5839:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/reviews/pr198-codereview-codex-5-5.md:5841:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5842:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/reviews/pr198-codereview-codex-5-5.md:5844:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:214:- market sale proceeds
docs/reviews/pr198-codereview-codex-5-5.md:5845:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:215:- market purchase spending
docs/reviews/pr198-codereview-codex-5-5.md:5848:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:346:    uint64 nextCommitSequence;   // global monotonic FIFO sequence for scheduled market actions
docs/reviews/pr198-codereview-codex-5-5.md:5852:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:460:    MarketExecutionMode marketMode;
docs/reviews/pr198-codereview-codex-5-5.md:5853:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:463:    address marketToken;   // market token for buy/sell
docs/reviews/pr198-codereview-codex-5-5.md:5854:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:464:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/reviews/pr198-codereview-codex-5-5.md:5855:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:465:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/reviews/pr198-codereview-codex-5-5.md:5857:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:493:Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.
docs/reviews/pr198-codereview-codex-5-5.md:5861:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:503:    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:5862:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:504:    uint256 marketAmount;    // exact-in for sell, exact-out for buy
docs/reviews/pr198-codereview-codex-5-5.md:5866:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:571:- `marketAmount` = exact amount of resource to sell
docs/reviews/pr198-codereview-codex-5-5.md:5870:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:580:- `marketAmount` = exact amount of resource to buy
docs/reviews/pr198-codereview-codex-5-5.md:5875:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/reviews/pr198-codereview-codex-5-5.md:5876:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/reviews/pr198-codereview-codex-5-5.md:5880:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:696:    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:5881:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:697:    uint256 marketAmount;
docs/reviews/pr198-codereview-codex-5-5.md:5886:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:735:## 11. Randomness, market execution, and reentrancy
docs/reviews/pr198-codereview-codex-5-5.md:5887:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:755:### 11.2 Immediate vs scheduled market ordering
docs/reviews/pr198-codereview-codex-5-5.md:5888:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:756:Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
docs/reviews/pr198-codereview-codex-5-5.md:5889:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:758:Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
docs/reviews/pr198-codereview-codex-5-5.md:5890:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:764:A scheduled market action:
docs/reviews/pr198-codereview-codex-5-5.md:5891:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:765:- is created when a travel-based market mission is accepted
docs/reviews/pr198-codereview-codex-5-5.md:5896:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:934:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/reviews/pr198-codereview-codex-5-5.md:5897:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:938:- scheduled market actions are never lazily replayed; they resolve eagerly at heartbeat
docs/reviews/pr198-codereview-codex-5-5.md:5898:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:940:- immediate market actions require `Waiting` + Unicorn Town + cooldown clear
docs/reviews/pr198-codereview-codex-5-5.md:5899:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:958:### 16.2 Immediate market sell
docs/reviews/pr198-codereview-codex-5-5.md:5900:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:960:- worker is `Waiting` in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5903:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:968:- worker stays in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5904:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:972:### 16.3 Scheduled market buy
docs/reviews/pr198-codereview-codex-5-5.md:5905:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:974:- worker is one tick away from Unicorn Town in tick 307
docs/reviews/pr198-codereview-codex-5-5.md:5909:docs/planning/clanworld_v4_3_schema_patch.md:23:For immediate market actions:
docs/reviews/pr198-codereview-codex-5-5.md:5912:docs/planning/clanworld_v4_3_schema_patch.md:97:Scheduled market actions are stored as:
docs/reviews/pr198-codereview-codex-5-5.md:5916:docs/planning/clanworld_v4_3_schema_patch.md:108:Within a given `executeAtTick`, scheduled market actions execute in:
docs/reviews/pr198-codereview-codex-5-5.md:5922:docs/planning/clanworld_v4_3_schema_patch.md:275:`ERR_CARRY_FULL` is especially relevant for explicit market buy validation and any direct action that is rejected because requested output cannot fit remaining carry capacity.
docs/reviews/pr198-codereview-codex-5-5.md:5923:docs/planning/clanworld_v4_3_schema_patch.md:318:- scheduled market execution
docs/reviews/pr198-codereview-codex-5-5.md:5924:docs/planning/clanworld_v4_3_schema_patch.md:344:- scheduled market actions stored by `executeAtTick`
docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-5.md:5926:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:39:3 = Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5927:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:100:Homebases may exist across the world but may **not** be placed in Unicorn Town.
docs/reviews/pr198-codereview-codex-5-5.md:5928:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:110:Deep Sea and Unicorn Town are not valid homebase spawn regions.
docs/reviews/pr198-codereview-codex-5-5.md:5929:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:127:2. resolve all **scheduled market actions** that mature in the closing tick
docs/reviews/pr198-codereview-codex-5-5.md:5930:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:162:- all scheduled market actions at heartbeat boundaries
docs/reviews/pr198-codereview-codex-5-5.md:5931:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:212:- goto Unicorn Town, sell wood
docs/reviews/pr198-codereview-codex-5-5.md:5932:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:215:- goto noop, sell wood immediately in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5933:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:264:- `market_sell` requested with invalid params
docs/reviews/pr198-codereview-codex-5-5.md:5934:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:317:- scheduled market buy
docs/reviews/pr198-codereview-codex-5-5.md:5935:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:318:- scheduled market sell
docs/reviews/pr198-codereview-codex-5-5.md:5936:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:323:V1 only includes **immediate market actions in Unicorn Town**.
docs/reviews/pr198-codereview-codex-5-5.md:5937:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:340:This is especially important for immediate market actions in Unicorn Town.
docs/reviews/pr198-codereview-codex-5-5.md:5940:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:502:# 5. Unicorn Town Market Model
docs/reviews/pr198-codereview-codex-5-5.md:5942:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:505:Unicorn Town is the only region where Uniswap market actions may be executed in v1.
docs/reviews/pr198-codereview-codex-5-5.md:5947:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:518:1. scheduled market action
docs/reviews/pr198-codereview-codex-5-5.md:5948:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:519:2. immediate market action
docs/reviews/pr198-codereview-codex-5-5.md:5949:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:521:## 5.4 Scheduled market action
docs/reviews/pr198-codereview-codex-5-5.md:5950:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:522:Scheduled market action is used when the clansman is not already eligible to trade immediately.
docs/reviews/pr198-codereview-codex-5-5.md:5951:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:525:- worker must travel to Unicorn Town first
docs/reviews/pr198-codereview-codex-5-5.md:5952:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:526:- after arrival, the market action occupies one full action tick
docs/reviews/pr198-codereview-codex-5-5.md:5953:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:530:- `goto Unicorn Town, action = sell wood`
docs/reviews/pr198-codereview-codex-5-5.md:5954:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:534:- one action tick in Unicorn Town is consumed
docs/reviews/pr198-codereview-codex-5-5.md:5955:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:536:- worker becomes `WAITING` in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5956:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:538:## 5.5 Immediate market action
docs/reviews/pr198-codereview-codex-5-5.md:5957:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:539:Immediate market action is allowed if and only if all of the following are true at tx execution time:
docs/reviews/pr198-codereview-codex-5-5.md:5958:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:540:- clansman is physically in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5960:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:548:- `action = market_buy` or `market_sell`
docs/reviews/pr198-codereview-codex-5-5.md:5961:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:552:## 5.6 Immediate market action effects
docs/reviews/pr198-codereview-codex-5-5.md:5962:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:553:On successful immediate market action:
docs/reviews/pr198-codereview-codex-5-5.md:5963:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:555:- the worker remains in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5964:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:560:Immediate market actions are therefore still **missions**, not an out-of-band bypass.
docs/reviews/pr198-codereview-codex-5-5.md:5965:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:562:## 5.7 Immediate vs scheduled collision ordering
docs/reviews/pr198-codereview-codex-5-5.md:5966:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:563:If immediate and scheduled market actions affect the same pool in the same tick:
docs/reviews/pr198-codereview-codex-5-5.md:5967:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:564:- immediate market actions execute against the current pool state at the time their Elder tx lands
docs/reviews/pr198-codereview-codex-5-5.md:5968:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:565:- scheduled market actions execute later, at the heartbeat that closes the tick, in deterministic FIFO order by mission commit order
docs/reviews/pr198-codereview-codex-5-5.md:5969:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:567:This intentionally allows a clansman already stationed in Unicorn Town to front-run scheduled trades that will execute at tick close.
docs/reviews/pr198-codereview-codex-5-5.md:5970:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:569:This is an intended feature of the game’s adversarial market design, not a bug.
docs/reviews/pr198-codereview-codex-5-5.md:5971:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:571:## 5.8 Scheduled market action ordering
docs/reviews/pr198-codereview-codex-5-5.md:5972:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:572:If multiple scheduled market actions mature in the same tick, Keeper executes them in deterministic FIFO order based on mission commit order.
docs/reviews/pr198-codereview-codex-5-5.md:5973:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:574:Each swap sees updated market state from earlier swaps in that sequence.
docs/reviews/pr198-codereview-codex-5-5.md:5974:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:577:For v1, market actions execute with **no slippage guard**.
docs/reviews/pr198-codereview-codex-5-5.md:5975:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:584:- scheduled market actions resolved at heartbeat close
docs/reviews/pr198-codereview-codex-5-5.md:5976:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:585:- immediate market actions executed in Elder txs while already in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5977:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:588:- adversarial market play
docs/reviews/pr198-codereview-codex-5-5.md:5979:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:593:## 5.10 Failed market actions
docs/reviews/pr198-codereview-codex-5-5.md:5980:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:594:If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
docs/reviews/pr198-codereview-codex-5-5.md:5981:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:597:- clansman becomes or remains `WAITING` in Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:5989:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:615:- market sales into seeded resource/gold pools
docs/reviews/pr198-codereview-codex-5-5.md:5992:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:638:Bandits may not spawn in Unicorn Town or Deep Sea.
docs/reviews/pr198-codereview-codex-5-5.md:5995:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:713:Bandits never enter Unicorn Town or Deep Sea.
docs/reviews/pr198-codereview-codex-5-5.md:5998:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:797:Bandits do not use Unicorn Town in v1.
docs/reviews/pr198-codereview-codex-5-5.md:5999:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1057:## 11.2 Intentional gameplay consequence
docs/reviews/pr198-codereview-codex-5-5.md:6002:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1108:This is intended to enable at least one meaningful early Unicorn Town trade.
docs/reviews/pr198-codereview-codex-5-5.md:6004:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1165:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/reviews/pr198-codereview-codex-5-5.md:6005:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1168:- immediate market actions require the worker to be in Unicorn Town and `WAITING`
docs/reviews/pr198-codereview-codex-5-5.md:6006:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1169:- scheduled market actions are never lazily resolved; they resolve eagerly at the heartbeat that closes their action tick
docs/reviews/pr198-codereview-codex-5-5.md:6007:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1190:- scheduled vs immediate market execution split
docs/reviews/pr198-codereview-codex-5-5.md:6016:packages/contracts/test/MissionTiming.t.sol:133:        assertEq(world.getActionDuration(ActionType.MarketBuy), 1, "market buy");
docs/reviews/pr198-codereview-codex-5-5.md:6017:packages/contracts/test/MissionTiming.t.sol:134:        assertEq(world.getActionDuration(ActionType.MarketSell), 1, "market sell");
docs/reviews/pr198-codereview-codex-5-5.md:6025:packages/contracts/src/ClanWorld.sol:2720:        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/pr198-codereview-codex-5-5.md:6052:packages/contracts/src/ClanWorld.sol:3091:        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:6055:packages/contracts/src/ClanWorld.sol:3157:    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
docs/reviews/pr198-codereview-codex-5-5.md:6107:packages/contracts/src/IClanWorld.sol:687:    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
docs/reviews/pr198-codereview-codex-5-5.md:6113:9f93593 (origin/dev-phase-6-market) fix(contracts): Phase 6 cloud-review fix-round - duplicate enums, unused param, MAX_MARKET_ACTIONS, carry overflow (Closes #269) (#270)
docs/reviews/pr198-codereview-codex-5-5.md:6197:  1217	            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
docs/reviews/pr198-codereview-codex-5-5.md:6199:  1219	            if (marketStatus == StatusCode.OK) {
docs/reviews/pr198-codereview-codex-5-5.md:6208:  1228	            result.marketMode = MarketExecutionMode.Immediate;
docs/reviews/pr198-codereview-codex-5-5.md:6264:  1284	        result.marketMode = isMarketAction ? MarketExecutionMode.Scheduled : MarketExecutionMode.None;
docs/reviews/pr198-codereview-codex-5-5.md:6286:  1306	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
docs/reviews/pr198-codereview-codex-5-5.md:6290:  1310	        m.marketToken = order.marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:6291:  1311	        m.marketAmount = order.marketAmount;
docs/reviews/pr198-codereview-codex-5-5.md:6294:  1314	        if (m.marketMode == MarketExecutionMode.Scheduled) {
docs/reviews/pr198-codereview-codex-5-5.md:6295:  1315	            _marketMissionCommitSequence[cs.clansmanId] = _world.nextCommitSequence++;
docs/reviews/pr198-codereview-codex-5-5.md:6297:  1317	            delete _marketMissionCommitSequence[cs.clansmanId];
docs/reviews/pr198-codereview-codex-5-5.md:6305:  1325	            commitSequence: _marketMissionCommitSequence[clansmanId],
docs/reviews/pr198-codereview-codex-5-5.md:6310:  1330	            marketToken: m.marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:6311:  1331	            marketAmount: m.marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:6316:  1336	            executeAtTick, sma.commitSequence, clanId, clansmanId, m.action, m.marketToken, m.marketAmount, m.maxGoldIn
docs/reviews/pr198-codereview-codex-5-5.md:6358:  1378	    /// @dev Execute the full scheduled market queue for the given tick, then delete it.
docs/reviews/pr198-codereview-codex-5-5.md:6401:  1421	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount
docs/reviews/pr198-codereview-codex-5-5.md:6403:  1423	                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-5.md:6405:  1425	                    marketStatus;
docs/reviews/pr198-codereview-codex-5-5.md:6418:  1438	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn
docs/reviews/pr198-codereview-codex-5-5.md:6420:  1440	                    StatusCode marketStatus
docs/reviews/pr198-codereview-codex-5-5.md:6422:  1442	                    marketStatus;
docs/reviews/pr198-codereview-codex-5-5.md:6485:  1505	    /// @dev Map a market token address to the canonical uint8 resource id used by market events.
docs/reviews/pr198-codereview-codex-5-5.md:6486:  1506	    function _marketResourceForToken(address token) internal view returns (uint8) {
docs/reviews/pr198-codereview-codex-5-5.md:6492:  1512	        revert("ClanWorld: invalid market resource");
docs/reviews/pr198-codereview-codex-5-5.md:6605:  1625	            return _executeImmediateMarketSell(clanId, clansmanId, order.marketToken, order.marketAmount);
docs/reviews/pr198-codereview-codex-5-5.md:6607:  1627	        return _executeImmediateMarketBuy(clanId, clansmanId, order.marketToken, order.marketAmount, order.maxGoldIn);
docs/reviews/pr198-codereview-codex-5-5.md:6655:  1675	                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:6760:  1780	                _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:6777:  1797	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
docs/reviews/pr198-codereview-codex-5-5.md:6823:  1843	            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:6832:  1852	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
docs/reviews/pr198-codereview-codex-5-5.md:6947:  1955	            _marketResourceForToken(token),
docs/reviews/pr198-codereview-codex-5-5.md:7007:  2015	        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/pr198-codereview-codex-5-5.md:7013:  2021	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
docs/reviews/pr198-codereview-codex-5-5.md:7015:  2023	            address tok = order.marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:7026:  2034	            if (action == ActionType.MarketBuy && order.marketAmount > _remainingCarryForToken(cs, tok)) {
docs/reviews/pr198-codereview-codex-5-5.md:7029:  2037	            // Immediate market orders execute during submitClanOrders when the
docs/reviews/pr198-codereview-codex-5-5.md:7030:  2038	            // worker is waiting in Unicorn Town and off cooldown. Other valid
docs/reviews/pr198-codereview-codex-5-5.md:7031:  2039	            // market orders enqueue when the scheduled mission resolves.
docs/reviews/pr198-codereview-codex-5-5.md:7033:  2041	                // Already at Unicorn Town — immediate if off cooldown, scheduled otherwise.
docs/reviews/pr198-codereview-codex-5-5.md:7074:  2082	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
docs/reviews/pr198-codereview-codex-5-5.md:7820:   377	    address marketToken;
docs/reviews/pr198-codereview-codex-5-5.md:7821:   378	    uint256 marketAmount;
docs/reviews/pr198-codereview-codex-5-5.md:7830:   387	    MarketExecutionMode marketMode;
docs/reviews/pr198-codereview-codex-5-5.md:7855:   556	    // ----- market -----
docs/reviews/pr198-codereview-codex-5-5.md:7885:   586	        address marketToken,
docs/reviews/pr198-codereview-codex-5-5.md:7886:   587	        uint256 marketAmount,
docs/reviews/pr198-codereview-codex-5-5.md:8012:    98	    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-5.md:8023:   109	    ///         Legacy alias for Phase 6.1 scheduled-market code.
docs/reviews/pr198-codereview-codex-5-5.md:8290:   799	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "sell should be immediate");
docs/reviews/pr198-codereview-codex-5-5.md:8309:   818	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market order should schedule while cooling down");
docs/reviews/pr198-codereview-codex-5-5.md:8310:   819	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "cooldown should force scheduled mode");
docs/reviews/pr198-codereview-codex-5-5.md:8312:   821	        assertEq(uint8(m.marketMode), uint8(MarketExecutionMode.Scheduled), "mission should be scheduled");
docs/reviews/pr198-codereview-codex-5-5.md:8328:   837	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
docs/reviews/pr198-codereview-codex-5-5.md:8329:   838	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "out-of-town should be scheduled");
docs/reviews/pr198-codereview-codex-5-5.md:8345:   854	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
docs/reviews/pr198-codereview-codex-5-5.md:8346:   855	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Scheduled), "busy worker should be scheduled");
docs/reviews/pr198-codereview-codex-5-5.md:8377:   886	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "buy should be immediate");
docs/reviews/pr198-codereview-codex-5-5.md:8406:   915	            uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed immediate action stays immediate"
docs/reviews/pr198-codereview-codex-5-5.md:8436:   945	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
docs/reviews/pr198-codereview-codex-5-5.md:8465:   974	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.Immediate), "failed buy should report immediate");
docs/reviews/pr198-codereview-codex-5-5.md:8487:   996	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
docs/reviews/pr198-codereview-codex-5-5.md:8507:  1016	        // Submit sell order — clansman travels to Unicorn Town then executes when the market mission settles
docs/reviews/pr198-codereview-codex-5-5.md:8674:  1183	        assertEq(uint8(r[0].marketMode), uint8(MarketExecutionMode.None), "rejected buy has no mode");
docs/reviews/pr198-codereview-codex-5-5.md:8853:  1362	                marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:8854:  1363	                marketAmount: uint256(i + 1) * 1e18,
docs/reviews/pr198-codereview-codex-5-5.md:8917:  1426	            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:8918:  1427	            marketAmount: 5e18,
docs/reviews/pr198-codereview-codex-5-5.md:8931:  1440	            marketToken: woodAddr,
docs/reviews/pr198-codereview-codex-5-5.md:8932:  1441	            marketAmount: 1e18,
docs/reviews/pr198-codereview-codex-5-5.md:9235:   874	    ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/pr198-codereview-codex-5-5.md:9256:   895	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/pr198-codereview-codex-5-5.md:9274:   913	    ///      Called from heartbeat before market execution and tick increment.
docs/reviews/pr198-codereview-codex-5-5.md:9315:model: gpt-5.5
docs/reviews/pr198-codereview-codex-5-5.md:9327:You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.
docs/reviews/pr198-codereview-codex-5-5.md:9329:Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.
docs/reviews/pr198-codereview-codex-5-5.md:9334:1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
docs/reviews/pr198-codereview-codex-5-5.md:9335:2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
docs/reviews/pr198-codereview-codex-5-5.md:9554:You are a senior staff engineer doing the FINAL pre-merge code review for PR #198 (Phase 6 — Unicorn Town Market) at head 9f93593.
docs/reviews/pr198-codereview-codex-5-5.md:9556:Phase 6 ships: ERC20 boundary tokens (wood/wheat/fish/iron) + 4 constant-product pools + immediate market path + scheduled market FIFO queue + market failure semantics + events surface. Recent fix-round (PR #270) addressed cloud reviewer findings on duplicate enums, unused params, max-actions cap, carry overflow handling, and CLANSMAN_CARRY_CAP rename.
docs/reviews/pr198-codereview-codex-5-5.md:9561:1. CROSS-CUTTING bugs at sub-issue boundaries (immediate vs scheduled market path collisions, FIFO ordering, queue-vs-execute timing)
docs/reviews/pr198-codereview-codex-5-5.md:9562:2. ARCHITECTURAL drift — does the phase deliver Unicorn Town market per spec §5.5-5.7 + §11.2?
docs/reviews/pr137-review-claude-opus.md:14:Implements Phase 2 economy: constant-product AMM pool (`StubPool`), market sell/buy execution, FIFO scheduled queue with commit-sequence ordering, treasury initialization, and pool seeding. Touches 6 files in `packages/contracts/`, +804/−31 lines. Self-assessed as **medium risk** — substantial Solidity additions (AMM math, scheduled queue, `via_ir = true`).
docs/reviews/pr137-review-claude-opus.md:16:Previously reviewed in R1 and R2 rounds (fixes preserved in rebase): heartbeat try/catch, stale queued action invalidation, ceiling division overflow safety, market events with realized amounts, test naming.
docs/reviews/pr137-review-claude-opus.md:24:| `packages/contracts/src/ClanWorld.sol` | +325/−15 | initTreasury, seedPools, scheduled market actions, FIFO queue, heartbeat execution |
docs/reviews/pr137-review-claude-opus.md:54:| 3 | `src/ClanWorld.sol` | 991–1075 | **Unbounded per-tick market queue.** No cap on `_scheduledMarketActions[tick].length`. `mintClan` is permissionless (no fee), so an attacker can create many clans and schedule many actions for the same tick. `heartbeat` iterates the full array — risk of OOG revert stalling progression. Confirmed by Agents 2, 6. | **SHOULD FIX** |
docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr137-review-claude-opus.md:58:| 7 | `script/Deploy.s.sol` | 30–46 | **Deploy script never calls `initTreasury` / `seedPools`.** Deployments using only this script leave the market offline (`poolsSeeded = false`). Operator must run separate transactions. Agent 4. | **SHOULD FIX** |
docs/reviews/pr137-review-claude-opus.md:61:| 10 | `src/ClanWorld.sol` | 1296–1320 | **Market token validation skipped before `initTreasury`.** If treasury is uninitialized, `marketToken` checks are bypassed. Orders enqueue with bogus tokens; execution fails later but queue state is wasted. Agent 2. | **SHOULD FIX** |
docs/reviews/pr137-review-claude-opus.md:124:**Finding #1: Stale queue guard.** Add a `missionNonce` field to `ScheduledMarketAction` and check it against `_missions[clansmanId].nonce` during execution. Alternatively, remove superseded queue entries when installing a replacement market mission (more gas but simpler invariant). This is the only merge-blocking issue.
docs/reviews/pr137-review-claude-opus.md:137:8. **#10 Market validation pre-init** — Reject market orders when `_treasury.woodToken == address(0)`
docs/reviews/pr137-review-claude-opus.md:153:The core AMM math is **correct and economically sound** — constant-product formulas, rounding, K-invariant all verified. The try/catch external self-call pattern for atomic market execution is **well-designed**. Test coverage is **adequate for hackathon standards**. Gold and resource conservation **hold**.
docs/reviews/pr137-review-claude-opus.md:155:The single blocking issue is the **incomplete stale queue guard** (Finding #1): same-type mission replacement can cause stale market actions to execute with wrong parameters. This is a correctness bug that affects fairness and economic integrity.
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:57:| Unicorn Town | `#D8B4D8` | Pastel lavender — only bright spot |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:196:| Unicorn Town trade | World: trade swirl effect over Unicorn Town sprite. Market panel: price moves, sparkline updates. Ticker: trade entry |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:251:            Unicorn Town (3)
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:266:| 3 | Unicorn Town | 500 | 500 |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:436:### 5.5 Animation key derivation
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:465:### 5.6 Wheelbarrow fill ratio
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:478:### 5.7 Determinism guarantees
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:594:api.market.getState(): MarketState;
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:597:api.market.getPriceHistory(args: { resource: 'wood' | 'iron' | 'wheat' | 'fish'; ticks: number }):
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:620:| Market tab | `getState`, `getPriceHistory` x4 | any market trade, tick advance |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:634:  | { kind: 'market_trade'; tick: number; clanId: number; resource: string; amountIn: number; amountOut: number; direction: 'buy' | 'sell'; createdAt: number }
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:804:- Sprite key derivation per §5.5 working
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:884:6. Trade events update market spot prices and event ticker
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:266:  market: {
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:285:- `market` — recent prices give the LLM enough context to recognize trends without dumping a full price history.
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:329:              marketResource: { type: 'string', enum: ['Wood','Iron','Wheat','Fish'], description: 'Required for MarketBuy/MarketSell.' },
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:330:              marketAmount: { type: 'string', description: 'For MarketSell: exact amount of resource to sell. For MarketBuy: exact amount of resource to buy. As 18-decimal string.' },
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:444:| Mira | "Trade aggressively. Specialize in what your region produces and dump it on the market. Use gold as a weapon. Whisper offers constantly." |
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:532:    const [snapshot, allClans, bandit, market, events, whispers, myLogs] = await Promise.all([
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:536:      this.convex.query('market.getState', {}),
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:550:      this.convex.query('market.getPriceHistory', { resource: 'wood', ticks: 8 }),
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:551:      this.convex.query('market.getPriceHistory', { resource: 'iron', ticks: 8 }),
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:552:      this.convex.query('market.getPriceHistory', { resource: 'wheat', ticks: 8 }),
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:553:      this.convex.query('market.getPriceHistory', { resource: 'fish', ticks: 8 }),
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:563:      market: {
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:564:        wood: { spot: market.wood.spotPriceGoldPerResource, recent: woodHist.map(h => h.spotPrice) },
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:565:        iron: { spot: market.iron.spotPriceGoldPerResource, recent: ironHist.map(h => h.spotPrice) },
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:566:        wheat: { spot: market.wheat.spotPriceGoldPerResource, recent: wheatHist.map(h => h.spotPrice) },
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:567:        fish: { spot: market.fish.spotPriceGoldPerResource, recent: fishHist.map(h => h.spotPrice) },
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:652:    if ((m.action === 'MarketBuy' || m.action === 'MarketSell') && !m.marketResource) {
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:653:      errors.push(`Market actions require marketResource`);
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:671:The LLM tool schema accepts `marketResource: 'Wood' | 'Iron' | 'Wheat' | 'Fish'` (a friendly enum), but the contract requires `marketToken: address` (an actual ERC20 contract address).
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:686:This table is shared across all agents (read-only, set once). When `executeDispatch` builds a `ClanOrder`, it uses `resourceTokenMap[input.marketResource]` to populate `marketToken`. Treasury values don't change after deployment, so caching at boot is safe.
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:688:### 11.2 `executeDispatch`
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:51:│   - marketState                  │
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:61:│   - api.market.getState          │
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:62:│   - api.market.getPriceHistory   │
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:148:- **Snapshot tables** — hold the *latest* state. One row per logical entity, overwritten on every refresh. Used for things the UI shows live: world state, clan views, bandit, market.
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:193:  marketState: defineTable({
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:300:3. Writes one `priceHistory` row per resource using `marketState.{wood,iron,wheat,fish}.spotPriceGoldPerResource`.
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:350:| `ImmediateMarketActionExecuted` | `market_trade` | direction inferred from token directions |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:351:| `ScheduledMarketActionExecuted` | `market_trade` | |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:352:| `ScheduledMarketActionCommitted` | (omit) | not surfaced; market panel polls scheduled queues |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:353:| `MarketActionFailed` | `market_trade` | with `failed: true` flag in payload |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:378:### 5.5 Failure handling
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:513:// convex/market.ts
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:521:    const row = await ctx.db.query('marketState')
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:717:- One `marketState` row with 4 pool reserves at the seeded ratios from v4 §5.11
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:719:- 20 `eventLog` rows mixing mission_assigned, resources_deposited, market_trade, bandit_spawned
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:753:6. **Perturb market prices:**
docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/planning/V1/05 0G/clanworld_clan_identity_spec.md:207:  name: string;              // skill identifier, e.g., "market-analysis"
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:23:For immediate market actions:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:97:Scheduled market actions are stored as:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:108:Within a given `executeAtTick`, scheduled market actions execute in:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:275:`ERR_CARRY_FULL` is especially relevant for explicit market buy validation and any direct action that is rejected because requested output cannot fit remaining carry capacity.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:318:- scheduled market execution
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:344:- scheduled market actions stored by `executeAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:122:**Purpose:** Single-call complete Unicorn Town market state including spot prices and the next two ticks of scheduled queue.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:153:- The indexer stores each tick's spot price into Convex, building a per-resource price-history series for sparkline charts in the market panel.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:154:- Drives: market panel, scheduled-trade ticker, per-resource price charts.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:251:| `getMarketState()`              | ~5 calls           | Medium — market panel + price charts   |
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:69:- immediate market action missions submitted while already in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:109:All v1 market actions are **Exact Input** actions only.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:111:### `market_sell`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:116:### `market_buy`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:121:V1 does **not** support Exact Output market actions.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:127:## A8. Immediate vs scheduled market execution ordering
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:131:### Immediate market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:133:- physically in Unicorn Town,
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:137:then an Elder may submit a market mission that executes **immediately in that tx**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:141:### Scheduled market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:142:If a market mission requires travel or action-tick maturation, it executes later at the heartbeat that closes its action tick.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:144:Scheduled market actions execute:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:149:An immediate market action during tick `T` can front-run scheduled market actions that will execute at the close of tick `T`.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:151:This is intentional and part of the adversarial market design.
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
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:502:# 5. Unicorn Town Market Model
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:505:Unicorn Town is the only region where Uniswap market actions may be executed in v1.
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
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:593:## 5.10 Failed market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:594:If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:597:- clansman becomes or remains `WAITING` in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:615:- market sales into seeded resource/gold pools
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:638:Bandits may not spawn in Unicorn Town or Deep Sea.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:713:Bandits never enter Unicorn Town or Deep Sea.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:797:Bandits do not use Unicorn Town in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1057:## 11.2 Intentional gameplay consequence
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1108:This is intended to enable at least one meaningful early Unicorn Town trade.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1165:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1168:- immediate market actions require the worker to be in Unicorn Town and `WAITING`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1169:- scheduled market actions are never lazily resolved; they resolve eagerly at the heartbeat that closes their action tick
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1190:- scheduled vs immediate market execution split
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:44:- market intent execution timing
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:59:2. resolve all scheduled market actions whose `executeAtTick == T`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:82:- scheduled market action resolution
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:88:### 2.6 Immediate market execution
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:89:Workers already in Unicorn Town and eligible to act may execute an immediate market action directly in the Elder tx.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:92:- requires the worker already be physically in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:214:- market sale proceeds
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:215:- market purchase spending
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:346:    uint64 nextCommitSequence;   // global monotonic FIFO sequence for scheduled market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:460:    MarketExecutionMode marketMode;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:463:    address marketToken;   // market token for buy/sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:464:    uint256 marketAmount;  // exact-in for sell, exact-out for buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:465:    uint256 maxGoldIn;     // market_buy only, 0 otherwise
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:493:Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:503:    address marketToken;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:504:    uint256 marketAmount;    // exact-in for sell, exact-out for buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:571:- `marketAmount` = exact amount of resource to sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:580:- `marketAmount` = exact amount of resource to buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:696:    address marketToken;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:697:    uint256 marketAmount;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:735:## 11. Randomness, market execution, and reentrancy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:755:### 11.2 Immediate vs scheduled market ordering
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:756:Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:758:Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:764:A scheduled market action:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:765:- is created when a travel-based market mission is accepted
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:934:- bandits cannot spawn in Unicorn Town or Deep Sea
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:938:- scheduled market actions are never lazily replayed; they resolve eagerly at heartbeat
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:940:- immediate market actions require `Waiting` + Unicorn Town + cooldown clear
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:958:### 16.2 Immediate market sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:960:- worker is `Waiting` in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:968:- worker stays in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:972:### 16.3 Scheduled market buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:974:- worker is one tick away from Unicorn Town in tick 307
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:438:### 11.2 New vocabulary (adopt across both submissions)
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:471:elder world market                # getMarketState
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:175:- no market or bandits yet
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:193:2. resolve scheduled market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:243:### 5.5 Implement deposit action
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:260:## Phase 6 — Unicorn Town market integration
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:275:### 6.3 Implement immediate market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:282:### 6.4 Implement scheduled market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:287:### 6.5 Implement market failure semantics
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:296:### 6.6 Add events and market result surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:298:- immediate market action executed
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:299:- scheduled market action executed
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:300:- market action failed
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:307:**Cut line:** immediate market actions can be deferred if necessary, but scheduled market execution must exist.
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:454:### 11.2 Implement rank computation
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:487:- maybe market preview
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:504:3. trade in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:525:- Unicorn Town trades
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:532:- immediate market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:559:7. market sell/buy  
docs/reviews/pr194-r2-codereview-codex-5-4.md:695:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr194-r2-codereview-codex-5-4.md:2474:     ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2499:         // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2940:     uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/pr194-r2-codereview-codex-5-4.md:3364:+                marketToken: address(0),
docs/reviews/pr194-r2-codereview-codex-5-4.md:3365:+                marketAmount: 0,
docs/reviews/pr194-r2-codereview-codex-5-4.md:4051:+            marketToken: address(0),
docs/reviews/pr194-r2-codereview-codex-5-4.md:4052:+            marketAmount: 0,
docs/reviews/pr194-r2-codereview-codex-5-4.md:4060:+            marketToken: address(0),
docs/reviews/pr194-r2-codereview-codex-5-4.md:4061:+            marketAmount: 0,
docs/reviews/pr194-r2-codereview-codex-5-4.md:4671:+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4684:+        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4685:+        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4688:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4918:-                      { name: 'marketMode', type: 'uint8' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4920:-                      { name: 'marketToken', type: 'address' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4921:-                      { name: 'marketAmount', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4943:-                  { name: 'marketMode', type: 'uint8' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4945:-                  { name: 'marketToken', type: 'address' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4946:-                  { name: 'marketAmount', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4992:-          { name: 'marketToken', type: 'address' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4993:-          { name: 'marketAmount', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5195:+        version: 3.2.4(@types/node@25.6.0)(lightningcss@1.32.0)
docs/reviews/pr194-r2-codereview-codex-5-4.md:5319:    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
docs/reviews/pr194-r2-codereview-codex-5-4.md:5389:        address marketToken,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5390:        uint256 marketAmount,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5763:    address marketToken; // market token for buy/sell
docs/reviews/pr194-r2-codereview-codex-5-4.md:5764:    uint256 marketAmount; // exact-in for sell, exact-out for buy
docs/reviews/pr194-r2-codereview-codex-5-4.md:5765:    uint256 maxGoldIn; // market_buy only, 0 otherwise
docs/reviews/pr194-r2-codereview-codex-5-4.md:5791:    address marketToken;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6612:   105	        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6613:   106	        marketAmount: 0n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:7530:   650	            // Scheduled market actions: already enqueued at submitClanOrders time.
docs/reviews/pr194-r2-codereview-codex-5-4.md:7954:    "@types/node": "25.6.0",
docs/reviews/pr194-r2-codereview-codex-5-4.md:8094:  3112	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8135:  2301	    ///         2. Execute scheduled market actions for closedTick (external calls).
docs/reviews/pr194-r2-codereview-codex-5-4.md:8152:  2318	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:7:**ship a working, legible, demoable autonomous-agent strategy game with real onchain state transitions and real market interaction.**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:65:ERC20s exist at the **market / payout boundary**, not as the canonical live game-state store.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:90:- market actions are heartbeat-eager, not lazy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:97:**Immediate market actions**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:98:- worker already in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:103:**Scheduled market actions**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:111:- `market_sell` = exact input, from worker carry to clan gold purse
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:112:- `market_buy` = exact output with `maxGoldIn`, from clan gold purse to worker carry
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:172:- market buy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:173:- market sell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:177:- Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:179:- scheduled and immediate market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:219:- market transparency is a feature
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:244:- immediate market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:253:- Unicorn Town market
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:272:### Milestone B — market alive
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:274:- worker travels to Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:314:4. a Unicorn Town trade
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:221:- `elder world market` — `getMarketState()`
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:302:    { "name": "market-analysis", "content": "<skill markdown>" }
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:381:- `clan:{id}:notebook/market_observations` — price patterns, trade outcomes
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:431:- All KV memory under `clan:{clanId}:*` — including any notes the previous agent wrote about other clans, market observations, grudges, alliances
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:531:    Anyone waiting can act. Consider: deposit, defend, market, new mission.

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/Reentrancy.t.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {MinimalERC20} from "../src/MinimalERC20.sol";
     7	import {StubPool} from "../src/StubPool.sol";
     8	import {
     9	    IClanWorld,
    10	    ClanWorldConstants,
    11	    ActionType,
    12	    StatusCode,
    13	    ClanOrder,
    14	    OrderResult,
    15	    Mission,
    16	    PoolSeedConfig
    17	} from "../src/IClanWorld.sol";
    18	
    19	contract MockReentrantPool {
    20	    address public immutable TOKEN_A;
    21	    address public immutable TOKEN_B;
    22	    address public immutable ENGINE;
    23	
    24	    uint256 public reserveA;
    25	    uint256 public reserveB;
    26	    bool public sawReentrantGuard;
    27	    bytes public lastRevertData;
    28	
    29	    bool private _seeded;
    30	
    31	    modifier onlyEngine() {
    32	        require(msg.sender == ENGINE, "MockReentrantPool: only engine");
    33	        _;
    34	    }
    35	
    36	    constructor(address tokenA_, address tokenB_, address engine_) {
    37	        TOKEN_A = tokenA_;
    38	        TOKEN_B = tokenB_;
    39	        ENGINE = engine_;
    40	    }
    41	
    42	    function seed(uint256 amountA, uint256 amountB) external onlyEngine {
    43	        require(!_seeded, "MockReentrantPool: already seeded");
    44	        require(amountA > 0 && amountB > 0, "MockReentrantPool: zero seed");
    45	        reserveA = amountA;
    46	        reserveB = amountB;
    47	        _seeded = true;
    48	    }
    49	
    50	    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
    51	        _attemptHeartbeatReentry();
    52	
    53	        require(amountIn > 0, "MockReentrantPool: zero amount");
    54	        require(reserveA > 0 && reserveB > 0, "MockReentrantPool: not seeded");
    55	        goldOut = (reserveB * amountIn) / (reserveA + amountIn);
    56	        require(goldOut > 0, "MockReentrantPool: zero output");
    57	        reserveA += amountIn;
    58	        reserveB -= goldOut;
    59	    }
    60	
    61	    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
    62	        require(amountOut > 0, "MockReentrantPool: zero amount");
    63	        require(amountOut < reserveA, "MockReentrantPool: insufficient resource reserve");
    64	        uint256 num = reserveB * amountOut;
    65	        uint256 denom = reserveA - amountOut;
    66	        goldIn = num / denom + (num % denom == 0 ? 0 : 1);
    67	    }
    68	
    69	    function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
    70	        require(amountOut > 0, "MockReentrantPool: zero amount");
    71	        require(amountOut < reserveA, "MockReentrantPool: insufficient resource reserve");
    72	        require(reserveB > 0, "MockReentrantPool: not seeded");
    73	        uint256 num = reserveB * amountOut;
    74	        uint256 denom = reserveA - amountOut;
    75	        goldIn = num / denom + (num % denom == 0 ? 0 : 1);
    76	        reserveA -= amountOut;
    77	        reserveB += goldIn;
    78	    }
    79	
    80	    function getReserves() external view returns (uint256, uint256) {
    81	        return (reserveA, reserveB);
    82	    }
    83	
    84	    function _attemptHeartbeatReentry() internal {
    85	        try IClanWorld(ENGINE).heartbeat() {
    86	            revert("MockReentrantPool: heartbeat reentry succeeded");
    87	        } catch Error(string memory reason) {
    88	            if (keccak256(bytes(reason)) == keccak256(bytes("ClanWorld: reentrant call"))) {
    89	                sawReentrantGuard = true;
    90	            }
    91	        } catch (bytes memory data) {
    92	            lastRevertData = data;
    93	        }
    94	    }
    95	}
    96	
    97	contract ReentrancyTest is Test {
    98	    ClanWorld world;
    99	    address elder = address(0xA1);
   100	
   101	    MinimalERC20 woodToken;
   102	    MinimalERC20 ironToken;
   103	    MinimalERC20 wheatToken;
   104	    MinimalERC20 fishToken;
   105	    MinimalERC20 goldToken;
   106	    MinimalERC20 blueprintToken;
   107	    MockReentrantPool woodPool;
   108	    StubPool ironPool;
   109	    StubPool wheatPool;
   110	    StubPool fishPool;
   111	
   112	    function setUp() public {
   113	        world = new ClanWorld();
   114	    }
   115	
   116	    function test_marketPoolHeartbeatCallback_revertsWithReentrancyGuard() public {
   117	        _setupReentrantMarket();
   118	        uint32 clanId = _mintClan();
   119	        uint32 csId = _firstCs(clanId);
   120	
   121	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   122	
   123	        OrderResult[] memory results = _submitMarketSell(clanId, csId, address(woodToken), 5e18);
   124	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "market sell should enqueue");
   125	
   126	        Mission memory mission = world.getActiveMission(csId);
   127	        uint64 currentTick = world.getWorldState().currentTick;
   128	        uint256 ticksNeeded = uint256(mission.settlesAtTick - currentTick) + 1;
   129	        for (uint256 i = 0; i < ticksNeeded; i++) {
   130	            _advanceTick();
   131	        }
   132	
   133	        assertTrue(woodPool.sawReentrantGuard(), "pool callback should receive reentrant guard revert");
   134	        assertEq(woodPool.lastRevertData().length, 0, "callback should catch the string guard revert");
   135	        assertGt(world.getClan(clanId).goldBalance, goldBefore, "heartbeat should finish original market action");
   136	    }
   137	
   138	    function _setupReentrantMarket() internal {
   139	        woodToken = new MinimalERC20("Wood", "WOOD");
   140	        ironToken = new MinimalERC20("Iron", "IRON");
   141	        wheatToken = new MinimalERC20("Wheat", "WHEAT");
   142	        fishToken = new MinimalERC20("Fish", "FISH");
   143	        goldToken = new MinimalERC20("Gold", "GOLD");
   144	        blueprintToken = new MinimalERC20("BPRT", "BPRT");
   145	
   146	        address engine = address(world);
   147	        woodPool = new MockReentrantPool(address(woodToken), address(goldToken), engine);
   148	        ironPool = new StubPool(address(ironToken), address(goldToken), engine);
   149	        wheatPool = new StubPool(address(wheatToken), address(goldToken), engine);
   150	        fishPool = new StubPool(address(fishToken), address(goldToken), engine);
   151	
   152	        address[6] memory tokens = [
   153	            address(woodToken),
   154	            address(ironToken),
   155	            address(wheatToken),
   156	            address(fishToken),
   157	            address(goldToken),
   158	            address(blueprintToken)
   159	        ];
   160	        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
   161	        world.initTreasury(tokens, pools);
   162	
   163	        uint256 resSeed = world.INITIAL_RESOURCE_POOL_SEED();
   164	        uint256 goldSeed = world.INITIAL_GOLD_POOL_SEED();
   165	        uint256 totalGoldSeed = goldSeed * 4;
   166	
   167	        woodToken.seedTreasury(address(this), resSeed);
   168	        wheatToken.seedTreasury(address(this), resSeed);
   169	        fishToken.seedTreasury(address(this), resSeed);
   170	        ironToken.seedTreasury(address(this), resSeed);
   171	        goldToken.seedTreasury(address(this), totalGoldSeed);
   172	
   173	        woodToken.approve(address(world), resSeed);
   174	        wheatToken.approve(address(world), resSeed);
   175	        fishToken.approve(address(world), resSeed);
   176	        ironToken.approve(address(world), resSeed);
   177	        goldToken.approve(address(world), totalGoldSeed);
   178	
   179	        PoolSeedConfig memory cfg = PoolSeedConfig({
   180	            woodSeed: resSeed,
   181	            wheatSeed: resSeed,
   182	            fishSeed: resSeed,
   183	            ironSeed: resSeed,
   184	            goldSeedForWood: goldSeed,
   185	            goldSeedForWheat: goldSeed,
   186	            goldSeedForFish: goldSeed,
   187	            goldSeedForIron: goldSeed
   188	        });
   189	        world.seedPools(cfg);
   190	    }
   191	
   192	    function _advanceTick() internal {
   193	        vm.warp(world.getWorldState().nextHeartbeatAtTs);
   194	        world.heartbeat();
   195	    }
   196	
   197	    function _mintClan() internal returns (uint32 clanId) {
   198	        vm.prank(elder);
   199	        (clanId,) = world.mintClan(elder);
   200	    }
   201	
   202	    function _firstCs(uint32 clanId) internal view returns (uint32) {
   203	        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
   204	    }
   205	
   206	    function _submitMarketSell(uint32 clanId, uint32 csId, address token, uint256 amount)
   207	        internal
   208	        returns (OrderResult[] memory)
   209	    {
   210	        ClanOrder[] memory orders = new ClanOrder[](1);
   211	        orders[0] = ClanOrder({
   212	            clansmanId: csId,
   213	            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
   214	            action: ActionType.MarketSell,
   215	            targetClanId: 0,
   216	            marketToken: token,
   217	            marketAmount: amount,
   218	            maxGoldIn: 0
   219	        });
   220	        vm.prank(elder);
   221	        return world.submitClanOrders(clanId, orders);
   222	    }
   223	}

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/Gathering.t.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {
     7	    ClanWorldConstants,
     8	    ActionType,
     9	    ClansmanState,
    10	    StatusCode,
    11	    Clansman,
    12	    Mission,
    13	    ClanFullView,
    14	    ClanOrder,
    15	    OrderResult
    16	} from "../src/IClanWorld.sol";
    17	
    18	contract GatheringHarness is ClanWorld {
    19	    function setCarryWood(uint32 csId, uint256 wood) external {
    20	        _clansmen[csId].carryWood = wood;
    21	    }
    22	}
    23	
    24	contract GatheringTest is Test {
    25	    GatheringHarness world;
    26	    address elder = address(0xA1);
    27	
    28	    function setUp() public {
    29	        world = new GatheringHarness();
    30	    }
    31	
    32	    function _advanceTick() internal {
    33	        vm.warp(world.getWorldState().nextHeartbeatAtTs);
    34	        world.heartbeat();
    35	    }
    36	
    37	    function _advanceUntilCurrentTick(uint64 targetTick) internal {
    38	        while (world.getWorldState().currentTick < targetTick) {
    39	            _advanceTick();
    40	        }
    41	    }
    42	
    43	    function _mintClan() internal returns (uint32 clanId) {
    44	        vm.prank(elder);
    45	        (clanId,) = world.mintClan(elder);
    46	    }
    47	
    48	    function _firstCs(uint32 clanId) internal view returns (uint32) {
    49	        ClanFullView memory view_ = world.getClanFullView(clanId);
    50	        return view_.clansmen[0].clansman.clansman.clansmanId;
    51	    }
    52	
    53	    function _submitChopWood(uint32 clanId, uint32 csId) internal returns (OrderResult[] memory) {
    54	        ClanOrder[] memory orders = new ClanOrder[](1);
    55	        orders[0] = ClanOrder({
    56	            clansmanId: csId,
    57	            gotoRegion: ClanWorldConstants.REGION_FOREST,
    58	            action: ActionType.ChopWood,
    59	            targetClanId: 0,
    60	            marketToken: address(0),
    61	            marketAmount: 0,
    62	            maxGoldIn: 0
    63	        });
    64	
    65	        vm.prank(elder);
    66	        return world.submitClanOrders(clanId, orders);
    67	    }
    68	
    69	    function _settleChopWood(uint32 clanId, uint32 csId) internal returns (Clansman memory) {
    70	        OrderResult[] memory result = _submitChopWood(clanId, csId);
    71	        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
    72	
    73	        Mission memory mission = world.getActiveMission(csId);
    74	        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
    75	        world.settleClan(clanId);
    76	        return world.getClansman(csId);
    77	    }
    78	
    79	    function test_chopWoodAtForestYieldsBaseTimesActionDuration() public {
    80	        vm.prevrandao(bytes32(uint256(2)));
    81	        uint32 clanId = _mintClan();
    82	        uint32 csId = _firstCs(clanId);
    83	
    84	        Clansman memory cs = _settleChopWood(clanId, csId);
    85	
    86	        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
    87	        assertEq(cs.carryWood, expected, "base wood yield");
    88	    }
    89	
    90	    function test_chopWoodCritDistributionAcrossSeeds() public {
    91	        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
    92	        uint256 critCount = 0;
    93	        world = new GatheringHarness();
    94	        uint256 cleanState = vm.snapshotState();
    95	
    96	        for (uint256 i = 0; i < 100; i++) {
    97	            assertTrue(vm.revertToState(cleanState), "reset gathering world");
    98	            vm.prevrandao(bytes32(uint256(i + 10_000)));
    99	            uint32 clanId = _mintClan();
   100	            uint32 csId = _firstCs(clanId);
   101	
   102	            Clansman memory cs = _settleChopWood(clanId, csId);
   103	            if (cs.carryWood == baseYield * 2) {
   104	                critCount++;
   105	            } else {
   106	                assertEq(cs.carryWood, baseYield, "non-crit yield");
   107	            }
   108	        }
   109	
   110	        assertGe(critCount, 3, "crit count too low");
   111	        assertLe(critCount, 20, "crit count too high");
   112	    }
   113	
   114	    function test_chopWoodClampsToCarryCap() public {
   115	        uint32 clanId = _mintClan();
   116	        uint32 csId = _firstCs(clanId);
   117	        world.setCarryWood(csId, ClanWorldConstants.WOOD_CARRY_CAP - 1e18);
   118	
   119	        Clansman memory cs = _settleChopWood(clanId, csId);
   120	
   121	        assertEq(cs.carryWood, ClanWorldConstants.WOOD_CARRY_CAP, "wood carry cap");
   122	    }
   123	
   124	    function test_chopWoodAppliesCooldownPostSettle() public {
   125	        uint32 clanId = _mintClan();
   126	        uint32 csId = _firstCs(clanId);
   127	
   128	        Clansman memory cs = _settleChopWood(clanId, csId);
   129	
   130	        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
   131	        assertFalse(world.getActiveMission(csId).active, "mission inactive");
   132	        assertGt(cs.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
   133	    }
   134	}

exec
/bin/bash -lc "sed -n '752,775p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

If target chain behavior differs, the implementation may swap to a chain-appropriate source later, but caller-supplied seed input is forbidden.

### 11.2 Immediate vs scheduled market ordering
Immediate market actions during tick `T` execute immediately in Elder tx order against current pool state at that tx’s execution time.

Scheduled market actions for tick `T` execute later at the closing heartbeat of tick `T`, in ascending `commitSequence` order.

Therefore:
- immediate actions during tick `T` may front-run scheduled actions that will execute at the close of tick `T`

### 11.3 Scheduled action lifecycle
A scheduled market action:
- is created when a travel-based market mission is accepted
- is indexed under `executeAtTick`
- executes eagerly at the corresponding heartbeat
- is deleted from storage after success or failure resolution

### 11.4 Reentrancy and interaction discipline
Any function path that can trigger AMM interaction must follow checks-effects-interactions discipline.

Recommended:
- apply `nonReentrant` to immediate-order paths that can swap
- apply `nonReentrant` to `heartbeat()` if it performs scheduled swaps

exec
/bin/bash -lc "sed -n '536,610p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
- worker becomes `WAITING` in Unicorn Town

## 5.5 Immediate market action
Immediate market action is allowed if and only if all of the following are true at tx execution time:
- clansman is physically in Unicorn Town
- clansman is in `WAITING` state
- clansman is not on cooldown
- action params are valid
- required inventory or gold balance is available

If those conditions hold, an Elder may submit:
- `goto = noop`
- `action = market_buy` or `market_sell`

and the swap executes immediately in that Elder tx.

## 5.6 Immediate market action effects
On successful immediate market action:
- the swap executes immediately in normal blockchain tx order
- the worker remains in Unicorn Town
- the worker returns to `WAITING`
- cooldown is consumed as if the worker had completed a normal mission action
- mission metadata / nonce is updated like any other mission assignment

Immediate market actions are therefore still **missions**, not an out-of-band bypass.

## 5.7 Immediate vs scheduled collision ordering
If immediate and scheduled market actions affect the same pool in the same tick:
- immediate market actions execute against the current pool state at the time their Elder tx lands
- scheduled market actions execute later, at the heartbeat that closes the tick, in deterministic FIFO order by mission commit order

This intentionally allows a clansman already stationed in Unicorn Town to front-run scheduled trades that will execute at tick close.

This is an intended feature of the game’s adversarial market design, not a bug.

## 5.8 Scheduled market action ordering
If multiple scheduled market actions mature in the same tick, Keeper executes them in deterministic FIFO order based on mission commit order.

Each swap sees updated market state from earlier swaps in that sequence.

## 5.9 Slippage rule for v1
For v1, market actions execute with **no slippage guard**.

Formally:
- `minAmountOut = 0`
- swaps do not revert due to price movement or adverse output

This applies to:
- scheduled market actions resolved at heartbeat close
- immediate market actions executed in Elder txs while already in Unicorn Town

This is intentional and preserves:
- adversarial market play
- MEV / front-running strategy
- punishing but simple execution semantics
- no swap-failure branching due to slippage in v1

## 5.10 Failed market actions
If a scheduled or immediate market action fails for reasons other than slippage, such as malformed params or unavailable balances:
- no partial swap occurs
- clansman retains their inventory or balances
- clansman becomes or remains `WAITING` in Unicorn Town

## 5.11 Initial pool seeding
At deploy time, a treasury or deployer-controlled bootstrap address mints and seeds the initial resource and gold liquidity.

### Working initial seed ratios
- Wood / Gold: `1000 wood : 500 gold`
- Wheat / Gold: `1000 wheat : 700 gold`
- Fish / Gold: `500 fish : 600 gold`
- Iron / Gold: `250 iron : 800 gold`

These numbers are economic placeholders and may be tuned later, but v1 requires concrete initial pool anchors.

## 5.12 Gold faucets

exec
/bin/bash -lc "sed -n '88,120p' 'docs/planning/clanworld_v4_3_schema_patch.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
- new mission gets a new `actionStartTick`

Therefore `actionStartTick` never refers to "most recent resumed action"; it always refers to the current active mission only.

---

## D. Scheduled Market Action Storage

### D.1 Canonical storage layout
Scheduled market actions are stored as:

`mapping(uint64 => ScheduledMarketAction[]) scheduledMarketActionsByTick`

Key:
- `executeAtTick`

Value:
- append-only FIFO array for that tick

### D.2 FIFO ordering
Within a given `executeAtTick`, scheduled market actions execute in:
1. ascending append order in `scheduledMarketActionsByTick[executeAtTick]`
2. equivalently, ascending global `commitSequence`

### D.3 Heartbeat cleanup
When heartbeat closes tick `T`:
- iterate `scheduledMarketActionsByTick[T]`
- execute each action in FIFO order
- emit success/failure events
- delete `scheduledMarketActionsByTick[T]` after processing

### D.4 Global `commitSequence`
`nextCommitSequence` is:

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '850,940p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   850	        uint8 old = clan.monumentLevel;
   851	        clan.monumentLevel = nextLevel;
   852	        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
   853	        return true;
   854	    }
   855	
   856	    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
   857	    function _completeMission(Clansman storage cs, Mission storage m) internal {
   858	        cs.state = ClansmanState.WAITING;
   859	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
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

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2300,2365p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '330,430p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc 'git show 9f93593:packages/contracts/src/IClanWorld.sol | rg -n "struct Mission|struct ScheduledMarketAction|OrderResult|ImmediateMarketActionExecuted|ScheduledMarketActionExecuted|MarketActionFailed|getScheduledMarketActionsForTick"' in /home/claude/code/clan-world
 succeeded in 0ms:
281:struct Mission {
325:struct ScheduledMarketAction {
382:struct OrderResult {
557:    event ImmediateMarketActionExecuted(
567:    event ScheduledMarketActionExecuted(
577:    event MarketActionFailed(
672:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
740:    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '281,340p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   281	struct Mission {
   282	    bool active;
   283	
   284	    uint64 nonce;
   285	    uint64 submittedAtTick;
   286	    uint64 executesAtTick;
   287	    uint64 settlesAtTick;
   288	    uint32 clansmanId;
   289	
   290	    uint8 startRegion;
   291	    uint8 targetRegion;
   292	    ActionType action;
   293	
   294	    uint64 startTick;
   295	    uint64 arrivalTick;
   296	    uint64 actionStartTick;
   297	
   298	    bytes32 missionSeed;
   299	    MarketExecutionMode marketMode;
   300	
   301	    uint32 targetClanId; // DefendBase only
   302	    address marketToken; // market token for buy/sell
   303	    uint256 marketAmount; // exact-in for sell, exact-out for buy
   304	    uint256 maxGoldIn; // market_buy only, 0 otherwise
   305	}
   306	
   307	struct BanditTroop {
   308	    uint32 banditId;
   309	    BanditState state;
   310	
   311	    uint8 currentRegion;
   312	    uint8 attackAttemptsMade;
   313	    uint64 stateEnteredTick;
   314	    uint64 nextActionTick;
   315	
   316	    uint8 tier;
   317	    uint16 attackPower; // derived from tier; tier is canonical (v4.3 §G)
   318	
   319	    uint256 carryWood;
   320	    uint256 carryIron;
   321	    uint256 carryWheat;
   322	    uint256 carryFish;
   323	}
   324	
   325	struct ScheduledMarketAction {
   326	    uint64 executeAtTick;
   327	    uint64 commitSequence; // global monotonic FIFO order
   328	    uint64 missionNonce; // mission nonce captured when the action was queued
   329	    uint32 clanId;
   330	    uint32 clansmanId;
   331	    ActionType action; // MarketBuy or MarketSell
   332	
   333	    address marketToken;
   334	    uint256 marketAmount; // exact-in for sell, exact-out for buy
   335	    uint256 maxGoldIn; // buy only, 0 otherwise
   336	}
   337	
   338	struct DefenseContribution {
   339	    uint32 clansmanId;
   340	    uint32 clanId;

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '382,590p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   382	struct OrderResult {
   383	    uint32 clansmanId;
   384	    StatusCode status;
   385	    uint64 cooldownEndsAtTs;
   386	    uint64 missionNonce;
   387	    MarketExecutionMode marketMode;
   388	}
   389	
   390	struct PoolSeedConfig {
   391	    uint256 woodSeed;
   392	    uint256 wheatSeed;
   393	    uint256 fishSeed;
   394	    uint256 ironSeed;
   395	    uint256 goldSeedForWood;
   396	    uint256 goldSeedForWheat;
   397	    uint256 goldSeedForFish;
   398	    uint256 goldSeedForIron;
   399	}
   400	
   401	// =============================================================================
   402	// UI INDEXER AGGREGATOR STRUCTS (v4.4 additions)
   403	// =============================================================================
   404	
   405	struct LeaderboardEntry {
   406	    uint32 clanId;
   407	    address owner;
   408	    uint8 monumentLevel;
   409	    uint8 baseLevel;
   410	    uint8 wallLevel;
   411	    uint8 livingClansmen;
   412	    ClanState state;
   413	    uint256 lootValue; // settled
   414	}
   415	
   416	struct WorldSnapshot {
   417	    uint64 currentTick;
   418	    uint64 seasonStartTick;
   419	    uint64 seasonEndTick;
   420	    bool seasonFinalized;
   421	    uint64 currentSeasonNumber;
   422	    uint64 nextHeartbeatAtTick;
   423	    bool winterActive;
   424	    uint64 winterStartsAtTick;
   425	    uint64 winterEndsAtTick;
   426	    uint32 activeBanditId;
   427	    bytes32 currentTickSeed;
   428	
   429	    LeaderboardEntry[] leaderboard;
   430	}
   431	
   432	struct ClansmanFullView {
   433	    DerivedClansmanState clansman;
   434	    Mission activeMission;
   435	}
   436	
   437	struct ClanFullView {
   438	    DerivedClanState clan;
   439	    ClansmanFullView[] clansmen;
   440	    WheatPlot westPlot;
   441	    WheatPlot eastPlot;
   442	    uint32[] incomingDefenderIds; // legacy UI field; clanIds defending this clan's home region
   443	    uint32 thisClanDefendingBaseId; // defended home region, or 0 if none
   444	}
   445	
   446	struct PoolReserves {
   447	    address resourceToken;
   448	    uint256 resourceReserve;
   449	    uint256 goldReserve;
   450	    uint256 spotPriceGoldPerResource; // = goldReserve * 1e18 / resourceReserve, or 0
   451	}
   452	
   453	struct MarketState {
   454	    PoolReserves wood;
   455	    PoolReserves wheat;
   456	    PoolReserves fish;
   457	    PoolReserves iron;
   458	
   459	    uint64 currentTick;
   460	    ScheduledMarketAction[] currentTickQueue;
   461	    ScheduledMarketAction[] nextTickQueue;
   462	}
   463	
   464	struct ActiveBanditView {
   465	    bool exists;
   466	    uint32 banditId;
   467	    BanditState state;
   468	    uint8 currentRegion;
   469	    uint8 attackAttemptsMade;
   470	    uint8 maxAttemptsRemaining;
   471	    uint64 stateEnteredTick;
   472	    uint64 nextActionTick;
   473	    uint8 tier;
   474	    uint16 attackPower;
   475	
   476	    uint256 carryWood;
   477	    uint256 carryIron;
   478	    uint256 carryWheat;
   479	    uint256 carryFish;
   480	
   481	    uint32 projectedTargetClanId; // 0 if no eligible target in current region
   482	    uint256 projectedTargetLootValue;
   483	}
   484	
   485	struct RegionOccupant {
   486	    uint32 clansmanId;
   487	    uint32 clanId;
   488	    ClansmanState state;
   489	    ActionType currentAction;
   490	    uint64 missionNonce;
   491	}
   492	
   493	// =============================================================================
   494	// EVENTS
   495	// =============================================================================
   496	
   497	interface IClanWorldEvents {
   498	    // ----- world clock -----
   499	    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
   500	    event WinterStarted(uint64 indexed tick);
   501	    event WinterEnded(uint64 indexed tick);
   502	    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
   503	
   504	    // ----- clan lifecycle -----
   505	    event ClanSpawned(
   506	        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
   507	    );
   508	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   509	    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
   510	    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
   511	
   512	    // ----- missions -----
   513	    event MissionAssigned(
   514	        uint32 indexed clanId,
   515	        uint32 indexed clansmanId,
   516	        uint64 missionNonce,
   517	        ActionType action,
   518	        uint8 startRegion,
   519	        uint8 targetRegion,
   520	        uint64 startTick,
   521	        uint64 arrivalTick
   522	    );
   523	    event MissionInterrupted(
   524	        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
   525	    );
   526	    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   527	    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   528	
   529	    // ----- gathering / vault movement -----
   530	    event ResourcesGathered(
   531	        uint32 indexed clanId,
   532	        uint32 indexed clansmanId,
   533	        ActionType action,
   534	        uint256 woodGained,
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

exec
/bin/bash -lc "sed -n '184,220p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
**Clansman carry domain**
- wood
- iron
- wheat
- fish

Used for:
- gathering
- hauling
- direct sell-to-market when physically in Unicorn Town
- receiving bought resources from market actions

**Clan vault domain**
- wood
- iron
- wheat
- fish

Used for:
- upkeep
- winter burn
- construction spending
- bandit theft
- OTC clan-to-clan resource transfers

**Clan purse domain**
- gold
- blueprint fragments

Used for:
- market sale proceeds
- market purchase spending
- mercenary payments
- OTC transfers of abstract assets

### 4.4 Deposit rule
Carried resources do not count for:

exec
/bin/bash -lc "sed -n '952,980p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

Then:
- tick 307 = travel tick
- worker arrives at start of tick 308
- tick 308 is the first gather tick

### 16.2 Immediate market sell
If:
- worker is `Waiting` in Unicorn Town
- cooldown is clear
- worker carries 12 wood
- Elder submits `MarketSell(wood, 12e18)`

Then:
- swap executes immediately in that tx
- clan gold purse increases by output
- worker stays in Unicorn Town
- worker returns to `Waiting`
- cooldown is consumed

### 16.3 Scheduled market buy
If:
- worker is one tick away from Unicorn Town in tick 307
- Elder submits `MarketBuy(wood, 5e18, 10e18)`

Then:
- tick 307 = travel
- tick 308 = single action tick in town
- heartbeat closing tick 308 attempts to buy exactly 5 wood spending at most 10 gold

exec
/bin/bash -lc "sed -n '96,116p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

**Immediate market actions**
- worker already in Unicorn Town
- worker in `WAITING`
- worker off cooldown
- executes immediately in Elder tx

**Scheduled market actions**
- travel first
- one action tick
- executes at heartbeat closing the action tick

Immediate actions can front-run scheduled actions in the same tick.

### 3.7 Buy/sell asymmetry
- `market_sell` = exact input, from worker carry to clan gold purse
- `market_buy` = exact output with `maxGoldIn`, from clan gold purse to worker carry

### 3.8 OTC model
OTC transfers are:
- direct internal ledger transfers

codex
The spec pass turned up one very important thing: the intended inventory domains are more specific than the tests suggest. I’m drilling into whether the implementation accidentally moved market I/O onto clan vault balances instead of worker carry, because that would reshape a lot more than just a single function.
exec
/bin/bash -lc "rg -n \"boundary|mint|burn|carry domain|direct sell-to-market|receiving bought resources|ERC20 tokens exist at the market / payout boundary|market_sell = exact input|market_buy = exact output\" 'docs/planning/V1/01 Blockchain Game Spec'" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:680:    function mintClan(address to) external payable returns (uint32 clanId, uint256 iftTokenId);
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:85:- season boundary transitions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:108:- game semantics still treat each successful heartbeat as the boundary between discrete ticks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:172:ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:184:**Clansman carry domain**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:193:- direct sell-to-market when physically in Unicorn Town
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:194:- receiving bought resources from market actions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:513:Used for one combat boundary to distribute loot correctly.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:640:- `mintClan(...)`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:641:  - public payable if minting is enabled
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:655:function mintClan(address to) external payable returns (uint32 clanId, uint256 iftTokenId);
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:729:If prize pot splitting is implemented, `mintClan` should route the configured mint-revenue share into `TreasuryState.prizePotGold` or an equivalent internal treasury bucket.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:843:- remaining vault resources are burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:97:## Phase 2 — clan mint / spawn lifecycle
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:99:### 2.1 Implement clan minting
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:130:- minting a clan gives a fully initialized playable clan
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:262:### 6.1 Deploy/seed resource boundary tokens
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:264:- resource boundary tokens if needed
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:265:- gold token boundary
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:397:- overflow burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:422:- wood burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:437:- burn physical vault loot
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:467:- mint revenue split
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:519:- mint/spawn
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:573:At every phase boundary, ask:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:496:- excess is burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:600:At deploy time, a treasury or deployer-controlled bootstrap address mints and seeds the initial resource and gold liquidity.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:612:- starter grants to newly minted clans
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:726:- all carried loot is burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:805:- any clansman loot above carrying capacity is burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:806:- the remaining `50%` of carried bandit loot is burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:835:- wood burn = `1e18` wood per base per tick
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1084:A newly minted clan starts with:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1096:A newly minted clan starts with:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1105:A newly minted clan starts with:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1127:- all remaining vault resources are **burned**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:65:ERC20s exist at the **market / payout boundary**, not as the canonical live game-state store.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:128:- physical vault loot is burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:146:- clan mint / spawn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:336:- the onchain/offchain boundary is sane

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '760,1010p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9f93593:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '1010,1515p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
  1461	        bool sawClan1SellEvent;
  1462	        bool sawClan2BuyEvent;
  1463	        for (uint256 i = 0; i < logs.length; i++) {
  1464	            if (logs[i].emitter != address(world) || logs[i].topics.length == 0 || logs[i].topics[0] != scheduledSig) {
  1465	                continue;
  1466	            }
  1467	
  1468	            uint32 eventClanId = uint32(uint256(logs[i].topics[1]));
  1469	            (uint32 eventCsId, ActionType eventAction,,,,,) =
  1470	                abi.decode(logs[i].data, (uint32, ActionType, uint8, uint256, uint8, uint256, uint64));
  1471	
  1472	            if (eventClanId == clanId1 && eventCsId == csId1 && eventAction == ActionType.MarketSell) {
  1473	                sawClan1SellEvent = true;
  1474	            }
  1475	            if (eventClanId == clanId2 && eventCsId == csId2 && eventAction == ActionType.MarketBuy) {
  1476	                sawClan2BuyEvent = true;
  1477	            }
  1478	        }
  1479	
  1480	        world.settleClan(clanId1);
  1481	        world.settleClan(clanId2);
  1482	
  1483	        assertTrue(sawClan1SellEvent, "sell event should carry clan1 id");
  1484	        assertTrue(sawClan2BuyEvent, "buy event should carry clan2 id");
  1485	        // Clan1 sold wood → gold should increase
  1486	        assertGt(world.getClan(clanId1).goldBalance, 3e18, "clan1 should have more gold after sell");
  1487	        // Clan2 bought wood → vault wood should increase beyond starter 20e18
  1488	        assertGt(world.getClan(clanId2).vaultWood, 20e18, "clan2 should have more vault wood after buy");
  1489	        // Clan2 spent gold
  1490	        assertLt(world.getClan(clanId2).goldBalance, 3e18, "clan2 should have less gold after buy");
  1491	    }
  1492	
  1493	    function test_scheduledMarketFailure_doesNotAffectAnotherClan() public {
  1494	        address woodAddr = _setupMarket();
  1495	
  1496	        uint32 clanId1 = _mintClan();
  1497	        vm.prank(elder2);
  1498	        (uint32 clanId2,) = world.mintClan(elder2);
  1499	
  1500	        uint32 csId1 = _firstCs(clanId1);
  1501	        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
  1502	
  1503	        OrderResult[] memory buyFail = _submitMarketOrder(clanId1, csId1, ActionType.MarketBuy, woodAddr, 1e18, 0);
  1504	        assertEq(uint8(buyFail[0].status), uint8(StatusCode.OK), "failing buy should enqueue");
  1505	
  1506	        ClanOrder[] memory sellOrders = new ClanOrder[](1);
  1507	        sellOrders[0] = ClanOrder({
  1508	            clansmanId: csId2,
  1509	            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
  1510	            action: ActionType.MarketSell,
  1511	            targetClanId: 0,
  1512	            marketToken: woodAddr,
  1513	            marketAmount: 5e18,
  1514	            maxGoldIn: 0
  1515	        });

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | rg -n \"\\.mint\\(|\\.burn\\(|ResourceMinted|ResourceBurned|getResourceToken|getPool|getPrice\"" in /home/claude/code/clan-world
 succeeded in 0ms:
2150:    function getResourceToken(uint8 resourceType) external view override returns (address) {
2158:    function getPool(uint8 resourceType) external view override returns (address) {
2162:    function getPrice(uint8 resourceType, uint256 amountIn) external view override returns (uint256 amountOut) {

exec
/bin/bash -lc "sed -n '258,308p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
---

## Phase 6 — Unicorn Town market integration

### 6.1 Deploy/seed resource boundary tokens
Set up:
- resource boundary tokens if needed
- gold token boundary
- seed supply holder / treasury

### 6.2 Seed pools
Deploy and seed:
- wood/gold
- wheat/gold
- fish/gold
- iron/gold

### 6.3 Implement immediate market actions
- worker in town
- waiting
- off cooldown
- immediate sell exact-in
- immediate buy exact-out + maxGoldIn

### 6.4 Implement scheduled market actions
- queue by executeAtTick
- FIFO append ordering
- heartbeat execution and deletion

### 6.5 Implement market failure semantics
- buy fails if:
  - insufficient purse gold
  - required gold exceeds `maxGoldIn`
  - output exceeds carry capacity
  - liquidity unavailable
- action still consumes cooldown
- worker ends `WAITING`

### 6.6 Add events and market result surface
Emit:
- immediate market action executed
- scheduled market action executed
- market action failed

**Definition of done**
- worker can go to town and trade
- gold economy works
- immediate vs scheduled semantics are proven

**Cut line:** immediate market actions can be deferred if necessary, but scheduled market execution must exist.


exec
/bin/bash -lc "sed -n '168,206p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
- clan blueprint balance
- per-clansman carry balances

### 4.2 Role of ERC20 tokens
ERC20 tokens exist at the **market / payout boundary**, not as the canonical internal source of truth for gameplay.

ERC20s are used for:
- interaction with seeded Uniswap pools
- optional prize payout plumbing
- optional future externalization / withdrawal features

ERC20 balances are **not** the authoritative representation of a clan’s vault or a clansman’s wheelbarrow.

### 4.3 Balance domains
There are three balance domains:

**Clansman carry domain**
- wood
- iron
- wheat
- fish

Used for:
- gathering
- hauling
- direct sell-to-market when physically in Unicorn Town
- receiving bought resources from market actions

**Clan vault domain**
- wood
- iron
- wheat
- fish

Used for:
- upkeep
- winter burn
- construction spending
- bandit theft

exec
/bin/bash -lc "sed -n '60,72p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
Canonical internal balances:
- clan vault resources
- clan purse balances
- worker carry balances

ERC20s exist at the **market / payout boundary**, not as the canonical live game-state store.

### 3.2 Heartbeat model
- heartbeat is **permissionless**
- one heartbeat advances **one tick**
- heartbeat interval target is **60 seconds**
- heartbeat derives randomness internally from onchain sources
- heartbeat does **not** take caller-supplied randomness

exec
/bin/bash -lc "sed -n '490,520p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

### 7.8 ScheduledMarketAction

Scheduled market actions must resolve eagerly at heartbeat boundaries and should therefore be indexed explicitly rather than lazily inferred.

```solidity
struct ScheduledMarketAction {
    uint64 executeAtTick;
    uint64 commitSequence;   // global monotonic FIFO order
    uint32 clanId;
    uint32 clansmanId;
    ActionType action;       // MarketBuy or MarketSell

    address marketToken;
    uint256 marketAmount;    // exact-in for sell, exact-out for buy
    uint256 maxGoldIn;       // buy only, 0 otherwise
}
```

Resolved scheduled actions should be deleted from storage after execution.

### 7.9 DefenseContribution

Used for one combat boundary to distribute loot correctly.

```solidity
struct DefenseContribution {
    uint32 clansmanId;
    uint32 clanId;
    uint16 defensePoints;
}

exec
/bin/bash -lc "sed -n '968,978p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
- worker stays in Unicorn Town
- worker returns to `Waiting`
- cooldown is consumed

### 16.3 Scheduled market buy
If:
- worker is one tick away from Unicorn Town in tick 307
- Elder submits `MarketBuy(wood, 5e18, 10e18)`

Then:
- tick 307 = travel

exec
/bin/bash -lc "sed -n '978,990p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
- tick 307 = travel
- tick 308 = single action tick in town
- heartbeat closing tick 308 attempts to buy exactly 5 wood spending at most 10 gold
- if price moved above 10 gold or purse balance is insufficient, trade fails, worker ends `Waiting`, cooldown consumed

### 16.4 Starving mercenary
If Clan B has a worker defending Clan A, and Clan B later enters starvation:
- that defending worker contributes 0 defense until Clan B starvation ends

This is intentional.

---


codex
I’m checking one more timing seam now: whether the market action tick is landing on the intended heartbeat or one full tick late. The docs use “single action tick in town” language, so the exact `arrivalTick` versus `settlesAtTick` mapping matters here.
exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '280,350p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "rg -n \"arrivalTick|actionStartTick|settlesAtTick|single action tick|MarketBuy|MarketSell\" 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md' 'docs/planning/clanworld_v4_3_schema_patch.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:456:    uint64 arrivalTick;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:457:    uint64 actionStartTick;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:501:    ActionType action;       // MarketBuy or MarketSell
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:565:### 8.3 MarketSell semantics
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:566:`MarketSell` is **exact input only**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:574:### 8.4 MarketBuy semantics
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:575:`MarketBuy` is **exact output only**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:590:- `MarketSell.marketToken != goldToken`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:591:- `MarketBuy.marketToken != goldToken`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:963:- Elder submits `MarketSell(wood, 12e18)`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:975:- Elder submits `MarketBuy(wood, 5e18, 10e18)`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:979:- tick 308 = single action tick in town
docs/planning/clanworld_v4_3_schema_patch.md:34:- `actionStartTick = startTick`
docs/planning/clanworld_v4_3_schema_patch.md:66:## C. `actionStartTick` Semantics
docs/planning/clanworld_v4_3_schema_patch.md:69:`Mission.actionStartTick` means:
docs/planning/clanworld_v4_3_schema_patch.md:75:- `arrivalTick = startTick + travelTicks`
docs/planning/clanworld_v4_3_schema_patch.md:76:- `actionStartTick = arrivalTick`
docs/planning/clanworld_v4_3_schema_patch.md:79:- `arrivalTick = startTick`
docs/planning/clanworld_v4_3_schema_patch.md:80:- `actionStartTick = startTick`
docs/planning/clanworld_v4_3_schema_patch.md:88:- new mission gets a new `actionStartTick`
docs/planning/clanworld_v4_3_schema_patch.md:90:Therefore `actionStartTick` never refers to "most recent resumed action"; it always refers to the current active mission only.
docs/planning/clanworld_v4_3_schema_patch.md:343:- `actionStartTick` semantics for the active mission only
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:95:- `arrivalTick`

exec
/bin/bash -lc "sed -n '448,468p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
    uint64 nonce;
    uint32 clansmanId;

    uint8 startRegion;
    uint8 targetRegion;
    ActionType action;

    uint64 startTick;
    uint64 arrivalTick;
    uint64 actionStartTick;

    bytes32 missionSeed;
    MarketExecutionMode marketMode;

    uint32 targetClanId;   // DefendBase only
    address marketToken;   // market token for buy/sell
    uint256 marketAmount;  // exact-in for sell, exact-out for buy
    uint256 maxGoldIn;     // market_buy only, 0 otherwise
}
```


exec
/bin/bash -lc "sed -n '66,86p' 'docs/planning/clanworld_v4_3_schema_patch.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
## C. `actionStartTick` Semantics

### C.1 Meaning
`Mission.actionStartTick` means:

> the tick at which the **current active mission's action phase** begins.

### C.2 Initial value
For a newly assigned mission:
- `arrivalTick = startTick + travelTicks`
- `actionStartTick = arrivalTick`

For same-region / noop missions:
- `arrivalTick = startTick`
- `actionStartTick = startTick`

### C.3 No resume semantics
Missions do not pause and resume.

If a mission is interrupted:
- old mission is discarded

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1962,2046p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "sed -n '1,20p' docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md" in /home/claude/code/clan-world
 succeeded in 0ms:
not good ADR format but want to capture plannimg conversation decisions


Afew choices the contract dev should know about:
	1.	I made BanditState.NONE = 0 so bandit.state == NONE is the natural “no active bandit” check. v4.2 omitted this; adding it avoids the ambiguous default value.
	2.	I gave granular event payloads (e.g. BanditAttackResolved includes wallLevelAfter and stolen amounts inline). The indexer can render attack drama without secondary calls.
	3.	ScheduledMarketActionCommitted is a new event — fires when a market mission’s scheduled action is queued. The UI uses this to show “trade pending” indicators before execution.
Flag any disagreements and I’ll revise.


exec
/bin/bash -lc 'git show 9f93593:packages/contracts/src/ClanWorld.sol | rg -n "getScheduledMarketActionsForTick|getMarketState|currentTickQueue|nextTickQueue"' in /home/claude/code/clan-world
 succeeded in 0ms:
2252:    function getScheduledMarketActionsForTick(uint64 tick)
2448:    function getMarketState() external view override returns (MarketState memory) {
2455:            currentTickQueue: _scheduledMarketActions[_world.currentTick],
2456:            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]

exec
/bin/bash -lc "git show 9f93593:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2246,2462p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2246	        returns (WheatPlot memory west, WheatPlot memory east)
  2247	    {
  2248	        west = _wheatPlots[clanId][0];
  2249	        east = _wheatPlots[clanId][1];
  2250	    }
  2251	
  2252	    function getScheduledMarketActionsForTick(uint64 tick)
  2253	        external
  2254	        view
  2255	        override
  2256	        returns (ScheduledMarketAction[] memory)
  2257	    {
  2258	        return _scheduledMarketActions[tick];
  2259	    }
  2260	
  2261	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  2262	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  2263	        uint256 count = 0;
  2264	
  2265	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2266	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2267	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2268	                Mission storage mission = _missions[clanClansmen[j]];
  2269	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2270	                    count++;
  2271	                }
  2272	            }
  2273	        }
  2274	
  2275	        clansmanIds = new uint32[](count);
  2276	        uint256 out = 0;
  2277	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2278	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2279	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2280	                uint32 clansmanId = clanClansmen[j];
  2281	                Mission storage mission = _missions[clansmanId];
  2282	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2283	                    clansmanIds[out++] = clansmanId;
  2284	                }
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
  2366	    ///      View function — no gas cost for off-chain indexer/UI reads.
  2367	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  2368	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
  2369	        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
  2370	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2371	            uint32 cid = _allClanIds[i];
  2372	            Clan storage clan = _clans[cid];
  2373	            lb[i] = LeaderboardEntry({
  2374	                clanId: cid,
  2375	                owner: clan.owner,
  2376	                monumentLevel: clan.monumentLevel,
  2377	                baseLevel: clan.baseLevel,
  2378	                wallLevel: clan.wallLevel,
  2379	                livingClansmen: clan.livingClansmen,
  2380	                state: clan.clanState,
  2381	                lootValue: _lootValueRaw(clan)
  2382	            });
  2383	        }
  2384	
  2385	        return WorldSnapshot({
  2386	            currentTick: _world.currentTick,
  2387	            seasonStartTick: _world.seasonStartTick,
  2388	            seasonEndTick: _world.seasonEndTick,
  2389	            seasonFinalized: _world.seasonFinalized,
  2390	            currentSeasonNumber: _world.currentSeasonNumber,
  2391	            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
  2392	            winterActive: _world.winterActive,
  2393	            winterStartsAtTick: _world.winterStartsAtTick,
  2394	            winterEndsAtTick: _world.winterEndsAtTick,
  2395	            activeBanditId: _world.activeBanditId,
  2396	            currentTickSeed: _world.currentTickSeed,
  2397	            leaderboard: lb
  2398	        });
  2399	    }
  2400	
  2401	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
  2402	    ///      current; carry amounts and wheat progress lag until next settleClan() call.
  2403	    ///      Full view-only settlement simulation is deferred (tracked issue).
  2404	    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
  2405	        Clan storage clan = _clans[clanId];
  2406	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  2407	        uint256 lootVal = _lootValueRaw(clan);
  2408	
  2409	        DerivedClanState memory derivedClan =
  2410	            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
  2411	
  2412	        uint32[] storage csIds = _clanClansmanIds[clanId];
  2413	        ClansmanFullView[] memory clansmen = new ClansmanFullView[](csIds.length);
  2414	        for (uint256 i = 0; i < csIds.length; i++) {
  2415	            uint32 csId = csIds[i];
  2416	            Clansman memory cs = _clansmen[csId];
  2417	            Mission memory m = _missions[csId];
  2418	            uint8 effRegion = cs.currentRegion;
  2419	            if (cs.state == ClansmanState.TRAVELING && m.active) {
  2420	                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
  2421	            }
  2422	            DerivedClansmanState memory dcs = DerivedClansmanState({
  2423	                clansman: cs, activeMission: m, effectiveRegion: effRegion, derivedAtTick: _world.currentTick
  2424	            });
  2425	            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
  2426	        }
  2427	
  2428	        // Find if any of this clan's clansmen is defending a home region.
  2429	        uint32 thisClanDefendingBaseId = 0;
  2430	        for (uint256 i = 0; i < csIds.length; i++) {
  2431	            uint8 region = _clansmanDefendingRegion[csIds[i]];
  2432	            if (region != 0) {
  2433	                thisClanDefendingBaseId = region;
  2434	                break;
  2435	            }
  2436	        }
  2437	
  2438	        return ClanFullView({
  2439	            clan: derivedClan,
  2440	            clansmen: clansmen,
  2441	            westPlot: _wheatPlots[clanId][0],
  2442	            eastPlot: _wheatPlots[clanId][1],
  2443	            incomingDefenderIds: _defendingClansByRegion[clan.baseRegion],
  2444	            thisClanDefendingBaseId: thisClanDefendingBaseId
  2445	        });
  2446	    }
  2447	
  2448	    function getMarketState() external view override returns (MarketState memory) {
  2449	        return MarketState({
  2450	            wood: _poolReserves(_treasury.woodToken, _treasury.woodGoldPool),
  2451	            wheat: _poolReserves(_treasury.wheatToken, _treasury.wheatGoldPool),
  2452	            fish: _poolReserves(_treasury.fishToken, _treasury.fishGoldPool),
  2453	            iron: _poolReserves(_treasury.ironToken, _treasury.ironGoldPool),
  2454	            currentTick: _world.currentTick,
  2455	            currentTickQueue: _scheduledMarketActions[_world.currentTick],
  2456	            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]
  2457	        });
  2458	    }
  2459	
  2460	    function _poolReserves(address resourceToken, address poolAddr) internal view returns (PoolReserves memory pr) {
  2461	        pr.resourceToken = resourceToken;
  2462	        if (poolAddr == address(0) || resourceToken == address(0)) {

collab: Wait
codex
One subagent pass is back and it independently spotted the carry/vault mismatch in the buy path, which matches the bigger inventory-domain concern I was already tracing. I’m waiting on the security/data-flow pass now and then I’ll consolidate the final review with severity ordering.
collab: Wait
codex
# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
NEEDS_FIXES. The heartbeat/FIFO plumbing is close, but two core Phase 6 invariants are off: market trades are wired to clan vault balances instead of worker carry, and the new ERC20 boundary layer is only seeded, not actually bridged during swaps. There’s also spec drift in when scheduled actions enter the queue and how immediate actions surface mission state, which will leak into UI/indexer behavior and later bandit/upkeep interactions.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:1656-1673, 1723-1773, 1824-1847, 1883-1947, 2033-2035`  
  Market I/O is using the wrong balance domain. Sells read/deduct `clan.vault*`, and buys credit `clan.vault*`, while submission still rejects buys based on worker carry capacity. Per the Phase 6 market spec, `market_sell` is from worker carry and `market_buy` returns to worker carry. As shipped, a clansman can liquidate home-vault inventory without hauling it to Unicorn Town, and bought goods become instantly eligible for upkeep/bandit theft without a deposit trip. The tests lock this in (`packages/contracts/test/ClanWorld.t.sol:775-888, 1013-1085, 1485-1490`).
- `packages/contracts/src/StubPool.sol:66-94, 99-111` with `packages/contracts/src/ClanWorld.sol:1668-1673, 1771-1773, 1836-1847, 1946-1947`  
  The ERC20 boundary layer is not actually used during trading. Swaps mutate only `reserveA/reserveB`; no token transfer, mint, or burn happens on swap, and `ClanWorld` never calls the new `MinimalERC20.mint/burn` hooks. After the first trade, pool token balances diverge from the reserves the AMM quotes, so the seeded boundary-token state no longer backs the live market state. `packages/contracts/test/SeedPools.t.sol:61-93` only checks reserve/balance parity immediately after seeding, not after swaps.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:469-470, 1321-1337, 2252-2259, 2448-2456` and `packages/contracts/test/ClanWorld.t.sol:818-826, 837-844, 1271-1276`  
  Scheduled actions are now created lazily at mission settlement, not when the market mission is accepted. Execution order is recovered later by sorting `commitSequence`, but the queue/getter/event surface no longer represents pending trades ahead of the execution heartbeat. That drifts from the Phase 6 queue model and breaks the intended “trade pending” UI/indexer semantics.
- `packages/contracts/src/ClanWorld.sol:1210-1229`  
  Immediate market actions are implemented as an out-of-band fast path: nonce increments and the swap/event happen, but no mission is installed and no normal mission lifecycle is emitted. The Phase 6 spec says immediate trades are still missions, not a bypass, so this is behavioral/API drift for consumers that rely on canonical mission metadata or mission events.

## LOW severity findings
- `packages/contracts/test/ClanWorld.t.sol:775-1515`, `packages/contracts/test/SeedPools.t.sol:54-69`  
  Integration coverage is heavily wood-centric. The PR adds four resources/pools, but the immediate/scheduled trade seam is only exercised with wood; wheat/fish/iron are only covered by getter/seeding assertions, not by end-to-end buy/sell execution tests.
- `packages/contracts/test/Reentrancy.t.sol:50-78`  
  Reentrancy coverage still targets the legacy scheduled sell path. The new immediate-path pool calls (`swapExactInForOut`, `swapExactOutForInWithMaxIn`) are not directly exercised with a hostile pool, so the newly added external-call surface is only indirectly covered.

## Cross-cutting observations
- The settle-then-execute heartbeat ordering looks sound, and sorting by `commitSequence` should preserve deterministic same-tick FIFO execution.
- The biggest integrated consequence is cross-phase: routing market buys/sells through vault balances instead of worker carry materially changes Phase 5 deposit logistics and Phase 9 bandit timing.
- I reviewed the PR head/diff and specs, but I did not run `forge test` in this read-only session.
2026-04-29T18:06:47.200156Z ERROR codex_core::session: failed to record rollout items: thread 019dda66-8104-7693-bd3e-96fac497ee98 not found
2026-04-29T18:06:47.200615Z ERROR codex_core::session: failed to record rollout items: thread 019dda65-bc9d-74b1-898f-44824da0a158 not found
2026-04-29T18:06:47.200168Z ERROR codex_core::session: failed to record rollout items: thread 019dda66-8093-77c1-ba98-e55cc34b8ffa not found
tokens used
532,495
# Phase Super-Swarm Review — PR #198 (head 9f93593)

## SUMMARY
NEEDS_FIXES. The heartbeat/FIFO plumbing is close, but two core Phase 6 invariants are off: market trades are wired to clan vault balances instead of worker carry, and the new ERC20 boundary layer is only seeded, not actually bridged during swaps. There’s also spec drift in when scheduled actions enter the queue and how immediate actions surface mission state, which will leak into UI/indexer behavior and later bandit/upkeep interactions.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:1656-1673, 1723-1773, 1824-1847, 1883-1947, 2033-2035`  
  Market I/O is using the wrong balance domain. Sells read/deduct `clan.vault*`, and buys credit `clan.vault*`, while submission still rejects buys based on worker carry capacity. Per the Phase 6 market spec, `market_sell` is from worker carry and `market_buy` returns to worker carry. As shipped, a clansman can liquidate home-vault inventory without hauling it to Unicorn Town, and bought goods become instantly eligible for upkeep/bandit theft without a deposit trip. The tests lock this in (`packages/contracts/test/ClanWorld.t.sol:775-888, 1013-1085, 1485-1490`).
- `packages/contracts/src/StubPool.sol:66-94, 99-111` with `packages/contracts/src/ClanWorld.sol:1668-1673, 1771-1773, 1836-1847, 1946-1947`  
  The ERC20 boundary layer is not actually used during trading. Swaps mutate only `reserveA/reserveB`; no token transfer, mint, or burn happens on swap, and `ClanWorld` never calls the new `MinimalERC20.mint/burn` hooks. After the first trade, pool token balances diverge from the reserves the AMM quotes, so the seeded boundary-token state no longer backs the live market state. `packages/contracts/test/SeedPools.t.sol:61-93` only checks reserve/balance parity immediately after seeding, not after swaps.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:469-470, 1321-1337, 2252-2259, 2448-2456` and `packages/contracts/test/ClanWorld.t.sol:818-826, 837-844, 1271-1276`  
  Scheduled actions are now created lazily at mission settlement, not when the market mission is accepted. Execution order is recovered later by sorting `commitSequence`, but the queue/getter/event surface no longer represents pending trades ahead of the execution heartbeat. That drifts from the Phase 6 queue model and breaks the intended “trade pending” UI/indexer semantics.
- `packages/contracts/src/ClanWorld.sol:1210-1229`  
  Immediate market actions are implemented as an out-of-band fast path: nonce increments and the swap/event happen, but no mission is installed and no normal mission lifecycle is emitted. The Phase 6 spec says immediate trades are still missions, not a bypass, so this is behavioral/API drift for consumers that rely on canonical mission metadata or mission events.

## LOW severity findings
- `packages/contracts/test/ClanWorld.t.sol:775-1515`, `packages/contracts/test/SeedPools.t.sol:54-69`  
  Integration coverage is heavily wood-centric. The PR adds four resources/pools, but the immediate/scheduled trade seam is only exercised with wood; wheat/fish/iron are only covered by getter/seeding assertions, not by end-to-end buy/sell execution tests.
- `packages/contracts/test/Reentrancy.t.sol:50-78`  
  Reentrancy coverage still targets the legacy scheduled sell path. The new immediate-path pool calls (`swapExactInForOut`, `swapExactOutForInWithMaxIn`) are not directly exercised with a hostile pool, so the newly added external-call surface is only indirectly covered.

## Cross-cutting observations
- The settle-then-execute heartbeat ordering looks sound, and sorting by `commitSequence` should preserve deterministic same-tick FIFO execution.
- The biggest integrated consequence is cross-phase: routing market buys/sells through vault balances instead of worker carry materially changes Phase 5 deposit logistics and Phase 9 bandit timing.
- I reviewed the PR head/diff and specs, but I did not run `forge test` in this read-only session.
