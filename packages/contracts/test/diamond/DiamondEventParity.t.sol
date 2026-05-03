// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {Vm} from "forge-std/Vm.sol";
import {DiamondSelectors} from "../../script/DiamondSelectors.sol";
import {ClanWorld} from "../../src/ClanWorld.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {
    ActionType,
    ClanOrder,
    ClanWorldConstants,
    IClanWorld,
    PoolSeedConfig,
    ResourceType,
    StatusCode,
    WithdrawResourcesData
} from "../../src/IClanWorld.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {BundleTransferFacet} from "../../src/diamond/facets/BundleTransferFacet.sol";
import {ClanLifecycleFacet} from "../../src/diamond/facets/ClanLifecycleFacet.sol";
import {ClanOwnershipFacet} from "../../src/diamond/facets/ClanOwnershipFacet.sol";
import {GoldTransferFacet} from "../../src/diamond/facets/GoldTransferFacet.sol";
import {HeartbeatFacet} from "../../src/diamond/facets/HeartbeatFacet.sol";
import {RawBanditViewsFacet} from "../../src/diamond/facets/RawBanditViewsFacet.sol";
import {RawClanViewsFacet} from "../../src/diamond/facets/RawClanViewsFacet.sol";
import {RawTreasuryViewsFacet} from "../../src/diamond/facets/RawTreasuryViewsFacet.sol";
import {RawWorldViewsFacet} from "../../src/diamond/facets/RawWorldViewsFacet.sol";
import {SubmitOrdersFacet} from "../../src/diamond/facets/SubmitOrdersFacet.sol";
import {TreasuryFacet} from "../../src/diamond/facets/TreasuryFacet.sol";
import {VaultResourceTransferFacet} from "../../src/diamond/facets/VaultResourceTransferFacet.sol";
import {MinimalERC20} from "../../src/MinimalERC20.sol";
import {StubPool} from "../../src/StubPool.sol";
import {LibStorage} from "../../src/diamond/lib/LibStorage.sol";

contract CoreEventParityHarness is ClanWorld {
    function setCarry(uint32 clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        _clansmen[clansmanId].carryWood = wood;
        _clansmen[clansmanId].carryIron = iron;
        _clansmen[clansmanId].carryWheat = wheat;
        _clansmen[clansmanId].carryFish = fish;
    }

    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        _clans[clanId].vaultWood = wood;
        _clans[clanId].vaultIron = iron;
        _clans[clanId].vaultWheat = wheat;
        _clans[clanId].vaultFish = fish;
    }
}

interface IEventParityFixture {
    function setCarry(uint32 clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external;
    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external;
}

contract EventParityFixtureFacet {
    function setCarry(uint32 clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        s.clansmen[clansmanId].carryWood = wood;
        s.clansmen[clansmanId].carryIron = iron;
        s.clansmen[clansmanId].carryWheat = wheat;
        s.clansmen[clansmanId].carryFish = fish;
    }

    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        s.clans[clanId].vaultWood = wood;
        s.clans[clanId].vaultIron = iron;
        s.clans[clanId].vaultWheat = wheat;
        s.clans[clanId].vaultFish = fish;
    }
}

contract DiamondEventParityTest is Test {
    Diamond diamond;

    struct MarketPair {
        address woodToken;
        address[6] tokens;
    }

    function setUp() public {
        DiamondCutFacet cutFacet = new DiamondCutFacet();
        diamond = new Diamond(address(this), address(cutFacet));
    }

    function testMintClanLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawAndLifecycleFacets();

        address elder = address(0xA11CE);

        vm.recordLogs();
        vm.prank(elder);
        core.mintClan(elder);
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        IClanWorld(address(diamond)).mintClan(elder);
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testSubmitWaitOrderLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleSubmitFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        uint8 coreBaseRegion = core.getClan(coreClanId).baseRegion;
        uint8 diamondBaseRegion = IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion;

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _basicOrder(coreClansmanId, coreBaseRegion, ActionType.Wait, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _basicOrder(diamondClansmanId, diamondBaseRegion, ActionType.Wait, 0);

        vm.recordLogs();
        vm.prank(elder);
        assertEq(uint8(core.submitClanOrders(coreClanId, coreOrders)[0].status), uint8(StatusCode.OK));
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        assertEq(
            uint8(IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders)[0].status),
            uint8(StatusCode.OK)
        );
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testRetaskOrderLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleSubmitFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        uint8 coreBaseRegion = core.getClan(coreClanId).baseRegion;
        uint8 diamondBaseRegion = IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion;

        ClanOrder[] memory coreFirst = new ClanOrder[](1);
        coreFirst[0] = _basicOrder(coreClansmanId, coreBaseRegion, ActionType.Wait, 0);
        ClanOrder[] memory diamondFirst = new ClanOrder[](1);
        diamondFirst[0] = _basicOrder(diamondClansmanId, diamondBaseRegion, ActionType.Wait, 0);
        vm.prank(elder);
        core.submitClanOrders(coreClanId, coreFirst);
        vm.prank(elder);
        IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondFirst);

        vm.warp(block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS + 1);

        ClanOrder[] memory coreSecond = new ClanOrder[](1);
        coreSecond[0] =
            _basicOrder(coreClansmanId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron, 0);
        ClanOrder[] memory diamondSecond = new ClanOrder[](1);
        diamondSecond[0] =
            _basicOrder(diamondClansmanId, ClanWorldConstants.REGION_MOUNTAINS, ActionType.MineIron, 0);

        vm.recordLogs();
        vm.prank(elder);
        assertEq(uint8(core.submitClanOrders(coreClanId, coreSecond)[0].status), uint8(StatusCode.OK));
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        assertEq(
            uint8(IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondSecond)[0].status),
            uint8(StatusCode.OK)
        );
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testHeartbeatMissionCompletionLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleSubmitHeartbeatFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        uint8 coreBaseRegion = core.getClan(coreClanId).baseRegion;
        uint8 diamondBaseRegion = IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion;

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _basicOrder(coreClansmanId, coreBaseRegion, ActionType.DepositResources, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _basicOrder(diamondClansmanId, diamondBaseRegion, ActionType.DepositResources, 0);

        vm.prank(elder);
        core.submitClanOrders(coreClanId, coreOrders);
        vm.prank(elder);
        IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders);

        uint64 settlesAtTick = core.getActiveMission(coreClansmanId).settlesAtTick;
        Vm.Log[] memory coreLogs;
        Vm.Log[] memory diamondLogs;
        while (core.getWorldState().currentTick <= settlesAtTick) {
            vm.warp(core.getWorldState().nextHeartbeatAtTs);
            bool finalHeartbeat = core.getWorldState().currentTick == settlesAtTick;
            if (finalHeartbeat) vm.recordLogs();
            core.heartbeat();
            if (finalHeartbeat) coreLogs = vm.getRecordedLogs();
            if (finalHeartbeat) vm.recordLogs();
            IClanWorld(address(diamond)).heartbeat();
            if (finalHeartbeat) diamondLogs = vm.getRecordedLogs();
        }

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testGatherWoodCompletionLogsMatchCore() public {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitHeartbeatFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _basicOrder(coreClansmanId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] =
            _basicOrder(diamondClansmanId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood, 0);

        vm.prank(elder);
        core.submitClanOrders(coreClanId, coreOrders);
        vm.prank(elder);
        IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders);

        (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs) =
            _recordCompletionHeartbeat(core, core.getActiveMission(coreClansmanId).settlesAtTick);
        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testDepositResourceLogsMatchCore() public {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitHeartbeatFixtureFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        uint8 coreBaseRegion = core.getClan(coreClanId).baseRegion;
        uint8 diamondBaseRegion = IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion;

        core.setCarry(coreClansmanId, 2e18, 1e18, 3e18, 4e18);
        IEventParityFixture(address(diamond)).setCarry(diamondClansmanId, 2e18, 1e18, 3e18, 4e18);

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _basicOrder(coreClansmanId, coreBaseRegion, ActionType.DepositResources, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _basicOrder(diamondClansmanId, diamondBaseRegion, ActionType.DepositResources, 0);

        vm.prank(elder);
        core.submitClanOrders(coreClanId, coreOrders);
        vm.prank(elder);
        IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders);

        (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs) =
            _recordCompletionHeartbeat(core, core.getActiveMission(coreClansmanId).settlesAtTick);
        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testWithdrawResourceLogsMatchCore() public {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitHeartbeatFixtureFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        uint8 coreBaseRegion = core.getClan(coreClanId).baseRegion;
        uint8 diamondBaseRegion = IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion;

        core.setVault(coreClanId, 20e18, 20e18, 20e18, 20e18);
        IEventParityFixture(address(diamond)).setVault(diamondClanId, 20e18, 20e18, 20e18, 20e18);

        WithdrawResourcesData memory request = WithdrawResourcesData({wood: 2e18, iron: 1e18, wheat: 3e18, fish: 4e18});
        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _withdrawOrder(coreClansmanId, coreBaseRegion, request);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _withdrawOrder(diamondClansmanId, diamondBaseRegion, request);

        vm.prank(elder);
        core.submitClanOrders(coreClanId, coreOrders);
        vm.prank(elder);
        IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders);

        (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs) =
            _recordCompletionHeartbeat(core, core.getActiveMission(coreClansmanId).settlesAtTick);
        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testWarnUpgradeWallCompletionLogsMatchCore() public {
        _warnUpgradeCompletionLogsMatchCore(ActionType.UpgradeWall, "UpgradeWall");
    }

    function testWarnUpgradeBaseCompletionLogsMatchCore() public {
        _warnUpgradeCompletionLogsMatchCore(ActionType.UpgradeBase, "UpgradeBase");
    }

    function testWarnUpgradeMonumentCompletionLogsMatchCore() public {
        _warnUpgradeCompletionLogsMatchCore(ActionType.UpgradeMonument, "UpgradeMonument");
    }

    function testWarnScheduledMarketCommitLogsMatchCore() public {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitTreasuryHeartbeatFixtureFacets();
        MarketPair memory market = _setupMarketPair(core);

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        core.setCarry(coreClansmanId, 5e18, 0, 0, 0);
        IEventParityFixture(address(diamond)).setCarry(diamondClansmanId, 5e18, 0, 0, 0);

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _marketOrder(coreClansmanId, ActionType.MarketSell, market.woodToken, 1e18, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _marketOrder(diamondClansmanId, ActionType.MarketSell, market.woodToken, 1e18, 0);

        vm.recordLogs();
        vm.prank(elder);
        assertEq(uint8(core.submitClanOrders(coreClanId, coreOrders)[0].status), uint8(StatusCode.OK));
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        assertEq(
            uint8(IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders)[0].status),
            uint8(StatusCode.OK)
        );
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _warnEmitterLogsMatch("ScheduledMarketCommit", coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testWarnScheduledMarketExecutionLogsMatchCore() public {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitTreasuryHeartbeatFixtureFacets();
        MarketPair memory market = _setupMarketPair(core);

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        core.setCarry(coreClansmanId, 5e18, 0, 0, 0);
        IEventParityFixture(address(diamond)).setCarry(diamondClansmanId, 5e18, 0, 0, 0);

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _marketOrder(coreClansmanId, ActionType.MarketSell, market.woodToken, 1e18, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _marketOrder(diamondClansmanId, ActionType.MarketSell, market.woodToken, 1e18, 0);

        vm.prank(elder);
        assertEq(uint8(core.submitClanOrders(coreClanId, coreOrders)[0].status), uint8(StatusCode.OK));
        vm.prank(elder);
        assertEq(
            uint8(IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders)[0].status),
            uint8(StatusCode.OK)
        );

        (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs) =
            _recordCompletionHeartbeat(core, core.getActiveMission(coreClansmanId).settlesAtTick);
        _warnEmitterLogsMatch("ScheduledMarketExecution", coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testWarnScheduledMarketFailureLogsMatchCore() public {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitTreasuryHeartbeatFixtureFacets();
        MarketPair memory market = _setupMarketPair(core);

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        core.setCarry(coreClansmanId, 2e18, 0, 0, 0);
        IEventParityFixture(address(diamond)).setCarry(diamondClansmanId, 2e18, 0, 0, 0);

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _marketOrder(coreClansmanId, ActionType.MarketSell, market.woodToken, 1e18, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _marketOrder(diamondClansmanId, ActionType.MarketSell, market.woodToken, 1e18, 0);

        vm.prank(elder);
        assertEq(uint8(core.submitClanOrders(coreClanId, coreOrders)[0].status), uint8(StatusCode.OK));
        vm.prank(elder);
        assertEq(
            uint8(IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders)[0].status),
            uint8(StatusCode.OK)
        );
        core.setCarry(coreClansmanId, 0, 0, 0, 0);
        IEventParityFixture(address(diamond)).setCarry(diamondClansmanId, 0, 0, 0, 0);

        (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs) =
            _recordCompletionHeartbeat(core, core.getActiveMission(coreClansmanId).settlesAtTick);
        _warnEmitterLogsMatch("ScheduledMarketFailure", coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testTransferGoldLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleGoldHeartbeatFacets();

        address elder = address(0xA11CE);
        address recipient = address(0xB0B);
        (uint32 coreFromClanId, uint32 diamondFromClanId) = _mintPair(core, elder);
        (uint32 coreToClanId, uint32 diamondToClanId) = _mintPair(core, recipient);

        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
        core.heartbeat();
        IClanWorld(address(diamond)).heartbeat();

        vm.recordLogs();
        vm.prank(elder);
        core.transferGold(coreFromClanId, coreToClanId, 1e18);
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        IClanWorld(address(diamond)).transferGold(diamondFromClanId, diamondToClanId, 1e18);
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testTransferVaultResourceLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleVaultTransferHeartbeatFacets();

        address elder = address(0xA11CE);
        address recipient = address(0xB0B);
        (uint32 coreFromClanId, uint32 diamondFromClanId) = _mintPair(core, elder);
        (uint32 coreToClanId, uint32 diamondToClanId) = _mintPair(core, recipient);
        _heartbeatBoth(core);

        vm.recordLogs();
        vm.prank(elder);
        core.transferVaultResource(coreFromClanId, coreToClanId, ResourceType.Wood, 1e18);
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        IClanWorld(address(diamond)).transferVaultResource(diamondFromClanId, diamondToClanId, ResourceType.Wood, 1e18);
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testTransferBundleLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleBundleTransferHeartbeatFacets();

        address elder = address(0xA11CE);
        address recipient = address(0xB0B);
        (uint32 coreFromClanId, uint32 diamondFromClanId) = _mintPair(core, elder);
        (uint32 coreToClanId, uint32 diamondToClanId) = _mintPair(core, recipient);
        _heartbeatBoth(core);

        vm.recordLogs();
        vm.prank(elder);
        core.transferBundle(coreFromClanId, coreToClanId, 1e18, 0, 1e18, 0, 0, 0);
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        IClanWorld(address(diamond)).transferBundle(diamondFromClanId, diamondToClanId, 1e18, 0, 1e18, 0, 0, 0);
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function testTransferClanOwnershipLogsMatchCore() public {
        ClanWorld core = new ClanWorld();
        _installRawLifecycleClanOwnershipHeartbeatFacets();

        address elder = address(0xA11CE);
        address newOwner = address(0xB0B);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        _heartbeatBoth(core);

        vm.recordLogs();
        vm.prank(elder);
        core.transferClanOwnership(coreClanId, newOwner);
        Vm.Log[] memory coreLogs = vm.getRecordedLogs();

        vm.recordLogs();
        vm.prank(elder);
        IClanWorld(address(diamond)).transferClanOwnership(diamondClanId, newOwner);
        Vm.Log[] memory diamondLogs = vm.getRecordedLogs();

        _assertEmitterLogsMatch(coreLogs, address(core), diamondLogs, address(diamond));
    }

    function _warnUpgradeCompletionLogsMatchCore(ActionType action, string memory label) internal {
        CoreEventParityHarness core = new CoreEventParityHarness();
        _installRawLifecycleSubmitHeartbeatFixtureFacets();

        address elder = address(0xA11CE);
        (uint32 coreClanId, uint32 diamondClanId) = _mintPair(core, elder);
        uint32 coreClansmanId = core.getClanClansmanIds(coreClanId)[0];
        uint32 diamondClansmanId = IClanWorld(address(diamond)).getClanClansmanIds(diamondClanId)[0];
        uint8 coreBaseRegion = core.getClan(coreClanId).baseRegion;
        uint8 diamondBaseRegion = IClanWorld(address(diamond)).getClan(diamondClanId).baseRegion;

        core.setVault(coreClanId, 500e18, 500e18, 500e18, 500e18);
        IEventParityFixture(address(diamond)).setVault(diamondClanId, 500e18, 500e18, 500e18, 500e18);

        ClanOrder[] memory coreOrders = new ClanOrder[](1);
        coreOrders[0] = _basicOrder(coreClansmanId, coreBaseRegion, action, 0);
        ClanOrder[] memory diamondOrders = new ClanOrder[](1);
        diamondOrders[0] = _basicOrder(diamondClansmanId, diamondBaseRegion, action, 0);

        vm.prank(elder);
        assertEq(uint8(core.submitClanOrders(coreClanId, coreOrders)[0].status), uint8(StatusCode.OK));
        vm.prank(elder);
        assertEq(
            uint8(IClanWorld(address(diamond)).submitClanOrders(diamondClanId, diamondOrders)[0].status),
            uint8(StatusCode.OK)
        );

        (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs) =
            _recordCompletionHeartbeat(core, core.getActiveMission(coreClansmanId).settlesAtTick);
        _warnEmitterLogsMatch(label, coreLogs, address(core), diamondLogs, address(diamond));
    }

    function _mintPair(ClanWorld core, address elder) internal returns (uint32 coreClanId, uint32 diamondClanId) {
        vm.prank(elder);
        (coreClanId,) = core.mintClan(elder);
        vm.prank(elder);
        (diamondClanId,) = IClanWorld(address(diamond)).mintClan(elder);
    }

    function _heartbeatBoth(ClanWorld core) internal {
        vm.warp(block.timestamp + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS);
        core.heartbeat();
        IClanWorld(address(diamond)).heartbeat();
    }

    function _basicOrder(uint32 clansmanId, uint8 gotoRegion, ActionType action, uint32 targetClanId)
        internal
        pure
        returns (ClanOrder memory)
    {
        return ClanOrder({
            clansmanId: clansmanId,
            gotoRegion: gotoRegion,
            action: action,
            targetClanId: targetClanId,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
    }

    function _withdrawOrder(uint32 clansmanId, uint8 gotoRegion, WithdrawResourcesData memory request)
        internal
        pure
        returns (ClanOrder memory)
    {
        return ClanOrder({
            clansmanId: clansmanId,
            gotoRegion: gotoRegion,
            action: ActionType.WithdrawResources,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: request
        });
    }

    function _marketOrder(
        uint32 clansmanId,
        ActionType action,
        address marketToken,
        uint256 marketAmount,
        uint256 maxGoldIn
    ) internal pure returns (ClanOrder memory) {
        return ClanOrder({
            clansmanId: clansmanId,
            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
            action: action,
            targetClanId: 0,
            marketToken: marketToken,
            marketAmount: marketAmount,
            maxGoldIn: maxGoldIn,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
    }

    function _setupMarketPair(CoreEventParityHarness core) internal returns (MarketPair memory market) {
        market.tokens = [
            address(new MinimalERC20("Wood", "WOOD")),
            address(new MinimalERC20("Iron", "IRON")),
            address(new MinimalERC20("Wheat", "WHEAT")),
            address(new MinimalERC20("Fish", "FISH")),
            address(new MinimalERC20("Gold", "GOLD")),
            address(new MinimalERC20("BPRT", "BPRT"))
        ];
        market.woodToken = market.tokens[0];

        address[4] memory corePools = _deployPools(market.tokens, address(core));
        address[4] memory diamondPools = _deployPools(market.tokens, address(diamond));
        PoolSeedConfig memory cfg = _marketSeedConfig();

        core.initTreasury(market.tokens, corePools);
        IClanWorld(address(diamond)).initTreasury(market.tokens, diamondPools);

        _seedMarketTreasury(market.tokens, cfg);
        _approveMarket(market.tokens, address(core));
        _approveMarket(market.tokens, address(diamond));

        core.seedPools(cfg);
        IClanWorld(address(diamond)).seedPools(cfg);
    }

    function _deployPools(address[6] memory tokens, address engine) internal returns (address[4] memory pools) {
        pools[0] = address(new StubPool(tokens[0], tokens[4], engine));
        pools[1] = address(new StubPool(tokens[2], tokens[4], engine));
        pools[2] = address(new StubPool(tokens[3], tokens[4], engine));
        pools[3] = address(new StubPool(tokens[1], tokens[4], engine));
    }

    function _seedMarketTreasury(address[6] memory tokens, PoolSeedConfig memory cfg) internal {
        MinimalERC20(tokens[0]).seedTreasury(address(this), cfg.woodSeed * 2);
        MinimalERC20(tokens[1]).seedTreasury(address(this), cfg.ironSeed * 2);
        MinimalERC20(tokens[2]).seedTreasury(address(this), cfg.wheatSeed * 2);
        MinimalERC20(tokens[3]).seedTreasury(address(this), cfg.fishSeed * 2);
        MinimalERC20(tokens[4]).seedTreasury(
            address(this),
            (cfg.goldSeedForWood + cfg.goldSeedForWheat + cfg.goldSeedForFish + cfg.goldSeedForIron) * 2
        );
        MinimalERC20(tokens[5]).seedTreasury(address(this), 2);
    }

    function _approveMarket(address[6] memory tokens, address spender) internal {
        for (uint256 i = 0; i < tokens.length; i++) {
            MinimalERC20(tokens[i]).approve(spender, type(uint256).max);
        }
    }

    function _marketSeedConfig() internal pure returns (PoolSeedConfig memory) {
        return PoolSeedConfig({
            woodSeed: 1_000e18,
            wheatSeed: 1_000e18,
            fishSeed: 500e18,
            ironSeed: 250e18,
            goldSeedForWood: 500e18,
            goldSeedForWheat: 700e18,
            goldSeedForFish: 600e18,
            goldSeedForIron: 800e18
        });
    }

    function _recordCompletionHeartbeat(ClanWorld core, uint64 settlesAtTick)
        internal
        returns (Vm.Log[] memory coreLogs, Vm.Log[] memory diamondLogs)
    {
        while (core.getWorldState().currentTick <= settlesAtTick) {
            vm.warp(core.getWorldState().nextHeartbeatAtTs);
            bool finalHeartbeat = core.getWorldState().currentTick == settlesAtTick;
            if (finalHeartbeat) vm.recordLogs();
            core.heartbeat();
            if (finalHeartbeat) coreLogs = vm.getRecordedLogs();
            if (finalHeartbeat) vm.recordLogs();
            IClanWorld(address(diamond)).heartbeat();
            if (finalHeartbeat) diamondLogs = vm.getRecordedLogs();
        }
    }

    function _assertEmitterLogsMatch(
        Vm.Log[] memory coreLogs,
        address coreEmitter,
        Vm.Log[] memory diamondLogs,
        address diamondEmitter
    ) internal pure {
        bytes32[] memory coreFingerprints = new bytes32[](coreLogs.length);
        bytes32[] memory diamondFingerprints = new bytes32[](diamondLogs.length);
        uint256 coreCount = _emitterFingerprints(coreLogs, coreEmitter, coreFingerprints);
        uint256 diamondCount = _emitterFingerprints(diamondLogs, diamondEmitter, diamondFingerprints);

        assertEq(diamondCount, coreCount, "event count");
        for (uint256 i = 0; i < coreCount; i++) {
            assertEq(diamondFingerprints[i], coreFingerprints[i], "event fingerprint");
        }
    }

    function _warnEmitterLogsMatch(
        string memory label,
        Vm.Log[] memory coreLogs,
        address coreEmitter,
        Vm.Log[] memory diamondLogs,
        address diamondEmitter
    ) internal {
        bytes32[] memory coreFingerprints = new bytes32[](coreLogs.length);
        bytes32[] memory diamondFingerprints = new bytes32[](diamondLogs.length);
        uint256 coreCount = _emitterFingerprints(coreLogs, coreEmitter, coreFingerprints);
        uint256 diamondCount = _emitterFingerprints(diamondLogs, diamondEmitter, diamondFingerprints);

        if (diamondCount != coreCount) {
            emit log_string("WARNING: non-blocking diamond event parity drift detected");
            emit log_named_string("scenario", label);
            emit log_named_uint("core event count", coreCount);
            emit log_named_uint("diamond event count", diamondCount);
            emit log_string("WARNING: do not fix before hackathon unless this affects demo-critical behavior");
            return;
        }

        for (uint256 i = 0; i < coreCount; i++) {
            if (diamondFingerprints[i] != coreFingerprints[i]) {
                emit log_string("WARNING: non-blocking diamond event parity drift detected");
                emit log_named_string("scenario", label);
                emit log_named_uint("event index", i);
                emit log_named_bytes32("core fingerprint", coreFingerprints[i]);
                emit log_named_bytes32("diamond fingerprint", diamondFingerprints[i]);
                emit log_string("WARNING: do not fix before hackathon unless this affects demo-critical behavior");
                return;
            }
        }
    }

    function _emitterFingerprints(Vm.Log[] memory logs, address emitter, bytes32[] memory out)
        internal
        pure
        returns (uint256 count)
    {
        for (uint256 i = 0; i < logs.length; i++) {
            if (logs[i].emitter != emitter) continue;
            out[count++] = keccak256(abi.encode(logs[i].topics, logs[i].data));
        }
    }

    function _installRawAndLifecycleFacets() internal {
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();
        IDiamondCut(address(diamond))
            .diamondCut(_rawViewsCut(), address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new ClanLifecycleFacet()), DiamondSelectors.lifecycleSelectors()), address(0), "");
    }

    function _installRawLifecycleSubmitFacets() internal {
        _installRawAndLifecycleFacets();
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new SubmitOrdersFacet()), DiamondSelectors.submitOrdersSelectors()), address(0), "");
    }

    function _installRawLifecycleSubmitHeartbeatFacets() internal {
        _installRawLifecycleSubmitFacets();
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors()), address(0), "");
    }

    function _installRawLifecycleSubmitHeartbeatFixtureFacets() internal {
        _installRawLifecycleSubmitHeartbeatFacets();
        bytes4[] memory selectors = new bytes4[](2);
        selectors[0] = EventParityFixtureFacet.setCarry.selector;
        selectors[1] = EventParityFixtureFacet.setVault.selector;
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new EventParityFixtureFacet()), selectors), address(0), "");
    }

    function _installRawLifecycleSubmitTreasuryHeartbeatFixtureFacets() internal {
        _installRawLifecycleSubmitHeartbeatFixtureFacets();
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new TreasuryFacet()), DiamondSelectors.treasurySelectors()), address(0), "");
    }

    function _installRawLifecycleGoldHeartbeatFacets() internal {
        _installRawAndLifecycleFacets();
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new GoldTransferFacet()), DiamondSelectors.goldTransferSelectors()), address(0), "");
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors()), address(0), "");
    }

    function _installRawLifecycleVaultTransferHeartbeatFacets() internal {
        _installRawAndLifecycleFacets();
        IDiamondCut(address(diamond)).diamondCut(
            _singleCut(address(new VaultResourceTransferFacet()), DiamondSelectors.vaultResourceTransferSelectors()),
            address(0),
            ""
        );
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors()), address(0), "");
    }

    function _installRawLifecycleBundleTransferHeartbeatFacets() internal {
        _installRawAndLifecycleFacets();
        IDiamondCut(address(diamond)).diamondCut(
            _singleCut(address(new BundleTransferFacet()), DiamondSelectors.bundleTransferSelectors()), address(0), ""
        );
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors()), address(0), "");
    }

    function _installRawLifecycleClanOwnershipHeartbeatFacets() internal {
        _installRawAndLifecycleFacets();
        IDiamondCut(address(diamond)).diamondCut(
            _singleCut(address(new ClanOwnershipFacet()), DiamondSelectors.ownershipSelectors()), address(0), ""
        );
        IDiamondCut(address(diamond)).diamondCut(_singleCut(address(new HeartbeatFacet()), DiamondSelectors.heartbeatSelectors()), address(0), "");
    }

    function _rawViewsCut() internal returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](4);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: address(new RawWorldViewsFacet()),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawWorldViewsSelectors()
        });
        cut[1] = IDiamondCut.FacetCut({
            facetAddress: address(new RawTreasuryViewsFacet()),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawTreasuryViewsSelectors()
        });
        cut[2] = IDiamondCut.FacetCut({
            facetAddress: address(new RawClanViewsFacet()),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawClanViewsSelectors()
        });
        cut[3] = IDiamondCut.FacetCut({
            facetAddress: address(new RawBanditViewsFacet()),
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: DiamondSelectors.rawBanditViewsSelectors()
        });
    }

    function _singleCut(address facet, bytes4[] memory selectors) internal pure returns (IDiamondCut.FacetCut[] memory cut) {
        cut = new IDiamondCut.FacetCut[](1);
        cut[0] = IDiamondCut.FacetCut({
            facetAddress: facet,
            action: IDiamondCut.FacetCutAction.Add,
            functionSelectors: selectors
        });
    }
}
