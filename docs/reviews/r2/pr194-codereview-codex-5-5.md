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
session id: 019dda2f-645f-7fe0-81c2-f6f8c6a3cdcb
--------
user
Read the prompt and diff from stdin. Use parallel tool calls and subagents to thoroughly review. Output the full review to stdout in the requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for a multi-issue phase release PR.

PR: Phase 9 — Bandit System
Head SHA: 23f7f1a

## Phase context
Phase 9 release branch. Bundles all phase-9 sub-issues (bandit threat).

Sub-issues landed so far:
- #186 Phase 9.1 — Bandit troop state machine
- #187 Phase 9.2 — Spawn chance logic (PR #191)

Pending sub-issues:
- 9.3 — Eager-settle scope (settle bases + defenders before target selection)
- 9.4 — Deterministic attack resolution
- 9.5 — Defender reward split
- 9.6 — Blueprint reward
- 9.7 — Cleanup on target death

ABI note:
- Phase 9 intentionally redesigns the v1-prep bandit ABI: `BanditState` enum order/insertion and `BanditTroop` layout changed. ABI consumers must regenerate from the updated contract ABI.

Stays OPEN until Liam reviews + greens.

## Your task
This is a COHESIVE PHASE — multiple sub-issues already merged and reviewed individually; you are reviewing the integrated whole. Look for:

1. CROSS-CUTTING bugs that only surface when sub-issues integrate (race conditions between newly-added paths, state-machine inconsistencies, broken invariants across components)
2. ARCHITECTURAL drift — does the phase actually deliver its stated goal? Any sub-issue that diverged from plan?
3. SECURITY surface — auth, input validation, prompt injection, TOCTOU, resource leaks, secrets handling
4. DATA-flow correctness — schema consistency, migration safety, idempotency, error paths
5. Integration risks — newly-added code's effect on existing code paths, regression surface
6. Missing test coverage on the integration seams
7. Operational risk — deploy ordering, rollback safety, runtime config gaps

USE PARALLEL TOOL CALLS / SUB-AGENTS aggressively. You have full repo read access. Read all changed files. Look up callers of new functions. Trace state machines end-to-end.

## Output format

# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
2-4 sentences: overall verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.

## HIGH severity findings
(real bugs, security issues, data corruption, broken invariants. Each with file:line and one-paragraph explanation + suggested fix.)

## MEDIUM severity findings
(should-fix; design quality, missing edge cases, operational issues)

## LOW severity findings
(defer-OK; nits, style, minor cleanups, follow-up issues to file)

## Cross-cutting observations
(things that don't fit the per-finding format — patterns across the diff, architectural drift, suggested refactors)

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS BELOW.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..74fd4c5 100644
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
       "outputs": [
         {
           "name": "",
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
@@ -2747,6 +2895,31 @@
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
@@ -2897,6 +3070,31 @@
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
@@ -3008,6 +3206,85 @@
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
@@ -3568,6 +3845,31 @@
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
index 945490b..5d50af5 100644
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
@@ -75,8 +81,46 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // =========================================================================
 
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
+    uint256 private constant RESOURCE_UNIT = 1e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
+    uint256 internal constant DOMAIN_BANDIT_SPAWN = uint256(keccak256("clanworld.bandit.spawn.v1"));
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
@@ -91,13 +135,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -371,6 +415,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         for (uint64 tick = fromTick; tick < curTick; tick++) {
             // 1. Apply upkeep for this tick
             _applyUpkeep(clan, tick);
+            if (clan.clanState == ClanState.DEAD) break;
 
             // 2. Wheat plot regrow check (lazy, per tick)
             for (uint256 pi = 0; pi < 2; pi++) {
@@ -422,6 +467,130 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             clan.starvationStartsAtTick = 0;
             emit ClanStarvationChanged(clan.clanId, false, tick);
         }
+
+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
+            _killNextClansmanFromStarvation(clan, tick);
+        }
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
@@ -689,175 +858,1357 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
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
+                    bandit.state == BanditState.Resting
+                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
+                ) {
+                    _transitionBanditState(banditId, BanditState.Camped);
+                }
+            }
+        }
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
+        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
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
+            targetClan.blueprintBalance += 1;
+            emit BlueprintEarned(targetClanId, banditId, closedTick);
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
@@ -867,26 +2218,20 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -894,15 +2239,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1002,7 +2359,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     /// @notice Mint a new clan and spawn its homebase.
     function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
         require(to != address(0), "ClanWorld: zero address");
-        require(_allClanIds.length < 12, "ClanWorld: max clans");
+        require(_allClanIds.length < MAX_CLANS, "ClanWorld: max clans");
         clanId = _nextClanId++;
         iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
 
@@ -1182,7 +2539,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         ctx.targetClanId =
             order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
 
-        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
+        // NOOP bypass: treat 0 as "stay here"; DefendBase requires the defended base region.
         ctx.isNoop = order.action != ActionType.DefendBase
             && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
         if (ctx.isNoop) {
@@ -1686,8 +3043,12 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
 
@@ -1816,21 +3177,32 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1888,35 +3260,44 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1942,8 +3323,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     }
 
     function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
-        // Phase 1: return raw value (no simulation)
-        return _lootValueRaw(_clans[clanId]);
+        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
+        return _lootValueRaw(sim.clan);
     }
 
     /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
@@ -1951,6 +3332,32 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1992,49 +3399,50 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -2062,23 +3470,36 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
index 2b80fbe..697ff94 100644
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
+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
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
index 0000000..ce0a6c3
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
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS);
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
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS);
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
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS);
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
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS);
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
+        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS);
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
index 0000000..d636b18
--- /dev/null
+++ b/packages/contracts/test/BanditAttackResolution.t.sol
@@ -0,0 +1,576 @@
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
+    function setClanUpkeepState(uint32 clanId, uint64 lastSettledTick, uint256 vaultWheat, uint256 vaultFish)
+        external
+    {
+        Clan storage clan = _clans[clanId];
+        clan.lastSettledTick = lastSettledTick;
+        clan.vaultWheat = vaultWheat;
+        clan.vaultFish = vaultFish;
+        clan.starvationStartsAtTick = 0;
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
+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
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
+                logs[i].topics.length == 3 && logs[i].topics[0] == eventSig
+                    && logs[i].topics[1] == expectedBanditTopic && logs[i].topics[2] == expectedClanTopic
+            ) {
+                uint64 tick = abi.decode(logs[i].data, (uint64));
+                if (tick == expectedTick) return;
+            }
+        }
+        fail("expected BanditTargetDied log");
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
+        emit BlueprintEarned(clanId, banditId, world.getWorldState().currentTick);
+
+        _advanceTick();
+
+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 1, "target blueprint awarded");
+    }
+
+    function test_deadTargetCleanupReleasesDefendersAndEscapesBandit() public {
+        uint32[] memory clanIds = _mintClans(3);
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
+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
+        _advanceUntil(winterStart + 4);
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
+        assertEq(world.getClan(clanIds[0]).blueprintBalance, blueprintBefore[0] + 1, "base owner blueprint");
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
+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 2, "one blueprint per defeated bandit");
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
+        uint32 nonStarvingRoll =
+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
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
+    function _setupTwoAttackWorld(BanditAttackHarness target) internal returns (uint32 firstClanId, uint32 secondClanId) {
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
index 92781d3..66ca511 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -125,6 +125,12 @@ contract ClanWorldTest is Test {
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
@@ -282,6 +288,87 @@ contract ClanWorldTest is Test {
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
@@ -496,8 +583,11 @@ contract ClanWorldTest is Test {
         ClanFullView memory view_ = world.getClanFullView(clanId);
         uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
 
-        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
-        for (uint256 i = 0; i < 201; i++) {
+        // Advance until the clan is truly >200 ticks behind. Phase 9 bandit
+        // eager-settle may settle candidate-region bases on spawn ticks, so
+        // this test follows the invariant instead of assuming no heartbeat
+        // settlement ever touches the clan.
+        while (world.getWorldState().currentTick <= world.getClan(clanId).lastSettledTick + 200) {
             _advanceTick();
         }
 
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
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
I’ll use the GitHub review workflow shape here, plus parallel repo reads and subagents, since this is explicitly a final integrated review. I’m going to trace heartbeat, bandit state transitions, spawn targeting, attack resolution, and ABI/test seams against the actual files so the findings land with real line references.
exec
/bin/bash -lc "pwd && rg -n \"function heartbeat|function _evaluateBanditSpawns|function _resolveBanditAttack|function _transitionBanditState|function _eagerSettleForBandits|function _simulateSettleToTick|function _submit|enum BanditState|struct BanditTroop|function getBandit|event BanditAttackResolved|BanditState\\.NONE|BanditState\\.None|_tickSeeds\\[|currentTickSeed\" packages/contracts/src packages/contracts/test apps packages -g '"'!packages/contracts/abi/IClanWorld.json'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
/home/claude/code/omnipass-world/clan-world
packages/contracts/src/ClanWorld.sol:260:            bytes32 tickSeed = _tickSeeds[tick];
packages/contracts/src/ClanWorld.sol:724:    function heartbeat() external override {
packages/contracts/src/ClanWorld.sol:732:        _tickSeeds[_world.currentTick] = newSeed;                               // store under new tick
packages/contracts/src/ClanWorld.sol:733:        _world.currentTickSeed = newSeed;
packages/contracts/src/ClanWorld.sol:741:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
packages/contracts/src/ClanWorld.sol:1023:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
packages/contracts/src/ClanWorld.sol:1557:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
packages/contracts/src/ClanWorld.sol:1627:            currentTickSeed: _world.currentTickSeed,
packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
packages/contracts/src/IClanWorld.sol:106:enum BanditState {
packages/contracts/src/IClanWorld.sol:195:    bytes32 currentTickSeed;
packages/contracts/src/IClanWorld.sol:295:struct BanditTroop {
packages/contracts/src/IClanWorld.sol:411:    bytes32 currentTickSeed;
packages/contracts/src/IClanWorld.sol:604:    event BanditAttackResolved(
packages/contracts/src/IClanWorld.sol:667:    function heartbeat() external;
packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
packages/contracts/src/ClanWorldStub.sol:70:    function heartbeat() external override {
packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
packages/contracts/src/ClanWorldStub.sol:248:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
packages/contracts/src/ClanWorldStub.sol:278:            currentTickSeed: bytes32(0),
packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
packages/contracts/test/ClanWorld.t.sol:130:    function _submitOrder(uint32 clanId, uint32 csId, uint8 gotoRegion, ActionType action)
packages/contracts/test/ClanWorld.t.sol:165:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
packages/contracts/test/ClanWorld.t.sol:171:        bytes32 seedAfter = world.getWorldState().currentTickSeed;
packages/contracts/test/ClanWorld.t.sol:503:    function _submitMarketOrder(
packages/shared/src/adapters/IChainClient.ts:43:          { name: 'currentTickSeed', type: 'bytes32' },
packages/contracts/src/ClanWorld.sol:260:            bytes32 tickSeed = _tickSeeds[tick];
packages/contracts/src/ClanWorld.sol:724:    function heartbeat() external override {
packages/contracts/src/ClanWorld.sol:732:        _tickSeeds[_world.currentTick] = newSeed;                               // store under new tick
packages/contracts/src/ClanWorld.sol:733:        _world.currentTickSeed = newSeed;
packages/contracts/src/ClanWorld.sol:741:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
packages/contracts/src/ClanWorld.sol:1023:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
packages/contracts/src/ClanWorld.sol:1557:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
packages/contracts/src/ClanWorld.sol:1627:            currentTickSeed: _world.currentTickSeed,
packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
packages/contracts/src/IClanWorld.sol:106:enum BanditState {
packages/contracts/src/IClanWorld.sol:195:    bytes32 currentTickSeed;
packages/contracts/src/IClanWorld.sol:295:struct BanditTroop {
packages/contracts/src/IClanWorld.sol:411:    bytes32 currentTickSeed;
packages/contracts/src/IClanWorld.sol:604:    event BanditAttackResolved(
packages/contracts/src/IClanWorld.sol:667:    function heartbeat() external;
packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
packages/contracts/src/ClanWorldStub.sol:70:    function heartbeat() external override {
packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
packages/contracts/src/ClanWorldStub.sol:248:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
packages/contracts/src/ClanWorldStub.sol:278:            currentTickSeed: bytes32(0),
packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
packages/contracts/test/ClanWorld.t.sol:130:    function _submitOrder(uint32 clanId, uint32 csId, uint8 gotoRegion, ActionType action)
packages/contracts/test/ClanWorld.t.sol:165:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
packages/contracts/test/ClanWorld.t.sol:171:        bytes32 seedAfter = world.getWorldState().currentTickSeed;
packages/contracts/test/ClanWorld.t.sol:503:    function _submitMarketOrder(

2026-04-29T17:00:51.178894Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
codex
The local checkout appears to be a sibling/symlinked worktree and may not currently be at `23f7f1a`, so I’m treating the supplied diff as authoritative while using the repo for unchanged context and caller lookup. I’ve got three reviewers running in parallel on seed/state-machine, data-flow/ABI, and combat/cleanup/reward seams.
exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {
     5	    IClanWorld,
     6	    IClanWorldEvents,
     7	    ClanWorldConstants,
     8	    ClanState,
     9	    ClansmanState,
    10	    BanditState,
    11	    WheatPlotState,
    12	    ResourceType,
    13	    ActionType,
    14	    MarketExecutionMode,
    15	    StatusCode,
    16	    WorldState,
    17	    TreasuryState,
    18	    Clan,
    19	    WheatPlot,
    20	    Clansman,
    21	    Mission,
    22	    BanditTroop,
    23	    ScheduledMarketAction,
    24	    DefenseContribution,
    25	    PackedRoute,
    26	    DerivedClanState,
    27	    DerivedClansmanState,
    28	    ClanOrder,
    29	    OrderResult,
    30	    PoolSeedConfig,
    31	    LeaderboardEntry,
    32	    WorldSnapshot,
    33	    ClansmanFullView,
    34	    ClanFullView,
    35	    PoolReserves,
    36	    MarketState,
    37	    ActiveBanditView,
    38	    RegionOccupant
    39	} from "./IClanWorld.sol";
    40	import {StubPool} from "./StubPool.sol";
    41	
    42	/// @title ClanWorld
    43	/// @notice Phase 1+2 real engine implementation of IClanWorld v4.
    44	///         Implements: world clock, clan lifecycle, lazy settlement, resource gathering,
    45	///         deposit, wheat harvest, travel, NOOP bypass, order validation.
    46	///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
    47	contract ClanWorld is IClanWorld {
    48	
    49	    // =========================================================================
    50	    // STORAGE
    51	    // =========================================================================
    52	
    53	    WorldState private _world;
    54	    TreasuryState private _treasury;
    55	
    56	    mapping(uint32 => Clan) private _clans;
    57	    mapping(uint32 => Clansman) private _clansmen;
    58	    mapping(uint32 => Mission) private _missions;              // keyed by clansmanId
    59	    mapping(uint32 => WheatPlot[2]) private _wheatPlots;       // [0]=west [1]=east
    60	    mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
    61	    mapping(uint32 => uint32[]) private _incomingDefenders;    // targetClanId => clansmanIds
    62	    mapping(uint32 => uint32) private _clanDefendingBase;      // clansmanId => targetClanId
    63	    mapping(uint64 => bytes32) private _tickSeeds;              // tick => seed
    64	
    65	    uint32 private _nextClanId;
    66	    uint32 private _nextClansmanId;
    67	    uint32[] private _allClanIds;
    68	
    69	    // per-clan clansman list: clanId => clansmanId[]
    70	    mapping(uint32 => uint32[]) private _clanClansmanIds;
    71	
    72	    // =========================================================================
    73	    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    74	    // =========================================================================
    75	
    76	    uint256 private constant WHEAT_HARVEST_RATE = 20e18;
    77	
    78	    // =========================================================================
    79	    // CONSTRUCTOR
    80	    // =========================================================================
    81	
    82	    constructor() {
    83	        _world.currentTick = 0;
    84	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
    85	        _world.seasonStartTick = 0;
    86	        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
    87	        _treasury.treasuryOwner = msg.sender;
    88	        _nextClanId = 1;
    89	        _nextClansmanId = 1;
    90	    }
    91	
    92	    // =========================================================================
    93	    // TRAVEL DISTANCE MATRIX
    94	    // =========================================================================
    95	
    96	    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
    97	    // Adjacency:
    98	    //   Forest(1):       Mountains(2), WestFarms(4)
    99	    //   Mountains(2):    Forest(1), UnicornTown(3)
   100	    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
   101	    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
   102	    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
   103	    //   WestDocks(6):    WestFarms(4), DeepSea(8)
   104	    //   EastDocks(7):    EastFarms(5), DeepSea(8)
   105	    //   DeepSea(8):      WestDocks(6), EastDocks(7)
   106	    //
   107	    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
   108	    // Distance of 0 = same region.
   109	    //
   110	    // Full BFS-computed 8x8 matrix:
   111	    //   src\dst  1  2  3  4  5  6  7  8
   112	    //      1     0  1  2  1  3  2  4  3
   113	    //      2     1  0  1  2  2  3  3  4
   114	    //      3     2  1  0  1  1  2  2  3
   115	    //      4     1  2  1  0  2  1  3  2
   116	    //      5     3  2  1  2  0  3  1  2
   117	    //      6     2  3  2  1  3  0  2  1
   118	    //      7     4  3  2  3  1  2  0  1
   119	    //      8     3  4  3  2  2  1  1  0
   120	
   121	    function _distMatrix(uint8 src, uint8 dst) private pure returns (uint8) {
   122	        if (src == dst) return 0;
   123	        // Encode as (src-1)*8 + (dst-1), values from BFS
   124	        uint8[64] memory d = [
   125	            // src=1: to 1,2,3,4,5,6,7,8
   126	            0, 1, 2, 1, 3, 2, 4, 3,
   127	            // src=2: to 1,2,3,4,5,6,7,8
   128	            1, 0, 1, 2, 2, 3, 3, 4,
   129	            // src=3: to 1,2,3,4,5,6,7,8
   130	            2, 1, 0, 1, 1, 2, 2, 3,
   131	            // src=4: to 1,2,3,4,5,6,7,8
   132	            1, 2, 1, 0, 2, 1, 3, 2,
   133	            // src=5: to 1,2,3,4,5,6,7,8
   134	            3, 2, 1, 2, 0, 3, 1, 2,
   135	            // src=6: to 1,2,3,4,5,6,7,8
   136	            2, 3, 2, 1, 3, 0, 2, 1,
   137	            // src=7: to 1,2,3,4,5,6,7,8
   138	            4, 3, 2, 3, 1, 2, 0, 1,
   139	            // src=8: to 1,2,3,4,5,6,7,8
   140	            3, 4, 3, 2, 2, 1, 1, 0
   141	        ];
   142	        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
   143	    }
   144	
   145	    // Build a canonical path from src to dst (BFS on the adjacency graph).
   146	    // Returns packed bytes8: region IDs in order, zero-padded.
   147	    function _buildPath(uint8 src, uint8 dst) private pure returns (bytes8) {
   148	        if (src == dst) {
   149	            return bytes8(uint64(src) << 56);
   150	        }
   151	        // Adjacency list (1-indexed, 0 = end sentinel)
   152	        // adj[r] = neighbors of region r (up to 3)
   153	        uint8[3][9] memory adj = [
   154	            [0, 0, 0],       // 0: unused
   155	            [2, 4, 0],       // 1: Forest
   156	            [1, 3, 0],       // 2: Mountains
   157	            [2, 4, 5],       // 3: UnicornTown
   158	            [1, 3, 6],       // 4: WestFarms
   159	            [3, 7, 0],       // 5: EastFarms
   160	            [4, 8, 0],       // 6: WestDocks
   161	            [5, 8, 0],       // 7: EastDocks
   162	            [6, 7, 0]        // 8: DeepSea
   163	        ];
   164	
   165	        // BFS with parent tracking (max 8 nodes)
   166	        uint8[9] memory parent;
   167	        bool[9] memory visited;
   168	        uint8[9] memory queue;
   169	        uint256 head;
   170	        uint256 tail;
   171	
   172	        for (uint256 i = 0; i < 9; i++) {
   173	            parent[i] = 0;
   174	            visited[i] = false;
   175	        }
   176	
   177	        visited[src] = true;
   178	        queue[tail++] = src;
   179	
   180	        while (head < tail) {
   181	            uint8 curr = queue[head++];
   182	            if (curr == dst) break;
   183	            for (uint256 ni = 0; ni < 3; ni++) {
   184	                uint8 nb = adj[curr][ni];
   185	                if (nb == 0) break;
   186	                if (!visited[nb]) {
   187	                    visited[nb] = true;
   188	                    parent[nb] = curr;
   189	                    queue[tail++] = nb;
   190	                }
   191	            }
   192	        }
   193	
   194	        // Reconstruct path backwards
   195	        uint8[8] memory path;
   196	        uint256 pathLen;
   197	        uint8 cur = dst;
   198	        while (cur != src) {
   199	            path[pathLen++] = cur;
   200	            cur = parent[cur];
   201	        }
   202	        path[pathLen++] = src;
   203	
   204	        // Reverse into result
   205	        bytes8 packed;
   206	        uint64 byteShift = 56;
   207	        for (uint256 i = pathLen; i > 0; i--) {
   208	            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
   209	            if (byteShift >= 8) byteShift -= 8;
   210	        }
   211	        return packed;
   212	    }
   213	
   214	    function _travelTicks(uint8 fromRegion, uint8 toRegion) private pure returns (uint8) {
   215	        if (fromRegion == 0 || toRegion == 0) return 0; // NOOP region
   216	        if (fromRegion == toRegion) return 0;
   217	        if (fromRegion > 8 || toRegion > 8) return 0;
   218	        return _distMatrix(fromRegion, toRegion);
   219	    }
   220	

exec
/bin/bash -lc 'git status --short --branch && git rev-parse --show-toplevel && git rev-parse HEAD && git branch --show-current' in /home/claude/code/clan-world
 succeeded in 0ms:
## agent/bubble-elder-font...origin/dev [behind 17]
?? docs/planning/gold-bridge-monorepo.zip
?? docs/reviews/pr153-review-claude-opus.md
?? docs/reviews/pr153-review-codex-5-3.md
?? docs/reviews/pr194-codereview-codex-5-4.md
?? docs/reviews/pr194-codereview-codex-5-5.md
?? docs/reviews/pr194-codereview-gemini-3-1-pro.md
?? docs/reviews/pr194-codereview-opus-4-6.md
?? docs/reviews/pr194-codereview-opus-4-7.md
/home/claude/code/omnipass-world/clan-world
31a857064890cdbac0eef28d650bdb234d7b0576
agent/bubble-elder-font

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '220,460p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   220	
   221	    // =========================================================================
   222	    // INTERNAL SETTLEMENT
   223	    // =========================================================================
   224	
   225	    /// @dev Lazy settlement of a clan forward to currentTick.
   226	    ///      Mutates storage. Called before order submission and by public settleClan().
   227	    function _settleClan(uint32 clanId) internal {
   228	        Clan storage clan = _clans[clanId];
   229	        if (clan.clanId == 0) return;
   230	
   231	        uint64 curTick = _world.currentTick;
   232	        uint64 fromTick = clan.lastSettledTick;
   233	        if (fromTick >= curTick) return;
   234	
   235	        // Cap ticks settled per call to prevent block gas limit issues
   236	        uint64 maxSettleTicks = 200;
   237	        if (curTick > fromTick + maxSettleTicks) {
   238	            curTick = fromTick + maxSettleTicks;
   239	        }
   240	
   241	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   242	
   243	        // Settle tick by tick from fromTick to curTick - 1
   244	        // (curTick is still open; we settle through the last closed tick)
   245	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   246	            // 1. Apply upkeep for this tick
   247	            _applyUpkeep(clan, tick);
   248	
   249	            // 2. Wheat plot regrow check (lazy, per tick)
   250	            for (uint256 pi = 0; pi < 2; pi++) {
   251	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   252	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   253	                    plot.state = WheatPlotState.Harvestable;
   254	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   255	                    plot.regrowUntilTick = 0;
   256	                }
   257	            }
   258	
   259	            // 3. Advance each clansman
   260	            bytes32 tickSeed = _tickSeeds[tick];
   261	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   262	                uint32 csId = clansmanIds[i];
   263	                Clansman storage cs = _clansmen[csId];
   264	                if (cs.state == ClansmanState.DEAD) continue;
   265	
   266	                Mission storage m = _missions[csId];
   267	                if (!m.active) continue;
   268	
   269	                // Travel advancement: if still traveling and this tick >= arrivalTick, arrive
   270	                if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   271	                    cs.state = ClansmanState.ACTING;
   272	                    cs.currentRegion = m.targetRegion;
   273	                    emit WorkerArrived(clanId, csId, m.targetRegion, tick);
   274	                }
   275	
   276	                // Action resolution: if acting and this tick >= actionStartTick
   277	                if (cs.state == ClansmanState.ACTING && tick >= m.actionStartTick) {
   278	                    _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   279	                }
   280	            }
   281	        }
   282	
   283	        clan.lastSettledTick = curTick;
   284	        emit ClanSettled(clanId, curTick);
   285	    }
   286	
   287	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
   288	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   289	        if (clan.livingClansmen == 0) return;
   290	
   291	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   292	        uint256 fishNeeded  = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   293	
   294	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   295	        bool hadEnoughFish  = clan.vaultFish  >= fishNeeded;
   296	
   297	        if (hadEnoughWheat) {
   298	            clan.vaultWheat -= wheatNeeded;
   299	        } else {
   300	            clan.vaultWheat = 0;
   301	        }
   302	        if (hadEnoughFish) {
   303	            clan.vaultFish -= fishNeeded;
   304	        } else {
   305	            clan.vaultFish = 0;
   306	        }
   307	
   308	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   309	        if (starving && clan.starvationStartsAtTick == 0) {
   310	            clan.starvationStartsAtTick = tick;
   311	            emit ClanStarvationChanged(clan.clanId, true, tick);
   312	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   313	            clan.starvationStartsAtTick = 0;
   314	            emit ClanStarvationChanged(clan.clanId, false, tick);
   315	        }
   316	    }
   317	
   318	    /// @dev Check if a clan is currently starving (lazy read).
   319	    function _isStarving(Clan storage clan) internal view returns (bool) {
   320	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   321	    }
   322	
   323	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   324	    function _resolveAction(
   325	        Clan storage clan,
   326	        Clansman storage cs,
   327	        Mission storage m,
   328	        uint32 clanId,
   329	        uint64 tick,
   330	        bytes32 tickSeed
   331	    ) internal {
   332	        bool starving = _isStarving(clan);
   333	        ActionType action = m.action;
   334	
   335	        if (action == ActionType.ChopWood) {
   336	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   337	        } else if (action == ActionType.MineIron) {
   338	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   339	        } else if (action == ActionType.FishDocks) {
   340	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   341	        } else if (action == ActionType.FishDeepSea) {
   342	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   343	        } else if (action == ActionType.HarvestWheat) {
   344	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   345	        } else if (action == ActionType.DepositResources) {
   346	            _doDeposit(clan, cs, m, clanId, tick);
   347	        } else if (action == ActionType.Wait) {
   348	            // NOOP — worker stays ACTING (continuous), no transition needed
   349	            // Wait mission is effectively persistent until interrupted
   350	        } else if (action == ActionType.DefendBase) {
   351	            // Register defender at arrival (not at submission) per v4.3 — ensures only arrived clansmen counted.
   352	            // Guard: only register once (this branch executes every ACTING tick; idempotent via _clanDefendingBase check).
   353	            if (_clanDefendingBase[cs.clansmanId] != m.targetClanId) {
   354	                _incomingDefenders[m.targetClanId].push(cs.clansmanId);
   355	                _clanDefendingBase[cs.clansmanId] = m.targetClanId;
   356	            }
   357	        } else if (
   358	            action == ActionType.BuildWall ||
   359	            action == ActionType.UpgradeBase ||
   360	            action == ActionType.UpgradeMonument
   361	        ) {
   362	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   363	            _doBuilding(clan, cs, m, clanId, tick, action);
   364	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   365	            // Scheduled market actions: already enqueued at submitClanOrders time.
   366	            // Settlement resolves this action slot — just complete the mission.
   367	            // (Actual execution happened or will happen at heartbeat.)
   368	            _completeMission(cs, m);
   369	        }
   370	    }
   371	
   372	    // -------------------------------------------------------------------------
   373	    // Gathering helpers
   374	    // -------------------------------------------------------------------------
   375	
   376	    function _gatherWood(
   377	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   378	        Clansman storage cs,
   379	        Mission storage m,
   380	        uint32 clanId,
   381	        uint64 tick,
   382	        bool starving,
   383	        bytes32 tickSeed
   384	    ) internal {
   385	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
   386	        if (remaining == 0) {
   387	            _completeMission(cs, m);
   388	            return;
   389	        }
   390	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
   391	        // Crit roll: domain-separated RNG
   392	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   393	        uint256 critRoll = uint256(critRng) % 10000;
   394	        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
   395	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
   396	        }
   397	        if (starving) yield = yield / 2;
   398	        if (yield > remaining) yield = remaining;
   399	        cs.carryWood += yield;
   400	
   401	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   402	
   403	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
   404	            _completeMission(cs, m);
   405	        }
   406	        // else continuous — worker stays ACTING
   407	    }
   408	
   409	    function _gatherIron(
   410	        Clan storage clan,
   411	        Clansman storage cs,
   412	        Mission storage m,
   413	        uint32 clanId,
   414	        uint64 tick,
   415	        bool starving,
   416	        bytes32 tickSeed
   417	    ) internal {
   418	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   419	        if (remaining == 0) {
   420	            _completeMission(cs, m);
   421	            return;
   422	        }
   423	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   424	        if (starving) yield = yield / 2;
   425	        if (yield > remaining) yield = remaining;
   426	        cs.carryIron += yield;
   427	
   428	        // Gold bonus roll — scoped to reduce stack depth
   429	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   430	
   431	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   432	
   433	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   434	            _completeMission(cs, m);
   435	        }
   436	    }
   437	
   438	    function _rollIronGoldBonus(
   439	        Clan storage clan,
   440	        uint32 clansmanId,
   441	        uint64 nonce,
   442	        uint64 tick,
   443	        bytes32 tickSeed
   444	    ) internal returns (uint256 goldBonus) {
   445	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   446	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   447	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   448	            clan.goldBalance += goldBonus;
   449	        }
   450	    }
   451	
   452	    function _gatherFishDocks(
   453	        Clan storage,
   454	        Clansman storage cs,
   455	        Mission storage m,
   456	        uint32 clanId,
   457	        uint64 tick,
   458	        bool starving,
   459	        bytes32 tickSeed
   460	    ) internal {

exec
/bin/bash -lc "nl -ba packages/contracts/src/IClanWorld.sol | sed -n '90,330p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   106	enum BanditState {
   107	    NONE,
   108	    CAMPING,
   109	    RESTING,
   110	    ATTACKING,
   111	    DEFEATED,
   112	    ESCAPED
   113	}
   114	
   115	enum WheatPlotState {
   116	    Harvestable,
   117	    Regrowing,
   118	    WinterLocked
   119	}
   120	
   121	enum ResourceType {
   122	    Wood,
   123	    Iron,
   124	    Wheat,
   125	    Fish
   126	}
   127	
   128	enum ActionType {
   129	    None,
   130	    ChopWood,
   131	    MineIron,
   132	    FishDocks,
   133	    FishDeepSea,
   134	    HarvestWheat,
   135	    DepositResources,
   136	    BuildWall,
   137	    UpgradeBase,
   138	    UpgradeMonument,
   139	    DefendBase,
   140	    MarketBuy,
   141	    MarketSell,
   142	    Wait
   143	}
   144	
   145	enum MarketExecutionMode {
   146	    None,
   147	    Immediate,
   148	    Scheduled
   149	}
   150	
   151	enum StatusCode {
   152	    OK,
   153	    ERR_CLAN_DEAD,
   154	    ERR_CLAN_NOT_OWNED,
   155	    ERR_CLANSMAN_DEAD,
   156	    ERR_INVALID_CLANSMAN,
   157	    ERR_INVALID_REGION,
   158	    ERR_INVALID_ACTION,
   159	    ERR_INVALID_TARGET,
   160	    ERR_COOLDOWN_ACTIVE,
   161	    ERR_NOT_WAITING,
   162	    ERR_NOT_IN_UNICORN_TOWN,
   163	    ERR_NOT_AT_HOMEBASE,
   164	    ERR_NOT_AT_TARGET_BASE,
   165	    ERR_NOT_DEFENDABLE,
   166	    ERR_MISSING_RESOURCES,
   167	    ERR_EMPTY_CARGO,
   168	    ERR_PLOT_NOT_READY,
   169	    ERR_PLOT_EMPTY,
   170	    ERR_MARKET_ZERO_AMOUNT,
   171	    ERR_MARKET_UNSUPPORTED_TOKEN,
   172	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   173	    ERR_MARKET_BUY_OVER_CAPACITY,
   174	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   175	    ERR_WORLD_TICK_MISMATCH,
   176	    ERR_NO_ACTIVE_BANDIT,
   177	    ERR_SEASON_ENDED,
   178	    ERR_NOT_ENOUGH_GOLD,
   179	    ERR_CARRY_FULL
   180	}
   181	
   182	// =============================================================================
   183	// CORE STATE STRUCTS (raw storage shape)
   184	// =============================================================================
   185	
   186	struct WorldState {
   187	    uint64 currentTick;
   188	    uint64 seasonStartTick;
   189	    uint64 seasonEndTick;
   190	    bool   seasonFinalized;
   191	
   192	    uint64 nextHeartbeatAtTs;
   193	    uint64 nextBanditSpawnEligibleTick;
   194	    uint16 currentBanditSpawnChanceBps;
   195	    bytes32 currentTickSeed;
   196	
   197	    uint32 activeBanditId;       // 0 if none
   198	    bool   winterActive;
   199	    uint64 winterStartsAtTick;
   200	    uint64 winterEndsAtTick;     // 0 if not active
   201	
   202	    uint64 nextCommitSequence;   // global FIFO sequence for scheduled market actions
   203	}
   204	
   205	struct TreasuryState {
   206	    address treasuryOwner;
   207	    uint256 prizePotGold;
   208	
   209	    bool poolsSeeded;
   210	
   211	    address woodToken;
   212	    address wheatToken;
   213	    address fishToken;
   214	    address ironToken;
   215	    address goldToken;
   216	    address blueprintToken;
   217	
   218	    address woodGoldPool;
   219	    address wheatGoldPool;
   220	    address fishGoldPool;
   221	    address ironGoldPool;
   222	}
   223	
   224	struct Clan {
   225	    uint32 clanId;
   226	    uint256 iftTokenId;
   227	    address owner;
   228	    ClanState clanState;
   229	
   230	    uint8 baseRegion;
   231	    uint8 baseLevel;
   232	    uint8 wallLevel;
   233	    uint8 monumentLevel;
   234	    uint8 livingClansmen;
   235	
   236	    uint64 lastSettledTick;
   237	    uint64 starvationStartsAtTick; // 0 = none
   238	
   239	    uint16 coldDamage;             // resets to 0 at winter end
   240	
   241	    uint256 goldBalance;
   242	    uint256 blueprintBalance;
   243	
   244	    uint256 vaultWood;
   245	    uint256 vaultIron;
   246	    uint256 vaultWheat;
   247	    uint256 vaultFish;
   248	}
   249	
   250	struct WheatPlot {
   251	    WheatPlotState state;
   252	    uint8 region;                  // West Farms or East Farms
   253	    uint256 remainingWheat;
   254	    uint64 regrowUntilTick;
   255	}
   256	
   257	struct Clansman {
   258	    uint32 clansmanId;
   259	    uint32 clanId;
   260	    ClansmanState state;
   261	    uint8 currentRegion;
   262	
   263	    uint64 cooldownEndsAtTs;
   264	    uint64 lastMissionNonce;
   265	
   266	    uint256 carryWood;
   267	    uint256 carryIron;
   268	    uint256 carryWheat;
   269	    uint256 carryFish;
   270	}
   271	
   272	struct Mission {
   273	    bool active;
   274	
   275	    uint64 nonce;
   276	    uint32 clansmanId;
   277	
   278	    uint8 startRegion;
   279	    uint8 targetRegion;
   280	    ActionType action;
   281	
   282	    uint64 startTick;
   283	    uint64 arrivalTick;
   284	    uint64 actionStartTick;
   285	
   286	    bytes32 missionSeed;
   287	    MarketExecutionMode marketMode;
   288	
   289	    uint32 targetClanId;   // DefendBase only
   290	    address marketToken;   // market token for buy/sell
   291	    uint256 marketAmount;  // exact-in for sell, exact-out for buy
   292	    uint256 maxGoldIn;     // market_buy only, 0 otherwise
   293	}
   294	
   295	struct BanditTroop {
   296	    uint32 banditId;
   297	    BanditState state;
   298	
   299	    uint8 currentRegion;
   300	    uint8 attackAttemptsMade;
   301	    uint64 stateEnteredTick;
   302	    uint64 nextActionTick;
   303	
   304	    uint8 tier;
   305	    uint16 attackPower;            // derived from tier; tier is canonical (v4.3 §G)
   306	
   307	    uint256 carryWood;
   308	    uint256 carryIron;
   309	    uint256 carryWheat;
   310	    uint256 carryFish;
   311	}
   312	
   313	struct ScheduledMarketAction {
   314	    uint64 executeAtTick;
   315	    uint64 commitSequence;        // global monotonic FIFO order
   316	    uint32 clanId;
   317	    uint32 clansmanId;
   318	    ActionType action;            // MarketBuy or MarketSell
   319	
   320	    address marketToken;
   321	    uint256 marketAmount;         // exact-in for sell, exact-out for buy
   322	    uint256 maxGoldIn;            // buy only, 0 otherwise
   323	}
   324	
   325	struct DefenseContribution {
   326	    uint32 clansmanId;
   327	    uint32 clanId;
   328	    uint16 defensePoints;
   329	}
   330	

exec
/bin/bash -lc 'git cat-file -t 23f7f1a && git show --stat --oneline --decorate --no-renames 23f7f1a -- packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/BanditAttackResolution.t.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
commit
23f7f1a (origin/dev-phase-9-bandits) fix(contracts): Phase 9 cloud-review fix-round — bandit state advancement + ABI doc + 5 MED (Closes #264) (#265)
 packages/contracts/src/ClanWorld.sol               | 104 +++++++++++++--------
 packages/contracts/src/IClanWorld.sol              |   6 +-
 .../contracts/test/BanditAttackResolution.t.sol    |   4 +-
 3 files changed, 72 insertions(+), 42 deletions(-)

exec
/bin/bash -lc 'rg -n "HIGH|MEDIUM|LOW|Bandit|Phase 9|finding|heartbeat|currentTickSeed|_resolveBanditAttack|eager|loot|blueprint|target" docs/reviews/pr194-codereview-* docs/reviews/pr194-review-* 2>/dev/null' in /home/claude/code/clan-world
 exited 2 in 0ms:
docs/reviews/pr194-codereview-codex-5-5.md:19:PR: Phase 9 — Bandit System
docs/reviews/pr194-codereview-codex-5-5.md:23:Phase 9 release branch. Bundles all phase-9 sub-issues (bandit threat).
docs/reviews/pr194-codereview-codex-5-5.md:26:- #186 Phase 9.1 — Bandit troop state machine
docs/reviews/pr194-codereview-codex-5-5.md:27:- #187 Phase 9.2 — Spawn chance logic (PR #191)
docs/reviews/pr194-codereview-codex-5-5.md:30:- 9.3 — Eager-settle scope (settle bases + defenders before target selection)
docs/reviews/pr194-codereview-codex-5-5.md:34:- 9.7 — Cleanup on target death
docs/reviews/pr194-codereview-codex-5-5.md:37:- Phase 9 intentionally redesigns the v1-prep bandit ABI: `BanditState` enum order/insertion and `BanditTroop` layout changed. ABI consumers must regenerate from the updated contract ABI.
docs/reviews/pr194-codereview-codex-5-5.md:61:## HIGH severity findings
docs/reviews/pr194-codereview-codex-5-5.md:64:## MEDIUM severity findings
docs/reviews/pr194-codereview-codex-5-5.md:67:## LOW severity findings
docs/reviews/pr194-codereview-codex-5-5.md:71:(things that don't fit the per-finding format — patterns across the diff, architectural drift, suggested refactors)
docs/reviews/pr194-codereview-codex-5-5.md:73:If clean, say "CLEAN — no findings" under each section.
docs/reviews/pr194-codereview-codex-5-5.md:75:DIFF FOLLOWS BELOW.
docs/reviews/pr194-codereview-codex-5-5.md:106:       "name": "getActiveBanditView",
docs/reviews/pr194-codereview-codex-5-5.md:112:+      "name": "getBandit",
docs/reviews/pr194-codereview-codex-5-5.md:185:+          "internalType": "struct BanditTroop",
docs/reviews/pr194-codereview-codex-5-5.md:200:+              "internalType": "enum BanditState"
docs/reviews/pr194-codereview-codex-5-5.md:203:+              "name": "targetClanId",
docs/reviews/pr194-codereview-codex-5-5.md:251:           "internalType": "struct BanditTroop",
docs/reviews/pr194-codereview-codex-5-5.md:262:-              "internalType": "enum BanditState"
docs/reviews/pr194-codereview-codex-5-5.md:275:+              "internalType": "enum BanditState"
docs/reviews/pr194-codereview-codex-5-5.md:281:+              "name": "targetClanId",
docs/reviews/pr194-codereview-codex-5-5.md:323:+      "name": "getBanditsInRegion",
docs/reviews/pr194-codereview-codex-5-5.md:475:+      "name": "BanditTargetDied",
docs/reviews/pr194-codereview-codex-5-5.md:539:+      "name": "ClansmanKilledByBandit",
docs/reviews/pr194-codereview-codex-5-5.md:657:+      "name": "WallDamagedByBandit",
docs/reviews/pr194-codereview-codex-5-5.md:688:     ActiveBanditView,
docs/reviews/pr194-codereview-codex-5-5.md:699:+    mapping(uint32 => BanditTroop) internal _bandits;
docs/reviews/pr194-codereview-codex-5-5.md:701:+    mapping(uint8 => BanditSpawnState) internal _banditSpawnByRegion;
docs/reviews/pr194-codereview-codex-5-5.md:706:+    uint32 internal _nextBanditId;
docs/reviews/pr194-codereview-codex-5-5.md:707:+    uint32 internal _activeBanditCount;
docs/reviews/pr194-codereview-codex-5-5.md:716:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr194-codereview-codex-5-5.md:725:+    /// @dev Bandit spawn weights are a heartbeat-time heuristic. V1 has
docs/reviews/pr194-codereview-codex-5-5.md:727:+    ///      at most two rotating heartbeats while keeping heartbeat gas bounded.
docs/reviews/pr194-codereview-codex-5-5.md:732:+    ///      so this settles all possible bases today while keeping the heartbeat
docs/reviews/pr194-codereview-codex-5-5.md:744:+    struct BanditSpawnState {
docs/reviews/pr194-codereview-codex-5-5.md:759:         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
docs/reviews/pr194-codereview-codex-5-5.md:770:+        _nextBanditId = 1;
docs/reviews/pr194-codereview-codex-5-5.md:834:+    function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId) internal {
docs/reviews/pr194-codereview-codex-5-5.md:862:+        _abortBanditAttacksForDeadTarget(clanId, excludedBanditId);
docs/reviews/pr194-codereview-codex-5-5.md:877:+                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
docs/reviews/pr194-codereview-codex-5-5.md:892:+    function _abortBanditAttacksForDeadTarget(uint32 deadClanId, uint32 excludedBanditId) internal {
docs/reviews/pr194-codereview-codex-5-5.md:893:+        // Match _transitionBanditState's event stamp; heartbeat keeps currentTick
docs/reviews/pr194-codereview-codex-5-5.md:897:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:898:+            for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-5.md:899:+                uint32 banditId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-5.md:900:+                if (banditId == excludedBanditId) continue;
docs/reviews/pr194-codereview-codex-5-5.md:902:+                BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-5.md:903:+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
docs/reviews/pr194-codereview-codex-5-5.md:904:+                    _transitionBanditState(banditId, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-5.md:905:+                    emit BanditEscaped(banditId, currentTick);
docs/reviews/pr194-codereview-codex-5-5.md:906:+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
docs/reviews/pr194-codereview-codex-5-5.md:1037:+        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-5.md:1065:+            blueprintCost = 1e18;
docs/reviews/pr194-codereview-codex-5-5.md:1070:+                || clan.blueprintBalance < blueprintCost
docs/reviews/pr194-codereview-codex-5-5.md:1076:+        clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-5.md:1239:+                cs.currentRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-5.md:1378:+        if (m.targetRegion == ClanWorldConstants.REGION_WEST_FARMS) {
docs/reviews/pr194-codereview-codex-5-5.md:1380:+        } else if (m.targetRegion == ClanWorldConstants.REGION_EAST_FARMS) {
docs/reviews/pr194-codereview-codex-5-5.md:1519:+        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-5.md:1546:+            blueprintCost = 1e18;
docs/reviews/pr194-codereview-codex-5-5.md:1551:+                || sim.clan.blueprintBalance < blueprintCost
docs/reviews/pr194-codereview-codex-5-5.md:1557:+        sim.clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-5.md:1576:+    function _spawnBandit(uint8 region, uint32 strength) internal returns (uint32 id) {
docs/reviews/pr194-codereview-codex-5-5.md:1583:+        id = _nextBanditId++;
docs/reviews/pr194-codereview-codex-5-5.md:1584:+        _bandits[id] = BanditTroop({
docs/reviews/pr194-codereview-codex-5-5.md:1587:+            state: BanditState.Spawned,
docs/reviews/pr194-codereview-codex-5-5.md:1588:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-5.md:1598:+        _activeBanditCount += 1;
docs/reviews/pr194-codereview-codex-5-5.md:1600:+        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:1604:+        if (_world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
docs/reviews/pr194-codereview-codex-5-5.md:1605:+            _world.activeBanditId = id;
docs/reviews/pr194-codereview-codex-5-5.md:1608:+        emit BanditSpawned(id, region, 0, _banditStrengthForLegacyEvent(strength));
docs/reviews/pr194-codereview-codex-5-5.md:1611:+    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1612:+        require(targetClanId != ClanWorldConstants.CLAN_ID_NULL, "ClanWorld: invalid bandit target");
docs/reviews/pr194-codereview-codex-5-5.md:1613:+        _bandits[id].targetClanId = targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:1614:+        _transitionBanditState(id, BanditState.Attacking);
docs/reviews/pr194-codereview-codex-5-5.md:1617:+    function _transitionBanditState(uint32 id, BanditState newState) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1618:+        BanditTroop storage bandit = _bandits[id];
docs/reviews/pr194-codereview-codex-5-5.md:1620:+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
docs/reviews/pr194-codereview-codex-5-5.md:1622:+        BanditState oldState = bandit.state;
docs/reviews/pr194-codereview-codex-5-5.md:1623:+        require(_isValidBanditTransition(bandit, newState), "ClanWorld: invalid bandit transition");
docs/reviews/pr194-codereview-codex-5-5.md:1625:+        if (newState == BanditState.Defeated) {
docs/reviews/pr194-codereview-codex-5-5.md:1626:+            emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
docs/reviews/pr194-codereview-codex-5-5.md:1627:+            _deleteBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:1633:+        if (newState != BanditState.Attacking) {
docs/reviews/pr194-codereview-codex-5-5.md:1634:+            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
docs/reviews/pr194-codereview-codex-5-5.md:1637:+        emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
docs/reviews/pr194-codereview-codex-5-5.md:1640:+    function _isValidBanditTransition(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:1641:+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
docs/reviews/pr194-codereview-codex-5-5.md:1642:+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
docs/reviews/pr194-codereview-codex-5-5.md:1643:+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
docs/reviews/pr194-codereview-codex-5-5.md:1644:+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
docs/reviews/pr194-codereview-codex-5-5.md:1645:+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
docs/reviews/pr194-codereview-codex-5-5.md:1649:+    function _canBanditLeaveSpawned(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:1650:+        return newState == BanditState.Escaped
docs/reviews/pr194-codereview-codex-5-5.md:1651:+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
docs/reviews/pr194-codereview-codex-5-5.md:1654:+    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:1655:+        return newState == BanditState.Escaped
docs/reviews/pr194-codereview-codex-5-5.md:1656:+            || (newState == BanditState.Attacking
docs/reviews/pr194-codereview-codex-5-5.md:1657:+                && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
docs/reviews/pr194-codereview-codex-5-5.md:1661:+    function _canBanditLeaveAttacking(BanditState newState) internal pure returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:1662:+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
docs/reviews/pr194-codereview-codex-5-5.md:1665:+    function _canBanditLeaveEscaped(BanditState newState) internal pure returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:1666:+        return newState == BanditState.Resting;
docs/reviews/pr194-codereview-codex-5-5.md:1669:+    function _canBanditLeaveResting(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:1670:+        return newState == BanditState.Escaped
docs/reviews/pr194-codereview-codex-5-5.md:1671:+            || (newState == BanditState.Camped
docs/reviews/pr194-codereview-codex-5-5.md:1675:+    function _deleteBandit(uint32 id) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1676:+        BanditTroop storage bandit = _bandits[id];
docs/reviews/pr194-codereview-codex-5-5.md:1678:+        uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:1679:+        for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-5.md:1680:+            if (regionBandits[i] == id) {
docs/reviews/pr194-codereview-codex-5-5.md:1681:+                regionBandits[i] = regionBandits[regionBandits.length - 1];
docs/reviews/pr194-codereview-codex-5-5.md:1682:+                regionBandits.pop();
docs/reviews/pr194-codereview-codex-5-5.md:1688:+        if (_activeBanditCount > 0) {
docs/reviews/pr194-codereview-codex-5-5.md:1689:+            _activeBanditCount -= 1;
docs/reviews/pr194-codereview-codex-5-5.md:1691:+        if (_world.activeBanditId == id) {
docs/reviews/pr194-codereview-codex-5-5.md:1692:+            _world.activeBanditId = _findOldestActiveBandit();
docs/reviews/pr194-codereview-codex-5-5.md:1696:+    function _findOldestActiveBandit() internal view returns (uint32 oldestBanditId) {
docs/reviews/pr194-codereview-codex-5-5.md:1700:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:1701:+            for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-5.md:1702:+                uint32 candidateId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-5.md:1703:+                BanditTroop storage candidate = _bandits[candidateId];
docs/reviews/pr194-codereview-codex-5-5.md:1704:+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
docs/reviews/pr194-codereview-codex-5-5.md:1707:+                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
docs/reviews/pr194-codereview-codex-5-5.md:1708:+                    oldestBanditId = candidateId;
docs/reviews/pr194-codereview-codex-5-5.md:1714:+    function _advanceBanditStates(uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1717:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:1718:+            for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-5.md:1719:+                uint32 banditId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-5.md:1720:+                BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-5.md:1721:+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
docs/reviews/pr194-codereview-codex-5-5.md:1722:+                    _transitionBanditState(banditId, BanditState.Camped);
docs/reviews/pr194-codereview-codex-5-5.md:1724:+                    bandit.state == BanditState.Resting
docs/reviews/pr194-codereview-codex-5-5.md:1727:+                    _transitionBanditState(banditId, BanditState.Camped);
docs/reviews/pr194-codereview-codex-5-5.md:1739:+    function _resolveAttackingBandits(uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1741:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:1743:+            while (i < regionBandits.length) {
docs/reviews/pr194-codereview-codex-5-5.md:1744:+                uint32 banditId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-5.md:1745:+                BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-5.md:1746:+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
docs/reviews/pr194-codereview-codex-5-5.md:1748:+                    _resolveBanditAttack(banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:1758:+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1761:+        BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-5.md:1762:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
docs/reviews/pr194-codereview-codex-5-5.md:1769:+        uint32 targetClanId = bandit.targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:1770:+        Clan storage targetClan = _clans[targetClanId];
docs/reviews/pr194-codereview-codex-5-5.md:1771:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-codereview-codex-5-5.md:1772:+            _transitionBanditState(banditId, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-5.md:1773:+            emit BanditEscaped(banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:1777:+        _settleClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:1778:+        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
docs/reviews/pr194-codereview-codex-5-5.md:1780:+        bytes32 tickSeed = _world.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:1782:+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:1791:+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
docs/reviews/pr194-codereview-codex-5-5.md:1792:+            (incomingDamage, baseAbsorbed) = _applyBanditBaseDefense(targetClan, incomingDamage);
docs/reviews/pr194-codereview-codex-5-5.md:1794:+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:1798:+        emit BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-5.md:1800:+            targetClanId,
docs/reviews/pr194-codereview-codex-5-5.md:1804:+            targetClan.wallLevel,
docs/reviews/pr194-codereview-codex-5-5.md:1813:+            emit BanditDefeated(banditId, targetClanId, closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:1814:+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:1815:+            targetClan.blueprintBalance += 1;
docs/reviews/pr194-codereview-codex-5-5.md:1816:+            emit BlueprintEarned(targetClanId, banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:1817:+            _transitionBanditState(banditId, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-5.md:1819:+            _transitionBanditState(banditId, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-5.md:1820:+            emit BanditEscaped(banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:1824:+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
docs/reviews/pr194-codereview-codex-5-5.md:1825:+        BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-5.md:1826:+        uint32[] memory rewardedClanIds = _activeDefendingClanIds(targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:1835:+            perWood = _perClanBanditLootShare(bandit.carryWood, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-5.md:1836:+            perIron = _perClanBanditLootShare(bandit.carryIron, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-5.md:1837:+            perWheat = _perClanBanditLootShare(bandit.carryWheat, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-5.md:1838:+            perFish = _perClanBanditLootShare(bandit.carryFish, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-5.md:1839:+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-5.md:1867:+    function _perClanBanditLootShare(uint256 loot, uint256 nDefendingClans) internal pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-5.md:1869:+            return loot;
docs/reviews/pr194-codereview-codex-5-5.md:1871:+        return ((loot / RESOURCE_UNIT) / nDefendingClans) * RESOURCE_UNIT;
docs/reviews/pr194-codereview-codex-5-5.md:1874:+    function _activeDefendingClanIds(uint32 targetClanId) internal view returns (uint32[] memory clanIds) {
docs/reviews/pr194-codereview-codex-5-5.md:1875:+        uint8 targetRegion = _clans[targetClanId].baseRegion;
docs/reviews/pr194-codereview-codex-5-5.md:1876:+        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
docs/reviews/pr194-codereview-codex-5-5.md:1880:+            if (_clanHasActiveDefenderForTarget(defendingClans[i], targetClanId, targetRegion)) {
docs/reviews/pr194-codereview-codex-5-5.md:1889:+            if (_clanHasActiveDefenderForTarget(defenderClanId, targetClanId, targetRegion)) {
docs/reviews/pr194-codereview-codex-5-5.md:1895:+    function _clanHasActiveDefenderForTarget(uint32 defenderClanId, uint32 targetClanId, uint8 targetRegion)
docs/reviews/pr194-codereview-codex-5-5.md:1911:+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
docs/reviews/pr194-codereview-codex-5-5.md:1912:+                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
docs/reviews/pr194-codereview-codex-5-5.md:1920:+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
docs/reviews/pr194-codereview-codex-5-5.md:1925:+        uint8 targetRegion = _clans[targetClanId].baseRegion;
docs/reviews/pr194-codereview-codex-5-5.md:1926:+        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
docs/reviews/pr194-codereview-codex-5-5.md:1941:+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
docs/reviews/pr194-codereview-codex-5-5.md:1942:+                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
docs/reviews/pr194-codereview-codex-5-5.md:1950:+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
docs/reviews/pr194-codereview-codex-5-5.md:1965:+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
docs/reviews/pr194-codereview-codex-5-5.md:1969:+    function _applyBanditBaseDefense(Clan storage clan, uint32 incomingDamage)
docs/reviews/pr194-codereview-codex-5-5.md:1984:+    function _applyBanditClansmanCasualties(
docs/reviews/pr194-codereview-codex-5-5.md:1994:+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:2005:+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
docs/reviews/pr194-codereview-codex-5-5.md:2015:+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
docs/reviews/pr194-codereview-codex-5-5.md:2062:+    function _evaluateBanditSpawns(bytes32 tickSeed) internal {
docs/reviews/pr194-codereview-codex-5-5.md:2064:+        if (_activeBanditCount >= MAX_TOTAL_BANDITS) {
docs/reviews/pr194-codereview-codex-5-5.md:2065:+            _refreshBanditSpawnWorldPreview(regionWeights);
docs/reviews/pr194-codereview-codex-5-5.md:2076:+            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:2081:+            spawnState.probabilityAccum = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
docs/reviews/pr194-codereview-codex-5-5.md:2089:+        uint8 selectedRegion = _selectBanditSpawnRegion(tickSeed, candidateWeights);
docs/reviews/pr194-codereview-codex-5-5.md:2091:+            // _spawnBandit resets only the selected region's accumulator; other
docs/reviews/pr194-codereview-codex-5-5.md:2093:+            _spawnBandit(selectedRegion, _banditSpawnStrength(tickSeed, selectedRegion));
docs/reviews/pr194-codereview-codex-5-5.md:2096:+        _refreshBanditSpawnWorldPreview(regionWeights);
docs/reviews/pr194-codereview-codex-5-5.md:2111:+    function _incrementBanditSpawnProbability(uint16 probabilityAccum) internal pure returns (uint16) {
docs/reviews/pr194-codereview-codex-5-5.md:2143:+    function _selectBanditSpawnRegion(bytes32 tickSeed, uint256[] memory weights) internal pure returns (uint8) {
docs/reviews/pr194-codereview-codex-5-5.md:2167:+    function _eagerSettleForBandits(uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-5.md:2168:+        require(_world.currentTick == closedTick, "ClanWorld: eager settle tick mismatch");
docs/reviews/pr194-codereview-codex-5-5.md:2169:+        if (_activeBanditCount >= MAX_TOTAL_BANDITS) return;
docs/reviews/pr194-codereview-codex-5-5.md:2188:+            if (!_isBanditSpawnRegionCandidate(regionWeights, region)) {
docs/reviews/pr194-codereview-codex-5-5.md:2191:+            _eagerSettleBanditCandidateRegion(region);
docs/reviews/pr194-codereview-codex-5-5.md:2196:+    function _isBanditSpawnRegionCandidate(uint256[] memory regionWeights, uint8 region) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-5.md:2207:+        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:2212:+        uint16 nextProbability = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
docs/reviews/pr194-codereview-codex-5-5.md:2213:+        return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
docs/reviews/pr194-codereview-codex-5-5.md:2219:+    function _eagerSettleBanditCandidateRegion(uint8 region) internal {
docs/reviews/pr194-codereview-codex-5-5.md:2251:+            _eagerSettleActiveDefendersForBase(clanId, region);
docs/reviews/pr194-codereview-codex-5-5.md:2256:+    function _eagerSettleActiveDefendersForBase(uint32 targetClanId, uint8 region) internal {
docs/reviews/pr194-codereview-codex-5-5.md:2285:+                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
docs/reviews/pr194-codereview-codex-5-5.md:2294:-        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-5.md:2327:-            blueprintCost = 1e18;
docs/reviews/pr194-codereview-codex-5-5.md:2337:-                || clan.blueprintBalance < blueprintCost
docs/reviews/pr194-codereview-codex-5-5.md:2351:-        clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-5.md:2356:+                weights[clan.baseRegion - 1] += 100 + (_lootValueRaw(clan) / 1e18);
docs/reviews/pr194-codereview-codex-5-5.md:2387:+    function _refreshBanditSpawnWorldPreview(uint256[] memory regionWeights) internal {
docs/reviews/pr194-codereview-codex-5-5.md:2392:+            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-5.md:2395:+                _activeBanditCount < MAX_TOTAL_BANDITS && regionWeights[region - 1] > 0
docs/reviews/pr194-codereview-codex-5-5.md:2405:+        _world.nextBanditSpawnEligibleTick = nextEligibleTick == type(uint64).max ? 0 : nextEligibleTick;
docs/reviews/pr194-codereview-codex-5-5.md:2406:+        _world.currentBanditSpawnChanceBps = maxChance;
docs/reviews/pr194-codereview-codex-5-5.md:2411:     /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
docs/reviews/pr194-codereview-codex-5-5.md:2421:+    ///         3. Eager-settle clans touched by world events (Phase 9 stub).
docs/reviews/pr194-codereview-codex-5-5.md:2424:     function heartbeat() external override nonReentrant {
docs/reviews/pr194-codereview-codex-5-5.md:2425:         require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
docs/reviews/pr194-codereview-codex-5-5.md:2428:+        bytes32 closedTickSeed = _world.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:2434:-        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
docs/reviews/pr194-codereview-codex-5-5.md:2436:-        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-5.md:2446:-        // TODO Phase 3: _settleClansNearBandit(closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:2448:+        _eagerSettleForBandits(closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:2452:+        _advanceBanditStates(closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:2455:+        _resolveAttackingBandits(closedTick);
docs/reviews/pr194-codereview-codex-5-5.md:2458:+        _evaluateBanditSpawns(closedTickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:2469:+        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-5.md:2483:         ctx.targetClanId =
docs/reviews/pr194-codereview-codex-5-5.md:2484:             order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:2496:-        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
docs/reviews/pr194-codereview-codex-5-5.md:2497:+        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:2498:+        Clan storage targetClan = _clans[targetClanId];
docs/reviews/pr194-codereview-codex-5-5.md:2499:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-codereview-codex-5-5.md:2502:+        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
docs/reviews/pr194-codereview-codex-5-5.md:2510:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2511:-        return BanditTroop({
docs/reviews/pr194-codereview-codex-5-5.md:2513:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:2525:+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2526:+        BanditTroop memory bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-5.md:2527:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
docs/reviews/pr194-codereview-codex-5-5.md:2528:+            return BanditTroop({
docs/reviews/pr194-codereview-codex-5-5.md:2531:+                state: BanditState.None,
docs/reviews/pr194-codereview-codex-5-5.md:2532:+                targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-5.md:2545:+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2546:+        return getBandit(banditId);
docs/reviews/pr194-codereview-codex-5-5.md:2549:+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2563:-        uint256 lootVal = _lootValueRaw(clan);
docs/reviews/pr194-codereview-codex-5-5.md:2565:-            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
docs/reviews/pr194-codereview-codex-5-5.md:2575:-            // Simplified: if past arrivalTick, they're at target; else at start
docs/reviews/pr194-codereview-codex-5-5.md:2577:-                effectiveRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-5.md:2611:-    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:2613:+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:2614:+        return _bandits[banditId].targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:2623:-        return _lootValueRaw(_clans[clanId]);
docs/reviews/pr194-codereview-codex-5-5.md:2625:+        return _lootValueRaw(sim.clan);
docs/reviews/pr194-codereview-codex-5-5.md:2628:     /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
docs/reviews/pr194-codereview-codex-5-5.md:2636:+            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
docs/reviews/pr194-codereview-codex-5-5.md:2654:+            return tick >= m.arrivalTick ? m.targetRegion : m.startRegion;
docs/reviews/pr194-codereview-codex-5-5.md:2672:-        uint256 lootVal = _lootValueRaw(clan);
docs/reviews/pr194-codereview-codex-5-5.md:2675:-            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
docs/reviews/pr194-codereview-codex-5-5.md:2687:-                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
docs/reviews/pr194-codereview-codex-5-5.md:2714:+                thisClanDefendingBaseId = sim.missions[i].targetRegion;
docs/reviews/pr194-codereview-codex-5-5.md:2745:-    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2746:+    function getActiveBanditView() external view override returns (ActiveBanditView memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2747:+        BanditTroop memory bandit = _bandits[_world.activeBanditId];
docs/reviews/pr194-codereview-codex-5-5.md:2749:+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
docs/reviews/pr194-codereview-codex-5-5.md:2751:+            if (bandit.state == BanditState.Spawned) {
docs/reviews/pr194-codereview-codex-5-5.md:2753:+            } else if (bandit.state == BanditState.Camped) {
docs/reviews/pr194-codereview-codex-5-5.md:2755:+            } else if (bandit.state == BanditState.Resting) {
docs/reviews/pr194-codereview-codex-5-5.md:2760:         return ActiveBanditView({
docs/reviews/pr194-codereview-codex-5-5.md:2763:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:2787:+            projectedTargetClanId: bandit.targetClanId,
docs/reviews/pr194-codereview-codex-5-5.md:2799:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2800:+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2801:         return BanditTroop({
docs/reviews/pr194-codereview-codex-5-5.md:2803:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:2812:+            state: BanditState.None,
docs/reviews/pr194-codereview-codex-5-5.md:2813:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-5.md:2825:+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2826:+        return getBandit(banditId);
docs/reviews/pr194-codereview-codex-5-5.md:2829:+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
docs/reviews/pr194-codereview-codex-5-5.md:2837:         return ActiveBanditView({
docs/reviews/pr194-codereview-codex-5-5.md:2840:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:2841:+            state: BanditState.None,
docs/reviews/pr194-codereview-codex-5-5.md:2853:+// v1 ABI: Bandit state machine redesigned in Phase 9. ABI consumers must regenerate.
docs/reviews/pr194-codereview-codex-5-5.md:2854: enum BanditState {
docs/reviews/pr194-codereview-codex-5-5.md:2876:-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
docs/reviews/pr194-codereview-codex-5-5.md:2878:+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
docs/reviews/pr194-codereview-codex-5-5.md:2881:     uint64 nextBanditSpawnEligibleTick;
docs/reviews/pr194-codereview-codex-5-5.md:2886:+// v1 ABI: Bandit troop layout redesigned in Phase 9. ABI consumers must regenerate.
docs/reviews/pr194-codereview-codex-5-5.md:2887: struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-5.md:2891:     BanditState state;
docs/reviews/pr194-codereview-codex-5-5.md:2901:+    uint32 targetClanId; // 0 if not attacking
docs/reviews/pr194-codereview-codex-5-5.md:2914:     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-5.md:2915:     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-5.md:2916:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
docs/reviews/pr194-codereview-codex-5-5.md:2917:+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
docs/reviews/pr194-codereview-codex-5-5.md:2918:+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
docs/reviews/pr194-codereview-codex-5-5.md:2942:+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-5.md:2944:     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-5.md:2946:+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
docs/reviews/pr194-codereview-codex-5-5.md:2951:diff --git a/packages/contracts/test/Bandit.t.sol b/packages/contracts/test/Bandit.t.sol
docs/reviews/pr194-codereview-codex-5-5.md:2955:+++ b/packages/contracts/test/Bandit.t.sol
docs/reviews/pr194-codereview-codex-5-5.md:2962:+import {ActiveBanditView, ClanWorldConstants, BanditState, BanditTroop, WorldState} from "../src/IClanWorld.sol";
docs/reviews/pr194-codereview-codex-5-5.md:2964:+contract BanditHarness is ClanWorld {
docs/reviews/pr194-codereview-codex-5-5.md:2965:+    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:2966:+        return _spawnBandit(region, strength);
docs/reviews/pr194-codereview-codex-5-5.md:2969:+    function transitionBandit(uint32 id, BanditState newState) external {
docs/reviews/pr194-codereview-codex-5-5.md:2970:+        _transitionBanditState(id, newState);
docs/reviews/pr194-codereview-codex-5-5.md:2973:+    function transitionBanditToAttacking(uint32 id, uint32 targetClanId) external {
docs/reviews/pr194-codereview-codex-5-5.md:2974:+        _transitionBanditToAttacking(id, targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:2978:+contract BanditTest is Test {
docs/reviews/pr194-codereview-codex-5-5.md:2979:+    BanditHarness world;
docs/reviews/pr194-codereview-codex-5-5.md:2982:+        world = new BanditHarness();
docs/reviews/pr194-codereview-codex-5-5.md:2987:+        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-5.md:2997:+        id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3001:+    function _spawnCampAndAttack(uint32 targetClanId) internal returns (uint32 id) {
docs/reviews/pr194-codereview-codex-5-5.md:3004:+        world.transitionBanditToAttacking(id, targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:3007:+    function test_spawnBandit_recordsSpawnedTroopAndRegionIndex() public {
docs/reviews/pr194-codereview-codex-5-5.md:3008:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 250);
docs/reviews/pr194-codereview-codex-5-5.md:3011:+        BanditTroop memory bandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3014:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
docs/reviews/pr194-codereview-codex-5-5.md:3015:+        assertEq(bandit.targetClanId, 0, "no target while spawned");
docs/reviews/pr194-codereview-codex-5-5.md:3019:+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
docs/reviews/pr194-codereview-codex-5-5.md:3020:+        assertEq(regionBandits.length, 1, "region index length");
docs/reviews/pr194-codereview-codex-5-5.md:3021:+        assertEq(regionBandits[0], id, "region index id");
docs/reviews/pr194-codereview-codex-5-5.md:3024:+        assertEq(state.activeBanditId, id, "active bandit");
docs/reviews/pr194-codereview-codex-5-5.md:3027:+    function test_getBanditMissingIdReturnsNoneState() public view {
docs/reviews/pr194-codereview-codex-5-5.md:3028:+        BanditTroop memory bandit = world.getBandit(999);
docs/reviews/pr194-codereview-codex-5-5.md:3031:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
docs/reviews/pr194-codereview-codex-5-5.md:3034:+    function test_defaultBanditTroopStateIsNone() public pure {
docs/reviews/pr194-codereview-codex-5-5.md:3035:+        BanditTroop memory bandit;
docs/reviews/pr194-codereview-codex-5-5.md:3037:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
docs/reviews/pr194-codereview-codex-5-5.md:3041:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3045:+        BanditTroop memory bandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3046:+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
docs/reviews/pr194-codereview-codex-5-5.md:3050:+    function test_spawnedActiveBanditViewReportsNextActionTick() public {
docs/reviews/pr194-codereview-codex-5-5.md:3051:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3053:+        ActiveBanditView memory view_ = world.getActiveBanditView();
docs/reviews/pr194-codereview-codex-5-5.md:3056:+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
docs/reviews/pr194-codereview-codex-5-5.md:3064:+        world.transitionBanditToAttacking(id, 77);
docs/reviews/pr194-codereview-codex-5-5.md:3066:+        BanditTroop memory bandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3067:+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
docs/reviews/pr194-codereview-codex-5-5.md:3068:+        assertEq(bandit.targetClanId, 77, "target");
docs/reviews/pr194-codereview-codex-5-5.md:3072:+    function test_attackingToDefeatedDeletesBanditAndRegionIndex() public {
docs/reviews/pr194-codereview-codex-5-5.md:3075:+        world.transitionBandit(id, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-5.md:3077:+        BanditTroop memory deletedBandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3078:+        assertEq(deletedBandit.id, 0, "deleted id");
docs/reviews/pr194-codereview-codex-5-5.md:3079:+        assertEq(deletedBandit.region, 0, "deleted region");
docs/reviews/pr194-codereview-codex-5-5.md:3080:+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
docs/reviews/pr194-codereview-codex-5-5.md:3081:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
docs/reviews/pr194-codereview-codex-5-5.md:3082:+        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
docs/reviews/pr194-codereview-codex-5-5.md:3085:+    function test_defeatingActiveBanditPromotesOldestRemainingBandit() public {
docs/reviews/pr194-codereview-codex-5-5.md:3086:+        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3087:+        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 200);
docs/reviews/pr194-codereview-codex-5-5.md:3088:+        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_WEST_FARMS, 300);
docs/reviews/pr194-codereview-codex-5-5.md:3092:+        world.transitionBanditToAttacking(id1, 77);
docs/reviews/pr194-codereview-codex-5-5.md:3093:+        world.transitionBandit(id1, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-5.md:3095:+        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
docs/reviews/pr194-codereview-codex-5-5.md:3096:+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
docs/reviews/pr194-codereview-codex-5-5.md:3102:+        world.transitionBandit(id, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-5.md:3103:+        BanditTroop memory escaped = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3104:+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
docs/reviews/pr194-codereview-codex-5-5.md:3105:+        assertEq(escaped.targetClanId, 0, "target cleared on escape");
docs/reviews/pr194-codereview-codex-5-5.md:3107:+        world.transitionBandit(id, BanditState.Resting);
docs/reviews/pr194-codereview-codex-5-5.md:3108:+        BanditTroop memory resting = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3109:+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
docs/reviews/pr194-codereview-codex-5-5.md:3114:+        BanditTroop memory camped = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-5.md:3115:+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
docs/reviews/pr194-codereview-codex-5-5.md:3116:+        assertEq(camped.targetClanId, 0, "target remains clear");
docs/reviews/pr194-codereview-codex-5-5.md:3120:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3123:+        world.transitionBandit(id, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-5.md:3129:+        world.transitionBandit(id, BanditState.Attacking);
docs/reviews/pr194-codereview-codex-5-5.md:3132:+        world.transitionBandit(id, BanditState.None);
docs/reviews/pr194-codereview-codex-5-5.md:3135:+    function test_getBanditsInRegionReturnsListAndUpdatesAfterDelete() public {
docs/reviews/pr194-codereview-codex-5-5.md:3136:+        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3137:+        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 200);
docs/reviews/pr194-codereview-codex-5-5.md:3138:+        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 300);
docs/reviews/pr194-codereview-codex-5-5.md:3140:+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-5.md:3141:+        assertEq(forestBandits.length, 2, "forest count");
docs/reviews/pr194-codereview-codex-5-5.md:3142:+        assertEq(forestBandits[0], id1, "first forest bandit");
docs/reviews/pr194-codereview-codex-5-5.md:3143:+        assertEq(forestBandits[1], id2, "second forest bandit");
docs/reviews/pr194-codereview-codex-5-5.md:3145:+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
docs/reviews/pr194-codereview-codex-5-5.md:3146:+        assertEq(mountainBandits.length, 1, "mountain count");
docs/reviews/pr194-codereview-codex-5-5.md:3147:+        assertEq(mountainBandits[0], id3, "mountain bandit");
docs/reviews/pr194-codereview-codex-5-5.md:3151:+        world.transitionBanditToAttacking(id1, 77);
docs/reviews/pr194-codereview-codex-5-5.md:3152:+        world.transitionBandit(id1, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-5.md:3154:+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-5.md:3155:+        assertEq(forestBandits.length, 1, "forest count after delete");
docs/reviews/pr194-codereview-codex-5-5.md:3156:+        assertEq(forestBandits[0], id2, "remaining forest bandit");
docs/reviews/pr194-codereview-codex-5-5.md:3159:diff --git a/packages/contracts/test/BanditAttackResolution.t.sol b/packages/contracts/test/BanditAttackResolution.t.sol
docs/reviews/pr194-codereview-codex-5-5.md:3163:+++ b/packages/contracts/test/BanditAttackResolution.t.sol
docs/reviews/pr194-codereview-codex-5-5.md:3175:+    BanditState,
docs/reviews/pr194-codereview-codex-5-5.md:3184:+contract BanditAttackHarness is ClanWorld {
docs/reviews/pr194-codereview-codex-5-5.md:3185:+    function forceAttackingBandit(uint8 region, uint32 strength, uint32 targetClanId) external returns (uint32 id) {
docs/reviews/pr194-codereview-codex-5-5.md:3186:+        id = _spawnBandit(region, strength);
docs/reviews/pr194-codereview-codex-5-5.md:3187:+        _bandits[id].state = BanditState.Attacking;
docs/reviews/pr194-codereview-codex-5-5.md:3188:+        _bandits[id].targetClanId = targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:3209:+    function setBanditStrength(uint32 banditId, uint32 strength) external {
docs/reviews/pr194-codereview-codex-5-5.md:3213:+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
docs/reviews/pr194-codereview-codex-5-5.md:3228:+contract BanditAttackResolutionTest is Test {
docs/reviews/pr194-codereview-codex-5-5.md:3229:+    BanditAttackHarness world;
docs/reviews/pr194-codereview-codex-5-5.md:3232:+    event BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-5.md:3234:+        uint32 indexed targetClanId,
docs/reviews/pr194-codereview-codex-5-5.md:3245:+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-5.md:3247:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
docs/reviews/pr194-codereview-codex-5-5.md:3264:+        world = new BanditAttackHarness();
docs/reviews/pr194-codereview-codex-5-5.md:3281:+    function _defendTargetOrders(uint32 clanId, uint32 targetClanId, uint8 targetRegion, uint256 count)
docs/reviews/pr194-codereview-codex-5-5.md:3290:+                gotoRegion: targetRegion,
docs/reviews/pr194-codereview-codex-5-5.md:3292:+                targetClanId: targetClanId,
docs/reviews/pr194-codereview-codex-5-5.md:3304:+    function _submitTargetDefenders(uint32 defenderClanId, uint32 targetClanId, uint256 count)
docs/reviews/pr194-codereview-codex-5-5.md:3308:+        Clan memory targetClan = world.getClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:3309:+        ClanOrder[] memory orders = _defendTargetOrders(defenderClanId, targetClanId, targetClan.baseRegion, count);
docs/reviews/pr194-codereview-codex-5-5.md:3323:+        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-5.md:3338:+    function _assertBanditTargetDiedLog(
docs/reviews/pr194-codereview-codex-5-5.md:3340:+        uint32 expectedBanditId,
docs/reviews/pr194-codereview-codex-5-5.md:3344:+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
docs/reviews/pr194-codereview-codex-5-5.md:3345:+        bytes32 expectedBanditTopic = bytes32(uint256(expectedBanditId));
docs/reviews/pr194-codereview-codex-5-5.md:3350:+                    && logs[i].topics[1] == expectedBanditTopic && logs[i].topics[2] == expectedClanTopic
docs/reviews/pr194-codereview-codex-5-5.md:3356:+        fail("expected BanditTargetDied log");
docs/reviews/pr194-codereview-codex-5-5.md:3359:+    function _activateTargetDefenders(uint32 targetClanId, uint32[] memory defenderClanIds, uint256 countEach)
docs/reviews/pr194-codereview-codex-5-5.md:3364:+            uint64 executesAtTick = _submitTargetDefenders(defenderClanIds[i], targetClanId, countEach);
docs/reviews/pr194-codereview-codex-5-5.md:3378:+        return world.forceAttackingBandit(clan.baseRegion, strength, clanId);
docs/reviews/pr194-codereview-codex-5-5.md:3388:+    function test_defeatedBanditLootSingleDefendingClanGetsFullCarry() public {
docs/reviews/pr194-codereview-codex-5-5.md:3396:+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
docs/reviews/pr194-codereview-codex-5-5.md:3397:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3407:+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
docs/reviews/pr194-codereview-codex-5-5.md:3410:+    function test_defeatedBanditAwardsBlueprintToTargetClan() public {
docs/reviews/pr194-codereview-codex-5-5.md:3416:+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-5.md:3418:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3425:+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 1, "target blueprint awarded");
docs/reviews/pr194-codereview-codex-5-5.md:3428:+    function test_deadTargetCleanupReleasesDefendersAndEscapesBandit() public {
docs/reviews/pr194-codereview-codex-5-5.md:3431:+        uint32 targetClanId = clanIds[1];
docs/reviews/pr194-codereview-codex-5-5.md:3434:+        uint64 defenderExecutesAtTick = _submitTargetDefenders(defenderClanId, targetClanId, 1);
docs/reviews/pr194-codereview-codex-5-5.md:3441:+        assertEq(uint8(defenderStateBefore), uint8(ClansmanState.ACTING), "defender active before target death");
docs/reviews/pr194-codereview-codex-5-5.md:3442:+        assertEq(world.getActiveDefenders(targetClanId).length, 1, "target has active defender");
docs/reviews/pr194-codereview-codex-5-5.md:3447:+        world.setClanUpkeepState(targetClanId, deathFromTick, 0, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3450:+        uint32 banditId = _forceAttack(targetClanId, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3451:+        uint64 expectedBanditAbortTick = world.getWorldState().currentTick;
docs/reviews/pr194-codereview-codex-5-5.md:3454:+        world.settleClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:3456:+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
docs/reviews/pr194-codereview-codex-5-5.md:3458:+        Clan memory targetAfter = world.getClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-5.md:3459:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-codereview-codex-5-5.md:3460:+        assertEq(targetAfter.livingClansmen, 0, "target living count");
docs/reviews/pr194-codereview-codex-5-5.md:3466:+        assertEq(world.getActiveDefenders(targetClanId).length, 0, "target defender registry cleared");
docs/reviews/pr194-codereview-codex-5-5.md:3468:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-5.md:3469:+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
docs/reviews/pr194-codereview-codex-5-5.md:3478:+    function test_defeatedBanditLootSplitsEquallyAcrossFourDefendingClans() public {
docs/reviews/pr194-codereview-codex-5-5.md:3497:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-5.md:3498:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3516:+        uint256[4] memory blueprintBefore;
docs/reviews/pr194-codereview-codex-5-5.md:3518:+            blueprintBefore[i] = world.getClan(clanIds[i]).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-5.md:3522:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3526:+        assertEq(world.getClan(clanIds[0]).blueprintBalance, blueprintBefore[0] + 1, "base owner blueprint");
docs/reviews/pr194-codereview-codex-5-5.md:3528:+            assertEq(world.getClan(clanIds[i]).blueprintBalance, blueprintBefore[i], "helper clan no blueprint");
docs/reviews/pr194-codereview-codex-5-5.md:3532:+    function test_multipleDefeatedBanditsEachAwardBlueprint() public {
docs/reviews/pr194-codereview-codex-5-5.md:3538:+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-5.md:3539:+        uint32 firstBanditId = _forceAttack(clanId, 1);
docs/reviews/pr194-codereview-codex-5-5.md:3540:+        uint32 secondBanditId = _forceAttack(clanId, 1);
docs/reviews/pr194-codereview-codex-5-5.md:3541:+        world.setBanditStrength(firstBanditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3542:+        world.setBanditStrength(secondBanditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3546:+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 2, "one blueprint per defeated bandit");
docs/reviews/pr194-codereview-codex-5-5.md:3549:+    function test_defeatedBanditLootBurnsWholeTokenOverflowAcrossThreeDefendingClans() public {
docs/reviews/pr194-codereview-codex-5-5.md:3562:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-5.md:3563:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3566:+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
docs/reviews/pr194-codereview-codex-5-5.md:3568:+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
docs/reviews/pr194-codereview-codex-5-5.md:3595:+        uint256 targetWoodBefore = world.getClan(clanIds[0]).vaultWood;
docs/reviews/pr194-codereview-codex-5-5.md:3598:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-5.md:3599:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3603:+        assertEq(world.getClan(clanIds[0]).vaultWood, targetWoodBefore + 50e18, "target clan one share");
docs/reviews/pr194-codereview-codex-5-5.md:3607:+    function test_escapedBanditDoesNotDistributeCarry() public {
docs/reviews/pr194-codereview-codex-5-5.md:3611:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-5.md:3621:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-5.md:3624:+    function test_escapedBanditDoesNotAwardBlueprint() public {
docs/reviews/pr194-codereview-codex-5-5.md:3626:+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-5.md:3631:+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore, "blueprint unchanged");
docs/reviews/pr194-codereview-codex-5-5.md:3632:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-5.md:3635:+    function test_twoAliveDefendersWithSufficientDefenseDefeatBanditWithoutWallChip() public {
docs/reviews/pr194-codereview-codex-5-5.md:3641:+        bytes32 tickSeed = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:3646:+        world.setBanditStrength(banditId, strength);
docs/reviews/pr194-codereview-codex-5-5.md:3649:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
docs/reviews/pr194-codereview-codex-5-5.md:3663:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-5.md:3673:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-5.md:3684:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-codereview-codex-5-5.md:3685:+        assertEq(clan.vaultWood, 0, "dead target wood burned");
docs/reviews/pr194-codereview-codex-5-5.md:3686:+        assertEq(clan.vaultWheat, 0, "dead target wheat burned");
docs/reviews/pr194-codereview-codex-5-5.md:3687:+        assertEq(clan.vaultFish, 0, "dead target fish burned");
docs/reviews/pr194-codereview-codex-5-5.md:3688:+        assertEq(clan.vaultIron, 0, "dead target iron burned");
docs/reviews/pr194-codereview-codex-5-5.md:3701:+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
docs/reviews/pr194-codereview-codex-5-5.md:3711:+        BanditAttackHarness a = new BanditAttackHarness();
docs/reviews/pr194-codereview-codex-5-5.md:3712:+        BanditAttackHarness b = new BanditAttackHarness();
docs/reviews/pr194-codereview-codex-5-5.md:3722:+        a.heartbeat();
docs/reviews/pr194-codereview-codex-5-5.md:3723:+        b.heartbeat();
docs/reviews/pr194-codereview-codex-5-5.md:3725:+        assertEq(a.getClan(aFirst).livingClansmen, b.getClan(bFirst).livingClansmen, "first target deterministic");
docs/reviews/pr194-codereview-codex-5-5.md:3726:+        assertEq(a.getClan(aSecond).livingClansmen, b.getClan(bSecond).livingClansmen, "second target deterministic");
docs/reviews/pr194-codereview-codex-5-5.md:3727:+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
docs/reviews/pr194-codereview-codex-5-5.md:3728:+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
docs/reviews/pr194-codereview-codex-5-5.md:3731:+    function _setupTwoAttackWorld(BanditAttackHarness target) internal returns (uint32 firstClanId, uint32 secondClanId) {
docs/reviews/pr194-codereview-codex-5-5.md:3733:+        (firstClanId,) = target.mintClan(elder);
docs/reviews/pr194-codereview-codex-5-5.md:3735:+        (secondClanId,) = target.mintClan(elder);
docs/reviews/pr194-codereview-codex-5-5.md:3737:+        target.forceAttackingBandit(target.getClan(firstClanId).baseRegion, 100, firstClanId);
docs/reviews/pr194-codereview-codex-5-5.md:3738:+        target.forceAttackingBandit(target.getClan(secondClanId).baseRegion, 100, secondClanId);
docs/reviews/pr194-codereview-codex-5-5.md:3741:diff --git a/packages/contracts/test/BanditSpawn.t.sol b/packages/contracts/test/BanditSpawn.t.sol
docs/reviews/pr194-codereview-codex-5-5.md:3745:+++ b/packages/contracts/test/BanditSpawn.t.sol
docs/reviews/pr194-codereview-codex-5-5.md:3754:+    BanditState,
docs/reviews/pr194-codereview-codex-5-5.md:3755:+    BanditTroop,
docs/reviews/pr194-codereview-codex-5-5.md:3766:+contract BanditSpawnHarness is ClanWorld {
docs/reviews/pr194-codereview-codex-5-5.md:3767:+    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:3768:+        return _spawnBandit(region, strength);
docs/reviews/pr194-codereview-codex-5-5.md:3771:+    function evaluateBanditSpawns(bytes32 tickSeed) external {
docs/reviews/pr194-codereview-codex-5-5.md:3772:+        _evaluateBanditSpawns(tickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:3775:+    function setBanditSpawnState(uint8 region, uint64 lastSpawnTick, uint16 probabilityAccum) external {
docs/reviews/pr194-codereview-codex-5-5.md:3780:+    function getBanditSpawnState(uint8 region) external view returns (uint64 lastSpawnTick, uint16 probabilityAccum) {
docs/reviews/pr194-codereview-codex-5-5.md:3785:+    function activeBanditCount() external view returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:3786:+        return _activeBanditCount;
docs/reviews/pr194-codereview-codex-5-5.md:3797:+    function maxBanditsPerRegion() external pure returns (uint8) {
docs/reviews/pr194-codereview-codex-5-5.md:3801:+    function maxTotalBandits() external pure returns (uint8) {
docs/reviews/pr194-codereview-codex-5-5.md:3805:+    function maxBanditSpawnScanPerRegion() external pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-5.md:3809:+    function maxBanditEagerSettleBaseScanPerRegion() external pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-5.md:3818:+contract BanditSpawnTest is Test {
docs/reviews/pr194-codereview-codex-5-5.md:3820:+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
docs/reviews/pr194-codereview-codex-5-5.md:3822:+    BanditSpawnHarness world;
docs/reviews/pr194-codereview-codex-5-5.md:3825:+        world = new BanditSpawnHarness();
docs/reviews/pr194-codereview-codex-5-5.md:3828:+    function _advanceTick(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-5.md:3829:+        vm.warp(target.getWorldState().nextHeartbeatAtTs);
docs/reviews/pr194-codereview-codex-5-5.md:3830:+        target.heartbeat();
docs/reviews/pr194-codereview-codex-5-5.md:3833:+    function _advanceTicks(BanditSpawnHarness target, uint64 ticks) internal {
docs/reviews/pr194-codereview-codex-5-5.md:3835:+            _advanceTick(target);
docs/reviews/pr194-codereview-codex-5-5.md:3839:+    function _advancePastInitialCooldown(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-5.md:3840:+        _advanceTicks(target, target.minSpawnCooldownTicks());
docs/reviews/pr194-codereview-codex-5-5.md:3843:+    function _mintForestClan(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-5.md:3844:+        target.mintClan(address(this));
docs/reviews/pr194-codereview-codex-5-5.md:3847:+    function _mintUntilTwoForestClans(BanditSpawnHarness target) internal returns (uint32 first, uint32 second) {
docs/reviews/pr194-codereview-codex-5-5.md:3848:+        for (uint256 i = 0; i < target.maxBanditEagerSettleBaseScanPerRegion(); i++) {
docs/reviews/pr194-codereview-codex-5-5.md:3849:+            (uint32 clanId,) = target.mintClan(address(this));
docs/reviews/pr194-codereview-codex-5-5.md:3850:+            if (target.getClan(clanId).baseRegion == ClanWorldConstants.REGION_FOREST) {
docs/reviews/pr194-codereview-codex-5-5.md:3862:+    function _csId(BanditSpawnHarness target, uint32 clanId, uint256 index) internal view returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:3863:+        return target.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
docs/reviews/pr194-codereview-codex-5-5.md:3876:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-5.md:3885:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-5.md:3892:+    function _submitPendingWorkerAndDefender(BanditSpawnHarness target, uint32 clanId)
docs/reviews/pr194-codereview-codex-5-5.md:3896:+        Clan memory clan = target.getClan(clanId);
docs/reviews/pr194-codereview-codex-5-5.md:3897:+        workerId = _csId(target, clanId, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3898:+        defenderId = _csId(target, clanId, 1);
docs/reviews/pr194-codereview-codex-5-5.md:3901:+            target.submitClanOrders(clanId, _ordersForPendingWorkerAndDefender(workerId, defenderId, clan.baseRegion));
docs/reviews/pr194-codereview-codex-5-5.md:3906:+    function _blockNonForestSpawnRegions(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-5.md:3907:+        uint64 currentTick = target.getWorldState().currentTick;
docs/reviews/pr194-codereview-codex-5-5.md:3910:+                target.setBanditSpawnState(region, currentTick, 0);
docs/reviews/pr194-codereview-codex-5-5.md:3915:+    function _prevrandaoForNextForestSpawn(BanditSpawnHarness target) internal view returns (bytes32) {
docs/reviews/pr194-codereview-codex-5-5.md:3916:+        WorldState memory state = target.getWorldState();
docs/reviews/pr194-codereview-codex-5-5.md:3919:+            bytes32 nextSeed = keccak256(abi.encode(nextRandao, state.currentTickSeed, state.currentTick));
docs/reviews/pr194-codereview-codex-5-5.md:3920:+            if (target.banditSpawnRoll(nextSeed, ClanWorldConstants.REGION_FOREST) < 8000) {
docs/reviews/pr194-codereview-codex-5-5.md:3952:+        world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-5.md:3956:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
docs/reviews/pr194-codereview-codex-5-5.md:3957:+        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-5.md:3966:+        world.evaluateBanditSpawns(_missSeed(ClanWorldConstants.REGION_FOREST, increment));
docs/reviews/pr194-codereview-codex-5-5.md:3968:+        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-5.md:3978:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, oneStepBelowCap);
docs/reviews/pr194-codereview-codex-5-5.md:3979:+        world.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, oneStepBelowCap);
docs/reviews/pr194-codereview-codex-5-5.md:3981:+        world.evaluateBanditSpawns(
docs/reviews/pr194-codereview-codex-5-5.md:3985:+        uint8 selectedRegion = world.getBandit(1).region;
docs/reviews/pr194-codereview-codex-5-5.md:3989:+        (, uint16 selectedAccum) = world.getBanditSpawnState(selectedRegion);
docs/reviews/pr194-codereview-codex-5-5.md:3990:+        (, uint16 otherAccum) = world.getBanditSpawnState(otherRegion);
docs/reviews/pr194-codereview-codex-5-5.md:4000:+        uint8 maxPerRegion = world.maxBanditsPerRegion();
docs/reviews/pr194-codereview-codex-5-5.md:4002:+            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
docs/reviews/pr194-codereview-codex-5-5.md:4004:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4006:+        world.evaluateBanditSpawns(keccak256("per-region-cap"));
docs/reviews/pr194-codereview-codex-5-5.md:4008:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
docs/reviews/pr194-codereview-codex-5-5.md:4015:+        uint8 maxTotal = world.maxTotalBandits();
docs/reviews/pr194-codereview-codex-5-5.md:4017:+            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
docs/reviews/pr194-codereview-codex-5-5.md:4019:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4021:+        world.evaluateBanditSpawns(keccak256("global-cap"));
docs/reviews/pr194-codereview-codex-5-5.md:4023:+        assertEq(world.activeBanditCount(), maxTotal, "global cap");
docs/reviews/pr194-codereview-codex-5-5.md:4029:+        uint8 maxTotal = world.maxTotalBandits();
docs/reviews/pr194-codereview-codex-5-5.md:4031:+            world.spawnBandit(uint8(ClanWorldConstants.REGION_FOREST + (i % 8)), 100 + i);
docs/reviews/pr194-codereview-codex-5-5.md:4033:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 4321);
docs/reviews/pr194-codereview-codex-5-5.md:4038:+        assertEq(world.activeBanditCount(), maxTotal, "still at cap");
docs/reviews/pr194-codereview-codex-5-5.md:4039:+        assertEq(state.nextBanditSpawnEligibleTick, 0, "no eligible tick while capped");
docs/reviews/pr194-codereview-codex-5-5.md:4040:+        assertEq(state.currentBanditSpawnChanceBps, 4321, "preview chance refreshed");
docs/reviews/pr194-codereview-codex-5-5.md:4043:+    function test_heartbeatCompletesWhenClanCountExceedsBanditSpawnScanCap() public {
docs/reviews/pr194-codereview-codex-5-5.md:4044:+        uint256 clanCount = world.maxBanditSpawnScanPerRegion() + 1;
docs/reviews/pr194-codereview-codex-5-5.md:4053:+        assertEq(world.getWorldState().currentTick, 1, "heartbeat advanced");
docs/reviews/pr194-codereview-codex-5-5.md:4056:+    function test_heartbeatEagerSettlesCandidateRegionBasesAndDefendersBeforeBanditSpawn() public {
docs/reviews/pr194-codereview-codex-5-5.md:4064:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, world.getWorldState().currentTick, 0);
docs/reviews/pr194-codereview-codex-5-5.md:4077:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4084:+        emit BanditSpawned(1, 0, 0, 0);
docs/reviews/pr194-codereview-codex-5-5.md:4088:+        assertEq(world.getClan(clanId1).lastSettledTick, closedTick, "clan 1 eager-settled");
docs/reviews/pr194-codereview-codex-5-5.md:4089:+        assertEq(world.getClan(clanId2).lastSettledTick, closedTick, "clan 2 eager-settled");
docs/reviews/pr194-codereview-codex-5-5.md:4100:+        BanditTroop memory bandit = world.getBandit(1);
docs/reviews/pr194-codereview-codex-5-5.md:4101:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
docs/reviews/pr194-codereview-codex-5-5.md:4102:+        assertEq(bandit.region, ClanWorldConstants.REGION_FOREST, "forest selected after eager settle");
docs/reviews/pr194-codereview-codex-5-5.md:4107:+        BanditSpawnHarness a = new BanditSpawnHarness();
docs/reviews/pr194-codereview-codex-5-5.md:4108:+        BanditSpawnHarness b = new BanditSpawnHarness();
docs/reviews/pr194-codereview-codex-5-5.md:4115:+        a.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4116:+        a.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4117:+        b.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4118:+        b.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4121:+        a.evaluateBanditSpawns(seed);
docs/reviews/pr194-codereview-codex-5-5.md:4122:+        b.evaluateBanditSpawns(seed);
docs/reviews/pr194-codereview-codex-5-5.md:4124:+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
docs/reviews/pr194-codereview-codex-5-5.md:4136:+    function test_spawnedBanditEntersStateMachine() public {
docs/reviews/pr194-codereview-codex-5-5.md:4139:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-5.md:4141:+        world.evaluateBanditSpawns(keccak256("spawn-state-machine"));
docs/reviews/pr194-codereview-codex-5-5.md:4143:+        BanditTroop memory bandit = world.getBandit(1);
docs/reviews/pr194-codereview-codex-5-5.md:4145:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
docs/reviews/pr194-codereview-codex-5-5.md:4147:+        assertEq(state.activeBanditId, bandit.id, "active bandit set");
docs/reviews/pr194-codereview-codex-5-5.md:4155:         world.heartbeat();
docs/reviews/pr194-codereview-codex-5-5.md:4219:+        assertEq(world.quoteLootValueSettled(clanId), 20e18, "settled loot quote uses simulated vault");
docs/reviews/pr194-codereview-codex-5-5.md:4220:+        assertEq(world.quoteLootValueRaw(clanId), 44e18, "raw loot quote remains committed storage");
docs/reviews/pr194-codereview-codex-5-5.md:4261:+        // Advance until the clan is truly >200 ticks behind. Phase 9 bandit
docs/reviews/pr194-codereview-codex-5-5.md:4262:+        // eager-settle may settle candidate-region bases on spawn ticks, so
docs/reviews/pr194-codereview-codex-5-5.md:4263:+        // this test follows the invariant instead of assuming no heartbeat
docs/reviews/pr194-codereview-codex-5-5.md:4278:+import {BanditState, BanditTroop, ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
docs/reviews/pr194-codereview-codex-5-5.md:4286:+    function test_getBanditMissingIdReturnsNoneState() public view {
docs/reviews/pr194-codereview-codex-5-5.md:4287:+        BanditTroop memory bandit = stub.getBandit(999);
docs/reviews/pr194-codereview-codex-5-5.md:4290:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
docs/reviews/pr194-codereview-codex-5-5.md:4341:+        observedSeed = state.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4391:+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
docs/reviews/pr194-codereview-codex-5-5.md:4405:+            address(blueprintToken)
docs/reviews/pr194-codereview-codex-5-5.md:4426:     // test_heartbeat_settlementBeforeMarket
docs/reviews/pr194-codereview-codex-5-5.md:4429:         assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
docs/reviews/pr194-codereview-codex-5-5.md:4432:+    function test_heartbeat_scheduledMarketObservesClosedTickSeedBeforeIncrement() public {
docs/reviews/pr194-codereview-codex-5-5.md:4446:+        bytes32 seedForClosedTick = beforeClose.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4455:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/pr194-codereview-codex-5-5.md:4456:+        assertNotEq(afterClose.currentTickSeed, seedForClosedTick, "next tick seed publishes after close-tick work");
docs/reviews/pr194-codereview-codex-5-5.md:4460:     // test_heartbeat_seasonTransition
docs/reviews/pr194-codereview-codex-5-5.md:4465:I’ll use the GitHub review workflow shape here, plus parallel repo reads and subagents, since this is explicitly a final integrated review. I’m going to trace heartbeat, bandit state transitions, spawn targeting, attack resolution, and ABI/test seams against the actual files so the findings land with real line references.
docs/reviews/pr194-codereview-codex-5-5.md:4467:/bin/bash -lc "pwd && rg -n \"function heartbeat|function _evaluateBanditSpawns|function _resolveBanditAttack|function _transitionBanditState|function _eagerSettleForBandits|function _simulateSettleToTick|function _submit|enum BanditState|struct BanditTroop|function getBandit|event BanditAttackResolved|BanditState\\.NONE|BanditState\\.None|_tickSeeds\\[|currentTickSeed\" packages/contracts/src packages/contracts/test apps packages -g '"'!packages/contracts/abi/IClanWorld.json'"'" in /home/claude/code/clan-world
docs/reviews/pr194-codereview-codex-5-5.md:4471:packages/contracts/src/ClanWorld.sol:724:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-5.md:4473:packages/contracts/src/ClanWorld.sol:733:        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4474:packages/contracts/src/ClanWorld.sol:741:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:4475:packages/contracts/src/ClanWorld.sol:1023:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
docs/reviews/pr194-codereview-codex-5-5.md:4476:packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:4477:packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4478:packages/contracts/src/ClanWorld.sol:1557:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:4479:packages/contracts/src/ClanWorld.sol:1627:            currentTickSeed: _world.currentTickSeed,
docs/reviews/pr194-codereview-codex-5-5.md:4480:packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4481:packages/contracts/src/IClanWorld.sol:106:enum BanditState {
docs/reviews/pr194-codereview-codex-5-5.md:4482:packages/contracts/src/IClanWorld.sol:195:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4483:packages/contracts/src/IClanWorld.sol:295:struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-5.md:4484:packages/contracts/src/IClanWorld.sol:411:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4485:packages/contracts/src/IClanWorld.sol:604:    event BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-5.md:4486:packages/contracts/src/IClanWorld.sol:667:    function heartbeat() external;
docs/reviews/pr194-codereview-codex-5-5.md:4487:packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-5.md:4488:packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
docs/reviews/pr194-codereview-codex-5-5.md:4489:packages/contracts/src/ClanWorldStub.sol:70:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-5.md:4490:packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:4491:packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4492:packages/contracts/src/ClanWorldStub.sol:248:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:4493:packages/contracts/src/ClanWorldStub.sol:278:            currentTickSeed: bytes32(0),
docs/reviews/pr194-codereview-codex-5-5.md:4494:packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4496:packages/contracts/test/ClanWorld.t.sol:165:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4497:packages/contracts/test/ClanWorld.t.sol:171:        bytes32 seedAfter = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4499:packages/shared/src/adapters/IChainClient.ts:43:          { name: 'currentTickSeed', type: 'bytes32' },
docs/reviews/pr194-codereview-codex-5-5.md:4501:packages/contracts/src/ClanWorld.sol:724:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-5.md:4503:packages/contracts/src/ClanWorld.sol:733:        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4504:packages/contracts/src/ClanWorld.sol:741:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
docs/reviews/pr194-codereview-codex-5-5.md:4505:packages/contracts/src/ClanWorld.sol:1023:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
docs/reviews/pr194-codereview-codex-5-5.md:4506:packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:4507:packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4508:packages/contracts/src/ClanWorld.sol:1557:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:4509:packages/contracts/src/ClanWorld.sol:1627:            currentTickSeed: _world.currentTickSeed,
docs/reviews/pr194-codereview-codex-5-5.md:4510:packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4511:packages/contracts/src/IClanWorld.sol:106:enum BanditState {
docs/reviews/pr194-codereview-codex-5-5.md:4512:packages/contracts/src/IClanWorld.sol:195:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4513:packages/contracts/src/IClanWorld.sol:295:struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-5.md:4514:packages/contracts/src/IClanWorld.sol:411:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4515:packages/contracts/src/IClanWorld.sol:604:    event BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-5.md:4516:packages/contracts/src/IClanWorld.sol:667:    function heartbeat() external;
docs/reviews/pr194-codereview-codex-5-5.md:4517:packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-5.md:4518:packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
docs/reviews/pr194-codereview-codex-5-5.md:4519:packages/contracts/src/ClanWorldStub.sol:70:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-5.md:4520:packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-5.md:4521:packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4522:packages/contracts/src/ClanWorldStub.sol:248:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-5.md:4523:packages/contracts/src/ClanWorldStub.sol:278:            currentTickSeed: bytes32(0),
docs/reviews/pr194-codereview-codex-5-5.md:4524:packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-5.md:4526:packages/contracts/test/ClanWorld.t.sol:165:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4527:packages/contracts/test/ClanWorld.t.sol:171:        bytes32 seedAfter = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:4549:    10	    BanditState,
docs/reviews/pr194-codereview-codex-5-5.md:4561:    22	    BanditTroop,
docs/reviews/pr194-codereview-codex-5-5.md:4576:    37	    ActiveBanditView,
docs/reviews/pr194-codereview-codex-5-5.md:4600:    61	    mapping(uint32 => uint32[]) private _incomingDefenders;    // targetClanId => clansmanIds
docs/reviews/pr194-codereview-codex-5-5.md:4601:    62	    mapping(uint32 => uint32) private _clanDefendingBase;      // clansmanId => targetClanId
docs/reviews/pr194-codereview-codex-5-5.md:4832:   272	                    cs.currentRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-5.md:4833:   273	                    emit WorkerArrived(clanId, csId, m.targetRegion, tick);
docs/reviews/pr194-codereview-codex-5-5.md:4913:   353	            if (_clanDefendingBase[cs.clansmanId] != m.targetClanId) {
docs/reviews/pr194-codereview-codex-5-5.md:4914:   354	                _incomingDefenders[m.targetClanId].push(cs.clansmanId);
docs/reviews/pr194-codereview-codex-5-5.md:4915:   355	                _clanDefendingBase[cs.clansmanId] = m.targetClanId;
docs/reviews/pr194-codereview-codex-5-5.md:4927:   367	            // (Actual execution happened or will happen at heartbeat.)
docs/reviews/pr194-codereview-codex-5-5.md:5041:   106	enum BanditState {
docs/reviews/pr194-codereview-codex-5-5.md:5128:   193	    uint64 nextBanditSpawnEligibleTick;
docs/reviews/pr194-codereview-codex-5-5.md:5129:   194	    uint16 currentBanditSpawnChanceBps;
docs/reviews/pr194-codereview-codex-5-5.md:5130:   195	    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-5.md:5132:   197	    uint32 activeBanditId;       // 0 if none
docs/reviews/pr194-codereview-codex-5-5.md:5151:   216	    address blueprintToken;
docs/reviews/pr194-codereview-codex-5-5.md:5177:   242	    uint256 blueprintBalance;
docs/reviews/pr194-codereview-codex-5-5.md:5214:   279	    uint8 targetRegion;
docs/reviews/pr194-codereview-codex-5-5.md:5224:   289	    uint32 targetClanId;   // DefendBase only
docs/reviews/pr194-codereview-codex-5-5.md:5230:   295	struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-5.md:5232:   297	    BanditState state;
docs/reviews/pr194-codereview-codex-5-4.md:19:PR: Phase 9 — Bandit System
docs/reviews/pr194-codereview-codex-5-4.md:23:Phase 9 release branch. Bundles all phase-9 sub-issues (bandit threat).
docs/reviews/pr194-codereview-codex-5-4.md:26:- #186 Phase 9.1 — Bandit troop state machine
docs/reviews/pr194-codereview-codex-5-4.md:27:- #187 Phase 9.2 — Spawn chance logic (PR #191)
docs/reviews/pr194-codereview-codex-5-4.md:30:- 9.3 — Eager-settle scope (settle bases + defenders before target selection)
docs/reviews/pr194-codereview-codex-5-4.md:34:- 9.7 — Cleanup on target death
docs/reviews/pr194-codereview-codex-5-4.md:37:- Phase 9 intentionally redesigns the v1-prep bandit ABI: `BanditState` enum order/insertion and `BanditTroop` layout changed. ABI consumers must regenerate from the updated contract ABI.
docs/reviews/pr194-codereview-codex-5-4.md:61:## HIGH severity findings
docs/reviews/pr194-codereview-codex-5-4.md:64:## MEDIUM severity findings
docs/reviews/pr194-codereview-codex-5-4.md:67:## LOW severity findings
docs/reviews/pr194-codereview-codex-5-4.md:71:(things that don't fit the per-finding format — patterns across the diff, architectural drift, suggested refactors)
docs/reviews/pr194-codereview-codex-5-4.md:73:If clean, say "CLEAN — no findings" under each section.
docs/reviews/pr194-codereview-codex-5-4.md:75:DIFF FOLLOWS BELOW.
docs/reviews/pr194-codereview-codex-5-4.md:106:       "name": "getActiveBanditView",
docs/reviews/pr194-codereview-codex-5-4.md:112:+      "name": "getBandit",
docs/reviews/pr194-codereview-codex-5-4.md:185:+          "internalType": "struct BanditTroop",
docs/reviews/pr194-codereview-codex-5-4.md:200:+              "internalType": "enum BanditState"
docs/reviews/pr194-codereview-codex-5-4.md:203:+              "name": "targetClanId",
docs/reviews/pr194-codereview-codex-5-4.md:251:           "internalType": "struct BanditTroop",
docs/reviews/pr194-codereview-codex-5-4.md:262:-              "internalType": "enum BanditState"
docs/reviews/pr194-codereview-codex-5-4.md:275:+              "internalType": "enum BanditState"
docs/reviews/pr194-codereview-codex-5-4.md:281:+              "name": "targetClanId",
docs/reviews/pr194-codereview-codex-5-4.md:323:+      "name": "getBanditsInRegion",
docs/reviews/pr194-codereview-codex-5-4.md:475:+      "name": "BanditTargetDied",
docs/reviews/pr194-codereview-codex-5-4.md:539:+      "name": "ClansmanKilledByBandit",
docs/reviews/pr194-codereview-codex-5-4.md:657:+      "name": "WallDamagedByBandit",
docs/reviews/pr194-codereview-codex-5-4.md:688:     ActiveBanditView,
docs/reviews/pr194-codereview-codex-5-4.md:699:+    mapping(uint32 => BanditTroop) internal _bandits;
docs/reviews/pr194-codereview-codex-5-4.md:701:+    mapping(uint8 => BanditSpawnState) internal _banditSpawnByRegion;
docs/reviews/pr194-codereview-codex-5-4.md:706:+    uint32 internal _nextBanditId;
docs/reviews/pr194-codereview-codex-5-4.md:707:+    uint32 internal _activeBanditCount;
docs/reviews/pr194-codereview-codex-5-4.md:716:     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
docs/reviews/pr194-codereview-codex-5-4.md:725:+    /// @dev Bandit spawn weights are a heartbeat-time heuristic. V1 has
docs/reviews/pr194-codereview-codex-5-4.md:727:+    ///      at most two rotating heartbeats while keeping heartbeat gas bounded.
docs/reviews/pr194-codereview-codex-5-4.md:732:+    ///      so this settles all possible bases today while keeping the heartbeat
docs/reviews/pr194-codereview-codex-5-4.md:744:+    struct BanditSpawnState {
docs/reviews/pr194-codereview-codex-5-4.md:759:         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
docs/reviews/pr194-codereview-codex-5-4.md:770:+        _nextBanditId = 1;
docs/reviews/pr194-codereview-codex-5-4.md:834:+    function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId) internal {
docs/reviews/pr194-codereview-codex-5-4.md:862:+        _abortBanditAttacksForDeadTarget(clanId, excludedBanditId);
docs/reviews/pr194-codereview-codex-5-4.md:877:+                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
docs/reviews/pr194-codereview-codex-5-4.md:892:+    function _abortBanditAttacksForDeadTarget(uint32 deadClanId, uint32 excludedBanditId) internal {
docs/reviews/pr194-codereview-codex-5-4.md:893:+        // Match _transitionBanditState's event stamp; heartbeat keeps currentTick
docs/reviews/pr194-codereview-codex-5-4.md:897:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:898:+            for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-4.md:899:+                uint32 banditId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-4.md:900:+                if (banditId == excludedBanditId) continue;
docs/reviews/pr194-codereview-codex-5-4.md:902:+                BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-4.md:903:+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
docs/reviews/pr194-codereview-codex-5-4.md:904:+                    _transitionBanditState(banditId, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-4.md:905:+                    emit BanditEscaped(banditId, currentTick);
docs/reviews/pr194-codereview-codex-5-4.md:906:+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
docs/reviews/pr194-codereview-codex-5-4.md:1037:+        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:1065:+            blueprintCost = 1e18;
docs/reviews/pr194-codereview-codex-5-4.md:1070:+                || clan.blueprintBalance < blueprintCost
docs/reviews/pr194-codereview-codex-5-4.md:1076:+        clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:1239:+                cs.currentRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:1378:+        if (m.targetRegion == ClanWorldConstants.REGION_WEST_FARMS) {
docs/reviews/pr194-codereview-codex-5-4.md:1380:+        } else if (m.targetRegion == ClanWorldConstants.REGION_EAST_FARMS) {
docs/reviews/pr194-codereview-codex-5-4.md:1519:+        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:1546:+            blueprintCost = 1e18;
docs/reviews/pr194-codereview-codex-5-4.md:1551:+                || sim.clan.blueprintBalance < blueprintCost
docs/reviews/pr194-codereview-codex-5-4.md:1557:+        sim.clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:1576:+    function _spawnBandit(uint8 region, uint32 strength) internal returns (uint32 id) {
docs/reviews/pr194-codereview-codex-5-4.md:1583:+        id = _nextBanditId++;
docs/reviews/pr194-codereview-codex-5-4.md:1584:+        _bandits[id] = BanditTroop({
docs/reviews/pr194-codereview-codex-5-4.md:1587:+            state: BanditState.Spawned,
docs/reviews/pr194-codereview-codex-5-4.md:1588:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-4.md:1598:+        _activeBanditCount += 1;
docs/reviews/pr194-codereview-codex-5-4.md:1600:+        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:1604:+        if (_world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
docs/reviews/pr194-codereview-codex-5-4.md:1605:+            _world.activeBanditId = id;
docs/reviews/pr194-codereview-codex-5-4.md:1608:+        emit BanditSpawned(id, region, 0, _banditStrengthForLegacyEvent(strength));
docs/reviews/pr194-codereview-codex-5-4.md:1611:+    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1612:+        require(targetClanId != ClanWorldConstants.CLAN_ID_NULL, "ClanWorld: invalid bandit target");
docs/reviews/pr194-codereview-codex-5-4.md:1613:+        _bandits[id].targetClanId = targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:1614:+        _transitionBanditState(id, BanditState.Attacking);
docs/reviews/pr194-codereview-codex-5-4.md:1617:+    function _transitionBanditState(uint32 id, BanditState newState) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1618:+        BanditTroop storage bandit = _bandits[id];
docs/reviews/pr194-codereview-codex-5-4.md:1620:+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
docs/reviews/pr194-codereview-codex-5-4.md:1622:+        BanditState oldState = bandit.state;
docs/reviews/pr194-codereview-codex-5-4.md:1623:+        require(_isValidBanditTransition(bandit, newState), "ClanWorld: invalid bandit transition");
docs/reviews/pr194-codereview-codex-5-4.md:1625:+        if (newState == BanditState.Defeated) {
docs/reviews/pr194-codereview-codex-5-4.md:1626:+            emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
docs/reviews/pr194-codereview-codex-5-4.md:1627:+            _deleteBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:1633:+        if (newState != BanditState.Attacking) {
docs/reviews/pr194-codereview-codex-5-4.md:1634:+            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
docs/reviews/pr194-codereview-codex-5-4.md:1637:+        emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
docs/reviews/pr194-codereview-codex-5-4.md:1640:+    function _isValidBanditTransition(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:1641:+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
docs/reviews/pr194-codereview-codex-5-4.md:1642:+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
docs/reviews/pr194-codereview-codex-5-4.md:1643:+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
docs/reviews/pr194-codereview-codex-5-4.md:1644:+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
docs/reviews/pr194-codereview-codex-5-4.md:1645:+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
docs/reviews/pr194-codereview-codex-5-4.md:1649:+    function _canBanditLeaveSpawned(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:1650:+        return newState == BanditState.Escaped
docs/reviews/pr194-codereview-codex-5-4.md:1651:+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
docs/reviews/pr194-codereview-codex-5-4.md:1654:+    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:1655:+        return newState == BanditState.Escaped
docs/reviews/pr194-codereview-codex-5-4.md:1656:+            || (newState == BanditState.Attacking
docs/reviews/pr194-codereview-codex-5-4.md:1657:+                && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
docs/reviews/pr194-codereview-codex-5-4.md:1661:+    function _canBanditLeaveAttacking(BanditState newState) internal pure returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:1662:+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
docs/reviews/pr194-codereview-codex-5-4.md:1665:+    function _canBanditLeaveEscaped(BanditState newState) internal pure returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:1666:+        return newState == BanditState.Resting;
docs/reviews/pr194-codereview-codex-5-4.md:1669:+    function _canBanditLeaveResting(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:1670:+        return newState == BanditState.Escaped
docs/reviews/pr194-codereview-codex-5-4.md:1671:+            || (newState == BanditState.Camped
docs/reviews/pr194-codereview-codex-5-4.md:1675:+    function _deleteBandit(uint32 id) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1676:+        BanditTroop storage bandit = _bandits[id];
docs/reviews/pr194-codereview-codex-5-4.md:1678:+        uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:1679:+        for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-4.md:1680:+            if (regionBandits[i] == id) {
docs/reviews/pr194-codereview-codex-5-4.md:1681:+                regionBandits[i] = regionBandits[regionBandits.length - 1];
docs/reviews/pr194-codereview-codex-5-4.md:1682:+                regionBandits.pop();
docs/reviews/pr194-codereview-codex-5-4.md:1688:+        if (_activeBanditCount > 0) {
docs/reviews/pr194-codereview-codex-5-4.md:1689:+            _activeBanditCount -= 1;
docs/reviews/pr194-codereview-codex-5-4.md:1691:+        if (_world.activeBanditId == id) {
docs/reviews/pr194-codereview-codex-5-4.md:1692:+            _world.activeBanditId = _findOldestActiveBandit();
docs/reviews/pr194-codereview-codex-5-4.md:1696:+    function _findOldestActiveBandit() internal view returns (uint32 oldestBanditId) {
docs/reviews/pr194-codereview-codex-5-4.md:1700:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:1701:+            for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-4.md:1702:+                uint32 candidateId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-4.md:1703:+                BanditTroop storage candidate = _bandits[candidateId];
docs/reviews/pr194-codereview-codex-5-4.md:1704:+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
docs/reviews/pr194-codereview-codex-5-4.md:1707:+                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
docs/reviews/pr194-codereview-codex-5-4.md:1708:+                    oldestBanditId = candidateId;
docs/reviews/pr194-codereview-codex-5-4.md:1714:+    function _advanceBanditStates(uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1717:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:1718:+            for (uint256 i = 0; i < regionBandits.length; i++) {
docs/reviews/pr194-codereview-codex-5-4.md:1719:+                uint32 banditId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-4.md:1720:+                BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-4.md:1721:+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
docs/reviews/pr194-codereview-codex-5-4.md:1722:+                    _transitionBanditState(banditId, BanditState.Camped);
docs/reviews/pr194-codereview-codex-5-4.md:1724:+                    bandit.state == BanditState.Resting
docs/reviews/pr194-codereview-codex-5-4.md:1727:+                    _transitionBanditState(banditId, BanditState.Camped);
docs/reviews/pr194-codereview-codex-5-4.md:1739:+    function _resolveAttackingBandits(uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1741:+            uint32[] storage regionBandits = _banditsByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:1743:+            while (i < regionBandits.length) {
docs/reviews/pr194-codereview-codex-5-4.md:1744:+                uint32 banditId = regionBandits[i];
docs/reviews/pr194-codereview-codex-5-4.md:1745:+                BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-4.md:1746:+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
docs/reviews/pr194-codereview-codex-5-4.md:1748:+                    _resolveBanditAttack(banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:1758:+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1761:+        BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-4.md:1762:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
docs/reviews/pr194-codereview-codex-5-4.md:1769:+        uint32 targetClanId = bandit.targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:1770:+        Clan storage targetClan = _clans[targetClanId];
docs/reviews/pr194-codereview-codex-5-4.md:1771:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-codereview-codex-5-4.md:1772:+            _transitionBanditState(banditId, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-4.md:1773:+            emit BanditEscaped(banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:1777:+        _settleClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:1778:+        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
docs/reviews/pr194-codereview-codex-5-4.md:1780:+        bytes32 tickSeed = _world.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:1782:+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:1791:+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
docs/reviews/pr194-codereview-codex-5-4.md:1792:+            (incomingDamage, baseAbsorbed) = _applyBanditBaseDefense(targetClan, incomingDamage);
docs/reviews/pr194-codereview-codex-5-4.md:1794:+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:1798:+        emit BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-4.md:1800:+            targetClanId,
docs/reviews/pr194-codereview-codex-5-4.md:1804:+            targetClan.wallLevel,
docs/reviews/pr194-codereview-codex-5-4.md:1813:+            emit BanditDefeated(banditId, targetClanId, closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:1814:+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:1815:+            targetClan.blueprintBalance += 1;
docs/reviews/pr194-codereview-codex-5-4.md:1816:+            emit BlueprintEarned(targetClanId, banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:1817:+            _transitionBanditState(banditId, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-4.md:1819:+            _transitionBanditState(banditId, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-4.md:1820:+            emit BanditEscaped(banditId, closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:1824:+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
docs/reviews/pr194-codereview-codex-5-4.md:1825:+        BanditTroop storage bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-4.md:1826:+        uint32[] memory rewardedClanIds = _activeDefendingClanIds(targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:1835:+            perWood = _perClanBanditLootShare(bandit.carryWood, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-4.md:1836:+            perIron = _perClanBanditLootShare(bandit.carryIron, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-4.md:1837:+            perWheat = _perClanBanditLootShare(bandit.carryWheat, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-4.md:1838:+            perFish = _perClanBanditLootShare(bandit.carryFish, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-4.md:1839:+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
docs/reviews/pr194-codereview-codex-5-4.md:1867:+    function _perClanBanditLootShare(uint256 loot, uint256 nDefendingClans) internal pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-4.md:1869:+            return loot;
docs/reviews/pr194-codereview-codex-5-4.md:1871:+        return ((loot / RESOURCE_UNIT) / nDefendingClans) * RESOURCE_UNIT;
docs/reviews/pr194-codereview-codex-5-4.md:1874:+    function _activeDefendingClanIds(uint32 targetClanId) internal view returns (uint32[] memory clanIds) {
docs/reviews/pr194-codereview-codex-5-4.md:1875:+        uint8 targetRegion = _clans[targetClanId].baseRegion;
docs/reviews/pr194-codereview-codex-5-4.md:1876:+        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
docs/reviews/pr194-codereview-codex-5-4.md:1880:+            if (_clanHasActiveDefenderForTarget(defendingClans[i], targetClanId, targetRegion)) {
docs/reviews/pr194-codereview-codex-5-4.md:1889:+            if (_clanHasActiveDefenderForTarget(defenderClanId, targetClanId, targetRegion)) {
docs/reviews/pr194-codereview-codex-5-4.md:1895:+    function _clanHasActiveDefenderForTarget(uint32 defenderClanId, uint32 targetClanId, uint8 targetRegion)
docs/reviews/pr194-codereview-codex-5-4.md:1911:+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
docs/reviews/pr194-codereview-codex-5-4.md:1912:+                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
docs/reviews/pr194-codereview-codex-5-4.md:1920:+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
docs/reviews/pr194-codereview-codex-5-4.md:1925:+        uint8 targetRegion = _clans[targetClanId].baseRegion;
docs/reviews/pr194-codereview-codex-5-4.md:1926:+        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
docs/reviews/pr194-codereview-codex-5-4.md:1941:+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
docs/reviews/pr194-codereview-codex-5-4.md:1942:+                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
docs/reviews/pr194-codereview-codex-5-4.md:1950:+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
docs/reviews/pr194-codereview-codex-5-4.md:1965:+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
docs/reviews/pr194-codereview-codex-5-4.md:1969:+    function _applyBanditBaseDefense(Clan storage clan, uint32 incomingDamage)
docs/reviews/pr194-codereview-codex-5-4.md:1984:+    function _applyBanditClansmanCasualties(
docs/reviews/pr194-codereview-codex-5-4.md:1994:+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:2005:+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
docs/reviews/pr194-codereview-codex-5-4.md:2015:+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
docs/reviews/pr194-codereview-codex-5-4.md:2062:+    function _evaluateBanditSpawns(bytes32 tickSeed) internal {
docs/reviews/pr194-codereview-codex-5-4.md:2064:+        if (_activeBanditCount >= MAX_TOTAL_BANDITS) {
docs/reviews/pr194-codereview-codex-5-4.md:2065:+            _refreshBanditSpawnWorldPreview(regionWeights);
docs/reviews/pr194-codereview-codex-5-4.md:2076:+            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:2081:+            spawnState.probabilityAccum = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
docs/reviews/pr194-codereview-codex-5-4.md:2089:+        uint8 selectedRegion = _selectBanditSpawnRegion(tickSeed, candidateWeights);
docs/reviews/pr194-codereview-codex-5-4.md:2091:+            // _spawnBandit resets only the selected region's accumulator; other
docs/reviews/pr194-codereview-codex-5-4.md:2093:+            _spawnBandit(selectedRegion, _banditSpawnStrength(tickSeed, selectedRegion));
docs/reviews/pr194-codereview-codex-5-4.md:2096:+        _refreshBanditSpawnWorldPreview(regionWeights);
docs/reviews/pr194-codereview-codex-5-4.md:2111:+    function _incrementBanditSpawnProbability(uint16 probabilityAccum) internal pure returns (uint16) {
docs/reviews/pr194-codereview-codex-5-4.md:2143:+    function _selectBanditSpawnRegion(bytes32 tickSeed, uint256[] memory weights) internal pure returns (uint8) {
docs/reviews/pr194-codereview-codex-5-4.md:2167:+    function _eagerSettleForBandits(uint64 closedTick) internal {
docs/reviews/pr194-codereview-codex-5-4.md:2168:+        require(_world.currentTick == closedTick, "ClanWorld: eager settle tick mismatch");
docs/reviews/pr194-codereview-codex-5-4.md:2169:+        if (_activeBanditCount >= MAX_TOTAL_BANDITS) return;
docs/reviews/pr194-codereview-codex-5-4.md:2188:+            if (!_isBanditSpawnRegionCandidate(regionWeights, region)) {
docs/reviews/pr194-codereview-codex-5-4.md:2191:+            _eagerSettleBanditCandidateRegion(region);
docs/reviews/pr194-codereview-codex-5-4.md:2196:+    function _isBanditSpawnRegionCandidate(uint256[] memory regionWeights, uint8 region) internal view returns (bool) {
docs/reviews/pr194-codereview-codex-5-4.md:2207:+        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:2212:+        uint16 nextProbability = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
docs/reviews/pr194-codereview-codex-5-4.md:2213:+        return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
docs/reviews/pr194-codereview-codex-5-4.md:2219:+    function _eagerSettleBanditCandidateRegion(uint8 region) internal {
docs/reviews/pr194-codereview-codex-5-4.md:2251:+            _eagerSettleActiveDefendersForBase(clanId, region);
docs/reviews/pr194-codereview-codex-5-4.md:2256:+    function _eagerSettleActiveDefendersForBase(uint32 targetClanId, uint8 region) internal {
docs/reviews/pr194-codereview-codex-5-4.md:2285:+                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
docs/reviews/pr194-codereview-codex-5-4.md:2294:-        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:2327:-            blueprintCost = 1e18;
docs/reviews/pr194-codereview-codex-5-4.md:2337:-                || clan.blueprintBalance < blueprintCost
docs/reviews/pr194-codereview-codex-5-4.md:2351:-        clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:2356:+                weights[clan.baseRegion - 1] += 100 + (_lootValueRaw(clan) / 1e18);
docs/reviews/pr194-codereview-codex-5-4.md:2387:+    function _refreshBanditSpawnWorldPreview(uint256[] memory regionWeights) internal {
docs/reviews/pr194-codereview-codex-5-4.md:2392:+            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
docs/reviews/pr194-codereview-codex-5-4.md:2395:+                _activeBanditCount < MAX_TOTAL_BANDITS && regionWeights[region - 1] > 0
docs/reviews/pr194-codereview-codex-5-4.md:2405:+        _world.nextBanditSpawnEligibleTick = nextEligibleTick == type(uint64).max ? 0 : nextEligibleTick;
docs/reviews/pr194-codereview-codex-5-4.md:2406:+        _world.currentBanditSpawnChanceBps = maxChance;
docs/reviews/pr194-codereview-codex-5-4.md:2411:     /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
docs/reviews/pr194-codereview-codex-5-4.md:2421:+    ///         3. Eager-settle clans touched by world events (Phase 9 stub).
docs/reviews/pr194-codereview-codex-5-4.md:2424:     function heartbeat() external override nonReentrant {
docs/reviews/pr194-codereview-codex-5-4.md:2425:         require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
docs/reviews/pr194-codereview-codex-5-4.md:2428:+        bytes32 closedTickSeed = _world.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:2434:-        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
docs/reviews/pr194-codereview-codex-5-4.md:2436:-        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-4.md:2446:-        // TODO Phase 3: _settleClansNearBandit(closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:2448:+        _eagerSettleForBandits(closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:2452:+        _advanceBanditStates(closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:2455:+        _resolveAttackingBandits(closedTick);
docs/reviews/pr194-codereview-codex-5-4.md:2458:+        _evaluateBanditSpawns(closedTickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:2469:+        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-4.md:2483:         ctx.targetClanId =
docs/reviews/pr194-codereview-codex-5-4.md:2484:             order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:2496:-        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
docs/reviews/pr194-codereview-codex-5-4.md:2497:+        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:2498:+        Clan storage targetClan = _clans[targetClanId];
docs/reviews/pr194-codereview-codex-5-4.md:2499:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-codereview-codex-5-4.md:2502:+        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
docs/reviews/pr194-codereview-codex-5-4.md:2510:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2511:-        return BanditTroop({
docs/reviews/pr194-codereview-codex-5-4.md:2513:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-4.md:2525:+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2526:+        BanditTroop memory bandit = _bandits[banditId];
docs/reviews/pr194-codereview-codex-5-4.md:2527:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
docs/reviews/pr194-codereview-codex-5-4.md:2528:+            return BanditTroop({
docs/reviews/pr194-codereview-codex-5-4.md:2531:+                state: BanditState.None,
docs/reviews/pr194-codereview-codex-5-4.md:2532:+                targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-4.md:2545:+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2546:+        return getBandit(banditId);
docs/reviews/pr194-codereview-codex-5-4.md:2549:+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2563:-        uint256 lootVal = _lootValueRaw(clan);
docs/reviews/pr194-codereview-codex-5-4.md:2565:-            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
docs/reviews/pr194-codereview-codex-5-4.md:2575:-            // Simplified: if past arrivalTick, they're at target; else at start
docs/reviews/pr194-codereview-codex-5-4.md:2577:-                effectiveRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:2611:-    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:2613:+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:2614:+        return _bandits[banditId].targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:2623:-        return _lootValueRaw(_clans[clanId]);
docs/reviews/pr194-codereview-codex-5-4.md:2625:+        return _lootValueRaw(sim.clan);
docs/reviews/pr194-codereview-codex-5-4.md:2628:     /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
docs/reviews/pr194-codereview-codex-5-4.md:2636:+            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
docs/reviews/pr194-codereview-codex-5-4.md:2654:+            return tick >= m.arrivalTick ? m.targetRegion : m.startRegion;
docs/reviews/pr194-codereview-codex-5-4.md:2672:-        uint256 lootVal = _lootValueRaw(clan);
docs/reviews/pr194-codereview-codex-5-4.md:2675:-            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
docs/reviews/pr194-codereview-codex-5-4.md:2687:-                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
docs/reviews/pr194-codereview-codex-5-4.md:2714:+                thisClanDefendingBaseId = sim.missions[i].targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:2745:-    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2746:+    function getActiveBanditView() external view override returns (ActiveBanditView memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2747:+        BanditTroop memory bandit = _bandits[_world.activeBanditId];
docs/reviews/pr194-codereview-codex-5-4.md:2749:+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
docs/reviews/pr194-codereview-codex-5-4.md:2751:+            if (bandit.state == BanditState.Spawned) {
docs/reviews/pr194-codereview-codex-5-4.md:2753:+            } else if (bandit.state == BanditState.Camped) {
docs/reviews/pr194-codereview-codex-5-4.md:2755:+            } else if (bandit.state == BanditState.Resting) {
docs/reviews/pr194-codereview-codex-5-4.md:2760:         return ActiveBanditView({
docs/reviews/pr194-codereview-codex-5-4.md:2763:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-4.md:2787:+            projectedTargetClanId: bandit.targetClanId,
docs/reviews/pr194-codereview-codex-5-4.md:2799:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2800:+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2801:         return BanditTroop({
docs/reviews/pr194-codereview-codex-5-4.md:2803:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-4.md:2812:+            state: BanditState.None,
docs/reviews/pr194-codereview-codex-5-4.md:2813:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-4.md:2825:+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2826:+        return getBandit(banditId);
docs/reviews/pr194-codereview-codex-5-4.md:2829:+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
docs/reviews/pr194-codereview-codex-5-4.md:2837:         return ActiveBanditView({
docs/reviews/pr194-codereview-codex-5-4.md:2840:-            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-4.md:2841:+            state: BanditState.None,
docs/reviews/pr194-codereview-codex-5-4.md:2853:+// v1 ABI: Bandit state machine redesigned in Phase 9. ABI consumers must regenerate.
docs/reviews/pr194-codereview-codex-5-4.md:2854: enum BanditState {
docs/reviews/pr194-codereview-codex-5-4.md:2876:-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
docs/reviews/pr194-codereview-codex-5-4.md:2878:+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
docs/reviews/pr194-codereview-codex-5-4.md:2881:     uint64 nextBanditSpawnEligibleTick;
docs/reviews/pr194-codereview-codex-5-4.md:2886:+// v1 ABI: Bandit troop layout redesigned in Phase 9. ABI consumers must regenerate.
docs/reviews/pr194-codereview-codex-5-4.md:2887: struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-4.md:2891:     BanditState state;
docs/reviews/pr194-codereview-codex-5-4.md:2901:+    uint32 targetClanId; // 0 if not attacking
docs/reviews/pr194-codereview-codex-5-4.md:2914:     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-4.md:2915:     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-4.md:2916:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
docs/reviews/pr194-codereview-codex-5-4.md:2917:+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
docs/reviews/pr194-codereview-codex-5-4.md:2918:+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
docs/reviews/pr194-codereview-codex-5-4.md:2942:+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-4.md:2944:     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-4.md:2946:+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
docs/reviews/pr194-codereview-codex-5-4.md:2951:diff --git a/packages/contracts/test/Bandit.t.sol b/packages/contracts/test/Bandit.t.sol
docs/reviews/pr194-codereview-codex-5-4.md:2955:+++ b/packages/contracts/test/Bandit.t.sol
docs/reviews/pr194-codereview-codex-5-4.md:2962:+import {ActiveBanditView, ClanWorldConstants, BanditState, BanditTroop, WorldState} from "../src/IClanWorld.sol";
docs/reviews/pr194-codereview-codex-5-4.md:2964:+contract BanditHarness is ClanWorld {
docs/reviews/pr194-codereview-codex-5-4.md:2965:+    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:2966:+        return _spawnBandit(region, strength);
docs/reviews/pr194-codereview-codex-5-4.md:2969:+    function transitionBandit(uint32 id, BanditState newState) external {
docs/reviews/pr194-codereview-codex-5-4.md:2970:+        _transitionBanditState(id, newState);
docs/reviews/pr194-codereview-codex-5-4.md:2973:+    function transitionBanditToAttacking(uint32 id, uint32 targetClanId) external {
docs/reviews/pr194-codereview-codex-5-4.md:2974:+        _transitionBanditToAttacking(id, targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:2978:+contract BanditTest is Test {
docs/reviews/pr194-codereview-codex-5-4.md:2979:+    BanditHarness world;
docs/reviews/pr194-codereview-codex-5-4.md:2982:+        world = new BanditHarness();
docs/reviews/pr194-codereview-codex-5-4.md:2987:+        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:2997:+        id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3001:+    function _spawnCampAndAttack(uint32 targetClanId) internal returns (uint32 id) {
docs/reviews/pr194-codereview-codex-5-4.md:3004:+        world.transitionBanditToAttacking(id, targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:3007:+    function test_spawnBandit_recordsSpawnedTroopAndRegionIndex() public {
docs/reviews/pr194-codereview-codex-5-4.md:3008:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 250);
docs/reviews/pr194-codereview-codex-5-4.md:3011:+        BanditTroop memory bandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3014:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
docs/reviews/pr194-codereview-codex-5-4.md:3015:+        assertEq(bandit.targetClanId, 0, "no target while spawned");
docs/reviews/pr194-codereview-codex-5-4.md:3019:+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
docs/reviews/pr194-codereview-codex-5-4.md:3020:+        assertEq(regionBandits.length, 1, "region index length");
docs/reviews/pr194-codereview-codex-5-4.md:3021:+        assertEq(regionBandits[0], id, "region index id");
docs/reviews/pr194-codereview-codex-5-4.md:3024:+        assertEq(state.activeBanditId, id, "active bandit");
docs/reviews/pr194-codereview-codex-5-4.md:3027:+    function test_getBanditMissingIdReturnsNoneState() public view {
docs/reviews/pr194-codereview-codex-5-4.md:3028:+        BanditTroop memory bandit = world.getBandit(999);
docs/reviews/pr194-codereview-codex-5-4.md:3031:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
docs/reviews/pr194-codereview-codex-5-4.md:3034:+    function test_defaultBanditTroopStateIsNone() public pure {
docs/reviews/pr194-codereview-codex-5-4.md:3035:+        BanditTroop memory bandit;
docs/reviews/pr194-codereview-codex-5-4.md:3037:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
docs/reviews/pr194-codereview-codex-5-4.md:3041:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3045:+        BanditTroop memory bandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3046:+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
docs/reviews/pr194-codereview-codex-5-4.md:3050:+    function test_spawnedActiveBanditViewReportsNextActionTick() public {
docs/reviews/pr194-codereview-codex-5-4.md:3051:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3053:+        ActiveBanditView memory view_ = world.getActiveBanditView();
docs/reviews/pr194-codereview-codex-5-4.md:3056:+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
docs/reviews/pr194-codereview-codex-5-4.md:3064:+        world.transitionBanditToAttacking(id, 77);
docs/reviews/pr194-codereview-codex-5-4.md:3066:+        BanditTroop memory bandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3067:+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
docs/reviews/pr194-codereview-codex-5-4.md:3068:+        assertEq(bandit.targetClanId, 77, "target");
docs/reviews/pr194-codereview-codex-5-4.md:3072:+    function test_attackingToDefeatedDeletesBanditAndRegionIndex() public {
docs/reviews/pr194-codereview-codex-5-4.md:3075:+        world.transitionBandit(id, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-4.md:3077:+        BanditTroop memory deletedBandit = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3078:+        assertEq(deletedBandit.id, 0, "deleted id");
docs/reviews/pr194-codereview-codex-5-4.md:3079:+        assertEq(deletedBandit.region, 0, "deleted region");
docs/reviews/pr194-codereview-codex-5-4.md:3080:+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
docs/reviews/pr194-codereview-codex-5-4.md:3081:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
docs/reviews/pr194-codereview-codex-5-4.md:3082:+        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
docs/reviews/pr194-codereview-codex-5-4.md:3085:+    function test_defeatingActiveBanditPromotesOldestRemainingBandit() public {
docs/reviews/pr194-codereview-codex-5-4.md:3086:+        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3087:+        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 200);
docs/reviews/pr194-codereview-codex-5-4.md:3088:+        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_WEST_FARMS, 300);
docs/reviews/pr194-codereview-codex-5-4.md:3092:+        world.transitionBanditToAttacking(id1, 77);
docs/reviews/pr194-codereview-codex-5-4.md:3093:+        world.transitionBandit(id1, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-4.md:3095:+        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
docs/reviews/pr194-codereview-codex-5-4.md:3096:+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
docs/reviews/pr194-codereview-codex-5-4.md:3102:+        world.transitionBandit(id, BanditState.Escaped);
docs/reviews/pr194-codereview-codex-5-4.md:3103:+        BanditTroop memory escaped = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3104:+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
docs/reviews/pr194-codereview-codex-5-4.md:3105:+        assertEq(escaped.targetClanId, 0, "target cleared on escape");
docs/reviews/pr194-codereview-codex-5-4.md:3107:+        world.transitionBandit(id, BanditState.Resting);
docs/reviews/pr194-codereview-codex-5-4.md:3108:+        BanditTroop memory resting = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3109:+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
docs/reviews/pr194-codereview-codex-5-4.md:3114:+        BanditTroop memory camped = world.getBandit(id);
docs/reviews/pr194-codereview-codex-5-4.md:3115:+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
docs/reviews/pr194-codereview-codex-5-4.md:3116:+        assertEq(camped.targetClanId, 0, "target remains clear");
docs/reviews/pr194-codereview-codex-5-4.md:3120:+        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3123:+        world.transitionBandit(id, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-4.md:3129:+        world.transitionBandit(id, BanditState.Attacking);
docs/reviews/pr194-codereview-codex-5-4.md:3132:+        world.transitionBandit(id, BanditState.None);
docs/reviews/pr194-codereview-codex-5-4.md:3135:+    function test_getBanditsInRegionReturnsListAndUpdatesAfterDelete() public {
docs/reviews/pr194-codereview-codex-5-4.md:3136:+        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3137:+        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 200);
docs/reviews/pr194-codereview-codex-5-4.md:3138:+        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 300);
docs/reviews/pr194-codereview-codex-5-4.md:3140:+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-4.md:3141:+        assertEq(forestBandits.length, 2, "forest count");
docs/reviews/pr194-codereview-codex-5-4.md:3142:+        assertEq(forestBandits[0], id1, "first forest bandit");
docs/reviews/pr194-codereview-codex-5-4.md:3143:+        assertEq(forestBandits[1], id2, "second forest bandit");
docs/reviews/pr194-codereview-codex-5-4.md:3145:+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
docs/reviews/pr194-codereview-codex-5-4.md:3146:+        assertEq(mountainBandits.length, 1, "mountain count");
docs/reviews/pr194-codereview-codex-5-4.md:3147:+        assertEq(mountainBandits[0], id3, "mountain bandit");
docs/reviews/pr194-codereview-codex-5-4.md:3151:+        world.transitionBanditToAttacking(id1, 77);
docs/reviews/pr194-codereview-codex-5-4.md:3152:+        world.transitionBandit(id1, BanditState.Defeated);
docs/reviews/pr194-codereview-codex-5-4.md:3154:+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-4.md:3155:+        assertEq(forestBandits.length, 1, "forest count after delete");
docs/reviews/pr194-codereview-codex-5-4.md:3156:+        assertEq(forestBandits[0], id2, "remaining forest bandit");
docs/reviews/pr194-codereview-codex-5-4.md:3159:diff --git a/packages/contracts/test/BanditAttackResolution.t.sol b/packages/contracts/test/BanditAttackResolution.t.sol
docs/reviews/pr194-codereview-codex-5-4.md:3163:+++ b/packages/contracts/test/BanditAttackResolution.t.sol
docs/reviews/pr194-codereview-codex-5-4.md:3175:+    BanditState,
docs/reviews/pr194-codereview-codex-5-4.md:3184:+contract BanditAttackHarness is ClanWorld {
docs/reviews/pr194-codereview-codex-5-4.md:3185:+    function forceAttackingBandit(uint8 region, uint32 strength, uint32 targetClanId) external returns (uint32 id) {
docs/reviews/pr194-codereview-codex-5-4.md:3186:+        id = _spawnBandit(region, strength);
docs/reviews/pr194-codereview-codex-5-4.md:3187:+        _bandits[id].state = BanditState.Attacking;
docs/reviews/pr194-codereview-codex-5-4.md:3188:+        _bandits[id].targetClanId = targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:3209:+    function setBanditStrength(uint32 banditId, uint32 strength) external {
docs/reviews/pr194-codereview-codex-5-4.md:3213:+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
docs/reviews/pr194-codereview-codex-5-4.md:3228:+contract BanditAttackResolutionTest is Test {
docs/reviews/pr194-codereview-codex-5-4.md:3229:+    BanditAttackHarness world;
docs/reviews/pr194-codereview-codex-5-4.md:3232:+    event BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-4.md:3234:+        uint32 indexed targetClanId,
docs/reviews/pr194-codereview-codex-5-4.md:3245:+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-4.md:3247:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
docs/reviews/pr194-codereview-codex-5-4.md:3264:+        world = new BanditAttackHarness();
docs/reviews/pr194-codereview-codex-5-4.md:3281:+    function _defendTargetOrders(uint32 clanId, uint32 targetClanId, uint8 targetRegion, uint256 count)
docs/reviews/pr194-codereview-codex-5-4.md:3290:+                gotoRegion: targetRegion,
docs/reviews/pr194-codereview-codex-5-4.md:3292:+                targetClanId: targetClanId,
docs/reviews/pr194-codereview-codex-5-4.md:3304:+    function _submitTargetDefenders(uint32 defenderClanId, uint32 targetClanId, uint256 count)
docs/reviews/pr194-codereview-codex-5-4.md:3308:+        Clan memory targetClan = world.getClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:3309:+        ClanOrder[] memory orders = _defendTargetOrders(defenderClanId, targetClanId, targetClan.baseRegion, count);
docs/reviews/pr194-codereview-codex-5-4.md:3323:+        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:3338:+    function _assertBanditTargetDiedLog(
docs/reviews/pr194-codereview-codex-5-4.md:3340:+        uint32 expectedBanditId,
docs/reviews/pr194-codereview-codex-5-4.md:3344:+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
docs/reviews/pr194-codereview-codex-5-4.md:3345:+        bytes32 expectedBanditTopic = bytes32(uint256(expectedBanditId));
docs/reviews/pr194-codereview-codex-5-4.md:3350:+                    && logs[i].topics[1] == expectedBanditTopic && logs[i].topics[2] == expectedClanTopic
docs/reviews/pr194-codereview-codex-5-4.md:3356:+        fail("expected BanditTargetDied log");
docs/reviews/pr194-codereview-codex-5-4.md:3359:+    function _activateTargetDefenders(uint32 targetClanId, uint32[] memory defenderClanIds, uint256 countEach)
docs/reviews/pr194-codereview-codex-5-4.md:3364:+            uint64 executesAtTick = _submitTargetDefenders(defenderClanIds[i], targetClanId, countEach);
docs/reviews/pr194-codereview-codex-5-4.md:3378:+        return world.forceAttackingBandit(clan.baseRegion, strength, clanId);
docs/reviews/pr194-codereview-codex-5-4.md:3388:+    function test_defeatedBanditLootSingleDefendingClanGetsFullCarry() public {
docs/reviews/pr194-codereview-codex-5-4.md:3396:+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
docs/reviews/pr194-codereview-codex-5-4.md:3397:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3407:+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
docs/reviews/pr194-codereview-codex-5-4.md:3410:+    function test_defeatedBanditAwardsBlueprintToTargetClan() public {
docs/reviews/pr194-codereview-codex-5-4.md:3416:+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-4.md:3418:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3425:+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 1, "target blueprint awarded");
docs/reviews/pr194-codereview-codex-5-4.md:3428:+    function test_deadTargetCleanupReleasesDefendersAndEscapesBandit() public {
docs/reviews/pr194-codereview-codex-5-4.md:3431:+        uint32 targetClanId = clanIds[1];
docs/reviews/pr194-codereview-codex-5-4.md:3434:+        uint64 defenderExecutesAtTick = _submitTargetDefenders(defenderClanId, targetClanId, 1);
docs/reviews/pr194-codereview-codex-5-4.md:3441:+        assertEq(uint8(defenderStateBefore), uint8(ClansmanState.ACTING), "defender active before target death");
docs/reviews/pr194-codereview-codex-5-4.md:3442:+        assertEq(world.getActiveDefenders(targetClanId).length, 1, "target has active defender");
docs/reviews/pr194-codereview-codex-5-4.md:3447:+        world.setClanUpkeepState(targetClanId, deathFromTick, 0, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3450:+        uint32 banditId = _forceAttack(targetClanId, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3451:+        uint64 expectedBanditAbortTick = world.getWorldState().currentTick;
docs/reviews/pr194-codereview-codex-5-4.md:3454:+        world.settleClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:3456:+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
docs/reviews/pr194-codereview-codex-5-4.md:3458:+        Clan memory targetAfter = world.getClan(targetClanId);
docs/reviews/pr194-codereview-codex-5-4.md:3459:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-codereview-codex-5-4.md:3460:+        assertEq(targetAfter.livingClansmen, 0, "target living count");
docs/reviews/pr194-codereview-codex-5-4.md:3466:+        assertEq(world.getActiveDefenders(targetClanId).length, 0, "target defender registry cleared");
docs/reviews/pr194-codereview-codex-5-4.md:3468:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-4.md:3469:+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
docs/reviews/pr194-codereview-codex-5-4.md:3478:+    function test_defeatedBanditLootSplitsEquallyAcrossFourDefendingClans() public {
docs/reviews/pr194-codereview-codex-5-4.md:3497:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-4.md:3498:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3516:+        uint256[4] memory blueprintBefore;
docs/reviews/pr194-codereview-codex-5-4.md:3518:+            blueprintBefore[i] = world.getClan(clanIds[i]).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-4.md:3522:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3526:+        assertEq(world.getClan(clanIds[0]).blueprintBalance, blueprintBefore[0] + 1, "base owner blueprint");
docs/reviews/pr194-codereview-codex-5-4.md:3528:+            assertEq(world.getClan(clanIds[i]).blueprintBalance, blueprintBefore[i], "helper clan no blueprint");
docs/reviews/pr194-codereview-codex-5-4.md:3532:+    function test_multipleDefeatedBanditsEachAwardBlueprint() public {
docs/reviews/pr194-codereview-codex-5-4.md:3538:+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-4.md:3539:+        uint32 firstBanditId = _forceAttack(clanId, 1);
docs/reviews/pr194-codereview-codex-5-4.md:3540:+        uint32 secondBanditId = _forceAttack(clanId, 1);
docs/reviews/pr194-codereview-codex-5-4.md:3541:+        world.setBanditStrength(firstBanditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3542:+        world.setBanditStrength(secondBanditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3546:+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 2, "one blueprint per defeated bandit");
docs/reviews/pr194-codereview-codex-5-4.md:3549:+    function test_defeatedBanditLootBurnsWholeTokenOverflowAcrossThreeDefendingClans() public {
docs/reviews/pr194-codereview-codex-5-4.md:3562:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-4.md:3563:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3566:+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
docs/reviews/pr194-codereview-codex-5-4.md:3568:+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
docs/reviews/pr194-codereview-codex-5-4.md:3595:+        uint256 targetWoodBefore = world.getClan(clanIds[0]).vaultWood;
docs/reviews/pr194-codereview-codex-5-4.md:3598:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-4.md:3599:+        world.setBanditStrength(banditId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3603:+        assertEq(world.getClan(clanIds[0]).vaultWood, targetWoodBefore + 50e18, "target clan one share");
docs/reviews/pr194-codereview-codex-5-4.md:3607:+    function test_escapedBanditDoesNotDistributeCarry() public {
docs/reviews/pr194-codereview-codex-5-4.md:3611:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
docs/reviews/pr194-codereview-codex-5-4.md:3621:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-4.md:3624:+    function test_escapedBanditDoesNotAwardBlueprint() public {
docs/reviews/pr194-codereview-codex-5-4.md:3626:+        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
docs/reviews/pr194-codereview-codex-5-4.md:3631:+        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore, "blueprint unchanged");
docs/reviews/pr194-codereview-codex-5-4.md:3632:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-4.md:3635:+    function test_twoAliveDefendersWithSufficientDefenseDefeatBanditWithoutWallChip() public {
docs/reviews/pr194-codereview-codex-5-4.md:3641:+        bytes32 tickSeed = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:3646:+        world.setBanditStrength(banditId, strength);
docs/reviews/pr194-codereview-codex-5-4.md:3649:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
docs/reviews/pr194-codereview-codex-5-4.md:3663:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-4.md:3673:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
docs/reviews/pr194-codereview-codex-5-4.md:3684:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-codereview-codex-5-4.md:3685:+        assertEq(clan.vaultWood, 0, "dead target wood burned");
docs/reviews/pr194-codereview-codex-5-4.md:3686:+        assertEq(clan.vaultWheat, 0, "dead target wheat burned");
docs/reviews/pr194-codereview-codex-5-4.md:3687:+        assertEq(clan.vaultFish, 0, "dead target fish burned");
docs/reviews/pr194-codereview-codex-5-4.md:3688:+        assertEq(clan.vaultIron, 0, "dead target iron burned");
docs/reviews/pr194-codereview-codex-5-4.md:3701:+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
docs/reviews/pr194-codereview-codex-5-4.md:3711:+        BanditAttackHarness a = new BanditAttackHarness();
docs/reviews/pr194-codereview-codex-5-4.md:3712:+        BanditAttackHarness b = new BanditAttackHarness();
docs/reviews/pr194-codereview-codex-5-4.md:3722:+        a.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:3723:+        b.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:3725:+        assertEq(a.getClan(aFirst).livingClansmen, b.getClan(bFirst).livingClansmen, "first target deterministic");
docs/reviews/pr194-codereview-codex-5-4.md:3726:+        assertEq(a.getClan(aSecond).livingClansmen, b.getClan(bSecond).livingClansmen, "second target deterministic");
docs/reviews/pr194-codereview-codex-5-4.md:3727:+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
docs/reviews/pr194-codereview-codex-5-4.md:3728:+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
docs/reviews/pr194-codereview-codex-5-4.md:3731:+    function _setupTwoAttackWorld(BanditAttackHarness target) internal returns (uint32 firstClanId, uint32 secondClanId) {
docs/reviews/pr194-codereview-codex-5-4.md:3733:+        (firstClanId,) = target.mintClan(elder);
docs/reviews/pr194-codereview-codex-5-4.md:3735:+        (secondClanId,) = target.mintClan(elder);
docs/reviews/pr194-codereview-codex-5-4.md:3737:+        target.forceAttackingBandit(target.getClan(firstClanId).baseRegion, 100, firstClanId);
docs/reviews/pr194-codereview-codex-5-4.md:3738:+        target.forceAttackingBandit(target.getClan(secondClanId).baseRegion, 100, secondClanId);
docs/reviews/pr194-codereview-codex-5-4.md:3741:diff --git a/packages/contracts/test/BanditSpawn.t.sol b/packages/contracts/test/BanditSpawn.t.sol
docs/reviews/pr194-codereview-codex-5-4.md:3745:+++ b/packages/contracts/test/BanditSpawn.t.sol
docs/reviews/pr194-codereview-codex-5-4.md:3754:+    BanditState,
docs/reviews/pr194-codereview-codex-5-4.md:3755:+    BanditTroop,
docs/reviews/pr194-codereview-codex-5-4.md:3766:+contract BanditSpawnHarness is ClanWorld {
docs/reviews/pr194-codereview-codex-5-4.md:3767:+    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:3768:+        return _spawnBandit(region, strength);
docs/reviews/pr194-codereview-codex-5-4.md:3771:+    function evaluateBanditSpawns(bytes32 tickSeed) external {
docs/reviews/pr194-codereview-codex-5-4.md:3772:+        _evaluateBanditSpawns(tickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:3775:+    function setBanditSpawnState(uint8 region, uint64 lastSpawnTick, uint16 probabilityAccum) external {
docs/reviews/pr194-codereview-codex-5-4.md:3780:+    function getBanditSpawnState(uint8 region) external view returns (uint64 lastSpawnTick, uint16 probabilityAccum) {
docs/reviews/pr194-codereview-codex-5-4.md:3785:+    function activeBanditCount() external view returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:3786:+        return _activeBanditCount;
docs/reviews/pr194-codereview-codex-5-4.md:3797:+    function maxBanditsPerRegion() external pure returns (uint8) {
docs/reviews/pr194-codereview-codex-5-4.md:3801:+    function maxTotalBandits() external pure returns (uint8) {
docs/reviews/pr194-codereview-codex-5-4.md:3805:+    function maxBanditSpawnScanPerRegion() external pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-4.md:3809:+    function maxBanditEagerSettleBaseScanPerRegion() external pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-4.md:3818:+contract BanditSpawnTest is Test {
docs/reviews/pr194-codereview-codex-5-4.md:3820:+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
docs/reviews/pr194-codereview-codex-5-4.md:3822:+    BanditSpawnHarness world;
docs/reviews/pr194-codereview-codex-5-4.md:3825:+        world = new BanditSpawnHarness();
docs/reviews/pr194-codereview-codex-5-4.md:3828:+    function _advanceTick(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-4.md:3829:+        vm.warp(target.getWorldState().nextHeartbeatAtTs);
docs/reviews/pr194-codereview-codex-5-4.md:3830:+        target.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:3833:+    function _advanceTicks(BanditSpawnHarness target, uint64 ticks) internal {
docs/reviews/pr194-codereview-codex-5-4.md:3835:+            _advanceTick(target);
docs/reviews/pr194-codereview-codex-5-4.md:3839:+    function _advancePastInitialCooldown(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-4.md:3840:+        _advanceTicks(target, target.minSpawnCooldownTicks());
docs/reviews/pr194-codereview-codex-5-4.md:3843:+    function _mintForestClan(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-4.md:3844:+        target.mintClan(address(this));
docs/reviews/pr194-codereview-codex-5-4.md:3847:+    function _mintUntilTwoForestClans(BanditSpawnHarness target) internal returns (uint32 first, uint32 second) {
docs/reviews/pr194-codereview-codex-5-4.md:3848:+        for (uint256 i = 0; i < target.maxBanditEagerSettleBaseScanPerRegion(); i++) {
docs/reviews/pr194-codereview-codex-5-4.md:3849:+            (uint32 clanId,) = target.mintClan(address(this));
docs/reviews/pr194-codereview-codex-5-4.md:3850:+            if (target.getClan(clanId).baseRegion == ClanWorldConstants.REGION_FOREST) {
docs/reviews/pr194-codereview-codex-5-4.md:3862:+    function _csId(BanditSpawnHarness target, uint32 clanId, uint256 index) internal view returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:3863:+        return target.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
docs/reviews/pr194-codereview-codex-5-4.md:3876:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-4.md:3885:+            targetClanId: 0,
docs/reviews/pr194-codereview-codex-5-4.md:3892:+    function _submitPendingWorkerAndDefender(BanditSpawnHarness target, uint32 clanId)
docs/reviews/pr194-codereview-codex-5-4.md:3896:+        Clan memory clan = target.getClan(clanId);
docs/reviews/pr194-codereview-codex-5-4.md:3897:+        workerId = _csId(target, clanId, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3898:+        defenderId = _csId(target, clanId, 1);
docs/reviews/pr194-codereview-codex-5-4.md:3901:+            target.submitClanOrders(clanId, _ordersForPendingWorkerAndDefender(workerId, defenderId, clan.baseRegion));
docs/reviews/pr194-codereview-codex-5-4.md:3906:+    function _blockNonForestSpawnRegions(BanditSpawnHarness target) internal {
docs/reviews/pr194-codereview-codex-5-4.md:3907:+        uint64 currentTick = target.getWorldState().currentTick;
docs/reviews/pr194-codereview-codex-5-4.md:3910:+                target.setBanditSpawnState(region, currentTick, 0);
docs/reviews/pr194-codereview-codex-5-4.md:3915:+    function _prevrandaoForNextForestSpawn(BanditSpawnHarness target) internal view returns (bytes32) {
docs/reviews/pr194-codereview-codex-5-4.md:3916:+        WorldState memory state = target.getWorldState();
docs/reviews/pr194-codereview-codex-5-4.md:3919:+            bytes32 nextSeed = keccak256(abi.encode(nextRandao, state.currentTickSeed, state.currentTick));
docs/reviews/pr194-codereview-codex-5-4.md:3920:+            if (target.banditSpawnRoll(nextSeed, ClanWorldConstants.REGION_FOREST) < 8000) {
docs/reviews/pr194-codereview-codex-5-4.md:3952:+        world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
docs/reviews/pr194-codereview-codex-5-4.md:3956:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
docs/reviews/pr194-codereview-codex-5-4.md:3957:+        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-4.md:3966:+        world.evaluateBanditSpawns(_missSeed(ClanWorldConstants.REGION_FOREST, increment));
docs/reviews/pr194-codereview-codex-5-4.md:3968:+        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
docs/reviews/pr194-codereview-codex-5-4.md:3978:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, oneStepBelowCap);
docs/reviews/pr194-codereview-codex-5-4.md:3979:+        world.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, oneStepBelowCap);
docs/reviews/pr194-codereview-codex-5-4.md:3981:+        world.evaluateBanditSpawns(
docs/reviews/pr194-codereview-codex-5-4.md:3985:+        uint8 selectedRegion = world.getBandit(1).region;
docs/reviews/pr194-codereview-codex-5-4.md:3989:+        (, uint16 selectedAccum) = world.getBanditSpawnState(selectedRegion);
docs/reviews/pr194-codereview-codex-5-4.md:3990:+        (, uint16 otherAccum) = world.getBanditSpawnState(otherRegion);
docs/reviews/pr194-codereview-codex-5-4.md:4000:+        uint8 maxPerRegion = world.maxBanditsPerRegion();
docs/reviews/pr194-codereview-codex-5-4.md:4002:+            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
docs/reviews/pr194-codereview-codex-5-4.md:4004:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4006:+        world.evaluateBanditSpawns(keccak256("per-region-cap"));
docs/reviews/pr194-codereview-codex-5-4.md:4008:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
docs/reviews/pr194-codereview-codex-5-4.md:4015:+        uint8 maxTotal = world.maxTotalBandits();
docs/reviews/pr194-codereview-codex-5-4.md:4017:+            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
docs/reviews/pr194-codereview-codex-5-4.md:4019:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4021:+        world.evaluateBanditSpawns(keccak256("global-cap"));
docs/reviews/pr194-codereview-codex-5-4.md:4023:+        assertEq(world.activeBanditCount(), maxTotal, "global cap");
docs/reviews/pr194-codereview-codex-5-4.md:4029:+        uint8 maxTotal = world.maxTotalBandits();
docs/reviews/pr194-codereview-codex-5-4.md:4031:+            world.spawnBandit(uint8(ClanWorldConstants.REGION_FOREST + (i % 8)), 100 + i);
docs/reviews/pr194-codereview-codex-5-4.md:4033:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 4321);
docs/reviews/pr194-codereview-codex-5-4.md:4038:+        assertEq(world.activeBanditCount(), maxTotal, "still at cap");
docs/reviews/pr194-codereview-codex-5-4.md:4039:+        assertEq(state.nextBanditSpawnEligibleTick, 0, "no eligible tick while capped");
docs/reviews/pr194-codereview-codex-5-4.md:4040:+        assertEq(state.currentBanditSpawnChanceBps, 4321, "preview chance refreshed");
docs/reviews/pr194-codereview-codex-5-4.md:4043:+    function test_heartbeatCompletesWhenClanCountExceedsBanditSpawnScanCap() public {
docs/reviews/pr194-codereview-codex-5-4.md:4044:+        uint256 clanCount = world.maxBanditSpawnScanPerRegion() + 1;
docs/reviews/pr194-codereview-codex-5-4.md:4053:+        assertEq(world.getWorldState().currentTick, 1, "heartbeat advanced");
docs/reviews/pr194-codereview-codex-5-4.md:4056:+    function test_heartbeatEagerSettlesCandidateRegionBasesAndDefendersBeforeBanditSpawn() public {
docs/reviews/pr194-codereview-codex-5-4.md:4064:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, world.getWorldState().currentTick, 0);
docs/reviews/pr194-codereview-codex-5-4.md:4077:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4084:+        emit BanditSpawned(1, 0, 0, 0);
docs/reviews/pr194-codereview-codex-5-4.md:4088:+        assertEq(world.getClan(clanId1).lastSettledTick, closedTick, "clan 1 eager-settled");
docs/reviews/pr194-codereview-codex-5-4.md:4089:+        assertEq(world.getClan(clanId2).lastSettledTick, closedTick, "clan 2 eager-settled");
docs/reviews/pr194-codereview-codex-5-4.md:4100:+        BanditTroop memory bandit = world.getBandit(1);
docs/reviews/pr194-codereview-codex-5-4.md:4101:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
docs/reviews/pr194-codereview-codex-5-4.md:4102:+        assertEq(bandit.region, ClanWorldConstants.REGION_FOREST, "forest selected after eager settle");
docs/reviews/pr194-codereview-codex-5-4.md:4107:+        BanditSpawnHarness a = new BanditSpawnHarness();
docs/reviews/pr194-codereview-codex-5-4.md:4108:+        BanditSpawnHarness b = new BanditSpawnHarness();
docs/reviews/pr194-codereview-codex-5-4.md:4115:+        a.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4116:+        a.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4117:+        b.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4118:+        b.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4121:+        a.evaluateBanditSpawns(seed);
docs/reviews/pr194-codereview-codex-5-4.md:4122:+        b.evaluateBanditSpawns(seed);
docs/reviews/pr194-codereview-codex-5-4.md:4124:+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
docs/reviews/pr194-codereview-codex-5-4.md:4136:+    function test_spawnedBanditEntersStateMachine() public {
docs/reviews/pr194-codereview-codex-5-4.md:4139:+        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
docs/reviews/pr194-codereview-codex-5-4.md:4141:+        world.evaluateBanditSpawns(keccak256("spawn-state-machine"));
docs/reviews/pr194-codereview-codex-5-4.md:4143:+        BanditTroop memory bandit = world.getBandit(1);
docs/reviews/pr194-codereview-codex-5-4.md:4145:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
docs/reviews/pr194-codereview-codex-5-4.md:4147:+        assertEq(state.activeBanditId, bandit.id, "active bandit set");
docs/reviews/pr194-codereview-codex-5-4.md:4155:         world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4219:+        assertEq(world.quoteLootValueSettled(clanId), 20e18, "settled loot quote uses simulated vault");
docs/reviews/pr194-codereview-codex-5-4.md:4220:+        assertEq(world.quoteLootValueRaw(clanId), 44e18, "raw loot quote remains committed storage");
docs/reviews/pr194-codereview-codex-5-4.md:4261:+        // Advance until the clan is truly >200 ticks behind. Phase 9 bandit
docs/reviews/pr194-codereview-codex-5-4.md:4262:+        // eager-settle may settle candidate-region bases on spawn ticks, so
docs/reviews/pr194-codereview-codex-5-4.md:4263:+        // this test follows the invariant instead of assuming no heartbeat
docs/reviews/pr194-codereview-codex-5-4.md:4278:+import {BanditState, BanditTroop, ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
docs/reviews/pr194-codereview-codex-5-4.md:4286:+    function test_getBanditMissingIdReturnsNoneState() public view {
docs/reviews/pr194-codereview-codex-5-4.md:4287:+        BanditTroop memory bandit = stub.getBandit(999);
docs/reviews/pr194-codereview-codex-5-4.md:4290:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
docs/reviews/pr194-codereview-codex-5-4.md:4341:+        observedSeed = state.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4391:+        blueprintToken = new MinimalERC20("BPRT", "BPRT");
docs/reviews/pr194-codereview-codex-5-4.md:4405:+            address(blueprintToken)
docs/reviews/pr194-codereview-codex-5-4.md:4426:     // test_heartbeat_settlementBeforeMarket
docs/reviews/pr194-codereview-codex-5-4.md:4429:         assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
docs/reviews/pr194-codereview-codex-5-4.md:4432:+    function test_heartbeat_scheduledMarketObservesClosedTickSeedBeforeIncrement() public {
docs/reviews/pr194-codereview-codex-5-4.md:4446:+        bytes32 seedForClosedTick = beforeClose.currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4455:+        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
docs/reviews/pr194-codereview-codex-5-4.md:4456:+        assertNotEq(afterClose.currentTickSeed, seedForClosedTick, "next tick seed publishes after close-tick work");
docs/reviews/pr194-codereview-codex-5-4.md:4460:     // test_heartbeat_seasonTransition
docs/reviews/pr194-codereview-codex-5-4.md:4465:Reviewing the Phase 9 bandit diff end-to-end now. I’m going to trace the contract state machine, heartbeat ordering, and test coverage in parallel so we can catch integration bugs instead of just local issues.
docs/reviews/pr194-codereview-codex-5-4.md:4501:    // Bandit cadence
docs/reviews/pr194-codereview-codex-5-4.md:4538:    // Bandit combat
docs/reviews/pr194-codereview-codex-5-4.md:4574:enum BanditState {
docs/reviews/pr194-codereview-codex-5-4.md:4661:    uint64 nextBanditSpawnEligibleTick;
docs/reviews/pr194-codereview-codex-5-4.md:4662:    uint16 currentBanditSpawnChanceBps;
docs/reviews/pr194-codereview-codex-5-4.md:4663:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4665:    uint32 activeBanditId;       // 0 if none
docs/reviews/pr194-codereview-codex-5-4.md:4684:    address blueprintToken;
docs/reviews/pr194-codereview-codex-5-4.md:4710:    uint256 blueprintBalance;
docs/reviews/pr194-codereview-codex-5-4.md:4731:/bin/bash -lc "rg -n \"enum BanditState|struct BanditTroop|heartbeat\\(|_settleClan\\(|_settleCompletingMissions\\(|_resolveWorldEvents\\(|DefendBase|getActiveDefenders|submitClanOrders|_clearDefender|_applyUpkeep\\(|currentTickSeed|TickAdvanced|currentSeasonNumber|nextHeartbeatAtTick|getWorldState\\(\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/pr194-codereview-codex-5-4.md:4734:packages/contracts/test/ClanWorldStub.t.sol:28:        stub.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4736:packages/contracts/test/ClanWorldStub.t.sol:33:        stub.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4737:packages/contracts/src/ClanWorldStub.sol:70:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-4.md:4742:packages/contracts/src/ClanWorldStub.sol:278:            currentTickSeed: bytes32(0),
docs/reviews/pr194-codereview-codex-5-4.md:4743:packages/contracts/test/ClanWorld.t.sol:122:        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4745:packages/contracts/test/ClanWorld.t.sol:154:        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4746:packages/contracts/test/ClanWorld.t.sol:157:        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4747:packages/contracts/test/ClanWorld.t.sol:165:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4748:packages/contracts/test/ClanWorld.t.sol:169:        world.heartbeat();
docs/reviews/pr194-codereview-codex-5-4.md:4749:packages/contracts/test/ClanWorld.t.sol:171:        bytes32 seedAfter = world.getWorldState().currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4768:packages/contracts/test/ClanWorld.t.sol:830:    // Issue #95: DefendBase re-tasking — zero-travel same-target retask must not drop defender
docs/reviews/pr194-codereview-codex-5-4.md:4774:packages/contracts/test/ClanWorld.t.sol:862:        // Second DefendBase to same target: zero-travel re-task — the regression case.
docs/reviews/pr194-codereview-codex-5-4.md:4783:packages/contracts/src/ClanWorld.sol:724:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-4.md:4784:packages/contracts/src/ClanWorld.sol:733:        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4785:packages/contracts/src/ClanWorld.sol:741:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:4792:packages/contracts/src/ClanWorld.sol:1023:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
docs/reviews/pr194-codereview-codex-5-4.md:4793:packages/contracts/src/ClanWorld.sol:1325:        // DefendBase: targetClanId must be valid and gotoRegion must match target's baseRegion
docs/reviews/pr194-codereview-codex-5-4.md:4796:packages/contracts/src/ClanWorld.sol:1500:    function getActiveDefenders(uint32 targetClanId)
docs/reviews/pr194-codereview-codex-5-4.md:4797:packages/contracts/src/ClanWorld.sol:1627:            currentTickSeed: _world.currentTickSeed,
docs/reviews/pr194-codereview-codex-5-4.md:4798:packages/contracts/src/IClanWorld.sol:106:enum BanditState {
docs/reviews/pr194-codereview-codex-5-4.md:4800:packages/contracts/src/IClanWorld.sol:195:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4801:packages/contracts/src/IClanWorld.sol:289:    uint32 targetClanId;   // DefendBase only
docs/reviews/pr194-codereview-codex-5-4.md:4802:packages/contracts/src/IClanWorld.sol:295:struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-4.md:4803:packages/contracts/src/IClanWorld.sol:363:    uint32 targetClanId;          // DefendBase only
docs/reviews/pr194-codereview-codex-5-4.md:4804:packages/contracts/src/IClanWorld.sol:411:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:4806:packages/contracts/src/IClanWorld.sol:667:    function heartbeat() external;
docs/reviews/pr194-codereview-codex-5-4.md:4809:packages/contracts/src/IClanWorld.sol:747:    function getActiveDefenders(uint32 targetClanId)
docs/reviews/pr194-codereview-codex-5-4.md:4823:    BanditState,
docs/reviews/pr194-codereview-codex-5-4.md:4835:    BanditTroop,
docs/reviews/pr194-codereview-codex-5-4.md:4850:    ActiveBanditView,
docs/reviews/pr194-codereview-codex-5-4.md:4874:    mapping(uint32 => uint32[]) private _incomingDefenders;    // targetClanId => clansmanIds
docs/reviews/pr194-codereview-codex-5-4.md:4875:    mapping(uint32 => uint32) private _clanDefendingBase;      // clansmanId => targetClanId
docs/reviews/pr194-codereview-codex-5-4.md:5085:                    cs.currentRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:5086:                    emit WorkerArrived(clanId, csId, m.targetRegion, tick);
docs/reviews/pr194-codereview-codex-5-4.md:5166:            if (_clanDefendingBase[cs.clansmanId] != m.targetClanId) {
docs/reviews/pr194-codereview-codex-5-4.md:5167:                _incomingDefenders[m.targetClanId].push(cs.clansmanId);
docs/reviews/pr194-codereview-codex-5-4.md:5168:                _clanDefendingBase[cs.clansmanId] = m.targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:5180:            // (Actual execution happened or will happen at heartbeat.)
docs/reviews/pr194-codereview-codex-5-4.md:5346:        uint8 region = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:5506:        uint256 blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:5514:        else if (nextLevel <= 10) { woodCost = 200e18; wheatCost = 100e18; ironCost = 25e18; blueprintCost = 1e18; }
docs/reviews/pr194-codereview-codex-5-4.md:5517:            clan.vaultIron < ironCost || clan.blueprintBalance < blueprintCost) return false;
docs/reviews/pr194-codereview-codex-5-4.md:5522:        clan.blueprintBalance -= blueprintCost;
docs/reviews/pr194-codereview-codex-5-4.md:5541:    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
docs/reviews/pr194-codereview-codex-5-4.md:5542:    function heartbeat() external override {
docs/reviews/pr194-codereview-codex-5-4.md:5543:        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
docs/reviews/pr194-codereview-codex-5-4.md:5551:        _world.currentTickSeed = newSeed;
docs/reviews/pr194-codereview-codex-5-4.md:5559:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
docs/reviews/pr194-codereview-codex-5-4.md:5609:        clan.blueprintBalance = 0;
docs/reviews/pr194-codereview-codex-5-4.md:5741:I’ve got the baseline contract shape loaded now. Next I’m splitting the review into three parallel passes: bandit lifecycle + heartbeat, derived-settlement simulation, and ABI/test/integration seams so we can compare conclusions instead of trusting one path.
docs/reviews/pr194-codereview-codex-5-4.md:5756:        // Validate target region (1-8 or 0=noop)
docs/reviews/pr194-codereview-codex-5-4.md:5798:        // Registration at the target base happens at arrival in _resolveAction (v4.3 — arrived defenders only).
docs/reviews/pr194-codereview-codex-5-4.md:5807:        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
docs/reviews/pr194-codereview-codex-5-4.md:5814:        // When travelTicks == 0 the clansman is already at the target base — no travel window to wait out.
docs/reviews/pr194-codereview-codex-5-4.md:5815:        // The _resolveAction guard (clanDefendingBase != targetClanId) will be false next tick, preventing double-register.
docs/reviews/pr194-codereview-codex-5-4.md:5817:            _incomingDefenders[order.targetClanId].push(cs.clansmanId);
docs/reviews/pr194-codereview-codex-5-4.md:5818:            _clanDefendingBase[cs.clansmanId] = order.targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:5852:        m.targetRegion = ctx.gotoRegion;
docs/reviews/pr194-codereview-codex-5-4.md:5857:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
docs/reviews/pr194-codereview-codex-5-4.md:5861:        m.targetClanId = order.targetClanId;
docs/reviews/pr194-codereview-codex-5-4.md:5896:    function _removeDefender(uint32 targetClanId, uint32 clansmanId) internal {
docs/reviews/pr194-codereview-codex-5-4.md:5897:        uint32[] storage defenders = _incomingDefenders[targetClanId];
docs/reviews/pr194-codereview-codex-5-4.md:5911:    /// @dev Execute all scheduled market actions for the given tick. Called from heartbeat.
docs/reviews/pr194-codereview-codex-5-4.md:5994:        // DefendBase: targetClanId must be valid and gotoRegion must match target's baseRegion
docs/reviews/pr194-codereview-codex-5-4.md:5996:            if (order.targetClanId == 0 || _clans[order.targetClanId].clanId == 0) {
docs/reviews/pr194-codereview-codex-5-4.md:5999:            if (_clans[order.targetClanId].clanState == ClanState.DEAD) {
docs/reviews/pr194-codereview-codex-5-4.md:6002:            if (gotoRegion != _clans[order.targetClanId].baseRegion) {
docs/reviews/pr194-codereview-codex-5-4.md:6007:        // MarketBuy/MarketSell: must target Unicorn Town
docs/reviews/pr194-codereview-codex-5-4.md:6030:                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
docs/reviews/pr194-codereview-codex-5-4.md:6056:        _treasury.blueprintToken = tokens[5];
docs/reviews/pr194-codereview-codex-5-4.md:6133:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
docs/reviews/pr194-codereview-codex-5-4.md:6134:        return BanditTroop({
docs/reviews/pr194-codereview-codex-5-4.md:6136:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-4.md:6169:    function getActiveDefenders(uint32 targetClanId)
docs/reviews/pr194-codereview-codex-5-4.md:6175:        return _incomingDefenders[targetClanId];
docs/reviews/pr194-codereview-codex-5-4.md:6192:        uint256 lootVal = _lootValueRaw(clan);
docs/reviews/pr194-codereview-codex-5-4.md:6196:            lootValue: lootVal,
docs/reviews/pr194-codereview-codex-5-4.md:6211:            // Simplified: if past arrivalTick, they're at target; else at start
docs/reviews/pr194-codereview-codex-5-4.md:6213:                effectiveRegion = m.targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:6231:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
docs/reviews/pr194-codereview-codex-5-4.md:6254:        return _lootValueRaw(_clans[clanId]);
docs/reviews/pr194-codereview-codex-5-4.md:6259:        return _lootValueRaw(_clans[clanId]);
docs/reviews/pr194-codereview-codex-5-4.md:6262:    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
docs/reviews/pr194-codereview-codex-5-4.md:6263:    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
docs/reviews/pr194-codereview-codex-5-4.md:6271:    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
docs/reviews/pr194-codereview-codex-5-4.md:6288:                lootValue: _lootValueRaw(clan)
docs/reviews/pr194-codereview-codex-5-4.md:6300:            activeBanditId: _world.activeBanditId,
docs/reviews/pr194-codereview-codex-5-4.md:6301:            currentTickSeed: _world.currentTickSeed,
docs/reviews/pr194-codereview-codex-5-4.md:6312:        uint256 lootVal = _lootValueRaw(clan);
docs/reviews/pr194-codereview-codex-5-4.md:6317:            lootValue: lootVal,
docs/reviews/pr194-codereview-codex-5-4.md:6329:                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
docs/reviews/pr194-codereview-codex-5-4.md:6343:            uint32 target = _clanDefendingBase[csIds[i]];
docs/reviews/pr194-codereview-codex-5-4.md:6344:            if (target != 0) {
docs/reviews/pr194-codereview-codex-5-4.md:6345:                thisClanDefendingBaseId = target;
docs/reviews/pr194-codereview-codex-5-4.md:6383:    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
docs/reviews/pr194-codereview-codex-5-4.md:6384:        return ActiveBanditView({
docs/reviews/pr194-codereview-codex-5-4.md:6387:            state: BanditState.NONE,
docs/reviews/pr194-codereview-codex-5-4.md:6459:    uint256 blueprintBalance;
docs/reviews/pr194-codereview-codex-5-4.md:6496:    uint8 targetRegion;
docs/reviews/pr194-codereview-codex-5-4.md:6506:    uint32 targetClanId;   // DefendBase only
docs/reviews/pr194-codereview-codex-5-4.md:6512:struct BanditTroop {
docs/reviews/pr194-codereview-codex-5-4.md:6514:    BanditState state;
docs/reviews/pr194-codereview-codex-5-4.md:6560:    uint256 lootValue;            // current weighted loot value
docs/reviews/pr194-codereview-codex-5-4.md:6580:    uint32 targetClanId;          // DefendBase only
docs/reviews/pr194-codereview-codex-5-4.md:6616:    uint256 lootValue;            // settled
docs/reviews/pr194-codereview-codex-5-4.md:6627:    uint32 activeBanditId;
docs/reviews/pr194-codereview-codex-5-4.md:6628:    bytes32 currentTickSeed;
docs/reviews/pr194-codereview-codex-5-4.md:6670:struct ActiveBanditView {
docs/reviews/pr194-codereview-codex-5-4.md:6673:    BanditState state;
docs/reviews/pr194-codereview-codex-5-4.md:6687:    uint32 projectedTargetClanId;     // 0 if no eligible target in current region
docs/reviews/pr194-codereview-codex-5-4.md:6729:        uint8 targetRegion,
docs/reviews/pr194-codereview-codex-5-4.md:6817:    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
docs/reviews/pr194-codereview-codex-5-4.md:6818:    event BanditStateChanged(
docs/reviews/pr194-codereview-codex-5-4.md:6820:        BanditState oldState,
docs/reviews/pr194-codereview-codex-5-4.md:6821:        BanditState newState,
docs/reviews/pr194-codereview-codex-5-4.md:6825:    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-4.md:6826:    event BanditAttackResolved(
docs/reviews/pr194-codereview-codex-5-4.md:6828:        uint32 indexed targetClanId,
docs/reviews/pr194-codereview-codex-5-4.md:6839:    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-4.md:6840:    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
docs/reviews/pr194-codereview-codex-5-4.md:6891:    /// @notice Permissionless heartbeat. Closes the current tick, resolves
docs/reviews/pr194-codereview-codex-5-4.md:6894:    function heartbeat() external;
docs/reviews/pr194-codereview-codex-5-4.md:6941:        uint256 blueprint,
docs/reviews/pr194-codereview-codex-5-4.md:6962:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
docs/reviews/pr194-codereview-codex-5-4.md:6974:    function getActiveDefenders(uint32 targetClanId)
docs/reviews/pr194-codereview-codex-5-4.md:6996:    /// @notice Non-binding preview. Bandit targeting is recomputed at attack
docs/reviews/pr194-codereview-codex-5-4.md:6997:    ///         resolution time using then-current eagerly settled state.
docs/reviews/pr194-codereview-codex-5-4.md:6998:    function getBanditTargetPreview(uint32 banditId)

exec
/bin/bash -lc 'rg -n "function rngWeightedPick|library RNG|rngBounded" packages/contracts/src packages/contracts/test' in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc "rg -n \"getBanditTroop|getBandit\\(|BanditState|BanditTroop|activeBandit|BlueprintEarned|LootDistributed|WallDamagedByBandit|ClansmanKilledByBandit|BanditTargetDied\" apps packages/shared packages/agents packages/runner packages/orchestrator -g '"'!packages/contracts/abi/IClanWorld.json'"'" in /home/claude/code/clan-world
 exited 2 in 0ms:
rg: packages/orchestrator: No such file or directory (os error 2)
packages/shared/src/mocks/clanWorldFixture.ts:76:export interface BanditState {
packages/shared/src/mocks/clanWorldFixture.ts:309:const BANDIT_STATE: BanditState = {
packages/shared/src/mocks/clanWorldFixture.ts:329:  banditState: BanditState;
packages/shared/src/adapters/IChainClient.ts:42:          { name: 'activeBanditId', type: 'uint32' },

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '350,520p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   350	        Mission storage m = _missions[cs.clansmanId];
   351	
   352	        // Path 6: dead clansman — invalidate active mission if any
   353	        if (cs.state == ClansmanState.DEAD) {
   354	            if (m.active) {
   355	                if (m.action == ActionType.DefendBase) {
   356	                    _clearDefender(cs.clansmanId);
   357	                }
   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
   359	            }
   360	            return;
   361	        }
   362	
   363	        if (!m.active) return; // no active mission — nothing to settle
   364	
   365	        bytes32 tickSeed;
   366	        for (uint64 tick = fromTick; tick < toTick; tick++) {
   367	            tickSeed = _tickSeeds[tick];
   368	
   369	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   370	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   371	                cs.state = ClansmanState.ACTING;
   372	                cs.currentRegion = m.targetRegion;
   373	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   374	
   375	                if (m.action == ActionType.DefendBase) {
   376	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   377	                }
   378	            }
   379	
   380	            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
   381	
   382	            // Path 3: ACTING at/past settlesAtTick → resolve
   383	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
   384	                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   385	                if (m.active && getActionDuration(m.action) > 0) {
   386	                    _completeMission(cs, m);
   387	                }
   388	            }
   389	
   390	            // If mission completed during this tick, stop iterating
   391	            if (!m.active) break;
   392	        }
   393	    }
   394	
   395	    /// @dev Lazy settlement of a clan forward to currentTick.
   396	    ///      Mutates storage. Called before order submission and by public settleClan().
   397	    function _settleClan(uint32 clanId) internal {
   398	        Clan storage clan = _clans[clanId];
   399	        if (clan.clanId == 0) return;
   400	
   401	        uint64 curTick = _world.currentTick;
   402	        uint64 fromTick = clan.lastSettledTick;
   403	        if (fromTick >= curTick) return;
   404	
   405	        // Cap ticks settled per call to prevent block gas limit issues
   406	        uint64 maxSettleTicks = 200;
   407	        if (curTick > fromTick + maxSettleTicks) {
   408	            curTick = fromTick + maxSettleTicks;
   409	        }
   410	
   411	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   412	
   413	        // Settle tick by tick from fromTick to curTick - 1
   414	        // (curTick is still open; we settle through the last closed tick)
   415	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   416	            // 1. Apply upkeep for this tick
   417	            _applyUpkeep(clan, tick);
   418	            if (clan.clanState == ClanState.DEAD) break;
   419	
   420	            // 2. Wheat plot regrow check (lazy, per tick)
   421	            for (uint256 pi = 0; pi < 2; pi++) {
   422	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   423	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   424	                    plot.state = WheatPlotState.Harvestable;
   425	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   426	                    plot.regrowUntilTick = 0;
   427	                }
   428	            }
   429	
   430	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   431	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   432	                Clansman storage cs = _clansmen[clansmanIds[i]];
   433	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   434	            }
   435	        }
   436	
   437	        clan.lastSettledTick = curTick;
   438	        emit ClanSettled(clanId, curTick);
   439	    }
   440	
   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
   442	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   443	        if (clan.livingClansmen == 0) return;
   444	
   445	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   446	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   447	
   448	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   449	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   450	
   451	        if (hadEnoughWheat) {
   452	            clan.vaultWheat -= wheatNeeded;
   453	        } else {
   454	            clan.vaultWheat = 0;
   455	        }
   456	        if (hadEnoughFish) {
   457	            clan.vaultFish -= fishNeeded;
   458	        } else {
   459	            clan.vaultFish = 0;
   460	        }
   461	
   462	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   463	        if (starving && clan.starvationStartsAtTick == 0) {
   464	            clan.starvationStartsAtTick = tick;
   465	            emit ClanStarvationChanged(clan.clanId, true, tick);
   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   467	            clan.starvationStartsAtTick = 0;
   468	            emit ClanStarvationChanged(clan.clanId, false, tick);
   469	        }
   470	
   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
   472	            _killNextClansmanFromStarvation(clan, tick);
   473	        }
   474	    }
   475	
   476	    function _killNextClansmanFromStarvation(Clan storage clan, uint64 tick) internal {
   477	        if (clan.livingClansmen == 0) return;
   478	
   479	        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
   480	        for (uint256 i = 0; i < csIds.length; i++) {
   481	            Clansman storage cs = _clansmen[csIds[i]];
   482	            if (cs.state == ClansmanState.DEAD) continue;
   483	
   484	            _markClansmanDead(clan, cs);
   485	            if (clan.livingClansmen == 0) {
   486	                _markClanDead(clan.clanId, "starvation", tick);
   487	            }
   488	            return;
   489	        }
   490	    }
   491	
   492	    function _markClansmanDead(Clan storage clan, Clansman storage cs) internal {
   493	        if (cs.state == ClansmanState.DEAD) return;
   494	
   495	        cs.state = ClansmanState.DEAD;
   496	        cs.cooldownEndsAtTs = 0;
   497	        if (clan.livingClansmen > 0) {
   498	            clan.livingClansmen--;
   499	        }
   500	
   501	        Mission storage mission = _missions[cs.clansmanId];
   502	        if (mission.active) {
   503	            if (mission.action == ActionType.DefendBase) {
   504	                _clearDefender(cs.clansmanId);
   505	            }
   506	            mission.active = false;
   507	        }
   508	    }
   509	
   510	    function _markClanDead(uint32 clanId) internal {
   511	        _markClanDead(clanId, "unknown", _world.currentTick, ClanWorldConstants.BANDIT_ID_NULL);
   512	    }
   513	
   514	    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {
   515	        _markClanDead(clanId, reason, tick, ClanWorldConstants.BANDIT_ID_NULL);
   516	    }
   517	
   518	    function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId) internal {
   519	        Clan storage clan = _clans[clanId];
   520	        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '880,1280p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   880	            _completeMission(cs, m);
   881	            return;
   882	        }
   883	
   884	        bool success = false;
   885	        if (action == ActionType.BuildWall) {
   886	            success = _tryBuildWall(clan, clanId, tick);
   887	        } else if (action == ActionType.UpgradeBase) {
   888	            success = _tryUpgradeBase(clan, clanId, tick);
   889	        } else if (action == ActionType.UpgradeMonument) {
   890	            success = _tryUpgradeMonument(clan, clanId, tick);
   891	        }
   892	
   893	        if (!success) {
   894	            // Resources missing — worker transitions to WAITING
   895	        }
   896	        _completeMission(cs, m);
   897	    }
   898	
   899	    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   900	        uint8 nextLevel = clan.wallLevel + 1;
   901	        if (nextLevel > 5) return false;
   902	
   903	        uint256 woodCost;
   904	        uint256 ironCost;
   905	
   906	        if (nextLevel == 1) {
   907	            woodCost = 20e18;
   908	            ironCost = 0;
   909	        } else if (nextLevel == 2) {
   910	            woodCost = 35e18;
   911	            ironCost = 0;
   912	        } else if (nextLevel == 3) {
   913	            woodCost = 30e18;
   914	            ironCost = 5e18;
   915	        } else if (nextLevel == 4) {
   916	            woodCost = 40e18;
   917	            ironCost = 10e18;
   918	        } else {
   919	            woodCost = 50e18;
   920	            ironCost = 15e18;
   921	        }
   922	
   923	        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;
   924	
   925	        clan.vaultWood -= woodCost;
   926	        clan.vaultIron -= ironCost;
   927	        uint8 old = clan.wallLevel;
   928	        clan.wallLevel = nextLevel;
   929	        emit WallLevelChanged(clanId, old, nextLevel, tick);
   930	        return true;
   931	    }
   932	
   933	    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   934	        uint8 nextLevel = clan.baseLevel + 1;
   935	        if (nextLevel > 5) return false;
   936	
   937	        uint256 woodCost;
   938	        uint256 ironCost;
   939	        uint256 wheatCost;
   940	
   941	        if (nextLevel == 2) {
   942	            woodCost = 40e18;
   943	            ironCost = 0;
   944	            wheatCost = 20e18;
   945	        } else if (nextLevel == 3) {
   946	            woodCost = 60e18;
   947	            ironCost = 5e18;
   948	            wheatCost = 30e18;
   949	        } else if (nextLevel == 4) {
   950	            woodCost = 80e18;
   951	            ironCost = 10e18;
   952	            wheatCost = 40e18;
   953	        } else {
   954	            woodCost = 100e18;
   955	            ironCost = 15e18;
   956	            wheatCost = 50e18;
   957	        }
   958	
   959	        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost || clan.vaultWheat < wheatCost) return false;
   960	
   961	        clan.vaultWood -= woodCost;
   962	        clan.vaultIron -= ironCost;
   963	        clan.vaultWheat -= wheatCost;
   964	        uint8 old = clan.baseLevel;
   965	        clan.baseLevel = nextLevel;
   966	        emit BaseLevelChanged(clanId, old, nextLevel, tick);
   967	        return true;
   968	    }
   969	
   970	    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   971	        uint8 nextLevel = clan.monumentLevel + 1;
   972	        if (nextLevel > 10) return false;
   973	
   974	        uint256 woodCost;
   975	        uint256 wheatCost;
   976	        uint256 ironCost;
   977	        uint256 blueprintCost;
   978	
   979	        if (nextLevel == 1) {
   980	            woodCost = 30e18;
   981	            wheatCost = 20e18;
   982	        } else if (nextLevel == 2) {
   983	            woodCost = 50e18;
   984	            wheatCost = 30e18;
   985	        } else if (nextLevel == 3) {
   986	            woodCost = 70e18;
   987	            wheatCost = 40e18;
   988	            ironCost = 5e18;
   989	        } else if (nextLevel == 4) {
   990	            woodCost = 90e18;
   991	            wheatCost = 50e18;
   992	            ironCost = 10e18;
   993	        } else if (nextLevel == 5) {
   994	            woodCost = 120e18;
   995	            wheatCost = 60e18;
   996	            ironCost = 15e18;
   997	        } else if (nextLevel == 6) {
   998	            woodCost = 150e18;
   999	            wheatCost = 80e18;
  1000	            ironCost = 20e18;
  1001	        } else if (nextLevel <= 10) {
  1002	            woodCost = 200e18;
  1003	            wheatCost = 100e18;
  1004	            ironCost = 25e18;
  1005	            blueprintCost = 1e18;
  1006	        }
  1007	
  1008	        if (
  1009	            clan.vaultWood < woodCost || clan.vaultWheat < wheatCost || clan.vaultIron < ironCost
  1010	                || clan.blueprintBalance < blueprintCost
  1011	        ) return false;
  1012	
  1013	        clan.vaultWood -= woodCost;
  1014	        clan.vaultWheat -= wheatCost;
  1015	        clan.vaultIron -= ironCost;
  1016	        clan.blueprintBalance -= blueprintCost;
  1017	
  1018	        uint8 old = clan.monumentLevel;
  1019	        clan.monumentLevel = nextLevel;
  1020	        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
  1021	        return true;
  1022	    }
  1023	
  1024	    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
  1025	    function _completeMission(Clansman storage cs, Mission storage m) internal {
  1026	        cs.state = ClansmanState.WAITING;
  1027	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1028	        m.active = false;
  1029	        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
  1030	    }
  1031	
  1032	    // -------------------------------------------------------------------------
  1033	    // View-only settlement simulation
  1034	    // -------------------------------------------------------------------------
  1035	
  1036	    function _simulateSettleToTick(uint32 clanId, uint64 toTick)
  1037	        internal
  1038	        view
  1039	        returns (SettlementSimulation memory sim)
  1040	    {
  1041	        sim.clan = _clans[clanId];
  1042	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL) return sim;
  1043	
  1044	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
  1045	        sim.clansmen = new Clansman[](clansmanIds.length);
  1046	        sim.missions = new Mission[](clansmanIds.length);
  1047	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  1048	            uint32 clansmanId = clansmanIds[i];
  1049	            sim.clansmen[i] = _clansmen[clansmanId];
  1050	            sim.missions[i] = _missions[clansmanId];
  1051	        }
  1052	        sim.wheatPlots[0] = _wheatPlots[clanId][0];
  1053	        sim.wheatPlots[1] = _wheatPlots[clanId][1];
  1054	
  1055	        uint64 fromTick = sim.clan.lastSettledTick;
  1056	        if (fromTick >= toTick) return sim;
  1057	
  1058	        for (uint64 tick = fromTick; tick < toTick; tick++) {
  1059	            _simulateApplyUpkeep(sim, tick);
  1060	            if (sim.clan.clanState == ClanState.DEAD) break;
  1061	
  1062	            _simulateRegrowWheatPlots(sim, tick);
  1063	
  1064	            for (uint256 i = 0; i < sim.clansmen.length; i++) {
  1065	                _simulateSettleMissionForClansman(sim, i, tick, tick + 1);
  1066	            }
  1067	        }
  1068	
  1069	        sim.clan.lastSettledTick = toTick;
  1070	    }
  1071	
  1072	    function _simulateApplyUpkeep(SettlementSimulation memory sim, uint64 tick) internal view {
  1073	        if (sim.clan.livingClansmen == 0) return;
  1074	
  1075	        uint256 wheatNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
  1076	        uint256 fishNeeded = uint256(sim.clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
  1077	
  1078	        bool hadEnoughWheat = sim.clan.vaultWheat >= wheatNeeded;
  1079	        bool hadEnoughFish = sim.clan.vaultFish >= fishNeeded;
  1080	
  1081	        sim.clan.vaultWheat = hadEnoughWheat ? sim.clan.vaultWheat - wheatNeeded : 0;
  1082	        sim.clan.vaultFish = hadEnoughFish ? sim.clan.vaultFish - fishNeeded : 0;
  1083	
  1084	        bool starving = !hadEnoughWheat || !hadEnoughFish;
  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
  1086	            sim.clan.starvationStartsAtTick = tick;
  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
  1088	            sim.clan.starvationStartsAtTick = 0;
  1089	        }
  1090	
  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
  1092	            _simulateKillNextClansmanFromStarvation(sim);
  1093	        }
  1094	    }
  1095	
  1096	    function _simulateKillNextClansmanFromStarvation(SettlementSimulation memory sim) internal pure {
  1097	        if (sim.clan.livingClansmen == 0) return;
  1098	
  1099	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  1100	            if (sim.clansmen[i].state == ClansmanState.DEAD) continue;
  1101	
  1102	            _simulateMarkClansmanDead(sim, i);
  1103	            if (sim.clan.livingClansmen == 0) {
  1104	                _simulateMarkClanDead(sim);
  1105	            }
  1106	            return;
  1107	        }
  1108	    }
  1109	
  1110	    function _simulateMarkClansmanDead(SettlementSimulation memory sim, uint256 index) internal pure {
  1111	        if (sim.clansmen[index].state == ClansmanState.DEAD) return;
  1112	
  1113	        sim.clansmen[index].state = ClansmanState.DEAD;
  1114	        sim.clansmen[index].cooldownEndsAtTs = 0;
  1115	        if (sim.clan.livingClansmen > 0) {
  1116	            sim.clan.livingClansmen--;
  1117	        }
  1118	        if (sim.missions[index].active) {
  1119	            sim.missions[index].active = false;
  1120	        }
  1121	    }
  1122	
  1123	    function _simulateMarkClanDead(SettlementSimulation memory sim) internal pure {
  1124	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
  1125	
  1126	        sim.clan.clanState = ClanState.DEAD;
  1127	        sim.clan.vaultWood = 0;
  1128	        sim.clan.vaultWheat = 0;
  1129	        sim.clan.vaultFish = 0;
  1130	        sim.clan.vaultIron = 0;
  1131	        sim.clan.starvationStartsAtTick = 0;
  1132	        sim.clan.livingClansmen = 0;
  1133	
  1134	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  1135	            sim.clansmen[i].state = ClansmanState.DEAD;
  1136	            sim.clansmen[i].cooldownEndsAtTs = 0;
  1137	            if (sim.missions[i].active) {
  1138	                sim.missions[i].active = false;
  1139	            }
  1140	        }
  1141	    }
  1142	
  1143	    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
  1144	        for (uint256 pi = 0; pi < 2; pi++) {
  1145	            WheatPlot memory plot = sim.wheatPlots[pi];
  1146	            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
  1147	                plot.state = WheatPlotState.Harvestable;
  1148	                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
  1149	                plot.regrowUntilTick = 0;
  1150	                sim.wheatPlots[pi] = plot;
  1151	            }
  1152	        }
  1153	    }
  1154	
  1155	    function _simulateSettleMissionForClansman(
  1156	        SettlementSimulation memory sim,
  1157	        uint256 index,
  1158	        uint64 fromTick,
  1159	        uint64 toTick
  1160	    ) internal view {
  1161	        Clansman memory cs = sim.clansmen[index];
  1162	        Mission memory m = sim.missions[index];
  1163	
  1164	        if (cs.state == ClansmanState.DEAD) {
  1165	            if (m.active) {
  1166	                m.active = false;
  1167	            }
  1168	            sim.missions[index] = m;
  1169	            return;
  1170	        }
  1171	
  1172	        if (!m.active) return;
  1173	
  1174	        for (uint64 tick = fromTick; tick < toTick; tick++) {
  1175	            bytes32 tickSeed = _tickSeeds[tick];
  1176	
  1177	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
  1178	                cs.state = ClansmanState.ACTING;
  1179	                cs.currentRegion = m.targetRegion;
  1180	            }
  1181	
  1182	            if (m.action == ActionType.DefendBase) continue;
  1183	
  1184	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
  1185	                (cs, m) = _simulateResolveAction(sim, cs, m, tick, tickSeed);
  1186	                if (m.active && getActionDuration(m.action) > 0) {
  1187	                    (cs, m) = _simulateCompleteMission(cs, m);
  1188	                }
  1189	            }
  1190	
  1191	            if (!m.active) break;
  1192	        }
  1193	
  1194	        sim.clansmen[index] = cs;
  1195	        sim.missions[index] = m;
  1196	    }
  1197	
  1198	    function _simulateResolveAction(
  1199	        SettlementSimulation memory sim,
  1200	        Clansman memory cs,
  1201	        Mission memory m,
  1202	        uint64 tick,
  1203	        bytes32 tickSeed
  1204	    ) internal view returns (Clansman memory, Mission memory) {
  1205	        bool starving =
  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
  1207	        ActionType action = m.action;
  1208	
  1209	        if (action == ActionType.ChopWood) {
  1210	            (cs, m) = _simulateGatherWood(cs, m, tick, starving, tickSeed);
  1211	        } else if (action == ActionType.MineIron) {
  1212	            (cs, m) = _simulateGatherIron(sim, cs, m, tick, starving, tickSeed);
  1213	        } else if (action == ActionType.FishDocks) {
  1214	            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DOCKS_BPS);
  1215	        } else if (action == ActionType.FishDeepSea) {
  1216	            (cs, m) = _simulateGatherFish(cs, m, tick, starving, tickSeed, ClanWorldConstants.FISH_DEEP_BPS);
  1217	        } else if (action == ActionType.HarvestWheat) {
  1218	            (cs, m) = _simulateGatherWheat(sim, cs, m, tick, starving);
  1219	        } else if (action == ActionType.DepositResources) {
  1220	            (cs, m) = _simulateDoDeposit(sim, cs, m);
  1221	        } else if (
  1222	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
  1223	        ) {
  1224	            (cs, m) = _simulateDoBuilding(sim, cs, m, action);
  1225	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  1226	            (cs, m) = _simulateCompleteMission(cs, m);
  1227	        }
  1228	
  1229	        return (cs, m);
  1230	    }
  1231	
  1232	    function _simulateGatherWood(Clansman memory cs, Mission memory m, uint64 tick, bool starving, bytes32 tickSeed)
  1233	        internal
  1234	        view
  1235	        returns (Clansman memory, Mission memory)
  1236	    {
  1237	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
  1238	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1239	
  1240	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
  1241	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
  1242	        if (uint256(critRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
  1243	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
  1244	        }
  1245	        if (starving) yield = yield / 2;
  1246	        if (yield > remaining) yield = remaining;
  1247	        cs.carryWood += yield;
  1248	
  1249	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
  1250	            return _simulateCompleteMission(cs, m);
  1251	        }
  1252	        return (cs, m);
  1253	    }
  1254	
  1255	    function _simulateGatherIron(
  1256	        SettlementSimulation memory sim,
  1257	        Clansman memory cs,
  1258	        Mission memory m,
  1259	        uint64 tick,
  1260	        bool starving,
  1261	        bytes32 tickSeed
  1262	    ) internal view returns (Clansman memory, Mission memory) {
  1263	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
  1264	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1265	
  1266	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
  1267	        if (starving) yield = yield / 2;
  1268	        if (yield > remaining) yield = remaining;
  1269	        cs.carryIron += yield;
  1270	
  1271	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, cs.clansmanId, m.nonce, tick));
  1272	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
  1273	            sim.clan.goldBalance += ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
  1274	        }
  1275	
  1276	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
  1277	            return _simulateCompleteMission(cs, m);
  1278	        }
  1279	        return (cs, m);
  1280	    }

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1560,2120p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1560	        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
  1561	
  1562	        BanditState oldState = bandit.state;
  1563	        require(_isValidBanditTransition(bandit, newState), "ClanWorld: invalid bandit transition");
  1564	
  1565	        if (newState == BanditState.Defeated) {
  1566	            emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
  1567	            _deleteBandit(id);
  1568	            return;
  1569	        }
  1570	
  1571	        bandit.state = newState;
  1572	        bandit.tickEnteredState = _world.currentTick;
  1573	        if (newState != BanditState.Attacking) {
  1574	            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
  1575	        }
  1576	
  1577	        emit BanditStateChanged(id, oldState, newState, bandit.region, _world.currentTick);
  1578	    }
  1579	
  1580	    function _isValidBanditTransition(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1581	        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
  1582	        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
  1583	        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
  1584	        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
  1585	        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
  1586	        return false;
  1587	    }
  1588	
  1589	    function _canBanditLeaveSpawned(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1590	        return newState == BanditState.Escaped
  1591	            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
  1592	    }
  1593	
  1594	    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1595	        return newState == BanditState.Escaped
  1596	            || (newState == BanditState.Attacking
  1597	                && bandit.targetClanId != ClanWorldConstants.CLAN_ID_NULL
  1598	                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS);
  1599	    }
  1600	
  1601	    function _canBanditLeaveAttacking(BanditState newState) internal pure returns (bool) {
  1602	        return newState == BanditState.Defeated || newState == BanditState.Escaped;
  1603	    }
  1604	
  1605	    function _canBanditLeaveEscaped(BanditState newState) internal pure returns (bool) {
  1606	        return newState == BanditState.Resting;
  1607	    }
  1608	
  1609	    function _canBanditLeaveResting(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
  1610	        return newState == BanditState.Escaped
  1611	            || (newState == BanditState.Camped
  1612	                && _world.currentTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS);
  1613	    }
  1614	
  1615	    function _deleteBandit(uint32 id) internal {
  1616	        BanditTroop storage bandit = _bandits[id];
  1617	        uint8 region = bandit.region;
  1618	        uint32[] storage regionBandits = _banditsByRegion[region];
  1619	        for (uint256 i = 0; i < regionBandits.length; i++) {
  1620	            if (regionBandits[i] == id) {
  1621	                regionBandits[i] = regionBandits[regionBandits.length - 1];
  1622	                regionBandits.pop();
  1623	                break;
  1624	            }
  1625	        }
  1626	
  1627	        delete _bandits[id];
  1628	        if (_activeBanditCount > 0) {
  1629	            _activeBanditCount -= 1;
  1630	        }
  1631	        if (_world.activeBanditId == id) {
  1632	            _world.activeBanditId = _findOldestActiveBandit();
  1633	        }
  1634	    }
  1635	
  1636	    function _findOldestActiveBandit() internal view returns (uint32 oldestBanditId) {
  1637	        // V1 caps live troops at MAX_TOTAL_BANDITS = 8, so scanning the region
  1638	        // indexes is bounded even though storage mappings cannot be enumerated.
  1639	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1640	            uint32[] storage regionBandits = _banditsByRegion[region];
  1641	            for (uint256 i = 0; i < regionBandits.length; i++) {
  1642	                uint32 candidateId = regionBandits[i];
  1643	                BanditTroop storage candidate = _bandits[candidateId];
  1644	                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
  1645	                    continue;
  1646	                }
  1647	                if (oldestBanditId == ClanWorldConstants.BANDIT_ID_NULL || candidateId < oldestBanditId) {
  1648	                    oldestBanditId = candidateId;
  1649	                }
  1650	            }
  1651	        }
  1652	    }
  1653	
  1654	    function _advanceBanditStates(uint64 closedTick) internal {
  1655	        require(_world.currentTick == closedTick, "ClanWorld: bandit advance tick mismatch");
  1656	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1657	            uint32[] storage regionBandits = _banditsByRegion[region];
  1658	            for (uint256 i = 0; i < regionBandits.length; i++) {
  1659	                uint32 banditId = regionBandits[i];
  1660	                BanditTroop storage bandit = _bandits[banditId];
  1661	                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
  1662	                    _transitionBanditState(banditId, BanditState.Camped);
  1663	                } else if (
  1664	                    bandit.state == BanditState.Resting
  1665	                        && closedTick >= bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS
  1666	                ) {
  1667	                    _transitionBanditState(banditId, BanditState.Camped);
  1668	                }
  1669	            }
  1670	        }
  1671	    }
  1672	
  1673	    function _banditStrengthForLegacyEvent(uint32 strength) internal pure returns (uint16) {
  1674	        if (strength > type(uint16).max) return type(uint16).max;
  1675	        // forge-lint: disable-next-line(unsafe-typecast)
  1676	        return uint16(strength);
  1677	    }
  1678	
  1679	    function _resolveAttackingBandits(uint64 closedTick) internal {
  1680	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1681	            uint32[] storage regionBandits = _banditsByRegion[region];
  1682	            uint256 i = 0;
  1683	            while (i < regionBandits.length) {
  1684	                uint32 banditId = regionBandits[i];
  1685	                BanditTroop storage bandit = _bandits[banditId];
  1686	                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
  1687	                if (shouldResolve) {
  1688	                    _resolveBanditAttack(banditId, closedTick);
  1689	                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
  1690	                        continue;
  1691	                    }
  1692	                }
  1693	                i++;
  1694	            }
  1695	        }
  1696	    }
  1697	
  1698	    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
  1699	        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");
  1700	
  1701	        BanditTroop storage bandit = _bandits[banditId];
  1702	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
  1703	            return;
  1704	        }
  1705	        if (bandit.tickEnteredState != closedTick) {
  1706	            return;
  1707	        }
  1708	
  1709	        uint32 targetClanId = bandit.targetClanId;
  1710	        Clan storage targetClan = _clans[targetClanId];
  1711	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
  1712	            _transitionBanditState(banditId, BanditState.Escaped);
  1713	            emit BanditEscaped(banditId, closedTick);
  1714	            return;
  1715	        }
  1716	
  1717	        _settleClan(targetClanId);
  1718	        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
  1719	
  1720	        bytes32 tickSeed = _world.currentTickSeed;
  1721	        uint32 banditAttackPower = bandit.strength;
  1722	        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
  1723	        bool defeated = uint256(totalClansmanDefense) >= uint256(banditAttackPower) * 2;
  1724	
  1725	        uint32 wallDamage;
  1726	        uint32 baseAbsorbed;
  1727	        uint32 clansmanDamageAbsorbed;
  1728	        if (!defeated) {
  1729	            uint32 incomingDamage =
  1730	                banditAttackPower > totalClansmanDefense ? banditAttackPower - totalClansmanDefense : 0;
  1731	            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
  1732	            (incomingDamage, baseAbsorbed) = _applyBanditBaseDefense(targetClan, incomingDamage);
  1733	            clansmanDamageAbsorbed =
  1734	                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
  1735	        }
  1736	
  1737	        uint32 totalDefense = totalClansmanDefense + wallDamage + baseAbsorbed + clansmanDamageAbsorbed;
  1738	        emit BanditAttackResolved(
  1739	            banditId,
  1740	            targetClanId,
  1741	            defeated,
  1742	            _uint16Clamp(banditAttackPower),
  1743	            _uint16Clamp(totalDefense),
  1744	            targetClan.wallLevel,
  1745	            0,
  1746	            0,
  1747	            0,
  1748	            0,
  1749	            closedTick
  1750	        );
  1751	
  1752	        if (defeated) {
  1753	            emit BanditDefeated(banditId, targetClanId, closedTick);
  1754	            _distributeBanditLootToDefendingClans(banditId, targetClanId);
  1755	            targetClan.blueprintBalance += 1;
  1756	            emit BlueprintEarned(targetClanId, banditId, closedTick);
  1757	            _transitionBanditState(banditId, BanditState.Defeated);
  1758	        } else {
  1759	            _transitionBanditState(banditId, BanditState.Escaped);
  1760	            emit BanditEscaped(banditId, closedTick);
  1761	        }
  1762	    }
  1763	
  1764	    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
  1765	        BanditTroop storage bandit = _bandits[banditId];
  1766	        uint32[] memory rewardedClanIds = _activeDefendingClanIds(targetClanId);
  1767	        uint256 nDefendingClans = rewardedClanIds.length;
  1768	
  1769	        uint256 perWood;
  1770	        uint256 perIron;
  1771	        uint256 perWheat;
  1772	        uint256 perFish;
  1773	        uint256 perGold;
  1774	        if (nDefendingClans > 0) {
  1775	            perWood = _perClanBanditLootShare(bandit.carryWood, nDefendingClans);
  1776	            perIron = _perClanBanditLootShare(bandit.carryIron, nDefendingClans);
  1777	            perWheat = _perClanBanditLootShare(bandit.carryWheat, nDefendingClans);
  1778	            perFish = _perClanBanditLootShare(bandit.carryFish, nDefendingClans);
  1779	            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
  1780	
  1781	            for (uint256 i = 0; i < rewardedClanIds.length; i++) {
  1782	                Clan storage defenderClan = _clans[rewardedClanIds[i]];
  1783	                defenderClan.vaultWood += perWood;
  1784	                defenderClan.vaultIron += perIron;
  1785	                defenderClan.vaultWheat += perWheat;
  1786	                defenderClan.vaultFish += perFish;
  1787	                defenderClan.goldBalance += perGold;
  1788	            }
  1789	        }
  1790	
  1791	        emit LootDistributed(
  1792	            banditId,
  1793	            rewardedClanIds,
  1794	            perWood,
  1795	            perWheat,
  1796	            perFish,
  1797	            perIron,
  1798	            perGold,
  1799	            bandit.carryWood - (perWood * nDefendingClans),
  1800	            bandit.carryWheat - (perWheat * nDefendingClans),
  1801	            bandit.carryFish - (perFish * nDefendingClans),
  1802	            bandit.carryIron - (perIron * nDefendingClans),
  1803	            bandit.carryGold - (perGold * nDefendingClans)
  1804	        );
  1805	    }
  1806	
  1807	    function _perClanBanditLootShare(uint256 loot, uint256 nDefendingClans) internal pure returns (uint256) {
  1808	        if (nDefendingClans == 1) {
  1809	            return loot;
  1810	        }
  1811	        return ((loot / RESOURCE_UNIT) / nDefendingClans) * RESOURCE_UNIT;
  1812	    }
  1813	
  1814	    function _activeDefendingClanIds(uint32 targetClanId) internal view returns (uint32[] memory clanIds) {
  1815	        uint8 targetRegion = _clans[targetClanId].baseRegion;
  1816	        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
  1817	        uint256 count;
  1818	
  1819	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1820	            if (_clanHasActiveDefenderForTarget(defendingClans[i], targetClanId, targetRegion)) {
  1821	                count++;
  1822	            }
  1823	        }
  1824	
  1825	        clanIds = new uint32[](count);
  1826	        uint256 out;
  1827	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1828	            uint32 defenderClanId = defendingClans[i];
  1829	            if (_clanHasActiveDefenderForTarget(defenderClanId, targetClanId, targetRegion)) {
  1830	                clanIds[out++] = defenderClanId;
  1831	            }
  1832	        }
  1833	    }
  1834	
  1835	    function _clanHasActiveDefenderForTarget(uint32 defenderClanId, uint32 targetClanId, uint8 targetRegion)
  1836	        internal
  1837	        view
  1838	        returns (bool)
  1839	    {
  1840	        Clan storage defenderClan = _clans[defenderClanId];
  1841	        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
  1842	            return false;
  1843	        }
  1844	
  1845	        uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
  1846	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  1847	            uint32 clansmanId = clansmanIds[i];
  1848	            Clansman storage cs = _clansmen[clansmanId];
  1849	            Mission storage mission = _missions[clansmanId];
  1850	            if (
  1851	                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
  1852	                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
  1853	            ) {
  1854	                return true;
  1855	            }
  1856	        }
  1857	        return false;
  1858	    }
  1859	
  1860	    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
  1861	        internal
  1862	        view
  1863	        returns (uint32 totalDefense)
  1864	    {
  1865	        uint8 targetRegion = _clans[targetClanId].baseRegion;
  1866	        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
  1867	
  1868	        for (uint256 i = 0; i < defendingClans.length; i++) {
  1869	            uint32 defenderClanId = defendingClans[i];
  1870	            Clan storage defenderClan = _clans[defenderClanId];
  1871	            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
  1872	                continue;
  1873	            }
  1874	
  1875	            uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
  1876	            for (uint256 j = 0; j < clansmanIds.length; j++) {
  1877	                uint32 clansmanId = clansmanIds[j];
  1878	                Clansman storage cs = _clansmen[clansmanId];
  1879	                Mission storage mission = _missions[clansmanId];
  1880	                if (
  1881	                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
  1882	                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
  1883	                ) {
  1884	                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
  1885	                }
  1886	            }
  1887	        }
  1888	    }
  1889	
  1890	    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
  1891	        internal
  1892	        returns (uint32 remainingDamage, uint32 wallDamage)
  1893	    {
  1894	        remainingDamage = incomingDamage;
  1895	        if (remainingDamage == 0 || clan.wallLevel == 0) {
  1896	            return (remainingDamage, 0);
  1897	        }
  1898	
  1899	        wallDamage = remainingDamage < WALL_HP_PER_LEVEL ? remainingDamage : WALL_HP_PER_LEVEL;
  1900	        remainingDamage -= wallDamage;
  1901	        if (wallDamage >= WALL_HP_PER_LEVEL) {
  1902	            if (clan.wallLevel > 0) {
  1903	                clan.wallLevel--;
  1904	            }
  1905	            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
  1906	        }
  1907	    }
  1908	
  1909	    function _applyBanditBaseDefense(Clan storage clan, uint32 incomingDamage)
  1910	        internal
  1911	        view
  1912	        returns (uint32 remainingDamage, uint32 baseAbsorbed)
  1913	    {
  1914	        remainingDamage = incomingDamage;
  1915	        if (remainingDamage == 0 || clan.baseLevel == 0) {
  1916	            return (remainingDamage, 0);
  1917	        }
  1918	
  1919	        uint32 baseDefense = uint32(clan.baseLevel) * BASE_HP_PER_LEVEL;
  1920	        baseAbsorbed = remainingDamage < baseDefense ? remainingDamage : baseDefense;
  1921	        remainingDamage -= baseAbsorbed;
  1922	    }
  1923	
  1924	    function _applyBanditClansmanCasualties(
  1925	        Clan storage clan,
  1926	        uint32 clanId,
  1927	        uint32 banditId,
  1928	        uint32 incomingDamage,
  1929	        bytes32 tickSeed
  1930	    ) internal returns (uint32 damageAbsorbed) {
  1931	        uint32 remainingDamage = incomingDamage;
  1932	        uint32 killIndex = 0;
  1933	        while (remainingDamage > 0) {
  1934	            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
  1935	            if (victimId == 0) {
  1936	                break;
  1937	            }
  1938	
  1939	            Clansman storage victim = _clansmen[victimId];
  1940	            _markClansmanDead(clan, victim);
  1941	
  1942	            uint32 absorbed = remainingDamage < CLANSMAN_HP ? remainingDamage : CLANSMAN_HP;
  1943	            damageAbsorbed += absorbed;
  1944	            remainingDamage -= absorbed;
  1945	            emit ClansmanKilledByBandit(clanId, victimId, banditId);
  1946	            killIndex++;
  1947	
  1948	            if (clan.livingClansmen == 0) {
  1949	                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
  1950	                break;
  1951	            }
  1952	        }
  1953	    }
  1954	
  1955	    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
  1956	        internal
  1957	        view
  1958	        returns (uint32 victimId)
  1959	    {
  1960	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
  1961	        uint256 livingCount;
  1962	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  1963	            if (_clansmen[clansmanIds[i]].state != ClansmanState.DEAD) {
  1964	                livingCount++;
  1965	            }
  1966	        }
  1967	        if (livingCount == 0) {
  1968	            return 0;
  1969	        }
  1970	
  1971	        uint256 pick =
  1972	            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
  1973	        uint256 seen;
  1974	        for (uint256 i = 0; i < clansmanIds.length; i++) {
  1975	            uint32 candidateId = clansmanIds[i];
  1976	            if (_clansmen[candidateId].state == ClansmanState.DEAD) {
  1977	                continue;
  1978	            }
  1979	            if (seen == pick) {
  1980	                return candidateId;
  1981	            }
  1982	            seen++;
  1983	        }
  1984	    }
  1985	
  1986	    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)
  1987	        internal
  1988	        pure
  1989	        returns (uint32)
  1990	    {
  1991	        return uint32(
  1992	            uint256(keccak256(abi.encode("clansman_defense", tickSeed, banditId, clansmanId)))
  1993	                % (CLANSMAN_MAX_DEFENSE_DAMAGE + 1)
  1994	        );
  1995	    }
  1996	
  1997	    function _uint16Clamp(uint32 value) internal pure returns (uint16) {
  1998	        if (value > type(uint16).max) return type(uint16).max;
  1999	        return uint16(value);
  2000	    }
  2001	
  2002	    function _evaluateBanditSpawns(bytes32 tickSeed) internal {
  2003	        uint256[] memory regionWeights = _banditSpawnRegionWeights();
  2004	        if (_activeBanditCount >= MAX_TOTAL_BANDITS) {
  2005	            _refreshBanditSpawnWorldPreview(regionWeights);
  2006	            return;
  2007	        }
  2008	
  2009	        uint256[] memory candidateWeights = new uint256[](8);
  2010	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  2011	            uint256 weight = regionWeights[region - 1];
  2012	            if (weight == 0 || _banditsByRegion[region].length >= MAX_BANDITS_PER_REGION) {
  2013	                continue;
  2014	            }
  2015	
  2016	            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
  2017	            if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
  2018	                continue;
  2019	            }
  2020	
  2021	            spawnState.probabilityAccum = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
  2022	            if (_banditSpawnRollPasses(tickSeed, region, spawnState.probabilityAccum)) {
  2023	                candidateWeights[region - 1] = weight;
  2024	            }
  2025	        }
  2026	
  2027	        uint8 selectedRegion = _selectBanditSpawnRegion(tickSeed, candidateWeights);
  2028	        if (selectedRegion != ClanWorldConstants.REGION_NOOP) {
  2029	            // _spawnBandit resets only the selected region's accumulator; other
  2030	            // eligible regions retain their accumulated pressure for later ticks.
  2031	            _spawnBandit(selectedRegion, _banditSpawnStrength(tickSeed, selectedRegion));
  2032	        }
  2033	
  2034	        _refreshBanditSpawnWorldPreview(regionWeights);
  2035	    }
  2036	
  2037	    function _incrementBanditSpawnProbability(uint16 probabilityAccum) internal pure returns (uint16) {
  2038	        uint256 next = uint256(probabilityAccum) + BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS;
  2039	        if (next > BANDIT_SPAWN_MAX_PROBABILITY_BPS) {
  2040	            return BANDIT_SPAWN_MAX_PROBABILITY_BPS;
  2041	        }
  2042	        // forge-lint: disable-next-line(unsafe-typecast)
  2043	        return uint16(next);
  2044	    }
  2045	
  2046	    function _banditSpawnRollPasses(bytes32 tickSeed, uint8 region, uint16 probabilityAccum)
  2047	        internal
  2048	        pure
  2049	        returns (bool)
  2050	    {
  2051	        return _banditSpawnRoll(tickSeed, region) < uint256(probabilityAccum);
  2052	    }
  2053	
  2054	    function _banditSpawnRoll(bytes32 tickSeed, uint8 region) internal pure returns (uint256) {
  2055	        uint256 nonce = uint256(keccak256(abi.encodePacked("bandit_spawn", region)));
  2056	        return RNG.rngBounded(tickSeed, DOMAIN_BANDIT_SPAWN, nonce, 10000);
  2057	    }
  2058	
  2059	    function _selectBanditSpawnRegion(bytes32 tickSeed, uint256[] memory weights) internal pure returns (uint8) {
  2060	        uint256 selected = RNG.rngWeightedPick(
  2061	            tickSeed, DOMAIN_BANDIT_SPAWN, uint256(keccak256(abi.encodePacked("bandit_spawn_region"))), weights
  2062	        );
  2063	        if (weights.length == 0 || weights[selected] == 0) {
  2064	            return ClanWorldConstants.REGION_NOOP;
  2065	        }
  2066	        // forge-lint: disable-next-line(unsafe-typecast)
  2067	        return uint8(selected + 1);
  2068	    }
  2069	
  2070	    function _banditSpawnStrength(bytes32 tickSeed, uint8 region) internal pure returns (uint32) {
  2071	        uint256 nonce = uint256(keccak256(abi.encodePacked("bandit_spawn_strength", region)));
  2072	        uint256 roll = RNG.rngBounded(tickSeed, DOMAIN_BANDIT_SPAWN, nonce, BANDIT_SPAWN_STRENGTH_SPREAD);
  2073	        // forge-lint: disable-next-line(unsafe-typecast)
  2074	        return MIN_BANDIT_SPAWN_STRENGTH + uint32(roll);
  2075	    }
  2076	
  2077	    function _eagerSettleForBandits(uint64 closedTick) internal {
  2078	        require(_world.currentTick == closedTick, "ClanWorld: eager settle tick mismatch");
  2079	        if (_activeBanditCount >= MAX_TOTAL_BANDITS) return;
  2080	
  2081	        uint256[] memory regionWeights = _banditSpawnRegionWeights();
  2082	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  2083	            if (!_isBanditSpawnRegionCandidate(regionWeights, region)) {
  2084	                continue;
  2085	            }
  2086	            _eagerSettleBanditCandidateRegion(region);
  2087	        }
  2088	    }
  2089	
  2090	    function _isBanditSpawnRegionCandidate(uint256[] memory regionWeights, uint8 region) internal view returns (bool) {
  2091	        if (regionWeights[region - 1] == 0 || _banditsByRegion[region].length >= MAX_BANDITS_PER_REGION) {
  2092	            return false;
  2093	        }
  2094	
  2095	        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
  2096	        if (_world.currentTick < spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS) {
  2097	            return false;
  2098	        }
  2099	
  2100	        uint16 nextProbability = _incrementBanditSpawnProbability(spawnState.probabilityAccum);
  2101	        return _banditSpawnRollPasses(_world.currentTickSeed, region, nextProbability);
  2102	    }
  2103	
  2104	    function _eagerSettleBanditCandidateRegion(uint8 region) internal {
  2105	        uint256 clanScanCount = _allClanIds.length < MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION
  2106	            ? _allClanIds.length
  2107	            : MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION;
  2108	
  2109	        for (uint256 i = 0; i < clanScanCount; i++) {
  2110	            uint32 clanId = _allClanIds[i];
  2111	            Clan storage clan = _clans[clanId];
  2112	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
  2113	                continue;
  2114	            }
  2115	
  2116	            _settleClan(clanId);
  2117	            _eagerSettleActiveDefendersForBase(clanId, region);
  2118	        }
  2119	    }
  2120	

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2200,2360p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2200	                _activeBanditCount < MAX_TOTAL_BANDITS && regionWeights[region - 1] > 0
  2201	                    && _banditsByRegion[region].length < MAX_BANDITS_PER_REGION && eligibleTick < nextEligibleTick
  2202	            ) {
  2203	                nextEligibleTick = eligibleTick;
  2204	            }
  2205	            if (spawnState.probabilityAccum > maxChance) {
  2206	                maxChance = spawnState.probabilityAccum;
  2207	            }
  2208	        }
  2209	
  2210	        _world.nextBanditSpawnEligibleTick = nextEligibleTick == type(uint64).max ? 0 : nextEligibleTick;
  2211	        _world.currentBanditSpawnChanceBps = maxChance;
  2212	    }
  2213	
  2214	    // =========================================================================
  2215	    // WORLD PROGRESSION
  2216	    // =========================================================================
  2217	
  2218	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
  2219	    ///         Execution order per spec §4.2 (CEI-safe):
  2220	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
  2221	    ///         1. Settle missions completing this tick.
  2222	    ///         2. Execute scheduled market actions for closedTick (external calls).
  2223	    ///         3. Eager-settle clans touched by world events (Phase 9 stub).
  2224	    ///         4. Advance bandit timers and resolve closed-tick bandit/world events.
  2225	    ///         5. Increment tick and publish the next tick seed atomically.
  2226	    function heartbeat() external override nonReentrant {
  2227	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
  2228	
  2229	        uint64 closedTick = _world.currentTick;
  2230	        bytes32 closedTickSeed = _world.currentTickSeed;
  2231	
  2232	        // CEI: update rate-limit guard before any external calls
  2233	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
  2234	
  2235	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
  2236	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  2237	        _settleCompletingMissions(closedTick);
  2238	
  2239	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
  2240	        _executeScheduledMarketActions(closedTick);
  2241	
  2242	        // Step 3: Eager-settle bases and active defenders in bandit spawn-candidate regions.
  2243	        _eagerSettleForBandits(closedTick);
  2244	
  2245	        // Step 4: Advance deterministic bandit timers for the closed tick.
  2246	        _advanceBanditStates(closedTick);
  2247	
  2248	        // Step 5: Resolve deterministic bandit attacks for the closed tick.
  2249	        _resolveAttackingBandits(closedTick);
  2250	
  2251	        // Step 6: Evaluate deterministic bandit spawns for the closed tick.
  2252	        _evaluateBanditSpawns(closedTickSeed);
  2253	
  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
  2255	        _resolveWorldEvents(closedTick);
  2256	
  2257	        // Step 8: Increment tick and publish the opened tick seed as one visible state transition.
  2258	        uint64 newTick = closedTick + 1;
  2259	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick));
  2260	        _world.currentTick = newTick;
  2261	        _tickSeeds[newTick] = newSeed;
  2262	        _world.currentTickSeed = newSeed;
  2263	        _world.nextHeartbeatAtTick = newTick + 1;
  2264	
  2265	        emit TickAdvanced(closedTick, newTick, newSeed);
  2266	    }
  2267	
  2268	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
  2269	    ///      Called from heartbeat before market execution and tick increment.
  2270	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  2271	    function _settleCompletingMissions(uint64 tick) internal {
  2272	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2273	            uint32 clanId = _allClanIds[i];
  2274	            Clan storage clan = _clans[clanId];
  2275	            if (clan.clanState == ClanState.DEAD) continue;
  2276	
  2277	            uint32[] storage csIds = _clanClansmanIds[clanId];
  2278	            for (uint256 j = 0; j < csIds.length; j++) {
  2279	                Clansman storage cs = _clansmen[csIds[j]];
  2280	                if (cs.state == ClansmanState.DEAD) continue;
  2281	
  2282	                Mission storage m = _missions[cs.clansmanId];
  2283	                if (!m.active) continue;
  2284	                if (m.settlesAtTick != tick) continue; // not due this tick
  2285	
  2286	                // Settle this mission using the single-tick range [tick, tick+1).
  2287	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
  2288	            }
  2289	        }
  2290	    }
  2291	
  2292	    /// @dev Resolve world events for the tick that was just closed.
  2293	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
  2294	    function _resolveWorldEvents(uint64 closedTick) internal {
  2295	        uint64 newTick = closedTick + 1;
  2296	
  2297	        // --- season boundary ---
  2298	        if (newTick >= _world.seasonEndTick) {
  2299	            _world.currentSeasonNumber += 1;
  2300	            _world.seasonStartTick = _world.seasonEndTick;
  2301	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
  2302	            // reset winter timers for new season
  2303	            _world.winterActive = false;
  2304	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
  2305	                - ClanWorldConstants.WINTER_DURATION_TICKS;
  2306	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
  2307	        }
  2308	
  2309	        // --- winter transitions (timer only; mechanics = Phase 10) ---
  2310	        if (
  2311	            !_world.winterActive && newTick >= _world.winterStartsAtTick
  2312	                && _world.winterStartsAtTick < _world.seasonEndTick
  2313	        ) {
  2314	            _world.winterActive = true;
  2315	            emit WinterStarted(newTick);
  2316	        }
  2317	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
  2318	            _world.winterActive = false;
  2319	            emit WinterEnded(newTick);
  2320	            // schedule next winter cycle within this season
  2321	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
  2322	                - ClanWorldConstants.WINTER_DURATION_TICKS;
  2323	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
  2324	            if (nextWinterStart < _world.seasonEndTick) {
  2325	                _world.winterStartsAtTick = nextWinterStart;
  2326	                _world.winterEndsAtTick = nextWinterEnd;
  2327	            } else {
  2328	                // no more winters this season; sentinel = seasonEndTick so guard never fires
  2329	                _world.winterStartsAtTick = _world.seasonEndTick;
  2330	                _world.winterEndsAtTick = _world.seasonEndTick;
  2331	            }
  2332	        }
  2333	    }
  2334	
  2335	    /// @notice Public settlement trigger — lazily settle a clan.
  2336	    function settleClan(uint32 clanId) external override nonReentrant {
  2337	        _settleClan(clanId);
  2338	    }
  2339	
  2340	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
  2341	    ///         Internally settles the entire clan (including upkeep) to guarantee
  2342	    ///         correct ordering and prevent double-settlement. Callers may call this
  2343	    ///         or settleClan interchangeably; both are safe and idempotent.
  2344	    function settleClansman(uint32 csId) external override nonReentrant {
  2345	        Clansman storage cs = _clansmen[csId];
  2346	        if (cs.clansmanId == 0) return;
  2347	        _settleClan(cs.clanId);
  2348	    }
  2349	
  2350	    /// @notice Finalize season. Phase 1 stub.
  2351	    function finalizeSeason() external override {
  2352	        // TODO Phase 3
  2353	    }
  2354	
  2355	    // =========================================================================
  2356	    // CLAN LIFECYCLE
  2357	    // =========================================================================
  2358	
  2359	    /// @notice Mint a new clan and spawn its homebase.
  2360	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '520,620p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   520	        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
   521	
   522	        uint8 baseRegion = clan.baseRegion;
   523	        clan.clanState = ClanState.DEAD;
   524	        clan.vaultWood = 0;
   525	        clan.vaultWheat = 0;
   526	        clan.vaultFish = 0;
   527	        clan.vaultIron = 0;
   528	        clan.starvationStartsAtTick = 0;
   529	        clan.livingClansmen = 0;
   530	
   531	        uint32[] storage csIds = _clanClansmanIds[clanId];
   532	        for (uint256 i = 0; i < csIds.length; i++) {
   533	            Clansman storage cs = _clansmen[csIds[i]];
   534	            cs.state = ClansmanState.DEAD;
   535	            cs.cooldownEndsAtTs = 0;
   536	            Mission storage mission = _missions[csIds[i]];
   537	            if (mission.active) {
   538	                if (mission.action == ActionType.DefendBase) {
   539	                    _clearDefender(csIds[i]);
   540	                }
   541	                mission.active = false;
   542	            }
   543	        }
   544	
   545	        _releaseDefendersForDeadTarget(clanId, baseRegion);
   546	        _abortBanditAttacksForDeadTarget(clanId, excludedBanditId);
   547	
   548	        emit ClanEliminated(clanId, tick);
   549	    }
   550	
   551	    function _releaseDefendersForDeadTarget(uint32 deadClanId, uint8 baseRegion) internal {
   552	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   553	            uint32 defenderClanId = _allClanIds[i];
   554	            if (defenderClanId == deadClanId) continue;
   555	
   556	            uint32[] storage csIds = _clanClansmanIds[defenderClanId];
   557	            for (uint256 j = 0; j < csIds.length; j++) {
   558	                uint32 clansmanId = csIds[j];
   559	                Mission storage mission = _missions[clansmanId];
   560	                if (
   561	                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
   562	                        && _clansmanDefendingRegion[clansmanId] == baseRegion
   563	                ) {
   564	                    _clearDefender(clansmanId);
   565	                    mission.active = false;
   566	
   567	                    Clansman storage defender = _clansmen[clansmanId];
   568	                    if (defender.state != ClansmanState.DEAD) {
   569	                        defender.state = ClansmanState.WAITING;
   570	                    }
   571	                }
   572	            }
   573	        }
   574	    }
   575	
   576	    function _abortBanditAttacksForDeadTarget(uint32 deadClanId, uint32 excludedBanditId) internal {
   577	        // Match _transitionBanditState's event stamp; heartbeat keeps currentTick
   578	        // equal to the closed tick while aborting linked bandit attacks.
   579	        uint64 currentTick = _world.currentTick;
   580	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
   581	            uint32[] storage regionBandits = _banditsByRegion[region];
   582	            for (uint256 i = 0; i < regionBandits.length; i++) {
   583	                uint32 banditId = regionBandits[i];
   584	                if (banditId == excludedBanditId) continue;
   585	
   586	                BanditTroop storage bandit = _bandits[banditId];
   587	                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
   588	                    _transitionBanditState(banditId, BanditState.Escaped);
   589	                    emit BanditEscaped(banditId, currentTick);
   590	                    emit BanditTargetDied(banditId, deadClanId, currentTick);
   591	                }
   592	            }
   593	        }
   594	    }
   595	
   596	    /// @dev Check if a clan is currently starving (lazy read).
   597	    function _isStarving(Clan storage clan) internal view returns (bool) {
   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   599	    }
   600	
   601	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   602	    function _resolveAction(
   603	        Clan storage clan,
   604	        Clansman storage cs,
   605	        Mission storage m,
   606	        uint32 clanId,
   607	        uint64 tick,
   608	        bytes32 tickSeed
   609	    ) internal {
   610	        bool starving = _isStarving(clan);
   611	        ActionType action = m.action;
   612	
   613	        if (action == ActionType.ChopWood) {
   614	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   615	        } else if (action == ActionType.MineIron) {
   616	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   617	        } else if (action == ActionType.FishDocks) {
   618	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   619	        } else if (action == ActionType.FishDeepSea) {
   620	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1510,1565p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1510	    }
  1511	
  1512	    // =========================================================================
  1513	    // BANDIT STATE MACHINE
  1514	    // =========================================================================
  1515	
  1516	    function _spawnBandit(uint8 region, uint32 strength) internal returns (uint32 id) {
  1517	        require(
  1518	            region >= ClanWorldConstants.REGION_FOREST && region <= ClanWorldConstants.REGION_DEEP_SEA,
  1519	            "ClanWorld: invalid bandit region"
  1520	        );
  1521	        require(strength > 0, "ClanWorld: invalid bandit strength");
  1522	
  1523	        id = _nextBanditId++;
  1524	        _bandits[id] = BanditTroop({
  1525	            id: id,
  1526	            region: region,
  1527	            state: BanditState.Spawned,
  1528	            targetClanId: 0,
  1529	            tickEnteredState: _world.currentTick,
  1530	            strength: strength,
  1531	            carryWood: 0,
  1532	            carryIron: 0,
  1533	            carryWheat: 0,
  1534	            carryFish: 0,
  1535	            carryGold: 0
  1536	        });
  1537	        _banditsByRegion[region].push(id);
  1538	        _activeBanditCount += 1;
  1539	
  1540	        BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
  1541	        spawnState.lastSpawnTick = _world.currentTick;
  1542	        spawnState.probabilityAccum = 0;
  1543	
  1544	        if (_world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
  1545	            _world.activeBanditId = id;
  1546	        }
  1547	
  1548	        emit BanditSpawned(id, region, 0, _banditStrengthForLegacyEvent(strength));
  1549	    }
  1550	
  1551	    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
  1552	        require(targetClanId != ClanWorldConstants.CLAN_ID_NULL, "ClanWorld: invalid bandit target");
  1553	        _bandits[id].targetClanId = targetClanId;
  1554	        _transitionBanditState(id, BanditState.Attacking);
  1555	    }
  1556	
  1557	    function _transitionBanditState(uint32 id, BanditState newState) internal {
  1558	        BanditTroop storage bandit = _bandits[id];
  1559	        require(bandit.id != ClanWorldConstants.BANDIT_ID_NULL, "ClanWorld: bandit not found");
  1560	        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
  1561	
  1562	        BanditState oldState = bandit.state;
  1563	        require(_isValidBanditTransition(bandit, newState), "ClanWorld: invalid bandit transition");
  1564	
  1565	        if (newState == BanditState.Defeated) {

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2120,2205p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2120	
  2121	    function _eagerSettleActiveDefendersForBase(uint32 targetClanId, uint8 region) internal {
  2122	        uint32[] storage defendingClans = _defendingClansByRegion[region];
  2123	        uint256 defendingClanScanCount = defendingClans.length < MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION
  2124	            ? defendingClans.length
  2125	            : MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION;
  2126	        uint256 defendersScanned;
  2127	
  2128	        for (uint256 i = 0; i < defendingClanScanCount; i++) {
  2129	            uint32 defenderClanId = defendingClans[i];
  2130	            uint32[] storage clansmanIds = _clanClansmanIds[defenderClanId];
  2131	
  2132	            for (
  2133	                uint256 j = 0;
  2134	                j < clansmanIds.length && defendersScanned < MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION;
  2135	                j++
  2136	            ) {
  2137	                defendersScanned += 1;
  2138	                Mission storage mission = _missions[clansmanIds[j]];
  2139	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2140	                    _settleClan(defenderClanId);
  2141	                    break;
  2142	                }
  2143	            }
  2144	
  2145	            if (defendersScanned >= MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION) {
  2146	                break;
  2147	            }
  2148	        }
  2149	    }
  2150	
  2151	    function _banditSpawnRegionWeights() internal view returns (uint256[] memory weights) {
  2152	        weights = new uint256[](8);
  2153	        uint256 clanCount = _allClanIds.length;
  2154	        if (clanCount == 0) {
  2155	            return weights;
  2156	        }
  2157	
  2158	        uint256 scanCount = clanCount < MAX_BANDIT_SPAWN_SCAN_PER_REGION ? clanCount : MAX_BANDIT_SPAWN_SCAN_PER_REGION;
  2159	        uint256 startIndex = uint256(_world.currentTick) % clanCount;
  2160	        uint256 clansmenScanned;
  2161	        for (uint256 i = 0; i < scanCount; i++) {
  2162	            Clan storage clan = _clans[_allClanIds[(startIndex + i) % clanCount]];
  2163	            if (clan.clanState == ClanState.DEAD) {
  2164	                continue;
  2165	            }
  2166	
  2167	            if (
  2168	                clan.baseRegion >= ClanWorldConstants.REGION_FOREST
  2169	                    && clan.baseRegion <= ClanWorldConstants.REGION_DEEP_SEA
  2170	            ) {
  2171	                weights[clan.baseRegion - 1] += 100 + (_lootValueRaw(clan) / 1e18);
  2172	            }
  2173	
  2174	            uint32[] storage clansmanIds = _clanClansmanIds[clan.clanId];
  2175	            for (
  2176	                uint256 j = 0;
  2177	                j < clansmanIds.length && clansmenScanned < MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION;
  2178	                j++
  2179	            ) {
  2180	                clansmenScanned += 1;
  2181	                Clansman storage cs = _clansmen[clansmanIds[j]];
  2182	                if (
  2183	                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
  2184	                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
  2185	                ) {
  2186	                    weights[cs.currentRegion - 1] += 25;
  2187	                }
  2188	            }
  2189	        }
  2190	    }
  2191	
  2192	    function _refreshBanditSpawnWorldPreview(uint256[] memory regionWeights) internal {
  2193	        uint64 nextEligibleTick = type(uint64).max;
  2194	        uint16 maxChance = 0;
  2195	
  2196	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  2197	            BanditSpawnState storage spawnState = _banditSpawnByRegion[region];
  2198	            uint64 eligibleTick = spawnState.lastSpawnTick + MIN_SPAWN_COOLDOWN_TICKS;
  2199	            if (
  2200	                _activeBanditCount < MAX_TOTAL_BANDITS && regionWeights[region - 1] > 0
  2201	                    && _banditsByRegion[region].length < MAX_BANDITS_PER_REGION && eligibleTick < nextEligibleTick
  2202	            ) {
  2203	                nextEligibleTick = eligibleTick;
  2204	            }
  2205	            if (spawnState.probabilityAccum > maxChance) {

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '3040,3140p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3040	
  3041	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  3042	        internal
  3043	        view
  3044	        returns (StatusCode)
  3045	    {
  3046	        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
  3047	        Clan storage targetClan = _clans[targetClanId];
  3048	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
  3049	            return StatusCode.ERR_INVALID_TARGET;
  3050	        }
  3051	        if (gotoRegion != targetClan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  3052	        return StatusCode.OK;
  3053	    }
  3054	
  3055	    // =========================================================================
  3056	    // TREASURY / POOL SEEDING
  3057	    // =========================================================================
  3058	
  3059	    /// @notice One-time treasury initialization: register token and pool addresses.
  3060	    ///         Must be called before seedPools. Callable only once.
  3061	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  3062	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  3063	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  3064	
  3065	        _treasury.woodToken = tokens[0];
  3066	        _treasury.ironToken = tokens[1];
  3067	        _treasury.wheatToken = tokens[2];
  3068	        _treasury.fishToken = tokens[3];
  3069	        _treasury.goldToken = tokens[4];
  3070	        _treasury.blueprintToken = tokens[5];
  3071	
  3072	        _treasury.woodGoldPool = pools[0];
  3073	        _treasury.wheatGoldPool = pools[1];
  3074	        _treasury.fishGoldPool = pools[2];
  3075	        _treasury.ironGoldPool = pools[3];
  3076	    }
  3077	
  3078	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  3079	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  3080	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  3081	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  3082	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  3083	
  3084	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  3085	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  3086	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  3087	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  3088	
  3089	        _treasury.poolsSeeded = true;
  3090	
  3091	        emit PoolsSeeded(
  3092	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  3093	        );
  3094	    }
  3095	
  3096	    // =========================================================================
  3097	    // OTC TRANSFERS
  3098	    // =========================================================================
  3099	
  3100	    function transferGold(uint32, uint32, uint256) external pure override {
  3101	        revert("OTC transfers not implemented");
  3102	    }
  3103	
  3104	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  3105	        revert("OTC transfers not implemented");
  3106	    }
  3107	
  3108	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  3109	        revert("OTC transfers not implemented");
  3110	    }
  3111	
  3112	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  3113	        external
  3114	        pure
  3115	        override
  3116	    {
  3117	        revert("OTC transfers not implemented");
  3118	    }
  3119	
  3120	    // =========================================================================
  3121	    // RAW READ GETTERS
  3122	    // =========================================================================
  3123	
  3124	    function getWorldState() external view override returns (WorldState memory) {
  3125	        return _world;
  3126	    }
  3127	
  3128	    function getTreasuryState() external view override returns (TreasuryState memory) {
  3129	        return _treasury;
  3130	    }
  3131	
  3132	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  3133	        return _clans[clanId];
  3134	    }
  3135	
  3136	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  3137	        return _clansmen[clansmanId];
  3138	    }
  3139	
  3140	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {

exec
/bin/bash -lc 'git show 23f7f1a:packages/contracts/src/lib/RNG.sol | nl -ba' in /home/claude/code/clan-world
 succeeded in 4ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	/// @title ClanWorld domain-separated RNG helpers
     5	/// @notice Cheap deterministic randomness derived from a published per-tick seed.
     6	library RNG {
     7	    uint256 internal constant DOMAIN_MISSION_RESOLUTION = uint256(keccak256("clanworld.mission.resolve.v1"));
     8	    uint256 internal constant DOMAIN_BANDIT_SPAWN = uint256(keccak256("clanworld.bandit.spawn.v1"));
     9	    uint256 internal constant DOMAIN_MARKET_NOISE = uint256(keccak256("clanworld.market.noise.v1"));
    10	    uint256 internal constant DOMAIN_WEATHER = uint256(keccak256("clanworld.weather.v1"));
    11	    uint256 internal constant DOMAIN_FAIR_ITERATION = uint256(keccak256("clanworld.fair.iteration.v1"));
    12	
    13	    uint256 internal constant MAX_SHUFFLE_N = 64;
    14	
    15	    error ShuffleSizeTooLarge(uint256 n, uint256 max);
    16	
    17	    /// @notice Returns a uniform uint256 in [0, 2^256).
    18	    function rngUniform(bytes32 seed, uint256 domainSalt, uint256 nonce) internal pure returns (uint256) {
    19	        return uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce)));
    20	    }
    21	
    22	    /// @notice Returns a uniform value in [0, max) using rejection sampling.
    23	    /// @dev Returns 0 when max is 0 so callers can handle optional random choices cheaply.
    24	    function rngBounded(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256 max) internal pure returns (uint256) {
    25	        if (max == 0) {
    26	            return 0;
    27	        }
    28	
    29	        uint256 remainder;
    30	        unchecked {
    31	            // Computes 2^256 % max without trying to represent 2^256 directly.
    32	            remainder = (uint256(0) - max) % max;
    33	        }
    34	        uint256 maxValid = type(uint256).max - remainder;
    35	        uint256 attempt = 0;
    36	
    37	        while (true) {
    38	            uint256 value = uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce, max, attempt)));
    39	            if (value <= maxValid) {
    40	                return value % max;
    41	            }
    42	
    43	            unchecked {
    44	                attempt++;
    45	            }
    46	        }
    47	
    48	        revert("RNG: unreachable");
    49	    }
    50	
    51	    /// @notice Returns true with 50% probability.
    52	    function rngBool(bytes32 seed, uint256 domainSalt, uint256 nonce) internal pure returns (bool) {
    53	        return rngUniform(seed, domainSalt, nonce) & 1 == 1;
    54	    }
    55	
    56	    /// @notice Picks an index in proportion to each weight.
    57	    /// @dev Returns 0 for empty arrays and zero-total arrays.
    58	    function rngWeightedPick(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256[] memory weights)
    59	        internal
    60	        pure
    61	        returns (uint256 index)
    62	    {
    63	        uint256 total = 0;
    64	        for (uint256 i = 0; i < weights.length; i++) {
    65	            total += weights[i];
    66	        }
    67	
    68	        if (total == 0) {
    69	            return 0;
    70	        }
    71	
    72	        uint256 pick = rngBounded(seed, domainSalt, nonce, total);
    73	        uint256 cumulative = 0;
    74	        for (uint256 i = 0; i < weights.length; i++) {
    75	            cumulative += weights[i];
    76	            if (pick < cumulative) {
    77	                return i;
    78	            }
    79	        }
    80	
    81	        return weights.length - 1;
    82	    }
    83	
    84	    /// @notice Returns a Fisher-Yates permutation of [0, n).
    85	    /// @dev Reverts above MAX_SHUFFLE_N to keep callers away from accidental gas blowups.
    86	    function rngShuffle(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256 n)
    87	        internal
    88	        pure
    89	        returns (uint8[] memory permutation)
    90	    {
    91	        if (n > MAX_SHUFFLE_N) {
    92	            revert ShuffleSizeTooLarge(n, MAX_SHUFFLE_N);
    93	        }
    94	
    95	        permutation = new uint8[](n);
    96	        for (uint256 i = 0; i < n; i++) {
    97	            // casting is safe because n is bounded by MAX_SHUFFLE_N (64).
    98	            // forge-lint: disable-next-line(unsafe-typecast)
    99	            permutation[i] = uint8(i);
   100	        }
   101	
   102	        for (uint256 i = n; i > 1; i--) {
   103	            uint256 stepNonce = uint256(keccak256(abi.encodePacked(nonce, n, i)));
   104	            uint256 j = rngBounded(seed, domainSalt, stepNonce, i);
   105	            uint8 tmp = permutation[i - 1];
   106	            permutation[i - 1] = permutation[j];
   107	            permutation[j] = tmp;
   108	        }
   109	    }
   110	}

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/test/BanditSpawn.t.sol | nl -ba | sed -n '150,380p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   150	        Clan memory clan = target.getClan(clanId);
   151	        workerId = _csId(target, clanId, 0);
   152	        defenderId = _csId(target, clanId, 1);
   153	
   154	        OrderResult[] memory results =
   155	            target.submitClanOrders(clanId, _ordersForPendingWorkerAndDefender(workerId, defenderId, clan.baseRegion));
   156	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "worker order accepted");
   157	        assertEq(uint8(results[1].status), uint8(StatusCode.OK), "defender order accepted");
   158	    }
   159	
   160	    function _blockNonForestSpawnRegions(BanditSpawnHarness target) internal {
   161	        uint64 currentTick = target.getWorldState().currentTick;
   162	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
   163	            if (region != ClanWorldConstants.REGION_FOREST) {
   164	                target.setBanditSpawnState(region, currentTick, 0);
   165	            }
   166	        }
   167	    }
   168	
   169	    function _prevrandaoForNextForestSpawn(BanditSpawnHarness target) internal view returns (bytes32) {
   170	        WorldState memory state = target.getWorldState();
   171	        for (uint256 i = 1; i < 256; i++) {
   172	            bytes32 nextRandao = keccak256(abi.encodePacked("forest-spawn-randao", i));
   173	            bytes32 nextSeed = keccak256(abi.encode(nextRandao, state.currentTickSeed, state.currentTick));
   174	            if (target.banditSpawnRoll(nextSeed, ClanWorldConstants.REGION_FOREST) < 8000) {
   175	                return nextRandao;
   176	            }
   177	        }
   178	        revert("missing forest spawn randao");
   179	    }
   180	
   181	    function _missSeed(uint8 region, uint16 probability) internal view returns (bytes32) {
   182	        for (uint256 i = 0; i < 256; i++) {
   183	            bytes32 seed = keccak256(abi.encodePacked("miss", i));
   184	            if (world.banditSpawnRoll(seed, region) >= probability) {
   185	                return seed;
   186	            }
   187	        }
   188	        revert("missing miss seed");
   189	    }
   190	
   191	    function _hitBothSeed(uint8 firstRegion, uint8 secondRegion, uint16 probability) internal view returns (bytes32) {
   192	        for (uint256 i = 0; i < 256; i++) {
   193	            bytes32 seed = keccak256(abi.encodePacked("hit-both", i));
   194	            if (
   195	                world.banditSpawnRoll(seed, firstRegion) < probability
   196	                    && world.banditSpawnRoll(seed, secondRegion) < probability
   197	            ) {
   198	                return seed;
   199	            }
   200	        }
   201	        revert("missing hit seed");
   202	    }
   203	
   204	    function test_cooldownEnforcedAfterSpawn() public {
   205	        _mintForestClan(world);
   206	        world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
   207	
   208	        _advanceTicks(world, world.minSpawnCooldownTicks() - 1);
   209	
   210	        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
   211	        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
   212	        assertEq(probabilityAccum, 0, "cooldown does not accumulate chance");
   213	    }
   214	
   215	    function test_probabilityRisesAfterCooldown() public {
   216	        _advancePastInitialCooldown(world);
   217	        _mintForestClan(world);
   218	
   219	        uint16 increment = world.spawnProbabilityIncrementBps();
   220	        world.evaluateBanditSpawns(_missSeed(ClanWorldConstants.REGION_FOREST, increment));
   221	
   222	        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
   223	        assertEq(probabilityAccum, increment, "first eligible tick increments accumulator");
   224	    }
   225	
   226	    function test_spawnResetsOnlySelectedRegionAccumulator() public {
   227	        _advancePastInitialCooldown(world);
   228	        world.mintClan(address(0xF0));
   229	        world.mintClan(address(0xB0));
   230	
   231	        uint16 oneStepBelowCap = 7000;
   232	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, oneStepBelowCap);
   233	        world.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, oneStepBelowCap);
   234	
   235	        world.evaluateBanditSpawns(
   236	            _hitBothSeed(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_MOUNTAINS, 8000)
   237	        );
   238	
   239	        uint8 selectedRegion = world.getBandit(1).region;
   240	        uint8 otherRegion = selectedRegion == ClanWorldConstants.REGION_FOREST
   241	            ? ClanWorldConstants.REGION_MOUNTAINS
   242	            : ClanWorldConstants.REGION_FOREST;
   243	        (, uint16 selectedAccum) = world.getBanditSpawnState(selectedRegion);
   244	        (, uint16 otherAccum) = world.getBanditSpawnState(otherRegion);
   245	
   246	        assertEq(selectedAccum, 0, "selected region reset");
   247	        assertEq(otherAccum, 8000, "unselected candidate retained accum");
   248	    }
   249	
   250	    function test_perRegionCapEnforced() public {
   251	        _advancePastInitialCooldown(world);
   252	        _mintForestClan(world);
   253	
   254	        uint8 maxPerRegion = world.maxBanditsPerRegion();
   255	        for (uint8 i = 0; i < maxPerRegion; i++) {
   256	            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
   257	        }
   258	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   259	
   260	        world.evaluateBanditSpawns(keccak256("per-region-cap"));
   261	
   262	        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
   263	    }
   264	
   265	    function test_globalCapEnforced() public {
   266	        _advancePastInitialCooldown(world);
   267	        _mintForestClan(world);
   268	
   269	        uint8 maxTotal = world.maxTotalBandits();
   270	        for (uint8 i = 0; i < maxTotal; i++) {
   271	            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
   272	        }
   273	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   274	
   275	        world.evaluateBanditSpawns(keccak256("global-cap"));
   276	
   277	        assertEq(world.activeBanditCount(), maxTotal, "global cap");
   278	    }
   279	
   280	    function test_globalCapRefreshesPreviewOnHeartbeat() public {
   281	        _mintForestClan(world);
   282	
   283	        uint8 maxTotal = world.maxTotalBandits();
   284	        for (uint8 i = 0; i < maxTotal; i++) {
   285	            world.spawnBandit(uint8(ClanWorldConstants.REGION_FOREST + (i % 8)), 100 + i);
   286	        }
   287	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 4321);
   288	
   289	        _advanceTick(world);
   290	
   291	        WorldState memory state = world.getWorldState();
   292	        assertEq(world.activeBanditCount(), maxTotal, "still at cap");
   293	        assertEq(state.nextBanditSpawnEligibleTick, 0, "no eligible tick while capped");
   294	        assertEq(state.currentBanditSpawnChanceBps, 4321, "preview chance refreshed");
   295	    }
   296	
   297	    function test_heartbeatCompletesWhenClanCountExceedsBanditSpawnScanCap() public {
   298	        uint256 clanCount = world.maxBanditSpawnScanPerRegion() + 1;
   299	        uint160 ownerId = 1;
   300	        for (uint256 i = 0; i < clanCount; i++) {
   301	            world.mintClan(address(ownerId));
   302	            ownerId += 1;
   303	        }
   304	
   305	        _advanceTick(world);
   306	
   307	        assertEq(world.getWorldState().currentTick, 1, "heartbeat advanced");
   308	    }
   309	
   310	    function test_heartbeatEagerSettlesCandidateRegionBasesAndDefendersBeforeBanditSpawn() public {
   311	        _advancePastInitialCooldown(world);
   312	        (uint32 clanId1, uint32 clanId2) = _mintUntilTwoForestClans(world);
   313	
   314	        (uint32 workerId1, uint32 defenderId1) = _submitPendingWorkerAndDefender(world, clanId1);
   315	        (uint32 workerId2, uint32 defenderId2) = _submitPendingWorkerAndDefender(world, clanId2);
   316	
   317	        _blockNonForestSpawnRegions(world);
   318	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, world.getWorldState().currentTick, 0);
   319	        vm.prevrandao(_prevrandaoForNextForestSpawn(world));
   320	        _advanceTick(world);
   321	
   322	        uint64 closedTick = world.getWorldState().currentTick;
   323	        assertEq(world.getClan(clanId1).lastSettledTick, closedTick - 1, "clan 1 setup: unsettled");
   324	        assertEq(world.getClan(clanId2).lastSettledTick, closedTick - 1, "clan 2 setup: unsettled");
   325	        assertTrue(world.getActiveMission(workerId1).active, "clan 1 worker has pending mission");
   326	        assertTrue(world.getActiveMission(workerId2).active, "clan 2 worker has pending mission");
   327	        assertEq(world.getActiveDefenders(clanId1)[0], defenderId1, "clan 1 defender registered");
   328	        assertEq(world.getActiveDefenders(clanId2)[0], defenderId2, "clan 2 defender registered");
   329	
   330	        _blockNonForestSpawnRegions(world);
   331	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   332	
   333	        vm.expectEmit(true, false, false, true);
   334	        emit ClanSettled(clanId1, closedTick);
   335	        vm.expectEmit(true, false, false, true);
   336	        emit ClanSettled(clanId2, closedTick);
   337	        vm.expectEmit(true, false, false, false);
   338	        emit BanditSpawned(1, 0, 0, 0);
   339	
   340	        _advanceTick(world);
   341	
   342	        assertEq(world.getClan(clanId1).lastSettledTick, closedTick, "clan 1 eager-settled");
   343	        assertEq(world.getClan(clanId2).lastSettledTick, closedTick, "clan 2 eager-settled");
   344	
   345	        Mission memory defenderMission1 = world.getActiveMission(defenderId1);
   346	        Mission memory defenderMission2 = world.getActiveMission(defenderId2);
   347	        assertTrue(defenderMission1.active, "clan 1 defender remains active");
   348	        assertTrue(defenderMission2.active, "clan 2 defender remains active");
   349	        assertEq(uint8(defenderMission1.action), uint8(ActionType.DefendBase), "clan 1 defender action");
   350	        assertEq(uint8(defenderMission2.action), uint8(ActionType.DefendBase), "clan 2 defender action");
   351	        assertEq(uint8(world.getClansman(defenderId1).state), uint8(ClansmanState.ACTING), "clan 1 defender settled");
   352	        assertEq(uint8(world.getClansman(defenderId2).state), uint8(ClansmanState.ACTING), "clan 2 defender settled");
   353	
   354	        BanditTroop memory bandit = world.getBandit(1);
   355	        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
   356	        assertEq(bandit.region, ClanWorldConstants.REGION_FOREST, "forest selected after eager settle");
   357	        assertEq(bandit.tickEnteredState, closedTick, "spawn used closed tick");
   358	    }
   359	
   360	    function test_regionSelectionDeterministicForSameSeed() public {
   361	        BanditSpawnHarness a = new BanditSpawnHarness();
   362	        BanditSpawnHarness b = new BanditSpawnHarness();
   363	        _advancePastInitialCooldown(a);
   364	        _advancePastInitialCooldown(b);
   365	        a.mintClan(address(0xA11CE));
   366	        a.mintClan(address(0xB0B));
   367	        b.mintClan(address(0xA11CE));
   368	        b.mintClan(address(0xB0B));
   369	        a.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   370	        a.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
   371	        b.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   372	        b.setBanditSpawnState(ClanWorldConstants.REGION_MOUNTAINS, 0, 10000);
   373	
   374	        bytes32 seed = keccak256("deterministic-region");
   375	        a.evaluateBanditSpawns(seed);
   376	        b.evaluateBanditSpawns(seed);
   377	
   378	        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
   379	    }
   380	

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '100,220p;290,315p;590,620p;725,740p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   604	    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
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

exec
/bin/bash -lc 'git show 23f7f1a:packages/contracts/src/ClanWorld.sol | rg -n "function _clearDefender|function _registerDefender|function getActiveDefenders|_defendingClansByRegion|_clansmanDefendingRegion|DefendBase"' in /home/claude/code/clan-world
 succeeded in 0ms:
62:    mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
64:    mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
355:                if (m.action == ActionType.DefendBase) {
375:                if (m.action == ActionType.DefendBase) {
380:            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
503:            if (mission.action == ActionType.DefendBase) {
538:                if (mission.action == ActionType.DefendBase) {
561:                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
562:                        && _clansmanDefendingRegion[clansmanId] == baseRegion
628:        } else if (action == ActionType.DefendBase) {
1182:            if (m.action == ActionType.DefendBase) continue;
1816:        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
1852:                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
1866:        uint32[] storage defendingClans = _defendingClansByRegion[targetRegion];
1882:                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
2122:        uint32[] storage defendingClans = _defendingClansByRegion[region];
2139:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
2508:        if (order.action == ActionType.DefendBase) {
2509:            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
2518:                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
2540:            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
2542:        // NOOP bypass: treat 0 as "stay here"; DefendBase requires the defended base region.
2543:        ctx.isNoop = order.action != ActionType.DefendBase
2598:        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
2631:        m.settlesAtTick = order.action == ActionType.DefendBase
2681:    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
2682:        if (_clansmanDefendingRegion[clansmanId] == region) return;
2686:            _defendingClansByRegion[region].push(clanId);
2689:        _clansmanDefendingRegion[clansmanId] = region;
2692:    function _clearDefender(uint32 clansmanId) internal {
2693:        uint8 region = _clansmanDefendingRegion[clansmanId];
2702:            uint32[] storage clans = _defendingClansByRegion[region];
2712:        delete _clansmanDefendingRegion[clansmanId];
3008:        if (action == ActionType.DefendBase) {
3009:            return _validateDefendBaseOrder(clan, order, gotoRegion);
3041:    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
3227:    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
3228:        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
3235:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
3248:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
3256:        return _defendingClansByRegion[region];
3422:                sim.missions[i].active && sim.missions[i].action == ActionType.DefendBase
3432:                uint8 region = _clansmanDefendingRegion[csIds[i]];
3445:            incomingDefenderIds: _defendingClansByRegion[sim.clan.baseRegion],

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2460,2650p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2460	                        missionNonce: 0
  2461	                    });
  2462	                }
  2463	                return results;
  2464	            }
  2465	        }
  2466	
  2467	        // Lazy settle before processing orders
  2468	        _settleClan(clanId);
  2469	
  2470	        results = new OrderResult[](orders.length);
  2471	
  2472	        for (uint256 i = 0; i < orders.length; i++) {
  2473	            results[i] = _processOrder(clanId, clan, orders[i]);
  2474	        }
  2475	
  2476	        return results;
  2477	    }
  2478	
  2479	    struct OrderCtx {
  2480	        uint8 gotoRegion;
  2481	        uint8 fromRegion;
  2482	        bool isNoop;
  2483	        uint8 travelTicks;
  2484	        uint64 arrivalTick;
  2485	        uint64 newNonce;
  2486	        uint32 targetClanId;
  2487	        bool wasActive;
  2488	        uint64 oldNonce;
  2489	    }
  2490	
  2491	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  2492	        internal
  2493	        returns (OrderResult memory result)
  2494	    {
  2495	        result.clansmanId = order.clansmanId;
  2496	
  2497	        // Validate clansman
  2498	        Clansman storage cs = _clansmen[order.clansmanId];
  2499	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  2500	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  2501	            return result;
  2502	        }
  2503	        if (cs.state == ClansmanState.DEAD) {
  2504	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  2505	            return result;
  2506	        }
  2507	
  2508	        if (order.action == ActionType.DefendBase) {
  2509	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  2510	            if (defendErr != StatusCode.OK) {
  2511	                result.status = defendErr;
  2512	                return result;
  2513	            }
  2514	
  2515	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  2516	            Mission storage currentM = _missions[order.clansmanId];
  2517	            if (
  2518	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  2519	                    && currentM.targetClanId == defendTargetClanId
  2520	            ) {
  2521	                result.status = StatusCode.OK;
  2522	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  2523	                result.missionNonce = currentM.nonce;
  2524	                return result;
  2525	            }
  2526	        }
  2527	
  2528	        // Cooldown check
  2529	        if (block.timestamp < cs.cooldownEndsAtTs) {
  2530	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
  2531	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  2532	            result.missionNonce = cs.lastMissionNonce;
  2533	            return result;
  2534	        }
  2535	
  2536	        OrderCtx memory ctx;
  2537	        ctx.fromRegion = cs.currentRegion;
  2538	        ctx.gotoRegion = order.gotoRegion;
  2539	        ctx.targetClanId =
  2540	            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
  2541	
  2542	        // NOOP bypass: treat 0 as "stay here"; DefendBase requires the defended base region.
  2543	        ctx.isNoop = order.action != ActionType.DefendBase
  2544	            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
  2545	        if (ctx.isNoop) {
  2546	            ctx.gotoRegion = ctx.fromRegion;
  2547	        }
  2548	
  2549	        // Validate target region (1-8 or 0=noop)
  2550	        if (ctx.gotoRegion > 8) {
  2551	            result.status = StatusCode.ERR_INVALID_REGION;
  2552	            return result;
  2553	        }
  2554	
  2555	        // Validate action
  2556	        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
  2557	        if (actionErr != StatusCode.OK) {
  2558	            result.status = actionErr;
  2559	            return result;
  2560	        }
  2561	
  2562	        // Capture existing mission state
  2563	        Mission storage existingM = _missions[order.clansmanId];
  2564	        ctx.wasActive = existingM.active;
  2565	        ctx.oldNonce = existingM.nonce;
  2566	
  2567	        // Compute travel from the clansman's current region to the order target.
  2568	        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
  2569	        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
  2570	
  2571	        // New nonce
  2572	        ctx.newNonce = cs.lastMissionNonce + 1;
  2573	        cs.lastMissionNonce = ctx.newNonce;
  2574	
  2575	        // Install mission via helper to keep stack shallow
  2576	        _installMission(existingM, order, cs, ctx);
  2577	
  2578	        // Update clansman state
  2579	        if (ctx.travelTicks > 0) {
  2580	            cs.state = ClansmanState.TRAVELING;
  2581	        } else {
  2582	            // NOOP / same-region: no traveling state per v4.3 §A
  2583	            cs.state = ClansmanState.ACTING;
  2584	            cs.currentRegion = ctx.gotoRegion;
  2585	        }
  2586	
  2587	        // Start cooldown
  2588	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  2589	
  2590	        _clearDefender(cs.clansmanId);
  2591	
  2592	        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
  2593	        // executeAtTick = arrivalTick (not arrivalTick+1).
  2594	        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
  2595	            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
  2596	        }
  2597	
  2598	        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
  2599	            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
  2600	        }
  2601	
  2602	        if (ctx.wasActive) {
  2603	            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
  2604	        }
  2605	
  2606	        emit MissionAssigned(
  2607	            clanId,
  2608	            order.clansmanId,
  2609	            ctx.newNonce,
  2610	            order.action,
  2611	            ctx.fromRegion,
  2612	            ctx.gotoRegion,
  2613	            _world.currentTick,
  2614	            ctx.arrivalTick
  2615	        );
  2616	
  2617	        result.status = StatusCode.OK;
  2618	        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  2619	        result.missionNonce = ctx.newNonce;
  2620	        return result;
  2621	    }
  2622	
  2623	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  2624	        internal
  2625	    {
  2626	        m.active = true;
  2627	        m.nonce = ctx.newNonce;
  2628	        m.clansmanId = cs.clansmanId;
  2629	        m.submittedAtTick = _world.currentTick;
  2630	        m.executesAtTick = ctx.arrivalTick;
  2631	        m.settlesAtTick = order.action == ActionType.DefendBase
  2632	            ? type(uint64).max
  2633	            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
  2634	        m.startRegion = ctx.fromRegion;
  2635	        m.targetRegion = ctx.gotoRegion;
  2636	        m.action = order.action;
  2637	        m.startTick = _world.currentTick;
  2638	        m.arrivalTick = ctx.arrivalTick;
  2639	        m.actionStartTick = ctx.arrivalTick;
  2640	        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
  2641	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
  2642	            ? MarketExecutionMode.Scheduled
  2643	            : MarketExecutionMode.None;
  2644	        m.targetClanId = ctx.targetClanId;
  2645	        m.marketToken = order.marketToken;
  2646	        m.marketAmount = order.marketAmount;
  2647	        m.maxGoldIn = order.maxGoldIn;
  2648	    }
  2649	
  2650	    function _enqueueScheduledMarketAction(

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2668,2720p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2668	        _scheduledMarketActions[executeAtTick].push(sma);
  2669	        emit ScheduledMarketActionCommitted(
  2670	            executeAtTick,
  2671	            sma.commitSequence,
  2672	            clanId,
  2673	            clansmanId,
  2674	            order.action,
  2675	            order.marketToken,
  2676	            order.marketAmount,
  2677	            order.maxGoldIn
  2678	        );
  2679	    }
  2680	
  2681	    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
  2682	        if (_clansmanDefendingRegion[clansmanId] == region) return;
  2683	        _clearDefender(clansmanId);
  2684	
  2685	        if (_defenderCountByRegionClan[region][clanId] == 0) {
  2686	            _defendingClansByRegion[region].push(clanId);
  2687	        }
  2688	        _defenderCountByRegionClan[region][clanId]++;
  2689	        _clansmanDefendingRegion[clansmanId] = region;
  2690	    }
  2691	
  2692	    function _clearDefender(uint32 clansmanId) internal {
  2693	        uint8 region = _clansmanDefendingRegion[clansmanId];
  2694	        if (region == 0) return;
  2695	
  2696	        uint32 clanId = _clansmen[clansmanId].clanId;
  2697	        uint256 count = _defenderCountByRegionClan[region][clanId];
  2698	        if (count > 1) {
  2699	            _defenderCountByRegionClan[region][clanId] = count - 1;
  2700	        } else {
  2701	            delete _defenderCountByRegionClan[region][clanId];
  2702	            uint32[] storage clans = _defendingClansByRegion[region];
  2703	            for (uint256 i = 0; i < clans.length; i++) {
  2704	                if (clans[i] == clanId) {
  2705	                    clans[i] = clans[clans.length - 1];
  2706	                    clans.pop();
  2707	                    break;
  2708	                }
  2709	            }
  2710	        }
  2711	
  2712	        delete _clansmanDefendingRegion[clansmanId];
  2713	    }
  2714	
  2715	    // =========================================================================
  2716	    // MARKET EXECUTION (Phase 2)
  2717	    // =========================================================================
  2718	
  2719	    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
  2720	    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '3124,3195p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3124	    function getWorldState() external view override returns (WorldState memory) {
  3125	        return _world;
  3126	    }
  3127	
  3128	    function getTreasuryState() external view override returns (TreasuryState memory) {
  3129	        return _treasury;
  3130	    }
  3131	
  3132	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  3133	        return _clans[clanId];
  3134	    }
  3135	
  3136	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  3137	        return _clansmen[clansmanId];
  3138	    }
  3139	
  3140	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  3141	        return _missions[clansmanId];
  3142	    }
  3143	
  3144	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  3145	        external
  3146	        view
  3147	        override
  3148	        returns (uint64 submitted, uint64 executes, uint64 settles)
  3149	    {
  3150	        Mission memory m = _missions[clansmanId];
  3151	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  3152	            return (0, 0, 0);
  3153	        }
  3154	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  3155	    }
  3156	
  3157	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  3158	        if (
  3159	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  3160	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  3161	        ) {
  3162	            return 4;
  3163	        }
  3164	
  3165	        if (
  3166	            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
  3167	                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
  3168	                || action == ActionType.MarketSell
  3169	        ) {
  3170	            return 1;
  3171	        }
  3172	
  3173	        return 0;
  3174	    }
  3175	
  3176	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
  3177	        return uint64(_travelTicks(fromRegion, toRegion));
  3178	    }
  3179	
  3180	    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
  3181	        BanditTroop memory bandit = _bandits[banditId];
  3182	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
  3183	            return BanditTroop({
  3184	                id: 0,
  3185	                region: 0,
  3186	                state: BanditState.None,
  3187	                targetClanId: 0,
  3188	                tickEnteredState: 0,
  3189	                strength: 0,
  3190	                carryWood: 0,
  3191	                carryIron: 0,
  3192	                carryWheat: 0,
  3193	                carryFish: 0,
  3194	                carryGold: 0
  3195	            });

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '3218,3265p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3218	    function getScheduledMarketActionsForTick(uint64 tick)
  3219	        external
  3220	        view
  3221	        override
  3222	        returns (ScheduledMarketAction[] memory)
  3223	    {
  3224	        return _scheduledMarketActions[tick];
  3225	    }
  3226	
  3227	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  3228	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  3229	        uint256 count = 0;
  3230	
  3231	        for (uint256 i = 0; i < defendingClans.length; i++) {
  3232	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  3233	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  3234	                Mission storage mission = _missions[clanClansmen[j]];
  3235	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  3236	                    count++;
  3237	                }
  3238	            }
  3239	        }
  3240	
  3241	        clansmanIds = new uint32[](count);
  3242	        uint256 out = 0;
  3243	        for (uint256 i = 0; i < defendingClans.length; i++) {
  3244	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  3245	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  3246	                uint32 clansmanId = clanClansmen[j];
  3247	                Mission storage mission = _missions[clansmanId];
  3248	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  3249	                    clansmanIds[out++] = clansmanId;
  3250	                }
  3251	            }
  3252	        }
  3253	    }
  3254	
  3255	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
  3256	        return _defendingClansByRegion[region];
  3257	    }
  3258	
  3259	    // =========================================================================
  3260	    // DERIVED READ GETTERS (read-only, no storage mutation)
  3261	    // =========================================================================
  3262	
  3263	    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
  3264	        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
  3265	        return _derivedClanStateFromSimulation(sim.clan);

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '3195,3218p;3480,3530p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3195	            });
  3196	        }
  3197	        return bandit;
  3198	    }
  3199	
  3200	    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
  3201	        return getBandit(banditId);
  3202	    }
  3203	
  3204	    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
  3205	        return _banditsByRegion[region];
  3206	    }
  3207	
  3208	    function getWheatPlots(uint32 clanId)
  3209	        external
  3210	        view
  3211	        override
  3212	        returns (WheatPlot memory west, WheatPlot memory east)
  3213	    {
  3214	        west = _wheatPlots[clanId][0];
  3215	        east = _wheatPlots[clanId][1];
  3216	    }
  3217	
  3218	    function getScheduledMarketActionsForTick(uint64 tick)
  3480	            } else if (bandit.state == BanditState.Camped) {
  3481	                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
  3482	            } else if (bandit.state == BanditState.Resting) {
  3483	                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS;
  3484	            }
  3485	        }
  3486	
  3487	        return ActiveBanditView({
  3488	            exists: exists,
  3489	            banditId: bandit.id,
  3490	            state: bandit.state,
  3491	            currentRegion: bandit.region,
  3492	            attackAttemptsMade: 0,
  3493	            maxAttemptsRemaining: 0,
  3494	            stateEnteredTick: bandit.tickEnteredState,
  3495	            nextActionTick: nextActionTick,
  3496	            tier: 0,
  3497	            attackPower: _banditStrengthForLegacyEvent(bandit.strength),
  3498	            carryWood: bandit.carryWood,
  3499	            carryIron: bandit.carryIron,
  3500	            carryWheat: bandit.carryWheat,
  3501	            carryFish: bandit.carryFish,
  3502	            projectedTargetClanId: bandit.targetClanId,
  3503	            projectedTargetLootValue: 0
  3504	        });
  3505	    }
  3506	
  3507	    /// @dev View function — no gas cost for off-chain indexer/UI reads.
  3508	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  3509	    function getRegionPopulation(uint8 region) external view override returns (RegionOccupant[] memory) {
  3510	        // Count matching occupants first
  3511	        uint256 count = 0;
  3512	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  3513	            uint32 cid = _allClanIds[i];
  3514	            uint32[] storage csIds = _clanClansmanIds[cid];
  3515	            for (uint256 j = 0; j < csIds.length; j++) {
  3516	                Clansman storage cs = _clansmen[csIds[j]];
  3517	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
  3518	                    count++;
  3519	                }
  3520	            }
  3521	        }
  3522	
  3523	        RegionOccupant[] memory occupants = new RegionOccupant[](count);
  3524	        uint256 idx = 0;
  3525	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  3526	            uint32 cid = _allClanIds[i];
  3527	            uint32[] storage csIds = _clanClansmanIds[cid];
  3528	            for (uint256 j = 0; j < csIds.length; j++) {
  3529	                uint32 csId = csIds[j];
  3530	                Clansman storage cs = _clansmen[csId];

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorldStub.sol | nl -ba | sed -n '180,235p;330,365p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   180	            active: false,
   181	            nonce: 0,
   182	            submittedAtTick: 0,
   183	            executesAtTick: 0,
   184	            settlesAtTick: 0,
   185	            clansmanId: 0,
   186	            startRegion: 0,
   187	            targetRegion: 0,
   188	            action: ActionType.None,
   189	            startTick: 0,
   190	            arrivalTick: 0,
   191	            actionStartTick: 0,
   192	            missionSeed: bytes32(0),
   193	            marketMode: MarketExecutionMode.None,
   194	            targetClanId: 0,
   195	            marketToken: address(0),
   196	            marketAmount: 0,
   197	            maxGoldIn: 0
   198	        });
   199	    }
   200	
   201	    function getMissionTiming(uint32, uint32)
   202	        external
   203	        pure
   204	        override
   205	        returns (uint64 submitted, uint64 executes, uint64 settles)
   206	    {
   207	        return (0, 0, 0);
   208	    }
   209	
   210	    function getActionDuration(ActionType) external pure override returns (uint64) {
   211	        return 0;
   212	    }
   213	
   214	    function getTravelTicks(uint8, uint8) external pure override returns (uint64) {
   215	        return 0;
   216	    }
   217	
   218	    function getBandit(uint32) public pure override returns (BanditTroop memory) {
   219	        return BanditTroop({
   220	            id: 0,
   221	            region: 0,
   222	            state: BanditState.None,
   223	            targetClanId: 0,
   224	            tickEnteredState: 0,
   225	            strength: 0,
   226	            carryWood: 0,
   227	            carryIron: 0,
   228	            carryWheat: 0,
   229	            carryFish: 0,
   230	            carryGold: 0
   231	        });
   232	    }
   233	
   234	    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
   235	        return getBandit(banditId);
   330	                resourceToken: _treasury.woodToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   331	            }),
   332	            wheat: PoolReserves({
   333	                resourceToken: _treasury.wheatToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   334	            }),
   335	            fish: PoolReserves({
   336	                resourceToken: _treasury.fishToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   337	            }),
   338	            iron: PoolReserves({
   339	                resourceToken: _treasury.ironToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   340	            }),
   341	            currentTick: _world.currentTick,
   342	            currentTickQueue: new ScheduledMarketAction[](0),
   343	            nextTickQueue: new ScheduledMarketAction[](0)
   344	        });
   345	    }
   346	
   347	    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
   348	        return ActiveBanditView({
   349	            exists: false,
   350	            banditId: 0,
   351	            state: BanditState.None,
   352	            currentRegion: 0,
   353	            attackAttemptsMade: 0,
   354	            maxAttemptsRemaining: 0,
   355	            stateEnteredTick: 0,
   356	            nextActionTick: 0,
   357	            tier: 0,
   358	            attackPower: 0,
   359	            carryWood: 0,
   360	            carryIron: 0,
   361	            carryWheat: 0,
   362	            carryFish: 0,
   363	            projectedTargetClanId: 0,
   364	            projectedTargetLootValue: 0
   365	        });

exec
/bin/bash -lc "git show 23f7f1a:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '1,110p' && git show 23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts | nl -ba | sed -n '60,95p;300,338p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	import fs from 'node:fs';
     2	import { createPublicClient, createWalletClient, http, fallback, defineChain } from 'viem';
     3	import { privateKeyToAccount } from 'viem/accounts';
     4	import type { ClanFullView, ClanOrder, Tick } from '../types';
     5	import { readEnv } from './_env';
     6	
     7	export interface IChainClient {
     8	  getCurrentTick(): Promise<Tick>;
     9	  submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }>;
    10	  getClanFullView(clanId: string): Promise<ClanFullView>;
    11	}
    12	
    13	const DEFAULT_CONTRACT_ADDRESS = '0x1BF5649f29CbB53E117a5aE969A18A71790f83E8' as const;
    14	
    15	export const baseSepolia = defineChain({
    16	  id: 84532,
    17	  name: 'Base Sepolia',
    18	  nativeCurrency: { name: 'Ether', symbol: 'ETH', decimals: 18 },
    19	  rpcUrls: {
    20	    default: { http: ['https://sepolia.base.org'] },
    21	  },
    22	});
    23	
    24	// Minimal ABI — only the two read functions we call.
    25	const CLAN_WORLD_ABI = [
    26	  {
    27	    type: 'function',
    28	    name: 'getWorldSnapshot',
    29	    inputs: [],
    30	    outputs: [
    31	      {
    32	        name: '',
    33	        type: 'tuple',
    34	        components: [
    35	          { name: 'currentTick', type: 'uint64' },
    36	          { name: 'seasonStartTick', type: 'uint64' },
    37	          { name: 'seasonEndTick', type: 'uint64' },
    38	          { name: 'seasonFinalized', type: 'bool' },
    39	          { name: 'winterActive', type: 'bool' },
    40	          { name: 'winterStartsAtTick', type: 'uint64' },
    41	          { name: 'winterEndsAtTick', type: 'uint64' },
    42	          { name: 'activeBanditId', type: 'uint32' },
    43	          { name: 'currentTickSeed', type: 'bytes32' },
    44	          {
    45	            name: 'leaderboard',
    46	            type: 'tuple[]',
    47	            components: [
    48	              { name: 'clanId', type: 'uint32' },
    49	              { name: 'owner', type: 'address' },
    50	              { name: 'monumentLevel', type: 'uint8' },
    51	              { name: 'baseLevel', type: 'uint8' },
    52	              { name: 'wallLevel', type: 'uint8' },
    53	              { name: 'livingClansmen', type: 'uint8' },
    54	              { name: 'state', type: 'uint8' },
    55	              { name: 'lootValue', type: 'uint256' },
    56	            ],
    57	          },
    58	        ],
    59	      },
    60	    ],
    61	    stateMutability: 'view',
    62	  },
    63	  {
    64	    type: 'function',
    65	    name: 'getClanFullView',
    66	    inputs: [{ name: '', type: 'uint32' }],
    67	    outputs: [
    68	      {
    69	        name: '',
    70	        type: 'tuple',
    71	        components: [
    72	          {
    73	            name: 'clan',
    74	            type: 'tuple',
    75	            components: [
    76	              {
    77	                name: 'clan',
    78	                type: 'tuple',
    79	                components: [
    80	                  { name: 'clanId', type: 'uint32' },
    81	                  { name: 'iftTokenId', type: 'uint256' },
    82	                  { name: 'owner', type: 'address' },
    83	                  { name: 'clanState', type: 'uint8' },
    84	                  { name: 'baseRegion', type: 'uint8' },
    85	                  { name: 'baseLevel', type: 'uint8' },
    86	                  { name: 'wallLevel', type: 'uint8' },
    87	                  { name: 'monumentLevel', type: 'uint8' },
    88	                  { name: 'livingClansmen', type: 'uint8' },
    89	                  { name: 'lastSettledTick', type: 'uint64' },
    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
    91	                  { name: 'coldDamage', type: 'uint16' },
    92	                  { name: 'goldBalance', type: 'uint256' },
    93	                  { name: 'blueprintBalance', type: 'uint256' },
    94	                  { name: 'vaultWood', type: 'uint256' },
    95	                  { name: 'vaultIron', type: 'uint256' },
    96	                  { name: 'vaultWheat', type: 'uint256' },
    97	                  { name: 'vaultFish', type: 'uint256' },
    98	                ],
    99	              },
   100	              { name: 'isStarving', type: 'bool' },
   101	              { name: 'lootValue', type: 'uint256' },
   102	              { name: 'derivedAtTick', type: 'uint64' },
   103	            ],
   104	          },
   105	          // clansmen, westPlot, eastPlot etc. omitted — not needed for Wave 0 mapping
   106	          {
   107	            name: 'clansmen',
   108	            type: 'tuple[]',
   109	            components: [
   110	              {
    60	 * Posted by Elder agents pre-tx so the bubble appears immediately.
    61	 */
    62	export interface AgentLog {
    63	  id: string;
    64	  clanId: string;
    65	  tick: Tick;
    66	  /** Up to 240 chars per frontend speech-bubble convention. */
    67	  message: string;
    68	  /** Wall-clock ms (Date.now()) at post time. */
    69	  postedAt: number;
    70	  level: 'reasoning' | 'action' | 'observation';
    71	}
    72	
    73	/**
    74	 * Bandit troop state. Mirrors v4 §6 / v4.2 §7.7 — collapsed for the demo.
    75	 */
    76	export interface BanditState {
    77	  banditId: string;
    78	  state: 'CAMPING' | 'RESTING' | 'ATTACKING' | 'DEFEATED' | 'ESCAPED';
    79	  currentRegion: string;
    80	  /** Tick at which the troop entered its current state. */
    81	  stateEnteredTick: Tick;
    82	  /** Tick at which the next state transition / attack resolves. */
    83	  nextActionTick: Tick;
    84	  attackAttemptsMade: number;
    85	  tier: number;
    86	  attackPower: number;
    87	  /** Carried loot (18-decimal wei strings). '0' when troop has not yet stolen. */
    88	  carryWood: string;
    89	  carryIron: string;
    90	  carryWheat: string;
    91	  carryFish: string;
    92	}
    93	
    94	/** Sentinel for `Whisper.toClanId` indicating a public bulletin. */
    95	export const PUBLIC_WHISPER_RECIPIENT = '*';
   300	    fromClanId: 'sora',
   301	    toClanId: PUBLIC_WHISPER_RECIPIENT, // public bulletin
   302	    text: 'Monument tier 6 reached. Trading partnerships welcome — I have wheat surplus. No alliances of convenience.',
   303	    tick: 80,
   304	  },
   305	];
   306	
   307	// Bandit — CAMPING in Mountains (Brennan's region). Camp 3 ticks per v4 §6.6;
   308	// spawned tick 78 → exits camp tick 81 → ATTACK.
   309	const BANDIT_STATE: BanditState = {
   310	  banditId: 'bandit-001',
   311	  state: 'CAMPING',
   312	  currentRegion: 'mountains',
   313	  stateEnteredTick: 78,
   314	  nextActionTick: 81,
   315	  attackAttemptsMade: 0,
   316	  tier: 2,
   317	  attackPower: 45, // tier-2 per v4 §6.14
   318	  carryWood: '0',
   319	  carryIron: '0',
   320	  carryWheat: '0',
   321	  carryFish: '0',
   322	};
   323	
   324	export const WORLD_FIXTURE: {
   325	  worldSnapshot: WorldSnapshot;
   326	  clans: ClanFullView[];
   327	  agentLogs: AgentLog[];
   328	  whispers: Whisper[];
   329	  banditState: BanditState;
   330	} = {
   331	  worldSnapshot: WORLD_SNAPSHOT,
   332	  clans: CLAN_VIEWS,
   333	  agentLogs: AGENT_LOGS,
   334	  whispers: WHISPERS,
   335	  banditState: BANDIT_STATE,
   336	};
   337	
   338	// Convenience accessors used by Convex `seedMockState` mutation.

exec
/bin/bash -lc "git grep -n \"BanditState\\.NONE\\|BanditState\\.None\\|getBanditTroop\\|getBandit(\\|BanditTroop\\|activeBanditId\\|currentSeasonNumber\\|nextHeartbeatAtTick\" 23f7f1a -- ':"'!packages/contracts/abi/IClanWorld.json'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:apps/server/convex/schema.ts:10:    currentSeasonNumber: v.optional(v.number()),
23f7f1a:apps/server/convex/schema.ts:16:    nextHeartbeatAtTick: v.optional(v.number()),
23f7f1a:docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md:5:	1.	I made BanditState.NONE = 0 so bandit.state == NONE is the natural “no active bandit” check. v4.2 omitted this; adding it avoids the ambiguous default value.
23f7f1a:docs/planning/clanworld_numbered_implementation_plan.md:76:- `getBanditTroopRaw`
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:341:    uint32 activeBanditId;       // 0 if none
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:469:### 7.7 BanditTroop
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:472:struct BanditTroop {
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:608:function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
23f7f1a:docs/planning/clanworld_v4_3_schema_patch.md:178:`BanditTroop.tier` is canonical.
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:34:- 1 call for `getBanditTroop`
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:60:    uint32 activeBanditId; // 0 if none
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:166:    bool   exists;                    // false if WorldState.activeBanditId == 0
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:193:- Folds `getBanditTroop` + `getBanditTargetPreview` into one call.
23f7f1a:docs/planning/phase-3-test-spec.md:48:**Setup:** Spawn one bandit (probability 100%, valid region). Confirm `activeBanditId != 0`.
23f7f1a:docs/planning/phase-3-test-spec.md:52:**Assert:** `WorldState.activeBanditId` unchanged (same bandit ID); no second bandit created. (v4.2 §15 invariant.)
23f7f1a:docs/planning/phase-3-test-spec.md:63:- `BanditTroop.state == BanditState.Camping`
23f7f1a:docs/planning/phase-3-test-spec.md:64:- `BanditTroop.nextActionTick == N + CAMPING_DURATION` (e.g. 2 or 3 per implementation)
23f7f1a:docs/planning/phase-3-test-spec.md:65:- `BanditTroop.stateEnteredTick == N`
23f7f1a:docs/planning/phase-3-test-spec.md:66:- `BanditTroop.currentRegion` is a valid non-excluded region.
23f7f1a:docs/planning/phase-3-test-spec.md:76:**Assert:** Both times, `BanditTroop.state == BanditState.Camping`.
23f7f1a:docs/planning/phase-3-test-spec.md:86:**Assert:** `BanditTroop.state == BanditState.Attacking`.
23f7f1a:docs/planning/phase-3-test-spec.md:211:- `BanditTroop.state == BanditState.Defeated`.
23f7f1a:docs/planning/phase-3-test-spec.md:226:- `BanditTroop.state == BanditState.Retreating` (or `Defeated` per implementation).
23f7f1a:docs/planning/phase-3-test-spec.md:461:- `activeBanditId == 0` (bandit cleared from world state on defeat/retreat).
23f7f1a:docs/planning/phase-3-test-spec.md:467:**Fuzzer:** For any sequence of heartbeats, `WorldState.activeBanditId != 0` for at most one bandit ID at any point.
23f7f1a:docs/planning/phase-3-test-spec.md:475:**Fuzzer:** For any spawned bandit, `BanditTroop.currentRegion != REGION_UNICORN_TOWN && != REGION_DEEP_SEA`.
23f7f1a:packages/contracts/src/ClanWorld.sol:22:    BanditTroop,
23f7f1a:packages/contracts/src/ClanWorld.sol:65:    mapping(uint32 => BanditTroop) internal _bandits;
23f7f1a:packages/contracts/src/ClanWorld.sol:134:        _world.currentSeasonNumber = 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:135:        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
23f7f1a:packages/contracts/src/ClanWorld.sol:586:                BanditTroop storage bandit = _bandits[banditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:1524:        _bandits[id] = BanditTroop({
23f7f1a:packages/contracts/src/ClanWorld.sol:1544:        if (_world.activeBanditId == ClanWorldConstants.BANDIT_ID_NULL) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1545:            _world.activeBanditId = id;
23f7f1a:packages/contracts/src/ClanWorld.sol:1558:        BanditTroop storage bandit = _bandits[id];
23f7f1a:packages/contracts/src/ClanWorld.sol:1560:        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
23f7f1a:packages/contracts/src/ClanWorld.sol:1580:    function _isValidBanditTransition(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1589:    function _canBanditLeaveSpawned(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1594:    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1609:    function _canBanditLeaveResting(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1616:        BanditTroop storage bandit = _bandits[id];
23f7f1a:packages/contracts/src/ClanWorld.sol:1631:        if (_world.activeBanditId == id) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1632:            _world.activeBanditId = _findOldestActiveBandit();
23f7f1a:packages/contracts/src/ClanWorld.sol:1643:                BanditTroop storage candidate = _bandits[candidateId];
23f7f1a:packages/contracts/src/ClanWorld.sol:1644:                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1660:                BanditTroop storage bandit = _bandits[banditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:1685:                BanditTroop storage bandit = _bandits[banditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:1701:        BanditTroop storage bandit = _bandits[banditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:1765:        BanditTroop storage bandit = _bandits[banditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:2263:        _world.nextHeartbeatAtTick = newTick + 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:2299:            _world.currentSeasonNumber += 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:3180:    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3181:        BanditTroop memory bandit = _bandits[banditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:3182:        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3183:            return BanditTroop({
23f7f1a:packages/contracts/src/ClanWorld.sol:3186:                state: BanditState.None,
23f7f1a:packages/contracts/src/ClanWorld.sol:3200:    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3201:        return getBandit(banditId);
23f7f1a:packages/contracts/src/ClanWorld.sol:3391:            currentSeasonNumber: _world.currentSeasonNumber,
23f7f1a:packages/contracts/src/ClanWorld.sol:3392:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
23f7f1a:packages/contracts/src/ClanWorld.sol:3396:            activeBanditId: _world.activeBanditId,
23f7f1a:packages/contracts/src/ClanWorld.sol:3474:        BanditTroop memory bandit = _bandits[_world.activeBanditId];
23f7f1a:packages/contracts/src/ClanWorld.sol:3476:        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:22:    BanditTroop,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:52:        _world.currentSeasonNumber = 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:53:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:81:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:218:    function getBandit(uint32) public pure override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:219:        return BanditTroop({
23f7f1a:packages/contracts/src/ClanWorldStub.sol:222:            state: BanditState.None,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:234:    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:235:        return getBandit(banditId);
23f7f1a:packages/contracts/src/ClanWorldStub.sol:303:            currentSeasonNumber: _world.currentSeasonNumber,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:304:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:308:            activeBanditId: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:351:            state: BanditState.None,
23f7f1a:packages/contracts/src/IClanWorld.sol:193:    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
23f7f1a:packages/contracts/src/IClanWorld.sol:194:    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
23f7f1a:packages/contracts/src/IClanWorld.sol:201:    uint32 activeBanditId; // 0 if none
23f7f1a:packages/contracts/src/IClanWorld.sol:303:struct BanditTroop {
23f7f1a:packages/contracts/src/IClanWorld.sol:412:    uint64 currentSeasonNumber;
23f7f1a:packages/contracts/src/IClanWorld.sol:413:    uint64 nextHeartbeatAtTick;
23f7f1a:packages/contracts/src/IClanWorld.sol:417:    uint32 activeBanditId;
23f7f1a:packages/contracts/src/IClanWorld.sol:734:    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
23f7f1a:packages/contracts/src/IClanWorld.sol:736:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
23f7f1a:packages/contracts/test/Bandit.t.sol:6:import {ActiveBanditView, ClanWorldConstants, BanditState, BanditTroop, WorldState} from "../src/IClanWorld.sol";
23f7f1a:packages/contracts/test/Bandit.t.sol:55:        BanditTroop memory bandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:68:        assertEq(state.activeBanditId, id, "active bandit");
23f7f1a:packages/contracts/test/Bandit.t.sol:72:        BanditTroop memory bandit = world.getBandit(999);
23f7f1a:packages/contracts/test/Bandit.t.sol:75:        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
23f7f1a:packages/contracts/test/Bandit.t.sol:78:    function test_defaultBanditTroopStateIsNone() public pure {
23f7f1a:packages/contracts/test/Bandit.t.sol:79:        BanditTroop memory bandit;
23f7f1a:packages/contracts/test/Bandit.t.sol:81:        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
23f7f1a:packages/contracts/test/Bandit.t.sol:89:        BanditTroop memory bandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:110:        BanditTroop memory bandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:121:        BanditTroop memory deletedBandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:124:        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
23f7f1a:packages/contracts/test/Bandit.t.sol:126:        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
23f7f1a:packages/contracts/test/Bandit.t.sol:139:        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
23f7f1a:packages/contracts/test/Bandit.t.sol:140:        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
23f7f1a:packages/contracts/test/Bandit.t.sol:147:        BanditTroop memory escaped = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:152:        BanditTroop memory resting = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:158:        BanditTroop memory camped = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:176:        world.transitionBandit(id, BanditState.None);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:243:        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:304:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:305:        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:457:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:468:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:485:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:499:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:509:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:563:        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:564:        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:9:    BanditTroop,
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:239:        uint8 selectedRegion = world.getBandit(1).region;
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:354:        BanditTroop memory bandit = world.getBandit(1);
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:378:        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:397:        BanditTroop memory bandit = world.getBandit(1);
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:401:        assertEq(state.activeBanditId, bandit.id, "active bandit set");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:27:    BanditTroop,
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2010:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2072:        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2081:    function test_nextHeartbeatAtTick_tracks_tick() public {
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2083:        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2088:        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:6:import {BanditState, BanditTroop, ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:39:        BanditTroop memory bandit = stub.getBandit(999);
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:42:        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:48:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:51:        assertEq(ws.nextHeartbeatAtTick, ws.currentTick + 1, "next heartbeat opens currentTick + 1");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:394:    //   - currentSeasonNumber increments on the heartbeat that closes tick
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:400:        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:410:        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:47:          { name: 'activeBanditId', type: 'uint32' },
23f7f1a:packages/shared/src/adapters/IChainClient.ts:42:          { name: 'activeBanditId', type: 'uint32' },

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '390,430p;670,710p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '3368,3405p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3368	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  3369	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
  3370	        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
  3371	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  3372	            uint32 cid = _allClanIds[i];
  3373	            Clan storage clan = _clans[cid];
  3374	            lb[i] = LeaderboardEntry({
  3375	                clanId: cid,
  3376	                owner: clan.owner,
  3377	                monumentLevel: clan.monumentLevel,
  3378	                baseLevel: clan.baseLevel,
  3379	                wallLevel: clan.wallLevel,
  3380	                livingClansmen: clan.livingClansmen,
  3381	                state: clan.clanState,
  3382	                lootValue: _lootValueRaw(clan)
  3383	            });
  3384	        }
  3385	
  3386	        return WorldSnapshot({
  3387	            currentTick: _world.currentTick,
  3388	            seasonStartTick: _world.seasonStartTick,
  3389	            seasonEndTick: _world.seasonEndTick,
  3390	            seasonFinalized: _world.seasonFinalized,
  3391	            currentSeasonNumber: _world.currentSeasonNumber,
  3392	            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
  3393	            winterActive: _world.winterActive,
  3394	            winterStartsAtTick: _world.winterStartsAtTick,
  3395	            winterEndsAtTick: _world.winterEndsAtTick,
  3396	            activeBanditId: _world.activeBanditId,
  3397	            currentTickSeed: _world.currentTickSeed,
  3398	            leaderboard: lb
  3399	        });
  3400	    }
  3401	
  3402	    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
  3403	        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
  3404	        DerivedClanState memory derivedClan = _derivedClanStateFromSimulation(sim.clan);
  3405	

exec
/bin/bash -lc "git show 23f7f1a:packages/runner/src/runnerCastHeartbeat.ts | nl -ba | sed -n '1,85p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	import {
     2	  ContractFunctionRevertedError,
     3	  createPublicClient,
     4	  createWalletClient,
     5	  http,
     6	  type Account,
     7	  type PublicClient,
     8	  type WalletClient,
     9	} from 'viem';
    10	import { privateKeyToAccount } from 'viem/accounts';
    11	import { baseSepolia } from '@clan-world/shared/adapters';
    12	import {
    13	  HeartbeatRateLimitedError,
    14	  type IHeartbeatCaller,
    15	} from '@clan-world/agents/seams';
    16	
    17	/**
    18	 * Minimal ABI: only the `heartbeat()` write and the `nextHeartbeatAtTs` field
    19	 * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
    20	 * ABI here so the runner stays decoupled from contract-package versioning.
    21	 */
    22	const HEARTBEAT_ABI = [
    23	  {
    24	    type: 'function',
    25	    name: 'heartbeat',
    26	    inputs: [],
    27	    outputs: [],
    28	    stateMutability: 'nonpayable',
    29	  },
    30	  {
    31	    type: 'function',
    32	    name: 'getWorldState',
    33	    inputs: [],
    34	    outputs: [
    35	      {
    36	        name: '',
    37	        type: 'tuple',
    38	        components: [
    39	          { name: 'currentTick', type: 'uint64' },
    40	          { name: 'seasonStartTick', type: 'uint64' },
    41	          { name: 'seasonEndTick', type: 'uint64' },
    42	          { name: 'seasonFinalized', type: 'bool' },
    43	          { name: 'nextHeartbeatAtTs', type: 'uint64' },
    44	          { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
    45	          { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
    46	          { name: 'currentTickSeed', type: 'bytes32' },
    47	          { name: 'activeBanditId', type: 'uint32' },
    48	          { name: 'winterActive', type: 'bool' },
    49	          { name: 'winterStartsAtTick', type: 'uint64' },
    50	          { name: 'winterEndsAtTick', type: 'uint64' },
    51	          { name: 'nextCommitSequence', type: 'uint64' },
    52	        ],
    53	      },
    54	    ],
    55	    stateMutability: 'view',
    56	  },
    57	] as const;
    58	
    59	export interface RunnerHeartbeatConfig {
    60	  /** Hex-encoded 64-char private key, optionally 0x-prefixed. */
    61	  privateKey: string;
    62	  /** Override RPC URL; defaults to the Base Sepolia public endpoint. */
    63	  rpcUrl?: string;
    64	  /** ClanWorld contract address. */
    65	  contractAddress: `0x${string}`;
    66	}
    67	
    68	/**
    69	 * Reads `RUNNER_PRIVATE_KEY`, `RPC_URL_PRIMARY`, `CLAN_WORLD_CONTRACT_ADDRESS`
    70	 * from env. Throws if `RUNNER_PRIVATE_KEY` is missing — the runner intentionally
    71	 * does not generate or store its own wallet; provisioning is operator-side.
    72	 */
    73	export function configFromEnv(env: NodeJS.ProcessEnv = process.env): RunnerHeartbeatConfig {
    74	  const pk = env['RUNNER_PRIVATE_KEY'];
    75	  if (!pk) {
    76	    throw new Error(
    77	      'RUNNER_PRIVATE_KEY is not set — the runner needs a dedicated wallet (NEVER reuse an Elder wallet). ' +
    78	        'Provision a fresh key, fund it with testnet ETH, and export RUNNER_PRIVATE_KEY before starting the daemon.',
    79	    );
    80	  }
    81	  const contractAddress = env['CLAN_WORLD_CONTRACT_ADDRESS'];
    82	  if (!contractAddress || !/^0x[0-9a-fA-F]{40}$/.test(contractAddress)) {
    83	    throw new Error(
    84	      `CLAN_WORLD_CONTRACT_ADDRESS missing or invalid; expected 0x-prefixed 40-hex-char address, got ${String(contractAddress)}`,
    85	    );

exec
/bin/bash -lc "git show 23f7f1a:apps/server/convex/schema.ts | nl -ba | sed -n '1,35p' && git show 23f7f1a:apps/server/convex/chain.ts 2>/dev/null | nl -ba | sed -n '1,140p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	import { defineSchema, defineTable } from "convex/server";
     2	import { v } from "convex/values";
     3	
     4	export default defineSchema({
     5	  worldSnapshot: defineTable({
     6	    tick: v.number(),
     7	    tickEpochStartedAt: v.number(),
     8	    tickEpochDurationMs: v.number(),
     9	    // Season + winter timers (Phase 4.4)
    10	    currentSeasonNumber: v.optional(v.number()),
    11	    seasonStartTick: v.optional(v.number()),
    12	    seasonEndTick: v.optional(v.number()),
    13	    winterActive: v.optional(v.boolean()),
    14	    winterStartsAtTick: v.optional(v.number()),
    15	    winterEndsAtTick: v.optional(v.number()),
    16	    nextHeartbeatAtTick: v.optional(v.number()),
    17	    regions: v.array(
    18	      v.object({
    19	        id: v.string(),
    20	        name: v.string(),
    21	        ownerClanId: v.union(v.string(), v.null()),
    22	      })
    23	    ),
    24	    clans: v.array(
    25	      v.object({
    26	        id: v.string(),
    27	        name: v.string(),
    28	        treasury: v.string(),
    29	      })
    30	    ),
    31	  }),
    32	  agentLogs: defineTable({
    33	    level: v.union(v.literal("info"), v.literal("warn"), v.literal("error")),
    34	    message: v.string(),
    35	    timestamp: v.number(),

exec
/bin/bash -lc "git grep -n \"getWorldState\\|getWorldSnapshot\\|getClanFullView\\|CLAN_WORLD_ABI\\|HEARTBEAT_ABI\\|currentSeasonNumber\\|nextHeartbeatAtTick\" 23f7f1a -- apps packages | head -200" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:apps/server/AGENTS.md:7:- **Queries:** `getSnapshot`, `getClanFullView`, `subscribeWhispers` — the read surface for the frontend.
23f7f1a:apps/server/convex/schema.ts:10:    currentSeasonNumber: v.optional(v.number()),
23f7f1a:apps/server/convex/schema.ts:16:    nextHeartbeatAtTick: v.optional(v.number()),
23f7f1a:packages/agents/src/__tests__/cli.test.ts:18:    getClanFullView: vi.fn(async (clanId: string) => ({
23f7f1a:packages/agents/src/__tests__/cli.test.ts:34:    getClanFullView: vi.fn(async (clanId: string) => ({
23f7f1a:packages/agents/src/__tests__/cli.test.ts:72:    expect(convex.getClanFullView).toHaveBeenCalledWith('clan-1');
23f7f1a:packages/agents/src/cli.ts:105:    const result = await deps.convex.getClanFullView(clanId);
23f7f1a:packages/agents/test/cli.test.ts:34:    async getClanFullView() { return STUB_CLAN_VIEW; },
23f7f1a:packages/agents/test/cli.test.ts:45:    async getClanFullView(clanId: string): Promise<ClanFullView> {
23f7f1a:packages/contracts/abi/IClanWorld.json:557:      "name": "getClanFullView",
23f7f1a:packages/contracts/abi/IClanWorld.json:1994:      "name": "getWorldSnapshot",
23f7f1a:packages/contracts/abi/IClanWorld.json:2023:              "name": "currentSeasonNumber",
23f7f1a:packages/contracts/abi/IClanWorld.json:2028:              "name": "nextHeartbeatAtTick",
23f7f1a:packages/contracts/abi/IClanWorld.json:2111:      "name": "getWorldState",
23f7f1a:packages/contracts/abi/IClanWorld.json:2140:              "name": "currentSeasonNumber",
23f7f1a:packages/contracts/abi/IClanWorld.json:2145:              "name": "nextHeartbeatAtTick",
23f7f1a:packages/contracts/src/ClanWorld.sol:134:        _world.currentSeasonNumber = 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:135:        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
23f7f1a:packages/contracts/src/ClanWorld.sol:2263:        _world.nextHeartbeatAtTick = newTick + 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:2299:            _world.currentSeasonNumber += 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:3124:    function getWorldState() external view override returns (WorldState memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3369:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3391:            currentSeasonNumber: _world.currentSeasonNumber,
23f7f1a:packages/contracts/src/ClanWorld.sol:3392:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
23f7f1a:packages/contracts/src/ClanWorld.sol:3402:    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:52:        _world.currentSeasonNumber = 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:53:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:81:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:132:    function getWorldState() external view override returns (WorldState memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:297:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:303:            currentSeasonNumber: _world.currentSeasonNumber,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:304:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:314:    function getClanFullView(uint32) external view override returns (ClanFullView memory) {
23f7f1a:packages/contracts/src/IClanWorld.sol:193:    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
23f7f1a:packages/contracts/src/IClanWorld.sol:194:    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
23f7f1a:packages/contracts/src/IClanWorld.sol:412:    uint64 currentSeasonNumber;
23f7f1a:packages/contracts/src/IClanWorld.sol:413:    uint64 nextHeartbeatAtTick;
23f7f1a:packages/contracts/src/IClanWorld.sol:715:    function getWorldState() external view returns (WorldState memory);
23f7f1a:packages/contracts/src/IClanWorld.sol:779:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
23f7f1a:packages/contracts/src/IClanWorld.sol:784:    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory);
23f7f1a:packages/contracts/test/Bandit.t.sol:30:        vm.warp(world.getWorldState().nextHeartbeatAtTs);
23f7f1a:packages/contracts/test/Bandit.t.sol:60:        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick, "entered tick");
23f7f1a:packages/contracts/test/Bandit.t.sol:67:        WorldState memory state = world.getWorldState();
23f7f1a:packages/contracts/test/Bandit.t.sol:91:        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick - 1, "entered camp tick");
23f7f1a:packages/contracts/test/Bandit.t.sol:101:        assertEq(view_.nextActionTick, world.getWorldState().currentTick + 1, "spawn delay tick");
23f7f1a:packages/contracts/test/Bandit.t.sol:113:        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick, "entered attack tick");
23f7f1a:packages/contracts/test/Bandit.t.sol:126:        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
23f7f1a:packages/contracts/test/Bandit.t.sol:139:        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
23f7f1a:packages/contracts/test/Bandit.t.sol:154:        assertEq(resting.tickEnteredState, world.getWorldState().currentTick, "resting tick");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:109:        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:163:        while (world.getWorldState().currentTick <= tick) {
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:169:        while (world.getWorldState().currentTick < tick) {
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:257:        emit BlueprintEarned(clanId, banditId, world.getWorldState().currentTick);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:280:        uint64 winterStart = world.getWorldState().winterStartsAtTick;
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:287:        uint64 expectedBanditAbortTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:402:        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:404:        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:477:        bytes32 tickSeed = world.getWorldState().currentTickSeed;
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:537:            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:83:        vm.warp(target.getWorldState().nextHeartbeatAtTs);
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:117:        return target.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:161:        uint64 currentTick = target.getWorldState().currentTick;
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:170:        WorldState memory state = target.getWorldState();
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:291:        WorldState memory state = world.getWorldState();
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:307:        assertEq(world.getWorldState().currentTick, 1, "heartbeat advanced");
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:318:        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, world.getWorldState().currentTick, 0);
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:322:        uint64 closedTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:398:        WorldState memory state = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:170:        uint64 tickBefore = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:174:        uint64 tickAfter = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:183:        WorldState memory beforeFirst = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:190:        WorldState memory afterFirst = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:199:        WorldState memory afterSecond = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:209:        uint64 tickBefore = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:214:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "non-owner caller should advance heartbeat");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:219:        uint64 previousTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:228:            WorldState memory state = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:273:    // Test 5: mintClan — getClanFullView shows 4 WAITING clansmen at homebase
23f7f1a:packages/contracts/test/ClanWorld.t.sol:278:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:313:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:335:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:378:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:418:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:436:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:473:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:510:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:583:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:590:        while (world.getWorldState().currentTick <= world.getClan(clanId).lastSettledTick + 200) {
23f7f1a:packages/contracts/test/ClanWorld.t.sol:623:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:676:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:682:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:706:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:730:        uint64 curTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:753:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:766:        uint64 curTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:795:        uint64 curTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:834:        uint64 curTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:892:            ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:944:            ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:955:        ClanFullView memory nativeView = world.getClanFullView(nativeClanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1037:        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1088:        uint64 curTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1112:        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1146:        uint64 curTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1190:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1232:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1280:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1306:        uint32 cs2Id = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1335:        uint32 csId = harness.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1382:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1430:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1459:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1514:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1575:        csId = harness.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1613:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1647:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1680:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1707:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1768:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1806:        uint8 homeRegion = harness.getClanFullView(clanId).clan.clan.baseRegion;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1841:        uint8 baseRegion = harness.getClanFullView(clanId).clan.clan.baseRegion;
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1887:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1917:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:1936:        ClanFullView memory v = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2009:        WorldState memory ws = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2010:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2045:        assertTrue(world.getWorldState().winterActive, "winter should be active");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2046:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2056:        WorldState memory ws = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2071:        WorldState memory ws = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2072:        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2081:    function test_nextHeartbeatAtTick_tracks_tick() public {
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2082:        WorldState memory ws0 = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2083:        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2086:        WorldState memory ws1 = world.getWorldState();
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2088:        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:28:        assertEq(stub.getWorldState().currentTick, 1);
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:30:        assertEq(stub.getWorldState().currentTick, 2);
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:33:    function test_getWorldSnapshot_returns_current_tick() public {
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:35:        assertEq(stub.getWorldSnapshot().currentTick, 2);
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:46:        WorldState memory ws = stub.getWorldState();
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:48:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:51:        assertEq(ws.nextHeartbeatAtTick, ws.currentTick + 1, "next heartbeat opens currentTick + 1");
23f7f1a:packages/contracts/test/DefendBase.t.sol:32:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/DefendBase.t.sol:78:        while (world.getWorldState().currentTick < targetTick) {
23f7f1a:packages/contracts/test/DefendBase.t.sol:100:        uint64 submittedAtTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:74:        WorldState memory state = HeartbeatOrderingHarness(ENGINE).getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:136:        while (world.getWorldState().currentTick < targetTick) {
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:147:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:151:        return world.getClanFullView(clanId).clansmen[1].clansman.clansman.clansmanId;
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:294:        uint64 t0 = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:339:        WorldState memory before_ = world.getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:346:        WorldState memory after_ = world.getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:358:        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:359:        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:374:        WorldState memory beforeClose = world.getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:384:        WorldState memory afterClose = world.getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:394:    //   - currentSeasonNumber increments on the heartbeat that closes tick
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:399:        WorldState memory ws0 = world.getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:400:        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:409:        WorldState memory ws1 = world.getWorldState();
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:410:        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:425:        uint64 tickBefore = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:426:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:430:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "tick must increment");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:431:        assertNotEq(world.getWorldState().currentTickSeed, seedBefore, "seed must change");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:449:        uint64 t0 = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/MissionTiming.t.sol:32:        while (world.getWorldState().currentTick < targetTick) {
23f7f1a:packages/contracts/test/MissionTiming.t.sol:43:        ClanFullView memory view_ = world.getClanFullView(clanId);
23f7f1a:packages/contracts/test/MissionTiming.t.sol:68:        uint64 submittedAt = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/MissionTiming.t.sol:165:        assertEq(secondSubmitted, world.getWorldState().currentTick, "second submit uses current tick");
23f7f1a:packages/contracts/test/Reentrancy.t.sol:127:        uint64 currentTick = world.getWorldState().currentTick;
23f7f1a:packages/contracts/test/Reentrancy.t.sol:179:        vm.warp(world.getWorldState().nextHeartbeatAtTs);
23f7f1a:packages/contracts/test/Reentrancy.t.sol:189:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
23f7f1a:packages/runner/README.md:186:- Heartbeat rate-limit detection re-reads `getWorldState()` after a revert.
23f7f1a:packages/runner/src/composeSituationBlock.ts:39:    deps.convex.getClanFullView(args.clanId),
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:19: * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:22:const HEARTBEAT_ABI = [
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:32:    name: 'getWorldState',
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:99: * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:128:        abi: HEARTBEAT_ABI,
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:167:      abi: HEARTBEAT_ABI,
23f7f1a:packages/runner/src/runnerCastHeartbeat.ts:168:      functionName: 'getWorldState',
23f7f1a:packages/runner/test/composeSituationBlock.test.ts:20:    async getClanFullView(clanId: string) {
23f7f1a:packages/runner/test/composeSituationBlock.test.ts:116:          async getClanFullView() {
23f7f1a:packages/runner/test/composeSituationBlock.test.ts:140:          async getClanFullView(clanId: string) {
23f7f1a:packages/runner/test/tickLoop.test.ts:22:    async getClanFullView(clanId: string) {
23f7f1a:packages/shared/src/adapters/IChainClient.ts:10:  getClanFullView(clanId: string): Promise<ClanFullView>;
23f7f1a:packages/shared/src/adapters/IChainClient.ts:25:const CLAN_WORLD_ABI = [
23f7f1a:packages/shared/src/adapters/IChainClient.ts:28:    name: 'getWorldSnapshot',
23f7f1a:packages/shared/src/adapters/IChainClient.ts:65:    name: 'getClanFullView',
23f7f1a:packages/shared/src/adapters/IChainClient.ts:247:  async getClanFullView(clanId: string): Promise<ClanFullView> {
23f7f1a:packages/shared/src/adapters/IChainClient.ts:283:      abi: CLAN_WORLD_ABI,
23f7f1a:packages/shared/src/adapters/IChainClient.ts:284:      functionName: 'getWorldSnapshot',
23f7f1a:packages/shared/src/adapters/IChainClient.ts:369:      abi: CLAN_WORLD_ABI,
23f7f1a:packages/shared/src/adapters/IChainClient.ts:377:  async getClanFullView(clanId: string): Promise<ClanFullView> {
23f7f1a:packages/shared/src/adapters/IChainClient.ts:380:      abi: CLAN_WORLD_ABI,
23f7f1a:packages/shared/src/adapters/IChainClient.ts:381:      functionName: 'getClanFullView',
23f7f1a:packages/shared/src/adapters/IConvexClient.ts:9:  getClanFullView(clanId: string): Promise<ClanFullView>;
23f7f1a:packages/shared/src/adapters/IConvexClient.ts:23:  async getClanFullView(clanId: string): Promise<ClanFullView> {

exec
/bin/bash -lc "git show 23f7f1a:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '230,325p;360,410p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   230	          { name: 'status', type: 'uint8' },
   231	          { name: 'cooldownEndsAtTs', type: 'uint64' },
   232	          { name: 'missionNonce', type: 'uint64' },
   233	        ],
   234	      },
   235	    ],
   236	    stateMutability: 'nonpayable',
   237	  },
   238	] as const;
   239	
   240	class StubChainClient implements IChainClient {
   241	  async getCurrentTick(): Promise<Tick> {
   242	    return 0;
   243	  }
   244	  async submitOrders(_clanId: string, _orders: ClanOrder[]): Promise<{ txHash: string }> {
   245	    return { txHash: '0xstub' };
   246	  }
   247	  async getClanFullView(clanId: string): Promise<ClanFullView> {
   248	    return {
   249	      clan: { id: clanId, name: `clan-${clanId}`, treasury: '0' },
   250	      controlledRegions: [],
   251	      pendingOrders: [],
   252	      whispers: [],
   253	    };
   254	  }
   255	}
   256	
   257	class RealChainClient implements IChainClient {
   258	  private readonly client: ReturnType<typeof createPublicClient>;
   259	  private readonly contractAddress: `0x${string}`;
   260	  private readonly transport: ReturnType<typeof http> | ReturnType<typeof fallback>;
   261	
   262	  constructor() {
   263	    const primaryRpc = readEnv('RPC_URL_PRIMARY');
   264	    const fallbackRpc = readEnv('RPC_URL_FALLBACK');
   265	
   266	    this.transport =
   267	      primaryRpc && fallbackRpc
   268	        ? fallback([http(primaryRpc), http(fallbackRpc)])
   269	        : http(primaryRpc ?? fallbackRpc);
   270	
   271	    this.contractAddress = (readEnv('CLAN_WORLD_CONTRACT_ADDRESS') ??
   272	      DEFAULT_CONTRACT_ADDRESS) as `0x${string}`;
   273	
   274	    this.client = createPublicClient({
   275	      chain: baseSepolia,
   276	      transport: this.transport,
   277	    });
   278	  }
   279	
   280	  async getCurrentTick(): Promise<Tick> {
   281	    const snapshot = await this.client.readContract({
   282	      address: this.contractAddress,
   283	      abi: CLAN_WORLD_ABI,
   284	      functionName: 'getWorldSnapshot',
   285	    });
   286	    return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
   287	  }
   288	
   289	  async submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }> {
   290	    // Wave 0: single-Elder only — concurrent nonce coordination deferred to Wave 1
   291	    const parsedClanId = parseInt(clanId, 10);
   292	    if (isNaN(parsedClanId) || String(parsedClanId) !== clanId.trim()) {
   293	      throw new Error(`submitOrders: clanId must be a decimal integer, got '${clanId}'`);
   294	    }
   295	
   296	    for (const order of orders) {
   297	      if (order.kind === 'mission') {
   298	        const { clansmanId, gotoRegion, action } = order.payload;
   299	        if (clansmanId === undefined || gotoRegion === undefined || action === undefined) {
   300	          throw new Error(`submitOrders: mission order missing required payload fields (clansmanId, gotoRegion, action)`);
   301	        }
   302	      }
   303	    }
   304	
   305	    const nonMissionOrders = orders.filter(o => o.kind !== 'mission');
   306	    if (nonMissionOrders.length > 0) {
   307	      console.warn(`[RealChainClient] submitOrders: ${nonMissionOrders.length} non-mission order(s) skipped (Wave 0 only supports 'mission' kind)`);
   308	    }
   309	
   310	    const contractOrders = orders
   311	      .filter(o => o.kind === 'mission')
   312	      .map(o => ({
   313	        clansmanId: Number(o.payload.clansmanId),
   314	        gotoRegion: Number(o.payload.gotoRegion),
   315	        action: Number(o.payload.action),
   316	        targetClanId: 0,
   317	        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
   318	        marketAmount: 0n,
   319	        maxGoldIn: 0n,
   320	      }));
   321	
   322	    if (contractOrders.length === 0) {
   323	      throw new Error('submitOrders: no valid mission orders to submit');
   324	    }
   325	
   360	    const account = privateKeyToAccount(pk as `0x${string}`);
   361	    const walletClient = createWalletClient({
   362	      account,
   363	      chain: baseSepolia,
   364	      transport: this.transport,
   365	    });
   366	
   367	    const hash = await walletClient.writeContract({
   368	      address: this.contractAddress,
   369	      abi: CLAN_WORLD_ABI,
   370	      functionName: 'submitClanOrders',
   371	      args: [parsedClanId, contractOrders],
   372	    });
   373	
   374	    return { txHash: hash };
   375	  }
   376	
   377	  async getClanFullView(clanId: string): Promise<ClanFullView> {
   378	    const result = await this.client.readContract({
   379	      address: this.contractAddress,
   380	      abi: CLAN_WORLD_ABI,
   381	      functionName: 'getClanFullView',
   382	      args: [parseInt(clanId, 10)],
   383	    });
   384	
   385	    const inner = result.clan.clan;
   386	    return {
   387	      clan: {
   388	        id: String(inner.clanId),
   389	        name: `clan-${inner.clanId}`,
   390	        treasury: String(inner.goldBalance),
   391	      },
   392	      // controlledRegions, pendingOrders, whispers not available in Wave 0 contract read
   393	      controlledRegions: [], // Wave 0: omit base region from controlledRegions; populated in Wave 1 from on-chain data
   394	      pendingOrders: [],
   395	      whispers: [],
   396	    };
   397	  }
   398	}
   399	
   400	export function createChainClient(): IChainClient {
   401	  return readEnv('CLAN_WORLD_USE_STUB_CHAIN') === 'true'
   402	    ? new StubChainClient()
   403	    : new RealChainClient();
   404	}

exec
/bin/bash -lc "git grep -n \"_transitionBanditToAttacking\\|BanditState.Attacking\\|targetClanId =\" 23f7f1a -- packages/contracts/src/ClanWorld.sol packages/contracts/test | sed -n '1,120p'" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:packages/contracts/src/ClanWorld.sol:561:                    mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == deadClanId
23f7f1a:packages/contracts/src/ClanWorld.sol:587:                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1551:    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
23f7f1a:packages/contracts/src/ClanWorld.sol:1553:        _bandits[id].targetClanId = targetClanId;
23f7f1a:packages/contracts/src/ClanWorld.sol:1554:        _transitionBanditState(id, BanditState.Attacking);
23f7f1a:packages/contracts/src/ClanWorld.sol:1573:        if (newState != BanditState.Attacking) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1574:            bandit.targetClanId = ClanWorldConstants.CLAN_ID_NULL;
23f7f1a:packages/contracts/src/ClanWorld.sol:1583:        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
23f7f1a:packages/contracts/src/ClanWorld.sol:1596:            || (newState == BanditState.Attacking
23f7f1a:packages/contracts/src/ClanWorld.sol:1686:                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
23f7f1a:packages/contracts/src/ClanWorld.sol:1702:        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1709:        uint32 targetClanId = bandit.targetClanId;
23f7f1a:packages/contracts/src/ClanWorld.sol:1852:                    && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
23f7f1a:packages/contracts/src/ClanWorld.sol:1882:                        && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId
23f7f1a:packages/contracts/src/ClanWorld.sol:2139:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
23f7f1a:packages/contracts/src/ClanWorld.sol:2515:            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
23f7f1a:packages/contracts/src/ClanWorld.sol:2519:                    && currentM.targetClanId == defendTargetClanId
23f7f1a:packages/contracts/src/ClanWorld.sol:2539:        ctx.targetClanId =
23f7f1a:packages/contracts/src/ClanWorld.sol:2540:            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
23f7f1a:packages/contracts/src/ClanWorld.sol:2644:        m.targetClanId = ctx.targetClanId;
23f7f1a:packages/contracts/src/ClanWorld.sol:3046:        uint32 targetClanId = order.targetClanId == 0 ? clan.clanId : order.targetClanId;
23f7f1a:packages/contracts/src/ClanWorld.sol:3235:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3248:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
23f7f1a:packages/contracts/test/Bandit.t.sol:18:        _transitionBanditToAttacking(id, targetClanId);
23f7f1a:packages/contracts/test/Bandit.t.sol:111:        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
23f7f1a:packages/contracts/test/Bandit.t.sol:173:        world.transitionBandit(id, BanditState.Attacking);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:23:        _bandits[id].state = BanditState.Attacking;
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:24:        _bandits[id].targetClanId = targetClanId;
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:267:        uint32 targetClanId = clanIds[1];

codex
Two integrated issues are already standing out: the production heartbeat never appears to call the internal camped-to-attacking target transition, and there’s a nastier edge where settling a target during attack resolution can abort the same bandit before the resolver reuses it. I’m checking ABI consumers too, because this diff inserts tuple fields and the hand-written viem ABIs are the kind of thing that quietly ruin your afternoon.
collab: Wait
collab: Wait
collab: Wait
codex
# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
NEEDS_FIXES. The integrated phase has real blockers: production bandits never reach `Attacking`, TypeScript heartbeat/snapshot clients have stale tuple ABIs, and attack resolution can revert when target settlement kills the target mid-resolution. I would not merge until these are fixed and covered by production-path heartbeat tests.

## HIGH severity findings

`packages/contracts/src/ClanWorld.sol:1654` — Camped bandits never select a target or transition to `Attacking` in production. `_transitionBanditToAttacking` exists at `packages/contracts/src/ClanWorld.sol:1551`, but only harness tests call it; heartbeat `_advanceBanditStates` only handles `Spawned -> Camped` and `Resting -> Camped`, so `_resolveAttackingBandits` at `packages/contracts/src/ClanWorld.sol:1679` never runs for real spawned bandits. Add deterministic target selection for eligible camped bandits, call `_transitionBanditToAttacking`, and add an end-to-end spawn-to-attack heartbeat test without harness state mutation.

`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.

`packages/runner/src/runnerCastHeartbeat.ts:39` — the runner’s `getWorldState()` ABI is also stale. `WorldState` now has `currentSeasonNumber` and `nextHeartbeatAtTick` before `nextHeartbeatAtTs` at `packages/contracts/src/IClanWorld.sol:188`, but the runner expects `nextHeartbeatAtTs` immediately after `seasonFinalized`. `readNextHeartbeatAt()` can read the wrong slot or fail decoding, breaking heartbeat automation. Regenerate or update this ABI before deploy.

`packages/contracts/src/ClanWorld.sol:1717` — `_resolveBanditAttack` can revert if `_settleClan(targetClanId)` kills the target. Target death calls `_markClanDead`, which aborts attacking bandits for that target at `packages/contracts/src/ClanWorld.sol:576`; then `_resolveBanditAttack` continues and may try to transition the already-`Escaped` bandit again at `packages/contracts/src/ClanWorld.sol:1758`, which is invalid. Re-check `targetClan.clanState` and `bandit.state/targetClanId` after target and defender settlement, returning if cleanup already handled the bandit.

## MEDIUM severity findings

`packages/contracts/src/ClanWorld.sol:1605` — `Escaped` is non-terminal in the validator, but heartbeat never advances `Escaped -> Resting`. Failed attacks and dead-target aborts leave live bandits indexed and counted in `_activeBanditCount`, eventually freezing future spawns at `MAX_TOTAL_BANDITS`. Either delete escaped bandits or add automatic `Escaped -> Resting -> Camped` progression with tests.

`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.

`packages/contracts/src/ClanWorld.sol:1531` — production bandits never gain carry loot. Spawn initializes carry to zero, attack resolution emits stolen amounts as zero, and the reward tests populate carry with harness-only setters. If defender reward split is in scope for this phase, implement production loot capture or explicitly defer it and add a production-path test.

`packages/shared/src/adapters/IChainClient.ts:316` — the chain adapter always sends `targetClanId: 0`, making cross-clan defense and defender reward splitting unreachable through real TypeScript clients. Map `order.payload.targetClanId ?? 0` into contract orders and cover it with an adapter test.

## LOW severity findings

`packages/contracts/src/IClanWorld.sol:603` — both `BlueprintAwarded` and `BlueprintEarned` now exist, but the implementation emits `BlueprintEarned`. Remove or mark the older event deprecated so indexers do not split on two names.

`packages/shared/src/mocks/clanWorldFixture.ts:76` — shared mock bandit state still uses old fields and state names (`CAMPING`, `tier`, `attackPower`, no `Spawned`, no `targetClanId`, no `strength`, no `carryGold`). Deferable if demo-only, but it will keep UI/tests out of sync with the Phase 9 ABI.

## Cross-cutting observations

The strongest pattern is that contract unit tests exercise internal harness paths, while the real heartbeat orchestration is undercovered. Add a small number of production-path tests for spawn → camp → target → attack, failed attack → escaped/resting lifecycle, target death during attack settlement, en-route defender cleanup, and real loot acquisition/distribution.

The ABI source of truth is split: regenerated Solidity ABI exists, but real consumers use hand-written minimal ABIs. This PR is ABI-breaking, so deploy should be lockstep with regenerated TS consumers and a clear rollback plan.
2026-04-29T17:05:07.956411Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-645f-7fe0-81c2-f6f8c6a3cdcb not found
2026-04-29T17:05:07.957619Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-dc02-79e3-a1ce-149ba9327f7c not found
2026-04-29T17:05:07.957677Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-db76-7842-8539-22fa208bdcc0 not found
2026-04-29T17:05:07.957885Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-dadd-7582-9151-3e864895c1c5 not found
tokens used
361,261
# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
NEEDS_FIXES. The integrated phase has real blockers: production bandits never reach `Attacking`, TypeScript heartbeat/snapshot clients have stale tuple ABIs, and attack resolution can revert when target settlement kills the target mid-resolution. I would not merge until these are fixed and covered by production-path heartbeat tests.

## HIGH severity findings

`packages/contracts/src/ClanWorld.sol:1654` — Camped bandits never select a target or transition to `Attacking` in production. `_transitionBanditToAttacking` exists at `packages/contracts/src/ClanWorld.sol:1551`, but only harness tests call it; heartbeat `_advanceBanditStates` only handles `Spawned -> Camped` and `Resting -> Camped`, so `_resolveAttackingBandits` at `packages/contracts/src/ClanWorld.sol:1679` never runs for real spawned bandits. Add deterministic target selection for eligible camped bandits, call `_transitionBanditToAttacking`, and add an end-to-end spawn-to-attack heartbeat test without harness state mutation.

`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.

`packages/runner/src/runnerCastHeartbeat.ts:39` — the runner’s `getWorldState()` ABI is also stale. `WorldState` now has `currentSeasonNumber` and `nextHeartbeatAtTick` before `nextHeartbeatAtTs` at `packages/contracts/src/IClanWorld.sol:188`, but the runner expects `nextHeartbeatAtTs` immediately after `seasonFinalized`. `readNextHeartbeatAt()` can read the wrong slot or fail decoding, breaking heartbeat automation. Regenerate or update this ABI before deploy.

`packages/contracts/src/ClanWorld.sol:1717` — `_resolveBanditAttack` can revert if `_settleClan(targetClanId)` kills the target. Target death calls `_markClanDead`, which aborts attacking bandits for that target at `packages/contracts/src/ClanWorld.sol:576`; then `_resolveBanditAttack` continues and may try to transition the already-`Escaped` bandit again at `packages/contracts/src/ClanWorld.sol:1758`, which is invalid. Re-check `targetClan.clanState` and `bandit.state/targetClanId` after target and defender settlement, returning if cleanup already handled the bandit.

## MEDIUM severity findings

`packages/contracts/src/ClanWorld.sol:1605` — `Escaped` is non-terminal in the validator, but heartbeat never advances `Escaped -> Resting`. Failed attacks and dead-target aborts leave live bandits indexed and counted in `_activeBanditCount`, eventually freezing future spawns at `MAX_TOTAL_BANDITS`. Either delete escaped bandits or add automatic `Escaped -> Resting -> Camped` progression with tests.

`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.

`packages/contracts/src/ClanWorld.sol:1531` — production bandits never gain carry loot. Spawn initializes carry to zero, attack resolution emits stolen amounts as zero, and the reward tests populate carry with harness-only setters. If defender reward split is in scope for this phase, implement production loot capture or explicitly defer it and add a production-path test.

`packages/shared/src/adapters/IChainClient.ts:316` — the chain adapter always sends `targetClanId: 0`, making cross-clan defense and defender reward splitting unreachable through real TypeScript clients. Map `order.payload.targetClanId ?? 0` into contract orders and cover it with an adapter test.

## LOW severity findings

`packages/contracts/src/IClanWorld.sol:603` — both `BlueprintAwarded` and `BlueprintEarned` now exist, but the implementation emits `BlueprintEarned`. Remove or mark the older event deprecated so indexers do not split on two names.

`packages/shared/src/mocks/clanWorldFixture.ts:76` — shared mock bandit state still uses old fields and state names (`CAMPING`, `tier`, `attackPower`, no `Spawned`, no `targetClanId`, no `strength`, no `carryGold`). Deferable if demo-only, but it will keep UI/tests out of sync with the Phase 9 ABI.

## Cross-cutting observations

The strongest pattern is that contract unit tests exercise internal harness paths, while the real heartbeat orchestration is undercovered. Add a small number of production-path tests for spawn → camp → target → attack, failed attack → escaped/resting lifecycle, target death during attack settlement, en-route defender cleanup, and real loot acquisition/distribution.

The ABI source of truth is split: regenerated Solidity ABI exists, but real consumers use hand-written minimal ABIs. This PR is ABI-breaking, so deploy should be lockstep with regenerated TS consumers and a clear rollback plan.
