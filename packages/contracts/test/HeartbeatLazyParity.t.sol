// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {
    ClanWorldConstants,
    ClansmanState,
    ActionType,
    StatusCode,
    Clan,
    Clansman,
    Mission,
    ClanOrder,
    OrderResult,
    WithdrawResourcesData
} from "../src/IClanWorld.sol";

contract HeartbeatLazyParityHarness is ClanWorld {
    function setCurrentTickForTest(uint64 tick) external {
        _world.currentTick = tick;
        _world.nextHeartbeatAtTick = tick + 1;
        _world.nextHeartbeatAtTs = 0;
        _world.currentTickSeed = keccak256(abi.encode("test-seed", tick));
    }

    function setClanSettlementState(
        uint32 clanId,
        uint64 lastSettledTick,
        uint256 vaultWood,
        uint256 vaultWheat,
        uint256 vaultFish,
        uint64 starvationStartsAtTick
    ) external {
        Clan storage clan = _clans[clanId];
        clan.lastSettledTick = lastSettledTick;
        clan.vaultWood = vaultWood;
        clan.vaultWheat = vaultWheat;
        clan.vaultFish = vaultFish;
        clan.starvationStartsAtTick = starvationStartsAtTick;
    }

    function setClansmanCarry(uint32 clansmanId, uint256 wood, uint256 iron, uint256 wheat, uint256 fish) external {
        Clansman storage cs = _clansmen[clansmanId];
        cs.carryWood = wood;
        cs.carryIron = iron;
        cs.carryWheat = wheat;
        cs.carryFish = fish;
    }
}

contract HeartbeatLazyParityTest is Test {
    address elder = address(0xA1);

    function test_heartbeatAndLazyProduceIdenticalState_completingMission() public {
        uint64 closedTick = 10;
        HeartbeatLazyParityHarness heartbeatWorld = new HeartbeatLazyParityHarness();
        HeartbeatLazyParityHarness lazyWorld = new HeartbeatLazyParityHarness();

        (uint32 heartbeatClan, uint32 heartbeatCs) = _setupDepositScenario(heartbeatWorld, closedTick);
        (uint32 lazyClan, uint32 lazyCs) = _setupDepositScenario(lazyWorld, closedTick);

        heartbeatWorld.setCurrentTickForTest(closedTick);
        heartbeatWorld.heartbeat();

        lazyWorld.setCurrentTickForTest(closedTick + 1);
        lazyWorld.settleClan(lazyClan);

        _assertClanParity(heartbeatWorld.getClan(heartbeatClan), lazyWorld.getClan(lazyClan));
        _assertClansmanParity(heartbeatWorld.getClansman(heartbeatCs), lazyWorld.getClansman(lazyCs));
        _assertMissionParity(heartbeatWorld.getActiveMission(heartbeatCs), lazyWorld.getActiveMission(lazyCs));
    }

    function test_heartbeatAdvancesLastSettledTick() public {
        HeartbeatLazyParityHarness world = new HeartbeatLazyParityHarness();
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);

        uint64 closedTick = world.getWorldState().currentTick;
        world.heartbeat();

        assertEq(world.getClan(clanId).lastSettledTick, closedTick + 1, "heartbeat settles through closed tick");
    }

    function test_starvingClansmanWithCompletingMission() public {
        HeartbeatLazyParityHarness world = new HeartbeatLazyParityHarness();
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);
        uint32 csId = _firstCs(world, clanId);

        uint64 closedTick = ClanWorldConstants.WINTER_START_TICK + 1;
        uint64 travelTicks = world.getTravelTicks(world.getClan(clanId).baseRegion, ClanWorldConstants.REGION_WEST_FARMS);
        uint64 submitTick = closedTick - travelTicks - world.getActionDuration(ActionType.HarvestWheat);
        world.setCurrentTickForTest(submitTick);
        world.setClanSettlementState(clanId, submitTick, 100e18, 100e18, 100e18, 0);
        _submitOrder(world, clanId, csId, ClanWorldConstants.REGION_WEST_FARMS, ActionType.HarvestWheat);
        assertEq(world.getActiveMission(csId).settlesAtTick, closedTick, "test setup: mission completes at closed tick");

        world.setCurrentTickForTest(closedTick);
        world.setClanSettlementState(clanId, closedTick, 100e18, 0, 0, ClanWorldConstants.WINTER_START_TICK);

        world.heartbeat();

        Clan memory clan = world.getClan(clanId);
        Clansman memory cs = world.getClansman(csId);
        Mission memory mission = world.getActiveMission(csId);

        assertEq(uint8(cs.state), uint8(ClansmanState.DEAD), "starvation kills before mission settlement");
        assertFalse(mission.active, "dead clansman mission is invalidated");
        assertEq(cs.carryWheat, 0, "dead clansman does not harvest");
        assertEq(clan.livingClansmen, 3, "one clansman starved");
        assertEq(clan.lastSettledTick, closedTick + 1, "heartbeat advanced settlement cursor");
    }

    function test_heartbeatBoundedByMaxLazySettleBacklog() public {
        HeartbeatLazyParityHarness world = new HeartbeatLazyParityHarness();
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);

        uint64 closedTick = 300;
        uint64 lastSettledTick = closedTick - 250;
        world.setCurrentTickForTest(closedTick);
        world.setClanSettlementState(clanId, lastSettledTick, 1000e18, 1000e18, 1000e18, 0);

        world.heartbeat();

        assertEq(world.getClan(clanId).lastSettledTick, lastSettledTick + 200, "heartbeat caps settlement backlog");
    }

    function _setupDepositScenario(HeartbeatLazyParityHarness world, uint64 closedTick)
        internal
        returns (uint32 clanId, uint32 csId)
    {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
        csId = _firstCs(world, clanId);
        uint8 homeRegion = world.getClan(clanId).baseRegion;
        uint64 submitTick = closedTick - world.getActionDuration(ActionType.DepositResources);

        world.setCurrentTickForTest(submitTick);
        world.setClanSettlementState(clanId, submitTick, 20e18, 4e18, 1e18, 0);
        world.setClansmanCarry(csId, 0, 0, 2e18, 0);
        _submitOrder(world, clanId, csId, homeRegion, ActionType.DepositResources);
        uint64 settlesAtTick = world.getActiveMission(csId).settlesAtTick;
        assertEq(settlesAtTick, closedTick, "test setup: deposit completes at closed tick");
        world.setCurrentTickForTest(settlesAtTick);
        world.setClanSettlementState(clanId, settlesAtTick, 20e18, 4e18, 1e18, 0);
    }

    function _submitOrder(
        HeartbeatLazyParityHarness world,
        uint32 clanId,
        uint32 csId,
        uint8 gotoRegion,
        ActionType action
    ) internal returns (OrderResult[] memory results) {
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: gotoRegion,
            action: action,
            targetClanId: 0,
            marketToken: address(0),
            marketAmount: 0,
            maxGoldIn: 0,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
        vm.prank(elder);
        results = world.submitClanOrders(clanId, orders);
        assertEq(uint8(results[0].status), uint8(StatusCode.OK), "order accepted");
    }

    function _firstCs(HeartbeatLazyParityHarness world, uint32 clanId) internal view returns (uint32) {
        return world.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
    }

    function _assertClanParity(Clan memory a, Clan memory b) internal {
        assertEq(a.lastSettledTick, b.lastSettledTick, "lastSettledTick");
        assertEq(a.livingClansmen, b.livingClansmen, "livingClansmen");
        assertEq(a.coldDamage, b.coldDamage, "coldDamage");
        assertEq(a.vaultWood, b.vaultWood, "vaultWood");
        assertEq(a.vaultIron, b.vaultIron, "vaultIron");
        assertEq(a.vaultWheat, b.vaultWheat, "vaultWheat");
        assertEq(a.vaultFish, b.vaultFish, "vaultFish");
        assertEq(a.starvationStartsAtTick, b.starvationStartsAtTick, "starvationStartsAtTick");
        assertEq(uint8(a.clanState), uint8(b.clanState), "clanState");
    }

    function _assertClansmanParity(Clansman memory a, Clansman memory b) internal {
        assertEq(uint8(a.state), uint8(b.state), "clansman state");
        assertEq(a.currentRegion, b.currentRegion, "currentRegion");
        assertEq(a.cooldownEndsAtTs, b.cooldownEndsAtTs, "cooldownEndsAtTs");
        assertEq(a.carryWood, b.carryWood, "carryWood");
        assertEq(a.carryIron, b.carryIron, "carryIron");
        assertEq(a.carryWheat, b.carryWheat, "carryWheat");
        assertEq(a.carryFish, b.carryFish, "carryFish");
    }

    function _assertMissionParity(Mission memory a, Mission memory b) internal {
        assertEq(a.active, b.active, "mission active");
        assertEq(a.settlesAtTick, b.settlesAtTick, "settlesAtTick");
        assertEq(uint8(a.action), uint8(b.action), "action");
    }
}
