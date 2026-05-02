// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ActionType, Clan, Clansman, Mission, WheatPlot} from "../../IClanWorld.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract RawClanViewsFacet {
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

    function getWheatPlots(uint32 clanId) external view returns (WheatPlot memory west, WheatPlot memory east) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        west = s.wheatPlots[clanId][0];
        east = s.wheatPlots[clanId][1];
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
}
