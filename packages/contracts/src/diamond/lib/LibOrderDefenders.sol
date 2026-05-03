// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {LibStorage} from "./LibStorage.sol";

library LibOrderDefenders {
    function registerDefender(LibStorage.AppStorage storage s, uint8 region, uint32 clanId, uint32 clansmanId) public {
        if (s.clansmanDefendingRegion[clansmanId] == region) return;
        clearDefender(s, clansmanId);
        if (s.defenderCountByRegionClan[region][clanId] == 0) s.defendingClansByRegion[region].push(clanId);
        s.defenderCountByRegionClan[region][clanId]++;
        s.clansmanDefendingRegion[clansmanId] = region;
    }

    function clearDefender(LibStorage.AppStorage storage s, uint32 clansmanId) public {
        uint8 region = s.clansmanDefendingRegion[clansmanId];
        if (region == 0) return;
        uint32 clanId = s.clansmen[clansmanId].clanId;
        uint256 count = s.defenderCountByRegionClan[region][clanId];
        if (count > 1) {
            s.defenderCountByRegionClan[region][clanId] = count - 1;
        } else {
            delete s.defenderCountByRegionClan[region][clanId];
            uint32[] storage clans = s.defendingClansByRegion[region];
            for (uint256 i = 0; i < clans.length; i++) {
                if (clans[i] == clanId) {
                    clans[i] = clans[clans.length - 1];
                    clans.pop();
                    break;
                }
            }
        }
        delete s.clansmanDefendingRegion[clansmanId];
    }
}
