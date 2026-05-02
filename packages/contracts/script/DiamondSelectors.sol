// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {IDiamondLoupe} from "../src/diamond/IDiamondLoupe.sol";
import {IClanWorld} from "../src/IClanWorld.sol";

library DiamondSelectors {
    function loupeSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](4);
        selectors[0] = IDiamondLoupe.facets.selector;
        selectors[1] = IDiamondLoupe.facetFunctionSelectors.selector;
        selectors[2] = IDiamondLoupe.facetAddresses.selector;
        selectors[3] = IDiamondLoupe.facetAddress.selector;
    }

    function rawViewsSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](26);
        selectors[0] = IClanWorld.getWorldState.selector;
        selectors[1] = IClanWorld.getTreasuryState.selector;
        selectors[2] = IClanWorld.getResourceToken.selector;
        selectors[3] = IClanWorld.getPool.selector;
        selectors[4] = IClanWorld.getPrice.selector;
        selectors[5] = IClanWorld.getClan.selector;
        selectors[6] = IClanWorld.getClansman.selector;
        selectors[7] = IClanWorld.getActiveMission.selector;
        selectors[8] = IClanWorld.getMissionTiming.selector;
        selectors[9] = IClanWorld.isWinter.selector;
        selectors[10] = IClanWorld.getWallUpgradeCost.selector;
        selectors[11] = IClanWorld.getBaseUpgradeCost.selector;
        selectors[12] = IClanWorld.getMonumentUpgradeCost.selector;
        selectors[13] = IClanWorld.getActionDuration.selector;
        selectors[14] = IClanWorld.getTravelTicks.selector;
        selectors[15] = IClanWorld.getBandit.selector;
        selectors[16] = IClanWorld.getBanditTroop.selector;
        selectors[17] = IClanWorld.getBanditsInRegion.selector;
        selectors[18] = IClanWorld.getWheatPlots.selector;
        selectors[19] = IClanWorld.getScheduledMarketActionsForTick.selector;
        selectors[20] = IClanWorld.getActiveDefenders.selector;
        selectors[21] = IClanWorld.getDefendingClans.selector;
        selectors[22] = IClanWorld.getClanIds.selector;
        selectors[23] = IClanWorld.getClanClansmanIds.selector;
        selectors[24] = IClanWorld.getClansmanDefendingRegion.selector;
        selectors[25] = IClanWorld.getMonumentLevelReachedAt.selector;
    }

    function lifecycleSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.mintClan.selector;
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
}
