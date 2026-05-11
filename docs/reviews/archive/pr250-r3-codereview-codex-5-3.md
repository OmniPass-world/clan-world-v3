Reading additional input from stdin...
OpenAI Codex v0.125.0 (research preview)
--------
workdir: /home/claude/code/clan-world
model: gpt-5.3-codex
provider: openai
approval: never
sandbox: read-only
reasoning effort: high
reasoning summaries: none
session id: 019ddc0a-81aa-7901-a19c-4e5d3a9cd0c1
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output full review in requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for a multi-issue phase release PR.

PR: Phase 10 — Winter + Elimination (post fix-round merge)
Head SHA: 400463e

## Your task

This is a COHESIVE PHASE — sub-issues + fix-rounds already merged. Review the integrated whole. Look for:

1. CROSS-CUTTING bugs that surface at integration seams (race conditions, state machine inconsistencies, broken invariants across components, regressions introduced by fix-rounds themselves)
2. ARCHITECTURAL drift — does the phase deliver its stated goal?
3. SECURITY surface — auth, input validation, prompt injection, TOCTOU, resource leaks
4. DATA-flow correctness — schema consistency, idempotency, error paths
5. Integration risks — newly-added code's effect on existing code paths
6. Missing test coverage on integration seams
7. Specifically check that fix-round fixes didn't introduce NEW regressions

USE PARALLEL TOOL CALLS / SUB-AGENTS aggressively. Read changed files. Trace state machines. Don't skim.

## Output format

# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.

## HIGH severity findings
(real bugs with file:line + paragraph + suggested fix)

## MEDIUM severity findings
(should-fix; design quality, edge cases, ops)

## LOW severity findings
(defer-OK; nits, polish, follow-ups)

## Cross-cutting observations

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS BELOW.
---
---
diff --git a/docs/planning/clanworld_v4_spec.md b/docs/planning/clanworld_v4_spec.md
index b8296b4..8f4e5bd 100644
--- a/docs/planning/clanworld_v4_spec.md
+++ b/docs/planning/clanworld_v4_spec.md
@@ -823,7 +823,7 @@ This is a required bookkeeping part of the combat model.
 # 7. Winter Rules
 
 ## 7.1 Winter cadence
-Winter occurs every `110` ticks.
+Winter occurs every `110` ticks. The first winter opens at tick `110`, so ticks `[100, 110)` remain pre-winter preparation time.
 
 ## 7.2 Winter duration
 Winter lasts `10` ticks.
@@ -832,7 +832,7 @@ Winter lasts `10` ticks.
 During winter, each clan consumes:
 - wheat consumption = `2×` normal
 - fish consumption = `2×` normal
-- wood burn = `1e18` wood per base per tick
+- wood burn = `1e18` wood per base per tick, plus `0.5e18` wood per living clansman per tick
 
 ## 7.4 Winter farming rule
 During winter:
@@ -1211,4 +1211,3 @@ The following values are intentionally tunable without changing the core mechani
 - bandit spawn probability ramp
 - bandit reward magnitudes
 - season reward pot percentages in later versions
-
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..a4e9f2d 100644
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
@@ -1639,6 +1581,40 @@
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
@@ -1751,6 +1727,30 @@
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
@@ -1929,6 +1929,16 @@
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
@@ -2036,6 +2046,16 @@
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
@@ -2111,6 +2131,19 @@
       "outputs": [],
       "stateMutability": "nonpayable"
     },
+    {
+      "type": "function",
+      "name": "isWinter",
+      "inputs": [],
+      "outputs": [
+        {
+          "name": "",
+          "type": "bool",
+          "internalType": "bool"
+        }
+      ],
+      "stateMutability": "view"
+    },
     {
       "type": "function",
       "name": "mintClan",
@@ -2270,6 +2303,19 @@
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
@@ -2778,6 +2824,56 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "ClanColdShortage",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "tick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        },
+        {
+          "name": "woodShort",
+          "type": "uint256",
+          "indexed": false,
+          "internalType": "uint256"
+        }
+      ],
+      "anonymous": false
+    },
+    {
+      "type": "event",
+      "name": "ClanDied",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "tick",
+          "type": "uint64",
+          "indexed": false,
+          "internalType": "uint64"
+        },
+        {
+          "name": "reason",
+          "type": "string",
+          "indexed": false,
+          "internalType": "string"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "ClanEliminated",
@@ -2878,6 +2974,31 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "ClansmanColdDeath",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "csId",
+          "type": "uint32",
+          "indexed": false,
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
       "name": "ClansmanDiedFromCold",
@@ -3568,6 +3689,31 @@
       ],
       "anonymous": false
     },
+    {
+      "type": "event",
+      "name": "WallDegradedByCold",
+      "inputs": [
+        {
+          "name": "clanId",
+          "type": "uint32",
+          "indexed": true,
+          "internalType": "uint32"
+        },
+        {
+          "name": "newWallLevel",
+          "type": "uint8",
+          "indexed": false,
+          "internalType": "uint8"
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
       "name": "WallLevelChanged",
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..1f1cf62 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -38,6 +38,7 @@ import {
     RegionOccupant
 } from "./IClanWorld.sol";
 import {StubPool} from "./StubPool.sol";
+import {RNG} from "./lib/RNG.sol";
 import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
 
 /// @title ClanWorld
@@ -77,6 +78,9 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
+    /// @dev Caps winter crop boundary work; mintClan keeps the clan cap within this budget.
+    uint256 public constant MAX_CROP_TRANSITION_PER_TICK = 48;
+    uint256 private constant MAX_CLANS_FOR_CROP_TRANSITIONS = MAX_CROP_TRANSITION_PER_TICK / 2;
 
     // =========================================================================
     // CONSTRUCTOR
@@ -89,11 +93,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
         _world.currentSeasonNumber = 1;
         _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
-        // i.e. ticks [100, 110)
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
         _world.winterActive = false;
         _treasury.treasuryOwner = msg.sender;
         _nextClanId = 1;
@@ -371,6 +372,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         for (uint64 tick = fromTick; tick < curTick; tick++) {
             // 1. Apply upkeep for this tick
             _applyUpkeep(clan, tick);
+            if (clan.clanState == ClanState.DEAD) break;
 
             // 2. Wheat plot regrow check (lazy, per tick)
             for (uint256 pi = 0; pi < 2; pi++) {
@@ -389,16 +391,29 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             }
         }
 
+        if (curTick > fromTick && !_isWinterActiveAt(curTick) && _isWinterActiveAt(curTick - 1)) {
+            clan.coldDamage = 0;
+        }
+
         clan.lastSettledTick = curTick;
         emit ClanSettled(clanId, curTick);
     }
 
-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
     function _applyUpkeep(Clan storage clan, uint64 tick) internal {
+        bool winter = _isWinterActiveAt(tick);
+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
+            clan.coldDamage = 0;
+        }
+
         if (clan.livingClansmen == 0) return;
 
         uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
         uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
+        if (winter) {
+            wheatNeeded = wheatNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
+            fishNeeded = fishNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
+        }
 
         bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
         bool hadEnoughFish = clan.vaultFish >= fishNeeded;
@@ -422,6 +437,164 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             clan.starvationStartsAtTick = 0;
             emit ClanStarvationChanged(clan.clanId, false, tick);
         }
+
+        uint8 livingBeforeStarvation = clan.livingClansmen;
+        if (winter && starving) {
+            (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
+            uint64 effectiveStarvationStartsAtTick =
+                clan.starvationStartsAtTick > winterStartsAtTick ? clan.starvationStartsAtTick : winterStartsAtTick;
+            if (effectiveStarvationStartsAtTick < tick) {
+                _killNextClansmanFromStarvation(clan, tick);
+            }
+        }
+        if (clan.clanState == ClanState.DEAD) return;
+
+        if (winter) {
+            uint256 woodNeeded = ClanWorldConstants.WINTER_WOOD_BURN_PER_BASE + uint256(livingBeforeStarvation)
+                * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
+            if (clan.vaultWood >= woodNeeded) {
+                clan.vaultWood -= woodNeeded;
+            } else {
+                uint256 woodShort = woodNeeded - clan.vaultWood;
+                clan.vaultWood = 0;
+                uint16 oldColdDamage = clan.coldDamage;
+                if (clan.coldDamage < type(uint16).max) {
+                    clan.coldDamage += 1;
+                }
+                emit ClanColdShortage(clan.clanId, tick, woodShort);
+                _applyColdDamageConsequence(clan, tick, oldColdDamage);
+            }
+        }
+    }
+
+    function _applyColdDamageConsequence(Clan storage clan, uint64 tick, uint16 oldColdDamage) internal {
+        uint16 newColdDamage = clan.coldDamage;
+        if (newColdDamage == oldColdDamage) return;
+
+        if (clan.wallLevel > 0) {
+            if (
+                newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
+                    <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
+            ) return;
+
+            clan.wallLevel--;
+            emit WallDegradedByCold(clan.clanId, clan.wallLevel, tick);
+            return;
+        }
+
+        if (
+            newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
+                <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
+        ) return;
+
+        _killRandomClansmanFromCold(clan, tick, newColdDamage);
+    }
+
+    function _killRandomClansmanFromCold(Clan storage clan, uint64 tick, uint16 coldDamage) internal {
+        if (clan.livingClansmen == 0) return;
+
+        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
+        uint256 livingCount = 0;
+        for (uint256 i = 0; i < csIds.length; i++) {
+            if (_clansmen[csIds[i]].state != ClansmanState.DEAD) {
+                livingCount++;
+            }
+        }
+        if (livingCount == 0) return;
+
+        uint256 pick = RNG.rngBounded(
+            _world.currentTickSeed,
+            RNG.DOMAIN_COLD_DAMAGE,
+            uint256(keccak256(abi.encodePacked(clan.clanId, tick, coldDamage))),
+            livingCount
+        );
+
+        uint256 seen = 0;
+        for (uint256 i = 0; i < csIds.length; i++) {
+            Clansman storage cs = _clansmen[csIds[i]];
+            if (cs.state == ClansmanState.DEAD) continue;
+            if (seen != pick) {
+                seen++;
+                continue;
+            }
+
+            _markClansmanDeadFromCold(clan, cs, tick);
+            return;
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
+    function _markClansmanDeadFromCold(Clan storage clan, Clansman storage cs, uint64 tick) internal {
+        _markClansmanDead(clan, cs);
+
+        emit ClansmanColdDeath(clan.clanId, cs.clansmanId, tick);
+        if (clan.livingClansmen == 0) {
+            _markClanDead(clan.clanId, "cold", tick);
+        }
+    }
+
+    function _markClansmanDead(Clan storage clan, Clansman storage cs) internal {
+        if (cs.state == ClansmanState.DEAD) return;
+
+        cs.state = ClansmanState.DEAD;
+        if (clan.livingClansmen > 0) {
+            clan.livingClansmen--;
+        }
+
+        Mission storage m = _missions[cs.clansmanId];
+        if (m.active) {
+            if (m.action == ActionType.DefendBase) {
+                _clearDefender(cs.clansmanId);
+            }
+            m.active = false;
+        }
+    }
+
+    function _markClanDead(uint32 clanId) internal {
+        _markClanDead(clanId, "unknown", _world.currentTick);
+    }
+
+    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {
+        Clan storage clan = _clans[clanId];
+        if (clan.clanId == 0 || clan.clanState == ClanState.DEAD) return;
+
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
+            _clansmen[csIds[i]].state = ClansmanState.DEAD;
+            Mission storage m = _missions[csIds[i]];
+            if (m.active) {
+                if (m.action == ActionType.DefendBase) {
+                    _clearDefender(csIds[i]);
+                }
+                m.active = false;
+            }
+        }
+
+        emit ClanEliminated(clanId, tick);
+        emit ClanDied(clanId, tick, reason);
     }
 
     /// @dev Check if a clan is currently starving (lazy read).
@@ -638,6 +811,11 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         }
 
         WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
+        if (plot.state == WheatPlotState.WinterLocked) {
+            // Winter-locked plots cannot be harvested; queued missions end with no yield.
+            _completeMission(cs, m);
+            return;
+        }
         if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
             // Plot not ready — worker waits
             _completeMission(cs, m);
@@ -942,39 +1120,89 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             _world.currentSeasonNumber += 1;
             _world.seasonStartTick = _world.seasonEndTick;
             _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
-            // reset winter timers for new season
-            _world.winterActive = false;
-            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
-                - ClanWorldConstants.WINTER_DURATION_TICKS;
-            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
         }
 
         // --- winter transitions (timer only; mechanics = Phase 10) ---
-        if (
-            !_world.winterActive && newTick >= _world.winterStartsAtTick
-                && _world.winterStartsAtTick < _world.seasonEndTick
-        ) {
-            _world.winterActive = true;
-            emit WinterStarted(newTick);
-        }
-        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
-            _world.winterActive = false;
-            emit WinterEnded(newTick);
-            // schedule next winter cycle within this season
-            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
-                - ClanWorldConstants.WINTER_DURATION_TICKS;
-            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
-            if (nextWinterStart < _world.seasonEndTick) {
-                _world.winterStartsAtTick = nextWinterStart;
-                _world.winterEndsAtTick = nextWinterEnd;
-            } else {
-                // no more winters this season; sentinel = seasonEndTick so guard never fires
-                _world.winterStartsAtTick = _world.seasonEndTick;
-                _world.winterEndsAtTick = _world.seasonEndTick;
+        bool wasWinter = _isWinterActiveAt(closedTick);
+        bool nowWinter = _isWinterActiveAt(newTick);
+        if (!wasWinter && nowWinter) {
+            _lockWheatPlotsForWinter();
+            emit WinterStarted(_winterEventTick(newTick));
+        }
+        if (wasWinter && !nowWinter) {
+            _restartWheatPlotsAfterWinter(newTick);
+            emit WinterEnded(_winterEventTick(newTick));
+        }
+    }
+
+    function _lockWheatPlotsForWinter() internal {
+        uint256 transitions;
+        for (uint256 i = 0; i < _allClanIds.length; i++) {
+            uint32 clanId = _allClanIds[i];
+            for (uint256 pi = 0; pi < 2; pi++) {
+                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
+                WheatPlot storage plot = _wheatPlots[clanId][pi];
+                plot.state = WheatPlotState.WinterLocked;
+                plot.remainingWheat = 0;
+                plot.regrowUntilTick = 0;
+                transitions++;
             }
         }
     }
 
+    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
+        uint256 transitions;
+        for (uint256 i = 0; i < _allClanIds.length; i++) {
+            uint32 clanId = _allClanIds[i];
+            for (uint256 pi = 0; pi < 2; pi++) {
+                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
+                WheatPlot storage plot = _wheatPlots[clanId][pi];
+                if (plot.state == WheatPlotState.WinterLocked) {
+                    plot.state = WheatPlotState.Regrowing;
+                    plot.remainingWheat = 0;
+                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
+                }
+                transitions++;
+            }
+        }
+    }
+
+    function _winterEventTick(uint64 tick) internal pure returns (uint64) {
+        return tick;
+    }
+
+    function _isWinterActiveAt(uint64 tick) internal pure returns (bool) {
+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
+            return false;
+        }
+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
+        return elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
+    }
+
+    function _winterWindowForTick(uint64 tick)
+        internal
+        pure
+        returns (bool active, uint64 startsAtTick, uint64 endsAtTick)
+    {
+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
+            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
+            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
+            return (false, startsAtTick, endsAtTick);
+        }
+
+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
+        uint64 cycleIndex = elapsed / ClanWorldConstants.WINTER_PERIOD_TICKS;
+        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
+        active = elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
+        startsAtTick = active ? cycleStart : cycleStart + ClanWorldConstants.WINTER_PERIOD_TICKS;
+        endsAtTick = startsAtTick + ClanWorldConstants.WINTER_DURATION_TICKS;
+    }
+
+    function _worldStateView() internal view returns (WorldState memory ws) {
+        ws = _world;
+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
+    }
+
     /// @notice Public settlement trigger — lazily settle a clan.
     function settleClan(uint32 clanId) external override nonReentrant {
         _settleClan(clanId);
@@ -1003,6 +1231,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
         require(to != address(0), "ClanWorld: zero address");
         require(_allClanIds.length < 12, "ClanWorld: max clans");
+        require(_allClanIds.length < MAX_CLANS_FOR_CROP_TRANSITIONS, "ClanWorld: clan cap");
         clanId = _nextClanId++;
         iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
 
@@ -1039,17 +1268,22 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         clan.vaultWheat = 20e18;
         clan.vaultFish = 2e18;
 
+        WheatPlotState startingPlotState =
+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
+        uint256 startingWheat =
+            startingPlotState == WheatPlotState.WinterLocked ? 0 : ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
+
         // Wheat plots
         _wheatPlots[clanId][0] = WheatPlot({
-            state: WheatPlotState.Harvestable,
+            state: startingPlotState,
             region: ClanWorldConstants.REGION_WEST_FARMS,
-            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
+            remainingWheat: startingWheat,
             regrowUntilTick: 0
         });
         _wheatPlots[clanId][1] = WheatPlot({
-            state: WheatPlotState.Harvestable,
+            state: startingPlotState,
             region: ClanWorldConstants.REGION_EAST_FARMS,
-            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
+            remainingWheat: startingWheat,
             regrowUntilTick: 0
         });
 
@@ -1090,7 +1324,6 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
         // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
-        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
         {
             uint64 lastSettled = _clans[clanId].lastSettledTick;
             if (_world.currentTick > lastSettled + 200) {
@@ -1098,7 +1331,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
                 for (uint256 i = 0; i < orders.length; i++) {
                     results[i] = OrderResult({
                         clansmanId: orders[i].clansmanId,
-                        status: StatusCode.ERR_INVALID_ACTION,
+                        status: StatusCode.ERR_MUST_SETTLE_FIRST,
                         cooldownEndsAtTs: 0,
                         missionNonce: 0
                     });
@@ -1111,6 +1344,17 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         _settleClan(clanId);
 
         results = new OrderResult[](orders.length);
+        if (clan.clanState == ClanState.DEAD) {
+            for (uint256 i = 0; i < orders.length; i++) {
+                results[i] = OrderResult({
+                    clansmanId: orders[i].clansmanId,
+                    status: StatusCode.ERR_CLAN_DEAD,
+                    cooldownEndsAtTs: 0,
+                    missionNonce: 0
+                });
+            }
+            return results;
+        }
 
         for (uint256 i = 0; i < orders.length; i++) {
             results[i] = _processOrder(clanId, clan, orders[i]);
@@ -1646,6 +1890,9 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             ) {
                 return StatusCode.ERR_INVALID_REGION;
             }
+            if (_isWinterActiveAt(_world.currentTick)) {
+                return StatusCode.ERR_WINTER_LOCKED;
+            }
         }
 
         if (action == ActionType.DefendBase) {
@@ -1761,7 +2008,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // =========================================================================
 
     function getWorldState() external view override returns (WorldState memory) {
-        return _world;
+        return _worldStateView();
     }
 
     function getTreasuryState() external view override returns (TreasuryState memory) {
@@ -1793,6 +2040,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
     }
 
+    function isWinter() external view override returns (bool) {
+        return _isWinterActiveAt(_world.currentTick);
+    }
+
     function getActionDuration(ActionType action) public pure override returns (uint64) {
         if (
             action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
@@ -1976,18 +2227,19 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             });
         }
 
+        WorldState memory ws = _worldStateView();
         return WorldSnapshot({
-            currentTick: _world.currentTick,
-            seasonStartTick: _world.seasonStartTick,
-            seasonEndTick: _world.seasonEndTick,
-            seasonFinalized: _world.seasonFinalized,
-            currentSeasonNumber: _world.currentSeasonNumber,
-            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
-            winterActive: _world.winterActive,
-            winterStartsAtTick: _world.winterStartsAtTick,
-            winterEndsAtTick: _world.winterEndsAtTick,
-            activeBanditId: _world.activeBanditId,
-            currentTickSeed: _world.currentTickSeed,
+            currentTick: ws.currentTick,
+            seasonStartTick: ws.seasonStartTick,
+            seasonEndTick: ws.seasonEndTick,
+            seasonFinalized: ws.seasonFinalized,
+            currentSeasonNumber: ws.currentSeasonNumber,
+            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
+            winterActive: ws.winterActive,
+            winterStartsAtTick: ws.winterStartsAtTick,
+            winterEndsAtTick: ws.winterEndsAtTick,
+            activeBanditId: ws.activeBanditId,
+            currentTickSeed: ws.currentTickSeed,
             leaderboard: lb
         });
     }
diff --git a/packages/contracts/src/ClanWorldStub.sol b/packages/contracts/src/ClanWorldStub.sol
index 36ce29b..80dc211 100644
--- a/packages/contracts/src/ClanWorldStub.sol
+++ b/packages/contracts/src/ClanWorldStub.sol
@@ -51,9 +51,8 @@ contract ClanWorldStub is IClanWorld {
         _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
         _world.currentSeasonNumber = 1;
         _world.nextHeartbeatAtTick = _world.currentTick + 1;
-        _world.winterStartsAtTick =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
         _world.winterActive = false;
 
         _treasury.woodToken = tokens[0];
@@ -80,6 +79,14 @@ contract ClanWorldStub is IClanWorld {
         _world.currentTick += 1;
         _world.nextHeartbeatAtTick = _world.currentTick + 1;
         _world.nextHeartbeatAtTs = uint64(block.timestamp);
+        bool wasWinter = _isWinterActiveAt(closed);
+        bool nowWinter = _isWinterActiveAt(_world.currentTick);
+        if (!wasWinter && nowWinter) {
+            emit WinterStarted(_winterEventTick(_world.currentTick));
+        }
+        if (wasWinter && !nowWinter) {
+            emit WinterEnded(_winterEventTick(_world.currentTick));
+        }
         emit TickAdvanced(closed, _world.currentTick, bytes32(0));
     }
 
@@ -130,7 +137,7 @@ contract ClanWorldStub is IClanWorld {
     // -------------------------------------------------------------------------
 
     function getWorldState() external view override returns (WorldState memory) {
-        return _world;
+        return _worldStateView();
     }
 
     function getTreasuryState() external view override returns (TreasuryState memory) {
@@ -207,6 +214,10 @@ contract ClanWorldStub is IClanWorld {
         return (0, 0, 0);
     }
 
+    function isWinter() external view override returns (bool) {
+        return _isWinterActiveAt(_world.currentTick);
+    }
+
     function getActionDuration(ActionType) external pure override returns (uint64) {
         return 0;
     }
@@ -288,16 +299,17 @@ contract ClanWorldStub is IClanWorld {
     // -------------------------------------------------------------------------
 
     function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
+        WorldState memory ws = _worldStateView();
         return WorldSnapshot({
-            currentTick: _world.currentTick,
-            seasonStartTick: _world.seasonStartTick,
-            seasonEndTick: _world.seasonEndTick,
+            currentTick: ws.currentTick,
+            seasonStartTick: ws.seasonStartTick,
+            seasonEndTick: ws.seasonEndTick,
             seasonFinalized: false,
-            currentSeasonNumber: _world.currentSeasonNumber,
-            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
-            winterActive: _world.winterActive,
-            winterStartsAtTick: _world.winterStartsAtTick,
-            winterEndsAtTick: _world.winterEndsAtTick,
+            currentSeasonNumber: ws.currentSeasonNumber,
+            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
+            winterActive: ws.winterActive,
+            winterStartsAtTick: ws.winterStartsAtTick,
+            winterEndsAtTick: ws.winterEndsAtTick,
             activeBanditId: 0,
             currentTickSeed: bytes32(0),
             leaderboard: new LeaderboardEntry[](0)
@@ -361,4 +373,40 @@ contract ClanWorldStub is IClanWorld {
     function getRegionPopulation(uint8) external pure override returns (RegionOccupant[] memory) {
         return new RegionOccupant[](0);
     }
+
+    function _isWinterActiveAt(uint64 tick) internal pure returns (bool) {
+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
+            return false;
+        }
+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
+        return elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
+    }
+
+    function _winterEventTick(uint64 tick) internal pure returns (uint64) {
+        return tick;
+    }
+
+    function _winterWindowForTick(uint64 tick)
+        internal
+        pure
+        returns (bool active, uint64 startsAtTick, uint64 endsAtTick)
+    {
+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
+            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
+            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
+            return (false, startsAtTick, endsAtTick);
+        }
+
+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
+        uint64 cycleIndex = elapsed / ClanWorldConstants.WINTER_PERIOD_TICKS;
+        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
+        active = elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
+        startsAtTick = active ? cycleStart : cycleStart + ClanWorldConstants.WINTER_PERIOD_TICKS;
+        endsAtTick = startsAtTick + ClanWorldConstants.WINTER_DURATION_TICKS;
+    }
+
+    function _worldStateView() internal view returns (WorldState memory ws) {
+        ws = _world;
+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
+    }
 }
diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
index 2b80fbe..f39efad 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -26,8 +26,10 @@ pragma solidity ^0.8.34;
 library ClanWorldConstants {
     // World cadence
     uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
-    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
+    // First winter opens at tick 110; ticks [100,110) remain pre-winter runway.
+    uint64 internal constant WINTER_START_TICK = 110;
     uint64 internal constant WINTER_DURATION_TICKS = 10;
+    uint64 internal constant WINTER_PERIOD_TICKS = 110;
     uint64 internal constant SEASON_DURATION_TICKS = 360;
 
     // Bandit cadence
@@ -60,8 +62,11 @@ library ClanWorldConstants {
     // Upkeep
     uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
     uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
+    uint256 internal constant WINTER_WOOD_BURN_PER_CLANSMAN = 5e17; // 0.5
     uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
     uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
+    uint16 internal constant COLD_DAMAGE_PER_WALL_DEGRADATION = 2;
+    uint16 internal constant COLD_DAMAGE_PER_CLANSMAN_DEATH = 2;
 
     // Wheat plots
     uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
@@ -176,7 +181,9 @@ enum StatusCode {
     ERR_NO_ACTIVE_BANDIT,
     ERR_SEASON_ENDED,
     ERR_NOT_ENOUGH_GOLD,
-    ERR_CARRY_FULL
+    ERR_CARRY_FULL,
+    ERR_WINTER_LOCKED,
+    ERR_MUST_SETTLE_FIRST
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
@@ -499,7 +506,11 @@ interface IClanWorldEvents {
     );
     event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
     event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
+    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
     event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
+    event ClanColdShortage(uint32 indexed clanId, uint64 tick, uint256 woodShort);
+    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint64 tick);
+    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint64 tick);
 
     // ----- missions -----
     event MissionAssigned(
@@ -710,6 +721,9 @@ interface IClanWorld is IClanWorldEvents {
         view
         returns (uint64 submitted, uint64 executes, uint64 settles);
 
+    /// @notice True iff currentTick is inside the recurring winter window.
+    function isWinter() external view returns (bool);
+
     function getActionDuration(ActionType action) external pure returns (uint64);
 
     function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
diff --git a/packages/contracts/src/lib/RNG.sol b/packages/contracts/src/lib/RNG.sol
index 5536537..e15dd07 100644
--- a/packages/contracts/src/lib/RNG.sol
+++ b/packages/contracts/src/lib/RNG.sol
@@ -9,6 +9,7 @@ library RNG {
     uint256 internal constant DOMAIN_MARKET_NOISE = uint256(keccak256("clanworld.market.noise.v1"));
     uint256 internal constant DOMAIN_WEATHER = uint256(keccak256("clanworld.weather.v1"));
     uint256 internal constant DOMAIN_FAIR_ITERATION = uint256(keccak256("clanworld.fair.iteration.v1"));
+    uint256 internal constant DOMAIN_COLD_DAMAGE = uint256(keccak256("cold_damage"));
 
     uint256 internal constant MAX_SHUFFLE_N = 64;
 
diff --git a/packages/contracts/test/ClanWorld.t.sol b/packages/contracts/test/ClanWorld.t.sol
index 92781d3..fe31f01 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -48,6 +48,35 @@ contract ClanWorldTestHarness is ClanWorld {
     function killClansman(uint32 csId) external {
         _clansmen[csId].state = ClansmanState.DEAD;
     }
+
+    function setClanUpkeepState(
+        uint32 clanId,
+        uint64 lastSettledTick,
+        uint256 vaultWood,
+        uint256 vaultWheat,
+        uint256 vaultFish,
+        uint16 coldDamage
+    ) external {
+        Clan storage clan = _clans[clanId];
+        clan.lastSettledTick = lastSettledTick;
+        clan.vaultWood = vaultWood;
+        clan.vaultWheat = vaultWheat;
+        clan.vaultFish = vaultFish;
+        clan.coldDamage = coldDamage;
+    }
+
+    function setClanWallLevel(uint32 clanId, uint8 wallLevel) external {
+        _clans[clanId].wallLevel = wallLevel;
+    }
+
+    function setClanStarvationStartsAtTick(uint32 clanId, uint64 starvationStartsAtTick) external {
+        _clans[clanId].starvationStartsAtTick = starvationStartsAtTick;
+    }
+
+    function setClanIronAndGold(uint32 clanId, uint256 vaultIron, uint256 goldBalance) external {
+        _clans[clanId].vaultIron = vaultIron;
+        _clans[clanId].goldBalance = goldBalance;
+    }
 }
 
 contract ClanWorldTest is Test {
@@ -125,6 +154,42 @@ contract ClanWorldTest is Test {
         world.heartbeat();
     }
 
+    function _advanceToTick(uint64 targetTick) internal {
+        while (world.getWorldState().currentTick < targetTick) {
+            _advanceTick();
+        }
+    }
+
+    function _countLogs(Vm.Log[] memory logs, bytes32 eventSig) internal pure returns (uint256 count) {
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (logs[i].topics.length > 0 && logs[i].topics[0] == eventSig) {
+                count++;
+            }
+        }
+    }
+
+    function _assertClanDiedLog(
+        Vm.Log[] memory logs,
+        uint32 expectedClanId,
+        uint64 expectedTick,
+        string memory expectedReason
+    ) internal {
+        bytes32 eventSig = keccak256("ClanDied(uint32,uint64,string)");
+        bytes32 expectedClanTopic = bytes32(uint256(expectedClanId));
+        for (uint256 i = 0; i < logs.length; i++) {
+            if (logs[i].topics.length < 2 || logs[i].topics[0] != eventSig || logs[i].topics[1] != expectedClanTopic) {
+                continue;
+            }
+
+            (uint64 tick, string memory reason) = abi.decode(logs[i].data, (uint64, string));
+            if (tick == expectedTick && keccak256(bytes(reason)) == keccak256(bytes(expectedReason))) {
+                return;
+            }
+        }
+
+        fail("expected ClanDied log");
+    }
+
     function _mintClan() internal returns (uint32 clanId) {
         vm.prank(elder);
         (clanId,) = world.mintClan(elder);
@@ -488,10 +553,10 @@ contract ClanWorldTest is Test {
     }
 
     // -------------------------------------------------------------------------
-    // Test B: submitClanOrders returns ERR_INVALID_ACTION when clan is >200 ticks behind
+    // Test B: submitClanOrders returns ERR_MUST_SETTLE_FIRST when clan is >200 ticks behind
     // -------------------------------------------------------------------------
 
-    function test_submitClanOrders_reverts_when_clan_too_far_behind() public {
+    function test_submitClanOrders_returnsMustSettleFirst_when_clan_too_far_behind() public {
         uint32 clanId = _mintClan();
         ClanFullView memory view_ = world.getClanFullView(clanId);
         uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
@@ -501,7 +566,7 @@ contract ClanWorldTest is Test {
             _advanceTick();
         }
 
-        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
+        // submitClanOrders should return ERR_MUST_SETTLE_FIRST
         // without reverting, for every order in the batch
         ClanOrder[] memory orders = new ClanOrder[](1);
         orders[0] = ClanOrder({
@@ -519,9 +584,11 @@ contract ClanWorldTest is Test {
         assertEq(results.length, 1, "should return one result");
         assertEq(
             uint8(results[0].status),
-            uint8(StatusCode.ERR_INVALID_ACTION),
-            "clan >200 ticks behind must return ERR_INVALID_ACTION (settle-first guard)"
+            uint8(StatusCode.ERR_MUST_SETTLE_FIRST),
+            "clan >200 ticks behind must return ERR_MUST_SETTLE_FIRST"
         );
+        assertEq(uint8(StatusCode.ERR_WINTER_LOCKED), 28, "existing status indices must remain stable");
+        assertEq(uint8(StatusCode.ERR_MUST_SETTLE_FIRST), 29, "new status code must be appended");
     }
 
     // -------------------------------------------------------------------------
@@ -1814,7 +1881,11 @@ contract ClanWorldTest is Test {
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
@@ -1920,56 +1991,462 @@ contract ClanWorldTest is Test {
         assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
         assertEq(ws.seasonStartTick, 0);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
-        assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
-        );
+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
         assertFalse(ws.winterActive);
+        assertFalse(world.isWinter());
+    }
+
+    function test_worldSnapshot_initialWinterStartsAtTick() public view {
+        WorldSnapshot memory snapshot = world.getWorldSnapshot();
+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
+        assertEq(snapshot.winterStartsAtTick, 110);
     }
 
     function test_winter_onset() public {
-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
-        // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
-        uint64 winterStart =
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
-        for (uint64 i = 0; i < winterStart - 1; i++) {
-            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
-            world.heartbeat();
-        }
-        // currentTick == 99; next heartbeat opens tick 100 and should emit WinterStarted(100)
+        _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart - 1);
+
         vm.recordLogs();
-        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
-        world.heartbeat();
+        _advanceTick();
         Vm.Log[] memory logs = vm.getRecordedLogs();
 
-        bytes32 winterSig = keccak256("WinterStarted(uint64)");
-        bool foundWinterStarted = false;
-        for (uint256 i = 0; i < logs.length; i++) {
-            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
-                foundWinterStarted = true;
-                break;
-            }
-        }
-        assertTrue(foundWinterStarted, "WinterStarted event should have been emitted at tick 100");
-        assertTrue(world.getWorldState().winterActive, "winter should be active");
-        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
+        assertEq(_countLogs(logs, keccak256("WinterStarted(uint64)")), 1, "WinterStarted emits once");
+        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
+
+        _advanceTick();
+        WorldState memory ws = world.getWorldState();
+        assertTrue(world.isWinter(), "winter should be active past start tick");
+        assertTrue(ws.winterActive, "world state should report winter active");
+        assertEq(ws.winterStartsAtTick, winterStart);
+        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
     }
 
     function test_winter_end_and_next_cycle() public {
-        // Advance past first winter end tick (= 110)
-        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
-        for (uint64 i = 0; i <= winterEnd; i++) {
-            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
-            world.heartbeat();
-        }
+        _mintClan();
+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
+        _advanceToTick(winterEnd - 1);
+
+        vm.recordLogs();
+        _advanceTick();
+        _advanceTick();
+        _advanceTick();
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+
         WorldState memory ws = world.getWorldState();
         assertFalse(ws.winterActive, "winter should be over");
-        // next winter at [210, 220)
+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
+        assertEq(_countLogs(logs, keccak256("WinterEnded(uint64)")), 1, "WinterEnded emits once");
+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
+    }
+
+    function test_winter_restarts_after_full_period() public {
+        _mintClan();
+        uint64 nextWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS;
+        _advanceToTick(nextWinterStart - 1);
+
+        vm.recordLogs();
+        _advanceTick();
+        _advanceTick();
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+
+        assertEq(_countLogs(logs, keccak256("WinterStarted(uint64)")), 1, "next WinterStarted emits once");
+        assertTrue(world.isWinter(), "winter should be active in next period");
+        assertEq(world.getWorldState().currentTick, nextWinterStart + 1);
+    }
+
+    function test_winter_cropTransitions_lockThenRestartRegrow() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId1 = _mintClan();
+        uint32 clanId2 = _mintClan();
+        harness.setClanUpkeepState(clanId1, 0, 1000e18, 1000e18, 1000e18, 0);
+        harness.setClanUpkeepState(clanId2, 0, 1000e18, 1000e18, 1000e18, 0);
+
+        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
+        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
+        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
+
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart - 1);
+        _advanceTick();
+
+        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
+        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
+        assertEq(west1.remainingWheat, 0, "winter lock clears remaining wheat");
+        assertEq(east1.remainingWheat, 0, "winter lock clears all plots");
+        assertEq(west1.regrowUntilTick, 0, "winter lock clears regrow tick");
+        assertEq(east2.regrowUntilTick, 0, "winter lock clears all regrow ticks");
+
+        OrderResult[] memory results =
+            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
+        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
+
+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
+        _advanceToTick(winterEnd - 1);
+        _advanceTick();
+
+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
+        (west1, east1) = world.getWheatPlots(clanId1);
+        (west2, east2) = world.getWheatPlots(clanId2);
+        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
+        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
+        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
+        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
+
+        _advanceToTick(expectedRegrowUntil + 1);
+        world.settleClan(clanId1);
+        world.settleClan(clanId2);
+
+        (west1, east1) = world.getWheatPlots(clanId1);
+        (west2, east2) = world.getWheatPlots(clanId2);
+        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
+        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
+        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
+        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
+        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
+        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
+    }
+
+    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart - 2);
+
+        OrderResult[] memory results =
+            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
+        Mission memory queuedMission = world.getActiveMission(1);
+
+        _advanceToTick(queuedMission.settlesAtTick + 1);
+
+        Clansman memory cs = world.getClansman(1);
+        Mission memory mission = world.getActiveMission(1);
+        (WheatPlot memory west,) = world.getWheatPlots(clanId);
+        assertFalse(mission.active, "locked harvest mission should complete");
+        assertEq(cs.carryWheat, 0, "locked harvest should yield no wheat");
+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
+        assertEq(west.remainingWheat, 0, "winter start clears locked plot");
+    }
+
+    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 1);
+
+        harness.setClanWallLevel(clanId, 2);
+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
+
+        world.settleClan(clanId);
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
+        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
+        assertEq(clan.vaultWood, 97e18, "winter wood burn should include base and per clansman");
+        assertEq(clan.coldDamage, 0, "sufficient wood should avoid cold damage");
+        assertEq(clan.wallLevel, 2, "sufficient wood should not degrade wall");
+        assertEq(clan.livingClansmen, 4, "sufficient wood should not kill clansmen");
+    }
+
+    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 1);
+
+        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.ClanColdShortage(clanId, winterStart, 2e18);
+        world.settleClan(clanId);
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
+        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
+    }
+
+    function test_coldDamage_degradesWallEveryTwoShortages() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 1);
+
+        harness.setClanWallLevel(clanId, 2);
+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
+
+        vm.expectEmit(true, false, false, true);
+        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, winterStart);
+        world.settleClan(clanId);
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.coldDamage, 2, "cold damage should increment");
+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
+        assertEq(clan.livingClansmen, 4, "wall should absorb cold before clansmen die");
+    }
+
+    function test_coldDamage_zeroWallKillsOneClansmanEveryTwoShortages() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 1);
+
+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
+
+        vm.recordLogs();
+        world.settleClan(clanId);
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.coldDamage, 2, "cold damage should hit death threshold");
+        assertEq(clan.wallLevel, 0, "wall should remain clamped at zero");
+        assertEq(clan.livingClansmen, 3, "one clansman should die from cold");
+        assertEq(_countLogs(logs, keccak256("ClansmanColdDeath(uint32,uint32,uint64)")), 1, "cold death should emit");
+
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        uint256 deadCount = 0;
+        for (uint256 i = 0; i < view_.clansmen.length; i++) {
+            if (view_.clansmen[i].clansman.clansman.state == ClansmanState.DEAD) {
+                deadCount++;
+            }
+        }
+        assertEq(deadCount, 1, "exactly one stored clansman should be dead");
+    }
+
+    function test_pre_winter_starver_dies_in_winter_at_same_cadence() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 preWinterStarver = _mintClan();
+        uint32 winterStarver = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 2);
+
+        harness.setClanUpkeepState(preWinterStarver, winterStart, 100e18, 0, 0, 0);
+        harness.setClanStarvationStartsAtTick(preWinterStarver, winterStart - 5);
+        harness.setClanUpkeepState(winterStarver, winterStart, 100e18, 0, 0, 0);
+
+        world.settleClan(preWinterStarver);
+        world.settleClan(winterStarver);
+
+        Clan memory preWinterClan = world.getClan(preWinterStarver);
+        Clan memory winterClan = world.getClan(winterStarver);
+        assertEq(preWinterClan.livingClansmen, 3, "pre-winter starver should skip first winter tick death");
+        assertEq(winterClan.livingClansmen, 3, "fresh winter starver should match pre-winter cadence");
+    }
+
+    function test_winter_starvationWoodBurnUsesPreDeathLivingCount() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 2);
+
+        harness.setClanUpkeepState(clanId, winterStart + 1, 25e17, 0, 0, 0);
+        harness.setClanStarvationStartsAtTick(clanId, winterStart);
+
+        world.settleClan(clanId);
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.livingClansmen, 3, "starvation should kill one clansman");
+        assertEq(clan.vaultWood, 0, "wood should be consumed after shortage");
+        assertEq(clan.coldDamage, 1, "wood burn should use the pre-starvation living count");
+    }
+
+    function test_winterColdDamageResetIsLazyPerClanAfterPartialSettlement() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 partiallySettledClan = _mintClan();
+        uint32 neverSettledClan = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        uint64 winterEnd = winterStart + ClanWorldConstants.WINTER_DURATION_TICKS;
+        _advanceToTick(winterStart + 3);
+
+        harness.setClanWallLevel(partiallySettledClan, 10);
+        harness.setClanWallLevel(neverSettledClan, 10);
+        harness.setClanUpkeepState(partiallySettledClan, winterStart, 0, 100e18, 100e18, 0);
+        harness.setClanUpkeepState(neverSettledClan, winterStart, 0, 100e18, 100e18, 0);
+
+        world.settleClan(partiallySettledClan);
+        Clan memory partialMidWinter = world.getClan(partiallySettledClan);
+        assertEq(partialMidWinter.wallLevel, 9, "three shortages should have degraded one wall level");
+        assertEq(partialMidWinter.coldDamage, 3, "mid-winter settlement should preserve accrued cold");
+
+        _advanceToTick(winterEnd);
+        world.settleClan(partiallySettledClan);
+        world.settleClan(neverSettledClan);
+
+        Clan memory partialAfterWinter = world.getClan(partiallySettledClan);
+        Clan memory neverAfterWinter = world.getClan(neverSettledClan);
         assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
+            partialAfterWinter.wallLevel, neverAfterWinter.wallLevel, "partial and lazy settlement should converge"
         );
+        assertEq(partialAfterWinter.wallLevel, 5, "ten shortages should degrade five wall levels");
+        assertEq(partialAfterWinter.coldDamage, 0, "partial clan cold damage resets after crossing winter end");
+        assertEq(neverAfterWinter.coldDamage, 0, "lazy clan cold damage resets after crossing winter end");
+    }
+
+    function test_winterMintedClanStartsWithLockedEmptyWheatPlots() public {
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart);
+
+        uint32 clanId = _mintClan();
+        ClanFullView memory view_ = world.getClanFullView(clanId);
+        assertEq(uint8(view_.westPlot.state), uint8(WheatPlotState.WinterLocked), "west plot should start locked");
+        assertEq(uint8(view_.eastPlot.state), uint8(WheatPlotState.WinterLocked), "east plot should start locked");
+        assertEq(view_.westPlot.remainingWheat, 0, "west locked plot should start empty");
+        assertEq(view_.eastPlot.remainingWheat, 0, "east locked plot should start empty");
+    }
+
+    function test_winterCropTransitionCapCoversCurrentClanCap() public {
+        for (uint256 i = 0; i < 12; i++) {
+            _mintClan();
+        }
+        assertGe(world.MAX_CROP_TRANSITION_PER_TICK(), 24, "transition cap must cover 12 clans x 2 plots");
+
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart);
+        assertEq(world.getWorldState().currentTick, winterStart, "full clan cap should not brick winter heartbeat");
+    }
+
+    function test_starvation_kill_deferred_to_next_tick() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 1);
+
+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
+        world.settleClan(clanId);
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.starvationStartsAtTick, winterStart, "starvation starts on first short-food tick");
+        assertEq(clan.livingClansmen, 4, "first starvation tick should not kill");
+
+        _advanceTick();
+        world.settleClan(clanId);
+
+        clan = world.getClan(clanId);
+        assertEq(clan.livingClansmen, 3, "first death lands on the next tick");
+    }
+
+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        uint256 goldBalance = 11e18;
+
+        _advanceToTick(winterStart + 5);
+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
+        harness.setClanIronAndGold(clanId, 5e18, goldBalance);
+
+        vm.recordLogs();
+        world.settleClan(clanId);
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
+        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
+        assertEq(clan.vaultWood, 0, "wood should burn on death");
+        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
+        assertEq(clan.vaultFish, 0, "fish should burn on death");
+        assertEq(clan.vaultIron, 0, "iron should burn on death");
+        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
+        _assertClanDiedLog(logs, clanId, winterStart + 4, "starvation");
+    }
+
+    function test_clanDeath_coldDamageMarksDeadAndBurnsVault() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        uint256 goldBalance = 13e18;
+        _advanceToTick(winterStart + 7);
+
+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
+        harness.setClanIronAndGold(clanId, 9e18, goldBalance);
+
+        vm.recordLogs();
+        world.settleClan(clanId);
+        Vm.Log[] memory logs = vm.getRecordedLogs();
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "cold should mark clan dead");
+        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
+        assertEq(clan.vaultWood, 0, "wood should burn on death");
+        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
+        assertEq(clan.vaultFish, 0, "fish should burn on death");
+        assertEq(clan.vaultIron, 0, "iron should burn on death");
+        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
+    }
+
+    function test_deadClanCannotSubmitClanOrders() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 5);
+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
+        world.settleClan(clanId);
+
+        vm.expectRevert("ClanWorld: clan dead");
+        _submitOrder(clanId, 1, ClanWorldConstants.REGION_FOREST, ActionType.Wait);
+    }
+
+    function test_clanDeath_doesNotAffectOtherClan() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanIdA = _mintClan();
+        uint32 clanIdB = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        Clan memory clanABefore = world.getClan(clanIdA);
+
+        _advanceToTick(winterStart + 5);
+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
+        world.settleClan(clanIdB);
+
+        Clan memory clanAAfter = world.getClan(clanIdA);
+        Clan memory clanB = world.getClan(clanIdB);
+        assertEq(uint8(clanB.clanState), uint8(ClanState.DEAD), "clan B should be dead");
+        assertEq(uint8(clanAAfter.clanState), uint8(ClanState.ACTIVE), "clan A should stay active");
+        assertEq(clanAAfter.livingClansmen, clanABefore.livingClansmen, "clan A living count should not change");
+        assertEq(clanAAfter.vaultWood, clanABefore.vaultWood, "clan A wood should not burn");
+        assertEq(clanAAfter.vaultWheat, clanABefore.vaultWheat, "clan A wheat should not burn");
+        assertEq(clanAAfter.vaultFish, clanABefore.vaultFish, "clan A fish should not burn");
+        assertEq(clanAAfter.goldBalance, clanABefore.goldBalance, "clan A gold should not change");
+    }
+
+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
+        _advanceToTick(winterEnd + 1);
+
+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
+
+        world.settleClan(clanId);
+
+        Clan memory clan = world.getClan(clanId);
+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
     }
 
     function test_season_transition() public {
@@ -1982,9 +2459,8 @@ contract ClanWorldTest is Test {
         assertEq(ws.currentSeasonNumber, 2, "season number should increment");
         assertEq(ws.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2);
-        // winter reset for new season
-        uint64 expectedWinterStart = ClanWorldConstants.SEASON_DURATION_TICKS
-            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
+        // winter is derived from the global recurring schedule
+        uint64 expectedWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS * 3;
         assertEq(ws.winterStartsAtTick, expectedWinterStart);
     }
 
diff --git a/packages/contracts/test/ClanWorldStub.t.sol b/packages/contracts/test/ClanWorldStub.t.sol
index 4625795..272a98c 100644
--- a/packages/contracts/test/ClanWorldStub.t.sol
+++ b/packages/contracts/test/ClanWorldStub.t.sol
@@ -42,11 +42,9 @@ contract ClanWorldStubTest is Test {
         assertEq(ws.seasonStartTick, 0);
         assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
         assertEq(ws.nextHeartbeatAtTick, ws.currentTick + 1, "next heartbeat opens currentTick + 1");
-        assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
-        );
-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
         assertFalse(ws.winterActive);
+        assertFalse(stub.isWinter());
     }
 }
diff --git a/packages/runner/src/runnerCastHeartbeat.ts b/packages/runner/src/runnerCastHeartbeat.ts
index 125904b..a5cf6e5 100644
--- a/packages/runner/src/runnerCastHeartbeat.ts
+++ b/packages/runner/src/runnerCastHeartbeat.ts
@@ -40,6 +40,8 @@ const HEARTBEAT_ABI = [
           { name: 'seasonStartTick', type: 'uint64' },
           { name: 'seasonEndTick', type: 'uint64' },
           { name: 'seasonFinalized', type: 'bool' },
+          { name: 'currentSeasonNumber', type: 'uint64' },
+          { name: 'nextHeartbeatAtTick', type: 'uint64' },
           { name: 'nextHeartbeatAtTs', type: 'uint64' },
           { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
           { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
diff --git a/packages/shared/src/adapters/IChainClient.ts b/packages/shared/src/adapters/IChainClient.ts
index bf97929..03e4c73 100644
--- a/packages/shared/src/adapters/IChainClient.ts
+++ b/packages/shared/src/adapters/IChainClient.ts
@@ -36,6 +36,8 @@ const CLAN_WORLD_ABI = [
           { name: 'seasonStartTick', type: 'uint64' },
           { name: 'seasonEndTick', type: 'uint64' },
           { name: 'seasonFinalized', type: 'bool' },
+          { name: 'currentSeasonNumber', type: 'uint64' },
+          { name: 'nextHeartbeatAtTick', type: 'uint64' },
           { name: 'winterActive', type: 'bool' },
           { name: 'winterStartsAtTick', type: 'uint64' },
           { name: 'winterEndsAtTick', type: 'uint64' },
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
Reviewing the integrated Phase 10 diff now with a focus on cross-cutting regressions at winter/elimination seams. I’ll parallelize code inspection and spin up subagents for independent passes (state machine, ABI/integration, and tests), then consolidate into the requested severity-ordered review format.
web search: 
web search: 
exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '320,560p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   320	
   321	        bytes32 tickSeed;
   322	        for (uint64 tick = fromTick; tick < toTick; tick++) {
   323	            tickSeed = _tickSeeds[tick];
   324	
   325	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   326	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   327	                cs.state = ClansmanState.ACTING;
   328	                cs.currentRegion = m.targetRegion;
   329	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   330	
   331	                if (m.action == ActionType.DefendBase) {
   332	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   333	                }
   334	            }
   335	
   336	            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
   337	
   338	            // Path 3: ACTING at/past settlesAtTick → resolve
   339	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
   340	                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   341	                if (m.active && getActionDuration(m.action) > 0) {
   342	                    _completeMission(cs, m);
   343	                }
   344	            }
   345	
   346	            // If mission completed during this tick, stop iterating
   347	            if (!m.active) break;
   348	        }
   349	    }
   350	
   351	    /// @dev Lazy settlement of a clan forward to currentTick.
   352	    ///      Mutates storage. Called before order submission and by public settleClan().
   353	    function _settleClan(uint32 clanId) internal {
   354	        Clan storage clan = _clans[clanId];
   355	        if (clan.clanId == 0) return;
   356	
   357	        uint64 curTick = _world.currentTick;
   358	        uint64 fromTick = clan.lastSettledTick;
   359	        if (fromTick >= curTick) return;
   360	
   361	        // Cap ticks settled per call to prevent block gas limit issues
   362	        uint64 maxSettleTicks = 200;
   363	        if (curTick > fromTick + maxSettleTicks) {
   364	            curTick = fromTick + maxSettleTicks;
   365	        }
   366	
   367	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   368	
   369	        // Settle tick by tick from fromTick to curTick - 1
   370	        // (curTick is still open; we settle through the last closed tick)
   371	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   372	            // 1. Apply upkeep for this tick
   373	            _applyUpkeep(clan, tick);
   374	
   375	            // 2. Wheat plot regrow check (lazy, per tick)
   376	            for (uint256 pi = 0; pi < 2; pi++) {
   377	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   378	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   379	                    plot.state = WheatPlotState.Harvestable;
   380	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   381	                    plot.regrowUntilTick = 0;
   382	                }
   383	            }
   384	
   385	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   386	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   387	                Clansman storage cs = _clansmen[clansmanIds[i]];
   388	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   389	            }
   390	        }
   391	
   392	        clan.lastSettledTick = curTick;
   393	        emit ClanSettled(clanId, curTick);
   394	    }
   395	
   396	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
   397	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   398	        if (clan.livingClansmen == 0) return;
   399	
   400	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   401	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   402	
   403	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   404	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   405	
   406	        if (hadEnoughWheat) {
   407	            clan.vaultWheat -= wheatNeeded;
   408	        } else {
   409	            clan.vaultWheat = 0;
   410	        }
   411	        if (hadEnoughFish) {
   412	            clan.vaultFish -= fishNeeded;
   413	        } else {
   414	            clan.vaultFish = 0;
   415	        }
   416	
   417	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   418	        if (starving && clan.starvationStartsAtTick == 0) {
   419	            clan.starvationStartsAtTick = tick;
   420	            emit ClanStarvationChanged(clan.clanId, true, tick);
   421	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   422	            clan.starvationStartsAtTick = 0;
   423	            emit ClanStarvationChanged(clan.clanId, false, tick);
   424	        }
   425	    }
   426	
   427	    /// @dev Check if a clan is currently starving (lazy read).
   428	    function _isStarving(Clan storage clan) internal view returns (bool) {
   429	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   430	    }
   431	
   432	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   433	    function _resolveAction(
   434	        Clan storage clan,
   435	        Clansman storage cs,
   436	        Mission storage m,
   437	        uint32 clanId,
   438	        uint64 tick,
   439	        bytes32 tickSeed
   440	    ) internal {
   441	        bool starving = _isStarving(clan);
   442	        ActionType action = m.action;
   443	
   444	        if (action == ActionType.ChopWood) {
   445	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   446	        } else if (action == ActionType.MineIron) {
   447	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   448	        } else if (action == ActionType.FishDocks) {
   449	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   450	        } else if (action == ActionType.FishDeepSea) {
   451	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   452	        } else if (action == ActionType.HarvestWheat) {
   453	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   454	        } else if (action == ActionType.DepositResources) {
   455	            _doDeposit(clan, cs, m, clanId, tick);
   456	        } else if (action == ActionType.Wait) {
   457	            // NOOP — worker stays ACTING (continuous), no transition needed
   458	            // Wait mission is effectively persistent until interrupted
   459	        } else if (action == ActionType.DefendBase) {
   460	            // Persistent mission. Registration happens atomically at order submission.
   461	        } else if (
   462	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   463	        ) {
   464	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   465	            _doBuilding(clan, cs, m, clanId, tick, action);
   466	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   467	            // Scheduled market actions: already enqueued at submitClanOrders time.
   468	            // Settlement resolves this action slot — just complete the mission.
   469	            // (Actual execution happened or will happen at heartbeat.)
   470	            _completeMission(cs, m);
   471	        }
   472	    }
   473	
   474	    // -------------------------------------------------------------------------
   475	    // Gathering helpers
   476	    // -------------------------------------------------------------------------
   477	
   478	    function _gatherWood(
   479	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   480	        Clansman storage cs,
   481	        Mission storage m,
   482	        uint32 clanId,
   483	        uint64 tick,
   484	        bool starving,
   485	        bytes32 tickSeed
   486	    ) internal {
   487	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
   488	        if (remaining == 0) {
   489	            _completeMission(cs, m);
   490	            return;
   491	        }
   492	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
   493	        // Crit roll: domain-separated RNG
   494	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   495	        uint256 critRoll = uint256(critRng) % 10000;
   496	        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
   497	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
   498	        }
   499	        if (starving) yield = yield / 2;
   500	        if (yield > remaining) yield = remaining;
   501	        cs.carryWood += yield;
   502	
   503	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   504	
   505	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
   506	            _completeMission(cs, m);
   507	        }
   508	        // else continuous — worker stays ACTING
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

exec
/bin/bash -lc 'git rev-parse --short HEAD && git show --name-only --pretty=format: 400463e' in /home/claude/code/clan-world
 succeeded in 0ms:
6cf6f39
docs/planning/clanworld_v4_spec.md
packages/contracts/abi/IClanWorld.json
packages/contracts/src/ClanWorld.sol
packages/contracts/src/IClanWorld.sol
packages/contracts/test/ClanWorld.t.sol
packages/runner/src/runnerCastHeartbeat.ts
packages/shared/src/adapters/IChainClient.ts

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '900,1160p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   900	        // Step 4: Resolve world events (season boundary, winter transitions).
   901	        _resolveWorldEvents(closedTick);
   902	
   903	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
   904	        uint64 newTick = closedTick + 1;
   905	        _world.currentTick = newTick;
   906	        _world.nextHeartbeatAtTick = newTick + 1;
   907	
   908	        emit TickAdvanced(closedTick, newTick, newSeed);
   909	    }
   910	
   911	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
   912	    ///      Called from heartbeat before market execution and tick increment.
   913	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   914	    function _settleCompletingMissions(uint64 tick) internal {
   915	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   916	            uint32 clanId = _allClanIds[i];
   917	            Clan storage clan = _clans[clanId];
   918	            if (clan.clanState == ClanState.DEAD) continue;
   919	
   920	            uint32[] storage csIds = _clanClansmanIds[clanId];
   921	            for (uint256 j = 0; j < csIds.length; j++) {
   922	                Clansman storage cs = _clansmen[csIds[j]];
   923	                if (cs.state == ClansmanState.DEAD) continue;
   924	
   925	                Mission storage m = _missions[cs.clansmanId];
   926	                if (!m.active) continue;
   927	                if (m.settlesAtTick != tick) continue; // not due this tick
   928	
   929	                // Settle this mission using the single-tick range [tick, tick+1).
   930	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   931	            }
   932	        }
   933	    }
   934	
   935	    /// @dev Resolve world events for the tick that was just closed.
   936	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
   937	    function _resolveWorldEvents(uint64 closedTick) internal {
   938	        uint64 newTick = closedTick + 1;
   939	
   940	        // --- season boundary ---
   941	        if (newTick >= _world.seasonEndTick) {
   942	            _world.currentSeasonNumber += 1;
   943	            _world.seasonStartTick = _world.seasonEndTick;
   944	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
   945	            // reset winter timers for new season
   946	            _world.winterActive = false;
   947	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   948	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   949	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   950	        }
   951	
   952	        // --- winter transitions (timer only; mechanics = Phase 10) ---
   953	        if (
   954	            !_world.winterActive && newTick >= _world.winterStartsAtTick
   955	                && _world.winterStartsAtTick < _world.seasonEndTick
   956	        ) {
   957	            _world.winterActive = true;
   958	            emit WinterStarted(newTick);
   959	        }
   960	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
   961	            _world.winterActive = false;
   962	            emit WinterEnded(newTick);
   963	            // schedule next winter cycle within this season
   964	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   965	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   966	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   967	            if (nextWinterStart < _world.seasonEndTick) {
   968	                _world.winterStartsAtTick = nextWinterStart;
   969	                _world.winterEndsAtTick = nextWinterEnd;
   970	            } else {
   971	                // no more winters this season; sentinel = seasonEndTick so guard never fires
   972	                _world.winterStartsAtTick = _world.seasonEndTick;
   973	                _world.winterEndsAtTick = _world.seasonEndTick;
   974	            }
   975	        }
   976	    }
   977	
   978	    /// @notice Public settlement trigger — lazily settle a clan.
   979	    function settleClan(uint32 clanId) external override nonReentrant {
   980	        _settleClan(clanId);
   981	    }
   982	
   983	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   984	    ///         Internally settles the entire clan (including upkeep) to guarantee
   985	    ///         correct ordering and prevent double-settlement. Callers may call this
   986	    ///         or settleClan interchangeably; both are safe and idempotent.
   987	    function settleClansman(uint32 csId) external override nonReentrant {
   988	        Clansman storage cs = _clansmen[csId];
   989	        if (cs.clansmanId == 0) return;
   990	        _settleClan(cs.clanId);
   991	    }
   992	
   993	    /// @notice Finalize season. Phase 1 stub.
   994	    function finalizeSeason() external override {
   995	        // TODO Phase 3
   996	    }
   997	
   998	    // =========================================================================
   999	    // CLAN LIFECYCLE
  1000	    // =========================================================================
  1001	
  1002	    /// @notice Mint a new clan and spawn its homebase.
  1003	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1004	        require(to != address(0), "ClanWorld: zero address");
  1005	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1006	        clanId = _nextClanId++;
  1007	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1008	
  1009	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1010	        uint8[6] memory spawnRegions = [
  1011	            ClanWorldConstants.REGION_FOREST,
  1012	            ClanWorldConstants.REGION_MOUNTAINS,
  1013	            ClanWorldConstants.REGION_WEST_FARMS,
  1014	            ClanWorldConstants.REGION_EAST_FARMS,
  1015	            ClanWorldConstants.REGION_WEST_DOCKS,
  1016	            ClanWorldConstants.REGION_EAST_DOCKS
  1017	        ];
  1018	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1019	
  1020	        // Create clan
  1021	        Clan storage clan = _clans[clanId];
  1022	        clan.clanId = clanId;
  1023	        clan.iftTokenId = iftTokenId;
  1024	        clan.owner = to;
  1025	        clan.clanState = ClanState.ACTIVE;
  1026	        clan.baseRegion = baseRegion;
  1027	        clan.baseLevel = 1;
  1028	        clan.wallLevel = 0;
  1029	        clan.monumentLevel = 0;
  1030	        clan.livingClansmen = 4;
  1031	        clan.lastSettledTick = _world.currentTick;
  1032	        clan.starvationStartsAtTick = 0;
  1033	        clan.coldDamage = 0;
  1034	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1035	        clan.blueprintBalance = 0;
  1036	        // Starting vault per v4 spec §12.4
  1037	        clan.vaultWood = 20e18;
  1038	        clan.vaultIron = 0;
  1039	        clan.vaultWheat = 20e18;
  1040	        clan.vaultFish = 2e18;
  1041	
  1042	        // Wheat plots
  1043	        _wheatPlots[clanId][0] = WheatPlot({
  1044	            state: WheatPlotState.Harvestable,
  1045	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1046	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1047	            regrowUntilTick: 0
  1048	        });
  1049	        _wheatPlots[clanId][1] = WheatPlot({
  1050	            state: WheatPlotState.Harvestable,
  1051	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1052	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1053	            regrowUntilTick: 0
  1054	        });
  1055	
  1056	        // Create 4 clansmen
  1057	        for (uint256 i = 0; i < 4; i++) {
  1058	            uint32 csId = _nextClansmanId++;
  1059	            Clansman storage cs = _clansmen[csId];
  1060	            cs.clansmanId = csId;
  1061	            cs.clanId = clanId;
  1062	            cs.state = ClansmanState.WAITING;
  1063	            cs.currentRegion = baseRegion;
  1064	            cs.cooldownEndsAtTs = 0;
  1065	            cs.lastMissionNonce = 0;
  1066	            cs.carryWood = 0;
  1067	            cs.carryIron = 0;
  1068	            cs.carryWheat = 0;
  1069	            cs.carryFish = 0;
  1070	            _clanClansmanIds[clanId].push(csId);
  1071	        }
  1072	
  1073	        _allClanIds.push(clanId);
  1074	
  1075	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1076	        return (clanId, iftTokenId);
  1077	    }
  1078	
  1079	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1080	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1081	        external
  1082	        override
  1083	        nonReentrant
  1084	        returns (OrderResult[] memory results)
  1085	    {
  1086	        Clan storage clan = _clans[clanId];
  1087	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1088	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1089	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1090	
  1091	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1092	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1093	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  1094	        {
  1095	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1096	            if (_world.currentTick > lastSettled + 200) {
  1097	                results = new OrderResult[](orders.length);
  1098	                for (uint256 i = 0; i < orders.length; i++) {
  1099	                    results[i] = OrderResult({
  1100	                        clansmanId: orders[i].clansmanId,
  1101	                        status: StatusCode.ERR_INVALID_ACTION,
  1102	                        cooldownEndsAtTs: 0,
  1103	                        missionNonce: 0
  1104	                    });
  1105	                }
  1106	                return results;
  1107	            }
  1108	        }
  1109	
  1110	        // Lazy settle before processing orders
  1111	        _settleClan(clanId);
  1112	
  1113	        results = new OrderResult[](orders.length);
  1114	
  1115	        for (uint256 i = 0; i < orders.length; i++) {
  1116	            results[i] = _processOrder(clanId, clan, orders[i]);
  1117	        }
  1118	
  1119	        return results;
  1120	    }
  1121	
  1122	    struct OrderCtx {
  1123	        uint8 gotoRegion;
  1124	        uint8 fromRegion;
  1125	        bool isNoop;
  1126	        uint8 travelTicks;
  1127	        uint64 arrivalTick;
  1128	        uint64 newNonce;
  1129	        uint32 targetClanId;
  1130	        bool wasActive;
  1131	        uint64 oldNonce;
  1132	    }
  1133	
  1134	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  1135	        internal
  1136	        returns (OrderResult memory result)
  1137	    {
  1138	        result.clansmanId = order.clansmanId;
  1139	
  1140	        // Validate clansman
  1141	        Clansman storage cs = _clansmen[order.clansmanId];
  1142	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  1143	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  1144	            return result;
  1145	        }
  1146	        if (cs.state == ClansmanState.DEAD) {
  1147	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  1148	            return result;
  1149	        }
  1150	
  1151	        if (order.action == ActionType.DefendBase) {
  1152	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  1153	            if (defendErr != StatusCode.OK) {
  1154	                result.status = defendErr;
  1155	                return result;
  1156	            }
  1157	
  1158	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1159	            Mission storage currentM = _missions[order.clansmanId];
  1160	            if (

codex
I confirmed the target SHA isn’t the current workspace HEAD, so I’m now reviewing the exact `400463e` snapshot directly with `git show` to avoid false positives from later edits. In parallel I’m launching subagents for contracts-state-machine and adapter/ABI drift checks.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
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
    41	import {RNG} from "./lib/RNG.sol";
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
    65	    mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
    66	
    67	    uint32 private _nextClanId;
    68	    uint32 private _nextClansmanId;
    69	    uint32[] private _allClanIds;
    70	
    71	    // per-clan clansman list: clanId => clansmanId[]
    72	    mapping(uint32 => uint32[]) private _clanClansmanIds;
    73	
    74	    // =========================================================================
    75	    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    76	    // =========================================================================
    77	
    78	    uint256 private constant WHEAT_HARVEST_RATE = 20e18;
    79	    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
    80	    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
    81	    /// @dev Caps winter crop boundary work; mintClan keeps the clan cap within this budget.
    82	    uint256 public constant MAX_CROP_TRANSITION_PER_TICK = 48;
    83	    uint256 private constant MAX_CLANS_FOR_CROP_TRANSITIONS = MAX_CROP_TRANSITION_PER_TICK / 2;
    84	
    85	    // =========================================================================
    86	    // CONSTRUCTOR
    87	    // =========================================================================
    88	
    89	    constructor() {
    90	        _world.currentTick = 0;
    91	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
    92	        _world.seasonStartTick = 0;
    93	        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
    94	        _world.currentSeasonNumber = 1;
    95	        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
    96	        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
    97	        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
    98	        _world.winterActive = false;
    99	        _treasury.treasuryOwner = msg.sender;
   100	        _nextClanId = 1;
   101	        _nextClansmanId = 1;
   102	    }
   103	
   104	    // =========================================================================
   105	    // TRAVEL DISTANCE MATRIX
   106	    // =========================================================================
   107	
   108	    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
   109	    // Adjacency:
   110	    //   Forest(1):       Mountains(2), WestFarms(4)
   111	    //   Mountains(2):    Forest(1), UnicornTown(3)
   112	    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
   113	    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
   114	    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
   115	    //   WestDocks(6):    WestFarms(4), DeepSea(8)
   116	    //   EastDocks(7):    EastFarms(5), DeepSea(8)
   117	    //   DeepSea(8):      WestDocks(6), EastDocks(7)
   118	    //
   119	    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
   120	    // Distance of 0 = same region.
   121	    //
   122	    // Full BFS-computed 8x8 matrix:
   123	    //   src\dst  1  2  3  4  5  6  7  8
   124	    //      1     0  1  2  1  3  2  4  3
   125	    //      2     1  0  1  2  2  3  3  4
   126	    //      3     2  1  0  1  1  2  2  3
   127	    //      4     1  2  1  0  2  1  3  2
   128	    //      5     3  2  1  2  0  3  1  2
   129	    //      6     2  3  2  1  3  0  2  1
   130	    //      7     4  3  2  3  1  2  0  1
   131	    //      8     3  4  3  2  2  1  1  0
   132	
   133	    function _distMatrix(uint8 src, uint8 dst) private pure returns (uint8) {
   134	        if (src == dst) return 0;
   135	        // Encode as (src-1)*8 + (dst-1), values from BFS
   136	        uint8[64] memory d = [
   137	            // src=1: to 1,2,3,4,5,6,7,8
   138	            0,
   139	            1,
   140	            2,
   141	            1,
   142	            3,
   143	            2,
   144	            4,
   145	            3,
   146	            // src=2: to 1,2,3,4,5,6,7,8
   147	            1,
   148	            0,
   149	            1,
   150	            2,
   151	            2,
   152	            3,
   153	            3,
   154	            4,
   155	            // src=3: to 1,2,3,4,5,6,7,8
   156	            2,
   157	            1,
   158	            0,
   159	            1,
   160	            1,
   161	            2,
   162	            2,
   163	            3,
   164	            // src=4: to 1,2,3,4,5,6,7,8
   165	            1,
   166	            2,
   167	            1,
   168	            0,
   169	            2,
   170	            1,
   171	            3,
   172	            2,
   173	            // src=5: to 1,2,3,4,5,6,7,8
   174	            3,
   175	            2,
   176	            1,
   177	            2,
   178	            0,
   179	            3,
   180	            1,
   181	            2,
   182	            // src=6: to 1,2,3,4,5,6,7,8
   183	            2,
   184	            3,
   185	            2,
   186	            1,
   187	            3,
   188	            0,
   189	            2,
   190	            1,
   191	            // src=7: to 1,2,3,4,5,6,7,8
   192	            4,
   193	            3,
   194	            2,
   195	            3,
   196	            1,
   197	            2,
   198	            0,
   199	            1,
   200	            // src=8: to 1,2,3,4,5,6,7,8
   201	            3,
   202	            4,
   203	            3,
   204	            2,
   205	            2,
   206	            1,
   207	            1,
   208	            0
   209	        ];
   210	        return d[uint8(src - 1) * 8 + uint8(dst - 1)];
   211	    }
   212	
   213	    // Build a canonical path from src to dst (BFS on the adjacency graph).
   214	    // Returns packed bytes8: region IDs in order, zero-padded.
   215	    function _buildPath(uint8 src, uint8 dst) private pure returns (bytes8) {
   216	        if (src == dst) {
   217	            return bytes8(uint64(src) << 56);
   218	        }
   219	        // Adjacency list (1-indexed, 0 = end sentinel)
   220	        // adj[r] = neighbors of region r (up to 3)

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1080,1360p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1080	
  1081	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
  1082	        uint64 newTick = closedTick + 1;
  1083	        _world.currentTick = newTick;
  1084	        _world.nextHeartbeatAtTick = newTick + 1;
  1085	
  1086	        emit TickAdvanced(closedTick, newTick, newSeed);
  1087	    }
  1088	
  1089	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
  1090	    ///      Called from heartbeat before market execution and tick increment.
  1091	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  1092	    function _settleCompletingMissions(uint64 tick) internal {
  1093	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1094	            uint32 clanId = _allClanIds[i];
  1095	            Clan storage clan = _clans[clanId];
  1096	            if (clan.clanState == ClanState.DEAD) continue;
  1097	
  1098	            uint32[] storage csIds = _clanClansmanIds[clanId];
  1099	            for (uint256 j = 0; j < csIds.length; j++) {
  1100	                Clansman storage cs = _clansmen[csIds[j]];
  1101	                if (cs.state == ClansmanState.DEAD) continue;
  1102	
  1103	                Mission storage m = _missions[cs.clansmanId];
  1104	                if (!m.active) continue;
  1105	                if (m.settlesAtTick != tick) continue; // not due this tick
  1106	
  1107	                // Settle this mission using the single-tick range [tick, tick+1).
  1108	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
  1109	            }
  1110	        }
  1111	    }
  1112	
  1113	    /// @dev Resolve world events for the tick that was just closed.
  1114	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
  1115	    function _resolveWorldEvents(uint64 closedTick) internal {
  1116	        uint64 newTick = closedTick + 1;
  1117	
  1118	        // --- season boundary ---
  1119	        if (newTick >= _world.seasonEndTick) {
  1120	            _world.currentSeasonNumber += 1;
  1121	            _world.seasonStartTick = _world.seasonEndTick;
  1122	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
  1123	        }
  1124	
  1125	        // --- winter transitions (timer only; mechanics = Phase 10) ---
  1126	        bool wasWinter = _isWinterActiveAt(closedTick);
  1127	        bool nowWinter = _isWinterActiveAt(newTick);
  1128	        if (!wasWinter && nowWinter) {
  1129	            _lockWheatPlotsForWinter();
  1130	            emit WinterStarted(_winterEventTick(newTick));
  1131	        }
  1132	        if (wasWinter && !nowWinter) {
  1133	            _restartWheatPlotsAfterWinter(newTick);
  1134	            emit WinterEnded(_winterEventTick(newTick));
  1135	        }
  1136	    }
  1137	
  1138	    function _lockWheatPlotsForWinter() internal {
  1139	        uint256 transitions;
  1140	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1141	            uint32 clanId = _allClanIds[i];
  1142	            for (uint256 pi = 0; pi < 2; pi++) {
  1143	                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
  1144	                WheatPlot storage plot = _wheatPlots[clanId][pi];
  1145	                plot.state = WheatPlotState.WinterLocked;
  1146	                plot.remainingWheat = 0;
  1147	                plot.regrowUntilTick = 0;
  1148	                transitions++;
  1149	            }
  1150	        }
  1151	    }
  1152	
  1153	    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
  1154	        uint256 transitions;
  1155	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1156	            uint32 clanId = _allClanIds[i];
  1157	            for (uint256 pi = 0; pi < 2; pi++) {
  1158	                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
  1159	                WheatPlot storage plot = _wheatPlots[clanId][pi];
  1160	                if (plot.state == WheatPlotState.WinterLocked) {
  1161	                    plot.state = WheatPlotState.Regrowing;
  1162	                    plot.remainingWheat = 0;
  1163	                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
  1164	                }
  1165	                transitions++;
  1166	            }
  1167	        }
  1168	    }
  1169	
  1170	    function _winterEventTick(uint64 tick) internal pure returns (uint64) {
  1171	        return tick;
  1172	    }
  1173	
  1174	    function _isWinterActiveAt(uint64 tick) internal pure returns (bool) {
  1175	        if (tick < ClanWorldConstants.WINTER_START_TICK) {
  1176	            return false;
  1177	        }
  1178	        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
  1179	        return elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
  1180	    }
  1181	
  1182	    function _winterWindowForTick(uint64 tick)
  1183	        internal
  1184	        pure
  1185	        returns (bool active, uint64 startsAtTick, uint64 endsAtTick)
  1186	    {
  1187	        if (tick < ClanWorldConstants.WINTER_START_TICK) {
  1188	            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
  1189	            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  1190	            return (false, startsAtTick, endsAtTick);
  1191	        }
  1192	
  1193	        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
  1194	        uint64 cycleIndex = elapsed / ClanWorldConstants.WINTER_PERIOD_TICKS;
  1195	        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
  1196	        active = elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
  1197	        startsAtTick = active ? cycleStart : cycleStart + ClanWorldConstants.WINTER_PERIOD_TICKS;
  1198	        endsAtTick = startsAtTick + ClanWorldConstants.WINTER_DURATION_TICKS;
  1199	    }
  1200	
  1201	    function _worldStateView() internal view returns (WorldState memory ws) {
  1202	        ws = _world;
  1203	        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
  1204	    }
  1205	
  1206	    /// @notice Public settlement trigger — lazily settle a clan.
  1207	    function settleClan(uint32 clanId) external override nonReentrant {
  1208	        _settleClan(clanId);
  1209	    }
  1210	
  1211	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
  1212	    ///         Internally settles the entire clan (including upkeep) to guarantee
  1213	    ///         correct ordering and prevent double-settlement. Callers may call this
  1214	    ///         or settleClan interchangeably; both are safe and idempotent.
  1215	    function settleClansman(uint32 csId) external override nonReentrant {
  1216	        Clansman storage cs = _clansmen[csId];
  1217	        if (cs.clansmanId == 0) return;
  1218	        _settleClan(cs.clanId);
  1219	    }
  1220	
  1221	    /// @notice Finalize season. Phase 1 stub.
  1222	    function finalizeSeason() external override {
  1223	        // TODO Phase 3
  1224	    }
  1225	
  1226	    // =========================================================================
  1227	    // CLAN LIFECYCLE
  1228	    // =========================================================================
  1229	
  1230	    /// @notice Mint a new clan and spawn its homebase.
  1231	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1232	        require(to != address(0), "ClanWorld: zero address");
  1233	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1234	        require(_allClanIds.length < MAX_CLANS_FOR_CROP_TRANSITIONS, "ClanWorld: clan cap");
  1235	        clanId = _nextClanId++;
  1236	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1237	
  1238	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1239	        uint8[6] memory spawnRegions = [
  1240	            ClanWorldConstants.REGION_FOREST,
  1241	            ClanWorldConstants.REGION_MOUNTAINS,
  1242	            ClanWorldConstants.REGION_WEST_FARMS,
  1243	            ClanWorldConstants.REGION_EAST_FARMS,
  1244	            ClanWorldConstants.REGION_WEST_DOCKS,
  1245	            ClanWorldConstants.REGION_EAST_DOCKS
  1246	        ];
  1247	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1248	
  1249	        // Create clan
  1250	        Clan storage clan = _clans[clanId];
  1251	        clan.clanId = clanId;
  1252	        clan.iftTokenId = iftTokenId;
  1253	        clan.owner = to;
  1254	        clan.clanState = ClanState.ACTIVE;
  1255	        clan.baseRegion = baseRegion;
  1256	        clan.baseLevel = 1;
  1257	        clan.wallLevel = 0;
  1258	        clan.monumentLevel = 0;
  1259	        clan.livingClansmen = 4;
  1260	        clan.lastSettledTick = _world.currentTick;
  1261	        clan.starvationStartsAtTick = 0;
  1262	        clan.coldDamage = 0;
  1263	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1264	        clan.blueprintBalance = 0;
  1265	        // Starting vault per v4 spec §12.4
  1266	        clan.vaultWood = 20e18;
  1267	        clan.vaultIron = 0;
  1268	        clan.vaultWheat = 20e18;
  1269	        clan.vaultFish = 2e18;
  1270	
  1271	        WheatPlotState startingPlotState =
  1272	            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
  1273	        uint256 startingWheat =
  1274	            startingPlotState == WheatPlotState.WinterLocked ? 0 : ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
  1275	
  1276	        // Wheat plots
  1277	        _wheatPlots[clanId][0] = WheatPlot({
  1278	            state: startingPlotState,
  1279	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1280	            remainingWheat: startingWheat,
  1281	            regrowUntilTick: 0
  1282	        });
  1283	        _wheatPlots[clanId][1] = WheatPlot({
  1284	            state: startingPlotState,
  1285	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1286	            remainingWheat: startingWheat,
  1287	            regrowUntilTick: 0
  1288	        });
  1289	
  1290	        // Create 4 clansmen
  1291	        for (uint256 i = 0; i < 4; i++) {
  1292	            uint32 csId = _nextClansmanId++;
  1293	            Clansman storage cs = _clansmen[csId];
  1294	            cs.clansmanId = csId;
  1295	            cs.clanId = clanId;
  1296	            cs.state = ClansmanState.WAITING;
  1297	            cs.currentRegion = baseRegion;
  1298	            cs.cooldownEndsAtTs = 0;
  1299	            cs.lastMissionNonce = 0;
  1300	            cs.carryWood = 0;
  1301	            cs.carryIron = 0;
  1302	            cs.carryWheat = 0;
  1303	            cs.carryFish = 0;
  1304	            _clanClansmanIds[clanId].push(csId);
  1305	        }
  1306	
  1307	        _allClanIds.push(clanId);
  1308	
  1309	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1310	        return (clanId, iftTokenId);
  1311	    }
  1312	
  1313	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1314	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1315	        external
  1316	        override
  1317	        nonReentrant
  1318	        returns (OrderResult[] memory results)
  1319	    {
  1320	        Clan storage clan = _clans[clanId];
  1321	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1322	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1323	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1324	
  1325	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1326	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1327	        {
  1328	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1329	            if (_world.currentTick > lastSettled + 200) {
  1330	                results = new OrderResult[](orders.length);
  1331	                for (uint256 i = 0; i < orders.length; i++) {
  1332	                    results[i] = OrderResult({
  1333	                        clansmanId: orders[i].clansmanId,
  1334	                        status: StatusCode.ERR_MUST_SETTLE_FIRST,
  1335	                        cooldownEndsAtTs: 0,
  1336	                        missionNonce: 0
  1337	                    });
  1338	                }
  1339	                return results;
  1340	            }
  1341	        }
  1342	
  1343	        // Lazy settle before processing orders
  1344	        _settleClan(clanId);
  1345	
  1346	        results = new OrderResult[](orders.length);
  1347	        if (clan.clanState == ClanState.DEAD) {
  1348	            for (uint256 i = 0; i < orders.length; i++) {
  1349	                results[i] = OrderResult({
  1350	                    clansmanId: orders[i].clansmanId,
  1351	                    status: StatusCode.ERR_CLAN_DEAD,
  1352	                    cooldownEndsAtTs: 0,
  1353	                    missionNonce: 0
  1354	                });
  1355	            }
  1356	            return results;
  1357	        }
  1358	
  1359	        for (uint256 i = 0; i < orders.length; i++) {
  1360	            results[i] = _processOrder(clanId, clan, orders[i]);

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '340,560p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   375	            if (clan.clanState == ClanState.DEAD) break;
   376	
   377	            // 2. Wheat plot regrow check (lazy, per tick)
   378	            for (uint256 pi = 0; pi < 2; pi++) {
   379	                WheatPlot storage plot = _wheatPlots[clanId][pi];
   380	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
   381	                    plot.state = WheatPlotState.Harvestable;
   382	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
   383	                    plot.regrowUntilTick = 0;
   384	                }
   385	            }
   386	
   387	            // 3. Advance each clansman (single-tick range: [tick, tick+1))
   388	            for (uint256 i = 0; i < clansmanIds.length; i++) {
   389	                Clansman storage cs = _clansmen[clansmanIds[i]];
   390	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   391	            }
   392	        }
   393	
   394	        if (curTick > fromTick && !_isWinterActiveAt(curTick) && _isWinterActiveAt(curTick - 1)) {
   395	            clan.coldDamage = 0;
   396	        }
   397	
   398	        clan.lastSettledTick = curTick;
   399	        emit ClanSettled(clanId, curTick);
   400	    }
   401	
   402	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
   403	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   404	        bool winter = _isWinterActiveAt(tick);
   405	        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
   406	            clan.coldDamage = 0;
   407	        }
   408	
   409	        if (clan.livingClansmen == 0) return;
   410	
   411	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   412	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   413	        if (winter) {
   414	            wheatNeeded = wheatNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
   415	            fishNeeded = fishNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
   416	        }
   417	
   418	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   419	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   420	
   421	        if (hadEnoughWheat) {
   422	            clan.vaultWheat -= wheatNeeded;
   423	        } else {
   424	            clan.vaultWheat = 0;
   425	        }
   426	        if (hadEnoughFish) {
   427	            clan.vaultFish -= fishNeeded;
   428	        } else {
   429	            clan.vaultFish = 0;
   430	        }
   431	
   432	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   433	        if (starving && clan.starvationStartsAtTick == 0) {
   434	            clan.starvationStartsAtTick = tick;
   435	            emit ClanStarvationChanged(clan.clanId, true, tick);
   436	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   437	            clan.starvationStartsAtTick = 0;
   438	            emit ClanStarvationChanged(clan.clanId, false, tick);
   439	        }
   440	
   441	        uint8 livingBeforeStarvation = clan.livingClansmen;
   442	        if (winter && starving) {
   443	            (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
   444	            uint64 effectiveStarvationStartsAtTick =
   445	                clan.starvationStartsAtTick > winterStartsAtTick ? clan.starvationStartsAtTick : winterStartsAtTick;
   446	            if (effectiveStarvationStartsAtTick < tick) {
   447	                _killNextClansmanFromStarvation(clan, tick);
   448	            }
   449	        }
   450	        if (clan.clanState == ClanState.DEAD) return;
   451	
   452	        if (winter) {
   453	            uint256 woodNeeded = ClanWorldConstants.WINTER_WOOD_BURN_PER_BASE + uint256(livingBeforeStarvation)
   454	                * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
   455	            if (clan.vaultWood >= woodNeeded) {
   456	                clan.vaultWood -= woodNeeded;
   457	            } else {
   458	                uint256 woodShort = woodNeeded - clan.vaultWood;
   459	                clan.vaultWood = 0;
   460	                uint16 oldColdDamage = clan.coldDamage;
   461	                if (clan.coldDamage < type(uint16).max) {
   462	                    clan.coldDamage += 1;
   463	                }
   464	                emit ClanColdShortage(clan.clanId, tick, woodShort);
   465	                _applyColdDamageConsequence(clan, tick, oldColdDamage);
   466	            }
   467	        }
   468	    }
   469	
   470	    function _applyColdDamageConsequence(Clan storage clan, uint64 tick, uint16 oldColdDamage) internal {
   471	        uint16 newColdDamage = clan.coldDamage;
   472	        if (newColdDamage == oldColdDamage) return;
   473	
   474	        if (clan.wallLevel > 0) {
   475	            if (
   476	                newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
   477	                    <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
   478	            ) return;
   479	
   480	            clan.wallLevel--;
   481	            emit WallDegradedByCold(clan.clanId, clan.wallLevel, tick);
   482	            return;
   483	        }
   484	
   485	        if (
   486	            newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
   487	                <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
   488	        ) return;
   489	
   490	        _killRandomClansmanFromCold(clan, tick, newColdDamage);
   491	    }
   492	
   493	    function _killRandomClansmanFromCold(Clan storage clan, uint64 tick, uint16 coldDamage) internal {
   494	        if (clan.livingClansmen == 0) return;
   495	
   496	        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
   497	        uint256 livingCount = 0;
   498	        for (uint256 i = 0; i < csIds.length; i++) {
   499	            if (_clansmen[csIds[i]].state != ClansmanState.DEAD) {
   500	                livingCount++;
   501	            }
   502	        }
   503	        if (livingCount == 0) return;
   504	
   505	        uint256 pick = RNG.rngBounded(
   506	            _world.currentTickSeed,
   507	            RNG.DOMAIN_COLD_DAMAGE,
   508	            uint256(keccak256(abi.encodePacked(clan.clanId, tick, coldDamage))),
   509	            livingCount
   510	        );
   511	
   512	        uint256 seen = 0;
   513	        for (uint256 i = 0; i < csIds.length; i++) {
   514	            Clansman storage cs = _clansmen[csIds[i]];
   515	            if (cs.state == ClansmanState.DEAD) continue;
   516	            if (seen != pick) {
   517	                seen++;
   518	                continue;
   519	            }
   520	
   521	            _markClansmanDeadFromCold(clan, cs, tick);
   522	            return;
   523	        }
   524	    }
   525	
   526	    function _killNextClansmanFromStarvation(Clan storage clan, uint64 tick) internal {
   527	        if (clan.livingClansmen == 0) return;
   528	
   529	        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
   530	        for (uint256 i = 0; i < csIds.length; i++) {
   531	            Clansman storage cs = _clansmen[csIds[i]];
   532	            if (cs.state == ClansmanState.DEAD) continue;
   533	
   534	            _markClansmanDead(clan, cs);
   535	            if (clan.livingClansmen == 0) {
   536	                _markClanDead(clan.clanId, "starvation", tick);
   537	            }
   538	            return;
   539	        }
   540	    }
   541	
   542	    function _markClansmanDeadFromCold(Clan storage clan, Clansman storage cs, uint64 tick) internal {
   543	        _markClansmanDead(clan, cs);
   544	
   545	        emit ClansmanColdDeath(clan.clanId, cs.clansmanId, tick);
   546	        if (clan.livingClansmen == 0) {
   547	            _markClanDead(clan.clanId, "cold", tick);
   548	        }
   549	    }
   550	
   551	    function _markClansmanDead(Clan storage clan, Clansman storage cs) internal {
   552	        if (cs.state == ClansmanState.DEAD) return;
   553	
   554	        cs.state = ClansmanState.DEAD;
   555	        if (clan.livingClansmen > 0) {
   556	            clan.livingClansmen--;
   557	        }
   558	
   559	        Mission storage m = _missions[cs.clansmanId];
   560	        if (m.active) {

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '560,900p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   560	        if (m.active) {
   561	            if (m.action == ActionType.DefendBase) {
   562	                _clearDefender(cs.clansmanId);
   563	            }
   564	            m.active = false;
   565	        }
   566	    }
   567	
   568	    function _markClanDead(uint32 clanId) internal {
   569	        _markClanDead(clanId, "unknown", _world.currentTick);
   570	    }
   571	
   572	    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {
   573	        Clan storage clan = _clans[clanId];
   574	        if (clan.clanId == 0 || clan.clanState == ClanState.DEAD) return;
   575	
   576	        clan.clanState = ClanState.DEAD;
   577	        clan.vaultWood = 0;
   578	        clan.vaultWheat = 0;
   579	        clan.vaultFish = 0;
   580	        clan.vaultIron = 0;
   581	        clan.starvationStartsAtTick = 0;
   582	        clan.livingClansmen = 0;
   583	
   584	        uint32[] storage csIds = _clanClansmanIds[clanId];
   585	        for (uint256 i = 0; i < csIds.length; i++) {
   586	            _clansmen[csIds[i]].state = ClansmanState.DEAD;
   587	            Mission storage m = _missions[csIds[i]];
   588	            if (m.active) {
   589	                if (m.action == ActionType.DefendBase) {
   590	                    _clearDefender(csIds[i]);
   591	                }
   592	                m.active = false;
   593	            }
   594	        }
   595	
   596	        emit ClanEliminated(clanId, tick);
   597	        emit ClanDied(clanId, tick, reason);
   598	    }
   599	
   600	    /// @dev Check if a clan is currently starving (lazy read).
   601	    function _isStarving(Clan storage clan) internal view returns (bool) {
   602	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   603	    }
   604	
   605	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   606	    function _resolveAction(
   607	        Clan storage clan,
   608	        Clansman storage cs,
   609	        Mission storage m,
   610	        uint32 clanId,
   611	        uint64 tick,
   612	        bytes32 tickSeed
   613	    ) internal {
   614	        bool starving = _isStarving(clan);
   615	        ActionType action = m.action;
   616	
   617	        if (action == ActionType.ChopWood) {
   618	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   619	        } else if (action == ActionType.MineIron) {
   620	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   621	        } else if (action == ActionType.FishDocks) {
   622	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   623	        } else if (action == ActionType.FishDeepSea) {
   624	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   625	        } else if (action == ActionType.HarvestWheat) {
   626	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   627	        } else if (action == ActionType.DepositResources) {
   628	            _doDeposit(clan, cs, m, clanId, tick);
   629	        } else if (action == ActionType.Wait) {
   630	            // NOOP — worker stays ACTING (continuous), no transition needed
   631	            // Wait mission is effectively persistent until interrupted
   632	        } else if (action == ActionType.DefendBase) {
   633	            // Persistent mission. Registration happens atomically at order submission.
   634	        } else if (
   635	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   636	        ) {
   637	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   638	            _doBuilding(clan, cs, m, clanId, tick, action);
   639	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   640	            // Scheduled market actions: already enqueued at submitClanOrders time.
   641	            // Settlement resolves this action slot — just complete the mission.
   642	            // (Actual execution happened or will happen at heartbeat.)
   643	            _completeMission(cs, m);
   644	        }
   645	    }
   646	
   647	    // -------------------------------------------------------------------------
   648	    // Gathering helpers
   649	    // -------------------------------------------------------------------------
   650	
   651	    function _gatherWood(
   652	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   653	        Clansman storage cs,
   654	        Mission storage m,
   655	        uint32 clanId,
   656	        uint64 tick,
   657	        bool starving,
   658	        bytes32 tickSeed
   659	    ) internal {
   660	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
   661	        if (remaining == 0) {
   662	            _completeMission(cs, m);
   663	            return;
   664	        }
   665	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
   666	        // Crit roll: domain-separated RNG
   667	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   668	        uint256 critRoll = uint256(critRng) % 10000;
   669	        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
   670	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
   671	        }
   672	        if (starving) yield = yield / 2;
   673	        if (yield > remaining) yield = remaining;
   674	        cs.carryWood += yield;
   675	
   676	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   677	
   678	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
   679	            _completeMission(cs, m);
   680	        }
   681	        // else continuous — worker stays ACTING
   682	    }
   683	
   684	    function _gatherIron(
   685	        Clan storage clan,
   686	        Clansman storage cs,
   687	        Mission storage m,
   688	        uint32 clanId,
   689	        uint64 tick,
   690	        bool starving,
   691	        bytes32 tickSeed
   692	    ) internal {
   693	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   694	        if (remaining == 0) {
   695	            _completeMission(cs, m);
   696	            return;
   697	        }
   698	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   699	        if (starving) yield = yield / 2;
   700	        if (yield > remaining) yield = remaining;
   701	        cs.carryIron += yield;
   702	
   703	        // Gold bonus roll — scoped to reduce stack depth
   704	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   705	
   706	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   707	
   708	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   709	            _completeMission(cs, m);
   710	        }
   711	    }
   712	
   713	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   714	        internal
   715	        returns (uint256 goldBonus)
   716	    {
   717	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   718	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   719	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   720	            clan.goldBalance += goldBonus;
   721	        }
   722	    }
   723	
   724	    function _gatherFishDocks(
   725	        Clan storage,
   726	        Clansman storage cs,
   727	        Mission storage m,
   728	        uint32 clanId,
   729	        uint64 tick,
   730	        bool starving,
   731	        bytes32 tickSeed
   732	    ) internal {
   733	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   734	        if (remaining == 0) {
   735	            _completeMission(cs, m);
   736	            return;
   737	        }
   738	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   739	        uint256 fishRoll = uint256(fishRng) % 10000;
   740	        uint256 yield = 0;
   741	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   742	            yield = 1e18;
   743	        }
   744	        if (starving) yield = yield / 2;
   745	        if (yield > remaining) yield = remaining;
   746	        if (yield > 0) {
   747	            cs.carryFish += yield;
   748	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   749	        }
   750	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   751	            _completeMission(cs, m);
   752	        }
   753	    }
   754	
   755	    function _gatherFishDeepSea(
   756	        Clan storage,
   757	        Clansman storage cs,
   758	        Mission storage m,
   759	        uint32 clanId,
   760	        uint64 tick,
   761	        bool starving,
   762	        bytes32 tickSeed
   763	    ) internal {
   764	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   765	        if (remaining == 0) {
   766	            _completeMission(cs, m);
   767	            return;
   768	        }
   769	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   770	        uint256 fishRoll = uint256(fishRng) % 10000;
   771	        uint256 yield = 0;
   772	        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
   773	            yield = 1e18;
   774	        }
   775	        if (starving) yield = yield / 2;
   776	        if (yield > remaining) yield = remaining;
   777	        if (yield > 0) {
   778	            cs.carryFish += yield;
   779	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
   780	        }
   781	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   782	            _completeMission(cs, m);
   783	        }
   784	    }
   785	
   786	    function _gatherWheat(
   787	        Clan storage,
   788	        /* clan — unused but kept positional for callsite parity */
   789	        Clansman storage cs,
   790	        Mission storage m,
   791	        uint32 clanId,
   792	        uint64 tick,
   793	        bool starving
   794	    ) internal {
   795	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
   796	        if (remaining == 0) {
   797	            _completeMission(cs, m);
   798	            return;
   799	        }
   800	        // Determine plot index from region
   801	        uint8 region = m.targetRegion;
   802	        uint256 plotIdx;
   803	        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
   804	            plotIdx = 0;
   805	        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
   806	            plotIdx = 1;
   807	        } else {
   808	            // Wrong region — complete (no harvest)
   809	            _completeMission(cs, m);
   810	            return;
   811	        }
   812	
   813	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
   814	        if (plot.state == WheatPlotState.WinterLocked) {
   815	            // Winter-locked plots cannot be harvested; queued missions end with no yield.
   816	            _completeMission(cs, m);
   817	            return;
   818	        }
   819	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
   820	            // Plot not ready — worker waits
   821	            _completeMission(cs, m);
   822	            return;
   823	        }
   824	
   825	        uint256 yield = WHEAT_HARVEST_RATE;
   826	        if (starving) yield = yield / 2;
   827	        if (yield > remaining) yield = remaining;
   828	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
   829	
   830	        cs.carryWheat += yield;
   831	        plot.remainingWheat -= yield;
   832	
   833	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
   834	
   835	        if (plot.remainingWheat == 0) {
   836	            plot.state = WheatPlotState.Regrowing;
   837	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
   838	        }
   839	
   840	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
   841	            _completeMission(cs, m);
   842	        }
   843	        // else continuous
   844	    }
   845	
   846	    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
   847	        internal
   848	    {
   849	        // Must be at homebase region
   850	        if (cs.currentRegion != clan.baseRegion) {
   851	            _completeMission(cs, m);
   852	            return;
   853	        }
   854	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
   855	        if (!hasAnything) {
   856	            _completeMission(cs, m);
   857	            return;
   858	        }
   859	
   860	        uint256 w = cs.carryWood;
   861	        uint256 ir = cs.carryIron;
   862	        uint256 wh = cs.carryWheat;
   863	        uint256 fi = cs.carryFish;
   864	
   865	        clan.vaultWood += w;
   866	        clan.vaultIron += ir;
   867	        clan.vaultWheat += wh;
   868	        clan.vaultFish += fi;
   869	
   870	        cs.carryWood = 0;
   871	        cs.carryIron = 0;
   872	        cs.carryWheat = 0;
   873	        cs.carryFish = 0;
   874	
   875	        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
   876	        _completeMission(cs, m);
   877	    }
   878	
   879	    function _doBuilding(
   880	        Clan storage clan,
   881	        Clansman storage cs,
   882	        Mission storage m,
   883	        uint32 clanId,
   884	        uint64 tick,
   885	        ActionType action
   886	    ) internal {
   887	        // Must be at homebase
   888	        if (cs.currentRegion != clan.baseRegion) {
   889	            _completeMission(cs, m);
   890	            return;
   891	        }
   892	
   893	        bool success = false;
   894	        if (action == ActionType.BuildWall) {
   895	            success = _tryBuildWall(clan, clanId, tick);
   896	        } else if (action == ActionType.UpgradeBase) {
   897	            success = _tryUpgradeBase(clan, clanId, tick);
   898	        } else if (action == ActionType.UpgradeMonument) {
   899	            success = _tryUpgradeMonument(clan, clanId, tick);
   900	        }

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1360,1760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1360	            results[i] = _processOrder(clanId, clan, orders[i]);
  1361	        }
  1362	
  1363	        return results;
  1364	    }
  1365	
  1366	    struct OrderCtx {
  1367	        uint8 gotoRegion;
  1368	        uint8 fromRegion;
  1369	        bool isNoop;
  1370	        uint8 travelTicks;
  1371	        uint64 arrivalTick;
  1372	        uint64 newNonce;
  1373	        uint32 targetClanId;
  1374	        bool wasActive;
  1375	        uint64 oldNonce;
  1376	    }
  1377	
  1378	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  1379	        internal
  1380	        returns (OrderResult memory result)
  1381	    {
  1382	        result.clansmanId = order.clansmanId;
  1383	
  1384	        // Validate clansman
  1385	        Clansman storage cs = _clansmen[order.clansmanId];
  1386	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  1387	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  1388	            return result;
  1389	        }
  1390	        if (cs.state == ClansmanState.DEAD) {
  1391	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  1392	            return result;
  1393	        }
  1394	
  1395	        if (order.action == ActionType.DefendBase) {
  1396	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  1397	            if (defendErr != StatusCode.OK) {
  1398	                result.status = defendErr;
  1399	                return result;
  1400	            }
  1401	
  1402	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1403	            Mission storage currentM = _missions[order.clansmanId];
  1404	            if (
  1405	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  1406	                    && currentM.targetClanId == defendTargetClanId
  1407	            ) {
  1408	                result.status = StatusCode.OK;
  1409	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1410	                result.missionNonce = currentM.nonce;
  1411	                return result;
  1412	            }
  1413	        }
  1414	
  1415	        // Cooldown check
  1416	        if (block.timestamp < cs.cooldownEndsAtTs) {
  1417	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
  1418	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1419	            result.missionNonce = cs.lastMissionNonce;
  1420	            return result;
  1421	        }
  1422	
  1423	        OrderCtx memory ctx;
  1424	        ctx.fromRegion = cs.currentRegion;
  1425	        ctx.gotoRegion = order.gotoRegion;
  1426	        ctx.targetClanId =
  1427	            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
  1428	
  1429	        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
  1430	        ctx.isNoop = order.action != ActionType.DefendBase
  1431	            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
  1432	        if (ctx.isNoop) {
  1433	            ctx.gotoRegion = ctx.fromRegion;
  1434	        }
  1435	
  1436	        // Validate target region (1-8 or 0=noop)
  1437	        if (ctx.gotoRegion > 8) {
  1438	            result.status = StatusCode.ERR_INVALID_REGION;
  1439	            return result;
  1440	        }
  1441	
  1442	        // Validate action
  1443	        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
  1444	        if (actionErr != StatusCode.OK) {
  1445	            result.status = actionErr;
  1446	            return result;
  1447	        }
  1448	
  1449	        // Capture existing mission state
  1450	        Mission storage existingM = _missions[order.clansmanId];
  1451	        ctx.wasActive = existingM.active;
  1452	        ctx.oldNonce = existingM.nonce;
  1453	
  1454	        // Compute travel from the clansman's current region to the order target.
  1455	        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
  1456	        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
  1457	
  1458	        // New nonce
  1459	        ctx.newNonce = cs.lastMissionNonce + 1;
  1460	        cs.lastMissionNonce = ctx.newNonce;
  1461	
  1462	        // Install mission via helper to keep stack shallow
  1463	        _installMission(existingM, order, cs, ctx);
  1464	
  1465	        // Update clansman state
  1466	        if (ctx.travelTicks > 0) {
  1467	            cs.state = ClansmanState.TRAVELING;
  1468	        } else {
  1469	            // NOOP / same-region: no traveling state per v4.3 §A
  1470	            cs.state = ClansmanState.ACTING;
  1471	            cs.currentRegion = ctx.gotoRegion;
  1472	        }
  1473	
  1474	        // Start cooldown
  1475	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1476	
  1477	        _clearDefender(cs.clansmanId);
  1478	
  1479	        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
  1480	        // executeAtTick = arrivalTick (not arrivalTick+1).
  1481	        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
  1482	            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
  1483	        }
  1484	
  1485	        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
  1486	            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
  1487	        }
  1488	
  1489	        if (ctx.wasActive) {
  1490	            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
  1491	        }
  1492	
  1493	        emit MissionAssigned(
  1494	            clanId,
  1495	            order.clansmanId,
  1496	            ctx.newNonce,
  1497	            order.action,
  1498	            ctx.fromRegion,
  1499	            ctx.gotoRegion,
  1500	            _world.currentTick,
  1501	            ctx.arrivalTick
  1502	        );
  1503	
  1504	        result.status = StatusCode.OK;
  1505	        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1506	        result.missionNonce = ctx.newNonce;
  1507	        return result;
  1508	    }
  1509	
  1510	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  1511	        internal
  1512	    {
  1513	        m.active = true;
  1514	        m.nonce = ctx.newNonce;
  1515	        m.clansmanId = cs.clansmanId;
  1516	        m.submittedAtTick = _world.currentTick;
  1517	        m.executesAtTick = ctx.arrivalTick;
  1518	        m.settlesAtTick = order.action == ActionType.DefendBase
  1519	            ? type(uint64).max
  1520	            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
  1521	        m.startRegion = ctx.fromRegion;
  1522	        m.targetRegion = ctx.gotoRegion;
  1523	        m.action = order.action;
  1524	        m.startTick = _world.currentTick;
  1525	        m.arrivalTick = ctx.arrivalTick;
  1526	        m.actionStartTick = ctx.arrivalTick;
  1527	        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
  1528	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
  1529	            ? MarketExecutionMode.Scheduled
  1530	            : MarketExecutionMode.None;
  1531	        m.targetClanId = ctx.targetClanId;
  1532	        m.marketToken = order.marketToken;
  1533	        m.marketAmount = order.marketAmount;
  1534	        m.maxGoldIn = order.maxGoldIn;
  1535	    }
  1536	
  1537	    function _enqueueScheduledMarketAction(
  1538	        uint32 clanId,
  1539	        ClanOrder calldata order,
  1540	        uint32 clansmanId,
  1541	        uint64 executeAtTick,
  1542	        uint64 missionNonce
  1543	    ) internal {
  1544	        ScheduledMarketAction memory sma = ScheduledMarketAction({
  1545	            executeAtTick: executeAtTick,
  1546	            commitSequence: _world.nextCommitSequence++,
  1547	            missionNonce: missionNonce,
  1548	            clanId: clanId,
  1549	            clansmanId: clansmanId,
  1550	            action: order.action,
  1551	            marketToken: order.marketToken,
  1552	            marketAmount: order.marketAmount,
  1553	            maxGoldIn: order.maxGoldIn
  1554	        });
  1555	        _scheduledMarketActions[executeAtTick].push(sma);
  1556	        emit ScheduledMarketActionCommitted(
  1557	            executeAtTick,
  1558	            sma.commitSequence,
  1559	            clanId,
  1560	            clansmanId,
  1561	            order.action,
  1562	            order.marketToken,
  1563	            order.marketAmount,
  1564	            order.maxGoldIn
  1565	        );
  1566	    }
  1567	
  1568	    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
  1569	        if (_clansmanDefendingRegion[clansmanId] == region) return;
  1570	        _clearDefender(clansmanId);
  1571	
  1572	        if (_defenderCountByRegionClan[region][clanId] == 0) {
  1573	            _defendingClansByRegion[region].push(clanId);
  1574	        }
  1575	        _defenderCountByRegionClan[region][clanId]++;
  1576	        _clansmanDefendingRegion[clansmanId] = region;
  1577	    }
  1578	
  1579	    function _clearDefender(uint32 clansmanId) internal {
  1580	        uint8 region = _clansmanDefendingRegion[clansmanId];
  1581	        if (region == 0) return;
  1582	
  1583	        uint32 clanId = _clansmen[clansmanId].clanId;
  1584	        uint256 count = _defenderCountByRegionClan[region][clanId];
  1585	        if (count > 1) {
  1586	            _defenderCountByRegionClan[region][clanId] = count - 1;
  1587	        } else {
  1588	            delete _defenderCountByRegionClan[region][clanId];
  1589	            uint32[] storage clans = _defendingClansByRegion[region];
  1590	            for (uint256 i = 0; i < clans.length; i++) {
  1591	                if (clans[i] == clanId) {
  1592	                    clans[i] = clans[clans.length - 1];
  1593	                    clans.pop();
  1594	                    break;
  1595	                }
  1596	            }
  1597	        }
  1598	
  1599	        delete _clansmanDefendingRegion[clansmanId];
  1600	    }
  1601	
  1602	    // =========================================================================
  1603	    // MARKET EXECUTION (Phase 2)
  1604	    // =========================================================================
  1605	
  1606	    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
  1607	    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
  1608	    function _executeScheduledMarketActions(uint64 tick) internal {
  1609	        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
  1610	        uint256 len = actions.length;
  1611	        if (len == 0) return;
  1612	
  1613	        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;
  1614	
  1615	        for (uint256 i = 0; i < processCount; i++) {
  1616	            ScheduledMarketAction storage sma = actions[i];
  1617	
  1618	            // Validate clansman still belongs to the clan
  1619	            Clansman storage cs = _clansmen[sma.clansmanId];
  1620	            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
  1621	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
  1622	                continue;
  1623	            }
  1624	
  1625	            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
  1626	            // Note: _completeMission sets m.active=false during settlement (by design), so we
  1627	            // cannot use m.active as a validity signal here — check action type and nonce.
  1628	            Mission storage m = _missions[sma.clansmanId];
  1629	            if (m.action != sma.action || m.nonce != sma.missionNonce) {
  1630	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1631	                continue;
  1632	            }
  1633	
  1634	            if (sma.action == ActionType.MarketSell) {
  1635	                try this._executeMarketSellExternal(
  1636	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
  1637	                ) {
  1638	                // success
  1639	                }
  1640	                catch {
  1641	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1642	                }
  1643	            } else if (sma.action == ActionType.MarketBuy) {
  1644	                try this._executeMarketBuyExternal(
  1645	                    tick,
  1646	                    sma.clanId,
  1647	                    sma.clansmanId,
  1648	                    sma.marketToken,
  1649	                    sma.marketAmount,
  1650	                    sma.maxGoldIn,
  1651	                    sma.commitSequence
  1652	                ) {
  1653	                // success
  1654	                }
  1655	                catch {
  1656	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1657	                }
  1658	            }
  1659	        }
  1660	
  1661	        if (len > processCount) {
  1662	            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
  1663	            for (uint256 i = processCount; i < len; i++) {
  1664	                nextActions.push(actions[i]);
  1665	            }
  1666	            // Invariant: each tick queue executes in global commitSequence order, including
  1667	            // older overflow actions merged into a tick that already has native actions.
  1668	            _sortScheduledMarketActionsByCommitSequence(nextActions);
  1669	        }
  1670	
  1671	        delete _scheduledMarketActions[tick];
  1672	    }
  1673	
  1674	    function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
  1675	        for (uint256 i = 1; i < actions.length; i++) {
  1676	            ScheduledMarketAction memory key = actions[i];
  1677	            uint256 j = i;
  1678	            while (j > 0 && actions[j - 1].commitSequence > key.commitSequence) {
  1679	                actions[j] = actions[j - 1];
  1680	                j--;
  1681	            }
  1682	            actions[j] = key;
  1683	        }
  1684	    }
  1685	
  1686	    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
  1687	    function _executeMarketSellExternal(
  1688	        uint64 closedTick,
  1689	        uint32 clanId,
  1690	        uint32 clansmanId,
  1691	        address token,
  1692	        uint256 amount,
  1693	        uint64 commitSequence
  1694	    ) external {
  1695	        require(msg.sender == address(this), "ClanWorld: internal only");
  1696	        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
  1697	    }
  1698	
  1699	    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
  1700	    function _executeMarketBuyExternal(
  1701	        uint64 closedTick,
  1702	        uint32 clanId,
  1703	        uint32 clansmanId,
  1704	        address token,
  1705	        uint256 amountOut,
  1706	        uint256 maxGoldIn,
  1707	        uint64 commitSequence
  1708	    ) external {
  1709	        require(msg.sender == address(this), "ClanWorld: internal only");
  1710	        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
  1711	    }
  1712	
  1713	    /// @dev Map a resource token address to its pool address.
  1714	    function _poolFor(address token) internal view returns (address pool) {
  1715	        if (token == _treasury.woodToken) return _treasury.woodGoldPool;
  1716	        if (token == _treasury.ironToken) return _treasury.ironGoldPool;
  1717	        if (token == _treasury.wheatToken) return _treasury.wheatGoldPool;
  1718	        if (token == _treasury.fishToken) return _treasury.fishGoldPool;
  1719	        return address(0);
  1720	    }
  1721	
  1722	    /// @dev Add an amount of a resource token to the clan vault.
  1723	    function _addToVault(Clan storage clan, address token, uint256 amount) internal {
  1724	        if (token == _treasury.woodToken) {
  1725	            clan.vaultWood += amount;
  1726	            return;
  1727	        }
  1728	        if (token == _treasury.ironToken) {
  1729	            clan.vaultIron += amount;
  1730	            return;
  1731	        }
  1732	        if (token == _treasury.wheatToken) {
  1733	            clan.vaultWheat += amount;
  1734	            return;
  1735	        }
  1736	        if (token == _treasury.fishToken) {
  1737	            clan.vaultFish += amount;
  1738	            return;
  1739	        }
  1740	    }
  1741	
  1742	    /// @dev Deduct an amount of a resource token from the clan vault. Returns false if insufficient.
  1743	    function _deductFromVault(Clan storage clan, address token, uint256 amount) internal returns (bool) {
  1744	        if (token == _treasury.woodToken) {
  1745	            if (clan.vaultWood < amount) return false;
  1746	            clan.vaultWood -= amount;
  1747	            return true;
  1748	        }
  1749	        if (token == _treasury.ironToken) {
  1750	            if (clan.vaultIron < amount) return false;
  1751	            clan.vaultIron -= amount;
  1752	            return true;
  1753	        }
  1754	        if (token == _treasury.wheatToken) {
  1755	            if (clan.vaultWheat < amount) return false;
  1756	            clan.vaultWheat -= amount;
  1757	            return true;
  1758	        }
  1759	        if (token == _treasury.fishToken) {
  1760	            if (clan.vaultFish < amount) return false;

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1760,2160p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1760	            if (clan.vaultFish < amount) return false;
  1761	            clan.vaultFish -= amount;
  1762	            return true;
  1763	        }
  1764	        return false;
  1765	    }
  1766	
  1767	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
  1768	    function _executeMarketSell(
  1769	        uint64 closedTick,
  1770	        uint32 clanId,
  1771	        uint32 clansmanId,
  1772	        address token,
  1773	        uint256 amount,
  1774	        uint64 commitSequence
  1775	    ) internal {
  1776	        if (!_treasury.poolsSeeded) {
  1777	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1778	            return;
  1779	        }
  1780	        address poolAddr = _poolFor(token);
  1781	        if (poolAddr == address(0)) {
  1782	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1783	            return;
  1784	        }
  1785	
  1786	        Clan storage clan = _clans[clanId];
  1787	        if (!_deductFromVault(clan, token, amount)) {
  1788	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
  1789	            return;
  1790	        }
  1791	
  1792	        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
  1793	        clan.goldBalance += goldOut;
  1794	
  1795	        emit ScheduledMarketActionExecuted(
  1796	            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
  1797	        );
  1798	    }
  1799	
  1800	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
  1801	    function _executeMarketBuy(
  1802	        uint64 closedTick,
  1803	        uint32 clanId,
  1804	        uint32 clansmanId,
  1805	        address token,
  1806	        uint256 amountOut,
  1807	        uint256 maxGoldIn,
  1808	        uint64 commitSequence
  1809	    ) internal {
  1810	        if (!_treasury.poolsSeeded) {
  1811	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1812	            return;
  1813	        }
  1814	        address poolAddr = _poolFor(token);
  1815	        if (poolAddr == address(0)) {
  1816	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1817	            return;
  1818	        }
  1819	
  1820	        // Quote gold cost without updating reserves
  1821	        uint256 goldIn = StubPool(poolAddr).quoteBuy(amountOut);
  1822	
  1823	        Clan storage clan = _clans[clanId];
  1824	
  1825	        if (goldIn > maxGoldIn) {
  1826	            emit MarketActionFailed(
  1827	                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
  1828	            );
  1829	            return;
  1830	        }
  1831	        if (clan.goldBalance < goldIn) {
  1832	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
  1833	            return;
  1834	        }
  1835	
  1836	        // Execute — use return value to guard against any future pool divergence
  1837	        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
  1838	        clan.goldBalance -= actualGoldIn;
  1839	        _addToVault(clan, token, amountOut);
  1840	
  1841	        emit ScheduledMarketActionExecuted(
  1842	            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
  1843	        );
  1844	    }
  1845	
  1846	    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
  1847	        internal
  1848	        view
  1849	        returns (StatusCode)
  1850	    {
  1851	        ActionType action = order.action;
  1852	
  1853	        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
  1854	
  1855	        // DepositResources: must go to homebase
  1856	        if (action == ActionType.DepositResources) {
  1857	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  1858	        }
  1859	
  1860	        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
  1861	        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
  1862	        {
  1863	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  1864	        }
  1865	
  1866	        // ChopWood: must go to Forest
  1867	        if (action == ActionType.ChopWood) {
  1868	            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
  1869	        }
  1870	        // MineIron: must go to Mountains
  1871	        if (action == ActionType.MineIron) {
  1872	            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
  1873	        }
  1874	        // FishDocks: must go to WestDocks or EastDocks
  1875	        if (action == ActionType.FishDocks) {
  1876	            if (
  1877	                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
  1878	            ) {
  1879	                return StatusCode.ERR_INVALID_REGION;
  1880	            }
  1881	        }
  1882	        // FishDeepSea: must go to DeepSea
  1883	        if (action == ActionType.FishDeepSea) {
  1884	            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
  1885	        }
  1886	        // HarvestWheat: must go to WestFarms or EastFarms
  1887	        if (action == ActionType.HarvestWheat) {
  1888	            if (
  1889	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  1890	            ) {
  1891	                return StatusCode.ERR_INVALID_REGION;
  1892	            }
  1893	            if (_isWinterActiveAt(_world.currentTick)) {
  1894	                return StatusCode.ERR_WINTER_LOCKED;
  1895	            }
  1896	        }
  1897	
  1898	        if (action == ActionType.DefendBase) {
  1899	            return _validateDefendBaseOrder(clan, order, gotoRegion);
  1900	        }
  1901	
  1902	        // MarketBuy/MarketSell: must target Unicorn Town
  1903	        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  1904	            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1905	            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
  1906	                return StatusCode.ERR_INVALID_REGION;
  1907	            }
  1908	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
  1909	            // Validate token is a supported resource token (not gold itself)
  1910	            address tok = order.marketToken;
  1911	            if (tok == address(0) || tok == _treasury.goldToken) {
  1912	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1913	            }
  1914	            if (
  1915	                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
  1916	                    && tok != _treasury.fishToken
  1917	            ) {
  1918	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1919	            }
  1920	            // Market orders are always enqueued for the arrivalTick FIFO queue.
  1921	            // _resolveAction records mission completion but does not execute any swap.
  1922	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
  1923	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
  1924	            }
  1925	        }
  1926	
  1927	        cs; // suppress unused warning
  1928	        return StatusCode.OK;
  1929	    }
  1930	
  1931	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  1932	        internal
  1933	        view
  1934	        returns (StatusCode)
  1935	    {
  1936	        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1937	        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
  1938	        return StatusCode.OK;
  1939	    }
  1940	
  1941	    // =========================================================================
  1942	    // TREASURY / POOL SEEDING
  1943	    // =========================================================================
  1944	
  1945	    /// @notice One-time treasury initialization: register token and pool addresses.
  1946	    ///         Must be called before seedPools. Callable only once.
  1947	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  1948	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  1949	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1950	
  1951	        _treasury.woodToken = tokens[0];
  1952	        _treasury.ironToken = tokens[1];
  1953	        _treasury.wheatToken = tokens[2];
  1954	        _treasury.fishToken = tokens[3];
  1955	        _treasury.goldToken = tokens[4];
  1956	        _treasury.blueprintToken = tokens[5];
  1957	
  1958	        _treasury.woodGoldPool = pools[0];
  1959	        _treasury.wheatGoldPool = pools[1];
  1960	        _treasury.fishGoldPool = pools[2];
  1961	        _treasury.ironGoldPool = pools[3];
  1962	    }
  1963	
  1964	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  1965	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  1966	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1967	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  1968	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  1969	
  1970	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  1971	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  1972	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  1973	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  1974	
  1975	        _treasury.poolsSeeded = true;
  1976	
  1977	        emit PoolsSeeded(
  1978	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  1979	        );
  1980	    }
  1981	
  1982	    // =========================================================================
  1983	    // OTC TRANSFERS
  1984	    // =========================================================================
  1985	
  1986	    function transferGold(uint32, uint32, uint256) external pure override {
  1987	        revert("OTC transfers not implemented");
  1988	    }
  1989	
  1990	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  1991	        revert("OTC transfers not implemented");
  1992	    }
  1993	
  1994	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  1995	        revert("OTC transfers not implemented");
  1996	    }
  1997	
  1998	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  1999	        external
  2000	        pure
  2001	        override
  2002	    {
  2003	        revert("OTC transfers not implemented");
  2004	    }
  2005	
  2006	    // =========================================================================
  2007	    // RAW READ GETTERS
  2008	    // =========================================================================
  2009	
  2010	    function getWorldState() external view override returns (WorldState memory) {
  2011	        return _worldStateView();
  2012	    }
  2013	
  2014	    function getTreasuryState() external view override returns (TreasuryState memory) {
  2015	        return _treasury;
  2016	    }
  2017	
  2018	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  2019	        return _clans[clanId];
  2020	    }
  2021	
  2022	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  2023	        return _clansmen[clansmanId];
  2024	    }
  2025	
  2026	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  2027	        return _missions[clansmanId];
  2028	    }
  2029	
  2030	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  2031	        external
  2032	        view
  2033	        override
  2034	        returns (uint64 submitted, uint64 executes, uint64 settles)
  2035	    {
  2036	        Mission memory m = _missions[clansmanId];
  2037	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  2038	            return (0, 0, 0);
  2039	        }
  2040	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  2041	    }
  2042	
  2043	    function isWinter() external view override returns (bool) {
  2044	        return _isWinterActiveAt(_world.currentTick);
  2045	    }
  2046	
  2047	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  2048	        if (
  2049	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  2050	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  2051	        ) {
  2052	            return 4;
  2053	        }
  2054	
  2055	        if (
  2056	            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
  2057	                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
  2058	                || action == ActionType.MarketSell
  2059	        ) {
  2060	            return 1;
  2061	        }
  2062	
  2063	        return 0;
  2064	    }
  2065	
  2066	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
  2067	        return uint64(_travelTicks(fromRegion, toRegion));
  2068	    }
  2069	
  2070	    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
  2071	        return BanditTroop({
  2072	            banditId: 0,
  2073	            state: BanditState.NONE,
  2074	            currentRegion: 0,
  2075	            attackAttemptsMade: 0,
  2076	            stateEnteredTick: 0,
  2077	            nextActionTick: 0,
  2078	            tier: 0,
  2079	            attackPower: 0,
  2080	            carryWood: 0,
  2081	            carryIron: 0,
  2082	            carryWheat: 0,
  2083	            carryFish: 0
  2084	        });
  2085	    }
  2086	
  2087	    function getWheatPlots(uint32 clanId)
  2088	        external
  2089	        view
  2090	        override
  2091	        returns (WheatPlot memory west, WheatPlot memory east)
  2092	    {
  2093	        west = _wheatPlots[clanId][0];
  2094	        east = _wheatPlots[clanId][1];
  2095	    }
  2096	
  2097	    function getScheduledMarketActionsForTick(uint64 tick)
  2098	        external
  2099	        view
  2100	        override
  2101	        returns (ScheduledMarketAction[] memory)
  2102	    {
  2103	        return _scheduledMarketActions[tick];
  2104	    }
  2105	
  2106	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  2107	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  2108	        uint256 count = 0;
  2109	
  2110	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2111	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2112	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2113	                Mission storage mission = _missions[clanClansmen[j]];
  2114	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2115	                    count++;
  2116	                }
  2117	            }
  2118	        }
  2119	
  2120	        clansmanIds = new uint32[](count);
  2121	        uint256 out = 0;
  2122	        for (uint256 i = 0; i < defendingClans.length; i++) {
  2123	            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
  2124	            for (uint256 j = 0; j < clanClansmen.length; j++) {
  2125	                uint32 clansmanId = clanClansmen[j];
  2126	                Mission storage mission = _missions[clansmanId];
  2127	                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
  2128	                    clansmanIds[out++] = clansmanId;
  2129	                }
  2130	            }
  2131	        }
  2132	    }
  2133	
  2134	    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
  2135	        return _defendingClansByRegion[region];
  2136	    }
  2137	
  2138	    // =========================================================================
  2139	    // DERIVED READ GETTERS (read-only, no storage mutation)
  2140	    // =========================================================================
  2141	
  2142	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
  2143	    ///      Carry amounts lag until next settleClan().
  2144	    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
  2145	        Clan memory clan = _clans[clanId];
  2146	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  2147	        uint256 lootVal = _lootValueRaw(clan);
  2148	        return
  2149	            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
  2150	    }
  2151	
  2152	    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
  2153	        Clansman memory cs = _clansmen[clansmanId];
  2154	        Mission memory m = _missions[clansmanId];
  2155	        uint8 effectiveRegion = cs.currentRegion;
  2156	        if (cs.state == ClansmanState.TRAVELING && m.active) {
  2157	            // Simplified: if past arrivalTick, they're at target; else at start
  2158	            if (_world.currentTick >= m.arrivalTick) {
  2159	                effectiveRegion = m.targetRegion;
  2160	            } else {

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2160,2360p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2160	            } else {
  2161	                effectiveRegion = m.startRegion;
  2162	            }
  2163	        }
  2164	        return DerivedClansmanState({
  2165	            clansman: cs, activeMission: m, effectiveRegion: effectiveRegion, derivedAtTick: _world.currentTick
  2166	        });
  2167	    }
  2168	
  2169	    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
  2170	        return 0; // Phase 3
  2171	    }
  2172	
  2173	    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
  2174	        external
  2175	        pure
  2176	        override
  2177	        returns (uint8 travelTicks, bytes8 path)
  2178	    {
  2179	        if (srcRegion > 8 || dstRegion > 8) {
  2180	            return (0, bytes8(0));
  2181	        }
  2182	        if (srcRegion == dstRegion || srcRegion == 0 || dstRegion == 0) {
  2183	            travelTicks = 0;
  2184	            path = bytes8(uint64(srcRegion) << 56);
  2185	            return (travelTicks, path);
  2186	        }
  2187	        travelTicks = _travelTicks(srcRegion, dstRegion);
  2188	        path = _buildPath(srcRegion, dstRegion);
  2189	    }
  2190	
  2191	    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256) {
  2192	        return _lootValueRaw(_clans[clanId]);
  2193	    }
  2194	
  2195	    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
  2196	        // Phase 1: return raw value (no simulation)
  2197	        return _lootValueRaw(_clans[clanId]);
  2198	    }
  2199	
  2200	    /// @dev Compute loot value per v4 spec §6.9: wood=1, wheat=1, fish=2, iron=4 points.
  2201	    function _lootValueRaw(Clan memory clan) internal pure returns (uint256) {
  2202	        return clan.vaultWood + clan.vaultWheat + clan.vaultFish * 2 + clan.vaultIron * 4;
  2203	    }
  2204	
  2205	    // =========================================================================
  2206	    // UI INDEXER AGGREGATOR GETTERS
  2207	    // =========================================================================
  2208	
  2209	    /// @dev Leaderboard loot values reflect vault contents only (last-settled state).
  2210	    ///      Carry amounts not included. Full view-only settlement deferred.
  2211	    ///      View function — no gas cost for off-chain indexer/UI reads.
  2212	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  2213	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
  2214	        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
  2215	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2216	            uint32 cid = _allClanIds[i];
  2217	            Clan storage clan = _clans[cid];
  2218	            lb[i] = LeaderboardEntry({
  2219	                clanId: cid,
  2220	                owner: clan.owner,
  2221	                monumentLevel: clan.monumentLevel,
  2222	                baseLevel: clan.baseLevel,
  2223	                wallLevel: clan.wallLevel,
  2224	                livingClansmen: clan.livingClansmen,
  2225	                state: clan.clanState,
  2226	                lootValue: _lootValueRaw(clan)
  2227	            });
  2228	        }
  2229	
  2230	        WorldState memory ws = _worldStateView();
  2231	        return WorldSnapshot({
  2232	            currentTick: ws.currentTick,
  2233	            seasonStartTick: ws.seasonStartTick,
  2234	            seasonEndTick: ws.seasonEndTick,
  2235	            seasonFinalized: ws.seasonFinalized,
  2236	            currentSeasonNumber: ws.currentSeasonNumber,
  2237	            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
  2238	            winterActive: ws.winterActive,
  2239	            winterStartsAtTick: ws.winterStartsAtTick,
  2240	            winterEndsAtTick: ws.winterEndsAtTick,
  2241	            activeBanditId: ws.activeBanditId,
  2242	            currentTickSeed: ws.currentTickSeed,
  2243	            leaderboard: lb
  2244	        });
  2245	    }
  2246	
  2247	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
  2248	    ///      current; carry amounts and wheat progress lag until next settleClan() call.
  2249	    ///      Full view-only settlement simulation is deferred (tracked issue).
  2250	    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
  2251	        Clan storage clan = _clans[clanId];
  2252	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
  2253	        uint256 lootVal = _lootValueRaw(clan);
  2254	
  2255	        DerivedClanState memory derivedClan =
  2256	            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
  2257	
  2258	        uint32[] storage csIds = _clanClansmanIds[clanId];
  2259	        ClansmanFullView[] memory clansmen = new ClansmanFullView[](csIds.length);
  2260	        for (uint256 i = 0; i < csIds.length; i++) {
  2261	            uint32 csId = csIds[i];
  2262	            Clansman memory cs = _clansmen[csId];
  2263	            Mission memory m = _missions[csId];
  2264	            uint8 effRegion = cs.currentRegion;
  2265	            if (cs.state == ClansmanState.TRAVELING && m.active) {
  2266	                effRegion = _world.currentTick >= m.arrivalTick ? m.targetRegion : m.startRegion;
  2267	            }
  2268	            DerivedClansmanState memory dcs = DerivedClansmanState({
  2269	                clansman: cs, activeMission: m, effectiveRegion: effRegion, derivedAtTick: _world.currentTick
  2270	            });
  2271	            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
  2272	        }
  2273	
  2274	        // Find if any of this clan's clansmen is defending a home region.
  2275	        uint32 thisClanDefendingBaseId = 0;
  2276	        for (uint256 i = 0; i < csIds.length; i++) {
  2277	            uint8 region = _clansmanDefendingRegion[csIds[i]];
  2278	            if (region != 0) {
  2279	                thisClanDefendingBaseId = region;
  2280	                break;
  2281	            }
  2282	        }
  2283	
  2284	        return ClanFullView({
  2285	            clan: derivedClan,
  2286	            clansmen: clansmen,
  2287	            westPlot: _wheatPlots[clanId][0],
  2288	            eastPlot: _wheatPlots[clanId][1],
  2289	            incomingDefenderIds: _defendingClansByRegion[clan.baseRegion],
  2290	            thisClanDefendingBaseId: thisClanDefendingBaseId
  2291	        });
  2292	    }
  2293	
  2294	    function getMarketState() external view override returns (MarketState memory) {
  2295	        return MarketState({
  2296	            wood: _poolReserves(_treasury.woodToken, _treasury.woodGoldPool),
  2297	            wheat: _poolReserves(_treasury.wheatToken, _treasury.wheatGoldPool),
  2298	            fish: _poolReserves(_treasury.fishToken, _treasury.fishGoldPool),
  2299	            iron: _poolReserves(_treasury.ironToken, _treasury.ironGoldPool),
  2300	            currentTick: _world.currentTick,
  2301	            currentTickQueue: _scheduledMarketActions[_world.currentTick],
  2302	            nextTickQueue: _scheduledMarketActions[_world.currentTick + 1]
  2303	        });
  2304	    }
  2305	
  2306	    function _poolReserves(address resourceToken, address poolAddr) internal view returns (PoolReserves memory pr) {
  2307	        pr.resourceToken = resourceToken;
  2308	        if (poolAddr == address(0) || resourceToken == address(0)) {
  2309	            return pr;
  2310	        }
  2311	        (uint256 rA, uint256 rB) = StubPool(poolAddr).getReserves();
  2312	        pr.resourceReserve = rA;
  2313	        pr.goldReserve = rB;
  2314	        pr.spotPriceGoldPerResource = rA > 0 ? (rB * 1e18) / rA : 0;
  2315	    }
  2316	
  2317	    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
  2318	        return ActiveBanditView({
  2319	            exists: false,
  2320	            banditId: 0,
  2321	            state: BanditState.NONE,
  2322	            currentRegion: 0,
  2323	            attackAttemptsMade: 0,
  2324	            maxAttemptsRemaining: 0,
  2325	            stateEnteredTick: 0,
  2326	            nextActionTick: 0,
  2327	            tier: 0,
  2328	            attackPower: 0,
  2329	            carryWood: 0,
  2330	            carryIron: 0,
  2331	            carryWheat: 0,
  2332	            carryFish: 0,
  2333	            projectedTargetClanId: 0,
  2334	            projectedTargetLootValue: 0
  2335	        });
  2336	    }
  2337	
  2338	    /// @dev View function — no gas cost for off-chain indexer/UI reads.
  2339	    ///      Iterates all clans. Canonical game cap: ≤12 clans, ≤4 clansmen each = ≤48 iterations.
  2340	    function getRegionPopulation(uint8 region) external view override returns (RegionOccupant[] memory) {
  2341	        // Count matching occupants first
  2342	        uint256 count = 0;
  2343	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2344	            uint32 cid = _allClanIds[i];
  2345	            uint32[] storage csIds = _clanClansmanIds[cid];
  2346	            for (uint256 j = 0; j < csIds.length; j++) {
  2347	                Clansman storage cs = _clansmen[csIds[j]];
  2348	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
  2349	                    count++;
  2350	                }
  2351	            }
  2352	        }
  2353	
  2354	        RegionOccupant[] memory occupants = new RegionOccupant[](count);
  2355	        uint256 idx = 0;
  2356	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  2357	            uint32 cid = _allClanIds[i];
  2358	            uint32[] storage csIds = _clanClansmanIds[cid];
  2359	            for (uint256 j = 0; j < csIds.length; j++) {
  2360	                uint32 csId = csIds[j];

codex
I’ve now pulled the exact contract snapshot from `400463e`; next I’m validating enum/layout compatibility and event semantics against interface and TS consumers to catch integration-only breaks.
exec
/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
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
    29	    // First winter opens at tick 110; ticks [100,110) remain pre-winter runway.
    30	    uint64 internal constant WINTER_START_TICK = 110;
    31	    uint64 internal constant WINTER_DURATION_TICKS = 10;
    32	    uint64 internal constant WINTER_PERIOD_TICKS = 110;
    33	    uint64 internal constant SEASON_DURATION_TICKS = 360;
    34	
    35	    // Bandit cadence
    36	    uint64 internal constant BANDIT_COOLDOWN_TICKS = 10;
    37	    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    38	    uint64 internal constant BANDIT_REST_TICKS = 2;
    39	    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;
    40	
    41	    // Clansman cadence
    42	    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
    43	
    44	    // Carry caps (per clansman)
    45	    uint256 internal constant WOOD_CAP = 15e18;
    46	    uint256 internal constant IRON_CAP = 5e18;
    47	    uint256 internal constant WHEAT_CAP = 40e18;
    48	    uint256 internal constant FISH_CAP = 8e18;
    49	
    50	    // Gathering yields
    51	    uint256 internal constant WOOD_BASE_YIELD = 2e18;
    52	    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
    53	    uint16 internal constant WOOD_CRIT_BPS = 2000; // 20%
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
    65	    uint256 internal constant WINTER_WOOD_BURN_PER_CLANSMAN = 5e17; // 0.5
    66	    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    67	    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
    68	    uint16 internal constant COLD_DAMAGE_PER_WALL_DEGRADATION = 2;
    69	    uint16 internal constant COLD_DAMAGE_PER_CLANSMAN_DEATH = 2;
    70	
    71	    // Wheat plots
    72	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    73	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
    74	
    75	    // Bandit combat
    76	    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    77	    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%
    78	
    79	    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    80	    uint8 internal constant REGION_NOOP = 0;
    81	    uint8 internal constant REGION_FOREST = 1;
    82	    uint8 internal constant REGION_MOUNTAINS = 2;
    83	    uint8 internal constant REGION_UNICORN_TOWN = 3;
    84	    uint8 internal constant REGION_WEST_FARMS = 4;
    85	    uint8 internal constant REGION_EAST_FARMS = 5;
    86	    uint8 internal constant REGION_WEST_DOCKS = 6;
    87	    uint8 internal constant REGION_EAST_DOCKS = 7;
    88	    uint8 internal constant REGION_DEEP_SEA = 8;
    89	
    90	    // Sentinels
    91	    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    92	    uint32 internal constant BANDIT_ID_NULL = 0;
    93	}
    94	
    95	// =============================================================================
    96	// ENUMS
    97	// =============================================================================
    98	
    99	enum ClanState {
   100	    ACTIVE,
   101	    DEAD
   102	}
   103	
   104	enum ClansmanState {
   105	    WAITING,
   106	    TRAVELING,
   107	    ACTING,
   108	    DEAD
   109	}
   110	
   111	enum BanditState {
   112	    NONE,
   113	    CAMPING,
   114	    RESTING,
   115	    ATTACKING,
   116	    DEFEATED,
   117	    ESCAPED
   118	}
   119	
   120	enum WheatPlotState {
   121	    Harvestable,
   122	    Regrowing,
   123	    WinterLocked
   124	}
   125	
   126	enum ResourceType {
   127	    Wood,
   128	    Iron,
   129	    Wheat,
   130	    Fish
   131	}
   132	
   133	enum ActionType {
   134	    None,
   135	    ChopWood,
   136	    MineIron,
   137	    FishDocks,
   138	    FishDeepSea,
   139	    HarvestWheat,
   140	    DepositResources,
   141	    BuildWall,
   142	    UpgradeBase,
   143	    UpgradeMonument,
   144	    DefendBase,
   145	    MarketBuy,
   146	    MarketSell,
   147	    Wait
   148	}
   149	
   150	enum MarketExecutionMode {
   151	    None,
   152	    Immediate,
   153	    Scheduled
   154	}
   155	
   156	enum StatusCode {
   157	    OK,
   158	    ERR_CLAN_DEAD,
   159	    ERR_CLAN_NOT_OWNED,
   160	    ERR_CLANSMAN_DEAD,
   161	    ERR_INVALID_CLANSMAN,
   162	    ERR_INVALID_REGION,
   163	    ERR_INVALID_ACTION,
   164	    ERR_INVALID_TARGET,
   165	    ERR_COOLDOWN_ACTIVE,
   166	    ERR_NOT_WAITING,
   167	    ERR_NOT_IN_UNICORN_TOWN,
   168	    ERR_NOT_AT_HOMEBASE,
   169	    ERR_NOT_AT_TARGET_BASE,
   170	    ERR_NOT_DEFENDABLE,
   171	    ERR_MISSING_RESOURCES,
   172	    ERR_EMPTY_CARGO,
   173	    ERR_PLOT_NOT_READY,
   174	    ERR_PLOT_EMPTY,
   175	    ERR_MARKET_ZERO_AMOUNT,
   176	    ERR_MARKET_UNSUPPORTED_TOKEN,
   177	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   178	    ERR_MARKET_BUY_OVER_CAPACITY,
   179	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   180	    ERR_WORLD_TICK_MISMATCH,
   181	    ERR_NO_ACTIVE_BANDIT,
   182	    ERR_SEASON_ENDED,
   183	    ERR_NOT_ENOUGH_GOLD,
   184	    ERR_CARRY_FULL,
   185	    ERR_WINTER_LOCKED,
   186	    ERR_MUST_SETTLE_FIRST
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
/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '260,560p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   260	    WheatPlotState state;
   261	    uint8 region; // West Farms or East Farms
   262	    uint256 remainingWheat;
   263	    uint64 regrowUntilTick;
   264	}
   265	
   266	struct Clansman {
   267	    uint32 clansmanId;
   268	    uint32 clanId;
   269	    ClansmanState state;
   270	    uint8 currentRegion;
   271	
   272	    uint64 cooldownEndsAtTs;
   273	    uint64 lastMissionNonce;
   274	
   275	    uint256 carryWood;
   276	    uint256 carryIron;
   277	    uint256 carryWheat;
   278	    uint256 carryFish;
   279	}
   280	
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
   341	    uint16 defensePoints;
   342	}
   343	
   344	struct PackedRoute {
   345	    uint8 travelTicks;
   346	    bytes8 path; // ordered region ids, e.g. [6,4,3,2,0,0,0,0]
   347	}
   348	
   349	// =============================================================================
   350	// DERIVED VIEW STRUCTS (read-only, settled forward to current tick)
   351	// =============================================================================
   352	
   353	struct DerivedClanState {
   354	    Clan clan; // settled to current tick
   355	    bool isStarving;
   356	    uint256 lootValue; // current weighted loot value
   357	    uint64 derivedAtTick;
   358	}
   359	
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
   387	}
   388	
   389	struct PoolSeedConfig {
   390	    uint256 woodSeed;
   391	    uint256 wheatSeed;
   392	    uint256 fishSeed;
   393	    uint256 ironSeed;
   394	    uint256 goldSeedForWood;
   395	    uint256 goldSeedForWheat;
   396	    uint256 goldSeedForFish;
   397	    uint256 goldSeedForIron;
   398	}
   399	
   400	// =============================================================================
   401	// UI INDEXER AGGREGATOR STRUCTS (v4.4 additions)
   402	// =============================================================================
   403	
   404	struct LeaderboardEntry {
   405	    uint32 clanId;
   406	    address owner;
   407	    uint8 monumentLevel;
   408	    uint8 baseLevel;
   409	    uint8 wallLevel;
   410	    uint8 livingClansmen;
   411	    ClanState state;
   412	    uint256 lootValue; // settled
   413	}
   414	
   415	struct WorldSnapshot {
   416	    uint64 currentTick;
   417	    uint64 seasonStartTick;
   418	    uint64 seasonEndTick;
   419	    bool seasonFinalized;
   420	    uint64 currentSeasonNumber;
   421	    uint64 nextHeartbeatAtTick;
   422	    bool winterActive;
   423	    uint64 winterStartsAtTick;
   424	    uint64 winterEndsAtTick;
   425	    uint32 activeBanditId;
   426	    bytes32 currentTickSeed;
   427	
   428	    LeaderboardEntry[] leaderboard;
   429	}
   430	
   431	struct ClansmanFullView {
   432	    DerivedClansmanState clansman;
   433	    Mission activeMission;
   434	}
   435	
   436	struct ClanFullView {
   437	    DerivedClanState clan;
   438	    ClansmanFullView[] clansmen;
   439	    WheatPlot westPlot;
   440	    WheatPlot eastPlot;
   441	    uint32[] incomingDefenderIds; // legacy UI field; clanIds defending this clan's home region
   442	    uint32 thisClanDefendingBaseId; // defended home region, or 0 if none
   443	}
   444	
   445	struct PoolReserves {
   446	    address resourceToken;
   447	    uint256 resourceReserve;
   448	    uint256 goldReserve;
   449	    uint256 spotPriceGoldPerResource; // = goldReserve * 1e18 / resourceReserve, or 0
   450	}
   451	
   452	struct MarketState {
   453	    PoolReserves wood;
   454	    PoolReserves wheat;
   455	    PoolReserves fish;
   456	    PoolReserves iron;
   457	
   458	    uint64 currentTick;
   459	    ScheduledMarketAction[] currentTickQueue;
   460	    ScheduledMarketAction[] nextTickQueue;
   461	}
   462	
   463	struct ActiveBanditView {
   464	    bool exists;
   465	    uint32 banditId;
   466	    BanditState state;
   467	    uint8 currentRegion;
   468	    uint8 attackAttemptsMade;
   469	    uint8 maxAttemptsRemaining;
   470	    uint64 stateEnteredTick;
   471	    uint64 nextActionTick;
   472	    uint8 tier;
   473	    uint16 attackPower;
   474	
   475	    uint256 carryWood;
   476	    uint256 carryIron;
   477	    uint256 carryWheat;
   478	    uint256 carryFish;
   479	
   480	    uint32 projectedTargetClanId; // 0 if no eligible target in current region
   481	    uint256 projectedTargetLootValue;
   482	}
   483	
   484	struct RegionOccupant {
   485	    uint32 clansmanId;
   486	    uint32 clanId;
   487	    ClansmanState state;
   488	    ActionType currentAction;
   489	    uint64 missionNonce;
   490	}
   491	
   492	// =============================================================================
   493	// EVENTS
   494	// =============================================================================
   495	
   496	interface IClanWorldEvents {
   497	    // ----- world clock -----
   498	    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
   499	    event WinterStarted(uint64 indexed tick);
   500	    event WinterEnded(uint64 indexed tick);
   501	    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
   502	
   503	    // ----- clan lifecycle -----
   504	    event ClanSpawned(
   505	        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
   506	    );
   507	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   508	    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
   509	    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
   510	    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
   511	    event ClanColdShortage(uint32 indexed clanId, uint64 tick, uint256 woodShort);
   512	    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint64 tick);
   513	    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint64 tick);
   514	
   515	    // ----- missions -----
   516	    event MissionAssigned(
   517	        uint32 indexed clanId,
   518	        uint32 indexed clansmanId,
   519	        uint64 missionNonce,
   520	        ActionType action,
   521	        uint8 startRegion,
   522	        uint8 targetRegion,
   523	        uint64 startTick,
   524	        uint64 arrivalTick
   525	    );
   526	    event MissionInterrupted(
   527	        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
   528	    );
   529	    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   530	    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   531	
   532	    // ----- gathering / vault movement -----
   533	    event ResourcesGathered(
   534	        uint32 indexed clanId,
   535	        uint32 indexed clansmanId,
   536	        ActionType action,
   537	        uint256 woodGained,
   538	        uint256 ironGained,
   539	        uint256 wheatGained,
   540	        uint256 fishGained,
   541	        uint256 goldBonus,
   542	        uint64 atTick
   543	    );
   544	    event ResourcesDeposited(
   545	        uint32 indexed clanId,
   546	        uint32 indexed clansmanId,
   547	        uint256 wood,
   548	        uint256 iron,
   549	        uint256 wheat,
   550	        uint256 fish,
   551	        uint64 atTick
   552	    );
   553	
   554	    // ----- building -----
   555	    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   556	    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   557	    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   558	
   559	    // ----- market -----
   560	    event ImmediateMarketActionExecuted(

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '560,860p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   560	    event ImmediateMarketActionExecuted(
   561	        uint32 indexed clanId,
   562	        uint32 indexed clansmanId,
   563	        address tokenIn,
   564	        address tokenOut,
   565	        uint256 amountIn,
   566	        uint256 amountOut,
   567	        uint64 atTick
   568	    );
   569	    event ScheduledMarketActionExecuted(
   570	        uint64 indexed executeAtTick,
   571	        uint64 indexed commitSequence,
   572	        uint32 indexed clanId,
   573	        uint32 clansmanId,
   574	        address tokenIn,
   575	        address tokenOut,
   576	        uint256 amountIn,
   577	        uint256 amountOut
   578	    );
   579	    event ScheduledMarketActionCommitted(
   580	        uint64 indexed executeAtTick,
   581	        uint64 indexed commitSequence,
   582	        uint32 indexed clanId,
   583	        uint32 clansmanId,
   584	        ActionType action,
   585	        address marketToken,
   586	        uint256 marketAmount,
   587	        uint256 maxGoldIn
   588	    );
   589	    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
   590	
   591	    // ----- bandits -----
   592	    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
   593	    event BanditStateChanged(
   594	        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
   595	    );
   596	    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
   597	    event BanditAttackResolved(
   598	        uint32 indexed banditId,
   599	        uint32 indexed targetClanId,
   600	        bool defended,
   601	        uint16 attackPower,
   602	        uint16 totalDefense,
   603	        uint16 wallLevelAfter,
   604	        uint256 stolenWood,
   605	        uint256 stolenIron,
   606	        uint256 stolenWheat,
   607	        uint256 stolenFish,
   608	        uint64 atTick
   609	    );
   610	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
   611	    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
   612	    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
   613	    event LootDistributedToDefender(
   614	        uint32 indexed banditId,
   615	        uint32 indexed clanId,
   616	        uint32 indexed clansmanId,
   617	        uint256 wood,
   618	        uint256 iron,
   619	        uint256 wheat,
   620	        uint256 fish
   621	    );
   622	
   623	    // ----- winter cold damage -----
   624	    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
   625	    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
   626	
   627	    // ----- OTC transfers -----
   628	    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   629	    event VaultResourceTransferred(
   630	        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
   631	    );
   632	    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   633	
   634	    // ----- treasury / pools -----
   635	    event PoolsSeeded(address woodGoldPool, address wheatGoldPool, address fishGoldPool, address ironGoldPool);
   636	}
   637	
   638	// =============================================================================
   639	// MAIN INTERFACE
   640	// =============================================================================
   641	
   642	interface IClanWorld is IClanWorldEvents {
   643	    // -------------------------------------------------------------------------
   644	    // World progression
   645	    // -------------------------------------------------------------------------
   646	
   647	    /// @notice Permissionless heartbeat. Closes the current tick, resolves
   648	    ///         scheduled market actions and world events, advances the tick.
   649	    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
   650	    function heartbeat() external;
   651	
   652	    /// @notice Lazily settle a clan forward to current tick. Idempotent.
   653	    function settleClan(uint32 clanId) external;
   654	
   655	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   656	    function settleClansman(uint32 csId) external;
   657	
   658	    /// @notice Finalize the current season. Permissionless after seasonEndTick.
   659	    function finalizeSeason() external;
   660	
   661	    // -------------------------------------------------------------------------
   662	    // Clan lifecycle
   663	    // -------------------------------------------------------------------------
   664	
   665	    /// @notice Mint a new clan iNFT and spawn its homebase in a valid region.
   666	    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
   667	
   668	    /// @notice Submit one or more orders for a single clan's clansmen.
   669	    ///         Per-order failures do not revert the tx.
   670	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
   671	
   672	    // -------------------------------------------------------------------------
   673	    // Treasury / pool seeding
   674	    // -------------------------------------------------------------------------
   675	
   676	    /// @notice Owner-only. Registers token and pool addresses once before seeding.
   677	    ///         tokens order: wood, iron, wheat, fish, gold, blueprint.
   678	    ///         pools order: wood, wheat, fish, iron.
   679	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external;
   680	
   681	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
   682	    function seedPools(PoolSeedConfig calldata cfg) external;
   683	
   684	    // -------------------------------------------------------------------------
   685	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
   686	    // -------------------------------------------------------------------------
   687	
   688	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   689	
   690	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
   691	
   692	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   693	
   694	    function transferBundle(
   695	        uint32 fromClanId,
   696	        uint32 toClanId,
   697	        uint256 gold,
   698	        uint256 blueprint,
   699	        uint256 wood,
   700	        uint256 iron,
   701	        uint256 wheat,
   702	        uint256 fish
   703	    ) external;
   704	
   705	    // -------------------------------------------------------------------------
   706	    // Raw read getters (committed storage, no settlement simulation)
   707	    // -------------------------------------------------------------------------
   708	
   709	    function getWorldState() external view returns (WorldState memory);
   710	
   711	    function getTreasuryState() external view returns (TreasuryState memory);
   712	
   713	    function getClan(uint32 clanId) external view returns (Clan memory);
   714	
   715	    function getClansman(uint32 clansmanId) external view returns (Clansman memory);
   716	
   717	    function getActiveMission(uint32 clansmanId) external view returns (Mission memory);
   718	
   719	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
   720	        external
   721	        view
   722	        returns (uint64 submitted, uint64 executes, uint64 settles);
   723	
   724	    /// @notice True iff currentTick is inside the recurring winter window.
   725	    function isWinter() external view returns (bool);
   726	
   727	    function getActionDuration(ActionType action) external pure returns (uint64);
   728	
   729	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
   730	
   731	    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
   732	
   733	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
   734	
   735	    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
   736	
   737	    function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds);
   738	
   739	    function getDefendingClans(uint8 region) external view returns (uint32[] memory clanIds);
   740	
   741	    // -------------------------------------------------------------------------
   742	    // Derived read getters (read-only simulation forward to current tick)
   743	    //
   744	    // Per v4.3 §J ("Derived Getter Non-Mutation Rule"), these MUST NOT mutate
   745	    // any storage, including settlement checkpoints, cached flags, or queues.
   746	    // -------------------------------------------------------------------------
   747	
   748	    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory);
   749	
   750	    function getDerivedClansmanState(uint32 clansmanId) external view returns (DerivedClansmanState memory);
   751	
   752	    /// @notice Non-binding preview. Bandit targeting is recomputed at attack
   753	    ///         resolution time using then-current eagerly settled state.
   754	    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
   755	
   756	    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external view returns (uint8 travelTicks, bytes8 path);
   757	
   758	    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);
   759	
   760	    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);
   761	
   762	    // -------------------------------------------------------------------------
   763	    // UI indexer aggregator getters (v4.4 additions)
   764	    //
   765	    // These are pure compositions of the derived getters above and exist only
   766	    // to reduce indexer RPC traffic. They MUST follow the same non-mutation
   767	    // rule as the underlying derived getters.
   768	    // -------------------------------------------------------------------------
   769	
   770	    /// @notice Single-call top-level world state plus per-clan leaderboard data.
   771	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
   772	    function getWorldSnapshot() external view returns (WorldSnapshot memory);
   773	
   774	    /// @notice Single-call complete clan rendering data: derived clan + all
   775	    ///         clansmen with derived state and active missions + plot states +
   776	    ///         defender bookkeeping. Drives the entire sprite layer.
   777	    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory);
   778	
   779	    /// @notice Single-call market panel data: 4 pool reserves + spot prices +
   780	    ///         scheduled queues for current and next tick. Drives market UI
   781	    ///         and indexer's per-tick price-history sampling.
   782	    function getMarketState() external view returns (MarketState memory);
   783	
   784	    /// @notice Single-call bandit drama state, including projected target if
   785	    ///         attack resolved this tick. Drives the bandit warning UI.
   786	    function getActiveBanditView() external view returns (ActiveBanditView memory);
   787	
   788	    /// @notice Optional. List clansmen currently in a region for tap-to-inspect
   789	    ///         tooltips. Can be derived clientside; included for completeness.
   790	    function getRegionPopulation(uint8 region) external view returns (RegionOccupant[] memory);
   791	}

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorldStub.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
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
    40	
    41	/// @notice Stub implementation of IClanWorld for Base Sepolia deployment.
    42	///         Stores tick state and token/pool addresses. All game logic is no-op.
    43	contract ClanWorldStub is IClanWorld {
    44	    WorldState private _world;
    45	    TreasuryState private _treasury;
    46	
    47	    constructor(address[6] memory tokens, address[4] memory pools) {
    48	        _world.currentTick = 1;
    49	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
    50	        _world.seasonStartTick = 0;
    51	        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
    52	        _world.currentSeasonNumber = 1;
    53	        _world.nextHeartbeatAtTick = _world.currentTick + 1;
    54	        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
    55	        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
    56	        _world.winterActive = false;
    57	
    58	        _treasury.woodToken = tokens[0];
    59	        _treasury.ironToken = tokens[1];
    60	        _treasury.wheatToken = tokens[2];
    61	        _treasury.fishToken = tokens[3];
    62	        _treasury.goldToken = tokens[4];
    63	        _treasury.blueprintToken = tokens[5];
    64	
    65	        _treasury.woodGoldPool = pools[0];
    66	        _treasury.wheatGoldPool = pools[1];
    67	        _treasury.fishGoldPool = pools[2];
    68	        _treasury.ironGoldPool = pools[3];
    69	
    70	        _treasury.treasuryOwner = msg.sender;
    71	    }
    72	
    73	    // -------------------------------------------------------------------------
    74	    // World progression
    75	    // -------------------------------------------------------------------------
    76	
    77	    function heartbeat() external override {
    78	        uint64 closed = _world.currentTick;
    79	        _world.currentTick += 1;
    80	        _world.nextHeartbeatAtTick = _world.currentTick + 1;
    81	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
    82	        bool wasWinter = _isWinterActiveAt(closed);
    83	        bool nowWinter = _isWinterActiveAt(_world.currentTick);
    84	        if (!wasWinter && nowWinter) {
    85	            emit WinterStarted(_winterEventTick(_world.currentTick));
    86	        }
    87	        if (wasWinter && !nowWinter) {
    88	            emit WinterEnded(_winterEventTick(_world.currentTick));
    89	        }
    90	        emit TickAdvanced(closed, _world.currentTick, bytes32(0));
    91	    }
    92	
    93	    function settleClan(uint32) external override {}
    94	
    95	    function settleClansman(uint32) external override {}
    96	
    97	    function finalizeSeason() external override {}
    98	
    99	    // -------------------------------------------------------------------------
   100	    // Clan lifecycle
   101	    // -------------------------------------------------------------------------
   102	
   103	    function mintClan(address) external override returns (uint32, uint256) {
   104	        return (1, 1);
   105	    }
   106	
   107	    function submitClanOrders(uint32, ClanOrder[] calldata orders)
   108	        external
   109	        override
   110	        returns (OrderResult[] memory results)
   111	    {
   112	        results = new OrderResult[](orders.length);
   113	    }
   114	
   115	    // -------------------------------------------------------------------------
   116	    // Treasury
   117	    // -------------------------------------------------------------------------
   118	
   119	    function initTreasury(address[6] calldata, address[4] calldata) external override {}
   120	
   121	    function seedPools(PoolSeedConfig calldata) external override {}
   122	
   123	    // -------------------------------------------------------------------------
   124	    // OTC transfers
   125	    // -------------------------------------------------------------------------
   126	
   127	    function transferGold(uint32, uint32, uint256) external override {}
   128	
   129	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external override {}
   130	
   131	    function transferBlueprint(uint32, uint32, uint256) external override {}
   132	
   133	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256) external override {}
   134	
   135	    // -------------------------------------------------------------------------
   136	    // Raw read getters
   137	    // -------------------------------------------------------------------------
   138	
   139	    function getWorldState() external view override returns (WorldState memory) {
   140	        return _worldStateView();
   141	    }
   142	
   143	    function getTreasuryState() external view override returns (TreasuryState memory) {
   144	        return _treasury;
   145	    }
   146	
   147	    function getClan(uint32) external pure override returns (Clan memory) {
   148	        return Clan({
   149	            clanId: 0,
   150	            iftTokenId: 0,
   151	            owner: address(0),
   152	            clanState: ClanState.ACTIVE,
   153	            baseRegion: 0,
   154	            baseLevel: 0,
   155	            wallLevel: 0,
   156	            monumentLevel: 0,
   157	            livingClansmen: 0,
   158	            lastSettledTick: 0,
   159	            starvationStartsAtTick: 0,
   160	            coldDamage: 0,
   161	            goldBalance: 0,
   162	            blueprintBalance: 0,
   163	            vaultWood: 0,
   164	            vaultIron: 0,
   165	            vaultWheat: 0,
   166	            vaultFish: 0
   167	        });
   168	    }
   169	
   170	    function getClansman(uint32) external pure override returns (Clansman memory) {
   171	        return Clansman({
   172	            clansmanId: 0,
   173	            clanId: 0,
   174	            state: ClansmanState.WAITING,
   175	            currentRegion: 0,
   176	            cooldownEndsAtTs: 0,
   177	            lastMissionNonce: 0,
   178	            carryWood: 0,
   179	            carryIron: 0,
   180	            carryWheat: 0,
   181	            carryFish: 0
   182	        });
   183	    }
   184	
   185	    function getActiveMission(uint32) external pure override returns (Mission memory) {
   186	        return Mission({
   187	            active: false,
   188	            nonce: 0,
   189	            submittedAtTick: 0,
   190	            executesAtTick: 0,
   191	            settlesAtTick: 0,
   192	            clansmanId: 0,
   193	            startRegion: 0,
   194	            targetRegion: 0,
   195	            action: ActionType.None,
   196	            startTick: 0,
   197	            arrivalTick: 0,
   198	            actionStartTick: 0,
   199	            missionSeed: bytes32(0),
   200	            marketMode: MarketExecutionMode.None,
   201	            targetClanId: 0,
   202	            marketToken: address(0),
   203	            marketAmount: 0,
   204	            maxGoldIn: 0
   205	        });
   206	    }
   207	
   208	    function getMissionTiming(uint32, uint32)
   209	        external
   210	        pure
   211	        override
   212	        returns (uint64 submitted, uint64 executes, uint64 settles)
   213	    {
   214	        return (0, 0, 0);
   215	    }
   216	
   217	    function isWinter() external view override returns (bool) {
   218	        return _isWinterActiveAt(_world.currentTick);
   219	    }
   220	
   221	    function getActionDuration(ActionType) external pure override returns (uint64) {
   222	        return 0;
   223	    }
   224	
   225	    function getTravelTicks(uint8, uint8) external pure override returns (uint64) {
   226	        return 0;
   227	    }
   228	
   229	    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
   230	        return BanditTroop({
   231	            banditId: 0,
   232	            state: BanditState.NONE,
   233	            currentRegion: 0,
   234	            attackAttemptsMade: 0,
   235	            stateEnteredTick: 0,
   236	            nextActionTick: 0,
   237	            tier: 0,
   238	            attackPower: 0,
   239	            carryWood: 0,
   240	            carryIron: 0,

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorldStub.sol | nl -ba | sed -n '240,440p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   240	            carryIron: 0,
   241	            carryWheat: 0,
   242	            carryFish: 0
   243	        });
   244	    }
   245	
   246	    function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
   247	        west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
   248	        east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
   249	    }
   250	
   251	    function getScheduledMarketActionsForTick(uint64) external pure override returns (ScheduledMarketAction[] memory) {
   252	        return new ScheduledMarketAction[](0);
   253	    }
   254	
   255	    function getActiveDefenders(uint32) external pure override returns (uint32[] memory) {
   256	        return new uint32[](0);
   257	    }
   258	
   259	    function getDefendingClans(uint8) external pure override returns (uint32[] memory) {
   260	        return new uint32[](0);
   261	    }
   262	
   263	    // -------------------------------------------------------------------------
   264	    // Derived read getters
   265	    // -------------------------------------------------------------------------
   266	
   267	    function getDerivedClanState(uint32) external view override returns (DerivedClanState memory) {
   268	        Clan memory c = this.getClan(0);
   269	        return DerivedClanState({clan: c, isStarving: false, lootValue: 0, derivedAtTick: _world.currentTick});
   270	    }
   271	
   272	    function getDerivedClansmanState(uint32) external view override returns (DerivedClansmanState memory) {
   273	        Clansman memory cm = this.getClansman(0);
   274	        Mission memory m = this.getActiveMission(0);
   275	        return
   276	            DerivedClansmanState({
   277	                clansman: cm, activeMission: m, effectiveRegion: 0, derivedAtTick: _world.currentTick
   278	            });
   279	    }
   280	
   281	    function getBanditTargetPreview(uint32) external pure override returns (uint32) {
   282	        return 0;
   283	    }
   284	
   285	    function quoteTravel(uint8, uint8) external pure override returns (uint8, bytes8) {
   286	        return (0, bytes8(0));
   287	    }
   288	
   289	    function quoteLootValueRaw(uint32) external pure override returns (uint256) {
   290	        return 0;
   291	    }
   292	
   293	    function quoteLootValueSettled(uint32) external pure override returns (uint256) {
   294	        return 0;
   295	    }
   296	
   297	    // -------------------------------------------------------------------------
   298	    // UI indexer aggregator getters
   299	    // -------------------------------------------------------------------------
   300	
   301	    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
   302	        WorldState memory ws = _worldStateView();
   303	        return WorldSnapshot({
   304	            currentTick: ws.currentTick,
   305	            seasonStartTick: ws.seasonStartTick,
   306	            seasonEndTick: ws.seasonEndTick,
   307	            seasonFinalized: false,
   308	            currentSeasonNumber: ws.currentSeasonNumber,
   309	            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
   310	            winterActive: ws.winterActive,
   311	            winterStartsAtTick: ws.winterStartsAtTick,
   312	            winterEndsAtTick: ws.winterEndsAtTick,
   313	            activeBanditId: 0,
   314	            currentTickSeed: bytes32(0),
   315	            leaderboard: new LeaderboardEntry[](0)
   316	        });
   317	    }
   318	
   319	    function getClanFullView(uint32) external view override returns (ClanFullView memory) {
   320	        return ClanFullView({
   321	            clan: DerivedClanState({
   322	                clan: this.getClan(0), isStarving: false, lootValue: 0, derivedAtTick: _world.currentTick
   323	            }),
   324	            clansmen: new ClansmanFullView[](0),
   325	            westPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
   326	            eastPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
   327	            incomingDefenderIds: new uint32[](0),
   328	            thisClanDefendingBaseId: 0
   329	        });
   330	    }
   331	
   332	    function getMarketState() external view override returns (MarketState memory) {
   333	        return MarketState({
   334	            wood: PoolReserves({
   335	                resourceToken: _treasury.woodToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   336	            }),
   337	            wheat: PoolReserves({
   338	                resourceToken: _treasury.wheatToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   339	            }),
   340	            fish: PoolReserves({
   341	                resourceToken: _treasury.fishToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   342	            }),
   343	            iron: PoolReserves({
   344	                resourceToken: _treasury.ironToken, resourceReserve: 0, goldReserve: 0, spotPriceGoldPerResource: 0
   345	            }),
   346	            currentTick: _world.currentTick,
   347	            currentTickQueue: new ScheduledMarketAction[](0),
   348	            nextTickQueue: new ScheduledMarketAction[](0)
   349	        });
   350	    }
   351	
   352	    function getActiveBanditView() external pure override returns (ActiveBanditView memory) {
   353	        return ActiveBanditView({
   354	            exists: false,
   355	            banditId: 0,
   356	            state: BanditState.NONE,
   357	            currentRegion: 0,
   358	            attackAttemptsMade: 0,
   359	            maxAttemptsRemaining: 0,
   360	            stateEnteredTick: 0,
   361	            nextActionTick: 0,
   362	            tier: 0,
   363	            attackPower: 0,
   364	            carryWood: 0,
   365	            carryIron: 0,
   366	            carryWheat: 0,
   367	            carryFish: 0,
   368	            projectedTargetClanId: 0,
   369	            projectedTargetLootValue: 0
   370	        });
   371	    }
   372	
   373	    function getRegionPopulation(uint8) external pure override returns (RegionOccupant[] memory) {
   374	        return new RegionOccupant[](0);
   375	    }
   376	
   377	    function _isWinterActiveAt(uint64 tick) internal pure returns (bool) {
   378	        if (tick < ClanWorldConstants.WINTER_START_TICK) {
   379	            return false;
   380	        }
   381	        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
   382	        return elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
   383	    }
   384	
   385	    function _winterEventTick(uint64 tick) internal pure returns (uint64) {
   386	        return tick;
   387	    }
   388	
   389	    function _winterWindowForTick(uint64 tick)
   390	        internal
   391	        pure
   392	        returns (bool active, uint64 startsAtTick, uint64 endsAtTick)
   393	    {
   394	        if (tick < ClanWorldConstants.WINTER_START_TICK) {
   395	            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
   396	            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
   397	            return (false, startsAtTick, endsAtTick);
   398	        }
   399	
   400	        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
   401	        uint64 cycleIndex = elapsed / ClanWorldConstants.WINTER_PERIOD_TICKS;
   402	        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
   403	        active = elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
   404	        startsAtTick = active ? cycleStart : cycleStart + ClanWorldConstants.WINTER_PERIOD_TICKS;
   405	        endsAtTick = startsAtTick + ClanWorldConstants.WINTER_DURATION_TICKS;
   406	    }
   407	
   408	    function _worldStateView() internal view returns (WorldState memory ws) {
   409	        ws = _world;
   410	        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
   411	    }
   412	}

exec
/bin/bash -lc "git show 400463e:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
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
    39	          { name: 'currentSeasonNumber', type: 'uint64' },
    40	          { name: 'nextHeartbeatAtTick', type: 'uint64' },
    41	          { name: 'winterActive', type: 'bool' },
    42	          { name: 'winterStartsAtTick', type: 'uint64' },
    43	          { name: 'winterEndsAtTick', type: 'uint64' },
    44	          { name: 'activeBanditId', type: 'uint32' },
    45	          { name: 'currentTickSeed', type: 'bytes32' },
    46	          {
    47	            name: 'leaderboard',
    48	            type: 'tuple[]',
    49	            components: [
    50	              { name: 'clanId', type: 'uint32' },
    51	              { name: 'owner', type: 'address' },
    52	              { name: 'monumentLevel', type: 'uint8' },
    53	              { name: 'baseLevel', type: 'uint8' },
    54	              { name: 'wallLevel', type: 'uint8' },
    55	              { name: 'livingClansmen', type: 'uint8' },
    56	              { name: 'state', type: 'uint8' },
    57	              { name: 'lootValue', type: 'uint256' },
    58	            ],
    59	          },
    60	        ],
    61	      },
    62	    ],
    63	    stateMutability: 'view',
    64	  },
    65	  {
    66	    type: 'function',
    67	    name: 'getClanFullView',
    68	    inputs: [{ name: '', type: 'uint32' }],
    69	    outputs: [
    70	      {
    71	        name: '',
    72	        type: 'tuple',
    73	        components: [
    74	          {
    75	            name: 'clan',
    76	            type: 'tuple',
    77	            components: [
    78	              {
    79	                name: 'clan',
    80	                type: 'tuple',
    81	                components: [
    82	                  { name: 'clanId', type: 'uint32' },
    83	                  { name: 'iftTokenId', type: 'uint256' },
    84	                  { name: 'owner', type: 'address' },
    85	                  { name: 'clanState', type: 'uint8' },
    86	                  { name: 'baseRegion', type: 'uint8' },
    87	                  { name: 'baseLevel', type: 'uint8' },
    88	                  { name: 'wallLevel', type: 'uint8' },
    89	                  { name: 'monumentLevel', type: 'uint8' },
    90	                  { name: 'livingClansmen', type: 'uint8' },
    91	                  { name: 'lastSettledTick', type: 'uint64' },
    92	                  { name: 'starvationStartsAtTick', type: 'uint64' },
    93	                  { name: 'coldDamage', type: 'uint16' },
    94	                  { name: 'goldBalance', type: 'uint256' },
    95	                  { name: 'blueprintBalance', type: 'uint256' },
    96	                  { name: 'vaultWood', type: 'uint256' },
    97	                  { name: 'vaultIron', type: 'uint256' },
    98	                  { name: 'vaultWheat', type: 'uint256' },
    99	                  { name: 'vaultFish', type: 'uint256' },
   100	                ],
   101	              },
   102	              { name: 'isStarving', type: 'bool' },
   103	              { name: 'lootValue', type: 'uint256' },
   104	              { name: 'derivedAtTick', type: 'uint64' },
   105	            ],
   106	          },
   107	          // clansmen, westPlot, eastPlot etc. omitted — not needed for Wave 0 mapping
   108	          {
   109	            name: 'clansmen',
   110	            type: 'tuple[]',
   111	            components: [
   112	              {
   113	                name: 'clansman',
   114	                type: 'tuple',
   115	                components: [
   116	                  {
   117	                    name: 'clansman',
   118	                    type: 'tuple',
   119	                    components: [
   120	                      { name: 'clansmanId', type: 'uint32' },
   121	                      { name: 'clanId', type: 'uint32' },
   122	                      { name: 'state', type: 'uint8' },
   123	                      { name: 'currentRegion', type: 'uint8' },
   124	                      { name: 'cooldownEndsAtTs', type: 'uint64' },
   125	                      { name: 'lastMissionNonce', type: 'uint64' },
   126	                      { name: 'carryWood', type: 'uint256' },
   127	                      { name: 'carryIron', type: 'uint256' },
   128	                      { name: 'carryWheat', type: 'uint256' },
   129	                      { name: 'carryFish', type: 'uint256' },
   130	                    ],
   131	                  },
   132	                  {
   133	                    name: 'activeMission',
   134	                    type: 'tuple',
   135	                    components: [
   136	                      { name: 'active', type: 'bool' },
   137	                      { name: 'nonce', type: 'uint64' },
   138	                      { name: 'clansmanId', type: 'uint32' },
   139	                      { name: 'startRegion', type: 'uint8' },
   140	                      { name: 'targetRegion', type: 'uint8' },
   141	                      { name: 'action', type: 'uint8' },
   142	                      { name: 'startTick', type: 'uint64' },
   143	                      { name: 'arrivalTick', type: 'uint64' },
   144	                      { name: 'actionStartTick', type: 'uint64' },
   145	                      { name: 'missionSeed', type: 'bytes32' },
   146	                      { name: 'marketMode', type: 'uint8' },
   147	                      { name: 'targetClanId', type: 'uint32' },
   148	                      { name: 'marketToken', type: 'address' },
   149	                      { name: 'marketAmount', type: 'uint256' },
   150	                      { name: 'maxGoldIn', type: 'uint256' },
   151	                    ],
   152	                  },
   153	                  { name: 'effectiveRegion', type: 'uint8' },
   154	                  { name: 'derivedAtTick', type: 'uint64' },
   155	                ],
   156	              },
   157	              {
   158	                name: 'activeMission',
   159	                type: 'tuple',
   160	                components: [
   161	                  { name: 'active', type: 'bool' },
   162	                  { name: 'nonce', type: 'uint64' },
   163	                  { name: 'clansmanId', type: 'uint32' },
   164	                  { name: 'startRegion', type: 'uint8' },
   165	                  { name: 'targetRegion', type: 'uint8' },
   166	                  { name: 'action', type: 'uint8' },
   167	                  { name: 'startTick', type: 'uint64' },
   168	                  { name: 'arrivalTick', type: 'uint64' },
   169	                  { name: 'actionStartTick', type: 'uint64' },
   170	                  { name: 'missionSeed', type: 'bytes32' },
   171	                  { name: 'marketMode', type: 'uint8' },
   172	                  { name: 'targetClanId', type: 'uint32' },
   173	                  { name: 'marketToken', type: 'address' },
   174	                  { name: 'marketAmount', type: 'uint256' },
   175	                  { name: 'maxGoldIn', type: 'uint256' },
   176	                ],
   177	              },
   178	            ],
   179	          },
   180	          {
   181	            name: 'westPlot',
   182	            type: 'tuple',
   183	            components: [
   184	              { name: 'state', type: 'uint8' },
   185	              { name: 'region', type: 'uint8' },
   186	              { name: 'remainingWheat', type: 'uint256' },
   187	              { name: 'regrowUntilTick', type: 'uint64' },
   188	            ],
   189	          },
   190	          {
   191	            name: 'eastPlot',
   192	            type: 'tuple',
   193	            components: [
   194	              { name: 'state', type: 'uint8' },
   195	              { name: 'region', type: 'uint8' },
   196	              { name: 'remainingWheat', type: 'uint256' },
   197	              { name: 'regrowUntilTick', type: 'uint64' },
   198	            ],
   199	          },
   200	          { name: 'incomingDefenderIds', type: 'uint32[]' },
   201	          { name: 'thisClanDefendingBaseId', type: 'uint32' },
   202	        ],
   203	      },
   204	    ],
   205	    stateMutability: 'view',
   206	  },
   207	  {
   208	    name: 'submitClanOrders',
   209	    type: 'function',
   210	    inputs: [
   211	      { name: 'clanId', type: 'uint32' },
   212	      {
   213	        name: 'orders',
   214	        type: 'tuple[]',
   215	        components: [
   216	          { name: 'clansmanId', type: 'uint32' },
   217	          { name: 'gotoRegion', type: 'uint8' },
   218	          { name: 'action', type: 'uint8' },
   219	          { name: 'targetClanId', type: 'uint32' },
   220	          { name: 'marketToken', type: 'address' },

exec
/bin/bash -lc "git show 400463e:packages/runner/src/runnerCastHeartbeat.ts | nl -ba | sed -n '1,220p'" in /home/claude/code/clan-world
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
    43	          { name: 'currentSeasonNumber', type: 'uint64' },
    44	          { name: 'nextHeartbeatAtTick', type: 'uint64' },
    45	          { name: 'nextHeartbeatAtTs', type: 'uint64' },
    46	          { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
    47	          { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
    48	          { name: 'currentTickSeed', type: 'bytes32' },
    49	          { name: 'activeBanditId', type: 'uint32' },
    50	          { name: 'winterActive', type: 'bool' },
    51	          { name: 'winterStartsAtTick', type: 'uint64' },
    52	          { name: 'winterEndsAtTick', type: 'uint64' },
    53	          { name: 'nextCommitSequence', type: 'uint64' },
    54	        ],
    55	      },
    56	    ],
    57	    stateMutability: 'view',
    58	  },
    59	] as const;
    60	
    61	export interface RunnerHeartbeatConfig {
    62	  /** Hex-encoded 64-char private key, optionally 0x-prefixed. */
    63	  privateKey: string;
    64	  /** Override RPC URL; defaults to the Base Sepolia public endpoint. */
    65	  rpcUrl?: string;
    66	  /** ClanWorld contract address. */
    67	  contractAddress: `0x${string}`;
    68	}
    69	
    70	/**
    71	 * Reads `RUNNER_PRIVATE_KEY`, `RPC_URL_PRIMARY`, `CLAN_WORLD_CONTRACT_ADDRESS`
    72	 * from env. Throws if `RUNNER_PRIVATE_KEY` is missing — the runner intentionally
    73	 * does not generate or store its own wallet; provisioning is operator-side.
    74	 */
    75	export function configFromEnv(env: NodeJS.ProcessEnv = process.env): RunnerHeartbeatConfig {
    76	  const pk = env['RUNNER_PRIVATE_KEY'];
    77	  if (!pk) {
    78	    throw new Error(
    79	      'RUNNER_PRIVATE_KEY is not set — the runner needs a dedicated wallet (NEVER reuse an Elder wallet). ' +
    80	        'Provision a fresh key, fund it with testnet ETH, and export RUNNER_PRIVATE_KEY before starting the daemon.',
    81	    );
    82	  }
    83	  const contractAddress = env['CLAN_WORLD_CONTRACT_ADDRESS'];
    84	  if (!contractAddress || !/^0x[0-9a-fA-F]{40}$/.test(contractAddress)) {
    85	    throw new Error(
    86	      `CLAN_WORLD_CONTRACT_ADDRESS missing or invalid; expected 0x-prefixed 40-hex-char address, got ${String(contractAddress)}`,
    87	    );
    88	  }
    89	  return {
    90	    privateKey: pk,
    91	    rpcUrl: env['RPC_URL_PRIMARY'] || env['RPC_URL_FALLBACK'],
    92	    contractAddress: contractAddress as `0x${string}`,
    93	  };
    94	}
    95	
    96	/**
    97	 * Viem-backed `IHeartbeatCaller`. Wallet account is the dedicated runner key.
    98	 *
    99	 * Rate-limit detection: ClanWorld.heartbeat() reverts when called before
   100	 * `nextHeartbeatAtTs`. We don't have a typed custom error in the ABI, so on
   101	 * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
   102	 * still in the future, throw `HeartbeatRateLimitedError(nextAllowedAt)`.
   103	 * Other reverts surface as the original error.
   104	 */
   105	export class RunnerCastHeartbeat implements IHeartbeatCaller {
   106	  private readonly publicClient: PublicClient;
   107	  private readonly walletClient: WalletClient;
   108	  private readonly account: Account;
   109	  private readonly contractAddress: `0x${string}`;
   110	
   111	  constructor(cfg: RunnerHeartbeatConfig) {
   112	    const pk = normalizePk(cfg.privateKey);
   113	    this.account = privateKeyToAccount(pk);
   114	    const transport = cfg.rpcUrl ? http(cfg.rpcUrl) : http();
   115	    this.publicClient = createPublicClient({ chain: baseSepolia, transport });
   116	    this.walletClient = createWalletClient({
   117	      account: this.account,
   118	      chain: baseSepolia,
   119	      transport,
   120	    });
   121	    this.contractAddress = cfg.contractAddress;
   122	  }
   123	
   124	  async callHeartbeat(): Promise<{ txHash: string }> {
   125	    try {
   126	      const hash = await this.walletClient.writeContract({
   127	        account: this.account,
   128	        chain: baseSepolia,
   129	        address: this.contractAddress,
   130	        abi: HEARTBEAT_ABI,
   131	        functionName: 'heartbeat',
   132	        args: [],
   133	      });
   134	      // Wait for confirmation per the seam contract ("not fire-and-forget").
   135	      const receipt = await this.publicClient.waitForTransactionReceipt({ hash });
   136	      if (receipt.status !== 'success') {
   137	        // Mined-but-reverted. Most common cause is the rate-limit window
   138	        // hadn't elapsed yet (when simulation succeeded but execution didn't).
   139	        // Re-read state to upgrade to HeartbeatRateLimitedError when applicable.
   140	        const next = await this.readNextHeartbeatAt().catch(() => undefined);
   141	        if (next !== undefined && next > Math.floor(Date.now() / 1000)) {
   142	          throw new HeartbeatRateLimitedError(next);
   143	        }
   144	        throw new Error(`heartbeat tx ${hash} reverted on-chain`);
   145	      }
   146	      return { txHash: hash };
   147	    } catch (err) {
   148	      // Already a rate-limit error — rethrow immediately; no second RPC read.
   149	      if (err instanceof HeartbeatRateLimitedError) throw err;
   150	      // Attempt to upgrade only simulation-level contract reverts to
   151	      // HeartbeatRateLimitedError; pre-flight/RPC errors must surface unchanged.
   152	      if (!(err instanceof ContractFunctionRevertedError)) throw err;
   153	      const next = await this.readNextHeartbeatAt().catch(() => undefined);
   154	      if (next !== undefined && next > Math.floor(Date.now() / 1000)) {
   155	        throw new HeartbeatRateLimitedError(next);
   156	      }
   157	      throw err;
   158	    }
   159	  }
   160	
   161	  async isHeartbeatDue(): Promise<boolean> {
   162	    const next = await this.readNextHeartbeatAt();
   163	    return next <= Math.floor(Date.now() / 1000);
   164	  }
   165	
   166	  private async readNextHeartbeatAt(): Promise<number> {
   167	    const state = await this.publicClient.readContract({
   168	      address: this.contractAddress,
   169	      abi: HEARTBEAT_ABI,
   170	      functionName: 'getWorldState',
   171	      args: [],
   172	    });
   173	    // viem decodes the named tuple into an object with the same field names.
   174	    return Number((state as { nextHeartbeatAtTs: bigint }).nextHeartbeatAtTs);
   175	  }
   176	}
   177	
   178	function normalizePk(pk: string): `0x${string}` {
   179	  const trimmed = pk.trim();
   180	  const withPrefix = trimmed.startsWith('0x') ? trimmed : `0x${trimmed}`;
   181	  if (!/^0x[0-9a-fA-F]{64}$/.test(withPrefix)) {
   182	    throw new Error(
   183	      'RUNNER_PRIVATE_KEY is not a valid 64-hex-char private key (0x-prefixed optional)',
   184	    );
   185	  }
   186	  return withPrefix as `0x${string}`;
   187	}

exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | jq '.[] | select(.name==\"getWorldState\")'" in /home/claude/code/clan-world
 exited 5 in 0ms:
jq: error (at <stdin>:3806): Cannot index array with string "name"

exec
/bin/bash -lc "python - <<'PY'
import json,sys
j=json.load(open('/tmp/x','w'))
PY" in /home/claude/code/clan-world
 exited 127 in 0ms:
/bin/bash: line 1: python: command not found

exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | sed -n '1,30p'" in /home/claude/code/clan-world
 succeeded in 0ms:
{
  "abi": [
    {
      "type": "function",
      "name": "finalizeSeason",
      "inputs": [],
      "outputs": [],
      "stateMutability": "nonpayable"
    },
    {
      "type": "function",
      "name": "getActionDuration",
      "inputs": [
        {
          "name": "action",
          "type": "uint8",
          "internalType": "enum ActionType"
        }
      ],
      "outputs": [
        {
          "name": "",
          "type": "uint64",
          "internalType": "uint64"
        }
      ],
      "stateMutability": "pure"
    },
    {
      "type": "function",

exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | jq '.abi[] | select(.name==\"getWorldState\")'" in /home/claude/code/clan-world
 succeeded in 0ms:
{
  "type": "function",
  "name": "getWorldState",
  "inputs": [],
  "outputs": [
    {
      "name": "",
      "type": "tuple",
      "internalType": "struct WorldState",
      "components": [
        {
          "name": "currentTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "seasonStartTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "seasonEndTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "seasonFinalized",
          "type": "bool",
          "internalType": "bool"
        },
        {
          "name": "currentSeasonNumber",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "nextHeartbeatAtTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "nextHeartbeatAtTs",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "nextBanditSpawnEligibleTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "currentBanditSpawnChanceBps",
          "type": "uint16",
          "internalType": "uint16"
        },
        {
          "name": "currentTickSeed",
          "type": "bytes32",
          "internalType": "bytes32"
        },
        {
          "name": "activeBanditId",
          "type": "uint32",
          "internalType": "uint32"
        },
        {
          "name": "winterActive",
          "type": "bool",
          "internalType": "bool"
        },
        {
          "name": "winterStartsAtTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "winterEndsAtTick",
          "type": "uint64",
          "internalType": "uint64"
        },
        {
          "name": "nextCommitSequence",
          "type": "uint64",
          "internalType": "uint64"
        }
      ]
    }
  ],
  "stateMutability": "view"
}

codex
I’m now validating the new tests against actual runtime behavior to make sure fix-round tests didn’t codify incorrect assumptions. After that I’ll merge subagent findings and produce the final severity-ordered review.
exec
/bin/bash -lc "git show 400463e:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '1950,2360p'" in /home/claude/code/clan-world
 succeeded in 1ms:
  1950	            gotoRegion: ClanWorldConstants.REGION_NOOP,
  1951	            action: ActionType.Wait,
  1952	            targetClanId: 0,
  1953	            marketToken: address(0),
  1954	            marketAmount: 0,
  1955	            maxGoldIn: 0
  1956	        });
  1957	
  1958	        // Order 3: invalid — cs2 ChopWood to Mountains (wrong region for ChopWood)
  1959	        orders[3] = ClanOrder({
  1960	            clansmanId: cs2,
  1961	            gotoRegion: ClanWorldConstants.REGION_MOUNTAINS,
  1962	            action: ActionType.ChopWood,
  1963	            targetClanId: 0,
  1964	            marketToken: address(0),
  1965	            marketAmount: 0,
  1966	            maxGoldIn: 0
  1967	        });
  1968	
  1969	        vm.prank(elder);
  1970	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
  1971	
  1972	        assertEq(results.length, 4, "3.E8: must return 4 results");
  1973	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "3.E8: order[0] must be OK");
  1974	        assertEq(
  1975	            uint8(results[1].status),
  1976	            uint8(StatusCode.ERR_INVALID_CLANSMAN),
  1977	            "3.E8: order[1] must be ERR_INVALID_CLANSMAN"
  1978	        );
  1979	        assertEq(uint8(results[2].status), uint8(StatusCode.OK), "3.E8: order[2] must be OK");
  1980	        assertEq(
  1981	            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"
  1982	        );
  1983	    }
  1984	
  1985	    // =====================================================================
  1986	    // Phase 4.4 — season + winter timer tests
  1987	    // =====================================================================
  1988	
  1989	    function test_season_initialState() public {
  1990	        WorldState memory ws = world.getWorldState();
  1991	        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
  1992	        assertEq(ws.seasonStartTick, 0);
  1993	        assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
  1994	        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
  1995	        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
  1996	        assertFalse(ws.winterActive);
  1997	        assertFalse(world.isWinter());
  1998	    }
  1999	
  2000	    function test_worldSnapshot_initialWinterStartsAtTick() public view {
  2001	        WorldSnapshot memory snapshot = world.getWorldSnapshot();
  2002	        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
  2003	        assertEq(snapshot.winterStartsAtTick, 110);
  2004	    }
  2005	
  2006	    function test_winter_onset() public {
  2007	        _mintClan();
  2008	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2009	        _advanceToTick(winterStart - 1);
  2010	
  2011	        vm.recordLogs();
  2012	        _advanceTick();
  2013	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2014	
  2015	        assertEq(_countLogs(logs, keccak256("WinterStarted(uint64)")), 1, "WinterStarted emits once");
  2016	        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
  2017	
  2018	        _advanceTick();
  2019	        WorldState memory ws = world.getWorldState();
  2020	        assertTrue(world.isWinter(), "winter should be active past start tick");
  2021	        assertTrue(ws.winterActive, "world state should report winter active");
  2022	        assertEq(ws.winterStartsAtTick, winterStart);
  2023	        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
  2024	    }
  2025	
  2026	    function test_winter_end_and_next_cycle() public {
  2027	        _mintClan();
  2028	        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  2029	        _advanceToTick(winterEnd - 1);
  2030	
  2031	        vm.recordLogs();
  2032	        _advanceTick();
  2033	        _advanceTick();
  2034	        _advanceTick();
  2035	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2036	
  2037	        WorldState memory ws = world.getWorldState();
  2038	        assertFalse(ws.winterActive, "winter should be over");
  2039	        assertFalse(world.isWinter(), "isWinter should be false after winter end");
  2040	        assertEq(_countLogs(logs, keccak256("WinterEnded(uint64)")), 1, "WinterEnded emits once");
  2041	        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
  2042	    }
  2043	
  2044	    function test_winter_restarts_after_full_period() public {
  2045	        _mintClan();
  2046	        uint64 nextWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS;
  2047	        _advanceToTick(nextWinterStart - 1);
  2048	
  2049	        vm.recordLogs();
  2050	        _advanceTick();
  2051	        _advanceTick();
  2052	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2053	
  2054	        assertEq(_countLogs(logs, keccak256("WinterStarted(uint64)")), 1, "next WinterStarted emits once");
  2055	        assertTrue(world.isWinter(), "winter should be active in next period");
  2056	        assertEq(world.getWorldState().currentTick, nextWinterStart + 1);
  2057	    }
  2058	
  2059	    function test_winter_cropTransitions_lockThenRestartRegrow() public {
  2060	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2061	        world = harness;
  2062	        uint32 clanId1 = _mintClan();
  2063	        uint32 clanId2 = _mintClan();
  2064	        harness.setClanUpkeepState(clanId1, 0, 1000e18, 1000e18, 1000e18, 0);
  2065	        harness.setClanUpkeepState(clanId2, 0, 1000e18, 1000e18, 1000e18, 0);
  2066	
  2067	        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
  2068	        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
  2069	        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
  2070	
  2071	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2072	        _advanceToTick(winterStart - 1);
  2073	        _advanceTick();
  2074	
  2075	        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
  2076	        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
  2077	        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
  2078	        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
  2079	        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
  2080	        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
  2081	        assertEq(west1.remainingWheat, 0, "winter lock clears remaining wheat");
  2082	        assertEq(east1.remainingWheat, 0, "winter lock clears all plots");
  2083	        assertEq(west1.regrowUntilTick, 0, "winter lock clears regrow tick");
  2084	        assertEq(east2.regrowUntilTick, 0, "winter lock clears all regrow ticks");
  2085	
  2086	        OrderResult[] memory results =
  2087	            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
  2088	        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
  2089	
  2090	        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  2091	        _advanceToTick(winterEnd - 1);
  2092	        _advanceTick();
  2093	
  2094	        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
  2095	        (west1, east1) = world.getWheatPlots(clanId1);
  2096	        (west2, east2) = world.getWheatPlots(clanId2);
  2097	        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
  2098	        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
  2099	        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
  2100	        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
  2101	        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
  2102	        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
  2103	        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
  2104	        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
  2105	
  2106	        _advanceToTick(expectedRegrowUntil + 1);
  2107	        world.settleClan(clanId1);
  2108	        world.settleClan(clanId2);
  2109	
  2110	        (west1, east1) = world.getWheatPlots(clanId1);
  2111	        (west2, east2) = world.getWheatPlots(clanId2);
  2112	        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
  2113	        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
  2114	        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
  2115	        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
  2116	        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
  2117	        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
  2118	    }
  2119	
  2120	    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
  2121	        uint32 clanId = _mintClan();
  2122	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2123	        _advanceToTick(winterStart - 2);
  2124	
  2125	        OrderResult[] memory results =
  2126	            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
  2127	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
  2128	        Mission memory queuedMission = world.getActiveMission(1);
  2129	
  2130	        _advanceToTick(queuedMission.settlesAtTick + 1);
  2131	
  2132	        Clansman memory cs = world.getClansman(1);
  2133	        Mission memory mission = world.getActiveMission(1);
  2134	        (WheatPlot memory west,) = world.getWheatPlots(clanId);
  2135	        assertFalse(mission.active, "locked harvest mission should complete");
  2136	        assertEq(cs.carryWheat, 0, "locked harvest should yield no wheat");
  2137	        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
  2138	        assertEq(west.remainingWheat, 0, "winter start clears locked plot");
  2139	    }
  2140	
  2141	    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
  2142	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2143	        world = harness;
  2144	        uint32 clanId = _mintClan();
  2145	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2146	        _advanceToTick(winterStart + 1);
  2147	
  2148	        harness.setClanWallLevel(clanId, 2);
  2149	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
  2150	
  2151	        world.settleClan(clanId);
  2152	
  2153	        Clan memory clan = world.getClan(clanId);
  2154	        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
  2155	        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
  2156	        assertEq(clan.vaultWood, 97e18, "winter wood burn should include base and per clansman");
  2157	        assertEq(clan.coldDamage, 0, "sufficient wood should avoid cold damage");
  2158	        assertEq(clan.wallLevel, 2, "sufficient wood should not degrade wall");
  2159	        assertEq(clan.livingClansmen, 4, "sufficient wood should not kill clansmen");
  2160	    }
  2161	
  2162	    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
  2163	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2164	        world = harness;
  2165	        uint32 clanId = _mintClan();
  2166	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2167	        _advanceToTick(winterStart + 1);
  2168	
  2169	        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
  2170	
  2171	        vm.expectEmit(true, false, false, true);
  2172	        emit IClanWorldEvents.ClanColdShortage(clanId, winterStart, 2e18);
  2173	        world.settleClan(clanId);
  2174	
  2175	        Clan memory clan = world.getClan(clanId);
  2176	        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
  2177	        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
  2178	    }
  2179	
  2180	    function test_coldDamage_degradesWallEveryTwoShortages() public {
  2181	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2182	        world = harness;
  2183	        uint32 clanId = _mintClan();
  2184	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2185	        _advanceToTick(winterStart + 1);
  2186	
  2187	        harness.setClanWallLevel(clanId, 2);
  2188	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2189	
  2190	        vm.expectEmit(true, false, false, true);
  2191	        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, winterStart);
  2192	        world.settleClan(clanId);
  2193	
  2194	        Clan memory clan = world.getClan(clanId);
  2195	        assertEq(clan.coldDamage, 2, "cold damage should increment");
  2196	        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
  2197	        assertEq(clan.livingClansmen, 4, "wall should absorb cold before clansmen die");
  2198	    }
  2199	
  2200	    function test_coldDamage_zeroWallKillsOneClansmanEveryTwoShortages() public {
  2201	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2202	        world = harness;
  2203	        uint32 clanId = _mintClan();
  2204	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2205	        _advanceToTick(winterStart + 1);
  2206	
  2207	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2208	
  2209	        vm.recordLogs();
  2210	        world.settleClan(clanId);
  2211	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2212	
  2213	        Clan memory clan = world.getClan(clanId);
  2214	        assertEq(clan.coldDamage, 2, "cold damage should hit death threshold");
  2215	        assertEq(clan.wallLevel, 0, "wall should remain clamped at zero");
  2216	        assertEq(clan.livingClansmen, 3, "one clansman should die from cold");
  2217	        assertEq(_countLogs(logs, keccak256("ClansmanColdDeath(uint32,uint32,uint64)")), 1, "cold death should emit");
  2218	
  2219	        ClanFullView memory view_ = world.getClanFullView(clanId);
  2220	        uint256 deadCount = 0;
  2221	        for (uint256 i = 0; i < view_.clansmen.length; i++) {
  2222	            if (view_.clansmen[i].clansman.clansman.state == ClansmanState.DEAD) {
  2223	                deadCount++;
  2224	            }
  2225	        }
  2226	        assertEq(deadCount, 1, "exactly one stored clansman should be dead");
  2227	    }
  2228	
  2229	    function test_pre_winter_starver_dies_in_winter_at_same_cadence() public {
  2230	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2231	        world = harness;
  2232	        uint32 preWinterStarver = _mintClan();
  2233	        uint32 winterStarver = _mintClan();
  2234	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2235	        _advanceToTick(winterStart + 2);
  2236	
  2237	        harness.setClanUpkeepState(preWinterStarver, winterStart, 100e18, 0, 0, 0);
  2238	        harness.setClanStarvationStartsAtTick(preWinterStarver, winterStart - 5);
  2239	        harness.setClanUpkeepState(winterStarver, winterStart, 100e18, 0, 0, 0);
  2240	
  2241	        world.settleClan(preWinterStarver);
  2242	        world.settleClan(winterStarver);
  2243	
  2244	        Clan memory preWinterClan = world.getClan(preWinterStarver);
  2245	        Clan memory winterClan = world.getClan(winterStarver);
  2246	        assertEq(preWinterClan.livingClansmen, 3, "pre-winter starver should skip first winter tick death");
  2247	        assertEq(winterClan.livingClansmen, 3, "fresh winter starver should match pre-winter cadence");
  2248	    }
  2249	
  2250	    function test_winter_starvationWoodBurnUsesPreDeathLivingCount() public {
  2251	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2252	        world = harness;
  2253	        uint32 clanId = _mintClan();
  2254	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2255	        _advanceToTick(winterStart + 2);
  2256	
  2257	        harness.setClanUpkeepState(clanId, winterStart + 1, 25e17, 0, 0, 0);
  2258	        harness.setClanStarvationStartsAtTick(clanId, winterStart);
  2259	
  2260	        world.settleClan(clanId);
  2261	
  2262	        Clan memory clan = world.getClan(clanId);
  2263	        assertEq(clan.livingClansmen, 3, "starvation should kill one clansman");
  2264	        assertEq(clan.vaultWood, 0, "wood should be consumed after shortage");
  2265	        assertEq(clan.coldDamage, 1, "wood burn should use the pre-starvation living count");
  2266	    }
  2267	
  2268	    function test_winterColdDamageResetIsLazyPerClanAfterPartialSettlement() public {
  2269	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2270	        world = harness;
  2271	        uint32 partiallySettledClan = _mintClan();
  2272	        uint32 neverSettledClan = _mintClan();
  2273	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2274	        uint64 winterEnd = winterStart + ClanWorldConstants.WINTER_DURATION_TICKS;
  2275	        _advanceToTick(winterStart + 3);
  2276	
  2277	        harness.setClanWallLevel(partiallySettledClan, 10);
  2278	        harness.setClanWallLevel(neverSettledClan, 10);
  2279	        harness.setClanUpkeepState(partiallySettledClan, winterStart, 0, 100e18, 100e18, 0);
  2280	        harness.setClanUpkeepState(neverSettledClan, winterStart, 0, 100e18, 100e18, 0);
  2281	
  2282	        world.settleClan(partiallySettledClan);
  2283	        Clan memory partialMidWinter = world.getClan(partiallySettledClan);
  2284	        assertEq(partialMidWinter.wallLevel, 9, "three shortages should have degraded one wall level");
  2285	        assertEq(partialMidWinter.coldDamage, 3, "mid-winter settlement should preserve accrued cold");
  2286	
  2287	        _advanceToTick(winterEnd);
  2288	        world.settleClan(partiallySettledClan);
  2289	        world.settleClan(neverSettledClan);
  2290	
  2291	        Clan memory partialAfterWinter = world.getClan(partiallySettledClan);
  2292	        Clan memory neverAfterWinter = world.getClan(neverSettledClan);
  2293	        assertEq(
  2294	            partialAfterWinter.wallLevel, neverAfterWinter.wallLevel, "partial and lazy settlement should converge"
  2295	        );
  2296	        assertEq(partialAfterWinter.wallLevel, 5, "ten shortages should degrade five wall levels");
  2297	        assertEq(partialAfterWinter.coldDamage, 0, "partial clan cold damage resets after crossing winter end");
  2298	        assertEq(neverAfterWinter.coldDamage, 0, "lazy clan cold damage resets after crossing winter end");
  2299	    }
  2300	
  2301	    function test_winterMintedClanStartsWithLockedEmptyWheatPlots() public {
  2302	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2303	        _advanceToTick(winterStart);
  2304	
  2305	        uint32 clanId = _mintClan();
  2306	        ClanFullView memory view_ = world.getClanFullView(clanId);
  2307	        assertEq(uint8(view_.westPlot.state), uint8(WheatPlotState.WinterLocked), "west plot should start locked");
  2308	        assertEq(uint8(view_.eastPlot.state), uint8(WheatPlotState.WinterLocked), "east plot should start locked");
  2309	        assertEq(view_.westPlot.remainingWheat, 0, "west locked plot should start empty");
  2310	        assertEq(view_.eastPlot.remainingWheat, 0, "east locked plot should start empty");
  2311	    }
  2312	
  2313	    function test_winterCropTransitionCapCoversCurrentClanCap() public {
  2314	        for (uint256 i = 0; i < 12; i++) {
  2315	            _mintClan();
  2316	        }
  2317	        assertGe(world.MAX_CROP_TRANSITION_PER_TICK(), 24, "transition cap must cover 12 clans x 2 plots");
  2318	
  2319	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2320	        _advanceToTick(winterStart);
  2321	        assertEq(world.getWorldState().currentTick, winterStart, "full clan cap should not brick winter heartbeat");
  2322	    }
  2323	
  2324	    function test_starvation_kill_deferred_to_next_tick() public {
  2325	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2326	        world = harness;
  2327	        uint32 clanId = _mintClan();
  2328	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2329	        _advanceToTick(winterStart + 1);
  2330	
  2331	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2332	        world.settleClan(clanId);
  2333	
  2334	        Clan memory clan = world.getClan(clanId);
  2335	        assertEq(clan.starvationStartsAtTick, winterStart, "starvation starts on first short-food tick");
  2336	        assertEq(clan.livingClansmen, 4, "first starvation tick should not kill");
  2337	
  2338	        _advanceTick();
  2339	        world.settleClan(clanId);
  2340	
  2341	        clan = world.getClan(clanId);
  2342	        assertEq(clan.livingClansmen, 3, "first death lands on the next tick");
  2343	    }
  2344	
  2345	    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
  2346	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2347	        world = harness;
  2348	        uint32 clanId = _mintClan();
  2349	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2350	        uint256 goldBalance = 11e18;
  2351	
  2352	        _advanceToTick(winterStart + 5);
  2353	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2354	        harness.setClanIronAndGold(clanId, 5e18, goldBalance);
  2355	
  2356	        vm.recordLogs();
  2357	        world.settleClan(clanId);
  2358	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2359	
  2360	        Clan memory clan = world.getClan(clanId);

exec
/bin/bash -lc "git show 400463e:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '520,700p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   520	        world.settleClan(clanId);
   521	
   522	        // cs0 carry iron should be cleared and vault iron should have increased
   523	        Clansman memory cs0After = world.getClansman(cs0);
   524	        assertEq(cs0After.carryIron, 0, "carry iron should be cleared after deposit");
   525	
   526	        uint256 vaultAfter = world.getClan(clanId).vaultIron;
   527	        assertGt(vaultAfter, vaultBefore, "vault iron should increase after deposit");
   528	
   529	        // cs1 unused — suppress warning
   530	        cs1;
   531	    }
   532	
   533	    function test_quoteTravel_outOfBounds_returnsZero() public {
   534	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
   535	        assertEq(ticks, 0, "out-of-range src should return 0 ticks");
   536	        assertEq(path, bytes8(0), "out-of-range src should return empty path");
   537	
   538	        (ticks, path) = world.quoteTravel(1, 9);
   539	        assertEq(ticks, 0, "out-of-range dst should return 0 ticks");
   540	        assertEq(path, bytes8(0), "out-of-range dst should return empty path");
   541	    }
   542	
   543	    // -------------------------------------------------------------------------
   544	    // Test A: quoteTravel(9,9) — both out-of-bounds same-region, must return (0, bytes8(0))
   545	    // -------------------------------------------------------------------------
   546	
   547	    function test_quoteTravel_outOfBounds_sameRegion() public {
   548	        // Previously (9,9) escaped the > 8 guard (same-region early return fired first),
   549	        // returning (0, bytes8(uint64(9) << 56)) instead of (0, bytes8(0)).
   550	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 9);
   551	        assertEq(ticks, 0, "quoteTravel(9,9): travelTicks must be 0");
   552	        assertEq(path, bytes8(0), "quoteTravel(9,9): path must be bytes8(0), not a packed 9-region sentinel");
   553	    }
   554	
   555	    // -------------------------------------------------------------------------
   556	    // Test B: submitClanOrders returns ERR_MUST_SETTLE_FIRST when clan is >200 ticks behind
   557	    // -------------------------------------------------------------------------
   558	
   559	    function test_submitClanOrders_returnsMustSettleFirst_when_clan_too_far_behind() public {
   560	        uint32 clanId = _mintClan();
   561	        ClanFullView memory view_ = world.getClanFullView(clanId);
   562	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
   563	
   564	        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
   565	        for (uint256 i = 0; i < 201; i++) {
   566	            _advanceTick();
   567	        }
   568	
   569	        // submitClanOrders should return ERR_MUST_SETTLE_FIRST
   570	        // without reverting, for every order in the batch
   571	        ClanOrder[] memory orders = new ClanOrder[](1);
   572	        orders[0] = ClanOrder({
   573	            clansmanId: csId,
   574	            gotoRegion: ClanWorldConstants.REGION_FOREST,
   575	            action: ActionType.ChopWood,
   576	            targetClanId: 0,
   577	            marketToken: address(0),
   578	            marketAmount: 0,
   579	            maxGoldIn: 0
   580	        });
   581	        vm.prank(elder);
   582	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
   583	
   584	        assertEq(results.length, 1, "should return one result");
   585	        assertEq(
   586	            uint8(results[0].status),
   587	            uint8(StatusCode.ERR_MUST_SETTLE_FIRST),
   588	            "clan >200 ticks behind must return ERR_MUST_SETTLE_FIRST"
   589	        );
   590	        assertEq(uint8(StatusCode.ERR_WINTER_LOCKED), 28, "existing status indices must remain stable");
   591	        assertEq(uint8(StatusCode.ERR_MUST_SETTLE_FIRST), 29, "new status code must be appended");
   592	    }
   593	
   594	    // -------------------------------------------------------------------------
   595	    // Test C: cooldown resets on mission interrupt
   596	    // -------------------------------------------------------------------------
   597	
   598	    function test_cooldown_resets_on_mission_interrupt() public {
   599	        uint32 clanId = _mintClan();
   600	        ClanFullView memory view_ = world.getClanFullView(clanId);
   601	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
   602	
   603	        // Submit first mission — sends clansman to Forest to chop wood
   604	        OrderResult[] memory r1 = _submitOrder(clanId, csId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood);
   605	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
   606	        uint64 firstCooldown = r1[0].cooldownEndsAtTs;
   607	        assertGt(firstCooldown, 0, "cooldown should be set after first order");
   608	
   609	        // Wait for cooldown to expire
   610	        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
   611	
   612	        // Advance tick so heartbeat is valid, then submit interrupt mission
   613	        _advanceTick();
   614	
   615	        // Submit a new mission to interrupt the first (still en-route to Forest)
   616	        // Use MineIron in Mountains — different target, forces interrupt
   617	        OrderResult[] memory r2 = _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);
   618	        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "interrupt order should succeed");
   619	
   620	        uint64 newCooldown = r2[0].cooldownEndsAtTs;
   621	        assertGt(newCooldown, firstCooldown, "new cooldown must be later than first cooldown");
   622	        assertGt(newCooldown, block.timestamp, "new cooldown must be in the future");
   623	    }
   624	
   625	    // =========================================================================
   626	    // Phase 2 Market Tests
   627	    // =========================================================================
   628	
   629	    // Helper: submit a market order for a specific clansman
   630	    function _submitMarketOrder(
   631	        uint32 clanId,
   632	        uint32 csId,
   633	        ActionType action,
   634	        address token,
   635	        uint256 amount,
   636	        uint256 maxGold
   637	    ) internal returns (OrderResult[] memory) {
   638	        ClanOrder[] memory orders = new ClanOrder[](1);
   639	        orders[0] = ClanOrder({
   640	            clansmanId: csId,
   641	            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
   642	            action: action,
   643	            targetClanId: 0,
   644	            marketToken: token,
   645	            marketAmount: amount,
   646	            maxGoldIn: maxGold
   647	        });
   648	        vm.prank(elder);
   649	        return world.submitClanOrders(clanId, orders);
   650	    }
   651	
   652	    function _submitAllClanMarketSells(uint32 clanId, address token) internal returns (uint256 count) {
   653	        ClanFullView memory view_ = world.getClanFullView(clanId);
   654	        count = view_.clansmen.length;
   655	        return _submitFirstClanMarketSells(clanId, token, count);
   656	    }
   657	
   658	    function _submitFirstClanMarketSells(uint32 clanId, address token, uint256 count) internal returns (uint256) {
   659	        ClanFullView memory view_ = world.getClanFullView(clanId);
   660	        require(count <= view_.clansmen.length, "too many clansmen");
   661	        ClanOrder[] memory orders = new ClanOrder[](count);
   662	        for (uint256 i = 0; i < count; i++) {
   663	            orders[i] = ClanOrder({
   664	                clansmanId: view_.clansmen[i].clansman.clansman.clansmanId,
   665	                gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
   666	                action: ActionType.MarketSell,
   667	                targetClanId: 0,
   668	                marketToken: token,
   669	                marketAmount: 1e18,
   670	                maxGoldIn: 0
   671	            });
   672	        }
   673	        vm.prank(elder);
   674	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
   675	        for (uint256 i = 0; i < results.length; i++) {
   676	            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "market sell should enqueue");
   677	        }
   678	        return count;
   679	    }
   680	
   681	    // Helper: get the first clansman id for a clan
   682	    function _firstCs(uint32 clanId) internal view returns (uint32) {
   683	        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
   684	    }
   685	
   686	    // -------------------------------------------------------------------------
   687	    // Test 11: sell_creditsGold — after scheduled sell, clan.goldBalance > starter gold
   688	    // -------------------------------------------------------------------------
   689	
   690	    function test_sell_creditsGold() public {
   691	        address woodAddr = _setupMarket();
   692	        uint32 clanId = _mintClan();
   693	        uint32 csId = _firstCs(clanId);
   694	
   695	        // Clan starts with 20 wood in vault (starter pack)
   696	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   697	
   698	        // Submit sell order — clansman travels to Unicorn Town then executes at actionStartTick
   699	        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
   700	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "sell order should be accepted");

exec
/bin/bash -lc "git show 400463e:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '2360,2660p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2360	        Clan memory clan = world.getClan(clanId);
  2361	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
  2362	        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
  2363	        assertEq(clan.vaultWood, 0, "wood should burn on death");
  2364	        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
  2365	        assertEq(clan.vaultFish, 0, "fish should burn on death");
  2366	        assertEq(clan.vaultIron, 0, "iron should burn on death");
  2367	        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
  2368	        _assertClanDiedLog(logs, clanId, winterStart + 4, "starvation");
  2369	    }
  2370	
  2371	    function test_clanDeath_coldDamageMarksDeadAndBurnsVault() public {
  2372	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2373	        world = harness;
  2374	        uint32 clanId = _mintClan();
  2375	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2376	        uint256 goldBalance = 13e18;
  2377	        _advanceToTick(winterStart + 7);
  2378	
  2379	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2380	        harness.setClanIronAndGold(clanId, 9e18, goldBalance);
  2381	
  2382	        vm.recordLogs();
  2383	        world.settleClan(clanId);
  2384	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2385	
  2386	        Clan memory clan = world.getClan(clanId);
  2387	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "cold should mark clan dead");
  2388	        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
  2389	        assertEq(clan.vaultWood, 0, "wood should burn on death");
  2390	        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
  2391	        assertEq(clan.vaultFish, 0, "fish should burn on death");
  2392	        assertEq(clan.vaultIron, 0, "iron should burn on death");
  2393	        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
  2394	        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
  2395	    }
  2396	
  2397	    function test_deadClanCannotSubmitClanOrders() public {
  2398	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2399	        world = harness;
  2400	        uint32 clanId = _mintClan();
  2401	
  2402	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2403	        _advanceToTick(winterStart + 5);
  2404	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2405	        world.settleClan(clanId);
  2406	
  2407	        vm.expectRevert("ClanWorld: clan dead");
  2408	        _submitOrder(clanId, 1, ClanWorldConstants.REGION_FOREST, ActionType.Wait);
  2409	    }
  2410	
  2411	    function test_clanDeath_doesNotAffectOtherClan() public {
  2412	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2413	        world = harness;
  2414	        uint32 clanIdA = _mintClan();
  2415	        uint32 clanIdB = _mintClan();
  2416	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2417	        Clan memory clanABefore = world.getClan(clanIdA);
  2418	
  2419	        _advanceToTick(winterStart + 5);
  2420	        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
  2421	        world.settleClan(clanIdB);
  2422	
  2423	        Clan memory clanAAfter = world.getClan(clanIdA);
  2424	        Clan memory clanB = world.getClan(clanIdB);
  2425	        assertEq(uint8(clanB.clanState), uint8(ClanState.DEAD), "clan B should be dead");
  2426	        assertEq(uint8(clanAAfter.clanState), uint8(ClanState.ACTIVE), "clan A should stay active");
  2427	        assertEq(clanAAfter.livingClansmen, clanABefore.livingClansmen, "clan A living count should not change");
  2428	        assertEq(clanAAfter.vaultWood, clanABefore.vaultWood, "clan A wood should not burn");
  2429	        assertEq(clanAAfter.vaultWheat, clanABefore.vaultWheat, "clan A wheat should not burn");
  2430	        assertEq(clanAAfter.vaultFish, clanABefore.vaultFish, "clan A fish should not burn");
  2431	        assertEq(clanAAfter.goldBalance, clanABefore.goldBalance, "clan A gold should not change");
  2432	    }
  2433	
  2434	    function test_winter_upkeep_returnsToNormalAfterWinter() public {
  2435	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2436	        world = harness;
  2437	        uint32 clanId = _mintClan();
  2438	        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  2439	        _advanceToTick(winterEnd + 1);
  2440	
  2441	        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
  2442	
  2443	        world.settleClan(clanId);
  2444	
  2445	        Clan memory clan = world.getClan(clanId);
  2446	        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
  2447	        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
  2448	        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
  2449	        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
  2450	    }
  2451	
  2452	    function test_season_transition() public {
  2453	        // Advance SEASON_DURATION_TICKS heartbeats to cross season boundary
  2454	        for (uint256 i = 0; i < ClanWorldConstants.SEASON_DURATION_TICKS; i++) {
  2455	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2456	            world.heartbeat();
  2457	        }
  2458	        WorldState memory ws = world.getWorldState();
  2459	        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
  2460	        assertEq(ws.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS);
  2461	        assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2);
  2462	        // winter is derived from the global recurring schedule
  2463	        uint64 expectedWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS * 3;
  2464	        assertEq(ws.winterStartsAtTick, expectedWinterStart);
  2465	    }
  2466	
  2467	    function test_nextHeartbeatAtTick_tracks_tick() public {
  2468	        WorldState memory ws0 = world.getWorldState();
  2469	        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
  2470	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2471	        world.heartbeat();
  2472	        WorldState memory ws1 = world.getWorldState();
  2473	        assertEq(ws1.currentTick, 1);
  2474	        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
  2475	    }
  2476	}

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n \"_world\\.winter\"" in /home/claude/code/clan-world
 succeeded in 0ms:
96:        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
97:        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
98:        _world.winterActive = false;

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n \"_markClanDead\\(\"" in /home/claude/code/clan-world
 succeeded in 0ms:
536:                _markClanDead(clan.clanId, "starvation", tick);
547:            _markClanDead(clan.clanId, "cold", tick);
568:    function _markClanDead(uint32 clanId) internal {
569:        _markClanDead(clanId, "unknown", _world.currentTick);
572:    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {

web search: 
web search: 
exec
/bin/bash -lc 'git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n "ClansmanDiedFromCold|ColdDamageApplied|ClanDied|ClanEliminated|ClansmanColdDeath|WallDegradedByCold"' in /home/claude/code/clan-world
 succeeded in 0ms:
481:            emit WallDegradedByCold(clan.clanId, clan.wallLevel, tick);
545:        emit ClansmanColdDeath(clan.clanId, cs.clansmanId, tick);
596:        emit ClanEliminated(clanId, tick);
597:        emit ClanDied(clanId, tick, reason);

exec
/bin/bash -lc "git show 400463e:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '220,460p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   220	          { name: 'marketToken', type: 'address' },
   221	          { name: 'marketAmount', type: 'uint256' },
   222	          { name: 'maxGoldIn', type: 'uint256' },
   223	        ],
   224	      },
   225	    ],
   226	    outputs: [
   227	      {
   228	        name: 'results',
   229	        type: 'tuple[]',
   230	        components: [
   231	          { name: 'clansmanId', type: 'uint32' },
   232	          { name: 'status', type: 'uint8' },
   233	          { name: 'cooldownEndsAtTs', type: 'uint64' },
   234	          { name: 'missionNonce', type: 'uint64' },
   235	        ],
   236	      },
   237	    ],
   238	    stateMutability: 'nonpayable',
   239	  },
   240	] as const;
   241	
   242	class StubChainClient implements IChainClient {
   243	  async getCurrentTick(): Promise<Tick> {
   244	    return 0;
   245	  }
   246	  async submitOrders(_clanId: string, _orders: ClanOrder[]): Promise<{ txHash: string }> {
   247	    return { txHash: '0xstub' };
   248	  }
   249	  async getClanFullView(clanId: string): Promise<ClanFullView> {
   250	    return {
   251	      clan: { id: clanId, name: `clan-${clanId}`, treasury: '0' },
   252	      controlledRegions: [],
   253	      pendingOrders: [],
   254	      whispers: [],
   255	    };
   256	  }
   257	}
   258	
   259	class RealChainClient implements IChainClient {
   260	  private readonly client: ReturnType<typeof createPublicClient>;
   261	  private readonly contractAddress: `0x${string}`;
   262	  private readonly transport: ReturnType<typeof http> | ReturnType<typeof fallback>;
   263	
   264	  constructor() {
   265	    const primaryRpc = readEnv('RPC_URL_PRIMARY');
   266	    const fallbackRpc = readEnv('RPC_URL_FALLBACK');
   267	
   268	    this.transport =
   269	      primaryRpc && fallbackRpc
   270	        ? fallback([http(primaryRpc), http(fallbackRpc)])
   271	        : http(primaryRpc ?? fallbackRpc);
   272	
   273	    this.contractAddress = (readEnv('CLAN_WORLD_CONTRACT_ADDRESS') ??
   274	      DEFAULT_CONTRACT_ADDRESS) as `0x${string}`;
   275	
   276	    this.client = createPublicClient({
   277	      chain: baseSepolia,
   278	      transport: this.transport,
   279	    });
   280	  }
   281	
   282	  async getCurrentTick(): Promise<Tick> {
   283	    const snapshot = await this.client.readContract({
   284	      address: this.contractAddress,
   285	      abi: CLAN_WORLD_ABI,
   286	      functionName: 'getWorldSnapshot',
   287	    });
   288	    return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
   289	  }
   290	
   291	  async submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }> {
   292	    // Wave 0: single-Elder only — concurrent nonce coordination deferred to Wave 1
   293	    const parsedClanId = parseInt(clanId, 10);
   294	    if (isNaN(parsedClanId) || String(parsedClanId) !== clanId.trim()) {
   295	      throw new Error(`submitOrders: clanId must be a decimal integer, got '${clanId}'`);
   296	    }
   297	
   298	    for (const order of orders) {
   299	      if (order.kind === 'mission') {
   300	        const { clansmanId, gotoRegion, action } = order.payload;
   301	        if (clansmanId === undefined || gotoRegion === undefined || action === undefined) {
   302	          throw new Error(`submitOrders: mission order missing required payload fields (clansmanId, gotoRegion, action)`);
   303	        }
   304	      }
   305	    }
   306	
   307	    const nonMissionOrders = orders.filter(o => o.kind !== 'mission');
   308	    if (nonMissionOrders.length > 0) {
   309	      console.warn(`[RealChainClient] submitOrders: ${nonMissionOrders.length} non-mission order(s) skipped (Wave 0 only supports 'mission' kind)`);
   310	    }
   311	
   312	    const contractOrders = orders
   313	      .filter(o => o.kind === 'mission')
   314	      .map(o => ({
   315	        clansmanId: Number(o.payload.clansmanId),
   316	        gotoRegion: Number(o.payload.gotoRegion),
   317	        action: Number(o.payload.action),
   318	        targetClanId: 0,
   319	        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
   320	        marketAmount: 0n,
   321	        maxGoldIn: 0n,
   322	      }));
   323	
   324	    if (contractOrders.length === 0) {
   325	      throw new Error('submitOrders: no valid mission orders to submit');
   326	    }
   327	
   328	    const keyPath = readEnv('ELDER_WALLET_KEY_PATH');
   329	    let pk: string | undefined;
   330	    let pkSource: string | undefined;
   331	    if (keyPath) {
   332	      try {
   333	        pk = fs.readFileSync(keyPath, 'utf8').trim();
   334	        pkSource = `ELDER_WALLET_KEY_PATH file at ${keyPath}`;
   335	      } catch (err) {
   336	        if (err && typeof err === 'object' && 'code' in err && err.code === 'ENOENT') {
   337	          throw new Error(
   338	            `ELDER_WALLET_KEY_PATH file not found at ${keyPath}; either set DEPLOYER_PRIVATE_KEY env var or provide a key file`,
   339	          );
   340	        }
   341	        const msg = err instanceof Error ? err.message : String(err);
   342	        throw new Error(`Failed to read ELDER_WALLET_KEY_PATH file at ${keyPath}: ${msg}`);
   343	      }
   344	    } else {
   345	      const fallbackKey = readEnv('DEPLOYER_PRIVATE_KEY');
   346	      if (fallbackKey) {
   347	        console.warn('[RealChainClient] ELDER_WALLET_KEY_PATH not set; falling back to DEPLOYER_PRIVATE_KEY (deprecated)');
   348	        pk = fallbackKey;
   349	        pkSource = 'DEPLOYER_PRIVATE_KEY env var';
   350	      }
   351	    }
   352	    if (!pk) throw new Error('Neither ELDER_WALLET_KEY_PATH nor DEPLOYER_PRIVATE_KEY is set');
   353	
   354	    // Normalize: add 0x prefix if missing
   355	    if (!pk.startsWith('0x')) pk = '0x' + pk;
   356	    if (!/^0x[0-9a-fA-F]{64}$/.test(pk)) {
   357	      throw new Error(
   358	        `Invalid private key from ${pkSource ?? 'unknown source'}: expected a 64-hex-char private key (0x-prefixed optional)`,
   359	      );
   360	    }
   361	
   362	    const account = privateKeyToAccount(pk as `0x${string}`);
   363	    const walletClient = createWalletClient({
   364	      account,
   365	      chain: baseSepolia,
   366	      transport: this.transport,
   367	    });
   368	
   369	    const hash = await walletClient.writeContract({
   370	      address: this.contractAddress,
   371	      abi: CLAN_WORLD_ABI,
   372	      functionName: 'submitClanOrders',
   373	      args: [parsedClanId, contractOrders],
   374	    });
   375	
   376	    return { txHash: hash };
   377	  }
   378	
   379	  async getClanFullView(clanId: string): Promise<ClanFullView> {
   380	    const result = await this.client.readContract({
   381	      address: this.contractAddress,
   382	      abi: CLAN_WORLD_ABI,
   383	      functionName: 'getClanFullView',
   384	      args: [parseInt(clanId, 10)],
   385	    });
   386	
   387	    const inner = result.clan.clan;
   388	    return {
   389	      clan: {
   390	        id: String(inner.clanId),
   391	        name: `clan-${inner.clanId}`,
   392	        treasury: String(inner.goldBalance),
   393	      },
   394	      // controlledRegions, pendingOrders, whispers not available in Wave 0 contract read
   395	      controlledRegions: [], // Wave 0: omit base region from controlledRegions; populated in Wave 1 from on-chain data
   396	      pendingOrders: [],
   397	      whispers: [],
   398	    };
   399	  }
   400	}
   401	
   402	export function createChainClient(): IChainClient {
   403	  return readEnv('CLAN_WORLD_USE_STUB_CHAIN') === 'true'
   404	    ? new StubChainClient()
   405	    : new RealChainClient();
   406	}

web search: 
web search: 
exec
/bin/bash -lc 'rg -n "getWorldState|seasonStartTick|nextHeartbeatAtTs" packages -g"*.ts"' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/runner/src/runnerCastHeartbeat.ts:18: * Minimal ABI: only the `heartbeat()` write and the `nextHeartbeatAtTs` field
packages/runner/src/runnerCastHeartbeat.ts:19: * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
packages/runner/src/runnerCastHeartbeat.ts:32:    name: 'getWorldState',
packages/runner/src/runnerCastHeartbeat.ts:40:          { name: 'seasonStartTick', type: 'uint64' },
packages/runner/src/runnerCastHeartbeat.ts:43:          { name: 'nextHeartbeatAtTs', type: 'uint64' },
packages/runner/src/runnerCastHeartbeat.ts:98: * `nextHeartbeatAtTs`. We don't have a typed custom error in the ABI, so on
packages/runner/src/runnerCastHeartbeat.ts:99: * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
packages/runner/src/runnerCastHeartbeat.ts:168:      functionName: 'getWorldState',
packages/runner/src/runnerCastHeartbeat.ts:172:    return Number((state as { nextHeartbeatAtTs: bigint }).nextHeartbeatAtTs);
packages/shared/src/adapters/IChainClient.ts:36:          { name: 'seasonStartTick', type: 'uint64' },
packages/agents/src/seams/IHeartbeatCaller.ts:34:/** Thrown when heartbeat() is called before nextHeartbeatAtTs has elapsed. */

exec
/bin/bash -lc 'rg -n "currentSeasonNumber|nextHeartbeatAtTick" packages -g"*.ts"' in /home/claude/code/clan-world
 exited 1 in 0ms:
web search: 
web search: 
web search: 
web search: 
web search: 
web search: 
exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '990,1105p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   990	            wheatCost = 20e18;
   991	        } else if (nextLevel == 2) {
   992	            woodCost = 50e18;
   993	            wheatCost = 30e18;
   994	        } else if (nextLevel == 3) {
   995	            woodCost = 70e18;
   996	            wheatCost = 40e18;
   997	            ironCost = 5e18;
   998	        } else if (nextLevel == 4) {
   999	            woodCost = 90e18;
  1000	            wheatCost = 50e18;
  1001	            ironCost = 10e18;
  1002	        } else if (nextLevel == 5) {
  1003	            woodCost = 120e18;
  1004	            wheatCost = 60e18;
  1005	            ironCost = 15e18;
  1006	        } else if (nextLevel == 6) {
  1007	            woodCost = 150e18;
  1008	            wheatCost = 80e18;
  1009	            ironCost = 20e18;
  1010	        } else if (nextLevel <= 10) {
  1011	            woodCost = 200e18;
  1012	            wheatCost = 100e18;
  1013	            ironCost = 25e18;
  1014	            blueprintCost = 1e18;
  1015	        }
  1016	
  1017	        if (
  1018	            clan.vaultWood < woodCost || clan.vaultWheat < wheatCost || clan.vaultIron < ironCost
  1019	                || clan.blueprintBalance < blueprintCost
  1020	        ) return false;
  1021	
  1022	        clan.vaultWood -= woodCost;
  1023	        clan.vaultWheat -= wheatCost;
  1024	        clan.vaultIron -= ironCost;
  1025	        clan.blueprintBalance -= blueprintCost;
  1026	
  1027	        uint8 old = clan.monumentLevel;
  1028	        clan.monumentLevel = nextLevel;
  1029	        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
  1030	        return true;
  1031	    }
  1032	
  1033	    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
  1034	    function _completeMission(Clansman storage cs, Mission storage m) internal {
  1035	        cs.state = ClansmanState.WAITING;
  1036	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1037	        m.active = false;
  1038	        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
  1039	    }
  1040	
  1041	    // =========================================================================
  1042	    // WORLD PROGRESSION
  1043	    // =========================================================================
  1044	
  1045	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
  1046	    ///         Execution order per spec §4.2 (CEI-safe):
  1047	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
  1048	    ///         Seed:      closedTick seed derived and published before step 1 so
  1049	    ///                    settlement RNG reads real entropy, not zero.
  1050	    ///         1. Settle missions completing this tick.
  1051	    ///         2. Execute scheduled market actions for closedTick (external calls).
  1052	    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
  1053	    ///         4. Resolve world events (season boundary, winter transitions).
  1054	    ///         5. Increment tick and publish (seed already written above).
  1055	    function heartbeat() external override nonReentrant {
  1056	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
  1057	
  1058	        uint64 closedTick = _world.currentTick;
  1059	
  1060	        // CEI: update rate-limit guard before any external calls
  1061	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
  1062	
  1063	        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
  1064	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
  1065	        _tickSeeds[closedTick] = newSeed;
  1066	        _world.currentTickSeed = newSeed;
  1067	
  1068	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
  1069	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  1070	        _settleCompletingMissions(closedTick);
  1071	
  1072	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
  1073	        _executeScheduledMarketActions(closedTick);
  1074	
  1075	        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
  1076	        // TODO Phase 3: _settleClansNearBandit(closedTick);
  1077	
  1078	        // Step 4: Resolve world events (season boundary, winter transitions).
  1079	        _resolveWorldEvents(closedTick);
  1080	
  1081	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
  1082	        uint64 newTick = closedTick + 1;
  1083	        _world.currentTick = newTick;
  1084	        _world.nextHeartbeatAtTick = newTick + 1;
  1085	
  1086	        emit TickAdvanced(closedTick, newTick, newSeed);
  1087	    }
  1088	
  1089	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
  1090	    ///      Called from heartbeat before market execution and tick increment.
  1091	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  1092	    function _settleCompletingMissions(uint64 tick) internal {
  1093	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1094	            uint32 clanId = _allClanIds[i];
  1095	            Clan storage clan = _clans[clanId];
  1096	            if (clan.clanState == ClanState.DEAD) continue;
  1097	
  1098	            uint32[] storage csIds = _clanClansmanIds[clanId];
  1099	            for (uint256 j = 0; j < csIds.length; j++) {
  1100	                Clansman storage cs = _clansmen[csIds[j]];
  1101	                if (cs.state == ClansmanState.DEAD) continue;
  1102	
  1103	                Mission storage m = _missions[cs.clansmanId];
  1104	                if (!m.active) continue;
  1105	                if (m.settlesAtTick != tick) continue; // not due this tick

exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | rg -n '\"name\": \"settleClansman\"|\"name\": \"isWinter\"|\"name\": \"getWorldState\"'" in /home/claude/code/clan-world
 succeeded in 0ms:
2021:      "name": "getWorldState",
2136:      "name": "isWinter",
2308:      "name": "settleClansman",

collab: Wait
collab: Wait
collab: Wait
web search: 
web search: 
web search: 
web search: viem decode tuple fewer components than returned data
exec
/bin/bash -lc 'git show 400463e''^:packages/contracts/src/IClanWorld.sol | rg -n "ClanColdShortage|WallDegradedByCold|ClansmanColdDeath|ClansmanDiedFromCold|ColdDamageApplied|ClanDied"' in /home/claude/code/clan-world
 succeeded in 0ms:
508:    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
510:    event ClanColdShortage(uint32 indexed clanId, uint32 tick, uint256 woodShort);
511:    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint32 tick);
512:    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint32 tick);
623:    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
624:    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);

web search: 
web search: x
codex
I have enough validated evidence from all three subagents and direct code tracing; I’m consolidating into the final phase review now with severity ordering, concrete file:line references, and merge recommendation.
collab: CloseAgent
2026-04-30T01:45:37.343523Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-e0aa-7bd2-8a92-ff446b5ee496 not found
collab: CloseAgent
2026-04-30T01:45:37.349494Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-e10f-76e1-bdbb-b8e85a5054d5 not found
collab: CloseAgent
2026-04-30T01:45:37.353008Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-e155-74d3-af7a-43eba1bef974 not found
codex
# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY
**NEEDS_FIXES.** The phase is close, but there are two high-risk integration bugs: cold-death RNG is settlement-time dependent (griefable/non-deterministic), and the shared TS ABI for `getClanFullView` is stale vs on-chain `Mission` layout (decode corruption risk). There are also medium integration risks around event signature drift and lazy-elimination semantics not being enforced at heartbeat level. I recommend **do not merge** until the high-severity items are fixed.

## HIGH severity findings
1. Cold-death RNG uses current global seed instead of per-settled-tick seed.  
Refs: [ClanWorld.sol:505](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:505), [ClanWorld.sol:506](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:506), [ClanWorld.sol:372](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:372), [ClanWorld.sol:403](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:403), [ClanWorld.sol:65](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:65)  
`_killRandomClansmanFromCold` is called while lazily settling historical ticks, but randomness is derived from `_world.currentTickSeed` (latest), not the seed for `tick` being settled. Outcome depends on *when* settlement is called, and `settleClan` is permissionless, so third parties can influence who dies.  
Suggested fix: thread tick seed from settlement path (`_tickSeeds[tick]`) through upkeep/cold-damage functions and use that for cold-death selection.

2. Shared adapter ABI for `getClanFullView` is out of sync with on-chain `Mission` tuple.  
Refs: [IChainClient.ts:136](/home/claude/code/clan-world/packages/shared/src/adapters/IChainClient.ts:136), [IChainClient.ts:161](/home/claude/code/clan-world/packages/shared/src/adapters/IChainClient.ts:161), [IChainClient.ts:380](/home/claude/code/clan-world/packages/shared/src/adapters/IChainClient.ts:380), [IClanWorld.sol:285](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:285), [IClanWorld.sol:286](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:286), [IClanWorld.sol:287](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:287), [ClanWorld.sol:2269](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:2269), [ClanWorld.sol:2271](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:2271)  
The handwritten TS ABI omits `submittedAtTick/executesAtTick/settlesAtTick` in `Mission` components while contract returns full `Mission` in nested views. This is a decode-shape mismatch with runtime breakage/corruption risk.  
Suggested fix: stop hand-maintaining this ABI fragment; generate/import ABI from contracts package (or fully align tuple components immediately).

## MEDIUM severity findings
1. Event signature drift risk for indexers/listeners (`uint32 tick` -> `uint64 tick`).  
Refs: [IClanWorld.sol:511](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:511), [IClanWorld.sol:512](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:512), [IClanWorld.sol:513](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:513)  
Changing event arg types changes `topic0`; existing consumers filtering old signatures will silently miss events unless migrated in lockstep.  
Suggested fix: coordinate indexer migration explicitly (or emit compatibility events during transition).

2. Elimination/upkeep is still lazy-settlement-bound, not heartbeat-global.  
Refs: [ClanWorld.sol:1068](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1068), [ClanWorld.sol:1073](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1073), [ClanWorld.sol:372](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:372)  
Heartbeat settles due missions and market actions, but does not apply per-clan upkeep globally. Winter deaths can lag until an explicit clan settlement path runs, which weakens “tick-authoritative elimination” semantics.  
Suggested fix: either eager-settle affected clans in heartbeat during winter windows, or document and enforce this as an explicit game invariant with compensating logic.

3. Coverage gap: settle-first guard validated only with single-order batch.  
Refs: [ClanWorld.t.sol:559](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:559)  
Guard behavior is intended per-order across batch submissions, but test exercises one order only.  
Suggested fix: add multi-order batch assertions to prevent partial/default result regressions.

## LOW severity findings
1. New cold-death event tests only count signature occurrence, not payload correctness.  
Refs: [ClanWorld.t.sol:2217](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:2217)  
A wrong tick/csId payload could pass current test shape.  
Suggested fix: decode log data and assert tick/csId values, similar to `_assertClanDiedLog`.

## Cross-cutting observations
Winter timing model (`start=110`, periodic windows, derived world-state view) is internally consistent across contract, stub, ABI, and runner/shared tuple updates. The main blockers are deterministic correctness at settlement seams and ABI consumer safety.
2026-04-30T01:46:09.140507Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-81aa-7901-a19c-4e5d3a9cd0c1 not found
tokens used
241,243
# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY
**NEEDS_FIXES.** The phase is close, but there are two high-risk integration bugs: cold-death RNG is settlement-time dependent (griefable/non-deterministic), and the shared TS ABI for `getClanFullView` is stale vs on-chain `Mission` layout (decode corruption risk). There are also medium integration risks around event signature drift and lazy-elimination semantics not being enforced at heartbeat level. I recommend **do not merge** until the high-severity items are fixed.

## HIGH severity findings
1. Cold-death RNG uses current global seed instead of per-settled-tick seed.  
Refs: [ClanWorld.sol:505](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:505), [ClanWorld.sol:506](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:506), [ClanWorld.sol:372](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:372), [ClanWorld.sol:403](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:403), [ClanWorld.sol:65](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:65)  
`_killRandomClansmanFromCold` is called while lazily settling historical ticks, but randomness is derived from `_world.currentTickSeed` (latest), not the seed for `tick` being settled. Outcome depends on *when* settlement is called, and `settleClan` is permissionless, so third parties can influence who dies.  
Suggested fix: thread tick seed from settlement path (`_tickSeeds[tick]`) through upkeep/cold-damage functions and use that for cold-death selection.

2. Shared adapter ABI for `getClanFullView` is out of sync with on-chain `Mission` tuple.  
Refs: [IChainClient.ts:136](/home/claude/code/clan-world/packages/shared/src/adapters/IChainClient.ts:136), [IChainClient.ts:161](/home/claude/code/clan-world/packages/shared/src/adapters/IChainClient.ts:161), [IChainClient.ts:380](/home/claude/code/clan-world/packages/shared/src/adapters/IChainClient.ts:380), [IClanWorld.sol:285](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:285), [IClanWorld.sol:286](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:286), [IClanWorld.sol:287](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:287), [ClanWorld.sol:2269](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:2269), [ClanWorld.sol:2271](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:2271)  
The handwritten TS ABI omits `submittedAtTick/executesAtTick/settlesAtTick` in `Mission` components while contract returns full `Mission` in nested views. This is a decode-shape mismatch with runtime breakage/corruption risk.  
Suggested fix: stop hand-maintaining this ABI fragment; generate/import ABI from contracts package (or fully align tuple components immediately).

## MEDIUM severity findings
1. Event signature drift risk for indexers/listeners (`uint32 tick` -> `uint64 tick`).  
Refs: [IClanWorld.sol:511](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:511), [IClanWorld.sol:512](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:512), [IClanWorld.sol:513](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:513)  
Changing event arg types changes `topic0`; existing consumers filtering old signatures will silently miss events unless migrated in lockstep.  
Suggested fix: coordinate indexer migration explicitly (or emit compatibility events during transition).

2. Elimination/upkeep is still lazy-settlement-bound, not heartbeat-global.  
Refs: [ClanWorld.sol:1068](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1068), [ClanWorld.sol:1073](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1073), [ClanWorld.sol:372](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:372)  
Heartbeat settles due missions and market actions, but does not apply per-clan upkeep globally. Winter deaths can lag until an explicit clan settlement path runs, which weakens “tick-authoritative elimination” semantics.  
Suggested fix: either eager-settle affected clans in heartbeat during winter windows, or document and enforce this as an explicit game invariant with compensating logic.

3. Coverage gap: settle-first guard validated only with single-order batch.  
Refs: [ClanWorld.t.sol:559](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:559)  
Guard behavior is intended per-order across batch submissions, but test exercises one order only.  
Suggested fix: add multi-order batch assertions to prevent partial/default result regressions.

## LOW severity findings
1. New cold-death event tests only count signature occurrence, not payload correctness.  
Refs: [ClanWorld.t.sol:2217](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:2217)  
A wrong tick/csId payload could pass current test shape.  
Suggested fix: decode log data and assert tick/csId values, similar to `_assertClanDiedLog`.

## Cross-cutting observations
Winter timing model (`start=110`, periodic windows, derived world-state view) is internally consistent across contract, stub, ABI, and runner/shared tuple updates. The main blockers are deterministic correctness at settlement seams and ABI consumer safety.
EXIT=0
