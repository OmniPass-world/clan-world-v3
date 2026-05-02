// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {DerivedClanState, DerivedClansmanState, Mission} from "../../IClanWorld.sol";
import {LibMission} from "../lib/LibMission.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibStorage} from "../lib/LibStorage.sol";

/// @notice Future diamond home for simulation-heavy derived reads.
/// @dev Keep settlement simulation here once AppStorage migration is live. Pure
///      scoring helpers belong in LibScoring so the lens and facet share exact
///      score/loot semantics without copying tiny rule code.
contract DerivedViewsFacet {
    function derivedViewsFacetVersion() external pure returns (bytes4) {
        return bytes4(keccak256("DerivedViewsFacet.v1"));
    }

    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        return DerivedClanState({
            clan: s.clans[clanId],
            isStarving: s.clans[clanId].starvationStartsAtTick != 0
                && s.clans[clanId].starvationStartsAtTick <= s.world.currentTick,
            lootValue: LibScoring.lootValue(s.clans[clanId]),
            derivedAtTick: s.world.currentTick
        });
    }

    function getDerivedClansmanState(uint32 clansmanId) external view returns (DerivedClansmanState memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        Mission memory mission = s.missions[clansmanId];
        if (s.clansmen[clansmanId].clansmanId == 0) {
            return DerivedClansmanState({
                clansman: s.clansmen[clansmanId],
                activeMission: mission,
                effectiveRegion: 0,
                derivedAtTick: s.world.currentTick
            });
        }

        return DerivedClansmanState({
            clansman: s.clansmen[clansmanId],
            activeMission: mission,
            effectiveRegion: LibMission.effectiveRegion(s.clansmen[clansmanId], mission, s.world.currentTick),
            derivedAtTick: s.world.currentTick
        });
    }
}
