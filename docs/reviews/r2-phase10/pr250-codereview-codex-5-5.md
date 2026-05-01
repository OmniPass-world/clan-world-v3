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
session id: 019ddb5a-e95f-7660-9aaa-e3290b8931f8
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
I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
2026-04-29T22:27:58.251932Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T22:27:58.257592Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T22:27:58.346691Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
codex
The review agents are running now. I’m also going to inspect the local contract/package context directly so the final call isn’t just diff-reading in a vacuum.
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
/bin/bash -lc "rg -n \"function _markClanDead|_markClansmanDead|ClanEliminated|ERR_CLAN_DEAD|ClanState.DEAD|settleClan\\(|getClanFullView|enum ClanState|struct Clan|coldDamage|WinterLocked|WinterStarted|WinterEnded|winterStartsAtTick|TICKS_PER_WINTER_CYCLE|WINTER_START_TICK\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorld.sol:136:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:399:    ///      Mutates storage. Called before order submission and by public settleClan().
packages/contracts/src/ClanWorld.sol:400:    function _settleClan(uint32 clanId) internal {
packages/contracts/src/ClanWorld.sol:952:            if (clan.clanState == ClanState.DEAD) continue;
packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
packages/contracts/src/ClanWorld.sol:992:            emit WinterStarted(newTick);
packages/contracts/src/ClanWorld.sol:996:            emit WinterEnded(newTick);
packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:1013:    function settleClan(uint32 clanId) external override nonReentrant {
packages/contracts/src/ClanWorld.sol:1014:        _settleClan(clanId);
packages/contracts/src/ClanWorld.sol:1024:        _settleClan(cs.clanId);
packages/contracts/src/ClanWorld.sol:1067:        clan.coldDamage = 0;
packages/contracts/src/ClanWorld.sol:1125:        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
packages/contracts/src/ClanWorld.sol:1145:        _settleClan(clanId);
packages/contracts/src/ClanWorld.sol:2222:    ///      Carry amounts lag until next settleClan().
packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorld.sol:2424:    ///      current; carry amounts and wheat progress lag until next settleClan() call.
packages/contracts/src/ClanWorld.sol:2426:    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:47:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
packages/contracts/test/WallUpgrades.t.sol:40:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/WallUpgrades.t.sol:44:        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
packages/contracts/test/MissionTiming.t.sol:43:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/MissionTiming.t.sol:103:        world.settleClan(clanId);
packages/contracts/test/MissionTiming.t.sol:112:        world.settleClan(clanId);
packages/contracts/src/IClanWorld.sol:29:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
packages/contracts/src/IClanWorld.sol:96:enum ClanState {
packages/contracts/src/IClanWorld.sol:120:    WinterLocked
packages/contracts/src/IClanWorld.sol:156:    ERR_CLAN_DEAD,
packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:229:struct Clan {
packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
packages/contracts/src/IClanWorld.sol:262:struct Clansman {
packages/contracts/src/IClanWorld.sol:367:struct ClanOrder {
packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:427:struct ClansmanFullView {
packages/contracts/src/IClanWorld.sol:432:struct ClanFullView {
packages/contracts/src/IClanWorld.sol:495:    event WinterStarted(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:496:    event WinterEnded(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:504:    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:648:    function settleClan(uint32 clanId) external;
packages/contracts/src/IClanWorld.sol:791:    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory);
packages/contracts/test/Gathering.t.sol:49:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/Gathering.t.sol:75:        world.settleClan(clanId);
packages/contracts/test/HeartbeatOrdering.t.sol:80:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/HeartbeatOrdering.t.sol:84:        return world.getClanFullView(clanId).clansmen[1].clansman.clansman.clansmanId;
packages/contracts/test/MonumentUpgrades.t.sol:48:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/MonumentUpgrades.t.sol:52:        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
packages/contracts/test/MonumentUpgrades.t.sol:144:        assertEq(world.getClanFullView(clanId).clan.clan.monumentLevel, 1, "monument level after settle");
packages/contracts/test/RankGetters.t.sol:37:        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorldStub.sol:55:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorldStub.sol:86:    function settleClan(uint32) external override {}
packages/contracts/src/ClanWorldStub.sol:153:            coldDamage: 0,
packages/contracts/src/ClanWorldStub.sol:345:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorldStub.sol:353:    function getClanFullView(uint32) external view override returns (ClanFullView memory) {
packages/contracts/test/Reentrancy.t.sol:189:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/BaseUpgrades.t.sol:40:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/BaseUpgrades.t.sol:44:        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
packages/contracts/test/DefendBase.t.sol:32:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/DefendBase.t.sol:126:        world.settleClan(clanId);
packages/contracts/test/DefendBase.t.sol:143:        world.settleClan(clanId);
packages/contracts/test/DefendBase.t.sol:159:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:274:    // Test 5: mintClan — getClanFullView shows 4 WAITING clansmen at homebase
packages/contracts/test/ClanWorld.t.sol:279:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:298:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:338:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:356:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:393:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:417:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:430:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:446:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:462:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:602:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:639:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:692:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:698:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:722:        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:753:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:769:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:788:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:908:            ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:960:            ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:971:        ClanFullView memory nativeView = world.getClanFullView(nativeClanId);
packages/contracts/test/ClanWorld.t.sol:1053:        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:1128:        uint32 csId2 = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:1168:        world.settleClan(clanId1);
packages/contracts/test/ClanWorld.t.sol:1169:        world.settleClan(clanId2);
packages/contracts/test/ClanWorld.t.sol:1206:        ClanFullView memory view_ = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1248:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1296:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1322:        uint32 cs2Id = world.getClanFullView(clanId2).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:1351:        uint32 csId = harness.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:1398:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1414:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1445:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1474:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1496:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1505:            world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1529:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1590:        csId = harness.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
packages/contracts/test/ClanWorld.t.sol:1628:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1662:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1695:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1708:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1722:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1768:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1783:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1806:        world.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1821:        uint8 homeRegion = harness.getClanFullView(clanId).clan.clan.baseRegion;
packages/contracts/test/ClanWorld.t.sol:1839:        harness.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1856:        uint8 baseRegion = harness.getClanFullView(clanId).clan.clan.baseRegion;
packages/contracts/test/ClanWorld.t.sol:1885:        harness.settleClan(clanId);
packages/contracts/test/ClanWorld.t.sol:1902:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1932:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:1951:        ClanFullView memory v = world.getClanFullView(clanId);
packages/contracts/test/ClanWorld.t.sol:2029:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2030:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2036:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
packages/contracts/test/ClanWorld.t.sol:2037:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
packages/contracts/test/ClanWorld.t.sol:2038:        // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
packages/contracts/test/ClanWorld.t.sol:2040:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/test/ClanWorld.t.sol:2045:        // currentTick == 99; next heartbeat opens tick 100 and should emit WinterStarted(100)
packages/contracts/test/ClanWorld.t.sol:2051:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
packages/contracts/test/ClanWorld.t.sol:2052:        bool foundWinterStarted = false;
packages/contracts/test/ClanWorld.t.sol:2055:                foundWinterStarted = true;
packages/contracts/test/ClanWorld.t.sol:2059:        assertTrue(foundWinterStarted, "WinterStarted event should have been emitted at tick 100");
packages/contracts/test/ClanWorld.t.sol:2066:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/test/ClanWorld.t.sol:2075:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2076:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2092:            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/test/ClanWorld.t.sol:2093:        assertEq(ws.winterStartsAtTick, expectedWinterStart);

exec
/bin/bash -lc 'git status --short && git rev-parse --short HEAD && git branch --show-current' in /home/claude/code/clan-world
 succeeded in 0ms:
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
?? docs/reviews/pr198-synthesis.md
?? docs/reviews/pr199-codereview-codex-5-4.md
?? docs/reviews/pr199-codereview-codex-5-5.md
?? docs/reviews/pr199-codereview-opus-4-7.md
?? docs/reviews/pr200-codereview-codex-5-4.md
?? docs/reviews/pr200-codereview-codex-5-5.md
?? docs/reviews/pr200-codereview-opus-4-7.md
?? docs/reviews/pr250-codereview-codex-5-4.md
?? docs/reviews/pr250-codereview-codex-5-5.md
?? docs/reviews/pr250-codereview-opus-4-7.md
?? docs/reviews/r2/
33193b7
pr199-review

codex
Local files aren’t at the PR head, so I’m treating the pasted diff as authoritative and using the repo mainly for surrounding contracts/spec context. One thing already stands out: this PR changes the winter start model from the older `[100,110)` style to `WINTER_START_TICK = 110`, so I’m checking whether that matches the phase spec rather than assuming it’s intentional.
exec
/bin/bash -lc "sed -n '80,180p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "rg -n \"A2|winter|starvation|elimination|clan death|dead clan|cold_damage|cold damage|WinterLocked|WINTER_START|TICKS_PER_WINTER|4\\.12|10\\.|Phase 10\" docs packages -g '*.md' -g '*.sol'" in /home/claude/code/clan-world
 succeeded in 1ms:
packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorld.sol:136:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:138:        _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:349:        // Path 6: dead clansman — invalidate active mission if any
packages/contracts/src/ClanWorld.sol:361:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
packages/contracts/src/ClanWorld.sol:443:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
packages/contracts/src/ClanWorld.sol:465:        if (starving && clan.starvationStartsAtTick == 0) {
packages/contracts/src/ClanWorld.sol:466:            clan.starvationStartsAtTick = tick;
packages/contracts/src/ClanWorld.sol:468:        } else if (!starving && clan.starvationStartsAtTick != 0) {
packages/contracts/src/ClanWorld.sol:469:            clan.starvationStartsAtTick = 0;
packages/contracts/src/ClanWorld.sol:476:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:909:    ///         4. Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:934:        // Step 4: Resolve world events (season boundary, winter transitions).
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
packages/contracts/src/ClanWorld.sol:2221:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
packages/contracts/src/ClanWorld.sol:2225:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/ClanWorld.sol:2414:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorld.sol:2416:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/src/ClanWorld.sol:2423:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
packages/contracts/src/ClanWorld.sol:2428:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
packages/contracts/src/IClanWorld.sol:29:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
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
packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorldStub.sol:55:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorldStub.sol:57:        _world.winterActive = false;
packages/contracts/src/ClanWorldStub.sol:152:            starvationStartsAtTick: 0,
packages/contracts/src/ClanWorldStub.sol:344:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorldStub.sol:345:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorldStub.sol:346:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:47:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
packages/contracts/test/ClanWorldStub.t.sol:50:        assertFalse(ws.winterActive);
packages/contracts/test/WallUpgrades.t.sol:28:    address elder2 = address(0xA2);
packages/contracts/test/RankGetters.t.sol:24:    address elderB = address(0xA2);
packages/contracts/test/BaseUpgrades.t.sol:28:    address elder2 = address(0xA2);
packages/contracts/test/MonumentUpgrades.t.sol:36:    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:174:                 m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr199-codereview-codex-5-4.md:177:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-4.md:1120:+    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:1623:+    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:1827:+    address elderB = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:2036:+    address elder2 = address(0xA2);
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
docs/reviews/pr199-codereview-codex-5-4.md:4106:    29	    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/pr199-codereview-codex-5-4.md:4280:   203	    bool winterActive;
docs/reviews/pr199-codereview-codex-5-4.md:4281:   204	    uint64 winterStartsAtTick;
docs/reviews/pr199-codereview-codex-5-4.md:4282:   205	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr199-codereview-codex-5-4.md:4396:   618	    // ----- winter cold damage -----
docs/reviews/pr199-codereview-codex-5-4.md:4563:   785	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:4613:   934	        // Step 4: Resolve world events (season boundary, winter transitions).
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
docs/reviews/pr199-codereview-codex-5-4.md:5279:docs/reviews/pr199-codereview-codex-5-4.md:4563:   785	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5352:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:5492:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:5493:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:5580:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5622:docs/reviews/pr250-codereview-codex-5-4.md:1974:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5623:docs/reviews/pr250-codereview-codex-5-4.md:2270:docs/reviews/pr199-codereview-codex-5-4.md:4563:   785	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5624:docs/reviews/pr250-codereview-codex-5-4.md:2510:docs/reviews/pr250-codereview-codex-5-4.md:1974:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5625:docs/reviews/pr250-codereview-codex-5-4.md:2766:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:5628:docs/reviews/pr250-codereview-codex-5-4.md:2803:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5629:docs/reviews/pr250-codereview-codex-5-4.md:2849:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5630:docs/reviews/pr250-codereview-codex-5-4.md:2871:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5631:docs/reviews/pr250-codereview-codex-5-4.md:2873:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:5635:docs/reviews/pr250-codereview-codex-5-4.md:3452:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:5636:docs/reviews/pr250-codereview-codex-5-4.md:3453:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:5637:docs/reviews/pr250-codereview-codex-5-4.md:3555:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5638:docs/reviews/pr250-codereview-codex-5-4.md:3562:docs/reviews/pr199-codereview-codex-5-5.md:5365:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:5639:docs/reviews/pr250-codereview-codex-5-4.md:3566:docs/reviews/pr199-codereview-codex-5-5.md:5457:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:5640:docs/reviews/pr250-codereview-codex-5-4.md:3570:docs/reviews/pr199-codereview-codex-5-5.md:5508:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5641:docs/reviews/pr250-codereview-codex-5-4.md:3571:docs/reviews/pr199-codereview-codex-5-5.md:5603:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5642:docs/reviews/pr250-codereview-codex-5-4.md:3572:docs/reviews/pr199-codereview-codex-5-5.md:5607:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5643:docs/reviews/pr250-codereview-codex-5-4.md:3573:docs/reviews/pr199-codereview-codex-5-5.md:5609:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5645:docs/reviews/pr250-codereview-codex-5-4.md:3578:docs/reviews/pr199-codereview-codex-5-5.md:5880:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:5650:docs/reviews/pr250-codereview-codex-5-4.md:3604:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:5651:docs/reviews/pr250-codereview-codex-5-4.md:3605:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:5652:docs/reviews/pr250-codereview-codex-5-4.md:3609:docs/reviews/pr199-codereview-codex-5-5.md:7008:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr199-codereview-codex-5-4.md:5653:docs/reviews/pr250-codereview-codex-5-4.md:3610:docs/reviews/pr199-codereview-codex-5-5.md:7068:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:5654:docs/reviews/pr250-codereview-codex-5-4.md:3611:docs/reviews/pr199-codereview-codex-5-5.md:7074:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5655:docs/reviews/pr250-codereview-codex-5-4.md:3612:docs/reviews/pr199-codereview-codex-5-5.md:7162:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-4.md:5656:docs/reviews/pr250-codereview-codex-5-4.md:3613:docs/reviews/pr199-codereview-codex-5-5.md:7163:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5657:docs/reviews/pr250-codereview-codex-5-4.md:3614:docs/reviews/pr199-codereview-codex-5-5.md:7167:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5659:docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr199-codereview-codex-5-4.md:5660:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr199-codereview-codex-5-4.md:5661:docs/reviews/pr250-codereview-codex-5-4.md:3629:docs/reviews/pr199-codereview-codex-5-5.md:7384:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:5662:docs/reviews/pr250-codereview-codex-5-4.md:3635:docs/reviews/pr199-codereview-codex-5-5.md:8708:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5663:docs/reviews/pr250-codereview-codex-5-4.md:3845:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5664:docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr199-codereview-codex-5-4.md:5665:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr199-codereview-codex-5-4.md:5666:docs/reviews/pr250-codereview-codex-5-4.md:3889:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5667:docs/reviews/pr250-codereview-codex-5-4.md:3911:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5668:docs/reviews/pr250-codereview-codex-5-4.md:3960:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-4.md:5669:docs/reviews/pr250-codereview-codex-5-4.md:3962:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5670:docs/reviews/pr250-codereview-codex-5-4.md:4000:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5672:docs/reviews/pr250-codereview-codex-5-4.md:4308:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:5673:docs/reviews/pr250-codereview-codex-5-4.md:4339:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:5674:docs/reviews/pr250-codereview-codex-5-4.md:4423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:5675:docs/reviews/pr250-codereview-codex-5-4.md:4499:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:5676:docs/reviews/pr250-codereview-codex-5-4.md:4527:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:5677:docs/reviews/pr250-codereview-codex-5-4.md:4540:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr199-codereview-codex-5-4.md:5682:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr199-codereview-codex-5-4.md:5683:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr199-codereview-codex-5-4.md:5688:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5795:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-4.md:5796:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5798:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:5915:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:6026:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:6027:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:6029:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:6347:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:47:| `gold-bright` | `#D4A24C` | Highlights, leaderboard winner |
docs/reviews/pr199-codereview-codex-5-4.md:6368:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr199-codereview-codex-5-4.md:6414:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:6426:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:6444:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:6460:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:6496:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/reviews/pr199-codereview-codex-5-4.md:6525:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:6588:docs/planning/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/reviews/pr199-codereview-codex-5-4.md:6632:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:6915:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:7073:docs/reviews/pr199-codereview-codex-5-5.md:5365:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:7098:docs/reviews/pr199-codereview-codex-5-5.md:5422:docs/planning/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/reviews/pr199-codereview-codex-5-4.md:7109:docs/reviews/pr199-codereview-codex-5-5.md:5457:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:7111:docs/reviews/pr199-codereview-codex-5-5.md:5508:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:7183:docs/reviews/pr199-codereview-codex-5-5.md:5603:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:7184:docs/reviews/pr199-codereview-codex-5-5.md:5607:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:7186:docs/reviews/pr199-codereview-codex-5-5.md:5609:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:7267:docs/reviews/pr199-codereview-codex-5-5.md:5880:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-4.md:7414:docs/reviews/pr199-codereview-codex-5-5.md:6543:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:47:| `gold-bright` | `#D4A24C` | Highlights, leaderboard winner |
docs/reviews/pr199-codereview-codex-5-4.md:7524:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:7525:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-4.md:7536:docs/reviews/pr199-codereview-codex-5-5.md:7008:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr199-codereview-codex-5-4.md:7541:docs/reviews/pr199-codereview-codex-5-5.md:7068:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-4.md:7543:docs/reviews/pr199-codereview-codex-5-5.md:7074:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:7612:docs/reviews/pr199-codereview-codex-5-5.md:7162:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-4.md:7613:docs/reviews/pr199-codereview-codex-5-5.md:7163:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:7615:docs/reviews/pr199-codereview-codex-5-5.md:7167:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-4.md:7664:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr199-codereview-codex-5-4.md:7665:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr199-codereview-codex-5-4.md:7680:docs/reviews/pr199-codereview-codex-5-5.md:7384:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-4.md:7705:docs/reviews/pr199-codereview-codex-5-5.md:7441:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/reviews/pr199-codereview-codex-5-4.md:7910:docs/reviews/pr199-codereview-codex-5-5.md:8708:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-4.md:8059:    28	    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:8239:    28	    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:8425:    36	    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:8623:    24	    address elderB = address(0xA2);
docs/reviews/pr199-codereview-codex-5-4.md:9120:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
packages/contracts/test/ClanWorld.t.sol:63:    address elder2 = address(0xA2);
packages/contracts/test/ClanWorld.t.sol:1342:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
packages/contracts/test/ClanWorld.t.sol:1370:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
packages/contracts/test/ClanWorld.t.sol:1816:    // Path 6: dead clansman mid-mission → mission invalidated
packages/contracts/test/ClanWorld.t.sol:2020:    // Phase 4.4 — season + winter timer tests
packages/contracts/test/ClanWorld.t.sol:2029:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2030:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2032:        assertFalse(ws.winterActive);
packages/contracts/test/ClanWorld.t.sol:2035:    function test_winter_onset() public {
packages/contracts/test/ClanWorld.t.sol:2036:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
packages/contracts/test/ClanWorld.t.sol:2037:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
packages/contracts/test/ClanWorld.t.sol:2039:        uint64 winterStart =
packages/contracts/test/ClanWorld.t.sol:2040:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/test/ClanWorld.t.sol:2041:        for (uint64 i = 0; i < winterStart - 1; i++) {
packages/contracts/test/ClanWorld.t.sol:2051:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
packages/contracts/test/ClanWorld.t.sol:2054:            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
packages/contracts/test/ClanWorld.t.sol:2060:        assertTrue(world.getWorldState().winterActive, "winter should be active");
packages/contracts/test/ClanWorld.t.sol:2061:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
packages/contracts/test/ClanWorld.t.sol:2064:    function test_winter_end_and_next_cycle() public {
packages/contracts/test/ClanWorld.t.sol:2065:        // Advance past first winter end tick (= 110)
packages/contracts/test/ClanWorld.t.sol:2066:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/test/ClanWorld.t.sol:2067:        for (uint64 i = 0; i <= winterEnd; i++) {
packages/contracts/test/ClanWorld.t.sol:2072:        assertFalse(ws.winterActive, "winter should be over");
packages/contracts/test/ClanWorld.t.sol:2073:        // next winter at [210, 220)
packages/contracts/test/ClanWorld.t.sol:2075:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2076:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2090:        // winter reset for new season
packages/contracts/test/ClanWorld.t.sol:2092:            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/test/ClanWorld.t.sol:2093:        assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr198-codereview-codex-5-4.md:322:               "name": "winterActive",
docs/reviews/pr198-codereview-codex-5-4.md:747:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr198-codereview-codex-5-4.md:749:-        _world.winterStartsAtTick =
docs/reviews/pr198-codereview-codex-5-4.md:750:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:751:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:752:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr198-codereview-codex-5-4.md:753:         _world.winterActive = false;
docs/reviews/pr198-codereview-codex-5-4.md:756:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:3197:-            ws.winterStartsAtTick,
docs/reviews/pr198-codereview-codex-5-4.md:3198:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr198-codereview-codex-5-4.md:3199:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr198-codereview-codex-5-4.md:3201:         assertFalse(ws.winterActive);
docs/reviews/pr198-codereview-codex-5-4.md:3204:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr198-codereview-codex-5-4.md:3205:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr198-codereview-codex-5-4.md:3207:-        uint64 winterStart =
docs/reviews/pr198-codereview-codex-5-4.md:3208:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:3209:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:3210:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr198-codereview-codex-5-4.md:3853:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr198-codereview-codex-5-4.md:3943:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr198-codereview-codex-5-4.md:3945:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-4.md:3946:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr198-codereview-codex-5-4.md:3947:        _world.winterActive = false;
docs/reviews/pr198-codereview-codex-5-4.md:4144:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/pr198-codereview-codex-5-4.md:4317:    bool winterActive;
docs/reviews/pr198-codereview-codex-5-4.md:4318:    uint64 winterStartsAtTick;
docs/reviews/pr198-codereview-codex-5-4.md:4319:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr198-codereview-codex-5-4.md:4356:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr198-codereview-codex-5-4.md:4358:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr198-codereview-codex-5-4.md:4651:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:5846:    29	    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
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
docs/reviews/pr198-codereview-codex-5-4.md:8029:docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/reviews/pr198-codereview-codex-5-4.md:8055:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-4.md:8157:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr198-codereview-codex-5-4.md:8772:   876	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr198-codereview-codex-5-4.md:8797:   901	        // Step 4: Resolve world events (season boundary, winter transitions).
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
docs/reviews/pr198-codereview-codex-5-4.md:9407:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/reviews/pr198-codereview-codex-5-4.md:9414:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:9415:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:9421:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr198-codereview-codex-5-4.md:9423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr198-codereview-codex-5-4.md:9436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr198-codereview-codex-5-4.md:9437:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/reviews/pr198-codereview-codex-5-4.md:9445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr198-codereview-codex-5-4.md:9450:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/reviews/pr198-codereview-codex-5-4.md:10322:- winter burn
docs/reviews/pr198-codereview-codex-5-4.md:10402:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/reviews/pr198-codereview-codex-5-4.md:10403:- that defending worker contributes 0 defense until Clan B starvation ends
docs/reviews/pr198-codereview-codex-5-4.md:10444:   309	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr198-codereview-codex-5-4.md:10450:   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr198-codereview-codex-5-4.md:10728:  2297	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr198-codereview-codex-5-4.md:10732:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-4.md:10823:  2392	            winterActive: _world.winterActive,
docs/reviews/pr198-codereview-codex-5-4.md:10824:  2393	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr198-codereview-codex-5-4.md:10825:  2394	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr198-codereview-codex-5-4.md:10832:  2401	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr198-codereview-codex-5-4.md:10837:  2406	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reference/architecture-decisions.md:133:## 10. KeeperHub absent in Submission 1
docs/planning/phase-3-test-spec.md:105:- Clan B has starvation developing — unsettled penalty.
docs/planning/phase-3-test-spec.md:173:**Setup:** Register worker as defender. Set worker HP to 0 (or advance starvation until death).
docs/planning/phase-3-test-spec.md:175:**Action:** Trigger worker death (via heartbeat starvation or forced state).
docs/planning/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/planning/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/planning/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/planning/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/planning/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:17:Senior staff engineer FINAL pre-merge review for PR #250 (Phase 10 — Winter + Elimination) at head e4a0d4c.
docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-4.md:22:1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
docs/reviews/pr250-codereview-codex-5-4.md:23:2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
docs/reviews/pr250-codereview-codex-5-4.md:24:3. RNG hygiene (cold_damage domain, deterministic clansman death pick)
docs/reviews/pr250-codereview-codex-5-4.md:25:4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
docs/reviews/pr250-codereview-codex-5-4.md:26:5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
docs/reviews/pr250-codereview-codex-5-4.md:27:6. Test coverage on winter boundary edges + clan-death paths
docs/reviews/pr250-codereview-codex-5-4.md:243:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:463:+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
docs/reviews/pr250-codereview-codex-5-4.md:472:-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:474:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:475:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:476:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:477:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:478:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:479:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:502:-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:503:+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
docs/reviews/pr250-codereview-codex-5-4.md:505:+        bool winter = _isWinterActiveAt(tick);
docs/reviews/pr250-codereview-codex-5-4.md:506:+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
docs/reviews/pr250-codereview-codex-5-4.md:514:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:522:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:526:+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
docs/reviews/pr250-codereview-codex-5-4.md:527:+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:532:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:615:+                _markClanDead(clan.clanId, "starvation", tick);
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
docs/reviews/pr250-codereview-codex-5-4.md:784:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-4.md:787:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:791:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:796:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-4.md:797:+            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:798:+            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:802:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:804:+        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:812:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-codereview-codex-5-4.md:916:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:917:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:918:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:927:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:928:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:929:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:943:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:944:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
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
docs/reviews/pr250-codereview-codex-5-4.md:1018:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-4.md:1021:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1025:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:1026:+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:1031:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:1036:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-4.md:1037:+            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1038:+            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1042:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1044:+        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1052:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:1063:-    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr250-codereview-codex-5-4.md:1064:+    uint64 internal constant WINTER_START_TICK = 110;
docs/reviews/pr250-codereview-codex-5-4.md:1131:+    /// @notice True iff currentTick is inside the recurring winter window.
docs/reviews/pr250-codereview-codex-5-4.md:1145:+    uint256 internal constant DOMAIN_COLD_DAMAGE = uint256(keccak256("cold_damage"));
docs/reviews/pr250-codereview-codex-5-4.md:1282:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1283:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:1285:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:1286:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1287:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:1293:+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:1294:+        assertEq(snapshot.winterStartsAtTick, 110);
docs/reviews/pr250-codereview-codex-5-4.md:1297:     function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:1298:-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:1299:-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:1301:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:1302:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
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
docs/reviews/pr250-codereview-codex-5-4.md:1362:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:1364:+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
docs/reviews/pr250-codereview-codex-5-4.md:1366:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1369:+    function test_winter_restarts_after_full_period() public {
docs/reviews/pr250-codereview-codex-5-4.md:1371:+        uint64 nextWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS;
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
docs/reviews/pr250-codereview-codex-5-4.md:1476:+        assertEq(clan.coldDamage, 0, "sufficient wood should avoid cold damage");
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
docs/reviews/pr250-codereview-codex-5-4.md:1514:+        assertEq(clan.coldDamage, 2, "cold damage should increment");
docs/reviews/pr250-codereview-codex-5-4.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr250-codereview-codex-5-4.md:1523:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1524:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1526:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:1533:+        assertEq(clan.coldDamage, 2, "cold damage should hit death threshold");
docs/reviews/pr250-codereview-codex-5-4.md:1548:+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
docs/reviews/pr250-codereview-codex-5-4.md:1552:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1555:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:1556:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1564:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
docs/reviews/pr250-codereview-codex-5-4.md:1570:+        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
docs/reviews/pr250-codereview-codex-5-4.md:1571:+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
docs/reviews/pr250-codereview-codex-5-4.md:1578:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1580:+        _advanceToTick(winterStart + 7);
docs/reviews/pr250-codereview-codex-5-4.md:1582:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:1596:+        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
docs/reviews/pr250-codereview-codex-5-4.md:1597:+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
docs/reviews/pr250-codereview-codex-5-4.md:1605:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1606:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:1607:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1619:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:1622:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:1623:+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:1637:+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
docs/reviews/pr250-codereview-codex-5-4.md:1641:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1642:+        _advanceToTick(winterEnd + 1);
docs/reviews/pr250-codereview-codex-5-4.md:1644:+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
docs/reviews/pr250-codereview-codex-5-4.md:1649:+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:1650:+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:1651:+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
docs/reviews/pr250-codereview-codex-5-4.md:1652:+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
docs/reviews/pr250-codereview-codex-5-4.md:1660:-        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:1662:-            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:1663:+        // winter is derived from the global recurring schedule
docs/reviews/pr250-codereview-codex-5-4.md:1664:+        uint64 expectedWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS * 3;
docs/reviews/pr250-codereview-codex-5-4.md:1665:         assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-4.md:1677:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:1678:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:1680:-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-4.md:1681:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:1682:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:1683:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:1690:Reviewing the Phase 10 diff with a code-review lens. I’m pulling the contract, tests, and the relevant spec/docs in parallel first, and I’ll split a couple of focused checks out to subagents so we can cover the boundary-tick and cross-phase risks thoroughly.
docs/reviews/pr250-codereview-codex-5-4.md:1691:  → Gather contract, test, and spec context around winter, starvation, elimination, and dead-clan rules
docs/reviews/pr250-codereview-codex-5-4.md:1725:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
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
docs/reviews/pr250-codereview-codex-5-4.md:2246:/bin/bash -lc "rg -n \"winter|starvation|elimination|dead-clan|ClanState.DEAD|§10|§4\\.12|§A2|WinterLocked|OTC\" docs packages -g '"'!packages/contracts/test/**'"'" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:2248:packages/runner/src/runnerCastHeartbeat.ts:48:          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2249:packages/runner/src/runnerCastHeartbeat.ts:49:          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2250:packages/runner/src/runnerCastHeartbeat.ts:50:          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2251:docs/reviews/pr199-codereview-codex-5-4.md:177:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2252:docs/reviews/pr199-codereview-codex-5-4.md:2551:   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2253:docs/reviews/pr199-codereview-codex-5-4.md:2573:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2254:docs/reviews/pr199-codereview-codex-5-4.md:2574:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2255:docs/reviews/pr199-codereview-codex-5-4.md:2576:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2256:docs/reviews/pr199-codereview-codex-5-4.md:2577:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2257:docs/reviews/pr199-codereview-codex-5-4.md:2584:   476	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2258:docs/reviews/pr199-codereview-codex-5-4.md:3017:   909	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2259:docs/reviews/pr199-codereview-codex-5-4.md:3879:  2221	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:2260:docs/reviews/pr199-codereview-codex-5-4.md:3883:  2225	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2261:docs/reviews/pr199-codereview-codex-5-4.md:4072:  2414	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2262:docs/reviews/pr199-codereview-codex-5-4.md:4073:  2415	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2263:docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:2264:docs/reviews/pr199-codereview-codex-5-4.md:4280:   203	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:2265:docs/reviews/pr199-codereview-codex-5-4.md:4281:   204	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2266:docs/reviews/pr199-codereview-codex-5-4.md:4282:   205	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:2267:docs/reviews/pr199-codereview-codex-5-4.md:4396:   618	    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:2270:docs/reviews/pr199-codereview-codex-5-4.md:4563:   785	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:2271:docs/reviews/pr199-codereview-codex-5-4.md:4613:   934	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2273:docs/reviews/pr199-codereview-codex-5-4.md:4658:   979	            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:2274:docs/reviews/pr199-codereview-codex-5-4.md:4659:   980	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2275:docs/reviews/pr199-codereview-codex-5-4.md:4660:   981	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2276:docs/reviews/pr199-codereview-codex-5-4.md:4662:   983	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2277:docs/reviews/pr199-codereview-codex-5-4.md:4665:   986	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:2278:docs/reviews/pr199-codereview-codex-5-4.md:4667:   988	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:2279:docs/reviews/pr199-codereview-codex-5-4.md:4668:   989	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:2280:docs/reviews/pr199-codereview-codex-5-4.md:4670:   991	            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:2281:docs/reviews/pr199-codereview-codex-5-4.md:4673:   994	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2282:docs/reviews/pr199-codereview-codex-5-4.md:4674:   995	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2283:docs/reviews/pr199-codereview-codex-5-4.md:4676:   997	            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:2284:docs/reviews/pr199-codereview-codex-5-4.md:4677:   998	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2285:docs/reviews/pr199-codereview-codex-5-4.md:4679:  1000	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2286:docs/reviews/pr199-codereview-codex-5-4.md:4681:  1002	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2287:docs/reviews/pr199-codereview-codex-5-4.md:4682:  1003	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2288:docs/reviews/pr199-codereview-codex-5-4.md:4684:  1005	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:2289:docs/reviews/pr199-codereview-codex-5-4.md:4685:  1006	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2290:docs/reviews/pr199-codereview-codex-5-4.md:4686:  1007	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2291:docs/reviews/pr199-codereview-codex-5-4.md:4745:  1066	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2297:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-4.md:2298:docs/reviews/pr250-codereview-codex-5-4.md:22:1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
docs/reviews/pr250-codereview-codex-5-4.md:2299:docs/reviews/pr250-codereview-codex-5-4.md:23:2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
docs/reviews/pr250-codereview-codex-5-4.md:2300:docs/reviews/pr250-codereview-codex-5-4.md:25:4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
docs/reviews/pr250-codereview-codex-5-4.md:2301:docs/reviews/pr250-codereview-codex-5-4.md:26:5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
docs/reviews/pr250-codereview-codex-5-4.md:2302:docs/reviews/pr250-codereview-codex-5-4.md:27:6. Test coverage on winter boundary edges + clan-death paths
docs/reviews/pr250-codereview-codex-5-4.md:2303:docs/reviews/pr250-codereview-codex-5-4.md:243:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:2304:docs/reviews/pr250-codereview-codex-5-4.md:463:+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
docs/reviews/pr250-codereview-codex-5-4.md:2305:docs/reviews/pr250-codereview-codex-5-4.md:472:-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2306:docs/reviews/pr250-codereview-codex-5-4.md:474:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2307:docs/reviews/pr250-codereview-codex-5-4.md:476:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2308:docs/reviews/pr250-codereview-codex-5-4.md:477:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2309:docs/reviews/pr250-codereview-codex-5-4.md:478:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2310:docs/reviews/pr250-codereview-codex-5-4.md:479:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2312:docs/reviews/pr250-codereview-codex-5-4.md:502:-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2313:docs/reviews/pr250-codereview-codex-5-4.md:503:+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
docs/reviews/pr250-codereview-codex-5-4.md:2314:docs/reviews/pr250-codereview-codex-5-4.md:505:+        bool winter = _isWinterActiveAt(tick);
docs/reviews/pr250-codereview-codex-5-4.md:2315:docs/reviews/pr250-codereview-codex-5-4.md:506:+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
docs/reviews/pr250-codereview-codex-5-4.md:2316:docs/reviews/pr250-codereview-codex-5-4.md:514:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:2317:docs/reviews/pr250-codereview-codex-5-4.md:522:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2318:docs/reviews/pr250-codereview-codex-5-4.md:526:+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
docs/reviews/pr250-codereview-codex-5-4.md:2319:docs/reviews/pr250-codereview-codex-5-4.md:527:+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2321:docs/reviews/pr250-codereview-codex-5-4.md:532:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:2322:docs/reviews/pr250-codereview-codex-5-4.md:615:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:2325:docs/reviews/pr250-codereview-codex-5-4.md:660:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2326:docs/reviews/pr250-codereview-codex-5-4.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-4.md:2327:docs/reviews/pr250-codereview-codex-5-4.md:696:-            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:2328:docs/reviews/pr250-codereview-codex-5-4.md:697:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2329:docs/reviews/pr250-codereview-codex-5-4.md:698:-            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2330:docs/reviews/pr250-codereview-codex-5-4.md:700:-            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2331:docs/reviews/pr250-codereview-codex-5-4.md:703:         // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:2332:docs/reviews/pr250-codereview-codex-5-4.md:705:-            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:2333:docs/reviews/pr250-codereview-codex-5-4.md:706:-                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:2334:docs/reviews/pr250-codereview-codex-5-4.md:708:-            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:2335:docs/reviews/pr250-codereview-codex-5-4.md:711:-        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2336:docs/reviews/pr250-codereview-codex-5-4.md:712:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2337:docs/reviews/pr250-codereview-codex-5-4.md:714:-            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:2338:docs/reviews/pr250-codereview-codex-5-4.md:715:-            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2339:docs/reviews/pr250-codereview-codex-5-4.md:717:-            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2340:docs/reviews/pr250-codereview-codex-5-4.md:719:-                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2341:docs/reviews/pr250-codereview-codex-5-4.md:720:-                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2342:docs/reviews/pr250-codereview-codex-5-4.md:722:-                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:2343:docs/reviews/pr250-codereview-codex-5-4.md:723:-                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2344:docs/reviews/pr250-codereview-codex-5-4.md:724:-                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2345:docs/reviews/pr250-codereview-codex-5-4.md:729:+            emit WinterStarted(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-4.md:2346:docs/reviews/pr250-codereview-codex-5-4.md:734:+            emit WinterEnded(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-4.md:2347:docs/reviews/pr250-codereview-codex-5-4.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/pr250-codereview-codex-5-4.md:2348:docs/reviews/pr250-codereview-codex-5-4.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-4.md:2349:docs/reviews/pr250-codereview-codex-5-4.md:773:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:2350:docs/reviews/pr250-codereview-codex-5-4.md:778:+        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:2351:docs/reviews/pr250-codereview-codex-5-4.md:791:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:2352:docs/reviews/pr250-codereview-codex-5-4.md:812:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:2353:docs/reviews/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-codereview-codex-5-4.md:2355:docs/reviews/pr250-codereview-codex-5-4.md:916:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2356:docs/reviews/pr250-codereview-codex-5-4.md:917:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2357:docs/reviews/pr250-codereview-codex-5-4.md:918:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2358:docs/reviews/pr250-codereview-codex-5-4.md:927:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2359:docs/reviews/pr250-codereview-codex-5-4.md:928:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2360:docs/reviews/pr250-codereview-codex-5-4.md:929:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2361:docs/reviews/pr250-codereview-codex-5-4.md:943:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2362:docs/reviews/pr250-codereview-codex-5-4.md:945:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2363:docs/reviews/pr250-codereview-codex-5-4.md:946:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2364:docs/reviews/pr250-codereview-codex-5-4.md:947:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2365:docs/reviews/pr250-codereview-codex-5-4.md:948:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2366:docs/reviews/pr250-codereview-codex-5-4.md:958:+            emit WinterStarted(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-4.md:2367:docs/reviews/pr250-codereview-codex-5-4.md:961:+            emit WinterEnded(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-4.md:2368:docs/reviews/pr250-codereview-codex-5-4.md:1001:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2369:docs/reviews/pr250-codereview-codex-5-4.md:1002:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2370:docs/reviews/pr250-codereview-codex-5-4.md:1003:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2371:docs/reviews/pr250-codereview-codex-5-4.md:1006:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2372:docs/reviews/pr250-codereview-codex-5-4.md:1007:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2373:docs/reviews/pr250-codereview-codex-5-4.md:1008:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2374:docs/reviews/pr250-codereview-codex-5-4.md:1025:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:2375:docs/reviews/pr250-codereview-codex-5-4.md:1026:+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:2376:docs/reviews/pr250-codereview-codex-5-4.md:1031:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:2377:docs/reviews/pr250-codereview-codex-5-4.md:1052:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:2378:docs/reviews/pr250-codereview-codex-5-4.md:1131:+    /// @notice True iff currentTick is inside the recurring winter window.
docs/reviews/pr250-codereview-codex-5-4.md:2379:docs/reviews/pr250-codereview-codex-5-4.md:1282:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2380:docs/reviews/pr250-codereview-codex-5-4.md:1285:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:2381:docs/reviews/pr250-codereview-codex-5-4.md:1286:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:2382:docs/reviews/pr250-codereview-codex-5-4.md:1287:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2383:docs/reviews/pr250-codereview-codex-5-4.md:1293:+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:2384:docs/reviews/pr250-codereview-codex-5-4.md:1294:+        assertEq(snapshot.winterStartsAtTick, 110);
docs/reviews/pr250-codereview-codex-5-4.md:2385:docs/reviews/pr250-codereview-codex-5-4.md:1297:     function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:2386:docs/reviews/pr250-codereview-codex-5-4.md:1298:-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:2387:docs/reviews/pr250-codereview-codex-5-4.md:1299:-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:2388:docs/reviews/pr250-codereview-codex-5-4.md:1301:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2389:docs/reviews/pr250-codereview-codex-5-4.md:1303:-        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2390:docs/reviews/pr250-codereview-codex-5-4.md:1309:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2391:docs/reviews/pr250-codereview-codex-5-4.md:1310:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-4.md:2392:docs/reviews/pr250-codereview-codex-5-4.md:1318:-        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-4.md:2393:docs/reviews/pr250-codereview-codex-5-4.md:1321:-            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-4.md:2394:docs/reviews/pr250-codereview-codex-5-4.md:1327:-        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:2395:docs/reviews/pr250-codereview-codex-5-4.md:1328:-        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:2396:docs/reviews/pr250-codereview-codex-5-4.md:1330:+        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
docs/reviews/pr250-codereview-codex-5-4.md:2397:docs/reviews/pr250-codereview-codex-5-4.md:1334:+        assertTrue(world.isWinter(), "winter should be active past start tick");
docs/reviews/pr250-codereview-codex-5-4.md:2398:docs/reviews/pr250-codereview-codex-5-4.md:1335:+        assertTrue(ws.winterActive, "world state should report winter active");
docs/reviews/pr250-codereview-codex-5-4.md:2399:docs/reviews/pr250-codereview-codex-5-4.md:1336:+        assertEq(ws.winterStartsAtTick, winterStart);
docs/reviews/pr250-codereview-codex-5-4.md:2400:docs/reviews/pr250-codereview-codex-5-4.md:1337:+        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:2401:docs/reviews/pr250-codereview-codex-5-4.md:1340:     function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-4.md:2402:docs/reviews/pr250-codereview-codex-5-4.md:1341:-        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-4.md:2403:docs/reviews/pr250-codereview-codex-5-4.md:1342:-        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2404:docs/reviews/pr250-codereview-codex-5-4.md:1343:-        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2405:docs/reviews/pr250-codereview-codex-5-4.md:1348:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2406:docs/reviews/pr250-codereview-codex-5-4.md:1349:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-4.md:2407:docs/reviews/pr250-codereview-codex-5-4.md:1358:         assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-4.md:2408:docs/reviews/pr250-codereview-codex-5-4.md:1359:-        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-4.md:2409:docs/reviews/pr250-codereview-codex-5-4.md:1361:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2410:docs/reviews/pr250-codereview-codex-5-4.md:1364:+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
docs/reviews/pr250-codereview-codex-5-4.md:2411:docs/reviews/pr250-codereview-codex-5-4.md:1366:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:2412:docs/reviews/pr250-codereview-codex-5-4.md:1369:+    function test_winter_restarts_after_full_period() public {
docs/reviews/pr250-codereview-codex-5-4.md:2413:docs/reviews/pr250-codereview-codex-5-4.md:1380:+        assertTrue(world.isWinter(), "winter should be active in next period");
docs/reviews/pr250-codereview-codex-5-4.md:2414:docs/reviews/pr250-codereview-codex-5-4.md:1384:+    function test_winter_cropTransitions_lockThenRestartRegrow() public {
docs/reviews/pr250-codereview-codex-5-4.md:2415:docs/reviews/pr250-codereview-codex-5-4.md:1392:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2416:docs/reviews/pr250-codereview-codex-5-4.md:1393:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-4.md:2417:docs/reviews/pr250-codereview-codex-5-4.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:2418:docs/reviews/pr250-codereview-codex-5-4.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:2419:docs/reviews/pr250-codereview-codex-5-4.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:2420:docs/reviews/pr250-codereview-codex-5-4.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:2421:docs/reviews/pr250-codereview-codex-5-4.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/pr250-codereview-codex-5-4.md:2422:docs/reviews/pr250-codereview-codex-5-4.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/pr250-codereview-codex-5-4.md:2423:docs/reviews/pr250-codereview-codex-5-4.md:1407:+        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
docs/reviews/pr250-codereview-codex-5-4.md:2424:docs/reviews/pr250-codereview-codex-5-4.md:1409:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2425:docs/reviews/pr250-codereview-codex-5-4.md:1410:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-4.md:2426:docs/reviews/pr250-codereview-codex-5-4.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2427:docs/reviews/pr250-codereview-codex-5-4.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/pr250-codereview-codex-5-4.md:2428:docs/reviews/pr250-codereview-codex-5-4.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/pr250-codereview-codex-5-4.md:2429:docs/reviews/pr250-codereview-codex-5-4.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-4.md:2430:docs/reviews/pr250-codereview-codex-5-4.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-4.md:2431:docs/reviews/pr250-codereview-codex-5-4.md:1439:+    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
docs/reviews/pr250-codereview-codex-5-4.md:2432:docs/reviews/pr250-codereview-codex-5-4.md:1441:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2433:docs/reviews/pr250-codereview-codex-5-4.md:1442:+        _advanceToTick(winterStart - 2);
docs/reviews/pr250-codereview-codex-5-4.md:2434:docs/reviews/pr250-codereview-codex-5-4.md:1446:+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
docs/reviews/pr250-codereview-codex-5-4.md:2435:docs/reviews/pr250-codereview-codex-5-4.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/pr250-codereview-codex-5-4.md:2436:docs/reviews/pr250-codereview-codex-5-4.md:1460:+    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
docs/reviews/pr250-codereview-codex-5-4.md:2437:docs/reviews/pr250-codereview-codex-5-4.md:1464:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2438:docs/reviews/pr250-codereview-codex-5-4.md:1465:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2439:docs/reviews/pr250-codereview-codex-5-4.md:1468:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2440:docs/reviews/pr250-codereview-codex-5-4.md:1473:+        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-4.md:2441:docs/reviews/pr250-codereview-codex-5-4.md:1474:+        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-4.md:2442:docs/reviews/pr250-codereview-codex-5-4.md:1475:+        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
docs/reviews/pr250-codereview-codex-5-4.md:2443:docs/reviews/pr250-codereview-codex-5-4.md:1481:+    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
docs/reviews/pr250-codereview-codex-5-4.md:2444:docs/reviews/pr250-codereview-codex-5-4.md:1485:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2445:docs/reviews/pr250-codereview-codex-5-4.md:1486:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2446:docs/reviews/pr250-codereview-codex-5-4.md:1488:+        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2447:docs/reviews/pr250-codereview-codex-5-4.md:1491:+        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
docs/reviews/pr250-codereview-codex-5-4.md:2448:docs/reviews/pr250-codereview-codex-5-4.md:1495:+        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
docs/reviews/pr250-codereview-codex-5-4.md:2449:docs/reviews/pr250-codereview-codex-5-4.md:1496:+        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
docs/reviews/pr250-codereview-codex-5-4.md:2450:docs/reviews/pr250-codereview-codex-5-4.md:1503:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2451:docs/reviews/pr250-codereview-codex-5-4.md:1504:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2452:docs/reviews/pr250-codereview-codex-5-4.md:1507:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:2453:docs/reviews/pr250-codereview-codex-5-4.md:1510:+        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
docs/reviews/pr250-codereview-codex-5-4.md:2454:docs/reviews/pr250-codereview-codex-5-4.md:1523:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2455:docs/reviews/pr250-codereview-codex-5-4.md:1524:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2456:docs/reviews/pr250-codereview-codex-5-4.md:1526:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:2457:docs/reviews/pr250-codereview-codex-5-4.md:1548:+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
docs/reviews/pr250-codereview-codex-5-4.md:2458:docs/reviews/pr250-codereview-codex-5-4.md:1552:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2459:docs/reviews/pr250-codereview-codex-5-4.md:1555:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:2460:docs/reviews/pr250-codereview-codex-5-4.md:1556:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2461:docs/reviews/pr250-codereview-codex-5-4.md:1564:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
docs/reviews/pr250-codereview-codex-5-4.md:2462:docs/reviews/pr250-codereview-codex-5-4.md:1571:+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
docs/reviews/pr250-codereview-codex-5-4.md:2463:docs/reviews/pr250-codereview-codex-5-4.md:1578:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2464:docs/reviews/pr250-codereview-codex-5-4.md:1580:+        _advanceToTick(winterStart + 7);
docs/reviews/pr250-codereview-codex-5-4.md:2465:docs/reviews/pr250-codereview-codex-5-4.md:1582:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:2467:docs/reviews/pr250-codereview-codex-5-4.md:1597:+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
docs/reviews/pr250-codereview-codex-5-4.md:2468:docs/reviews/pr250-codereview-codex-5-4.md:1605:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2469:docs/reviews/pr250-codereview-codex-5-4.md:1606:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:2470:docs/reviews/pr250-codereview-codex-5-4.md:1607:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2471:docs/reviews/pr250-codereview-codex-5-4.md:1619:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2472:docs/reviews/pr250-codereview-codex-5-4.md:1622:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:2473:docs/reviews/pr250-codereview-codex-5-4.md:1623:+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2475:docs/reviews/pr250-codereview-codex-5-4.md:1637:+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
docs/reviews/pr250-codereview-codex-5-4.md:2476:docs/reviews/pr250-codereview-codex-5-4.md:1641:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2477:docs/reviews/pr250-codereview-codex-5-4.md:1642:+        _advanceToTick(winterEnd + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2478:docs/reviews/pr250-codereview-codex-5-4.md:1644:+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
docs/reviews/pr250-codereview-codex-5-4.md:2479:docs/reviews/pr250-codereview-codex-5-4.md:1649:+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:2480:docs/reviews/pr250-codereview-codex-5-4.md:1650:+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:2481:docs/reviews/pr250-codereview-codex-5-4.md:1651:+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
docs/reviews/pr250-codereview-codex-5-4.md:2482:docs/reviews/pr250-codereview-codex-5-4.md:1652:+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
docs/reviews/pr250-codereview-codex-5-4.md:2483:docs/reviews/pr250-codereview-codex-5-4.md:1660:-        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:2484:docs/reviews/pr250-codereview-codex-5-4.md:1663:+        // winter is derived from the global recurring schedule
docs/reviews/pr250-codereview-codex-5-4.md:2485:docs/reviews/pr250-codereview-codex-5-4.md:1665:         assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-4.md:2486:docs/reviews/pr250-codereview-codex-5-4.md:1677:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2487:docs/reviews/pr250-codereview-codex-5-4.md:1680:-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-4.md:2488:docs/reviews/pr250-codereview-codex-5-4.md:1681:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:2489:docs/reviews/pr250-codereview-codex-5-4.md:1682:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:2490:docs/reviews/pr250-codereview-codex-5-4.md:1683:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2491:docs/reviews/pr250-codereview-codex-5-4.md:1691:  → Gather contract, test, and spec context around winter, starvation, elimination, and dead-clan rules
docs/reviews/pr250-codereview-codex-5-4.md:2493:docs/reviews/pr250-codereview-codex-5-4.md:1816:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:2494:docs/reviews/pr250-codereview-codex-5-4.md:1899:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:2495:docs/reviews/pr250-codereview-codex-5-4.md:1900:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2496:docs/reviews/pr250-codereview-codex-5-4.md:1901:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:2497:docs/reviews/pr250-codereview-codex-5-4.md:1938:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:2498:docs/reviews/pr250-codereview-codex-5-4.md:1940:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:2500:docs/reviews/pr250-codereview-codex-5-4.md:1961:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:2501:docs/reviews/pr250-codereview-codex-5-4.md:1962:packages/contracts/src/IClanWorld.sol:203:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:2502:docs/reviews/pr250-codereview-codex-5-4.md:1963:packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2503:docs/reviews/pr250-codereview-codex-5-4.md:1964:packages/contracts/src/IClanWorld.sol:205:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:2504:docs/reviews/pr250-codereview-codex-5-4.md:1965:packages/contracts/src/IClanWorld.sol:242:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:2505:docs/reviews/pr250-codereview-codex-5-4.md:1966:packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:2506:docs/reviews/pr250-codereview-codex-5-4.md:1967:packages/contracts/src/IClanWorld.sol:418:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:2507:docs/reviews/pr250-codereview-codex-5-4.md:1968:packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2508:docs/reviews/pr250-codereview-codex-5-4.md:1969:packages/contracts/src/IClanWorld.sol:420:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2509:docs/reviews/pr250-codereview-codex-5-4.md:1971:packages/contracts/src/IClanWorld.sol:618:    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:2510:docs/reviews/pr250-codereview-codex-5-4.md:1974:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:2511:docs/reviews/pr250-codereview-codex-5-4.md:1978:packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2512:docs/reviews/pr250-codereview-codex-5-4.md:1979:packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-4.md:2513:docs/reviews/pr250-codereview-codex-5-4.md:1980:packages/contracts/test/ClanWorldStub.t.sol:50:        assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2514:docs/reviews/pr250-codereview-codex-5-4.md:2013:packages/contracts/test/ClanWorld.t.sol:2020:    // Phase 4.4 — season + winter timer tests
docs/reviews/pr250-codereview-codex-5-4.md:2515:docs/reviews/pr250-codereview-codex-5-4.md:2015:packages/contracts/test/ClanWorld.t.sol:2029:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2516:docs/reviews/pr250-codereview-codex-5-4.md:2016:packages/contracts/test/ClanWorld.t.sol:2032:        assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2517:docs/reviews/pr250-codereview-codex-5-4.md:2017:packages/contracts/test/ClanWorld.t.sol:2035:    function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:2518:docs/reviews/pr250-codereview-codex-5-4.md:2018:packages/contracts/test/ClanWorld.t.sol:2036:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:2519:docs/reviews/pr250-codereview-codex-5-4.md:2019:packages/contracts/test/ClanWorld.t.sol:2037:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:2520:docs/reviews/pr250-codereview-codex-5-4.md:2020:packages/contracts/test/ClanWorld.t.sol:2039:        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2521:docs/reviews/pr250-codereview-codex-5-4.md:2021:packages/contracts/test/ClanWorld.t.sol:2041:        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2522:docs/reviews/pr250-codereview-codex-5-4.md:2022:packages/contracts/test/ClanWorld.t.sol:2051:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-4.md:2523:docs/reviews/pr250-codereview-codex-5-4.md:2023:packages/contracts/test/ClanWorld.t.sol:2054:            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-4.md:2524:docs/reviews/pr250-codereview-codex-5-4.md:2024:packages/contracts/test/ClanWorld.t.sol:2060:        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:2525:docs/reviews/pr250-codereview-codex-5-4.md:2025:packages/contracts/test/ClanWorld.t.sol:2061:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:2526:docs/reviews/pr250-codereview-codex-5-4.md:2026:packages/contracts/test/ClanWorld.t.sol:2064:    function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-4.md:2527:docs/reviews/pr250-codereview-codex-5-4.md:2027:packages/contracts/test/ClanWorld.t.sol:2065:        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-4.md:2528:docs/reviews/pr250-codereview-codex-5-4.md:2028:packages/contracts/test/ClanWorld.t.sol:2066:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2529:docs/reviews/pr250-codereview-codex-5-4.md:2029:packages/contracts/test/ClanWorld.t.sol:2067:        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2530:docs/reviews/pr250-codereview-codex-5-4.md:2031:packages/contracts/test/ClanWorld.t.sol:2072:        assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-4.md:2531:docs/reviews/pr250-codereview-codex-5-4.md:2032:packages/contracts/test/ClanWorld.t.sol:2073:        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-4.md:2532:docs/reviews/pr250-codereview-codex-5-4.md:2033:packages/contracts/test/ClanWorld.t.sol:2075:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2533:docs/reviews/pr250-codereview-codex-5-4.md:2035:packages/contracts/test/ClanWorld.t.sol:2090:        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:2534:docs/reviews/pr250-codereview-codex-5-4.md:2036:packages/contracts/test/ClanWorld.t.sol:2093:        assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-4.md:2535:docs/reviews/pr250-codereview-codex-5-4.md:2039:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:2536:docs/reviews/pr250-codereview-codex-5-4.md:2040:packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2537:docs/reviews/pr250-codereview-codex-5-4.md:2041:packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2538:docs/reviews/pr250-codereview-codex-5-4.md:2042:packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2539:docs/reviews/pr250-codereview-codex-5-4.md:2043:packages/contracts/src/ClanWorld.sol:138:        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2540:docs/reviews/pr250-codereview-codex-5-4.md:2045:packages/contracts/src/ClanWorld.sol:443:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2541:docs/reviews/pr250-codereview-codex-5-4.md:2046:packages/contracts/src/ClanWorld.sol:465:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2542:docs/reviews/pr250-codereview-codex-5-4.md:2047:packages/contracts/src/ClanWorld.sol:466:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2543:docs/reviews/pr250-codereview-codex-5-4.md:2048:packages/contracts/src/ClanWorld.sol:468:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2544:docs/reviews/pr250-codereview-codex-5-4.md:2049:packages/contracts/src/ClanWorld.sol:469:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2545:docs/reviews/pr250-codereview-codex-5-4.md:2050:packages/contracts/src/ClanWorld.sol:476:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2546:docs/reviews/pr250-codereview-codex-5-4.md:2051:packages/contracts/src/ClanWorld.sol:909:    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2547:docs/reviews/pr250-codereview-codex-5-4.md:2052:packages/contracts/src/ClanWorld.sol:934:        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2549:docs/reviews/pr250-codereview-codex-5-4.md:2054:packages/contracts/src/ClanWorld.sol:979:            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:2550:docs/reviews/pr250-codereview-codex-5-4.md:2055:packages/contracts/src/ClanWorld.sol:980:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2551:docs/reviews/pr250-codereview-codex-5-4.md:2056:packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2552:docs/reviews/pr250-codereview-codex-5-4.md:2057:packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2553:docs/reviews/pr250-codereview-codex-5-4.md:2058:packages/contracts/src/ClanWorld.sol:986:        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:2554:docs/reviews/pr250-codereview-codex-5-4.md:2059:packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:2555:docs/reviews/pr250-codereview-codex-5-4.md:2060:packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:2556:docs/reviews/pr250-codereview-codex-5-4.md:2061:packages/contracts/src/ClanWorld.sol:991:            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:2557:docs/reviews/pr250-codereview-codex-5-4.md:2062:packages/contracts/src/ClanWorld.sol:994:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2558:docs/reviews/pr250-codereview-codex-5-4.md:2063:packages/contracts/src/ClanWorld.sol:995:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2559:docs/reviews/pr250-codereview-codex-5-4.md:2064:packages/contracts/src/ClanWorld.sol:997:            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:2560:docs/reviews/pr250-codereview-codex-5-4.md:2065:packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2561:docs/reviews/pr250-codereview-codex-5-4.md:2066:packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2562:docs/reviews/pr250-codereview-codex-5-4.md:2067:packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2563:docs/reviews/pr250-codereview-codex-5-4.md:2068:packages/contracts/src/ClanWorld.sol:1003:                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2564:docs/reviews/pr250-codereview-codex-5-4.md:2069:packages/contracts/src/ClanWorld.sol:1005:                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:2565:docs/reviews/pr250-codereview-codex-5-4.md:2070:packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2566:docs/reviews/pr250-codereview-codex-5-4.md:2071:packages/contracts/src/ClanWorld.sol:1007:                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2567:docs/reviews/pr250-codereview-codex-5-4.md:2073:packages/contracts/src/ClanWorld.sol:1066:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2568:docs/reviews/pr250-codereview-codex-5-4.md:2077:packages/contracts/src/ClanWorld.sol:2221:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:2569:docs/reviews/pr250-codereview-codex-5-4.md:2078:packages/contracts/src/ClanWorld.sol:2225:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2570:docs/reviews/pr250-codereview-codex-5-4.md:2079:packages/contracts/src/ClanWorld.sol:2414:            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2571:docs/reviews/pr250-codereview-codex-5-4.md:2080:packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2572:docs/reviews/pr250-codereview-codex-5-4.md:2081:packages/contracts/src/ClanWorld.sol:2416:            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2573:docs/reviews/pr250-codereview-codex-5-4.md:2082:packages/contracts/src/ClanWorld.sol:2423:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:2574:docs/reviews/pr250-codereview-codex-5-4.md:2083:packages/contracts/src/ClanWorld.sol:2428:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2575:packages/shared/src/mocks/clanWorldFixture.ts:10://   1. Aldric (clan 0) is one tick from starvation (vaultWheat 1e18, 2 living
docs/reviews/pr250-codereview-codex-5-4.md:2576:packages/shared/src/mocks/clanWorldFixture.ts:46:  /** Cached starvation flag per v4 §4.13 (lazy starvation tracking). */
docs/reviews/pr250-codereview-codex-5-4.md:2577:docs/reviews/pr194-r2-codereview-codex-5-5.md:23:3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
docs/reviews/pr250-codereview-codex-5-4.md:2578:docs/reviews/pr194-r2-codereview-codex-5-5.md:402:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:2579:docs/reviews/pr194-r2-codereview-codex-5-5.md:740:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2580:docs/reviews/pr194-r2-codereview-codex-5-5.md:742:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2581:docs/reviews/pr194-r2-codereview-codex-5-5.md:744:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:2582:docs/reviews/pr194-r2-codereview-codex-5-5.md:745:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2583:docs/reviews/pr194-r2-codereview-codex-5-5.md:746:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2585:docs/reviews/pr194-r2-codereview-codex-5-5.md:763:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2586:docs/reviews/pr194-r2-codereview-codex-5-5.md:767:+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:2587:docs/reviews/pr194-r2-codereview-codex-5-5.md:776:+        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2588:docs/reviews/pr194-r2-codereview-codex-5-5.md:778:+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2589:docs/reviews/pr194-r2-codereview-codex-5-5.md:780:+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr250-codereview-codex-5-4.md:2590:docs/reviews/pr194-r2-codereview-codex-5-5.md:781:+            && seasonOffset < winterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2591:docs/reviews/pr194-r2-codereview-codex-5-5.md:794:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:2594:docs/reviews/pr194-r2-codereview-codex-5-5.md:836:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2596:docs/reviews/pr194-r2-codereview-codex-5-5.md:1137:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2597:docs/reviews/pr194-r2-codereview-codex-5-5.md:1138:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2598:docs/reviews/pr194-r2-codereview-codex-5-5.md:1139:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2599:docs/reviews/pr194-r2-codereview-codex-5-5.md:1140:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2600:docs/reviews/pr194-r2-codereview-codex-5-5.md:1143:+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:2603:docs/reviews/pr194-r2-codereview-codex-5-5.md:1183:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2604:docs/reviews/pr194-r2-codereview-codex-5-5.md:1258:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2613:docs/reviews/pr194-r2-codereview-codex-5-5.md:2476:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2614:docs/reviews/pr194-r2-codereview-codex-5-5.md:2507:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2615:docs/reviews/pr194-r2-codereview-codex-5-5.md:2517:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2617:docs/reviews/pr194-r2-codereview-codex-5-5.md:2615:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:2618:docs/reviews/pr194-r2-codereview-codex-5-5.md:2619:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2619:docs/reviews/pr194-r2-codereview-codex-5-5.md:2691:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2620:docs/reviews/pr194-r2-codereview-codex-5-5.md:2723:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:2621:docs/reviews/pr194-r2-codereview-codex-5-5.md:2728:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2622:docs/reviews/pr194-r2-codereview-codex-5-5.md:3254:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2623:docs/reviews/pr194-r2-codereview-codex-5-5.md:3262:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2624:docs/reviews/pr194-r2-codereview-codex-5-5.md:3554:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2625:docs/reviews/pr194-r2-codereview-codex-5-5.md:3555:+        _advanceUntil(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:2626:docs/reviews/pr194-r2-codereview-codex-5-5.md:3574:+        uint64 deathFromTick = winterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2628:docs/reviews/pr194-r2-codereview-codex-5-5.md:3685:+    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr250-codereview-codex-5-4.md:2629:docs/reviews/pr194-r2-codereview-codex-5-5.md:3686:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2630:docs/reviews/pr194-r2-codereview-codex-5-5.md:3687:+        _advanceUntil(winterStart + 30);
docs/reviews/pr250-codereview-codex-5-4.md:2631:docs/reviews/pr194-r2-codereview-codex-5-5.md:3688:+        assertFalse(world.getWorldState().winterActive, "test settles after winter");
docs/reviews/pr250-codereview-codex-5-4.md:2632:docs/reviews/pr194-r2-codereview-codex-5-5.md:3691:+        world.setClanUpkeepState(clanId, winterStart, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2633:docs/reviews/pr194-r2-codereview-codex-5-5.md:3694:+        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
docs/reviews/pr250-codereview-codex-5-4.md:2634:docs/reviews/pr194-r2-codereview-codex-5-5.md:3700:+        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
docs/reviews/pr250-codereview-codex-5-4.md:2635:docs/reviews/pr194-r2-codereview-codex-5-5.md:3705:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2636:docs/reviews/pr194-r2-codereview-codex-5-5.md:3706:+        _advanceUntil(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2637:docs/reviews/pr194-r2-codereview-codex-5-5.md:3709:+        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2640:docs/reviews/pr194-r2-codereview-codex-5-5.md:4374:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr250-codereview-codex-5-4.md:2641:docs/reviews/pr194-r2-codereview-codex-5-5.md:4380:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:2642:docs/reviews/pr194-r2-codereview-codex-5-5.md:4442:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:2643:docs/reviews/pr194-r2-codereview-codex-5-5.md:4443:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:2644:docs/reviews/pr194-r2-codereview-codex-5-5.md:4486:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2645:docs/reviews/pr194-r2-codereview-codex-5-5.md:4488:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:2646:docs/reviews/pr194-r2-codereview-codex-5-5.md:4490:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2647:docs/reviews/pr194-r2-codereview-codex-5-5.md:4493:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:2648:docs/reviews/pr194-r2-codereview-codex-5-5.md:4494:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:2649:docs/reviews/pr194-r2-codereview-codex-5-5.md:4496:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2650:docs/reviews/pr194-r2-codereview-codex-5-5.md:4498:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:2651:docs/reviews/pr194-r2-codereview-codex-5-5.md:4499:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2652:docs/reviews/pr194-r2-codereview-codex-5-5.md:4746:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2653:docs/reviews/pr194-r2-codereview-codex-5-5.md:4747:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2654:docs/reviews/pr194-r2-codereview-codex-5-5.md:4748:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2655:docs/reviews/pr194-r2-codereview-codex-5-5.md:4813:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2656:docs/reviews/pr194-r2-codereview-codex-5-5.md:4814:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2657:docs/reviews/pr194-r2-codereview-codex-5-5.md:4815:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2658:docs/reviews/pr194-r2-codereview-codex-5-5.md:4864:-                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2659:docs/reviews/pr194-r2-codereview-codex-5-5.md:5069:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2660:docs/reviews/pr194-r2-codereview-codex-5-5.md:5070:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2661:docs/reviews/pr194-r2-codereview-codex-5-5.md:5071:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2662:docs/reviews/pr194-r2-codereview-codex-5-5.md:5087:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2663:docs/reviews/pr194-r2-codereview-codex-5-5.md:5088:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2664:docs/reviews/pr194-r2-codereview-codex-5-5.md:5089:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2665:docs/reviews/pr194-r2-codereview-codex-5-5.md:5125:+        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:2666:docs/reviews/pr194-r2-codereview-codex-5-5.md:5126:+        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:2667:docs/reviews/pr194-r2-codereview-codex-5-5.md:5127:+        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:2668:docs/reviews/pr194-r2-codereview-codex-5-5.md:5141:+    expect(state.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:2669:docs/reviews/pr194-r2-codereview-codex-5-5.md:5144:+  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr250-codereview-codex-5-4.md:2670:docs/reviews/pr194-r2-codereview-codex-5-5.md:5153:+        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:2671:docs/reviews/pr194-r2-codereview-codex-5-5.md:5154:+        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:2672:docs/reviews/pr194-r2-codereview-codex-5-5.md:5155:+        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:2673:docs/reviews/pr194-r2-codereview-codex-5-5.md:5170:+    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:2674:docs/reviews/pr194-r2-codereview-codex-5-5.md:5398:packages/shared/test/clanWorldAbi.test.ts:97:  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr250-codereview-codex-5-4.md:2675:docs/reviews/pr194-r2-codereview-codex-5-5.md:5423:/bin/bash -lc 'rg -n "test_e2e_banditLifecycle|test_winterStarvationReplay|test_resolveBanditAttackReturns|test_defeatedBanditAwardsBlueprint|test_deadTargetCleanup|test_heartbeat_scheduledMarketObserves" packages/contracts/test' in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:2676:docs/reviews/pr194-r2-codereview-codex-5-5.md:5428:packages/contracts/test/BanditAttackResolution.t.sol:464:    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr250-codereview-codex-5-4.md:2678:docs/reviews/pr194-r2-codereview-codex-5-5.md:5478:   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2679:docs/reviews/pr194-r2-codereview-codex-5-5.md:5500:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2680:docs/reviews/pr194-r2-codereview-codex-5-5.md:5501:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2681:docs/reviews/pr194-r2-codereview-codex-5-5.md:5503:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2682:docs/reviews/pr194-r2-codereview-codex-5-5.md:5504:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2684:docs/reviews/pr194-r2-codereview-codex-5-5.md:5539:  1099	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2685:docs/reviews/pr194-r2-codereview-codex-5-5.md:5540:  1100	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2686:docs/reviews/pr194-r2-codereview-codex-5-5.md:5541:  1101	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2687:docs/reviews/pr194-r2-codereview-codex-5-5.md:5542:  1102	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2688:docs/reviews/pr194-r2-codereview-codex-5-5.md:5545:  1105	        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:2691:docs/reviews/pr194-r2-codereview-codex-5-5.md:5585:  1145	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2692:docs/reviews/pr194-r2-codereview-codex-5-5.md:5660:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2697:docs/reviews/pr194-r2-codereview-codex-5-5.md:6101:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2698:docs/reviews/pr194-r2-codereview-codex-5-5.md:6102:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2699:docs/reviews/pr194-r2-codereview-codex-5-5.md:6104:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2700:docs/reviews/pr194-r2-codereview-codex-5-5.md:6105:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2701:docs/reviews/pr194-r2-codereview-codex-5-5.md:6109:   473	        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:2702:docs/reviews/pr194-r2-codereview-codex-5-5.md:6118:   482	        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2703:docs/reviews/pr194-r2-codereview-codex-5-5.md:6120:   484	        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2704:docs/reviews/pr194-r2-codereview-codex-5-5.md:6122:   486	        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr250-codereview-codex-5-4.md:2705:docs/reviews/pr194-r2-codereview-codex-5-5.md:6123:   487	            && seasonOffset < winterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2706:docs/reviews/pr194-r2-codereview-codex-5-5.md:6136:   500	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:2708:docs/reviews/pr194-r2-codereview-codex-5-5.md:6221:  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:2709:docs/reviews/pr194-r2-codereview-codex-5-5.md:6374:  3472	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2710:docs/reviews/pr194-r2-codereview-codex-5-5.md:6375:  3473	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2711:docs/reviews/pr194-r2-codereview-codex-5-5.md:6376:  3474	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2712:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:2713:docs/reviews/pr194-r2-codereview-codex-5-5.md:6393:473:        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:2714:docs/reviews/pr194-r2-codereview-codex-5-5.md:6395:612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2715:docs/reviews/pr194-r2-codereview-codex-5-5.md:6404:1105:        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:2716:docs/reviews/pr194-r2-codereview-codex-5-5.md:6406:1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2719:docs/reviews/pr194-r2-codereview-codex-5-5.md:6413:3415:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2720:docs/reviews/pr194-r2-codereview-codex-5-5.md:6585:   202	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:2721:docs/reviews/pr194-r2-codereview-codex-5-5.md:6586:   203	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2722:docs/reviews/pr194-r2-codereview-codex-5-5.md:6587:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:2723:docs/reviews/pr194-r2-codereview-codex-5-5.md:6639:   414	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:2724:docs/reviews/pr194-r2-codereview-codex-5-5.md:6640:   415	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2725:docs/reviews/pr194-r2-codereview-codex-5-5.md:6641:   416	    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2726:docs/reviews/pr194-r2-codereview-codex-5-5.md:6881:   138	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2727:docs/reviews/pr194-r2-codereview-codex-5-5.md:6883:   140	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:2728:docs/reviews/pr194-r2-codereview-codex-5-5.md:6884:   141	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2729:docs/reviews/pr194-r2-codereview-codex-5-5.md:6885:   142	        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2730:docs/reviews/pr194-r2-codereview-codex-5-5.md:6906:   612	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2732:docs/reviews/pr194-r2-codereview-codex-5-5.md:6949:  2381	            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:2733:docs/reviews/pr194-r2-codereview-codex-5-5.md:6950:  2382	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2734:docs/reviews/pr194-r2-codereview-codex-5-5.md:6951:  2383	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2735:docs/reviews/pr194-r2-codereview-codex-5-5.md:6953:  2385	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2736:docs/reviews/pr194-r2-codereview-codex-5-5.md:6956:  2388	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:2737:docs/reviews/pr194-r2-codereview-codex-5-5.md:6958:  2390	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:2738:docs/reviews/pr194-r2-codereview-codex-5-5.md:6959:  2391	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:2739:docs/reviews/pr194-r2-codereview-codex-5-5.md:6961:  2393	            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:2740:docs/reviews/pr194-r2-codereview-codex-5-5.md:6964:  2396	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2741:docs/reviews/pr194-r2-codereview-codex-5-5.md:6965:  2397	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2742:docs/reviews/pr194-r2-codereview-codex-5-5.md:6967:  2399	            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:2743:docs/reviews/pr194-r2-codereview-codex-5-5.md:6968:  2400	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2744:docs/reviews/pr194-r2-codereview-codex-5-5.md:6970:  2402	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2745:docs/reviews/pr194-r2-codereview-codex-5-5.md:6972:  2404	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2746:docs/reviews/pr194-r2-codereview-codex-5-5.md:6973:  2405	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2747:docs/reviews/pr194-r2-codereview-codex-5-5.md:6975:  2407	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:2748:docs/reviews/pr194-r2-codereview-codex-5-5.md:6976:  2408	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2749:docs/reviews/pr194-r2-codereview-codex-5-5.md:6977:  2409	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2750:docs/reviews/pr194-r2-codereview-codex-5-5.md:7197:    22	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2751:docs/reviews/pr194-r2-codereview-codex-5-5.md:7198:    23	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2752:docs/reviews/pr194-r2-codereview-codex-5-5.md:7199:    24	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2753:docs/reviews/pr194-r2-codereview-codex-5-5.md:7215:    40	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2754:docs/reviews/pr194-r2-codereview-codex-5-5.md:7216:    41	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2755:docs/reviews/pr194-r2-codereview-codex-5-5.md:7217:    42	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2756:docs/reviews/pr194-r2-codereview-codex-5-5.md:7253:    78	        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:2757:docs/reviews/pr194-r2-codereview-codex-5-5.md:7254:    79	        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:2758:docs/reviews/pr194-r2-codereview-codex-5-5.md:7255:    80	        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:2759:docs/reviews/pr194-r2-codereview-codex-5-5.md:7269:    94	    expect(state.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:2760:docs/reviews/pr194-r2-codereview-codex-5-5.md:7272:    97	  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr250-codereview-codex-5-4.md:2761:docs/reviews/pr194-r2-codereview-codex-5-5.md:7281:   106	        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:2762:docs/reviews/pr194-r2-codereview-codex-5-5.md:7282:   107	        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:2763:docs/reviews/pr194-r2-codereview-codex-5-5.md:7283:   108	        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:2764:docs/reviews/pr194-r2-codereview-codex-5-5.md:7298:   123	    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:2765:docs/reviews/pr194-r2-codereview-codex-5-5.md:7561:getWorldState ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","nextHeartbeatAtTs:uint64","nextBanditSpawnEligibleTick:uint64","currentBanditSpawnChanceBps:uint16","currentTickSeed:bytes32","activeBanditId:uint32","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","nextCommitSequence:uint64"]
docs/reviews/pr250-codereview-codex-5-4.md:2766:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr250-codereview-codex-5-4.md:2767:docs/reviews/pr194-r2-codereview-codex-5-5.md:7580:packages/contracts/src/ClanWorld.sol:138:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2768:docs/reviews/pr194-r2-codereview-codex-5-5.md:7581:packages/contracts/src/ClanWorld.sol:140:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:2769:docs/reviews/pr194-r2-codereview-codex-5-5.md:7582:packages/contracts/src/ClanWorld.sol:141:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2770:docs/reviews/pr194-r2-codereview-codex-5-5.md:7588:packages/contracts/src/ClanWorld.sol:484:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2771:docs/reviews/pr194-r2-codereview-codex-5-5.md:7589:packages/contracts/src/ClanWorld.sol:486:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr250-codereview-codex-5-4.md:2772:docs/reviews/pr194-r2-codereview-codex-5-5.md:7610:packages/contracts/src/ClanWorld.sol:2383:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2773:docs/reviews/pr194-r2-codereview-codex-5-5.md:7612:packages/contracts/src/ClanWorld.sol:2385:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2774:docs/reviews/pr194-r2-codereview-codex-5-5.md:7613:packages/contracts/src/ClanWorld.sol:2400:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2775:docs/reviews/pr194-r2-codereview-codex-5-5.md:7615:packages/contracts/src/ClanWorld.sol:2402:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2776:docs/reviews/pr194-r2-codereview-codex-5-5.md:7698:   333	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2777:docs/reviews/pr194-r2-codereview-codex-5-5.md:7699:   334	        _advanceUntil(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:2778:docs/reviews/pr194-r2-codereview-codex-5-5.md:7718:   353	        uint64 deathFromTick = winterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2780:docs/reviews/pr194-r2-codereview-codex-5-5.md:7829:   464	    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr250-codereview-codex-5-4.md:2781:docs/reviews/pr194-r2-codereview-codex-5-5.md:7830:   465	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2782:docs/reviews/pr194-r2-codereview-codex-5-5.md:7831:   466	        _advanceUntil(winterStart + 30);
docs/reviews/pr250-codereview-codex-5-4.md:2783:docs/reviews/pr194-r2-codereview-codex-5-5.md:7832:   467	        assertFalse(world.getWorldState().winterActive, "test settles after winter");
docs/reviews/pr250-codereview-codex-5-4.md:2784:docs/reviews/pr194-r2-codereview-codex-5-5.md:7835:   470	        world.setClanUpkeepState(clanId, winterStart, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2785:docs/reviews/pr194-r2-codereview-codex-5-5.md:7838:   473	        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
docs/reviews/pr250-codereview-codex-5-4.md:2786:docs/reviews/pr194-r2-codereview-codex-5-5.md:7844:   479	        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
docs/reviews/pr250-codereview-codex-5-4.md:2787:docs/reviews/pr194-r2-codereview-codex-5-5.md:7849:   484	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:2788:docs/reviews/pr194-r2-codereview-codex-5-5.md:7850:   485	        _advanceUntil(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:2789:docs/reviews/pr194-r2-codereview-codex-5-5.md:7853:   488	        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr250-codereview-codex-5-4.md:2798:packages/shared/src/adapters/IChainClient.ts:39:          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:2799:packages/shared/src/adapters/IChainClient.ts:40:          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2800:packages/shared/src/adapters/IChainClient.ts:41:          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2801:packages/shared/src/adapters/IChainClient.ts:90:                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:2803:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:2804:docs/reviews/pr200-codereview-codex-5-5.md:741:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:2805:docs/reviews/pr200-codereview-codex-5-5.md:1718:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2806:docs/reviews/pr200-codereview-codex-5-5.md:1720:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2807:docs/reviews/pr200-codereview-codex-5-5.md:1722:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:2808:docs/reviews/pr200-codereview-codex-5-5.md:1723:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2809:docs/reviews/pr200-codereview-codex-5-5.md:1724:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2810:docs/reviews/pr200-codereview-codex-5-5.md:1733:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2829:docs/reviews/pr200-codereview-codex-5-5.md:2316:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2830:docs/reviews/pr200-codereview-codex-5-5.md:2318:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2831:docs/reviews/pr200-codereview-codex-5-5.md:2319:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2832:docs/reviews/pr200-codereview-codex-5-5.md:2320:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2836:docs/reviews/pr200-codereview-codex-5-5.md:3547:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2837:docs/reviews/pr200-codereview-codex-5-5.md:3549:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:2838:docs/reviews/pr200-codereview-codex-5-5.md:3551:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:2839:docs/reviews/pr200-codereview-codex-5-5.md:3554:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:2840:docs/reviews/pr200-codereview-codex-5-5.md:3555:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:2841:docs/reviews/pr200-codereview-codex-5-5.md:3557:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:2842:docs/reviews/pr200-codereview-codex-5-5.md:3559:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:2843:docs/reviews/pr200-codereview-codex-5-5.md:3560:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:2849:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:2850:docs/reviews/pr200-codereview-codex-5-5.md:5076:/bin/bash -lc 'rg -n "DEAD|clanState|starvation|dead|ClanState.DEAD|propose.*Transfer|accept.*Transfer|zero|expiry" packages/contracts/test packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:2851:docs/reviews/pr200-codereview-codex-5-5.md:5082:packages/contracts/src/ClanWorld.sol:397:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2852:docs/reviews/pr200-codereview-codex-5-5.md:5083:packages/contracts/src/ClanWorld.sol:419:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2853:docs/reviews/pr200-codereview-codex-5-5.md:5084:packages/contracts/src/ClanWorld.sol:420:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:2854:docs/reviews/pr200-codereview-codex-5-5.md:5085:packages/contracts/src/ClanWorld.sol:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:2855:docs/reviews/pr200-codereview-codex-5-5.md:5086:packages/contracts/src/ClanWorld.sol:423:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2856:docs/reviews/pr200-codereview-codex-5-5.md:5087:packages/contracts/src/ClanWorld.sol:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2858:docs/reviews/pr200-codereview-codex-5-5.md:5094:packages/contracts/src/ClanWorld.sol:1033:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2859:docs/reviews/pr200-codereview-codex-5-5.md:5100:packages/contracts/src/ClanWorld.sol:2297:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:2860:docs/reviews/pr200-codereview-codex-5-5.md:5101:packages/contracts/src/ClanWorld.sol:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2861:docs/reviews/pr200-codereview-codex-5-5.md:5103:packages/contracts/src/ClanWorld.sol:2401:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:2862:docs/reviews/pr200-codereview-codex-5-5.md:5104:packages/contracts/src/ClanWorld.sol:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:2871:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:2873:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:2879:docs/reviews/pr200-codereview-codex-5-5.md:5973:docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:2884:docs/reviews/pr200-codereview-codex-5-5.md:6004:docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:2887:docs/reviews/pr200-codereview-codex-5-5.md:6023:docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:2901:docs/reviews/pr200-codereview-codex-5-5.md:6161:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:2904:docs/reviews/pr200-codereview-codex-5-5.md:6278:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:2905:docs/reviews/pr200-codereview-codex-5-5.md:6281:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr250-codereview-codex-5-4.md:2906:docs/reviews/pr200-codereview-codex-5-5.md:6286:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:2909:docs/reviews/pr200-codereview-codex-5-5.md:6316:- winter transition checks
docs/reviews/pr250-codereview-codex-5-4.md:2911:docs/reviews/pr200-codereview-codex-5-5.md:6951:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2934:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-4.md:2935:docs/reviews/pr250-codereview-codex-5-5.md:22:1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
docs/reviews/pr250-codereview-codex-5-4.md:2936:docs/reviews/pr250-codereview-codex-5-5.md:23:2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
docs/reviews/pr250-codereview-codex-5-4.md:2937:docs/reviews/pr250-codereview-codex-5-5.md:25:4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
docs/reviews/pr250-codereview-codex-5-4.md:2938:docs/reviews/pr250-codereview-codex-5-5.md:26:5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
docs/reviews/pr250-codereview-codex-5-4.md:2939:docs/reviews/pr250-codereview-codex-5-5.md:27:6. Test coverage on winter boundary edges + clan-death paths
docs/reviews/pr250-codereview-codex-5-4.md:2940:docs/reviews/pr250-codereview-codex-5-5.md:243:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:2941:docs/reviews/pr250-codereview-codex-5-5.md:463:+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
docs/reviews/pr250-codereview-codex-5-4.md:2942:docs/reviews/pr250-codereview-codex-5-5.md:472:-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:2943:docs/reviews/pr250-codereview-codex-5-5.md:474:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2944:docs/reviews/pr250-codereview-codex-5-5.md:476:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:2945:docs/reviews/pr250-codereview-codex-5-5.md:477:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:2946:docs/reviews/pr250-codereview-codex-5-5.md:478:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:2947:docs/reviews/pr250-codereview-codex-5-5.md:479:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2949:docs/reviews/pr250-codereview-codex-5-5.md:502:-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:2950:docs/reviews/pr250-codereview-codex-5-5.md:503:+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
docs/reviews/pr250-codereview-codex-5-4.md:2951:docs/reviews/pr250-codereview-codex-5-5.md:505:+        bool winter = _isWinterActiveAt(tick);
docs/reviews/pr250-codereview-codex-5-4.md:2952:docs/reviews/pr250-codereview-codex-5-5.md:506:+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
docs/reviews/pr250-codereview-codex-5-4.md:2953:docs/reviews/pr250-codereview-codex-5-5.md:514:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:2954:docs/reviews/pr250-codereview-codex-5-5.md:522:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2955:docs/reviews/pr250-codereview-codex-5-5.md:526:+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
docs/reviews/pr250-codereview-codex-5-4.md:2956:docs/reviews/pr250-codereview-codex-5-5.md:527:+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2958:docs/reviews/pr250-codereview-codex-5-5.md:532:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-4.md:2959:docs/reviews/pr250-codereview-codex-5-5.md:615:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:2962:docs/reviews/pr250-codereview-codex-5-5.md:660:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:2963:docs/reviews/pr250-codereview-codex-5-5.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-4.md:2964:docs/reviews/pr250-codereview-codex-5-5.md:696:-            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:2965:docs/reviews/pr250-codereview-codex-5-5.md:697:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2966:docs/reviews/pr250-codereview-codex-5-5.md:698:-            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2967:docs/reviews/pr250-codereview-codex-5-5.md:700:-            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2968:docs/reviews/pr250-codereview-codex-5-5.md:703:         // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:2969:docs/reviews/pr250-codereview-codex-5-5.md:705:-            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:2970:docs/reviews/pr250-codereview-codex-5-5.md:706:-                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:2971:docs/reviews/pr250-codereview-codex-5-5.md:708:-            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:2972:docs/reviews/pr250-codereview-codex-5-5.md:711:-        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:2973:docs/reviews/pr250-codereview-codex-5-5.md:712:-            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:2974:docs/reviews/pr250-codereview-codex-5-5.md:714:-            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:2975:docs/reviews/pr250-codereview-codex-5-5.md:715:-            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:2976:docs/reviews/pr250-codereview-codex-5-5.md:717:-            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:2977:docs/reviews/pr250-codereview-codex-5-5.md:719:-                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:2978:docs/reviews/pr250-codereview-codex-5-5.md:720:-                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:2979:docs/reviews/pr250-codereview-codex-5-5.md:722:-                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:2980:docs/reviews/pr250-codereview-codex-5-5.md:723:-                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2981:docs/reviews/pr250-codereview-codex-5-5.md:724:-                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:2982:docs/reviews/pr250-codereview-codex-5-5.md:729:+            emit WinterStarted(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-4.md:2983:docs/reviews/pr250-codereview-codex-5-5.md:734:+            emit WinterEnded(_winterEventTick(newTick));
docs/reviews/pr250-codereview-codex-5-4.md:2984:docs/reviews/pr250-codereview-codex-5-5.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/pr250-codereview-codex-5-4.md:2985:docs/reviews/pr250-codereview-codex-5-5.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-codereview-codex-5-4.md:2986:docs/reviews/pr250-codereview-codex-5-5.md:773:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:2987:docs/reviews/pr250-codereview-codex-5-5.md:778:+        require(tick <= type(uint32).max, "ClanWorld: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:2988:docs/reviews/pr250-codereview-codex-5-5.md:791:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:2989:docs/reviews/pr250-codereview-codex-5-5.md:812:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:2990:docs/reviews/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-codereview-codex-5-4.md:2992:docs/reviews/pr250-codereview-codex-5-5.md:916:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2993:docs/reviews/pr250-codereview-codex-5-5.md:917:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2994:docs/reviews/pr250-codereview-codex-5-5.md:918:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2995:docs/reviews/pr250-codereview-codex-5-5.md:927:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:2996:docs/reviews/pr250-codereview-codex-5-5.md:928:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2997:docs/reviews/pr250-codereview-codex-5-5.md:929:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:2998:docs/reviews/pr250-codereview-codex-5-5.md:943:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:2999:docs/reviews/pr250-codereview-codex-5-5.md:945:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3000:docs/reviews/pr250-codereview-codex-5-5.md:946:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3001:docs/reviews/pr250-codereview-codex-5-5.md:947:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:3002:docs/reviews/pr250-codereview-codex-5-5.md:948:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3003:docs/reviews/pr250-codereview-codex-5-5.md:958:+            emit WinterStarted(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-4.md:3004:docs/reviews/pr250-codereview-codex-5-5.md:961:+            emit WinterEnded(_winterEventTick(_world.currentTick));
docs/reviews/pr250-codereview-codex-5-4.md:3005:docs/reviews/pr250-codereview-codex-5-5.md:1001:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3006:docs/reviews/pr250-codereview-codex-5-5.md:1002:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3007:docs/reviews/pr250-codereview-codex-5-5.md:1003:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3008:docs/reviews/pr250-codereview-codex-5-5.md:1006:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3009:docs/reviews/pr250-codereview-codex-5-5.md:1007:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3010:docs/reviews/pr250-codereview-codex-5-5.md:1008:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3011:docs/reviews/pr250-codereview-codex-5-5.md:1025:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-4.md:3012:docs/reviews/pr250-codereview-codex-5-5.md:1026:+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-4.md:3013:docs/reviews/pr250-codereview-codex-5-5.md:1031:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-4.md:3014:docs/reviews/pr250-codereview-codex-5-5.md:1052:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-4.md:3015:docs/reviews/pr250-codereview-codex-5-5.md:1131:+    /// @notice True iff currentTick is inside the recurring winter window.
docs/reviews/pr250-codereview-codex-5-4.md:3016:docs/reviews/pr250-codereview-codex-5-5.md:1282:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3017:docs/reviews/pr250-codereview-codex-5-5.md:1285:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:3018:docs/reviews/pr250-codereview-codex-5-5.md:1286:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:3019:docs/reviews/pr250-codereview-codex-5-5.md:1287:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:3020:docs/reviews/pr250-codereview-codex-5-5.md:1293:+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:3021:docs/reviews/pr250-codereview-codex-5-5.md:1294:+        assertEq(snapshot.winterStartsAtTick, 110);
docs/reviews/pr250-codereview-codex-5-4.md:3022:docs/reviews/pr250-codereview-codex-5-5.md:1297:     function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:3023:docs/reviews/pr250-codereview-codex-5-5.md:1298:-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:3024:docs/reviews/pr250-codereview-codex-5-5.md:1299:-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:3025:docs/reviews/pr250-codereview-codex-5-5.md:1301:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:3026:docs/reviews/pr250-codereview-codex-5-5.md:1303:-        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:3027:docs/reviews/pr250-codereview-codex-5-5.md:1309:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3028:docs/reviews/pr250-codereview-codex-5-5.md:1310:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-4.md:3029:docs/reviews/pr250-codereview-codex-5-5.md:1318:-        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-4.md:3030:docs/reviews/pr250-codereview-codex-5-5.md:1321:-            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-4.md:3031:docs/reviews/pr250-codereview-codex-5-5.md:1327:-        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:3032:docs/reviews/pr250-codereview-codex-5-5.md:1328:-        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:3033:docs/reviews/pr250-codereview-codex-5-5.md:1330:+        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be winter start");
docs/reviews/pr250-codereview-codex-5-4.md:3034:docs/reviews/pr250-codereview-codex-5-5.md:1334:+        assertTrue(world.isWinter(), "winter should be active past start tick");
docs/reviews/pr250-codereview-codex-5-4.md:3035:docs/reviews/pr250-codereview-codex-5-5.md:1335:+        assertTrue(ws.winterActive, "world state should report winter active");
docs/reviews/pr250-codereview-codex-5-4.md:3036:docs/reviews/pr250-codereview-codex-5-5.md:1336:+        assertEq(ws.winterStartsAtTick, winterStart);
docs/reviews/pr250-codereview-codex-5-4.md:3037:docs/reviews/pr250-codereview-codex-5-5.md:1337:+        assertEq(ws.winterEndsAtTick, winterStart + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:3038:docs/reviews/pr250-codereview-codex-5-5.md:1340:     function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-4.md:3039:docs/reviews/pr250-codereview-codex-5-5.md:1341:-        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-4.md:3040:docs/reviews/pr250-codereview-codex-5-5.md:1342:-        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3041:docs/reviews/pr250-codereview-codex-5-5.md:1343:-        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:3042:docs/reviews/pr250-codereview-codex-5-5.md:1348:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:3043:docs/reviews/pr250-codereview-codex-5-5.md:1349:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-4.md:3044:docs/reviews/pr250-codereview-codex-5-5.md:1358:         assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-4.md:3045:docs/reviews/pr250-codereview-codex-5-5.md:1359:-        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-4.md:3046:docs/reviews/pr250-codereview-codex-5-5.md:1361:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3047:docs/reviews/pr250-codereview-codex-5-5.md:1364:+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
docs/reviews/pr250-codereview-codex-5-4.md:3048:docs/reviews/pr250-codereview-codex-5-5.md:1366:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:3049:docs/reviews/pr250-codereview-codex-5-5.md:1369:+    function test_winter_restarts_after_full_period() public {
docs/reviews/pr250-codereview-codex-5-4.md:3050:docs/reviews/pr250-codereview-codex-5-5.md:1380:+        assertTrue(world.isWinter(), "winter should be active in next period");
docs/reviews/pr250-codereview-codex-5-4.md:3051:docs/reviews/pr250-codereview-codex-5-5.md:1384:+    function test_winter_cropTransitions_lockThenRestartRegrow() public {
docs/reviews/pr250-codereview-codex-5-4.md:3052:docs/reviews/pr250-codereview-codex-5-5.md:1392:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3053:docs/reviews/pr250-codereview-codex-5-5.md:1393:+        _advanceToTick(winterStart - 1);
docs/reviews/pr250-codereview-codex-5-4.md:3054:docs/reviews/pr250-codereview-codex-5-5.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:3055:docs/reviews/pr250-codereview-codex-5-5.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:3056:docs/reviews/pr250-codereview-codex-5-5.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:3057:docs/reviews/pr250-codereview-codex-5-5.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/pr250-codereview-codex-5-4.md:3058:docs/reviews/pr250-codereview-codex-5-5.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/pr250-codereview-codex-5-4.md:3059:docs/reviews/pr250-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/pr250-codereview-codex-5-4.md:3060:docs/reviews/pr250-codereview-codex-5-5.md:1407:+        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_WINTER_LOCKED), "harvest locked during winter");
docs/reviews/pr250-codereview-codex-5-4.md:3061:docs/reviews/pr250-codereview-codex-5-5.md:1409:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:3062:docs/reviews/pr250-codereview-codex-5-5.md:1410:+        _advanceToTick(winterEnd - 1);
docs/reviews/pr250-codereview-codex-5-4.md:3063:docs/reviews/pr250-codereview-codex-5-5.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:3064:docs/reviews/pr250-codereview-codex-5-5.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/pr250-codereview-codex-5-4.md:3065:docs/reviews/pr250-codereview-codex-5-5.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/pr250-codereview-codex-5-4.md:3066:docs/reviews/pr250-codereview-codex-5-5.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-4.md:3067:docs/reviews/pr250-codereview-codex-5-5.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/pr250-codereview-codex-5-4.md:3068:docs/reviews/pr250-codereview-codex-5-5.md:1439:+    function test_winterLockedPlotSettlesInFlightHarvestWithNoYield() public {
docs/reviews/pr250-codereview-codex-5-4.md:3069:docs/reviews/pr250-codereview-codex-5-5.md:1441:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3070:docs/reviews/pr250-codereview-codex-5-5.md:1442:+        _advanceToTick(winterStart - 2);
docs/reviews/pr250-codereview-codex-5-4.md:3071:docs/reviews/pr250-codereview-codex-5-5.md:1446:+        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "pre-winter harvest order should queue");
docs/reviews/pr250-codereview-codex-5-4.md:3072:docs/reviews/pr250-codereview-codex-5-5.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/pr250-codereview-codex-5-4.md:3073:docs/reviews/pr250-codereview-codex-5-5.md:1460:+    function test_winter_upkeep_doublesFoodAndBurnsWood() public {
docs/reviews/pr250-codereview-codex-5-4.md:3074:docs/reviews/pr250-codereview-codex-5-5.md:1464:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3075:docs/reviews/pr250-codereview-codex-5-5.md:1465:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:3076:docs/reviews/pr250-codereview-codex-5-5.md:1468:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3077:docs/reviews/pr250-codereview-codex-5-5.md:1473:+        assertEq(clan.vaultWheat, 92e18, "winter wheat upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-4.md:3078:docs/reviews/pr250-codereview-codex-5-5.md:1474:+        assertEq(clan.vaultFish, 100e18 - 8e17, "winter fish upkeep should be 2x");
docs/reviews/pr250-codereview-codex-5-4.md:3079:docs/reviews/pr250-codereview-codex-5-5.md:1475:+        assertEq(clan.vaultWood, 98e18, "winter wood burn should be per clansman");
docs/reviews/pr250-codereview-codex-5-4.md:3080:docs/reviews/pr250-codereview-codex-5-5.md:1481:+    function test_winter_upkeep_insufficientWood_emitsColdShortage() public {
docs/reviews/pr250-codereview-codex-5-4.md:3081:docs/reviews/pr250-codereview-codex-5-5.md:1485:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3082:docs/reviews/pr250-codereview-codex-5-5.md:1486:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:3083:docs/reviews/pr250-codereview-codex-5-5.md:1488:+        harness.setClanUpkeepState(clanId, winterStart, 1e18, 100e18, 100e18, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3084:docs/reviews/pr250-codereview-codex-5-5.md:1491:+        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 1e18);
docs/reviews/pr250-codereview-codex-5-4.md:3085:docs/reviews/pr250-codereview-codex-5-5.md:1495:+        assertEq(clan.vaultWood, 0, "short winter burn should consume remaining wood");
docs/reviews/pr250-codereview-codex-5-4.md:3086:docs/reviews/pr250-codereview-codex-5-5.md:1496:+        assertEq(clan.coldDamage, 1, "short winter burn should mark cold damage");
docs/reviews/pr250-codereview-codex-5-4.md:3087:docs/reviews/pr250-codereview-codex-5-5.md:1503:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3088:docs/reviews/pr250-codereview-codex-5-5.md:1504:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:3089:docs/reviews/pr250-codereview-codex-5-5.md:1507:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:3090:docs/reviews/pr250-codereview-codex-5-5.md:1510:+        emit IClanWorldEvents.WallDegradedByCold(clanId, 1, uint32(winterStart));
docs/reviews/pr250-codereview-codex-5-4.md:3091:docs/reviews/pr250-codereview-codex-5-5.md:1523:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3092:docs/reviews/pr250-codereview-codex-5-5.md:1524:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:3093:docs/reviews/pr250-codereview-codex-5-5.md:1526:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:3094:docs/reviews/pr250-codereview-codex-5-5.md:1548:+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
docs/reviews/pr250-codereview-codex-5-4.md:3095:docs/reviews/pr250-codereview-codex-5-5.md:1552:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3096:docs/reviews/pr250-codereview-codex-5-5.md:1555:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3097:docs/reviews/pr250-codereview-codex-5-5.md:1556:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3098:docs/reviews/pr250-codereview-codex-5-5.md:1564:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
docs/reviews/pr250-codereview-codex-5-4.md:3099:docs/reviews/pr250-codereview-codex-5-5.md:1571:+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
docs/reviews/pr250-codereview-codex-5-4.md:3100:docs/reviews/pr250-codereview-codex-5-5.md:1578:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3101:docs/reviews/pr250-codereview-codex-5-5.md:1580:+        _advanceToTick(winterStart + 7);
docs/reviews/pr250-codereview-codex-5-4.md:3102:docs/reviews/pr250-codereview-codex-5-5.md:1582:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-4.md:3104:docs/reviews/pr250-codereview-codex-5-5.md:1597:+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
docs/reviews/pr250-codereview-codex-5-4.md:3105:docs/reviews/pr250-codereview-codex-5-5.md:1605:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3106:docs/reviews/pr250-codereview-codex-5-5.md:1606:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3107:docs/reviews/pr250-codereview-codex-5-5.md:1607:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3108:docs/reviews/pr250-codereview-codex-5-5.md:1619:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-4.md:3109:docs/reviews/pr250-codereview-codex-5-5.md:1622:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3110:docs/reviews/pr250-codereview-codex-5-5.md:1623:+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3112:docs/reviews/pr250-codereview-codex-5-5.md:1637:+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
docs/reviews/pr250-codereview-codex-5-4.md:3113:docs/reviews/pr250-codereview-codex-5-5.md:1641:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:3114:docs/reviews/pr250-codereview-codex-5-5.md:1642:+        _advanceToTick(winterEnd + 1);
docs/reviews/pr250-codereview-codex-5-4.md:3115:docs/reviews/pr250-codereview-codex-5-5.md:1644:+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
docs/reviews/pr250-codereview-codex-5-4.md:3116:docs/reviews/pr250-codereview-codex-5-5.md:1649:+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:3117:docs/reviews/pr250-codereview-codex-5-5.md:1650:+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-4.md:3118:docs/reviews/pr250-codereview-codex-5-5.md:1651:+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
docs/reviews/pr250-codereview-codex-5-4.md:3119:docs/reviews/pr250-codereview-codex-5-5.md:1652:+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
docs/reviews/pr250-codereview-codex-5-4.md:3120:docs/reviews/pr250-codereview-codex-5-5.md:1660:-        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:3121:docs/reviews/pr250-codereview-codex-5-5.md:1663:+        // winter is derived from the global recurring schedule
docs/reviews/pr250-codereview-codex-5-4.md:3122:docs/reviews/pr250-codereview-codex-5-5.md:1665:         assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-4.md:3123:docs/reviews/pr250-codereview-codex-5-5.md:1677:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3124:docs/reviews/pr250-codereview-codex-5-5.md:1680:-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-4.md:3125:docs/reviews/pr250-codereview-codex-5-5.md:1681:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-4.md:3126:docs/reviews/pr250-codereview-codex-5-5.md:1682:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-4.md:3127:docs/reviews/pr250-codereview-codex-5-5.md:1683:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:3128:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
docs/reviews/pr250-codereview-codex-5-4.md:3129:docs/reviews/pr194-r2-codereview-codex-5-4.md:23:3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
docs/reviews/pr250-codereview-codex-5-4.md:3130:docs/reviews/pr194-r2-codereview-codex-5-4.md:402:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3131:docs/reviews/pr194-r2-codereview-codex-5-4.md:740:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3132:docs/reviews/pr194-r2-codereview-codex-5-4.md:742:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3133:docs/reviews/pr194-r2-codereview-codex-5-4.md:744:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3134:docs/reviews/pr194-r2-codereview-codex-5-4.md:745:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3135:docs/reviews/pr194-r2-codereview-codex-5-4.md:746:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3137:docs/reviews/pr194-r2-codereview-codex-5-4.md:763:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3138:docs/reviews/pr194-r2-codereview-codex-5-4.md:767:+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:3139:docs/reviews/pr194-r2-codereview-codex-5-4.md:776:+        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:3140:docs/reviews/pr194-r2-codereview-codex-5-4.md:778:+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3141:docs/reviews/pr194-r2-codereview-codex-5-4.md:780:+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr250-codereview-codex-5-4.md:3142:docs/reviews/pr194-r2-codereview-codex-5-4.md:781:+            && seasonOffset < winterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:3143:docs/reviews/pr194-r2-codereview-codex-5-4.md:794:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:3146:docs/reviews/pr194-r2-codereview-codex-5-4.md:836:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3148:docs/reviews/pr194-r2-codereview-codex-5-4.md:1137:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3149:docs/reviews/pr194-r2-codereview-codex-5-4.md:1138:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3150:docs/reviews/pr194-r2-codereview-codex-5-4.md:1139:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3151:docs/reviews/pr194-r2-codereview-codex-5-4.md:1140:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3152:docs/reviews/pr194-r2-codereview-codex-5-4.md:1143:+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:3155:docs/reviews/pr194-r2-codereview-codex-5-4.md:1183:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3156:docs/reviews/pr194-r2-codereview-codex-5-4.md:1258:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3165:docs/reviews/pr194-r2-codereview-codex-5-4.md:2476:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3166:docs/reviews/pr194-r2-codereview-codex-5-4.md:2507:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3167:docs/reviews/pr194-r2-codereview-codex-5-4.md:2517:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3169:docs/reviews/pr194-r2-codereview-codex-5-4.md:2615:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3170:docs/reviews/pr194-r2-codereview-codex-5-4.md:2619:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3171:docs/reviews/pr194-r2-codereview-codex-5-4.md:2691:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3172:docs/reviews/pr194-r2-codereview-codex-5-4.md:2723:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3173:docs/reviews/pr194-r2-codereview-codex-5-4.md:2728:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3174:docs/reviews/pr194-r2-codereview-codex-5-4.md:3254:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3175:docs/reviews/pr194-r2-codereview-codex-5-4.md:3262:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3176:docs/reviews/pr194-r2-codereview-codex-5-4.md:3554:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3177:docs/reviews/pr194-r2-codereview-codex-5-4.md:3555:+        _advanceUntil(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3178:docs/reviews/pr194-r2-codereview-codex-5-4.md:3574:+        uint64 deathFromTick = winterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3180:docs/reviews/pr194-r2-codereview-codex-5-4.md:3685:+    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr250-codereview-codex-5-4.md:3181:docs/reviews/pr194-r2-codereview-codex-5-4.md:3686:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3182:docs/reviews/pr194-r2-codereview-codex-5-4.md:3687:+        _advanceUntil(winterStart + 30);
docs/reviews/pr250-codereview-codex-5-4.md:3183:docs/reviews/pr194-r2-codereview-codex-5-4.md:3688:+        assertFalse(world.getWorldState().winterActive, "test settles after winter");
docs/reviews/pr250-codereview-codex-5-4.md:3184:docs/reviews/pr194-r2-codereview-codex-5-4.md:3691:+        world.setClanUpkeepState(clanId, winterStart, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3185:docs/reviews/pr194-r2-codereview-codex-5-4.md:3694:+        assertEq(uint8(preview.clan.clan.clanState), uint8(ClanState.DEAD), "derived view replays winter deaths");
docs/reviews/pr250-codereview-codex-5-4.md:3186:docs/reviews/pr194-r2-codereview-codex-5-4.md:3700:+        assertEq(uint8(settled.clanState), uint8(ClanState.DEAD), "settlement replays winter deaths");
docs/reviews/pr250-codereview-codex-5-4.md:3187:docs/reviews/pr194-r2-codereview-codex-5-4.md:3705:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3188:docs/reviews/pr194-r2-codereview-codex-5-4.md:3706:+        _advanceUntil(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-4.md:3189:docs/reviews/pr194-r2-codereview-codex-5-4.md:3709:+        world.setClanUpkeepState(targetClanId, winterStart, 0, 0);
docs/reviews/pr250-codereview-codex-5-4.md:3192:docs/reviews/pr194-r2-codereview-codex-5-4.md:4374:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr250-codereview-codex-5-4.md:3193:docs/reviews/pr194-r2-codereview-codex-5-4.md:4380:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3194:docs/reviews/pr194-r2-codereview-codex-5-4.md:4442:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3195:docs/reviews/pr194-r2-codereview-codex-5-4.md:4443:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3196:docs/reviews/pr194-r2-codereview-codex-5-4.md:4486:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3197:docs/reviews/pr194-r2-codereview-codex-5-4.md:4488:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:3198:docs/reviews/pr194-r2-codereview-codex-5-4.md:4490:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:3199:docs/reviews/pr194-r2-codereview-codex-5-4.md:4493:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:3200:docs/reviews/pr194-r2-codereview-codex-5-4.md:4494:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:3201:docs/reviews/pr194-r2-codereview-codex-5-4.md:4496:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:3202:docs/reviews/pr194-r2-codereview-codex-5-4.md:4498:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3203:docs/reviews/pr194-r2-codereview-codex-5-4.md:4499:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:3204:docs/reviews/pr194-r2-codereview-codex-5-4.md:4746:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3205:docs/reviews/pr194-r2-codereview-codex-5-4.md:4747:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3206:docs/reviews/pr194-r2-codereview-codex-5-4.md:4748:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3207:docs/reviews/pr194-r2-codereview-codex-5-4.md:4813:-          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3208:docs/reviews/pr194-r2-codereview-codex-5-4.md:4814:-          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3209:docs/reviews/pr194-r2-codereview-codex-5-4.md:4815:-          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3210:docs/reviews/pr194-r2-codereview-codex-5-4.md:4864:-                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3211:docs/reviews/pr194-r2-codereview-codex-5-4.md:5069:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3212:docs/reviews/pr194-r2-codereview-codex-5-4.md:5070:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3213:docs/reviews/pr194-r2-codereview-codex-5-4.md:5071:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3214:docs/reviews/pr194-r2-codereview-codex-5-4.md:5087:+      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3215:docs/reviews/pr194-r2-codereview-codex-5-4.md:5088:+      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3216:docs/reviews/pr194-r2-codereview-codex-5-4.md:5089:+      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3217:docs/reviews/pr194-r2-codereview-codex-5-4.md:5125:+        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:3218:docs/reviews/pr194-r2-codereview-codex-5-4.md:5126:+        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:3219:docs/reviews/pr194-r2-codereview-codex-5-4.md:5127:+        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:3220:docs/reviews/pr194-r2-codereview-codex-5-4.md:5141:+    expect(state.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:3221:docs/reviews/pr194-r2-codereview-codex-5-4.md:5144:+  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr250-codereview-codex-5-4.md:3222:docs/reviews/pr194-r2-codereview-codex-5-4.md:5153:+        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:3223:docs/reviews/pr194-r2-codereview-codex-5-4.md:5154:+        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:3224:docs/reviews/pr194-r2-codereview-codex-5-4.md:5155:+        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:3225:docs/reviews/pr194-r2-codereview-codex-5-4.md:5170:+    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
docs/reviews/pr250-codereview-codex-5-4.md:3227:docs/reviews/pr194-r2-codereview-codex-5-4.md:5203:  → Inspect changed Solidity bandit/heartbeat/starvation code in context and identify behavioral regressions
docs/reviews/pr250-codereview-codex-5-4.md:3228:docs/reviews/pr194-r2-codereview-codex-5-4.md:5315:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3229:docs/reviews/pr194-r2-codereview-codex-5-4.md:5316:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3230:docs/reviews/pr194-r2-codereview-codex-5-4.md:5317:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3231:docs/reviews/pr194-r2-codereview-codex-5-4.md:5354:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3232:docs/reviews/pr194-r2-codereview-codex-5-4.md:5356:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3233:docs/reviews/pr194-r2-codereview-codex-5-4.md:5445:    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:3235:docs/reviews/pr194-r2-codereview-codex-5-4.md:5514:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3238:docs/reviews/pr194-r2-codereview-codex-5-4.md:5675:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3239:docs/reviews/pr194-r2-codereview-codex-5-4.md:5676:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3240:docs/reviews/pr194-r2-codereview-codex-5-4.md:5677:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3242:docs/reviews/pr194-r2-codereview-codex-5-4.md:5902:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3243:docs/reviews/pr194-r2-codereview-codex-5-4.md:5924:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3244:docs/reviews/pr194-r2-codereview-codex-5-4.md:5925:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3245:docs/reviews/pr194-r2-codereview-codex-5-4.md:5927:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3246:docs/reviews/pr194-r2-codereview-codex-5-4.md:5928:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3247:docs/reviews/pr194-r2-codereview-codex-5-4.md:5932:        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:3248:docs/reviews/pr194-r2-codereview-codex-5-4.md:5941:        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:3249:docs/reviews/pr194-r2-codereview-codex-5-4.md:5943:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3250:docs/reviews/pr194-r2-codereview-codex-5-4.md:5945:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr250-codereview-codex-5-4.md:3251:docs/reviews/pr194-r2-codereview-codex-5-4.md:5946:            && seasonOffset < winterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:3252:docs/reviews/pr194-r2-codereview-codex-5-4.md:5959:                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:3254:docs/reviews/pr194-r2-codereview-codex-5-4.md:5989:        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3255:docs/reviews/pr194-r2-codereview-codex-5-4.md:5990:            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3256:docs/reviews/pr194-r2-codereview-codex-5-4.md:5991:        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3257:docs/reviews/pr194-r2-codereview-codex-5-4.md:5992:            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3258:docs/reviews/pr194-r2-codereview-codex-5-4.md:5995:        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr250-codereview-codex-5-4.md:3261:docs/reviews/pr194-r2-codereview-codex-5-4.md:6035:        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3262:docs/reviews/pr194-r2-codereview-codex-5-4.md:6110:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3263:docs/reviews/pr194-r2-codereview-codex-5-4.md:6177:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3265:docs/reviews/pr194-r2-codereview-codex-5-4.md:6450:  3415	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3266:docs/reviews/pr194-r2-codereview-codex-5-4.md:6879:    22	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3267:docs/reviews/pr194-r2-codereview-codex-5-4.md:6880:    23	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3268:docs/reviews/pr194-r2-codereview-codex-5-4.md:6881:    24	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3269:docs/reviews/pr194-r2-codereview-codex-5-4.md:6897:    40	      { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3270:docs/reviews/pr194-r2-codereview-codex-5-4.md:6898:    41	      { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3271:docs/reviews/pr194-r2-codereview-codex-5-4.md:6899:    42	      { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3272:docs/reviews/pr194-r2-codereview-codex-5-4.md:6935:    78	        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:3273:docs/reviews/pr194-r2-codereview-codex-5-4.md:6936:    79	        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:3274:docs/reviews/pr194-r2-codereview-codex-5-4.md:6937:    80	        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:3275:docs/reviews/pr194-r2-codereview-codex-5-4.md:6951:    94	    expect(state.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:3276:docs/reviews/pr194-r2-codereview-codex-5-4.md:6954:    97	  it('decodes a known-good getWorldSnapshot() result with season fields before winter fields', () => {
docs/reviews/pr250-codereview-codex-5-4.md:3277:docs/reviews/pr194-r2-codereview-codex-5-4.md:6963:   106	        winterActive: true,
docs/reviews/pr250-codereview-codex-5-4.md:3278:docs/reviews/pr194-r2-codereview-codex-5-4.md:6964:   107	        winterStartsAtTick: 100n,
docs/reviews/pr250-codereview-codex-5-4.md:3279:docs/reviews/pr194-r2-codereview-codex-5-4.md:6965:   108	        winterEndsAtTick: 110n,
docs/reviews/pr250-codereview-codex-5-4.md:3280:docs/reviews/pr194-r2-codereview-codex-5-4.md:6980:   123	    expect(snapshot.winterActive).toBe(true);
docs/reviews/pr250-codereview-codex-5-4.md:3281:docs/reviews/pr194-r2-codereview-codex-5-4.md:6995:  3472	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3282:docs/reviews/pr194-r2-codereview-codex-5-4.md:6996:  3473	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3283:docs/reviews/pr194-r2-codereview-codex-5-4.md:6997:  3474	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3290:docs/reviews/pr194-r2-codereview-codex-5-4.md:7465:   612	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3292:docs/reviews/pr194-r2-codereview-codex-5-4.md:7998:612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3293:docs/reviews/pr194-r2-codereview-codex-5-4.md:7999:1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3294:docs/reviews/pr194-r2-codereview-codex-5-4.md:8038:3415:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3296:docs/reviews/pr194-r2-codereview-codex-5-4.md:8167:  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3297:docs/reviews/pr194-r2-codereview-codex-5-4.md:8182:The source pass is down to two likely review items: one in view-simulation starvation handling, and one possible stale-state issue during target selection before a camped bandit attacks. I’m waiting on the parallel passes now to see whether they independently confirm those or uncover anything else in the ABI/tests lane.
docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr250-codereview-codex-5-4.md:3300:docs/reviews/pr194-r2-codereview-codex-5-4.md:8313:  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr250-codereview-codex-5-4.md:3302:docs/reviews/pr194-r2-codereview-codex-5-4.md:8349:  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
docs/reviews/pr250-codereview-codex-5-4.md:3303:docs/reviews/r2/pr194-codereview-codex-5-5.md:430:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3304:docs/reviews/r2/pr194-codereview-codex-5-5.md:760:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3305:docs/reviews/r2/pr194-codereview-codex-5-5.md:762:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3306:docs/reviews/r2/pr194-codereview-codex-5-5.md:764:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3307:docs/reviews/r2/pr194-codereview-codex-5-5.md:765:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3308:docs/reviews/r2/pr194-codereview-codex-5-5.md:766:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3310:docs/reviews/r2/pr194-codereview-codex-5-5.md:783:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3311:docs/reviews/r2/pr194-codereview-codex-5-5.md:787:+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3312:docs/reviews/r2/pr194-codereview-codex-5-5.md:802:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:3315:docs/reviews/r2/pr194-codereview-codex-5-5.md:844:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3317:docs/reviews/r2/pr194-codereview-codex-5-5.md:1145:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3318:docs/reviews/r2/pr194-codereview-codex-5-5.md:1146:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3319:docs/reviews/r2/pr194-codereview-codex-5-5.md:1147:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3320:docs/reviews/r2/pr194-codereview-codex-5-5.md:1148:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3321:docs/reviews/r2/pr194-codereview-codex-5-5.md:1151:+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3324:docs/reviews/r2/pr194-codereview-codex-5-5.md:1191:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3325:docs/reviews/r2/pr194-codereview-codex-5-5.md:1266:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3331:docs/reviews/r2/pr194-codereview-codex-5-5.md:2419:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3332:docs/reviews/r2/pr194-codereview-codex-5-5.md:2450:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3333:docs/reviews/r2/pr194-codereview-codex-5-5.md:2460:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3335:docs/reviews/r2/pr194-codereview-codex-5-5.md:2558:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3336:docs/reviews/r2/pr194-codereview-codex-5-5.md:2562:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3337:docs/reviews/r2/pr194-codereview-codex-5-5.md:2634:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3338:docs/reviews/r2/pr194-codereview-codex-5-5.md:2666:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3339:docs/reviews/r2/pr194-codereview-codex-5-5.md:2671:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3340:docs/reviews/r2/pr194-codereview-codex-5-5.md:3196:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3341:docs/reviews/r2/pr194-codereview-codex-5-5.md:3206:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3342:docs/reviews/r2/pr194-codereview-codex-5-5.md:3444:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3343:docs/reviews/r2/pr194-codereview-codex-5-5.md:3445:+        _advanceUntil(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3344:docs/reviews/r2/pr194-codereview-codex-5-5.md:3446:+        uint64 deathFromTick = winterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3347:docs/reviews/r2/pr194-codereview-codex-5-5.md:4180:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr250-codereview-codex-5-4.md:3348:docs/reviews/r2/pr194-codereview-codex-5-5.md:4186:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3349:docs/reviews/r2/pr194-codereview-codex-5-5.md:4248:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3350:docs/reviews/r2/pr194-codereview-codex-5-5.md:4249:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3351:docs/reviews/r2/pr194-codereview-codex-5-5.md:4585:    46	///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3352:docs/reviews/r2/pr194-codereview-codex-5-5.md:4847:   287	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3353:docs/reviews/r2/pr194-codereview-codex-5-5.md:4869:   309	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3354:docs/reviews/r2/pr194-codereview-codex-5-5.md:4870:   310	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3355:docs/reviews/r2/pr194-codereview-codex-5-5.md:4872:   312	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3356:docs/reviews/r2/pr194-codereview-codex-5-5.md:4873:   313	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3357:docs/reviews/r2/pr194-codereview-codex-5-5.md:4880:   320	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3358:docs/reviews/r2/pr194-codereview-codex-5-5.md:5053:   118	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3359:docs/reviews/r2/pr194-codereview-codex-5-5.md:5133:   198	    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3360:docs/reviews/r2/pr194-codereview-codex-5-5.md:5134:   199	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3361:docs/reviews/r2/pr194-codereview-codex-5-5.md:5135:   200	    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3362:docs/reviews/r2/pr194-codereview-codex-5-5.md:5172:   237	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3363:docs/reviews/r2/pr194-codereview-codex-5-5.md:5174:   239	    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3374:docs/reviews/r2/pr194-codereview-codex-5-5.md:6893:   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3375:docs/reviews/r2/pr194-codereview-codex-5-5.md:6915:   463	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3376:docs/reviews/r2/pr194-codereview-codex-5-5.md:6916:   464	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3377:docs/reviews/r2/pr194-codereview-codex-5-5.md:6918:   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3378:docs/reviews/r2/pr194-codereview-codex-5-5.md:6919:   467	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3379:docs/reviews/r2/pr194-codereview-codex-5-5.md:6923:   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3380:docs/reviews/r2/pr194-codereview-codex-5-5.md:6938:   486	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:3383:docs/reviews/r2/pr194-codereview-codex-5-5.md:7182:  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3384:docs/reviews/r2/pr194-codereview-codex-5-5.md:7183:  1086	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3385:docs/reviews/r2/pr194-codereview-codex-5-5.md:7184:  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3386:docs/reviews/r2/pr194-codereview-codex-5-5.md:7185:  1088	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3387:docs/reviews/r2/pr194-codereview-codex-5-5.md:7188:  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3390:docs/reviews/r2/pr194-codereview-codex-5-5.md:7228:  1131	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3391:docs/reviews/r2/pr194-codereview-codex-5-5.md:7303:  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3396:docs/reviews/r2/pr194-codereview-codex-5-5.md:8001:  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3398:docs/reviews/r2/pr194-codereview-codex-5-5.md:8049:  2302	            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:3399:docs/reviews/r2/pr194-codereview-codex-5-5.md:8050:  2303	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3400:docs/reviews/r2/pr194-codereview-codex-5-5.md:8051:  2304	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:3401:docs/reviews/r2/pr194-codereview-codex-5-5.md:8053:  2306	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3402:docs/reviews/r2/pr194-codereview-codex-5-5.md:8056:  2309	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:3403:docs/reviews/r2/pr194-codereview-codex-5-5.md:8058:  2311	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:3404:docs/reviews/r2/pr194-codereview-codex-5-5.md:8059:  2312	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:3405:docs/reviews/r2/pr194-codereview-codex-5-5.md:8061:  2314	            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:3406:docs/reviews/r2/pr194-codereview-codex-5-5.md:8064:  2317	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3407:docs/reviews/r2/pr194-codereview-codex-5-5.md:8065:  2318	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3408:docs/reviews/r2/pr194-codereview-codex-5-5.md:8067:  2320	            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:3409:docs/reviews/r2/pr194-codereview-codex-5-5.md:8068:  2321	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:3410:docs/reviews/r2/pr194-codereview-codex-5-5.md:8070:  2323	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3411:docs/reviews/r2/pr194-codereview-codex-5-5.md:8072:  2325	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3412:docs/reviews/r2/pr194-codereview-codex-5-5.md:8073:  2326	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:3413:docs/reviews/r2/pr194-codereview-codex-5-5.md:8075:  2328	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:3414:docs/reviews/r2/pr194-codereview-codex-5-5.md:8076:  2329	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:3415:docs/reviews/r2/pr194-codereview-codex-5-5.md:8077:  2330	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:3418:docs/reviews/r2/pr194-codereview-codex-5-5.md:8120:   528	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3419:docs/reviews/r2/pr194-codereview-codex-5-5.md:8190:   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3427:docs/reviews/r2/pr194-codereview-codex-5-5.md:8841:   120	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3428:docs/reviews/r2/pr194-codereview-codex-5-5.md:8923:   202	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3429:docs/reviews/r2/pr194-codereview-codex-5-5.md:8924:   203	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3430:docs/reviews/r2/pr194-codereview-codex-5-5.md:8925:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3431:docs/reviews/r2/pr194-codereview-codex-5-5.md:9660:    39	          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3432:docs/reviews/r2/pr194-codereview-codex-5-5.md:9661:    40	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3433:docs/reviews/r2/pr194-codereview-codex-5-5.md:9662:    41	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3434:docs/reviews/r2/pr194-codereview-codex-5-5.md:9711:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3435:docs/reviews/r2/pr194-codereview-codex-5-5.md:9975:   414	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3436:docs/reviews/r2/pr194-codereview-codex-5-5.md:9976:   415	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3437:docs/reviews/r2/pr194-codereview-codex-5-5.md:9977:   416	    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3439:docs/reviews/r2/pr194-codereview-codex-5-5.md:10062:  3393	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3440:docs/reviews/r2/pr194-codereview-codex-5-5.md:10063:  3394	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3441:docs/reviews/r2/pr194-codereview-codex-5-5.md:10064:  3395	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3442:docs/reviews/r2/pr194-codereview-codex-5-5.md:10126:    48	          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3443:docs/reviews/r2/pr194-codereview-codex-5-5.md:10127:    49	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3444:docs/reviews/r2/pr194-codereview-codex-5-5.md:10128:    50	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3445:docs/reviews/r2/pr194-codereview-codex-5-5.md:10176:     9	    // Season + winter timers (Phase 4.4)
docs/reviews/pr250-codereview-codex-5-4.md:3446:docs/reviews/r2/pr194-codereview-codex-5-5.md:10180:    13	    winterActive: v.optional(v.boolean()),
docs/reviews/pr250-codereview-codex-5-4.md:3447:docs/reviews/r2/pr194-codereview-codex-5-5.md:10181:    14	    winterStartsAtTick: v.optional(v.number()),
docs/reviews/pr250-codereview-codex-5-4.md:3448:docs/reviews/r2/pr194-codereview-codex-5-5.md:10182:    15	    winterEndsAtTick: v.optional(v.number()),
docs/reviews/pr250-codereview-codex-5-4.md:3449:docs/reviews/r2/pr194-codereview-codex-5-5.md:10260:23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:280:        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3450:docs/reviews/r2/pr194-codereview-codex-5-5.md:10333:23f7f1a:packages/contracts/test/ClanWorld.t.sol:2045:        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:3451:docs/reviews/r2/pr194-codereview-codex-5-5.md:10334:23f7f1a:packages/contracts/test/ClanWorld.t.sol:2046:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:3452:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr250-codereview-codex-5-4.md:3453:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr250-codereview-codex-5-4.md:3454:docs/reviews/pr194-r2-synthesis.md:14:- ✅ Winter starvation replay uses replayed-tick winter status
docs/reviews/pr250-codereview-codex-5-4.md:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr250-codereview-codex-5-4.md:3456:docs/reviews/r2/pr194-codereview-opus-4-6.md:51:### M2: `_simulateResolveAction` starvation check uses `_world.currentTick` instead of simulation tick
docs/reviews/pr250-codereview-codex-5-4.md:3457:docs/reviews/r2/pr194-codereview-opus-4-6.md:56:bool starving = sim.clan.starvationStartsAtTick != 0 
docs/reviews/pr250-codereview-codex-5-4.md:3458:docs/reviews/r2/pr194-codereview-opus-4-6.md:57:    && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3459:docs/reviews/r2/pr194-codereview-opus-4-6.md:60:The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.
docs/reviews/pr250-codereview-codex-5-4.md:3460:docs/reviews/r2/pr194-codereview-opus-4-6.md:62:**Impact:** Low in practice — the upkeep/resolution ordering within the simulation loop prevents the misalignment. But it's a latent bug if the simulation structure changes. Should use `sim.clan.starvationStartsAtTick <= tick` for correctness parity.
docs/reviews/pr250-codereview-codex-5-4.md:3461:docs/reviews/pr198-codereview-codex-5-4.md:322:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3462:docs/reviews/pr198-codereview-codex-5-4.md:747:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3463:docs/reviews/pr198-codereview-codex-5-4.md:749:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3464:docs/reviews/pr198-codereview-codex-5-4.md:751:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3465:docs/reviews/pr198-codereview-codex-5-4.md:752:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3466:docs/reviews/pr198-codereview-codex-5-4.md:753:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3467:docs/reviews/pr198-codereview-codex-5-4.md:756:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3469:docs/reviews/pr198-codereview-codex-5-4.md:3197:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3470:docs/reviews/pr198-codereview-codex-5-4.md:3199:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:3471:docs/reviews/pr198-codereview-codex-5-4.md:3201:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:3472:docs/reviews/pr198-codereview-codex-5-4.md:3204:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:3473:docs/reviews/pr198-codereview-codex-5-4.md:3205:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:3474:docs/reviews/pr198-codereview-codex-5-4.md:3207:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:3475:docs/reviews/pr198-codereview-codex-5-4.md:3209:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3476:docs/reviews/pr198-codereview-codex-5-4.md:3210:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:3477:docs/reviews/pr198-codereview-codex-5-4.md:3853:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3478:docs/reviews/pr198-codereview-codex-5-4.md:3943:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3479:docs/reviews/pr198-codereview-codex-5-4.md:3945:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3480:docs/reviews/pr198-codereview-codex-5-4.md:3946:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3481:docs/reviews/pr198-codereview-codex-5-4.md:3947:        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3482:docs/reviews/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3483:docs/reviews/pr198-codereview-codex-5-4.md:4317:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3484:docs/reviews/pr198-codereview-codex-5-4.md:4318:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3485:docs/reviews/pr198-codereview-codex-5-4.md:4319:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3486:docs/reviews/pr198-codereview-codex-5-4.md:4356:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3487:docs/reviews/pr198-codereview-codex-5-4.md:4358:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3488:docs/reviews/pr198-codereview-codex-5-4.md:4651:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3494:docs/reviews/pr198-codereview-codex-5-4.md:5941:   124	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3495:docs/reviews/pr198-codereview-codex-5-4.md:6024:   207	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3496:docs/reviews/pr198-codereview-codex-5-4.md:6025:   208	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3497:docs/reviews/pr198-codereview-codex-5-4.md:6026:   209	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3498:docs/reviews/pr198-codereview-codex-5-4.md:6063:   246	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3499:docs/reviews/pr198-codereview-codex-5-4.md:6065:   248	    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3500:docs/reviews/pr198-codereview-codex-5-4.md:7052:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3501:docs/reviews/pr198-codereview-codex-5-4.md:7125:docs/reviews/r2/pr194-codereview-codex-5-5.md:4585:    46	///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3502:docs/reviews/pr198-codereview-codex-5-4.md:7206:docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr250-codereview-codex-5-4.md:3503:docs/reviews/pr198-codereview-codex-5-4.md:7293:docs/reviews/r2/pr194-codereview-codex-5-4.md:4859:///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3504:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
docs/reviews/pr250-codereview-codex-5-4.md:3505:docs/reviews/pr198-codereview-codex-5-4.md:7662:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3506:docs/reviews/pr198-codereview-codex-5-4.md:7821:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3508:docs/reviews/pr198-codereview-codex-5-4.md:8029:docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/reviews/pr250-codereview-codex-5-4.md:3509:docs/reviews/pr198-codereview-codex-5-4.md:8055:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3510:docs/reviews/pr198-codereview-codex-5-4.md:8157:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr250-codereview-codex-5-4.md:3511:docs/reviews/pr198-codereview-codex-5-4.md:8772:   876	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3512:docs/reviews/pr198-codereview-codex-5-4.md:8797:   901	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3514:docs/reviews/pr198-codereview-codex-5-4.md:8842:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3515:docs/reviews/pr198-codereview-codex-5-4.md:8978:   397	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3516:docs/reviews/pr198-codereview-codex-5-4.md:9000:   419	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3517:docs/reviews/pr198-codereview-codex-5-4.md:9001:   420	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3518:docs/reviews/pr198-codereview-codex-5-4.md:9003:   422	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3519:docs/reviews/pr198-codereview-codex-5-4.md:9004:   423	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3520:docs/reviews/pr198-codereview-codex-5-4.md:9011:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3521:docs/reviews/pr198-codereview-codex-5-4.md:9133:   423	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3522:docs/reviews/pr198-codereview-codex-5-4.md:9134:   424	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3523:docs/reviews/pr198-codereview-codex-5-4.md:9135:   425	    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3524:docs/reviews/pr198-codereview-codex-5-4.md:9325:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:3529:docs/reviews/pr198-codereview-codex-5-4.md:9407:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/reviews/pr250-codereview-codex-5-4.md:3530:docs/reviews/pr198-codereview-codex-5-4.md:9414:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:3531:docs/reviews/pr198-codereview-codex-5-4.md:9415:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:3532:docs/reviews/pr198-codereview-codex-5-4.md:9421:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr250-codereview-codex-5-4.md:3533:docs/reviews/pr198-codereview-codex-5-4.md:9423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr250-codereview-codex-5-4.md:3534:docs/reviews/pr198-codereview-codex-5-4.md:9436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr250-codereview-codex-5-4.md:3535:docs/reviews/pr198-codereview-codex-5-4.md:9437:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/reviews/pr250-codereview-codex-5-4.md:3536:docs/reviews/pr198-codereview-codex-5-4.md:9445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr250-codereview-codex-5-4.md:3537:docs/reviews/pr198-codereview-codex-5-4.md:10322:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:3538:docs/reviews/pr198-codereview-codex-5-4.md:10402:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/reviews/pr250-codereview-codex-5-4.md:3539:docs/reviews/pr198-codereview-codex-5-4.md:10403:- that defending worker contributes 0 defense until Clan B starvation ends
docs/reviews/pr250-codereview-codex-5-4.md:3540:docs/reviews/pr198-codereview-codex-5-4.md:10728:  2297	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3541:docs/reviews/pr198-codereview-codex-5-4.md:10732:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3542:docs/reviews/pr198-codereview-codex-5-4.md:10823:  2392	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3543:docs/reviews/pr198-codereview-codex-5-4.md:10824:  2393	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3544:docs/reviews/pr198-codereview-codex-5-4.md:10825:  2394	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3545:docs/reviews/pr198-codereview-codex-5-4.md:10832:  2401	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3546:docs/reviews/pr198-codereview-codex-5-4.md:10837:  2406	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3547:docs/reviews/pr199-codereview-codex-5-5.md:177:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3548:docs/reviews/pr199-codereview-codex-5-5.md:2992:docs/reviews/pr194-r2-codereview-codex-5-5.md:5423:/bin/bash -lc 'rg -n "test_e2e_banditLifecycle|test_winterStarvationReplay|test_resolveBanditAttackReturns|test_defeatedBanditAwardsBlueprint|test_deadTargetCleanup|test_heartbeat_scheduledMarketObserves" packages/contracts/test' in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:3549:docs/reviews/pr199-codereview-codex-5-5.md:2996:docs/reviews/pr194-r2-codereview-codex-5-5.md:5428:packages/contracts/test/BanditAttackResolution.t.sol:464:    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr250-codereview-codex-5-4.md:3550:docs/reviews/pr199-codereview-codex-5-5.md:3027:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:3551:docs/reviews/pr199-codereview-codex-5-5.md:3511:docs/reviews/r2/pr194-codereview-codex-5-5.md:10260:23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:280:        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3552:docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
docs/reviews/pr250-codereview-codex-5-4.md:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr250-codereview-codex-5-4.md:3554:docs/reviews/pr199-codereview-codex-5-5.md:3694:docs/reviews/r2/pr194-codereview-opus-4-6.md:60:The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.
docs/reviews/pr250-codereview-codex-5-4.md:3555:docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:3556:docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-4.md:3557:docs/reviews/pr199-codereview-codex-5-5.md:5260:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3558:docs/reviews/pr199-codereview-codex-5-5.md:5261:docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr250-codereview-codex-5-4.md:3559:docs/reviews/pr199-codereview-codex-5-5.md:5290:docs/planning/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr250-codereview-codex-5-4.md:3560:docs/reviews/pr199-codereview-codex-5-5.md:5316:docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr250-codereview-codex-5-4.md:3561:docs/reviews/pr199-codereview-codex-5-5.md:5332:docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr250-codereview-codex-5-4.md:3562:docs/reviews/pr199-codereview-codex-5-5.md:5365:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr250-codereview-codex-5-4.md:3563:docs/reviews/pr199-codereview-codex-5-5.md:5383:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr250-codereview-codex-5-4.md:3564:docs/reviews/pr199-codereview-codex-5-5.md:5384:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr250-codereview-codex-5-4.md:3565:docs/reviews/pr199-codereview-codex-5-5.md:5438:docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr250-codereview-codex-5-4.md:3566:docs/reviews/pr199-codereview-codex-5-5.md:5457:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr250-codereview-codex-5-4.md:3567:docs/reviews/pr199-codereview-codex-5-5.md:5470:docs/planning/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr250-codereview-codex-5-4.md:3568:docs/reviews/pr199-codereview-codex-5-5.md:5473:docs/planning/clanworld_v4_1_addendum.md:156:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr250-codereview-codex-5-4.md:3569:docs/reviews/pr199-codereview-codex-5-5.md:5474:docs/planning/clanworld_v4_1_addendum.md:174:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr250-codereview-codex-5-4.md:3570:docs/reviews/pr199-codereview-codex-5-5.md:5508:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3571:docs/reviews/pr199-codereview-codex-5-5.md:5603:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:3572:docs/reviews/pr199-codereview-codex-5-5.md:5607:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3573:docs/reviews/pr199-codereview-codex-5-5.md:5609:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3578:docs/reviews/pr199-codereview-codex-5-5.md:5880:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr250-codereview-codex-5-4.md:3583:docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-4.md:3584:docs/reviews/pr199-codereview-codex-5-5.md:6006:docs/reviews/pr198-codereview-codex-5-4.md:7052:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3585:docs/reviews/pr199-codereview-codex-5-5.md:6026:docs/reviews/pr198-codereview-codex-5-4.md:7662:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3586:docs/reviews/pr199-codereview-codex-5-5.md:6033:docs/reviews/pr198-codereview-codex-5-4.md:7821:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3587:docs/reviews/pr199-codereview-codex-5-5.md:6039:docs/reviews/pr198-codereview-codex-5-4.md:8055:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3588:docs/reviews/pr199-codereview-codex-5-5.md:6062:docs/reviews/pr198-codereview-codex-5-4.md:9421:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr250-codereview-codex-5-4.md:3589:docs/reviews/pr199-codereview-codex-5-5.md:6063:docs/reviews/pr198-codereview-codex-5-4.md:9423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr250-codereview-codex-5-4.md:3590:docs/reviews/pr199-codereview-codex-5-5.md:6064:docs/reviews/pr198-codereview-codex-5-4.md:9436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr250-codereview-codex-5-4.md:3591:docs/reviews/pr199-codereview-codex-5-5.md:6066:docs/reviews/pr198-codereview-codex-5-4.md:9445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr250-codereview-codex-5-4.md:3593:docs/reviews/pr199-codereview-codex-5-5.md:6195:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3594:docs/reviews/pr199-codereview-codex-5-5.md:6202:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr250-codereview-codex-5-4.md:3604:docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr250-codereview-codex-5-4.md:3605:docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr250-codereview-codex-5-4.md:3606:docs/reviews/pr199-codereview-codex-5-5.md:6919:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:430:- The game rewards monument-building, but you must survive winters and bandits to get there.
docs/reviews/pr250-codereview-codex-5-4.md:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr250-codereview-codex-5-4.md:3609:docs/reviews/pr199-codereview-codex-5-5.md:7008:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr250-codereview-codex-5-4.md:3610:docs/reviews/pr199-codereview-codex-5-5.md:7068:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr250-codereview-codex-5-4.md:3611:docs/reviews/pr199-codereview-codex-5-5.md:7074:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3612:docs/reviews/pr199-codereview-codex-5-5.md:7162:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:3613:docs/reviews/pr199-codereview-codex-5-5.md:7163:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3614:docs/reviews/pr199-codereview-codex-5-5.md:7167:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3617:docs/reviews/pr199-codereview-codex-5-5.md:7239:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr250-codereview-codex-5-4.md:3618:docs/reviews/pr199-codereview-codex-5-5.md:7241:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:157:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr250-codereview-codex-5-4.md:3619:docs/reviews/pr199-codereview-codex-5-5.md:7242:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:175:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr250-codereview-codex-5-4.md:3620:docs/reviews/pr199-codereview-codex-5-5.md:7249:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr250-codereview-codex-5-4.md:3623:docs/reviews/pr199-codereview-codex-5-5.md:7270:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr250-codereview-codex-5-4.md:3624:docs/reviews/pr199-codereview-codex-5-5.md:7282:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:3625:docs/reviews/pr199-codereview-codex-5-5.md:7283:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr250-codereview-codex-5-4.md:3626:docs/reviews/pr199-codereview-codex-5-5.md:7312:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr250-codereview-codex-5-4.md:3627:docs/reviews/pr199-codereview-codex-5-5.md:7338:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr250-codereview-codex-5-4.md:3628:docs/reviews/pr199-codereview-codex-5-5.md:7354:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr250-codereview-codex-5-4.md:3629:docs/reviews/pr199-codereview-codex-5-5.md:7384:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr250-codereview-codex-5-4.md:3630:docs/reviews/pr199-codereview-codex-5-5.md:7402:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr250-codereview-codex-5-4.md:3631:docs/reviews/pr199-codereview-codex-5-5.md:7403:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr250-codereview-codex-5-4.md:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr250-codereview-codex-5-4.md:3635:docs/reviews/pr199-codereview-codex-5-5.md:8708:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:3636:docs/reviews/pr199-codereview-codex-5-5.md:10207:  2221	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3637:docs/reviews/pr199-codereview-codex-5-5.md:10211:  2225	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3638:docs/reviews/pr199-codereview-codex-5-5.md:10286:  2414	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3639:docs/reviews/pr199-codereview-codex-5-5.md:10287:  2415	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3640:docs/reviews/pr199-codereview-codex-5-5.md:10288:  2416	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3641:docs/reviews/pr199-codereview-codex-5-5.md:10295:  2423	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3642:docs/reviews/pr199-codereview-codex-5-5.md:10300:  2428	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3643:docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3644:packages/contracts/src/ClanWorld.sol:47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3645:packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3646:packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3647:packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3648:packages/contracts/src/ClanWorld.sol:138:        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3649:packages/contracts/src/ClanWorld.sol:443:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3650:packages/contracts/src/ClanWorld.sol:465:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3651:packages/contracts/src/ClanWorld.sol:466:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3652:packages/contracts/src/ClanWorld.sol:468:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3653:packages/contracts/src/ClanWorld.sol:469:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3654:packages/contracts/src/ClanWorld.sol:476:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3655:packages/contracts/src/ClanWorld.sol:909:    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3656:packages/contracts/src/ClanWorld.sol:934:        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3658:packages/contracts/src/ClanWorld.sol:979:            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:3659:packages/contracts/src/ClanWorld.sol:980:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3660:packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:3661:packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3662:packages/contracts/src/ClanWorld.sol:986:        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:3663:packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:3664:packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:3665:packages/contracts/src/ClanWorld.sol:991:            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:3666:packages/contracts/src/ClanWorld.sol:994:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3667:packages/contracts/src/ClanWorld.sol:995:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3668:packages/contracts/src/ClanWorld.sol:997:            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:3669:packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:3670:packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3671:packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3672:packages/contracts/src/ClanWorld.sol:1003:                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:3673:packages/contracts/src/ClanWorld.sol:1005:                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:3674:packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:3675:packages/contracts/src/ClanWorld.sol:1007:                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:3676:packages/contracts/src/ClanWorld.sol:1066:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3682:packages/contracts/src/ClanWorld.sol:2221:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3683:packages/contracts/src/ClanWorld.sol:2225:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3684:packages/contracts/src/ClanWorld.sol:2414:            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3685:packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3686:packages/contracts/src/ClanWorld.sol:2416:            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3687:packages/contracts/src/ClanWorld.sol:2423:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3688:packages/contracts/src/ClanWorld.sol:2428:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3689:docs/reviews/r2/pr194-codereview-codex-5-4.md:430:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3690:docs/reviews/r2/pr194-codereview-codex-5-4.md:760:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3691:docs/reviews/r2/pr194-codereview-codex-5-4.md:762:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3692:docs/reviews/r2/pr194-codereview-codex-5-4.md:764:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3693:docs/reviews/r2/pr194-codereview-codex-5-4.md:765:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3694:docs/reviews/r2/pr194-codereview-codex-5-4.md:766:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3696:docs/reviews/r2/pr194-codereview-codex-5-4.md:783:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3697:docs/reviews/r2/pr194-codereview-codex-5-4.md:787:+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3698:docs/reviews/r2/pr194-codereview-codex-5-4.md:802:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:3701:docs/reviews/r2/pr194-codereview-codex-5-4.md:844:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3703:docs/reviews/r2/pr194-codereview-codex-5-4.md:1145:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3704:docs/reviews/r2/pr194-codereview-codex-5-4.md:1146:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3705:docs/reviews/r2/pr194-codereview-codex-5-4.md:1147:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3706:docs/reviews/r2/pr194-codereview-codex-5-4.md:1148:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3707:docs/reviews/r2/pr194-codereview-codex-5-4.md:1151:+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3710:docs/reviews/r2/pr194-codereview-codex-5-4.md:1191:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3711:docs/reviews/r2/pr194-codereview-codex-5-4.md:1266:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3717:docs/reviews/r2/pr194-codereview-codex-5-4.md:2419:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3718:docs/reviews/r2/pr194-codereview-codex-5-4.md:2450:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3719:docs/reviews/r2/pr194-codereview-codex-5-4.md:2460:+        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3721:docs/reviews/r2/pr194-codereview-codex-5-4.md:2558:-    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3722:docs/reviews/r2/pr194-codereview-codex-5-4.md:2562:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3723:docs/reviews/r2/pr194-codereview-codex-5-4.md:2634:+        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3724:docs/reviews/r2/pr194-codereview-codex-5-4.md:2666:-    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3725:docs/reviews/r2/pr194-codereview-codex-5-4.md:2671:-        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3726:docs/reviews/r2/pr194-codereview-codex-5-4.md:3196:+        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3727:docs/reviews/r2/pr194-codereview-codex-5-4.md:3206:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3728:docs/reviews/r2/pr194-codereview-codex-5-4.md:3444:+        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3729:docs/reviews/r2/pr194-codereview-codex-5-4.md:3445:+        _advanceUntil(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3730:docs/reviews/r2/pr194-codereview-codex-5-4.md:3446:+        uint64 deathFromTick = winterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3733:docs/reviews/r2/pr194-codereview-codex-5-4.md:4180:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr250-codereview-codex-5-4.md:3734:docs/reviews/r2/pr194-codereview-codex-5-4.md:4186:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3735:docs/reviews/r2/pr194-codereview-codex-5-4.md:4248:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3736:docs/reviews/r2/pr194-codereview-codex-5-4.md:4249:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr250-codereview-codex-5-4.md:3737:docs/reviews/r2/pr194-codereview-codex-5-4.md:4586:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3738:docs/reviews/r2/pr194-codereview-codex-5-4.md:4666:    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3739:docs/reviews/r2/pr194-codereview-codex-5-4.md:4667:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3740:docs/reviews/r2/pr194-codereview-codex-5-4.md:4668:    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3741:docs/reviews/r2/pr194-codereview-codex-5-4.md:4705:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3742:docs/reviews/r2/pr194-codereview-codex-5-4.md:4707:    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3743:docs/reviews/r2/pr194-codereview-codex-5-4.md:4859:///         Phase 2 (market execution) and Phase 3 (bandits, winter damage) are stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3744:docs/reviews/r2/pr194-codereview-codex-5-4.md:5100:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3745:docs/reviews/r2/pr194-codereview-codex-5-4.md:5122:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3746:docs/reviews/r2/pr194-codereview-codex-5-4.md:5123:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3747:docs/reviews/r2/pr194-codereview-codex-5-4.md:5125:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3748:docs/reviews/r2/pr194-codereview-codex-5-4.md:5126:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3749:docs/reviews/r2/pr194-codereview-codex-5-4.md:5133:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3750:docs/reviews/r2/pr194-codereview-codex-5-4.md:5606:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3757:docs/reviews/r2/pr194-codereview-codex-5-4.md:6182:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:3758:docs/reviews/r2/pr194-codereview-codex-5-4.md:6191:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3759:docs/reviews/r2/pr194-codereview-codex-5-4.md:6297:            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3760:docs/reviews/r2/pr194-codereview-codex-5-4.md:6298:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3761:docs/reviews/r2/pr194-codereview-codex-5-4.md:6299:            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3762:docs/reviews/r2/pr194-codereview-codex-5-4.md:6306:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr250-codereview-codex-5-4.md:3763:docs/reviews/r2/pr194-codereview-codex-5-4.md:6311:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3764:docs/reviews/r2/pr194-codereview-codex-5-4.md:6624:    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3765:docs/reviews/r2/pr194-codereview-codex-5-4.md:6625:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3766:docs/reviews/r2/pr194-codereview-codex-5-4.md:6626:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3767:docs/reviews/r2/pr194-codereview-codex-5-4.md:6852:    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:3770:docs/reviews/r2/pr194-codereview-codex-5-4.md:7072:    48	///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/pr250-codereview-codex-5-4.md:3771:docs/reviews/r2/pr194-codereview-codex-5-4.md:7160:   136	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3772:docs/reviews/r2/pr194-codereview-codex-5-4.md:7162:   138	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3773:docs/reviews/r2/pr194-codereview-codex-5-4.md:7163:   139	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3774:docs/reviews/r2/pr194-codereview-codex-5-4.md:7164:   140	        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3776:docs/reviews/r2/pr194-codereview-codex-5-4.md:7465:   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:3777:docs/reviews/r2/pr194-codereview-codex-5-4.md:7492:   463	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3778:docs/reviews/r2/pr194-codereview-codex-5-4.md:7493:   464	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3779:docs/reviews/r2/pr194-codereview-codex-5-4.md:7495:   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3780:docs/reviews/r2/pr194-codereview-codex-5-4.md:7496:   467	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3781:docs/reviews/r2/pr194-codereview-codex-5-4.md:7500:   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3782:docs/reviews/r2/pr194-codereview-codex-5-4.md:7515:   486	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr250-codereview-codex-5-4.md:3785:docs/reviews/r2/pr194-codereview-codex-5-4.md:7557:   528	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3786:docs/reviews/r2/pr194-codereview-codex-5-4.md:7627:   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3788:docs/reviews/r2/pr194-codereview-codex-5-4.md:8119:  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3789:docs/reviews/r2/pr194-codereview-codex-5-4.md:8120:  1086	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3790:docs/reviews/r2/pr194-codereview-codex-5-4.md:8121:  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:3791:docs/reviews/r2/pr194-codereview-codex-5-4.md:8122:  1088	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3792:docs/reviews/r2/pr194-codereview-codex-5-4.md:8125:  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3795:docs/reviews/r2/pr194-codereview-codex-5-4.md:8165:  1131	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3796:docs/reviews/r2/pr194-codereview-codex-5-4.md:8240:  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3802:docs/reviews/r2/pr194-codereview-codex-5-4.md:9293:  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:3804:docs/reviews/r2/pr194-codereview-codex-5-4.md:9341:  2302	            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:3805:docs/reviews/r2/pr194-codereview-codex-5-4.md:9342:  2303	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3806:docs/reviews/r2/pr194-codereview-codex-5-4.md:9343:  2304	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:3807:docs/reviews/r2/pr194-codereview-codex-5-4.md:9345:  2306	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3808:docs/reviews/r2/pr194-codereview-codex-5-4.md:9348:  2309	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:3809:docs/reviews/r2/pr194-codereview-codex-5-4.md:9350:  2311	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:3810:docs/reviews/r2/pr194-codereview-codex-5-4.md:9351:  2312	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:3811:docs/reviews/r2/pr194-codereview-codex-5-4.md:9353:  2314	            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:3812:docs/reviews/r2/pr194-codereview-codex-5-4.md:9356:  2317	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:3813:docs/reviews/r2/pr194-codereview-codex-5-4.md:9357:  2318	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3814:docs/reviews/r2/pr194-codereview-codex-5-4.md:9359:  2320	            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:3815:docs/reviews/r2/pr194-codereview-codex-5-4.md:9360:  2321	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:3816:docs/reviews/r2/pr194-codereview-codex-5-4.md:9362:  2323	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3817:docs/reviews/r2/pr194-codereview-codex-5-4.md:9364:  2325	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3818:docs/reviews/r2/pr194-codereview-codex-5-4.md:9365:  2326	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:3819:docs/reviews/r2/pr194-codereview-codex-5-4.md:9367:  2328	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:3820:docs/reviews/r2/pr194-codereview-codex-5-4.md:9368:  2329	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:3821:docs/reviews/r2/pr194-codereview-codex-5-4.md:9369:  2330	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:3822:docs/reviews/r2/pr194-codereview-codex-5-4.md:9433:  2389	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3829:docs/reviews/r2/pr194-codereview-codex-5-4.md:10385:  3336	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3830:docs/reviews/r2/pr194-codereview-codex-5-4.md:10442:  3393	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3831:docs/reviews/r2/pr194-codereview-codex-5-4.md:10443:  3394	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3832:docs/reviews/r2/pr194-codereview-codex-5-4.md:10444:  3395	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3833:docs/reviews/r2/pr194-codereview-codex-5-4.md:10631:   120	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3834:docs/reviews/r2/pr194-codereview-codex-5-4.md:10713:   202	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3835:docs/reviews/r2/pr194-codereview-codex-5-4.md:10714:   203	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3836:docs/reviews/r2/pr194-codereview-codex-5-4.md:10715:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3837:docs/reviews/r2/pr194-codereview-codex-5-4.md:10757:   241	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3838:docs/reviews/r2/pr194-codereview-codex-5-4.md:10759:   243	    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3839:docs/reviews/r2/pr194-codereview-codex-5-4.md:10930:   414	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3840:docs/reviews/r2/pr194-codereview-codex-5-4.md:10931:   415	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3841:docs/reviews/r2/pr194-codereview-codex-5-4.md:10932:   416	    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3842:docs/reviews/r2/pr194-codereview-codex-5-4.md:11010:   629	    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:3845:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:3846:docs/reviews/r2/pr194-codereview-codex-5-4.md:11197:    32	        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:3847:docs/reviews/r2/pr194-codereview-codex-5-4.md:11207:    42	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:3849:docs/reviews/r2/pr194-codereview-codex-5-4.md:13460:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3850:docs/reviews/r2/pr194-codereview-codex-5-4.md:13724:   305	            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3851:docs/reviews/r2/pr194-codereview-codex-5-4.md:13725:   306	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3852:docs/reviews/r2/pr194-codereview-codex-5-4.md:13726:   307	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3853:docs/reviews/r2/pr194-codereview-codex-5-4.md:13925:23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/reviews/pr250-codereview-codex-5-4.md:3854:docs/reviews/r2/pr194-codereview-codex-5-4.md:13972:   280	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3855:docs/reviews/r2/pr194-codereview-codex-5-4.md:13973:   281	        _advanceUntil(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-4.md:3856:docs/reviews/r2/pr194-codereview-codex-5-4.md:13974:   282	        uint64 deathFromTick = winterStart;
docs/reviews/pr250-codereview-codex-5-4.md:3859:docs/reviews/r2/pr194-codereview-codex-5-4.md:14702:    48	          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3860:docs/reviews/r2/pr194-codereview-codex-5-4.md:14703:    49	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3861:docs/reviews/r2/pr194-codereview-codex-5-4.md:14704:    50	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3862:docs/reviews/r2/pr194-codereview-codex-5-4.md:14817:    39	          { name: 'winterActive', type: 'bool' },
docs/reviews/pr250-codereview-codex-5-4.md:3863:docs/reviews/r2/pr194-codereview-codex-5-4.md:14818:    40	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3864:docs/reviews/r2/pr194-codereview-codex-5-4.md:14819:    41	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3865:docs/reviews/r2/pr194-codereview-codex-5-4.md:14868:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr250-codereview-codex-5-4.md:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
docs/reviews/pr250-codereview-codex-5-4.md:3867:docs/reviews/r2/pr194-codereview-codex-5-4.md:14957:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
docs/reviews/pr250-codereview-codex-5-4.md:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
docs/reviews/pr250-codereview-codex-5-4.md:3869:docs/reviews/r2/pr194-codereview-codex-5-4.md:14999:- `packages/contracts/src/ClanWorld.sol:471-473, 1072-1093` — Winter starvation deaths are keyed off the current world winter flags while replaying historical ticks. If a clan starves during winter but is only settled or viewed after winter ends, both `_applyUpkeep()` and `_simulateApplyUpkeep()` replay those winter ticks with `winterActive == false` and skip the deaths entirely. That breaks both mutating settlement and derived getters. Suggested fix: derive winter status from the replayed `tick` itself, not from the current `_world.winterActive` snapshot.
docs/reviews/pr250-codereview-codex-5-4.md:3870:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
docs/reviews/pr250-codereview-codex-5-4.md:3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
docs/reviews/pr250-codereview-codex-5-4.md:3872:docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
docs/reviews/pr250-codereview-codex-5-4.md:3873:docs/reviews/r2/pr194-synthesis.md:35:| M3 | `ClanWorld.sol:1206` (`_simulateResolveAction`) | 4.6 = **1/5** | MED | **Starvation check uses `_world.currentTick` instead of simulation tick.** Latent bug — current control flow prevents misalignment, but breaks if simulation structure changes. Use `sim.clan.starvationStartsAtTick <= tick`. |
docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/pr250-codereview-codex-5-4.md:3876:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
docs/reviews/pr250-codereview-codex-5-4.md:3877:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:3878:packages/contracts/src/IClanWorld.sol:203:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3879:packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3880:packages/contracts/src/IClanWorld.sol:205:    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:3881:packages/contracts/src/IClanWorld.sol:242:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:3882:packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:3883:packages/contracts/src/IClanWorld.sol:418:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:3884:packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3885:packages/contracts/src/IClanWorld.sol:420:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:3886:packages/contracts/src/IClanWorld.sol:618:    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:3889:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:3890:packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3891:packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3892:packages/contracts/src/ClanWorldStub.sol:57:        _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3894:packages/contracts/src/ClanWorldStub.sol:152:            starvationStartsAtTick: 0,
docs/reviews/pr250-codereview-codex-5-4.md:3895:packages/contracts/src/ClanWorldStub.sol:344:            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:3896:packages/contracts/src/ClanWorldStub.sol:345:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3897:packages/contracts/src/ClanWorldStub.sol:346:            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3898:docs/planning/phase-3-test-spec.md:105:- Clan B has starvation developing — unsettled penalty.
docs/reviews/pr250-codereview-codex-5-4.md:3899:docs/planning/phase-3-test-spec.md:173:**Setup:** Register worker as defender. Set worker HP to 0 (or advance starvation until death).
docs/reviews/pr250-codereview-codex-5-4.md:3900:docs/planning/phase-3-test-spec.md:175:**Action:** Trigger worker death (via heartbeat starvation or forced state).
docs/reviews/pr250-codereview-codex-5-4.md:3901:packages/contracts/abi/IClanWorld.json:479:              "name": "starvationStartsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3902:packages/contracts/abi/IClanWorld.json:600:                      "name": "starvationStartsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3903:packages/contracts/abi/IClanWorld.json:1164:                  "name": "starvationStartsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3904:packages/contracts/abi/IClanWorld.json:1933:              "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3905:packages/contracts/abi/IClanWorld.json:1938:              "name": "winterStartsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3906:packages/contracts/abi/IClanWorld.json:1943:              "name": "winterEndsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3907:packages/contracts/abi/IClanWorld.json:2065:              "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3908:packages/contracts/abi/IClanWorld.json:2070:              "name": "winterStartsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3909:packages/contracts/abi/IClanWorld.json:2075:              "name": "winterEndsAtTick",
docs/reviews/pr250-codereview-codex-5-4.md:3911:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:3912:docs/reviews/pr200-codereview-codex-5-4.md:741:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:3913:docs/reviews/pr200-codereview-codex-5-4.md:1718:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:3914:docs/reviews/pr200-codereview-codex-5-4.md:1720:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3915:docs/reviews/pr200-codereview-codex-5-4.md:1722:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3916:docs/reviews/pr200-codereview-codex-5-4.md:1723:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:3917:docs/reviews/pr200-codereview-codex-5-4.md:1724:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3918:docs/reviews/pr200-codereview-codex-5-4.md:1733:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:3937:docs/reviews/pr200-codereview-codex-5-4.md:2316:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:3938:docs/reviews/pr200-codereview-codex-5-4.md:2318:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:3939:docs/reviews/pr200-codereview-codex-5-4.md:2319:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:3940:docs/reviews/pr200-codereview-codex-5-4.md:2320:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:3944:docs/reviews/pr200-codereview-codex-5-4.md:3547:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:3945:docs/reviews/pr200-codereview-codex-5-4.md:3549:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:3946:docs/reviews/pr200-codereview-codex-5-4.md:3551:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:3947:docs/reviews/pr200-codereview-codex-5-4.md:3554:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:3948:docs/reviews/pr200-codereview-codex-5-4.md:3555:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:3949:docs/reviews/pr200-codereview-codex-5-4.md:3557:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:3950:docs/reviews/pr200-codereview-codex-5-4.md:3559:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:3951:docs/reviews/pr200-codereview-codex-5-4.md:3560:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:3960:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:3962:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:4000:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr250-codereview-codex-5-4.md:4011:docs/reviews/pr200-codereview-codex-5-4.md:4779:docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4037:docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4042:docs/reviews/pr200-codereview-codex-5-4.md:4850:docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr250-codereview-codex-5-4.md:4050:docs/reviews/pr200-codereview-codex-5-4.md:4864:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4054:docs/reviews/pr200-codereview-codex-5-4.md:4868:docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4069:docs/reviews/pr200-codereview-codex-5-4.md:4905:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4074:docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4079:docs/reviews/pr200-codereview-codex-5-4.md:4924:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr250-codereview-codex-5-4.md:4094:docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4098:docs/reviews/pr200-codereview-codex-5-4.md:5100:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:4099:docs/reviews/pr200-codereview-codex-5-4.md:5103:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr250-codereview-codex-5-4.md:4100:docs/reviews/pr200-codereview-codex-5-4.md:5108:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:4103:docs/reviews/pr200-codereview-codex-5-4.md:5138:- winter transition checks
docs/reviews/pr250-codereview-codex-5-4.md:4108:docs/reviews/pr200-codereview-codex-5-4.md:5622:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4114:docs/reviews/pr200-codereview-codex-5-4.md:6324:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4117:docs/reviews/pr200-codereview-codex-5-4.md:6376:- dead clan restrictions on OTC
docs/reviews/pr250-codereview-codex-5-4.md:4118:docs/reviews/pr200-codereview-codex-5-4.md:6399:- winter interaction hook
docs/reviews/pr250-codereview-codex-5-4.md:4119:docs/reviews/pr200-codereview-codex-5-4.md:6406:### 5.6 Implement starvation consequences
docs/reviews/pr250-codereview-codex-5-4.md:4120:docs/reviews/pr200-codereview-codex-5-4.md:6407:- next-tick starvation start
docs/reviews/pr250-codereview-codex-5-4.md:4122:docs/reviews/pr200-codereview-codex-5-4.md:6413:/bin/bash -lc "rg -n \"_settleClan\\(|submitClanOrders\\(|clan.lastSettledTick|starvationStartsAtTick|_resolveWorldEvents|_killClan|clanState = ClanState.DEAD|clanState "'!= ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-4.md:4123:docs/reviews/pr200-codereview-codex-5-4.md:6418:419:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4124:docs/reviews/pr200-codereview-codex-5-4.md:6419:420:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:4125:docs/reviews/pr200-codereview-codex-5-4.md:6420:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4126:docs/reviews/pr200-codereview-codex-5-4.md:6421:423:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4127:docs/reviews/pr200-codereview-codex-5-4.md:6422:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4128:docs/reviews/pr200-codereview-codex-5-4.md:6428:1033:        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4129:docs/reviews/pr200-codereview-codex-5-4.md:6431:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4130:docs/reviews/pr200-codereview-codex-5-4.md:6432:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4131:docs/reviews/pr200-codereview-codex-5-4.md:6735:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:4132:docs/reviews/pr200-codereview-codex-5-4.md:6757:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4133:docs/reviews/pr200-codereview-codex-5-4.md:6758:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:4134:docs/reviews/pr200-codereview-codex-5-4.md:6763:            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:4135:docs/reviews/pr200-codereview-codex-5-4.md:6765:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4136:docs/reviews/pr200-codereview-codex-5-4.md:6766:            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4137:docs/reviews/pr200-codereview-codex-5-4.md:6773:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4139:docs/reviews/pr200-codereview-codex-5-4.md:6877:        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:4141:docs/reviews/pr200-codereview-codex-5-4.md:6922:            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:4142:docs/reviews/pr200-codereview-codex-5-4.md:6923:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:4143:docs/reviews/pr200-codereview-codex-5-4.md:6924:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:4144:docs/reviews/pr200-codereview-codex-5-4.md:6926:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:4145:docs/reviews/pr200-codereview-codex-5-4.md:6929:        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:4146:docs/reviews/pr200-codereview-codex-5-4.md:6931:            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:4147:docs/reviews/pr200-codereview-codex-5-4.md:6932:                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:4148:docs/reviews/pr200-codereview-codex-5-4.md:6934:            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:4149:docs/reviews/pr200-codereview-codex-5-4.md:6937:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:4150:docs/reviews/pr200-codereview-codex-5-4.md:6938:            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:4151:docs/reviews/pr200-codereview-codex-5-4.md:6940:            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:4152:docs/reviews/pr200-codereview-codex-5-4.md:6941:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:4153:docs/reviews/pr200-codereview-codex-5-4.md:6943:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:4154:docs/reviews/pr200-codereview-codex-5-4.md:6945:                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:4155:docs/reviews/pr200-codereview-codex-5-4.md:6946:                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:4156:docs/reviews/pr200-codereview-codex-5-4.md:6948:                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:4157:docs/reviews/pr200-codereview-codex-5-4.md:6949:                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:4158:docs/reviews/pr200-codereview-codex-5-4.md:6950:                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:4159:docs/reviews/pr200-codereview-codex-5-4.md:7129:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:4179:docs/reviews/pr200-codereview-codex-5-4.md:7758:   660	### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4185:docs/planning/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:4186:docs/planning/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr250-codereview-codex-5-4.md:4187:docs/planning/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:4190:docs/planning/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/reviews/pr250-codereview-codex-5-4.md:4191:docs/planning/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/reviews/pr250-codereview-codex-5-4.md:4192:docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4193:docs/planning/battleplan-v1.txt:6:	•	Demo cadence: 20s/tick. Six-minute season at 18 ticks gets you through 1–2 winters’ worth of drama without burning judges’ attention.
docs/reviews/pr250-codereview-codex-5-4.md:4196:docs/reviews/pr198-codereview-codex-5-5.md:322:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-4.md:4197:docs/reviews/pr198-codereview-codex-5-5.md:747:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-4.md:4198:docs/reviews/pr198-codereview-codex-5-5.md:749:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-4.md:4199:docs/reviews/pr198-codereview-codex-5-5.md:751:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:4200:docs/reviews/pr198-codereview-codex-5-5.md:752:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:4201:docs/reviews/pr198-codereview-codex-5-5.md:753:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:4202:docs/reviews/pr198-codereview-codex-5-5.md:756:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4204:docs/reviews/pr198-codereview-codex-5-5.md:3197:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:4205:docs/reviews/pr198-codereview-codex-5-5.md:3199:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:4206:docs/reviews/pr198-codereview-codex-5-5.md:3201:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:4207:docs/reviews/pr198-codereview-codex-5-5.md:3204:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:4208:docs/reviews/pr198-codereview-codex-5-5.md:3205:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:4209:docs/reviews/pr198-codereview-codex-5-5.md:3207:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:4210:docs/reviews/pr198-codereview-codex-5-5.md:3209:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:4211:docs/reviews/pr198-codereview-codex-5-5.md:3210:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:4212:docs/reviews/pr198-codereview-codex-5-5.md:4028:   120	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:4213:docs/reviews/pr198-codereview-codex-5-5.md:4110:   202	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4214:docs/reviews/pr198-codereview-codex-5-5.md:4111:   203	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4215:docs/reviews/pr198-codereview-codex-5-5.md:4112:   204	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:4216:docs/reviews/pr198-codereview-codex-5-5.md:4149:   241	    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:4217:docs/reviews/pr198-codereview-codex-5-5.md:4151:   243	    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:4218:docs/reviews/pr198-codereview-codex-5-5.md:4322:   414	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4219:docs/reviews/pr198-codereview-codex-5-5.md:4323:   415	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4220:docs/reviews/pr198-codereview-codex-5-5.md:4324:   416	    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4221:docs/reviews/pr198-codereview-codex-5-5.md:4537:   629	    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:4226:docs/reviews/pr198-codereview-codex-5-5.md:4696:  1145	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4227:docs/reviews/pr198-codereview-codex-5-5.md:4771:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4232:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:4233:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:4239:docs/reviews/pr198-codereview-codex-5-5.md:7349:   397	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:4240:docs/reviews/pr198-codereview-codex-5-5.md:7371:   419	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4241:docs/reviews/pr198-codereview-codex-5-5.md:7372:   420	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:4242:docs/reviews/pr198-codereview-codex-5-5.md:7374:   422	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4243:docs/reviews/pr198-codereview-codex-5-5.md:7375:   423	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4244:docs/reviews/pr198-codereview-codex-5-5.md:7382:   430	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4245:docs/reviews/pr198-codereview-codex-5-5.md:7736:   124	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:4246:docs/reviews/pr198-codereview-codex-5-5.md:8158:  2297	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr250-codereview-codex-5-4.md:4247:docs/reviews/pr198-codereview-codex-5-5.md:8162:  2301	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4248:docs/reviews/pr198-codereview-codex-5-5.md:9237:   876	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:4249:docs/reviews/pr198-codereview-codex-5-5.md:9262:   901	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:4251:docs/reviews/pr198-codereview-codex-5-5.md:9785:  1033	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4252:docs/reviews/pr198-codereview-codex-5-5.md:9945:1033-        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4253:docs/reviews/pr198-codereview-codex-5-5.md:10193:   204	- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:4257:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:14:If a question is not answered here, the answer is: do whatever produces the most legible, dramatic, mobile-first demo. When in doubt, optimize for the 8 demo moments listed in §10.
docs/reviews/pr250-codereview-codex-5-4.md:4258:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:125:│  bar | winter | bandit warning    │
docs/reviews/pr250-codereview-codex-5-4.md:4259:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:147:- Winter indicator (snowflake icon if `winterActive`)
docs/reviews/pr250-codereview-codex-5-4.md:4260:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:616:| Top HUD bar | `getSnapshot` | tick advance, winter, bandit |
docs/reviews/pr250-codereview-codex-5-4.md:4261:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:797:- TopBar with tick clock, season bar, winter and bandit chips
docs/reviews/pr250-codereview-codex-5-4.md:4263:docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr250-codereview-codex-5-4.md:4264:docs/planning/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/reviews/pr250-codereview-codex-5-4.md:4265:docs/planning/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/reviews/pr250-codereview-codex-5-4.md:4266:docs/planning/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/reviews/pr250-codereview-codex-5-4.md:4267:docs/planning/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/reviews/pr250-codereview-codex-5-4.md:4268:docs/planning/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/reviews/pr250-codereview-codex-5-4.md:4269:docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr250-codereview-codex-5-4.md:4270:docs/planning/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/reviews/pr250-codereview-codex-5-4.md:4271:docs/planning/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/reviews/pr250-codereview-codex-5-4.md:4273:docs/planning/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/reviews/pr250-codereview-codex-5-4.md:4274:docs/planning/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/reviews/pr250-codereview-codex-5-4.md:4275:docs/planning/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/reviews/pr250-codereview-codex-5-4.md:4276:docs/planning/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/reviews/pr250-codereview-codex-5-4.md:4277:docs/planning/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/reviews/pr250-codereview-codex-5-4.md:4278:docs/planning/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/reviews/pr250-codereview-codex-5-4.md:4279:docs/planning/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/reviews/pr250-codereview-codex-5-4.md:4280:docs/planning/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/reviews/pr250-codereview-codex-5-4.md:4282:docs/planning/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/reviews/pr250-codereview-codex-5-4.md:4283:docs/planning/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/reviews/pr250-codereview-codex-5-4.md:4285:docs/planning/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/reviews/pr250-codereview-codex-5-4.md:4287:docs/planning/clanworld_numbered_implementation_plan.md:537:- winter
docs/reviews/pr250-codereview-codex-5-4.md:4289:docs/planning/clanworld_numbered_implementation_plan.md:564:12. winter  
docs/reviews/pr250-codereview-codex-5-4.md:4293:docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4294:docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr250-codereview-codex-5-4.md:4296:docs/planning/clanworld_v1_implementation_profile.md:242:- winter
docs/reviews/pr250-codereview-codex-5-4.md:4297:docs/planning/clanworld_v1_implementation_profile.md:298:- starvation
docs/reviews/pr250-codereview-codex-5-4.md:4298:docs/planning/clanworld_v1_implementation_profile.md:299:- winter
docs/reviews/pr250-codereview-codex-5-4.md:4299:docs/planning/clanworld_v1_implementation_profile.md:301:- elimination
docs/reviews/pr250-codereview-codex-5-4.md:4300:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:260:  winterActive: boolean;
docs/reviews/pr250-codereview-codex-5-4.md:4301:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:430:- The game rewards monument-building, but you must survive winters and bandits to get there.
docs/reviews/pr250-codereview-codex-5-4.md:4302:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:456:{winterActive ? "WINTER IS ACTIVE." : ""}
docs/reviews/pr250-codereview-codex-5-4.md:4303:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:559:      winterActive: snapshot.winterActive,
docs/reviews/pr250-codereview-codex-5-4.md:4304:docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:360:| `ColdDamageApplied` | (omit, unless we add winter UI) | |
docs/reviews/pr250-codereview-codex-5-4.md:4305:docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:361:| `ClansmanDiedFromCold` | (omit, unless we add winter UI) | |
docs/reviews/pr250-codereview-codex-5-4.md:4306:docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:708:  - `winterActive = false`, `winterStartsAtTick = 110`
docs/reviews/pr250-codereview-codex-5-4.md:4307:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/reviews/pr250-codereview-codex-5-4.md:4308:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr250-codereview-codex-5-4.md:4309:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/reviews/pr250-codereview-codex-5-4.md:4310:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/reviews/pr250-codereview-codex-5-4.md:4311:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:4314:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:4316:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4317:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/reviews/pr250-codereview-codex-5-4.md:4318:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:4319:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/reviews/pr250-codereview-codex-5-4.md:4320:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:4321:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/pr250-codereview-codex-5-4.md:4322:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/pr250-codereview-codex-5-4.md:4323:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4326:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/reviews/pr250-codereview-codex-5-4.md:4327:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr250-codereview-codex-5-4.md:4328:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/reviews/pr250-codereview-codex-5-4.md:4329:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/reviews/pr250-codereview-codex-5-4.md:4330:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/reviews/pr250-codereview-codex-5-4.md:4331:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr250-codereview-codex-5-4.md:4333:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/reviews/pr250-codereview-codex-5-4.md:4334:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/reviews/pr250-codereview-codex-5-4.md:4336:docs/planning/clanworld_v4_4_ui_indexer_getters.md:57:    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4337:docs/planning/clanworld_v4_4_ui_indexer_getters.md:58:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4338:docs/planning/clanworld_v4_4_ui_indexer_getters.md:59:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4339:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr250-codereview-codex-5-4.md:4340:docs/planning/clanworld_v4_1_addendum.md:20:## A2. First winter timing
docs/reviews/pr250-codereview-codex-5-4.md:4341:docs/planning/clanworld_v4_1_addendum.md:22:The first winter begins at **tick 110**.
docs/reviews/pr250-codereview-codex-5-4.md:4342:docs/planning/clanworld_v4_1_addendum.md:24:Thereafter, winter begins every 110 ticks:
docs/reviews/pr250-codereview-codex-5-4.md:4343:docs/planning/clanworld_v4_1_addendum.md:30:Each winter lasts **10 ticks**, as already specified in v4.
docs/reviews/pr250-codereview-codex-5-4.md:4344:docs/planning/clanworld_v4_1_addendum.md:81:## A6. Just-in-time winter logistics are intentionally punitive
docs/reviews/pr250-codereview-codex-5-4.md:4345:docs/planning/clanworld_v4_1_addendum.md:85:2. update starvation status for the next tick if shortages occur
docs/reviews/pr250-codereview-codex-5-4.md:4346:docs/planning/clanworld_v4_1_addendum.md:91:This means that during winter:
docs/reviews/pr250-codereview-codex-5-4.md:4347:docs/planning/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr250-codereview-codex-5-4.md:4348:docs/planning/clanworld_v4_1_addendum.md:156:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr250-codereview-codex-5-4.md:4349:docs/planning/clanworld_v4_1_addendum.md:158:This is intentional and follows the existing rule that starvation reduces all clansmen defense contribution to zero.
docs/reviews/pr250-codereview-codex-5-4.md:4350:docs/planning/clanworld_v4_1_addendum.md:166:## A10. No change to summer starvation lethality
docs/reviews/pr250-codereview-codex-5-4.md:4351:docs/planning/clanworld_v4_1_addendum.md:168:Starvation outside winter does **not** directly kill clansmen.
docs/reviews/pr250-codereview-codex-5-4.md:4352:docs/planning/clanworld_v4_1_addendum.md:170:While starving outside winter:
docs/reviews/pr250-codereview-codex-5-4.md:4353:docs/planning/clanworld_v4_1_addendum.md:174:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr250-codereview-codex-5-4.md:4354:docs/planning/clanworld_v4_1_addendum.md:176:This is intentional for v1 to avoid excessively punishing early-game elimination.
docs/reviews/pr250-codereview-codex-5-4.md:4357:docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/reviews/pr250-codereview-codex-5-4.md:4358:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:4359:docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr250-codereview-codex-5-4.md:4361:docs/planning/clanworld_v4_spec.md:353:2. update starvation status for the next tick if shortages occur
docs/reviews/pr250-codereview-codex-5-4.md:4362:docs/planning/clanworld_v4_spec.md:402:- starvation checks
docs/reviews/pr250-codereview-codex-5-4.md:4363:docs/planning/clanworld_v4_spec.md:403:- winter wood burn
docs/reviews/pr250-codereview-codex-5-4.md:4364:docs/planning/clanworld_v4_spec.md:475:- starvation status begins on the **next tick**
docs/reviews/pr250-codereview-codex-5-4.md:4365:docs/planning/clanworld_v4_spec.md:484:Outside Winter, starvation does **not** directly kill clansmen in v1.
docs/reviews/pr250-codereview-codex-5-4.md:4366:docs/planning/clanworld_v4_spec.md:486:## 4.13 Lazy starvation tracking
docs/reviews/pr250-codereview-codex-5-4.md:4367:docs/planning/clanworld_v4_spec.md:487:To keep heartbeat cheap, starvation should not require full clan mutation every tick.
docs/reviews/pr250-codereview-codex-5-4.md:4368:docs/planning/clanworld_v4_spec.md:490:- store starvation threshold timing or equivalent compact status data
docs/reviews/pr250-codereview-codex-5-4.md:4369:docs/planning/clanworld_v4_spec.md:491:- interpret starvation status lazily at clan settlement time and in views
docs/reviews/pr250-codereview-codex-5-4.md:4370:docs/planning/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr250-codereview-codex-5-4.md:4371:docs/planning/clanworld_v4_spec.md:832:During winter, each clan consumes:
docs/reviews/pr250-codereview-codex-5-4.md:4372:docs/planning/clanworld_v4_spec.md:838:During winter:
docs/reviews/pr250-codereview-codex-5-4.md:4373:docs/planning/clanworld_v4_spec.md:841:- regrowth does not complete during winter
docs/reviews/pr250-codereview-codex-5-4.md:4374:docs/planning/clanworld_v4_spec.md:843:At winter start:
docs/reviews/pr250-codereview-codex-5-4.md:4375:docs/planning/clanworld_v4_spec.md:845:- both plots restart regrowing after winter ends
docs/reviews/pr250-codereview-codex-5-4.md:4376:docs/planning/clanworld_v4_spec.md:846:- both plots become harvestable again `4` ticks after winter ends
docs/reviews/pr250-codereview-codex-5-4.md:4377:docs/planning/clanworld_v4_spec.md:849:During winter:
docs/reviews/pr250-codereview-codex-5-4.md:4378:docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr250-codereview-codex-5-4.md:4379:docs/planning/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/reviews/pr250-codereview-codex-5-4.md:4380:docs/planning/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/reviews/pr250-codereview-codex-5-4.md:4381:docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr250-codereview-codex-5-4.md:4382:docs/planning/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/reviews/pr250-codereview-codex-5-4.md:4385:docs/planning/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/reviews/pr250-codereview-codex-5-4.md:4386:docs/planning/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/reviews/pr250-codereview-codex-5-4.md:4387:docs/planning/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/reviews/pr250-codereview-codex-5-4.md:4388:docs/planning/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/reviews/pr250-codereview-codex-5-4.md:4390:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr250-codereview-codex-5-4.md:4391:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr250-codereview-codex-5-4.md:4393:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:353:2. update starvation status for the next tick if shortages occur
docs/reviews/pr250-codereview-codex-5-4.md:4394:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:402:- starvation checks
docs/reviews/pr250-codereview-codex-5-4.md:4395:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/reviews/pr250-codereview-codex-5-4.md:4396:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:475:- starvation status begins on the **next tick**
docs/reviews/pr250-codereview-codex-5-4.md:4397:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:484:Outside Winter, starvation does **not** directly kill clansmen in v1.
docs/reviews/pr250-codereview-codex-5-4.md:4398:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:486:## 4.13 Lazy starvation tracking
docs/reviews/pr250-codereview-codex-5-4.md:4399:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:487:To keep heartbeat cheap, starvation should not require full clan mutation every tick.
docs/reviews/pr250-codereview-codex-5-4.md:4400:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:490:- store starvation threshold timing or equivalent compact status data
docs/reviews/pr250-codereview-codex-5-4.md:4401:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:491:- interpret starvation status lazily at clan settlement time and in views
docs/reviews/pr250-codereview-codex-5-4.md:4402:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr250-codereview-codex-5-4.md:4403:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:832:During winter, each clan consumes:
docs/reviews/pr250-codereview-codex-5-4.md:4404:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:838:During winter:
docs/reviews/pr250-codereview-codex-5-4.md:4405:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:841:- regrowth does not complete during winter
docs/reviews/pr250-codereview-codex-5-4.md:4406:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843:At winter start:
docs/reviews/pr250-codereview-codex-5-4.md:4407:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:845:- both plots restart regrowing after winter ends
docs/reviews/pr250-codereview-codex-5-4.md:4408:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:846:- both plots become harvestable again `4` ticks after winter ends
docs/reviews/pr250-codereview-codex-5-4.md:4409:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:849:During winter:
docs/reviews/pr250-codereview-codex-5-4.md:4410:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr250-codereview-codex-5-4.md:4411:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/reviews/pr250-codereview-codex-5-4.md:4412:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/reviews/pr250-codereview-codex-5-4.md:4413:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr250-codereview-codex-5-4.md:4414:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/reviews/pr250-codereview-codex-5-4.md:4417:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/reviews/pr250-codereview-codex-5-4.md:4418:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/reviews/pr250-codereview-codex-5-4.md:4419:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/reviews/pr250-codereview-codex-5-4.md:4420:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/reviews/pr250-codereview-codex-5-4.md:4422:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/reviews/pr250-codereview-codex-5-4.md:4423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr250-codereview-codex-5-4.md:4424:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/reviews/pr250-codereview-codex-5-4.md:4425:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/reviews/pr250-codereview-codex-5-4.md:4426:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:4429:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/reviews/pr250-codereview-codex-5-4.md:4431:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4432:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/reviews/pr250-codereview-codex-5-4.md:4433:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:4434:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/reviews/pr250-codereview-codex-5-4.md:4435:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:4436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/pr250-codereview-codex-5-4.md:4437:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/pr250-codereview-codex-5-4.md:4438:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr250-codereview-codex-5-4.md:4441:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/reviews/pr250-codereview-codex-5-4.md:4442:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr250-codereview-codex-5-4.md:4443:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/reviews/pr250-codereview-codex-5-4.md:4444:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/reviews/pr250-codereview-codex-5-4.md:4445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/reviews/pr250-codereview-codex-5-4.md:4446:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr250-codereview-codex-5-4.md:4448:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/reviews/pr250-codereview-codex-5-4.md:4449:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/reviews/pr250-codereview-codex-5-4.md:4451:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr250-codereview-codex-5-4.md:4452:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/reviews/pr250-codereview-codex-5-4.md:4453:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/reviews/pr250-codereview-codex-5-4.md:4454:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/reviews/pr250-codereview-codex-5-4.md:4455:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/reviews/pr250-codereview-codex-5-4.md:4456:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/reviews/pr250-codereview-codex-5-4.md:4457:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/reviews/pr250-codereview-codex-5-4.md:4458:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/reviews/pr250-codereview-codex-5-4.md:4459:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/reviews/pr250-codereview-codex-5-4.md:4461:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/reviews/pr250-codereview-codex-5-4.md:4462:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/reviews/pr250-codereview-codex-5-4.md:4463:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/reviews/pr250-codereview-codex-5-4.md:4464:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/reviews/pr250-codereview-codex-5-4.md:4465:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/reviews/pr250-codereview-codex-5-4.md:4466:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/reviews/pr250-codereview-codex-5-4.md:4467:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/reviews/pr250-codereview-codex-5-4.md:4468:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/reviews/pr250-codereview-codex-5-4.md:4470:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/reviews/pr250-codereview-codex-5-4.md:4471:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/reviews/pr250-codereview-codex-5-4.md:4473:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/reviews/pr250-codereview-codex-5-4.md:4475:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:537:- winter
docs/reviews/pr250-codereview-codex-5-4.md:4477:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:564:12. winter  
docs/reviews/pr250-codereview-codex-5-4.md:4478:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:4479:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr250-codereview-codex-5-4.md:4480:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/reviews/pr250-codereview-codex-5-4.md:4483:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/reviews/pr250-codereview-codex-5-4.md:4484:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/reviews/pr250-codereview-codex-5-4.md:4485:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4489:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr250-codereview-codex-5-4.md:4490:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr250-codereview-codex-5-4.md:4492:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:242:- winter
docs/reviews/pr250-codereview-codex-5-4.md:4493:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:298:- starvation
docs/reviews/pr250-codereview-codex-5-4.md:4494:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:299:- winter
docs/reviews/pr250-codereview-codex-5-4.md:4495:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:301:- elimination
docs/reviews/pr250-codereview-codex-5-4.md:4496:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:57:    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4497:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:58:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4498:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:59:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4499:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr250-codereview-codex-5-4.md:4500:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:20:## A2. First winter timing
docs/reviews/pr250-codereview-codex-5-4.md:4501:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:22:The first winter begins at **tick 110**.
docs/reviews/pr250-codereview-codex-5-4.md:4502:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:24:Thereafter, winter begins every 110 ticks:
docs/reviews/pr250-codereview-codex-5-4.md:4503:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:30:Each winter lasts **10 ticks**, as already specified in v4.
docs/reviews/pr250-codereview-codex-5-4.md:4504:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:81:## A6. Just-in-time winter logistics are intentionally punitive
docs/reviews/pr250-codereview-codex-5-4.md:4505:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:85:2. update starvation status for the next tick if shortages occur
docs/reviews/pr250-codereview-codex-5-4.md:4506:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:91:This means that during winter:
docs/reviews/pr250-codereview-codex-5-4.md:4507:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr250-codereview-codex-5-4.md:4508:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:157:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr250-codereview-codex-5-4.md:4509:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:159:This is intentional and follows the existing rule that starvation reduces all clansmen defense contribution to zero.
docs/reviews/pr250-codereview-codex-5-4.md:4510:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:167:## A10. No change to summer starvation lethality
docs/reviews/pr250-codereview-codex-5-4.md:4511:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:169:Starvation outside winter does **not** directly kill clansmen.
docs/reviews/pr250-codereview-codex-5-4.md:4512:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:171:While starving outside winter:
docs/reviews/pr250-codereview-codex-5-4.md:4513:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:175:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr250-codereview-codex-5-4.md:4514:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:177:This is intentional for v1 to avoid excessively punishing early-game elimination.
docs/reviews/pr250-codereview-codex-5-4.md:4515:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:118:    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:4516:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:198:    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4517:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:199:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4518:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:200:    uint64 winterEndsAtTick;     // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:4519:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:237:    uint64 starvationStartsAtTick; // 0 = none
docs/reviews/pr250-codereview-codex-5-4.md:4520:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:239:    uint16 coldDamage;             // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-4.md:4521:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:407:    bool   winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:4522:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:408:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4523:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:409:    uint64 winterEndsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:4524:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:630:    // ----- winter cold damage -----
docs/reviews/pr250-codereview-codex-5-4.md:4527:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:794:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr250-codereview-codex-5-4.md:4534:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:243:GAMMA: { strategy: ‘Survive winter. Convert wheat surplus into defense contracts.’,
docs/reviews/pr250-codereview-codex-5-4.md:4535:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:246:goals: [‘Survive winter’, ‘Stabilize food’] },
docs/reviews/pr250-codereview-codex-5-4.md:4536:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:258:{ kind: ‘STARVATION’, text: ‘GAMMA below food threshold, starvation flag set’ },
docs/reviews/pr250-codereview-codex-5-4.md:4539:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:153:4. **Add 0G Compute SDK integration** with `@0glabs/0g-serving-broker`. Build `elder act` to optionally capture TEE attestations. Decision pending Discord answers — see §10.
docs/reviews/pr250-codereview-codex-5-4.md:4540:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr250-codereview-codex-5-4.md:4541:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:383:- `clan:{id}:notebook/winter_reflections/{seasonNum}` — TBD, see §11
docs/reviews/pr250-codereview-codex-5-4.md:4544:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:612:| 8 | Winter self-reflection: agent updates own learnings doc | Agent design | Post-MVP / experimentation | Idea: during winter (low activity), agents prompted to consolidate KV notebook into a self-curated "lessons learned" doc. Open whether this is just a notebook key or something more structured. Defer until we see how agents naturally use KV |
docs/reviews/pr250-codereview-codex-5-4.md:4553:I’ve got the contract surface and spec references loaded. Next I’m drilling into the exact winter/death execution paths locally, while two subagents independently check spec timing and cross-phase dead-clan interactions so we don’t miss a seam regression.
docs/reviews/pr250-codereview-codex-5-4.md:4569:   349	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr250-codereview-codex-5-4.md:4581:   361	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr250-codereview-codex-5-4.md:4663:   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-4.md:4685:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4686:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr250-codereview-codex-5-4.md:4688:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr250-codereview-codex-5-4.md:4689:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:4696:   476	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr250-codereview-codex-5-4.md:4854:   909	    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:4879:   934	        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr250-codereview-codex-5-4.md:4924:   979	            // reset winter timers for new season
docs/reviews/pr250-codereview-codex-5-4.md:4925:   980	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:4926:   981	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:4928:   983	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:4931:   986	        // --- winter transitions (timer only; mechanics = Phase 10) ---
docs/reviews/pr250-codereview-codex-5-4.md:4933:   988	            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-4.md:4934:   989	                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-4.md:4936:   991	            _world.winterActive = true;
docs/reviews/pr250-codereview-codex-5-4.md:4939:   994	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
docs/reviews/pr250-codereview-codex-5-4.md:4940:   995	            _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-4.md:4942:   997	            // schedule next winter cycle within this season
docs/reviews/pr250-codereview-codex-5-4.md:4943:   998	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-4.md:4945:  1000	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-4.md:4947:  1002	                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-4.md:4948:  1003	                _world.winterEndsAtTick = nextWinterEnd;
docs/reviews/pr250-codereview-codex-5-4.md:4950:  1005	                // no more winters this season; sentinel = seasonEndTick so guard never fires
docs/reviews/pr250-codereview-codex-5-4.md:4951:  1006	                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:4952:  1007	                _world.winterEndsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-4.md:5011:  1066	        clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-4.md:5583:    29	    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr250-codereview-codex-5-4.md:5674:   120	    WinterLocked
docs/reviews/pr250-codereview-codex-5-4.md:5757:   203	    bool winterActive;
docs/reviews/pr250-codereview-codex-5-4.md:5758:   204	    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-4.md:5759:   205	    uint64 winterEndsAtTick; // 0 if not active
docs/reviews/pr250-codereview-codex-5-4.md:5839:  2020	    // Phase 4.4 — season + winter timer tests
docs/reviews/pr250-codereview-codex-5-4.md:5848:  2029	            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:5849:  2030	            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:5851:  2032	        assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-4.md:5854:  2035	    function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-4.md:5855:  2036	        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-4.md:5856:  2037	        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-4.md:5858:  2039	        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-4.md:5859:  2040	            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-4.md:5860:  2041	        for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:5870:  2051	        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-4.md:5873:  2054	            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
docs/reviews/pr250-codereview-codex-5-4.md:5879:  2060	        assertTrue(world.getWorldState().winterActive, "winter should be active");
docs/reviews/pr250-codereview-codex-5-4.md:5880:  2061	        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
docs/reviews/pr250-codereview-codex-5-4.md:5883:  2064	    function test_winter_end_and_next_cycle() public {
docs/reviews/pr250-codereview-codex-5-4.md:5884:  2065	        // Advance past first winter end tick (= 110)
docs/reviews/pr250-codereview-codex-5-4.md:5885:  2066	        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-4.md:5886:  2067	        for (uint64 i = 0; i <= winterEnd; i++) {
docs/reviews/pr250-codereview-codex-5-4.md:5891:  2072	        assertFalse(ws.winterActive, "winter should be over");
docs/reviews/pr250-codereview-codex-5-4.md:5892:  2073	        // next winter at [210, 220)
docs/reviews/pr250-codereview-codex-5-4.md:5894:  2075	            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-4.md:5895:  2076	            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-4.md:5909:  2090	        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-4.md:5911:  2092	            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-4.md:5912:  2093	        assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:28:10. `clanworld_agent_runner_spec.md` — agent runner *(see §11 of THIS addendum: tool-use architecture in §7-§11 of agent runner spec is deprecated; Submission 1 uses the simpler `elder` CLI per §11.4 below)*
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:399:## 10. What to do next
docs/planning/V1/07 clanworld_v4_5_alignment_addendum.md:555:10. **Demo script** — the 3-minute Track 2 video, beat by beat, ending on the iNFT transfer moment
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:14:If a question is not answered here, the answer is: do whatever produces the most legible, dramatic, mobile-first demo. When in doubt, optimize for the 8 demo moments listed in §10.
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:47:| `gold-bright` | `#D4A24C` | Highlights, leaderboard winner |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:70:| 3 | `#D4A24C` | Gold |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:73:| 6 | `#A85A2C` | Copper |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:125:│  bar | winter | bandit warning    │
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:147:- Winter indicator (snowflake icon if `winterActive`)
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:616:| Top HUD bar | `getSnapshot` | tick advance, winter, bandit |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:661:    "@use-gesture/react": "^10.x",
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:778:## 10. Build Milestones
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:797:- TopBar with tick clock, season bar, winter and bandit chips
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:260:  winterActive: boolean;
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:430:- The game rewards monument-building, but you must survive winters and bandits to get there.
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:456:{winterActive ? "WINTER IS ACTIVE." : ""}
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:559:      winterActive: snapshot.winterActive,
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:625:## 10. Validation Layer
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:360:| `ColdDamageApplied` | (omit, unless we add winter UI) | |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:361:| `ClansmanDiedFromCold` | (omit, unless we add winter UI) | |
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:687:## 10. Mock Mode
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:691:### 10.1 What mock mode replaces
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:701:### 10.2 `seedMockState` — initial state
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:708:  - `winterActive = false`, `winterStartsAtTick = 110`
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:723:### 10.3 `advanceMockTick` — per-tick mock advancement
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:766:### 10.4 Mock mode runtime
docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:780:### 10.5 Why this is worth building
docs/planning/V1/02 Frontend Spec/clanworld_master_coordination.md:115:Backend spec §12 asks: when `heartbeat()` runs, does `TickAdvanced` fire **before** or **after** other tick-end side effects (bandit attacks, scheduled markets, winter transitions)?
docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md:69:| 0G Storage Reward contract | `0xA97B57b4BdFEA2D0a25e535bd849ad4e6C440A69` |
docs/planning/V1/05 0G/clanworld_inft_deployment_notes.md:483:## 10. Glossary (incremental — see other specs for full)
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:153:4. **Add 0G Compute SDK integration** with `@0glabs/0g-serving-broker`. Build `elder act` to optionally capture TEE attestations. Decision pending Discord answers — see §10.
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:383:- `clan:{id}:notebook/winter_reflections/{seasonNum}` — TBD, see §11
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:566:## 10. Model selection — DECISION PENDING (Discord answers)
docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:612:| 8 | Winter self-reflection: agent updates own learnings doc | Agent design | Post-MVP / experimentation | Idea: during winter (low activity), agents prompted to consolidate KV notebook into a self-curated "lessons learned" doc. Open whether this is just a notebook key or something more structured. Defer until we see how agents naturally use KV |
docs/planning/V1/05 0G/clanworld_clan_memory_spec.md:530:## 10. Testing strategy
docs/planning/V1/05 0G/clanworld_clan_identity_spec.md:481:## 10. Convenience helpers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
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
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:353:2. update starvation status for the next tick if shortages occur
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:402:- starvation checks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:403:- winter wood burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:475:- starvation status begins on the **next tick**
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:477:## 4.12 Starvation effects
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
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:854:## 7.6 Winter failure: cold damage
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:985:A season does **not** end immediately when a clan first reaches monument level 10.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1003:# 10. Human Steering Rules
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1005:## 10.1 Steering philosophy
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1010:## 10.2 Steering channels
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1023:## 10.3 Strategic alignment structure
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1041:## 10.4 No v1 restrictions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:244:uint64 TICKS_PER_WINTER_CYCLE = 110;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:632:## 10. Write interfaces and access control
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:634:### 10.1 Access control rules
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:649:### 10.2 Core write functions
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:687:### 10.4 ClanOrder
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:702:### 10.5 OrderResult
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:713:### 10.6 Pool seeding config
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:728:### 10.7 Prize pot routing note
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:424:### 10.3 Implement cold damage
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:435:### 10.5 Implement clan death
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:537:- winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:564:12. winter  
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:242:- winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:298:- starvation
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:299:- winter
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:300:- cold damage
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:301:- elimination
docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:341:## 10. Guiding principle
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
docs/planning/clanworld_v4_5_alignment_addendum.md:28:10. `clanworld_agent_runner_spec.md` — agent runner *(see §11 of THIS addendum: tool-use architecture in §7-§11 of agent runner spec is deprecated; Submission 1 uses the simpler `elder` CLI per §11.4 below)*
docs/planning/clanworld_v4_5_alignment_addendum.md:399:## 10. What to do next
docs/planning/clanworld_v4_5_alignment_addendum.md:555:10. **Demo script** — the 3-minute Track 2 video, beat by beat, ending on the iNFT transfer moment
docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/planning/clanworld_v4_spec.md:353:2. update starvation status for the next tick if shortages occur
docs/planning/clanworld_v4_spec.md:402:- starvation checks
docs/planning/clanworld_v4_spec.md:403:- winter wood burn
docs/planning/clanworld_v4_spec.md:475:- starvation status begins on the **next tick**
docs/planning/clanworld_v4_spec.md:477:## 4.12 Starvation effects
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
docs/planning/clanworld_v4_spec.md:854:## 7.6 Winter failure: cold damage
docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/planning/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/planning/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/planning/clanworld_v4_spec.md:985:A season does **not** end immediately when a clan first reaches monument level 10.
docs/planning/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/planning/clanworld_v4_spec.md:1003:# 10. Human Steering Rules
docs/planning/clanworld_v4_spec.md:1005:## 10.1 Steering philosophy
docs/planning/clanworld_v4_spec.md:1010:## 10.2 Steering channels
docs/planning/clanworld_v4_spec.md:1023:## 10.3 Strategic alignment structure
docs/planning/clanworld_v4_spec.md:1041:## 10.4 No v1 restrictions
docs/planning/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/planning/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/planning/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/planning/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/planning/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:244:uint64 TICKS_PER_WINTER_CYCLE = 110;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:632:## 10. Write interfaces and access control
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:634:### 10.1 Access control rules
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:649:### 10.2 Core write functions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:687:### 10.4 ClanOrder
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:702:### 10.5 OrderResult
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:713:### 10.6 Pool seeding config
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:728:### 10.7 Prize pot routing note
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/planning/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/planning/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/planning/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/planning/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/planning/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/planning/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/planning/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/planning/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/planning/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/planning/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/planning/clanworld_numbered_implementation_plan.md:424:### 10.3 Implement cold damage
docs/planning/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/planning/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/planning/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/planning/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/planning/clanworld_numbered_implementation_plan.md:435:### 10.5 Implement clan death
docs/planning/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/planning/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/planning/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/planning/clanworld_numbered_implementation_plan.md:537:- winter
docs/planning/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/planning/clanworld_numbered_implementation_plan.md:564:12. winter  
docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/planning/clanworld_v1_implementation_profile.md:242:- winter
docs/planning/clanworld_v1_implementation_profile.md:298:- starvation
docs/planning/clanworld_v1_implementation_profile.md:299:- winter
docs/planning/clanworld_v1_implementation_profile.md:300:- cold damage
docs/planning/clanworld_v1_implementation_profile.md:301:- elimination
docs/planning/clanworld_v1_implementation_profile.md:341:## 10. Guiding principle
docs/reviews/pr250-codereview-codex-5-5.md:17:Senior staff engineer FINAL pre-merge review for PR #250 (Phase 10 — Winter + Elimination) at head e4a0d4c.
docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr250-codereview-codex-5-5.md:22:1. Cross-cutting bugs at boundary ticks (winter start/end transitions)
docs/reviews/pr250-codereview-codex-5-5.md:23:2. Spec compliance §A2 winter timing + §4.12 starvation + §10 elimination
docs/reviews/pr250-codereview-codex-5-5.md:24:3. RNG hygiene (cold_damage domain, deterministic clansman death pick)
docs/reviews/pr250-codereview-codex-5-5.md:25:4. Clan death helper used by Phase 5.6 starvation + Phase 9.4 attack + 10.3 cold damage
docs/reviews/pr250-codereview-codex-5-5.md:26:5. Cross-phase contracts: Phase 7.5 OTC dead-clan restrict reads ClanState.DEAD; Phase 4.4 winter timer plumbing already in dev
docs/reviews/pr250-codereview-codex-5-5.md:27:6. Test coverage on winter boundary edges + clan-death paths
docs/reviews/pr250-codereview-codex-5-5.md:243:               "name": "winterActive",
docs/reviews/pr250-codereview-codex-5-5.md:463:+    /// @dev Caps winter crop boundary work: 24 clans x 2 wheat plots = 48 plot writes.
docs/reviews/pr250-codereview-codex-5-5.md:472:-        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-5.md:474:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-5.md:475:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-5.md:476:-        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-5.md:477:+        _world.winterStartsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:478:+        _world.winterEndsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:479:         _world.winterActive = false;
docs/reviews/pr250-codereview-codex-5-5.md:502:-    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr250-codereview-codex-5-5.md:503:+    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
docs/reviews/pr250-codereview-codex-5-5.md:505:+        bool winter = _isWinterActiveAt(tick);
docs/reviews/pr250-codereview-codex-5-5.md:506:+        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
docs/reviews/pr250-codereview-codex-5-5.md:514:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-5.md:522:             clan.starvationStartsAtTick = 0;
docs/reviews/pr250-codereview-codex-5-5.md:526:+        (, uint64 winterStartsAtTick,) = _winterWindowForTick(tick);
docs/reviews/pr250-codereview-codex-5-5.md:527:+        if (winter && starving && clan.starvationStartsAtTick >= winterStartsAtTick) {
docs/reviews/pr250-codereview-codex-5-5.md:532:+        if (winter) {
docs/reviews/pr250-codereview-codex-5-5.md:615:+                _markClanDead(clan.clanId, "starvation", tick);
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
docs/reviews/pr250-codereview-codex-5-5.md:784:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-5.md:787:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:791:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-5.md:796:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-5.md:797:+            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:798:+            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:802:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:804:+        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:812:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-codereview-codex-5-5.md:916:-            winterActive: _world.winterActive,
docs/reviews/pr250-codereview-codex-5-5.md:917:-            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:918:-            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:927:+            winterActive: ws.winterActive,
docs/reviews/pr250-codereview-codex-5-5.md:928:+            winterStartsAtTick: ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:929:+            winterEndsAtTick: ws.winterEndsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:943:-        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-5.md:944:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
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
docs/reviews/pr250-codereview-codex-5-5.md:1018:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-5.md:1021:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1025:+    function _winterEventTick(uint64 tick) internal pure returns (uint32) {
docs/reviews/pr250-codereview-codex-5-5.md:1026:+        require(tick <= type(uint32).max, "ClanWorldStub: winter tick overflow");
docs/reviews/pr250-codereview-codex-5-5.md:1031:+    function _winterWindowForTick(uint64 tick)
docs/reviews/pr250-codereview-codex-5-5.md:1036:+        if (tick < ClanWorldConstants.WINTER_START_TICK) {
docs/reviews/pr250-codereview-codex-5-5.md:1037:+            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1038:+            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1042:+        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1044:+        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1052:+        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) = _winterWindowForTick(_world.currentTick);
docs/reviews/pr250-codereview-codex-5-5.md:1063:-    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr250-codereview-codex-5-5.md:1064:+    uint64 internal constant WINTER_START_TICK = 110;
docs/reviews/pr250-codereview-codex-5-5.md:1131:+    /// @notice True iff currentTick is inside the recurring winter window.
docs/reviews/pr250-codereview-codex-5-5.md:1145:+    uint256 internal constant DOMAIN_COLD_DAMAGE = uint256(keccak256("cold_damage"));
docs/reviews/pr250-codereview-codex-5-5.md:1282:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1283:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-5.md:1285:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-5.md:1286:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1287:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-5.md:1293:+        assertEq(snapshot.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-5.md:1294:+        assertEq(snapshot.winterStartsAtTick, 110);
docs/reviews/pr250-codereview-codex-5-5.md:1297:     function test_winter_onset() public {
docs/reviews/pr250-codereview-codex-5-5.md:1298:-        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-5.md:1299:-        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-5.md:1301:-        uint64 winterStart =
docs/reviews/pr250-codereview-codex-5-5.md:1302:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
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
docs/reviews/pr250-codereview-codex-5-5.md:1362:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-5.md:1364:+        assertFalse(world.isWinter(), "isWinter should be false after winter end");
docs/reviews/pr250-codereview-codex-5-5.md:1366:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1369:+    function test_winter_restarts_after_full_period() public {
docs/reviews/pr250-codereview-codex-5-5.md:1371:+        uint64 nextWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS;
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
docs/reviews/pr250-codereview-codex-5-5.md:1476:+        assertEq(clan.coldDamage, 0, "sufficient wood should avoid cold damage");
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
docs/reviews/pr250-codereview-codex-5-5.md:1514:+        assertEq(clan.coldDamage, 2, "cold damage should increment");
docs/reviews/pr250-codereview-codex-5-5.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr250-codereview-codex-5-5.md:1523:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1524:+        _advanceToTick(winterStart + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1526:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-5.md:1533:+        assertEq(clan.coldDamage, 2, "cold damage should hit death threshold");
docs/reviews/pr250-codereview-codex-5-5.md:1548:+    function test_clanDeath_starvationMarksDeadBurnsVaultAndPreservesGold() public {
docs/reviews/pr250-codereview-codex-5-5.md:1552:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1555:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-5.md:1556:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1564:+        assertEq(uint8(clan.clanState), uint8(ClanState.DEAD), "starvation should mark clan dead");
docs/reviews/pr250-codereview-codex-5-5.md:1570:+        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
docs/reviews/pr250-codereview-codex-5-5.md:1571:+        _assertClanDiedLog(logs, clanId, winterStart + 3, "starvation");
docs/reviews/pr250-codereview-codex-5-5.md:1578:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1580:+        _advanceToTick(winterStart + 7);
docs/reviews/pr250-codereview-codex-5-5.md:1582:+        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
docs/reviews/pr250-codereview-codex-5-5.md:1596:+        assertEq(clan.goldBalance, goldBalance, "gold should survive clan death");
docs/reviews/pr250-codereview-codex-5-5.md:1597:+        _assertClanDiedLog(logs, clanId, winterStart + 6, "cold");
docs/reviews/pr250-codereview-codex-5-5.md:1605:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1606:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-5.md:1607:+        harness.setClanUpkeepState(clanId, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1619:+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
docs/reviews/pr250-codereview-codex-5-5.md:1622:+        _advanceToTick(winterStart + 4);
docs/reviews/pr250-codereview-codex-5-5.md:1623:+        harness.setClanUpkeepState(clanIdB, winterStart, 100e18, 0, 0, 0);
docs/reviews/pr250-codereview-codex-5-5.md:1637:+    function test_winter_upkeep_returnsToNormalAfterWinter() public {
docs/reviews/pr250-codereview-codex-5-5.md:1641:+        uint64 winterEnd = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1642:+        _advanceToTick(winterEnd + 1);
docs/reviews/pr250-codereview-codex-5-5.md:1644:+        harness.setClanUpkeepState(clanId, winterEnd, 100e18, 100e18, 100e18, 2);
docs/reviews/pr250-codereview-codex-5-5.md:1649:+        assertEq(clan.vaultWheat, 96e18, "post-winter wheat upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-5.md:1650:+        assertEq(clan.vaultFish, 100e18 - 4e17, "post-winter fish upkeep should be normal");
docs/reviews/pr250-codereview-codex-5-5.md:1651:+        assertEq(clan.vaultWood, 100e18, "post-winter should not burn wood");
docs/reviews/pr250-codereview-codex-5-5.md:1652:+        assertEq(clan.coldDamage, 0, "cold damage should reset after winter");
docs/reviews/pr250-codereview-codex-5-5.md:1660:-        // winter reset for new season
docs/reviews/pr250-codereview-codex-5-5.md:1662:-            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1663:+        // winter is derived from the global recurring schedule
docs/reviews/pr250-codereview-codex-5-5.md:1664:+        uint64 expectedWinterStart = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_PERIOD_TICKS * 3;
docs/reviews/pr250-codereview-codex-5-5.md:1665:         assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-5.md:1677:-            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1678:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-5.md:1680:-        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-5.md:1681:+        assertEq(ws.winterStartsAtTick, ClanWorldConstants.WINTER_START_TICK);
docs/reviews/pr250-codereview-codex-5-5.md:1682:+        assertEq(ws.winterEndsAtTick, ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS);
docs/reviews/pr250-codereview-codex-5-5.md:1683:         assertFalse(ws.winterActive);
docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
docs/reviews/pr250-codereview-codex-5-5.md:1749:/bin/bash -lc "rg -n \"function _markClanDead|_markClansmanDead|ClanEliminated|ERR_CLAN_DEAD|ClanState.DEAD|settleClan\\(|getClanFullView|enum ClanState|struct Clan|coldDamage|WinterLocked|WinterStarted|WinterEnded|winterStartsAtTick|TICKS_PER_WINTER_CYCLE|WINTER_START_TICK\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/pr250-codereview-codex-5-5.md:1751:packages/contracts/src/ClanWorld.sol:133:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr250-codereview-codex-5-5.md:1752:packages/contracts/src/ClanWorld.sol:135:        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-5.md:1753:packages/contracts/src/ClanWorld.sol:136:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-5.md:1754:packages/contracts/src/ClanWorld.sol:137:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-5.md:1758:packages/contracts/src/ClanWorld.sol:981:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-5.md:1759:packages/contracts/src/ClanWorld.sol:983:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-5.md:1760:packages/contracts/src/ClanWorld.sol:988:            !_world.winterActive && newTick >= _world.winterStartsAtTick
docs/reviews/pr250-codereview-codex-5-5.md:1761:packages/contracts/src/ClanWorld.sol:989:                && _world.winterStartsAtTick < _world.seasonEndTick
docs/reviews/pr250-codereview-codex-5-5.md:1764:packages/contracts/src/ClanWorld.sol:998:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr250-codereview-codex-5-5.md:1765:packages/contracts/src/ClanWorld.sol:1000:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-5.md:1766:packages/contracts/src/ClanWorld.sol:1002:                _world.winterStartsAtTick = nextWinterStart;
docs/reviews/pr250-codereview-codex-5-5.md:1767:packages/contracts/src/ClanWorld.sol:1006:                _world.winterStartsAtTick = _world.seasonEndTick;
docs/reviews/pr250-codereview-codex-5-5.md:1775:packages/contracts/src/ClanWorld.sol:2415:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1778:packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1779:packages/contracts/test/ClanWorldStub.t.sol:47:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-5.md:1780:packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
docs/reviews/pr250-codereview-codex-5-5.md:1786:packages/contracts/src/IClanWorld.sol:29:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr250-codereview-codex-5-5.md:1788:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/pr250-codereview-codex-5-5.md:1790:packages/contracts/src/IClanWorld.sol:204:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-5.md:1792:packages/contracts/src/IClanWorld.sol:244:    uint16 coldDamage; // resets to 0 at winter end
docs/reviews/pr250-codereview-codex-5-5.md:1795:packages/contracts/src/IClanWorld.sol:419:    uint64 winterStartsAtTick;
docs/reviews/pr250-codereview-codex-5-5.md:1811:packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
docs/reviews/pr250-codereview-codex-5-5.md:1812:packages/contracts/src/ClanWorldStub.sol:55:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1813:packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr250-codereview-codex-5-5.md:1816:packages/contracts/src/ClanWorldStub.sol:345:            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1878:packages/contracts/test/ClanWorld.t.sol:2029:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1879:packages/contracts/test/ClanWorld.t.sol:2030:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-5.md:1880:packages/contracts/test/ClanWorld.t.sol:2036:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr250-codereview-codex-5-5.md:1881:packages/contracts/test/ClanWorld.t.sol:2037:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr250-codereview-codex-5-5.md:1883:packages/contracts/test/ClanWorld.t.sol:2040:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr250-codereview-codex-5-5.md:1885:packages/contracts/test/ClanWorld.t.sol:2051:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
docs/reviews/pr250-codereview-codex-5-5.md:1889:packages/contracts/test/ClanWorld.t.sol:2066:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr250-codereview-codex-5-5.md:1890:packages/contracts/test/ClanWorld.t.sol:2075:            ws.winterStartsAtTick,
docs/reviews/pr250-codereview-codex-5-5.md:1891:packages/contracts/test/ClanWorld.t.sol:2076:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr250-codereview-codex-5-5.md:1892:packages/contracts/test/ClanWorld.t.sol:2092:            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr250-codereview-codex-5-5.md:1893:packages/contracts/test/ClanWorld.t.sol:2093:        assertEq(ws.winterStartsAtTick, expectedWinterStart);
docs/reviews/pr250-codereview-codex-5-5.md:1924:Local files aren’t at the PR head, so I’m treating the pasted diff as authoritative and using the repo mainly for surrounding contracts/spec context. One thing already stands out: this PR changes the winter start model from the older `[100,110)` style to `WINTER_START_TICK = 110`, so I’m checking whether that matches the phase spec rather than assuming it’s intentional.
docs/reviews/pr133-review-claude-opus.md:57:| 13 | `apps/web/src/components/cockpit/tabs/TerminalTab.tsx` | 53–57 | **Iframe `sandbox="allow-scripts allow-same-origin"` is permissive.** Scripts in the iframe run in `cockpit.clan-world.com` origin. Any XSS/bug in ttyd assets can interact with same-origin cookies/APIs. **Fix:** Ensure ttyd is started read-only; restrict who can reach ttyd URLs via network/auth; consider `referrerPolicy="no-referrer"`. | W1-A2 |
docs/reviews/pr133-review-claude-opus.md:58:| 14 | `apps/web/.env.example` | 22 | **`VITE_CLANWORLD_DEMO_MODE=true` in committed example file.** Building from `.env.example` without editing bakes mock data into production. **Fix:** Default to `false` or blank with a comment; keep `true` only in `.env.development.local`. | W1-A2, W2-A10 |
docs/reviews/pr133-review-claude-opus.md:74:| 25 | `apps/web/src/App.tsx` | 64–80 | **`/cockpit` has no in-app authentication.** Intentional for judge view, but production deployment needs edge protection (VPN, Cloudflare Access, etc.). | W1-A2 |
docs/reviews/pr194-r2-synthesis.md:14:- ✅ Winter starvation replay uses replayed-tick winter status
docs/reviews/pr137-review-claude-opus.md:62:| 11 | `src/IClanWorld.sol` | 362–367 | **No sell-side slippage protection.** Only buys carry `maxGoldIn`. Sellers have no on-chain worst-case proceeds guarantee. Agents 3, 10. | **DEFER** |
docs/reviews/pr137-review-claude-opus.md:63:| 12 | `src/StubPool.sol` | 43–49 | **Pool can become one-sided unusable.** Repeated sells can drain gold reserves to dust, causing `require(goldOut > 0)` to revert on all subsequent sells. Buys can recapitalize but require clan gold. Agents 3, 10. | **DEFER** |
docs/reviews/pr137-review-claude-opus.md:72:| 21 | `src/ClanWorld.sol` | 1655–1663 | **Spot price oracle is manipulable.** Instantaneous midpoint, not TWAP. Acceptable if clients don't treat it as an ungameable price feed. Agents 3, 10. | **DEFER** |
docs/reviews/pr137-review-claude-opus.md:81:| 30 | `src/StubPool.sol` | 43–74 | **K-invariant preservation.** Verified: floor on output (sell) and ceil on input (buy) both preserve or increase K. Rounding favors the pool/protocol. Agents 3, 10. | **SKIP** |
docs/reviews/pr137-review-claude-opus.md:85:| 34 | — | — | **Gold conservation.** Verified: gold is created only via `mintClan` starter gold and iron mining RNG. Market swaps transfer gold between clan balance and pool reserves with no leak. Agent 10. | **SKIP** |
docs/reviews/pr137-review-claude-opus.md:86:| 35 | — | — | **Resource conservation.** Verified: sell deducts exact `amount` from vault, pool receives same; buy receives exact `amountOut` from pool, vault credits same. Agent 10. | **SKIP** |
docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:27:5. Dead-clan restrict (10.5 hook works correctly)
docs/reviews/pr200-codereview-codex-5-5.md:741:               "name": "winterActive",
docs/reviews/pr200-codereview-codex-5-5.md:1718:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr200-codereview-codex-5-5.md:1720:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-5.md:1721:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-5.md:1722:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-5.md:1723:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr200-codereview-codex-5-5.md:1724:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-5.md:1733:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:2316:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-5.md:2317:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr200-codereview-codex-5-5.md:2318:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr200-codereview-codex-5-5.md:2319:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr200-codereview-codex-5-5.md:2320:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-5.md:2999:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-5.md:3179:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-5.md:3547:-            ws.winterStartsAtTick,
docs/reviews/pr200-codereview-codex-5-5.md:3548:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr200-codereview-codex-5-5.md:3549:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr200-codereview-codex-5-5.md:3551:         assertFalse(ws.winterActive);
docs/reviews/pr200-codereview-codex-5-5.md:3554:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr200-codereview-codex-5-5.md:3555:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr200-codereview-codex-5-5.md:3557:-        uint64 winterStart =
docs/reviews/pr200-codereview-codex-5-5.md:3558:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-5.md:3559:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-5.md:3560:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr200-codereview-codex-5-5.md:3596:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-5.md:3951:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-5.md:4485:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr200-codereview-codex-5-5.md:5076:/bin/bash -lc 'rg -n "DEAD|clanState|starvation|dead|ClanState.DEAD|propose.*Transfer|accept.*Transfer|zero|expiry" packages/contracts/test packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-5.md:5079:packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-5.md:5081:packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-5.md:5082:packages/contracts/src/ClanWorld.sol:397:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr200-codereview-codex-5-5.md:5083:packages/contracts/src/ClanWorld.sol:419:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr200-codereview-codex-5-5.md:5084:packages/contracts/src/ClanWorld.sol:420:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-5.md:5085:packages/contracts/src/ClanWorld.sol:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr200-codereview-codex-5-5.md:5086:packages/contracts/src/ClanWorld.sol:423:            clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-5.md:5087:packages/contracts/src/ClanWorld.sol:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:5094:packages/contracts/src/ClanWorld.sol:1033:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-5.md:5100:packages/contracts/src/ClanWorld.sol:2297:    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr200-codereview-codex-5-5.md:5101:packages/contracts/src/ClanWorld.sol:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:5103:packages/contracts/src/ClanWorld.sol:2401:    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr200-codereview-codex-5-5.md:5104:packages/contracts/src/ClanWorld.sol:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-5.md:5137:packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
docs/reviews/pr200-codereview-codex-5-5.md:5140:packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
docs/reviews/pr200-codereview-codex-5-5.md:5144:packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated
docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:5307:5. Dead-clan restrict (10.5 hook works correctly)
docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-5.md:5531:5. Dead-clan restrict (10.5 hook works correctly)
docs/reviews/pr200-codereview-codex-5-5.md:5973:docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:5979:docs/reviews/pr200-codereview-codex-5-4.md:4857:docs/planning/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-5.md:6004:docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6010:docs/reviews/pr200-codereview-codex-5-4.md:4931:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-5.md:6022:docs/reviews/pr200-codereview-codex-5-4.md:5024:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-5.md:6023:docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6024:docs/reviews/pr200-codereview-codex-5-4.md:5065:### 10.4 ClanOrder
docs/reviews/pr200-codereview-codex-5-5.md:6025:docs/reviews/pr200-codereview-codex-5-4.md:5080:### 10.5 OrderResult
docs/reviews/pr200-codereview-codex-5-5.md:6059:packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-5.md:6060:packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-5.md:6073:packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
docs/reviews/pr200-codereview-codex-5-5.md:6074:packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
docs/reviews/pr200-codereview-codex-5-5.md:6075:packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated
docs/reviews/pr200-codereview-codex-5-5.md:6147:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-5.md:6161:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-5.md:6188:### 10.4 ClanOrder
docs/reviews/pr200-codereview-codex-5-5.md:6203:### 10.5 OrderResult
docs/reviews/pr200-codereview-codex-5-5.md:6217:The prior artifacts are transcripts, not clean review outputs, so I’m using them cautiously. The implementation-plan spec is small but important: Phase 7 explicitly says dead clans cannot initiate transfers; the diff blocks dead accept as well, which may be fine as a stricter rule, but I’m checking for places where that stricter lifecycle leaves proposals stuck or ambiguous.
docs/reviews/pr200-codereview-codex-5-5.md:6278:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-5.md:6281:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr200-codereview-codex-5-5.md:6286:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-5.md:6316:- winter transition checks
docs/reviews/pr200-codereview-codex-5-5.md:6951:        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:23:3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
docs/reviews/pr194-r2-codereview-codex-5-5.md:402:               "name": "winterActive",
docs/reviews/pr194-r2-codereview-codex-5-5.md:740:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-5.md:742:-        _world.winterStartsAtTick =
docs/reviews/pr194-r2-codereview-codex-5-5.md:743:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:744:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:745:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-5.md:746:         _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-5.md:763:             clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:767:+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:774:+        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:776:+        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-5.md:777:+            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr194-r2-codereview-codex-5-5.md:778:+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:780:+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-5.md:781:+            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-5.md:794:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-5.md:836:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1137:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1138:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1139:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1140:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1143:+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1183:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1258:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:2476:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2507:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:2517:+        // Step 7: Resolve world events (season boundary, winter transitions).
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
docs/reviews/pr194-r2-codereview-codex-5-5.md:4374:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4380:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4442:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4443:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-5.md:4486:-            ws.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-5.md:4487:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr194-r2-codereview-codex-5-5.md:4488:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr194-r2-codereview-codex-5-5.md:4490:         assertFalse(ws.winterActive);
docs/reviews/pr194-r2-codereview-codex-5-5.md:4493:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:4494:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr194-r2-codereview-codex-5-5.md:4496:-        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-5.md:4497:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
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
docs/reviews/pr194-r2-codereview-codex-5-5.md:5478:   443	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr194-r2-codereview-codex-5-5.md:5500:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5501:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5503:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5504:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5539:  1099	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5540:  1100	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5541:  1101	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5542:  1102	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5545:  1105	        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5585:  1145	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5660:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6101:   465	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6102:   466	            clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6104:   468	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6105:   469	            clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6109:   473	        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6116:   480	        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6118:   482	        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-5.md:6119:   483	            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6120:   484	        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6122:   486	        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-5.md:6123:   487	            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6136:   500	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-5.md:6221:  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-5.md:6374:  3472	            winterActive: _world.winterActive,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6375:  3473	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6376:  3474	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
docs/reviews/pr194-r2-codereview-codex-5-5.md:6393:473:        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6395:612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:6404:1105:        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6406:1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
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
docs/reviews/pr194-r2-codereview-codex-5-5.md:7426:  "packageManager": "pnpm@10.28.1",
docs/reviews/pr194-r2-codereview-codex-5-5.md:7561:getWorldState ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","nextHeartbeatAtTs:uint64","nextBanditSpawnEligibleTick:uint64","currentBanditSpawnChanceBps:uint16","currentTickSeed:bytes32","activeBanditId:uint32","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","nextCommitSequence:uint64"]
docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr194-r2-codereview-codex-5-5.md:7568:/bin/bash -lc 'rg -n "BANDIT_CAMP_TICKS|BANDIT_REST_TICKS|REGION_FOREST|REGION_DEEP_SEA|SEASON_DURATION_TICKS|TICKS_PER_WINTER_CYCLE|WINTER_DURATION_TICKS|WHEAT_UPKEEP|FISH_UPKEEP" packages/contracts/src/IClanWorld.sol packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr194-r2-codereview-codex-5-5.md:7570:packages/contracts/src/IClanWorld.sol:29:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7580:packages/contracts/src/ClanWorld.sol:138:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-5.md:7581:packages/contracts/src/ClanWorld.sol:140:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-5.md:7582:packages/contracts/src/ClanWorld.sol:141:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-5.md:7586:packages/contracts/src/ClanWorld.sol:480:        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7587:packages/contracts/src/ClanWorld.sol:483:            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7588:packages/contracts/src/ClanWorld.sol:484:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7589:packages/contracts/src/ClanWorld.sol:486:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-5.md:7610:packages/contracts/src/ClanWorld.sol:2383:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr194-r2-codereview-codex-5-5.md:7612:packages/contracts/src/ClanWorld.sol:2385:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7613:packages/contracts/src/ClanWorld.sol:2400:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
docs/reviews/pr194-r2-codereview-codex-5-5.md:7615:packages/contracts/src/ClanWorld.sol:2402:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7698:   333	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7699:   334	        _advanceUntil(winterStart + 4);
docs/reviews/pr194-r2-codereview-codex-5-5.md:7718:   353	        uint64 deathFromTick = winterStart;
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
docs/reviews/pr194-r2-codereview-codex-5-5.md:8491:   354	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr194-r2-codereview-codex-5-5.md:8497:   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr153-review-codex-5-3.md:85:- **Runner/agents hardening:** #4, #10.
docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
docs/reviews/pr194-r2-codereview-codex-5-4.md:23:3. Winter starvation replay — does _applyUpkeep + _simulateApplyUpkeep now derive winter status from replayed tick (not current flag)?
docs/reviews/pr194-r2-codereview-codex-5-4.md:402:               "name": "winterActive",
docs/reviews/pr194-r2-codereview-codex-5-4.md:740:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr194-r2-codereview-codex-5-4.md:742:-        _world.winterStartsAtTick =
docs/reviews/pr194-r2-codereview-codex-5-4.md:743:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-4.md:744:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr194-r2-codereview-codex-5-4.md:745:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr194-r2-codereview-codex-5-4.md:746:         _world.winterActive = false;
docs/reviews/pr194-r2-codereview-codex-5-4.md:763:             clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:767:+        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:774:+        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-4.md:776:+        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-4.md:777:+            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr194-r2-codereview-codex-5-4.md:778:+        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-4.md:780:+        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-4.md:781:+            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-4.md:794:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-4.md:836:+        clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1137:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1138:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1139:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1140:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1143:+        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1183:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1258:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:2476:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2507:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:2517:+        // Step 7: Resolve world events (season boundary, winter transitions).
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
docs/reviews/pr194-r2-codereview-codex-5-4.md:4374:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4380:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4442:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4443:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/pr194-r2-codereview-codex-5-4.md:4486:-            ws.winterStartsAtTick,
docs/reviews/pr194-r2-codereview-codex-5-4.md:4487:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr194-r2-codereview-codex-5-4.md:4488:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr194-r2-codereview-codex-5-4.md:4490:         assertFalse(ws.winterActive);
docs/reviews/pr194-r2-codereview-codex-5-4.md:4493:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr194-r2-codereview-codex-5-4.md:4494:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr194-r2-codereview-codex-5-4.md:4496:-        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-4.md:4497:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
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
docs/reviews/pr194-r2-codereview-codex-5-4.md:5514:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5675:    bool winterActive;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5676:    uint64 winterStartsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5677:    uint64 winterEndsAtTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5902:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr194-r2-codereview-codex-5-4.md:5924:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5925:            clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5927:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5928:            clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5932:        if (starving && _isWinterTick(tick) && clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5939:        uint64 cycleOffset = seasonOffset % ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5941:        uint64 winterStart =
docs/reviews/pr194-r2-codereview-codex-5-4.md:5942:            cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5943:        uint64 winterEnd = cycleStart + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5945:        return winterStart < ClanWorldConstants.SEASON_DURATION_TICKS && seasonOffset >= winterStart
docs/reviews/pr194-r2-codereview-codex-5-4.md:5946:            && seasonOffset < winterEnd;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5959:                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/pr194-r2-codereview-codex-5-4.md:5989:        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5990:            sim.clan.starvationStartsAtTick = tick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5991:        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5992:            sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5995:        if (starving && _isWinterTick(tick) && sim.clan.starvationStartsAtTick <= tick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:6035:        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6110:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6177:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
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
docs/reviews/pr194-r2-codereview-codex-5-4.md:7465:   612	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7655:   354	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr194-r2-codereview-codex-5-4.md:7661:   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr194-r2-codereview-codex-5-4.md:7998:612:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7999:1220:            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:8038:3415:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:8167:  2333	        // Step 7: Resolve world events (season boundary, winter transitions).
docs/reviews/pr194-r2-codereview-codex-5-4.md:8182:The source pass is down to two likely review items: one in view-simulation starvation handling, and one possible stale-state issue during target selection before a camped bandit attacks. I’m waiting on the parallel passes now to see whether they independently confirm those or uncover anything else in the ABI/tests lane.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8313:  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr194-r2-codereview-codex-5-4.md:8349:  - Winter starvation replay is using `_isWinterTick(tick)` in both storage settlement and view simulation.
docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:27:5. Dead-clan restrict (10.5 hook works correctly)
docs/reviews/pr200-codereview-codex-5-4.md:741:               "name": "winterActive",
docs/reviews/pr200-codereview-codex-5-4.md:1718:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr200-codereview-codex-5-4.md:1720:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-4.md:1721:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-4.md:1722:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-4.md:1723:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr200-codereview-codex-5-4.md:1724:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-4.md:1733:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:2316:-        _world.winterStartsAtTick =
docs/reviews/pr200-codereview-codex-5-4.md:2317:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr200-codereview-codex-5-4.md:2318:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
docs/reviews/pr200-codereview-codex-5-4.md:2319:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
docs/reviews/pr200-codereview-codex-5-4.md:2320:         _world.winterActive = false;
docs/reviews/pr200-codereview-codex-5-4.md:2999:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-4.md:3179:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-4.md:3547:-            ws.winterStartsAtTick,
docs/reviews/pr200-codereview-codex-5-4.md:3548:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr200-codereview-codex-5-4.md:3549:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr200-codereview-codex-5-4.md:3551:         assertFalse(ws.winterActive);
docs/reviews/pr200-codereview-codex-5-4.md:3554:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr200-codereview-codex-5-4.md:3555:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr200-codereview-codex-5-4.md:3557:-        uint64 winterStart =
docs/reviews/pr200-codereview-codex-5-4.md:3558:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-4.md:3559:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr200-codereview-codex-5-4.md:3560:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr200-codereview-codex-5-4.md:3596:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-4.md:3951:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-4.md:4485:+    address elderB = address(0xA2);
docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-4.md:4667:docs/reviews/pr194-r2-codereview-codex-5-5.md:8491:   354	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4668:docs/reviews/pr194-r2-codereview-codex-5-5.md:8497:   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:4690:docs/reviews/pr200-codereview-codex-5-5.md:5079:packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4691:docs/reviews/pr200-codereview-codex-5-5.md:5081:packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4692:docs/reviews/pr200-codereview-codex-5-5.md:5137:packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
docs/reviews/pr200-codereview-codex-5-4.md:4693:docs/reviews/pr200-codereview-codex-5-5.md:5140:packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
docs/reviews/pr200-codereview-codex-5-4.md:4694:docs/reviews/pr200-codereview-codex-5-5.md:5144:packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated
docs/reviews/pr200-codereview-codex-5-4.md:4697:docs/reviews/r2/pr194-codereview-codex-5-5.md:6804:   352	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4698:docs/reviews/r2/pr194-codereview-codex-5-5.md:6810:   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4706:docs/reviews/r2/pr194-codereview-codex-5-5.md:10611:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr200-codereview-codex-5-4.md:4707:docs/reviews/r2/pr194-codereview-codex-5-5.md:10653:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr200-codereview-codex-5-4.md:4725:docs/reviews/pr198-codereview-codex-5-4.md:9450:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/reviews/pr200-codereview-codex-5-4.md:4726:docs/reviews/pr198-codereview-codex-5-4.md:10444:   309	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4727:docs/reviews/pr198-codereview-codex-5-4.md:10450:   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4740:docs/reviews/r2/pr194-codereview-codex-5-4.md:7376:   352	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4741:docs/reviews/r2/pr194-codereview-codex-5-4.md:7382:   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr200-codereview-codex-5-4.md:4779:docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4783:docs/reviews/pr198-codereview-codex-5-5.md:5721:docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:4784:docs/reviews/pr198-codereview-codex-5-5.md:6003:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:4790:docs/reviews/pr198-codereview-codex-5-5.md:7261:   309	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4791:docs/reviews/pr198-codereview-codex-5-5.md:7267:   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4815:docs/reviews/pr194-r2-codereview-codex-5-4.md:7655:   354	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4816:docs/reviews/pr194-r2-codereview-codex-5-4.md:7661:   360	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4843:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4846:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/reviews/pr200-codereview-codex-5-4.md:4847:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/reviews/pr200-codereview-codex-5-4.md:4850:docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr200-codereview-codex-5-4.md:4857:docs/planning/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-4.md:4864:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4868:docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4884:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:4885:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:4886:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/reviews/pr200-codereview-codex-5-4.md:4888:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/reviews/pr200-codereview-codex-5-4.md:4897:docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:4898:docs/planning/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:4899:docs/planning/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/reviews/pr200-codereview-codex-5-4.md:4901:docs/planning/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
docs/reviews/pr200-codereview-codex-5-4.md:4905:docs/planning/V1/01 Blockchain Game Spec/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/reviews/pr200-codereview-codex-5-4.md:4917:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:660:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:4920:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/reviews/pr200-codereview-codex-5-4.md:4921:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/reviews/pr200-codereview-codex-5-4.md:4924:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/reviews/pr200-codereview-codex-5-4.md:4931:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-4.md:4936:packages/contracts/src/ClanWorld.sol:309:        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:4937:packages/contracts/src/ClanWorld.sol:315:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:4950:packages/contracts/test/ClanWorld.t.sol:1699:    // 3.E3: ERR_CLANSMAN_DEAD — dead clansman is rejected
docs/reviews/pr200-codereview-codex-5-4.md:4951:packages/contracts/test/ClanWorld.t.sol:1727:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
docs/reviews/pr200-codereview-codex-5-4.md:4952:packages/contracts/test/ClanWorld.t.sol:2173:    // Path 6: dead clansman mid-mission → mission invalidated
docs/reviews/pr200-codereview-codex-5-4.md:5024:### 7.5 Restrict dead clans
docs/reviews/pr200-codereview-codex-5-4.md:5038:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:5065:### 10.4 ClanOrder
docs/reviews/pr200-codereview-codex-5-4.md:5080:### 10.5 OrderResult
docs/reviews/pr200-codereview-codex-5-4.md:5100:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-4.md:5103:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/reviews/pr200-codereview-codex-5-4.md:5108:- `starvationStartsAtTick`
docs/reviews/pr200-codereview-codex-5-4.md:5138:- winter transition checks
docs/reviews/pr200-codereview-codex-5-4.md:5371:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:5372:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr200-codereview-codex-5-4.md:5622:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6296:## 10. Write interfaces and access control
docs/reviews/pr200-codereview-codex-5-4.md:6298:### 10.1 Access control rules
docs/reviews/pr200-codereview-codex-5-4.md:6313:### 10.2 Core write functions
docs/reviews/pr200-codereview-codex-5-4.md:6324:### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:6351:### 10.4 ClanOrder
docs/reviews/pr200-codereview-codex-5-4.md:6376:- dead clan restrictions on OTC
docs/reviews/pr200-codereview-codex-5-4.md:6399:- winter interaction hook
docs/reviews/pr200-codereview-codex-5-4.md:6406:### 5.6 Implement starvation consequences
docs/reviews/pr200-codereview-codex-5-4.md:6407:- next-tick starvation start
docs/reviews/pr200-codereview-codex-5-4.md:6413:/bin/bash -lc "rg -n \"_settleClan\\(|submitClanOrders\\(|clan.lastSettledTick|starvationStartsAtTick|_resolveWorldEvents|_killClan|clanState = ClanState.DEAD|clanState "'!= ClanState.DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
docs/reviews/pr200-codereview-codex-5-4.md:6418:419:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6419:420:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-4.md:6420:422:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6421:423:            clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6422:430:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6428:1033:        clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6431:2301:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6432:2406:        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6647:        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr200-codereview-codex-5-4.md:6653:                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr200-codereview-codex-5-4.md:6735:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/pr200-codereview-codex-5-4.md:6757:        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6758:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-4.md:6763:            clan.starvationStartsAtTick = tick;
docs/reviews/pr200-codereview-codex-5-4.md:6765:        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/pr200-codereview-codex-5-4.md:6766:            clan.starvationStartsAtTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6773:        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr200-codereview-codex-5-4.md:6877:        // Step 4: Resolve world events (season boundary, winter transitions).
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
docs/reviews/pr200-codereview-codex-5-4.md:7218:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
docs/reviews/pr200-codereview-codex-5-4.md:7747:   649	### 10.2 Core write functions
docs/reviews/pr200-codereview-codex-5-4.md:7758:   660	### 10.3 OTC transfer surface
docs/reviews/pr200-codereview-codex-5-4.md:7785:   687	### 10.4 ClanOrder
docs/reviews/pr200-codereview-codex-5-4.md:7825:   326	### 7.5 Restrict dead clans
docs/reviews/pr198-codereview-codex-5-5.md:322:               "name": "winterActive",
docs/reviews/pr198-codereview-codex-5-5.md:747:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/pr198-codereview-codex-5-5.md:749:-        _world.winterStartsAtTick =
docs/reviews/pr198-codereview-codex-5-5.md:750:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-5.md:751:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-5.md:752:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/pr198-codereview-codex-5-5.md:753:         _world.winterActive = false;
docs/reviews/pr198-codereview-codex-5-5.md:756:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-5.md:3197:-            ws.winterStartsAtTick,
docs/reviews/pr198-codereview-codex-5-5.md:3198:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr198-codereview-codex-5-5.md:3199:+            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
docs/reviews/pr198-codereview-codex-5-5.md:3201:         assertFalse(ws.winterActive);
docs/reviews/pr198-codereview-codex-5-5.md:3204:         // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
docs/reviews/pr198-codereview-codex-5-5.md:3205:         // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
docs/reviews/pr198-codereview-codex-5-5.md:3207:-        uint64 winterStart =
docs/reviews/pr198-codereview-codex-5-5.md:3208:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-5.md:3209:+        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/pr198-codereview-codex-5-5.md:3210:         for (uint64 i = 0; i < winterStart - 1; i++) {
docs/reviews/pr198-codereview-codex-5-5.md:3937:    29	    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
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
docs/reviews/pr198-codereview-codex-5-5.md:4696:  1145	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:4771:  1220	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-5.md:5721:docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr198-codereview-codex-5-5.md:6003:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/reviews/pr198-codereview-codex-5-5.md:7261:   309	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr198-codereview-codex-5-5.md:7267:   315	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
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
docs/reviews/pr198-codereview-codex-5-5.md:9785:  1033	        clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:9945:1033-        clan.starvationStartsAtTick = 0;
docs/reviews/pr198-codereview-codex-5-5.md:10193:   204	- winter burn
docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
docs/reviews/r2/pr194-synthesis.md:34:| M2 | `ClanWorld.sol:~525` (`_markClanDead`) | 4.7 = **1/5** | MED | **`reason` parameter swallowed.** Caller passes "starvation"/"bandit"/"unknown" but `ClanEliminated` event has no cause field. Either add cause field to event, or remove the parameter. |
docs/reviews/r2/pr194-synthesis.md:35:| M3 | `ClanWorld.sol:1206` (`_simulateResolveAction`) | 4.6 = **1/5** | MED | **Starvation check uses `_world.currentTick` instead of simulation tick.** Latent bug — current control flow prevents misalignment, but breaks if simulation structure changes. Use `sim.clan.starvationStartsAtTick <= tick`. |
docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/r2/pr194-codereview-opus-4-6.md:51:### M2: `_simulateResolveAction` starvation check uses `_world.currentTick` instead of simulation tick
docs/reviews/r2/pr194-codereview-opus-4-6.md:56:bool starving = sim.clan.starvationStartsAtTick != 0 
docs/reviews/r2/pr194-codereview-opus-4-6.md:57:    && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-opus-4-6.md:60:The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.
docs/reviews/r2/pr194-codereview-opus-4-6.md:62:**Impact:** Low in practice — the upkeep/resolution ordering within the simulation loop prevents the misalignment. But it's a latent bug if the simulation structure changes. Should use `sim.clan.starvationStartsAtTick <= tick` for correctness parity.
docs/reviews/r2/pr194-codereview-codex-5-5.md:430:               "name": "winterActive",
docs/reviews/r2/pr194-codereview-codex-5-5.md:760:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/r2/pr194-codereview-codex-5-5.md:762:-        _world.winterStartsAtTick =
docs/reviews/r2/pr194-codereview-codex-5-5.md:763:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-5.md:764:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-5.md:765:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/r2/pr194-codereview-codex-5-5.md:766:         _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-5.md:783:             clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:787:+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:802:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-5.md:844:+        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1145:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1146:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1147:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1148:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1151:+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1191:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1266:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:2419:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2450:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-5.md:2460:+        // Step 7: Resolve world events (season boundary, winter transitions).
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
docs/reviews/r2/pr194-codereview-codex-5-5.md:6804:   352	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/r2/pr194-codereview-codex-5-5.md:6810:   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/r2/pr194-codereview-codex-5-5.md:6893:   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/r2/pr194-codereview-codex-5-5.md:6915:   463	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6916:   464	            clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6918:   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6919:   467	            clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6923:   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6938:   486	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-5.md:7182:  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7183:  1086	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7184:  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7185:  1088	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7188:  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7228:  1131	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7303:  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8001:  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
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
docs/reviews/r2/pr194-codereview-codex-5-5.md:8120:   528	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:8190:   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
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
docs/reviews/r2/pr194-codereview-codex-5-5.md:10611:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/r2/pr194-codereview-codex-5-5.md:10653:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/r2/pr194-codereview-codex-5-4.md:430:               "name": "winterActive",
docs/reviews/r2/pr194-codereview-codex-5-4.md:760:         // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/r2/pr194-codereview-codex-5-4.md:762:-        _world.winterStartsAtTick =
docs/reviews/r2/pr194-codereview-codex-5-4.md:763:-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-4.md:764:+        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-4.md:765:         _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/r2/pr194-codereview-codex-5-4.md:766:         _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-4.md:783:             clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:787:+        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:802:+                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:844:+        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1145:+        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1146:+            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1147:+        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1148:+            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1151:+        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1191:+        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1266:+            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:2419:-    ///         4. Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2450:-        // Step 4: Resolve world events (season boundary, winter transitions).
docs/reviews/r2/pr194-codereview-codex-5-4.md:2460:+        // Step 7: Resolve world events (season boundary, winter transitions).
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
docs/reviews/r2/pr194-codereview-codex-5-4.md:4180:+        assertEq(derived.clan.starvationStartsAtTick, 5, "starvation starts at first unpaid upkeep tick");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4186:+        assertEq(raw.starvationStartsAtTick, 0, "raw starvation flag unchanged");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4248:+        assertEq(raw1.starvationStartsAtTick, 0, "clan 1 raw starvation unchanged");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4249:+        assertEq(raw2.starvationStartsAtTick, 0, "clan 2 raw starvation unchanged");
docs/reviews/r2/pr194-codereview-codex-5-4.md:4497:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
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
docs/reviews/r2/pr194-codereview-codex-5-4.md:7072:    48	///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7160:   136	        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
docs/reviews/r2/pr194-codereview-codex-5-4.md:7162:   138	        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
docs/reviews/r2/pr194-codereview-codex-5-4.md:7163:   139	        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
docs/reviews/r2/pr194-codereview-codex-5-4.md:7164:   140	        _world.winterActive = false;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7376:   352	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/r2/pr194-codereview-codex-5-4.md:7382:   358	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/r2/pr194-codereview-codex-5-4.md:7465:   441	    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
docs/reviews/r2/pr194-codereview-codex-5-4.md:7492:   463	        if (starving && clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7493:   464	            clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7495:   466	        } else if (!starving && clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7496:   467	            clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7500:   471	        if (starving && _world.winterActive && clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7515:   486	                _markClanDead(clan.clanId, "starvation", tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:7557:   528	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7627:   598	        return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8119:  1085	        if (starving && sim.clan.starvationStartsAtTick == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8120:  1086	            sim.clan.starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8121:  1087	        } else if (!starving && sim.clan.starvationStartsAtTick != 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8122:  1088	            sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8125:  1091	        if (starving && _world.winterActive && sim.clan.starvationStartsAtTick >= _world.winterStartsAtTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8165:  1131	        sim.clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8240:  1206	            sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:9293:  2254	        // Step 7: Resolve world events (season boundary, winter transitions).
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
docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/r2/pr194-codereview-codex-5-4.md:11197:    32	        _clans[clanId].starvationStartsAtTick = tick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:11207:    42	        clan.starvationStartsAtTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:13460:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/r2/pr194-codereview-codex-5-4.md:13724:   305	            winterActive: _world.winterActive,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13725:   306	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13726:   307	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/r2/pr194-codereview-codex-5-4.md:13925:23f7f1a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/reviews/r2/pr194-codereview-codex-5-4.md:13972:   280	        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:13973:   281	        _advanceUntil(winterStart + 4);
docs/reviews/r2/pr194-codereview-codex-5-4.md:13974:   282	        uint64 deathFromTick = winterStart;
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
docs/reviews/pr199-codereview-codex-5-5.md:174:                 m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr199-codereview-codex-5-5.md:177:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:1120:+    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-5.md:1623:+    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-5.md:1827:+    address elderB = address(0xA2);
docs/reviews/pr199-codereview-codex-5-5.md:2036:+    address elder2 = address(0xA2);
docs/reviews/pr199-codereview-codex-5-5.md:2738:docs/reviews/pr250-codereview-codex-5-4.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr199-codereview-codex-5-5.md:2811:docs/reviews/pr250-codereview-codex-5-5.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr199-codereview-codex-5-5.md:2992:docs/reviews/pr194-r2-codereview-codex-5-5.md:5423:/bin/bash -lc 'rg -n "test_e2e_banditLifecycle|test_winterStarvationReplay|test_resolveBanditAttackReturns|test_defeatedBanditAwardsBlueprint|test_deadTargetCleanup|test_heartbeat_scheduledMarketObserves" packages/contracts/test' in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:2996:docs/reviews/pr194-r2-codereview-codex-5-5.md:5428:packages/contracts/test/BanditAttackResolution.t.sol:464:    function test_winterStarvationReplayUsesHistoricalWinterTicks() public {
docs/reviews/pr199-codereview-codex-5-5.md:3027:docs/reviews/pr194-r2-codereview-codex-5-5.md:6390:/bin/bash -lc "rg -n \"function _resolveAction|bool starving|_isStarving\\(|starvationStartsAtTick <=\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:3511:docs/reviews/r2/pr194-codereview-codex-5-5.md:10260:23f7f1a:packages/contracts/test/BanditAttackResolution.t.sol:280:        uint64 winterStart = world.getWorldState().winterStartsAtTick;
docs/reviews/pr199-codereview-codex-5-5.md:3685:docs/reviews/r2/pr194-codereview-opus-4-7.md:22:`ClanWorld.sol:~525` overload is `function _markClanDead(uint32 clanId, string memory, uint64 tick, uint32 excludedBanditId)` — the named-by-position-only `string memory` argument is intentional (forge-lint workaround), but it means callers pass `"starvation"` / `"bandit"` / `"unknown"` and that information is dropped on the floor. The emitted `ClanEliminated(clanId, tick)` event has no cause field. If an operator wants to know whether a clan died from starvation vs. bandit, they must replay logs from preceding `BanditAttackResolved` / `ClansmanKilledByBandit` events. Either add a cause field to `ClanEliminated`, or remove the parameter entirely so the API doesn't pretend to track something it discards.
docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr199-codereview-codex-5-5.md:3694:docs/reviews/r2/pr194-codereview-opus-4-6.md:60:The real `_resolveAction` checks `_isStarving(clan)` which also reads `_world.currentTick`. Since the simulation loop variable `tick` ranges from `lastSettledTick` to `currentTick`, and starvation can be set during simulation at any intermediate tick, this comparison against `_world.currentTick` is overly permissive. A clan that starts starving at tick 50 would be treated as "starving" for all simulated ticks from 50 onward even if `currentTick` is 100 — which happens to be correct behavior. However, if `starvationStartsAtTick` is set to a tick *after* the current simulation tick but *before* `_world.currentTick` (can't happen in practice since upkeep runs before resolution each tick), this would be wrong.
docs/reviews/pr199-codereview-codex-5-5.md:4764:packages/contracts/src/IClanWorld.sol:785:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr199-codereview-codex-5-5.md:5061:docs/reviews/pr250-codereview-codex-5-4.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr199-codereview-codex-5-5.md:5260:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:5261:docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/reviews/pr199-codereview-codex-5-5.md:5290:docs/planning/clanworld_v4_spec.md:492:- do not force heartbeat-based full clan settlement solely to flip starvation state
docs/reviews/pr199-codereview-codex-5-5.md:5316:docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr199-codereview-codex-5-5.md:5332:docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/reviews/pr199-codereview-codex-5-5.md:5342:docs/planning/clanworld_v4_spec.md:985:A season does **not** end immediately when a clan first reaches monument level 10.
docs/reviews/pr199-codereview-codex-5-5.md:5365:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-5.md:5383:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr199-codereview-codex-5-5.md:5384:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr199-codereview-codex-5-5.md:5422:docs/planning/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/reviews/pr199-codereview-codex-5-5.md:5438:docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/reviews/pr199-codereview-codex-5-5.md:5457:docs/planning/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-5.md:5470:docs/planning/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr199-codereview-codex-5-5.md:5473:docs/planning/clanworld_v4_1_addendum.md:156:If a clansman from Clan A is defending Clan B's base, and Clan A enters starvation, that defending clansman contributes **0 defense** while starvation is active, even though they are physically present at Clan B's base.
docs/reviews/pr199-codereview-codex-5-5.md:5474:docs/planning/clanworld_v4_1_addendum.md:174:Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.
docs/reviews/pr199-codereview-codex-5-5.md:5508:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:5603:docs/reviews/pr200-codereview-codex-5-5.md:5053:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-5.md:5607:docs/reviews/pr200-codereview-codex-5-5.md:5299:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:5609:docs/reviews/pr200-codereview-codex-5-5.md:5523:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:5880:docs/reviews/pr194-r2-codereview-codex-5-5.md:7562:getWorldSnapshot ["currentTick:uint64","seasonStartTick:uint64","seasonEndTick:uint64","seasonFinalized:bool","currentSeasonNumber:uint64","nextHeartbeatAtTick:uint64","winterActive:bool","winterStartsAtTick:uint64","winterEndsAtTick:uint64","activeBanditId:uint32","currentTickSeed:bytes32","leaderboard:tuple[]"]
docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr199-codereview-codex-5-5.md:5926:docs/reviews/pr250-codereview-codex-5-5.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr199-codereview-codex-5-5.md:6006:docs/reviews/pr198-codereview-codex-5-4.md:7052:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6026:docs/reviews/pr198-codereview-codex-5-4.md:7662:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6033:docs/reviews/pr198-codereview-codex-5-4.md:7821:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6039:docs/reviews/pr198-codereview-codex-5-4.md:8055:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6062:docs/reviews/pr198-codereview-codex-5-4.md:9421:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr199-codereview-codex-5-5.md:6063:docs/reviews/pr198-codereview-codex-5-4.md:9423:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr199-codereview-codex-5-5.md:6064:docs/reviews/pr198-codereview-codex-5-4.md:9436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md:96:then the base **still takes the winter wood-burn failure / cold-damage consequence for that tick**.
docs/reviews/pr199-codereview-codex-5-5.md:6066:docs/reviews/pr198-codereview-codex-5-4.md:9445:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/reviews/pr199-codereview-codex-5-5.md:6195:docs/reviews/pr198-codereview-codex-5-5.md:5643:docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6202:docs/reviews/pr198-codereview-codex-5-5.md:5925:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
docs/reviews/pr199-codereview-codex-5-5.md:6543:docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:47:| `gold-bright` | `#D4A24C` | Highlights, leaderboard winner |
docs/reviews/pr199-codereview-codex-5-5.md:6904:docs/reviews/r2/pr194-codereview-codex-5-5.md:10601:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-5.md:6905:docs/reviews/r2/pr194-codereview-codex-5-5.md:10611:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr199-codereview-codex-5-5.md:6906:docs/reviews/r2/pr194-codereview-codex-5-5.md:10643:`packages/shared/src/adapters/IChainClient.ts:34` — `RealChainClient` still declares the old `getWorldSnapshot()` tuple shape. The contract `WorldSnapshot` now inserts `currentSeasonNumber` and `nextHeartbeatAtTick` before `winterActive` at `packages/contracts/src/IClanWorld.sol:407`, but the hand-written viem ABI jumps from `seasonFinalized` to `winterActive`. This can decode the dynamic `leaderboard` offset incorrectly and break `getCurrentTick()`. Update/import the generated ABI and add a decode test for the minimal client ABI.
docs/reviews/pr199-codereview-codex-5-5.md:6907:docs/reviews/r2/pr194-codereview-codex-5-5.md:10653:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr199-codereview-codex-5-5.md:6919:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:430:- The game rewards monument-building, but you must survive winters and bandits to get there.
docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr199-codereview-codex-5-5.md:6957:docs/planning/V1/02 Frontend Spec/clanworld_convex_backend_spec.md:780:### 10.5 Why this is worth building
docs/reviews/pr199-codereview-codex-5-5.md:7008:docs/planning/V1/05 0G/clanworld_0g_battleplan_and_spec.md:218:- `elder world snapshot` — `getWorldSnapshot()` — world tick, season clock, winter, leaderboard, bandit id
docs/reviews/pr199-codereview-codex-5-5.md:7068:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:84:- Drives: top HUD bar, season clock, winter indicator, leaderboard panel, bandit summary
docs/reviews/pr199-codereview-codex-5-5.md:7074:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:7162:docs/reviews/pr200-codereview-codex-5-4.md:4656:/bin/bash -lc "rg -n \"7\\.1|7\\.2|7\\.3|7\\.4|7\\.5|OTC|atomic|dead-clan|dead clan|blueprint transfer|vault transfer|gold transfer|bundled transfer\" docs packages/contracts -g '"'!packages/contracts/abi/**'"'" in /home/claude/code/clan-world
docs/reviews/pr199-codereview-codex-5-5.md:7163:docs/reviews/pr200-codereview-codex-5-4.md:4679:docs/reviews/pr200-codereview-codex-5-5.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
docs/reviews/pr199-codereview-codex-5-5.md:7165:docs/reviews/pr200-codereview-codex-5-4.md:4706:docs/reviews/r2/pr194-codereview-codex-5-5.md:10611:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr199-codereview-codex-5-5.md:7166:docs/reviews/pr200-codereview-codex-5-4.md:4707:docs/reviews/r2/pr194-codereview-codex-5-5.md:10653:`packages/contracts/src/ClanWorld.sol:551` — target-death cleanup misses en-route defenders. `_releaseDefendersForDeadTarget` requires `_clansmanDefendingRegion[clansmanId] == baseRegion`, but traveling defenders are not registered until arrival, so their `DefendBase` mission can survive a target death and later register against a dead clan. Clear all active `DefendBase` missions with `mission.targetClanId == deadClanId`, registered or not.
docs/reviews/pr199-codereview-codex-5-5.md:7167:docs/reviews/pr200-codereview-codex-5-4.md:4761:docs/reviews/pr200-codereview-codex-5-4.md:19:Phase 7 ships: gold transfer + vault transfer + blueprint transfer + bundled transfer + dead-clan restrict. Atomic-swap pattern (proposer + acceptor, NOT order book per Liam directive). Builds on Phase 5/6/10.
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
docs/reviews/pr199-codereview-codex-5-5.md:7364:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:985:A season does **not** end immediately when a clan first reaches monument level 10.
docs/reviews/pr199-codereview-codex-5-5.md:7384:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/reviews/pr199-codereview-codex-5-5.md:7402:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/reviews/pr199-codereview-codex-5-5.md:7403:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/reviews/pr199-codereview-codex-5-5.md:7441:docs/planning/V1/01 Blockchain Game Spec/clanworld_numbered_implementation_plan.md:562:10. ranking  
docs/reviews/pr199-codereview-codex-5-5.md:7967:docs/reviews/pr199-codereview-codex-5-5.md:2738:docs/reviews/pr250-codereview-codex-5-4.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr199-codereview-codex-5-5.md:7985:docs/reviews/pr199-codereview-codex-5-5.md:2811:docs/reviews/pr250-codereview-codex-5-5.md:1515:+        assertEq(clan.wallLevel, 1, "second cold damage should degrade wall by one");
docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
docs/reviews/pr199-codereview-codex-5-5.md:8708:docs/reviews/r2/pr194-codereview-codex-5-4.md:11159:   778	    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
docs/reviews/pr199-codereview-codex-5-5.md:8914:   349	        // Path 6: dead clansman — invalidate active mission if any
docs/reviews/pr199-codereview-codex-5-5.md:8926:   361	                m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr199-codereview-codex-5-5.md:10207:  2221	    /// @dev Returns last-settled state; starvation check uses currentTick (live).
docs/reviews/pr199-codereview-codex-5-5.md:10211:  2225	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:10286:  2414	            winterActive: _world.winterActive,
docs/reviews/pr199-codereview-codex-5-5.md:10287:  2415	            winterStartsAtTick: _world.winterStartsAtTick,
docs/reviews/pr199-codereview-codex-5-5.md:10288:  2416	            winterEndsAtTick: _world.winterEndsAtTick,
docs/reviews/pr199-codereview-codex-5-5.md:10295:  2423	    /// @dev Returns last-settled storage state. Vault contents and starvation flag are
docs/reviews/pr199-codereview-codex-5-5.md:10300:  2428	        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
docs/reviews/pr199-codereview-codex-5-5.md:11152:    39	          { name: 'winterActive', type: 'bool' },
docs/reviews/pr199-codereview-codex-5-5.md:11153:    40	          { name: 'winterStartsAtTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-5.md:11154:    41	          { name: 'winterEndsAtTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-5.md:11203:    90	                  { name: 'starvationStartsAtTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-5.md:11561:b335e9c feat(contracts+convex): Phase 4.4 — winter/season timers (Closes #169) (#175)
docs/reviews/pr199-codereview-codex-5-5.md:11745:                 m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr199-codereview-codex-5-5.md:11748:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
docs/reviews/pr199-codereview-codex-5-5.md:11969:                 m.active = false; // silent invalidation; dead clansman gets no MissionCompleted
docs/reviews/pr199-codereview-codex-5-5.md:11972:         return clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;

exec
/bin/bash -lc "sed -n '380,470p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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
    bool exists;
    uint32 banditId;
    BanditState state;
    uint8 currentRegion;
    uint8 attackAttemptsMade;
    uint8 maxAttemptsRemaining;
    uint64 stateEnteredTick;
    uint64 nextActionTick;
    uint8 tier;
    uint16 attackPower;


exec
/bin/bash -lc "sed -n '900,1040p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
    ///         Execution order per spec §4.2 (CEI-safe):
    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
    ///         Seed:      closedTick seed derived and published before step 1 so
    ///                    settlement RNG reads real entropy, not zero.
    ///         1. Settle missions completing this tick.
    ///         2. Execute scheduled market actions for closedTick (external calls).
    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
    ///         4. Resolve world events (season boundary, winter transitions).
    ///         5. Increment tick and publish (seed already written above).
    function heartbeat() external override nonReentrant {
        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");

        uint64 closedTick = _world.currentTick;

        // CEI: update rate-limit guard before any external calls
        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;

        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
        _tickSeeds[closedTick] = newSeed;
        _world.currentTickSeed = newSeed;

        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
        _settleCompletingMissions(closedTick);

        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
        _executeScheduledMarketActions(closedTick);

        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
        // TODO Phase 3: _settleClansNearBandit(closedTick);

        // Step 4: Resolve world events (season boundary, winter transitions).
        _resolveWorldEvents(closedTick);

        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
        uint64 newTick = closedTick + 1;
        _world.currentTick = newTick;
        _world.nextHeartbeatAtTick = newTick + 1;

        emit TickAdvanced(closedTick, newTick, newSeed);
    }

    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
    ///      Called from heartbeat before market execution and tick increment.
    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
    function _settleCompletingMissions(uint64 tick) internal {
        for (uint256 i = 0; i < _allClanIds.length; i++) {
            uint32 clanId = _allClanIds[i];
            Clan storage clan = _clans[clanId];
            if (clan.clanState == ClanState.DEAD) continue;

            uint32[] storage csIds = _clanClansmanIds[clanId];
            for (uint256 j = 0; j < csIds.length; j++) {
                Clansman storage cs = _clansmen[csIds[j]];
                if (cs.state == ClansmanState.DEAD) continue;

                Mission storage m = _missions[cs.clansmanId];
                if (!m.active) continue;
                if (m.settlesAtTick != tick) continue; // not due this tick

                // Settle this mission using the single-tick range [tick, tick+1).
                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
            }
        }
    }

    /// @dev Resolve world events for the tick that was just closed.
    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
    function _resolveWorldEvents(uint64 closedTick) internal {
        uint64 newTick = closedTick + 1;

        // --- season boundary ---
        if (newTick >= _world.seasonEndTick) {
            _world.currentSeasonNumber += 1;
            _world.seasonStartTick = _world.seasonEndTick;
            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
            // reset winter timers for new season
            _world.winterActive = false;
            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
                - ClanWorldConstants.WINTER_DURATION_TICKS;
            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
        }

        // --- winter transitions (timer only; mechanics = Phase 10) ---
        if (
            !_world.winterActive && newTick >= _world.winterStartsAtTick
                && _world.winterStartsAtTick < _world.seasonEndTick
        ) {
            _world.winterActive = true;
            emit WinterStarted(newTick);
        }
        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
            _world.winterActive = false;
            emit WinterEnded(newTick);
            // schedule next winter cycle within this season
            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
                - ClanWorldConstants.WINTER_DURATION_TICKS;
            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
            if (nextWinterStart < _world.seasonEndTick) {
                _world.winterStartsAtTick = nextWinterStart;
                _world.winterEndsAtTick = nextWinterEnd;
            } else {
                // no more winters this season; sentinel = seasonEndTick so guard never fires
                _world.winterStartsAtTick = _world.seasonEndTick;
                _world.winterEndsAtTick = _world.seasonEndTick;
            }
        }
    }

    /// @notice Public settlement trigger — lazily settle a clan.
    function settleClan(uint32 clanId) external override nonReentrant {
        _settleClan(clanId);
    }

    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
    ///         Internally settles the entire clan (including upkeep) to guarantee
    ///         correct ordering and prevent double-settlement. Callers may call this
    ///         or settleClan interchangeably; both are safe and idempotent.
    function settleClansman(uint32 csId) external override nonReentrant {
        Clansman storage cs = _clansmen[csId];
        if (cs.clansmanId == 0) return;
        _settleClan(cs.clanId);
    }

    /// @notice Finalize season. Phase 1 stub.
    function finalizeSeason() external override {
        // TODO Phase 3
    }

    // =========================================================================
    // CLAN LIFECYCLE
    // =========================================================================

    /// @notice Mint a new clan and spawn its homebase.
    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
        require(to != address(0), "ClanWorld: zero address");
        require(_allClanIds.length < 12, "ClanWorld: max clans");
        clanId = _nextClanId++;

exec
/bin/bash -lc "rg -n \"Winter|winter|starvation|eliminat|cold damage|wood burn|crop|dead\" docs/planning -g '*.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/phase-3-test-spec.md:105:- Clan B has starvation developing — unsettled penalty.
docs/planning/phase-3-test-spec.md:173:**Setup:** Register worker as defender. Set worker HP to 0 (or advance starvation until death).
docs/planning/phase-3-test-spec.md:175:**Action:** Trigger worker death (via heartbeat starvation or forced state).
docs/planning/phase-3-test-spec.md:193:**Setup:** Register worker of Clan B as defender of Clan A. Kill Clan A (set workers = 0 / mark dead).
docs/planning/clanworld_v4_3_schema_patch.md:283:- `starvationStartsAtTick`
docs/planning/clanworld_v4_3_schema_patch.md:286:exist, remove `starvingCached` in favor of deriving starvation from canonical state.
docs/planning/clanworld_v4_3_schema_patch.md:291:- `starvationStartsAtTick`
docs/planning/clanworld_v4_3_schema_patch.md:321:- winter transition checks
docs/planning/clanworld_v4_3_schema_patch.md:353:- removal of redundant starvation cache where possible
docs/planning/clanworld_v4_3_schema_patch.md:354:- dead clans blocked from OTC transfers
docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
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
docs/planning/clanworld_v4_spec.md:823:# 7. Winter Rules
docs/planning/clanworld_v4_spec.md:825:## 7.1 Winter cadence
docs/planning/clanworld_v4_spec.md:826:Winter occurs every `110` ticks.
docs/planning/clanworld_v4_spec.md:828:## 7.2 Winter duration
docs/planning/clanworld_v4_spec.md:829:Winter lasts `10` ticks.
docs/planning/clanworld_v4_spec.md:831:## 7.3 Winter upkeep
docs/planning/clanworld_v4_spec.md:832:During winter, each clan consumes:
docs/planning/clanworld_v4_spec.md:835:- wood burn = `1e18` wood per base per tick
docs/planning/clanworld_v4_spec.md:837:## 7.4 Winter farming rule
docs/planning/clanworld_v4_spec.md:838:During winter:
docs/planning/clanworld_v4_spec.md:841:- regrowth does not complete during winter
docs/planning/clanworld_v4_spec.md:843:At winter start:
docs/planning/clanworld_v4_spec.md:845:- both plots restart regrowing after winter ends
docs/planning/clanworld_v4_spec.md:846:- both plots become harvestable again `4` ticks after winter ends
docs/planning/clanworld_v4_spec.md:848:## 7.5 Winter gathering outside farming
docs/planning/clanworld_v4_spec.md:849:During winter:
docs/planning/clanworld_v4_spec.md:854:## 7.6 Winter failure: cold damage
docs/planning/clanworld_v4_spec.md:855:If a base cannot pay the required winter wood burn for a tick:
docs/planning/clanworld_v4_spec.md:863:If a clan’s living clansmen count reaches `0`, the clan enters the `DEAD` / eliminated state defined in §12.7.
docs/planning/clanworld_v4_spec.md:866:`coldDamage` resets to `0` at the end of each Winter.
docs/planning/clanworld_v4_spec.md:868:Cold damage does **not** persist across winters.
docs/planning/clanworld_v4_spec.md:870:Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.
docs/planning/clanworld_v4_spec.md:947:- walls may lose levels from winter cold damage and bandit pressure
docs/planning/clanworld_v4_spec.md:962:For v1, an eligible clan is a clan that is **not** in the `DEAD` / eliminated state at season end.
docs/planning/clanworld_v4_spec.md:991:This is approximately three winters under the current winter cadence.
docs/planning/clanworld_v4_spec.md:1102:These values are intended to prevent instant early starvation while still forcing quick economic action.
docs/planning/clanworld_v4_spec.md:1117:## 12.7 Clan death / eliminated state
docs/planning/clanworld_v4_spec.md:1118:If a clan’s living clansmen count reaches `0`, the clan enters the `DEAD` / eliminated state.
docs/planning/clanworld_v4_spec.md:1120:While a clan is dead:
docs/planning/clanworld_v4_spec.md:1130:- purse gold remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1131:- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season
docs/planning/clanworld_v4_spec.md:1163:- a dead clansman cannot receive orders
docs/planning/clanworld_v4_spec.md:1189:- starvation weakens but does not kill outside Winter
docs/planning/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/planning/clanworld_v4_spec.md:1195:- cold damage reset each winter
docs/planning/clanworld_v4_spec.md:1201:- dead clans are eliminated and remaining vault resources are burned
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
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:43:- winter cadence and cold damage
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:46:- elimination state and payout ranking inputs
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:69:This is required so target selection, defense totals, starvation state, and vault values are correct at attack resolution time.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:84:- winter boundary transitions
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:204:- winter burn
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:222:- winter burn
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:342:    bool winterActive;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:343:    uint64 winterStartsAtTick;   // first winter start is tick 110
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:344:    uint64 winterEndsAtTick;     // 0 if not active
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:388:    uint64 starvationStartsAtTick; // 0 or max sentinel if none
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:391:    uint16 coldDamage;             // resets to 0 at winter end
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:416:Winter transition semantics:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:811:2. update starvation status if shortage occurred
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:832:### 12.7 Summer starvation
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:833:Outside winter, starvation is nonlethal.
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:838:- does not lose workers directly from starvation outside winter
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:886:event WinterStarted(uint64 indexed tick);
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:887:event WinterEnded(uint64 indexed tick);
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:931:- a dead clansman cannot receive orders
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:932:- a dead clan cannot receive new workers or orders
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:933:- only vault resources count for upkeep, winter burn, building, and bandit target value
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:984:If Clan B has a worker defending Clan A, and Clan B later enters starvation:
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:985:- that defending worker contributes 0 defense until Clan B starvation ends
docs/planning/clanworld_v1_implementation_profile.md:129:- dead clan cannot send OTC transfers
docs/planning/clanworld_v1_implementation_profile.md:218:- no attempt to eliminate adversarial front-running
docs/planning/clanworld_v1_implementation_profile.md:224:- only vault contents count for upkeep/building/winter
docs/planning/clanworld_v1_implementation_profile.md:242:- winter
docs/planning/clanworld_v1_implementation_profile.md:298:- starvation
docs/planning/clanworld_v1_implementation_profile.md:299:- winter
docs/planning/clanworld_v1_implementation_profile.md:300:- cold damage
docs/planning/clanworld_v1_implementation_profile.md:301:- elimination
docs/planning/clanworld_v4_5_alignment_addendum.md:23:### Submission 1 build docs (World mini app, earlier deadline)
docs/planning/clanworld_v4_5_alignment_addendum.md:32:### Submission 2 source materials (later deadline)
docs/planning/clanworld_v4_5_alignment_addendum.md:48:The project is delivering **two hackathon submissions** at different deadlines, plus follow-up integrations:
docs/planning/clanworld_v4_5_alignment_addendum.md:50:### 1.1 Submission 1 — World mini app (earlier deadline)
docs/planning/clanworld_v4_5_alignment_addendum.md:580:- **OpenAgents Track 2 bounty criteria** — re-read closer to deadline to make sure the spec still meets criteria
docs/planning/clanworld_numbered_implementation_plan.md:127:- dead clan restrictions on OTC
docs/planning/clanworld_numbered_implementation_plan.md:154:- starvation state evolution
docs/planning/clanworld_numbered_implementation_plan.md:207:### 4.4 Add winter / season timers to world state
docs/planning/clanworld_numbered_implementation_plan.md:210:- winter timing
docs/planning/clanworld_numbered_implementation_plan.md:218:**Cut line:** if needed, winter logic can still be stubbed here.
docs/planning/clanworld_numbered_implementation_plan.md:241:- winter interaction hook
docs/planning/clanworld_numbered_implementation_plan.md:248:### 5.6 Implement starvation consequences
docs/planning/clanworld_numbered_implementation_plan.md:249:- next-tick starvation start
docs/planning/clanworld_numbered_implementation_plan.md:256:**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.
docs/planning/clanworld_numbered_implementation_plan.md:326:### 7.5 Restrict dead clans
docs/planning/clanworld_numbered_implementation_plan.md:403:- clear defender registry for dead target
docs/planning/clanworld_numbered_implementation_plan.md:413:## Phase 10 — winter and elimination
docs/planning/clanworld_numbered_implementation_plan.md:415:### 10.1 Implement winter schedule
docs/planning/clanworld_numbered_implementation_plan.md:416:- first winter at tick 110
docs/planning/clanworld_numbered_implementation_plan.md:420:### 10.2 Implement winter upkeep
docs/planning/clanworld_numbered_implementation_plan.md:422:- wood burn
docs/planning/clanworld_numbered_implementation_plan.md:424:### 10.3 Implement cold damage
docs/planning/clanworld_numbered_implementation_plan.md:428:- reset at winter end
docs/planning/clanworld_numbered_implementation_plan.md:430:### 10.4 Implement crop winter transitions
docs/planning/clanworld_numbered_implementation_plan.md:432:- lock during winter
docs/planning/clanworld_numbered_implementation_plan.md:433:- restart regrow after winter
docs/planning/clanworld_numbered_implementation_plan.md:436:- set dead state
docs/planning/clanworld_numbered_implementation_plan.md:438:- preserve dead-clan restrictions
docs/planning/clanworld_numbered_implementation_plan.md:441:- winter can kill clans and end runs
docs/planning/clanworld_numbered_implementation_plan.md:443:**Cut line:** this entire phase is the first major system you can defer if the core loop already works and deadline is near.
docs/planning/clanworld_numbered_implementation_plan.md:451:- approximately 3 winters
docs/planning/clanworld_numbered_implementation_plan.md:507:6. optional winter hardship
docs/planning/clanworld_numbered_implementation_plan.md:537:- winter
docs/planning/clanworld_numbered_implementation_plan.md:564:12. winter  

exec
/bin/bash -lc "sed -n '820,880p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

---

# 7. Winter Rules

## 7.1 Winter cadence
Winter occurs every `110` ticks.

## 7.2 Winter duration
Winter lasts `10` ticks.

## 7.3 Winter upkeep
During winter, each clan consumes:
- wheat consumption = `2×` normal
- fish consumption = `2×` normal
- wood burn = `1e18` wood per base per tick

## 7.4 Winter farming rule
During winter:
- wheat plots are unavailable
- harvesting wheat is not allowed
- regrowth does not complete during winter

At winter start:
- all plots are cleared
- both plots restart regrowing after winter ends
- both plots become harvestable again `4` ticks after winter ends

## 7.5 Winter gathering outside farming
During winter:
- logging is allowed
- mining is allowed
- fishing is allowed

## 7.6 Winter failure: cold damage
If a base cannot pay the required winter wood burn for a tick:
- it gains `1 coldDamage`

## 7.7 Cold damage consequences
- every `2 coldDamage` reduces `wallLevel` by `1`
- once `wallLevel == 0`, every `2 additional coldDamage` kills `1 clansman`

Clansman deaths from cold apply only while wall level is zero.
If a clan’s living clansmen count reaches `0`, the clan enters the `DEAD` / eliminated state defined in §12.7.

## 7.8 Cold damage reset
`coldDamage` resets to `0` at the end of each Winter.

Cold damage does **not** persist across winters.

Wall damage and clansman deaths caused by earlier winter cold still persist, but the temporary cold-damage accumulator does not carry into the next winter cycle.

---

# 8. Building Actions, Levels, and Resource Requirements

## 8.1 Build action class
The following are **single-tick actions** that consume one full action tick at homebase:
- `build_wall`
- `upgrade_base`
- `upgrade_monument`

exec
/bin/bash -lc "sed -n '470,505p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

These costs apply only if the clan has living clansmen.

## 4.11 Starvation trigger
If a clan vault cannot satisfy required wheat or fish upkeep during a tick:
- starvation status begins on the **next tick**

## 4.12 Starvation effects
While starving:
- all gathering outputs are reduced by `50%`
- all clansmen contribute `0` defense

Starvation remains active until a later clan settlement determines that required food supply is again sustainable.

Outside Winter, starvation does **not** directly kill clansmen in v1.

## 4.13 Lazy starvation tracking
To keep heartbeat cheap, starvation should not require full clan mutation every tick.

Preferred implementation:
- store starvation threshold timing or equivalent compact status data
- interpret starvation status lazily at clan settlement time and in views
- do not force heartbeat-based full clan settlement solely to flip starvation state

## 4.14 Bandit drop overflow
If a clansman receives bandit-drop loot above carrying capacity:
- excess is burned

No over-capacity exception exists.

---

# 5. Unicorn Town Market Model

## 5.1 Market venue
Unicorn Town is the only region where Uniswap market actions may be executed in v1.

exec
/bin/bash -lc "sed -n '960,1005p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
4. highest surviving wall level

For v1, an eligible clan is a clan that is **not** in the `DEAD` / eliminated state at season end.

## 9.2 Monument cap
Maximum monument level is `10`.

## 9.3 Late monument gate
Monument levels `7–10` require Blueprint Fragments in addition to normal resources.

Current working rule:
- each monument level from `7` to `10` requires `1e18` Blueprint Fragment

## 9.4 Prize pot source
For v1:
- `50%` of all iNFT purchase revenue is routed into the season prize pot

## 9.5 Prize distribution
At season end:
- 1st place: `50%` of pot
- 2nd place: `25%` of pot
- 3rd place: `12.5%` of pot
- 4th place: `12.5%` of pot

## 9.6 Season duration
A season does **not** end immediately when a clan first reaches monument level 10.

For v1:
- season starts at `seasonStartTick`
- season ends at `seasonStartTick + 360 ticks`

This is approximately three winters under the current winter cadence.

## 9.7 End of season resolution
At `seasonEndTick`:
- rankings are computed using the primary ranking rule
- prize pot is distributed according to the payout ladder among eligible clans
- if fewer than four eligible clans remain, only the corresponding top payout slots are paid
- any unallocated remainder stays in treasury or may roll into a future season pot, implementation-defined for v1
- any post-season persistence or reset behavior is implementation-defined for now and may be specified later

---

# 10. Human Steering Rules

## 10.1 Steering philosophy

exec
/bin/bash -lc "sed -n '18,36p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
---

## A2. First winter timing

The first winter begins at **tick 110**.

Thereafter, winter begins every 110 ticks:
- tick 110
- tick 220
- tick 330
- and so on

Each winter lasts **10 ticks**, as already specified in v4.

---

## A3. Bandit movement duration between regions

Bandit movement between adjacent outer-ring regions takes **0 additional ticks**.

exec
/bin/bash -lc "sed -n '804,822p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md'" in /home/claude/code/clan-world
 succeeded in 0ms:
- worker transitions to `Waiting`
- unless the action is a persistent continuous action like `DefendBase`

### 12.4 Per-tick local settlement order
For each settled local tick:

1. apply clan upkeep for the tick
2. update starvation status if shortage occurred
3. advance traveling workers by one tick along route
4. resolve one tick of continuous action
5. apply single-tick action effects
6. move completed or blocked workers to `Waiting`

### 12.5 Just-in-time deposit rule
Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.

This is intentional and punishes just-in-time logistics.

### 12.6 Overflow clamp rule

exec
/bin/bash -lc "sed -n '1117,1134p' docs/planning/clanworld_v4_spec.md" in /home/claude/code/clan-world
 succeeded in 0ms:
## 12.7 Clan death / eliminated state
If a clan’s living clansmen count reaches `0`, the clan enters the `DEAD` / eliminated state.

While a clan is dead:
- the Elder may no longer assign missions
- the clan may not trade, build, defend, or gather
- the clan is removed from active play for the rest of the season
- the iNFT remains owned, but the clan is unusable until any future season-reset or resurrection system that may be defined later

When a clan dies in v1:
- all remaining vault resources are **burned**
- no ruin cache is created
- no salvage action exists
- purse gold remains bound to the dead clan and is unusable for the remainder of the season
- Blueprint Fragment balance remains bound to the dead clan and is unusable for the remainder of the season

---


exec
/bin/bash -lc 'rg -n "_releaseDefendersForDeadTarget|targetClanId|DefendBase|ClanEliminated|_markClanDead|bandit|DEAD" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
47:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
350:        if (cs.state == ClansmanState.DEAD) {
352:                if (m.action == ActionType.DefendBase) {
378:                if (m.action == ActionType.DefendBase) {
383:            if (m.action == ActionType.DefendBase) continue; // persistent defender mission
508:        } else if (action == ActionType.DefendBase) {
931:        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
952:            if (clan.clanState == ClanState.DEAD) continue;
957:                if (cs.state == ClansmanState.DEAD) continue;
1163:        uint32 targetClanId;
1180:        if (cs.state == ClansmanState.DEAD) {
1181:            result.status = StatusCode.ERR_CLANSMAN_DEAD;
1185:        if (order.action == ActionType.DefendBase) {
1186:            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
1192:            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
1195:                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
1196:                    && currentM.targetClanId == defendTargetClanId
1216:        ctx.targetClanId =
1217:            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
1219:        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
1220:        ctx.isNoop = order.action != ActionType.DefendBase
1295:        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
1328:        m.settlesAtTick = order.action == ActionType.DefendBase
1341:        m.targetClanId = ctx.targetClanId;
1430:            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
1717:        if (action == ActionType.DefendBase) {
1718:            return _validateDefendBaseOrder(clan, order, gotoRegion);
1952:    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
1958:        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
2151:            banditId: 0,
2185:    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
2186:        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
2193:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
2206:                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
2496:            banditId: 0,
2524:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {
2538:                if (cs.state != ClansmanState.DEAD && cs.currentRegion == region) {

codex
The spec confirms A2 moved first winter to tick 110, so the new constant is correct. The sharper questions now are execution order and lifecycle cleanup: whether the winter opening heartbeat actually locks crops before the opened tick is observable, and whether “clan death” is really the common helper for cold, starvation, and attack paths.
collab: Wait
exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr250-codereview-codex-5-4.md" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr250-codereview-codex-5-5.md" in /home/claude/code/clan-world
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
session id: 019ddb5a-e95f-7660-9aaa-e3290b8931f8
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

exec
/bin/bash -lc "sed -n '1,220p' docs/reviews/pr250-codereview-opus-4-7.md" in /home/claude/code/clan-world
 succeeded in 0ms:
exec
/bin/bash -lc "rg -n \"function submitClanOrders|require\\(.*clan dead|ERR_CLAN_DEAD|_processOrder|_validateOrder|clanState\" packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
952:            if (clan.clanState == ClanState.DEAD) continue;
1059:        clan.clanState = ClanState.ACTIVE;
1114:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
1123:        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
1150:            results[i] = _processOrder(clanId, clan, orders[i]);
1168:    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
2312:            if (_clans[clanId].clanState != ClanState.ACTIVE) continue;
2402:                state: clan.clanState,

exec
/bin/bash -lc 'rg -n "''^# Phase|''^## SUMMARY|''^## HIGH|''^## MEDIUM|''^## LOW|HIGH|MEDIUM|LOW|NEEDS_FIXES|LGTM|finding" docs/reviews/pr250-codereview-codex-5-4.md' in /home/claude/code/clan-world
 succeeded in 0ms:
33:# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
34:## SUMMARY
35:## HIGH severity findings
36:## MEDIUM severity findings
37:## LOW severity findings
40:DIFF FOLLOWS.
1693:  • Synthesize findings into final pre-merge review with severity ranking and file references
2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2874:docs/reviews/pr200-codereview-codex-5-5.md:5949:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
2876:docs/reviews/pr200-codereview-codex-5-5.md:5952:docs/reviews/pr200-codereview-codex-5-4.md:4772:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
2878:docs/reviews/pr200-codereview-codex-5-5.md:5965:docs/reviews/pr200-codereview-codex-5-4.md:4827:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
3128:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3504:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
3870:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
3957:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
4006:docs/reviews/pr200-codereview-codex-5-4.md:4772:docs/reviews/pr200-codereview-codex-5-4.md:4636:Reviewing the Phase 7 diff as a cohesive pre-merge pass. I’m going to split this into parallel reads: one track on OTC lifecycle/security/accounting in the contract, and one on spec/tests/dead-clan coverage so we can converge on cross-cutting findings faster.
4031:docs/reviews/pr200-codereview-codex-5-4.md:4827:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },
4533:docs/planning/ClanWorldCockpit_BrainstormingMock.jsx:187:{ tick: 174, severity: ‘HIGH’, text: ‘BLUEPRINT FRAGMENT moved DELTA → GAMMA via OTC’ },

exec
/bin/bash -lc 'rg -n "''^# Phase|''^## SUMMARY|''^## HIGH|''^## MEDIUM|''^## LOW|HIGH|MEDIUM|LOW|NEEDS_FIXES|LGTM|finding" docs/reviews/pr250-codereview-codex-5-5.md' in /home/claude/code/clan-world
 succeeded in 0ms:
33:# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
34:## SUMMARY
35:## HIGH severity findings
36:## MEDIUM severity findings
37:## LOW severity findings
40:DIFF FOLLOWS.
1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
2149:docs/reviews/pr199-codereview-codex-5-4.md:5368:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2150:docs/reviews/pr199-codereview-codex-5-4.md:5370:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2158:docs/reviews/pr199-codereview-codex-5-4.md:5626:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2159:docs/reviews/pr199-codereview-codex-5-4.md:5627:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2164:docs/reviews/pr199-codereview-codex-5-4.md:5633:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2165:docs/reviews/pr199-codereview-codex-5-4.md:5634:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2176:docs/reviews/pr199-codereview-codex-5-4.md:5646:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2177:docs/reviews/pr199-codereview-codex-5-4.md:5647:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2178:docs/reviews/pr199-codereview-codex-5-4.md:5648:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2179:docs/reviews/pr199-codereview-codex-5-4.md:5649:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2188:docs/reviews/pr199-codereview-codex-5-4.md:5659:docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
2189:docs/reviews/pr199-codereview-codex-5-4.md:5660:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
2193:docs/reviews/pr199-codereview-codex-5-4.md:5664:docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
2194:docs/reviews/pr199-codereview-codex-5-4.md:5665:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
2206:docs/reviews/pr199-codereview-codex-5-4.md:5682:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
2207:docs/reviews/pr199-codereview-codex-5-4.md:5683:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
2216:docs/reviews/pr199-codereview-codex-5-4.md:6330:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2217:docs/reviews/pr199-codereview-codex-5-4.md:6331:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2237:docs/reviews/pr199-codereview-codex-5-4.md:7282:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2238:docs/reviews/pr199-codereview-codex-5-4.md:7284:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
2239:docs/reviews/pr199-codereview-codex-5-4.md:7409:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2240:docs/reviews/pr199-codereview-codex-5-4.md:7410:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
2250:docs/reviews/pr199-codereview-codex-5-4.md:7664:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
2251:docs/reviews/pr199-codereview-codex-5-4.md:7665:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
2331:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
3181:docs/reviews/pr250-codereview-codex-5-4.md:2796:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
3182:docs/reviews/pr250-codereview-codex-5-4.md:2797:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
3418:docs/reviews/pr250-codereview-codex-5-4.md:3128:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
3498:docs/reviews/pr250-codereview-codex-5-4.md:3226:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
3554:docs/reviews/pr250-codereview-codex-5-4.md:3299:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3556:docs/reviews/pr250-codereview-codex-5-4.md:3301:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3667:docs/reviews/pr250-codereview-codex-5-4.md:3455:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3710:docs/reviews/pr250-codereview-codex-5-4.md:3504:docs/reviews/pr198-codereview-codex-5-4.md:7386:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
3753:docs/reviews/pr250-codereview-codex-5-4.md:3553:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3775:docs/reviews/pr250-codereview-codex-5-4.md:3581:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
3776:docs/reviews/pr250-codereview-codex-5-4.md:3582:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
3788:docs/reviews/pr250-codereview-codex-5-4.md:3600:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3789:docs/reviews/pr250-codereview-codex-5-4.md:3601:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
3793:docs/reviews/pr250-codereview-codex-5-4.md:3607:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3804:docs/reviews/pr250-codereview-codex-5-4.md:3621:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
3805:docs/reviews/pr250-codereview-codex-5-4.md:3622:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
3815:docs/reviews/pr250-codereview-codex-5-4.md:3632:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
3996:docs/reviews/pr250-codereview-codex-5-4.md:3866:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
3998:docs/reviews/pr250-codereview-codex-5-4.md:3868:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
4000:docs/reviews/pr250-codereview-codex-5-4.md:3870:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
4001:docs/reviews/pr250-codereview-codex-5-4.md:3871:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
4004:docs/reviews/pr250-codereview-codex-5-4.md:3874:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
4005:docs/reviews/pr250-codereview-codex-5-4.md:3875:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
4974:docs/reviews/pr250-codereview-codex-5-5.md:1690:I’ll use the contracts guide plus a few focused explorers in parallel: one on winter boundary/spec behavior, one on clan death/RNG, and one on ABI/test-contract compatibility. Then I’ll reconcile their findings against the supplied diff for a final pre-merge review.
5294:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
5295:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
5382:docs/reviews/pr194-r2-codereview-codex-5-4.md:5202:Reviewing the fix-round diff against the surrounding contract and TS client code now. I’m going to split this into parallel passes: one on the Solidity bandit/heartbeat path, one on ABI/TypeScript decode safety, and one on the new tests and starvation replay logic so we can verify the five prior HIGHs and look for regressions.
5442:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
5444:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
5642:docs/reviews/r2/pr194-synthesis.md:22:| H2 | `runner/src/runnerCastHeartbeat.ts:34-51` + `shared/src/adapters/IChainClient.ts:34-58` | 5.4 + 5.5 = **2/5** | HIGH | **Stale handwritten ABI tuples.** `WorldState`/`WorldSnapshot` inserted `currentSeasonNumber` + `nextHeartbeatAtTick` before `nextHeartbeatAtTs`/`winterActive`, but TS clients still use old layout. Will misdecode heartbeat timestamps and break heartbeat automation at deploy. Fix: regenerate from generated ABI imports, or hand-fix both tuples. |
5643:docs/reviews/r2/pr194-synthesis.md:23:| H3 | `ClanWorld.sol:471-473, 1072-1093` | 5.4 = **1/5** | HIGH | **Winter starvation replay bug.** `_applyUpkeep` + `_simulateApplyUpkeep` read `_world.winterActive` (current flag) while replaying historical ticks. A clan that starved during a past winter but settled after winter ends will skip those deaths in both mutating settlement AND derived getters. Fix: derive winter status from the replayed `tick`, not the current world flag. |
5646:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
5647:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
5648:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
5903:docs/reviews/r2/pr194-codereview-codex-5-4.md:14952:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
5905:docs/reviews/r2/pr194-codereview-codex-5-4.md:14994:NEEDS_FIXES. The biggest blocker is that the production heartbeat still does not drive the full bandit lifecycle: real bandits can spawn and camp, but there is no live path to target selection/attack, and escaped bandits never re-enter the cycle. There are also two deploy-safety issues outside the contract core: winter starvation replay is wrong for historically-settled ticks, and the repo still contains stale handwritten ABIs that will misdecode the new `WorldState` shape.
5920:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
5945:docs/reviews/pr199-codereview-codex-5-5.md:5911:docs/reviews/pr194-r2-codereview-codex-5-5.md:8631:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
5946:docs/reviews/pr199-codereview-codex-5-5.md:5914:docs/reviews/pr194-r2-codereview-codex-5-5.md:8662:NEEDS_FIXES. The 5 prior HIGH fixes appear to have landed: heartbeat now drives bandit attacks, ABI tuple order is updated via generated ABI, winter starvation replay uses historical ticks, blueprint rewards are `1e18`, and the target-death attack race now returns safely. I found no new HIGH issues, but there are 2 MEDIUM lifecycle/data-consistency bugs and 1 LOW test gap.
5959:docs/reviews/pr199-codereview-codex-5-5.md:6516:docs/reviews/pr194-r2-codereview-codex-5-4.md:8291:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
5960:docs/reviews/pr199-codereview-codex-5-5.md:6520:docs/reviews/pr194-r2-codereview-codex-5-4.md:8327:NEEDS_FIXES. The five named HIGH fixes mostly landed: heartbeat now drives the bandit lifecycle, `WorldState`/`WorldSnapshot` tuple order is aligned in Solidity + generated ABI + TS consumers, winter starvation replay is using replayed ticks, blueprint reward is now `1e18`, and the target-death race no longer reverts. But there is still a new HIGH game-logic regression in production: camped bandits pick attack targets from stale raw clan state, so once the heartbeat path is live they can attack the wrong clan.
5967:docs/reviews/pr199-codereview-codex-5-5.md:6929:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
5981:docs/reviews/pr199-codereview-codex-5-5.md:7252:docs/reviews/r2/pr194-synthesis.md:64:- **Codex 5.4:** NEEDS_FIXES — 4 HIGH (incl. ABI staleness + winter replay + blueprint unit) + multiple LOW
5982:docs/reviews/pr199-codereview-codex-5-5.md:7253:docs/reviews/r2/pr194-synthesis.md:75:- Single-model findings: **rest** — including 3 of the 5 HIGH (winter replay, blueprint unit, attack-revert-on-death)
5996:docs/reviews/pr199-codereview-codex-5-5.md:8190:docs/reviews/pr199-codereview-codex-5-5.md:3692:docs/reviews/r2/pr194-codereview-opus-4-6.md:11:Phase 9 delivers the bandit state machine (spawn, camp, attack, defeat/escape cycle), deterministic attack resolution, loot distribution, and a significant refactor of the heartbeat seed timing. The state machine is well-structured and the heartbeat ordering is correct. One HIGH-severity bug exists in `_isBanditSpawnRegionCandidate` using the wrong seed for its pre-spawn candidacy check, which causes eager-settle to diverge from the actual spawn decision. Four MEDIUM-severity issues affect the `getClanFullView` defender display, simulation starvation check, wall damage accounting, and missing integration test coverage. **Merge is safe after the HIGH fix; MEDIUMs can be follow-up issues.**
6709:# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
6710:## SUMMARY
6711:## HIGH severity findings
6712:## MEDIUM severity findings
6713:## LOW severity findings
6716:DIFF FOLLOWS.
6933:# Phase Super-Swarm Review — PR #250 (head e4a0d4c)
6934:## SUMMARY
6935:## HIGH severity findings
6936:## MEDIUM severity findings
6937:## LOW severity findings
6940:DIFF FOLLOWS.

exec
/bin/bash -lc 'rg -n "''^# Phase|''^## SUMMARY|''^## HIGH|''^## MEDIUM|''^## LOW|HIGH|MEDIUM|LOW|NEEDS_FIXES|LGTM|finding" docs/reviews/pr250-codereview-opus-4-7.md' in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc "sed -n '246,256p' docs/planning/clanworld_numbered_implementation_plan.md" in /home/claude/code/clan-world
 succeeded in 0ms:
- proper event emission

### 5.6 Implement starvation consequences
- next-tick starvation start
- 50% gathering penalty
- 0 defense contribution

**Definition of done**
- resources can be gathered, carried, deposited, and consumed by the economy

**Cut line:** wheat regrow can be simplified temporarily if necessary, but deposit and starvation cannot.

exec
/bin/bash -lc "sed -n '166,176p' 'docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_1_addendum.md'" in /home/claude/code/clan-world
 succeeded in 0ms:

## A10. No change to summer starvation lethality

Starvation outside winter does **not** directly kill clansmen.

While starving outside winter:
- gathering output is reduced by 50%
- defense contribution becomes 0

Clansman death from deprivation occurs only through the winter cold-damage / wall-collapse pathway already specified in v4.


exec
/bin/bash -lc "sed -n '413,439p' docs/planning/clanworld_numbered_implementation_plan.md" in /home/claude/code/clan-world
 succeeded in 0ms:
## Phase 10 — winter and elimination

### 10.1 Implement winter schedule
- first winter at tick 110
- duration 10 ticks
- repeated cadence

### 10.2 Implement winter upkeep
- doubled food upkeep
- wood burn

### 10.3 Implement cold damage
- accumulation
- wall degradation
- clansman deaths once walls are zero
- reset at winter end

### 10.4 Implement crop winter transitions
- clear plots
- lock during winter
- restart regrow after winter

### 10.5 Implement clan death
- set dead state
- burn physical vault loot
- preserve dead-clan restrictions


collab: Wait
collab: Wait
exec
/bin/bash -lc "sed -n '330,390p' packages/contracts/src/ClanWorld.sol && sed -n '360,420p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:

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
            if (clan.clanState == ClanState.DEAD) break;

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
            if (clan.clanState == ClanState.DEAD) break;

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

        if (curTick > fromTick && !_isWinterActiveAt(curTick) && _isWinterActiveAt(curTick - 1)) {
            clan.coldDamage = 0;
        }

        clan.lastSettledTick = curTick;
        emit ClanSettled(clanId, curTick);
    }

    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food and cold damage if winter wood is short.
    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
        bool winter = _isWinterActiveAt(tick);
        if (!winter && tick > 0 && _isWinterActiveAt(tick - 1)) {
            clan.coldDamage = 0;
        }

        if (clan.livingClansmen == 0) return;

        uint256 wheatNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.WHEAT_UPKEEP_PER_CLANSMAN;
        uint256 fishNeeded = uint256(clan.livingClansmen) * ClanWorldConstants.FISH_UPKEEP_PER_CLANSMAN;
        if (winter) {
            wheatNeeded = wheatNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
            fishNeeded = fishNeeded * ClanWorldConstants.WINTER_UPKEEP_MULTIPLIER_BPS / 10000;
        }

        bool hadEnoughWheat = clan.vaultWheat >= wheatNeeded;
        bool hadEnoughFish = clan.vaultFish >= fishNeeded;

        if (hadEnoughWheat) {

exec
/bin/bash -lc 'rg -n "reserved|pending|UpgradeWall|UpgradeBase|UpgradeMonument|_refund|refund|pendingWall|wall.*Reserved|base.*Reserved|monument" packages/contracts/src/ClanWorld.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
628:            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
889:        } else if (action == ActionType.UpgradeBase) {
890:            success = _tryUpgradeBase(clan, clanId, tick);
891:        } else if (action == ActionType.UpgradeMonument) {
892:            success = _tryUpgradeMonument(clan, clanId, tick);
935:    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
972:    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
973:        uint8 nextLevel = clan.monumentLevel + 1;
1020:        uint8 old = clan.monumentLevel;
1021:        clan.monumentLevel = nextLevel;
1260:        clan.monumentLevel = 0;
1860:        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
1861:        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
2056:            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
2057:                || action == ActionType.UpgradeMonument || action == ActionType.MarketBuy
2221:                monumentLevel: clan.monumentLevel,

exec
/bin/bash -lc "sed -n '2200,2450p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

        WorldState memory ws = _worldStateView();
        return WorldSnapshot({
            currentTick: ws.currentTick,
            seasonStartTick: ws.seasonStartTick,
            seasonEndTick: ws.seasonEndTick,
            seasonFinalized: ws.seasonFinalized,
            currentSeasonNumber: ws.currentSeasonNumber,
            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
            winterActive: ws.winterActive,
            winterStartsAtTick: ws.winterStartsAtTick,
            winterEndsAtTick: ws.winterEndsAtTick,
            activeBanditId: ws.activeBanditId,
            currentTickSeed: ws.currentTickSeed,
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

        DerivedClanState memory derivedClan =
            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});

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
                clansman: cs, activeMission: m, effectiveRegion: effRegion, derivedAtTick: _world.currentTick
            });
            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: m});
        }

        // Find if any of this clan's clansmen is defending a home region.
        uint32 thisClanDefendingBaseId = 0;
        for (uint256 i = 0; i < csIds.length; i++) {
            uint8 region = _clansmanDefendingRegion[csIds[i]];
            if (region != 0) {
                thisClanDefendingBaseId = region;
                break;
            }
        }

        return ClanFullView({
            clan: derivedClan,
            clansmen: clansmen,
            westPlot: _wheatPlots[clanId][0],
            eastPlot: _wheatPlots[clanId][1],
            incomingDefenderIds: _defendingClansByRegion[clan.baseRegion],
            thisClanDefendingBaseId: thisClanDefendingBaseId
        });
    }

    function getMarketState() external view override returns (MarketState memory) {
        return MarketState({
            wood: _poolReserves(_treasury.woodToken, _treasury.woodGoldPool),
            wheat: _poolReserves(_treasury.wheatToken, _treasury.wheatGoldPool),
            fish: _poolReserves(_treasury.fishToken, _treasury.fishGoldPool),
            iron: _poolReserves(_treasury.ironToken, _treasury.ironGoldPool),
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
    function getRegionPopulation(uint8 region) external view override returns (RegionOccupant[] memory) {
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

codex
# Phase Super-Swarm Review — PR #250 (head e4a0d4c)

## SUMMARY

NEEDS FIXES. The A2 winter cadence change to first winter at tick `110` is correct, but the phase has several merge blockers: winter upkeep is overcharging wood, cold-death RNG is not deterministic under lazy settlement, starvation now has an extra lethal path that conflicts with the written starvation rules, and winter event ABI compatibility is broken.

## HIGH severity findings

`packages/contracts/src/ClanWorld.sol::_killRandomClansmanFromCold` — Cold-death RNG uses `_world.currentTickSeed` while replaying historical settlement ticks. A cold death for tick `110` can pick a different clansman depending on whether `settleClan()` is called at tick `111` or tick `120`. Use the seed for the settled tick, e.g. `_tickSeeds[tick]`, not the latest world seed.

`packages/contracts/src/IClanWorld.sol`, `packages/contracts/src/ClanWorld.sol::_applyUpkeep` — Winter wood burn is implemented as per-clansman (`livingClansmen * 0.5e18`), but spec §7.3 says `1e18` wood per base per tick. A normal 4-clansman clan burns `2e18`; the new test codifies the wrong expectation.

`packages/contracts/src/ClanWorld.sol::_applyUpkeep`, `_killNextClansmanFromStarvation` — Starvation directly kills clansmen during winter, and can kill on the same tick starvation is first detected. Spec §4.12/A10 says starvation is a debuff path, while deprivation deaths happen through winter cold damage / wall collapse. If this is intentional, the spec needs to change; as written, this is a behavior bug.

`packages/contracts/src/IClanWorld.sol`, `packages/contracts/abi/IClanWorld.json` — `WinterStarted` / `WinterEnded` changed from `uint64` to `uint32`, changing event signature hashes. Existing indexers listening for `WinterStarted(uint64)` / `WinterEnded(uint64)` will silently miss transitions. Keep `uint64` unless all consumers migrate together.

## MEDIUM severity findings

`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.

`packages/contracts/src/ClanWorld.sol::_markClanDead` — Clan death clears the dead clan’s own defenders, but does not release external `DefendBase` missions targeting the dead clan. That can leave other clans’ persistent defender missions/registries stuck on a dead base.

`packages/contracts/src/ClanWorld.sol::submitClanOrders` — Dead-clan order behavior is split: already-dead clans still hit the pre-settlement revert, while clans that die during lazy settlement return per-order `ERR_CLAN_DEAD`. Pick one API contract and test both paths.

## LOW severity findings

`packages/contracts/src/lib/RNG.sol` — `DOMAIN_COLD_DAMAGE = keccak256("cold_damage")` is not namespaced/versioned like the other RNG domains. Prefer the established `clanworld.*.v1` style unless the literal domain is externally specified.

Tests cover happy paths, but miss the exact boundary assertions at `currentTick == 110` and `currentTick == 120`, deterministic cold victim selection across delayed settlement, and the spec-required crop clearing at winter start.

## Cross-cutting observations

`ClanState.DEAD` is the right anchor for Phase 7.5 OTC restrictions, and status codes were appended rather than inserted, which preserves existing numeric values. The death helper is close, but before merge it needs to become the single reliable lifecycle path for starvation/cold/bandit death, including vault burn, mission cleanup, defender release, and consistent events.
2026-04-29T22:32:26.686846Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5a-e95f-7660-9aaa-e3290b8931f8 not found
2026-04-29T22:32:26.687146Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5b-4982-7491-b74c-4223021aff1d not found
2026-04-29T22:32:26.687470Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5b-4a10-7973-9cb4-561746aeba78 not found
2026-04-29T22:32:26.687527Z ERROR codex_core::session: failed to record rollout items: thread 019ddb5b-4a83-7712-bacd-f6c8d8099c3a not found
tokens used
120,885
# Phase Super-Swarm Review — PR #250 (head e4a0d4c)

## SUMMARY

NEEDS FIXES. The A2 winter cadence change to first winter at tick `110` is correct, but the phase has several merge blockers: winter upkeep is overcharging wood, cold-death RNG is not deterministic under lazy settlement, starvation now has an extra lethal path that conflicts with the written starvation rules, and winter event ABI compatibility is broken.

## HIGH severity findings

`packages/contracts/src/ClanWorld.sol::_killRandomClansmanFromCold` — Cold-death RNG uses `_world.currentTickSeed` while replaying historical settlement ticks. A cold death for tick `110` can pick a different clansman depending on whether `settleClan()` is called at tick `111` or tick `120`. Use the seed for the settled tick, e.g. `_tickSeeds[tick]`, not the latest world seed.

`packages/contracts/src/IClanWorld.sol`, `packages/contracts/src/ClanWorld.sol::_applyUpkeep` — Winter wood burn is implemented as per-clansman (`livingClansmen * 0.5e18`), but spec §7.3 says `1e18` wood per base per tick. A normal 4-clansman clan burns `2e18`; the new test codifies the wrong expectation.

`packages/contracts/src/ClanWorld.sol::_applyUpkeep`, `_killNextClansmanFromStarvation` — Starvation directly kills clansmen during winter, and can kill on the same tick starvation is first detected. Spec §4.12/A10 says starvation is a debuff path, while deprivation deaths happen through winter cold damage / wall collapse. If this is intentional, the spec needs to change; as written, this is a behavior bug.

`packages/contracts/src/IClanWorld.sol`, `packages/contracts/abi/IClanWorld.json` — `WinterStarted` / `WinterEnded` changed from `uint64` to `uint32`, changing event signature hashes. Existing indexers listening for `WinterStarted(uint64)` / `WinterEnded(uint64)` will silently miss transitions. Keep `uint64` unless all consumers migrate together.

## MEDIUM severity findings

`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.

`packages/contracts/src/ClanWorld.sol::_markClanDead` — Clan death clears the dead clan’s own defenders, but does not release external `DefendBase` missions targeting the dead clan. That can leave other clans’ persistent defender missions/registries stuck on a dead base.

`packages/contracts/src/ClanWorld.sol::submitClanOrders` — Dead-clan order behavior is split: already-dead clans still hit the pre-settlement revert, while clans that die during lazy settlement return per-order `ERR_CLAN_DEAD`. Pick one API contract and test both paths.

## LOW severity findings

`packages/contracts/src/lib/RNG.sol` — `DOMAIN_COLD_DAMAGE = keccak256("cold_damage")` is not namespaced/versioned like the other RNG domains. Prefer the established `clanworld.*.v1` style unless the literal domain is externally specified.

Tests cover happy paths, but miss the exact boundary assertions at `currentTick == 110` and `currentTick == 120`, deterministic cold victim selection across delayed settlement, and the spec-required crop clearing at winter start.

## Cross-cutting observations

`ClanState.DEAD` is the right anchor for Phase 7.5 OTC restrictions, and status codes were appended rather than inserted, which preserves existing numeric values. The death helper is close, but before merge it needs to become the single reliable lifecycle path for starvation/cold/bandit death, including vault burn, mission cleanup, defender release, and consistent events.
