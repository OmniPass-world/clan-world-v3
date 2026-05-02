// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {ClanWorldConstants, BanditState, Clan} from "../src/IClanWorld.sol";

contract BanditDefeatGoldRewardHarness is ClanWorld {
    function forceAttackingBandit(uint8 region, uint32 strength, uint32 targetClanId) external returns (uint32 id) {
        id = _spawnBandit(region, strength);
        _bandits[id].state = BanditState.Attacking;
        _bandits[id].targetClanId = targetClanId;
        _bandits[id].tickEnteredState = _world.currentTick;
    }

    function setBanditStrength(uint32 banditId, uint32 strength) external {
        _bandits[banditId].strength = strength;
    }

    function blueprintUnit() external pure returns (uint256) {
        return BLUEPRINT_UNIT;
    }
}

contract BanditDefeatGoldRewardTest is Test {
    BanditDefeatGoldRewardHarness world;

    function setUp() public {
        world = new BanditDefeatGoldRewardHarness();
    }

    function _advanceTick() internal {
        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
        world.heartbeat();
    }

    function test_banditDefeatRewardsBothBlueprintAndGold() public {
        (uint32 clanId,) = world.mintClan(address(0xA1));
        Clan memory beforeClan = world.getClan(clanId);
        uint32 banditId = world.forceAttackingBandit(beforeClan.baseRegion, 1, clanId);
        world.setBanditStrength(banditId, 0);

        _advanceTick();

        Clan memory afterClan = world.getClan(clanId);
        assertEq(afterClan.blueprintBalance, beforeClan.blueprintBalance + world.blueprintUnit(), "blueprint reward");
        assertEq(afterClan.goldBalance, beforeClan.goldBalance + 1e18, "gold reward");
    }
}
