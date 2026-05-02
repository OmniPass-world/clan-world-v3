// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ClanWorldConstants,
    WorldState
} from "../src/IClanWorld.sol";

contract SeasonFinalizationHarness is ClanWorld {
    function setCurrentTick(uint64 tick) external {
        _world.currentTick = tick;
        _world.nextHeartbeatAtTick = tick + 1;
    }

    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        _clans[clanId].vaultWood = wood;
        _clans[clanId].vaultIron = iron;
        _clans[clanId].vaultWheat = wheat;
        _clans[clanId].vaultFish = fish;
    }

    function setLivingClansmen(uint32 clanId, uint8 livingClansmen) external {
        _clans[clanId].livingClansmen = livingClansmen;
    }

    function disableBanditsForTest() external {
        _activeBanditCount = MAX_TOTAL_BANDITS;
    }
}

contract SeasonFinalizationTest is Test {
    event SeasonFinalized(uint64 indexed tick, uint32[] rankedClanIds, uint256[] scores);

    SeasonFinalizationHarness world;
    address elderA = address(0xA1);
    address elderB = address(0xA2);
    address elderC = address(0xA3);

    function setUp() public {
        world = new SeasonFinalizationHarness();
        world.disableBanditsForTest();
    }

    function _mintClan(address owner) internal returns (uint32 clanId) {
        vm.prank(owner);
        (clanId,) = world.mintClan(owner);
    }

    function _advanceTick() internal {
        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
        world.heartbeat();
    }

    function _advanceToTick(uint64 targetTick) internal {
        while (world.getWorldState().currentTick < targetTick) {
            _advanceTick();
        }
    }

    function test_finalizeSeasonRevertsBeforeSeasonEnd() public {
        vm.expectRevert("ClanWorld: season not ended");
        world.finalizeSeason();
    }

    function test_finalizeSeasonEmitsRankingsAfterSeasonEnd() public {
        uint32 clanA = _mintClan(elderA);
        uint32 clanB = _mintClan(elderB);
        uint32 clanC = _mintClan(elderC);
        world.setVault(clanA, 100e18, 0, 100e18, 0);
        world.setVault(clanB, 300e18, 0, 100e18, 0);
        world.setVault(clanC, 200e18, 0, 100e18, 0);
        world.setLivingClansmen(clanA, 0);
        world.setLivingClansmen(clanB, 0);
        world.setLivingClansmen(clanC, 0);
        _advanceToTick(ClanWorldConstants.SEASON_DURATION_TICKS);

        (uint32[] memory expectedRanked, uint256[] memory expectedScores) = world.getRankings();
        assertEq(expectedRanked[0], clanB, "rank 1");
        assertEq(expectedRanked[1], clanC, "rank 2");
        assertEq(expectedRanked[2], clanA, "rank 3");

        vm.expectEmit(true, false, false, true, address(world));
        emit SeasonFinalized(ClanWorldConstants.SEASON_DURATION_TICKS, expectedRanked, expectedScores);
        world.finalizeSeason();
    }

    function test_finalizeSeasonIsIdempotent() public {
        _advanceToTick(ClanWorldConstants.SEASON_DURATION_TICKS);
        world.finalizeSeason();

        vm.expectRevert("ClanWorld: season finalized");
        world.finalizeSeason();
    }

    function test_seasonAutoRollBlockedUntilFinalized() public {
        _advanceToTick(ClanWorldConstants.SEASON_DURATION_TICKS);

        WorldState memory beforeFinalize = world.getWorldState();
        assertEq(beforeFinalize.currentSeasonNumber, 1, "season number stays put");
        assertEq(beforeFinalize.seasonStartTick, 0, "season start stays put");
        assertEq(beforeFinalize.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS, "season end stays put");
        assertFalse(beforeFinalize.seasonFinalized, "season not finalized");

        world.finalizeSeason();
        _advanceTick();

        WorldState memory afterRoll = world.getWorldState();
        assertEq(afterRoll.currentSeasonNumber, 2, "season rolls after finalization");
        assertEq(afterRoll.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS, "new season start");
        assertEq(afterRoll.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2, "new season end");
        assertFalse(afterRoll.seasonFinalized, "new season reset");
    }

    function test_finalizeSeasonSettlesAllClans() public {
        uint32 clanA = _mintClan(elderA);
        uint32 clanB = _mintClan(elderB);
        uint32 clanC = _mintClan(elderC);
        world.setLivingClansmen(clanA, 0);
        world.setLivingClansmen(clanB, 0);
        world.setLivingClansmen(clanC, 0);
        _advanceToTick(ClanWorldConstants.SEASON_DURATION_TICKS);

        world.finalizeSeason();

        uint64 expectedSettledTick = world.getWorldState().currentTick + 1;
        assertEq(world.getClan(clanA).lastSettledTick, expectedSettledTick, "clan A settled");
        assertEq(world.getClan(clanB).lastSettledTick, expectedSettledTick, "clan B settled");
        assertEq(world.getClan(clanC).lastSettledTick, expectedSettledTick, "clan C settled");
    }

    function test_finalizeSeasonResetsForNextSeason() public {
        uint32 clanId = _mintClan(elderA);
        world.setLivingClansmen(clanId, 0);
        _advanceToTick(ClanWorldConstants.SEASON_DURATION_TICKS);

        world.finalizeSeason();
        _advanceTick();

        WorldState memory nextSeason = world.getWorldState();
        assertEq(nextSeason.currentSeasonNumber, 2, "season two active");
        assertFalse(nextSeason.seasonFinalized, "finalized flag reset");

        _advanceToTick(nextSeason.seasonEndTick);
        WorldState memory limbo = world.getWorldState();
        assertEq(limbo.currentSeasonNumber, 2, "second season also waits for finalization");

        world.finalizeSeason();
        _advanceTick();

        WorldState memory thirdSeason = world.getWorldState();
        assertEq(thirdSeason.currentSeasonNumber, 3, "third season active");
        assertEq(thirdSeason.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2, "third season start");
        assertEq(thirdSeason.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 3, "third season end");
        assertFalse(thirdSeason.seasonFinalized, "finalized flag reset again");
    }
}
