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
    WithdrawResourcesData,
    OrderResult,
    MarketExecutionMode
} from "../src/IClanWorld.sol";

contract GatheringHarness is ClanWorld {
    function setCarryWood(uint32 csId, uint256 wood) external {
        _clansmen[csId].carryWood = wood;
    }

    function setVaultFood(uint32 clanId, uint256 wheat, uint256 fish) external {
        _clans[clanId].vaultWheat = wheat;
        _clans[clanId].vaultFish = fish;
    }

    function seedLazyHarvestAtTick(uint32 clanId, uint32 csId, uint64 tick) external {
        _clans[clanId].lastSettledTick = tick;

        Clansman storage cs = _clansmen[csId];
        cs.state = ClansmanState.ACTING;
        cs.currentRegion = ClanWorldConstants.REGION_WEST_FARMS;

        _missions[csId] = Mission({
            active: true,
            nonce: cs.lastMissionNonce + 1,
            submittedAtTick: tick,
            executesAtTick: tick,
            settlesAtTick: tick,
            clansmanId: csId,
            startRegion: ClanWorldConstants.REGION_WEST_FARMS,
            targetRegion: ClanWorldConstants.REGION_WEST_FARMS,
            action: ActionType.HarvestWheat,
            startTick: tick,
            arrivalTick: tick,
            actionStartTick: tick,
            missionSeed: bytes32(0),
            marketMode: MarketExecutionMode.None,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
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
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
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

    function test_chopWoodAtForestYieldsBaseOrCritBonus() public {
        vm.prevrandao(bytes32(uint256(2)));
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);

        Clansman memory cs = _settleChopWood(clanId, csId);

        uint256 baseYield =
            ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(world.getActionDuration(ActionType.ChopWood));
        assertTrue(
            cs.carryWood == baseYield || cs.carryWood == baseYield * 2,
            "wood yield"
        );
    }

    function test_chopWoodCritDistributionAcrossSeeds() public {
        uint256 critCount = 0;
        world = new GatheringHarness();
        uint256 cleanState = vm.snapshotState();

        for (uint256 i = 0; i < 100; i++) {
            assertTrue(vm.revertToState(cleanState), "reset gathering world");
            vm.prevrandao(bytes32(uint256(i + 10_000)));
            uint32 clanId = _mintClan();
            uint32 csId = _firstCs(clanId);

            Clansman memory cs = _settleChopWood(clanId, csId);
            uint256 baseYield =
                ClanWorldConstants.WOOD_YIELD_PER_TICK * uint256(world.getActionDuration(ActionType.ChopWood));
            if (cs.carryWood == baseYield * 2) {
                critCount++;
            } else {
                assertEq(cs.carryWood, baseYield, "non-crit yield");
            }
        }

        assertGe(critCount, 5, "crit count too low");
        assertLe(critCount, 20, "crit count too high");
    }

    function test_chopWoodClampsToCarryCap() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        world.setCarryWood(csId, ClanWorldConstants.WOOD_CAP - 1e18);

        Clansman memory cs = _settleChopWood(clanId, csId);

        assertEq(cs.carryWood, ClanWorldConstants.WOOD_CAP, "wood carry cap");
    }

    function test_starvationBeginsOnNextTickAfterUpkeepFailure() public {
        uint32 clanId = _mintClan();
        uint64 failureTick = world.getWorldState().currentTick;
        world.setVaultFood(clanId, 0, 0);

        _advanceTick();
        world.settleClan(clanId);

        ClanFullView memory failed = world.getClanFullView(clanId);
        assertEq(failed.clan.clan.starvationStartsAtTick, failureTick + 1, "starvation starts next tick");
        assertTrue(failed.clan.isStarving, "starving once next tick is current");

        _advanceTick();

        ClanFullView memory nextTick = world.getClanFullView(clanId);
        assertTrue(nextTick.clan.isStarving, "starvation remains active without food");
    }

    function test_starvationPenaltyUsesLazySettledTick() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        uint256 fullYield =
            ClanWorldConstants.WHEAT_YIELD_PER_TICK * uint256(world.getActionDuration(ActionType.HarvestWheat));

        _advanceUntilCurrentTick(5);
        world.seedLazyHarvestAtTick(clanId, csId, 5);
        world.setVaultFood(clanId, 0, 0);
        _advanceUntilCurrentTick(6);
        world.settleClan(clanId);

        Clansman memory failureTickHarvest = world.getClansman(csId);
        ClanFullView memory failureTickClan = world.getClanFullView(clanId);
        assertEq(failureTickClan.clan.clan.starvationStartsAtTick, 6, "starvation starts next tick");
        assertTrue(failureTickClan.clan.isStarving, "live view is starving at tick 6");
        assertEq(failureTickHarvest.carryWheat, fullYield, "failure tick harvest is not penalized");

        _advanceUntilCurrentTick(7);

        world.seedLazyHarvestAtTick(clanId, csId, 6);
        world.settleClan(clanId);

        Clansman memory nextTickHarvest = world.getClansman(csId);
        assertEq(nextTickHarvest.carryWheat, fullYield + fullYield / 2, "next tick harvest is penalized");
    }

    function test_chopWoodReschedulesWhenCarryCapNotReached() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);

        OrderResult[] memory result = _submitChopWood(clanId, csId);
        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");

        Mission memory mission = world.getActiveMission(csId);
        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
        world.settleClan(clanId);

        Clansman memory cs = world.getClansman(csId);
        assertEq(uint8(cs.state), uint8(ClansmanState.ACTING), "mission continues");
        assertTrue(world.getActiveMission(csId).active, "mission remains active");
        assertEq(
            world.getActiveMission(csId).settlesAtTick,
            mission.settlesAtTick + world.getActionDuration(ActionType.ChopWood),
            "next chop scheduled"
        );
    }

    function test_chopWoodAppliesCooldownWhenCarryCapReached() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        world.setCarryWood(csId, ClanWorldConstants.WOOD_CAP - 1e18);

        OrderResult[] memory result = _submitChopWood(clanId, csId);
        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
        uint64 submitCooldown = result[0].cooldownEndsAtTs;

        Mission memory mission = world.getActiveMission(csId);
        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
        world.settleClan(clanId);

        Clansman memory cs = world.getClansman(csId);

        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
        assertFalse(world.getActiveMission(csId).active, "mission inactive");
        assertEq(cs.cooldownEndsAtTs, submitCooldown, "natural completion keeps submit-time cooldown");
    }
}
