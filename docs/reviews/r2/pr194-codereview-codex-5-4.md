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
session id: 019dda2f-5d1a-70b1-862c-5f07af1d4125
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
Reviewing the Phase 9 bandit diff end-to-end now. I’m going to trace the contract state machine, heartbeat ordering, and test coverage in parallel so we can catch integration bugs instead of just local issues.
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
    uint8  internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;

    // Clansman cadence
    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;

    // Carry caps (per clansman)
    uint256 internal constant WOOD_CAP  = 15e18;
    uint256 internal constant IRON_CAP  = 5e18;
    uint256 internal constant WHEAT_CAP = 40e18;
    uint256 internal constant FISH_CAP  = 8e18;

    // Gathering yields
    uint256 internal constant WOOD_BASE_YIELD = 2e18;
    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
    uint16  internal constant WOOD_CRIT_BPS = 2000;            // 20%

    uint256 internal constant IRON_BASE_YIELD = 5e17;          // 0.5e18
    uint16  internal constant GOLD_FROM_IRON_BPS = 200;        // 2%
    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;

    uint16  internal constant FISH_DOCKS_BPS = 2500;           // 25%
    uint16  internal constant FISH_DEEP_BPS = 7500;            // 75%

    // Upkeep
    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    uint16  internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x

    // Wheat plots
    uint64  internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;

    // Bandit combat
    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000;        // 20%
    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%

    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    uint8 internal constant REGION_NOOP          = 0;
    uint8 internal constant REGION_FOREST        = 1;
    uint8 internal constant REGION_MOUNTAINS     = 2;
    uint8 internal constant REGION_UNICORN_TOWN  = 3;
    uint8 internal constant REGION_WEST_FARMS    = 4;
    uint8 internal constant REGION_EAST_FARMS    = 5;
    uint8 internal constant REGION_WEST_DOCKS    = 6;
    uint8 internal constant REGION_EAST_DOCKS    = 7;
    uint8 internal constant REGION_DEEP_SEA      = 8;

    // Sentinels
    uint32 internal constant CLAN_ID_NULL = 0;     // valid clan IDs start at 1
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

enum BanditState {
    NONE,
    CAMPING,
    RESTING,
    ATTACKING,
    DEFEATED,
    ESCAPED
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
    bool   seasonFinalized;

    uint64 nextHeartbeatAtTs;
    uint64 nextBanditSpawnEligibleTick;
    uint16 currentBanditSpawnChanceBps;
    bytes32 currentTickSeed;

    uint32 activeBanditId;       // 0 if none
    bool   winterActive;
    uint64 winterStartsAtTick;
    uint64 winterEndsAtTick;     // 0 if not active

    uint64 nextCommitSequence;   // global FIFO sequence for scheduled market actions
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

    uint16 coldDamage;             // resets to 0 at winter end

    uint256 goldBalance;
    uint256 blueprintBalance;

    uint256 vaultWood;
    uint256 vaultIron;
    uint256 vaultWheat;
    uint256 vaultFish;
}

struct WheatPlot {
    WheatPlotState state;
    uint8 region;                  // West Farms or East Farms
    uint256 remainingWheat;
    uint64 regrowUntilTick;
}

struct Clansman {
    uint32 clansmanId;
    uint32 clanId;
    ClansmanState state;

exec
/bin/bash -lc "rg -n \"enum BanditState|struct BanditTroop|heartbeat\\(|_settleClan\\(|_settleCompletingMissions\\(|_resolveWorldEvents\\(|DefendBase|getActiveDefenders|submitClanOrders|_clearDefender|_applyUpkeep\\(|currentTickSeed|TickAdvanced|currentSeasonNumber|nextHeartbeatAtTick|getWorldState\\(\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/test/ClanWorldStub.t.sol:27:        assertEq(stub.getWorldState().currentTick, 1);
packages/contracts/test/ClanWorldStub.t.sol:28:        stub.heartbeat();
packages/contracts/test/ClanWorldStub.t.sol:29:        assertEq(stub.getWorldState().currentTick, 2);
packages/contracts/test/ClanWorldStub.t.sol:33:        stub.heartbeat();
packages/contracts/src/ClanWorldStub.sol:70:    function heartbeat() external override {
packages/contracts/src/ClanWorldStub.sol:74:        emit TickAdvanced(closed, _world.currentTick, bytes32(0));
packages/contracts/src/ClanWorldStub.sol:89:    function submitClanOrders(uint32, ClanOrder[] calldata orders)
packages/contracts/src/ClanWorldStub.sol:122:    function getWorldState() external view override returns (WorldState memory) {
packages/contracts/src/ClanWorldStub.sol:224:    function getActiveDefenders(uint32)
packages/contracts/src/ClanWorldStub.sol:278:            currentTickSeed: bytes32(0),
packages/contracts/test/ClanWorld.t.sol:122:        world.heartbeat();
packages/contracts/test/ClanWorld.t.sol:145:        return world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:154:        world.heartbeat();
packages/contracts/test/ClanWorld.t.sol:157:        world.heartbeat();
packages/contracts/test/ClanWorld.t.sol:165:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
packages/contracts/test/ClanWorld.t.sol:169:        world.heartbeat();
packages/contracts/test/ClanWorld.t.sol:171:        bytes32 seedAfter = world.getWorldState().currentTickSeed;
packages/contracts/test/ClanWorld.t.sol:258:        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:306:        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:431:    // Test B: submitClanOrders returns ERR_INVALID_ACTION when clan is >200 ticks behind
packages/contracts/test/ClanWorld.t.sol:434:    function test_submitClanOrders_reverts_when_clan_too_far_behind() public {
packages/contracts/test/ClanWorld.t.sol:444:        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
packages/contracts/test/ClanWorld.t.sol:457:        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:522:        return world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:551:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:587:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:616:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:655:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:692:        OrderResult[] memory r1 = world.submitClanOrders(clanId1, orders1);
packages/contracts/test/ClanWorld.t.sol:707:        OrderResult[] memory r2 = world.submitClanOrders(clanId2, orders2);
packages/contracts/test/ClanWorld.t.sol:729:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:767:        world.submitClanOrders(clanId1, sellOrders);
packages/contracts/test/ClanWorld.t.sol:781:        world.submitClanOrders(clanId2, buyOrders);
packages/contracts/test/ClanWorld.t.sol:787:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:825:        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:830:    // Issue #95: DefendBase re-tasking — zero-travel same-target retask must not drop defender
packages/contracts/test/ClanWorld.t.sol:839:        // First DefendBase: clansman defends its own clan (same region → zero travel).
packages/contracts/test/ClanWorld.t.sol:844:            action: ActionType.DefendBase,
packages/contracts/test/ClanWorld.t.sol:851:        OrderResult[] memory r1 = world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:852:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first DefendBase OK");
packages/contracts/test/ClanWorld.t.sol:858:        uint32[] memory defs = world.getActiveDefenders(clanId);
packages/contracts/test/ClanWorld.t.sol:862:        // Second DefendBase to same target: zero-travel re-task — the regression case.
packages/contracts/test/ClanWorld.t.sol:864:        OrderResult[] memory r2 = world.submitClanOrders(clanId, orders);
packages/contracts/test/ClanWorld.t.sol:865:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "re-task DefendBase OK");
packages/contracts/test/ClanWorld.t.sol:868:        defs = world.getActiveDefenders(clanId);
packages/contracts/src/ClanWorld.sol:227:    function _settleClan(uint32 clanId) internal {
packages/contracts/src/ClanWorld.sol:247:            _applyUpkeep(clan, tick);
packages/contracts/src/ClanWorld.sol:288:    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:350:        } else if (action == ActionType.DefendBase) {
packages/contracts/src/ClanWorld.sol:365:            // Scheduled market actions: already enqueued at submitClanOrders time.
packages/contracts/src/ClanWorld.sol:724:    function heartbeat() external override {
packages/contracts/src/ClanWorld.sol:733:        _world.currentTickSeed = newSeed;
packages/contracts/src/ClanWorld.sol:741:        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
packages/contracts/src/ClanWorld.sol:746:        _settleClan(clanId);
packages/contracts/src/ClanWorld.sol:836:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
packages/contracts/src/ClanWorld.sol:866:        _settleClan(clanId);
packages/contracts/src/ClanWorld.sol:963:        // DefendBase: deregister old assignment immediately (clansman is being re-tasked).
packages/contracts/src/ClanWorld.sol:979:        // DefendBase zero-travel: register synchronously so getActiveDefenders() has no drop window.
packages/contracts/src/ClanWorld.sol:982:        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
packages/contracts/src/ClanWorld.sol:1023:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
packages/contracts/src/ClanWorld.sol:1325:        // DefendBase: targetClanId must be valid and gotoRegion must match target's baseRegion
packages/contracts/src/ClanWorld.sol:1326:        if (action == ActionType.DefendBase) {
packages/contracts/src/ClanWorld.sol:1444:    function getWorldState() external view override returns (WorldState memory) {
packages/contracts/src/ClanWorld.sol:1500:    function getActiveDefenders(uint32 targetClanId)
packages/contracts/src/ClanWorld.sol:1627:            currentTickSeed: _world.currentTickSeed,
packages/contracts/src/IClanWorld.sol:106:enum BanditState {
packages/contracts/src/IClanWorld.sol:139:    DefendBase,
packages/contracts/src/IClanWorld.sol:195:    bytes32 currentTickSeed;
packages/contracts/src/IClanWorld.sol:289:    uint32 targetClanId;   // DefendBase only
packages/contracts/src/IClanWorld.sol:295:struct BanditTroop {
packages/contracts/src/IClanWorld.sol:363:    uint32 targetClanId;          // DefendBase only
packages/contracts/src/IClanWorld.sol:411:    bytes32 currentTickSeed;
packages/contracts/src/IClanWorld.sol:483:    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
packages/contracts/src/IClanWorld.sol:667:    function heartbeat() external;
packages/contracts/src/IClanWorld.sol:684:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
packages/contracts/src/IClanWorld.sol:725:    function getWorldState() external view returns (WorldState memory);
packages/contracts/src/IClanWorld.sol:747:    function getActiveDefenders(uint32 targetClanId)

exec
/bin/bash -lc "sed -n '1,420p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
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
import {StubPool} from "./StubPool.sol";

/// @title ClanWorld
/// @notice Phase 1+2 real engine implementation of IClanWorld v4.
///         Implements: world clock, clan lifecycle, lazy settlement, resource gathering,
///         deposit, wheat harvest, travel, NOOP bypass, order validation.
///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
contract ClanWorld is IClanWorld {

    // =========================================================================
    // STORAGE
    // =========================================================================

    WorldState private _world;
    TreasuryState private _treasury;

    mapping(uint32 => Clan) private _clans;
    mapping(uint32 => Clansman) private _clansmen;
    mapping(uint32 => Mission) private _missions;              // keyed by clansmanId
    mapping(uint32 => WheatPlot[2]) private _wheatPlots;       // [0]=west [1]=east
    mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
    mapping(uint32 => uint32[]) private _incomingDefenders;    // targetClanId => clansmanIds
    mapping(uint32 => uint32) private _clanDefendingBase;      // clansmanId => targetClanId
    mapping(uint64 => bytes32) private _tickSeeds;              // tick => seed

    uint32 private _nextClanId;
    uint32 private _nextClansmanId;
    uint32[] private _allClanIds;

    // per-clan clansman list: clanId => clansmanId[]
    mapping(uint32 => uint32[]) private _clanClansmanIds;

    // =========================================================================
    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    // =========================================================================

    uint256 private constant WHEAT_HARVEST_RATE = 20e18;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    constructor() {
        _world.currentTick = 0;
        _world.nextHeartbeatAtTs = uint64(block.timestamp);
        _world.seasonStartTick = 0;
        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
        _treasury.treasuryOwner = msg.sender;
        _nextClanId = 1;
        _nextClansmanId = 1;
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
            0, 1, 2, 1, 3, 2, 4, 3,
            // src=2: to 1,2,3,4,5,6,7,8
            1, 0, 1, 2, 2, 3, 3, 4,
            // src=3: to 1,2,3,4,5,6,7,8
            2, 1, 0, 1, 1, 2, 2, 3,
            // src=4: to 1,2,3,4,5,6,7,8
            1, 2, 1, 0, 2, 1, 3, 2,
            // src=5: to 1,2,3,4,5,6,7,8
            3, 2, 1, 2, 0, 3, 1, 2,
            // src=6: to 1,2,3,4,5,6,7,8
            2, 3, 2, 1, 3, 0, 2, 1,
            // src=7: to 1,2,3,4,5,6,7,8
            4, 3, 2, 3, 1, 2, 0, 1,
            // src=8: to 1,2,3,4,5,6,7,8
            3, 4, 3, 2, 2, 1, 1, 0
        ];
        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
    }

    // Build a canonical path from src to dst (BFS on the adjacency graph).
    // Returns packed bytes8: region IDs in order, zero-padded.
    function _buildPath(uint8 src, uint8 dst) private pure returns (bytes8) {
        if (src == dst) {
            return bytes8(uint64(src) << 56);
        }
        // Adjacency list (1-indexed, 0 = end sentinel)
        // adj[r] = neighbors of region r (up to 3)
        uint8[3][9] memory adj = [
            [0, 0, 0],       // 0: unused
            [2, 4, 0],       // 1: Forest
            [1, 3, 0],       // 2: Mountains
            [2, 4, 5],       // 3: UnicornTown
            [1, 3, 6],       // 4: WestFarms
            [3, 7, 0],       // 5: EastFarms
            [4, 8, 0],       // 6: WestDocks
            [5, 8, 0],       // 7: EastDocks
            [6, 7, 0]        // 8: DeepSea
        ];

        // BFS with parent tracking (max 8 nodes)
        uint8[9] memory parent;
        bool[9] memory visited;
        uint8[9] memory queue;
        uint256 head;
        uint256 tail;

        for (uint256 i = 0; i < 9; i++) {
            parent[i] = 0;
            visited[i] = false;
        }

        visited[src] = true;
        queue[tail++] = src;

        while (head < tail) {
            uint8 curr = queue[head++];
            if (curr == dst) break;
            for (uint256 ni = 0; ni < 3; ni++) {
                uint8 nb = adj[curr][ni];
                if (nb == 0) break;
                if (!visited[nb]) {
                    visited[nb] = true;
                    parent[nb] = curr;
                    queue[tail++] = nb;
                }
            }
        }

        // Reconstruct path backwards
        uint8[8] memory path;
        uint256 pathLen;
        uint8 cur = dst;
        while (cur != src) {
            path[pathLen++] = cur;
            cur = parent[cur];
        }
        path[pathLen++] = src;

        // Reverse into result
        bytes8 packed;
        uint64 byteShift = 56;
        for (uint256 i = pathLen; i > 0; i--) {
            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
            if (byteShift >= 8) byteShift -= 8;
        }
        return packed;
    }

    function _travelTicks(uint8 fromRegion, uint8 toRegion) private pure returns (uint8) {
        if (fromRegion == 0 || toRegion == 0) return 0; // NOOP region
        if (fromRegion == toRegion) return 0;
        if (fromRegion > 8 || toRegion > 8) return 0;
        return _distMatrix(fromRegion, toRegion);
    }

    // =========================================================================
    // INTERNAL SETTLEMENT
    // =========================================================================

    /// @dev Lazy settlement of a clan forward to currentTick.
    ///      Mutates storage. Called before order submission and by public settleClan().
    function _settleClan(uint32 clanId) internal {
        Clan storage clan = _clans[clanId];
        if (clan.clanId == 0) return;

        uint64 curTick = _world.currentTick;
        uint64 fromTick = clan.lastSettledTick;
        if (fromTick >= curTick) return;

        // Cap ticks settled per call to prevent block gas limit issues
        uint64 maxSettleTicks = 200;
        if (curTick > fromTick + maxSettleTicks) {
            curTick = fromTick + maxSettleTicks;
        }

        uint32[] storage clansmanIds = _clanClansmanIds[clanId];

        // Settle tick by tick from fromTick to curTick - 1
        // (curTick is still open; we settle through the last closed tick)
        for (uint64 tick = fromTick; tick < curTick; tick++) {
            // 1. Apply upkeep for this tick
            _applyUpkeep(clan, tick);

            // 2. Wheat plot regrow check (lazy, per tick)
            for (uint256 pi = 0; pi < 2; pi++) {
                WheatPlot storage plot = _wheatPlots[clanId][pi];
                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
                    plot.state = WheatPlotState.Harvestable;
                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
                    plot.regrowUntilTick = 0;
                }
            }

            // 3. Advance each clansman
            bytes32 tickSeed = _tickSeeds[tick];
            for (uint256 i = 0; i < clansmanIds.length; i++) {
                uint32 csId = clansmanIds[i];
                Clansman storage cs = _clansmen[csId];
                if (cs.state == ClansmanState.DEAD) continue;

                Mission storage m = _missions[csId];
                if (!m.active) continue;

                // Travel advancement: if still traveling and this tick >= arrivalTick, arrive
                if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
                    cs.state = ClansmanState.ACTING;
                    cs.currentRegion = m.targetRegion;
                    emit WorkerArrived(clanId, csId, m.targetRegion, tick);
                }

                // Action resolution: if acting and this tick >= actionStartTick
                if (cs.state == ClansmanState.ACTING && tick >= m.actionStartTick) {
                    _resolveAction(clan, cs, m, clanId, tick, tickSeed);
                }
            }
        }

        clan.lastSettledTick = curTick;
        emit ClanSettled(clanId, curTick);
    }

    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
        if (clan.livingClansmen == 0) return;

        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
        uint256 fishNeeded  = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;

        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
        bool hadEnoughFish  = clan.vaultFish  >= fishNeeded;

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
    }

    /// @dev Check if a clan is currently starving (lazy read).
    function _isStarving(Clan storage clan) internal view returns (bool) {
        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
    }

    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
    function _resolveAction(
        Clan storage clan,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bytes32 tickSeed
    ) internal {
        bool starving = _isStarving(clan);
        ActionType action = m.action;

        if (action == ActionType.ChopWood) {
            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.MineIron) {
            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.FishDocks) {
            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.FishDeepSea) {
            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
        } else if (action == ActionType.HarvestWheat) {
            _gatherWheat(clan, cs, m, clanId, tick, starving);
        } else if (action == ActionType.DepositResources) {
            _doDeposit(clan, cs, m, clanId, tick);
        } else if (action == ActionType.Wait) {
            // NOOP — worker stays ACTING (continuous), no transition needed
            // Wait mission is effectively persistent until interrupted
        } else if (action == ActionType.DefendBase) {
            // Register defender at arrival (not at submission) per v4.3 — ensures only arrived clansmen counted.
            // Guard: only register once (this branch executes every ACTING tick; idempotent via _clanDefendingBase check).
            if (_clanDefendingBase[cs.clansmanId] != m.targetClanId) {
                _incomingDefenders[m.targetClanId].push(cs.clansmanId);
                _clanDefendingBase[cs.clansmanId] = m.targetClanId;
            }
        } else if (
            action == ActionType.BuildWall ||
            action == ActionType.UpgradeBase ||
            action == ActionType.UpgradeMonument
        ) {
            // Phase 1 stub: check homebase, check resources; if ok, stub success
            _doBuilding(clan, cs, m, clanId, tick, action);
        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            // Scheduled market actions: already enqueued at submitClanOrders time.
            // Settlement resolves this action slot — just complete the mission.
            // (Actual execution happened or will happen at heartbeat.)
            _completeMission(cs, m);
        }
    }

    // -------------------------------------------------------------------------
    // Gathering helpers
    // -------------------------------------------------------------------------

    function _gatherWood(
        Clan storage, // clan (unused — no clan-level mutation in wood gather)
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
        // Crit roll: domain-separated RNG
        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 critRoll = uint256(critRng) % 10000;
        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
        }
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        cs.carryWood += yield;

        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);

        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
            _completeMission(cs, m);
        }
        // else continuous — worker stays ACTING
    }

    function _gatherIron(
        Clan storage clan,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
        if (remaining == 0) {
            _completeMission(cs, m);

exec
/bin/bash -lc "sed -n '420,920p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
            _completeMission(cs, m);
            return;
        }
        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        cs.carryIron += yield;

        // Gold bonus roll — scoped to reduce stack depth
        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);

        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);

        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
            _completeMission(cs, m);
        }
    }

    function _rollIronGoldBonus(
        Clan storage clan,
        uint32 clansmanId,
        uint64 nonce,
        uint64 tick,
        bytes32 tickSeed
    ) internal returns (uint256 goldBonus) {
        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
            clan.goldBalance += goldBonus;
        }
    }

    function _gatherFishDocks(
        Clan storage,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 fishRoll = uint256(fishRng) % 10000;
        uint256 yield = 0;
        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
            yield = 1e18;
        }
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        if (yield > 0) {
            cs.carryFish += yield;
            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
        }
        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
            _completeMission(cs, m);
        }
    }

    function _gatherFishDeepSea(
        Clan storage,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving,
        bytes32 tickSeed
    ) internal {
        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
        uint256 fishRoll = uint256(fishRng) % 10000;
        uint256 yield = 0;
        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
            yield = 1e18;
        }
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        if (yield > 0) {
            cs.carryFish += yield;
            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
        }
        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
            _completeMission(cs, m);
        }
    }

    function _gatherWheat(
        Clan storage /* clan — unused but kept positional for callsite parity */,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving
    ) internal {
        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
        if (remaining == 0) {
            _completeMission(cs, m);
            return;
        }
        // Determine plot index from region
        uint8 region = m.targetRegion;
        uint256 plotIdx;
        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
            plotIdx = 0;
        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
            plotIdx = 1;
        } else {
            // Wrong region — complete (no harvest)
            _completeMission(cs, m);
            return;
        }

        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
            // Plot not ready — worker waits
            _completeMission(cs, m);
            return;
        }

        uint256 yield = WHEAT_HARVEST_RATE;
        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        if (yield > plot.remainingWheat) yield = plot.remainingWheat;

        cs.carryWheat += yield;
        plot.remainingWheat -= yield;

        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);

        if (plot.remainingWheat == 0) {
            plot.state = WheatPlotState.Regrowing;
            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
        }

        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
            _completeMission(cs, m);
        }
        // else continuous
    }

    function _doDeposit(
        Clan storage clan,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick
    ) internal {
        // Must be at homebase region
        if (cs.currentRegion != clan.baseRegion) {
            _completeMission(cs, m);
            return;
        }
        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
        if (!hasAnything) {
            _completeMission(cs, m);
            return;
        }

        uint256 w = cs.carryWood;
        uint256 ir = cs.carryIron;
        uint256 wh = cs.carryWheat;
        uint256 fi = cs.carryFish;

        clan.vaultWood  += w;
        clan.vaultIron  += ir;
        clan.vaultWheat += wh;
        clan.vaultFish  += fi;

        cs.carryWood  = 0;
        cs.carryIron  = 0;
        cs.carryWheat = 0;
        cs.carryFish  = 0;

        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
        _completeMission(cs, m);
    }

    function _doBuilding(
        Clan storage clan,
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        ActionType action
    ) internal {
        // Must be at homebase
        if (cs.currentRegion != clan.baseRegion) {
            _completeMission(cs, m);
            return;
        }

        bool success = false;
        if (action == ActionType.BuildWall) {
            success = _tryBuildWall(clan, clanId, tick);
        } else if (action == ActionType.UpgradeBase) {
            success = _tryUpgradeBase(clan, clanId, tick);
        } else if (action == ActionType.UpgradeMonument) {
            success = _tryUpgradeMonument(clan, clanId, tick);
        }

        if (!success) {
            // Resources missing — worker transitions to WAITING
        }
        _completeMission(cs, m);
    }

    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
        uint8 nextLevel = clan.wallLevel + 1;
        if (nextLevel > 5) return false;

        uint256 woodCost;
        uint256 ironCost;

        if (nextLevel == 1) { woodCost = 20e18; ironCost = 0; }
        else if (nextLevel == 2) { woodCost = 35e18; ironCost = 0; }
        else if (nextLevel == 3) { woodCost = 30e18; ironCost = 5e18; }
        else if (nextLevel == 4) { woodCost = 40e18; ironCost = 10e18; }
        else { woodCost = 50e18; ironCost = 15e18; }

        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;

        clan.vaultWood -= woodCost;
        clan.vaultIron -= ironCost;
        uint8 old = clan.wallLevel;
        clan.wallLevel = nextLevel;
        emit WallLevelChanged(clanId, old, nextLevel, tick);
        return true;
    }

    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
        uint8 nextLevel = clan.baseLevel + 1;
        if (nextLevel > 5) return false;

        uint256 woodCost;
        uint256 ironCost;
        uint256 wheatCost;

        if (nextLevel == 2) { woodCost = 40e18; ironCost = 0; wheatCost = 20e18; }
        else if (nextLevel == 3) { woodCost = 60e18; ironCost = 5e18; wheatCost = 30e18; }
        else if (nextLevel == 4) { woodCost = 80e18; ironCost = 10e18; wheatCost = 40e18; }
        else { woodCost = 100e18; ironCost = 15e18; wheatCost = 50e18; }

        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost || clan.vaultWheat < wheatCost) return false;

        clan.vaultWood  -= woodCost;
        clan.vaultIron  -= ironCost;
        clan.vaultWheat -= wheatCost;
        uint8 old = clan.baseLevel;
        clan.baseLevel = nextLevel;
        emit BaseLevelChanged(clanId, old, nextLevel, tick);
        return true;
    }

    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
        uint8 nextLevel = clan.monumentLevel + 1;
        if (nextLevel > 10) return false;

        uint256 woodCost;
        uint256 wheatCost;
        uint256 ironCost;
        uint256 blueprintCost;

        if (nextLevel == 1)  { woodCost = 30e18;  wheatCost = 20e18; }
        else if (nextLevel == 2)  { woodCost = 50e18;  wheatCost = 30e18; }
        else if (nextLevel == 3)  { woodCost = 70e18;  wheatCost = 40e18; ironCost = 5e18; }
        else if (nextLevel == 4)  { woodCost = 90e18;  wheatCost = 50e18; ironCost = 10e18; }
        else if (nextLevel == 5)  { woodCost = 120e18; wheatCost = 60e18; ironCost = 15e18; }
        else if (nextLevel == 6)  { woodCost = 150e18; wheatCost = 80e18; ironCost = 20e18; }
        else if (nextLevel <= 10) { woodCost = 200e18; wheatCost = 100e18; ironCost = 25e18; blueprintCost = 1e18; }

        if (clan.vaultWood < woodCost || clan.vaultWheat < wheatCost ||
            clan.vaultIron < ironCost || clan.blueprintBalance < blueprintCost) return false;

        clan.vaultWood        -= woodCost;
        clan.vaultWheat       -= wheatCost;
        clan.vaultIron        -= ironCost;
        clan.blueprintBalance -= blueprintCost;

        uint8 old = clan.monumentLevel;
        clan.monumentLevel = nextLevel;
        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
        return true;
    }

    /// @dev Complete a mission: set worker to WAITING, mark mission inactive, emit event.
    function _completeMission(Clansman storage cs, Mission storage m) internal {
        cs.state = ClansmanState.WAITING;
        m.active = false;
        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
    }

    // =========================================================================
    // WORLD PROGRESSION
    // =========================================================================

    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
    function heartbeat() external override {
        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");

        uint64 closedTick = _world.currentTick;
        _world.currentTick = closedTick + 1;                                    // increment first

        // Derive tick seed: domain-separated from block randomness; stored under NEW tick
        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, closedTick, block.timestamp));
        _tickSeeds[_world.currentTick] = newSeed;                               // store under new tick
        _world.currentTickSeed = newSeed;

        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;

        // Phase 2: execute scheduled market actions for closedTick
        _executeScheduledMarketActions(closedTick);
        // TODO Phase 3: bandit state transitions and attacks

        emit TickAdvanced(closedTick, _world.currentTick, _world.currentTickSeed);
    }

    /// @notice Public settlement trigger — lazily settle a clan.
    function settleClan(uint32 clanId) external override {
        _settleClan(clanId);
    }

    /// @notice Finalize season. Phase 1 stub.
    function finalizeSeason() external override {
        // TODO Phase 3
    }

    // =========================================================================
    // CLAN LIFECYCLE
    // =========================================================================

    /// @notice Mint a new clan and spawn its homebase.
    function mintClan(address to) external override returns (uint32 clanId, uint256 iftTokenId) {
        require(to != address(0), "ClanWorld: zero address");
        require(_allClanIds.length < 12, "ClanWorld: max clans");
        clanId = _nextClanId++;
        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7

        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
        uint8[6] memory spawnRegions = [
            ClanWorldConstants.REGION_FOREST,
            ClanWorldConstants.REGION_MOUNTAINS,
            ClanWorldConstants.REGION_WEST_FARMS,
            ClanWorldConstants.REGION_EAST_FARMS,
            ClanWorldConstants.REGION_WEST_DOCKS,
            ClanWorldConstants.REGION_EAST_DOCKS
        ];
        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];

        // Create clan
        Clan storage clan = _clans[clanId];
        clan.clanId = clanId;
        clan.iftTokenId = iftTokenId;
        clan.owner = to;
        clan.clanState = ClanState.ACTIVE;
        clan.baseRegion = baseRegion;
        clan.baseLevel = 1;
        clan.wallLevel = 0;
        clan.monumentLevel = 0;
        clan.livingClansmen = 4;
        clan.lastSettledTick = _world.currentTick;
        clan.starvationStartsAtTick = 0;
        clan.coldDamage = 0;
        clan.goldBalance = 3e18;  // starter gold per v4 spec §12.5
        clan.blueprintBalance = 0;
        // Starting vault per v4 spec §12.4
        clan.vaultWood  = 20e18;
        clan.vaultIron  = 0;
        clan.vaultWheat = 20e18;
        clan.vaultFish  = 2e18;

        // Wheat plots
        _wheatPlots[clanId][0] = WheatPlot({
            state: WheatPlotState.Harvestable,
            region: ClanWorldConstants.REGION_WEST_FARMS,
            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
            regrowUntilTick: 0
        });
        _wheatPlots[clanId][1] = WheatPlot({
            state: WheatPlotState.Harvestable,
            region: ClanWorldConstants.REGION_EAST_FARMS,
            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
            regrowUntilTick: 0
        });

        // Create 4 clansmen
        for (uint256 i = 0; i < 4; i++) {
            uint32 csId = _nextClansmanId++;
            Clansman storage cs = _clansmen[csId];
            cs.clansmanId = csId;
            cs.clanId = clanId;
            cs.state = ClansmanState.WAITING;
            cs.currentRegion = baseRegion;
            cs.cooldownEndsAtTs = 0;
            cs.lastMissionNonce = 0;
            cs.carryWood  = 0;
            cs.carryIron  = 0;
            cs.carryWheat = 0;
            cs.carryFish  = 0;
            _clanClansmanIds[clanId].push(csId);
        }

        _allClanIds.push(clanId);

        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
        return (clanId, iftTokenId);
    }

    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
        external
        override
        returns (OrderResult[] memory results)
    {
        Clan storage clan = _clans[clanId];
        require(clan.clanId != 0, "ClanWorld: clan not found");
        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");

        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
        {
            uint64 lastSettled = _clans[clanId].lastSettledTick;
            if (_world.currentTick > lastSettled + 200) {
                results = new OrderResult[](orders.length);
                for (uint256 i = 0; i < orders.length; i++) {
                    results[i] = OrderResult({
                        clansmanId: orders[i].clansmanId,
                        status: StatusCode.ERR_INVALID_ACTION,
                        cooldownEndsAtTs: 0,
                        missionNonce: 0
                    });
                }
                return results;
            }
        }

        // Lazy settle before processing orders
        _settleClan(clanId);

        results = new OrderResult[](orders.length);

        for (uint256 i = 0; i < orders.length; i++) {
            results[i] = _processOrder(clanId, clan, orders[i]);
        }

        return results;
    }

    struct OrderCtx {
        uint8 gotoRegion;
        uint8 fromRegion;
        bool isNoop;
        uint8 travelTicks;
        uint64 arrivalTick;
        uint64 newNonce;
        bool wasActive;
        uint64 oldNonce;
    }

    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
        internal
        returns (OrderResult memory result)
    {
        result.clansmanId = order.clansmanId;

        // Validate clansman
        Clansman storage cs = _clansmen[order.clansmanId];
        if (cs.clansmanId == 0 || cs.clanId != clanId) {
            result.status = StatusCode.ERR_INVALID_CLANSMAN;
            return result;
        }
        if (cs.state == ClansmanState.DEAD) {
            result.status = StatusCode.ERR_CLANSMAN_DEAD;
            return result;
        }
        // Cooldown check
        if (block.timestamp < cs.cooldownEndsAtTs) {
            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
            result.missionNonce = cs.lastMissionNonce;
            return result;
        }

        OrderCtx memory ctx;
        ctx.fromRegion = cs.currentRegion;
        ctx.gotoRegion = order.gotoRegion;

        // NOOP bypass: treat 0 as "stay here"
        ctx.isNoop = (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
        if (ctx.isNoop) {
            ctx.gotoRegion = ctx.fromRegion;
        }

codex
I’ve got the baseline contract shape loaded now. Next I’m splitting the review into three parallel passes: bandit lifecycle + heartbeat, derived-settlement simulation, and ABI/test/integration seams so we can compare conclusions instead of trusting one path.
2026-04-29T17:00:56.319827Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T17:00:56.327804Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T17:00:56.332763Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "sed -n '920,1110p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        }

        // Validate target region (1-8 or 0=noop)
        if (ctx.gotoRegion > 8) {
            result.status = StatusCode.ERR_INVALID_REGION;
            return result;
        }

        // Validate action
        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
        if (actionErr != StatusCode.OK) {
            result.status = actionErr;
            return result;
        }

        // Capture existing mission state
        Mission storage existingM = _missions[order.clansmanId];
        ctx.wasActive = existingM.active;
        ctx.oldNonce = existingM.nonce;

        // Compute travel
        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
        ctx.arrivalTick = _world.currentTick + uint64(ctx.travelTicks);

        // New nonce
        ctx.newNonce = cs.lastMissionNonce + 1;
        cs.lastMissionNonce = ctx.newNonce;

        // Install mission via helper to keep stack shallow
        _installMission(existingM, order, cs, ctx);

        // Update clansman state
        if (ctx.travelTicks > 0) {
            cs.state = ClansmanState.TRAVELING;
        } else {
            // NOOP / same-region: no traveling state per v4.3 §A
            cs.state = ClansmanState.ACTING;
            cs.currentRegion = ctx.gotoRegion;
        }

        // Start cooldown
        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;

        // DefendBase: deregister old assignment immediately (clansman is being re-tasked).
        // Registration at the target base happens at arrival in _resolveAction (v4.3 — arrived defenders only).
        {
            uint32 oldTarget = _clanDefendingBase[cs.clansmanId];
            if (oldTarget != 0) {
                _removeDefender(oldTarget, cs.clansmanId);
                _clanDefendingBase[cs.clansmanId] = 0;
            }
        }

        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
        // executeAtTick = arrivalTick (not arrivalTick+1).
        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick);
        }

        // DefendBase zero-travel: register synchronously so getActiveDefenders() has no drop window.
        // When travelTicks == 0 the clansman is already at the target base — no travel window to wait out.
        // The _resolveAction guard (clanDefendingBase != targetClanId) will be false next tick, preventing double-register.
        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
            _incomingDefenders[order.targetClanId].push(cs.clansmanId);
            _clanDefendingBase[cs.clansmanId] = order.targetClanId;
        }

        if (ctx.wasActive) {
            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
        }

        emit MissionAssigned(
            clanId,
            order.clansmanId,
            ctx.newNonce,
            order.action,
            ctx.fromRegion,
            ctx.gotoRegion,
            _world.currentTick,
            ctx.arrivalTick
        );

        result.status = StatusCode.OK;
        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
        result.missionNonce = ctx.newNonce;
        return result;
    }

    function _installMission(
        Mission storage m,
        ClanOrder calldata order,
        Clansman storage cs,
        OrderCtx memory ctx
    ) internal {
        m.active = true;
        m.nonce = ctx.newNonce;
        m.clansmanId = cs.clansmanId;
        m.startRegion = ctx.fromRegion;
        m.targetRegion = ctx.gotoRegion;
        m.action = order.action;
        m.startTick = _world.currentTick;
        m.arrivalTick = ctx.arrivalTick;
        m.actionStartTick = ctx.arrivalTick;
        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
            ? MarketExecutionMode.Scheduled
            : MarketExecutionMode.None;
        m.targetClanId = order.targetClanId;
        m.marketToken = order.marketToken;
        m.marketAmount = order.marketAmount;
        m.maxGoldIn = order.maxGoldIn;
    }

    function _enqueueScheduledMarketAction(
        uint32 clanId,
        ClanOrder calldata order,
        uint32 clansmanId,
        uint64 executeAtTick
    ) internal {
        ScheduledMarketAction memory sma = ScheduledMarketAction({
            executeAtTick: executeAtTick,
            commitSequence: _world.nextCommitSequence++,
            clanId: clanId,
            clansmanId: clansmanId,
            action: order.action,
            marketToken: order.marketToken,
            marketAmount: order.marketAmount,
            maxGoldIn: order.maxGoldIn
        });
        _scheduledMarketActions[executeAtTick].push(sma);
        emit ScheduledMarketActionCommitted(
            executeAtTick,
            sma.commitSequence,
            clanId,
            clansmanId,
            order.action,
            order.marketToken,
            order.marketAmount,
            order.maxGoldIn
        );
    }

    function _removeDefender(uint32 targetClanId, uint32 clansmanId) internal {
        uint32[] storage defenders = _incomingDefenders[targetClanId];
        for (uint256 i = 0; i < defenders.length; i++) {
            if (defenders[i] == clansmanId) {
                defenders[i] = defenders[defenders.length - 1];
                defenders.pop();
                break;
            }
        }
    }

    // =========================================================================
    // MARKET EXECUTION (Phase 2)
    // =========================================================================

    /// @dev Execute all scheduled market actions for the given tick. Called from heartbeat.
    function _executeScheduledMarketActions(uint64 tick) internal {
        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
        uint256 len = actions.length;
        if (len == 0) return;

        for (uint256 i = 0; i < len; i++) {
            ScheduledMarketAction storage sma = actions[i];

            // Validate clansman still belongs to the clan
            Clansman storage cs = _clansmen[sma.clansmanId];
            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
                continue;
            }

            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
            // Note: _completeMission sets m.active=false during settlement (by design), so we
            // cannot use m.active as a validity signal here — check action type only.
            Mission storage m = _missions[sma.clansmanId];
            if (m.action != sma.action) {
                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
                continue;
            }

            if (sma.action == ActionType.MarketSell) {
                try this._executeMarketSellExternal(tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence) {
                    // success
                } catch {
                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
                }
            } else if (sma.action == ActionType.MarketBuy) {
                try this._executeMarketBuyExternal(tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.maxGoldIn, sma.commitSequence) {
                    // success

exec
/bin/bash -lc "sed -n '1280,1545p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
    function _validateAction(
        Clan storage clan,
        Clansman storage cs,
        ClanOrder calldata order,
        uint8 gotoRegion
    ) internal view returns (StatusCode) {
        ActionType action = order.action;

        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;

        // DepositResources: must go to homebase
        if (action == ActionType.DepositResources) {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
        }

        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument) {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
        }

        // ChopWood: must go to Forest
        if (action == ActionType.ChopWood) {
            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
        }
        // MineIron: must go to Mountains
        if (action == ActionType.MineIron) {
            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
        }
        // FishDocks: must go to WestDocks or EastDocks
        if (action == ActionType.FishDocks) {
            if (gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS) {
                return StatusCode.ERR_INVALID_REGION;
            }
        }
        // FishDeepSea: must go to DeepSea
        if (action == ActionType.FishDeepSea) {
            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
        }
        // HarvestWheat: must go to WestFarms or EastFarms
        if (action == ActionType.HarvestWheat) {
            if (gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS) {
                return StatusCode.ERR_INVALID_REGION;
            }
        }

        // DefendBase: targetClanId must be valid and gotoRegion must match target's baseRegion
        if (action == ActionType.DefendBase) {
            if (order.targetClanId == 0 || _clans[order.targetClanId].clanId == 0) {
                return StatusCode.ERR_INVALID_TARGET;
            }
            if (_clans[order.targetClanId].clanState == ClanState.DEAD) {
                return StatusCode.ERR_NOT_DEFENDABLE;
            }
            if (gotoRegion != _clans[order.targetClanId].baseRegion) {
                return StatusCode.ERR_NOT_AT_TARGET_BASE;
            }
        }

        // MarketBuy/MarketSell: must target Unicorn Town
        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
                return StatusCode.ERR_INVALID_REGION;
            }
            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
            // Validate token is a supported resource token (not gold itself)
            if (_treasury.woodToken != address(0)) {
                address tok = order.marketToken;
                if (tok == address(0) || tok == _treasury.goldToken) {
                    return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
                }
                if (tok != _treasury.woodToken &&
                    tok != _treasury.ironToken &&
                    tok != _treasury.wheatToken &&
                    tok != _treasury.fishToken) {
                    return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
                }
            }
            // Market orders are always enqueued for the arrivalTick FIFO queue.
            // _resolveAction records mission completion but does not execute any swap.
            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN &&
                cs.state == ClansmanState.WAITING) {
                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
            }
        }

        cs; // suppress unused warning
        return StatusCode.OK;
    }

    // =========================================================================
    // TREASURY / POOL SEEDING
    // =========================================================================

    /// @notice One-time treasury initialization: register token and pool addresses.
    ///         Must be called before seedPools. Callable only once.
    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external {
        require(
            !_treasury.poolsSeeded && _treasury.woodToken == address(0),
            "ClanWorld: treasury already init"
        );
        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");

        _treasury.woodToken      = tokens[0];
        _treasury.ironToken      = tokens[1];
        _treasury.wheatToken     = tokens[2];
        _treasury.fishToken      = tokens[3];
        _treasury.goldToken      = tokens[4];
        _treasury.blueprintToken = tokens[5];

        _treasury.woodGoldPool  = pools[0];
        _treasury.ironGoldPool  = pools[1];
        _treasury.wheatGoldPool = pools[2];
        _treasury.fishGoldPool  = pools[3];
    }

    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
    function seedPools(PoolSeedConfig calldata cfg) external override {
        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");

        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed,  cfg.goldSeedForWood);
        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed,  cfg.goldSeedForIron);
        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed,  cfg.goldSeedForFish);

        _treasury.poolsSeeded = true;

        emit PoolsSeeded(
            _treasury.woodGoldPool,
            _treasury.ironGoldPool,
            _treasury.wheatGoldPool,
            _treasury.fishGoldPool
        );
    }

    // =========================================================================
    // OTC TRANSFERS — Phase 2 stubs
    // =========================================================================

    function transferGold(uint32, uint32, uint256) external pure override {
        revert("ClanWorld: OTC transfers available in Phase 2");
    }

    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
        revert("ClanWorld: OTC transfers available in Phase 2");
    }

    function transferBlueprint(uint32, uint32, uint256) external pure override {
        revert("ClanWorld: OTC transfers available in Phase 2");
    }

    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
        external
        pure
        override
    {
        revert("ClanWorld: OTC transfers available in Phase 2");
    }

    // =========================================================================
    // RAW READ GETTERS
    // =========================================================================

    function getWorldState() external view override returns (WorldState memory) {
        return _world;
    }

    function getTreasuryState() external view override returns (TreasuryState memory) {
        return _treasury;
    }

    function getClan(uint32 clanId) external view override returns (Clan memory) {
        return _clans[clanId];
    }

    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
        return _clansmen[clansmanId];
    }

    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
        return _missions[clansmanId];
    }

    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
        return BanditTroop({
            banditId: 0,
            state: BanditState.NONE,
            currentRegion: 0,
            attackAttemptsMade: 0,
            stateEnteredTick: 0,
            nextActionTick: 0,
            tier: 0,
            attackPower: 0,
            carryWood: 0,
            carryIron: 0,
            carryWheat: 0,
            carryFish: 0
        });
    }

    function getWheatPlots(uint32 clanId)
        external
        view
        override
        returns (WheatPlot memory west, WheatPlot memory east)
    {
        west = _wheatPlots[clanId][0];
        east = _wheatPlots[clanId][1];
    }

    function getScheduledMarketActionsForTick(uint64 tick)
        external
        view
        override
        returns (ScheduledMarketAction[] memory)
    {
        return _scheduledMarketActions[tick];
    }

    function getActiveDefenders(uint32 targetClanId)
        external
        view
        override
        returns (uint32[] memory)
    {
        return _incomingDefenders[targetClanId];
    }

    // =========================================================================
    // DERIVED READ GETTERS (read-only, no storage mutation)
    // =========================================================================

    /// @dev Returns last-settled state; starvation check uses currentTick (live).
    ///      Carry amounts lag until next settleClan().
    function getDerivedClanState(uint32 clanId)
        external
        view
        override
        returns (DerivedClanState memory)
    {
        Clan memory clan = _clans[clanId];
        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
        uint256 lootVal = _lootValueRaw(clan);
        return DerivedClanState({
            clan: clan,
            isStarving: starving,
            lootValue: lootVal,
            derivedAtTick: _world.currentTick
        });
    }

    function getDerivedClansmanState(uint32 clansmanId)
        external
        view
        override
        returns (DerivedClansmanState memory)
    {
        Clansman memory cs = _clansmen[clansmanId];
        Mission memory m = _missions[clansmanId];
        uint8 effectiveRegion = cs.currentRegion;
        if (cs.state == ClansmanState.TRAVELING && m.active) {
            // Simplified: if past arrivalTick, they're at target; else at start
            if (_world.currentTick >= m.arrivalTick) {
                effectiveRegion = m.targetRegion;
            } else {

exec
/bin/bash -lc "sed -n '1545,1735p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
            } else {
                effectiveRegion = m.startRegion;
            }
        }
        return DerivedClansmanState({
            clansman: cs,
            activeMission: m,
            effectiveRegion: effectiveRegion,
            derivedAtTick: _world.currentTick
        });
    }

    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
        return 0; // Phase 3
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
        // Phase 1: return raw value (no simulation)
        return _lootValueRaw(_clans[clanId]);
    }

    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
    }

    // =========================================================================
    // UI INDEXER AGGREGATOR GETTERS
    // =========================================================================

    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
    ///      Carry amounts not included. Full view-only settlement deferred.
    ///      View function — no gas cost for off-chain indexer/UI reads.
    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
        for (uint256 i = 0; i < _allClanIds.length; i++) {
            uint32 cid = _allClanIds[i];
            Clan storage clan = _clans[cid];
            lb[i] = LeaderboardEntry({
                clanId: cid,
                owner: clan.owner,
                monumentLevel: clan.monumentLevel,
                baseLevel: clan.baseLevel,
                wallLevel: clan.wallLevel,
                livingClansmen: clan.livingClansmen,
                state: clan.clanState,
                lootValue: _lootValueRaw(clan)
            });
        }

        return WorldSnapshot({
            currentTick: _world.currentTick,
            seasonStartTick: _world.seasonStartTick,
            seasonEndTick: _world.seasonEndTick,
            seasonFinalized: _world.seasonFinalized,
            winterActive: _world.winterActive,
            winterStartsAtTick: _world.winterStartsAtTick,
            winterEndsAtTick: _world.winterEndsAtTick,
            activeBanditId: _world.activeBanditId,
            currentTickSeed: _world.currentTickSeed,
            leaderboard: lb
        });
    }

    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
    ///      current; carry amounts and wheat progress lag until next settleClan() call.
    ///      Full view-only settlement simulation is deferred (tracked issue).
    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
        Clan storage clan = _clans[clanId];
        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
        uint256 lootVal = _lootValueRaw(clan);

        DerivedClanState memory derivedClan = DerivedClanState({
            clan: clan,
            isStarving: starving,
            lootValue: lootVal,
            derivedAtTick: _world.currentTick
        });

        uint32[] storage csIds = _clanClansmanIds[clanId];
        ClansmanFullView[] memory clansmen = new ClansmanFullView[](csIds.length);
        for (uint256 i = 0; i < csIds.length; i++) {
            uint32 csId = csIds[i];
            Clansman memory cs = _clansmen[csId];
            Mission memory m = _missions[csId];
            uint8 effRegion = cs.currentRegion;
            if (cs.state == ClansmanState.TRAVELING && m.active) {
                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
            }
            DerivedClansmanState memory dcs = DerivedClansmanState({
                clansman: cs,
                activeMission: m,
                effectiveRegion: effRegion,
                derivedAtTick: _world.currentTick
            });
            clansmen[i] = ClansmanFullView({ clansman: dcs, activeMission: m });
        }

        // Find if any of this clan's clansmen is defending a base
        uint32 thisClanDefendingBaseId = 0;
        for (uint256 i = 0; i < csIds.length; i++) {
            uint32 target = _clanDefendingBase[csIds[i]];
            if (target != 0) {
                thisClanDefendingBaseId = target;
                break;
            }
        }

        return ClanFullView({
            clan: derivedClan,
            clansmen: clansmen,
            westPlot: _wheatPlots[clanId][0],
            eastPlot: _wheatPlots[clanId][1],
            incomingDefenderIds: _incomingDefenders[clanId],
            thisClanDefendingBaseId: thisClanDefendingBaseId
        });
    }

    function getMarketState() external view override returns (MarketState memory) {
        return MarketState({
            wood:  _poolReserves(_treasury.woodToken,  _treasury.woodGoldPool),
            wheat: _poolReserves(_treasury.wheatToken, _treasury.wheatGoldPool),
            fish:  _poolReserves(_treasury.fishToken,  _treasury.fishGoldPool),
            iron:  _poolReserves(_treasury.ironToken,  _treasury.ironGoldPool),
            currentTick: _world.currentTick,
            currentTickQueue: _scheduledMarketActions[_world.currentTick],
            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]
        });
    }

    function _poolReserves(address resourceToken, address poolAddr) internal view returns (PoolReserves memory pr) {
        pr.resourceToken = resourceToken;
        if (poolAddr == address(0) || resourceToken == address(0)) {
            return pr;
        }
        (uint256 rA, uint256 rB) = StubPool(poolAddr).getReserves();
        pr.resourceReserve = rA;
        pr.goldReserve = rB;
        pr.spotPriceGoldPerResource = rA > 0 ? (rB * 1e18) / rA : 0;
    }

    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
        return ActiveBanditView({
            exists: false,
            banditId: 0,
            state: BanditState.NONE,
            currentRegion: 0,
            attackAttemptsMade: 0,
            maxAttemptsRemaining: 0,
            stateEnteredTick: 0,
            nextActionTick: 0,
            tier: 0,
            attackPower: 0,
            carryWood: 0,
            carryIron: 0,
            carryWheat: 0,
            carryFish: 0,
            projectedTargetClanId: 0,
            projectedTargetLootValue: 0
        });
    }

    /// @dev View function — no gas cost for off-chain indexer/UI reads.
    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
    function getRegionPopulation(uint8 region)
        external
        view
        override

exec
/bin/bash -lc "sed -n '1735,2105p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        override
        returns (RegionOccupant[] memory)
    {
        // Count matching occupants first
        uint256 count = 0;
        for (uint256 i = 0; i < _allClanIds.length; i++) {
            uint32 cid = _allClanIds[i];
            uint32[] storage csIds = _clanClansmanIds[cid];
            for (uint256 j = 0; j < csIds.length; j++) {
                Clansman storage cs = _clansmen[csIds[j]];
                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
                    count++;
                }
            }
        }

        RegionOccupant[] memory occupants = new RegionOccupant[](count);
        uint256 idx = 0;
        for (uint256 i = 0; i < _allClanIds.length; i++) {
            uint32 cid = _allClanIds[i];
            uint32[] storage csIds = _clanClansmanIds[cid];
            for (uint256 j = 0; j < csIds.length; j++) {
                uint32 csId = csIds[j];
                Clansman storage cs = _clansmen[csId];
                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
                    Mission storage m = _missions[csId];
                    occupants[idx++] = RegionOccupant({
                        clansmanId: csId,
                        clanId: cid,
                        state: cs.state,
                        currentAction: m.active ? m.action : ActionType.None,
                        missionNonce: cs.lastMissionNonce
                    });
                }
            }
        }
        return occupants;
    }
}

exec
/bin/bash -lc "sed -n '240,420p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

    uint256 goldBalance;
    uint256 blueprintBalance;

    uint256 vaultWood;
    uint256 vaultIron;
    uint256 vaultWheat;
    uint256 vaultFish;
}

struct WheatPlot {
    WheatPlotState state;
    uint8 region;                  // West Farms or East Farms
    uint256 remainingWheat;
    uint64 regrowUntilTick;
}

struct Clansman {
    uint32 clansmanId;
    uint32 clanId;
    ClansmanState state;
    uint8 currentRegion;

    uint64 cooldownEndsAtTs;
    uint64 lastMissionNonce;

    uint256 carryWood;
    uint256 carryIron;
    uint256 carryWheat;
    uint256 carryFish;
}

struct Mission {
    bool active;

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

struct BanditTroop {
    uint32 banditId;
    BanditState state;

    uint8 currentRegion;
    uint8 attackAttemptsMade;
    uint64 stateEnteredTick;
    uint64 nextActionTick;

    uint8 tier;
    uint16 attackPower;            // derived from tier; tier is canonical (v4.3 §G)

    uint256 carryWood;
    uint256 carryIron;
    uint256 carryWheat;
    uint256 carryFish;
}

struct ScheduledMarketAction {
    uint64 executeAtTick;
    uint64 commitSequence;        // global monotonic FIFO order
    uint32 clanId;
    uint32 clansmanId;
    ActionType action;            // MarketBuy or MarketSell

    address marketToken;
    uint256 marketAmount;         // exact-in for sell, exact-out for buy
    uint256 maxGoldIn;            // buy only, 0 otherwise
}

struct DefenseContribution {
    uint32 clansmanId;
    uint32 clanId;
    uint16 defensePoints;
}

struct PackedRoute {
    uint8 travelTicks;
    bytes8 path;                  // ordered region ids, e.g. [6,4,3,2,0,0,0,0]
}

// =============================================================================
// DERIVED VIEW STRUCTS (read-only, settled forward to current tick)
// =============================================================================

struct DerivedClanState {
    Clan clan;                    // settled to current tick
    bool isStarving;
    uint256 lootValue;            // current weighted loot value
    uint64 derivedAtTick;
}

struct DerivedClansmanState {
    Clansman clansman;            // settled to current tick
    Mission activeMission;        // active=false if none
    uint8 effectiveRegion;        // for traveling, derived from route + elapsed ticks
    uint64 derivedAtTick;
}

// =============================================================================
// WRITE INPUT / OUTPUT STRUCTS
// =============================================================================

struct ClanOrder {
    uint32 clansmanId;
    uint8 gotoRegion;
    ActionType action;

    uint32 targetClanId;          // DefendBase only
    address marketToken;
    uint256 marketAmount;
    uint256 maxGoldIn;
}

struct OrderResult {
    uint32 clansmanId;
    StatusCode status;
    uint64 cooldownEndsAtTs;
    uint64 missionNonce;
}

struct PoolSeedConfig {
    uint256 woodSeed;
    uint256 wheatSeed;
    uint256 fishSeed;
    uint256 ironSeed;
    uint256 goldSeedForWood;
    uint256 goldSeedForWheat;
    uint256 goldSeedForFish;
    uint256 goldSeedForIron;
}

// =============================================================================
// UI INDEXER AGGREGATOR STRUCTS (v4.4 additions)
// =============================================================================

struct LeaderboardEntry {
    uint32 clanId;
    address owner;
    uint8 monumentLevel;
    uint8 baseLevel;
    uint8 wallLevel;
    uint8 livingClansmen;
    ClanState state;
    uint256 lootValue;            // settled
}

struct WorldSnapshot {
    uint64 currentTick;
    uint64 seasonStartTick;
    uint64 seasonEndTick;
    bool   seasonFinalized;
    bool   winterActive;
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


exec
/bin/bash -lc "sed -n '420,640p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

struct ClanFullView {
    DerivedClanState clan;
    ClansmanFullView[] clansmen;
    WheatPlot westPlot;
    WheatPlot eastPlot;
    uint32[] incomingDefenderIds;     // workers from other clans defending us
    uint32   thisClanDefendingBaseId; // 0 if none
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
    bool   exists;
    uint32 banditId;
    BanditState state;
    uint8  currentRegion;
    uint8  attackAttemptsMade;
    uint8  maxAttemptsRemaining;
    uint64 stateEnteredTick;
    uint64 nextActionTick;
    uint8  tier;
    uint16 attackPower;

    uint256 carryWood;
    uint256 carryIron;
    uint256 carryWheat;
    uint256 carryFish;

    uint32 projectedTargetClanId;     // 0 if no eligible target in current region
    uint256 projectedTargetLootValue;
}

struct RegionOccupant {
    uint32 clansmanId;
    uint32 clanId;
    ClansmanState state;
    ActionType currentAction;
    uint64 missionNonce;
}

// =============================================================================
// EVENTS
// =============================================================================

interface IClanWorldEvents {
    // ----- world clock -----
    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
    event WinterStarted(uint64 indexed tick);
    event WinterEnded(uint64 indexed tick);
    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);

    // ----- clan lifecycle -----
    event ClanSpawned(
        uint32 indexed clanId,
        address indexed owner,
        uint256 iftTokenId,
        uint8 baseRegion,
        uint64 atTick
    );
    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);

    // ----- missions -----
    event MissionAssigned(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint64 missionNonce,
        ActionType action,
        uint8 startRegion,
        uint8 targetRegion,
        uint64 startTick,
        uint64 arrivalTick
    );
    event MissionInterrupted(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint64 oldMissionNonce,
        uint64 newMissionNonce
    );
    event MissionCompleted(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint64 missionNonce,
        ActionType action
    );
    event WorkerArrived(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint8 region,
        uint64 tick
    );

    // ----- gathering / vault movement -----
    event ResourcesGathered(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        ActionType action,
        uint256 woodGained,
        uint256 ironGained,
        uint256 wheatGained,
        uint256 fishGained,
        uint256 goldBonus,
        uint64 atTick
    );
    event ResourcesDeposited(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish,
        uint64 atTick
    );

    // ----- building -----
    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);

    // ----- market -----
    event ImmediateMarketActionExecuted(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        address tokenIn,
        address tokenOut,
        uint256 amountIn,
        uint256 amountOut,
        uint64 atTick
    );
    event ScheduledMarketActionExecuted(
        uint64 indexed executeAtTick,
        uint64 indexed commitSequence,
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
    event MarketActionFailed(
        uint32 indexed clanId,
        uint32 indexed clansmanId,
        ActionType action,
        StatusCode reason
    );

    // ----- bandits -----
    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
    event BanditStateChanged(
        uint32 indexed banditId,
        BanditState oldState,
        BanditState newState,
        uint8 region,
        uint64 atTick
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
    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
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
        uint32 indexed fromClanId,
        uint32 indexed toClanId,
        ResourceType resource,
        uint256 amount,

exec
/bin/bash -lc "sed -n '640,780p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        uint256 amount,
        uint64 atTick
    );
    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);

    // ----- treasury / pools -----
    event PoolsSeeded(
        address woodGoldPool,
        address wheatGoldPool,
        address fishGoldPool,
        address ironGoldPool
    );
}

// =============================================================================
// MAIN INTERFACE
// =============================================================================

interface IClanWorld is IClanWorldEvents {

    // -------------------------------------------------------------------------
    // World progression
    // -------------------------------------------------------------------------

    /// @notice Permissionless heartbeat. Closes the current tick, resolves
    ///         scheduled market actions and world events, advances the tick.
    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
    function heartbeat() external;

    /// @notice Lazily settle a clan forward to current tick. Idempotent.
    function settleClan(uint32 clanId) external;

    /// @notice Finalize the current season. Permissionless after seasonEndTick.
    function finalizeSeason() external;

    // -------------------------------------------------------------------------
    // Clan lifecycle
    // -------------------------------------------------------------------------

    /// @notice Mint a new clan iNFT and spawn its homebase in a valid region.
    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);

    /// @notice Submit one or more orders for a single clan's clansmen.
    ///         Per-order failures do not revert the tx.
    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
        external
        returns (OrderResult[] memory);

    // -------------------------------------------------------------------------
    // Treasury / pool seeding
    // -------------------------------------------------------------------------

    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
    function seedPools(PoolSeedConfig calldata cfg) external;

    // -------------------------------------------------------------------------
    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
    // -------------------------------------------------------------------------

    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;

    function transferVaultResource(
        uint32 fromClanId,
        uint32 toClanId,
        ResourceType resource,
        uint256 amount
    ) external;

    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;

    function transferBundle(
        uint32 fromClanId,
        uint32 toClanId,
        uint256 gold,
        uint256 blueprint,
        uint256 wood,
        uint256 iron,
        uint256 wheat,
        uint256 fish
    ) external;

    // -------------------------------------------------------------------------
    // Raw read getters (committed storage, no settlement simulation)
    // -------------------------------------------------------------------------

    function getWorldState() external view returns (WorldState memory);

    function getTreasuryState() external view returns (TreasuryState memory);

    function getClan(uint32 clanId) external view returns (Clan memory);

    function getClansman(uint32 clansmanId) external view returns (Clansman memory);

    function getActiveMission(uint32 clansmanId) external view returns (Mission memory);

    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);

    function getWheatPlots(uint32 clanId)
        external
        view
        returns (WheatPlot memory west, WheatPlot memory east);

    function getScheduledMarketActionsForTick(uint64 tick)
        external
        view
        returns (ScheduledMarketAction[] memory);

    function getActiveDefenders(uint32 targetClanId)
        external
        view
        returns (uint32[] memory clansmanIds);

    // -------------------------------------------------------------------------
    // Derived read getters (read-only simulation forward to current tick)
    //
    // Per v4.3 §J ("Derived Getter Non-Mutation Rule"), these MUST NOT mutate
    // any storage, including settlement checkpoints, cached flags, or queues.
    // -------------------------------------------------------------------------

    function getDerivedClanState(uint32 clanId)
        external
        view
        returns (DerivedClanState memory);

    function getDerivedClansmanState(uint32 clansmanId)
        external
        view
        returns (DerivedClansmanState memory);

    /// @notice Non-binding preview. Bandit targeting is recomputed at attack
    ///         resolution time using then-current eagerly settled state.
    function getBanditTargetPreview(uint32 banditId)
        external
        view
        returns (uint32 previewClanId);

    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
        external
        view
        returns (uint8 travelTicks, bytes8 path);


codex
I found the workspace itself is still on the pre-change baseline, so I’m switching to the actual commit object for `23f7f1a` to get real line numbers and avoid reviewing against a stale local file.
exec
/bin/bash -lc 'git show --stat --oneline 23f7f1a' in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a fix(contracts): Phase 9 cloud-review fix-round — bandit state advancement + ABI doc + 5 MED (Closes #264) (#265)
 packages/contracts/src/ClanWorld.sol               | 104 +++++++++++++--------
 packages/contracts/src/IClanWorld.sol              |   6 +-
 packages/contracts/test/Bandit.t.sol               |  36 +++----
 .../contracts/test/BanditAttackResolution.t.sol    |   4 +-
 packages/contracts/test/BanditSpawn.t.sol          |  37 ++++++++
 5 files changed, 129 insertions(+), 58 deletions(-)

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1,460p'" in /home/claude/code/clan-world
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
    40	import {RNG} from "./lib/RNG.sol";
    41	import {StubPool} from "./StubPool.sol";
    42	import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
    43	
    44	/// @title ClanWorld
    45	/// @notice Phase 1+2 real engine implementation of IClanWorld v4.
    46	///         Implements: world clock, clan lifecycle, lazy settlement, resource gathering,
    47	///         deposit, wheat harvest, travel, NOOP bypass, order validation, and market execution.
    48	///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
    49	contract ClanWorld is IClanWorld, ReentrancyGuard {
    50	    // =========================================================================
    51	    // STORAGE
    52	    // =========================================================================
    53	
    54	    WorldState private _world;
    55	    TreasuryState private _treasury;
    56	
    57	    mapping(uint32 => Clan) internal _clans;
    58	    mapping(uint32 => Clansman) internal _clansmen;
    59	    mapping(uint32 => Mission) private _missions; // keyed by clansmanId
    60	    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
    61	    mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
    62	    mapping(uint8 => uint32[]) private _defendingClansByRegion; // home region => unique defending clanIds
    63	    mapping(uint8 => mapping(uint32 => uint256)) private _defenderCountByRegionClan; // region => clanId => clansmen count
    64	    mapping(uint32 => uint8) private _clansmanDefendingRegion; // clansmanId => defended home region
    65	    mapping(uint32 => BanditTroop) internal _bandits;
    66	    mapping(uint8 => uint32[]) internal _banditsByRegion; // region => bandit IDs
    67	    mapping(uint8 => BanditSpawnState) internal _banditSpawnByRegion;
    68	    mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
    69	
    70	    uint32 private _nextClanId;
    71	    uint32 private _nextClansmanId;
    72	    uint32 internal _nextBanditId;
    73	    uint32 internal _activeBanditCount;
    74	    uint32[] private _allClanIds;
    75	
    76	    // per-clan clansman list: clanId => clansmanId[]
    77	    mapping(uint32 => uint32[]) private _clanClansmanIds;
    78	
    79	    // =========================================================================
    80	    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    81	    // =========================================================================
    82	
    83	    uint256 private constant WHEAT_HARVEST_RATE = 20e18;
    84	    uint256 private constant RESOURCE_UNIT = 1e18;
    85	    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
    86	    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
    87	    uint256 internal constant DOMAIN_BANDIT_SPAWN = uint256(keccak256("clanworld.bandit.spawn.v1"));
    88	    uint64 internal constant MIN_SPAWN_COOLDOWN_TICKS = ClanWorldConstants.BANDIT_COOLDOWN_TICKS;
    89	    uint16 internal constant BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS = 1000;
    90	    uint16 internal constant BANDIT_SPAWN_MAX_PROBABILITY_BPS = 8000;
    91	    uint8 internal constant MAX_BANDITS_PER_REGION = 3;
    92	    uint8 internal constant MAX_TOTAL_BANDITS = 8;
    93	    uint8 internal constant MAX_CLANS = 12;
    94	    /// @dev Bandit spawn weights are a heartbeat-time heuristic. V1 has
    95	    ///      MAX_CLANS = 12, so scanning 8 clans per tick covers the live cap in
    96	    ///      at most two rotating heartbeats while keeping heartbeat gas bounded.
    97	    uint256 internal constant MAX_BANDIT_SPAWN_SCAN_PER_REGION = 8;
    98	    uint256 internal constant MAX_BANDIT_SPAWN_CLANSMEN_SCAN_PER_REGION = MAX_BANDIT_SPAWN_SCAN_PER_REGION * 4;
    99	    /// @dev Eager settlement scans the clan-indexed bases in each spawn-candidate
   100	    ///      region, not every clan globally per region forever. MAX_CLANS = 12,
   101	    ///      so this settles all possible bases today while keeping the heartbeat
   102	    ///      loop explicitly bounded if that cap grows.
   103	    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION = 12;
   104	    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_DEFENDING_CLANS_PER_REGION = 12;
   105	    uint256 internal constant MAX_BANDIT_EAGER_SETTLE_DEFENDER_SCAN_PER_REGION = 48;
   106	    uint32 internal constant MIN_BANDIT_SPAWN_STRENGTH = 100;
   107	    uint32 internal constant BANDIT_SPAWN_STRENGTH_SPREAD = 151;
   108	    uint32 internal constant CLANSMAN_MAX_DEFENSE_DAMAGE = 100;
   109	    uint32 internal constant WALL_HP_PER_LEVEL = 100;
   110	    uint32 internal constant BASE_HP_PER_LEVEL = 25;
   111	    uint32 internal constant CLANSMAN_HP = 100;
   112	
   113	    struct BanditSpawnState {
   114	        uint64 lastSpawnTick;
   115	        uint16 probabilityAccum;
   116	    }
   117	
   118	    struct SettlementSimulation {
   119	        Clan clan;
   120	        Clansman[] clansmen;
   121	        Mission[] missions;
   122	        WheatPlot[2] wheatPlots;
   123	    }
   124	
   125	    // =========================================================================
   126	    // CONSTRUCTOR
   127	    // =========================================================================
   128	
   129	    constructor() {
   130	        _world.currentTick = 0;
   131	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
   132	        _world.seasonStartTick = 0;
   133	        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
   134	        _world.currentSeasonNumber = 1;
   135	        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
   136	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
   137	        // i.e. ticks [100, 110)
   138	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
   139	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
   140	        _world.winterActive = false;
   141	        _treasury.treasuryOwner = msg.sender;
   142	        _nextClanId = 1;
   143	        _nextClansmanId = 1;
   144	        _nextBanditId = 1;
   145	    }
   146	
   147	    // =========================================================================
   148	    // TRAVEL DISTANCE MATRIX
   149	    // =========================================================================
   150	
   151	    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
   152	    // Adjacency:
   153	    //   Forest(1):       Mountains(2), WestFarms(4)
   154	    //   Mountains(2):    Forest(1), UnicornTown(3)
   155	    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
   156	    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
   157	    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
   158	    //   WestDocks(6):    WestFarms(4), DeepSea(8)
   159	    //   EastDocks(7):    EastFarms(5), DeepSea(8)
   160	    //   DeepSea(8):      WestDocks(6), EastDocks(7)
   161	    //
   162	    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
   163	    // Distance of 0 = same region.
   164	    //
   165	    // Full BFS-computed 8x8 matrix:
   166	    //   src\dst  1  2  3  4  5  6  7  8
   167	    //      1     0  1  2  1  3  2  4  3
   168	    //      2     1  0  1  2  2  3  3  4
   169	    //      3     2  1  0  1  1  2  2  3
   170	    //      4     1  2  1  0  2  1  3  2
   171	    //      5     3  2  1  2  0  3  1  2
   172	    //      6     2  3  2  1  3  0  2  1
   173	    //      7     4  3  2  3  1  2  0  1
   174	    //      8     3  4  3  2  2  1  1  0
   175	
   176	    function _distMatrix(uint8 src, uint8 dst) private pure returns (uint8) {
   177	        if (src == dst) return 0;
   178	        // Encode as (src-1)*8 + (dst-1), values from BFS
   179	        uint8[64] memory d = [
   180	            // src=1: to 1,2,3,4,5,6,7,8
   181	            0,
   182	            1,
   183	            2,
   184	            1,
   185	            3,
   186	            2,
   187	            4,
   188	            3,
   189	            // src=2: to 1,2,3,4,5,6,7,8
   190	            1,
   191	            0,
   192	            1,
   193	            2,
   194	            2,
   195	            3,
   196	            3,
   197	            4,
   198	            // src=3: to 1,2,3,4,5,6,7,8
   199	            2,
   200	            1,
   201	            0,
   202	            1,
   203	            1,
   204	            2,
   205	            2,
   206	            3,
   207	            // src=4: to 1,2,3,4,5,6,7,8
   208	            1,
   209	            2,
   210	            1,
   211	            0,
   212	            2,
   213	            1,
   214	            3,
   215	            2,
   216	            // src=5: to 1,2,3,4,5,6,7,8
   217	            3,
   218	            2,
   219	            1,
   220	            2,
   221	            0,
   222	            3,
   223	            1,
   224	            2,
   225	            // src=6: to 1,2,3,4,5,6,7,8
   226	            2,
   227	            3,
   228	            2,
   229	            1,
   230	            3,
   231	            0,
   232	            2,
   233	            1,
   234	            // src=7: to 1,2,3,4,5,6,7,8
   235	            4,
   236	            3,
   237	            2,
   238	            3,
   239	            1,
   240	            2,
   241	            0,
   242	            1,
   243	            // src=8: to 1,2,3,4,5,6,7,8
   244	            3,
   245	            4,
   246	            3,
   247	            2,
   248	            2,
   249	            1,
   250	            1,
   251	            0
   252	        ];
   253	        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
   254	    }
   255	
   256	    // Build a canonical path from src to dst (BFS on the adjacency graph).
   257	    // Returns packed bytes8: region IDs in order, zero-padded.
   258	    function _buildPath(uint8 src, uint8 dst) private pure returns (bytes8) {
   259	        if (src == dst) {
   260	            return bytes8(uint64(src) << 56);
   261	        }
   262	        // Adjacency list (1-indexed, 0 = end sentinel)
   263	        // adj[r] = neighbors of region r (up to 3)
   264	        uint8[3][9] memory adj = [
   265	            [0, 0, 0], // 0: unused
   266	            [2, 4, 0], // 1: Forest
   267	            [1, 3, 0], // 2: Mountains
   268	            [2, 4, 5], // 3: UnicornTown
   269	            [1, 3, 6], // 4: WestFarms
   270	            [3, 7, 0], // 5: EastFarms
   271	            [4, 8, 0], // 6: WestDocks
   272	            [5, 8, 0], // 7: EastDocks
   273	            [6, 7, 0] // 8: DeepSea
   274	        ];
   275	
   276	        // BFS with parent tracking (max 8 nodes)
   277	        uint8[9] memory parent;
   278	        bool[9] memory visited;
   279	        uint8[9] memory queue;
   280	        uint256 head;
   281	        uint256 tail;
   282	
   283	        for (uint256 i = 0; i < 9; i++) {
   284	            parent[i] = 0;
   285	            visited[i] = false;
   286	        }
   287	
   288	        visited[src] = true;
   289	        queue[tail++] = src;
   290	
   291	        while (head < tail) {
   292	            uint8 curr = queue[head++];
   293	            if (curr == dst) break;
   294	            for (uint256 ni = 0; ni < 3; ni++) {
   295	                uint8 nb = adj[curr][ni];
   296	                if (nb == 0) break;
   297	                if (!visited[nb]) {
   298	                    visited[nb] = true;
   299	                    parent[nb] = curr;
   300	                    queue[tail++] = nb;
   301	                }
   302	            }
   303	        }
   304	
   305	        // Reconstruct path backwards
   306	        uint8[8] memory path;
   307	        uint256 pathLen;
   308	        uint8 cur = dst;
   309	        while (cur != src) {
   310	            path[pathLen++] = cur;
   311	            cur = parent[cur];
   312	        }
   313	        path[pathLen++] = src;
   314	
   315	        // Reverse into result
   316	        bytes8 packed;
   317	        uint64 byteShift = 56;
   318	        for (uint256 i = pathLen; i > 0; i--) {
   319	            packed = packed | bytes8(uint64(path[i - 1]) << byteShift);
   320	            if (byteShift >= 8) byteShift -= 8;
   321	        }
   322	        return packed;
   323	    }
   324	
   325	    function _travelTicks(uint8 fromRegion, uint8 toRegion) private pure returns (uint8) {
   326	        if (fromRegion == 0 || toRegion == 0) return 0; // NOOP region
   327	        if (fromRegion == toRegion) return 0;
   328	        if (fromRegion > 8 || toRegion > 8) return 0;
   329	        return _distMatrix(fromRegion, toRegion);
   330	    }
   331	
   332	    function _addTicksClamped(uint64 tick, uint64 delta) private pure returns (uint64) {
   333	        if (type(uint64).max - tick < delta) return type(uint64).max;
   334	        return tick + delta;
   335	    }
   336	
   337	    // =========================================================================
   338	    // INTERNAL SETTLEMENT
   339	    // =========================================================================
   340	
   341	    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
   342	    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
   343	    function _settleMissionForClansman(
   344	        Clan storage clan,
   345	        Clansman storage cs,
   346	        uint32 clanId,
   347	        uint64 fromTick,
   348	        uint64 toTick
   349	    ) internal {
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '460,1040p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   621	        } else if (action == ActionType.HarvestWheat) {
   622	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   623	        } else if (action == ActionType.DepositResources) {
   624	            _doDeposit(clan, cs, m, clanId, tick);
   625	        } else if (action == ActionType.Wait) {
   626	            // NOOP — worker stays ACTING (continuous), no transition needed
   627	            // Wait mission is effectively persistent until interrupted
   628	        } else if (action == ActionType.DefendBase) {
   629	            // Persistent mission. Registration happens atomically at order submission.
   630	        } else if (
   631	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   632	        ) {
   633	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   634	            _doBuilding(clan, cs, m, clanId, tick, action);
   635	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   636	            // Scheduled market actions: already enqueued at submitClanOrders time.
   637	            // Settlement resolves this action slot — just complete the mission.
   638	            // (Actual execution happened or will happen at heartbeat.)
   639	            _completeMission(cs, m);
   640	        }
   641	    }
   642	
   643	    // -------------------------------------------------------------------------
   644	    // Gathering helpers
   645	    // -------------------------------------------------------------------------
   646	
   647	    function _gatherWood(
   648	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   649	        Clansman storage cs,
   650	        Mission storage m,
   651	        uint32 clanId,
   652	        uint64 tick,
   653	        bool starving,
   654	        bytes32 tickSeed
   655	    ) internal {
   656	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
   657	        if (remaining == 0) {
   658	            _completeMission(cs, m);
   659	            return;
   660	        }
   661	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
   662	        // Crit roll: domain-separated RNG
   663	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   664	        uint256 critRoll = uint256(critRng) % 10000;
   665	        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
   666	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
   667	        }
   668	        if (starving) yield = yield / 2;
   669	        if (yield > remaining) yield = remaining;
   670	        cs.carryWood += yield;
   671	
   672	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   673	
   674	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
   675	            _completeMission(cs, m);
   676	        }
   677	        // else continuous — worker stays ACTING
   678	    }
   679	
   680	    function _gatherIron(
   681	        Clan storage clan,
   682	        Clansman storage cs,
   683	        Mission storage m,
   684	        uint32 clanId,
   685	        uint64 tick,
   686	        bool starving,
   687	        bytes32 tickSeed
   688	    ) internal {
   689	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   690	        if (remaining == 0) {
   691	            _completeMission(cs, m);
   692	            return;
   693	        }
   694	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   695	        if (starving) yield = yield / 2;
   696	        if (yield > remaining) yield = remaining;
   697	        cs.carryIron += yield;
   698	
   699	        // Gold bonus roll — scoped to reduce stack depth
   700	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   701	
   702	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   703	
   704	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   705	            _completeMission(cs, m);
   706	        }
   707	    }
   708	
   709	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   710	        internal
   711	        returns (uint256 goldBonus)
   712	    {
   713	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   714	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   715	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   716	            clan.goldBalance += goldBonus;
   717	        }
   718	    }
   719	
   720	    function _gatherFishDocks(
   721	        Clan storage,
   722	        Clansman storage cs,
   723	        Mission storage m,
   724	        uint32 clanId,
   725	        uint64 tick,
   726	        bool starving,
   727	        bytes32 tickSeed
   728	    ) internal {
   729	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   730	        if (remaining == 0) {
   731	            _completeMission(cs, m);
   732	            return;
   733	        }
   734	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   735	        uint256 fishRoll = uint256(fishRng) % 10000;
   736	        uint256 yield = 0;
   737	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   738	            yield = 1e18;
   739	        }
   740	        if (starving) yield = yield / 2;
   741	        if (yield > remaining) yield = remaining;
   742	        if (yield > 0) {
   743	            cs.carryFish += yield;
   744	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   745	        }
   746	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   747	            _completeMission(cs, m);
   748	        }
   749	    }
   750	
   751	    function _gatherFishDeepSea(
   752	        Clan storage,
   753	        Clansman storage cs,
   754	        Mission storage m,
   755	        uint32 clanId,
   756	        uint64 tick,
   757	        bool starving,
   758	        bytes32 tickSeed
   759	    ) internal {
   760	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   761	        if (remaining == 0) {
   762	            _completeMission(cs, m);
   763	            return;
   764	        }
   765	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   766	        uint256 fishRoll = uint256(fishRng) % 10000;
   767	        uint256 yield = 0;
   768	        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
   769	            yield = 1e18;
   770	        }
   771	        if (starving) yield = yield / 2;
   772	        if (yield > remaining) yield = remaining;
   773	        if (yield > 0) {
   774	            cs.carryFish += yield;
   775	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
   776	        }
   777	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   778	            _completeMission(cs, m);
   779	        }
   780	    }
   781	
   782	    function _gatherWheat(
   783	        Clan storage,
   784	        /* clan — unused but kept positional for callsite parity */
   785	        Clansman storage cs,
   786	        Mission storage m,
   787	        uint32 clanId,
   788	        uint64 tick,
   789	        bool starving
   790	    ) internal {
   791	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
   792	        if (remaining == 0) {
   793	            _completeMission(cs, m);
   794	            return;
   795	        }
   796	        // Determine plot index from region
   797	        uint8 region = m.targetRegion;
   798	        uint256 plotIdx;
   799	        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
   800	            plotIdx = 0;
   801	        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
   802	            plotIdx = 1;
   803	        } else {
   804	            // Wrong region — complete (no harvest)
   805	            _completeMission(cs, m);
   806	            return;
   807	        }
   808	
   809	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
   810	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
   811	            // Plot not ready — worker waits
   812	            _completeMission(cs, m);
   813	            return;
   814	        }
   815	
   816	        uint256 yield = WHEAT_HARVEST_RATE;
   817	        if (starving) yield = yield / 2;
   818	        if (yield > remaining) yield = remaining;
   819	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
   820	
   821	        cs.carryWheat += yield;
   822	        plot.remainingWheat -= yield;
   823	
   824	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
   825	
   826	        if (plot.remainingWheat == 0) {
   827	            plot.state = WheatPlotState.Regrowing;
   828	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
   829	        }
   830	
   831	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
   832	            _completeMission(cs, m);
   833	        }
   834	        // else continuous
   835	    }
   836	
   837	    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
   838	        internal
   839	    {
   840	        // Must be at homebase region
   841	        if (cs.currentRegion != clan.baseRegion) {
   842	            _completeMission(cs, m);
   843	            return;
   844	        }
   845	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
   846	        if (!hasAnything) {
   847	            _completeMission(cs, m);
   848	            return;
   849	        }
   850	
   851	        uint256 w = cs.carryWood;
   852	        uint256 ir = cs.carryIron;
   853	        uint256 wh = cs.carryWheat;
   854	        uint256 fi = cs.carryFish;
   855	
   856	        clan.vaultWood += w;
   857	        clan.vaultIron += ir;
   858	        clan.vaultWheat += wh;
   859	        clan.vaultFish += fi;
   860	
   861	        cs.carryWood = 0;
   862	        cs.carryIron = 0;
   863	        cs.carryWheat = 0;
   864	        cs.carryFish = 0;
   865	
   866	        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
   867	        _completeMission(cs, m);
   868	    }
   869	
   870	    function _doBuilding(
   871	        Clan storage clan,
   872	        Clansman storage cs,
   873	        Mission storage m,
   874	        uint32 clanId,
   875	        uint64 tick,
   876	        ActionType action
   877	    ) internal {
   878	        // Must be at homebase
   879	        if (cs.currentRegion != clan.baseRegion) {
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1040,1700p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
  1281	
  1282	    function _simulateGatherFish(
  1283	        Clansman memory cs,
  1284	        Mission memory m,
  1285	        uint64 tick,
  1286	        bool starving,
  1287	        bytes32 tickSeed,
  1288	        uint256 successBps
  1289	    ) internal view returns (Clansman memory, Mission memory) {
  1290	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
  1291	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1292	
  1293	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
  1294	        uint256 yield = uint256(fishRng) % 10000 < successBps ? RESOURCE_UNIT : 0;
  1295	        if (starving) yield = yield / 2;
  1296	        if (yield > remaining) yield = remaining;
  1297	        if (yield > 0) {
  1298	            cs.carryFish += yield;
  1299	        }
  1300	
  1301	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
  1302	            return _simulateCompleteMission(cs, m);
  1303	        }
  1304	        return (cs, m);
  1305	    }
  1306	
  1307	    function _simulateGatherWheat(
  1308	        SettlementSimulation memory sim,
  1309	        Clansman memory cs,
  1310	        Mission memory m,
  1311	        uint64 tick,
  1312	        bool starving
  1313	    ) internal view returns (Clansman memory, Mission memory) {
  1314	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
  1315	        if (remaining == 0) return _simulateCompleteMission(cs, m);
  1316	
  1317	        uint256 plotIdx;
  1318	        if (m.targetRegion == ClanWorldConstants.REGION_WEST_FARMS) {
  1319	            plotIdx = 0;
  1320	        } else if (m.targetRegion == ClanWorldConstants.REGION_EAST_FARMS) {
  1321	            plotIdx = 1;
  1322	        } else {
  1323	            return _simulateCompleteMission(cs, m);
  1324	        }
  1325	
  1326	        WheatPlot memory plot = sim.wheatPlots[plotIdx];
  1327	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
  1328	            return _simulateCompleteMission(cs, m);
  1329	        }
  1330	
  1331	        uint256 yield = WHEAT_HARVEST_RATE;
  1332	        if (starving) yield = yield / 2;
  1333	        if (yield > remaining) yield = remaining;
  1334	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
  1335	
  1336	        cs.carryWheat += yield;
  1337	        plot.remainingWheat -= yield;
  1338	
  1339	        if (plot.remainingWheat == 0) {
  1340	            plot.state = WheatPlotState.Regrowing;
  1341	            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
  1342	        }
  1343	        sim.wheatPlots[plotIdx] = plot;
  1344	
  1345	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
  1346	            return _simulateCompleteMission(cs, m);
  1347	        }
  1348	        return (cs, m);
  1349	    }
  1350	
  1351	    function _simulateDoDeposit(SettlementSimulation memory sim, Clansman memory cs, Mission memory m)
  1352	        internal
  1353	        view
  1354	        returns (Clansman memory, Mission memory)
  1355	    {
  1356	        if (cs.currentRegion != sim.clan.baseRegion) return _simulateCompleteMission(cs, m);
  1357	
  1358	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
  1359	        if (!hasAnything) return _simulateCompleteMission(cs, m);
  1360	
  1361	        sim.clan.vaultWood += cs.carryWood;
  1362	        sim.clan.vaultIron += cs.carryIron;
  1363	        sim.clan.vaultWheat += cs.carryWheat;
  1364	        sim.clan.vaultFish += cs.carryFish;
  1365	
  1366	        cs.carryWood = 0;
  1367	        cs.carryIron = 0;
  1368	        cs.carryWheat = 0;
  1369	        cs.carryFish = 0;
  1370	
  1371	        return _simulateCompleteMission(cs, m);
  1372	    }
  1373	
  1374	    function _simulateDoBuilding(
  1375	        SettlementSimulation memory sim,
  1376	        Clansman memory cs,
  1377	        Mission memory m,
  1378	        ActionType action
  1379	    ) internal view returns (Clansman memory, Mission memory) {
  1380	        if (cs.currentRegion == sim.clan.baseRegion) {
  1381	            if (action == ActionType.BuildWall) {
  1382	                _simulateTryBuildWall(sim);
  1383	            } else if (action == ActionType.UpgradeBase) {
  1384	                _simulateTryUpgradeBase(sim);
  1385	            } else if (action == ActionType.UpgradeMonument) {
  1386	                _simulateTryUpgradeMonument(sim);
  1387	            }
  1388	        }
  1389	        return _simulateCompleteMission(cs, m);
  1390	    }
  1391	
  1392	    function _simulateTryBuildWall(SettlementSimulation memory sim) internal pure {
  1393	        uint8 nextLevel = sim.clan.wallLevel + 1;
  1394	        if (nextLevel > 5) return;
  1395	
  1396	        uint256 woodCost;
  1397	        uint256 ironCost;
  1398	        if (nextLevel == 1) {
  1399	            woodCost = 20e18;
  1400	        } else if (nextLevel == 2) {
  1401	            woodCost = 35e18;
  1402	        } else if (nextLevel == 3) {
  1403	            woodCost = 30e18;
  1404	            ironCost = 5e18;
  1405	        } else if (nextLevel == 4) {
  1406	            woodCost = 40e18;
  1407	            ironCost = 10e18;
  1408	        } else {
  1409	            woodCost = 50e18;
  1410	            ironCost = 15e18;
  1411	        }
  1412	
  1413	        if (sim.clan.vaultWood < woodCost || sim.clan.vaultIron < ironCost) return;
  1414	        sim.clan.vaultWood -= woodCost;
  1415	        sim.clan.vaultIron -= ironCost;
  1416	        sim.clan.wallLevel = nextLevel;
  1417	    }
  1418	
  1419	    function _simulateTryUpgradeBase(SettlementSimulation memory sim) internal pure {
  1420	        uint8 nextLevel = sim.clan.baseLevel + 1;
  1421	        if (nextLevel > 5) return;
  1422	
  1423	        uint256 woodCost;
  1424	        uint256 ironCost;
  1425	        uint256 wheatCost;
  1426	        if (nextLevel == 2) {
  1427	            woodCost = 40e18;
  1428	            wheatCost = 20e18;
  1429	        } else if (nextLevel == 3) {
  1430	            woodCost = 60e18;
  1431	            ironCost = 5e18;
  1432	            wheatCost = 30e18;
  1433	        } else if (nextLevel == 4) {
  1434	            woodCost = 80e18;
  1435	            ironCost = 10e18;
  1436	            wheatCost = 40e18;
  1437	        } else {
  1438	            woodCost = 100e18;
  1439	            ironCost = 15e18;
  1440	            wheatCost = 50e18;
  1441	        }
  1442	
  1443	        if (sim.clan.vaultWood < woodCost || sim.clan.vaultIron < ironCost || sim.clan.vaultWheat < wheatCost) {
  1444	            return;
  1445	        }
  1446	        sim.clan.vaultWood -= woodCost;
  1447	        sim.clan.vaultIron -= ironCost;
  1448	        sim.clan.vaultWheat -= wheatCost;
  1449	        sim.clan.baseLevel = nextLevel;
  1450	    }
  1451	
  1452	    function _simulateTryUpgradeMonument(SettlementSimulation memory sim) internal pure {
  1453	        uint8 nextLevel = sim.clan.monumentLevel + 1;
  1454	        if (nextLevel > 10) return;
  1455	
  1456	        uint256 woodCost;
  1457	        uint256 wheatCost;
  1458	        uint256 ironCost;
  1459	        uint256 blueprintCost;
  1460	        if (nextLevel == 1) {
  1461	            woodCost = 30e18;
  1462	            wheatCost = 20e18;
  1463	        } else if (nextLevel == 2) {
  1464	            woodCost = 50e18;
  1465	            wheatCost = 30e18;
  1466	        } else if (nextLevel == 3) {
  1467	            woodCost = 70e18;
  1468	            wheatCost = 40e18;
  1469	            ironCost = 5e18;
  1470	        } else if (nextLevel == 4) {
  1471	            woodCost = 90e18;
  1472	            wheatCost = 50e18;
  1473	            ironCost = 10e18;
  1474	        } else if (nextLevel == 5) {
  1475	            woodCost = 120e18;
  1476	            wheatCost = 60e18;
  1477	            ironCost = 15e18;
  1478	        } else if (nextLevel == 6) {
  1479	            woodCost = 150e18;
  1480	            wheatCost = 80e18;
  1481	            ironCost = 20e18;
  1482	        } else {
  1483	            woodCost = 200e18;
  1484	            wheatCost = 100e18;
  1485	            ironCost = 25e18;
  1486	            blueprintCost = 1e18;
  1487	        }
  1488	
  1489	        if (
  1490	            sim.clan.vaultWood < woodCost || sim.clan.vaultWheat < wheatCost || sim.clan.vaultIron < ironCost
  1491	                || sim.clan.blueprintBalance < blueprintCost
  1492	        ) return;
  1493	
  1494	        sim.clan.vaultWood -= woodCost;
  1495	        sim.clan.vaultWheat -= wheatCost;
  1496	        sim.clan.vaultIron -= ironCost;
  1497	        sim.clan.blueprintBalance -= blueprintCost;
  1498	        sim.clan.monumentLevel = nextLevel;
  1499	    }
  1500	
  1501	    function _simulateCompleteMission(Clansman memory cs, Mission memory m)
  1502	        internal
  1503	        view
  1504	        returns (Clansman memory, Mission memory)
  1505	    {
  1506	        cs.state = ClansmanState.WAITING;
  1507	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1508	        m.active = false;
  1509	        return (cs, m);
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1700,2360p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2360,3160p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2360	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  2361	        require(to != address(0), "ClanWorld: zero address");
  2362	        require(_allClanIds.length < MAX_CLANS, "ClanWorld: max clans");
  2363	        clanId = _nextClanId++;
  2364	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  2365	
  2366	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  2367	        uint8[6] memory spawnRegions = [
  2368	            ClanWorldConstants.REGION_FOREST,
  2369	            ClanWorldConstants.REGION_MOUNTAINS,
  2370	            ClanWorldConstants.REGION_WEST_FARMS,
  2371	            ClanWorldConstants.REGION_EAST_FARMS,
  2372	            ClanWorldConstants.REGION_WEST_DOCKS,
  2373	            ClanWorldConstants.REGION_EAST_DOCKS
  2374	        ];
  2375	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  2376	
  2377	        // Create clan
  2378	        Clan storage clan = _clans[clanId];
  2379	        clan.clanId = clanId;
  2380	        clan.iftTokenId = iftTokenId;
  2381	        clan.owner = to;
  2382	        clan.clanState = ClanState.ACTIVE;
  2383	        clan.baseRegion = baseRegion;
  2384	        clan.baseLevel = 1;
  2385	        clan.wallLevel = 0;
  2386	        clan.monumentLevel = 0;
  2387	        clan.livingClansmen = 4;
  2388	        clan.lastSettledTick = _world.currentTick;
  2389	        clan.starvationStartsAtTick = 0;
  2390	        clan.coldDamage = 0;
  2391	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  2392	        clan.blueprintBalance = 0;
  2393	        // Starting vault per v4 spec §12.4
  2394	        clan.vaultWood = 20e18;
  2395	        clan.vaultIron = 0;
  2396	        clan.vaultWheat = 20e18;
  2397	        clan.vaultFish = 2e18;
  2398	
  2399	        // Wheat plots
  2400	        _wheatPlots[clanId][0] = WheatPlot({
  2401	            state: WheatPlotState.Harvestable,
  2402	            region: ClanWorldConstants.REGION_WEST_FARMS,
  2403	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  2404	            regrowUntilTick: 0
  2405	        });
  2406	        _wheatPlots[clanId][1] = WheatPlot({
  2407	            state: WheatPlotState.Harvestable,
  2408	            region: ClanWorldConstants.REGION_EAST_FARMS,
  2409	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  2410	            regrowUntilTick: 0
  2411	        });
  2412	
  2413	        // Create 4 clansmen
  2414	        for (uint256 i = 0; i < 4; i++) {
  2415	            uint32 csId = _nextClansmanId++;
  2416	            Clansman storage cs = _clansmen[csId];
  2417	            cs.clansmanId = csId;
  2418	            cs.clanId = clanId;
  2419	            cs.state = ClansmanState.WAITING;
  2420	            cs.currentRegion = baseRegion;
  2421	            cs.cooldownEndsAtTs = 0;
  2422	            cs.lastMissionNonce = 0;
  2423	            cs.carryWood = 0;
  2424	            cs.carryIron = 0;
  2425	            cs.carryWheat = 0;
  2426	            cs.carryFish = 0;
  2427	            _clanClansmanIds[clanId].push(csId);
  2428	        }
  2429	
  2430	        _allClanIds.push(clanId);
  2431	
  2432	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  2433	        return (clanId, iftTokenId);
  2434	    }
  2435	
  2436	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  2437	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  2438	        external
  2439	        override
  2440	        nonReentrant
  2441	        returns (OrderResult[] memory results)
  2442	    {
  2443	        Clan storage clan = _clans[clanId];
  2444	        require(clan.clanId != 0, "ClanWorld: clan not found");
  2445	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  2446	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  2447	
  2448	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  2449	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  2450	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  2451	        {
  2452	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  2453	            if (_world.currentTick > lastSettled + 200) {
  2454	                results = new OrderResult[](orders.length);
  2455	                for (uint256 i = 0; i < orders.length; i++) {
  2456	                    results[i] = OrderResult({
  2457	                        clansmanId: orders[i].clansmanId,
  2458	                        status: StatusCode.ERR_INVALID_ACTION,
  2459	                        cooldownEndsAtTs: 0,
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
  2651	        uint32 clanId,
  2652	        ClanOrder calldata order,
  2653	        uint32 clansmanId,
  2654	        uint64 executeAtTick,
  2655	        uint64 missionNonce
  2656	    ) internal {
  2657	        ScheduledMarketAction memory sma = ScheduledMarketAction({
  2658	            executeAtTick: executeAtTick,
  2659	            commitSequence: _world.nextCommitSequence++,
  2660	            missionNonce: missionNonce,
  2661	            clanId: clanId,
  2662	            clansmanId: clansmanId,
  2663	            action: order.action,
  2664	            marketToken: order.marketToken,
  2665	            marketAmount: order.marketAmount,
  2666	            maxGoldIn: order.maxGoldIn
  2667	        });
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
  2721	    function _executeScheduledMarketActions(uint64 tick) internal {
  2722	        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
  2723	        uint256 len = actions.length;
  2724	        if (len == 0) return;
  2725	
  2726	        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;
  2727	
  2728	        for (uint256 i = 0; i < processCount; i++) {
  2729	            ScheduledMarketAction storage sma = actions[i];
  2730	
  2731	            // Validate clansman still belongs to the clan
  2732	            Clansman storage cs = _clansmen[sma.clansmanId];
  2733	            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
  2734	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
  2735	                continue;
  2736	            }
  2737	
  2738	            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
  2739	            // Note: _completeMission sets m.active=false during settlement (by design), so we
  2740	            // cannot use m.active as a validity signal here — check action type and nonce.
  2741	            Mission storage m = _missions[sma.clansmanId];
  2742	            if (m.action != sma.action || m.nonce != sma.missionNonce) {
  2743	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  2744	                continue;
  2745	            }
  2746	
  2747	            if (sma.action == ActionType.MarketSell) {
  2748	                try this._executeMarketSellExternal(
  2749	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
  2750	                ) {
  2751	                // success
  2752	                }
  2753	                catch {
  2754	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  2755	                }
  2756	            } else if (sma.action == ActionType.MarketBuy) {
  2757	                try this._executeMarketBuyExternal(
  2758	                    tick,
  2759	                    sma.clanId,
  2760	                    sma.clansmanId,
  2761	                    sma.marketToken,
  2762	                    sma.marketAmount,
  2763	                    sma.maxGoldIn,
  2764	                    sma.commitSequence
  2765	                ) {
  2766	                // success
  2767	                }
  2768	                catch {
  2769	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  2770	                }
  2771	            }
  2772	        }
  2773	
  2774	        if (len > processCount) {
  2775	            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
  2776	            for (uint256 i = processCount; i < len; i++) {
  2777	                nextActions.push(actions[i]);
  2778	            }
  2779	            // Invariant: each tick queue executes in global commitSequence order, including
  2780	            // older overflow actions merged into a tick that already has native actions.
  2781	            _sortScheduledMarketActionsByCommitSequence(nextActions);
  2782	        }
  2783	
  2784	        delete _scheduledMarketActions[tick];
  2785	    }
  2786	
  2787	    function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
  2788	        for (uint256 i = 1; i < actions.length; i++) {
  2789	            ScheduledMarketAction memory key = actions[i];
  2790	            uint256 j = i;
  2791	            while (j > 0 && actions[j - 1].commitSequence > key.commitSequence) {
  2792	                actions[j] = actions[j - 1];
  2793	                j--;
  2794	            }
  2795	            actions[j] = key;
  2796	        }
  2797	    }
  2798	
  2799	    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
  2800	    function _executeMarketSellExternal(
  2801	        uint64 closedTick,
  2802	        uint32 clanId,
  2803	        uint32 clansmanId,
  2804	        address token,
  2805	        uint256 amount,
  2806	        uint64 commitSequence
  2807	    ) external {
  2808	        require(msg.sender == address(this), "ClanWorld: internal only");
  2809	        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
  2810	    }
  2811	
  2812	    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
  2813	    function _executeMarketBuyExternal(
  2814	        uint64 closedTick,
  2815	        uint32 clanId,
  2816	        uint32 clansmanId,
  2817	        address token,
  2818	        uint256 amountOut,
  2819	        uint256 maxGoldIn,
  2820	        uint64 commitSequence
  2821	    ) external {
  2822	        require(msg.sender == address(this), "ClanWorld: internal only");
  2823	        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
  2824	    }
  2825	
  2826	    /// @dev Map a resource token address to its pool address.
  2827	    function _poolFor(address token) internal view returns (address pool) {
  2828	        if (token == _treasury.woodToken) return _treasury.woodGoldPool;
  2829	        if (token == _treasury.ironToken) return _treasury.ironGoldPool;
  2830	        if (token == _treasury.wheatToken) return _treasury.wheatGoldPool;
  2831	        if (token == _treasury.fishToken) return _treasury.fishGoldPool;
  2832	        return address(0);
  2833	    }
  2834	
  2835	    /// @dev Add an amount of a resource token to the clan vault.
  2836	    function _addToVault(Clan storage clan, address token, uint256 amount) internal {
  2837	        if (token == _treasury.woodToken) {
  2838	            clan.vaultWood += amount;
  2839	            return;
  2840	        }
  2841	        if (token == _treasury.ironToken) {
  2842	            clan.vaultIron += amount;
  2843	            return;
  2844	        }
  2845	        if (token == _treasury.wheatToken) {
  2846	            clan.vaultWheat += amount;
  2847	            return;
  2848	        }
  2849	        if (token == _treasury.fishToken) {
  2850	            clan.vaultFish += amount;
  2851	            return;
  2852	        }
  2853	    }
  2854	
  2855	    /// @dev Deduct an amount of a resource token from the clan vault. Returns false if insufficient.
  2856	    function _deductFromVault(Clan storage clan, address token, uint256 amount) internal returns (bool) {
  2857	        if (token == _treasury.woodToken) {
  2858	            if (clan.vaultWood < amount) return false;
  2859	            clan.vaultWood -= amount;
  2860	            return true;
  2861	        }
  2862	        if (token == _treasury.ironToken) {
  2863	            if (clan.vaultIron < amount) return false;
  2864	            clan.vaultIron -= amount;
  2865	            return true;
  2866	        }
  2867	        if (token == _treasury.wheatToken) {
  2868	            if (clan.vaultWheat < amount) return false;
  2869	            clan.vaultWheat -= amount;
  2870	            return true;
  2871	        }
  2872	        if (token == _treasury.fishToken) {
  2873	            if (clan.vaultFish < amount) return false;
  2874	            clan.vaultFish -= amount;
  2875	            return true;
  2876	        }
  2877	        return false;
  2878	    }
  2879	
  2880	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
  2881	    function _executeMarketSell(
  2882	        uint64 closedTick,
  2883	        uint32 clanId,
  2884	        uint32 clansmanId,
  2885	        address token,
  2886	        uint256 amount,
  2887	        uint64 commitSequence
  2888	    ) internal {
  2889	        if (!_treasury.poolsSeeded) {
  2890	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  2891	            return;
  2892	        }
  2893	        address poolAddr = _poolFor(token);
  2894	        if (poolAddr == address(0)) {
  2895	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  2896	            return;
  2897	        }
  2898	
  2899	        Clan storage clan = _clans[clanId];
  2900	        if (!_deductFromVault(clan, token, amount)) {
  2901	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
  2902	            return;
  2903	        }
  2904	
  2905	        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
  2906	        clan.goldBalance += goldOut;
  2907	
  2908	        emit ScheduledMarketActionExecuted(
  2909	            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
  2910	        );
  2911	    }
  2912	
  2913	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
  2914	    function _executeMarketBuy(
  2915	        uint64 closedTick,
  2916	        uint32 clanId,
  2917	        uint32 clansmanId,
  2918	        address token,
  2919	        uint256 amountOut,
  2920	        uint256 maxGoldIn,
  2921	        uint64 commitSequence
  2922	    ) internal {
  2923	        if (!_treasury.poolsSeeded) {
  2924	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  2925	            return;
  2926	        }
  2927	        address poolAddr = _poolFor(token);
  2928	        if (poolAddr == address(0)) {
  2929	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  2930	            return;
  2931	        }
  2932	
  2933	        // Quote gold cost without updating reserves
  2934	        uint256 goldIn = StubPool(poolAddr).quoteBuy(amountOut);
  2935	
  2936	        Clan storage clan = _clans[clanId];
  2937	
  2938	        if (goldIn > maxGoldIn) {
  2939	            emit MarketActionFailed(
  2940	                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
  2941	            );
  2942	            return;
  2943	        }
  2944	        if (clan.goldBalance < goldIn) {
  2945	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
  2946	            return;
  2947	        }
  2948	
  2949	        // Execute — use return value to guard against any future pool divergence
  2950	        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
  2951	        clan.goldBalance -= actualGoldIn;
  2952	        _addToVault(clan, token, amountOut);
  2953	
  2954	        emit ScheduledMarketActionExecuted(
  2955	            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
  2956	        );
  2957	    }
  2958	
  2959	    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
  2960	        internal
  2961	        view
  2962	        returns (StatusCode)
  2963	    {
  2964	        ActionType action = order.action;
  2965	
  2966	        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
  2967	
  2968	        // DepositResources: must go to homebase
  2969	        if (action == ActionType.DepositResources) {
  2970	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  2971	        }
  2972	
  2973	        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
  2974	        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
  2975	        {
  2976	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  2977	        }
  2978	
  2979	        // ChopWood: must go to Forest
  2980	        if (action == ActionType.ChopWood) {
  2981	            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
  2982	        }
  2983	        // MineIron: must go to Mountains
  2984	        if (action == ActionType.MineIron) {
  2985	            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
  2986	        }
  2987	        // FishDocks: must go to WestDocks or EastDocks
  2988	        if (action == ActionType.FishDocks) {
  2989	            if (
  2990	                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
  2991	            ) {
  2992	                return StatusCode.ERR_INVALID_REGION;
  2993	            }
  2994	        }
  2995	        // FishDeepSea: must go to DeepSea
  2996	        if (action == ActionType.FishDeepSea) {
  2997	            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
  2998	        }
  2999	        // HarvestWheat: must go to WestFarms or EastFarms
  3000	        if (action == ActionType.HarvestWheat) {
  3001	            if (
  3002	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  3003	            ) {
  3004	                return StatusCode.ERR_INVALID_REGION;
  3005	            }
  3006	        }
  3007	
  3008	        if (action == ActionType.DefendBase) {
  3009	            return _validateDefendBaseOrder(clan, order, gotoRegion);
  3010	        }
  3011	
  3012	        // MarketBuy/MarketSell: must target Unicorn Town
  3013	        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  3014	            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  3015	            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
  3016	                return StatusCode.ERR_INVALID_REGION;
  3017	            }
  3018	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
  3019	            // Validate token is a supported resource token (not gold itself)
  3020	            address tok = order.marketToken;
  3021	            if (tok == address(0) || tok == _treasury.goldToken) {
  3022	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  3023	            }
  3024	            if (
  3025	                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
  3026	                    && tok != _treasury.fishToken
  3027	            ) {
  3028	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  3029	            }
  3030	            // Market orders are always enqueued for the arrivalTick FIFO queue.
  3031	            // _resolveAction records mission completion but does not execute any swap.
  3032	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
  3033	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
  3034	            }
  3035	        }
  3036	
  3037	        cs; // suppress unused warning
  3038	        return StatusCode.OK;
  3039	    }
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '3160,3680p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
  3266	    }
  3267	
  3268	    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
  3269	        Clansman memory cs = _clansmen[clansmanId];
  3270	        if (cs.clansmanId == 0) {
  3271	            Mission memory emptyMission;
  3272	            return DerivedClansmanState({
  3273	                clansman: cs, activeMission: emptyMission, effectiveRegion: 0, derivedAtTick: _world.currentTick
  3274	            });
  3275	        }
  3276	
  3277	        SettlementSimulation memory sim = _simulateSettleToTick(cs.clanId, _world.currentTick);
  3278	        (bool found, uint256 index) = _findSimulatedClansman(sim, clansmanId);
  3279	        if (found) {
  3280	            cs = sim.clansmen[index];
  3281	            Mission memory m = sim.missions[index];
  3282	            return DerivedClansmanState({
  3283	                clansman: cs,
  3284	                activeMission: m,
  3285	                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
  3286	                derivedAtTick: _world.currentTick
  3287	            });
  3288	        }
  3289	
  3290	        Mission memory fallbackMission = _missions[clansmanId];
  3291	        return DerivedClansmanState({
  3292	            clansman: cs,
  3293	            activeMission: fallbackMission,
  3294	            effectiveRegion: _effectiveRegion(cs, fallbackMission, _world.currentTick),
  3295	            derivedAtTick: _world.currentTick
  3296	        });
  3297	    }
  3298	
  3299	    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
  3300	        return _bandits[banditId].targetClanId;
  3301	    }
  3302	
  3303	    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
  3304	        external
  3305	        pure
  3306	        override
  3307	        returns (uint8 travelTicks, bytes8 path)
  3308	    {
  3309	        if (srcRegion > 8 || dstRegion > 8) {
  3310	            return (0, bytes8(0));
  3311	        }
  3312	        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
  3313	            travelTicks = 0;
  3314	            path = bytes8(uint64(srcRegion) << 56);
  3315	            return (travelTicks, path);
  3316	        }
  3317	        travelTicks = _travelTicks(srcRegion, dstRegion);
  3318	        path = _buildPath(srcRegion, dstRegion);
  3319	    }
  3320	
  3321	    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
  3322	        return _lootValueRaw(_clans[clanId]);
  3323	    }
  3324	
  3325	    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
  3326	        SettlementSimulation memory sim = _simulateSettleToTick(clanId, _world.currentTick);
  3327	        return _lootValueRaw(sim.clan);
  3328	    }
  3329	
  3330	    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
  3331	    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
  3332	        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
  3333	    }
  3334	
  3335	    function _derivedClanStateFromSimulation(Clan memory clan) internal view returns (DerivedClanState memory) {
  3336	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  3337	        return DerivedClanState({
  3338	            clan: clan, isStarving: starving, lootValue: _lootValueRaw(clan), derivedAtTick: _world.currentTick
  3339	        });
  3340	    }
  3341	
  3342	    function _findSimulatedClansman(SettlementSimulation memory sim, uint32 clansmanId)
  3343	        internal
  3344	        pure
  3345	        returns (bool found, uint256 index)
  3346	    {
  3347	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  3348	            if (sim.clansmen[i].clansmanId == clansmanId) {
  3349	                return (true, i);
  3350	            }
  3351	        }
  3352	    }
  3353	
  3354	    function _effectiveRegion(Clansman memory cs, Mission memory m, uint64 tick) internal pure returns (uint8) {
  3355	        if (cs.state == ClansmanState.TRAVELING && m.active) {
  3356	            return tick >= m.arrivalTick ? m.targetRegion : m.startRegion;
  3357	        }
  3358	        return cs.currentRegion;
  3359	    }
  3360	
  3361	    // =========================================================================
  3362	    // UI INDEXER AGGREGATOR GETTERS
  3363	    // =========================================================================
  3364	
  3365	    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
  3366	    ///      Carry amounts not included. Full view-only settlement deferred.
  3367	    ///      View function — no gas cost for off-chain indexer/UI reads.
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
  3406	        ClansmanFullView[] memory clansmen = new ClansmanFullView[](sim.clansmen.length);
  3407	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  3408	            Clansman memory cs = sim.clansmen[i];
  3409	            Mission memory m = sim.missions[i];
  3410	            DerivedClansmanState memory dcs = DerivedClansmanState({
  3411	                clansman: cs,
  3412	                activeMission: m,
  3413	                effectiveRegion: _effectiveRegion(cs, m, _world.currentTick),
  3414	                derivedAtTick: _world.currentTick
  3415	            });
  3416	            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
  3417	        }
  3418	
  3419	        uint32 thisClanDefendingBaseId = 0;
  3420	        for (uint256 i = 0; i < sim.clansmen.length; i++) {
  3421	            if (
  3422	                sim.missions[i].active && sim.missions[i].action == ActionType.DefendBase
  3423	                    && sim.clansmen[i].state == ClansmanState.ACTING
  3424	            ) {
  3425	                thisClanDefendingBaseId = sim.missions[i].targetRegion;
  3426	                break;
  3427	            }
  3428	        }
  3429	        if (thisClanDefendingBaseId == 0) {
  3430	            uint32[] storage csIds = _clanClansmanIds[clanId];
  3431	            for (uint256 i = 0; i < csIds.length; i++) {
  3432	                uint8 region = _clansmanDefendingRegion[csIds[i]];
  3433	                if (region != 0) {
  3434	                    thisClanDefendingBaseId = region;
  3435	                    break;
  3436	                }
  3437	            }
  3438	        }
  3439	
  3440	        return ClanFullView({
  3441	            clan: derivedClan,
  3442	            clansmen: clansmen,
  3443	            westPlot: sim.wheatPlots[0],
  3444	            eastPlot: sim.wheatPlots[1],
  3445	            incomingDefenderIds: _defendingClansByRegion[sim.clan.baseRegion],
  3446	            thisClanDefendingBaseId: thisClanDefendingBaseId
  3447	        });
  3448	    }
  3449	
  3450	    function getMarketState() external view override returns (MarketState memory) {
  3451	        return MarketState({
  3452	            wood: _poolReserves(_treasury.woodToken, _treasury.woodGoldPool),
  3453	            wheat: _poolReserves(_treasury.wheatToken, _treasury.wheatGoldPool),
  3454	            fish: _poolReserves(_treasury.fishToken, _treasury.fishGoldPool),
  3455	            iron: _poolReserves(_treasury.ironToken, _treasury.ironGoldPool),
  3456	            currentTick: _world.currentTick,
  3457	            currentTickQueue: _scheduledMarketActions[_world.currentTick],
  3458	            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]
  3459	        });
  3460	    }
  3461	
  3462	    function _poolReserves(address resourceToken, address poolAddr) internal view returns (PoolReserves memory pr) {
  3463	        pr.resourceToken = resourceToken;
  3464	        if (poolAddr == address(0) || resourceToken == address(0)) {
  3465	            return pr;
  3466	        }
  3467	        (uint256 rA, uint256 rB) = StubPool(poolAddr).getReserves();
  3468	        pr.resourceReserve = rA;
  3469	        pr.goldReserve = rB;
  3470	        pr.spotPriceGoldPerResource = rA > 0 ? (rB * 1e18) / rA : 0;
  3471	    }
  3472	
  3473	    function getActiveBanditView() external view override returns (ActiveBanditView memory) {
  3474	        BanditTroop memory bandit = _bandits[_world.activeBanditId];
  3475	        uint64 nextActionTick = 0;
  3476	        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
  3477	        if (exists) {
  3478	            if (bandit.state == BanditState.Spawned) {
  3479	                nextActionTick = bandit.tickEnteredState + 1;
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
  3531	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
  3532	                    Mission storage m = _missions[csId];
  3533	                    occupants[idx++] = RegionOccupant({
  3534	                        clansmanId: csId,
  3535	                        clanId: cid,
  3536	                        state: cs.state,
  3537	                        currentAction: m.active ? m.action : ActionType.None,
  3538	                        missionNonce: cs.lastMissionNonce
  3539	                    });
  3540	                }
  3541	            }
  3542	        }
  3543	        return occupants;
  3544	    }
  3545	}

codex
I’ve got the exact `23f7f1a` line map now. I’m cross-checking the new contract against the interface and tests next, because a couple of places already look like they may be internally consistent in tests but still break real consumers or stale-view expectations.
exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '90,240p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '240,420p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '560,780p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   761	    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
   762	
   763	    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external view returns (uint8 travelTicks, bytes8 path);
   764	
   765	    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);
   766	
   767	    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);
   768	
   769	    // -------------------------------------------------------------------------
   770	    // UI indexer aggregator getters (v4.4 additions)
   771	    //
   772	    // These are pure compositions of the derived getters above and exist only
   773	    // to reduce indexer RPC traffic. They MUST follow the same non-mutation
   774	    // rule as the underlying derived getters.
   775	    // -------------------------------------------------------------------------
   776	
   777	    /// @notice Single-call top-level world state plus per-clan leaderboard data.
   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
   779	    function getWorldSnapshot() external view returns (WorldSnapshot memory);
   780	

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {Vm} from "forge-std/Vm.sol";
     6	import {ClanWorld} from "../src/ClanWorld.sol";
     7	import {
     8	    ClanWorldConstants,
     9	    ClanState,
    10	    ClansmanState,
    11	    BanditState,
    12	    ActionType,
    13	    StatusCode,
    14	    Clan,
    15	    Mission,
    16	    ClanOrder,
    17	    OrderResult
    18	} from "../src/IClanWorld.sol";
    19	
    20	contract BanditAttackHarness is ClanWorld {
    21	    function forceAttackingBandit(uint8 region, uint32 strength, uint32 targetClanId) external returns (uint32 id) {
    22	        id = _spawnBandit(region, strength);
    23	        _bandits[id].state = BanditState.Attacking;
    24	        _bandits[id].targetClanId = targetClanId;
    25	    }
    26	
    27	    function setWallLevel(uint32 clanId, uint8 wallLevel) external {
    28	        _clans[clanId].wallLevel = wallLevel;
    29	    }
    30	
    31	    function setStarvationStartsAt(uint32 clanId, uint64 tick) external {
    32	        _clans[clanId].starvationStartsAtTick = tick;
    33	    }
    34	
    35	    function setClanUpkeepState(uint32 clanId, uint64 lastSettledTick, uint256 vaultWheat, uint256 vaultFish)
    36	        external
    37	    {
    38	        Clan storage clan = _clans[clanId];
    39	        clan.lastSettledTick = lastSettledTick;
    40	        clan.vaultWheat = vaultWheat;
    41	        clan.vaultFish = vaultFish;
    42	        clan.starvationStartsAtTick = 0;
    43	    }
    44	
    45	    function setBanditStrength(uint32 banditId, uint32 strength) external {
    46	        _bandits[banditId].strength = strength;
    47	    }
    48	
    49	    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
    50	        external
    51	    {
    52	        _bandits[banditId].carryWood = wood;
    53	        _bandits[banditId].carryWheat = wheat;
    54	        _bandits[banditId].carryFish = fish;
    55	        _bandits[banditId].carryIron = iron;
    56	        _bandits[banditId].carryGold = gold;
    57	    }
    58	
    59	    function defenseRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId) external pure returns (uint32) {
    60	        return _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
    61	    }
    62	}
    63	
    64	contract BanditAttackResolutionTest is Test {
    65	    BanditAttackHarness world;
    66	    address elder = address(0xA1);
    67	
    68	    event BanditAttackResolved(
    69	        uint32 indexed banditId,
    70	        uint32 indexed targetClanId,
    71	        bool defended,
    72	        uint16 attackPower,
    73	        uint16 totalDefense,
    74	        uint16 wallLevelAfter,
    75	        uint256 stolenWood,
    76	        uint256 stolenIron,
    77	        uint256 stolenWheat,
    78	        uint256 stolenFish,
    79	        uint64 atTick
    80	    );
    81	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
    82	    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
    83	    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
    84	    event LootDistributed(
    85	        uint32 indexed banditId,
    86	        uint32[] clanIdsRewarded,
    87	        uint256 perClanWood,
    88	        uint256 perClanWheat,
    89	        uint256 perClanFish,
    90	        uint256 perClanIron,
    91	        uint256 perClanGold,
    92	        uint256 burnedWood,
    93	        uint256 burnedWheat,
    94	        uint256 burnedFish,
    95	        uint256 burnedIron,
    96	        uint256 burnedGold
    97	    );
    98	
    99	    function setUp() public {
   100	        world = new BanditAttackHarness();
   101	    }
   102	
   103	    function _mintClan() internal returns (uint32 clanId) {
   104	        vm.prank(elder);
   105	        (clanId,) = world.mintClan(elder);
   106	    }
   107	
   108	    function _csId(uint32 clanId, uint256 index) internal view returns (uint32) {
   109	        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
   110	    }
   111	
   112	    function _defendOrders(uint32 clanId, uint256 count) internal view returns (ClanOrder[] memory orders) {
   113	        Clan memory clan = world.getClan(clanId);
   114	        return _defendTargetOrders(clanId, clanId, clan.baseRegion, count);
   115	    }
   116	
   117	    function _defendTargetOrders(uint32 clanId, uint32 targetClanId, uint8 targetRegion, uint256 count)
   118	        internal
   119	        view
   120	        returns (ClanOrder[] memory orders)
   121	    {
   122	        orders = new ClanOrder[](count);
   123	        for (uint256 i = 0; i < count; i++) {
   124	            orders[i] = ClanOrder({
   125	                clansmanId: _csId(clanId, i),
   126	                gotoRegion: targetRegion,
   127	                action: ActionType.DefendBase,
   128	                targetClanId: targetClanId,
   129	                marketToken: address(0),
   130	                marketAmount: 0,
   131	                maxGoldIn: 0
   132	            });
   133	        }
   134	    }
   135	
   136	    function _submitDefenders(uint32 clanId, uint256 count) internal {
   137	        _submitTargetDefenders(clanId, clanId, count);
   138	    }
   139	
   140	    function _submitTargetDefenders(uint32 defenderClanId, uint32 targetClanId, uint256 count)
   141	        internal
   142	        returns (uint64 maxExecutesAtTick)
   143	    {
   144	        Clan memory targetClan = world.getClan(targetClanId);
   145	        ClanOrder[] memory orders = _defendTargetOrders(defenderClanId, targetClanId, targetClan.baseRegion, count);
   146	        vm.prank(elder);
   147	        OrderResult[] memory results = world.submitClanOrders(defenderClanId, orders);
   148	        for (uint256 i = 0; i < count; i++) {
   149	            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "defender order status");
   150	            Mission memory mission = world.getActiveMission(orders[i].clansmanId);
   151	            if (mission.executesAtTick > maxExecutesAtTick) {
   152	                maxExecutesAtTick = mission.executesAtTick;
   153	            }
   154	        }
   155	    }
   156	
   157	    function _advanceTick() internal {
   158	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   159	        world.heartbeat();
   160	    }
   161	
   162	    function _advanceUntilAfter(uint64 tick) internal {
   163	        while (world.getWorldState().currentTick <= tick) {
   164	            _advanceTick();
   165	        }
   166	    }
   167	
   168	    function _advanceUntil(uint64 tick) internal {
   169	        while (world.getWorldState().currentTick < tick) {
   170	            _advanceTick();
   171	        }
   172	    }
   173	
   174	    function _assertBanditTargetDiedLog(
   175	        Vm.Log[] memory logs,
   176	        uint32 expectedBanditId,
   177	        uint32 expectedDeadClanId,
   178	        uint64 expectedTick
   179	    ) internal {
   180	        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
   181	        bytes32 expectedBanditTopic = bytes32(uint256(expectedBanditId));
   182	        bytes32 expectedClanTopic = bytes32(uint256(expectedDeadClanId));
   183	        for (uint256 i = 0; i < logs.length; i++) {
   184	            if (
   185	                logs[i].topics.length == 3 && logs[i].topics[0] == eventSig
   186	                    && logs[i].topics[1] == expectedBanditTopic && logs[i].topics[2] == expectedClanTopic
   187	            ) {
   188	                uint64 tick = abi.decode(logs[i].data, (uint64));
   189	                if (tick == expectedTick) return;
   190	            }
   191	        }
   192	        fail("expected BanditTargetDied log");
   193	    }
   194	
   195	    function _activateTargetDefenders(uint32 targetClanId, uint32[] memory defenderClanIds, uint256 countEach)
   196	        internal
   197	    {
   198	        uint64 maxExecutesAtTick;
   199	        for (uint256 i = 0; i < defenderClanIds.length; i++) {
   200	            uint64 executesAtTick = _submitTargetDefenders(defenderClanIds[i], targetClanId, countEach);
   201	            if (executesAtTick > maxExecutesAtTick) {
   202	                maxExecutesAtTick = executesAtTick;
   203	            }
   204	        }
   205	
   206	        _advanceUntilAfter(maxExecutesAtTick);
   207	        for (uint256 i = 0; i < defenderClanIds.length; i++) {
   208	            world.settleClan(defenderClanIds[i]);
   209	        }
   210	    }
   211	
   212	    function _forceAttack(uint32 clanId, uint32 strength) internal returns (uint32 banditId) {
   213	        Clan memory clan = world.getClan(clanId);
   214	        return world.forceAttackingBandit(clan.baseRegion, strength, clanId);
   215	    }
   216	
   217	    function _mintClans(uint256 count) internal returns (uint32[] memory clanIds) {
   218	        clanIds = new uint32[](count);
   219	        for (uint256 i = 0; i < count; i++) {
   220	            clanIds[i] = _mintClan();
   221	        }
   222	    }
   223	
   224	    function test_defeatedBanditLootSingleDefendingClanGetsFullCarry() public {
   225	        uint32 clanId = _mintClan();
   226	        uint32[] memory defenders = new uint32[](1);
   227	        defenders[0] = clanId;
   228	        _activateTargetDefenders(clanId, defenders, 4);
   229	
   230	        Clan memory beforeClan = world.getClan(clanId);
   231	        uint32 banditId = _forceAttack(clanId, 1);
   232	        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
   233	        world.setBanditStrength(banditId, 0);
   234	
   235	        _advanceTick();
   236	
   237	        Clan memory afterClan = world.getClan(clanId);
   238	        assertEq(afterClan.vaultWood, beforeClan.vaultWood + 7e18, "wood full carry");
   239	        assertEq(afterClan.vaultWheat, beforeClan.vaultWheat + 11e18, "wheat full carry");
   240	        assertEq(afterClan.vaultFish, beforeClan.vaultFish + 13e18, "fish full carry");
   241	        assertEq(afterClan.vaultIron, beforeClan.vaultIron + 17e18, "iron full carry");
   242	        assertEq(afterClan.goldBalance, beforeClan.goldBalance + 19e18, "gold full carry");
   243	        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
   244	    }
   245	
   246	    function test_defeatedBanditAwardsBlueprintToTargetClan() public {
   247	        uint32 clanId = _mintClan();
   248	        uint32[] memory defenders = new uint32[](1);
   249	        defenders[0] = clanId;
   250	        _activateTargetDefenders(clanId, defenders, 4);
   251	
   252	        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
   253	        uint32 banditId = _forceAttack(clanId, 1);
   254	        world.setBanditStrength(banditId, 0);
   255	
   256	        vm.expectEmit(true, true, false, true, address(world));
   257	        emit BlueprintEarned(clanId, banditId, world.getWorldState().currentTick);
   258	
   259	        _advanceTick();
   260	

codex
I’ve confirmed the SHA in git and switched from the pasted cumulative diff to the actual head tree, which is much safer. I’m going to run the focused Foundry tests next so we can separate “looks suspicious” from “actually protected by the current suite.”
exec
/bin/bash -lc 'forge test --match-path packages/contracts/test/BanditSpawn.t.sol' in /home/claude/code/clan-world/packages/contracts
 exited 127 in 0ms:
/bin/bash: line 1: forge: command not found

exec
/bin/bash -lc 'forge test --match-path packages/contracts/test/Bandit.t.sol' in /home/claude/code/clan-world/packages/contracts
 exited 127 in 0ms:
/bin/bash: line 1: forge: command not found

exec
/bin/bash -lc 'forge test --match-path packages/contracts/test/BanditAttackResolution.t.sol' in /home/claude/code/clan-world/packages/contracts
 exited 127 in 0ms:
/bin/bash: line 1: forge: command not found

exec
/bin/bash -lc 'forge test --match-contract HeartbeatOrderingTest' in /home/claude/code/clan-world/packages/contracts
 exited 127 in 0ms:
/bin/bash: line 1: forge: command not found

exec
/bin/bash -lc 'git show 23f7f1a''^:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '"'1650,1785p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1650	    }
  1651	
  1652	    function _banditStrengthForLegacyEvent(uint32 strength) internal pure returns (uint16) {
  1653	        if (strength > type(uint16).max) return type(uint16).max;
  1654	        // forge-lint: disable-next-line(unsafe-typecast)
  1655	        return uint16(strength);
  1656	    }
  1657	
  1658	    function _resolveAttackingBandits(uint64 closedTick) internal {
  1659	        for (uint8 region = ClanWorldConstants.REGION_FOREST; region <= ClanWorldConstants.REGION_DEEP_SEA; region++) {
  1660	            uint32[] storage regionBandits = _banditsByRegion[region];
  1661	            uint256 i = 0;
  1662	            while (i < regionBandits.length) {
  1663	                uint32 banditId = regionBandits[i];
  1664	                BanditTroop storage bandit = _bandits[banditId];
  1665	                bool shouldResolve =
  1666	                    bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
  1667	                if (shouldResolve) {
  1668	                    _resolveBanditAttack(banditId, closedTick);
  1669	                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
  1670	                        continue;
  1671	                    }
  1672	                }
  1673	                i++;
  1674	            }
  1675	        }
  1676	    }
  1677	
  1678	    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
  1679	        require(_world.currentTick == closedTick, "ClanWorld: bandit attack tick mismatch");
  1680	
  1681	        BanditTroop storage bandit = _bandits[banditId];
  1682	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
  1683	            return;
  1684	        }
  1685	        if (bandit.tickEnteredState != closedTick) {
  1686	            return;
  1687	        }
  1688	
  1689	        uint32 targetClanId = bandit.targetClanId;
  1690	        Clan storage targetClan = _clans[targetClanId];
  1691	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
  1692	            _transitionBanditState(banditId, BanditState.Escaped);
  1693	            emit BanditEscaped(banditId, closedTick);
  1694	            return;
  1695	        }
  1696	
  1697	        _settleClan(targetClanId);
  1698	        _eagerSettleActiveDefendersForBase(targetClanId, targetClan.baseRegion);
  1699	
  1700	        bytes32 tickSeed = _world.currentTickSeed;
  1701	        uint32 banditAttackPower = bandit.strength;
  1702	        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
  1703	        bool defeated = uint256(totalClansmanDefense) >= uint256(banditAttackPower) * 2;
  1704	
  1705	        uint32 wallDamage;
  1706	        uint32 baseAbsorbed;
  1707	        uint32 clansmanDamageAbsorbed;
  1708	        if (!defeated) {
  1709	            uint32 incomingDamage =
  1710	                banditAttackPower > totalClansmanDefense ? banditAttackPower - totalClansmanDefense : 0;
  1711	            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
  1712	            (incomingDamage, baseAbsorbed) = _applyBanditBaseDefense(targetClan, incomingDamage);
  1713	            clansmanDamageAbsorbed =
  1714	                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
  1715	        }
  1716	
  1717	        uint32 totalDefense = totalClansmanDefense + wallDamage + baseAbsorbed + clansmanDamageAbsorbed;
  1718	        emit BanditAttackResolved(
  1719	            banditId,
  1720	            targetClanId,
  1721	            defeated,
  1722	            _uint16Clamp(banditAttackPower),
  1723	            _uint16Clamp(totalDefense),
  1724	            targetClan.wallLevel,
  1725	            0,
  1726	            0,
  1727	            0,
  1728	            0,
  1729	            closedTick
  1730	        );
  1731	
  1732	        if (defeated) {
  1733	            emit BanditDefeated(banditId, targetClanId, closedTick);
  1734	            _distributeBanditLootToDefendingClans(banditId, targetClanId);
  1735	            targetClan.blueprintBalance += 1;
  1736	            emit BlueprintEarned(targetClanId, banditId, closedTick);
  1737	            _transitionBanditState(banditId, BanditState.Defeated);
  1738	        } else {
  1739	            _transitionBanditState(banditId, BanditState.Escaped);
  1740	            emit BanditEscaped(banditId, closedTick);
  1741	        }
  1742	    }
  1743	
  1744	    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
  1745	        BanditTroop storage bandit = _bandits[banditId];
  1746	        uint32[] memory rewardedClanIds = _activeDefendingClanIds(targetClanId);
  1747	        uint256 nDefendingClans = rewardedClanIds.length;
  1748	
  1749	        uint256 perWood;
  1750	        uint256 perIron;
  1751	        uint256 perWheat;
  1752	        uint256 perFish;
  1753	        uint256 perGold;
  1754	        if (nDefendingClans > 0) {
  1755	            perWood = _perClanBanditLootShare(bandit.carryWood, nDefendingClans);
  1756	            perIron = _perClanBanditLootShare(bandit.carryIron, nDefendingClans);
  1757	            perWheat = _perClanBanditLootShare(bandit.carryWheat, nDefendingClans);
  1758	            perFish = _perClanBanditLootShare(bandit.carryFish, nDefendingClans);
  1759	            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
  1760	
  1761	            for (uint256 i = 0; i < rewardedClanIds.length; i++) {
  1762	                Clan storage defenderClan = _clans[rewardedClanIds[i]];
  1763	                defenderClan.vaultWood += perWood;
  1764	                defenderClan.vaultIron += perIron;
  1765	                defenderClan.vaultWheat += perWheat;
  1766	                defenderClan.vaultFish += perFish;
  1767	                defenderClan.goldBalance += perGold;
  1768	            }
  1769	        }
  1770	
  1771	        emit LootDistributed(
  1772	            banditId,
  1773	            rewardedClanIds,
  1774	            perWood,
  1775	            perWheat,
  1776	            perFish,
  1777	            perIron,
  1778	            perGold,
  1779	            bandit.carryWood - (perWood * nDefendingClans),
  1780	            bandit.carryWheat - (perWheat * nDefendingClans),
  1781	            bandit.carryFish - (perFish * nDefendingClans),
  1782	            bandit.carryIron - (perIron * nDefendingClans),
  1783	            bandit.carryGold - (perGold * nDefendingClans)
  1784	        );
  1785	    }

exec
/bin/bash -lc "rg -n \"BanditState\\.(NONE|CAMPING|RESTING|ATTACKING|DEFEATED|ESCAPED|None|Spawned|Camped|Resting|Attacking|Defeated|Escaped)|banditId|currentRegion|attackAttemptsMade|stateEnteredTick|nextActionTick|tier|attackPower|carryGold|getBandit\\(|getBanditTroop\\(|getBanditsInRegion\\(|LootDistributed\\(|BlueprintEarned|BanditTargetDied|WallDamagedByBandit|ClansmanKilledByBandit\" . --glob '"'!**/node_modules/**'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
./AGENTS.md:116:- `pr-review.md` — local 3-tier swarm review protocol
./AGENTS.md:123:- Run the local 3-tier swarm before opening the PR (Claude subagent + Codex + Gemini flash). All three GREEN.
./packages/runner/src/pollChainTick.ts:14: * full snapshot — cheaper on the public Convex tier.
./packages/shared/src/mocks/clanWorldFixture.ts:16:// yields, defense decomposition, bandit tiers). Resource amounts use 18-decimal
./packages/shared/src/mocks/clanWorldFixture.ts:77:  banditId: string;
./packages/shared/src/mocks/clanWorldFixture.ts:79:  currentRegion: string;
./packages/shared/src/mocks/clanWorldFixture.ts:81:  stateEnteredTick: Tick;
./packages/shared/src/mocks/clanWorldFixture.ts:83:  nextActionTick: Tick;
./packages/shared/src/mocks/clanWorldFixture.ts:84:  attackAttemptsMade: number;
./packages/shared/src/mocks/clanWorldFixture.ts:85:  tier: number;
./packages/shared/src/mocks/clanWorldFixture.ts:86:  attackPower: number;
./packages/shared/src/mocks/clanWorldFixture.ts:188:  blueprintBalance: '2000000000000000000', // closing on tier-7 unlock
./packages/shared/src/mocks/clanWorldFixture.ts:252:      'Blueprint count at 2. Two more and I unlock monument tier 7. Stay isolated, let the others bleed each other.',
./packages/shared/src/mocks/clanWorldFixture.ts:270:      'Two clansmen stationed defend_base, third en route from Mountains gathering. Will not be enough if bandits land tier-3+.',
./packages/shared/src/mocks/clanWorldFixture.ts:302:    text: 'Monument tier 6 reached. Trading partnerships welcome — I have wheat surplus. No alliances of convenience.',
./packages/shared/src/mocks/clanWorldFixture.ts:310:  banditId: 'bandit-001',
./packages/shared/src/mocks/clanWorldFixture.ts:312:  currentRegion: 'mountains',
./packages/shared/src/mocks/clanWorldFixture.ts:313:  stateEnteredTick: 78,
./packages/shared/src/mocks/clanWorldFixture.ts:314:  nextActionTick: 81,
./packages/shared/src/mocks/clanWorldFixture.ts:315:  attackAttemptsMade: 0,
./packages/shared/src/mocks/clanWorldFixture.ts:316:  tier: 2,
./packages/shared/src/mocks/clanWorldFixture.ts:317:  attackPower: 45, // tier-2 per v4 §6.14
./packages/shared/src/adapters/IChainClient.ts:121:                      { name: 'currentRegion', type: 'uint8' },
./packages/contracts/src/ClanWorld.sol:272:                    cs.currentRegion = m.targetRegion;
./packages/contracts/src/ClanWorld.sol:576:        if (cs.currentRegion != clan.baseRegion) {
./packages/contracts/src/ClanWorld.sol:614:        if (cs.currentRegion != clan.baseRegion) {
./packages/contracts/src/ClanWorld.sol:819:            cs.currentRegion = baseRegion;
./packages/contracts/src/ClanWorld.sol:913:        ctx.fromRegion = cs.currentRegion;
./packages/contracts/src/ClanWorld.sol:957:            cs.currentRegion = ctx.gotoRegion;
./packages/contracts/src/ClanWorld.sol:1359:            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN &&
./packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./packages/contracts/src/ClanWorld.sol:1466:            banditId: 0,
./packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
./packages/contracts/src/ClanWorld.sol:1468:            currentRegion: 0,
./packages/contracts/src/ClanWorld.sol:1469:            attackAttemptsMade: 0,
./packages/contracts/src/ClanWorld.sol:1470:            stateEnteredTick: 0,
./packages/contracts/src/ClanWorld.sol:1471:            nextActionTick: 0,
./packages/contracts/src/ClanWorld.sol:1472:            tier: 0,
./packages/contracts/src/ClanWorld.sol:1473:            attackPower: 0,
./packages/contracts/src/ClanWorld.sol:1540:        uint8 effectiveRegion = cs.currentRegion;
./packages/contracts/src/ClanWorld.sol:1653:            uint8 effRegion = cs.currentRegion;
./packages/contracts/src/ClanWorld.sol:1712:            banditId: 0,
./packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
./packages/contracts/src/ClanWorld.sol:1714:            currentRegion: 0,
./packages/contracts/src/ClanWorld.sol:1715:            attackAttemptsMade: 0,
./packages/contracts/src/ClanWorld.sol:1717:            stateEnteredTick: 0,
./packages/contracts/src/ClanWorld.sol:1718:            nextActionTick: 0,
./packages/contracts/src/ClanWorld.sol:1719:            tier: 0,
./packages/contracts/src/ClanWorld.sol:1720:            attackPower: 0,
./packages/contracts/src/ClanWorld.sol:1745:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
./packages/contracts/src/ClanWorld.sol:1759:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
./packages/contracts/src/IClanWorld.sol:261:    uint8 currentRegion;
./packages/contracts/src/IClanWorld.sol:296:    uint32 banditId;
./packages/contracts/src/IClanWorld.sol:299:    uint8 currentRegion;
./packages/contracts/src/IClanWorld.sol:300:    uint8 attackAttemptsMade;
./packages/contracts/src/IClanWorld.sol:301:    uint64 stateEnteredTick;
./packages/contracts/src/IClanWorld.sol:302:    uint64 nextActionTick;
./packages/contracts/src/IClanWorld.sol:304:    uint8 tier;
./packages/contracts/src/IClanWorld.sol:305:    uint16 attackPower;            // derived from tier; tier is canonical (v4.3 §G)
./packages/contracts/src/IClanWorld.sol:450:    uint32 banditId;
./packages/contracts/src/IClanWorld.sol:452:    uint8  currentRegion;
./packages/contracts/src/IClanWorld.sol:453:    uint8  attackAttemptsMade;
./packages/contracts/src/IClanWorld.sol:455:    uint64 stateEnteredTick;
./packages/contracts/src/IClanWorld.sol:456:    uint64 nextActionTick;
./packages/contracts/src/IClanWorld.sol:457:    uint8  tier;
./packages/contracts/src/IClanWorld.sol:458:    uint16 attackPower;
./packages/contracts/src/IClanWorld.sol:595:    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./packages/contracts/src/IClanWorld.sol:597:        uint32 indexed banditId,
./packages/contracts/src/IClanWorld.sol:603:    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
./packages/contracts/src/IClanWorld.sol:605:        uint32 indexed banditId,
./packages/contracts/src/IClanWorld.sol:608:        uint16 attackPower,
./packages/contracts/src/IClanWorld.sol:617:    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./packages/contracts/src/IClanWorld.sol:618:    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./packages/contracts/src/IClanWorld.sol:619:    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./packages/contracts/src/IClanWorld.sol:621:        uint32 indexed banditId,
./packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
./packages/contracts/src/ClanWorldStub.sol:158:            currentRegion: 0,
./packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./packages/contracts/src/ClanWorldStub.sol:190:            banditId: 0,
./packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
./packages/contracts/src/ClanWorldStub.sol:192:            currentRegion: 0,
./packages/contracts/src/ClanWorldStub.sol:193:            attackAttemptsMade: 0,
./packages/contracts/src/ClanWorldStub.sol:194:            stateEnteredTick: 0,
./packages/contracts/src/ClanWorldStub.sol:195:            nextActionTick: 0,
./packages/contracts/src/ClanWorldStub.sol:196:            tier: 0,
./packages/contracts/src/ClanWorldStub.sol:197:            attackPower: 0,
./packages/contracts/src/ClanWorldStub.sol:314:            banditId: 0,
./packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
./packages/contracts/src/ClanWorldStub.sol:316:            currentRegion: 0,
./packages/contracts/src/ClanWorldStub.sol:317:            attackAttemptsMade: 0,
./packages/contracts/src/ClanWorldStub.sol:319:            stateEnteredTick: 0,
./packages/contracts/src/ClanWorldStub.sol:320:            nextActionTick: 0,
./packages/contracts/src/ClanWorldStub.sol:321:            tier: 0,
./packages/contracts/src/ClanWorldStub.sol:322:            attackPower: 0,
./packages/contracts/abi/IClanWorld.json:31:              "name": "banditId",
./packages/contracts/abi/IClanWorld.json:41:              "name": "currentRegion",
./packages/contracts/abi/IClanWorld.json:46:              "name": "attackAttemptsMade",
./packages/contracts/abi/IClanWorld.json:56:              "name": "stateEnteredTick",
./packages/contracts/abi/IClanWorld.json:61:              "name": "nextActionTick",
./packages/contracts/abi/IClanWorld.json:66:              "name": "tier",
./packages/contracts/abi/IClanWorld.json:71:              "name": "attackPower",
./packages/contracts/abi/IClanWorld.json:261:              "name": "banditId",
./packages/contracts/abi/IClanWorld.json:271:              "name": "currentRegion",
./packages/contracts/abi/IClanWorld.json:276:              "name": "attackAttemptsMade",
./packages/contracts/abi/IClanWorld.json:281:              "name": "stateEnteredTick",
./packages/contracts/abi/IClanWorld.json:286:              "name": "nextActionTick",
./packages/contracts/abi/IClanWorld.json:291:              "name": "tier",
./packages/contracts/abi/IClanWorld.json:296:              "name": "attackPower",
./packages/contracts/abi/IClanWorld.json:602:                          "name": "currentRegion",
./packages/contracts/abi/IClanWorld.json:917:              "name": "currentRegion",
./packages/contracts/abi/IClanWorld.json:1126:                  "name": "currentRegion",
./packages/contracts/abi/IClanWorld.json:2299:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2317:          "name": "attackPower",
./packages/contracts/abi/IClanWorld.json:2372:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2397:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2416:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2447:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2459:          "name": "tier",
./packages/contracts/abi/IClanWorld.json:2465:          "name": "attackPower",
./packages/contracts/abi/IClanWorld.json:2478:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2552:          "name": "banditId",
./packages/contracts/abi/IClanWorld.json:2832:          "name": "banditId",
./packages/contracts/test/ClanWorld.t.sol:220:            assertEq(cs.currentRegion, baseRegion, "clansman should be at homebase");
./apps/server/convex/mock.ts:40:      { level: "warn" as const, message: "Bandit troop spotted near Mountains (tier 2)" },
./package.json:19:    "prettier": "3.8.3",
./pnpm-lock.yaml:17:      prettier:
./pnpm-lock.yaml:1742:  prettier@3.2.5:
./pnpm-lock.yaml:1747:  prettier@3.8.3:
./pnpm-lock.yaml:2924:      prettier: 3.2.5
./pnpm-lock.yaml:2933:      prettier: 3.2.5
./pnpm-lock.yaml:3395:  prettier@3.2.5: {}
./pnpm-lock.yaml:3397:  prettier@3.8.3: {}
./apps/landing/src/pages/LorePage.tsx:27:  { tier: 'I', strength: 30, reward: '1 blueprint', desc: 'Hungry and disorganised. A welcome practice fight.' },
./apps/landing/src/pages/LorePage.tsx:28:  { tier: 'II', strength: 45, reward: '1 blueprint', desc: 'Tested. They know which farms have wheat and which have walls.' },
./apps/landing/src/pages/LorePage.tsx:29:  { tier: 'III', strength: 65, reward: '2 blueprints', desc: 'Veterans. They will not engage unless they expect to win.' },
./apps/landing/src/pages/LorePage.tsx:30:  { tier: 'IV', strength: 90, reward: '2 blueprints', desc: 'Warlords. Their captains have names. Some clans pay tribute rather than fight.' },
./apps/landing/src/pages/LorePage.tsx:31:  { tier: 'V', strength: 130, reward: '3 blueprints', desc: 'Apocalyptic. A clan alone cannot stand. Coalitions form, and break, around them.' },
./apps/landing/src/pages/LorePage.tsx:613:          bandit camp roams the realm by tier, growing stronger and richer as
./apps/landing/src/pages/LorePage.tsx:631:              <tr key={b.tier}>
./apps/landing/src/pages/LorePage.tsx:632:                <td className="bestiary-tier">{b.tier}</td>
./apps/landing/src/pages/LorePage.tsx:762:          ten-tier monument. The realm has decided this.
./apps/landing/src/pages/LorePage.css:461:.bestiary-tier {
./docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md:5:	1.	I made BanditState.NONE = 0 so bandit.state == NONE is the natural “no active bandit” check. v4.2 omitted this; adding it avoids the ambiguous default value.
./docs/reviews/pr132-review-claude-opus.md:36:| 1 | Convention & Process Compliance | Check gitflow, commits, PR process | Missing `Closes #N`, no 3-tier swarm evidence |
./docs/reviews/pr132-review-claude-opus.md:47:| 2 | *(PR process)* | — | No evidence of local 3-tier swarm (Claude subagent + Codex + Gemini flash). Only Gemini Code Assist (cloud) reviewed. `pr-review.md` requires all 3 local tiers GREEN for Wave 1+ contract code before merge. | **MUST FIX** |
./docs/reviews/pr132-review-claude-opus.md:88:2. **Run the local 3-tier swarm** (Claude subagent + Codex + Gemini flash) and post signed tier comments on PR #132. Per `pr-review.md`, "Wave 1+ contract code: full 3-tier mandatory." Cloud Gemini Code Assist alone does not satisfy the convergence gate.
./docs/reviews/pr132-review-claude-opus.md:122:- The local 3-tier swarm review gate is satisfied (process requirement)
./docs/conventions/pr-review.md:20:- New rule: **local swarm is the iteration loop.** Run all 3 local tiers, apply fixes, re-run locally, REPEAT until local CLEAN before opening the PR. Cloud is a SINGLE final sanity pass.
./docs/conventions/pr-review.md:32:## Per-tier comments
./docs/conventions/pr-review.md:34:Each reviewer self-posts a brief signed comment on the PR identifying their tier:
./docs/conventions/pr-review.md:44:- All 3 local tiers report CLEAN on the latest commit.
./docs/conventions/pr-review.md:48:The orchestrator checks all three tiers — PM-internal swarm rounds aren't a substitute for orch-level swarm review on security-sensitive code.
./docs/conventions/pr-review.md:64:- **Wave 0 (this PR):** scaffold + docs only — single tier (local Claude subagent) is fine. No security implications.
./docs/conventions/pr-review.md:65:- **Wave 1+ contract code:** full 3-tier mandatory.
./docs/conventions/pr-review.md:66:- **Wave 1+ frontend / agents code:** full 3-tier mandatory.
./docs/conventions/pr-review.md:67:- **Demo-only fix commits during H4–H6:** single tier acceptable; code lives ~1 hour.
./docs/conventions/gitflow.md:19:4. **Run local 3-tier swarm review** before opening the PR (see `pr-review.md`). All three GREEN.
./docs/planning/phase-3-test-spec.md:38:**Assert:** Either (a) no bandit spawns (spawn skipped due to excluded region) OR (b) bandit's `currentRegion` is NOT Unicorn Town and NOT Deep Sea.
./docs/planning/phase-3-test-spec.md:64:- `BanditTroop.nextActionTick == N + CAMPING_DURATION` (e.g. 2 or 3 per implementation)
./docs/planning/phase-3-test-spec.md:65:- `BanditTroop.stateEnteredTick == N`
./docs/planning/phase-3-test-spec.md:66:- `BanditTroop.currentRegion` is a valid non-excluded region.
./docs/planning/phase-3-test-spec.md:72:**Setup:** Spawn bandit at tick N with `nextActionTick = N + 3`.
./docs/planning/phase-3-test-spec.md:82:**Setup:** Spawn bandit at tick N, `nextActionTick = N + 2`. Mint a clan in the bandit's region (so target selection can succeed).
./docs/planning/phase-3-test-spec.md:86:**Assert:** `BanditTroop.state == BanditState.Attacking`.
./docs/planning/phase-3-test-spec.md:205:- Register N defenders at defender clan's base; their combined `defensePoints` > bandit's `attackPower`.
./docs/planning/phase-3-test-spec.md:211:- `BanditTroop.state == BanditState.Defeated`.
./docs/planning/phase-3-test-spec.md:220:- Defender clan has 0 defenders registered (or weak defenders: `totalDefense < attackPower`).
./docs/planning/phase-3-test-spec.md:289:**Setup:** Resolve one bandit attack. Read `defenseContributionsByBanditId[banditId]`.
./docs/planning/phase-3-test-spec.md:307:**Setup:** Defender clan vault has 200 wood. Bandit `attackPower` overshoot carries 50 wood.
./docs/planning/phase-3-test-spec.md:388:**Setup:** Upgrade wall from tier 0 → 1 (cost C0). Record cost. Upgrade from tier 1 → 2 (cost C1).
./docs/planning/phase-3-test-spec.md:460:- `defenseContributionsByBanditId[banditId]` empty after resolution.
./docs/planning/phase-3-test-spec.md:475:**Fuzzer:** For any spawned bandit, `BanditTroop.currentRegion != REGION_UNICORN_TOWN && != REGION_DEEP_SEA`.
./docs/planning/phase-3-test-spec.md:502:- The `_eagerSettleForBandit(banditId)` call MUST happen before target selection AND before damage application (3.E2). Tests 3.E2-a and 3.E2-b directly verify this ordering.
./docs/planning/clanworld_v4_3_schema_patch.md:11:- `gotoRegion == currentRegion`, or
./docs/planning/clanworld_v4_3_schema_patch.md:178:`BanditTroop.tier` is canonical.
./docs/planning/clanworld_v4_3_schema_patch.md:180:`attackPower` is **derived**, not independently authoritative.
./docs/planning/clanworld_v4_3_schema_patch.md:183:Attack power is computed from tier using the locked bandit attack table / helper:
./docs/planning/clanworld_v4_3_schema_patch.md:185:`attackPower = getBanditAttackPower(tier)`
./docs/planning/clanworld_v4_3_schema_patch.md:188:If both `tier` and `attackPower` are present in an implementation struct for convenience, `tier` remains the source of truth and `attackPower` must be treated as cached/derived.
./docs/planning/clanworld_v4_3_schema_patch.md:266:- explicit constants per tier, or
./docs/planning/clanworld_v4_3_schema_patch.md:348:- `tier` as canonical bandit strength source
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:232:- no `carryGold` field exists
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:428:    uint8 currentRegion;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:473:    uint32 banditId;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:476:    uint8 currentRegion;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:477:    uint8 attackAttemptsMade;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:478:    uint64 stateEnteredTick;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:479:    uint64 nextActionTick;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:481:    uint8 tier;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:482:    uint16 attackPower;
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:608:function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:620:function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:883:event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:884:event BanditAttackResolved(uint32 indexed banditId, uint32 indexed targetClanId, bool defended, uint16 attackPower, uint16 totalDefense);
./docs/planning/clanworld_v4_2_state_schema_interface_spec.md:885:event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./docs/planning/clanworld_v4_spec.md:750:Working bandit attack tiers:
./docs/planning/clanworld_v4_spec.md:757:Exact tier schedule may be tuned later, but intended direction is:
./docs/planning/clanworld_v4_spec.md:1206:- bandit attack tier schedule
./docs/reviews/pr194-codereview-codex-5-5.md:121:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:238:+              "name": "carryGold",
./docs/reviews/pr194-codereview-codex-5-5.md:254:-              "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:265:-              "name": "currentRegion",
./docs/reviews/pr194-codereview-codex-5-5.md:271:-              "name": "attackAttemptsMade",
./docs/reviews/pr194-codereview-codex-5-5.md:278:-              "name": "stateEnteredTick",
./docs/reviews/pr194-codereview-codex-5-5.md:286:-              "name": "nextActionTick",
./docs/reviews/pr194-codereview-codex-5-5.md:292:-              "name": "tier",
./docs/reviews/pr194-codereview-codex-5-5.md:297:-              "name": "attackPower",
./docs/reviews/pr194-codereview-codex-5-5.md:312:+              "name": "carryGold",
./docs/reviews/pr194-codereview-codex-5-5.md:475:+      "name": "BanditTargetDied",
./docs/reviews/pr194-codereview-codex-5-5.md:478:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:507:+      "name": "BlueprintEarned",
./docs/reviews/pr194-codereview-codex-5-5.md:516:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:539:+      "name": "ClansmanKilledByBandit",
./docs/reviews/pr194-codereview-codex-5-5.md:554:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:574:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:657:+      "name": "WallDamagedByBandit",
./docs/reviews/pr194-codereview-codex-5-5.md:672:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-5.md:899:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:900:+                if (banditId == excludedBanditId) continue;
./docs/reviews/pr194-codereview-codex-5-5.md:902:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:903:+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
./docs/reviews/pr194-codereview-codex-5-5.md:904:+                    _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:905:+                    emit BanditEscaped(banditId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:906:+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:939:+        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-5.md:1239:+                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:1416:+        if (cs.currentRegion != sim.clan.baseRegion) return _simulateCompleteMission(cs, m);
./docs/reviews/pr194-codereview-codex-5-5.md:1440:+        if (cs.currentRegion == sim.clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-5.md:1587:+            state: BanditState.Spawned,
./docs/reviews/pr194-codereview-codex-5-5.md:1595:+            carryGold: 0
./docs/reviews/pr194-codereview-codex-5-5.md:1614:+        _transitionBanditState(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:1620:+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-5.md:1625:+        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-5.md:1633:+        if (newState != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:1641:+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:1642:+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:1643:+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:1644:+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:1645:+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:1650:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:1651:+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
./docs/reviews/pr194-codereview-codex-5-5.md:1655:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:1656:+            || (newState == BanditState.Attacking
./docs/reviews/pr194-codereview-codex-5-5.md:1662:+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
./docs/reviews/pr194-codereview-codex-5-5.md:1666:+        return newState == BanditState.Resting;
./docs/reviews/pr194-codereview-codex-5-5.md:1670:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:1671:+            || (newState == BanditState.Camped
./docs/reviews/pr194-codereview-codex-5-5.md:1704:+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:1719:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:1720:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:1721:+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
./docs/reviews/pr194-codereview-codex-5-5.md:1722:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:1724:+                    bandit.state == BanditState.Resting
./docs/reviews/pr194-codereview-codex-5-5.md:1727:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:1744:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:1745:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:1746:+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
./docs/reviews/pr194-codereview-codex-5-5.md:1748:+                    _resolveBanditAttack(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:1749:+                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
./docs/reviews/pr194-codereview-codex-5-5.md:1758:+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:1761:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:1762:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:1772:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:1773:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:1782:+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:1791:+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
./docs/reviews/pr194-codereview-codex-5-5.md:1794:+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:1799:+            banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:1813:+            emit BanditDefeated(banditId, targetClanId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:1814:+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
./docs/reviews/pr194-codereview-codex-5-5.md:1816:+            emit BlueprintEarned(targetClanId, banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:1817:+            _transitionBanditState(banditId, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:1819:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:1820:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:1824:+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:1825:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:1839:+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
./docs/reviews/pr194-codereview-codex-5-5.md:1851:+        emit LootDistributed(
./docs/reviews/pr194-codereview-codex-5-5.md:1852:+            banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:1863:+            bandit.carryGold - (perGold * nDefendingClans)
./docs/reviews/pr194-codereview-codex-5-5.md:1911:+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:1920:+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:1941:+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:1944:+                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-5.md:1950:+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
./docs/reviews/pr194-codereview-codex-5-5.md:1965:+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:1987:+        uint32 banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:1994:+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:2005:+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:2009:+                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:2015:+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:2032:+            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
./docs/reviews/pr194-codereview-codex-5-5.md:2046:+    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)
./docs/reviews/pr194-codereview-codex-5-5.md:2052:+            uint256(keccak256(abi.encode("clansman_defense", tickSeed, banditId, clansmanId)))
./docs/reviews/pr194-codereview-codex-5-5.md:2108:-        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-5.md:2372:+                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
./docs/reviews/pr194-codereview-codex-5-5.md:2373:+                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
./docs/reviews/pr194-codereview-codex-5-5.md:2375:+                    weights[cs.currentRegion - 1] += 25;
./docs/reviews/pr194-codereview-codex-5-5.md:2510:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2512:-            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2513:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:2514:-            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2515:-            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2516:-            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2517:-            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2518:-            tier: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2519:-            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2525:+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2526:+        BanditTroop memory bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:2527:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:2531:+                state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:2539:+                carryGold: 0
./docs/reviews/pr194-codereview-codex-5-5.md:2545:+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2546:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:2549:+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2573:-        uint8 effectiveRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:2613:+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-5.md:2614:+        return _bandits[banditId].targetClanId;
./docs/reviews/pr194-codereview-codex-5-5.md:2656:+        return cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:2685:-            uint8 effRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:2748:+        uint64 nextActionTick = 0;
./docs/reviews/pr194-codereview-codex-5-5.md:2749:+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
./docs/reviews/pr194-codereview-codex-5-5.md:2751:+            if (bandit.state == BanditState.Spawned) {
./docs/reviews/pr194-codereview-codex-5-5.md:2752:+                nextActionTick = bandit.tickEnteredState + 1;
./docs/reviews/pr194-codereview-codex-5-5.md:2753:+            } else if (bandit.state == BanditState.Camped) {
./docs/reviews/pr194-codereview-codex-5-5.md:2754:+                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
./docs/reviews/pr194-codereview-codex-5-5.md:2755:+            } else if (bandit.state == BanditState.Resting) {
./docs/reviews/pr194-codereview-codex-5-5.md:2756:+                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS;
./docs/reviews/pr194-codereview-codex-5-5.md:2762:-            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2763:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:2764:-            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2766:+            banditId: bandit.id,
./docs/reviews/pr194-codereview-codex-5-5.md:2768:+            currentRegion: bandit.region,
./docs/reviews/pr194-codereview-codex-5-5.md:2769:             attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2771:-            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2772:-            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2773:+            stateEnteredTick: bandit.tickEnteredState,
./docs/reviews/pr194-codereview-codex-5-5.md:2774:+            nextActionTick: nextActionTick,
./docs/reviews/pr194-codereview-codex-5-5.md:2775:             tier: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2776:-            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2782:+            attackPower: _banditStrengthForLegacyEvent(bandit.strength),
./docs/reviews/pr194-codereview-codex-5-5.md:2799:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2800:+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2802:-            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2803:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:2804:-            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2805:-            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2806:-            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2807:-            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2808:-            tier: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2809:-            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2812:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:2821:+            carryGold: 0
./docs/reviews/pr194-codereview-codex-5-5.md:2825:+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2826:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:2829:+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:2839:             banditId: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2840:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:2841:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:2842:             currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2843:             attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-5.md:2888:-    uint32 banditId;
./docs/reviews/pr194-codereview-codex-5-5.md:2893:-    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:2894:-    uint8 attackAttemptsMade;
./docs/reviews/pr194-codereview-codex-5-5.md:2895:-    uint64 stateEnteredTick;
./docs/reviews/pr194-codereview-codex-5-5.md:2896:-    uint64 nextActionTick;
./docs/reviews/pr194-codereview-codex-5-5.md:2898:-    uint8 tier;
./docs/reviews/pr194-codereview-codex-5-5.md:2899:-    uint16 attackPower; // derived from tier; tier is canonical (v4.3 §G)
./docs/reviews/pr194-codereview-codex-5-5.md:2908:+    uint256 carryGold;
./docs/reviews/pr194-codereview-codex-5-5.md:2914:     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:2915:     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:2916:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:2917:+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:2918:+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:2919:     event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./docs/reviews/pr194-codereview-codex-5-5.md:2920:+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:2921:+    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-5.md:2922:+        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:2936:         uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:2942:+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:2944:     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:2946:+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
./docs/reviews/pr194-codereview-codex-5-5.md:3011:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3014:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:3019:+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-5.md:3028:+        BanditTroop memory bandit = world.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-5.md:3031:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-5.md:3037:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
./docs/reviews/pr194-codereview-codex-5-5.md:3045:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3046:+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:3055:+        assertEq(view_.banditId, id, "active bandit");
./docs/reviews/pr194-codereview-codex-5-5.md:3056:+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:3057:+        assertEq(view_.nextActionTick, world.getWorldState().currentTick + 1, "spawn delay tick");
./docs/reviews/pr194-codereview-codex-5-5.md:3066:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3067:+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:3075:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:3077:+        BanditTroop memory deletedBandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3080:+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
./docs/reviews/pr194-codereview-codex-5-5.md:3081:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
./docs/reviews/pr194-codereview-codex-5-5.md:3093:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:3096:+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
./docs/reviews/pr194-codereview-codex-5-5.md:3102:+        world.transitionBandit(id, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:3103:+        BanditTroop memory escaped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3104:+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:3107:+        world.transitionBandit(id, BanditState.Resting);
./docs/reviews/pr194-codereview-codex-5-5.md:3108:+        BanditTroop memory resting = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3109:+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
./docs/reviews/pr194-codereview-codex-5-5.md:3114:+        BanditTroop memory camped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:3115:+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
./docs/reviews/pr194-codereview-codex-5-5.md:3123:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:3129:+        world.transitionBandit(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:3132:+        world.transitionBandit(id, BanditState.None);
./docs/reviews/pr194-codereview-codex-5-5.md:3140:+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-5.md:3145:+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-5.md:3152:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:3154:+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-5.md:3187:+        _bandits[id].state = BanditState.Attacking;
./docs/reviews/pr194-codereview-codex-5-5.md:3209:+    function setBanditStrength(uint32 banditId, uint32 strength) external {
./docs/reviews/pr194-codereview-codex-5-5.md:3210:+        _bandits[banditId].strength = strength;
./docs/reviews/pr194-codereview-codex-5-5.md:3213:+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
./docs/reviews/pr194-codereview-codex-5-5.md:3216:+        _bandits[banditId].carryWood = wood;
./docs/reviews/pr194-codereview-codex-5-5.md:3217:+        _bandits[banditId].carryWheat = wheat;
./docs/reviews/pr194-codereview-codex-5-5.md:3218:+        _bandits[banditId].carryFish = fish;
./docs/reviews/pr194-codereview-codex-5-5.md:3219:+        _bandits[banditId].carryIron = iron;
./docs/reviews/pr194-codereview-codex-5-5.md:3220:+        _bandits[banditId].carryGold = gold;
./docs/reviews/pr194-codereview-codex-5-5.md:3223:+    function defenseRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId) external pure returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-5.md:3224:+        return _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-5.md:3233:+        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:3236:+        uint16 attackPower,
./docs/reviews/pr194-codereview-codex-5-5.md:3245:+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:3246:+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:3247:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:3248:+    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-5.md:3249:+        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:3338:+    function _assertBanditTargetDiedLog(
./docs/reviews/pr194-codereview-codex-5-5.md:3344:+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
./docs/reviews/pr194-codereview-codex-5-5.md:3356:+        fail("expected BanditTargetDied log");
./docs/reviews/pr194-codereview-codex-5-5.md:3376:+    function _forceAttack(uint32 clanId, uint32 strength) internal returns (uint32 banditId) {
./docs/reviews/pr194-codereview-codex-5-5.md:3395:+        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3396:+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
./docs/reviews/pr194-codereview-codex-5-5.md:3397:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:3407:+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-5.md:3417:+        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3418:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:3421:+        emit BlueprintEarned(clanId, banditId, world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:3450:+        uint32 banditId = _forceAttack(targetClanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:3456:+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
./docs/reviews/pr194-codereview-codex-5-5.md:3468:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:3469:+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
./docs/reviews/pr194-codereview-codex-5-5.md:3496:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3497:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:3498:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:3521:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3522:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:3561:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3562:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:3563:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:3566:+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:3568:+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:3570:+        emit LootDistributed(banditId, clanIds, 33e18, 33e18, 33e18, 33e18, 33e18, 1e18, 1e18, 1e18, 1e18, 1e18);
./docs/reviews/pr194-codereview-codex-5-5.md:3597:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3598:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:3599:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:3610:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:3611:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:3621:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:3627:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:3632:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:3640:+        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-5.md:3642:+        uint32 defense = world.defenseRoll(tickSeed, banditId, _csId(clanId, 0))
./docs/reviews/pr194-codereview-codex-5-5.md:3643:+            + world.defenseRoll(tickSeed, banditId, _csId(clanId, 1));
./docs/reviews/pr194-codereview-codex-5-5.md:3646:+        world.setBanditStrength(banditId, strength);
./docs/reviews/pr194-codereview-codex-5-5.md:3649:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-5.md:3657:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:3663:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:3668:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:3673:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:3699:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:3701:+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
./docs/reviews/pr194-codereview-codex-5-5.md:3727:+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-5.md:3728:+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-5.md:3820:+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-5.md:3956:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
./docs/reviews/pr194-codereview-codex-5-5.md:3985:+        uint8 selectedRegion = world.getBandit(1).region;
./docs/reviews/pr194-codereview-codex-5-5.md:4008:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
./docs/reviews/pr194-codereview-codex-5-5.md:4100:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:4101:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
./docs/reviews/pr194-codereview-codex-5-5.md:4124:+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
./docs/reviews/pr194-codereview-codex-5-5.md:4143:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:4145:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
./docs/reviews/pr194-codereview-codex-5-5.md:4201:+        assertEq(derived.clansman.currentRegion, ClanWorldConstants.REGION_MOUNTAINS, "derived current region");
./docs/reviews/pr194-codereview-codex-5-5.md:4207:+        assertEq(raw.currentRegion, view_.clan.clan.baseRegion, "raw current region unchanged");
./docs/reviews/pr194-codereview-codex-5-5.md:4287:+        BanditTroop memory bandit = stub.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-5.md:4290:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-5.md:4476:packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:4477:packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4480:packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4487:packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:4488:packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
./docs/reviews/pr194-codereview-codex-5-5.md:4490:packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:4491:packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4494:packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4506:packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:4507:packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4510:packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4517:packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:4518:packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
./docs/reviews/pr194-codereview-codex-5-5.md:4520:packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:4521:packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4524:packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:4832:   272	                    cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:5196:   261	    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:5231:   296	    uint32 banditId;
./docs/reviews/pr194-codereview-codex-5-5.md:5234:   299	    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:5235:   300	    uint8 attackAttemptsMade;
./docs/reviews/pr194-codereview-codex-5-5.md:5236:   301	    uint64 stateEnteredTick;
./docs/reviews/pr194-codereview-codex-5-5.md:5237:   302	    uint64 nextActionTick;
./docs/reviews/pr194-codereview-codex-5-5.md:5239:   304	    uint8 tier;
./docs/reviews/pr194-codereview-codex-5-5.md:5240:   305	    uint16 attackPower;            // derived from tier; tier is canonical (v4.3 §G)
./docs/reviews/pr194-codereview-codex-5-5.md:5303:docs/reviews/pr194-codereview-codex-5-5.md:475:+      "name": "BanditTargetDied",
./docs/reviews/pr194-codereview-codex-5-5.md:5304:docs/reviews/pr194-codereview-codex-5-5.md:539:+      "name": "ClansmanKilledByBandit",
./docs/reviews/pr194-codereview-codex-5-5.md:5305:docs/reviews/pr194-codereview-codex-5-5.md:657:+      "name": "WallDamagedByBandit",
./docs/reviews/pr194-codereview-codex-5-5.md:5325:docs/reviews/pr194-codereview-codex-5-5.md:899:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:5326:docs/reviews/pr194-codereview-codex-5-5.md:900:+                if (banditId == excludedBanditId) continue;
./docs/reviews/pr194-codereview-codex-5-5.md:5327:docs/reviews/pr194-codereview-codex-5-5.md:902:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:5328:docs/reviews/pr194-codereview-codex-5-5.md:903:+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
./docs/reviews/pr194-codereview-codex-5-5.md:5329:docs/reviews/pr194-codereview-codex-5-5.md:904:+                    _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:5330:docs/reviews/pr194-codereview-codex-5-5.md:905:+                    emit BanditEscaped(banditId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5331:docs/reviews/pr194-codereview-codex-5-5.md:906:+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5336:docs/reviews/pr194-codereview-codex-5-5.md:1239:+                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:5346:docs/reviews/pr194-codereview-codex-5-5.md:1587:+            state: BanditState.Spawned,
./docs/reviews/pr194-codereview-codex-5-5.md:5356:docs/reviews/pr194-codereview-codex-5-5.md:1614:+        _transitionBanditState(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:5359:docs/reviews/pr194-codereview-codex-5-5.md:1620:+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-5.md:5362:docs/reviews/pr194-codereview-codex-5-5.md:1625:+        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-5.md:5365:docs/reviews/pr194-codereview-codex-5-5.md:1633:+        if (newState != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:5369:docs/reviews/pr194-codereview-codex-5-5.md:1641:+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:5370:docs/reviews/pr194-codereview-codex-5-5.md:1642:+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:5371:docs/reviews/pr194-codereview-codex-5-5.md:1643:+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:5372:docs/reviews/pr194-codereview-codex-5-5.md:1644:+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:5373:docs/reviews/pr194-codereview-codex-5-5.md:1645:+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:5375:docs/reviews/pr194-codereview-codex-5-5.md:1650:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:5376:docs/reviews/pr194-codereview-codex-5-5.md:1651:+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
./docs/reviews/pr194-codereview-codex-5-5.md:5378:docs/reviews/pr194-codereview-codex-5-5.md:1655:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:5379:docs/reviews/pr194-codereview-codex-5-5.md:1656:+            || (newState == BanditState.Attacking
./docs/reviews/pr194-codereview-codex-5-5.md:5382:docs/reviews/pr194-codereview-codex-5-5.md:1662:+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
./docs/reviews/pr194-codereview-codex-5-5.md:5384:docs/reviews/pr194-codereview-codex-5-5.md:1666:+        return newState == BanditState.Resting;
./docs/reviews/pr194-codereview-codex-5-5.md:5386:docs/reviews/pr194-codereview-codex-5-5.md:1670:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:5387:docs/reviews/pr194-codereview-codex-5-5.md:1671:+            || (newState == BanditState.Camped
./docs/reviews/pr194-codereview-codex-5-5.md:5404:docs/reviews/pr194-codereview-codex-5-5.md:1704:+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:5410:docs/reviews/pr194-codereview-codex-5-5.md:1719:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:5411:docs/reviews/pr194-codereview-codex-5-5.md:1720:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:5412:docs/reviews/pr194-codereview-codex-5-5.md:1721:+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
./docs/reviews/pr194-codereview-codex-5-5.md:5413:docs/reviews/pr194-codereview-codex-5-5.md:1722:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:5414:docs/reviews/pr194-codereview-codex-5-5.md:1724:+                    bandit.state == BanditState.Resting
./docs/reviews/pr194-codereview-codex-5-5.md:5415:docs/reviews/pr194-codereview-codex-5-5.md:1727:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:5419:docs/reviews/pr194-codereview-codex-5-5.md:1744:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:5420:docs/reviews/pr194-codereview-codex-5-5.md:1745:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:5421:docs/reviews/pr194-codereview-codex-5-5.md:1746:+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
./docs/reviews/pr194-codereview-codex-5-5.md:5422:docs/reviews/pr194-codereview-codex-5-5.md:1748:+                    _resolveBanditAttack(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5423:docs/reviews/pr194-codereview-codex-5-5.md:1758:+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:5424:docs/reviews/pr194-codereview-codex-5-5.md:1761:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:5425:docs/reviews/pr194-codereview-codex-5-5.md:1762:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:5429:docs/reviews/pr194-codereview-codex-5-5.md:1772:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:5430:docs/reviews/pr194-codereview-codex-5-5.md:1773:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5434:docs/reviews/pr194-codereview-codex-5-5.md:1782:+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:5435:docs/reviews/pr194-codereview-codex-5-5.md:1791:+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
./docs/reviews/pr194-codereview-codex-5-5.md:5437:docs/reviews/pr194-codereview-codex-5-5.md:1794:+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:5441:docs/reviews/pr194-codereview-codex-5-5.md:1813:+            emit BanditDefeated(banditId, targetClanId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5442:docs/reviews/pr194-codereview-codex-5-5.md:1814:+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
./docs/reviews/pr194-codereview-codex-5-5.md:5444:docs/reviews/pr194-codereview-codex-5-5.md:1816:+            emit BlueprintEarned(targetClanId, banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5445:docs/reviews/pr194-codereview-codex-5-5.md:1817:+            _transitionBanditState(banditId, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:5446:docs/reviews/pr194-codereview-codex-5-5.md:1819:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:5447:docs/reviews/pr194-codereview-codex-5-5.md:1820:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5448:docs/reviews/pr194-codereview-codex-5-5.md:1824:+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:5449:docs/reviews/pr194-codereview-codex-5-5.md:1825:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:5455:docs/reviews/pr194-codereview-codex-5-5.md:1839:+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
./docs/reviews/pr194-codereview-codex-5-5.md:5465:docs/reviews/pr194-codereview-codex-5-5.md:1911:+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:5467:docs/reviews/pr194-codereview-codex-5-5.md:1920:+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:5470:docs/reviews/pr194-codereview-codex-5-5.md:1941:+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:5472:docs/reviews/pr194-codereview-codex-5-5.md:1950:+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
./docs/reviews/pr194-codereview-codex-5-5.md:5473:docs/reviews/pr194-codereview-codex-5-5.md:1965:+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:5476:docs/reviews/pr194-codereview-codex-5-5.md:1994:+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:5477:docs/reviews/pr194-codereview-codex-5-5.md:2005:+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:5478:docs/reviews/pr194-codereview-codex-5-5.md:2015:+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:5533:docs/reviews/pr194-codereview-codex-5-5.md:2510:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5535:docs/reviews/pr194-codereview-codex-5-5.md:2513:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5536:docs/reviews/pr194-codereview-codex-5-5.md:2525:+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5537:docs/reviews/pr194-codereview-codex-5-5.md:2526:+        BanditTroop memory bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:5538:docs/reviews/pr194-codereview-codex-5-5.md:2527:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:5540:docs/reviews/pr194-codereview-codex-5-5.md:2531:+                state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:5542:docs/reviews/pr194-codereview-codex-5-5.md:2545:+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5543:docs/reviews/pr194-codereview-codex-5-5.md:2546:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:5544:docs/reviews/pr194-codereview-codex-5-5.md:2549:+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5550:docs/reviews/pr194-codereview-codex-5-5.md:2613:+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-5.md:5551:docs/reviews/pr194-codereview-codex-5-5.md:2614:+        return _bandits[banditId].targetClanId;
./docs/reviews/pr194-codereview-codex-5-5.md:5564:docs/reviews/pr194-codereview-codex-5-5.md:2749:+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
./docs/reviews/pr194-codereview-codex-5-5.md:5565:docs/reviews/pr194-codereview-codex-5-5.md:2751:+            if (bandit.state == BanditState.Spawned) {
./docs/reviews/pr194-codereview-codex-5-5.md:5566:docs/reviews/pr194-codereview-codex-5-5.md:2753:+            } else if (bandit.state == BanditState.Camped) {
./docs/reviews/pr194-codereview-codex-5-5.md:5567:docs/reviews/pr194-codereview-codex-5-5.md:2755:+            } else if (bandit.state == BanditState.Resting) {
./docs/reviews/pr194-codereview-codex-5-5.md:5569:docs/reviews/pr194-codereview-codex-5-5.md:2763:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5571:docs/reviews/pr194-codereview-codex-5-5.md:2799:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5572:docs/reviews/pr194-codereview-codex-5-5.md:2800:+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5574:docs/reviews/pr194-codereview-codex-5-5.md:2803:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5575:docs/reviews/pr194-codereview-codex-5-5.md:2812:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:5577:docs/reviews/pr194-codereview-codex-5-5.md:2825:+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5578:docs/reviews/pr194-codereview-codex-5-5.md:2826:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:5579:docs/reviews/pr194-codereview-codex-5-5.md:2829:+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5581:docs/reviews/pr194-codereview-codex-5-5.md:2840:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5582:docs/reviews/pr194-codereview-codex-5-5.md:2841:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:5592:docs/reviews/pr194-codereview-codex-5-5.md:2914:     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5593:docs/reviews/pr194-codereview-codex-5-5.md:2915:     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5594:docs/reviews/pr194-codereview-codex-5-5.md:2916:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:5595:docs/reviews/pr194-codereview-codex-5-5.md:2917:+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:5596:docs/reviews/pr194-codereview-codex-5-5.md:2918:+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:5597:docs/reviews/pr194-codereview-codex-5-5.md:2942:+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:5598:docs/reviews/pr194-codereview-codex-5-5.md:2944:     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:5599:docs/reviews/pr194-codereview-codex-5-5.md:2946:+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
./docs/reviews/pr194-codereview-codex-5-5.md:5619:docs/reviews/pr194-codereview-codex-5-5.md:3011:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5620:docs/reviews/pr194-codereview-codex-5-5.md:3014:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:5622:docs/reviews/pr194-codereview-codex-5-5.md:3019:+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-5.md:5627:docs/reviews/pr194-codereview-codex-5-5.md:3028:+        BanditTroop memory bandit = world.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-5.md:5628:docs/reviews/pr194-codereview-codex-5-5.md:3031:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-5.md:5631:docs/reviews/pr194-codereview-codex-5-5.md:3037:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
./docs/reviews/pr194-codereview-codex-5-5.md:5633:docs/reviews/pr194-codereview-codex-5-5.md:3045:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5634:docs/reviews/pr194-codereview-codex-5-5.md:3046:+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:5638:docs/reviews/pr194-codereview-codex-5-5.md:3056:+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:5640:docs/reviews/pr194-codereview-codex-5-5.md:3066:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5641:docs/reviews/pr194-codereview-codex-5-5.md:3067:+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:5644:docs/reviews/pr194-codereview-codex-5-5.md:3075:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:5645:docs/reviews/pr194-codereview-codex-5-5.md:3077:+        BanditTroop memory deletedBandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5648:docs/reviews/pr194-codereview-codex-5-5.md:3080:+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
./docs/reviews/pr194-codereview-codex-5-5.md:5649:docs/reviews/pr194-codereview-codex-5-5.md:3081:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
./docs/reviews/pr194-codereview-codex-5-5.md:5656:docs/reviews/pr194-codereview-codex-5-5.md:3093:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:5658:docs/reviews/pr194-codereview-codex-5-5.md:3096:+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
./docs/reviews/pr194-codereview-codex-5-5.md:5659:docs/reviews/pr194-codereview-codex-5-5.md:3102:+        world.transitionBandit(id, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:5660:docs/reviews/pr194-codereview-codex-5-5.md:3103:+        BanditTroop memory escaped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5661:docs/reviews/pr194-codereview-codex-5-5.md:3104:+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:5663:docs/reviews/pr194-codereview-codex-5-5.md:3107:+        world.transitionBandit(id, BanditState.Resting);
./docs/reviews/pr194-codereview-codex-5-5.md:5664:docs/reviews/pr194-codereview-codex-5-5.md:3108:+        BanditTroop memory resting = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5665:docs/reviews/pr194-codereview-codex-5-5.md:3109:+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
./docs/reviews/pr194-codereview-codex-5-5.md:5666:docs/reviews/pr194-codereview-codex-5-5.md:3114:+        BanditTroop memory camped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:5667:docs/reviews/pr194-codereview-codex-5-5.md:3115:+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
./docs/reviews/pr194-codereview-codex-5-5.md:5670:docs/reviews/pr194-codereview-codex-5-5.md:3123:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:5671:docs/reviews/pr194-codereview-codex-5-5.md:3129:+        world.transitionBandit(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:5672:docs/reviews/pr194-codereview-codex-5-5.md:3132:+        world.transitionBandit(id, BanditState.None);
./docs/reviews/pr194-codereview-codex-5-5.md:5677:docs/reviews/pr194-codereview-codex-5-5.md:3140:+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-5.md:5681:docs/reviews/pr194-codereview-codex-5-5.md:3145:+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-5.md:5685:docs/reviews/pr194-codereview-codex-5-5.md:3152:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:5686:docs/reviews/pr194-codereview-codex-5-5.md:3154:+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-5.md:5695:docs/reviews/pr194-codereview-codex-5-5.md:3187:+        _bandits[id].state = BanditState.Attacking;
./docs/reviews/pr194-codereview-codex-5-5.md:5697:docs/reviews/pr194-codereview-codex-5-5.md:3209:+    function setBanditStrength(uint32 banditId, uint32 strength) external {
./docs/reviews/pr194-codereview-codex-5-5.md:5698:docs/reviews/pr194-codereview-codex-5-5.md:3213:+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
./docs/reviews/pr194-codereview-codex-5-5.md:5703:docs/reviews/pr194-codereview-codex-5-5.md:3245:+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5704:docs/reviews/pr194-codereview-codex-5-5.md:3247:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:5713:docs/reviews/pr194-codereview-codex-5-5.md:3338:+    function _assertBanditTargetDiedLog(
./docs/reviews/pr194-codereview-codex-5-5.md:5715:docs/reviews/pr194-codereview-codex-5-5.md:3344:+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
./docs/reviews/pr194-codereview-codex-5-5.md:5718:docs/reviews/pr194-codereview-codex-5-5.md:3356:+        fail("expected BanditTargetDied log");
./docs/reviews/pr194-codereview-codex-5-5.md:5723:docs/reviews/pr194-codereview-codex-5-5.md:3396:+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
./docs/reviews/pr194-codereview-codex-5-5.md:5724:docs/reviews/pr194-codereview-codex-5-5.md:3397:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:5725:docs/reviews/pr194-codereview-codex-5-5.md:3407:+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-5.md:5728:docs/reviews/pr194-codereview-codex-5-5.md:3418:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:5736:docs/reviews/pr194-codereview-codex-5-5.md:3450:+        uint32 banditId = _forceAttack(targetClanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:5739:docs/reviews/pr194-codereview-codex-5-5.md:3456:+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5744:docs/reviews/pr194-codereview-codex-5-5.md:3468:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:5745:docs/reviews/pr194-codereview-codex-5-5.md:3469:+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
./docs/reviews/pr194-codereview-codex-5-5.md:5747:docs/reviews/pr194-codereview-codex-5-5.md:3497:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:5748:docs/reviews/pr194-codereview-codex-5-5.md:3498:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:5751:docs/reviews/pr194-codereview-codex-5-5.md:3522:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:5762:docs/reviews/pr194-codereview-codex-5-5.md:3562:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:5763:docs/reviews/pr194-codereview-codex-5-5.md:3563:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:5764:docs/reviews/pr194-codereview-codex-5-5.md:3566:+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5765:docs/reviews/pr194-codereview-codex-5-5.md:3568:+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:5767:docs/reviews/pr194-codereview-codex-5-5.md:3598:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:5768:docs/reviews/pr194-codereview-codex-5-5.md:3599:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:5771:docs/reviews/pr194-codereview-codex-5-5.md:3611:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:5772:docs/reviews/pr194-codereview-codex-5-5.md:3621:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:5776:docs/reviews/pr194-codereview-codex-5-5.md:3632:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:5779:docs/reviews/pr194-codereview-codex-5-5.md:3646:+        world.setBanditStrength(banditId, strength);
./docs/reviews/pr194-codereview-codex-5-5.md:5780:docs/reviews/pr194-codereview-codex-5-5.md:3649:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-5.md:5781:docs/reviews/pr194-codereview-codex-5-5.md:3663:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:5782:docs/reviews/pr194-codereview-codex-5-5.md:3673:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:5788:docs/reviews/pr194-codereview-codex-5-5.md:3701:+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
./docs/reviews/pr194-codereview-codex-5-5.md:5795:docs/reviews/pr194-codereview-codex-5-5.md:3727:+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-5.md:5796:docs/reviews/pr194-codereview-codex-5-5.md:3728:+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-5.md:5820:docs/reviews/pr194-codereview-codex-5-5.md:3820:+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-5.md:5853:docs/reviews/pr194-codereview-codex-5-5.md:3956:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
./docs/reviews/pr194-codereview-codex-5-5.md:5860:docs/reviews/pr194-codereview-codex-5-5.md:3985:+        uint8 selectedRegion = world.getBandit(1).region;
./docs/reviews/pr194-codereview-codex-5-5.md:5867:docs/reviews/pr194-codereview-codex-5-5.md:4008:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
./docs/reviews/pr194-codereview-codex-5-5.md:5888:docs/reviews/pr194-codereview-codex-5-5.md:4100:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:5889:docs/reviews/pr194-codereview-codex-5-5.md:4101:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
./docs/reviews/pr194-codereview-codex-5-5.md:5899:docs/reviews/pr194-codereview-codex-5-5.md:4124:+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
./docs/reviews/pr194-codereview-codex-5-5.md:5903:docs/reviews/pr194-codereview-codex-5-5.md:4143:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:5904:docs/reviews/pr194-codereview-codex-5-5.md:4145:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
./docs/reviews/pr194-codereview-codex-5-5.md:5914:docs/reviews/pr194-codereview-codex-5-5.md:4287:+        BanditTroop memory bandit = stub.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-5.md:5915:docs/reviews/pr194-codereview-codex-5-5.md:4290:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-5.md:5932:docs/reviews/pr194-codereview-codex-5-5.md:4476:packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5933:docs/reviews/pr194-codereview-codex-5-5.md:4477:packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5936:docs/reviews/pr194-codereview-codex-5-5.md:4480:packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5943:docs/reviews/pr194-codereview-codex-5-5.md:4487:packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:5944:docs/reviews/pr194-codereview-codex-5-5.md:4488:packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
./docs/reviews/pr194-codereview-codex-5-5.md:5946:docs/reviews/pr194-codereview-codex-5-5.md:4490:packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5947:docs/reviews/pr194-codereview-codex-5-5.md:4491:packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5950:docs/reviews/pr194-codereview-codex-5-5.md:4494:packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5958:docs/reviews/pr194-codereview-codex-5-5.md:4506:packages/contracts/src/ClanWorld.sol:1464:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5959:docs/reviews/pr194-codereview-codex-5-5.md:4507:packages/contracts/src/ClanWorld.sol:1467:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5962:docs/reviews/pr194-codereview-codex-5-5.md:4510:packages/contracts/src/ClanWorld.sol:1713:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5969:docs/reviews/pr194-codereview-codex-5-5.md:4517:packages/contracts/src/IClanWorld.sol:735:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:5970:docs/reviews/pr194-codereview-codex-5-5.md:4518:packages/contracts/src/IClanWorld.sol:771:    function getBanditTargetPreview(uint32 banditId)
./docs/reviews/pr194-codereview-codex-5-5.md:5972:docs/reviews/pr194-codereview-codex-5-5.md:4520:packages/contracts/src/ClanWorldStub.sol:188:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:5973:docs/reviews/pr194-codereview-codex-5-5.md:4521:packages/contracts/src/ClanWorldStub.sol:191:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5976:docs/reviews/pr194-codereview-codex-5-5.md:4524:packages/contracts/src/ClanWorldStub.sol:315:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:5984:docs/reviews/pr194-codereview-codex-5-5.md:4832:   272	                    cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:6024:docs/reviews/pr194-codereview-codex-5-4.md:475:+      "name": "BanditTargetDied",
./docs/reviews/pr194-codereview-codex-5-5.md:6025:docs/reviews/pr194-codereview-codex-5-4.md:539:+      "name": "ClansmanKilledByBandit",
./docs/reviews/pr194-codereview-codex-5-5.md:6026:docs/reviews/pr194-codereview-codex-5-4.md:657:+      "name": "WallDamagedByBandit",
./docs/reviews/pr194-codereview-codex-5-5.md:6046:docs/reviews/pr194-codereview-codex-5-4.md:899:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:6047:docs/reviews/pr194-codereview-codex-5-4.md:900:+                if (banditId == excludedBanditId) continue;
./docs/reviews/pr194-codereview-codex-5-5.md:6048:docs/reviews/pr194-codereview-codex-5-4.md:902:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:6049:docs/reviews/pr194-codereview-codex-5-4.md:903:+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
./docs/reviews/pr194-codereview-codex-5-5.md:6050:docs/reviews/pr194-codereview-codex-5-4.md:904:+                    _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:6051:docs/reviews/pr194-codereview-codex-5-4.md:905:+                    emit BanditEscaped(banditId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6052:docs/reviews/pr194-codereview-codex-5-4.md:906:+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6057:docs/reviews/pr194-codereview-codex-5-4.md:1239:+                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:6067:docs/reviews/pr194-codereview-codex-5-4.md:1587:+            state: BanditState.Spawned,
./docs/reviews/pr194-codereview-codex-5-5.md:6077:docs/reviews/pr194-codereview-codex-5-4.md:1614:+        _transitionBanditState(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:6080:docs/reviews/pr194-codereview-codex-5-4.md:1620:+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-5.md:6083:docs/reviews/pr194-codereview-codex-5-4.md:1625:+        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-5.md:6086:docs/reviews/pr194-codereview-codex-5-4.md:1633:+        if (newState != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:6090:docs/reviews/pr194-codereview-codex-5-4.md:1641:+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:6091:docs/reviews/pr194-codereview-codex-5-4.md:1642:+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:6092:docs/reviews/pr194-codereview-codex-5-4.md:1643:+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:6093:docs/reviews/pr194-codereview-codex-5-4.md:1644:+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:6094:docs/reviews/pr194-codereview-codex-5-4.md:1645:+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:6096:docs/reviews/pr194-codereview-codex-5-4.md:1650:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:6097:docs/reviews/pr194-codereview-codex-5-4.md:1651:+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
./docs/reviews/pr194-codereview-codex-5-5.md:6099:docs/reviews/pr194-codereview-codex-5-4.md:1655:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:6100:docs/reviews/pr194-codereview-codex-5-4.md:1656:+            || (newState == BanditState.Attacking
./docs/reviews/pr194-codereview-codex-5-5.md:6103:docs/reviews/pr194-codereview-codex-5-4.md:1662:+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
./docs/reviews/pr194-codereview-codex-5-5.md:6105:docs/reviews/pr194-codereview-codex-5-4.md:1666:+        return newState == BanditState.Resting;
./docs/reviews/pr194-codereview-codex-5-5.md:6107:docs/reviews/pr194-codereview-codex-5-4.md:1670:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:6108:docs/reviews/pr194-codereview-codex-5-4.md:1671:+            || (newState == BanditState.Camped
./docs/reviews/pr194-codereview-codex-5-5.md:6125:docs/reviews/pr194-codereview-codex-5-4.md:1704:+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:6131:docs/reviews/pr194-codereview-codex-5-4.md:1719:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:6132:docs/reviews/pr194-codereview-codex-5-4.md:1720:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:6133:docs/reviews/pr194-codereview-codex-5-4.md:1721:+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
./docs/reviews/pr194-codereview-codex-5-5.md:6134:docs/reviews/pr194-codereview-codex-5-4.md:1722:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:6135:docs/reviews/pr194-codereview-codex-5-4.md:1724:+                    bandit.state == BanditState.Resting
./docs/reviews/pr194-codereview-codex-5-5.md:6136:docs/reviews/pr194-codereview-codex-5-4.md:1727:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:6140:docs/reviews/pr194-codereview-codex-5-4.md:1744:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:6141:docs/reviews/pr194-codereview-codex-5-4.md:1745:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:6142:docs/reviews/pr194-codereview-codex-5-4.md:1746:+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
./docs/reviews/pr194-codereview-codex-5-5.md:6143:docs/reviews/pr194-codereview-codex-5-4.md:1748:+                    _resolveBanditAttack(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6144:docs/reviews/pr194-codereview-codex-5-4.md:1758:+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:6145:docs/reviews/pr194-codereview-codex-5-4.md:1761:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:6146:docs/reviews/pr194-codereview-codex-5-4.md:1762:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:6150:docs/reviews/pr194-codereview-codex-5-4.md:1772:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:6151:docs/reviews/pr194-codereview-codex-5-4.md:1773:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6155:docs/reviews/pr194-codereview-codex-5-4.md:1782:+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:6156:docs/reviews/pr194-codereview-codex-5-4.md:1791:+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
./docs/reviews/pr194-codereview-codex-5-5.md:6158:docs/reviews/pr194-codereview-codex-5-4.md:1794:+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:6162:docs/reviews/pr194-codereview-codex-5-4.md:1813:+            emit BanditDefeated(banditId, targetClanId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6163:docs/reviews/pr194-codereview-codex-5-4.md:1814:+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
./docs/reviews/pr194-codereview-codex-5-5.md:6165:docs/reviews/pr194-codereview-codex-5-4.md:1816:+            emit BlueprintEarned(targetClanId, banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6166:docs/reviews/pr194-codereview-codex-5-4.md:1817:+            _transitionBanditState(banditId, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:6167:docs/reviews/pr194-codereview-codex-5-4.md:1819:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:6168:docs/reviews/pr194-codereview-codex-5-4.md:1820:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6169:docs/reviews/pr194-codereview-codex-5-4.md:1824:+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:6170:docs/reviews/pr194-codereview-codex-5-4.md:1825:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:6176:docs/reviews/pr194-codereview-codex-5-4.md:1839:+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
./docs/reviews/pr194-codereview-codex-5-5.md:6186:docs/reviews/pr194-codereview-codex-5-4.md:1911:+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:6188:docs/reviews/pr194-codereview-codex-5-4.md:1920:+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:6191:docs/reviews/pr194-codereview-codex-5-4.md:1941:+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:6193:docs/reviews/pr194-codereview-codex-5-4.md:1950:+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
./docs/reviews/pr194-codereview-codex-5-5.md:6194:docs/reviews/pr194-codereview-codex-5-4.md:1965:+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:6197:docs/reviews/pr194-codereview-codex-5-4.md:1994:+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:6198:docs/reviews/pr194-codereview-codex-5-4.md:2005:+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:6199:docs/reviews/pr194-codereview-codex-5-4.md:2015:+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:6254:docs/reviews/pr194-codereview-codex-5-4.md:2510:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6256:docs/reviews/pr194-codereview-codex-5-4.md:2513:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:6257:docs/reviews/pr194-codereview-codex-5-4.md:2525:+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6258:docs/reviews/pr194-codereview-codex-5-4.md:2526:+        BanditTroop memory bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:6259:docs/reviews/pr194-codereview-codex-5-4.md:2527:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:6261:docs/reviews/pr194-codereview-codex-5-4.md:2531:+                state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:6263:docs/reviews/pr194-codereview-codex-5-4.md:2545:+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6264:docs/reviews/pr194-codereview-codex-5-4.md:2546:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:6265:docs/reviews/pr194-codereview-codex-5-4.md:2549:+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6271:docs/reviews/pr194-codereview-codex-5-4.md:2613:+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-5.md:6272:docs/reviews/pr194-codereview-codex-5-4.md:2614:+        return _bandits[banditId].targetClanId;
./docs/reviews/pr194-codereview-codex-5-5.md:6285:docs/reviews/pr194-codereview-codex-5-4.md:2749:+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
./docs/reviews/pr194-codereview-codex-5-5.md:6286:docs/reviews/pr194-codereview-codex-5-4.md:2751:+            if (bandit.state == BanditState.Spawned) {
./docs/reviews/pr194-codereview-codex-5-5.md:6287:docs/reviews/pr194-codereview-codex-5-4.md:2753:+            } else if (bandit.state == BanditState.Camped) {
./docs/reviews/pr194-codereview-codex-5-5.md:6288:docs/reviews/pr194-codereview-codex-5-4.md:2755:+            } else if (bandit.state == BanditState.Resting) {
./docs/reviews/pr194-codereview-codex-5-5.md:6290:docs/reviews/pr194-codereview-codex-5-4.md:2763:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:6292:docs/reviews/pr194-codereview-codex-5-4.md:2799:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6293:docs/reviews/pr194-codereview-codex-5-4.md:2800:+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6295:docs/reviews/pr194-codereview-codex-5-4.md:2803:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:6296:docs/reviews/pr194-codereview-codex-5-4.md:2812:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:6298:docs/reviews/pr194-codereview-codex-5-4.md:2825:+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6299:docs/reviews/pr194-codereview-codex-5-4.md:2826:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:6300:docs/reviews/pr194-codereview-codex-5-4.md:2829:+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6302:docs/reviews/pr194-codereview-codex-5-4.md:2840:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:6303:docs/reviews/pr194-codereview-codex-5-4.md:2841:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:6313:docs/reviews/pr194-codereview-codex-5-4.md:2914:     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6314:docs/reviews/pr194-codereview-codex-5-4.md:2915:     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6315:docs/reviews/pr194-codereview-codex-5-4.md:2916:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:6316:docs/reviews/pr194-codereview-codex-5-4.md:2917:+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:6317:docs/reviews/pr194-codereview-codex-5-4.md:2918:+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:6318:docs/reviews/pr194-codereview-codex-5-4.md:2942:+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:6319:docs/reviews/pr194-codereview-codex-5-4.md:2944:     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:6320:docs/reviews/pr194-codereview-codex-5-4.md:2946:+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
./docs/reviews/pr194-codereview-codex-5-5.md:6340:docs/reviews/pr194-codereview-codex-5-4.md:3011:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6341:docs/reviews/pr194-codereview-codex-5-4.md:3014:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:6343:docs/reviews/pr194-codereview-codex-5-4.md:3019:+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-5.md:6348:docs/reviews/pr194-codereview-codex-5-4.md:3028:+        BanditTroop memory bandit = world.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-5.md:6349:docs/reviews/pr194-codereview-codex-5-4.md:3031:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-5.md:6352:docs/reviews/pr194-codereview-codex-5-4.md:3037:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
./docs/reviews/pr194-codereview-codex-5-5.md:6354:docs/reviews/pr194-codereview-codex-5-4.md:3045:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6355:docs/reviews/pr194-codereview-codex-5-4.md:3046:+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:6359:docs/reviews/pr194-codereview-codex-5-4.md:3056:+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:6361:docs/reviews/pr194-codereview-codex-5-4.md:3066:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6362:docs/reviews/pr194-codereview-codex-5-4.md:3067:+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
./docs/reviews/pr194-codereview-codex-5-5.md:6365:docs/reviews/pr194-codereview-codex-5-4.md:3075:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:6366:docs/reviews/pr194-codereview-codex-5-4.md:3077:+        BanditTroop memory deletedBandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6369:docs/reviews/pr194-codereview-codex-5-4.md:3080:+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
./docs/reviews/pr194-codereview-codex-5-5.md:6370:docs/reviews/pr194-codereview-codex-5-4.md:3081:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
./docs/reviews/pr194-codereview-codex-5-5.md:6377:docs/reviews/pr194-codereview-codex-5-4.md:3093:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:6379:docs/reviews/pr194-codereview-codex-5-4.md:3096:+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
./docs/reviews/pr194-codereview-codex-5-5.md:6380:docs/reviews/pr194-codereview-codex-5-4.md:3102:+        world.transitionBandit(id, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:6381:docs/reviews/pr194-codereview-codex-5-4.md:3103:+        BanditTroop memory escaped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6382:docs/reviews/pr194-codereview-codex-5-4.md:3104:+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:6384:docs/reviews/pr194-codereview-codex-5-4.md:3107:+        world.transitionBandit(id, BanditState.Resting);
./docs/reviews/pr194-codereview-codex-5-5.md:6385:docs/reviews/pr194-codereview-codex-5-4.md:3108:+        BanditTroop memory resting = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6386:docs/reviews/pr194-codereview-codex-5-4.md:3109:+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
./docs/reviews/pr194-codereview-codex-5-5.md:6387:docs/reviews/pr194-codereview-codex-5-4.md:3114:+        BanditTroop memory camped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-5.md:6388:docs/reviews/pr194-codereview-codex-5-4.md:3115:+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
./docs/reviews/pr194-codereview-codex-5-5.md:6391:docs/reviews/pr194-codereview-codex-5-4.md:3123:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:6392:docs/reviews/pr194-codereview-codex-5-4.md:3129:+        world.transitionBandit(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:6393:docs/reviews/pr194-codereview-codex-5-4.md:3132:+        world.transitionBandit(id, BanditState.None);
./docs/reviews/pr194-codereview-codex-5-5.md:6398:docs/reviews/pr194-codereview-codex-5-4.md:3140:+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-5.md:6402:docs/reviews/pr194-codereview-codex-5-4.md:3145:+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-5.md:6406:docs/reviews/pr194-codereview-codex-5-4.md:3152:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:6407:docs/reviews/pr194-codereview-codex-5-4.md:3154:+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-5.md:6416:docs/reviews/pr194-codereview-codex-5-4.md:3187:+        _bandits[id].state = BanditState.Attacking;
./docs/reviews/pr194-codereview-codex-5-5.md:6418:docs/reviews/pr194-codereview-codex-5-4.md:3209:+    function setBanditStrength(uint32 banditId, uint32 strength) external {
./docs/reviews/pr194-codereview-codex-5-5.md:6419:docs/reviews/pr194-codereview-codex-5-4.md:3213:+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
./docs/reviews/pr194-codereview-codex-5-5.md:6424:docs/reviews/pr194-codereview-codex-5-4.md:3245:+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6425:docs/reviews/pr194-codereview-codex-5-4.md:3247:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:6434:docs/reviews/pr194-codereview-codex-5-4.md:3338:+    function _assertBanditTargetDiedLog(
./docs/reviews/pr194-codereview-codex-5-5.md:6436:docs/reviews/pr194-codereview-codex-5-4.md:3344:+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
./docs/reviews/pr194-codereview-codex-5-5.md:6439:docs/reviews/pr194-codereview-codex-5-4.md:3356:+        fail("expected BanditTargetDied log");
./docs/reviews/pr194-codereview-codex-5-5.md:6444:docs/reviews/pr194-codereview-codex-5-4.md:3396:+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
./docs/reviews/pr194-codereview-codex-5-5.md:6445:docs/reviews/pr194-codereview-codex-5-4.md:3397:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:6446:docs/reviews/pr194-codereview-codex-5-4.md:3407:+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-5.md:6449:docs/reviews/pr194-codereview-codex-5-4.md:3418:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:6457:docs/reviews/pr194-codereview-codex-5-4.md:3450:+        uint32 banditId = _forceAttack(targetClanId, 100);
./docs/reviews/pr194-codereview-codex-5-5.md:6460:docs/reviews/pr194-codereview-codex-5-4.md:3456:+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6465:docs/reviews/pr194-codereview-codex-5-4.md:3468:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:6466:docs/reviews/pr194-codereview-codex-5-4.md:3469:+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
./docs/reviews/pr194-codereview-codex-5-5.md:6468:docs/reviews/pr194-codereview-codex-5-4.md:3497:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:6469:docs/reviews/pr194-codereview-codex-5-4.md:3498:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:6472:docs/reviews/pr194-codereview-codex-5-4.md:3522:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:6483:docs/reviews/pr194-codereview-codex-5-4.md:3562:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:6484:docs/reviews/pr194-codereview-codex-5-4.md:3563:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:6485:docs/reviews/pr194-codereview-codex-5-4.md:3566:+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6486:docs/reviews/pr194-codereview-codex-5-4.md:3568:+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6488:docs/reviews/pr194-codereview-codex-5-4.md:3598:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:6489:docs/reviews/pr194-codereview-codex-5-4.md:3599:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-5.md:6492:docs/reviews/pr194-codereview-codex-5-4.md:3611:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-5.md:6493:docs/reviews/pr194-codereview-codex-5-4.md:3621:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:6497:docs/reviews/pr194-codereview-codex-5-4.md:3632:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:6500:docs/reviews/pr194-codereview-codex-5-4.md:3646:+        world.setBanditStrength(banditId, strength);
./docs/reviews/pr194-codereview-codex-5-5.md:6501:docs/reviews/pr194-codereview-codex-5-4.md:3649:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-5.md:6502:docs/reviews/pr194-codereview-codex-5-4.md:3663:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:6503:docs/reviews/pr194-codereview-codex-5-4.md:3673:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-5.md:6509:docs/reviews/pr194-codereview-codex-5-4.md:3701:+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
./docs/reviews/pr194-codereview-codex-5-5.md:6516:docs/reviews/pr194-codereview-codex-5-4.md:3727:+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-5.md:6517:docs/reviews/pr194-codereview-codex-5-4.md:3728:+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-5.md:6541:docs/reviews/pr194-codereview-codex-5-4.md:3820:+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-5.md:6574:docs/reviews/pr194-codereview-codex-5-4.md:3956:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
./docs/reviews/pr194-codereview-codex-5-5.md:6581:docs/reviews/pr194-codereview-codex-5-4.md:3985:+        uint8 selectedRegion = world.getBandit(1).region;
./docs/reviews/pr194-codereview-codex-5-5.md:6588:docs/reviews/pr194-codereview-codex-5-4.md:4008:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
./docs/reviews/pr194-codereview-codex-5-5.md:6609:docs/reviews/pr194-codereview-codex-5-4.md:4100:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:6610:docs/reviews/pr194-codereview-codex-5-4.md:4101:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
./docs/reviews/pr194-codereview-codex-5-5.md:6620:docs/reviews/pr194-codereview-codex-5-4.md:4124:+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
./docs/reviews/pr194-codereview-codex-5-5.md:6624:docs/reviews/pr194-codereview-codex-5-4.md:4143:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:6625:docs/reviews/pr194-codereview-codex-5-4.md:4145:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
./docs/reviews/pr194-codereview-codex-5-5.md:6635:docs/reviews/pr194-codereview-codex-5-4.md:4287:+        BanditTroop memory bandit = stub.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-5.md:6636:docs/reviews/pr194-codereview-codex-5-4.md:4290:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-5.md:6690:docs/reviews/pr194-codereview-codex-5-4.md:5085:                    cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:6728:docs/reviews/pr194-codereview-codex-5-4.md:6133:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:6730:docs/reviews/pr194-codereview-codex-5-4.md:6136:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:6754:docs/reviews/pr194-codereview-codex-5-4.md:6387:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-5.md:6769:docs/reviews/pr194-codereview-codex-5-4.md:6817:    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-5.md:6773:docs/reviews/pr194-codereview-codex-5-4.md:6825:    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6776:docs/reviews/pr194-codereview-codex-5-4.md:6839:    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6777:docs/reviews/pr194-codereview-codex-5-4.md:6840:    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:6781:docs/reviews/pr194-codereview-codex-5-4.md:6962:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:6785:docs/reviews/pr194-codereview-codex-5-4.md:6998:    function getBanditTargetPreview(uint32 banditId)
./docs/reviews/pr194-codereview-codex-5-5.md:6791:/bin/bash -lc "rg -n \"getBanditTroop|getBandit\\(|BanditState|BanditTroop|activeBandit|BlueprintEarned|LootDistributed|WallDamagedByBandit|ClansmanKilledByBandit|BanditTargetDied\" apps packages/shared packages/agents packages/runner packages/orchestrator -g '"'!packages/contracts/abi/IClanWorld.json'"'" in /home/claude/code/clan-world
./docs/reviews/pr194-codereview-codex-5-5.md:6824:   372	                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:7276:  1179	                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:7382:  1560	        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-5.md:7387:  1565	        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-5.md:7395:  1573	        if (newState != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:7403:  1581	        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:7404:  1582	        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:7405:  1583	        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:7406:  1584	        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
./docs/reviews/pr194-codereview-codex-5-5.md:7407:  1585	        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-5.md:7412:  1590	        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:7413:  1591	            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
./docs/reviews/pr194-codereview-codex-5-5.md:7417:  1595	        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:7418:  1596	            || (newState == BanditState.Attacking
./docs/reviews/pr194-codereview-codex-5-5.md:7424:  1602	        return newState == BanditState.Defeated || newState == BanditState.Escaped;
./docs/reviews/pr194-codereview-codex-5-5.md:7428:  1606	        return newState == BanditState.Resting;
./docs/reviews/pr194-codereview-codex-5-5.md:7432:  1610	        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-5.md:7433:  1611	            || (newState == BanditState.Camped
./docs/reviews/pr194-codereview-codex-5-5.md:7466:  1644	                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:7481:  1659	                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:7482:  1660	                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:7483:  1661	                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
./docs/reviews/pr194-codereview-codex-5-5.md:7484:  1662	                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:7486:  1664	                    bandit.state == BanditState.Resting
./docs/reviews/pr194-codereview-codex-5-5.md:7489:  1667	                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-5.md:7506:  1684	                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:7507:  1685	                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:7508:  1686	                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
./docs/reviews/pr194-codereview-codex-5-5.md:7510:  1688	                    _resolveBanditAttack(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:7511:  1689	                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
./docs/reviews/pr194-codereview-codex-5-5.md:7520:  1698	    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:7523:  1701	        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:7524:  1702	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-5.md:7534:  1712	            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:7535:  1713	            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:7544:  1722	        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:7553:  1731	            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
./docs/reviews/pr194-codereview-codex-5-5.md:7556:  1734	                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:7561:  1739	            banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:7575:  1753	            emit BanditDefeated(banditId, targetClanId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:7576:  1754	            _distributeBanditLootToDefendingClans(banditId, targetClanId);
./docs/reviews/pr194-codereview-codex-5-5.md:7578:  1756	            emit BlueprintEarned(targetClanId, banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:7579:  1757	            _transitionBanditState(banditId, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-5.md:7581:  1759	            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:7582:  1760	            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-5.md:7586:  1764	    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
./docs/reviews/pr194-codereview-codex-5-5.md:7587:  1765	        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:7601:  1779	            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
./docs/reviews/pr194-codereview-codex-5-5.md:7613:  1791	        emit LootDistributed(
./docs/reviews/pr194-codereview-codex-5-5.md:7614:  1792	            banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:7625:  1803	            bandit.carryGold - (perGold * nDefendingClans)
./docs/reviews/pr194-codereview-codex-5-5.md:7673:  1851	                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:7682:  1860	    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:7703:  1881	                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-5.md:7706:  1884	                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-5.md:7712:  1890	    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
./docs/reviews/pr194-codereview-codex-5-5.md:7727:  1905	            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:7749:  1927	        uint32 banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:7756:  1934	            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
./docs/reviews/pr194-codereview-codex-5-5.md:7767:  1945	            emit ClansmanKilledByBandit(clanId, victimId, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:7771:  1949	                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:7777:  1955	    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-5.md:7794:  1972	            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
./docs/reviews/pr194-codereview-codex-5-5.md:7808:  1986	    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)
./docs/reviews/pr194-codereview-codex-5-5.md:7814:  1992	            uint256(keccak256(abi.encode("clansman_defense", tickSeed, banditId, clansmanId)))
./docs/reviews/pr194-codereview-codex-5-5.md:8175:   583	                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-5.md:8176:   584	                if (banditId == excludedBanditId) continue;
./docs/reviews/pr194-codereview-codex-5-5.md:8178:   586	                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:8179:   587	                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
./docs/reviews/pr194-codereview-codex-5-5.md:8180:   588	                    _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-5.md:8181:   589	                    emit BanditEscaped(banditId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:8182:   590	                    emit BanditTargetDied(banditId, deadClanId, currentTick);
./docs/reviews/pr194-codereview-codex-5-5.md:8234:  1527	            state: BanditState.Spawned,
./docs/reviews/pr194-codereview-codex-5-5.md:8242:  1535	            carryGold: 0
./docs/reviews/pr194-codereview-codex-5-5.md:8261:  1554	        _transitionBanditState(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-5.md:8267:  1560	        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-5.md:8272:  1565	        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-5.md:8340:  2183	                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
./docs/reviews/pr194-codereview-codex-5-5.md:8341:  2184	                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
./docs/reviews/pr194-codereview-codex-5-5.md:8343:  2186	                    weights[cs.currentRegion - 1] += 25;
./docs/reviews/pr194-codereview-codex-5-5.md:8646:   210	        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
./docs/reviews/pr194-codereview-codex-5-5.md:8675:   239	        uint8 selectedRegion = world.getBandit(1).region;
./docs/reviews/pr194-codereview-codex-5-5.md:8698:   262	        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
./docs/reviews/pr194-codereview-codex-5-5.md:8790:   354	        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-5.md:8791:   355	        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
./docs/reviews/pr194-codereview-codex-5-5.md:8814:   378	        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
./docs/reviews/pr194-codereview-codex-5-5.md:8966:   314	    uint256 carryGold;
./docs/reviews/pr194-codereview-codex-5-5.md:8976:   598	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:8977:   599	    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-5.md:8978:   600	    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:8979:   601	    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:8980:   602	    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-5.md:8981:   603	    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./docs/reviews/pr194-codereview-codex-5-5.md:8982:   604	    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-5.md:8983:   605	    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-5.md:8984:   606	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:8998:   620	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-5.md:9008:   734	    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:9010:   736	    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-5.md:9012:   738	    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
./docs/reviews/pr194-codereview-codex-5-5.md:9144:  2537	        ctx.fromRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:9191:  2584	            cs.currentRegion = ctx.gotoRegion;
./docs/reviews/pr194-codereview-codex-5-5.md:9375:  3180	    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-5.md:9376:  3181	        BanditTroop memory bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-5.md:9377:  3182	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-5.md:9381:  3186	                state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-5.md:9389:  3194	                carryGold: 0
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:167:    uint32 banditId;
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:169:    uint8  currentRegion;
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:170:    uint8  attackAttemptsMade;
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:172:    uint64 stateEnteredTick;
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:173:    uint64 nextActionTick;
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:174:    uint8  tier;
./docs/planning/clanworld_v4_4_ui_indexer_getters.md:175:    uint16 attackPower;
./docs/reviews/pr194-codereview-codex-5-4.md:121:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:238:+              "name": "carryGold",
./docs/reviews/pr194-codereview-codex-5-4.md:254:-              "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:265:-              "name": "currentRegion",
./docs/reviews/pr194-codereview-codex-5-4.md:271:-              "name": "attackAttemptsMade",
./docs/reviews/pr194-codereview-codex-5-4.md:278:-              "name": "stateEnteredTick",
./docs/reviews/pr194-codereview-codex-5-4.md:286:-              "name": "nextActionTick",
./docs/reviews/pr194-codereview-codex-5-4.md:292:-              "name": "tier",
./docs/reviews/pr194-codereview-codex-5-4.md:297:-              "name": "attackPower",
./docs/reviews/pr194-codereview-codex-5-4.md:312:+              "name": "carryGold",
./docs/reviews/pr194-codereview-codex-5-4.md:475:+      "name": "BanditTargetDied",
./docs/reviews/pr194-codereview-codex-5-4.md:478:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:507:+      "name": "BlueprintEarned",
./docs/reviews/pr194-codereview-codex-5-4.md:516:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:539:+      "name": "ClansmanKilledByBandit",
./docs/reviews/pr194-codereview-codex-5-4.md:554:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:574:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:657:+      "name": "WallDamagedByBandit",
./docs/reviews/pr194-codereview-codex-5-4.md:672:+          "name": "banditId",
./docs/reviews/pr194-codereview-codex-5-4.md:899:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-4.md:900:+                if (banditId == excludedBanditId) continue;
./docs/reviews/pr194-codereview-codex-5-4.md:902:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:903:+                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
./docs/reviews/pr194-codereview-codex-5-4.md:904:+                    _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:905:+                    emit BanditEscaped(banditId, currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:906:+                    emit BanditTargetDied(banditId, deadClanId, currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:939:+        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:1239:+                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:1416:+        if (cs.currentRegion != sim.clan.baseRegion) return _simulateCompleteMission(cs, m);
./docs/reviews/pr194-codereview-codex-5-4.md:1440:+        if (cs.currentRegion == sim.clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:1587:+            state: BanditState.Spawned,
./docs/reviews/pr194-codereview-codex-5-4.md:1595:+            carryGold: 0
./docs/reviews/pr194-codereview-codex-5-4.md:1614:+        _transitionBanditState(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-4.md:1620:+        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-4.md:1625:+        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-4.md:1633:+        if (newState != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-4.md:1641:+        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-4.md:1642:+        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-4.md:1643:+        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
./docs/reviews/pr194-codereview-codex-5-4.md:1644:+        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
./docs/reviews/pr194-codereview-codex-5-4.md:1645:+        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-4.md:1650:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-4.md:1651:+            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
./docs/reviews/pr194-codereview-codex-5-4.md:1655:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-4.md:1656:+            || (newState == BanditState.Attacking
./docs/reviews/pr194-codereview-codex-5-4.md:1662:+        return newState == BanditState.Defeated || newState == BanditState.Escaped;
./docs/reviews/pr194-codereview-codex-5-4.md:1666:+        return newState == BanditState.Resting;
./docs/reviews/pr194-codereview-codex-5-4.md:1670:+        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-4.md:1671:+            || (newState == BanditState.Camped
./docs/reviews/pr194-codereview-codex-5-4.md:1704:+                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-4.md:1719:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-4.md:1720:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:1721:+                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
./docs/reviews/pr194-codereview-codex-5-4.md:1722:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-4.md:1724:+                    bandit.state == BanditState.Resting
./docs/reviews/pr194-codereview-codex-5-4.md:1727:+                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-4.md:1744:+                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-4.md:1745:+                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:1746:+                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
./docs/reviews/pr194-codereview-codex-5-4.md:1748:+                    _resolveBanditAttack(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:1749:+                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
./docs/reviews/pr194-codereview-codex-5-4.md:1758:+    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
./docs/reviews/pr194-codereview-codex-5-4.md:1761:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:1762:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-4.md:1772:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:1773:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:1782:+        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
./docs/reviews/pr194-codereview-codex-5-4.md:1791:+            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
./docs/reviews/pr194-codereview-codex-5-4.md:1794:+                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
./docs/reviews/pr194-codereview-codex-5-4.md:1799:+            banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:1813:+            emit BanditDefeated(banditId, targetClanId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:1814:+            _distributeBanditLootToDefendingClans(banditId, targetClanId);
./docs/reviews/pr194-codereview-codex-5-4.md:1816:+            emit BlueprintEarned(targetClanId, banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:1817:+            _transitionBanditState(banditId, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-4.md:1819:+            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:1820:+            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:1824:+    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
./docs/reviews/pr194-codereview-codex-5-4.md:1825:+        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:1839:+            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
./docs/reviews/pr194-codereview-codex-5-4.md:1851:+        emit LootDistributed(
./docs/reviews/pr194-codereview-codex-5-4.md:1852:+            banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:1863:+            bandit.carryGold - (perGold * nDefendingClans)
./docs/reviews/pr194-codereview-codex-5-4.md:1911:+                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-4.md:1920:+    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-4.md:1941:+                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-4.md:1944:+                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-4.md:1950:+    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
./docs/reviews/pr194-codereview-codex-5-4.md:1965:+            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:1987:+        uint32 banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:1994:+            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
./docs/reviews/pr194-codereview-codex-5-4.md:2005:+            emit ClansmanKilledByBandit(clanId, victimId, banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:2009:+                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:2015:+    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-4.md:2032:+            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
./docs/reviews/pr194-codereview-codex-5-4.md:2046:+    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)
./docs/reviews/pr194-codereview-codex-5-4.md:2052:+            uint256(keccak256(abi.encode("clansman_defense", tickSeed, banditId, clansmanId)))
./docs/reviews/pr194-codereview-codex-5-4.md:2108:-        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:2372:+                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
./docs/reviews/pr194-codereview-codex-5-4.md:2373:+                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
./docs/reviews/pr194-codereview-codex-5-4.md:2375:+                    weights[cs.currentRegion - 1] += 25;
./docs/reviews/pr194-codereview-codex-5-4.md:2510:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2512:-            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2513:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-4.md:2514:-            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2515:-            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2516:-            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2517:-            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2518:-            tier: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2519:-            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2525:+    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2526:+        BanditTroop memory bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:2527:+        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-4.md:2531:+                state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-4.md:2539:+                carryGold: 0
./docs/reviews/pr194-codereview-codex-5-4.md:2545:+    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2546:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:2549:+    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2573:-        uint8 effectiveRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:2613:+    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-4.md:2614:+        return _bandits[banditId].targetClanId;
./docs/reviews/pr194-codereview-codex-5-4.md:2656:+        return cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:2685:-            uint8 effRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:2748:+        uint64 nextActionTick = 0;
./docs/reviews/pr194-codereview-codex-5-4.md:2749:+        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
./docs/reviews/pr194-codereview-codex-5-4.md:2751:+            if (bandit.state == BanditState.Spawned) {
./docs/reviews/pr194-codereview-codex-5-4.md:2752:+                nextActionTick = bandit.tickEnteredState + 1;
./docs/reviews/pr194-codereview-codex-5-4.md:2753:+            } else if (bandit.state == BanditState.Camped) {
./docs/reviews/pr194-codereview-codex-5-4.md:2754:+                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
./docs/reviews/pr194-codereview-codex-5-4.md:2755:+            } else if (bandit.state == BanditState.Resting) {
./docs/reviews/pr194-codereview-codex-5-4.md:2756:+                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS;
./docs/reviews/pr194-codereview-codex-5-4.md:2762:-            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2763:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-4.md:2764:-            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2766:+            banditId: bandit.id,
./docs/reviews/pr194-codereview-codex-5-4.md:2768:+            currentRegion: bandit.region,
./docs/reviews/pr194-codereview-codex-5-4.md:2769:             attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2771:-            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2772:-            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2773:+            stateEnteredTick: bandit.tickEnteredState,
./docs/reviews/pr194-codereview-codex-5-4.md:2774:+            nextActionTick: nextActionTick,
./docs/reviews/pr194-codereview-codex-5-4.md:2775:             tier: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2776:-            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2782:+            attackPower: _banditStrengthForLegacyEvent(bandit.strength),
./docs/reviews/pr194-codereview-codex-5-4.md:2799:-    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2800:+    function getBandit(uint32) public pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2802:-            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2803:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-4.md:2804:-            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2805:-            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2806:-            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2807:-            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2808:-            tier: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2809:-            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2812:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-4.md:2821:+            carryGold: 0
./docs/reviews/pr194-codereview-codex-5-4.md:2825:+    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2826:+        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:2829:+    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:2839:             banditId: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2840:-            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-4.md:2841:+            state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-4.md:2842:             currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2843:             attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:2888:-    uint32 banditId;
./docs/reviews/pr194-codereview-codex-5-4.md:2893:-    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:2894:-    uint8 attackAttemptsMade;
./docs/reviews/pr194-codereview-codex-5-4.md:2895:-    uint64 stateEnteredTick;
./docs/reviews/pr194-codereview-codex-5-4.md:2896:-    uint64 nextActionTick;
./docs/reviews/pr194-codereview-codex-5-4.md:2898:-    uint8 tier;
./docs/reviews/pr194-codereview-codex-5-4.md:2899:-    uint16 attackPower; // derived from tier; tier is canonical (v4.3 §G)
./docs/reviews/pr194-codereview-codex-5-4.md:2908:+    uint256 carryGold;
./docs/reviews/pr194-codereview-codex-5-4.md:2914:     event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:2915:     event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:2916:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:2917:+    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:2918:+    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:2919:     event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./docs/reviews/pr194-codereview-codex-5-4.md:2920:+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:2921:+    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-4.md:2922:+        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:2936:         uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:2942:+    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-4.md:2944:     function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-4.md:2946:+    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
./docs/reviews/pr194-codereview-codex-5-4.md:3011:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3014:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-4.md:3019:+        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-4.md:3028:+        BanditTroop memory bandit = world.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-4.md:3031:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-4.md:3037:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
./docs/reviews/pr194-codereview-codex-5-4.md:3045:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3046:+        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
./docs/reviews/pr194-codereview-codex-5-4.md:3055:+        assertEq(view_.banditId, id, "active bandit");
./docs/reviews/pr194-codereview-codex-5-4.md:3056:+        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
./docs/reviews/pr194-codereview-codex-5-4.md:3057:+        assertEq(view_.nextActionTick, world.getWorldState().currentTick + 1, "spawn delay tick");
./docs/reviews/pr194-codereview-codex-5-4.md:3066:+        BanditTroop memory bandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3067:+        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
./docs/reviews/pr194-codereview-codex-5-4.md:3075:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-4.md:3077:+        BanditTroop memory deletedBandit = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3080:+        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
./docs/reviews/pr194-codereview-codex-5-4.md:3081:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
./docs/reviews/pr194-codereview-codex-5-4.md:3093:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-4.md:3096:+        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
./docs/reviews/pr194-codereview-codex-5-4.md:3102:+        world.transitionBandit(id, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:3103:+        BanditTroop memory escaped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3104:+        assertEq(uint8(escaped.state), uint8(BanditState.Escaped), "escaped");
./docs/reviews/pr194-codereview-codex-5-4.md:3107:+        world.transitionBandit(id, BanditState.Resting);
./docs/reviews/pr194-codereview-codex-5-4.md:3108:+        BanditTroop memory resting = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3109:+        assertEq(uint8(resting.state), uint8(BanditState.Resting), "resting");
./docs/reviews/pr194-codereview-codex-5-4.md:3114:+        BanditTroop memory camped = world.getBandit(id);
./docs/reviews/pr194-codereview-codex-5-4.md:3115:+        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");
./docs/reviews/pr194-codereview-codex-5-4.md:3123:+        world.transitionBandit(id, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-4.md:3129:+        world.transitionBandit(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-4.md:3132:+        world.transitionBandit(id, BanditState.None);
./docs/reviews/pr194-codereview-codex-5-4.md:3140:+        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-4.md:3145:+        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
./docs/reviews/pr194-codereview-codex-5-4.md:3152:+        world.transitionBandit(id1, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-4.md:3154:+        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
./docs/reviews/pr194-codereview-codex-5-4.md:3187:+        _bandits[id].state = BanditState.Attacking;
./docs/reviews/pr194-codereview-codex-5-4.md:3209:+    function setBanditStrength(uint32 banditId, uint32 strength) external {
./docs/reviews/pr194-codereview-codex-5-4.md:3210:+        _bandits[banditId].strength = strength;
./docs/reviews/pr194-codereview-codex-5-4.md:3213:+    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
./docs/reviews/pr194-codereview-codex-5-4.md:3216:+        _bandits[banditId].carryWood = wood;
./docs/reviews/pr194-codereview-codex-5-4.md:3217:+        _bandits[banditId].carryWheat = wheat;
./docs/reviews/pr194-codereview-codex-5-4.md:3218:+        _bandits[banditId].carryFish = fish;
./docs/reviews/pr194-codereview-codex-5-4.md:3219:+        _bandits[banditId].carryIron = iron;
./docs/reviews/pr194-codereview-codex-5-4.md:3220:+        _bandits[banditId].carryGold = gold;
./docs/reviews/pr194-codereview-codex-5-4.md:3223:+    function defenseRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId) external pure returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-4.md:3224:+        return _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-4.md:3233:+        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:3236:+        uint16 attackPower,
./docs/reviews/pr194-codereview-codex-5-4.md:3245:+    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:3246:+    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:3247:+    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:3248:+    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-4.md:3249:+        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:3338:+    function _assertBanditTargetDiedLog(
./docs/reviews/pr194-codereview-codex-5-4.md:3344:+        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
./docs/reviews/pr194-codereview-codex-5-4.md:3356:+        fail("expected BanditTargetDied log");
./docs/reviews/pr194-codereview-codex-5-4.md:3376:+    function _forceAttack(uint32 clanId, uint32 strength) internal returns (uint32 banditId) {
./docs/reviews/pr194-codereview-codex-5-4.md:3395:+        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3396:+        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
./docs/reviews/pr194-codereview-codex-5-4.md:3397:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:3407:+        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-4.md:3417:+        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3418:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:3421:+        emit BlueprintEarned(clanId, banditId, world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:3450:+        uint32 banditId = _forceAttack(targetClanId, 100);
./docs/reviews/pr194-codereview-codex-5-4.md:3456:+        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
./docs/reviews/pr194-codereview-codex-5-4.md:3468:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-4.md:3469:+        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
./docs/reviews/pr194-codereview-codex-5-4.md:3496:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3497:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-4.md:3498:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:3521:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3522:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:3561:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3562:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-4.md:3563:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:3566:+        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:3568:+        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:3570:+        emit LootDistributed(banditId, clanIds, 33e18, 33e18, 33e18, 33e18, 33e18, 1e18, 1e18, 1e18, 1e18, 1e18);
./docs/reviews/pr194-codereview-codex-5-4.md:3597:+        uint32 banditId = _forceAttack(clanIds[0], 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3598:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-4.md:3599:+        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:3610:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-4.md:3611:+        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
./docs/reviews/pr194-codereview-codex-5-4.md:3621:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-4.md:3627:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-4.md:3632:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-4.md:3640:+        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-4.md:3642:+        uint32 defense = world.defenseRoll(tickSeed, banditId, _csId(clanId, 0))
./docs/reviews/pr194-codereview-codex-5-4.md:3643:+            + world.defenseRoll(tickSeed, banditId, _csId(clanId, 1));
./docs/reviews/pr194-codereview-codex-5-4.md:3646:+        world.setBanditStrength(banditId, strength);
./docs/reviews/pr194-codereview-codex-5-4.md:3649:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-4.md:3657:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-4.md:3663:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-4.md:3668:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-4.md:3673:+        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
./docs/reviews/pr194-codereview-codex-5-4.md:3699:+        uint32 banditId = _forceAttack(clanId, 100);
./docs/reviews/pr194-codereview-codex-5-4.md:3701:+            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
./docs/reviews/pr194-codereview-codex-5-4.md:3727:+        assertEq(uint8(a.getBandit(1).state), uint8(b.getBandit(1).state), "first bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-4.md:3728:+        assertEq(uint8(a.getBandit(2).state), uint8(b.getBandit(2).state), "second bandit state deterministic");
./docs/reviews/pr194-codereview-codex-5-4.md:3820:+    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-4.md:3956:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
./docs/reviews/pr194-codereview-codex-5-4.md:3985:+        uint8 selectedRegion = world.getBandit(1).region;
./docs/reviews/pr194-codereview-codex-5-4.md:4008:+        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
./docs/reviews/pr194-codereview-codex-5-4.md:4100:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-4.md:4101:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "bandit spawned");
./docs/reviews/pr194-codereview-codex-5-4.md:4124:+        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
./docs/reviews/pr194-codereview-codex-5-4.md:4143:+        BanditTroop memory bandit = world.getBandit(1);
./docs/reviews/pr194-codereview-codex-5-4.md:4145:+        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "spawned state");
./docs/reviews/pr194-codereview-codex-5-4.md:4201:+        assertEq(derived.clansman.currentRegion, ClanWorldConstants.REGION_MOUNTAINS, "derived current region");
./docs/reviews/pr194-codereview-codex-5-4.md:4207:+        assertEq(raw.currentRegion, view_.clan.clan.baseRegion, "raw current region unchanged");
./docs/reviews/pr194-codereview-codex-5-4.md:4287:+        BanditTroop memory bandit = stub.getBandit(999);
./docs/reviews/pr194-codereview-codex-5-4.md:4290:+        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
./docs/reviews/pr194-codereview-codex-5-4.md:5085:                    cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:5394:        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:5432:        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:5637:            cs.currentRegion = baseRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:5731:        ctx.fromRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:5791:            cs.currentRegion = ctx.gotoRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:6028:            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN &&
./docs/reviews/pr194-codereview-codex-5-4.md:6133:    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:6135:            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6136:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-4.md:6137:            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6138:            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6139:            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6140:            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6141:            tier: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6142:            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6209:        uint8 effectiveRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:6327:            uint8 effRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:6386:            banditId: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6387:            state: BanditState.NONE,
./docs/reviews/pr194-codereview-codex-5-4.md:6388:            currentRegion: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6389:            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6391:            stateEnteredTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6392:            nextActionTick: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6393:            tier: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6394:            attackPower: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:6424:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
./docs/reviews/pr194-codereview-codex-5-4.md:6438:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
./docs/reviews/pr194-codereview-codex-5-4.md:6478:    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:6513:    uint32 banditId;
./docs/reviews/pr194-codereview-codex-5-4.md:6516:    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:6517:    uint8 attackAttemptsMade;
./docs/reviews/pr194-codereview-codex-5-4.md:6518:    uint64 stateEnteredTick;
./docs/reviews/pr194-codereview-codex-5-4.md:6519:    uint64 nextActionTick;
./docs/reviews/pr194-codereview-codex-5-4.md:6521:    uint8 tier;
./docs/reviews/pr194-codereview-codex-5-4.md:6522:    uint16 attackPower;            // derived from tier; tier is canonical (v4.3 §G)
./docs/reviews/pr194-codereview-codex-5-4.md:6672:    uint32 banditId;
./docs/reviews/pr194-codereview-codex-5-4.md:6674:    uint8  currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:6675:    uint8  attackAttemptsMade;
./docs/reviews/pr194-codereview-codex-5-4.md:6677:    uint64 stateEnteredTick;
./docs/reviews/pr194-codereview-codex-5-4.md:6678:    uint64 nextActionTick;
./docs/reviews/pr194-codereview-codex-5-4.md:6679:    uint8  tier;
./docs/reviews/pr194-codereview-codex-5-4.md:6680:    uint16 attackPower;
./docs/reviews/pr194-codereview-codex-5-4.md:6817:    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-4.md:6819:        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:6825:    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:6827:        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:6830:        uint16 attackPower,
./docs/reviews/pr194-codereview-codex-5-4.md:6839:    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:6840:    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:6841:    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./docs/reviews/pr194-codereview-codex-5-4.md:6843:        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:6962:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-4.md:6998:    function getBanditTargetPreview(uint32 banditId)
./docs/reviews/pr194-codereview-codex-5-4.md:7396:   372	                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:7612:   583	                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-4.md:7613:   584	                if (banditId == excludedBanditId) continue;
./docs/reviews/pr194-codereview-codex-5-4.md:7615:   586	                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:7616:   587	                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
./docs/reviews/pr194-codereview-codex-5-4.md:7617:   588	                    _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:7618:   589	                    emit BanditEscaped(banditId, currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:7619:   590	                    emit BanditTargetDied(banditId, deadClanId, currentTick);
./docs/reviews/pr194-codereview-codex-5-4.md:7870:   841	        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:7908:   879	        if (cs.currentRegion != clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:8213:  1179	                cs.currentRegion = m.targetRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:8390:  1356	        if (cs.currentRegion != sim.clan.baseRegion) return _simulateCompleteMission(cs, m);
./docs/reviews/pr194-codereview-codex-5-4.md:8414:  1380	        if (cs.currentRegion == sim.clan.baseRegion) {
./docs/reviews/pr194-codereview-codex-5-4.md:8561:  1527	            state: BanditState.Spawned,
./docs/reviews/pr194-codereview-codex-5-4.md:8569:  1535	            carryGold: 0
./docs/reviews/pr194-codereview-codex-5-4.md:8588:  1554	        _transitionBanditState(id, BanditState.Attacking);
./docs/reviews/pr194-codereview-codex-5-4.md:8594:  1560	        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
./docs/reviews/pr194-codereview-codex-5-4.md:8599:  1565	        if (newState == BanditState.Defeated) {
./docs/reviews/pr194-codereview-codex-5-4.md:8607:  1573	        if (newState != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-4.md:8615:  1581	        if (bandit.state == BanditState.Spawned) return _canBanditLeaveSpawned(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-4.md:8616:  1582	        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-4.md:8617:  1583	        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
./docs/reviews/pr194-codereview-codex-5-4.md:8618:  1584	        if (bandit.state == BanditState.Escaped) return _canBanditLeaveEscaped(newState);
./docs/reviews/pr194-codereview-codex-5-4.md:8619:  1585	        if (bandit.state == BanditState.Resting) return _canBanditLeaveResting(bandit, newState);
./docs/reviews/pr194-codereview-codex-5-4.md:8624:  1590	        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-4.md:8625:  1591	            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
./docs/reviews/pr194-codereview-codex-5-4.md:8629:  1595	        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-4.md:8630:  1596	            || (newState == BanditState.Attacking
./docs/reviews/pr194-codereview-codex-5-4.md:8636:  1602	        return newState == BanditState.Defeated || newState == BanditState.Escaped;
./docs/reviews/pr194-codereview-codex-5-4.md:8640:  1606	        return newState == BanditState.Resting;
./docs/reviews/pr194-codereview-codex-5-4.md:8644:  1610	        return newState == BanditState.Escaped
./docs/reviews/pr194-codereview-codex-5-4.md:8645:  1611	            || (newState == BanditState.Camped
./docs/reviews/pr194-codereview-codex-5-4.md:8678:  1644	                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-4.md:8693:  1659	                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-4.md:8694:  1660	                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:8695:  1661	                if (bandit.state == BanditState.Spawned && closedTick > bandit.tickEnteredState) {
./docs/reviews/pr194-codereview-codex-5-4.md:8696:  1662	                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-4.md:8698:  1664	                    bandit.state == BanditState.Resting
./docs/reviews/pr194-codereview-codex-5-4.md:8701:  1667	                    _transitionBanditState(banditId, BanditState.Camped);
./docs/reviews/pr194-codereview-codex-5-4.md:8718:  1684	                uint32 banditId = regionBandits[i];
./docs/reviews/pr194-codereview-codex-5-4.md:8719:  1685	                BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:8720:  1686	                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
./docs/reviews/pr194-codereview-codex-5-4.md:8722:  1688	                    _resolveBanditAttack(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:8723:  1689	                    if (_bandits[banditId].id == ClanWorldConstants.BANDIT_ID_NULL) {
./docs/reviews/pr194-codereview-codex-5-4.md:8732:  1698	    function _resolveBanditAttack(uint32 banditId, uint64 closedTick) internal {
./docs/reviews/pr194-codereview-codex-5-4.md:8740:  1701	        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:8741:  1702	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
./docs/reviews/pr194-codereview-codex-5-4.md:8751:  1712	            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:8752:  1713	            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:8761:  1722	        uint32 totalClansmanDefense = _totalBanditClansmanDefense(banditId, targetClanId, tickSeed);
./docs/reviews/pr194-codereview-codex-5-4.md:8770:  1731	            (incomingDamage, wallDamage) = _applyBanditWallDamage(targetClan, targetClanId, banditId, incomingDamage);
./docs/reviews/pr194-codereview-codex-5-4.md:8773:  1734	                _applyBanditClansmanCasualties(targetClan, targetClanId, banditId, incomingDamage, tickSeed);
./docs/reviews/pr194-codereview-codex-5-4.md:8778:  1739	            banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:8792:  1753	            emit BanditDefeated(banditId, targetClanId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:8793:  1754	            _distributeBanditLootToDefendingClans(banditId, targetClanId);
./docs/reviews/pr194-codereview-codex-5-4.md:8795:  1756	            emit BlueprintEarned(targetClanId, banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:8796:  1757	            _transitionBanditState(banditId, BanditState.Defeated);
./docs/reviews/pr194-codereview-codex-5-4.md:8798:  1759	            _transitionBanditState(banditId, BanditState.Escaped);
./docs/reviews/pr194-codereview-codex-5-4.md:8799:  1760	            emit BanditEscaped(banditId, closedTick);
./docs/reviews/pr194-codereview-codex-5-4.md:8803:  1764	    function _distributeBanditLootToDefendingClans(uint32 banditId, uint32 targetClanId) internal {
./docs/reviews/pr194-codereview-codex-5-4.md:8804:  1765	        BanditTroop storage bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:8818:  1779	            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
./docs/reviews/pr194-codereview-codex-5-4.md:8830:  1791	        emit LootDistributed(
./docs/reviews/pr194-codereview-codex-5-4.md:8831:  1792	            banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:8842:  1803	            bandit.carryGold - (perGold * nDefendingClans)
./docs/reviews/pr194-codereview-codex-5-4.md:8890:  1851	                cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-4.md:8899:  1860	    function _totalBanditClansmanDefense(uint32 banditId, uint32 targetClanId, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-4.md:8920:  1881	                    cs.state == ClansmanState.ACTING && cs.currentRegion == targetRegion && mission.active
./docs/reviews/pr194-codereview-codex-5-4.md:8923:  1884	                    totalDefense += _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-4.md:8929:  1890	    function _applyBanditWallDamage(Clan storage clan, uint32 clanId, uint32 banditId, uint32 incomingDamage)
./docs/reviews/pr194-codereview-codex-5-4.md:8944:  1905	            emit WallDamagedByBandit(clanId, clan.wallLevel, banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:8966:  1927	        uint32 banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:8973:  1934	            uint32 victimId = _pickBanditClansmanVictim(clanId, banditId, killIndex, tickSeed);
./docs/reviews/pr194-codereview-codex-5-4.md:8984:  1945	            emit ClansmanKilledByBandit(clanId, victimId, banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:8988:  1949	                _markClanDead(clanId, "bandit", _world.currentTick, banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:8994:  1955	    function _pickBanditClansmanVictim(uint32 clanId, uint32 banditId, uint32 killIndex, bytes32 tickSeed)
./docs/reviews/pr194-codereview-codex-5-4.md:9011:  1972	            uint256(keccak256(abi.encode("bandit_clansman_kill", tickSeed, banditId, clanId, killIndex))) % livingCount;
./docs/reviews/pr194-codereview-codex-5-4.md:9025:  1986	    function _clansmanDefenseDamageRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId)
./docs/reviews/pr194-codereview-codex-5-4.md:9031:  1992	            uint256(keccak256(abi.encode("clansman_defense", tickSeed, banditId, clansmanId)))
./docs/reviews/pr194-codereview-codex-5-4.md:9222:  2183	                    cs.state != ClansmanState.DEAD && cs.currentRegion >= ClanWorldConstants.REGION_FOREST
./docs/reviews/pr194-codereview-codex-5-4.md:9223:  2184	                        && cs.currentRegion <= ClanWorldConstants.REGION_DEEP_SEA
./docs/reviews/pr194-codereview-codex-5-4.md:9225:  2186	                    weights[cs.currentRegion - 1] += 25;
./docs/reviews/pr194-codereview-codex-5-4.md:9464:  2420	            cs.currentRegion = baseRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:9581:  2537	        ctx.fromRegion = cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:9628:  2584	            cs.currentRegion = ctx.gotoRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:10076:  3032	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
./docs/reviews/pr194-codereview-codex-5-4.md:10229:  3180	    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:10230:  3181	        BanditTroop memory bandit = _bandits[banditId];
./docs/reviews/pr194-codereview-codex-5-4.md:10231:  3182	        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
./docs/reviews/pr194-codereview-codex-5-4.md:10235:  3186	                state: BanditState.None,
./docs/reviews/pr194-codereview-codex-5-4.md:10243:  3194	                carryGold: 0
./docs/reviews/pr194-codereview-codex-5-4.md:10249:  3200	    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:10250:  3201	        return getBandit(banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:10253:  3204	    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
./docs/reviews/pr194-codereview-codex-5-4.md:10348:  3299	    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-4.md:10349:  3300	        return _bandits[banditId].targetClanId;
./docs/reviews/pr194-codereview-codex-5-4.md:10407:  3358	        return cs.currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:10524:  3475	        uint64 nextActionTick = 0;
./docs/reviews/pr194-codereview-codex-5-4.md:10525:  3476	        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
./docs/reviews/pr194-codereview-codex-5-4.md:10527:  3478	            if (bandit.state == BanditState.Spawned) {
./docs/reviews/pr194-codereview-codex-5-4.md:10528:  3479	                nextActionTick = bandit.tickEnteredState + 1;
./docs/reviews/pr194-codereview-codex-5-4.md:10529:  3480	            } else if (bandit.state == BanditState.Camped) {
./docs/reviews/pr194-codereview-codex-5-4.md:10530:  3481	                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
./docs/reviews/pr194-codereview-codex-5-4.md:10531:  3482	            } else if (bandit.state == BanditState.Resting) {
./docs/reviews/pr194-codereview-codex-5-4.md:10532:  3483	                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS;
./docs/reviews/pr194-codereview-codex-5-4.md:10538:  3489	            banditId: bandit.id,
./docs/reviews/pr194-codereview-codex-5-4.md:10540:  3491	            currentRegion: bandit.region,
./docs/reviews/pr194-codereview-codex-5-4.md:10541:  3492	            attackAttemptsMade: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:10543:  3494	            stateEnteredTick: bandit.tickEnteredState,
./docs/reviews/pr194-codereview-codex-5-4.md:10544:  3495	            nextActionTick: nextActionTick,
./docs/reviews/pr194-codereview-codex-5-4.md:10545:  3496	            tier: 0,
./docs/reviews/pr194-codereview-codex-5-4.md:10546:  3497	            attackPower: _banditStrengthForLegacyEvent(bandit.strength),
./docs/reviews/pr194-codereview-codex-5-4.md:10566:  3517	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
./docs/reviews/pr194-codereview-codex-5-4.md:10580:  3531	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
./docs/reviews/pr194-codereview-codex-5-4.md:10781:   265	    uint8 currentRegion;
./docs/reviews/pr194-codereview-codex-5-4.md:10830:   314	    uint256 carryGold;
./docs/reviews/pr194-codereview-codex-5-4.md:10961:   580	    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
./docs/reviews/pr194-codereview-codex-5-4.md:10963:   582	        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
./docs/reviews/pr194-codereview-codex-5-4.md:10965:   584	    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:10967:   586	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:10970:   589	        uint16 attackPower,
./docs/reviews/pr194-codereview-codex-5-4.md:10979:   598	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:10980:   599	    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:10981:   600	    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:10982:   601	    event WallDamagedByBandit(uint32 indexed clanId, uint8 newLevel, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:10983:   602	    event ClansmanKilledByBandit(uint32 indexed clanId, uint32 indexed clansmanId, uint32 indexed banditId);
./docs/reviews/pr194-codereview-codex-5-4.md:10984:   603	    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
./docs/reviews/pr194-codereview-codex-5-4.md:10985:   604	    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:10986:   605	    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-4.md:10987:   606	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:11001:   620	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:11115:   734	    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-4.md:11117:   736	    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
./docs/reviews/pr194-codereview-codex-5-4.md:11119:   738	    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
./docs/reviews/pr194-codereview-codex-5-4.md:11142:   761	    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
./docs/reviews/pr194-codereview-codex-5-4.md:11188:    23	        _bandits[id].state = BanditState.Attacking;
./docs/reviews/pr194-codereview-codex-5-4.md:11210:    45	    function setBanditStrength(uint32 banditId, uint32 strength) external {
./docs/reviews/pr194-codereview-codex-5-4.md:11211:    46	        _bandits[banditId].strength = strength;
./docs/reviews/pr194-codereview-codex-5-4.md:11214:    49	    function setBanditCarry(uint32 banditId, uint256 wood, uint256 wheat, uint256 fish, uint256 iron, uint256 gold)
./docs/reviews/pr194-codereview-codex-5-4.md:11217:    52	        _bandits[banditId].carryWood = wood;
./docs/reviews/pr194-codereview-codex-5-4.md:11218:    53	        _bandits[banditId].carryWheat = wheat;
./docs/reviews/pr194-codereview-codex-5-4.md:11219:    54	        _bandits[banditId].carryFish = fish;
./docs/reviews/pr194-codereview-codex-5-4.md:11220:    55	        _bandits[banditId].carryIron = iron;
./docs/reviews/pr194-codereview-codex-5-4.md:11221:    56	        _bandits[banditId].carryGold = gold;
./docs/reviews/pr194-codereview-codex-5-4.md:11224:    59	    function defenseRoll(bytes32 tickSeed, uint32 banditId, uint32 clansmanId) external pure returns (uint32) {
./docs/reviews/pr194-codereview-codex-5-4.md:11225:    60	        return _clansmanDefenseDamageRoll(tickSeed, banditId, clansmanId);
./docs/reviews/pr194-codereview-codex-5-4.md:11234:    69	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:11237:    72	        uint16 attackPower,
./docs/reviews/pr194-codereview-codex-5-4.md:11246:    81	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
./docs/reviews/pr194-codereview-codex-5-4.md:11247:    82	    event BlueprintEarned(uint32 indexed clanId, uint32 indexed banditId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:11248:    83	    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
./docs/reviews/pr194-codereview-codex-5-4.md:11249:    84	    event LootDistributed(
./docs/reviews/pr194-codereview-codex-5-4.md:11250:    85	        uint32 indexed banditId,
./docs/reviews/pr194-codereview-codex-5-4.md:11339:   174	    function _assertBanditTargetDiedLog(
./docs/reviews/pr194-codereview-codex-5-4.md:11345:   180	        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
./docs/reviews/pr194-codereview-codex-5-4.md:11357:   192	        fail("expected BanditTargetDied log");
./docs/reviews/pr194-codereview-codex-5-4.md:11377:   212	    function _forceAttack(uint32 clanId, uint32 strength) internal returns (uint32 banditId) {
./docs/reviews/pr194-codereview-codex-5-4.md:11396:   231	        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-4.md:11397:   232	        world.setBanditCarry(banditId, 7e18, 11e18, 13e18, 17e18, 19e18);
./docs/reviews/pr194-codereview-codex-5-4.md:11398:   233	        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:11408:   243	        assertEq(world.getBandit(banditId).id, 0, "defeated bandit deleted");
./docs/reviews/pr194-codereview-codex-5-4.md:11418:   253	        uint32 banditId = _forceAttack(clanId, 1);
./docs/reviews/pr194-codereview-codex-5-4.md:11419:   254	        world.setBanditStrength(banditId, 0);
./docs/reviews/pr194-codereview-codex-5-4.md:11422:   257	        emit BlueprintEarned(clanId, banditId, world.getWorldState().currentTick);

exec
/bin/bash -lc 'git show 23f7f1a''^:packages/contracts/test/BanditSpawn.t.sol | nl -ba | sed -n '"'1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// SPDX-License-Identifier: MIT
     2	pragma solidity ^0.8.34;
     3	
     4	import {Test} from "forge-std/Test.sol";
     5	import {ClanWorld} from "../src/ClanWorld.sol";
     6	import {
     7	    ActionType,
     8	    BanditState,
     9	    BanditTroop,
    10	    Clan,
    11	    ClanOrder,
    12	    ClanWorldConstants,
    13	    ClansmanState,
    14	    Mission,
    15	    OrderResult,
    16	    StatusCode,
    17	    WorldState
    18	} from "../src/IClanWorld.sol";
    19	
    20	contract BanditSpawnHarness is ClanWorld {
    21	    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
    22	        return _spawnBandit(region, strength);
    23	    }
    24	
    25	    function evaluateBanditSpawns(bytes32 tickSeed) external {
    26	        _evaluateBanditSpawns(tickSeed);
    27	    }
    28	
    29	    function setBanditSpawnState(uint8 region, uint64 lastSpawnTick, uint16 probabilityAccum) external {
    30	        _banditSpawnByRegion[region].lastSpawnTick = lastSpawnTick;
    31	        _banditSpawnByRegion[region].probabilityAccum = probabilityAccum;
    32	    }
    33	
    34	    function getBanditSpawnState(uint8 region) external view returns (uint64 lastSpawnTick, uint16 probabilityAccum) {
    35	        lastSpawnTick = _banditSpawnByRegion[region].lastSpawnTick;
    36	        probabilityAccum = _banditSpawnByRegion[region].probabilityAccum;
    37	    }
    38	
    39	    function activeBanditCount() external view returns (uint32) {
    40	        return _activeBanditCount;
    41	    }
    42	
    43	    function minSpawnCooldownTicks() external pure returns (uint64) {
    44	        return MIN_SPAWN_COOLDOWN_TICKS;
    45	    }
    46	
    47	    function spawnProbabilityIncrementBps() external pure returns (uint16) {
    48	        return BANDIT_SPAWN_PROBABILITY_INCREMENT_BPS;
    49	    }
    50	
    51	    function maxBanditsPerRegion() external pure returns (uint8) {
    52	        return MAX_BANDITS_PER_REGION;
    53	    }
    54	
    55	    function maxTotalBandits() external pure returns (uint8) {
    56	        return MAX_TOTAL_BANDITS;
    57	    }
    58	
    59	    function maxBanditSpawnScanPerRegion() external pure returns (uint256) {
    60	        return MAX_BANDIT_SPAWN_SCAN_PER_REGION;
    61	    }
    62	
    63	    function maxBanditEagerSettleBaseScanPerRegion() external pure returns (uint256) {
    64	        return MAX_BANDIT_EAGER_SETTLE_BASE_SCAN_PER_REGION;
    65	    }
    66	
    67	    function banditSpawnRoll(bytes32 tickSeed, uint8 region) external pure returns (uint256) {
    68	        return _banditSpawnRoll(tickSeed, region);
    69	    }
    70	}
    71	
    72	contract BanditSpawnTest is Test {
    73	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
    74	    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
    75	
    76	    BanditSpawnHarness world;
    77	
    78	    function setUp() public {
    79	        world = new BanditSpawnHarness();
    80	    }
    81	
    82	    function _advanceTick(BanditSpawnHarness target) internal {
    83	        vm.warp(target.getWorldState().nextHeartbeatAtTs);
    84	        target.heartbeat();
    85	    }
    86	
    87	    function _advanceTicks(BanditSpawnHarness target, uint64 ticks) internal {
    88	        for (uint64 i = 0; i < ticks; i++) {
    89	            _advanceTick(target);
    90	        }
    91	    }
    92	
    93	    function _advancePastInitialCooldown(BanditSpawnHarness target) internal {
    94	        _advanceTicks(target, target.minSpawnCooldownTicks());
    95	    }
    96	
    97	    function _mintForestClan(BanditSpawnHarness target) internal {
    98	        target.mintClan(address(this));
    99	    }
   100	
   101	    function _mintUntilTwoForestClans(BanditSpawnHarness target) internal returns (uint32 first, uint32 second) {
   102	        for (uint256 i = 0; i < target.maxBanditEagerSettleBaseScanPerRegion(); i++) {
   103	            (uint32 clanId,) = target.mintClan(address(this));
   104	            if (target.getClan(clanId).baseRegion == ClanWorldConstants.REGION_FOREST) {
   105	                if (first == 0) {
   106	                    first = clanId;
   107	                } else {
   108	                    second = clanId;
   109	                    return (first, second);
   110	                }
   111	            }
   112	        }
   113	        revert("missing second forest clan");
   114	    }
   115	
   116	    function _csId(BanditSpawnHarness target, uint32 clanId, uint256 index) internal view returns (uint32) {
   117	        return target.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
   118	    }
   119	
   120	    function _ordersForPendingWorkerAndDefender(uint32 workerId, uint32 defenderId, uint8 baseRegion)
   121	        internal
   122	        pure
   123	        returns (ClanOrder[] memory orders)
   124	    {
   125	        orders = new ClanOrder[](2);
   126	        orders[0] = ClanOrder({
   127	            clansmanId: workerId,
   128	            gotoRegion: ClanWorldConstants.REGION_FOREST,
   129	            action: ActionType.ChopWood,
   130	            targetClanId: 0,
   131	            marketToken: address(0),
   132	            marketAmount: 0,
   133	            maxGoldIn: 0
   134	        });
   135	        orders[1] = ClanOrder({
   136	            clansmanId: defenderId,
   137	            gotoRegion: baseRegion,
   138	            action: ActionType.DefendBase,
   139	            targetClanId: 0,
   140	            marketToken: address(0),
   141	            marketAmount: 0,
   142	            maxGoldIn: 0
   143	        });
   144	    }
   145	
   146	    function _submitPendingWorkerAndDefender(BanditSpawnHarness target, uint32 clanId)
   147	        internal
   148	        returns (uint32 workerId, uint32 defenderId)
   149	    {
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
   191	    function test_cooldownEnforcedAfterSpawn() public {
   192	        _mintForestClan(world);
   193	        world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
   194	
   195	        _advanceTicks(world, world.minSpawnCooldownTicks() - 1);
   196	
   197	        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
   198	        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
   199	        assertEq(probabilityAccum, 0, "cooldown does not accumulate chance");
   200	    }
   201	
   202	    function test_probabilityRisesAfterCooldown() public {
   203	        _advancePastInitialCooldown(world);
   204	        _mintForestClan(world);
   205	
   206	        uint16 increment = world.spawnProbabilityIncrementBps();
   207	        world.evaluateBanditSpawns(_missSeed(ClanWorldConstants.REGION_FOREST, increment));
   208	
   209	        (, uint16 probabilityAccum) = world.getBanditSpawnState(ClanWorldConstants.REGION_FOREST);
   210	        assertEq(probabilityAccum, increment, "first eligible tick increments accumulator");
   211	    }
   212	
   213	    function test_perRegionCapEnforced() public {
   214	        _advancePastInitialCooldown(world);
   215	        _mintForestClan(world);
   216	
   217	        uint8 maxPerRegion = world.maxBanditsPerRegion();
   218	        for (uint8 i = 0; i < maxPerRegion; i++) {
   219	            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
   220	        }
   221	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   222	
   223	        world.evaluateBanditSpawns(keccak256("per-region-cap"));
   224	
   225	        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
   226	    }
   227	
   228	    function test_globalCapEnforced() public {
   229	        _advancePastInitialCooldown(world);
   230	        _mintForestClan(world);
   231	
   232	        uint8 maxTotal = world.maxTotalBandits();
   233	        for (uint8 i = 0; i < maxTotal; i++) {
   234	            world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100 + i);
   235	        }
   236	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 10000);
   237	
   238	        world.evaluateBanditSpawns(keccak256("global-cap"));
   239	
   240	        assertEq(world.activeBanditCount(), maxTotal, "global cap");
   241	    }
   242	
   243	    function test_globalCapRefreshesPreviewOnHeartbeat() public {
   244	        _mintForestClan(world);
   245	
   246	        uint8 maxTotal = world.maxTotalBandits();
   247	        for (uint8 i = 0; i < maxTotal; i++) {
   248	            world.spawnBandit(uint8(ClanWorldConstants.REGION_FOREST + (i % 8)), 100 + i);
   249	        }
   250	        world.setBanditSpawnState(ClanWorldConstants.REGION_FOREST, 0, 4321);
   251	
   252	        _advanceTick(world);
   253	
   254	        WorldState memory state = world.getWorldState();
   255	        assertEq(world.activeBanditCount(), maxTotal, "still at cap");
   256	        assertEq(state.nextBanditSpawnEligibleTick, 0, "no eligible tick while capped");
   257	        assertEq(state.currentBanditSpawnChanceBps, 4321, "preview chance refreshed");
   258	    }
   259	
   260	    function test_heartbeatCompletesWhenClanCountExceedsBanditSpawnScanCap() public {

codex
The broad search turned up off-chain mocks and adapters that still speak the old bandit schema, so I’m checking whether that drift exists in the head tree too or only in the current workspace checkout. That matters a lot for whether this is a real merge blocker versus just local branch skew.
exec
/bin/bash -lc "git grep -n \"banditId: string|currentRegion: string|attackAttemptsMade|stateEnteredTick|nextActionTick|tier: number|attackPower: number|BanditState\\.NONE|BanditState\\.CAMPING|BanditState\\.ATTACKING|BanditState\\.DEFEATED|getBanditTroop\\(|getBandit\\(|getBanditsInRegion\\(\" 23f7f1a -- packages apps | head -n 300" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: command line, 'banditId: string|currentRegion: string|attackAttemptsMade|stateEnteredTick|nextActionTick|tier: number|attackPower: number|BanditState\.NONE|BanditState\.CAMPING|BanditState\.ATTACKING|BanditState\.DEFEATED|getBanditTroop\(|getBandit\(|getBanditsInRegion\(': Unmatched ( or \(

exec
/bin/bash -lc "git show 23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts | nl -ba | sed -n '60,120p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
    96	
    97	// Regions (8-region travel graph per v4 §1.2). Ownership tracks homebase.
    98	const REGIONS: Region[] = [
    99	  { id: 'forest', name: 'Forest', ownerClanId: 'aldric' },
   100	  { id: 'mountains', name: 'Mountains', ownerClanId: 'brennan' },
   101	  { id: 'unicorn-town', name: 'Unicorn Town', ownerClanId: null },
   102	  { id: 'west-farmland', name: 'West Farmland', ownerClanId: 'mira' },
   103	  { id: 'east-farmland', name: 'East Farmland', ownerClanId: 'sora' },
   104	  { id: 'west-docks', name: 'West Docks', ownerClanId: null },
   105	  { id: 'east-docks', name: 'East Docks', ownerClanId: null },
   106	  { id: 'deep-sea', name: 'Deep Sea', ownerClanId: null },
   107	];
   108	
   109	// Clans — asymmetric mid-game state showing strategic divergence.
   110	
   111	// Aldric (clan 0) — STARVATION HOOK: 1e18 wheat + 2 clansmen = empties next tick.
   112	const ALDRIC: ClanDemoState = {
   113	  id: 'aldric',
   114	  clanId: 0,
   115	  name: 'Aldric',
   116	  baseRegion: 'forest',
   117	  baseLevel: 2,
   118	  wallLevel: 2,
   119	  monumentLevel: 3,
   120	  livingClansmen: 2,

exec
/bin/bash -lc "git show 23f7f1a:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '90,150p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   111	                name: 'clansman',
   112	                type: 'tuple',
   113	                components: [
   114	                  {
   115	                    name: 'clansman',
   116	                    type: 'tuple',
   117	                    components: [
   118	                      { name: 'clansmanId', type: 'uint32' },
   119	                      { name: 'clanId', type: 'uint32' },
   120	                      { name: 'state', type: 'uint8' },
   121	                      { name: 'currentRegion', type: 'uint8' },
   122	                      { name: 'cooldownEndsAtTs', type: 'uint64' },
   123	                      { name: 'lastMissionNonce', type: 'uint64' },
   124	                      { name: 'carryWood', type: 'uint256' },
   125	                      { name: 'carryIron', type: 'uint256' },
   126	                      { name: 'carryWheat', type: 'uint256' },
   127	                      { name: 'carryFish', type: 'uint256' },
   128	                    ],
   129	                  },
   130	                  {
   131	                    name: 'activeMission',
   132	                    type: 'tuple',
   133	                    components: [
   134	                      { name: 'active', type: 'bool' },
   135	                      { name: 'nonce', type: 'uint64' },
   136	                      { name: 'clansmanId', type: 'uint32' },
   137	                      { name: 'startRegion', type: 'uint8' },
   138	                      { name: 'targetRegion', type: 'uint8' },
   139	                      { name: 'action', type: 'uint8' },
   140	                      { name: 'startTick', type: 'uint64' },
   141	                      { name: 'arrivalTick', type: 'uint64' },
   142	                      { name: 'actionStartTick', type: 'uint64' },
   143	                      { name: 'missionSeed', type: 'bytes32' },
   144	                      { name: 'marketMode', type: 'uint8' },
   145	                      { name: 'targetClanId', type: 'uint32' },
   146	                      { name: 'marketToken', type: 'address' },
   147	                      { name: 'marketAmount', type: 'uint256' },
   148	                      { name: 'maxGoldIn', type: 'uint256' },
   149	                    ],
   150	                  },

exec
/bin/bash -lc "git grep -n \"getWorldSnapshot\\(|currentSeasonNumber|nextHeartbeatAtTick\" 23f7f1a -- packages apps | head -n 200" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: command line, 'getWorldSnapshot\(|currentSeasonNumber|nextHeartbeatAtTick': Unmatched ( or \(

exec
/bin/bash -lc "git grep -n -e 'BanditState.None' -e 'BanditState.NONE' -e 'currentSeasonNumber' -e 'nextHeartbeatAtTick' 23f7f1a -- packages apps" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:apps/server/convex/schema.ts:10:    currentSeasonNumber: v.optional(v.number()),
23f7f1a:apps/server/convex/schema.ts:16:    nextHeartbeatAtTick: v.optional(v.number()),
23f7f1a:packages/contracts/abi/IClanWorld.json:2023:              "name": "currentSeasonNumber",
23f7f1a:packages/contracts/abi/IClanWorld.json:2028:              "name": "nextHeartbeatAtTick",
23f7f1a:packages/contracts/abi/IClanWorld.json:2140:              "name": "currentSeasonNumber",
23f7f1a:packages/contracts/abi/IClanWorld.json:2145:              "name": "nextHeartbeatAtTick",
23f7f1a:packages/contracts/src/ClanWorld.sol:134:        _world.currentSeasonNumber = 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:135:        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
23f7f1a:packages/contracts/src/ClanWorld.sol:1560:        require(newState != BanditState.None, "ClanWorld: invalid bandit transition");
23f7f1a:packages/contracts/src/ClanWorld.sol:1644:                if (candidate.id == ClanWorldConstants.BANDIT_ID_NULL || candidate.state == BanditState.None) {
23f7f1a:packages/contracts/src/ClanWorld.sol:2263:        _world.nextHeartbeatAtTick = newTick + 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:2299:            _world.currentSeasonNumber += 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:3182:        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3186:                state: BanditState.None,
23f7f1a:packages/contracts/src/ClanWorld.sol:3391:            currentSeasonNumber: _world.currentSeasonNumber,
23f7f1a:packages/contracts/src/ClanWorld.sol:3392:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
23f7f1a:packages/contracts/src/ClanWorld.sol:3476:        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:52:        _world.currentSeasonNumber = 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:53:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:81:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
23f7f1a:packages/contracts/src/ClanWorldStub.sol:222:            state: BanditState.None,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:303:            currentSeasonNumber: _world.currentSeasonNumber,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:304:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:351:            state: BanditState.None,
23f7f1a:packages/contracts/src/IClanWorld.sol:193:    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
23f7f1a:packages/contracts/src/IClanWorld.sol:194:    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
23f7f1a:packages/contracts/src/IClanWorld.sol:412:    uint64 currentSeasonNumber;
23f7f1a:packages/contracts/src/IClanWorld.sol:413:    uint64 nextHeartbeatAtTick;
23f7f1a:packages/contracts/test/Bandit.t.sol:75:        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
23f7f1a:packages/contracts/test/Bandit.t.sol:81:        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
23f7f1a:packages/contracts/test/Bandit.t.sol:124:        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
23f7f1a:packages/contracts/test/Bandit.t.sol:176:        world.transitionBandit(id, BanditState.None);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:485:        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2010:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2072:        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2081:    function test_nextHeartbeatAtTick_tracks_tick() public {
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2083:        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
23f7f1a:packages/contracts/test/ClanWorld.t.sol:2088:        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:42:        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:48:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:51:        assertEq(ws.nextHeartbeatAtTick, ws.currentTick + 1, "next heartbeat opens currentTick + 1");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:394:    //   - currentSeasonNumber increments on the heartbeat that closes tick
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:400:        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol:410:        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/abi/IClanWorld.json | rg -n 'getBandit|getBanditTroop|getBanditsInRegion|currentSeasonNumber|nextHeartbeatAtTick|carryGold|stateEnteredTick|attackAttemptsMade|tier|attackPower'" in /home/claude/code/clan-world
 succeeded in 0ms:
60:              "name": "attackAttemptsMade",
70:              "name": "stateEnteredTick",
80:              "name": "tier",
85:              "name": "attackPower",
256:      "name": "getBandit",
321:              "name": "carryGold",
332:      "name": "getBanditTargetPreview",
351:      "name": "getBanditTroop",
416:              "name": "carryGold",
427:      "name": "getBanditsInRegion",
2023:              "name": "currentSeasonNumber",
2028:              "name": "nextHeartbeatAtTick",
2140:              "name": "currentSeasonNumber",
2145:              "name": "nextHeartbeatAtTick",
2624:          "name": "attackPower",
2766:          "name": "tier",
2772:          "name": "attackPower",

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/ClanWorldStub.sol | nl -ba | sed -n '180,360p'" in /home/claude/code/clan-world
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
   236	    }
   237	
   238	    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
   239	        return new uint32[](0);
   240	    }
   241	
   242	    function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
   243	        west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
   244	        east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
   245	    }
   246	
   247	    function getScheduledMarketActionsForTick(uint64) external pure override returns (ScheduledMarketAction[] memory) {
   248	        return new ScheduledMarketAction[](0);
   249	    }
   250	
   251	    function getActiveDefenders(uint32) external pure override returns (uint32[] memory) {
   252	        return new uint32[](0);
   253	    }
   254	
   255	    function getDefendingClans(uint8) external pure override returns (uint32[] memory) {
   256	        return new uint32[](0);
   257	    }
   258	
   259	    // -------------------------------------------------------------------------
   260	    // Derived read getters
   261	    // -------------------------------------------------------------------------
   262	
   263	    function getDerivedClanState(uint32) external view override returns (DerivedClanState memory) {
   264	        Clan memory c = this.getClan(0);
   265	        return DerivedClanState({clan: c, isStarving: false, lootValue: 0, derivedAtTick: _world.currentTick});
   266	    }
   267	
   268	    function getDerivedClansmanState(uint32) external view override returns (DerivedClansmanState memory) {
   269	        Clansman memory cm = this.getClansman(0);
   270	        Mission memory m = this.getActiveMission(0);
   271	        return
   272	            DerivedClansmanState({
   273	                clansman: cm, activeMission: m, effectiveRegion: 0, derivedAtTick: _world.currentTick
   274	            });
   275	    }
   276	
   277	    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
   278	        return 0;
   279	    }
   280	
   281	    function quoteTravel(uint8, uint8) external pure override returns (uint8, bytes8) {
   282	        return (0, bytes8(0));
   283	    }
   284	
   285	    function quoteLootValueRaw(uint32) external pure override returns (uint256) {
   286	        return 0;
   287	    }
   288	
   289	    function quoteLootValueSettled(uint32) external pure override returns (uint256) {
   290	        return 0;
   291	    }
   292	
   293	    // -------------------------------------------------------------------------
   294	    // UI indexer aggregator getters
   295	    // -------------------------------------------------------------------------
   296	
   297	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
   298	        return WorldSnapshot({
   299	            currentTick: _world.currentTick,
   300	            seasonStartTick: _world.seasonStartTick,
   301	            seasonEndTick: _world.seasonEndTick,
   302	            seasonFinalized: false,
   303	            currentSeasonNumber: _world.currentSeasonNumber,
   304	            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
   305	            winterActive: _world.winterActive,
   306	            winterStartsAtTick: _world.winterStartsAtTick,
   307	            winterEndsAtTick: _world.winterEndsAtTick,
   308	            activeBanditId: 0,
   309	            currentTickSeed: bytes32(0),
   310	            leaderboard: new LeaderboardEntry[](0)
   311	        });
   312	    }
   313	
   314	    function getClanFullView(uint32) external view override returns (ClanFullView memory) {
   315	        return ClanFullView({
   316	            clan: DerivedClanState({
   317	                clan: this.getClan(0), isStarving: false, lootValue: 0, derivedAtTick: _world.currentTick
   318	            }),
   319	            clansmen: new ClansmanFullView[](0),
   320	            westPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
   321	            eastPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
   322	            incomingDefenderIds: new uint32[](0),
   323	            thisClanDefendingBaseId: 0
   324	        });
   325	    }
   326	
   327	    function getMarketState() external view override returns (MarketState memory) {
   328	        return MarketState({
   329	            wood: PoolReserves({
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/test/HeartbeatOrdering.t.sol | nl -ba | sed -n '330,420p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   330	    }
   331	
   332	    // -------------------------------------------------------------------------
   333	    // test_heartbeat_atomicTickSeedPublish
   334	    //
   335	    // Proves Step 5: after heartbeat, currentTick increments by 1, currentTickSeed
   336	    // changes, and the new seed is deterministic from block.prevrandao + prior seed.
   337	    // -------------------------------------------------------------------------
   338	    function test_heartbeat_atomicTickSeedPublish() public {
   339	        WorldState memory before_ = world.getWorldState();
   340	        uint64 tickBefore = before_.currentTick;
   341	        bytes32 seedBefore = before_.currentTickSeed;
   342	
   343	        bytes32 expectedSeed = keccak256(abi.encode(block.prevrandao, seedBefore, tickBefore));
   344	        world.heartbeat();
   345	
   346	        WorldState memory after_ = world.getWorldState();
   347	        assertEq(after_.currentTick, tickBefore + 1, "tick must increment by 1");
   348	        assertNotEq(after_.currentTickSeed, seedBefore, "seed must change after heartbeat");
   349	        assertEq(after_.currentTickSeed, expectedSeed, "seed must match keccak(prevrandao, oldSeed, closedTick)");
   350	
   351	        // Second heartbeat chains from the new seed
   352	        uint64 tick2 = after_.currentTick;
   353	        bytes32 seed2 = after_.currentTickSeed;
   354	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   355	        bytes32 expectedSeed2 = keccak256(abi.encode(block.prevrandao, seed2, tick2));
   356	        world.heartbeat();
   357	
   358	        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
   359	        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
   360	    }
   361	
   362	    function test_heartbeat_scheduledMarketObservesClosedTickSeedBeforeIncrement() public {
   363	        _setupRecordingMarket();
   364	        uint32 clanId = _mintClan();
   365	        uint32 csId = _firstCs(clanId);
   366	
   367	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, address(woodToken), 5e18, 0);
   368	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
   369	
   370	        Mission memory m = world.getActiveMission(csId);
   371	        uint64 executeAtTick = m.actionStartTick;
   372	        _advanceToTick(executeAtTick);
   373	
   374	        WorldState memory beforeClose = world.getWorldState();
   375	        assertEq(beforeClose.currentTick, executeAtTick, "setup must be at execute tick before close");
   376	        bytes32 seedForClosedTick = beforeClose.currentTickSeed;
   377	
   378	        _advanceTick();
   379	
   380	        assertTrue(recordingWoodPool.observedSell(), "scheduled sell must execute");
   381	        assertEq(recordingWoodPool.observedTick(), executeAtTick, "market observes closed tick before increment");
   382	        assertEq(recordingWoodPool.observedSeed(), seedForClosedTick, "market observes seed for closed tick");
   383	
   384	        WorldState memory afterClose = world.getWorldState();
   385	        assertEq(afterClose.currentTick, executeAtTick + 1, "heartbeat opens next tick after market execution");
   386	        assertNotEq(afterClose.currentTickSeed, seedForClosedTick, "next tick seed publishes after close-tick work");
   387	    }
   388	
   389	    // -------------------------------------------------------------------------
   390	    // test_heartbeat_seasonTransition
   391	    //
   392	    // Proves Step 4 (world events) fires AFTER Steps 1-3:
   393	    //   - season boundary is crossed at SEASON_DURATION_TICKS
   394	    //   - currentSeasonNumber increments on the heartbeat that closes tick
   395	    //     SEASON_DURATION_TICKS-1 (newTick = SEASON_DURATION_TICKS >= seasonEndTick)
   396	    //   - no crash or revert when there are no pending missions or market actions
   397	    // -------------------------------------------------------------------------
   398	    function test_heartbeat_seasonTransition() public {
   399	        WorldState memory ws0 = world.getWorldState();
   400	        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
   401	        assertEq(ws0.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS, "seasonEndTick starts at 360");
   402	
   403	        // Advance SEASON_DURATION_TICKS heartbeats to cross the boundary
   404	        for (uint256 i = 0; i < ClanWorldConstants.SEASON_DURATION_TICKS; i++) {
   405	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   406	            world.heartbeat();
   407	        }
   408	
   409	        WorldState memory ws1 = world.getWorldState();
   410	        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");
   411	        assertEq(ws1.currentTick, ClanWorldConstants.SEASON_DURATION_TICKS, "tick must be at season boundary");
   412	        assertEq(ws1.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS, "new seasonStartTick");
   413	        assertEq(ws1.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2, "new seasonEndTick");
   414	    }
   415	
   416	    // -------------------------------------------------------------------------
   417	    // test_heartbeat_noopTick
   418	    //
   419	    // Heartbeat with no pending missions and no queued market actions must:
   420	    //   - not revert

exec
/bin/bash -lc "git grep -n \"_transitionBanditToAttacking\\|transitionBanditToAttacking\\|BanditState.Attacking\" 23f7f1a -- packages/contracts/src packages/contracts/test | head -n 200" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:packages/contracts/src/ClanWorld.sol:587:                if (bandit.state == BanditState.Attacking && bandit.targetClanId == deadClanId) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1551:    function _transitionBanditToAttacking(uint32 id, uint32 targetClanId) internal {
23f7f1a:packages/contracts/src/ClanWorld.sol:1554:        _transitionBanditState(id, BanditState.Attacking);
23f7f1a:packages/contracts/src/ClanWorld.sol:1573:        if (newState != BanditState.Attacking) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1583:        if (bandit.state == BanditState.Attacking) return _canBanditLeaveAttacking(newState);
23f7f1a:packages/contracts/src/ClanWorld.sol:1596:            || (newState == BanditState.Attacking
23f7f1a:packages/contracts/src/ClanWorld.sol:1686:                bool shouldResolve = bandit.state == BanditState.Attacking && bandit.tickEnteredState == closedTick;
23f7f1a:packages/contracts/src/ClanWorld.sol:1702:        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state != BanditState.Attacking) {
23f7f1a:packages/contracts/test/Bandit.t.sol:17:    function transitionBanditToAttacking(uint32 id, uint32 targetClanId) external {
23f7f1a:packages/contracts/test/Bandit.t.sol:18:        _transitionBanditToAttacking(id, targetClanId);
23f7f1a:packages/contracts/test/Bandit.t.sol:48:        world.transitionBanditToAttacking(id, targetClanId);
23f7f1a:packages/contracts/test/Bandit.t.sol:108:        world.transitionBanditToAttacking(id, 77);
23f7f1a:packages/contracts/test/Bandit.t.sol:111:        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
23f7f1a:packages/contracts/test/Bandit.t.sol:136:        world.transitionBanditToAttacking(id1, 77);
23f7f1a:packages/contracts/test/Bandit.t.sol:173:        world.transitionBandit(id, BanditState.Attacking);
23f7f1a:packages/contracts/test/Bandit.t.sol:195:        world.transitionBanditToAttacking(id1, 77);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:23:        _bandits[id].state = BanditState.Attacking;

exec
/bin/bash -lc 'git grep -n "Camped" 23f7f1a -- packages/contracts/src/ClanWorld.sol packages/contracts/test | head -n 200' in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:packages/contracts/src/ClanWorld.sol:1582:        if (bandit.state == BanditState.Camped) return _canBanditLeaveCamped(bandit, newState);
23f7f1a:packages/contracts/src/ClanWorld.sol:1591:            || (newState == BanditState.Camped && _world.currentTick >= bandit.tickEnteredState + 1);
23f7f1a:packages/contracts/src/ClanWorld.sol:1594:    function _canBanditLeaveCamped(BanditTroop storage bandit, BanditState newState) internal view returns (bool) {
23f7f1a:packages/contracts/src/ClanWorld.sol:1611:            || (newState == BanditState.Camped
23f7f1a:packages/contracts/src/ClanWorld.sol:1662:                    _transitionBanditState(banditId, BanditState.Camped);
23f7f1a:packages/contracts/src/ClanWorld.sol:1667:                    _transitionBanditState(banditId, BanditState.Camped);
23f7f1a:packages/contracts/src/ClanWorld.sol:3480:            } else if (bandit.state == BanditState.Camped) {
23f7f1a:packages/contracts/test/Bandit.t.sol:84:    function test_spawnedToCampedTransitionAdvancesAfterSpawnDelay() public {
23f7f1a:packages/contracts/test/Bandit.t.sol:90:        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
23f7f1a:packages/contracts/test/Bandit.t.sol:143:    function test_attackingToEscapedRestingCampedCycleWorks() public {
23f7f1a:packages/contracts/test/Bandit.t.sol:159:        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped again");

exec
/bin/bash -lc "git grep -n \"target selection\\|projectedTargetClanId\\|getBanditTargetPreview\\|select.*target\\|BanditTarget\" 23f7f1a -- packages/contracts/src packages/contracts/test docs | head -n 200" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:docs/planning/clanworld_numbered_implementation_plan.md:86:- `getBanditTargetPreview`
23f7f1a:docs/planning/clanworld_numbered_implementation_plan.md:384:Before target selection:
23f7f1a:docs/planning/clanworld_v1_implementation_profile.md:187:- target selection
23f7f1a:docs/planning/clanworld_v1_implementation_profile.md:283:- target selection
23f7f1a:docs/planning/clanworld_v4_1_addendum.md:42:4. resolve next target selection / attack logic there
23f7f1a:docs/planning/clanworld_v4_1_addendum.md:48:## A4. Eager-settle scope for bandit target selection
23f7f1a:docs/planning/clanworld_v4_1_addendum.md:50:Before bandit target selection is computed in a region, **all clans with homebases in the bandit's current region must be eagerly settled to that tick**.
23f7f1a:docs/planning/clanworld_v4_1_addendum.md:55:- target selection is not based only on the eventually selected base
23f7f1a:docs/planning/clanworld_v4_1_addendum.md:56:- target selection is computed only after all candidate bases in the current region have been eagerly settled
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:65:Before selecting a bandit target in region `R`, the engine must eagerly settle:
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:620:function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:626:`getBanditTargetPreview()` is **non-binding**.
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:35:- 1 call for `getBanditTargetPreview`
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:184:    uint32 projectedTargetClanId;     // 0 if no eligible target in current region
23f7f1a:docs/planning/clanworld_v4_4_ui_indexer_getters.md:193:- Folds `getBanditTroop` + `getBanditTargetPreview` into one call.
23f7f1a:docs/planning/clanworld_v4_spec.md:676:Bandit target selection is based on **current vault state at attack resolution time**, not at spawn time.
23f7f1a:docs/planning/clanworld_v4_spec.md:688:## 6.8 Attack target selection
23f7f1a:docs/planning/phase-3-test-spec.md:82:**Setup:** Spawn bandit at tick N, `nextActionTick = N + 2`. Mint a clan in the bandit's region (so target selection can succeed).
23f7f1a:docs/planning/phase-3-test-spec.md:502:- The `_eagerSettleForBandit(banditId)` call MUST happen before target selection AND before damage application (3.E2). Tests 3.E2-a and 3.E2-b directly verify this ordering.
23f7f1a:packages/contracts/src/ClanWorld.sol:590:                    emit BanditTargetDied(banditId, deadClanId, currentTick);
23f7f1a:packages/contracts/src/ClanWorld.sol:3299:    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3502:            projectedTargetClanId: bandit.targetClanId,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:277:    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:363:            projectedTargetClanId: 0,
23f7f1a:packages/contracts/src/IClanWorld.sol:472:    uint32 projectedTargetClanId; // 0 if no eligible target in current region
23f7f1a:packages/contracts/src/IClanWorld.sol:600:    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
23f7f1a:packages/contracts/src/IClanWorld.sol:761:    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:83:    event BanditTargetDied(uint32 indexed banditId, uint32 indexed deadClanId, uint64 tick);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:174:    function _assertBanditTargetDiedLog(
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:180:        bytes32 eventSig = keccak256("BanditTargetDied(uint32,uint32,uint64)");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:192:        fail("expected BanditTargetDied log");
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:292:        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol | nl -ba | sed -n '260,560p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   260	
   261	        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 1, "target blueprint awarded");
   262	    }
   263	
   264	    function test_deadTargetCleanupReleasesDefendersAndEscapesBandit() public {
   265	        uint32[] memory clanIds = _mintClans(3);
   266	        uint32 defenderClanId = clanIds[0];
   267	        uint32 targetClanId = clanIds[1];
   268	        uint32 unaffectedClanId = clanIds[2];
   269	
   270	        uint64 defenderExecutesAtTick = _submitTargetDefenders(defenderClanId, targetClanId, 1);
   271	        _advanceUntilAfter(defenderExecutesAtTick);
   272	        world.settleClan(defenderClanId);
   273	
   274	        uint32 defenderId = _csId(defenderClanId, 0);
   275	        ClansmanState defenderStateBefore = world.getClansman(defenderId).state;
   276	        uint64 defenderCooldownBefore = world.getClansman(defenderId).cooldownEndsAtTs;
   277	        assertEq(uint8(defenderStateBefore), uint8(ClansmanState.ACTING), "defender active before target death");
   278	        assertEq(world.getActiveDefenders(targetClanId).length, 1, "target has active defender");
   279	
   280	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
   281	        _advanceUntil(winterStart + 4);
   282	        uint64 deathFromTick = winterStart;
   283	        world.setClanUpkeepState(targetClanId, deathFromTick, 0, 0);
   284	
   285	        Clan memory unaffectedBefore = world.getClan(unaffectedClanId);
   286	        uint32 banditId = _forceAttack(targetClanId, 100);
   287	        uint64 expectedBanditAbortTick = world.getWorldState().currentTick;
   288	
   289	        vm.recordLogs();
   290	        world.settleClan(targetClanId);
   291	        Vm.Log[] memory logs = vm.getRecordedLogs();
   292	        _assertBanditTargetDiedLog(logs, banditId, targetClanId, expectedBanditAbortTick);
   293	
   294	        Clan memory targetAfter = world.getClan(targetClanId);
   295	        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
   296	        assertEq(targetAfter.livingClansmen, 0, "target living count");
   297	
   298	        ClansmanState defenderStateAfter = world.getClansman(defenderId).state;
   299	        assertEq(uint8(defenderStateAfter), uint8(ClansmanState.WAITING), "defender released to waiting");
   300	        assertEq(world.getClansman(defenderId).cooldownEndsAtTs, defenderCooldownBefore, "cooldown unchanged");
   301	        assertFalse(world.getActiveMission(defenderId).active, "defender mission cleared");
   302	        assertEq(world.getActiveDefenders(targetClanId).length, 0, "target defender registry cleared");
   303	
   304	        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
   305	        assertEq(world.getBandit(banditId).targetClanId, 0, "bandit target cleared");
   306	
   307	        Clan memory unaffectedAfter = world.getClan(unaffectedClanId);
   308	        assertEq(uint8(unaffectedAfter.clanState), uint8(unaffectedBefore.clanState), "clan C state unaffected");
   309	        assertEq(unaffectedAfter.livingClansmen, unaffectedBefore.livingClansmen, "clan C living unaffected");
   310	        assertEq(unaffectedAfter.vaultWheat, unaffectedBefore.vaultWheat, "clan C wheat unaffected");
   311	        assertEq(unaffectedAfter.vaultFish, unaffectedBefore.vaultFish, "clan C fish unaffected");
   312	    }
   313	
   314	    function test_defeatedBanditLootSplitsEquallyAcrossFourDefendingClans() public {
   315	        uint32[] memory clanIds = _mintClans(4);
   316	        _activateTargetDefenders(clanIds[0], clanIds, 1);
   317	
   318	        uint256[4] memory woodBefore;
   319	        uint256[4] memory wheatBefore;
   320	        uint256[4] memory fishBefore;
   321	        uint256[4] memory ironBefore;
   322	        uint256[4] memory goldBefore;
   323	        for (uint256 i = 0; i < clanIds.length; i++) {
   324	            Clan memory clan = world.getClan(clanIds[i]);
   325	            woodBefore[i] = clan.vaultWood;
   326	            wheatBefore[i] = clan.vaultWheat;
   327	            fishBefore[i] = clan.vaultFish;
   328	            ironBefore[i] = clan.vaultIron;
   329	            goldBefore[i] = clan.goldBalance;
   330	        }
   331	
   332	        uint32 banditId = _forceAttack(clanIds[0], 1);
   333	        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
   334	        world.setBanditStrength(banditId, 0);
   335	
   336	        _advanceTick();
   337	
   338	        for (uint256 i = 0; i < clanIds.length; i++) {
   339	            Clan memory clan = world.getClan(clanIds[i]);
   340	            assertEq(clan.vaultWood, woodBefore[i] + 25e18, "wood share");
   341	            assertEq(clan.vaultWheat, wheatBefore[i] + 25e18, "wheat share");
   342	            assertEq(clan.vaultFish, fishBefore[i] + 25e18, "fish share");
   343	            assertEq(clan.vaultIron, ironBefore[i] + 25e18, "iron share");
   344	            assertEq(clan.goldBalance, goldBefore[i] + 25e18, "gold share");
   345	        }
   346	    }
   347	
   348	    function test_mixedClanDefenseAwardsBlueprintOnlyToBaseOwner() public {
   349	        uint32[] memory clanIds = _mintClans(4);
   350	        _activateTargetDefenders(clanIds[0], clanIds, 1);
   351	
   352	        uint256[4] memory blueprintBefore;
   353	        for (uint256 i = 0; i < clanIds.length; i++) {
   354	            blueprintBefore[i] = world.getClan(clanIds[i]).blueprintBalance;
   355	        }
   356	
   357	        uint32 banditId = _forceAttack(clanIds[0], 1);
   358	        world.setBanditStrength(banditId, 0);
   359	
   360	        _advanceTick();
   361	
   362	        assertEq(world.getClan(clanIds[0]).blueprintBalance, blueprintBefore[0] + 1, "base owner blueprint");
   363	        for (uint256 i = 1; i < clanIds.length; i++) {
   364	            assertEq(world.getClan(clanIds[i]).blueprintBalance, blueprintBefore[i], "helper clan no blueprint");
   365	        }
   366	    }
   367	
   368	    function test_multipleDefeatedBanditsEachAwardBlueprint() public {
   369	        uint32 clanId = _mintClan();
   370	        uint32[] memory defenders = new uint32[](1);
   371	        defenders[0] = clanId;
   372	        _activateTargetDefenders(clanId, defenders, 4);
   373	
   374	        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
   375	        uint32 firstBanditId = _forceAttack(clanId, 1);
   376	        uint32 secondBanditId = _forceAttack(clanId, 1);
   377	        world.setBanditStrength(firstBanditId, 0);
   378	        world.setBanditStrength(secondBanditId, 0);
   379	
   380	        _advanceTick();
   381	
   382	        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore + 2, "one blueprint per defeated bandit");
   383	    }
   384	
   385	    function test_defeatedBanditLootBurnsWholeTokenOverflowAcrossThreeDefendingClans() public {
   386	        uint32[] memory clanIds = _mintClans(3);
   387	        _activateTargetDefenders(clanIds[0], clanIds, 1);
   388	
   389	        uint256 woodBeforeTotal;
   390	        uint256 goldBeforeTotal;
   391	        for (uint256 i = 0; i < clanIds.length; i++) {
   392	            Clan memory clan = world.getClan(clanIds[i]);
   393	            woodBeforeTotal += clan.vaultWood;
   394	            goldBeforeTotal += clan.goldBalance;
   395	        }
   396	
   397	        uint32 banditId = _forceAttack(clanIds[0], 1);
   398	        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
   399	        world.setBanditStrength(banditId, 0);
   400	
   401	        vm.expectEmit(true, true, false, false, address(world));
   402	        emit BanditAttackResolved(banditId, clanIds[0], true, 0, 0, 0, 0, 0, 0, 0, world.getWorldState().currentTick);
   403	        vm.expectEmit(true, true, false, true, address(world));
   404	        emit BanditDefeated(banditId, clanIds[0], world.getWorldState().currentTick);
   405	        vm.expectEmit(true, false, false, true, address(world));
   406	        emit LootDistributed(banditId, clanIds, 33e18, 33e18, 33e18, 33e18, 33e18, 1e18, 1e18, 1e18, 1e18, 1e18);
   407	
   408	        _advanceTick();
   409	
   410	        uint256 woodAfterTotal;
   411	        uint256 goldAfterTotal;
   412	        for (uint256 i = 0; i < clanIds.length; i++) {
   413	            Clan memory clan = world.getClan(clanIds[i]);
   414	            assertEq(clan.vaultWood, 20e18 + 33e18, "wood share");
   415	            assertEq(clan.goldBalance, 3e18 + 33e18, "gold share");
   416	            woodAfterTotal += clan.vaultWood;
   417	            goldAfterTotal += clan.goldBalance;
   418	        }
   419	        assertEq(woodAfterTotal - woodBeforeTotal, 99e18, "wood overflow burned");
   420	        assertEq(goldAfterTotal - goldBeforeTotal, 99e18, "gold overflow burned");
   421	    }
   422	
   423	    function test_multipleDefendersFromSameClanStillReceiveOneClanShare() public {
   424	        uint32[] memory clanIds = _mintClans(2);
   425	        uint64 firstExecutesAtTick = _submitTargetDefenders(clanIds[0], clanIds[0], 3);
   426	        uint64 secondExecutesAtTick = _submitTargetDefenders(clanIds[1], clanIds[0], 1);
   427	        _advanceUntilAfter(firstExecutesAtTick > secondExecutesAtTick ? firstExecutesAtTick : secondExecutesAtTick);
   428	        world.settleClan(clanIds[0]);
   429	        world.settleClan(clanIds[1]);
   430	
   431	        uint256 targetWoodBefore = world.getClan(clanIds[0]).vaultWood;
   432	        uint256 helperWoodBefore = world.getClan(clanIds[1]).vaultWood;
   433	        uint32 banditId = _forceAttack(clanIds[0], 1);
   434	        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
   435	        world.setBanditStrength(banditId, 0);
   436	
   437	        _advanceTick();
   438	
   439	        assertEq(world.getClan(clanIds[0]).vaultWood, targetWoodBefore + 50e18, "target clan one share");
   440	        assertEq(world.getClan(clanIds[1]).vaultWood, helperWoodBefore + 50e18, "helper clan one share");
   441	    }
   442	
   443	    function test_escapedBanditDoesNotDistributeCarry() public {
   444	        uint32 clanId = _mintClan();
   445	        Clan memory beforeClan = world.getClan(clanId);
   446	        uint32 banditId = _forceAttack(clanId, 100);
   447	        world.setBanditCarry(banditId, 100e18, 100e18, 100e18, 100e18, 100e18);
   448	
   449	        _advanceTick();
   450	
   451	        Clan memory afterClan = world.getClan(clanId);
   452	        assertEq(afterClan.vaultWood, beforeClan.vaultWood, "wood unchanged");
   453	        assertEq(afterClan.vaultWheat, beforeClan.vaultWheat, "wheat unchanged");
   454	        assertEq(afterClan.vaultFish, beforeClan.vaultFish, "fish unchanged");
   455	        assertEq(afterClan.vaultIron, beforeClan.vaultIron, "iron unchanged");
   456	        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "gold unchanged");
   457	        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
   458	    }
   459	
   460	    function test_escapedBanditDoesNotAwardBlueprint() public {
   461	        uint32 clanId = _mintClan();
   462	        uint256 blueprintBefore = world.getClan(clanId).blueprintBalance;
   463	        uint32 banditId = _forceAttack(clanId, 100);
   464	
   465	        _advanceTick();
   466	
   467	        assertEq(world.getClan(clanId).blueprintBalance, blueprintBefore, "blueprint unchanged");
   468	        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
   469	    }
   470	
   471	    function test_twoAliveDefendersWithSufficientDefenseDefeatBanditWithoutWallChip() public {
   472	        uint32 clanId = _mintClan();
   473	        _submitDefenders(clanId, 2);
   474	        world.setWallLevel(clanId, 1);
   475	
   476	        uint32 banditId = _forceAttack(clanId, 1);
   477	        bytes32 tickSeed = world.getWorldState().currentTickSeed;
   478	        uint32 defense = world.defenseRoll(tickSeed, banditId, _csId(clanId, 0))
   479	            + world.defenseRoll(tickSeed, banditId, _csId(clanId, 1));
   480	        uint32 strength = defense / 2;
   481	        if (strength == 0) strength = 1;
   482	        world.setBanditStrength(banditId, strength);
   483	        _advanceTick();
   484	
   485	        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.None), "defeated bandit deleted");
   486	        assertEq(world.getClan(clanId).wallLevel, 1, "wall was not chipped");
   487	        assertEq(world.getClan(clanId).livingClansmen, 4, "no casualties");
   488	    }
   489	
   490	    function test_weakDefenseChipsWallOneLevel() public {
   491	        uint32 clanId = _mintClan();
   492	        world.setWallLevel(clanId, 1);
   493	        uint32 banditId = _forceAttack(clanId, 100);
   494	
   495	        _advanceTick();
   496	
   497	        assertEq(world.getClan(clanId).wallLevel, 0, "wall level decreased");
   498	        assertEq(world.getClan(clanId).livingClansmen, 4, "wall absorbed full hit");
   499	        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
   500	    }
   501	
   502	    function test_wallZeroWeakDefenseKillsClansmanDeterministically() public {
   503	        uint32 clanId = _mintClan();
   504	        uint32 banditId = _forceAttack(clanId, 100);
   505	
   506	        _advanceTick();
   507	
   508	        assertEq(world.getClan(clanId).livingClansmen, 3, "one clansman died");
   509	        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Escaped), "bandit escaped");
   510	    }
   511	
   512	    function test_allClansmenDeadMarksClanDead() public {
   513	        uint32 clanId = _mintClan();
   514	        _forceAttack(clanId, 425);
   515	
   516	        _advanceTick();
   517	
   518	        Clan memory clan = world.getClan(clanId);
   519	        assertEq(clan.livingClansmen, 0, "all clansmen dead");
   520	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
   521	        assertEq(clan.vaultWood, 0, "dead target wood burned");
   522	        assertEq(clan.vaultWheat, 0, "dead target wheat burned");
   523	        assertEq(clan.vaultFish, 0, "dead target fish burned");
   524	        assertEq(clan.vaultIron, 0, "dead target iron burned");
   525	    }
   526	
   527	    function test_starvingDefenderContributesZeroDefense() public {
   528	        uint32 clanId = _mintClan();
   529	        _submitDefenders(clanId, 1);
   530	        _advanceTick();
   531	        world.settleClan(clanId);
   532	        world.setWallLevel(clanId, 1);
   533	        world.setStarvationStartsAt(clanId, 1);
   534	
   535	        uint32 banditId = _forceAttack(clanId, 100);
   536	        uint32 nonStarvingRoll =
   537	            world.defenseRoll(world.getWorldState().currentTickSeed, banditId, _csId(clanId, 0));
   538	        assertGt(nonStarvingRoll, 0, "test setup needs nonzero roll");
   539	
   540	        _advanceTick();
   541	
   542	        assertEq(world.getClan(clanId).wallLevel, 0, "starving defender did not reduce incoming wall hit");
   543	        assertEq(world.getClan(clanId).livingClansmen, 4, "wall absorbed full hit");
   544	    }
   545	
   546	    function test_twoAttacksSameTickDeterminismAcrossReplay() public {
   547	        BanditAttackHarness a = new BanditAttackHarness();
   548	        BanditAttackHarness b = new BanditAttackHarness();
   549	
   550	        uint32 aFirst;
   551	        uint32 aSecond;
   552	        uint32 bFirst;
   553	        uint32 bSecond;
   554	        (aFirst, aSecond) = _setupTwoAttackWorld(a);
   555	        (bFirst, bSecond) = _setupTwoAttackWorld(b);
   556	
   557	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   558	        a.heartbeat();
   559	        b.heartbeat();
   560	

exec
/bin/bash -lc "git grep -n -e 'banditId:' -e 'currentRegion:' -e 'attackAttemptsMade' -e 'stateEnteredTick' -e 'nextActionTick' -e 'tier:' -e 'attackPower:' -e 'carryGold' -e 'BanditState.NONE' -e 'BanditState.CAMPING' -e 'BanditState.ATTACKING' -e 'BanditState.DEFEATED' -e 'getBanditTroop(' -e 'getBandit(' -e 'getBanditsInRegion(' 23f7f1a -- packages apps" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:apps/landing/src/pages/LorePage.tsx:27:  { tier: 'I', strength: 30, reward: '1 blueprint', desc: 'Hungry and disorganised. A welcome practice fight.' },
23f7f1a:apps/landing/src/pages/LorePage.tsx:28:  { tier: 'II', strength: 45, reward: '1 blueprint', desc: 'Tested. They know which farms have wheat and which have walls.' },
23f7f1a:apps/landing/src/pages/LorePage.tsx:29:  { tier: 'III', strength: 65, reward: '2 blueprints', desc: 'Veterans. They will not engage unless they expect to win.' },
23f7f1a:apps/landing/src/pages/LorePage.tsx:30:  { tier: 'IV', strength: 90, reward: '2 blueprints', desc: 'Warlords. Their captains have names. Some clans pay tribute rather than fight.' },
23f7f1a:apps/landing/src/pages/LorePage.tsx:31:  { tier: 'V', strength: 130, reward: '3 blueprints', desc: 'Apocalyptic. A clan alone cannot stand. Coalitions form, and break, around them.' },
23f7f1a:packages/contracts/abi/IClanWorld.json:60:              "name": "attackAttemptsMade",
23f7f1a:packages/contracts/abi/IClanWorld.json:70:              "name": "stateEnteredTick",
23f7f1a:packages/contracts/abi/IClanWorld.json:75:              "name": "nextActionTick",
23f7f1a:packages/contracts/abi/IClanWorld.json:321:              "name": "carryGold",
23f7f1a:packages/contracts/abi/IClanWorld.json:416:              "name": "carryGold",
23f7f1a:packages/contracts/src/ClanWorld.sol:1535:            carryGold: 0
23f7f1a:packages/contracts/src/ClanWorld.sol:1779:            perGold = _perClanBanditLootShare(bandit.carryGold, nDefendingClans);
23f7f1a:packages/contracts/src/ClanWorld.sol:1803:            bandit.carryGold - (perGold * nDefendingClans)
23f7f1a:packages/contracts/src/ClanWorld.sol:3180:    function getBandit(uint32 banditId) public view override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3194:                carryGold: 0
23f7f1a:packages/contracts/src/ClanWorld.sol:3200:    function getBanditTroop(uint32 banditId) external view override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3201:        return getBandit(banditId);
23f7f1a:packages/contracts/src/ClanWorld.sol:3204:    function getBanditsInRegion(uint8 region) external view override returns (uint32[] memory) {
23f7f1a:packages/contracts/src/ClanWorld.sol:3475:        uint64 nextActionTick = 0;
23f7f1a:packages/contracts/src/ClanWorld.sol:3479:                nextActionTick = bandit.tickEnteredState + 1;
23f7f1a:packages/contracts/src/ClanWorld.sol:3481:                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
23f7f1a:packages/contracts/src/ClanWorld.sol:3483:                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_REST_TICKS;
23f7f1a:packages/contracts/src/ClanWorld.sol:3489:            banditId: bandit.id,
23f7f1a:packages/contracts/src/ClanWorld.sol:3491:            currentRegion: bandit.region,
23f7f1a:packages/contracts/src/ClanWorld.sol:3492:            attackAttemptsMade: 0,
23f7f1a:packages/contracts/src/ClanWorld.sol:3494:            stateEnteredTick: bandit.tickEnteredState,
23f7f1a:packages/contracts/src/ClanWorld.sol:3495:            nextActionTick: nextActionTick,
23f7f1a:packages/contracts/src/ClanWorld.sol:3496:            tier: 0,
23f7f1a:packages/contracts/src/ClanWorld.sol:3497:            attackPower: _banditStrengthForLegacyEvent(bandit.strength),
23f7f1a:packages/contracts/src/ClanWorldStub.sol:168:            currentRegion: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:218:    function getBandit(uint32) public pure override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:230:            carryGold: 0
23f7f1a:packages/contracts/src/ClanWorldStub.sol:234:    function getBanditTroop(uint32 banditId) external pure override returns (BanditTroop memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:235:        return getBandit(banditId);
23f7f1a:packages/contracts/src/ClanWorldStub.sol:238:    function getBanditsInRegion(uint8) external pure override returns (uint32[] memory) {
23f7f1a:packages/contracts/src/ClanWorldStub.sol:350:            banditId: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:352:            currentRegion: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:353:            attackAttemptsMade: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:355:            stateEnteredTick: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:356:            nextActionTick: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:357:            tier: 0,
23f7f1a:packages/contracts/src/ClanWorldStub.sol:358:            attackPower: 0,
23f7f1a:packages/contracts/src/IClanWorld.sol:314:    uint256 carryGold;
23f7f1a:packages/contracts/src/IClanWorld.sol:460:    uint8 attackAttemptsMade;
23f7f1a:packages/contracts/src/IClanWorld.sol:462:    uint64 stateEnteredTick;
23f7f1a:packages/contracts/src/IClanWorld.sol:463:    uint64 nextActionTick;
23f7f1a:packages/contracts/src/IClanWorld.sol:734:    function getBandit(uint32 banditId) external view returns (BanditTroop memory);
23f7f1a:packages/contracts/src/IClanWorld.sol:736:    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
23f7f1a:packages/contracts/src/IClanWorld.sol:738:    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory);
23f7f1a:packages/contracts/test/Bandit.t.sol:55:        BanditTroop memory bandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:63:        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
23f7f1a:packages/contracts/test/Bandit.t.sol:72:        BanditTroop memory bandit = world.getBandit(999);
23f7f1a:packages/contracts/test/Bandit.t.sol:89:        BanditTroop memory bandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:101:        assertEq(view_.nextActionTick, world.getWorldState().currentTick + 1, "spawn delay tick");
23f7f1a:packages/contracts/test/Bandit.t.sol:110:        BanditTroop memory bandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:121:        BanditTroop memory deletedBandit = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:125:        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
23f7f1a:packages/contracts/test/Bandit.t.sol:140:        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
23f7f1a:packages/contracts/test/Bandit.t.sol:147:        BanditTroop memory escaped = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:152:        BanditTroop memory resting = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:158:        BanditTroop memory camped = world.getBandit(id);
23f7f1a:packages/contracts/test/Bandit.t.sol:184:        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
23f7f1a:packages/contracts/test/Bandit.t.sol:189:        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
23f7f1a:packages/contracts/test/Bandit.t.sol:198:        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:56:        _bandits[banditId].carryGold = gold;
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
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:210:        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 1, "cooldown blocks second spawn");
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:239:        uint8 selectedRegion = world.getBandit(1).region;
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:262:        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, maxPerRegion, "region cap");
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:354:        BanditTroop memory bandit = world.getBandit(1);
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:378:        assertEq(a.getBandit(1).region, b.getBandit(1).region, "same seed selects same region");
23f7f1a:packages/contracts/test/BanditSpawn.t.sol:397:        BanditTroop memory bandit = world.getBandit(1);
23f7f1a:packages/contracts/test/ClanWorldStub.t.sol:39:        BanditTroop memory bandit = stub.getBandit(999);
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:77:  banditId: string;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:79:  currentRegion: string;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:81:  stateEnteredTick: Tick;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:83:  nextActionTick: Tick;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:84:  attackAttemptsMade: number;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:85:  tier: number;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:86:  attackPower: number;
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:310:  banditId: 'bandit-001',
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:312:  currentRegion: 'mountains',
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:313:  stateEnteredTick: 78,
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:314:  nextActionTick: 81,
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:315:  attackAttemptsMade: 0,
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:316:  tier: 2,
23f7f1a:packages/shared/src/mocks/clanWorldFixture.ts:317:  attackPower: 45, // tier-2 per v4 §6.14

exec
/bin/bash -lc "git grep -n -e 'defended' -e 'wall chip' -e 'bandit attack' -e 'totalDefense' -e 'BANDIT_DROP_TO_DEFENDERS_BPS' 23f7f1a -- docs/planning docs/reference packages/contracts/src/IClanWorld.sol | head -n 200" in /home/claude/code/clan-world
 succeeded in 0ms:
23f7f1a:docs/planning/clanworld_numbered_implementation_plan.md:393:- wall chip logic
23f7f1a:docs/planning/clanworld_numbered_implementation_plan.md:400:- defended base receives blueprint fragment
23f7f1a:docs/planning/clanworld_v1_implementation_profile.md:91:- bandit attacks are heartbeat-eager
23f7f1a:docs/planning/clanworld_v1_implementation_profile.md:122:Active defenders are explicitly indexed by defended base.
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:61:4. resolve world events for tick `T` (including bandit attacks and bandit state transitions)
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:83:- bandit attacks and bandit state transitions
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:276:uint16 BANDIT_DROP_TO_DEFENDERS_BPS = 5000;   // 50%
23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:884:event BanditAttackResolved(uint32 indexed banditId, uint32 indexed targetClanId, bool defended, uint16 attackPower, uint16 totalDefense);
23f7f1a:docs/planning/clanworld_v4_3_schema_patch.md:163:### F.3 Cleanup when defended clan dies
23f7f1a:docs/planning/clanworld_v4_3_schema_patch.md:183:Attack power is computed from tier using the locked bandit attack table / helper:
23f7f1a:docs/planning/clanworld_v4_spec.md:129:4. resolve world events for the closing tick, including bandit state transitions and bandit attacks
23f7f1a:docs/planning/clanworld_v4_spec.md:160:- bandit attacks
23f7f1a:docs/planning/clanworld_v4_spec.md:329:- one bandit attack
23f7f1a:docs/planning/clanworld_v4_spec.md:750:Working bandit attack tiers:
23f7f1a:docs/planning/clanworld_v4_spec.md:771:If `clansmanDefense < banditAttack` and `totalDefense >= banditAttack`:
23f7f1a:docs/planning/clanworld_v4_spec.md:778:If `totalDefense < banditAttack`:
23f7f1a:docs/planning/clanworld_v4_spec.md:809:The defended base vault receives:
23f7f1a:docs/planning/clanworld_v4_spec.md:1206:- bandit attack tier schedule
23f7f1a:docs/planning/phase-3-test-spec.md:183:**Setup:** Register worker W as defender of Clan A. Let bandit attack and resolve (defender wins or loses). Do not interrupt W.
23f7f1a:docs/planning/phase-3-test-spec.md:212:- `BanditAttackResolved` event emitted with `defended == true`.
23f7f1a:docs/planning/phase-3-test-spec.md:220:- Defender clan has 0 defenders registered (or weak defenders: `totalDefense < attackPower`).
23f7f1a:docs/planning/phase-3-test-spec.md:229:- `BanditAttackResolved` event emitted with `defended == false`.
23f7f1a:docs/planning/phase-3-test-spec.md:268:- Defenders win (combined 4 > bandit attack power).
23f7f1a:docs/planning/phase-3-test-spec.md:289:**Setup:** Resolve one bandit attack. Read `defenseContributionsByBanditId[banditId]`.
23f7f1a:packages/contracts/src/IClanWorld.sol:72:    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%
23f7f1a:packages/contracts/src/IClanWorld.sol:434:    uint32 thisClanDefendingBaseId; // defended home region, or 0 if none
23f7f1a:packages/contracts/src/IClanWorld.sol:588:        bool defended,
23f7f1a:packages/contracts/src/IClanWorld.sol:590:        uint16 totalDefense,

exec
/bin/bash -lc "git show 23f7f1a:docs/planning/clanworld_v4_spec.md | nl -ba | sed -n '660,740p'" in /home/claude/code/clan-world
exec
/bin/bash -lc "git show 23f7f1a:docs/planning/phase-3-test-spec.md | nl -ba | sed -n '180,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   180	
   181	### test_defenderRegistry_persistsAcrossMultipleBanditCycles
   182	
   183	**Setup:** Register worker W as defender of Clan A. Let bandit attack and resolve (defender wins or loses). Do not interrupt W.
   184	
   185	**Action:** Advance several ticks. Let second bandit spawn and attack same base.
   186	
   187	**Assert:** W is still in `getActiveDefenders(A)` for the second attack. (v4.2 §8.2 — DefendBase is continuous.)
   188	
   189	---
   190	
   191	### test_defenderRegistry_removedOnClanDeath
   192	
   193	**Setup:** Register worker of Clan B as defender of Clan A. Kill Clan A (set workers = 0 / mark dead).
   194	
   195	**Assert:** Worker is removed from `activeDefendersByBase[A]`.
   196	
   197	---
   198	
   199	## 3.E4 — Deterministic combat
   200	
   201	### test_combat_defenderWins_whenTotalDefenseGeAttackPower
   202	
   203	**Setup:**
   204	- Mint attacker clan, mint defender clan.
   205	- Register N defenders at defender clan's base; their combined `defensePoints` > bandit's `attackPower`.
   206	- Spawn bandit targeting defender clan.
   207	
   208	**Action:** Advance to attack tick.
   209	
   210	**Assert:**
   211	- `BanditTroop.state == BanditState.Defeated`.
   212	- `BanditAttackResolved` event emitted with `defended == true`.
   213	- Defender clan vault unchanged (no vault damage).
   214	
   215	---
   216	
   217	### test_combat_attackerWins_whenTotalDefenseLtAttackPower
   218	
   219	**Setup:**
   220	- Defender clan has 0 defenders registered (or weak defenders: `totalDefense < attackPower`).
   221	- Spawn bandit.
   222	
   223	**Action:** Advance to attack tick.
   224	
   225	**Assert:**
   226	- `BanditTroop.state == BanditState.Retreating` (or `Defeated` per implementation).
   227	- Defender clan wall HP decremented.
   228	- Defender clan vault decremented by bandit carry amount.
   229	- `BanditAttackResolved` event emitted with `defended == false`.
   230	
   231	---
   232	
   233	### test_combat_determinism_sameSeedSameOutcome
   234	
   235	**Setup:**
   236	- Snapshot full state at start of tick T (bandit + defenders + seed).
   237	- Record outcome (winner, loot amounts, any randomized values like wound chance).
   238	
   239	**Action:** Re-fork same block via `vm.snapshot` / `vm.revertTo`. Replay heartbeat.
   240	
   241	**Assert:** Outcome byte-for-byte identical. (v4.2 §11.1 — tickSeed as sole randomness source.)
   242	
   243	---
   244	
   245	### test_combat_determinism_differentSeedDifferentOutcome
   246	
   247	**Setup:** Same bandit + defender state, but different `tickSeed` (advance one extra block to change block hash before deriving seed).
   248	
   249	**Assert:** At least one randomized outcome differs (e.g. crit, wound chance). Confirms seed is actually wired into resolution logic.
   250	
   251	---
   252	
   253	### test_combat_noReentrancy_defenderCallbackHarmless
   254	
   255	**Setup:** Deploy a mock defender that re-enters `heartbeat()` on `BanditAttackResolved` callback.
   256	
   257	**Assert:** Reentrancy guard triggers (revert) or re-entry has no effect on state consistency. (v4.2 §11.4.)
   258	
   259	---
   260	

 succeeded in 0ms:
   660	CAMPING
   661	RESTING
   662	ATTACKING
   663	DEFEATED
   664	ESCAPED
   665	```
   666	
   667	## 6.6 Camping phase
   668	After spawning, bandits camp in place for `3` ticks.
   669	
   670	During camp:
   671	- they do not move
   672	- they do not attack
   673	- camp location is visible to all players
   674	
   675	## 6.7 Mutable targeting is intentional
   676	Bandit target selection is based on **current vault state at attack resolution time**, not at spawn time.
   677	
   678	This is an intentional design choice.
   679	
   680	### Intended consequences
   681	This intentionally allows agents to influence targeting during the warning window by:
   682	- moving resources out of vault into worker inventories
   683	- converting resources into gold where feasible
   684	- baiting or shifting target priority toward another base
   685	
   686	This mutability is a feature intended to create strategic planning, bluffing, panic logistics, and adversarial manipulation.
   687	
   688	## 6.8 Attack target selection
   689	When camp ends, or when bandits arrive in a new region during rampage:
   690	- identify the highest loot-value homebase in the current region
   691	- if no base exists in the region, attack resolution is a noop
   692	- if multiple bases are tied for the highest loot-value, including a zero-loot tie, target the base with the lowest `clanId`
   693	
   694	This tiebreak is deterministic and prevents bandit targeting from stalling or becoming ambiguous when multiple bases have identical vault values.
   695	
   696	## 6.9 Loot-value score
   697	Bandits target by fixed weighted vault value:
   698	- wood = `1` point per token
   699	- wheat = `1` point per token
   700	- fish = `2` points per token
   701	- iron = `4` points per token
   702	- gold = `0` points
   703	
   704	Only vault physical resources are counted.
   705	
   706	## 6.10 Rampage path
   707	Bandits move clockwise through the outer ring in this order:
   708	
   709	```text
   710	1 -> 2 -> 5 -> 7 -> 6 -> 4 -> 1 ...
   711	```
   712	
   713	Bandits never enter Unicorn Town or Deep Sea.
   714	
   715	## 6.11 Rampage cycle
   716	After each attack attempt or noop:
   717	- bandits enter `RESTING` state for `2` ticks
   718	- then move to the next clockwise outer-ring region
   719	- then resolve the next attack attempt or noop
   720	
   721	## 6.12 Maximum attack attempts
   722	A troop may make at most `6` attack attempts after leaving camp.
   723	
   724	If still alive after the 6th attempt:
   725	- troop enters `ESCAPED`
   726	- all carried loot is burned
   727	- troop is removed from world
   728	
   729	## 6.13 Defense decomposition
   730	Combat is deterministic.
   731	
   732	Total defense is the sum of:
   733	- `clansmanDefense`
   734	- `wallDefense`
   735	- `baseDefense`
   736	
   737	### Clansman defense
   738	For the attacked base:
   739	- each clansman physically present on `defend_base` mission contributes `10`
   740	- each clansman physically present and `WAITING` at their own homebase contributes `5`

exec
/bin/bash -lc "git show 23f7f1a:docs/planning/clanworld_v4_spec.md | nl -ba | sed -n '736,820p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   736	
   737	### Clansman defense
   738	For the attacked base:
   739	- each clansman physically present on `defend_base` mission contributes `10`
   740	- each clansman physically present and `WAITING` at their own homebase contributes `5`
   741	- starving clansmen contribute `0`
   742	
   743	### Wall defense
   744	`wallDefense = 10 × wallLevel`
   745	
   746	### Base defense
   747	`baseDefense = 5 × baseLevel`
   748	
   749	## 6.14 Bandit strength scaling
   750	Working bandit attack tiers:
   751	- Tier 1 = `30`
   752	- Tier 2 = `45`
   753	- Tier 3 = `60`
   754	- Tier 4 = `80`
   755	- Tier 5 = `95`
   756	
   757	Exact tier schedule may be tuned later, but intended direction is:
   758	- early bandits are survivable with modest prep
   759	- late bandits require active defense and/or mercenaries
   760	
   761	## 6.15 Attack outcomes
   762	
   763	### Case A: clansman defense alone is sufficient
   764	If `clansmanDefense >= banditAttack`:
   765	- defense succeeds
   766	- bandits are defeated
   767	- no wall damage
   768	- no base damage
   769	
   770	### Case B: total defense is sufficient, but walls/base were needed
   771	If `clansmanDefense < banditAttack` and `totalDefense >= banditAttack`:
   772	- defense succeeds
   773	- bandits are defeated
   774	- wall level is reduced by `1`
   775	- base takes no damage
   776	
   777	### Case C: defense fails
   778	If `totalDefense < banditAttack`:
   779	- defense fails
   780	- bandits steal `20%` of each vault physical resource:
   781	  - wood
   782	  - wheat
   783	  - fish
   784	  - iron
   785	- wall level is reduced by `1`
   786	- base takes no damage in v1
   787	- bandits remain alive and continue rampage after rest cycle
   788	
   789	Bandits do not steal gold in v1.
   790	
   791	## 6.16 Bandit loot inventory
   792	Bandits carry stolen physical resources internally until:
   793	- defeated
   794	- escaped
   795	
   796	Bandits do not convert resources into gold in v1.
   797	Bandits do not use Unicorn Town in v1.
   798	
   799	## 6.17 Bandit defeat rewards
   800	When bandits are defeated:
   801	
   802	### Resource drop
   803	- `50%` of carried bandit loot is dropped
   804	- dropped loot is split evenly among all defending clansmen who contributed nonzero defense
   805	- any clansman loot above carrying capacity is burned
   806	- the remaining `50%` of carried bandit loot is burned
   807	
   808	### Bonus reward
   809	The defended base vault receives:
   810	- `1e18` Blueprint Fragment
   811	- `1e18` Gold
   812	
   813	## 6.18 Defender contribution bookkeeping
   814	Bandit resolution must determine:
   815	- which clansmen contributed nonzero defense
   816	- which clan each belongs to
   817	- how dropped loot is apportioned across them
   818	
   819	This is a required bookkeeping part of the combat model.
   820	

collab: Wait
codex
One of the parallel passes finished and it confirmed the biggest concern I was already seeing: the production heartbeat never drives the full bandit lifecycle end-to-end, while the tests mostly do it through harness-only shortcuts. I’m waiting on the simulation/API passes so we can merge the findings cleanly instead of duplicating them.
collab: Wait
collab: Wait
codex
The last pass came back with two deploy-safety issues I definitely want in the final review: a blueprint unit mismatch and stale handwritten ABIs in `runner`/`shared`. I’m pulling those exact line references now so the review lands crisp and actionable.
exec
/bin/bash -lc "git show 23f7f1a:packages/runner/src/runnerCastHeartbeat.ts | nl -ba | sed -n '1,120p'" in /home/claude/code/clan-world
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
    86	  }
    87	  return {
    88	    privateKey: pk,
    89	    rpcUrl: env['RPC_URL_PRIMARY'] || env['RPC_URL_FALLBACK'],
    90	    contractAddress: contractAddress as `0x${string}`,
    91	  };
    92	}
    93	
    94	/**
    95	 * Viem-backed `IHeartbeatCaller`. Wallet account is the dedicated runner key.
    96	 *
    97	 * Rate-limit detection: ClanWorld.heartbeat() reverts when called before
    98	 * `nextHeartbeatAtTs`. We don't have a typed custom error in the ABI, so on
    99	 * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
   100	 * still in the future, throw `HeartbeatRateLimitedError(nextAllowedAt)`.
   101	 * Other reverts surface as the original error.
   102	 */
   103	export class RunnerCastHeartbeat implements IHeartbeatCaller {
   104	  private readonly publicClient: PublicClient;
   105	  private readonly walletClient: WalletClient;
   106	  private readonly account: Account;
   107	  private readonly contractAddress: `0x${string}`;
   108	
   109	  constructor(cfg: RunnerHeartbeatConfig) {
   110	    const pk = normalizePk(cfg.privateKey);
   111	    this.account = privateKeyToAccount(pk);
   112	    const transport = cfg.rpcUrl ? http(cfg.rpcUrl) : http();
   113	    this.publicClient = createPublicClient({ chain: baseSepolia, transport });
   114	    this.walletClient = createWalletClient({
   115	      account: this.account,
   116	      chain: baseSepolia,
   117	      transport,
   118	    });
   119	    this.contractAddress = cfg.contractAddress;
   120	  }

exec
/bin/bash -lc "git show 23f7f1a:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '1,90p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '440,480p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 23f7f1a:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md | nl -ba | sed -n '628,646p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/02' does not exist in '23f7f1a'

exec
/bin/bash -lc "git show '23f7f1a:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md' | nl -ba | sed -n '632,642p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md' exists on disk, but not in '23f7f1a'

exec
/bin/bash -lc "git show 23f7f1a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '420,438p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

codex
# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:1551, 1654, 1679` — The state machine is incomplete in production. Heartbeat advances `Spawned -> Camped` and `Resting -> Camped`, but there is no non-test caller for `_transitionBanditToAttacking`, and no live caller ever moves `Escaped -> Resting`. The tests mask this by forcing state transitions through harness-only helpers in `packages/contracts/test/Bandit.t.sol` and `packages/contracts/test/BanditAttackResolution.t.sol`. As merged, real bandits never attack, and any escaped bandit would permanently occupy active/cap slots. Suggested fix: wire heartbeat-driven target selection/rampage transitions into the live contract and add at least one end-to-end test from natural spawn through attack and post-attack recovery.

- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.

- `packages/contracts/src/ClanWorld.sol:1755` and `packages/contracts/src/ClanWorld.sol:1005-1016` — Blueprint rewards use the wrong unit. Bandit defeat credits `targetClan.blueprintBalance += 1`, but monument upgrades consume `1e18` blueprint units. That makes one bandit win worth one wei of blueprint, and the new test in `packages/contracts/test/BanditAttackResolution.t.sol:252-261` locks that bug in. Suggested fix: award `1e18` per blueprint reward and update the event/test expectations to the same unit.

- `packages/runner/src/runnerCastHeartbeat.ts:34-51`, `packages/shared/src/adapters/IChainClient.ts:34-58`, `packages/contracts/src/IClanWorld.sol:188-206` — Handwritten ABI tuples are stale after the `WorldState` layout change. `currentSeasonNumber` and `nextHeartbeatAtTick` were inserted before `nextHeartbeatAtTs`, but the runner and shared adapter still decode the old tuple. That can misread timestamps and break heartbeat gating/off-chain reads at deploy time. Suggested fix: update both manual ABIs in this PR, or replace them with generated ABI imports.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:1709-1718, 1948-1950, 518-546` — `_resolveBanditAttack()` checks `targetClanState`, then settles the target, but never re-checks whether that settlement killed the clan. If the target dies during pre-attack settlement, the current attacker still resolves a normal attack path against an already-dead target, while only the other attackers get `BanditTargetDied`. Suggested fix: re-check target liveness immediately after `_settleClan(targetClanId)` and escape/abort the current attacker too.

- `packages/contracts/src/ClanWorld.sol:551-573` — Target-death cleanup only releases defenders that already arrived and registered in `_clansmanDefendingRegion`. Defenders still traveling to a dead target keep an active `DefendBase` mission forever. Suggested fix: clear all matching `DefendBase` missions for `targetClanId`, not just the ones with an active region registration.

- `packages/contracts/src/ClanWorld.sol:1177-1182, 3402-3446` — Derived simulation does not mirror live defender bookkeeping. A simulated `DefendBase` arrival never registers a defender, and simulated death paths do not release defender registry state, yet `getClanFullView()` mixes simulated clan data with raw defender indexes. That can make derived views disagree with what real settlement would show. Suggested fix: either simulate defender-registry side effects too, or stop composing simulated state with raw defender indexes.

- `packages/contracts/src/IClanWorld.sol:603-604` and `packages/contracts/src/ClanWorld.sol:1755-1756` — The legacy `BlueprintAwarded` event remains in the interface but is never emitted; only `BlueprintEarned` fires now. That is a deploy/rollback compatibility risk for existing consumers. Suggested fix: dual-emit for one release or update consumers in lockstep and remove the dead legacy event.

- `packages/contracts/src/ClanWorld.sol:1548, 3473-3503` and `packages/contracts/src/IClanWorld.sol:455-473` — Several legacy bandit-facing fields are still exposed but now carry placeholder zeroes (`tier`, `attackAttemptsMade`, `maxAttemptsRemaining`, `projectedTargetLootValue`). That silently degrades downstream behavior instead of failing loudly. Suggested fix: either compute real values or version/remove the legacy fields and update consumers.

## LOW severity findings
- `packages/contracts/src/ClanWorld.sol:1501-1508` — `_simulateCompleteMission()` writes `cooldownEndsAtTs` from `block.timestamp`, so derived getters are wall-clock-sensitive rather than tick-deterministic. Two reads in the same tick can return different cooldowns.

- `packages/shared/src/mocks/clanWorldFixture.ts:76-92, 309-317` — The shared mock/demo bandit schema is still pre-Phase-9 (`CAMPING`, `tier`, old field names). That will drift further from the contract seam even if Solidity-side fixes land.

- `packages/contracts/src/ClanWorld.sol:3180-3202` — `getBanditTroop()` is kept as a compatibility shim to `getBandit()`, but there is no direct compatibility test covering that alias.

## Cross-cutting observations
- The integrated head tree has drifted materially from the documented bandit design: combat is now randomized per-defender roll, reward split is clan-based, legacy fields are partially stubbed, and there is still no heartbeat-driven target-selection/rampage path. The new tests mostly validate internal helpers via harnesses instead of proving the production heartbeat can drive the full game loop.

- I could not run Foundry locally because `forge` is not installed in this environment, so this review is based on static analysis of the `23f7f1a` tree plus parallel code-review passes.
2026-04-29T17:09:18.324916Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-e249-75a2-93f6-512856d719fe not found
2026-04-29T17:09:18.325056Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-e2d1-7f82-a752-ba7e0788f3e4 not found
2026-04-29T17:09:18.325381Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-e365-7c20-9a1f-38ad9f4e9220 not found
2026-04-29T17:09:18.325418Z ERROR codex_core::session: failed to record rollout items: thread 019dda2f-5d1a-70b1-862c-5f07af1d4125 not found
tokens used
305,905
# Phase Super-Swarm Review — PR #194 (head 23f7f1a)

## SUMMARY
NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:1551, 1654, 1679` — The state machine is incomplete in production. Heartbeat advances `Spawned -> Camped` and `Resting -> Camped`, but there is no non-test caller for `_transitionBanditToAttacking`, and no live caller ever moves `Escaped -> Resting`. The tests mask this by forcing state transitions through harness-only helpers in `packages/contracts/test/Bandit.t.sol` and `packages/contracts/test/BanditAttackResolution.t.sol`. As merged, real bandits never attack, and any escaped bandit would permanently occupy active/cap slots. Suggested fix: wire heartbeat-driven target selection/rampage transitions into the live contract and add at least one end-to-end test from natural spawn through attack and post-attack recovery.

- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.

- `packages/contracts/src/ClanWorld.sol:1755` and `packages/contracts/src/ClanWorld.sol:1005-1016` — Blueprint rewards use the wrong unit. Bandit defeat credits `targetClan.blueprintBalance += 1`, but monument upgrades consume `1e18` blueprint units. That makes one bandit win worth one wei of blueprint, and the new test in `packages/contracts/test/BanditAttackResolution.t.sol:252-261` locks that bug in. Suggested fix: award `1e18` per blueprint reward and update the event/test expectations to the same unit.

- `packages/runner/src/runnerCastHeartbeat.ts:34-51`, `packages/shared/src/adapters/IChainClient.ts:34-58`, `packages/contracts/src/IClanWorld.sol:188-206` — Handwritten ABI tuples are stale after the `WorldState` layout change. `currentSeasonNumber` and `nextHeartbeatAtTick` were inserted before `nextHeartbeatAtTs`, but the runner and shared adapter still decode the old tuple. That can misread timestamps and break heartbeat gating/off-chain reads at deploy time. Suggested fix: update both manual ABIs in this PR, or replace them with generated ABI imports.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:1709-1718, 1948-1950, 518-546` — `_resolveBanditAttack()` checks `targetClanState`, then settles the target, but never re-checks whether that settlement killed the clan. If the target dies during pre-attack settlement, the current attacker still resolves a normal attack path against an already-dead target, while only the other attackers get `BanditTargetDied`. Suggested fix: re-check target liveness immediately after `_settleClan(targetClanId)` and escape/abort the current attacker too.

- `packages/contracts/src/ClanWorld.sol:551-573` — Target-death cleanup only releases defenders that already arrived and registered in `_clansmanDefendingRegion`. Defenders still traveling to a dead target keep an active `DefendBase` mission forever. Suggested fix: clear all matching `DefendBase` missions for `targetClanId`, not just the ones with an active region registration.

- `packages/contracts/src/ClanWorld.sol:1177-1182, 3402-3446` — Derived simulation does not mirror live defender bookkeeping. A simulated `DefendBase` arrival never registers a defender, and simulated death paths do not release defender registry state, yet `getClanFullView()` mixes simulated clan data with raw defender indexes. That can make derived views disagree with what real settlement would show. Suggested fix: either simulate defender-registry side effects too, or stop composing simulated state with raw defender indexes.

- `packages/contracts/src/IClanWorld.sol:603-604` and `packages/contracts/src/ClanWorld.sol:1755-1756` — The legacy `BlueprintAwarded` event remains in the interface but is never emitted; only `BlueprintEarned` fires now. That is a deploy/rollback compatibility risk for existing consumers. Suggested fix: dual-emit for one release or update consumers in lockstep and remove the dead legacy event.

- `packages/contracts/src/ClanWorld.sol:1548, 3473-3503` and `packages/contracts/src/IClanWorld.sol:455-473` — Several legacy bandit-facing fields are still exposed but now carry placeholder zeroes (`tier`, `attackAttemptsMade`, `maxAttemptsRemaining`, `projectedTargetLootValue`). That silently degrades downstream behavior instead of failing loudly. Suggested fix: either compute real values or version/remove the legacy fields and update consumers.

## LOW severity findings
- `packages/contracts/src/ClanWorld.sol:1501-1508` — `_simulateCompleteMission()` writes `cooldownEndsAtTs` from `block.timestamp`, so derived getters are wall-clock-sensitive rather than tick-deterministic. Two reads in the same tick can return different cooldowns.

- `packages/shared/src/mocks/clanWorldFixture.ts:76-92, 309-317` — The shared mock/demo bandit schema is still pre-Phase-9 (`CAMPING`, `tier`, old field names). That will drift further from the contract seam even if Solidity-side fixes land.

- `packages/contracts/src/ClanWorld.sol:3180-3202` — `getBanditTroop()` is kept as a compatibility shim to `getBandit()`, but there is no direct compatibility test covering that alias.

## Cross-cutting observations
- The integrated head tree has drifted materially from the documented bandit design: combat is now randomized per-defender roll, reward split is clan-based, legacy fields are partially stubbed, and there is still no heartbeat-driven target-selection/rampage path. The new tests mostly validate internal helpers via harnesses instead of proving the production heartbeat can drive the full game loop.

- I could not run Foundry locally because `forge` is not installed in this environment, so this review is based on static analysis of the `23f7f1a` tree plus parallel code-review passes.
