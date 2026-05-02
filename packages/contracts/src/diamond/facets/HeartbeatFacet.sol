// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanState, ClanWorldConstants, IClanWorldEvents, WheatPlot, WheatPlotState} from "../../IClanWorld.sol";
import {LibSeason} from "../lib/LibSeason.sol";
import {LibSettlement} from "../lib/LibSettlement.sol";
import {LibStorage} from "../lib/LibStorage.sol";

contract HeartbeatFacet is IClanWorldEvents {
    uint256 private constant MAX_CROP_TRANSITION_PER_TICK = 64;

    function heartbeat() external {
        LibStorage.AppStorage storage s = LibStorage.appStorage();
        LibStorage.enterNonReentrant(s);
        require(block.timestamp >= s.world.nextHeartbeatAtTs, "ClanWorld: heartbeat rate limited");

        if (_isSeasonFinalizationPending(s)) {
            LibStorage.exitNonReentrant(s);
            return;
        }

        uint64 closedTick = s.world.currentTick;
        bytes32 closedTickSeed = s.world.currentTickSeed;
        s.world.nextHeartbeatAtTs = uint64(block.timestamp) + ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;

        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            if (s.clans[clanId].clanState != ClanState.DEAD && s.clans[clanId].lastSettledTick <= closedTick) {
                LibSettlement.SettlementSimulation memory sim = LibSettlement.simulateToTick(s, clanId, closedTick + 1);
                LibSettlement.commitSimulation(s, sim);
            }
        }

        _resolveWorldEvents(s, closedTick);

        uint64 newTick = closedTick + 1;
        bytes32 newSeed = keccak256(abi.encode(block.prevrandao, closedTickSeed, closedTick));
        s.world.currentTick = newTick;
        s.tickSeeds[newTick] = newSeed;
        s.world.currentTickSeed = newSeed;
        s.world.nextHeartbeatAtTick = newTick + 1;

        emit TickAdvanced(closedTick, newTick, newSeed);
        LibStorage.exitNonReentrant(s);
    }

    function _isSeasonFinalizationPending(LibStorage.AppStorage storage s) private view returns (bool) {
        return s.world.currentTick + 1 >= s.world.seasonEndTick && !s.world.seasonFinalized;
    }

    function _resolveWorldEvents(LibStorage.AppStorage storage s, uint64 closedTick) private {
        uint64 newTick = closedTick + 1;

        if (newTick >= s.world.seasonEndTick && s.world.seasonFinalized) {
            s.world.currentSeasonNumber += 1;
            s.world.seasonStartTick = s.world.seasonEndTick;
            s.world.seasonEndTick = s.world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
            s.world.seasonFinalized = false;
        }

        bool wasWinter = LibSeason.isWinterActiveAt(closedTick);
        bool nowWinter = LibSeason.isWinterActiveAt(newTick);
        if (!wasWinter && nowWinter) {
            _lockWheatPlotsForWinter(s);
            emit WinterStarted(newTick);
        }
        if (wasWinter && !nowWinter) {
            _restartWheatPlotsAfterWinter(s, newTick);
            emit WinterEnded(newTick);
        }
    }

    function _lockWheatPlotsForWinter(LibStorage.AppStorage storage s) private {
        uint256 transitions;
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            for (uint256 pi = 0; pi < 2; pi++) {
                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
                WheatPlot storage plot = s.wheatPlots[clanId][pi];
                plot.state = WheatPlotState.WinterLocked;
                plot.remainingWheat = 0;
                plot.regrowUntilTick = 0;
                transitions++;
            }
        }
    }

    function _restartWheatPlotsAfterWinter(LibStorage.AppStorage storage s, uint64 currentTick) private {
        uint256 transitions;
        for (uint256 i = 0; i < s.allClanIds.length; i++) {
            uint32 clanId = s.allClanIds[i];
            for (uint256 pi = 0; pi < 2; pi++) {
                require(transitions < MAX_CROP_TRANSITION_PER_TICK, "ClanWorld: crop transition cap");
                WheatPlot storage plot = s.wheatPlots[clanId][pi];
                if (plot.state == WheatPlotState.WinterLocked) {
                    plot.state = WheatPlotState.Regrowing;
                    plot.remainingWheat = 0;
                    plot.regrowUntilTick = currentTick + ClanWorldConstants.WHEAT_PLOT_REGROW_TICKS;
                }
                transitions++;
            }
        }
    }
}
