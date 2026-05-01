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
session id: 019ddc3f-fa96-7883-a9bb-72ac1f27f8c9
--------
user
Read prompt+diff from stdin. Use parallel tool calls + subagents. Output full review in requested format.

<stdin>
You are a senior staff engineer doing the FINAL pre-merge code review for a multi-issue phase release PR.

PR: Phase 5 — Gathering / Deposits / Economy
Head SHA: 9ccf94a

## Your task

Cohesive phase review. Look for:
1. CROSS-CUTTING bugs at integration seams
2. ARCHITECTURAL drift — does the phase deliver its stated goal?
3. SECURITY surface
4. DATA-flow correctness — schema consistency, idempotency, error paths
5. Integration risks
6. Missing test coverage
7. Phase 5 specifically: gathering / deposits / economy — verify resource accounting, deposit-event semantics, gathering yield + crit logic, anti-cheat on resource amounts

USE PARALLEL TOOL CALLS / SUB-AGENTS aggressively.

## Output format

# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY
2-4 sentences: verdict (CLEAN | NEEDS_FIXES), top concerns, merge recommendation.

## HIGH severity findings
## MEDIUM severity findings
## LOW severity findings
## Cross-cutting observations

If clean, say "CLEAN — no findings" under each section.

DIFF FOLLOWS BELOW.
---
---
diff --git a/packages/contracts/src/ClanWorld.sol b/packages/contracts/src/ClanWorld.sol
index 945490b..2c5fc0a 100644
--- a/packages/contracts/src/ClanWorld.sol
+++ b/packages/contracts/src/ClanWorld.sol
@@ -74,6 +74,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
     // CONSTANTS — Wheat harvest rate (not in IClanWorld constants)
     // =========================================================================
 
+    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
     uint256 private constant WHEAT_HARVEST_RATE = 20e18;
     /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
     uint256 public constant MAX_MARKET_ACTIONS_PER_TICK = 32;
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
@@ -484,28 +487,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         bool starving,
         bytes32 tickSeed
     ) internal {
-        uint256 remaining = ClanWorldConstants.WOOD_CAP - cs.carryWood;
-        if (remaining == 0) {
+        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
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
+        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
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
+        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
             _completeMission(cs, m);
         }
-        // else continuous — worker stays ACTING
     }
 
     function _gatherIron(
@@ -665,7 +667,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
         // else continuous
     }
 
-    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
+    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
         internal
     {
         // Must be at homebase region
@@ -675,26 +677,27 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
 
@@ -1610,7 +1613,7 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
 
         // DepositResources: must go to homebase
         if (action == ActionType.DepositResources) {
-            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
+            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
         }
 
         // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
@@ -1801,10 +1804,13 @@ contract ClanWorld is IClanWorld, ReentrancyGuard {
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
diff --git a/packages/contracts/src/IClanWorld.sol b/packages/contracts/src/IClanWorld.sol
index 2b80fbe..8a8ca97 100644
--- a/packages/contracts/src/IClanWorld.sol
+++ b/packages/contracts/src/IClanWorld.sol
@@ -40,15 +40,17 @@ library ClanWorldConstants {
     uint64 internal constant CLANSMAN_COOLDOWN_SECONDS = 60;
 
     // Carry caps (per clansman)
-    uint256 internal constant WOOD_CAP = 15e18;
+    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
+    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
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
@@ -533,11 +535,11 @@ interface IClanWorldEvents {
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
diff --git a/packages/contracts/test/ClanWorld.t.sol b/packages/contracts/test/ClanWorld.t.sol
index 92781d3..21199ec 100644
--- a/packages/contracts/test/ClanWorld.t.sol
+++ b/packages/contracts/test/ClanWorld.t.sol
@@ -48,6 +48,13 @@ contract ClanWorldTestHarness is ClanWorld {
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
 }
 
 contract ClanWorldTest is Test {
@@ -447,7 +454,7 @@ contract ClanWorldTest is Test {
         vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
         _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
 
-        // Advance travel back to homebase + deposit duration.
+        // Advance through travel back to homebase and the deposit's 1-tick transfer.
         (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
         for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
             _advanceTick();
@@ -465,6 +472,105 @@ contract ClanWorldTest is Test {
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
@@ -1293,15 +1399,14 @@ contract ClanWorldTest is Test {
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
 
diff --git a/packages/contracts/test/Gathering.t.sol b/packages/contracts/test/Gathering.t.sol
new file mode 100644
index 0000000..4f64396
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
+        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);
+
+        Clansman memory cs = _settleChopWood(clanId, csId);
+
+        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");
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
</stdin>
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
web search: 
web search: 
codex
Reviewing this phase as a cohesive pre-merge pass now; I’ll parallelize checks across contracts, tests, and downstream consumers (indexer/schema) and then return a severity-ranked final verdict in the exact format you requested.
web search: 
web search: 
exec
/bin/bash -lc "rg -n \"ResourcesDeposited\\(|WOOD_YIELD_PER_TICK|CLANSMAN_CARRY_CAP|DepositResources|ERR_INVALID_REGION|ERR_NOT_AT_HOMEBASE\" packages -S" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:454:        } else if (action == ActionType.DepositResources) {
packages/contracts/src/ClanWorld.sol:697:        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
packages/contracts/src/ClanWorld.sol:1194:            result.status = StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1611:        // DepositResources: must go to homebase
packages/contracts/src/ClanWorld.sol:1612:        if (action == ActionType.DepositResources) {
packages/contracts/src/ClanWorld.sol:1613:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
packages/contracts/src/ClanWorld.sol:1619:            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
packages/contracts/src/ClanWorld.sol:1624:            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1628:            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1635:                return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1640:            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1647:                return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1659:                return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1689:        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
packages/contracts/src/ClanWorld.sol:1805:            action == ActionType.DepositResources || action == ActionType.BuildWall || action == ActionType.UpgradeBase
packages/contracts/src/IClanWorld.sol:135:    DepositResources,
packages/contracts/src/IClanWorld.sol:157:    ERR_INVALID_REGION,
packages/contracts/src/IClanWorld.sol:163:    ERR_NOT_AT_HOMEBASE,
packages/contracts/src/IClanWorld.sol:533:    event ResourcesDeposited(
packages/contracts/test/MissionTiming.t.sol:128:        assertEq(world.getActionDuration(ActionType.DepositResources), 1, "deposit");
packages/contracts/test/DefendBase.t.sol:180:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "non-home defend rejected");
packages/contracts/test/HeartbeatOrdering.t.sol:194:        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
packages/contracts/test/HeartbeatOrdering.t.sol:321:        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:448:        _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:452:        for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
packages/contracts/test/ClanWorld.t.sol:1095:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "market sell to Forest should fail");
packages/contracts/test/ClanWorld.t.sol:1296:        // Send clansman to homebase to DepositResources (empty carry → completes in 1 tick).
packages/contracts/test/ClanWorld.t.sol:1298:        OrderResult[] memory r = _submitOrder(clanId, csId, homeRegion, ActionType.DepositResources);
packages/contracts/test/ClanWorld.t.sol:1419:    // 3.E6: ERR_INVALID_REGION — wrong region for action type
packages/contracts/test/ClanWorld.t.sol:1430:        // ChopWood to Mountains (not Forest) → ERR_INVALID_REGION
packages/contracts/test/ClanWorld.t.sol:1433:            uint8(r0[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: ChopWood to Mountains must be invalid"
packages/contracts/test/ClanWorld.t.sol:1436:        // MineIron to Forest (not Mountains) → ERR_INVALID_REGION
packages/contracts/test/ClanWorld.t.sol:1438:        assertEq(uint8(r1[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: MineIron to Forest must be invalid");
packages/contracts/test/ClanWorld.t.sol:1440:        // FishDocks to Forest (not WestDocks or EastDocks) → ERR_INVALID_REGION
packages/contracts/test/ClanWorld.t.sol:1442:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: FishDocks to Forest must be invalid");
packages/contracts/test/ClanWorld.t.sol:1910:            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"

exec
/bin/bash -lc 'git rev-parse --short HEAD && git status --short' in /home/claude/code/clan-world
 succeeded in 0ms:
6cf6f39
?? docs/planning/gold-bridge-monorepo.zip
?? docs/reviews/pr153-review-claude-opus.md
?? docs/reviews/pr153-review-codex-5-3.md
?? docs/reviews/pr193-codereview-codex-5-3.md
?? docs/reviews/pr193-codereview-codex-5-4.md
?? docs/reviews/pr193-codereview-codex-5-5.md
?? docs/reviews/pr193-codereview-gemini-3-1-pro.md
?? docs/reviews/pr193-codereview-opus-4-6.md
?? docs/reviews/pr193-codereview-opus-4-7.md
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
?? docs/reviews/pr200-r2-synthesis.md
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
?? docs/reviews/pr250-r3-synthesis.md
?? docs/reviews/r2-phase10/
?? docs/reviews/r2-phase6/
?? docs/reviews/r2/

exec
/bin/bash -lc 'git show --stat --oneline 9ccf94a' in /home/claude/code/clan-world
 succeeded in 0ms:
9ccf94a fix(contracts): Phase 5 — ResourcesDeposited event order + tick + 4 MED (gemini-CA r1 on #193) (Closes #201) (#234)
 packages/contracts/src/ClanWorld.sol    | 19 +++++++++----------
 packages/contracts/src/IClanWorld.sol   |  5 +++--
 packages/contracts/test/ClanWorld.t.sol |  6 +++---
 packages/contracts/test/Gathering.t.sol |  6 ++++--
 4 files changed, 19 insertions(+), 17 deletions(-)

codex
I confirmed the referenced commit exists (`9ccf94a`), and I’m now inspecting surrounding contract logic at that exact SHA (not current workspace state) to catch seam-level regressions that a pure patch read can miss.
exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1560,1865p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1560	    function _executeMarketBuy(
  1561	        uint64 closedTick,
  1562	        uint32 clanId,
  1563	        uint32 clansmanId,
  1564	        address token,
  1565	        uint256 amountOut,
  1566	        uint256 maxGoldIn,
  1567	        uint64 commitSequence
  1568	    ) internal {
  1569	        if (!_treasury.poolsSeeded) {
  1570	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1571	            return;
  1572	        }
  1573	        address poolAddr = _poolFor(token);
  1574	        if (poolAddr == address(0)) {
  1575	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1576	            return;
  1577	        }
  1578	
  1579	        // Quote gold cost without updating reserves
  1580	        uint256 goldIn = StubPool(poolAddr).quoteBuy(amountOut);
  1581	
  1582	        Clan storage clan = _clans[clanId];
  1583	
  1584	        if (goldIn > maxGoldIn) {
  1585	            emit MarketActionFailed(
  1586	                clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_MARKET_BUY_MAX_GOLD_EXCEEDED
  1587	            );
  1588	            return;
  1589	        }
  1590	        if (clan.goldBalance < goldIn) {
  1591	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
  1592	            return;
  1593	        }
  1594	
  1595	        // Execute — use return value to guard against any future pool divergence
  1596	        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
  1597	        clan.goldBalance -= actualGoldIn;
  1598	        _addToVault(clan, token, amountOut);
  1599	
  1600	        emit ScheduledMarketActionExecuted(
  1601	            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
  1602	        );
  1603	    }
  1604	
  1605	    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
  1606	        internal
  1607	        view
  1608	        returns (StatusCode)
  1609	    {
  1610	        ActionType action = order.action;
  1611	
  1612	        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;
  1613	
  1614	        // DepositResources: must go to homebase
  1615	        if (action == ActionType.DepositResources) {
  1616	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1617	        }
  1618	
  1619	        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
  1620	        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
  1621	        {
  1622	            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_NOT_AT_HOMEBASE;
  1623	        }
  1624	
  1625	        // ChopWood: must go to Forest
  1626	        if (action == ActionType.ChopWood) {
  1627	            if (gotoRegion != ClanWorldConstants.REGION_FOREST) return StatusCode.ERR_INVALID_REGION;
  1628	        }
  1629	        // MineIron: must go to Mountains
  1630	        if (action == ActionType.MineIron) {
  1631	            if (gotoRegion != ClanWorldConstants.REGION_MOUNTAINS) return StatusCode.ERR_INVALID_REGION;
  1632	        }
  1633	        // FishDocks: must go to WestDocks or EastDocks
  1634	        if (action == ActionType.FishDocks) {
  1635	            if (
  1636	                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
  1637	            ) {
  1638	                return StatusCode.ERR_INVALID_REGION;
  1639	            }
  1640	        }
  1641	        // FishDeepSea: must go to DeepSea
  1642	        if (action == ActionType.FishDeepSea) {
  1643	            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
  1644	        }
  1645	        // HarvestWheat: must go to WestFarms or EastFarms
  1646	        if (action == ActionType.HarvestWheat) {
  1647	            if (
  1648	                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
  1649	            ) {
  1650	                return StatusCode.ERR_INVALID_REGION;
  1651	            }
  1652	        }
  1653	
  1654	        if (action == ActionType.DefendBase) {
  1655	            return _validateDefendBaseOrder(clan, order, gotoRegion);
  1656	        }
  1657	
  1658	        // MarketBuy/MarketSell: must target Unicorn Town
  1659	        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
  1660	            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1661	            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
  1662	                return StatusCode.ERR_INVALID_REGION;
  1663	            }
  1664	            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
  1665	            // Validate token is a supported resource token (not gold itself)
  1666	            address tok = order.marketToken;
  1667	            if (tok == address(0) || tok == _treasury.goldToken) {
  1668	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1669	            }
  1670	            if (
  1671	                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
  1672	                    && tok != _treasury.fishToken
  1673	            ) {
  1674	                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
  1675	            }
  1676	            // Market orders are always enqueued for the arrivalTick FIFO queue.
  1677	            // _resolveAction records mission completion but does not execute any swap.
  1678	            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
  1679	                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
  1680	            }
  1681	        }
  1682	
  1683	        cs; // suppress unused warning
  1684	        return StatusCode.OK;
  1685	    }
  1686	
  1687	    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
  1688	        internal
  1689	        view
  1690	        returns (StatusCode)
  1691	    {
  1692	        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
  1693	        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
  1694	        return StatusCode.OK;
  1695	    }
  1696	
  1697	    // =========================================================================
  1698	    // TREASURY / POOL SEEDING
  1699	    // =========================================================================
  1700	
  1701	    /// @notice One-time treasury initialization: register token and pool addresses.
  1702	    ///         Must be called before seedPools. Callable only once.
  1703	    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
  1704	        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
  1705	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1706	
  1707	        _treasury.woodToken = tokens[0];
  1708	        _treasury.ironToken = tokens[1];
  1709	        _treasury.wheatToken = tokens[2];
  1710	        _treasury.fishToken = tokens[3];
  1711	        _treasury.goldToken = tokens[4];
  1712	        _treasury.blueprintToken = tokens[5];
  1713	
  1714	        _treasury.woodGoldPool = pools[0];
  1715	        _treasury.wheatGoldPool = pools[1];
  1716	        _treasury.fishGoldPool = pools[2];
  1717	        _treasury.ironGoldPool = pools[3];
  1718	    }
  1719	
  1720	    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
  1721	    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
  1722	        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
  1723	        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
  1724	        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");
  1725	
  1726	        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
  1727	        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
  1728	        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
  1729	        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);
  1730	
  1731	        _treasury.poolsSeeded = true;
  1732	
  1733	        emit PoolsSeeded(
  1734	            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
  1735	        );
  1736	    }
  1737	
  1738	    // =========================================================================
  1739	    // OTC TRANSFERS
  1740	    // =========================================================================
  1741	
  1742	    function transferGold(uint32, uint32, uint256) external pure override {
  1743	        revert("OTC transfers not implemented");
  1744	    }
  1745	
  1746	    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
  1747	        revert("OTC transfers not implemented");
  1748	    }
  1749	
  1750	    function transferBlueprint(uint32, uint32, uint256) external pure override {
  1751	        revert("OTC transfers not implemented");
  1752	    }
  1753	
  1754	    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
  1755	        external
  1756	        pure
  1757	        override
  1758	    {
  1759	        revert("OTC transfers not implemented");
  1760	    }
  1761	
  1762	    // =========================================================================
  1763	    // RAW READ GETTERS
  1764	    // =========================================================================
  1765	
  1766	    function getWorldState() external view override returns (WorldState memory) {
  1767	        return _world;
  1768	    }
  1769	
  1770	    function getTreasuryState() external view override returns (TreasuryState memory) {
  1771	        return _treasury;
  1772	    }
  1773	
  1774	    function getClan(uint32 clanId) external view override returns (Clan memory) {
  1775	        return _clans[clanId];
  1776	    }
  1777	
  1778	    function getClansman(uint32 clansmanId) external view override returns (Clansman memory) {
  1779	        return _clansmen[clansmanId];
  1780	    }
  1781	
  1782	    function getActiveMission(uint32 clansmanId) external view override returns (Mission memory) {
  1783	        return _missions[clansmanId];
  1784	    }
  1785	
  1786	    function getMissionTiming(uint32 clanId, uint32 clansmanId)
  1787	        external
  1788	        view
  1789	        override
  1790	        returns (uint64 submitted, uint64 executes, uint64 settles)
  1791	    {
  1792	        Mission memory m = _missions[clansmanId];
  1793	        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
  1794	            return (0, 0, 0);
  1795	        }
  1796	        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
  1797	    }
  1798	
  1799	    function getActionDuration(ActionType action) public pure override returns (uint64) {
  1800	        if (
  1801	            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
  1802	                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
  1803	        ) {
  1804	            return 4;
  1805	        }
  1806	
  1807	        if (action == ActionType.DepositResources) {
  1808	            return DEPOSIT_DURATION_TICKS;
  1809	        }
  1810	
  1811	        if (
  1812	            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
  1813	                || action == ActionType.MarketBuy || action == ActionType.MarketSell
  1814	        ) {
  1815	            return 1;
  1816	        }
  1817	
  1818	        return 0;
  1819	    }
  1820	
  1821	    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
  1822	        return uint64(_travelTicks(fromRegion, toRegion));
  1823	    }
  1824	
  1825	    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {
  1826	        return BanditTroop({
  1827	            banditId: 0,
  1828	            state: BanditState.NONE,
  1829	            currentRegion: 0,
  1830	            attackAttemptsMade: 0,
  1831	            stateEnteredTick: 0,
  1832	            nextActionTick: 0,
  1833	            tier: 0,
  1834	            attackPower: 0,
  1835	            carryWood: 0,
  1836	            carryIron: 0,
  1837	            carryWheat: 0,
  1838	            carryFish: 0
  1839	        });
  1840	    }
  1841	
  1842	    function getWheatPlots(uint32 clanId)
  1843	        external
  1844	        view
  1845	        override
  1846	        returns (WheatPlot memory west, WheatPlot memory east)
  1847	    {
  1848	        west = _wheatPlots[clanId][0];
  1849	        east = _wheatPlots[clanId][1];
  1850	    }
  1851	
  1852	    function getScheduledMarketActionsForTick(uint64 tick)
  1853	        external
  1854	        view
  1855	        override
  1856	        returns (ScheduledMarketAction[] memory)
  1857	    {
  1858	        return _scheduledMarketActions[tick];
  1859	    }
  1860	
  1861	    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
  1862	        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
  1863	        uint256 count = 0;
  1864	
  1865	        for (uint256 i = 0; i < defendingClans.length; i++) {

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '430,760p'" in /home/claude/code/clan-world
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
   470	            // Scheduled market actions: already enqueued at submitClanOrders time.
   471	            // Settlement resolves this action slot — just complete the mission.
   472	            // (Actual execution happened or will happen at heartbeat.)
   473	            _completeMission(cs, m);
   474	        }
   475	    }
   476	
   477	    // -------------------------------------------------------------------------
   478	    // Gathering helpers
   479	    // -------------------------------------------------------------------------
   480	
   481	    function _gatherWood(
   482	        Clan storage, // clan (unused — no clan-level mutation in wood gather)
   483	        Clansman storage cs,
   484	        Mission storage m,
   485	        uint32 clanId,
   486	        uint64 tick,
   487	        bool starving,
   488	        bytes32 tickSeed
   489	    ) internal {
   490	        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
   491	            _completeMission(cs, m);
   492	            return;
   493	        }
   494	
   495	        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
   496	        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
   497	        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
   498	        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
   499	            yield *= 2;
   500	        }
   501	
   502	        if (starving) yield = yield / 2;
   503	        if (yield > remaining) yield = remaining;
   504	        cs.carryWood += yield;
   505	
   506	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
   507	
   508	        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
   509	            _completeMission(cs, m);
   510	        }
   511	    }
   512	
   513	    function _gatherIron(
   514	        Clan storage clan,
   515	        Clansman storage cs,
   516	        Mission storage m,
   517	        uint32 clanId,
   518	        uint64 tick,
   519	        bool starving,
   520	        bytes32 tickSeed
   521	    ) internal {
   522	        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
   523	        if (remaining == 0) {
   524	            _completeMission(cs, m);
   525	            return;
   526	        }
   527	        uint256 yield = ClanWorldConstants.IRON_BASE_YIELD;
   528	        if (starving) yield = yield / 2;
   529	        if (yield > remaining) yield = remaining;
   530	        cs.carryIron += yield;
   531	
   532	        // Gold bonus roll — scoped to reduce stack depth
   533	        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
   534	
   535	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
   536	
   537	        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
   538	            _completeMission(cs, m);
   539	        }
   540	    }
   541	
   542	    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
   543	        internal
   544	        returns (uint256 goldBonus)
   545	    {
   546	        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
   547	        if (uint256(goldRng) % 10000 < ClanWorldConstants.GOLD_FROM_IRON_BPS) {
   548	            goldBonus = ClanWorldConstants.GOLD_FROM_IRON_AMOUNT;
   549	            clan.goldBalance += goldBonus;
   550	        }
   551	    }
   552	
   553	    function _gatherFishDocks(
   554	        Clan storage,
   555	        Clansman storage cs,
   556	        Mission storage m,
   557	        uint32 clanId,
   558	        uint64 tick,
   559	        bool starving,
   560	        bytes32 tickSeed
   561	    ) internal {
   562	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   563	        if (remaining == 0) {
   564	            _completeMission(cs, m);
   565	            return;
   566	        }
   567	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   568	        uint256 fishRoll = uint256(fishRng) % 10000;
   569	        uint256 yield = 0;
   570	        if (fishRoll < ClanWorldConstants.FISH_DOCKS_BPS) {
   571	            yield = 1e18;
   572	        }
   573	        if (starving) yield = yield / 2;
   574	        if (yield > remaining) yield = remaining;
   575	        if (yield > 0) {
   576	            cs.carryFish += yield;
   577	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
   578	        }
   579	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   580	            _completeMission(cs, m);
   581	        }
   582	    }
   583	
   584	    function _gatherFishDeepSea(
   585	        Clan storage,
   586	        Clansman storage cs,
   587	        Mission storage m,
   588	        uint32 clanId,
   589	        uint64 tick,
   590	        bool starving,
   591	        bytes32 tickSeed
   592	    ) internal {
   593	        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
   594	        if (remaining == 0) {
   595	            _completeMission(cs, m);
   596	            return;
   597	        }
   598	        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
   599	        uint256 fishRoll = uint256(fishRng) % 10000;
   600	        uint256 yield = 0;
   601	        if (fishRoll < ClanWorldConstants.FISH_DEEP_BPS) {
   602	            yield = 1e18;
   603	        }
   604	        if (starving) yield = yield / 2;
   605	        if (yield > remaining) yield = remaining;
   606	        if (yield > 0) {
   607	            cs.carryFish += yield;
   608	            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
   609	        }
   610	        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
   611	            _completeMission(cs, m);
   612	        }
   613	    }
   614	
   615	    function _gatherWheat(
   616	        Clan storage,
   617	        /* clan — unused but kept positional for callsite parity */
   618	        Clansman storage cs,
   619	        Mission storage m,
   620	        uint32 clanId,
   621	        uint64 tick,
   622	        bool starving
   623	    ) internal {
   624	        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
   625	        if (remaining == 0) {
   626	            _completeMission(cs, m);
   627	            return;
   628	        }
   629	        // Determine plot index from region
   630	        uint8 region = m.targetRegion;
   631	        uint256 plotIdx;
   632	        if (region == ClanWorldConstants.REGION_WEST_FARMS) {
   633	            plotIdx = 0;
   634	        } else if (region == ClanWorldConstants.REGION_EAST_FARMS) {
   635	            plotIdx = 1;
   636	        } else {
   637	            // Wrong region — complete (no harvest)
   638	            _completeMission(cs, m);
   639	            return;
   640	        }
   641	
   642	        WheatPlot storage plot = _wheatPlots[clanId][plotIdx];
   643	        if (plot.state != WheatPlotState.Harvestable || plot.remainingWheat == 0) {
   644	            // Plot not ready — worker waits
   645	            _completeMission(cs, m);
   646	            return;
   647	        }
   648	
   649	        uint256 yield = WHEAT_HARVEST_RATE;
   650	        if (starving) yield = yield / 2;
   651	        if (yield > remaining) yield = remaining;
   652	        if (yield > plot.remainingWheat) yield = plot.remainingWheat;
   653	
   654	        cs.carryWheat += yield;
   655	        plot.remainingWheat -= yield;
   656	
   657	        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
   658	
   659	        if (plot.remainingWheat == 0) {
   660	            plot.state = WheatPlotState.Regrowing;
   661	            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
   662	        }
   663	
   664	        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
   665	            _completeMission(cs, m);
   666	        }
   667	        // else continuous
   668	    }
   669	
   670	    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
   671	        internal
   672	    {
   673	        // Must be at homebase region
   674	        if (cs.currentRegion != clan.baseRegion) {
   675	            _completeMission(cs, m);
   676	            return;
   677	        }
   678	        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
   679	        if (!hasAnything) {
   680	            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
   681	            _completeMission(cs, m);
   682	            return;
   683	        }
   684	
   685	        uint256 woodDelta = cs.carryWood;
   686	        uint256 ironDelta = cs.carryIron;
   687	        uint256 wheatDelta = cs.carryWheat;
   688	        uint256 fishDelta = cs.carryFish;
   689	
   690	        clan.vaultWood += woodDelta;
   691	        clan.vaultIron += ironDelta;
   692	        clan.vaultWheat += wheatDelta;
   693	        clan.vaultFish += fishDelta;
   694	
   695	        cs.carryWood = 0;
   696	        cs.carryIron = 0;
   697	        cs.carryWheat = 0;
   698	        cs.carryFish = 0;
   699	
   700	        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
   701	        _completeMission(cs, m);
   702	    }
   703	
   704	    function _doBuilding(
   705	        Clan storage clan,
   706	        Clansman storage cs,
   707	        Mission storage m,
   708	        uint32 clanId,
   709	        uint64 tick,
   710	        ActionType action
   711	    ) internal {
   712	        // Must be at homebase
   713	        if (cs.currentRegion != clan.baseRegion) {
   714	            _completeMission(cs, m);
   715	            return;
   716	        }
   717	
   718	        bool success = false;
   719	        if (action == ActionType.BuildWall) {
   720	            success = _tryBuildWall(clan, clanId, tick);
   721	        } else if (action == ActionType.UpgradeBase) {
   722	            success = _tryUpgradeBase(clan, clanId, tick);
   723	        } else if (action == ActionType.UpgradeMonument) {
   724	            success = _tryUpgradeMonument(clan, clanId, tick);
   725	        }
   726	
   727	        if (!success) {
   728	            // Resources missing — worker transitions to WAITING
   729	        }
   730	        _completeMission(cs, m);
   731	    }
   732	
   733	    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
   734	        uint8 nextLevel = clan.wallLevel + 1;
   735	        if (nextLevel > 5) return false;
   736	
   737	        uint256 woodCost;
   738	        uint256 ironCost;
   739	
   740	        if (nextLevel == 1) {
   741	            woodCost = 20e18;
   742	            ironCost = 0;
   743	        } else if (nextLevel == 2) {
   744	            woodCost = 35e18;
   745	            ironCost = 0;
   746	        } else if (nextLevel == 3) {
   747	            woodCost = 30e18;
   748	            ironCost = 5e18;
   749	        } else if (nextLevel == 4) {
   750	            woodCost = 40e18;
   751	            ironCost = 10e18;
   752	        } else {
   753	            woodCost = 50e18;
   754	            ironCost = 15e18;
   755	        }
   756	
   757	        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;
   758	
   759	        clan.vaultWood -= woodCost;
   760	        clan.vaultIron -= ironCost;

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '1,240p'" in /home/claude/code/clan-world
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
   193	    uint64 currentSeasonNumber;   // 1-indexed; incremented each time seasonEndTick is crossed
   194	    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
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
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '500,580p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   500	        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
   501	    );
   502	    event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   503	    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
   504	    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
   505	
   506	    // ----- missions -----
   507	    event MissionAssigned(
   508	        uint32 indexed clanId,
   509	        uint32 indexed clansmanId,
   510	        uint64 missionNonce,
   511	        ActionType action,
   512	        uint8 startRegion,
   513	        uint8 targetRegion,
   514	        uint64 startTick,
   515	        uint64 arrivalTick
   516	    );
   517	    event MissionInterrupted(
   518	        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
   519	    );
   520	    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   521	    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   522	
   523	    // ----- gathering / vault movement -----
   524	    event ResourcesGathered(
   525	        uint32 indexed clanId,
   526	        uint32 indexed clansmanId,
   527	        ActionType action,
   528	        uint256 woodGained,
   529	        uint256 ironGained,
   530	        uint256 wheatGained,
   531	        uint256 fishGained,
   532	        uint256 goldBonus,
   533	        uint64 atTick
   534	    );
   535	    event ResourcesDeposited(
   536	        uint32 indexed clanId,
   537	        uint32 indexed clansmanId,
   538	        uint256 woodDelta,
   539	        uint256 ironDelta,
   540	        uint256 wheatDelta,
   541	        uint256 fishDelta,
   542	        uint32 tick
   543	    );
   544	
   545	    // ----- building -----
   546	    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   547	    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   548	    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
   549	
   550	    // ----- market -----
   551	    event ImmediateMarketActionExecuted(
   552	        uint32 indexed clanId,
   553	        uint32 indexed clansmanId,
   554	        address tokenIn,
   555	        address tokenOut,
   556	        uint256 amountIn,
   557	        uint256 amountOut,
   558	        uint64 atTick
   559	    );
   560	    event ScheduledMarketActionExecuted(
   561	        uint64 indexed executeAtTick,
   562	        uint64 indexed commitSequence,
   563	        uint32 indexed clanId,
   564	        uint32 clansmanId,
   565	        address tokenIn,
   566	        address tokenOut,
   567	        uint256 amountIn,
   568	        uint256 amountOut
   569	    );
   570	    event ScheduledMarketActionCommitted(
   571	        uint64 indexed executeAtTick,
   572	        uint64 indexed commitSequence,
   573	        uint32 indexed clanId,
   574	        uint32 clansmanId,
   575	        ActionType action,
   576	        address marketToken,
   577	        uint256 marketAmount,
   578	        uint256 maxGoldIn
   579	    );
   580	    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);

codex
I’ve pulled the exact changed contract sections; next I’m scanning all tests and off-chain consumers for ABI/data-model mismatches (especially the `ResourcesDeposited` signature and tick width change).
exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '430,620p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   430	        ClanFullView memory view_ = world.getClanFullView(clanId);
   431	        uint8 homeRegion = view_.clan.clan.baseRegion;
   432	
   433	        // Use two clansmen: one mines iron, one stays home to deposit
   434	        uint32 cs0 = view_.clansmen[0].clansman.clansman.clansmanId;
   435	        uint32 cs1 = view_.clansmen[1].clansman.clansman.clansmanId;
   436	
   437	        // Send cs0 to Mountains to mine iron (requires travel from non-Mountains home)
   438	        uint8 targetRegion = ClanWorldConstants.REGION_MOUNTAINS;
   439	        (uint8 travelToMountains,) = world.quoteTravel(homeRegion, targetRegion);
   440	        _submitOrder(clanId, cs0, targetRegion, ActionType.MineIron);
   441	
   442	        // Advance through travel and the four-tick mining duration.
   443	        for (uint256 i = 0; i < uint256(travelToMountains) + world.getActionDuration(ActionType.MineIron) + 1; i++) {
   444	            _advanceTick();
   445	        }
   446	        world.settleClan(clanId);
   447	
   448	        uint256 carryBefore = world.getClansman(cs0).carryIron;
   449	        assertGt(carryBefore, 0, "cs0 should have carry iron after mining at Mountains");
   450	
   451	        uint256 vaultBefore = world.getClan(clanId).vaultIron;
   452	
   453	        // Wait for cs0 cooldown to expire, then send back to deposit
   454	        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
   455	        _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
   456	
   457	        // Advance through travel back to homebase and the deposit's 1-tick transfer.
   458	        (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
   459	        for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
   460	            _advanceTick();
   461	        }
   462	        world.settleClan(clanId);
   463	
   464	        // cs0 carry iron should be cleared and vault iron should have increased
   465	        Clansman memory cs0After = world.getClansman(cs0);
   466	        assertEq(cs0After.carryIron, 0, "carry iron should be cleared after deposit");
   467	
   468	        uint256 vaultAfter = world.getClan(clanId).vaultIron;
   469	        assertGt(vaultAfter, vaultBefore, "vault iron should increase after deposit");
   470	
   471	        // cs1 unused — suppress warning
   472	        cs1;
   473	    }
   474	
   475	    function test_depositResources_woodCarryMovesToVaultAndClears() public {
   476	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   477	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   478	        uint256 woodDelta = 5e18;
   479	
   480	        harness.setCarry(csId, woodDelta, 0, 0, 0);
   481	        uint256 vaultBefore = harness.getClan(clanId).vaultWood;
   482	
   483	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   484	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
   485	
   486	        Mission memory mission = harness.getActiveMission(csId);
   487	        assertEq(
   488	            mission.settlesAtTick,
   489	            mission.executesAtTick + harness.getActionDuration(ActionType.DepositResources),
   490	            "deposit settles after transfer duration"
   491	        );
   492	
   493	        _advanceTickHarness(harness);
   494	        _advanceTickHarness(harness);
   495	
   496	        assertEq(harness.getClan(clanId).vaultWood, vaultBefore + woodDelta, "vault wood receives carried wood");
   497	        assertEq(harness.getClansman(csId).carryWood, 0, "carry wood is cleared");
   498	    }
   499	
   500	    function test_depositResources_emptyCarryNoopsWithoutEvent() public {
   501	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   502	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   503	
   504	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   505	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "empty deposit order should be accepted");
   506	
   507	        vm.recordLogs();
   508	        _advanceTickHarness(harness);
   509	        _advanceTickHarness(harness);
   510	        Vm.Log[] memory logs = vm.getRecordedLogs();
   511	        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
   512	
   513	        for (uint256 i = 0; i < logs.length; i++) {
   514	            assertTrue(logs[i].topics[0] != depositedTopic, "empty deposit emits no ResourcesDeposited event");
   515	        }
   516	
   517	        assertFalse(harness.getActiveMission(csId).active, "empty deposit still completes");
   518	    }
   519	
   520	    function test_depositResources_multipleTypesMoveTogether() public {
   521	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   522	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   523	        uint256 woodDelta = 4e18;
   524	        uint256 ironDelta = 2e18;
   525	        uint256 fishDelta = 3e18;
   526	
   527	        harness.setCarry(csId, woodDelta, ironDelta, 0, fishDelta);
   528	        Clan memory beforeClan = harness.getClan(clanId);
   529	
   530	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   531	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
   532	
   533	        _advanceTickHarness(harness);
   534	        _advanceTickHarness(harness);
   535	
   536	        Clan memory afterClan = harness.getClan(clanId);
   537	        Clansman memory afterCs = harness.getClansman(csId);
   538	
   539	        assertEq(afterClan.vaultWood, beforeClan.vaultWood + woodDelta, "wood transferred");
   540	        assertEq(afterClan.vaultIron, beforeClan.vaultIron + ironDelta, "iron transferred");
   541	        assertEq(afterClan.vaultFish, beforeClan.vaultFish + fishDelta, "fish transferred");
   542	        assertEq(afterCs.carryWood, 0, "wood carry cleared");
   543	        assertEq(afterCs.carryIron, 0, "iron carry cleared");
   544	        assertEq(afterCs.carryFish, 0, "fish carry cleared");
   545	    }
   546	
   547	    function test_depositResources_requiresHomeRegion() public {
   548	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   549	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   550	        uint8 nonHomeRegion = homeRegion == ClanWorldConstants.REGION_FOREST
   551	            ? ClanWorldConstants.REGION_MOUNTAINS
   552	            : ClanWorldConstants.REGION_FOREST;
   553	
   554	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, nonHomeRegion, ActionType.DepositResources);
   555	
   556	        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_REGION), "deposit must target home region");
   557	    }
   558	
   559	    function test_depositResources_eventHasCorrectDeltas() public {
   560	        (ClanWorldTestHarness harness, uint32 clanId, uint32 csId) = _setupHarness();
   561	        uint8 homeRegion = harness.getClan(clanId).baseRegion;
   562	
   563	        harness.setCarry(csId, 5e18, 2e18, 1e18, 3e18);
   564	
   565	        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
   566	        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
   567	
   568	        _advanceTickHarness(harness);
   569	        vm.expectEmit(true, true, false, true);
   570	        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
   571	        _advanceTickHarness(harness);
   572	    }
   573	
   574	    function test_quoteTravel_outOfBounds_returnsZero() public {
   575	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
   576	        assertEq(ticks, 0, "out-of-range src should return 0 ticks");
   577	        assertEq(path, bytes8(0), "out-of-range src should return empty path");
   578	
   579	        (ticks, path) = world.quoteTravel(1, 9);
   580	        assertEq(ticks, 0, "out-of-range dst should return 0 ticks");
   581	        assertEq(path, bytes8(0), "out-of-range dst should return empty path");
   582	    }
   583	
   584	    // -------------------------------------------------------------------------
   585	    // Test A: quoteTravel(9,9) — both out-of-bounds same-region, must return (0, bytes8(0))
   586	    // -------------------------------------------------------------------------
   587	
   588	    function test_quoteTravel_outOfBounds_sameRegion() public {
   589	        // Previously (9,9) escaped the > 8 guard (same-region early return fired first),
   590	        // returning (0, bytes8(uint64(9) << 56)) instead of (0, bytes8(0)).
   591	        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 9);
   592	        assertEq(ticks, 0, "quoteTravel(9,9): travelTicks must be 0");
   593	        assertEq(path, bytes8(0), "quoteTravel(9,9): path must be bytes8(0), not a packed 9-region sentinel");
   594	    }
   595	
   596	    // -------------------------------------------------------------------------
   597	    // Test B: submitClanOrders returns ERR_INVALID_ACTION when clan is >200 ticks behind
   598	    // -------------------------------------------------------------------------
   599	
   600	    function test_submitClanOrders_reverts_when_clan_too_far_behind() public {
   601	        uint32 clanId = _mintClan();
   602	        ClanFullView memory view_ = world.getClanFullView(clanId);
   603	        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;
   604	
   605	        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
   606	        for (uint256 i = 0; i < 201; i++) {
   607	            _advanceTick();
   608	        }
   609	
   610	        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
   611	        // without reverting, for every order in the batch
   612	        ClanOrder[] memory orders = new ClanOrder[](1);
   613	        orders[0] = ClanOrder({
   614	            clansmanId: csId,
   615	            gotoRegion: ClanWorldConstants.REGION_FOREST,
   616	            action: ActionType.ChopWood,
   617	            targetClanId: 0,
   618	            marketToken: address(0),
   619	            marketAmount: 0,
   620	            maxGoldIn: 0

exec
/bin/bash -lc 'git show 9ccf94a:packages/contracts/test/Gathering.t.sol | nl -ba' in /home/claude/code/clan-world
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
   117	        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);
   118	
   119	        Clansman memory cs = _settleChopWood(clanId, csId);
   120	
   121	        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");
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
/bin/bash -lc 'rg -n "ResourcesDeposited|woodDelta|ironDelta|wheatDelta|fishDelta|atTick|tick" apps packages -S' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/runner/.env.example:13:# and the tick loop will idle (snapshot.tick = 0).
apps/orchestrator/AGENTS.md:3:Node app that supervises the 4 long-running Elder Claude Code sessions. One Elder per clan, each running as a `claude` CLI subprocess in `--print --resume` mode. The orchestrator pumps a `<situation>` block into stdin every tick, captures the Elder's tool-call output (via the `elder` CLI), and moves on.
apps/orchestrator/AGENTS.md:8:- **Tick pump:** read current tick from chain, format `<situation>` block from Convex snapshot, send to each Elder, await `elder clan submit-orders` toolbelt invocation.
apps/orchestrator/AGENTS.md:20:- (TBD) `src/tick.ts` — per-tick coordination loop.
apps/orchestrator/AGENTS.md:27:- **Strict per-tick deadline.** `agentThinkBudgetMs` (12s for S1, 45s for S2) — orchestrator force-cancels and submits empty orders if an Elder doesn't return in time.
apps/orchestrator/AGENTS.md:32:- **`IChainClient`** — reads tick, fires heartbeat tx (until S2 hands that off to KeeperHub).
apps/orchestrator/AGENTS.md:33:- **`IConvexClient`** — reads snapshot per tick, posts logs.
apps/orchestrator/AGENTS.md:35:- **`ILLMClient`** — used by the optional narrator (post-tick storyline summary). Elders don't go through this — they're full Claude Code sessions.
packages/runner/src/filePeerInbox.ts:24: * that guarantee exactly-once must deduplicate by (fromClanId, tick, msgId)").
packages/runner/src/filePeerInbox.ts:60:  async send(toClanId: string, message: string, tick: number): Promise<void> {
packages/runner/src/filePeerInbox.ts:75:      tick,
packages/runner/src/filePeerInbox.ts:120:  tick?: number;
packages/runner/src/filePeerInbox.ts:140:    tick: raw.tick ?? 0,
packages/runner/src/fileMemoryStore.ts:60:      // the tick loop. The next `save()` will overwrite with a valid JSON doc.
apps/server/AGENTS.md:3:Convex backend. Hosts game-state queries, the indexer cron, and the post-tick webhook. One Convex deployment per realm (one for S1 World Chain Sepolia, redeployed for S2 Base Sepolia).
apps/server/AGENTS.md:9:- **Indexer cron:** runs every 5s as a safety net; the primary trigger is the post-tick webhook (per addendum §4).
apps/server/AGENTS.md:10:- **Post-tick webhook:** HTTP action at `/api/heartbeat-webhook` — re-runs both event indexer and state snapshot refresh.
apps/server/AGENTS.md:35:- **`ILLMClient`** — used by the optional narrator function (S2) to summarize ticks.
packages/runner/src/composeSituationBlock.ts:14:  tick: number;
packages/runner/src/composeSituationBlock.ts:18: * Compose the per-tick situation block for a single Elder.
packages/runner/src/composeSituationBlock.ts:23: *   2. Tick state             — current tick + tick-epoch info
packages/runner/src/composeSituationBlock.ts:45:  lines.push(`# Tick ${args.tick} — Elder ${args.elder} (Clan ${args.clanId})`);
packages/runner/src/composeSituationBlock.ts:50:    `You are Elder ${args.elder}, ruling Clan ${args.clanId}. You reason once per tick and ` +
packages/runner/src/composeSituationBlock.ts:51:      `submit clan orders via the \`elder clan submit-orders\` CLI before the tick closes.`,
packages/runner/src/composeSituationBlock.ts:56:  lines.push(`- current tick: ${args.tick}`);
packages/runner/src/composeSituationBlock.ts:57:  lines.push(`- tick started at (unix s): ${snap.tickEpoch.startedAt}`);
packages/runner/src/composeSituationBlock.ts:58:  lines.push(`- tick duration: ${snap.tickEpoch.durationMs}ms`);
packages/runner/src/composeSituationBlock.ts:81:  lines.push(`- pending orders for next tick: ${view.pendingOrders.length}`);
packages/runner/src/composeSituationBlock.ts:92:      lines.push(`- [${p.sentAt}] from clan ${p.fromClanId} (tick ${p.tick}): ${p.message}`);
packages/runner/src/composeSituationBlock.ts:100:      lines.push(`- tick ${w.tick} from clan ${w.fromClanId} → ${w.toClanId}: ${w.text}`);
packages/runner/src/composeSituationBlock.ts:108:    lines.push('- (empty — first tick, or post-clear bootstrap)');
apps/orchestrator/src/main.ts:10:  // Get current tick
apps/orchestrator/src/main.ts:11:  const tick = await chain.getCurrentTick();
apps/orchestrator/src/main.ts:12:  console.error(`[orchestrator] tick=${tick}`);
apps/orchestrator/src/main.ts:27:    await convex.postLog('info', `Elder Aldric (clan-${CLAN_ID}): ChopWood submitted — txHash=${txHash} tick=${tick}`);
apps/orchestrator/src/main.ts:34:    JSON.stringify({ tick, txHash, clan: CLAN_ID, mission: 'ChopWood-Forest' }, null, 2) + '\n',
packages/runner/src/main.ts:6:import { tickLoop, type PerElderDeps } from './tickLoop';
packages/runner/src/main.ts:24: * next tick — this is just the "you are Elder N, clan X, await tick" preamble.
packages/runner/src/main.ts:31:    'A full situation block will arrive on the next tick. Until then, you can use the `elder` CLI to:',
packages/runner/src/main.ts:37:    'Wait for the next tick.',
packages/runner/src/main.ts:146:    await tickLoop({
packages/runner/src/main.ts:154:    console.log('[runner] tick loop exited');
packages/runner/src/pollChainTick.ts:4: * Read the current tick from Convex.
packages/runner/src/pollChainTick.ts:8: * `{ tick: 0, ... }`. The tick loop interprets `0` as "no real chain state
packages/runner/src/pollChainTick.ts:18:  return snap.tick;
packages/runner/src/heartbeatScheduler.ts:17:   * Shared latch — Cycle A only fires heartbeat after Cycle B settles a new tick.
packages/runner/src/heartbeatScheduler.ts:40:  // Cycle A only fires when Cycle B has settled a tick NEWER than the last one
packages/runner/src/heartbeatScheduler.ts:55:        // Only fire after Cycle B has settled a tick newer than our last heartbeat.
packages/runner/src/heartbeatScheduler.ts:57:        // for Cycle B to settle the next tick; reading after the call would mark
packages/runner/src/heartbeatScheduler.ts:58:        // the newer tick as already-heartbeated and permanently skip it.
packages/runner/clanworld-runner.service:2:Description=ClanWorld Runner Daemon (per-tick Elder orchestrator)
packages/runner/README.md:4:per-tick reasoning loop. Memory and peer-inbox layers are pluggable.
packages/runner/README.md:63:elder-1-last-tick.txt        ← TmuxRunnerInbox idempotency marker
packages/runner/README.md:64:elder-2-last-tick.txt
packages/runner/README.md:80:  `{ tick: 0, ... }`. The tick loop interprets this as "no real chain state"
packages/runner/src/settleWindow.ts:9: * Returns early if `signal` is aborted; the tick loop uses this to react to
packages/runner/test/filePeerInbox.test.ts:57:        tick: 0,
packages/runner/src/tickLoop.ts:28:  /** Optional shared latch — Cycle A waits for Cycle B to call markSettled(tick). */
packages/runner/src/tickLoop.ts:45: * Per-tick Elder delivery loop (Cycle B):
packages/runner/src/tickLoop.ts:64: *   On restart, TmuxRunnerInbox idempotency (last-tick.txt) prevents double-delivery.
packages/runner/src/tickLoop.ts:72:export async function tickLoop(deps: TickLoopDeps): Promise<void> {
packages/runner/src/tickLoop.ts:88:      log.info(`tick ${chainTick} observed (last processed: ${lastProcessedTick})`);
packages/runner/src/tickLoop.ts:97:      // self-heal mid-tick, while permanent failures don't deadlock the world.
packages/runner/src/tickLoop.ts:107:              { elder, clanId: deps.config.elderToClanId[elder], tick: chainTick },
packages/runner/src/tickLoop.ts:122:              `deliverSituationBlock(elder=${elder}, tick=${chainTick})`,
packages/runner/src/tickLoop.ts:129:            if (status.reason === 'duplicate-tick') {
packages/runner/src/tickLoop.ts:130:              log.info(`elder ${elder}: tick ${chainTick} already processed by other elder, skipping`);
packages/runner/src/tickLoop.ts:152:        log.warn(`tick ${chainTick}: retry ${attempt}/${MAX_RETRIES} for ${failedElders.length} failed elder(s): [${failedElders.join(', ')}]`);
packages/runner/src/tickLoop.ts:164:          `[tickLoop] WARNING: tick ${chainTick} advancing despite ${failedElders.length} elder(s) still failing after ${MAX_RETRIES} retries: [${failedElders.join(', ')}]. Game state for these clans may diverge until they recover.`,
packages/runner/src/tmuxRunnerInbox.ts:13: *   Re-delivering tick N's block to the same Elder while tick N is still
packages/runner/src/tmuxRunnerInbox.ts:14: *   in-flight must be a no-op. We persist the last-delivered tick to a small
packages/runner/src/tmuxRunnerInbox.ts:15: *   per-Elder marker file so idempotency survives a runner restart mid-tick.
packages/runner/src/tmuxRunnerInbox.ts:39:    this.markerFile = path.join(opts.stateDir, `elder-${opts.elder}-last-tick.txt`);
packages/runner/src/tmuxRunnerInbox.ts:46:  async deliverSituationBlock(tick: number, block: string, signal?: AbortSignal): Promise<DeliveryStatus> {
packages/runner/src/tmuxRunnerInbox.ts:49:    if (last !== undefined && last >= tick) {
packages/runner/src/tmuxRunnerInbox.ts:50:      return { ok: false, reason: 'duplicate-tick' };
packages/runner/src/tmuxRunnerInbox.ts:55:      writeLastTick(this.markerFile, tick);
packages/runner/src/tmuxRunnerInbox.ts:82:      // /clear failures are non-fatal — the next tick will try again.
packages/runner/src/tmuxRunnerInbox.ts:85:      // Consume the ack flag so the next final-tick warning starts fresh.
packages/runner/src/tmuxRunnerInbox.ts:150:function writeLastTick(file: string, tick: number): void {
packages/runner/src/tmuxRunnerInbox.ts:152:  writeRestrictedFileSync(file, `${tick}\n`, {
packages/runner/src/types.ts:5: * through a per-tick reasoning loop:
packages/runner/src/types.ts:7: *   1. Poll Convex for the latest tick.
packages/runner/src/types.ts:26:  /** Milliseconds between Convex tick polls when no new tick is observed. */
packages/runner/src/axlPeerInbox.ts:19: *   - Deduplication by (fromClanId, tick, msgId) is maintained in an in-memory Set.
packages/runner/src/axlPeerInbox.ts:88:  tick: number;
packages/runner/src/axlPeerInbox.ts:107:    typeof obj['tick'] !== 'number' ||
packages/runner/src/axlPeerInbox.ts:158:    typeof obj['tick'] === 'number' &&
packages/runner/src/axlPeerInbox.ts:275:  async send(toClanId: string, message: string, tick: number): Promise<void> {
packages/runner/src/axlPeerInbox.ts:277:    const msgId = this.#generateMsgId(tick);
packages/runner/src/axlPeerInbox.ts:278:    await this.#sendWithMsgId(toClanId, message, tick, msgId);
packages/runner/src/axlPeerInbox.ts:294:  /** Generate a collision-resistant msgId for a given tick (MED 3 — stable via sendIdempotent). */
packages/runner/src/axlPeerInbox.ts:295:  #generateMsgId(tick: number): string {
packages/runner/src/axlPeerInbox.ts:297:    return `${this.#myClanId}:${tick}:${Date.now()}-${randomSuffix}`;
packages/runner/src/axlPeerInbox.ts:304:    tick: number,
packages/runner/src/axlPeerInbox.ts:319:      tick,
packages/runner/src/axlPeerInbox.ts:330:   * Deduplication by (fromClanId, tick, msgId) prevents re-adding on repeated drains.
packages/runner/src/axlPeerInbox.ts:400:      // Idempotency: skip duplicates by (fromClanId, tick, msgId).
packages/runner/src/axlPeerInbox.ts:401:      const dedupKey = `${envelope.fromClanId}:${envelope.tick}:${envelope.msgId}`;
packages/runner/src/axlPeerInbox.ts:409:        tick: envelope.tick,
packages/runner/src/axlPeerInbox.ts:442:      const dedupKey = `${entry.fromClanId}:${entry.tick}:${entry.msgId}`;
packages/runner/test/axlPeerInbox.test.ts:99:    expect(msgs[0]!.tick).toBe(7);
packages/runner/test/axlPeerInbox.test.ts:149:      tick: number;
packages/runner/test/axlPeerInbox.test.ts:155:    expect(envelope.tick).toBe(5);
packages/runner/test/axlPeerInbox.test.ts:182:      message: 'msg1', tick: 1, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:187:      message: 'msg2', tick: 2, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:212:      message: 'not for me', tick: 1, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:224:      message: 'wrong net', tick: 1, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:247:      message: 'persistent', tick: 1, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:265:// Idempotency — dedup by (fromClanId, tick, msgId)
packages/runner/test/axlPeerInbox.test.ts:274:  it('deduplicates messages with same (fromClanId, tick, msgId)', async () => {
packages/runner/test/axlPeerInbox.test.ts:277:      message: 'dedup me', tick: 3, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:292:  it('does not deduplicate messages with different msgIds (same tick)', async () => {
packages/runner/test/axlPeerInbox.test.ts:295:      message: 'msg-a', tick: 3, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:300:      message: 'msg-b', tick: 3, sentAt: new Date().toISOString(),
packages/runner/test/axlPeerInbox.test.ts:394:    // FilePeerInbox generates msgId from myClanId:tick:Date.now().
packages/runner/test/axlPeerInbox.test.ts:438:      tick: 1,
packages/runner/test/axlPeerInbox.test.ts:463:      tick: 2,
packages/runner/test/axlPeerInbox.test.ts:487:      tick: 1,
packages/runner/test/axlPeerInbox.test.ts:568:      tick: 7,
packages/runner/test/axlPeerInbox.test.ts:598:      tick: 1,
packages/runner/test/axlPeerInbox.test.ts:625:      tick: 1,
packages/runner/test/axlPeerInbox.test.ts:645:      tick: 1,
packages/runner/test/axlPeerInbox.test.ts:700:      tick: 9,
packages/runner/test/axlPeerInbox.test.ts:728:    const badShape = JSON.stringify({ fromClanId: 'clan-ember', tick: 1, message: 'bad' });
packages/runner/test/axlPeerInbox.test.ts:733:      tick: 10,
packages/runner/src/settleLatch.ts:3: * Cycle B calls markSettled(tick) after its settle window completes.
packages/runner/src/settleLatch.ts:5: * current on-chain tick hasn't been settled yet, Cycle B is still working.
packages/runner/src/settleLatch.ts:9:  markSettled(tick: number): void;
packages/runner/src/settleLatch.ts:13:  let _tick = -1;
packages/runner/src/settleLatch.ts:15:    lastSettledTick: () => _tick,
packages/runner/src/settleLatch.ts:16:    markSettled: (tick) => { if (tick > _tick) _tick = tick; },
packages/runner/test/heartbeatScheduler.test.ts:115:    settleLatch.markSettled(1); // Cycle B already settled tick 1
packages/runner/test/heartbeatScheduler.test.ts:125:  it('skips heartbeat when Cycle B has not settled the current tick', async () => {
packages/runner/test/heartbeatScheduler.test.ts:139:    // Cycle B settles a tick — Cycle A now allowed to fire
packages/runner/test/heartbeatScheduler.test.ts:147:  it('slow callHeartbeat + Cycle B advances mid-call does not permanently skip next tick', async () => {
packages/runner/test/heartbeatScheduler.test.ts:148:    // Scenario: Cycle A fires for tick 1. callHeartbeat takes 250ms. During that wait,
packages/runner/test/heartbeatScheduler.test.ts:149:    // Cycle B settles tick 2. Without snapshot-before-call, lastHeartbeatForTick would
packages/runner/test/heartbeatScheduler.test.ts:150:    // be set to 2 after the call, making Cycle A think tick 2 was already heartbeated.
packages/runner/test/heartbeatScheduler.test.ts:152:    settleLatch.markSettled(1); // Cycle B settled tick 1
packages/runner/test/heartbeatScheduler.test.ts:168:    // Trigger first interval — starts callHeartbeat for tick 1 (settledSnapshot = 1)
packages/runner/test/heartbeatScheduler.test.ts:172:    // Cycle B settles tick 2 WHILE callHeartbeat is still in-flight
packages/runner/test/heartbeatScheduler.test.ts:180:    // So the next interval must fire heartbeat for tick 2
packages/runner/test/heartbeatScheduler.test.ts:182:    expect(callHeartbeat).toHaveBeenCalledTimes(2); // fired again for tick 2
packages/runner/test/tmuxRunnerInbox.test.ts:58:  it('returns duplicate-tick when re-delivering the same tick', async () => {
packages/runner/test/tmuxRunnerInbox.test.ts:69:    expect(second).toEqual({ ok: false, reason: 'duplicate-tick' });
packages/runner/test/tmuxRunnerInbox.test.ts:74:  it('also rejects an older tick after a newer one was delivered', async () => {
packages/runner/test/tmuxRunnerInbox.test.ts:85:    expect(older).toEqual({ ok: false, reason: 'duplicate-tick' });
packages/runner/test/tmuxRunnerInbox.test.ts:102:  it('persists the last-tick marker so idempotency survives restart', async () => {
packages/runner/test/tmuxRunnerInbox.test.ts:113:    const marker = path.join(tmpDir, 'elder-1-last-tick.txt');
packages/runner/test/tmuxRunnerInbox.test.ts:126:    expect(replay).toEqual({ ok: false, reason: 'duplicate-tick' });
packages/runner/test/tmuxRunnerInbox.test.ts:169:  it('does not write last-tick marker when delivery is aborted mid-send', async () => {
packages/runner/test/tmuxRunnerInbox.test.ts:186:    const marker = path.join(tmpDir, 'elder-1-last-tick.txt');
apps/landing/src/pages/LandingPage.tsx:50:                ERC-7857 iNFTs · persistent agent memory · live world ticks · onchain ownership transfer
apps/landing/src/pages/LandingPage.tsx:119:              remembers tick 412 — when clan three betrayed it for a barrel of fish."
apps/landing/src/pages/LandingPage.tsx:186:              body="In the second season, an Elder named Mira refused to trade with Aldric for nine consecutive ticks, citing a memory of broken oath. The owner had transferred Mira to a new wallet between seasons. The new owner had never met Aldric. Mira remembered."
packages/runner/test/composeSituationBlock.test.ts:14:        tick: 12,
packages/runner/test/composeSituationBlock.test.ts:15:        tickEpoch: { startedAt: 1700000000, durationMs: 60000 },
packages/runner/test/composeSituationBlock.test.ts:62:      { elder: 1, clanId: '1', tick: 12 },
packages/runner/test/composeSituationBlock.test.ts:65:        memory: fakeMemory({ strategy: 'expand-north', last_tick_handled: '11' }),
packages/runner/test/composeSituationBlock.test.ts:70:            message: 'truce until tick 20?',
packages/runner/test/composeSituationBlock.test.ts:71:            tick: 11,
packages/runner/test/composeSituationBlock.test.ts:99:    expect(block).toContain('truce until tick 20?');
packages/runner/test/composeSituationBlock.test.ts:105:      { elder: 3, clanId: '3', tick: 1 },
packages/runner/test/composeSituationBlock.test.ts:110:              tick: 1,
packages/runner/test/composeSituationBlock.test.ts:111:              tickEpoch: { startedAt: 0, durationMs: 20000 },
packages/runner/test/composeSituationBlock.test.ts:132:    expect(block).toContain('first tick, or post-clear bootstrap');
packages/runner/test/composeSituationBlock.test.ts:137:      { elder: 2, clanId: '2', tick: 5 },
packages/runner/test/tickLoop.test.ts:9:import { tickLoop, type Logger, type PerElderDeps } from '../src/tickLoop';
packages/runner/test/tickLoop.test.ts:12:function fakeConvex(tick: number): IConvexClient {
packages/runner/test/tickLoop.test.ts:16:        tick,
packages/runner/test/tickLoop.test.ts:17:        tickEpoch: { startedAt: 0, durationMs: 60000 },
packages/runner/test/tickLoop.test.ts:71:describe('tickLoop', () => {
packages/runner/test/tickLoop.test.ts:72:  it('treats duplicate-tick delivery as an idempotent success without retries', async () => {
packages/runner/test/tickLoop.test.ts:74:    const delivered: Array<{ elder: ElderId; tick: number }> = [];
packages/runner/test/tickLoop.test.ts:79:        async deliverSituationBlock(tick: number): Promise<DeliveryStatus> {
packages/runner/test/tickLoop.test.ts:80:          delivered.push({ elder, tick });
packages/runner/test/tickLoop.test.ts:81:          return { ok: false, reason: 'duplicate-tick' };
packages/runner/test/tickLoop.test.ts:105:    await tickLoop({
packages/runner/test/tickLoop.test.ts:117:    expect(delivered).toEqual(ELDER_IDS.map(elder => ({ elder, tick: 9 })));
packages/runner/test/tickLoop.test.ts:118:    expect(logs.info.flat().join('\n')).toContain('tick 9 already processed');
apps/landing/src/styles/global.css:402:  position: sticky;
apps/server/convex/heartbeat.ts:30:  | { status: "ok"; tick: number }
apps/server/convex/heartbeat.ts:40:    // calls from inserting duplicate tick rows.
apps/server/convex/heartbeat.ts:43:      snap.tickEpochStartedAt +
apps/server/convex/heartbeat.ts:44:      Math.floor(snap.tickEpochDurationMs / 1000);
apps/server/convex/heartbeat.ts:49:    const newTick = snap.tick + 1;
apps/server/convex/heartbeat.ts:51:      tick: newTick,
apps/server/convex/heartbeat.ts:52:      tickEpochStartedAt: Math.floor(Date.now() / 1000),
apps/server/convex/heartbeat.ts:53:      tickEpochDurationMs: snap.tickEpochDurationMs,
apps/server/convex/heartbeat.ts:59:      message: `heartbeat: tick ${snap.tick} → ${newTick}`,
apps/server/convex/heartbeat.ts:63:    return { status: "ok", tick: newTick };
apps/server/convex/schema.ts:6:    tick: v.number(),
apps/server/convex/schema.ts:7:    tickEpochStartedAt: v.number(),
apps/server/convex/schema.ts:8:    tickEpochDurationMs: v.number(),
apps/landing/src/pages/LorePage.tsx:381:          The world advances in <strong>ticks</strong>. A tick is a fixed unit of
apps/landing/src/pages/LorePage.tsx:383:          production. At the close of each tick, all pending actions are
apps/landing/src/pages/LorePage.tsx:388:        <div className="tick-wheel-wrap">
apps/landing/src/pages/LorePage.tsx:389:          <svg viewBox="0 0 360 360" className="tick-wheel" aria-label="The cycle of a tick">
apps/landing/src/pages/LorePage.tsx:391:              <radialGradient id="tickWheelBg" cx="50%" cy="50%" r="50%">
apps/landing/src/pages/LorePage.tsx:396:            <circle cx="180" cy="180" r="150" fill="url(#tickWheelBg)"
apps/landing/src/pages/LorePage.tsx:428:                fontSize="14" fill="#E8D8B5">tick</text>
apps/landing/src/pages/LorePage.tsx:452:          number of ticks of travel. The map below renders both the geography
apps/landing/src/pages/LorePage.tsx:466:              <th>Yield / tick</th>
apps/landing/src/pages/LorePage.tsx:472:            <tr><td>Forest</td><td>Wood</td><td>5</td><td>2 ticks</td><td>Dense, easy entry from Unicorn Town.</td></tr>
apps/landing/src/pages/LorePage.tsx:473:            <tr><td>Mountains</td><td>Iron</td><td>3</td><td>3 ticks</td><td>Slow yield, rich reward.</td></tr>
apps/landing/src/pages/LorePage.tsx:475:            <tr><td>West Farms</td><td>Wheat</td><td>4</td><td>2 ticks</td><td>Bandits favour these fields.</td></tr>
apps/landing/src/pages/LorePage.tsx:476:            <tr><td>East Farms</td><td>Wheat</td><td>4</td><td>2 ticks</td><td>Slightly safer. Slightly.</td></tr>
apps/landing/src/pages/LorePage.tsx:477:            <tr><td>West Docks</td><td>Fish</td><td>4</td><td>3 ticks</td><td>Stormy. Yield is unpredictable.</td></tr>
apps/landing/src/pages/LorePage.tsx:478:            <tr><td>East Docks</td><td>Fish</td><td>4</td><td>3 ticks</td><td>Calmer. Modestly more reliable.</td></tr>
apps/landing/src/pages/LorePage.tsx:479:            <tr><td>Deep Sea</td><td>Pearls</td><td>1 (rare)</td><td>5 ticks</td><td>Drakes. Few return.</td></tr>
apps/landing/src/pages/LorePage.tsx:503:          <div className="bar-axis pixel">0 — 5 units / tick</div>
apps/landing/src/pages/LorePage.tsx:568:              { x: 650, label: 'COOLDOWN', sub: '~3 ticks rest', color: '#3F704D' },
apps/landing/src/pages/LorePage.tsx:600:              The cooldown is roughly three ticks. Tuned by the realm itself,
apps/landing/src/pages/LorePage.tsx:660:          A season is three hundred and sixty ticks long. Winter falls upon the
apps/landing/src/pages/LorePage.tsx:661:          realm <strong>every hundred and ten ticks</strong>, and each winter
apps/landing/src/pages/LorePage.tsx:662:          lasts <strong>ten ticks</strong> — three winters in all, before the
apps/landing/src/pages/LorePage.tsx:669:          <div className="season-axis-top pixel">tick 0 → tick 360 · winter every 110 ticks · lasts 10 ticks</div>
apps/landing/src/pages/LorePage.tsx:709:              Each clan burns one Wood per tick during winter to maintain
apps/landing/src/pages/LorePage.tsx:711:              their clansmen — one health per tick per unburnt fire.
apps/landing/src/pages/LorePage.tsx:728:          A season ends when the bell rings tick three hundred and sixty, or
apps/landing/src/pages/LorePage.tsx:793:          at the next tick boundary) and <strong>swap scheduled</strong>{' '}
apps/landing/src/pages/LorePage.tsx:794:          (resolve at a specified future tick, useful for arbitrage windows).
apps/landing/src/pages/LorePage.tsx:845:            ticking. The bell will ring shortly.
apps/server/convex/mock.ts:29:      tick: 42,
apps/server/convex/mock.ts:30:      tickEpochStartedAt: Math.floor(Date.now() / 1000) - 42 * 20,
apps/server/convex/mock.ts:31:      tickEpochDurationMs: 20_000,
apps/server/convex/mock.ts:38:      { level: "info" as const, message: "Agent initialized — tick 42" },
apps/landing/src/pages/LorePage.css:296:.tick-wheel-wrap {
apps/landing/src/pages/LorePage.css:302:.tick-wheel {
apps/server/convex/getSnapshot.ts:8:        tick: 0,
apps/server/convex/getSnapshot.ts:9:        tickEpoch: { startedAt: 0, durationMs: 20_000 },
apps/server/convex/getSnapshot.ts:15:      tick: snap.tick,
apps/server/convex/getSnapshot.ts:16:      tickEpoch: {
apps/server/convex/getSnapshot.ts:17:        startedAt: snap.tickEpochStartedAt,
apps/server/convex/getSnapshot.ts:18:        durationMs: snap.tickEpochDurationMs,
apps/landing/public/logos/keeperhub.svg:12:  <!-- hour ticks -->
packages/contracts/AGENTS.md:8:- (Wave 1+) Implements the engine: tick advancement, mission resolution, heartbeat throttle, treasury accounting.
packages/contracts/src/ClanWorld.sol:60:    mapping(uint64 => ScheduledMarketAction[]) private _scheduledMarketActions; // keyed by tick
packages/contracts/src/ClanWorld.sol:64:    mapping(uint64 => bytes32) private _tickSeeds; // tick => seed
packages/contracts/src/ClanWorld.sol:78:    /// @dev Caps market queue work per heartbeat; overflow is deferred to the next tick.
packages/contracts/src/ClanWorld.sol:91:        _world.nextHeartbeatAtTick = 1; // first heartbeat will open tick 1
packages/contracts/src/ClanWorld.sol:93:        // i.e. ticks [100, 110)
packages/contracts/src/ClanWorld.sol:288:    function _addTicksClamped(uint64 tick, uint64 delta) private pure returns (uint64) {
packages/contracts/src/ClanWorld.sol:289:        if (type(uint64).max - tick < delta) return type(uint64).max;
packages/contracts/src/ClanWorld.sol:290:        return tick + delta;
packages/contracts/src/ClanWorld.sol:297:    /// @dev Settle a single clansman's mission for the tick range [fromTick, toTick).
packages/contracts/src/ClanWorld.sol:321:        bytes32 tickSeed;
packages/contracts/src/ClanWorld.sol:322:        for (uint64 tick = fromTick; tick < toTick; tick++) {
packages/contracts/src/ClanWorld.sol:323:            tickSeed = _tickSeeds[tick];
packages/contracts/src/ClanWorld.sol:326:            if (cs.state == ClansmanState.TRAVELING && tick >= m.arrivalTick) {
packages/contracts/src/ClanWorld.sol:329:                emit WorkerArrived(clanId, cs.clansmanId, m.targetRegion, tick);
packages/contracts/src/ClanWorld.sol:339:            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
packages/contracts/src/ClanWorld.sol:340:                _resolveAction(clan, cs, m, clanId, tick, tickSeed);
packages/contracts/src/ClanWorld.sol:346:            // If mission completed during this tick, stop iterating
packages/contracts/src/ClanWorld.sol:361:        // Cap ticks settled per call to prevent block gas limit issues
packages/contracts/src/ClanWorld.sol:369:        // Settle tick by tick from fromTick to curTick - 1
packages/contracts/src/ClanWorld.sol:370:        // (curTick is still open; we settle through the last closed tick)
packages/contracts/src/ClanWorld.sol:371:        for (uint64 tick = fromTick; tick < curTick; tick++) {
packages/contracts/src/ClanWorld.sol:372:            // 1. Apply upkeep for this tick
packages/contracts/src/ClanWorld.sol:373:            _applyUpkeep(clan, tick);
packages/contracts/src/ClanWorld.sol:375:            // 2. Wheat plot regrow check (lazy, per tick)
packages/contracts/src/ClanWorld.sol:378:                if (plot.state == WheatPlotState.Regrowing && tick >= plot.regrowUntilTick) {
packages/contracts/src/ClanWorld.sol:385:            // 3. Advance each clansman (single-tick range: [tick, tick+1))
packages/contracts/src/ClanWorld.sol:388:                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
packages/contracts/src/ClanWorld.sol:396:    /// @dev Apply one tick of upkeep. Marks starvation if insufficient food.
packages/contracts/src/ClanWorld.sol:397:    function _applyUpkeep(Clan storage clan, uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:419:            clan.starvationStartsAtTick = tick;
packages/contracts/src/ClanWorld.sol:420:            emit ClanStarvationChanged(clan.clanId, true, tick);
packages/contracts/src/ClanWorld.sol:423:            emit ClanStarvationChanged(clan.clanId, false, tick);
packages/contracts/src/ClanWorld.sol:432:    /// @dev Resolve one tick of action for a clansman that is in ACTING state.
packages/contracts/src/ClanWorld.sol:438:        uint64 tick,
packages/contracts/src/ClanWorld.sol:439:        bytes32 tickSeed
packages/contracts/src/ClanWorld.sol:445:            _gatherWood(clan, cs, m, clanId, tick, starving, tickSeed);
packages/contracts/src/ClanWorld.sol:447:            _gatherIron(clan, cs, m, clanId, tick, starving, tickSeed);
packages/contracts/src/ClanWorld.sol:449:            _gatherFishDocks(clan, cs, m, clanId, tick, starving, tickSeed);
packages/contracts/src/ClanWorld.sol:451:            _gatherFishDeepSea(clan, cs, m, clanId, tick, starving, tickSeed);
packages/contracts/src/ClanWorld.sol:453:            _gatherWheat(clan, cs, m, clanId, tick, starving);
packages/contracts/src/ClanWorld.sol:455:            _doDeposit(clan, cs, m, clanId, tick);
packages/contracts/src/ClanWorld.sol:465:            _doBuilding(clan, cs, m, clanId, tick, action);
packages/contracts/src/ClanWorld.sol:483:        uint64 tick,
packages/contracts/src/ClanWorld.sol:485:        bytes32 tickSeed
packages/contracts/src/ClanWorld.sol:494:        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
packages/contracts/src/ClanWorld.sol:503:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
packages/contracts/src/ClanWorld.sol:516:        uint64 tick,
packages/contracts/src/ClanWorld.sol:518:        bytes32 tickSeed
packages/contracts/src/ClanWorld.sol:531:        uint256 goldBonus = _rollIronGoldBonus(clan, cs.clansmanId, m.nonce, tick, tickSeed);
packages/contracts/src/ClanWorld.sol:533:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
packages/contracts/src/ClanWorld.sol:540:    function _rollIronGoldBonus(Clan storage clan, uint32 clansmanId, uint64 nonce, uint64 tick, bytes32 tickSeed)
packages/contracts/src/ClanWorld.sol:544:        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
packages/contracts/src/ClanWorld.sol:556:        uint64 tick,
packages/contracts/src/ClanWorld.sol:558:        bytes32 tickSeed
packages/contracts/src/ClanWorld.sol:565:        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
packages/contracts/src/ClanWorld.sol:575:            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
packages/contracts/src/ClanWorld.sol:587:        uint64 tick,
packages/contracts/src/ClanWorld.sol:589:        bytes32 tickSeed
packages/contracts/src/ClanWorld.sol:596:        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
packages/contracts/src/ClanWorld.sol:606:            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
packages/contracts/src/ClanWorld.sol:619:        uint64 tick,
packages/contracts/src/ClanWorld.sol:655:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
packages/contracts/src/ClanWorld.sol:659:            plot.regrowUntilTick = tick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
packages/contracts/src/ClanWorld.sol:668:    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
packages/contracts/src/ClanWorld.sol:697:        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
packages/contracts/src/ClanWorld.sol:706:        uint64 tick,
packages/contracts/src/ClanWorld.sol:717:            success = _tryBuildWall(clan, clanId, tick);
packages/contracts/src/ClanWorld.sol:719:            success = _tryUpgradeBase(clan, clanId, tick);
packages/contracts/src/ClanWorld.sol:721:            success = _tryUpgradeMonument(clan, clanId, tick);
packages/contracts/src/ClanWorld.sol:730:    function _tryBuildWall(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
packages/contracts/src/ClanWorld.sol:760:        emit WallLevelChanged(clanId, old, nextLevel, tick);
packages/contracts/src/ClanWorld.sol:764:    function _tryUpgradeBase(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
packages/contracts/src/ClanWorld.sol:797:        emit BaseLevelChanged(clanId, old, nextLevel, tick);
packages/contracts/src/ClanWorld.sol:801:    function _tryUpgradeMonument(Clan storage clan, uint32 clanId, uint64 tick) internal returns (bool) {
packages/contracts/src/ClanWorld.sol:851:        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
packages/contracts/src/ClanWorld.sol:867:    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
packages/contracts/src/ClanWorld.sol:872:    ///         1. Settle missions completing this tick.
packages/contracts/src/ClanWorld.sol:876:    ///         5. Increment tick and publish (seed already written above).
packages/contracts/src/ClanWorld.sol:887:        _tickSeeds[closedTick] = newSeed;
packages/contracts/src/ClanWorld.sol:890:        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
packages/contracts/src/ClanWorld.sol:903:        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
packages/contracts/src/ClanWorld.sol:911:    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
packages/contracts/src/ClanWorld.sol:912:    ///      Called from heartbeat before market execution and tick increment.
packages/contracts/src/ClanWorld.sol:914:    function _settleCompletingMissions(uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:927:                if (m.settlesAtTick != tick) continue; // not due this tick
packages/contracts/src/ClanWorld.sol:929:                // Settle this mission using the single-tick range [tick, tick+1).
packages/contracts/src/ClanWorld.sol:930:                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
packages/contracts/src/ClanWorld.sol:935:    /// @dev Resolve world events for the tick that was just closed.
packages/contracts/src/ClanWorld.sol:983:    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
packages/contracts/src/ClanWorld.sol:1091:        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
packages/contracts/src/ClanWorld.sol:1092:        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
packages/contracts/src/ClanWorld.sol:1235:        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
packages/contracts/src/ClanWorld.sol:1362:    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
packages/contracts/src/ClanWorld.sol:1363:    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
packages/contracts/src/ClanWorld.sol:1364:    function _executeScheduledMarketActions(uint64 tick) internal {
packages/contracts/src/ClanWorld.sol:1365:        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
packages/contracts/src/ClanWorld.sol:1392:                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
packages/contracts/src/ClanWorld.sol:1401:                    tick,
packages/contracts/src/ClanWorld.sol:1418:            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
packages/contracts/src/ClanWorld.sol:1422:            // Invariant: each tick queue executes in global commitSequence order, including
packages/contracts/src/ClanWorld.sol:1423:            // older overflow actions merged into a tick that already has native actions.
packages/contracts/src/ClanWorld.sol:1427:        delete _scheduledMarketActions[tick];
packages/contracts/src/ClanWorld.sol:1846:    function getScheduledMarketActionsForTick(uint64 tick)
packages/contracts/src/ClanWorld.sol:1852:        return _scheduledMarketActions[tick];
apps/web/AGENTS.md:33:- **`IChainClient`** — used only for direct reads where Convex hasn't indexed yet (e.g., the very first tick before the indexer cron runs). Stub returns hardcoded `tick=0`.
packages/shared/src/mocks/clanWorldFixture.ts:5:// fixture seeds the WORLD STATE (mid-game, tick 80) and a small batch of
packages/shared/src/mocks/clanWorldFixture.ts:10://   1. Aldric (clan 0) is one tick from starvation (vaultWheat 1e18, 2 living
packages/shared/src/mocks/clanWorldFixture.ts:11://      clansmen consume 2e18/tick).
packages/shared/src/mocks/clanWorldFixture.ts:13://      currently CAMPING in Mountains, exits camp tick 81 → attack incoming.
packages/shared/src/mocks/clanWorldFixture.ts:65:  tick: Tick;
packages/shared/src/mocks/clanWorldFixture.ts:111:// Aldric (clan 0) — STARVATION HOOK: 1e18 wheat + 2 clansmen = empties next tick.
packages/shared/src/mocks/clanWorldFixture.ts:121:  starvingCached: false, // about to flip true at tick 81 settlement
packages/shared/src/mocks/clanWorldFixture.ts:193:// World snapshot — currentTick = 80, 20s tick window for Submission 1.
packages/shared/src/mocks/clanWorldFixture.ts:195:  tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:196:  tickEpoch: {
packages/shared/src/mocks/clanWorldFixture.ts:197:    startedAt: Math.floor(Date.now() / 1000) - 5, // current tick window opened ~5s ago
packages/shared/src/mocks/clanWorldFixture.ts:217:// Capped at 240 chars; `postedAt` offsets backwards so logs read in tick order.
packages/shared/src/mocks/clanWorldFixture.ts:223:    tick: 78,
packages/shared/src/mocks/clanWorldFixture.ts:225:      'Wheat reserves critical. Need trade with Mira before tick 81 or workforce collapses.',
packages/shared/src/mocks/clanWorldFixture.ts:232:    tick: 79,
packages/shared/src/mocks/clanWorldFixture.ts:241:    tick: 79,
packages/shared/src/mocks/clanWorldFixture.ts:250:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:259:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:268:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:277:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:290:    text: 'Wheat for wood, 3:1. Three ticks to ship. Take it or starve.',
packages/shared/src/mocks/clanWorldFixture.ts:291:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:297:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:303:    tick: 80,
packages/shared/src/mocks/clanWorldFixture.ts:307:// Bandit — CAMPING in Mountains (Brennan's region). Camp 3 ticks per v4 §6.6;
packages/shared/src/mocks/clanWorldFixture.ts:308:// spawned tick 78 → exits camp tick 81 → ATTACK.
packages/contracts/src/IClanWorld.sol:192:    uint64 nextHeartbeatAtTick;   // estimated tick that will be opened by the next heartbeat (for off-chain UI)
packages/contracts/src/IClanWorld.sol:343:// DERIVED VIEW STRUCTS (read-only, settled forward to current tick)
packages/contracts/src/IClanWorld.sol:347:    Clan clan; // settled to current tick
packages/contracts/src/IClanWorld.sol:354:    Clansman clansman; // settled to current tick
packages/contracts/src/IClanWorld.sol:356:    uint8 effectiveRegion; // for traveling, derived from route + elapsed ticks
packages/contracts/src/IClanWorld.sol:491:    event TickAdvanced(uint64 closedTick, uint64 openedTick, bytes32 tickSeed);
packages/contracts/src/IClanWorld.sol:492:    event WinterStarted(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:493:    event WinterEnded(uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:494:    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds);
packages/contracts/src/IClanWorld.sol:498:        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
packages/contracts/src/IClanWorld.sol:501:    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
packages/contracts/src/IClanWorld.sol:502:    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
packages/contracts/src/IClanWorld.sol:519:    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
packages/contracts/src/IClanWorld.sol:531:        uint64 atTick
packages/contracts/src/IClanWorld.sol:533:    event ResourcesDeposited(
packages/contracts/src/IClanWorld.sol:540:        uint64 atTick
packages/contracts/src/IClanWorld.sol:544:    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
packages/contracts/src/IClanWorld.sol:545:    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
packages/contracts/src/IClanWorld.sol:546:    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
packages/contracts/src/IClanWorld.sol:556:        uint64 atTick
packages/contracts/src/IClanWorld.sol:583:        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
packages/contracts/src/IClanWorld.sol:585:    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
packages/contracts/src/IClanWorld.sol:597:        uint64 atTick
packages/contracts/src/IClanWorld.sol:599:    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
packages/contracts/src/IClanWorld.sol:600:    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
packages/contracts/src/IClanWorld.sol:613:    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
packages/contracts/src/IClanWorld.sol:614:    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
packages/contracts/src/IClanWorld.sol:617:    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
packages/contracts/src/IClanWorld.sol:619:        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
packages/contracts/src/IClanWorld.sol:621:    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
packages/contracts/src/IClanWorld.sol:636:    /// @notice Permissionless heartbeat. Closes the current tick, resolves
packages/contracts/src/IClanWorld.sol:637:    ///         scheduled market actions and world events, advances the tick.
packages/contracts/src/IClanWorld.sol:641:    /// @notice Lazily settle a clan forward to current tick. Idempotent.
packages/contracts/src/IClanWorld.sol:644:    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
packages/contracts/src/IClanWorld.sol:721:    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
packages/contracts/src/IClanWorld.sol:728:    // Derived read getters (read-only simulation forward to current tick)
packages/contracts/src/IClanWorld.sol:766:    ///         scheduled queues for current and next tick. Drives market UI
packages/contracts/src/IClanWorld.sol:767:    ///         and indexer's per-tick price-history sampling.
packages/contracts/src/IClanWorld.sol:771:    ///         attack resolved this tick. Drives the bandit warning UI.
packages/shared/src/adapters/ILLMClient.ts:8:// keys. AnthropicClient here is for non-Elder LLM uses (e.g., the post-tick narrator).
packages/contracts/src/lib/RNG.sol:5:/// @notice Cheap deterministic randomness derived from a published per-tick seed.
packages/contracts/src/ClanWorldStub.sol:42:///         Stores tick state and token/pool addresses. All game logic is no-op.
packages/shared/src/adapters/IConvexClient.ts:17:      tick: 0,
packages/shared/src/adapters/IConvexClient.ts:18:      tickEpoch: { startedAt: 0, durationMs: 20_000 },
packages/shared/src/adapters/IConvexClient.ts:59:        return { tick: 0, tickEpoch: { startedAt: 0, durationMs: 20_000 }, regions: [], clans: [] };
apps/web/src/components/cockpit/MiniCockpit.tsx:18: * Phase A.5b: tick counter moved out of this component and into the global
apps/web/src/components/cockpit/MiniCockpit.tsx:20: * longer takes a `tick` prop.
apps/web/src/WorldMap.tsx:192:  tick: number;
apps/web/src/WorldMap.tsx:288:  const tickerCbRef = useRef<((ticker: { deltaMS: number }) => void) | null>(null);
apps/web/src/WorldMap.tsx:293:  const travelTickerCbRef = useRef<((ticker: { deltaMS: number }) => void) | null>(null);
apps/web/src/WorldMap.tsx:297:  // Zone-pulse ticker (visual heartbeat for clan zone halos).
apps/web/src/WorldMap.tsx:308:  // Derived live tick counter — the worldSnapshot.tick field is currently
apps/web/src/WorldMap.tsx:313:    return Math.max(snapshot?.tick ?? 0, logs.length);
apps/web/src/WorldMap.tsx:314:  }, [logs, snapshot?.tick]);
apps/web/src/WorldMap.tsx:406:        // 3. Speech bubble layer + ticker (PR #43) — added last so bubbles render above everything.
apps/web/src/WorldMap.tsx:411:        const cb = (ticker: { deltaMS: number }) => {
apps/web/src/WorldMap.tsx:455:          void ticker.deltaMS;
apps/web/src/WorldMap.tsx:457:        tickerCbRef.current = cb;
apps/web/src/WorldMap.tsx:458:        app.ticker.add(cb);
apps/web/src/WorldMap.tsx:460:        // 3b. Worker travel layer + ticker (PR #44) — clan-colored dots crossing routes.
apps/web/src/WorldMap.tsx:467:        const travelCb = (_ticker: { deltaMS: number }) => {
apps/web/src/WorldMap.tsx:524:        app.ticker.add(travelCb);
apps/web/src/WorldMap.tsx:526:        // 3c. Zone-pulse ticker — gently breathe alpha on each clan zone halo so
apps/web/src/WorldMap.tsx:539:        app.ticker.add(zonePulseCb);
apps/web/src/WorldMap.tsx:540:        // Stash cleanup ref via the same pattern as travel ticker (re-use travelTickerCbRef).
apps/web/src/WorldMap.tsx:563:      if (a && tickerCbRef.current) a.ticker.remove(tickerCbRef.current);
apps/web/src/WorldMap.tsx:564:      if (a && travelTickerCbRef.current) a.ticker.remove(travelTickerCbRef.current);
apps/web/src/WorldMap.tsx:565:      if (a && zonePulseCbRef.current) a.ticker.remove(zonePulseCbRef.current);
apps/web/src/WorldMap.tsx:573:      tickerCbRef.current = null;
apps/web/src/WorldMap.tsx:830:    // Bottom margin reserves space for the compact tick/level pulse panel.
apps/web/src/WorldMap.tsx:866:      // Big translucent CLAN ZONES — drawn first, breathing animation ticker pulses alpha.
apps/web/src/WorldMap.tsx:867:      // Stored geometry only here; alpha applied per-tick.
apps/web/src/WorldMap.tsx:1005:      // Bandit redraw uses live tick — recompute alongside layout
apps/web/src/WorldMap.tsx:1010:  // ---- Bandit: hooded silhouette over Mountains + ticks-until-attack readout
apps/web/src/WorldMap.tsx:1056:    const tick = snapshot?.tick ?? 0;
apps/web/src/WorldMap.tsx:1057:    const ticksUntil = Math.max(0, DEMO_BANDIT.attacksAtTick - tick);
apps/web/src/WorldMap.tsx:1058:    text.text = `${ticksUntil}t`;
apps/web/src/WorldMap.tsx:1071:  }, [snapshot?.tick, pixiReady]);
apps/web/src/WorldMap.tsx:1082:  // Pulse the bandit icon when 1-2 ticks from attacking — urgency cue for the demo
apps/web/src/WorldMap.tsx:1087:    const tick = snapshot?.tick ?? 0;
apps/web/src/WorldMap.tsx:1088:    const ticksUntil = DEMO_BANDIT.attacksAtTick - tick;
apps/web/src/WorldMap.tsx:1089:    const shouldPulse = ticksUntil <= 2 && ticksUntil >= 0;
apps/web/src/WorldMap.tsx:1104:    app.ticker.add(onTick);
apps/web/src/WorldMap.tsx:1106:      app.ticker.remove(onTick);
apps/web/src/WorldMap.tsx:1112:  }, [snapshot?.tick, pixiReady]);
apps/web/src/WorldMap.tsx:1317:          gives a glance of the world tick + a tight per-clan summary line.
apps/web/src/WorldMap.tsx:1340:            tick {liveTick}
apps/web/src/WorldMap.tsx:1408: * The tail is a separate Graphics object so the ticker can hide it on
apps/web/src/WorldMap.tsx:1476:  // Graphics so the ticker can hide it on non-bottom (stacked) bubbles —
packages/shared/src/adapters/IChainClient.ts:286:    return Number(snapshot.currentTick); // safe: tick values are small enough to fit Number precisely in Wave 0
apps/web/src/components/cockpit/CockpitHeader.tsx:6: * Phase A.5b stub: returns hardcoded tick so the header has something to show.
apps/web/src/components/cockpit/CockpitHeader.tsx:9: * so the value updates live as the engine ticks. The pulse animation in
apps/web/src/components/cockpit/CockpitHeader.tsx:12:function useCurrentTick(): { tick: number; tickMax: number } {
apps/web/src/components/cockpit/CockpitHeader.tsx:13:  return { tick: 4, tickMax: 10 };
apps/web/src/components/cockpit/CockpitHeader.tsx:18: * to repeat the tick counter four times. Single instance now.
apps/web/src/components/cockpit/CockpitHeader.tsx:20: * Layout: title left, tick counter middle-right, connection pill far right.
apps/web/src/components/cockpit/CockpitHeader.tsx:23:  const { tick, tickMax } = useCurrentTick();
apps/web/src/components/cockpit/CockpitHeader.tsx:60:        <TickCounter tick={tick} tickMax={tickMax} />
apps/web/src/components/cockpit/CockpitHeader.tsx:68:  tick: number;
apps/web/src/components/cockpit/CockpitHeader.tsx:69:  tickMax: number;
apps/web/src/components/cockpit/CockpitHeader.tsx:74: * Uses a ref-tracked previous tick so the flash fires only on actual change,
apps/web/src/components/cockpit/CockpitHeader.tsx:77:function TickCounter({ tick, tickMax }: TickProps) {
apps/web/src/components/cockpit/CockpitHeader.tsx:79:  const prevTickRef = useRef(tick);
apps/web/src/components/cockpit/CockpitHeader.tsx:82:    if (tick === prevTickRef.current) return;
apps/web/src/components/cockpit/CockpitHeader.tsx:83:    prevTickRef.current = tick;
apps/web/src/components/cockpit/CockpitHeader.tsx:87:  }, [tick]);
apps/web/src/components/cockpit/CockpitHeader.tsx:91:      data-testid="cockpit-header-tick"
apps/web/src/components/cockpit/CockpitHeader.tsx:111:      title="Engine tick"
apps/web/src/components/cockpit/CockpitHeader.tsx:114:      <span>{tick}</span>
apps/web/src/components/cockpit/CockpitHeader.tsx:116:      <span style={{ opacity: 0.6 }}>{tickMax}</span>
packages/contracts/abi/IClanWorld.json:1693:          "name": "tick",
packages/contracts/abi/IClanWorld.json:2543:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2568:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2587:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2618:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2686:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2717:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2773:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2792:          "name": "tick",
packages/contracts/abi/IClanWorld.json:2848:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2873:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2892:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2923:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:2954:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3003:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3231:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3272:      "name": "ResourcesDeposited",
packages/contracts/abi/IClanWorld.json:3311:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3372:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3495:          "name": "tick",
packages/contracts/abi/IClanWorld.json:3526:          "name": "tickSeed",
packages/contracts/abi/IClanWorld.json:3563:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3594:          "name": "atTick",
packages/contracts/abi/IClanWorld.json:3607:          "name": "tick",
packages/contracts/abi/IClanWorld.json:3620:          "name": "tick",
packages/contracts/abi/IClanWorld.json:3651:          "name": "tick",
packages/shared/src/types.ts:8:  /** Unix seconds when this tick window started. */
packages/shared/src/types.ts:10:  /** Duration of one tick in milliseconds. 20000 for Submission 1, 60000 for Submission 2 live. */
packages/shared/src/types.ts:28:  tick: Tick;
packages/shared/src/types.ts:29:  tickEpoch: TickEpoch;
packages/shared/src/types.ts:37:  /** Pending orders this clan submitted for the next tick. */
packages/shared/src/types.ts:52:  tick: Tick;
apps/web/src/components/cockpit/shared/CockpitTabBar.tsx:35: * Phase A.5b: tick counter removed from per-tab bar; it now lives on the
packages/contracts/test/ClanWorldStub.t.sol:27:    function test_heartbeat_increments_tick() public {
packages/contracts/test/ClanWorldStub.t.sol:33:    function test_getWorldSnapshot_returns_current_tick() public {
packages/contracts/test/MissionTiming.t.sol:78:        assertEq(mission.submittedAtTick, submittedAt, "submitted tick");
packages/contracts/test/MissionTiming.t.sol:79:        assertEq(mission.executesAtTick, submittedAt + travel, "executes tick");
packages/contracts/test/MissionTiming.t.sol:80:        assertEq(mission.settlesAtTick, submittedAt + travel + duration, "settles tick");
packages/contracts/test/MissionTiming.t.sol:82:        assertEq(mission.startTick, mission.submittedAtTick, "legacy start tick mirrors submitted");
packages/contracts/test/MissionTiming.t.sol:100:        assertEq(mission.settlesAtTick, mission.executesAtTick + 4, "four tick action duration");
packages/contracts/test/MissionTiming.t.sol:164:        assertEq(firstSubmitted, 0, "first submit starts at tick 0");
packages/contracts/test/MissionTiming.t.sol:165:        assertEq(secondSubmitted, world.getWorldState().currentTick, "second submit uses current tick");
packages/contracts/test/HeartbeatOrdering.t.sol:171:    // heartbeat closing tick T:
packages/contracts/test/HeartbeatOrdering.t.sol:173:    //     = 1 tick travel. arrivalTick = T0+1, settlesAtTick = T0+2.
packages/contracts/test/HeartbeatOrdering.t.sol:174:    //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
packages/contracts/test/HeartbeatOrdering.t.sol:175:    //     executeAtTick = T0+2. (Same tick as cs0 settles.)
packages/contracts/test/HeartbeatOrdering.t.sol:190:        // Deposit from Mountains to Forest: travel = 1 tick.
packages/contracts/test/HeartbeatOrdering.t.sol:199:        // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
packages/contracts/test/HeartbeatOrdering.t.sol:200:        // executeAtTick = t0+2. Same tick as deposit settles.
packages/contracts/test/HeartbeatOrdering.t.sol:209:        assertEq(world.getClan(clanId).vaultWood, 0, "vault wood must be 0 before test tick");
packages/contracts/test/HeartbeatOrdering.t.sol:213:        // Advance to tick t0+2. The heartbeat closing t0+2 runs:
packages/contracts/test/HeartbeatOrdering.t.sol:233:        uint64 tickBefore = before_.currentTick;
packages/contracts/test/HeartbeatOrdering.t.sol:236:        bytes32 expectedSeed = keccak256(abi.encode(block.prevrandao, seedBefore, tickBefore));
packages/contracts/test/HeartbeatOrdering.t.sol:240:        assertEq(after_.currentTick, tickBefore + 1, "tick must increment by 1");
packages/contracts/test/HeartbeatOrdering.t.sol:245:        uint64 tick2 = after_.currentTick;
packages/contracts/test/HeartbeatOrdering.t.sol:248:        bytes32 expectedSeed2 = keccak256(abi.encode(block.prevrandao, seed2, tick2));
packages/contracts/test/HeartbeatOrdering.t.sol:251:        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
packages/contracts/test/HeartbeatOrdering.t.sol:260:    //   - currentSeasonNumber increments on the heartbeat that closes tick
packages/contracts/test/HeartbeatOrdering.t.sol:277:        assertEq(ws1.currentTick, ClanWorldConstants.SEASON_DURATION_TICKS, "tick must be at season boundary");
packages/contracts/test/HeartbeatOrdering.t.sol:291:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/HeartbeatOrdering.t.sol:296:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "tick must increment");
packages/contracts/test/HeartbeatOrdering.t.sol:303:    // Proves that when a mission settles at tick T AND a market action is queued at
packages/contracts/test/HeartbeatOrdering.t.sol:304:    // tick T, both fire in the same heartbeat without conflict.
packages/contracts/test/HeartbeatOrdering.t.sol:317:        // cs0: placed at Mountains (1 tick from Forest homebase).
packages/contracts/test/HeartbeatOrdering.t.sol:325:        // cs1: at Forest, sells wood to UT. Forest→UT = 2 ticks. executeAtTick = t0+2.
packages/contracts/test/HeartbeatOrdering.t.sol:333:        // Advance to tick t0+3 (the heartbeat closing t0+2 runs step1+step2 together).
packages/contracts/test/Reentrancy.t.sol:128:        uint256 ticksNeeded = uint256(mission.actionStartTick - currentTick) + 1;
packages/contracts/test/Reentrancy.t.sol:129:        for (uint256 i = 0; i < ticksNeeded; i++) {
packages/contracts/test/ClanWorld.t.sol:163:    function test_heartbeat_tickIncrementsExactlyOne() public {
packages/contracts/test/ClanWorld.t.sol:164:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:168:        uint64 tickAfter = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:169:        assertEq(tickAfter - tickBefore, 1, "heartbeat should advance exactly one tick");
packages/contracts/test/ClanWorld.t.sol:173:    // Test 2: tick seed changes after heartbeat
packages/contracts/test/ClanWorld.t.sol:185:        assertEq(afterFirst.currentTickSeed, expectedFirstSeed, "first seed should use closed tick and prior seed");
packages/contracts/test/ClanWorld.t.sol:203:        uint64 tickBefore = world.getWorldState().currentTick;
packages/contracts/test/ClanWorld.t.sol:208:        assertEq(world.getWorldState().currentTick, tickBefore + 1, "non-owner caller should advance heartbeat");
packages/contracts/test/ClanWorld.t.sol:223:            assertEq(state.currentTick, previousTick + 1, "each heartbeat should advance exactly one tick");
packages/contracts/test/ClanWorld.t.sol:390:        // Submit MineIron mission to Mountains (region 2) — requires at least 1 travel tick from Forest(1)
packages/contracts/test/ClanWorld.t.sol:403:        // Advance until the arrival tick and four-tick action duration have both closed.
packages/contracts/test/ClanWorld.t.sol:404:        uint256 ticksToAdvance = uint256(travelTicks) + world.getActionDuration(ActionType.MineIron) + 1;
packages/contracts/test/ClanWorld.t.sol:405:        for (uint256 i = 0; i < ticksToAdvance; i++) {
packages/contracts/test/ClanWorld.t.sol:435:        // Advance through travel and the four-tick mining duration.
packages/contracts/test/ClanWorld.t.sol:469:        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
packages/contracts/test/ClanWorld.t.sol:470:        assertEq(ticks, 0, "out-of-range src should return 0 ticks");
packages/contracts/test/ClanWorld.t.sol:473:        (ticks, path) = world.quoteTravel(1, 9);
packages/contracts/test/ClanWorld.t.sol:474:        assertEq(ticks, 0, "out-of-range dst should return 0 ticks");
packages/contracts/test/ClanWorld.t.sol:485:        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 9);
packages/contracts/test/ClanWorld.t.sol:486:        assertEq(ticks, 0, "quoteTravel(9,9): travelTicks must be 0");
packages/contracts/test/ClanWorld.t.sol:491:    // Test B: submitClanOrders returns ERR_INVALID_ACTION when clan is >200 ticks behind
packages/contracts/test/ClanWorld.t.sol:499:        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
packages/contracts/test/ClanWorld.t.sol:523:            "clan >200 ticks behind must return ERR_INVALID_ACTION (settle-first guard)"
packages/contracts/test/ClanWorld.t.sol:545:        // Advance tick so heartbeat is valid, then submit interrupt mission
packages/contracts/test/ClanWorld.t.sol:635:        // Find out which tick the action fires
packages/contracts/test/ClanWorld.t.sol:639:        // Advance ticks until heartbeat closes executeAtTick
packages/contracts/test/ClanWorld.t.sol:641:        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
packages/contracts/test/ClanWorld.t.sol:642:        for (uint256 i = 0; i < ticksNeeded; i++) {
packages/contracts/test/ClanWorld.t.sol:677:        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
packages/contracts/test/ClanWorld.t.sol:678:        for (uint256 i = 0; i < ticksNeeded; i++) {
packages/contracts/test/ClanWorld.t.sol:707:        // Advance all ticks UP TO (but not including) the execute tick
packages/contracts/test/ClanWorld.t.sol:745:        uint256 ticksNeeded = uint256(executeAtTick - curTick) + 1;
packages/contracts/test/ClanWorld.t.sol:746:        for (uint256 i = 0; i < ticksNeeded; i++) {
packages/contracts/test/ClanWorld.t.sol:782:        _advanceTick(); // close tick before the stale entry
packages/contracts/test/ClanWorld.t.sol:786:        _advanceTick(); // close stale entry tick
packages/contracts/test/ClanWorld.t.sol:789:        _advanceTick(); // close replacement entry tick
packages/contracts/test/ClanWorld.t.sol:828:            "all aligned actions should share tick 2"
packages/contracts/test/ClanWorld.t.sol:831:        _advanceTick(); // close tick 1
packages/contracts/test/ClanWorld.t.sol:832:        _advanceTick(); // close tick 2, process cap and defer remainder
packages/contracts/test/ClanWorld.t.sol:834:        assertEq(world.getScheduledMarketActionsForTick(executeAtTick).length, 0, "original tick queue cleared");
packages/contracts/test/ClanWorld.t.sol:838:            "overflow actions deferred to next tick"
packages/contracts/test/ClanWorld.t.sol:841:        _advanceTick(); // close tick 3, process deferred actions
packages/contracts/test/ClanWorld.t.sol:881:        assertEq(uint8(nativeResult[0].status), uint8(StatusCode.OK), "native next-tick action should enqueue");
packages/contracts/test/ClanWorld.t.sol:889:        assertEq(nativeQueueBefore.length, 1, "native next-tick queue should exist before overflow merge");
packages/contracts/test/ClanWorld.t.sol:892:        _advanceTick(); // close tick 1
packages/contracts/test/ClanWorld.t.sol:893:        _advanceTick(); // close tick 2, process cap and defer overflow into tick 3
packages/contracts/test/ClanWorld.t.sol:897:        assertEq(mergedQueue.length, overflowCount + 1, "overflow should merge with native next-tick action");
packages/contracts/test/ClanWorld.t.sol:910:        _advanceTick(); // close tick 3 and execute the sorted merged queue
packages/contracts/test/ClanWorld.t.sol:983:        // Check commitSequence in the queues for each clan's respective tick
packages/contracts/test/ClanWorld.t.sol:997:        // Advance ticks to cover both actions
packages/contracts/test/ClanWorld.t.sol:1000:        uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
packages/contracts/test/ClanWorld.t.sol:1001:        for (uint256 i = 0; i < ticksNeeded; i++) {
packages/contracts/test/ClanWorld.t.sol:1057:        uint256 ticksNeeded = uint256(maxTick - curTick) + 1;
packages/contracts/test/ClanWorld.t.sol:1058:        for (uint256 i = 0; i < ticksNeeded; i++) {
packages/contracts/test/ClanWorld.t.sol:1296:        // Send clansman to homebase to DepositResources (empty carry → completes in 1 tick).
packages/contracts/test/ClanWorld.t.sol:1297:        // This is the cleanest single-tick completion: _doDeposit with empty carry calls _completeMission immediately.
packages/contracts/test/ClanWorld.t.sol:1301:        // Wait for submit-time cooldown to expire (warp only; no ticks yet)
packages/contracts/test/ClanWorld.t.sol:1304:        // Advance until the one-tick deposit duration has closed.
packages/contracts/test/ClanWorld.t.sol:1396:        // Run 3 more settle ticks — each calls _resolveAction for DefendBase
packages/contracts/test/ClanWorld.t.sol:1405:                "DefendBase must not slide cooldown: _completeMission must not be called per tick"
packages/contracts/test/ClanWorld.t.sol:1414:            "DefendBase clansman must remain ACTING after 3 settlement ticks"
packages/contracts/test/ClanWorld.t.sol:1510:    // Helper: advance tick on a harness-based world
packages/contracts/test/ClanWorld.t.sol:1527:        // Travel from Forest(1) to Mountains(2) is 1 tick — clansman starts TRAVELING
packages/contracts/test/ClanWorld.t.sol:1529:        assertGt(travelTicks, 0, "path1: need travel > 0 ticks");
packages/contracts/test/ClanWorld.t.sol:1539:        // Call settleClansman without advancing any ticks (same tick as submission)
packages/contracts/test/ClanWorld.t.sol:1545:            uint8(csAfter.state), uint8(ClansmanState.TRAVELING), "path1: must still be TRAVELING (no ticks passed)"
packages/contracts/test/ClanWorld.t.sol:1561:        // Travel Forest(1) → Mountains(2) = 1 tick
packages/contracts/test/ClanWorld.t.sol:1563:        assertGt(travelTicks, 0, "path2: need travel > 0 ticks");
packages/contracts/test/ClanWorld.t.sol:1569:        // i.e. currentTick >= arrivalTick + 1, so advance travelTicks + 1 ticks.
packages/contracts/test/ClanWorld.t.sol:1585:    // Path 3: ACTING + tick >= settlesAtTick → resolves action
packages/contracts/test/ClanWorld.t.sol:1597:        // Advance through travel and the four-tick mining duration.
packages/contracts/test/ClanWorld.t.sol:1622:        // 4 ticks of travel (Forest→Mountains→UnicornTown→EastFarms→EastDocks), ensuring the
packages/contracts/test/ClanWorld.t.sol:1623:        // clansman is still TRAVELING when interrupted after just 1 tick.
packages/contracts/test/ClanWorld.t.sol:1625:        assertGt(travelTicks1, 1, "path4: first mission needs > 1 tick travel so it stays TRAVELING after 1 tick");
packages/contracts/test/ClanWorld.t.sol:1632:        // Advance only 1 tick — first mission still TRAVELING (arrivalTick is 4 ticks away)
packages/contracts/test/ClanWorld.t.sol:1633:        // Wait for cooldown and advance 1 tick, then interrupt
packages/contracts/test/ClanWorld.t.sol:1656:        // Advance until new mission resolves (from current position after 1 tick)
packages/contracts/test/ClanWorld.t.sol:1697:        // Advance 5 ticks and settle
packages/contracts/test/ClanWorld.t.sol:1719:        // From Forest homebase, go to Mountains (1 tick travel)
packages/contracts/test/ClanWorld.t.sol:1805:        // Advance through travel and the four-tick mining duration WITHOUT calling settleClan.
packages/contracts/test/ClanWorld.t.sol:1811:        // carryIron is already > 0 after tick advancement — no manual settleClansman required.
packages/contracts/test/ClanWorld.t.sol:1830:        // No ticks advanced — clan is already up to date
packages/contracts/test/ClanWorld.t.sol:1931:        // winterStartsAtTick = 100; WinterStarted fires on the heartbeat that opens tick 100
packages/contracts/test/ClanWorld.t.sol:1933:        // Advance 99 heartbeats so currentTick=99, then one more heartbeat fires WinterStarted at tick 100.
packages/contracts/test/ClanWorld.t.sol:1940:        // currentTick == 99; next heartbeat opens tick 100 and should emit WinterStarted(100)
packages/contracts/test/ClanWorld.t.sol:1954:        assertTrue(foundWinterStarted, "WinterStarted event should have been emitted at tick 100");
packages/contracts/test/ClanWorld.t.sol:1960:        // Advance past first winter end tick (= 110)
packages/contracts/test/ClanWorld.t.sol:1991:    function test_nextHeartbeatAtTick_tracks_tick() public {
packages/contracts/test/ClanWorld.t.sol:1993:        assertEq(ws0.nextHeartbeatAtTick, 1, "before first heartbeat, next = tick 1");
packages/contracts/test/ClanWorld.t.sol:1998:        assertEq(ws1.nextHeartbeatAtTick, 2, "after tick 1, next = tick 2");
packages/contracts/test/DefendBase.t.sol:115:        assertEq(mission.startTick, submittedAtTick, "start tick");
packages/contracts/test/DefendBase.t.sol:116:        assertEq(mission.arrivalTick, submittedAtTick, "arrival tick");
packages/contracts/test/DefendBase.t.sol:117:        assertEq(mission.actionStartTick, submittedAtTick, "action tick");
apps/web/src/components/cockpit/tabs/CommsTab.tsx:12:  tick: number;
apps/web/src/components/cockpit/tabs/CommsTab.tsx:27:  { tick: 4, kind: 'orch',    speaker: 'orchestrator', body: 'Tick T04 begun. Yield <directives>.' },
apps/web/src/components/cockpit/tabs/CommsTab.tsx:28:  { tick: 4, kind: 'whisper', speaker: 'clan-3',       body: 'AXL: "trade ore for wood, 2:1?"' },
apps/web/src/components/cockpit/tabs/CommsTab.tsx:29:  { tick: 3, kind: 'human',   speaker: 'liam',         body: 'Slow your raids — diplomacy first.' },
apps/web/src/components/cockpit/tabs/CommsTab.tsx:30:  { tick: 3, kind: 'whisper', speaker: 'clan-2',       body: 'AXL: declined; counter offered 3:1.' },
apps/web/src/components/cockpit/tabs/CommsTab.tsx:31:  { tick: 2, kind: 'orch',    speaker: 'orchestrator', body: 'Bandit camp surfaced at forest.' },
apps/web/src/components/cockpit/tabs/CommsTab.tsx:128:                <span style={{ color: tokens.text.muted }}>T{l.tick}</span>
apps/web/src/components/cockpit/tabs/VaultTab.tsx:19:  { tick: 4, type: 'gain',  amount: '+45 gold',  source: 'raid · forest' },
apps/web/src/components/cockpit/tabs/VaultTab.tsx:20:  { tick: 3, type: 'spend', amount: '-12 wood',  source: 'mill upkeep'   },
apps/web/src/components/cockpit/tabs/VaultTab.tsx:21:  { tick: 2, type: 'gain',  amount: '+8 stone',  source: 'quarry'        },
apps/web/src/components/cockpit/tabs/VaultTab.tsx:22:  { tick: 1, type: 'gain',  amount: '+24 gold',  source: 'tribute'       },
apps/web/src/components/cockpit/tabs/VaultTab.tsx:132:              <span style={{ color: tokens.text.muted, width: '32px' }}>T{m.tick}</span>
apps/web/tests/e2e/04-cockpit-shell.spec.ts:12: *   - app-level header has tick counter + connection pill (Phase A.5b)
apps/web/tests/e2e/04-cockpit-shell.spec.ts:15: *   - tab switching, tick counter live updates
apps/web/tests/e2e/04-cockpit-shell.spec.ts:68:    // Phase A.5b: tick counter is now app-level (single instance), not
apps/web/tests/e2e/04-cockpit-shell.spec.ts:70:    await expect(page.locator('[data-testid="cockpit-header-tick"]')).toBeVisible();
apps/web/tests/e2e/04-cockpit-shell.spec.ts:71:    await expect(page.locator('[data-testid="cockpit-header-tick"]')).toHaveCount(1);
packages/agents/src/__tests__/cli.test.ts:13:      tick: 42,
packages/agents/src/__tests__/cli.test.ts:14:      tickEpoch: { startedAt: 1000, durationMs: 20_000 },
packages/agents/src/__tests__/cli.test.ts:61:    expect(parsed.tick).toBe(42);
apps/web/tests/e2e/02-demo-mode.spec.ts:73:    // Scoreboard tick panel must NOT be visible (only shown in DEMO_MODE)
packages/agents/src/seams/IRunnerInbox.ts:4: * The runner pushes per-tick situation blocks into the Elder's tmux session;
packages/agents/src/seams/IRunnerInbox.ts:7: * twice for the same tick must not cause duplicate processing.
packages/agents/src/seams/IRunnerInbox.ts:11:   * Deliver a per-tick situation block to the Elder.
packages/agents/src/seams/IRunnerInbox.ts:19:   * - Idempotent: re-delivering tick N's block while the Elder is still processing tick N
packages/agents/src/seams/IRunnerInbox.ts:20:   *   must be a no-op (same tick, same block = skip).
packages/agents/src/seams/IRunnerInbox.ts:22:  deliverSituationBlock(tick: number, block: string, signal?: AbortSignal): Promise<DeliveryStatus>;
packages/agents/src/seams/IRunnerInbox.ts:28:   * 1. Runner sends final-tick warning to Elder.
packages/agents/src/seams/IRunnerInbox.ts:36:   * - Must never deadlock the tick loop.
packages/agents/src/seams/IRunnerInbox.ts:43:  | { ok: false; reason: 'session-down' | 'timeout' | 'duplicate-tick' | 'aborted' };
apps/web/tests/e2e/03-message-bubbles.spec.ts:8://   1. world events       — parchment-style global ticks / chain heartbeat
apps/web/tests/e2e/03-message-bubbles.spec.ts:16:// the feed by tick + intra-tick sequence.
apps/web/src/components/cockpit/tabs/ZeroGTab.tsx:17:  { tick: 4, op: 'WRITE', key: 'mood',         note: 'cautious → wary'   },
apps/web/src/components/cockpit/tabs/ZeroGTab.tsx:18:  { tick: 3, op: 'READ',  key: 'last_grudge',  note: 'planning retort'   },
apps/web/src/components/cockpit/tabs/ZeroGTab.tsx:19:  { tick: 2, op: 'WRITE', key: 'wood_threshold', note: 'raise to 80'     },
apps/web/src/components/cockpit/tabs/ZeroGTab.tsx:20:  { tick: 1, op: 'READ',  key: 'pref_target',  note: 'mission seeding'   },
apps/web/src/components/cockpit/tabs/ZeroGTab.tsx:122:                <span style={{ color: tokens.text.muted }}>T{c.tick}</span>
packages/agents/src/seams/IElderPeerInbox.ts:8: * - Messages are ordered (FIFO) within a sender/recipient pair per tick.
packages/agents/src/seams/IElderPeerInbox.ts:14: *   implementations that guarantee exactly-once must deduplicate by (fromClanId, tick, msgId).
packages/agents/src/seams/IElderPeerInbox.ts:22:   * @param tick     - current game tick (used for ordering and deduplication)
packages/agents/src/seams/IElderPeerInbox.ts:24:  send(toClanId: string, message: string, tick: number): Promise<void>;
packages/agents/src/seams/IElderPeerInbox.ts:42:  tick: number;
apps/web/src/hooks/useConnectionStatus.ts:46: * `disconnected` state — that state is sticky until the user clicks
apps/web/src/hooks/useConnectionStatus.ts:69:  // the timer chain on each tick).
apps/web/src/hooks/useConnectionStatus.ts:140:      // Don't auto-revive from disconnected — that state is sticky until
apps/web/src/styles/cockpit-tokens.ts:31:    accent: '#d4a544',        // active tab / tick counter glow
packages/agents/src/seams/IElderMemoryStore.ts:24:   * - Must complete before the tick loop continues (caller does not fire-and-forget).
packages/agents/src/seams/IElderMemoryStore.ts:31:   * Used by the runner to compose the continuity summary block on final-tick warning.
packages/agents/test/cli.test.ts:14:  tick: 7,
packages/agents/test/cli.test.ts:15:  tickEpoch: { startedAt: 1700000000, durationMs: 60000 },
packages/agents/test/cli.test.ts:78:  it('returns JSON-parseable WorldSnapshot with correct tick', async () => {
packages/agents/test/cli.test.ts:81:    expect(parsed.tick).toBe(7);
apps/web/src/pages/Cockpit.tsx:17: * Phase A.5b: terminal tabs render a live ttyd iframe; tick counter +
apps/web/src/pages/Cockpit.tsx:23: * Phase B (separate PR) wires real Convex data + tick subscription.

exec
/bin/bash -lc 'rg -n "ResourcesDeposited|ResourcesGathered|MissionCompleted|StatusCode|ERR_NOT_AT_HOMEBASE|ERR_INVALID_REGION|DepositResources" apps/server packages/shared apps/web packages/agents packages/runner -S' in /home/claude/code/clan-world
 exited 1 in 0ms:
exec
/bin/bash -lc 'rg -n "interface IClanWorldEvents|typechain|abi|decodeEventLog|parseLog|ResourcesDeposited" packages/contracts apps/server packages/shared -S' in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:494:        bytes32 critRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
packages/contracts/src/ClanWorld.sol:544:        bytes32 goldRng = keccak256(abi.encode("iron_gold_bonus", tickSeed, clansmanId, nonce, tick));
packages/contracts/src/ClanWorld.sol:565:        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
packages/contracts/src/ClanWorld.sol:596:        bytes32 fishRng = keccak256(abi.encode("fish_roll", tickSeed, cs.clansmanId, m.nonce, tick));
packages/contracts/src/ClanWorld.sol:697:        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
packages/contracts/src/ClanWorld.sol:886:        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
packages/contracts/src/ClanWorld.sol:1283:        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
packages/contracts/src/IClanWorld.sol:489:interface IClanWorldEvents {
packages/contracts/src/IClanWorld.sol:533:    event ResourcesDeposited(
packages/contracts/src/lib/RNG.sol:19:        return uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce)));
packages/contracts/src/lib/RNG.sol:38:            uint256 value = uint256(keccak256(abi.encodePacked(domainSalt, seed, nonce, max, attempt)));
packages/contracts/src/lib/RNG.sol:51:    /// @notice Returns true with 50% probability.
packages/contracts/src/lib/RNG.sol:103:            uint256 stepNonce = uint256(keccak256(abi.encodePacked(nonce, n, i)));
packages/contracts/abi/IClanWorld.json:2:  "abi": [
packages/contracts/abi/IClanWorld.json:8:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:103:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:122:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:233:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:267:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:286:      "stateMutability": "pure"
packages/contracts/abi/IClanWorld.json:310:      "stateMutability": "pure"
packages/contracts/abi/IClanWorld.json:329:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:410:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:521:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1000:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1071:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1090:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1223:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1408:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1640:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1686:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1752:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1832:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:1900:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:2007:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:2087:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:2094:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2112:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2136:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2155:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:2174:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:2203:      "stateMutability": "view"
packages/contracts/abi/IClanWorld.json:2258:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2271:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2354:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2377:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2425:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2448:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:2476:      "stateMutability": "nonpayable"
packages/contracts/abi/IClanWorld.json:3272:      "name": "ResourcesDeposited",
packages/shared/src/adapters/IChainClient.ts:61:    stateMutability: 'view',
packages/shared/src/adapters/IChainClient.ts:203:    stateMutability: 'view',
packages/shared/src/adapters/IChainClient.ts:236:    stateMutability: 'nonpayable',
packages/shared/src/adapters/IChainClient.ts:283:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:369:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:380:      abi: CLAN_WORLD_ABI,
packages/contracts/test/HeartbeatOrdering.t.sol:236:        bytes32 expectedSeed = keccak256(abi.encode(block.prevrandao, seedBefore, tickBefore));
packages/contracts/test/HeartbeatOrdering.t.sol:248:        bytes32 expectedSeed2 = keccak256(abi.encode(block.prevrandao, seed2, tick2));
packages/contracts/test/RNG.t.sol:208:            uint256 firstSample = uint256(keccak256(abi.encodePacked(DOMAIN_A, SEED, nonce, max, uint256(0))));
packages/contracts/test/ClanWorld.t.sol:181:            keccak256(abi.encode(block.prevrandao, beforeFirst.currentTickSeed, beforeFirst.currentTick));
packages/contracts/test/ClanWorld.t.sol:190:            keccak256(abi.encode(block.prevrandao, afterFirst.currentTickSeed, afterFirst.currentTick));

exec
/bin/bash -lc "git show 9ccf94a:packages/shared/src/adapters/IChainClient.ts | nl -ba | sed -n '1,420p'" in /home/claude/code/clan-world
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
   151	                  { name: 'effectiveRegion', type: 'uint8' },
   152	                  { name: 'derivedAtTick', type: 'uint64' },
   153	                ],
   154	              },
   155	              {
   156	                name: 'activeMission',
   157	                type: 'tuple',
   158	                components: [
   159	                  { name: 'active', type: 'bool' },
   160	                  { name: 'nonce', type: 'uint64' },
   161	                  { name: 'clansmanId', type: 'uint32' },
   162	                  { name: 'startRegion', type: 'uint8' },
   163	                  { name: 'targetRegion', type: 'uint8' },
   164	                  { name: 'action', type: 'uint8' },
   165	                  { name: 'startTick', type: 'uint64' },
   166	                  { name: 'arrivalTick', type: 'uint64' },
   167	                  { name: 'actionStartTick', type: 'uint64' },
   168	                  { name: 'missionSeed', type: 'bytes32' },
   169	                  { name: 'marketMode', type: 'uint8' },
   170	                  { name: 'targetClanId', type: 'uint32' },
   171	                  { name: 'marketToken', type: 'address' },
   172	                  { name: 'marketAmount', type: 'uint256' },
   173	                  { name: 'maxGoldIn', type: 'uint256' },
   174	                ],
   175	              },
   176	            ],
   177	          },
   178	          {
   179	            name: 'westPlot',
   180	            type: 'tuple',
   181	            components: [
   182	              { name: 'state', type: 'uint8' },
   183	              { name: 'region', type: 'uint8' },
   184	              { name: 'remainingWheat', type: 'uint256' },
   185	              { name: 'regrowUntilTick', type: 'uint64' },
   186	            ],
   187	          },
   188	          {
   189	            name: 'eastPlot',
   190	            type: 'tuple',
   191	            components: [
   192	              { name: 'state', type: 'uint8' },
   193	              { name: 'region', type: 'uint8' },
   194	              { name: 'remainingWheat', type: 'uint256' },
   195	              { name: 'regrowUntilTick', type: 'uint64' },
   196	            ],
   197	          },
   198	          { name: 'incomingDefenderIds', type: 'uint32[]' },
   199	          { name: 'thisClanDefendingBaseId', type: 'uint32' },
   200	        ],
   201	      },
   202	    ],
   203	    stateMutability: 'view',
   204	  },
   205	  {
   206	    name: 'submitClanOrders',
   207	    type: 'function',
   208	    inputs: [
   209	      { name: 'clanId', type: 'uint32' },
   210	      {
   211	        name: 'orders',
   212	        type: 'tuple[]',
   213	        components: [
   214	          { name: 'clansmanId', type: 'uint32' },
   215	          { name: 'gotoRegion', type: 'uint8' },
   216	          { name: 'action', type: 'uint8' },
   217	          { name: 'targetClanId', type: 'uint32' },
   218	          { name: 'marketToken', type: 'address' },
   219	          { name: 'marketAmount', type: 'uint256' },
   220	          { name: 'maxGoldIn', type: 'uint256' },
   221	        ],
   222	      },
   223	    ],
   224	    outputs: [
   225	      {
   226	        name: 'results',
   227	        type: 'tuple[]',
   228	        components: [
   229	          { name: 'clansmanId', type: 'uint32' },
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
   326	    const keyPath = readEnv('ELDER_WALLET_KEY_PATH');
   327	    let pk: string | undefined;
   328	    let pkSource: string | undefined;
   329	    if (keyPath) {
   330	      try {
   331	        pk = fs.readFileSync(keyPath, 'utf8').trim();
   332	        pkSource = `ELDER_WALLET_KEY_PATH file at ${keyPath}`;
   333	      } catch (err) {
   334	        if (err && typeof err === 'object' && 'code' in err && err.code === 'ENOENT') {
   335	          throw new Error(
   336	            `ELDER_WALLET_KEY_PATH file not found at ${keyPath}; either set DEPLOYER_PRIVATE_KEY env var or provide a key file`,
   337	          );
   338	        }
   339	        const msg = err instanceof Error ? err.message : String(err);
   340	        throw new Error(`Failed to read ELDER_WALLET_KEY_PATH file at ${keyPath}: ${msg}`);
   341	      }
   342	    } else {
   343	      const fallbackKey = readEnv('DEPLOYER_PRIVATE_KEY');
   344	      if (fallbackKey) {
   345	        console.warn('[RealChainClient] ELDER_WALLET_KEY_PATH not set; falling back to DEPLOYER_PRIVATE_KEY (deprecated)');
   346	        pk = fallbackKey;
   347	        pkSource = 'DEPLOYER_PRIVATE_KEY env var';
   348	      }
   349	    }
   350	    if (!pk) throw new Error('Neither ELDER_WALLET_KEY_PATH nor DEPLOYER_PRIVATE_KEY is set');
   351	
   352	    // Normalize: add 0x prefix if missing
   353	    if (!pk.startsWith('0x')) pk = '0x' + pk;
   354	    if (!/^0x[0-9a-fA-F]{64}$/.test(pk)) {
   355	      throw new Error(
   356	        `Invalid private key from ${pkSource ?? 'unknown source'}: expected a 64-hex-char private key (0x-prefixed optional)`,
   357	      );
   358	    }
   359	
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
/bin/bash -lc 'git show 9ccf94a:packages/contracts/abi/IClanWorld.json | rg -n "ResourcesDeposited|woodDelta|wood|atTick|tick" -n -C 3' in /home/claude/code/clan-world
 succeeded in 0ms:
1418-          "internalType": "struct MarketState",
1419-          "components": [
1420-            {
1421:              "name": "wood",
1422-              "type": "tuple",
1423-              "internalType": "struct PoolReserves",
1424-              "components": [
--
1690-      "name": "getScheduledMarketActionsForTick",
1691-      "inputs": [
1692-        {
1693:          "name": "tick",
1694-          "type": "uint64",
1695-          "internalType": "uint64"
1696-        }
--
1777-              "internalType": "bool"
1778-            },
1779-            {
1780:              "name": "woodToken",
1781-              "type": "address",
1782-              "internalType": "address"
1783-            },
--
1807-              "internalType": "address"
1808-            },
1809-            {
1810:              "name": "woodGoldPool",
1811-              "type": "address",
1812-              "internalType": "address"
1813-            },
--
2212-          "internalType": "struct PoolSeedConfig",
2213-          "components": [
2214-            {
2215:              "name": "woodSeed",
2216-              "type": "uint256",
2217-              "internalType": "uint256"
2218-            },
--
2401-          "internalType": "uint256"
2402-        },
2403-        {
2404:          "name": "wood",
2405-          "type": "uint256",
2406-          "internalType": "uint256"
2407-        },
--
2540-          "internalType": "uint256"
2541-        },
2542-        {
2543:          "name": "atTick",
2544-          "type": "uint64",
2545-          "indexed": false,
2546-          "internalType": "uint64"
--
2565-          "internalType": "uint32"
2566-        },
2567-        {
2568:          "name": "atTick",
2569-          "type": "uint64",
2570-          "indexed": false,
2571-          "internalType": "uint64"
--
2584-          "internalType": "uint32"
2585-        },
2586-        {
2587:          "name": "atTick",
2588-          "type": "uint64",
2589-          "indexed": false,
2590-          "internalType": "uint64"
--
2615-          "internalType": "uint8"
2616-        },
2617-        {
2618:          "name": "atTick",
2619-          "type": "uint64",
2620-          "indexed": false,
2621-          "internalType": "uint64"
--
2683-          "internalType": "uint8"
2684-        },
2685-        {
2686:          "name": "atTick",
2687-          "type": "uint64",
2688-          "indexed": false,
2689-          "internalType": "uint64"
--
2714-          "internalType": "uint8"
2715-        },
2716-        {
2717:          "name": "atTick",
2718-          "type": "uint64",
2719-          "indexed": false,
2720-          "internalType": "uint64"
--
2770-          "internalType": "uint256"
2771-        },
2772-        {
2773:          "name": "atTick",
2774-          "type": "uint64",
2775-          "indexed": false,
2776-          "internalType": "uint64"
--
2789-          "internalType": "uint32"
2790-        },
2791-        {
2792:          "name": "tick",
2793-          "type": "uint64",
2794-          "indexed": true,
2795-          "internalType": "uint64"
--
2845-          "internalType": "uint8"
2846-        },
2847-        {
2848:          "name": "atTick",
2849-          "type": "uint64",
2850-          "indexed": false,
2851-          "internalType": "uint64"
--
2870-          "internalType": "bool"
2871-        },
2872-        {
2873:          "name": "atTick",
2874-          "type": "uint64",
2875-          "indexed": false,
2876-          "internalType": "uint64"
--
2889-          "internalType": "uint32"
2890-        },
2891-        {
2892:          "name": "atTick",
2893-          "type": "uint64",
2894-          "indexed": false,
2895-          "internalType": "uint64"
--
2920-          "internalType": "uint16"
2921-        },
2922-        {
2923:          "name": "atTick",
2924-          "type": "uint64",
2925-          "indexed": false,
2926-          "internalType": "uint64"
--
2951-          "internalType": "uint256"
2952-        },
2953-        {
2954:          "name": "atTick",
2955-          "type": "uint64",
2956-          "indexed": false,
2957-          "internalType": "uint64"
--
3000-          "internalType": "uint256"
3001-        },
3002-        {
3003:          "name": "atTick",
3004-          "type": "uint64",
3005-          "indexed": false,
3006-          "internalType": "uint64"
--
3031-          "internalType": "uint32"
3032-        },
3033-        {
3034:          "name": "wood",
3035-          "type": "uint256",
3036-          "indexed": false,
3037-          "internalType": "uint256"
--
3228-          "internalType": "uint8"
3229-        },
3230-        {
3231:          "name": "atTick",
3232-          "type": "uint64",
3233-          "indexed": false,
3234-          "internalType": "uint64"
--
3241-      "name": "PoolsSeeded",
3242-      "inputs": [
3243-        {
3244:          "name": "woodGoldPool",
3245-          "type": "address",
3246-          "indexed": false,
3247-          "internalType": "address"
--
3269-    },
3270-    {
3271-      "type": "event",
3272:      "name": "ResourcesDeposited",
3273-      "inputs": [
3274-        {
3275-          "name": "clanId",
--
3284-          "internalType": "uint32"
3285-        },
3286-        {
3287:          "name": "wood",
3288-          "type": "uint256",
3289-          "indexed": false,
3290-          "internalType": "uint256"
--
3308-          "internalType": "uint256"
3309-        },
3310-        {
3311:          "name": "atTick",
3312-          "type": "uint64",
3313-          "indexed": false,
3314-          "internalType": "uint64"
--
3339-          "internalType": "enum ActionType"
3340-        },
3341-        {
3342:          "name": "woodGained",
3343-          "type": "uint256",
3344-          "indexed": false,
3345-          "internalType": "uint256"
--
3369-          "internalType": "uint256"
3370-        },
3371-        {
3372:          "name": "atTick",
3373-          "type": "uint64",
3374-          "indexed": false,
3375-          "internalType": "uint64"
--
3492-      "name": "SeasonFinalized",
3493-      "inputs": [
3494-        {
3495:          "name": "tick",
3496-          "type": "uint64",
3497-          "indexed": true,
3498-          "internalType": "uint64"
--
3523-          "internalType": "uint64"
3524-        },
3525-        {
3526:          "name": "tickSeed",
3527-          "type": "bytes32",
3528-          "indexed": false,
3529-          "internalType": "bytes32"
--
3560-          "internalType": "uint256"
3561-        },
3562-        {
3563:          "name": "atTick",
3564-          "type": "uint64",
3565-          "indexed": false,
3566-          "internalType": "uint64"
--
3591-          "internalType": "uint8"
3592-        },
3593-        {
3594:          "name": "atTick",
3595-          "type": "uint64",
3596-          "indexed": false,
3597-          "internalType": "uint64"
--
3604-      "name": "WinterEnded",
3605-      "inputs": [
3606-        {
3607:          "name": "tick",
3608-          "type": "uint64",
3609-          "indexed": true,
3610-          "internalType": "uint64"
--
3617-      "name": "WinterStarted",
3618-      "inputs": [
3619-        {
3620:          "name": "tick",
3621-          "type": "uint64",
3622-          "indexed": true,
3623-          "internalType": "uint64"
--
3648-          "internalType": "uint8"
3649-        },
3650-        {
3651:          "name": "tick",
3652-          "type": "uint64",
3653-          "indexed": false,
3654-          "internalType": "uint64"

exec
/bin/bash -lc "rg -n \"packages/contracts/abi/IClanWorld.json|IClanWorld.json|CLAN_WORLD_ABI|ResourcesDeposited\\(\" apps packages -S" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/src/ClanWorld.sol:697:        emit ResourcesDeposited(clanId, cs.clansmanId, w, ir, wh, fi, tick);
packages/contracts/src/IClanWorld.sol:533:    event ResourcesDeposited(
packages/shared/src/adapters/IChainClient.ts:25:const CLAN_WORLD_ABI = [
packages/shared/src/adapters/IChainClient.ts:283:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:369:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:380:      abi: CLAN_WORLD_ABI,

codex
I’m splitting this into parallel specialist passes now: one subagent on contract economics/security invariants, one on test adequacy, and one on ABI/indexer integration drift so we can converge on a higher-confidence final verdict.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '980,1385p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   980	
   981	    /// @notice Public settlement trigger — lazily settle a clan.
   982	    function settleClan(uint32 clanId) external override nonReentrant {
   983	        _settleClan(clanId);
   984	    }
   985	
   986	    /// @notice Lazily settle a single clansman's mission to current tick. Idempotent.
   987	    ///         Internally settles the entire clan (including upkeep) to guarantee
   988	    ///         correct ordering and prevent double-settlement. Callers may call this
   989	    ///         or settleClan interchangeably; both are safe and idempotent.
   990	    function settleClansman(uint32 csId) external override nonReentrant {
   991	        Clansman storage cs = _clansmen[csId];
   992	        if (cs.clansmanId == 0) return;
   993	        _settleClan(cs.clanId);
   994	    }
   995	
   996	    /// @notice Finalize season. Phase 1 stub.
   997	    function finalizeSeason() external override {
   998	        // TODO Phase 3
   999	    }
  1000	
  1001	    // =========================================================================
  1002	    // CLAN LIFECYCLE
  1003	    // =========================================================================
  1004	
  1005	    /// @notice Mint a new clan and spawn its homebase.
  1006	    function mintClan(address to) external override nonReentrant returns (uint32 clanId, uint256 iftTokenId) {
  1007	        require(to != address(0), "ClanWorld: zero address");
  1008	        require(_allClanIds.length < 12, "ClanWorld: max clans");
  1009	        clanId = _nextClanId++;
  1010	        iftTokenId = uint256(clanId); // Phase 1 placeholder; real iNFT is Phase 7
  1011	
  1012	        // Assign homebase region: valid spawn regions are 1,2,4,5,6,7 (not 3=UnicornTown, not 8=DeepSea)
  1013	        uint8[6] memory spawnRegions = [
  1014	            ClanWorldConstants.REGION_FOREST,
  1015	            ClanWorldConstants.REGION_MOUNTAINS,
  1016	            ClanWorldConstants.REGION_WEST_FARMS,
  1017	            ClanWorldConstants.REGION_EAST_FARMS,
  1018	            ClanWorldConstants.REGION_WEST_DOCKS,
  1019	            ClanWorldConstants.REGION_EAST_DOCKS
  1020	        ];
  1021	        uint8 baseRegion = spawnRegions[(clanId - 1) % 6];
  1022	
  1023	        // Create clan
  1024	        Clan storage clan = _clans[clanId];
  1025	        clan.clanId = clanId;
  1026	        clan.iftTokenId = iftTokenId;
  1027	        clan.owner = to;
  1028	        clan.clanState = ClanState.ACTIVE;
  1029	        clan.baseRegion = baseRegion;
  1030	        clan.baseLevel = 1;
  1031	        clan.wallLevel = 0;
  1032	        clan.monumentLevel = 0;
  1033	        clan.livingClansmen = 4;
  1034	        clan.lastSettledTick = _world.currentTick;
  1035	        clan.starvationStartsAtTick = 0;
  1036	        clan.coldDamage = 0;
  1037	        clan.goldBalance = 3e18; // starter gold per v4 spec §12.5
  1038	        clan.blueprintBalance = 0;
  1039	        // Starting vault per v4 spec §12.4
  1040	        clan.vaultWood = 20e18;
  1041	        clan.vaultIron = 0;
  1042	        clan.vaultWheat = 20e18;
  1043	        clan.vaultFish = 2e18;
  1044	
  1045	        // Wheat plots
  1046	        _wheatPlots[clanId][0] = WheatPlot({
  1047	            state: WheatPlotState.Harvestable,
  1048	            region: ClanWorldConstants.REGION_WEST_FARMS,
  1049	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1050	            regrowUntilTick: 0
  1051	        });
  1052	        _wheatPlots[clanId][1] = WheatPlot({
  1053	            state: WheatPlotState.Harvestable,
  1054	            region: ClanWorldConstants.REGION_EAST_FARMS,
  1055	            remainingWheat: ClanWorldConstants.WHEAT_PLOT_STARTING_WHEAT,
  1056	            regrowUntilTick: 0
  1057	        });
  1058	
  1059	        // Create 4 clansmen
  1060	        for (uint256 i = 0; i < 4; i++) {
  1061	            uint32 csId = _nextClansmanId++;
  1062	            Clansman storage cs = _clansmen[csId];
  1063	            cs.clansmanId = csId;
  1064	            cs.clanId = clanId;
  1065	            cs.state = ClansmanState.WAITING;
  1066	            cs.currentRegion = baseRegion;
  1067	            cs.cooldownEndsAtTs = 0;
  1068	            cs.lastMissionNonce = 0;
  1069	            cs.carryWood = 0;
  1070	            cs.carryIron = 0;
  1071	            cs.carryWheat = 0;
  1072	            cs.carryFish = 0;
  1073	            _clanClansmanIds[clanId].push(csId);
  1074	        }
  1075	
  1076	        _allClanIds.push(clanId);
  1077	
  1078	        emit ClanSpawned(clanId, to, iftTokenId, baseRegion, _world.currentTick);
  1079	        return (clanId, iftTokenId);
  1080	    }
  1081	
  1082	    /// @notice Submit one or more orders for a clan's clansmen. Per-order failures don't revert.
  1083	    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
  1084	        external
  1085	        override
  1086	        nonReentrant
  1087	        returns (OrderResult[] memory results)
  1088	    {
  1089	        Clan storage clan = _clans[clanId];
  1090	        require(clan.clanId != 0, "ClanWorld: clan not found");
  1091	        require(clan.owner == msg.sender, "ClanWorld: not clan owner");
  1092	        require(clan.clanState == ClanState.ACTIVE, "ClanWorld: clan dead");
  1093	
  1094	        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
  1095	        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
  1096	        // ERR_MUST_SETTLE_FIRST not in StatusCode enum — using ERR_INVALID_ACTION as the closest proxy
  1097	        {
  1098	            uint64 lastSettled = _clans[clanId].lastSettledTick;
  1099	            if (_world.currentTick > lastSettled + 200) {
  1100	                results = new OrderResult[](orders.length);
  1101	                for (uint256 i = 0; i < orders.length; i++) {
  1102	                    results[i] = OrderResult({
  1103	                        clansmanId: orders[i].clansmanId,
  1104	                        status: StatusCode.ERR_INVALID_ACTION,
  1105	                        cooldownEndsAtTs: 0,
  1106	                        missionNonce: 0
  1107	                    });
  1108	                }
  1109	                return results;
  1110	            }
  1111	        }
  1112	
  1113	        // Lazy settle before processing orders
  1114	        _settleClan(clanId);
  1115	
  1116	        results = new OrderResult[](orders.length);
  1117	
  1118	        for (uint256 i = 0; i < orders.length; i++) {
  1119	            results[i] = _processOrder(clanId, clan, orders[i]);
  1120	        }
  1121	
  1122	        return results;
  1123	    }
  1124	
  1125	    struct OrderCtx {
  1126	        uint8 gotoRegion;
  1127	        uint8 fromRegion;
  1128	        bool isNoop;
  1129	        uint8 travelTicks;
  1130	        uint64 arrivalTick;
  1131	        uint64 newNonce;
  1132	        uint32 targetClanId;
  1133	        bool wasActive;
  1134	        uint64 oldNonce;
  1135	    }
  1136	
  1137	    function _processOrder(uint32 clanId, Clan storage clan, ClanOrder calldata order)
  1138	        internal
  1139	        returns (OrderResult memory result)
  1140	    {
  1141	        result.clansmanId = order.clansmanId;
  1142	
  1143	        // Validate clansman
  1144	        Clansman storage cs = _clansmen[order.clansmanId];
  1145	        if (cs.clansmanId == 0 || cs.clanId != clanId) {
  1146	            result.status = StatusCode.ERR_INVALID_CLANSMAN;
  1147	            return result;
  1148	        }
  1149	        if (cs.state == ClansmanState.DEAD) {
  1150	            result.status = StatusCode.ERR_CLANSMAN_DEAD;
  1151	            return result;
  1152	        }
  1153	
  1154	        if (order.action == ActionType.DefendBase) {
  1155	            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
  1156	            if (defendErr != StatusCode.OK) {
  1157	                result.status = defendErr;
  1158	                return result;
  1159	            }
  1160	
  1161	            uint32 defendTargetClanId = order.targetClanId == 0 ? clanId : order.targetClanId;
  1162	            Mission storage currentM = _missions[order.clansmanId];
  1163	            if (
  1164	                currentM.active && currentM.action == ActionType.DefendBase && currentM.targetRegion == order.gotoRegion
  1165	                    && currentM.targetClanId == defendTargetClanId
  1166	            ) {
  1167	                result.status = StatusCode.OK;
  1168	                result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1169	                result.missionNonce = currentM.nonce;
  1170	                return result;
  1171	            }
  1172	        }
  1173	
  1174	        // Cooldown check
  1175	        if (block.timestamp < cs.cooldownEndsAtTs) {
  1176	            result.status = StatusCode.ERR_COOLDOWN_ACTIVE;
  1177	            result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1178	            result.missionNonce = cs.lastMissionNonce;
  1179	            return result;
  1180	        }
  1181	
  1182	        OrderCtx memory ctx;
  1183	        ctx.fromRegion = cs.currentRegion;
  1184	        ctx.gotoRegion = order.gotoRegion;
  1185	        ctx.targetClanId =
  1186	            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;
  1187	
  1188	        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
  1189	        ctx.isNoop = order.action != ActionType.DefendBase
  1190	            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
  1191	        if (ctx.isNoop) {
  1192	            ctx.gotoRegion = ctx.fromRegion;
  1193	        }
  1194	
  1195	        // Validate target region (1-8 or 0=noop)
  1196	        if (ctx.gotoRegion > 8) {
  1197	            result.status = StatusCode.ERR_INVALID_REGION;
  1198	            return result;
  1199	        }
  1200	
  1201	        // Validate action
  1202	        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
  1203	        if (actionErr != StatusCode.OK) {
  1204	            result.status = actionErr;
  1205	            return result;
  1206	        }
  1207	
  1208	        // Capture existing mission state
  1209	        Mission storage existingM = _missions[order.clansmanId];
  1210	        ctx.wasActive = existingM.active;
  1211	        ctx.oldNonce = existingM.nonce;
  1212	
  1213	        // Compute travel from the clansman's current region to the order target.
  1214	        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
  1215	        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));
  1216	
  1217	        // New nonce
  1218	        ctx.newNonce = cs.lastMissionNonce + 1;
  1219	        cs.lastMissionNonce = ctx.newNonce;
  1220	
  1221	        // Install mission via helper to keep stack shallow
  1222	        _installMission(existingM, order, cs, ctx);
  1223	
  1224	        // Update clansman state
  1225	        if (ctx.travelTicks > 0) {
  1226	            cs.state = ClansmanState.TRAVELING;
  1227	        } else {
  1228	            // NOOP / same-region: no traveling state per v4.3 §A
  1229	            cs.state = ClansmanState.ACTING;
  1230	            cs.currentRegion = ctx.gotoRegion;
  1231	        }
  1232	
  1233	        // Start cooldown
  1234	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
  1235	
  1236	        _clearDefender(cs.clansmanId);
  1237	
  1238	        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
  1239	        // executeAtTick = arrivalTick (not arrivalTick+1).
  1240	        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
  1241	            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
  1242	        }
  1243	
  1244	        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
  1245	            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
  1246	        }
  1247	
  1248	        if (ctx.wasActive) {
  1249	            emit MissionInterrupted(clanId, order.clansmanId, ctx.oldNonce, ctx.newNonce);
  1250	        }
  1251	
  1252	        emit MissionAssigned(
  1253	            clanId,
  1254	            order.clansmanId,
  1255	            ctx.newNonce,
  1256	            order.action,
  1257	            ctx.fromRegion,
  1258	            ctx.gotoRegion,
  1259	            _world.currentTick,
  1260	            ctx.arrivalTick
  1261	        );
  1262	
  1263	        result.status = StatusCode.OK;
  1264	        result.cooldownEndsAtTs = cs.cooldownEndsAtTs;
  1265	        result.missionNonce = ctx.newNonce;
  1266	        return result;
  1267	    }
  1268	
  1269	    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
  1270	        internal
  1271	    {
  1272	        m.active = true;
  1273	        m.nonce = ctx.newNonce;
  1274	        m.clansmanId = cs.clansmanId;
  1275	        m.submittedAtTick = _world.currentTick;
  1276	        m.executesAtTick = ctx.arrivalTick;
  1277	        m.settlesAtTick = order.action == ActionType.DefendBase
  1278	            ? type(uint64).max
  1279	            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
  1280	        m.startRegion = ctx.fromRegion;
  1281	        m.targetRegion = ctx.gotoRegion;
  1282	        m.action = order.action;
  1283	        m.startTick = _world.currentTick;
  1284	        m.arrivalTick = ctx.arrivalTick;
  1285	        m.actionStartTick = ctx.arrivalTick;
  1286	        m.missionSeed = keccak256(abi.encode(_world.currentTickSeed, cs.clansmanId, ctx.newNonce));
  1287	        m.marketMode = (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell)
  1288	            ? MarketExecutionMode.Scheduled
  1289	            : MarketExecutionMode.None;
  1290	        m.targetClanId = ctx.targetClanId;
  1291	        m.marketToken = order.marketToken;
  1292	        m.marketAmount = order.marketAmount;
  1293	        m.maxGoldIn = order.maxGoldIn;
  1294	    }
  1295	
  1296	    function _enqueueScheduledMarketAction(
  1297	        uint32 clanId,
  1298	        ClanOrder calldata order,
  1299	        uint32 clansmanId,
  1300	        uint64 executeAtTick,
  1301	        uint64 missionNonce
  1302	    ) internal {
  1303	        ScheduledMarketAction memory sma = ScheduledMarketAction({
  1304	            executeAtTick: executeAtTick,
  1305	            commitSequence: _world.nextCommitSequence++,
  1306	            missionNonce: missionNonce,
  1307	            clanId: clanId,
  1308	            clansmanId: clansmanId,
  1309	            action: order.action,
  1310	            marketToken: order.marketToken,
  1311	            marketAmount: order.marketAmount,
  1312	            maxGoldIn: order.maxGoldIn
  1313	        });
  1314	        _scheduledMarketActions[executeAtTick].push(sma);
  1315	        emit ScheduledMarketActionCommitted(
  1316	            executeAtTick,
  1317	            sma.commitSequence,
  1318	            clanId,
  1319	            clansmanId,
  1320	            order.action,
  1321	            order.marketToken,
  1322	            order.marketAmount,
  1323	            order.maxGoldIn
  1324	        );
  1325	    }
  1326	
  1327	    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
  1328	        if (_clansmanDefendingRegion[clansmanId] == region) return;
  1329	        _clearDefender(clansmanId);
  1330	
  1331	        if (_defenderCountByRegionClan[region][clanId] == 0) {
  1332	            _defendingClansByRegion[region].push(clanId);
  1333	        }
  1334	        _defenderCountByRegionClan[region][clanId]++;
  1335	        _clansmanDefendingRegion[clansmanId] = region;
  1336	    }
  1337	
  1338	    function _clearDefender(uint32 clansmanId) internal {
  1339	        uint8 region = _clansmanDefendingRegion[clansmanId];
  1340	        if (region == 0) return;
  1341	
  1342	        uint32 clanId = _clansmen[clansmanId].clanId;
  1343	        uint256 count = _defenderCountByRegionClan[region][clanId];
  1344	        if (count > 1) {
  1345	            _defenderCountByRegionClan[region][clanId] = count - 1;
  1346	        } else {
  1347	            delete _defenderCountByRegionClan[region][clanId];
  1348	            uint32[] storage clans = _defendingClansByRegion[region];
  1349	            for (uint256 i = 0; i < clans.length; i++) {
  1350	                if (clans[i] == clanId) {
  1351	                    clans[i] = clans[clans.length - 1];
  1352	                    clans.pop();
  1353	                    break;
  1354	                }
  1355	            }
  1356	        }
  1357	
  1358	        delete _clansmanDefendingRegion[clansmanId];
  1359	    }
  1360	
  1361	    // =========================================================================
  1362	    // MARKET EXECUTION (Phase 2)
  1363	    // =========================================================================
  1364	
  1365	    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
  1366	    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
  1367	    function _executeScheduledMarketActions(uint64 tick) internal {
  1368	        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
  1369	        uint256 len = actions.length;
  1370	        if (len == 0) return;
  1371	
  1372	        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;
  1373	
  1374	        for (uint256 i = 0; i < processCount; i++) {
  1375	            ScheduledMarketAction storage sma = actions[i];
  1376	
  1377	            // Validate clansman still belongs to the clan
  1378	            Clansman storage cs = _clansmen[sma.clansmanId];
  1379	            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
  1380	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
  1381	                continue;
  1382	            }
  1383	
  1384	            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
  1385	            // Note: _completeMission sets m.active=false during settlement (by design), so we

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1385,1565p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  1385	            // Note: _completeMission sets m.active=false during settlement (by design), so we
  1386	            // cannot use m.active as a validity signal here — check action type and nonce.
  1387	            Mission storage m = _missions[sma.clansmanId];
  1388	            if (m.action != sma.action || m.nonce != sma.missionNonce) {
  1389	                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1390	                continue;
  1391	            }
  1392	
  1393	            if (sma.action == ActionType.MarketSell) {
  1394	                try this._executeMarketSellExternal(
  1395	                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
  1396	                ) {
  1397	                // success
  1398	                }
  1399	                catch {
  1400	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1401	                }
  1402	            } else if (sma.action == ActionType.MarketBuy) {
  1403	                try this._executeMarketBuyExternal(
  1404	                    tick,
  1405	                    sma.clanId,
  1406	                    sma.clansmanId,
  1407	                    sma.marketToken,
  1408	                    sma.marketAmount,
  1409	                    sma.maxGoldIn,
  1410	                    sma.commitSequence
  1411	                ) {
  1412	                // success
  1413	                }
  1414	                catch {
  1415	                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
  1416	                }
  1417	            }
  1418	        }
  1419	
  1420	        if (len > processCount) {
  1421	            ScheduledMarketAction[] storage nextActions = _scheduledMarketActions[tick + 1];
  1422	            for (uint256 i = processCount; i < len; i++) {
  1423	                nextActions.push(actions[i]);
  1424	            }
  1425	            // Invariant: each tick queue executes in global commitSequence order, including
  1426	            // older overflow actions merged into a tick that already has native actions.
  1427	            _sortScheduledMarketActionsByCommitSequence(nextActions);
  1428	        }
  1429	
  1430	        delete _scheduledMarketActions[tick];
  1431	    }
  1432	
  1433	    function _sortScheduledMarketActionsByCommitSequence(ScheduledMarketAction[] storage actions) internal {
  1434	        for (uint256 i = 1; i < actions.length; i++) {
  1435	            ScheduledMarketAction memory key = actions[i];
  1436	            uint256 j = i;
  1437	            while (j > 0 && actions[j - 1].commitSequence > key.commitSequence) {
  1438	                actions[j] = actions[j - 1];
  1439	                j--;
  1440	            }
  1441	            actions[j] = key;
  1442	        }
  1443	    }
  1444	
  1445	    /// @dev External wrapper for _executeMarketSell — enables try/catch from heartbeat loop.
  1446	    function _executeMarketSellExternal(
  1447	        uint64 closedTick,
  1448	        uint32 clanId,
  1449	        uint32 clansmanId,
  1450	        address token,
  1451	        uint256 amount,
  1452	        uint64 commitSequence
  1453	    ) external {
  1454	        require(msg.sender == address(this), "ClanWorld: internal only");
  1455	        _executeMarketSell(closedTick, clanId, clansmanId, token, amount, commitSequence);
  1456	    }
  1457	
  1458	    /// @dev External wrapper for _executeMarketBuy — enables try/catch from heartbeat loop.
  1459	    function _executeMarketBuyExternal(
  1460	        uint64 closedTick,
  1461	        uint32 clanId,
  1462	        uint32 clansmanId,
  1463	        address token,
  1464	        uint256 amountOut,
  1465	        uint256 maxGoldIn,
  1466	        uint64 commitSequence
  1467	    ) external {
  1468	        require(msg.sender == address(this), "ClanWorld: internal only");
  1469	        _executeMarketBuy(closedTick, clanId, clansmanId, token, amountOut, maxGoldIn, commitSequence);
  1470	    }
  1471	
  1472	    /// @dev Map a resource token address to its pool address.
  1473	    function _poolFor(address token) internal view returns (address pool) {
  1474	        if (token == _treasury.woodToken) return _treasury.woodGoldPool;
  1475	        if (token == _treasury.ironToken) return _treasury.ironGoldPool;
  1476	        if (token == _treasury.wheatToken) return _treasury.wheatGoldPool;
  1477	        if (token == _treasury.fishToken) return _treasury.fishGoldPool;
  1478	        return address(0);
  1479	    }
  1480	
  1481	    /// @dev Add an amount of a resource token to the clan vault.
  1482	    function _addToVault(Clan storage clan, address token, uint256 amount) internal {
  1483	        if (token == _treasury.woodToken) {
  1484	            clan.vaultWood += amount;
  1485	            return;
  1486	        }
  1487	        if (token == _treasury.ironToken) {
  1488	            clan.vaultIron += amount;
  1489	            return;
  1490	        }
  1491	        if (token == _treasury.wheatToken) {
  1492	            clan.vaultWheat += amount;
  1493	            return;
  1494	        }
  1495	        if (token == _treasury.fishToken) {
  1496	            clan.vaultFish += amount;
  1497	            return;
  1498	        }
  1499	    }
  1500	
  1501	    /// @dev Deduct an amount of a resource token from the clan vault. Returns false if insufficient.
  1502	    function _deductFromVault(Clan storage clan, address token, uint256 amount) internal returns (bool) {
  1503	        if (token == _treasury.woodToken) {
  1504	            if (clan.vaultWood < amount) return false;
  1505	            clan.vaultWood -= amount;
  1506	            return true;
  1507	        }
  1508	        if (token == _treasury.ironToken) {
  1509	            if (clan.vaultIron < amount) return false;
  1510	            clan.vaultIron -= amount;
  1511	            return true;
  1512	        }
  1513	        if (token == _treasury.wheatToken) {
  1514	            if (clan.vaultWheat < amount) return false;
  1515	            clan.vaultWheat -= amount;
  1516	            return true;
  1517	        }
  1518	        if (token == _treasury.fishToken) {
  1519	            if (clan.vaultFish < amount) return false;
  1520	            clan.vaultFish -= amount;
  1521	            return true;
  1522	        }
  1523	        return false;
  1524	    }
  1525	
  1526	    /// @dev Execute a scheduled market sell: deduct resource from vault, credit gold.
  1527	    function _executeMarketSell(
  1528	        uint64 closedTick,
  1529	        uint32 clanId,
  1530	        uint32 clansmanId,
  1531	        address token,
  1532	        uint256 amount,
  1533	        uint64 commitSequence
  1534	    ) internal {
  1535	        if (!_treasury.poolsSeeded) {
  1536	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1537	            return;
  1538	        }
  1539	        address poolAddr = _poolFor(token);
  1540	        if (poolAddr == address(0)) {
  1541	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN);
  1542	            return;
  1543	        }
  1544	
  1545	        Clan storage clan = _clans[clanId];
  1546	        if (!_deductFromVault(clan, token, amount)) {
  1547	            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketSell, StatusCode.ERR_MISSING_RESOURCES);
  1548	            return;
  1549	        }
  1550	
  1551	        uint256 goldOut = StubPool(poolAddr).sellResource(amount);
  1552	        clan.goldBalance += goldOut;
  1553	
  1554	        emit ScheduledMarketActionExecuted(
  1555	            closedTick, commitSequence, clanId, clansmanId, token, _treasury.goldToken, amount, goldOut
  1556	        );
  1557	    }
  1558	
  1559	    /// @dev Execute a scheduled market buy: deduct gold from purse, credit resource to vault.
  1560	    function _executeMarketBuy(
  1561	        uint64 closedTick,
  1562	        uint32 clanId,
  1563	        uint32 clansmanId,
  1564	        address token,
  1565	        uint256 amountOut,

exec
/bin/bash -lc 'git show 9ccf94a:packages/contracts/src/ClanWorld.sol | rg -n "function _settleClan|_settleClansman|settlesAtTick|executesAtTick|actionStartTick|ACTING" -n' in /home/claude/code/clan-world
 succeeded in 0ms:
326:            // Path 1 → Path 2 transition: TRAVELING → ACTING at arrivalTick
328:                cs.state = ClansmanState.ACTING;
339:            // Path 3: ACTING at/past settlesAtTick → resolve
340:            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
354:    function _settleClan(uint32 clanId) internal {
433:    /// @dev Resolve an action for a clansman that is in ACTING state.
460:            // NOOP — worker stays ACTING (continuous), no transition needed
893:        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
914:    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
930:                if (m.settlesAtTick != tick) continue; // not due this tick
1229:            cs.state = ClansmanState.ACTING;
1276:        m.executesAtTick = ctx.arrivalTick;
1277:        m.settlesAtTick = order.action == ActionType.DefendBase
1285:        m.actionStartTick = ctx.arrivalTick;
1796:        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '250,430p'" in /home/claude/code/clan-world
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '840,980p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   840	        }
   841	
   842	        if (
   843	            clan.vaultWood < woodCost || clan.vaultWheat < wheatCost || clan.vaultIron < ironCost
   844	                || clan.blueprintBalance < blueprintCost
   845	        ) return false;
   846	
   847	        clan.vaultWood -= woodCost;
   848	        clan.vaultWheat -= wheatCost;
   849	        clan.vaultIron -= ironCost;
   850	        clan.blueprintBalance -= blueprintCost;
   851	
   852	        uint8 old = clan.monumentLevel;
   853	        clan.monumentLevel = nextLevel;
   854	        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
   855	        return true;
   856	    }
   857	
   858	    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
   859	    function _completeMission(Clansman storage cs, Mission storage m) internal {
   860	        cs.state = ClansmanState.WAITING;
   861	        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
   862	        m.active = false;
   863	        emit MissionCompleted(_clans[cs.clanId].clanId, cs.clansmanId, m.nonce, m.action);
   864	    }
   865	
   866	    // =========================================================================
   867	    // WORLD PROGRESSION
   868	    // =========================================================================
   869	
   870	    /// @notice Permissionless heartbeat. Closes the current tick, advances tick counter.
   871	    ///         Execution order per spec §4.2 (CEI-safe):
   872	    ///         CEI guard: nextHeartbeatAtTs written first to close reentrancy window.
   873	    ///         Seed:      closedTick seed derived and published before step 1 so
   874	    ///                    settlement RNG reads real entropy, not zero.
   875	    ///         1. Settle missions completing this tick.
   876	    ///         2. Execute scheduled market actions for closedTick (external calls).
   877	    ///         3. Eager-settle clans touched by world events (Phase 3 stub).
   878	    ///         4. Resolve world events (season boundary, winter transitions).
   879	    ///         5. Increment tick and publish (seed already written above).
   880	    function heartbeat() external override nonReentrant {
   881	        require(block.timestamp >= _world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");
   882	
   883	        uint64 closedTick = _world.currentTick;
   884	
   885	        // CEI: update rate-limit guard before any external calls
   886	        _world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
   887	
   888	        // Derive and publish seed for closedTick before step 1 (settlement reads it for RNG)
   889	        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, _world.currentTickSeed, closedTick));
   890	        _tickSeeds[closedTick] = newSeed;
   891	        _world.currentTickSeed = newSeed;
   892	
   893	        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
   894	        // Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   895	        _settleCompletingMissions(closedTick);
   896	
   897	        // Step 2: Execute scheduled market actions for closedTick (may make external calls).
   898	        _executeScheduledMarketActions(closedTick);
   899	
   900	        // Step 3: Eager-settle clans touched by world events (Phase 3 bandit — stub).
   901	        // TODO Phase 3: _settleClansNearBandit(closedTick);
   902	
   903	        // Step 4: Resolve world events (season boundary, winter transitions).
   904	        _resolveWorldEvents(closedTick);
   905	
   906	        // Step 5: Increment tick and publish (seed already written above; complete the atomic pair).
   907	        uint64 newTick = closedTick + 1;
   908	        _world.currentTick = newTick;
   909	        _world.nextHeartbeatAtTick = newTick + 1;
   910	
   911	        emit TickAdvanced(closedTick, newTick, newSeed);
   912	    }
   913	
   914	    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
   915	    ///      Called from heartbeat before market execution and tick increment.
   916	    ///      Bounded by 12-clan cap x 4 clansmen = 48 max iterations.
   917	    function _settleCompletingMissions(uint64 tick) internal {
   918	        for (uint256 i = 0; i < _allClanIds.length; i++) {
   919	            uint32 clanId = _allClanIds[i];
   920	            Clan storage clan = _clans[clanId];
   921	            if (clan.clanState == ClanState.DEAD) continue;
   922	
   923	            uint32[] storage csIds = _clanClansmanIds[clanId];
   924	            for (uint256 j = 0; j < csIds.length; j++) {
   925	                Clansman storage cs = _clansmen[csIds[j]];
   926	                if (cs.state == ClansmanState.DEAD) continue;
   927	
   928	                Mission storage m = _missions[cs.clansmanId];
   929	                if (!m.active) continue;
   930	                if (m.settlesAtTick != tick) continue; // not due this tick
   931	
   932	                // Settle this mission using the single-tick range [tick, tick+1).
   933	                _settleMissionForClansman(clan, cs, clanId, tick, tick + 1);
   934	            }
   935	        }
   936	    }
   937	
   938	    /// @dev Resolve world events for the tick that was just closed.
   939	    ///      Uses closedTick+1 as the equivalent of the old `newTick` for transition checks.
   940	    function _resolveWorldEvents(uint64 closedTick) internal {
   941	        uint64 newTick = closedTick + 1;
   942	
   943	        // --- season boundary ---
   944	        if (newTick >= _world.seasonEndTick) {
   945	            _world.currentSeasonNumber += 1;
   946	            _world.seasonStartTick = _world.seasonEndTick;
   947	            _world.seasonEndTick = _world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
   948	            // reset winter timers for new season
   949	            _world.winterActive = false;
   950	            _world.winterStartsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   951	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   952	            _world.winterEndsAtTick = _world.seasonStartTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   953	        }
   954	
   955	        // --- winter transitions (timer only; mechanics = Phase 10) ---
   956	        if (
   957	            !_world.winterActive && newTick >= _world.winterStartsAtTick
   958	                && _world.winterStartsAtTick < _world.seasonEndTick
   959	        ) {
   960	            _world.winterActive = true;
   961	            emit WinterStarted(newTick);
   962	        }
   963	        if (_world.winterActive && newTick >= _world.winterEndsAtTick) {
   964	            _world.winterActive = false;
   965	            emit WinterEnded(newTick);
   966	            // schedule next winter cycle within this season
   967	            uint64 nextWinterStart = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE
   968	                - ClanWorldConstants.WINTER_DURATION_TICKS;
   969	            uint64 nextWinterEnd = _world.winterEndsAtTick + ClanWorldConstants.TICKS_PER_WINTER_CYCLE;
   970	            if (nextWinterStart < _world.seasonEndTick) {
   971	                _world.winterStartsAtTick = nextWinterStart;
   972	                _world.winterEndsAtTick = nextWinterEnd;
   973	            } else {
   974	                // no more winters this season; sentinel = seasonEndTick so guard never fires
   975	                _world.winterStartsAtTick = _world.seasonEndTick;
   976	                _world.winterEndsAtTick = _world.seasonEndTick;
   977	            }
   978	        }
   979	    }
   980	

web search: 
web search: 
codex
I’ve verified mission-settlement timing in detail; I’m now drilling into `apps/server` indexer code to confirm whether the event signature/type change (`ResourcesDeposited ... uint32 tick`) breaks existing decoders.
exec
/bin/bash -lc 'rg -n "ResourcesDeposited|ResourcesGathered|TickAdvanced|decodeEventLog|parseEventLogs|watchContractEvent|getLogs|topic|atTick|tick" apps/server -S' in /home/claude/code/clan-world
 succeeded in 0ms:
apps/server/AGENTS.md:3:Convex backend. Hosts game-state queries, the indexer cron, and the post-tick webhook. One Convex deployment per realm (one for S1 World Chain Sepolia, redeployed for S2 Base Sepolia).
apps/server/AGENTS.md:9:- **Indexer cron:** runs every 5s as a safety net; the primary trigger is the post-tick webhook (per addendum §4).
apps/server/AGENTS.md:10:- **Post-tick webhook:** HTTP action at `/api/heartbeat-webhook` — re-runs both event indexer and state snapshot refresh.
apps/server/AGENTS.md:35:- **`ILLMClient`** — used by the optional narrator function (S2) to summarize ticks.
apps/server/convex/heartbeat.ts:30:  | { status: "ok"; tick: number }
apps/server/convex/heartbeat.ts:40:    // calls from inserting duplicate tick rows.
apps/server/convex/heartbeat.ts:43:      snap.tickEpochStartedAt +
apps/server/convex/heartbeat.ts:44:      Math.floor(snap.tickEpochDurationMs / 1000);
apps/server/convex/heartbeat.ts:49:    const newTick = snap.tick + 1;
apps/server/convex/heartbeat.ts:51:      tick: newTick,
apps/server/convex/heartbeat.ts:52:      tickEpochStartedAt: Math.floor(Date.now() / 1000),
apps/server/convex/heartbeat.ts:53:      tickEpochDurationMs: snap.tickEpochDurationMs,
apps/server/convex/heartbeat.ts:59:      message: `heartbeat: tick ${snap.tick} → ${newTick}`,
apps/server/convex/heartbeat.ts:63:    return { status: "ok", tick: newTick };
apps/server/convex/schema.ts:6:    tick: v.number(),
apps/server/convex/schema.ts:7:    tickEpochStartedAt: v.number(),
apps/server/convex/schema.ts:8:    tickEpochDurationMs: v.number(),
apps/server/convex/mock.ts:29:      tick: 42,
apps/server/convex/mock.ts:30:      tickEpochStartedAt: Math.floor(Date.now() / 1000) - 42 * 20,
apps/server/convex/mock.ts:31:      tickEpochDurationMs: 20_000,
apps/server/convex/mock.ts:38:      { level: "info" as const, message: "Agent initialized — tick 42" },
apps/server/convex/getSnapshot.ts:8:        tick: 0,
apps/server/convex/getSnapshot.ts:9:        tickEpoch: { startedAt: 0, durationMs: 20_000 },
apps/server/convex/getSnapshot.ts:15:      tick: snap.tick,
apps/server/convex/getSnapshot.ts:16:      tickEpoch: {
apps/server/convex/getSnapshot.ts:17:        startedAt: snap.tickEpochStartedAt,
apps/server/convex/getSnapshot.ts:18:        durationMs: snap.tickEpochDurationMs,

exec
/bin/bash -lc "rg -n \"IClanWorld\\.json|abi|contracts/abi|IClanWorldEvents|StatusCode|ERR_NOT_AT_HOMEBASE|ERR_INVALID_REGION\" apps/server packages/shared apps/web -S" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/shared/src/adapters/IChainClient.ts:61:    stateMutability: 'view',
packages/shared/src/adapters/IChainClient.ts:203:    stateMutability: 'view',
packages/shared/src/adapters/IChainClient.ts:236:    stateMutability: 'nonpayable',
packages/shared/src/adapters/IChainClient.ts:283:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:369:      abi: CLAN_WORLD_ABI,
packages/shared/src/adapters/IChainClient.ts:380:      abi: CLAN_WORLD_ABI,
apps/web/src/WorldMap.tsx:962:        // Base size: ~128px at 1x scale, scales with viewport (2x bump for phone readability)
apps/web/src/hooks/useConnectionStatus.ts:16: * Known limitation: this is a *reachability* probe, not a *health* probe.

web search: 
web search: 
web search: 
web search: 
exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/MissionTiming.t.sol | nl -ba | sed -n '1,260p'" in /home/claude/code/clan-world
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
    11	    ClanFullView,
    12	    Clansman,
    13	    ClanOrder,
    14	    OrderResult,
    15	    Mission
    16	} from "../src/IClanWorld.sol";
    17	
    18	contract MissionTimingTest is Test {
    19	    ClanWorld world;
    20	    address elder = address(0xA1);
    21	
    22	    function setUp() public {
    23	        world = new ClanWorld();
    24	    }
    25	
    26	    function _advanceTick() internal {
    27	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
    28	        world.heartbeat();
    29	    }
    30	
    31	    function _advanceUntilCurrentTick(uint64 targetTick) internal {
    32	        while (world.getWorldState().currentTick < targetTick) {
    33	            _advanceTick();
    34	        }
    35	    }
    36	
    37	    function _mintClan() internal returns (uint32 clanId) {
    38	        vm.prank(elder);
    39	        (clanId,) = world.mintClan(elder);
    40	    }
    41	
    42	    function _firstCs(uint32 clanId) internal view returns (uint32) {
    43	        ClanFullView memory view_ = world.getClanFullView(clanId);
    44	        return view_.clansmen[0].clansman.clansman.clansmanId;
    45	    }
    46	
    47	    function _submitOrder(uint32 clanId, uint32 csId, uint8 gotoRegion, ActionType action)
    48	        internal
    49	        returns (OrderResult[] memory)
    50	    {
    51	        ClanOrder[] memory orders = new ClanOrder[](1);
    52	        orders[0] = ClanOrder({
    53	            clansmanId: csId,
    54	            gotoRegion: gotoRegion,
    55	            action: action,
    56	            targetClanId: 0,
    57	            marketToken: address(0),
    58	            marketAmount: 0,
    59	            maxGoldIn: 0
    60	        });
    61	        vm.prank(elder);
    62	        return world.submitClanOrders(clanId, orders);
    63	    }
    64	
    65	    function test_submitStoresSubmittedExecutesAndSettlesTicks() public {
    66	        uint32 clanId = _mintClan();
    67	        uint32 csId = _firstCs(clanId);
    68	        uint64 submittedAt = world.getWorldState().currentTick;
    69	        uint64 travel = world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_MOUNTAINS);
    70	        uint64 duration = world.getActionDuration(ActionType.MineIron);
    71	
    72	        OrderResult[] memory results =
    73	            _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);
    74	
    75	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");
    76	
    77	        Mission memory mission = world.getActiveMission(csId);
    78	        assertEq(mission.submittedAtTick, submittedAt, "submitted tick");
    79	        assertEq(mission.executesAtTick, submittedAt + travel, "executes tick");
    80	        assertEq(mission.settlesAtTick, submittedAt + travel + duration, "settles tick");
    81	
    82	        assertEq(mission.startTick, mission.submittedAtTick, "legacy start tick mirrors submitted");
    83	        assertEq(mission.actionStartTick, mission.executesAtTick, "legacy action start mirrors executes");
    84	
    85	        (uint64 submitted, uint64 executes, uint64 settles) = world.getMissionTiming(clanId, csId);
    86	        assertEq(submitted, mission.submittedAtTick, "getter submitted");
    87	        assertEq(executes, mission.executesAtTick, "getter executes");
    88	        assertEq(settles, mission.settlesAtTick, "getter settles");
    89	    }
    90	
    91	    function test_settlementWaitsUntilSettlesAtTickForGatherMission() public {
    92	        uint32 clanId = _mintClan();
    93	        uint32 csId = _firstCs(clanId);
    94	
    95	        OrderResult[] memory results =
    96	            _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);
    97	        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");
    98	
    99	        Mission memory mission = world.getActiveMission(csId);
   100	        assertEq(mission.settlesAtTick, mission.executesAtTick + 4, "four tick action duration");
   101	
   102	        _advanceUntilCurrentTick(mission.executesAtTick + 1);
   103	        world.settleClan(clanId);
   104	
   105	        Clansman memory arrived = world.getClansman(csId);
   106	        assertEq(uint8(arrived.state), uint8(ClansmanState.ACTING), "arrived and action started");
   107	        assertEq(arrived.currentRegion, ClanWorldConstants.REGION_MOUNTAINS, "arrived at target");
   108	        assertEq(arrived.carryIron, 0, "no iron before settlesAtTick");
   109	        assertTrue(world.getActiveMission(csId).active, "mission remains active before settlesAtTick");
   110	
   111	        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
   112	        world.settleClan(clanId);
   113	
   114	        Clansman memory settled = world.getClansman(csId);
   115	        assertGt(settled.carryIron, 0, "iron granted at settlesAtTick");
   116	        assertEq(uint8(settled.state), uint8(ClansmanState.WAITING), "mission completed");
   117	        assertFalse(world.getActiveMission(csId).active, "mission inactive after settlement");
   118	        assertGt(settled.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
   119	    }
   120	
   121	    function test_getActionDuration_eachActionType() public view {
   122	        assertEq(world.getActionDuration(ActionType.None), 0, "none");
   123	        assertEq(world.getActionDuration(ActionType.ChopWood), 4, "chop wood");
   124	        assertEq(world.getActionDuration(ActionType.MineIron), 4, "mine iron");
   125	        assertEq(world.getActionDuration(ActionType.FishDocks), 4, "fish docks");
   126	        assertEq(world.getActionDuration(ActionType.FishDeepSea), 4, "fish deep sea");
   127	        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
   128	        assertEq(world.getActionDuration(ActionType.DepositResources), 1, "deposit");
   129	        assertEq(world.getActionDuration(ActionType.BuildWall), 1, "build wall");
   130	        assertEq(world.getActionDuration(ActionType.UpgradeBase), 1, "upgrade base");
   131	        assertEq(world.getActionDuration(ActionType.UpgradeMonument), 1, "upgrade monument");
   132	        assertEq(world.getActionDuration(ActionType.DefendBase), 0, "defend base");
   133	        assertEq(world.getActionDuration(ActionType.MarketBuy), 1, "market buy");
   134	        assertEq(world.getActionDuration(ActionType.MarketSell), 1, "market sell");
   135	        assertEq(world.getActionDuration(ActionType.Wait), 0, "wait");
   136	    }
   137	
   138	    function test_getTravelTicks_adjacentAndDistantMatchTable() public view {
   139	        assertEq(
   140	            world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_MOUNTAINS), 1, "adjacent"
   141	        );
   142	        assertEq(
   143	            world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_EAST_DOCKS), 4, "distant"
   144	        );
   145	        assertEq(
   146	            world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_FOREST), 0, "same region"
   147	        );
   148	    }
   149	
   150	    function test_missionOverwriteResetsSubmittedAtTick() public {
   151	        uint32 clanId = _mintClan();
   152	        uint32 csId = _firstCs(clanId);
   153	
   154	        OrderResult[] memory first = _submitOrder(clanId, csId, ClanWorldConstants.REGION_NOOP, ActionType.Wait);
   155	        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first wait");
   156	        uint64 firstSubmitted = world.getActiveMission(csId).submittedAtTick;
   157	
   158	        _advanceTick();
   159	
   160	        OrderResult[] memory second = _submitOrder(clanId, csId, ClanWorldConstants.REGION_NOOP, ActionType.Wait);
   161	        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "second wait");
   162	        uint64 secondSubmitted = world.getActiveMission(csId).submittedAtTick;
   163	
   164	        assertEq(firstSubmitted, 0, "first submit starts at tick 0");
   165	        assertEq(secondSubmitted, world.getWorldState().currentTick, "second submit uses current tick");
   166	        assertGt(secondSubmitted, firstSubmitted, "countdown restarts");
   167	    }
   168	
   169	    function test_getMissionTimingReturnsZerosForNonExistentMission() public view {
   170	        (uint64 submitted, uint64 executes, uint64 settles) = world.getMissionTiming(123, 456);
   171	
   172	        assertEq(submitted, 0, "submitted");
   173	        assertEq(executes, 0, "executes");
   174	        assertEq(settles, 0, "settles");
   175	    }
   176	}

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/HeartbeatOrdering.t.sol | nl -ba | sed -n '150,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   150	        world.initTreasury(tokens, pools);
   151	
   152	        uint256 resSeed = 1000e18;
   153	        uint256 goldSeed = 1000e18;
   154	        PoolSeedConfig memory cfg = PoolSeedConfig({
   155	            woodSeed: resSeed,
   156	            wheatSeed: resSeed,
   157	            fishSeed: resSeed,
   158	            ironSeed: resSeed,
   159	            goldSeedForWood: goldSeed,
   160	            goldSeedForWheat: goldSeed,
   161	            goldSeedForFish: goldSeed,
   162	            goldSeedForIron: goldSeed
   163	        });
   164	        world.seedPools(cfg);
   165	    }
   166	
   167	    // -------------------------------------------------------------------------
   168	    // test_heartbeat_settlementBeforeMarket
   169	    //
   170	    // Proves Step 1 (settle) fires before Step 2 (market execute) within the SAME
   171	    // heartbeat closing tick T:
   172	    //   - cs0 is placed at Mountains (region 2). Deposit to homebase Forest (region 1)
   173	    //     = 1 tick travel. arrivalTick = T0+1, settlesAtTick = T0+2.
   174	    //   - cs1 at Forest submits MarketSell to UT (region 3) = 2 ticks travel.
   175	    //     executeAtTick = T0+2. (Same tick as cs0 settles.)
   176	    //   - vault starts at 0; cs0 has carry wood.
   177	    //   - Heartbeat at T0+2: Step 1 deposits cs0 carry wood to vault, Step 2 sells
   178	    //     it. Gold increases only if Step 1 ran first (vault was 0 before Step 1).
   179	    //   - If ordering were reversed, sell would fail (vault still 0) and gold stays flat.
   180	    // -------------------------------------------------------------------------
   181	    function test_heartbeat_settlementBeforeMarket() public {
   182	        _setupMarket();
   183	        uint32 clanId = _mintClan();
   184	        uint32 csId0 = _firstCs(clanId);
   185	        uint32 csId1 = _secondCs(clanId);
   186	
   187	        uint64 t0 = world.getWorldState().currentTick;
   188	
   189	        // Place cs0 at Mountains (region 2). Homebase = Forest (region 1).
   190	        // Deposit from Mountains to Forest: travel = 1 tick.
   191	        world.setClansmanRegion(csId0, ClanWorldConstants.REGION_MOUNTAINS);
   192	
   193	        // cs0: submit Deposit. arrivalTick = t0+1, settlesAtTick = t0+2.
   194	        OrderResult[] memory r0 = _submitOrder(clanId, csId0, ClanWorldConstants.REGION_FOREST, ActionType.DepositResources);
   195	        assertEq(uint8(r0[0].status), uint8(StatusCode.OK), "deposit order must succeed");
   196	        Mission memory depositMission = world.getActiveMission(csId0);
   197	        assertEq(depositMission.settlesAtTick, t0 + 2, "deposit settlesAtTick must be t0+2");
   198	
   199	        // cs1: at Forest. Submit MarketSell to UT. Forest→UT = 2 ticks.
   200	        // executeAtTick = t0+2. Same tick as deposit settles.
   201	        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId1, ActionType.MarketSell, address(woodToken), 5e18, 0);
   202	        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "market sell order must enqueue");
   203	        Mission memory sellMission = world.getActiveMission(csId1);
   204	        assertEq(sellMission.arrivalTick, t0 + 2, "sell arrivalTick must be t0+2");
   205	
   206	        // Give cs0 carry wood. Zero vault so market sell only succeeds if step1 ran first.
   207	        world.setCarryWood(csId0, 10e18);
   208	        world.setVaultWood(clanId, 0);
   209	        assertEq(world.getClan(clanId).vaultWood, 0, "vault wood must be 0 before test tick");
   210	
   211	        uint256 goldBefore = world.getClan(clanId).goldBalance;
   212	
   213	        // Advance to tick t0+2. The heartbeat closing t0+2 runs:
   214	        //   Step 1: _settleCompletingMissions(t0+2) → deposit fires, cs0 carry 10e18 → vault
   215	        //   Step 2: _executeScheduledMarketActions(t0+2) → sell fires, 5e18 vault wood → gold
   216	        // If reversed: sell would fail (vault=0), gold unchanged.
   217	        _advanceToTick(t0 + 3);
   218	
   219	        uint256 goldAfter = world.getClan(clanId).goldBalance;
   220	        assertGt(goldAfter, goldBefore, "gold must increase: settlement ran before market sell");
   221	        assertEq(world.getClansman(csId0).carryWood, 0, "cs0 carry wood cleared by deposit");
   222	        assertFalse(world.getActiveMission(csId0).active, "deposit mission must be complete");
   223	    }
   224	
   225	    // -------------------------------------------------------------------------
   226	    // test_heartbeat_atomicTickSeedPublish
   227	    //
   228	    // Proves Step 5: after heartbeat, currentTick increments by 1, currentTickSeed
   229	    // changes, and the new seed is deterministic from block.prevrandao + prior seed.
   230	    // -------------------------------------------------------------------------
   231	    function test_heartbeat_atomicTickSeedPublish() public {
   232	        WorldState memory before_ = world.getWorldState();
   233	        uint64 tickBefore = before_.currentTick;
   234	        bytes32 seedBefore = before_.currentTickSeed;
   235	
   236	        bytes32 expectedSeed = keccak256(abi.encode(block.prevrandao, seedBefore, tickBefore));
   237	        world.heartbeat();
   238	
   239	        WorldState memory after_ = world.getWorldState();
   240	        assertEq(after_.currentTick, tickBefore + 1, "tick must increment by 1");
   241	        assertNotEq(after_.currentTickSeed, seedBefore, "seed must change after heartbeat");
   242	        assertEq(after_.currentTickSeed, expectedSeed, "seed must match keccak(prevrandao, oldSeed, closedTick)");
   243	
   244	        // Second heartbeat chains from the new seed
   245	        uint64 tick2 = after_.currentTick;
   246	        bytes32 seed2 = after_.currentTickSeed;
   247	        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
   248	        bytes32 expectedSeed2 = keccak256(abi.encode(block.prevrandao, seed2, tick2));
   249	        world.heartbeat();
   250	
   251	        assertEq(world.getWorldState().currentTick, tick2 + 1, "tick must increment again");
   252	        assertEq(world.getWorldState().currentTickSeed, expectedSeed2, "seed must chain from prior seed");
   253	    }
   254	
   255	    // -------------------------------------------------------------------------
   256	    // test_heartbeat_seasonTransition
   257	    //
   258	    // Proves Step 4 (world events) fires AFTER Steps 1-3:
   259	    //   - season boundary is crossed at SEASON_DURATION_TICKS
   260	    //   - currentSeasonNumber increments on the heartbeat that closes tick

codex
Parallel passes are running; while they finish, I’ve confirmed one key seam risk locally: `IClanWorld.sol` changes `ResourcesDeposited` to `(..., uint32 tick)` but `packages/contracts/abi/IClanWorld.json` at `9ccf94a` still declares `(..., uint64 atTick)`, which can desync event topic hashing for downstream log consumers.
web search: 
web search: 
collab: Wait
codex
One subagent finished and corroborated the tick-width risk as low severity; I’m waiting on the remaining two specialist passes now to finalize severity and coverage conclusions.
collab: Wait
collab: Wait
codex
# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY
NEEDS_FIXES. The on-chain deposit/gathering accounting path looks internally consistent, but there is a critical integration seam break: the emitted `ResourcesDeposited` event signature no longer matches the checked-in ABI artifact, which can cause ABI-driven consumers to miss deposit logs entirely. I recommend **do not merge** until ABI artifacts are regenerated/committed and seam consumers are validated against the new signature.

## HIGH severity findings
- **Stale ABI artifact breaks event decoding at integration seams.**  
  Contract/interface now use `ResourcesDeposited(..., uint32 tick)` in [IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:535) and emit it in [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:700), and tests hash the new signature in [ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:511).  
  But ABI artifact still declares `ResourcesDeposited(..., uint64 atTick)` in [IClanWorld.json](/home/claude/code/clan-world/packages/contracts/abi/IClanWorld.json:3272).  
  Because `topic0` depends on full types, old ABI filters/parsers will not match new deposit events.

## MEDIUM severity findings
- **Deposit event tick coverage is narrow (only no-travel, early tick path).**  
  Current test checks one fixed case (`tick=1`) in [ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:559), [ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:570), but does not assert tick correctness for travel/non-zero-world-tick settlements.
- **Crit/yield validation is mostly probabilistic, not deterministic.**  
  [Gathering.t.sol](/home/claude/code/clan-world/packages/contracts/test/Gathering.t.sol:90) uses broad distribution bounds; a regression in hash input composition could still pass.

## LOW severity findings
- **Tick width is now lossy in deposit events (`uint64 -> uint32`).**  
  Cast occurs in [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:458) and event type in [IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:542). This is a very long-horizon wrap risk for off-chain dedupe keyed by tick.
- **Error-code semantics drift for deposit-at-home validation.**  
  Validation now returns `ERR_INVALID_REGION` instead of `ERR_NOT_AT_HOMEBASE` in [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1616). If UI/ops tooling keys on specific codes, behavior messaging may change.

## Cross-cutting observations
- Resource accounting in the changed deposit path is coherent: deltas are copied, vault increments, carry zeroed, then event emitted ([ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:685)).  
- Empty deposits intentionally produce no `ResourcesDeposited` event and still complete mission; tests cover this behavior ([ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:500)).  
- Wood economy balance materially changed (base/crit math and crit bps) via [IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:50) and [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:496); this looks intentional but should be explicitly signed off as a gameplay rebalance.
2026-04-30T02:43:13.791560Z ERROR codex_core::session: failed to record rollout items: thread 019ddc40-f476-73d0-8c3d-55c9717b1cca not found
2026-04-30T02:43:13.792176Z ERROR codex_core::session: failed to record rollout items: thread 019ddc40-f42e-7293-931e-e5fb98e7c729 not found
2026-04-30T02:43:13.792502Z ERROR codex_core::session: failed to record rollout items: thread 019ddc40-f3d7-76c2-93bf-0af778b708ee not found
2026-04-30T02:43:13.792952Z ERROR codex_core::session: failed to record rollout items: thread 019ddc3f-fa96-7883-a9bb-72ac1f27f8c9 not found
tokens used
235,835
# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY
NEEDS_FIXES. The on-chain deposit/gathering accounting path looks internally consistent, but there is a critical integration seam break: the emitted `ResourcesDeposited` event signature no longer matches the checked-in ABI artifact, which can cause ABI-driven consumers to miss deposit logs entirely. I recommend **do not merge** until ABI artifacts are regenerated/committed and seam consumers are validated against the new signature.

## HIGH severity findings
- **Stale ABI artifact breaks event decoding at integration seams.**  
  Contract/interface now use `ResourcesDeposited(..., uint32 tick)` in [IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:535) and emit it in [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:700), and tests hash the new signature in [ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:511).  
  But ABI artifact still declares `ResourcesDeposited(..., uint64 atTick)` in [IClanWorld.json](/home/claude/code/clan-world/packages/contracts/abi/IClanWorld.json:3272).  
  Because `topic0` depends on full types, old ABI filters/parsers will not match new deposit events.

## MEDIUM severity findings
- **Deposit event tick coverage is narrow (only no-travel, early tick path).**  
  Current test checks one fixed case (`tick=1`) in [ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:559), [ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:570), but does not assert tick correctness for travel/non-zero-world-tick settlements.
- **Crit/yield validation is mostly probabilistic, not deterministic.**  
  [Gathering.t.sol](/home/claude/code/clan-world/packages/contracts/test/Gathering.t.sol:90) uses broad distribution bounds; a regression in hash input composition could still pass.

## LOW severity findings
- **Tick width is now lossy in deposit events (`uint64 -> uint32`).**  
  Cast occurs in [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:458) and event type in [IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:542). This is a very long-horizon wrap risk for off-chain dedupe keyed by tick.
- **Error-code semantics drift for deposit-at-home validation.**  
  Validation now returns `ERR_INVALID_REGION` instead of `ERR_NOT_AT_HOMEBASE` in [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:1616). If UI/ops tooling keys on specific codes, behavior messaging may change.

## Cross-cutting observations
- Resource accounting in the changed deposit path is coherent: deltas are copied, vault increments, carry zeroed, then event emitted ([ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:685)).  
- Empty deposits intentionally produce no `ResourcesDeposited` event and still complete mission; tests cover this behavior ([ClanWorld.t.sol](/home/claude/code/clan-world/packages/contracts/test/ClanWorld.t.sol:500)).  
- Wood economy balance materially changed (base/crit math and crit bps) via [IClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/IClanWorld.sol:50) and [ClanWorld.sol](/home/claude/code/clan-world/packages/contracts/src/ClanWorld.sol:496); this looks intentional but should be explicitly signed off as a gameplay rebalance.
EXIT=0
