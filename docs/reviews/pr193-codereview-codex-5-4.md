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
session id: 019ddc40-02b9-7fc1-b860-2e0c7d98b3bd
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
codex
I’m treating this as a final pre-merge phase review, so I’m going to read the contract/tests around gathering and deposits, then split parallel passes across subagents for economics/data-flow and security/integration seams before I synthesize one cohesive verdict.
Running a multi-pass pre-merge review with parallel code inspection and delegated sidecar reviews, then consolidating findings by severity.
  → Load contract and test context around gathering, deposits, and mission settlement
  • Delegate parallel review passes for economics/data-flow and security/integration
  • Synthesize findings into requested phase review format
codex
I’m pulling the surrounding contract and test context now so we can judge the diff against actual settlement/order flow rather than reviewing the patch in isolation.
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
/bin/bash -lc "rg --files packages/contracts | rg 'AGENTS\\.md|ClanWorld\\.sol|IClanWorld\\.sol|ClanWorld\\.t\\.sol|Gathering\\.t\\.sol'" in /home/claude/code/clan-world
 succeeded in 0ms:
packages/contracts/AGENTS.md
packages/contracts/src/ClanWorld.sol
packages/contracts/src/IClanWorld.sol
packages/contracts/test/ClanWorld.t.sol

exec
/bin/bash -lc 'git status --short' in /home/claude/code/clan-world
 succeeded in 0ms:
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

codex
I’ve got the contracts package guidance; next I’m reading the mission lifecycle and constants around deposit/gathering so the delegated passes can stay tightly scoped and non-overlapping.
exec
/bin/bash -lc "sed -n '420,760p' packages/contracts/src/ClanWorld.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
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

    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint64 tick)
        internal
    {
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

        clan.vaultWood += w;
        clan.vaultIron += ir;
        clan.vaultWheat += wh;
        clan.vaultFish += fi;

        cs.carryWood = 0;
        cs.carryIron = 0;
        cs.carryWheat = 0;
        cs.carryFish = 0;

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

        if (nextLevel == 1) {
            woodCost = 20e18;
            ironCost = 0;
        } else if (nextLevel == 2) {
            woodCost = 35e18;
            ironCost = 0;
        } else if (nextLevel == 3) {
            woodCost = 30e18;
            ironCost = 5e18;
        } else if (nextLevel == 4) {
            woodCost = 40e18;
            ironCost = 10e18;
        } else {
            woodCost = 50e18;
            ironCost = 15e18;
        }

        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;

        clan.vaultWood -= woodCost;
        clan.vaultIron -= ironCost;
        uint8 old = clan.wallLevel;
        clan.wallLevel = nextLevel;
        emit WallLevelChanged(clanId, old, nextLevel, tick);

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
/bin/bash -lc "sed -n '1,260p' packages/contracts/test/Gathering.t.sol" in /home/claude/code/clan-world
 exited 2 in 0ms:
sed: can't read packages/contracts/test/Gathering.t.sol: No such file or directory

exec
/bin/bash -lc "sed -n '430,560p' packages/contracts/test/ClanWorld.t.sol" in /home/claude/code/clan-world
 succeeded in 0ms:
        // Send cs0 to Mountains to mine iron (requires travel from non-Mountains home)
        uint8 targetRegion = ClanWorldConstants.REGION_MOUNTAINS;
        (uint8 travelToMountains,) = world.quoteTravel(homeRegion, targetRegion);
        _submitOrder(clanId, cs0, targetRegion, ActionType.MineIron);

        // Advance through travel and the four-tick mining duration.
        for (uint256 i = 0; i < uint256(travelToMountains) + world.getActionDuration(ActionType.MineIron) + 1; i++) {
            _advanceTick();
        }
        world.settleClan(clanId);

        uint256 carryBefore = world.getClansman(cs0).carryIron;
        assertGt(carryBefore, 0, "cs0 should have carry iron after mining at Mountains");

        uint256 vaultBefore = world.getClan(clanId).vaultIron;

        // Wait for cs0 cooldown to expire, then send back to deposit
        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);
        _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);

        // Advance travel back to homebase + deposit duration.
        (uint8 travelBack,) = world.quoteTravel(targetRegion, homeRegion);
        for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
            _advanceTick();
        }
        world.settleClan(clanId);

        // cs0 carry iron should be cleared and vault iron should have increased
        Clansman memory cs0After = world.getClansman(cs0);
        assertEq(cs0After.carryIron, 0, "carry iron should be cleared after deposit");

        uint256 vaultAfter = world.getClan(clanId).vaultIron;
        assertGt(vaultAfter, vaultBefore, "vault iron should increase after deposit");

        // cs1 unused — suppress warning
        cs1;
    }

    function test_quoteTravel_outOfBounds_returnsZero() public {
        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 1);
        assertEq(ticks, 0, "out-of-range src should return 0 ticks");
        assertEq(path, bytes8(0), "out-of-range src should return empty path");

        (ticks, path) = world.quoteTravel(1, 9);
        assertEq(ticks, 0, "out-of-range dst should return 0 ticks");
        assertEq(path, bytes8(0), "out-of-range dst should return empty path");
    }

    // -------------------------------------------------------------------------
    // Test A: quoteTravel(9,9) — both out-of-bounds same-region, must return (0, bytes8(0))
    // -------------------------------------------------------------------------

    function test_quoteTravel_outOfBounds_sameRegion() public {
        // Previously (9,9) escaped the > 8 guard (same-region early return fired first),
        // returning (0, bytes8(uint64(9) << 56)) instead of (0, bytes8(0)).
        (uint8 ticks, bytes8 path) = world.quoteTravel(9, 9);
        assertEq(ticks, 0, "quoteTravel(9,9): travelTicks must be 0");
        assertEq(path, bytes8(0), "quoteTravel(9,9): path must be bytes8(0), not a packed 9-region sentinel");
    }

    // -------------------------------------------------------------------------
    // Test B: submitClanOrders returns ERR_INVALID_ACTION when clan is >200 ticks behind
    // -------------------------------------------------------------------------

    function test_submitClanOrders_reverts_when_clan_too_far_behind() public {
        uint32 clanId = _mintClan();
        ClanFullView memory view_ = world.getClanFullView(clanId);
        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;

        // Advance 201 ticks — clan is now 201 ticks behind its lastSettledTick
        for (uint256 i = 0; i < 201; i++) {
            _advanceTick();
        }

        // submitClanOrders should return ERR_INVALID_ACTION (ERR_MUST_SETTLE_FIRST proxy)
        // without reverting, for every order in the batch
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: ClanWorldConstants.REGION_FOREST,
            action: ActionType.ChopWood,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0
        });
        vm.prank(elder);
        OrderResult[] memory results = world.submitClanOrders(clanId, orders);

        assertEq(results.length, 1, "should return one result");
        assertEq(
            uint8(results[0].status),
            uint8(StatusCode.ERR_INVALID_ACTION),
            "clan >200 ticks behind must return ERR_INVALID_ACTION (settle-first guard)"
        );
    }

    // -------------------------------------------------------------------------
    // Test C: cooldown resets on mission interrupt
    // -------------------------------------------------------------------------

    function test_cooldown_resets_on_mission_interrupt() public {
        uint32 clanId = _mintClan();
        ClanFullView memory view_ = world.getClanFullView(clanId);
        uint32 csId = view_.clansmen[0].clansman.clansman.clansmanId;

        // Submit first mission — sends clansman to Forest to chop wood
        OrderResult[] memory r1 = _submitOrder(clanId, csId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood);
        assertEq(uint8(r1[0].status), uint8(StatusCode.OK), "first order should succeed");
        uint64 firstCooldown = r1[0].cooldownEndsAtTs;
        assertGt(firstCooldown, 0, "cooldown should be set after first order");

        // Wait for cooldown to expire
        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);

        // Advance tick so heartbeat is valid, then submit interrupt mission
        _advanceTick();

        // Submit a new mission to interrupt the first (still en-route to Forest)
        // Use MineIron in Mountains — different target, forces interrupt
        OrderResult[] memory r2 = _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);
        assertEq(uint8(r2[0].status), uint8(StatusCode.OK), "interrupt order should succeed");

        uint64 newCooldown = r2[0].cooldownEndsAtTs;
        assertGt(newCooldown, firstCooldown, "new cooldown must be later than first cooldown");
        assertGt(newCooldown, block.timestamp, "new cooldown must be in the future");
    }

    // =========================================================================
    // Phase 2 Market Tests
    // =========================================================================

codex
The working tree doesn’t currently contain the new `Gathering.t.sol`, so I’m checking the actual `9ccf94a` object and surrounding history directly to avoid reviewing against the wrong checkout.
exec
/bin/bash -lc 'git rev-parse --verify 9ccf94a''^{commit}' in /home/claude/code/clan-world
 succeeded in 1ms:
9ccf94aeb8b1c9a0001c6ddcb163d0df0bb76d3b

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/Gathering.t.sol | sed -n '1,260p'" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ClanWorldConstants,
    ActionType,
    ClansmanState,
    StatusCode,
    Clansman,
    Mission,
    ClanFullView,
    ClanOrder,
    OrderResult
} from "../src/IClanWorld.sol";

contract GatheringHarness is ClanWorld {
    function setCarryWood(uint32 csId, uint256 wood) external {
        _clansmen[csId].carryWood = wood;
    }
}

contract GatheringTest is Test {
    GatheringHarness world;
    address elder = address(0xA1);

    function setUp() public {
        world = new GatheringHarness();
    }

    function _advanceTick() internal {
        vm.warp(world.getWorldState().nextHeartbeatAtTs);
        world.heartbeat();
    }

    function _advanceUntilCurrentTick(uint64 targetTick) internal {
        while (world.getWorldState().currentTick < targetTick) {
            _advanceTick();
        }
    }

    function _mintClan() internal returns (uint32 clanId) {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _firstCs(uint32 clanId) internal view returns (uint32) {
        ClanFullView memory view_ = world.getClanFullView(clanId);
        return view_.clansmen[0].clansman.clansman.clansmanId;
    }

    function _submitChopWood(uint32 clanId, uint32 csId) internal returns (OrderResult[] memory) {
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: ClanWorldConstants.REGION_FOREST,
            action: ActionType.ChopWood,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0
        });

        vm.prank(elder);
        return world.submitClanOrders(clanId, orders);
    }

    function _settleChopWood(uint32 clanId, uint32 csId) internal returns (Clansman memory) {
        OrderResult[] memory result = _submitChopWood(clanId, csId);
        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");

        Mission memory mission = world.getActiveMission(csId);
        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
        world.settleClan(clanId);
        return world.getClansman(csId);
    }

    function test_chopWoodAtForestYieldsBaseTimesActionDuration() public {
        vm.prevrandao(bytes32(uint256(2)));
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);

        Clansman memory cs = _settleChopWood(clanId, csId);

        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
        assertEq(cs.carryWood, expected, "base wood yield");
    }

    function test_chopWoodCritDistributionAcrossSeeds() public {
        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
        uint256 critCount = 0;
        world = new GatheringHarness();
        uint256 cleanState = vm.snapshotState();

        for (uint256 i = 0; i < 100; i++) {
            assertTrue(vm.revertToState(cleanState), "reset gathering world");
            vm.prevrandao(bytes32(uint256(i + 10_000)));
            uint32 clanId = _mintClan();
            uint32 csId = _firstCs(clanId);

            Clansman memory cs = _settleChopWood(clanId, csId);
            if (cs.carryWood == baseYield * 2) {
                critCount++;
            } else {
                assertEq(cs.carryWood, baseYield, "non-crit yield");
            }
        }

        assertGe(critCount, 3, "crit count too low");
        assertLe(critCount, 20, "crit count too high");
    }

    function test_chopWoodClampsToCarryCap() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);

        Clansman memory cs = _settleChopWood(clanId, csId);

        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");
    }

    function test_chopWoodAppliesCooldownPostSettle() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);

        Clansman memory cs = _settleChopWood(clanId, csId);

        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
        assertFalse(world.getActiveMission(csId).active, "mission inactive");
        assertGt(cs.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
    }
}

exec
/bin/bash -lc 'git show --stat --oneline --decorate --no-patch 9ccf94a' in /home/claude/code/clan-world
 succeeded in 0ms:
9ccf94a (origin/dev-phase-5-economy) fix(contracts): Phase 5 — ResourcesDeposited event order + tick + 4 MED (gemini-CA r1 on #193) (Closes #201) (#234)

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '420,760p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
            _completeMission(cs, m);
            return;
        }

        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
        bytes32 woodRng = keccak256(abi.encode("wood_crit", tickSeed, cs.clansmanId, m.nonce, tick));
        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
            yield *= 2;
        }

        if (starving) yield = yield / 2;
        if (yield > remaining) yield = remaining;
        cs.carryWood += yield;

        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);

        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
            _completeMission(cs, m);
        }
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

    function _doDeposit(Clan storage clan, Clansman storage cs, Mission storage m, uint32 clanId, uint32 tick)
        internal
    {
        // Must be at homebase region
        if (cs.currentRegion != clan.baseRegion) {
            _completeMission(cs, m);
            return;
        }
        bool hasAnything = cs.carryWood > 0 || cs.carryIron > 0 || cs.carryWheat > 0 || cs.carryFish > 0;
        if (!hasAnything) {
            // Empty deposits are silent no-ops; no zero-delta event for indexers to process.
            _completeMission(cs, m);
            return;
        }

        uint256 woodDelta = cs.carryWood;
        uint256 ironDelta = cs.carryIron;
        uint256 wheatDelta = cs.carryWheat;
        uint256 fishDelta = cs.carryFish;

        clan.vaultWood += woodDelta;
        clan.vaultIron += ironDelta;
        clan.vaultWheat += wheatDelta;
        clan.vaultFish += fishDelta;

        cs.carryWood = 0;
        cs.carryIron = 0;
        cs.carryWheat = 0;
        cs.carryFish = 0;

        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
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

        if (nextLevel == 1) {
            woodCost = 20e18;
            ironCost = 0;
        } else if (nextLevel == 2) {
            woodCost = 35e18;
            ironCost = 0;
        } else if (nextLevel == 3) {
            woodCost = 30e18;
            ironCost = 5e18;
        } else if (nextLevel == 4) {
            woodCost = 40e18;
            ironCost = 10e18;
        } else {
            woodCost = 50e18;
            ironCost = 15e18;
        }

        if (clan.vaultWood < woodCost || clan.vaultIron < ironCost) return false;

        clan.vaultWood -= woodCost;
        clan.vaultIron -= ironCost;

codex
I’ve confirmed the review target commit exists and I’m now tracing submission validation, mission timing, and settle semantics around the changed actions. After that I’ll fold in the subagent results.
exec
/bin/bash -lc 'git show 9ccf94a:packages/contracts/src/ClanWorld.sol | rg -n "validate|submitClanOrders|getActionDuration|settleClan|_completeMission|executesAtTick|settlesAtTick|DepositResources|ResourcesDeposited|ERR_EMPTY_CARGO|ERR_CARRY_FULL"' in /home/claude/code/clan-world
 succeeded in 0ms:
299:    ///      Handles all 6 mission lifecycle paths. Called from _settleClan and settleClansman.
309:        // Path 6: dead clansman — invalidate active mission if any
339:            // Path 3: ACTING at/past settlesAtTick → resolve
340:            if (cs.state == ClansmanState.ACTING && tick >= m.settlesAtTick) {
342:                if (m.active && getActionDuration(m.action) > 0) {
343:                    _completeMission(cs, m);
353:    ///      Mutates storage. Called before order submission and by public settleClan().
354:    function _settleClan(uint32 clanId) internal {
455:        } else if (action == ActionType.DepositResources) {
470:            // Scheduled market actions: already enqueued at submitClanOrders time.
473:            _completeMission(cs, m);
491:            _completeMission(cs, m);
496:        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
509:            _completeMission(cs, m);
524:            _completeMission(cs, m);
538:            _completeMission(cs, m);
564:            _completeMission(cs, m);
580:            _completeMission(cs, m);
595:            _completeMission(cs, m);
611:            _completeMission(cs, m);
626:            _completeMission(cs, m);
638:            _completeMission(cs, m);
645:            _completeMission(cs, m);
665:            _completeMission(cs, m);
675:            _completeMission(cs, m);
681:            _completeMission(cs, m);
700:        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
701:        _completeMission(cs, m);
714:            _completeMission(cs, m);
730:        _completeMission(cs, m);
859:    function _completeMission(Clansman storage cs, Mission storage m) internal {
893:        // Step 1: Settle missions that complete this tick (settlesAtTick == closedTick).
901:        // TODO Phase 3: _settleClansNearBandit(closedTick);
914:    /// @dev Settle missions that complete exactly at `tick` (settlesAtTick == tick).
930:                if (m.settlesAtTick != tick) continue; // not due this tick
982:    function settleClan(uint32 clanId) external override nonReentrant {
983:        _settleClan(clanId);
989:    ///         or settleClan interchangeably; both are safe and idempotent.
990:    function settleClansman(uint32 csId) external override nonReentrant {
993:        _settleClan(cs.clanId);
1083:    function submitClanOrders(uint32 clanId, ClanOrder[] calldata orders)
1094:        // Guard: if clan is more than 200 ticks behind, caller must call settleClan() first
1095:        // (_settleClan caps at 200 ticks per call; submitting into a partially-settled clan corrupts invariants)
1114:        _settleClan(clanId);
1155:            StatusCode defendErr = _validateDefendBaseOrder(clan, order, order.gotoRegion);
1202:        StatusCode actionErr = _validateAction(clan, cs, order, ctx.gotoRegion);
1276:        m.executesAtTick = ctx.arrivalTick;
1277:        m.settlesAtTick = order.action == ActionType.DefendBase
1279:            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
1385:            // Note: _completeMission sets m.active=false during settlement (by design), so we
1605:    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
1614:        // DepositResources: must go to homebase
1615:        if (action == ActionType.DepositResources) {
1655:            return _validateDefendBaseOrder(clan, order, gotoRegion);
1687:    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
1796:        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
1799:    function getActionDuration(ActionType action) public pure override returns (uint64) {
1807:        if (action == ActionType.DepositResources) {
1898:    ///      Carry amounts lag until next settleClan().
2002:    ///      current; carry amounts and wheat progress lag until next settleClan() call.

exec
/bin/bash -lc 'git show 9ccf94a:packages/contracts/src/IClanWorld.sol | rg -n "event ResourcesDeposited|WOOD_|CARRY|ERR_EMPTY_CARGO|ERR_CARRY_FULL|ActionType|StatusCode"' in /home/claude/code/clan-world
 succeeded in 0ms:
43:    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
44:    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
50:    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
51:    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
52:    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
53:    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%
65:    uint256 internal constant WINTER_WOOD_BURN_PER_BASE = 1e18;
130:enum ActionType {
153:enum StatusCode {
169:    ERR_EMPTY_CARGO,
181:    ERR_CARRY_FULL
287:    ActionType action;
326:    ActionType action; // MarketBuy or MarketSell
369:    ActionType action;
379:    StatusCode status;
483:    ActionType currentAction;
511:        ActionType action,
520:    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
527:        ActionType action,
535:    event ResourcesDeposited(
575:        ActionType action,
580:    event MarketActionFailed(uint32 indexed clanId, uint32 indexed clansmanId, ActionType action, StatusCode reason);
715:    function getActionDuration(ActionType action) external pure returns (uint64);

exec
/bin/bash -lc 'git show 9ccf94a:packages/contracts/test/ClanWorld.t.sol | rg -n "deposit|DepositResources|ResourcesDeposited|gather|wood|carry cap|ERR_INVALID_REGION"' in /home/claude/code/clan-world
 succeeded in 0ms:
52:    function setCarry(uint32 csId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
53:        _clansmen[csId].carryWood = wood;
66:    MinimalERC20 woodToken;
72:    StubPool woodPool;
81:    /// @dev Deploy tokens + pools, call initTreasury + seedPools. Returns wood token address.
82:    function _setupMarket() internal returns (address woodAddr) {
83:        woodToken = new MinimalERC20("Wood", "WOOD");
91:        woodPool = new StubPool(address(woodToken), address(goldToken), wAddr);
97:            address(woodToken),
104:        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
108:        // Seed: 1000 wood + 1000 gold per pool (spot price 1 gold / 1 wood)
112:            woodSeed: resSeed,
123:        return address(woodToken);
267:        assertGt(clan.vaultWood, 0, "should have starting wood");
303:        // Valid order: send to forest to chop wood
388:    // Test 9: settlement — travel + gather
421:        assertGt(cs.carryIron, 0, "clansman should have gathered iron after travel to Mountains");
425:    // Test 10: settlement — deposit adds to vault and clears carry
428:    function test_settlement_depositAddsToVault() public {
433:        // Use two clansmen: one mines iron, one stays home to deposit
453:        // Wait for cs0 cooldown to expire, then send back to deposit
455:        _submitOrder(clanId, cs0, homeRegion, ActionType.DepositResources);
457:        // Advance through travel back to homebase and the deposit's 1-tick transfer.
459:        for (uint256 i = 0; i < uint256(travelBack) + world.getActionDuration(ActionType.DepositResources) + 1; i++) {
466:        assertEq(cs0After.carryIron, 0, "carry iron should be cleared after deposit");
469:        assertGt(vaultAfter, vaultBefore, "vault iron should increase after deposit");
475:    function test_depositResources_woodCarryMovesToVaultAndClears() public {
478:        uint256 woodDelta = 5e18;
480:        harness.setCarry(csId, woodDelta, 0, 0, 0);
483:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
484:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
489:            mission.executesAtTick + harness.getActionDuration(ActionType.DepositResources),
490:            "deposit settles after transfer duration"
496:        assertEq(harness.getClan(clanId).vaultWood, vaultBefore + woodDelta, "vault wood receives carried wood");
497:        assertEq(harness.getClansman(csId).carryWood, 0, "carry wood is cleared");
500:    function test_depositResources_emptyCarryNoopsWithoutEvent() public {
504:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
505:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "empty deposit order should be accepted");
511:        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
514:            assertTrue(logs[i].topics[0] != depositedTopic, "empty deposit emits no ResourcesDeposited event");
517:        assertFalse(harness.getActiveMission(csId).active, "empty deposit still completes");
520:    function test_depositResources_multipleTypesMoveTogether() public {
523:        uint256 woodDelta = 4e18;
527:        harness.setCarry(csId, woodDelta, ironDelta, 0, fishDelta);
530:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
531:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
539:        assertEq(afterClan.vaultWood, beforeClan.vaultWood + woodDelta, "wood transferred");
542:        assertEq(afterCs.carryWood, 0, "wood carry cleared");
547:    function test_depositResources_requiresHomeRegion() public {
554:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, nonHomeRegion, ActionType.DepositResources);
556:        assertEq(uint8(r[0].status), uint8(StatusCode.ERR_INVALID_REGION), "deposit must target home region");
559:    function test_depositResources_eventHasCorrectDeltas() public {
565:        OrderResult[] memory r = _submitOrderHarness(harness, clanId, csId, homeRegion, ActionType.DepositResources);
566:        assertEq(uint8(r[0].status), uint8(StatusCode.OK), "deposit order should be accepted");
570:        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
642:        // Submit first mission — sends clansman to Forest to chop wood
730:        address woodAddr = _setupMarket();
734:        // Clan starts with 20 wood in vault (starter pack)
738:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 5e18, 0);
764:        address woodAddr = _setupMarket();
775:        // Submit buy order for 1e18 wood, maxGoldIn = generous 500e18
776:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 500e18);
793:        assertGt(vaultWoodAfter, vaultWoodBefore, "vault wood should increase after buy");
801:        address woodAddr = _setupMarket();
806:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketBuy, woodAddr, 1e18, 0);
837:        address woodAddr = _setupMarket();
841:        OrderResult[] memory r = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
863:        address woodAddr = _setupMarket();
867:        OrderResult[] memory r1 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 1e18, 0);
875:        OrderResult[] memory r2 = _submitMarketOrder(clanId, csId, ActionType.MarketSell, woodAddr, 2e18, 0);
900:        address woodAddr = _setupMarket();
919:            totalQueued += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
925:            totalQueued += _submitAllClanMarketSells(distOneClans[i], woodAddr);
952:        address woodAddr = _setupMarket();
974:        uint256 totalQueuedForTickTwo = _submitFirstClanMarketSells(nativeClanId, woodAddr, 3);
976:            totalQueuedForTickTwo += _submitAllClanMarketSells(distTwoClans[i], woodAddr);
982:            totalQueuedForTickTwo += _submitAllClanMarketSells(distOneClans[i], woodAddr);
986:            _submitMarketOrder(nativeClanId, nativeCsId, ActionType.MarketSell, woodAddr, 1e18, 0);
1045:        address woodAddr = _setupMarket();
1062:            marketToken: woodAddr,
1077:            marketToken: woodAddr,
1117:    // Test 16: twoClan_sellBuyCycle — clan A sells wood, clan B buys wood; both succeed
1121:        address woodAddr = _setupMarket();
1130:        // Clan 1: sell 5e18 wood
1137:            marketToken: woodAddr,
1144:        // Clan 2: buy 1e18 wood with maxGoldIn = 100e18
1151:            marketToken: woodAddr,
1171:        // Clan1 sold wood → gold should increase
1173:        // Clan2 bought wood → vault wood should increase beyond starter 20e18
1174:        assertGt(world.getClan(clanId2).vaultWood, 20e18, "clan2 should have more vault wood after buy");
1184:        address woodAddr = _setupMarket();
1195:            marketToken: woodAddr,
1201:        assertEq(uint8(results[0].status), uint8(StatusCode.ERR_INVALID_REGION), "market sell to Forest should fail");
1402:        // Send clansman to homebase to DepositResources (empty carry -> completes when the arrival tick closes).
1404:        OrderResult[] memory r = _submitOrder(clanId, csId, homeRegion, ActionType.DepositResources);
1524:    // 3.E6: ERR_INVALID_REGION — wrong region for action type
1535:        // ChopWood to Mountains (not Forest) → ERR_INVALID_REGION
1538:            uint8(r0[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: ChopWood to Mountains must be invalid"
1541:        // MineIron to Forest (not Mountains) → ERR_INVALID_REGION
1543:        assertEq(uint8(r1[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: MineIron to Forest must be invalid");
1545:        // FishDocks to Forest (not WestDocks or EastDocks) → ERR_INVALID_REGION
1547:        assertEq(uint8(r2[0].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E6: FishDocks to Forest must be invalid");
1652:        assertEq(csAfter.carryIron, 0, "path1: no iron gathered (haven't arrived)");
1712:        assertGt(csAfter.carryIron, 0, "path3: clansman should have gathered iron after action resolved");
2015:            uint8(results[3].status), uint8(StatusCode.ERR_INVALID_REGION), "3.E8: order[3] must be ERR_INVALID_REGION"

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '299,380p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '1083,1295p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
        ctx.targetClanId =
            order.action == ActionType.DefendBase && order.targetClanId == 0 ? clanId : order.targetClanId;

        // NOOP bypass: treat 0 as "stay here"; DefendBase requires explicit home region.
        ctx.isNoop = order.action != ActionType.DefendBase
            && (ctx.gotoRegion == ClanWorldConstants.REGION_NOOP || ctx.gotoRegion == ctx.fromRegion);
        if (ctx.isNoop) {
            ctx.gotoRegion = ctx.fromRegion;
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

        // Compute travel from the clansman's current region to the order target.
        ctx.travelTicks = _travelTicks(ctx.fromRegion, ctx.gotoRegion);
        ctx.arrivalTick = _addTicksClamped(_world.currentTick, uint64(ctx.travelTicks));

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

        _clearDefender(cs.clansmanId);

        // v4.2 §8 L758: "executes at heartbeat closing tick T" where T = arrivalTick.
        // executeAtTick = arrivalTick (not arrivalTick+1).
        if (order.action == ActionType.MarketBuy || order.action == ActionType.MarketSell) {
            _enqueueScheduledMarketAction(clanId, order, cs.clansmanId, ctx.arrivalTick, ctx.newNonce);
        }

        if (order.action == ActionType.DefendBase && ctx.travelTicks == 0) {
            _registerDefender(ctx.gotoRegion, clanId, cs.clansmanId);
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

    function _installMission(Mission storage m, ClanOrder calldata order, Clansman storage cs, OrderCtx memory ctx)
        internal
    {
        m.active = true;
        m.nonce = ctx.newNonce;
        m.clansmanId = cs.clansmanId;
        m.submittedAtTick = _world.currentTick;
        m.executesAtTick = ctx.arrivalTick;
        m.settlesAtTick = order.action == ActionType.DefendBase
            ? type(uint64).max
            : _addTicksClamped(ctx.arrivalTick, getActionDuration(order.action));
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
        m.targetClanId = ctx.targetClanId;
        m.marketToken = order.marketToken;
        m.marketAmount = order.marketAmount;
        m.maxGoldIn = order.maxGoldIn;
    }


exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '1590,1825p'" in /home/claude/code/clan-world
 succeeded in 0ms:
        if (clan.goldBalance < goldIn) {
            emit MarketActionFailed(clanId, clansmanId, ActionType.MarketBuy, StatusCode.ERR_NOT_ENOUGH_GOLD);
            return;
        }

        // Execute — use return value to guard against any future pool divergence
        uint256 actualGoldIn = StubPool(poolAddr).buyResource(amountOut);
        clan.goldBalance -= actualGoldIn;
        _addToVault(clan, token, amountOut);

        emit ScheduledMarketActionExecuted(
            closedTick, commitSequence, clanId, clansmanId, _treasury.goldToken, token, actualGoldIn, amountOut
        );
    }

    function _validateAction(Clan storage clan, Clansman storage cs, ClanOrder calldata order, uint8 gotoRegion)
        internal
        view
        returns (StatusCode)
    {
        ActionType action = order.action;

        if (action == ActionType.None) return StatusCode.ERR_INVALID_ACTION;

        // DepositResources: must go to homebase
        if (action == ActionType.DepositResources) {
            if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
        }

        // BuildWall / UpgradeBase / UpgradeMonument: must go to homebase
        if (action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument)
        {
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
            if (
                gotoRegion != ClanWorldConstants.REGION_WEST_DOCKS && gotoRegion != ClanWorldConstants.REGION_EAST_DOCKS
            ) {
                return StatusCode.ERR_INVALID_REGION;
            }
        }
        // FishDeepSea: must go to DeepSea
        if (action == ActionType.FishDeepSea) {
            if (gotoRegion != ClanWorldConstants.REGION_DEEP_SEA) return StatusCode.ERR_INVALID_REGION;
        }
        // HarvestWheat: must go to WestFarms or EastFarms
        if (action == ActionType.HarvestWheat) {
            if (
                gotoRegion != ClanWorldConstants.REGION_WEST_FARMS && gotoRegion != ClanWorldConstants.REGION_EAST_FARMS
            ) {
                return StatusCode.ERR_INVALID_REGION;
            }
        }

        if (action == ActionType.DefendBase) {
            return _validateDefendBaseOrder(clan, order, gotoRegion);
        }

        // MarketBuy/MarketSell: must target Unicorn Town
        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            if (_treasury.woodToken == address(0)) return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
            if (gotoRegion != ClanWorldConstants.REGION_UNICORN_TOWN) {
                return StatusCode.ERR_INVALID_REGION;
            }
            if (order.marketAmount == 0) return StatusCode.ERR_MARKET_ZERO_AMOUNT;
            // Validate token is a supported resource token (not gold itself)
            address tok = order.marketToken;
            if (tok == address(0) || tok == _treasury.goldToken) {
                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
            }
            if (
                tok != _treasury.woodToken && tok != _treasury.ironToken && tok != _treasury.wheatToken
                    && tok != _treasury.fishToken
            ) {
                return StatusCode.ERR_MARKET_UNSUPPORTED_TOKEN;
            }
            // Market orders are always enqueued for the arrivalTick FIFO queue.
            // _resolveAction records mission completion but does not execute any swap.
            if (cs.currentRegion == ClanWorldConstants.REGION_UNICORN_TOWN && cs.state == ClansmanState.WAITING) {
                // Already at Unicorn Town — arrivalTick == currentTick, queued for next heartbeat.
            }
        }

        cs; // suppress unused warning
        return StatusCode.OK;
    }

    function _validateDefendBaseOrder(Clan storage clan, ClanOrder calldata order, uint8 gotoRegion)
        internal
        view
        returns (StatusCode)
    {
        if (gotoRegion != clan.baseRegion) return StatusCode.ERR_INVALID_REGION;
        if (order.targetClanId != 0 && order.targetClanId != clan.clanId) return StatusCode.ERR_INVALID_TARGET;
        return StatusCode.OK;
    }

    // =========================================================================
    // TREASURY / POOL SEEDING
    // =========================================================================

    /// @notice One-time treasury initialization: register token and pool addresses.
    ///         Must be called before seedPools. Callable only once.
    function initTreasury(address[6] calldata tokens, address[4] calldata pools) external override nonReentrant {
        require(!_treasury.poolsSeeded && _treasury.woodToken == address(0), "ClanWorld: treasury already init");
        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");

        _treasury.woodToken = tokens[0];
        _treasury.ironToken = tokens[1];
        _treasury.wheatToken = tokens[2];
        _treasury.fishToken = tokens[3];
        _treasury.goldToken = tokens[4];
        _treasury.blueprintToken = tokens[5];

        _treasury.woodGoldPool = pools[0];
        _treasury.wheatGoldPool = pools[1];
        _treasury.fishGoldPool = pools[2];
        _treasury.ironGoldPool = pools[3];
    }

    /// @notice Owner-only. Seeds the four Unicorn Town AMM pools.
    function seedPools(PoolSeedConfig calldata cfg) external override nonReentrant {
        require(msg.sender == _treasury.treasuryOwner, "ClanWorld: not owner");
        require(!_treasury.poolsSeeded, "ClanWorld: pools already seeded");
        require(_treasury.woodToken != address(0), "ClanWorld: treasury not init");

        StubPool(_treasury.woodGoldPool).seed(cfg.woodSeed, cfg.goldSeedForWood);
        StubPool(_treasury.wheatGoldPool).seed(cfg.wheatSeed, cfg.goldSeedForWheat);
        StubPool(_treasury.fishGoldPool).seed(cfg.fishSeed, cfg.goldSeedForFish);
        StubPool(_treasury.ironGoldPool).seed(cfg.ironSeed, cfg.goldSeedForIron);

        _treasury.poolsSeeded = true;

        emit PoolsSeeded(
            _treasury.woodGoldPool, _treasury.wheatGoldPool, _treasury.fishGoldPool, _treasury.ironGoldPool
        );
    }

    // =========================================================================
    // OTC TRANSFERS
    // =========================================================================

    function transferGold(uint32, uint32, uint256) external pure override {
        revert("OTC transfers not implemented");
    }

    function transferVaultResource(uint32, uint32, ResourceType, uint256) external pure override {
        revert("OTC transfers not implemented");
    }

    function transferBlueprint(uint32, uint32, uint256) external pure override {
        revert("OTC transfers not implemented");
    }

    function transferBundle(uint32, uint32, uint256, uint256, uint256, uint256, uint256, uint256)
        external
        pure
        override
    {
        revert("OTC transfers not implemented");
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

    function getMissionTiming(uint32 clanId, uint32 clansmanId)
        external
        view
        override
        returns (uint64 submitted, uint64 executes, uint64 settles)
    {
        Mission memory m = _missions[clansmanId];
        if (!m.active || _clansmen[clansmanId].clanId != clanId) {
            return (0, 0, 0);
        }
        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
    }

    function getActionDuration(ActionType action) public pure override returns (uint64) {
        if (
            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
        ) {
            return 4;
        }

        if (action == ActionType.DepositResources) {
            return DEPOSIT_DURATION_TICKS;
        }

        if (
            action == ActionType.BuildWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
                || action == ActionType.MarketBuy || action == ActionType.MarketSell
        ) {
            return 1;
        }

        return 0;
    }

    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure override returns (uint64) {
        return uint64(_travelTicks(fromRegion, toRegion));
    }

    function getBanditTroop(uint32) external pure override returns (BanditTroop memory) {

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '850,950p'" in /home/claude/code/clan-world
 succeeded in 0ms:
        clan.blueprintBalance -= blueprintCost;

        uint8 old = clan.monumentLevel;
        clan.monumentLevel = nextLevel;
        emit MonumentLevelChanged(clanId, old, nextLevel, tick);
        return true;
    }

    /// @dev Complete a mission: set worker to WAITING, set cooldown, mark mission inactive, emit event.
    function _completeMission(Clansman storage cs, Mission storage m) internal {
        cs.state = ClansmanState.WAITING;
        cs.cooldownEndsAtTs = uint64(block.timestamp) + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS;
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

codex
I have enough local context to split the review cleanly now. I’m delegating three independent sidecar passes: economy/resource-accounting, ABI/indexer integration drift, and security/error-path/test coverage, while I keep tracing the contract flow myself.
2026-04-30T02:38:58.273242Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-30T02:38:58.323630Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
2026-04-30T02:38:58.328923Z ERROR codex_core::tools::router: error=Full-history forked agents inherit the parent agent type, model, and reasoning effort; omit agent_type, model, and reasoning_effort, or spawn without a full-history fork.
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
collab: SpawnAgent
exec
/bin/bash -lc 'git grep -n "ResourcesDeposited" 9ccf94a -- .' in /home/claude/code/clan-world
exec
/bin/bash -lc "git grep -n \"WOOD_CAP\\|CLANSMAN_CARRY_CAP\\|WOOD_BASE_YIELD\\|WOOD_YIELD_PER_TICK\\|WOOD_CRIT_BONUS\\|WOOD_CRIT_BPS\" 9ccf94a -- ." in /home/claude/code/clan-world
exec
/bin/bash -lc "git grep -n \"ERR_EMPTY_CARGO\\|ERR_CARRY_FULL\\|DepositResources\" 9ccf94a -- ':(exclude)packages/contracts/src/ClanWorld.sol' ':(exclude)packages/contracts/src/IClanWorld.sol'" in /home/claude/code/clan-world
 succeeded in 103ms:
 succeeded in 137ms:
9ccf94a:packages/contracts/src/ClanWorld.sol:490:        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
9ccf94a:packages/contracts/src/ClanWorld.sol:495:        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
9ccf94a:packages/contracts/src/ClanWorld.sol:496:        uint256 yield = ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(getActionDuration(ActionType.ChopWood));
9ccf94a:packages/contracts/src/ClanWorld.sol:498:        if (uint256(woodRng) % 10000 < ClanWorldConstants.WOOD_CRIT_BPS) {
9ccf94a:packages/contracts/src/ClanWorld.sol:508:        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
9ccf94a:packages/contracts/src/IClanWorld.sol:43:    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
9ccf94a:packages/contracts/src/IClanWorld.sol:44:    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
9ccf94a:packages/contracts/src/IClanWorld.sol:50:    uint256 internal constant WOOD_YIELD_PER_TICK = 1e18;
9ccf94a:packages/contracts/src/IClanWorld.sol:51:    uint256 internal constant WOOD_BASE_YIELD = WOOD_YIELD_PER_TICK;
9ccf94a:packages/contracts/src/IClanWorld.sol:52:    uint256 internal constant WOOD_CRIT_BONUS = WOOD_YIELD_PER_TICK;
9ccf94a:packages/contracts/src/IClanWorld.sol:53:    uint16 internal constant WOOD_CRIT_BPS = 1000; // 10%
9ccf94a:packages/contracts/test/Gathering.t.sol:86:        uint256 expected = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
9ccf94a:packages/contracts/test/Gathering.t.sol:91:        uint256 baseYield = ClanWorldConstants.WOOD_YIELD_PER_TICK * world.getActionDuration(ActionType.ChopWood);
9ccf94a:packages/contracts/test/Gathering.t.sol:117:        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);
9ccf94a:packages/contracts/test/Gathering.t.sol:121:        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");

 succeeded in 112ms:
codex
I’m checking the authored spec/docs next, because one of the bigger review questions here is whether the phase implementation actually matches the declared contract and economy model, not just whether the Solidity compiles.
exec
/bin/bash -lc 'git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | rg -n "DepositResources|ResourcesDeposited|WOOD_CAP|WOOD_BASE_YIELD|WOOD_CRIT_BPS|ERR_EMPTY_CARGO|ERR_CARRY_FULL|carry cap|wood"' in /home/claude/code/clan-world
 succeeded in 0ms:
137:- tick 308 is the first wood-gather action tick
185:- wood
197:- wood
227:Carried resources only enter the clan vault after `DepositResources` resolves at homebase.
255:uint256 WOOD_CAP  = 15e18;
260:uint256 WOOD_BASE_YIELD = 2e18;
262:uint16  WOOD_CRIT_BPS = 2000;      // 20%
358:    address woodToken;
365:    address woodGoldPool;
584:- requested output must fit within remaining carry capacity of that worker
673:    uint256 wood,
717:    uint256 woodSeed;
818:Because upkeep is applied before deposit effects, a worker arriving home with wood/food in carry does **not** save the base from that same tick’s winter burn or upkeep shortage.
826:- worker has 14 wood already carried
827:- 1 wood capacity remains
828:- tick roll would yield 3 wood
829:- worker only gains 1 wood
859:event ResourcesDeposited(uint32 indexed clanId, uint32 indexed clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish);
912:    ERR_EMPTY_CARGO,
962:- worker carries 12 wood
963:- Elder submits `MarketSell(wood, 12e18)`
975:- Elder submits `MarketBuy(wood, 5e18, 10e18)`
980:- heartbeat closing tick 308 attempts to buy exactly 5 wood spending at most 10 gold

exec
/bin/bash -lc 'git show 9ccf94a:docs/planning/clanworld_v4_3_schema_patch.md | rg -n "ERR_CARRY_FULL|ERR_EMPTY_CARGO|DepositResources|carry|market buy|resource|vault"' in /home/claude/code/clan-world
 succeeded in 0ms:
202:- reads currently committed vault balances only
273:- `ERR_CARRY_FULL`
275:`ERR_CARRY_FULL` is especially relevant for explicit market buy validation and any direct action that is rejected because requested output cannot fit remaining carry capacity.

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/abi/IClanWorld.json | rg -n 'ResourcesDeposited|woodDelta|tick|uint32'" in /home/claude/code/clan-world
 succeeded in 0ms:
27:              "type": "uint32",
28:              "internalType": "uint32"
92:              "type": "uint32",
93:              "internalType": "uint32"
111:          "type": "uint32",
112:          "internalType": "uint32"
118:          "type": "uint32[]",
119:          "internalType": "uint32[]"
130:          "type": "uint32",
131:          "internalType": "uint32"
167:              "type": "uint32",
168:              "internalType": "uint32"
212:              "type": "uint32",
213:              "internalType": "uint32"
241:          "type": "uint32",
242:          "internalType": "uint32"
246:          "type": "uint32",
247:          "internalType": "uint32"
318:          "type": "uint32",
319:          "internalType": "uint32"
325:          "type": "uint32",
326:          "internalType": "uint32"
337:          "type": "uint32",
338:          "internalType": "uint32"
349:              "type": "uint32",
350:              "internalType": "uint32"
418:          "type": "uint32",
419:          "internalType": "uint32"
430:              "type": "uint32",
431:              "internalType": "uint32"
529:          "type": "uint32",
530:          "internalType": "uint32"
551:                      "type": "uint32",
552:                      "internalType": "uint32"
675:                          "type": "uint32",
676:                          "internalType": "uint32"
680:                          "type": "uint32",
681:                          "internalType": "uint32"
757:                          "type": "uint32",
758:                          "internalType": "uint32"
802:                          "type": "uint32",
803:                          "internalType": "uint32"
866:                      "type": "uint32",
867:                      "internalType": "uint32"
911:                      "type": "uint32",
912:                      "internalType": "uint32"
989:              "type": "uint32[]",
990:              "internalType": "uint32[]"
994:              "type": "uint32",
995:              "internalType": "uint32"
1008:          "type": "uint32",
1009:          "internalType": "uint32"
1020:              "type": "uint32",
1021:              "internalType": "uint32"
1025:              "type": "uint32",
1026:              "internalType": "uint32"
1086:          "type": "uint32[]",
1087:          "internalType": "uint32[]"
1098:          "type": "uint32",
1099:          "internalType": "uint32"
1115:                  "type": "uint32",
1116:                  "internalType": "uint32"
1231:          "type": "uint32",
1232:          "internalType": "uint32"
1248:                  "type": "uint32",
1249:                  "internalType": "uint32"
1253:                  "type": "uint32",
1254:                  "internalType": "uint32"
1330:                  "type": "uint32",
1331:                  "internalType": "uint32"
1375:                  "type": "uint32",
1376:                  "internalType": "uint32"
1555:                  "type": "uint32",
1556:                  "internalType": "uint32"
1560:                  "type": "uint32",
1561:                  "internalType": "uint32"
1607:                  "type": "uint32",
1608:                  "internalType": "uint32"
1612:                  "type": "uint32",
1613:                  "internalType": "uint32"
1660:              "type": "uint32",
1661:              "internalType": "uint32"
1665:              "type": "uint32",
1666:              "internalType": "uint32"
1693:          "name": "tick",
1721:              "type": "uint32",
1722:              "internalType": "uint32"
1726:              "type": "uint32",
1727:              "internalType": "uint32"
1840:          "type": "uint32",
1841:          "internalType": "uint32"
1949:              "type": "uint32",
1950:              "internalType": "uint32"
1964:                  "type": "uint32",
1965:                  "internalType": "uint32"
2061:              "type": "uint32",
2062:              "internalType": "uint32"
2127:          "type": "uint32",
2128:          "internalType": "uint32"
2144:          "type": "uint32",
2145:          "internalType": "uint32"
2163:          "type": "uint32",
2164:          "internalType": "uint32"
2266:          "type": "uint32",
2267:          "internalType": "uint32"
2279:          "type": "uint32",
2280:          "internalType": "uint32"
2289:              "type": "uint32",
2290:              "internalType": "uint32"
2304:              "type": "uint32",
2305:              "internalType": "uint32"
2333:              "type": "uint32",
2334:              "internalType": "uint32"
2362:          "type": "uint32",
2363:          "internalType": "uint32"
2367:          "type": "uint32",
2368:          "internalType": "uint32"
2385:          "type": "uint32",
2386:          "internalType": "uint32"
2390:          "type": "uint32",
2391:          "internalType": "uint32"
2433:          "type": "uint32",
2434:          "internalType": "uint32"
2438:          "type": "uint32",
2439:          "internalType": "uint32"
2456:          "type": "uint32",
2457:          "internalType": "uint32"
2461:          "type": "uint32",
2462:          "internalType": "uint32"
2484:          "type": "uint32",
2486:          "internalType": "uint32"
2490:          "type": "uint32",
2492:          "internalType": "uint32"
2557:          "type": "uint32",
2559:          "internalType": "uint32"
2563:          "type": "uint32",
2565:          "internalType": "uint32"
2582:          "type": "uint32",
2584:          "internalType": "uint32"
2601:          "type": "uint32",
2603:          "internalType": "uint32"
2632:          "type": "uint32",
2634:          "internalType": "uint32"
2663:          "type": "uint32",
2665:          "internalType": "uint32"
2700:          "type": "uint32",
2702:          "internalType": "uint32"
2731:          "type": "uint32",
2733:          "internalType": "uint32"
2737:          "type": "uint32",
2739:          "internalType": "uint32"
2756:          "type": "uint32",
2758:          "internalType": "uint32"
2762:          "type": "uint32",
2764:          "internalType": "uint32"
2787:          "type": "uint32",
2789:          "internalType": "uint32"
2792:          "name": "tick",
2806:          "type": "uint32",
2808:          "internalType": "uint32"
2825:          "type": "uint32",
2827:          "internalType": "uint32"
2862:          "type": "uint32",
2864:          "internalType": "uint32"
2887:          "type": "uint32",
2889:          "internalType": "uint32"
2906:          "type": "uint32",
2908:          "internalType": "uint32"
2937:          "type": "uint32",
2939:          "internalType": "uint32"
2943:          "type": "uint32",
2945:          "internalType": "uint32"
2968:          "type": "uint32",
2970:          "internalType": "uint32"
2974:          "type": "uint32",
2976:          "internalType": "uint32"
3017:          "type": "uint32",
3019:          "internalType": "uint32"
3023:          "type": "uint32",
3025:          "internalType": "uint32"
3029:          "type": "uint32",
3031:          "internalType": "uint32"
3066:          "type": "uint32",
3068:          "internalType": "uint32"
3072:          "type": "uint32",
3074:          "internalType": "uint32"
3097:          "type": "uint32",
3099:          "internalType": "uint32"
3103:          "type": "uint32",
3105:          "internalType": "uint32"
3152:          "type": "uint32",
3154:          "internalType": "uint32"
3158:          "type": "uint32",
3160:          "internalType": "uint32"
3183:          "type": "uint32",
3185:          "internalType": "uint32"
3189:          "type": "uint32",
3191:          "internalType": "uint32"
3214:          "type": "uint32",
3216:          "internalType": "uint32"
3272:      "name": "ResourcesDeposited",
3276:          "type": "uint32",
3278:          "internalType": "uint32"
3282:          "type": "uint32",
3284:          "internalType": "uint32"
3325:          "type": "uint32",
3327:          "internalType": "uint32"
3331:          "type": "uint32",
3333:          "internalType": "uint32"
3398:          "type": "uint32",
3400:          "internalType": "uint32"
3404:          "type": "uint32",
3406:          "internalType": "uint32"
3453:          "type": "uint32",
3455:          "internalType": "uint32"
3459:          "type": "uint32",
3461:          "internalType": "uint32"
3495:          "name": "tick",
3502:          "type": "uint32[]",
3504:          "internalType": "uint32[]"
3526:          "name": "tickSeed",
3540:          "type": "uint32",
3542:          "internalType": "uint32"
3546:          "type": "uint32",
3548:          "internalType": "uint32"
3577:          "type": "uint32",
3579:          "internalType": "uint32"
3607:          "name": "tick",
3620:          "name": "tick",
3634:          "type": "uint32",
3636:          "internalType": "uint32"
3640:          "type": "uint32",
3642:          "internalType": "uint32"
3651:          "name": "tick",

exec
/bin/bash -lc "git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | sed -n '240,285p'" in /home/claude/code/clan-world
 succeeded in 0ms:
These are config constants or governance-set params for v4.2.

```solidity
uint64 TICK_SECONDS = 60;
uint64 TICKS_PER_WINTER_CYCLE = 110;
uint64 WINTER_DURATION_TICKS = 10;
uint64 SEASON_DURATION_TICKS = 360;

uint64 BANDIT_COOLDOWN_TICKS = 10;
uint64 BANDIT_CAMP_TICKS = 3;
uint64 BANDIT_REST_TICKS = 2;
uint8  BANDIT_MAX_ATTACK_ATTEMPTS = 6;

uint64 CLANSMAN_COOLDOWN_SECONDS = 60;

uint256 WOOD_CAP  = 15e18;
uint256 IRON_CAP  = 5e18;
uint256 WHEAT_CAP = 40e18;
uint256 FISH_CAP  = 8e18;

uint256 WOOD_BASE_YIELD = 2e18;
uint256 WOOD_CRIT_BONUS = 1e18;
uint16  WOOD_CRIT_BPS = 2000;      // 20%

uint256 IRON_BASE_YIELD = 5e17;    // 0.5e18, literal fractional yield
uint16  GOLD_FROM_IRON_BPS = 200;  // 2%
uint256 GOLD_FROM_IRON_AMOUNT = 1e18;

uint16  FISH_DOCKS_BPS = 2500;     // 25%
uint16  FISH_DEEP_BPS = 7500;      // 75%

uint256 WHEAT_UPKEEP_PER_CLANSMAN = 1e18;
uint256 FISH_UPKEEP_PER_CLANSMAN = 1e17; // 0.1 fish
uint256 WINTER_WOOD_BURN_PER_BASE = 1e18;

uint16 BANDIT_BASE_STEAL_BPS = 2000;          // 20%
uint16 BANDIT_DROP_TO_DEFENDERS_BPS = 5000;   // 50%
```

---

## 6. Route model

### 6.1 Canonical path table
All travel is resolved using a canonical, deterministic route table for `(srcRegion, dstRegion)`.


exec
/bin/bash -lc "git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | sed -n '810,835p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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
Normal gathering yield is clamped to the worker’s remaining capacity.

Example:
- worker has 14 wood already carried
- 1 wood capacity remains
- tick roll would yield 3 wood
- worker only gains 1 wood
- mission then transitions to `Waiting`

### 12.7 Summer starvation
Outside winter, starvation is nonlethal.

A starving clan:

exec
/bin/bash -lc "git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | sed -n '950,985p'" in /home/claude/code/clan-world
 succeeded in 0ms:
- worker is one tick from Forest
- Elder submits `(goto Forest, ChopWood)` during tick 307

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
- if price moved above 10 gold or purse balance is insufficient, trade fails, worker ends `Waiting`, cooldown consumed

### 16.4 Starving mercenary
If Clan B has a worker defending Clan A, and Clan B later enters starvation:
- that defending worker contributes 0 defense until Clan B starvation ends

exec
/bin/bash -lc 'git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | rg -n "continuous action|four-tick|4 tick|4-tick|MineIron|FishDocks|HarvestWheat|WOOD_BASE_YIELD|IRON_BASE_YIELD|ResourcesGathered"' in /home/claude/code/clan-world
 succeeded in 0ms:
260:uint256 WOOD_BASE_YIELD = 2e18;
264:uint256 IRON_BASE_YIELD = 5e17;    // 0.5e18, literal fractional yield
419:- after 4 ticks: `Regrowing -> Harvestable` with reset `remainingWheat`
610:function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory);
805:- unless the action is a persistent continuous action like `DefendBase`
813:4. resolve one tick of continuous action
858:event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/MissionTiming.t.sol | sed -n '1,220p'" in /home/claude/code/clan-world
 succeeded in 0ms:
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ClanWorldConstants,
    ActionType,
    ClansmanState,
    StatusCode,
    ClanFullView,
    Clansman,
    ClanOrder,
    OrderResult,
    Mission
} from "../src/IClanWorld.sol";

contract MissionTimingTest is Test {
    ClanWorld world;
    address elder = address(0xA1);

    function setUp() public {
        world = new ClanWorld();
    }

    function _advanceTick() internal {
        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
        world.heartbeat();
    }

    function _advanceUntilCurrentTick(uint64 targetTick) internal {
        while (world.getWorldState().currentTick < targetTick) {
            _advanceTick();
        }
    }

    function _mintClan() internal returns (uint32 clanId) {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _firstCs(uint32 clanId) internal view returns (uint32) {
        ClanFullView memory view_ = world.getClanFullView(clanId);
        return view_.clansmen[0].clansman.clansman.clansmanId;
    }

    function _submitOrder(uint32 clanId, uint32 csId, uint8 gotoRegion, ActionType action)
        internal
        returns (OrderResult[] memory)
    {
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: gotoRegion,
            action: action,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0
        });
        vm.prank(elder);
        return world.submitClanOrders(clanId, orders);
    }

    function test_submitStoresSubmittedExecutesAndSettlesTicks() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        uint64 submittedAt = world.getWorldState().currentTick;
        uint64 travel = world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_MOUNTAINS);
        uint64 duration = world.getActionDuration(ActionType.MineIron);

        OrderResult[] memory results =
            _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);

        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");

        Mission memory mission = world.getActiveMission(csId);
        assertEq(mission.submittedAtTick, submittedAt, "submitted tick");
        assertEq(mission.executesAtTick, submittedAt + travel, "executes tick");
        assertEq(mission.settlesAtTick, submittedAt + travel + duration, "settles tick");

        assertEq(mission.startTick, mission.submittedAtTick, "legacy start tick mirrors submitted");
        assertEq(mission.actionStartTick, mission.executesAtTick, "legacy action start mirrors executes");

        (uint64 submitted, uint64 executes, uint64 settles) = world.getMissionTiming(clanId, csId);
        assertEq(submitted, mission.submittedAtTick, "getter submitted");
        assertEq(executes, mission.executesAtTick, "getter executes");
        assertEq(settles, mission.settlesAtTick, "getter settles");
    }

    function test_settlementWaitsUntilSettlesAtTickForGatherMission() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);

        OrderResult[] memory results =
            _submitOrder(clanId, csId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron);
        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order should be accepted");

        Mission memory mission = world.getActiveMission(csId);
        assertEq(mission.settlesAtTick, mission.executesAtTick + 4, "four tick action duration");

        _advanceUntilCurrentTick(mission.executesAtTick + 1);
        world.settleClan(clanId);

        Clansman memory arrived = world.getClansman(csId);
        assertEq(uint8(arrived.state), uint8(ClansmanState.ACTING), "arrived and action started");
        assertEq(arrived.currentRegion, ClanWorldConstants.REGION_MOUNTAINS, "arrived at target");
        assertEq(arrived.carryIron, 0, "no iron before settlesAtTick");
        assertTrue(world.getActiveMission(csId).active, "mission remains active before settlesAtTick");

        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
        world.settleClan(clanId);

        Clansman memory settled = world.getClansman(csId);
        assertGt(settled.carryIron, 0, "iron granted at settlesAtTick");
        assertEq(uint8(settled.state), uint8(ClansmanState.WAITING), "mission completed");
        assertFalse(world.getActiveMission(csId).active, "mission inactive after settlement");
        assertGt(settled.cooldownEndsAtTs, block.timestamp, "cooldown starts on settlement");
    }

    function test_getActionDuration_eachActionType() public view {
        assertEq(world.getActionDuration(ActionType.None), 0, "none");
        assertEq(world.getActionDuration(ActionType.ChopWood), 4, "chop wood");
        assertEq(world.getActionDuration(ActionType.MineIron), 4, "mine iron");
        assertEq(world.getActionDuration(ActionType.FishDocks), 4, "fish docks");
        assertEq(world.getActionDuration(ActionType.FishDeepSea), 4, "fish deep sea");
        assertEq(world.getActionDuration(ActionType.HarvestWheat), 4, "harvest wheat");
        assertEq(world.getActionDuration(ActionType.DepositResources), 1, "deposit");
        assertEq(world.getActionDuration(ActionType.BuildWall), 1, "build wall");
        assertEq(world.getActionDuration(ActionType.UpgradeBase), 1, "upgrade base");
        assertEq(world.getActionDuration(ActionType.UpgradeMonument), 1, "upgrade monument");
        assertEq(world.getActionDuration(ActionType.DefendBase), 0, "defend base");
        assertEq(world.getActionDuration(ActionType.MarketBuy), 1, "market buy");
        assertEq(world.getActionDuration(ActionType.MarketSell), 1, "market sell");
        assertEq(world.getActionDuration(ActionType.Wait), 0, "wait");
    }

    function test_getTravelTicks_adjacentAndDistantMatchTable() public view {
        assertEq(
            world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_MOUNTAINS), 1, "adjacent"
        );
        assertEq(
            world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_EAST_DOCKS), 4, "distant"
        );
        assertEq(
            world.getTravelTicks(ClanWorldConstants.REGION_FOREST, ClanWorldConstants.REGION_FOREST), 0, "same region"
        );
    }

    function test_missionOverwriteResetsSubmittedAtTick() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);

        OrderResult[] memory first = _submitOrder(clanId, csId, ClanWorldConstants.REGION_NOOP, ActionType.Wait);
        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first wait");
        uint64 firstSubmitted = world.getActiveMission(csId).submittedAtTick;

        _advanceTick();

        OrderResult[] memory second = _submitOrder(clanId, csId, ClanWorldConstants.REGION_NOOP, ActionType.Wait);
        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "second wait");
        uint64 secondSubmitted = world.getActiveMission(csId).submittedAtTick;

        assertEq(firstSubmitted, 0, "first submit starts at tick 0");
        assertEq(secondSubmitted, world.getWorldState().currentTick, "second submit uses current tick");
        assertGt(secondSubmitted, firstSubmitted, "countdown restarts");
    }

    function test_getMissionTimingReturnsZerosForNonExistentMission() public view {
        (uint64 submitted, uint64 executes, uint64 settles) = world.getMissionTiming(123, 456);

        assertEq(submitted, 0, "submitted");
        assertEq(executes, 0, "executes");
        assertEq(settles, 0, "settles");
    }
}

exec
/bin/bash -lc "git grep -n \"IClanWorld.json\\|abi/IClanWorld\\|wagmi\\|viem\\|ResourcesGathered\\|ResourcesDeposited\" 9ccf94a -- 'apps/*' 'packages/*'" in /home/claude/code/clan-world
 succeeded in 0ms:
9ccf94a:packages/agents/src/seams/IHeartbeatCaller.ts:4: * S2 bootstrap: runner-side `cast send` (or viem writeContract) using a dedicated runner wallet.
9ccf94a:packages/contracts/AGENTS.md:24:- **No TypeScript in this package.** Type generation for consumers happens via `wagmi typegen` or hand-written ABI types in `@clan-world/shared`.
9ccf94a:packages/contracts/AGENTS.md:32:This package IS one side of `IChainClient` — `RealChainClient` (in `@clan-world/shared/adapters`) calls into the deployed contract via viem/ethers using ABIs derived from this package.
9ccf94a:packages/contracts/abi/IClanWorld.json:3272:      "name": "ResourcesDeposited",
9ccf94a:packages/contracts/abi/IClanWorld.json:3321:      "name": "ResourcesGathered",
9ccf94a:packages/contracts/src/ClanWorld.sol:506:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.ChopWood, yield, 0, 0, 0, 0, tick);
9ccf94a:packages/contracts/src/ClanWorld.sol:535:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.MineIron, 0, yield, 0, 0, goldBonus, tick);
9ccf94a:packages/contracts/src/ClanWorld.sol:577:            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDocks, 0, 0, 0, yield, 0, tick);
9ccf94a:packages/contracts/src/ClanWorld.sol:608:            emit ResourcesGathered(clanId, cs.clansmanId, ActionType.FishDeepSea, 0, 0, 0, yield, 0, tick);
9ccf94a:packages/contracts/src/ClanWorld.sol:657:        emit ResourcesGathered(clanId, cs.clansmanId, ActionType.HarvestWheat, 0, 0, yield, 0, 0, tick);
9ccf94a:packages/contracts/src/ClanWorld.sol:700:        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
9ccf94a:packages/contracts/src/IClanWorld.sol:524:    event ResourcesGathered(
9ccf94a:packages/contracts/src/IClanWorld.sol:535:    event ResourcesDeposited(
9ccf94a:packages/contracts/test/ClanWorld.t.sol:511:        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
9ccf94a:packages/contracts/test/ClanWorld.t.sol:514:            assertTrue(logs[i].topics[0] != depositedTopic, "empty deposit emits no ResourcesDeposited event");
9ccf94a:packages/contracts/test/ClanWorld.t.sol:570:        emit IClanWorldEvents.ResourcesDeposited(clanId, csId, 5e18, 2e18, 1e18, 3e18, 1);
9ccf94a:packages/runner/.env.example:17:# the viem Base Sepolia default if unset.
9ccf94a:packages/runner/README.md:29:| `IHeartbeatCaller`    | `RunnerCastHeartbeat`      | viem `writeContract`, dedicated runner wallet     |
9ccf94a:packages/runner/package.json:22:    "viem": "^2.48.4"
9ccf94a:packages/runner/src/runnerCastHeartbeat.ts:9:} from 'viem';
9ccf94a:packages/runner/src/runnerCastHeartbeat.ts:10:import { privateKeyToAccount } from 'viem/accounts';
9ccf94a:packages/runner/src/runnerCastHeartbeat.ts:171:    // viem decodes the named tuple into an object with the same field names.
9ccf94a:packages/shared/package.json:23:    "viem": "^2.48.4"
9ccf94a:packages/shared/src/adapters/IChainClient.ts:2:import { createPublicClient, createWalletClient, http, fallback, defineChain } from 'viem';
9ccf94a:packages/shared/src/adapters/IChainClient.ts:3:import { privateKeyToAccount } from 'viem/accounts';

exec
/bin/bash -lc "git grep -n \"woodDelta\\|ironDelta\\|wheatDelta\\|fishDelta\\|atTick\\|ResourcesDeposited(.*uint64\\|ResourcesDeposited(.*uint32\" 9ccf94a -- ." in /home/claude/code/clan-world
 succeeded in 0ms:
9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md:859:event ResourcesDeposited(uint32 indexed clanId, uint32 indexed clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish);
9ccf94a:packages/contracts/abi/IClanWorld.json:2543:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2568:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2587:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2618:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2686:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2717:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2773:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2848:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2873:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2892:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2923:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:2954:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:3003:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:3231:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:3311:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:3372:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:3563:          "name": "atTick",
9ccf94a:packages/contracts/abi/IClanWorld.json:3594:          "name": "atTick",
9ccf94a:packages/contracts/src/ClanWorld.sol:685:        uint256 woodDelta = cs.carryWood;
9ccf94a:packages/contracts/src/ClanWorld.sol:686:        uint256 ironDelta = cs.carryIron;
9ccf94a:packages/contracts/src/ClanWorld.sol:687:        uint256 wheatDelta = cs.carryWheat;
9ccf94a:packages/contracts/src/ClanWorld.sol:688:        uint256 fishDelta = cs.carryFish;
9ccf94a:packages/contracts/src/ClanWorld.sol:690:        clan.vaultWood += woodDelta;
9ccf94a:packages/contracts/src/ClanWorld.sol:691:        clan.vaultIron += ironDelta;
9ccf94a:packages/contracts/src/ClanWorld.sol:692:        clan.vaultWheat += wheatDelta;
9ccf94a:packages/contracts/src/ClanWorld.sol:693:        clan.vaultFish += fishDelta;
9ccf94a:packages/contracts/src/ClanWorld.sol:700:        emit ResourcesDeposited(clanId, cs.clansmanId, woodDelta, ironDelta, wheatDelta, fishDelta, tick);
9ccf94a:packages/contracts/src/IClanWorld.sol:500:        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
9ccf94a:packages/contracts/src/IClanWorld.sol:504:    event ClanStarvationChanged(uint32 indexed clanId, bool isStarving, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:533:        uint64 atTick
9ccf94a:packages/contracts/src/IClanWorld.sol:538:        uint256 woodDelta,
9ccf94a:packages/contracts/src/IClanWorld.sol:539:        uint256 ironDelta,
9ccf94a:packages/contracts/src/IClanWorld.sol:540:        uint256 wheatDelta,
9ccf94a:packages/contracts/src/IClanWorld.sol:541:        uint256 fishDelta,
9ccf94a:packages/contracts/src/IClanWorld.sol:546:    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:547:    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:548:    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:558:        uint64 atTick
9ccf94a:packages/contracts/src/IClanWorld.sol:585:        uint32 indexed banditId, BanditState oldState, BanditState newState, uint8 region, uint64 atTick
9ccf94a:packages/contracts/src/IClanWorld.sol:587:    event BanditMoved(uint32 indexed banditId, uint8 fromRegion, uint8 toRegion, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:599:        uint64 atTick
9ccf94a:packages/contracts/src/IClanWorld.sol:601:    event BanditDefeated(uint32 indexed banditId, uint32 indexed targetClanId, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:602:    event BanditEscaped(uint32 indexed banditId, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:615:    event ColdDamageApplied(uint32 indexed clanId, uint16 oldDamage, uint16 newDamage, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:616:    event ClansmanDiedFromCold(uint32 indexed clanId, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:619:    event GoldTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
9ccf94a:packages/contracts/src/IClanWorld.sol:621:        uint32 indexed fromClanId, uint32 indexed toClanId, ResourceType resource, uint256 amount, uint64 atTick
9ccf94a:packages/contracts/src/IClanWorld.sol:623:    event BlueprintTransferred(uint32 indexed fromClanId, uint32 indexed toClanId, uint256 amount, uint64 atTick);
9ccf94a:packages/contracts/test/ClanWorld.t.sol:478:        uint256 woodDelta = 5e18;
9ccf94a:packages/contracts/test/ClanWorld.t.sol:480:        harness.setCarry(csId, woodDelta, 0, 0, 0);
9ccf94a:packages/contracts/test/ClanWorld.t.sol:496:        assertEq(harness.getClan(clanId).vaultWood, vaultBefore + woodDelta, "vault wood receives carried wood");
9ccf94a:packages/contracts/test/ClanWorld.t.sol:511:        bytes32 depositedTopic = keccak256("ResourcesDeposited(uint32,uint32,uint256,uint256,uint256,uint256,uint32)");
9ccf94a:packages/contracts/test/ClanWorld.t.sol:523:        uint256 woodDelta = 4e18;
9ccf94a:packages/contracts/test/ClanWorld.t.sol:524:        uint256 ironDelta = 2e18;
9ccf94a:packages/contracts/test/ClanWorld.t.sol:525:        uint256 fishDelta = 3e18;
9ccf94a:packages/contracts/test/ClanWorld.t.sol:527:        harness.setCarry(csId, woodDelta, ironDelta, 0, fishDelta);
9ccf94a:packages/contracts/test/ClanWorld.t.sol:539:        assertEq(afterClan.vaultWood, beforeClan.vaultWood + woodDelta, "wood transferred");
9ccf94a:packages/contracts/test/ClanWorld.t.sol:540:        assertEq(afterClan.vaultIron, beforeClan.vaultIron + ironDelta, "iron transferred");
9ccf94a:packages/contracts/test/ClanWorld.t.sol:541:        assertEq(afterClan.vaultFish, beforeClan.vaultFish + fishDelta, "fish transferred");

exec
/bin/bash -lc "git grep -n \"ERR_EMPTY_CARGO\\|ERR_CARRY_FULL\" 9ccf94a -- packages/contracts/src packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
9ccf94a:packages/contracts/src/IClanWorld.sol:169:    ERR_EMPTY_CARGO,
9ccf94a:packages/contracts/src/IClanWorld.sol:181:    ERR_CARRY_FULL

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '1295,1415p'" in /home/claude/code/clan-world
 succeeded in 0ms:

    function _enqueueScheduledMarketAction(
        uint32 clanId,
        ClanOrder calldata order,
        uint32 clansmanId,
        uint64 executeAtTick,
        uint64 missionNonce
    ) internal {
        ScheduledMarketAction memory sma = ScheduledMarketAction({
            executeAtTick: executeAtTick,
            commitSequence: _world.nextCommitSequence++,
            missionNonce: missionNonce,
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

    function _registerDefender(uint8 region, uint32 clanId, uint32 clansmanId) internal {
        if (_clansmanDefendingRegion[clansmanId] == region) return;
        _clearDefender(clansmanId);

        if (_defenderCountByRegionClan[region][clanId] == 0) {
            _defendingClansByRegion[region].push(clanId);
        }
        _defenderCountByRegionClan[region][clanId]++;
        _clansmanDefendingRegion[clansmanId] = region;
    }

    function _clearDefender(uint32 clansmanId) internal {
        uint8 region = _clansmanDefendingRegion[clansmanId];
        if (region == 0) return;

        uint32 clanId = _clansmen[clansmanId].clanId;
        uint256 count = _defenderCountByRegionClan[region][clanId];
        if (count > 1) {
            _defenderCountByRegionClan[region][clanId] = count - 1;
        } else {
            delete _defenderCountByRegionClan[region][clanId];
            uint32[] storage clans = _defendingClansByRegion[region];
            for (uint256 i = 0; i < clans.length; i++) {
                if (clans[i] == clanId) {
                    clans[i] = clans[clans.length - 1];
                    clans.pop();
                    break;
                }
            }
        }

        delete _clansmanDefendingRegion[clansmanId];
    }

    // =========================================================================
    // MARKET EXECUTION (Phase 2)
    // =========================================================================

    /// @dev Execute up to MAX_MARKET_ACTIONS_PER_TICK scheduled market actions for the given tick.
    ///      Overflow is appended to the next tick to keep heartbeat gas bounded.
    function _executeScheduledMarketActions(uint64 tick) internal {
        ScheduledMarketAction[] storage actions = _scheduledMarketActions[tick];
        uint256 len = actions.length;
        if (len == 0) return;

        uint256 processCount = len > MAX_MARKET_ACTIONS_PER_TICK ? MAX_MARKET_ACTIONS_PER_TICK : len;

        for (uint256 i = 0; i < processCount; i++) {
            ScheduledMarketAction storage sma = actions[i];

            // Validate clansman still belongs to the clan
            Clansman storage cs = _clansmen[sma.clansmanId];
            if (cs.clanId != sma.clanId || cs.state == ClansmanState.DEAD) {
                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_CLANSMAN);
                continue;
            }

            // Guard: clansman was re-tasked if mission action no longer matches the queued type.
            // Note: _completeMission sets m.active=false during settlement (by design), so we
            // cannot use m.active as a validity signal here — check action type and nonce.
            Mission storage m = _missions[sma.clansmanId];
            if (m.action != sma.action || m.nonce != sma.missionNonce) {
                emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
                continue;
            }

            if (sma.action == ActionType.MarketSell) {
                try this._executeMarketSellExternal(
                    tick, sma.clanId, sma.clansmanId, sma.marketToken, sma.marketAmount, sma.commitSequence
                ) {
                // success
                }
                catch {
                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);
                }
            } else if (sma.action == ActionType.MarketBuy) {
                try this._executeMarketBuyExternal(
                    tick,
                    sma.clanId,
                    sma.clansmanId,
                    sma.marketToken,
                    sma.marketAmount,
                    sma.maxGoldIn,
                    sma.commitSequence
                ) {
                // success
                }
                catch {
                    emit MarketActionFailed(sma.clanId, sma.clansmanId, sma.action, StatusCode.ERR_INVALID_ACTION);

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | sed -n '1860,2025p'" in /home/claude/code/clan-world
 succeeded in 0ms:

    function getActiveDefenders(uint32 targetClanId) external view override returns (uint32[] memory clansmanIds) {
        uint32[] storage defendingClans = _defendingClansByRegion[_clans[targetClanId].baseRegion];
        uint256 count = 0;

        for (uint256 i = 0; i < defendingClans.length; i++) {
            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
            for (uint256 j = 0; j < clanClansmen.length; j++) {
                Mission storage mission = _missions[clanClansmen[j]];
                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
                    count++;
                }
            }
        }

        clansmanIds = new uint32[](count);
        uint256 out = 0;
        for (uint256 i = 0; i < defendingClans.length; i++) {
            uint32[] storage clanClansmen = _clanClansmanIds[defendingClans[i]];
            for (uint256 j = 0; j < clanClansmen.length; j++) {
                uint32 clansmanId = clanClansmen[j];
                Mission storage mission = _missions[clansmanId];
                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
                    clansmanIds[out++] = clansmanId;
                }
            }
        }
    }

    function getDefendingClans(uint8 region) external view override returns (uint32[] memory) {
        return _defendingClansByRegion[region];
    }

    // =========================================================================
    // DERIVED READ GETTERS (read-only, no storage mutation)
    // =========================================================================

    /// @dev Returns last-settled state; starvation check uses currentTick (live).
    ///      Carry amounts lag until next settleClan().
    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
        Clan memory clan = _clans[clanId];
        bool starving = clan.starvationStartsAtTick != 0 && clan.starvationStartsAtTick <= _world.currentTick;
        uint256 lootVal = _lootValueRaw(clan);
        return
            DerivedClanState({clan: clan, isStarving: starving, lootValue: lootVal, derivedAtTick: _world.currentTick});
    }

    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
        Clansman memory cs = _clansmen[clansmanId];
        Mission memory m = _missions[clansmanId];
        uint8 effectiveRegion = cs.currentRegion;
        if (cs.state == ClansmanState.TRAVELING && m.active) {
            // Simplified: if past arrivalTick, they're at target; else at start
            if (_world.currentTick >= m.arrivalTick) {
                effectiveRegion = m.targetRegion;
            } else {
                effectiveRegion = m.startRegion;
            }
        }
        return DerivedClansmanState({
            clansman: cs, activeMission: m, effectiveRegion: effectiveRegion, derivedAtTick: _world.currentTick
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
            currentSeasonNumber: _world.currentSeasonNumber,
            nextHeartbeatAtTick: _world.nextHeartbeatAtTick,
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

exec
/bin/bash -lc "git grep -n \"WOOD_CAP\\|IRON_CAP\\|WHEAT_CAP\\|FISH_CAP\\|CLANSMAN_CARRY_CAP\" 9ccf94a -- packages/contracts/src/ClanWorld.sol packages/contracts/src/IClanWorld.sol packages/contracts/test" in /home/claude/code/clan-world
 succeeded in 0ms:
9ccf94a:packages/contracts/src/ClanWorld.sol:490:        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
9ccf94a:packages/contracts/src/ClanWorld.sol:495:        uint256 remaining = ClanWorldConstants.CLANSMAN_CARRY_CAP - cs.carryWood;
9ccf94a:packages/contracts/src/ClanWorld.sol:508:        if (cs.carryWood >= ClanWorldConstants.CLANSMAN_CARRY_CAP) {
9ccf94a:packages/contracts/src/ClanWorld.sol:522:        uint256 remaining = ClanWorldConstants.IRON_CAP - cs.carryIron;
9ccf94a:packages/contracts/src/ClanWorld.sol:537:        if (cs.carryIron >= ClanWorldConstants.IRON_CAP) {
9ccf94a:packages/contracts/src/ClanWorld.sol:562:        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
9ccf94a:packages/contracts/src/ClanWorld.sol:579:        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
9ccf94a:packages/contracts/src/ClanWorld.sol:593:        uint256 remaining = ClanWorldConstants.FISH_CAP - cs.carryFish;
9ccf94a:packages/contracts/src/ClanWorld.sol:610:        if (cs.carryFish >= ClanWorldConstants.FISH_CAP) {
9ccf94a:packages/contracts/src/ClanWorld.sol:624:        uint256 remaining = ClanWorldConstants.WHEAT_CAP - cs.carryWheat;
9ccf94a:packages/contracts/src/ClanWorld.sol:664:        if (cs.carryWheat >= ClanWorldConstants.WHEAT_CAP || plot.remainingWheat == 0) {
9ccf94a:packages/contracts/src/IClanWorld.sol:43:    uint256 internal constant CLANSMAN_CARRY_CAP = 10e18;
9ccf94a:packages/contracts/src/IClanWorld.sol:44:    uint256 internal constant WOOD_CAP = CLANSMAN_CARRY_CAP;
9ccf94a:packages/contracts/src/IClanWorld.sol:45:    uint256 internal constant IRON_CAP = 5e18;
9ccf94a:packages/contracts/src/IClanWorld.sol:46:    uint256 internal constant WHEAT_CAP = 40e18;
9ccf94a:packages/contracts/src/IClanWorld.sol:47:    uint256 internal constant FISH_CAP = 8e18;
9ccf94a:packages/contracts/test/Gathering.t.sol:117:        world.setCarryWood(csId, ClanWorldConstants.CLANSMAN_CARRY_CAP - 1e18);
9ccf94a:packages/contracts/test/Gathering.t.sol:121:        assertEq(cs.carryWood, ClanWorldConstants.CLANSMAN_CARRY_CAP, "wood carry cap");

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/IClanWorld.sol | sed -n '500,550p'" in /home/claude/code/clan-world
 succeeded in 0ms:
        uint32 indexed clanId, address indexed owner, uint256 iftTokenId, uint8 baseRegion, uint64 atTick
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
        uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce
    );
    event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
    event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);

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
        uint256 woodDelta,
        uint256 ironDelta,
        uint256 wheatDelta,
        uint256 fishDelta,
        uint32 tick
    );

    // ----- building -----
    event WallLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
    event BaseLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);
    event MonumentLevelChanged(uint32 indexed clanId, uint8 oldLevel, uint8 newLevel, uint64 atTick);

    // ----- market -----

collab: Wait
codex
One delegated pass found a potentially stale checked-in ABI, which is exactly the kind of cross-stream seam risk we need to be sure about. I’m verifying that directly against the artifact before I lock the final severity.
exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/abi/IClanWorld.json | sed -n '3268,3338p'" in /home/claude/code/clan-world
 succeeded in 0ms:
      "anonymous": false
    },
    {
      "type": "event",
      "name": "ResourcesDeposited",
      "inputs": [
        {
          "name": "clanId",
          "type": "uint32",
          "indexed": true,
          "internalType": "uint32"
        },
        {
          "name": "clansmanId",
          "type": "uint32",
          "indexed": true,
          "internalType": "uint32"
        },
        {
          "name": "wood",
          "type": "uint256",
          "indexed": false,
          "internalType": "uint256"
        },
        {
          "name": "iron",
          "type": "uint256",
          "indexed": false,
          "internalType": "uint256"
        },
        {
          "name": "wheat",
          "type": "uint256",
          "indexed": false,
          "internalType": "uint256"
        },
        {
          "name": "fish",
          "type": "uint256",
          "indexed": false,
          "internalType": "uint256"
        },
        {
          "name": "atTick",
          "type": "uint64",
          "indexed": false,
          "internalType": "uint64"
        }
      ],
      "anonymous": false
    },
    {
      "type": "event",
      "name": "ResourcesGathered",
      "inputs": [
        {
          "name": "clanId",
          "type": "uint32",
          "indexed": true,
          "internalType": "uint32"
        },
        {
          "name": "clansmanId",
          "type": "uint32",
          "indexed": true,
          "internalType": "uint32"
        },
        {
          "name": "action",
          "type": "uint8",
          "indexed": false,

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '530,548p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '448,706p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

collab: Wait
collab: Wait
codex
I’ve got all three sidecar passes back. I’m cross-checking one last thing locally: whether the “wood only” duration fix leaves the other 4-tick gather actions economically inconsistent, because that’s the main difference between a clean phase and a partial one.
exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1796,1812p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '334,346p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/IClanWorld.sol | nl -ba | sed -n '40,55p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | nl -ba | sed -n '809,831p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/abi/IClanWorld.json | nl -ba | sed -n '3270,3316p'" in /home/claude/code/clan-world
 succeeded in 0ms:
  3270	    {
  3271	      "type": "event",
  3272	      "name": "ResourcesDeposited",
  3273	      "inputs": [
  3274	        {
  3275	          "name": "clanId",
  3276	          "type": "uint32",
  3277	          "indexed": true,
  3278	          "internalType": "uint32"
  3279	        },
  3280	        {
  3281	          "name": "clansmanId",
  3282	          "type": "uint32",
  3283	          "indexed": true,
  3284	          "internalType": "uint32"
  3285	        },
  3286	        {
  3287	          "name": "wood",
  3288	          "type": "uint256",
  3289	          "indexed": false,
  3290	          "internalType": "uint256"
  3291	        },
  3292	        {
  3293	          "name": "iron",
  3294	          "type": "uint256",
  3295	          "indexed": false,
  3296	          "internalType": "uint256"
  3297	        },
  3298	        {
  3299	          "name": "wheat",
  3300	          "type": "uint256",
  3301	          "indexed": false,
  3302	          "internalType": "uint256"
  3303	        },
  3304	        {
  3305	          "name": "fish",
  3306	          "type": "uint256",
  3307	          "indexed": false,
  3308	          "internalType": "uint256"
  3309	        },
  3310	        {
  3311	          "name": "atTick",
  3312	          "type": "uint64",
  3313	          "indexed": false,
  3314	          "internalType": "uint64"
  3315	        }
  3316	      ],

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/src/ClanWorld.sol | nl -ba | sed -n '1608,1622p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/Gathering.t.sol | nl -ba | sed -n '72,132p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:packages/contracts/test/ClanWorld.t.sol | nl -ba | sed -n '475,572p'" in /home/claude/code/clan-world
 succeeded in 0ms:
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

exec
/bin/bash -lc "git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | nl -ba | sed -n '252,263p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   252	
   253	uint64 CLANSMAN_COOLDOWN_SECONDS = 60;
   254	
   255	uint256 WOOD_CAP  = 15e18;
   256	uint256 IRON_CAP  = 5e18;
   257	uint256 WHEAT_CAP = 40e18;
   258	uint256 FISH_CAP  = 8e18;
   259	
   260	uint256 WOOD_BASE_YIELD = 2e18;
   261	uint256 WOOD_CRIT_BONUS = 1e18;
   262	uint16  WOOD_CRIT_BPS = 2000;      // 20%
   263	

exec
/bin/bash -lc "git show 9ccf94a:docs/planning/clanworld_v4_2_state_schema_interface_spec.md | nl -ba | sed -n '854,861p'" in /home/claude/code/clan-world
 succeeded in 0ms:
   854	event ClanSettled(uint32 indexed clanId, uint64 settledToTick);
   855	event MissionAssigned(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action, uint8 startRegion, uint8 targetRegion);
   856	event MissionInterrupted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 oldMissionNonce, uint64 newMissionNonce);
   857	event MissionCompleted(uint32 indexed clanId, uint32 indexed clansmanId, uint64 missionNonce, ActionType action);
   858	event WorkerArrived(uint32 indexed clanId, uint32 indexed clansmanId, uint8 region, uint64 tick);
   859	event ResourcesDeposited(uint32 indexed clanId, uint32 indexed clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish);
   860	
   861	event ImmediateMarketActionExecuted(

codex
# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY
NEEDS_FIXES. The deposit path is closer, but this phase still has two merge-blocking seams: gathering economics are only corrected for wood while the other 4-tick gather actions still settle as single-tick payouts, and the canonical `ResourcesDeposited` seam was changed without a matching packaged ABI update. I would not merge until the economy behavior is made internally consistent and the contract artifact/interface drift is resolved.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:339-343`, `513-667`, `1799-1805`, `docs/planning/clanworld_v4_2_state_schema_interface_spec.md:810-814`  
  Gathering is still resolved exactly once at `settlesAtTick`, then the mission is completed immediately. This PR compensates only `ChopWood` by multiplying its payout by action duration, but `MineIron`, `HarvestWheat`, `FishDocks`, and `FishDeepSea` still pay only a single settle-time yield / single RNG roll despite being 4-tick actions. That leaves Phase 5 resource accounting inconsistent across resource types and materially under-credits non-wood gathering.

- `packages/contracts/src/IClanWorld.sol:535-542`, `packages/contracts/src/ClanWorld.sol:455-458`, `670-700`, `packages/contracts/abi/IClanWorld.json:3272-3315`, `docs/planning/clanworld_v4_2_state_schema_interface_spec.md:859`  
  `ResourcesDeposited` is now a different event shape on the canonical seam, and the checked-in ABI artifact was not updated to match. The Solidity source emits `ResourcesDeposited(..., woodDelta, ironDelta, wheatDelta, fishDelta, uint32 tick)`, while the packaged ABI still advertises `(..., wood, iron, wheat, fish, uint64 atTick)`. Any downstream decoder or watcher using `packages/contracts/abi/IClanWorld.json` will miss or misdecode deposit events, which is a direct indexer/UI/agent integration break.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:495-499`, `packages/contracts/src/IClanWorld.sol:50-53`, `docs/planning/clanworld_v4_2_state_schema_interface_spec.md:260-262`  
  The new wood crit implementation no longer matches the declared crit model. The code ignores `WOOD_CRIT_BONUS` and doubles the full 4-tick batch instead, so the published constants/documentation and the actual payout formula have drifted apart. Even if the rebalance is intentional, the contract surface and authored economy rules are no longer describing the same system.

- `packages/contracts/src/ClanWorld.sol:1614-1617`, `packages/contracts/src/IClanWorld.sol:153-170`, `packages/contracts/test/ClanWorld.t.sol:547-556`  
  Deposit validation now returns `ERR_INVALID_REGION` instead of `ERR_NOT_AT_HOMEBASE`. That is externally visible behavior drift on a canonical enum surface, and any UI/adapter logic that previously mapped “wrong deposit destination” to the more specific homebase error will silently change behavior.

## LOW severity findings
- `packages/contracts/test/ClanWorld.t.sol:475-572`  
  The new deposit tests do not pin the idempotency seam after heartbeat settlement. I’d want one regression that lets heartbeat resolve the deposit, then calls `settleClan` or `settleClansman` afterward and proves there is no second vault credit and no duplicate `ResourcesDeposited`.

- `packages/contracts/test/Gathering.t.sol:79-132`, `packages/contracts/src/ClanWorld.sol:495-503`  
  The new wood tests cover base yield, rough crit distribution, cap clamp, and cooldown, but they do not cover starvation or the starvation+crit+clamp interaction. For an economy phase, that leaves one of the riskiest accounting branches unpinned.

## Cross-cutting observations
- Phase 5 is not cohesive yet: deposits got a semantics pass, but gathering is still economically inconsistent across resource types.
- The anti-cheat surface on deposit amounts looks okay in this diff: credited vault amounts still come only from internal carry state, not user-supplied deltas.
- The `IClanWorld` seam drift is broader than code alone: the spec/docs still describe the old deposit event and old wood constants.
- I did not run `forge test`; this review is from code/diff inspection only because the session is read-only.
2026-04-30T02:44:18.026995Z ERROR codex_core::session: failed to record rollout items: thread 019ddc41-1a89-7c00-95e0-33b6dcd92bbf not found
2026-04-30T02:44:18.027004Z ERROR codex_core::session: failed to record rollout items: thread 019ddc41-1aeb-7e31-8ae3-726eb3727e33 not found
2026-04-30T02:44:18.027095Z ERROR codex_core::session: failed to record rollout items: thread 019ddc40-02b9-7fc1-b860-2e0c7d98b3bd not found
2026-04-30T02:44:18.027207Z ERROR codex_core::session: failed to record rollout items: thread 019ddc41-1a17-77b2-93ea-d87c90bc7aa7 not found
tokens used
291,126
# Phase Super-Swarm Review — PR #193 (head 9ccf94a)

## SUMMARY
NEEDS_FIXES. The deposit path is closer, but this phase still has two merge-blocking seams: gathering economics are only corrected for wood while the other 4-tick gather actions still settle as single-tick payouts, and the canonical `ResourcesDeposited` seam was changed without a matching packaged ABI update. I would not merge until the economy behavior is made internally consistent and the contract artifact/interface drift is resolved.

## HIGH severity findings
- `packages/contracts/src/ClanWorld.sol:339-343`, `513-667`, `1799-1805`, `docs/planning/clanworld_v4_2_state_schema_interface_spec.md:810-814`  
  Gathering is still resolved exactly once at `settlesAtTick`, then the mission is completed immediately. This PR compensates only `ChopWood` by multiplying its payout by action duration, but `MineIron`, `HarvestWheat`, `FishDocks`, and `FishDeepSea` still pay only a single settle-time yield / single RNG roll despite being 4-tick actions. That leaves Phase 5 resource accounting inconsistent across resource types and materially under-credits non-wood gathering.

- `packages/contracts/src/IClanWorld.sol:535-542`, `packages/contracts/src/ClanWorld.sol:455-458`, `670-700`, `packages/contracts/abi/IClanWorld.json:3272-3315`, `docs/planning/clanworld_v4_2_state_schema_interface_spec.md:859`  
  `ResourcesDeposited` is now a different event shape on the canonical seam, and the checked-in ABI artifact was not updated to match. The Solidity source emits `ResourcesDeposited(..., woodDelta, ironDelta, wheatDelta, fishDelta, uint32 tick)`, while the packaged ABI still advertises `(..., wood, iron, wheat, fish, uint64 atTick)`. Any downstream decoder or watcher using `packages/contracts/abi/IClanWorld.json` will miss or misdecode deposit events, which is a direct indexer/UI/agent integration break.

## MEDIUM severity findings
- `packages/contracts/src/ClanWorld.sol:495-499`, `packages/contracts/src/IClanWorld.sol:50-53`, `docs/planning/clanworld_v4_2_state_schema_interface_spec.md:260-262`  
  The new wood crit implementation no longer matches the declared crit model. The code ignores `WOOD_CRIT_BONUS` and doubles the full 4-tick batch instead, so the published constants/documentation and the actual payout formula have drifted apart. Even if the rebalance is intentional, the contract surface and authored economy rules are no longer describing the same system.

- `packages/contracts/src/ClanWorld.sol:1614-1617`, `packages/contracts/src/IClanWorld.sol:153-170`, `packages/contracts/test/ClanWorld.t.sol:547-556`  
  Deposit validation now returns `ERR_INVALID_REGION` instead of `ERR_NOT_AT_HOMEBASE`. That is externally visible behavior drift on a canonical enum surface, and any UI/adapter logic that previously mapped “wrong deposit destination” to the more specific homebase error will silently change behavior.

## LOW severity findings
- `packages/contracts/test/ClanWorld.t.sol:475-572`  
  The new deposit tests do not pin the idempotency seam after heartbeat settlement. I’d want one regression that lets heartbeat resolve the deposit, then calls `settleClan` or `settleClansman` afterward and proves there is no second vault credit and no duplicate `ResourcesDeposited`.

- `packages/contracts/test/Gathering.t.sol:79-132`, `packages/contracts/src/ClanWorld.sol:495-503`  
  The new wood tests cover base yield, rough crit distribution, cap clamp, and cooldown, but they do not cover starvation or the starvation+crit+clamp interaction. For an economy phase, that leaves one of the riskiest accounting branches unpinned.

## Cross-cutting observations
- Phase 5 is not cohesive yet: deposits got a semantics pass, but gathering is still economically inconsistent across resource types.
- The anti-cheat surface on deposit amounts looks okay in this diff: credited vault amounts still come only from internal carry state, not user-supplied deltas.
- The `IClanWorld` seam drift is broader than code alone: the spec/docs still describe the old deposit event and old wood constants.
- I did not run `forge test`; this review is from code/diff inspection only because the session is read-only.
EXIT=0
