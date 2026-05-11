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
session id: 019dda4d-22c3-70e2-b36d-bd5df8409d2b
--------
user
Read the prompt and diff from stdin. Use parallel tool calls and subagents to thoroughly review. Output the full review to stdout in the requested format.

<stdin>
You are reviewing the FIX-ROUND for PR #194 (Phase 9 bandit system) at head 218f902.

Previous super-swarm found 5 HIGH findings. The fix-round attempted to address all of them. Your job: verify the fixes work AND that no new bugs were introduced. Pay special attention to:

1. Camped→Attacking dispatch in heartbeat — does production now actually drive the bandit attack lifecycle? Test it.
2. TypeScript ABI updates — runner + shared client decode the new WorldState/WorldSnapshot correctly?
3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
4. Blueprint reward = 1e18 (was += 1).
5. _resolveBanditAttack revert race — does it now safely return when settlement kills target mid-attack?

Plus standard cohesive-phase review: cross-cutting bugs, security surface, data flow, integration risks, test coverage.

USE PARALLEL TOOL CALLS. Read the diff carefully — the fix-round is on top of the prior PR work.

Output format same as before:

# Phase Super-Swarm Review (R2) — PR #194 (head 218f902)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES). Did the 5 HIGH fixes land cleanly? Any new HIGH/MEDIUM introduced?

## HIGH severity findings
(or "CLEAN — no findings")

## MEDIUM severity findings

## LOW severity findings

## Cross-cutting observations

DIFF FOLLOWS BELOW.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..b924990 100644
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
@@ -234,80 +253,79 @@
     },
     {
       "type": "function",
-      "name": "getMissionTiming",
+      "name": "getBandit",
       "inputs": [
         {
-          "name": "clanId",
-          "type": "uint32",
-          "internalType": "uint32"
-        },
-        {
-          "name": "clansmanId",
+          "name": "banditId",
           "type": "uint32",
           "internalType": "uint32"
         }
       ],
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
       "outputs": [
         {
           "name": "",
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
+          "type": "tuple",
+          "internalType": "struct BanditTroop",
+          "components": [
+            {
+              "name": "id",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "region",
+              "type": "uint8",
+              "internalType": "uint8"
+            },
+            {
+              "name": "state",
+              "type": "uint8",
+              "internalType": "enum BanditState"
+            },
+            {
+              "name": "targetClanId",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "tickEnteredState",
+              "type": "uint64",
+              "internalType": "uint64"
+            },
+            {
+              "name": "strength",
+              "type": "uint32",
+              "internalType": "uint32"
+            },
+            {
+              "name": "carryWood",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "carryIron",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "carryWheat",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "carryFish",
+              "type": "uint256",
+              "internalType": "uint256"
+            },
+            {
+              "name": "carryGold",
+              "type": "uint256",
+              "internalType": "uint256"
+            }
+          ]
         }
       ],
-      "stateMutability": "pure"
+      "stateMutability": "view"
     },
     {
       "type": "function",
@@ -345,44 +363,34 @@
           "internalType": "struct BanditTroop",
           "components": [
             {
-              "name": "banditId",
+              "name": "id",
               "type": "uint32",
               "internalType": "uint32"
             },
             {
-              "name": "state",
-              "type": "uint8",
-              "internalType": "enum BanditState"
-            },
-            {
-              "name": "currentRegion",
+              "name": "region",
               "type": "uint8",
               "internalType": "uint8"
             },
             {
-              "name": "attackAttemptsMade",
+              "name": "state",
               "type": "uint8",
-              "internalType": "uint8"
+              "internalType": "enum BanditState"
             },
             {
-              "name": "stateEnteredTick",
-              "type": "uint64",
-              "internalType": "uint64"
+              "name": "targetClanId",
+              "type": "uint32",
+              "internalType": "uint32"
             },
             {
-              "name": "nextActionTick",
+              "name": "tickEnteredState",
               "type": "uint64",
               "internalType": "uint64"
             },
             {
-              "name": "tier",
-              "type": "uint8",
-              "internalType": "uint8"
-            },
-            {
-              "name": "attackPower",
-              "type": "uint16",
-              "internalType": "uint16"
+              "name": "strength",
+              "type": "uint32",
+              "internalType": "uint32"
             },
             {
               "name": "carryWood",
@@ -403,12 +411,36 @@
               "name": "carryFish",
               "type": "uint256",
               "internalType": "uint256"
+            },
+            {
+              "name": "carryGold",
+              "type": "uint256",
+              "internalType": "uint256"
             }
           ]
         }
       ],
       "stateMutability": "view"
     },
+    {
+      "type": "function",
+      "name": "getBanditsInRegion",
+      "inputs": [
+        {
+          "name": "region",
+          "type": "uint8",
+          "internalType": "uint8"
+        }
+      ],
+      "outputs": [
+        {
+          "name": "",
+          "type": "uint32[]",
+          "internalType": "uint32[]"
+        }
+      ],
+      "stateMutability": "view"
+    },
     {
       "type": "function",
       "name": "getClan",
@@ -1639,6 +1671,40 @@
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
     {
       "type": "function",
       "name": "getRegionPopulation",
@@ -1751,6 +1817,30 @@
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
@@ -1929,6 +2019,16 @@
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
@@ -2036,6 +2136,16 @@
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
@@ -2270,6 +2380,19 @@
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
@@ -2691,6 +2814,31 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "BanditTargetDied",
+      "inputs": [
+        {
+          "name": "banditId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "deadClanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "tick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "BaseLevelChanged",
@@ -2747,6 +2895,37 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "BlueprintEarned",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "banditId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "amount",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "tick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "BlueprintTransferred",
@@ -2897,6 +3076,31 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "ClansmanKilledByBandit",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "clansmanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "banditId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "ColdDamageApplied",
@@ -3008,6 +3212,85 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "LootDistributed",
+      "inputs": [
+        {
+          "name": "banditId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "clanIdsRewarded",
+          "type": "uint32[]",
+          "indexed": false,
+          "internalType": "uint32[]"
+        },
+        {
+          "name": "perClanWood",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "perClanWheat",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "perClanFish",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "perClanIron",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "perClanGold",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "burnedWood",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "burnedWheat",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "burnedFish",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "burnedIron",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        },
+        {
+          "name": "burnedGold",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "LootDistributedToDefender",
@@ -3568,6 +3851,31 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "WallDamagedByBandit",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "newLevel",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "uint8"
+        },
+        {
+          "name": "banditId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "WallLevelChanged",
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..85a1751 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -37,6 +37,7 @@ import {
     ActiveBanditView,
     RegionOccupant
 } from "./IClanWorld.sol";
+import {RNG} from "./lib/RNG.sol";
 import {StubPool} from "./StubPool.sol";
 import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
 
@@ -61,10 +62,15 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
     mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
     mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
+    mapping(uint32 => BanditTroop) internal _bandits;
+    mapping(uint8 => uint32[]) internal _banditsByRegion; // region => bandit IDs
+    mapping(uint8 => BanditSpawnState) internal _banditSpawnByRegion;
     mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
 
     uint32 private _nextClanId;
     uint32 private _nextClansmanId;
+    uint32 internal _nextBanditId;
+    uint32 internal _activeBanditCount;
     uint32[] private _allClanIds;
 
     // per-clan clansman list: clanId => clansmanId[]
@@ -75,8 +81,48 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // =========================================================================
 
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
+    uint256 private constant RESOURCE_UNIT = 1e18;
+    uint256 internal constant BLUEPRINT_UNIT = 1e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
+    uint256 internal constant DOMAIN_BANDIT_SPAWN = uint256(keccak256("clanworld.bandit.spawn.v1"));
+    uint256 internal constant DOMAIN_BANDIT_TARGET_PICK = uint256(keccak256("bandit_target_pick"));
+    uint64 internal constant MIN_SPAWN_COOLDOWN_TICKS = ClanWorldConstants.BANDIT_COOLDOWN_TICKS;
+    uint16 internal constant BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS = 1000;
+    uint16 internal constant BANDIT_SPAWN_MAX_PROBABILITY_BPS = 8000;
+    uint8 internal constant MAX_BANDITS_PER_REGION = 3;
+    uint8 internal constant MAX_TOTAL_BANDITS = 8;
+    uint8 internal constant MAX_CLANS = 12;
+    /// @dev Bandit spawn weights are a heartbeat-time heuristic. V1 has
+    ///      MAX_CLANS = 12, so scanning 8 clans per tick covers the live cap in
+    ///      at most two rotating heartbeats while keeping heartbeat gas bounded.
+    uint256 internal constant MAX_BANDIT_SPAWN_SCAN_PER_REGION = 8;
+    uint256 internal constant MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION = MAX_BANDIT_SPAWN_SCAN_PER_REGION * 4;
+    /// @dev Eager settlement scans the clan-indexed bases in each spawn-candidate
+    ///      region, not every clan globally per region forever. MAX_CLANS = 12,
+    ///      so this settles all possible bases today while keeping the heartbeat
+    ///      loop explicitly bounded if that cap grows.
+    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION = 12;
+    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION = 12;
+    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION = 48;
+    uint32 internal constant MIN_BANDIT_SPAWN_STRENGTH = 100;
+    uint32 internal constant BANDIT_SPAWN_STRENGTH_SPREAD = 151;
+    uint32 internal constant CLANSMAN_MAX_DEFENSE_DAMAGE = 100;
+    uint32 internal constant WALL_HP_PER_LEVEL = 100;
+    uint32 internal constant BASE_HP_PER_LEVEL = 25;
+    uint32 internal constant CLANSMAN_HP = 100;
+
+    struct BanditSpawnState {
+        uint64 lastSpawnTick;
+        uint16 probabilityAccum;
+    }
+
+    struct SettlementSimulation {
+        Clan clan;
+        Clansman[] clansmen;
+        Mission[] missions;
+        WheatPlot[2] wheatPlots;
+    }
 
     // =========================================================================
     // CONSTRUCTOR
@@ -91,13 +137,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
         // i.e. ticks [100, 110)
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
         _world.winterActive = false;
         _treasury.treasuryOwner = msg.sender;
         _nextClanId = 1;
         _nextClansmanId = 1;
+        _nextBanditId = 1;
     }
 
     // =========================================================================
@@ -371,6 +417,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         for (uint64 tick = fromTick; tick < curTick; tick++) {
             // 1. Apply upkeep for this tick
             _applyUpkeep(clan, tick);
+            if (clan.clanState == ClanState.DEAD) break;
 
             // 2. Wheat plot regrow check (lazy, per tick)
             for (uint256 pi = 0; pi < 2; pi++) {
@@ -422,6 +469,142 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             clan.starvationStartsAtTick = 0;
             emit ClanStarvationChanged(clan.clanId, false, tick);
         }
+
+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
+            _killNextClansmanFromStarvation(clan, tick);
+        }
+    }
+
+    function _isWinterTick(uint64 tick) internal pure returns (bool) {
+        uint64 seasonOffset = tick % ClanWorldConstants.SEASON_DURATION_TICKS;
+        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
+        uint64 cycleStart = seasonOffset - cycleOffset;
+        uint64 winterStart =
+            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
+
+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
+            && seasonOffset < winterEnd;
+    }
+
+    function _killNextClansmanFromStarvation(Clan storage clan, uint64 tick) internal {
+        if (clan.livingClansmen == 0) return;
+
+        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
+        for (uint256 i = 0; i < csIds.length; i++) {
+            Clansman storage cs = _clansmen[csIds[i]];
+            if (cs.state == ClansmanState.DEAD) continue;
+
+            _markClansmanDead(clan, cs);
+            if (clan.livingClansmen == 0) {
+                _markClanDead(clan.clanId, "starvation", tick);
+            }
+            return;
+        }
+    }
+
+    function _markClansmanDead(Clan storage clan, Clansman storage cs) internal {
+        if (cs.state == ClansmanState.DEAD) return;
+
+        cs.state = ClansmanState.DEAD;
+        cs.cooldownEndsAtTs = 0;
+        if (clan.livingClansmen > 0) {
+            clan.livingClansmen--;
+        }
+
+        Mission storage mission = _missions[cs.clansmanId];
+        if (mission.active) {
+            if (mission.action == ActionType.DefendBase) {
+                _clearDefender(cs.clansmanId);
+            }
+            mission.active = false;
+        }
+    }
+
+    function _markClanDead(uint32 clanId) internal {
+        _markClanDead(clanId, "unknown", _world.currentTick, ClanWorldConstants.BANDIT_ID_NULL);
+    }
+
+    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {
+        _markClanDead(clanId, reason, tick, ClanWorldConstants.BANDIT_ID_NULL);
+    }
+
+    function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId) internal {
+        Clan storage clan = _clans[clanId];
+        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
+
+        uint8 baseRegion = clan.baseRegion;
+        clan.clanState = ClanState.DEAD;
+        clan.vaultWood = 0;
+        clan.vaultWheat = 0;
+        clan.vaultFish = 0;
+        clan.vaultIron = 0;
+        clan.starvationStartsAtTick = 0;
+        clan.livingClansmen = 0;
+
+        uint32[] storage csIds = _clanClansmanIds[clanId];
+        for (uint256 i = 0; i < csIds.length; i++) {
+            Clansman storage cs = _clansmen[csIds[i]];
+            cs.state = ClansmanState.DEAD;
+            cs.cooldownEndsAtTs = 0;
+            Mission storage mission = _missions[csIds[i]];
+            if (mission.active) {
+                if (mission.action == ActionType.DefendBase) {
+                    _clearDefender(csIds[i]);
+                }
+                mission.active = false;
+            }
+        }
+
+        _releaseDefendersForDeadTarget(clanId, baseRegion);
+        _abortBanditAttacksForDeadTarget(clanId, excludedBanditId);
+
+        emit ClanEliminated(clanId, tick);
+    }
+
+    function _releaseDefendersForDeadTarget(uint32 deadClanId, uint8 baseRegion) internal {
+        for (uint256 i = 0; i < _allClanIds.length; i++) {
+            uint32 defenderClanId = _allClanIds[i];
+            if (defenderClanId == deadClanId) continue;
+
+            uint32[] storage csIds = _clanClansmanIds[defenderClanId];
+            for (uint256 j = 0; j < csIds.length; j++) {
+                uint32 clansmanId = csIds[j];
+                Mission storage mission = _missions[clansmanId];
+                if (
+                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
+                        && _clansmanDefendingRegion[clansmanId] == baseRegion
+                ) {
+                    _clearDefender(clansmanId);
+                    mission.active = false;
+
+                    Clansman storage defender = _clansmen[clansmanId];
+                    if (defender.state != ClansmanState.DEAD) {
+                        defender.state = ClansmanState.WAITING;
+                    }
+                }
+            }
+        }
+    }
+
+    function _abortBanditAttacksForDeadTarget(uint32 deadClanId, uint32 excludedBanditId) internal {
+        // Match _transitionBanditState's event stamp; heartbeat keeps currentTick
+        // equal to the closed tick while aborting linked bandit attacks.
+        uint64 currentTick = _world.currentTick;
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            uint32[] storage regionBandits = _banditsByRegion[region];
+            for (uint256 i = 0; i < regionBandits.length; i++) {
+                uint32 banditId = regionBandits[i];
+                if (banditId == excludedBanditId) continue;
+
+                BanditTroop storage bandit = _bandits[banditId];
+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
+                    _transitionBanditState(banditId, BanditState.Escaped);
+                    emit BanditEscaped(banditId, currentTick);
+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
+                }
+            }
+        }
     }
 
     /// @dev Check if a clan is currently starving (lazy read).
@@ -689,175 +872,1422 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         clan.vaultWheat += wh;
         clan.vaultFish += fi;
 
-        cs.carryWood = 0;
-        cs.carryIron = 0;
-        cs.carryWheat = 0;
-        cs.carryFish = 0;
+        cs.carryWood = 0;
+        cs.carryIron = 0;
+        cs.carryWheat = 0;
+        cs.carryFish = 0;
+
+        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
+        _completeMission(cs, m);
+    }
+
+    function _doBuilding(
+        Clan storage clan,
+        Clansman storage cs,
+        Mission storage m,
+        uint32 clanId,
+        uint64 tick,
+        ActionType action
+    ) internal {
+        // Must be at homebase
+        if (cs.currentRegion != clan.baseRegion) {
+            _completeMission(cs, m);
+            return;
+        }
+
+        bool success = false;
+        if (action == ActionType.BuildWall) {
+            success = _tryBuildWall(clan, clanId, tick);
+        } else if (action == ActionType.UpgradeBase) {
+            success = _tryUpgradeBase(clan, clanId, tick);
+        } else if (action == ActionType.UpgradeMonument) {
+            success = _tryUpgradeMonument(clan, clanId, tick);
+        }
+
+        if (!success) {
+            // Resources missing — worker transitions to WAITING
+        }
+        _completeMission(cs, m);
+    }
+
+    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
+        uint8 nextLevel = clan.wallLevel + 1;
+        if (nextLevel > 5) return false;
+
+        uint256 woodCost;
+        uint256 ironCost;
+
+        if (nextLevel == 1) {
+            woodCost = 20e18;
+            ironCost = 0;
+        } else if (nextLevel == 2) {
+            woodCost = 35e18;
+            ironCost = 0;
+        } else if (nextLevel == 3) {
+            woodCost = 30e18;
+            ironCost = 5e18;
+        } else if (nextLevel == 4) {
+            woodCost = 40e18;
+            ironCost = 10e18;
+        } else {
+            woodCost = 50e18;
+            ironCost = 15e18;
+        }
+
+        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;
+
+        clan.vaultWood -= woodCost;
+        clan.vaultIron -= ironCost;
+        uint8 old = clan.wallLevel;
+        clan.wallLevel = nextLevel;
+        emit WallLevelChanged(clanId, old, nextLevel, tick);
+        return true;
+    }
+
+    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
+        uint8 nextLevel = clan.baseLevel + 1;
+        if (nextLevel > 5) return false;
+
+        uint256 woodCost;
+        uint256 ironCost;
+        uint256 wheatCost;
+
+        if (nextLevel == 2) {
+            woodCost = 40e18;
+            ironCost = 0;
+            wheatCost = 20e18;
+        } else if (nextLevel == 3) {
+            woodCost = 60e18;
+            ironCost = 5e18;
+            wheatCost = 30e18;
+        } else if (nextLevel == 4) {
+            woodCost = 80e18;
+            ironCost = 10e18;
+            wheatCost = 40e18;
+        } else {
+            woodCost = 100e18;
+            ironCost = 15e18;
+            wheatCost = 50e18;
+        }
+
+        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost || clan.vaultWheat < wheatCost) return false;
+
+        clan.vaultWood -= woodCost;
+        clan.vaultIron -= ironCost;
+        clan.vaultWheat -= wheatCost;
+        uint8 old = clan.baseLevel;
+        clan.baseLevel = nextLevel;
+        emit BaseLevelChanged(clanId, old, nextLevel, tick);
+        return true;
+    }
+
+    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
+        uint8 nextLevel = clan.monumentLevel + 1;
+        if (nextLevel > 10) return false;
+
+        uint256 woodCost;
+        uint256 wheatCost;
+        uint256 ironCost;
+        uint256 blueprintCost;
+
+        if (nextLevel == 1) {
+            woodCost = 30e18;
+            wheatCost = 20e18;
+        } else if (nextLevel == 2) {
+            woodCost = 50e18;
+            wheatCost = 30e18;
+        } else if (nextLevel == 3) {
+            woodCost = 70e18;
+            wheatCost = 40e18;
+            ironCost = 5e18;
+        } else if (nextLevel == 4) {
+            woodCost = 90e18;
+            wheatCost = 50e18;
+            ironCost = 10e18;
+        } else if (nextLevel == 5) {
+            woodCost = 120e18;
+            wheatCost = 60e18;
+            ironCost = 15e18;
+        } else if (nextLevel == 6) {
+            woodCost = 150e18;
+            wheatCost = 80e18;
+            ironCost = 20e18;
+        } else if (nextLevel <= 10) {
+            woodCost = 200e18;
+            wheatCost = 100e18;
+            ironCost = 25e18;
+            blueprintCost = 1e18;
+        }
+
+        if (
+            clan.vaultWood < woodCost || clan.vaultWheat < wheatCost || clan.vaultIron < ironCost
+                || clan.blueprintBalance < blueprintCost
+        ) return false;
+
+        clan.vaultWood -= woodCost;
+        clan.vaultWheat -= wheatCost;
+        clan.vaultIron -= ironCost;
+        clan.blueprintBalance -= blueprintCost;
+
+        uint8 old = clan.monumentLevel;
+        clan.monumentLevel = nextLevel;
+        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
+        return true;
+    }
+
+    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
+    function _completeMission(Clansman storage cs, Mission storage m) internal {
+        cs.state = ClansmanState.WAITING;
+        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
+        m.active = false;
+        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
+    }
+
+    // -------------------------------------------------------------------------
+    // View-only settlement simulation
+    // -------------------------------------------------------------------------
+
+    function _simulateSettleToTick(uint32 clanId, uint64 toTick)
+        internal
+        view
+        returns (SettlementSimulation memory sim)
+    {
+        sim.clan = _clans[clanId];
+        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL) return sim;
+
+        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
+        sim.clansmen = new Clansman[](clansmanIds.length);
+        sim.missions = new Mission[](clansmanIds.length);
+        for (uint256 i = 0; i < clansmanIds.length; i++) {
+            uint32 clansmanId = clansmanIds[i];
+            sim.clansmen[i] = _clansmen[clansmanId];
+            sim.missions[i] = _missions[clansmanId];
+        }
+        sim.wheatPlots[0] = _wheatPlots[clanId][0];
+        sim.wheatPlots[1] = _wheatPlots[clanId][1];
+
+        uint64 fromTick = sim.clan.lastSettledTick;
+        if (fromTick >= toTick) return sim;
+
+        for (uint64 tick = fromTick; tick < toTick; tick++) {
+            _simulateApplyUpkeep(sim, tick);
+            if (sim.clan.clanState == ClanState.DEAD) break;
+
+            _simulateRegrowWheatPlots(sim, tick);
+
+            for (uint256 i = 0; i < sim.clansmen.length; i++) {
+                _simulateSettleMissionForClansman(sim, i, tick, tick + 1);
+            }
+        }
+
+        sim.clan.lastSettledTick = toTick;
+    }
+
+    function _simulateApplyUpkeep(SettlementSimulation memory sim, uint64 tick) internal view {
+        if (sim.clan.livingClansmen == 0) return;
+
+        uint256 wheatNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
+        uint256 fishNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
+
+        bool hadEnoughWheat = sim.clan.vaultWheat >= wheatNeeded;
+        bool hadEnoughFish = sim.clan.vaultFish >= fishNeeded;
+
+        sim.clan.vaultWheat = hadEnoughWheat ? sim.clan.vaultWheat - wheatNeeded : 0;
+        sim.clan.vaultFish = hadEnoughFish ? sim.clan.vaultFish - fishNeeded : 0;
+
+        bool starving = !hadEnoughWheat || !hadEnoughFish;
+        if (starving && sim.clan.starvationStartsAtTick == 0) {
+            sim.clan.starvationStartsAtTick = tick;
+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
+            sim.clan.starvationStartsAtTick = 0;
+        }
+
+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
+            _simulateKillNextClansmanFromStarvation(sim);
+        }
+    }
+
+    function _simulateKillNextClansmanFromStarvation(SettlementSimulation memory sim) internal pure {
+        if (sim.clan.livingClansmen == 0) return;
+
+        for (uint256 i = 0; i < sim.clansmen.length; i++) {
+            if (sim.clansmen[i].state == ClansmanState.DEAD) continue;
+
+            _simulateMarkClansmanDead(sim, i);
+            if (sim.clan.livingClansmen == 0) {
+                _simulateMarkClanDead(sim);
+            }
+            return;
+        }
+    }
+
+    function _simulateMarkClansmanDead(SettlementSimulation memory sim, uint256 index) internal pure {
+        if (sim.clansmen[index].state == ClansmanState.DEAD) return;
+
+        sim.clansmen[index].state = ClansmanState.DEAD;
+        sim.clansmen[index].cooldownEndsAtTs = 0;
+        if (sim.clan.livingClansmen > 0) {
+            sim.clan.livingClansmen--;
+        }
+        if (sim.missions[index].active) {
+            sim.missions[index].active = false;
+        }
+    }
+
+    function _simulateMarkClanDead(SettlementSimulation memory sim) internal pure {
+        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
+
+        sim.clan.clanState = ClanState.DEAD;
+        sim.clan.vaultWood = 0;
+        sim.clan.vaultWheat = 0;
+        sim.clan.vaultFish = 0;
+        sim.clan.vaultIron = 0;
+        sim.clan.starvationStartsAtTick = 0;
+        sim.clan.livingClansmen = 0;
+
+        for (uint256 i = 0; i < sim.clansmen.length; i++) {
+            sim.clansmen[i].state = ClansmanState.DEAD;
+            sim.clansmen[i].cooldownEndsAtTs = 0;
+            if (sim.missions[i].active) {
+                sim.missions[i].active = false;
+            }
+        }
+    }
+
+    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
+        for (uint256 pi = 0; pi < 2; pi++) {
+            WheatPlot memory plot = sim.wheatPlots[pi];
+            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
+                plot.state = WheatPlotState.Harvestable;
+                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
+                plot.regrowUntilTick = 0;
+                sim.wheatPlots[pi] = plot;
+            }
+        }
+    }
+
+    function _simulateSettleMissionForClansman(
+        SettlementSimulation memory sim,
+        uint256 index,
+        uint64 fromTick,
+        uint64 toTick
+    ) internal view {
+        Clansman memory cs = sim.clansmen[index];
+        Mission memory m = sim.missions[index];
+
+        if (cs.state == ClansmanState.DEAD) {
+            if (m.active) {
+                m.active = false;
+            }
+            sim.missions[index] = m;
+            return;
+        }
+
+        if (!m.active) return;
+
+        for (uint64 tick = fromTick; tick < toTick; tick++) {
+            bytes32 tickSeed = _tickSeeds[tick];
+
+            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
+                cs.state = ClansmanState.ACTING;
+                cs.currentRegion = m.targetRegion;
+            }
+
+            if (m.action == ActionType.DefendBase) continue;
+
+            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
+                (cs, m) = _simulateResolveAction(sim, cs, m, tick, tickSeed);
+                if (m.active && getActionDuration(m.action) > 0) {
+                    (cs, m) = _simulateCompleteMission(cs, m);
+                }
+            }
+
+            if (!m.active) break;
+        }
+
+        sim.clansmen[index] = cs;
+        sim.missions[index] = m;
+    }
+
+    function _simulateResolveAction(
+        SettlementSimulation memory sim,
+        Clansman memory cs,
+        Mission memory m,
+        uint64 tick,
+        bytes32 tickSeed
+    ) internal view returns (Clansman memory, Mission memory) {
+        bool starving =
+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
+        ActionType action = m.action;
+
+        if (action == ActionType.ChopWood) {
+            (cs, m) = _simulateGatherWood(cs, m, tick, starving, tickSeed);
+        } else if (action == ActionType.MineIron) {
+            (cs, m) = _simulateGatherIron(sim, cs, m, tick, starving, tickSeed);
+        } else if (action == ActionType.FishDocks) {
+            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DOCKS_BPS);
+        } else if (action == ActionType.FishDeepSea) {
+            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DEEP_BPS);
+        } else if (action == ActionType.HarvestWheat) {
+            (cs, m) = _simulateGatherWheat(sim, cs, m, tick, starving);
+        } else if (action == ActionType.DepositResources) {
+            (cs, m) = _simulateDoDeposit(sim, cs, m);
+        } else if (
+            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
+        ) {
+            (cs, m) = _simulateDoBuilding(sim, cs, m, action);
+        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
+            (cs, m) = _simulateCompleteMission(cs, m);
+        }
+
+        return (cs, m);
+    }
+
+    function _simulateGatherWood(Clansman memory cs, Mission memory m, uint64 tick, bool starving, bytes32 tickSeed)
+        internal
+        view
+        returns (Clansman memory, Mission memory)
+    {
+        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
+        if (remaining == 0) return _simulateCompleteMission(cs, m);
+
+        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
+        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
+        if (uint256(critRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
+            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
+        }
+        if (starving) yield = yield / 2;
+        if (yield > remaining) yield = remaining;
+        cs.carryWood += yield;
+
+        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
+            return _simulateCompleteMission(cs, m);
+        }
+        return (cs, m);
+    }
+
+    function _simulateGatherIron(
+        SettlementSimulation memory sim,
+        Clansman memory cs,
+        Mission memory m,
+        uint64 tick,
+        bool starving,
+        bytes32 tickSeed
+    ) internal view returns (Clansman memory, Mission memory) {
+        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
+        if (remaining == 0) return _simulateCompleteMission(cs, m);
+
+        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
+        if (starving) yield = yield / 2;
+        if (yield > remaining) yield = remaining;
+        cs.carryIron += yield;
+
+        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, cs.clansmanId, m.nonce, tick));
+        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
+            sim.clan.goldBalance += ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
+        }
+
+        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
+            return _simulateCompleteMission(cs, m);
+        }
+        return (cs, m);
+    }
+
+    function _simulateGatherFish(
+        Clansman memory cs,
+        Mission memory m,
+        uint64 tick,
+        bool starving,
+        bytes32 tickSeed,
+        uint256 successBps
+    ) internal view returns (Clansman memory, Mission memory) {
+        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
+        if (remaining == 0) return _simulateCompleteMission(cs, m);
+
+        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
+        uint256 yield = uint256(fishRng) % 10000 < successBps ? RESOURCE_UNIT : 0;
+        if (starving) yield = yield / 2;
+        if (yield > remaining) yield = remaining;
+        if (yield > 0) {
+            cs.carryFish += yield;
+        }
+
+        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
+            return _simulateCompleteMission(cs, m);
+        }
+        return (cs, m);
+    }
+
+    function _simulateGatherWheat(
+        SettlementSimulation memory sim,
+        Clansman memory cs,
+        Mission memory m,
+        uint64 tick,
+        bool starving
+    ) internal view returns (Clansman memory, Mission memory) {
+        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
+        if (remaining == 0) return _simulateCompleteMission(cs, m);
+
+        uint256 plotIdx;
+        if (m.targetRegion == ClanWorldConstants.REGION_WEST_FARMS) {
+            plotIdx = 0;
+        } else if (m.targetRegion == ClanWorldConstants.REGION_EAST_FARMS) {
+            plotIdx = 1;
+        } else {
+            return _simulateCompleteMission(cs, m);
+        }
+
+        WheatPlot memory plot = sim.wheatPlots[plotIdx];
+        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
+            return _simulateCompleteMission(cs, m);
+        }
+
+        uint256 yield = WHEAT_HARVEST_RATE;
+        if (starving) yield = yield / 2;
+        if (yield > remaining) yield = remaining;
+        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
+
+        cs.carryWheat += yield;
+        plot.remainingWheat -= yield;
+
+        if (plot.remainingWheat == 0) {
+            plot.state = WheatPlotState.Regrowing;
+            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
+        }
+        sim.wheatPlots[plotIdx] = plot;
+
+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
+            return _simulateCompleteMission(cs, m);
+        }
+        return (cs, m);
+    }
+
+    function _simulateDoDeposit(SettlementSimulation memory sim, Clansman memory cs, Mission memory m)
+        internal
+        view
+        returns (Clansman memory, Mission memory)
+    {
+        if (cs.currentRegion != sim.clan.baseRegion) return _simulateCompleteMission(cs, m);
+
+        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
+        if (!hasAnything) return _simulateCompleteMission(cs, m);
+
+        sim.clan.vaultWood += cs.carryWood;
+        sim.clan.vaultIron += cs.carryIron;
+        sim.clan.vaultWheat += cs.carryWheat;
+        sim.clan.vaultFish += cs.carryFish;
+
+        cs.carryWood = 0;
+        cs.carryIron = 0;
+        cs.carryWheat = 0;
+        cs.carryFish = 0;
+
+        return _simulateCompleteMission(cs, m);
+    }
+
+    function _simulateDoBuilding(
+        SettlementSimulation memory sim,
+        Clansman memory cs,
+        Mission memory m,
+        ActionType action
+    ) internal view returns (Clansman memory, Mission memory) {
+        if (cs.currentRegion == sim.clan.baseRegion) {
+            if (action == ActionType.BuildWall) {
+                _simulateTryBuildWall(sim);
+            } else if (action == ActionType.UpgradeBase) {
+                _simulateTryUpgradeBase(sim);
+            } else if (action == ActionType.UpgradeMonument) {
+                _simulateTryUpgradeMonument(sim);
+            }
+        }
+        return _simulateCompleteMission(cs, m);
+    }
+
+    function _simulateTryBuildWall(SettlementSimulation memory sim) internal pure {
+        uint8 nextLevel = sim.clan.wallLevel + 1;
+        if (nextLevel > 5) return;
+
+        uint256 woodCost;
+        uint256 ironCost;
+        if (nextLevel == 1) {
+            woodCost = 20e18;
+        } else if (nextLevel == 2) {
+            woodCost = 35e18;
+        } else if (nextLevel == 3) {
+            woodCost = 30e18;
+            ironCost = 5e18;
+        } else if (nextLevel == 4) {
+            woodCost = 40e18;
+            ironCost = 10e18;
+        } else {
+            woodCost = 50e18;
+            ironCost = 15e18;
+        }
+
+        if (sim.clan.vaultWood < woodCost || sim.clan.vaultIron < ironCost) return;
+        sim.clan.vaultWood -= woodCost;
+        sim.clan.vaultIron -= ironCost;
+        sim.clan.wallLevel = nextLevel;
+    }
+
+    function _simulateTryUpgradeBase(SettlementSimulation memory sim) internal pure {
+        uint8 nextLevel = sim.clan.baseLevel + 1;
+        if (nextLevel > 5) return;
+
+        uint256 woodCost;
+        uint256 ironCost;
+        uint256 wheatCost;
+        if (nextLevel == 2) {
+            woodCost = 40e18;
+            wheatCost = 20e18;
+        } else if (nextLevel == 3) {
+            woodCost = 60e18;
+            ironCost = 5e18;
+            wheatCost = 30e18;
+        } else if (nextLevel == 4) {
+            woodCost = 80e18;
+            ironCost = 10e18;
+            wheatCost = 40e18;
+        } else {
+            woodCost = 100e18;
+            ironCost = 15e18;
+            wheatCost = 50e18;
+        }
+
+        if (sim.clan.vaultWood < woodCost || sim.clan.vaultIron < ironCost || sim.clan.vaultWheat < wheatCost) {
+            return;
+        }
+        sim.clan.vaultWood -= woodCost;
+        sim.clan.vaultIron -= ironCost;
+        sim.clan.vaultWheat -= wheatCost;
+        sim.clan.baseLevel = nextLevel;
+    }
+
+    function _simulateTryUpgradeMonument(SettlementSimulation memory sim) internal pure {
+        uint8 nextLevel = sim.clan.monumentLevel + 1;
+        if (nextLevel > 10) return;
+
+        uint256 woodCost;
+        uint256 wheatCost;
+        uint256 ironCost;
+        uint256 blueprintCost;
+        if (nextLevel == 1) {
+            woodCost = 30e18;
+            wheatCost = 20e18;
+        } else if (nextLevel == 2) {
+            woodCost = 50e18;
+            wheatCost = 30e18;
+        } else if (nextLevel == 3) {
+            woodCost = 70e18;
+            wheatCost = 40e18;
+            ironCost = 5e18;
+        } else if (nextLevel == 4) {
+            woodCost = 90e18;
+            wheatCost = 50e18;
+            ironCost = 10e18;
+        } else if (nextLevel == 5) {
+            woodCost = 120e18;
+            wheatCost = 60e18;
+            ironCost = 15e18;
+        } else if (nextLevel == 6) {
+            woodCost = 150e18;
+            wheatCost = 80e18;
+            ironCost = 20e18;
+        } else {
+            woodCost = 200e18;
+            wheatCost = 100e18;
+            ironCost = 25e18;
+            blueprintCost = 1e18;
+        }
+
+        if (
+            sim.clan.vaultWood < woodCost || sim.clan.vaultWheat < wheatCost || sim.clan.vaultIron < ironCost
+                || sim.clan.blueprintBalance < blueprintCost
+        ) return;
+
+        sim.clan.vaultWood -= woodCost;
+        sim.clan.vaultWheat -= wheatCost;
+        sim.clan.vaultIron -= ironCost;
+        sim.clan.blueprintBalance -= blueprintCost;
+        sim.clan.monumentLevel = nextLevel;
+    }
+
+    function _simulateCompleteMission(Clansman memory cs, Mission memory m)
+        internal
+        view
+        returns (Clansman memory, Mission memory)
+    {
+        cs.state = ClansmanState.WAITING;
+        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
+        m.active = false;
+        return (cs, m);
+    }
+
+    // =========================================================================
+    // BANDIT STATE MACHINE
+    // =========================================================================
+
+    function _spawnBandit(uint8 region, uint32 strength) internal returns (uint32 id) {
+        require(
+            region >= ClanWorldConstants.REGION_FOREST && region <= ClanWorldConstants.REGION_DEEP_SEA,
+            "ClanWorld: invalid bandit region"
+        );
+        require(strength > 0, "ClanWorld: invalid bandit strength");
+
+        id = _nextBanditId++;
+        _bandits[id] = BanditTroop({
+            id: id,
+            region: region,
+            state: BanditState.Spawned,
+            targetClanId: 0,
+            tickEnteredState: _world.currentTick,
+            strength: strength,
+            carryWood: 0,
+            carryIron: 0,
+            carryWheat: 0,
+            carryFish: 0,
+            carryGold: 0
+        });
+        _banditsByRegion[region].push(id);
+        _activeBanditCount += 1;
+
+        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
+        spawnState.lastSpawnTick = _world.currentTick;
+        spawnState.probabilityAccum = 0;
+
+        if (_world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
+            _world.activeBanditId = id;
+        }
+
+        emit BanditSpawned(id, region, 0, _banditStrengthForLegacyEvent(strength));
+    }
+
+    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
+        require(targetClanId != ClanWorldConstants.CLAN_ID_NULL, "ClanWorld: invalid bandit target");
+        _bandits[id].targetClanId = targetClanId;
+        _transitionBanditState(id, BanditState.Attacking);
+    }
+
+    function _transitionBanditState(uint32 id, BanditState newState) internal {
+        BanditTroop storage bandit = _bandits[id];
+        require(bandit.id != ClanWorldConstants.BANDIT_ID_NULL, "ClanWorld: bandit not found");
+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
+
+        BanditState oldState = bandit.state;
+        require(_isValidBanditTransition(bandit, newState), "ClanWorld: invalid bandit transition");
+
+        if (newState == BanditState.Defeated) {
+            emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
+            _deleteBandit(id);
+            return;
+        }
+
+        bandit.state = newState;
+        bandit.tickEnteredState = _world.currentTick;
+        if (newState != BanditState.Attacking) {
+            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
+        }
+
+        emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
+    }
+
+    function _isValidBanditTransition(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
+        return false;
+    }
+
+    function _canBanditLeaveSpawned(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
+        return newState == BanditState.Escaped
+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
+    }
+
+    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
+        return newState == BanditState.Escaped
+            || (newState == BanditState.Attacking
+                && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
+                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS);
+    }
+
+    function _canBanditLeaveAttacking(BanditState newState) internal pure returns (bool) {
+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
+    }
+
+    function _canBanditLeaveEscaped(BanditState newState) internal pure returns (bool) {
+        return newState == BanditState.Resting;
+    }
+
+    function _canBanditLeaveResting(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
+        return newState == BanditState.Escaped
+            || (newState == BanditState.Camped
+                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS);
+    }
+
+    function _deleteBandit(uint32 id) internal {
+        BanditTroop storage bandit = _bandits[id];
+        uint8 region = bandit.region;
+        uint32[] storage regionBandits = _banditsByRegion[region];
+        for (uint256 i = 0; i < regionBandits.length; i++) {
+            if (regionBandits[i] == id) {
+                regionBandits[i] = regionBandits[regionBandits.length - 1];
+                regionBandits.pop();
+                break;
+            }
+        }
+
+        delete _bandits[id];
+        if (_activeBanditCount > 0) {
+            _activeBanditCount -= 1;
+        }
+        if (_world.activeBanditId == id) {
+            _world.activeBanditId = _findOldestActiveBandit();
+        }
+    }
+
+    function _findOldestActiveBandit() internal view returns (uint32 oldestBanditId) {
+        // V1 caps live troops at MAX_TOTAL_BANDITS = 8, so scanning the region
+        // indexes is bounded even though storage mappings cannot be enumerated.
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            uint32[] storage regionBandits = _banditsByRegion[region];
+            for (uint256 i = 0; i < regionBandits.length; i++) {
+                uint32 candidateId = regionBandits[i];
+                BanditTroop storage candidate = _bandits[candidateId];
+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
+                    continue;
+                }
+                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
+                    oldestBanditId = candidateId;
+                }
+            }
+        }
+    }
+
+    function _advanceBanditStates(uint64 closedTick) internal {
+        require(_world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            uint32[] storage regionBandits = _banditsByRegion[region];
+            for (uint256 i = 0; i < regionBandits.length; i++) {
+                uint32 banditId = regionBandits[i];
+                BanditTroop storage bandit = _bandits[banditId];
+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
+                    _transitionBanditState(banditId, BanditState.Camped);
+                } else if (
+                    bandit.state == BanditState.Camped
+                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS
+                ) {
+                    uint32 targetClanId = _pickBanditAttackTarget(bandit);
+                    if (targetClanId == ClanWorldConstants.CLAN_ID_NULL) {
+                        _transitionBanditState(banditId, BanditState.Escaped);
+                        emit BanditEscaped(banditId, closedTick);
+                    } else {
+                        _transitionBanditToAttacking(banditId, targetClanId);
+                    }
+                } else if (
+                    bandit.state == BanditState.Escaped
+                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
+                ) {
+                    _transitionBanditState(banditId, BanditState.Resting);
+                } else if (
+                    bandit.state == BanditState.Resting
+                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
+                ) {
+                    _transitionBanditState(banditId, BanditState.Camped);
+                }
+            }
+        }
+    }
+
+    function _pickBanditAttackTarget(BanditTroop storage bandit) internal view returns (uint32 targetClanId) {
+        uint32[MAX_CLANS] memory tiedClanIds;
+        uint256 tiedCount;
+        uint256 bestLootValue;
+
+        for (uint256 i = 0; i < _allClanIds.length; i++) {
+            uint32 clanId = _allClanIds[i];
+            Clan storage clan = _clans[clanId];
+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
+                continue;
+            }
+
+            uint256 lootValue = _lootValueRaw(clan);
+            if (tiedCount == 0 || lootValue > bestLootValue) {
+                bestLootValue = lootValue;
+                tiedClanIds[0] = clanId;
+                tiedCount = 1;
+            } else if (lootValue == bestLootValue) {
+                tiedClanIds[tiedCount] = clanId;
+                tiedCount++;
+            }
+        }
+
+        if (tiedCount == 0) {
+            return ClanWorldConstants.CLAN_ID_NULL;
+        }
+        if (tiedCount == 1) {
+            return tiedClanIds[0];
+        }
+
+        uint256 selected = RNG.rngBounded(_world.currentTickSeed, DOMAIN_BANDIT_TARGET_PICK, bandit.id, tiedCount);
+        return tiedClanIds[selected];
+    }
+
+    function _banditStrengthForLegacyEvent(uint32 strength) internal pure returns (uint16) {
+        if (strength > type(uint16).max) return type(uint16).max;
+        // forge-lint: disable-next-line(unsafe-typecast)
+        return uint16(strength);
+    }
+
+    function _resolveAttackingBandits(uint64 closedTick) internal {
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            uint32[] storage regionBandits = _banditsByRegion[region];
+            uint256 i = 0;
+            while (i < regionBandits.length) {
+                uint32 banditId = regionBandits[i];
+                BanditTroop storage bandit = _bandits[banditId];
+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
+                if (shouldResolve) {
+                    _resolveBanditAttack(banditId, closedTick);
+                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
+                        continue;
+                    }
+                }
+                i++;
+            }
+        }
+    }
+
+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
+        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");
+
+        BanditTroop storage bandit = _bandits[banditId];
+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
+            return;
+        }
+        if (bandit.tickEnteredState != closedTick) {
+            return;
+        }
+
+        uint32 targetClanId = bandit.targetClanId;
+        Clan storage targetClan = _clans[targetClanId];
+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
+            _transitionBanditState(banditId, BanditState.Escaped);
+            emit BanditEscaped(banditId, closedTick);
+            return;
+        }
+
+        _settleClan(targetClanId);
+        if (bandit.state != BanditState.Attacking || bandit.targetClanId != targetClanId) {
+            return;
+        }
+        if (targetClan.clanState == ClanState.DEAD) {
+            _transitionBanditState(banditId, BanditState.Escaped);
+            emit BanditEscaped(banditId, closedTick);
+            return;
+        }
+
+        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
+        if (
+            bandit.state != BanditState.Attacking || bandit.targetClanId != targetClanId
+                || targetClan.clanState == ClanState.DEAD
+        ) {
+            return;
+        }
+
+        bytes32 tickSeed = _world.currentTickSeed;
+        uint32 banditAttackPower = bandit.strength;
+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
+        bool defeated = uint256(totalClansmanDefense) >= uint256(banditAttackPower) * 2;
+
+        uint32 wallDamage;
+        uint32 baseAbsorbed;
+        uint32 clansmanDamageAbsorbed;
+        if (!defeated) {
+            uint32 incomingDamage =
+                banditAttackPower > totalClansmanDefense ? banditAttackPower - totalClansmanDefense : 0;
+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
+            (incomingDamage, baseAbsorbed) = _applyBanditBaseDefense(targetClan, incomingDamage);
+            clansmanDamageAbsorbed =
+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
+        }
+
+        uint32 totalDefense = totalClansmanDefense + wallDamage + baseAbsorbed + clansmanDamageAbsorbed;
+        emit BanditAttackResolved(
+            banditId,
+            targetClanId,
+            defeated,
+            _uint16Clamp(banditAttackPower),
+            _uint16Clamp(totalDefense),
+            targetClan.wallLevel,
+            0,
+            0,
+            0,
+            0,
+            closedTick
+        );
+
+        if (defeated) {
+            emit BanditDefeated(banditId, targetClanId, closedTick);
+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
+            targetClan.blueprintBalance += BLUEPRINT_UNIT;
+            emit BlueprintEarned(targetClanId, banditId, BLUEPRINT_UNIT, closedTick);
+            _transitionBanditState(banditId, BanditState.Defeated);
+        } else {
+            _transitionBanditState(banditId, BanditState.Escaped);
+            emit BanditEscaped(banditId, closedTick);
+        }
+    }
+
+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
+        BanditTroop storage bandit = _bandits[banditId];
+        uint32[] memory rewardedClanIds = _activeDefendingClanIds(targetClanId);
+        uint256 nDefendingClans = rewardedClanIds.length;
+
+        uint256 perWood;
+        uint256 perIron;
+        uint256 perWheat;
+        uint256 perFish;
+        uint256 perGold;
+        if (nDefendingClans > 0) {
+            perWood = _perClanBanditLootShare(bandit.carryWood, nDefendingClans);
+            perIron = _perClanBanditLootShare(bandit.carryIron, nDefendingClans);
+            perWheat = _perClanBanditLootShare(bandit.carryWheat, nDefendingClans);
+            perFish = _perClanBanditLootShare(bandit.carryFish, nDefendingClans);
+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
+
+            for (uint256 i = 0; i < rewardedClanIds.length; i++) {
+                Clan storage defenderClan = _clans[rewardedClanIds[i]];
+                defenderClan.vaultWood += perWood;
+                defenderClan.vaultIron += perIron;
+                defenderClan.vaultWheat += perWheat;
+                defenderClan.vaultFish += perFish;
+                defenderClan.goldBalance += perGold;
+            }
+        }
+
+        emit LootDistributed(
+            banditId,
+            rewardedClanIds,
+            perWood,
+            perWheat,
+            perFish,
+            perIron,
+            perGold,
+            bandit.carryWood - (perWood * nDefendingClans),
+            bandit.carryWheat - (perWheat * nDefendingClans),
+            bandit.carryFish - (perFish * nDefendingClans),
+            bandit.carryIron - (perIron * nDefendingClans),
+            bandit.carryGold - (perGold * nDefendingClans)
+        );
+    }
+
+    function _perClanBanditLootShare(uint256 loot, uint256 nDefendingClans) internal pure returns (uint256) {
+        if (nDefendingClans == 1) {
+            return loot;
+        }
+        return ((loot / RESOURCE_UNIT) / nDefendingClans) * RESOURCE_UNIT;
+    }
+
+    function _activeDefendingClanIds(uint32 targetClanId) internal view returns (uint32[] memory clanIds) {
+        uint8 targetRegion = _clans[targetClanId].baseRegion;
+        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
+        uint256 count;
+
+        for (uint256 i = 0; i < defendingClans.length; i++) {
+            if (_clanHasActiveDefenderForTarget(defendingClans[i], targetClanId, targetRegion)) {
+                count++;
+            }
+        }
+
+        clanIds = new uint32[](count);
+        uint256 out;
+        for (uint256 i = 0; i < defendingClans.length; i++) {
+            uint32 defenderClanId = defendingClans[i];
+            if (_clanHasActiveDefenderForTarget(defenderClanId, targetClanId, targetRegion)) {
+                clanIds[out++] = defenderClanId;
+            }
+        }
+    }
+
+    function _clanHasActiveDefenderForTarget(uint32 defenderClanId, uint32 targetClanId, uint8 targetRegion)
+        internal
+        view
+        returns (bool)
+    {
+        Clan storage defenderClan = _clans[defenderClanId];
+        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
+            return false;
+        }
+
+        uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
+        for (uint256 i = 0; i < clansmanIds.length; i++) {
+            uint32 clansmanId = clansmanIds[i];
+            Clansman storage cs = _clansmen[clansmanId];
+            Mission storage mission = _missions[clansmanId];
+            if (
+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
+                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
+            ) {
+                return true;
+            }
+        }
+        return false;
+    }
+
+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
+        internal
+        view
+        returns (uint32 totalDefense)
+    {
+        uint8 targetRegion = _clans[targetClanId].baseRegion;
+        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
+
+        for (uint256 i = 0; i < defendingClans.length; i++) {
+            uint32 defenderClanId = defendingClans[i];
+            Clan storage defenderClan = _clans[defenderClanId];
+            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
+                continue;
+            }
+
+            uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
+            for (uint256 j = 0; j < clansmanIds.length; j++) {
+                uint32 clansmanId = clansmanIds[j];
+                Clansman storage cs = _clansmen[clansmanId];
+                Mission storage mission = _missions[clansmanId];
+                if (
+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
+                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
+                ) {
+                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
+                }
+            }
+        }
+    }
+
+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
+        internal
+        returns (uint32 remainingDamage, uint32 wallDamage)
+    {
+        remainingDamage = incomingDamage;
+        if (remainingDamage == 0 || clan.wallLevel == 0) {
+            return (remainingDamage, 0);
+        }
+
+        wallDamage = remainingDamage < WALL_HP_PER_LEVEL ? remainingDamage : WALL_HP_PER_LEVEL;
+        remainingDamage -= wallDamage;
+        if (wallDamage >= WALL_HP_PER_LEVEL) {
+            if (clan.wallLevel > 0) {
+                clan.wallLevel--;
+            }
+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
+        }
+    }
+
+    function _applyBanditBaseDefense(Clan storage clan, uint32 incomingDamage)
+        internal
+        view
+        returns (uint32 remainingDamage, uint32 baseAbsorbed)
+    {
+        remainingDamage = incomingDamage;
+        if (remainingDamage == 0 || clan.baseLevel == 0) {
+            return (remainingDamage, 0);
+        }
+
+        uint32 baseDefense = uint32(clan.baseLevel) * BASE_HP_PER_LEVEL;
+        baseAbsorbed = remainingDamage < baseDefense ? remainingDamage : baseDefense;
+        remainingDamage -= baseAbsorbed;
+    }
+
+    function _applyBanditClansmanCasualties(
+        Clan storage clan,
+        uint32 clanId,
+        uint32 banditId,
+        uint32 incomingDamage,
+        bytes32 tickSeed
+    ) internal returns (uint32 damageAbsorbed) {
+        uint32 remainingDamage = incomingDamage;
+        uint32 killIndex = 0;
+        while (remainingDamage > 0) {
+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
+            if (victimId == 0) {
+                break;
+            }
+
+            Clansman storage victim = _clansmen[victimId];
+            _markClansmanDead(clan, victim);
+
+            uint32 absorbed = remainingDamage < CLANSMAN_HP ? remainingDamage : CLANSMAN_HP;
+            damageAbsorbed += absorbed;
+            remainingDamage -= absorbed;
+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
+            killIndex++;
+
+            if (clan.livingClansmen == 0) {
+                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
+                break;
+            }
+        }
+    }
+
+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
+        internal
+        view
+        returns (uint32 victimId)
+    {
+        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
+        uint256 livingCount;
+        for (uint256 i = 0; i < clansmanIds.length; i++) {
+            if (_clansmen[clansmanIds[i]].state != ClansmanState.DEAD) {
+                livingCount++;
+            }
+        }
+        if (livingCount == 0) {
+            return 0;
+        }
+
+        uint256 pick =
+            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
+        uint256 seen;
+        for (uint256 i = 0; i < clansmanIds.length; i++) {
+            uint32 candidateId = clansmanIds[i];
+            if (_clansmen[candidateId].state == ClansmanState.DEAD) {
+                continue;
+            }
+            if (seen == pick) {
+                return candidateId;
+            }
+            seen++;
+        }
+    }
+
+    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)
+        internal
+        pure
+        returns (uint32)
+    {
+        return uint32(
+            uint256(keccak256(abi.encode("clansman_defense", tickSeed, banditId, clansmanId)))
+                % (CLANSMAN_MAX_DEFENSE_DAMAGE + 1)
+        );
+    }
+
+    function _uint16Clamp(uint32 value) internal pure returns (uint16) {
+        if (value > type(uint16).max) return type(uint16).max;
+        return uint16(value);
+    }
+
+    function _evaluateBanditSpawns(bytes32 tickSeed) internal {
+        uint256[] memory regionWeights = _banditSpawnRegionWeights();
+        if (_activeBanditCount >= MAX_TOTAL_BANDITS) {
+            _refreshBanditSpawnWorldPreview(regionWeights);
+            return;
+        }
+
+        uint256[] memory candidateWeights = new uint256[](8);
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            uint256 weight = regionWeights[region - 1];
+            if (weight == 0 || _banditsByRegion[region].length >= MAX_BANDITS_PER_REGION) {
+                continue;
+            }
+
+            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
+            if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
+                continue;
+            }
+
+            spawnState.probabilityAccum = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
+            if (_banditSpawnRollPasses(tickSeed, region, spawnState.probabilityAccum)) {
+                candidateWeights[region - 1] = weight;
+            }
+        }
 
-        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
-        _completeMission(cs, m);
+        uint8 selectedRegion = _selectBanditSpawnRegion(tickSeed, candidateWeights);
+        if (selectedRegion != ClanWorldConstants.REGION_NOOP) {
+            // _spawnBandit resets only the selected region's accumulator; other
+            // eligible regions retain their accumulated pressure for later ticks.
+            _spawnBandit(selectedRegion, _banditSpawnStrength(tickSeed, selectedRegion));
+        }
+
+        _refreshBanditSpawnWorldPreview(regionWeights);
     }
 
-    function _doBuilding(
-        Clan storage clan,
-        Clansman storage cs,
-        Mission storage m,
-        uint32 clanId,
-        uint64 tick,
-        ActionType action
-    ) internal {
-        // Must be at homebase
-        if (cs.currentRegion != clan.baseRegion) {
-            _completeMission(cs, m);
-            return;
+    function _incrementBanditSpawnProbability(uint16 probabilityAccum) internal pure returns (uint16) {
+        uint256 next = uint256(probabilityAccum) + BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS;
+        if (next > BANDIT_SPAWN_MAX_PROBABILITY_BPS) {
+            return BANDIT_SPAWN_MAX_PROBABILITY_BPS;
         }
+        // forge-lint: disable-next-line(unsafe-typecast)
+        return uint16(next);
+    }
 
-        bool success = false;
-        if (action == ActionType.BuildWall) {
-            success = _tryBuildWall(clan, clanId, tick);
-        } else if (action == ActionType.UpgradeBase) {
-            success = _tryUpgradeBase(clan, clanId, tick);
-        } else if (action == ActionType.UpgradeMonument) {
-            success = _tryUpgradeMonument(clan, clanId, tick);
-        }
+    function _banditSpawnRollPasses(bytes32 tickSeed, uint8 region, uint16 probabilityAccum)
+        internal
+        pure
+        returns (bool)
+    {
+        return _banditSpawnRoll(tickSeed, region) < uint256(probabilityAccum);
+    }
 
-        if (!success) {
-            // Resources missing — worker transitions to WAITING
+    function _banditSpawnRoll(bytes32 tickSeed, uint8 region) internal pure returns (uint256) {
+        uint256 nonce = uint256(keccak256(abi.encodePacked("bandit_spawn", region)));
+        return RNG.rngBounded(tickSeed, DOMAIN_BANDIT_SPAWN, nonce, 10000);
+    }
+
+    function _selectBanditSpawnRegion(bytes32 tickSeed, uint256[] memory weights) internal pure returns (uint8) {
+        uint256 selected = RNG.rngWeightedPick(
+            tickSeed, DOMAIN_BANDIT_SPAWN, uint256(keccak256(abi.encodePacked("bandit_spawn_region"))), weights
+        );
+        if (weights.length == 0 || weights[selected] == 0) {
+            return ClanWorldConstants.REGION_NOOP;
         }
-        _completeMission(cs, m);
+        // forge-lint: disable-next-line(unsafe-typecast)
+        return uint8(selected + 1);
     }
 
-    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
-        uint8 nextLevel = clan.wallLevel + 1;
-        if (nextLevel > 5) return false;
+    function _banditSpawnStrength(bytes32 tickSeed, uint8 region) internal pure returns (uint32) {
+        uint256 nonce = uint256(keccak256(abi.encodePacked("bandit_spawn_strength", region)));
+        uint256 roll = RNG.rngBounded(tickSeed, DOMAIN_BANDIT_SPAWN, nonce, BANDIT_SPAWN_STRENGTH_SPREAD);
+        // forge-lint: disable-next-line(unsafe-typecast)
+        return MIN_BANDIT_SPAWN_STRENGTH + uint32(roll);
+    }
 
-        uint256 woodCost;
-        uint256 ironCost;
+    function _eagerSettleForBandits(uint64 closedTick) internal {
+        require(_world.currentTick == closedTick, "ClanWorld: eager settle tick mismatch");
+        if (_activeBanditCount >= MAX_TOTAL_BANDITS) return;
 
-        if (nextLevel == 1) {
-            woodCost = 20e18;
-            ironCost = 0;
-        } else if (nextLevel == 2) {
-            woodCost = 35e18;
-            ironCost = 0;
-        } else if (nextLevel == 3) {
-            woodCost = 30e18;
-            ironCost = 5e18;
-        } else if (nextLevel == 4) {
-            woodCost = 40e18;
-            ironCost = 10e18;
-        } else {
-            woodCost = 50e18;
-            ironCost = 15e18;
+        uint256[] memory regionWeights = _banditSpawnRegionWeights();
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            if (!_isBanditSpawnRegionCandidate(regionWeights, region)) {
+                continue;
+            }
+            _eagerSettleBanditCandidateRegion(region);
         }
+    }
 
-        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;
+    function _isBanditSpawnRegionCandidate(uint256[] memory regionWeights, uint8 region) internal view returns (bool) {
+        if (regionWeights[region - 1] == 0 || _banditsByRegion[region].length >= MAX_BANDITS_PER_REGION) {
+            return false;
+        }
 
-        clan.vaultWood -= woodCost;
-        clan.vaultIron -= ironCost;
-        uint8 old = clan.wallLevel;
-        clan.wallLevel = nextLevel;
-        emit WallLevelChanged(clanId, old, nextLevel, tick);
-        return true;
+        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
+        if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
+            return false;
+        }
+
+        uint16 nextProbability = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
+        return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
     }
 
-    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
-        uint8 nextLevel = clan.baseLevel + 1;
-        if (nextLevel > 5) return false;
+    function _eagerSettleBanditCandidateRegion(uint8 region) internal {
+        uint256 clanScanCount = _allClanIds.length < MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION
+            ? _allClanIds.length
+            : MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION;
 
-        uint256 woodCost;
-        uint256 ironCost;
-        uint256 wheatCost;
+        for (uint256 i = 0; i < clanScanCount; i++) {
+            uint32 clanId = _allClanIds[i];
+            Clan storage clan = _clans[clanId];
+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
+                continue;
+            }
 
-        if (nextLevel == 2) {
-            woodCost = 40e18;
-            ironCost = 0;
-            wheatCost = 20e18;
-        } else if (nextLevel == 3) {
-            woodCost = 60e18;
-            ironCost = 5e18;
-            wheatCost = 30e18;
-        } else if (nextLevel == 4) {
-            woodCost = 80e18;
-            ironCost = 10e18;
-            wheatCost = 40e18;
-        } else {
-            woodCost = 100e18;
-            ironCost = 15e18;
-            wheatCost = 50e18;
+            _settleClan(clanId);
+            _eagerSettleActiveDefendersForBase(clanId, region);
         }
+    }
 
-        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost || clan.vaultWheat < wheatCost) return false;
+    function _eagerSettleActiveDefendersForBase(uint32 targetClanId, uint8 region) internal {
+        uint32[] storage defendingClans = _defendingClansByRegion[region];
+        uint256 defendingClanScanCount = defendingClans.length < MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION
+            ? defendingClans.length
+            : MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION;
+        uint256 defendersScanned;
 
-        clan.vaultWood -= woodCost;
-        clan.vaultIron -= ironCost;
-        clan.vaultWheat -= wheatCost;
-        uint8 old = clan.baseLevel;
-        clan.baseLevel = nextLevel;
-        emit BaseLevelChanged(clanId, old, nextLevel, tick);
-        return true;
-    }
+        for (uint256 i = 0; i < defendingClanScanCount; i++) {
+            uint32 defenderClanId = defendingClans[i];
+            uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
 
-    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
-        uint8 nextLevel = clan.monumentLevel + 1;
-        if (nextLevel > 10) return false;
+            for (
+                uint256 j = 0;
+                j < clansmanIds.length && defendersScanned < MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION;
+                j++
+            ) {
+                defendersScanned += 1;
+                Mission storage mission = _missions[clansmanIds[j]];
+                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
+                    _settleClan(defenderClanId);
+                    break;
+                }
+            }
 
-        uint256 woodCost;
-        uint256 wheatCost;
-        uint256 ironCost;
-        uint256 blueprintCost;
+            if (defendersScanned >= MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION) {
+                break;
+            }
+        }
+    }
 
-        if (nextLevel == 1) {
-            woodCost = 30e18;
-            wheatCost = 20e18;
-        } else if (nextLevel == 2) {
-            woodCost = 50e18;
-            wheatCost = 30e18;
-        } else if (nextLevel == 3) {
-            woodCost = 70e18;
-            wheatCost = 40e18;
-            ironCost = 5e18;
-        } else if (nextLevel == 4) {
-            woodCost = 90e18;
-            wheatCost = 50e18;
-            ironCost = 10e18;
-        } else if (nextLevel == 5) {
-            woodCost = 120e18;
-            wheatCost = 60e18;
-            ironCost = 15e18;
-        } else if (nextLevel == 6) {
-            woodCost = 150e18;
-            wheatCost = 80e18;
-            ironCost = 20e18;
-        } else if (nextLevel <= 10) {
-            woodCost = 200e18;
-            wheatCost = 100e18;
-            ironCost = 25e18;
-            blueprintCost = 1e18;
+    function _banditSpawnRegionWeights() internal view returns (uint256[] memory weights) {
+        weights = new uint256[](8);
+        uint256 clanCount = _allClanIds.length;
+        if (clanCount == 0) {
+            return weights;
         }
 
-        if (
-            clan.vaultWood < woodCost || clan.vaultWheat < wheatCost || clan.vaultIron < ironCost
-                || clan.blueprintBalance < blueprintCost
-        ) return false;
+        uint256 scanCount = clanCount < MAX_BANDIT_SPAWN_SCAN_PER_REGION ? clanCount : MAX_BANDIT_SPAWN_SCAN_PER_REGION;
+        uint256 startIndex = uint256(_world.currentTick) % clanCount;
+        uint256 clansmenScanned;
+        for (uint256 i = 0; i < scanCount; i++) {
+            Clan storage clan = _clans[_allClanIds[(startIndex + i) % clanCount]];
+            if (clan.clanState == ClanState.DEAD) {
+                continue;
+            }
 
-        clan.vaultWood -= woodCost;
-        clan.vaultWheat -= wheatCost;
-        clan.vaultIron -= ironCost;
-        clan.blueprintBalance -= blueprintCost;
+            if (
+                clan.baseRegion >= ClanWorldConstants.REGION_FOREST
+                    && clan.baseRegion <= ClanWorldConstants.REGION_DEEP_SEA
+            ) {
+                weights[clan.baseRegion - 1] += 100 + (_lootValueRaw(clan) / 1e18);
+            }
 
-        uint8 old = clan.monumentLevel;
-        clan.monumentLevel = nextLevel;
-        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
-        return true;
+            uint32[] storage clansmanIds = _clanClansmanIds[clan.clanId];
+            for (
+                uint256 j = 0;
+                j < clansmanIds.length && clansmenScanned < MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION;
+                j++
+            ) {
+                clansmenScanned += 1;
+                Clansman storage cs = _clansmen[clansmanIds[j]];
+                if (
+                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
+                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
+                ) {
+                    weights[cs.currentRegion - 1] += 25;
+                }
+            }
+        }
     }
 
-    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
-    function _completeMission(Clansman storage cs, Mission storage m) internal {
-        cs.state = ClansmanState.WAITING;
-        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
-        m.active = false;
-        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
+    function _refreshBanditSpawnWorldPreview(uint256[] memory regionWeights) internal {
+        uint64 nextEligibleTick = type(uint64).max;
+        uint16 maxChance = 0;
+
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
+            uint64 eligibleTick = spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS;
+            if (
+                _activeBanditCount < MAX_TOTAL_BANDITS && regionWeights[region - 1] > 0
+                    && _banditsByRegion[region].length < MAX_BANDITS_PER_REGION && eligibleTick < nextEligibleTick
+            ) {
+                nextEligibleTick = eligibleTick;
+            }
+            if (spawnState.probabilityAccum > maxChance) {
+                maxChance = spawnState.probabilityAccum;
+            }
+        }
+
+        _world.nextBanditSpawnEligibleTick = nextEligibleTick == type(uint64).max ? 0 : nextEligibleTick;
+        _world.currentBanditSpawnChanceBps = maxChance;
     }
 
     // =========================================================================
@@ -867,26 +2297,20 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
     ///         Execution order per spec §4.2 (CEI-safe):
     ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
-    ///         Seed:      closedTick seed derived and published before step 1 so
-    ///                    settlement RNG reads real entropy, not zero.
     ///         1. Settle missions completing this tick.
     ///         2. Execute scheduled market actions for closedTick (external calls).
-    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
-    ///         4. Resolve world events (season boundary, winter transitions).
-    ///         5. Increment tick and publish (seed already written above).
+    ///         3. Eager-settle clans touched by world events (Phase 9 stub).
+    ///         4. Advance bandit timers and resolve closed-tick bandit/world events.
+    ///         5. Increment tick and publish the next tick seed atomically.
     function heartbeat() external override nonReentrant {
         require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
 
         uint64 closedTick = _world.currentTick;
+        bytes32 closedTickSeed = _world.currentTickSeed;
 
         // CEI: update rate-limit guard before any external calls
         _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
 
-        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
-        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
-        _tickSeeds[closedTick] = newSeed;
-        _world.currentTickSeed = newSeed;
-
         // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
         // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
         _settleCompletingMissions(closedTick);
@@ -894,15 +2318,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         // Step 2: Execute scheduled market actions for closedTick (may make external calls).
         _executeScheduledMarketActions(closedTick);
 
-        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
-        // TODO Phase 3: _settleClansNearBandit(closedTick);
+        // Step 3: Eager-settle bases and active defenders in bandit spawn-candidate regions.
+        _eagerSettleForBandits(closedTick);
 
-        // Step 4: Resolve world events (season boundary, winter transitions).
+        // Step 4: Advance deterministic bandit timers for the closed tick.
+        _advanceBanditStates(closedTick);
+
+        // Step 5: Resolve deterministic bandit attacks for the closed tick.
+        _resolveAttackingBandits(closedTick);
+
+        // Step 6: Evaluate deterministic bandit spawns for the closed tick.
+        _evaluateBanditSpawns(closedTickSeed);
+
+        // Step 7: Resolve world events (season boundary, winter transitions).
         _resolveWorldEvents(closedTick);
 
-        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
+        // Step 8: Increment tick and publish the opened tick seed as one visible state transition.
         uint64 newTick = closedTick + 1;
+        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick));
         _world.currentTick = newTick;
+        _tickSeeds[newTick] = newSeed;
+        _world.currentTickSeed = newSeed;
         _world.nextHeartbeatAtTick = newTick + 1;
 
         emit TickAdvanced(closedTick, newTick, newSeed);
@@ -1002,7 +2438,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     /// @notice Mint a new clan and spawn its homebase.
     function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
         require(to != address(0), "ClanWorld: zero address");
-        require(_allClanIds.length < 12, "ClanWorld: max clans");
+        require(_allClanIds.length < MAX_CLANS, "ClanWorld: max clans");
         clanId = _nextClanId++;
         iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
 
@@ -1182,7 +2618,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         ctx.targetClanId =
             order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
 
-        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
+        // NOOP bypass: treat 0 as "stay here"; DefendBase requires the defended base region.
         ctx.isNoop = order.action != ActionType.DefendBase
             && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
         if (ctx.isNoop) {
@@ -1686,8 +3122,12 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         view
         returns (StatusCode)
     {
-        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
-        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
+        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
+        Clan storage targetClan = _clans[targetClanId];
+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
+            return StatusCode.ERR_INVALID_TARGET;
+        }
+        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
         return StatusCode.OK;
     }
 
@@ -1816,21 +3256,32 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return uint64(_travelTicks(fromRegion, toRegion));
     }
 
-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
-        return BanditTroop({
-            banditId: 0,
-            state: BanditState.NONE,
-            currentRegion: 0,
-            attackAttemptsMade: 0,
-            stateEnteredTick: 0,
-            nextActionTick: 0,
-            tier: 0,
-            attackPower: 0,
-            carryWood: 0,
-            carryIron: 0,
-            carryWheat: 0,
-            carryFish: 0
-        });
+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
+        BanditTroop memory bandit = _bandits[banditId];
+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
+            return BanditTroop({
+                id: 0,
+                region: 0,
+                state: BanditState.None,
+                targetClanId: 0,
+                tickEnteredState: 0,
+                strength: 0,
+                carryWood: 0,
+                carryIron: 0,
+                carryWheat: 0,
+                carryFish: 0,
+                carryGold: 0
+            });
+        }
+        return bandit;
+    }
+
+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
+        return getBandit(banditId);
+    }
+
+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
+        return _banditsByRegion[region];
     }
 
     function getWheatPlots(uint32 clanId)
@@ -1888,35 +3339,44 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // DERIVED READ GETTERS (read-only, no storage mutation)
     // =========================================================================
 
-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
-    ///      Carry amounts lag until next settleClan().
     function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
-        Clan memory clan = _clans[clanId];
-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
-        uint256 lootVal = _lootValueRaw(clan);
-        return
-            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
+        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
+        return _derivedClanStateFromSimulation(sim.clan);
     }
 
     function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
         Clansman memory cs = _clansmen[clansmanId];
-        Mission memory m = _missions[clansmanId];
-        uint8 effectiveRegion = cs.currentRegion;
-        if (cs.state == ClansmanState.TRAVELING && m.active) {
-            // Simplified: if past arrivalTick, they're at target; else at start
-            if (_world.currentTick >= m.arrivalTick) {
-                effectiveRegion = m.targetRegion;
-            } else {
-                effectiveRegion = m.startRegion;
-            }
+        if (cs.clansmanId == 0) {
+            Mission memory emptyMission;
+            return DerivedClansmanState({
+                clansman: cs, activeMission: emptyMission, effectiveRegion: 0, derivedAtTick: _world.currentTick
+            });
+        }
+
+        SettlementSimulation memory sim = _simulateSettleToTick(cs.clanId, _world.currentTick);
+        (bool found, uint256 index) = _findSimulatedClansman(sim, clansmanId);
+        if (found) {
+            cs = sim.clansmen[index];
+            Mission memory m = sim.missions[index];
+            return DerivedClansmanState({
+                clansman: cs,
+                activeMission: m,
+                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
+                derivedAtTick: _world.currentTick
+            });
         }
+
+        Mission memory fallbackMission = _missions[clansmanId];
         return DerivedClansmanState({
-            clansman: cs, activeMission: m, effectiveRegion: effectiveRegion, derivedAtTick: _world.currentTick
+            clansman: cs,
+            activeMission: fallbackMission,
+            effectiveRegion: _effectiveRegion(cs, fallbackMission, _world.currentTick),
+            derivedAtTick: _world.currentTick
         });
     }
 
-    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
-        return 0; // Phase 3
+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
+        return _bandits[banditId].targetClanId;
     }
 
     function quoteTravel(uint8 srcRegion, uint8 dstRegion)
@@ -1942,8 +3402,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     }
 
     function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
-        // Phase 1: return raw value (no simulation)
-        return _lootValueRaw(_clans[clanId]);
+        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
+        return _lootValueRaw(sim.clan);
     }
 
     /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
@@ -1951,6 +3411,32 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
     }
 
+    function _derivedClanStateFromSimulation(Clan memory clan) internal view returns (DerivedClanState memory) {
+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
+        return DerivedClanState({
+            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
+        });
+    }
+
+    function _findSimulatedClansman(SettlementSimulation memory sim, uint32 clansmanId)
+        internal
+        pure
+        returns (bool found, uint256 index)
+    {
+        for (uint256 i = 0; i < sim.clansmen.length; i++) {
+            if (sim.clansmen[i].clansmanId == clansmanId) {
+                return (true, i);
+            }
+        }
+    }
+
+    function _effectiveRegion(Clansman memory cs, Mission memory m, uint64 tick) internal pure returns (uint8) {
+        if (cs.state == ClansmanState.TRAVELING && m.active) {
+            return tick >= m.arrivalTick ? m.targetRegion : m.startRegion;
+        }
+        return cs.currentRegion;
+    }
+
     // =========================================================================
     // UI INDEXER AGGREGATOR GETTERS
     // =========================================================================
@@ -1992,49 +3478,50 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         });
     }
 
-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
-    ///      current; carry amounts and wheat progress lag until next settleClan() call.
-    ///      Full view-only settlement simulation is deferred (tracked issue).
     function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
-        Clan storage clan = _clans[clanId];
-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
-        uint256 lootVal = _lootValueRaw(clan);
-
-        DerivedClanState memory derivedClan =
-            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
+        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
+        DerivedClanState memory derivedClan = _derivedClanStateFromSimulation(sim.clan);
 
-        uint32[] storage csIds = _clanClansmanIds[clanId];
-        ClansmanFullView[] memory clansmen = new ClansmanFullView[](csIds.length);
-        for (uint256 i = 0; i < csIds.length; i++) {
-            uint32 csId = csIds[i];
-            Clansman memory cs = _clansmen[csId];
-            Mission memory m = _missions[csId];
-            uint8 effRegion = cs.currentRegion;
-            if (cs.state == ClansmanState.TRAVELING && m.active) {
-                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
-            }
+        ClansmanFullView[] memory clansmen = new ClansmanFullView[](sim.clansmen.length);
+        for (uint256 i = 0; i < sim.clansmen.length; i++) {
+            Clansman memory cs = sim.clansmen[i];
+            Mission memory m = sim.missions[i];
             DerivedClansmanState memory dcs = DerivedClansmanState({
-                clansman: cs, activeMission: m, effectiveRegion: effRegion, derivedAtTick: _world.currentTick
+                clansman: cs,
+                activeMission: m,
+                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
+                derivedAtTick: _world.currentTick
             });
             clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
         }
 
-        // Find if any of this clan's clansmen is defending a home region.
         uint32 thisClanDefendingBaseId = 0;
-        for (uint256 i = 0; i < csIds.length; i++) {
-            uint8 region = _clansmanDefendingRegion[csIds[i]];
-            if (region != 0) {
-                thisClanDefendingBaseId = region;
+        for (uint256 i = 0; i < sim.clansmen.length; i++) {
+            if (
+                sim.missions[i].active && sim.missions[i].action == ActionType.DefendBase
+                    && sim.clansmen[i].state == ClansmanState.ACTING
+            ) {
+                thisClanDefendingBaseId = sim.missions[i].targetRegion;
                 break;
             }
         }
+        if (thisClanDefendingBaseId == 0) {
+            uint32[] storage csIds = _clanClansmanIds[clanId];
+            for (uint256 i = 0; i < csIds.length; i++) {
+                uint8 region = _clansmanDefendingRegion[csIds[i]];
+                if (region != 0) {
+                    thisClanDefendingBaseId = region;
+                    break;
+                }
+            }
+        }
 
         return ClanFullView({
             clan: derivedClan,
             clansmen: clansmen,
-            westPlot: _wheatPlots[clanId][0],
-            eastPlot: _wheatPlots[clanId][1],
-            incomingDefenderIds: _defendingClansByRegion[clan.baseRegion],
+            westPlot: sim.wheatPlots[0],
+            eastPlot: sim.wheatPlots[1],
+            incomingDefenderIds: _defendingClansByRegion[sim.clan.baseRegion],
             thisClanDefendingBaseId: thisClanDefendingBaseId
         });
     }
@@ -2062,23 +3549,36 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         pr.spotPriceGoldPerResource = rA > 0 ? (rB * 1e18) / rA : 0;
     }
 
-    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
+    function getActiveBanditView() external view override returns (ActiveBanditView memory) {
+        BanditTroop memory bandit = _bandits[_world.activeBanditId];
+        uint64 nextActionTick = 0;
+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
+        if (exists) {
+            if (bandit.state == BanditState.Spawned) {
+                nextActionTick = bandit.tickEnteredState + 1;
+            } else if (bandit.state == BanditState.Camped) {
+                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
+            } else if (bandit.state == BanditState.Resting) {
+                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS;
+            }
+        }
+
         return ActiveBanditView({
-            exists: false,
-            banditId: 0,
-            state: BanditState.NONE,
-            currentRegion: 0,
+            exists: exists,
+            banditId: bandit.id,
+            state: bandit.state,
+            currentRegion: bandit.region,
             attackAttemptsMade: 0,
             maxAttemptsRemaining: 0,
-            stateEnteredTick: 0,
-            nextActionTick: 0,
+            stateEnteredTick: bandit.tickEnteredState,
+            nextActionTick: nextActionTick,
             tier: 0,
-            attackPower: 0,
-            carryWood: 0,
-            carryIron: 0,
-            carryWheat: 0,
-            carryFish: 0,
-            projectedTargetClanId: 0,
+            attackPower: _banditStrengthForLegacyEvent(bandit.strength),
+            carryWood: bandit.carryWood,
+            carryIron: bandit.carryIron,
+            carryWheat: bandit.carryWheat,
+            carryFish: bandit.carryFish,
+            projectedTargetClanId: bandit.targetClanId,
             projectedTargetLootValue: 0
         });
     }
diff --git a/packages/contracts/src/ClanWorldStub.sol b/packages/contracts/src/ClanWorldStub.sol
index 36ce29b..afe90ac 100644
--- a/packages/contracts/src/ClanWorldStub.sol
+++ b/packages/contracts/src/ClanWorldStub.sol
@@ -215,23 +215,30 @@ contract ClanWorldStub is IClanWorld {
         return 0;
     }
 
-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
         return BanditTroop({
-            banditId: 0,
-            state: BanditState.NONE,
-            currentRegion: 0,
-            attackAttemptsMade: 0,
-            stateEnteredTick: 0,
-            nextActionTick: 0,
-            tier: 0,
-            attackPower: 0,
+            id: 0,
+            region: 0,
+            state: BanditState.None,
+            targetClanId: 0,
+            tickEnteredState: 0,
+            strength: 0,
             carryWood: 0,
             carryIron: 0,
             carryWheat: 0,
-            carryFish: 0
+            carryFish: 0,
+            carryGold: 0
         });
     }
 
+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
+        return getBandit(banditId);
+    }
+
+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
+        return new uint32[](0);
+    }
+
     function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
         west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
         east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
@@ -341,7 +348,7 @@ contract ClanWorldStub is IClanWorld {
         return ActiveBanditView({
             exists: false,
             banditId: 0,
-            state: BanditState.NONE,
+            state: BanditState.None,
             currentRegion: 0,
             attackAttemptsMade: 0,
             maxAttemptsRemaining: 0,
diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
index 2b80fbe..5013b92 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -103,13 +103,15 @@ enum ClansmanState {
     DEAD
 }
 
+// v1 ABI: Bandit state machine redesigned in Phase 9. ABI consumers must regenerate.
 enum BanditState {
-    NONE,
-    CAMPING,
-    RESTING,
-    ATTACKING,
-    DEFEATED,
-    ESCAPED
+    None,
+    Spawned,
+    Camped,
+    Resting,
+    Attacking,
+    Defeated,
+    Escaped
 }
 
 enum WheatPlotState {
@@ -188,8 +190,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -297,22 +299,19 @@ struct Mission {
     uint256 maxGoldIn; // market_buy only, 0 otherwise
 }
 
+// v1 ABI: Bandit troop layout redesigned in Phase 9. ABI consumers must regenerate.
 struct BanditTroop {
-    uint32 banditId;
+    uint32 id;
+    uint8 region;
     BanditState state;
-
-    uint8 currentRegion;
-    uint8 attackAttemptsMade;
-    uint64 stateEnteredTick;
-    uint64 nextActionTick;
-
-    uint8 tier;
-    uint16 attackPower; // derived from tier; tier is canonical (v4.3 §G)
-
+    uint32 targetClanId; // 0 if not attacking
+    uint64 tickEnteredState;
+    uint32 strength; // hp / combat power
     uint256 carryWood;
     uint256 carryIron;
     uint256 carryWheat;
     uint256 carryFish;
+    uint256 carryGold;
 }
 
 struct ScheduledMarketAction {
@@ -598,7 +597,25 @@ interface IClanWorldEvents {
     );
     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
     event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
+    event LootDistributed(
+        uint32 indexed banditId,
+        uint32[] clanIdsRewarded,
+        uint256 perClanWood,
+        uint256 perClanWheat,
+        uint256 perClanFish,
+        uint256 perClanIron,
+        uint256 perClanGold,
+        uint256 burnedWood,
+        uint256 burnedWheat,
+        uint256 burnedFish,
+        uint256 burnedIron,
+        uint256 burnedGold
+    );
     event LootDistributedToDefender(
         uint32 indexed banditId,
         uint32 indexed clanId,
@@ -714,8 +731,12 @@ interface IClanWorld is IClanWorldEvents {
 
     function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
 
+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
+
     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
 
+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
+
     function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
 
     function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
diff --git a/packages/contracts/test/Bandit.t.sol b/packages/contracts/test/Bandit.t.sol
new file mode 100644
index 0000000..49ce87f
--- /dev/null
+++ b/packages/contracts/test/Bandit.t.sol
@@ -0,0 +1,202 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {ActiveBanditView, ClanWorldConstants, BanditState, BanditTroop, WorldState} from "../src/IClanWorld.sol";
+
+contract BanditHarness is ClanWorld {
+    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
+        return _spawnBandit(region, strength);
+    }
+
+    function transitionBandit(uint32 id, BanditState newState) external {
+        _transitionBanditState(id, newState);
+    }
+
+    function transitionBanditToAttacking(uint32 id, uint32 targetClanId) external {
+        _transitionBanditToAttacking(id, targetClanId);
+    }
+}
+
+contract BanditTest is Test {
+    BanditHarness world;
+
+    function setUp() public {
+        world = new BanditHarness();
+    }
+
+    function _advanceTick() internal {
+        vm.warp(world.getWorldState().nextHeartbeatAtTs);
+        world.heartbeat();
+    }
+
+    function _advanceTicks(uint64 ticks) internal {
+        for (uint64 i = 0; i < ticks; i++) {
+            _advanceTick();
+        }
+    }
+
+    function _spawnAndCamp() internal returns (uint32 id) {
+        id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+        _advanceTicks(2);
+    }
+
+    function _spawnCampAndAttack(uint32 targetClanId) internal returns (uint32 id) {
+        id = _spawnAndCamp();
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
+        world.transitionBanditToAttacking(id, targetClanId);
+    }
+
+    function test_spawnBandit_recordsSpawnedTroopAndRegionIndex() public {
+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 250);
+
+        assertEq(id, 1, "first bandit id");
+        BanditTroop memory bandit = world.getBandit(id);
+        assertEq(bandit.id, id, "bandit id");
+        assertEq(bandit.region, ClanWorldConstants.REGION_MOUNTAINS, "region");
+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
+        assertEq(bandit.targetClanId, 0, "no target while spawned");
+        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick, "entered tick");
+        assertEq(bandit.strength, 250, "strength");
+
+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
+        assertEq(regionBandits.length, 1, "region index length");
+        assertEq(regionBandits[0], id, "region index id");
+
+        WorldState memory state = world.getWorldState();
+        assertEq(state.activeBanditId, id, "active bandit");
+    }
+
+    function test_getBanditMissingIdReturnsNoneState() public view {
+        BanditTroop memory bandit = world.getBandit(999);
+
+        assertEq(bandit.id, 0, "missing id");
+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
+    }
+
+    function test_defaultBanditTroopStateIsNone() public pure {
+        BanditTroop memory bandit;
+
+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
+    }
+
+    function test_spawnedToCampedTransitionAdvancesAfterSpawnDelay() public {
+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+
+        _advanceTicks(2);
+
+        BanditTroop memory bandit = world.getBandit(id);
+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
+        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick - 1, "entered camp tick");
+    }
+
+    function test_spawnedActiveBanditViewReportsNextActionTick() public {
+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+
+        ActiveBanditView memory view_ = world.getActiveBanditView();
+
+        assertEq(view_.banditId, id, "active bandit");
+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
+        assertEq(view_.nextActionTick, world.getWorldState().currentTick + 1, "spawn delay tick");
+    }
+
+    function test_campedToAttackingTransitionSucceedsWithTargetAfterCampDuration() public {
+        uint32 id = _spawnAndCamp();
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
+
+        world.transitionBanditToAttacking(id, 77);
+
+        BanditTroop memory bandit = world.getBandit(id);
+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
+        assertEq(bandit.targetClanId, 77, "target");
+        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick, "entered attack tick");
+    }
+
+    function test_attackingToDefeatedDeletesBanditAndRegionIndex() public {
+        uint32 id = _spawnCampAndAttack(77);
+
+        world.transitionBandit(id, BanditState.Defeated);
+
+        BanditTroop memory deletedBandit = world.getBandit(id);
+        assertEq(deletedBandit.id, 0, "deleted id");
+        assertEq(deletedBandit.region, 0, "deleted region");
+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
+        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
+    }
+
+    function test_defeatingActiveBanditPromotesOldestRemainingBandit() public {
+        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 200);
+        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_WEST_FARMS, 300);
+
+        _advanceTicks(2);
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
+        world.transitionBanditToAttacking(id1, 77);
+        world.transitionBandit(id1, BanditState.Defeated);
+
+        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
+    }
+
+    function test_attackingToEscapedRestingCampedCycleWorks() public {
+        uint32 id = _spawnCampAndAttack(77);
+
+        world.transitionBandit(id, BanditState.Escaped);
+        BanditTroop memory escaped = world.getBandit(id);
+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
+        assertEq(escaped.targetClanId, 0, "target cleared on escape");
+
+        world.transitionBandit(id, BanditState.Resting);
+        BanditTroop memory resting = world.getBandit(id);
+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
+        assertEq(resting.tickEnteredState, world.getWorldState().currentTick, "resting tick");
+
+        _advanceTicks(ClanWorldConstants.BANDIT_REST_TICKS + 1);
+
+        BanditTroop memory camped = world.getBandit(id);
+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
+        assertEq(camped.targetClanId, 0, "target remains clear");
+    }
+
+    function test_invalidTransitionsRevert() public {
+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+
+        vm.expectRevert("ClanWorld: invalid bandit transition");
+        world.transitionBandit(id, BanditState.Defeated);
+
+        _advanceTicks(2);
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
+
+        vm.expectRevert("ClanWorld: invalid bandit transition");
+        world.transitionBandit(id, BanditState.Attacking);
+
+        vm.expectRevert("ClanWorld: invalid bandit transition");
+        world.transitionBandit(id, BanditState.None);
+    }
+
+    function test_getBanditsInRegionReturnsListAndUpdatesAfterDelete() public {
+        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 200);
+        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 300);
+
+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
+        assertEq(forestBandits.length, 2, "forest count");
+        assertEq(forestBandits[0], id1, "first forest bandit");
+        assertEq(forestBandits[1], id2, "second forest bandit");
+
+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
+        assertEq(mountainBandits.length, 1, "mountain count");
+        assertEq(mountainBandits[0], id3, "mountain bandit");
+
+        _advanceTicks(2);
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
+        world.transitionBanditToAttacking(id1, 77);
+        world.transitionBandit(id1, BanditState.Defeated);
+
+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
+        assertEq(forestBandits.length, 1, "forest count after delete");
+        assertEq(forestBandits[0], id2, "remaining forest bandit");
+    }
+}
diff --git a/packages/contracts/test/BanditAttackResolution.t.sol b/packages/contracts/test/BanditAttackResolution.t.sol
new file mode 100644
index 0000000..eb15f01
--- /dev/null
+++ b/packages/contracts/test/BanditAttackResolution.t.sol
@@ -0,0 +1,693 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {Vm} from "forge-std/Vm.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {
+    ClanWorldConstants,
+    ClanState,
+    ClansmanState,
+    BanditState,
+    ActionType,
+    StatusCode,
+    Clan,
+    ClanFullView,
+    Mission,
+    ClanOrder,
+    OrderResult
+} from "../src/IClanWorld.sol";
+
+contract BanditAttackHarness is ClanWorld {
+    function forceAttackingBandit(uint8 region, uint32 strength, uint32 targetClanId) external returns (uint32 id) {
+        id = _spawnBandit(region, strength);
+        _bandits[id].state = BanditState.Attacking;
+        _bandits[id].targetClanId = targetClanId;
+    }
+
+    function setWallLevel(uint32 clanId, uint8 wallLevel) external {
+        _clans[clanId].wallLevel = wallLevel;
+    }
+
+    function setStarvationStartsAt(uint32 clanId, uint64 tick) external {
+        _clans[clanId].starvationStartsAtTick = tick;
+    }
+
+    function setClanUpkeepState(uint32 clanId, uint64 lastSettledTick, uint256 vaultWheat, uint256 vaultFish) external {
+        Clan storage clan = _clans[clanId];
+        clan.lastSettledTick = lastSettledTick;
+        clan.vaultWheat = vaultWheat;
+        clan.vaultFish = vaultFish;
+        clan.starvationStartsAtTick = 0;
+    }
+
+    function setLivingClansmenForTest(uint32 clanId, uint8 livingClansmen) external {
+        _clans[clanId].livingClansmen = livingClansmen;
+    }
+
+    function blockBanditSpawnsForTest() external {
+        uint64 currentTick = this.getWorldState().currentTick;
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            _banditSpawnByRegion[region].lastSpawnTick = currentTick;
+        }
+    }
+
+    function blueprintUnit() external pure returns (uint256) {
+        return BLUEPRINT_UNIT;
+    }
+
+    function setBanditStrength(uint32 banditId, uint32 strength) external {
+        _bandits[banditId].strength = strength;
+    }
+
+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
+        external
+    {
+        _bandits[banditId].carryWood = wood;
+        _bandits[banditId].carryWheat = wheat;
+        _bandits[banditId].carryFish = fish;
+        _bandits[banditId].carryIron = iron;
+        _bandits[banditId].carryGold = gold;
+    }
+
+    function defenseRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId) external pure returns (uint32) {
+        return _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
+    }
+}
+
+contract BanditAttackResolutionTest is Test {
+    BanditAttackHarness world;
+    address elder = address(0xA1);
+
+    event BanditAttackResolved(
+        uint32 indexed banditId,
+        uint32 indexed targetClanId,
+        bool defended,
+        uint16 attackPower,
+        uint16 totalDefense,
+        uint16 wallLevelAfter,
+        uint256 stolenWood,
+        uint256 stolenIron,
+        uint256 stolenWheat,
+        uint256 stolenFish,
+        uint64 atTick
+    );
+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
+    event LootDistributed(
+        uint32 indexed banditId,
+        uint32[] clanIdsRewarded,
+        uint256 perClanWood,
+        uint256 perClanWheat,
+        uint256 perClanFish,
+        uint256 perClanIron,
+        uint256 perClanGold,
+        uint256 burnedWood,
+        uint256 burnedWheat,
+        uint256 burnedFish,
+        uint256 burnedIron,
+        uint256 burnedGold
+    );
+
+    function setUp() public {
+        world = new BanditAttackHarness();
+    }
+
+    function _mintClan() internal returns (uint32 clanId) {
+        vm.prank(elder);
+        (clanId,) = world.mintClan(elder);
+    }
+
+    function _csId(uint32 clanId, uint256 index) internal view returns (uint32) {
+        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
+    }
+
+    function _defendOrders(uint32 clanId, uint256 count) internal view returns (ClanOrder[] memory orders) {
+        Clan memory clan = world.getClan(clanId);
+        return _defendTargetOrders(clanId, clanId, clan.baseRegion, count);
+    }
+
+    function _defendTargetOrders(uint32 clanId, uint32 targetClanId, uint8 targetRegion, uint256 count)
+        internal
+        view
+        returns (ClanOrder[] memory orders)
+    {
+        orders = new ClanOrder[](count);
+        for (uint256 i = 0; i < count; i++) {
+            orders[i] = ClanOrder({
+                clansmanId: _csId(clanId, i),
+                gotoRegion: targetRegion,
+                action: ActionType.DefendBase,
+                targetClanId: targetClanId,
+                marketToken: address(0),
+                marketAmount: 0,
+                maxGoldIn: 0
+            });
+        }
+    }
+
+    function _submitDefenders(uint32 clanId, uint256 count) internal {
+        _submitTargetDefenders(clanId, clanId, count);
+    }
+
+    function _submitTargetDefenders(uint32 defenderClanId, uint32 targetClanId, uint256 count)
+        internal
+        returns (uint64 maxExecutesAtTick)
+    {
+        Clan memory targetClan = world.getClan(targetClanId);
+        ClanOrder[] memory orders = _defendTargetOrders(defenderClanId, targetClanId, targetClan.baseRegion, count);
+        vm.prank(elder);
+        OrderResult[] memory results = world.submitClanOrders(defenderClanId, orders);
+        for (uint256 i = 0; i < count; i++) {
+            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "defender order status");
+            Mission memory mission = world.getActiveMission(orders[i].clansmanId);
+            if (mission.executesAtTick > maxExecutesAtTick) {
+                maxExecutesAtTick = mission.executesAtTick;
+            }
+        }
+    }
+
+    function _advanceTick() internal {
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        world.heartbeat();
+    }
+
+    function _advanceUntilAfter(uint64 tick) internal {
+        while (world.getWorldState().currentTick <= tick) {
+            _advanceTick();
+        }
+    }
+
+    function _advanceUntil(uint64 tick) internal {
+        while (world.getWorldState().currentTick < tick) {
+            _advanceTick();
+        }
+    }
+
+    function _assertBanditTargetDiedLog(
+        Vm.Log[] memory logs,
+        uint32 expectedBanditId,
+        uint32 expectedDeadClanId,
+        uint64 expectedTick
+    ) internal {
+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
+        bytes32 expectedBanditTopic = bytes32(uint256(expectedBanditId));
+        bytes32 expectedClanTopic = bytes32(uint256(expectedDeadClanId));
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (
+                logs[i].topics.length == 3 && logs[i].topics[0] == eventSig && logs[i].topics[1] == expectedBanditTopic
+                    && logs[i].topics[2] == expectedClanTopic
+            ) {
+                uint64 tick = abi.decode(logs[i].data, (uint64));
+                if (tick == expectedTick) return;
+            }
+        }
+        fail("expected BanditTargetDied log");
+    }
+
+    function _assertBanditAttackResolvedLog(Vm.Log[] memory logs, uint32 expectedBanditId, uint32 expectedTargetClanId)
+        internal
+    {
+        bytes32 eventSig = keccak256(
+            "BanditAttackResolved(uint32,uint32,bool,uint16,uint16,uint16,uint256,uint256,uint256,uint256,uint64)"
+        );
+        bytes32 expectedBanditTopic = bytes32(uint256(expectedBanditId));
+        bytes32 expectedClanTopic = bytes32(uint256(expectedTargetClanId));
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (
+                logs[i].topics.length == 3 && logs[i].topics[0] == eventSig && logs[i].topics[1] == expectedBanditTopic
+                    && logs[i].topics[2] == expectedClanTopic
+            ) {
+                return;
+            }
+        }
+        fail("expected BanditAttackResolved log");
+    }
+
+    function _activateTargetDefenders(uint32 targetClanId, uint32[] memory defenderClanIds, uint256 countEach)
+        internal
+    {
+        uint64 maxExecutesAtTick;
+        for (uint256 i = 0; i < defenderClanIds.length; i++) {
+            uint64 executesAtTick = _submitTargetDefenders(defenderClanIds[i], targetClanId, countEach);
+            if (executesAtTick > maxExecutesAtTick) {
+                maxExecutesAtTick = executesAtTick;
+            }
+        }
+
+        _advanceUntilAfter(maxExecutesAtTick);
+        for (uint256 i = 0; i < defenderClanIds.length; i++) {
+            world.settleClan(defenderClanIds[i]);
+        }
+    }
+
+    function _forceAttack(uint32 clanId, uint32 strength) internal returns (uint32 banditId) {
+        Clan memory clan = world.getClan(clanId);
+        return world.forceAttackingBandit(clan.baseRegion, strength, clanId);
+    }
+
+    function _mintClans(uint256 count) internal returns (uint32[] memory clanIds) {
+        clanIds = new uint32[](count);
+        for (uint256 i = 0; i < count; i++) {
+            clanIds[i] = _mintClan();
+        }
+    }
+
+    function test_defeatedBanditLootSingleDefendingClanGetsFullCarry() public {
+        uint32 clanId = _mintClan();
+        uint32[] memory defenders = new uint32[](1);
+        defenders[0] = clanId;
+        _activateTargetDefenders(clanId, defenders, 4);
+
+        Clan memory beforeClan = world.getClan(clanId);
+        uint32 banditId = _forceAttack(clanId, 1);
+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
+        world.setBanditStrength(banditId, 0);
+
+        _advanceTick();
+
+        Clan memory afterClan = world.getClan(clanId);
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood + 7e18, "wood full carry");
+        assertEq(afterClan.vaultWheat, beforeClan.vaultWheat + 11e18, "wheat full carry");
+        assertEq(afterClan.vaultFish, beforeClan.vaultFish + 13e18, "fish full carry");
+        assertEq(afterClan.vaultIron, beforeClan.vaultIron + 17e18, "iron full carry");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance + 19e18, "gold full carry");
+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
+    }
+
+    function test_defeatedBanditAwardsBlueprintToTargetClan() public {
+        uint32 clanId = _mintClan();
+        uint32[] memory defenders = new uint32[](1);
+        defenders[0] = clanId;
+        _activateTargetDefenders(clanId, defenders, 4);
+
+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
+        uint32 banditId = _forceAttack(clanId, 1);
+        world.setBanditStrength(banditId, 0);
+
+        vm.expectEmit(true, true, false, true, address(world));
+        emit BlueprintEarned(clanId, banditId, world.blueprintUnit(), world.getWorldState().currentTick);
+
+        _advanceTick();
+
+        assertEq(
+            world.getClan(clanId).blueprintBalance, blueprintBefore + world.blueprintUnit(), "target blueprint awarded"
+        );
+    }
+
+    function test_e2e_banditLifecycle_throughHeartbeat() public {
+        uint32 clanId = _mintClan();
+
+        uint32 banditId;
+        for (uint256 i = 0; i < 64; i++) {
+            vm.prevrandao(keccak256(abi.encodePacked("bandit-lifecycle", i)));
+            _advanceTick();
+            banditId = world.getWorldState().activeBanditId;
+            if (banditId != 0) break;
+        }
+        assertGt(banditId, 0, "bandit spawned through heartbeat");
+
+        _advanceTick();
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Camped), "spawned to camped");
+
+        uint64 campedAt = world.getBandit(banditId).tickEnteredState;
+        while (world.getWorldState().currentTick < campedAt + ClanWorldConstants.BANDIT_CAMP_TICKS) {
+            _advanceTick();
+        }
+
+        vm.recordLogs();
+        _advanceTick();
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+        _assertBanditAttackResolvedLog(logs, banditId, clanId);
+
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "attack resolved to escaped");
+
+        for (uint64 i = 0; i < ClanWorldConstants.BANDIT_REST_TICKS; i++) {
+            _advanceTick();
+        }
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Resting), "escaped recovered to resting");
+    }
+
+    function test_deadTargetCleanupReleasesDefendersAndEscapesBandit() public {
+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
+        _advanceUntil(winterStart + 4);
+
+        uint32[] memory clanIds = _mintClans(3);
+        world.blockBanditSpawnsForTest();
+
+        uint32 defenderClanId = clanIds[0];
+        uint32 targetClanId = clanIds[1];
+        uint32 unaffectedClanId = clanIds[2];
+
+        uint64 defenderExecutesAtTick = _submitTargetDefenders(defenderClanId, targetClanId, 1);
+        _advanceUntilAfter(defenderExecutesAtTick);
+        world.settleClan(defenderClanId);
+
+        uint32 defenderId = _csId(defenderClanId, 0);
+        ClansmanState defenderStateBefore = world.getClansman(defenderId).state;
+        uint64 defenderCooldownBefore = world.getClansman(defenderId).cooldownEndsAtTs;
+        assertEq(uint8(defenderStateBefore), uint8(ClansmanState.ACTING), "defender active before target death");
+        assertEq(world.getActiveDefenders(targetClanId).length, 1, "target has active defender");
+
+        uint64 deathFromTick = winterStart;
+        world.setClanUpkeepState(targetClanId, deathFromTick, 0, 0);
+
+        Clan memory unaffectedBefore = world.getClan(unaffectedClanId);
+        uint32 banditId = _forceAttack(targetClanId, 100);
+        uint64 expectedBanditAbortTick = world.getWorldState().currentTick;
+
+        vm.recordLogs();
+        world.settleClan(targetClanId);
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
+
+        Clan memory targetAfter = world.getClan(targetClanId);
+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
+        assertEq(targetAfter.livingClansmen, 0, "target living count");
+
+        ClansmanState defenderStateAfter = world.getClansman(defenderId).state;
+        assertEq(uint8(defenderStateAfter), uint8(ClansmanState.WAITING), "defender released to waiting");
+        assertEq(world.getClansman(defenderId).cooldownEndsAtTs, defenderCooldownBefore, "cooldown unchanged");
+        assertFalse(world.getActiveMission(defenderId).active, "defender mission cleared");
+        assertEq(world.getActiveDefenders(targetClanId).length, 0, "target defender registry cleared");
+
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
+
+        Clan memory unaffectedAfter = world.getClan(unaffectedClanId);
+        assertEq(uint8(unaffectedAfter.clanState), uint8(unaffectedBefore.clanState), "clan C state unaffected");
+        assertEq(unaffectedAfter.livingClansmen, unaffectedBefore.livingClansmen, "clan C living unaffected");
+        assertEq(unaffectedAfter.vaultWheat, unaffectedBefore.vaultWheat, "clan C wheat unaffected");
+        assertEq(unaffectedAfter.vaultFish, unaffectedBefore.vaultFish, "clan C fish unaffected");
+    }
+
+    function test_defeatedBanditLootSplitsEquallyAcrossFourDefendingClans() public {
+        uint32[] memory clanIds = _mintClans(4);
+        _activateTargetDefenders(clanIds[0], clanIds, 1);
+
+        uint256[4] memory woodBefore;
+        uint256[4] memory wheatBefore;
+        uint256[4] memory fishBefore;
+        uint256[4] memory ironBefore;
+        uint256[4] memory goldBefore;
+        for (uint256 i = 0; i < clanIds.length; i++) {
+            Clan memory clan = world.getClan(clanIds[i]);
+            woodBefore[i] = clan.vaultWood;
+            wheatBefore[i] = clan.vaultWheat;
+            fishBefore[i] = clan.vaultFish;
+            ironBefore[i] = clan.vaultIron;
+            goldBefore[i] = clan.goldBalance;
+        }
+
+        uint32 banditId = _forceAttack(clanIds[0], 1);
+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
+        world.setBanditStrength(banditId, 0);
+
+        _advanceTick();
+
+        for (uint256 i = 0; i < clanIds.length; i++) {
+            Clan memory clan = world.getClan(clanIds[i]);
+            assertEq(clan.vaultWood, woodBefore[i] + 25e18, "wood share");
+            assertEq(clan.vaultWheat, wheatBefore[i] + 25e18, "wheat share");
+            assertEq(clan.vaultFish, fishBefore[i] + 25e18, "fish share");
+            assertEq(clan.vaultIron, ironBefore[i] + 25e18, "iron share");
+            assertEq(clan.goldBalance, goldBefore[i] + 25e18, "gold share");
+        }
+    }
+
+    function test_mixedClanDefenseAwardsBlueprintOnlyToBaseOwner() public {
+        uint32[] memory clanIds = _mintClans(4);
+        _activateTargetDefenders(clanIds[0], clanIds, 1);
+
+        uint256[4] memory blueprintBefore;
+        for (uint256 i = 0; i < clanIds.length; i++) {
+            blueprintBefore[i] = world.getClan(clanIds[i]).blueprintBalance;
+        }
+
+        uint32 banditId = _forceAttack(clanIds[0], 1);
+        world.setBanditStrength(banditId, 0);
+
+        _advanceTick();
+
+        assertEq(
+            world.getClan(clanIds[0]).blueprintBalance,
+            blueprintBefore[0] + world.blueprintUnit(),
+            "base owner blueprint"
+        );
+        for (uint256 i = 1; i < clanIds.length; i++) {
+            assertEq(world.getClan(clanIds[i]).blueprintBalance, blueprintBefore[i], "helper clan no blueprint");
+        }
+    }
+
+    function test_multipleDefeatedBanditsEachAwardBlueprint() public {
+        uint32 clanId = _mintClan();
+        uint32[] memory defenders = new uint32[](1);
+        defenders[0] = clanId;
+        _activateTargetDefenders(clanId, defenders, 4);
+
+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
+        uint32 firstBanditId = _forceAttack(clanId, 1);
+        uint32 secondBanditId = _forceAttack(clanId, 1);
+        world.setBanditStrength(firstBanditId, 0);
+        world.setBanditStrength(secondBanditId, 0);
+
+        _advanceTick();
+
+        assertEq(
+            world.getClan(clanId).blueprintBalance,
+            blueprintBefore + 2 * world.blueprintUnit(),
+            "one blueprint per defeated bandit"
+        );
+    }
+
+    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
+        _advanceUntil(winterStart + 30);
+        assertFalse(world.getWorldState().winterActive, "test settles after winter");
+
+        uint32 clanId = _mintClan();
+        world.setClanUpkeepState(clanId, winterStart, 0, 0);
+
+        ClanFullView memory preview = world.getClanFullView(clanId);
+        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
+        assertEq(preview.clan.clan.livingClansmen, 0, "derived living count");
+
+        world.settleClan(clanId);
+
+        Clan memory settled = world.getClan(clanId);
+        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
+        assertEq(settled.livingClansmen, 0, "settled living count");
+    }
+
+    function test_resolveBanditAttackReturnsWhenTargetDiesDuringSettlement() public {
+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
+        _advanceUntil(winterStart + 1);
+
+        uint32 targetClanId = _mintClan();
+        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
+        world.setLivingClansmenForTest(targetClanId, 1);
+
+        uint32 banditId = _forceAttack(targetClanId, 100);
+
+        _advanceTick();
+
+        assertEq(uint8(world.getClan(targetClanId).clanState), uint8(ClanState.DEAD), "target starved");
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped once");
+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
+    }
+
+    function test_defeatedBanditLootBurnsWholeTokenOverflowAcrossThreeDefendingClans() public {
+        uint32[] memory clanIds = _mintClans(3);
+        _activateTargetDefenders(clanIds[0], clanIds, 1);
+
+        uint256 woodBeforeTotal;
+        uint256 goldBeforeTotal;
+        for (uint256 i = 0; i < clanIds.length; i++) {
+            Clan memory clan = world.getClan(clanIds[i]);
+            woodBeforeTotal += clan.vaultWood;
+            goldBeforeTotal += clan.goldBalance;
+        }
+
+        uint32 banditId = _forceAttack(clanIds[0], 1);
+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
+        world.setBanditStrength(banditId, 0);
+
+        vm.expectEmit(true, true, false, false, address(world));
+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
+        vm.expectEmit(true, true, false, true, address(world));
+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
+        vm.expectEmit(true, false, false, true, address(world));
+        emit LootDistributed(banditId, clanIds, 33e18, 33e18, 33e18, 33e18, 33e18, 1e18, 1e18, 1e18, 1e18, 1e18);
+
+        _advanceTick();
+
+        uint256 woodAfterTotal;
+        uint256 goldAfterTotal;
+        for (uint256 i = 0; i < clanIds.length; i++) {
+            Clan memory clan = world.getClan(clanIds[i]);
+            assertEq(clan.vaultWood, 20e18 + 33e18, "wood share");
+            assertEq(clan.goldBalance, 3e18 + 33e18, "gold share");
+            woodAfterTotal += clan.vaultWood;
+            goldAfterTotal += clan.goldBalance;
+        }
+        assertEq(woodAfterTotal - woodBeforeTotal, 99e18, "wood overflow burned");
+        assertEq(goldAfterTotal - goldBeforeTotal, 99e18, "gold overflow burned");
+    }
+
+    function test_multipleDefendersFromSameClanStillReceiveOneClanShare() public {
+        uint32[] memory clanIds = _mintClans(2);
+        uint64 firstExecutesAtTick = _submitTargetDefenders(clanIds[0], clanIds[0], 3);
+        uint64 secondExecutesAtTick = _submitTargetDefenders(clanIds[1], clanIds[0], 1);
+        _advanceUntilAfter(firstExecutesAtTick > secondExecutesAtTick ? firstExecutesAtTick : secondExecutesAtTick);
+        world.settleClan(clanIds[0]);
+        world.settleClan(clanIds[1]);
+
+        uint256 targetWoodBefore = world.getClan(clanIds[0]).vaultWood;
+        uint256 helperWoodBefore = world.getClan(clanIds[1]).vaultWood;
+        uint32 banditId = _forceAttack(clanIds[0], 1);
+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
+        world.setBanditStrength(banditId, 0);
+
+        _advanceTick();
+
+        assertEq(world.getClan(clanIds[0]).vaultWood, targetWoodBefore + 50e18, "target clan one share");
+        assertEq(world.getClan(clanIds[1]).vaultWood, helperWoodBefore + 50e18, "helper clan one share");
+    }
+
+    function test_escapedBanditDoesNotDistributeCarry() public {
+        uint32 clanId = _mintClan();
+        Clan memory beforeClan = world.getClan(clanId);
+        uint32 banditId = _forceAttack(clanId, 100);
+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
+
+        _advanceTick();
+
+        Clan memory afterClan = world.getClan(clanId);
+        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "wood unchanged");
+        assertEq(afterClan.vaultWheat, beforeClan.vaultWheat, "wheat unchanged");
+        assertEq(afterClan.vaultFish, beforeClan.vaultFish, "fish unchanged");
+        assertEq(afterClan.vaultIron, beforeClan.vaultIron, "iron unchanged");
+        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "gold unchanged");
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
+    }
+
+    function test_escapedBanditDoesNotAwardBlueprint() public {
+        uint32 clanId = _mintClan();
+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
+        uint32 banditId = _forceAttack(clanId, 100);
+
+        _advanceTick();
+
+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore, "blueprint unchanged");
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
+    }
+
+    function test_twoAliveDefendersWithSufficientDefenseDefeatBanditWithoutWallChip() public {
+        uint32 clanId = _mintClan();
+        _submitDefenders(clanId, 2);
+        world.setWallLevel(clanId, 1);
+
+        uint32 banditId = _forceAttack(clanId, 1);
+        bytes32 tickSeed = world.getWorldState().currentTickSeed;
+        uint32 defense = world.defenseRoll(tickSeed, banditId, _csId(clanId, 0))
+            + world.defenseRoll(tickSeed, banditId, _csId(clanId, 1));
+        uint32 strength = defense / 2;
+        if (strength == 0) strength = 1;
+        world.setBanditStrength(banditId, strength);
+        _advanceTick();
+
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
+        assertEq(world.getClan(clanId).wallLevel, 1, "wall was not chipped");
+        assertEq(world.getClan(clanId).livingClansmen, 4, "no casualties");
+    }
+
+    function test_weakDefenseChipsWallOneLevel() public {
+        uint32 clanId = _mintClan();
+        world.setWallLevel(clanId, 1);
+        uint32 banditId = _forceAttack(clanId, 100);
+
+        _advanceTick();
+
+        assertEq(world.getClan(clanId).wallLevel, 0, "wall level decreased");
+        assertEq(world.getClan(clanId).livingClansmen, 4, "wall absorbed full hit");
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
+    }
+
+    function test_wallZeroWeakDefenseKillsClansmanDeterministically() public {
+        uint32 clanId = _mintClan();
+        uint32 banditId = _forceAttack(clanId, 100);
+
+        _advanceTick();
+
+        assertEq(world.getClan(clanId).livingClansmen, 3, "one clansman died");
+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
+    }
+
+    function test_allClansmenDeadMarksClanDead() public {
+        uint32 clanId = _mintClan();
+        _forceAttack(clanId, 425);
+
+        _advanceTick();
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.livingClansmen, 0, "all clansmen dead");
+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
+        assertEq(clan.vaultWood, 0, "dead target wood burned");
+        assertEq(clan.vaultWheat, 0, "dead target wheat burned");
+        assertEq(clan.vaultFish, 0, "dead target fish burned");
+        assertEq(clan.vaultIron, 0, "dead target iron burned");
+    }
+
+    function test_starvingDefenderContributesZeroDefense() public {
+        uint32 clanId = _mintClan();
+        _submitDefenders(clanId, 1);
+        _advanceTick();
+        world.settleClan(clanId);
+        world.setWallLevel(clanId, 1);
+        world.setStarvationStartsAt(clanId, 1);
+
+        uint32 banditId = _forceAttack(clanId, 100);
+        uint32 nonStarvingRoll = world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
+        assertGt(nonStarvingRoll, 0, "test setup needs nonzero roll");
+
+        _advanceTick();
+
+        assertEq(world.getClan(clanId).wallLevel, 0, "starving defender did not reduce incoming wall hit");
+        assertEq(world.getClan(clanId).livingClansmen, 4, "wall absorbed full hit");
+    }
+
+    function test_twoAttacksSameTickDeterminismAcrossReplay() public {
+        BanditAttackHarness a = new BanditAttackHarness();
+        BanditAttackHarness b = new BanditAttackHarness();
+
+        uint32 aFirst;
+        uint32 aSecond;
+        uint32 bFirst;
+        uint32 bSecond;
+        (aFirst, aSecond) = _setupTwoAttackWorld(a);
+        (bFirst, bSecond) = _setupTwoAttackWorld(b);
+
+        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
+        a.heartbeat();
+        b.heartbeat();
+
+        assertEq(a.getClan(aFirst).livingClansmen, b.getClan(bFirst).livingClansmen, "first target deterministic");
+        assertEq(a.getClan(aSecond).livingClansmen, b.getClan(bSecond).livingClansmen, "second target deterministic");
+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
+    }
+
+    function _setupTwoAttackWorld(BanditAttackHarness target)
+        internal
+        returns (uint32 firstClanId, uint32 secondClanId)
+    {
+        vm.prank(elder);
+        (firstClanId,) = target.mintClan(elder);
+        vm.prank(elder);
+        (secondClanId,) = target.mintClan(elder);
+
+        target.forceAttackingBandit(target.getClan(firstClanId).baseRegion, 100, firstClanId);
+        target.forceAttackingBandit(target.getClan(secondClanId).baseRegion, 100, secondClanId);
+    }
+}
diff --git a/packages/contracts/test/BanditSpawn.t.sol b/packages/contracts/test/BanditSpawn.t.sol
new file mode 100644
index 0000000..1b7ebb0
--- /dev/null
+++ b/packages/contracts/test/BanditSpawn.t.sol
@@ -0,0 +1,403 @@
+// SPDX-License-Identifier: MIT
+pragma solidity ^0.8.34;
+
+import {Test} from "forge-std/Test.sol";
+import {ClanWorld} from "../src/ClanWorld.sol";
+import {
+    ActionType,
+    BanditState,
+    BanditTroop,
+    Clan,
+    ClanOrder,
+    ClanWorldConstants,
+    ClansmanState,
+    Mission,
+    OrderResult,
+    StatusCode,
+    WorldState
+} from "../src/IClanWorld.sol";
+
+contract BanditSpawnHarness is ClanWorld {
+    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
+        return _spawnBandit(region, strength);
+    }
+
+    function evaluateBanditSpawns(bytes32 tickSeed) external {
+        _evaluateBanditSpawns(tickSeed);
+    }
+
+    function setBanditSpawnState(uint8 region, uint64 lastSpawnTick, uint16 probabilityAccum) external {
+        _banditSpawnByRegion[region].lastSpawnTick = lastSpawnTick;
+        _banditSpawnByRegion[region].probabilityAccum = probabilityAccum;
+    }
+
+    function getBanditSpawnState(uint8 region) external view returns (uint64 lastSpawnTick, uint16 probabilityAccum) {
+        lastSpawnTick = _banditSpawnByRegion[region].lastSpawnTick;
+        probabilityAccum = _banditSpawnByRegion[region].probabilityAccum;
+    }
+
+    function activeBanditCount() external view returns (uint32) {
+        return _activeBanditCount;
+    }
+
+    function minSpawnCooldownTicks() external pure returns (uint64) {
+        return MIN_SPAWN_COOLDOWN_TICKS;
+    }
+
+    function spawnProbabilityIncrementBps() external pure returns (uint16) {
+        return BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS;
+    }
+
+    function maxBanditsPerRegion() external pure returns (uint8) {
+        return MAX_BANDITS_PER_REGION;
+    }
+
+    function maxTotalBandits() external pure returns (uint8) {
+        return MAX_TOTAL_BANDITS;
+    }
+
+    function maxBanditSpawnScanPerRegion() external pure returns (uint256) {
+        return MAX_BANDIT_SPAWN_SCAN_PER_REGION;
+    }
+
+    function maxBanditEagerSettleBaseScanPerRegion() external pure returns (uint256) {
+        return MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION;
+    }
+
+    function banditSpawnRoll(bytes32 tickSeed, uint8 region) external pure returns (uint256) {
+        return _banditSpawnRoll(tickSeed, region);
+    }
+}
+
+contract BanditSpawnTest is Test {
+    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
+
+    BanditSpawnHarness world;
+
+    function setUp() public {
+        world = new BanditSpawnHarness();
+    }
+
+    function _advanceTick(BanditSpawnHarness target) internal {
+        vm.warp(target.getWorldState().nextHeartbeatAtTs);
+        target.heartbeat();
+    }
+
+    function _advanceTicks(BanditSpawnHarness target, uint64 ticks) internal {
+        for (uint64 i = 0; i < ticks; i++) {
+            _advanceTick(target);
+        }
+    }
+
+    function _advancePastInitialCooldown(BanditSpawnHarness target) internal {
+        _advanceTicks(target, target.minSpawnCooldownTicks());
+    }
+
+    function _mintForestClan(BanditSpawnHarness target) internal {
+        target.mintClan(address(this));
+    }
+
+    function _mintUntilTwoForestClans(BanditSpawnHarness target) internal returns (uint32 first, uint32 second) {
+        for (uint256 i = 0; i < target.maxBanditEagerSettleBaseScanPerRegion(); i++) {
+            (uint32 clanId,) = target.mintClan(address(this));
+            if (target.getClan(clanId).baseRegion == ClanWorldConstants.REGION_FOREST) {
+                if (first == 0) {
+                    first = clanId;
+                } else {
+                    second = clanId;
+                    return (first, second);
+                }
+            }
+        }
+        revert("missing second forest clan");
+    }
+
+    function _csId(BanditSpawnHarness target, uint32 clanId, uint256 index) internal view returns (uint32) {
+        return target.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
+    }
+
+    function _ordersForPendingWorkerAndDefender(uint32 workerId, uint32 defenderId, uint8 baseRegion)
+        internal
+        pure
+        returns (ClanOrder[] memory orders)
+    {
+        orders = new ClanOrder[](2);
+        orders[0] = ClanOrder({
+            clansmanId: workerId,
+            gotoRegion: ClanWorldConstants.REGION_FOREST,
+            action: ActionType.ChopWood,
+            targetClanId: 0,
+            marketToken: address(0),
+            marketAmount: 0,
+            maxGoldIn: 0
+        });
+        orders[1] = ClanOrder({
+            clansmanId: defenderId,
+            gotoRegion: baseRegion,
+            action: ActionType.DefendBase,
+            targetClanId: 0,
+            marketToken: address(0),
+            marketAmount: 0,
+            maxGoldIn: 0
+        });
+    }
+
+    function _submitPendingWorkerAndDefender(BanditSpawnHarness target, uint32 clanId)
+        internal
+        returns (uint32 workerId, uint32 defenderId)
+    {
+        Clan memory clan = target.getClan(clanId);
+        workerId = _csId(target, clanId, 0);
+        defenderId = _csId(target, clanId, 1);
+
+        OrderResult[] memory results =
+            target.submitClanOrders(clanId, _ordersForPendingWorkerAndDefender(workerId, defenderId, clan.baseRegion));
+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "worker order accepted");
+        assertEq(uint8(results[1].status), uint8(StatusCode.OK), "defender order accepted");
+    }
+
+    function _blockNonForestSpawnRegions(BanditSpawnHarness target) internal {
+        uint64 currentTick = target.getWorldState().currentTick;
+        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
+            if (region != ClanWorldConstants.REGION_FOREST) {
+                target.setBanditSpawnState(region, currentTick, 0);
+            }
+        }
+    }
+
+    function _prevrandaoForNextForestSpawn(BanditSpawnHarness target) internal view returns (bytes32) {
+        WorldState memory state = target.getWorldState();
+        for (uint256 i = 1; i < 256; i++) {
+            bytes32 nextRandao = keccak256(abi.encodePacked("forest-spawn-randao", i));
+            bytes32 nextSeed = keccak256(abi.encode(nextRandao, state.currentTickSeed, state.currentTick));
+            if (target.banditSpawnRoll(nextSeed, ClanWorldConstants.REGION_FOREST) < 8000) {
+                return nextRandao;
+            }
+        }
+        revert("missing forest spawn randao");
+    }
+
+    function _missSeed(uint8 region, uint16 probability) internal view returns (bytes32) {
+        for (uint256 i = 0; i < 256; i++) {
+            bytes32 seed = keccak256(abi.encodePacked("miss", i));
+            if (world.banditSpawnRoll(seed, region) >= probability) {
+                return seed;
+            }
+        }
+        revert("missing miss seed");
+    }
+
+    function _hitBothSeed(uint8 firstRegion, uint8 secondRegion, uint16 probability) internal view returns (bytes32) {
+        for (uint256 i = 0; i < 256; i++) {
+            bytes32 seed = keccak256(abi.encodePacked("hit-both", i));
+            if (
+                world.banditSpawnRoll(seed, firstRegion) < probability
+                    && world.banditSpawnRoll(seed, secondRegion) < probability
+            ) {
+                return seed;
+            }
+        }
+        revert("missing hit seed");
+    }
+
+    function test_cooldownEnforcedAfterSpawn() public {
+        _mintForestClan(world);
+        world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
+
+        _advanceTicks(world, world.minSpawnCooldownTicks() - 1);
+
+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
+        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
+        assertEq(probabilityAccum, 0, "cooldown does not accumulate chance");
+    }
+
+    function test_probabilityRisesAfterCooldown() public {
+        _advancePastInitialCooldown(world);
+        _mintForestClan(world);
+
+        uint16 increment = world.spawnProbabilityIncrementBps();
+        world.evaluateBanditSpawns(_missSeed(ClanWorldConstants.REGION_FOREST, increment));
+
+        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
+        assertEq(probabilityAccum, increment, "first eligible tick increments accumulator");
+    }
+
+    function test_spawnResetsOnlySelectedRegionAccumulator() public {
+        _advancePastInitialCooldown(world);
+        world.mintClan(address(0xF0));
+        world.mintClan(address(0xB0));
+
+        uint16 oneStepBelowCap = 7000;
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, oneStepBelowCap);
+        world.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, oneStepBelowCap);
+
+        world.evaluateBanditSpawns(
+            _hitBothSeed(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_MOUNTAINS, 8000)
+        );
+
+        uint8 selectedRegion = world.getBandit(1).region;
+        uint8 otherRegion = selectedRegion == ClanWorldConstants.REGION_FOREST
+            ? ClanWorldConstants.REGION_MOUNTAINS
+            : ClanWorldConstants.REGION_FOREST;
+        (, uint16 selectedAccum) = world.getBanditSpawnState(selectedRegion);
+        (, uint16 otherAccum) = world.getBanditSpawnState(otherRegion);
+
+        assertEq(selectedAccum, 0, "selected region reset");
+        assertEq(otherAccum, 8000, "unselected candidate retained accum");
+    }
+
+    function test_perRegionCapEnforced() public {
+        _advancePastInitialCooldown(world);
+        _mintForestClan(world);
+
+        uint8 maxPerRegion = world.maxBanditsPerRegion();
+        for (uint8 i = 0; i < maxPerRegion; i++) {
+            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
+        }
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
+
+        world.evaluateBanditSpawns(keccak256("per-region-cap"));
+
+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
+    }
+
+    function test_globalCapEnforced() public {
+        _advancePastInitialCooldown(world);
+        _mintForestClan(world);
+
+        uint8 maxTotal = world.maxTotalBandits();
+        for (uint8 i = 0; i < maxTotal; i++) {
+            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
+        }
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
+
+        world.evaluateBanditSpawns(keccak256("global-cap"));
+
+        assertEq(world.activeBanditCount(), maxTotal, "global cap");
+    }
+
+    function test_globalCapRefreshesPreviewOnHeartbeat() public {
+        _mintForestClan(world);
+
+        uint8 maxTotal = world.maxTotalBandits();
+        for (uint8 i = 0; i < maxTotal; i++) {
+            world.spawnBandit(uint8(ClanWorldConstants.REGION_FOREST + (i % 8)), 100 + i);
+        }
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 4321);
+
+        _advanceTick(world);
+
+        WorldState memory state = world.getWorldState();
+        assertEq(world.activeBanditCount(), maxTotal, "still at cap");
+        assertEq(state.nextBanditSpawnEligibleTick, 0, "no eligible tick while capped");
+        assertEq(state.currentBanditSpawnChanceBps, 4321, "preview chance refreshed");
+    }
+
+    function test_heartbeatCompletesWhenClanCountExceedsBanditSpawnScanCap() public {
+        uint256 clanCount = world.maxBanditSpawnScanPerRegion() + 1;
+        uint160 ownerId = 1;
+        for (uint256 i = 0; i < clanCount; i++) {
+            world.mintClan(address(ownerId));
+            ownerId += 1;
+        }
+
+        _advanceTick(world);
+
+        assertEq(world.getWorldState().currentTick, 1, "heartbeat advanced");
+    }
+
+    function test_heartbeatEagerSettlesCandidateRegionBasesAndDefendersBeforeBanditSpawn() public {
+        _advancePastInitialCooldown(world);
+        (uint32 clanId1, uint32 clanId2) = _mintUntilTwoForestClans(world);
+
+        (uint32 workerId1, uint32 defenderId1) = _submitPendingWorkerAndDefender(world, clanId1);
+        (uint32 workerId2, uint32 defenderId2) = _submitPendingWorkerAndDefender(world, clanId2);
+
+        _blockNonForestSpawnRegions(world);
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, world.getWorldState().currentTick, 0);
+        vm.prevrandao(_prevrandaoForNextForestSpawn(world));
+        _advanceTick(world);
+
+        uint64 closedTick = world.getWorldState().currentTick;
+        assertEq(world.getClan(clanId1).lastSettledTick, closedTick - 1, "clan 1 setup: unsettled");
+        assertEq(world.getClan(clanId2).lastSettledTick, closedTick - 1, "clan 2 setup: unsettled");
+        assertTrue(world.getActiveMission(workerId1).active, "clan 1 worker has pending mission");
+        assertTrue(world.getActiveMission(workerId2).active, "clan 2 worker has pending mission");
+        assertEq(world.getActiveDefenders(clanId1)[0], defenderId1, "clan 1 defender registered");
+        assertEq(world.getActiveDefenders(clanId2)[0], defenderId2, "clan 2 defender registered");
+
+        _blockNonForestSpawnRegions(world);
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
+
+        vm.expectEmit(true, false, false, true);
+        emit ClanSettled(clanId1, closedTick);
+        vm.expectEmit(true, false, false, true);
+        emit ClanSettled(clanId2, closedTick);
+        vm.expectEmit(true, false, false, false);
+        emit BanditSpawned(1, 0, 0, 0);
+
+        _advanceTick(world);
+
+        assertEq(world.getClan(clanId1).lastSettledTick, closedTick, "clan 1 eager-settled");
+        assertEq(world.getClan(clanId2).lastSettledTick, closedTick, "clan 2 eager-settled");
+
+        Mission memory defenderMission1 = world.getActiveMission(defenderId1);
+        Mission memory defenderMission2 = world.getActiveMission(defenderId2);
+        assertTrue(defenderMission1.active, "clan 1 defender remains active");
+        assertTrue(defenderMission2.active, "clan 2 defender remains active");
+        assertEq(uint8(defenderMission1.action), uint8(ActionType.DefendBase), "clan 1 defender action");
+        assertEq(uint8(defenderMission2.action), uint8(ActionType.DefendBase), "clan 2 defender action");
+        assertEq(uint8(world.getClansman(defenderId1).state), uint8(ClansmanState.ACTING), "clan 1 defender settled");
+        assertEq(uint8(world.getClansman(defenderId2).state), uint8(ClansmanState.ACTING), "clan 2 defender settled");
+
+        BanditTroop memory bandit = world.getBandit(1);
+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
+        assertEq(bandit.region, ClanWorldConstants.REGION_FOREST, "forest selected after eager settle");
+        assertEq(bandit.tickEnteredState, closedTick, "spawn used closed tick");
+    }
+
+    function test_regionSelectionDeterministicForSameSeed() public {
+        BanditSpawnHarness a = new BanditSpawnHarness();
+        BanditSpawnHarness b = new BanditSpawnHarness();
+        _advancePastInitialCooldown(a);
+        _advancePastInitialCooldown(b);
+        a.mintClan(address(0xA11CE));
+        a.mintClan(address(0xB0B));
+        b.mintClan(address(0xA11CE));
+        b.mintClan(address(0xB0B));
+        a.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
+        a.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
+        b.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
+        b.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
+
+        bytes32 seed = keccak256("deterministic-region");
+        a.evaluateBanditSpawns(seed);
+        b.evaluateBanditSpawns(seed);
+
+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
+    }
+
+    function test_rngNoncePerRegionIsIndependent() public view {
+        bytes32 seed = keccak256("region-nonce");
+
+        uint256 forestRoll = world.banditSpawnRoll(seed, ClanWorldConstants.REGION_FOREST);
+        uint256 mountainRoll = world.banditSpawnRoll(seed, ClanWorldConstants.REGION_MOUNTAINS);
+
+        assertTrue(forestRoll != mountainRoll, "region nonce changes roll");
+    }
+
+    function test_spawnedBanditEntersStateMachine() public {
+        _advancePastInitialCooldown(world);
+        _mintForestClan(world);
+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
+
+        world.evaluateBanditSpawns(keccak256("spawn-state-machine"));
+
+        BanditTroop memory bandit = world.getBandit(1);
+        WorldState memory state = world.getWorldState();
+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
+        assertEq(bandit.tickEnteredState, state.currentTick, "entered current tick");
+        assertEq(state.activeBanditId, bandit.id, "active bandit set");
+    }
+}
diff --git a/packages/contracts/test/ClanWorld.t.sol b/packages/contracts/test/ClanWorld.t.sol
index 92781d3..d966b47 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -48,6 +48,10 @@ contract ClanWorldTestHarness is ClanWorld {
     function killClansman(uint32 csId) external {
         _clansmen[csId].state = ClansmanState.DEAD;
     }
+
+    function setLastSettledTickForTest(uint32 clanId, uint64 lastSettledTick) external {
+        _clans[clanId].lastSettledTick = lastSettledTick;
+    }
 }
 
 contract ClanWorldTest is Test {
@@ -68,7 +72,7 @@ contract ClanWorldTest is Test {
     StubPool fishPool;
 
     function setUp() public {
-        world = new ClanWorld();
+        world = new ClanWorldTestHarness();
     }
 
     /// @dev Deploy tokens + pools, call initTreasury + seedPools. Returns wood token address.
@@ -125,6 +129,12 @@ contract ClanWorldTest is Test {
         world.heartbeat();
     }
 
+    function _advanceTicks(uint256 count) internal {
+        for (uint256 i = 0; i < count; i++) {
+            _advanceTick();
+        }
+    }
+
     function _mintClan() internal returns (uint32 clanId) {
         vm.prank(elder);
         (clanId,) = world.mintClan(elder);
@@ -282,6 +292,87 @@ contract ClanWorldTest is Test {
         }
     }
 
+    function test_issue230_derivedClanState_simulatesStarvationWithoutMutatingRaw() public {
+        uint32 clanId = _mintClan();
+
+        _advanceTicks(6);
+
+        DerivedClanState memory derived = world.getDerivedClanState(clanId);
+        assertEq(derived.derivedAtTick, 6, "derived tick");
+        assertTrue(derived.isStarving, "derived clan should be starving");
+        assertEq(derived.clan.lastSettledTick, 6, "derived clan settled to current tick");
+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
+        assertEq(derived.clan.vaultWheat, 0, "simulated wheat spent");
+        assertEq(derived.clan.vaultFish, 0, "simulated fish spent");
+
+        Clan memory raw = world.getClan(clanId);
+        assertEq(raw.lastSettledTick, 0, "raw settlement checkpoint unchanged");
+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
+        assertEq(raw.vaultWheat, 20e18, "raw wheat unchanged");
+        assertEq(raw.vaultFish, 2e18, "raw fish unchanged");
+    }
+
+    function test_issue230_derivedClansmanState_simulatesArrivalWithoutMutatingRaw() public {
+        uint32 clanId = _mintClan();
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
+
+        _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);
+        _advanceTicks(2);
+
+        DerivedClansmanState memory derived = world.getDerivedClansmanState(csId);
+        assertEq(uint8(derived.clansman.state), uint8(ClansmanState.ACTING), "derived clansman arrived");
+        assertEq(derived.clansman.currentRegion, ClanWorldConstants.REGION_MOUNTAINS, "derived current region");
+        assertEq(derived.effectiveRegion, ClanWorldConstants.REGION_MOUNTAINS, "derived effective region");
+        assertTrue(derived.activeMission.active, "mission remains active before settle tick");
+
+        Clansman memory raw = world.getClansman(csId);
+        assertEq(uint8(raw.state), uint8(ClansmanState.TRAVELING), "raw clansman remains unmutated");
+        assertEq(raw.currentRegion, view_.clan.clan.baseRegion, "raw current region unchanged");
+    }
+
+    function test_issue230_fullViewAndLootQuoteUseSimulationButRawStaysCommitted() public {
+        uint32 clanId = _mintClan();
+
+        _advanceTicks(6);
+
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        assertTrue(view_.clan.isStarving, "full view clan should be starving");
+        assertEq(view_.clan.clan.vaultWheat, 0, "full view simulated wheat spent");
+        assertEq(view_.clan.clan.vaultFish, 0, "full view simulated fish spent");
+        assertEq(world.quoteLootValueSettled(clanId), 20e18, "settled loot quote uses simulated vault");
+        assertEq(world.quoteLootValueRaw(clanId), 44e18, "raw loot quote remains committed storage");
+
+        Clan memory raw = world.getClan(clanId);
+        assertEq(raw.lastSettledTick, 0, "full view did not settle raw clan");
+        assertEq(raw.vaultWheat, 20e18, "full view did not mutate raw wheat");
+        assertEq(raw.vaultFish, 2e18, "full view did not mutate raw fish");
+    }
+
+    function test_issue230_multipleClansSimulateIndependentlyFromDifferentCheckpoints() public {
+        uint32 clanId1 = _mintClan();
+        vm.prank(elder2);
+        (uint32 clanId2,) = world.mintClan(elder2);
+
+        _advanceTicks(2);
+        world.settleClan(clanId1);
+        _advanceTicks(4);
+
+        DerivedClanState memory derived1 = world.getDerivedClanState(clanId1);
+        DerivedClanState memory derived2 = world.getDerivedClanState(clanId2);
+        assertTrue(derived1.isStarving, "clan 1 simulated from later checkpoint");
+        assertTrue(derived2.isStarving, "clan 2 simulated from genesis checkpoint");
+        assertEq(derived1.clan.lastSettledTick, 6, "clan 1 derived to current");
+        assertEq(derived2.clan.lastSettledTick, 6, "clan 2 derived to current");
+
+        Clan memory raw1 = world.getClan(clanId1);
+        Clan memory raw2 = world.getClan(clanId2);
+        assertEq(raw1.lastSettledTick, 2, "clan 1 raw checkpoint unchanged after derived reads");
+        assertEq(raw2.lastSettledTick, 0, "clan 2 raw checkpoint unchanged after derived reads");
+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
+    }
+
     // -------------------------------------------------------------------------
     // Test 6: submitOrders — per-order failure (bad clansmanId) doesn't revert
     // -------------------------------------------------------------------------
@@ -492,14 +583,14 @@ contract ClanWorldTest is Test {
     // -------------------------------------------------------------------------
 
     function test_submitClanOrders_reverts_when_clan_too_far_behind() public {
+        while (world.getWorldState().currentTick <= 200) {
+            _advanceTick();
+        }
+
         uint32 clanId = _mintClan();
         ClanFullView memory view_ = world.getClanFullView(clanId);
         uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
-
-        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
-        for (uint256 i = 0; i < 201; i++) {
-            _advanceTick();
-        }
+        ClanWorldTestHarness(address(world)).setLastSettledTickForTest(clanId, 0);
 
         // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
         // without reverting, for every order in the batch
@@ -1814,7 +1905,11 @@ contract ClanWorldTest is Test {
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
@@ -1921,8 +2016,7 @@ contract ClanWorldTest is Test {
         assertEq(ws.seasonStartTick, 0);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
         assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
         );
         assertFalse(ws.winterActive);
     }
@@ -1931,8 +2025,7 @@ contract ClanWorldTest is Test {
         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
         // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
-        uint64 winterStart =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
         for (uint64 i = 0; i < winterStart - 1; i++) {
             vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
             world.heartbeat();
diff --git a/packages/contracts/test/ClanWorldStub.t.sol b/packages/contracts/test/ClanWorldStub.t.sol
index 4625795..d318a25 100644
--- a/packages/contracts/test/ClanWorldStub.t.sol
+++ b/packages/contracts/test/ClanWorldStub.t.sol
@@ -3,7 +3,7 @@ pragma solidity ^0.8.34;
 
 import {Test} from "forge-std/Test.sol";
 import {ClanWorldStub} from "../src/ClanWorldStub.sol";
-import {ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
+import {BanditState, BanditTroop, ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
 import {MinimalERC20} from "../src/MinimalERC20.sol";
 import {StubPool} from "../src/StubPool.sol";
 
@@ -35,6 +35,13 @@ contract ClanWorldStubTest is Test {
         assertEq(stub.getWorldSnapshot().currentTick, 2);
     }
 
+    function test_getBanditMissingIdReturnsNoneState() public view {
+        BanditTroop memory bandit = stub.getBandit(999);
+
+        assertEq(bandit.id, 0, "missing id");
+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
+    }
+
     function test_initial_timer_fields_match_ClanWorld() public {
         WorldState memory ws = stub.getWorldState();
 
diff --git a/packages/contracts/test/HeartbeatOrdering.t.sol b/packages/contracts/test/HeartbeatOrdering.t.sol
index 9d012e8..6871f90 100644
--- a/packages/contracts/test/HeartbeatOrdering.t.sol
+++ b/packages/contracts/test/HeartbeatOrdering.t.sol
@@ -36,6 +36,72 @@ contract HeartbeatOrderingHarness is ClanWorld {
     }
 }
 
+contract RecordingPool {
+    address public immutable TOKEN_A;
+    address public immutable TOKEN_B;
+    address public immutable ENGINE;
+
+    uint256 public reserveA;
+    uint256 public reserveB;
+    uint64 public observedTick;
+    bytes32 public observedSeed;
+    bool public observedSell;
+    bool private _seeded;
+
+    modifier onlyEngine() {
+        require(msg.sender == ENGINE, "RecordingPool: only engine");
+        _;
+    }
+
+    constructor(address tokenA_, address tokenB_, address engine_) {
+        TOKEN_A = tokenA_;
+        TOKEN_B = tokenB_;
+        ENGINE = engine_;
+    }
+
+    function seed(uint256 amountA, uint256 amountB) external onlyEngine {
+        require(!_seeded, "RecordingPool: already seeded");
+        require(amountA > 0 && amountB > 0, "RecordingPool: zero seed");
+        reserveA = amountA;
+        reserveB = amountB;
+        _seeded = true;
+    }
+
+    function sellResource(uint256 amountIn) external onlyEngine returns (uint256 goldOut) {
+        require(amountIn > 0, "RecordingPool: zero amount");
+        require(reserveA > 0 && reserveB > 0, "RecordingPool: not seeded");
+
+        WorldState memory state = HeartbeatOrderingHarness(ENGINE).getWorldState();
+        observedTick = state.currentTick;
+        observedSeed = state.currentTickSeed;
+        observedSell = true;
+
+        goldOut = (reserveB * amountIn) / (reserveA + amountIn);
+        require(goldOut > 0, "RecordingPool: zero output");
+        reserveA += amountIn;
+        reserveB -= goldOut;
+    }
+
+    function quoteBuy(uint256 amountOut) external view returns (uint256 goldIn) {
+        require(amountOut > 0, "RecordingPool: zero amount");
+        require(amountOut < reserveA, "RecordingPool: insufficient resource reserve");
+        uint256 num = reserveB * amountOut;
+        uint256 denom_ = reserveA - amountOut;
+        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
+    }
+
+    function buyResource(uint256 amountOut) external onlyEngine returns (uint256 goldIn) {
+        require(amountOut > 0, "RecordingPool: zero amount");
+        require(amountOut < reserveA, "RecordingPool: insufficient resource reserve");
+        require(reserveB > 0, "RecordingPool: not seeded");
+        uint256 num = reserveB * amountOut;
+        uint256 denom_ = reserveA - amountOut;
+        goldIn = num / denom_ + (num % denom_ == 0 ? 0 : 1);
+        reserveA -= amountOut;
+        reserveB += goldIn;
+    }
+}
+
 contract HeartbeatOrderingTest is Test {
     HeartbeatOrderingHarness world;
     address elder = address(0xA1);
@@ -51,6 +117,7 @@ contract HeartbeatOrderingTest is Test {
     StubPool ironPool;
     StubPool wheatPool;
     StubPool fishPool;
+    RecordingPool recordingWoodPool;
 
     function setUp() public {
         world = new HeartbeatOrderingHarness();
@@ -164,6 +231,46 @@ contract HeartbeatOrderingTest is Test {
         world.seedPools(cfg);
     }
 
+    function _setupRecordingMarket() internal {
+        woodToken = new MinimalERC20("Wood", "WOOD");
+        ironToken = new MinimalERC20("Iron", "IRON");
+        wheatToken = new MinimalERC20("Wheat", "WHEAT");
+        fishToken = new MinimalERC20("Fish", "FISH");
+        goldToken = new MinimalERC20("Gold", "GOLD");
+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
+
+        address wAddr = address(world);
+        recordingWoodPool = new RecordingPool(address(woodToken), address(goldToken), wAddr);
+        ironPool = new StubPool(address(ironToken), address(goldToken), wAddr);
+        wheatPool = new StubPool(address(wheatToken), address(goldToken), wAddr);
+        fishPool = new StubPool(address(fishToken), address(goldToken), wAddr);
+
+        address[6] memory tokens = [
+            address(woodToken),
+            address(ironToken),
+            address(wheatToken),
+            address(fishToken),
+            address(goldToken),
+            address(blueprintToken)
+        ];
+        address[4] memory pools = [address(recordingWoodPool), address(wheatPool), address(fishPool), address(ironPool)];
+        world.initTreasury(tokens, pools);
+
+        uint256 resSeed = 1000e18;
+        uint256 goldSeed = 1000e18;
+        PoolSeedConfig memory cfg = PoolSeedConfig({
+            woodSeed: resSeed,
+            wheatSeed: resSeed,
+            fishSeed: resSeed,
+            ironSeed: resSeed,
+            goldSeedForWood: goldSeed,
+            goldSeedForWheat: goldSeed,
+            goldSeedForFish: goldSeed,
+            goldSeedForIron: goldSeed
+        });
+        world.seedPools(cfg);
+    }
+
     // -------------------------------------------------------------------------
     // test_heartbeat_settlementBeforeMarket
     //
@@ -252,6 +359,33 @@ contract HeartbeatOrderingTest is Test {
         assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
     }
 
+    function test_heartbeat_scheduledMarketObservesClosedTickSeedBeforeIncrement() public {
+        _setupRecordingMarket();
+        uint32 clanId = _mintClan();
+        uint32 csId = _firstCs(clanId);
+
+        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, address(woodToken), 5e18, 0);
+        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
+
+        Mission memory m = world.getActiveMission(csId);
+        uint64 executeAtTick = m.actionStartTick;
+        _advanceToTick(executeAtTick);
+
+        WorldState memory beforeClose = world.getWorldState();
+        assertEq(beforeClose.currentTick, executeAtTick, "setup must be at execute tick before close");
+        bytes32 seedForClosedTick = beforeClose.currentTickSeed;
+
+        _advanceTick();
+
+        assertTrue(recordingWoodPool.observedSell(), "scheduled sell must execute");
+        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
+        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
+
+        WorldState memory afterClose = world.getWorldState();
+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
+        assertNotEq(afterClose.currentTickSeed, seedForClosedTick, "next tick seed publishes after close-tick work");
+    }
+
     // -------------------------------------------------------------------------
     // test_heartbeat_seasonTransition
     //
diff --git a/packages/runner/src/runnerCastHeartbeat.ts b/packages/runner/src/runnerCastHeartbeat.ts
index 125904b..4a99b6c 100644
--- a/packages/runner/src/runnerCastHeartbeat.ts
+++ b/packages/runner/src/runnerCastHeartbeat.ts
@@ -4,57 +4,19 @@ import {
   createWalletClient,
   http,
   type Account,
+  type Abi,
   type PublicClient,
   type WalletClient,
 } from 'viem';
 import { privateKeyToAccount } from 'viem/accounts';
+import IClanWorldArtifact from '../../contracts/abi/IClanWorld.json';
 import { baseSepolia } from '@clan-world/shared/adapters';
 import {
   HeartbeatRateLimitedError,
   type IHeartbeatCaller,
 } from '@clan-world/agents/seams';
 
-/**
- * Minimal ABI: only the `heartbeat()` write and the `nextHeartbeatAtTs` field
- * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
- * ABI here so the runner stays decoupled from contract-package versioning.
- */
-const HEARTBEAT_ABI = [
-  {
-    type: 'function',
-    name: 'heartbeat',
-    inputs: [],
-    outputs: [],
-    stateMutability: 'nonpayable',
-  },
-  {
-    type: 'function',
-    name: 'getWorldState',
-    inputs: [],
-    outputs: [
-      {
-        name: '',
-        type: 'tuple',
-        components: [
-          { name: 'currentTick', type: 'uint64' },
-          { name: 'seasonStartTick', type: 'uint64' },
-          { name: 'seasonEndTick', type: 'uint64' },
-          { name: 'seasonFinalized', type: 'bool' },
-          { name: 'nextHeartbeatAtTs', type: 'uint64' },
-          { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
-          { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
-          { name: 'currentTickSeed', type: 'bytes32' },
-          { name: 'activeBanditId', type: 'uint32' },
-          { name: 'winterActive', type: 'bool' },
-          { name: 'winterStartsAtTick', type: 'uint64' },
-          { name: 'winterEndsAtTick', type: 'uint64' },
-          { name: 'nextCommitSequence', type: 'uint64' },
-        ],
-      },
-    ],
-    stateMutability: 'view',
-  },
-] as const;
+export const HEARTBEAT_ABI = IClanWorldArtifact.abi as Abi;
 
 export interface RunnerHeartbeatConfig {
   /** Hex-encoded 64-char private key, optionally 0x-prefixed. */
diff --git a/packages/shared/package.json b/packages/shared/package.json
index 6519a84..b9d4558 100644
--- a/packages/shared/package.json
+++ b/packages/shared/package.json
@@ -11,12 +11,14 @@
   },
   "scripts": {
     "build": "tsc -b",
+    "test": "vitest run",
     "typecheck": "tsc --noEmit",
     "lint": "echo 'lint stub'",
     "clean": "rm -rf dist .turbo"
   },
   "devDependencies": {
-    "typescript": "5.9.3"
+    "typescript": "5.9.3",
+    "vitest": "^3.2.0"
   },
   "dependencies": {
     "convex": "1.17.4",
diff --git a/packages/shared/src/adapters/IChainClient.ts b/packages/shared/src/adapters/IChainClient.ts
index bf97929..aa4c6b4 100644
--- a/packages/shared/src/adapters/IChainClient.ts
+++ b/packages/shared/src/adapters/IChainClient.ts
@@ -1,7 +1,9 @@
 import fs from 'node:fs';
 import { createPublicClient, createWalletClient, http, fallback, defineChain } from 'viem';
 import { privateKeyToAccount } from 'viem/accounts';
+import IClanWorldArtifact from '../../../contracts/abi/IClanWorld.json';
 import type { ClanFullView, ClanOrder, Tick } from '../types';
+import type { Abi } from 'viem';
 import { readEnv } from './_env';
 
 export interface IChainClient {
@@ -21,221 +23,7 @@ export const baseSepolia = defineChain({
   },
 });
 
-// Minimal ABI — only the two read functions we call.
-const CLAN_WORLD_ABI = [
-  {
-    type: 'function',
-    name: 'getWorldSnapshot',
-    inputs: [],
-    outputs: [
-      {
-        name: '',
-        type: 'tuple',
-        components: [
-          { name: 'currentTick', type: 'uint64' },
-          { name: 'seasonStartTick', type: 'uint64' },
-          { name: 'seasonEndTick', type: 'uint64' },
-          { name: 'seasonFinalized', type: 'bool' },
-          { name: 'winterActive', type: 'bool' },
-          { name: 'winterStartsAtTick', type: 'uint64' },
-          { name: 'winterEndsAtTick', type: 'uint64' },
-          { name: 'activeBanditId', type: 'uint32' },
-          { name: 'currentTickSeed', type: 'bytes32' },
-          {
-            name: 'leaderboard',
-            type: 'tuple[]',
-            components: [
-              { name: 'clanId', type: 'uint32' },
-              { name: 'owner', type: 'address' },
-              { name: 'monumentLevel', type: 'uint8' },
-              { name: 'baseLevel', type: 'uint8' },
-              { name: 'wallLevel', type: 'uint8' },
-              { name: 'livingClansmen', type: 'uint8' },
-              { name: 'state', type: 'uint8' },
-              { name: 'lootValue', type: 'uint256' },
-            ],
-          },
-        ],
-      },
-    ],
-    stateMutability: 'view',
-  },
-  {
-    type: 'function',
-    name: 'getClanFullView',
-    inputs: [{ name: '', type: 'uint32' }],
-    outputs: [
-      {
-        name: '',
-        type: 'tuple',
-        components: [
-          {
-            name: 'clan',
-            type: 'tuple',
-            components: [
-              {
-                name: 'clan',
-                type: 'tuple',
-                components: [
-                  { name: 'clanId', type: 'uint32' },
-                  { name: 'iftTokenId', type: 'uint256' },
-                  { name: 'owner', type: 'address' },
-                  { name: 'clanState', type: 'uint8' },
-                  { name: 'baseRegion', type: 'uint8' },
-                  { name: 'baseLevel', type: 'uint8' },
-                  { name: 'wallLevel', type: 'uint8' },
-                  { name: 'monumentLevel', type: 'uint8' },
-                  { name: 'livingClansmen', type: 'uint8' },
-                  { name: 'lastSettledTick', type: 'uint64' },
-                  { name: 'starvationStartsAtTick', type: 'uint64' },
-                  { name: 'coldDamage', type: 'uint16' },
-                  { name: 'goldBalance', type: 'uint256' },
-                  { name: 'blueprintBalance', type: 'uint256' },
-                  { name: 'vaultWood', type: 'uint256' },
-                  { name: 'vaultIron', type: 'uint256' },
-                  { name: 'vaultWheat', type: 'uint256' },
-                  { name: 'vaultFish', type: 'uint256' },
-                ],
-              },
-              { name: 'isStarving', type: 'bool' },
-              { name: 'lootValue', type: 'uint256' },
-              { name: 'derivedAtTick', type: 'uint64' },
-            ],
-          },
-          // clansmen, westPlot, eastPlot etc. omitted — not needed for Wave 0 mapping
-          {
-            name: 'clansmen',
-            type: 'tuple[]',
-            components: [
-              {
-                name: 'clansman',
-                type: 'tuple',
-                components: [
-                  {
-                    name: 'clansman',
-                    type: 'tuple',
-                    components: [
-                      { name: 'clansmanId', type: 'uint32' },
-                      { name: 'clanId', type: 'uint32' },
-                      { name: 'state', type: 'uint8' },
-                      { name: 'currentRegion', type: 'uint8' },
-                      { name: 'cooldownEndsAtTs', type: 'uint64' },
-                      { name: 'lastMissionNonce', type: 'uint64' },
-                      { name: 'carryWood', type: 'uint256' },
-                      { name: 'carryIron', type: 'uint256' },
-                      { name: 'carryWheat', type: 'uint256' },
-                      { name: 'carryFish', type: 'uint256' },
-                    ],
-                  },
-                  {
-                    name: 'activeMission',
-                    type: 'tuple',
-                    components: [
-                      { name: 'active', type: 'bool' },
-                      { name: 'nonce', type: 'uint64' },
-                      { name: 'clansmanId', type: 'uint32' },
-                      { name: 'startRegion', type: 'uint8' },
-                      { name: 'targetRegion', type: 'uint8' },
-                      { name: 'action', type: 'uint8' },
-                      { name: 'startTick', type: 'uint64' },
-                      { name: 'arrivalTick', type: 'uint64' },
-                      { name: 'actionStartTick', type: 'uint64' },
-                      { name: 'missionSeed', type: 'bytes32' },
-                      { name: 'marketMode', type: 'uint8' },
-                      { name: 'targetClanId', type: 'uint32' },
-                      { name: 'marketToken', type: 'address' },
-                      { name: 'marketAmount', type: 'uint256' },
-                      { name: 'maxGoldIn', type: 'uint256' },
-                    ],
-                  },
-                  { name: 'effectiveRegion', type: 'uint8' },
-                  { name: 'derivedAtTick', type: 'uint64' },
-                ],
-              },
-              {
-                name: 'activeMission',
-                type: 'tuple',
-                components: [
-                  { name: 'active', type: 'bool' },
-                  { name: 'nonce', type: 'uint64' },
-                  { name: 'clansmanId', type: 'uint32' },
-                  { name: 'startRegion', type: 'uint8' },
-                  { name: 'targetRegion', type: 'uint8' },
-                  { name: 'action', type: 'uint8' },
-                  { name: 'startTick', type: 'uint64' },
-                  { name: 'arrivalTick', type: 'uint64' },
-                  { name: 'actionStartTick', type: 'uint64' },
-                  { name: 'missionSeed', type: 'bytes32' },
-                  { name: 'marketMode', type: 'uint8' },
-                  { name: 'targetClanId', type: 'uint32' },
-                  { name: 'marketToken', type: 'address' },
-                  { name: 'marketAmount', type: 'uint256' },
-                  { name: 'maxGoldIn', type: 'uint256' },
-                ],
-              },
-            ],
-          },
-          {
-            name: 'westPlot',
-            type: 'tuple',
-            components: [
-              { name: 'state', type: 'uint8' },
-              { name: 'region', type: 'uint8' },
-              { name: 'remainingWheat', type: 'uint256' },
-              { name: 'regrowUntilTick', type: 'uint64' },
-            ],
-          },
-          {
-            name: 'eastPlot',
-            type: 'tuple',
-            components: [
-              { name: 'state', type: 'uint8' },
-              { name: 'region', type: 'uint8' },
-              { name: 'remainingWheat', type: 'uint256' },
-              { name: 'regrowUntilTick', type: 'uint64' },
-            ],
-          },
-          { name: 'incomingDefenderIds', type: 'uint32[]' },
-          { name: 'thisClanDefendingBaseId', type: 'uint32' },
-        ],
-      },
-    ],
-    stateMutability: 'view',
-  },
-  {
-    name: 'submitClanOrders',
-    type: 'function',
-    inputs: [
-      { name: 'clanId', type: 'uint32' },
-      {
-        name: 'orders',
-        type: 'tuple[]',
-        components: [
-          { name: 'clansmanId', type: 'uint32' },
-          { name: 'gotoRegion', type: 'uint8' },
-          { name: 'action', type: 'uint8' },
-          { name: 'targetClanId', type: 'uint32' },
-          { name: 'marketToken', type: 'address' },
-          { name: 'marketAmount', type: 'uint256' },
-          { name: 'maxGoldIn', type: 'uint256' },
-        ],
-      },
-    ],
-    outputs: [
-      {
-        name: 'results',
-        type: 'tuple[]',
-        components: [
-          { name: 'clansmanId', type: 'uint32' },
-          { name: 'status', type: 'uint8' },
-          { name: 'cooldownEndsAtTs', type: 'uint64' },
-          { name: 'missionNonce', type: 'uint64' },
-        ],
-      },
-    ],
-    stateMutability: 'nonpayable',
-  },
-] as const;
+export const CLAN_WORLD_ABI = IClanWorldArtifact.abi as Abi;
 
 class StubChainClient implements IChainClient {
   async getCurrentTick(): Promise<Tick> {
@@ -282,7 +70,7 @@ class RealChainClient implements IChainClient {
       address: this.contractAddress,
       abi: CLAN_WORLD_ABI,
       functionName: 'getWorldSnapshot',
-    });
+    }) as { currentTick: bigint };
     return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
   }
 
@@ -380,7 +168,14 @@ class RealChainClient implements IChainClient {
       abi: CLAN_WORLD_ABI,
       functionName: 'getClanFullView',
       args: [parseInt(clanId, 10)],
-    });
+    }) as {
+      clan: {
+        clan: {
+          clanId: number;
+          goldBalance: bigint;
+        };
+      };
+    };
 
     const inner = result.clan.clan;
     return {
diff --git a/packages/shared/test/clanWorldAbi.test.ts b/packages/shared/test/clanWorldAbi.test.ts
new file mode 100644
index 0000000..77d203f
--- /dev/null
+++ b/packages/shared/test/clanWorldAbi.test.ts
@@ -0,0 +1,126 @@
+import { describe, expect, it } from 'vitest';
+import { decodeFunctionResult, encodeAbiParameters } from 'viem';
+import { CLAN_WORLD_ABI } from '../src/adapters/IChainClient';
+
+const ZERO_BYTES32 = `0x${'00'.repeat(32)}` as `0x${string}`;
+
+const worldStateTuple = [
+  {
+    type: 'tuple',
+    components: [
+      { name: 'currentTick', type: 'uint64' },
+      { name: 'seasonStartTick', type: 'uint64' },
+      { name: 'seasonEndTick', type: 'uint64' },
+      { name: 'seasonFinalized', type: 'bool' },
+      { name: 'currentSeasonNumber', type: 'uint64' },
+      { name: 'nextHeartbeatAtTick', type: 'uint64' },
+      { name: 'nextHeartbeatAtTs', type: 'uint64' },
+      { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
+      { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
+      { name: 'currentTickSeed', type: 'bytes32' },
+      { name: 'activeBanditId', type: 'uint32' },
+      { name: 'winterActive', type: 'bool' },
+      { name: 'winterStartsAtTick', type: 'uint64' },
+      { name: 'winterEndsAtTick', type: 'uint64' },
+      { name: 'nextCommitSequence', type: 'uint64' },
+    ],
+  },
+] as const;
+
+const worldSnapshotTuple = [
+  {
+    type: 'tuple',
+    components: [
+      { name: 'currentTick', type: 'uint64' },
+      { name: 'seasonStartTick', type: 'uint64' },
+      { name: 'seasonEndTick', type: 'uint64' },
+      { name: 'seasonFinalized', type: 'bool' },
+      { name: 'currentSeasonNumber', type: 'uint64' },
+      { name: 'nextHeartbeatAtTick', type: 'uint64' },
+      { name: 'winterActive', type: 'bool' },
+      { name: 'winterStartsAtTick', type: 'uint64' },
+      { name: 'winterEndsAtTick', type: 'uint64' },
+      { name: 'activeBanditId', type: 'uint32' },
+      { name: 'currentTickSeed', type: 'bytes32' },
+      {
+        name: 'leaderboard',
+        type: 'tuple[]',
+        components: [
+          { name: 'clanId', type: 'uint32' },
+          { name: 'owner', type: 'address' },
+          { name: 'monumentLevel', type: 'uint8' },
+          { name: 'baseLevel', type: 'uint8' },
+          { name: 'wallLevel', type: 'uint8' },
+          { name: 'livingClansmen', type: 'uint8' },
+          { name: 'state', type: 'uint8' },
+          { name: 'lootValue', type: 'uint256' },
+        ],
+      },
+    ],
+  },
+] as const;
+
+describe('ClanWorld generated ABI tuple decoding', () => {
+  it('decodes a known-good getWorldState() result with the current Solidity order', () => {
+    const data = encodeAbiParameters(worldStateTuple, [
+      {
+        currentTick: 11n,
+        seasonStartTick: 1n,
+        seasonEndTick: 360n,
+        seasonFinalized: false,
+        currentSeasonNumber: 7n,
+        nextHeartbeatAtTick: 12n,
+        nextHeartbeatAtTs: 1_900_000_001n,
+        nextBanditSpawnEligibleTick: 21n,
+        currentBanditSpawnChanceBps: 3000,
+        currentTickSeed: ZERO_BYTES32,
+        activeBanditId: 42,
+        winterActive: true,
+        winterStartsAtTick: 100n,
+        winterEndsAtTick: 110n,
+        nextCommitSequence: 9n,
+      },
+    ]);
+
+    const state = decodeFunctionResult({
+      abi: CLAN_WORLD_ABI,
+      functionName: 'getWorldState',
+      data,
+    }) as Record<string, unknown>;
+
+    expect(state.currentSeasonNumber).toBe(7n);
+    expect(state.nextHeartbeatAtTick).toBe(12n);
+    expect(state.nextHeartbeatAtTs).toBe(1_900_000_001n);
+    expect(state.winterActive).toBe(true);
+  });
+
+  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
+    const data = encodeAbiParameters(worldSnapshotTuple, [
+      {
+        currentTick: 33n,
+        seasonStartTick: 0n,
+        seasonEndTick: 360n,
+        seasonFinalized: false,
+        currentSeasonNumber: 2n,
+        nextHeartbeatAtTick: 34n,
+        winterActive: true,
+        winterStartsAtTick: 100n,
+        winterEndsAtTick: 110n,
+        activeBanditId: 3,
+        currentTickSeed: ZERO_BYTES32,
+        leaderboard: [],
+      },
+    ]);
+
+    const snapshot = decodeFunctionResult({
+      abi: CLAN_WORLD_ABI,
+      functionName: 'getWorldSnapshot',
+      data,
+    }) as Record<string, unknown>;
+
+    expect(snapshot.currentSeasonNumber).toBe(2n);
+    expect(snapshot.nextHeartbeatAtTick).toBe(34n);
+    expect(snapshot.winterActive).toBe(true);
+    expect(snapshot.activeBanditId).toBe(3);
+  });
+});
diff --git a/packages/shared/tsconfig.json b/packages/shared/tsconfig.json
index 25e8cea..f9f9e67 100644
--- a/packages/shared/tsconfig.json
+++ b/packages/shared/tsconfig.json
@@ -3,5 +3,5 @@
   "compilerOptions": {
     "noEmit": true
   },
-  "include": ["src/**/*"]
+  "include": ["src/**/*", "test/**/*"]
 }
diff --git a/pnpm-lock.yaml b/pnpm-lock.yaml
index 09dc3ad..c12d415 100644
--- a/pnpm-lock.yaml
+++ b/pnpm-lock.yaml
@@ -191,6 +191,9 @@ importers:
       typescript:
         specifier: 5.9.3
         version: 5.9.3
+      vitest:
+        specifier: ^3.2.0
+        version: 3.2.4(@types/node@25.6.0)(lightningcss@1.32.0)
 
 packages:
 
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
  → Inspect changed Solidity bandit/heartbeat/starvation code in context and identify behavioral regressions
  • Inspect ABI + TypeScript client/runner changes for decode/order mismatches
  • Inspect and assess new tests for coverage gaps or invalid assumptions
  • Synthesize findings into requested review format
codex
I’ve got the review broken into three focused lanes now: contract lifecycle, ABI/off-chain decode, and test validity/coverage. While those run in parallel, I’m reading the exact heartbeat and settlement paths in the main tree to cross-check the diff against adjacent logic.
2026-04-29T17:33:26.325069Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T17:33:26.337365Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T17:33:26.348341Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
exec
/bin/bash -lc 'pwd && rg -n "function heartbeat|_advanceBanditStates|_resolveAttackingBandits|_resolveBanditAttack|_simulateResolveAction|_simulateApplyUpkeep|_applyUpkeep|getWorldSnapshot|struct WorldSnapshot|enum BanditState|struct BanditTroop|BlueprintAwarded|BlueprintEarned|BanditAttackResolved|getActiveDefenders|settleClansman" packages/contracts/src packages/shared/src packages/runner/src' in /home/claude/code/clan-world
 succeeded in 0ms:
/home/claude/code/omnipass-world/clan-world
packages/shared/src/adapters/IChainClient.ts:28:    name: 'getWorldSnapshot',
packages/shared/src/adapters/IChainClient.ts:284:      functionName: 'getWorldSnapshot',
packages/contracts/src/IClanWorld.sol:107:enum BanditState {
packages/contracts/src/IClanWorld.sol:303:struct BanditTroop {
packages/contracts/src/IClanWorld.sol:407:struct WorldSnapshot {
packages/contracts/src/IClanWorld.sol:585:    event BanditAttackResolved(
packages/contracts/src/IClanWorld.sol:603:    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
packages/contracts/src/IClanWorld.sol:604:    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
packages/contracts/src/IClanWorld.sol:656:    function heartbeat() external;
packages/contracts/src/IClanWorld.sol:662:    function settleClansman(uint32 csId) external;
packages/contracts/src/IClanWorld.sol:744:    function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds);
packages/contracts/src/IClanWorld.sol:779:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
packages/contracts/src/ClanWorldStub.sol:78:    function heartbeat() external override {
packages/contracts/src/ClanWorldStub.sol:88:    function settleClansman(uint32) external override {}
packages/contracts/src/ClanWorldStub.sol:251:    function getActiveDefenders(uint32) external pure override returns (uint32[] memory) {
packages/contracts/src/ClanWorldStub.sol:297:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
packages/contracts/src/ClanWorld.sol:342:    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
packages/contracts/src/ClanWorld.sol:417:            _applyUpkeep(clan, tick);
packages/contracts/src/ClanWorld.sol:442:    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:1059:            _simulateApplyUpkeep(sim, tick);
packages/contracts/src/ClanWorld.sol:1072:    function _simulateApplyUpkeep(SettlementSimulation memory sim, uint64 tick) internal view {
packages/contracts/src/ClanWorld.sol:1185:                (cs, m) = _simulateResolveAction(sim, cs, m, tick, tickSeed);
packages/contracts/src/ClanWorld.sol:1198:    function _simulateResolveAction(
packages/contracts/src/ClanWorld.sol:1654:    function _advanceBanditStates(uint64 closedTick) internal {
packages/contracts/src/ClanWorld.sol:1679:    function _resolveAttackingBandits(uint64 closedTick) internal {
packages/contracts/src/ClanWorld.sol:1688:                    _resolveBanditAttack(banditId, closedTick);
packages/contracts/src/ClanWorld.sol:1698:    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
packages/contracts/src/ClanWorld.sol:1738:        emit BanditAttackResolved(
packages/contracts/src/ClanWorld.sol:1756:            emit BlueprintEarned(targetClanId, banditId, closedTick);
packages/contracts/src/ClanWorld.sol:2226:    function heartbeat() external override nonReentrant {
packages/contracts/src/ClanWorld.sol:2246:        _advanceBanditStates(closedTick);
packages/contracts/src/ClanWorld.sol:2249:        _resolveAttackingBandits(closedTick);
packages/contracts/src/ClanWorld.sol:2344:    function settleClansman(uint32 csId) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:3227:    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
packages/contracts/src/ClanWorld.sol:3369:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {

codex
The first pass already surfaced one suspicious event-signature mismatch in the interface diff, so I’m drilling into the exact emitted call sites and ABI declarations next. I’m also pulling the surrounding `WorldSnapshot`/`WorldState` structs because the JSON ABI update is only safe if the Solidity tuple order stayed aligned end to end.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "sed -n '150,260p' packages/contracts/src/IClanWorld.sol && printf '\\n---\\n' && sed -n '560,640p' packages/contracts/src/IClanWorld.sol && printf '\\n---\\n' && sed -n '3360,3435p' packages/contracts/src/ClanWorld.sol && printf '\\n---\\n' && sed -n '1730,1765p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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


---
        uint32 indexed clanId,
        uint32 clansmanId,
        address tokenIn,
        address tokenOut,
        uint256 amountIn,
        uint256 amountOut
    );
    event ScheduledMarketActionCommitted(
        uint64 indexed executeAtTick,
        uint64 indexed commitSequence,
        uint32 indexed clanId,
        uint32 clansmanId,
        ActionType action,
        address marketToken,
        uint256 marketAmount,
        uint256 maxGoldIn
    );
    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);

    // ----- bandits -----
    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
    event BanditStateChanged(
        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
    );
    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
    event BanditAttackResolved(
        uint32 indexed banditId,
        uint32 indexed targetClanId,
        bool defended,
        uint16 attackPower,
        uint16 totalDefense,
        uint16 wallLevelAfter,
        uint256 stolenWood,
        uint256 stolenIron,
        uint256 stolenWheat,
        uint256 stolenFish,
        uint64 atTick
    );
    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
    event LootDistributed(
        uint32 indexed banditId,
        uint32[] clanIdsRewarded,
        uint256 perClanWood,
        uint256 perClanWheat,
        uint256 perClanFish,
        uint256 perClanIron,
        uint256 perClanGold,
        uint256 burnedWood,
        uint256 burnedWheat,
        uint256 burnedFish,
        uint256 burnedIron,
        uint256 burnedGold
    );
    event LootDistributedToDefender(
        uint32 indexed banditId,
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    );

    // ----- winter cold damage -----
    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);

    // ----- OTC transfers -----
    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
    event VaultResourceTransferred(
        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
    );
    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);

    // ----- treasury / pools -----

---
            Mission memory m = sim.missions[index];
            return DerivedClansmanState({
                clansman: cs,
                activeMission: m,
                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
                derivedAtTick: _world.currentTick
            });
        }

        Mission memory fallbackMission = _missions[clansmanId];
        return DerivedClansmanState({
            clansman: cs,
            activeMission: fallbackMission,
            effectiveRegion: _effectiveRegion(cs, fallbackMission, _world.currentTick),
            derivedAtTick: _world.currentTick
        });
    }

    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
        return _bandits[banditId].targetClanId;
    }

    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
        external
        pure
        override
        returns (uint8 travelTicks, bytes8 path)
    {
        if (srcRegion > 8 || dstRegion > 8) {
            return (0, bytes8(0));
        }
        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
            travelTicks = 0;
            path = bytes8(uint64(srcRegion) << 56);
            return (travelTicks, path);
        }
        travelTicks = _travelTicks(srcRegion, dstRegion);
        path = _buildPath(srcRegion, dstRegion);
    }

    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
        return _lootValueRaw(_clans[clanId]);
    }

    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
        return _lootValueRaw(sim.clan);
    }

    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
    }

    function _derivedClanStateFromSimulation(Clan memory clan) internal view returns (DerivedClanState memory) {
        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
        return DerivedClanState({
            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
        });
    }

    function _findSimulatedClansman(SettlementSimulation memory sim, uint32 clansmanId)
        internal
        pure
        returns (bool found, uint256 index)
    {
        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].clansmanId == clansmanId) {
                return (true, i);
            }
        }
    }

    function _effectiveRegion(Clansman memory cs, Mission memory m, uint64 tick) internal pure returns (uint8) {
        if (cs.state == ClansmanState.TRAVELING && m.active) {
            return tick >= m.arrivalTick ? m.targetRegion : m.startRegion;

---
            return tiedClanIds[0];
        }

        uint256 selected = RNG.rngBounded(_world.currentTickSeed, DOMAIN_BANDIT_TARGET_PICK, bandit.id, tiedCount);
        return tiedClanIds[selected];
    }

    function _banditStrengthForLegacyEvent(uint32 strength) internal pure returns (uint16) {
        if (strength > type(uint16).max) return type(uint16).max;
        // forge-lint: disable-next-line(unsafe-typecast)
        return uint16(strength);
    }

    function _resolveAttackingBandits(uint64 closedTick) internal {
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = _banditsByRegion[region];
            uint256 i = 0;
            while (i < regionBandits.length) {
                uint32 banditId = regionBandits[i];
                BanditTroop storage bandit = _bandits[banditId];
                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
                if (shouldResolve) {
                    _resolveBanditAttack(banditId, closedTick);
                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
                        continue;
                    }
                }
                i++;
            }
        }
    }

    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");

        BanditTroop storage bandit = _bandits[banditId];

exec
/bin/bash -lc "sed -n '1698,1775p' packages/contracts/src/ClanWorld.sol && printf '\\n---\\n' && sed -n '396,455p' packages/contracts/src/IClanWorld.sol && printf '\\n---\\n' && sed -n '3369,3408p' packages/contracts/src/ClanWorld.sol && printf '\\n---\\n' && sed -n '295,325p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
                }
            }
        }
    }

    function _pickBanditAttackTarget(BanditTroop storage bandit) internal view returns (uint32 targetClanId) {
        uint32[MAX_CLANS] memory tiedClanIds;
        uint256 tiedCount;
        uint256 bestLootValue;

        for (uint256 i = 0; i < _allClanIds.length; i++) {
            uint32 clanId = _allClanIds[i];
            Clan storage clan = _clans[clanId];
            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
                continue;
            }

            uint256 lootValue = _lootValueRaw(clan);
            if (tiedCount == 0 || lootValue > bestLootValue) {
                bestLootValue = lootValue;
                tiedClanIds[0] = clanId;
                tiedCount = 1;
            } else if (lootValue == bestLootValue) {
                tiedClanIds[tiedCount] = clanId;
                tiedCount++;
            }
        }

        if (tiedCount == 0) {
            return ClanWorldConstants.CLAN_ID_NULL;
        }
        if (tiedCount == 1) {
            return tiedClanIds[0];
        }

        uint256 selected = RNG.rngBounded(_world.currentTickSeed, DOMAIN_BANDIT_TARGET_PICK, bandit.id, tiedCount);
        return tiedClanIds[selected];
    }

    function _banditStrengthForLegacyEvent(uint32 strength) internal pure returns (uint16) {
        if (strength > type(uint16).max) return type(uint16).max;
        // forge-lint: disable-next-line(unsafe-typecast)
        return uint16(strength);
    }

    function _resolveAttackingBandits(uint64 closedTick) internal {
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = _banditsByRegion[region];
            uint256 i = 0;
            while (i < regionBandits.length) {
                uint32 banditId = regionBandits[i];
                BanditTroop storage bandit = _bandits[banditId];
                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
                if (shouldResolve) {
                    _resolveBanditAttack(banditId, closedTick);
                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
                        continue;
                    }
                }
                i++;
            }
        }
    }

    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");

        BanditTroop storage bandit = _bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
            return;
        }
        if (bandit.tickEnteredState != closedTick) {
            return;
        }

        uint32 targetClanId = bandit.targetClanId;
        Clan storage targetClan = _clans[targetClanId];
        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {

---
struct LeaderboardEntry {
    uint32 clanId;
    address owner;
    uint8 monumentLevel;
    uint8 baseLevel;
    uint8 wallLevel;
    uint8 livingClansmen;
    ClanState state;
    uint256 lootValue; // settled
}

struct WorldSnapshot {
    uint64 currentTick;
    uint64 seasonStartTick;
    uint64 seasonEndTick;
    bool seasonFinalized;
    uint64 currentSeasonNumber;
    uint64 nextHeartbeatAtTick;
    bool winterActive;
    uint64 winterStartsAtTick;
    uint64 winterEndsAtTick;
    uint32 activeBanditId;
    bytes32 currentTickSeed;

    LeaderboardEntry[] leaderboard;
}

struct ClansmanFullView {
    DerivedClansmanState clansman;
    Mission activeMission;
}

struct ClanFullView {
    DerivedClanState clan;
    ClansmanFullView[] clansmen;
    WheatPlot westPlot;
    WheatPlot eastPlot;
    uint32[] incomingDefenderIds; // legacy UI field; clanIds defending this clan's home region
    uint32 thisClanDefendingBaseId; // defended home region, or 0 if none
}

struct PoolReserves {
    address resourceToken;
    uint256 resourceReserve;
    uint256 goldReserve;
    uint256 spotPriceGoldPerResource; // = goldReserve * 1e18 / resourceReserve, or 0
}

struct MarketState {
    PoolReserves wood;
    PoolReserves wheat;
    PoolReserves fish;
    PoolReserves iron;

    uint64 currentTick;
    ScheduledMarketAction[] currentTickQueue;
    ScheduledMarketAction[] nextTickQueue;
}

struct ActiveBanditView {

---
        Mission memory fallbackMission = _missions[clansmanId];
        return DerivedClansmanState({
            clansman: cs,
            activeMission: fallbackMission,
            effectiveRegion: _effectiveRegion(cs, fallbackMission, _world.currentTick),
            derivedAtTick: _world.currentTick
        });
    }

    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
        return _bandits[banditId].targetClanId;
    }

    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
        external
        pure
        override
        returns (uint8 travelTicks, bytes8 path)
    {
        if (srcRegion > 8 || dstRegion > 8) {
            return (0, bytes8(0));
        }
        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
            travelTicks = 0;
            path = bytes8(uint64(srcRegion) << 56);
            return (travelTicks, path);
        }
        travelTicks = _travelTicks(srcRegion, dstRegion);
        path = _buildPath(srcRegion, dstRegion);
    }

    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
        return _lootValueRaw(_clans[clanId]);
    }

    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
        return _lootValueRaw(sim.clan);
    }


---

    uint32 targetClanId; // DefendBase only
    address marketToken; // market token for buy/sell
    uint256 marketAmount; // exact-in for sell, exact-out for buy
    uint256 maxGoldIn; // market_buy only, 0 otherwise
}

// v1 ABI: Bandit troop layout redesigned in Phase 9. ABI consumers must regenerate.
struct BanditTroop {
    uint32 id;
    uint8 region;
    BanditState state;
    uint32 targetClanId; // 0 if not attacking
    uint64 tickEnteredState;
    uint32 strength; // hp / combat power
    uint256 carryWood;
    uint256 carryIron;
    uint256 carryWheat;
    uint256 carryFish;
    uint256 carryGold;
}

struct ScheduledMarketAction {
    uint64 executeAtTick;
    uint64 commitSequence; // global monotonic FIFO order
    uint64 missionNonce; // mission nonce captured when the action was queued
    uint32 clanId;
    uint32 clansmanId;
    ActionType action; // MarketBuy or MarketSell

    address marketToken;

exec
/bin/bash -lc "sed -n '2226,2265p' packages/contracts/src/ClanWorld.sol && printf '\\n---\\n' && sed -n '1638,1698p' packages/contracts/src/ClanWorld.sol && printf '\\n---\\n' && sed -n '442,500p' packages/contracts/src/ClanWorld.sol && printf '\\n---\\n' && sed -n '1072,1228p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
            }
        }
    }

    function _banditSpawnRegionWeights() internal view returns (uint256[] memory weights) {
        weights = new uint256[](8);
        uint256 clanCount = _allClanIds.length;
        if (clanCount == 0) {
            return weights;
        }

        uint256 scanCount = clanCount < MAX_BANDIT_SPAWN_SCAN_PER_REGION ? clanCount : MAX_BANDIT_SPAWN_SCAN_PER_REGION;
        uint256 startIndex = uint256(_world.currentTick) % clanCount;
        uint256 clansmenScanned;
        for (uint256 i = 0; i < scanCount; i++) {
            Clan storage clan = _clans[_allClanIds[(startIndex + i) % clanCount]];
            if (clan.clanState == ClanState.DEAD) {
                continue;
            }

            if (
                clan.baseRegion >= ClanWorldConstants.REGION_FOREST
                    && clan.baseRegion <= ClanWorldConstants.REGION_DEEP_SEA
            ) {
                weights[clan.baseRegion - 1] += 100 + (_lootValueRaw(clan) / 1e18);
            }

            uint32[] storage clansmanIds = _clanClansmanIds[clan.clanId];
            for (
                uint256 j = 0;
                j < clansmanIds.length && clansmenScanned < MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION;
                j++
            ) {
                clansmenScanned += 1;
                Clansman storage cs = _clansmen[clansmanIds[j]];
                if (
                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
                ) {
                    weights[cs.currentRegion - 1] += 25;

---
            }
        }

        delete _bandits[id];
        if (_activeBanditCount > 0) {
            _activeBanditCount -= 1;
        }
        if (_world.activeBanditId == id) {
            _world.activeBanditId = _findOldestActiveBandit();
        }
    }

    function _findOldestActiveBandit() internal view returns (uint32 oldestBanditId) {
        // V1 caps live troops at MAX_TOTAL_BANDITS = 8, so scanning the region
        // indexes is bounded even though storage mappings cannot be enumerated.
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = _banditsByRegion[region];
            for (uint256 i = 0; i < regionBandits.length; i++) {
                uint32 candidateId = regionBandits[i];
                BanditTroop storage candidate = _bandits[candidateId];
                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
                    continue;
                }
                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
                    oldestBanditId = candidateId;
                }
            }
        }
    }

    function _advanceBanditStates(uint64 closedTick) internal {
        require(_world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
            uint32[] storage regionBandits = _banditsByRegion[region];
            for (uint256 i = 0; i < regionBandits.length; i++) {
                uint32 banditId = regionBandits[i];
                BanditTroop storage bandit = _bandits[banditId];
                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
                    _transitionBanditState(banditId, BanditState.Camped);
                } else if (
                    bandit.state == BanditState.Camped
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS
                ) {
                    uint32 targetClanId = _pickBanditAttackTarget(bandit);
                    if (targetClanId == ClanWorldConstants.CLAN_ID_NULL) {
                        _transitionBanditState(banditId, BanditState.Escaped);
                        emit BanditEscaped(banditId, closedTick);
                    } else {
                        _transitionBanditToAttacking(banditId, targetClanId);
                    }
                } else if (
                    bandit.state == BanditState.Escaped
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
                ) {
                    _transitionBanditState(banditId, BanditState.Resting);
                } else if (
                    bandit.state == BanditState.Resting
                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
                ) {
                    _transitionBanditState(banditId, BanditState.Camped);
                }

---

    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
        if (clan.livingClansmen == 0) return;

        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;

        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
        bool hadEnoughFish = clan.vaultFish >= fishNeeded;

        if (hadEnoughWheat) {
            clan.vaultWheat -= wheatNeeded;
        } else {
            clan.vaultWheat = 0;
        }
        if (hadEnoughFish) {
            clan.vaultFish -= fishNeeded;
        } else {
            clan.vaultFish = 0;
        }

        bool starving = !hadEnoughWheat || !hadEnoughFish;
        if (starving && clan.starvationStartsAtTick == 0) {
            clan.starvationStartsAtTick = tick;
            emit ClanStarvationChanged(clan.clanId, true, tick);
        } else if (!starving && clan.starvationStartsAtTick != 0) {
            clan.starvationStartsAtTick = 0;
            emit ClanStarvationChanged(clan.clanId, false, tick);
        }

        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
            _killNextClansmanFromStarvation(clan, tick);
        }
    }

    function _isWinterTick(uint64 tick) internal pure returns (bool) {
        uint64 seasonOffset = tick % ClanWorldConstants.SEASON_DURATION_TICKS;
        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
        uint64 cycleStart = seasonOffset - cycleOffset;
        uint64 winterStart =
            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;

        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
            && seasonOffset < winterEnd;
    }

    function _killNextClansmanFromStarvation(Clan storage clan, uint64 tick) internal {
        if (clan.livingClansmen == 0) return;

        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
        for (uint256 i = 0; i < csIds.length; i++) {
            Clansman storage cs = _clansmen[csIds[i]];
            if (cs.state == ClansmanState.DEAD) continue;

            _markClansmanDead(clan, cs);
            if (clan.livingClansmen == 0) {
                _markClanDead(clan.clanId, "starvation", tick);

---
        for (uint64 tick = fromTick; tick < toTick; tick++) {
            _simulateApplyUpkeep(sim, tick);
            if (sim.clan.clanState == ClanState.DEAD) break;

            _simulateRegrowWheatPlots(sim, tick);

            for (uint256 i = 0; i < sim.clansmen.length; i++) {
                _simulateSettleMissionForClansman(sim, i, tick, tick + 1);
            }
        }

        sim.clan.lastSettledTick = toTick;
    }

    function _simulateApplyUpkeep(SettlementSimulation memory sim, uint64 tick) internal view {
        if (sim.clan.livingClansmen == 0) return;

        uint256 wheatNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
        uint256 fishNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;

        bool hadEnoughWheat = sim.clan.vaultWheat >= wheatNeeded;
        bool hadEnoughFish = sim.clan.vaultFish >= fishNeeded;

        sim.clan.vaultWheat = hadEnoughWheat ? sim.clan.vaultWheat - wheatNeeded : 0;
        sim.clan.vaultFish = hadEnoughFish ? sim.clan.vaultFish - fishNeeded : 0;

        bool starving = !hadEnoughWheat || !hadEnoughFish;
        if (starving && sim.clan.starvationStartsAtTick == 0) {
            sim.clan.starvationStartsAtTick = tick;
        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
            sim.clan.starvationStartsAtTick = 0;
        }

        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
            _simulateKillNextClansmanFromStarvation(sim);
        }
    }

    function _simulateKillNextClansmanFromStarvation(SettlementSimulation memory sim) internal pure {
        if (sim.clan.livingClansmen == 0) return;

        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].state == ClansmanState.DEAD) continue;

            _simulateMarkClansmanDead(sim, i);
            if (sim.clan.livingClansmen == 0) {
                _simulateMarkClanDead(sim);
            }
            return;
        }
    }

    function _simulateMarkClansmanDead(SettlementSimulation memory sim, uint256 index) internal pure {
        if (sim.clansmen[index].state == ClansmanState.DEAD) return;

        sim.clansmen[index].state = ClansmanState.DEAD;
        sim.clansmen[index].cooldownEndsAtTs = 0;
        if (sim.clan.livingClansmen > 0) {
            sim.clan.livingClansmen--;
        }
        if (sim.missions[index].active) {
            sim.missions[index].active = false;
        }
    }

    function _simulateMarkClanDead(SettlementSimulation memory sim) internal pure {
        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;

        sim.clan.clanState = ClanState.DEAD;
        sim.clan.vaultWood = 0;
        sim.clan.vaultWheat = 0;
        sim.clan.vaultFish = 0;
        sim.clan.vaultIron = 0;
        sim.clan.starvationStartsAtTick = 0;
        sim.clan.livingClansmen = 0;

        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            sim.clansmen[i].state = ClansmanState.DEAD;
            sim.clansmen[i].cooldownEndsAtTs = 0;
            if (sim.missions[i].active) {
                sim.missions[i].active = false;
            }
        }
    }

    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
        for (uint256 pi = 0; pi < 2; pi++) {
            WheatPlot memory plot = sim.wheatPlots[pi];
            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
                plot.state = WheatPlotState.Harvestable;
                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
                plot.regrowUntilTick = 0;
                sim.wheatPlots[pi] = plot;
            }
        }
    }

    function _simulateSettleMissionForClansman(
        SettlementSimulation memory sim,
        uint256 index,
        uint64 fromTick,
        uint64 toTick
    ) internal view {
        Clansman memory cs = sim.clansmen[index];
        Mission memory m = sim.missions[index];

        if (cs.state == ClansmanState.DEAD) {
            if (m.active) {
                m.active = false;
            }
            sim.missions[index] = m;
            return;
        }

        if (!m.active) return;

        for (uint64 tick = fromTick; tick < toTick; tick++) {
            bytes32 tickSeed = _tickSeeds[tick];

            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
                cs.state = ClansmanState.ACTING;
                cs.currentRegion = m.targetRegion;
            }

            if (m.action == ActionType.DefendBase) continue;

            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
                (cs, m) = _simulateResolveAction(sim, cs, m, tick, tickSeed);
                if (m.active && getActionDuration(m.action) > 0) {
                    (cs, m) = _simulateCompleteMission(cs, m);
                }
            }

            if (!m.active) break;
        }

        sim.clansmen[index] = cs;
        sim.missions[index] = m;
    }

    function _simulateResolveAction(
        SettlementSimulation memory sim,
        Clansman memory cs,
        Mission memory m,
        uint64 tick,
        bytes32 tickSeed
    ) internal view returns (Clansman memory, Mission memory) {
        bool starving =
            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
        ActionType action = m.action;

        if (action == ActionType.ChopWood) {
            (cs, m) = _simulateGatherWood(cs, m, tick, starving, tickSeed);
        } else if (action == ActionType.MineIron) {
            (cs, m) = _simulateGatherIron(sim, cs, m, tick, starving, tickSeed);
        } else if (action == ActionType.FishDocks) {
            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DOCKS_BPS);

codex
One concrete regression is emerging in the view-only replay path, so I’m checking whether it’s isolated to simulation or leaks into on-chain settlement too. I’m also confirming whether the interface event declaration actually matches the new `emit BlueprintEarned(...)` call, because the snippet I pulled looks inconsistent.
exec
/bin/bash -lc "nl -ba packages/contracts/src/IClanWorld.sol | sed -n '596,608p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1748,1760p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1198,1220p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '2226,2258p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

---
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

---
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

---
  2226	            }
  2227	        }
  2228	    }
  2229	
  2230	    function _banditSpawnRegionWeights() internal view returns (uint256[] memory weights) {
  2231	        weights = new uint256[](8);
  2232	        uint256 clanCount = _allClanIds.length;
  2233	        if (clanCount == 0) {
  2234	            return weights;
  2235	        }
  2236	
  2237	        uint256 scanCount = clanCount < MAX_BANDIT_SPAWN_SCAN_PER_REGION ? clanCount : MAX_BANDIT_SPAWN_SCAN_PER_REGION;
  2238	        uint256 startIndex = uint256(_world.currentTick) % clanCount;
  2239	        uint256 clansmenScanned;
  2240	        for (uint256 i = 0; i < scanCount; i++) {
  2241	            Clan storage clan = _clans[_allClanIds[(startIndex + i) % clanCount]];
  2242	            if (clan.clanState == ClanState.DEAD) {
  2243	                continue;
  2244	            }
  2245	
  2246	            if (
  2247	                clan.baseRegion >= ClanWorldConstants.REGION_FOREST
  2248	                    && clan.baseRegion <= ClanWorldConstants.REGION_DEEP_SEA
  2249	            ) {
  2250	                weights[clan.baseRegion - 1] += 100 + (_lootValueRaw(clan) / 1e18);
  2251	            }
  2252	
  2253	            uint32[] storage clansmanIds = _clanClansmanIds[clan.clanId];
  2254	            for (
  2255	                uint256 j = 0;
  2256	                j < clansmanIds.length && clansmenScanned < MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION;
  2257	                j++
  2258	            ) {

exec
/bin/bash -lc "rg -n \"getWorldSnapshot\\(|getWorldState\\(|struct WorldSnapshot|struct WorldState|BlueprintEarned\\(|BlueprintAwarded\\(\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/abi/IClanWorld.json packages/shared/src/adapters/IChainClient.ts packages/runner/src/runnerCastHeartbeat.ts" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/runner/src/runnerCastHeartbeat.ts:61: * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
packages/contracts/src/ClanWorld.sol:1835:            emit BlueprintEarned(targetClanId, banditId, BLUEPRINT_UNIT, closedTick);
packages/contracts/src/ClanWorld.sol:3203:    function getWorldState() external view override returns (WorldState memory) {
packages/contracts/src/ClanWorld.sol:3448:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
packages/contracts/src/IClanWorld.sol:188:struct WorldState {
packages/contracts/src/IClanWorld.sol:407:struct WorldSnapshot {
packages/contracts/src/IClanWorld.sol:603:    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
packages/contracts/src/IClanWorld.sol:604:    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
packages/contracts/src/IClanWorld.sol:715:    function getWorldState() external view returns (WorldState memory);
packages/contracts/src/IClanWorld.sol:779:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
packages/contracts/abi/IClanWorld.json:2000:          "internalType": "struct WorldSnapshot",
packages/contracts/abi/IClanWorld.json:2117:          "internalType": "struct WorldState",

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '3198,3468p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3198	
  3199	    // =========================================================================
  3200	    // RAW READ GETTERS
  3201	    // =========================================================================
  3202	
  3203	    function getWorldState() external view override returns (WorldState memory) {
  3204	        return _world;
  3205	    }
  3206	
  3207	    function getTreasuryState() external view override returns (TreasuryState memory) {
  3208	        return _treasury;
  3209	    }
  3210	
  3211	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  3212	        return _clans[clanId];
  3213	    }
  3214	
  3215	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  3216	        return _clansmen[clansmanId];
  3217	    }
  3218	
  3219	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  3220	        return _missions[clansmanId];
  3221	    }
  3222	
  3223	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  3224	        external
  3225	        view
  3226	        override
  3227	        returns (uint64 submitted, uint64 executes, uint64 settles)
  3228	    {
  3229	        Mission memory m = _missions[clansmanId];
  3230	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  3231	            return (0, 0, 0);
  3232	        }
  3233	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  3234	    }
  3235	
  3236	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  3237	        if (
  3238	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  3239	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  3240	        ) {
  3241	            return 4;
  3242	        }
  3243	
  3244	        if (
  3245	            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
  3246	                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
  3247	                || action == ActionType.MarketSell
  3248	        ) {
  3249	            return 1;
  3250	        }
  3251	
  3252	        return 0;
  3253	    }
  3254	
  3255	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
  3256	        return uint64(_travelTicks(fromRegion, toRegion));
  3257	    }
  3258	
  3259	    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
  3260	        BanditTroop memory bandit = _bandits[banditId];
  3261	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
  3262	            return BanditTroop({
  3263	                id: 0,
  3264	                region: 0,
  3265	                state: BanditState.None,
  3266	                targetClanId: 0,
  3267	                tickEnteredState: 0,
  3268	                strength: 0,
  3269	                carryWood: 0,
  3270	                carryIron: 0,
  3271	                carryWheat: 0,
  3272	                carryFish: 0,
  3273	                carryGold: 0
  3274	            });
  3275	        }
  3276	        return bandit;
  3277	    }
  3278	
  3279	    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
  3280	        return getBandit(banditId);
  3281	    }
  3282	
  3283	    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
  3284	        return _banditsByRegion[region];
  3285	    }
  3286	
  3287	    function getWheatPlots(uint32 clanId)
  3288	        external
  3289	        view
  3290	        override
  3291	        returns (WheatPlot memory west, WheatPlot memory east)
  3292	    {
  3293	        west = _wheatPlots[clanId][0];
  3294	        east = _wheatPlots[clanId][1];
  3295	    }
  3296	
  3297	    function getScheduledMarketActionsForTick(uint64 tick)
  3298	        external
  3299	        view
  3300	        override
  3301	        returns (ScheduledMarketAction[] memory)
  3302	    {
  3303	        return _scheduledMarketActions[tick];
  3304	    }
  3305	
  3306	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  3307	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  3308	        uint256 count = 0;
  3309	
  3310	        for (uint256 i = 0; i < defendingClans.length; i++) {
  3311	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  3312	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  3313	                Mission storage mission = _missions[clanClansmen[j]];
  3314	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  3315	                    count++;
  3316	                }
  3317	            }
  3318	        }
  3319	
  3320	        clansmanIds = new uint32[](count);
  3321	        uint256 out = 0;
  3322	        for (uint256 i = 0; i < defendingClans.length; i++) {
  3323	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  3324	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  3325	                uint32 clansmanId = clanClansmen[j];
  3326	                Mission storage mission = _missions[clansmanId];
  3327	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  3328	                    clansmanIds[out++] = clansmanId;
  3329	                }
  3330	            }
  3331	        }
  3332	    }
  3333	
  3334	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
  3335	        return _defendingClansByRegion[region];
  3336	    }
  3337	
  3338	    // =========================================================================
  3339	    // DERIVED READ GETTERS (read-only, no storage mutation)
  3340	    // =========================================================================
  3341	
  3342	    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
  3343	        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
  3344	        return _derivedClanStateFromSimulation(sim.clan);
  3345	    }
  3346	
  3347	    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
  3348	        Clansman memory cs = _clansmen[clansmanId];
  3349	        if (cs.clansmanId == 0) {
  3350	            Mission memory emptyMission;
  3351	            return DerivedClansmanState({
  3352	                clansman: cs, activeMission: emptyMission, effectiveRegion: 0, derivedAtTick: _world.currentTick
  3353	            });
  3354	        }
  3355	
  3356	        SettlementSimulation memory sim = _simulateSettleToTick(cs.clanId, _world.currentTick);
  3357	        (bool found, uint256 index) = _findSimulatedClansman(sim, clansmanId);
  3358	        if (found) {
  3359	            cs = sim.clansmen[index];
  3360	            Mission memory m = sim.missions[index];
  3361	            return DerivedClansmanState({
  3362	                clansman: cs,
  3363	                activeMission: m,
  3364	                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
  3365	                derivedAtTick: _world.currentTick
  3366	            });
  3367	        }
  3368	
  3369	        Mission memory fallbackMission = _missions[clansmanId];
  3370	        return DerivedClansmanState({
  3371	            clansman: cs,
  3372	            activeMission: fallbackMission,
  3373	            effectiveRegion: _effectiveRegion(cs, fallbackMission, _world.currentTick),
  3374	            derivedAtTick: _world.currentTick
  3375	        });
  3376	    }
  3377	
  3378	    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
  3379	        return _bandits[banditId].targetClanId;
  3380	    }
  3381	
  3382	    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
  3383	        external
  3384	        pure
  3385	        override
  3386	        returns (uint8 travelTicks, bytes8 path)
  3387	    {
  3388	        if (srcRegion > 8 || dstRegion > 8) {
  3389	            return (0, bytes8(0));
  3390	        }
  3391	        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
  3392	            travelTicks = 0;
  3393	            path = bytes8(uint64(srcRegion) << 56);
  3394	            return (travelTicks, path);
  3395	        }
  3396	        travelTicks = _travelTicks(srcRegion, dstRegion);
  3397	        path = _buildPath(srcRegion, dstRegion);
  3398	    }
  3399	
  3400	    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
  3401	        return _lootValueRaw(_clans[clanId]);
  3402	    }
  3403	
  3404	    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
  3405	        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
  3406	        return _lootValueRaw(sim.clan);
  3407	    }
  3408	
  3409	    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
  3410	    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
  3411	        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
  3412	    }
  3413	
  3414	    function _derivedClanStateFromSimulation(Clan memory clan) internal view returns (DerivedClanState memory) {
  3415	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  3416	        return DerivedClanState({
  3417	            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
  3418	        });
  3419	    }
  3420	
  3421	    function _findSimulatedClansman(SettlementSimulation memory sim, uint32 clansmanId)
  3422	        internal
  3423	        pure
  3424	        returns (bool found, uint256 index)
  3425	    {
  3426	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  3427	            if (sim.clansmen[i].clansmanId == clansmanId) {
  3428	                return (true, i);
  3429	            }
  3430	        }
  3431	    }
  3432	
  3433	    function _effectiveRegion(Clansman memory cs, Mission memory m, uint64 tick) internal pure returns (uint8) {
  3434	        if (cs.state == ClansmanState.TRAVELING && m.active) {
  3435	            return tick >= m.arrivalTick ? m.targetRegion : m.startRegion;
  3436	        }
  3437	        return cs.currentRegion;
  3438	    }
  3439	
  3440	    // =========================================================================
  3441	    // UI INDEXER AGGREGATOR GETTERS
  3442	    // =========================================================================
  3443	
  3444	    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
  3445	    ///      Carry amounts not included. Full view-only settlement deferred.
  3446	    ///      View function — no gas cost for off-chain indexer/UI reads.
  3447	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  3448	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
  3449	        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
  3450	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  3451	            uint32 cid = _allClanIds[i];
  3452	            Clan storage clan = _clans[cid];
  3453	            lb[i] = LeaderboardEntry({
  3454	                clanId: cid,
  3455	                owner: clan.owner,
  3456	                monumentLevel: clan.monumentLevel,
  3457	                baseLevel: clan.baseLevel,
  3458	                wallLevel: clan.wallLevel,
  3459	                livingClansmen: clan.livingClansmen,
  3460	                state: clan.clanState,
  3461	                lootValue: _lootValueRaw(clan)
  3462	            });
  3463	        }
  3464	
  3465	        return WorldSnapshot({
  3466	            currentTick: _world.currentTick,
  3467	            seasonStartTick: _world.seasonStartTick,
  3468	            seasonEndTick: _world.seasonEndTick,

exec
/bin/bash -lc "nl -ba packages/shared/src/adapters/IChainClient.ts | sed -n '1,230p' && printf '\\n---\\n' && nl -ba packages/runner/src/runnerCastHeartbeat.ts | sed -n '1,180p' && printf '\\n---\\n' && nl -ba packages/shared/test/clanWorldAbi.test.ts | sed -n '1,180p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	import fs from 'node:fs';
     2	import { createPublicClient, createWalletClient, http, fallback, defineChain } from 'viem';
     3	import { privateKeyToAccount } from 'viem/accounts';
     4	import IClanWorldArtifact from '../../../contracts/abi/IClanWorld.json';
     5	import type { ClanFullView, ClanOrder, Tick } from '../types';
     6	import type { Abi } from 'viem';
     7	import { readEnv } from './_env';
     8	
     9	export interface IChainClient {
    10	  getCurrentTick(): Promise<Tick>;
    11	  submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }>;
    12	  getClanFullView(clanId: string): Promise<ClanFullView>;
    13	}
    14	
    15	const DEFAULT_CONTRACT_ADDRESS = '0x1BF5649f29CbB53E117a5aE969A18A71790f83E8' as const;
    16	
    17	export const baseSepolia = defineChain({
    18	  id: 84532,
    19	  name: 'Base Sepolia',
    20	  nativeCurrency: { name: 'Ether', symbol: 'ETH', decimals: 18 },
    21	  rpcUrls: {
    22	    default: { http: ['https://sepolia.base.org'] },
    23	  },
    24	});
    25	
    26	export const CLAN_WORLD_ABI = IClanWorldArtifact.abi as Abi;
    27	
    28	class StubChainClient implements IChainClient {
    29	  async getCurrentTick(): Promise<Tick> {
    30	    return 0;
    31	  }
    32	  async submitOrders(_clanId: string, _orders: ClanOrder[]): Promise<{ txHash: string }> {
    33	    return { txHash: '0xstub' };
    34	  }
    35	  async getClanFullView(clanId: string): Promise<ClanFullView> {
    36	    return {
    37	      clan: { id: clanId, name: `clan-${clanId}`, treasury: '0' },
    38	      controlledRegions: [],
    39	      pendingOrders: [],
    40	      whispers: [],
    41	    };
    42	  }
    43	}
    44	
    45	class RealChainClient implements IChainClient {
    46	  private readonly client: ReturnType<typeof createPublicClient>;
    47	  private readonly contractAddress: `0x${string}`;
    48	  private readonly transport: ReturnType<typeof http> | ReturnType<typeof fallback>;
    49	
    50	  constructor() {
    51	    const primaryRpc = readEnv('RPC_URL_PRIMARY');
    52	    const fallbackRpc = readEnv('RPC_URL_FALLBACK');
    53	
    54	    this.transport =
    55	      primaryRpc && fallbackRpc
    56	        ? fallback([http(primaryRpc), http(fallbackRpc)])
    57	        : http(primaryRpc ?? fallbackRpc);
    58	
    59	    this.contractAddress = (readEnv('CLAN_WORLD_CONTRACT_ADDRESS') ??
    60	      DEFAULT_CONTRACT_ADDRESS) as `0x${string}`;
    61	
    62	    this.client = createPublicClient({
    63	      chain: baseSepolia,
    64	      transport: this.transport,
    65	    });
    66	  }
    67	
    68	  async getCurrentTick(): Promise<Tick> {
    69	    const snapshot = await this.client.readContract({
    70	      address: this.contractAddress,
    71	      abi: CLAN_WORLD_ABI,
    72	      functionName: 'getWorldSnapshot',
    73	    }) as { currentTick: bigint };
    74	    return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
    75	  }
    76	
    77	  async submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }> {
    78	    // Wave 0: single-Elder only — concurrent nonce coordination deferred to Wave 1
    79	    const parsedClanId = parseInt(clanId, 10);
    80	    if (isNaN(parsedClanId) || String(parsedClanId) !== clanId.trim()) {
    81	      throw new Error(`submitOrders: clanId must be a decimal integer, got '${clanId}'`);
    82	    }
    83	
    84	    for (const order of orders) {
    85	      if (order.kind === 'mission') {
    86	        const { clansmanId, gotoRegion, action } = order.payload;
    87	        if (clansmanId === undefined || gotoRegion === undefined || action === undefined) {
    88	          throw new Error(`submitOrders: mission order missing required payload fields (clansmanId, gotoRegion, action)`);
    89	        }
    90	      }
    91	    }
    92	
    93	    const nonMissionOrders = orders.filter(o => o.kind !== 'mission');
    94	    if (nonMissionOrders.length > 0) {
    95	      console.warn(`[RealChainClient] submitOrders: ${nonMissionOrders.length} non-mission order(s) skipped (Wave 0 only supports 'mission' kind)`);
    96	    }
    97	
    98	    const contractOrders = orders
    99	      .filter(o => o.kind === 'mission')
   100	      .map(o => ({
   101	        clansmanId: Number(o.payload.clansmanId),
   102	        gotoRegion: Number(o.payload.gotoRegion),
   103	        action: Number(o.payload.action),
   104	        targetClanId: 0,
   105	        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
   106	        marketAmount: 0n,
   107	        maxGoldIn: 0n,
   108	      }));
   109	
   110	    if (contractOrders.length === 0) {
   111	      throw new Error('submitOrders: no valid mission orders to submit');
   112	    }
   113	
   114	    const keyPath = readEnv('ELDER_WALLET_KEY_PATH');
   115	    let pk: string | undefined;
   116	    let pkSource: string | undefined;
   117	    if (keyPath) {
   118	      try {
   119	        pk = fs.readFileSync(keyPath, 'utf8').trim();
   120	        pkSource = `ELDER_WALLET_KEY_PATH file at ${keyPath}`;
   121	      } catch (err) {
   122	        if (err && typeof err === 'object' && 'code' in err && err.code === 'ENOENT') {
   123	          throw new Error(
   124	            `ELDER_WALLET_KEY_PATH file not found at ${keyPath}; either set DEPLOYER_PRIVATE_KEY env var or provide a key file`,
   125	          );
   126	        }
   127	        const msg = err instanceof Error ? err.message : String(err);
   128	        throw new Error(`Failed to read ELDER_WALLET_KEY_PATH file at ${keyPath}: ${msg}`);
   129	      }
   130	    } else {
   131	      const fallbackKey = readEnv('DEPLOYER_PRIVATE_KEY');
   132	      if (fallbackKey) {
   133	        console.warn('[RealChainClient] ELDER_WALLET_KEY_PATH not set; falling back to DEPLOYER_PRIVATE_KEY (deprecated)');
   134	        pk = fallbackKey;
   135	        pkSource = 'DEPLOYER_PRIVATE_KEY env var';
   136	      }
   137	    }
   138	    if (!pk) throw new Error('Neither ELDER_WALLET_KEY_PATH nor DEPLOYER_PRIVATE_KEY is set');
   139	
   140	    // Normalize: add 0x prefix if missing
   141	    if (!pk.startsWith('0x')) pk = '0x' + pk;
   142	    if (!/^0x[0-9a-fA-F]{64}$/.test(pk)) {
   143	      throw new Error(
   144	        `Invalid private key from ${pkSource ?? 'unknown source'}: expected a 64-hex-char private key (0x-prefixed optional)`,
   145	      );
   146	    }
   147	
   148	    const account = privateKeyToAccount(pk as `0x${string}`);
   149	    const walletClient = createWalletClient({
   150	      account,
   151	      chain: baseSepolia,
   152	      transport: this.transport,
   153	    });
   154	
   155	    const hash = await walletClient.writeContract({
   156	      address: this.contractAddress,
   157	      abi: CLAN_WORLD_ABI,
   158	      functionName: 'submitClanOrders',
   159	      args: [parsedClanId, contractOrders],
   160	    });
   161	
   162	    return { txHash: hash };
   163	  }
   164	
   165	  async getClanFullView(clanId: string): Promise<ClanFullView> {
   166	    const result = await this.client.readContract({
   167	      address: this.contractAddress,
   168	      abi: CLAN_WORLD_ABI,
   169	      functionName: 'getClanFullView',
   170	      args: [parseInt(clanId, 10)],
   171	    }) as {
   172	      clan: {
   173	        clan: {
   174	          clanId: number;
   175	          goldBalance: bigint;
   176	        };
   177	      };
   178	    };
   179	
   180	    const inner = result.clan.clan;
   181	    return {
   182	      clan: {
   183	        id: String(inner.clanId),
   184	        name: `clan-${inner.clanId}`,
   185	        treasury: String(inner.goldBalance),
   186	      },
   187	      // controlledRegions, pendingOrders, whispers not available in Wave 0 contract read
   188	      controlledRegions: [], // Wave 0: omit base region from controlledRegions; populated in Wave 1 from on-chain data
   189	      pendingOrders: [],
   190	      whispers: [],
   191	    };
   192	  }
   193	}
   194	
   195	export function createChainClient(): IChainClient {
   196	  return readEnv('CLAN_WORLD_USE_STUB_CHAIN') === 'true'
   197	    ? new StubChainClient()
   198	    : new RealChainClient();
   199	}

---
     1	import {
     2	  ContractFunctionRevertedError,
     3	  createPublicClient,
     4	  createWalletClient,
     5	  http,
     6	  type Account,
     7	  type Abi,
     8	  type PublicClient,
     9	  type WalletClient,
    10	} from 'viem';
    11	import { privateKeyToAccount } from 'viem/accounts';
    12	import IClanWorldArtifact from '../../contracts/abi/IClanWorld.json';
    13	import { baseSepolia } from '@clan-world/shared/adapters';
    14	import {
    15	  HeartbeatRateLimitedError,
    16	  type IHeartbeatCaller,
    17	} from '@clan-world/agents/seams';
    18	
    19	export const HEARTBEAT_ABI = IClanWorldArtifact.abi as Abi;
    20	
    21	export interface RunnerHeartbeatConfig {
    22	  /** Hex-encoded 64-char private key, optionally 0x-prefixed. */
    23	  privateKey: string;
    24	  /** Override RPC URL; defaults to the Base Sepolia public endpoint. */
    25	  rpcUrl?: string;
    26	  /** ClanWorld contract address. */
    27	  contractAddress: `0x${string}`;
    28	}
    29	
    30	/**
    31	 * Reads `RUNNER_PRIVATE_KEY`, `RPC_URL_PRIMARY`, `CLAN_WORLD_CONTRACT_ADDRESS`
    32	 * from env. Throws if `RUNNER_PRIVATE_KEY` is missing — the runner intentionally
    33	 * does not generate or store its own wallet; provisioning is operator-side.
    34	 */
    35	export function configFromEnv(env: NodeJS.ProcessEnv = process.env): RunnerHeartbeatConfig {
    36	  const pk = env['RUNNER_PRIVATE_KEY'];
    37	  if (!pk) {
    38	    throw new Error(
    39	      'RUNNER_PRIVATE_KEY is not set — the runner needs a dedicated wallet (NEVER reuse an Elder wallet). ' +
    40	        'Provision a fresh key, fund it with testnet ETH, and export RUNNER_PRIVATE_KEY before starting the daemon.',
    41	    );
    42	  }
    43	  const contractAddress = env['CLAN_WORLD_CONTRACT_ADDRESS'];
    44	  if (!contractAddress || !/^0x[0-9a-fA-F]{40}$/.test(contractAddress)) {
    45	    throw new Error(
    46	      `CLAN_WORLD_CONTRACT_ADDRESS missing or invalid; expected 0x-prefixed 40-hex-char address, got ${String(contractAddress)}`,
    47	    );
    48	  }
    49	  return {
    50	    privateKey: pk,
    51	    rpcUrl: env['RPC_URL_PRIMARY'] || env['RPC_URL_FALLBACK'],
    52	    contractAddress: contractAddress as `0x${string}`,
    53	  };
    54	}
    55	
    56	/**
    57	 * Viem-backed `IHeartbeatCaller`. Wallet account is the dedicated runner key.
    58	 *
    59	 * Rate-limit detection: ClanWorld.heartbeat() reverts when called before
    60	 * `nextHeartbeatAtTs`. We don't have a typed custom error in the ABI, so on
    61	 * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
    62	 * still in the future, throw `HeartbeatRateLimitedError(nextAllowedAt)`.
    63	 * Other reverts surface as the original error.
    64	 */
    65	export class RunnerCastHeartbeat implements IHeartbeatCaller {
    66	  private readonly publicClient: PublicClient;
    67	  private readonly walletClient: WalletClient;
    68	  private readonly account: Account;
    69	  private readonly contractAddress: `0x${string}`;
    70	
    71	  constructor(cfg: RunnerHeartbeatConfig) {
    72	    const pk = normalizePk(cfg.privateKey);
    73	    this.account = privateKeyToAccount(pk);
    74	    const transport = cfg.rpcUrl ? http(cfg.rpcUrl) : http();
    75	    this.publicClient = createPublicClient({ chain: baseSepolia, transport });
    76	    this.walletClient = createWalletClient({
    77	      account: this.account,
    78	      chain: baseSepolia,
    79	      transport,
    80	    });
    81	    this.contractAddress = cfg.contractAddress;
    82	  }
    83	
    84	  async callHeartbeat(): Promise<{ txHash: string }> {
    85	    try {
    86	      const hash = await this.walletClient.writeContract({
    87	        account: this.account,
    88	        chain: baseSepolia,
    89	        address: this.contractAddress,
    90	        abi: HEARTBEAT_ABI,
    91	        functionName: 'heartbeat',
    92	        args: [],
    93	      });
    94	      // Wait for confirmation per the seam contract ("not fire-and-forget").
    95	      const receipt = await this.publicClient.waitForTransactionReceipt({ hash });
    96	      if (receipt.status !== 'success') {
    97	        // Mined-but-reverted. Most common cause is the rate-limit window
    98	        // hadn't elapsed yet (when simulation succeeded but execution didn't).
    99	        // Re-read state to upgrade to HeartbeatRateLimitedError when applicable.
   100	        const next = await this.readNextHeartbeatAt().catch(() => undefined);
   101	        if (next !== undefined && next > Math.floor(Date.now() / 1000)) {
   102	          throw new HeartbeatRateLimitedError(next);
   103	        }
   104	        throw new Error(`heartbeat tx ${hash} reverted on-chain`);
   105	      }
   106	      return { txHash: hash };
   107	    } catch (err) {
   108	      // Already a rate-limit error — rethrow immediately; no second RPC read.
   109	      if (err instanceof HeartbeatRateLimitedError) throw err;
   110	      // Attempt to upgrade only simulation-level contract reverts to
   111	      // HeartbeatRateLimitedError; pre-flight/RPC errors must surface unchanged.
   112	      if (!(err instanceof ContractFunctionRevertedError)) throw err;
   113	      const next = await this.readNextHeartbeatAt().catch(() => undefined);
   114	      if (next !== undefined && next > Math.floor(Date.now() / 1000)) {
   115	        throw new HeartbeatRateLimitedError(next);
   116	      }
   117	      throw err;
   118	    }
   119	  }
   120	
   121	  async isHeartbeatDue(): Promise<boolean> {
   122	    const next = await this.readNextHeartbeatAt();
   123	    return next <= Math.floor(Date.now() / 1000);
   124	  }
   125	
   126	  private async readNextHeartbeatAt(): Promise<number> {
   127	    const state = await this.publicClient.readContract({
   128	      address: this.contractAddress,
   129	      abi: HEARTBEAT_ABI,
   130	      functionName: 'getWorldState',
   131	      args: [],
   132	    });
   133	    // viem decodes the named tuple into an object with the same field names.
   134	    return Number((state as { nextHeartbeatAtTs: bigint }).nextHeartbeatAtTs);
   135	  }
   136	}
   137	
   138	function normalizePk(pk: string): `0x${string}` {
   139	  const trimmed = pk.trim();
   140	  const withPrefix = trimmed.startsWith('0x') ? trimmed : `0x${trimmed}`;
   141	  if (!/^0x[0-9a-fA-F]{64}$/.test(withPrefix)) {
   142	    throw new Error(
   143	      'RUNNER_PRIVATE_KEY is not a valid 64-hex-char private key (0x-prefixed optional)',
   144	    );
   145	  }
   146	  return withPrefix as `0x${string}`;
   147	}

---
     1	import { describe, expect, it } from 'vitest';
     2	import { decodeFunctionResult, encodeAbiParameters } from 'viem';
     3	import { CLAN_WORLD_ABI } from '../src/adapters/IChainClient';
     4	
     5	const ZERO_BYTES32 = `0x${'00'.repeat(32)}` as `0x${string}`;
     6	
     7	const worldStateTuple = [
     8	  {
     9	    type: 'tuple',
    10	    components: [
    11	      { name: 'currentTick', type: 'uint64' },
    12	      { name: 'seasonStartTick', type: 'uint64' },
    13	      { name: 'seasonEndTick', type: 'uint64' },
    14	      { name: 'seasonFinalized', type: 'bool' },
    15	      { name: 'currentSeasonNumber', type: 'uint64' },
    16	      { name: 'nextHeartbeatAtTick', type: 'uint64' },
    17	      { name: 'nextHeartbeatAtTs', type: 'uint64' },
    18	      { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
    19	      { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
    20	      { name: 'currentTickSeed', type: 'bytes32' },
    21	      { name: 'activeBanditId', type: 'uint32' },
    22	      { name: 'winterActive', type: 'bool' },
    23	      { name: 'winterStartsAtTick', type: 'uint64' },
    24	      { name: 'winterEndsAtTick', type: 'uint64' },
    25	      { name: 'nextCommitSequence', type: 'uint64' },
    26	    ],
    27	  },
    28	] as const;
    29	
    30	const worldSnapshotTuple = [
    31	  {
    32	    type: 'tuple',
    33	    components: [
    34	      { name: 'currentTick', type: 'uint64' },
    35	      { name: 'seasonStartTick', type: 'uint64' },
    36	      { name: 'seasonEndTick', type: 'uint64' },
    37	      { name: 'seasonFinalized', type: 'bool' },
    38	      { name: 'currentSeasonNumber', type: 'uint64' },
    39	      { name: 'nextHeartbeatAtTick', type: 'uint64' },
    40	      { name: 'winterActive', type: 'bool' },
    41	      { name: 'winterStartsAtTick', type: 'uint64' },
    42	      { name: 'winterEndsAtTick', type: 'uint64' },
    43	      { name: 'activeBanditId', type: 'uint32' },
    44	      { name: 'currentTickSeed', type: 'bytes32' },
    45	      {
    46	        name: 'leaderboard',
    47	        type: 'tuple[]',
    48	        components: [
    49	          { name: 'clanId', type: 'uint32' },
    50	          { name: 'owner', type: 'address' },
    51	          { name: 'monumentLevel', type: 'uint8' },
    52	          { name: 'baseLevel', type: 'uint8' },
    53	          { name: 'wallLevel', type: 'uint8' },
    54	          { name: 'livingClansmen', type: 'uint8' },
    55	          { name: 'state', type: 'uint8' },
    56	          { name: 'lootValue', type: 'uint256' },
    57	        ],
    58	      },
    59	    ],
    60	  },
    61	] as const;
    62	
    63	describe('ClanWorld generated ABI tuple decoding', () => {
    64	  it('decodes a known-good getWorldState() result with the current Solidity order', () => {
    65	    const data = encodeAbiParameters(worldStateTuple, [
    66	      {
    67	        currentTick: 11n,
    68	        seasonStartTick: 1n,
    69	        seasonEndTick: 360n,
    70	        seasonFinalized: false,
    71	        currentSeasonNumber: 7n,
    72	        nextHeartbeatAtTick: 12n,
    73	        nextHeartbeatAtTs: 1_900_000_001n,
    74	        nextBanditSpawnEligibleTick: 21n,
    75	        currentBanditSpawnChanceBps: 3000,
    76	        currentTickSeed: ZERO_BYTES32,
    77	        activeBanditId: 42,
    78	        winterActive: true,
    79	        winterStartsAtTick: 100n,
    80	        winterEndsAtTick: 110n,
    81	        nextCommitSequence: 9n,
    82	      },
    83	    ]);
    84	
    85	    const state = decodeFunctionResult({
    86	      abi: CLAN_WORLD_ABI,
    87	      functionName: 'getWorldState',
    88	      data,
    89	    }) as Record<string, unknown>;
    90	
    91	    expect(state.currentSeasonNumber).toBe(7n);
    92	    expect(state.nextHeartbeatAtTick).toBe(12n);
    93	    expect(state.nextHeartbeatAtTs).toBe(1_900_000_001n);
    94	    expect(state.winterActive).toBe(true);
    95	  });
    96	
    97	  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
    98	    const data = encodeAbiParameters(worldSnapshotTuple, [
    99	      {
   100	        currentTick: 33n,
   101	        seasonStartTick: 0n,
   102	        seasonEndTick: 360n,
   103	        seasonFinalized: false,
   104	        currentSeasonNumber: 2n,
   105	        nextHeartbeatAtTick: 34n,
   106	        winterActive: true,
   107	        winterStartsAtTick: 100n,
   108	        winterEndsAtTick: 110n,
   109	        activeBanditId: 3,
   110	        currentTickSeed: ZERO_BYTES32,
   111	        leaderboard: [],
   112	      },
   113	    ]);
   114	
   115	    const snapshot = decodeFunctionResult({
   116	      abi: CLAN_WORLD_ABI,
   117	      functionName: 'getWorldSnapshot',
   118	      data,
   119	    }) as Record<string, unknown>;
   120	
   121	    expect(snapshot.currentSeasonNumber).toBe(2n);
   122	    expect(snapshot.nextHeartbeatAtTick).toBe(34n);
   123	    expect(snapshot.winterActive).toBe(true);
   124	    expect(snapshot.activeBanditId).toBe(3);
   125	  });
   126	});

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '3465,3498p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3465	        return WorldSnapshot({
  3466	            currentTick: _world.currentTick,
  3467	            seasonStartTick: _world.seasonStartTick,
  3468	            seasonEndTick: _world.seasonEndTick,
  3469	            seasonFinalized: _world.seasonFinalized,
  3470	            currentSeasonNumber: _world.currentSeasonNumber,
  3471	            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
  3472	            winterActive: _world.winterActive,
  3473	            winterStartsAtTick: _world.winterStartsAtTick,
  3474	            winterEndsAtTick: _world.winterEndsAtTick,
  3475	            activeBanditId: _world.activeBanditId,
  3476	            currentTickSeed: _world.currentTickSeed,
  3477	            leaderboard: lb
  3478	        });
  3479	    }
  3480	
  3481	    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
  3482	        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
  3483	        DerivedClanState memory derivedClan = _derivedClanStateFromSimulation(sim.clan);
  3484	
  3485	        ClansmanFullView[] memory clansmen = new ClansmanFullView[](sim.clansmen.length);
  3486	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  3487	            Clansman memory cs = sim.clansmen[i];
  3488	            Mission memory m = sim.missions[i];
  3489	            DerivedClansmanState memory dcs = DerivedClansmanState({
  3490	                clansman: cs,
  3491	                activeMission: m,
  3492	                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
  3493	                derivedAtTick: _world.currentTick
  3494	            });
  3495	            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
  3496	        }
  3497	
  3498	        uint32 thisClanDefendingBaseId = 0;

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1698,1850p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1843,1960p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1960,2065p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
  1906	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1907	            uint32 defenderClanId = defendingClans[i];
  1908	            if (_clanHasActiveDefenderForTarget(defenderClanId, targetClanId, targetRegion)) {
  1909	                clanIds[out++] = defenderClanId;
  1910	            }
  1911	        }
  1912	    }
  1913	
  1914	    function _clanHasActiveDefenderForTarget(uint32 defenderClanId, uint32 targetClanId, uint8 targetRegion)
  1915	        internal
  1916	        view
  1917	        returns (bool)
  1918	    {
  1919	        Clan storage defenderClan = _clans[defenderClanId];
  1920	        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
  1921	            return false;
  1922	        }
  1923	
  1924	        uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
  1925	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  1926	            uint32 clansmanId = clansmanIds[i];
  1927	            Clansman storage cs = _clansmen[clansmanId];
  1928	            Mission storage mission = _missions[clansmanId];
  1929	            if (
  1930	                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
  1931	                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
  1932	            ) {
  1933	                return true;
  1934	            }
  1935	        }
  1936	        return false;
  1937	    }
  1938	
  1939	    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
  1940	        internal
  1941	        view
  1942	        returns (uint32 totalDefense)
  1943	    {
  1944	        uint8 targetRegion = _clans[targetClanId].baseRegion;
  1945	        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
  1946	
  1947	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1948	            uint32 defenderClanId = defendingClans[i];
  1949	            Clan storage defenderClan = _clans[defenderClanId];
  1950	            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
  1951	                continue;
  1952	            }
  1953	
  1954	            uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
  1955	            for (uint256 j = 0; j < clansmanIds.length; j++) {
  1956	                uint32 clansmanId = clansmanIds[j];
  1957	                Clansman storage cs = _clansmen[clansmanId];
  1958	                Mission storage mission = _missions[clansmanId];
  1959	                if (
  1960	                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active

---
  1960	                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
  1961	                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
  1962	                ) {
  1963	                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
  1964	                }
  1965	            }
  1966	        }
  1967	    }
  1968	
  1969	    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
  1970	        internal
  1971	        returns (uint32 remainingDamage, uint32 wallDamage)
  1972	    {
  1973	        remainingDamage = incomingDamage;
  1974	        if (remainingDamage == 0 || clan.wallLevel == 0) {
  1975	            return (remainingDamage, 0);
  1976	        }
  1977	
  1978	        wallDamage = remainingDamage < WALL_HP_PER_LEVEL ? remainingDamage : WALL_HP_PER_LEVEL;
  1979	        remainingDamage -= wallDamage;
  1980	        if (wallDamage >= WALL_HP_PER_LEVEL) {
  1981	            if (clan.wallLevel > 0) {
  1982	                clan.wallLevel--;
  1983	            }
  1984	            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
  1985	        }
  1986	    }
  1987	
  1988	    function _applyBanditBaseDefense(Clan storage clan, uint32 incomingDamage)
  1989	        internal
  1990	        view
  1991	        returns (uint32 remainingDamage, uint32 baseAbsorbed)
  1992	    {
  1993	        remainingDamage = incomingDamage;
  1994	        if (remainingDamage == 0 || clan.baseLevel == 0) {
  1995	            return (remainingDamage, 0);
  1996	        }
  1997	
  1998	        uint32 baseDefense = uint32(clan.baseLevel) * BASE_HP_PER_LEVEL;
  1999	        baseAbsorbed = remainingDamage < baseDefense ? remainingDamage : baseDefense;
  2000	        remainingDamage -= baseAbsorbed;
  2001	    }
  2002	
  2003	    function _applyBanditClansmanCasualties(
  2004	        Clan storage clan,
  2005	        uint32 clanId,
  2006	        uint32 banditId,
  2007	        uint32 incomingDamage,
  2008	        bytes32 tickSeed
  2009	    ) internal returns (uint32 damageAbsorbed) {
  2010	        uint32 remainingDamage = incomingDamage;
  2011	        uint32 killIndex = 0;
  2012	        while (remainingDamage > 0) {
  2013	            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
  2014	            if (victimId == 0) {
  2015	                break;
  2016	            }
  2017	
  2018	            Clansman storage victim = _clansmen[victimId];
  2019	            _markClansmanDead(clan, victim);
  2020	
  2021	            uint32 absorbed = remainingDamage < CLANSMAN_HP ? remainingDamage : CLANSMAN_HP;
  2022	            damageAbsorbed += absorbed;
  2023	            remainingDamage -= absorbed;
  2024	            emit ClansmanKilledByBandit(clanId, victimId, banditId);
  2025	            killIndex++;
  2026	
  2027	            if (clan.livingClansmen == 0) {
  2028	                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
  2029	                break;
  2030	            }
  2031	        }
  2032	    }
  2033	
  2034	    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
  2035	        internal
  2036	        view
  2037	        returns (uint32 victimId)
  2038	    {
  2039	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
  2040	        uint256 livingCount;
  2041	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  2042	            if (_clansmen[clansmanIds[i]].state != ClansmanState.DEAD) {
  2043	                livingCount++;
  2044	            }
  2045	        }
  2046	        if (livingCount == 0) {
  2047	            return 0;
  2048	        }
  2049	
  2050	        uint256 pick =
  2051	            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
  2052	        uint256 seen;
  2053	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  2054	            uint32 candidateId = clansmanIds[i];
  2055	            if (_clansmen[candidateId].state == ClansmanState.DEAD) {
  2056	                continue;
  2057	            }
  2058	            if (seen == pick) {
  2059	                return candidateId;
  2060	            }
  2061	            seen++;
  2062	        }
  2063	    }
  2064	
  2065	    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '544,575p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   544	
   545	        uint32[] storage csIds = _clanClansmanIds[clanId];
   546	        for (uint256 i = 0; i < csIds.length; i++) {
   547	            Clansman storage cs = _clansmen[csIds[i]];
   548	            cs.state = ClansmanState.DEAD;
   549	            cs.cooldownEndsAtTs = 0;
   550	            Mission storage mission = _missions[csIds[i]];
   551	            if (mission.active) {
   552	                if (mission.action == ActionType.DefendBase) {
   553	                    _clearDefender(csIds[i]);
   554	                }
   555	                mission.active = false;
   556	            }
   557	        }
   558	
   559	        _releaseDefendersForDeadTarget(clanId, baseRegion);
   560	        _abortBanditAttacksForDeadTarget(clanId, excludedBanditId);
   561	
   562	        emit ClanEliminated(clanId, tick);
   563	    }
   564	
   565	    function _releaseDefendersForDeadTarget(uint32 deadClanId, uint8 baseRegion) internal {
   566	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   567	            uint32 defenderClanId = _allClanIds[i];
   568	            if (defenderClanId == deadClanId) continue;
   569	
   570	            uint32[] storage csIds = _clanClansmanIds[defenderClanId];
   571	            for (uint256 j = 0; j < csIds.length; j++) {
   572	                uint32 clansmanId = csIds[j];
   573	                Mission storage mission = _missions[clansmanId];
   574	                if (
   575	                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '596,620p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   596	            for (uint256 i = 0; i < regionBandits.length; i++) {
   597	                uint32 banditId = regionBandits[i];
   598	                if (banditId == excludedBanditId) continue;
   599	
   600	                BanditTroop storage bandit = _bandits[banditId];
   601	                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
   602	                    _transitionBanditState(banditId, BanditState.Escaped);
   603	                    emit BanditEscaped(banditId, currentTick);
   604	                    emit BanditTargetDied(banditId, deadClanId, currentTick);
   605	                }
   606	            }
   607	        }
   608	    }
   609	
   610	    /// @dev Check if a clan is currently starving (lazy read).
   611	    function _isStarving(Clan storage clan) internal view returns (bool) {
   612	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   613	    }
   614	
   615	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   616	    function _resolveAction(
   617	        Clan storage clan,
   618	        Clansman storage cs,
   619	        Mission storage m,
   620	        uint32 clanId,

codex
I’m at the point where the main remaining questions are “does it compile cleanly?” and “do the new tests actually execute under this tree?” I’ll try a narrow test invocation next; if the sandbox blocks writes, I’ll fall back to source-only verification and call that out explicitly in the review.
exec
/bin/bash -c 'forge test --match-contract BanditAttackResolutionTest' in /home/claude/code/clan-world/packages/contracts
 exited 127 in 0ms:
/bin/bash: line 1: forge: command not found

exec
/bin/bash -c 'pnpm --dir /home/claude/code/clan-world/packages/shared test' in /home/claude/code/clan-world
 exited 1 in 332ms:

> @clan-world/shared@0.0.0 test /home/claude/code/omnipass-world/clan-world/packages/shared
> vitest run

sh: 1: vitest: not found
 ELIFECYCLE  Test failed. See above for more details.

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '615,760p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '342,440p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   615	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   616	    function _resolveAction(
   617	        Clan storage clan,
   618	        Clansman storage cs,
   619	        Mission storage m,
   620	        uint32 clanId,
   621	        uint64 tick,
   622	        bytes32 tickSeed
   623	    ) internal {
   624	        bool starving = _isStarving(clan);
   625	        ActionType action = m.action;
   626	
   627	        if (action == ActionType.ChopWood) {
   628	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   629	        } else if (action == ActionType.MineIron) {
   630	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   631	        } else if (action == ActionType.FishDocks) {
   632	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   633	        } else if (action == ActionType.FishDeepSea) {
   634	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   635	        } else if (action == ActionType.HarvestWheat) {
   636	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   637	        } else if (action == ActionType.DepositResources) {
   638	            _doDeposit(clan, cs, m, clanId, tick);
   639	        } else if (action == ActionType.Wait) {
   640	            // NOOP — worker stays ACTING (continuous), no transition needed
   641	            // Wait mission is effectively persistent until interrupted
   642	        } else if (action == ActionType.DefendBase) {
   643	            // Persistent mission. Registration happens atomically at order submission.
   644	        } else if (
   645	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   646	        ) {
   647	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   648	            _doBuilding(clan, cs, m, clanId, tick, action);
   649	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   650	            // Scheduled market actions: already enqueued at submitClanOrders time.
   651	            // Settlement resolves this action slot — just complete the mission.
   652	            // (Actual execution happened or will happen at heartbeat.)
   653	            _completeMission(cs, m);
   654	        }
   655	    }
   656	
   657	    // -------------------------------------------------------------------------
   658	    // Gathering helpers
   659	    // -------------------------------------------------------------------------
   660	
   661	    function _gatherWood(
   662	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   663	        Clansman storage cs,
   664	        Mission storage m,
   665	        uint32 clanId,
   666	        uint64 tick,
   667	        bool starving,
   668	        bytes32 tickSeed
   669	    ) internal {
   670	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
   671	        if (remaining == 0) {
   672	            _completeMission(cs, m);
   673	            return;
   674	        }
   675	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
   676	        // Crit roll: domain-separated RNG
   677	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   678	        uint256 critRoll = uint256(critRng) % 10000;
   679	        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
   680	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
   681	        }
   682	        if (starving) yield = yield / 2;
   683	        if (yield > remaining) yield = remaining;
   684	        cs.carryWood += yield;
   685	
   686	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   687	
   688	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
   689	            _completeMission(cs, m);
   690	        }
   691	        // else continuous — worker stays ACTING
   692	    }
   693	
   694	    function _gatherIron(
   695	        Clan storage clan,
   696	        Clansman storage cs,
   697	        Mission storage m,
   698	        uint32 clanId,
   699	        uint64 tick,
   700	        bool starving,
   701	        bytes32 tickSeed
   702	    ) internal {
   703	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   704	        if (remaining == 0) {
   705	            _completeMission(cs, m);
   706	            return;
   707	        }
   708	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   709	        if (starving) yield = yield / 2;
   710	        if (yield > remaining) yield = remaining;
   711	        cs.carryIron += yield;
   712	
   713	        // Gold bonus roll — scoped to reduce stack depth
   714	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   715	
   716	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   717	
   718	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   719	            _completeMission(cs, m);
   720	        }
   721	    }
   722	
   723	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   724	        internal
   725	        returns (uint256 goldBonus)
   726	    {
   727	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   728	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   729	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   730	            clan.goldBalance += goldBonus;
   731	        }
   732	    }
   733	
   734	    function _gatherFishDocks(
   735	        Clan storage,
   736	        Clansman storage cs,
   737	        Mission storage m,
   738	        uint32 clanId,
   739	        uint64 tick,
   740	        bool starving,
   741	        bytes32 tickSeed
   742	    ) internal {
   743	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   744	        if (remaining == 0) {
   745	            _completeMission(cs, m);
   746	            return;
   747	        }
   748	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   749	        uint256 fishRoll = uint256(fishRng) % 10000;
   750	        uint256 yield = 0;
   751	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   752	            yield = 1e18;
   753	        }
   754	        if (starving) yield = yield / 2;
   755	        if (yield > remaining) yield = remaining;
   756	        if (yield > 0) {
   757	            cs.carryFish += yield;
   758	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   759	        }
   760	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {

---
   342	
   343	    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
   344	    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
   345	    function _settleMissionForClansman(
   346	        Clan storage clan,
   347	        Clansman storage cs,
   348	        uint32 clanId,
   349	        uint64 fromTick,
   350	        uint64 toTick
   351	    ) internal {
   352	        Mission storage m = _missions[cs.clansmanId];
   353	
   354	        // Path 6: dead clansman — invalidate active mission if any
   355	        if (cs.state == ClansmanState.DEAD) {
   356	            if (m.active) {
   357	                if (m.action == ActionType.DefendBase) {
   358	                    _clearDefender(cs.clansmanId);
   359	                }
   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
   361	            }
   362	            return;
   363	        }
   364	
   365	        if (!m.active) return; // no active mission — nothing to settle
   366	
   367	        bytes32 tickSeed;
   368	        for (uint64 tick = fromTick; tick < toTick; tick++) {
   369	            tickSeed = _tickSeeds[tick];
   370	
   371	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   372	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   373	                cs.state = ClansmanState.ACTING;
   374	                cs.currentRegion = m.targetRegion;
   375	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   376	
   377	                if (m.action == ActionType.DefendBase) {
   378	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   379	                }
   380	            }
   381	
   382	            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
   383	
   384	            // Path 3: ACTING at/past settlesAtTick → resolve
   385	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
   386	                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   387	                if (m.active && getActionDuration(m.action) > 0) {
   388	                    _completeMission(cs, m);
   389	                }
   390	            }
   391	
   392	            // If mission completed during this tick, stop iterating
   393	            if (!m.active) break;
   394	        }
   395	    }
   396	
   397	    /// @dev Lazy settlement of a clan forward to currentTick.
   398	    ///      Mutates storage. Called before order submission and by public settleClan().
   399	    function _settleClan(uint32 clanId) internal {
   400	        Clan storage clan = _clans[clanId];
   401	        if (clan.clanId == 0) return;
   402	
   403	        uint64 curTick = _world.currentTick;
   404	        uint64 fromTick = clan.lastSettledTick;
   405	        if (fromTick >= curTick) return;
   406	
   407	        // Cap ticks settled per call to prevent block gas limit issues
   408	        uint64 maxSettleTicks = 200;
   409	        if (curTick > fromTick + maxSettleTicks) {
   410	            curTick = fromTick + maxSettleTicks;
   411	        }
   412	
   413	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   414	
   415	        // Settle tick by tick from fromTick to curTick - 1
   416	        // (curTick is still open; we settle through the last closed tick)
   417	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   418	            // 1. Apply upkeep for this tick
   419	            _applyUpkeep(clan, tick);
   420	            if (clan.clanState == ClanState.DEAD) break;
   421	
   422	            // 2. Wheat plot regrow check (lazy, per tick)
   423	            for (uint256 pi = 0; pi < 2; pi++) {
   424	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   425	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   426	                    plot.state = WheatPlotState.Harvestable;
   427	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   428	                    plot.regrowUntilTick = 0;
   429	                }
   430	            }
   431	
   432	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   433	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   434	                Clansman storage cs = _clansmen[clansmanIds[i]];
   435	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   436	            }
   437	        }
   438	
   439	        clan.lastSettledTick = curTick;
   440	        emit ClanSettled(clanId, curTick);

exec
/bin/bash -lc "rg -n \"BlueprintAwarded|BlueprintEarned|BanditState\\.(NONE|CAMPING|RESTING|ATTACKING|DEFEATED|ESCAPED|None|Spawned|Camped|Resting|Attacking|Defeated|Escaped)|getBanditTroop\\(|getBandit\\(|getBanditsInRegion\\(\" packages apps" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:601:                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
packages/contracts/src/ClanWorld.sol:602:                    _transitionBanditState(banditId, BanditState.Escaped);
packages/contracts/src/ClanWorld.sol:1541:            state: BanditState.Spawned,
packages/contracts/src/ClanWorld.sol:1568:        _transitionBanditState(id, BanditState.Attacking);
packages/contracts/src/ClanWorld.sol:1574:        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
packages/contracts/src/ClanWorld.sol:1579:        if (newState == BanditState.Defeated) {
packages/contracts/src/ClanWorld.sol:1587:        if (newState != BanditState.Attacking) {
packages/contracts/src/ClanWorld.sol:1595:        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
packages/contracts/src/ClanWorld.sol:1596:        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
packages/contracts/src/ClanWorld.sol:1597:        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
packages/contracts/src/ClanWorld.sol:1598:        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
packages/contracts/src/ClanWorld.sol:1599:        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
packages/contracts/src/ClanWorld.sol:1604:        return newState == BanditState.Escaped
packages/contracts/src/ClanWorld.sol:1605:            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
packages/contracts/src/ClanWorld.sol:1609:        return newState == BanditState.Escaped
packages/contracts/src/ClanWorld.sol:1610:            || (newState == BanditState.Attacking
packages/contracts/src/ClanWorld.sol:1616:        return newState == BanditState.Defeated || newState == BanditState.Escaped;
packages/contracts/src/ClanWorld.sol:1620:        return newState == BanditState.Resting;
packages/contracts/src/ClanWorld.sol:1624:        return newState == BanditState.Escaped
packages/contracts/src/ClanWorld.sol:1625:            || (newState == BanditState.Camped
packages/contracts/src/ClanWorld.sol:1658:                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
packages/contracts/src/ClanWorld.sol:1675:                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
packages/contracts/src/ClanWorld.sol:1676:                    _transitionBanditState(banditId, BanditState.Camped);
packages/contracts/src/ClanWorld.sol:1678:                    bandit.state == BanditState.Camped
packages/contracts/src/ClanWorld.sol:1683:                        _transitionBanditState(banditId, BanditState.Escaped);
packages/contracts/src/ClanWorld.sol:1689:                    bandit.state == BanditState.Escaped
packages/contracts/src/ClanWorld.sol:1692:                    _transitionBanditState(banditId, BanditState.Resting);
packages/contracts/src/ClanWorld.sol:1694:                    bandit.state == BanditState.Resting
packages/contracts/src/ClanWorld.sol:1697:                    _transitionBanditState(banditId, BanditState.Camped);
packages/contracts/src/ClanWorld.sol:1750:                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
packages/contracts/src/ClanWorld.sol:1766:        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
packages/contracts/src/ClanWorld.sol:1776:            _transitionBanditState(banditId, BanditState.Escaped);
packages/contracts/src/ClanWorld.sol:1782:        if (bandit.state != BanditState.Attacking || bandit.targetClanId != targetClanId) {
packages/contracts/src/ClanWorld.sol:1786:            _transitionBanditState(banditId, BanditState.Escaped);
packages/contracts/src/ClanWorld.sol:1793:            bandit.state != BanditState.Attacking || bandit.targetClanId != targetClanId
packages/contracts/src/ClanWorld.sol:1835:            emit BlueprintEarned(targetClanId, banditId, BLUEPRINT_UNIT, closedTick);
packages/contracts/src/ClanWorld.sol:1836:            _transitionBanditState(banditId, BanditState.Defeated);
packages/contracts/src/ClanWorld.sol:1838:            _transitionBanditState(banditId, BanditState.Escaped);
packages/contracts/src/ClanWorld.sol:3259:    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
packages/contracts/src/ClanWorld.sol:3261:        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
packages/contracts/src/ClanWorld.sol:3265:                state: BanditState.None,
packages/contracts/src/ClanWorld.sol:3279:    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
packages/contracts/src/ClanWorld.sol:3280:        return getBandit(banditId);
packages/contracts/src/ClanWorld.sol:3283:    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
packages/contracts/src/ClanWorld.sol:3555:        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
packages/contracts/src/ClanWorld.sol:3557:            if (bandit.state == BanditState.Spawned) {
packages/contracts/src/ClanWorld.sol:3559:            } else if (bandit.state == BanditState.Camped) {
packages/contracts/src/ClanWorld.sol:3561:            } else if (bandit.state == BanditState.Resting) {
packages/contracts/src/IClanWorld.sol:603:    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
packages/contracts/src/IClanWorld.sol:604:    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
packages/contracts/src/IClanWorld.sol:734:    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
packages/contracts/src/IClanWorld.sol:736:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
packages/contracts/src/IClanWorld.sol:738:    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
packages/contracts/src/ClanWorldStub.sol:218:    function getBandit(uint32) public pure override returns (BanditTroop memory) {
packages/contracts/src/ClanWorldStub.sol:222:            state: BanditState.None,
packages/contracts/src/ClanWorldStub.sol:234:    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
packages/contracts/src/ClanWorldStub.sol:235:        return getBandit(banditId);
packages/contracts/src/ClanWorldStub.sol:238:    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
packages/contracts/src/ClanWorldStub.sol:351:            state: BanditState.None,
packages/contracts/abi/IClanWorld.json:2875:      "name": "BlueprintAwarded",
packages/contracts/abi/IClanWorld.json:2900:      "name": "BlueprintEarned",
packages/contracts/test/ClanWorldStub.t.sol:39:        BanditTroop memory bandit = stub.getBandit(999);
packages/contracts/test/ClanWorldStub.t.sol:42:        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
packages/contracts/test/BanditAttackResolution.t.sol:24:        _bandits[id].state = BanditState.Attacking;
packages/contracts/test/BanditAttackResolution.t.sol:96:    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint256 amount, uint64 tick);
packages/contracts/test/BanditAttackResolution.t.sol:276:        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
packages/contracts/test/BanditAttackResolution.t.sol:290:        emit BlueprintEarned(clanId, banditId, world.blueprintUnit(), world.getWorldState().currentTick);
packages/contracts/test/BanditAttackResolution.t.sol:312:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Camped), "spawned to camped");
packages/contracts/test/BanditAttackResolution.t.sol:314:        uint64 campedAt = world.getBandit(banditId).tickEnteredState;
packages/contracts/test/BanditAttackResolution.t.sol:324:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "attack resolved to escaped");
packages/contracts/test/BanditAttackResolution.t.sol:329:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Resting), "escaped recovered to resting");
packages/contracts/test/BanditAttackResolution.t.sol:375:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
packages/contracts/test/BanditAttackResolution.t.sol:376:        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
packages/contracts/test/BanditAttackResolution.t.sol:496:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped once");
packages/contracts/test/BanditAttackResolution.t.sol:497:        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
packages/contracts/test/BanditAttackResolution.t.sol:572:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
packages/contracts/test/BanditAttackResolution.t.sol:583:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
packages/contracts/test/BanditAttackResolution.t.sol:600:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
packages/contracts/test/BanditAttackResolution.t.sol:614:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
packages/contracts/test/BanditAttackResolution.t.sol:624:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
packages/contracts/test/BanditAttackResolution.t.sol:677:        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
packages/contracts/test/BanditAttackResolution.t.sol:678:        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
packages/contracts/test/Bandit.t.sol:55:        BanditTroop memory bandit = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:58:        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
packages/contracts/test/Bandit.t.sol:63:        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
packages/contracts/test/Bandit.t.sol:72:        BanditTroop memory bandit = world.getBandit(999);
packages/contracts/test/Bandit.t.sol:75:        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
packages/contracts/test/Bandit.t.sol:81:        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
packages/contracts/test/Bandit.t.sol:89:        BanditTroop memory bandit = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:90:        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
packages/contracts/test/Bandit.t.sol:100:        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
packages/contracts/test/Bandit.t.sol:110:        BanditTroop memory bandit = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:111:        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
packages/contracts/test/Bandit.t.sol:119:        world.transitionBandit(id, BanditState.Defeated);
packages/contracts/test/Bandit.t.sol:121:        BanditTroop memory deletedBandit = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:124:        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
packages/contracts/test/Bandit.t.sol:125:        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
packages/contracts/test/Bandit.t.sol:137:        world.transitionBandit(id1, BanditState.Defeated);
packages/contracts/test/Bandit.t.sol:140:        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
packages/contracts/test/Bandit.t.sol:146:        world.transitionBandit(id, BanditState.Escaped);
packages/contracts/test/Bandit.t.sol:147:        BanditTroop memory escaped = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:148:        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
packages/contracts/test/Bandit.t.sol:151:        world.transitionBandit(id, BanditState.Resting);
packages/contracts/test/Bandit.t.sol:152:        BanditTroop memory resting = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:153:        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
packages/contracts/test/Bandit.t.sol:158:        BanditTroop memory camped = world.getBandit(id);
packages/contracts/test/Bandit.t.sol:159:        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
packages/contracts/test/Bandit.t.sol:167:        world.transitionBandit(id, BanditState.Defeated);
packages/contracts/test/Bandit.t.sol:173:        world.transitionBandit(id, BanditState.Attacking);
packages/contracts/test/Bandit.t.sol:176:        world.transitionBandit(id, BanditState.None);
packages/contracts/test/Bandit.t.sol:184:        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
packages/contracts/test/Bandit.t.sol:189:        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
packages/contracts/test/Bandit.t.sol:196:        world.transitionBandit(id1, BanditState.Defeated);
packages/contracts/test/Bandit.t.sol:198:        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
packages/contracts/test/BanditSpawn.t.sol:210:        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
packages/contracts/test/BanditSpawn.t.sol:239:        uint8 selectedRegion = world.getBandit(1).region;
packages/contracts/test/BanditSpawn.t.sol:262:        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
packages/contracts/test/BanditSpawn.t.sol:354:        BanditTroop memory bandit = world.getBandit(1);
packages/contracts/test/BanditSpawn.t.sol:355:        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
packages/contracts/test/BanditSpawn.t.sol:378:        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
packages/contracts/test/BanditSpawn.t.sol:397:        BanditTroop memory bandit = world.getBandit(1);
packages/contracts/test/BanditSpawn.t.sol:399:        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");

exec
/bin/bash -lc "cat tsconfig.json && printf '\\n---\\n' && cat packages/shared/tsconfig.json && printf '\\n---\\n' && cat packages/runner/tsconfig.json && printf '\\n---\\n' && cat packages/shared/package.json && printf '\\n---\\n' && cat packages/runner/package.json" in /home/claude/code/clan-world
 succeeded in 0ms:
{
  "extends": "./tsconfig.base.json",
  "compilerOptions": {
    "noEmit": true
  },
  "include": [],
  "files": []
}

---
{
  "extends": "../../tsconfig.base.json",
  "compilerOptions": {
    "noEmit": true
  },
  "include": ["src/**/*", "test/**/*"]
}

---
{
  "extends": "../../tsconfig.base.json",
  "compilerOptions": {
    "noEmit": true,
    "types": ["node"]
  },
  "include": ["src/**/*", "test/**/*"]
}

---
{
  "name": "@clan-world/shared",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "main": "./src/index.ts",
  "types": "./src/index.ts",
  "exports": {
    ".": "./src/index.ts",
    "./adapters": "./src/adapters/index.ts"
  },
  "scripts": {
    "build": "tsc -b",
    "test": "vitest run",
    "typecheck": "tsc --noEmit",
    "lint": "echo 'lint stub'",
    "clean": "rm -rf dist .turbo"
  },
  "devDependencies": {
    "typescript": "5.9.3",
    "vitest": "^3.2.0"
  },
  "dependencies": {
    "convex": "1.17.4",
    "viem": "^2.48.4"
  }
}

---
{
  "name": "@clan-world/runner",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "main": "./src/main.ts",
  "scripts": {
    "dev": "tsx watch src/main.ts",
    "start": "tsx --env-file=../../.env.local src/main.ts",
    "build": "echo 'uses tsx at runtime; no emit build needed'",
    "typecheck": "tsc --noEmit",
    "test": "vitest run",
    "lint": "echo 'lint stub'",
    "clean": "rm -rf dist .turbo"
  },
  "dependencies": {
    "@0glabs/0g-ts-sdk": "0.3.3",
    "@clan-world/agents": "workspace:*",
    "@clan-world/shared": "workspace:*",
    "ethers": "6.13.1",
    "tsx": "4.19.2",
    "viem": "^2.48.4"
  },
  "devDependencies": {
    "@types/node": "25.6.0",
    "typescript": "5.9.3",
    "vitest": "^3.2.0"
  }
}

exec
/bin/bash -lc 'cat tsconfig.base.json' in /home/claude/code/clan-world
 succeeded in 0ms:
{
  "$schema": "https://json.schemastore.org/tsconfig",
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2022"],
    "module": "ESNext",
    "moduleResolution": "bundler",
    "moduleDetection": "force",
    "verbatimModuleSyntax": true,
    "esModuleInterop": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noImplicitOverride": true,
    "noFallthroughCasesInSwitch": true,
    "noPropertyAccessFromIndexSignature": false,
    "forceConsistentCasingInFileNames": true,
    "skipLibCheck": true,
    "declaration": true,
    "declarationMap": true,
    "sourceMap": true,
    "incremental": true
  },
  "exclude": ["node_modules", "dist", ".turbo", ".next", "out", "coverage"]
}

exec
/bin/bash -lc "rg -n \"_world\\.currentTick\" packages/contracts/src/ClanWorld.sol | sed -n '1,200p'" in /home/claude/code/clan-world
 succeeded in 0ms:
132:        _world.currentTick = 0;
403:        uint64 curTick = _world.currentTick;
525:        _markClanDead(clanId, "unknown", _world.currentTick, ClanWorldConstants.BANDIT_ID_NULL);
593:        uint64 currentTick = _world.currentTick;
612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
1543:            tickEnteredState: _world.currentTick,
1555:        spawnState.lastSpawnTick = _world.currentTick;
1580:            emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
1586:        bandit.tickEnteredState = _world.currentTick;
1591:        emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
1605:            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
1612:                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS);
1626:                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS);
1669:        require(_world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
1733:        uint256 selected = RNG.rngBounded(_world.currentTickSeed, DOMAIN_BANDIT_TARGET_PICK, bandit.id, tiedCount);
1763:        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");
1799:        bytes32 tickSeed = _world.currentTickSeed;
2028:                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
2096:            if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
2157:        require(_world.currentTick == closedTick, "ClanWorld: eager settle tick mismatch");
2175:        if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
2180:        return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
2238:        uint256 startIndex = uint256(_world.currentTick) % clanCount;
2308:        uint64 closedTick = _world.currentTick;
2309:        bytes32 closedTickSeed = _world.currentTickSeed;
2339:        _world.currentTick = newTick;
2341:        _world.currentTickSeed = newSeed;
2467:        clan.lastSettledTick = _world.currentTick;
2511:        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
2532:            if (_world.currentTick > lastSettled + 200) {
2648:        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
2692:            _world.currentTick,
2708:        m.submittedAtTick = _world.currentTick;
2716:        m.startTick = _world.currentTick;
2719:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
3343:        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
3352:                clansman: cs, activeMission: emptyMission, effectiveRegion: 0, derivedAtTick: _world.currentTick
3356:        SettlementSimulation memory sim = _simulateSettleToTick(cs.clanId, _world.currentTick);
3364:                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
3365:                derivedAtTick: _world.currentTick
3373:            effectiveRegion: _effectiveRegion(cs, fallbackMission, _world.currentTick),
3374:            derivedAtTick: _world.currentTick
3405:        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
3415:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
3417:            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
3466:            currentTick: _world.currentTick,
3476:            currentTickSeed: _world.currentTickSeed,
3482:        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
3492:                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
3493:                derivedAtTick: _world.currentTick
3535:            currentTick: _world.currentTick,
3536:            currentTickQueue: _scheduledMarketActions[_world.currentTick],
3537:            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1668,1705p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '3110,3145p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

---
  3110	            // _resolveAction records mission completion but does not execute any swap.
  3111	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
  3112	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
  3113	            }
  3114	        }
  3115	
  3116	        cs; // suppress unused warning
  3117	        return StatusCode.OK;
  3118	    }
  3119	
  3120	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  3121	        internal
  3122	        view
  3123	        returns (StatusCode)
  3124	    {
  3125	        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
  3126	        Clan storage targetClan = _clans[targetClanId];
  3127	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
  3128	            return StatusCode.ERR_INVALID_TARGET;
  3129	        }
  3130	        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  3131	        return StatusCode.OK;
  3132	    }
  3133	
  3134	    // =========================================================================
  3135	    // TREASURY / POOL SEEDING
  3136	    // =========================================================================
  3137	
  3138	    /// @notice One-time treasury initialization: register token and pool addresses.
  3139	    ///         Must be called before seedPools. Callable only once.
  3140	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  3141	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  3142	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  3143	
  3144	        _treasury.woodToken = tokens[0];
  3145	        _treasury.ironToken = tokens[1];

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '2298,2345p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2298	    ///         Execution order per spec §4.2 (CEI-safe):
  2299	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
  2300	    ///         1. Settle missions completing this tick.
  2301	    ///         2. Execute scheduled market actions for closedTick (external calls).
  2302	    ///         3. Eager-settle clans touched by world events (Phase 9 stub).
  2303	    ///         4. Advance bandit timers and resolve closed-tick bandit/world events.
  2304	    ///         5. Increment tick and publish the next tick seed atomically.
  2305	    function heartbeat() external override nonReentrant {
  2306	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
  2307	
  2308	        uint64 closedTick = _world.currentTick;
  2309	        bytes32 closedTickSeed = _world.currentTickSeed;
  2310	
  2311	        // CEI: update rate-limit guard before any external calls
  2312	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
  2313	
  2314	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
  2315	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  2316	        _settleCompletingMissions(closedTick);
  2317	
  2318	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
  2319	        _executeScheduledMarketActions(closedTick);
  2320	
  2321	        // Step 3: Eager-settle bases and active defenders in bandit spawn-candidate regions.
  2322	        _eagerSettleForBandits(closedTick);
  2323	
  2324	        // Step 4: Advance deterministic bandit timers for the closed tick.
  2325	        _advanceBanditStates(closedTick);
  2326	
  2327	        // Step 5: Resolve deterministic bandit attacks for the closed tick.
  2328	        _resolveAttackingBandits(closedTick);
  2329	
  2330	        // Step 6: Evaluate deterministic bandit spawns for the closed tick.
  2331	        _evaluateBanditSpawns(closedTickSeed);
  2332	
  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
  2334	        _resolveWorldEvents(closedTick);
  2335	
  2336	        // Step 8: Increment tick and publish the opened tick seed as one visible state transition.
  2337	        uint64 newTick = closedTick + 1;
  2338	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick));
  2339	        _world.currentTick = newTick;
  2340	        _tickSeeds[newTick] = newSeed;
  2341	        _world.currentTickSeed = newSeed;
  2342	        _world.nextHeartbeatAtTick = newTick + 1;
  2343	
  2344	        emit TickAdvanced(closedTick, newTick, newSeed);
  2345	    }

codex
The source pass is down to two likely review items: one in view-simulation starvation handling, and one possible stale-state issue during target selection before a camped bandit attacks. I’m waiting on the parallel passes now to see whether they independently confirm those or uncover anything else in the ABI/tests lane.
collab: Wait
exec
/bin/bash -lc 'rg -n "_pickBanditAttackTarget|target preview|BanditTargetPreview|getBanditTargetPreview|activeBanditId|currentBanditSpawnChanceBps" packages/contracts/test packages/shared/test packages/runner/src packages/shared/src' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/test/BanditAttackResolution.t.sol:306:            banditId = world.getWorldState().activeBanditId;
packages/contracts/test/Bandit.t.sol:68:        assertEq(state.activeBanditId, id, "active bandit");
packages/contracts/test/Bandit.t.sol:126:        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
packages/contracts/test/Bandit.t.sol:139:        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
packages/contracts/test/BanditSpawn.t.sol:294:        assertEq(state.currentBanditSpawnChanceBps, 4321, "preview chance refreshed");
packages/contracts/test/BanditSpawn.t.sol:401:        assertEq(state.activeBanditId, bandit.id, "active bandit set");
packages/shared/test/clanWorldAbi.test.ts:19:      { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
packages/shared/test/clanWorldAbi.test.ts:21:      { name: 'activeBanditId', type: 'uint32' },
packages/shared/test/clanWorldAbi.test.ts:43:      { name: 'activeBanditId', type: 'uint32' },
packages/shared/test/clanWorldAbi.test.ts:75:        currentBanditSpawnChanceBps: 3000,
packages/shared/test/clanWorldAbi.test.ts:77:        activeBanditId: 42,
packages/shared/test/clanWorldAbi.test.ts:109:        activeBanditId: 3,
packages/shared/test/clanWorldAbi.test.ts:124:    expect(snapshot.activeBanditId).toBe(3);

collab: Wait
collab: Wait
exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '565,590p' && printf '\\n---\\n' && nl -ba packages/contracts/src/ClanWorld.sol | sed -n '371,381p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   565	    function _releaseDefendersForDeadTarget(uint32 deadClanId, uint8 baseRegion) internal {
   566	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   567	            uint32 defenderClanId = _allClanIds[i];
   568	            if (defenderClanId == deadClanId) continue;
   569	
   570	            uint32[] storage csIds = _clanClansmanIds[defenderClanId];
   571	            for (uint256 j = 0; j < csIds.length; j++) {
   572	                uint32 clansmanId = csIds[j];
   573	                Mission storage mission = _missions[clansmanId];
   574	                if (
   575	                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
   576	                        && _clansmanDefendingRegion[clansmanId] == baseRegion
   577	                ) {
   578	                    _clearDefender(clansmanId);
   579	                    mission.active = false;
   580	
   581	                    Clansman storage defender = _clansmen[clansmanId];
   582	                    if (defender.state != ClansmanState.DEAD) {
   583	                        defender.state = ClansmanState.WAITING;
   584	                    }
   585	                }
   586	            }
   587	        }
   588	    }
   589	
   590	    function _abortBanditAttacksForDeadTarget(uint32 deadClanId, uint32 excludedBanditId) internal {

---
   371	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   372	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   373	                cs.state = ClansmanState.ACTING;
   374	                cs.currentRegion = m.targetRegion;
   375	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   376	
   377	                if (m.action == ActionType.DefendBase) {
   378	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   379	                }
   380	            }
   381	

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '2156,2192p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2156	    function _eagerSettleForBandits(uint64 closedTick) internal {
  2157	        require(_world.currentTick == closedTick, "ClanWorld: eager settle tick mismatch");
  2158	        if (_activeBanditCount >= MAX_TOTAL_BANDITS) return;
  2159	
  2160	        uint256[] memory regionWeights = _banditSpawnRegionWeights();
  2161	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  2162	            if (!_isBanditSpawnRegionCandidate(regionWeights, region)) {
  2163	                continue;
  2164	            }
  2165	            _eagerSettleBanditCandidateRegion(region);
  2166	        }
  2167	    }
  2168	
  2169	    function _isBanditSpawnRegionCandidate(uint256[] memory regionWeights, uint8 region) internal view returns (bool) {
  2170	        if (regionWeights[region - 1] == 0 || _banditsByRegion[region].length >= MAX_BANDITS_PER_REGION) {
  2171	            return false;
  2172	        }
  2173	
  2174	        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
  2175	        if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
  2176	            return false;
  2177	        }
  2178	
  2179	        uint16 nextProbability = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
  2180	        return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
  2181	    }
  2182	
  2183	    function _eagerSettleBanditCandidateRegion(uint8 region) internal {
  2184	        uint256 clanScanCount = _allClanIds.length < MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION
  2185	            ? _allClanIds.length
  2186	            : MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION;
  2187	
  2188	        for (uint256 i = 0; i < clanScanCount; i++) {
  2189	            uint32 clanId = _allClanIds[i];
  2190	            Clan storage clan = _clans[clanId];
  2191	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
  2192	                continue;

codex
# Phase Super-Swarm Review (R2) — PR #194 (head 218f902)

## SUMMARY
NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:1703`, `packages/contracts/src/ClanWorld.sol:2156`, `packages/contracts/src/ClanWorld.sol:2305`  
  Heartbeat now correctly drives `Camped -> Attacking -> resolution`, but `_pickBanditAttackTarget()` still ranks candidates from raw `_clans` storage without first settling all competing clans to `closedTick`. The new eager-settle step only settles spawn-candidate regions and is skipped entirely at the global bandit cap, so live attacks can choose the wrong target based on stale loot/liveness, then resolve that wrong attack normally. The new end-to-end heartbeat test proves the lifecycle is live, but it only uses a single clan, so this competitive-selection bug is still untested.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:565`, `packages/contracts/src/ClanWorld.sol:576`, `packages/contracts/src/ClanWorld.sol:377`  
  Dead-target cleanup only releases defenders that have already arrived and been registered in `_clansmanDefendingRegion`. A clansman still traveling on a `DefendBase` mission toward a clan that dies keeps the mission active, can arrive later, and gets registered to defend a base that no longer exists. The added cleanup test settles the defender into `ACTING` before killing the target, so this path is still uncovered.

## LOW severity findings
- `packages/shared/test/clanWorldAbi.test.ts:63`, `packages/shared/src/adapters/IChainClient.ts:165`  
  The ABI regression tests only prove `getWorldState()` and `getWorldSnapshot()` decoding. `IChainClient.getClanFullView()` now also depends on the generated ABI, but there is no decode fixture covering that deeper nested tuple.
- `packages/contracts/test/BanditAttackResolution.t.sol:483`  
  The target-death race test proves the final state no longer reverts and ends in `Escaped`, but it does not assert that `BanditAttackResolved` was not emitted after the target died. A future double-processing regression could still pass this test.
- `packages/contracts/test/Bandit.t.sol:163`  
  The camp-duration fix has a positive boundary test, but no matching “one tick too early must fail” assertion. That leaves the off-by-one boundary only partially pinned.

## Cross-cutting observations
- The five named fix-round items are otherwise in good shape:
  - Heartbeat wiring is live via `heartbeat() -> _advanceBanditStates() -> _resolveAttackingBandits()`.
  - `WorldState` and `WorldSnapshot` ordering matches between Solidity, generated ABI JSON, and the TS consumers, and both runner/shared now consume the generated ABI instead of hand-written tuples.
  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
  - Blueprint reward is now `BLUEPRINT_UNIT = 1e18`.
  - `_resolveBanditAttack()` now safely returns if settlement kills the target mid-attack.
- ABI compatibility is still a deployment risk for any stale external consumer: `BanditState` ordinals and `getBanditTroop()` tuple layout changed on the same selector. The monorepo consumers were updated, but anything outside this tree must regenerate before rollout.
- I could not execute the new tests in this environment: `forge` is not installed here, and `vitest` is also unavailable, so the verification above is from static review plus the added test intent rather than a live run.
2026-04-29T17:39:20.755791Z ERROR codex_core::session: failed to record rollout items: thread 019dda4d-baa2-7ab2-9163-766164b870c6 not found
2026-04-29T17:39:20.755805Z ERROR codex_core::session: failed to record rollout items: thread 019dda4d-ba01-7cb1-84fb-32bcb728388e not found
2026-04-29T17:39:20.755887Z ERROR codex_core::session: failed to record rollout items: thread 019dda4d-22c3-70e2-b36d-bd5df8409d2b not found
2026-04-29T17:39:20.759761Z ERROR codex_core::session: failed to record rollout items: thread 019dda4d-ba73-7541-a240-64737b6b5601 not found
tokens used
207,177
# Phase Super-Swarm Review (R2) — PR #194 (head 218f902)

## SUMMARY
NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:1703`, `packages/contracts/src/ClanWorld.sol:2156`, `packages/contracts/src/ClanWorld.sol:2305`  
  Heartbeat now correctly drives `Camped -> Attacking -> resolution`, but `_pickBanditAttackTarget()` still ranks candidates from raw `_clans` storage without first settling all competing clans to `closedTick`. The new eager-settle step only settles spawn-candidate regions and is skipped entirely at the global bandit cap, so live attacks can choose the wrong target based on stale loot/liveness, then resolve that wrong attack normally. The new end-to-end heartbeat test proves the lifecycle is live, but it only uses a single clan, so this competitive-selection bug is still untested.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:565`, `packages/contracts/src/ClanWorld.sol:576`, `packages/contracts/src/ClanWorld.sol:377`  
  Dead-target cleanup only releases defenders that have already arrived and been registered in `_clansmanDefendingRegion`. A clansman still traveling on a `DefendBase` mission toward a clan that dies keeps the mission active, can arrive later, and gets registered to defend a base that no longer exists. The added cleanup test settles the defender into `ACTING` before killing the target, so this path is still uncovered.

## LOW severity findings
- `packages/shared/test/clanWorldAbi.test.ts:63`, `packages/shared/src/adapters/IChainClient.ts:165`  
  The ABI regression tests only prove `getWorldState()` and `getWorldSnapshot()` decoding. `IChainClient.getClanFullView()` now also depends on the generated ABI, but there is no decode fixture covering that deeper nested tuple.
- `packages/contracts/test/BanditAttackResolution.t.sol:483`  
  The target-death race test proves the final state no longer reverts and ends in `Escaped`, but it does not assert that `BanditAttackResolved` was not emitted after the target died. A future double-processing regression could still pass this test.
- `packages/contracts/test/Bandit.t.sol:163`  
  The camp-duration fix has a positive boundary test, but no matching “one tick too early must fail” assertion. That leaves the off-by-one boundary only partially pinned.

## Cross-cutting observations
- The five named fix-round items are otherwise in good shape:
  - Heartbeat wiring is live via `heartbeat() -> _advanceBanditStates() -> _resolveAttackingBandits()`.
  - `WorldState` and `WorldSnapshot` ordering matches between Solidity, generated ABI JSON, and the TS consumers, and both runner/shared now consume the generated ABI instead of hand-written tuples.
  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
  - Blueprint reward is now `BLUEPRINT_UNIT = 1e18`.
  - `_resolveBanditAttack()` now safely returns if settlement kills the target mid-attack.
- ABI compatibility is still a deployment risk for any stale external consumer: `BanditState` ordinals and `getBanditTroop()` tuple layout changed on the same selector. The monorepo consumers were updated, but anything outside this tree must regenerate before rollout.
- I could not execute the new tests in this environment: `forge` is not installed here, and `vitest` is also unavailable, so the verification above is from static review plus the added test intent rather than a live run.
