// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Script, console} from "forge-std/Script.sol";
import {DiamondSelectors} from "./DiamondSelectors.sol";
import {ClanWorldLens} from "../src/ClanWorldLens.sol";
import {IClanWorld, PoolSeedConfig} from "../src/IClanWorld.sol";
import {MinimalERC20} from "../src/MinimalERC20.sol";
import {StubPool} from "../src/StubPool.sol";
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
import {HeartbeatConfigFacet} from "../src/diamond/facets/HeartbeatConfigFacet.sol";
import {HeartbeatFacet} from "../src/diamond/facets/HeartbeatFacet.sol";
import {MarketViewsFacet} from "../src/diamond/facets/MarketViewsFacet.sol";
import {OwnershipFacet} from "../src/diamond/facets/OwnershipFacet.sol";
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
import {WorldPauseFacet} from "../src/diamond/facets/WorldPauseFacet.sol";

contract DeployDiamond is Script {
    uint256 private constant INITIAL_WOOD_POOL_SEED = 1_000e18;
    uint256 private constant INITIAL_WHEAT_POOL_SEED = 1_000e18;
    uint256 private constant INITIAL_FISH_POOL_SEED = 500e18;
    uint256 private constant INITIAL_IRON_POOL_SEED = 250e18;
    uint256 private constant INITIAL_GOLD_SEED_FOR_WOOD = 500e18;
    uint256 private constant INITIAL_GOLD_SEED_FOR_WHEAT = 700e18;
    uint256 private constant INITIAL_GOLD_SEED_FOR_FISH = 600e18;
    uint256 private constant INITIAL_GOLD_SEED_FOR_IRON = 800e18;

    function run() external {
        uint256 deployerPrivateKey = vm.envUint("DEPLOYER_PRIVATE_KEY");
        address owner = vm.addr(deployerPrivateKey);

        vm.startBroadcast(deployerPrivateKey);

        DiamondCutFacet cutFacet = new DiamondCutFacet();
        Diamond diamond = new Diamond(owner, address(cutFacet));
        DiamondLoupeFacet loupeFacet = new DiamondLoupeFacet();
        OwnershipFacet adminOwnershipFacet = new OwnershipFacet();
        HeartbeatFacet heartbeatFacet = new HeartbeatFacet();
        HeartbeatConfigFacet heartbeatConfigFacet = new HeartbeatConfigFacet();
        WorldPauseFacet worldPauseFacet = new WorldPauseFacet();
        FinalizeSeasonFacet finalizeSeasonFacet = new FinalizeSeasonFacet();
        RawWorldViewsFacet rawWorldViewsFacet = new RawWorldViewsFacet();
        RawTreasuryViewsFacet rawTreasuryViewsFacet = new RawTreasuryViewsFacet();
        RawClanViewsFacet rawClanViewsFacet = new RawClanViewsFacet();
        RawBanditViewsFacet rawBanditViewsFacet = new RawBanditViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        SubmitOrdersFacet submitOrdersFacet = new SubmitOrdersFacet();
        ClanOwnershipFacet clanOwnershipFacet = new ClanOwnershipFacet();
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
                _coreFacetCuts(
                    address(loupeFacet),
                    address(adminOwnershipFacet),
                    address(heartbeatFacet),
                    address(heartbeatConfigFacet),
                    address(worldPauseFacet),
                    address(finalizeSeasonFacet),
                    address(rawWorldViewsFacet),
                    address(rawTreasuryViewsFacet),
                    address(rawClanViewsFacet),
                    address(rawBanditViewsFacet),
                    address(lifecycleFacet)
                ),
                address(0),
                ""
            );
        IDiamondCut(address(diamond))
            .diamondCut(
                _orderFacetCuts(
                    address(submitOrdersFacet),
                    address(clanOwnershipFacet),
                    address(treasuryFacet),
                    address(settlementFacet),
                    address(goldTransferFacet),
                    address(vaultResourceTransferFacet),
                    address(blueprintTransferFacet),
                    address(bundleTransferFacet)
                ),
                address(0),
                ""
            );
        IDiamondCut(address(diamond))
            .diamondCut(
                _viewFacetCuts(
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

        IClanWorld game = IClanWorld(address(diamond));
        ClanWorldLens lens = new ClanWorldLens(game);

        MinimalERC20 wood = new MinimalERC20("Wood", "WOOD");
        MinimalERC20 iron = new MinimalERC20("Iron", "IRON");
        MinimalERC20 wheat = new MinimalERC20("Wheat", "WHEAT");
        MinimalERC20 fish = new MinimalERC20("Fish", "FISH");
        MinimalERC20 gold = new MinimalERC20("Gold", "GOLD");
        MinimalERC20 blueprint = new MinimalERC20("ClanWorld Blueprint", "BPRT");

        StubPool woodGold = new StubPool(address(wood), address(gold), address(diamond));
        StubPool wheatGold = new StubPool(address(wheat), address(gold), address(diamond));
        StubPool fishGold = new StubPool(address(fish), address(gold), address(diamond));
        StubPool ironGold = new StubPool(address(iron), address(gold), address(diamond));

        address[6] memory tokens =
            [address(wood), address(iron), address(wheat), address(fish), address(gold), address(blueprint)];
        address[4] memory pools = [address(woodGold), address(wheatGold), address(fishGold), address(ironGold)];
        game.initTreasury(tokens, pools);

        uint256 totalGoldSeed = INITIAL_GOLD_SEED_FOR_WOOD + INITIAL_GOLD_SEED_FOR_WHEAT + INITIAL_GOLD_SEED_FOR_FISH
            + INITIAL_GOLD_SEED_FOR_IRON;
        wood.seedTreasury(owner, INITIAL_WOOD_POOL_SEED);
        wheat.seedTreasury(owner, INITIAL_WHEAT_POOL_SEED);
        fish.seedTreasury(owner, INITIAL_FISH_POOL_SEED);
        iron.seedTreasury(owner, INITIAL_IRON_POOL_SEED);
        gold.seedTreasury(owner, totalGoldSeed);

        wood.approve(address(diamond), INITIAL_WOOD_POOL_SEED);
        wheat.approve(address(diamond), INITIAL_WHEAT_POOL_SEED);
        fish.approve(address(diamond), INITIAL_FISH_POOL_SEED);
        iron.approve(address(diamond), INITIAL_IRON_POOL_SEED);
        gold.approve(address(diamond), totalGoldSeed);

        game.seedPools(
            PoolSeedConfig({
                woodSeed: INITIAL_WOOD_POOL_SEED,
                wheatSeed: INITIAL_WHEAT_POOL_SEED,
                fishSeed: INITIAL_FISH_POOL_SEED,
                ironSeed: INITIAL_IRON_POOL_SEED,
                goldSeedForWood: INITIAL_GOLD_SEED_FOR_WOOD,
                goldSeedForWheat: INITIAL_GOLD_SEED_FOR_WHEAT,
                goldSeedForFish: INITIAL_GOLD_SEED_FOR_FISH,
                goldSeedForIron: INITIAL_GOLD_SEED_FOR_IRON
            })
        );

        console.log("CLAN_WORLD_DIAMOND_ADDRESS:       ", address(diamond));
        console.log("CLAN_WORLD_LENS_ADDRESS:          ", address(lens));
        console.log("WOOD_TOKEN_ADDRESS:               ", address(wood));
        console.log("IRON_TOKEN_ADDRESS:               ", address(iron));
        console.log("WHEAT_TOKEN_ADDRESS:              ", address(wheat));
        console.log("FISH_TOKEN_ADDRESS:               ", address(fish));
        console.log("GOLD_TOKEN_ADDRESS:               ", address(gold));
        console.log("BLUEPRINT_TOKEN_ADDRESS:          ", address(blueprint));
        console.log("WOOD_GOLD_POOL_ADDRESS:           ", address(woodGold));
        console.log("WHEAT_GOLD_POOL_ADDRESS:          ", address(wheatGold));
        console.log("FISH_GOLD_POOL_ADDRESS:           ", address(fishGold));
        console.log("IRON_GOLD_POOL_ADDRESS:           ", address(ironGold));
        console.log("DIAMOND_CUT_FACET_ADDRESS:        ", address(cutFacet));
        console.log("DIAMOND_LOUPE_FACET_ADDRESS:      ", address(loupeFacet));
        console.log("OWNERSHIP_FACET_ADDRESS:          ", address(adminOwnershipFacet));
        console.log("HEARTBEAT_FACET_ADDRESS:          ", address(heartbeatFacet));
        console.log("HEARTBEAT_CONFIG_FACET_ADDRESS:   ", address(heartbeatConfigFacet));
        console.log("WORLD_PAUSE_FACET_ADDRESS:        ", address(worldPauseFacet));
        console.log("FINALIZE_SEASON_FACET_ADDRESS:    ", address(finalizeSeasonFacet));
        console.log("CLAN_LIFECYCLE_FACET_ADDRESS:     ", address(lifecycleFacet));
        console.log("RAW_WORLD_VIEWS_FACET_ADDRESS:    ", address(rawWorldViewsFacet));
        console.log("RAW_TREASURY_VIEWS_FACET_ADDRESS: ", address(rawTreasuryViewsFacet));
        console.log("RAW_CLAN_VIEWS_FACET_ADDRESS:     ", address(rawClanViewsFacet));
        console.log("RAW_BANDIT_VIEWS_FACET_ADDRESS:   ", address(rawBanditViewsFacet));
        console.log("SUBMIT_ORDERS_FACET_ADDRESS:      ", address(submitOrdersFacet));
        console.log("CLAN_OWNERSHIP_FACET_ADDRESS:     ", address(clanOwnershipFacet));
        console.log("DERIVED_VIEWS_FACET_ADDRESS:      ", address(derivedViewsFacet));
        console.log("TREASURY_FACET_ADDRESS:           ", address(treasuryFacet));
        console.log("SETTLEMENT_FACET_ADDRESS:         ", address(settlementFacet));
        console.log("GOLD_TRANSFER_FACET_ADDRESS:      ", address(goldTransferFacet));
        console.log("VAULT_RESOURCE_TRANSFER_FACET_ADDRESS:", address(vaultResourceTransferFacet));
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

    function _coreFacetCuts(
        address loupeFacet,
        address adminOwnershipFacet,
        address heartbeatFacet,
        address heartbeatConfigFacet,
        address worldPauseFacet,
        address finalizeSeasonFacet,
        address rawWorldViewsFacet,
        address rawTreasuryViewsFacet,
        address rawClanViewsFacet,
        address rawBanditViewsFacet,
        address lifecycleFacet
    ) private pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](11);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: loupeFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.loupeSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: adminOwnershipFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.ownershipFacetSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: heartbeatFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.heartbeatSelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: heartbeatConfigFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.heartbeatConfigSelectors()
        });
        cut[4] = IDiamondCut.FacetCut({
            facetAddress: worldPauseFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.worldPauseSelectors()
        });
        cut[5] = IDiamondCut.FacetCut({
            facetAddress: finalizeSeasonFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.seasonSelectors()
        });
        cut[6] = IDiamondCut.FacetCut({
            facetAddress: rawWorldViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawWorldViewsSelectors()
        });
        cut[7] = IDiamondCut.FacetCut({
            facetAddress: rawTreasuryViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawTreasuryViewsSelectors()
        });
        cut[8] = IDiamondCut.FacetCut({
            facetAddress: rawClanViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawClanViewsSelectors()
        });
        cut[9] = IDiamondCut.FacetCut({
            facetAddress: rawBanditViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawBanditViewsSelectors()
        });
        cut[10] = IDiamondCut.FacetCut({
            facetAddress: lifecycleFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.lifecycleSelectors()
        });
    }

    function _orderFacetCuts(
        address submitOrdersFacet,
        address clanOwnershipFacet,
        address treasuryFacet,
        address settlementFacet,
        address goldTransferFacet,
        address vaultResourceTransferFacet,
        address blueprintTransferFacet,
        address bundleTransferFacet
    ) private pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](8);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: submitOrdersFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.submitOrdersSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: clanOwnershipFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.ownershipSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: treasuryFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.treasurySelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: settlementFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.settlementSelectors()
        });
        cut[4] = IDiamondCut.FacetCut({
            facetAddress: goldTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.goldTransferSelectors()
        });
        cut[5] = IDiamondCut.FacetCut({
            facetAddress: vaultResourceTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.vaultResourceTransferSelectors()
        });
        cut[6] = IDiamondCut.FacetCut({
            facetAddress: blueprintTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.blueprintTransferSelectors()
        });
        cut[7] = IDiamondCut.FacetCut({
            facetAddress: bundleTransferFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.bundleTransferSelectors()
        });
    }

    function _viewFacetCuts(
        address derivedViewsFacet,
        address marketViewsFacet,
        address banditViewsFacet,
        address regionViewsFacet,
        address snapshotViewsFacet,
        address clanFullViewFacet,
        address quoteViewsFacet,
        address scoringViewsFacet
    ) private pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](8);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: derivedViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.derivedViewsSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: marketViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.marketViewsSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: banditViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.banditViewsSelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: regionViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.regionViewsSelectors()
        });
        cut[4] = IDiamondCut.FacetCut({
            facetAddress: snapshotViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.snapshotViewsSelectors()
        });
        cut[5] = IDiamondCut.FacetCut({
            facetAddress: clanFullViewFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.clanFullViewSelectors()
        });
        cut[6] = IDiamondCut.FacetCut({
            facetAddress: quoteViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.quoteViewsSelectors()
        });
        cut[7] = IDiamondCut.FacetCut({
            facetAddress: scoringViewsFacet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.scoringViewsSelectors()
        });
    }
}
