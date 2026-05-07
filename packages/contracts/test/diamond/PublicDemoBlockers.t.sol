// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Vm} from "forge-std/Vm.sol";
import {DiamondSelectors} from "../../script/DiamondSelectors.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {IDiamondLoupe} from "../../src/diamond/IDiamondLoupe.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {DiamondLoupeFacet} from "../../src/diamond/facets/DiamondLoupeFacet.sol";
import {OwnershipFacet} from "../../src/diamond/facets/OwnershipFacet.sol";
import {RawWorldViewsFacet} from "../../src/diamond/facets/RawWorldViewsFacet.sol";
import {RawTreasuryViewsFacet} from "../../src/diamond/facets/RawTreasuryViewsFacet.sol";
import {RawClanViewsFacet} from "../../src/diamond/facets/RawClanViewsFacet.sol";
import {TreasuryFacet} from "../../src/diamond/facets/TreasuryFacet.sol";
import {ClanLifecycleFacet} from "../../src/diamond/facets/ClanLifecycleFacet.sol";
import {HeartbeatFacet} from "../../src/diamond/facets/HeartbeatFacet.sol";
import {SettlementFacet} from "../../src/diamond/facets/SettlementFacet.sol";
import {SubmitOrdersFacet} from "../../src/diamond/facets/SubmitOrdersFacet.sol";
import {LibStorage} from "../../src/diamond/lib/LibStorage.sol";
import {MinimalERC20} from "../../src/MinimalERC20.sol";
import {StubPool} from "../../src/StubPool.sol";
import {
    ActionType,
    Clan,
    ClanOrder,
    ClanWorldConstants,
    IClanWorld,
    PoolSeedConfig,
    StatusCode,
    WithdrawResourcesData
} from "../../src/IClanWorld.sol";

interface IPublicDemoHarness {
    function setCurrentTick(uint64 tick) external;
    function setClanUpkeepState(
        uint32 clanId,
        uint64 lastSettledTick,
        uint256 vaultWood,
        uint256 vaultWheat,
        uint256 vaultFish,
        uint16 coldDamage
    ) external;
    function setClanWallLevel(uint32 clanId, uint8 wallLevel) external;
    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external;
    function wallReservation(uint32 clansmanId)
        external
        view
        returns (bool active, uint8 fromLevel, uint256 woodCost, uint256 ironCost);
    function pendingWallUpgrades(uint32 clanId) external view returns (uint8);
    function reservedWood(uint32 clanId) external view returns (uint256);
    function reservedIron(uint32 clanId) external view returns (uint256);
}

contract PublicDemoHarnessFacet {
    function setCurrentTick(uint64 tick) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        bytes32 tickSeed = bytes32(uint256(tick));
        s.world.currentTick = tick;
        s.world.nextHeartbeatAtTick = tick + 1;
        s.world.nextHeartbeatAtTs = 0;
        s.world.currentTickSeed = tickSeed;
        s.tickSeeds[tick] = tickSeed;
    }

    function setClanUpkeepState(
        uint32 clanId,
        uint64 lastSettledTick,
        uint256 vaultWood,
        uint256 vaultWheat,
        uint256 vaultFish,
        uint16 coldDamage
    ) external {
        Clan storage clan = LibStorage.appStorage().clans[clanId];
        clan.lastSettledTick = lastSettledTick;
        clan.vaultWood = vaultWood;
        clan.vaultWheat = vaultWheat;
        clan.vaultFish = vaultFish;
        clan.starvationStartsAtTick = 0;
        clan.coldDamage = coldDamage;
    }

    function setClanWallLevel(uint32 clanId, uint8 wallLevel) external {
        LibStorage.appStorage().clans[clanId].wallLevel = wallLevel;
    }

    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        Clan storage clan = LibStorage.appStorage().clans[clanId];
        clan.vaultWood = wood;
        clan.vaultIron = iron;
        clan.vaultWheat = wheat;
        clan.vaultFish = fish;
    }

    function wallReservation(uint32 clansmanId)
        external
        view
        returns (bool active, uint8 fromLevel, uint256 woodCost, uint256 ironCost)
    {
        LibStorage.WallUpgradeReservation storage r = LibStorage.appStorage().wallUpgradeReservations[clansmanId];
        return (r.active, r.fromLevel, r.woodCost, r.ironCost);
    }

    function pendingWallUpgrades(uint32 clanId) external view returns (uint8) {
        return LibStorage.appStorage().pendingWallUpgradesByClan[clanId];
    }

    function reservedWood(uint32 clanId) external view returns (uint256) {
        return LibStorage.appStorage().reservedWoodByClan[clanId];
    }

    function reservedIron(uint32 clanId) external view returns (uint256) {
        return LibStorage.appStorage().reservedIronByClan[clanId];
    }
}

contract PublicDemoBlockersTest is Test {
    event ClanColdShortage(uint32 indexed clanId, uint64 tick, uint256 woodShort);
    event WallDegradedByCold(uint32 indexed clanId, uint8 newWallLevel, uint64 tick);
    event ClansmanColdDeath(uint32 indexed clanId, uint32 csId, uint64 tick);
    event ClanEliminated(uint32 indexed clanId, uint64 indexed tick);
    event ClanDied(uint32 indexed clanId, uint64 tick, string reason);

    address private elder = address(0xA11CE);

    function test_bootstrapSelectorsHideGameplayUntilTreasurySeeded() public {
        Diamond diamond = _newDiamond();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut(address(diamond)).diamondCut(_bootstrapCut(), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));

        IDiamondLoupe loupe = IDiamondLoupe(address(diamond));
        assertEq(loupe.facetAddress(IClanWorld.initTreasury.selector) != address(0), true, "treasury selector");
        assertEq(loupe.facetAddress(IClanWorld.seedPools.selector) != address(0), true, "seed selector");
        assertEq(loupe.facetAddress(IClanWorld.mintClan.selector), address(0), "mint hidden");
        assertEq(loupe.facetAddress(IClanWorld.heartbeat.selector), address(0), "heartbeat hidden");
        assertEq(loupe.facetAddress(IClanWorld.submitClanOrders.selector), address(0), "orders hidden");
        assertEq(loupe.facetAddress(IClanWorld.settleClan.selector), address(0), "settlement hidden");

        _initAndSeedTreasury(IClanWorld(address(diamond)));

        IDiamondCut(address(diamond)).diamondCut(_gameplayCut(), address(0), "");
        assertEq(loupe.facetAddress(IClanWorld.mintClan.selector) != address(0), true, "mint live");
        assertEq(loupe.facetAddress(IClanWorld.heartbeat.selector) != address(0), true, "heartbeat live");
        assertEq(loupe.facetAddress(IClanWorld.submitClanOrders.selector) != address(0), true, "orders live");
        assertEq(loupe.facetAddress(IClanWorld.settleClan.selector) != address(0), true, "settlement live");
    }

    function test_settlementUpkeepEmitsWinterColdAndDeathEvents() public {
        IClanWorld world = _deploySettlementDiamond();
        IPublicDemoHarness harness = IPublicDemoHarness(address(world));
        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;

        uint32 coldShortClan = _mint(world);
        harness.setCurrentTick(winterStart + 1);
        harness.setClanUpkeepState(coldShortClan, winterStart, 1e18, 100e18, 100e18, 0);
        vm.expectEmit(true, false, false, true, address(world));
        emit ClanColdShortage(coldShortClan, winterStart, 2e18);
        world.settleClan(coldShortClan);

        uint32 wallClan = _mint(world);
        harness.setCurrentTick(winterStart + 1);
        harness.setClanWallLevel(wallClan, 2);
        harness.setClanUpkeepState(wallClan, winterStart, 0, 100e18, 100e18, 1);
        vm.expectEmit(true, false, false, true, address(world));
        emit WallDegradedByCold(wallClan, 1, winterStart);
        world.settleClan(wallClan);

        uint32 coldDeathClan = _mint(world);
        harness.setCurrentTick(winterStart + 1);
        harness.setClanUpkeepState(coldDeathClan, winterStart, 0, 100e18, 100e18, 1);
        vm.recordLogs();
        world.settleClan(coldDeathClan);
        assertEq(_countLogs(vm.getRecordedLogs(), keccak256("ClansmanColdDeath(uint32,uint32,uint64)")), 1);

        uint32 starvedClan = _mint(world);
        harness.setCurrentTick(winterStart + 6);
        harness.setClanUpkeepState(starvedClan, winterStart, 100e18, 0, 0, 0);
        vm.expectEmit(true, true, false, true, address(world));
        emit ClanEliminated(starvedClan, winterStart + 5);
        vm.expectEmit(true, false, false, true, address(world));
        emit ClanDied(starvedClan, winterStart + 5, "starvation");
        world.settleClan(starvedClan);
    }

    function test_wallUpgradeReservationClearsWhenColdDegradesWallBeforeCompletion() public {
        IClanWorld world = _deploySettlementDiamond();
        IPublicDemoHarness harness = IPublicDemoHarness(address(world));
        uint32 clanId = _mint(world);
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        harness.setClanWallLevel(clanId, 1);
        harness.setVault(clanId, 100e18, 100e18, 100e18, 100e18);

        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: clansmanId,
            gotoRegion: world.getClan(clanId).baseRegion,
            action: ActionType.UpgradeWall,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
        vm.prank(elder);
        assertEq(uint8(world.submitClanOrders(clanId, orders)[0].status), uint8(StatusCode.OK), "upgrade queued");
        (bool active, uint8 fromLevel, uint256 woodCost, uint256 ironCost) = harness.wallReservation(clansmanId);
        assertTrue(active, "reservation active");
        assertEq(fromLevel, 1, "from level");
        assertEq(harness.pendingWallUpgrades(clanId), 1, "pending wall");
        assertEq(harness.reservedWood(clanId), woodCost, "reserved wood");
        assertEq(harness.reservedIron(clanId), ironCost, "reserved iron");

        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
        harness.setCurrentTick(winterStart + 1);
        harness.setClanUpkeepState(clanId, winterStart, 0, 100e18, 100e18, 1);
        world.settleClan(clanId);

        (active,,,) = harness.wallReservation(clansmanId);
        assertFalse(active, "reservation cleared");
        assertEq(harness.pendingWallUpgrades(clanId), 0, "pending cleared");
        assertEq(harness.reservedWood(clanId), 0, "wood released");
        assertEq(harness.reservedIron(clanId), 0, "iron released");
        assertEq(uint8(world.getActiveMission(clansmanId).action), uint8(ActionType.UpgradeWall), "mission action retained");
        assertFalse(world.getActiveMission(clansmanId).active, "mission completed");
    }

    function _deploySettlementDiamond() private returns (IClanWorld world) {
        Diamond diamond = _newDiamond();
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();
        IDiamondCut(address(diamond)).diamondCut(_settlementCut(), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));
        return IClanWorld(address(diamond));
    }

    function _newDiamond() private returns (Diamond) {
        DiamondCutFacet cutFacet = new DiamondCutFacet();
        return new Diamond(address(this), address(cutFacet));
    }

    function _mint(IClanWorld world) private returns (uint32 clanId) {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _initAndSeedTreasury(IClanWorld world) private {
        address[6] memory tokens = [
            address(new MinimalERC20("Wood", "WOOD")),
            address(new MinimalERC20("Iron", "IRON")),
            address(new MinimalERC20("Wheat", "WHEAT")),
            address(new MinimalERC20("Fish", "FISH")),
            address(new MinimalERC20("Gold", "GOLD")),
            address(new MinimalERC20("Blueprint", "BPRT"))
        ];
        address[4] memory pools = [
            address(new StubPool(tokens[0], tokens[4], address(world))),
            address(new StubPool(tokens[2], tokens[4], address(world))),
            address(new StubPool(tokens[3], tokens[4], address(world))),
            address(new StubPool(tokens[1], tokens[4], address(world)))
        ];
        PoolSeedConfig memory cfg = PoolSeedConfig({
            woodSeed: 100e18,
            wheatSeed: 100e18,
            fishSeed: 100e18,
            ironSeed: 100e18,
            goldSeedForWood: 100e18,
            goldSeedForWheat: 100e18,
            goldSeedForFish: 100e18,
            goldSeedForIron: 100e18
        });

        world.initTreasury(tokens, pools);
        MinimalERC20(tokens[0]).seedTreasury(address(this), cfg.woodSeed);
        MinimalERC20(tokens[1]).seedTreasury(address(this), cfg.ironSeed);
        MinimalERC20(tokens[2]).seedTreasury(address(this), cfg.wheatSeed);
        MinimalERC20(tokens[3]).seedTreasury(address(this), cfg.fishSeed);
        MinimalERC20(tokens[4]).seedTreasury(
            address(this), cfg.goldSeedForWood + cfg.goldSeedForWheat + cfg.goldSeedForFish + cfg.goldSeedForIron
        );
        for (uint256 i = 0; i < 5; i++) {
            MinimalERC20(tokens[i]).approve(address(world), type(uint256).max);
        }
        world.seedPools(cfg);
    }

    function _bootstrapCut() private returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](5);
        cut[0] = _facetCut(address(new DiamondLoupeFacet()), DiamondSelectors.loupeSelectors());
        cut[1] = _facetCut(address(new OwnershipFacet()), DiamondSelectors.ownershipFacetSelectors());
        cut[2] = _facetCut(address(new TreasuryFacet()), DiamondSelectors.treasurySelectors());
        cut[3] = _facetCut(address(new RawWorldViewsFacet()), DiamondSelectors.rawWorldViewsSelectors());
        cut[4] = _facetCut(address(new RawTreasuryViewsFacet()), DiamondSelectors.rawTreasuryViewsSelectors());
    }

    function _gameplayCut() private returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](5);
        cut[0] = _facetCut(address(new ClanLifecycleFacet()), DiamondSelectors.lifecycleSelectors());
        cut[1] = _facetCut(address(new SubmitOrdersFacet()), DiamondSelectors.submitOrdersSelectors());
        cut[2] = _facetCut(address(new SettlementFacet()), DiamondSelectors.settlementSelectors());
        cut[3] = _facetCut(address(new RawClanViewsFacet()), DiamondSelectors.rawClanViewsSelectors());
        cut[4] = _facetCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors());
    }

    function _settlementCut() private returns (IDiamondCut.FacetCut[] memory cut) {
        bytes4[] memory harnessSelectors = new bytes4[](8);
        harnessSelectors[0] = PublicDemoHarnessFacet.setCurrentTick.selector;
        harnessSelectors[1] = PublicDemoHarnessFacet.setClanUpkeepState.selector;
        harnessSelectors[2] = PublicDemoHarnessFacet.setClanWallLevel.selector;
        harnessSelectors[3] = PublicDemoHarnessFacet.setVault.selector;
        harnessSelectors[4] = PublicDemoHarnessFacet.wallReservation.selector;
        harnessSelectors[5] = PublicDemoHarnessFacet.pendingWallUpgrades.selector;
        harnessSelectors[6] = PublicDemoHarnessFacet.reservedWood.selector;
        harnessSelectors[7] = PublicDemoHarnessFacet.reservedIron.selector;

        cut = new IDiamondCut.FacetCut[](6);
        cut[0] = _facetCut(address(new RawWorldViewsFacet()), DiamondSelectors.rawWorldViewsSelectors());
        cut[1] = _facetCut(address(new RawClanViewsFacet()), DiamondSelectors.rawClanViewsSelectors());
        cut[2] = _facetCut(address(new ClanLifecycleFacet()), DiamondSelectors.lifecycleSelectors());
        cut[3] = _facetCut(address(new SubmitOrdersFacet()), DiamondSelectors.submitOrdersSelectors());
        cut[4] = _facetCut(address(new SettlementFacet()), DiamondSelectors.settlementSelectors());
        cut[5] = _facetCut(address(new PublicDemoHarnessFacet()), harnessSelectors);
    }

    function _facetCut(address facet, bytes4[] memory selectors) private pure returns (IDiamondCut.FacetCut memory) {
        return IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: selectors
        });
    }

    function _countLogs(Vm.Log[] memory logs, bytes32 topic0) private pure returns (uint256 count) {
        for (uint256 i = 0; i < logs.length; i++) {
            if (logs[i].topics.length > 0 && logs[i].topics[0] == topic0) count++;
        }
    }
}
