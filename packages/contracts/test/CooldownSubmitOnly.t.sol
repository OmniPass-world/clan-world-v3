// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {MinimalERC20} from "../src/MinimalERC20.sol";
import {StubPool} from "../src/StubPool.sol";
import {
    ClanWorldConstants,
    ActionType,
    ClansmanState,
    StatusCode,
    Clansman,
    Mission,
    ClanFullView,
    ClanOrder,
    WithdrawResourcesData,
    OrderResult,
    PoolSeedConfig
} from "../src/IClanWorld.sol";

contract CooldownSubmitOnlyHarness is ClanWorld {
    function setCarryWood(uint32 csId, uint256 wood) external {
        _clansmen[csId].carryWood = wood;
    }

    function setClansmanForTest(uint32 csId, ClansmanState state, uint8 region, uint64 cooldownEndsAtTs) external {
        _clansmen[csId].state = state;
        _clansmen[csId].currentRegion = region;
        _clansmen[csId].cooldownEndsAtTs = cooldownEndsAtTs;
    }
}

contract CooldownSubmitOnlyTest is Test {
    CooldownSubmitOnlyHarness world;
    address elder = address(0xA1);

    MinimalERC20 woodToken;
    MinimalERC20 ironToken;
    MinimalERC20 wheatToken;
    MinimalERC20 fishToken;
    MinimalERC20 goldToken;
    MinimalERC20 blueprintToken;

    function setUp() public {
        world = new CooldownSubmitOnlyHarness();
    }

    function _advanceTick() internal {
        vm.warp(world.getWorldState().nextHeartbeatAtTs);
        world.heartbeat();
    }

    function _advanceUntilCurrentTick(uint64 targetTick) internal {
        while (world.getWorldState().currentTick < targetTick) {
            _advanceTick();
        }
    }

    function _mintClan() internal returns (uint32 clanId) {
        vm.prank(elder);
        (clanId,) = world.mintClan(elder);
    }

    function _firstCs(uint32 clanId) internal view returns (uint32) {
        ClanFullView memory view_ = world.getClanFullView(clanId);
        return view_.clansmen[0].clansman.clansman.clansmanId;
    }

    function _submitOrder(uint32 clanId, uint32 csId, uint8 gotoRegion, ActionType action)
        internal
        returns (OrderResult[] memory)
    {
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
        return world.submitClanOrders(clanId, orders);
    }

    function _submitMarketBuy(uint32 clanId, uint32 csId, address token, uint256 amount, uint256 maxGold)
        internal
        returns (OrderResult[] memory)
    {
        ClanOrder[] memory orders = new ClanOrder[](1);
        orders[0] = ClanOrder({
            clansmanId: csId,
            gotoRegion: ClanWorldConstants.REGION_UNICORN_TOWN,
            action: ActionType.MarketBuy,
            targetClanId: 0,
            marketToken: token,
            marketAmount: amount,
            maxGoldIn: maxGold,
            withdrawResources: WithdrawResourcesData({wood: 0, iron: 0, wheat: 0, fish: 0})
        });
        vm.prank(elder);
        return world.submitClanOrders(clanId, orders);
    }

    function _setupMarket() internal returns (address woodAddr) {
        woodToken = new MinimalERC20("Wood", "WOOD");
        ironToken = new MinimalERC20("Iron", "IRON");
        wheatToken = new MinimalERC20("Wheat", "WHEAT");
        fishToken = new MinimalERC20("Fish", "FISH");
        goldToken = new MinimalERC20("Gold", "GOLD");
        blueprintToken = new MinimalERC20("BPRT", "BPRT");

        StubPool woodPool = new StubPool(address(woodToken), address(goldToken), address(world));
        StubPool ironPool = new StubPool(address(ironToken), address(goldToken), address(world));
        StubPool wheatPool = new StubPool(address(wheatToken), address(goldToken), address(world));
        StubPool fishPool = new StubPool(address(fishToken), address(goldToken), address(world));

        address[6] memory tokens = [
            address(woodToken),
            address(ironToken),
            address(wheatToken),
            address(fishToken),
            address(goldToken),
            address(blueprintToken)
        ];
        address[4] memory pools = [address(woodPool), address(wheatPool), address(fishPool), address(ironPool)];
        world.initTreasury(tokens, pools);

        uint256 woodSeed = world.INITIAL_WOOD_POOL_SEED();
        uint256 wheatSeed = world.INITIAL_WHEAT_POOL_SEED();
        uint256 fishSeed = world.INITIAL_FISH_POOL_SEED();
        uint256 ironSeed = world.INITIAL_IRON_POOL_SEED();
        uint256 goldForWood = world.INITIAL_GOLD_SEED_FOR_WOOD();
        uint256 goldForWheat = world.INITIAL_GOLD_SEED_FOR_WHEAT();
        uint256 goldForFish = world.INITIAL_GOLD_SEED_FOR_FISH();
        uint256 goldForIron = world.INITIAL_GOLD_SEED_FOR_IRON();
        uint256 totalGoldSeed = goldForWood + goldForWheat + goldForFish + goldForIron;

        woodToken.seedTreasury(address(this), woodSeed);
        wheatToken.seedTreasury(address(this), wheatSeed);
        fishToken.seedTreasury(address(this), fishSeed);
        ironToken.seedTreasury(address(this), ironSeed);
        goldToken.seedTreasury(address(this), totalGoldSeed);

        woodToken.approve(address(world), woodSeed);
        wheatToken.approve(address(world), wheatSeed);
        fishToken.approve(address(world), fishSeed);
        ironToken.approve(address(world), ironSeed);
        goldToken.approve(address(world), totalGoldSeed);

        PoolSeedConfig memory cfg = PoolSeedConfig({
            woodSeed: woodSeed,
            wheatSeed: wheatSeed,
            fishSeed: fishSeed,
            ironSeed: ironSeed,
            goldSeedForWood: goldForWood,
            goldSeedForWheat: goldForWheat,
            goldSeedForFish: goldForFish,
            goldSeedForIron: goldForIron
        });
        world.seedPools(cfg);
        return address(woodToken);
    }

    function test_naturalCompletionDoesNotResetCooldown() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        world.setCarryWood(csId, ClanWorldConstants.WOOD_CAP - 1e18);

        OrderResult[] memory result =
            _submitOrder(clanId, csId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood);
        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "chop wood accepted");
        uint64 submitCooldown = result[0].cooldownEndsAtTs;

        Mission memory mission = world.getActiveMission(csId);
        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
        world.settleClan(clanId);

        Clansman memory cs = world.getClansman(csId);
        assertEq(uint8(cs.state), uint8(ClansmanState.WAITING), "mission completed");
        assertEq(cs.cooldownEndsAtTs, submitCooldown, "natural completion keeps submit-time cooldown");
    }

    function test_immediateMarketSuccessSetsCooldown() public {
        address woodAddr = _setupMarket();
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        world.setClansmanForTest(csId, ClansmanState.WAITING, ClanWorldConstants.REGION_UNICORN_TOWN, 0);

        OrderResult[] memory result = _submitMarketBuy(clanId, csId, woodAddr, 1e18, 2e18);
        Clansman memory cs = world.getClansman(csId);

        assertEq(uint8(result[0].status), uint8(StatusCode.OK), "market buy accepted");
        assertEq(cs.cooldownEndsAtTs, block.timestamp + ClanWorldConstants.CLANSMAN_COOLDOWN_SECONDS, "cooldown set");
    }

    function test_chainedOrdersFluidWithoutCompletionCooldown() public {
        uint32 clanId = _mintClan();
        uint32 csId = _firstCs(clanId);
        uint8 homeRegion = world.getClan(clanId).baseRegion;
        world.setCarryWood(csId, ClanWorldConstants.WOOD_CAP - 1e18);

        OrderResult[] memory gather =
            _submitOrder(clanId, csId, ClanWorldConstants.REGION_FOREST, ActionType.ChopWood);
        assertEq(uint8(gather[0].status), uint8(StatusCode.OK), "gather accepted");

        Mission memory mission = world.getActiveMission(csId);
        _advanceUntilCurrentTick(mission.settlesAtTick + 1);
        if (block.timestamp < gather[0].cooldownEndsAtTs) {
            vm.warp(gather[0].cooldownEndsAtTs);
        }
        world.settleClan(clanId);

        OrderResult[] memory deposit = _submitOrder(clanId, csId, homeRegion, ActionType.DepositResources);
        assertEq(uint8(deposit[0].status), uint8(StatusCode.OK), "deposit should not wait on completion cooldown");
    }
}
