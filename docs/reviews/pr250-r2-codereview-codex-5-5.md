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
session id: 019ddb7d-f252-7301-b23b-26009debd9f5
--------
user
Read prompt+diff. Use parallel tool calls + subagents. Output review.

<stdin>
You are reviewing the FIX-ROUND for PR #250 (Phase 10 Winter + Elimination) at head 1210e90.

Previous super-swarm found 4 HIGHs (3 cross-model + 1 single-model) + several MEDs. The fix-round (PR #287 merged) addressed:
- H2 starvation guard inversion + off-by-one timing (drop >= clause + defer kill to tick+1)
- H3 wood burn per-base ON TOP of per-clansman (per Liam directive — both fairness knobs)
- H4 uint64 ABI revert (events back to uint64 from uint32)
- H5 wheat plot clear at winter start (per spec §843)

NOT in fix-round: H1 cold-damage RNG seed (Liam parked as MAYBE-future #283 — could be discoverable mechanic).

Verify the 4 fixes work + no new bugs. Specifically check:
1. Starvation kill timing — pre-winter starver dies at SAME cadence as fresh-winter starver
2. Wood burn = (clansmen × 0.5e18) + (1 × 1e18 per base) for 4-clansman = 3e18/winter-tick
3. WinterStarted/WinterEnded events are uint64
4. Wheat plots at winter start: state=WinterLocked AND remainingWheat=0 AND regrow cleared
5. Spec §7.3 patched to document per-base wood burn addition

Plus standard review: cross-cutting bugs introduced by the fix.

USE PARALLEL TOOL CALLS.

## Output

# Phase Super-Swarm Review (R2) — PR #250 (head 1210e90)
## SUMMARY
## HIGH severity findings (or "CLEAN — no findings")
## MEDIUM severity findings
## LOW severity findings
## Cross-cutting observations

DIFF FOLLOWS.
---
diff --git a/docs/planning/clanworld_v4_spec.md b/docs/planning/clanworld_v4_spec.md
index b8296b4..3b1177f 100644
--- a/docs/planning/clanworld_v4_spec.md
+++ b/docs/planning/clanworld_v4_spec.md
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
index 2ca0d2c..c2920b2 100644
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
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..17517c2 100644
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
+        if (winter && starving && clan.starvationStartsAtTick < tick) {
+            _killNextClansmanFromStarvation(clan, tick);
+        }
+        if (clan.clanState == ClanState.DEAD) return;
+
+        if (winter) {
+            uint256 woodNeeded = ClanWorldConstants.WINTER_WOOD_BURN_PER_BASE
+                + uint256(clan.livingClansmen) * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
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
@@ -942,39 +1113,102 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
+    function _resetColdDamageForAllClans() internal {
+        for (uint256 i = 0; i < _allClanIds.length; i++) {
+            _clans[_allClanIds[i]].coldDamage = 0;
+        }
+    }
+
+    function _winterEventTick(uint64 tick) internal pure returns (uint64) {
+        return tick;
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
@@ -1039,15 +1273,18 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1090,7 +1327,6 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
         // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
-        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
         {
             uint64 lastSettled = _clans[clanId].lastSettledTick;
             if (_world.currentTick > lastSettled + 200) {
@@ -1098,7 +1334,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
                 for (uint256 i = 0; i < orders.length; i++) {
                     results[i] = OrderResult({
                         clansmanId: orders[i].clansmanId,
-                        status: StatusCode.ERR_INVALID_ACTION,
+                        status: StatusCode.ERR_MUST_SETTLE_FIRST,
                         cooldownEndsAtTs: 0,
                         missionNonce: 0
                     });
@@ -1111,6 +1347,17 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
@@ -1646,6 +1893,9 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
             ) {
                 return StatusCode.ERR_INVALID_REGION;
             }
+            if (_isWinterActiveAt(_world.currentTick)) {
+                return StatusCode.ERR_WINTER_LOCKED;
+            }
         }
 
         if (action == ActionType.DefendBase) {
@@ -1761,7 +2011,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // =========================================================================
 
     function getWorldState() external view override returns (WorldState memory) {
-        return _world;
+        return _worldStateView();
     }
 
     function getTreasuryState() external view override returns (TreasuryState memory) {
@@ -1793,6 +2043,10 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
     }
 
+    function isWinter() external view override returns (bool) {
+        return _isWinterActiveAt(_world.currentTick);
+    }
+
     function getActionDuration(ActionType action) public pure override returns (uint64) {
         if (
             action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
@@ -1976,18 +2230,19 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
index 2b80fbe..6a44d2c 100644
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
@@ -60,8 +61,11 @@ library ClanWorldConstants {
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
@@ -176,7 +180,9 @@ enum StatusCode {
     ERR_NO_ACTIVE_BANDIT,
     ERR_SEASON_ENDED,
     ERR_NOT_ENOUGH_GOLD,
-    ERR_CARRY_FULL
+    ERR_CARRY_FULL,
+    ERR_WINTER_LOCKED,
+    ERR_MUST_SETTLE_FIRST
 }
 
 // =============================================================================
@@ -188,8 +194,8 @@ struct WorldState {
     uint64 seasonStartTick;
     uint64 seasonEndTick;
     bool seasonFinalized;
-    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
-    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
+    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
+    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
 
     uint64 nextHeartbeatAtTs;
     uint64 nextBanditSpawnEligibleTick;
@@ -499,7 +505,11 @@ interface IClanWorldEvents {
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
@@ -710,6 +720,9 @@ interface IClanWorld is IClanWorldEvents {
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
index 92781d3..3f0611b 100644
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
@@ -1920,56 +1991,389 @@ contract ClanWorldTest is Test {
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
-        assertEq(
-            ws.winterStartsAtTick,
-            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
-        );
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
+        emit IClanWorldEvents.ClanColdShortage(clanId, uint32(winterStart), 2e18);
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
+    function test_pre_winter_starver_dies_in_winter_at_same_cadence() public {
+        ClanWorldTestHarness harness = new ClanWorldTestHarness();
+        world = harness;
+        uint32 preWinterStarver = _mintClan();
+        uint32 winterStarver = _mintClan();
+        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
+        _advanceToTick(winterStart + 6);
+
+        harness.setClanUpkeepState(preWinterStarver, winterStart + 3, 100e18, 0, 0, 0);
+        harness.setClanStarvationStartsAtTick(preWinterStarver, winterStart - 5);
+        harness.setClanUpkeepState(winterStarver, winterStart + 3, 100e18, 0, 0, 0);
+        harness.setClanStarvationStartsAtTick(winterStarver, winterStart + 2);
+
+        world.settleClan(preWinterStarver);
+        world.settleClan(winterStarver);
+
+        Clan memory preWinterClan = world.getClan(preWinterStarver);
+        Clan memory winterClan = world.getClan(winterStarver);
+        assertEq(preWinterClan.livingClansmen, 1, "pre-winter starver should lose one clansman per winter tick");
+        assertEq(winterClan.livingClansmen, 1, "fresh winter starver should lose clansmen at the same cadence");
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
@@ -1982,9 +2386,8 @@ contract ClanWorldTest is Test {
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
I’ll review this as a fix-round audit: first confirm the five requested checks, then look for cross-cutting regressions the diff may have introduced. I’ll split a couple of focused code-review passes into subagents and inspect the contract/tests locally in parallel.
2026-04-29T23:06:14.777342Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-29T23:06:14.828458Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
exec
/bin/bash -lc 'rg -n "function _applyUpkeep|function _settleClan|function _processOrder|function _validateOrder|function submitClanOrders|function heartbeat|event WinterStarted|event WinterEnded|enum WheatPlotState|struct WheatPlot|ERR_CLAN_DEAD|ERR_WINTER_LOCKED|WINTER_START_TICK|TICKS_PER_WINTER_CYCLE|ClanDied|ClanColdShortage|WallDegradedByCold|ClansmanColdDeath" packages/contracts/src packages/contracts/test docs/planning/clanworld_v4_spec.md' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:94:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
packages/contracts/src/ClanWorld.sol:96:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/src/ClanWorld.sol:97:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:354:    function _settleClan(uint32 clanId) internal {
packages/contracts/src/ClanWorld.sol:398:    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:878:    function heartbeat() external override nonReentrant {
packages/contracts/src/ClanWorld.sol:948:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:950:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:965:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:967:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:1081:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
packages/contracts/src/ClanWorld.sol:1136:    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
packages/contracts/src/IClanWorld.sol:29:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
packages/contracts/src/IClanWorld.sol:121:enum WheatPlotState {
packages/contracts/src/IClanWorld.sol:159:    ERR_CLAN_DEAD,
packages/contracts/src/IClanWorld.sol:259:struct WheatPlot {
packages/contracts/src/IClanWorld.sol:500:    event WinterStarted(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:501:    event WinterEnded(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:652:    function heartbeat() external;
packages/contracts/src/IClanWorld.sol:672:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders) external returns (OrderResult[] memory);
packages/contracts/src/ClanWorldStub.sol:55:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorldStub.sol:78:    function heartbeat() external override {
packages/contracts/src/ClanWorldStub.sol:100:    function submitClanOrders(uint32, ClanOrder[] calldata orders)
packages/contracts/test/ClanWorld.t.sol:2421:            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2430:        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/test/ClanWorld.t.sol:2456:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/test/ClanWorld.t.sol:2466:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2482:            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/test/ClanWorldStub.t.sol:47:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);

exec
/bin/bash -lc 'git status --short && git rev-parse --show-toplevel && git rev-parse HEAD' in /home/claude/code/clan-world
 succeeded in 0ms:
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
?? docs/reviews/pr199-codereview-codex-5-4.md
?? docs/reviews/pr199-codereview-codex-5-5.md
?? docs/reviews/pr199-codereview-opus-4-7.md
?? docs/reviews/pr199-synthesis.md
?? docs/reviews/pr200-codereview-codex-5-4.md
?? docs/reviews/pr200-codereview-codex-5-5.md
?? docs/reviews/pr200-codereview-opus-4-7.md
?? docs/reviews/pr200-synthesis.md
?? docs/reviews/pr250-r2-codereview-codex-5-5.md
?? docs/reviews/pr250-r2-codereview-opus-4-7.md
?? docs/reviews/r2-phase10/
?? docs/reviews/r2-phase6/
?? docs/reviews/r2/
/home/claude/code/omnipass-world/clan-world
fdcc04b5bbdee4f3a9f2c5479d119cdbdba3ae92

codex
The local checkout isn’t at the provided fix-round head, so I’m treating the pasted diff as the source of truth and using the repo mostly for surrounding shape. I hit a spawn parameter constraint on the first attempt; re-launching those subreviews with inherited agent type now.
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "sed -n '1,220p' packages/contracts/src/IClanWorld.sol && sed -n '490,510p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
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
    uint256 internal constant WOOD_CARRY_CAP = 10e18;
    uint256 internal constant WOOD_CAP = WOOD_CARRY_CAP;
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

    // Market event resource ids. ResourceType covers the four vault resources;
    // gold is the quote asset emitted as resource id 4.
    uint8 internal constant RESOURCE_GOLD = 4;

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
    ERR_WORLD_TICK_MISMATCH,
    ERR_NO_ACTIVE_BANDIT,
    ERR_SEASON_ENDED,
    ERR_NOT_ENOUGH_GOLD,
    ERR_CARRY_FULL,
    ERR_LIQUIDITY_INSUFFICIENT,
    ERR_MAX_GOLD_IN_EXCEEDED
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
        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
    );
    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);

exec
/bin/bash -lc "sed -n '860,990p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        m.active = false;
        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
    }

    // =========================================================================
    // WORLD PROGRESSION
    // =========================================================================

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

exec
/bin/bash -lc "sed -n '330,460p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

    /// @dev Resolve an action for a clansman that is in ACTING state.
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
            // Deposit event ABI intentionally stores the current tick as uint32.
            // forge-lint: disable-next-line(unsafe-typecast)
            _doDeposit(clan, cs, m, clanId, uint32(tick));
        } else if (action == ActionType.Wait) {
            // NOOP — worker stays ACTING (continuous), no transition needed

exec
/bin/bash -lc "sed -n '1080,1170p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
        external
        override
        nonReentrant
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
                        missionNonce: 0,
                        marketMode: MarketExecutionMode.None
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
        uint32 targetClanId;
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

        if (order.action == ActionType.DefendBase) {
            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
            if (defendErr != StatusCode.OK) {
                result.status = defendErr;
                return result;
            }

            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
            Mission storage currentM = _missions[order.clansmanId];
            if (
                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
                    && currentM.targetClanId == defendTargetClanId
            ) {
                result.status = StatusCode.OK;
                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
                result.missionNonce = currentM.nonce;
                return result;
            }

codex
I’m already seeing one thing worth treating carefully: the diff changes the winter schedule constants from a derived first window `[100, 110)` to a literal first window `[110, 120)`. I’m checking whether that is intentional spec movement or a behavioral regression before assigning severity.
exec
/bin/bash -lc "rg -n \"winter|WINTER_START|TICKS_PER_WINTER|\\[100|110|7\\.3|7\\.4\" docs/planning/clanworld_v4_spec.md docs/reference packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
docs/planning/clanworld_v4_spec.md:5:**Scope:** World layout, tick semantics, settlement rules, missions, resources, market execution, bandits, winter, building, victory, human steering, trust model, spawn rules, elimination rules, pool seeding, and season lifecycle.
docs/planning/clanworld_v4_spec.md:11:ClanWorld is an autonomous strategy game in which each player owns a homebase iNFT and an Elder agent that directs a small clan of workers across a shared world. Workers travel between regions, gather and deposit materials, defend bases, survive winter, trade assets, and compete to build the tallest monument.
docs/planning/clanworld_v4_spec.md:403:- winter wood burn
docs/planning/clanworld_v4_spec.md:826:Winter occurs every `110` ticks.
docs/planning/clanworld_v4_spec.md:831:## 7.3 Winter upkeep
docs/planning/clanworld_v4_spec.md:832:During winter, each clan consumes:
docs/planning/clanworld_v4_spec.md:837:## 7.4 Winter farming rule
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
docs/planning/clanworld_v4_spec.md:1194:- winter every 110 ticks for 10 ticks
docs/planning/clanworld_v4_spec.md:1195:- cold damage reset each winter
packages/contracts/src/ClanWorld.sol:48:///         Phase 2 is implemented; Phase 3 (bandits, winter damage) remains stubbed.
packages/contracts/src/ClanWorld.sol:94:        // First winter: last WINTER_DURATION_TICKS of first TICKS_PER_WINTER_CYCLE cycle
packages/contracts/src/ClanWorld.sol:95:        // i.e. ticks [100, 110)
packages/contracts/src/ClanWorld.sol:96:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/src/ClanWorld.sol:97:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:98:        _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:876:    ///         4. Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:901:        // Step 4: Resolve world events (season boundary, winter transitions).
packages/contracts/src/ClanWorld.sol:946:            // reset winter timers for new season
packages/contracts/src/ClanWorld.sol:947:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:948:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:950:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:953:        // --- winter transitions (timer only; mechanics = Phase 10) ---
packages/contracts/src/ClanWorld.sol:955:            !_world.winterActive && newTick >= _world.winterStartsAtTick
packages/contracts/src/ClanWorld.sol:956:                && _world.winterStartsAtTick < _world.seasonEndTick
packages/contracts/src/ClanWorld.sol:958:            _world.winterActive = true;
packages/contracts/src/ClanWorld.sol:961:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
packages/contracts/src/ClanWorld.sol:962:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:964:            // schedule next winter cycle within this season
packages/contracts/src/ClanWorld.sol:965:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:967:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:969:                _world.winterStartsAtTick = nextWinterStart;
packages/contracts/src/ClanWorld.sol:970:                _world.winterEndsAtTick = nextWinterEnd;
packages/contracts/src/ClanWorld.sol:972:                // no more winters this season; sentinel = seasonEndTick so guard never fires
packages/contracts/src/ClanWorld.sol:973:                _world.winterStartsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:974:                _world.winterEndsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:2454:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorld.sol:2455:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorld.sol:2456:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/src/IClanWorld.sol:29:    uint64 internal constant TICKS_PER_WINTER_CYCLE = 110;
packages/contracts/src/IClanWorld.sol:207:    bool winterActive;
packages/contracts/src/IClanWorld.sol:208:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:209:    uint64 winterEndsAtTick; // 0 if not active
packages/contracts/src/IClanWorld.sol:248:    uint16 coldDamage; // resets to 0 at winter end
packages/contracts/src/IClanWorld.sol:423:    bool winterActive;
packages/contracts/src/IClanWorld.sol:424:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:425:    uint64 winterEndsAtTick;
packages/contracts/src/IClanWorld.sol:625:    // ----- winter cold damage -----
packages/contracts/src/IClanWorld.sol:776:    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorldStub.sol:55:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorldStub.sol:57:        _world.winterActive = false;
packages/contracts/src/ClanWorldStub.sol:318:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorldStub.sol:319:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorldStub.sol:320:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:47:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
packages/contracts/test/ClanWorldStub.t.sol:50:        assertFalse(ws.winterActive);
packages/contracts/test/ClanWorld.t.sol:2412:    // Phase 4.4 — season + winter timer tests
packages/contracts/test/ClanWorld.t.sol:2421:            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2423:        assertFalse(ws.winterActive);
packages/contracts/test/ClanWorld.t.sol:2426:    function test_winter_onset() public {
packages/contracts/test/ClanWorld.t.sol:2427:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
packages/contracts/test/ClanWorld.t.sol:2428:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
packages/contracts/test/ClanWorld.t.sol:2430:        uint64 winterStart = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/test/ClanWorld.t.sol:2431:        for (uint64 i = 0; i < winterStart - 1; i++) {
packages/contracts/test/ClanWorld.t.sol:2441:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
packages/contracts/test/ClanWorld.t.sol:2444:            if (logs[i].topics.length > 0 && logs[i].topics[0] == winterSig) {
packages/contracts/test/ClanWorld.t.sol:2450:        assertTrue(world.getWorldState().winterActive, "winter should be active");
packages/contracts/test/ClanWorld.t.sol:2451:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
packages/contracts/test/ClanWorld.t.sol:2454:    function test_winter_end_and_next_cycle() public {
packages/contracts/test/ClanWorld.t.sol:2455:        // Advance past first winter end tick (= 110)
packages/contracts/test/ClanWorld.t.sol:2456:        uint64 winterEnd = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/test/ClanWorld.t.sol:2457:        for (uint64 i = 0; i <= winterEnd; i++) {
packages/contracts/test/ClanWorld.t.sol:2462:        assertFalse(ws.winterActive, "winter should be over");
packages/contracts/test/ClanWorld.t.sol:2463:        // next winter at [210, 220)
packages/contracts/test/ClanWorld.t.sol:2465:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2466:            ClanWorldConstants.TICKS_PER_WINTER_CYCLE * 2 - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2480:        // winter reset for new season
packages/contracts/test/ClanWorld.t.sol:2482:            + ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS;
packages/contracts/test/ClanWorld.t.sol:2483:        assertEq(ws.winterStartsAtTick, expectedWinterStart);

exec
/bin/bash -lc "rg -n \"enum StatusCode|ERR_LIQUIDITY|ERR_MAX_GOLD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|StatusCode\" packages -g '*.ts' -g '*.tsx' -g '*.sol' -g '*.json'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorldStub.sol:15:    StatusCode,
packages/contracts/src/ClanWorld.sol:15:    StatusCode,
packages/contracts/src/ClanWorld.sol:1094:        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
packages/contracts/src/ClanWorld.sol:1102:                        status: StatusCode.ERR_INVALID_ACTION,
packages/contracts/src/ClanWorld.sol:1145:            result.status = StatusCode.ERR_INVALID_CLANSMAN;
packages/contracts/src/ClanWorld.sol:1149:            result.status = StatusCode.ERR_CLANSMAN_DEAD;
packages/contracts/src/ClanWorld.sol:1154:            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
packages/contracts/src/ClanWorld.sol:1155:            if (defendErr != StatusCode.OK) {
packages/contracts/src/ClanWorld.sol:1166:                result.status = StatusCode.OK;
packages/contracts/src/ClanWorld.sol:1178:            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
packages/contracts/src/ClanWorld.sol:1199:            result.status = StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1204:        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
packages/contracts/src/ClanWorld.sol:1205:        if (actionErr != StatusCode.OK) {
packages/contracts/src/ClanWorld.sol:1217:            StatusCode marketStatus = _executeImmediateMarket(clanId, order, cs.clansmanId);
packages/contracts/src/ClanWorld.sol:1219:            if (marketStatus == StatusCode.OK) {
packages/contracts/src/ClanWorld.sol:1225:            result.status = StatusCode.OK;
packages/contracts/src/ClanWorld.sol:1281:        result.status = StatusCode.OK;
packages/contracts/src/ClanWorld.sol:1397:                    StatusCode.ERR_INVALID_CLANSMAN,
packages/contracts/src/ClanWorld.sol:1413:                    StatusCode.ERR_INVALID_ACTION,
packages/contracts/src/ClanWorld.sol:1423:                    StatusCode marketStatus
packages/contracts/src/ClanWorld.sol:1432:                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:1440:                    StatusCode marketStatus
packages/contracts/src/ClanWorld.sol:1449:                        StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:1487:    ) external returns (StatusCode) {
packages/contracts/src/ClanWorld.sol:1500:    ) external returns (StatusCode) {
packages/contracts/src/ClanWorld.sol:1529:        StatusCode reason,
packages/contracts/src/ClanWorld.sol:1633:        StatusCode reason,
packages/contracts/src/ClanWorld.sol:1635:    ) internal returns (StatusCode) {
packages/contracts/src/ClanWorld.sol:1670:        returns (StatusCode)
packages/contracts/src/ClanWorld.sol:1680:        returns (StatusCode)
packages/contracts/src/ClanWorld.sol:1688:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1699:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1712:                StatusCode.ERR_MISSING_RESOURCES,
packages/contracts/src/ClanWorld.sol:1724:                StatusCode.ERR_MISSING_RESOURCES,
packages/contracts/src/ClanWorld.sol:1741:            return StatusCode.OK;
packages/contracts/src/ClanWorld.sol:1749:                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:1761:    ) internal returns (StatusCode) {
packages/contracts/src/ClanWorld.sol:1768:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1779:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1791:                StatusCode.ERR_CARRY_FULL,
packages/contracts/src/ClanWorld.sol:1805:                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:1817:                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
packages/contracts/src/ClanWorld.sol:1827:                StatusCode.ERR_NOT_ENOUGH_GOLD,
packages/contracts/src/ClanWorld.sol:1845:            return StatusCode.OK;
packages/contracts/src/ClanWorld.sol:1852:                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:1861:        returns (StatusCode)
packages/contracts/src/ClanWorld.sol:1869:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1880:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1893:                StatusCode.ERR_MISSING_RESOURCES,
packages/contracts/src/ClanWorld.sol:1911:        return StatusCode.OK;
packages/contracts/src/ClanWorld.sol:1922:    ) internal returns (StatusCode) {
packages/contracts/src/ClanWorld.sol:1929:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1940:                StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN,
packages/contracts/src/ClanWorld.sol:1952:                StatusCode.ERR_CARRY_FULL,
packages/contracts/src/ClanWorld.sol:1966:                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:1979:                StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
packages/contracts/src/ClanWorld.sol:1989:                StatusCode.ERR_NOT_ENOUGH_GOLD,
packages/contracts/src/ClanWorld.sol:2003:                StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/ClanWorld.sol:2021:        return StatusCode.OK;
packages/contracts/src/ClanWorld.sol:2027:        returns (StatusCode)
packages/contracts/src/ClanWorld.sol:2031:        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
packages/contracts/src/ClanWorld.sol:2035:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2041:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
packages/contracts/src/ClanWorld.sol:2046:            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2050:            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2057:                return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2062:            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2069:                return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2079:            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
packages/contracts/src/ClanWorld.sol:2081:                return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2083:            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
packages/contracts/src/ClanWorld.sol:2087:                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
packages/contracts/src/ClanWorld.sol:2093:                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
packages/contracts/src/ClanWorld.sol:2097:                return StatusCode.ERR_CARRY_FULL;
packages/contracts/src/ClanWorld.sol:2108:        return StatusCode.OK;
packages/contracts/src/ClanWorld.sol:2114:        returns (StatusCode)
packages/contracts/src/ClanWorld.sol:2116:        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:2117:        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
packages/contracts/src/ClanWorld.sol:2118:        return StatusCode.OK;
packages/contracts/src/IClanWorld.sol:157:enum StatusCode {
packages/contracts/src/IClanWorld.sol:185:    ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/src/IClanWorld.sol:186:    ERR_MAX_GOLD_IN_EXCEEDED
packages/contracts/src/IClanWorld.sol:384:    StatusCode status;
packages/contracts/src/IClanWorld.sol:578:        uint32 indexed clanId, uint32 csId, ActionType action, MarketExecutionMode mode, StatusCode reason, uint64 tick
packages/contracts/abi/IClanWorld.json:2434:              "internalType": "enum StatusCode"
packages/contracts/abi/IClanWorld.json:3198:          "internalType": "enum StatusCode"
packages/contracts/test/Reentrancy.t.sol:12:    StatusCode,
packages/contracts/test/Reentrancy.t.sol:132:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "market sell should enqueue");
packages/contracts/test/MissionTiming.t.sol:10:    StatusCode,
packages/contracts/test/MissionTiming.t.sol:75:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");
packages/contracts/test/MissionTiming.t.sol:97:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");
packages/contracts/test/MissionTiming.t.sol:155:        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first wait");
packages/contracts/test/MissionTiming.t.sol:161:        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "second wait");
packages/contracts/test/Gathering.t.sol:10:    StatusCode,
packages/contracts/test/Gathering.t.sol:71:        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
packages/contracts/test/HeartbeatOrdering.t.sol:13:    StatusCode,
packages/contracts/test/HeartbeatOrdering.t.sol:210:        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
packages/contracts/test/HeartbeatOrdering.t.sol:217:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
packages/contracts/test/HeartbeatOrdering.t.sol:340:        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit must succeed");
packages/contracts/test/HeartbeatOrdering.t.sol:347:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell must enqueue");
packages/contracts/test/ClanWorld.t.sol:20:    StatusCode,
packages/contracts/test/ClanWorld.t.sol:351:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "valid order should succeed");
packages/contracts/test/ClanWorld.t.sol:352:        assertEq(uint8(results[1].status), uint8(StatusCode.ERR_INVALID_CLANSMAN), "bad csId should fail");
packages/contracts/test/ClanWorld.t.sol:366:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
packages/contracts/test/ClanWorld.t.sol:370:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE), "second order should hit cooldown");
packages/contracts/test/ClanWorld.t.sol:397:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "NOOP order should succeed");
packages/contracts/test/ClanWorld.t.sol:425:        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
packages/contracts/test/ClanWorld.t.sol:507:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
packages/contracts/test/ClanWorld.t.sol:528:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "empty deposit order should be accepted");
packages/contracts/test/ClanWorld.t.sol:554:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
packages/contracts/test/ClanWorld.t.sol:579:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_REGION), "deposit must target home region");
packages/contracts/test/ClanWorld.t.sol:589:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
packages/contracts/test/ClanWorld.t.sol:633:        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
packages/contracts/test/ClanWorld.t.sol:651:            uint8(StatusCode.ERR_INVALID_ACTION),
packages/contracts/test/ClanWorld.t.sol:667:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
packages/contracts/test/ClanWorld.t.sol:680:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "interrupt order should succeed");
packages/contracts/test/ClanWorld.t.sol:738:            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "market sell should enqueue");
packages/contracts/test/ClanWorld.t.sol:800:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate sell should be accepted");
packages/contracts/test/ClanWorld.t.sol:821:            uint8(r[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE), "market order on cooldown must be rejected"
packages/contracts/test/ClanWorld.t.sol:833:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "out-of-town market order should schedule");
packages/contracts/test/ClanWorld.t.sol:850:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "busy market worker should schedule");
packages/contracts/test/ClanWorld.t.sol:883:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "immediate buy should be accepted");
packages/contracts/test/ClanWorld.t.sol:903:            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/test/ClanWorld.t.sol:912:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
packages/contracts/test/ClanWorld.t.sol:935:            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
packages/contracts/test/ClanWorld.t.sol:943:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
packages/contracts/test/ClanWorld.t.sol:964:            StatusCode.ERR_NOT_ENOUGH_GOLD,
packages/contracts/test/ClanWorld.t.sol:972:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "failed immediate action is still accepted");
packages/contracts/test/ClanWorld.t.sol:994:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
packages/contracts/test/ClanWorld.t.sol:1020:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "sell order should be accepted");
packages/contracts/test/ClanWorld.t.sol:1072:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
packages/contracts/test/ClanWorld.t.sol:1094:    // Test 13: buy_maxGoldIn — buy fails with ERR_MAX_GOLD_IN_EXCEEDED
packages/contracts/test/ClanWorld.t.sol:1104:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "order submission should succeed");
packages/contracts/test/ClanWorld.t.sol:1125:            StatusCode.ERR_MAX_GOLD_IN_EXCEEDED,
packages/contracts/test/ClanWorld.t.sol:1145:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
packages/contracts/test/ClanWorld.t.sol:1158:            StatusCode.ERR_NOT_ENOUGH_GOLD,
packages/contracts/test/ClanWorld.t.sol:1187:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CARRY_FULL), "over-capacity buy should be rejected");
packages/contracts/test/ClanWorld.t.sol:1203:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "buy order should be accepted");
packages/contracts/test/ClanWorld.t.sol:1216:            StatusCode.ERR_LIQUIDITY_INSUFFICIENT,
packages/contracts/test/ClanWorld.t.sol:1240:        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
packages/contracts/test/ClanWorld.t.sol:1267:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first sell order should be accepted");
packages/contracts/test/ClanWorld.t.sol:1275:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "replacement sell order should be accepted");
packages/contracts/test/ClanWorld.t.sol:1391:            assertEq(uint8(results[i].status), uint8(StatusCode.OK), "sell order ok");
packages/contracts/test/ClanWorld.t.sol:1535:        assertEq(uint8(buyFail[0].status), uint8(StatusCode.OK), "failing buy should enqueue");
packages/contracts/test/ClanWorld.t.sol:1549:        assertEq(uint8(sellOk[0].status), uint8(StatusCode.OK), "other clan sell should enqueue");
packages/contracts/test/ClanWorld.t.sol:1589:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "market sell to Forest should fail");
packages/contracts/test/ClanWorld.t.sol:1624:            uint8(StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN),
packages/contracts/test/ClanWorld.t.sol:1627:        assertEq(uint8(results[1].status), uint8(StatusCode.OK), "other batch orders should proceed");
packages/contracts/test/ClanWorld.t.sol:1653:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first DefendBase OK");
packages/contracts/test/ClanWorld.t.sol:1666:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "re-task DefendBase OK");
packages/contracts/test/ClanWorld.t.sol:1689:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "3.E1: status must be OK");
packages/contracts/test/ClanWorld.t.sol:1726:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_CLANSMAN), "3.E2: cross-clan csId must be invalid");
packages/contracts/test/ClanWorld.t.sol:1758:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_CLANSMAN_DEAD), "3.E3: dead clansman must be rejected");
packages/contracts/test/ClanWorld.t.sol:1771:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "3.E4: first order must succeed");
packages/contracts/test/ClanWorld.t.sol:1776:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE), "3.E4: must return ERR_COOLDOWN_ACTIVE");
packages/contracts/test/ClanWorld.t.sol:1793:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "3.E5: order must succeed");
packages/contracts/test/ClanWorld.t.sol:1813:            uint8(StatusCode.ERR_COOLDOWN_ACTIVE),
packages/contracts/test/ClanWorld.t.sol:1823:        assertEq(uint8(r3[0].status), uint8(StatusCode.OK), "3.E5: after cooldown expires must succeed");
packages/contracts/test/ClanWorld.t.sol:1839:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "3.E5b: first order must succeed");
packages/contracts/test/ClanWorld.t.sol:1851:            uint8(StatusCode.OK),
packages/contracts/test/ClanWorld.t.sol:1879:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "DefendBase order must succeed");
packages/contracts/test/ClanWorld.t.sol:1926:            uint8(r0[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: ChopWood to Mountains must be invalid"
packages/contracts/test/ClanWorld.t.sol:1931:        assertEq(uint8(r1[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: MineIron to Forest must be invalid");
packages/contracts/test/ClanWorld.t.sol:1935:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: FishDocks to Forest must be invalid");
packages/contracts/test/ClanWorld.t.sol:1948:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "3.E7: first order must succeed");
packages/contracts/test/ClanWorld.t.sol:1960:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "3.E7: interrupt order must succeed");
packages/contracts/test/ClanWorld.t.sol:2025:        assertEq(uint8(r[0].status), uint8(StatusCode.OK));
packages/contracts/test/ClanWorld.t.sol:2121:        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "path4: first order must succeed");
packages/contracts/test/ClanWorld.t.sol:2139:        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "path4: interrupt order must succeed");
packages/contracts/test/ClanWorld.t.sol:2188:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "path5: DefendBase order must succeed");
packages/contracts/test/ClanWorld.t.sol:2218:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "path6: order must succeed");
packages/contracts/test/ClanWorld.t.sol:2259:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "path6-defender: DefendBase order must succeed");
packages/contracts/test/ClanWorld.t.sol:2399:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "3.E8: order[0] must be OK");
packages/contracts/test/ClanWorld.t.sol:2402:            uint8(StatusCode.ERR_INVALID_CLANSMAN),
packages/contracts/test/ClanWorld.t.sol:2405:        assertEq(uint8(results[2].status), uint8(StatusCode.OK), "3.E8: order[2] must be OK");
packages/contracts/test/ClanWorld.t.sol:2407:            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"
packages/contracts/test/ClanWorld.t.sol:2527:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "immediate sell must succeed");
packages/contracts/test/ClanWorld.t.sol:2563:        assertNotEq(uint8(results[0].status), uint8(StatusCode.OK), "empty carry sell must not succeed");
packages/contracts/test/ClanWorld.t.sol:2587:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_COOLDOWN_ACTIVE),
packages/contracts/test/ClanWorld.t.sol:2607:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "buy: immediate market buy must succeed");
packages/contracts/test/DefendBase.t.sol:10:    StatusCode,
packages/contracts/test/DefendBase.t.sol:104:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "defend_base status");
packages/contracts/test/DefendBase.t.sol:139:        assertEq(uint8(move[0].status), uint8(StatusCode.OK), "move away accepted");
packages/contracts/test/DefendBase.t.sol:149:        assertEq(uint8(defend[0].status), uint8(StatusCode.OK), "defend accepted");
packages/contracts/test/DefendBase.t.sol:180:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "non-home defend rejected");
packages/contracts/test/DefendBase.t.sol:194:        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first status");
packages/contracts/test/DefendBase.t.sol:195:        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "same-target no-op status");
packages/contracts/test/DefendBase.t.sol:210:        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "explicit self is same target");
packages/contracts/test/DefendBase.t.sol:225:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "defend accepted");
packages/contracts/test/DefendBase.t.sol:284:        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "wait retask accepted");

exec
/bin/bash -lc "rg -n \"getWorldState|getWorldSnapshot|winterStartsAtTick|winterEndsAtTick|winterActive|isWinter|WinterStarted|WinterEnded|ClanColdShortage|ClansmanColdDeath|WallDegradedByCold|ClanDied\" apps packages -g '*.ts' -g '*.tsx' -g '*.sol' -g '*.json'" in /home/claude/code/clan-world
 succeeded in 0ms:
apps/server/convex/schema.ts:13:    winterActive: v.optional(v.boolean()),
apps/server/convex/schema.ts:14:    winterStartsAtTick: v.optional(v.number()),
apps/server/convex/schema.ts:15:    winterEndsAtTick: v.optional(v.number()),
packages/runner/src/runnerCastHeartbeat.ts:19: * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
packages/runner/src/runnerCastHeartbeat.ts:32:    name: 'getWorldState',
packages/runner/src/runnerCastHeartbeat.ts:48:          { name: 'winterActive', type: 'bool' },
packages/runner/src/runnerCastHeartbeat.ts:49:          { name: 'winterStartsAtTick', type: 'uint64' },
packages/runner/src/runnerCastHeartbeat.ts:50:          { name: 'winterEndsAtTick', type: 'uint64' },
packages/runner/src/runnerCastHeartbeat.ts:99: * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
packages/runner/src/runnerCastHeartbeat.ts:168:      functionName: 'getWorldState',
packages/shared/src/adapters/IChainClient.ts:28:    name: 'getWorldSnapshot',
packages/shared/src/adapters/IChainClient.ts:39:          { name: 'winterActive', type: 'bool' },
packages/shared/src/adapters/IChainClient.ts:40:          { name: 'winterStartsAtTick', type: 'uint64' },
packages/shared/src/adapters/IChainClient.ts:41:          { name: 'winterEndsAtTick', type: 'uint64' },
packages/shared/src/adapters/IChainClient.ts:284:      functionName: 'getWorldSnapshot',
packages/contracts/src/ClanWorld.sol:96:        _world.winterStartsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS; // = 100
packages/contracts/src/ClanWorld.sol:97:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE; // = 110
packages/contracts/src/ClanWorld.sol:98:        _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:947:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:948:            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:950:            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:955:            !_world.winterActive && newTick >= _world.winterStartsAtTick
packages/contracts/src/ClanWorld.sol:956:                && _world.winterStartsAtTick < _world.seasonEndTick
packages/contracts/src/ClanWorld.sol:958:            _world.winterActive = true;
packages/contracts/src/ClanWorld.sol:959:            emit WinterStarted(newTick);
packages/contracts/src/ClanWorld.sol:961:        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
packages/contracts/src/ClanWorld.sol:962:            _world.winterActive = false;
packages/contracts/src/ClanWorld.sol:963:            emit WinterEnded(newTick);
packages/contracts/src/ClanWorld.sol:965:            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
packages/contracts/src/ClanWorld.sol:967:            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorld.sol:969:                _world.winterStartsAtTick = nextWinterStart;
packages/contracts/src/ClanWorld.sol:970:                _world.winterEndsAtTick = nextWinterEnd;
packages/contracts/src/ClanWorld.sol:973:                _world.winterStartsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:974:                _world.winterEndsAtTick = _world.seasonEndTick;
packages/contracts/src/ClanWorld.sol:2204:    function getWorldState() external view override returns (WorldState memory) {
packages/contracts/src/ClanWorld.sol:2430:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
packages/contracts/src/ClanWorld.sol:2454:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorld.sol:2455:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorld.sol:2456:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/src/IClanWorld.sol:207:    bool winterActive;
packages/contracts/src/IClanWorld.sol:208:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:209:    uint64 winterEndsAtTick; // 0 if not active
packages/contracts/src/IClanWorld.sol:423:    bool winterActive;
packages/contracts/src/IClanWorld.sol:424:    uint64 winterStartsAtTick;
packages/contracts/src/IClanWorld.sol:425:    uint64 winterEndsAtTick;
packages/contracts/src/IClanWorld.sol:500:    event WinterStarted(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:501:    event WinterEnded(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:711:    function getWorldState() external view returns (WorldState memory);
packages/contracts/src/IClanWorld.sol:777:    function getWorldSnapshot() external view returns (WorldSnapshot memory);
packages/contracts/src/ClanWorldStub.sol:54:        _world.winterStartsAtTick =
packages/contracts/src/ClanWorldStub.sol:56:        _world.winterEndsAtTick = ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
packages/contracts/src/ClanWorldStub.sol:57:        _world.winterActive = false;
packages/contracts/src/ClanWorldStub.sol:132:    function getWorldState() external view override returns (WorldState memory) {
packages/contracts/src/ClanWorldStub.sol:310:    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
packages/contracts/src/ClanWorldStub.sol:318:            winterActive: _world.winterActive,
packages/contracts/src/ClanWorldStub.sol:319:            winterStartsAtTick: _world.winterStartsAtTick,
packages/contracts/src/ClanWorldStub.sol:320:            winterEndsAtTick: _world.winterEndsAtTick,
packages/contracts/abi/IClanWorld.json:1966:      "name": "getWorldSnapshot",
packages/contracts/abi/IClanWorld.json:2005:              "name": "winterActive",
packages/contracts/abi/IClanWorld.json:2010:              "name": "winterStartsAtTick",
packages/contracts/abi/IClanWorld.json:2015:              "name": "winterEndsAtTick",
packages/contracts/abi/IClanWorld.json:2083:      "name": "getWorldState",
packages/contracts/abi/IClanWorld.json:2147:              "name": "winterActive",
packages/contracts/abi/IClanWorld.json:2152:              "name": "winterStartsAtTick",
packages/contracts/abi/IClanWorld.json:2157:              "name": "winterEndsAtTick",
packages/contracts/abi/IClanWorld.json:3772:      "name": "WinterEnded",
packages/contracts/abi/IClanWorld.json:3785:      "name": "WinterStarted",
packages/contracts/test/ClanWorldStub.t.sol:28:        assertEq(stub.getWorldState().currentTick, 1);
packages/contracts/test/ClanWorldStub.t.sol:30:        assertEq(stub.getWorldState().currentTick, 2);
packages/contracts/test/ClanWorldStub.t.sol:33:    function test_getWorldSnapshot_returns_current_tick() public {
packages/contracts/test/ClanWorldStub.t.sol:35:        assertEq(stub.getWorldSnapshot().currentTick, 2);
packages/contracts/test/ClanWorldStub.t.sol:39:        WorldState memory ws = stub.getWorldState();
packages/contracts/test/ClanWorldStub.t.sol:46:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorldStub.t.sol:49:        assertEq(ws.winterEndsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE);
packages/contracts/test/ClanWorldStub.t.sol:50:        assertFalse(ws.winterActive);
packages/contracts/test/MissionTiming.t.sol:32:        while (world.getWorldState().currentTick < targetTick) {
packages/contracts/test/MissionTiming.t.sol:68:        uint64 submittedAt = world.getWorldState().currentTick;
packages/contracts/test/MissionTiming.t.sol:165:        assertEq(secondSubmitted, world.getWorldState().currentTick, "second submit uses current tick");
packages/contracts/test/Gathering.t.sol:33:        vm.warp(world.getWorldState().nextHeartbeatAtTs);
packages/contracts/test/Gathering.t.sol:38:        while (world.getWorldState().currentTick < targetTick) {
packages/contracts/test/HeartbeatOrdering.t.sol:69:        while (world.getWorldState().currentTick < targetTick) {
packages/contracts/test/HeartbeatOrdering.t.sol:201:        uint64 t0 = world.getWorldState().currentTick;
packages/contracts/test/HeartbeatOrdering.t.sol:249:        WorldState memory before_ = world.getWorldState();
packages/contracts/test/HeartbeatOrdering.t.sol:256:        WorldState memory after_ = world.getWorldState();
packages/contracts/test/HeartbeatOrdering.t.sol:268:        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
packages/contracts/test/HeartbeatOrdering.t.sol:269:        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
packages/contracts/test/HeartbeatOrdering.t.sol:282:        WorldState memory ws0 = world.getWorldState();
packages/contracts/test/HeartbeatOrdering.t.sol:292:        WorldState memory ws1 = world.getWorldState();
packages/contracts/test/HeartbeatOrdering.t.sol:308:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/HeartbeatOrdering.t.sol:309:        bytes32 seedBefore = world.getWorldState().currentTickSeed;
packages/contracts/test/HeartbeatOrdering.t.sol:313:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "tick must increment");
packages/contracts/test/HeartbeatOrdering.t.sol:314:        assertNotEq(world.getWorldState().currentTickSeed, seedBefore, "seed must change");
packages/contracts/test/HeartbeatOrdering.t.sol:332:        uint64 t0 = world.getWorldState().currentTick;
packages/contracts/test/Reentrancy.t.sol:135:        uint64 currentTick = world.getWorldState().currentTick;
packages/contracts/test/Reentrancy.t.sol:201:        vm.warp(world.getWorldState().nextHeartbeatAtTs);
packages/contracts/test/ClanWorld.t.sol:194:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:198:        uint64 tickAfter = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:207:        WorldState memory beforeFirst = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:214:        WorldState memory afterFirst = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:223:        WorldState memory afterSecond = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:233:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:238:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "non-owner caller should advance heartbeat");
packages/contracts/test/ClanWorld.t.sol:243:        uint64 previousTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:252:            WorldState memory state = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:744:        while (world.getWorldState().currentTick < tick) {
packages/contracts/test/ClanWorld.t.sol:793:            world.getWorldState().currentTick
packages/contracts/test/ClanWorld.t.sol:809:        assertEq(world.getScheduledMarketActionsForTick(world.getWorldState().currentTick).length, 0, "no queue entry");
packages/contracts/test/ClanWorld.t.sol:877:            world.getWorldState().currentTick
packages/contracts/test/ClanWorld.t.sol:904:            world.getWorldState().currentTick
packages/contracts/test/ClanWorld.t.sol:936:            world.getWorldState().currentTick
packages/contracts/test/ClanWorld.t.sol:965:            world.getWorldState().currentTick
packages/contracts/test/ClanWorld.t.sol:1028:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1077:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1108:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1245:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1289:        while (world.getWorldState().currentTick <= oldExecuteAtTick) {
packages/contracts/test/ClanWorld.t.sol:1294:        while (world.getWorldState().currentTick <= newExecuteAtTick) {
packages/contracts/test/ClanWorld.t.sol:1399:        while (world.getWorldState().currentTick < executeAtTick) {
packages/contracts/test/ClanWorld.t.sol:1476:        uint64 curTick = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:1557:        while (world.getWorldState().currentTick <= maxTick) {
packages/contracts/test/ClanWorld.t.sol:2416:        WorldState memory ws = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2421:            ws.winterStartsAtTick, ClanWorldConstants.TICKS_PER_WINTER_CYCLE - ClanWorldConstants.WINTER_DURATION_TICKS
packages/contracts/test/ClanWorld.t.sol:2423:        assertFalse(ws.winterActive);
packages/contracts/test/ClanWorld.t.sol:2427:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
packages/contracts/test/ClanWorld.t.sol:2428:        // i.e. when closedTick=99, newTick=100 >= winterStartsAtTick=100.
packages/contracts/test/ClanWorld.t.sol:2429:        // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
packages/contracts/test/ClanWorld.t.sol:2435:        // currentTick == 99; next heartbeat opens tick 100 and should emit WinterStarted(100)
packages/contracts/test/ClanWorld.t.sol:2441:        bytes32 winterSig = keccak256("WinterStarted(uint64)");
packages/contracts/test/ClanWorld.t.sol:2442:        bool foundWinterStarted = false;
packages/contracts/test/ClanWorld.t.sol:2445:                foundWinterStarted = true;
packages/contracts/test/ClanWorld.t.sol:2449:        assertTrue(foundWinterStarted, "WinterStarted event should have been emitted at tick 100");
packages/contracts/test/ClanWorld.t.sol:2450:        assertTrue(world.getWorldState().winterActive, "winter should be active");
packages/contracts/test/ClanWorld.t.sol:2451:        assertEq(world.getWorldState().currentTick, winterStart, "currentTick should be 100");
packages/contracts/test/ClanWorld.t.sol:2461:        WorldState memory ws = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2462:        assertFalse(ws.winterActive, "winter should be over");
packages/contracts/test/ClanWorld.t.sol:2465:            ws.winterStartsAtTick,
packages/contracts/test/ClanWorld.t.sol:2476:        WorldState memory ws = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2483:        assertEq(ws.winterStartsAtTick, expectedWinterStart);
packages/contracts/test/ClanWorld.t.sol:2487:        WorldState memory ws0 = world.getWorldState();
packages/contracts/test/ClanWorld.t.sol:2491:        WorldState memory ws1 = world.getWorldState();
packages/contracts/test/DefendBase.t.sol:78:        while (world.getWorldState().currentTick < targetTick) {
packages/contracts/test/DefendBase.t.sol:100:        uint64 submittedAtTick = world.getWorldState().currentTick;

exec
/bin/bash -lc "rg -n \"WheatPlot|WinterLocked|remainingWheat|regrowUntilTick|HarvestWheat|WHEAT_PLOT\" apps packages docs -g '*.ts' -g '*.tsx' -g '*.sol' -g '*.md'" in /home/claude/code/clan-world
 succeeded in 1ms:
docs/reviews/pr250-r2-codereview-codex-5-5.md:31:4. Wheat plots at winter start: state=WinterLocked AND remainingWheat=0 AND regrow cleared
docs/reviews/pr250-r2-codereview-codex-5-5.md:684:         WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/pr250-r2-codereview-codex-5-5.md:685:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-r2-codereview-codex-5-5.md:690:         if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr250-r2-codereview-codex-5-5.md:729:+            _lockWheatPlotsForWinter();
docs/reviews/pr250-r2-codereview-codex-5-5.md:734:+            _restartWheatPlotsAfterWinter(newTick);
docs/reviews/pr250-r2-codereview-codex-5-5.md:739:+    function _lockWheatPlotsForWinter() internal {
docs/reviews/pr250-r2-codereview-codex-5-5.md:745:+                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr250-r2-codereview-codex-5-5.md:746:+                plot.state = WheatPlotState.WinterLocked;
docs/reviews/pr250-r2-codereview-codex-5-5.md:747:+                plot.remainingWheat = 0;
docs/reviews/pr250-r2-codereview-codex-5-5.md:748:+                plot.regrowUntilTick = 0;
docs/reviews/pr250-r2-codereview-codex-5-5.md:754:+    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
docs/reviews/pr250-r2-codereview-codex-5-5.md:760:+                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr250-r2-codereview-codex-5-5.md:761:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr250-r2-codereview-codex-5-5.md:762:+                    plot.state = WheatPlotState.Regrowing;
docs/reviews/pr250-r2-codereview-codex-5-5.md:763:+                    plot.remainingWheat = 0;
docs/reviews/pr250-r2-codereview-codex-5-5.md:764:+                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr250-r2-codereview-codex-5-5.md:826:+        WheatPlotState startingPlotState =
docs/reviews/pr250-r2-codereview-codex-5-5.md:827:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr250-r2-codereview-codex-5-5.md:830:         _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/pr250-r2-codereview-codex-5-5.md:831:-            state: WheatPlotState.Harvestable,
docs/reviews/pr250-r2-codereview-codex-5-5.md:834:             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr250-r2-codereview-codex-5-5.md:835:             regrowUntilTick: 0
docs/reviews/pr250-r2-codereview-codex-5-5.md:837:         _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/pr250-r2-codereview-codex-5-5.md:838:-            state: WheatPlotState.Harvestable,
docs/reviews/pr250-r2-codereview-codex-5-5.md:841:             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr250-r2-codereview-codex-5-5.md:842:             regrowUntilTick: 0
docs/reviews/pr250-r2-codereview-codex-5-5.md:1083:     uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/pr250-r2-codereview-codex-5-5.md:1387:+        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1388:+        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1389:+        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1395:+        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1396:+        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1397:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1398:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1399:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1400:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1401:+        assertEq(west1.remainingWheat, 0, "winter lock clears remaining wheat");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1402:+        assertEq(east1.remainingWheat, 0, "winter lock clears all plots");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, 0, "winter lock clears regrow tick");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1404:+        assertEq(east2.regrowUntilTick, 0, "winter lock clears all regrow ticks");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1407:+            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1414:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr250-r2-codereview-codex-5-5.md:1415:+        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1416:+        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1417:+        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1418:+        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1419:+        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1420:+        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1421:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1422:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1423:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1424:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1430:+        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1431:+        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1432:+        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1433:+        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1434:+        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1435:+        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1436:+        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1437:+        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1446:+            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1454:+        (WheatPlot memory west,) = world.getWheatPlots(clanId);
docs/reviews/pr250-r2-codereview-codex-5-5.md:1457:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1458:+        assertEq(west.remainingWheat, 0, "winter start clears locked plot");
docs/reviews/pr250-r2-codereview-codex-5-5.md:1740:/bin/bash -lc 'rg -n "function _applyUpkeep|function _settleClan|function _processOrder|function _validateOrder|function submitClanOrders|function heartbeat|event WinterStarted|event WinterEnded|enum WheatPlotState|struct WheatPlot|ERR_CLAN_DEAD|ERR_WINTER_LOCKED|WINTER_START_TICK|TICKS_PER_WINTER_CYCLE|ClanDied|ClanColdShortage|WallDegradedByCold|ClansmanColdDeath" packages/contracts/src packages/contracts/test docs/planning/clanworld_v4_spec.md' in /home/claude/code/clan-world
docs/reviews/pr250-r2-codereview-codex-5-5.md:1755:packages/contracts/src/IClanWorld.sol:121:enum WheatPlotState {
docs/reviews/pr250-r2-codereview-codex-5-5.md:1757:packages/contracts/src/IClanWorld.sol:259:struct WheatPlot {
docs/reviews/pr250-r2-codereview-codex-5-5.md:1879:    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/pr250-r2-codereview-codex-5-5.md:1880:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/pr250-r2-codereview-codex-5-5.md:1931:enum WheatPlotState {
docs/reviews/pr250-r2-codereview-codex-5-5.md:1934:    WinterLocked
docs/reviews/pr250-r2-codereview-codex-5-5.md:1950:    HarvestWheat,
docs/reviews/pr250-r2-codereview-codex-5-5.md:2239:                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr250-r2-codereview-codex-5-5.md:2240:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr250-r2-codereview-codex-5-5.md:2241:                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr250-r2-codereview-codex-5-5.md:2242:                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr250-r2-codereview-codex-5-5.md:2243:                    plot.regrowUntilTick = 0;
docs/reviews/pr250-r2-codereview-codex-5-5.md:2314:        } else if (action == ActionType.HarvestWheat) {
packages/shared/src/adapters/IChainClient.ts:184:              { name: 'remainingWheat', type: 'uint256' },
packages/shared/src/adapters/IChainClient.ts:185:              { name: 'regrowUntilTick', type: 'uint64' },
packages/shared/src/adapters/IChainClient.ts:194:              { name: 'remainingWheat', type: 'uint256' },
packages/shared/src/adapters/IChainClient.ts:195:              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-4.md:186:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-4.md:1567:         assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-4.md:2270:packages/contracts/src/ClanWorld.sol:453:        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-4.md:2281:packages/contracts/src/ClanWorld.sol:655:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/pr199-codereview-codex-5-4.md:2340:packages/contracts/src/ClanWorld.sol:2003:        if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-4.md:2346:packages/contracts/src/ClanWorld.sol:2202:                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr199-codereview-codex-5-4.md:2366:packages/contracts/test/MissionTiming.t.sol:127:        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-4.md:2607:   499	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-4.md:2796:   688	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/pr199-codereview-codex-5-4.md:2797:   689	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr199-codereview-codex-5-4.md:2806:   698	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/pr199-codereview-codex-5-4.md:2809:   701	        plot.remainingWheat -= yield;
docs/reviews/pr199-codereview-codex-5-4.md:2811:   703	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/pr199-codereview-codex-5-4.md:2813:   705	        if (plot.remainingWheat == 0) {
docs/reviews/pr199-codereview-codex-5-4.md:2814:   706	            plot.state = WheatPlotState.Regrowing;
docs/reviews/pr199-codereview-codex-5-4.md:2815:   707	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr199-codereview-codex-5-4.md:2818:   710	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr199-codereview-codex-5-4.md:3551:  1708	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr199-codereview-codex-5-4.md:3552:  1709	        if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-4.md:3779:  2121	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr199-codereview-codex-5-4.md:3824:  2166	    function getWheatPlots(uint32 clanId)
docs/reviews/pr199-codereview-codex-5-4.md:3828:  2170	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/pr199-codereview-codex-5-4.md:4146:    69	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/pr199-codereview-codex-5-4.md:4147:    70	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/pr199-codereview-codex-5-4.md:4194:   117	enum WheatPlotState {
docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/pr199-codereview-codex-5-4.md:4213:   136	    HarvestWheat,
docs/reviews/pr199-codereview-codex-5-4.md:4512:   734	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/pr199-codereview-codex-5-4.md:4756:  1077	        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/pr199-codereview-codex-5-4.md:4757:  1078	            state: WheatPlotState.Harvestable,
docs/reviews/pr199-codereview-codex-5-4.md:4759:  1080	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr199-codereview-codex-5-4.md:4760:  1081	            regrowUntilTick: 0
docs/reviews/pr199-codereview-codex-5-4.md:4762:  1083	        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/pr199-codereview-codex-5-4.md:4763:  1084	            state: WheatPlotState.Harvestable,
docs/reviews/pr199-codereview-codex-5-4.md:4765:  1086	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr199-codereview-codex-5-4.md:4766:  1087	            regrowUntilTick: 0
docs/reviews/pr199-codereview-codex-5-4.md:6355:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:326:                enum: ['ChopWood','MineIron','FishDocks','FishDeepSea','HarvestWheat','DepositResources','BuildWall','UpgradeBase','UpgradeMonument','DefendBase','MarketBuy','MarketSell','Wait']
docs/reviews/pr199-codereview-codex-5-4.md:9097:   127	        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-4.md:11163:    HarvestWheat,
docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:5:PR #250 is a substantial Phase-10 cohesion ship: winter schedule, 2× food upkeep + per-clansman wood burn, cold damage → wall degradation → RNG clansman death, wheat plot WinterLocked transitions, and a unified `_markClanDead` helper. The structural shape is good — death routes through `_markClansmanDead`/`_markClanDead`, dead clans short-circuit at `submitClanOrders` and `_settleCompletingMissions`, defenders are cleared, vault resources burn, gold survives. Tests are dense and exercise most happy paths.
docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:64:`_lockWheatPlotsForWinter` *unconditionally* sets every plot's `state = WinterLocked` regardless of pre-existing state. If a plot was `Regrowing` with `regrowUntilTick <= 109` (pre-winter), and the clan didn't trigger `_settleClan` in the [regrow-end, winter-start] window, the lazy regrow check (`if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick)`) at line 379 will never see `Regrowing` — it's already been overwritten to `WinterLocked` by the heartbeat. The "should-have-been-Harvestable" tick is silently dropped.
docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:66:Practical impact is bounded: post-winter `_restartWheatPlotsAfterWinter` resets all plots to a fresh `Regrowing(winterEnd + 4)`, so the only damage is lost harvest *opportunity* in the pre-winter regrow window for clans that didn't settle. With orchestrator polling every tick the window is small. Still, fairness asymmetry between settled/unsettled clans is real.
docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:68:**Fix options.** (a) In `_lockWheatPlotsForWinter`, perform the lazy regrow check first: `if (plot.state == Regrowing && plot.regrowUntilTick <= currentTick) { plot.state = Harvestable; ... }` then lock. (b) Skip lock for plots already past their regrow tick and let the lazy-settle eat them. Option (a) is straightforward and adds 1 read + 1 conditional per plot.
docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:143:`ClanWorld.sol:1161-1165`. Iterates all clans, including DEAD ones. Resetting `coldDamage = 0` on a dead clan is harmless (the field is unread for dead clans). Wasted gas at winter-end heartbeat. Add `if (clan.clanState == ClanState.DEAD) continue;`. Same for `_lockWheatPlotsForWinter` and `_restartWheatPlotsAfterWinter` (lines 1132-1158) — locking/unlocking a dead clan's plots writes to slots that will never be read again. Trivial gas cleanup.
docs/reviews/r2-phase10/pr250-codereview-opus-4-7.md:179:6. **Spec compliance is solid except for M1 (wood burn).** §A2 winter timing is correctly implemented as a global recurring schedule (PR v4.5 addendum supersedes the per-season-reset older spec). §10 elimination — vault burn yes, gold preservation yes, mission/defender cleanup yes. §10.2/§10.3 cold damage thresholds (every 2) — correct via integer-division-step pattern. §10.4 wheat plot WinterLocked + restart — correct, modulo M2 boundary.
packages/contracts/src/ClanWorld.sol:11:    WheatPlotState,
packages/contracts/src/ClanWorld.sol:19:    WheatPlot,
packages/contracts/src/ClanWorld.sol:60:    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
packages/contracts/src/ClanWorld.sol:378:                WheatPlot storage plot = _wheatPlots[clanId][pi];
packages/contracts/src/ClanWorld.sol:379:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
packages/contracts/src/ClanWorld.sol:380:                    plot.state = WheatPlotState.Harvestable;
packages/contracts/src/ClanWorld.sol:381:                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
packages/contracts/src/ClanWorld.sol:382:                    plot.regrowUntilTick = 0;
packages/contracts/src/ClanWorld.sol:453:        } else if (action == ActionType.HarvestWheat) {
packages/contracts/src/ClanWorld.sol:640:        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
packages/contracts/src/ClanWorld.sol:641:        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
packages/contracts/src/ClanWorld.sol:650:        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
packages/contracts/src/ClanWorld.sol:653:        plot.remainingWheat -= yield;
packages/contracts/src/ClanWorld.sol:655:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
packages/contracts/src/ClanWorld.sol:657:        if (plot.remainingWheat == 0) {
packages/contracts/src/ClanWorld.sol:658:            plot.state = WheatPlotState.Regrowing;
packages/contracts/src/ClanWorld.sol:659:            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
packages/contracts/src/ClanWorld.sol:662:        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
packages/contracts/src/ClanWorld.sol:1044:        _wheatPlots[clanId][0] = WheatPlot({
packages/contracts/src/ClanWorld.sol:1045:            state: WheatPlotState.Harvestable,
packages/contracts/src/ClanWorld.sol:1047:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
packages/contracts/src/ClanWorld.sol:1048:            regrowUntilTick: 0
packages/contracts/src/ClanWorld.sol:1050:        _wheatPlots[clanId][1] = WheatPlot({
packages/contracts/src/ClanWorld.sol:1051:            state: WheatPlotState.Harvestable,
packages/contracts/src/ClanWorld.sol:1053:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
packages/contracts/src/ClanWorld.sol:1054:            regrowUntilTick: 0
packages/contracts/src/ClanWorld.sol:2064:        // HarvestWheat: must go to WestFarms or EastFarms
packages/contracts/src/ClanWorld.sol:2065:        if (action == ActionType.HarvestWheat) {
packages/contracts/src/ClanWorld.sol:2264:                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
packages/contracts/src/ClanWorld.sol:2304:    function getWheatPlots(uint32 clanId)
packages/contracts/src/ClanWorld.sol:2308:        returns (WheatPlot memory west, WheatPlot memory east)
packages/contracts/src/IClanWorld.sol:69:    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
packages/contracts/src/IClanWorld.sol:70:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
packages/contracts/src/IClanWorld.sol:121:enum WheatPlotState {
packages/contracts/src/IClanWorld.sol:124:    WinterLocked
packages/contracts/src/IClanWorld.sol:140:    HarvestWheat,
packages/contracts/src/IClanWorld.sol:259:struct WheatPlot {
packages/contracts/src/IClanWorld.sol:260:    WheatPlotState state;
packages/contracts/src/IClanWorld.sol:262:    uint256 remainingWheat;
packages/contracts/src/IClanWorld.sol:263:    uint64 regrowUntilTick;
packages/contracts/src/IClanWorld.sol:440:    WheatPlot westPlot;
packages/contracts/src/IClanWorld.sol:441:    WheatPlot eastPlot;
packages/contracts/src/IClanWorld.sol:738:    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
packages/contracts/src/ClanWorldStub.sol:11:    WheatPlotState,
packages/contracts/src/ClanWorldStub.sol:19:    WheatPlot,
packages/contracts/src/ClanWorldStub.sol:255:    function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
packages/contracts/src/ClanWorldStub.sol:256:        west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
packages/contracts/src/ClanWorldStub.sol:257:        east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
packages/contracts/src/ClanWorldStub.sol:333:            westPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
packages/contracts/src/ClanWorldStub.sol:334:            eastPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
packages/contracts/test/MissionTiming.t.sol:127:        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
packages/contracts/test/ClanWorld.t.sol:16:    WheatPlotState,
packages/contracts/test/ClanWorld.t.sol:24:    WheatPlot,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:683:         WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:689:         if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:728:+            _lockWheatPlotsForWinter();
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:733:+            _restartWheatPlotsAfterWinter(newTick);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:738:+    function _lockWheatPlotsForWinter() internal {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:750:+    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:756:+                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:758:+                    plot.state = WheatPlotState.Regrowing;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:759:+                    plot.remainingWheat = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:760:+                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:822:+        WheatPlotState startingPlotState =
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:826:         _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:827:-            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:830:             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:831:             regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:833:         _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:834:-            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:837:             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:838:             regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1081:     uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1388:+        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1389:+        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1390:+        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1396:+        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1397:+        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1406:+            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1414:+        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1415:+        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1416:+        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1417:+        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1418:+        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1419:+        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1429:+        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1430:+        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1431:+        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1432:+        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1433:+        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1434:+        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1435:+        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1436:+        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1445:+            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1453:+        (WheatPlot memory west,) = world.getWheatPlots(clanId);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1457:+        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1765:    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1766:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1813:enum WheatPlotState {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1816:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1832:    HarvestWheat,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1951:struct WheatPlot {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1952:    WheatPlotState state;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1954:    uint256 remainingWheat;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1955:    uint64 regrowUntilTick;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:1961:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2246:/bin/bash -lc "rg -n \"winter|starvation|elimination|dead-clan|ClanState.DEAD|§10|§4\\.12|§A2|WinterLocked|OTC\" docs packages -g '"'!packages/contracts/test/**'"'" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2263:docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2297:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2326:docs/reviews/pr250-codereview-codex-5-4.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2347:docs/reviews/pr250-codereview-codex-5-4.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2348:docs/reviews/pr250-codereview-codex-5-4.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2353:docs/reviews/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2417:docs/reviews/pr250-codereview-codex-5-4.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2418:docs/reviews/pr250-codereview-codex-5-4.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2419:docs/reviews/pr250-codereview-codex-5-4.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2420:docs/reviews/pr250-codereview-codex-5-4.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2421:docs/reviews/pr250-codereview-codex-5-4.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2422:docs/reviews/pr250-codereview-codex-5-4.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2426:docs/reviews/pr250-codereview-codex-5-4.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2427:docs/reviews/pr250-codereview-codex-5-4.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2428:docs/reviews/pr250-codereview-codex-5-4.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2429:docs/reviews/pr250-codereview-codex-5-4.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2430:docs/reviews/pr250-codereview-codex-5-4.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2435:docs/reviews/pr250-codereview-codex-5-4.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2493:docs/reviews/pr250-codereview-codex-5-4.md:1816:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2500:docs/reviews/pr250-codereview-codex-5-4.md:1961:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2934:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2963:docs/reviews/pr250-codereview-codex-5-5.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2984:docs/reviews/pr250-codereview-codex-5-5.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2985:docs/reviews/pr250-codereview-codex-5-5.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:2990:docs/reviews/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3054:docs/reviews/pr250-codereview-codex-5-5.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3055:docs/reviews/pr250-codereview-codex-5-5.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3056:docs/reviews/pr250-codereview-codex-5-5.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3057:docs/reviews/pr250-codereview-codex-5-5.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3058:docs/reviews/pr250-codereview-codex-5-5.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3059:docs/reviews/pr250-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3063:docs/reviews/pr250-codereview-codex-5-5.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3064:docs/reviews/pr250-codereview-codex-5-5.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3065:docs/reviews/pr250-codereview-codex-5-5.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3066:docs/reviews/pr250-codereview-codex-5-5.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3067:docs/reviews/pr250-codereview-codex-5-5.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3072:docs/reviews/pr250-codereview-codex-5-5.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3358:docs/reviews/r2/pr194-codereview-codex-5-5.md:5053:   118	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3427:docs/reviews/r2/pr194-codereview-codex-5-5.md:8841:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3482:docs/reviews/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3494:docs/reviews/pr198-codereview-codex-5-4.md:5941:   124	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3556:docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3583:docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3643:docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3737:docs/reviews/r2/pr194-codereview-codex-5-4.md:4586:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3833:docs/reviews/r2/pr194-codereview-codex-5-4.md:10631:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:3877:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4159:docs/reviews/pr200-codereview-codex-5-4.md:7129:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4212:docs/reviews/pr198-codereview-codex-5-5.md:4028:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4245:docs/reviews/pr198-codereview-codex-5-5.md:7736:   124	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4321:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4322:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4437:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4515:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:118:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4644:   424	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4645:   425	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4646:   426	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4647:   427	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4648:   428	                    plot.regrowUntilTick = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:4719:   499	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5022:  1077	        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5023:  1078	            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5025:  1080	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5026:  1081	            regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5028:  1083	        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5029:  1084	            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5031:  1086	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5032:  1087	            regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5623:    69	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5624:    70	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5671:   117	enum WheatPlotState {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5674:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5690:   136	    HarvestWheat,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5963:   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5964:   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5965:   380	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5966:   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:5967:   382	                    plot.regrowUntilTick = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6203:   618	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6432:  1122	            _lockWheatPlotsForWinter();
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6442:  1122	            _lockWheatPlotsForWinter();
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6447:  1127	            _restartWheatPlotsAfterWinter(newTick);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6452:  1132	    function _lockWheatPlotsForWinter() internal {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6458:  1138	                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6464:  1144	    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6470:  1150	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6471:  1151	                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6472:  1152	                    plot.state = WheatPlotState.Regrowing;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6473:  1153	                    plot.remainingWheat = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6474:  1154	                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6593:  1273	        WheatPlotState startingPlotState =
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6594:  1274	            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6597:  1277	        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6600:  1280	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6601:  1281	            regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6603:  1283	        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6606:  1286	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6607:  1287	            regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6716:  1886	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6717:  1887	        if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6880:  2050	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6950:    70	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6951:    71	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:6998:   118	enum WheatPlotState {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7001:   121	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7017:   137	    HarvestWheat,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7376:   731	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7694:  2059	        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7695:  2060	        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7696:  2061	        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7702:  2067	        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7703:  2068	        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7704:  2069	        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7705:  2070	        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7706:  2071	        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7707:  2072	        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7708:  2073	        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7709:  2074	        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7712:  2077	            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7719:  2084	        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7720:  2085	        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7721:  2086	        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7722:  2087	        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7723:  2088	        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7724:  2089	        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7725:  2090	        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7726:  2091	        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7727:  2092	        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7728:  2093	        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7729:  2094	        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7735:  2100	        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7736:  2101	        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7737:  2102	        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7738:  2103	        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7739:  2104	        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7740:  2105	        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7741:  2106	        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7742:  2107	        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7751:  2116	            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7759:  2124	        (WheatPlot memory west,) = world.getWheatPlots(clanId);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7762:  2127	        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:7763:  2128	        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:8000:    11	    WheatPlotState,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:8008:    19	    WheatPlot,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:8582:    11	    WheatPlotState,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:8590:    19	    WheatPlot,
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:8631:    60	    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9353:  2087	    function getWheatPlots(uint32 clanId)
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9357:  2091	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9759:   806	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9760:   807	        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9765:   812	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9774:   821	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9777:   824	        plot.remainingWheat -= yield;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9779:   826	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9781:   828	        if (plot.remainingWheat == 0) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9782:   829	            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9783:   830	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9786:   833	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9803:  2088	        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9804:  2089	        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9805:  2090	        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9806:  2091	        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9807:  2092	        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9808:  2093	        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9809:  2094	        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9815:  2100	        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9816:  2101	        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9817:  2102	        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9818:  2103	        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9819:  2104	        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9820:  2105	        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9821:  2106	        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9822:  2107	        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9831:  2116	            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9839:  2124	        (WheatPlot memory west,) = world.getWheatPlots(clanId);
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9842:  2127	        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:9843:  2128	        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:10070:- `packages/contracts/src/ClanWorld.sol:1132-1140` locks wheat plots for winter without clearing `remainingWheat` or resetting regrow state, and the tests at `packages/contracts/test/ClanWorld.t.sol:2069-2074` and `:2125-2128` explicitly assert that preserved-state behavior. That disagrees with `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843-846`, which says winter start clears all plots. Harvest is blocked, so this is mostly a state/spec mismatch today, but getters will expose impossible in-winter crop state and future logic can accidentally depend on it.
docs/reviews/r2-phase10/pr250-codereview-codex-5-4.md:10093:- `packages/contracts/src/ClanWorld.sol:1132-1140` locks wheat plots for winter without clearing `remainingWheat` or resetting regrow state, and the tests at `packages/contracts/test/ClanWorld.t.sol:2069-2074` and `:2125-2128` explicitly assert that preserved-state behavior. That disagrees with `docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:843-846`, which says winter start clears all plots. Harvest is blocked, so this is mostly a state/spec mismatch today, but getters will expose impossible in-winter crop state and future logic can accidentally depend on it.
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:683:         WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:689:         if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:728:+            _lockWheatPlotsForWinter();
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:733:+            _restartWheatPlotsAfterWinter(newTick);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:738:+    function _lockWheatPlotsForWinter() internal {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:750:+    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:756:+                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:758:+                    plot.state = WheatPlotState.Regrowing;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:759:+                    plot.remainingWheat = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:760:+                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:822:+        WheatPlotState startingPlotState =
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:826:         _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:827:-            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:830:             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:831:             regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:833:         _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:834:-            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:837:             remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:838:             regrowUntilTick: 0
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1081:     uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1388:+        (WheatPlot memory westBefore, WheatPlot memory eastBefore) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1389:+        assertEq(uint8(westBefore.state), uint8(WheatPlotState.Harvestable), "west starts harvestable");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1390:+        assertEq(uint8(eastBefore.state), uint8(WheatPlotState.Harvestable), "east starts harvestable");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1396:+        (WheatPlot memory west1, WheatPlot memory east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1397:+        (WheatPlot memory west2, WheatPlot memory east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1406:+            _submitOrder(clanId1, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1414:+        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1415:+        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1416:+        assertEq(uint8(west1.state), uint8(WheatPlotState.Regrowing), "clan1 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1417:+        assertEq(uint8(east1.state), uint8(WheatPlotState.Regrowing), "clan1 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1418:+        assertEq(uint8(west2.state), uint8(WheatPlotState.Regrowing), "clan2 west restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1419:+        assertEq(uint8(east2.state), uint8(WheatPlotState.Regrowing), "clan2 east restarts regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1429:+        (west1, east1) = world.getWheatPlots(clanId1);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1430:+        (west2, east2) = world.getWheatPlots(clanId2);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1431:+        assertEq(uint8(west1.state), uint8(WheatPlotState.Harvestable), "clan1 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1432:+        assertEq(uint8(east1.state), uint8(WheatPlotState.Harvestable), "clan1 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1433:+        assertEq(uint8(west2.state), uint8(WheatPlotState.Harvestable), "clan2 west harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1434:+        assertEq(uint8(east2.state), uint8(WheatPlotState.Harvestable), "clan2 east harvestable after regrow");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1435:+        assertEq(west1.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "west wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1436:+        assertEq(east2.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "east wheat refills");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1445:+            _submitOrder(clanId, 1, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1453:+        (WheatPlot memory west,) = world.getWheatPlots(clanId);
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1457:+        assertEq(west.remainingWheat, ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT, "locked harvest does not drain plot");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1749:/bin/bash -lc "rg -n \"function _markClanDead|_markClansmanDead|ClanEliminated|ERR_CLAN_DEAD|ClanState.DEAD|settleClan\\(|getClanFullView|enum ClanState|struct Clan|coldDamage|WinterLocked|WinterStarted|WinterEnded|winterStartsAtTick|TICKS_PER_WINTER_CYCLE|WINTER_START_TICK\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1788:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1965:enum WheatPlotState {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1968:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:1984:    HarvestWheat,
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2031:/bin/bash -lc "rg -n \"A2|winter|starvation|elimination|clan death|dead clan|cold_damage|cold damage|WinterLocked|WINTER_START|TICKS_PER_WINTER|4\\.12|10\\.|Phase 10\" docs packages -g '*.md' -g '*.sol'" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2076:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2121:docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2313:docs/reviews/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2321:docs/reviews/pr198-codereview-codex-5-4.md:5941:   124	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2382:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2409:docs/reviews/pr250-codereview-codex-5-4.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2430:docs/reviews/pr250-codereview-codex-5-4.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2431:docs/reviews/pr250-codereview-codex-5-4.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2443:docs/reviews/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2521:docs/reviews/pr250-codereview-codex-5-4.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2522:docs/reviews/pr250-codereview-codex-5-4.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2523:docs/reviews/pr250-codereview-codex-5-4.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2524:docs/reviews/pr250-codereview-codex-5-4.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2525:docs/reviews/pr250-codereview-codex-5-4.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2526:docs/reviews/pr250-codereview-codex-5-4.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2530:docs/reviews/pr250-codereview-codex-5-4.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2531:docs/reviews/pr250-codereview-codex-5-4.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2532:docs/reviews/pr250-codereview-codex-5-4.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2533:docs/reviews/pr250-codereview-codex-5-4.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2534:docs/reviews/pr250-codereview-codex-5-4.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2539:docs/reviews/pr250-codereview-codex-5-4.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2605:docs/reviews/pr250-codereview-codex-5-4.md:1816:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2611:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2612:docs/reviews/pr250-codereview-codex-5-4.md:1961:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2686:docs/reviews/pr250-codereview-codex-5-4.md:2246:/bin/bash -lc "rg -n \"winter|starvation|elimination|dead-clan|ClanState.DEAD|§10|§4\\.12|§A2|WinterLocked|OTC\" docs packages -g '"'!packages/contracts/test/**'"'" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2702:docs/reviews/pr250-codereview-codex-5-4.md:2263:docs/reviews/pr199-codereview-codex-5-4.md:4197:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2728:docs/reviews/pr250-codereview-codex-5-4.md:2297:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2753:docs/reviews/pr250-codereview-codex-5-4.md:2326:docs/reviews/pr250-codereview-codex-5-4.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2774:docs/reviews/pr250-codereview-codex-5-4.md:2347:docs/reviews/pr250-codereview-codex-5-4.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2775:docs/reviews/pr250-codereview-codex-5-4.md:2348:docs/reviews/pr250-codereview-codex-5-4.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2780:docs/reviews/pr250-codereview-codex-5-4.md:2353:docs/reviews/pr250-codereview-codex-5-4.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2843:docs/reviews/pr250-codereview-codex-5-4.md:2417:docs/reviews/pr250-codereview-codex-5-4.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2844:docs/reviews/pr250-codereview-codex-5-4.md:2418:docs/reviews/pr250-codereview-codex-5-4.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2845:docs/reviews/pr250-codereview-codex-5-4.md:2419:docs/reviews/pr250-codereview-codex-5-4.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2846:docs/reviews/pr250-codereview-codex-5-4.md:2420:docs/reviews/pr250-codereview-codex-5-4.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2847:docs/reviews/pr250-codereview-codex-5-4.md:2421:docs/reviews/pr250-codereview-codex-5-4.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2848:docs/reviews/pr250-codereview-codex-5-4.md:2422:docs/reviews/pr250-codereview-codex-5-4.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2852:docs/reviews/pr250-codereview-codex-5-4.md:2426:docs/reviews/pr250-codereview-codex-5-4.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2853:docs/reviews/pr250-codereview-codex-5-4.md:2427:docs/reviews/pr250-codereview-codex-5-4.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2854:docs/reviews/pr250-codereview-codex-5-4.md:2428:docs/reviews/pr250-codereview-codex-5-4.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2855:docs/reviews/pr250-codereview-codex-5-4.md:2429:docs/reviews/pr250-codereview-codex-5-4.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2856:docs/reviews/pr250-codereview-codex-5-4.md:2430:docs/reviews/pr250-codereview-codex-5-4.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2861:docs/reviews/pr250-codereview-codex-5-4.md:2435:docs/reviews/pr250-codereview-codex-5-4.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2916:docs/reviews/pr250-codereview-codex-5-4.md:2493:docs/reviews/pr250-codereview-codex-5-4.md:1816:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2922:docs/reviews/pr250-codereview-codex-5-4.md:2499:docs/reviews/pr250-codereview-codex-5-4.md:1959:/bin/bash -lc "rg -n \"winter|starvation|ClanState\\.DEAD|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|ClanDied|ClanEliminated|coldDamage|WinterLocked|settleClansman|_markClanDead|_killNextClansmanFromStarvation|_killRandomClansmanFromCold|getWorldState|isWinter\" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol packages/contracts/test/ClanWorldStub.t.sol" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:2923:docs/reviews/pr250-codereview-codex-5-4.md:2500:docs/reviews/pr250-codereview-codex-5-4.md:1961:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3231:docs/reviews/pr250-codereview-codex-5-4.md:2934:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3256:docs/reviews/pr250-codereview-codex-5-4.md:2963:docs/reviews/pr250-codereview-codex-5-5.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3277:docs/reviews/pr250-codereview-codex-5-4.md:2984:docs/reviews/pr250-codereview-codex-5-5.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3278:docs/reviews/pr250-codereview-codex-5-4.md:2985:docs/reviews/pr250-codereview-codex-5-5.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3283:docs/reviews/pr250-codereview-codex-5-4.md:2990:docs/reviews/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3346:docs/reviews/pr250-codereview-codex-5-4.md:3054:docs/reviews/pr250-codereview-codex-5-5.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3347:docs/reviews/pr250-codereview-codex-5-4.md:3055:docs/reviews/pr250-codereview-codex-5-5.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3348:docs/reviews/pr250-codereview-codex-5-4.md:3056:docs/reviews/pr250-codereview-codex-5-5.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3349:docs/reviews/pr250-codereview-codex-5-4.md:3057:docs/reviews/pr250-codereview-codex-5-5.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3350:docs/reviews/pr250-codereview-codex-5-4.md:3058:docs/reviews/pr250-codereview-codex-5-5.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3351:docs/reviews/pr250-codereview-codex-5-4.md:3059:docs/reviews/pr250-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3355:docs/reviews/pr250-codereview-codex-5-4.md:3063:docs/reviews/pr250-codereview-codex-5-5.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3356:docs/reviews/pr250-codereview-codex-5-4.md:3064:docs/reviews/pr250-codereview-codex-5-5.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3357:docs/reviews/pr250-codereview-codex-5-4.md:3065:docs/reviews/pr250-codereview-codex-5-5.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3358:docs/reviews/pr250-codereview-codex-5-4.md:3066:docs/reviews/pr250-codereview-codex-5-5.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3359:docs/reviews/pr250-codereview-codex-5-4.md:3067:docs/reviews/pr250-codereview-codex-5-5.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3364:docs/reviews/pr250-codereview-codex-5-4.md:3072:docs/reviews/pr250-codereview-codex-5-5.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3599:docs/reviews/pr250-codereview-codex-5-4.md:3358:docs/reviews/r2/pr194-codereview-codex-5-5.md:5053:   118	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3640:docs/reviews/pr250-codereview-codex-5-4.md:3427:docs/reviews/r2/pr194-codereview-codex-5-5.md:8841:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3693:docs/reviews/pr250-codereview-codex-5-4.md:3482:docs/reviews/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3700:docs/reviews/pr250-codereview-codex-5-4.md:3494:docs/reviews/pr198-codereview-codex-5-4.md:5941:   124	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3756:docs/reviews/pr250-codereview-codex-5-4.md:3556:docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3777:docs/reviews/pr250-codereview-codex-5-4.md:3583:docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3824:docs/reviews/pr250-codereview-codex-5-4.md:3643:docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3898:docs/reviews/pr250-codereview-codex-5-4.md:3737:docs/reviews/r2/pr194-codereview-codex-5-4.md:4586:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:3968:docs/reviews/pr250-codereview-codex-5-4.md:3833:docs/reviews/r2/pr194-codereview-codex-5-4.md:10631:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4007:docs/reviews/pr250-codereview-codex-5-4.md:3877:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4114:docs/reviews/pr250-codereview-codex-5-4.md:4159:docs/reviews/pr200-codereview-codex-5-4.md:7129:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4138:docs/reviews/pr250-codereview-codex-5-4.md:4212:docs/reviews/pr198-codereview-codex-5-5.md:4028:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4158:docs/reviews/pr250-codereview-codex-5-4.md:4245:docs/reviews/pr198-codereview-codex-5-5.md:7736:   124	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4217:docs/reviews/pr250-codereview-codex-5-4.md:4321:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4218:docs/reviews/pr250-codereview-codex-5-4.md:4322:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4315:docs/reviews/pr250-codereview-codex-5-4.md:4436:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4316:docs/reviews/pr250-codereview-codex-5-4.md:4437:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4379:docs/reviews/pr250-codereview-codex-5-4.md:4515:docs/planning/V1/01 Blockchain Game Spec/IClanWorld.sol.txt:118:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4428:docs/reviews/pr250-codereview-codex-5-4.md:5674:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4571:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4572:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4699:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4700:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4754:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4781:docs/reviews/pr250-codereview-codex-5-5.md:684:+        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4802:docs/reviews/pr250-codereview-codex-5-5.md:744:+                _wheatPlots[clanId][pi].state = WheatPlotState.WinterLocked;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4803:docs/reviews/pr250-codereview-codex-5-5.md:757:+                if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4815:docs/reviews/pr250-codereview-codex-5-5.md:823:+            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4893:docs/reviews/pr250-codereview-codex-5-5.md:1398:+        assertEq(uint8(west1.state), uint8(WheatPlotState.WinterLocked), "clan1 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4894:docs/reviews/pr250-codereview-codex-5-5.md:1399:+        assertEq(uint8(east1.state), uint8(WheatPlotState.WinterLocked), "clan1 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4895:docs/reviews/pr250-codereview-codex-5-5.md:1400:+        assertEq(uint8(west2.state), uint8(WheatPlotState.WinterLocked), "clan2 west locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4896:docs/reviews/pr250-codereview-codex-5-5.md:1401:+        assertEq(uint8(east2.state), uint8(WheatPlotState.WinterLocked), "clan2 east locks at winter start");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4897:docs/reviews/pr250-codereview-codex-5-5.md:1402:+        assertEq(west1.remainingWheat, westBefore.remainingWheat, "winter lock preserves remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4898:docs/reviews/pr250-codereview-codex-5-5.md:1403:+        assertEq(west1.regrowUntilTick, westBefore.regrowUntilTick, "winter lock preserves regrow tick");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4902:docs/reviews/pr250-codereview-codex-5-5.md:1413:+        uint64 expectedRegrowUntil = winterEnd + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4903:docs/reviews/pr250-codereview-codex-5-5.md:1420:+        assertEq(west1.remainingWheat, 0, "winter end clears remaining wheat");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4904:docs/reviews/pr250-codereview-codex-5-5.md:1421:+        assertEq(east2.remainingWheat, 0, "winter end clears all plots");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4905:docs/reviews/pr250-codereview-codex-5-5.md:1422:+        assertEq(west1.regrowUntilTick, expectedRegrowUntil, "west regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4906:docs/reviews/pr250-codereview-codex-5-5.md:1423:+        assertEq(east2.regrowUntilTick, expectedRegrowUntil, "east regrow restarts from winter end");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4911:docs/reviews/pr250-codereview-codex-5-5.md:1456:+        assertEq(uint8(west.state), uint8(WheatPlotState.WinterLocked), "plot remains winter locked");
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4975:docs/reviews/pr250-codereview-codex-5-5.md:1749:/bin/bash -lc "rg -n \"function _markClanDead|_markClansmanDead|ClanEliminated|ERR_CLAN_DEAD|ClanState.DEAD|settleClan\\(|getClanFullView|enum ClanState|struct Clan|coldDamage|WinterLocked|WinterStarted|WinterEnded|winterStartsAtTick|TICKS_PER_WINTER_CYCLE|WINTER_START_TICK\" packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:4993:docs/reviews/pr250-codereview-codex-5-5.md:1788:packages/contracts/src/IClanWorld.sol:120:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5585:docs/reviews/pr200-codereview-codex-5-4.md:7129:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5610:docs/reviews/pr198-codereview-codex-5-5.md:4028:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5634:docs/reviews/pr198-codereview-codex-5-5.md:7736:   124	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5696:docs/reviews/r2/pr194-codereview-codex-5-5.md:5053:   118	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5739:docs/reviews/r2/pr194-codereview-codex-5-5.md:8841:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5803:docs/reviews/r2/pr194-codereview-codex-5-4.md:4586:    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5875:docs/reviews/r2/pr194-codereview-codex-5-4.md:10631:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5923:docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:5947:docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6007:docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6076:    WheatPlot westPlot;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6077:    WheatPlot eastPlot;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6350:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6351:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6695:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:6919:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7388:                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7389:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7390:                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7391:                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7392:                    plot.regrowUntilTick = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7419:                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7420:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7421:                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7422:                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7423:                    plot.regrowUntilTick = 0;
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7683:`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.
docs/reviews/r2-phase10/pr250-codereview-codex-5-5.md:7722:`packages/contracts/src/ClanWorld.sol::_lockWheatPlotsForWinter` — Winter start locks plots but preserves `remainingWheat` and `regrowUntilTick`. Spec §7.4 says “At winter start: all plots are cleared.” The test currently asserts the opposite.
docs/planning/clanworld_v4_3_schema_patch.md:262:- `WHEAT_PLOT_REGROW_TICKS = 4`
docs/planning/clanworld_v4_3_schema_patch.md:263:- `WHEAT_PLOT_STARTING_WHEAT = 100e18`
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:387:| `ACTING` | `ChopWood`, `MineIron`, `FishDocks`, `FishDeepSea`, `HarvestWheat` | `'GATHERING'` |
docs/planning/V1/02 Frontend Spec/clanworld_frontend_spec.md:448:        case 'HarvestWheat': return 'clansman_harvest';
docs/reviews/pr199-codereview-codex-5-5.md:186:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:1567:         assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-5.md:2310:packages/contracts/test/MissionTiming.t.sol:127:        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-5.md:2491:docs/reviews/pr199-codereview-codex-5-5.md:1567:         assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-5.md:2633:docs/reviews/pr199-codereview-codex-5-4.md:1567:         assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-5.md:2705:docs/reviews/pr199-codereview-codex-5-4.md:2366:packages/contracts/test/MissionTiming.t.sol:127:        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-5.md:5052:docs/reviews/pr250-codereview-codex-5-4.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr199-codereview-codex-5-5.md:5917:docs/reviews/pr250-codereview-codex-5-5.md:19:Phase 10 ships: winter schedule (10.1) + winter upkeep 2x food + wood burn (10.2) + cold damage walls degrade then RNG clansman death (10.3) + crop transitions WinterLocked (10.4) + clan death (10.5).
docs/reviews/pr199-codereview-codex-5-5.md:9491:  1708	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr199-codereview-codex-5-5.md:9492:  1709	        if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:9820:  2121	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr199-codereview-codex-5-5.md:10152:  2166	    function getWheatPlots(uint32 clanId)
docs/reviews/pr199-codereview-codex-5-5.md:10156:  2170	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/pr199-codereview-codex-5-5.md:10402:   120	    WinterLocked
docs/reviews/pr199-codereview-codex-5-5.md:10418:   136	    HarvestWheat,
docs/reviews/pr199-codereview-codex-5-5.md:10508:   734	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/pr199-codereview-codex-5-5.md:10628:   273	    function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
docs/reviews/pr199-codereview-codex-5-5.md:10629:   274	        west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/pr199-codereview-codex-5-5.md:10630:   275	        east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/pr199-codereview-codex-5-5.md:10922:  1708	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr199-codereview-codex-5-5.md:10923:  1709	        if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:11297:   184	              { name: 'remainingWheat', type: 'uint256' },
docs/reviews/pr199-codereview-codex-5-5.md:11298:   185	              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-5.md:11307:   194	              { name: 'remainingWheat', type: 'uint256' },
docs/reviews/pr199-codereview-codex-5-5.md:11308:   195	              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr199-codereview-codex-5-5.md:11757:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:11981:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:12206:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:12647:docs/reviews/pr199-codereview-codex-5-4.md:2270:packages/contracts/src/ClanWorld.sol:453:        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:12658:docs/reviews/pr199-codereview-codex-5-4.md:2281:packages/contracts/src/ClanWorld.sol:655:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/pr199-codereview-codex-5-5.md:12717:docs/reviews/pr199-codereview-codex-5-4.md:2340:packages/contracts/src/ClanWorld.sol:2003:        if (action == ActionType.HarvestWheat) {
docs/reviews/pr199-codereview-codex-5-5.md:12723:docs/reviews/pr199-codereview-codex-5-4.md:2346:packages/contracts/src/ClanWorld.sol:2202:                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr199-codereview-codex-5-5.md:13103:   127	        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
docs/reviews/pr199-codereview-codex-5-5.md:13264:    60	    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/pr199-codereview-codex-5-5.md:13401:   806	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/pr199-codereview-codex-5-5.md:13402:   807	        if (plot.state == WheatPlotState.WinterLocked) {
docs/reviews/pr199-codereview-codex-5-5.md:13407:   812	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:13416:   821	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/pr199-codereview-codex-5-5.md:13419:   824	        plot.remainingWheat -= yield;
docs/reviews/pr199-codereview-codex-5-5.md:13421:   826	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/pr199-codereview-codex-5-5.md:13423:   828	        if (plot.remainingWheat == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:13424:   829	            plot.state = WheatPlotState.Regrowing;
docs/reviews/pr199-codereview-codex-5-5.md:13425:   830	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr199-codereview-codex-5-5.md:13428:   833	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr199-codereview-codex-5-5.md:13825:  1886	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr199-codereview-codex-5-5.md:13826:  1887	        if (action == ActionType.HarvestWheat) {
docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:326:                enum: ['ChopWood','MineIron','FishDocks','FishDeepSea','HarvestWheat','DepositResources','BuildWall','UpgradeBase','UpgradeMonument','DefendBase','MarketBuy','MarketSell','Wait']
docs/reviews/pr194-r2-codereview-codex-5-5.md:733:+        WheatPlot[2] wheatPlots;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1114:+            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/pr194-r2-codereview-codex-5-5.md:1195:+    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1197:+            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/pr194-r2-codereview-codex-5-5.md:1198:+            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1199:+                plot.state = WheatPlotState.Harvestable;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1200:+                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1201:+                plot.regrowUntilTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1269:+        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1378:+        WheatPlot memory plot = sim.wheatPlots[plotIdx];
docs/reviews/pr194-r2-codereview-codex-5-5.md:1379:+        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1386:+        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1389:+        plot.remainingWheat -= yield;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1391:+        if (plot.remainingWheat == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:1392:+            plot.state = WheatPlotState.Regrowing;
docs/reviews/pr194-r2-codereview-codex-5-5.md:1393:+            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
docs/reviews/pr194-r2-codereview-codex-5-5.md:1397:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:2610:     function getWheatPlots(uint32 clanId)
docs/reviews/pr194-r2-codereview-codex-5-5.md:2890:     function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:2891:         west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/pr194-r2-codereview-codex-5-5.md:2892:         east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/pr194-r2-codereview-codex-5-5.md:2927: enum WheatPlotState {
docs/reviews/pr194-r2-codereview-codex-5-5.md:3005:     function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/pr194-r2-codereview-codex-5-5.md:4958:-              { name: 'remainingWheat', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4959:-              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4968:-              { name: 'remainingWheat', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:4969:-              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-5.md:5459:   424	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr194-r2-codereview-codex-5-5.md:5460:   425	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5461:   426	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5462:   427	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5463:   428	                    plot.regrowUntilTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5516:  1076	            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/pr194-r2-codereview-codex-5-5.md:5597:  1157	    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5599:  1159	            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/pr194-r2-codereview-codex-5-5.md:5600:  1160	            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:5601:  1161	                plot.state = WheatPlotState.Harvestable;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5602:  1162	                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5603:  1163	                plot.regrowUntilTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-5.md:5671:  1231	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6280:  3239	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr194-r2-codereview-codex-5-5.md:6328:  3287	    function getWheatPlots(uint32 clanId)
docs/reviews/pr194-r2-codereview-codex-5-5.md:6332:  3291	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/pr194-r2-codereview-codex-5-5.md:6433:   635	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr194-r2-codereview-codex-5-5.md:6867:   124	        WheatPlot[2] wheatPlots;
docs/reviews/pr194-r2-codereview-codex-5-5.md:7532:      "remainingWheat:uint256",
docs/reviews/pr194-r2-codereview-codex-5-5.md:7533:      "regrowUntilTick:uint64"
docs/reviews/pr194-r2-codereview-codex-5-5.md:7542:      "remainingWheat:uint256",
docs/reviews/pr194-r2-codereview-codex-5-5.md:7543:      "regrowUntilTick:uint64"
docs/reviews/pr194-r2-codereview-codex-5-5.md:8405:  3078	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr194-r2-codereview-codex-5-5.md:8406:  3079	        if (action == ActionType.HarvestWheat) {
docs/reviews/pr200-codereview-codex-5-5.md:1742:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr200-codereview-codex-5-5.md:4922:        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr200-codereview-codex-5-5.md:4923:        if (action == ActionType.HarvestWheat) {
docs/reviews/pr200-codereview-codex-5-5.md:5969:docs/reviews/pr200-codereview-codex-5-4.md:4839:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/reviews/pr200-codereview-codex-5-5.md:6000:docs/reviews/pr200-codereview-codex-5-4.md:4913:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/reviews/pr200-codereview-codex-5-5.md:6962:        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/pr200-codereview-codex-5-5.md:6963:            state: WheatPlotState.Harvestable,
docs/reviews/pr200-codereview-codex-5-5.md:6965:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr200-codereview-codex-5-5.md:6966:            regrowUntilTick: 0
docs/reviews/pr200-codereview-codex-5-5.md:6968:        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/pr200-codereview-codex-5-5.md:6969:            state: WheatPlotState.Harvestable,
docs/reviews/pr200-codereview-codex-5-5.md:6971:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr200-codereview-codex-5-5.md:6972:            regrowUntilTick: 0
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:262:- `WHEAT_PLOT_REGROW_TICKS = 4`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_3_schema_patch.md:263:- `WHEAT_PLOT_STARTING_WHEAT = 100e18`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:98:    WheatPlot westPlot;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_4_ui_indexer_getters.md:99:    WheatPlot eastPlot;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:452:- `remainingWheat`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:453:- `regrowUntilTick`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:457:- `harvest_wheat` removes wheat from `remainingWheat`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_spec.md:458:- when `remainingWheat == 0`, the plot enters regrow state
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:408:struct WheatPlot {
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:409:    WheatPlotState state;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:411:    uint256 remainingWheat;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:412:    uint64 regrowUntilTick;
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:419:- after 4 ticks: `Regrowing -> Harvestable` with reset `remainingWheat`
docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:609:function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/planning/clanworld_v4_spec.md:452:- `remainingWheat`
docs/planning/clanworld_v4_spec.md:453:- `regrowUntilTick`
docs/planning/clanworld_v4_spec.md:457:- `harvest_wheat` removes wheat from `remainingWheat`
docs/planning/clanworld_v4_spec.md:458:- when `remainingWheat == 0`, the plot enters regrow state
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:408:struct WheatPlot {
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:409:    WheatPlotState state;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:411:    uint256 remainingWheat;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:412:    uint64 regrowUntilTick;
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:417:- at winter start: plots enter `WinterLocked`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:418:- at winter end: `WinterLocked -> Regrowing`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:419:- after 4 ticks: `Regrowing -> Harvestable` with reset `remainingWheat`
docs/planning/clanworld_v4_2_state_schema_interface_spec.md:609:function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/planning/clanworld_v4_4_ui_indexer_getters.md:98:    WheatPlot westPlot;
docs/planning/clanworld_v4_4_ui_indexer_getters.md:99:    WheatPlot eastPlot;
docs/reviews/pr194-r2-codereview-codex-5-4.md:733:+        WheatPlot[2] wheatPlots;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1114:+            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/pr194-r2-codereview-codex-5-4.md:1195:+    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1197:+            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/pr194-r2-codereview-codex-5-4.md:1198:+            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1199:+                plot.state = WheatPlotState.Harvestable;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1200:+                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1201:+                plot.regrowUntilTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1269:+        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1378:+        WheatPlot memory plot = sim.wheatPlots[plotIdx];
docs/reviews/pr194-r2-codereview-codex-5-4.md:1379:+        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1386:+        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1389:+        plot.remainingWheat -= yield;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1391:+        if (plot.remainingWheat == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:1392:+            plot.state = WheatPlotState.Regrowing;
docs/reviews/pr194-r2-codereview-codex-5-4.md:1393:+            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
docs/reviews/pr194-r2-codereview-codex-5-4.md:1397:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:2610:     function getWheatPlots(uint32 clanId)
docs/reviews/pr194-r2-codereview-codex-5-4.md:2890:     function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:2891:         west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/pr194-r2-codereview-codex-5-4.md:2892:         east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/pr194-r2-codereview-codex-5-4.md:2927: enum WheatPlotState {
docs/reviews/pr194-r2-codereview-codex-5-4.md:3005:     function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/pr194-r2-codereview-codex-5-4.md:4958:-              { name: 'remainingWheat', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4959:-              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4968:-              { name: 'remainingWheat', type: 'uint256' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:4969:-              { name: 'regrowUntilTick', type: 'uint64' },
docs/reviews/pr194-r2-codereview-codex-5-4.md:5367:struct WheatPlot {
docs/reviews/pr194-r2-codereview-codex-5-4.md:5368:    WheatPlotState state;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5370:    uint256 remainingWheat;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5371:    uint64 regrowUntilTick;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5692:    WheatPlot westPlot;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5693:    WheatPlot eastPlot;
docs/reviews/pr194-r2-codereview-codex-5-4.md:5966:            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/pr194-r2-codereview-codex-5-4.md:6047:    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/pr194-r2-codereview-codex-5-4.md:6049:            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/pr194-r2-codereview-codex-5-4.md:6050:            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:6051:                plot.state = WheatPlotState.Harvestable;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6052:                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6053:                plot.regrowUntilTick = 0;
docs/reviews/pr194-r2-codereview-codex-5-4.md:6274:  3239	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr194-r2-codereview-codex-5-4.md:6322:  3287	    function getWheatPlots(uint32 clanId)
docs/reviews/pr194-r2-codereview-codex-5-4.md:6326:  3291	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/pr194-r2-codereview-codex-5-4.md:7515:   635	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7725:   424	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr194-r2-codereview-codex-5-4.md:7726:   425	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr194-r2-codereview-codex-5-4.md:7727:   426	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7728:   427	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr194-r2-codereview-codex-5-4.md:7729:   428	                    plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:753:+        WheatPlot[2] wheatPlots;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1122:+            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/r2/pr194-codereview-codex-5-5.md:1203:+    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1205:+            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/r2/pr194-codereview-codex-5-5.md:1206:+            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1207:+                plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1208:+                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1209:+                plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1277:+        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1386:+        WheatPlot memory plot = sim.wheatPlots[plotIdx];
docs/reviews/r2/pr194-codereview-codex-5-5.md:1387:+        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1394:+        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1397:+        plot.remainingWheat -= yield;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1399:+        if (plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:1400:+            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2/pr194-codereview-codex-5-5.md:1401:+            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
docs/reviews/r2/pr194-codereview-codex-5-5.md:1405:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2553:     function getWheatPlots(uint32 clanId)
docs/reviews/r2/pr194-codereview-codex-5-5.md:2833:     function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2834:         west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/r2/pr194-codereview-codex-5-5.md:2835:         east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/r2/pr194-codereview-codex-5-5.md:2870: enum WheatPlotState {
docs/reviews/r2/pr194-codereview-codex-5-5.md:2948:     function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2/pr194-codereview-codex-5-5.md:4550:    11	    WheatPlotState,
docs/reviews/r2/pr194-codereview-codex-5-5.md:4558:    19	    WheatPlot,
docs/reviews/r2/pr194-codereview-codex-5-5.md:4598:    59	    mapping(uint32 => WheatPlot[2]) private _wheatPlots;       // [0]=west [1]=east
docs/reviews/r2/pr194-codereview-codex-5-5.md:4811:   251	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2/pr194-codereview-codex-5-5.md:4812:   252	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:4813:   253	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-5.md:4814:   254	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-5.md:4815:   255	                    plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:4903:   343	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:5050:   115	enum WheatPlotState {
docs/reviews/r2/pr194-codereview-codex-5-5.md:5053:   118	    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-5.md:5069:   134	    HarvestWheat,
docs/reviews/r2/pr194-codereview-codex-5-5.md:5185:   250	struct WheatPlot {
docs/reviews/r2/pr194-codereview-codex-5-5.md:5186:   251	    WheatPlotState state;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5188:   253	    uint256 remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-5.md:5189:   254	    uint64 regrowUntilTick;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6874:   422	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2/pr194-codereview-codex-5-5.md:6875:   423	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:6876:   424	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6877:   425	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-5.md:6878:   426	                    plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7159:  1062	            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/r2/pr194-codereview-codex-5-5.md:7240:  1143	    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7242:  1145	            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/r2/pr194-codereview-codex-5-5.md:7243:  1146	            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:7244:  1147	                plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7245:  1148	                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7246:  1149	                plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-5.md:7314:  1217	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-5.md:8838:   117	enum WheatPlotState {
docs/reviews/r2/pr194-codereview-codex-5-5.md:8841:   120	    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-5.md:8857:   136	    HarvestWheat,
docs/reviews/r2/pr194-codereview-codex-5-5.md:9014:   740	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2/pr194-codereview-codex-5-5.md:9355:  3160	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/r2/pr194-codereview-codex-5-5.md:9460:  3208	    function getWheatPlots(uint32 clanId)
docs/reviews/r2/pr194-codereview-codex-5-5.md:9464:  3212	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/pr200-codereview-codex-5-4.md:1742:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr200-codereview-codex-5-4.md:4839:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/reviews/pr200-codereview-codex-5-4.md:4913:docs/planning/V1/01 Blockchain Game Spec/clanworld_v4_2_state_schema_interface_spec.md:403:### 7.4 WheatPlot
docs/reviews/pr200-codereview-codex-5-4.md:5633:        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/pr200-codereview-codex-5-4.md:5634:            state: WheatPlotState.Harvestable,
docs/reviews/pr200-codereview-codex-5-4.md:5636:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr200-codereview-codex-5-4.md:5637:            regrowUntilTick: 0
docs/reviews/pr200-codereview-codex-5-4.md:5639:        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/pr200-codereview-codex-5-4.md:5640:            state: WheatPlotState.Harvestable,
docs/reviews/pr200-codereview-codex-5-4.md:5642:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr200-codereview-codex-5-4.md:5643:            regrowUntilTick: 0
docs/reviews/pr200-codereview-codex-5-4.md:6056:        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr200-codereview-codex-5-4.md:6057:        if (action == ActionType.HarvestWheat) {
docs/reviews/pr200-codereview-codex-5-4.md:6256:                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr200-codereview-codex-5-4.md:6716:                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr200-codereview-codex-5-4.md:6717:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr200-codereview-codex-5-4.md:6718:                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr200-codereview-codex-5-4.md:6719:                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr200-codereview-codex-5-4.md:6720:                    plot.regrowUntilTick = 0;
docs/reviews/pr200-codereview-codex-5-4.md:6796:        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr200-codereview-codex-5-4.md:7126:enum WheatPlotState {
docs/reviews/pr200-codereview-codex-5-4.md:7129:    WinterLocked
docs/reviews/pr200-codereview-codex-5-4.md:7145:    HarvestWheat,
docs/reviews/pr200-codereview-codex-5-4.md:7258:    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/pr200-codereview-codex-5-4.md:7259:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/pr200-codereview-codex-5-4.md:7711:   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr200-codereview-codex-5-4.md:7712:   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr200-codereview-codex-5-4.md:7713:   380	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr200-codereview-codex-5-4.md:7714:   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr200-codereview-codex-5-4.md:7715:   382	                    plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:753:+        WheatPlot[2] wheatPlots;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1122:+            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:1203:+    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1205:+            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/r2/pr194-codereview-codex-5-4.md:1206:+            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1207:+                plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1208:+                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1209:+                plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1277:+        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1386:+        WheatPlot memory plot = sim.wheatPlots[plotIdx];
docs/reviews/r2/pr194-codereview-codex-5-4.md:1387:+        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1394:+        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1397:+        plot.remainingWheat -= yield;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1399:+        if (plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:1400:+            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2/pr194-codereview-codex-5-4.md:1401:+            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
docs/reviews/r2/pr194-codereview-codex-5-4.md:1405:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2553:     function getWheatPlots(uint32 clanId)
docs/reviews/r2/pr194-codereview-codex-5-4.md:2833:     function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2834:         west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/r2/pr194-codereview-codex-5-4.md:2835:         east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/r2/pr194-codereview-codex-5-4.md:2870: enum WheatPlotState {
docs/reviews/r2/pr194-codereview-codex-5-4.md:2948:     function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2/pr194-codereview-codex-5-4.md:4535:    uint64  internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4536:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4583:enum WheatPlotState {
docs/reviews/r2/pr194-codereview-codex-5-4.md:4586:    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-4.md:4602:    HarvestWheat,
docs/reviews/r2/pr194-codereview-codex-5-4.md:4718:struct WheatPlot {
docs/reviews/r2/pr194-codereview-codex-5-4.md:4719:    WheatPlotState state;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4721:    uint256 remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4722:    uint64 regrowUntilTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:4824:    WheatPlotState,
docs/reviews/r2/pr194-codereview-codex-5-4.md:4832:    WheatPlot,
docs/reviews/r2/pr194-codereview-codex-5-4.md:4872:    mapping(uint32 => WheatPlot[2]) private _wheatPlots;       // [0]=west [1]=east
docs/reviews/r2/pr194-codereview-codex-5-4.md:5064:                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2/pr194-codereview-codex-5-4.md:5065:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5066:                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5067:                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5068:                    plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5156:        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5358:        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/r2/pr194-codereview-codex-5-4.md:5359:        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5368:        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5371:        plot.remainingWheat -= yield;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5373:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:5375:        if (plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5376:            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5377:            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2/pr194-codereview-codex-5-4.md:5380:        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:5617:        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2/pr194-codereview-codex-5-4.md:5618:            state: WheatPlotState.Harvestable,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5620:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5621:            regrowUntilTick: 0
docs/reviews/r2/pr194-codereview-codex-5-4.md:5623:        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2/pr194-codereview-codex-5-4.md:5624:            state: WheatPlotState.Harvestable,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5626:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2/pr194-codereview-codex-5-4.md:5627:            regrowUntilTick: 0
docs/reviews/r2/pr194-codereview-codex-5-4.md:5987:        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/r2/pr194-codereview-codex-5-4.md:5988:        if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:6150:    function getWheatPlots(uint32 clanId)
docs/reviews/r2/pr194-codereview-codex-5-4.md:6154:        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/r2/pr194-codereview-codex-5-4.md:6467:struct WheatPlot {
docs/reviews/r2/pr194-codereview-codex-5-4.md:6468:    WheatPlotState state;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6470:    uint256 remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6471:    uint64 regrowUntilTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6646:    WheatPlot westPlot;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6647:    WheatPlot eastPlot;
docs/reviews/r2/pr194-codereview-codex-5-4.md:6964:    function getWheatPlots(uint32 clanId)
docs/reviews/r2/pr194-codereview-codex-5-4.md:6967:        returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2/pr194-codereview-codex-5-4.md:7035:    11	    WheatPlotState,
docs/reviews/r2/pr194-codereview-codex-5-4.md:7043:    19	    WheatPlot,
docs/reviews/r2/pr194-codereview-codex-5-4.md:7084:    60	    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/r2/pr194-codereview-codex-5-4.md:7146:   122	        WheatPlot[2] wheatPlots;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7446:   422	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2/pr194-codereview-codex-5-4.md:7447:   423	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7448:   424	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7449:   425	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7450:   426	                    plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7650:   621	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7838:   809	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/r2/pr194-codereview-codex-5-4.md:7839:   810	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7848:   819	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7851:   822	        plot.remainingWheat -= yield;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7853:   824	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:7855:   826	        if (plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:7856:   827	            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7857:   828	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2/pr194-codereview-codex-5-4.md:7860:   831	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8096:  1062	            _simulateRegrowWheatPlots(sim, tick);
docs/reviews/r2/pr194-codereview-codex-5-4.md:8177:  1143	    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8179:  1145	            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/r2/pr194-codereview-codex-5-4.md:8180:  1146	            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8181:  1147	                plot.state = WheatPlotState.Harvestable;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8182:  1148	                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8183:  1149	                plot.regrowUntilTick = 0;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8251:  1217	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8360:  1326	        WheatPlot memory plot = sim.wheatPlots[plotIdx];
docs/reviews/r2/pr194-codereview-codex-5-4.md:8361:  1327	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8368:  1334	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8371:  1337	        plot.remainingWheat -= yield;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8373:  1339	        if (plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:8374:  1340	            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2/pr194-codereview-codex-5-4.md:8375:  1341	            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
docs/reviews/r2/pr194-codereview-codex-5-4.md:8379:  1345	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:9444:  2400	        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2/pr194-codereview-codex-5-4.md:9445:  2401	            state: WheatPlotState.Harvestable,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9447:  2403	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9448:  2404	            regrowUntilTick: 0
docs/reviews/r2/pr194-codereview-codex-5-4.md:9450:  2406	        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2/pr194-codereview-codex-5-4.md:9451:  2407	            state: WheatPlotState.Harvestable,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9453:  2409	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2/pr194-codereview-codex-5-4.md:9454:  2410	            regrowUntilTick: 0
docs/reviews/r2/pr194-codereview-codex-5-4.md:10043:  2999	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/r2/pr194-codereview-codex-5-4.md:10044:  3000	        if (action == ActionType.HarvestWheat) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:10204:  3160	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/r2/pr194-codereview-codex-5-4.md:10209:  3160	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/r2/pr194-codereview-codex-5-4.md:10257:  3208	    function getWheatPlots(uint32 clanId)
docs/reviews/r2/pr194-codereview-codex-5-4.md:10261:  3212	        returns (WheatPlot memory west, WheatPlot memory east)
docs/reviews/r2/pr194-codereview-codex-5-4.md:10628:   117	enum WheatPlotState {
docs/reviews/r2/pr194-codereview-codex-5-4.md:10631:   120	    WinterLocked
docs/reviews/r2/pr194-codereview-codex-5-4.md:10647:   136	    HarvestWheat,
docs/reviews/r2/pr194-codereview-codex-5-4.md:10770:   254	struct WheatPlot {
docs/reviews/r2/pr194-codereview-codex-5-4.md:10771:   255	    WheatPlotState state;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10773:   257	    uint256 remainingWheat;
docs/reviews/r2/pr194-codereview-codex-5-4.md:10774:   258	    uint64 regrowUntilTick;
docs/reviews/r2/pr194-codereview-codex-5-4.md:11121:   740	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2/pr194-codereview-codex-5-4.md:13661:   242	    function getWheatPlots(uint32) external pure override returns (WheatPlot memory west, WheatPlot memory east) {
docs/reviews/r2/pr194-codereview-codex-5-4.md:13662:   243	        west = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/r2/pr194-codereview-codex-5-4.md:13663:   244	        east = WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0});
docs/reviews/r2/pr194-codereview-codex-5-4.md:13739:   320	            westPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
docs/reviews/r2/pr194-codereview-codex-5-4.md:13740:   321	            eastPlot: WheatPlot({state: WheatPlotState.Harvestable, region: 0, remainingWheat: 0, regrowUntilTick: 0}),
docs/reviews/r2/pr194-codereview-codex-5-4.md:14939:   431	    WheatPlot westPlot;
docs/reviews/r2/pr194-codereview-codex-5-4.md:14940:   432	    WheatPlot eastPlot;
docs/reviews/pr198-r2-codereview-codex-5-5.md:708:     mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/pr198-r2-codereview-codex-5-5.md:748:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:5310:  2064	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr198-r2-codereview-codex-5-5.md:5311:  2065	        if (action == ActionType.HarvestWheat) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:5590:   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr198-r2-codereview-codex-5-5.md:5591:   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:5592:   380	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5593:   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5594:   382	                    plot.regrowUntilTick = 0;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5665:   453	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:5803:   640	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/pr198-r2-codereview-codex-5-5.md:5804:   641	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:5813:   650	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5816:   653	        plot.remainingWheat -= yield;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5818:   655	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/pr198-r2-codereview-codex-5-5.md:5820:   657	        if (plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:5821:   658	            plot.state = WheatPlotState.Regrowing;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5822:   659	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr198-r2-codereview-codex-5-5.md:5825:   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:6438:   140	    HarvestWheat,
docs/reviews/pr198-r2-codereview-codex-5-5.md:6679:  1044	        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/pr198-r2-codereview-codex-5-5.md:6680:  1045	            state: WheatPlotState.Harvestable,
docs/reviews/pr198-r2-codereview-codex-5-5.md:6682:  1047	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr198-r2-codereview-codex-5-5.md:6683:  1048	            regrowUntilTick: 0
docs/reviews/pr198-r2-codereview-codex-5-5.md:6685:  1050	        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/pr198-r2-codereview-codex-5-5.md:6686:  1051	            state: WheatPlotState.Harvestable,
docs/reviews/pr198-r2-codereview-codex-5-5.md:6688:  1053	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr198-r2-codereview-codex-5-5.md:6689:  1054	            regrowUntilTick: 0
docs/reviews/pr198-r2-codereview-codex-5-5.md:7058:docs/reviews/pr199-codereview-codex-5-4.md:2818:   710	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:7232:docs/reviews/pr199-codereview-codex-5-4.md:6355:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:326:                enum: ['ChopWood','MineIron','FishDocks','FishDeepSea','HarvestWheat','DepositResources','BuildWall','UpgradeBase','UpgradeMonument','DefendBase','MarketBuy','MarketSell','Wait']
docs/reviews/pr198-r2-codereview-codex-5-5.md:7926:docs/reviews/pr250-codereview-codex-5-4.md:9786:   833	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:8359:docs/reviews/pr198-r2-codereview-codex-5-5.md:5825:   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:8809:docs/reviews/pr194-r2-codereview-codex-5-5.md:1397:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:9028:docs/reviews/pr250-codereview-opus-4-7.md:5:PR #250 is a substantial Phase-10 cohesion ship: winter schedule, 2× food upkeep + per-clansman wood burn, cold damage → wall degradation → RNG clansman death, wheat plot WinterLocked transitions, and a unified `_markClanDead` helper. The structural shape is good — death routes through `_markClansmanDead`/`_markClanDead`, dead clans short-circuit at `submitClanOrders` and `_settleCompletingMissions`, defenders are cleared, vault resources burn, gold survives. Tests are dense and exercise most happy paths.
docs/reviews/pr198-r2-codereview-codex-5-5.md:9032:docs/reviews/pr250-codereview-opus-4-7.md:179:6. **Spec compliance is solid except for M1 (wood burn).** §A2 winter timing is correctly implemented as a global recurring schedule (PR v4.5 addendum supersedes the per-season-reset older spec). §10 elimination — vault burn yes, gold preservation yes, mission/defender cleanup yes. §10.2/§10.3 cold damage thresholds (every 2) — correct via integer-division-step pattern. §10.4 wheat plot WinterLocked + restart — correct, modulo M2 boundary.
docs/reviews/pr198-r2-codereview-codex-5-5.md:9302:docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4910:  1359	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:9597:docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7614:   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:9799:docs/reviews/r2/pr194-codereview-codex-5-5.md:1405:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:10725:docs/reviews/pr194-r2-codereview-codex-5-4.md:1397:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:11623:docs/reviews/pr198-r2-codereview-codex-5-4.md:5371:   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:12697:docs/reviews/pr199-codereview-codex-5-5.md:13428:   833	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:12809:docs/reviews/r2/pr194-codereview-codex-5-4.md:1405:+        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:12971:docs/reviews/r2/pr194-codereview-codex-5-4.md:5380:        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:13072:docs/reviews/r2/pr194-codereview-codex-5-4.md:7860:   831	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:13117:docs/reviews/r2/pr194-codereview-codex-5-4.md:8379:  1345	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-5.md:13523:docs/planning/V1/02 Frontend Spec/clanworld_agent_runner_spec.md:326:                enum: ['ChopWood','MineIron','FishDocks','FishDeepSea','HarvestWheat','DepositResources','BuildWall','UpgradeBase','UpgradeMonument','DefendBase','MarketBuy','MarketSell','Wait']
docs/reviews/pr198-r2-codereview-codex-5-4.md:708:     mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/pr198-r2-codereview-codex-5-4.md:748:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:4285:        WheatPlotState startingPlotState =
docs/reviews/pr198-r2-codereview-codex-5-4.md:4286:            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
docs/reviews/pr198-r2-codereview-codex-5-4.md:4289:        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/pr198-r2-codereview-codex-5-4.md:4292:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr198-r2-codereview-codex-5-4.md:4293:            regrowUntilTick: 0
docs/reviews/pr198-r2-codereview-codex-5-4.md:4295:        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/pr198-r2-codereview-codex-5-4.md:4298:            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/pr198-r2-codereview-codex-5-4.md:4299:            regrowUntilTick: 0
docs/reviews/pr198-r2-codereview-codex-5-4.md:4796:    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/pr198-r2-codereview-codex-5-4.md:4797:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/pr198-r2-codereview-codex-5-4.md:4844:enum WheatPlotState {
docs/reviews/pr198-r2-codereview-codex-5-4.md:4847:    WinterLocked
docs/reviews/pr198-r2-codereview-codex-5-4.md:4863:    HarvestWheat,
docs/reviews/pr198-r2-codereview-codex-5-4.md:4983:struct WheatPlot {
docs/reviews/pr198-r2-codereview-codex-5-4.md:4984:    WheatPlotState state;
docs/reviews/pr198-r2-codereview-codex-5-4.md:4986:    uint256 remainingWheat;
docs/reviews/pr198-r2-codereview-codex-5-4.md:5162:   453	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:5349:   640	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/pr198-r2-codereview-codex-5-4.md:5350:   641	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:5359:   650	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/pr198-r2-codereview-codex-5-4.md:5362:   653	        plot.remainingWheat -= yield;
docs/reviews/pr198-r2-codereview-codex-5-4.md:5364:   655	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/pr198-r2-codereview-codex-5-4.md:5366:   657	        if (plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:5367:   658	            plot.state = WheatPlotState.Regrowing;
docs/reviews/pr198-r2-codereview-codex-5-4.md:5368:   659	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/pr198-r2-codereview-codex-5-4.md:5371:   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:6474:  2064	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/pr198-r2-codereview-codex-5-4.md:6475:  2065	        if (action == ActionType.HarvestWheat) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:6674:  2264	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/pr198-r2-codereview-codex-5-4.md:7074:   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/pr198-r2-codereview-codex-5-4.md:7075:   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/pr198-r2-codereview-codex-5-4.md:7076:   380	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/pr198-r2-codereview-codex-5-4.md:7077:   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/pr198-r2-codereview-codex-5-4.md:7078:   382	                    plot.regrowUntilTick = 0;
docs/reviews/pr198-r2-codereview-codex-5-4.md:13218:   259	struct WheatPlot {
docs/reviews/pr198-r2-codereview-codex-5-4.md:13219:   260	    WheatPlotState state;
docs/reviews/pr198-r2-codereview-codex-5-4.md:13234:    11	    WheatPlotState,
docs/reviews/pr198-r2-codereview-codex-5-4.md:13242:    19	    WheatPlot,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:725:     mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:765:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:3975:    67	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:3976:    68	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4025:   117	enum WheatPlotState {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4028:   120	    WinterLocked
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4044:   136	    HarvestWheat,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4162:   254	struct WheatPlot {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4163:   255	    WheatPlotState state;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4165:   257	    uint256 remainingWheat;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4166:   258	    uint64 regrowUntilTick;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4339:   431	    WheatPlot westPlot;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4340:   432	    WheatPlot eastPlot;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4648:   740	    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east);
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4708:  1157	    function _simulateRegrowWheatPlots(SettlementSimulation memory sim, uint64 tick) internal pure {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4710:  1159	            WheatPlot memory plot = sim.wheatPlots[pi];
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4711:  1160	            if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4712:  1161	                plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4713:  1162	                plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4714:  1163	                plot.regrowUntilTick = 0;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4782:  1231	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4891:  1340	        WheatPlot memory plot = sim.wheatPlots[plotIdx];
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4892:  1341	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4899:  1348	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4902:  1351	        plot.remainingWheat -= yield;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4904:  1353	        if (plot.remainingWheat == 0) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4905:  1354	            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4906:  1355	            plot.regrowUntilTick = _addTicksClamped(tick, ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS);
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:4910:  1359	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:6994:  2002	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:6995:  2003	        if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7194:  2202	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7330:   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7331:   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7332:   380	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7333:   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7334:   382	                    plot.regrowUntilTick = 0;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7405:   453	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7592:   640	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7593:   641	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7602:   650	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7605:   653	        plot.remainingWheat -= yield;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7607:   655	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7609:   657	        if (plot.remainingWheat == 0) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7610:   658	            plot.state = WheatPlotState.Regrowing;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7611:   659	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7614:   662	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7681:    69	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7682:    70	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7733:   121	enum WheatPlotState {
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7736:   124	    WinterLocked
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:7752:   140	    HarvestWheat,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9796:  1044	        _wheatPlots[clanId][0] = WheatPlot({
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9797:  1045	            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9799:  1047	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9800:  1048	            regrowUntilTick: 0
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9802:  1050	        _wheatPlots[clanId][1] = WheatPlot({
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9803:  1051	            state: WheatPlotState.Harvestable,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9805:  1053	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
docs/reviews/r2-phase6/pr198-codereview-codex-5-5.md:9806:  1054	            regrowUntilTick: 0
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:725:     mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:765:         } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:3816:    WheatPlotState,
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:3824:    WheatPlot,
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:3865:    mapping(uint32 => WheatPlot[2]) private _wheatPlots; // [0]=west [1]=east
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:3929:        WheatPlot[2] wheatPlots;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4182:    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4183:    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4232:enum WheatPlotState {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4235:    WinterLocked
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4251:    HarvestWheat,
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4369:struct WheatPlot {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4370:    WheatPlotState state;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4372:    uint256 remainingWheat;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4373:    uint64 regrowUntilTick;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:4674:   453	        } else if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:5800:  2202	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:5886:    69	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:5887:    70	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:5938:   121	enum WheatPlotState {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:5941:   124	    WinterLocked
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:5957:   140	    HarvestWheat,
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:6076:   259	struct WheatPlot {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:6077:   260	    WheatPlotState state;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:8959:   378	                WheatPlot storage plot = _wheatPlots[clanId][pi];
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:8960:   379	                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:8961:   380	                    plot.state = WheatPlotState.Harvestable;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:8962:   381	                    plot.remainingWheat = ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:8963:   382	                    plot.regrowUntilTick = 0;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:9150:   440	    WheatPlot westPlot;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:9151:   441	    WheatPlot eastPlot;
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:10607:  2002	        // HarvestWheat: must go to WestFarms or EastFarms
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:10608:  2003	        if (action == ActionType.HarvestWheat) {
docs/reviews/r2-phase6/pr198-codereview-codex-5-4.md:10677:  2246	        returns (WheatPlot memory west, WheatPlot memory east)

exec
/bin/bash -lc "sed -n '1020,1105p' packages/contracts/src/ClanWorld.sol && sed -n '930,1010p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
exec
/bin/bash -lc 'rg -n "MAX.*CLAN|MAX_CLANS|24|_nextClanId|_allClanIds|mintClan|clan cap|capacity" packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol docs/planning/clanworld_v4_spec.md' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:68:    uint32 private _nextClanId;
packages/contracts/src/ClanWorld.sol:70:    uint32[] private _allClanIds;
packages/contracts/src/ClanWorld.sol:100:        _nextClanId = 1;
packages/contracts/src/ClanWorld.sol:892:        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
packages/contracts/src/ClanWorld.sol:914:    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
packages/contracts/src/ClanWorld.sol:916:        for (uint256 i = 0; i < _allClanIds.length; i++) {
packages/contracts/src/ClanWorld.sol:917:            uint32 clanId = _allClanIds[i];
packages/contracts/src/ClanWorld.sol:1004:    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
packages/contracts/src/ClanWorld.sol:1006:        require(_allClanIds.length < 12, "ClanWorld: max clans");
packages/contracts/src/ClanWorld.sol:1007:        clanId = _nextClanId++;
packages/contracts/src/ClanWorld.sol:1074:        _allClanIds.push(clanId);
packages/contracts/src/ClanWorld.sol:2095:            // Over-capacity buys are rejected at submission; no partial fills or overflow refunds.
packages/contracts/src/ClanWorld.sol:2431:        LeaderboardEntry[] memory lb = new LeaderboardEntry[](_allClanIds.length);
packages/contracts/src/ClanWorld.sol:2432:        for (uint256 i = 0; i < _allClanIds.length; i++) {
packages/contracts/src/ClanWorld.sol:2433:            uint32 cid = _allClanIds[i];
packages/contracts/src/ClanWorld.sol:2559:        for (uint256 i = 0; i < _allClanIds.length; i++) {
packages/contracts/src/ClanWorld.sol:2560:            uint32 cid = _allClanIds[i];
packages/contracts/src/ClanWorld.sol:2572:        for (uint256 i = 0; i < _allClanIds.length; i++) {
packages/contracts/src/ClanWorld.sol:2573:            uint32 cid = _allClanIds[i];
packages/contracts/src/IClanWorld.sol:668:    function mintClan(address to) external returns (uint32 clanId, uint256 iftTokenId);
docs/planning/clanworld_v4_spec.md:434:If a gathering tick would exceed remaining carrying capacity for a resource, the yield is **clamped** to remaining capacity.
docs/planning/clanworld_v4_spec.md:438:- worker wood capacity is `15e18`
docs/planning/clanworld_v4_spec.md:442:If the relevant capacity becomes full due to that clamped yield:
docs/planning/clanworld_v4_spec.md:495:If a clansman receives bandit-drop loot above carrying capacity:
docs/planning/clanworld_v4_spec.md:498:No over-capacity exception exists.
docs/planning/clanworld_v4_spec.md:805:- any clansman loot above carrying capacity is burned

 succeeded in 0ms:

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
        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
        clan.blueprintBalance = 0;
        // Starting vault per v4 spec §12.4
        clan.vaultWood = 20e18;
        clan.vaultIron = 0;
        clan.vaultWheat = 20e18;
        clan.vaultFish = 2e18;

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
            cs.carryWood = 0;
            cs.carryIron = 0;
            cs.carryWheat = 0;
            cs.carryFish = 0;
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
        nonReentrant
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
                        missionNonce: 0,
                        marketMode: MarketExecutionMode.None
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
        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7

        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)

exec
/bin/bash -lc "sed -n '820,875p' docs/planning/clanworld_v4_spec.md && sed -n '1188,1198p' docs/planning/clanworld_v4_spec.md && sed -n '940,995p' docs/planning/clanworld_v4_spec.md" in /home/claude/code/clan-world
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

- two wheat plots with harvest/regrow cycle
- starvation weakens but does not kill outside Winter
- scheduled vs immediate market execution split
- no slippage guard in v1
- deterministic visible mutable bandit targeting
- equal defender loot split by defender, not defense weight
- winter every 110 ticks for 10 ticks
- cold damage reset each winter
- wall-only structural degradation in v1
- level-10 monument cap
- fixed 360-tick season
## 8.6 Build validation
If a clansman reaches homebase and attempts a build action:
- if required resources are present in vault, the action succeeds and consumes one action tick
- if required resources are absent, the clansman becomes `WAITING`

## 8.7 Damage interaction
In v1:
- walls may lose levels from winter cold damage and bandit pressure
- base levels do not lose levels from bandits in v1
- monument levels do not lose levels in v1

---

# 9. Victory Condition and Season Lifecycle

## 9.1 Primary ranking rule
Eligible clans are ranked by:
1. highest `monumentLevel`
2. earliest tick reaching that level
3. highest surviving vault loot-value
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

exec
/bin/bash -lc "sed -n '1,140p' packages/runner/src/runnerCastHeartbeat.ts && sed -n '1,70p' packages/shared/src/adapters/IChainClient.ts" in /home/claude/code/clan-world
 succeeded in 0ms:
import {
  ContractFunctionRevertedError,
  createPublicClient,
  createWalletClient,
  http,
  type Account,
  type PublicClient,
  type WalletClient,
} from 'viem';
import { privateKeyToAccount } from 'viem/accounts';
import { baseSepolia } from '@clan-world/shared/adapters';
import {
  HeartbeatRateLimitedError,
  type IHeartbeatCaller,
} from '@clan-world/agents/seams';

/**
 * Minimal ABI: only the `heartbeat()` write and the `nextHeartbeatAtTs` field
 * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
 * ABI here so the runner stays decoupled from contract-package versioning.
 */
const HEARTBEAT_ABI = [
  {
    type: 'function',
    name: 'heartbeat',
    inputs: [],
    outputs: [],
    stateMutability: 'nonpayable',
  },
  {
    type: 'function',
    name: 'getWorldState',
    inputs: [],
    outputs: [
      {
        name: '',
        type: 'tuple',
        components: [
          { name: 'currentTick', type: 'uint64' },
          { name: 'seasonStartTick', type: 'uint64' },
          { name: 'seasonEndTick', type: 'uint64' },
          { name: 'seasonFinalized', type: 'bool' },
          { name: 'nextHeartbeatAtTs', type: 'uint64' },
          { name: 'nextBanditSpawnEligibleTick', type: 'uint64' },
          { name: 'currentBanditSpawnChanceBps', type: 'uint16' },
          { name: 'currentTickSeed', type: 'bytes32' },
          { name: 'activeBanditId', type: 'uint32' },
          { name: 'winterActive', type: 'bool' },
          { name: 'winterStartsAtTick', type: 'uint64' },
          { name: 'winterEndsAtTick', type: 'uint64' },
          { name: 'nextCommitSequence', type: 'uint64' },
        ],
      },
    ],
    stateMutability: 'view',
  },
] as const;

export interface RunnerHeartbeatConfig {
  /** Hex-encoded 64-char private key, optionally 0x-prefixed. */
  privateKey: string;
  /** Override RPC URL; defaults to the Base Sepolia public endpoint. */
  rpcUrl?: string;
  /** ClanWorld contract address. */
  contractAddress: `0x${string}`;
}

/**
 * Reads `RUNNER_PRIVATE_KEY`, `RPC_URL_PRIMARY`, `CLAN_WORLD_CONTRACT_ADDRESS`
 * from env. Throws if `RUNNER_PRIVATE_KEY` is missing — the runner intentionally
 * does not generate or store its own wallet; provisioning is operator-side.
 */
export function configFromEnv(env: NodeJS.ProcessEnv = process.env): RunnerHeartbeatConfig {
  const pk = env['RUNNER_PRIVATE_KEY'];
  if (!pk) {
    throw new Error(
      'RUNNER_PRIVATE_KEY is not set — the runner needs a dedicated wallet (NEVER reuse an Elder wallet). ' +
        'Provision a fresh key, fund it with testnet ETH, and export RUNNER_PRIVATE_KEY before starting the daemon.',
    );
  }
  const contractAddress = env['CLAN_WORLD_CONTRACT_ADDRESS'];
  if (!contractAddress || !/^0x[0-9a-fA-F]{40}$/.test(contractAddress)) {
    throw new Error(
      `CLAN_WORLD_CONTRACT_ADDRESS missing or invalid; expected 0x-prefixed 40-hex-char address, got ${String(contractAddress)}`,
    );
  }
  return {
    privateKey: pk,
    rpcUrl: env['RPC_URL_PRIMARY'] || env['RPC_URL_FALLBACK'],
    contractAddress: contractAddress as `0x${string}`,
  };
}

/**
 * Viem-backed `IHeartbeatCaller`. Wallet account is the dedicated runner key.
 *
 * Rate-limit detection: ClanWorld.heartbeat() reverts when called before
 * `nextHeartbeatAtTs`. We don't have a typed custom error in the ABI, so on
 * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
 * still in the future, throw `HeartbeatRateLimitedError(nextAllowedAt)`.
 * Other reverts surface as the original error.
 */
export class RunnerCastHeartbeat implements IHeartbeatCaller {
  private readonly publicClient: PublicClient;
  private readonly walletClient: WalletClient;
  private readonly account: Account;
  private readonly contractAddress: `0x${string}`;

  constructor(cfg: RunnerHeartbeatConfig) {
    const pk = normalizePk(cfg.privateKey);
    this.account = privateKeyToAccount(pk);
    const transport = cfg.rpcUrl ? http(cfg.rpcUrl) : http();
    this.publicClient = createPublicClient({ chain: baseSepolia, transport });
    this.walletClient = createWalletClient({
      account: this.account,
      chain: baseSepolia,
      transport,
    });
    this.contractAddress = cfg.contractAddress;
  }

  async callHeartbeat(): Promise<{ txHash: string }> {
    try {
      const hash = await this.walletClient.writeContract({
        account: this.account,
        chain: baseSepolia,
        address: this.contractAddress,
        abi: HEARTBEAT_ABI,
        functionName: 'heartbeat',
        args: [],
      });
      // Wait for confirmation per the seam contract ("not fire-and-forget").
      const receipt = await this.publicClient.waitForTransactionReceipt({ hash });
      if (receipt.status !== 'success') {
        // Mined-but-reverted. Most common cause is the rate-limit window
        // hadn't elapsed yet (when simulation succeeded but execution didn't).
        // Re-read state to upgrade to HeartbeatRateLimitedError when applicable.
        const next = await this.readNextHeartbeatAt().catch(() => undefined);
        if (next !== undefined && next > Math.floor(Date.now() / 1000)) {
          throw new HeartbeatRateLimitedError(next);
import fs from 'node:fs';
import { createPublicClient, createWalletClient, http, fallback, defineChain } from 'viem';
import { privateKeyToAccount } from 'viem/accounts';
import type { ClanFullView, ClanOrder, Tick } from '../types';
import { readEnv } from './_env';

export interface IChainClient {
  getCurrentTick(): Promise<Tick>;
  submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }>;
  getClanFullView(clanId: string): Promise<ClanFullView>;
}

const DEFAULT_CONTRACT_ADDRESS = '0x1BF5649f29CbB53E117a5aE969A18A71790f83E8' as const;

export const baseSepolia = defineChain({
  id: 84532,
  name: 'Base Sepolia',
  nativeCurrency: { name: 'Ether', symbol: 'ETH', decimals: 18 },
  rpcUrls: {
    default: { http: ['https://sepolia.base.org'] },
  },
});

// Minimal ABI — only the two read functions we call.
const CLAN_WORLD_ABI = [
  {
    type: 'function',
    name: 'getWorldSnapshot',
    inputs: [],
    outputs: [
      {
        name: '',
        type: 'tuple',
        components: [
          { name: 'currentTick', type: 'uint64' },
          { name: 'seasonStartTick', type: 'uint64' },
          { name: 'seasonEndTick', type: 'uint64' },
          { name: 'seasonFinalized', type: 'bool' },
          { name: 'winterActive', type: 'bool' },
          { name: 'winterStartsAtTick', type: 'uint64' },
          { name: 'winterEndsAtTick', type: 'uint64' },
          { name: 'activeBanditId', type: 'uint32' },
          { name: 'currentTickSeed', type: 'bytes32' },
          {
            name: 'leaderboard',
            type: 'tuple[]',
            components: [
              { name: 'clanId', type: 'uint32' },
              { name: 'owner', type: 'address' },
              { name: 'monumentLevel', type: 'uint8' },
              { name: 'baseLevel', type: 'uint8' },
              { name: 'wallLevel', type: 'uint8' },
              { name: 'livingClansmen', type: 'uint8' },
              { name: 'state', type: 'uint8' },
              { name: 'lootValue', type: 'uint256' },
            ],
          },
        ],
      },
    ],
    stateMutability: 'view',
  },
  {
    type: 'function',
    name: 'getClanFullView',
    inputs: [{ name: '', type: 'uint32' }],
    outputs: [
      {
        name: '',
        type: 'tuple',

exec
/bin/bash -lc "sed -n '395,435p' packages/contracts/src/IClanWorld.sol && sed -n '760,785p' packages/contracts/src/IClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

    function quoteTravel(uint8 srcRegion, uint8 dstRegion) external view returns (uint8 travelTicks, bytes8 path);

    function quoteLootValueRaw(uint32 clanId) external view returns (uint256 lootValue);

    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue);

    // -------------------------------------------------------------------------
    // UI indexer aggregator getters (v4.4 additions)
    //
    // These are pure compositions of the derived getters above and exist only
    // to reduce indexer RPC traffic. They MUST follow the same non-mutation
    // rule as the underlying derived getters.
    // -------------------------------------------------------------------------

    /// @notice Single-call top-level world state plus per-clan leaderboard data.
    ///         Drives top HUD bar, season clock, winter indicator, leaderboard.
    function getWorldSnapshot() external view returns (WorldSnapshot memory);

    /// @notice Single-call complete clan rendering data: derived clan + all
    ///         clansmen with derived state and active missions + plot states +
    ///         defender bookkeeping. Drives the entire sprite layer.
    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory);

    /// @notice Single-call market panel data: 4 pool reserves + spot prices +
    ///         scheduled queues for current and next tick. Drives market UI

exec
/bin/bash -lc "sed -n '20,55p' packages/shared/src/adapters/IChainClient.ts && sed -n '270,305p' packages/shared/src/adapters/IChainClient.ts" in /home/claude/code/clan-world
 succeeded in 0ms:
    default: { http: ['https://sepolia.base.org'] },
  },
});

// Minimal ABI — only the two read functions we call.
const CLAN_WORLD_ABI = [
  {
    type: 'function',
    name: 'getWorldSnapshot',
    inputs: [],
    outputs: [
      {
        name: '',
        type: 'tuple',
        components: [
          { name: 'currentTick', type: 'uint64' },
          { name: 'seasonStartTick', type: 'uint64' },
          { name: 'seasonEndTick', type: 'uint64' },
          { name: 'seasonFinalized', type: 'bool' },
          { name: 'winterActive', type: 'bool' },
          { name: 'winterStartsAtTick', type: 'uint64' },
          { name: 'winterEndsAtTick', type: 'uint64' },
          { name: 'activeBanditId', type: 'uint32' },
          { name: 'currentTickSeed', type: 'bytes32' },
          {
            name: 'leaderboard',
            type: 'tuple[]',
            components: [
              { name: 'clanId', type: 'uint32' },
              { name: 'owner', type: 'address' },
              { name: 'monumentLevel', type: 'uint8' },
              { name: 'baseLevel', type: 'uint8' },
              { name: 'wallLevel', type: 'uint8' },
              { name: 'livingClansmen', type: 'uint8' },
              { name: 'state', type: 'uint8' },
              { name: 'lootValue', type: 'uint256' },

    this.contractAddress = (readEnv('CLAN_WORLD_CONTRACT_ADDRESS') ??
      DEFAULT_CONTRACT_ADDRESS) as `0x${string}`;

    this.client = createPublicClient({
      chain: baseSepolia,
      transport: this.transport,
    });
  }

  async getCurrentTick(): Promise<Tick> {
    const snapshot = await this.client.readContract({
      address: this.contractAddress,
      abi: CLAN_WORLD_ABI,
      functionName: 'getWorldSnapshot',
    });
    return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
  }

  async submitOrders(clanId: string, orders: ClanOrder[]): Promise<{ txHash: string }> {
    // Wave 0: single-Elder only — concurrent nonce coordination deferred to Wave 1
    const parsedClanId = parseInt(clanId, 10);
    if (isNaN(parsedClanId) || String(parsedClanId) !== clanId.trim()) {
      throw new Error(`submitOrders: clanId must be a decimal integer, got '${clanId}'`);
    }

    for (const order of orders) {
      if (order.kind === 'mission') {
        const { clansmanId, gotoRegion, action } = order.payload;
        if (clansmanId === undefined || gotoRegion === undefined || action === undefined) {
          throw new Error(`submitOrders: mission order missing required payload fields (clansmanId, gotoRegion, action)`);
        }
      }
    }

    const nonMissionOrders = orders.filter(o => o.kind !== 'mission');

exec
/bin/bash -lc "rg -n \"currentSeasonNumber|nextHeartbeatAtTick\" apps packages -g '*.ts' -g '*.tsx' -g '*.sol' -g '*.json'" in /home/claude/code/clan-world
 succeeded in 0ms:
apps/server/convex/schema.ts:10:    currentSeasonNumber: v.optional(v.number()),
apps/server/convex/schema.ts:16:    nextHeartbeatAtTick: v.optional(v.number()),
packages/contracts/test/ClanWorldStub.t.sol:41:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
packages/contracts/test/ClanWorldStub.t.sol:44:        assertEq(ws.nextHeartbeatAtTick, ws.currentTick + 1, "next heartbeat opens currentTick + 1");
packages/contracts/src/ClanWorld.sol:92:        _world.currentSeasonNumber = 1;
packages/contracts/src/ClanWorld.sol:93:        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
packages/contracts/src/ClanWorld.sol:907:        _world.nextHeartbeatAtTick = newTick + 1;
packages/contracts/src/ClanWorld.sol:943:            _world.currentSeasonNumber += 1;
packages/contracts/src/ClanWorld.sol:2452:            currentSeasonNumber: _world.currentSeasonNumber,
packages/contracts/src/ClanWorld.sol:2453:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
packages/contracts/test/HeartbeatOrdering.t.sol:277:    //   - currentSeasonNumber increments on the heartbeat that closes tick
packages/contracts/test/HeartbeatOrdering.t.sol:283:        assertEq(ws0.currentSeasonNumber, 1, "season starts at 1");
packages/contracts/test/HeartbeatOrdering.t.sol:293:        assertEq(ws1.currentSeasonNumber, 2, "season must have incremented to 2 after Steps 1-4");
packages/contracts/src/IClanWorld.sol:198:    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
packages/contracts/src/IClanWorld.sol:199:    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
packages/contracts/src/IClanWorld.sol:421:    uint64 currentSeasonNumber;
packages/contracts/src/IClanWorld.sol:422:    uint64 nextHeartbeatAtTick;
packages/contracts/src/ClanWorldStub.sol:52:        _world.currentSeasonNumber = 1;
packages/contracts/src/ClanWorldStub.sol:53:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
packages/contracts/src/ClanWorldStub.sol:81:        _world.nextHeartbeatAtTick = _world.currentTick + 1;
packages/contracts/src/ClanWorldStub.sol:316:            currentSeasonNumber: _world.currentSeasonNumber,
packages/contracts/src/ClanWorldStub.sol:317:            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
packages/contracts/abi/IClanWorld.json:1995:              "name": "currentSeasonNumber",
packages/contracts/abi/IClanWorld.json:2000:              "name": "nextHeartbeatAtTick",
packages/contracts/abi/IClanWorld.json:2112:              "name": "currentSeasonNumber",
packages/contracts/abi/IClanWorld.json:2117:              "name": "nextHeartbeatAtTick",
packages/contracts/test/ClanWorld.t.sol:2417:        assertEq(ws.currentSeasonNumber, 1, "season starts at 1");
packages/contracts/test/ClanWorld.t.sol:2477:        assertEq(ws.currentSeasonNumber, 2, "season number should increment");
packages/contracts/test/ClanWorld.t.sol:2486:    function test_nextHeartbeatAtTick_tracks_tick() public {
packages/contracts/test/ClanWorld.t.sol:2488:        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
packages/contracts/test/ClanWorld.t.sol:2493:        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");

exec
/bin/bash -lc "sed -n '140,200p' packages/runner/src/runnerCastHeartbeat.ts && sed -n '305,380p' packages/shared/src/adapters/IChainClient.ts" in /home/claude/code/clan-world
 succeeded in 0ms:
          throw new HeartbeatRateLimitedError(next);
        }
        throw new Error(`heartbeat tx ${hash} reverted on-chain`);
      }
      return { txHash: hash };
    } catch (err) {
      // Already a rate-limit error — rethrow immediately; no second RPC read.
      if (err instanceof HeartbeatRateLimitedError) throw err;
      // Attempt to upgrade only simulation-level contract reverts to
      // HeartbeatRateLimitedError; pre-flight/RPC errors must surface unchanged.
      if (!(err instanceof ContractFunctionRevertedError)) throw err;
      const next = await this.readNextHeartbeatAt().catch(() => undefined);
      if (next !== undefined && next > Math.floor(Date.now() / 1000)) {
        throw new HeartbeatRateLimitedError(next);
      }
      throw err;
    }
  }

  async isHeartbeatDue(): Promise<boolean> {
    const next = await this.readNextHeartbeatAt();
    return next <= Math.floor(Date.now() / 1000);
  }

  private async readNextHeartbeatAt(): Promise<number> {
    const state = await this.publicClient.readContract({
      address: this.contractAddress,
      abi: HEARTBEAT_ABI,
      functionName: 'getWorldState',
      args: [],
    });
    // viem decodes the named tuple into an object with the same field names.
    return Number((state as { nextHeartbeatAtTs: bigint }).nextHeartbeatAtTs);
  }
}

function normalizePk(pk: string): `0x${string}` {
  const trimmed = pk.trim();
  const withPrefix = trimmed.startsWith('0x') ? trimmed : `0x${trimmed}`;
  if (!/^0x[0-9a-fA-F]{64}$/.test(withPrefix)) {
    throw new Error(
      'RUNNER_PRIVATE_KEY is not a valid 64-hex-char private key (0x-prefixed optional)',
    );
  }
  return withPrefix as `0x${string}`;
}
    const nonMissionOrders = orders.filter(o => o.kind !== 'mission');
    if (nonMissionOrders.length > 0) {
      console.warn(`[RealChainClient] submitOrders: ${nonMissionOrders.length} non-mission order(s) skipped (Wave 0 only supports 'mission' kind)`);
    }

    const contractOrders = orders
      .filter(o => o.kind === 'mission')
      .map(o => ({
        clansmanId: Number(o.payload.clansmanId),
        gotoRegion: Number(o.payload.gotoRegion),
        action: Number(o.payload.action),
        targetClanId: 0,
        marketToken: '0x0000000000000000000000000000000000000000' as `0x${string}`,
        marketAmount: 0n,
        maxGoldIn: 0n,
      }));

    if (contractOrders.length === 0) {
      throw new Error('submitOrders: no valid mission orders to submit');
    }

    const keyPath = readEnv('ELDER_WALLET_KEY_PATH');
    let pk: string | undefined;
    let pkSource: string | undefined;
    if (keyPath) {
      try {
        pk = fs.readFileSync(keyPath, 'utf8').trim();
        pkSource = `ELDER_WALLET_KEY_PATH file at ${keyPath}`;
      } catch (err) {
        if (err && typeof err === 'object' && 'code' in err && err.code === 'ENOENT') {
          throw new Error(
            `ELDER_WALLET_KEY_PATH file not found at ${keyPath}; either set DEPLOYER_PRIVATE_KEY env var or provide a key file`,
          );
        }
        const msg = err instanceof Error ? err.message : String(err);
        throw new Error(`Failed to read ELDER_WALLET_KEY_PATH file at ${keyPath}: ${msg}`);
      }
    } else {
      const fallbackKey = readEnv('DEPLOYER_PRIVATE_KEY');
      if (fallbackKey) {
        console.warn('[RealChainClient] ELDER_WALLET_KEY_PATH not set; falling back to DEPLOYER_PRIVATE_KEY (deprecated)');
        pk = fallbackKey;
        pkSource = 'DEPLOYER_PRIVATE_KEY env var';
      }
    }
    if (!pk) throw new Error('Neither ELDER_WALLET_KEY_PATH nor DEPLOYER_PRIVATE_KEY is set');

    // Normalize: add 0x prefix if missing
    if (!pk.startsWith('0x')) pk = '0x' + pk;
    if (!/^0x[0-9a-fA-F]{64}$/.test(pk)) {
      throw new Error(
        `Invalid private key from ${pkSource ?? 'unknown source'}: expected a 64-hex-char private key (0x-prefixed optional)`,
      );
    }

    const account = privateKeyToAccount(pk as `0x${string}`);
    const walletClient = createWalletClient({
      account,
      chain: baseSepolia,
      transport: this.transport,
    });

    const hash = await walletClient.writeContract({
      address: this.contractAddress,
      abi: CLAN_WORLD_ABI,
      functionName: 'submitClanOrders',
      args: [parsedClanId, contractOrders],
    });

    return { txHash: hash };
  }

  async getClanFullView(clanId: string): Promise<ClanFullView> {
    const result = await this.client.readContract({
      address: this.contractAddress,
      abi: CLAN_WORLD_ABI,

exec
/bin/bash -lc "rg -n \"CLAN_WORLD_ABI|HEARTBEAT_ABI|getWorldSnapshot|getWorldState\" apps packages -g '*.ts' -g '*.tsx'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/runner/src/runnerCastHeartbeat.ts:19: * we read out of `getWorldState()`. We avoid pulling in the full IClanWorld
packages/runner/src/runnerCastHeartbeat.ts:22:const HEARTBEAT_ABI = [
packages/runner/src/runnerCastHeartbeat.ts:32:    name: 'getWorldState',
packages/runner/src/runnerCastHeartbeat.ts:99: * any revert we re-read `getWorldState().nextHeartbeatAtTs` and, if it is
packages/runner/src/runnerCastHeartbeat.ts:128:        abi: HEARTBEAT_ABI,
packages/runner/src/runnerCastHeartbeat.ts:167:      abi: HEARTBEAT_ABI,
packages/runner/src/runnerCastHeartbeat.ts:168:      functionName: 'getWorldState',
packages/shared/src/adapters/IChainClient.ts:25:const CLAN_WORLD_ABI = [
packages/shared/src/adapters/IChainClient.ts:28:    name: 'getWorldSnapshot',
packages/shared/src/adapters/IChainClient.ts:283:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:284:      functionName: 'getWorldSnapshot',
packages/shared/src/adapters/IChainClient.ts:369:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:380:      abi: CLAN_WORLD_ABI,

exec
/bin/bash -lc "rg -n \"StatusCode|ERR_CLAN_DEAD|ERR_CARRY_FULL|ERR_LIQUIDITY|ERR_WINTER_LOCKED|ERR_MUST_SETTLE_FIRST|status\" packages/shared apps packages/agents packages/orchestrator -g '*.ts' -g '*.tsx'" in /home/claude/code/clan-world
 exited 2 in 0ms:
rg: packages/orchestrator: No such file or directory (os error 2)
packages/agents/src/seams/IRunnerInbox.ts:18:   * - Must not throw on transient Elder session unavailability — surface via returned status.
apps/server/convex/http.ts:13:    status: 204,
apps/server/convex/verify.ts:40:      status: 400,
apps/server/convex/verify.ts:52:      status: 200,
apps/server/convex/verify.ts:73:      status: 400,
apps/server/convex/verify.ts:79:    status: 200,
packages/shared/src/adapters/IChainClient.ts:230:          { name: 'status', type: 'uint8' },
apps/server/convex/heartbeat.ts:7:      status: 405,
apps/server/convex/heartbeat.ts:16:      status: 401,
apps/server/convex/heartbeat.ts:24:    status: 200,
apps/server/convex/heartbeat.ts:30:  | { status: "ok"; tick: number }
apps/server/convex/heartbeat.ts:31:  | { status: "no-op"; reason: string };
apps/server/convex/heartbeat.ts:36:    if (!snap) return { status: "no-op", reason: "no snapshot to refresh" };
apps/server/convex/heartbeat.ts:46:      return { status: "no-op", reason: "epoch not yet elapsed" };
apps/server/convex/heartbeat.ts:63:    return { status: "ok", tick: newTick };
apps/web/tests/e2e/04-cockpit-shell.spec.ts:25:        status: 200,
apps/web/tests/e2e/04-cockpit-shell.spec.ts:102:    await expect(pill).toHaveAttribute('data-status', 'disconnected', {
apps/web/tests/e2e/04-cockpit-shell.spec.ts:115:    await expect(pill).toHaveAttribute('data-status', /reconnecting|disconnected/, {
apps/web/src/components/cockpit/CockpitHeader.tsx:24:  const { status, retry } = useConnectionStatus();
apps/web/src/components/cockpit/CockpitHeader.tsx:61:        <ConnectionPill status={status} onRetry={retry} />
apps/web/src/components/cockpit/CockpitHeader.tsx:122:  status: ConnectionStatus;
apps/web/src/components/cockpit/CockpitHeader.tsx:145: * Connection state pill. Shows current heartbeat status with a glyph + label,
apps/web/src/components/cockpit/CockpitHeader.tsx:148:function ConnectionPill({ status, onRetry }: PillProps) {
apps/web/src/components/cockpit/CockpitHeader.tsx:149:  const color = STATUS_COLORS[status];
apps/web/src/components/cockpit/CockpitHeader.tsx:153:      data-status={status}
apps/web/src/components/cockpit/CockpitHeader.tsx:167:      title={`Cockpit backend: ${STATUS_LABELS[status]}`}
apps/web/src/components/cockpit/CockpitHeader.tsx:176:          animation: status === 'reconnecting' ? 'cockpit-pill-spin 1.2s linear infinite' : undefined,
apps/web/src/components/cockpit/CockpitHeader.tsx:179:        {STATUS_GLYPHS[status]}
apps/web/src/components/cockpit/CockpitHeader.tsx:181:      <span style={{ color: tokens.text.onIron }}>{STATUS_LABELS[status]}</span>
apps/web/src/components/cockpit/CockpitHeader.tsx:182:      {status === 'disconnected' && (
apps/web/src/App.tsx:94:          console.error('Verify failed:', res.status);
apps/web/src/hooks/useConnectionStatus.ts:52: * through `status` (state) and `retry()` (a stable callback that bumps a
apps/web/src/hooks/useConnectionStatus.ts:56:  status: ConnectionStatus;
apps/web/src/hooks/useConnectionStatus.ts:64:  const [status, setStatus] = useState<ConnectionStatus>('reconnecting');
apps/web/src/hooks/useConnectionStatus.ts:70:  const statusRef = useRef<ConnectionStatus>(status);
apps/web/src/hooks/useConnectionStatus.ts:71:  statusRef.current = status;
apps/web/src/hooks/useConnectionStatus.ts:143:      if (statusRef.current === 'disconnected') return;
apps/web/src/hooks/useConnectionStatus.ts:157:  return { status, retry };

exec
/bin/bash -lc "rg -n \"MarketExecutionMode|OrderResult|submitClanOrders\" packages/shared apps packages/agents packages/orchestrator -g '*.ts' -g '*.tsx'" in /home/claude/code/clan-world
 exited 2 in 0ms:
rg: packages/orchestrator: No such file or directory (os error 2)
packages/shared/src/adapters/IChainClient.ts:206:    name: 'submitClanOrders',
packages/shared/src/adapters/IChainClient.ts:370:      functionName: 'submitClanOrders',

codex
The requested fixes mostly line up in the contract diff: starvation now uses `< tick`, wood burn is base plus per-clansman, winter events are `uint64`, wheat lock clears existing plots, and §7.3 is patched. The more interesting review risk is in the edges around newly derived ABI/view shapes and winter-time minting.
collab: Wait
codex
The upkeep subreview caught a real miss: the new `< tick` guard fixes “fresh winter starts starving” but not “already starving before winter starts.” I’m doing one last pass on ABI/view consumers and wheat-lock behavior, then I’ll synthesize cleanly with severity.
collab: Wait
exec
/bin/bash -lc "git show 1210e90:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '24,72p;155,185p;490,515p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
    65	    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
    66	    uint16 internal constant WINTER_UPKEEP_MULTIPLIER_BPS = 20000; // 2x
    67	    uint16 internal constant COLD_DAMAGE_PER_WALL_DEGRADATION = 2;
    68	    uint16 internal constant COLD_DAMAGE_PER_CLANSMAN_DEATH = 2;
    69	
    70	    // Wheat plots
    71	    uint64 internal constant WHEAT_PLOT_REGROW_TICKS = 4;
    72	    uint256 internal constant WHEAT_PLOT_STARTING_WHEAT = 100e18;
   155	enum StatusCode {
   156	    OK,
   157	    ERR_CLAN_DEAD,
   158	    ERR_CLAN_NOT_OWNED,
   159	    ERR_CLANSMAN_DEAD,
   160	    ERR_INVALID_CLANSMAN,
   161	    ERR_INVALID_REGION,
   162	    ERR_INVALID_ACTION,
   163	    ERR_INVALID_TARGET,
   164	    ERR_COOLDOWN_ACTIVE,
   165	    ERR_NOT_WAITING,
   166	    ERR_NOT_IN_UNICORN_TOWN,
   167	    ERR_NOT_AT_HOMEBASE,
   168	    ERR_NOT_AT_TARGET_BASE,
   169	    ERR_NOT_DEFENDABLE,
   170	    ERR_MISSING_RESOURCES,
   171	    ERR_EMPTY_CARGO,
   172	    ERR_PLOT_NOT_READY,
   173	    ERR_PLOT_EMPTY,
   174	    ERR_MARKET_ZERO_AMOUNT,
   175	    ERR_MARKET_UNSUPPORTED_TOKEN,
   176	    ERR_IMMEDIATE_MARKET_NOT_ELIGIBLE,
   177	    ERR_MARKET_BUY_OVER_CAPACITY,
   178	    ERR_MARKET_BUY_MAX_GOLD_EXCEEDED,
   179	    ERR_WORLD_TICK_MISMATCH,
   180	    ERR_NO_ACTIVE_BANDIT,
   181	    ERR_SEASON_ENDED,
   182	    ERR_NOT_ENOUGH_GOLD,
   183	    ERR_CARRY_FULL,
   184	    ERR_WINTER_LOCKED,
   185	    ERR_MUST_SETTLE_FIRST
   490	
   491	// =============================================================================
   492	// EVENTS
   493	// =============================================================================
   494	
   495	interface IClanWorldEvents {
   496	    // ----- world clock -----
   497	    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
   498	    event WinterStarted(uint64 indexed tick);
   499	    event WinterEnded(uint64 indexed tick);
   500	    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
   501	
   502	    // ----- clan lifecycle -----
   503	    event ClanSpawned(
   504	        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
   505	    );
   506	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   507	    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
   508	    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);
   509	    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
   510	    event ClanColdShortage(uint32 indexed clanId, uint32 tick, uint256 woodShort);
   511	    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint32 tick);
   512	    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint32 tick);
   513	
   514	    // ----- missions -----
   515	    event MissionAssigned(

exec
/bin/bash -lc 'git cat-file -e 1210e90''^{commit} && git show --stat --oneline --no-renames 1210e90 -- packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test/ClanWorld.t.sol' in /home/claude/code/clan-world
 succeeded in 0ms:
1210e90 fix(contracts): Phase 10 super-swarm r2 — starvation guard/timing + wood burn per-base + uint64 ABI revert + wheat plot clear (Closes #283-companion) (#287)
 packages/contracts/src/ClanWorld.sol    | 15 ++++---
 packages/contracts/src/IClanWorld.sol   |  5 ++-
 packages/contracts/test/ClanWorld.t.sol | 77 ++++++++++++++++++++++++++++-----
 3 files changed, 77 insertions(+), 20 deletions(-)

exec
/bin/bash -lc "git show 1210e90:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '420,475p;1110,1170p;1265,1295p;2010,2035p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   440	        if (winter && starving && clan.starvationStartsAtTick < tick) {
   441	            _killNextClansmanFromStarvation(clan, tick);
   442	        }
   443	        if (clan.clanState == ClanState.DEAD) return;
   444	
   445	        if (winter) {
   446	            uint256 woodNeeded = ClanWorldConstants.WINTER_WOOD_BURN_PER_BASE
   447	                + uint256(clan.livingClansmen) * ClanWorldConstants.WINTER_WOOD_BURN_PER_CLANSMAN;
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
  1138	                WheatPlot storage plot = _wheatPlots[clanId][pi];
  1139	                plot.state = WheatPlotState.WinterLocked;
  1140	                plot.remainingWheat = 0;
  1141	                plot.regrowUntilTick = 0;
  1142	                transitions++;
  1143	            }
  1144	        }
  1145	    }
  1146	
  1147	    function _restartWheatPlotsAfterWinter(uint64 currentTick) internal {
  1148	        uint256 transitions;
  1149	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1150	            uint32 clanId = _allClanIds[i];
  1151	            for (uint256 pi = 0; pi < 2; pi++) {
  1152	                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
  1153	                WheatPlot storage plot = _wheatPlots[clanId][pi];
  1154	                if (plot.state == WheatPlotState.WinterLocked) {
  1155	                    plot.state = WheatPlotState.Regrowing;
  1156	                    plot.remainingWheat = 0;
  1157	                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
  1158	                }
  1159	                transitions++;
  1160	            }
  1161	        }
  1162	    }
  1163	
  1164	    function _resetColdDamageForAllClans() internal {
  1165	        for (uint256 i = 0; i < _allClanIds.length; i++) {
  1166	            _clans[_allClanIds[i]].coldDamage = 0;
  1167	        }
  1168	    }
  1169	
  1170	    function _winterEventTick(uint64 tick) internal pure returns (uint64) {
  1265	        clan.lastSettledTick = _world.currentTick;
  1266	        clan.starvationStartsAtTick = 0;
  1267	        clan.coldDamage = 0;
  1268	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1269	        clan.blueprintBalance = 0;
  1270	        // Starting vault per v4 spec §12.4
  1271	        clan.vaultWood = 20e18;
  1272	        clan.vaultIron = 0;
  1273	        clan.vaultWheat = 20e18;
  1274	        clan.vaultFish = 2e18;
  1275	
  1276	        WheatPlotState startingPlotState =
  1277	            _isWinterActiveAt(_world.currentTick) ? WheatPlotState.WinterLocked : WheatPlotState.Harvestable;
  1278	
  1279	        // Wheat plots
  1280	        _wheatPlots[clanId][0] = WheatPlot({
  1281	            state: startingPlotState,
  1282	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1283	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1284	            regrowUntilTick: 0
  1285	        });
  1286	        _wheatPlots[clanId][1] = WheatPlot({
  1287	            state: startingPlotState,
  1288	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1289	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1290	            regrowUntilTick: 0
  1291	        });
  1292	
  1293	        // Create 4 clansmen
  1294	        for (uint256 i = 0; i < 4; i++) {
  1295	            uint32 csId = _nextClansmanId++;
  2010	    // RAW READ GETTERS
  2011	    // =========================================================================
  2012	
  2013	    function getWorldState() external view override returns (WorldState memory) {
  2014	        return _worldStateView();
  2015	    }
  2016	
  2017	    function getTreasuryState() external view override returns (TreasuryState memory) {
  2018	        return _treasury;
  2019	    }
  2020	
  2021	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  2022	        return _clans[clanId];
  2023	    }
  2024	
  2025	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  2026	        return _clansmen[clansmanId];
  2027	    }
  2028	
  2029	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  2030	        return _missions[clansmanId];
  2031	    }
  2032	
  2033	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  2034	        external
  2035	        view

exec
/bin/bash -lc "git show 1210e90:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '25,58p;275,290p' && git show 1210e90:packages/runner/src/runnerCastHeartbeat.ts | nl -ba | sed -n '28,54p;162,174p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
   162	  }
   163	
   164	  private async readNextHeartbeatAt(): Promise<number> {
   165	    const state = await this.publicClient.readContract({
   166	      address: this.contractAddress,
   167	      abi: HEARTBEAT_ABI,
   168	      functionName: 'getWorldState',
   169	      args: [],
   170	    });
   171	    // viem decodes the named tuple into an object with the same field names.
   172	    return Number((state as { nextHeartbeatAtTs: bigint }).nextHeartbeatAtTs);
   173	  }
   174	}

exec
/bin/bash -lc "git show 1210e90:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '190,214p;414,430p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   190	// =============================================================================
   191	
   192	struct WorldState {
   193	    uint64 currentTick;
   194	    uint64 seasonStartTick;
   195	    uint64 seasonEndTick;
   196	    bool seasonFinalized;
   197	    uint64 currentSeasonNumber; // 1-indexed; incremented each time seasonEndTick is crossed
   198	    uint64 nextHeartbeatAtTick; // estimated tick that will be opened by the next heartbeat (for off-chain UI)
   199	
   200	    uint64 nextHeartbeatAtTs;
   201	    uint64 nextBanditSpawnEligibleTick;
   202	    uint16 currentBanditSpawnChanceBps;
   203	    bytes32 currentTickSeed;
   204	
   205	    uint32 activeBanditId; // 0 if none
   206	    bool winterActive;
   207	    uint64 winterStartsAtTick;
   208	    uint64 winterEndsAtTick; // 0 if not active
   209	
   210	    uint64 nextCommitSequence; // global FIFO sequence for scheduled market actions
   211	}
   212	
   213	struct TreasuryState {
   214	    address treasuryOwner;
   414	struct WorldSnapshot {
   415	    uint64 currentTick;
   416	    uint64 seasonStartTick;
   417	    uint64 seasonEndTick;
   418	    bool seasonFinalized;
   419	    uint64 currentSeasonNumber;
   420	    uint64 nextHeartbeatAtTick;
   421	    bool winterActive;
   422	    uint64 winterStartsAtTick;
   423	    uint64 winterEndsAtTick;
   424	    uint32 activeBanditId;
   425	    bytes32 currentTickSeed;
   426	
   427	    LeaderboardEntry[] leaderboard;
   428	}
   429	
   430	struct ClansmanFullView {

exec
/bin/bash -lc "git show 1210e90:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1320,1355p;1888,1900p;2230,2245p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1320	        nonReentrant
  1321	        returns (OrderResult[] memory results)
  1322	    {
  1323	        Clan storage clan = _clans[clanId];
  1324	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1325	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1326	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1327	
  1328	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1329	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1330	        {
  1331	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1332	            if (_world.currentTick > lastSettled + 200) {
  1333	                results = new OrderResult[](orders.length);
  1334	                for (uint256 i = 0; i < orders.length; i++) {
  1335	                    results[i] = OrderResult({
  1336	                        clansmanId: orders[i].clansmanId,
  1337	                        status: StatusCode.ERR_MUST_SETTLE_FIRST,
  1338	                        cooldownEndsAtTs: 0,
  1339	                        missionNonce: 0
  1340	                    });
  1341	                }
  1342	                return results;
  1343	            }
  1344	        }
  1345	
  1346	        // Lazy settle before processing orders
  1347	        _settleClan(clanId);
  1348	
  1349	        results = new OrderResult[](orders.length);
  1350	        if (clan.clanState == ClanState.DEAD) {
  1351	            for (uint256 i = 0; i < orders.length; i++) {
  1352	                results[i] = OrderResult({
  1353	                    clansmanId: orders[i].clansmanId,
  1354	                    status: StatusCode.ERR_CLAN_DEAD,
  1355	                    cooldownEndsAtTs: 0,
  1888	        }
  1889	        // HarvestWheat: must go to WestFarms or EastFarms
  1890	        if (action == ActionType.HarvestWheat) {
  1891	            if (
  1892	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  1893	            ) {
  1894	                return StatusCode.ERR_INVALID_REGION;
  1895	            }
  1896	            if (_isWinterActiveAt(_world.currentTick)) {
  1897	                return StatusCode.ERR_WINTER_LOCKED;
  1898	            }
  1899	        }
  1900	
  2230	            });
  2231	        }
  2232	
  2233	        WorldState memory ws = _worldStateView();
  2234	        return WorldSnapshot({
  2235	            currentTick: ws.currentTick,
  2236	            seasonStartTick: ws.seasonStartTick,
  2237	            seasonEndTick: ws.seasonEndTick,
  2238	            seasonFinalized: ws.seasonFinalized,
  2239	            currentSeasonNumber: ws.currentSeasonNumber,
  2240	            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
  2241	            winterActive: ws.winterActive,
  2242	            winterStartsAtTick: ws.winterStartsAtTick,
  2243	            winterEndsAtTick: ws.winterEndsAtTick,
  2244	            activeBanditId: ws.activeBanditId,
  2245	            currentTickSeed: ws.currentTickSeed,

exec
/bin/bash -lc "git show 1210e90:docs/planning/clanworld_v4_spec.md | nl -ba | sed -n '831,836p' && git show 1210e90:packages/contracts/abi/IClanWorld.json | nl -ba | sed -n '3768,3788p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   831	## 7.3 Winter upkeep
   832	During winter, each clan consumes:
   833	- wheat consumption = `2×` normal
   834	- fish consumption = `2×` normal
   835	- wood burn = `1e18` wood per base per tick, plus `0.5e18` wood per living clansman per tick
   836	
  3768	          "indexed": true,
  3769	          "internalType": "uint64"
  3770	        }
  3771	      ],
  3772	      "anonymous": false
  3773	    },
  3774	    {
  3775	      "type": "event",
  3776	      "name": "WorkerArrived",
  3777	      "inputs": [
  3778	        {
  3779	          "name": "clanId",
  3780	          "type": "uint32",
  3781	          "indexed": true,
  3782	          "internalType": "uint32"
  3783	        },
  3784	        {
  3785	          "name": "clansmanId",
  3786	          "type": "uint32",
  3787	          "indexed": true,
  3788	          "internalType": "uint32"

codex
# Phase Super-Swarm Review (R2) — PR #250 (head 1210e90)

## SUMMARY

Not clean. Three requested fixes verify cleanly: wood formula for a non-starving 4-clansman clan is `3e18`, winter events are back to `uint64`, existing wheat plots lock with `remainingWheat = 0` and `regrowUntilTick = 0`, and spec §7.3 is patched.

Two HIGHs remain: starvation timing still differs for pre-winter starvers, and hardcoded TS ABIs are now out of sync with `WorldState` / `WorldSnapshot`.

## HIGH severity findings

1. **Pre-winter starvers still die one tick earlier than fresh winter starvers**

`packages/contracts/src/ClanWorld.sol@1210e90:431-441`

The guard now uses:

```solidity
if (winter && starving && clan.starvationStartsAtTick < tick)
```

That defers death for a clan that starts starving on the first winter tick, but not for a clan already starving before winter. Example: winter starts at tick `110`; a pre-winter starver with `starvationStartsAtTick = 105` dies on tick `110`, while a fresh winter starver with `starvationStartsAtTick = 110` first dies on tick `111`.

The added test misses this because it sets both clans’ `lastSettledTick` after winter has already begun. The comparison needs an effective winter starvation start, e.g. `max(starvationStartsAtTick, currentWinterStartTick)`.

2. **Hardcoded TS ABIs are stale after `WorldState` / `WorldSnapshot` layout additions**

`packages/contracts/src/IClanWorld.sol@1210e90:192` and `:414` include `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields. But:

`packages/runner/src/runnerCastHeartbeat.ts@1210e90:38-51` omits them from `getWorldState`, so `readNextHeartbeatAt()` decodes `currentSeasonNumber` as `nextHeartbeatAtTs`.

`packages/shared/src/adapters/IChainClient.ts@1210e90:34-44` omits them from `getWorldSnapshot`, so viem decodes later fields at wrong offsets; the dynamic `leaderboard` offset is especially likely to fail decoding.

Impact: heartbeat due checks and real chain reads can break or misbehave even though the generated ABI JSON is correct.

## MEDIUM severity findings

1. **Wood burn uses post-starvation living count on the same tick**

`packages/contracts/src/ClanWorld.sol@1210e90:440-447`

If starvation kills a clansman, wood burn is calculated afterward from the reduced `livingClansmen`. A 4-clansman clan that loses one to starvation that tick burns `2.5e18`, not `3e18`. The requested non-starving case passes, but the same-tick ordering is a fairness wrinkle.

2. **Clans minted during winter start `WinterLocked` with nonzero wheat**

`packages/contracts/src/ClanWorld.sol@1210e90:1276-1290`

Winter-minted plots get `state = WinterLocked`, but still initialize `remainingWheat = WHEAT_PLOT_STARTING_WHEAT`. Harvesting is blocked, so this is mostly a state/indexer invariant bug, but it violates the winter-lock triple expected for plots.

## LOW severity findings

CLEAN — no findings.

## Cross-cutting observations

- `WinterStarted` / `WinterEnded` are correctly `uint64` in `IClanWorld.sol@1210e90:497-499`.
- Existing wheat plot winter lock correctly sets `WinterLocked`, `remainingWheat = 0`, and `regrowUntilTick = 0` at `ClanWorld.sol@1210e90:1138-1141`.
- Spec §7.3 now documents per-base plus per-clansman wood burn at `docs/planning/clanworld_v4_spec.md@1210e90:831-835`.
- H1 cold RNG seed was intentionally parked, so I did not count it as a finding.
- I did not run forge tests; this was a read-only review pass with parallel local inspection plus two subagent reviews.
2026-04-29T23:10:23.734489Z ERROR codex_core::session: failed to record rollout items: thread 019ddb7e-6ff0-7f92-8cf8-1c691778c501 not found
2026-04-29T23:10:23.734548Z ERROR codex_core::session: failed to record rollout items: thread 019ddb7e-7098-77b2-9b27-681c725b81a4 not found
2026-04-29T23:10:23.734757Z ERROR codex_core::session: failed to record rollout items: thread 019ddb7d-f252-7301-b23b-26009debd9f5 not found
tokens used
233,586
# Phase Super-Swarm Review (R2) — PR #250 (head 1210e90)

## SUMMARY

Not clean. Three requested fixes verify cleanly: wood formula for a non-starving 4-clansman clan is `3e18`, winter events are back to `uint64`, existing wheat plots lock with `remainingWheat = 0` and `regrowUntilTick = 0`, and spec §7.3 is patched.

Two HIGHs remain: starvation timing still differs for pre-winter starvers, and hardcoded TS ABIs are now out of sync with `WorldState` / `WorldSnapshot`.

## HIGH severity findings

1. **Pre-winter starvers still die one tick earlier than fresh winter starvers**

`packages/contracts/src/ClanWorld.sol@1210e90:431-441`

The guard now uses:

```solidity
if (winter && starving && clan.starvationStartsAtTick < tick)
```

That defers death for a clan that starts starving on the first winter tick, but not for a clan already starving before winter. Example: winter starts at tick `110`; a pre-winter starver with `starvationStartsAtTick = 105` dies on tick `110`, while a fresh winter starver with `starvationStartsAtTick = 110` first dies on tick `111`.

The added test misses this because it sets both clans’ `lastSettledTick` after winter has already begun. The comparison needs an effective winter starvation start, e.g. `max(starvationStartsAtTick, currentWinterStartTick)`.

2. **Hardcoded TS ABIs are stale after `WorldState` / `WorldSnapshot` layout additions**

`packages/contracts/src/IClanWorld.sol@1210e90:192` and `:414` include `currentSeasonNumber` and `nextHeartbeatAtTick` before winter fields. But:

`packages/runner/src/runnerCastHeartbeat.ts@1210e90:38-51` omits them from `getWorldState`, so `readNextHeartbeatAt()` decodes `currentSeasonNumber` as `nextHeartbeatAtTs`.

`packages/shared/src/adapters/IChainClient.ts@1210e90:34-44` omits them from `getWorldSnapshot`, so viem decodes later fields at wrong offsets; the dynamic `leaderboard` offset is especially likely to fail decoding.

Impact: heartbeat due checks and real chain reads can break or misbehave even though the generated ABI JSON is correct.

## MEDIUM severity findings

1. **Wood burn uses post-starvation living count on the same tick**

`packages/contracts/src/ClanWorld.sol@1210e90:440-447`

If starvation kills a clansman, wood burn is calculated afterward from the reduced `livingClansmen`. A 4-clansman clan that loses one to starvation that tick burns `2.5e18`, not `3e18`. The requested non-starving case passes, but the same-tick ordering is a fairness wrinkle.

2. **Clans minted during winter start `WinterLocked` with nonzero wheat**

`packages/contracts/src/ClanWorld.sol@1210e90:1276-1290`

Winter-minted plots get `state = WinterLocked`, but still initialize `remainingWheat = WHEAT_PLOT_STARTING_WHEAT`. Harvesting is blocked, so this is mostly a state/indexer invariant bug, but it violates the winter-lock triple expected for plots.

## LOW severity findings

CLEAN — no findings.

## Cross-cutting observations

- `WinterStarted` / `WinterEnded` are correctly `uint64` in `IClanWorld.sol@1210e90:497-499`.
- Existing wheat plot winter lock correctly sets `WinterLocked`, `remainingWheat = 0`, and `regrowUntilTick = 0` at `ClanWorld.sol@1210e90:1138-1141`.
- Spec §7.3 now documents per-base plus per-clansman wood burn at `docs/planning/clanworld_v4_spec.md@1210e90:831-835`.
- H1 cold RNG seed was intentionally parked, so I did not count it as a finding.
- I did not run forge tests; this was a read-only review pass with parallel local inspection plus two subagent reviews.
