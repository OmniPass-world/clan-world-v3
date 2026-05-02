// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ClanWorldConstants,
    Clan,
    ClanOrder,
    WithdrawResourcesData,
    ClansmanState,
    BanditState,
    BanditTroop,
    OrderResult,
    StatusCode,
    ActionType
} from "../src/IClanWorld.sol";

contract ReservationConsistencyHarness is ClanWorld {
    function setCurrentTick(uint64 tick) external {
        _world.currentTick = tick;
        _world.nextHeartbeatAtTick = tick + 1;
    }

    function setVault(uint32 clanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        _clans[clanId].vaultWood = wood;
        _clans[clanId].vaultIron = iron;
        _clans[clanId].vaultWheat = wheat;
        _clans[clanId].vaultFish = fish;
    }

    function setClanUpkeepState(
        uint32 clanId,
        uint64 lastSettledTick,
        uint256 vaultWood,
        uint256 vaultWheat,
        uint256 vaultFish
    ) external {
        Clan storage clan = _clans[clanId];
        clan.lastSettledTick = lastSettledTick;
        clan.vaultWood = vaultWood;
        clan.vaultWheat = vaultWheat;
        clan.vaultFish = vaultFish;
        clan.starvationStartsAtTick = 0;
        clan.coldDamage = 0;
    }

    function setClansmanCarry(uint32 clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        _clansmen[clansmanId].carryWood = wood;
        _clansmen[clansmanId].carryIron = iron;
        _clansmen[clansmanId].carryWheat = wheat;
        _clansmen[clansmanId].carryFish = fish;
    }

    function setClansmanWaitingAt(uint32 clansmanId, uint8 region) external {
        _clansmen[clansmanId].state = ClansmanState.WAITING;
        _clansmen[clansmanId].currentRegion = region;
        _clansmen[clansmanId].cooldownEndsAtTs = 0;
    }

    function forceCampedBandit(uint8 region, uint64 tickEnteredState) external returns (uint32 id) {
        id = _spawnBandit(region, 100);
        _bandits[id].state = BanditState.Camped;
        _bandits[id].tickEnteredState = tickEnteredState;
    }

    function advanceBanditStatesForTest(uint64 closedTick) external {
        _advanceBanditStates(closedTick);
    }

    function stealBanditVaultLootForTest(uint32 targetClanId)
        external
        returns (uint256 stolenWood, uint256 stolenIron, uint256 stolenWheat, uint256 stolenFish)
    {
        uint32 banditId = _spawnBandit(_clans[targetClanId].baseRegion, 100);
        BanditTroop storage bandit = _bandits[banditId];
        return _stealBanditVaultLoot(bandit, _clans[targetClanId]);
    }

    function reserveWallUpgradeForTest(uint32 clanId, uint32 clansmanId) external {
        _reserveWallUpgrade(_clans[clanId], clanId, clansmanId, 1);
    }
}

contract ReservationConsistencyTest is Test {
    ReservationConsistencyHarness world;
    address elder = address(0xA1);

    function setUp() public {
        world = new ReservationConsistencyHarness();
    }

    function _mintClan() internal returns (uint32 clanId) {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _csAt(uint32 clanId, uint256 index) internal view returns (uint32) {
        return world.getClanFullView(clanId).clansmen[index].clansman.clansman.clansmanId;
    }

    function _submit(uint32 clanId, uint32 csId, ActionType action) internal returns (OrderResult[] memory) {
        Clan memory clan = world.getClan(clanId);
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: clan.baseRegion,
            action: action,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
        vm.prank(elder);
        return world.submitClanOrders(clanId, orders);
    }

    function _advanceTicks(uint256 count) internal {
        for (uint256 i = 0; i < count; i++) {
            vm.warp(world.getWorldState().nextHeartbeatAtTs);
            world.heartbeat();
        }
    }

    function test_banditPicksHighestLootSettledTarget() public {
        uint32 clanA = _mintClan();
        for (uint256 i = 0; i < 5; i++) {
            _mintClan();
        }
        uint32 clanB = _mintClan();
        assertEq(world.getClan(clanA).baseRegion, world.getClan(clanB).baseRegion, "same base region");

        uint32 csId = _csAt(clanA, 0);
        world.setVault(clanA, 0, 0, 20e18, 2e18);
        world.setVault(clanB, 0, 0, 50e18, 2e18);
        world.setClansmanCarry(csId, 0, 0, 100e18, 0);

        OrderResult[] memory deposit = _submit(clanA, csId, ActionType.DepositResources);
        assertEq(uint8(deposit[0].status), uint8(StatusCode.OK), "deposit queued");

        uint64 pickTick = ClanWorldConstants.BANDIT_CAMP_TICKS;
        uint32 banditId = world.forceCampedBandit(world.getClan(clanA).baseRegion, 0);
        world.setCurrentTick(pickTick);

        world.advanceBanditStatesForTest(pickTick);

        assertEq(uint8(world.getBandit(banditId).state), uint8(BanditState.Attacking), "bandit attacking");
        assertEq(world.getBandit(banditId).targetClanId, clanA, "settled deposit wins target ranking");
    }

    function test_upgradeQueueOnePendingPerType() public {
        uint32 clanId = _mintClan();
        uint32 firstCsId = _csAt(clanId, 0);
        uint32 secondCsId = _csAt(clanId, 1);
        world.setVault(clanId, 100e18, 100e18, 100e18, 100e18);

        OrderResult[] memory first = _submit(clanId, firstCsId, ActionType.UpgradeWall);
        assertEq(uint8(first[0].status), uint8(StatusCode.OK), "first wall upgrade queued");

        OrderResult[] memory blocked = _submit(clanId, secondCsId, ActionType.UpgradeWall);
        assertEq(uint8(blocked[0].status), uint8(StatusCode.ERR_INVALID_ACTION), "second wall upgrade blocked");

        _advanceTicks(world.getActionDuration(ActionType.UpgradeWall) + 1);

        OrderResult[] memory second = _submit(clanId, secondCsId, ActionType.UpgradeWall);
        assertEq(uint8(second[0].status), uint8(StatusCode.OK), "wall upgrade can queue after first completes");
    }

    function test_reservedResourcesProtectedFromBanditTheft() public {
        uint32 clanId = _mintClan();
        uint32 csId = _csAt(clanId, 0);
        world.setVault(clanId, 100e18, 100e18, 20e18, 100e18);

        OrderResult[] memory queued = _submit(clanId, csId, ActionType.UpgradeBase);
        assertEq(uint8(queued[0].status), uint8(StatusCode.OK), "base upgrade queued");

        (,, uint256 stolenWheat,) = world.stealBanditVaultLootForTest(clanId);

        assertEq(stolenWheat, 0, "reserved wheat is not stealable");
        assertEq(world.getClan(clanId).vaultWheat, 20e18, "reserved wheat stays in vault");
    }

    function test_reservedWoodProtectedFromWinterBurn() public {
        uint32 clanId = _mintClan();
        uint32 csId = _csAt(clanId, 0);
        world.setVault(clanId, 20e18, 0, 100e18, 100e18);
        world.reserveWallUpgradeForTest(clanId, csId);

        uint64 winterStart = ClanWorldConstants.WINTER_START_TICK;
        world.setClanUpkeepState(clanId, winterStart, 20e18, 100e18, 100e18);
        world.setCurrentTick(winterStart + 1);
        world.settleClan(clanId);

        Clan memory clan = world.getClan(clanId);
        assertEq(clan.vaultWood, 20e18, "reserved wood is not burned");
        assertEq(clan.coldDamage, 1, "wood shortfall applies cold damage");
    }
}
