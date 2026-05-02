// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {DiamondSelectors} from "../../script/DiamondSelectors.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {ClanWorld} from "../../src/ClanWorld.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {
    IClanWorld,
    ActiveBanditView,
    ActionType,
    Clan,
    Clansman,
    DerivedClanState,
    DerivedClansmanState,
    MarketState,
    RegionOccupant,
    WheatPlot,
    WorldSnapshot,
    WorldState
} from "../../src/IClanWorld.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {IDiamondLoupe} from "../../src/diamond/IDiamondLoupe.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {BanditViewsFacet} from "../../src/diamond/facets/BanditViewsFacet.sol";
import {ClanLifecycleFacet} from "../../src/diamond/facets/ClanLifecycleFacet.sol";
import {DerivedViewsFacet} from "../../src/diamond/facets/DerivedViewsFacet.sol";
import {DiamondLoupeFacet} from "../../src/diamond/facets/DiamondLoupeFacet.sol";
import {MarketViewsFacet} from "../../src/diamond/facets/MarketViewsFacet.sol";
import {RawViewsFacet} from "../../src/diamond/facets/RawViewsFacet.sol";
import {RegionViewsFacet} from "../../src/diamond/facets/RegionViewsFacet.sol";
import {SnapshotViewsFacet} from "../../src/diamond/facets/SnapshotViewsFacet.sol";

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
        assertEq(diamondWorld.getResourceToken(0), core.getResourceToken(0));
        assertEq(diamondWorld.getPool(0), core.getPool(0));

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

    function testDiamondMintClanMatchesCoreInitialState() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );
        IDiamondCut(address(diamond)).diamondCut(_lifecycleCut(address(lifecycleFacet)), address(0), "");

        address elder = address(0xA11CE);
        vm.prank(elder);
        (uint32 coreClanId, uint256 coreIftTokenId) = core.mintClan(elder);
        vm.prank(elder);
        (uint32 diamondClanId, uint256 diamondIftTokenId) = IClanWorld(address(diamond)).mintClan(elder);

        assertEq(diamondClanId, coreClanId);
        assertEq(diamondIftTokenId, coreIftTokenId);
        _assertClanEq(IClanWorld(address(diamond)).getClan(diamondClanId), core.getClan(coreClanId));
        assertEq(IClanWorld(address(diamond)).getClanIds().length, core.getClanIds().length);
        assertEq(IClanWorld(address(diamond)).getActiveDefenders(diamondClanId).length, 0);

        uint32[] memory diamondClansmen = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId);
        uint32[] memory coreClansmen = core.getClanClansmanIds(coreClanId);
        assertEq(diamondClansmen.length, coreClansmen.length);
        for (uint256 i = 0; i < coreClansmen.length; i++) {
            assertEq(diamondClansmen[i], coreClansmen[i]);
            _assertClansmanEq(
                IClanWorld(address(diamond)).getClansman(diamondClansmen[i]), core.getClansman(coreClansmen[i])
            );
        }

        (WheatPlot memory diamondWest, WheatPlot memory diamondEast) =
            IClanWorld(address(diamond)).getWheatPlots(diamondClanId);
        (WheatPlot memory coreWest, WheatPlot memory coreEast) = core.getWheatPlots(coreClanId);
        _assertWheatPlotEq(diamondWest, coreWest);
        _assertWheatPlotEq(diamondEast, coreEast);
    }

    function testDiamondBasicDerivedViewsMatchCoreAfterMint() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        DerivedViewsFacet derivedViewsFacet = new DerivedViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );
        IDiamondCut(address(diamond)).diamondCut(_lifecycleCut(address(lifecycleFacet)), address(0), "");
        IDiamondCut(address(diamond)).diamondCut(_derivedViewsCut(address(derivedViewsFacet)), address(0), "");

        address elder = address(0xA11CE);
        vm.prank(elder);
        (uint32 coreClanId,) = core.mintClan(elder);
        vm.prank(elder);
        (uint32 diamondClanId,) = IClanWorld(address(diamond)).mintClan(elder);

        DerivedClanState memory diamondClan = IClanWorld(address(diamond)).getDerivedClanState(diamondClanId);
        DerivedClanState memory coreClan = core.getDerivedClanState(coreClanId);
        _assertDerivedClanStateEq(diamondClan, coreClan);

        uint32 clansmanId = core.getClanClansmanIds(coreClanId)[0];
        DerivedClansmanState memory diamondClansman = IClanWorld(address(diamond)).getDerivedClansmanState(clansmanId);
        DerivedClansmanState memory coreClansman = core.getDerivedClansmanState(clansmanId);
        _assertDerivedClansmanStateEq(diamondClansman, coreClansman);

        DerivedClansmanState memory missing = IClanWorld(address(diamond)).getDerivedClansmanState(999);
        assertEq(missing.effectiveRegion, 0);
        assertEq(missing.derivedAtTick, core.getWorldState().currentTick);
    }

    function testDiamondMarketStateMatchesCoreDefaults() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        MarketViewsFacet marketViewsFacet = new MarketViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );
        IDiamondCut(address(diamond)).diamondCut(_marketViewsCut(address(marketViewsFacet)), address(0), "");

        _assertMarketStateEq(IClanWorld(address(diamond)).getMarketState(), core.getMarketState());
    }

    function testDiamondActiveBanditViewMatchesCoreDefault() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        BanditViewsFacet banditViewsFacet = new BanditViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );
        IDiamondCut(address(diamond)).diamondCut(_banditViewsCut(address(banditViewsFacet)), address(0), "");

        _assertActiveBanditViewEq(IClanWorld(address(diamond)).getActiveBanditView(), core.getActiveBanditView());
    }

    function testDiamondRegionPopulationMatchesCoreAfterMint() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        RegionViewsFacet regionViewsFacet = new RegionViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );
        IDiamondCut(address(diamond)).diamondCut(_lifecycleCut(address(lifecycleFacet)), address(0), "");
        IDiamondCut(address(diamond)).diamondCut(_regionViewsCut(address(regionViewsFacet)), address(0), "");

        address elder = address(0xA11CE);
        vm.prank(elder);
        (uint32 coreClanId,) = core.mintClan(elder);
        vm.prank(elder);
        (uint32 diamondClanId,) = IClanWorld(address(diamond)).mintClan(elder);

        uint8 baseRegion = core.getClan(coreClanId).baseRegion;
        assertEq(baseRegion, IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion);
        _assertRegionPopulationEq(
            IClanWorld(address(diamond)).getRegionPopulation(baseRegion), core.getRegionPopulation(baseRegion)
        );
    }

    function testDiamondWorldSnapshotMatchesCoreAfterMint() public {
        ClanWorld core = new ClanWorld();
        RawViewsFacet rawViewsFacet = new RawViewsFacet();
        ClanLifecycleFacet lifecycleFacet = new ClanLifecycleFacet();
        SnapshotViewsFacet snapshotViewsFacet = new SnapshotViewsFacet();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond))
            .diamondCut(
                _rawViewsCut(address(rawViewsFacet)), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ())
            );
        IDiamondCut(address(diamond)).diamondCut(_lifecycleCut(address(lifecycleFacet)), address(0), "");
        IDiamondCut(address(diamond)).diamondCut(_snapshotViewsCut(address(snapshotViewsFacet)), address(0), "");

        address elder = address(0xA11CE);
        vm.prank(elder);
        core.mintClan(elder);
        vm.prank(elder);
        IClanWorld(address(diamond)).mintClan(elder);

        _assertWorldSnapshotEq(IClanWorld(address(diamond)).getWorldSnapshot(), core.getWorldSnapshot());
    }

    function _rawViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawViewsSelectors()
        });
    }

    function _lifecycleCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.lifecycleSelectors()
        });
    }

    function _derivedViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.derivedViewsSelectors()
        });
    }

    function _marketViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.marketViewsSelectors()
        });
    }

    function _banditViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.banditViewsSelectors()
        });
    }

    function _regionViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.regionViewsSelectors()
        });
    }

    function _snapshotViewsCut(address facet) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.snapshotViewsSelectors()
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

    function _assertClanEq(Clan memory actual, Clan memory expected) internal pure {
        assertEq(actual.clanId, expected.clanId, "clanId");
        assertEq(actual.iftTokenId, expected.iftTokenId, "iftTokenId");
        assertEq(actual.owner, expected.owner, "owner");
        assertEq(uint8(actual.clanState), uint8(expected.clanState), "clanState");
        assertEq(actual.baseRegion, expected.baseRegion, "baseRegion");
        assertEq(actual.baseLevel, expected.baseLevel, "baseLevel");
        assertEq(actual.wallLevel, expected.wallLevel, "wallLevel");
        assertEq(actual.monumentLevel, expected.monumentLevel, "monumentLevel");
        assertEq(actual.livingClansmen, expected.livingClansmen, "livingClansmen");
        assertEq(actual.lastSettledTick, expected.lastSettledTick, "lastSettledTick");
        assertEq(actual.starvationStartsAtTick, expected.starvationStartsAtTick, "starvationStartsAtTick");
        assertEq(actual.coldDamage, expected.coldDamage, "coldDamage");
        assertEq(actual.ownerNonce, expected.ownerNonce, "ownerNonce");
        assertEq(actual.goldBalance, expected.goldBalance, "goldBalance");
        assertEq(actual.blueprintBalance, expected.blueprintBalance, "blueprintBalance");
        assertEq(actual.vaultWood, expected.vaultWood, "vaultWood");
        assertEq(actual.vaultIron, expected.vaultIron, "vaultIron");
        assertEq(actual.vaultWheat, expected.vaultWheat, "vaultWheat");
        assertEq(actual.vaultFish, expected.vaultFish, "vaultFish");
    }

    function _assertClansmanEq(Clansman memory actual, Clansman memory expected) internal pure {
        assertEq(actual.clansmanId, expected.clansmanId, "clansmanId");
        assertEq(actual.clanId, expected.clanId, "clansman clanId");
        assertEq(uint8(actual.state), uint8(expected.state), "clansman state");
        assertEq(actual.currentRegion, expected.currentRegion, "currentRegion");
        assertEq(actual.cooldownEndsAtTs, expected.cooldownEndsAtTs, "cooldownEndsAtTs");
        assertEq(actual.lastMissionNonce, expected.lastMissionNonce, "lastMissionNonce");
        assertEq(actual.carryWood, expected.carryWood, "carryWood");
        assertEq(actual.carryIron, expected.carryIron, "carryIron");
        assertEq(actual.carryWheat, expected.carryWheat, "carryWheat");
        assertEq(actual.carryFish, expected.carryFish, "carryFish");
    }

    function _assertWheatPlotEq(WheatPlot memory actual, WheatPlot memory expected) internal pure {
        assertEq(uint8(actual.state), uint8(expected.state), "plot state");
        assertEq(actual.region, expected.region, "plot region");
        assertEq(actual.remainingWheat, expected.remainingWheat, "remainingWheat");
        assertEq(actual.regrowUntilTick, expected.regrowUntilTick, "regrowUntilTick");
    }

    function _assertDerivedClanStateEq(DerivedClanState memory actual, DerivedClanState memory expected) internal pure {
        _assertClanEq(actual.clan, expected.clan);
        assertEq(actual.isStarving, expected.isStarving, "isStarving");
        assertEq(actual.lootValue, expected.lootValue, "lootValue");
        assertEq(actual.derivedAtTick, expected.derivedAtTick, "derivedAtTick");
    }

    function _assertDerivedClansmanStateEq(DerivedClansmanState memory actual, DerivedClansmanState memory expected)
        internal
        pure
    {
        _assertClansmanEq(actual.clansman, expected.clansman);
        assertEq(actual.effectiveRegion, expected.effectiveRegion, "effectiveRegion");
        assertEq(actual.derivedAtTick, expected.derivedAtTick, "derived clansman tick");
        assertEq(actual.activeMission.active, expected.activeMission.active, "mission active");
        assertEq(actual.activeMission.nonce, expected.activeMission.nonce, "mission nonce");
    }

    function _assertMarketStateEq(MarketState memory actual, MarketState memory expected) internal pure {
        assertEq(actual.wood.resourceToken, expected.wood.resourceToken, "wood token");
        assertEq(actual.wheat.resourceToken, expected.wheat.resourceToken, "wheat token");
        assertEq(actual.fish.resourceToken, expected.fish.resourceToken, "fish token");
        assertEq(actual.iron.resourceToken, expected.iron.resourceToken, "iron token");
        assertEq(actual.currentTick, expected.currentTick, "market tick");
        assertEq(actual.currentTickQueue.length, expected.currentTickQueue.length, "current queue");
        assertEq(actual.nextTickQueue.length, expected.nextTickQueue.length, "next queue");
    }

    function _assertActiveBanditViewEq(ActiveBanditView memory actual, ActiveBanditView memory expected) internal pure {
        assertEq(actual.exists, expected.exists, "bandit exists");
        assertEq(actual.banditId, expected.banditId, "bandit id");
        assertEq(uint8(actual.state), uint8(expected.state), "bandit state");
        assertEq(actual.currentRegion, expected.currentRegion, "bandit region");
        assertEq(actual.attackAttemptsMade, expected.attackAttemptsMade, "bandit attempts");
        assertEq(actual.maxAttemptsRemaining, expected.maxAttemptsRemaining, "bandit remaining");
        assertEq(actual.stateEnteredTick, expected.stateEnteredTick, "bandit entered");
        assertEq(actual.nextActionTick, expected.nextActionTick, "bandit next");
        assertEq(actual.tier, expected.tier, "bandit tier");
        assertEq(actual.attackPower, expected.attackPower, "bandit power");
        assertEq(actual.carryWood, expected.carryWood, "bandit wood");
        assertEq(actual.carryIron, expected.carryIron, "bandit iron");
        assertEq(actual.carryWheat, expected.carryWheat, "bandit wheat");
        assertEq(actual.carryFish, expected.carryFish, "bandit fish");
        assertEq(actual.projectedTargetClanId, expected.projectedTargetClanId, "bandit target");
        assertEq(actual.projectedTargetLootValue, expected.projectedTargetLootValue, "bandit loot");
    }

    function _assertRegionPopulationEq(RegionOccupant[] memory actual, RegionOccupant[] memory expected) internal pure {
        assertEq(actual.length, expected.length, "population length");
        for (uint256 i = 0; i < expected.length; i++) {
            assertEq(actual[i].clansmanId, expected[i].clansmanId, "population clansman");
            assertEq(actual[i].clanId, expected[i].clanId, "population clan");
            assertEq(uint8(actual[i].state), uint8(expected[i].state), "population state");
            assertEq(uint8(actual[i].currentAction), uint8(expected[i].currentAction), "population action");
            assertEq(actual[i].missionNonce, expected[i].missionNonce, "population nonce");
        }
    }

    function _assertWorldSnapshotEq(WorldSnapshot memory actual, WorldSnapshot memory expected) internal pure {
        assertEq(actual.currentTick, expected.currentTick, "snapshot currentTick");
        assertEq(actual.seasonStartTick, expected.seasonStartTick, "snapshot seasonStartTick");
        assertEq(actual.seasonEndTick, expected.seasonEndTick, "snapshot seasonEndTick");
        assertEq(actual.seasonFinalized, expected.seasonFinalized, "snapshot seasonFinalized");
        assertEq(actual.currentSeasonNumber, expected.currentSeasonNumber, "snapshot season");
        assertEq(actual.nextHeartbeatAtTick, expected.nextHeartbeatAtTick, "snapshot next heartbeat");
        assertEq(actual.winterActive, expected.winterActive, "snapshot winterActive");
        assertEq(actual.winterStartsAtTick, expected.winterStartsAtTick, "snapshot winter start");
        assertEq(actual.winterEndsAtTick, expected.winterEndsAtTick, "snapshot winter end");
        assertEq(actual.activeBanditId, expected.activeBanditId, "snapshot active bandit");
        assertEq(actual.currentTickSeed, expected.currentTickSeed, "snapshot seed");
        assertEq(actual.leaderboard.length, expected.leaderboard.length, "leaderboard length");
        for (uint256 i = 0; i < expected.leaderboard.length; i++) {
            assertEq(actual.leaderboard[i].clanId, expected.leaderboard[i].clanId, "leaderboard clanId");
            assertEq(actual.leaderboard[i].owner, expected.leaderboard[i].owner, "leaderboard owner");
            assertEq(actual.leaderboard[i].monumentLevel, expected.leaderboard[i].monumentLevel, "leaderboard monument");
            assertEq(actual.leaderboard[i].baseLevel, expected.leaderboard[i].baseLevel, "leaderboard base");
            assertEq(actual.leaderboard[i].wallLevel, expected.leaderboard[i].wallLevel, "leaderboard wall");
            assertEq(actual.leaderboard[i].livingClansmen, expected.leaderboard[i].livingClansmen, "leaderboard living");
            assertEq(uint8(actual.leaderboard[i].state), uint8(expected.leaderboard[i].state), "leaderboard state");
            assertEq(actual.leaderboard[i].lootValue, expected.leaderboard[i].lootValue, "leaderboard loot");
        }
    }
}
