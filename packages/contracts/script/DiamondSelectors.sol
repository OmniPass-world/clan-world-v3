// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IDiamondLoupe} from "../src/diamond/IDiamondLoupe.sol";
import {IClanWorld} from "../src/IClanWorld.sol";
import {HeartbeatConfigFacet} from "../src/diamond/facets/HeartbeatConfigFacet.sol";
import {HeartbeatFacet} from "../src/diamond/facets/HeartbeatFacet.sol";
import {OwnershipFacet} from "../src/diamond/facets/OwnershipFacet.sol";
import {WorldPauseFacet} from "../src/diamond/facets/WorldPauseFacet.sol";

library DiamondSelectors {
    function loupeSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](4);
        selectors[0] = IDiamondLoupe.facets.selector;
        selectors[1] = IDiamondLoupe.facetFunctionSelectors.selector;
        selectors[2] = IDiamondLoupe.facetAddresses.selector;
        selectors[3] = IDiamondLoupe.facetAddress.selector;
    }

    function heartbeatSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = HeartbeatFacet.heartbeat.selector;
    }

    function heartbeatConfigSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](6);
        selectors[0] = HeartbeatConfigFacet.heartbeatIntervalSeconds.selector;
        selectors[1] = HeartbeatConfigFacet.setHeartbeatIntervalSeconds.selector;
        selectors[2] = HeartbeatConfigFacet.clansmanCooldownSeconds.selector;
        selectors[3] = HeartbeatConfigFacet.setClansmanCooldownSeconds.selector;
        selectors[4] = HeartbeatConfigFacet.banditSpawnTriggered.selector;
        selectors[5] = HeartbeatConfigFacet.triggerBanditSpawn.selector;
    }

    function worldPauseSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](3);
        selectors[0] = WorldPauseFacet.pauseWorld.selector;
        selectors[1] = WorldPauseFacet.unpauseWorld.selector;
        selectors[2] = WorldPauseFacet.isWorldPaused.selector;
    }

    function seasonSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.finalizeSeason.selector;
    }

    function ownershipFacetSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](2);
        selectors[0] = OwnershipFacet.owner.selector;
        selectors[1] = OwnershipFacet.transferOwnership.selector;
    }

    function rawWorldViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](7);
        selectors[0] = IClanWorld.getWorldState.selector;
        selectors[1] = IClanWorld.isWinter.selector;
        selectors[2] = IClanWorld.getWallUpgradeCost.selector;
        selectors[3] = IClanWorld.getBaseUpgradeCost.selector;
        selectors[4] = IClanWorld.getMonumentUpgradeCost.selector;
        selectors[5] = IClanWorld.getActionDuration.selector;
        selectors[6] = IClanWorld.getTravelTicks.selector;
    }

    function rawTreasuryViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](5);
        selectors[0] = IClanWorld.getTreasuryState.selector;
        selectors[1] = IClanWorld.getResourceToken.selector;
        selectors[2] = IClanWorld.getPool.selector;
        selectors[3] = IClanWorld.getPrice.selector;
        selectors[4] = IClanWorld.getScheduledMarketActionsForTick.selector;
    }

    function rawClanViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](11);
        selectors[0] = IClanWorld.getClan.selector;
        selectors[1] = IClanWorld.getClansman.selector;
        selectors[2] = IClanWorld.getActiveMission.selector;
        selectors[3] = IClanWorld.getMissionTiming.selector;
        selectors[4] = IClanWorld.getWheatPlots.selector;
        selectors[5] = IClanWorld.getActiveDefenders.selector;
        selectors[6] = IClanWorld.getDefendingClans.selector;
        selectors[7] = IClanWorld.getClanIds.selector;
        selectors[8] = IClanWorld.getClanClansmanIds.selector;
        selectors[9] = IClanWorld.getClansmanDefendingRegion.selector;
        selectors[10] = IClanWorld.getMonumentLevelReachedAt.selector;
    }

    function rawBanditViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](3);
        selectors[0] = IClanWorld.getBandit.selector;
        selectors[1] = IClanWorld.getBanditTroop.selector;
        selectors[2] = IClanWorld.getBanditsInRegion.selector;
    }

    function lifecycleSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.mintClan.selector;
    }

    function submitOrdersSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.submitClanOrders.selector;
    }

    function ownershipSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.transferClanOwnership.selector;
    }

    function treasurySelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](2);
        selectors[0] = IClanWorld.initTreasury.selector;
        selectors[1] = IClanWorld.seedPools.selector;
    }

    function settlementSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](2);
        selectors[0] = IClanWorld.settleClan.selector;
        selectors[1] = IClanWorld.settleClansman.selector;
    }

    function directTransferSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](4);
        selectors[0] = IClanWorld.transferGold.selector;
        selectors[1] = IClanWorld.transferVaultResource.selector;
        selectors[2] = IClanWorld.transferBlueprint.selector;
        selectors[3] = IClanWorld.transferBundle.selector;
    }

    function derivedViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](2);
        selectors[0] = IClanWorld.getDerivedClanState.selector;
        selectors[1] = IClanWorld.getDerivedClansmanState.selector;
    }

    function marketViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.getMarketState.selector;
    }

    function banditViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.getActiveBanditView.selector;
    }

    function regionViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.getRegionPopulation.selector;
    }

    function snapshotViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.getWorldSnapshot.selector;
    }

    function clanFullViewSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.getClanFullView.selector;
    }

    function quoteViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](3);
        selectors[0] = IClanWorld.getBanditTargetPreview.selector;
        selectors[1] = IClanWorld.quoteTravel.selector;
        selectors[2] = IClanWorld.quoteLootValueRaw.selector;
    }

    function scoringViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](3);
        selectors[0] = IClanWorld.quoteLootValueSettled.selector;
        selectors[1] = IClanWorld.getClanScore.selector;
        selectors[2] = IClanWorld.getRankings.selector;
    }
}
