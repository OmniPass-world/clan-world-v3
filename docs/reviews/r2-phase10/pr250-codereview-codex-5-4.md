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
session id: 019ddb5a-e511-7d02-9013-8e8851981be4
--------
user
Read prompt+diff. Use parallel tool calls + subagents. Output review.

<stdin>
Senior staff engineer FINAL pre-merge review for PR #250 (Phase 10 — Winter + Elimination) at head e4a0d4c.

Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).

## Cohesive-phase review
1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
3. RNG hygiene (cold_damage domain, deterministic clansman death pick)
4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
6. Test coverage on winter boundary edges + clan-death paths

USE PARALLEL TOOL CALLS.

## Output

# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
## SUMMARY
## HIGH severity findings
## MEDIUM severity findings
## LOW severity findings
## Cross-cutting observations

DIFF FOLLOWS.
---
diff --git a/packages/contracts/abi/IClanWorld.json b/packages/contracts/abi/IClanWorld.json
index 2ca0d2c..25edd17 100644
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
+          "type": "uint32",
+          "indexed": false,
+          "internalType": "uint32"
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
+          "type": "uint32",
+          "indexed": false,
+          "internalType": "uint32"
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
+          "type": "uint32",
+          "indexed": false,
+          "internalType": "uint32"
+        }
+      ],
+      "anonymous": false
+    },
     {
       "type": "event",
       "name": "WallLevelChanged",
@@ -3605,9 +3751,9 @@
       "inputs": [
         {
           "name": "tick",
-          "type": "uint64",
+          "type": "uint32",
           "indexed": true,
-          "internalType": "uint64"
+          "internalType": "uint32"
         }
       ],
       "anonymous": false
@@ -3618,9 +3764,9 @@
       "inputs": [
         {
           "name": "tick",
-          "type": "uint64",
+          "type": "uint32",
           "indexed": true,
-          "internalType": "uint64"
+          "internalType": "uint32"
         }
       ],
       "anonymous": false
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..b39bf22 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -38,6 +38,7 @@ import {
     RegionOccupant
 } from "./IClanWorld.sol";
 import {StubPool} from "./StubPool.sol";
+import {RNG} from "./lib/RNG.sol";
 import {ReentrancyGuard} from "./util/ReentrancyGuard.sol";
 
 /// @title ClanWorld
@@ -77,6 +78,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
+    uint256 public constant MAX_CROP_TRANSITION_PER_TICK = 48;
 
     // =========================================================================
     // CONSTRUCTOR
@@ -89,11 +92,8 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -371,6 +371,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         for (uint64 tick = fromTick; tick < curTick; tick++) {
             // 1. Apply upkeep for this tick
             _applyUpkeep(clan, tick);
+            if (clan.clanState == ClanState.DEAD) break;
 
             // 2. Wheat plot regrow check (lazy, per tick)
             for (uint256 pi = 0; pi < 2; pi++) {
@@ -389,16 +390,29 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -422,6 +436,158 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             clan.starvationStartsAtTick = 0;
             emit ClanStarvationChanged(clan.clanId, false, tick);
         }
+
+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
+            _killNextClansmanFromStarvation(clan, tick);
+        }
+        if (clan.clanState == ClanState.DEAD) return;
+
+        if (winter) {
+            uint256 woodNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
+            if (clan.vaultWood >= woodNeeded) {
+                clan.vaultWood -= woodNeeded;
+            } else {
+                uint256 woodShort = woodNeeded - clan.vaultWood;
+                clan.vaultWood = 0;
+                uint16 oldColdDamage = clan.coldDamage;
+                if (clan.coldDamage < type(uint16).max) {
+                    clan.coldDamage += 1;
+                }
+                emit ClanColdShortage(clan.clanId, _eventTick(tick), woodShort);
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
+            emit WallDegradedByCold(clan.clanId, clan.wallLevel, _eventTick(tick));
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
+        emit ClansmanColdDeath(clan.clanId, cs.clansmanId, _eventTick(tick));
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
@@ -638,6 +804,11 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -942,39 +1113,99 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
+            _resetColdDamageForAllClans();
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
+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
+                transitions++;
+            }
+        }
+    }
+
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
             }
         }
     }
 
+    function _resetColdDamageForAllClans() internal {
+        for (uint256 i = 0; i < _allClanIds.length; i++) {
+            _clans[_allClanIds[i]].coldDamage = 0;
+        }
+    }
+
+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
+        return _eventTick(tick);
+    }
+
+    function _eventTick(uint64 tick) internal pure returns (uint32) {
+        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
+        // forge-lint: disable-next-line(unsafe-typecast)
+        return uint32(tick);
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
@@ -1039,15 +1270,18 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         clan.vaultWheat = 20e18;
         clan.vaultFish = 2e18;
 
+        WheatPlotState startingPlotState =
+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
+
         // Wheat plots
         _wheatPlots[clanId][0] = WheatPlot({
-            state: WheatPlotState.Harvestable,
+            state: startingPlotState,
             region: ClanWorldConstants.REGION_WEST_FARMS,
             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
             regrowUntilTick: 0
         });
         _wheatPlots[clanId][1] = WheatPlot({
-            state: WheatPlotState.Harvestable,
+            state: startingPlotState,
             region: ClanWorldConstants.REGION_EAST_FARMS,
             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
             regrowUntilTick: 0
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
index 36ce29b..3700486 100644
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
@@ -361,4 +373,42 @@ contract ClanWorldStub is IClanWorld {
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
+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
+        // forge-lint: disable-next-line(unsafe-typecast)
+        return uint32(tick);
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
index 2b80fbe..4ea413a 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -26,8 +26,9 @@ pragma solidity ^0.8.34;
 library ClanWorldConstants {
     // World cadence
     uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
-    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
+    uint64 internal constant WINTER_START_TICK = 110;
     uint64 internal constant WINTER_DURATION_TICKS = 10;
+    uint64 internal constant WINTER_PERIOD_TICKS = 110;
     uint64 internal constant SEASON_DURATION_TICKS = 360;
 
     // Bandit cadence
@@ -60,8 +61,10 @@ library ClanWorldConstants {
     // Upkeep
     uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
     uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
-    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
+    uint256 internal constant WINTER_WOOD_BURN_PER_CLANSMAN = 5e17; // 0.5
     uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
+    uint16 internal constant COLD_DAMAGE_PER_WALL_DEGRADATION = 2;
+    uint16 internal constant COLD_DAMAGE_PER_CLANSMAN_DEATH = 2;
 
     // Wheat plots
     uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
@@ -176,7 +179,9 @@ enum StatusCode {
     ERR_NO_ACTIVE_BANDIT,
     ERR_SEASON_ENDED,
     ERR_NOT_ENOUGH_GOLD,
-    ERR_CARRY_FULL
+    ERR_CARRY_FULL,
+    ERR_WINTER_LOCKED,
+    ERR_MUST_SETTLE_FIRST
 }
 
 // =============================================================================
@@ -188,8 +193,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -489,8 +494,8 @@ struct RegionOccupant {
 interface IClanWorldEvents {
     // ----- world clock -----
     event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
-    event WinterStarted(uint64 indexed tick);
-    event WinterEnded(uint64 indexed tick);
+    event WinterStarted(uint32 indexed tick);
+    event WinterEnded(uint32 indexed tick);
     event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
 
     // ----- clan lifecycle -----
@@ -499,7 +504,11 @@ interface IClanWorldEvents {
     );
     event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
     event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
+    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
     event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
+    event ClanColdShortage(uint32 indexed clanId, uint32 tick, uint256 woodShort);
+    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint32 tick);
+    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint32 tick);
 
     // ----- missions -----
     event MissionAssigned(
@@ -710,6 +719,9 @@ interface IClanWorld is IClanWorldEvents {
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
index 92781d3..9c97a7b 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -48,6 +48,31 @@ contract ClanWorldTestHarness is ClanWorld {
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
+    function setClanIronAndGold(uint32 clanId, uint256 vaultIron, uint256 goldBalance) external {
+        _clans[clanId].vaultIron = vaultIron;
+        _clans[clanId].goldBalance = goldBalance;
+    }
 }
 
 contract ClanWorldTest is Test {
@@ -125,6 +150,42 @@ contract ClanWorldTest is Test {
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
@@ -488,10 +549,10 @@ contract ClanWorldTest is Test {
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
@@ -501,7 +562,7 @@ contract ClanWorldTest is Test {
             _advanceTick();
         }
 
-        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
+        // submitClanOrders should return ERR_MUST_SETTLE_FIRST
         // without reverting, for every order in the batch
         ClanOrder[] memory orders = new ClanOrder[](1);
         orders[0] = ClanOrder({
@@ -519,9 +580,11 @@ contract ClanWorldTest is Test {
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
@@ -1814,7 +1877,11 @@ contract ClanWorldTest is Test {
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
@@ -1920,56 +1987,340 @@ contract ClanWorldTest is Test {
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
+        assertEq(_countLogs(logs, keccak256("WinterStarted(uint32)")), 1, "WinterStarted emits once");
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
-        assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
-        );
+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
+        assertEq(_countLogs(logs, keccak256("WinterEnded(uint32)")), 1, "WinterEnded emits once");
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
+        assertEq(_countLogs(logs, keccak256("WinterStarted(uint32)")), 1, "next WinterStarted emits once");
+        assertTrue(world.isWinter(), "winter should be active in next period");
+        assertEq(world.getWorldState().currentTick, nextWinterStart + 1);
+    }
+
+    function test_winter_cropTransitions_lockThenRestartRegrow() public {
+        uint32 clanId1 = _mintClan();
+        uint32 clanId2 = _mintClan();
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
+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
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
+        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
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
+        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
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
+        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
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
+        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
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
+        assertEq(_countLogs(logs, keccak256("ClansmanColdDeath(uint32,uint32,uint32)")), 1, "cold death should emit");
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
+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 clanId = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        uint256 goldBalance = 11e18;
+
+        _advanceToTick(winterStart + 4);
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
+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
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
+        _advanceToTick(winterStart + 4);
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
+        _advanceToTick(winterStart + 4);
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
@@ -1982,9 +2333,8 @@ contract ClanWorldTest is Test {
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
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
Reviewing the Phase 10 diff with a code-review lens. I’m pulling the contract, tests, and the relevant spec/docs in parallel first, and I’ll split a couple of focused checks out to subagents so we can cover the boundary-tick and cross-phase risks thoroughly.
  → Gather contract, test, and spec context around winter, starvation, elimination, and dead-clan rules
  • Delegate focused parallel checks for boundary timing and cross-phase dead-clan interactions
  • Synthesize findings into final pre-merge review with severity ranking and file references
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
    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
    uint256 internal constant IRON_CAP = 5e18;
    uint256 internal constant WHEAT_CAP = 40e18;
    uint256 internal constant FISH_CAP = 8e18;

    // Gathering yields
    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%

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
    Wait,
    UpgradeWall
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

exec
/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/IClanWorld.sol:120:    WinterLocked
packages/contracts/src/IClanWorld.sol:203:    bool winterActive;
packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:205:    uint64 winterEndsAtTick; // 0 if not active
packages/contracts/src/IClanWorld.sol:242:    uint64 starvationStartsAtTick; // 0 = none
packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
packages/contracts/src/IClanWorld.sol:418:    bool winterActive;
packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:420:    uint64 winterEndsAtTick;
packages/contracts/src/IClanWorld.sol:504:    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:618:    // ----- winter cold damage -----
packages/contracts/src/IClanWorld.sol:651:    function settleClansman(uint32 csId) external;
packages/contracts/src/IClanWorld.sol:704:    function getWorldState() external view returns (WorldState memory);
packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
packages/contracts/test/ClanWorldStub.t.sol:28:        assertEq(stub.getWorldState().currentTick, 1);
packages/contracts/test/ClanWorldStub.t.sol:30:        assertEq(stub.getWorldState().currentTick, 2);
packages/contracts/test/ClanWorldStub.t.sol:39:        WorldState memory ws = stub.getWorldState();
packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
packages/contracts/test/ClanWorldStub.t.sol:50:        assertFalse(ws.winterActive);
packages/contracts/test/ClanWorld.t.sol:171:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:175:        uint64 tickAfter = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:184:        WorldState memory beforeFirst = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:191:        WorldState memory afterFirst = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:200:        WorldState memory afterSecond = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:210:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:215:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "non-owner caller should advance heartbeat");
packages/contracts/test/ClanWorld.t.sol:220:        uint64 previousTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:229:            WorldState memory state = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:610:        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
packages/contracts/test/ClanWorld.t.sol:746:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:782:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:811:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:850:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1104:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1162:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1644:        // Call settleClansman without advancing any ticks (same tick as submission)
packages/contracts/test/ClanWorld.t.sol:1645:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:1679:        // Call settleClansman on-demand
packages/contracts/test/ClanWorld.t.sol:1680:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:1896:    // test_lazySettle_settleClansman_onDemand
packages/contracts/test/ClanWorld.t.sol:1897:    // settleClansman settles a single clansman without settleClan
packages/contracts/test/ClanWorld.t.sol:1900:    function test_lazySettle_settleClansman_onDemand() public {
packages/contracts/test/ClanWorld.t.sol:1916:        // carryIron is already > 0 after tick advancement — no manual settleClansman required.
packages/contracts/test/ClanWorld.t.sol:1919:        // Call settleClansman on-demand — must be idempotent (no crash, no double-credit).
packages/contracts/test/ClanWorld.t.sol:1921:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:1922:        assertEq(world.getClansman(csId).carryIron, ironBefore, "onDemand: settleClansman is idempotent after heartbeat settle");
packages/contracts/test/ClanWorld.t.sol:1926:    // test_lazySettle_settleClansman_noopIfUpToDate
packages/contracts/test/ClanWorld.t.sol:1927:    // settleClansman is a no-op when clan is already settled
packages/contracts/test/ClanWorld.t.sol:1930:    function test_lazySettle_settleClansman_noopIfUpToDate() public {
packages/contracts/test/ClanWorld.t.sol:1936:        // settleClansman must not revert and must leave state unchanged
packages/contracts/test/ClanWorld.t.sol:1937:        world.settleClansman(csId);
packages/contracts/test/ClanWorld.t.sol:2020:    // Phase 4.4 — season + winter timer tests
packages/contracts/test/ClanWorld.t.sol:2024:        WorldState memory ws = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2029:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2032:        assertFalse(ws.winterActive);
packages/contracts/test/ClanWorld.t.sol:2035:    function test_winter_onset() public {
packages/contracts/test/ClanWorld.t.sol:2036:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
packages/contracts/test/ClanWorld.t.sol:2037:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
packages/contracts/test/ClanWorld.t.sol:2039:        uint64 winterStart =
packages/contracts/test/ClanWorld.t.sol:2041:        for (uint64 i = 0; i < winterStart - 1; i++) {
packages/contracts/test/ClanWorld.t.sol:2051:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
packages/contracts/test/ClanWorld.t.sol:2054:            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
packages/contracts/test/ClanWorld.t.sol:2060:        assertTrue(world.getWorldState().winterActive, "winter should be active");
packages/contracts/test/ClanWorld.t.sol:2061:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
packages/contracts/test/ClanWorld.t.sol:2064:    function test_winter_end_and_next_cycle() public {
packages/contracts/test/ClanWorld.t.sol:2065:        // Advance past first winter end tick (= 110)
packages/contracts/test/ClanWorld.t.sol:2066:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/test/ClanWorld.t.sol:2067:        for (uint64 i = 0; i <= winterEnd; i++) {
packages/contracts/test/ClanWorld.t.sol:2071:        WorldState memory ws = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2072:        assertFalse(ws.winterActive, "winter should be over");
packages/contracts/test/ClanWorld.t.sol:2073:        // next winter at [210, 220)
packages/contracts/test/ClanWorld.t.sol:2075:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2086:        WorldState memory ws = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2090:        // winter reset for new season
packages/contracts/test/ClanWorld.t.sol:2093:        assertEq(ws.winterStartsAtTick, expectedWinterStart);
packages/contracts/test/ClanWorld.t.sol:2097:        WorldState memory ws0 = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2101:        WorldState memory ws1 = world.getWorldState();
packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:138:        _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:339:    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
packages/contracts/src/ClanWorld.sol:443:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
packages/contracts/src/ClanWorld.sol:465:        if (starving && clan.starvationStartsAtTick == 0) {
packages/contracts/src/ClanWorld.sol:466:            clan.starvationStartsAtTick = tick;
packages/contracts/src/ClanWorld.sol:468:        } else if (!starving && clan.starvationStartsAtTick != 0) {
packages/contracts/src/ClanWorld.sol:469:            clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:476:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:909:    ///         4. Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:934:        // Step 4: Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:952:            if (clan.clanState == ClanState.DEAD) continue;
packages/contracts/src/ClanWorld.sol:979:            // reset winter timers for new season
packages/contracts/src/ClanWorld.sol:980:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:986:        // --- winter transitions (timer only; mechanics = Phase 10) ---
packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
packages/contracts/src/ClanWorld.sol:991:            _world.winterActive = true;
packages/contracts/src/ClanWorld.sol:994:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
packages/contracts/src/ClanWorld.sol:995:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:997:            // schedule next winter cycle within this season
packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
packages/contracts/src/ClanWorld.sol:1003:                _world.winterEndsAtTick = nextWinterEnd;
packages/contracts/src/ClanWorld.sol:1005:                // no more winters this season; sentinel = seasonEndTick so guard never fires
packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:1007:                _world.winterEndsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:1021:    function settleClansman(uint32 csId) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:1066:        clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:1067:        clan.coldDamage = 0;
packages/contracts/src/ClanWorld.sol:1127:        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
packages/contracts/src/ClanWorld.sol:2031:    function getWorldState() external view override returns (WorldState memory) {
packages/contracts/src/ClanWorld.sol:2221:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
packages/contracts/src/ClanWorld.sol:2225:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:2414:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorld.sol:2416:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/src/ClanWorld.sol:2423:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
packages/contracts/src/ClanWorld.sol:2428:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;

exec
/bin/bash -lc "sed -n '1,260p' packages/contracts/src/lib/RNG.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

/// @title ClanWorld domain-separated RNG helpers
/// @notice Cheap deterministic randomness derived from a published per-tick seed.
library RNG {
    uint256 internal constant DOMAIN_MISSION_RESOLUTION = uint256(keccak256("clanworld.mission.resolve.v1"));
    uint256 internal constant DOMAIN_BANDIT_SPAWN = uint256(keccak256("clanworld.bandit.spawn.v1"));
    uint256 internal constant DOMAIN_MARKET_NOISE = uint256(keccak256("clanworld.market.noise.v1"));
    uint256 internal constant DOMAIN_WEATHER = uint256(keccak256("clanworld.weather.v1"));
    uint256 internal constant DOMAIN_FAIR_ITERATION = uint256(keccak256("clanworld.fair.iteration.v1"));

    uint256 internal constant MAX_SHUFFLE_N = 64;

    error ShuffleSizeTooLarge(uint256 n, uint256 max);

    /// @notice Returns a uniform uint256 in [0, 2^256).
    function rngUniform(bytes32 seed, uint256 domainSalt, uint256 nonce) internal pure returns (uint256) {
        return uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce)));
    }

    /// @notice Returns a uniform value in [0, max) using rejection sampling.
    /// @dev Returns 0 when max is 0 so callers can handle optional random choices cheaply.
    function rngBounded(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256 max) internal pure returns (uint256) {
        if (max == 0) {
            return 0;
        }

        uint256 remainder;
        unchecked {
            // Computes 2^256 % max without trying to represent 2^256 directly.
            remainder = (uint256(0) - max) % max;
        }
        uint256 maxValid = type(uint256).max - remainder;
        uint256 attempt = 0;

        while (true) {
            uint256 value = uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce, max, attempt)));
            if (value <= maxValid) {
                return value % max;
            }

            unchecked {
                attempt++;
            }
        }

        revert("RNG: unreachable");
    }

    /// @notice Returns true with 50% probability.
    function rngBool(bytes32 seed, uint256 domainSalt, uint256 nonce) internal pure returns (bool) {
        return rngUniform(seed, domainSalt, nonce) & 1 == 1;
    }

    /// @notice Picks an index in proportion to each weight.
    /// @dev Returns 0 for empty arrays and zero-total arrays.
    function rngWeightedPick(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256[] memory weights)
        internal
        pure
        returns (uint256 index)
    {
        uint256 total = 0;
        for (uint256 i = 0; i < weights.length; i++) {
            total += weights[i];
        }

        if (total == 0) {
            return 0;
        }

        uint256 pick = rngBounded(seed, domainSalt, nonce, total);
        uint256 cumulative = 0;
        for (uint256 i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (pick < cumulative) {
                return i;
            }
        }

        return weights.length - 1;
    }

    /// @notice Returns a Fisher-Yates permutation of [0, n).
    /// @dev Reverts above MAX_SHUFFLE_N to keep callers away from accidental gas blowups.
    function rngShuffle(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256 n)
        internal
        pure
        returns (uint8[] memory permutation)
    {
        if (n > MAX_SHUFFLE_N) {
            revert ShuffleSizeTooLarge(n, MAX_SHUFFLE_N);
        }

        permutation = new uint8[](n);
        for (uint256 i = 0; i < n; i++) {
            // casting is safe because n is bounded by MAX_SHUFFLE_N (64).
            // forge-lint: disable-next-line(unsafe-typecast)
            permutation[i] = uint8(i);
        }

        for (uint256 i = n; i > 1; i--) {
            uint256 stepNonce = uint256(keccak256(abi.encodePacked(nonce, n, i)));
            uint256 j = rngBounded(seed, domainSalt, stepNonce, i);
            uint8 tmp = permutation[i - 1];
            permutation[i - 1] = permutation[j];
            permutation[j] = tmp;
        }
    }
}

exec
/bin/bash -lc "sed -n '1,220p' packages/contracts/AGENTS.md" in /home/claude/code/clan-world
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
/bin/bash -lc "rg -n \"winter|starvation|elimination|dead-clan|ClanState.DEAD|§10|§4\\.12|§A2|WinterLocked|OTC\" docs packages -g '"'!packages/contracts/test/**'"'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/runner/src/runnerCastHeartbeat.ts:48:          { name: 'winterActive', type: 'bool' },
packages/runner/src/runnerCastHeartbeat.ts:49:          { name: 'winterStartsAtTick', type: 'uint64' },
packages/runner/src/runnerCastHeartbeat.ts:50:          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-4.md:177:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-4.md:2551:   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr199-codereview-codex-5-4.md:2573:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr199-codereview-codex-5-4.md:2574:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr199-codereview-codex-5-4.md:2576:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr199-codereview-codex-5-4.md:2577:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr199-codereview-codex-5-4.md:2584:   476	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-4.md:3017:   909	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr199-codereview-codex-5-4.md:3879:  2221	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr199-codereview-codex-5-4.md:3883:  2225	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-4.md:4072:  2414	            winterActive: _world.winterActive,
docs/reviews/pr199-codereview-codex-5-4.md:4073:  2415	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/pr199-codereview-codex-5-4.md:4280:   203	    bool winterActive;
docs/reviews/pr199-codereview-codex-5-4.md:4281:   204	    uint64 winterStartsAtTick;
docs/reviews/pr199-codereview-codex-5-4.md:4282:   205	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr199-codereview-codex-5-4.md:4396:   618	    // ----- winter cold damage -----
docs/reviews/pr199-codereview-codex-5-4.md:4400:   622	    // ----- OTC transfers -----
docs/reviews/pr199-codereview-codex-5-4.md:4458:   680	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr199-codereview-codex-5-4.md:4563:   785	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:4613:   934	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr199-codereview-codex-5-4.md:4631:   952	            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr199-codereview-codex-5-4.md:4658:   979	            // reset winter timers for new season
docs/reviews/pr199-codereview-codex-5-4.md:4659:   980	            _world.winterActive = false;
docs/reviews/pr199-codereview-codex-5-4.md:4660:   981	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr199-codereview-codex-5-4.md:4662:   983	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr199-codereview-codex-5-4.md:4665:   986	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr199-codereview-codex-5-4.md:4667:   988	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr199-codereview-codex-5-4.md:4668:   989	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr199-codereview-codex-5-4.md:4670:   991	            _world.winterActive = true;
docs/reviews/pr199-codereview-codex-5-4.md:4673:   994	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr199-codereview-codex-5-4.md:4674:   995	            _world.winterActive = false;
docs/reviews/pr199-codereview-codex-5-4.md:4676:   997	            // schedule next winter cycle within this season
docs/reviews/pr199-codereview-codex-5-4.md:4677:   998	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr199-codereview-codex-5-4.md:4679:  1000	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr199-codereview-codex-5-4.md:4681:  1002	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr199-codereview-codex-5-4.md:4682:  1003	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr199-codereview-codex-5-4.md:4684:  1005	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr199-codereview-codex-5-4.md:4685:  1006	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr199-codereview-codex-5-4.md:4686:  1007	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr199-codereview-codex-5-4.md:4745:  1066	        clan.starvationStartsAtTick = 0;
docs/reviews/pr199-codereview-codex-5-4.md:5033:  2004	    // OTC TRANSFERS
docs/reviews/pr199-codereview-codex-5-4.md:5037:  2008	        revert("OTC transfers not implemented");
docs/reviews/pr199-codereview-codex-5-4.md:5041:  2012	        revert("OTC transfers not implemented");
docs/reviews/pr199-codereview-codex-5-4.md:5045:  2016	        revert("OTC transfers not implemented");
docs/reviews/pr199-codereview-codex-5-4.md:5053:  2024	        revert("OTC transfers not implemented");
docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-4.md:22:1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
docs/reviews/pr250-codereview-codex-5-4.md:23:2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
docs/reviews/pr250-codereview-codex-5-4.md:25:4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
docs/reviews/pr250-codereview-codex-5-4.md:26:5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
docs/reviews/pr250-codereview-codex-5-4.md:27:6. Test coverage on winter boundary edges + clan-death paths
docs/reviews/pr250-codereview-codex-5-4.md:243:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:463:+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
docs/reviews/pr250-codereview-codex-5-4.md:472:-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:474:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:476:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:477:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:478:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:479:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:486:+            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/pr250-codereview-codex-5-4.md:502:-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:503:+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
docs/reviews/pr250-codereview-codex-5-4.md:505:+        bool winter = _isWinterActiveAt(tick);
docs/reviews/pr250-codereview-codex-5-4.md:506:+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
docs/reviews/pr250-codereview-codex-5-4.md:514:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:522:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:526:+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
docs/reviews/pr250-codereview-codex-5-4.md:527:+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:530:+        if (clan.clanState == ClanState.DEAD) return;
docs/reviews/pr250-codereview-codex-5-4.md:532:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:615:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:653:+        if (clan.clanId == 0 || clan.clanState == ClanState.DEAD) return;
docs/reviews/pr250-codereview-codex-5-4.md:655:+        clan.clanState = ClanState.DEAD;
docs/reviews/pr250-codereview-codex-5-4.md:660:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-4.md:696:-            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:697:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:698:-            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:700:-            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:703:         // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:705:-            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:706:-                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:708:-            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:711:-        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:712:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:714:-            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:715:-            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:717:-            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:719:-                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:720:-                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:722:-                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:723:-                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:724:-                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:729:+            emit WinterStarted(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-4.md:734:+            emit WinterEnded(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-4.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/pr250-codereview-codex-5-4.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-4.md:773:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:778:+        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:791:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:812:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-codereview-codex-5-4.md:860:+        if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr250-codereview-codex-5-4.md:916:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:917:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:918:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:927:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:928:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:929:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:943:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:945:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:946:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:947:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:948:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:958:+            emit WinterStarted(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-4.md:961:+            emit WinterEnded(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-4.md:1001:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:1002:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1003:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1006:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:1007:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1008:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1025:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:1026:+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:1031:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:1052:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:1131:+    /// @notice True iff currentTick is inside the recurring winter window.
docs/reviews/pr250-codereview-codex-5-4.md:1282:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1285:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:1286:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1287:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:1293:+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:1294:+        assertEq(snapshot.winterStartsAtTick, 110);
docs/reviews/pr250-codereview-codex-5-4.md:1297:     function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:1298:-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:1299:-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:1301:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:1303:-        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:1309:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1310:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-4.md:1318:-        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-4.md:1321:-            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-4.md:1327:-        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:1328:-        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:1330:+        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
docs/reviews/pr250-codereview-codex-5-4.md:1334:+        assertTrue(world.isWinter(), "winter should be active past start tick");
docs/reviews/pr250-codereview-codex-5-4.md:1335:+        assertTrue(ws.winterActive, "world state should report winter active");
docs/reviews/pr250-codereview-codex-5-4.md:1336:+        assertEq(ws.winterStartsAtTick, winterStart);
docs/reviews/pr250-codereview-codex-5-4.md:1337:+        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1340:     function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-4.md:1341:-        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-4.md:1342:-        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:1343:-        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:1348:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1349:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-4.md:1358:         assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-4.md:1359:-        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-4.md:1361:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1364:+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
docs/reviews/pr250-codereview-codex-5-4.md:1366:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1369:+    function test_winter_restarts_after_full_period() public {
docs/reviews/pr250-codereview-codex-5-4.md:1380:+        assertTrue(world.isWinter(), "winter should be active in next period");
docs/reviews/pr250-codereview-codex-5-4.md:1384:+    function test_winter_cropTransitions_lockThenRestartRegrow() public {
docs/reviews/pr250-codereview-codex-5-4.md:1392:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1393:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-4.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/pr250-codereview-codex-5-4.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/pr250-codereview-codex-5-4.md:1407:+        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
docs/reviews/pr250-codereview-codex-5-4.md:1409:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1410:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-4.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/pr250-codereview-codex-5-4.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/pr250-codereview-codex-5-4.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-4.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-4.md:1439:+    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
docs/reviews/pr250-codereview-codex-5-4.md:1441:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1442:+        _advanceToTick(winterStart - 2);
docs/reviews/pr250-codereview-codex-5-4.md:1446:+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
docs/reviews/pr250-codereview-codex-5-4.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/pr250-codereview-codex-5-4.md:1460:+    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
docs/reviews/pr250-codereview-codex-5-4.md:1464:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1465:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1468:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1473:+        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-4.md:1474:+        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-4.md:1475:+        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
docs/reviews/pr250-codereview-codex-5-4.md:1481:+    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
docs/reviews/pr250-codereview-codex-5-4.md:1485:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1486:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1488:+        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1491:+        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
docs/reviews/pr250-codereview-codex-5-4.md:1495:+        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
docs/reviews/pr250-codereview-codex-5-4.md:1496:+        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
docs/reviews/pr250-codereview-codex-5-4.md:1503:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1504:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1507:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:1510:+        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
docs/reviews/pr250-codereview-codex-5-4.md:1523:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1524:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1526:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:1548:+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
docs/reviews/pr250-codereview-codex-5-4.md:1552:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1555:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:1556:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1564:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
docs/reviews/pr250-codereview-codex-5-4.md:1571:+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
docs/reviews/pr250-codereview-codex-5-4.md:1578:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1580:+        _advanceToTick(winterStart + 7);
docs/reviews/pr250-codereview-codex-5-4.md:1582:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:1590:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "cold should mark clan dead");
docs/reviews/pr250-codereview-codex-5-4.md:1597:+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
docs/reviews/pr250-codereview-codex-5-4.md:1605:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1606:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:1607:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1619:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1622:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:1623:+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1628:+        assertEq(uint8(clanB.clanState), uint8(ClanState.DEAD), "clan B should be dead");
docs/reviews/pr250-codereview-codex-5-4.md:1637:+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
docs/reviews/pr250-codereview-codex-5-4.md:1641:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1642:+        _advanceToTick(winterEnd + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1644:+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
docs/reviews/pr250-codereview-codex-5-4.md:1649:+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:1650:+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:1651:+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
docs/reviews/pr250-codereview-codex-5-4.md:1652:+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
docs/reviews/pr250-codereview-codex-5-4.md:1660:-        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:1663:+        // winter is derived from the global recurring schedule
docs/reviews/pr250-codereview-codex-5-4.md:1665:         assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-4.md:1677:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1680:-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-4.md:1681:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:1682:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1683:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:1691:  → Gather contract, test, and spec context around winter, starvation, elimination, and dead-clan rules
docs/reviews/pr250-codereview-codex-5-4.md:1692:  • Delegate focused parallel checks for boundary timing and cross-phase dead-clan interactions
docs/reviews/pr250-codereview-codex-5-4.md:1816:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:1899:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:1900:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:1901:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:1938:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:1940:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:1961:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:1962:packages/contracts/src/IClanWorld.sol:203:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:1963:packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:1964:packages/contracts/src/IClanWorld.sol:205:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:1965:packages/contracts/src/IClanWorld.sol:242:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:1966:packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:1967:packages/contracts/src/IClanWorld.sol:418:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:1968:packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:1969:packages/contracts/src/IClanWorld.sol:420:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:1971:packages/contracts/src/IClanWorld.sol:618:    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:1974:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:1978:packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1979:packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-4.md:1980:packages/contracts/test/ClanWorldStub.t.sol:50:        assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2013:packages/contracts/test/ClanWorld.t.sol:2020:    // Phase 4.4 — season + winter timer tests
docs/reviews/pr250-codereview-codex-5-4.md:2015:packages/contracts/test/ClanWorld.t.sol:2029:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2016:packages/contracts/test/ClanWorld.t.sol:2032:        assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2017:packages/contracts/test/ClanWorld.t.sol:2035:    function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:2018:packages/contracts/test/ClanWorld.t.sol:2036:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:2019:packages/contracts/test/ClanWorld.t.sol:2037:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:2020:packages/contracts/test/ClanWorld.t.sol:2039:        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2021:packages/contracts/test/ClanWorld.t.sol:2041:        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2022:packages/contracts/test/ClanWorld.t.sol:2051:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-4.md:2023:packages/contracts/test/ClanWorld.t.sol:2054:            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-4.md:2024:packages/contracts/test/ClanWorld.t.sol:2060:        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:2025:packages/contracts/test/ClanWorld.t.sol:2061:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:2026:packages/contracts/test/ClanWorld.t.sol:2064:    function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-4.md:2027:packages/contracts/test/ClanWorld.t.sol:2065:        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-4.md:2028:packages/contracts/test/ClanWorld.t.sol:2066:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2029:packages/contracts/test/ClanWorld.t.sol:2067:        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2031:packages/contracts/test/ClanWorld.t.sol:2072:        assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-4.md:2032:packages/contracts/test/ClanWorld.t.sol:2073:        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-4.md:2033:packages/contracts/test/ClanWorld.t.sol:2075:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2035:packages/contracts/test/ClanWorld.t.sol:2090:        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:2036:packages/contracts/test/ClanWorld.t.sol:2093:        assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-4.md:2039:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:2040:packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2041:packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2042:packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2043:packages/contracts/src/ClanWorld.sol:138:        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2045:packages/contracts/src/ClanWorld.sol:443:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2046:packages/contracts/src/ClanWorld.sol:465:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2047:packages/contracts/src/ClanWorld.sol:466:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2048:packages/contracts/src/ClanWorld.sol:468:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2049:packages/contracts/src/ClanWorld.sol:469:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2050:packages/contracts/src/ClanWorld.sol:476:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2051:packages/contracts/src/ClanWorld.sol:909:    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2052:packages/contracts/src/ClanWorld.sol:934:        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2053:packages/contracts/src/ClanWorld.sol:952:            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr250-codereview-codex-5-4.md:2054:packages/contracts/src/ClanWorld.sol:979:            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:2055:packages/contracts/src/ClanWorld.sol:980:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2056:packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2057:packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2058:packages/contracts/src/ClanWorld.sol:986:        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:2059:packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:2060:packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:2061:packages/contracts/src/ClanWorld.sol:991:            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:2062:packages/contracts/src/ClanWorld.sol:994:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2063:packages/contracts/src/ClanWorld.sol:995:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2064:packages/contracts/src/ClanWorld.sol:997:            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:2065:packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2066:packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2067:packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2068:packages/contracts/src/ClanWorld.sol:1003:                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2069:packages/contracts/src/ClanWorld.sol:1005:                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:2070:packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2071:packages/contracts/src/ClanWorld.sol:1007:                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2073:packages/contracts/src/ClanWorld.sol:1066:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2077:packages/contracts/src/ClanWorld.sol:2221:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:2078:packages/contracts/src/ClanWorld.sol:2225:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2079:packages/contracts/src/ClanWorld.sol:2414:            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2080:packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2081:packages/contracts/src/ClanWorld.sol:2416:            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2082:packages/contracts/src/ClanWorld.sol:2423:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:2083:packages/contracts/src/ClanWorld.sol:2428:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/shared/src/mocks/clanWorldFixture.ts:10://   1. Aldric (clan 0) is one tick from starvation (vaultWheat 1e18, 2 living
packages/shared/src/mocks/clanWorldFixture.ts:46:  /** Cached starvation flag per v4 §4.13 (lazy starvation tracking). */
docs/reviews/pr194-r2-codereview-codex-5-5.md:23:3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
docs/reviews/pr194-r2-codereview-codex-5-5.md:402:               "name": "winterActive",
docs/reviews/pr194-r2-codereview-codex-5-5.md:740:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-5.md:742:-        _world.winterStartsAtTick =
docs/reviews/pr194-r2-codereview-codex-5-5.md:744:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:745:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-5.md:746:         _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-5.md:758:+            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-5.md:763:             clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:767:+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:776:+        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-5.md:778:+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:780:+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-5.md:781:+            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-5.md:794:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-5.md:828:+        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-5.md:831:+        clan.clanState = ClanState.DEAD;
docs/reviews/pr194-r2-codereview-codex-5-5.md:836:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1112:+            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1137:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1138:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1139:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1140:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1143:+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1176:+        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1178:+        sim.clan.clanState = ClanState.DEAD;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1183:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1258:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1749:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1813:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1823:+        if (targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1832:+                || targetClan.clanState == ClanState.DEAD
docs/reviews/pr194-r2-codereview-codex-5-5.md:1958:+        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1988:+            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:2287:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:2401:+            if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:2476:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2507:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2517:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2556:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:2615:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2619:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:2691:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:2723:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr194-r2-codereview-codex-5-5.md:2728:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3254:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3262:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3554:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3555:+        _advanceUntil(winterStart + 4);
docs/reviews/pr194-r2-codereview-codex-5-5.md:3574:+        uint64 deathFromTick = winterStart;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3587:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-r2-codereview-codex-5-5.md:3685:+    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr194-r2-codereview-codex-5-5.md:3686:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3687:+        _advanceUntil(winterStart + 30);
docs/reviews/pr194-r2-codereview-codex-5-5.md:3688:+        assertFalse(world.getWorldState().winterActive, "test settles after winter");
docs/reviews/pr194-r2-codereview-codex-5-5.md:3691:+        world.setClanUpkeepState(clanId, winterStart, 0, 0);
docs/reviews/pr194-r2-codereview-codex-5-5.md:3694:+        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
docs/reviews/pr194-r2-codereview-codex-5-5.md:3700:+        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
docs/reviews/pr194-r2-codereview-codex-5-5.md:3705:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:3706:+        _advanceUntil(winterStart + 1);
docs/reviews/pr194-r2-codereview-codex-5-5.md:3709:+        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
docs/reviews/pr194-r2-codereview-codex-5-5.md:3716:+        assertEq(uint8(world.getClan(targetClanId).clanState), uint8(ClanState.DEAD), "target starved");
docs/reviews/pr194-r2-codereview-codex-5-5.md:3856:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4374:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4380:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4442:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4443:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4486:-            ws.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-5.md:4488:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr194-r2-codereview-codex-5-5.md:4490:         assertFalse(ws.winterActive);
docs/reviews/pr194-r2-codereview-codex-5-5.md:4493:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:4494:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr194-r2-codereview-codex-5-5.md:4496:-        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-5.md:4498:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:4499:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:4746:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4747:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4748:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4813:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4814:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4815:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4864:-                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5069:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5070:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5071:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5087:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5088:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5089:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5125:+        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-5.md:5126:+        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:5127:+        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:5141:+    expect(state.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-5.md:5144:+  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5153:+        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-5.md:5154:+        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:5155:+        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:5170:+    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-5.md:5398:packages/shared/test/clanWorldAbi.test.ts:97:  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5423:/bin/bash -lc 'rg -n "test_e2e_banditLifecycle|test_winterStarvationReplay|test_resolveBanditAttackReturns|test_defeatedBanditAwardsBlueprint|test_deadTargetCleanup|test_heartbeat_scheduledMarketObserves" packages/contracts/test' in /home/claude/code/clan-world
docs/reviews/pr194-r2-codereview-codex-5-5.md:5428:packages/contracts/test/BanditAttackResolution.t.sol:464:    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5455:   420	            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5478:   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr194-r2-codereview-codex-5-5.md:5500:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5501:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5503:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5504:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5514:  1074	            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5539:  1099	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5540:  1100	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5541:  1101	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5542:  1102	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5545:  1105	        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5578:  1138	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5580:  1140	        sim.clan.clanState = ClanState.DEAD;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5585:  1145	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5660:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5841:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5905:  1775	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5920:  1785	        if (targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5929:  1794	                || targetClan.clanState == ClanState.DEAD
docs/reviews/pr194-r2-codereview-codex-5-5.md:6101:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6102:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6104:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6105:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6109:   473	        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6118:   482	        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-5.md:6120:   484	        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6122:   486	        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-5.md:6123:   487	            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6136:   500	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-5.md:6170:   534	        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6221:  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:6374:  3472	            winterActive: _world.winterActive,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6375:  3473	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6376:  3474	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
docs/reviews/pr194-r2-codereview-codex-5-5.md:6393:473:        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6395:612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6404:1105:        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6406:1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6411:1920:        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6412:1950:            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6413:3415:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6585:   202	    bool winterActive;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6586:   203	    uint64 winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6587:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr194-r2-codereview-codex-5-5.md:6639:   414	    bool winterActive;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6640:   415	    uint64 winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6641:   416	    uint64 winterEndsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6881:   138	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-5.md:6883:   140	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:6884:   141	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-5.md:6885:   142	        _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6906:   612	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6922:  2354	            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6949:  2381	            // reset winter timers for new season
docs/reviews/pr194-r2-codereview-codex-5-5.md:6950:  2382	            _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6951:  2383	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr194-r2-codereview-codex-5-5.md:6953:  2385	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6956:  2388	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr194-r2-codereview-codex-5-5.md:6958:  2390	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr194-r2-codereview-codex-5-5.md:6959:  2391	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr194-r2-codereview-codex-5-5.md:6961:  2393	            _world.winterActive = true;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6964:  2396	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6965:  2397	            _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6967:  2399	            // schedule next winter cycle within this season
docs/reviews/pr194-r2-codereview-codex-5-5.md:6968:  2400	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr194-r2-codereview-codex-5-5.md:6970:  2402	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6972:  2404	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6973:  2405	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6975:  2407	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr194-r2-codereview-codex-5-5.md:6976:  2408	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6977:  2409	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7197:    22	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:7198:    23	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:7199:    24	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:7215:    40	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:7216:    41	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:7217:    42	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:7253:    78	        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-5.md:7254:    79	        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:7255:    80	        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:7269:    94	    expect(state.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7272:    97	  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr194-r2-codereview-codex-5-5.md:7281:   106	        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-5.md:7282:   107	        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:7283:   108	        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-5.md:7298:   123	    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7561:getWorldState ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","nextHeartbeatAtTs:uint64","nextBanditSpawnEligibleTick:uint64","currentBanditSpawnChanceBps:uint16","currentTickSeed:bytes32","activeBanditId:uint32","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","nextCommitSequence:uint64"]
docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr194-r2-codereview-codex-5-5.md:7580:packages/contracts/src/ClanWorld.sol:138:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-5.md:7581:packages/contracts/src/ClanWorld.sol:140:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:7582:packages/contracts/src/ClanWorld.sol:141:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-5.md:7588:packages/contracts/src/ClanWorld.sol:484:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7589:packages/contracts/src/ClanWorld.sol:486:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-5.md:7610:packages/contracts/src/ClanWorld.sol:2383:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr194-r2-codereview-codex-5-5.md:7612:packages/contracts/src/ClanWorld.sol:2385:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7613:packages/contracts/src/ClanWorld.sol:2400:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr194-r2-codereview-codex-5-5.md:7615:packages/contracts/src/ClanWorld.sol:2402:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7698:   333	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7699:   334	        _advanceUntil(winterStart + 4);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7718:   353	        uint64 deathFromTick = winterStart;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7731:   366	        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-r2-codereview-codex-5-5.md:7829:   464	    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr194-r2-codereview-codex-5-5.md:7830:   465	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7831:   466	        _advanceUntil(winterStart + 30);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7832:   467	        assertFalse(world.getWorldState().winterActive, "test settles after winter");
docs/reviews/pr194-r2-codereview-codex-5-5.md:7835:   470	        world.setClanUpkeepState(clanId, winterStart, 0, 0);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7838:   473	        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
docs/reviews/pr194-r2-codereview-codex-5-5.md:7844:   479	        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
docs/reviews/pr194-r2-codereview-codex-5-5.md:7849:   484	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7850:   485	        _advanceUntil(winterStart + 1);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7853:   488	        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7860:   495	        assertEq(uint8(world.getClan(targetClanId).clanState), uint8(ClanState.DEAD), "target starved");
docs/reviews/pr194-r2-codereview-codex-5-5.md:8132:  2191	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:8183:  2242	            if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:8454:  3127	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:8578:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:8596:  1785	        if (targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
packages/shared/src/adapters/IChainClient.ts:39:          { name: 'winterActive', type: 'bool' },
packages/shared/src/adapters/IChainClient.ts:40:          { name: 'winterStartsAtTick', type: 'uint64' },
packages/shared/src/adapters/IChainClient.ts:41:          { name: 'winterEndsAtTick', type: 'uint64' },
packages/shared/src/adapters/IChainClient.ts:90:                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr200-codereview-codex-5-5.md:17:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:741:               "name": "winterActive",
docs/reviews/pr200-codereview-codex-5-5.md:1718:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr200-codereview-codex-5-5.md:1720:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-5.md:1722:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-5.md:1723:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr200-codereview-codex-5-5.md:1724:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-5.md:1733:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:1871:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-5.md:1882:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1885:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1911:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1912:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1947:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1950:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1980:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:1981:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2038:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2041:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2061:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2062:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2099:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2102:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2135:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2136:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-5.md:2209:         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:2316:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-5.md:2318:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr200-codereview-codex-5-5.md:2319:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr200-codereview-codex-5-5.md:2320:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-5.md:2323:     // OTC transfers
docs/reviews/pr200-codereview-codex-5-5.md:2557:     // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-5.md:2638:     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-5.md:3547:-            ws.winterStartsAtTick,
docs/reviews/pr200-codereview-codex-5-5.md:3549:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr200-codereview-codex-5-5.md:3551:         assertFalse(ws.winterActive);
docs/reviews/pr200-codereview-codex-5-5.md:3554:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr200-codereview-codex-5-5.md:3555:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr200-codereview-codex-5-5.md:3557:-        uint64 winterStart =
docs/reviews/pr200-codereview-codex-5-5.md:3559:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-5.md:3560:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr200-codereview-codex-5-5.md:3634:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-5.md:3654:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-5.md:3678:+        world.setClanState(clanB, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-5.md:3702:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-5.md:3721:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr200-codereview-codex-5-5.md:5076:/bin/bash -lc 'rg -n "DEAD|clanState|starvation|dead|ClanState.DEAD|propose.*Transfer|accept.*Transfer|zero|expiry" packages/contracts/test packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-5.md:5082:packages/contracts/src/ClanWorld.sol:397:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr200-codereview-codex-5-5.md:5083:packages/contracts/src/ClanWorld.sol:419:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr200-codereview-codex-5-5.md:5084:packages/contracts/src/ClanWorld.sol:420:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-5.md:5085:packages/contracts/src/ClanWorld.sol:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr200-codereview-codex-5-5.md:5086:packages/contracts/src/ClanWorld.sol:423:            clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-5.md:5087:packages/contracts/src/ClanWorld.sol:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:5090:packages/contracts/src/ClanWorld.sol:919:            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr200-codereview-codex-5-5.md:5094:packages/contracts/src/ClanWorld.sol:1033:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-5.md:5100:packages/contracts/src/ClanWorld.sol:2297:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr200-codereview-codex-5-5.md:5101:packages/contracts/src/ClanWorld.sol:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:5103:packages/contracts/src/ClanWorld.sol:2401:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr200-codereview-codex-5-5.md:5104:packages/contracts/src/ClanWorld.sol:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:5158:The live tree already shows one interesting mismatch: `rg` only found the legacy `transferGold` surface, while the pasted diff adds the OTC functions. I’m going to verify whether this workspace is on an older local checkout, and I’ll still review the supplied diff as authoritative for PR head.
docs/reviews/pr200-codereview-codex-5-5.md:5222:    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-5.md:5226:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:5230:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:5234:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:5242:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:5273:I’m on local `9f93593`, not the supplied `0c20b46`, so I’ll avoid treating the checkout as the source of truth for the new OTC code. I found prior PR200 review artifacts in `docs/reviews`, and I’m pulling those in as extra signal while the subagents do independent passes on the diff.
docs/reviews/pr200-codereview-codex-5-5.md:5297:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:5521:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:5949:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-5.md:5951:docs/reviews/pr200-codereview-codex-5-4.md:4723:docs/reviews/pr198-codereview-codex-5-4.md:9397:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-5.md:5952:docs/reviews/pr200-codereview-codex-5-4.md:4772:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-5.md:5953:docs/reviews/pr200-codereview-codex-5-4.md:4777:docs/planning/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-5.md:5965:docs/reviews/pr200-codereview-codex-5-4.md:4827:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
docs/reviews/pr200-codereview-codex-5-5.md:5973:docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:5974:docs/reviews/pr200-codereview-codex-5-4.md:4852:docs/planning/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:5980:docs/reviews/pr200-codereview-codex-5-4.md:4862:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-5.md:5981:docs/reviews/pr200-codereview-codex-5-4.md:4866:docs/planning/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-5.md:5996:docs/reviews/pr200-codereview-codex-5-4.md:4903:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-5.md:6004:docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6005:docs/reviews/pr200-codereview-codex-5-4.md:4926:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6017:docs/reviews/pr200-codereview-codex-5-4.md:5009:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6023:docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6055:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/reviews/pr200-codereview-codex-5-5.md:6056:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/reviews/pr200-codereview-codex-5-5.md:6057:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6058:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:563:11. OTC  
docs/reviews/pr200-codereview-codex-5-5.md:6063:packages/contracts/src/ClanWorld.sol:2115:    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-5.md:6064:packages/contracts/src/ClanWorld.sol:2119:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:6065:packages/contracts/src/ClanWorld.sol:2123:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:6066:packages/contracts/src/ClanWorld.sol:2127:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:6067:packages/contracts/src/ClanWorld.sol:2135:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-5.md:6068:packages/contracts/src/IClanWorld.sol:629:    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-5.md:6069:packages/contracts/src/IClanWorld.sol:687:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-5.md:6070:packages/contracts/src/ClanWorldStub.sol:117:    // OTC transfers
docs/reviews/pr200-codereview-codex-5-5.md:6132:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6161:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6181:OTC transfers may draw only from:
docs/reviews/pr200-codereview-codex-5-5.md:6186:OTC transfers may **not** draw from worker carry balances.
docs/reviews/pr200-codereview-codex-5-5.md:6278:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-5.md:6281:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr200-codereview-codex-5-5.md:6286:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-5.md:6293:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-5.md:6296:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/reviews/pr200-codereview-codex-5-5.md:6316:- winter transition checks
docs/reviews/pr200-codereview-codex-5-5.md:6338:/bin/bash -lc "rg -n \"mintClan\\(|goldBalance|blueprintBalance|clanState = ClanState.DEAD|clanState == ClanState.DEAD|_poolForResourceType|seedPools\\(|transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|settleClansman\\(|DepositResources|CLANSMAN_CARRY_CAP|WOOD_YIELD_PER_TICK|ResourceMinted|ResourceBurned\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-5.md:6951:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-5.md:7098:Top concern: OTC proposals are not bound to the proposer owner or an ownership nonce. If clan ownership can change after proposal creation, an old proposal can debit the new owner’s clan when accepted. Secondary concerns: the canonical direct transfer functions still exist but revert, and this PR includes unrelated ABI/event changes that can break indexers.
docs/reviews/pr200-codereview-codex-5-5.md:7103:  `packages/contracts/src/ClanWorld.sol` OTC `propose*Transfer` / `accept*Transfer`; `packages/contracts/src/IClanWorld.sol` proposal structs.  
docs/reviews/pr200-codereview-codex-5-5.md:7108:- **Canonical direct OTC functions remain public but revert.**  
docs/reviews/pr200-codereview-codex-5-5.md:7110:  The interface still advertises the old transfer surface, but implementation returns `"OTC transfers not implemented"`. If proposer/acceptor is now the only supported surface, remove or clearly supersede these functions; otherwise Phase 7 still has callable transfer methods that fail.
docs/reviews/pr200-codereview-codex-5-5.md:7112:- **Gold transfer expiry ABI is inconsistent with the rest of OTC.**  
docs/reviews/pr200-codereview-codex-5-5.md:7113:  `proposeGoldTransfer` / `GoldTransferProposed` use `uint256 expiryTick`, while stored proposals and all other OTC proposal APIs use `uint64`. The implementation bounds and casts, but event/getter data types disagree. Make gold match the other proposal types.
docs/reviews/pr200-codereview-codex-5-5.md:7117:  The event changed from `uint64 atTick` to `uint32 tick`, with a cast from world `uint64`. This breaks existing event consumers and will wrap ticks above `uint32`. It is also outside the OTC scope.
docs/reviews/pr200-codereview-codex-5-5.md:7121:- **Individual OTC proposals allow empty/no-op transfers.**  
docs/reviews/pr200-codereview-codex-5-5.md:7127:- **Stub OTC lifecycle is too thin to catch integration bugs.**  
docs/reviews/pr200-codereview-codex-5-5.md:7137:- The PR mixes OTC work with pool/token/deposit/gathering ABI and behavior changes, which makes final merge risk higher than Phase 7 alone.
docs/reviews/pr200-codereview-codex-5-5.md:7149:Top concern: OTC proposals are not bound to the proposer owner or an ownership nonce. If clan ownership can change after proposal creation, an old proposal can debit the new owner’s clan when accepted. Secondary concerns: the canonical direct transfer functions still exist but revert, and this PR includes unrelated ABI/event changes that can break indexers.
docs/reviews/pr200-codereview-codex-5-5.md:7154:  `packages/contracts/src/ClanWorld.sol` OTC `propose*Transfer` / `accept*Transfer`; `packages/contracts/src/IClanWorld.sol` proposal structs.  
docs/reviews/pr200-codereview-codex-5-5.md:7159:- **Canonical direct OTC functions remain public but revert.**  
docs/reviews/pr200-codereview-codex-5-5.md:7161:  The interface still advertises the old transfer surface, but implementation returns `"OTC transfers not implemented"`. If proposer/acceptor is now the only supported surface, remove or clearly supersede these functions; otherwise Phase 7 still has callable transfer methods that fail.
docs/reviews/pr200-codereview-codex-5-5.md:7163:- **Gold transfer expiry ABI is inconsistent with the rest of OTC.**  
docs/reviews/pr200-codereview-codex-5-5.md:7164:  `proposeGoldTransfer` / `GoldTransferProposed` use `uint256 expiryTick`, while stored proposals and all other OTC proposal APIs use `uint64`. The implementation bounds and casts, but event/getter data types disagree. Make gold match the other proposal types.
docs/reviews/pr200-codereview-codex-5-5.md:7168:  The event changed from `uint64 atTick` to `uint32 tick`, with a cast from world `uint64`. This breaks existing event consumers and will wrap ticks above `uint32`. It is also outside the OTC scope.
docs/reviews/pr200-codereview-codex-5-5.md:7172:- **Individual OTC proposals allow empty/no-op transfers.**  
docs/reviews/pr200-codereview-codex-5-5.md:7178:- **Stub OTC lifecycle is too thin to catch integration bugs.**  
docs/reviews/pr200-codereview-codex-5-5.md:7188:- The PR mixes OTC work with pool/token/deposit/gathering ABI and behavior changes, which makes final merge risk higher than Phase 7 alone.
docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr137-review-claude-opus.md:134:5. **#5, #6 Stale comments** — Quick fix: update NatSpec and OTC revert strings
docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-5.md:22:1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
docs/reviews/pr250-codereview-codex-5-5.md:23:2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
docs/reviews/pr250-codereview-codex-5-5.md:25:4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
docs/reviews/pr250-codereview-codex-5-5.md:26:5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
docs/reviews/pr250-codereview-codex-5-5.md:27:6. Test coverage on winter boundary edges + clan-death paths
docs/reviews/pr250-codereview-codex-5-5.md:243:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-5.md:463:+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
docs/reviews/pr250-codereview-codex-5-5.md:472:-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-5.md:474:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-5.md:476:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-5.md:477:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:478:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:479:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-5.md:486:+            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/pr250-codereview-codex-5-5.md:502:-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-5.md:503:+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
docs/reviews/pr250-codereview-codex-5-5.md:505:+        bool winter = _isWinterActiveAt(tick);
docs/reviews/pr250-codereview-codex-5-5.md:506:+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
docs/reviews/pr250-codereview-codex-5-5.md:514:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-5.md:522:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-5.md:526:+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
docs/reviews/pr250-codereview-codex-5-5.md:527:+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-5.md:530:+        if (clan.clanState == ClanState.DEAD) return;
docs/reviews/pr250-codereview-codex-5-5.md:532:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-5.md:615:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-5.md:653:+        if (clan.clanId == 0 || clan.clanState == ClanState.DEAD) return;
docs/reviews/pr250-codereview-codex-5-5.md:655:+        clan.clanState = ClanState.DEAD;
docs/reviews/pr250-codereview-codex-5-5.md:660:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-5.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-5.md:696:-            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-5.md:697:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-5.md:698:-            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-5.md:700:-            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-5.md:703:         // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-5.md:705:-            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-5.md:706:-                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-5.md:708:-            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-5.md:711:-        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-5.md:712:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-5.md:714:-            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-5.md:715:-            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-5.md:717:-            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-5.md:719:-                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-5.md:720:-                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-5.md:722:-                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-5.md:723:-                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-5.md:724:-                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-5.md:729:+            emit WinterStarted(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-5.md:734:+            emit WinterEnded(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-5.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/pr250-codereview-codex-5-5.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-5.md:773:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-5.md:778:+        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-5.md:791:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-5.md:812:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-codereview-codex-5-5.md:860:+        if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr250-codereview-codex-5-5.md:916:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-5.md:917:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:918:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:927:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-5.md:928:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:929:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:943:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-5.md:945:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-5.md:946:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:947:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:948:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-5.md:958:+            emit WinterStarted(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-5.md:961:+            emit WinterEnded(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-5.md:1001:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-5.md:1002:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1003:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1006:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-5.md:1007:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1008:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1025:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-5.md:1026:+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-5.md:1031:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-5.md:1052:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-5.md:1131:+    /// @notice True iff currentTick is inside the recurring winter window.
docs/reviews/pr250-codereview-codex-5-5.md:1282:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1285:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-5.md:1286:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1287:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-5.md:1293:+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-5.md:1294:+        assertEq(snapshot.winterStartsAtTick, 110);
docs/reviews/pr250-codereview-codex-5-5.md:1297:     function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-5.md:1298:-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-5.md:1299:-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-5.md:1301:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-5.md:1303:-        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-5.md:1309:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1310:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-5.md:1318:-        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-5.md:1321:-            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-5.md:1327:-        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-5.md:1328:-        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-5.md:1330:+        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
docs/reviews/pr250-codereview-codex-5-5.md:1334:+        assertTrue(world.isWinter(), "winter should be active past start tick");
docs/reviews/pr250-codereview-codex-5-5.md:1335:+        assertTrue(ws.winterActive, "world state should report winter active");
docs/reviews/pr250-codereview-codex-5-5.md:1336:+        assertEq(ws.winterStartsAtTick, winterStart);
docs/reviews/pr250-codereview-codex-5-5.md:1337:+        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1340:     function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-5.md:1341:-        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-5.md:1342:-        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-5.md:1343:-        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-5.md:1348:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1349:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-5.md:1358:         assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-5.md:1359:-        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-5.md:1361:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1364:+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
docs/reviews/pr250-codereview-codex-5-5.md:1366:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1369:+    function test_winter_restarts_after_full_period() public {
docs/reviews/pr250-codereview-codex-5-5.md:1380:+        assertTrue(world.isWinter(), "winter should be active in next period");
docs/reviews/pr250-codereview-codex-5-5.md:1384:+    function test_winter_cropTransitions_lockThenRestartRegrow() public {
docs/reviews/pr250-codereview-codex-5-5.md:1392:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1393:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-5.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-5.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-5.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-5.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-5.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/pr250-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/pr250-codereview-codex-5-5.md:1407:+        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
docs/reviews/pr250-codereview-codex-5-5.md:1409:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1410:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-5.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/pr250-codereview-codex-5-5.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/pr250-codereview-codex-5-5.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-5.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-5.md:1439:+    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
docs/reviews/pr250-codereview-codex-5-5.md:1441:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1442:+        _advanceToTick(winterStart - 2);
docs/reviews/pr250-codereview-codex-5-5.md:1446:+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
docs/reviews/pr250-codereview-codex-5-5.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/pr250-codereview-codex-5-5.md:1460:+    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
docs/reviews/pr250-codereview-codex-5-5.md:1464:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1465:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1468:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1473:+        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-5.md:1474:+        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-5.md:1475:+        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
docs/reviews/pr250-codereview-codex-5-5.md:1481:+    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
docs/reviews/pr250-codereview-codex-5-5.md:1485:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1486:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1488:+        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1491:+        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
docs/reviews/pr250-codereview-codex-5-5.md:1495:+        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
docs/reviews/pr250-codereview-codex-5-5.md:1496:+        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
docs/reviews/pr250-codereview-codex-5-5.md:1503:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1504:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1507:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-5.md:1510:+        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
docs/reviews/pr250-codereview-codex-5-5.md:1523:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1524:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1526:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-5.md:1548:+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
docs/reviews/pr250-codereview-codex-5-5.md:1552:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1555:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-5.md:1556:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1564:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
docs/reviews/pr250-codereview-codex-5-5.md:1571:+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
docs/reviews/pr250-codereview-codex-5-5.md:1578:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1580:+        _advanceToTick(winterStart + 7);
docs/reviews/pr250-codereview-codex-5-5.md:1582:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-5.md:1590:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "cold should mark clan dead");
docs/reviews/pr250-codereview-codex-5-5.md:1597:+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
docs/reviews/pr250-codereview-codex-5-5.md:1605:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1606:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-5.md:1607:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1619:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1622:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-5.md:1623:+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1628:+        assertEq(uint8(clanB.clanState), uint8(ClanState.DEAD), "clan B should be dead");
docs/reviews/pr250-codereview-codex-5-5.md:1637:+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
docs/reviews/pr250-codereview-codex-5-5.md:1641:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1642:+        _advanceToTick(winterEnd + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1644:+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
docs/reviews/pr250-codereview-codex-5-5.md:1649:+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-5.md:1650:+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-5.md:1651:+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
docs/reviews/pr250-codereview-codex-5-5.md:1652:+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
docs/reviews/pr250-codereview-codex-5-5.md:1660:-        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-5.md:1663:+        // winter is derived from the global recurring schedule
docs/reviews/pr250-codereview-codex-5-5.md:1665:         assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-5.md:1677:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1680:-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-5.md:1681:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-5.md:1682:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1683:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
docs/reviews/pr194-r2-codereview-codex-5-4.md:23:3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
docs/reviews/pr194-r2-codereview-codex-5-4.md:402:               "name": "winterActive",
docs/reviews/pr194-r2-codereview-codex-5-4.md:740:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-4.md:742:-        _world.winterStartsAtTick =
docs/reviews/pr194-r2-codereview-codex-5-4.md:744:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-4.md:745:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-4.md:746:         _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-4.md:758:+            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-4.md:763:             clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:767:+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:776:+        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-4.md:778:+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-4.md:780:+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-4.md:781:+            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-4.md:794:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-4.md:828:+        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-4.md:831:+        clan.clanState = ClanState.DEAD;
docs/reviews/pr194-r2-codereview-codex-5-4.md:836:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1112:+            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1137:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1138:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1139:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1140:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1143:+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1176:+        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1178:+        sim.clan.clanState = ClanState.DEAD;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1183:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1258:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1749:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1813:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1823:+        if (targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1832:+                || targetClan.clanState == ClanState.DEAD
docs/reviews/pr194-r2-codereview-codex-5-4.md:1958:+        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1988:+            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:2287:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:2401:+            if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:2476:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2507:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2517:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2556:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:2615:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2619:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:2691:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:2723:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr194-r2-codereview-codex-5-4.md:2728:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3254:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3262:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3554:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3555:+        _advanceUntil(winterStart + 4);
docs/reviews/pr194-r2-codereview-codex-5-4.md:3574:+        uint64 deathFromTick = winterStart;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3587:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-r2-codereview-codex-5-4.md:3685:+    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr194-r2-codereview-codex-5-4.md:3686:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3687:+        _advanceUntil(winterStart + 30);
docs/reviews/pr194-r2-codereview-codex-5-4.md:3688:+        assertFalse(world.getWorldState().winterActive, "test settles after winter");
docs/reviews/pr194-r2-codereview-codex-5-4.md:3691:+        world.setClanUpkeepState(clanId, winterStart, 0, 0);
docs/reviews/pr194-r2-codereview-codex-5-4.md:3694:+        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
docs/reviews/pr194-r2-codereview-codex-5-4.md:3700:+        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
docs/reviews/pr194-r2-codereview-codex-5-4.md:3705:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:3706:+        _advanceUntil(winterStart + 1);
docs/reviews/pr194-r2-codereview-codex-5-4.md:3709:+        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
docs/reviews/pr194-r2-codereview-codex-5-4.md:3716:+        assertEq(uint8(world.getClan(targetClanId).clanState), uint8(ClanState.DEAD), "target starved");
docs/reviews/pr194-r2-codereview-codex-5-4.md:3856:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4374:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4380:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4442:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4443:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4486:-            ws.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-4.md:4488:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr194-r2-codereview-codex-5-4.md:4490:         assertFalse(ws.winterActive);
docs/reviews/pr194-r2-codereview-codex-5-4.md:4493:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr194-r2-codereview-codex-5-4.md:4494:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr194-r2-codereview-codex-5-4.md:4496:-        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-4.md:4498:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-4.md:4499:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:4746:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4747:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4748:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4813:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4814:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4815:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4864:-                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5069:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5070:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5071:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5087:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5088:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5089:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5125:+        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5126:+        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5127:+        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5141:+    expect(state.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-4.md:5144:+  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5153:+        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5154:+        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5155:+        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:5170:+    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
docs/reviews/pr194-r2-codereview-codex-5-4.md:5203:  → Inspect changed Solidity bandit/heartbeat/starvation code in context and identify behavioral regressions
docs/reviews/pr194-r2-codereview-codex-5-4.md:5315:    bool winterActive;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5316:    uint64 winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5317:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr194-r2-codereview-codex-5-4.md:5354:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr194-r2-codereview-codex-5-4.md:5356:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr194-r2-codereview-codex-5-4.md:5445:    // ----- winter cold damage -----
docs/reviews/pr194-r2-codereview-codex-5-4.md:5449:    // ----- OTC transfers -----
docs/reviews/pr194-r2-codereview-codex-5-4.md:5514:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5590:            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5654:        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5675:    bool winterActive;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5676:    uint64 winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5677:    uint64 winterEndsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5812:            if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5902:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr194-r2-codereview-codex-5-4.md:5924:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5925:            clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5927:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5928:            clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5932:        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5941:        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-4.md:5943:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5945:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-4.md:5946:            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5959:                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-4.md:5964:            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5989:        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5990:            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5991:        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5992:            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5995:        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:6028:        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6030:        sim.clan.clanState = ClanState.DEAD;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6035:        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6110:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6177:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6196:  2242	            if (clan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:6450:  3415	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6879:    22	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:6880:    23	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:6881:    24	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:6897:    40	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:6898:    41	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:6899:    42	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:6935:    78	        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6936:    79	        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6937:    80	        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6951:    94	    expect(state.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-4.md:6954:    97	  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr194-r2-codereview-codex-5-4.md:6963:   106	        winterActive: true,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6964:   107	        winterStartsAtTick: 100n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6965:   108	        winterEndsAtTick: 110n,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6980:   123	    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr194-r2-codereview-codex-5-4.md:6995:  3472	            winterActive: _world.winterActive,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6996:  3473	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-4.md:6997:  3474	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-4.md:7039:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7103:  1775	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7113:  1785	        if (targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7122:  1794	                || targetClan.clanState == ClanState.DEAD
docs/reviews/pr194-r2-codereview-codex-5-4.md:7260:  1920	        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7290:  1950	            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7465:   612	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7721:   420	            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7998:612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7999:1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:8038:3415:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:8109:  3127	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:8167:  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:8182:The source pass is down to two likely review items: one in view-simulation starvation handling, and one possible stale-state issue during target selection before a camped bandit attacks. I’m waiting on the parallel passes now to see whether they independently confirm those or uncover anything else in the ABI/tests lane.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8284:  2191	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8313:  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8349:  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
docs/reviews/r2/pr194-codereview-codex-5-5.md:430:               "name": "winterActive",
docs/reviews/r2/pr194-codereview-codex-5-5.md:760:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/r2/pr194-codereview-codex-5-5.md:762:-        _world.winterStartsAtTick =
docs/reviews/r2/pr194-codereview-codex-5-5.md:764:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-5.md:765:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/r2/pr194-codereview-codex-5-5.md:766:         _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-5.md:778:+            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-5.md:783:             clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:787:+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:802:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-5.md:836:+        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-5.md:839:+        clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-5.md:844:+        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1120:+            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1145:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1146:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1147:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1148:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1151:+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1184:+        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1186:+        sim.clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1191:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1266:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1771:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1901:+        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1931:+            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2230:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2344:+            if (clan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2419:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2450:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2460:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2499:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2558:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2562:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:2634:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:2666:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/r2/pr194-codereview-codex-5-5.md:2671:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:3196:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:3206:+        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:3444:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:3445:+        _advanceUntil(winterStart + 4);
docs/reviews/r2/pr194-codereview-codex-5-5.md:3446:+        uint64 deathFromTick = winterStart;
docs/reviews/r2/pr194-codereview-codex-5-5.md:3459:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-5.md:3684:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4180:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4186:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4248:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4249:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/r2/pr194-codereview-codex-5-5.md:4585:    46	///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/r2/pr194-codereview-codex-5-5.md:4847:   287	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/r2/pr194-codereview-codex-5-5.md:4869:   309	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:4870:   310	            clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:4872:   312	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:4873:   313	            clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:4880:   320	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5053:   118	    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-5.md:5133:   198	    bool   winterActive;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5134:   199	    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5135:   200	    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/r2/pr194-codereview-codex-5-5.md:5172:   237	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/r2/pr194-codereview-codex-5-5.md:5174:   239	    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/r2/pr194-codereview-codex-5-5.md:5428:docs/reviews/pr194-codereview-codex-5-5.md:1771:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:5531:docs/reviews/pr194-codereview-codex-5-5.md:2499:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:5741:docs/reviews/pr194-codereview-codex-5-5.md:3459:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-5.md:5783:docs/reviews/pr194-codereview-codex-5-5.md:3684:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-5.md:6149:docs/reviews/pr194-codereview-codex-5-4.md:1771:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6252:docs/reviews/pr194-codereview-codex-5-4.md:2499:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6462:docs/reviews/pr194-codereview-codex-5-4.md:3459:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-5.md:6504:docs/reviews/pr194-codereview-codex-5-4.md:3684:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-5.md:6723:docs/reviews/pr194-codereview-codex-5-4.md:5999:            if (_clans[order.targetClanId].clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6870:   418	            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6893:   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/r2/pr194-codereview-codex-5-5.md:6915:   463	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6916:   464	            clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6918:   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6919:   467	            clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6923:   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6938:   486	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-5.md:6972:   520	        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7157:  1060	            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7182:  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7183:  1086	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7184:  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7185:  1088	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7188:  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7221:  1124	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7223:  1126	        sim.clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7228:  1131	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7303:  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7533:  1711	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7663:  1841	        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7693:  1871	            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7934:  2112	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:8001:  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-5.md:8022:  2275	            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8049:  2302	            // reset winter timers for new season
docs/reviews/r2/pr194-codereview-codex-5-5.md:8050:  2303	            _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8051:  2304	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/r2/pr194-codereview-codex-5-5.md:8053:  2306	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8056:  2309	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/r2/pr194-codereview-codex-5-5.md:8058:  2311	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/r2/pr194-codereview-codex-5-5.md:8059:  2312	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/r2/pr194-codereview-codex-5-5.md:8061:  2314	            _world.winterActive = true;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8064:  2317	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:8065:  2318	            _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8067:  2320	            // schedule next winter cycle within this season
docs/reviews/r2/pr194-codereview-codex-5-5.md:8068:  2321	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/r2/pr194-codereview-codex-5-5.md:8070:  2323	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8072:  2325	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8073:  2326	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8075:  2328	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/r2/pr194-codereview-codex-5-5.md:8076:  2329	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8077:  2330	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8112:   520	        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8115:   523	        clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8120:   528	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8190:   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8320:  2163	            if (clan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:8375:  3048	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:8424:  3097	    // OTC TRANSFERS
docs/reviews/r2/pr194-codereview-codex-5-5.md:8428:  3101	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8432:  3105	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8436:  3109	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8444:  3117	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-5.md:8841:   120	    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-5.md:8923:   202	    bool winterActive;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8924:   203	    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8925:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/r2/pr194-codereview-codex-5-5.md:9660:    39	          { name: 'winterActive', type: 'bool' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:9661:    40	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:9662:    41	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:9711:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:9975:   414	    bool winterActive;
docs/reviews/r2/pr194-codereview-codex-5-5.md:9976:   415	    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:9977:   416	    uint64 winterEndsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:10013:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/r2/pr194-codereview-codex-5-5.md:10062:  3393	            winterActive: _world.winterActive,
docs/reviews/r2/pr194-codereview-codex-5-5.md:10063:  3394	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-5.md:10064:  3395	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-5.md:10126:    48	          { name: 'winterActive', type: 'bool' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:10127:    49	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:10128:    50	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-5.md:10176:     9	    // Season + winter timers (Phase 4.4)
docs/reviews/r2/pr194-codereview-codex-5-5.md:10180:    13	    winterActive: v.optional(v.boolean()),
docs/reviews/r2/pr194-codereview-codex-5-5.md:10181:    14	    winterStartsAtTick: v.optional(v.number()),
docs/reviews/r2/pr194-codereview-codex-5-5.md:10182:    15	    winterEndsAtTick: v.optional(v.number()),
docs/reviews/r2/pr194-codereview-codex-5-5.md:10260:23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:280:        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:10333:23f7f1a:packages/contracts/test/ClanWorld.t.sol:2045:        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/r2/pr194-codereview-codex-5-5.md:10334:23f7f1a:packages/contracts/test/ClanWorld.t.sol:2046:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr194-r2-synthesis.md:14:- ✅ Winter starvation replay uses replayed-tick winter status
docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/r2/pr194-codereview-opus-4-6.md:51:### M2: `_simulateResolveAction` starvation check uses `_world.currentTick` instead of simulation tick
docs/reviews/r2/pr194-codereview-opus-4-6.md:56:bool starving = sim.clan.starvationStartsAtTick != 0 
docs/reviews/r2/pr194-codereview-opus-4-6.md:57:    && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-opus-4-6.md:60:The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.
docs/reviews/r2/pr194-codereview-opus-4-6.md:62:**Impact:** Low in practice — the upkeep/resolution ordering within the simulation loop prevents the misalignment. But it's a latent bug if the simulation structure changes. Should use `sim.clan.starvationStartsAtTick <= tick` for correctness parity.
docs/reviews/pr198-codereview-codex-5-4.md:322:               "name": "winterActive",
docs/reviews/pr198-codereview-codex-5-4.md:747:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr198-codereview-codex-5-4.md:749:-        _world.winterStartsAtTick =
docs/reviews/pr198-codereview-codex-5-4.md:751:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:752:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr198-codereview-codex-5-4.md:753:         _world.winterActive = false;
docs/reviews/pr198-codereview-codex-5-4.md:756:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:1648:     // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-4.md:3197:-            ws.winterStartsAtTick,
docs/reviews/pr198-codereview-codex-5-4.md:3199:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr198-codereview-codex-5-4.md:3201:         assertFalse(ws.winterActive);
docs/reviews/pr198-codereview-codex-5-4.md:3204:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr198-codereview-codex-5-4.md:3205:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr198-codereview-codex-5-4.md:3207:-        uint64 winterStart =
docs/reviews/pr198-codereview-codex-5-4.md:3209:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:3210:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr198-codereview-codex-5-4.md:3853:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr198-codereview-codex-5-4.md:3943:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr198-codereview-codex-5-4.md:3945:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:3946:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr198-codereview-codex-5-4.md:3947:        _world.winterActive = false;
docs/reviews/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/pr198-codereview-codex-5-4.md:4317:    bool winterActive;
docs/reviews/pr198-codereview-codex-5-4.md:4318:    uint64 winterStartsAtTick;
docs/reviews/pr198-codereview-codex-5-4.md:4319:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr198-codereview-codex-5-4.md:4356:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr198-codereview-codex-5-4.md:4358:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr198-codereview-codex-5-4.md:4651:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:5713:  2115	    // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-4.md:5717:  2119	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5721:  2123	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5725:  2127	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5733:  2135	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-4.md:5941:   124	    WinterLocked
docs/reviews/pr198-codereview-codex-5-4.md:6024:   207	    bool winterActive;
docs/reviews/pr198-codereview-codex-5-4.md:6025:   208	    uint64 winterStartsAtTick;
docs/reviews/pr198-codereview-codex-5-4.md:6026:   209	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr198-codereview-codex-5-4.md:6063:   246	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr198-codereview-codex-5-4.md:6065:   248	    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr198-codereview-codex-5-4.md:7052:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-4.md:7125:docs/reviews/r2/pr194-codereview-codex-5-5.md:4585:    46	///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/pr198-codereview-codex-5-4.md:7206:docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr198-codereview-codex-5-4.md:7293:docs/reviews/r2/pr194-codereview-codex-5-4.md:4859:///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
docs/reviews/pr198-codereview-codex-5-4.md:7662:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-4.md:7821:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-4.md:7972:docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr198-codereview-codex-5-4.md:8029:docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/reviews/pr198-codereview-codex-5-4.md:8055:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-4.md:8157:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr198-codereview-codex-5-4.md:8772:   876	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr198-codereview-codex-5-4.md:8797:   901	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr198-codereview-codex-5-4.md:8815:   919	            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr198-codereview-codex-5-4.md:8842:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:8978:   397	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr198-codereview-codex-5-4.md:9000:   419	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr198-codereview-codex-5-4.md:9001:   420	            clan.starvationStartsAtTick = tick;
docs/reviews/pr198-codereview-codex-5-4.md:9003:   422	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr198-codereview-codex-5-4.md:9004:   423	            clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-4.md:9011:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:9133:   423	    bool winterActive;
docs/reviews/pr198-codereview-codex-5-4.md:9134:   424	    uint64 winterStartsAtTick;
docs/reviews/pr198-codereview-codex-5-4.md:9135:   425	    uint64 winterEndsAtTick;
docs/reviews/pr198-codereview-codex-5-4.md:9325:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:9328:- OTC clan-to-clan resource transfers
docs/reviews/pr198-codereview-codex-5-4.md:9338:- OTC transfers of abstract assets
docs/reviews/pr198-codereview-codex-5-4.md:9397:### 3.8 OTC model
docs/reviews/pr198-codereview-codex-5-4.md:9398:OTC transfers are:
docs/reviews/pr198-codereview-codex-5-4.md:9407:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/reviews/pr198-codereview-codex-5-4.md:9414:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:9415:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:9421:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr198-codereview-codex-5-4.md:9423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr198-codereview-codex-5-4.md:9436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr198-codereview-codex-5-4.md:9437:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/reviews/pr198-codereview-codex-5-4.md:9445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr198-codereview-codex-5-4.md:10322:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:10402:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/reviews/pr198-codereview-codex-5-4.md:10403:- that defending worker contributes 0 defense until Clan B starvation ends
docs/reviews/pr198-codereview-codex-5-4.md:10728:  2297	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr198-codereview-codex-5-4.md:10732:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:10823:  2392	            winterActive: _world.winterActive,
docs/reviews/pr198-codereview-codex-5-4.md:10824:  2393	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr198-codereview-codex-5-4.md:10825:  2394	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr198-codereview-codex-5-4.md:10832:  2401	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr198-codereview-codex-5-4.md:10837:  2406	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:177:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:2992:docs/reviews/pr194-r2-codereview-codex-5-5.md:5423:/bin/bash -lc 'rg -n "test_e2e_banditLifecycle|test_winterStarvationReplay|test_resolveBanditAttackReturns|test_defeatedBanditAwardsBlueprint|test_deadTargetCleanup|test_heartbeat_scheduledMarketObserves" packages/contracts/test' in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:2996:docs/reviews/pr194-r2-codereview-codex-5-5.md:5428:packages/contracts/test/BanditAttackResolution.t.sol:464:    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr199-codereview-codex-5-5.md:3027:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:3511:docs/reviews/r2/pr194-codereview-codex-5-5.md:10260:23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:280:        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr199-codereview-codex-5-5.md:3694:docs/reviews/r2/pr194-codereview-opus-4-6.md:60:The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.
docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr199-codereview-codex-5-5.md:5260:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:5261:docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr199-codereview-codex-5-5.md:5290:docs/planning/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr199-codereview-codex-5-5.md:5316:docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr199-codereview-codex-5-5.md:5332:docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr199-codereview-codex-5-5.md:5365:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-5.md:5383:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr199-codereview-codex-5-5.md:5384:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr199-codereview-codex-5-5.md:5438:docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr199-codereview-codex-5-5.md:5457:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-5.md:5470:docs/planning/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr199-codereview-codex-5-5.md:5473:docs/planning/clanworld_v4_1_addendum.md:156:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr199-codereview-codex-5-5.md:5474:docs/planning/clanworld_v4_1_addendum.md:174:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr199-codereview-codex-5-5.md:5508:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:5603:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-5.md:5607:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:5609:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:5623:docs/reviews/pr200-codereview-codex-5-5.md:6338:/bin/bash -lc "rg -n \"mintClan\\(|goldBalance|blueprintBalance|clanState = ClanState.DEAD|clanState == ClanState.DEAD|_poolForResourceType|seedPools\\(|transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|settleClansman\\(|DepositResources|CLANSMAN_CARRY_CAP|WOOD_YIELD_PER_TICK|ResourceMinted|ResourceBurned\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:5726:docs/reviews/pr194-r2-codereview-codex-5-5.md:1749:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:5756:docs/reviews/pr194-r2-codereview-codex-5-5.md:2287:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:5834:docs/reviews/pr194-r2-codereview-codex-5-5.md:5841:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:5880:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr199-codereview-codex-5-5.md:5896:docs/reviews/pr194-r2-codereview-codex-5-5.md:8132:  2191	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:5910:docs/reviews/pr194-r2-codereview-codex-5-5.md:8578:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr199-codereview-codex-5-5.md:6006:docs/reviews/pr198-codereview-codex-5-4.md:7052:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6026:docs/reviews/pr198-codereview-codex-5-4.md:7662:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6033:docs/reviews/pr198-codereview-codex-5-4.md:7821:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6039:docs/reviews/pr198-codereview-codex-5-4.md:8055:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6062:docs/reviews/pr198-codereview-codex-5-4.md:9421:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr199-codereview-codex-5-5.md:6063:docs/reviews/pr198-codereview-codex-5-4.md:9423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr199-codereview-codex-5-5.md:6064:docs/reviews/pr198-codereview-codex-5-4.md:9436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr199-codereview-codex-5-5.md:6066:docs/reviews/pr198-codereview-codex-5-4.md:9445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr199-codereview-codex-5-5.md:6184:docs/reviews/pr198-codereview-codex-5-5.md:5262:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:6195:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6202:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6328:docs/reviews/pr194-r2-codereview-codex-5-4.md:1749:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:6358:docs/reviews/pr194-r2-codereview-codex-5-4.md:2287:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:6438:docs/reviews/pr194-r2-codereview-codex-5-4.md:5590:            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:6478:docs/reviews/pr194-r2-codereview-codex-5-4.md:7039:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:6515:docs/reviews/pr194-r2-codereview-codex-5-4.md:8284:  2191	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-5.md:6647:docs/reviews/r2/pr194-codereview-codex-5-5.md:2230:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:6848:docs/reviews/r2/pr194-codereview-codex-5-5.md:7934:  2112	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-5.md:6919:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:430:- The game rewards monument-building, but you must survive winters and bandits to get there.
docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr199-codereview-codex-5-5.md:6980:docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md:73:Per battle plan §10: **deploy on mainnet** (Aristotle). The 0.1 OG/day testnet faucet limit is going to be annoying with 4 elders running heartbeats, GLM-5 Compute calls if used, KV writes, and iNFT mint+update transactions. A small mainnet OG balance (a few OG covers the entire demo lifecycle — see Gas costs in §7) is cleaner than rationing testnet drips.
docs/reviews/pr199-codereview-codex-5-5.md:7008:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr199-codereview-codex-5-5.md:7068:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-5.md:7074:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:7162:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:7163:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:7167:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:7181:docs/reviews/pr200-codereview-codex-5-4.md:5093:The spec slice is loaded: Phase 7 only promised the transfer surface plus dead-clan blocking on initiation, while the deeper state-schema doc still describes direct transfer entrypoints. I’m now checking whether the proposal-based implementation preserves those guarantees and whether the new token/pool changes accidentally widened the blast radius.
docs/reviews/pr199-codereview-codex-5-5.md:7183:docs/reviews/pr200-codereview-codex-5-4.md:5160:/bin/bash -lc "rg -n \"mintClan\\(|goldBalance|blueprintBalance|clanState = ClanState.DEAD|clanState == ClanState.DEAD|_poolForResourceType|seedPools\\(|transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|settleClansman\\(|DepositResources|CLANSMAN_CARRY_CAP|WOOD_YIELD_PER_TICK|ResourceMinted|ResourceBurned\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:7239:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr199-codereview-codex-5-5.md:7241:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:157:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr199-codereview-codex-5-5.md:7242:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:175:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr199-codereview-codex-5-5.md:7270:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr199-codereview-codex-5-5.md:7282:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:7283:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr199-codereview-codex-5-5.md:7312:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr199-codereview-codex-5-5.md:7338:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr199-codereview-codex-5-5.md:7354:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr199-codereview-codex-5-5.md:7384:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-5.md:7402:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr199-codereview-codex-5-5.md:7403:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr199-codereview-codex-5-5.md:8486:docs/reviews/r2/pr194-codereview-codex-5-4.md:2230:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:8661:docs/reviews/r2/pr194-codereview-codex-5-4.md:9151:  2112	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/pr199-codereview-codex-5-5.md:8708:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-5.md:10207:  2221	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr199-codereview-codex-5-5.md:10211:  2225	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:10286:  2414	            winterActive: _world.winterActive,
docs/reviews/pr199-codereview-codex-5-5.md:10287:  2415	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr199-codereview-codex-5-5.md:10288:  2416	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr199-codereview-codex-5-5.md:10295:  2423	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr199-codereview-codex-5-5.md:10300:  2428	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:138:        _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:443:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
packages/contracts/src/ClanWorld.sol:465:        if (starving && clan.starvationStartsAtTick == 0) {
packages/contracts/src/ClanWorld.sol:466:            clan.starvationStartsAtTick = tick;
packages/contracts/src/ClanWorld.sol:468:        } else if (!starving && clan.starvationStartsAtTick != 0) {
packages/contracts/src/ClanWorld.sol:469:            clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:476:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:909:    ///         4. Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:934:        // Step 4: Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:952:            if (clan.clanState == ClanState.DEAD) continue;
packages/contracts/src/ClanWorld.sol:979:            // reset winter timers for new season
packages/contracts/src/ClanWorld.sol:980:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:986:        // --- winter transitions (timer only; mechanics = Phase 10) ---
packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
packages/contracts/src/ClanWorld.sol:991:            _world.winterActive = true;
packages/contracts/src/ClanWorld.sol:994:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
packages/contracts/src/ClanWorld.sol:995:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:997:            // schedule next winter cycle within this season
packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
packages/contracts/src/ClanWorld.sol:1003:                _world.winterEndsAtTick = nextWinterEnd;
packages/contracts/src/ClanWorld.sol:1005:                // no more winters this season; sentinel = seasonEndTick so guard never fires
packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:1007:                _world.winterEndsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:1066:        clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:2004:    // OTC TRANSFERS
packages/contracts/src/ClanWorld.sol:2008:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2012:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2016:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2024:        revert("OTC transfers not implemented");
packages/contracts/src/ClanWorld.sol:2221:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
packages/contracts/src/ClanWorld.sol:2225:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:2414:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorld.sol:2416:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/src/ClanWorld.sol:2423:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
packages/contracts/src/ClanWorld.sol:2428:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:430:               "name": "winterActive",
docs/reviews/r2/pr194-codereview-codex-5-4.md:760:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/r2/pr194-codereview-codex-5-4.md:762:-        _world.winterStartsAtTick =
docs/reviews/r2/pr194-codereview-codex-5-4.md:764:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-4.md:765:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/r2/pr194-codereview-codex-5-4.md:766:         _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-4.md:778:+            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-4.md:783:             clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:787:+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:802:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:836:+        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-4.md:839:+        clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-4.md:844:+        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1120:+            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1145:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1146:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1147:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1148:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1151:+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1184:+        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1186:+        sim.clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1191:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1266:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1771:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1901:+        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1931:+            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2230:+            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2344:+            if (clan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2419:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2450:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2460:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2499:+        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2558:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2562:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:2634:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:2666:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/r2/pr194-codereview-codex-5-4.md:2671:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:3196:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:3206:+        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:3444:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:3445:+        _advanceUntil(winterStart + 4);
docs/reviews/r2/pr194-codereview-codex-5-4.md:3446:+        uint64 deathFromTick = winterStart;
docs/reviews/r2/pr194-codereview-codex-5-4.md:3459:+        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-4.md:3684:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4180:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4186:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4248:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4249:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4586:    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-4.md:4666:    bool   winterActive;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4667:    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4668:    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/r2/pr194-codereview-codex-5-4.md:4705:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/r2/pr194-codereview-codex-5-4.md:4707:    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/r2/pr194-codereview-codex-5-4.md:4859:///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/r2/pr194-codereview-codex-5-4.md:5100:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/r2/pr194-codereview-codex-5-4.md:5122:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5123:            clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5125:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5126:            clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5133:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5606:        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5999:            if (_clans[order.targetClanId].clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:6086:    // OTC TRANSFERS — Phase 2 stubs
docs/reviews/r2/pr194-codereview-codex-5-4.md:6090:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6094:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6098:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6106:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/r2/pr194-codereview-codex-5-4.md:6182:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/r2/pr194-codereview-codex-5-4.md:6191:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6297:            winterActive: _world.winterActive,
docs/reviews/r2/pr194-codereview-codex-5-4.md:6298:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:6299:            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:6306:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/r2/pr194-codereview-codex-5-4.md:6311:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6624:    bool   winterActive;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6625:    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6626:    uint64 winterEndsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6852:    // ----- winter cold damage -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:6856:    // ----- OTC transfers -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:6923:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/r2/pr194-codereview-codex-5-4.md:7072:    48	///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7160:   136	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/r2/pr194-codereview-codex-5-4.md:7162:   138	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-4.md:7163:   139	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/r2/pr194-codereview-codex-5-4.md:7164:   140	        _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7442:   418	            if (clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7465:   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7492:   463	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7493:   464	            clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7495:   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7496:   467	            clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7500:   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7515:   486	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:7549:   520	        if (clan.clanId == ClanWorldConstants.CLAN_ID_NULL || clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7552:   523	        clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7557:   528	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7627:   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8094:  1060	            if (sim.clan.clanState == ClanState.DEAD) break;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8119:  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8120:  1086	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8121:  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8122:  1088	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8125:  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8158:  1124	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8160:  1126	        sim.clan.clanState = ClanState.DEAD;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8165:  1131	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8240:  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8750:  1711	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8880:  1841	        if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8910:  1871	            if (defenderClan.clanState == ClanState.DEAD || _isStarving(defenderClan)) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:9151:  2112	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != region) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:9202:  2163	            if (clan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:9293:  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-4.md:9314:  2275	            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9341:  2302	            // reset winter timers for new season
docs/reviews/r2/pr194-codereview-codex-5-4.md:9342:  2303	            _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9343:  2304	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/r2/pr194-codereview-codex-5-4.md:9345:  2306	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9348:  2309	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/r2/pr194-codereview-codex-5-4.md:9350:  2311	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/r2/pr194-codereview-codex-5-4.md:9351:  2312	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/r2/pr194-codereview-codex-5-4.md:9353:  2314	            _world.winterActive = true;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9356:  2317	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:9357:  2318	            _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9359:  2320	            // schedule next winter cycle within this season
docs/reviews/r2/pr194-codereview-codex-5-4.md:9360:  2321	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/r2/pr194-codereview-codex-5-4.md:9362:  2323	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9364:  2325	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9365:  2326	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9367:  2328	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/r2/pr194-codereview-codex-5-4.md:9368:  2329	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9369:  2330	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9433:  2389	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10092:  3048	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:10141:  3097	    // OTC TRANSFERS
docs/reviews/r2/pr194-codereview-codex-5-4.md:10145:  3101	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10149:  3105	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10153:  3109	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10161:  3117	        revert("OTC transfers not implemented");
docs/reviews/r2/pr194-codereview-codex-5-4.md:10385:  3336	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10442:  3393	            winterActive: _world.winterActive,
docs/reviews/r2/pr194-codereview-codex-5-4.md:10443:  3394	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:10444:  3395	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:10631:   120	    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-4.md:10713:   202	    bool winterActive;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10714:   203	    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10715:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/r2/pr194-codereview-codex-5-4.md:10757:   241	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/r2/pr194-codereview-codex-5-4.md:10759:   243	    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/r2/pr194-codereview-codex-5-4.md:10930:   414	    bool winterActive;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10931:   415	    uint64 winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10932:   416	    uint64 winterEndsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:11010:   629	    // ----- winter cold damage -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:11014:   633	    // ----- OTC transfers -----
docs/reviews/r2/pr194-codereview-codex-5-4.md:11072:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/r2/pr194-codereview-codex-5-4.md:11197:    32	        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:11207:    42	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:11493:  1691	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:13460:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:13724:   305	            winterActive: _world.winterActive,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13725:   306	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13726:   307	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13925:23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:13972:   280	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:13973:   281	        _advanceUntil(winterStart + 4);
docs/reviews/r2/pr194-codereview-codex-5-4.md:13974:   282	        uint64 deathFromTick = winterStart;
docs/reviews/r2/pr194-codereview-codex-5-4.md:13987:   295	        assertEq(uint8(targetAfter.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-4.md:14212:   520	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "target clan dead");
docs/reviews/r2/pr194-codereview-codex-5-4.md:14702:    48	          { name: 'winterActive', type: 'bool' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14703:    49	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14704:    50	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14817:    39	          { name: 'winterActive', type: 'bool' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14818:    40	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14819:    41	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14868:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
docs/reviews/r2/pr194-synthesis.md:35:| M3 | `ClanWorld.sol:1206` (`_simulateResolveAction`) | 4.6 = **1/5** | MED | **Starvation check uses `_world.currentTick` instead of simulation tick.** Latent bug — current control flow prevents misalignment, but breaks if simulation structure changes. Use `sim.clan.starvationStartsAtTick <= tick`. |
docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
packages/contracts/src/IClanWorld.sol:120:    WinterLocked
packages/contracts/src/IClanWorld.sol:203:    bool winterActive;
packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:205:    uint64 winterEndsAtTick; // 0 if not active
packages/contracts/src/IClanWorld.sol:242:    uint64 starvationStartsAtTick; // 0 = none
packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
packages/contracts/src/IClanWorld.sol:418:    bool winterActive;
packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:420:    uint64 winterEndsAtTick;
packages/contracts/src/IClanWorld.sol:618:    // ----- winter cold damage -----
packages/contracts/src/IClanWorld.sol:622:    // ----- OTC transfers -----
packages/contracts/src/IClanWorld.sol:680:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorldStub.sol:57:        _world.winterActive = false;
packages/contracts/src/ClanWorldStub.sol:117:    // OTC transfers
packages/contracts/src/ClanWorldStub.sol:152:            starvationStartsAtTick: 0,
packages/contracts/src/ClanWorldStub.sol:344:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorldStub.sol:345:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorldStub.sol:346:            winterEndsAtTick: _world.winterEndsAtTick,
docs/planning/phase-3-test-spec.md:105:- Clan B has starvation developing — unsettled penalty.
docs/planning/phase-3-test-spec.md:173:**Setup:** Register worker as defender. Set worker HP to 0 (or advance starvation until death).
docs/planning/phase-3-test-spec.md:175:**Action:** Trigger worker death (via heartbeat starvation or forced state).
packages/contracts/abi/IClanWorld.json:479:              "name": "starvationStartsAtTick",
packages/contracts/abi/IClanWorld.json:600:                      "name": "starvationStartsAtTick",
packages/contracts/abi/IClanWorld.json:1164:                  "name": "starvationStartsAtTick",
packages/contracts/abi/IClanWorld.json:1933:              "name": "winterActive",
packages/contracts/abi/IClanWorld.json:1938:              "name": "winterStartsAtTick",
packages/contracts/abi/IClanWorld.json:1943:              "name": "winterEndsAtTick",
packages/contracts/abi/IClanWorld.json:2065:              "name": "winterActive",
packages/contracts/abi/IClanWorld.json:2070:              "name": "winterStartsAtTick",
packages/contracts/abi/IClanWorld.json:2075:              "name": "winterEndsAtTick",
docs/reviews/pr200-codereview-codex-5-4.md:17:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:741:               "name": "winterActive",
docs/reviews/pr200-codereview-codex-5-4.md:1718:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr200-codereview-codex-5-4.md:1720:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-4.md:1722:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-4.md:1723:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr200-codereview-codex-5-4.md:1724:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-4.md:1733:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:1871:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:1882:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1885:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1911:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1912:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1947:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1950:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1980:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:1981:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2038:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2041:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2061:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2062:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2099:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2102:+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2135:+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2136:+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:2209:         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:2316:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-4.md:2318:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr200-codereview-codex-5-4.md:2319:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr200-codereview-codex-5-4.md:2320:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-4.md:2323:     // OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:2557:     // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:2638:     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:3547:-            ws.winterStartsAtTick,
docs/reviews/pr200-codereview-codex-5-4.md:3549:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr200-codereview-codex-5-4.md:3551:         assertFalse(ws.winterActive);
docs/reviews/pr200-codereview-codex-5-4.md:3554:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr200-codereview-codex-5-4.md:3555:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr200-codereview-codex-5-4.md:3557:-        uint64 winterStart =
docs/reviews/pr200-codereview-codex-5-4.md:3559:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-4.md:3560:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr200-codereview-codex-5-4.md:3634:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-4.md:3654:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-4.md:3678:+        world.setClanState(clanB, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-4.md:3702:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-4.md:3721:+        world.setClanState(clanA, ClanState.DEAD);
docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-4.md:4637:  → Gather contract/spec context for Phase 7 OTC transfer surface and related invariants
docs/reviews/pr200-codereview-codex-5-4.md:4638:  • Parallelize focused review across OTC logic and test/spec coverage
docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-4.md:4678:docs/reviews/pr200-codereview-codex-5-5.md:17:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:4683:docs/reviews/pr200-codereview-codex-5-5.md:1871:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4685:docs/reviews/pr200-codereview-codex-5-5.md:2209:         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4686:docs/reviews/pr200-codereview-codex-5-5.md:2323:     // OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4687:docs/reviews/pr200-codereview-codex-5-5.md:2557:     // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4688:docs/reviews/pr200-codereview-codex-5-5.md:2638:     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4700:docs/reviews/r2/pr194-codereview-codex-5-5.md:8424:  3097	    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4701:docs/reviews/r2/pr194-codereview-codex-5-5.md:8428:  3101	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4702:docs/reviews/r2/pr194-codereview-codex-5-5.md:8432:  3105	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4703:docs/reviews/r2/pr194-codereview-codex-5-5.md:8436:  3109	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4704:docs/reviews/r2/pr194-codereview-codex-5-5.md:8444:  3117	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4705:docs/reviews/r2/pr194-codereview-codex-5-5.md:10013:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4708:docs/reviews/pr198-codereview-codex-5-4.md:1648:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4710:docs/reviews/pr198-codereview-codex-5-4.md:5713:  2115	    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4711:docs/reviews/pr198-codereview-codex-5-4.md:5717:  2119	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4712:docs/reviews/pr198-codereview-codex-5-4.md:5721:  2123	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4713:docs/reviews/pr198-codereview-codex-5-4.md:5725:  2127	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4714:docs/reviews/pr198-codereview-codex-5-4.md:5733:  2135	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4718:docs/reviews/pr198-codereview-codex-5-4.md:7972:docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr200-codereview-codex-5-4.md:4721:docs/reviews/pr198-codereview-codex-5-4.md:9328:- OTC clan-to-clan resource transfers
docs/reviews/pr200-codereview-codex-5-4.md:4722:docs/reviews/pr198-codereview-codex-5-4.md:9338:- OTC transfers of abstract assets
docs/reviews/pr200-codereview-codex-5-4.md:4723:docs/reviews/pr198-codereview-codex-5-4.md:9397:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-4.md:4724:docs/reviews/pr198-codereview-codex-5-4.md:9398:OTC transfers are:
docs/reviews/pr200-codereview-codex-5-4.md:4733:docs/reviews/r2/pr194-codereview-codex-5-4.md:6086:    // OTC TRANSFERS — Phase 2 stubs
docs/reviews/pr200-codereview-codex-5-4.md:4734:docs/reviews/r2/pr194-codereview-codex-5-4.md:6090:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/pr200-codereview-codex-5-4.md:4735:docs/reviews/r2/pr194-codereview-codex-5-4.md:6094:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/pr200-codereview-codex-5-4.md:4736:docs/reviews/r2/pr194-codereview-codex-5-4.md:6098:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/pr200-codereview-codex-5-4.md:4737:docs/reviews/r2/pr194-codereview-codex-5-4.md:6106:        revert("ClanWorld: OTC transfers available in Phase 2");
docs/reviews/pr200-codereview-codex-5-4.md:4738:docs/reviews/r2/pr194-codereview-codex-5-4.md:6856:    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4739:docs/reviews/r2/pr194-codereview-codex-5-4.md:6923:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4744:docs/reviews/r2/pr194-codereview-codex-5-4.md:10141:  3097	    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4745:docs/reviews/r2/pr194-codereview-codex-5-4.md:10145:  3101	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4746:docs/reviews/r2/pr194-codereview-codex-5-4.md:10149:  3105	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4747:docs/reviews/r2/pr194-codereview-codex-5-4.md:10153:  3109	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4748:docs/reviews/r2/pr194-codereview-codex-5-4.md:10161:  3117	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4749:docs/reviews/r2/pr194-codereview-codex-5-4.md:11014:   633	    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4750:docs/reviews/r2/pr194-codereview-codex-5-4.md:11072:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4760:docs/reviews/pr200-codereview-codex-5-4.md:17:Senior staff engineer FINAL pre-merge review for PR #200 (Phase 7 — OTC Transfer Surface) at head 0c20b46.
docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:4765:docs/reviews/pr200-codereview-codex-5-4.md:1871:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4767:docs/reviews/pr200-codereview-codex-5-4.md:2209:         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4768:docs/reviews/pr200-codereview-codex-5-4.md:2323:     // OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4769:docs/reviews/pr200-codereview-codex-5-4.md:2557:     // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4770:docs/reviews/pr200-codereview-codex-5-4.md:2638:     // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4772:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
docs/reviews/pr200-codereview-codex-5-4.md:4773:docs/reviews/pr200-codereview-codex-5-4.md:4637:  → Gather contract/spec context for Phase 7 OTC transfer surface and related invariants
docs/reviews/pr200-codereview-codex-5-4.md:4774:docs/reviews/pr200-codereview-codex-5-4.md:4638:  • Parallelize focused review across OTC logic and test/spec coverage
docs/reviews/pr200-codereview-codex-5-4.md:4777:docs/planning/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-4.md:4778:docs/planning/clanworld_v4_3_schema_patch.md:301:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/reviews/pr200-codereview-codex-5-4.md:4779:docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4780:docs/reviews/pr198-codereview-codex-5-5.md:1648:     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4781:docs/reviews/pr198-codereview-codex-5-5.md:4541:   633	    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4782:docs/reviews/pr198-codereview-codex-5-5.md:4599:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4785:docs/reviews/pr198-codereview-codex-5-5.md:7107:  2115	    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4786:docs/reviews/pr198-codereview-codex-5-5.md:7111:  2119	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4787:docs/reviews/pr198-codereview-codex-5-5.md:7115:  2123	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4788:docs/reviews/pr198-codereview-codex-5-5.md:7119:  2127	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4789:docs/reviews/pr198-codereview-codex-5-5.md:7127:  2135	        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4798:docs/reviews/pr137-review-claude-opus.md:57:| 6 | `src/ClanWorld.sol` | 1378–1394 | **OTC transfer functions still `revert("... Phase 2")`.** The string "Phase 2" is misleading now that Phase 2 market economy is implemented. Should clarify these are OTC-specific stubs. Agent 7. | **SHOULD FIX** |
docs/reviews/pr200-codereview-codex-5-4.md:4799:docs/reviews/pr137-review-claude-opus.md:134:5. **#5, #6 Stale comments** — Quick fix: update NatSpec and OTC revert strings
docs/reviews/pr200-codereview-codex-5-4.md:4806:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:857:- OTC asset transfer UI (whispers chat is sufficient)
docs/reviews/pr200-codereview-codex-5-4.md:4813:docs/reviews/pr194-r2-codereview-codex-5-4.md:5449:    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4819:docs/reviews/pr153-review-claude-opus.md:46:| 9 | `contracts/src/ClanWorld.sol` | 1417–1437 | **OTC revert strings say "Phase 2".** Misleading since Phase 2 economy is now live; OTC is a separate unimplemented feature. PR #137 #6 — NOT FIXED. | Docs |
docs/reviews/pr200-codereview-codex-5-4.md:4820:docs/reviews/pr153-review-claude-opus.md:158:| PR #137 #6 (SHOULD FIX) | OTC revert strings | **Not fixed** |
docs/reviews/pr200-codereview-codex-5-4.md:4821:docs/reviews/pr153-review-claude-opus.md:201:- #9: Change OTC revert strings to "OTC transfers not implemented"
docs/reviews/pr200-codereview-codex-5-4.md:4823:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:169:{ tick: 186, type: ‘OTC’, clan: ‘ALPHA’, counter: ‘GAMMA’, resource: ‘WHEAT’, amount: 8, gold: 5.6 },
docs/reviews/pr200-codereview-codex-5-4.md:4824:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:172:{ tick: 180, type: ‘OTC’, clan: ‘ALPHA’, counter: ‘BETA’, resource: ‘GOLD’, amount: 5 },
docs/reviews/pr200-codereview-codex-5-4.md:4825:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:175:{ tick: 174, type: ‘OTC’, clan: ‘DELTA’, counter: ‘GAMMA’, resource: ‘BLUEPRINT’, amount: 1 },
docs/reviews/pr200-codereview-codex-5-4.md:4826:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:179:{ tick: 163, type: ‘OTC’, clan: ‘GAMMA’, counter: ‘DELTA’, resource: ‘WHEAT’, amount: 6 },
docs/reviews/pr200-codereview-codex-5-4.md:4827:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
docs/reviews/pr200-codereview-codex-5-4.md:4828:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:1030:}}>{t.type === ‘AMM’ ? ‘⌬ UNISWAP’ : ‘✦ OTC’}</span>
docs/reviews/pr200-codereview-codex-5-4.md:4829:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:1589:        <LabelValue label="OTC TRANSFERS" value="34" color={T.gamma} />
docs/reviews/pr200-codereview-codex-5-4.md:4833:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:207:- OTC clan-to-clan resource transfers
docs/reviews/pr200-codereview-codex-5-4.md:4834:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:217:- OTC transfers of abstract assets
docs/reviews/pr200-codereview-codex-5-4.md:4835:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:225:- OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4844:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:680:OTC transfers may draw only from:
docs/reviews/pr200-codereview-codex-5-4.md:4845:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:685:OTC transfers may **not** draw from worker carry balances.
docs/reviews/pr200-codereview-codex-5-4.md:4848:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:939:- OTC transfers cannot draw from worker carry balances
docs/reviews/pr200-codereview-codex-5-4.md:4849:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:997:- explicit OTC transfer functions
docs/reviews/pr200-codereview-codex-5-4.md:4850:docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr200-codereview-codex-5-4.md:4852:docs/planning/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4858:docs/planning/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/reviews/pr200-codereview-codex-5-4.md:4859:docs/planning/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/reviews/pr200-codereview-codex-5-4.md:4860:docs/planning/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4861:docs/planning/clanworld_numbered_implementation_plan.md:563:11. OTC  
docs/reviews/pr200-codereview-codex-5-4.md:4862:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-4.md:4863:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:301:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/reviews/pr200-codereview-codex-5-4.md:4864:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4865:docs/planning/clanworld_v1_implementation_profile.md:28:- trustless OTC escrow
docs/reviews/pr200-codereview-codex-5-4.md:4866:docs/planning/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-4.md:4867:docs/planning/clanworld_v1_implementation_profile.md:115:OTC transfers are:
docs/reviews/pr200-codereview-codex-5-4.md:4868:docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4869:docs/planning/clanworld_v1_implementation_profile.md:238:- advanced OTC convenience batching
docs/reviews/pr200-codereview-codex-5-4.md:4870:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:634:    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4871:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:696:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4876:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:348:OTC negotiation happens off-chain / via AXL, and actual asset transfer happens directly at token/account level, not by worker courier simulation.
docs/reviews/pr200-codereview-codex-5-4.md:4882:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1050:# 11. OTC Trust Model
docs/reviews/pr200-codereview-codex-5-4.md:4883:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1053:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/reviews/pr200-codereview-codex-5-4.md:4887:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1200:- OTC trust is social, not escrowed
docs/reviews/pr200-codereview-codex-5-4.md:4889:docs/planning/clanworld_v4_spec.md:348:OTC negotiation happens off-chain / via AXL, and actual asset transfer happens directly at token/account level, not by worker courier simulation.
docs/reviews/pr200-codereview-codex-5-4.md:4895:docs/planning/clanworld_v4_spec.md:1050:# 11. OTC Trust Model
docs/reviews/pr200-codereview-codex-5-4.md:4896:docs/planning/clanworld_v4_spec.md:1053:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/reviews/pr200-codereview-codex-5-4.md:4900:docs/planning/clanworld_v4_spec.md:1200:- OTC trust is social, not escrowed
docs/reviews/pr200-codereview-codex-5-4.md:4902:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:28:- trustless OTC escrow
docs/reviews/pr200-codereview-codex-5-4.md:4903:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/reviews/pr200-codereview-codex-5-4.md:4904:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:115:OTC transfers are:
docs/reviews/pr200-codereview-codex-5-4.md:4905:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4906:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:238:- advanced OTC convenience batching
docs/reviews/pr200-codereview-codex-5-4.md:4907:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:207:- OTC clan-to-clan resource transfers
docs/reviews/pr200-codereview-codex-5-4.md:4908:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:217:- OTC transfers of abstract assets
docs/reviews/pr200-codereview-codex-5-4.md:4909:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:225:- OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4918:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:680:OTC transfers may draw only from:
docs/reviews/pr200-codereview-codex-5-4.md:4919:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:685:OTC transfers may **not** draw from worker carry balances.
docs/reviews/pr200-codereview-codex-5-4.md:4922:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:939:- OTC transfers cannot draw from worker carry balances
docs/reviews/pr200-codereview-codex-5-4.md:4923:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:997:- explicit OTC transfer functions
docs/reviews/pr200-codereview-codex-5-4.md:4924:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr200-codereview-codex-5-4.md:4926:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4932:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/reviews/pr200-codereview-codex-5-4.md:4933:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/reviews/pr200-codereview-codex-5-4.md:4934:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4935:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:563:11. OTC  
docs/reviews/pr200-codereview-codex-5-4.md:4940:packages/contracts/src/ClanWorld.sol:2115:    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:4941:packages/contracts/src/ClanWorld.sol:2119:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4942:packages/contracts/src/ClanWorld.sol:2123:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4943:packages/contracts/src/ClanWorld.sol:2127:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4944:packages/contracts/src/ClanWorld.sol:2135:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:4945:packages/contracts/src/IClanWorld.sol:629:    // ----- OTC transfers -----
docs/reviews/pr200-codereview-codex-5-4.md:4946:packages/contracts/src/IClanWorld.sol:687:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr200-codereview-codex-5-4.md:4947:packages/contracts/src/ClanWorldStub.sol:117:    // OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:5009:## Phase 7 — OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:5058:OTC transfers may draw only from:
docs/reviews/pr200-codereview-codex-5-4.md:5063:OTC transfers may **not** draw from worker carry balances.
docs/reviews/pr200-codereview-codex-5-4.md:5093:The spec slice is loaded: Phase 7 only promised the transfer surface plus dead-clan blocking on initiation, while the deeper state-schema doc still describes direct transfer entrypoints. I’m now checking whether the proposal-based implementation preserves those guarantees and whether the new token/pool changes accidentally widened the blast radius.
docs/reviews/pr200-codereview-codex-5-4.md:5100:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-4.md:5103:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr200-codereview-codex-5-4.md:5108:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-4.md:5115:## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-4.md:5118:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/reviews/pr200-codereview-codex-5-4.md:5138:- winter transition checks
docs/reviews/pr200-codereview-codex-5-4.md:5160:/bin/bash -lc "rg -n \"mintClan\\(|goldBalance|blueprintBalance|clanState = ClanState.DEAD|clanState == ClanState.DEAD|_poolForResourceType|seedPools\\(|transferGold\\(|transferVaultResource\\(|transferBlueprint\\(|transferBundle\\(|settleClansman\\(|DepositResources|CLANSMAN_CARRY_CAP|WOOD_YIELD_PER_TICK|ResourceMinted|ResourceBurned\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-4.md:5335:packages/contracts/src/ClanWorld.sol:919:            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr200-codereview-codex-5-4.md:5388:# 11. OTC Trust Model
docs/reviews/pr200-codereview-codex-5-4.md:5391:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/reviews/pr200-codereview-codex-5-4.md:5622:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6169:    // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:6173:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:6177:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:6181:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:6189:        revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:6324:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:6344:OTC transfers may draw only from:
docs/reviews/pr200-codereview-codex-5-4.md:6349:OTC transfers may **not** draw from worker carry balances.
docs/reviews/pr200-codereview-codex-5-4.md:6376:- dead clan restrictions on OTC
docs/reviews/pr200-codereview-codex-5-4.md:6399:- winter interaction hook
docs/reviews/pr200-codereview-codex-5-4.md:6406:### 5.6 Implement starvation consequences
docs/reviews/pr200-codereview-codex-5-4.md:6407:- next-tick starvation start
docs/reviews/pr200-codereview-codex-5-4.md:6411:One cross-cutting seam is looking especially important now: balances and clan state in this engine are lazily settled. I’m checking whether the new OTC entrypoints settle the source/target clans before reading balances or dead/alive status, because if they don’t, proposal acceptance can run against stale gold/vault/death state.
docs/reviews/pr200-codereview-codex-5-4.md:6413:/bin/bash -lc "rg -n \"_settleClan\\(|submitClanOrders\\(|clan.lastSettledTick|starvationStartsAtTick|_resolveWorldEvents|_killClan|clanState = ClanState.DEAD|clanState "'!= ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-4.md:6418:419:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6419:420:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-4.md:6420:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6421:423:            clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6422:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6428:1033:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6431:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6432:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6735:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr200-codereview-codex-5-4.md:6757:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6758:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-4.md:6763:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-4.md:6765:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6766:            clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6773:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6866:/bin/bash -lc 'rg -n "_killClan|ClanDied|livingClansmen == 0|vaultWood = 0|vaultWheat = 0|vaultFish = 0|vaultIron = 0|clanState = ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-4.md:6877:        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr200-codereview-codex-5-4.md:6895:            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr200-codereview-codex-5-4.md:6922:            // reset winter timers for new season
docs/reviews/pr200-codereview-codex-5-4.md:6923:            _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-4.md:6924:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr200-codereview-codex-5-4.md:6926:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr200-codereview-codex-5-4.md:6929:        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr200-codereview-codex-5-4.md:6931:            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr200-codereview-codex-5-4.md:6932:                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr200-codereview-codex-5-4.md:6934:            _world.winterActive = true;
docs/reviews/pr200-codereview-codex-5-4.md:6937:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr200-codereview-codex-5-4.md:6938:            _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-4.md:6940:            // schedule next winter cycle within this season
docs/reviews/pr200-codereview-codex-5-4.md:6941:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr200-codereview-codex-5-4.md:6943:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr200-codereview-codex-5-4.md:6945:                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr200-codereview-codex-5-4.md:6946:                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr200-codereview-codex-5-4.md:6948:                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr200-codereview-codex-5-4.md:6949:                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr200-codereview-codex-5-4.md:6950:                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr200-codereview-codex-5-4.md:7129:    WinterLocked
docs/reviews/pr200-codereview-codex-5-4.md:7273:The biggest likely defect is now pretty clear: the new OTC entrypoints appear to bypass the engine’s lazy-settlement discipline, while every existing balance-sensitive write path settles first. I’m pulling exact surrounding snippets so I can tie that to concrete file references and separate it from softer spec/API concerns.
docs/reviews/pr200-codereview-codex-5-4.md:7288:  1871	     // OTC TRANSFERS
docs/reviews/pr200-codereview-codex-5-4.md:7299:  1882	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7302:  1885	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7328:  1911	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7329:  1912	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7364:  1947	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7367:  1950	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7397:  1980	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7398:  1981	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7455:  2038	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7458:  2041	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7478:  2061	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7479:  2062	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7516:  2099	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7519:  2102	+        require(_clans[toClanId].clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7552:  2135	+        require(fromClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7553:  2136	+        require(toClan.clanState != ClanState.DEAD, "ERR_CLAN_DEAD");
docs/reviews/pr200-codereview-codex-5-4.md:7626:  2209	         revert("OTC transfers not implemented");
docs/reviews/pr200-codereview-codex-5-4.md:7758:   660	### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:7778:   680	OTC transfers may draw only from:
docs/reviews/pr200-codereview-codex-5-4.md:7783:   685	OTC transfers may **not** draw from worker carry balances.
docs/reviews/pr200-codereview-codex-5-4.md:7793:   298	## M. Dead Clan OTC Restriction (explicit restatement)
docs/reviews/pr200-codereview-codex-5-4.md:7796:   301	All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/reviews/pr200-codereview-codex-5-4.md:7810:   311	## Phase 7 — OTC transfer surface
docs/planning/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/planning/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/planning/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/planning/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/planning/clanworld_v4_3_schema_patch.md:301:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/planning/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/planning/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/planning/battleplan-v1.txt:6:	•	Demo cadence: 20s/tick. Six-minute season at 18 ticks gets you through 1–2 winters’ worth of drama without burning judges’ attention.
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:510:- TEE attestation capture (if Option β model selected — see battleplan §10)
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:578:- **Discord answers from 0G** on the questions in battleplan §10 (model selection auth, KV access control)
docs/reviews/pr198-codereview-codex-5-5.md:322:               "name": "winterActive",
docs/reviews/pr198-codereview-codex-5-5.md:747:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr198-codereview-codex-5-5.md:749:-        _world.winterStartsAtTick =
docs/reviews/pr198-codereview-codex-5-5.md:751:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-5.md:752:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr198-codereview-codex-5-5.md:753:         _world.winterActive = false;
docs/reviews/pr198-codereview-codex-5-5.md:756:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-5.md:1648:     // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-5.md:3197:-            ws.winterStartsAtTick,
docs/reviews/pr198-codereview-codex-5-5.md:3199:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr198-codereview-codex-5-5.md:3201:         assertFalse(ws.winterActive);
docs/reviews/pr198-codereview-codex-5-5.md:3204:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr198-codereview-codex-5-5.md:3205:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr198-codereview-codex-5-5.md:3207:-        uint64 winterStart =
docs/reviews/pr198-codereview-codex-5-5.md:3209:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-5.md:3210:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr198-codereview-codex-5-5.md:4028:   120	    WinterLocked
docs/reviews/pr198-codereview-codex-5-5.md:4110:   202	    bool winterActive;
docs/reviews/pr198-codereview-codex-5-5.md:4111:   203	    uint64 winterStartsAtTick;
docs/reviews/pr198-codereview-codex-5-5.md:4112:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr198-codereview-codex-5-5.md:4149:   241	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr198-codereview-codex-5-5.md:4151:   243	    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr198-codereview-codex-5-5.md:4322:   414	    bool winterActive;
docs/reviews/pr198-codereview-codex-5-5.md:4323:   415	    uint64 winterStartsAtTick;
docs/reviews/pr198-codereview-codex-5-5.md:4324:   416	    uint64 winterEndsAtTick;
docs/reviews/pr198-codereview-codex-5-5.md:4537:   629	    // ----- winter cold damage -----
docs/reviews/pr198-codereview-codex-5-5.md:4541:   633	    // ----- OTC transfers -----
docs/reviews/pr198-codereview-codex-5-5.md:4599:   691	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/reviews/pr198-codereview-codex-5-5.md:4689:  1138	        if (sim.clan.clanId == ClanWorldConstants.CLAN_ID_NULL || sim.clan.clanState == ClanState.DEAD) return;
docs/reviews/pr198-codereview-codex-5-5.md:4691:  1140	        sim.clan.clanState = ClanState.DEAD;
docs/reviews/pr198-codereview-codex-5-5.md:4696:  1145	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:4771:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-5.md:5262:  1711	            if (clan.clanState == ClanState.DEAD || clan.baseRegion != bandit.region || clan.livingClansmen == 0) {
docs/reviews/pr198-codereview-codex-5-5.md:5326:  1775	        if (targetClan.clanId == ClanWorldConstants.CLAN_ID_NULL || targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr198-codereview-codex-5-5.md:5336:  1785	        if (targetClan.clanState == ClanState.DEAD) {
docs/reviews/pr198-codereview-codex-5-5.md:5345:  1794	                || targetClan.clanState == ClanState.DEAD
docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-5.md:7107:  2115	    // OTC TRANSFERS
docs/reviews/pr198-codereview-codex-5-5.md:7111:  2119	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7115:  2123	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7119:  2127	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7127:  2135	        revert("OTC transfers not implemented");
docs/reviews/pr198-codereview-codex-5-5.md:7349:   397	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr198-codereview-codex-5-5.md:7371:   419	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr198-codereview-codex-5-5.md:7372:   420	            clan.starvationStartsAtTick = tick;
docs/reviews/pr198-codereview-codex-5-5.md:7374:   422	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr198-codereview-codex-5-5.md:7375:   423	            clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:7382:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-5.md:7736:   124	    WinterLocked
docs/reviews/pr198-codereview-codex-5-5.md:8158:  2297	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr198-codereview-codex-5-5.md:8162:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-5.md:9237:   876	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr198-codereview-codex-5-5.md:9262:   901	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr198-codereview-codex-5-5.md:9280:   919	            if (clan.clanState == ClanState.DEAD) continue;
docs/reviews/pr198-codereview-codex-5-5.md:9785:  1033	        clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:9945:1033-        clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:10193:   204	- winter burn
docs/reviews/pr153-review-claude-opus.md:46:| 9 | `contracts/src/ClanWorld.sol` | 1417–1437 | **OTC revert strings say "Phase 2".** Misleading since Phase 2 economy is now live; OTC is a separate unimplemented feature. PR #137 #6 — NOT FIXED. | Docs |
docs/reviews/pr153-review-claude-opus.md:158:| PR #137 #6 (SHOULD FIX) | OTC revert strings | **Not fixed** |
docs/reviews/pr153-review-claude-opus.md:201:- #9: Change OTC revert strings to "OTC transfers not implemented"
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:14:If a question is not answered here, the answer is: do whatever produces the most legible, dramatic, mobile-first demo. When in doubt, optimize for the 8 demo moments listed in §10.
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:125:│  bar | winter | bandit warning    │
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:147:- Winter indicator (snowflake icon if `winterActive`)
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:616:| Top HUD bar | `getSnapshot` | tick advance, winter, bandit |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:797:- TopBar with tick clock, season bar, winter and bandit chips
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:857:- OTC asset transfer UI (whispers chat is sufficient)
docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/planning/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/planning/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/planning/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/planning/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/planning/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/planning/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/planning/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/planning/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/planning/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/planning/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/planning/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/planning/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/planning/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/planning/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/planning/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/planning/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/planning/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/planning/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/planning/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/planning/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/planning/clanworld_numbered_implementation_plan.md:537:- winter
docs/planning/clanworld_numbered_implementation_plan.md:563:11. OTC  
docs/planning/clanworld_numbered_implementation_plan.md:564:12. winter  
docs/planning/clanworld_v1_implementation_profile.md:28:- trustless OTC escrow
docs/planning/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/planning/clanworld_v1_implementation_profile.md:115:OTC transfers are:
docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/planning/clanworld_v1_implementation_profile.md:238:- advanced OTC convenience batching
docs/planning/clanworld_v1_implementation_profile.md:242:- winter
docs/planning/clanworld_v1_implementation_profile.md:298:- starvation
docs/planning/clanworld_v1_implementation_profile.md:299:- winter
docs/planning/clanworld_v1_implementation_profile.md:301:- elimination
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:260:  winterActive: boolean;
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:430:- The game rewards monument-building, but you must survive winters and bandits to get there.
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:456:{winterActive ? "WINTER IS ACTIVE." : ""}
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:559:      winterActive: snapshot.winterActive,
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:360:| `ColdDamageApplied` | (omit, unless we add winter UI) | |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:361:| `ClansmanDiedFromCold` | (omit, unless we add winter UI) | |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:708:  - `winterActive = false`, `winterStartsAtTick = 110`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:207:- OTC clan-to-clan resource transfers
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:217:- OTC transfers of abstract assets
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:225:- OTC transfers
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:680:OTC transfers may draw only from:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:685:OTC transfers may **not** draw from worker carry balances.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:939:- OTC transfers cannot draw from worker carry balances
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:997:- explicit OTC transfer functions
docs/planning/clanworld_v4_4_ui_indexer_getters.md:57:    bool   winterActive;
docs/planning/clanworld_v4_4_ui_indexer_getters.md:58:    uint64 winterStartsAtTick;
docs/planning/clanworld_v4_4_ui_indexer_getters.md:59:    uint64 winterEndsAtTick;
docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/planning/clanworld_v4_1_addendum.md:20:## A2. First winter timing
docs/planning/clanworld_v4_1_addendum.md:22:The first winter begins at **tick 110**.
docs/planning/clanworld_v4_1_addendum.md:24:Thereafter, winter begins every 110 ticks:
docs/planning/clanworld_v4_1_addendum.md:30:Each winter lasts **10 ticks**, as already specified in v4.
docs/planning/clanworld_v4_1_addendum.md:81:## A6. Just-in-time winter logistics are intentionally punitive
docs/planning/clanworld_v4_1_addendum.md:85:2. update starvation status for the next tick if shortages occur
docs/planning/clanworld_v4_1_addendum.md:91:This means that during winter:
docs/planning/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/planning/clanworld_v4_1_addendum.md:156:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/planning/clanworld_v4_1_addendum.md:158:This is intentional and follows the existing rule that starvation reduces all clansmen defense contribution to zero.
docs/planning/clanworld_v4_1_addendum.md:166:## A10. No change to summer starvation lethality
docs/planning/clanworld_v4_1_addendum.md:168:Starvation outside winter does **not** directly kill clansmen.
docs/planning/clanworld_v4_1_addendum.md:170:While starving outside winter:
docs/planning/clanworld_v4_1_addendum.md:174:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/planning/clanworld_v4_1_addendum.md:176:This is intentional for v1 to avoid excessively punishing early-game elimination.
docs/planning/clanworld_v4_5_alignment_addendum.md:510:- TEE attestation capture (if Option β model selected — see battleplan §10)
docs/planning/clanworld_v4_5_alignment_addendum.md:578:- **Discord answers from 0G** on the questions in battleplan §10 (model selection auth, KV access control)
docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/planning/clanworld_v4_spec.md:348:OTC negotiation happens off-chain / via AXL, and actual asset transfer happens directly at token/account level, not by worker courier simulation.
docs/planning/clanworld_v4_spec.md:353:2. update starvation status for the next tick if shortages occur
docs/planning/clanworld_v4_spec.md:402:- starvation checks
docs/planning/clanworld_v4_spec.md:403:- winter wood burn
docs/planning/clanworld_v4_spec.md:475:- starvation status begins on the **next tick**
docs/planning/clanworld_v4_spec.md:484:Outside Winter, starvation does **not** directly kill clansmen in v1.
docs/planning/clanworld_v4_spec.md:486:## 4.13 Lazy starvation tracking
docs/planning/clanworld_v4_spec.md:487:To keep heartbeat cheap, starvation should not require full clan mutation every tick.
docs/planning/clanworld_v4_spec.md:490:- store starvation threshold timing or equivalent compact status data
docs/planning/clanworld_v4_spec.md:491:- interpret starvation status lazily at clan settlement time and in views
docs/planning/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/planning/clanworld_v4_spec.md:832:During winter, each clan consumes:
docs/planning/clanworld_v4_spec.md:838:During winter:
docs/planning/clanworld_v4_spec.md:841:- regrowth does not complete during winter
docs/planning/clanworld_v4_spec.md:843:At winter start:
docs/planning/clanworld_v4_spec.md:845:- both plots restart regrowing after winter ends
docs/planning/clanworld_v4_spec.md:846:- both plots become harvestable again `4` ticks after winter ends
docs/planning/clanworld_v4_spec.md:849:During winter:
docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/planning/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/planning/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/planning/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/planning/clanworld_v4_spec.md:1050:# 11. OTC Trust Model
docs/planning/clanworld_v4_spec.md:1053:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/planning/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/planning/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/planning/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/planning/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/planning/clanworld_v4_spec.md:1200:- OTC trust is social, not escrowed
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:348:OTC negotiation happens off-chain / via AXL, and actual asset transfer happens directly at token/account level, not by worker courier simulation.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:353:2. update starvation status for the next tick if shortages occur
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:402:- starvation checks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:475:- starvation status begins on the **next tick**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:484:Outside Winter, starvation does **not** directly kill clansmen in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:486:## 4.13 Lazy starvation tracking
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:487:To keep heartbeat cheap, starvation should not require full clan mutation every tick.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:490:- store starvation threshold timing or equivalent compact status data
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:491:- interpret starvation status lazily at clan settlement time and in views
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:832:During winter, each clan consumes:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:838:During winter:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:841:- regrowth does not complete during winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843:At winter start:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:845:- both plots restart regrowing after winter ends
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:846:- both plots become harvestable again `4` ticks after winter ends
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:849:During winter:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1050:# 11. OTC Trust Model
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1053:OTC deals, mercenary promises, alliances, threats, betrayals, and cooperative pacts are intentionally **non-atomic** in v1.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1200:- OTC trust is social, not escrowed
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:207:- OTC clan-to-clan resource transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:217:- OTC transfers of abstract assets
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:225:- OTC transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:680:OTC transfers may draw only from:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:685:OTC transfers may **not** draw from worker carry balances.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:939:- OTC transfers cannot draw from worker carry balances
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:997:- explicit OTC transfer functions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:311:## Phase 7 — OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:492:- OTC transfer calls
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:531:- OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:537:- winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:563:11. OTC  
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:564:12. winter  
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:298:## M. Dead Clan OTC Restriction (explicit restatement)
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:301:All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:28:- trustless OTC escrow
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:114:### 3.8 OTC model
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:115:OTC transfers are:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:238:- advanced OTC convenience batching
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:242:- winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:298:- starvation
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:299:- winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:301:- elimination
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:57:    bool   winterActive;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:58:    uint64 winterStartsAtTick;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:59:    uint64 winterEndsAtTick;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:20:## A2. First winter timing
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:22:The first winter begins at **tick 110**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:24:Thereafter, winter begins every 110 ticks:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:30:Each winter lasts **10 ticks**, as already specified in v4.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:81:## A6. Just-in-time winter logistics are intentionally punitive
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:85:2. update starvation status for the next tick if shortages occur
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:91:This means that during winter:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:157:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:159:This is intentional and follows the existing rule that starvation reduces all clansmen defense contribution to zero.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:167:## A10. No change to summer starvation lethality
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:169:Starvation outside winter does **not** directly kill clansmen.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:171:While starving outside winter:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:175:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:177:This is intentional for v1 to avoid excessively punishing early-game elimination.
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:118:    WinterLocked
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:198:    bool   winterActive;
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:199:    uint64 winterStartsAtTick;
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:200:    uint64 winterEndsAtTick;     // 0 if not active
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:237:    uint64 starvationStartsAtTick; // 0 = none
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:239:    uint16 coldDamage;             // resets to 0 at winter end
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:407:    bool   winterActive;
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:408:    uint64 winterStartsAtTick;
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:409:    uint64 winterEndsAtTick;
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:630:    // ----- winter cold damage -----
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:634:    // ----- OTC transfers -----
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:696:    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/planning/V1/05 0G/clanworld_clan_identity_spec.md:470:- **Explicit refresh** — caller calls `refresh()` (proposed convenience, see §10) to re-fetch from chain + storage and re-decrypt
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:169:{ tick: 186, type: ‘OTC’, clan: ‘ALPHA’, counter: ‘GAMMA’, resource: ‘WHEAT’, amount: 8, gold: 5.6 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:172:{ tick: 180, type: ‘OTC’, clan: ‘ALPHA’, counter: ‘BETA’, resource: ‘GOLD’, amount: 5 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:175:{ tick: 174, type: ‘OTC’, clan: ‘DELTA’, counter: ‘GAMMA’, resource: ‘BLUEPRINT’, amount: 1 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:179:{ tick: 163, type: ‘OTC’, clan: ‘GAMMA’, counter: ‘DELTA’, resource: ‘WHEAT’, amount: 6 },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:243:GAMMA: { strategy: ‘Survive winter. Convert wheat surplus into defense contracts.’,
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:246:goals: [‘Survive winter’, ‘Stabilize food’] },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:258:{ kind: ‘STARVATION’, text: ‘GAMMA below food threshold, starvation flag set’ },
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:1030:}}>{t.type === ‘AMM’ ? ‘⌬ UNISWAP’ : ‘✦ OTC’}</span>
docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:1589:        <LabelValue label="OTC TRANSFERS" value="34" color={T.gamma} />
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:153:4. **Add 0G Compute SDK integration** with `@0glabs/0g-serving-broker`. Build `elder act` to optionally capture TEE attestations. Decision pending Discord answers — see §10.
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:383:- `clan:{id}:notebook/winter_reflections/{seasonNum}` — TBD, see §11
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:605:| 1 | Anthropic-compat proxy auth model | Discord ask | Step 4 / model decision | See §10 |
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:606:| 2 | TEE attestation surfacing on Anthropic-compat | Discord ask | Step 4 | See §10 |
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:612:| 8 | Winter self-reflection: agent updates own learnings doc | Agent design | Post-MVP / experimentation | Idea: during winter (low activity), agents prompted to consolidate KV notebook into a self-curated "lessons learned" doc. Open whether this is just a notebook key or something more structured. Defer until we see how agents naturally use KV |
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:613:| 9 | 0G Storage KV access control mechanism | Discord ask | Step 2 | Native signed-write gating vs orchestrator-mediated. Affects §7 implementation. See §10 |
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:627:- **TEE attestation verification flow** — for skeptical third parties (only relevant if §10 lands favorably)
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:662:- ⚠️ TEE attestation chain: ship only if §10 Discord answers are favorable
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:672:- ❓ How orchestrator captures TEE signature for `elder act` attestation writes (depends on §10)
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:706:- **TEE attestation verification flow** — only if §10 Discord answers land favorably and we ship sealed inference
docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md:73:Per battle plan §10: **deploy on mainnet** (Aristotle). The 0.1 OG/day testnet faucet limit is going to be annoying with 4 elders running heartbeats, GLM-5 Compute calls if used, KV writes, and iNFT mint+update transactions. A small mainnet OG balance (a few OG covers the entire demo lifecycle — see Gas costs in §7) is cleaner than rationing testnet drips.

codex
I’ve got the contract surface and spec references loaded. Next I’m drilling into the exact winter/death execution paths locally, while two subagents independently check spec timing and cross-phase dead-clan interactions so we don’t miss a seam regression.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '340,620p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   340	    function _settleMissionForClansman(
   341	        Clan storage clan,
   342	        Clansman storage cs,
   343	        uint32 clanId,
   344	        uint64 fromTick,
   345	        uint64 toTick
   346	    ) internal {
   347	        Mission storage m = _missions[cs.clansmanId];
   348	
   349	        // Path 6: dead clansman — invalidate active mission if any
   350	        if (cs.state == ClansmanState.DEAD) {
   351	            if (m.active) {
   352	                if (m.action == ActionType.DefendBase) {
   353	                    _clearDefender(cs.clansmanId);
   354	                } else if (m.action == ActionType.UpgradeWall) {
   355	                    _refundWallUpgradeReservation(cs.clansmanId);
   356	                } else if (m.action == ActionType.UpgradeBase) {
   357	                    _refundBaseUpgradeReservation(cs.clansmanId);
   358	                } else if (m.action == ActionType.UpgradeMonument) {
   359	                    _refundMonumentUpgradeReservation(cs.clansmanId);
   360	                }
   361	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
   362	            }
   363	            return;
   364	        }
   365	
   366	        if (!m.active) return; // no active mission — nothing to settle
   367	
   368	        bytes32 tickSeed;
   369	        for (uint64 tick = fromTick; tick < toTick; tick++) {
   370	            tickSeed = _tickSeeds[tick];
   371	
   372	            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
   373	            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
   374	                cs.state = ClansmanState.ACTING;
   375	                cs.currentRegion = m.targetRegion;
   376	                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
   377	
   378	                if (m.action == ActionType.DefendBase) {
   379	                    _registerDefender(m.targetRegion, clanId, cs.clansmanId);
   380	                }
   381	            }
   382	
   383	            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
   384	
   385	            // Path 3: ACTING at/past settlesAtTick → resolve
   386	            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
   387	                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
   388	                if (m.active && getActionDuration(m.action) > 0) {
   389	                    _completeMission(cs, m);
   390	                }
   391	            }
   392	
   393	            // If mission completed during this tick, stop iterating
   394	            if (!m.active) break;
   395	        }
   396	    }
   397	
   398	    /// @dev Lazy settlement of a clan forward to currentTick.
   399	    ///      Mutates storage. Called before order submission and by public settleClan().
   400	    function _settleClan(uint32 clanId) internal {
   401	        Clan storage clan = _clans[clanId];
   402	        if (clan.clanId == 0) return;
   403	
   404	        uint64 curTick = _world.currentTick;
   405	        uint64 fromTick = clan.lastSettledTick;
   406	        if (fromTick >= curTick) return;
   407	
   408	        // Cap ticks settled per call to prevent block gas limit issues
   409	        uint64 maxSettleTicks = 200;
   410	        if (curTick > fromTick + maxSettleTicks) {
   411	            curTick = fromTick + maxSettleTicks;
   412	        }
   413	
   414	        uint32[] storage clansmanIds = _clanClansmanIds[clanId];
   415	
   416	        // Settle tick by tick from fromTick to curTick - 1
   417	        // (curTick is still open; we settle through the last closed tick)
   418	        for (uint64 tick = fromTick; tick < curTick; tick++) {
   419	            // 1. Apply upkeep for this tick
   420	            _applyUpkeep(clan, tick);
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
   441	    }
   442	
   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
   444	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   445	        if (clan.livingClansmen == 0) return;
   446	
   447	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   448	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   449	
   450	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   451	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   452	
   453	        if (hadEnoughWheat) {
   454	            clan.vaultWheat -= wheatNeeded;
   455	        } else {
   456	            clan.vaultWheat = 0;
   457	        }
   458	        if (hadEnoughFish) {
   459	            clan.vaultFish -= fishNeeded;
   460	        } else {
   461	            clan.vaultFish = 0;
   462	        }
   463	
   464	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   465	        if (starving && clan.starvationStartsAtTick == 0) {
   466	            clan.starvationStartsAtTick = tick;
   467	            emit ClanStarvationChanged(clan.clanId, true, tick);
   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   469	            clan.starvationStartsAtTick = 0;
   470	            emit ClanStarvationChanged(clan.clanId, false, tick);
   471	        }
   472	    }
   473	
   474	    /// @dev Check if a clan is currently starving (lazy read).
   475	    function _isStarving(Clan storage clan) internal view returns (bool) {
   476	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   477	    }
   478	
   479	    /// @dev Resolve an action for a clansman that is in ACTING state.
   480	    function _resolveAction(
   481	        Clan storage clan,
   482	        Clansman storage cs,
   483	        Mission storage m,
   484	        uint32 clanId,
   485	        uint64 tick,
   486	        bytes32 tickSeed
   487	    ) internal {
   488	        bool starving = _isStarving(clan);
   489	        ActionType action = m.action;
   490	
   491	        if (action == ActionType.ChopWood) {
   492	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   493	        } else if (action == ActionType.MineIron) {
   494	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   495	        } else if (action == ActionType.FishDocks) {
   496	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   497	        } else if (action == ActionType.FishDeepSea) {
   498	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   499	        } else if (action == ActionType.HarvestWheat) {
   500	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   501	        } else if (action == ActionType.DepositResources) {
   502	            // Deposit event ABI intentionally stores the current tick as uint32.
   503	            // forge-lint: disable-next-line(unsafe-typecast)
   504	            _doDeposit(clan, cs, m, clanId, uint32(tick));
   505	        } else if (action == ActionType.Wait) {
   506	            // NOOP — worker stays ACTING (continuous), no transition needed
   507	            // Wait mission is effectively persistent until interrupted
   508	        } else if (action == ActionType.DefendBase) {
   509	            // Persistent mission. Registration happens atomically at order submission.
   510	        } else if (
   511	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   512	                || action == ActionType.UpgradeWall
   513	        ) {
   514	            _doBuilding(clan, cs, m, clanId, tick, action);
   515	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   516	            // Scheduled market actions: already enqueued at submitClanOrders time.
   517	            // Settlement resolves this action slot — just complete the mission.
   518	            // (Actual execution happened or will happen at heartbeat.)
   519	            _completeMission(cs, m);
   520	        }
   521	    }
   522	
   523	    // -------------------------------------------------------------------------
   524	    // Gathering helpers
   525	    // -------------------------------------------------------------------------
   526	
   527	    function _gatherWood(
   528	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   529	        Clansman storage cs,
   530	        Mission storage m,
   531	        uint32 clanId,
   532	        uint64 tick,
   533	        bool starving,
   534	        bytes32 tickSeed
   535	    ) internal {
   536	        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
   537	            _completeMission(cs, m);
   538	            return;
   539	        }
   540	
   541	        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
   542	        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
   543	        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   544	        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
   545	            yield *= 2;
   546	        }
   547	
   548	        if (starving) yield = yield / 2;
   549	        if (yield > remaining) yield = remaining;
   550	        cs.carryWood += yield;
   551	
   552	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   553	
   554	        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
   555	            _completeMission(cs, m);
   556	        }
   557	    }
   558	
   559	    function _gatherIron(
   560	        Clan storage clan,
   561	        Clansman storage cs,
   562	        Mission storage m,
   563	        uint32 clanId,
   564	        uint64 tick,
   565	        bool starving,
   566	        bytes32 tickSeed
   567	    ) internal {
   568	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   569	        if (remaining == 0) {
   570	            _completeMission(cs, m);
   571	            return;
   572	        }
   573	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   574	        if (starving) yield = yield / 2;
   575	        if (yield > remaining) yield = remaining;
   576	        cs.carryIron += yield;
   577	
   578	        // Gold bonus roll — scoped to reduce stack depth
   579	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   580	
   581	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   582	
   583	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   584	            _completeMission(cs, m);
   585	        }
   586	    }
   587	
   588	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   589	        internal
   590	        returns (uint256 goldBonus)
   591	    {
   592	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   593	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   594	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   595	            clan.goldBalance += goldBonus;
   596	        }
   597	    }
   598	
   599	    function _gatherFishDocks(
   600	        Clan storage,
   601	        Clansman storage cs,
   602	        Mission storage m,
   603	        uint32 clanId,
   604	        uint64 tick,
   605	        bool starving,
   606	        bytes32 tickSeed
   607	    ) internal {
   608	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   609	        if (remaining == 0) {
   610	            _completeMission(cs, m);
   611	            return;
   612	        }
   613	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   614	        uint256 fishRoll = uint256(fishRng) % 10000;
   615	        uint256 yield = 0;
   616	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   617	            yield = 1e18;
   618	        }
   619	        if (starving) yield = yield / 2;
   620	        if (yield > remaining) yield = remaining;

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '900,1095p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   900	
   901	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
   902	    ///         Execution order per spec §4.2 (CEI-safe):
   903	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
   904	    ///         Seed:      closedTick seed derived and published before step 1 so
   905	    ///                    settlement RNG reads real entropy, not zero.
   906	    ///         1. Settle missions completing this tick.
   907	    ///         2. Execute scheduled market actions for closedTick (external calls).
   908	    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
   909	    ///         4. Resolve world events (season boundary, winter transitions).
   910	    ///         5. Increment tick and publish (seed already written above).
   911	    function heartbeat() external override nonReentrant {
   912	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
   913	
   914	        uint64 closedTick = _world.currentTick;
   915	
   916	        // CEI: update rate-limit guard before any external calls
   917	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
   918	
   919	        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
   920	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
   921	        _tickSeeds[closedTick] = newSeed;
   922	        _world.currentTickSeed = newSeed;
   923	
   924	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
   925	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   926	        _settleCompletingMissions(closedTick);
   927	
   928	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
   929	        _executeScheduledMarketActions(closedTick);
   930	
   931	        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
   932	        // TODO Phase 3: _settleClansNearBandit(closedTick);
   933	
   934	        // Step 4: Resolve world events (season boundary, winter transitions).
   935	        _resolveWorldEvents(closedTick);
   936	
   937	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
   938	        uint64 newTick = closedTick + 1;
   939	        _world.currentTick = newTick;
   940	        _world.nextHeartbeatAtTick = newTick + 1;
   941	
   942	        emit TickAdvanced(closedTick, newTick, newSeed);
   943	    }
   944	
   945	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
   946	    ///      Called from heartbeat before market execution and tick increment.
   947	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   948	    function _settleCompletingMissions(uint64 tick) internal {
   949	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   950	            uint32 clanId = _allClanIds[i];
   951	            Clan storage clan = _clans[clanId];
   952	            if (clan.clanState == ClanState.DEAD) continue;
   953	
   954	            uint32[] storage csIds = _clanClansmanIds[clanId];
   955	            for (uint256 j = 0; j < csIds.length; j++) {
   956	                Clansman storage cs = _clansmen[csIds[j]];
   957	                if (cs.state == ClansmanState.DEAD) continue;
   958	
   959	                Mission storage m = _missions[cs.clansmanId];
   960	                if (!m.active) continue;
   961	                if (m.settlesAtTick != tick) continue; // not due this tick
   962	
   963	                // Settle this mission using the single-tick range [tick, tick+1).
   964	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   965	            }
   966	        }
   967	    }
   968	
   969	    /// @dev Resolve world events for the tick that was just closed.
   970	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
   971	    function _resolveWorldEvents(uint64 closedTick) internal {
   972	        uint64 newTick = closedTick + 1;
   973	
   974	        // --- season boundary ---
   975	        if (newTick >= _world.seasonEndTick) {
   976	            _world.currentSeasonNumber += 1;
   977	            _world.seasonStartTick = _world.seasonEndTick;
   978	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
   979	            // reset winter timers for new season
   980	            _world.winterActive = false;
   981	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   982	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   983	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   984	        }
   985	
   986	        // --- winter transitions (timer only; mechanics = Phase 10) ---
   987	        if (
   988	            !_world.winterActive && newTick >= _world.winterStartsAtTick
   989	                && _world.winterStartsAtTick < _world.seasonEndTick
   990	        ) {
   991	            _world.winterActive = true;
   992	            emit WinterStarted(newTick);
   993	        }
   994	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
   995	            _world.winterActive = false;
   996	            emit WinterEnded(newTick);
   997	            // schedule next winter cycle within this season
   998	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   999	                - ClanWorldConstants.WINTER_DURATION_TICKS;
  1000	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
  1001	            if (nextWinterStart < _world.seasonEndTick) {
  1002	                _world.winterStartsAtTick = nextWinterStart;
  1003	                _world.winterEndsAtTick = nextWinterEnd;
  1004	            } else {
  1005	                // no more winters this season; sentinel = seasonEndTick so guard never fires
  1006	                _world.winterStartsAtTick = _world.seasonEndTick;
  1007	                _world.winterEndsAtTick = _world.seasonEndTick;
  1008	            }
  1009	        }
  1010	    }
  1011	
  1012	    /// @notice Public settlement trigger — lazily settle a clan.
  1013	    function settleClan(uint32 clanId) external override nonReentrant {
  1014	        _settleClan(clanId);
  1015	    }
  1016	
  1017	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
  1018	    ///         Internally settles the entire clan (including upkeep) to guarantee
  1019	    ///         correct ordering and prevent double-settlement. Callers may call this
  1020	    ///         or settleClan interchangeably; both are safe and idempotent.
  1021	    function settleClansman(uint32 csId) external override nonReentrant {
  1022	        Clansman storage cs = _clansmen[csId];
  1023	        if (cs.clansmanId == 0) return;
  1024	        _settleClan(cs.clanId);
  1025	    }
  1026	
  1027	    /// @notice Finalize season. Phase 1 stub.
  1028	    function finalizeSeason() external override {
  1029	        // TODO Phase 3
  1030	    }
  1031	
  1032	    // =========================================================================
  1033	    // CLAN LIFECYCLE
  1034	    // =========================================================================
  1035	
  1036	    /// @notice Mint a new clan and spawn its homebase.
  1037	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1038	        require(to != address(0), "ClanWorld: zero address");
  1039	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1040	        clanId = _nextClanId++;
  1041	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1042	
  1043	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1044	        uint8[6] memory spawnRegions = [
  1045	            ClanWorldConstants.REGION_FOREST,
  1046	            ClanWorldConstants.REGION_MOUNTAINS,
  1047	            ClanWorldConstants.REGION_WEST_FARMS,
  1048	            ClanWorldConstants.REGION_EAST_FARMS,
  1049	            ClanWorldConstants.REGION_WEST_DOCKS,
  1050	            ClanWorldConstants.REGION_EAST_DOCKS
  1051	        ];
  1052	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1053	
  1054	        // Create clan
  1055	        Clan storage clan = _clans[clanId];
  1056	        clan.clanId = clanId;
  1057	        clan.iftTokenId = iftTokenId;
  1058	        clan.owner = to;
  1059	        clan.clanState = ClanState.ACTIVE;
  1060	        clan.baseRegion = baseRegion;
  1061	        clan.baseLevel = 1;
  1062	        clan.wallLevel = 0;
  1063	        clan.monumentLevel = 0;
  1064	        clan.livingClansmen = 4;
  1065	        clan.lastSettledTick = _world.currentTick;
  1066	        clan.starvationStartsAtTick = 0;
  1067	        clan.coldDamage = 0;
  1068	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1069	        clan.blueprintBalance = 0;
  1070	        // Starting vault per v4 spec §12.4
  1071	        clan.vaultWood = 20e18;
  1072	        clan.vaultIron = 0;
  1073	        clan.vaultWheat = 20e18;
  1074	        clan.vaultFish = 2e18;
  1075	
  1076	        // Wheat plots
  1077	        _wheatPlots[clanId][0] = WheatPlot({
  1078	            state: WheatPlotState.Harvestable,
  1079	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1080	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1081	            regrowUntilTick: 0
  1082	        });
  1083	        _wheatPlots[clanId][1] = WheatPlot({
  1084	            state: WheatPlotState.Harvestable,
  1085	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1086	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1087	            regrowUntilTick: 0
  1088	        });
  1089	
  1090	        // Create 4 clansmen
  1091	        for (uint256 i = 0; i < 4; i++) {
  1092	            uint32 csId = _nextClansmanId++;
  1093	            Clansman storage cs = _clansmen[csId];
  1094	            cs.clansmanId = csId;
  1095	            cs.clanId = clanId;

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1090,1385p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1090	        // Create 4 clansmen
  1091	        for (uint256 i = 0; i < 4; i++) {
  1092	            uint32 csId = _nextClansmanId++;
  1093	            Clansman storage cs = _clansmen[csId];
  1094	            cs.clansmanId = csId;
  1095	            cs.clanId = clanId;
  1096	            cs.state = ClansmanState.WAITING;
  1097	            cs.currentRegion = baseRegion;
  1098	            cs.cooldownEndsAtTs = 0;
  1099	            cs.lastMissionNonce = 0;
  1100	            cs.carryWood = 0;
  1101	            cs.carryIron = 0;
  1102	            cs.carryWheat = 0;
  1103	            cs.carryFish = 0;
  1104	            _clanClansmanIds[clanId].push(csId);
  1105	        }
  1106	
  1107	        _allClanIds.push(clanId);
  1108	
  1109	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1110	        return (clanId, iftTokenId);
  1111	    }
  1112	
  1113	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1114	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1115	        external
  1116	        override
  1117	        nonReentrant
  1118	        returns (OrderResult[] memory results)
  1119	    {
  1120	        Clan storage clan = _clans[clanId];
  1121	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1122	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1123	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1124	
  1125	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1126	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1127	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  1128	        {
  1129	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1130	            if (_world.currentTick > lastSettled + 200) {
  1131	                results = new OrderResult[](orders.length);
  1132	                for (uint256 i = 0; i < orders.length; i++) {
  1133	                    results[i] = OrderResult({
  1134	                        clansmanId: orders[i].clansmanId,
  1135	                        status: StatusCode.ERR_INVALID_ACTION,
  1136	                        cooldownEndsAtTs: 0,
  1137	                        missionNonce: 0
  1138	                    });
  1139	                }
  1140	                return results;
  1141	            }
  1142	        }
  1143	
  1144	        // Lazy settle before processing orders
  1145	        _settleClan(clanId);
  1146	
  1147	        results = new OrderResult[](orders.length);
  1148	
  1149	        for (uint256 i = 0; i < orders.length; i++) {
  1150	            results[i] = _processOrder(clanId, clan, orders[i]);
  1151	        }
  1152	
  1153	        return results;
  1154	    }
  1155	
  1156	    struct OrderCtx {
  1157	        uint8 gotoRegion;
  1158	        uint8 fromRegion;
  1159	        bool isNoop;
  1160	        uint8 travelTicks;
  1161	        uint64 arrivalTick;
  1162	        uint64 newNonce;
  1163	        uint32 targetClanId;
  1164	        bool wasActive;
  1165	        uint64 oldNonce;
  1166	    }
  1167	
  1168	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  1169	        internal
  1170	        returns (OrderResult memory result)
  1171	    {
  1172	        result.clansmanId = order.clansmanId;
  1173	
  1174	        // Validate clansman
  1175	        Clansman storage cs = _clansmen[order.clansmanId];
  1176	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  1177	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  1178	            return result;
  1179	        }
  1180	        if (cs.state == ClansmanState.DEAD) {
  1181	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  1182	            return result;
  1183	        }
  1184	
  1185	        if (order.action == ActionType.DefendBase) {
  1186	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  1187	            if (defendErr != StatusCode.OK) {
  1188	                result.status = defendErr;
  1189	                return result;
  1190	            }
  1191	
  1192	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1193	            Mission storage currentM = _missions[order.clansmanId];
  1194	            if (
  1195	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  1196	                    && currentM.targetClanId == defendTargetClanId
  1197	            ) {
  1198	                result.status = StatusCode.OK;
  1199	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1200	                result.missionNonce = currentM.nonce;
  1201	                return result;
  1202	            }
  1203	        }
  1204	
  1205	        // Cooldown check
  1206	        if (block.timestamp < cs.cooldownEndsAtTs) {
  1207	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
  1208	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1209	            result.missionNonce = cs.lastMissionNonce;
  1210	            return result;
  1211	        }
  1212	
  1213	        OrderCtx memory ctx;
  1214	        ctx.fromRegion = cs.currentRegion;
  1215	        ctx.gotoRegion = order.gotoRegion;
  1216	        ctx.targetClanId =
  1217	            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
  1218	
  1219	        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
  1220	        ctx.isNoop = order.action != ActionType.DefendBase
  1221	            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
  1222	        if (ctx.isNoop) {
  1223	            ctx.gotoRegion = ctx.fromRegion;
  1224	        }
  1225	
  1226	        // Validate target region (1-8 or 0=noop)
  1227	        if (ctx.gotoRegion > 8) {
  1228	            result.status = StatusCode.ERR_INVALID_REGION;
  1229	            return result;
  1230	        }
  1231	
  1232	        // Validate action
  1233	        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
  1234	        if (actionErr != StatusCode.OK) {
  1235	            result.status = actionErr;
  1236	            return result;
  1237	        }
  1238	
  1239	        // Capture existing mission state
  1240	        Mission storage existingM = _missions[order.clansmanId];
  1241	        ctx.wasActive = existingM.active;
  1242	        ctx.oldNonce = existingM.nonce;
  1243	
  1244	        // Compute travel from the clansman's current region to the order target.
  1245	        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
  1246	        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
  1247	
  1248	        // New nonce
  1249	        ctx.newNonce = cs.lastMissionNonce + 1;
  1250	        cs.lastMissionNonce = ctx.newNonce;
  1251	
  1252	        if (ctx.wasActive && existingM.action == ActionType.UpgradeWall) {
  1253	            _refundWallUpgradeReservation(order.clansmanId);
  1254	        }
  1255	        if (ctx.wasActive && existingM.action == ActionType.UpgradeBase) {
  1256	            _refundBaseUpgradeReservation(order.clansmanId);
  1257	        }
  1258	        if (ctx.wasActive && existingM.action == ActionType.UpgradeMonument) {
  1259	            _refundMonumentUpgradeReservation(order.clansmanId);
  1260	        }
  1261	
  1262	        if (order.action == ActionType.UpgradeWall) {
  1263	            _reserveWallUpgrade(clan, clanId, order.clansmanId, ctx.newNonce);
  1264	        }
  1265	        if (order.action == ActionType.UpgradeBase) {
  1266	            _reserveBaseUpgrade(clan, clanId, order.clansmanId, ctx.newNonce);
  1267	        }
  1268	        if (order.action == ActionType.UpgradeMonument) {
  1269	            _reserveMonumentUpgrade(clan, clanId, order.clansmanId, ctx.newNonce);
  1270	        }
  1271	
  1272	        // Install mission via helper to keep stack shallow
  1273	        _installMission(existingM, order, cs, ctx);
  1274	
  1275	        // Update clansman state
  1276	        if (ctx.travelTicks > 0) {
  1277	            cs.state = ClansmanState.TRAVELING;
  1278	        } else {
  1279	            // NOOP / same-region: no traveling state per v4.3 §A
  1280	            cs.state = ClansmanState.ACTING;
  1281	            cs.currentRegion = ctx.gotoRegion;
  1282	        }
  1283	
  1284	        // Start cooldown
  1285	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1286	
  1287	        _clearDefender(cs.clansmanId);
  1288	
  1289	        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
  1290	        // executeAtTick = arrivalTick (not arrivalTick+1).
  1291	        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
  1292	            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
  1293	        }
  1294	
  1295	        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
  1296	            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
  1297	        }
  1298	
  1299	        if (ctx.wasActive) {
  1300	            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
  1301	        }
  1302	
  1303	        emit MissionAssigned(
  1304	            clanId,
  1305	            order.clansmanId,
  1306	            ctx.newNonce,
  1307	            order.action,
  1308	            ctx.fromRegion,
  1309	            ctx.gotoRegion,
  1310	            _world.currentTick,
  1311	            ctx.arrivalTick
  1312	        );
  1313	
  1314	        result.status = StatusCode.OK;
  1315	        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1316	        result.missionNonce = ctx.newNonce;
  1317	        return result;
  1318	    }
  1319	
  1320	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  1321	        internal
  1322	    {
  1323	        m.active = true;
  1324	        m.nonce = ctx.newNonce;
  1325	        m.clansmanId = cs.clansmanId;
  1326	        m.submittedAtTick = _world.currentTick;
  1327	        m.executesAtTick = ctx.arrivalTick;
  1328	        m.settlesAtTick = order.action == ActionType.DefendBase
  1329	            ? type(uint64).max
  1330	            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
  1331	        m.startRegion = ctx.fromRegion;
  1332	        m.targetRegion = ctx.gotoRegion;
  1333	        m.action = order.action;
  1334	        m.startTick = _world.currentTick;
  1335	        m.arrivalTick = ctx.arrivalTick;
  1336	        m.actionStartTick = ctx.arrivalTick;
  1337	        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
  1338	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
  1339	            ? MarketExecutionMode.Scheduled
  1340	            : MarketExecutionMode.None;
  1341	        m.targetClanId = ctx.targetClanId;
  1342	        m.marketToken = order.marketToken;
  1343	        m.marketAmount = order.marketAmount;
  1344	        m.maxGoldIn = order.maxGoldIn;
  1345	    }
  1346	
  1347	    function _enqueueScheduledMarketAction(
  1348	        uint32 clanId,
  1349	        ClanOrder calldata order,
  1350	        uint32 clansmanId,
  1351	        uint64 executeAtTick,
  1352	        uint64 missionNonce
  1353	    ) internal {
  1354	        ScheduledMarketAction memory sma = ScheduledMarketAction({
  1355	            executeAtTick: executeAtTick,
  1356	            commitSequence: _world.nextCommitSequence++,
  1357	            missionNonce: missionNonce,
  1358	            clanId: clanId,
  1359	            clansmanId: clansmanId,
  1360	            action: order.action,
  1361	            marketToken: order.marketToken,
  1362	            marketAmount: order.marketAmount,
  1363	            maxGoldIn: order.maxGoldIn
  1364	        });
  1365	        _scheduledMarketActions[executeAtTick].push(sma);
  1366	        emit ScheduledMarketActionCommitted(
  1367	            executeAtTick,
  1368	            sma.commitSequence,
  1369	            clanId,
  1370	            clansmanId,
  1371	            order.action,
  1372	            order.marketToken,
  1373	            order.marketAmount,
  1374	            order.maxGoldIn
  1375	        );
  1376	    }
  1377	
  1378	    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
  1379	        if (_clansmanDefendingRegion[clansmanId] == region) return;
  1380	        _clearDefender(clansmanId);
  1381	
  1382	        if (_defenderCountByRegionClan[region][clanId] == 0) {
  1383	            _defendingClansByRegion[region].push(clanId);
  1384	        }
  1385	        _defenderCountByRegionClan[region][clanId]++;

exec
/bin/bash -lc "nl -ba packages/contracts/src/ClanWorld.sol | sed -n '1860,2065p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1860	
  1861	    function _clearBaseUpgradeReservation(uint32 clansmanId) internal {
  1862	        BaseUpgradeReservation storage reservation = _baseUpgradeReservations[clansmanId];
  1863	        if (!reservation.active) return;
  1864	
  1865	        uint32 clanId = reservation.clanId;
  1866	        if (_pendingBaseUpgradesByClan[clanId] > 0) {
  1867	            _pendingBaseUpgradesByClan[clanId] -= 1;
  1868	        }
  1869	
  1870	        delete _baseUpgradeReservations[clansmanId];
  1871	    }
  1872	
  1873	    function _validateUpgradeMonumentOrder(Clan storage clan, uint32 clansmanId) internal view returns (StatusCode) {
  1874	        uint8 pendingUpgrades = _pendingMonumentUpgradesByClan[clan.clanId];
  1875	        uint256 availableWood = clan.vaultWood;
  1876	        uint256 availableIron = clan.vaultIron;
  1877	        uint256 availableWheat = clan.vaultWheat;
  1878	        uint256 availableBlueprint = clan.blueprintBalance;
  1879	
  1880	        MonumentUpgradeReservation storage existing = _monumentUpgradeReservations[clansmanId];
  1881	        if (existing.active && existing.clanId == clan.clanId) {
  1882	            pendingUpgrades -= 1;
  1883	            availableWood += existing.woodCost;
  1884	            availableIron += existing.ironCost;
  1885	            availableWheat += existing.wheatCost;
  1886	            availableBlueprint += existing.blueprintCost;
  1887	        }
  1888	
  1889	        uint8 plannedCurrentLevel = clan.monumentLevel + pendingUpgrades;
  1890	        if (plannedCurrentLevel >= MONUMENT_MAX_LEVEL) return StatusCode.ERR_INVALID_ACTION;
  1891	
  1892	        (uint256 woodCost, uint256 ironCost, uint256 wheatCost, uint256 blueprintCost) =
  1893	            _monumentUpgradeCost(plannedCurrentLevel);
  1894	        if (
  1895	            availableWood < woodCost || availableIron < ironCost || availableWheat < wheatCost
  1896	                || availableBlueprint < blueprintCost
  1897	        ) {
  1898	            return StatusCode.ERR_MISSING_RESOURCES;
  1899	        }
  1900	
  1901	        return StatusCode.OK;
  1902	    }
  1903	
  1904	    function _reserveMonumentUpgrade(Clan storage clan, uint32 clanId, uint32 clansmanId, uint64 missionNonce)
  1905	        internal
  1906	    {
  1907	        uint8 plannedCurrentLevel = clan.monumentLevel + _pendingMonumentUpgradesByClan[clanId];
  1908	        (uint256 woodCost, uint256 ironCost, uint256 wheatCost, uint256 blueprintCost) =
  1909	            _monumentUpgradeCost(plannedCurrentLevel);
  1910	
  1911	        clan.vaultWood -= woodCost;
  1912	        clan.vaultIron -= ironCost;
  1913	        clan.vaultWheat -= wheatCost;
  1914	        clan.blueprintBalance -= blueprintCost;
  1915	        _pendingMonumentUpgradesByClan[clanId] += 1;
  1916	
  1917	        _monumentUpgradeReservations[clansmanId] = MonumentUpgradeReservation({
  1918	            active: true,
  1919	            clanId: clanId,
  1920	            missionNonce: missionNonce,
  1921	            woodCost: woodCost,
  1922	            ironCost: ironCost,
  1923	            wheatCost: wheatCost,
  1924	            blueprintCost: blueprintCost
  1925	        });
  1926	    }
  1927	
  1928	    function _refundMonumentUpgradeReservation(uint32 clansmanId) internal {
  1929	        MonumentUpgradeReservation storage reservation = _monumentUpgradeReservations[clansmanId];
  1930	        if (!reservation.active) return;
  1931	
  1932	        Clan storage clan = _clans[reservation.clanId];
  1933	        clan.vaultWood += reservation.woodCost;
  1934	        clan.vaultIron += reservation.ironCost;
  1935	        clan.vaultWheat += reservation.wheatCost;
  1936	        clan.blueprintBalance += reservation.blueprintCost;
  1937	        _clearMonumentUpgradeReservation(clansmanId);
  1938	    }
  1939	
  1940	    function _clearMonumentUpgradeReservation(uint32 clansmanId) internal {
  1941	        MonumentUpgradeReservation storage reservation = _monumentUpgradeReservations[clansmanId];
  1942	        if (!reservation.active) return;
  1943	
  1944	        uint32 clanId = reservation.clanId;
  1945	        if (_pendingMonumentUpgradesByClan[clanId] > 0) {
  1946	            _pendingMonumentUpgradesByClan[clanId] -= 1;
  1947	        }
  1948	
  1949	        delete _monumentUpgradeReservations[clansmanId];
  1950	    }
  1951	
  1952	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  1953	        internal
  1954	        view
  1955	        returns (StatusCode)
  1956	    {
  1957	        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1958	        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
  1959	        return StatusCode.OK;
  1960	    }
  1961	
  1962	    // =========================================================================
  1963	    // TREASURY / POOL SEEDING
  1964	    // =========================================================================
  1965	
  1966	    /// @notice One-time treasury initialization: register token and pool addresses.
  1967	    ///         Must be called before seedPools. Callable only once.
  1968	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  1969	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  1970	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1971	
  1972	        _treasury.woodToken = tokens[0];
  1973	        _treasury.ironToken = tokens[1];
  1974	        _treasury.wheatToken = tokens[2];
  1975	        _treasury.fishToken = tokens[3];
  1976	        _treasury.goldToken = tokens[4];
  1977	        _treasury.blueprintToken = tokens[5];
  1978	
  1979	        _treasury.woodGoldPool = pools[0];
  1980	        _treasury.wheatGoldPool = pools[1];
  1981	        _treasury.fishGoldPool = pools[2];
  1982	        _treasury.ironGoldPool = pools[3];
  1983	    }
  1984	
  1985	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  1986	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  1987	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1988	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  1989	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  1990	
  1991	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  1992	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  1993	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  1994	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  1995	
  1996	        _treasury.poolsSeeded = true;
  1997	
  1998	        emit PoolsSeeded(
  1999	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  2000	        );
  2001	    }
  2002	
  2003	    // =========================================================================
  2004	    // OTC TRANSFERS
  2005	    // =========================================================================
  2006	
  2007	    function transferGold(uint32, uint32, uint256) external pure override {
  2008	        revert("OTC transfers not implemented");
  2009	    }
  2010	
  2011	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  2012	        revert("OTC transfers not implemented");
  2013	    }
  2014	
  2015	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  2016	        revert("OTC transfers not implemented");
  2017	    }
  2018	
  2019	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  2020	        external
  2021	        pure
  2022	        override
  2023	    {
  2024	        revert("OTC transfers not implemented");
  2025	    }
  2026	
  2027	    // =========================================================================
  2028	    // RAW READ GETTERS
  2029	    // =========================================================================
  2030	
  2031	    function getWorldState() external view override returns (WorldState memory) {
  2032	        return _world;
  2033	    }
  2034	
  2035	    function getTreasuryState() external view override returns (TreasuryState memory) {
  2036	        return _treasury;
  2037	    }
  2038	
  2039	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  2040	        return _clans[clanId];
  2041	    }
  2042	
  2043	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  2044	        return _clansmen[clansmanId];
  2045	    }
  2046	
  2047	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  2048	        return _missions[clansmanId];
  2049	    }
  2050	
  2051	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  2052	        external
  2053	        view
  2054	        override
  2055	        returns (uint64 submitted, uint64 executes, uint64 settles)
  2056	    {
  2057	        Mission memory m = _missions[clansmanId];
  2058	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  2059	            return (0, 0, 0);
  2060	        }
  2061	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  2062	    }
  2063	
  2064	    function getWallUpgradeCost(uint8 currentLevel) public pure override returns (uint256 wood, uint256 iron) {
  2065	        return _wallUpgradeCost(currentLevel);

exec
/bin/bash -lc "nl -ba packages/contracts/src/IClanWorld.sol | sed -n '1,240p'" in /home/claude/code/clan-world
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
    43	    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
    44	    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
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
    87	    // Sentinels
    88	    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    89	    uint32 internal constant BANDIT_ID_NULL = 0;
    90	}
    91	
    92	// =============================================================================
    93	// ENUMS
    94	// =============================================================================
    95	
    96	enum ClanState {
    97	    ACTIVE,
    98	    DEAD
    99	}
   100	
   101	enum ClansmanState {
   102	    WAITING,
   103	    TRAVELING,
   104	    ACTING,
   105	    DEAD
   106	}
   107	
   108	enum BanditState {
   109	    NONE,
   110	    CAMPING,
   111	    RESTING,
   112	    ATTACKING,
   113	    DEFEATED,
   114	    ESCAPED
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
   144	    Wait,
   145	    UpgradeWall
   146	}
   147	
   148	enum MarketExecutionMode {
   149	    None,
   150	    Immediate,
   151	    Scheduled
   152	}
   153	
   154	enum StatusCode {
   155	    OK,
   156	    ERR_CLAN_DEAD,
   157	    ERR_CLAN_NOT_OWNED,
   158	    ERR_CLANSMAN_DEAD,
   159	    ERR_INVALID_CLANSMAN,
   160	    ERR_INVALID_REGION,
   161	    ERR_INVALID_ACTION,
   162	    ERR_INVALID_TARGET,
   163	    ERR_COOLDOWN_ACTIVE,
   164	    ERR_NOT_WAITING,
   165	    ERR_NOT_IN_UNICORN_TOWN,
   166	    ERR_NOT_AT_HOMEBASE,
   167	    ERR_NOT_AT_TARGET_BASE,
   168	    ERR_NOT_DEFENDABLE,
   169	    ERR_MISSING_RESOURCES,
   170	    ERR_EMPTY_CARGO,
   171	    ERR_PLOT_NOT_READY,
   172	    ERR_PLOT_EMPTY,
   173	    ERR_MARKET_ZERO_AMOUNT,
   174	    ERR_MARKET_UNSUPPORTED_TOKEN,
   175	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   176	    ERR_MARKET_BUY_OVER_CAPACITY,
   177	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   178	    ERR_WORLD_TICK_MISMATCH,
   179	    ERR_NO_ACTIVE_BANDIT,
   180	    ERR_SEASON_ENDED,
   181	    ERR_NOT_ENOUGH_GOLD,
   182	    ERR_CARRY_FULL
   183	}
   184	
   185	// =============================================================================
   186	// CORE STATE STRUCTS (raw storage shape)
   187	// =============================================================================
   188	
   189	struct WorldState {
   190	    uint64 currentTick;
   191	    uint64 seasonStartTick;
   192	    uint64 seasonEndTick;
   193	    bool seasonFinalized;
   194	    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
   195	    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
   196	
   197	    uint64 nextHeartbeatAtTs;
   198	    uint64 nextBanditSpawnEligibleTick;
   199	    uint16 currentBanditSpawnChanceBps;
   200	    bytes32 currentTickSeed;
   201	
   202	    uint32 activeBanditId; // 0 if none
   203	    bool winterActive;
   204	    uint64 winterStartsAtTick;
   205	    uint64 winterEndsAtTick; // 0 if not active
   206	
   207	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
   208	}
   209	
   210	struct TreasuryState {
   211	    address treasuryOwner;
   212	    uint256 prizePotGold;
   213	
   214	    bool poolsSeeded;
   215	
   216	    address woodToken;
   217	    address wheatToken;
   218	    address fishToken;
   219	    address ironToken;
   220	    address goldToken;
   221	    address blueprintToken;
   222	
   223	    address woodGoldPool;
   224	    address wheatGoldPool;
   225	    address fishGoldPool;
   226	    address ironGoldPool;
   227	}
   228	
   229	struct Clan {
   230	    uint32 clanId;
   231	    uint256 iftTokenId;
   232	    address owner;
   233	    ClanState clanState;
   234	
   235	    uint8 baseRegion;
   236	    uint8 baseLevel;
   237	    uint8 wallLevel;
   238	    uint8 monumentLevel;
   239	    uint8 livingClansmen;
   240	

exec
/bin/bash -lc "nl -ba packages/contracts/test/ClanWorld.t.sol | sed -n '1980,2365p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1980	
  1981	        // Order 2: valid — cs1 Wait at homebase (NOOP)
  1982	        orders[2] = ClanOrder({
  1983	            clansmanId: cs1,
  1984	            gotoRegion: ClanWorldConstants.REGION_NOOP,
  1985	            action: ActionType.Wait,
  1986	            targetClanId: 0,
  1987	            marketToken: address(0),
  1988	            marketAmount: 0,
  1989	            maxGoldIn: 0
  1990	        });
  1991	
  1992	        // Order 3: invalid — cs2 ChopWood to Mountains (wrong region for ChopWood)
  1993	        orders[3] = ClanOrder({
  1994	            clansmanId: cs2,
  1995	            gotoRegion: ClanWorldConstants.REGION_MOUNTAINS,
  1996	            action: ActionType.ChopWood,
  1997	            targetClanId: 0,
  1998	            marketToken: address(0),
  1999	            marketAmount: 0,
  2000	            maxGoldIn: 0
  2001	        });
  2002	
  2003	        vm.prank(elder);
  2004	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
  2005	
  2006	        assertEq(results.length, 4, "3.E8: must return 4 results");
  2007	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "3.E8: order[0] must be OK");
  2008	        assertEq(
  2009	            uint8(results[1].status),
  2010	            uint8(StatusCode.ERR_INVALID_CLANSMAN),
  2011	            "3.E8: order[1] must be ERR_INVALID_CLANSMAN"
  2012	        );
  2013	        assertEq(uint8(results[2].status), uint8(StatusCode.OK), "3.E8: order[2] must be OK");
  2014	        assertEq(
  2015	            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"
  2016	        );
  2017	    }
  2018	
  2019	    // =====================================================================
  2020	    // Phase 4.4 — season + winter timer tests
  2021	    // =====================================================================
  2022	
  2023	    function test_season_initialState() public {
  2024	        WorldState memory ws = world.getWorldState();
  2025	        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
  2026	        assertEq(ws.seasonStartTick, 0);
  2027	        assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
  2028	        assertEq(
  2029	            ws.winterStartsAtTick,
  2030	            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
  2031	        );
  2032	        assertFalse(ws.winterActive);
  2033	    }
  2034	
  2035	    function test_winter_onset() public {
  2036	        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
  2037	        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
  2038	        // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
  2039	        uint64 winterStart =
  2040	            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
  2041	        for (uint64 i = 0; i < winterStart - 1; i++) {
  2042	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2043	            world.heartbeat();
  2044	        }
  2045	        // currentTick == 99; next heartbeat opens tick 100 and should emit WinterStarted(100)
  2046	        vm.recordLogs();
  2047	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2048	        world.heartbeat();
  2049	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2050	
  2051	        bytes32 winterSig = keccak256("WinterStarted(uint64)");
  2052	        bool foundWinterStarted = false;
  2053	        for (uint256 i = 0; i < logs.length; i++) {
  2054	            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
  2055	                foundWinterStarted = true;
  2056	                break;
  2057	            }
  2058	        }
  2059	        assertTrue(foundWinterStarted, "WinterStarted event should have been emitted at tick 100");
  2060	        assertTrue(world.getWorldState().winterActive, "winter should be active");
  2061	        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
  2062	    }
  2063	
  2064	    function test_winter_end_and_next_cycle() public {
  2065	        // Advance past first winter end tick (= 110)
  2066	        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
  2067	        for (uint64 i = 0; i <= winterEnd; i++) {
  2068	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2069	            world.heartbeat();
  2070	        }
  2071	        WorldState memory ws = world.getWorldState();
  2072	        assertFalse(ws.winterActive, "winter should be over");
  2073	        // next winter at [210, 220)
  2074	        assertEq(
  2075	            ws.winterStartsAtTick,
  2076	            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
  2077	        );
  2078	    }
  2079	
  2080	    function test_season_transition() public {
  2081	        // Advance SEASON_DURATION_TICKS heartbeats to cross season boundary
  2082	        for (uint256 i = 0; i < ClanWorldConstants.SEASON_DURATION_TICKS; i++) {
  2083	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2084	            world.heartbeat();
  2085	        }
  2086	        WorldState memory ws = world.getWorldState();
  2087	        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
  2088	        assertEq(ws.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS);
  2089	        assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2);
  2090	        // winter reset for new season
  2091	        uint64 expectedWinterStart = ClanWorldConstants.SEASON_DURATION_TICKS
  2092	            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
  2093	        assertEq(ws.winterStartsAtTick, expectedWinterStart);
  2094	    }
  2095	
  2096	    function test_nextHeartbeatAtTick_tracks_tick() public {
  2097	        WorldState memory ws0 = world.getWorldState();
  2098	        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
  2099	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2100	        world.heartbeat();
  2101	        WorldState memory ws1 = world.getWorldState();
  2102	        assertEq(ws1.currentTick, 1);
  2103	        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
  2104	    }
  2105	}

codex
The working tree still has older contract/test snapshots, so I’m switching to `git show` against `e4a0d4c` for the exact reviewed head. That’ll keep the line references accurate to the PR instead of the local base state.
exec
/bin/bash -lc 'git rev-parse --verify e4a0d4c''^{commit}' in /home/claude/code/clan-world
 succeeded in 0ms:
e4a0d4c2c332abbc2064572364e20a85b322e169

exec
/bin/bash -lc 'git show --stat --oneline --decorate=short e4a0d4c' in /home/claude/code/clan-world
 succeeded in 0ms:
e4a0d4c (origin/dev-phase-10-winter) fix(contracts): add ERR_MUST_SETTLE_FIRST + initialize winterStartsAtTick (Closes #231 #233) (#259)
 packages/contracts/src/ClanWorld.sol    |  3 +--
 packages/contracts/src/IClanWorld.sol   |  3 ++-
 packages/contracts/test/ClanWorld.t.sol | 18 +++++++++++++-----
 3 files changed, 16 insertions(+), 8 deletions(-)

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '360,650p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   374	            if (clan.clanState == ClanState.DEAD) break;
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
   393	        if (curTick > fromTick && !_isWinterActiveAt(curTick) && _isWinterActiveAt(curTick - 1)) {
   394	            clan.coldDamage = 0;
   395	        }
   396	
   397	        clan.lastSettledTick = curTick;
   398	        emit ClanSettled(clanId, curTick);
   399	    }
   400	
   401	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
   402	    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
   403	        bool winter = _isWinterActiveAt(tick);
   404	        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
   405	            clan.coldDamage = 0;
   406	        }
   407	
   408	        if (clan.livingClansmen == 0) return;
   409	
   410	        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
   411	        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
   412	        if (winter) {
   413	            wheatNeeded = wheatNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
   414	            fishNeeded = fishNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
   415	        }
   416	
   417	        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
   418	        bool hadEnoughFish = clan.vaultFish >= fishNeeded;
   419	
   420	        if (hadEnoughWheat) {
   421	            clan.vaultWheat -= wheatNeeded;
   422	        } else {
   423	            clan.vaultWheat = 0;
   424	        }
   425	        if (hadEnoughFish) {
   426	            clan.vaultFish -= fishNeeded;
   427	        } else {
   428	            clan.vaultFish = 0;
   429	        }
   430	
   431	        bool starving = !hadEnoughWheat || !hadEnoughFish;
   432	        if (starving && clan.starvationStartsAtTick == 0) {
   433	            clan.starvationStartsAtTick = tick;
   434	            emit ClanStarvationChanged(clan.clanId, true, tick);
   435	        } else if (!starving && clan.starvationStartsAtTick != 0) {
   436	            clan.starvationStartsAtTick = 0;
   437	            emit ClanStarvationChanged(clan.clanId, false, tick);
   438	        }
   439	
   440	        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
   441	        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
   442	            _killNextClansmanFromStarvation(clan, tick);
   443	        }
   444	        if (clan.clanState == ClanState.DEAD) return;
   445	
   446	        if (winter) {
   447	            uint256 woodNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
   448	            if (clan.vaultWood >= woodNeeded) {
   449	                clan.vaultWood -= woodNeeded;
   450	            } else {
   451	                uint256 woodShort = woodNeeded - clan.vaultWood;
   452	                clan.vaultWood = 0;
   453	                uint16 oldColdDamage = clan.coldDamage;
   454	                if (clan.coldDamage < type(uint16).max) {
   455	                    clan.coldDamage += 1;
   456	                }
   457	                emit ClanColdShortage(clan.clanId, _eventTick(tick), woodShort);
   458	                _applyColdDamageConsequence(clan, tick, oldColdDamage);
   459	            }
   460	        }
   461	    }
   462	
   463	    function _applyColdDamageConsequence(Clan storage clan, uint64 tick, uint16 oldColdDamage) internal {
   464	        uint16 newColdDamage = clan.coldDamage;
   465	        if (newColdDamage == oldColdDamage) return;
   466	
   467	        if (clan.wallLevel > 0) {
   468	            if (
   469	                newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
   470	                    <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_WALL_DEGRADATION
   471	            ) return;
   472	
   473	            clan.wallLevel--;
   474	            emit WallDegradedByCold(clan.clanId, clan.wallLevel, _eventTick(tick));
   475	            return;
   476	        }
   477	
   478	        if (
   479	            newColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
   480	                <= oldColdDamage / ClanWorldConstants.COLD_DAMAGE_PER_CLANSMAN_DEATH
   481	        ) return;
   482	
   483	        _killRandomClansmanFromCold(clan, tick, newColdDamage);
   484	    }
   485	
   486	    function _killRandomClansmanFromCold(Clan storage clan, uint64 tick, uint16 coldDamage) internal {
   487	        if (clan.livingClansmen == 0) return;
   488	
   489	        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
   490	        uint256 livingCount = 0;
   491	        for (uint256 i = 0; i < csIds.length; i++) {
   492	            if (_clansmen[csIds[i]].state != ClansmanState.DEAD) {
   493	                livingCount++;
   494	            }
   495	        }
   496	        if (livingCount == 0) return;
   497	
   498	        uint256 pick = RNG.rngBounded(
   499	            _world.currentTickSeed,
   500	            RNG.DOMAIN_COLD_DAMAGE,
   501	            uint256(keccak256(abi.encodePacked(clan.clanId, tick, coldDamage))),
   502	            livingCount
   503	        );
   504	
   505	        uint256 seen = 0;
   506	        for (uint256 i = 0; i < csIds.length; i++) {
   507	            Clansman storage cs = _clansmen[csIds[i]];
   508	            if (cs.state == ClansmanState.DEAD) continue;
   509	            if (seen != pick) {
   510	                seen++;
   511	                continue;
   512	            }
   513	
   514	            _markClansmanDeadFromCold(clan, cs, tick);
   515	            return;
   516	        }
   517	    }
   518	
   519	    function _killNextClansmanFromStarvation(Clan storage clan, uint64 tick) internal {
   520	        if (clan.livingClansmen == 0) return;
   521	
   522	        uint32[] storage csIds = _clanClansmanIds[clan.clanId];
   523	        for (uint256 i = 0; i < csIds.length; i++) {
   524	            Clansman storage cs = _clansmen[csIds[i]];
   525	            if (cs.state == ClansmanState.DEAD) continue;
   526	
   527	            _markClansmanDead(clan, cs);
   528	            if (clan.livingClansmen == 0) {
   529	                _markClanDead(clan.clanId, "starvation", tick);
   530	            }
   531	            return;
   532	        }
   533	    }
   534	
   535	    function _markClansmanDeadFromCold(Clan storage clan, Clansman storage cs, uint64 tick) internal {
   536	        _markClansmanDead(clan, cs);
   537	
   538	        emit ClansmanColdDeath(clan.clanId, cs.clansmanId, _eventTick(tick));
   539	        if (clan.livingClansmen == 0) {
   540	            _markClanDead(clan.clanId, "cold", tick);
   541	        }
   542	    }
   543	
   544	    function _markClansmanDead(Clan storage clan, Clansman storage cs) internal {
   545	        if (cs.state == ClansmanState.DEAD) return;
   546	
   547	        cs.state = ClansmanState.DEAD;
   548	        if (clan.livingClansmen > 0) {
   549	            clan.livingClansmen--;
   550	        }
   551	
   552	        Mission storage m = _missions[cs.clansmanId];
   553	        if (m.active) {
   554	            if (m.action == ActionType.DefendBase) {
   555	                _clearDefender(cs.clansmanId);
   556	            }
   557	            m.active = false;
   558	        }
   559	    }
   560	
   561	    function _markClanDead(uint32 clanId) internal {
   562	        _markClanDead(clanId, "unknown", _world.currentTick);
   563	    }
   564	
   565	    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {
   566	        Clan storage clan = _clans[clanId];
   567	        if (clan.clanId == 0 || clan.clanState == ClanState.DEAD) return;
   568	
   569	        clan.clanState = ClanState.DEAD;
   570	        clan.vaultWood = 0;
   571	        clan.vaultWheat = 0;
   572	        clan.vaultFish = 0;
   573	        clan.vaultIron = 0;
   574	        clan.starvationStartsAtTick = 0;
   575	        clan.livingClansmen = 0;
   576	
   577	        uint32[] storage csIds = _clanClansmanIds[clanId];
   578	        for (uint256 i = 0; i < csIds.length; i++) {
   579	            _clansmen[csIds[i]].state = ClansmanState.DEAD;
   580	            Mission storage m = _missions[csIds[i]];
   581	            if (m.active) {
   582	                if (m.action == ActionType.DefendBase) {
   583	                    _clearDefender(csIds[i]);
   584	                }
   585	                m.active = false;
   586	            }
   587	        }
   588	
   589	        emit ClanEliminated(clanId, tick);
   590	        emit ClanDied(clanId, tick, reason);
   591	    }
   592	
   593	    /// @dev Check if a clan is currently starving (lazy read).
   594	    function _isStarving(Clan storage clan) internal view returns (bool) {
   595	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
   596	    }
   597	
   598	    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
   599	    function _resolveAction(
   600	        Clan storage clan,
   601	        Clansman storage cs,
   602	        Mission storage m,
   603	        uint32 clanId,
   604	        uint64 tick,
   605	        bytes32 tickSeed
   606	    ) internal {
   607	        bool starving = _isStarving(clan);
   608	        ActionType action = m.action;
   609	
   610	        if (action == ActionType.ChopWood) {
   611	            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
   612	        } else if (action == ActionType.MineIron) {
   613	            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
   614	        } else if (action == ActionType.FishDocks) {
   615	            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
   616	        } else if (action == ActionType.FishDeepSea) {
   617	            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
   618	        } else if (action == ActionType.HarvestWheat) {
   619	            _gatherWheat(clan, cs, m, clanId, tick, starving);
   620	        } else if (action == ActionType.DepositResources) {
   621	            _doDeposit(clan, cs, m, clanId, tick);
   622	        } else if (action == ActionType.Wait) {
   623	            // NOOP — worker stays ACTING (continuous), no transition needed
   624	            // Wait mission is effectively persistent until interrupted
   625	        } else if (action == ActionType.DefendBase) {
   626	            // Persistent mission. Registration happens atomically at order submission.
   627	        } else if (
   628	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
   629	        ) {
   630	            // Phase 1 stub: check homebase, check resources; if ok, stub success
   631	            _doBuilding(clan, cs, m, clanId, tick, action);
   632	        } else if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
   633	            // Scheduled market actions: already enqueued at submitClanOrders time.
   634	            // Settlement resolves this action slot — just complete the mission.
   635	            // (Actual execution happened or will happen at heartbeat.)
   636	            _completeMission(cs, m);
   637	        }
   638	    }
   639	
   640	    // -------------------------------------------------------------------------
   641	    // Gathering helpers
   642	    // -------------------------------------------------------------------------
   643	
   644	    function _gatherWood(
   645	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   646	        Clansman storage cs,
   647	        Mission storage m,
   648	        uint32 clanId,
   649	        uint64 tick,
   650	        bool starving,

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '930,1125p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   930	        clan.wallLevel = nextLevel;
   931	        emit WallLevelChanged(clanId, old, nextLevel, tick);
   932	        return true;
   933	    }
   934	
   935	    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   936	        uint8 nextLevel = clan.baseLevel + 1;
   937	        if (nextLevel > 5) return false;
   938	
   939	        uint256 woodCost;
   940	        uint256 ironCost;
   941	        uint256 wheatCost;
   942	
   943	        if (nextLevel == 2) {
   944	            woodCost = 40e18;
   945	            ironCost = 0;
   946	            wheatCost = 20e18;
   947	        } else if (nextLevel == 3) {
   948	            woodCost = 60e18;
   949	            ironCost = 5e18;
   950	            wheatCost = 30e18;
   951	        } else if (nextLevel == 4) {
   952	            woodCost = 80e18;
   953	            ironCost = 10e18;
   954	            wheatCost = 40e18;
   955	        } else {
   956	            woodCost = 100e18;
   957	            ironCost = 15e18;
   958	            wheatCost = 50e18;
   959	        }
   960	
   961	        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost || clan.vaultWheat < wheatCost) return false;
   962	
   963	        clan.vaultWood -= woodCost;
   964	        clan.vaultIron -= ironCost;
   965	        clan.vaultWheat -= wheatCost;
   966	        uint8 old = clan.baseLevel;
   967	        clan.baseLevel = nextLevel;
   968	        emit BaseLevelChanged(clanId, old, nextLevel, tick);
   969	        return true;
   970	    }
   971	
   972	    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   973	        uint8 nextLevel = clan.monumentLevel + 1;
   974	        if (nextLevel > 10) return false;
   975	
   976	        uint256 woodCost;
   977	        uint256 wheatCost;
   978	        uint256 ironCost;
   979	        uint256 blueprintCost;
   980	
   981	        if (nextLevel == 1) {
   982	            woodCost = 30e18;
   983	            wheatCost = 20e18;
   984	        } else if (nextLevel == 2) {
   985	            woodCost = 50e18;
   986	            wheatCost = 30e18;
   987	        } else if (nextLevel == 3) {
   988	            woodCost = 70e18;
   989	            wheatCost = 40e18;
   990	            ironCost = 5e18;
   991	        } else if (nextLevel == 4) {
   992	            woodCost = 90e18;
   993	            wheatCost = 50e18;
   994	            ironCost = 10e18;
   995	        } else if (nextLevel == 5) {
   996	            woodCost = 120e18;
   997	            wheatCost = 60e18;
   998	            ironCost = 15e18;
   999	        } else if (nextLevel == 6) {
  1000	            woodCost = 150e18;
  1001	            wheatCost = 80e18;
  1002	            ironCost = 20e18;
  1003	        } else if (nextLevel <= 10) {
  1004	            woodCost = 200e18;
  1005	            wheatCost = 100e18;
  1006	            ironCost = 25e18;
  1007	            blueprintCost = 1e18;
  1008	        }
  1009	
  1010	        if (
  1011	            clan.vaultWood < woodCost || clan.vaultWheat < wheatCost || clan.vaultIron < ironCost
  1012	                || clan.blueprintBalance < blueprintCost
  1013	        ) return false;
  1014	
  1015	        clan.vaultWood -= woodCost;
  1016	        clan.vaultWheat -= wheatCost;
  1017	        clan.vaultIron -= ironCost;
  1018	        clan.blueprintBalance -= blueprintCost;
  1019	
  1020	        uint8 old = clan.monumentLevel;
  1021	        clan.monumentLevel = nextLevel;
  1022	        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
  1023	        return true;
  1024	    }
  1025	
  1026	    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
  1027	    function _completeMission(Clansman storage cs, Mission storage m) internal {
  1028	        cs.state = ClansmanState.WAITING;
  1029	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1030	        m.active = false;
  1031	        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
  1032	    }
  1033	
  1034	    // =========================================================================
  1035	    // WORLD PROGRESSION
  1036	    // =========================================================================
  1037	
  1038	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
  1039	    ///         Execution order per spec §4.2 (CEI-safe):
  1040	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
  1041	    ///         Seed:      closedTick seed derived and published before step 1 so
  1042	    ///                    settlement RNG reads real entropy, not zero.
  1043	    ///         1. Settle missions completing this tick.
  1044	    ///         2. Execute scheduled market actions for closedTick (external calls).
  1045	    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
  1046	    ///         4. Resolve world events (season boundary, winter transitions).
  1047	    ///         5. Increment tick and publish (seed already written above).
  1048	    function heartbeat() external override nonReentrant {
  1049	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
  1050	
  1051	        uint64 closedTick = _world.currentTick;
  1052	
  1053	        // CEI: update rate-limit guard before any external calls
  1054	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
  1055	
  1056	        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
  1057	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
  1058	        _tickSeeds[closedTick] = newSeed;
  1059	        _world.currentTickSeed = newSeed;
  1060	
  1061	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
  1062	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  1063	        _settleCompletingMissions(closedTick);
  1064	
  1065	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
  1066	        _executeScheduledMarketActions(closedTick);
  1067	
  1068	        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
  1069	        // TODO Phase 3: _settleClansNearBandit(closedTick);
  1070	
  1071	        // Step 4: Resolve world events (season boundary, winter transitions).
  1072	        _resolveWorldEvents(closedTick);
  1073	
  1074	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
  1075	        uint64 newTick = closedTick + 1;
  1076	        _world.currentTick = newTick;
  1077	        _world.nextHeartbeatAtTick = newTick + 1;
  1078	
  1079	        emit TickAdvanced(closedTick, newTick, newSeed);
  1080	    }
  1081	
  1082	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
  1083	    ///      Called from heartbeat before market execution and tick increment.
  1084	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
  1085	    function _settleCompletingMissions(uint64 tick) internal {
  1086	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1087	            uint32 clanId = _allClanIds[i];
  1088	            Clan storage clan = _clans[clanId];
  1089	            if (clan.clanState == ClanState.DEAD) continue;
  1090	
  1091	            uint32[] storage csIds = _clanClansmanIds[clanId];
  1092	            for (uint256 j = 0; j < csIds.length; j++) {
  1093	                Clansman storage cs = _clansmen[csIds[j]];
  1094	                if (cs.state == ClansmanState.DEAD) continue;
  1095	
  1096	                Mission storage m = _missions[cs.clansmanId];
  1097	                if (!m.active) continue;
  1098	                if (m.settlesAtTick != tick) continue; // not due this tick
  1099	
  1100	                // Settle this mission using the single-tick range [tick, tick+1).
  1101	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
  1102	            }
  1103	        }
  1104	    }
  1105	
  1106	    /// @dev Resolve world events for the tick that was just closed.
  1107	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
  1108	    function _resolveWorldEvents(uint64 closedTick) internal {
  1109	        uint64 newTick = closedTick + 1;
  1110	
  1111	        // --- season boundary ---
  1112	        if (newTick >= _world.seasonEndTick) {
  1113	            _world.currentSeasonNumber += 1;
  1114	            _world.seasonStartTick = _world.seasonEndTick;
  1115	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
  1116	        }
  1117	
  1118	        // --- winter transitions (timer only; mechanics = Phase 10) ---
  1119	        bool wasWinter = _isWinterActiveAt(closedTick);
  1120	        bool nowWinter = _isWinterActiveAt(newTick);
  1121	        if (!wasWinter && nowWinter) {
  1122	            _lockWheatPlotsForWinter();
  1123	            emit WinterStarted(_winterEventTick(newTick));
  1124	        }
  1125	        if (wasWinter && !nowWinter) {

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1120,1385p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1120	        bool nowWinter = _isWinterActiveAt(newTick);
  1121	        if (!wasWinter && nowWinter) {
  1122	            _lockWheatPlotsForWinter();
  1123	            emit WinterStarted(_winterEventTick(newTick));
  1124	        }
  1125	        if (wasWinter && !nowWinter) {
  1126	            _resetColdDamageForAllClans();
  1127	            _restartWheatPlotsAfterWinter(newTick);
  1128	            emit WinterEnded(_winterEventTick(newTick));
  1129	        }
  1130	    }
  1131	
  1132	    function _lockWheatPlotsForWinter() internal {
  1133	        uint256 transitions;
  1134	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1135	            uint32 clanId = _allClanIds[i];
  1136	            for (uint256 pi = 0; pi < 2; pi++) {
  1137	                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
  1138	                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
  1139	                transitions++;
  1140	            }
  1141	        }
  1142	    }
  1143	
  1144	    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
  1145	        uint256 transitions;
  1146	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1147	            uint32 clanId = _allClanIds[i];
  1148	            for (uint256 pi = 0; pi < 2; pi++) {
  1149	                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
  1150	                WheatPlot storage plot = _wheatPlots[clanId][pi];
  1151	                if (plot.state == WheatPlotState.WinterLocked) {
  1152	                    plot.state = WheatPlotState.Regrowing;
  1153	                    plot.remainingWheat = 0;
  1154	                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
  1155	                }
  1156	                transitions++;
  1157	            }
  1158	        }
  1159	    }
  1160	
  1161	    function _resetColdDamageForAllClans() internal {
  1162	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1163	            _clans[_allClanIds[i]].coldDamage = 0;
  1164	        }
  1165	    }
  1166	
  1167	    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
  1168	        return _eventTick(tick);
  1169	    }
  1170	
  1171	    function _eventTick(uint64 tick) internal pure returns (uint32) {
  1172	        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
  1173	        // forge-lint: disable-next-line(unsafe-typecast)
  1174	        return uint32(tick);
  1175	    }
  1176	
  1177	    function _isWinterActiveAt(uint64 tick) internal pure returns (bool) {
  1178	        if (tick < ClanWorldConstants.WINTER_START_TICK) {
  1179	            return false;
  1180	        }
  1181	        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
  1182	        return elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
  1183	    }
  1184	
  1185	    function _winterWindowForTick(uint64 tick)
  1186	        internal
  1187	        pure
  1188	        returns (bool active, uint64 startsAtTick, uint64 endsAtTick)
  1189	    {
  1190	        if (tick < ClanWorldConstants.WINTER_START_TICK) {
  1191	            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
  1192	            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  1193	            return (false, startsAtTick, endsAtTick);
  1194	        }
  1195	
  1196	        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
  1197	        uint64 cycleIndex = elapsed / ClanWorldConstants.WINTER_PERIOD_TICKS;
  1198	        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
  1199	        active = elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
  1200	        startsAtTick = active ? cycleStart : cycleStart + ClanWorldConstants.WINTER_PERIOD_TICKS;
  1201	        endsAtTick = startsAtTick + ClanWorldConstants.WINTER_DURATION_TICKS;
  1202	    }
  1203	
  1204	    function _worldStateView() internal view returns (WorldState memory ws) {
  1205	        ws = _world;
  1206	        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
  1207	    }
  1208	
  1209	    /// @notice Public settlement trigger — lazily settle a clan.
  1210	    function settleClan(uint32 clanId) external override nonReentrant {
  1211	        _settleClan(clanId);
  1212	    }
  1213	
  1214	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
  1215	    ///         Internally settles the entire clan (including upkeep) to guarantee
  1216	    ///         correct ordering and prevent double-settlement. Callers may call this
  1217	    ///         or settleClan interchangeably; both are safe and idempotent.
  1218	    function settleClansman(uint32 csId) external override nonReentrant {
  1219	        Clansman storage cs = _clansmen[csId];
  1220	        if (cs.clansmanId == 0) return;
  1221	        _settleClan(cs.clanId);
  1222	    }
  1223	
  1224	    /// @notice Finalize season. Phase 1 stub.
  1225	    function finalizeSeason() external override {
  1226	        // TODO Phase 3
  1227	    }
  1228	
  1229	    // =========================================================================
  1230	    // CLAN LIFECYCLE
  1231	    // =========================================================================
  1232	
  1233	    /// @notice Mint a new clan and spawn its homebase.
  1234	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1235	        require(to != address(0), "ClanWorld: zero address");
  1236	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1237	        clanId = _nextClanId++;
  1238	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1239	
  1240	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1241	        uint8[6] memory spawnRegions = [
  1242	            ClanWorldConstants.REGION_FOREST,
  1243	            ClanWorldConstants.REGION_MOUNTAINS,
  1244	            ClanWorldConstants.REGION_WEST_FARMS,
  1245	            ClanWorldConstants.REGION_EAST_FARMS,
  1246	            ClanWorldConstants.REGION_WEST_DOCKS,
  1247	            ClanWorldConstants.REGION_EAST_DOCKS
  1248	        ];
  1249	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1250	
  1251	        // Create clan
  1252	        Clan storage clan = _clans[clanId];
  1253	        clan.clanId = clanId;
  1254	        clan.iftTokenId = iftTokenId;
  1255	        clan.owner = to;
  1256	        clan.clanState = ClanState.ACTIVE;
  1257	        clan.baseRegion = baseRegion;
  1258	        clan.baseLevel = 1;
  1259	        clan.wallLevel = 0;
  1260	        clan.monumentLevel = 0;
  1261	        clan.livingClansmen = 4;
  1262	        clan.lastSettledTick = _world.currentTick;
  1263	        clan.starvationStartsAtTick = 0;
  1264	        clan.coldDamage = 0;
  1265	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1266	        clan.blueprintBalance = 0;
  1267	        // Starting vault per v4 spec §12.4
  1268	        clan.vaultWood = 20e18;
  1269	        clan.vaultIron = 0;
  1270	        clan.vaultWheat = 20e18;
  1271	        clan.vaultFish = 2e18;
  1272	
  1273	        WheatPlotState startingPlotState =
  1274	            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
  1275	
  1276	        // Wheat plots
  1277	        _wheatPlots[clanId][0] = WheatPlot({
  1278	            state: startingPlotState,
  1279	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1280	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1281	            regrowUntilTick: 0
  1282	        });
  1283	        _wheatPlots[clanId][1] = WheatPlot({
  1284	            state: startingPlotState,
  1285	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1286	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
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
  1381	    {
  1382	        result.clansmanId = order.clansmanId;
  1383	
  1384	        // Validate clansman
  1385	        Clansman storage cs = _clansmen[order.clansmanId];

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1880,2065p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '20,240p'" in /home/claude/code/clan-world
 succeeded in 0ms:
    20	 */
    21	
    22	// =============================================================================
    23	// CONSTANTS
    24	// =============================================================================
    25	
    26	library ClanWorldConstants {
    27	    // World cadence
    28	    uint64 internal constant HEARTBEAT_INTERVAL_SECONDS = 60;
    29	    uint64 internal constant WINTER_START_TICK = 110;
    30	    uint64 internal constant WINTER_DURATION_TICKS = 10;
    31	    uint64 internal constant WINTER_PERIOD_TICKS = 110;
    32	    uint64 internal constant SEASON_DURATION_TICKS = 360;
    33	
    34	    // Bandit cadence
    35	    uint64 internal constant BANDIT_COOLDOWN_TICKS = 10;
    36	    uint64 internal constant BANDIT_CAMP_TICKS = 3;
    37	    uint64 internal constant BANDIT_REST_TICKS = 2;
    38	    uint8 internal constant BANDIT_MAX_ATTACK_ATTEMPTS = 6;
    39	
    40	    // Clansman cadence
    41	    uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
    42	
    43	    // Carry caps (per clansman)
    44	    uint256 internal constant WOOD_CAP = 15e18;
    45	    uint256 internal constant IRON_CAP = 5e18;
    46	    uint256 internal constant WHEAT_CAP = 40e18;
    47	    uint256 internal constant FISH_CAP = 8e18;
    48	
    49	    // Gathering yields
    50	    uint256 internal constant WOOD_BASE_YIELD = 2e18;
    51	    uint256 internal constant WOOD_CRIT_BONUS = 1e18;
    52	    uint16 internal constant WOOD_CRIT_BPS = 2000; // 20%
    53	
    54	    uint256 internal constant IRON_BASE_YIELD = 5e17; // 0.5e18
    55	    uint16 internal constant GOLD_FROM_IRON_BPS = 200; // 2%
    56	    uint256 internal constant GOLD_FROM_IRON_AMOUNT = 1e18;
    57	
    58	    uint16 internal constant FISH_DOCKS_BPS = 2500; // 25%
    59	    uint16 internal constant FISH_DEEP_BPS = 7500; // 75%
    60	
    61	    // Upkeep
    62	    uint256 internal constant WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
    63	    uint256 internal constant FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1
    64	    uint256 internal constant WINTER_WOOD_BURN_PER_CLANSMAN = 5e17; // 0.5
    65	    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
    66	    uint16 internal constant COLD_DAMAGE_PER_WALL_DEGRADATION = 2;
    67	    uint16 internal constant COLD_DAMAGE_PER_CLANSMAN_DEATH = 2;
    68	
    69	    // Wheat plots
    70	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    71	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
    72	
    73	    // Bandit combat
    74	    uint16 internal constant BANDIT_BASE_STEAL_BPS = 2000; // 20%
    75	    uint16 internal constant BANDIT_DROP_TO_DEFENDERS_BPS = 5000; // 50%
    76	
    77	    // Region IDs (1-indexed; 0 = NOOP / unset sentinel)
    78	    uint8 internal constant REGION_NOOP = 0;
    79	    uint8 internal constant REGION_FOREST = 1;
    80	    uint8 internal constant REGION_MOUNTAINS = 2;
    81	    uint8 internal constant REGION_UNICORN_TOWN = 3;
    82	    uint8 internal constant REGION_WEST_FARMS = 4;
    83	    uint8 internal constant REGION_EAST_FARMS = 5;
    84	    uint8 internal constant REGION_WEST_DOCKS = 6;
    85	    uint8 internal constant REGION_EAST_DOCKS = 7;
    86	    uint8 internal constant REGION_DEEP_SEA = 8;
    87	
    88	    // Sentinels
    89	    uint32 internal constant CLAN_ID_NULL = 0; // valid clan IDs start at 1
    90	    uint32 internal constant BANDIT_ID_NULL = 0;
    91	}
    92	
    93	// =============================================================================
    94	// ENUMS
    95	// =============================================================================
    96	
    97	enum ClanState {
    98	    ACTIVE,
    99	    DEAD
   100	}
   101	
   102	enum ClansmanState {
   103	    WAITING,
   104	    TRAVELING,
   105	    ACTING,
   106	    DEAD
   107	}
   108	
   109	enum BanditState {
   110	    NONE,
   111	    CAMPING,
   112	    RESTING,
   113	    ATTACKING,
   114	    DEFEATED,
   115	    ESCAPED
   116	}
   117	
   118	enum WheatPlotState {
   119	    Harvestable,
   120	    Regrowing,
   121	    WinterLocked
   122	}
   123	
   124	enum ResourceType {
   125	    Wood,
   126	    Iron,
   127	    Wheat,
   128	    Fish
   129	}
   130	
   131	enum ActionType {
   132	    None,
   133	    ChopWood,
   134	    MineIron,
   135	    FishDocks,
   136	    FishDeepSea,
   137	    HarvestWheat,
   138	    DepositResources,
   139	    BuildWall,
   140	    UpgradeBase,
   141	    UpgradeMonument,
   142	    DefendBase,
   143	    MarketBuy,
   144	    MarketSell,
   145	    Wait
   146	}
   147	
   148	enum MarketExecutionMode {
   149	    None,
   150	    Immediate,
   151	    Scheduled
   152	}
   153	
   154	enum StatusCode {
   155	    OK,
   156	    ERR_CLAN_DEAD,
   157	    ERR_CLAN_NOT_OWNED,
   158	    ERR_CLANSMAN_DEAD,
   159	    ERR_INVALID_CLANSMAN,
   160	    ERR_INVALID_REGION,
   161	    ERR_INVALID_ACTION,
   162	    ERR_INVALID_TARGET,
   163	    ERR_COOLDOWN_ACTIVE,
   164	    ERR_NOT_WAITING,
   165	    ERR_NOT_IN_UNICORN_TOWN,
   166	    ERR_NOT_AT_HOMEBASE,
   167	    ERR_NOT_AT_TARGET_BASE,
   168	    ERR_NOT_DEFENDABLE,
   169	    ERR_MISSING_RESOURCES,
   170	    ERR_EMPTY_CARGO,
   171	    ERR_PLOT_NOT_READY,
   172	    ERR_PLOT_EMPTY,
   173	    ERR_MARKET_ZERO_AMOUNT,
   174	    ERR_MARKET_UNSUPPORTED_TOKEN,
   175	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   176	    ERR_MARKET_BUY_OVER_CAPACITY,
   177	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   178	    ERR_WORLD_TICK_MISMATCH,
   179	    ERR_NO_ACTIVE_BANDIT,
   180	    ERR_SEASON_ENDED,
   181	    ERR_NOT_ENOUGH_GOLD,
   182	    ERR_CARRY_FULL,
   183	    ERR_WINTER_LOCKED,
   184	    ERR_MUST_SETTLE_FIRST
   185	}
   186	
   187	// =============================================================================
   188	// CORE STATE STRUCTS (raw storage shape)
   189	// =============================================================================
   190	
   191	struct WorldState {
   192	    uint64 currentTick;
   193	    uint64 seasonStartTick;
   194	    uint64 seasonEndTick;
   195	    bool seasonFinalized;
   196	    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
   197	    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
   198	
   199	    uint64 nextHeartbeatAtTs;
   200	    uint64 nextBanditSpawnEligibleTick;
   201	    uint16 currentBanditSpawnChanceBps;
   202	    bytes32 currentTickSeed;
   203	
   204	    uint32 activeBanditId; // 0 if none
   205	    bool winterActive;
   206	    uint64 winterStartsAtTick;
   207	    uint64 winterEndsAtTick; // 0 if not active
   208	
   209	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
   210	}
   211	
   212	struct TreasuryState {
   213	    address treasuryOwner;
   214	    uint256 prizePotGold;
   215	
   216	    bool poolsSeeded;
   217	
   218	    address woodToken;
   219	    address wheatToken;
   220	    address fishToken;
   221	    address ironToken;
   222	    address goldToken;
   223	    address blueprintToken;
   224	
   225	    address woodGoldPool;
   226	    address wheatGoldPool;
   227	    address fishGoldPool;
   228	    address ironGoldPool;
   229	}
   230	
   231	struct Clan {
   232	    uint32 clanId;
   233	    uint256 iftTokenId;
   234	    address owner;
   235	    ClanState clanState;
   236	
   237	    uint8 baseRegion;
   238	    uint8 baseLevel;
   239	    uint8 wallLevel;
   240	    uint8 monumentLevel;

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '480,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   480	}
   481	
   482	struct RegionOccupant {
   483	    uint32 clansmanId;
   484	    uint32 clanId;
   485	    ClansmanState state;
   486	    ActionType currentAction;
   487	    uint64 missionNonce;
   488	}
   489	
   490	// =============================================================================
   491	// EVENTS
   492	// =============================================================================
   493	
   494	interface IClanWorldEvents {
   495	    // ----- world clock -----
   496	    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
   497	    event WinterStarted(uint32 indexed tick);
   498	    event WinterEnded(uint32 indexed tick);
   499	    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
   500	
   501	    // ----- clan lifecycle -----
   502	    event ClanSpawned(
   503	        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
   504	    );
   505	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   506	    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
   507	    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
   508	    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
   509	    event ClanColdShortage(uint32 indexed clanId, uint32 tick, uint256 woodShort);
   510	    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint32 tick);
   511	    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint32 tick);
   512	
   513	    // ----- missions -----
   514	    event MissionAssigned(
   515	        uint32 indexed clanId,
   516	        uint32 indexed clansmanId,
   517	        uint64 missionNonce,
   518	        ActionType action,
   519	        uint8 startRegion,
   520	        uint8 targetRegion,
   521	        uint64 startTick,
   522	        uint64 arrivalTick
   523	    );
   524	    event MissionInterrupted(
   525	        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
   526	    );
   527	    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   528	    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   529	
   530	    // ----- gathering / vault movement -----
   531	    event ResourcesGathered(
   532	        uint32 indexed clanId,
   533	        uint32 indexed clansmanId,
   534	        ActionType action,
   535	        uint256 woodGained,
   536	        uint256 ironGained,
   537	        uint256 wheatGained,
   538	        uint256 fishGained,
   539	        uint256 goldBonus,
   540	        uint64 atTick
   541	    );
   542	    event ResourcesDeposited(
   543	        uint32 indexed clanId,
   544	        uint32 indexed clansmanId,
   545	        uint256 wood,
   546	        uint256 iron,
   547	        uint256 wheat,
   548	        uint256 fish,
   549	        uint64 atTick
   550	    );
   551	
   552	    // ----- building -----
   553	    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   554	    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   555	    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   556	
   557	    // ----- market -----
   558	    event ImmediateMarketActionExecuted(
   559	        uint32 indexed clanId,
   560	        uint32 indexed clansmanId,
   561	        address tokenIn,
   562	        address tokenOut,
   563	        uint256 amountIn,
   564	        uint256 amountOut,
   565	        uint64 atTick
   566	    );
   567	    event ScheduledMarketActionExecuted(
   568	        uint64 indexed executeAtTick,
   569	        uint64 indexed commitSequence,
   570	        uint32 indexed clanId,
   571	        uint32 clansmanId,
   572	        address tokenIn,
   573	        address tokenOut,
   574	        uint256 amountIn,
   575	        uint256 amountOut
   576	    );
   577	    event ScheduledMarketActionCommitted(
   578	        uint64 indexed executeAtTick,
   579	        uint64 indexed commitSequence,
   580	        uint32 indexed clanId,
   581	        uint32 clansmanId,
   582	        ActionType action,
   583	        address marketToken,
   584	        uint256 marketAmount,
   585	        uint256 maxGoldIn
   586	    );
   587	    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
   588	
   589	    // ----- bandits -----
   590	    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);
   591	    event BanditStateChanged(
   592	        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
   593	    );
   594	    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
   595	    event BanditAttackResolved(
   596	        uint32 indexed banditId,
   597	        uint32 indexed targetClanId,
   598	        bool defended,
   599	        uint16 attackPower,
   600	        uint16 totalDefense,
   601	        uint16 wallLevelAfter,
   602	        uint256 stolenWood,
   603	        uint256 stolenIron,
   604	        uint256 stolenWheat,
   605	        uint256 stolenFish,
   606	        uint64 atTick
   607	    );
   608	    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
   609	    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
   610	    event BlueprintAwarded(uint32 indexed clanId, uint32 indexed banditId, uint256 amount);
   611	    event LootDistributedToDefender(
   612	        uint32 indexed banditId,
   613	        uint32 indexed clanId,
   614	        uint32 indexed clansmanId,
   615	        uint256 wood,
   616	        uint256 iron,
   617	        uint256 wheat,
   618	        uint256 fish
   619	    );
   620	
   621	    // ----- winter cold damage -----
   622	    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
   623	    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
   624	
   625	    // ----- OTC transfers -----
   626	    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   627	    event VaultResourceTransferred(
   628	        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
   629	    );
   630	    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
   631	
   632	    // ----- treasury / pools -----
   633	    event PoolsSeeded(address woodGoldPool, address wheatGoldPool, address fishGoldPool, address ironGoldPool);
   634	}
   635	
   636	// =============================================================================
   637	// MAIN INTERFACE
   638	// =============================================================================
   639	
   640	interface IClanWorld is IClanWorldEvents {
   641	    // -------------------------------------------------------------------------
   642	    // World progression
   643	    // -------------------------------------------------------------------------
   644	
   645	    /// @notice Permissionless heartbeat. Closes the current tick, resolves
   646	    ///         scheduled market actions and world events, advances the tick.
   647	    ///         Rate-limited by WorldState.nextHeartbeatAtTs.
   648	    function heartbeat() external;
   649	
   650	    /// @notice Lazily settle a clan forward to current tick. Idempotent.
   651	    function settleClan(uint32 clanId) external;
   652	
   653	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   654	    function settleClansman(uint32 csId) external;
   655	
   656	    /// @notice Finalize the current season. Permissionless after seasonEndTick.
   657	    function finalizeSeason() external;
   658	
   659	    // -------------------------------------------------------------------------
   660	    // Clan lifecycle
   661	    // -------------------------------------------------------------------------
   662	
   663	    /// @notice Mint a new clan iNFT and spawn its homebase in a valid region.
   664	    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
   665	
   666	    /// @notice Submit one or more orders for a single clan's clansmen.
   667	    ///         Per-order failures do not revert the tx.
   668	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
   669	
   670	    // -------------------------------------------------------------------------
   671	    // Treasury / pool seeding
   672	    // -------------------------------------------------------------------------
   673	
   674	    /// @notice Owner-only. Registers token and pool addresses once before seeding.
   675	    ///         tokens order: wood, iron, wheat, fish, gold, blueprint.
   676	    ///         pools order: wood, wheat, fish, iron.
   677	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external;
   678	
   679	    /// @notice Owner-only. Seeds the four Unicorn Town pools at deploy time.
   680	    function seedPools(PoolSeedConfig calldata cfg) external;
   681	
   682	    // -------------------------------------------------------------------------
   683	    // OTC transfers (clan-to-clan, sender must be ACTIVE per v4.3 §M)
   684	    // -------------------------------------------------------------------------
   685	
   686	    function transferGold(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   687	
   688	    function transferVaultResource(uint32 fromClanId, uint32 toClanId, ResourceType resource, uint256 amount) external;
   689	
   690	    function transferBlueprint(uint32 fromClanId, uint32 toClanId, uint256 amount) external;
   691	
   692	    function transferBundle(
   693	        uint32 fromClanId,
   694	        uint32 toClanId,
   695	        uint256 gold,
   696	        uint256 blueprint,
   697	        uint256 wood,
   698	        uint256 iron,
   699	        uint256 wheat,
   700	        uint256 fish
   701	    ) external;
   702	
   703	    // -------------------------------------------------------------------------
   704	    // Raw read getters (committed storage, no settlement simulation)
   705	    // -------------------------------------------------------------------------
   706	
   707	    function getWorldState() external view returns (WorldState memory);
   708	
   709	    function getTreasuryState() external view returns (TreasuryState memory);
   710	
   711	    function getClan(uint32 clanId) external view returns (Clan memory);
   712	
   713	    function getClansman(uint32 clansmanId) external view returns (Clansman memory);
   714	
   715	    function getActiveMission(uint32 clansmanId) external view returns (Mission memory);
   716	
   717	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
   718	        external
   719	        view
   720	        returns (uint64 submitted, uint64 executes, uint64 settles);
   721	
   722	    /// @notice True iff currentTick is inside the recurring winter window.
   723	    function isWinter() external view returns (bool);
   724	
   725	    function getActionDuration(ActionType action) external pure returns (uint64);
   726	
   727	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64);
   728	
   729	    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory);
   730	
   731	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
   732	
   733	    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
   734	
   735	    function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds);
   736	
   737	    function getDefendingClans(uint8 region) external view returns (uint32[] memory clanIds);
   738	
   739	    // -------------------------------------------------------------------------
   740	    // Derived read getters (read-only simulation forward to current tick)
   741	    //
   742	    // Per v4.3 §J ("Derived Getter Non-Mutation Rule"), these MUST NOT mutate
   743	    // any storage, including settlement checkpoints, cached flags, or queues.
   744	    // -------------------------------------------------------------------------
   745	
   746	    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory);
   747	
   748	    function getDerivedClansmanState(uint32 clansmanId) external view returns (DerivedClansmanState memory);
   749	
   750	    /// @notice Non-binding preview. Bandit targeting is recomputed at attack
   751	    ///         resolution time using then-current eagerly settled state.
   752	    function getBanditTargetPreview(uint32 banditId) external view returns (uint32 previewClanId);
   753	
   754	    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external view returns (uint8 travelTicks, bytes8 path);
   755	
   756	    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);
   757	
   758	    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);
   759	
   760	    // -------------------------------------------------------------------------

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '40,95p'" in /home/claude/code/clan-world
 succeeded in 0ms:
    40	    PoolReserves,
    41	    MarketState,
    42	    ActiveBanditView,
    43	    RegionOccupant
    44	} from "../src/IClanWorld.sol";
    45	
    46	/// @dev Test harness that exposes internal state manipulation for unit tests.
    47	contract ClanWorldTestHarness is ClanWorld {
    48	    function killClansman(uint32 csId) external {
    49	        _clansmen[csId].state = ClansmanState.DEAD;
    50	    }
    51	
    52	    function setClanUpkeepState(
    53	        uint32 clanId,
    54	        uint64 lastSettledTick,
    55	        uint256 vaultWood,
    56	        uint256 vaultWheat,
    57	        uint256 vaultFish,
    58	        uint16 coldDamage
    59	    ) external {
    60	        Clan storage clan = _clans[clanId];
    61	        clan.lastSettledTick = lastSettledTick;
    62	        clan.vaultWood = vaultWood;
    63	        clan.vaultWheat = vaultWheat;
    64	        clan.vaultFish = vaultFish;
    65	        clan.coldDamage = coldDamage;
    66	    }
    67	
    68	    function setClanWallLevel(uint32 clanId, uint8 wallLevel) external {
    69	        _clans[clanId].wallLevel = wallLevel;
    70	    }
    71	
    72	    function setClanIronAndGold(uint32 clanId, uint256 vaultIron, uint256 goldBalance) external {
    73	        _clans[clanId].vaultIron = vaultIron;
    74	        _clans[clanId].goldBalance = goldBalance;
    75	    }
    76	}
    77	
    78	contract ClanWorldTest is Test {
    79	    ClanWorld world;
    80	    address elder = address(0xA1);
    81	    address elder2 = address(0xA2);
    82	
    83	    // Phase 2 market infrastructure
    84	    MinimalERC20 woodToken;
    85	    MinimalERC20 ironToken;
    86	    MinimalERC20 wheatToken;
    87	    MinimalERC20 fishToken;
    88	    MinimalERC20 goldToken;
    89	    MinimalERC20 blueprintToken;
    90	    StubPool woodPool;
    91	    StubPool ironPool;
    92	    StubPool wheatPool;
    93	    StubPool fishPool;
    94	
    95	    function setUp() public {

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '540,610p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   540	    // Test A: quoteTravel(9,9) — both out-of-bounds same-region, must return (0, bytes8(0))
   541	    // -------------------------------------------------------------------------
   542	
   543	    function test_quoteTravel_outOfBounds_sameRegion() public {
   544	        // Previously (9,9) escaped the > 8 guard (same-region early return fired first),
   545	        // returning (0, bytes8(uint64(9) << 56)) instead of (0, bytes8(0)).
   546	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 9);
   547	        assertEq(ticks, 0, "quoteTravel(9,9): travelTicks must be 0");
   548	        assertEq(path, bytes8(0), "quoteTravel(9,9): path must be bytes8(0), not a packed 9-region sentinel");
   549	    }
   550	
   551	    // -------------------------------------------------------------------------
   552	    // Test B: submitClanOrders returns ERR_MUST_SETTLE_FIRST when clan is >200 ticks behind
   553	    // -------------------------------------------------------------------------
   554	
   555	    function test_submitClanOrders_returnsMustSettleFirst_when_clan_too_far_behind() public {
   556	        uint32 clanId = _mintClan();
   557	        ClanFullView memory view_ = world.getClanFullView(clanId);
   558	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
   559	
   560	        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
   561	        for (uint256 i = 0; i < 201; i++) {
   562	            _advanceTick();
   563	        }
   564	
   565	        // submitClanOrders should return ERR_MUST_SETTLE_FIRST
   566	        // without reverting, for every order in the batch
   567	        ClanOrder[] memory orders = new ClanOrder[](1);
   568	        orders[0] = ClanOrder({
   569	            clansmanId: csId,
   570	            gotoRegion: ClanWorldConstants.REGION_FOREST,
   571	            action: ActionType.ChopWood,
   572	            targetClanId: 0,
   573	            marketToken: address(0),
   574	            marketAmount: 0,
   575	            maxGoldIn: 0
   576	        });
   577	        vm.prank(elder);
   578	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
   579	
   580	        assertEq(results.length, 1, "should return one result");
   581	        assertEq(
   582	            uint8(results[0].status),
   583	            uint8(StatusCode.ERR_MUST_SETTLE_FIRST),
   584	            "clan >200 ticks behind must return ERR_MUST_SETTLE_FIRST"
   585	        );
   586	        assertEq(uint8(StatusCode.ERR_WINTER_LOCKED), 28, "existing status indices must remain stable");
   587	        assertEq(uint8(StatusCode.ERR_MUST_SETTLE_FIRST), 29, "new status code must be appended");
   588	    }
   589	
   590	    // -------------------------------------------------------------------------
   591	    // Test C: cooldown resets on mission interrupt
   592	    // -------------------------------------------------------------------------
   593	
   594	    function test_cooldown_resets_on_mission_interrupt() public {
   595	        uint32 clanId = _mintClan();
   596	        ClanFullView memory view_ = world.getClanFullView(clanId);
   597	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
   598	
   599	        // Submit first mission — sends clansman to Forest to chop wood
   600	        OrderResult[] memory r1 = _submitOrder(clanId, csId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood);
   601	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
   602	        uint64 firstCooldown = r1[0].cooldownEndsAtTs;
   603	        assertGt(firstCooldown, 0, "cooldown should be set after first order");
   604	
   605	        // Wait for cooldown to expire
   606	        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
   607	
   608	        // Advance tick so heartbeat is valid, then submit interrupt mission
   609	        _advanceTick();
   610	

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '1910,2360p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1910	
  1911	    function test_phase3E8_partialBatch_mixedResults() public {
  1912	        uint32 clanId = _mintClan();
  1913	        ClanFullView memory v = world.getClanFullView(clanId);
  1914	
  1915	        uint32 cs0 = v.clansmen[0].clansman.clansman.clansmanId;
  1916	        uint32 cs1 = v.clansmen[1].clansman.clansman.clansmanId;
  1917	        uint32 cs2 = v.clansmen[2].clansman.clansman.clansmanId;
  1918	
  1919	        ClanOrder[] memory orders = new ClanOrder[](4);
  1920	
  1921	        // Order 0: valid — cs0 ChopWood to Forest
  1922	        orders[0] = ClanOrder({
  1923	            clansmanId: cs0,
  1924	            gotoRegion: ClanWorldConstants.REGION_FOREST,
  1925	            action: ActionType.ChopWood,
  1926	            targetClanId: 0,
  1927	            marketToken: address(0),
  1928	            marketAmount: 0,
  1929	            maxGoldIn: 0
  1930	        });
  1931	
  1932	        // Order 1: invalid clansmanId 9999
  1933	        orders[1] = ClanOrder({
  1934	            clansmanId: 9999,
  1935	            gotoRegion: ClanWorldConstants.REGION_FOREST,
  1936	            action: ActionType.ChopWood,
  1937	            targetClanId: 0,
  1938	            marketToken: address(0),
  1939	            marketAmount: 0,
  1940	            maxGoldIn: 0
  1941	        });
  1942	
  1943	        // Order 2: valid — cs1 Wait at homebase (NOOP)
  1944	        orders[2] = ClanOrder({
  1945	            clansmanId: cs1,
  1946	            gotoRegion: ClanWorldConstants.REGION_NOOP,
  1947	            action: ActionType.Wait,
  1948	            targetClanId: 0,
  1949	            marketToken: address(0),
  1950	            marketAmount: 0,
  1951	            maxGoldIn: 0
  1952	        });
  1953	
  1954	        // Order 3: invalid — cs2 ChopWood to Mountains (wrong region for ChopWood)
  1955	        orders[3] = ClanOrder({
  1956	            clansmanId: cs2,
  1957	            gotoRegion: ClanWorldConstants.REGION_MOUNTAINS,
  1958	            action: ActionType.ChopWood,
  1959	            targetClanId: 0,
  1960	            marketToken: address(0),
  1961	            marketAmount: 0,
  1962	            maxGoldIn: 0
  1963	        });
  1964	
  1965	        vm.prank(elder);
  1966	        OrderResult[] memory results = world.submitClanOrders(clanId, orders);
  1967	
  1968	        assertEq(results.length, 4, "3.E8: must return 4 results");
  1969	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "3.E8: order[0] must be OK");
  1970	        assertEq(
  1971	            uint8(results[1].status),
  1972	            uint8(StatusCode.ERR_INVALID_CLANSMAN),
  1973	            "3.E8: order[1] must be ERR_INVALID_CLANSMAN"
  1974	        );
  1975	        assertEq(uint8(results[2].status), uint8(StatusCode.OK), "3.E8: order[2] must be OK");
  1976	        assertEq(
  1977	            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"
  1978	        );
  1979	    }
  1980	
  1981	    // =====================================================================
  1982	    // Phase 4.4 — season + winter timer tests
  1983	    // =====================================================================
  1984	
  1985	    function test_season_initialState() public {
  1986	        WorldState memory ws = world.getWorldState();
  1987	        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
  1988	        assertEq(ws.seasonStartTick, 0);
  1989	        assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS);
  1990	        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
  1991	        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
  1992	        assertFalse(ws.winterActive);
  1993	        assertFalse(world.isWinter());
  1994	    }
  1995	
  1996	    function test_worldSnapshot_initialWinterStartsAtTick() public view {
  1997	        WorldSnapshot memory snapshot = world.getWorldSnapshot();
  1998	        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
  1999	        assertEq(snapshot.winterStartsAtTick, 110);
  2000	    }
  2001	
  2002	    function test_winter_onset() public {
  2003	        _mintClan();
  2004	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2005	        _advanceToTick(winterStart - 1);
  2006	
  2007	        vm.recordLogs();
  2008	        _advanceTick();
  2009	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2010	
  2011	        assertEq(_countLogs(logs, keccak256("WinterStarted(uint32)")), 1, "WinterStarted emits once");
  2012	        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
  2013	
  2014	        _advanceTick();
  2015	        WorldState memory ws = world.getWorldState();
  2016	        assertTrue(world.isWinter(), "winter should be active past start tick");
  2017	        assertTrue(ws.winterActive, "world state should report winter active");
  2018	        assertEq(ws.winterStartsAtTick, winterStart);
  2019	        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
  2020	    }
  2021	
  2022	    function test_winter_end_and_next_cycle() public {
  2023	        _mintClan();
  2024	        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  2025	        _advanceToTick(winterEnd - 1);
  2026	
  2027	        vm.recordLogs();
  2028	        _advanceTick();
  2029	        _advanceTick();
  2030	        _advanceTick();
  2031	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2032	
  2033	        WorldState memory ws = world.getWorldState();
  2034	        assertFalse(ws.winterActive, "winter should be over");
  2035	        assertFalse(world.isWinter(), "isWinter should be false after winter end");
  2036	        assertEq(_countLogs(logs, keccak256("WinterEnded(uint32)")), 1, "WinterEnded emits once");
  2037	        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
  2038	    }
  2039	
  2040	    function test_winter_restarts_after_full_period() public {
  2041	        _mintClan();
  2042	        uint64 nextWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS;
  2043	        _advanceToTick(nextWinterStart - 1);
  2044	
  2045	        vm.recordLogs();
  2046	        _advanceTick();
  2047	        _advanceTick();
  2048	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2049	
  2050	        assertEq(_countLogs(logs, keccak256("WinterStarted(uint32)")), 1, "next WinterStarted emits once");
  2051	        assertTrue(world.isWinter(), "winter should be active in next period");
  2052	        assertEq(world.getWorldState().currentTick, nextWinterStart + 1);
  2053	    }
  2054	
  2055	    function test_winter_cropTransitions_lockThenRestartRegrow() public {
  2056	        uint32 clanId1 = _mintClan();
  2057	        uint32 clanId2 = _mintClan();
  2058	
  2059	        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
  2060	        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
  2061	        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
  2062	
  2063	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2064	        _advanceToTick(winterStart - 1);
  2065	        _advanceTick();
  2066	
  2067	        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
  2068	        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
  2069	        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
  2070	        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
  2071	        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
  2072	        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
  2073	        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
  2074	        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
  2075	
  2076	        OrderResult[] memory results =
  2077	            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
  2078	        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
  2079	
  2080	        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  2081	        _advanceToTick(winterEnd - 1);
  2082	        _advanceTick();
  2083	
  2084	        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
  2085	        (west1, east1) = world.getWheatPlots(clanId1);
  2086	        (west2, east2) = world.getWheatPlots(clanId2);
  2087	        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
  2088	        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
  2089	        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
  2090	        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
  2091	        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
  2092	        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
  2093	        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
  2094	        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
  2095	
  2096	        _advanceToTick(expectedRegrowUntil + 1);
  2097	        world.settleClan(clanId1);
  2098	        world.settleClan(clanId2);
  2099	
  2100	        (west1, east1) = world.getWheatPlots(clanId1);
  2101	        (west2, east2) = world.getWheatPlots(clanId2);
  2102	        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
  2103	        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
  2104	        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
  2105	        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
  2106	        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
  2107	        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
  2108	    }
  2109	
  2110	    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
  2111	        uint32 clanId = _mintClan();
  2112	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2113	        _advanceToTick(winterStart - 2);
  2114	
  2115	        OrderResult[] memory results =
  2116	            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
  2117	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
  2118	        Mission memory queuedMission = world.getActiveMission(1);
  2119	
  2120	        _advanceToTick(queuedMission.settlesAtTick + 1);
  2121	
  2122	        Clansman memory cs = world.getClansman(1);
  2123	        Mission memory mission = world.getActiveMission(1);
  2124	        (WheatPlot memory west,) = world.getWheatPlots(clanId);
  2125	        assertFalse(mission.active, "locked harvest mission should complete");
  2126	        assertEq(cs.carryWheat, 0, "locked harvest should yield no wheat");
  2127	        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
  2128	        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
  2129	    }
  2130	
  2131	    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
  2132	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2133	        world = harness;
  2134	        uint32 clanId = _mintClan();
  2135	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2136	        _advanceToTick(winterStart + 1);
  2137	
  2138	        harness.setClanWallLevel(clanId, 2);
  2139	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
  2140	
  2141	        world.settleClan(clanId);
  2142	
  2143	        Clan memory clan = world.getClan(clanId);
  2144	        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
  2145	        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
  2146	        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
  2147	        assertEq(clan.coldDamage, 0, "sufficient wood should avoid cold damage");
  2148	        assertEq(clan.wallLevel, 2, "sufficient wood should not degrade wall");
  2149	        assertEq(clan.livingClansmen, 4, "sufficient wood should not kill clansmen");
  2150	    }
  2151	
  2152	    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
  2153	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2154	        world = harness;
  2155	        uint32 clanId = _mintClan();
  2156	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2157	        _advanceToTick(winterStart + 1);
  2158	
  2159	        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
  2160	
  2161	        vm.expectEmit(true, false, false, true);
  2162	        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
  2163	        world.settleClan(clanId);
  2164	
  2165	        Clan memory clan = world.getClan(clanId);
  2166	        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
  2167	        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
  2168	    }
  2169	
  2170	    function test_coldDamage_degradesWallEveryTwoShortages() public {
  2171	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2172	        world = harness;
  2173	        uint32 clanId = _mintClan();
  2174	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2175	        _advanceToTick(winterStart + 1);
  2176	
  2177	        harness.setClanWallLevel(clanId, 2);
  2178	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2179	
  2180	        vm.expectEmit(true, false, false, true);
  2181	        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
  2182	        world.settleClan(clanId);
  2183	
  2184	        Clan memory clan = world.getClan(clanId);
  2185	        assertEq(clan.coldDamage, 2, "cold damage should increment");
  2186	        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
  2187	        assertEq(clan.livingClansmen, 4, "wall should absorb cold before clansmen die");
  2188	    }
  2189	
  2190	    function test_coldDamage_zeroWallKillsOneClansmanEveryTwoShortages() public {
  2191	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2192	        world = harness;
  2193	        uint32 clanId = _mintClan();
  2194	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2195	        _advanceToTick(winterStart + 1);
  2196	
  2197	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2198	
  2199	        vm.recordLogs();
  2200	        world.settleClan(clanId);
  2201	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2202	
  2203	        Clan memory clan = world.getClan(clanId);
  2204	        assertEq(clan.coldDamage, 2, "cold damage should hit death threshold");
  2205	        assertEq(clan.wallLevel, 0, "wall should remain clamped at zero");
  2206	        assertEq(clan.livingClansmen, 3, "one clansman should die from cold");
  2207	        assertEq(_countLogs(logs, keccak256("ClansmanColdDeath(uint32,uint32,uint32)")), 1, "cold death should emit");
  2208	
  2209	        ClanFullView memory view_ = world.getClanFullView(clanId);
  2210	        uint256 deadCount = 0;
  2211	        for (uint256 i = 0; i < view_.clansmen.length; i++) {
  2212	            if (view_.clansmen[i].clansman.clansman.state == ClansmanState.DEAD) {
  2213	                deadCount++;
  2214	            }
  2215	        }
  2216	        assertEq(deadCount, 1, "exactly one stored clansman should be dead");
  2217	    }
  2218	
  2219	    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
  2220	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2221	        world = harness;
  2222	        uint32 clanId = _mintClan();
  2223	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2224	        uint256 goldBalance = 11e18;
  2225	
  2226	        _advanceToTick(winterStart + 4);
  2227	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2228	        harness.setClanIronAndGold(clanId, 5e18, goldBalance);
  2229	
  2230	        vm.recordLogs();
  2231	        world.settleClan(clanId);
  2232	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2233	
  2234	        Clan memory clan = world.getClan(clanId);
  2235	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
  2236	        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
  2237	        assertEq(clan.vaultWood, 0, "wood should burn on death");
  2238	        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
  2239	        assertEq(clan.vaultFish, 0, "fish should burn on death");
  2240	        assertEq(clan.vaultIron, 0, "iron should burn on death");
  2241	        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
  2242	        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
  2243	    }
  2244	
  2245	    function test_clanDeath_coldDamageMarksDeadAndBurnsVault() public {
  2246	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2247	        world = harness;
  2248	        uint32 clanId = _mintClan();
  2249	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2250	        uint256 goldBalance = 13e18;
  2251	        _advanceToTick(winterStart + 7);
  2252	
  2253	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2254	        harness.setClanIronAndGold(clanId, 9e18, goldBalance);
  2255	
  2256	        vm.recordLogs();
  2257	        world.settleClan(clanId);
  2258	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2259	
  2260	        Clan memory clan = world.getClan(clanId);
  2261	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "cold should mark clan dead");
  2262	        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
  2263	        assertEq(clan.vaultWood, 0, "wood should burn on death");
  2264	        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
  2265	        assertEq(clan.vaultFish, 0, "fish should burn on death");
  2266	        assertEq(clan.vaultIron, 0, "iron should burn on death");
  2267	        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
  2268	        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
  2269	    }
  2270	
  2271	    function test_deadClanCannotSubmitClanOrders() public {
  2272	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2273	        world = harness;
  2274	        uint32 clanId = _mintClan();
  2275	
  2276	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2277	        _advanceToTick(winterStart + 4);
  2278	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2279	        world.settleClan(clanId);
  2280	
  2281	        vm.expectRevert("ClanWorld: clan dead");
  2282	        _submitOrder(clanId, 1, ClanWorldConstants.REGION_FOREST, ActionType.Wait);
  2283	    }
  2284	
  2285	    function test_clanDeath_doesNotAffectOtherClan() public {
  2286	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2287	        world = harness;
  2288	        uint32 clanIdA = _mintClan();
  2289	        uint32 clanIdB = _mintClan();
  2290	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2291	        Clan memory clanABefore = world.getClan(clanIdA);
  2292	
  2293	        _advanceToTick(winterStart + 4);
  2294	        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
  2295	        world.settleClan(clanIdB);
  2296	
  2297	        Clan memory clanAAfter = world.getClan(clanIdA);
  2298	        Clan memory clanB = world.getClan(clanIdB);
  2299	        assertEq(uint8(clanB.clanState), uint8(ClanState.DEAD), "clan B should be dead");
  2300	        assertEq(uint8(clanAAfter.clanState), uint8(ClanState.ACTIVE), "clan A should stay active");
  2301	        assertEq(clanAAfter.livingClansmen, clanABefore.livingClansmen, "clan A living count should not change");
  2302	        assertEq(clanAAfter.vaultWood, clanABefore.vaultWood, "clan A wood should not burn");
  2303	        assertEq(clanAAfter.vaultWheat, clanABefore.vaultWheat, "clan A wheat should not burn");
  2304	        assertEq(clanAAfter.vaultFish, clanABefore.vaultFish, "clan A fish should not burn");
  2305	        assertEq(clanAAfter.goldBalance, clanABefore.goldBalance, "clan A gold should not change");
  2306	    }
  2307	
  2308	    function test_winter_upkeep_returnsToNormalAfterWinter() public {
  2309	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2310	        world = harness;
  2311	        uint32 clanId = _mintClan();
  2312	        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
  2313	        _advanceToTick(winterEnd + 1);
  2314	
  2315	        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
  2316	
  2317	        world.settleClan(clanId);
  2318	
  2319	        Clan memory clan = world.getClan(clanId);
  2320	        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
  2321	        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
  2322	        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
  2323	        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
  2324	    }
  2325	
  2326	    function test_season_transition() public {
  2327	        // Advance SEASON_DURATION_TICKS heartbeats to cross season boundary
  2328	        for (uint256 i = 0; i < ClanWorldConstants.SEASON_DURATION_TICKS; i++) {
  2329	            vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2330	            world.heartbeat();
  2331	        }
  2332	        WorldState memory ws = world.getWorldState();
  2333	        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
  2334	        assertEq(ws.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS);
  2335	        assertEq(ws.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2);
  2336	        // winter is derived from the global recurring schedule
  2337	        uint64 expectedWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS * 3;
  2338	        assertEq(ws.winterStartsAtTick, expectedWinterStart);
  2339	    }
  2340	
  2341	    function test_nextHeartbeatAtTick_tracks_tick() public {
  2342	        WorldState memory ws0 = world.getWorldState();
  2343	        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
  2344	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
  2345	        world.heartbeat();
  2346	        WorldState memory ws1 = world.getWorldState();
  2347	        assertEq(ws1.currentTick, 1);
  2348	        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
  2349	    }
  2350	}

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorldStub.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
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
/bin/bash -lc "git show e4a0d4c:'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md' | nl -ba | sed -n '18,110p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md' exists on disk, but not in 'e4a0d4c'

exec
/bin/bash -lc "git show e4a0d4c:'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md' | nl -ba | sed -n '160,182p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md' exists on disk, but not in 'e4a0d4c'

exec
/bin/bash -lc "git show e4a0d4c:'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md' | nl -ba | sed -n '838,875p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md' exists on disk, but not in 'e4a0d4c'

exec
/bin/bash -lc "git show e4a0d4c:'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md' | nl -ba | sed -n '804,840p'" in /home/claude/code/clan-world
exec
/bin/bash -lc "git show e4a0d4c:'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md' | nl -ba | sed -n '298,314p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md' exists on disk, but not in 'e4a0d4c'

 succeeded in 0ms:
fatal: path 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md' exists on disk, but not in 'e4a0d4c'

exec
/bin/bash -lc "git show e4a0d4c:'docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md' | nl -ba | sed -n '413,442p'" in /home/claude/code/clan-world
 succeeded in 0ms:
fatal: path 'docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md' exists on disk, but not in 'e4a0d4c'

codex
The contract head is confirmed. The deeper spec files aren’t versioned in that commit, so I’m reading the on-disk planning docs next for the exact wording behind winter cadence, starvation lethality, and elimination semantics.
exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md' | sed -n '18,110p'" in /home/claude/code/clan-world
 succeeded in 0ms:
    18	---
    19	
    20	## A2. First winter timing
    21	
    22	The first winter begins at **tick 110**.
    23	
    24	Thereafter, winter begins every 110 ticks:
    25	- tick 110
    26	- tick 220
    27	- tick 330
    28	- and so on
    29	
    30	Each winter lasts **10 ticks**, as already specified in v4.
    31	
    32	---
    33	
    34	## A3. Bandit movement duration between regions
    35	
    36	Bandit movement between adjacent outer-ring regions takes **0 additional ticks**.
    37	
    38	Bandit lifecycle between attacks is:
    39	1. complete attack attempt or noop in current region
    40	2. enter `RESTING` for 2 ticks
    41	3. after rest completes, move immediately to the next clockwise region
    42	4. resolve next target selection / attack logic there
    43	
    44	UI may visually animate movement during or after rest, but from the game-engine perspective movement itself consumes no extra tick beyond the 2-tick rest window.
    45	
    46	---
    47	
    48	## A4. Eager-settle scope for bandit target selection
    49	
    50	Before bandit target selection is computed in a region, **all clans with homebases in the bandit's current region must be eagerly settled to that tick**.
    51	
    52	This requirement exists because bandit targeting depends on each base's current vault loot-value, and accurate loot-value requires up-to-date clan settlement.
    53	
    54	Therefore:
    55	- target selection is not based only on the eventually selected base
    56	- target selection is computed only after all candidate bases in the current region have been eagerly settled
    57	- this is in addition to settling any external defender clans that physically contribute to the eventual defense event
    58	
    59	---
    60	
    61	## A5. Mission interruption and cooldown semantics
    62	
    63	**Every successful mission submission starts cooldown**, including an interruption/replacement of a currently active mission.
    64	
    65	This includes:
    66	- normal mission assignment
    67	- mission replacement while traveling
    68	- mission replacement while acting
    69	- immediate market action missions submitted while already in Unicorn Town
    70	
    71	If a new mission submission succeeds:
    72	- the clansman is settled to current tick
    73	- prior mission progress through the current tick is preserved
    74	- the old mission is replaced
    75	- cooldown starts immediately from that successful submission
    76	
    77	Unsuccessful / rejected mission submissions do **not** start cooldown.
    78	
    79	---
    80	
    81	## A6. Just-in-time winter logistics are intentionally punitive
    82	
    83	The per-tick local settlement order remains:
    84	1. apply clan upkeep for the tick
    85	2. update starvation status for the next tick if shortages occur
    86	3. advance travel
    87	4. resolve continuous actions
    88	5. apply single-tick action effects
    89	6. check terminal conditions
    90	
    91	This means that during winter:
    92	- if a base begins a tick with `0` wood in vault,
    93	- and a clansman arrives that same tick carrying wood,
    94	- and that wood is deposited later in step 5,
    95	
    96	then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
    97	
    98	This is intentional.
    99	
   100	Implication:
   101	- carried resources do not save a base from upkeep until they are actually deposited
   102	- just-in-time logistics are risky by design
   103	- players are expected to maintain a real vault buffer rather than rely on same-tick rescue deliveries
   104	
   105	---
   106	
   107	## A7. Market action input mode
   108	
   109	All v1 market actions are **Exact Input** actions only.
   110	

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md' | sed -n '160,182p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   160	
   161	Implication:
   162	- mercenary reliability depends on the mercenary clan's own home economy
   163	- Elders may need to monitor not only where mercenaries are, but whether their home clan is still fed
   164	
   165	---
   166	
   167	## A10. No change to summer starvation lethality
   168	
   169	Starvation outside winter does **not** directly kill clansmen.
   170	
   171	While starving outside winter:
   172	- gathering output is reduced by 50%
   173	- defense contribution becomes 0
   174	
   175	Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
   176	
   177	This is intentional for v1 to avoid excessively punishing early-game elimination.
   178	
   179	---
   180	
   181	## A11. No change to equal defender loot split
   182	

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md' | sed -n '838,875p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   838	During winter:
   839	- wheat plots are unavailable
   840	- harvesting wheat is not allowed
   841	- regrowth does not complete during winter
   842	
   843	At winter start:
   844	- all plots are cleared
   845	- both plots restart regrowing after winter ends
   846	- both plots become harvestable again `4` ticks after winter ends
   847	
   848	## 7.5 Winter gathering outside farming
   849	During winter:
   850	- logging is allowed
   851	- mining is allowed
   852	- fishing is allowed
   853	
   854	## 7.6 Winter failure: cold damage
   855	If a base cannot pay the required winter wood burn for a tick:
   856	- it gains `1 coldDamage`
   857	
   858	## 7.7 Cold damage consequences
   859	- every `2 coldDamage` reduces `wallLevel` by `1`
   860	- once `wallLevel == 0`, every `2 additional coldDamage` kills `1 clansman`
   861	
   862	Clansman deaths from cold apply only while wall level is zero.
   863	If a clan’s living clansmen count reaches `0`, the clan enters the `DEAD` / eliminated state defined in §12.7.
   864	
   865	## 7.8 Cold damage reset
   866	`coldDamage` resets to `0` at the end of each Winter.
   867	
   868	Cold damage does **not** persist across winters.
   869	
   870	Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
   871	
   872	---
   873	
   874	# 8. Building Actions, Levels, and Resource Requirements
   875	

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md' | sed -n '804,840p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   804	- worker transitions to `Waiting`
   805	- unless the action is a persistent continuous action like `DefendBase`
   806	
   807	### 12.4 Per-tick local settlement order
   808	For each settled local tick:
   809	
   810	1. apply clan upkeep for the tick
   811	2. update starvation status if shortage occurred
   812	3. advance traveling workers by one tick along route
   813	4. resolve one tick of continuous action
   814	5. apply single-tick action effects
   815	6. move completed or blocked workers to `Waiting`
   816	
   817	### 12.5 Just-in-time deposit rule
   818	Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
   819	
   820	This is intentional and punishes just-in-time logistics.
   821	
   822	### 12.6 Overflow clamp rule
   823	Normal gathering yield is clamped to the worker’s remaining capacity.
   824	
   825	Example:
   826	- worker has 14 wood already carried
   827	- 1 wood capacity remains
   828	- tick roll would yield 3 wood
   829	- worker only gains 1 wood
   830	- mission then transitions to `Waiting`
   831	
   832	### 12.7 Summer starvation
   833	Outside winter, starvation is nonlethal.
   834	
   835	A starving clan:
   836	- gathers at 50% output
   837	- contributes 0 defense
   838	- does not lose workers directly from starvation outside winter
   839	
   840	### 12.8 Dead clans

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md' | sed -n '298,314p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   298	## M. Dead Clan OTC Restriction (explicit restatement)
   299	
   300	### M.1 Dead sender restriction
   301	All OTC transfer entrypoints must require sender clan state == `ACTIVE`:
   302	
   303	- `transferGold`
   304	- `transferVaultResource`
   305	- `transferBlueprint`
   306	- `transferBundle`
   307	
   308	Dead clans may not initiate outbound value transfer.
   309	
   310	---
   311	
   312	## N. Implementation Notes (Non-blocking)
   313	
   314	### N.1 Heartbeat gas exposure

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md' | sed -n '413,442p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   413	## Phase 10 — winter and elimination
   414	
   415	### 10.1 Implement winter schedule
   416	- first winter at tick 110
   417	- duration 10 ticks
   418	- repeated cadence
   419	
   420	### 10.2 Implement winter upkeep
   421	- doubled food upkeep
   422	- wood burn
   423	
   424	### 10.3 Implement cold damage
   425	- accumulation
   426	- wall degradation
   427	- clansman deaths once walls are zero
   428	- reset at winter end
   429	
   430	### 10.4 Implement crop winter transitions
   431	- clear plots
   432	- lock during winter
   433	- restart regrow after winter
   434	
   435	### 10.5 Implement clan death
   436	- set dead state
   437	- burn physical vault loot
   438	- preserve dead-clan restrictions
   439	
   440	**Definition of done**
   441	- winter can kill clans and end runs
   442	

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | rg -n \"_markClanDead|ClanState\\.DEAD|starvationStartsAtTick|defense|_isStarving|UpgradeWall|_refundWallUpgradeReservation|_refundBaseUpgradeReservation|_refundMonumentUpgradeReservation|pending.*Upgrade|coldDamage|transferGold|getDerivedClanState|getDerivedClansmanState|quoteLootValueSettled|getClanFullView|getWorldSnapshot|getTravelTicks\"" in /home/claude/code/clan-world
 succeeded in 0ms:
374:            if (clan.clanState == ClanState.DEAD) break;
394:            clan.coldDamage = 0;
405:            clan.coldDamage = 0;
432:        if (starving && clan.starvationStartsAtTick == 0) {
433:            clan.starvationStartsAtTick = tick;
435:        } else if (!starving && clan.starvationStartsAtTick != 0) {
436:            clan.starvationStartsAtTick = 0;
441:        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
444:        if (clan.clanState == ClanState.DEAD) return;
453:                uint16 oldColdDamage = clan.coldDamage;
454:                if (clan.coldDamage < type(uint16).max) {
455:                    clan.coldDamage += 1;
464:        uint16 newColdDamage = clan.coldDamage;
486:    function _killRandomClansmanFromCold(Clan storage clan, uint64 tick, uint16 coldDamage) internal {
501:            uint256(keccak256(abi.encodePacked(clan.clanId, tick, coldDamage))),
529:                _markClanDead(clan.clanId, "starvation", tick);
540:            _markClanDead(clan.clanId, "cold", tick);
561:    function _markClanDead(uint32 clanId) internal {
562:        _markClanDead(clanId, "unknown", _world.currentTick);
565:    function _markClanDead(uint32 clanId, string memory reason, uint64 tick) internal {
567:        if (clan.clanId == 0 || clan.clanState == ClanState.DEAD) return;
569:        clan.clanState = ClanState.DEAD;
574:        clan.starvationStartsAtTick = 0;
594:    function _isStarving(Clan storage clan) internal view returns (bool) {
595:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
607:        bool starving = _isStarving(clan);
1089:            if (clan.clanState == ClanState.DEAD) continue;
1163:            _clans[_allClanIds[i]].coldDamage = 0;
1263:        clan.starvationStartsAtTick = 0;
1264:        clan.coldDamage = 0;
1347:        if (clan.clanState == ClanState.DEAD) {
1986:    function transferGold(uint32, uint32, uint256) external pure override {
2066:    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
2144:    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
2146:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
2152:    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
2195:    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256) {
2213:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
2250:    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
2252:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1,130p'" in /home/claude/code/clan-world
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
    81	    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
    82	    uint256 public constant MAX_CROP_TRANSITION_PER_TICK = 48;
    83	
    84	    // =========================================================================
    85	    // CONSTRUCTOR
    86	    // =========================================================================
    87	
    88	    constructor() {
    89	        _world.currentTick = 0;
    90	        _world.nextHeartbeatAtTs = uint64(block.timestamp);
    91	        _world.seasonStartTick = 0;
    92	        _world.seasonEndTick = ClanWorldConstants.SEASON_DURATION_TICKS;
    93	        _world.currentSeasonNumber = 1;
    94	        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
    95	        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
    96	        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
    97	        _world.winterActive = false;
    98	        _treasury.treasuryOwner = msg.sender;
    99	        _nextClanId = 1;
   100	        _nextClansmanId = 1;
   101	    }
   102	
   103	    // =========================================================================
   104	    // TRAVEL DISTANCE MATRIX
   105	    // =========================================================================
   106	
   107	    // Precomputed BFS distances for 8 regions (1-indexed; index 0 unused).
   108	    // Adjacency:
   109	    //   Forest(1):       Mountains(2), WestFarms(4)
   110	    //   Mountains(2):    Forest(1), UnicornTown(3)
   111	    //   UnicornTown(3):  Mountains(2), WestFarms(4), EastFarms(5)
   112	    //   WestFarms(4):    Forest(1), UnicornTown(3), WestDocks(6)
   113	    //   EastFarms(5):    UnicornTown(3), EastDocks(7)
   114	    //   WestDocks(6):    WestFarms(4), DeepSea(8)
   115	    //   EastDocks(7):    EastFarms(5), DeepSea(8)
   116	    //   DeepSea(8):      WestDocks(6), EastDocks(7)
   117	    //
   118	    // Distance table dist[src][dst] — 0-indexed internally (region - 1).
   119	    // Distance of 0 = same region.
   120	    //
   121	    // Full BFS-computed 8x8 matrix:
   122	    //   src\dst  1  2  3  4  5  6  7  8
   123	    //      1     0  1  2  1  3  2  4  3
   124	    //      2     1  0  1  2  2  3  3  4
   125	    //      3     2  1  0  1  1  2  2  3
   126	    //      4     1  2  1  0  2  1  3  2
   127	    //      5     3  2  1  2  0  3  1  2
   128	    //      6     2  3  2  1  3  0  2  1
   129	    //      7     4  3  2  3  1  2  0  1
   130	    //      8     3  4  3  2  2  1  1  0

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '650,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   650	        bool starving,
   651	        bytes32 tickSeed
   652	    ) internal {
   653	        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
   654	        if (remaining == 0) {
   655	            _completeMission(cs, m);
   656	            return;
   657	        }
   658	        uint256 yield = ClanWorldConstants.WOOD_BASE_YIELD;
   659	        // Crit roll: domain-separated RNG
   660	        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   661	        uint256 critRoll = uint256(critRng) % 10000;
   662	        if (critRoll < ClanWorldConstants.WOOD_CRIT_BPS) {
   663	            yield += ClanWorldConstants.WOOD_CRIT_BONUS;
   664	        }
   665	        if (starving) yield = yield / 2;
   666	        if (yield > remaining) yield = remaining;
   667	        cs.carryWood += yield;
   668	
   669	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   670	
   671	        if (cs.carryWood >= ClanWorldConstants.WOOD_CAP) {
   672	            _completeMission(cs, m);
   673	        }
   674	        // else continuous — worker stays ACTING
   675	    }
   676	
   677	    function _gatherIron(
   678	        Clan storage clan,
   679	        Clansman storage cs,
   680	        Mission storage m,
   681	        uint32 clanId,
   682	        uint64 tick,
   683	        bool starving,
   684	        bytes32 tickSeed
   685	    ) internal {
   686	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   687	        if (remaining == 0) {
   688	            _completeMission(cs, m);
   689	            return;
   690	        }
   691	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   692	        if (starving) yield = yield / 2;
   693	        if (yield > remaining) yield = remaining;
   694	        cs.carryIron += yield;
   695	
   696	        // Gold bonus roll — scoped to reduce stack depth
   697	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   698	
   699	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   700	
   701	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   702	            _completeMission(cs, m);
   703	        }
   704	    }
   705	
   706	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   707	        internal
   708	        returns (uint256 goldBonus)
   709	    {
   710	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   711	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   712	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   713	            clan.goldBalance += goldBonus;
   714	        }
   715	    }
   716	
   717	    function _gatherFishDocks(
   718	        Clan storage,
   719	        Clansman storage cs,
   720	        Mission storage m,
   721	        uint32 clanId,
   722	        uint64 tick,
   723	        bool starving,
   724	        bytes32 tickSeed
   725	    ) internal {
   726	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   727	        if (remaining == 0) {
   728	            _completeMission(cs, m);
   729	            return;
   730	        }
   731	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   732	        uint256 fishRoll = uint256(fishRng) % 10000;
   733	        uint256 yield = 0;
   734	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   735	            yield = 1e18;
   736	        }
   737	        if (starving) yield = yield / 2;
   738	        if (yield > remaining) yield = remaining;
   739	        if (yield > 0) {
   740	            cs.carryFish += yield;
   741	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   742	        }
   743	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   744	            _completeMission(cs, m);
   745	        }
   746	    }
   747	
   748	    function _gatherFishDeepSea(
   749	        Clan storage,
   750	        Clansman storage cs,
   751	        Mission storage m,
   752	        uint32 clanId,
   753	        uint64 tick,
   754	        bool starving,
   755	        bytes32 tickSeed
   756	    ) internal {
   757	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   758	        if (remaining == 0) {
   759	            _completeMission(cs, m);
   760	            return;

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1380,1880p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '2060,2445p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
  2361	                Clansman storage cs = _clansmen[csId];
  2362	                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
  2363	                    Mission storage m = _missions[csId];
  2364	                    occupants[idx++] = RegionOccupant({
  2365	                        clansmanId: csId,
  2366	                        clanId: cid,
  2367	                        state: cs.state,
  2368	                        currentAction: m.active ? m.action : ActionType.None,
  2369	                        missionNonce: cs.lastMissionNonce
  2370	                    });
  2371	                }
  2372	            }
  2373	        }
  2374	        return occupants;
  2375	    }
  2376	}

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md' | sed -n '248,258p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   248	### 5.6 Implement starvation consequences
   249	- next-tick starvation start
   250	- 50% gathering penalty
   251	- 0 defense contribution
   252	
   253	**Definition of done**
   254	- resources can be gathered, carried, deposited, and consumed by the economy
   255	
   256	**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
   257	
   258	---

exec
/bin/bash -lc "rg -n \"starvationStartsAtTick|next-tick starvation|starvation start|winter starvation|Phase 5.6|starvation consequences\" 'docs/planning/V1/01 Blockchain Game Spec'" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:237:    uint64 starvationStartsAtTick; // 0 = none
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md' | sed -n '280,296p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   280	
   281	### L.1 Canonical recommendation
   282	If both:
   283	- `starvationStartsAtTick`
   284	- `starvingCached`
   285	
   286	exist, remove `starvingCached` in favor of deriving starvation from canonical state.
   287	
   288	### L.2 Preferred model
   289	Use:
   290	- settlement checkpoint data
   291	- `starvationStartsAtTick`
   292	- current tick context
   293	
   294	rather than redundant cached boolean state when avoidable.
   295	
   296	---

exec
/bin/bash -lc "nl -ba 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md' | sed -n '384,392p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   384	    uint8 monumentLevel;
   385	    uint8 livingClansmen;
   386	
   387	    uint64 lastSettledTick;
   388	    uint64 starvationStartsAtTick; // 0 or max sentinel if none
   389	    bool starvingCached;
   390	
   391	    uint16 coldDamage;             // resets to 0 at winter end
   392	

codex
I’ve got two concrete contract-level risks already and I’m waiting on the parallel passes to see whether they uncover anything else on the spec/cross-phase seams before I lock the severity ordering.
collab: Wait
collab: Wait
codex
One of the parallel passes has confirmed the RNG issue I suspected. I’m giving the other two a little more time for the spec-boundary and dead-clan seam checks, then I’ll consolidate the review.
collab: Wait
exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '760,845p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   760	            return;
   761	        }
   762	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   763	        uint256 fishRoll = uint256(fishRng) % 10000;
   764	        uint256 yield = 0;
   765	        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
   766	            yield = 1e18;
   767	        }
   768	        if (starving) yield = yield / 2;
   769	        if (yield > remaining) yield = remaining;
   770	        if (yield > 0) {
   771	            cs.carryFish += yield;
   772	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
   773	        }
   774	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   775	            _completeMission(cs, m);
   776	        }
   777	    }
   778	
   779	    function _gatherWheat(
   780	        Clan storage,
   781	        /* clan — unused but kept positional for callsite parity */
   782	        Clansman storage cs,
   783	        Mission storage m,
   784	        uint32 clanId,
   785	        uint64 tick,
   786	        bool starving
   787	    ) internal {
   788	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
   789	        if (remaining == 0) {
   790	            _completeMission(cs, m);
   791	            return;
   792	        }
   793	        // Determine plot index from region
   794	        uint8 region = m.targetRegion;
   795	        uint256 plotIdx;
   796	        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
   797	            plotIdx = 0;
   798	        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
   799	            plotIdx = 1;
   800	        } else {
   801	            // Wrong region — complete (no harvest)
   802	            _completeMission(cs, m);
   803	            return;
   804	        }
   805	
   806	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
   807	        if (plot.state == WheatPlotState.WinterLocked) {
   808	            // Winter-locked plots cannot be harvested; queued missions end with no yield.
   809	            _completeMission(cs, m);
   810	            return;
   811	        }
   812	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
   813	            // Plot not ready — worker waits
   814	            _completeMission(cs, m);
   815	            return;
   816	        }
   817	
   818	        uint256 yield = WHEAT_HARVEST_RATE;
   819	        if (starving) yield = yield / 2;
   820	        if (yield > remaining) yield = remaining;
   821	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
   822	
   823	        cs.carryWheat += yield;
   824	        plot.remainingWheat -= yield;
   825	
   826	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
   827	
   828	        if (plot.remainingWheat == 0) {
   829	            plot.state = WheatPlotState.Regrowing;
   830	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
   831	        }
   832	
   833	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
   834	            _completeMission(cs, m);
   835	        }
   836	        // else continuous
   837	    }
   838	
   839	    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
   840	        internal
   841	    {
   842	        // Must be at homebase region
   843	        if (cs.currentRegion != clan.baseRegion) {
   844	            _completeMission(cs, m);
   845	            return;

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '2088,2290p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  2088	        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
  2089	        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
  2090	        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
  2091	        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
  2092	        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
  2093	        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
  2094	        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
  2095	
  2096	        _advanceToTick(expectedRegrowUntil + 1);
  2097	        world.settleClan(clanId1);
  2098	        world.settleClan(clanId2);
  2099	
  2100	        (west1, east1) = world.getWheatPlots(clanId1);
  2101	        (west2, east2) = world.getWheatPlots(clanId2);
  2102	        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
  2103	        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
  2104	        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
  2105	        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
  2106	        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
  2107	        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
  2108	    }
  2109	
  2110	    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
  2111	        uint32 clanId = _mintClan();
  2112	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2113	        _advanceToTick(winterStart - 2);
  2114	
  2115	        OrderResult[] memory results =
  2116	            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
  2117	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
  2118	        Mission memory queuedMission = world.getActiveMission(1);
  2119	
  2120	        _advanceToTick(queuedMission.settlesAtTick + 1);
  2121	
  2122	        Clansman memory cs = world.getClansman(1);
  2123	        Mission memory mission = world.getActiveMission(1);
  2124	        (WheatPlot memory west,) = world.getWheatPlots(clanId);
  2125	        assertFalse(mission.active, "locked harvest mission should complete");
  2126	        assertEq(cs.carryWheat, 0, "locked harvest should yield no wheat");
  2127	        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
  2128	        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
  2129	    }
  2130	
  2131	    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
  2132	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2133	        world = harness;
  2134	        uint32 clanId = _mintClan();
  2135	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2136	        _advanceToTick(winterStart + 1);
  2137	
  2138	        harness.setClanWallLevel(clanId, 2);
  2139	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
  2140	
  2141	        world.settleClan(clanId);
  2142	
  2143	        Clan memory clan = world.getClan(clanId);
  2144	        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
  2145	        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
  2146	        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
  2147	        assertEq(clan.coldDamage, 0, "sufficient wood should avoid cold damage");
  2148	        assertEq(clan.wallLevel, 2, "sufficient wood should not degrade wall");
  2149	        assertEq(clan.livingClansmen, 4, "sufficient wood should not kill clansmen");
  2150	    }
  2151	
  2152	    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
  2153	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2154	        world = harness;
  2155	        uint32 clanId = _mintClan();
  2156	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2157	        _advanceToTick(winterStart + 1);
  2158	
  2159	        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
  2160	
  2161	        vm.expectEmit(true, false, false, true);
  2162	        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
  2163	        world.settleClan(clanId);
  2164	
  2165	        Clan memory clan = world.getClan(clanId);
  2166	        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
  2167	        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
  2168	    }
  2169	
  2170	    function test_coldDamage_degradesWallEveryTwoShortages() public {
  2171	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2172	        world = harness;
  2173	        uint32 clanId = _mintClan();
  2174	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2175	        _advanceToTick(winterStart + 1);
  2176	
  2177	        harness.setClanWallLevel(clanId, 2);
  2178	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2179	
  2180	        vm.expectEmit(true, false, false, true);
  2181	        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
  2182	        world.settleClan(clanId);
  2183	
  2184	        Clan memory clan = world.getClan(clanId);
  2185	        assertEq(clan.coldDamage, 2, "cold damage should increment");
  2186	        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
  2187	        assertEq(clan.livingClansmen, 4, "wall should absorb cold before clansmen die");
  2188	    }
  2189	
  2190	    function test_coldDamage_zeroWallKillsOneClansmanEveryTwoShortages() public {
  2191	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2192	        world = harness;
  2193	        uint32 clanId = _mintClan();
  2194	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2195	        _advanceToTick(winterStart + 1);
  2196	
  2197	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2198	
  2199	        vm.recordLogs();
  2200	        world.settleClan(clanId);
  2201	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2202	
  2203	        Clan memory clan = world.getClan(clanId);
  2204	        assertEq(clan.coldDamage, 2, "cold damage should hit death threshold");
  2205	        assertEq(clan.wallLevel, 0, "wall should remain clamped at zero");
  2206	        assertEq(clan.livingClansmen, 3, "one clansman should die from cold");
  2207	        assertEq(_countLogs(logs, keccak256("ClansmanColdDeath(uint32,uint32,uint32)")), 1, "cold death should emit");
  2208	
  2209	        ClanFullView memory view_ = world.getClanFullView(clanId);
  2210	        uint256 deadCount = 0;
  2211	        for (uint256 i = 0; i < view_.clansmen.length; i++) {
  2212	            if (view_.clansmen[i].clansman.clansman.state == ClansmanState.DEAD) {
  2213	                deadCount++;
  2214	            }
  2215	        }
  2216	        assertEq(deadCount, 1, "exactly one stored clansman should be dead");
  2217	    }
  2218	
  2219	    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
  2220	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2221	        world = harness;
  2222	        uint32 clanId = _mintClan();
  2223	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2224	        uint256 goldBalance = 11e18;
  2225	
  2226	        _advanceToTick(winterStart + 4);
  2227	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2228	        harness.setClanIronAndGold(clanId, 5e18, goldBalance);
  2229	
  2230	        vm.recordLogs();
  2231	        world.settleClan(clanId);
  2232	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2233	
  2234	        Clan memory clan = world.getClan(clanId);
  2235	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
  2236	        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
  2237	        assertEq(clan.vaultWood, 0, "wood should burn on death");
  2238	        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
  2239	        assertEq(clan.vaultFish, 0, "fish should burn on death");
  2240	        assertEq(clan.vaultIron, 0, "iron should burn on death");
  2241	        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
  2242	        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
  2243	    }
  2244	
  2245	    function test_clanDeath_coldDamageMarksDeadAndBurnsVault() public {
  2246	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2247	        world = harness;
  2248	        uint32 clanId = _mintClan();
  2249	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2250	        uint256 goldBalance = 13e18;
  2251	        _advanceToTick(winterStart + 7);
  2252	
  2253	        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
  2254	        harness.setClanIronAndGold(clanId, 9e18, goldBalance);
  2255	
  2256	        vm.recordLogs();
  2257	        world.settleClan(clanId);
  2258	        Vm.Log[] memory logs = vm.getRecordedLogs();
  2259	
  2260	        Clan memory clan = world.getClan(clanId);
  2261	        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "cold should mark clan dead");
  2262	        assertEq(clan.livingClansmen, 0, "all clansmen should be dead");
  2263	        assertEq(clan.vaultWood, 0, "wood should burn on death");
  2264	        assertEq(clan.vaultWheat, 0, "wheat should burn on death");
  2265	        assertEq(clan.vaultFish, 0, "fish should burn on death");
  2266	        assertEq(clan.vaultIron, 0, "iron should burn on death");
  2267	        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
  2268	        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
  2269	    }
  2270	
  2271	    function test_deadClanCannotSubmitClanOrders() public {
  2272	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2273	        world = harness;
  2274	        uint32 clanId = _mintClan();
  2275	
  2276	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
  2277	        _advanceToTick(winterStart + 4);
  2278	        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
  2279	        world.settleClan(clanId);
  2280	
  2281	        vm.expectRevert("ClanWorld: clan dead");
  2282	        _submitOrder(clanId, 1, ClanWorldConstants.REGION_FOREST, ActionType.Wait);
  2283	    }
  2284	
  2285	    function test_clanDeath_doesNotAffectOtherClan() public {
  2286	        ClanWorldTestHarness harness = new ClanWorldTestHarness();
  2287	        world = harness;
  2288	        uint32 clanIdA = _mintClan();
  2289	        uint32 clanIdB = _mintClan();
  2290	        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;

exec
/bin/bash -lc "git show e4a0d4c:packages/contracts/src/lib/RNG.sol | nl -ba | sed -n '1,40p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
    12	    uint256 internal constant DOMAIN_COLD_DAMAGE = uint256(keccak256("cold_damage"));
    13	
    14	    uint256 internal constant MAX_SHUFFLE_N = 64;
    15	
    16	    error ShuffleSizeTooLarge(uint256 n, uint256 max);
    17	
    18	    /// @notice Returns a uniform uint256 in [0, 2^256).
    19	    function rngUniform(bytes32 seed, uint256 domainSalt, uint256 nonce) internal pure returns (uint256) {
    20	        return uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce)));
    21	    }
    22	
    23	    /// @notice Returns a uniform value in [0, max) using rejection sampling.
    24	    /// @dev Returns 0 when max is 0 so callers can handle optional random choices cheaply.
    25	    function rngBounded(bytes32 seed, uint256 domainSalt, uint256 nonce, uint256 max) internal pure returns (uint256) {
    26	        if (max == 0) {
    27	            return 0;
    28	        }
    29	
    30	        uint256 remainder;
    31	        unchecked {
    32	            // Computes 2^256 % max without trying to represent 2^256 directly.
    33	            remainder = (uint256(0) - max) % max;
    34	        }
    35	        uint256 maxValid = type(uint256).max - remainder;
    36	        uint256 attempt = 0;
    37	
    38	        while (true) {
    39	            uint256 value = uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce, max, attempt)));
    40	            if (value <= maxValid) {

collab: CloseAgent
2026-04-29T22:34:06.242118Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5b-6178-7c03-aefd-007fab9c1806 not found
2026-04-29T22:34:06.249080Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5b-6242-7953-aadc-158a2224fbf7 not found
2026-04-29T22:34:06.255832Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5b-61bc-7fa0-b9c2-ef7ae2229bf6 not found
collab: CloseAgent
collab: CloseAgent
  ✓ Gather contract, test, and spec context around winter, starvation, elimination, and dead-clan rules
  ✓ Delegate focused parallel checks for boundary timing and cross-phase dead-clan interactions
  → Synthesize findings into final pre-merge review with severity ranking and file references
codex
# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
## SUMMARY
Not ready to merge. I found 2 high-severity issues that change game outcomes, plus 2 medium risks and 1 low hygiene issue. The biggest problems are non-deterministic cold-death RNG during lazy settlement and an off-by-one starvation kill path that makes winter elimination happen a tick earlier than the spec.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:486-503` uses `_world.currentTickSeed` to pick the cold-death victim, while winter catch-up settlement replays historical ticks in `_settleClan` at `packages/contracts/src/ClanWorld.sol:371-389`. That makes historical cold deaths depend on when `settleClan` is finally called, not on the tick being settled. The same clan can lose a different clansman if settled one heartbeat later, which breaks deterministic replay and violates the intended per-tick RNG model.
- `packages/contracts/src/ClanWorld.sol:431-442` kills from winter starvation on the same tick that shortage is first detected, because `starvationStartsAtTick` is set to `tick` and then immediately used for lethality. That conflicts with the spec’s “update starvation status for the next tick” ordering in `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:83-90` and `.../clanworld_numbered_implementation_plan.md:248-250`. The new test at `packages/contracts/test/ClanWorld.t.sol:2219-2242` also bakes in the off-by-one by expecting total death at `winterStart + 3`; under next-tick starvation start, that elimination should land a tick later.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:1132-1140` locks wheat plots for winter without clearing `remainingWheat` or resetting regrow state, and the tests at `packages/contracts/test/ClanWorld.t.sol:2069-2074` and `:2125-2128` explicitly assert that preserved-state behavior. That disagrees with `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843-846`, which says winter start clears all plots. Harvest is blocked, so this is mostly a state/spec mismatch today, but getters will expose impossible in-winter crop state and future logic can accidentally depend on it.
- `packages/contracts/test/ClanWorld.t.sol:2190-2268` does not actually pin RNG determinism or exact death identity/timing. The new tests assert “one clansman died” and “clan eventually died,” but never assert which `csId` was chosen or that two equivalent settlement schedules produce the same result. That gap is why the seed bug above can slip through.

## LOW severity findings
- `packages/contracts/src/lib/RNG.sol:12` introduces `DOMAIN_COLD_DAMAGE` as `keccak256("cold_damage")`, unlike the existing namespaced/versioned salts (`clanworld.*.v1`). It still separates the call site, but it’s weaker RNG hygiene than the rest of the library.

## Cross-cutting observations
- The recurring winter schedule itself looks aligned with A2: first winter at tick 110, then every 110 ticks after that (`packages/contracts/src/IClanWorld.sol:28-32`, `packages/contracts/src/ClanWorld.sol:1177-1206`).
- Dead-clan submission handling is coherent: already-dead clans revert at entry, and clans that die during `_settleClan` return `ERR_CLAN_DEAD` for the batch (`packages/contracts/src/ClanWorld.sol:1320-1357`).
- `packages/contracts/src/IClanWorld.sol:496-498` changes `WinterStarted`/`WinterEnded` to `uint32` event signatures. That is fine only if every downstream consumer regenerates from the updated ABI; any hand-written decoders keyed to the old `uint64` signature will break.
- Review was static against the provided diff and `git show e4a0d4c`; I did not execute `forge test` in this read-only environment.
2026-04-29T22:34:49.938151Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5a-e511-7d02-9013-8e8851981be4 not found
tokens used
144,842
# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
## SUMMARY
Not ready to merge. I found 2 high-severity issues that change game outcomes, plus 2 medium risks and 1 low hygiene issue. The biggest problems are non-deterministic cold-death RNG during lazy settlement and an off-by-one starvation kill path that makes winter elimination happen a tick earlier than the spec.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:486-503` uses `_world.currentTickSeed` to pick the cold-death victim, while winter catch-up settlement replays historical ticks in `_settleClan` at `packages/contracts/src/ClanWorld.sol:371-389`. That makes historical cold deaths depend on when `settleClan` is finally called, not on the tick being settled. The same clan can lose a different clansman if settled one heartbeat later, which breaks deterministic replay and violates the intended per-tick RNG model.
- `packages/contracts/src/ClanWorld.sol:431-442` kills from winter starvation on the same tick that shortage is first detected, because `starvationStartsAtTick` is set to `tick` and then immediately used for lethality. That conflicts with the spec’s “update starvation status for the next tick” ordering in `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:83-90` and `.../clanworld_numbered_implementation_plan.md:248-250`. The new test at `packages/contracts/test/ClanWorld.t.sol:2219-2242` also bakes in the off-by-one by expecting total death at `winterStart + 3`; under next-tick starvation start, that elimination should land a tick later.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:1132-1140` locks wheat plots for winter without clearing `remainingWheat` or resetting regrow state, and the tests at `packages/contracts/test/ClanWorld.t.sol:2069-2074` and `:2125-2128` explicitly assert that preserved-state behavior. That disagrees with `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843-846`, which says winter start clears all plots. Harvest is blocked, so this is mostly a state/spec mismatch today, but getters will expose impossible in-winter crop state and future logic can accidentally depend on it.
- `packages/contracts/test/ClanWorld.t.sol:2190-2268` does not actually pin RNG determinism or exact death identity/timing. The new tests assert “one clansman died” and “clan eventually died,” but never assert which `csId` was chosen or that two equivalent settlement schedules produce the same result. That gap is why the seed bug above can slip through.

## LOW severity findings
- `packages/contracts/src/lib/RNG.sol:12` introduces `DOMAIN_COLD_DAMAGE` as `keccak256("cold_damage")`, unlike the existing namespaced/versioned salts (`clanworld.*.v1`). It still separates the call site, but it’s weaker RNG hygiene than the rest of the library.

## Cross-cutting observations
- The recurring winter schedule itself looks aligned with A2: first winter at tick 110, then every 110 ticks after that (`packages/contracts/src/IClanWorld.sol:28-32`, `packages/contracts/src/ClanWorld.sol:1177-1206`).
- Dead-clan submission handling is coherent: already-dead clans revert at entry, and clans that die during `_settleClan` return `ERR_CLAN_DEAD` for the batch (`packages/contracts/src/ClanWorld.sol:1320-1357`).
- `packages/contracts/src/IClanWorld.sol:496-498` changes `WinterStarted`/`WinterEnded` to `uint32` event signatures. That is fine only if every downstream consumer regenerates from the updated ABI; any hand-written decoders keyed to the old `uint64` signature will break.
- Review was static against the provided diff and `git show e4a0d4c`; I did not execute `forge test` in this read-only environment.
