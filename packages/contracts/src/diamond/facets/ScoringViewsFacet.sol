// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanState} from "../../IClanWorld.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract ScoringViewsFacet {
    uint256 internal constant MAX_CLAN_SCAN_FOR_RANKING = 24;

    function quoteLootValueSettled(uint32 clanId) external view returns (uint256 lootValue) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        return LibScoring.lootValue(sim.clan);
    }

    function getClanScore(uint32 clanId)
        external
        view
        returns (uint256 score, uint64 monumentReachedAtTick, uint8 monumentLevel)
    {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        return _getClanScoreFromSimulation(s, clanId, sim);
    }

    function getRankings() external view returns (uint32[] memory clanIdsRanked, uint256[] memory scores) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        uint256 scanCount = s.allClanIds.length;
        if (scanCount > MAX_CLAN_SCAN_FOR_RANKING) {
            scanCount = MAX_CLAN_SCAN_FOR_RANKING;
        }

        uint32[] memory tempClanIds = new uint32[](scanCount);
        uint256[] memory tempScores = new uint256[](scanCount);
        uint256 liveCount;

        for (uint256 i = 0; i < scanCount; i++) {
            uint32 clanId = s.allClanIds[i];
            LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
            if (sim.clan.clanState != ClanState.ACTIVE) continue;

            (uint256 score,,) = _getClanScoreFromSimulation(s, clanId, sim);
            tempClanIds[liveCount] = clanId;
            tempScores[liveCount] = score;
            liveCount++;
        }

        for (uint256 i = 1; i < liveCount; i++) {
            uint32 keyClanId = tempClanIds[i];
            uint256 keyScore = tempScores[i];
            uint256 j = i;
            while (j > 0 && LibScoring.rankingComesAfter(tempClanIds[j - 1], tempScores[j - 1], keyClanId, keyScore)) {
                tempClanIds[j] = tempClanIds[j - 1];
                tempScores[j] = tempScores[j - 1];
                j--;
            }
            tempClanIds[j] = keyClanId;
            tempScores[j] = keyScore;
        }

        clanIdsRanked = new uint32[](liveCount);
        scores = new uint256[](liveCount);
        for (uint256 i = 0; i < liveCount; i++) {
            clanIdsRanked[i] = tempClanIds[i];
            scores[i] = tempScores[i];
        }
    }

    function _getClanScoreFromSimulation(
        LibStorage.AppStorage storage s,
        uint32 clanId,
        LibSettlement.SettlementSimulation memory sim
    ) private view returns (uint256 score, uint64 monumentReachedAtTick, uint8 monumentLevel) {
        monumentLevel = sim.clan.monumentLevel;
        if (monumentLevel > 0) {
            monumentReachedAtTick = s.monumentLevelReachedAt[clanId][monumentLevel];
            if (monumentReachedAtTick == 0) {
                monumentReachedAtTick = sim.simMonumentReachedAt[monumentLevel];
            }
        }

        return LibScoring.packClanScore(sim.clan, monumentReachedAtTick, monumentLevel);
    }
}
