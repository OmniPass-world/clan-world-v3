// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {
    ActionType,
    ClanFullView,
    ClansmanFullView,
    ClansmanState,
    DerivedClanState,
    DerivedClansmanState,
    Mission,
    WheatPlot
} from "../../IClanWorld.sol";
import {LibMission} from "../lib/LibMission.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract ClanFullViewFacet {
    function getClanFullView(uint32 clanId) external view returns (ClanFullView memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        DerivedClanState memory derivedClan = DerivedClanState({
            clan: s.clans[clanId],
            isStarving: s.clans[clanId].starvationStartsAtTick != 0
                && s.clans[clanId].starvationStartsAtTick <= s.world.currentTick,
            lootValue: LibScoring.lootValue(s.clans[clanId]),
            derivedAtTick: s.world.currentTick
        });

        uint32[] storage clansmanIds = s.clanClansmanIds[clanId];
        ClansmanFullView[] memory clansmen = new ClansmanFullView[](clansmanIds.length);
        uint32 thisClanDefendingBaseId = 0;

        for (uint256 i = 0; i < clansmanIds.length; i++) {
            uint32 clansmanId = clansmanIds[i];
            Mission memory mission = s.missions[clansmanId];
            DerivedClansmanState memory dcs = DerivedClansmanState({
                clansman: s.clansmen[clansmanId],
                activeMission: mission,
                effectiveRegion: LibMission.effectiveRegion(s.clansmen[clansmanId], mission, s.world.currentTick),
                derivedAtTick: s.world.currentTick
            });
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
                uint8 region = s.clansmanDefendingRegion[clansmanIds[i]];
                if (region != 0) {
                    thisClanDefendingBaseId = region;
                    break;
                }
            }
        }

        WheatPlot memory westPlot = s.wheatPlots[clanId][0];
        WheatPlot memory eastPlot = s.wheatPlots[clanId][1];
        return ClanFullView({
            clan: derivedClan,
            clansmen: clansmen,
            westPlot: westPlot,
            eastPlot: eastPlot,
            incomingDefenderIds: s.defendingClansByRegion[derivedClan.clan.baseRegion],
            thisClanDefendingBaseId: thisClanDefendingBaseId
        });
    }
}
