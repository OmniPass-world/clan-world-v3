// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

import {ClanWorldConstants} from "../../IClanWorld.sol";
import {LibStorage} from "./LibStorage.sol";

library LibWorldClock {
    function heartbeatIntervalSeconds(LibStorage.AppStorage storage s) internal view returns (uint64) {
        uint64 configuredIntervalSeconds = s.heartbeatIntervalSeconds;
        if (configuredIntervalSeconds == 0) {
            return ClanWorldConstants.HEARTBEAT_INTERVAL_SECONDS;
        }
        return configuredIntervalSeconds;
    }

    function scheduleNextHeartbeat(LibStorage.AppStorage storage s) internal {
        s.world.nextHeartbeatAtTs = uint64(block.timestamp) + heartbeatIntervalSeconds(s);
    }
}
