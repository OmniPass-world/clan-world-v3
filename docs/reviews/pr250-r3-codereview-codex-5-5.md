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
session id: 019ddc0a-92c8-7603-89bb-83421831e3ff
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
I’ll treat this as a final-review pass and use the repo as ground truth around the diff, especially the settlement/winter state paths and ABI consumers. I’m also going to split a couple of focused checks to subagents so the integration seams get more eyes.
2026-04-30T01:39:43.335554Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
codex
While those two reviewers dig, I’m reading the changed surfaces locally: contract state transitions first, then the TypeScript ABI consumers. The main thing I want to verify is whether “derived winter state” and “stored world state” stay consistent across heartbeat, settlement, and views.
exec
/bin/bash -lc "sed -n '1,260p' packages/contracts/AGENTS.md" in /home/claude/code/clan-world
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
    bool seasonFinalized;
    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)

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

struct Clansman {
    uint32 clansmanId;

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
    mapping(uint64 => bytes32) private _tickSeeds; // tick => seed

    uint32 private _nextClanId;
    uint32 private _nextClansmanId;
    uint32[] private _allClanIds;

    // per-clan clansman list: clanId => clansmanId[]
    mapping(uint32 => uint32[]) private _clanClansmanIds;

    // =========================================================================
    // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
    // =========================================================================

    uint256 private constant WHEAT_HARVEST_RATE = 20e18;
    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
    uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;

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
        _world.winterStartsAtTick =
            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
        _world.winterActive = false;
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
        if (src == dst) {
            return bytes8(uint64(src) << 56);
        }
        // Adjacency list (1-indexed, 0 = end sentinel)
        // adj[r] = neighbors of region r (up to 3)
        uint8[3][9] memory adj = [
            [0, 0, 0], // 0: unused
            [2, 4, 0], // 1: Forest
            [1, 3, 0], // 2: Mountains
            [2, 4, 5], // 3: UnicornTown
            [1, 3, 6], // 4: WestFarms
            [3, 7, 0], // 5: EastFarms
            [4, 8, 0], // 6: WestDocks
            [5, 8, 0], // 7: EastDocks
            [6, 7, 0] // 8: DeepSea
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


exec
/bin/bash -lc "sed -n '260,620p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

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

    function _addTicksClamped(uint64 tick, uint64 delta) private pure returns (uint64) {
        if (type(uint64).max - tick < delta) return type(uint64).max;
        return tick + delta;
    }

    // =========================================================================
    // INTERNAL SETTLEMENT
    // =========================================================================

    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
    function _settleMissionForClansman(
        Clan storage clan,
        Clansman storage cs,
        uint32 clanId,
        uint64 fromTick,
        uint64 toTick
    ) internal {
        Mission storage m = _missions[cs.clansmanId];

        // Path 6: dead clansman — invalidate active mission if any
        if (cs.state == ClansmanState.DEAD) {
            if (m.active) {
                if (m.action == ActionType.DefendBase) {
                    _clearDefender(cs.clansmanId);
                }
                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
            }
            return;
        }

        if (!m.active) return; // no active mission — nothing to settle

        bytes32 tickSeed;
        for (uint64 tick = fromTick; tick < toTick; tick++) {
            tickSeed = _tickSeeds[tick];

            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
                cs.state = ClansmanState.ACTING;
                cs.currentRegion = m.targetRegion;
                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);

                if (m.action == ActionType.DefendBase) {
                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
                }
            }

            if (m.action == ActionType.DefendBase) continue; // persistent defender mission

            // Path 3: ACTING at/past settlesAtTick → resolve
            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
                if (m.active && getActionDuration(m.action) > 0) {
                    _completeMission(cs, m);
                }
            }

            // If mission completed during this tick, stop iterating
            if (!m.active) break;
        }
    }

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

            // 3. Advance each clansman (single-tick range: [tick, tick+1))
            for (uint256 i = 0; i < clansmanIds.length; i++) {
                Clansman storage cs = _clansmen[clansmanIds[i]];
                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
            }
        }

        clan.lastSettledTick = curTick;
        emit ClanSettled(clanId, curTick);
    }

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
            // Persistent mission. Registration happens atomically at order submission.
        } else if (
            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
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

    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
        internal
        returns (uint256 goldBonus)
    {
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
        Clan storage,
        /* clan — unused but kept positional for callsite parity */
        Clansman storage cs,
        Mission storage m,
        uint32 clanId,
        uint64 tick,
        bool starving

codex
Small wrinkle: the checked-out working tree appears older than the pasted head in a few key places, so I’m checking whether `400463e` is available locally. If it is, I’ll review the actual commit contents instead of mentally applying the diff.
exec
/bin/bash -lc 'git rev-parse --show-toplevel' in /home/claude/code/clan-world
 succeeded in 0ms:
/home/claude/code/omnipass-world/clan-world

exec
/bin/bash -lc 'git status --short --branch' in /home/claude/code/clan-world
 succeeded in 0ms:
## dev...origin/dev
?? docs/planning/gold-bridge-monorepo.zip
?? docs/reviews/pr153-review-claude-opus.md
?? docs/reviews/pr153-review-codex-5-3.md
?? docs/reviews/pr194-r2-codereview-codex-5-4.md
?? docs/reviews/pr194-r2-codereview-codex-5-5.md
?? docs/reviews/pr194-r2-codereview-gemini-3-1-pro.md
?? docs/reviews/pr194-r2-codereview-opus-4-7.md
?? docs/reviews/pr194-r2-synthesis.md
?? docs/reviews/pr198-r2-codereview-codex-5-4.md
?? docs/reviews/pr198-r2-codereview-codex-5-5.md
?? docs/reviews/pr198-r2-codereview-opus-4-7.md
?? docs/reviews/pr198-r2-synthesis.md
?? docs/reviews/pr199-codereview-codex-5-4.md
?? docs/reviews/pr199-codereview-codex-5-5.md
?? docs/reviews/pr199-codereview-opus-4-7.md
?? docs/reviews/pr199-r3-codereview-codex-5-4.md
?? docs/reviews/pr199-r3-codereview-codex-5-5.md
?? docs/reviews/pr199-r3-codereview-gemini-3-1-pro.md
?? docs/reviews/pr199-r3-codereview-opus-4-6.md
?? docs/reviews/pr199-r3-codereview-opus-4-7.md
?? docs/reviews/pr199-r3-synthesis.md
?? docs/reviews/pr199-synthesis.md
?? docs/reviews/pr200-codereview-codex-5-4.md
?? docs/reviews/pr200-codereview-codex-5-5.md
?? docs/reviews/pr200-codereview-opus-4-7.md
?? docs/reviews/pr200-r2-codereview-codex-5-3.md
?? docs/reviews/pr200-r2-codereview-codex-5-4.md
?? docs/reviews/pr200-r2-codereview-codex-5-5.md
?? docs/reviews/pr200-r2-codereview-gemini-3-1-pro.md
?? docs/reviews/pr200-r2-codereview-opus-4-6.md
?? docs/reviews/pr200-r2-codereview-opus-4-7.md
?? docs/reviews/pr200-synthesis.md
?? docs/reviews/pr250-r2-codereview-codex-5-5.md
?? docs/reviews/pr250-r2-codereview-opus-4-7.md
?? docs/reviews/pr250-r2-synthesis-update.md
?? docs/reviews/pr250-r2-synthesis.md
?? docs/reviews/pr250-r3-codereview-codex-5-3.md
?? docs/reviews/pr250-r3-codereview-codex-5-4.md
?? docs/reviews/pr250-r3-codereview-codex-5-5.md
?? docs/reviews/pr250-r3-codereview-gemini-3-1-pro.md
?? docs/reviews/pr250-r3-codereview-opus-4-6.md
?? docs/reviews/pr250-r3-codereview-opus-4-7.md
?? docs/reviews/r2-phase10/
?? docs/reviews/r2-phase6/
?? docs/reviews/r2/

exec
/bin/bash -lc 'git show --stat --oneline --decorate 400463e' in /home/claude/code/clan-world
 succeeded in 0ms:
400463e (origin/dev-phase-10-winter) fix(contracts): Phase 10 R3 — cold-reset regression + starvation guard + ABI staleness + crop-transition brick + 4 MED (#289)
 docs/planning/clanworld_v4_spec.md           |  2 +-
 packages/contracts/abi/IClanWorld.json       | 12 ++--
 packages/contracts/src/ClanWorld.sol         | 43 ++++++-------
 packages/contracts/src/IClanWorld.sol        |  7 ++-
 packages/contracts/test/ClanWorld.t.sol      | 91 +++++++++++++++++++++++++---
 packages/runner/src/runnerCastHeartbeat.ts   |  2 +
 packages/shared/src/adapters/IChainClient.ts |  2 +
 7 files changed, 117 insertions(+), 42 deletions(-)

exec
/bin/bash -lc 'git branch --contains 400463e' in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1080,1380p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1600,2045p'" in /home/claude/code/clan-world
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

 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '550,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1000,1090p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2040,2275p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git grep -n \"ERR_WINTER_LOCKED\\|ERR_MUST_SETTLE_FIRST\\|StatusCode\\|WorldState\" 400463e -- packages apps | head -200" in /home/claude/code/clan-world
 succeeded in 0ms:
400463e:packages/contracts/abi/IClanWorld.json:2021:      "name": "getWorldState",
400463e:packages/contracts/abi/IClanWorld.json:2027:          "internalType": "struct WorldState",
400463e:packages/contracts/abi/IClanWorld.json:2385:              "internalType": "enum StatusCode"
400463e:packages/contracts/abi/IClanWorld.json:3207:          "internalType": "enum StatusCode"
400463e:packages/contracts/src/ClanWorld.sol:15:    StatusCode,
400463e:packages/contracts/src/ClanWorld.sol:16:    WorldState,
400463e:packages/contracts/src/ClanWorld.sol:54:    WorldState private _world;
400463e:packages/contracts/src/ClanWorld.sol:1201:    function _worldStateView() internal view returns (WorldState memory ws) {
400463e:packages/contracts/src/ClanWorld.sol:1334:                        status: StatusCode.ERR_MUST_SETTLE_FIRST,
400463e:packages/contracts/src/ClanWorld.sol:1351:                    status: StatusCode.ERR_CLAN_DEAD,
400463e:packages/contracts/src/ClanWorld.sol:1387:            result.status = StatusCode.ERR_INVALID_CLANSMAN;
400463e:packages/contracts/src/ClanWorld.sol:1391:            result.status = StatusCode.ERR_CLANSMAN_DEAD;
400463e:packages/contracts/src/ClanWorld.sol:1396:            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
400463e:packages/contracts/src/ClanWorld.sol:1397:            if (defendErr != StatusCode.OK) {
400463e:packages/contracts/src/ClanWorld.sol:1408:                result.status = StatusCode.OK;
400463e:packages/contracts/src/ClanWorld.sol:1417:            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
400463e:packages/contracts/src/ClanWorld.sol:1438:            result.status = StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1443:        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
400463e:packages/contracts/src/ClanWorld.sol:1444:        if (actionErr != StatusCode.OK) {
400463e:packages/contracts/src/ClanWorld.sol:1504:        result.status = StatusCode.OK;
400463e:packages/contracts/src/ClanWorld.sol:1621:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
400463e:packages/contracts/src/ClanWorld.sol:1630:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
400463e:packages/contracts/src/ClanWorld.sol:1641:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
400463e:packages/contracts/src/ClanWorld.sol:1656:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
400463e:packages/contracts/src/ClanWorld.sol:1777:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
400463e:packages/contracts/src/ClanWorld.sol:1782:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
400463e:packages/contracts/src/ClanWorld.sol:1788:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
400463e:packages/contracts/src/ClanWorld.sol:1811:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
400463e:packages/contracts/src/ClanWorld.sol:1816:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
400463e:packages/contracts/src/ClanWorld.sol:1827:                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
400463e:packages/contracts/src/ClanWorld.sol:1832:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
400463e:packages/contracts/src/ClanWorld.sol:1849:        returns (StatusCode)
400463e:packages/contracts/src/ClanWorld.sol:1853:        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
400463e:packages/contracts/src/ClanWorld.sol:1857:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
400463e:packages/contracts/src/ClanWorld.sol:1863:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
400463e:packages/contracts/src/ClanWorld.sol:1868:            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1872:            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1879:                return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1884:            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1891:                return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1894:                return StatusCode.ERR_WINTER_LOCKED;
400463e:packages/contracts/src/ClanWorld.sol:1904:            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
400463e:packages/contracts/src/ClanWorld.sol:1906:                return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1908:            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
400463e:packages/contracts/src/ClanWorld.sol:1912:                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
400463e:packages/contracts/src/ClanWorld.sol:1918:                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
400463e:packages/contracts/src/ClanWorld.sol:1928:        return StatusCode.OK;
400463e:packages/contracts/src/ClanWorld.sol:1934:        returns (StatusCode)
400463e:packages/contracts/src/ClanWorld.sol:1936:        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
400463e:packages/contracts/src/ClanWorld.sol:1937:        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
400463e:packages/contracts/src/ClanWorld.sol:1938:        return StatusCode.OK;
400463e:packages/contracts/src/ClanWorld.sol:2010:    function getWorldState() external view override returns (WorldState memory) {
400463e:packages/contracts/src/ClanWorld.sol:2230:        WorldState memory ws = _worldStateView();
400463e:packages/contracts/src/ClanWorldStub.sol:15:    StatusCode,
400463e:packages/contracts/src/ClanWorldStub.sol:16:    WorldState,
400463e:packages/contracts/src/ClanWorldStub.sol:44:    WorldState private _world;
400463e:packages/contracts/src/ClanWorldStub.sol:139:    function getWorldState() external view override returns (WorldState memory) {
400463e:packages/contracts/src/ClanWorldStub.sol:302:        WorldState memory ws = _worldStateView();
400463e:packages/contracts/src/ClanWorldStub.sol:408:    function _worldStateView() internal view returns (WorldState memory ws) {
400463e:packages/contracts/src/IClanWorld.sol:156:enum StatusCode {
400463e:packages/contracts/src/IClanWorld.sol:185:    ERR_WINTER_LOCKED,
400463e:packages/contracts/src/IClanWorld.sol:186:    ERR_MUST_SETTLE_FIRST
400463e:packages/contracts/src/IClanWorld.sol:193:struct WorldState {
400463e:packages/contracts/src/IClanWorld.sol:384:    StatusCode status;
400463e:packages/contracts/src/IClanWorld.sol:589:    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
400463e:packages/contracts/src/IClanWorld.sol:649:    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
400463e:packages/contracts/src/IClanWorld.sol:709:    function getWorldState() external view returns (WorldState memory);
400463e:packages/contracts/test/ClanWorld.t.sol:20:    StatusCode,
400463e:packages/contracts/test/ClanWorld.t.sol:21:    WorldState,
400463e:packages/contracts/test/ClanWorld.t.sol:158:        while (world.getWorldState().currentTick < targetTick) {
400463e:packages/contracts/test/ClanWorld.t.sol:229:        uint64 tickBefore = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:233:        uint64 tickAfter = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:242:        WorldState memory beforeFirst = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:249:        WorldState memory afterFirst = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:258:        WorldState memory afterSecond = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:268:        uint64 tickBefore = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:273:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "non-owner caller should advance heartbeat");
400463e:packages/contracts/test/ClanWorld.t.sol:278:        uint64 previousTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:287:            WorldState memory state = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:386:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "valid order should succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:387:        assertEq(uint8(results[1].status), uint8(StatusCode.ERR_INVALID_CLANSMAN), "bad csId should fail");
400463e:packages/contracts/test/ClanWorld.t.sol:401:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:405:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE), "second order should hit cooldown");
400463e:packages/contracts/test/ClanWorld.t.sol:432:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "NOOP order should succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:460:        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
400463e:packages/contracts/test/ClanWorld.t.sol:556:    // Test B: submitClanOrders returns ERR_MUST_SETTLE_FIRST when clan is >200 ticks behind
400463e:packages/contracts/test/ClanWorld.t.sol:569:        // submitClanOrders should return ERR_MUST_SETTLE_FIRST
400463e:packages/contracts/test/ClanWorld.t.sol:587:            uint8(StatusCode.ERR_MUST_SETTLE_FIRST),
400463e:packages/contracts/test/ClanWorld.t.sol:588:            "clan >200 ticks behind must return ERR_MUST_SETTLE_FIRST"
400463e:packages/contracts/test/ClanWorld.t.sol:590:        assertEq(uint8(StatusCode.ERR_WINTER_LOCKED), 28, "existing status indices must remain stable");
400463e:packages/contracts/test/ClanWorld.t.sol:591:        assertEq(uint8(StatusCode.ERR_MUST_SETTLE_FIRST), 29, "new status code must be appended");
400463e:packages/contracts/test/ClanWorld.t.sol:605:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:618:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "interrupt order should succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:676:            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "market sell should enqueue");
400463e:packages/contracts/test/ClanWorld.t.sol:700:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "sell order should be accepted");
400463e:packages/contracts/test/ClanWorld.t.sol:707:        uint64 curTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:738:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
400463e:packages/contracts/test/ClanWorld.t.sol:743:        uint64 curTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:768:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "order submission should succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:772:        uint64 curTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:785:            clanId, csId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
400463e:packages/contracts/test/ClanWorld.t.sol:803:        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
400463e:packages/contracts/test/ClanWorld.t.sol:811:        uint64 curTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:829:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first sell order should be accepted");
400463e:packages/contracts/test/ClanWorld.t.sol:837:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "replacement sell order should be accepted");
400463e:packages/contracts/test/ClanWorld.t.sol:852:        emit IClanWorldEvents.MarketActionFailed(clanId, csId, ActionType.MarketSell, StatusCode.ERR_INVALID_ACTION);
400463e:packages/contracts/test/ClanWorld.t.sol:948:        assertEq(uint8(nativeResult[0].status), uint8(StatusCode.OK), "native next-tick action should enqueue");
400463e:packages/contracts/test/ClanWorld.t.sol:1029:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "clan1 sell order ok");
400463e:packages/contracts/test/ClanWorld.t.sol:1044:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "clan2 sell order ok");
400463e:packages/contracts/test/ClanWorld.t.sol:1065:        uint64 curTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:1123:        uint64 curTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/ClanWorld.t.sol:1162:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "market sell to Forest should fail");
400463e:packages/contracts/test/ClanWorld.t.sol:1197:            uint8(StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN),
400463e:packages/contracts/test/ClanWorld.t.sol:1200:        assertEq(uint8(results[1].status), uint8(StatusCode.OK), "other batch orders should proceed");
400463e:packages/contracts/test/ClanWorld.t.sol:1226:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first DefendBase OK");
400463e:packages/contracts/test/ClanWorld.t.sol:1239:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "re-task DefendBase OK");
400463e:packages/contracts/test/ClanWorld.t.sol:1262:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "3.E1: status must be OK");
400463e:packages/contracts/test/ClanWorld.t.sol:1299:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_CLANSMAN), "3.E2: cross-clan csId must be invalid");
400463e:packages/contracts/test/ClanWorld.t.sol:1331:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
400463e:packages/contracts/test/ClanWorld.t.sol:1344:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "3.E4: first order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1349:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE), "3.E4: must return ERR_COOLDOWN_ACTIVE");
400463e:packages/contracts/test/ClanWorld.t.sol:1366:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "3.E5: order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1387:            uint8(StatusCode.ERR_COOLDOWN_ACTIVE),
400463e:packages/contracts/test/ClanWorld.t.sol:1397:        assertEq(uint8(r3[0].status), uint8(StatusCode.OK), "3.E5: after cooldown expires must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1413:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "3.E5b: first order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1425:            uint8(StatusCode.OK),
400463e:packages/contracts/test/ClanWorld.t.sol:1453:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "DefendBase order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1500:            uint8(r0[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: ChopWood to Mountains must be invalid"
400463e:packages/contracts/test/ClanWorld.t.sol:1505:        assertEq(uint8(r1[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: MineIron to Forest must be invalid");
400463e:packages/contracts/test/ClanWorld.t.sol:1509:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: FishDocks to Forest must be invalid");
400463e:packages/contracts/test/ClanWorld.t.sol:1522:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "3.E7: first order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1534:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "3.E7: interrupt order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1599:        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
400463e:packages/contracts/test/ClanWorld.t.sol:1695:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "path4: first order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1713:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "path4: interrupt order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1762:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "path5: DefendBase order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1792:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "path6: order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1833:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "path6-defender: DefendBase order must succeed");
400463e:packages/contracts/test/ClanWorld.t.sol:1973:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "3.E8: order[0] must be OK");
400463e:packages/contracts/test/ClanWorld.t.sol:1976:            uint8(StatusCode.ERR_INVALID_CLANSMAN),
400463e:packages/contracts/test/ClanWorld.t.sol:1979:        assertEq(uint8(results[2].status), uint8(StatusCode.OK), "3.E8: order[2] must be OK");
400463e:packages/contracts/test/ClanWorld.t.sol:1981:            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"
400463e:packages/contracts/test/ClanWorld.t.sol:1990:        WorldState memory ws = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:2016:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
400463e:packages/contracts/test/ClanWorld.t.sol:2019:        WorldState memory ws = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:2037:        WorldState memory ws = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:2056:        assertEq(world.getWorldState().currentTick, nextWinterStart + 1);
400463e:packages/contracts/test/ClanWorld.t.sol:2088:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
400463e:packages/contracts/test/ClanWorld.t.sol:2127:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
400463e:packages/contracts/test/ClanWorld.t.sol:2321:        assertEq(world.getWorldState().currentTick, winterStart, "full clan cap should not brick winter heartbeat");
400463e:packages/contracts/test/ClanWorld.t.sol:2458:        WorldState memory ws = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:2468:        WorldState memory ws0 = world.getWorldState();
400463e:packages/contracts/test/ClanWorld.t.sol:2472:        WorldState memory ws1 = world.getWorldState();
400463e:packages/contracts/test/ClanWorldStub.t.sol:6:import {ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
400463e:packages/contracts/test/ClanWorldStub.t.sol:28:        assertEq(stub.getWorldState().currentTick, 1);
400463e:packages/contracts/test/ClanWorldStub.t.sol:30:        assertEq(stub.getWorldState().currentTick, 2);
400463e:packages/contracts/test/ClanWorldStub.t.sol:39:        WorldState memory ws = stub.getWorldState();
400463e:packages/contracts/test/DefendBase.t.sol:10:    StatusCode,
400463e:packages/contracts/test/DefendBase.t.sol:78:        while (world.getWorldState().currentTick < targetTick) {
400463e:packages/contracts/test/DefendBase.t.sol:100:        uint64 submittedAtTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/DefendBase.t.sol:104:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "defend_base status");
400463e:packages/contracts/test/DefendBase.t.sol:139:        assertEq(uint8(move[0].status), uint8(StatusCode.OK), "move away accepted");
400463e:packages/contracts/test/DefendBase.t.sol:149:        assertEq(uint8(defend[0].status), uint8(StatusCode.OK), "defend accepted");
400463e:packages/contracts/test/DefendBase.t.sol:180:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "non-home defend rejected");
400463e:packages/contracts/test/DefendBase.t.sol:194:        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first status");
400463e:packages/contracts/test/DefendBase.t.sol:195:        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "same-target no-op status");
400463e:packages/contracts/test/DefendBase.t.sol:210:        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "explicit self is same target");
400463e:packages/contracts/test/DefendBase.t.sol:225:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "defend accepted");
400463e:packages/contracts/test/DefendBase.t.sol:284:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "wait retask accepted");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:13:    StatusCode,
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:14:    WorldState,
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:69:        while (world.getWorldState().currentTick < targetTick) {
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:187:        uint64 t0 = world.getWorldState().currentTick;
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:195:        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:202:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:232:        WorldState memory before_ = world.getWorldState();
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:239:        WorldState memory after_ = world.getWorldState();
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:251:        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:252:        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:265:        WorldState memory ws0 = world.getWorldState();
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:275:        WorldState memory ws1 = world.getWorldState();
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:291:        uint64 tickBefore = world.getWorldState().currentTick;
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:292:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:296:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "tick must increment");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:297:        assertNotEq(world.getWorldState().currentTickSeed, seedBefore, "seed must change");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:315:        uint64 t0 = world.getWorldState().currentTick;
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:322:        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
400463e:packages/contracts/test/HeartbeatOrdering.t.sol:328:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
400463e:packages/contracts/test/MissionTiming.t.sol:10:    StatusCode,
400463e:packages/contracts/test/MissionTiming.t.sol:32:        while (world.getWorldState().currentTick < targetTick) {
400463e:packages/contracts/test/MissionTiming.t.sol:68:        uint64 submittedAt = world.getWorldState().currentTick;
400463e:packages/contracts/test/MissionTiming.t.sol:75:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");
400463e:packages/contracts/test/MissionTiming.t.sol:97:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");
400463e:packages/contracts/test/MissionTiming.t.sol:155:        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first wait");
400463e:packages/contracts/test/MissionTiming.t.sol:161:        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "second wait");
400463e:packages/contracts/test/MissionTiming.t.sol:165:        assertEq(secondSubmitted, world.getWorldState().currentTick, "second submit uses current tick");
400463e:packages/contracts/test/Reentrancy.t.sol:12:    StatusCode,
400463e:packages/contracts/test/Reentrancy.t.sol:124:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "market sell should enqueue");
400463e:packages/contracts/test/Reentrancy.t.sol:127:        uint64 currentTick = world.getWorldState().currentTick;
400463e:packages/contracts/test/Reentrancy.t.sol:179:        vm.warp(world.getWorldState().nextHeartbeatAtTs);

codex
I’ve got the head commit available as `origin/dev-phase-10-winter`, so I’m using `git show 400463e:…` to inspect the exact merged state without touching the worktree. One promising seam is the ordering between heartbeat-driven work and lazy clan upkeep/death; that’s where the winter mechanics change the old assumptions most.
exec
/bin/bash -lc "sed -n '1,240p' docs/reviews/pr250-r3-synthesis.md" in /home/claude/code/clan-world
 exited 2 in 0ms:
sed: can't read docs/reviews/pr250-r3-synthesis.md: No such file or directory

exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr250-r3-codereview-codex-5-5.md" in /home/claude/code/clan-world
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
session id: 019ddc0a-92c8-7603-89bb-83421831e3ff
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

exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr250-r3-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "git show 400463e:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '220,520p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 400463e:packages/runner/src/runnerCastHeartbeat.ts | nl -ba | sed -n '1,180p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorldStub.sol | nl -ba | sed -n '1,440p'" in /home/claude/code/clan-world
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
/bin/bash -lc "git grep -n \"clanState\\|livingClansmen\\|ERR_CLAN_DEAD\\|ERR_MUST_SETTLE_FIRST\\|winterActive\\|winterStartsAtTick\" 400463e -- apps packages/shared packages/runner packages/agents | head -240" in /home/claude/code/clan-world
 succeeded in 0ms:
400463e:apps/server/convex/schema.ts:13:    winterActive: v.optional(v.boolean()),
400463e:apps/server/convex/schema.ts:14:    winterStartsAtTick: v.optional(v.number()),
400463e:packages/runner/src/runnerCastHeartbeat.ts:50:          { name: 'winterActive', type: 'bool' },
400463e:packages/runner/src/runnerCastHeartbeat.ts:51:          { name: 'winterStartsAtTick', type: 'uint64' },
400463e:packages/shared/src/adapters/IChainClient.ts:41:          { name: 'winterActive', type: 'bool' },
400463e:packages/shared/src/adapters/IChainClient.ts:42:          { name: 'winterStartsAtTick', type: 'uint64' },
400463e:packages/shared/src/adapters/IChainClient.ts:55:              { name: 'livingClansmen', type: 'uint8' },
400463e:packages/shared/src/adapters/IChainClient.ts:85:                  { name: 'clanState', type: 'uint8' },
400463e:packages/shared/src/adapters/IChainClient.ts:90:                  { name: 'livingClansmen', type: 'uint8' },
400463e:packages/shared/src/mocks/clanWorldFixture.ts:45:  livingClansmen: number;
400463e:packages/shared/src/mocks/clanWorldFixture.ts:120:  livingClansmen: 2,
400463e:packages/shared/src/mocks/clanWorldFixture.ts:140:  livingClansmen: 3,
400463e:packages/shared/src/mocks/clanWorldFixture.ts:160:  livingClansmen: 4,
400463e:packages/shared/src/mocks/clanWorldFixture.ts:180:  livingClansmen: 3,

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '260,430p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '430,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 400463e:packages/shared/src/types.ts | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
     1	// Minimal shared types. Wave 0 placeholders — real shapes are defined in the v4
     2	// state schema spec (docs/planning/V1/01 Blockchain Game Spec/
     3	// clanworld_v4_2_state_schema_interface_spec.md). Expand as streams need them.
     4	
     5	export type Tick = number;
     6	
     7	export interface TickEpoch {
     8	  /** Unix seconds when this tick window started. */
     9	  startedAt: number;
    10	  /** Duration of one tick in milliseconds. 20000 for Submission 1, 60000 for Submission 2 live. */
    11	  durationMs: number;
    12	}
    13	
    14	export interface Region {
    15	  id: string;
    16	  name: string;
    17	  ownerClanId: string | null;
    18	}
    19	
    20	export interface Clan {
    21	  id: string;
    22	  name: string;
    23	  /** Decimal string representation of wei amount; consumers parse with BigInt() if arithmetic needed. */
    24	  treasury: string;
    25	}
    26	
    27	export interface WorldSnapshot {
    28	  tick: Tick;
    29	  tickEpoch: TickEpoch;
    30	  regions: Region[];
    31	  clans: Clan[];
    32	}
    33	
    34	export interface ClanFullView {
    35	  clan: Clan;
    36	  controlledRegions: Region[];
    37	  /** Pending orders this clan submitted for the next tick. */
    38	  pendingOrders: ClanOrder[];
    39	  /** Inbound whispers since the last view. */
    40	  whispers: Whisper[];
    41	}
    42	
    43	export interface ClanOrder {
    44	  kind: 'mission' | 'transfer' | 'mint';
    45	  payload: Record<string, unknown>;
    46	}
    47	
    48	export interface Whisper {
    49	  fromClanId: string;
    50	  toClanId: string;
    51	  text: string;
    52	  tick: Tick;
    53	}

exec
/bin/bash -lc "git show 400463e:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '1980,2435p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 400463e:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '2434,2490p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
/bin/bash -lc "ls docs/reviews | rg 'pr250|r3'" in /home/claude/code/clan-world
 succeeded in 0ms:
pr199-r3-codereview-codex-5-4.md
pr199-r3-codereview-codex-5-5.md
pr199-r3-codereview-gemini-3-1-pro.md
pr199-r3-codereview-opus-4-6.md
pr199-r3-codereview-opus-4-7.md
pr199-r3-synthesis.md
pr250-r2-codereview-codex-5-5.md
pr250-r2-codereview-opus-4-7.md
pr250-r2-synthesis.md
pr250-r2-synthesis-update.md
pr250-r3-codereview-codex-5-3.md
pr250-r3-codereview-codex-5-4.md
pr250-r3-codereview-codex-5-5.md
pr250-r3-codereview-gemini-3-1-pro.md
pr250-r3-codereview-opus-4-6.md
pr250-r3-codereview-opus-4-7.md

exec
/bin/bash -lc 'rg -n "HIGH|MEDIUM|finding|ClanWorld.sol|NEEDS_FIXES|CLEAN" docs/reviews/pr250-r3-codereview-codex-5-5.md' in /home/claude/code/clan-world
 succeeded in 0ms:
41:2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.
43:## HIGH severity findings
46:## MEDIUM severity findings
49:## LOW severity findings
54:If clean, say "CLEAN — no findings" under each section.
467:diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
469:--- a/packages/contracts/src/ClanWorld.sol
470:+++ b/packages/contracts/src/ClanWorld.sol
473: } from "./IClanWorld.sol";
1084:diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
1086:--- a/packages/contracts/src/IClanWorld.sol
1087:+++ b/packages/contracts/src/IClanWorld.sol
1871:Solidity contracts for ClanWorld. Foundry-managed. The canonical seam interface is `src/IClanWorld.sol` — every other workspace talks to chain through it (via the `IChainClient` adapter).
1882:Only `src/IClanWorld.sol` exists. No engine impl, no deploy scripts, no tests. Wave 1 ships the engine + deploy.
1886:- `src/IClanWorld.sol` — canonical seam (DO NOT MODIFY without ADR; see `../../docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md`).
1893:- **`IClanWorld.sol` is sacred.** Changes require: ADR amendment, full team sign-off, all 6 spec docs reviewed for ripple effects.
1913:/bin/bash -lc "sed -n '1,260p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
2177:/bin/bash -lc "sed -n '1,260p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
2217:} from "./IClanWorld.sol";
2441:/bin/bash -lc "sed -n '260,620p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
2868: packages/contracts/src/ClanWorld.sol         | 43 ++++++-------
2869: packages/contracts/src/IClanWorld.sol        |  7 ++-
2879:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '340,560p'" in /home/claude/code/clan-world
3104:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1080,1380p'" in /home/claude/code/clan-world
3409:/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
3411:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1600,2045p'" in /home/claude/code/clan-world
4103:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '550,760p'" in /home/claude/code/clan-world
4318:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1000,1090p'" in /home/claude/code/clan-world
4413:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2040,2275p'" in /home/claude/code/clan-world
4659:400463e:packages/contracts/src/ClanWorld.sol:15:    StatusCode,
4660:400463e:packages/contracts/src/ClanWorld.sol:16:    WorldState,
4661:400463e:packages/contracts/src/ClanWorld.sol:54:    WorldState private _world;
4662:400463e:packages/contracts/src/ClanWorld.sol:1201:    function _worldStateView() internal view returns (WorldState memory ws) {
4663:400463e:packages/contracts/src/ClanWorld.sol:1334:                        status: StatusCode.ERR_MUST_SETTLE_FIRST,
4664:400463e:packages/contracts/src/ClanWorld.sol:1351:                    status: StatusCode.ERR_CLAN_DEAD,
4665:400463e:packages/contracts/src/ClanWorld.sol:1387:            result.status = StatusCode.ERR_INVALID_CLANSMAN;
4666:400463e:packages/contracts/src/ClanWorld.sol:1391:            result.status = StatusCode.ERR_CLANSMAN_DEAD;
4667:400463e:packages/contracts/src/ClanWorld.sol:1396:            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
4668:400463e:packages/contracts/src/ClanWorld.sol:1397:            if (defendErr != StatusCode.OK) {
4669:400463e:packages/contracts/src/ClanWorld.sol:1408:                result.status = StatusCode.OK;
4670:400463e:packages/contracts/src/ClanWorld.sol:1417:            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
4671:400463e:packages/contracts/src/ClanWorld.sol:1438:            result.status = StatusCode.ERR_INVALID_REGION;
4672:400463e:packages/contracts/src/ClanWorld.sol:1443:        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
4673:400463e:packages/contracts/src/ClanWorld.sol:1444:        if (actionErr != StatusCode.OK) {
4674:400463e:packages/contracts/src/ClanWorld.sol:1504:        result.status = StatusCode.OK;
4675:400463e:packages/contracts/src/ClanWorld.sol:1621:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
4676:400463e:packages/contracts/src/ClanWorld.sol:1630:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
4677:400463e:packages/contracts/src/ClanWorld.sol:1641:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
4678:400463e:packages/contracts/src/ClanWorld.sol:1656:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
4679:400463e:packages/contracts/src/ClanWorld.sol:1777:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
4680:400463e:packages/contracts/src/ClanWorld.sol:1782:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
4681:400463e:packages/contracts/src/ClanWorld.sol:1788:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
4682:400463e:packages/contracts/src/ClanWorld.sol:1811:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
4683:400463e:packages/contracts/src/ClanWorld.sol:1816:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
4684:400463e:packages/contracts/src/ClanWorld.sol:1827:                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
4685:400463e:packages/contracts/src/ClanWorld.sol:1832:            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
4686:400463e:packages/contracts/src/ClanWorld.sol:1849:        returns (StatusCode)
4687:400463e:packages/contracts/src/ClanWorld.sol:1853:        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
4688:400463e:packages/contracts/src/ClanWorld.sol:1857:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
4689:400463e:packages/contracts/src/ClanWorld.sol:1863:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
4690:400463e:packages/contracts/src/ClanWorld.sol:1868:            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
4691:400463e:packages/contracts/src/ClanWorld.sol:1872:            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
4692:400463e:packages/contracts/src/ClanWorld.sol:1879:                return StatusCode.ERR_INVALID_REGION;
4693:400463e:packages/contracts/src/ClanWorld.sol:1884:            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
4694:400463e:packages/contracts/src/ClanWorld.sol:1891:                return StatusCode.ERR_INVALID_REGION;
4695:400463e:packages/contracts/src/ClanWorld.sol:1894:                return StatusCode.ERR_WINTER_LOCKED;
4696:400463e:packages/contracts/src/ClanWorld.sol:1904:            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
4697:400463e:packages/contracts/src/ClanWorld.sol:1906:                return StatusCode.ERR_INVALID_REGION;
4698:400463e:packages/contracts/src/ClanWorld.sol:1908:            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
4699:400463e:packages/contracts/src/ClanWorld.sol:1912:                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
4700:400463e:packages/contracts/src/ClanWorld.sol:1918:                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
4701:400463e:packages/contracts/src/ClanWorld.sol:1928:        return StatusCode.OK;
4702:400463e:packages/contracts/src/ClanWorld.sol:1934:        returns (StatusCode)
4703:400463e:packages/contracts/src/ClanWorld.sol:1936:        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
4704:400463e:packages/contracts/src/ClanWorld.sol:1937:        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
4705:400463e:packages/contracts/src/ClanWorld.sol:1938:        return StatusCode.OK;
4706:400463e:packages/contracts/src/ClanWorld.sol:2010:    function getWorldState() external view override returns (WorldState memory) {
4707:400463e:packages/contracts/src/ClanWorld.sol:2230:        WorldState memory ws = _worldStateView();
4714:400463e:packages/contracts/src/IClanWorld.sol:156:enum StatusCode {
4715:400463e:packages/contracts/src/IClanWorld.sol:185:    ERR_WINTER_LOCKED,
4716:400463e:packages/contracts/src/IClanWorld.sol:186:    ERR_MUST_SETTLE_FIRST
4717:400463e:packages/contracts/src/IClanWorld.sol:193:struct WorldState {
4718:400463e:packages/contracts/src/IClanWorld.sol:384:    StatusCode status;
4719:400463e:packages/contracts/src/IClanWorld.sol:589:    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
4720:400463e:packages/contracts/src/IClanWorld.sol:649:    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
4721:400463e:packages/contracts/src/IClanWorld.sol:709:    function getWorldState() external view returns (WorldState memory);
4808:400463e:packages/contracts/test/ClanWorldStub.t.sol:6:import {ClanWorldConstants, WorldState} from "../src/IClanWorld.sol";
4906:2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.
4908:## HIGH severity findings
4911:## MEDIUM severity findings
4914:## LOW severity findings
4919:If clean, say "CLEAN — no findings" under each section.
5730:    39	} from "./IClanWorld.sol";
6124:/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '260,430p'" in /home/claude/code/clan-world
6299:/bin/bash -lc "git show 400463e:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '430,760p'" in /home/claude/code/clan-world

exec
/bin/bash -lc 'rg -n "HIGH|MEDIUM|finding|ClanWorld.sol|NEEDS_FIXES|CLEAN" docs/reviews/pr250-r3-codereview-codex-5-4.md' in /home/claude/code/clan-world
 succeeded in 0ms:
41:2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.
43:## HIGH severity findings
46:## MEDIUM severity findings
49:## LOW severity findings
54:If clean, say "CLEAN — no findings" under each section.
467:diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
469:--- a/packages/contracts/src/ClanWorld.sol
470:+++ b/packages/contracts/src/ClanWorld.sol
473: } from "./IClanWorld.sol";
1084:diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
1086:--- a/packages/contracts/src/IClanWorld.sol
1087:+++ b/packages/contracts/src/IClanWorld.sol
1862:  • Synthesize findings into requested pre-merge review format
1870:Solidity contracts for ClanWorld. Foundry-managed. The canonical seam interface is `src/IClanWorld.sol` — every other workspace talks to chain through it (via the `IChainClient` adapter).
1881:Only `src/IClanWorld.sol` exists. No engine impl, no deploy scripts, no tests. Wave 1 ships the engine + deploy.
1885:- `src/IClanWorld.sol` — canonical seam (DO NOT MODIFY without ADR; see `../../docs/adr/ADR001-IClanWorld-as-web2-web3-integration-seam.md`).
1892:- **`IClanWorld.sol` is sacred.** Changes require: ADR amendment, full team sign-off, all 6 spec docs reviewed for ripple effects.
1970: packages/contracts/src/ClanWorld.sol         | 43 ++++++-------
1971: packages/contracts/src/IClanWorld.sol        |  7 ++-
1986:/bin/bash -lc 'rg -n "function _applyUpkeep|function _settleClan|function heartbeat|function _processOrder|function _completeHarvestMission|function _completeMission|function _markClanDead|function _clearDefender|enum StatusCode|struct WorldState|function settleClansman|function submitClanOrders|function mintClan|function getWorldState|function getWorldSnapshot|function getActiveMission|function getClansman|WheatPlotState|winter" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol' in /home/claude/code/clan-world
1988:/bin/bash -lc "sed -n '1,260p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
2252:packages/contracts/src/ClanWorld.sol:11:    WheatPlotState,
2253:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
2254:packages/contracts/src/ClanWorld.sol:92:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
2255:packages/contracts/src/ClanWorld.sol:94:        _world.winterStartsAtTick =
2256:packages/contracts/src/ClanWorld.sol:96:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
2257:packages/contracts/src/ClanWorld.sol:97:        _world.winterActive = false;
2258:packages/contracts/src/ClanWorld.sol:353:    function _settleClan(uint32 clanId) internal {
2259:packages/contracts/src/ClanWorld.sol:378:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
2260:packages/contracts/src/ClanWorld.sol:379:                    plot.state = WheatPlotState.Harvestable;
2261:packages/contracts/src/ClanWorld.sol:397:    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
2262:packages/contracts/src/ClanWorld.sol:641:        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
2263:packages/contracts/src/ClanWorld.sol:658:            plot.state = WheatPlotState.Regrowing;
2264:packages/contracts/src/ClanWorld.sol:856:    function _completeMission(Clansman storage cs, Mission storage m) internal {
2265:packages/contracts/src/ClanWorld.sol:875:    ///         4. Resolve world events (season boundary, winter transitions).
2266:packages/contracts/src/ClanWorld.sol:877:    function heartbeat() external override nonReentrant {
2267:packages/contracts/src/ClanWorld.sol:900:        // Step 4: Resolve world events (season boundary, winter transitions).
2268:packages/contracts/src/ClanWorld.sol:945:            // reset winter timers for new season
2269:packages/contracts/src/ClanWorld.sol:946:            _world.winterActive = false;
2270:packages/contracts/src/ClanWorld.sol:947:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
2271:packages/contracts/src/ClanWorld.sol:949:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
2272:packages/contracts/src/ClanWorld.sol:952:        // --- winter transitions (timer only; mechanics = Phase 10) ---
2273:packages/contracts/src/ClanWorld.sol:954:            !_world.winterActive && newTick >= _world.winterStartsAtTick
2274:packages/contracts/src/ClanWorld.sol:955:                && _world.winterStartsAtTick < _world.seasonEndTick
2275:packages/contracts/src/ClanWorld.sol:957:            _world.winterActive = true;
2276:packages/contracts/src/ClanWorld.sol:960:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
2277:packages/contracts/src/ClanWorld.sol:961:            _world.winterActive = false;
2278:packages/contracts/src/ClanWorld.sol:963:            // schedule next winter cycle within this season
2279:packages/contracts/src/ClanWorld.sol:964:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
2280:packages/contracts/src/ClanWorld.sol:966:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
2281:packages/contracts/src/ClanWorld.sol:968:                _world.winterStartsAtTick = nextWinterStart;
2282:packages/contracts/src/ClanWorld.sol:969:                _world.winterEndsAtTick = nextWinterEnd;
2283:packages/contracts/src/ClanWorld.sol:971:                // no more winters this season; sentinel = seasonEndTick so guard never fires
2284:packages/contracts/src/ClanWorld.sol:972:                _world.winterStartsAtTick = _world.seasonEndTick;
2285:packages/contracts/src/ClanWorld.sol:973:                _world.winterEndsAtTick = _world.seasonEndTick;
2286:packages/contracts/src/ClanWorld.sol:987:    function settleClansman(uint32 csId) external override nonReentrant {
2287:packages/contracts/src/ClanWorld.sol:1003:    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
2288:packages/contracts/src/ClanWorld.sol:1044:            state: WheatPlotState.Harvestable,
2289:packages/contracts/src/ClanWorld.sol:1050:            state: WheatPlotState.Harvestable,
2290:packages/contracts/src/ClanWorld.sol:1080:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
2291:packages/contracts/src/ClanWorld.sol:1134:    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
2292:packages/contracts/src/ClanWorld.sol:1335:    function _clearDefender(uint32 clansmanId) internal {
2293:packages/contracts/src/ClanWorld.sol:1763:    function getWorldState() external view override returns (WorldState memory) {
2294:packages/contracts/src/ClanWorld.sol:1775:    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
2295:packages/contracts/src/ClanWorld.sol:1779:    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
2296:packages/contracts/src/ClanWorld.sol:1962:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
2297:packages/contracts/src/ClanWorld.sol:1986:            winterActive: _world.winterActive,
2298:packages/contracts/src/ClanWorld.sol:1987:            winterStartsAtTick: _world.winterStartsAtTick,
2299:packages/contracts/src/ClanWorld.sol:1988:            winterEndsAtTick: _world.winterEndsAtTick,
2300:packages/contracts/src/IClanWorld.sol:115:enum WheatPlotState {
2301:packages/contracts/src/IClanWorld.sol:151:enum StatusCode {
2302:packages/contracts/src/IClanWorld.sol:186:struct WorldState {
2303:packages/contracts/src/IClanWorld.sol:200:    bool winterActive;
2304:packages/contracts/src/IClanWorld.sol:201:    uint64 winterStartsAtTick;
2305:packages/contracts/src/IClanWorld.sol:202:    uint64 winterEndsAtTick; // 0 if not active
2306:packages/contracts/src/IClanWorld.sol:241:    uint16 coldDamage; // resets to 0 at winter end
2307:packages/contracts/src/IClanWorld.sol:253:    WheatPlotState state;
2308:packages/contracts/src/IClanWorld.sol:415:    bool winterActive;
2309:packages/contracts/src/IClanWorld.sol:416:    uint64 winterStartsAtTick;
2310:packages/contracts/src/IClanWorld.sol:417:    uint64 winterEndsAtTick;
2311:packages/contracts/src/IClanWorld.sol:612:    // ----- winter cold damage -----
2312:packages/contracts/src/IClanWorld.sol:639:    function heartbeat() external;
2313:packages/contracts/src/IClanWorld.sol:645:    function settleClansman(uint32 csId) external;
2314:packages/contracts/src/IClanWorld.sol:655:    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
2315:packages/contracts/src/IClanWorld.sol:659:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
2316:packages/contracts/src/IClanWorld.sol:698:    function getWorldState() external view returns (WorldState memory);
2317:packages/contracts/src/IClanWorld.sol:704:    function getClansman(uint32 clansmanId) external view returns (Clansman memory);
2318:packages/contracts/src/IClanWorld.sol:706:    function getActiveMission(uint32 clansmanId) external view returns (Mission memory);
2319:packages/contracts/src/IClanWorld.sol:757:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
2320:packages/contracts/src/IClanWorld.sol:758:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
2783:/bin/bash -lc "sed -n '330,1160p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
3658:} from "./IClanWorld.sol";
3891:/bin/bash -lc "rg -n \"struct WorldSnapshot|WorldSnapshot\\(|getWorldSnapshot|currentSeasonNumber|nextHeartbeatAtTick|nextHeartbeatAtTs\" packages/contracts/src/IClanWorld.sol packages/shared/src/types.ts packages/shared/src -g'*.ts'" in /home/claude/code/clan-world
3893:packages/contracts/src/IClanWorld.sol:191:    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
3894:packages/contracts/src/IClanWorld.sol:192:    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
3895:packages/contracts/src/IClanWorld.sol:194:    uint64 nextHeartbeatAtTs;
3896:packages/contracts/src/IClanWorld.sol:408:struct WorldSnapshot {
3897:packages/contracts/src/IClanWorld.sol:413:    uint64 currentSeasonNumber;
3898:packages/contracts/src/IClanWorld.sol:414:    uint64 nextHeartbeatAtTick;
3899:packages/contracts/src/IClanWorld.sol:638:    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
3900:packages/contracts/src/IClanWorld.sol:758:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
3905:/bin/bash -lc "sed -n '380,470p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
4021:packages/contracts/src/ClanWorld.sol:1093:        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
4022:packages/contracts/src/ClanWorld.sol:1101:                        status: StatusCode.ERR_INVALID_ACTION,
4023:packages/contracts/src/ClanWorld.sol:1143:            result.status = StatusCode.ERR_INVALID_CLANSMAN;
4024:packages/contracts/src/ClanWorld.sol:1147:            result.status = StatusCode.ERR_CLANSMAN_DEAD;
4025:packages/contracts/src/ClanWorld.sol:1154:                result.status = defendErr;
4026:packages/contracts/src/ClanWorld.sol:1164:                result.status = StatusCode.OK;
4027:packages/contracts/src/ClanWorld.sol:1173:            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
4028:packages/contracts/src/ClanWorld.sol:1194:            result.status = StatusCode.ERR_INVALID_REGION;
4029:packages/contracts/src/ClanWorld.sol:1201:            result.status = actionErr;
4030:packages/contracts/src/ClanWorld.sol:1260:        result.status = StatusCode.OK;
4031:packages/contracts/src/ClanWorld.sol:1386:                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
4032:packages/contracts/src/ClanWorld.sol:1397:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
4033:packages/contracts/src/ClanWorld.sol:1412:                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
4034:packages/contracts/src/ClanWorld.sol:1609:        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
4035:packages/contracts/src/IClanWorld.sol:153:    ERR_CLAN_DEAD,
4036:packages/contracts/src/IClanWorld.sol:158:    ERR_INVALID_ACTION,
4037:packages/contracts/src/IClanWorld.sol:377:    StatusCode status;
4326:packages/contracts/src/ClanWorld.sol:90:        _world.currentSeasonNumber = 1;
4327:packages/contracts/src/ClanWorld.sol:91:        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
4328:packages/contracts/src/ClanWorld.sol:97:        _world.winterActive = false;
4329:packages/contracts/src/ClanWorld.sol:906:        _world.nextHeartbeatAtTick = newTick + 1;
4330:packages/contracts/src/ClanWorld.sol:942:            _world.currentSeasonNumber += 1;
4331:packages/contracts/src/ClanWorld.sol:946:            _world.winterActive = false;
4332:packages/contracts/src/ClanWorld.sol:954:            !_world.winterActive && newTick >= _world.winterStartsAtTick
4333:packages/contracts/src/ClanWorld.sol:957:            _world.winterActive = true;
4334:packages/contracts/src/ClanWorld.sol:960:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
4335:packages/contracts/src/ClanWorld.sol:961:            _world.winterActive = false;
4336:packages/contracts/src/ClanWorld.sol:1763:    function getWorldState() external view override returns (WorldState memory) {
4337:packages/contracts/src/ClanWorld.sol:1962:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
4338:packages/contracts/src/ClanWorld.sol:1984:            currentSeasonNumber: _world.currentSeasonNumber,
4339:packages/contracts/src/ClanWorld.sol:1985:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
4340:packages/contracts/src/ClanWorld.sol:1986:            winterActive: _world.winterActive,
4341:packages/contracts/src/IClanWorld.sol:191:    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
4342:packages/contracts/src/IClanWorld.sol:192:    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
4343:packages/contracts/src/IClanWorld.sol:200:    bool winterActive;
4344:packages/contracts/src/IClanWorld.sol:413:    uint64 currentSeasonNumber;
4345:packages/contracts/src/IClanWorld.sol:414:    uint64 nextHeartbeatAtTick;
4346:packages/contracts/src/IClanWorld.sol:415:    bool winterActive;
4347:packages/contracts/src/IClanWorld.sol:698:    function getWorldState() external view returns (WorldState memory);
4348:packages/contracts/src/IClanWorld.sol:758:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
4420:/bin/bash -lc 'rg -n "function _validateAction|ActionType.HarvestWheat|ERR_WINTER_LOCKED|WheatPlotState.WinterLocked|starvationStartsAtTick|coldDamage|wallLevel|livingClansmen" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
4448:/bin/bash -lc "sed -n '1560,1715p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
4610:/bin/bash -lc "rg -n \"ClansmanDiedFromCold|ClanColdShortage|ClansmanColdDeath|WallDegradedByCold|ClanDied|WinterStarted|WinterEnded\" packages/contracts/src/IClanWorld.sol packages/contracts/src/ClanWorld.sol packages/contracts/src/ClanWorldStub.sol packages -g'*.ts'" in /home/claude/code/clan-world
4612:packages/contracts/src/IClanWorld.sol:492:    event WinterStarted(uint64 indexed tick);
4613:packages/contracts/src/IClanWorld.sol:493:    event WinterEnded(uint64 indexed tick);
4614:packages/contracts/src/IClanWorld.sol:614:    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
4615:packages/contracts/src/ClanWorld.sol:958:            emit WinterStarted(newTick);
4616:packages/contracts/src/ClanWorld.sol:962:            emit WinterEnded(newTick);
4619:/bin/bash -lc "sed -n '480,640p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
4784:/bin/bash -lc 'rg -n "incomingDefenderIds|thisClanDefendingBaseId|_incomingDefenderIds|_defend|_clearDefender|DefendBase" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
4824:/bin/bash -lc "sed -n '1320,1385p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
4925:docs/reviews/pr250-r2-synthesis-update.md:13:| H3 | `ClanWorld.sol:1126` (`_resolveWorldEvents`), `:1164-1168` (`_resetColdDamageForAllClans`) | **Heartbeat-eager `_resetColdDamageForAllClans()` corrupts mid-winter state for partially-settled clans.** Opus reproduced locally: a clan settled at tick 113 (3 winter ticks processed, coldDamage=3, wallLevel=9) gets `coldDamage=0` zeroed by the eager heartbeat reset at winter-end. Lazy `_settleClan` then replays ticks 113-119 starting from `coldDamage=0` — gets only 3 wall degradations vs the 5 the never-settled clan gets. **Same clan ends winter at wallLevel=6 instead of 5.** Worst case: missed clansman death (livingClansmen + elimination outcome divergence). Cloud Codex P1 flagged this independently as DoS surface. Opus's repro shows it's a CORRECTNESS bug, not just DoS. **Fix:** delete `_resetColdDamageForAllClans()` + its call site at line 1126. The in-loop reset at `_applyUpkeep:404-406` and post-loop reset at `_settleClan:393-395` already handle the boundary correctly during replay. The eager scan is redundant + harmful, AND saves O(n_clans) heartbeat gas. |
4926:docs/reviews/pr250-r2-synthesis-update.md:19:| H1 | `ClanWorld.sol:431-441` (starvation guard) | HIGH | (in test verification — agrees) | — | HIGH |
4927:docs/reviews/pr250-r2-synthesis-update.md:24:| M2 | `ClanWorld.sol:1276-1290` (winter-mint plots) | MED | — | — | MED |
4928:docs/reviews/pr250-r2-synthesis-update.md:25:| M3 (NEW) | `IClanWorld.sol:28-31` (winter-tick shift) | — | MED | — | MED |
5419:docs/reviews/pr250-r3-codereview-codex-5-3.md:6991:/bin/bash -lc "git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n \"_world\\.winter\"" in /home/claude/code/clan-world
5424:docs/reviews/pr250-r3-codereview-codex-5-3.md:7009:/bin/bash -lc 'git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n "ClansmanDiedFromCold|ColdDamageApplied|ClanDied|ClanEliminated|ClansmanColdDeath|WallDegradedByCold"' in /home/claude/code/clan-world
5431:docs/reviews/pr250-r2-synthesis.md:9:**Verdict: NEEDS_FIXES — single-model verdict but concrete + targeted findings.** R1 fixes mostly landed, but Codex 5.5 found 2 NEW HIGHs that the R1 fix-round introduced or didn't fully close: starvation timing edge case for pre-winter starvers + hardcoded TS ABI staleness after WorldState/WorldSnapshot layout additions.
5433:docs/reviews/pr250-r2-synthesis.md:22:| H1 | `ClanWorld.sol:431-441` | HIGH | **Pre-winter starvers still die one tick earlier than fresh winter starvers.** The guard `if (winter && starving && clan.starvationStartsAtTick < tick)` defers death for a clan that starts starving on the first winter tick, but not for a clan already starving BEFORE winter. Example: winter starts at tick 110; a pre-winter starver with `starvationStartsAtTick = 105` dies on tick 110, while a fresh winter starver with `starvationStartsAtTick = 110` first dies on tick 111. **The added test missed this** because it sets both clans' `lastSettledTick` after winter has already begun. **Fix:** comparison needs effective winter starvation start, e.g. `max(starvationStartsAtTick, currentWinterStartTick)`. The H2 R1 fix went 80% but missed the edge. |
5434:docs/reviews/pr250-r2-synthesis.md:23:| H2 | `IClanWorld.sol:192, :414`; `packages/runner/src/runnerCastHeartbeat.ts:38-51`; `packages/shared/src/adapters/IChainClient.ts:34-44` | HIGH | **Hardcoded TS ABIs stale after WorldState / WorldSnapshot layout additions.** `IClanWorld.sol` adds `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields, but the TS hardcoded structs in runner + shared adapters do NOT include them. Practical impact: `runnerCastHeartbeat.ts` decodes `currentSeasonNumber` as `nextHeartbeatAtTs` (wrong offset), and `IChainClient.ts` decodes `getWorldSnapshot` later fields at wrong offsets — the dynamic `leaderboard` offset is especially likely to fail decoding. **Fix:** regenerate TS ABI structs from current `IClanWorld.sol` state, OR if the hardcoded copy is intentional, add the new fields. The generated ABI JSON is correct; only the hardcoded TS mirrors are stale. |
5435:docs/reviews/pr250-r2-synthesis.md:29:| M1 | `ClanWorld.sol:440-447` | **Wood burn uses post-starvation living count on same tick.** If starvation kills a clansman, wood burn is calculated AFTER from the reduced `livingClansmen`. A 4-clansman clan that loses one to starvation on a tick burns `2.5e18`, not `3e18` (per-clansman 0.5×3 + per-base 1.0). Fairness wrinkle — same-tick ordering. Liam: deliberate or fix? |
5436:docs/reviews/pr250-r2-synthesis.md:30:| M2 | `ClanWorld.sol:1276-1290` | **Clans minted during winter start `WinterLocked` with nonzero wheat.** Winter-minted plots get `state = WinterLocked`, but still initialize `remainingWheat = WHEAT_PLOT_STARTING_WHEAT`. Harvesting is blocked, so this is a state/indexer invariant bug not a gameplay exploit, but it violates the winter-lock triple expected for plots. Fix: gate the `remainingWheat` init on winter state too. |
5437:docs/reviews/pr250-r2-synthesis.md:38:- **Codex 5.5:** NEEDS_FIXES — 2 HIGH (starvation edge + ABI staleness) + 2 MED (wood burn ordering + winter-mint plot state).
5657:docs/reviews/pr250-r2-codereview-codex-5-5.md:1742:packages/contracts/src/ClanWorld.sol:94:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
5658:docs/reviews/pr250-r2-codereview-codex-5-5.md:1743:packages/contracts/src/ClanWorld.sol:96:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
5659:docs/reviews/pr250-r2-codereview-codex-5-5.md:1744:packages/contracts/src/ClanWorld.sol:97:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
5660:docs/reviews/pr250-r2-codereview-codex-5-5.md:1748:packages/contracts/src/ClanWorld.sol:948:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
5661:docs/reviews/pr250-r2-codereview-codex-5-5.md:1749:packages/contracts/src/ClanWorld.sol:950:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
5662:docs/reviews/pr250-r2-codereview-codex-5-5.md:1750:packages/contracts/src/ClanWorld.sol:965:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
5663:docs/reviews/pr250-r2-codereview-codex-5-5.md:1751:packages/contracts/src/ClanWorld.sol:967:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
5717:docs/reviews/pr250-r2-codereview-codex-5-5.md:2443:packages/contracts/src/ClanWorld.sol:48:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
5718:docs/reviews/pr250-r2-codereview-codex-5-5.md:2444:packages/contracts/src/ClanWorld.sol:94:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
5719:docs/reviews/pr250-r2-codereview-codex-5-5.md:2446:packages/contracts/src/ClanWorld.sol:96:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
5720:docs/reviews/pr250-r2-codereview-codex-5-5.md:2447:packages/contracts/src/ClanWorld.sol:97:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
5721:docs/reviews/pr250-r2-codereview-codex-5-5.md:2448:packages/contracts/src/ClanWorld.sol:98:        _world.winterActive = false;
5722:docs/reviews/pr250-r2-codereview-codex-5-5.md:2449:packages/contracts/src/ClanWorld.sol:876:    ///         4. Resolve world events (season boundary, winter transitions).
5723:docs/reviews/pr250-r2-codereview-codex-5-5.md:2450:packages/contracts/src/ClanWorld.sol:901:        // Step 4: Resolve world events (season boundary, winter transitions).
5724:docs/reviews/pr250-r2-codereview-codex-5-5.md:2451:packages/contracts/src/ClanWorld.sol:946:            // reset winter timers for new season
5725:docs/reviews/pr250-r2-codereview-codex-5-5.md:2452:packages/contracts/src/ClanWorld.sol:947:            _world.winterActive = false;
5726:docs/reviews/pr250-r2-codereview-codex-5-5.md:2453:packages/contracts/src/ClanWorld.sol:948:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
5727:docs/reviews/pr250-r2-codereview-codex-5-5.md:2454:packages/contracts/src/ClanWorld.sol:950:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
5728:docs/reviews/pr250-r2-codereview-codex-5-5.md:2455:packages/contracts/src/ClanWorld.sol:953:        // --- winter transitions (timer only; mechanics = Phase 10) ---
5729:docs/reviews/pr250-r2-codereview-codex-5-5.md:2456:packages/contracts/src/ClanWorld.sol:955:            !_world.winterActive && newTick >= _world.winterStartsAtTick
5730:docs/reviews/pr250-r2-codereview-codex-5-5.md:2457:packages/contracts/src/ClanWorld.sol:956:                && _world.winterStartsAtTick < _world.seasonEndTick
5731:docs/reviews/pr250-r2-codereview-codex-5-5.md:2458:packages/contracts/src/ClanWorld.sol:958:            _world.winterActive = true;
5732:docs/reviews/pr250-r2-codereview-codex-5-5.md:2459:packages/contracts/src/ClanWorld.sol:961:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
5733:docs/reviews/pr250-r2-codereview-codex-5-5.md:2460:packages/contracts/src/ClanWorld.sol:962:            _world.winterActive = false;
5734:docs/reviews/pr250-r2-codereview-codex-5-5.md:2461:packages/contracts/src/ClanWorld.sol:964:            // schedule next winter cycle within this season
5735:docs/reviews/pr250-r2-codereview-codex-5-5.md:2462:packages/contracts/src/ClanWorld.sol:965:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
5736:docs/reviews/pr250-r2-codereview-codex-5-5.md:2463:packages/contracts/src/ClanWorld.sol:967:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
5737:docs/reviews/pr250-r2-codereview-codex-5-5.md:2464:packages/contracts/src/ClanWorld.sol:969:                _world.winterStartsAtTick = nextWinterStart;
5738:docs/reviews/pr250-r2-codereview-codex-5-5.md:2465:packages/contracts/src/ClanWorld.sol:970:                _world.winterEndsAtTick = nextWinterEnd;
5739:docs/reviews/pr250-r2-codereview-codex-5-5.md:2466:packages/contracts/src/ClanWorld.sol:972:                // no more winters this season; sentinel = seasonEndTick so guard never fires
5740:docs/reviews/pr250-r2-codereview-codex-5-5.md:2467:packages/contracts/src/ClanWorld.sol:973:                _world.winterStartsAtTick = _world.seasonEndTick;
5741:docs/reviews/pr250-r2-codereview-codex-5-5.md:2468:packages/contracts/src/ClanWorld.sol:974:                _world.winterEndsAtTick = _world.seasonEndTick;
5742:docs/reviews/pr250-r2-codereview-codex-5-5.md:2469:packages/contracts/src/ClanWorld.sol:2454:            winterActive: _world.winterActive,
5743:docs/reviews/pr250-r2-codereview-codex-5-5.md:2470:packages/contracts/src/ClanWorld.sol:2455:            winterStartsAtTick: _world.winterStartsAtTick,
5744:docs/reviews/pr250-r2-codereview-codex-5-5.md:2471:packages/contracts/src/ClanWorld.sol:2456:            winterEndsAtTick: _world.winterEndsAtTick,
5745:docs/reviews/pr250-r2-codereview-codex-5-5.md:2473:packages/contracts/src/IClanWorld.sol:207:    bool winterActive;
5746:docs/reviews/pr250-r2-codereview-codex-5-5.md:2474:packages/contracts/src/IClanWorld.sol:208:    uint64 winterStartsAtTick;
5747:docs/reviews/pr250-r2-codereview-codex-5-5.md:2475:packages/contracts/src/IClanWorld.sol:209:    uint64 winterEndsAtTick; // 0 if not active
5748:docs/reviews/pr250-r2-codereview-codex-5-5.md:2476:packages/contracts/src/IClanWorld.sol:248:    uint16 coldDamage; // resets to 0 at winter end
5749:docs/reviews/pr250-r2-codereview-codex-5-5.md:2477:packages/contracts/src/IClanWorld.sol:423:    bool winterActive;
5750:docs/reviews/pr250-r2-codereview-codex-5-5.md:2478:packages/contracts/src/IClanWorld.sol:424:    uint64 winterStartsAtTick;
5751:docs/reviews/pr250-r2-codereview-codex-5-5.md:2479:packages/contracts/src/IClanWorld.sol:425:    uint64 winterEndsAtTick;
5752:docs/reviews/pr250-r2-codereview-codex-5-5.md:2480:packages/contracts/src/IClanWorld.sol:625:    // ----- winter cold damage -----
5753:docs/reviews/pr250-r2-codereview-codex-5-5.md:2481:packages/contracts/src/IClanWorld.sol:776:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
5794:docs/reviews/pr250-r2-codereview-codex-5-5.md:2729:packages/contracts/src/ClanWorld.sol:96:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
5795:docs/reviews/pr250-r2-codereview-codex-5-5.md:2730:packages/contracts/src/ClanWorld.sol:97:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
5796:docs/reviews/pr250-r2-codereview-codex-5-5.md:2731:packages/contracts/src/ClanWorld.sol:98:        _world.winterActive = false;
5797:docs/reviews/pr250-r2-codereview-codex-5-5.md:2732:packages/contracts/src/ClanWorld.sol:947:            _world.winterActive = false;
5798:docs/reviews/pr250-r2-codereview-codex-5-5.md:2733:packages/contracts/src/ClanWorld.sol:948:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
5799:docs/reviews/pr250-r2-codereview-codex-5-5.md:2734:packages/contracts/src/ClanWorld.sol:950:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
5800:docs/reviews/pr250-r2-codereview-codex-5-5.md:2735:packages/contracts/src/ClanWorld.sol:955:            !_world.winterActive && newTick >= _world.winterStartsAtTick
5801:docs/reviews/pr250-r2-codereview-codex-5-5.md:2736:packages/contracts/src/ClanWorld.sol:956:                && _world.winterStartsAtTick < _world.seasonEndTick
5802:docs/reviews/pr250-r2-codereview-codex-5-5.md:2737:packages/contracts/src/ClanWorld.sol:958:            _world.winterActive = true;
5803:docs/reviews/pr250-r2-codereview-codex-5-5.md:2739:packages/contracts/src/ClanWorld.sol:961:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
5804:docs/reviews/pr250-r2-codereview-codex-5-5.md:2740:packages/contracts/src/ClanWorld.sol:962:            _world.winterActive = false;
5805:docs/reviews/pr250-r2-codereview-codex-5-5.md:2742:packages/contracts/src/ClanWorld.sol:965:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
5806:docs/reviews/pr250-r2-codereview-codex-5-5.md:2743:packages/contracts/src/ClanWorld.sol:967:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
5807:docs/reviews/pr250-r2-codereview-codex-5-5.md:2744:packages/contracts/src/ClanWorld.sol:969:                _world.winterStartsAtTick = nextWinterStart;
5808:docs/reviews/pr250-r2-codereview-codex-5-5.md:2745:packages/contracts/src/ClanWorld.sol:970:                _world.winterEndsAtTick = nextWinterEnd;
5809:docs/reviews/pr250-r2-codereview-codex-5-5.md:2746:packages/contracts/src/ClanWorld.sol:973:                _world.winterStartsAtTick = _world.seasonEndTick;
5810:docs/reviews/pr250-r2-codereview-codex-5-5.md:2747:packages/contracts/src/ClanWorld.sol:974:                _world.winterEndsAtTick = _world.seasonEndTick;
5811:docs/reviews/pr250-r2-codereview-codex-5-5.md:2750:packages/contracts/src/ClanWorld.sol:2454:            winterActive: _world.winterActive,
5812:docs/reviews/pr250-r2-codereview-codex-5-5.md:2751:packages/contracts/src/ClanWorld.sol:2455:            winterStartsAtTick: _world.winterStartsAtTick,
5813:docs/reviews/pr250-r2-codereview-codex-5-5.md:2752:packages/contracts/src/ClanWorld.sol:2456:            winterEndsAtTick: _world.winterEndsAtTick,
5814:docs/reviews/pr250-r2-codereview-codex-5-5.md:2753:packages/contracts/src/IClanWorld.sol:207:    bool winterActive;
5815:docs/reviews/pr250-r2-codereview-codex-5-5.md:2754:packages/contracts/src/IClanWorld.sol:208:    uint64 winterStartsAtTick;
5816:docs/reviews/pr250-r2-codereview-codex-5-5.md:2755:packages/contracts/src/IClanWorld.sol:209:    uint64 winterEndsAtTick; // 0 if not active
5817:docs/reviews/pr250-r2-codereview-codex-5-5.md:2756:packages/contracts/src/IClanWorld.sol:423:    bool winterActive;
5818:docs/reviews/pr250-r2-codereview-codex-5-5.md:2757:packages/contracts/src/IClanWorld.sol:424:    uint64 winterStartsAtTick;
5819:docs/reviews/pr250-r2-codereview-codex-5-5.md:2758:packages/contracts/src/IClanWorld.sol:425:    uint64 winterEndsAtTick;
5865:docs/reviews/pr250-r2-codereview-codex-5-5.md:2986:docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:143:`ClanWorld.sol:1161-1165`. Iterates all clans, including DEAD ones. Resetting `coldDamage = 0` on a dead clan is harmless (the field is unread for dead clans). Wasted gas at winter-end heartbeat. Add `if (clan.clanState == ClanState.DEAD) continue;`. Same for `_lockWheatPlotsForWinter` and `_restartWheatPlotsAfterWinter` (lines 1132-1158) — locking/unlocking a dead clan's plots writes to slots that will never be read again. Trivial gas cleanup.
5880:docs/reviews/pr250-r2-codereview-codex-5-5.md:3110:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
5895:docs/reviews/pr250-r2-codereview-codex-5-5.md:3132:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
5932:docs/reviews/pr250-r2-codereview-codex-5-5.md:3293:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:10070:- `packages/contracts/src/ClanWorld.sol:1132-1140` locks wheat plots for winter without clearing `remainingWheat` or resetting regrow state, and the tests at `packages/contracts/test/ClanWorld.t.sol:2069-2074` and `:2125-2128` explicitly assert that preserved-state behavior. That disagrees with `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843-846`, which says winter start clears all plots. Harvest is blocked, so this is mostly a state/spec mismatch today, but getters will expose impossible in-winter crop state and future logic can accidentally depend on it.
5933:docs/reviews/pr250-r2-codereview-codex-5-5.md:3294:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:10093:- `packages/contracts/src/ClanWorld.sol:1132-1140` locks wheat plots for winter without clearing `remainingWheat` or resetting regrow state, and the tests at `packages/contracts/test/ClanWorld.t.sol:2069-2074` and `:2125-2128` explicitly assert that preserved-state behavior. That disagrees with `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843-846`, which says winter start clears all plots. Harvest is blocked, so this is mostly a state/spec mismatch today, but getters will expose impossible in-winter crop state and future logic can accidentally depend on it.
5962:docs/reviews/pr250-r2-codereview-codex-5-5.md:3383:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2611:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
5977:docs/reviews/pr250-r2-codereview-codex-5-5.md:3405:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2922:docs/reviews/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
6021:docs/reviews/pr250-r2-codereview-codex-5-5.md:3492:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7683:`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.
6022:docs/reviews/pr250-r2-codereview-codex-5-5.md:3493:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7722:`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.
6111:docs/reviews/pr250-r2-codereview-codex-5-5.md:5561:Two HIGHs remain: starvation timing still differs for pre-winter starvers, and hardcoded TS ABIs are now out of sync with `WorldState` / `WorldSnapshot`.
6116:docs/reviews/pr250-r2-codereview-codex-5-5.md:5581:`packages/contracts/src/IClanWorld.sol@1210e90:192` and `:414` include `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields. But:
6121:docs/reviews/pr250-r2-codereview-codex-5-5.md:5610:- Existing wheat plot winter lock correctly sets `WinterLocked`, `remainingWheat = 0`, and `regrowUntilTick = 0` at `ClanWorld.sol@1210e90:1138-1141`.
6123:docs/reviews/pr250-r2-codereview-codex-5-5.md:5625:Two HIGHs remain: starvation timing still differs for pre-winter starvers, and hardcoded TS ABIs are now out of sync with `WorldState` / `WorldSnapshot`.
6128:docs/reviews/pr250-r2-codereview-codex-5-5.md:5645:`packages/contracts/src/IClanWorld.sol@1210e90:192` and `:414` include `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields. But:
6133:docs/reviews/pr250-r2-codereview-codex-5-5.md:5674:- Existing wheat plot winter lock correctly sets `WinterLocked`, `remainingWheat = 0`, and `regrowUntilTick = 0` at `ClanWorld.sol@1210e90:1138-1141`.
6140:docs/reviews/pr199-codereview-codex-5-5.md:3027:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
6142:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
6143:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6145:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6163:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6164:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6177:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6178:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6179:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6180:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6182:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6188:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
6189:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
6190:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6202:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6219:docs/reviews/pr199-codereview-codex-5-5.md:12325:docs/reviews/pr199-codereview-codex-5-5.md:3027:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
6220:docs/reviews/pr199-codereview-codex-5-5.md:12345:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6221:docs/reviews/pr199-codereview-codex-5-5.md:12526:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6222:docs/reviews/pr199-codereview-codex-5-5.md:12534:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6223:docs/reviews/pr199-codereview-codex-5-5.md:12582:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6224:docs/reviews/pr199-codereview-codex-5-5.md:12772:docs/reviews/pr199-codereview-codex-5-4.md:5660:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6225:docs/reviews/pr199-codereview-codex-5-5.md:12773:docs/reviews/pr199-codereview-codex-5-4.md:5665:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6226:docs/reviews/pr199-codereview-codex-5-5.md:12775:docs/reviews/pr199-codereview-codex-5-4.md:5683:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6227:docs/reviews/pr199-codereview-codex-5-5.md:12884:docs/reviews/pr199-codereview-codex-5-4.md:7665:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6228:docs/reviews/pr199-codereview-codex-5-5.md:12910:docs/reviews/pr199-codereview-codex-5-4.md:9120:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6229:docs/reviews/pr199-codereview-codex-5-5.md:13126:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6236:docs/reviews/pr199-codereview-codex-5-5.md:13992:packages/contracts/src/ClanWorld.sol:1172:        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
6237:docs/reviews/pr199-synthesis.md:26:| H5 | `ClanWorld.sol:544-559` (`_markClansmanDead`), `:565-591` (`_markClanDead`) | 4.7 = 1/3 | HIGH | **Cold-damage + starvation deaths DON'T refund upgrade reservations.** `_markClansmanDead` sets `cs.state = DEAD; m.active = false;` immediately. The death-path refund branch in `_settleMissionForClansman:349-364` gates on `if (m.active)` — already false. Permanent vault resource loss + `_pending*UpgradesByClan` counter never decrements → eventually blocks ALL future upgrades. **Phase 4.4 winter cold damage already ships this regression.** Fix: extract `_invalidateActiveMission` helper that calls refund THEN sets m.active=false. Apply in `_markClansmanDead` + `_markClanDead`. |
6239:docs/reviews/pr199-synthesis.md:58:**Recommend Path A (fix-round) — high confidence.** Phase 8 has the most multi-model HIGH overlap of any phase super-swarm so far + 1 critical single-model finding (H5 death-refund) that's already shipping resource-loss bugs via Phase 4.4 winter. Fix scope:
6281:docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6282:docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6283:docs/reviews/pr199-codereview-codex-5-4.md:5492:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6284:docs/reviews/pr199-codereview-codex-5-4.md:5493:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6286:docs/reviews/pr199-codereview-codex-5-4.md:5622:docs/reviews/pr250-codereview-codex-5-4.md:1974:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6288:docs/reviews/pr199-codereview-codex-5-4.md:5624:docs/reviews/pr250-codereview-codex-5-4.md:2510:docs/reviews/pr250-codereview-codex-5-4.md:1974:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6290:docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6291:docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6293:docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6294:docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6295:docs/reviews/pr199-codereview-codex-5-4.md:5635:docs/reviews/pr250-codereview-codex-5-4.md:3452:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6296:docs/reviews/pr199-codereview-codex-5-4.md:5636:docs/reviews/pr250-codereview-codex-5-4.md:3453:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6297:docs/reviews/pr199-codereview-codex-5-4.md:5637:docs/reviews/pr250-codereview-codex-5-4.md:3555:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6302:docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6303:docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6304:docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6305:docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6306:docs/reviews/pr199-codereview-codex-5-4.md:5650:docs/reviews/pr250-codereview-codex-5-4.md:3604:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6307:docs/reviews/pr199-codereview-codex-5-4.md:5651:docs/reviews/pr250-codereview-codex-5-4.md:3605:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6310:docs/reviews/pr199-codereview-codex-5-4.md:5659:docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
6311:docs/reviews/pr199-codereview-codex-5-4.md:5660:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6315:docs/reviews/pr199-codereview-codex-5-4.md:5664:docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
6316:docs/reviews/pr199-codereview-codex-5-4.md:5665:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6317:docs/reviews/pr199-codereview-codex-5-4.md:5666:docs/reviews/pr250-codereview-codex-5-4.md:3889:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6322:docs/reviews/pr199-codereview-codex-5-4.md:5676:docs/reviews/pr250-codereview-codex-5-4.md:4527:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6324:docs/reviews/pr199-codereview-codex-5-4.md:5682:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
6325:docs/reviews/pr199-codereview-codex-5-4.md:5683:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6327:docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6328:docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6331:docs/reviews/pr199-codereview-codex-5-4.md:6426:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6332:docs/reviews/pr199-codereview-codex-5-4.md:6444:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6336:docs/reviews/pr199-codereview-codex-5-4.md:6915:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
6341:docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6342:docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6343:docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6344:docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6345:docs/reviews/pr199-codereview-codex-5-4.md:7524:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6346:docs/reviews/pr199-codereview-codex-5-4.md:7525:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
6349:docs/reviews/pr199-codereview-codex-5-4.md:7664:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
6350:docs/reviews/pr199-codereview-codex-5-4.md:7665:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
6353:docs/reviews/pr199-codereview-codex-5-4.md:9120:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6358:docs/reviews/pr199-codereview-codex-5-4.md:9815:docs/reviews/pr250-codereview-codex-5-4.md:3867:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6359:docs/reviews/pr199-codereview-codex-5-4.md:9816:docs/reviews/pr250-codereview-codex-5-4.md:3869:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6360:docs/reviews/pr199-codereview-codex-5-4.md:9817:docs/reviews/pr250-codereview-codex-5-4.md:3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
6361:docs/reviews/pr199-codereview-codex-5-4.md:9828:docs/reviews/pr250-codereview-codex-5-5.md:3997:docs/reviews/pr250-codereview-codex-5-4.md:3867:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6362:docs/reviews/pr199-codereview-codex-5-4.md:9829:docs/reviews/pr250-codereview-codex-5-5.md:3999:docs/reviews/pr250-codereview-codex-5-4.md:3869:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6363:docs/reviews/pr199-codereview-codex-5-4.md:9830:docs/reviews/pr250-codereview-codex-5-5.md:4001:docs/reviews/pr250-codereview-codex-5-4.md:3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
6364:docs/reviews/pr199-codereview-codex-5-4.md:9831:docs/reviews/pr250-codereview-codex-5-5.md:5643:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
6365:docs/reviews/pr199-codereview-codex-5-4.md:9832:docs/reviews/pr250-codereview-codex-5-5.md:5904:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6366:docs/reviews/pr199-codereview-codex-5-4.md:9833:docs/reviews/pr250-codereview-codex-5-5.md:5906:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6367:docs/reviews/pr199-codereview-codex-5-4.md:9842:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6368:docs/reviews/pr199-codereview-codex-5-4.md:9844:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
6369:docs/reviews/pr199-codereview-codex-5-4.md:9846:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
6375:docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:7:But three real issues land: (1) **cold-damage RNG uses `_world.currentTickSeed` instead of the historical `_tickSeeds[tick]`**, which the rest of the codebase consistently uses for lazy-settle RNG — this breaks lazy-settle determinism and is timing-exploitable; (2) **starvation kill condition has an inverted-fairness guard** that grants pre-winter-starvers immunity from winter starvation kills; (3) **wood burn semantics deviate from spec** (per-clansman 0.5e18 vs spec's per-base 1e18). Plus a handful of MEDIUM/LOW smaller issues. Recommend NOT merging until at least the first two are addressed (or explicitly resolved as design changes with an ADR/spec patch).
6391:docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:143:`ClanWorld.sol:1161-1165`. Iterates all clans, including DEAD ones. Resetting `coldDamage = 0` on a dead clan is harmless (the field is unread for dead clans). Wasted gas at winter-end heartbeat. Add `if (clan.clanState == ClanState.DEAD) continue;`. Same for `_lockWheatPlotsForWinter` and `_restartWheatPlotsAfterWinter` (lines 1132-1158) — locking/unlocking a dead clan's plots writes to slots that will never be read again. Trivial gas cleanup.
6392:docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:146:`ClanWorld.sol:80, 1137, 1149`. Hardcoded for "24 clans × 2 plots" but the project ships with 8 clans (per `docs/planning/V1/07 ... v4.5 ... §3.1`). If anyone ever bumps `MAX_CLANS` past 24 without bumping this constant, the heartbeat that crosses winter will revert and the world freezes. Either make the cap a function of `MAX_CLANS`, or downgrade the `require` to a "cap = process up to N this tick, defer the rest" pattern (mirroring `MAX_MARKET_ACTIONS_PER_TICK`). Since the plot transition needs to be atomic at the boundary, the deferred approach doesn't fit cleanly — at minimum, add an invariant test `assertLe(MAX_CLANS * 2, MAX_CROP_TRANSITION_PER_TICK)`.
6748:docs/reviews/pr198-r2-codereview-codex-5-4.md:7909:packages/contracts/src/ClanWorld.sol:48:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6749:docs/reviews/pr198-r2-codereview-codex-5-4.md:7952:docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6750:docs/reviews/pr198-r2-codereview-codex-5-4.md:7954:docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6751:docs/reviews/pr198-r2-codereview-codex-5-4.md:7963:docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6752:docs/reviews/pr198-r2-codereview-codex-5-4.md:7964:docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6753:docs/reviews/pr198-r2-codereview-codex-5-4.md:7965:docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6754:docs/reviews/pr198-r2-codereview-codex-5-4.md:7966:docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6756:docs/reviews/pr198-r2-codereview-codex-5-4.md:7968:docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6757:docs/reviews/pr198-r2-codereview-codex-5-4.md:7969:docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6758:docs/reviews/pr198-r2-codereview-codex-5-4.md:7970:docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6759:docs/reviews/pr198-r2-codereview-codex-5-4.md:7971:docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6765:docs/reviews/pr198-r2-codereview-codex-5-4.md:7989:docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6766:docs/reviews/pr198-r2-codereview-codex-5-4.md:7990:docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6771:docs/reviews/pr198-r2-codereview-codex-5-4.md:8016:docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6772:docs/reviews/pr198-r2-codereview-codex-5-4.md:8018:docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6773:docs/reviews/pr198-r2-codereview-codex-5-4.md:8026:docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6774:docs/reviews/pr198-r2-codereview-codex-5-4.md:8027:docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6777:docs/reviews/pr198-r2-codereview-codex-5-4.md:8057:docs/reviews/pr199-codereview-codex-5-4.md:9120:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6781:docs/reviews/pr198-r2-codereview-codex-5-4.md:8125:docs/reviews/pr198-r2-codereview-codex-5-5.md:5463:packages/contracts/src/ClanWorld.sol:48:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6783:docs/reviews/pr198-r2-codereview-codex-5-4.md:8671:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6784:docs/reviews/pr198-r2-codereview-codex-5-4.md:8675:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6787:docs/reviews/pr198-r2-codereview-codex-5-4.md:8864:docs/reviews/pr250-codereview-codex-5-4.md:2039:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6788:docs/reviews/pr198-r2-codereview-codex-5-4.md:8866:docs/reviews/pr250-codereview-codex-5-4.md:2535:docs/reviews/pr250-codereview-codex-5-4.md:2039:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6790:docs/reviews/pr198-r2-codereview-codex-5-4.md:8871:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6791:docs/reviews/pr198-r2-codereview-codex-5-4.md:8872:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6792:docs/reviews/pr198-r2-codereview-codex-5-4.md:8874:docs/reviews/pr250-codereview-codex-5-4.md:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
6795:docs/reviews/pr198-r2-codereview-codex-5-4.md:8879:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6796:docs/reviews/pr198-r2-codereview-codex-5-4.md:8880:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6798:docs/reviews/pr198-r2-codereview-codex-5-4.md:8882:docs/reviews/pr250-codereview-codex-5-4.md:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6809:docs/reviews/pr198-r2-codereview-codex-5-4.md:8893:docs/reviews/pr250-codereview-codex-5-4.md:3552:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
6810:docs/reviews/pr198-r2-codereview-codex-5-4.md:8894:docs/reviews/pr250-codereview-codex-5-4.md:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6815:docs/reviews/pr198-r2-codereview-codex-5-4.md:8902:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6816:docs/reviews/pr198-r2-codereview-codex-5-4.md:8903:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6824:docs/reviews/pr198-r2-codereview-codex-5-4.md:8915:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6825:docs/reviews/pr198-r2-codereview-codex-5-4.md:8916:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6827:docs/reviews/pr198-r2-codereview-codex-5-4.md:8918:docs/reviews/pr250-codereview-codex-5-4.md:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6830:docs/reviews/pr198-r2-codereview-codex-5-4.md:8921:docs/reviews/pr250-codereview-codex-5-4.md:3620:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
6834:docs/reviews/pr198-r2-codereview-codex-5-4.md:8925:docs/reviews/pr250-codereview-codex-5-4.md:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6835:docs/reviews/pr198-r2-codereview-codex-5-4.md:8926:docs/reviews/pr250-codereview-codex-5-4.md:3644:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6838:docs/reviews/pr198-r2-codereview-codex-5-4.md:8929:docs/reviews/pr250-codereview-codex-5-4.md:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
6839:docs/reviews/pr198-r2-codereview-codex-5-4.md:8930:docs/reviews/pr250-codereview-codex-5-4.md:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
6840:docs/reviews/pr198-r2-codereview-codex-5-4.md:8931:docs/reviews/pr250-codereview-codex-5-4.md:3872:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
6841:docs/reviews/pr198-r2-codereview-codex-5-4.md:8932:docs/reviews/pr250-codereview-codex-5-4.md:3876:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
6859:docs/reviews/pr198-r2-codereview-codex-5-4.md:8977:docs/reviews/pr250-codereview-codex-5-5.md:2033:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6860:docs/reviews/pr198-r2-codereview-codex-5-4.md:8978:docs/reviews/pr250-codereview-codex-5-5.md:2149:docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6861:docs/reviews/pr198-r2-codereview-codex-5-4.md:8979:docs/reviews/pr250-codereview-codex-5-5.md:2150:docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6862:docs/reviews/pr198-r2-codereview-codex-5-4.md:8980:docs/reviews/pr250-codereview-codex-5-5.md:2158:docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6863:docs/reviews/pr198-r2-codereview-codex-5-4.md:8981:docs/reviews/pr250-codereview-codex-5-5.md:2159:docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6864:docs/reviews/pr198-r2-codereview-codex-5-4.md:8982:docs/reviews/pr250-codereview-codex-5-5.md:2164:docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6865:docs/reviews/pr198-r2-codereview-codex-5-4.md:8983:docs/reviews/pr250-codereview-codex-5-5.md:2165:docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6867:docs/reviews/pr198-r2-codereview-codex-5-4.md:8985:docs/reviews/pr250-codereview-codex-5-5.md:2176:docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6868:docs/reviews/pr198-r2-codereview-codex-5-4.md:8986:docs/reviews/pr250-codereview-codex-5-5.md:2177:docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6869:docs/reviews/pr198-r2-codereview-codex-5-4.md:8987:docs/reviews/pr250-codereview-codex-5-5.md:2178:docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6870:docs/reviews/pr198-r2-codereview-codex-5-4.md:8988:docs/reviews/pr250-codereview-codex-5-5.md:2179:docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6876:docs/reviews/pr198-r2-codereview-codex-5-4.md:8994:docs/reviews/pr250-codereview-codex-5-5.md:2216:docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6877:docs/reviews/pr198-r2-codereview-codex-5-4.md:8995:docs/reviews/pr250-codereview-codex-5-5.md:2217:docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6882:docs/reviews/pr198-r2-codereview-codex-5-4.md:9000:docs/reviews/pr250-codereview-codex-5-5.md:2237:docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6883:docs/reviews/pr198-r2-codereview-codex-5-4.md:9001:docs/reviews/pr250-codereview-codex-5-5.md:2238:docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6884:docs/reviews/pr198-r2-codereview-codex-5-4.md:9002:docs/reviews/pr250-codereview-codex-5-5.md:2239:docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6885:docs/reviews/pr198-r2-codereview-codex-5-4.md:9003:docs/reviews/pr250-codereview-codex-5-5.md:2240:docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6888:docs/reviews/pr198-r2-codereview-codex-5-4.md:9006:docs/reviews/pr250-codereview-codex-5-5.md:2259:docs/reviews/pr199-codereview-codex-5-4.md:9120:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6898:docs/reviews/pr198-r2-codereview-codex-5-4.md:9016:docs/reviews/pr250-codereview-codex-5-5.md:2647:docs/reviews/pr250-codereview-codex-5-4.md:2039:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6899:docs/reviews/pr198-r2-codereview-codex-5-4.md:9017:docs/reviews/pr250-codereview-codex-5-5.md:2958:docs/reviews/pr250-codereview-codex-5-4.md:2535:docs/reviews/pr250-codereview-codex-5-4.md:2039:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6901:docs/reviews/pr198-r2-codereview-codex-5-4.md:9019:docs/reviews/pr250-codereview-codex-5-5.md:3181:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6902:docs/reviews/pr198-r2-codereview-codex-5-4.md:9020:docs/reviews/pr250-codereview-codex-5-5.md:3182:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6903:docs/reviews/pr198-r2-codereview-codex-5-4.md:9021:docs/reviews/pr250-codereview-codex-5-5.md:3498:docs/reviews/pr250-codereview-codex-5-4.md:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
6906:docs/reviews/pr198-r2-codereview-codex-5-4.md:9024:docs/reviews/pr250-codereview-codex-5-5.md:3554:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6907:docs/reviews/pr198-r2-codereview-codex-5-4.md:9025:docs/reviews/pr250-codereview-codex-5-5.md:3556:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6909:docs/reviews/pr198-r2-codereview-codex-5-4.md:9027:docs/reviews/pr250-codereview-codex-5-5.md:3667:docs/reviews/pr250-codereview-codex-5-4.md:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6920:docs/reviews/pr198-r2-codereview-codex-5-4.md:9038:docs/reviews/pr250-codereview-codex-5-5.md:3752:docs/reviews/pr250-codereview-codex-5-4.md:3552:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
6921:docs/reviews/pr198-r2-codereview-codex-5-4.md:9039:docs/reviews/pr250-codereview-codex-5-5.md:3753:docs/reviews/pr250-codereview-codex-5-4.md:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6926:docs/reviews/pr198-r2-codereview-codex-5-4.md:9044:docs/reviews/pr250-codereview-codex-5-5.md:3775:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6927:docs/reviews/pr198-r2-codereview-codex-5-4.md:9045:docs/reviews/pr250-codereview-codex-5-5.md:3776:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6935:docs/reviews/pr198-r2-codereview-codex-5-4.md:9053:docs/reviews/pr250-codereview-codex-5-5.md:3788:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6936:docs/reviews/pr198-r2-codereview-codex-5-4.md:9054:docs/reviews/pr250-codereview-codex-5-5.md:3789:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6938:docs/reviews/pr198-r2-codereview-codex-5-4.md:9056:docs/reviews/pr250-codereview-codex-5-5.md:3793:docs/reviews/pr250-codereview-codex-5-4.md:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6941:docs/reviews/pr198-r2-codereview-codex-5-4.md:9059:docs/reviews/pr250-codereview-codex-5-5.md:3803:docs/reviews/pr250-codereview-codex-5-4.md:3620:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
6945:docs/reviews/pr198-r2-codereview-codex-5-4.md:9063:docs/reviews/pr250-codereview-codex-5-5.md:3815:docs/reviews/pr250-codereview-codex-5-4.md:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6946:docs/reviews/pr198-r2-codereview-codex-5-4.md:9064:docs/reviews/pr250-codereview-codex-5-5.md:3825:docs/reviews/pr250-codereview-codex-5-4.md:3644:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
6949:docs/reviews/pr198-r2-codereview-codex-5-4.md:9067:docs/reviews/pr250-codereview-codex-5-5.md:3996:docs/reviews/pr250-codereview-codex-5-4.md:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
6950:docs/reviews/pr198-r2-codereview-codex-5-4.md:9068:docs/reviews/pr250-codereview-codex-5-5.md:3998:docs/reviews/pr250-codereview-codex-5-4.md:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
6951:docs/reviews/pr198-r2-codereview-codex-5-4.md:9069:docs/reviews/pr250-codereview-codex-5-5.md:4002:docs/reviews/pr250-codereview-codex-5-4.md:3872:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
6952:docs/reviews/pr198-r2-codereview-codex-5-4.md:9070:docs/reviews/pr250-codereview-codex-5-5.md:4006:docs/reviews/pr250-codereview-codex-5-4.md:3876:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
6984:docs/reviews/pr198-r2-codereview-codex-5-4.md:9102:docs/reviews/pr250-codereview-codex-5-5.md:5294:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6985:docs/reviews/pr198-r2-codereview-codex-5-4.md:9103:docs/reviews/pr250-codereview-codex-5-5.md:5295:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
6986:docs/reviews/pr198-r2-codereview-codex-5-4.md:9104:docs/reviews/pr250-codereview-codex-5-5.md:5297:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
6987:docs/reviews/pr198-r2-codereview-codex-5-4.md:9105:docs/reviews/pr250-codereview-codex-5-5.md:5382:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
6990:docs/reviews/pr198-r2-codereview-codex-5-4.md:9108:docs/reviews/pr250-codereview-codex-5-5.md:5442:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6991:docs/reviews/pr198-r2-codereview-codex-5-4.md:9109:docs/reviews/pr250-codereview-codex-5-5.md:5444:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
6994:docs/reviews/pr198-r2-codereview-codex-5-4.md:9112:docs/reviews/pr250-codereview-codex-5-5.md:5644:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
6995:docs/reviews/pr198-r2-codereview-codex-5-4.md:9113:docs/reviews/pr250-codereview-codex-5-5.md:5648:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6999:docs/reviews/pr198-r2-codereview-codex-5-4.md:9117:docs/reviews/pr250-codereview-codex-5-5.md:5903:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7000:docs/reviews/pr198-r2-codereview-codex-5-4.md:9118:docs/reviews/pr250-codereview-codex-5-5.md:5905:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7002:docs/reviews/pr198-r2-codereview-codex-5-4.md:9120:docs/reviews/pr250-codereview-codex-5-5.md:5919:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
7003:docs/reviews/pr198-r2-codereview-codex-5-4.md:9121:docs/reviews/pr250-codereview-codex-5-5.md:5920:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7008:docs/reviews/pr198-r2-codereview-codex-5-4.md:9126:docs/reviews/pr250-codereview-codex-5-5.md:5945:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7009:docs/reviews/pr198-r2-codereview-codex-5-4.md:9127:docs/reviews/pr250-codereview-codex-5-5.md:5946:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7017:docs/reviews/pr198-r2-codereview-codex-5-4.md:9135:docs/reviews/pr250-codereview-codex-5-5.md:5959:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7018:docs/reviews/pr198-r2-codereview-codex-5-4.md:9136:docs/reviews/pr250-codereview-codex-5-5.md:5960:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7020:docs/reviews/pr198-r2-codereview-codex-5-4.md:9138:docs/reviews/pr250-codereview-codex-5-5.md:5967:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7023:docs/reviews/pr198-r2-codereview-codex-5-4.md:9141:docs/reviews/pr250-codereview-codex-5-5.md:5980:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
7027:docs/reviews/pr198-r2-codereview-codex-5-4.md:9145:docs/reviews/pr250-codereview-codex-5-5.md:5996:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7033:docs/reviews/pr198-r2-codereview-codex-5-4.md:9158:docs/reviews/pr250-codereview-codex-5-5.md:7147:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7034:docs/reviews/pr198-r2-codereview-codex-5-4.md:9159:docs/reviews/pr250-codereview-codex-5-5.md:7148:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7035:docs/reviews/pr198-r2-codereview-codex-5-4.md:9160:docs/reviews/pr250-codereview-codex-5-5.md:7153:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
7036:docs/reviews/pr198-r2-codereview-codex-5-4.md:9161:docs/reviews/pr250-codereview-codex-5-5.md:7154:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7037:docs/reviews/pr198-r2-codereview-codex-5-4.md:9162:docs/reviews/pr250-codereview-codex-5-5.md:7155:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7038:docs/reviews/pr198-r2-codereview-codex-5-4.md:9163:docs/reviews/pr250-codereview-codex-5-5.md:7156:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7039:docs/reviews/pr198-r2-codereview-codex-5-4.md:9164:docs/reviews/pr250-codereview-codex-5-5.md:7158:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7040:docs/reviews/pr198-r2-codereview-codex-5-4.md:9165:docs/reviews/pr250-codereview-codex-5-5.md:7159:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7041:docs/reviews/pr198-r2-codereview-codex-5-4.md:9166:docs/reviews/pr250-codereview-codex-5-5.md:7160:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7042:docs/reviews/pr198-r2-codereview-codex-5-4.md:9167:docs/reviews/pr250-codereview-codex-5-5.md:7161:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7043:docs/reviews/pr198-r2-codereview-codex-5-4.md:9168:docs/reviews/pr250-codereview-codex-5-5.md:7162:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7044:docs/reviews/pr198-r2-codereview-codex-5-4.md:9169:docs/reviews/pr250-codereview-codex-5-5.md:7163:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7045:docs/reviews/pr198-r2-codereview-codex-5-4.md:9170:docs/reviews/pr250-codereview-codex-5-5.md:7166:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7046:docs/reviews/pr198-r2-codereview-codex-5-4.md:9171:docs/reviews/pr250-codereview-codex-5-5.md:7167:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7047:docs/reviews/pr198-r2-codereview-codex-5-4.md:9172:docs/reviews/pr250-codereview-codex-5-5.md:7168:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7048:docs/reviews/pr198-r2-codereview-codex-5-4.md:9173:docs/reviews/pr250-codereview-codex-5-5.md:7188:2149:docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7049:docs/reviews/pr198-r2-codereview-codex-5-4.md:9174:docs/reviews/pr250-codereview-codex-5-5.md:7189:2150:docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7050:docs/reviews/pr198-r2-codereview-codex-5-4.md:9175:docs/reviews/pr250-codereview-codex-5-5.md:7190:2158:docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7051:docs/reviews/pr198-r2-codereview-codex-5-4.md:9176:docs/reviews/pr250-codereview-codex-5-5.md:7191:2159:docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7052:docs/reviews/pr198-r2-codereview-codex-5-4.md:9177:docs/reviews/pr250-codereview-codex-5-5.md:7192:2164:docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7053:docs/reviews/pr198-r2-codereview-codex-5-4.md:9178:docs/reviews/pr250-codereview-codex-5-5.md:7193:2165:docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7054:docs/reviews/pr198-r2-codereview-codex-5-4.md:9179:docs/reviews/pr250-codereview-codex-5-5.md:7194:2176:docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7055:docs/reviews/pr198-r2-codereview-codex-5-4.md:9180:docs/reviews/pr250-codereview-codex-5-5.md:7195:2177:docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7056:docs/reviews/pr198-r2-codereview-codex-5-4.md:9181:docs/reviews/pr250-codereview-codex-5-5.md:7196:2178:docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7057:docs/reviews/pr198-r2-codereview-codex-5-4.md:9182:docs/reviews/pr250-codereview-codex-5-5.md:7197:2179:docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7058:docs/reviews/pr198-r2-codereview-codex-5-4.md:9183:docs/reviews/pr250-codereview-codex-5-5.md:7204:2216:docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7059:docs/reviews/pr198-r2-codereview-codex-5-4.md:9184:docs/reviews/pr250-codereview-codex-5-5.md:7205:2217:docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7060:docs/reviews/pr198-r2-codereview-codex-5-4.md:9185:docs/reviews/pr250-codereview-codex-5-5.md:7206:2237:docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7061:docs/reviews/pr198-r2-codereview-codex-5-4.md:9186:docs/reviews/pr250-codereview-codex-5-5.md:7207:2238:docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7062:docs/reviews/pr198-r2-codereview-codex-5-4.md:9187:docs/reviews/pr250-codereview-codex-5-5.md:7208:2239:docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7063:docs/reviews/pr198-r2-codereview-codex-5-4.md:9188:docs/reviews/pr250-codereview-codex-5-5.md:7209:2240:docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7064:docs/reviews/pr198-r2-codereview-codex-5-4.md:9189:docs/reviews/pr250-codereview-codex-5-5.md:7213:3181:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7065:docs/reviews/pr198-r2-codereview-codex-5-4.md:9190:docs/reviews/pr250-codereview-codex-5-5.md:7214:3182:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7066:docs/reviews/pr198-r2-codereview-codex-5-4.md:9191:docs/reviews/pr250-codereview-codex-5-5.md:7216:3498:docs/reviews/pr250-codereview-codex-5-4.md:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
7067:docs/reviews/pr198-r2-codereview-codex-5-4.md:9192:docs/reviews/pr250-codereview-codex-5-5.md:7217:3554:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7068:docs/reviews/pr198-r2-codereview-codex-5-4.md:9193:docs/reviews/pr250-codereview-codex-5-5.md:7218:3556:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7069:docs/reviews/pr198-r2-codereview-codex-5-4.md:9194:docs/reviews/pr250-codereview-codex-5-5.md:7219:3667:docs/reviews/pr250-codereview-codex-5-4.md:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7070:docs/reviews/pr198-r2-codereview-codex-5-4.md:9195:docs/reviews/pr250-codereview-codex-5-5.md:7221:3753:docs/reviews/pr250-codereview-codex-5-4.md:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7071:docs/reviews/pr198-r2-codereview-codex-5-4.md:9196:docs/reviews/pr250-codereview-codex-5-5.md:7222:3775:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7072:docs/reviews/pr198-r2-codereview-codex-5-4.md:9197:docs/reviews/pr250-codereview-codex-5-5.md:7223:3776:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7073:docs/reviews/pr198-r2-codereview-codex-5-4.md:9198:docs/reviews/pr250-codereview-codex-5-5.md:7224:3788:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7074:docs/reviews/pr198-r2-codereview-codex-5-4.md:9199:docs/reviews/pr250-codereview-codex-5-5.md:7225:3789:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7075:docs/reviews/pr198-r2-codereview-codex-5-4.md:9200:docs/reviews/pr250-codereview-codex-5-5.md:7226:3793:docs/reviews/pr250-codereview-codex-5-4.md:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7076:docs/reviews/pr198-r2-codereview-codex-5-4.md:9201:docs/reviews/pr250-codereview-codex-5-5.md:7229:3815:docs/reviews/pr250-codereview-codex-5-4.md:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7077:docs/reviews/pr198-r2-codereview-codex-5-4.md:9202:docs/reviews/pr250-codereview-codex-5-5.md:7230:3996:docs/reviews/pr250-codereview-codex-5-4.md:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7078:docs/reviews/pr198-r2-codereview-codex-5-4.md:9203:docs/reviews/pr250-codereview-codex-5-5.md:7231:3998:docs/reviews/pr250-codereview-codex-5-4.md:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7079:docs/reviews/pr198-r2-codereview-codex-5-4.md:9204:docs/reviews/pr250-codereview-codex-5-5.md:7237:5294:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7080:docs/reviews/pr198-r2-codereview-codex-5-4.md:9205:docs/reviews/pr250-codereview-codex-5-5.md:7238:5295:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7081:docs/reviews/pr198-r2-codereview-codex-5-4.md:9206:docs/reviews/pr250-codereview-codex-5-5.md:7239:5382:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
7082:docs/reviews/pr198-r2-codereview-codex-5-4.md:9207:docs/reviews/pr250-codereview-codex-5-5.md:7240:5442:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7083:docs/reviews/pr198-r2-codereview-codex-5-4.md:9208:docs/reviews/pr250-codereview-codex-5-5.md:7241:5444:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7084:docs/reviews/pr198-r2-codereview-codex-5-4.md:9209:docs/reviews/pr250-codereview-codex-5-5.md:7246:5648:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7085:docs/reviews/pr198-r2-codereview-codex-5-4.md:9210:docs/reviews/pr250-codereview-codex-5-5.md:7247:5903:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7086:docs/reviews/pr198-r2-codereview-codex-5-4.md:9211:docs/reviews/pr250-codereview-codex-5-5.md:7248:5905:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7087:docs/reviews/pr198-r2-codereview-codex-5-4.md:9212:docs/reviews/pr250-codereview-codex-5-5.md:7249:5920:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7088:docs/reviews/pr198-r2-codereview-codex-5-4.md:9213:docs/reviews/pr250-codereview-codex-5-5.md:7250:5945:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7089:docs/reviews/pr198-r2-codereview-codex-5-4.md:9214:docs/reviews/pr250-codereview-codex-5-5.md:7251:5946:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7090:docs/reviews/pr198-r2-codereview-codex-5-4.md:9215:docs/reviews/pr250-codereview-codex-5-5.md:7252:5959:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7091:docs/reviews/pr198-r2-codereview-codex-5-4.md:9216:docs/reviews/pr250-codereview-codex-5-5.md:7253:5960:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7092:docs/reviews/pr198-r2-codereview-codex-5-4.md:9217:docs/reviews/pr250-codereview-codex-5-5.md:7254:5967:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7093:docs/reviews/pr198-r2-codereview-codex-5-4.md:9218:docs/reviews/pr250-codereview-codex-5-5.md:7257:5996:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7108:docs/reviews/pr198-r2-codereview-codex-5-4.md:9661:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
7111:docs/reviews/pr198-r2-codereview-codex-5-4.md:9949:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7112:docs/reviews/pr198-r2-codereview-codex-5-4.md:9951:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7115:docs/reviews/pr198-r2-codereview-codex-5-4.md:10662:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
7116:docs/reviews/pr198-r2-codereview-codex-5-4.md:10664:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7121:docs/reviews/pr198-r2-codereview-codex-5-4.md:10997:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7122:docs/reviews/pr198-r2-codereview-codex-5-4.md:10999:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
7130:docs/reviews/pr198-r2-codereview-codex-5-4.md:11029:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7131:docs/reviews/pr198-r2-codereview-codex-5-4.md:11031:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
7133:docs/reviews/pr198-r2-codereview-codex-5-4.md:11059:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7136:docs/reviews/pr198-r2-codereview-codex-5-4.md:11069:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
7140:docs/reviews/pr198-r2-codereview-codex-5-4.md:11090:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7141:docs/reviews/pr198-r2-codereview-codex-5-4.md:11162:docs/reviews/pr199-codereview-codex-5-5.md:12345:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7142:docs/reviews/pr198-r2-codereview-codex-5-4.md:11183:docs/reviews/pr199-codereview-codex-5-5.md:12526:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7143:docs/reviews/pr198-r2-codereview-codex-5-4.md:11185:docs/reviews/pr199-codereview-codex-5-5.md:12582:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
7144:docs/reviews/pr198-r2-codereview-codex-5-4.md:11204:docs/reviews/pr199-codereview-codex-5-5.md:12910:docs/reviews/pr199-codereview-codex-5-4.md:9120:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
7145:docs/reviews/pr198-r2-codereview-codex-5-4.md:11209:docs/reviews/pr199-codereview-codex-5-5.md:13126:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
7148:docs/reviews/pr198-r2-codereview-codex-5-4.md:12821:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7149:docs/reviews/pr198-r2-codereview-codex-5-4.md:12827:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
7150:docs/reviews/pr198-r2-codereview-codex-5-4.md:12847:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
7154:docs/reviews/pr198-r2-codereview-codex-5-4.md:12906:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
7335:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4380:docs/reviews/pr250-codereview-codex-5-4.md:4516:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:198:    bool   winterActive;
7336:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4381:docs/reviews/pr250-codereview-codex-5-4.md:4517:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:199:    uint64 winterStartsAtTick;
7337:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4382:docs/reviews/pr250-codereview-codex-5-4.md:4518:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:200:    uint64 winterEndsAtTick;     // 0 if not active
7338:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4383:docs/reviews/pr250-codereview-codex-5-4.md:4519:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:237:    uint64 starvationStartsAtTick; // 0 = none
7339:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4384:docs/reviews/pr250-codereview-codex-5-4.md:4520:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:239:    uint16 coldDamage;             // resets to 0 at winter end
7340:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4385:docs/reviews/pr250-codereview-codex-5-4.md:4521:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:407:    bool   winterActive;
7341:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4386:docs/reviews/pr250-codereview-codex-5-4.md:4522:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:408:    uint64 winterStartsAtTick;
7342:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4387:docs/reviews/pr250-codereview-codex-5-4.md:4523:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:409:    uint64 winterEndsAtTick;
7343:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4388:docs/reviews/pr250-codereview-codex-5-4.md:4524:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:630:    // ----- winter cold damage -----
7344:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4389:docs/reviews/pr250-codereview-codex-5-4.md:4527:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
7804:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4974:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
7806:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4976:docs/reviews/pr250-codereview-codex-5-5.md:1751:packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
7807:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4977:docs/reviews/pr250-codereview-codex-5-5.md:1752:packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
7808:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4979:docs/reviews/pr250-codereview-codex-5-5.md:1754:packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
7809:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4980:docs/reviews/pr250-codereview-codex-5-5.md:1758:packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
7810:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4981:docs/reviews/pr250-codereview-codex-5-5.md:1759:packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
7811:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4982:docs/reviews/pr250-codereview-codex-5-5.md:1760:packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
7812:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4983:docs/reviews/pr250-codereview-codex-5-5.md:1761:packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
7813:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4984:docs/reviews/pr250-codereview-codex-5-5.md:1764:packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
7814:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4985:docs/reviews/pr250-codereview-codex-5-5.md:1765:packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
7815:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4986:docs/reviews/pr250-codereview-codex-5-5.md:1766:packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
7816:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4987:docs/reviews/pr250-codereview-codex-5-5.md:1767:packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
7817:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4988:docs/reviews/pr250-codereview-codex-5-5.md:1775:packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
7820:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4994:docs/reviews/pr250-codereview-codex-5-5.md:1790:packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
7821:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4995:docs/reviews/pr250-codereview-codex-5-5.md:1792:packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
7822:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4996:docs/reviews/pr250-codereview-codex-5-5.md:1795:packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
7855:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5054:docs/reviews/pr200-codereview-codex-5-5.md:5076:/bin/bash -lc 'rg -n "DEAD|clanState|starvation|dead|ClanState.DEAD|propose.*Transfer|accept.*Transfer|zero|expiry" packages/contracts/test packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
7856:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5057:docs/reviews/pr200-codereview-codex-5-5.md:5082:packages/contracts/src/ClanWorld.sol:397:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
7857:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5058:docs/reviews/pr200-codereview-codex-5-5.md:5083:packages/contracts/src/ClanWorld.sol:419:        if (starving && clan.starvationStartsAtTick == 0) {
7858:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5059:docs/reviews/pr200-codereview-codex-5-5.md:5084:packages/contracts/src/ClanWorld.sol:420:            clan.starvationStartsAtTick = tick;
7859:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5060:docs/reviews/pr200-codereview-codex-5-5.md:5085:packages/contracts/src/ClanWorld.sol:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
7860:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5061:docs/reviews/pr200-codereview-codex-5-5.md:5086:packages/contracts/src/ClanWorld.sol:423:            clan.starvationStartsAtTick = 0;
7861:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5062:docs/reviews/pr200-codereview-codex-5-5.md:5087:packages/contracts/src/ClanWorld.sol:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
7862:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5063:docs/reviews/pr200-codereview-codex-5-5.md:5094:packages/contracts/src/ClanWorld.sol:1033:        clan.starvationStartsAtTick = 0;
7863:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5064:docs/reviews/pr200-codereview-codex-5-5.md:5100:packages/contracts/src/ClanWorld.sol:2297:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
7864:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5065:docs/reviews/pr200-codereview-codex-5-5.md:5101:packages/contracts/src/ClanWorld.sol:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
7865:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5066:docs/reviews/pr200-codereview-codex-5-5.md:5103:packages/contracts/src/ClanWorld.sol:2401:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
7866:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5067:docs/reviews/pr200-codereview-codex-5-5.md:5104:packages/contracts/src/ClanWorld.sol:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
7980:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5213:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
8032:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5268:docs/reviews/pr194-r2-codereview-codex-5-5.md:7580:packages/contracts/src/ClanWorld.sol:138:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
8033:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5269:docs/reviews/pr194-r2-codereview-codex-5-5.md:7581:packages/contracts/src/ClanWorld.sol:140:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
8034:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5270:docs/reviews/pr194-r2-codereview-codex-5-5.md:7582:packages/contracts/src/ClanWorld.sol:141:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
8035:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5273:docs/reviews/pr194-r2-codereview-codex-5-5.md:7588:packages/contracts/src/ClanWorld.sol:484:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
8036:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5274:docs/reviews/pr194-r2-codereview-codex-5-5.md:7589:packages/contracts/src/ClanWorld.sol:486:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
8037:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5275:docs/reviews/pr194-r2-codereview-codex-5-5.md:7610:packages/contracts/src/ClanWorld.sol:2383:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
8038:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5276:docs/reviews/pr194-r2-codereview-codex-5-5.md:7612:packages/contracts/src/ClanWorld.sol:2385:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
8039:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5277:docs/reviews/pr194-r2-codereview-codex-5-5.md:7613:packages/contracts/src/ClanWorld.sol:2400:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
8040:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5278:docs/reviews/pr194-r2-codereview-codex-5-5.md:7615:packages/contracts/src/ClanWorld.sol:2402:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
8054:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5294:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8055:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5295:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8056:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5297:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
8136:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5382:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
8192:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5442:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8194:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5444:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8223:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5548:docs/reviews/pr200-codereview-codex-5-4.md:6413:/bin/bash -lc "rg -n \"_settleClan\\(|submitClanOrders\\(|clan.lastSettledTick|starvationStartsAtTick|_resolveWorldEvents|_killClan|clanState = ClanState.DEAD|clanState "'!= ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
8299:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5642:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8300:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5643:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
8301:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5644:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
8302:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5645:docs/reviews/r2/pr194-synthesis.md:35:| M3 | `ClanWorld.sol:1206` (`_simulateResolveAction`) | 4.6 = **1/5** | MED | **Starvation check uses `_world.currentTick` instead of simulation tick.** Latent bug — current control flow prevents misalignment, but breaks if simulation structure changes. Use `sim.clan.starvationStartsAtTick <= tick`. |
8303:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5646:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8304:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5647:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8305:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5648:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8415:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5763:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
8416:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5765:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
8547:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5903:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8548:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5904:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
8549:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5905:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8550:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5906:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
8556:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5917:docs/reviews/pr199-codereview-codex-5-5.md:3027:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
8558:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5919:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
8559:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5920:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8561:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5922:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
8579:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5945:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8580:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5946:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8593:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5959:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8594:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5960:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8595:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5962:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
8596:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5964:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
8598:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5967:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8604:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5980:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
8605:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5981:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8606:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5982:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8618:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5996:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8802:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7147:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8803:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7148:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8804:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7152:3128:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
8805:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7153:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
8806:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7154:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8807:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7155:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8808:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7156:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8809:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7157:3504:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8810:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7158:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8811:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7159:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8812:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7160:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8813:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7161:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8814:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7162:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8815:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7163:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8816:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7164:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8817:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7165:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8818:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7166:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8819:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7167:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8820:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7168:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8821:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7169:3870:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8822:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7170:3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
8823:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7171:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8824:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7172:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8825:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7187:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
8826:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7188:2149:docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8827:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7189:2150:docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8828:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7190:2158:docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8829:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7191:2159:docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8830:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7192:2164:docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8831:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7193:2165:docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8832:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7194:2176:docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8833:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7195:2177:docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8834:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7196:2178:docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8835:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7197:2179:docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8836:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7198:2188:docs/reviews/pr199-codereview-codex-5-4.md:5659:docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8837:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7199:2189:docs/reviews/pr199-codereview-codex-5-4.md:5660:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8838:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7200:2193:docs/reviews/pr199-codereview-codex-5-4.md:5664:docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8839:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7201:2194:docs/reviews/pr199-codereview-codex-5-4.md:5665:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8840:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7202:2206:docs/reviews/pr199-codereview-codex-5-4.md:5682:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8841:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7203:2207:docs/reviews/pr199-codereview-codex-5-4.md:5683:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8842:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7204:2216:docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8843:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7205:2217:docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8844:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7206:2237:docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8845:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7207:2238:docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8846:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7208:2239:docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8847:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7209:2240:docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8848:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7210:2250:docs/reviews/pr199-codereview-codex-5-4.md:7664:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8849:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7211:2251:docs/reviews/pr199-codereview-codex-5-4.md:7665:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8850:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7212:2331:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8851:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7213:3181:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8852:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7214:3182:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8853:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7215:3418:docs/reviews/pr250-codereview-codex-5-4.md:3128:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
8854:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7216:3498:docs/reviews/pr250-codereview-codex-5-4.md:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
8855:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7217:3554:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8856:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7218:3556:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8857:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7219:3667:docs/reviews/pr250-codereview-codex-5-4.md:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8858:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7220:3710:docs/reviews/pr250-codereview-codex-5-4.md:3504:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8859:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7221:3753:docs/reviews/pr250-codereview-codex-5-4.md:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8860:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7222:3775:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8861:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7223:3776:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8862:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7224:3788:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8863:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7225:3789:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8864:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7226:3793:docs/reviews/pr250-codereview-codex-5-4.md:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8865:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7227:3804:docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8866:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7228:3805:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8867:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7229:3815:docs/reviews/pr250-codereview-codex-5-4.md:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8868:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7230:3996:docs/reviews/pr250-codereview-codex-5-4.md:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8869:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7231:3998:docs/reviews/pr250-codereview-codex-5-4.md:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8870:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7232:4000:docs/reviews/pr250-codereview-codex-5-4.md:3870:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8871:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7233:4001:docs/reviews/pr250-codereview-codex-5-4.md:3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
8872:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7234:4004:docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8873:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7235:4005:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8874:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7236:4974:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
8875:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7237:5294:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8876:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7238:5295:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8877:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7239:5382:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
8878:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7240:5442:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8879:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7241:5444:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8880:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7242:5642:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
8881:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7243:5643:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
8882:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7244:5646:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8883:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7245:5647:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8884:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7246:5648:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8885:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7247:5903:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8886:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7248:5905:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
8887:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7249:5920:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8888:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7250:5945:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8889:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7251:5946:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
8890:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7252:5959:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8891:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7253:5960:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
8892:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7254:5967:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8893:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7255:5981:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
8894:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7256:5982:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
8895:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7257:5996:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
8922:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7677:`packages/contracts/src/ClanWorld.sol::_applyUpkeep`, `_killNextClansmanFromStarvation` — Starvation directly kills clansmen during winter, and can kill on the same tick starvation is first detected. Spec §4.12/A10 says starvation is a debuff path, while deprivation deaths happen through winter cold damage / wall collapse. If this is intentional, the spec needs to change; as written, this is a behavior bug.
8923:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7683:`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.
8927:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7716:`packages/contracts/src/ClanWorld.sol::_applyUpkeep`, `_killNextClansmanFromStarvation` — Starvation directly kills clansmen during winter, and can kill on the same tick starvation is first detected. Spec §4.12/A10 says starvation is a debuff path, while deprivation deaths happen through winter cold damage / wall collapse. If this is intentional, the spec needs to change; as written, this is a behavior bug.
8928:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7722:`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.
9011:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
9069:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
9071:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
9182:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
9234:docs/reviews/pr194-r2-codereview-codex-5-5.md:7580:packages/contracts/src/ClanWorld.sol:138:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
9235:docs/reviews/pr194-r2-codereview-codex-5-5.md:7581:packages/contracts/src/ClanWorld.sol:140:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
9236:docs/reviews/pr194-r2-codereview-codex-5-5.md:7582:packages/contracts/src/ClanWorld.sol:141:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
9237:docs/reviews/pr194-r2-codereview-codex-5-5.md:7588:packages/contracts/src/ClanWorld.sol:484:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
9238:docs/reviews/pr194-r2-codereview-codex-5-5.md:7589:packages/contracts/src/ClanWorld.sol:486:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
9239:docs/reviews/pr194-r2-codereview-codex-5-5.md:7610:packages/contracts/src/ClanWorld.sol:2383:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
9240:docs/reviews/pr194-r2-codereview-codex-5-5.md:7612:packages/contracts/src/ClanWorld.sol:2385:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
9241:docs/reviews/pr194-r2-codereview-codex-5-5.md:7613:packages/contracts/src/ClanWorld.sol:2400:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
9242:docs/reviews/pr194-r2-codereview-codex-5-5.md:7615:packages/contracts/src/ClanWorld.sol:2402:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
9256:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
9257:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
9302:docs/reviews/pr200-r2-codereview-codex-5-5.md:7363:/bin/bash -lc 'git show 2bec876:packages/contracts/src/ClanWorld.sol | rg -n "clanState = ClanState.DEAD|livingClansmen|starvation|coldDamage|_applyUpkeep|DEAD" -C 2' in /home/claude/code/clan-world
9328:docs/reviews/pr250-r2-codereview-opus-4-7.md:7:The four targeted fixes (H2 starvation, H3 wood burn, H4 uint64 events, H5 wheat plot clear) are correctly implemented and exercised by 15 new tests, all passing. Spec §7.3 was patched as required. **However, the fix-round introduces a new HIGH-severity correctness bug**: the heartbeat-eager `_resetColdDamageForAllClans()` call at winter-end destroys the mid-winter `coldDamage` value for any clan whose `lastSettledTick` is inside the winter window, causing the lazy re-settle to under-count cold consequences (wall degradations / clansman deaths). I reproduced it locally — a clan settled mid-winter with `coldDamage=3` ends winter with `wallLevel=6` instead of `5` for the otherwise-identical never-settled clan. The fix is a one-liner: drop the eager reset; the in-loop / post-loop checks in `_settleClan` already handle the boundary correctly.
9329:docs/reviews/pr250-r2-codereview-opus-4-7.md:13:| H2 | Starvation guard `< tick` (not `<=`) + defer to tick+1 | ✓ | ClanWorld.sol:440 (`starvationStartsAtTick < tick`); `test_starvation_kill_deferred_to_next_tick`, `test_pre_winter_starver_dies_in_winter_at_same_cadence` |
9330:docs/reviews/pr250-r2-codereview-opus-4-7.md:14:| H3 | Wood burn = base + per-clansman | ✓ | ClanWorld.sol:446-447; `test_winter_upkeep_doublesFoodAndBurnsWood` (4-clansman = 3e18 ✓) |
9331:docs/reviews/pr250-r2-codereview-opus-4-7.md:15:| H4 | WinterStarted/Ended back to uint64 | ✓ | IClanWorld.sol:498-499 (`uint64 indexed tick`); `_winterEventTick` returns uint64 |
9405:docs/reviews/pr199-r3-codereview-codex-5-5.md:8486:./docs/reviews/pr199-r3-codereview-codex-5-4.md:8360:docs/reviews/pr199-codereview-opus-4-7.md:7:**Verdict: NEEDS FIXES — do not merge as-is.** Phase 8.1–8.4 ship the correct shape (queue-time vault deduction, mission-nonce-bound reservations, score/rank getters), but four HIGH-severity bugs land with this PR — three of which produce permanent, silent resource loss in well-reachable scenarios (cold death, starvation, BuildWall+UpgradeWall race, level-binding mismatch on cancellation). One MEDIUM-severity bug re-violates the #261 derived-getter contract that #230 was supposed to enforce. The score formula bit-packing and `getRankings` sort are correct and well-designed; the bugs are entirely in the reservation/refund lifecycle and in the choice to read raw committed storage from a documented "settled"-named getter. Cost tables match v4 spec §8 line-for-line. Durations diverge from spec ("single-tick") to 2/2/4 — likely intentional but unannounced.
9406:docs/reviews/pr199-r3-codereview-codex-5-5.md:8606:./docs/reviews/pr199-r3-codereview-codex-5-4.md:9244:docs/reviews/pr198-r2-codereview-codex-5-5.md:9033:docs/reviews/pr199-codereview-opus-4-7.md:7:**Verdict: NEEDS FIXES — do not merge as-is.** Phase 8.1–8.4 ship the correct shape (queue-time vault deduction, mission-nonce-bound reservations, score/rank getters), but four HIGH-severity bugs land with this PR — three of which produce permanent, silent resource loss in well-reachable scenarios (cold death, starvation, BuildWall+UpgradeWall race, level-binding mismatch on cancellation). One MEDIUM-severity bug re-violates the #261 derived-getter contract that #230 was supposed to enforce. The score formula bit-packing and `getRankings` sort are correct and well-designed; the bugs are entirely in the reservation/refund lifecycle and in the choice to read raw committed storage from a documented "settled"-named getter. Cost tables match v4 spec §8 line-for-line. Durations diverge from spec ("single-tick") to 2/2/4 — likely intentional but unannounced.
9408:docs/reviews/pr199-r3-codereview-codex-5-5.md:8799:./docs/reviews/pr199-codereview-opus-4-7.md:7:**Verdict: NEEDS FIXES — do not merge as-is.** Phase 8.1–8.4 ship the correct shape (queue-time vault deduction, mission-nonce-bound reservations, score/rank getters), but four HIGH-severity bugs land with this PR — three of which produce permanent, silent resource loss in well-reachable scenarios (cold death, starvation, BuildWall+UpgradeWall race, level-binding mismatch on cancellation). One MEDIUM-severity bug re-violates the #261 derived-getter contract that #230 was supposed to enforce. The score formula bit-packing and `getRankings` sort are correct and well-designed; the bugs are entirely in the reservation/refund lifecycle and in the choice to read raw committed storage from a documented "settled"-named getter. Cost tables match v4 spec §8 line-for-line. Durations diverge from spec ("single-tick") to 2/2/4 — likely intentional but unannounced.
9409:docs/reviews/pr199-r3-codereview-codex-5-5.md:8951:./docs/reviews/pr198-r2-codereview-codex-5-5.md:9033:docs/reviews/pr199-codereview-opus-4-7.md:7:**Verdict: NEEDS FIXES — do not merge as-is.** Phase 8.1–8.4 ship the correct shape (queue-time vault deduction, mission-nonce-bound reservations, score/rank getters), but four HIGH-severity bugs land with this PR — three of which produce permanent, silent resource loss in well-reachable scenarios (cold death, starvation, BuildWall+UpgradeWall race, level-binding mismatch on cancellation). One MEDIUM-severity bug re-violates the #261 derived-getter contract that #230 was supposed to enforce. The score formula bit-packing and `getRankings` sort are correct and well-designed; the bugs are entirely in the reservation/refund lifecycle and in the choice to read raw committed storage from a documented "settled"-named getter. Cost tables match v4 spec §8 line-for-line. Durations diverge from spec ("single-tick") to 2/2/4 — likely intentional but unannounced.
9429:docs/reviews/pr199-codereview-opus-4-7.md:7:**Verdict: NEEDS FIXES — do not merge as-is.** Phase 8.1–8.4 ship the correct shape (queue-time vault deduction, mission-nonce-bound reservations, score/rank getters), but four HIGH-severity bugs land with this PR — three of which produce permanent, silent resource loss in well-reachable scenarios (cold death, starvation, BuildWall+UpgradeWall race, level-binding mismatch on cancellation). One MEDIUM-severity bug re-violates the #261 derived-getter contract that #230 was supposed to enforce. The score formula bit-packing and `getRankings` sort are correct and well-designed; the bugs are entirely in the reservation/refund lifecycle and in the choice to read raw committed storage from a documented "settled"-named getter. Cost tables match v4 spec §8 line-for-line. Durations diverge from spec ("single-tick") to 2/2/4 — likely intentional but unannounced.
9430:docs/reviews/pr199-codereview-opus-4-7.md:19:### H1 — Cold-damage and starvation death paths permanently burn upgrade reservations and orphan the `_pending*UpgradesByClan` counter [`packages/contracts/src/ClanWorld.sol:544-559`, `:565-591`]
9434:docs/reviews/pr199-codereview-opus-4-7.md:233:- **codex-5-4 produced no signal** — the session was killed mid-investigation; treat its file as empty. codex-5-5 is the only complete prior automated review and identified H2/H3/H4 (originally numbered H1/H2/H3 in their report) plus several MEDIUMs that overlap with M3/M5 here. This review adds H1 (cold/starvation refund miss — current bug, not theoretical), M1 (resource-burn ordering at settle), M2 (silent failure path), M6 (wood scope creep), M7 (event ABI break), L1 (`_` prefix), L2 (error-code regression), L7 (test gaps).
9464:docs/reviews/pr200-codereview-codex-5-4.md:6413:/bin/bash -lc "rg -n \"_settleClan\\(|submitClanOrders\\(|clan.lastSettledTick|starvationStartsAtTick|_resolveWorldEvents|_killClan|clanState = ClanState.DEAD|clanState "'!= ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
9480:docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
9539:docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
9576:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
9577:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
9578:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
9579:docs/reviews/r2/pr194-synthesis.md:35:| M3 | `ClanWorld.sol:1206` (`_simulateResolveAction`) | 4.6 = **1/5** | MED | **Starvation check uses `_world.currentTick` instead of simulation tick.** Latent bug — current control flow prevents misalignment, but breaks if simulation structure changes. Use `sim.clan.starvationStartsAtTick <= tick`. |
9580:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
9581:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
9687:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
9688:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
9689:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
9690:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
9874:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
9875:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
9876:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
9877:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
10037:2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.
10039:## HIGH severity findings
10042:## MEDIUM severity findings
10045:## LOW severity findings
10050:If clean, say "CLEAN — no findings" under each section.
10249:**Verdict: NEEDS_FIXES — single-model verdict but concrete + targeted findings.** R1 fixes mostly landed, but Codex 5.5 found 2 NEW HIGHs that the R1 fix-round introduced or didn't fully close: starvation timing edge case for pre-winter starvers + hardcoded TS ABI staleness after WorldState/WorldSnapshot layout additions.
10258:## MUST FIX — single-model HIGH (codex 5.5)
10262:| H1 | `ClanWorld.sol:431-441` | HIGH | **Pre-winter starvers still die one tick earlier than fresh winter starvers.** The guard `if (winter && starving && clan.starvationStartsAtTick < tick)` defers death for a clan that starts starving on the first winter tick, but not for a clan already starving BEFORE winter. Example: winter starts at tick 110; a pre-winter starver with `starvationStartsAtTick = 105` dies on tick 110, while a fresh winter starver with `starvationStartsAtTick = 110` first dies on tick 111. **The added test missed this** because it sets both clans' `lastSettledTick` after winter has already begun. **Fix:** comparison needs effective winter starvation start, e.g. `max(starvationStartsAtTick, currentWinterStartTick)`. The H2 R1 fix went 80% but missed the edge. |
10263:| H2 | `IClanWorld.sol:192, :414`; `packages/runner/src/runnerCastHeartbeat.ts:38-51`; `packages/shared/src/adapters/IChainClient.ts:34-44` | HIGH | **Hardcoded TS ABIs stale after WorldState / WorldSnapshot layout additions.** `IClanWorld.sol` adds `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields, but the TS hardcoded structs in runner + shared adapters do NOT include them. Practical impact: `runnerCastHeartbeat.ts` decodes `currentSeasonNumber` as `nextHeartbeatAtTs` (wrong offset), and `IChainClient.ts` decodes `getWorldSnapshot` later fields at wrong offsets — the dynamic `leaderboard` offset is especially likely to fail decoding. **Fix:** regenerate TS ABI structs from current `IClanWorld.sol` state, OR if the hardcoded copy is intentional, add the new fields. The generated ABI JSON is correct; only the hardcoded TS mirrors are stale. |
10265:## SHOULD FIX (MEDIUM)
10269:| M1 | `ClanWorld.sol:440-447` | **Wood burn uses post-starvation living count on same tick.** If starvation kills a clansman, wood burn is calculated AFTER from the reduced `livingClansmen`. A 4-clansman clan that loses one to starvation on a tick burns `2.5e18`, not `3e18` (per-clansman 0.5×3 + per-base 1.0). Fairness wrinkle — same-tick ordering. Liam: deliberate or fix? |
10270:| M2 | `ClanWorld.sol:1276-1290` | **Clans minted during winter start `WinterLocked` with nonzero wheat.** Winter-minted plots get `state = WinterLocked`, but still initialize `remainingWheat = WHEAT_PLOT_STARTING_WHEAT`. Harvesting is blocked, so this is a state/indexer invariant bug not a gameplay exploit, but it violates the winter-lock triple expected for plots. Fix: gate the `remainingWheat` init on winter state too. |
10274:- **H1 cold-damage RNG seed** — intentionally parked per Liam directive on Phase 10 R1 ("could actually be more interesting to see if elders figure this out"). Not counted as R2 finding.
10278:- **Codex 5.5:** NEEDS_FIXES — 2 HIGH (starvation edge + ABI staleness) + 2 MED (wood burn ordering + winter-mint plot state).
10283:- N/A — single-model verdict. Confidence is lower than typical R2 but findings are specific + reproducible from code-grep.
10291:If Liam wants single-model R2 to be more confident, can re-dispatch Opus 4.7 (or fall back to Opus 4.6) to verify before merging. Default: trust codex 5.5 on these — they're code-grep-verified findings, not LLM speculation.
10413:/bin/bash -lc "rg -n \"function getTravelTicks|getTravelTicks\\(\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
10415:packages/contracts/src/ClanWorld.sol:1815:    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
10416:packages/contracts/src/IClanWorld.sol:715:    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
10419:/bin/bash -lc "sed -n '1788,1815p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
10513:docs/reviews/pr250-r3-codereview-codex-5-3.md:7009:/bin/bash -lc 'git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n "ClansmanDiedFromCold|ColdDamageApplied|ClanDied|ClanEliminated|ClansmanColdDeath|WallDegradedByCold"' in /home/claude/code/clan-world
10541:docs/reviews/pr250-r2-codereview-codex-5-5.md:3110:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10542:docs/reviews/pr250-r2-codereview-codex-5-5.md:3132:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10543:docs/reviews/pr250-r2-codereview-codex-5-5.md:3383:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2611:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10544:docs/reviews/pr250-r2-codereview-codex-5-5.md:3405:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2922:docs/reviews/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10551:packages/contracts/src/IClanWorld.sol:613:    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
10552:packages/contracts/src/IClanWorld.sol:614:    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
10582:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10587:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10592:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4138:docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
10646:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2611:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10651:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2922:docs/reviews/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10678:docs/reviews/pr198-r2-codereview-codex-5-5.md:7720:docs/reviews/pr250-codereview-codex-5-4.md:4138:docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
10680:docs/reviews/pr198-r2-codereview-codex-5-5.md:11153:docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
10714:docs/reviews/pr250-r3-codereview-codex-5-4.md:4610:/bin/bash -lc "rg -n \"ClansmanDiedFromCold|ClanColdShortage|ClansmanColdDeath|WallDegradedByCold|ClanDied|WinterStarted|WinterEnded\" packages/contracts/src/IClanWorld.sol packages/contracts/src/ClanWorld.sol packages/contracts/src/ClanWorldStub.sol packages -g'*.ts'" in /home/claude/code/clan-world
10715:docs/reviews/pr250-r3-codereview-codex-5-4.md:4614:packages/contracts/src/IClanWorld.sol:614:    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
10738:docs/reviews/pr250-r3-codereview-codex-5-4.md:5424:docs/reviews/pr250-r3-codereview-codex-5-3.md:7009:/bin/bash -lc 'git show 400463e:packages/contracts/src/ClanWorld.sol | rg -n "ClansmanDiedFromCold|ColdDamageApplied|ClanDied|ClanEliminated|ClansmanColdDeath|WallDegradedByCold"' in /home/claude/code/clan-world
10754:docs/reviews/pr250-r3-codereview-codex-5-4.md:5880:docs/reviews/pr250-r2-codereview-codex-5-5.md:3110:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10755:docs/reviews/pr250-r3-codereview-codex-5-4.md:5895:docs/reviews/pr250-r2-codereview-codex-5-5.md:3132:docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10756:docs/reviews/pr250-r3-codereview-codex-5-4.md:5962:docs/reviews/pr250-r2-codereview-codex-5-5.md:3383:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2611:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10757:docs/reviews/pr250-r3-codereview-codex-5-4.md:5977:docs/reviews/pr250-r2-codereview-codex-5-5.md:3405:docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2922:docs/reviews/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
10802:docs/reviews/pr250-r3-codereview-codex-5-4.md:9480:docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
10860:docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
10869:packages/contracts/src/ClanWorld.sol:1093:        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
10870:packages/contracts/src/IClanWorld.sol:151:enum StatusCode {
10871:packages/contracts/src/IClanWorld.sol:179:    ERR_CARRY_FULL

exec
/bin/bash -lc 'rg -n "HIGH|MEDIUM|finding|ClanWorld.sol|NEEDS_FIXES|CLEAN" docs/reviews/pr250-r3-codereview-opus-4-7.md' in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc 'rg -n "HIGH|MEDIUM|finding|ClanWorld.sol|NEEDS_FIXES|CLEAN" docs/reviews/pr250-r3-codereview-gemini-3-1-pro.md' in /home/claude/code/clan-world
 succeeded in 0ms:
691:Error executing tool grep_search: Path does not exist: /home/claude/claudes-world/packages/contracts/src/ClanWorld.sol

collab: Wait
codex
One subagent reported against the stale checkout, so I’m treating only the finding that still reproduces against `400463e`: heartbeat-settled missions bypass the normal per-tick upkeep pipeline. The stale winter-timing/status findings are already fixed in the head commit, so I’m filtering them out rather than carrying noise into the final review.
collab: Wait
exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | nl -ba | sed -n '660,710p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   660	                        {
   661	                          "name": "carryFish",
   662	                          "type": "uint256",
   663	                          "internalType": "uint256"
   664	                        }
   665	                      ]
   666	                    },
   667	                    {
   668	                      "name": "activeMission",
   669	                      "type": "tuple",
   670	                      "internalType": "struct Mission",
   671	                      "components": [
   672	                        {
   673	                          "name": "active",
   674	                          "type": "bool",
   675	                          "internalType": "bool"
   676	                        },
   677	                        {
   678	                          "name": "nonce",
   679	                          "type": "uint64",
   680	                          "internalType": "uint64"
   681	                        },
   682	                        {
   683	                          "name": "submittedAtTick",
   684	                          "type": "uint64",
   685	                          "internalType": "uint64"
   686	                        },
   687	                        {
   688	                          "name": "executesAtTick",
   689	                          "type": "uint64",
   690	                          "internalType": "uint64"
   691	                        },
   692	                        {
   693	                          "name": "settlesAtTick",
   694	                          "type": "uint64",
   695	                          "internalType": "uint64"
   696	                        },
   697	                        {
   698	                          "name": "clansmanId",
   699	                          "type": "uint32",
   700	                          "internalType": "uint32"
   701	                        },
   702	                        {
   703	                          "name": "startRegion",
   704	                          "type": "uint8",
   705	                          "internalType": "uint8"
   706	                        },
   707	                        {
   708	                          "name": "targetRegion",
   709	                          "type": "uint8",
   710	                          "internalType": "uint8"

exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | nl -ba | sed -n '780,810p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   780	                  "components": [
   781	                    {
   782	                      "name": "active",
   783	                      "type": "bool",
   784	                      "internalType": "bool"
   785	                    },
   786	                    {
   787	                      "name": "nonce",
   788	                      "type": "uint64",
   789	                      "internalType": "uint64"
   790	                    },
   791	                    {
   792	                      "name": "submittedAtTick",
   793	                      "type": "uint64",
   794	                      "internalType": "uint64"
   795	                    },
   796	                    {
   797	                      "name": "executesAtTick",
   798	                      "type": "uint64",
   799	                      "internalType": "uint64"
   800	                    },
   801	                    {
   802	                      "name": "settlesAtTick",
   803	                      "type": "uint64",
   804	                      "internalType": "uint64"
   805	                    },
   806	                    {
   807	                      "name": "clansmanId",
   808	                      "type": "uint32",
   809	                      "internalType": "uint32"
   810	                    },

exec
/bin/bash -lc "git show 400463e:packages/contracts/abi/IClanWorld.json | nl -ba | sed -n '2988,3020p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2988	          "name": "csId",
  2989	          "type": "uint32",
  2990	          "indexed": false,
  2991	          "internalType": "uint32"
  2992	        },
  2993	        {
  2994	          "name": "tick",
  2995	          "type": "uint64",
  2996	          "indexed": false,
  2997	          "internalType": "uint64"
  2998	        }
  2999	      ],
  3000	      "anonymous": false
  3001	    },
  3002	    {
  3003	      "type": "event",
  3004	      "name": "ClansmanDiedFromCold",
  3005	      "inputs": [
  3006	        {
  3007	          "name": "clanId",
  3008	          "type": "uint32",
  3009	          "indexed": true,
  3010	          "internalType": "uint32"
  3011	        },
  3012	        {
  3013	          "name": "atTick",
  3014	          "type": "uint64",
  3015	          "indexed": false,
  3016	          "internalType": "uint64"
  3017	        }
  3018	      ],
  3019	      "anonymous": false
  3020	    },

exec
/bin/bash -lc "git show 400463e:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '128,170p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc 'git show 400463e:packages/contracts/abi/IClanWorld.json | nl -ba | rg -n "ColdDamageApplied|ClansmanDiedFromCold"' in /home/claude/code/clan-world
 succeeded in 0ms:
3004:  3004	      "name": "ClansmanDiedFromCold",
3023:  3023	      "name": "ColdDamageApplied",

codex
# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY
NEEDS_FIXES. The contract-side winter mechanics are mostly integrated, but there are two merge blockers: heartbeat-settled missions bypass the normal upkeep/death pipeline, and the shared hand-written ABI for `getClanFullView` is stale enough to misdecode mission tuples. Do not merge until the HIGH findings are fixed.

## HIGH severity findings

`packages/contracts/src/ClanWorld.sol:1070`, `packages/contracts/src/ClanWorld.sol:1092`

`heartbeat()` calls `_settleCompletingMissions(closedTick)` directly, but that path only resolves missions. It skips the canonical `_settleClan` per-tick ordering: upkeep, winter food/wood burn, cold/starvation deaths, crop regrow, and `lastSettledTick` advancement. A clan can have a due deposit/market/harvest mission completed by heartbeat, then later lazy settlement replays earlier winter upkeep against the already-mutated vault/mission state. Worse, a clansman that should have died from starvation/cold before the due tick can still complete the mission.

Suggested fix: refactor heartbeat mission settlement to use the same per-tick settlement pipeline for touched clans through `closedTick + 1`, with `lastSettledTick` updated, before market execution. Add an integration test where a mission settles during winter while the clan has insufficient food/wood.

`packages/shared/src/adapters/IChainClient.ts:136`, `packages/shared/src/adapters/IChainClient.ts:161`, `packages/contracts/abi/IClanWorld.json:682`, `packages/contracts/abi/IClanWorld.json:792`

The hand-written `getClanFullView` ABI omits `Mission.submittedAtTick`, `executesAtTick`, and `settlesAtTick` in both nested `activeMission` tuples. The canonical ABI includes those fields between `nonce` and `clansmanId`. This shifts every following decoded field inside `clansmen[]`, so real reads can corrupt mission/action/market data or fail decoding.

Suggested fix: stop hand-maintaining this ABI fragment, or copy the exact `Mission` component list into both locations. Add an ABI parity test against `packages/contracts/abi/IClanWorld.json`.

## MEDIUM severity findings

`packages/contracts/src/ClanWorld.sol:505`

Cold-death victim selection uses `_world.currentTickSeed`, not the seed for the replayed `tick`. Because lazy settlement can happen many heartbeats later, the selected dead clansman can change depending on when the clan is settled. That makes the death identity timing-manipulable.

Suggested fix: use `_tickSeeds[tick]` for cold-death RNG and add a test proving the same historical winter tick picks the same victim even if settlement is delayed.

`packages/shared/src/adapters/IChainClient.ts:7`, `packages/shared/src/adapters/IChainClient.ts:369`

`submitClanOrders` returns only `{ txHash }`, while the contract returns per-order `StatusCode`s including the new `ERR_WINTER_LOCKED` and `ERR_MUST_SETTLE_FIRST`. `writeContract` discards return data, so Elders can treat rejected orders as successful submitted transactions.

Suggested fix: use `simulateContract` before broadcasting, widen the adapter return type to include decoded `OrderResult[]`, and update CLI/orchestrator callers.

`packages/contracts/src/IClanWorld.sol:623`, `packages/contracts/abi/IClanWorld.json:3004`, `packages/contracts/abi/IClanWorld.json:3023`

The interface/ABI still expose stale cold events `ColdDamageApplied` and `ClansmanDiedFromCold`, but the implementation emits the newer `ClanColdShortage`, `WallDegradedByCold`, `ClansmanColdDeath`, and `ClanDied`. ABI-driven indexers may subscribe to the old events and silently miss winter damage/death.

Suggested fix: remove stale events and regenerate ABI, or intentionally emit compatibility events and document both sets.

## LOW severity findings

`packages/runner/src/runnerCastHeartbeat.ts:38`

The runner now has a corrected hand-written `getWorldState` ABI, but there is no parity test. This is exactly the seam that regressed in prior fix rounds.

Suggested fix: add a small test comparing the runner ABI field order for `getWorldState` against the canonical ABI, especially `nextHeartbeatAtTs`.

## Cross-cutting observations

The fix-round did address the earlier winter start, cold reset, winter-minted plot, status-code, and `WorldSnapshot` ABI drift issues.

The remaining risk is concentrated in hand-maintained integration seams: heartbeat settlement shortcuts and copied ABI tuples. Contract tests cover many winter happy paths, but they do not yet cover heartbeat-driven mission completion plus winter upkeep/death in the same timeline.
2026-04-30T01:46:07.843531Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-d689-7ea2-abf6-647132d8368a not found
2026-04-30T01:46:07.843564Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-e907-79b2-b234-114d847122bf not found
2026-04-30T01:46:07.843804Z ERROR codex_core::session: failed to record rollout items: thread 019ddc0a-92c8-7603-89bb-83421831e3ff not found
tokens used
414,765
# Phase Super-Swarm Review — PR #250 (head 400463e)

## SUMMARY
NEEDS_FIXES. The contract-side winter mechanics are mostly integrated, but there are two merge blockers: heartbeat-settled missions bypass the normal upkeep/death pipeline, and the shared hand-written ABI for `getClanFullView` is stale enough to misdecode mission tuples. Do not merge until the HIGH findings are fixed.

## HIGH severity findings

`packages/contracts/src/ClanWorld.sol:1070`, `packages/contracts/src/ClanWorld.sol:1092`

`heartbeat()` calls `_settleCompletingMissions(closedTick)` directly, but that path only resolves missions. It skips the canonical `_settleClan` per-tick ordering: upkeep, winter food/wood burn, cold/starvation deaths, crop regrow, and `lastSettledTick` advancement. A clan can have a due deposit/market/harvest mission completed by heartbeat, then later lazy settlement replays earlier winter upkeep against the already-mutated vault/mission state. Worse, a clansman that should have died from starvation/cold before the due tick can still complete the mission.

Suggested fix: refactor heartbeat mission settlement to use the same per-tick settlement pipeline for touched clans through `closedTick + 1`, with `lastSettledTick` updated, before market execution. Add an integration test where a mission settles during winter while the clan has insufficient food/wood.

`packages/shared/src/adapters/IChainClient.ts:136`, `packages/shared/src/adapters/IChainClient.ts:161`, `packages/contracts/abi/IClanWorld.json:682`, `packages/contracts/abi/IClanWorld.json:792`

The hand-written `getClanFullView` ABI omits `Mission.submittedAtTick`, `executesAtTick`, and `settlesAtTick` in both nested `activeMission` tuples. The canonical ABI includes those fields between `nonce` and `clansmanId`. This shifts every following decoded field inside `clansmen[]`, so real reads can corrupt mission/action/market data or fail decoding.

Suggested fix: stop hand-maintaining this ABI fragment, or copy the exact `Mission` component list into both locations. Add an ABI parity test against `packages/contracts/abi/IClanWorld.json`.

## MEDIUM severity findings

`packages/contracts/src/ClanWorld.sol:505`

Cold-death victim selection uses `_world.currentTickSeed`, not the seed for the replayed `tick`. Because lazy settlement can happen many heartbeats later, the selected dead clansman can change depending on when the clan is settled. That makes the death identity timing-manipulable.

Suggested fix: use `_tickSeeds[tick]` for cold-death RNG and add a test proving the same historical winter tick picks the same victim even if settlement is delayed.

`packages/shared/src/adapters/IChainClient.ts:7`, `packages/shared/src/adapters/IChainClient.ts:369`

`submitClanOrders` returns only `{ txHash }`, while the contract returns per-order `StatusCode`s including the new `ERR_WINTER_LOCKED` and `ERR_MUST_SETTLE_FIRST`. `writeContract` discards return data, so Elders can treat rejected orders as successful submitted transactions.

Suggested fix: use `simulateContract` before broadcasting, widen the adapter return type to include decoded `OrderResult[]`, and update CLI/orchestrator callers.

`packages/contracts/src/IClanWorld.sol:623`, `packages/contracts/abi/IClanWorld.json:3004`, `packages/contracts/abi/IClanWorld.json:3023`

The interface/ABI still expose stale cold events `ColdDamageApplied` and `ClansmanDiedFromCold`, but the implementation emits the newer `ClanColdShortage`, `WallDegradedByCold`, `ClansmanColdDeath`, and `ClanDied`. ABI-driven indexers may subscribe to the old events and silently miss winter damage/death.

Suggested fix: remove stale events and regenerate ABI, or intentionally emit compatibility events and document both sets.

## LOW severity findings

`packages/runner/src/runnerCastHeartbeat.ts:38`

The runner now has a corrected hand-written `getWorldState` ABI, but there is no parity test. This is exactly the seam that regressed in prior fix rounds.

Suggested fix: add a small test comparing the runner ABI field order for `getWorldState` against the canonical ABI, especially `nextHeartbeatAtTs`.

## Cross-cutting observations

The fix-round did address the earlier winter start, cold reset, winter-minted plot, status-code, and `WorldSnapshot` ABI drift issues.

The remaining risk is concentrated in hand-maintained integration seams: heartbeat settlement shortcuts and copied ABI tuples. Contract tests cover many winter happy paths, but they do not yet cover heartbeat-driven mission completion plus winter upkeep/death in the same timeline.
EXIT=0
