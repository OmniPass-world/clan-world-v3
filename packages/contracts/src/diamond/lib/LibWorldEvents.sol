// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanWorldConstants, WheatPlot, WheatPlotState} from "../../IClanWorld.sol";
import {LibSeason} from "./LibSeason.sol";
import {LibStorage} from "./LibStorage.sol";

library LibWorldEvents {
    uint256 private constant MAX_CROP_TRANSITION_PER_TICK = 48;

    event WinterStarted(uint64 indexed tick);
    event WinterEnded(uint64 indexed tick);

    function resolveWorldEvents(LibStorage.AppStorage storage s, uint64 closedTick) internal {
        uint64 newTick = closedTick + 1;

        if (newTick >= s.world.seasonEndTick && s.world.seasonFinalized) {
            rollSeason(s);
        }

        bool wasWinter = LibSeason.isWinterActiveAt(closedTick);
        bool nowWinter = LibSeason.isWinterActiveAt(newTick);
        if (!wasWinter && nowWinter) {
            lockWheatPlotsForWinter(s);
            emit WinterStarted(newTick);
        }
        if (wasWinter && !nowWinter) {
            restartWheatPlotsAfterWinter(s, newTick);
            emit WinterEnded(newTick);
        }
    }

    function rollSeason(LibStorage.AppStorage storage s) internal {
        s.world.currentSeasonNumber += 1;
        s.world.seasonStartTick = s.world.seasonEndTick;
        s.world.seasonEndTick = s.world.seasonStartTick + ClanWorldConstants.SEASON_DURATION_TICKS;
        s.world.seasonFinalized = false;
    }

    function lockWheatPlotsForWinter(LibStorage.AppStorage storage s) private {
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

    function restartWheatPlotsAfterWinter(LibStorage.AppStorage storage s, uint64 currentTick) private {
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
