// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Vm} from "forge-std/Vm.sol";
import {DiamondSelectors} from "../../script/DiamondSelectors.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {BlueprintTransferFacet} from "../../src/diamond/facets/BlueprintTransferFacet.sol";
import {BundleTransferFacet} from "../../src/diamond/facets/BundleTransferFacet.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {FinalizeSeasonFacet} from "../../src/diamond/facets/FinalizeSeasonFacet.sol";
import {GoldTransferFacet} from "../../src/diamond/facets/GoldTransferFacet.sol";
import {HeartbeatConfigFacet} from "../../src/diamond/facets/HeartbeatConfigFacet.sol";
import {HeartbeatFacet} from "../../src/diamond/facets/HeartbeatFacet.sol";
import {ClanLifecycleFacet} from "../../src/diamond/facets/ClanLifecycleFacet.sol";
import {ClanOwnershipFacet} from "../../src/diamond/facets/ClanOwnershipFacet.sol";
import {RawClanViewsFacet} from "../../src/diamond/facets/RawClanViewsFacet.sol";
import {RawBanditViewsFacet} from "../../src/diamond/facets/RawBanditViewsFacet.sol";
import {RawTreasuryViewsFacet} from "../../src/diamond/facets/RawTreasuryViewsFacet.sol";
import {RawWorldViewsFacet} from "../../src/diamond/facets/RawWorldViewsFacet.sol";
import {SettlementFacet} from "../../src/diamond/facets/SettlementFacet.sol";
import {SubmitOrdersFacet} from "../../src/diamond/facets/SubmitOrdersFacet.sol";
import {TreasuryFacet} from "../../src/diamond/facets/TreasuryFacet.sol";
import {VaultResourceTransferFacet} from "../../src/diamond/facets/VaultResourceTransferFacet.sol";
import {WorldPauseFacet} from "../../src/diamond/facets/WorldPauseFacet.sol";
import {LibStorage} from "../../src/diamond/lib/LibStorage.sol";
import {MinimalERC20} from "../../src/MinimalERC20.sol";
import {StubPool} from "../../src/StubPool.sol";
import {
    IClanWorld,
    BanditTroop,
    Clan,
    ClanOrder,
    ClanWorldConstants,
    OrderResult,
    ResourceType,
    ScheduledMarketAction,
    TreasuryState,
    WorldState
} from "../../src/IClanWorld.sol";

interface IWorldPause {
    function pauseWorld() external;
    function unpauseWorld() external;
    function isWorldPaused() external view returns (bool);
}

interface IHeartbeatAdmin {
    function setHeartbeatIntervalSeconds(uint64 intervalSeconds) external;
    function triggerBanditSpawn() external;
    function banditSpawnTriggered() external view returns (bool);
}

interface IWorldPauseHarness {
    function grantBlueprint(uint32 clanId, uint256 amount) external;
    function setCurrentTick(uint64 tick) external;
    function pausedAtTs() external view returns (uint64);
}

contract WorldPauseHarnessFacet {
    function grantBlueprint(uint32 clanId, uint256 amount) external {
        LibStorage.appStorage().clans[clanId].blueprintBalance += amount;
    }

    function setCurrentTick(uint64 tick) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        s.world.currentTick = tick;
        s.world.nextHeartbeatAtTick = tick + 1;
        s.world.nextHeartbeatAtTs = 0;
    }

    function pausedAtTs() external view returns (uint64) {
        return LibStorage.appStorage().world.pausedAtTs;
    }
}

contract WorldPauseTest is Test {
    event WorldPaused(uint64 indexed tick, uint64 pausedAtTs);
    event WorldUnpaused(uint64 indexed tick, uint64 durationSeconds, uint64 nextHeartbeatAtTs);
    event BanditSpawned(uint32 indexed banditId, uint8 region, uint8 tier, uint16 attackPower);

    IClanWorld private world;
    IWorldPause private pause;
    IWorldPauseHarness private harness;

    function setUp() public {
        world = _deployPauseHeartbeatDiamond();
        pause = IWorldPause(address(world));
        harness = IWorldPauseHarness(address(world));
    }

    function test_heartbeatAdvancesWhenNotPaused() public {
        assertEq(world.getWorldState().currentTick, 0, "pre tick");

        world.heartbeat();

        assertEq(world.getWorldState().currentTick, 1, "post tick");
    }

    function test_pauseRevertsHeartbeat_beforeRateLimit() public {
        world.heartbeat();
        assertEq(world.getWorldState().currentTick, 1, "setup tick");
        pause.pauseWorld();

        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.heartbeat();
    }

    function test_pauseWorldAndUnpauseWorldEmitPayloadsAndClearPausedAtTs() public {
        uint64 pausedAt = uint64(block.timestamp);
        vm.expectEmit(true, true, true, true, address(world));
        emit WorldPaused(0, pausedAt);
        pause.pauseWorld();
        assertTrue(pause.isWorldPaused(), "paused");
        assertTrue(world.getWorldState().worldPaused, "world state paused");
        assertEq(world.getWorldState().pausedAtTs, pausedAt, "world state pausedAt");
        assertEq(harness.pausedAtTs(), pausedAt, "pausedAt stored");

        vm.warp(block.timestamp + 37);
        uint64 expectedNextHeartbeatAtTs = uint64(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
        vm.expectEmit(true, true, true, true, address(world));
        emit WorldUnpaused(0, 37, expectedNextHeartbeatAtTs);
        pause.unpauseWorld();

        assertFalse(pause.isWorldPaused(), "unpaused");
        assertFalse(world.getWorldState().worldPaused, "world state unpaused");
        assertEq(world.getWorldState().pausedAtTs, 0, "world state pausedAt cleared");
        assertEq(harness.pausedAtTs(), 0, "pausedAt cleared");
        assertEq(world.getWorldState().nextHeartbeatAtTs, expectedNextHeartbeatAtTs, "next heartbeat");
    }

    function test_nonOwnerCannotPauseOrUnpause() public {
        vm.prank(address(0xBEEF));
        vm.expectRevert();
        pause.pauseWorld();

        pause.pauseWorld();

        vm.prank(address(0xBEEF));
        vm.expectRevert();
        pause.unpauseWorld();
    }

    function test_doublePauseReverts() public {
        pause.pauseWorld();

        vm.expectRevert(bytes("ClanWorld: already paused"));
        pause.pauseWorld();
    }

    function test_doubleUnpauseReverts() public {
        vm.expectRevert(bytes("ClanWorld: not paused"));
        pause.unpauseWorld();
    }

    function test_heartbeatIntervalChangeWhilePausedAffectsUnpauseCadence() public {
        pause.pauseWorld();
        IHeartbeatAdmin(address(world)).setHeartbeatIntervalSeconds(7);
        vm.warp(block.timestamp + 5);

        pause.unpauseWorld();

        assertEq(world.getWorldState().nextHeartbeatAtTs, block.timestamp + 7, "unpause cadence");
    }

    function test_triggerBanditSpawnQueuedDuringPause_FiresAfterUnpause() public {
        world.mintClan(address(0xA11CE));
        IHeartbeatAdmin heartbeatAdmin = IHeartbeatAdmin(address(world));
        pause.pauseWorld();

        uint64 pausedTick = world.getWorldState().currentTick;
        vm.recordLogs();
        heartbeatAdmin.triggerBanditSpawn();
        Vm.Log[] memory logs = vm.getRecordedLogs();

        assertEq(logs.length, 1, "only queue event emitted");
        assertTrue(heartbeatAdmin.banditSpawnTriggered(), "spawn queued");
        assertEq(world.getWorldState().currentBanditSpawnChanceBps, 10000, "preview chance");
        assertEq(world.getWorldState().currentTick, pausedTick, "tick frozen");
        assertEq(world.getWorldState().activeBanditId, 0, "no immediate active bandit");
        assertEq(world.getBanditTroop(1).id, 0, "no immediate bandit");

        pause.unpauseWorld();
        vm.warp(world.getWorldState().nextHeartbeatAtTs);

        vm.expectEmit(true, false, false, false, address(world));
        emit BanditSpawned(1, 0, 0, 0);
        world.heartbeat();

        assertFalse(heartbeatAdmin.banditSpawnTriggered(), "queue consumed");
        assertEq(world.getWorldState().activeBanditId, 1, "bandit active after heartbeat");
        assertEq(world.getBanditTroop(1).id, 1, "bandit spawned after unpause");
    }

    function test_settleClanWorksWhenNotPaused() public {
        (uint32 clanId,) = world.mintClan(address(0xA11CE));
        world.heartbeat();

        world.settleClan(clanId);

        assertEq(world.getClan(clanId).lastSettledTick, world.getWorldState().currentTick, "settled tick");
    }

    function test_settleClanRevertsWhenPaused() public {
        (uint32 clanId,) = world.mintClan(address(0xA11CE));
        pause.pauseWorld();

        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.settleClan(clanId);
    }

    function test_settleClansmanWorksWhenNotPaused() public {
        (uint32 clanId,) = world.mintClan(address(0xA11CE));
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        world.heartbeat();

        world.settleClansman(clansmanId);

        assertEq(world.getClan(clanId).lastSettledTick, world.getWorldState().currentTick, "settled tick");
    }

    function test_settleClansmanRevertsWhenPaused() public {
        (uint32 clanId,) = world.mintClan(address(0xA11CE));
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        pause.pauseWorld();

        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.settleClansman(clansmanId);
    }

    function test_submitOrdersWorksWhenNotPaused() public {
        address elder = address(0xA11CE);
        (uint32 clanId,) = world.mintClan(elder);
        ClanOrder[] memory orders = new ClanOrder[](0);

        vm.prank(elder);
        OrderResult[] memory results = world.submitClanOrders(clanId, orders);

        assertEq(results.length, 0, "empty orders accepted");
    }

    function test_submitOrdersRevertsWhenPaused() public {
        address elder = address(0xA11CE);
        (uint32 clanId,) = world.mintClan(elder);
        ClanOrder[] memory orders = new ClanOrder[](0);
        pause.pauseWorld();

        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.submitClanOrders(clanId, orders);
    }

    function test_mintClanWorksWhenNotPaused() public {
        (uint32 clanId,) = world.mintClan(address(0xA11CE));

        assertEq(clanId, 1, "first clan minted");
    }

    function test_mintClanRevertsWhenPausedFromDifferentEoa() public {
        pause.pauseWorld();

        vm.prank(address(0xB0B));
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.mintClan(address(0xB0B));
    }

    function test_transferClanOwnershipWorksWhenNotPaused() public {
        address elder = address(0xA11CE);
        address nextOwner = address(0xB0B);
        (uint32 clanId,) = world.mintClan(elder);

        vm.prank(elder);
        world.transferClanOwnership(clanId, nextOwner);

        assertEq(world.getClan(clanId).owner, nextOwner, "new clan owner");
    }

    function test_transferClanOwnershipRevertsWhenPaused() public {
        address elder = address(0xA11CE);
        (uint32 clanId,) = world.mintClan(elder);
        pause.pauseWorld();

        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.transferClanOwnership(clanId, address(0xB0B));
    }

    function test_transferGoldWorksWhenNotPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();

        vm.prank(elder);
        world.transferGold(fromClanId, toClanId, 1e18);

        assertEq(world.getClan(toClanId).goldBalance, 4e18, "recipient gold");
    }

    function test_transferGoldRevertsWhenPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();
        pause.pauseWorld();

        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.transferGold(fromClanId, toClanId, 1e18);
    }

    function test_transferVaultResourceWorksWhenNotPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();

        vm.prank(elder);
        world.transferVaultResource(fromClanId, toClanId, ResourceType.Wood, 1e18);

        assertEq(world.getClan(toClanId).vaultWood, 21e18, "recipient wood");
    }

    function test_transferVaultResourceRevertsWhenPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();
        pause.pauseWorld();

        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.transferVaultResource(fromClanId, toClanId, ResourceType.Wood, 1e18);
    }

    function test_transferBlueprintWorksWhenNotPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();
        harness.grantBlueprint(fromClanId, 2e18);

        vm.prank(elder);
        world.transferBlueprint(fromClanId, toClanId, 1e18);

        assertEq(world.getClan(toClanId).blueprintBalance, 1e18, "recipient blueprints");
    }

    function test_transferBlueprintRevertsWhenPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();
        pause.pauseWorld();

        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.transferBlueprint(fromClanId, toClanId, 1e18);
    }

    function test_transferBundleWorksWhenNotPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();

        vm.prank(elder);
        world.transferBundle(fromClanId, toClanId, 1e18, 0, 1e18, 0, 1e18, 0);

        assertEq(world.getClan(toClanId).goldBalance, 4e18, "recipient bundle gold");
        assertEq(world.getClan(toClanId).vaultWood, 21e18, "recipient bundle wood");
        assertEq(world.getClan(toClanId).vaultWheat, 21e18, "recipient bundle wheat");
    }

    function test_transferBundleRevertsWhenPaused() public {
        (uint32 fromClanId, uint32 toClanId, address elder,) = _mintTwoClans();
        pause.pauseWorld();

        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.transferBundle(fromClanId, toClanId, 1e18, 0, 0, 0, 0, 0);
    }

    function test_initTreasuryWorksWhenNotPaused() public {
        (address[6] memory tokens, address[4] memory pools) = _deployTreasuryBoundary();

        world.initTreasury(tokens, pools);

        TreasuryState memory treasury = world.getTreasuryState();
        assertEq(treasury.woodToken, tokens[0], "wood token");
        assertEq(treasury.ironGoldPool, pools[3], "iron pool");
    }

    function test_initTreasuryRevertsWhenPaused() public {
        address[6] memory tokens;
        address[4] memory pools;
        pause.pauseWorld();

        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.initTreasury(tokens, pools);
    }

    function test_finalizeSeasonSucceedsWhenNotPausedAtSeasonBoundary() public {
        harness.setCurrentTick(ClanWorldConstants.SEASON_DURATION_TICKS);

        world.finalizeSeason();

        assertTrue(world.getWorldState().seasonFinalized, "season finalized");
    }

    function test_finalizeSeasonRevertsWhilePaused() public {
        harness.setCurrentTick(ClanWorldConstants.SEASON_DURATION_TICKS);
        pause.pauseWorld();

        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.finalizeSeason();
    }

    function test_invariantPausedWorldStateFrozenAcrossWarpAndRevertedWrites() public {
        address elder = address(0xA11CE);
        (uint32 clanId,) = world.mintClan(elder);
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        pause.pauseWorld();

        WorldState memory beforeWorld = world.getWorldState();
        Clan memory beforeClan = world.getClan(clanId);
        BanditTroop memory beforeBandit = world.getBanditTroop(1);
        ScheduledMarketAction[] memory beforeMarketQueue =
            world.getScheduledMarketActionsForTick(beforeWorld.currentTick + 1);

        vm.warp(block.timestamp + 10 minutes);

        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.heartbeat();
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.settleClan(clanId);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.settleClansman(clansmanId);
        vm.prank(elder);
        vm.expectRevert(bytes("ClanWorld: world paused"));
        world.transferGold(clanId, clanId + 1, 1e18);

        WorldState memory afterWorld = world.getWorldState();
        Clan memory afterClan = world.getClan(clanId);
        BanditTroop memory afterBandit = world.getBanditTroop(1);
        ScheduledMarketAction[] memory afterMarketQueue =
            world.getScheduledMarketActionsForTick(beforeWorld.currentTick + 1);

        assertEq(afterWorld.currentTick, beforeWorld.currentTick, "currentTick frozen");
        assertEq(afterWorld.currentTickSeed, beforeWorld.currentTickSeed, "tick seed frozen");
        assertEq(afterClan.lastSettledTick, beforeClan.lastSettledTick, "lastSettledTick frozen");
        assertEq(afterClan.livingClansmen, beforeClan.livingClansmen, "living clansmen frozen");
        assertEq(afterClan.vaultWheat, beforeClan.vaultWheat, "wheat/starvation state frozen");
        assertEq(uint8(afterBandit.state), uint8(beforeBandit.state), "bandit state frozen");
        assertEq(afterMarketQueue.length, beforeMarketQueue.length, "market queue frozen");
    }

    function _deployPauseHeartbeatDiamond() internal returns (IClanWorld diamondWorld) {
        DiamondCutFacet cutFacet = new DiamondCutFacet();
        Diamond diamond = new Diamond(address(this), address(cutFacet));
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](18);
        cut[0] = _facetCut(address(new RawWorldViewsFacet()), DiamondSelectors.rawWorldViewsSelectors());
        cut[1] = _facetCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors());
        cut[2] = _facetCut(address(new HeartbeatConfigFacet()), DiamondSelectors.heartbeatConfigSelectors());
        cut[3] = _facetCut(address(new WorldPauseFacet()), _worldPauseSelectors());
        cut[4] = _facetCut(address(new ClanLifecycleFacet()), DiamondSelectors.lifecycleSelectors());
        cut[5] = _facetCut(address(new RawClanViewsFacet()), DiamondSelectors.rawClanViewsSelectors());
        cut[6] = _facetCut(address(new SettlementFacet()), DiamondSelectors.settlementSelectors());
        cut[7] = _facetCut(address(new SubmitOrdersFacet()), DiamondSelectors.submitOrdersSelectors());
        cut[8] = _facetCut(address(new ClanOwnershipFacet()), DiamondSelectors.ownershipSelectors());
        cut[9] = _facetCut(address(new GoldTransferFacet()), DiamondSelectors.goldTransferSelectors());
        cut[10] = _facetCut(address(new VaultResourceTransferFacet()), DiamondSelectors.vaultResourceTransferSelectors());
        cut[11] = _facetCut(address(new BlueprintTransferFacet()), DiamondSelectors.blueprintTransferSelectors());
        cut[12] = _facetCut(address(new BundleTransferFacet()), DiamondSelectors.bundleTransferSelectors());
        cut[13] = _facetCut(address(new WorldPauseHarnessFacet()), _worldPauseHarnessSelectors());
        cut[14] = _facetCut(address(new TreasuryFacet()), DiamondSelectors.treasurySelectors());
        cut[15] = _facetCut(address(new RawTreasuryViewsFacet()), DiamondSelectors.rawTreasuryViewsSelectors());
        cut[16] = _facetCut(address(new FinalizeSeasonFacet()), DiamondSelectors.seasonSelectors());
        cut[17] = _facetCut(address(new RawBanditViewsFacet()), DiamondSelectors.rawBanditViewsSelectors());

        IDiamondCut(address(diamond)).diamondCut(cut, address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));
        diamondWorld = IClanWorld(address(diamond));
        assertEq(diamondWorld.getWorldState().nextHeartbeatAtTs, block.timestamp, "initial heartbeat cadence");
        assertEq(ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS, 60, "heartbeat interval fixture");
    }

    function _facetCut(address facet, bytes4[] memory selectors) internal pure returns (IDiamondCut.FacetCut memory) {
        return IDiamondCut.FacetCut({
            facetAddress: facet, action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });
    }

    function _worldPauseSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](3);
        selectors[0] = WorldPauseFacet.pauseWorld.selector;
        selectors[1] = WorldPauseFacet.unpauseWorld.selector;
        selectors[2] = WorldPauseFacet.isWorldPaused.selector;
    }

    function _worldPauseHarnessSelectors() internal pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](3);
        selectors[0] = WorldPauseHarnessFacet.grantBlueprint.selector;
        selectors[1] = WorldPauseHarnessFacet.setCurrentTick.selector;
        selectors[2] = WorldPauseHarnessFacet.pausedAtTs.selector;
    }

    function _mintTwoClans() internal returns (uint32 fromClanId, uint32 toClanId, address fromOwner, address toOwner) {
        fromOwner = address(0xA11CE);
        toOwner = address(0xB0B);
        (fromClanId,) = world.mintClan(fromOwner);
        (toClanId,) = world.mintClan(toOwner);
    }

    function _deployTreasuryBoundary() internal returns (address[6] memory tokens, address[4] memory pools) {
        tokens[0] = address(new MinimalERC20("Wood", "WOOD"));
        tokens[1] = address(new MinimalERC20("Iron", "IRON"));
        tokens[2] = address(new MinimalERC20("Wheat", "WHEAT"));
        tokens[3] = address(new MinimalERC20("Fish", "FISH"));
        tokens[4] = address(new MinimalERC20("Gold", "GOLD"));
        tokens[5] = address(new MinimalERC20("Blueprint", "BP"));

        pools[0] = address(new StubPool(tokens[0], tokens[4], address(world)));
        pools[1] = address(new StubPool(tokens[2], tokens[4], address(world)));
        pools[2] = address(new StubPool(tokens[3], tokens[4], address(world)));
        pools[3] = address(new StubPool(tokens[1], tokens[4], address(world)));
    }
}
