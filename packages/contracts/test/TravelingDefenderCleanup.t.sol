// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ClansmanState,
    ActionType,
    StatusCode,
    Clan,
    Clansman,
    Mission,
    ClanOrder,
    WithdrawResourcesData,
    OrderResult
} from "../src/IClanWorld.sol";

contract TravelingDefenderCleanupHarness is ClanWorld {
    function markClanDeadForTest(uint32 clanId) external {
        _markClanDead(clanId, "test", _world.currentTick);
    }
}

contract TravelingDefenderCleanupTest is Test {
    TravelingDefenderCleanupHarness world;

    function setUp() public {
        world = new TravelingDefenderCleanupHarness();
    }

    function _csId(uint32 clanId) internal view returns (uint32) {
        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
    }

    function _defendOrder(uint32 csId, uint8 region, uint32 targetClanId) internal pure returns (ClanOrder[] memory) {
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: region,
            action: ActionType.DefendBase,
            targetClanId: targetClanId,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
        return orders;
    }

    function test_travelingDefenderClearsWhenTargetDies() public {
        (uint32 defenderClanId,) = world.mintClan(address(0xA1));
        (uint32 targetClanId,) = world.mintClan(address(0xA2));
        Clan memory targetClan = world.getClan(targetClanId);
        uint32 defenderId = _csId(defenderClanId);

        vm.prank(address(0xA1));
        OrderResult[] memory results =
            world.submitClanOrders(defenderClanId, _defendOrder(defenderId, targetClan.baseRegion, targetClanId));
        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "defender order");

        Mission memory beforeMission = world.getActiveMission(defenderId);
        assertTrue(beforeMission.active, "mission active before death");
        assertEq(uint8(beforeMission.action), uint8(ActionType.DefendBase), "defend mission");
        assertEq(beforeMission.targetClanId, targetClanId, "target stored");
        assertEq(uint8(world.getClansman(defenderId).state), uint8(ClansmanState.TRAVELING), "defender traveling");
        assertEq(world.getActiveDefenders(targetClanId).length, 0, "not registered before arrival");

        world.markClanDeadForTest(targetClanId);

        Mission memory afterMission = world.getActiveMission(defenderId);
        Clansman memory defender = world.getClansman(defenderId);
        assertFalse(afterMission.active, "traveling mission cleared");
        assertEq(uint8(defender.state), uint8(ClansmanState.WAITING), "defender released");
        assertEq(world.getActiveDefenders(targetClanId).length, 0, "registry remains empty");
    }
}
