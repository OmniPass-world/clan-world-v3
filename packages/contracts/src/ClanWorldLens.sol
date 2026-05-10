// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    IClanWorld,
    ClanWorldConstants,
    ClansmanState,
    ActionType,
    ResourceType,
    Clan,
    Clansman,
    Mission,
    BanditState,
    BanditTroop,
    WheatPlot,
    WorldState,
    LeaderboardEntry,
    DerivedClanState,
    DerivedClansmanState,
    WorldSnapshot,
    ClansmanFullView,
    ClanFullView,
    PoolReserves,
    MarketState,
    ActiveBanditView,
    RegionOccupant
} from "./IClanWorld.sol";
import {IClanWorldLens} from "./IClanWorldLens.sol";
import {LibScoring} from "./lib/LibScoring.sol";
import {LibTravel} from "./lib/LibTravel.sol";
import {StubPool} from "./StubPool.sol";

/// @notice Stateless reader contract for convenience views that should not live
///         in the core game bytecode long term.
contract ClanWorldLens is IClanWorldLens {
    IClanWorld public immutable override world;

    error ClanWorldLensZeroWorld();

    constructor(IClanWorld world_) {
        if (address(world_) == address(0)) {
            revert ClanWorldLensZeroWorld();
        }
        world = world_;
    }

    function getDerivedClanState(uint32 clanId) external view override returns (DerivedClanState memory) {
        return world.getDerivedClanState(clanId);
    }

    function getDerivedClansmanState(uint32 clansmanId) external view override returns (DerivedClansmanState memory) {
        return world.getDerivedClansmanState(clansmanId);
    }

    function getBanditTargetPreview(uint32 banditId) external view override returns (uint32 previewClanId) {
        return world.getBanditTargetPreview(banditId);
    }

    function quoteTravel(uint8 srcRegion, uint8 dstRegion)
        external
        pure
        override
        returns (uint8 travelTicks, bytes8 path)
    {
        return LibTravel.quoteTravel(srcRegion, dstRegion);
    }

    function quoteLootValueRaw(uint32 clanId) external view override returns (uint256 lootValue) {
        return LibScoring.lootValue(world.getClan(clanId));
    }

    function quoteLootValueSettled(uint32 clanId) external view override returns (uint256 lootValue) {
        return world.quoteLootValueSettled(clanId);
    }

    function getClanScore(uint32 clanId)
        external
        view
        override
        returns (uint256 score, uint64 monumentReachedAtTick, uint8 monumentLevel)
    {
        return world.getClanScore(clanId);
    }

    function getRankings() external view override returns (uint32[] memory clanIdsRanked, uint256[] memory scores) {
        return world.getRankings();
    }

    function getWorldSnapshot() external view override returns (WorldSnapshot memory) {
        uint32[] memory clanIds = world.getClanIds();
        LeaderboardEntry[] memory leaderboard = new LeaderboardEntry[](clanIds.length);
        for (uint256 i = 0; i < clanIds.length; i++) {
            uint32 clanId = clanIds[i];
            Clan memory clan = world.getClan(clanId);
            leaderboard[i] = LeaderboardEntry({
                clanId: clanId,
                owner: clan.owner,
                monumentLevel: clan.monumentLevel,
                baseLevel: clan.baseLevel,
                wallLevel: clan.wallLevel,
                livingClansmen: clan.livingClansmen,
                state: clan.clanState,
                lootValue: LibScoring.lootValue(clan)
            });
        }

        return _worldSnapshotWithLeaderboard(leaderboard);
    }

    function getClanFullView(uint32 clanId) external view override returns (ClanFullView memory) {
        DerivedClanState memory derivedClan = world.getDerivedClanState(clanId);
        uint32[] memory clansmanIds = world.getClanClansmanIds(clanId);
        ClansmanFullView[] memory clansmen = new ClansmanFullView[](clansmanIds.length);
        uint32 thisClanDefendingBaseId = 0;

        for (uint256 i = 0; i < clansmanIds.length; i++) {
            DerivedClansmanState memory dcs = world.getDerivedClansmanState(clansmanIds[i]);
            Mission memory mission = dcs.activeMission;
            clansmen[i] = ClansmanFullView({clansman: dcs, activeMission: mission});
            if (
                thisClanDefendingBaseId == 0 && mission.active && mission.action == ActionType.DefendBase
                    && dcs.clansman.state == ClansmanState.ACTING
            ) {
                thisClanDefendingBaseId = mission.targetRegion;
            }
        }

        if (thisClanDefendingBaseId == 0) {
            for (uint256 i = 0; i < clansmanIds.length; i++) {
                uint8 region = world.getClansmanDefendingRegion(clansmanIds[i]);
                if (region != 0) {
                    thisClanDefendingBaseId = region;
                    break;
                }
            }
        }

        (WheatPlot memory westPlot, WheatPlot memory eastPlot) = world.getWheatPlots(clanId);
        return ClanFullView({
            clan: derivedClan,
            clansmen: clansmen,
            westPlot: westPlot,
            eastPlot: eastPlot,
            incomingDefenderIds: world.getDefendingClans(derivedClan.clan.baseRegion),
            thisClanDefendingBaseId: thisClanDefendingBaseId
        });
    }

    /// @dev This one composes entirely from raw core reads, so it is already
    ///      safe to remove from core once consumers migrate to the lens.
    function getMarketState() external view override returns (MarketState memory) {
        uint64 currentTick = world.getWorldState().currentTick;
        return MarketState({
            wood: _poolReserves(uint8(ResourceType.Wood)),
            wheat: _poolReserves(uint8(ResourceType.Wheat)),
            fish: _poolReserves(uint8(ResourceType.Fish)),
            iron: _poolReserves(uint8(ResourceType.Iron)),
            currentTick: currentTick,
            currentTickQueue: world.getScheduledMarketActionsForTick(currentTick),
            nextTickQueue: world.getScheduledMarketActionsForTick(currentTick + 1)
        });
    }

    function getActiveBanditView() external view override returns (ActiveBanditView memory) {
        uint32 activeBanditId = world.getWorldState().activeBanditId;
        BanditTroop memory bandit = world.getBandit(activeBanditId);
        uint64 nextActionTick = 0;
        uint8 maxAttemptsRemaining = 0;
        bool exists = bandit.id != ClanWorldConstants.BANDIT_ID_NULL && bandit.state != BanditState.None;

        if (exists) {
            if (bandit.state == BanditState.Spawned) {
                nextActionTick = bandit.tickEnteredState + 1;
            } else if (bandit.state == BanditState.Camped) {
                nextActionTick = bandit.tickEnteredState + ClanWorldConstants.BANDIT_CAMP_TICKS;
            }
            if (bandit.attackAttemptsMade < ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS) {
                maxAttemptsRemaining = ClanWorldConstants.BANDIT_MAX_ATTACK_ATTEMPTS - bandit.attackAttemptsMade;
            }
        }

        return ActiveBanditView({
            exists: exists,
            banditId: bandit.id,
            state: bandit.state,
            currentRegion: bandit.region,
            attackAttemptsMade: bandit.attackAttemptsMade,
            maxAttemptsRemaining: maxAttemptsRemaining,
            stateEnteredTick: bandit.tickEnteredState,
            nextActionTick: nextActionTick,
            tier: bandit.tier,
            attackPower: LibScoring.banditAttackPower(bandit.strength),
            carryWood: bandit.carryWood,
            carryIron: bandit.carryIron,
            carryWheat: bandit.carryWheat,
            carryFish: bandit.carryFish,
            projectedTargetClanId: bandit.targetClanId,
            projectedTargetLootValue: 0
        });
    }

    function getRegionPopulation(uint8 region) external view override returns (RegionOccupant[] memory) {
        uint32[] memory clanIds = world.getClanIds();
        uint256 count = 0;
        for (uint256 i = 0; i < clanIds.length; i++) {
            uint32[] memory clansmanIds = world.getClanClansmanIds(clanIds[i]);
            for (uint256 j = 0; j < clansmanIds.length; j++) {
                Clansman memory clansman = world.getClansman(clansmanIds[j]);
                if (clansman.state != ClansmanState.DEAD && clansman.currentRegion == region) {
                    count++;
                }
            }
        }

        RegionOccupant[] memory occupants = new RegionOccupant[](count);
        uint256 out = 0;
        for (uint256 i = 0; i < clanIds.length; i++) {
            uint32[] memory clansmanIds = world.getClanClansmanIds(clanIds[i]);
            for (uint256 j = 0; j < clansmanIds.length; j++) {
                uint32 clansmanId = clansmanIds[j];
                Clansman memory clansman = world.getClansman(clansmanId);
                if (clansman.state != ClansmanState.DEAD && clansman.currentRegion == region) {
                    Mission memory mission = world.getActiveMission(clansmanId);
                    occupants[out++] = RegionOccupant({
                        clansmanId: clansmanId,
                        clanId: clanIds[i],
                        state: clansman.state,
                        currentAction: mission.active ? mission.action : ActionType.None,
                        missionNonce: clansman.lastMissionNonce
                    });
                }
            }
        }

        return occupants;
    }

    function _poolReserves(uint8 resourceType) private view returns (PoolReserves memory pr) {
        address resourceToken = world.getResourceToken(resourceType);
        address poolAddr = world.getPool(resourceType);
        pr.resourceToken = resourceToken;
        if (poolAddr == address(0) || resourceToken == address(0)) {
            return pr;
        }

        (uint256 resourceReserve, uint256 goldReserve) = StubPool(poolAddr).getReserves();
        pr.resourceReserve = resourceReserve;
        pr.goldReserve = goldReserve;
        pr.spotPriceGoldPerResource = resourceReserve > 0 ? (goldReserve * 1e18) / resourceReserve : 0;
    }

    function _worldSnapshotWithLeaderboard(LeaderboardEntry[] memory leaderboard)
        private
        view
        returns (WorldSnapshot memory)
    {
        WorldState memory ws = world.getWorldState();
        return WorldSnapshot({
            currentTick: ws.currentTick,
            seasonStartTick: ws.seasonStartTick,
            seasonEndTick: ws.seasonEndTick,
            seasonFinalized: ws.seasonFinalized,
            currentSeasonNumber: ws.currentSeasonNumber,
            nextHeartbeatAtTick: ws.nextHeartbeatAtTick,
            worldPaused: ws.worldPaused,
            pausedAtTs: ws.pausedAtTs,
            winterActive: ws.winterActive,
            winterStartsAtTick: ws.winterStartsAtTick,
            winterEndsAtTick: ws.winterEndsAtTick,
            activeBanditId: ws.activeBanditId,
            currentTickSeed: ws.currentTickSeed,
            leaderboard: leaderboard
        });
    }
}
