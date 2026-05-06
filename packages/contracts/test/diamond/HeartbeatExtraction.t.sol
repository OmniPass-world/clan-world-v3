// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {DiamondSelectors} from "../../script/DiamondSelectors.sol";
import {ClanWorld} from "../../src/ClanWorld.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {FinalizeSeasonFacet} from "../../src/diamond/facets/FinalizeSeasonFacet.sol";
import {HeartbeatFacet} from "../../src/diamond/facets/HeartbeatFacet.sol";
import {RawWorldViewsFacet} from "../../src/diamond/facets/RawWorldViewsFacet.sol";
import {LibStorage} from "../../src/diamond/lib/LibStorage.sol";
import {IClanWorld, ClanWorldConstants, WorldState} from "../../src/IClanWorld.sol";

interface IHeartbeatExtractionHarness {
    function setWorldForHeartbeat(uint64 currentTick, bool seasonFinalized) external;
}

contract HeartbeatExtractionHarnessFacet {
    function setWorldForHeartbeat(uint64 currentTick, bool seasonFinalized) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        bytes32 tickSeed = bytes32(uint256(currentTick));
        s.world.currentTick = currentTick;
        s.world.nextHeartbeatAtTick = currentTick + 1;
        s.world.nextHeartbeatAtTs = 0;
        s.world.currentTickSeed = tickSeed;
        s.world.seasonFinalized = seasonFinalized;
        s.tickSeeds[currentTick] = tickSeed;
    }
}

contract HeartbeatExtractionTest is Test {
    event WinterStarted(uint64 indexed tick);

    function test_emptyWorldHeartbeatAdvancesLikeCore() public {
        ClanWorld core = new ClanWorld();
        IClanWorld diamond = _deployDiamond();

        for (uint256 i = 0; i < 3; i++) {
            uint64 coreNext = core.getWorldState().nextHeartbeatAtTs;
            uint64 diamondNext = diamond.getWorldState().nextHeartbeatAtTs;
            vm.warp(coreNext > diamondNext ? coreNext : diamondNext);
            core.heartbeat();
            diamond.heartbeat();
        }

        _assertWorldStateEq(diamond.getWorldState(), core.getWorldState());
    }

    function test_heartbeatNoopsWhenSeasonFinalizationPending() public {
        IClanWorld diamond = _deployDiamond();
        IHeartbeatExtractionHarness(address(diamond)).setWorldForHeartbeat(ClanWorldConstants.SEASON_DURATION_TICKS, false);

        diamond.heartbeat();

        WorldState memory state = diamond.getWorldState();
        assertEq(state.currentTick, ClanWorldConstants.SEASON_DURATION_TICKS, "tick unchanged");
        assertFalse(state.seasonFinalized, "still pending finalization");
    }

    function test_heartbeatRollsSeasonAfterFinalization() public {
        IClanWorld diamond = _deployDiamond();
        IHeartbeatExtractionHarness(address(diamond)).setWorldForHeartbeat(ClanWorldConstants.SEASON_DURATION_TICKS, true);

        diamond.heartbeat();

        WorldState memory state = diamond.getWorldState();
        assertEq(state.currentSeasonNumber, 2, "season number");
        assertEq(state.seasonStartTick, ClanWorldConstants.SEASON_DURATION_TICKS, "season start");
        assertEq(state.seasonEndTick, ClanWorldConstants.SEASON_DURATION_TICKS * 2, "season end");
        assertFalse(state.seasonFinalized, "new season unfinalized");
    }

    function test_winterStartTransitionStillFires() public {
        IClanWorld diamond = _deployDiamond();
        IHeartbeatExtractionHarness(address(diamond)).setWorldForHeartbeat(ClanWorldConstants.WINTER_START_TICK - 1, false);

        vm.expectEmit(true, true, true, true, address(diamond));
        emit WinterStarted(ClanWorldConstants.WINTER_START_TICK);
        diamond.heartbeat();

        assertTrue(diamond.isWinter(), "winter active");
        assertEq(diamond.getWorldState().currentTick, ClanWorldConstants.WINTER_START_TICK, "winter tick");
    }

    function _deployDiamond() internal returns (IClanWorld diamondWorld) {
        DiamondCutFacet cutFacet = new DiamondCutFacet();
        Diamond diamond = new Diamond(address(this), address(cutFacet));
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](4);
        cut[0] = _facetCut(address(new RawWorldViewsFacet()), DiamondSelectors.rawWorldViewsSelectors());
        cut[1] = _facetCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors());
        cut[2] = _facetCut(address(new FinalizeSeasonFacet()), DiamondSelectors.seasonSelectors());
        cut[3] = _facetCut(address(new HeartbeatExtractionHarnessFacet()), _harnessSelectors());

        IDiamondCut(address(diamond)).diamondCut(cut, address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));
        diamondWorld = IClanWorld(address(diamond));
    }

    function _facetCut(address facet, bytes4[] memory selectors) internal pure returns (IDiamondCut.FacetCut memory) {
        return IDiamondCut.FacetCut({
            facetAddress: facet, action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });
    }

    function _harnessSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = HeartbeatExtractionHarnessFacet.setWorldForHeartbeat.selector;
    }

    function _assertWorldStateEq(WorldState memory actual, WorldState memory expected) internal pure {
        assertEq(actual.currentTick, expected.currentTick, "currentTick");
        assertEq(actual.seasonStartTick, expected.seasonStartTick, "seasonStartTick");
        assertEq(actual.seasonEndTick, expected.seasonEndTick, "seasonEndTick");
        assertEq(actual.seasonFinalized, expected.seasonFinalized, "seasonFinalized");
        assertEq(actual.currentSeasonNumber, expected.currentSeasonNumber, "currentSeasonNumber");
        assertEq(actual.nextHeartbeatAtTick, expected.nextHeartbeatAtTick, "nextHeartbeatAtTick");
        assertEq(actual.nextHeartbeatAtTs, expected.nextHeartbeatAtTs, "nextHeartbeatAtTs");
        assertEq(actual.nextBanditSpawnEligibleTick, expected.nextBanditSpawnEligibleTick, "nextBanditSpawnEligibleTick");
        assertEq(actual.currentBanditSpawnChanceBps, expected.currentBanditSpawnChanceBps, "spawn chance");
        assertEq(actual.currentTickSeed, expected.currentTickSeed, "currentTickSeed");
        assertEq(actual.activeBanditId, expected.activeBanditId, "activeBanditId");
        assertEq(actual.winterActive, expected.winterActive, "winterActive");
        assertEq(actual.winterStartsAtTick, expected.winterStartsAtTick, "winterStartsAtTick");
        assertEq(actual.winterEndsAtTick, expected.winterEndsAtTick, "winterEndsAtTick");
        assertEq(actual.nextCommitSequence, expected.nextCommitSequence, "nextCommitSequence");
        assertEq(actual.worldPaused, expected.worldPaused, "worldPaused");
        assertEq(actual.pausedAtTs, expected.pausedAtTs, "pausedAtTs");
    }
}
