// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ActiveBanditView,
    ClanWorldConstants,
    BanditState,
    BanditTroop,
    ClanState,
    WorldState
} from "../src/IClanWorld.sol";

contract BanditHarness is ClanWorld {
    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
        return _spawnBandit(region, strength);
    }

    function transitionBandit(uint32 id, BanditState newState) external {
        _transitionBanditState(id, newState);
    }

    function transitionBanditToAttacking(uint32 id, uint32 targetClanId) external {
        _transitionBanditToAttacking(id, targetClanId);
    }

    function setClanBaseAndLivingForTest(uint32 clanId, uint8 baseRegion, uint8 livingClansmen) external {
        _clans[clanId].baseRegion = baseRegion;
        _clans[clanId].livingClansmen = livingClansmen;
        _clans[clanId].clanState = ClanState.ACTIVE;
    }
}

contract BanditTest is Test {
    BanditHarness world;
    uint160 nextElder = 0xA11CE;

    function setUp() public {
        world = new BanditHarness();
    }

    function _advanceTick() internal {
        vm.warp(world.getWorldState().nextHeartbeatAtTs);
        world.heartbeat();
    }

    function _advanceTicks(uint64 ticks) internal {
        for (uint64 i = 0; i < ticks; i++) {
            _advanceTick();
        }
    }

    function _advanceUntil(uint64 tick) internal {
        while (world.getWorldState().currentTick < tick) {
            _advanceTick();
        }
    }

    function _closeTick(uint64 tick) internal {
        while (world.getWorldState().currentTick <= tick) {
            _advanceTick();
        }
    }

    function _mintClan() internal returns (uint32 clanId) {
        address elder = address(nextElder++);
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _spawnAndCamp() internal returns (uint32 id) {
        id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        _advanceTicks(2);
    }

    function _spawnCampAndAttack(uint32 targetClanId) internal returns (uint32 id) {
        id = _spawnAndCamp();
        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
        world.transitionBanditToAttacking(id, targetClanId);
    }

    function test_spawnBandit_recordsSpawnedTroopAndRegionIndex() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 250);

        assertEq(id, 1, "first bandit id");
        BanditTroop memory bandit = world.getBandit(id);
        assertEq(bandit.id, id, "bandit id");
        assertEq(bandit.region, ClanWorldConstants.REGION_MOUNTAINS, "region");
        assertEq(uint8(bandit.state), uint8(BanditState.Spawned), "state");
        assertEq(bandit.targetClanId, 0, "no target while spawned");
        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick, "entered tick");
        assertEq(bandit.strength, 250, "strength");
        assertEq(bandit.tier, 0, "custom strength has no v4 tier");
        assertEq(bandit.attackAttemptsMade, 0, "no attacks yet");

        uint32[] memory regionBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
        assertEq(regionBandits.length, 1, "region index length");
        assertEq(regionBandits[0], id, "region index id");

        WorldState memory state = world.getWorldState();
        assertEq(state.activeBanditId, id, "active bandit");
    }

    function test_getBanditAbiDecodeIncludesTierAndAttackAttempts() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 45);

        (bool getBanditOk, bytes memory getBanditData) =
            address(world).staticcall(abi.encodeWithSelector(world.getBandit.selector, id));
        assertTrue(getBanditOk, "getBandit call");
        BanditTroop memory bandit = abi.decode(getBanditData, (BanditTroop));
        assertEq(bandit.id, id, "getBandit id");
        assertEq(bandit.tier, 2, "getBandit tier");
        assertEq(bandit.attackAttemptsMade, 0, "getBandit attempts");

        (bool getTroopOk, bytes memory getTroopData) =
            address(world).staticcall(abi.encodeWithSignature("getBanditTroop(uint32)", id));
        assertTrue(getTroopOk, "getBanditTroop call");
        BanditTroop memory troop = abi.decode(getTroopData, (BanditTroop));
        assertEq(troop.id, id, "getBanditTroop id");
        assertEq(troop.tier, 2, "getBanditTroop tier");
        assertEq(troop.attackAttemptsMade, 0, "getBanditTroop attempts");
    }

    function test_getBanditMissingIdReturnsNoneState() public view {
        BanditTroop memory bandit = world.getBandit(999);

        assertEq(bandit.id, 0, "missing id");
        assertEq(uint8(bandit.state), uint8(BanditState.None), "missing state");
    }

    function test_defaultBanditTroopStateIsNone() public pure {
        BanditTroop memory bandit;

        assertEq(uint8(bandit.state), uint8(BanditState.None), "default state");
    }

    function test_spawnedToCampedTransitionAdvancesAfterSpawnDelay() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);

        _advanceTicks(2);

        BanditTroop memory bandit = world.getBandit(id);
        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "state");
        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick - 1, "entered camp tick");
    }

    function test_spawnedActiveBanditViewReportsNextActionTick() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);

        ActiveBanditView memory view_ = world.getActiveBanditView();

        assertEq(view_.banditId, id, "active bandit");
        assertEq(uint8(view_.state), uint8(BanditState.Spawned), "state");
        assertEq(view_.nextActionTick, world.getWorldState().currentTick + 1, "spawn delay tick");
    }

    function test_campedToAttackingTransitionSucceedsWithTargetAfterCampDuration() public {
        uint32 id = _spawnAndCamp();
        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);

        world.transitionBanditToAttacking(id, 77);

        BanditTroop memory bandit = world.getBandit(id);
        assertEq(uint8(bandit.state), uint8(BanditState.Attacking), "state");
        assertEq(bandit.targetClanId, 77, "target");
        assertEq(bandit.tickEnteredState, world.getWorldState().currentTick, "entered attack tick");
    }

    function test_attackingToDefeatedDeletesBanditAndRegionIndex() public {
        uint32 id = _spawnCampAndAttack(77);

        world.transitionBandit(id, BanditState.Defeated);

        BanditTroop memory deletedBandit = world.getBandit(id);
        assertEq(deletedBandit.id, 0, "deleted id");
        assertEq(deletedBandit.region, 0, "deleted region");
        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "deleted state");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "removed from region");
        assertEq(world.getWorldState().activeBanditId, 0, "active bandit cleared");
    }

    function test_defeatingActiveBanditPromotesOldestRemainingBandit() public {
        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 200);
        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_WEST_FARMS, 300);

        _advanceTicks(2);
        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
        world.transitionBanditToAttacking(id1, 77);
        world.transitionBandit(id1, BanditState.Defeated);

        assertEq(world.getWorldState().activeBanditId, id2, "oldest remaining promoted");
        assertEq(world.getBandit(id3).id, id3, "newer bandit remains live");
    }

    function test_attackingEscapeMovesBanditToNextRegion() public {
        uint32 id = _spawnCampAndAttack(77);

        world.transitionBandit(id, BanditState.Camped);
        BanditTroop memory camped = world.getBandit(id);
        assertEq(uint8(camped.state), uint8(BanditState.Camped), "camped");
        assertEq(camped.targetClanId, 0, "target remains clear");
        assertEq(camped.region, ClanWorldConstants.REGION_MOUNTAINS, "rampaged to next region");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "left forest index");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS)[0], id, "entered mountain index");
    }

    function test_multipleBanditsSameRegionAttackResolution() public {
        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 200);
        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 300);

        _advanceTicks(2);
        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);

        world.transitionBanditToAttacking(id1, 77);
        world.transitionBanditToAttacking(id2, 77);
        world.transitionBanditToAttacking(id3, 77);

        _advanceTick();

        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "all left forest");
        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
        assertEq(mountainBandits.length, 3, "all entered mountains");

        _assertResolvedInMountains(id1, mountainBandits, "id1");
        _assertResolvedInMountains(id2, mountainBandits, "id2");
        _assertResolvedInMountains(id3, mountainBandits, "id3");
    }

    function test_noTargetAdvancesToNextRegion() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint64 spawnedAt = world.getWorldState().currentTick;

        _closeTick(spawnedAt + 4);

        BanditTroop memory bandit = world.getBandit(id);
        assertEq(uint8(bandit.state), uint8(BanditState.Camped), "still camped");
        assertEq(bandit.region, ClanWorldConstants.REGION_MOUNTAINS, "advanced to next region");
        assertEq(bandit.attackAttemptsMade, 1, "no-target attempt counted");
        assertEq(bandit.tickEnteredState, spawnedAt + 4, "camp timer reset");
        assertEq(bandit.targetClanId, 0, "target remains clear");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "left forest index");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS)[0], id, "entered mountain index");
    }

    function test_noTargetCounterPersistsAcrossRegions() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint64 spawnedAt = world.getWorldState().currentTick;

        _closeTick(spawnedAt + 4);
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_MOUNTAINS, "first no-target move");
        assertEq(world.getBandit(id).attackAttemptsMade, 1, "first no-target counted");

        _closeTick(spawnedAt + 7);

        BanditTroop memory bandit = world.getBandit(id);
        assertEq(bandit.region, ClanWorldConstants.REGION_EAST_FARMS, "second no-target move");
        assertEq(bandit.attackAttemptsMade, 2, "counter persisted");
        assertEq(bandit.tickEnteredState, spawnedAt + 7, "second camp timer reset");
    }

    function test_terminalEscapeAfterSixNoTargetRegionAdvances() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint64 spawnedAt = world.getWorldState().currentTick;

        _closeTick(spawnedAt + 4);
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_MOUNTAINS, "first move");
        assertEq(world.getBandit(id).attackAttemptsMade, 1, "first attempt");

        _closeTick(spawnedAt + 7);
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_EAST_FARMS, "second move");
        assertEq(world.getBandit(id).attackAttemptsMade, 2, "second attempt");

        _closeTick(spawnedAt + 10);
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_EAST_DOCKS, "third move");
        assertEq(world.getBandit(id).attackAttemptsMade, 3, "third attempt");

        _closeTick(spawnedAt + 13);
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_WEST_DOCKS, "fourth move");
        assertEq(world.getBandit(id).attackAttemptsMade, 4, "fourth attempt");

        _closeTick(spawnedAt + 16);
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_WEST_FARMS, "fifth move");
        assertEq(world.getBandit(id).attackAttemptsMade, 5, "fifth attempt");

        _closeTick(spawnedAt + 19);

        BanditTroop memory deletedBandit = world.getBandit(id);
        assertEq(deletedBandit.id, 0, "bandit deleted at no-target cap");
        assertEq(uint8(deletedBandit.state), uint8(BanditState.None), "terminal escape removed troop");
        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_WEST_FARMS).length, 0, "removed from final region");
    }

    function test_mixedFailedAttackAndNoTargetTerminalEscape() public {
        uint32 clanId = _mintClan();

        uint32 noTargetTerminalId = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 11);
        uint64 spawnedAt = world.getWorldState().currentTick;

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 4);
        _closeTick(spawnedAt + 4);
        assertEq(world.getBandit(noTargetTerminalId).attackAttemptsMade, 1, "failed attack counted");
        assertEq(world.getBandit(noTargetTerminalId).region, ClanWorldConstants.REGION_MOUNTAINS, "failed attack moved");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 0);
        _closeTick(spawnedAt + 7);
        assertEq(world.getBandit(noTargetTerminalId).attackAttemptsMade, 2, "no-target counted");
        assertEq(world.getBandit(noTargetTerminalId).region, ClanWorldConstants.REGION_EAST_FARMS, "no-target moved");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_EAST_FARMS, 4);
        _closeTick(spawnedAt + 10);
        assertEq(world.getBandit(noTargetTerminalId).attackAttemptsMade, 3, "second failed attack counted");
        assertEq(
            world.getBandit(noTargetTerminalId).region, ClanWorldConstants.REGION_EAST_DOCKS, "second failed moved"
        );

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 0);
        _closeTick(spawnedAt + 13);
        assertEq(world.getBandit(noTargetTerminalId).attackAttemptsMade, 4, "second no-target counted");
        assertEq(
            world.getBandit(noTargetTerminalId).region, ClanWorldConstants.REGION_WEST_DOCKS, "second no-target moved"
        );

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_WEST_DOCKS, 4);
        _closeTick(spawnedAt + 16);
        assertEq(world.getBandit(noTargetTerminalId).attackAttemptsMade, 5, "third failed attack counted");
        assertEq(world.getBandit(noTargetTerminalId).region, ClanWorldConstants.REGION_WEST_FARMS, "third failed moved");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 0);
        _closeTick(spawnedAt + 19);
        assertEq(world.getBandit(noTargetTerminalId).id, 0, "sixth mixed attempt terminal escaped on no-target");

        uint32 failedAttackTerminalId = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 11);
        spawnedAt = world.getWorldState().currentTick;

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 0);
        _closeTick(spawnedAt + 4);
        assertEq(world.getBandit(failedAttackTerminalId).attackAttemptsMade, 1, "initial no-target counted");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_MOUNTAINS, 4);
        _closeTick(spawnedAt + 7);
        assertEq(world.getBandit(failedAttackTerminalId).attackAttemptsMade, 2, "initial failed attack counted");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 0);
        _closeTick(spawnedAt + 10);
        assertEq(world.getBandit(failedAttackTerminalId).attackAttemptsMade, 3, "middle no-target counted");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_EAST_DOCKS, 4);
        _closeTick(spawnedAt + 13);
        assertEq(world.getBandit(failedAttackTerminalId).attackAttemptsMade, 4, "middle failed attack counted");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_FOREST, 0);
        _closeTick(spawnedAt + 16);
        assertEq(world.getBandit(failedAttackTerminalId).attackAttemptsMade, 5, "final no-target counted");

        world.setClanBaseAndLivingForTest(clanId, ClanWorldConstants.REGION_WEST_FARMS, 4);
        _closeTick(spawnedAt + 19);
        assertEq(world.getBandit(failedAttackTerminalId).id, 0, "sixth mixed attempt terminal escaped on failed attack");
    }

    function test_multipleBanditsSameRegionNoTargetAdvance() public {
        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 200);
        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 300);
        uint64 spawnedAt = world.getWorldState().currentTick;

        _closeTick(spawnedAt + 4);

        assertEq(world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST).length, 0, "all left forest");
        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
        assertEq(mountainBandits.length, 3, "all entered mountains");

        _assertNoTargetAdvancedToMountains(id1, mountainBandits, "id1");
        _assertNoTargetAdvancedToMountains(id2, mountainBandits, "id2");
        _assertNoTargetAdvancedToMountains(id3, mountainBandits, "id3");
    }

    function test_steadyStateCycleIs3Ticks() public {
        for (uint256 i = 0; i < 6; i++) {
            _mintClan();
        }

        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 1000);
        uint64 spawnedAt = world.getWorldState().currentTick;

        _closeTick(spawnedAt + 4);
        assertEq(uint8(world.getBandit(id).state), uint8(BanditState.Camped), "first attack recamped");
        assertEq(world.getBandit(id).attackAttemptsMade, 1, "first attempt");
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_MOUNTAINS, "first move");
        assertEq(world.getBandit(id).tickEnteredState, spawnedAt + 4, "first recamp tick");

        _closeTick(spawnedAt + 7);
        assertEq(uint8(world.getBandit(id).state), uint8(BanditState.Camped), "second attack recamped");
        assertEq(world.getBandit(id).attackAttemptsMade, 2, "second attempt");
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_EAST_FARMS, "second move");
        assertEq(world.getBandit(id).tickEnteredState, spawnedAt + 7, "second recamp tick");

        _closeTick(spawnedAt + 10);
        assertEq(uint8(world.getBandit(id).state), uint8(BanditState.Camped), "third attack recamped");
        assertEq(world.getBandit(id).attackAttemptsMade, 3, "third attempt");
        assertEq(world.getBandit(id).region, ClanWorldConstants.REGION_EAST_DOCKS, "third move");
        assertEq(world.getBandit(id).tickEnteredState, spawnedAt + 10, "third recamp tick");
    }

    function test_invalidTransitionsRevert() public {
        uint32 id = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);

        vm.expectRevert("ClanWorld: invalid bandit transition");
        world.transitionBandit(id, BanditState.Defeated);

        _advanceTicks(2);
        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);

        vm.expectRevert("ClanWorld: invalid bandit transition");
        world.transitionBandit(id, BanditState.Attacking);

        vm.expectRevert("ClanWorld: invalid bandit transition");
        world.transitionBandit(id, BanditState.None);
    }

    function test_getBanditsInRegionReturnsListAndUpdatesAfterDelete() public {
        uint32 id1 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);
        uint32 id2 = world.spawnBandit(ClanWorldConstants.REGION_FOREST, 200);
        uint32 id3 = world.spawnBandit(ClanWorldConstants.REGION_MOUNTAINS, 300);

        uint32[] memory forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
        assertEq(forestBandits.length, 2, "forest count");
        assertEq(forestBandits[0], id1, "first forest bandit");
        assertEq(forestBandits[1], id2, "second forest bandit");

        uint32[] memory mountainBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_MOUNTAINS);
        assertEq(mountainBandits.length, 1, "mountain count");
        assertEq(mountainBandits[0], id3, "mountain bandit");

        _advanceTicks(2);
        _advanceTicks(ClanWorldConstants.BANDIT_CAMP_TICKS - 1);
        world.transitionBanditToAttacking(id1, 77);
        world.transitionBandit(id1, BanditState.Defeated);

        forestBandits = world.getBanditsInRegion(ClanWorldConstants.REGION_FOREST);
        assertEq(forestBandits.length, 1, "forest count after delete");
        assertEq(forestBandits[0], id2, "remaining forest bandit");
    }

    function _assertResolvedInMountains(uint32 id, uint32[] memory mountainBandits, string memory label) internal view {
        BanditTroop memory bandit = world.getBandit(id);
        assertEq(uint8(bandit.state), uint8(BanditState.Camped), label);
        assertEq(bandit.targetClanId, 0, label);
        assertEq(bandit.region, ClanWorldConstants.REGION_MOUNTAINS, label);
        assertTrue(_containsBandit(mountainBandits, id), label);
    }

    function _assertNoTargetAdvancedToMountains(uint32 id, uint32[] memory mountainBandits, string memory label)
        internal
        view
    {
        BanditTroop memory bandit = world.getBandit(id);
        assertEq(uint8(bandit.state), uint8(BanditState.Camped), label);
        assertEq(bandit.targetClanId, 0, label);
        assertEq(bandit.region, ClanWorldConstants.REGION_MOUNTAINS, label);
        assertEq(bandit.attackAttemptsMade, 1, label);
        assertTrue(_containsBandit(mountainBandits, id), label);
    }

    function _containsBandit(uint32[] memory banditIds, uint32 id) internal pure returns (bool) {
        for (uint256 i = 0; i < banditIds.length; i++) {
            if (banditIds[i] == id) {
                return true;
            }
        }
        return false;
    }
}
