// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {ClanWorldConstants} from "../src/IClanWorld.sol";

contract BanditForbiddenRegionsHarness is ClanWorld {
    function setCurrentTick(uint64 tick) external {
        _world.currentTick = tick;
        _world.nextHeartbeatAtTick = tick + 1;
    }

    function setClanRegion(uint32 clanId, uint8 region) external {
        _clans[clanId].baseRegion = region;
    }

    function setBanditSpawnState(uint8 region, uint64 lastSpawnTick, uint16 probabilityAccum) external {
        _banditSpawnByRegion[region].lastSpawnTick = lastSpawnTick;
        _banditSpawnByRegion[region].probabilityAccum = probabilityAccum;
    }

    function evaluateBanditSpawns(bytes32 tickSeed) external {
        _evaluateBanditSpawns(tickSeed);
    }

    function spawnBandit(uint8 region) external returns (uint32) {
        return _spawnBandit(region, 30);
    }

    function banditSpawnRegionWeights() external view returns (uint256[] memory) {
        return _banditSpawnRegionWeights();
    }

    function activeBanditCount() external view returns (uint32) {
        return _activeBanditCount;
    }
}

contract BanditForbiddenRegionsTest is Test {
    function test_banditNeverSpawnsInUnicornTownOrDeepSea() public {
        BanditForbiddenRegionsHarness world = new BanditForbiddenRegionsHarness();
        (uint32 unicornClanId,) = world.mintClan(address(0xA1));
        (uint32 deepSeaClanId,) = world.mintClan(address(0xA2));

        world.setClanRegion(unicornClanId, ClanWorldConstants.REGION_UNICORN_TOWN);
        world.setClanRegion(deepSeaClanId, ClanWorldConstants.REGION_DEEP_SEA);
        world.setCurrentTick(100);
        world.setBanditSpawnState(ClanWorldConstants.REGION_UNICORN_TOWN, 0, 9999);
        world.setBanditSpawnState(ClanWorldConstants.REGION_DEEP_SEA, 0, 9999);

        uint256[] memory weights = world.banditSpawnRegionWeights();
        assertEq(weights[ClanWorldConstants.REGION_UNICORN_TOWN - 1], 0, "unicorn town weight");
        assertEq(weights[ClanWorldConstants.REGION_DEEP_SEA - 1], 0, "deep sea weight");

        world.evaluateBanditSpawns(keccak256("forbidden-regions"));

        assertEq(world.activeBanditCount(), 0, "no forbidden spawn");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_UNICORN_TOWN).length, 0, "no unicorn bandit");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_DEEP_SEA).length, 0, "no deep sea bandit");

        vm.expectRevert(bytes("ClanWorld: forbidden bandit region"));
        world.spawnBandit(ClanWorldConstants.REGION_UNICORN_TOWN);

        vm.expectRevert(bytes("ClanWorld: forbidden bandit region"));
        world.spawnBandit(ClanWorldConstants.REGION_DEEP_SEA);
    }
}
