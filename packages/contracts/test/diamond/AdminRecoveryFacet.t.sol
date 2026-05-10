// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {DiamondSelectors} from "../../script/DiamondSelectors.sol";
import {Diamond} from "../../src/diamond/Diamond.sol";
import {ClanWorldDiamondInit} from "../../src/diamond/ClanWorldDiamondInit.sol";
import {IDiamondCut} from "../../src/diamond/IDiamondCut.sol";
import {AdminRecoveryFacet} from "../../src/diamond/facets/AdminRecoveryFacet.sol";
import {ClanLifecycleFacet} from "../../src/diamond/facets/ClanLifecycleFacet.sol";
import {DiamondCutFacet} from "../../src/diamond/facets/DiamondCutFacet.sol";
import {RawClanViewsFacet} from "../../src/diamond/facets/RawClanViewsFacet.sol";
import {RawTreasuryViewsFacet} from "../../src/diamond/facets/RawTreasuryViewsFacet.sol";
import {RawWorldViewsFacet} from "../../src/diamond/facets/RawWorldViewsFacet.sol";
import {SubmitOrdersFacet} from "../../src/diamond/facets/SubmitOrdersFacet.sol";
import {LibOrderDefenders} from "../../src/diamond/lib/LibOrderDefenders.sol";
import {LibOrderUpgrades} from "../../src/diamond/lib/LibOrderUpgrades.sol";
import {LibStorage} from "../../src/diamond/lib/LibStorage.sol";
import {
    ActionType,
    Clan,
    ClanState,
    Clansman,
    ClansmanState,
    IClanWorld,
    Mission,
    ScheduledMarketAction
} from "../../src/IClanWorld.sol";

interface IAdminRecoveryHarness {
    function setCurrentTick(uint64 tick) external;
    function markClansmanDead(uint32 clansmanId) external;
    function markClanDeadFromAllClansmen(uint32 clanId) external;
    function markClanDeadOtherPath(uint32 clanId) external;
    function dirtyClansmanState(uint32 clansmanId) external;
    function setDefenderMission(uint32 clansmanId, uint32 targetClanId) external;
    function reserveWallUpgrade(uint32 clansmanId) external;
    function setScheduledMarketMission(uint32 clansmanId, uint64 settlesAtTick) external;
    function pendingWallUpgrades(uint32 clanId) external view returns (uint8);
    function reservedWood(uint32 clanId) external view returns (uint256);
    function reservedIron(uint32 clanId) external view returns (uint256);
}

contract AdminRecoveryHarnessFacet {
    function setCurrentTick(uint64 tick) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        s.world.currentTick = tick;
        s.world.nextHeartbeatAtTick = tick + 1;
        s.world.currentTickSeed = bytes32(uint256(tick));
        s.tickSeeds[tick] = bytes32(uint256(tick));
    }

    function markClansmanDead(uint32 clansmanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Clansman storage cs = s.clansmen[clansmanId];
        if (cs.state != ClansmanState.DEAD && s.clans[cs.clanId].livingClansmen > 0) {
            s.clans[cs.clanId].livingClansmen -= 1;
        }
        cs.state = ClansmanState.DEAD;
    }

    function markClanDeadFromAllClansmen(uint32 clanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint32[] storage ids = s.clanClansmanIds[clanId];
        for (uint256 i = 0; i < ids.length; i++) {
            s.clansmen[ids[i]].state = ClansmanState.DEAD;
        }
        s.clans[clanId].livingClansmen = 0;
        s.clans[clanId].clanState = ClanState.DEAD;
        s.clans[clanId].coldDamage = 2;
    }

    function markClanDeadOtherPath(uint32 clanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint32 clansmanId = s.clanClansmanIds[clanId][0];
        s.clansmen[clansmanId].state = ClansmanState.DEAD;
        s.clans[clanId].clanState = ClanState.DEAD;
        s.clans[clanId].livingClansmen = 3;
        s.clans[clanId].coldDamage = 2;
    }

    function dirtyClansmanState(uint32 clansmanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Clansman storage cs = s.clansmen[clansmanId];
        cs.currentRegion = 8;
        cs.cooldownEndsAtTs = 123;
        cs.lastMissionNonce = 7;
        cs.carryWood = 1e18;
        cs.carryIron = 2e18;
        cs.carryWheat = 3e18;
        cs.carryFish = 4e18;
        Mission storage mission = s.missions[clansmanId];
        mission.active = true;
        mission.nonce = 7;
        mission.submittedAtTick = 1;
        mission.executesAtTick = 2;
        mission.settlesAtTick = 9;
        mission.clansmanId = clansmanId;
        mission.startRegion = 1;
        mission.targetRegion = 8;
        mission.action = ActionType.FishDeepSea;
        mission.startTick = 1;
        mission.arrivalTick = 2;
        mission.actionStartTick = 3;
        mission.missionSeed = bytes32(uint256(99));
    }

    function setDefenderMission(uint32 clansmanId, uint32 targetClanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Clansman storage cs = s.clansmen[clansmanId];
        uint8 region = s.clans[targetClanId].baseRegion;
        cs.state = ClansmanState.DEAD;
        cs.currentRegion = region;
        Mission storage mission = s.missions[clansmanId];
        mission.active = true;
        mission.clansmanId = clansmanId;
        mission.action = ActionType.DefendBase;
        mission.targetClanId = targetClanId;
        LibOrderDefenders.registerDefender(s, region, cs.clanId, clansmanId);
        if (s.clans[cs.clanId].livingClansmen > 0) s.clans[cs.clanId].livingClansmen -= 1;
    }

    function reserveWallUpgrade(uint32 clansmanId) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Clansman storage cs = s.clansmen[clansmanId];
        Clan storage clan = s.clans[cs.clanId];
        clan.wallLevel = 2;
        LibOrderUpgrades.reserveWallUpgrade(s, clan, clan.clanId, clansmanId, 7);
        s.missions[clansmanId].active = true;
        s.missions[clansmanId].action = ActionType.UpgradeWall;
        s.missions[clansmanId].nonce = 7;
        cs.state = ClansmanState.DEAD;
        if (clan.livingClansmen > 0) clan.livingClansmen -= 1;
    }

    function setScheduledMarketMission(uint32 clansmanId, uint64 settlesAtTick) external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Clansman storage cs = s.clansmen[clansmanId];
        cs.state = ClansmanState.DEAD;
        cs.lastMissionNonce = 1;
        if (s.clans[cs.clanId].livingClansmen > 0) s.clans[cs.clanId].livingClansmen -= 1;

        Mission storage mission = s.missions[clansmanId];
        mission.active = true;
        mission.nonce = 1;
        mission.settlesAtTick = settlesAtTick;
        mission.clansmanId = clansmanId;
        mission.action = ActionType.MarketSell;

        s.marketMissionCommitSequence[clansmanId] = 9;
        s.scheduledMarketActions[settlesAtTick].push(
            ScheduledMarketAction({
                executeAtTick: settlesAtTick,
                commitSequence: 9,
                missionNonce: 1,
                clanId: cs.clanId,
                clansmanId: clansmanId,
                action: ActionType.MarketSell,
                marketToken: address(0xBEEF),
                marketAmount: 1e18,
                maxGoldIn: 0
            })
        );
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

contract AdminRecoveryFacetTest is Test {
    event ClansmanRevived(uint32 indexed clanId, uint32 indexed clansmanId, uint64 atTick);
    event ResourcesInjected(
        uint32 indexed clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish, uint64 atTick
    );

    IClanWorld private world;
    IAdminRecoveryHarness private harness;
    address private elder = address(0xA11CE);
    address private stranger = address(0xB0B);

    function setUp() public {
        DiamondCutFacet cutFacet = new DiamondCutFacet();
        Diamond diamond = new Diamond(address(this), address(cutFacet));
        ClanWorldDiamondInit init = new ClanWorldDiamondInit();

        IDiamondCut.FacetCut[] memory cut = new IDiamondCut.FacetCut[](7);
        cut[0] = _facetCut(address(new RawWorldViewsFacet()), DiamondSelectors.rawWorldViewsSelectors());
        cut[1] = _facetCut(address(new RawTreasuryViewsFacet()), DiamondSelectors.rawTreasuryViewsSelectors());
        cut[2] = _facetCut(address(new RawClanViewsFacet()), DiamondSelectors.rawClanViewsSelectors());
        cut[3] = _facetCut(address(new ClanLifecycleFacet()), DiamondSelectors.lifecycleSelectors());
        cut[4] = _facetCut(address(new SubmitOrdersFacet()), DiamondSelectors.submitOrdersSelectors());
        cut[5] = _facetCut(address(new AdminRecoveryFacet()), DiamondSelectors.adminRecoverySelectors());
        cut[6] = _facetCut(address(new AdminRecoveryHarnessFacet()), _harnessSelectors());

        IDiamondCut(address(diamond)).diamondCut(cut, address(init), abi.encodeCall(ClanWorldDiamondInit.init, ()));
        world = IClanWorld(address(diamond));
        harness = IAdminRecoveryHarness(address(diamond));
    }

    function test_reviveClansman_ownerOnly() public {
        uint32 clanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        harness.markClansmanDead(clansmanId);

        vm.prank(stranger);
        vm.expectRevert();
        world.reviveClansman(clansmanId);
    }

    function test_reviveClansman_resetsStateAndEmits() public {
        uint32 clanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        uint8 baseRegion = world.getClan(clanId).baseRegion;
        harness.setCurrentTick(42);
        harness.markClansmanDead(clansmanId);
        harness.dirtyClansmanState(clansmanId);

        vm.expectEmit(true, true, false, true);
        emit ClansmanRevived(clanId, clansmanId, 42);
        world.reviveClansman(clansmanId);

        Clansman memory cs = world.getClansman(clansmanId);
        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "waiting");
        assertEq(cs.currentRegion, baseRegion, "base region");
        assertEq(cs.cooldownEndsAtTs, 0, "cooldown");
        assertEq(cs.lastMissionNonce, 42, "nonce bumped");
        assertEq(cs.carryWood, 0, "wood carry");
        assertEq(cs.carryIron, 0, "iron carry");
        assertEq(cs.carryWheat, 0, "wheat carry");
        assertEq(cs.carryFish, 0, "fish carry");
        assertFalse(world.getActiveMission(clansmanId).active, "mission cleared");
        assertEq(world.getClan(clanId).livingClansmen, 4, "living restored");
        assertEq(world.getClan(clanId).coldDamage, 0, "cold reset");
    }

    function test_reviveDeadClansmen_revivesDeadClan() public {
        uint32 clanId = _mint();
        uint32[] memory ids = world.getClanClansmanIds(clanId);
        harness.markClanDeadFromAllClansmen(clanId);

        world.reviveDeadClansmen(clanId);

        assertEq(uint8(world.getClan(clanId).clanState), uint8(ClanState.ACTIVE), "active");
        assertEq(world.getClan(clanId).livingClansmen, ids.length, "all living");
        assertEq(world.getClan(clanId).coldDamage, 0, "cold reset");
        for (uint256 i = 0; i < ids.length; i++) {
            assertEq(uint8(world.getClansman(ids[i]).state), uint8(ClansmanState.WAITING), "revived");
        }
    }

    function test_reviveClansman_doesNotReactivateOtherDeathPath() public {
        uint32 clanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        harness.markClanDeadOtherPath(clanId);

        world.reviveClansman(clansmanId);

        assertEq(uint8(world.getClan(clanId).clanState), uint8(ClanState.DEAD), "other death remains dead");
        assertEq(world.getClan(clanId).livingClansmen, 4, "living increments");
        assertEq(world.getClan(clanId).coldDamage, 0, "cold reset");
    }

    function test_reviveClansman_clearsDefenderRegistry() public {
        uint32 defenderClanId = _mint();
        uint32 targetClanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(defenderClanId)[0];
        harness.setDefenderMission(clansmanId, targetClanId);

        assertEq(world.getActiveDefenders(targetClanId).length, 1, "registered");
        assertGt(world.getClansmanDefendingRegion(clansmanId), 0, "defending");

        world.reviveClansman(clansmanId);

        assertEq(world.getActiveDefenders(targetClanId).length, 0, "active defenders cleared");
        assertEq(world.getClansmanDefendingRegion(clansmanId), 0, "defending region cleared");
        assertEq(world.getActiveMission(clansmanId).targetClanId, 0, "target cleared");
    }

    function test_reviveClansman_releasesWallReservation() public {
        uint32 clanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        harness.reserveWallUpgrade(clansmanId);

        assertEq(harness.pendingWallUpgrades(clanId), 1, "pending");
        assertGt(harness.reservedWood(clanId), 0, "reserved wood");
        assertGt(harness.reservedIron(clanId), 0, "reserved iron");

        world.reviveClansman(clansmanId);

        assertEq(harness.pendingWallUpgrades(clanId), 0, "pending cleared");
        assertEq(harness.reservedWood(clanId), 0, "wood released");
        assertEq(harness.reservedIron(clanId), 0, "iron released");
    }

    function test_reviveClansman_purgesStaleScheduledMarketActionBeforeNonceBump() public {
        uint32 clanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        harness.setCurrentTick(55);
        harness.setScheduledMarketMission(clansmanId, 80);
        assertEq(world.getScheduledMarketActionsForTick(80).length, 1, "queued");

        world.reviveClansman(clansmanId);

        assertEq(world.getScheduledMarketActionsForTick(80).length, 0, "purged");
        assertEq(world.getClansman(clansmanId).lastMissionNonce, 55, "nonce bumped after purge");
    }

    function test_reviveDeadClansmen_purgesBulkScheduledMarketActions() public {
        uint32 clanId = _mint();
        uint32 clansmanId = world.getClanClansmanIds(clanId)[0];
        harness.setCurrentTick(60);
        harness.setScheduledMarketMission(clansmanId, 90);

        world.reviveDeadClansmen(clanId);

        assertEq(world.getScheduledMarketActionsForTick(90).length, 0, "bulk purged");
        assertEq(world.getClansman(clansmanId).lastMissionNonce, 60, "nonce bumped");
    }

    function test_injectClanResources_ownerOnly() public {
        uint32 clanId = _mint();

        vm.prank(stranger);
        vm.expectRevert();
        world.injectClanResources(clanId, 1, 2, 3, 4);
    }

    function test_injectClanResources_addsVaultResourcesForDeadClanAndEmits() public {
        uint32 clanId = _mint();
        harness.markClanDeadFromAllClansmen(clanId);
        Clan memory beforeClan = world.getClan(clanId);

        vm.expectEmit(true, false, false, true);
        emit ResourcesInjected(clanId, 1e18, 2e18, 3e18, 4e18, 0);
        world.injectClanResources(clanId, 1e18, 2e18, 3e18, 4e18);

        Clan memory afterClan = world.getClan(clanId);
        assertEq(afterClan.vaultWood, beforeClan.vaultWood + 1e18, "wood");
        assertEq(afterClan.vaultIron, beforeClan.vaultIron + 2e18, "iron");
        assertEq(afterClan.vaultWheat, beforeClan.vaultWheat + 3e18, "wheat");
        assertEq(afterClan.vaultFish, beforeClan.vaultFish + 4e18, "fish");
        assertEq(afterClan.goldBalance, beforeClan.goldBalance, "gold");
        assertEq(afterClan.blueprintBalance, beforeClan.blueprintBalance, "blueprint");
        assertEq(uint8(afterClan.clanState), uint8(ClanState.DEAD), "still dead");
    }

    function test_injectClanResources_overflowReverts() public {
        uint32 clanId = _mint();
        uint256 max = type(uint256).max;
        world.injectClanResources(clanId, max - world.getClan(clanId).vaultWood, 0, 0, 0);

        vm.expectRevert();
        world.injectClanResources(clanId, 1, 0, 0, 0);
    }

    function test_adminRecoverySelectors() public {
        bytes4[] memory selectors = DiamondSelectors.adminRecoverySelectors();
        assertEq(selectors.length, 3, "selector count");
        assertEq(selectors[0], AdminRecoveryFacet.reviveDeadClansmen.selector, "bulk selector");
        assertEq(selectors[1], AdminRecoveryFacet.reviveClansman.selector, "single selector");
        assertEq(selectors[2], AdminRecoveryFacet.injectClanResources.selector, "inject selector");
    }

    function _mint() private returns (uint32 clanId) {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _facetCut(address facet, bytes4[] memory selectors) private pure returns (IDiamondCut.FacetCut memory) {
        return IDiamondCut.FacetCut({
            facetAddress: facet, action: IDiamondCut.FacetCutAction.Add, functionSelectors: selectors
        });
    }

    function _harnessSelectors() private pure returns (bytes4[] memory selectors) {
        selectors = new bytes4[](11);
        selectors[0] = AdminRecoveryHarnessFacet.setCurrentTick.selector;
        selectors[1] = AdminRecoveryHarnessFacet.markClansmanDead.selector;
        selectors[2] = AdminRecoveryHarnessFacet.markClanDeadFromAllClansmen.selector;
        selectors[3] = AdminRecoveryHarnessFacet.markClanDeadOtherPath.selector;
        selectors[4] = AdminRecoveryHarnessFacet.dirtyClansmanState.selector;
        selectors[5] = AdminRecoveryHarnessFacet.setDefenderMission.selector;
        selectors[6] = AdminRecoveryHarnessFacet.reserveWallUpgrade.selector;
        selectors[7] = AdminRecoveryHarnessFacet.setScheduledMarketMission.selector;
        selectors[8] = AdminRecoveryHarnessFacet.pendingWallUpgrades.selector;
        selectors[9] = AdminRecoveryHarnessFacet.reservedWood.selector;
        selectors[10] = AdminRecoveryHarnessFacet.reservedIron.selector;
    }
}
