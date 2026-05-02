// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {Clansman, ClansmanState, Mission} from "../../IClanWorld.sol";

library LibMission {
    function effectiveRegion(Clansman memory cs, Mission memory mission, uint64 tick) internal pure returns (uint8) {
        if (cs.state == ClansmanState.TRAVELING && mission.active) {
            return tick >= mission.arrivalTick ? mission.targetRegion : mission.startRegion;
        }
        return cs.currentRegion;
    }
}
