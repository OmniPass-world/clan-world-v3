// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanWorldConstants} from "../../IClanWorld.sol";

library LibSeason {
    function isWinterActiveAt(uint64 tick) internal pure returns (bool) {
        if (tick < ClanWorldConstants.WINTER_START_TICK) {
            return false;
        }
        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
        return elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
    }

    function winterWindowForTick(uint64 tick)
        internal
        pure
        returns (bool active, uint64 startsAtTick, uint64 endsAtTick)
    {
        if (tick < ClanWorldConstants.WINTER_START_TICK) {
            startsAtTick = ClanWorldConstants.WINTER_START_TICK;
            endsAtTick = ClanWorldConstants.WINTER_START_TICK + ClanWorldConstants.WINTER_DURATION_TICKS;
            return (false, startsAtTick, endsAtTick);
        }

        uint64 elapsed = tick - ClanWorldConstants.WINTER_START_TICK;
        uint64 cycleIndex = elapsed / ClanWorldConstants.WINTER_PERIOD_TICKS;
        uint64 cycleStart = ClanWorldConstants.WINTER_START_TICK + cycleIndex * ClanWorldConstants.WINTER_PERIOD_TICKS;
        active = elapsed % ClanWorldConstants.WINTER_PERIOD_TICKS < ClanWorldConstants.WINTER_DURATION_TICKS;
        startsAtTick = active ? cycleStart : cycleStart + ClanWorldConstants.WINTER_PERIOD_TICKS;
        endsAtTick = startsAtTick + ClanWorldConstants.WINTER_DURATION_TICKS;
    }
}
