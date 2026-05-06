// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clan, LeaderboardEntry, WorldSnapshot} from "../../IClanWorld.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibSeason} from "../lib/LibSeason.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract SnapshotViewsFacet {
    function getWorldSnapshot() external view returns (WorldSnapshot memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LeaderboardEntry[] memory leaderboard = new LeaderboardEntry[](s.allClanIds.length);
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            Clan storage clan = s.clans[clanId];
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

        (bool winterActive, uint64 winterStartsAtTick, uint64 winterEndsAtTick) =
            LibSeason.winterWindowForTick(s.world.currentTick);

        return WorldSnapshot({
            currentTick: s.world.currentTick,
            seasonStartTick: s.world.seasonStartTick,
            seasonEndTick: s.world.seasonEndTick,
            seasonFinalized: s.world.seasonFinalized,
            currentSeasonNumber: s.world.currentSeasonNumber,
            nextHeartbeatAtTick: s.world.nextHeartbeatAtTick,
            worldPaused: s.world.worldPaused,
            pausedAtTs: s.world.pausedAtTs,
            winterActive: winterActive,
            winterStartsAtTick: winterStartsAtTick,
            winterEndsAtTick: winterEndsAtTick,
            activeBanditId: s.world.activeBanditId,
            currentTickSeed: s.world.currentTickSeed,
            leaderboard: leaderboard
        });
    }
}
