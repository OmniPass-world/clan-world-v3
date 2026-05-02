// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {ClanWorld} from "../../src/ClanWorld.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {IClanWorld, ActionType, WorldState} from "../../src/IClanWorld.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {IDiamondLoupe} from "../../src/diamond/IDiamondLoupe.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {DiamondLoupeFacet} from "../../src/diamond/facets/DiamondLoupeFacet.sol";
import {RawViewsFacet} from "../../src/diamond/facets/RawViewsFacet.sol";

contract PingFacet {
    function ping() external pure returns (uint256) {
        return 42;
    }
}

interface IPing {
    function ping() external view returns (uint256);
}

contract DiamondSkeletonTest is Test {
    Diamond diamond;
    DiamondCutFacet cutFacet;

    function setUp() public {
        cutFacet = new DiamondCutFacet();
        diamond = new Diamond(address(this), address(cutFacet));
    }

    function testOwnerCanAddAndRouteFacetSelector() public {
        PingFacet pingFacet = new PingFacet();
        bytes4[] memory selectors = new bytes4[](1);
        selectors[0] = PingFacet.ping.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(pingFacet), action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });

        IDiamondCut(address(diamond)).diamondCut(cut, address(0), "");

        assertEq(IPing(address(diamond)).ping(), 42);
    }

    function testLoupeReportsAddedSelector() public {
        DiamondLoupeFacet loupeFacet = new DiamondLoupeFacet();
        bytes4[] memory selectors = new bytes4[](4);
        selectors[0] = IDiamondLoupe.facets.selector;
        selectors[1] = IDiamondLoupe.facetFunctionSelectors.selector;
        selectors[2] = IDiamondLoupe.facetAddresses.selector;
        selectors[3] = IDiamondLoupe.facetAddress.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(loupeFacet), action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });

        IDiamondCut(address(diamond)).diamondCut(cut, address(0), "");

        assertEq(IDiamondLoupe(address(diamond)).facetAddress(IDiamondLoupe.facets.selector), address(loupeFacet));
        assertEq(IDiamondLoupe(address(diamond)).facetAddresses().length, 2);
    }

    function testOnlyOwnerCanDiamondCut() public {
        PingFacet pingFacet = new PingFacet();
        bytes4[] memory selectors = new bytes4[](1);
        selectors[0] = PingFacet.ping.selector;

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(pingFacet), action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });

        vm.prank(address(0xBEEF));
        vm.expectRevert();
        IDiamondCut(address(diamond)).diamondCut(cut, address(0), "");
    }

    function testCanInitializeDiamondAndReadRawWorldState() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );

        IClanWorld diamondWorld = IClanWorld(address(diamond));
        WorldState memory expected = core.getWorldState();
        WorldState memory actual = diamondWorld.getWorldState();
        _assertWorldStateEq(actual, expected);

        assertEq(diamondWorld.isWinter(), core.isWinter());
        assertEq(diamondWorld.getActionDuration(ActionType.ChopWood), core.getActionDuration(ActionType.ChopWood));
        assertEq(diamondWorld.getTravelTicks(1, 8), core.getTravelTicks(1, 8));

        (uint256 wallWood, uint256 wallIron) = diamondWorld.getWallUpgradeCost(2);
        (uint256 coreWallWood, uint256 coreWallIron) = core.getWallUpgradeCost(2);
        assertEq(wallWood, coreWallWood);
        assertEq(wallIron, coreWallIron);

        assertEq(diamondWorld.getClanIds().length, 0);
        assertEq(uint8(diamondWorld.getBandit(1).state), 0);
    }

    function testDiamondInitCannotRunTwice() public {
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );

        IDiamondCut.FacetCut[] memory emptyCut = new IDiamondCut.FacetCut[](0);
        vm.expectRevert(ClanWorldDiamondInit.ClanWorldDiamondAlreadyInitialized.selector);
        IDiamondCut(address(diamond)).diamondCut(emptyCut, address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));
    }

    function _rawViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        bytes4[] memory selectors = new bytes4[](22);
        selectors[0] = IClanWorld.getWorldState.selector;
        selectors[1] = IClanWorld.getTreasuryState.selector;
        selectors[2] = IClanWorld.getClan.selector;
        selectors[3] = IClanWorld.getClansman.selector;
        selectors[4] = IClanWorld.getActiveMission.selector;
        selectors[5] = IClanWorld.getMissionTiming.selector;
        selectors[6] = IClanWorld.isWinter.selector;
        selectors[7] = IClanWorld.getWallUpgradeCost.selector;
        selectors[8] = IClanWorld.getBaseUpgradeCost.selector;
        selectors[9] = IClanWorld.getMonumentUpgradeCost.selector;
        selectors[10] = IClanWorld.getActionDuration.selector;
        selectors[11] = IClanWorld.getTravelTicks.selector;
        selectors[12] = IClanWorld.getBandit.selector;
        selectors[13] = IClanWorld.getBanditTroop.selector;
        selectors[14] = IClanWorld.getBanditsInRegion.selector;
        selectors[15] = IClanWorld.getWheatPlots.selector;
        selectors[16] = IClanWorld.getScheduledMarketActionsForTick.selector;
        selectors[17] = IClanWorld.getDefendingClans.selector;
        selectors[18] = IClanWorld.getClanIds.selector;
        selectors[19] = IClanWorld.getClanClansmanIds.selector;
        selectors[20] = IClanWorld.getClansmanDefendingRegion.selector;
        selectors[21] = IClanWorld.getMonumentLevelReachedAt.selector;

        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet, action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });
    }

    function _assertWorldStateEq(WorldState memory actual, WorldState memory expected) internal pure {
        assertEq(actual.currentTick, expected.currentTick, "currentTick");
        assertEq(actual.seasonStartTick, expected.seasonStartTick, "seasonStartTick");
        assertEq(actual.seasonEndTick, expected.seasonEndTick, "seasonEndTick");
        assertEq(actual.seasonFinalized, expected.seasonFinalized, "seasonFinalized");
        assertEq(actual.currentSeasonNumber, expected.currentSeasonNumber, "currentSeasonNumber");
        assertEq(actual.nextHeartbeatAtTick, expected.nextHeartbeatAtTick, "nextHeartbeatAtTick");
        assertEq(actual.nextHeartbeatAtTs, expected.nextHeartbeatAtTs, "nextHeartbeatAtTs");
        assertEq(actual.nextBanditSpawnEligibleTick, expected.nextBanditSpawnEligibleTick, "banditEligible");
        assertEq(actual.currentBanditSpawnChanceBps, expected.currentBanditSpawnChanceBps, "banditChance");
        assertEq(actual.currentTickSeed, expected.currentTickSeed, "currentTickSeed");
        assertEq(actual.activeBanditId, expected.activeBanditId, "activeBanditId");
        assertEq(actual.winterActive, expected.winterActive, "winterActive");
        assertEq(actual.winterStartsAtTick, expected.winterStartsAtTick, "winterStartsAtTick");
        assertEq(actual.winterEndsAtTick, expected.winterEndsAtTick, "winterEndsAtTick");
        assertEq(actual.nextCommitSequence, expected.nextCommitSequence, "nextCommitSequence");
    }
}
