// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    TreasuryState,
    Clan,
    Clansman,
    Mission,
    WheatPlot,
    BanditTroop,
    ScheduledMarketAction
} from "../../IClanWorld.sol";

library LibStorage {
    bytes32 internal constant APP_STORAGE_SLOT = keccak256("clan.world.app.storage.v1");

    uint256 internal constant NOT_ENTERED = 1;
    uint256 internal constant ENTERED = 2;

    /// @dev Mirrors ClanWorld.sol's stored world fields, excluding derived view fields.
    struct StoredWorldState {
        uint64 currentTick;
        uint64 seasonStartTick;
        uint64 seasonEndTick;
        bool seasonFinalized;
        uint64 currentSeasonNumber;
        uint64 nextHeartbeatAtTick;
        uint64 nextHeartbeatAtTs;
        uint64 nextBanditSpawnEligibleTick;
        uint16 currentBanditSpawnChanceBps;
        bytes32 currentTickSeed;
        uint32 activeBanditId;
        uint64 nextCommitSequence;
    }

    struct BanditSpawnState {
        uint64 lastSpawnTick;
        uint16 probabilityAccum;
    }

    struct WallUpgradeReservation {
        bool active;
        uint32 clanId;
        uint64 missionNonce;
        uint8 fromLevel;
        uint8 toLevel;
        uint256 woodCost;
        uint256 ironCost;
    }

    struct BaseUpgradeReservation {
        bool active;
        uint32 clanId;
        uint64 missionNonce;
        uint8 fromLevel;
        uint8 toLevel;
        uint256 woodCost;
        uint256 ironCost;
        uint256 wheatCost;
    }

    struct MonumentUpgradeReservation {
        bool active;
        uint32 clanId;
        uint64 missionNonce;
        uint8 fromLevel;
        uint8 toLevel;
        uint256 woodCost;
        uint256 ironCost;
        uint256 wheatCost;
        uint256 blueprintCost;
    }

    struct AppStorage {
        uint256 reentrancyStatus;
        bool initialized;
        StoredWorldState world;
        TreasuryState treasury;
        mapping(uint32 => Clan) clans;
        mapping(uint32 => Clansman) clansmen;
        mapping(uint32 => Mission) missions;
        mapping(uint32 => WheatPlot[2]) wheatPlots;
        mapping(uint64 => ScheduledMarketAction[]) scheduledMarketActions;
        mapping(uint32 => uint64) marketMissionCommitSequence;
        mapping(uint8 => uint32[]) defendingClansByRegion;
        mapping(uint8 => mapping(uint32 => uint256)) defenderCountByRegionClan;
        mapping(uint32 => uint8) clansmanDefendingRegion;
        mapping(uint32 => BanditTroop) bandits;
        mapping(uint8 => uint32[]) banditsByRegion;
        mapping(uint8 => BanditSpawnState) banditSpawnByRegion;
        mapping(uint64 => bytes32) tickSeeds;
        mapping(uint32 => WallUpgradeReservation) wallUpgradeReservations;
        mapping(uint32 => uint8) pendingWallUpgradesByClan;
        mapping(uint32 => BaseUpgradeReservation) baseUpgradeReservations;
        mapping(uint32 => uint8) pendingBaseUpgradesByClan;
        mapping(uint32 => MonumentUpgradeReservation) monumentUpgradeReservations;
        mapping(uint32 => uint8) pendingMonumentUpgradesByClan;
        mapping(uint32 => uint256) reservedWoodByClan;
        mapping(uint32 => uint256) reservedIronByClan;
        mapping(uint32 => uint256) reservedWheatByClan;
        mapping(uint32 => uint256) reservedBlueprintByClan;
        mapping(uint32 => mapping(uint8 => uint64)) monumentLevelReachedAt;
        uint32 nextClanId;
        uint32 nextClansmanId;
        uint32 nextBanditId;
        uint32 activeBanditCount;
        uint32[] allClanIds;
        mapping(uint32 => uint32[]) clanClansmanIds;
        uint64 heartbeatIntervalSeconds;
    }

    error ReentrantCall();

    function appStorage() internal pure returns (AppStorage storage s) {
        bytes32 slot = APP_STORAGE_SLOT;
        assembly {
            s.slot := slot
        }
    }

    function enterNonReentrant(AppStorage storage s) internal {
        if (s.reentrancyStatus == ENTERED) {
            revert ReentrantCall();
        }
        s.reentrancyStatus = ENTERED;
    }

    function exitNonReentrant(AppStorage storage s) internal {
        s.reentrancyStatus = NOT_ENTERED;
    }
}
