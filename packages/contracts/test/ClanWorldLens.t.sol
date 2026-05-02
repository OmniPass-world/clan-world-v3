// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Test} from "forge-std/Test.sol";
import {ClanWorld} from "../src/ClanWorld.sol";
import {ClanWorldLens} from "../src/ClanWorldLens.sol";
import {
    IClanWorld,
    ClanWorldConstants,
    ActionType,
    BanditState,
    ClansmanState,
    Clan,
    Clansman,
    Mission,
    WheatPlot,
    LeaderboardEntry,
    WorldSnapshot,
    ClanFullView,
    ClansmanFullView,
    DerivedClanState,
    DerivedClansmanState,
    MarketState,
    PoolReserves,
    ActiveBanditView,
    RegionOccupant
} from "../src/IClanWorld.sol";

contract ClanWorldLensHarness is ClanWorld {
    function spawnBandit(uint8 region, uint32 strength) external returns (uint32) {
        return _spawnBandit(region, strength);
    }

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

    function setMonumentLevelReachedAt(uint32 clanId, uint8 level, uint64 reachedAtTick) external {
        _clans[clanId].monumentLevel = level;
        _monumentLevelReachedAt[clanId][level] = reachedAtTick;
    }
}

contract ClanWorldLensTest is Test {
    ClanWorldLensHarness world;
    ClanWorldLens lens;
    address elder = address(0xA11CE);
    address elder2 = address(0xB0B);

    function setUp() public {
        world = new ClanWorldLensHarness();
        lens = new ClanWorldLens(IClanWorld(address(world)));
    }

    function test_constructorRejectsZeroWorld() public {
        vm.expectRevert(ClanWorldLens.ClanWorldLensZeroWorld.selector);
        new ClanWorldLens(IClanWorld(address(0)));
    }

    function test_lensWorldSnapshotMatchesCore() public {
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);
        assertGt(clanId, 0);

        WorldSnapshot memory core = world.getWorldSnapshot();
        WorldSnapshot memory fromLens = lens.getWorldSnapshot();

        _assertWorldSnapshotEq(fromLens, core);
    }

    function test_lensClanFullViewMatchesCore() public {
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);

        ClanFullView memory core = world.getClanFullView(clanId);
        ClanFullView memory fromLens = lens.getClanFullView(clanId);

        _assertClanFullViewEq(fromLens, core);
    }

    function test_lensRankingsAndScoreMatchCore() public {
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);

        (uint256 coreScore, uint64 coreReached, uint8 coreLevel) = world.getClanScore(clanId);
        (uint256 lensScore, uint64 lensReached, uint8 lensLevel) = lens.getClanScore(clanId);
        assertEq(lensScore, coreScore);
        assertEq(lensReached, coreReached);
        assertEq(lensLevel, coreLevel);

        (uint32[] memory coreRanked, uint256[] memory coreScores) = world.getRankings();
        (uint32[] memory lensRanked, uint256[] memory lensScores) = lens.getRankings();
        assertEq(lensRanked.length, coreRanked.length);
        assertEq(lensScores.length, coreScores.length);
        assertEq(lensRanked[0], coreRanked[0]);
        assertEq(lensScores[0], coreScores[0]);
    }

    function test_lensQuoteLootValueRawMatchesCore() public {
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);
        world.setVault(clanId, 100e18, 5e18, 40e18, 3e18);

        assertEq(lens.quoteLootValueRaw(clanId), world.quoteLootValueRaw(clanId));
    }

    function test_coreRawMonumentReachedAtGetter() public {
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);

        world.setMonumentLevelReachedAt(clanId, 1, 42);

        assertEq(world.getMonumentLevelReachedAt(clanId, 1), 42);
        assertEq(world.getMonumentLevelReachedAt(clanId, 2), 0);
    }

    function test_lensMarketStateMatchesCore() public view {
        MarketState memory core = world.getMarketState();
        MarketState memory fromLens = lens.getMarketState();

        _assertMarketStateEq(fromLens, core);
    }

    function test_lensBanditAndRegionViewsMatchCore() public {
        world.spawnBandit(ClanWorldConstants.REGION_FOREST, 100);

        ActiveBanditView memory coreBandit = world.getActiveBanditView();
        ActiveBanditView memory lensBandit = lens.getActiveBanditView();
        _assertActiveBanditViewEq(lensBandit, coreBandit);

        RegionOccupant[] memory corePopulation = world.getRegionPopulation(ClanWorldConstants.REGION_FOREST);
        RegionOccupant[] memory lensPopulation = lens.getRegionPopulation(ClanWorldConstants.REGION_FOREST);
        _assertRegionPopulationEq(lensPopulation, corePopulation);
    }

    function test_lensAggregatesMatchCoreAfterMultipleClansAndStateChanges() public {
        vm.prank(elder);
        (uint32 clanA,) = world.mintClan(elder);
        vm.prank(elder2);
        (uint32 clanB,) = world.mintClan(elder2);
        world.setVault(clanA, 100e18, 5e18, 40e18, 3e18);
        world.setVault(clanB, 75e18, 7e18, 50e18, 2e18);
        world.setCurrentTick(3);

        _assertWorldSnapshotEq(lens.getWorldSnapshot(), world.getWorldSnapshot());
        _assertClanFullViewEq(lens.getClanFullView(clanA), world.getClanFullView(clanA));
        _assertClanFullViewEq(lens.getClanFullView(clanB), world.getClanFullView(clanB));

        (uint32[] memory lensRanked, uint256[] memory lensScores) = lens.getRankings();
        (uint32[] memory coreRanked, uint256[] memory coreScores) = world.getRankings();
        assertEq(lensRanked.length, coreRanked.length, "ranked length");
        assertEq(lensScores.length, coreScores.length, "score length");
        for (uint256 i = 0; i < coreRanked.length; i++) {
            assertEq(lensRanked[i], coreRanked[i], "ranked clan id");
            assertEq(lensScores[i], coreScores[i], "ranked score");
        }
    }

    function test_lensViewsDoNotMutateCoreSettlementState() public {
        vm.prank(elder);
        (uint32 clanId,) = world.mintClan(elder);
        world.setCurrentTick(5);
        uint64 beforeSettled = world.getClan(clanId).lastSettledTick;
        uint32 csId = lens.getClanFullView(clanId).clansmen[0].clansman.clansman.clansmanId;
        ClansmanState beforeState = world.getClansman(csId).state;

        lens.getWorldSnapshot();
        lens.getClanFullView(clanId);
        lens.getMarketState();
        lens.getActiveBanditView();
        lens.getRegionPopulation(world.getClan(clanId).baseRegion);
        lens.quoteLootValueRaw(clanId);

        assertEq(world.getClan(clanId).lastSettledTick, beforeSettled, "lens must not settle clan");
        assertEq(uint8(world.getClansman(csId).state), uint8(beforeState), "lens must not mutate clansman");
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
            _assertLeaderboardEntryEq(actual.leaderboard[i], expected.leaderboard[i]);
        }
    }

    function _assertLeaderboardEntryEq(LeaderboardEntry memory actual, LeaderboardEntry memory expected) internal pure {
        assertEq(actual.clanId, expected.clanId, "leaderboard clanId");
        assertEq(actual.owner, expected.owner, "leaderboard owner");
        assertEq(actual.monumentLevel, expected.monumentLevel, "leaderboard monument");
        assertEq(actual.baseLevel, expected.baseLevel, "leaderboard base");
        assertEq(actual.wallLevel, expected.wallLevel, "leaderboard wall");
        assertEq(actual.livingClansmen, expected.livingClansmen, "leaderboard living");
        assertEq(uint8(actual.state), uint8(expected.state), "leaderboard state");
        assertEq(actual.lootValue, expected.lootValue, "leaderboard loot");
    }

    function _assertClanFullViewEq(ClanFullView memory actual, ClanFullView memory expected) internal pure {
        _assertDerivedClanStateEq(actual.clan, expected.clan);
        assertEq(actual.clansmen.length, expected.clansmen.length, "full clansmen length");
        for (uint256 i = 0; i < expected.clansmen.length; i++) {
            _assertClansmanFullViewEq(actual.clansmen[i], expected.clansmen[i]);
        }
        _assertWheatPlotEq(actual.westPlot, expected.westPlot, "west");
        _assertWheatPlotEq(actual.eastPlot, expected.eastPlot, "east");
        assertEq(actual.incomingDefenderIds.length, expected.incomingDefenderIds.length, "incoming defenders length");
        for (uint256 i = 0; i < expected.incomingDefenderIds.length; i++) {
            assertEq(actual.incomingDefenderIds[i], expected.incomingDefenderIds[i], "incoming defender");
        }
        assertEq(actual.thisClanDefendingBaseId, expected.thisClanDefendingBaseId, "defending base");
    }

    function _assertDerivedClanStateEq(DerivedClanState memory actual, DerivedClanState memory expected) internal pure {
        _assertClanEq(actual.clan, expected.clan);
        assertEq(actual.isStarving, expected.isStarving, "derived clan starving");
        assertEq(actual.lootValue, expected.lootValue, "derived clan loot");
        assertEq(actual.derivedAtTick, expected.derivedAtTick, "derived clan tick");
    }

    function _assertClansmanFullViewEq(ClansmanFullView memory actual, ClansmanFullView memory expected)
        internal
        pure
    {
        _assertDerivedClansmanStateEq(actual.clansman, expected.clansman);
        _assertMissionEq(actual.activeMission, expected.activeMission);
    }

    function _assertDerivedClansmanStateEq(DerivedClansmanState memory actual, DerivedClansmanState memory expected)
        internal
        pure
    {
        _assertClansmanEq(actual.clansman, expected.clansman);
        _assertMissionEq(actual.activeMission, expected.activeMission);
        assertEq(actual.effectiveRegion, expected.effectiveRegion, "effective region");
        assertEq(actual.derivedAtTick, expected.derivedAtTick, "derived clansman tick");
    }

    function _assertClanEq(Clan memory actual, Clan memory expected) internal pure {
        assertEq(actual.clanId, expected.clanId, "clan id");
        assertEq(actual.owner, expected.owner, "clan owner");
        assertEq(uint8(actual.clanState), uint8(expected.clanState), "clan state");
        assertEq(actual.baseRegion, expected.baseRegion, "clan base region");
        assertEq(actual.baseLevel, expected.baseLevel, "clan base");
        assertEq(actual.wallLevel, expected.wallLevel, "clan wall");
        assertEq(actual.monumentLevel, expected.monumentLevel, "clan monument");
        assertEq(actual.livingClansmen, expected.livingClansmen, "clan living");
        assertEq(actual.lastSettledTick, expected.lastSettledTick, "clan settled tick");
        assertEq(actual.goldBalance, expected.goldBalance, "clan gold");
        assertEq(actual.blueprintBalance, expected.blueprintBalance, "clan blueprint");
        assertEq(actual.vaultWood, expected.vaultWood, "clan wood");
        assertEq(actual.vaultIron, expected.vaultIron, "clan iron");
        assertEq(actual.vaultWheat, expected.vaultWheat, "clan wheat");
        assertEq(actual.vaultFish, expected.vaultFish, "clan fish");
    }

    function _assertClansmanEq(Clansman memory actual, Clansman memory expected) internal pure {
        assertEq(actual.clansmanId, expected.clansmanId, "clansman id");
        assertEq(actual.clanId, expected.clanId, "clansman clan");
        assertEq(uint8(actual.state), uint8(expected.state), "clansman state");
        assertEq(actual.currentRegion, expected.currentRegion, "clansman region");
        assertEq(actual.cooldownEndsAtTs, expected.cooldownEndsAtTs, "clansman cooldown");
        assertEq(actual.lastMissionNonce, expected.lastMissionNonce, "clansman nonce");
        assertEq(actual.carryWood, expected.carryWood, "clansman wood");
        assertEq(actual.carryIron, expected.carryIron, "clansman iron");
        assertEq(actual.carryWheat, expected.carryWheat, "clansman wheat");
        assertEq(actual.carryFish, expected.carryFish, "clansman fish");
    }

    function _assertMissionEq(Mission memory actual, Mission memory expected) internal pure {
        assertEq(actual.active, expected.active, "mission active");
        assertEq(actual.nonce, expected.nonce, "mission nonce");
        assertEq(actual.submittedAtTick, expected.submittedAtTick, "mission submitted");
        assertEq(actual.executesAtTick, expected.executesAtTick, "mission executes");
        assertEq(actual.settlesAtTick, expected.settlesAtTick, "mission settles");
        assertEq(actual.clansmanId, expected.clansmanId, "mission clansman");
        assertEq(actual.startRegion, expected.startRegion, "mission start");
        assertEq(actual.targetRegion, expected.targetRegion, "mission target");
        assertEq(uint8(actual.action), uint8(expected.action), "mission action");
        assertEq(actual.marketToken, expected.marketToken, "mission token");
        assertEq(actual.marketAmount, expected.marketAmount, "mission amount");
    }

    function _assertWheatPlotEq(WheatPlot memory actual, WheatPlot memory expected, string memory label) internal pure {
        assertEq(uint8(actual.state), uint8(expected.state), label);
        assertEq(actual.region, expected.region, label);
        assertEq(actual.remainingWheat, expected.remainingWheat, label);
        assertEq(actual.regrowUntilTick, expected.regrowUntilTick, label);
    }

    function _assertMarketStateEq(MarketState memory actual, MarketState memory expected) internal pure {
        _assertPoolReservesEq(actual.wood, expected.wood, "wood");
        _assertPoolReservesEq(actual.wheat, expected.wheat, "wheat");
        _assertPoolReservesEq(actual.fish, expected.fish, "fish");
        _assertPoolReservesEq(actual.iron, expected.iron, "iron");
        assertEq(actual.currentTick, expected.currentTick, "market tick");
        assertEq(actual.currentTickQueue.length, expected.currentTickQueue.length, "current queue");
        assertEq(actual.nextTickQueue.length, expected.nextTickQueue.length, "next queue");
    }

    function _assertPoolReservesEq(PoolReserves memory actual, PoolReserves memory expected, string memory label)
        internal
        pure
    {
        assertEq(actual.resourceToken, expected.resourceToken, label);
        assertEq(actual.resourceReserve, expected.resourceReserve, label);
        assertEq(actual.goldReserve, expected.goldReserve, label);
        assertEq(actual.spotPriceGoldPerResource, expected.spotPriceGoldPerResource, label);
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
        assertEq(actual.projectedTargetLootValue, expected.projectedTargetLootValue, "bandit target loot");
    }

    function _assertRegionPopulationEq(RegionOccupant[] memory actual, RegionOccupant[] memory expected)
        internal
        pure
    {
        assertEq(actual.length, expected.length, "population length");
        for (uint256 i = 0; i < expected.length; i++) {
            assertEq(actual[i].clansmanId, expected[i].clansmanId, "population clansman");
            assertEq(actual[i].clanId, expected[i].clanId, "population clan");
            assertEq(uint8(actual[i].state), uint8(expected[i].state), "population state");
            assertEq(uint8(actual[i].currentAction), uint8(expected[i].currentAction), "population action");
            assertEq(actual[i].missionNonce, expected[i].missionNonce, "population nonce");
        }
    }
}
