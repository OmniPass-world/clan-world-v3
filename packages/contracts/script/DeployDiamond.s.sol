// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Script, console} from "forge-std/Script.sol";
import {DiamondSelectors} from "./DiamondSelectors.sol";
import {Diamond} from "../src/diamond/Diamond.sol";
import {ClanWorldDiamondInit} from "../src/diamond/ClanWorldDiamondInit.sol";
import {IDiamondCut} from "../src/diamond/IDiamondCut.sol";
import {DiamondCutFacet} from "../src/diamond/facets/DiamondCutFacet.sol";
import {DiamondLoupeFacet} from "../src/diamond/facets/DiamondLoupeFacet.sol";
import {BanditViewsFacet} from "../src/diamond/facets/BanditViewsFacet.sol";
import {ClanLifecycleFacet} from "../src/diamond/facets/ClanLifecycleFacet.sol";
import {DerivedViewsFacet} from "../src/diamond/facets/DerivedViewsFacet.sol";
import {MarketViewsFacet} from "../src/diamond/facets/MarketViewsFacet.sol";
import {RawViewsFacet} from "../src/diamond/facets/RawViewsFacet.sol";
import {RegionViewsFacet} from "../src/diamond/facets/RegionViewsFacet.sol";
import {SnapshotViewsFacet} from "../src/diamond/facets/SnapshotViewsFacet.sol";

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
        MarketViewsFacet marketViewsFacet = new MarketViewsFacet();
        BanditViewsFacet banditViewsFacet = new BanditViewsFacet();
        RegionViewsFacet regionViewsFacet = new RegionViewsFacet();
        SnapshotViewsFacet snapshotViewsFacet = new SnapshotViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _initialFacetCuts(
                    address(loupeFacet),
                    address(rawViewsFacet),
                    address(lifecycleFacet),
                    address(derivedViewsFacet),
                    address(marketViewsFacet),
                    address(banditViewsFacet),
                    address(regionViewsFacet),
                    address(snapshotViewsFacet)
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
        console.log("MARKET_VIEWS_FACET_ADDRESS:       ", address(marketViewsFacet));
        console.log("BANDIT_VIEWS_FACET_ADDRESS:       ", address(banditViewsFacet));
        console.log("REGION_VIEWS_FACET_ADDRESS:       ", address(regionViewsFacet));
        console.log("SNAPSHOT_VIEWS_FACET_ADDRESS:     ", address(snapshotViewsFacet));
        console.log("CLAN_WORLD_DIAMOND_INIT_ADDRESS:  ", address(init));

        vm.stopBroadcast();
    }

    function _initialFacetCuts(
        address loupeFacet,
        address rawViewsFacet,
        address lifecycleFacet,
        address derivedViewsFacet,
        address marketViewsFacet,
        address banditViewsFacet,
        address regionViewsFacet,
        address snapshotViewsFacet
    ) private pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](8);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: loupeFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.loupeSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: rawViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawViewsSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: lifecycleFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.lifecycleSelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: derivedViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.derivedViewsSelectors()
        });
        cut[4] = IDiamondCut.FacetCut({
            facetAddress: marketViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.marketViewsSelectors()
        });
        cut[5] = IDiamondCut.FacetCut({
            facetAddress: banditViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.banditViewsSelectors()
        });
        cut[6] = IDiamondCut.FacetCut({
            facetAddress: regionViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.regionViewsSelectors()
        });
        cut[7] = IDiamondCut.FacetCut({
            facetAddress: snapshotViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.snapshotViewsSelectors()
        });
    }
}
