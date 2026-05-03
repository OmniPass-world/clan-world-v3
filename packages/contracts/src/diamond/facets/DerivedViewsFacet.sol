// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {DerivedClanState, DerivedClansmanState, Mission} from "../../IClanWorld.sol";
import {LibMission} from "../lib/LibMission.sol";
import {LibScoring} from "../../lib/LibScoring.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibStorage} from "../lib/LibStorage.sol";

/// @notice Future diamond home for simulation-heavy derived reads.
/// @dev Keep settlement simulation here once AppStorage migration is live. Pure
///      scoring helpers belong in LibScoring so the lens and facet share exact
///      score/loot semantics without copying tiny rule code.
contract DerivedViewsFacet {
    function getDerivedClanState(uint32 clanId) external view returns (DerivedClanState memory) {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, s.world.currentTick);
        return DerivedClanState({
            clan: sim.clan,
            isStarving: sim.clan.starvationStartsAtTick != 0 && sim.clan.starvationStartsAtTick <= s.world.currentTick,
            lootValue: LibScoring.lootValue(sim.clan),
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

        LibSettlement.SettlementSimulation memory sim =
            LibSettlement.simulateToTick(s, s.clansmen[clansmanId].clanId, s.world.currentTick);
        (bool found, uint256 index) = _findSimulatedClansman(sim, clansmanId);
        if (found) {
            return DerivedClansmanState({
                clansman: sim.clansmen[index],
                activeMission: sim.missions[index],
                effectiveRegion: LibMission.effectiveRegion(
                    sim.clansmen[index], sim.missions[index], s.world.currentTick
                ),
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

    function _findSimulatedClansman(LibSettlement.SettlementSimulation memory sim, uint32 clansmanId)
        private
        pure
        returns (bool found, uint256 index)
    {
        for (uint256 i = 0; i < sim.clansmen.length; i++) {
            if (sim.clansmen[i].clansmanId == clansmanId) {
                return (true, i);
            }
        }
    }
}
