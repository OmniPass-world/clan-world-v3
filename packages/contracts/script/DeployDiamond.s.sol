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
import {BlueprintTransferFacet} from "../src/diamond/facets/BlueprintTransferFacet.sol";
import {BundleTransferFacet} from "../src/diamond/facets/BundleTransferFacet.sol";
import {ClanFullViewFacet} from "../src/diamond/facets/ClanFullViewFacet.sol";
import {ClanLifecycleFacet} from "../src/diamond/facets/ClanLifecycleFacet.sol";
import {ClanOwnershipFacet} from "../src/diamond/facets/ClanOwnershipFacet.sol";
import {DerivedViewsFacet} from "../src/diamond/facets/DerivedViewsFacet.sol";
import {FinalizeSeasonFacet} from "../src/diamond/facets/FinalizeSeasonFacet.sol";
import {GoldTransferFacet} from "../src/diamond/facets/GoldTransferFacet.sol";
import {HeartbeatFacet} from "../src/diamond/facets/HeartbeatFacet.sol";
import {MarketViewsFacet} from "../src/diamond/facets/MarketViewsFacet.sol";
import {QuoteViewsFacet} from "../src/diamond/facets/QuoteViewsFacet.sol";
import {RawBanditViewsFacet} from "../src/diamond/facets/RawBanditViewsFacet.sol";
import {RawClanViewsFacet} from "../src/diamond/facets/RawClanViewsFacet.sol";
import {RawTreasuryViewsFacet} from "../src/diamond/facets/RawTreasuryViewsFacet.sol";
import {RawWorldViewsFacet} from "../src/diamond/facets/RawWorldViewsFacet.sol";
import {RegionViewsFacet} from "../src/diamond/facets/RegionViewsFacet.sol";
import {ScoringViewsFacet} from "../src/diamond/facets/ScoringViewsFacet.sol";
import {SettlementFacet} from "../src/diamond/facets/SettlementFacet.sol";
import {SnapshotViewsFacet} from "../src/diamond/facets/SnapshotViewsFacet.sol";
import {SubmitOrdersFacet} from "../src/diamond/facets/SubmitOrdersFacet.sol";
import {TreasuryFacet} from "../src/diamond/facets/TreasuryFacet.sol";
import {VaultResourceTransferFacet} from "../src/diamond/facets/VaultResourceTransferFacet.sol";

contract DeployDiamond is Script {
    function run() external {
        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
        address owner = vm.addr(deployerPrivateKey);

        vm.startBroadcast(deployerPrivateKey);

        DiamondCutFacet cutFacet = new DiamondCutFacet();
        Diamond diamond = new Diamond(owner, address(cutFacet));
        DiamondLoupeFacet loupeFacet = new DiamondLoupeFacet();
        HeartbeatFacet heartbeatFacet = new HeartbeatFacet();
        FinalizeSeasonFacet finalizeSeasonFacet = new FinalizeSeasonFacet();
        RawWorldViewsFacet rawWorldViewsFacet = new RawWorldViewsFacet();
        RawTreasuryViewsFacet rawTreasuryViewsFacet = new RawTreasuryViewsFacet();
        RawClanViewsFacet rawClanViewsFacet = new RawClanViewsFacet();
        RawBanditViewsFacet rawBanditViewsFacet = new RawBanditViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        SubmitOrdersFacet submitOrdersFacet = new SubmitOrdersFacet();
        ClanOwnershipFacet ownershipFacet = new ClanOwnershipFacet();
        TreasuryFacet treasuryFacet = new TreasuryFacet();
        SettlementFacet settlementFacet = new SettlementFacet();
        GoldTransferFacet goldTransferFacet = new GoldTransferFacet();
        VaultResourceTransferFacet vaultResourceTransferFacet = new VaultResourceTransferFacet();
        BlueprintTransferFacet blueprintTransferFacet = new BlueprintTransferFacet();
        BundleTransferFacet bundleTransferFacet = new BundleTransferFacet();
        DerivedViewsFacet derivedViewsFacet = new DerivedViewsFacet();
        MarketViewsFacet marketViewsFacet = new MarketViewsFacet();
        BanditViewsFacet banditViewsFacet = new BanditViewsFacet();
        RegionViewsFacet regionViewsFacet = new RegionViewsFacet();
        SnapshotViewsFacet snapshotViewsFacet = new SnapshotViewsFacet();
        ClanFullViewFacet clanFullViewFacet = new ClanFullViewFacet();
        QuoteViewsFacet quoteViewsFacet = new QuoteViewsFacet();
        ScoringViewsFacet scoringViewsFacet = new ScoringViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _initialFacetCuts(
                    address(loupeFacet),
                    address(heartbeatFacet),
                    address(finalizeSeasonFacet),
                    address(rawWorldViewsFacet),
                    address(rawTreasuryViewsFacet),
                    address(rawClanViewsFacet),
                    address(rawBanditViewsFacet),
                    address(lifecycleFacet),
                    address(submitOrdersFacet),
                    address(ownershipFacet),
                    address(treasuryFacet),
                    address(settlementFacet),
                    address(goldTransferFacet),
                    address(vaultResourceTransferFacet),
                    address(blueprintTransferFacet),
                    address(bundleTransferFacet),
                    address(derivedViewsFacet),
                    address(marketViewsFacet),
                    address(banditViewsFacet),
                    address(regionViewsFacet),
                    address(snapshotViewsFacet),
                    address(clanFullViewFacet),
                    address(quoteViewsFacet),
                    address(scoringViewsFacet)
                ),
                address(init),
                abi.encodeCall(ClanWorldDiamondInit.init, ())
            );

        console.log("CLAN_WORLD_DIAMOND_ADDRESS:       ", address(diamond));
        console.log("DIAMOND_CUT_FACET_ADDRESS:        ", address(cutFacet));
        console.log("DIAMOND_LOUPE_FACET_ADDRESS:      ", address(loupeFacet));
        console.log("HEARTBEAT_FACET_ADDRESS:          ", address(heartbeatFacet));
        console.log("FINALIZE_SEASON_FACET_ADDRESS:    ", address(finalizeSeasonFacet));
        console.log("CLAN_LIFECYCLE_FACET_ADDRESS:     ", address(lifecycleFacet));
        console.log("RAW_WORLD_VIEWS_FACET_ADDRESS:    ", address(rawWorldViewsFacet));
        console.log("RAW_TREASURY_VIEWS_FACET_ADDRESS: ", address(rawTreasuryViewsFacet));
        console.log("RAW_CLAN_VIEWS_FACET_ADDRESS:     ", address(rawClanViewsFacet));
        console.log("RAW_BANDIT_VIEWS_FACET_ADDRESS:   ", address(rawBanditViewsFacet));
        console.log("SUBMIT_ORDERS_FACET_ADDRESS:      ", address(submitOrdersFacet));
        console.log("CLAN_OWNERSHIP_FACET_ADDRESS:     ", address(ownershipFacet));
        console.log("DERIVED_VIEWS_FACET_ADDRESS:      ", address(derivedViewsFacet));
        console.log("TREASURY_FACET_ADDRESS:           ", address(treasuryFacet));
        console.log("SETTLEMENT_FACET_ADDRESS:         ", address(settlementFacet));
        console.log("GOLD_TRANSFER_FACET_ADDRESS:      ", address(goldTransferFacet));
        console.log("VAULT_RESOURCE_TRANSFER_FACET:    ", address(vaultResourceTransferFacet));
        console.log("BLUEPRINT_TRANSFER_FACET_ADDRESS: ", address(blueprintTransferFacet));
        console.log("BUNDLE_TRANSFER_FACET_ADDRESS:    ", address(bundleTransferFacet));
        console.log("MARKET_VIEWS_FACET_ADDRESS:       ", address(marketViewsFacet));
        console.log("BANDIT_VIEWS_FACET_ADDRESS:       ", address(banditViewsFacet));
        console.log("REGION_VIEWS_FACET_ADDRESS:       ", address(regionViewsFacet));
        console.log("SNAPSHOT_VIEWS_FACET_ADDRESS:     ", address(snapshotViewsFacet));
        console.log("CLAN_FULL_VIEW_FACET_ADDRESS:     ", address(clanFullViewFacet));
        console.log("QUOTE_VIEWS_FACET_ADDRESS:        ", address(quoteViewsFacet));
        console.log("SCORING_VIEWS_FACET_ADDRESS:      ", address(scoringViewsFacet));
        console.log("CLAN_WORLD_DIAMOND_INIT_ADDRESS:  ", address(init));

        vm.stopBroadcast();
    }

    function _initialFacetCuts(
        address loupeFacet,
        address heartbeatFacet,
        address finalizeSeasonFacet,
        address rawWorldViewsFacet,
        address rawTreasuryViewsFacet,
        address rawClanViewsFacet,
        address rawBanditViewsFacet,
        address lifecycleFacet,
        address submitOrdersFacet,
        address ownershipFacet,
        address treasuryFacet,
        address settlementFacet,
        address goldTransferFacet,
        address vaultResourceTransferFacet,
        address blueprintTransferFacet,
        address bundleTransferFacet,
        address derivedViewsFacet,
        address marketViewsFacet,
        address banditViewsFacet,
        address regionViewsFacet,
        address snapshotViewsFacet,
        address clanFullViewFacet,
        address quoteViewsFacet,
        address scoringViewsFacet
    ) private pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](24);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: loupeFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.loupeSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: heartbeatFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.heartbeatSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: finalizeSeasonFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.seasonSelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: rawWorldViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawWorldViewsSelectors()
        });
        cut[4] = IDiamondCut.FacetCut({
            facetAddress: rawTreasuryViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawTreasuryViewsSelectors()
        });
        cut[5] = IDiamondCut.FacetCut({
            facetAddress: rawClanViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawClanViewsSelectors()
        });
        cut[6] = IDiamondCut.FacetCut({
            facetAddress: rawBanditViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawBanditViewsSelectors()
        });
        cut[7] = IDiamondCut.FacetCut({
            facetAddress: lifecycleFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.lifecycleSelectors()
        });
        cut[8] = IDiamondCut.FacetCut({
            facetAddress: submitOrdersFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.submitOrdersSelectors()
        });
        cut[9] = IDiamondCut.FacetCut({
            facetAddress: ownershipFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.ownershipSelectors()
        });
        cut[10] = IDiamondCut.FacetCut({
            facetAddress: treasuryFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.treasurySelectors()
        });
        cut[11] = IDiamondCut.FacetCut({
            facetAddress: settlementFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.settlementSelectors()
        });
        cut[12] = IDiamondCut.FacetCut({
            facetAddress: goldTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.goldTransferSelectors()
        });
        cut[13] = IDiamondCut.FacetCut({
            facetAddress: vaultResourceTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.vaultResourceTransferSelectors()
        });
        cut[14] = IDiamondCut.FacetCut({
            facetAddress: blueprintTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.blueprintTransferSelectors()
        });
        cut[15] = IDiamondCut.FacetCut({
            facetAddress: bundleTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.bundleTransferSelectors()
        });
        cut[16] = IDiamondCut.FacetCut({
            facetAddress: derivedViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.derivedViewsSelectors()
        });
        cut[17] = IDiamondCut.FacetCut({
            facetAddress: marketViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.marketViewsSelectors()
        });
        cut[18] = IDiamondCut.FacetCut({
            facetAddress: banditViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.banditViewsSelectors()
        });
        cut[19] = IDiamondCut.FacetCut({
            facetAddress: regionViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.regionViewsSelectors()
        });
        cut[20] = IDiamondCut.FacetCut({
            facetAddress: snapshotViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.snapshotViewsSelectors()
        });
        cut[21] = IDiamondCut.FacetCut({
            facetAddress: clanFullViewFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.clanFullViewSelectors()
        });
        cut[22] = IDiamondCut.FacetCut({
            facetAddress: quoteViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.quoteViewsSelectors()
        });
        cut[23] = IDiamondCut.FacetCut({
            facetAddress: scoringViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.scoringViewsSelectors()
        });
    }
}
