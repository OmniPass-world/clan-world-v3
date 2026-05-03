// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ActionType, ClansmanState, Mission, RegionOccupant} from "../../IClanWorld.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract RegionViewsFacet {
    function getRegionPopulation(uint8 region) external view returns (RegionOccupant[] memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint256 count = 0;
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            uint32[] storage clansmanIds = s.clanClansmanIds[clanId];
            for (uint256 j = 0; j < clansmanIds.length; j++) {
                uint32 clansmanId = clansmanIds[j];
                if (
                    s.clansmen[clansmanId].state != ClansmanState.DEAD && s.clansmen[clansmanId].currentRegion == region
                ) {
                    count++;
                }
            }
        }

        RegionOccupant[] memory occupants = new RegionOccupant[](count);
        uint256 out = 0;
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            uint32[] storage clansmanIds = s.clanClansmanIds[clanId];
            for (uint256 j = 0; j < clansmanIds.length; j++) {
                uint32 clansmanId = clansmanIds[j];
                if (
                    s.clansmen[clansmanId].state != ClansmanState.DEAD && s.clansmen[clansmanId].currentRegion == region
                ) {
                    Mission storage mission = s.missions[clansmanId];
                    occupants[out++] = RegionOccupant({
                        clansmanId: clansmanId,
                        clanId: clanId,
                        state: s.clansmen[clansmanId].state,
                        currentAction: mission.active ? mission.action : ActionType.None,
                        missionNonce: s.clansmen[clansmanId].lastMissionNonce
                    });
                }
            }
        }
        return occupants;
    }
}
