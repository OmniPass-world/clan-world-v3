// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    BanditState,
    BanditTroop,
    Clan,
    ClanWorldConstants,
    Clansman,
    Mission,
    ResourceType,
    ScheduledMarketAction,
    TreasuryState,
    WheatPlot,
    WorldState
} from "../../IClanWorld.sol";
import {LibSeason} from "../lib/LibSeason.sol";
import {LibStorage} from "../lib/LibStorage.sol";
import {LibTravel} from "../../lib/LibTravel.sol";
import {StubPool} from "../../StubPool.sol";

contract RawViewsFacet {
    uint64 private constant DEPOSIT_DURATION_TICKS = 1;
    uint64 private constant BUILDING_DURATION_TICKS = 1;
    uint8 private constant MONUMENT_MAX_LEVEL = 10;

    function getWorldState() external view returns (WorldState memory ws) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        ws.currentTick = s.world.currentTick;
        ws.seasonStartTick = s.world.seasonStartTick;
        ws.seasonEndTick = s.world.seasonEndTick;
        ws.seasonFinalized = s.world.seasonFinalized;
        ws.currentSeasonNumber = s.world.currentSeasonNumber;
        ws.nextHeartbeatAtTick = s.world.nextHeartbeatAtTick;
        ws.nextHeartbeatAtTs = s.world.nextHeartbeatAtTs;
        ws.nextBanditSpawnEligibleTick = s.world.nextBanditSpawnEligibleTick;
        ws.currentBanditSpawnChanceBps = s.world.currentBanditSpawnChanceBps;
        ws.currentTickSeed = s.world.currentTickSeed;
        ws.activeBanditId = s.world.activeBanditId;
        ws.nextCommitSequence = s.world.nextCommitSequence;
        (ws.winterActive, ws.winterStartsAtTick, ws.winterEndsAtTick) =
            LibSeason.winterWindowForTick(s.world.currentTick);
    }

    function getTreasuryState() external view returns (TreasuryState memory) {
        return LibStorage.appStorage().treasury;
    }

    function getResourceToken(uint8 resourceType) external view returns (address) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        if (resourceType == uint8(ResourceType.Wood)) return s.treasury.woodToken;
        if (resourceType == uint8(ResourceType.Iron)) return s.treasury.ironToken;
        if (resourceType == uint8(ResourceType.Wheat)) return s.treasury.wheatToken;
        if (resourceType == uint8(ResourceType.Fish)) return s.treasury.fishToken;
        revert("ClanWorld: invalid resource");
    }

    function getPool(uint8 resourceType) external view returns (address) {
        return _poolForResourceType(resourceType);
    }

    function getPrice(uint8 resourceType, uint256 amountIn) external view returns (uint256 amountOut) {
        amountOut = StubPool(_poolForResourceType(resourceType)).getAmountOutForExactIn(amountIn);
    }

    function getClan(uint32 clanId) external view returns (Clan memory) {
        return LibStorage.appStorage().clans[clanId];
    }

    function getClansman(uint32 clansmanId) external view returns (Clansman memory) {
        return LibStorage.appStorage().clansmen[clansmanId];
    }

    function getActiveMission(uint32 clansmanId) external view returns (Mission memory) {
        return LibStorage.appStorage().missions[clansmanId];
    }

    function getMissionTiming(uint32 clanId, uint32 clansmanId)
        external
        view
        returns (uint64 submitted, uint64 executes, uint64 settles)
    {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Mission memory m = s.missions[clansmanId];
        if (!m.active || s.clansmen[clansmanId].clanId != clanId) {
            return (0, 0, 0);
        }
        return (m.submittedAtTick, m.executesAtTick, m.settlesAtTick);
    }

    function isWinter() external view returns (bool) {
        return LibSeason.isWinterActiveAt(LibStorage.appStorage().world.currentTick);
    }

    function getWallUpgradeCost(uint8 currentLevel) external pure returns (uint256 wood, uint256 iron) {
        if (currentLevel == 0) return (20e18, 0);
        if (currentLevel == 1) return (35e18, 0);
        if (currentLevel == 2) return (30e18, 5e18);
        if (currentLevel == 3) return (40e18, 10e18);
        if (currentLevel == 4) return (50e18, 15e18);
        return (0, 0);
    }

    function getBaseUpgradeCost(uint8 currentLevel) external pure returns (uint256 wood, uint256 iron, uint256 wheat) {
        if (currentLevel == 1) return (40e18, 0, 20e18);
        if (currentLevel == 2) return (60e18, 5e18, 30e18);
        if (currentLevel == 3) return (80e18, 10e18, 40e18);
        if (currentLevel == 4) return (100e18, 15e18, 50e18);
        return (0, 0, 0);
    }

    function getMonumentUpgradeCost(uint8 currentLevel)
        external
        pure
        returns (uint256 wood, uint256 iron, uint256 wheat, uint256 blueprint)
    {
        if (currentLevel == 0) return (30e18, 0, 20e18, 0);
        if (currentLevel == 1) return (50e18, 0, 30e18, 0);
        if (currentLevel == 2) return (70e18, 5e18, 40e18, 0);
        if (currentLevel == 3) return (90e18, 10e18, 50e18, 0);
        if (currentLevel == 4) return (120e18, 15e18, 60e18, 0);
        if (currentLevel == 5) return (150e18, 20e18, 80e18, 0);
        if (currentLevel >= 6 && currentLevel < MONUMENT_MAX_LEVEL) return (200e18, 25e18, 100e18, 1e18);
        return (0, 0, 0, 0);
    }

    function getActionDuration(ActionType action) external pure returns (uint64) {
        if (
            action == ActionType.ChopWood || action == ActionType.MineIron || action == ActionType.FishDocks
                || action == ActionType.FishDeepSea || action == ActionType.HarvestWheat
        ) {
            return 4;
        }

        if (action == ActionType.DepositResources || action == ActionType.WithdrawResources) {
            return DEPOSIT_DURATION_TICKS;
        }

        if (
            action == ActionType.UpgradeWall || action == ActionType.UpgradeBase || action == ActionType.UpgradeMonument
        ) {
            return BUILDING_DURATION_TICKS;
        }

        if (action == ActionType.MarketBuy || action == ActionType.MarketSell) {
            return 1;
        }

        return 0;
    }

    function getTravelTicks(uint8 fromRegion, uint8 toRegion) external pure returns (uint64) {
        return uint64(LibTravel.travelTicksBetween(fromRegion, toRegion));
    }

    function getBandit(uint32 banditId) public view returns (BanditTroop memory) {
        BanditTroop memory bandit = LibStorage.appStorage().bandits[banditId];
        if (bandit.id == ClanWorldConstants.BANDIT_ID_NULL || bandit.state == BanditState.None) {
            return BanditTroop({
                id: 0,
                region: 0,
                state: BanditState.None,
                targetClanId: 0,
                tickEnteredState: 0,
                strength: 0,
                tier: 0,
                attackAttemptsMade: 0,
                carryWood: 0,
                carryIron: 0,
                carryWheat: 0,
                carryFish: 0,
                carryGold: 0
            });
        }
        return bandit;
    }

    function getBanditTroop(uint32 banditId) external view returns (BanditTroop memory) {
        return getBandit(banditId);
    }

    function getBanditsInRegion(uint8 region) external view returns (uint32[] memory) {
        return LibStorage.appStorage().banditsByRegion[region];
    }

    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        west = s.wheatPlots[clanId][0];
        east = s.wheatPlots[clanId][1];
    }

    function getScheduledMarketActionsForTick(uint64 tick) external view returns (ScheduledMarketAction[] memory) {
        return LibStorage.appStorage().scheduledMarketActions[tick];
    }

    function getActiveDefenders(uint32 targetClanId) external view returns (uint32[] memory clansmanIds) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint32[] storage defendingClans = s.defendingClansByRegion[s.clans[targetClanId].baseRegion];
        uint256 count = 0;

        for (uint256 i = 0; i < defendingClans.length; i++) {
            uint32[] storage clanClansmen = s.clanClansmanIds[defendingClans[i]];
            for (uint256 j = 0; j < clanClansmen.length; j++) {
                Mission storage mission = s.missions[clanClansmen[j]];
                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
                    count++;
                }
            }
        }

        clansmanIds = new uint32[](count);
        uint256 out = 0;
        for (uint256 i = 0; i < defendingClans.length; i++) {
            uint32[] storage clanClansmen = s.clanClansmanIds[defendingClans[i]];
            for (uint256 j = 0; j < clanClansmen.length; j++) {
                uint32 clansmanId = clanClansmen[j];
                Mission storage mission = s.missions[clansmanId];
                if (mission.active && mission.action == ActionType.DefendBase && mission.targetClanId == targetClanId) {
                    clansmanIds[out++] = clansmanId;
                }
            }
        }
    }

    function getDefendingClans(uint8 region) external view returns (uint32[] memory clanIds) {
        return LibStorage.appStorage().defendingClansByRegion[region];
    }

    function getClanIds() external view returns (uint32[] memory clanIds) {
        return LibStorage.appStorage().allClanIds;
    }

    function getClanClansmanIds(uint32 clanId) external view returns (uint32[] memory clansmanIds) {
        return LibStorage.appStorage().clanClansmanIds[clanId];
    }

    function getClansmanDefendingRegion(uint32 clansmanId) external view returns (uint8 region) {
        return LibStorage.appStorage().clansmanDefendingRegion[clansmanId];
    }

    function getMonumentLevelReachedAt(uint32 clanId, uint8 level) external view returns (uint64 reachedAtTick) {
        return LibStorage.appStorage().monumentLevelReachedAt[clanId][level];
    }

    function _poolForResourceType(uint8 resourceType) private view returns (address) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        if (resourceType == uint8(ResourceType.Wood)) return s.treasury.woodGoldPool;
        if (resourceType == uint8(ResourceType.Iron)) return s.treasury.ironGoldPool;
        if (resourceType == uint8(ResourceType.Wheat)) return s.treasury.wheatGoldPool;
        if (resourceType == uint8(ResourceType.Fish)) return s.treasury.fishGoldPool;
        revert("ClanWorld: invalid resource");
    }
}
