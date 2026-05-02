// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Script, console} from "forge-std/Script.sol";
import {Diamond} from "../src/diamond/Diamond.sol";
import {ClanWorldDiamondInit} from "../src/diamond/ClanWorldDiamondInit.sol";
import {IDiamondCut} from "../src/diamond/IDiamondCut.sol";
import {IDiamondLoupe} from "../src/diamond/IDiamondLoupe.sol";
import {DiamondCutFacet} from "../src/diamond/facets/DiamondCutFacet.sol";
import {DiamondLoupeFacet} from "../src/diamond/facets/DiamondLoupeFacet.sol";
import {ClanLifecycleFacet} from "../src/diamond/facets/ClanLifecycleFacet.sol";
import {DerivedViewsFacet} from "../src/diamond/facets/DerivedViewsFacet.sol";
import {RawViewsFacet} from "../src/diamond/facets/RawViewsFacet.sol";
import {IClanWorld} from "../src/IClanWorld.sol";

contract DeployDiamond is Script {
    function run() external {
        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
        address owner = vm.addr(deployerPrivateKey);

        vm.startBroadcast(deployerPrivateKey);

        DiamondCutFacet cutFacet = new DiamondCutFacet();
        Diamond diamond = new Diamond(owner, address(cutFacet));
        DiamondLoupeFacet loupeFacet = new DiamondLoupeFacet();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        DerivedViewsFacet derivedViewsFacet = new DerivedViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _initialFacetCuts(
                    address(loupeFacet), address(rawViewsFacet), address(lifecycleFacet), address(derivedViewsFacet)
                ),
                address(init),
                abi.encodeCall(ClanWorldDiamondInit.init, ())
            );

        console.log("CLAN_WORLD_DIAMOND_ADDRESS:       ", address(diamond));
        console.log("DIAMOND_CUT_FACET_ADDRESS:        ", address(cutFacet));
        console.log("DIAMOND_LOUPE_FACET_ADDRESS:      ", address(loupeFacet));
        console.log("CLAN_LIFECYCLE_FACET_ADDRESS:     ", address(lifecycleFacet));
        console.log("RAW_VIEWS_FACET_ADDRESS:          ", address(rawViewsFacet));
        console.log("DERIVED_VIEWS_FACET_ADDRESS:      ", address(derivedViewsFacet));
        console.log("CLAN_WORLD_DIAMOND_INIT_ADDRESS:  ", address(init));

        vm.stopBroadcast();
    }

    function _initialFacetCuts(
        address loupeFacet,
        address rawViewsFacet,
        address lifecycleFacet,
        address derivedViewsFacet
    ) private pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](4);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: loupeFacet, action: IDiamondCut.FacetCutAction.Add, functionSelectors: _loupeSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: rawViewsFacet, action: IDiamondCut.FacetCutAction.Add, functionSelectors: _rawViewsSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: lifecycleFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: _lifecycleSelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: derivedViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: _derivedViewsSelectors()
        });
    }

    function _loupeSelectors() private pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](4);
        selectors[0] = IDiamondLoupe.facets.selector;
        selectors[1] = IDiamondLoupe.facetFunctionSelectors.selector;
        selectors[2] = IDiamondLoupe.facetAddresses.selector;
        selectors[3] = IDiamondLoupe.facetAddress.selector;
    }

    function _rawViewsSelectors() private pure returns (bytes4[] memory selectors) {
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

    function _lifecycleSelectors() private pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](1);
        selectors[0] = IClanWorld.mintClan.selector;
    }

    function _derivedViewsSelectors() private pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](2);
        selectors[0] = IClanWorld.getDerivedClanState.selector;
        selectors[1] = IClanWorld.getDerivedClansmanState.selector;
    }
}
